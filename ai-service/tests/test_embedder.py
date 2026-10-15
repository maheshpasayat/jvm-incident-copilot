from app.embedder import EMBEDDING_DIM, embed


def dot(a, b):
    return sum(x * y for x, y in zip(a, b))


def test_embedding_has_expected_size():
    assert len(embed("deadlock between two threads")) == EMBEDDING_DIM


def test_similar_text_is_closer_than_unrelated_text():
    query = embed("threads waiting for each other's locks")
    related = embed("a deadlock happens when threads hold locks the other needs")
    unrelated = embed("how to bake sourdough bread")
    assert dot(query, related) > dot(query, unrelated)