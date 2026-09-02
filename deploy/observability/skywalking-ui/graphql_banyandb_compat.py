#!/usr/bin/env python3
"""SkyWalking UI 10.4 + BanyanDB GraphQL compatibility proxy.

Official booster-ui 10.4 still falls back to queryBasicTraces / queryTrace (v1).
BanyanDB 0.10 rejects those with:
  UnsupportedOperationException: BanyanDB Trace Model changed, please use queryTraces

This proxy translates v1 list/detail queries to queryTraces (v2) and reshapes
the response so the existing UI can render the Trace tab.
"""

from __future__ import annotations

import json
import os
import re
import traceback
from datetime import datetime, timedelta, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.error import HTTPError
from urllib.request import Request, urlopen

OAP_URL = os.environ.get("OAP_URL", "http://skywalking-oap:12800").rstrip("/")
LISTEN_HOST = os.environ.get("LISTEN_HOST", "0.0.0.0")
LISTEN_PORT = int(os.environ.get("LISTEN_PORT", "12800"))

QUERY_TRACES = """
query CompatQueryTraces($condition: TraceQueryCondition) {
  queryTraces(condition: $condition) {
    traces {
      spans {
        traceId
        segmentId
        spanId
        parentSpanId
        refs { traceId parentSegmentId parentSpanId type }
        serviceCode
        serviceInstanceName
        startTime
        endTime
        endpointName
        type
        peer
        component
        isError
        layer
        tags { key value }
        logs { time data { key value } }
        attachedEvents {
          startTime { seconds nanos }
          event
          endTime { seconds nanos }
          tags { key value }
          summary { key value }
        }
      }
    }
  }
}
"""

_QUERY_TRACE_CALL = re.compile(r"\bqueryTrace\s*\(")


def _forward(path: str, method: str, headers: dict, body: bytes | None) -> tuple[int, dict, bytes]:
    url = OAP_URL + path
    req_headers = {
        key: value
        for key, value in headers.items()
        if key.lower() not in {"host", "content-length", "connection", "transfer-encoding"}
    }
    request = Request(url, data=body, headers=req_headers, method=method)
    try:
        with urlopen(request, timeout=60) as resp:
            return resp.status, dict(resp.headers.items()), resp.read()
    except HTTPError as exc:
        return exc.code, dict(exc.headers.items() if exc.headers else {}), exc.read() or b""


def _oap_graphql(payload: dict) -> dict:
    raw = json.dumps(payload).encode("utf-8")
    status, _, body = _forward(
        "/graphql",
        "POST",
        {"Content-Type": "application/json", "Accept": "application/json"},
        raw,
    )
    try:
        parsed = json.loads(body.decode("utf-8"))
    except json.JSONDecodeError:
        return {"errors": [{"message": f"OAP returned non-JSON (HTTP {status})"}]}
    return parsed


def _root_span(spans: list[dict]) -> dict | None:
    if not spans:
        return None
    for span in spans:
        if span.get("parentSpanId") == -1 and not span.get("refs"):
            return span
    return spans[0]


def _brief_traces(v2: dict) -> dict:
    if v2.get("errors"):
        return v2
    rows = []
    for item in ((v2.get("data") or {}).get("queryTraces") or {}).get("traces") or []:
        spans = item.get("spans") or []
        root = _root_span(spans)
        if not root:
            continue
        start = int(root.get("startTime") or 0)
        end = int(root.get("endTime") or start)
        trace_ids = []
        seen = set()
        for span in spans:
            tid = span.get("traceId")
            if tid and tid not in seen:
                seen.add(tid)
                trace_ids.append(tid)
        rows.append(
            {
                "key": root.get("segmentId") or (trace_ids[0] if trace_ids else ""),
                "endpointNames": [root["endpointName"]] if root.get("endpointName") else [],
                "duration": max(0, end - start),
                "start": start,
                "isError": any(bool(span.get("isError")) for span in spans),
                "traceIds": trace_ids,
            }
        )
    return {"data": {"data": {"traces": rows}}}


def _trace_detail(v2: dict) -> dict:
    if v2.get("errors"):
        return v2
    spans: list[dict] = []
    for item in ((v2.get("data") or {}).get("queryTraces") or {}).get("traces") or []:
        spans.extend(item.get("spans") or [])
    return {"data": {"trace": {"spans": spans}}}


def _default_duration() -> dict:
    end = datetime.now(timezone.utc)
    start = end - timedelta(hours=24)
    return {
        "start": start.strftime("%Y-%m-%d %H%M"),
        "end": end.strftime("%Y-%m-%d %H%M"),
        "step": "MINUTE",
    }


def _v2_condition(variables: dict, extra: dict | None = None) -> dict:
    condition = dict(variables.get("condition") or {})
    if extra:
        condition.update(extra)
    if "traceState" not in condition:
        condition["traceState"] = "ALL"
    if "queryOrder" not in condition:
        condition["queryOrder"] = "BY_START_TIME"
    if "paging" not in condition:
        condition["paging"] = {"pageNum": 1, "pageSize": 20}
    if "queryDuration" not in condition:
        condition["queryDuration"] = _default_duration()
    return {"condition": condition}


def _rewrite(payload: dict) -> dict | None:
    query = payload.get("query") or ""
    variables = payload.get("variables") or {}

    if "queryBasicTraces" in query:
        return _brief_traces(_oap_graphql({"query": QUERY_TRACES, "variables": _v2_condition(variables)}))

    if "queryTraces" in query or "hasQueryTracesV2Support" in query:
        return None

    if _QUERY_TRACE_CALL.search(query):
        extra = {}
        trace_id = variables.get("traceId")
        if trace_id:
            extra["traceId"] = trace_id
        if variables.get("duration") and "queryDuration" not in (variables.get("condition") or {}):
            extra["queryDuration"] = variables["duration"]
        return _trace_detail(_oap_graphql({"query": QUERY_TRACES, "variables": _v2_condition(variables, extra)}))

    return None


class Handler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def log_message(self, fmt: str, *args) -> None:
        print("%s - %s" % (self.address_string(), fmt % args), flush=True)

    def _write(self, status: int, headers: dict, body: bytes) -> None:
        self.send_response(status)
        skip = {"transfer-encoding", "connection", "content-length"}
        for key, value in headers.items():
            if key.lower() not in skip:
                self.send_header(key, value)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_HEAD(self) -> None:  # noqa: N802
        if self.path.startswith("/healthcheck"):
            self._write(200, {"Content-Type": "text/plain"}, b"")
            return
        status, headers, _ = _forward(self.path, "HEAD", dict(self.headers), None)
        self._write(status, headers, b"")

    def do_GET(self) -> None:  # noqa: N802
        if self.path.startswith("/healthcheck"):
            self._write(200, {"Content-Type": "text/plain"}, b"ok")
            return
        status, headers, body = _forward(self.path, "GET", dict(self.headers), None)
        self._write(status, headers, body)

    def do_POST(self) -> None:  # noqa: N802
        length = int(self.headers.get("Content-Length") or 0)
        raw = self.rfile.read(length) if length else b""
        if self.path.startswith("/graphql"):
            try:
                payload = json.loads(raw.decode("utf-8") or "{}")
                rewritten = _rewrite(payload)
            except Exception:
                traceback.print_exc()
                rewritten = None
            if rewritten is not None:
                body = json.dumps(rewritten).encode("utf-8")
                self._write(200, {"Content-Type": "application/json"}, body)
                return
        status, headers, body = _forward(self.path, "POST", dict(self.headers), raw)
        self._write(status, headers, body)


def main() -> None:
    server = ThreadingHTTPServer((LISTEN_HOST, LISTEN_PORT), Handler)
    print(f"skywalking graphql compat proxy on {LISTEN_HOST}:{LISTEN_PORT} -> {OAP_URL}", flush=True)
    server.serve_forever()


if __name__ == "__main__":
    main()
