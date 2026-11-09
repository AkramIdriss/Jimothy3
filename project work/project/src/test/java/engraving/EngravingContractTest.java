package engraving;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * The rules from {@link Engraving}'s Javadoc, checked for EVERY engraving type.
 *
 * <p><b>Adding a new engraving type?</b> Add a sample of it to {@link #samples()} and these
 * tests check it automatically.
 */
public class EngravingContractTest {

    /** Wide, tall, square and off-origin areas, like real box panels. */
    private static final List<Bounds> AREAS = List.of(
            new Bounds(0, 0, 100, 50),
            new Bounds(200, 160, 30, 90),
            new Bounds(-20, 40, 60, 60),
            new Bounds(12.5, 7.25, 4, 3));

    static List<Engraving> samples() {
        return List.of(
                new TextEngraving(List.of("JDE"), Fonts.defaultFont()),
                new TextEngraving(List.of("Sarah's", "Recipes & More <3"), Fonts.named(Font.SERIF)),
                new TextEngraving(List.of("gjpqy"), Fonts.named(Font.MONOSPACED)),  // descenders
                new ImageEngraving("disc", disc(80, 40), ImageEngraving.DEFAULT_THRESHOLD, false),
                new SvgLogoEngraving("logo", "<svg xmlns='http://www.w3.org/2000/svg' viewBox='10 10 50 25'>"
                        + "<rect x='10' y='10' width='50' height='25' fill='blue'/></svg>"));
    }

    @Test
    public void outputIsWellFormedSvgInEngraveStyle() {
        for (Engraving e : samples()) {
            for (Bounds area : AREAS) {
                String svg = e.toSvg(area);
                SvgAssert.assertWellFormed(svg);
                assertTrue(e.describe() + " should use class='engrave'", SvgAssert.countEngravings(svg) >= 1);
                assertTrue(e.describe() + " should use the engrave colour", svg.contains(EngraveStyle.ENGRAVE_COLOR));
                assertFalse(e.describe() + " must not draw cut lines", svg.contains("class='cut'"));
            }
        }
    }

    @Test
    public void everythingStaysInsideTheArea() {
        for (Engraving e : samples()) {
            for (Bounds area : AREAS) {
                Rectangle2D drawn = drawnBounds(e.toSvg(area));
                double tol = 0.01;
                assertTrue(e.describe() + " drew " + drawn + " outside " + area,
                        area.contains(drawn.getMinX(), drawn.getMinY(), tol)
                                && area.contains(drawn.getMaxX(), drawn.getMaxY(), tol));
            }
        }
    }

    @Test
    public void anEmptyAreaDrawsNothing() {
        for (Engraving e : samples()) {
            assertEquals(e.describe(), "", e.toSvg(new Bounds(10, 10, 0, 20)));
        }
    }

    @Test
    public void everyTypeDescribesItself() {
        for (Engraving e : samples()) {
            assertFalse(e.describe().isBlank());
        }
    }

    /**
     * What an engraving actually covers on the sheet: path coordinates, or for an SVG logo
     * (a {@code <g transform='matrix(...)'>}), its viewBox after the transform.
     */
    private static Rectangle2D drawnBounds(String svg) {
        Matcher m = Pattern.compile("matrix\\(([-\\d.]+) 0 0 ([-\\d.]+) ([-\\d.]+) ([-\\d.]+)\\)").matcher(svg);
        if (m.find()) {
            double s = Double.parseDouble(m.group(1));
            double tx = Double.parseDouble(m.group(3));
            double ty = Double.parseDouble(m.group(4));
            // the sample logo's viewBox is 10 10 50 25
            return new Rectangle2D.Double(10 * s + tx, 10 * s + ty, 50 * s, 25 * s);
        }
        return SvgAssert.engravePathBounds(svg);
    }

    /** A white image with a black disc in the middle. */
    static BufferedImage disc(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        int d = Math.min(w, h) / 2;
        g.fillOval(w / 2 - d / 2, h / 2 - d / 2, d, d);
        g.dispose();
        return img;
    }
}
