package shelterapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the live inventory state and every hold transition.
 *
 * The concurrency rule the whole product depends on:
 * placeHold() is synchronized and checks-and-decrements in one step,
 * so if two workers reach for the last bed in the same moment,
 * exactly one of them gets it. No overselling, ever.
 */
public class HoldManager {

    private final List<BedPool> pools;
    private final Map<String, BedPool> byId;
    private final List<Hold> holds;
    private final List<String> eventLog;
    private final List<String> turnAways;
    private int nextHoldId = 1;
    private final int holdSeconds;

    /** holdSeconds is short in the demo (production default is 45 minutes). */
    public HoldManager(List<BedPool> pools, int holdSeconds) {
        this.pools = pools;
        this.holdSeconds = holdSeconds;
        this.byId = new HashMap<String, BedPool>();
        for (BedPool p : pools) {
            byId.put(p.id, p);
        }
        this.holds = new ArrayList<Hold>();
        this.eventLog = new ArrayList<String>();
        this.turnAways = new ArrayList<String>();
    }

    public List<BedPool> pools() {
        return pools;
    }

    public BedPool pool(String id) {
        return byId.get(id);
    }

    public synchronized Hold placeHold(String poolId, String profileLabel) {
        BedPool p = byId.get(poolId);
        if (p == null || p.openCount() <= 0) {
            return null; // someone else took the last bed first
        }
        p.pendingHolds++;
        Hold h = new Hold();
        h.id = nextHoldId++;
        h.poolId = poolId;
        h.siteName = p.hiddenSite ? "Confidential site (relay)" : p.siteName;
        h.profileLabel = profileLabel;
        h.status = Hold.HELD;
        h.expiresAtMillis = System.currentTimeMillis() + holdSeconds * 1000L;
        holds.add(h);
        log("HOLD PLACED  #" + h.id + "  " + h.siteName + "  for: " + profileLabel);
        return h;
    }

    public synchronized void confirmHold(Hold h) {
        if (!Hold.HELD.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.pendingHolds--;
        p.confirmedHolds++;
        h.status = Hold.CONFIRMED;
        log("HOLD CONFIRMED  #" + h.id + "  " + h.siteName);
    }

    public synchronized void rejectHold(Hold h, String reason) {
        if (!Hold.HELD.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.pendingHolds--;
        h.status = Hold.REJECTED;
        h.rejectReason = reason;
        log("HOLD REJECTED  #" + h.id + "  " + h.siteName + "  reason: " + reason);
    }

    public synchronized void checkIn(Hold h) {
        if (!Hold.CONFIRMED.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.confirmedHolds--;
        p.checkedIn++;
        h.status = Hold.CHECKED_IN;
        log("CHECKED IN  #" + h.id + "  " + h.siteName);
    }

    public synchronized void noShow(Hold h) {
        if (!Hold.CONFIRMED.equals(h.status)) {
            return;
        }
        BedPool p = byId.get(h.poolId);
        p.confirmedHolds--;
        h.status = Hold.NO_SHOW;
        turnAways.add("NSHW - " + h.siteName + " - confirmed hold #" + h.id + " did not arrive");
        log("NO-SHOW  #" + h.id + "  " + h.siteName + "  (bed released, logged as NSHW)");
    }

    /** Expires any HELD hold whose countdown has run out. Returns how many expired. */
    public synchronized int expireHolds() {
        long now = System.currentTimeMillis();
        int expired = 0;
        for (Hold h : holds) {
            if (Hold.HELD.equals(h.status) && now >= h.expiresAtMillis) {
                BedPool p = byId.get(h.poolId);
                p.pendingHolds--;
                h.status = Hold.EXPIRED;
                expired++;
                log("HOLD EXPIRED  #" + h.id + "  " + h.siteName + "  (bed returned to the pool)");
            }
        }
        return expired;
    }

    public synchronized void logTurnAway(String code, String profileLabel) {
        turnAways.add(code + " - " + profileLabel);
        log("TURN-AWAY LOGGED  " + code + "  " + profileLabel);
    }

    public synchronized List<Hold> holds() {
        return new ArrayList<Hold>(holds);
    }

    public synchronized List<String> eventLog() {
        return new ArrayList<String>(eventLog);
    }

    public synchronized List<String> turnAways() {
        return new ArrayList<String>(turnAways);
    }

    private void log(String event) {
        eventLog.add(event);
    }
}
