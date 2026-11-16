package shelterapp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/** A pill-shaped button, Grail-style, painted with Java2D. */
public class PillButton extends JButton {

    private Color fill;
    private final Color baseFill;
    private final Color textColor;

    public PillButton(String text, Color fill, Color textColor) {
        super(text);
        this.fill = fill;
        this.baseFill = fill;
        this.textColor = textColor;
        setFont(Theme.bodyBold(14));
        setForeground(textColor);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                fill = baseFill.brighter();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                fill = baseFill;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(isEnabled() ? fill : fill.darker());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.dispose();
        setForeground(textColor);
        super.paintComponent(g);
    }
}
