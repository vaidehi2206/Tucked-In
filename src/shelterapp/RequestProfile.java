package shelterapp;

import java.util.ArrayList;
import java.util.List;

/**
 * What a worker is searching for, on behalf of a person.
 * No name is needed to search - a profile is needs only, by design.
 */
public class RequestProfile {

    public String id;
    public String label;
    public String categoryNeeded;          // WOM, MEN, MIX, YOU, FAM, CPL
    public List<String> attributesNeeded;  // subset the pool MUST have
    public int age;                        // 0 = not applicable / not asked
    public String party;                   // "single", "couple", "family"
    public String sobrietyNeed;            // "any" or "must-not-require-sobriety"
    public String referrerType;            // "outreach worker", "211 navigator", "hospital"

    public RequestProfile() {
        attributesNeeded = new ArrayList<String>();
        age = 0;
        sobrietyNeed = "any";
        referrerType = "outreach worker";
    }
}
