package engraving;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.Rectangle2D;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;

/** Test helpers for checking engraving SVG output. Public so the box tests can use them too. */
public final class SvgAssert {

    private SvgAssert() { }

    private static final Pattern ENGRAVE_PATH_D =
            Pattern.compile("<path class='engrave'[^>]* d='([^']*)'");
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(\\.\\d+)?");

    /** Fails unless {@code fragment} is well-formed XML once wrapped in an {@code <svg>} element. */
    public static void assertWellFormed(String fragment) {
        String doc = "<svg xmlns='http://www.w3.org/2000/svg'>" + fragment + "</svg>";
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            var builder = f.newDocumentBuilder();
            builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
            builder.parse(new InputSource(new StringReader(doc)));
        } catch (Exception e) {
            fail("Not well-formed SVG: " + e.getMessage() + "\n" + fragment);
        }
    }

    /** Number of engrave-styled elements in an SVG string. */
    public static int countEngravings(String svg) {
        return svg.split("class='engrave'", -1).length - 1;
    }

    /**
     * Bounding box of every coordinate in the engrave {@code <path>} elements of {@code svg}.
     * Works because engraving paths only use absolute M/L/Q/C/Z commands (x y pairs).
     * Returns null if there are no engrave paths.
     */
    public static Rectangle2D engravePathBounds(String svg) {
        Rectangle2D box = null;
        Matcher paths = ENGRAVE_PATH_D.matcher(svg);
        while (paths.find()) {
            Matcher n = NUMBER.matcher(paths.group(1));
            while (n.find()) {
                double x = Double.parseDouble(n.group());
                if (!n.find()) {
                    fail("Odd number of coordinates in path");
                }
                double y = Double.parseDouble(n.group());
                if (box == null) {
                    box = new Rectangle2D.Double(x, y, 0, 0);
                } else {
                    box.add(x, y);
                }
            }
        }
        return box;
    }

    /** Fails unless every engrave-path coordinate in {@code svg} lies inside {@code area}. */
    public static void assertPathsInside(String svg, Bounds area) {
        Rectangle2D box = engravePathBounds(svg);
        assertTrue("No engrave paths found", box != null);
        double tol = 0.01;
        assertTrue("Engraving " + box + " goes outside " + area,
                area.contains(box.getMinX(), box.getMinY(), tol) && area.contains(box.getMaxX(), box.getMaxY(), tol));
    }
}
