# Tucked In — Shelter Bed Reservation (Java Prototype)
*Everyone tucked in for the night.* A real-time shelter-bed reservation system: live, eligibility-aware beds with a confirmed hold before the person travels.
**University final project · Solo · Vaidehi Dabhi · Due December 9, 2026**
Plain Java + Swing. No Maven, no external libraries — everything runs from these 7 source files.

## Files (src/shelterapp/)
| File | What it is |
|---|---|
| `BedPool.java` | One bed pool at a shelter: category, attributes, counts, intake window, freshness |
| `RequestProfile.java` | A needs-only search profile (no name needed to search, by design) |
| `Hold.java` | A soft hold and its states: HELD → CONFIRMED → CHECKED_IN / NO_SHOW, or REJECTED / EXPIRED |
| `SampleData.java` | The fictional dataset: 10 invented sites, 11 pools, 5 test profiles (rainy night, 8:40 pm) |
| `EligibilityEngine.java` | The shelter-specific core: eligibility as HARD filters + a reason for every exclusion + turn-away code suggestion |
| `HoldManager.java` | Live inventory + all hold transitions. `placeHold()` is synchronized: two workers racing for the last bed → exactly one wins |
| `ShelterApp.java` | Swing UI with 3 tabs — Worker App, Shelter Console, City Admin — all reading/writing the same inventory |

## New front end — Grail-inspired UI (Nov 16, 2026)
A second window, `TuckedInGrailApp.java`, rebuilds the presentation layer in the design language of grail-app.com — colour-block chapters, rounded cards, pill buttons/tabs, stat tiles, big serif display type — using **pure Java Swing/Java2D, zero JavaScript**, and the *same* engine classes (`EligibilityEngine`, `HoldManager`, `SampleData`). Supporting components: `Theme`, `RoundedPanel`, `PillButton`, `ChapterHeader`, `StatTile`, `BedCardPanel`. The original `ShelterApp.java` is untouched and remains the stable fallback. Run `TuckedInGrailApp.java` in NetBeans to see it: Chapter 00 Tonight (hero) → 01 Worker (profile tabs + bed cards) → 02 Shelter Console → 03 City Admin (stat tiles + tonight board). Design study: `files/Grail_Frontend_Evaluation_and_Java_Plan.md` in the project docs.

## Run it in NetBeans
1. File → New Project → Java Application. Name it `ShelterApp`, **uncheck** "Create Main Class" if asked (we have one).
2. Copy the `shelterapp` folder from `src/` into your project's `src/` folder (or: right-click Source Packages → Add Existing Folder... and pick `src/shelterapp`'s parent after copying).
   - Simpler alternative: create the project, delete the generated package, then drag these 7 `.java` files into a new package named `shelterapp`.
3. Right-click `ShelterApp.java` → **Run File**. The window opens on the Worker App tab.

## The 3-minute demo script (Sara)
1. **Worker App:** profile = Sara → **Search**. One match: Harbourview, 2 beds open. Read the exclusion list aloud — the couples pool is full, the sobriety house is excluded because Sara needs low-barrier, the VAW house needs a pet rule it doesn't have. *Eligibility is a filter, not a ranking.*
2. **Place hold** on Harbourview (open drops 2 → 1). Note: HELD is not CONFIRMED — the shelter has the final say. Demo countdown is 90 seconds; production is 45 minutes.
3. **Shelter Console:** pick Harbourview [WOM] → the hold is in the inbox → **Confirm hold**. Only now would the worker send Sara travelling.
4. Back in **Arrivals** → **Check in**. Bed becomes occupied.
5. **Race demo (Worker App):** place Sara's hold first so Harbourview has 1 bed left, then click "two workers race for the last bed" — exactly one wins.
6. **Expiry:** place any hold and just wait 90 seconds — it expires and the bed returns to the pool (watch the Admin event log).
7. **City Admin:** tonight board (all pools, freshness, total open), then **Activate Rainier EWR Pop-Up** — 50 mats appear system-wide. Run the no-match profile search in the Worker App and log the turn-away — Admin shows it as unmet-demand data (code ATTR).

## Documentation (docs/)
- [UML diagrams](docs/UML_Diagrams.md) — class diagram, use case diagram, Sara sequence diagram, hold state diagram (Mermaid, renders on GitHub)
- [Weekly phases & presentation plan](docs/Weekly_Phases_and_Presentation_Plan.md) — 5 weekly phases to the **December 9, 2026** final presentation (30 minutes, live prototype), plus the GitHub commit rhythm
- Test log and final report join this folder as the phases complete.

## Honest limits (say these in the demo before the professor asks)
- All data is sample/fictional; no real shelters, no real personal data.
- One machine: the three tabs simulate three separate users of one shared inventory.
- Hold time is shortened to 90 s for the demo; the PRD rule is 45 min, shelter-set.
- Not a replacement for HIFIS / Coordinated Access — in production this is a "tonight layer" alongside them.
