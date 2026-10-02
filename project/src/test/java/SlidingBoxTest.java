/*

SlidingBoxTest.java
tests the sliding box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import static org.junit.Assert.*;

import org.junit.Test;

public class SlidingBoxTest {

    /**
     * The drawer is sized from the outer box using 3 mm material:
     *   innerWidth  = outerWidth  - 2 * 3
     *   innerDepth  = outerDepth  - 3
     *   innerHeight = outerHeight - 3
     * An outer box of 100 x 80 x 60 should give an inner box of 94 x 77 x 57,
     * which generateSVG() prints in its dimension label.
     */
    @Test
    public void innerDrawerSizedFromOuterBox() {
        SlidingBox box = new SlidingBox(100, 80, 60, 5, "");
        String svg = box.generateSVG();

        assertTrue(svg.contains("Outer: 100 × 80 × 60 mm"));
        assertTrue(svg.contains("Inner: 94 × 77 × 57 mm"));
    }

    /**
     * A monogram is engraved twice, uppercased: red on the outer top panel
     * and blue on the inner bottom panel. An empty monogram adds neither.
     */
    @Test
    public void monogramOnOuterTopAndInnerBottom() {
        String withName = new SlidingBox(100, 80, 60, 5, "abc").generateSVG();
        String blank    = new SlidingBox(100, 80, 60, 5, "").generateSVG();

        assertTrue(withName.startsWith("<?xml"));
        assertTrue(withName.endsWith("</svg>"));

        int count = withName.split(">ABC</text>", -1).length - 1;
        assertEquals("Monogram should appear on outer top and inner bottom", 2, count);
        assertTrue(withName.contains("fill: rgb(255,0,0)"));
        assertTrue(withName.contains("fill: rgb(0,0,255)"));
        assertFalse(withName.contains(">abc</text>"));

        assertFalse(blank.contains("fill: rgb(255,0,0)"));
        assertFalse(blank.contains("fill: rgb(0,0,255)"));
    }

        /**
     * The drawer slides in from the right, so the outer front panel's right
     * edge must be one straight cut from top to bottom with no teeth, while
     * the left edge keeps its teeth (4 of them, cut 3 mm in, for a 60 mm
     * tall panel with 5 mm teeth). Front and back are the same shape.
     */
    @Test
    public void outerFrontPanelHasFlatRightEdge() {
        SlidingBox box = new SlidingBox(100, 80, 60, 5, "");
        String front = box.makeOuterFrontPanelPath(0, 0);

        assertTrue(front.contains("H 100.000 V 60.000 "));
        assertFalse(front.contains("H 97.000 "));

        int leftTeeth = front.split("H 3\\.000 ", -1).length - 1;
        assertEquals(4, leftTeeth);

        assertEquals(front, box.makeOuterBackPanelPath(0, 0));
    }

    /**
     * The inner right side is the inner left side plus a semicircular
     * handle cutout centred on the top edge. The drawer is 77 mm deep, so
     * the handle is half that wide (38.5 mm): it runs from x = 19.25 to
     * x = 57.75 with a 19.25 mm radius. The left side has no arc at all.
     */
    @Test
    public void innerRightSideAddsCentredHandleToLeftSide() {
        SlidingBox box = new SlidingBox(100, 80, 60, 5, "");
        String left  = box.makeInnerLeftSidePanelPath(0, 0);
        String right = box.makeInnerRightSidePanelPath(0, 0);

        assertFalse(left.contains(" A "));
        assertTrue(right.startsWith(left));
        assertEquals(
                "M 19.250 0.000 A 19.250 19.250 0 0 0 57.750 0.000 Z ",
                right.substring(left.length()));
    }

    /**
     * Monogram font size is scaled to fit 60% of the panel and clamped to
     * the range 8 to 50 px. A single letter is limited by panel depth:
     * 0.6 * 80 = 48 px on the outer top and 0.6 * 77 = 46.2 px on the
     * smaller inner bottom. A 30-letter name would need about 3.3 px, so
     * it is raised to the 8 px minimum on both panels.
     */
    @Test
    public void monogramFontSizeScalesWithPanelAndIsClamped() {
        String oneLetter = new SlidingBox(100, 80, 60, 5, "A").generateSVG();
        String longName  = new SlidingBox(100, 80, 60, 5,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD").generateSVG();

        assertTrue(oneLetter.contains("font-size=\"48.0px\""));
        assertTrue(oneLetter.contains("font-size=\"46.2px\""));

        int minSized = longName.split("font-size=\"8\\.0px\"", -1).length - 1;
        assertEquals("Both monograms should fall back to the minimum", 2, minSized);
    }
}