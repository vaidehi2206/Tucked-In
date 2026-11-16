package shelterapp;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The sticky chapter bar, translated from Grail's "About [section] / Chapter NN"
 * indicator: product name left, chapter centre, scenario context right.
 */
public class ChapterHeader extends JPanel {

    private final JLabel chapterLabel;

    public ChapterHeader(String chapterText) {
        setLayout(new BorderLayout());
        setBackground(Theme.INK);

        JLabel brand = new JLabel("  TUCKED IN");
        brand.setFont(Theme.heading(16));
        brand.setForeground(Theme.GOLD);
        add(brand, BorderLayout.WEST);

        chapterLabel = new JLabel(chapterText, JLabel.CENTER);
        chapterLabel.setFont(Theme.bodyBold(14));
        chapterLabel.setForeground(Color.WHITE);
        add(chapterLabel, BorderLayout.CENTER);

        JLabel context = new JLabel("Rainy night · 8:40 pm · 5°C  ");
        context.setFont(Theme.body(13));
        context.setForeground(Theme.LAVENDER);
        add(context, BorderLayout.EAST);
    }

    public void setChapter(String chapterText) {
        chapterLabel.setText(chapterText);
    }
}
