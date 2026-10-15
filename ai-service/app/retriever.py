import os
from pathlib import Path

import psycopg
from pgvector.psycopg import register_vector

from app.embedder import embed

DB_URL = os.getenv("DATABASE_URL", "postgresql://copilot:copilot@localhost:5432/incidents")
RUNBOOK_DIR = Path(__file__).resolve().parent.parent / "runbooks"


def _connect() -> psycopg.Connection:
    conn = psycopg.connect(DB_URL, autocommit=True)
    conn.execute("CREATE EXTENSION IF NOT EXISTS vector")
    register_vector(conn)
    return conn


def index_runbooks() -> int:
    """Embed every runbook and store it. Safe to run repeatedly."""
    with _connect() as conn:
        conn.execute(
            "CREATE TABLE IF NOT EXISTS runbook_chunks ("
            "name text PRIMARY KEY, content text NOT NULL, embedding vector(384) NOT NULL)"
        )
        count = 0
        for path in sorted(RUNBOOK_DIR.glob("*.md")):
            content = path.read_text(encoding="utf-8")
            conn.execute(
                "INSERT INTO runbook_chunks (name, content, embedding) VALUES (%s, %s, %s) "
                "ON CONFLICT (name) DO UPDATE SET content = EXCLUDED.content, embedding = EXCLUDED.embedding",
                (path.stem, content, embed(content)),
            )
            count += 1
        return count


def retrieve(query: str, limit: int = 1) -> list[dict]:
    """Return the runbooks closest in meaning to the query."""
    with _connect() as conn:
        rows = conn.execute(
            "SELECT name, content, embedding <=> %s::vector AS distance "
            "FROM runbook_chunks ORDER BY distance LIMIT %s",
            (embed(query), limit),
        ).fetchall()
    return [{"name": n, "content": c, "distance": float(d)} for n, c, d in rows]
