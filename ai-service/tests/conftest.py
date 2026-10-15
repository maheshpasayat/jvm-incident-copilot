import pytest

from app.retriever import index_runbooks


@pytest.fixture(scope="session", autouse=True)
def indexed_runbooks():
    index_runbooks()
