# Long GC pauses

A garbage collection pause stops all application threads, so a long pause shows up as request latency spikes.

Signs: pauses of 200 ms or more, especially Full GC pauses of a second or more, and frequent collections when the heap stays near its limit.

Fixes: increase heap size if it is too small; reduce allocation rate and object churn; tune the collector, for example the G1 pause-time goal; look for large objects or caches that keep the heap full.
