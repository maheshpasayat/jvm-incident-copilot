# Thread pool exhaustion

Many request threads are BLOCKED waiting for the same lock, so the pool runs out of free threads and new requests queue up.

Signs: several threads BLOCKED on one lock, and one thread holding that lock while doing slow work such as network or database calls.

Fixes: never hold a lock during I/O; load data outside the lock and swap the result in; use concurrent collections or finer-grained locks; set timeouts on slow calls.
