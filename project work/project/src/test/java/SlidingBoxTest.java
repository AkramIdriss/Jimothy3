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

    /**
     * The outer top panel also has to let the drawer through, so its right
     * edge is one straight cut down the full 80 mm depth with no teeth at
     * x = 103. The other three edges keep their outward teeth: 8 on the
     * top and bottom edges (y = -3 and y = 83) and 6 on the left (x = -3).
     * Top and bottom are the same shape.
     */
    @Test
    public void outerTopPanelHasFlatRightEdge() {
        SlidingBox box = new SlidingBox(100, 80, 60, 5, "");
        String top = box.makeOuterTopPanelPath(0, 0);

        assertTrue(top.contains("H 100.000 V 80.000 H 90.000 "));
        assertFalse(top.contains("H 103.000 "));

        assertEquals(8, top.split("V -3\\.000 ", -1).length - 1);
        assertEquals(8, top.split("V 83\\.000 ", -1).length - 1);
        assertEquals(6, top.split("H -3\\.000 ", -1).length - 1);

        assertEquals(top, box.makeOuterBottomPanelPath(0, 0));
    }

    /**
     * The drawer is open at the top, so the inner front panel has nothing
     * above its top edge. On a 94 x 57 mm panel with 5 mm teeth it has
     * 5 teeth cut 3 mm in on each side (x = 91 and x = 3) and 7 notches
     * along the bottom reaching y = 60. Front and back are the same shape.
     */
    @Test
    public void innerFrontPanelHasFlatTopAndToothedSidesAndBottom() {
        SlidingBox box = new SlidingBox(100, 80, 60, 5, "");
        String front = box.makeInnerFrontPanelPath(0, 0);

        assertTrue(front.startsWith("M 0.000 0.000 "));
        assertFalse("Nothing should stick up above the top edge",
                front.contains("V -"));

        assertEquals(5, front.split("H 91\\.000 ", -1).length - 1);
        assertEquals(5, front.split("H 3\\.000 ", -1).length - 1);
        assertEquals(7, front.split("V 60\\.000 ", -1).length - 1);
        assertTrue(front.endsWith("Z"));

        assertEquals(front, box.makeInnerBackPanelPath(0, 0));
    }

    /**
     * The sheet holds ten cut panels: five for the outer box and five for
     * the drawer, under their own section headings. For a 100 x 80 x 60 box
     * the widest row is the drawer's front + two sides
     * (50 + 94 + 50 + 77 + 50 + 77 + 50 = 448) and the height stacks three
     * outer rows and three inner rows to 791.
     */
    @Test
    public void svgSheetHasTenPanelsInTwoSections() {
        String svg = new SlidingBox(100, 80, 60, 5, "").generateSVG();

        assertTrue(svg.contains(
                "<svg width='448mm' height='791mm' viewBox='0 0 448 791' "));
        assertEquals(10, svg.split("class='cut'", -1).length - 1);

        assertTrue(svg.contains(">OUTER BOX</text>"));
        assertTrue(svg.contains(">INNER BOX (Sliding Drawer)</text>"));
        assertTrue(svg.contains(
                ">Side Panel (Inner Right) - WITH HANDLE</text>"));
    }

    /**
     * The outer side panel keeps teeth on all four edges. On an 80 x 60
     * panel the side teeth stick 3 mm out (4 at x = 83, 4 at x = -3),
     * while the top and bottom have 6 notches cut 3 mm in (y = 3 and
     * y = 57). Nothing sticks out above the top edge.
     */
    @Test
    public void outerSidePanelHasTeethOutOnSidesAndNotchesInTopAndBottom() {
        String side = new SlidingBox(100, 80, 60, 5, "").makeOuterSidePanelPath(0, 0);

        assertEquals(4, side.split("H 83\\.000 ", -1).length - 1);
        assertEquals(4, side.split("H -3\\.000 ", -1).length - 1);
        assertEquals(6, side.split("V 3\\.000 ", -1).length - 1);
        assertEquals(6, side.split("V 57\\.000 ", -1).length - 1);
        assertFalse(side.contains("V -"));
    }

    /**
     * Every edge of the inner bottom panel is notched inward, so nothing
     * should poke outside the 94 x 77 mm panel. There are 7 notches on the
     * front and back edges (y = 3 and y = 74) and 6 on each side
     * (x = 91 and x = 3).
     */
    @Test
    public void innerBottomPanelIsFemaleOnAllEdges() {
        String bottom = new SlidingBox(100, 80, 60, 5, "").makeInnerBottomPanelPath(0, 0);

        assertEquals(7, bottom.split("V 3\\.000 ", -1).length - 1);
        assertEquals(7, bottom.split("V 74\\.000 ", -1).length - 1);
        assertEquals(6, bottom.split("H 91\\.000 ", -1).length - 1);
        assertEquals(6, bottom.split("H 3\\.000 ", -1).length - 1);

        assertFalse("Nothing should poke outside the panel", bottom.contains("-"));
        assertTrue(bottom.endsWith("V 0.000 Z"));
    }
}