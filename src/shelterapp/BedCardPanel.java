package shelterapp;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * One bed pool shown as a Grail-style rounded card:
 * badge, big open count, freshness chip, attribute chips, pill Hold button.
 */
public class BedCardPanel extends RoundedPanel {

    private final BedPool pool;
    private final PillButton holdButton;

    public BedCardPanel(BedPool pool) {
        super(Theme.CARD, 22);
        this.pool = pool;
        setLayout(new BorderLayout(8, 4));

        // Top row: name + category badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        String name = pool.hiddenSite ? "Confidential site (relay match)" : pool.siteName;
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(Theme.heading(16));
        nameLabel.setForeground(Theme.INK);
        top.add(nameLabel, BorderLayout.WEST);
        JLabel badge = new JLabel(" " + pool.category + " ");
        badge.setFont(Theme.bodyBold(12));
        badge.setForeground(Theme.SAGE_DARK);
        top.add(badge, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Middle: huge open count + freshness
        JPanel mid = new JPanel();
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        mid.setOpaque(false);
        JLabel count = new JLabel(pool.openCount() + " " + pool.countUnit + " open");
        count.setFont(Theme.display(30));
        count.setForeground(Theme.TERRA);
        mid.add(count);
        String areaText = pool.hiddenSite ? "Location via relay only" : pool.area;
        JLabel meta = new JLabel(areaText + " · updated " + pool.lastUpdatedMinAgo
                + " min ago (" + pool.freshness() + ") · intake until " + pool.intakeClose);
        meta.setFont(Theme.body(13));
        meta.setForeground(Theme.MUTED);
        mid.add(meta);
        StringBuilder attrs = new StringBuilder();
        for (String a : pool.attributes) {
            if (attrs.length() > 0) {
                attrs.append("  ·  ");
            }
            attrs.append(a);
        }
        JLabel attrLabel = new JLabel(attrs.toString());
        attrLabel.setFont(Theme.bodyBold(12));
        attrLabel.setForeground(Theme.SAGE_DARK);
        mid.add(attrLabel);
        add(mid, BorderLayout.CENTER);

        // Bottom: hold button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        holdButton = new PillButton("Hold this bed", Theme.TERRA, Theme.CREAM);
        bottom.add(holdButton);
        add(bottom, BorderLayout.SOUTH);
    }

    public BedPool getPool() {
        return pool;
    }

    public PillButton getHoldButton() {
        return holdButton;
    }
}
