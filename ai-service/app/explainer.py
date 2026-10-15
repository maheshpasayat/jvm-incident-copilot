from app.llm import LlmClient
from app.retriever import retrieve


def build_prompt(title: str, evidence: list[str], runbook: str) -> str:
    facts = "\n".join(f"- {line}" for line in evidence)
    return (
        "You are helping an engineer understand a JVM incident.\n"
        "Use ONLY the verified facts and the runbook below. Do not invent threads, locks, "
        "numbers or causes. If something is not in the facts, say it is unknown.\n\n"
        f"FINDING: {title}\n"
        f"VERIFIED FACTS:\n{facts}\n\n"
        f"RUNBOOK:\n{runbook}\n\n"
        "Write a short explanation of the likely cause and a suggested fix, in plain English, "
        "in at most 120 words."
    )


def explain(title: str, evidence: list[str], llm: LlmClient) -> dict:
    query = title + ". " + " ".join(evidence)
    runbook = retrieve(query)[0]
    prompt = build_prompt(title, evidence, runbook["content"])
    return {"explanation": llm.generate(prompt), "runbook": runbook["name"]}
