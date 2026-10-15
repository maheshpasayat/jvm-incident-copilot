from typing import Protocol

import httpx
import os
OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")
OLLAMA_MODEL = "llama3.2:3b"


class LlmClient(Protocol):
    def generate(self, prompt: str) -> str: ...


class OllamaClient:
    def __init__(self, base_url: str = OLLAMA_URL, model: str = OLLAMA_MODEL, timeout: float = 60.0):
        self.base_url = base_url
        self.model = model
        self.timeout = timeout

    def generate(self, prompt: str) -> str:
        response = httpx.post(
            f"{self.base_url}/api/generate",
            json={"model": self.model, "prompt": prompt, "stream": False},
            timeout=self.timeout,
        )
        response.raise_for_status()
        return response.json()["response"].strip()


class MockLlmClient:
    """Used in tests: returns a fixed answer and records the prompt it received."""

    def __init__(self, answer: str = "MOCK EXPLANATION"):
        self.answer = answer
        self.last_prompt: str | None = None

    def generate(self, prompt: str) -> str:
        self.last_prompt = prompt
        return self.answer
