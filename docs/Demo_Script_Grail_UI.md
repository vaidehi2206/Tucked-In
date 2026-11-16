# Demo Script — Tucked In (Grail-style UI)
**For the live-demo segment (minutes 10:00–22:00) of the December 9, 2026 presentation.**
Run `TuckedInGrailApp.java`. Fallback if anything fails live: `ShelterApp.java` (original window, same engine) or the screenshots in `docs/screenshots/`.

## Before you start (say this, 20 seconds)
"Everything you're about to see is sample data — invented shelters, invented people. The design was inspired by a study of modern consumer apps: one idea per screen, everything as cards. The engine underneath is the serious part."

## The flow
| # | Where | Do | Say |
|---|---|---|---|
| 1 | Chapter 00 · Tonight | Land on the hero. Pause. | "8:40 pm, raining, five degrees. This is the moment the current system handles with phone calls. Three cards: one shelter with beds, one that's full, one weather site nobody has switched on." |
| 2 | Chapter 00 | Click **Start with Sara →** | "Sara: wheelchair, partner, dog, needs low-barrier. In the old world that's four phone calls. Watch how many she needs here." |
| 3 | Chapter 01 · Worker | Sara's results are already searched: one bed card (Harbourview, 2 open). Scroll the exclusion list. | "One card. Not a list of maybes — every other pool was *excluded by a rule*, and the system can say why: couples pool full, sobriety house excluded because Sara needs low-barrier. Eligibility is a filter, not a ranking." |
| 4 | Chapter 01 | Click **Hold this bed** on Harbourview. | "The bed is HELD — not confirmed. The shelter always has the final say. In production this countdown is 45 minutes; for the demo it's 90 seconds so you can watch it expire later." |
| 5 | Chapter 02 · Console | Switch chapter; select Harbourview [WOM]; the hold is in the inbox → **Confirm hold**. | "Only now — after a human at the shelter said yes — would Sara be sent travelling. Confirmation before travel." |
| 6 | Chapter 02 | Arrivals → **Check in**. | "She arrived. The bed becomes occupied, and every surface sees the same number, because there is only one inventory." |
| 7 | Chapter 01 | Place a second hold and just let it expire (talk over the 90 s). | "And if life happens — bus missed, plans change — the hold dies on its own and the bed goes back. No phone call to release it, no bed lost for the night." |
| 8 | Chapter 03 · Admin | Show the four stat tiles, then click **Activate Rainier EWR Pop-Up**. | "The city's view. And when the weather decision is made, fifty mats appear everywhere at once — that's the surge the current twice-a-day list can't do." |
| 9 | Chapter 01 | Switch to the **No-match** tab, search, log the turn-away. Back to Admin: turn-away tile has moved. | "The most important screen in the app is a failure. This woman needed a pet-friendly medical bed that doesn't exist tonight — and now that gap is data, with a reason code, instead of a story nobody recorded." |

## If the professor asks (30-second answers)
- **"Why not just use the 211 list?"** — It's updated twice a day, weekdays, and shows categories, not live eligible beds or holds. This is the tonight layer on top of systems like that, not a replacement for HIFIS or Coordinated Access.
- **"What stops two workers booking the same bed?"** — `placeHold()` is synchronized: check and decrement happen as one step. Exactly one worker wins the last bed; the other is told instantly.
- **"Why does the new UI look like a game?"** — The design study (grail-app.com) showed one idea per screen and card-based layouts hold attention. The warmth is deliberate: this tool is used on the worst night of someone's year. All of it is Java Swing/Java2D — no JavaScript, no web stack.
- **"What's fake here?"** — The data (all sample), the 90-second hold (45 minutes in the spec), and three roles on one machine. The logic — filtering, holds, expiry, turn-away capture — is real.
