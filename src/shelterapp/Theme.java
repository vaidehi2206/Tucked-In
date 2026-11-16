package shelterapp;

import java.awt.Color;
import java.awt.Font;

/**
 * Tucked In visual theme - Grail-inspired design grammar
 * (colour-block chapters, huge type, rounded cards, pill controls)
 * translated into a warm "night" palette. Pure Java/Swing, no JavaScript.
 */
public class Theme {

    public static final Color INK = new Color(0x14, 0x16, 0x1A);
    public static final Color GOLD = new Color(0xE9, 0xB4, 0x4C);
    public static final Color CREAM = new Color(0xFF, 0xF6, 0xE8);
    public static final Color TERRA = new Color(0xD9, 0x6C, 0x47);
    public static final Color SAGE = new Color(0xDC, 0xE8, 0xDC);
    public static final Color SAGE_DARK = new Color(0x2F, 0x5D, 0x3A);
    public static final Color ROSE = new Color(0xF3, 0xD9, 0xD7);
    public static final Color LAVENDER = new Color(0xE9, 0xC6, 0xE8);
    public static final Color CARD = Color.WHITE;
    public static final Color AMBER = new Color(0xB8, 0x86, 0x0B);
    public static final Color MUTED = new Color(0x5A, 0x5A, 0x5A);

    public static Font display(int size) {
        return new Font("Serif", Font.BOLD, size);
    }

    public static Font heading(int size) {
        return new Font("SansSerif", Font.BOLD, size);
    }

    public static Font body(int size) {
        return new Font("SansSerif", Font.PLAIN, size);
    }

    public static Font bodyBold(int size) {
        return new Font("SansSerif", Font.BOLD, size);
    }

    public static Font mono(int size) {
        return new Font("Monospaced", Font.PLAIN, size);
    }
}
