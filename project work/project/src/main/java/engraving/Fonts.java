package engraving;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Finding and loading fonts. Fonts are only used to build outlines in Java, so the final SVG
 * does not need the font installed on the laser cutter's computer.
 */
public final class Fonts {

    private Fonts() { }

    /** Size fonts are laid out at. The outline is rescaled to fit the panel afterwards. */
    static final float LAYOUT_SIZE = 100f;

    /** The font used when the user doesn't pick one. */
    public static Font defaultFont() {
        return named(Font.SANS_SERIF);
    }

    /** A font by family name, e.g. "Serif" or "Georgia". Bold, because thin strokes engrave poorly. */
    public static Font named(String family) {
        return new Font(family, Font.BOLD, 1).deriveFont(LAYOUT_SIZE);
    }

    /** Loads a TrueType/OpenType font file (.ttf / .otf), exactly as the file draws it. */
    public static Font fromFile(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("Font file not found: " + file);
        }
        try {
            return Font.createFont(Font.TRUETYPE_FONT, file.toFile()).deriveFont(LAYOUT_SIZE);
        } catch (FontFormatException e) {
            throw new IOException("Not a usable font file (" + e.getMessage() + "): " + file);
        }
    }

    /**
     * Finds a font from one word the user typed: serif, sans, mono, part of an installed font's
     * name, or a path to a .ttf/.otf file.
     *
     * @throws IOException with a friendly message if there is no such font
     */
    public static Font find(String name) throws IOException {
        String n = name.toLowerCase(Locale.ROOT);
        switch (n) {
            case "serif":
                return named(Font.SERIF);
            case "sans", "sans-serif", "sansserif":
                return named(Font.SANS_SERIF);
            case "mono", "monospace", "monospaced":
                return named(Font.MONOSPACED);
            default:
                break;
        }
        if (n.endsWith(".ttf") || n.endsWith(".otf") || n.endsWith(".ttc")) {
            return fromFile(Path.of(name));
        }
        List<String> matches = installedMatching(name, 1);
        if (matches.isEmpty()) {
            throw new IOException("Font not found: " + name
                    + " (use serif, sans, mono, an installed font's name, or a .ttf/.otf file)");
        }
        return named(matches.get(0));
    }

    /** Installed font families whose name contains {@code query} (ignoring case), at most {@code limit}. */
    public static List<String> installedMatching(String query, int limit) {
        String q = query.toLowerCase(Locale.ROOT);
        String[] all = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ROOT);
        return Arrays.stream(all)
                .filter(name -> name.toLowerCase(Locale.ROOT).contains(q))
                .limit(limit)
                .toList();
    }
}
