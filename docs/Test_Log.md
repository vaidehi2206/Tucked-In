# Test Log — Shelter Bed Reservation System
Record every run here, the same day it happens. This log is the evidence base for the final report's evaluation section — if it isn't logged, it didn't happen.
Format: date · test · expected · actual · result (PASS / FIX + what changed)

| Date | Test | Expected | Actual | Result |
|---|---|---|---|---|
| | T0 — First NetBeans run | `ShelterApp.java` compiles and the window opens on the Worker tab | | |
| | T1 — Sara end-to-end | Search → Harbourview only → hold (2→1 open) → Console confirm → check-in; record time + clicks | | |
| | T2 — All 5 profiles | Sara→Harbourview · youth→Kitsilano · family→Fraser · discharge→Mercy · no-match→turn-away ATTR | | |
| | T3 — Last-bed race (run 3×) | Sara hold first (1 left), then race: exactly one winner each run | | |
| | T4 — Expiry & backfill | Hold expires at 90 s; bed returns to open count; EXPIRED in Admin event log | | |
| | T5 — Turn-away capture | No-match search → log turn-away → appears in Admin unmet demand (ATTR) | | |
| | T6 — EWR activation | Admin activates Rainier → 50 mats on tonight board and in MIX searches | | |
| | T7 — Phone comparison (narrated) | Sara by phone = 4 failed contacts (from problem statement) vs T1 clicks/time | | |
| | T8 — Usability (optional) | 2–3 classmates role-play; record top 3 confusions + fix made | | |
