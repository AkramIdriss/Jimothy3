package engraving;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * One or more lines of text in any font, engraved as vector outlines.
 *
 * <p>Because the letters are converted to shapes, the SVG looks the same on every computer,
 * even one that doesn't have the font installed. Lines are centred under each other and the
 * whole block is scaled to fit the area.
 */
public class TextEngraving implements Engraving {

    /** Space between lines, as a multiple of the font size. */
    private static final double LINE_SPACING = 1.2;

    private final List<String> lines;
    private final Font font;

    /**
     * @param lines the text, one entry per line; blank lines are dropped
     * @param font  the font to draw with (see {@link Fonts})
     * @throws IllegalArgumentException if there is no non-blank line
     */
    public TextEngraving(List<String> lines, Font font) {
        this.lines = lines.stream().map(String::strip).filter(s -> !s.isEmpty()).toList();
        this.font = font;
        if (this.lines.isEmpty()) {
            throw new IllegalArgumentException("Text engraving needs at least one non-blank line");
        }
    }

    public List<String> lines() {
        return lines;
    }

    public Font font() {
        return font;
    }

    @Override
    public String toSvg(Bounds area) {
        // The words are kept beside the outlines so the terminal preview can show them as text.
        String words = " data-text='" + SvgUtil.escapeAttribute(String.join("\n", lines)) + "'";
        return SvgUtil.fitAsPath(outline(), area, words);
    }

    /** All lines as one shape, centred horizontally around x = 0. */
    Shape outline() {
        FontRenderContext frc = new FontRenderContext(null, true, true);
        double lineHeight = font.getSize2D() * LINE_SPACING;
        Path2D.Double all = null;

        for (int i = 0; i < lines.size(); i++) {
            GlyphVector glyphs = font.createGlyphVector(frc, lines.get(i));
            Rectangle2D advance = glyphs.getLogicalBounds();
            AffineTransform place = AffineTransform.getTranslateInstance(-advance.getWidth() / 2.0, i * lineHeight);
            Shape line = place.createTransformedShape(glyphs.getOutline());
            if (all == null) {
                all = new Path2D.Double(line.getPathIterator(null).getWindingRule());
            }
            all.append(line, false);
        }
        return all;
    }

    @Override
    public String describe() {
        return "Text \"" + String.join(" / ", lines) + "\" (" + font.getFamily() + ")";
    }
}
