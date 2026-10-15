from fastapi.testclient import TestClient

from app.llm import MockLlmClient
from app.main import app, get_llm

app.dependency_overrides[get_llm] = lambda: MockLlmClient("MOCK EXPLANATION")
client = TestClient(app)


def test_explain_returns_grounded_explanation_with_runbook():
    response = client.post(
        "/explain",
        json={
            "title": "Deadlock between 2 threads",
            "evidence": ["order-worker-1 holds [0x1] and waits for 0x2"],
        },
    )
    assert response.status_code == 200
    assert response.json() == {"explanation": "MOCK EXPLANATION", "runbook": "deadlock"}


def test_explain_rejects_bad_body():
    assert client.post("/explain", json={"title": "x"}).status_code == 422
