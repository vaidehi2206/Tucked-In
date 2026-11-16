package shelterapp;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** One big-number metric tile for the Admin chapter. */
public class StatTile extends RoundedPanel {

    private final JLabel valueLabel;
    private final JLabel captionLabel;

    public StatTile(String caption, Color bg) {
        super(bg, 22);
        setLayout(new BorderLayout());
        valueLabel = new JLabel("0", JLabel.CENTER);
        valueLabel.setFont(Theme.display(40));
        valueLabel.setForeground(Theme.INK);
        add(valueLabel, BorderLayout.CENTER);
        captionLabel = new JLabel(caption, JLabel.CENTER);
        captionLabel.setFont(Theme.bodyBold(13));
        captionLabel.setForeground(Theme.MUTED);
        JPanel capWrap = new JPanel();
        capWrap.setOpaque(false);
        capWrap.add(captionLabel);
        add(capWrap, BorderLayout.SOUTH);
    }

    public void setValue(int value) {
        valueLabel.setText(String.valueOf(value));
    }
}
