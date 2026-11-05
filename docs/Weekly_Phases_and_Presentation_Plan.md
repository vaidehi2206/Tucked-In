# Weekly Phases & Presentation Plan
**Tucked In — Real-Time Shelter Bed Reservation System · Solo · Vaidehi Dabhi**
**Final presentation: Wednesday, December 9, 2026 — 30 minutes, working prototype, in class**
Everything is recorded in GitHub: code, these docs, diagrams, test results, and the final report live in the repo, committed week by week (not one big upload at the end — the commit history is part of the documentation).

## The 5 weekly phases (Thu–Wed weeks)

### Phase 1 — Design & setup · Thu Nov 5 – Wed Nov 11
- [x] Sample dataset built and verified (`sample_dataset.json`: 10 fictional sites, 11 pools, 5 profiles)
- [x] Prototype code scaffold (plain Java + Swing, 7 classes)
- [ ] **UML & class diagrams** — done as a document (`docs/UML_Diagrams.md`: class, use case, sequence, hold state diagram); review once and adjust if your course wants a specific tool/format
- [ ] **Create the GitHub repo** (V does this — 2 minutes, see repo checklist below) and push the first commit: code + README + docs
- [ ] First NetBeans run of `ShelterApp.java` — fix any compile errors (this is the Phase 1 exit test)
**Exit:** repo live, app opens on your laptop, diagrams committed.

### Phase 2 — Core logic verified · Thu Nov 12 – Wed Nov 18
- Run evaluation tests T1–T3 (report outline): Sara end-to-end (search → hold → confirm → check-in), all 5 profiles vs expected results, last-bed race ×3
- Fix what the runs expose; keep a test log in `docs/Test_Log.md` (date, test, expected, actual, pass/fix)
- Commit after every working fix — small commits, clear messages
**Exit:** T1–T3 passing and logged.

### Phase 3 — Surfaces & demo hardening · Thu Nov 19 – Wed Nov 25
- Tests T4–T6: hold expiry/backfill, turn-away capture in Admin, EWR activation
- Polish the three tabs for presentation visibility (font sizes, wording a classmate can follow)
- Usability pass if time (T8): 2–3 classmates role-play worker/front-desk; note top confusions, fix the worst one
**Exit:** full demo script runs start-to-finish without a dead end.

### Phase 4 — Report & evaluation · Thu Nov 26 – Wed Dec 2
- Fill the report outline's results table with the real numbers from the test log
- Write limitations + future work sections (drafts already in the outline)
- Screenshots of all three tabs saved to `docs/screenshots/` as backup
- Commit report draft to GitHub
**Exit:** report complete in the repo; only presentation prep remains.

### Phase 5 — Presentation week · Thu Dec 3 – Wed Dec 9
- Build the 30-minute talk (structure below); rehearse twice with a timer
- Dry-run the live demo on the presentation machine/laptop the day before (Dec 8), including the race and expiry beats
- Final commit + tag `v1.0-final` in GitHub
- **Wed Dec 9: present** — live prototype + Q&A
**Exit:** presented. 🎉

## The 30-minute presentation (timed structure)
| Time | Section | Content |
|---|---|---|
| 0:00–3:00 | The problem | Sara's rainy night, told as a story; the 4 failed phone calls; 2025 count numbers (2,715 people, 763 unsheltered) |
| 3:00–6:00 | What exists today | BC 211 twice-daily list; HIFIS/Coordinated Access (alongside, not replaced); San Diego Shelter Ready as closest cousin — honest positioning |
| 6:00–10:00 | Design | Hotel model → shelter rules; eligibility taxonomy; **UML class diagram + hold state diagram on screen**; the no-overselling rule |
| 10:00–22:00 | **Live demo** (the heart) | README demo script: Sara search + exclusion reasons → hold → console confirm → check-in → race for the last bed → 90-second expiry → EWR activation → no-match turn-away in Admin |
| 22:00–26:00 | Evaluation | Your real test-log numbers: time to confirmed hold vs phone process, race results, expiry, turn-away data |
| 26:00–28:00 | Limitations & future work | Sample data, one machine, 90-s demo TTL stated plainly; SMS view + real validation as future work |
| 28:00–30:00 | Close + questions | The closing line: the system doesn't create beds — it stops wasting the beds, hours, and trust that already exist. Then Q&A |

## GitHub repo checklist (one-time, ~10 minutes)
- [ ] Repo name: `tucked-in` (public or private per your course rules — ask the professor if unsure)
- [ ] Push the `ShelterApp` folder as the repo content: `src/`, `docs/`, `README.md`, `.gitignore`
- [ ] README on GitHub shows: problem, how to run in NetBeans, demo script, links to `docs/`
- [ ] Weekly rhythm: at least one commit per phase with the phase in the message (e.g., `Phase 2: fix hold expiry, T4 passing`)
- [ ] Diagrams live in `docs/UML_Diagrams.md` (Mermaid renders on GitHub — your professor sees the diagrams without downloading anything)
- [ ] Final: tag `v1.0-final` on presentation day
