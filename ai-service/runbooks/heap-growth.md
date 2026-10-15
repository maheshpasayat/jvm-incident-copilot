# Heap growth (likely memory leak)

If the heap used after each Full GC keeps rising, memory is not being released. This usually means objects are still referenced when they should not be.

Signs: heap after Full GC increases on every collection, followed by longer pauses and eventually OutOfMemoryError.

Fixes: take a heap dump and find the largest retained objects; check unbounded caches, static collections and listeners that are never removed; add size limits or eviction to caches.
