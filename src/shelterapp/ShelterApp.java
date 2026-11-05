package shelterapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Shelter Bed Reservation - prototype UI (Swing).
 * Three surfaces, one shared inventory (HoldManager):
 *   1. Worker App      - eligibility search + holds
 *   2. Shelter Console - availability updates, hold inbox, arrivals
 *   3. City Admin      - tonight board, turn-away data, EWR activation
 *
 * Demo note: a hold lasts 90 seconds here so expiry can be shown live.
 * The production rule from the PRD is 45 minutes, set by the shelter.
 */
public class ShelterApp extends JFrame {

    private static final int DEMO_HOLD_SECONDS = 90;

    private final HoldManager manager;
    private final List<RequestProfile> profiles;
    private final int scenarioMinutes = SampleData.SCENARIO_MINUTES;

    // Worker tab
    private JComboBox<String> profileBox;
    private JTextArea resultsArea;
    private JComboBox<String> matchBox;
    private List<BedPool> lastMatches;
    private RequestProfile currentProfile;
    private JTextArea myHoldsArea;

    // Console tab
    private JComboBox<String> poolBox;
    private JSpinner checkedInSpinner;
    private JLabel poolStatusLabel;
    private DefaultListModel<String> inboxModel;
    private JList<String> inboxList;
    private List<Hold> inboxHolds;
    private DefaultListModel<String> arrivalsModel;
    private JList<String> arrivalsList;
    private List<Hold> arrivalHolds;
    private String lastInboxSignature = "";

    // Admin tab
    private JTextArea tonightArea;
    private JTextArea turnAwayArea;
    private JTextArea eventArea;

    public ShelterApp() {
        super("Tucked In - Shelter Bed Reservation (sample data)");
        manager = new HoldManager(SampleData.pools(), DEMO_HOLD_SECONDS);
        profiles = SampleData.profiles();
        lastMatches = new ArrayList<BedPool>();
        inboxHolds = new ArrayList<Hold>();
        arrivalHolds = new ArrayList<Hold>();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Worker App", buildWorkerPanel());
        tabs.addTab("Shelter Console", buildConsolePanel());
        tabs.addTab("City Admin", buildAdminPanel());
        add(tabs);

        refreshConsoleLists(true);
        refreshAdmin();

        Timer timer = new Timer(1000, e -> {
            manager.expireHolds();
            refreshMyHolds();
            refreshConsoleLists(false);
            refreshAdmin();
        });
        timer.start();

        setSize(1050, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // ---------------- Worker App ----------------

    private JPanel buildWorkerPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Rainy night, 8:40 pm - search for:"));
        profileBox = new JComboBox<String>();
        for (RequestProfile r : profiles) {
            profileBox.addItem(r.label);
        }
        profileBox.setPreferredSize(new Dimension(430, 26));
        top.add(profileBox);
        JButton searchBtn = new JButton("Search eligible beds");
        searchBtn.addActionListener(e -> doSearch());
        top.add(searchBtn);
        panel.add(top, BorderLayout.NORTH);

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultsArea.setText("Pick a profile and search. Only pools that pass EVERY rule are shown.\n"
                + "A bed that excludes the person is never listed as available.");
        panel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        JPanel holdRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        matchBox = new JComboBox<String>();
        matchBox.setPreferredSize(new Dimension(430, 26));
        holdRow.add(matchBox);
        JButton holdBtn = new JButton("Place hold");
        holdBtn.addActionListener(e -> doPlaceHold());
        holdRow.add(holdBtn);
        JButton raceBtn = new JButton("Demo: two workers race for the last bed");
        raceBtn.addActionListener(e -> doRaceDemo());
        holdRow.add(raceBtn);
        bottom.add(holdRow, BorderLayout.NORTH);

        myHoldsArea = new JTextArea(6, 80);
        myHoldsArea.setEditable(false);
        myHoldsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        myHoldsArea.setText("My tonight: no holds yet.");
        bottom.add(new JScrollPane(myHoldsArea), BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void doSearch() {
        currentProfile = profiles.get(profileBox.getSelectedIndex());
        lastMatches = EligibilityEngine.search(manager.pools(), currentProfile, scenarioMinutes);
        StringBuilder sb = new StringBuilder();
        sb.append("Searching for: ").append(currentProfile.label).append("\n\n");
        matchBox.removeAllItems();
        if (lastMatches.isEmpty()) {
            sb.append("NO ELIGIBLE BED TONIGHT.\n");
            String code = EligibilityEngine.suggestTurnAwayCode(manager.pools(), currentProfile, scenarioMinutes);
            sb.append("Suggested turn-away code: ").append(code).append("\n");
            int answer = JOptionPane.showConfirmDialog(this,
                    "No eligible bed found. Log this as a turn-away (" + code + ")?\n"
                    + "The failed placement becomes data that shows where capacity is missing.",
                    "Log turn-away", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                manager.logTurnAway(code, currentProfile.label);
                sb.append("Logged as turn-away ").append(code).append(".\n");
            }
        } else {
            sb.append("ELIGIBLE - safe to send this person travelling:\n");
            for (BedPool p : lastMatches) {
                String line = displayLine(p);
                sb.append("  * ").append(line).append("\n");
                matchBox.addItem(line);
            }
        }
        sb.append("\nWhy the other pools were excluded (hard filters, not rankings):\n");
        for (BedPool p : manager.pools()) {
            if (lastMatches.contains(p)) {
                continue;
            }
            if (p.referralOnly && !currentProfile.referrerType.equals(p.referralTypeNeeded)) {
                continue; // referral-only pools are invisible, not "excluded", for other referrers
            }
            String reason = EligibilityEngine.exclusionReason(p, currentProfile, scenarioMinutes);
            String name = p.hiddenSite ? "Confidential site" : p.siteName;
            sb.append("  - ").append(name).append(" [").append(p.category).append("]: ").append(reason).append("\n");
        }
        resultsArea.setText(sb.toString());
        refreshAdmin();
    }

    private String displayLine(BedPool p) {
        if (p.hiddenSite) {
            return "Confidential site (relay match) - " + p.openCount() + " beds - updated "
                    + p.lastUpdatedMinAgo + " min ago (" + p.freshness() + ") - location via relay only";
        }
        return p.siteName + " (" + p.area + ") - " + p.openCount() + " " + p.countUnit
                + " open - updated " + p.lastUpdatedMinAgo + " min ago (" + p.freshness()
                + ") - intake until " + p.intakeClose;
    }

    private void doPlaceHold() {
        int idx = matchBox.getSelectedIndex();
        if (idx < 0 || idx >= lastMatches.size()) {
            JOptionPane.showMessageDialog(this, "Search first, then choose a bed to hold.");
            return;
        }
        BedPool p = lastMatches.get(idx);
        Hold h = manager.placeHold(p.id, currentProfile.label);
        if (h == null) {
            JOptionPane.showMessageDialog(this,
                    "That bed was just taken by another worker. Counts are already updated - search again.");
            doSearch();
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Hold #" + h.id + " placed at " + h.siteName + ".\n"
                + "It is HELD, not confirmed - the shelter still has the final say.\n"
                + "Demo countdown: " + DEMO_HOLD_SECONDS + " seconds (production: 45 minutes).");
        refreshMyHolds();
        refreshConsoleLists(true);
        refreshAdmin();
    }

    private void doRaceDemo() {
        final BedPool p = manager.pool("pool-01-wom");
        final Hold[] results = new Hold[2];
        Thread a = new Thread(() -> results[0] = manager.placeHold(p.id, "Race worker A"));
        Thread b = new Thread(() -> results[1] = manager.placeHold(p.id, "Race worker B"));
        a.start();
        b.start();
        try {
            a.join();
            b.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        int wins = (results[0] != null ? 1 : 0) + (results[1] != null ? 1 : 0);
        String msg;
        if (p.openCount() == 0 || wins == 1) {
            msg = "Race finished: exactly " + wins + " worker got the bed.\n"
                    + "The other worker was told instantly and can search again.\n"
                    + "Two workers can never be promised the same last bed.";
        } else {
            msg = "Race finished: " + wins + " holds placed - there were " + wins + "+ beds open.\n"
                    + "Tip for the demo: place Sara's hold first (2 open -> 1), then race for the last bed.";
        }
        JOptionPane.showMessageDialog(this, msg);
        refreshMyHolds();
        refreshConsoleLists(true);
        refreshAdmin();
    }

    private void refreshMyHolds() {
        List<Hold> holds = manager.holds();
        if (holds.isEmpty()) {
            myHoldsArea.setText("My tonight: no holds yet.");
            return;
        }
        StringBuilder sb = new StringBuilder("My tonight:\n");
        long now = System.currentTimeMillis();
        for (Hold h : holds) {
            sb.append("  ").append(h.describe(now)).append("\n");
        }
        myHoldsArea.setText(sb.toString());
    }

    // ---------------- Shelter Console ----------------

    private JPanel buildConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Pool:"));
        poolBox = new JComboBox<String>();
        for (BedPool p : manager.pools()) {
            poolBox.addItem(p.siteName + " [" + p.category + "]");
        }
        poolBox.setPreferredSize(new Dimension(330, 26));
        poolBox.addActionListener(e -> syncConsoleFromPool());
        top.add(poolBox);
        poolStatusLabel = new JLabel();
        top.add(poolStatusLabel);
        panel.add(top, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Checked in:"));
        checkedInSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        controls.add(checkedInSpinner);
        JButton updateBtn = new JButton("Update availability now");
        updateBtn.addActionListener(e -> {
            BedPool p = selectedPool();
            int value = (Integer) checkedInSpinner.getValue();
            p.checkedIn = Math.max(0, Math.min(value, p.physical));
            p.lastUpdatedMinAgo = 0;
            syncConsoleFromPool();
            refreshAdmin();
        });
        controls.add(updateBtn);
        JButton lastBedBtn = new JButton("Last bed");
        lastBedBtn.addActionListener(e -> setOpenTarget(1));
        controls.add(lastBedBtn);
        JButton fullBtn = new JButton("Full");
        fullBtn.addActionListener(e -> setOpenTarget(0));
        controls.add(fullBtn);
        JButton open3Btn = new JButton("Beds open (3)");
        open3Btn.addActionListener(e -> setOpenTarget(3));
        controls.add(open3Btn);

        JPanel lists = new JPanel(new BorderLayout(8, 8));
        JPanel inboxPanel = new JPanel(new BorderLayout(4, 4));
        inboxPanel.add(new JLabel("Hold inbox (waiting for your decision - you always have the final say)"), BorderLayout.NORTH);
        inboxModel = new DefaultListModel<String>();
        inboxList = new JList<String>(inboxModel);
        inboxList.setVisibleRowCount(5);
        inboxPanel.add(new JScrollPane(inboxList), BorderLayout.CENTER);
        JPanel inboxBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton confirmBtn = new JButton("Confirm hold");
        confirmBtn.addActionListener(e -> doConfirm());
        inboxBtns.add(confirmBtn);
        JButton rejectBtn = new JButton("Reject with reason");
        rejectBtn.addActionListener(e -> doReject());
        inboxBtns.add(rejectBtn);
        inboxPanel.add(inboxBtns, BorderLayout.SOUTH);

        JPanel arrivalsPanel = new JPanel(new BorderLayout(4, 4));
        arrivalsPanel.add(new JLabel("Arrivals (confirmed holds)"), BorderLayout.NORTH);
        arrivalsModel = new DefaultListModel<String>();
        arrivalsList = new JList<String>(arrivalsModel);
        arrivalsList.setVisibleRowCount(5);
        arrivalsPanel.add(new JScrollPane(arrivalsList), BorderLayout.CENTER);
        JPanel arrBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton checkInBtn = new JButton("Check in");
        checkInBtn.addActionListener(e -> doCheckIn());
        arrBtns.add(checkInBtn);
        JButton noShowBtn = new JButton("No-show");
        noShowBtn.addActionListener(e -> doNoShow());
        arrBtns.add(noShowBtn);
        arrivalsPanel.add(arrBtns, BorderLayout.SOUTH);

        lists.add(inboxPanel, BorderLayout.NORTH);
        lists.add(arrivalsPanel, BorderLayout.SOUTH);

        // controls + lists stacked in a wrapper
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.add(controls, BorderLayout.NORTH);
        wrapper.add(lists, BorderLayout.CENTER);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private BedPool selectedPool() {
        return manager.pools().get(poolBox.getSelectedIndex());
    }

    private void syncConsoleFromPool() {
        BedPool p = selectedPool();
        checkedInSpinner.setModel(new SpinnerNumberModel(p.checkedIn, 0, p.physical, 1));
        String state = p.active ? (p.openCount() + " " + p.countUnit + " open") : "NOT ACTIVATED";
        poolStatusLabel.setText(state + " - updated " + p.lastUpdatedMinAgo
                + " min ago (" + p.freshness() + ") - intake " + p.intakeOpen + "-" + p.intakeClose);
        refreshConsoleLists(true);
    }

    private void setOpenTarget(int targetOpen) {
        BedPool p = selectedPool();
        int others = p.confirmedHolds + p.walkInReserve + p.pendingHolds;
        int newCheckedIn = p.physical - others - targetOpen;
        p.checkedIn = Math.max(0, Math.min(newCheckedIn, p.physical));
        p.lastUpdatedMinAgo = 0;
        syncConsoleFromPool();
        refreshAdmin();
    }

    private void refreshConsoleLists(boolean force) {
        if (poolBox == null) {
            return;
        }
        BedPool selected = selectedPool();
        long now = System.currentTimeMillis();
        StringBuilder sig = new StringBuilder();
        List<Hold> inbox = new ArrayList<Hold>();
        List<Hold> arrivals = new ArrayList<Hold>();
        for (Hold h : manager.holds()) {
            if (!h.poolId.equals(selected.id)) {
                continue;
            }
            sig.append(h.id).append(h.status).append(';');
            if (Hold.HELD.equals(h.status)) {
                inbox.add(h);
            }
            if (Hold.CONFIRMED.equals(h.status)) {
                arrivals.add(h);
            }
        }
        if (!force && sig.toString().equals(lastInboxSignature)) {
            return;
        }
        lastInboxSignature = sig.toString();
        inboxHolds = inbox;
        arrivalHolds = arrivals;
        inboxModel.clear();
        for (Hold h : inbox) {
            inboxModel.addElement(h.describe(now));
        }
        arrivalsModel.clear();
        for (Hold h : arrivals) {
            arrivalsModel.addElement(h.describe(now));
        }
        syncPoolStatusOnly();
    }

    private void syncPoolStatusOnly() {
        if (poolStatusLabel == null || poolBox == null) {
            return;
        }
        BedPool p = selectedPool();
        String state = p.active ? (p.openCount() + " " + p.countUnit + " open") : "NOT ACTIVATED";
        poolStatusLabel.setText(state + " - updated " + p.lastUpdatedMinAgo
                + " min ago (" + p.freshness() + ") - intake " + p.intakeOpen + "-" + p.intakeClose);
    }

    private Hold selectedInboxHold() {
        int idx = inboxList.getSelectedIndex();
        if (idx < 0 || idx >= inboxHolds.size()) {
            return null;
        }
        return inboxHolds.get(idx);
    }

    private Hold selectedArrivalHold() {
        int idx = arrivalsList.getSelectedIndex();
        if (idx < 0 || idx >= arrivalHolds.size()) {
            return null;
        }
        return arrivalHolds.get(idx);
    }

    private void doConfirm() {
        Hold h = selectedInboxHold();
        if (h == null) {
            JOptionPane.showMessageDialog(this, "Select a hold in the inbox first.");
            return;
        }
        manager.confirmHold(h);
        JOptionPane.showMessageDialog(this, "Hold #" + h.id + " confirmed.\n"
                + "Only now does the worker see the exact location and send the person travelling.");
        refreshMyHolds();
        refreshConsoleLists(true);
        refreshAdmin();
    }

    private void doReject() {
        Hold h = selectedInboxHold();
        if (h == null) {
            JOptionPane.showMessageDialog(this, "Select a hold in the inbox first.");
            return;
        }
        String reason = JOptionPane.showInputDialog(this,
                "Reason for rejecting (recorded, helps the worker re-search):",
                "Reject hold", JOptionPane.PLAIN_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) {
            reason = "no reason given";
        }
        manager.rejectHold(h, reason.trim());
        refreshMyHolds();
        refreshConsoleLists(true);
        refreshAdmin();
    }

    private void doCheckIn() {
        Hold h = selectedArrivalHold();
        if (h == null) {
            JOptionPane.showMessageDialog(this, "Select a confirmed hold in Arrivals first.");
            return;
        }
        manager.checkIn(h);
        refreshMyHolds();
        refreshConsoleLists(true);
        refreshAdmin();
    }

    private void doNoShow() {
        Hold h = selectedArrivalHold();
        if (h == null) {
            JOptionPane.showMessageDialog(this, "Select a confirmed hold in Arrivals first.");
            return;
        }
        manager.noShow(h);
        refreshMyHolds();
        refreshConsoleLists(true);
        refreshAdmin();
    }

    // ---------------- City Admin ----------------

    private JPanel buildAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton ewrBtn = new JButton("Activate Rainier EWR Pop-Up (weather decision)");
        ewrBtn.addActionListener(e -> {
            BedPool ewr = manager.pool("pool-10-mix");
            ewr.active = true;
            ewr.lastUpdatedMinAgo = 0;
            JOptionPane.showMessageDialog(this,
                    "EWR site activated: 50 mats just became searchable across the whole system.");
            refreshAdmin();
        });
        top.add(ewrBtn);
        JButton refreshBtn = new JButton("Refresh boards");
        refreshBtn.addActionListener(e -> refreshAdmin());
        top.add(refreshBtn);
        panel.add(top, BorderLayout.NORTH);

        JPanel areas = new JPanel(new BorderLayout(8, 8));
        tonightArea = new JTextArea(14, 90);
        tonightArea.setEditable(false);
        tonightArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areas.add(new JScrollPane(tonightArea), BorderLayout.NORTH);

        JPanel lower = new JPanel(new BorderLayout(8, 8));
        turnAwayArea = new JTextArea(6, 90);
        turnAwayArea.setEditable(false);
        turnAwayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        lower.add(new JScrollPane(turnAwayArea), BorderLayout.NORTH);
        eventArea = new JTextArea(8, 90);
        eventArea.setEditable(false);
        eventArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lower.add(new JScrollPane(eventArea), BorderLayout.SOUTH);
        areas.add(lower, BorderLayout.CENTER);
        panel.add(areas, BorderLayout.CENTER);
        return panel;
    }

    private void refreshAdmin() {
        if (tonightArea == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("TONIGHT BOARD - rainy night, 8:40 pm (sample data)\n");
        sb.append(String.format("%-34s %-6s %8s %8s %10s%n", "Pool", "Cat", "Open", "Total", "Freshness"));
        int totalOpen = 0;
        for (BedPool p : manager.pools()) {
            String name = p.hiddenSite ? "Confidential site" : p.siteName;
            String fresh = p.active ? p.freshness() : "inactive";
            sb.append(String.format("%-34s %-6s %8d %8d %10s%n",
                    name, p.category, p.openCount(), p.physical, fresh));
            totalOpen += p.openCount();
        }
        sb.append("Total open tonight: ").append(totalOpen).append("\n");
        tonightArea.setText(sb.toString());

        StringBuilder ta = new StringBuilder("UNMET DEMAND - turn-aways tonight (the data that shows where beds are missing):\n");
        List<String> turnAways = manager.turnAways();
        if (turnAways.isEmpty()) {
            ta.append("  none logged yet - run the no-match profile search in the Worker App.\n");
        }
        for (String t : turnAways) {
            ta.append("  ").append(t).append("\n");
        }
        turnAwayArea.setText(ta.toString());

        StringBuilder ev = new StringBuilder("EVENT LOG (every hold transition, in order):\n");
        for (String e2 : manager.eventLog()) {
            ev.append("  ").append(e2).append("\n");
        }
        eventArea.setText(ev.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ShelterApp().setVisible(true));
    }
}
