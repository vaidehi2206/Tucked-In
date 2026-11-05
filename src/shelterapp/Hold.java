package shelterapp;

/**
 * A soft, time-boxed hold on one bed.
 * State machine: HELD -> CONFIRMED -> CHECKED_IN / NO_SHOW
 *                HELD -> REJECTED
 *                HELD -> EXPIRED (auto, when the countdown reaches zero)
 */
public class Hold {

    public static final String HELD = "HELD";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String REJECTED = "REJECTED";
    public static final String CHECKED_IN = "CHECKED_IN";
    public static final String NO_SHOW = "NO_SHOW";
    public static final String EXPIRED = "EXPIRED";

    public int id;
    public String poolId;
    public String siteName;
    public String profileLabel;
    public String status;
    public long expiresAtMillis;
    public String rejectReason;

    public long secondsLeft(long nowMillis) {
        long diff = (expiresAtMillis - nowMillis) / 1000;
        return Math.max(diff, 0);
    }

    public String describe(long nowMillis) {
        String base = "Hold #" + id + " - " + siteName + " - for: " + profileLabel + " - " + status;
        if (HELD.equals(status)) {
            long s = secondsLeft(nowMillis);
            base += " (" + (s / 60) + ":" + String.format("%02d", s % 60) + " left)";
        }
        if (REJECTED.equals(status) && rejectReason != null) {
            base += " - reason: " + rejectReason;
        }
        return base;
    }
}
