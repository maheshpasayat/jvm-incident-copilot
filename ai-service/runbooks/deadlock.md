# Deadlock

A deadlock happens when two or more threads each hold a lock the other needs, so none can continue.

Signs: threads in BLOCKED state, each waiting for a lock held by another thread in the cycle. The application stops making progress for those threads.

Fixes: always acquire locks in the same global order; hold locks for as short a time as possible; use tryLock with a timeout; avoid calling other services while holding a lock.
