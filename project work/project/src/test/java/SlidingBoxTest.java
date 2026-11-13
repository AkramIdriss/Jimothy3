/*

SlidingBoxTest.java
tests the sliding box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
package jimothypackage;

import static org.junit.Assert.*;

import org.junit.Test;

public class SlidingBoxTest {

    // Standard test box: 100 wide x 80 deep x 60 tall, 5 mm teeth, 3 mm material
    private static final double W = 100, D = 80, H = 60, TOOTH = 5, T = 3.0;

    // Inner drawer dimensions, derived the same way generateBoxSVG does
    private static final double IW = W - 2 * T;  // 94
    private static final double ID = D - T;      // 77
    private static final double IH = H - T;      // 57

    /** Shorthand for generateBoxSVG, taking width, depth, height in that order. */
    private static String svg(double w, double d, double h, String engraving) {
        // note: generateBoxSVG's parameter order is (width, height, depth, ...)
        return SlidingBox.generateBoxSVG(w, h, d, TOOTH, engraving);
    }

    /**
     * The drawer is sized from the outer box using 3 mm material:
     *   innerWidth  = outerWidth  - 2 * 3
     *   innerDepth  = outerDepth  - 3
     *   innerHeight = outerHeight - 3
     * An outer box of 100 x 80 x 60 should give an inner box of 94 x 77 x 57,
     * which generateBoxSVG() prints in its dimension label.
     */
    @Test
    public void innerDrawerSizedFromOuterBox() {
        String out = svg(W, D, H, "");

        assertTrue(out.contains("Outer: 100 × 80 × 60 mm"));
        assertTrue(out.contains("Inner: 94 × 77 × 57 mm"));
    }

    /**
     * A monogram is engraved twice, uppercased: red on the outer top panel
     * and blue on the inner bottom panel. An empty monogram adds neither.
     */
    @Test
    public void monogramOnOuterTopAndInnerBottom() {
        String withName = svg(W, D, H, "abc");
        String blank    = svg(W, D, H, "");

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
        String front = SlidingBox.makeOuterFrontPanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);
        String back  = SlidingBox.makeOuterBackPanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);

        assertTrue(front.contains("H 100.000 V 60.000 "));
        assertFalse(front.contains("H 97.000 "));

        int leftTeeth = front.split("H 3\\.000 ", -1).length - 1;
        assertEquals(4, leftTeeth);

        assertEquals(front, back);
    }

    /**
     * The inner right side is the inner left side plus a semicircular
     * handle cutout centred on the top edge. The drawer is 77 mm deep, so
     * the handle is half that wide (38.5 mm): it runs from x = 19.25 to
     * x = 57.75 with a 19.25 mm radius. The left side has no arc at all.
     */
    @Test
    public void innerRightSideAddsCentredHandleToLeftSide() {
        String left  = SlidingBox.makeInnerLeftSidePanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);
        String right = SlidingBox.makeInnerRightSidePanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);

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
        String oneLetter = svg(W, D, H, "A");
        String longName  = svg(W, D, H, "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD");

        assertTrue(oneLetter.contains("font-size=\"48.0px\""));
        assertTrue(oneLetter.contains("font-size=\"46.2px\""));

        int minSized = longName.split("font-size=\"8\\.0px\"", -1).length - 1;
        assertEquals("Both monograms should fall back to the minimum", 2, minSized);
    }

    /**
     * generateBoxSVG refuses any non-positive dimension or tooth width
     * and returns an empty string instead of a broken file.
     */
    @Test
    public void generateBoxSvgRejectsNonPositiveInputs() {
        assertEquals("", SlidingBox.generateBoxSVG(0, H, D, TOOTH, ""));
        assertEquals("", SlidingBox.generateBoxSVG(W, -1, D, TOOTH, ""));
        assertEquals("", SlidingBox.generateBoxSVG(W, H, 0, TOOTH, ""));
        assertEquals("", SlidingBox.generateBoxSVG(W, H, D, 0, ""));
    }

    /**
     * The sheet has ten cut panels: five for the outer sleeve (no right side,
     * since that is where the drawer comes out) and five for the drawer.
     */
    @Test
    public void sheetHasFiveOuterAndFiveInnerPanels() {
        String out = svg(W, D, H, "");

        assertEquals(10, out.split("class='cut'", -1).length - 1);
        assertTrue(out.contains(">OUTER BOX<"));
        assertTrue(out.contains(">INNER BOX (Sliding Drawer)<"));
        assertTrue(out.contains(">Side Panel (Outer Left)<"));
        assertFalse(out.contains("Outer Right"));
    }

    /**
     * The outer top and bottom are the same shape, and like the front and
     * back their right edge is one straight cut (x = 100 from y = 0 to 80)
     * so the drawer can slide out. The other edges keep outward teeth.
     */
    @Test
    public void outerTopAndBottomMatchWithFlatRightEdge() {
        String top    = SlidingBox.makeOuterTopPanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);
        String bottom = SlidingBox.makeOuterBottomPanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);

        assertEquals(top, bottom);
        assertTrue(top.contains("H 100.000 V 80.000 "));
        assertFalse(top.contains("H 103.000 "));
        assertTrue(top.contains("V -3.000 "));
        assertTrue(top.contains("H -3.000 "));
    }

    /**
     * The drawer bottom (94 x 77) has notches cut inward on every edge, so
     * nothing sticks out past its outline. 7 notches fit across the 94 mm
     * front edge and 6 down the 77 mm left edge with 5 mm teeth.
     */
    @Test
    public void innerBottomPanelNotchesStayInsideOutline() {
        String bottom = SlidingBox.makeInnerBottomPanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);

        assertFalse("No coordinate should go below zero", bottom.contains("-"));
        assertFalse(bottom.contains("H 97.000 "));
        assertFalse(bottom.contains("V 80.000 "));

        assertEquals(7, bottom.split("V 3\\.000 ", -1).length - 1);
        assertEquals(6, bottom.split("H 3\\.000 ", -1).length - 1);
    }

    /**
     * The drawer front is 94 mm wide with an open (flat) top, so nothing
     * rises above y = 0, and its bottom teeth reach 3 mm below its 57 mm
     * height (to y = 60) to lock into the drawer bottom.
     */
    @Test
    public void innerFrontPanelHasFlatTopAndBottomTeeth() {
        String front = SlidingBox.makeInnerFrontPanelPath(0, 0, W, H, D, TOOTH, "",
                IW, W, ID, D, IH, H, T);

        assertFalse("Top edge should be flat", front.contains("V -"));
        assertTrue(front.contains("H 94.000 "));
        assertFalse(front.contains("H 97.000 "));
        assertTrue(front.contains("V 60.000 "));
    }
}