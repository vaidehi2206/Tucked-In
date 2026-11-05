package shelterapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The fictional demo dataset: 10 invented Vancouver-style sites, 11 bed pools.
 * Numbers match files/sample_dataset.json exactly.
 * Scenario: rainy night, 8:40 pm (20:40), about 5 degrees.
 */
public class SampleData {

    public static final int SCENARIO_MINUTES = 20 * 60 + 40; // 8:40 pm

    private static BedPool pool(String id, String site, String area, boolean hidden,
                                String category, String[] attrs, int physical,
                                int checkedIn, int confirmed, int reserve,
                                String open, String close, int updatedMinAgo,
                                int ageMin, int ageMax, String[] party,
                                String sobriety, int maxStay) {
        BedPool p = new BedPool();
        p.id = id;
        p.siteName = site;
        p.area = area;
        p.hiddenSite = hidden;
        p.category = category;
        p.attributes.addAll(Arrays.asList(attrs));
        p.physical = physical;
        p.checkedIn = checkedIn;
        p.confirmedHolds = confirmed;
        p.walkInReserve = reserve;
        p.intakeOpen = open;
        p.intakeClose = close;
        p.lastUpdatedMinAgo = updatedMinAgo;
        p.ageMin = ageMin;
        p.ageMax = ageMax;
        p.partyTypes.addAll(Arrays.asList(party));
        p.sobrietyStance = sobriety;
        p.maxStayNights = maxStay;
        return p;
    }

    public static List<BedPool> pools() {
        List<BedPool> list = new ArrayList<BedPool>();

        // 1 - Sara's correct match: women, low-barrier, pets, wheelchair accessible
        list.add(pool("pool-01-wom", "Harbourview Women's Shelter", "Downtown Eastside", false,
                "WOM", new String[]{"LOW", "PET", "ACC"}, 40, 36, 1, 1,
                "15:00", "23:00", 8, 18, -1, new String[]{"single", "couple"}, "tolerant", 30));

        // 2 - high-volume men's shelter
        list.add(pool("pool-02-men", "North Shore Rd. Men's Shelter", "Downtown Eastside", false,
                "MEN", new String[]{"LOW"}, 60, 51, 1, 1,
                "16:00", "22:00", 14, 18, -1, new String[]{"single"}, "tolerant", 30));

        // 3 - youth, age band 16-24
        list.add(pool("pool-03-you", "Kitsilano Youth House", "Kitsilano", false,
                "YOU", new String[]{"MH"}, 18, 14, 0, 1,
                "18:00", "24:00", 22, 16, 24, new String[]{"single"}, "tolerant", 90));

        // 4 - families, counted in units not beds
        BedPool fam = pool("pool-04-fam", "Fraser Family Lodge", "East Vancouver", false,
                "FAM", new String[]{"ACC"}, 12, 9, 0, 1,
                "14:00", "21:00", 35, -1, -1, new String[]{"family", "couple"}, "tolerant", 60);
        fam.countUnit = "family units";
        list.add(fam);

        // 5a - mixed singles, pets ok
        list.add(pool("pool-05-mix", "West End Couples & Singles", "West End", false,
                "MIX", new String[]{"PET"}, 25, 20, 0, 1,
                "17:00", "22:30", 18, 18, -1, new String[]{"single", "couple"}, "tolerant", 30));

        // 5b - couples pool, FULL in the Sara scenario
        list.add(pool("pool-05-cpl", "West End Couples & Singles", "West End", false,
                "CPL", new String[]{"PET"}, 8, 7, 1, 0,
                "17:00", "22:30", 18, 18, -1, new String[]{"couple"}, "tolerant", 30));

        // 6 - Indigenous-led, low-barrier, mental-health supports (freshness: aging)
        list.add(pool("pool-06-mix", "Cedar Healing Lodge", "Commercial Drive", false,
                "MIX", new String[]{"IND", "LOW", "MH"}, 20, 14, 0, 1,
                "16:00", "22:00", 41, 18, -1, new String[]{"single", "couple"}, "tolerant", 30));

        // 7 - sobriety program: a low-barrier request must EXCLUDE this pool
        list.add(pool("pool-07-mix", "St. Brigid's Sobriety House", "Mount Pleasant", false,
                "MIX", new String[]{"SOB"}, 16, 13, 0, 1,
                "16:00", "21:00", 12, 18, -1, new String[]{"single"}, "program-required", 90));

        // 8 - medical respite, referral-only (hospital)
        BedPool med = pool("pool-08-mix", "Mercy Respite (Health Partner)", "Fairview", false,
                "MIX", new String[]{"MED"}, 10, 8, 1, 0,
                "00:00", "24:00", 9, 18, -1, new String[]{"single"}, "tolerant", 14);
        med.referralOnly = true;
        med.referralTypeNeeded = "hospital";
        list.add(med);

        // 9 - VAW house: hidden, relay match only, name/area never shown in results
        list.add(pool("pool-09-wom", "Safe Night VAW House", "HIDDEN", true,
                "WOM", new String[]{"LOW", "ACC"}, 14, 10, 0, 1,
                "00:00", "24:00", 16, 18, -1, new String[]{"single", "family"}, "tolerant", 30));

        // 10 - EWR pop-up: NOT activated at 5 degrees/rain, admin can activate it
        BedPool ewr = pool("pool-10-mix", "Rainier EWR Pop-Up", "Downtown Eastside", false,
                "MIX", new String[]{"LOW"}, 50, 0, 0, 0,
                "20:00", "02:00", -1, 18, -1, new String[]{"single", "couple"}, "tolerant", 1);
        ewr.countUnit = "mats";
        ewr.active = false;
        list.add(ewr);

        return list;
    }

    private static RequestProfile profile(String id, String label, String cat, String[] attrs,
                                          int age, String party, String sobriety, String referrer) {
        RequestProfile r = new RequestProfile();
        r.id = id;
        r.label = label;
        r.categoryNeeded = cat;
        r.attributesNeeded.addAll(Arrays.asList(attrs));
        r.age = age;
        r.party = party;
        r.sobrietyNeed = sobriety;
        r.referrerType = referrer;
        return r;
    }

    public static List<RequestProfile> profiles() {
        List<RequestProfile> list = new ArrayList<RequestProfile>();
        list.add(profile("profile-sara", "Sara - woman, wheelchair, partner + dog, low-barrier",
                "WOM", new String[]{"ACC", "PET", "LOW"}, 34, "couple",
                "must-not-require-sobriety", "outreach worker"));
        list.add(profile("profile-youth", "Youth, 19, needs mental-health supports",
                "YOU", new String[]{"MH"}, 19, "single", "any", "outreach worker"));
        list.add(profile("profile-family", "Family of 4, needs wheelchair access",
                "FAM", new String[]{"ACC"}, 0, "family", "any", "211 navigator"));
        list.add(profile("profile-discharge", "Hospital discharge, medical needs",
                "MIX", new String[]{"MED"}, 58, "single", "any", "hospital"));
        list.add(profile("profile-no-match", "Woman with pet + medical needs (no match - tests turn-away)",
                "WOM", new String[]{"PET", "MED"}, 41, "family",
                "must-not-require-sobriety", "outreach worker"));
        return list;
    }
}
