# Grail Frontend Evaluation + Java-Only Adaptation Plan for Tucked In
**Source studied:** https://grail-app.com (homepage, privacy, terms, robots.txt, sitemap.xml) — checked Nov 16, 2026, desktop only. Mobile layout was not verified.
**Purpose:** borrow Grail's design language for Tucked In without copying its product, text, artwork, or code.

## 1. What Grail is
Grail is an unreleased real-world quest app with a waitlist. Its promise is one small real-world quest per day, a 48-hour completion clock, four RPG-style stats, animal totem avatars, guilds, and voice notes. The site is anti-screen in tone: the product is framed as getting people back into offline life.

## 2. Page pattern worth studying
The homepage is one long scroll made of numbered chapters, roughly one idea per viewport:
- loading overlay with percentage counter;
- top nav with logo, one-line promise, and one repeated CTA;
- hero chapter with giant headline and floating 3D quest cards;
- problem chapter;
- clock/how-it-works chapter;
- outcomes chapter;
- voice-feedback chapter with phone-style mock cards;
- totem chapter with selectable character tabs;
- stats/guild chapter with four stat groups;
- guild chapter;
- waitlist chapter with one email field;
- footer.

There is no pricing grid, FAQ, testimonial wall, app-store CTA, or feature comparison table. The "how it works" is told as a story through chapters rather than documented as features.

## 3. Visual system
- **Layout:** full-bleed colour-block chapters; huge centred display type; lots of empty space; objects/cards floating around the edges in perspective.
- **Typography feel:** very large all-caps display headings with wide spacing, contrasted with bold tight sans/body text on cards.
- **Colour rhythm:** each chapter gets its own background: black hero, pale pink/lavender, cream, dark brown/gold, dark olive/brown, black closing section. Accent buttons are hot-pink pills.
- **Components:** white rounded cards, small badge/shield marks, avatar clusters, duration chips, pill buttons, pill tab groups with the selected tab filled.
- **Motion:** loading counter, scroll-scrubbed rotating card carousel, floating cards, rising bubbles, tab swaps, and a sticky chapter indicator that updates while scrolling.

## 4. Technology behind it — observed vs inferred
**Directly disclosed/observed:**
- Website served by Vercel.
- Waitlist emails stored in Supabase.
- Analytics via PostHog, described as anonymous scroll/click tracking with no session recording and no ad tracking; a localStorage timestamp is used to reduce repeat waitlist submissions.
- Semantic page landmarks were visible: nav, main headings, tablist/tab/tabpanel for the totem selector, and a required email field.
- Sitemap lists only the homepage. robots.txt names several app/lab routes that were not visited.

**Not verified:**
- No reliable framework fingerprint was visible from the browser view. Do not claim it is React, Next.js, Vue, Tailwind, Webflow, or Framer unless source/assets are inspected directly.
- The behaviour is consistent with a JavaScript-heavy client-side site using CSS/JS transforms or WebGL-style rendered assets for the 3D card motion, but that is an inference from behaviour, not a verified stack fact.
- Mobile responsiveness, formal performance, and screen-reader quality were not tested.

**Key point for Tucked In:** Grail's look is a web/JavaScript design system. In Java we should copy the *design grammar* — chapters, colour blocks, big type, rounded cards, pill controls, tabs, stat tiles, timers — not the web stack.

## 5. What Tucked In should borrow (and what it should not)
**Borrow:**
- one idea per screen/chapter;
- sticky chapter label, e.g. "Tucked In / Chapter 02 — Shelter Console";
- hero screen with one promise and one primary button;
- colour-blocked surfaces for Worker, Shelter Console, and Admin;
- bed results as rounded cards instead of plain text lists;
- profile selector as character-like tabs (Sara, Youth, Family, Discharge, No-match);
- four admin stat tiles (Open beds, Active holds, Turn-aways, Stale sites);
- big countdown for a hold;
- one repeated primary action style: pill button.

**Do not borrow:**
- Grail's quests, totems, guild names, copy, shields, animal artwork, or exact layout sequence;
- heavy loading screens or scroll-jacking for a working tool — shelter staff need speed and clarity;
- inaccessible all-caps tokenised headings; keep real readable text.

## 6. Tucked In palette translation
Keep Grail's colour-block idea, but make it warmer and night-focused:
- Hero / Admin: midnight ink `#14161A` with warm gold `#E9B44C` accents.
- Worker App: warm cream `#FFF6E8`, ink text, terracotta CTA `#D96C47`.
- Shelter Console: sage `#DCE8DC` / deep green accents for "open / confirmed" states.
- Warning/stale states: muted amber; full/no-match states: dusty rose.
- Cards: white or cream, 18-24 px rounded corners, soft shadow, one badge chip per attribute.

## 7. Java-only implementation plan (Swing first, ASAP)
Do **not** change the working logic classes: `BedPool`, `RequestProfile`, `Hold`, `SampleData`, `EligibilityEngine`, `HoldManager` stay as the engine. Only the UI layer changes.

Add small reusable Swing components under the same package (or `shelterapp.ui` if the course allows a subpackage):
1. `RoundedPanel` — JPanel with rounded corners, background colour, soft shadow, anti-aliased Java2D painting.
2. `PillButton` — JButton with pill shape, filled/outline states, hover colour change.
3. `ChapterHeader` — sticky-looking top bar: product name, chapter number/title, scenario time/weather.
4. `BedCardPanel` — one pool as a card: site name, category badge, open count huge, freshness chip, intake close, attributes chips, Hold button.
5. `StatTile` — Admin metric tile with big number and label; can animate count changes with a Swing Timer.
6. `CountdownLabel` — large mm:ss hold countdown; turns amber under 30 s, red/expired at zero.

Then rebuild `ShelterApp` as a chapter flow:
- Use `CardLayout` for chapters/screens instead of only tabs, or keep tabs but style each tab as a colour-block chapter with a chapter header.
- Worker chapter: profile tabs at top, result bed cards in a scrollable grid/list, selected card detail, big Place Hold pill.
- Console chapter: selected shelter card, three big availability pills (Beds open / Last bed / Full), hold inbox as cards, arrivals as cards.
- Admin chapter: four stat tiles, tonight board as compact card rows, EWR activation as a big pill CTA, turn-away/event log below.
- Motion with `javax.swing.Timer`: fade/slide chapter transitions, countdown pulse, stat count-up, card hover lift. Java2D handles gradients, rounded shapes, and shadows.

**JavaScript:** none is needed for the Swing desktop prototype. JavaScript only becomes relevant if Tucked In later gets a web landing page or an embedded WebView; for the Dec 9 Java deliverable, keep JS at zero and say so in the report: "The reference site is web-based; this prototype reproduces its design language natively in Java Swing/Java2D."

**JavaFX note:** JavaFX would make CSS styling and animation easier, but it changes the project setup and risks NetBeans/module friction this close to the deadline. Swing + custom painting is the safer ASAP route for this course build.

## 8. Fast schedule
- **90-minute skin pass:** palette constants, chapter headers, pill buttons, rounded result cards, bigger fonts. Existing text areas remain behind the cards if needed.
- **Half-day pass:** BedCardPanel + StatTile + profile tabs + countdown styling; keep all existing actions wired to the same HoldManager methods.
- **1–2 day pass:** CardLayout chapter flow, transitions, screenshots, usability check, report screenshots and UML note that UI components were added.

Recommended order for Tucked In: run/log the core tests first (Sara flow, race, expiry), then do the skin pass in Phase 3 so the visual work cannot break the verified logic story.
