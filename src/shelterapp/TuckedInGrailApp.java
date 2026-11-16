package shelterapp;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Tucked In - Grail-inspired front end (Java Swing / Java2D only, zero JavaScript).
 * Same engine as ShelterApp (EligibilityEngine + HoldManager + SampleData);
 * only the presentation layer changes: colour-block chapters, rounded cards,
 * pill buttons and tabs, stat tiles, big countdowns.
 *
 * The original ShelterApp window is untouched and remains the stable fallback.
 * Demo hold time is 90 seconds (production rule: 45 minutes).
 */
public class TuckedInGrailApp extends JFrame {

    private static final int DEMO_HOLD_SECONDS = 90;
    private static final String[] CHAPTERS = {"Tonight", "Worker", "Console", "Admin"};
    private static final String[] CHAPTER_TITLES = {
        "Chapter 00 · Tonight", "Chapter 01 · Worker — find a bed",
        "Chapter 02 · Shelter Console", "Chapter 03 · City Admin — tonight in numbers"};

    private final HoldManager manager;
    private final List<RequestProfile> profiles;
    private final int scenarioMinutes = SampleData.SCENARIO_MINUTES;

    private CardLayout cardLayout;
    private JPanel chapters;
    private ChapterHeader header;
    private final List<PillButton> navButtons = new ArrayList<PillButton>();

    // Worker chapter
    private JPanel profileTabs;
    private final List<PillButton> profileButtons = new ArrayList<PillButton>();
    private RequestProfile currentProfile;
    private JPanel resultsPanel;
    private JTextArea exclusionArea;
    private JTextArea myHoldsArea;

    // Console chapter
    private JComboBox<String> poolBox;
    private JLabel poolStatus;
    private DefaultListModel<String> inboxModel;
    private JList<String> inboxList;
    private List<Hold> inboxHolds = new ArrayList<Hold>();
    private DefaultListModel<String> arrivalsModel;
    private JList<String> arrivalsList;
    private List<Hold> arrivalHolds = new ArrayList<Hold>();
    private String lastSignature = "";

    // Admin chapter
    private StatTile openTile;
    private StatTile holdsTile;
    private StatTile turnTile;
    private StatTile staleTile;
    private JTextArea tonightArea;
    private JTextArea logArea;

    public TuckedInGrailApp() {
        super("Tucked In — Everyone tucked in for the night (sample data)");
        manager = new HoldManager(SampleData.pools(), DEMO_HOLD_SECONDS);
        profiles = SampleData.profiles();
        currentProfile = profiles.get(0);

        setLayout(new BorderLayout());
        header = new ChapterHeader(CHAPTER_TITLES[0]);
        add(header, BorderLayout.NORTH);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        nav.setBackground(Theme.INK);
        for (int i = 0; i < CHAPTERS.length; i++) {
            final int idx = i;
            PillButton b = new PillButton(CHAPTERS[i], i == 0 ? Theme.GOLD : Theme.MUTED,
                    i == 0 ? Theme.INK : Theme.CREAM);
            b.addActionListener(e -> showChapter(idx));
            navButtons.add(b);
            nav.add(b);
        }
        JPanel topWrap = new JPanel(new BorderLayout());
        topWrap.add(header, BorderLayout.NORTH);
        topWrap.add(nav, BorderLayout.SOUTH);
        add(topWrap, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        chapters = new JPanel(cardLayout);
        chapters.add(buildHeroChapter(), CHAPTERS[0]);
        chapters.add(buildWorkerChapter(), CHAPTERS[1]);
        chapters.add(buildConsoleChapter(), CHAPTERS[2]);
        chapters.add(buildAdminChapter(), CHAPTERS[3]);
        add(chapters, BorderLayout.CENTER);

        Timer timer = new Timer(1000, e -> {
            manager.expireHolds();
            refreshDynamic();
        });
        timer.start();

        setSize(1080, 760);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        doSearch();
        refreshDynamic();
    }

    private void showChapter(int idx) {
        cardLayout.show(chapters, CHAPTERS[idx]);
        header.setChapter(CHAPTER_TITLES[idx]);
        refreshDynamic();
    }

    // ---------- Chapter 00: Hero ----------
    private JPanel buildHeroChapter() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(Theme.INK);
        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setOpaque(false);

        JLabel kicker = new JLabel("ONE LIVE INVENTORY · EVERY SHELTER · TONIGHT", JLabel.CENTER);
        kicker.setFont(Theme.bodyBold(14));
        kicker.setForeground(Theme.LAVENDER);
        kicker.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(kicker);

        JLabel title = new JLabel("EVERYONE TUCKED IN", JLabel.CENTER);
        title.setFont(Theme.display(52));
        title.setForeground(Theme.CREAM);
        title.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(title);
        JLabel title2 = new JLabel("FOR THE NIGHT", JLabel.CENTER);
        title2.setFont(Theme.display(52));
        title2.setForeground(Theme.GOLD);
        title2.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(title2);

        JLabel sub = new JLabel("Search by what a person actually needs. Hold the bed. Get it confirmed. Then travel.", JLabel.CENTER);
        sub.setFont(Theme.body(16));
        sub.setForeground(Theme.CREAM);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        centre.add(sub);

        PillButton start = new PillButton("Start with Sara →", Theme.TERRA, Theme.CREAM);
        start.setAlignmentX(CENTER_ALIGNMENT);
        start.addActionListener(e -> {
            selectProfile(0);
            showChapter(1);
        });
        JPanel btnWrap = new JPanel();
        btnWrap.setOpaque(false);
        btnWrap.add(start);
        centre.add(btnWrap);

        // Floating preview cards row (static glance at tonight)
        JPanel cards = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        cards.setOpaque(false);
        for (String pid : new String[]{"pool-01-wom", "pool-05-cpl", "pool-10-mix"}) {
            BedPool p = manager.pool(pid);
            RoundedPanel mini = new RoundedPanel(Theme.CARD, 18);
            mini.setLayout(new BorderLayout());
            mini.setPreferredSize(new Dimension(300, 92));
            JLabel n = new JLabel(" " + (p.hiddenSite ? "Confidential site" : p.siteName));
            n.setFont(Theme.bodyBold(14));
            n.setForeground(Theme.INK);
            mini.add(n, BorderLayout.NORTH);
            JLabel c = new JLabel(" " + (p.active ? p.openCount() + " " + p.countUnit + " open" : "Not activated — weather call"));
            c.setFont(Theme.display(20));
            c.setForeground(Theme.TERRA);
            mini.add(c, BorderLayout.CENTER);
            cards.add(mini);
        }
        centre.add(cards);
        hero.add(centre, BorderLayout.CENTER);
        return hero;
    }

    // ---------- Chapter 01: Worker ----------
    private JPanel buildWorkerChapter() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(Theme.CREAM);

        profileTabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        profileTabs.setBackground(Theme.CREAM);
        String[] shortNames = {"Sara", "Youth", "Family", "Discharge", "No-match"};
        for (int i = 0; i < profiles.size(); i++) {
            final int idx = i;
            PillButton b = new PillButton(shortNames[i], i == 0 ? Theme.TERRA : Theme.MUTED,
                    Theme.CREAM);
            b.addActionListener(e -> {
                selectProfile(idx);
                doSearch();
            });
            profileButtons.add(b);
            profileTabs.add(b);
        }
        page.add(profileTabs, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Theme.CREAM);
        page.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setBackground(Theme.CREAM);
        exclusionArea = new JTextArea(5, 80);
        exclusionArea.setEditable(false);
        exclusionArea.setFont(Theme.mono(12));
        bottom.add(new JScrollPane(exclusionArea), BorderLayout.NORTH);
        myHoldsArea = new JTextArea(4, 80);
        myHoldsArea.setEditable(false);
        myHoldsArea.setFont(Theme.mono(12));
        bottom.add(new JScrollPane(myHoldsArea), BorderLayout.SOUTH);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private void selectProfile(int idx) {
        currentProfile = profiles.get(idx);
    }

    private void doSearch() {
        List<BedPool> matches = EligibilityEngine.search(manager.pools(), currentProfile, scenarioMinutes);
        resultsPanel.removeAll();
        if (matches.isEmpty()) {
            JLabel none = new JLabel("No eligible bed tonight for this profile.");
            none.setFont(Theme.heading(18));
            none.setForeground(Theme.TERRA);
            resultsPanel.add(none);
            String code = EligibilityEngine.suggestTurnAwayCode(manager.pools(), currentProfile, scenarioMinutes);
            int answer = JOptionPane.showConfirmDialog(this,
                    "Log this failed placement as a turn-away (" + code + ")?",
                    "Turn-away", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                manager.logTurnAway(code, currentProfile.label);
            }
        } else {
            for (BedPool p : matches) {
                BedCardPanel card = new BedCardPanel(p);
                card.setMaximumSize(new Dimension(980, 150));
                card.getHoldButton().addActionListener(e -> doPlaceHold(card.getPool()));
                resultsPanel.add(card);
            }
        }
        StringBuilder sb = new StringBuilder("Why the other pools were excluded (hard filters, not rankings):\n");
        for (BedPool p : manager.pools()) {
            if (matches.contains(p)) {
                continue;
            }
            if (p.referralOnly && !currentProfile.referrerType.equals(p.referralTypeNeeded)) {
                continue;
            }
            String reason = EligibilityEngine.exclusionReason(p, currentProfile, scenarioMinutes);
            sb.append("  - ").append(p.hiddenSite ? "Confidential site" : p.siteName)
                    .append(" [").append(p.category).append("]: ").append(reason).append("\n");
        }
        exclusionArea.setText(sb.toString());
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void doPlaceHold(BedPool p) {
        Hold h = manager.placeHold(p.id, currentProfile.label);
        if (h == null) {
            JOptionPane.showMessageDialog(this, "That bed was just taken. Search again — counts are live.");
            doSearch();
            return;
        }
        JOptionPane.showMessageDialog(this, "Hold #" + h.id + " placed at " + h.siteName
                + ".\nHELD is not CONFIRMED — the shelter has the final say.\nDemo countdown: "
                + DEMO_HOLD_SECONDS + " seconds (production: 45 minutes).");
        doSearch();
        refreshDynamic();
    }

    // ---------- Chapter 02: Console ----------
    private JPanel buildConsoleChapter() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(Theme.SAGE);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Theme.SAGE);
        top.add(new JLabel("Pool:"));
        poolBox = new JComboBox<String>();
        for (BedPool p : manager.pools()) {
            poolBox.addItem(p.siteName + " [" + p.category + "]");
        }
        poolBox.setPreferredSize(new Dimension(320, 26));
        poolBox.addActionListener(e -> refreshConsole(true));
        top.add(poolBox);
        poolStatus = new JLabel();
        poolStatus.setFont(Theme.bodyBold(14));
        poolStatus.setForeground(Theme.INK);
        top.add(poolStatus);
        page.add(top, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        actions.setBackground(Theme.SAGE);
        PillButton openBtn = new PillButton("Beds open (3)", Theme.SAGE_DARK, Theme.CREAM);
        openBtn.addActionListener(e -> setOpenTarget(3));
        actions.add(openBtn);
        PillButton lastBtn = new PillButton("Last bed", Theme.GOLD, Theme.INK);
        lastBtn.addActionListener(e -> setOpenTarget(1));
        actions.add(lastBtn);
        PillButton fullBtn = new PillButton("Full", Theme.TERRA, Theme.CREAM);
        fullBtn.addActionListener(e -> setOpenTarget(0));
        actions.add(fullBtn);

        JPanel lists = new JPanel(new GridLayout(2, 1, 8, 8));
        lists.setBackground(Theme.SAGE);
        JPanel inboxPanel = new JPanel(new BorderLayout(4, 4));
        inboxPanel.setOpaque(false);
        inboxPanel.add(new JLabel("Hold inbox — you always have the final say"), BorderLayout.NORTH);
        inboxModel = new DefaultListModel<String>();
        inboxList = new JList<String>(inboxModel);
        inboxList.setVisibleRowCount(5);
        inboxPanel.add(new JScrollPane(inboxList), BorderLayout.CENTER);
        JPanel inboxBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inboxBtns.setOpaque(false);
        JButton confirmBtn = new JButton("Confirm hold");
        confirmBtn.addActionListener(e -> doConfirm());
        inboxBtns.add(confirmBtn);
        JButton rejectBtn = new JButton("Reject with reason");
        rejectBtn.addActionListener(e -> doReject());
        inboxBtns.add(rejectBtn);
        inboxPanel.add(inboxBtns, BorderLayout.SOUTH);
        lists.add(inboxPanel);

        JPanel arrPanel = new JPanel(new BorderLayout(4, 4));
        arrPanel.setOpaque(false);
        arrPanel.add(new JLabel("Arrivals — confirmed holds"), BorderLayout.NORTH);
        arrivalsModel = new DefaultListModel<String>();
        arrivalsList = new JList<String>(arrivalsModel);
        arrivalsList.setVisibleRowCount(5);
        arrPanel.add(new JScrollPane(arrivalsList), BorderLayout.CENTER);
        JPanel arrBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arrBtns.setOpaque(false);
        JButton checkBtn = new JButton("Check in");
        checkBtn.addActionListener(e -> doCheckIn());
        arrBtns.add(checkBtn);
        JButton noBtn = new JButton("No-show");
        noBtn.addActionListener(e -> doNoShow());
        arrBtns.add(noBtn);
        arrPanel.add(arrBtns, BorderLayout.SOUTH);
        lists.add(arrPanel);

        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(Theme.SAGE);
        centre.add(actions, BorderLayout.NORTH);
        centre.add(lists, BorderLayout.CENTER);
        page.add(centre, BorderLayout.CENTER);
        return page;
    }

    private BedPool selectedPool() {
        return manager.pools().get(poolBox.getSelectedIndex());
    }

    private void setOpenTarget(int target) {
        BedPool p = selectedPool();
        int others = p.confirmedHolds + p.walkInReserve + p.pendingHolds;
        int nc = p.physical - others - target;
        p.checkedIn = Math.max(0, Math.min(nc, p.physical));
        p.lastUpdatedMinAgo = 0;
        refreshConsole(true);
        refreshDynamic();
    }

    private void refreshConsole(boolean force) {
        if (poolBox == null) {
            return;
        }
        BedPool selected = selectedPool();
        poolStatus.setText(selected.active
                ? selected.openCount() + " " + selected.countUnit + " open · updated "
                + selected.lastUpdatedMinAgo + " min ago (" + selected.freshness() + ")"
                : "NOT ACTIVATED");
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
        if (!force && sig.toString().equals(lastSignature)) {
            return;
        }
        lastSignature = sig.toString();
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
    }

    private void doConfirm() {
        int idx = inboxList.getSelectedIndex();
        if (idx < 0 || idx >= inboxHolds.size()) {
            JOptionPane.showMessageDialog(this, "Select a hold in the inbox first.");
            return;
        }
        manager.confirmHold(inboxHolds.get(idx));
        refreshConsole(true);
        refreshDynamic();
    }

    private void doReject() {
        int idx = inboxList.getSelectedIndex();
        if (idx < 0 || idx >= inboxHolds.size()) {
            JOptionPane.showMessageDialog(this, "Select a hold in the inbox first.");
            return;
        }
        String reason = JOptionPane.showInputDialog(this, "Reason for rejecting:", "Reject hold",
                JOptionPane.PLAIN_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) {
            reason = "no reason given";
        }
        manager.rejectHold(inboxHolds.get(idx), reason.trim());
        refreshConsole(true);
        refreshDynamic();
    }

    private void doCheckIn() {
        int idx = arrivalsList.getSelectedIndex();
        if (idx < 0 || idx >= arrivalHolds.size()) {
            JOptionPane.showMessageDialog(this, "Select a confirmed hold in Arrivals first.");
            return;
        }
        manager.checkIn(arrivalHolds.get(idx));
        refreshConsole(true);
        refreshDynamic();
    }

    private void doNoShow() {
        int idx = arrivalsList.getSelectedIndex();
        if (idx < 0 || idx >= arrivalHolds.size()) {
            JOptionPane.showMessageDialog(this, "Select a confirmed hold in Arrivals first.");
            return;
        }
        manager.noShow(arrivalHolds.get(idx));
        refreshConsole(true);
        refreshDynamic();
    }

    // ---------- Chapter 03: Admin ----------
    private JPanel buildAdminChapter() {
        JPanel page = new JPanel(new BorderLayout(8, 8));
        page.setBackground(Theme.INK);

        JPanel tiles = new JPanel(new GridLayout(1, 4, 12, 12));
        tiles.setBackground(Theme.INK);
        openTile = new StatTile("OPEN BEDS TONIGHT", Theme.CREAM);
        holdsTile = new StatTile("ACTIVE HOLDS", Theme.LAVENDER);
        turnTile = new StatTile("TURN-AWAYS LOGGED", Theme.ROSE);
        staleTile = new StatTile("POOLS NOT FRESH", Theme.GOLD);
        tiles.add(openTile);
        tiles.add(holdsTile);
        tiles.add(turnTile);
        tiles.add(staleTile);
        page.add(tiles, BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(6, 6));
        centre.setBackground(Theme.INK);
        tonightArea = new JTextArea(12, 90);
        tonightArea.setEditable(false);
        tonightArea.setFont(Theme.mono(13));
        centre.add(new JScrollPane(tonightArea), BorderLayout.CENTER);
        logArea = new JTextArea(7, 90);
        logArea.setEditable(false);
        logArea.setFont(Theme.mono(12));
        centre.add(new JScrollPane(logArea), BorderLayout.SOUTH);
        page.add(centre, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBackground(Theme.INK);
        PillButton ewr = new PillButton("Activate Rainier EWR Pop-Up — 50 mats", Theme.TERRA, Theme.CREAM);
        ewr.addActionListener(e -> {
            BedPool p = manager.pool("pool-10-mix");
            p.active = true;
            p.lastUpdatedMinAgo = 0;
            JOptionPane.showMessageDialog(this, "EWR activated: 50 mats are now searchable system-wide.");
            refreshDynamic();
        });
        bottom.add(ewr);
        page.add(bottom, BorderLayout.SOUTH);
        return page;
    }

    private void refreshDynamic() {
        // My tonight (worker)
        if (myHoldsArea != null) {
            List<Hold> holds = manager.holds();
            if (holds.isEmpty()) {
                myHoldsArea.setText("My tonight: no holds yet.");
            } else {
                StringBuilder sb = new StringBuilder("My tonight:\n");
                long now = System.currentTimeMillis();
                for (Hold h : holds) {
                    sb.append("  ").append(h.describe(now)).append("\n");
                }
                myHoldsArea.setText(sb.toString());
            }
        }
        refreshConsole(false);
        // Admin tiles + boards
        if (openTile != null) {
            int open = 0;
            int notFresh = 0;
            for (BedPool p : manager.pools()) {
                open += p.openCount();
                if (p.active && p.lastUpdatedMinAgo > 30) {
                    notFresh++;
                }
            }
            int activeHolds = 0;
            for (Hold h : manager.holds()) {
                if (Hold.HELD.equals(h.status) || Hold.CONFIRMED.equals(h.status)) {
                    activeHolds++;
                }
            }
            openTile.setValue(open);
            holdsTile.setValue(activeHolds);
            turnTile.setValue(manager.turnAways().size());
            staleTile.setValue(notFresh);

            StringBuilder sb = new StringBuilder("TONIGHT BOARD — sample data\n");
            sb.append(String.format("%-34s %-6s %8s %8s %10s%n", "Pool", "Cat", "Open", "Total", "Freshness"));
            for (BedPool p : manager.pools()) {
                sb.append(String.format("%-34s %-6s %8d %8d %10s%n",
                        p.hiddenSite ? "Confidential site" : p.siteName, p.category,
                        p.openCount(), p.physical, p.active ? p.freshness() : "inactive"));
            }
            tonightArea.setText(sb.toString());
            StringBuilder ev = new StringBuilder("EVENT LOG:\n");
            for (String e2 : manager.eventLog()) {
                ev.append("  ").append(e2).append("\n");
            }
            logArea.setText(ev.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TuckedInGrailApp().setVisible(true));
    }
}
