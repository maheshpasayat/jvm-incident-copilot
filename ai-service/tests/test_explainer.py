from app.explainer import build_prompt, explain
from app.llm import MockLlmClient

EVIDENCE = [
    "order-worker-1 holds [0x1] and waits for 0x2",
    "payment-worker-1 holds [0x2] and waits for 0x1",
]


def test_prompt_contains_facts_and_forbids_invention():
    prompt = build_prompt("Deadlock between 2 threads", EVIDENCE, "RUNBOOK TEXT")
    assert "order-worker-1 holds [0x1]" in prompt
    assert "RUNBOOK TEXT" in prompt
    assert "Do not invent" in prompt


def test_explain_uses_retrieved_runbook_and_mock_llm():
    llm = MockLlmClient("It is a lock cycle.")
    result = explain("Deadlock between 2 threads", EVIDENCE, llm)
    assert result == {"explanation": "It is a lock cycle.", "runbook": "deadlock"}
    assert "Deadlock" in llm.last_prompt
