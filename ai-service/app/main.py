from fastapi import Depends, FastAPI
from pydantic import BaseModel

from app.explainer import explain
from app.llm import LlmClient, OllamaClient
from app.retriever import index_runbooks

app = FastAPI(title="Incident Copilot AI Service", version="0.1.0")


class ExplainRequest(BaseModel):
    title: str
    evidence: list[str]


class ExplainResponse(BaseModel):
    explanation: str
    runbook: str


def get_llm() -> LlmClient:
    return OllamaClient()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post("/index")
def index() -> dict[str, int]:
    return {"indexed": index_runbooks()}


@app.post("/explain", response_model=ExplainResponse)
def explain_finding(request: ExplainRequest, llm: LlmClient = Depends(get_llm)) -> ExplainResponse:
    return ExplainResponse(**explain(request.title, request.evidence, llm))
