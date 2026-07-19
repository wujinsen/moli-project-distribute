from __future__ import annotations

import json
from http.server import BaseHTTPRequestHandler, HTTPServer
import threading

import pytest

from deep_research.models import ResearchOptions, ResearchSidecarRequest
from deep_research.orchestrator import run_research


class _Handler(BaseHTTPRequestHandler):
    calls: list[dict] = []

    def log_message(self, format, *args):  # noqa: A003
        return

    def do_POST(self):  # noqa: N802
        length = int(self.headers.get("Content-Length", "0"))
        raw = self.rfile.read(length).decode("utf-8")
        payload = json.loads(raw)
        self.__class__.calls.append(payload)
        slug = "develop/用户中心" if len(self.__class__.calls) % 2 else "develop/网关"
        body = {
            "code": 200,
            "data": {
                "mode": "retrieval",
                "citations": [
                    {
                        "slug": slug,
                        "title": slug,
                        "snippet": "mock snippet",
                        "docId": 1,
                    }
                ],
            },
        }
        encoded = json.dumps(body).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)


@pytest.fixture()
def mock_kb_server(monkeypatch):
    _Handler.calls = []
    server = HTTPServer(("127.0.0.1", 0), _Handler)
    port = server.server_address[1]
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    base = f"http://127.0.0.1:{port}"
    monkeypatch.setenv("KB_BASE_URL", base)
    monkeypatch.setattr("deep_research.kb_client.KB_BASE_URL", base)
    try:
        yield
    finally:
        server.shutdown()


def _request(topic: str = "茉莉微服务架构") -> ResearchSidecarRequest:
    return ResearchSidecarRequest(
        runId="test-run",
        topic=topic,
        spaceId=900000000000000003,
        authToken="Bearer test",
        options=ResearchOptions(
            maxSections=3,
            maxRetrieveRounds=1,
            latencyBudgetMs=120_000,
            topK=4,
            perSectionTopK=4,
            coverageThreshold=0.75,
        ),
    )


def test_orchestrator_phase_b_produces_report_md(mock_kb_server):
    result = run_research(_request())
    assert result.report_md
    assert "type: output" in result.report_md
    assert "[[" in result.report_md
    assert result.citations
    assert result.status in ("SUCCEEDED", "DEGRADED")
    assert any(evt.phase in ("writer", "reviewer") for evt in result.progress)


def test_orchestrator_degrades_on_tight_budget(mock_kb_server, monkeypatch):
    from deep_research import orchestrator as orch

    real_time = orch.time.time

    start = [real_time()]

    def fake_time():
        if not hasattr(fake_time, "calls"):
            fake_time.calls = 0
        fake_time.calls += 1
        if fake_time.calls <= 2:
            return start[0]
        return start[0] + 999

    monkeypatch.setattr(orch.time, "time", fake_time)
    req = _request()
    req.options.latency_budget_ms = 1000
    result = run_research(req)
    assert result.degraded is True
    assert result.degrade_reason == "BUDGET"
    assert result.report_md
    assert "降级摘要" in result.report_md
