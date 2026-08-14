from __future__ import annotations

import json
from http.server import BaseHTTPRequestHandler, HTTPServer
import threading

import pytest

from deep_research.kb_client import KbRestClient
from deep_research.retriever import merge_hits, retrieve_section
from deep_research.models import EvidenceHit


class _Handler(BaseHTTPRequestHandler):
    calls: list[dict] = []

    def log_message(self, format, *args):  # noqa: A003
        return

    def do_POST(self):  # noqa: N802
        length = int(self.headers.get("Content-Length", "0"))
        raw = self.rfile.read(length).decode("utf-8")
        payload = json.loads(raw)
        self.__class__.calls.append(
            {
                "path": self.path,
                "authorization": self.headers.get("Authorization"),
                "payload": payload,
            }
        )
        body = {
            "code": 200,
            "data": {
                "mode": "retrieval",
                "citations": [
                    {
                        "slug": "develop/用户中心",
                        "title": "用户中心",
                        "snippet": "用户中心服务说明",
                        "docId": 101,
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
def mock_kb_server():
    _Handler.calls = []
    server = HTTPServer(("127.0.0.1", 0), _Handler)
    port = server.server_address[1]
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        yield f"http://127.0.0.1:{port}", _Handler.calls
    finally:
        server.shutdown()


def test_kb_client_passes_use_llm_false_and_auth(mock_kb_server):
    base_url, calls = mock_kb_server
    client = KbRestClient(base_url=base_url, auth_token="Bearer test-token")
    data = client.ask(
        "网关路由",
        space_ids=[900000000000000003],
        top_k=5,
        retrieval_strategy="hybrid",
        use_llm=False,
    )
    assert data["mode"] == "retrieval"
    assert len(calls) == 1
    call = calls[0]
    assert call["path"] == "/kb/ask"
    assert call["authorization"] == "Bearer test-token"
    assert call["payload"]["useLlm"] is False
    assert call["payload"]["spaceIds"] == [900000000000000003]
    assert call["payload"]["retrievalStrategy"] == "hybrid"


def test_retriever_dedupes_slug_and_caps_queries(mock_kb_server):
    base_url, calls = mock_kb_server
    client = KbRestClient(base_url=base_url, auth_token="token")
    section = {
        "id": "s1",
        "retrieveQueries": ["q1", "q2", "q3", "q4", "q5"],
    }
    hits, used = retrieve_section(
        section,
        client,
        space_id=900000000000000003,
        space_ids=None,
        top_k=8,
        per_section_top_k=8,
        retrieval_strategy="hybrid",
        graph_expand=None,
        agentic=False,
    )
    assert len(used) == 4
    assert len(calls) == 4
    assert len(hits) == 1
    assert hits[0].slug == "develop/用户中心"


def test_merge_hits_keeps_highest_score():
    pool: dict[str, EvidenceHit] = {}
    merge_hits(pool, [EvidenceHit(slug="develop/a", score=0.5)])
    merge_hits(pool, [EvidenceHit(slug="develop/a", score=0.9)])
    assert len(pool) == 1
    assert pool["develop/a"].score == 0.9
