# UML Diagrams — Tucked In (Shelter Bed Reservation System)
**Drawn from the actual code** (package `shelterapp`, 7 classes). If the code changes, update this file in the same commit — that is the GitHub documentation rule for this project.
Diagrams are in Mermaid, which GitHub renders directly in markdown.

---

## 1. Class Diagram

```mermaid
classDiagram
    class BedPool {
        +String id
        +String siteName
        +String area
        +boolean hiddenSite
        +String category
        +List~String~ attributes
        +String countUnit
        +int physical
        +int checkedIn
        +int confirmedHolds
        +int walkInReserve
        +int pendingHolds
        +String intakeOpen
        +String intakeClose
        +int lastUpdatedMinAgo
        +boolean active
        +boolean referralOnly
        +String referralTypeNeeded
        +int ageMin
        +int ageMax
        +List~String~ partyTypes
        +String sobrietyStance
        +int maxStayNights
        +int openCount()
        +String freshness()
        +boolean holdableFreshness()
        +boolean intakeOpenAt(int)
        +int toMinutes(String)$ 
    }
    class RequestProfile {
        +String id
        +String label
        +String categoryNeeded
        +List~String~ attributesNeeded
        +int age
        +String party
        +String sobrietyNeed
        +String referrerType
    }
    class Hold {
        +String HELD$
        +String CONFIRMED$
        +String REJECTED$
        +String CHECKED_IN$
        +String NO_SHOW$
        +String EXPIRED$
        +int id
        +String poolId
        +String siteName
        +String profileLabel
        +String status
        +long expiresAtMillis
        +String rejectReason
        +long secondsLeft(long)
        +String describe(long)
    }
    class EligibilityEngine {
        +String exclusionReason(BedPool, RequestProfile, int)$
        +List~BedPool~ search(List~BedPool~, RequestProfile, int)$
        +String suggestTurnAwayCode(List~BedPool~, RequestProfile, int)$
    }
    class HoldManager {
        -List~BedPool~ pools
        -Map~String, BedPool~ byId
        -List~Hold~ holds
        -List~String~ eventLog
        -List~String~ turnAways
        -int nextHoldId
        -int holdSeconds
        +Hold placeHold(String, String)
        +void confirmHold(Hold)
        +void rejectHold(Hold, String)
        +void checkIn(Hold)
        +void noShow(Hold)
        +int expireHolds()
        +void logTurnAway(String, String)
        +List~Hold~ holds()
        +List~String~ eventLog()
        +List~String~ turnAways()
    }
    class SampleData {
        +int SCENARIO_MINUTES$
        +List~BedPool~ pools()$
        +List~RequestProfile~ profiles()$
    }
    class ShelterApp {
        -HoldManager manager
        -List~RequestProfile~ profiles
        -int scenarioMinutes
        +void main(String[])$
    }

    HoldManager "1" o-- "*" BedPool : one shared inventory
    HoldManager "1" o-- "*" Hold : manages
    Hold "*" --> "1" BedPool : poolId refers to
    EligibilityEngine ..> BedPool : reads rules
    EligibilityEngine ..> RequestProfile : matches against
    SampleData ..> BedPool : creates dataset
    SampleData ..> RequestProfile : creates profiles
    ShelterApp --> HoldManager : uses
    ShelterApp --> EligibilityEngine : searches via
    ShelterApp --> SampleData : loads scenario
    ShelterApp --|> JFrame : Swing window (3 tabs)
```

**How to explain it in the presentation (30 seconds):** three data classes (`BedPool`, `RequestProfile`, `Hold`), two engines (`EligibilityEngine` decides what matches, `HoldManager` owns the live counts and every hold transition), one dataset (`SampleData`), one window (`ShelterApp`) with three tabs sharing the same `HoldManager` — that sharing *is* the "one live inventory" idea from the hotel model.

---

## 2. Use Case Diagram

```mermaid
flowchart LR
    Worker(["Outreach worker /<br/>211 navigator / ER staff"])
    Staff(["Shelter front-desk staff"])
    Admin(["City admin"])

    subgraph System["Shelter Bed Reservation System"]
        UC1(["Search eligible beds"])
        UC2(["Place a hold"])
        UC3(["Log a turn-away"])
        UC4(["Update availability<br/>(Beds open / Last bed / Full)"])
        UC5(["Confirm or reject a hold"])
        UC6(["Check in / mark no-show"])
        UC7(["View tonight board"])
        UC8(["View unmet-demand data"])
        UC9(["Activate weather-response site"])
    end

    Worker --> UC1
    Worker --> UC2
    Worker --> UC3
    Staff --> UC4
    Staff --> UC5
    Staff --> UC6
    Admin --> UC7
    Admin --> UC8
    Admin --> UC9
```

---

## 3. Sequence Diagram — Sara's hold, end to end

```mermaid
sequenceDiagram
    actor W as Worker
    participant UI as ShelterApp (Worker tab)
    participant E as EligibilityEngine
    participant M as HoldManager
    participant C as Shelter Console
    actor S as Shelter staff

    W->>UI: Choose Sara profile, Search
    UI->>E: search(pools, Sara, 20:40)
    E-->>UI: Harbourview only (every other pool excluded, with reasons)
    UI-->>W: 1 eligible bed list + why the rest were excluded
    W->>UI: Place hold
    UI->>M: placeHold(pool-01) [synchronized]
    M-->>UI: Hold #1 HELD, countdown starts
    S->>C: Open hold inbox
    C->>M: confirmHold(#1)
    M-->>C: CONFIRMED (bed stays held)
    Note over W,S: Only now is Sara sent travelling -<br/>confirmation before travel
    S->>C: Arrivals -> Check in
    C->>M: checkIn(#1)
    M-->>C: CHECKED_IN (bed becomes occupied)

    opt Hold not confirmed in time
        M->>M: expireHolds() runs every second
        M-->>UI: Hold EXPIRED, bed returns to the pool
    end
    opt Two workers race for the last bed
        M->>M: placeHold is synchronized: check + decrement in one step
        M-->>UI: Exactly one worker wins; the other is told instantly
    end
```

---

## 4. State Diagram — the Hold lifecycle

```mermaid
stateDiagram-v2
    [*] --> HELD : Worker places hold (countdown starts)
    HELD --> CONFIRMED : Shelter confirms
    HELD --> REJECTED : Shelter rejects (reason recorded)
    HELD --> EXPIRED : Countdown reaches zero (auto)
    CONFIRMED --> CHECKED_IN : Person arrives
    CONFIRMED --> NO_SHOW : Person does not arrive (logged NSHW)
    REJECTED --> [*] : Bed was never taken out of the pool count for long
    EXPIRED --> [*] : Bed returns to open count
    CHECKED_IN --> [*] : Bed becomes occupied
    NO_SHOW --> [*] : Bed returns to open count
```

**The one rule to say out loud:** a bed's open count only ever changes inside `HoldManager`, one transition at a time — that is why the system can promise *no overselling*.
