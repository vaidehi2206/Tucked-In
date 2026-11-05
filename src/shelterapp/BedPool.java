package shelterapp;

import java.util.ArrayList;
import java.util.List;

/**
 * One bed pool at one shelter site.
 * Sample data only - every site in this prototype is fictional.
 *
 * Open count rule (from the project taxonomy):
 *   open = physical - checkedIn - confirmedHolds - walkInReserve - pendingHolds
 * Workers only ever see the open count, never the arithmetic.
 */
public class BedPool {

    public String id;
    public String siteName;
    public String area;
    public boolean hiddenSite;        // VAW site: name/location never displayed
    public String category;           // WOM, MEN, MIX, YOU, FAM, CPL
    public List<String> attributes;   // ACC, PET, LOW, SOB, MH, MED, IND
    public String countUnit;          // "beds", "family units", "mats"

    public int physical;
    public int checkedIn;
    public int confirmedHolds;
    public int walkInReserve;
    public int pendingHolds;          // holds placed, waiting for shelter to confirm

    public String intakeOpen;         // "15:00"
    public String intakeClose;        // "23:00", "24:00", or overnight "02:00"
    public int lastUpdatedMinAgo;     // -1 = never updated
    public boolean active;            // EWR pop-up starts inactive
    public boolean referralOnly;
    public String referralTypeNeeded; // e.g. "hospital"
    public int ageMin;                // -1 = no minimum
    public int ageMax;                // -1 = no maximum
    public List<String> partyTypes;   // "single", "couple", "family"
    public String sobrietyStance;     // "tolerant" or "program-required"
    public int maxStayNights;

    public BedPool() {
        attributes = new ArrayList<String>();
        partyTypes = new ArrayList<String>();
        active = true;
        countUnit = "beds";
        ageMin = -1;
        ageMax = -1;
        lastUpdatedMinAgo = 0;
    }

    public int openCount() {
        if (!active) {
            return 0;
        }
        int open = physical - checkedIn - confirmedHolds - walkInReserve - pendingHolds;
        return Math.max(open, 0);
    }

    public String freshness() {
        if (lastUpdatedMinAgo < 0) {
            return "not updated yet";
        }
        if (lastUpdatedMinAgo <= 30) {
            return "fresh";
        }
        if (lastUpdatedMinAgo <= 60) {
            return "aging";
        }
        if (lastUpdatedMinAgo <= 120) {
            return "stale";
        }
        return "call to verify";
    }

    public boolean holdableFreshness() {
        return lastUpdatedMinAgo >= 0 && lastUpdatedMinAgo <= 120;
    }

    /** Is the given time (minutes after midnight) inside the intake window? */
    public boolean intakeOpenAt(int minutes) {
        int open = toMinutes(intakeOpen);
        int close = toMinutes(intakeClose);
        if (close > open) {
            return minutes >= open && minutes < close;
        }
        // overnight window, e.g. 20:00 - 02:00
        return minutes >= open || minutes < close;
    }

    public static int toMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        int h = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]);
        return h * 60 + m;
    }
}
