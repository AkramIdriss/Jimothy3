package engraving;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.Locale;

/**
 * Helpers shared by engraving types that draw Java {@link Shape}s (text outlines, pixels...).
 */
public final class SvgUtil {

    private SvgUtil() { }

    /** Formats a number for SVG: 3 decimals, always with a '.' (never a locale-specific ','). */
    public static String num(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    /** Makes text safe inside an XML attribute; line breaks and non-ASCII become numeric references. */
    public static String escapeAttribute(String text) {
        StringBuilder out = new StringBuilder();
        text.codePoints().forEach(c -> {
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '\'' -> out.append("&apos;");
                case '"' -> out.append("&quot;");
                default -> {
                    if (c == '\n' || c > 126) {
                        out.append("&#").append(c).append(';');
                    } else if (c >= 32) {
                        out.append((char) c);
                    }
                }
            }
        });
        return out.toString();
    }

    /**
     * Scales {@code shape} (keeping its proportions) so it fits inside {@code area}, centres it,
     * and returns it as a single engrave-styled SVG {@code <path>} element.
     * Returns "" if the shape is empty or the area has no room.
     */
    public static String fitAsPath(Shape shape, Bounds area) {
        return fitAsPath(shape, area, "");
    }

    /** Same as {@link #fitAsPath(Shape, Bounds)}, with extra attributes (e.g. {@code data-text='Hi'}) on the element. */
    public static String fitAsPath(Shape shape, Bounds area, String extraAttributes) {
        Rectangle2D box = controlBounds(shape);
        if (box == null || area.isEmpty()) {
            return "";
        }
        Shape fitted = fitTransform(box, area).createTransformedShape(shape);
        String fillRule = fitted.getPathIterator(null).getWindingRule() == PathIterator.WIND_EVEN_ODD
                ? "evenodd" : "nonzero";
        return "<path " + EngraveStyle.attributes() + extraAttributes + " fill-rule='" + fillRule
                + "' d='" + toPathData(fitted) + "' />\n";
    }

    /**
     * Transform that scales a {@code source} rectangle uniformly to fit inside {@code area}
     * and centres it there.
     */
    public static AffineTransform fitTransform(Rectangle2D source, Bounds area) {
        double sw = Math.max(source.getWidth(), 1e-9);
        double sh = Math.max(source.getHeight(), 1e-9);
        double scale = Math.min(area.width() / sw, area.height() / sh);

        AffineTransform t = new AffineTransform();
        t.translate(area.centerX(), area.centerY());
        t.scale(scale, scale);
        t.translate(-source.getCenterX(), -source.getCenterY());
        return t;
    }

    /** Converts a shape to SVG path data using absolute M, L, Q, C and Z commands. */
    public static String toPathData(Shape shape) {
        StringBuilder d = new StringBuilder();
        double[] c = new double[6];
        for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            switch (it.currentSegment(c)) {
                case PathIterator.SEG_MOVETO -> d.append("M ").append(pt(c, 0));
                case PathIterator.SEG_LINETO -> d.append("L ").append(pt(c, 0));
                case PathIterator.SEG_QUADTO -> d.append("Q ").append(pt(c, 0)).append(pt(c, 2));
                case PathIterator.SEG_CUBICTO -> d.append("C ").append(pt(c, 0)).append(pt(c, 2)).append(pt(c, 4));
                case PathIterator.SEG_CLOSE -> d.append("Z ");
                default -> { }
            }
        }
        return d.toString().trim();
    }

    /**
     * Bounding box of every point in the shape, <i>including</i> curve control points.
     * Fitting with this box guarantees nothing in the output path can land outside the area.
     * Returns null for a shape with no points.
     */
    public static Rectangle2D controlBounds(Shape shape) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double[] c = new double[6];
        for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next()) {
            int points = switch (it.currentSegment(c)) {
                case PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> 1;
                case PathIterator.SEG_QUADTO -> 2;
                case PathIterator.SEG_CUBICTO -> 3;
                default -> 0;
            };
            for (int i = 0; i < points; i++) {
                minX = Math.min(minX, c[2 * i]);
                maxX = Math.max(maxX, c[2 * i]);
                minY = Math.min(minY, c[2 * i + 1]);
                maxY = Math.max(maxY, c[2 * i + 1]);
            }
        }
        if (minX > maxX) {
            return null;
        }
        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    private static String pt(double[] c, int i) {
        return num(c[i]) + " " + num(c[i + 1]) + " ";
    }
}
