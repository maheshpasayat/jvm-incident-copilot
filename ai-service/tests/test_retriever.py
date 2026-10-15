from app.retriever import index_runbooks, retrieve


def test_index_and_retrieve_matching_runbook():
    assert index_runbooks() == 4
    assert retrieve("Deadlock between 2 threads")[0]["name"] == "deadlock"
    assert retrieve("Heap after Full GC rose from 200 MB to 600 MB")[0]["name"] == "heap-growth"
    assert retrieve("3 GC pauses of 200 ms or more")[0]["name"] == "long-gc-pause"


def test_pool_exhaustion_finding_with_evidence_finds_its_runbook():
    query = (
        "4 threads blocked on lock 0x1. Lock 0x1 is held by http-nio-8080-exec-5. "
        "http-nio-8080-exec-1 is BLOCKED waiting for 0x1. "
        "http-nio-8080-exec-2 is BLOCKED waiting for 0x1."
    )
    assert retrieve(query)[0]["name"] == "pool-exhaustion"
