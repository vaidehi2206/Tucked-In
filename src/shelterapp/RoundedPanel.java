package shelterapp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** A panel painted as a rounded card with a soft shadow (Java2D). */
public class RoundedPanel extends JPanel {

    private final Color bg;
    private final int radius;

    public RoundedPanel(Color bg, int radius) {
        this.bg = bg;
        this.radius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        // soft shadow
        g2.setColor(new Color(0, 0, 0, 28));
        g2.fillRoundRect(3, 5, w - 6, h - 8, radius, radius);
        // card body
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w - 6, h - 8, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}
