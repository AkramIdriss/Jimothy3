/*

SlidingBoxTest.java
tests the sliding box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;

import engraving.Bounds;
import engraving.EngraveStyle;
import engraving.EngravingPlan;
import engraving.Fonts;
import engraving.SvgAssert;
import engraving.TextEngraving;
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

    /** Outer top and inner bottom panels of a 100 x 80 x 60 sliding box. */
    private static final Bounds OUTER_TOP    = new Bounds(200, 160, 100, 80);
    private static final Bounds INNER_BOTTOM = new Bounds(50, 664, 94, 77);

    /** The engraving elements of an SVG sheet, in the order they were drawn. */
    private static List<String> engravings(String svg) {
        return Arrays.stream(svg.split("\n")).filter(l -> l.contains("class='engrave'")).toList();
    }

    /**
     * A monogram is engraved twice, uppercased, in the shared engrave colour:
     * on the outer top panel and on the inner bottom panel. An empty
     * monogram adds neither.
     */
    @Test
    public void monogramOnOuterTopAndInnerBottom() {
        String withName = new SlidingBox(100, 80, 60, 5, "abc").generateSVG();
        String blank    = new SlidingBox(100, 80, 60, 5, "").generateSVG();

        assertTrue(withName.startsWith("<?xml"));
        assertTrue(withName.endsWith("</svg>"));

        assertEquals("Monogram should appear on outer top and inner bottom", 2,
                engravings(withName).size());
        assertTrue(withName.contains("fill='" + EngraveStyle.ENGRAVE_COLOR + "'"));
        assertEquals("Lowercase is uppercased",
                new SlidingBox(100, 80, 60, 5, "ABC").generateSVG(), withName);

        assertEquals(0, SvgAssert.countEngravings(blank));
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
     * The monogram is scaled to fit and centred on each panel, keeping clear
     * of the finger joints. The outer top is 100 x 80 at (200, 160), centre
     * (250, 200); the inner bottom is 94 x 77 at (50, 664), centre
     * (97, 702.5). Short and very long names both stay inside.
     */
    @Test
    public void monogramFitsAndIsCentredOnBothPanels() {
        for (String name : new String[] {"A", "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD"}) {
            List<String> parts = engravings(new SlidingBox(100, 80, 60, 5, name).generateSVG());
            SvgAssert.assertPathsInside(parts.get(0), OUTER_TOP.inset(EngraveStyle.PANEL_MARGIN_MM));
            SvgAssert.assertPathsInside(parts.get(1), INNER_BOTTOM.inset(EngraveStyle.PANEL_MARGIN_MM));

            Rectangle2D top = SvgAssert.engravePathBounds(parts.get(0));
            Rectangle2D bottom = SvgAssert.engravePathBounds(parts.get(1));
            assertEquals(250, top.getCenterX(), 0.01);
            assertEquals(200, top.getCenterY(), 0.01);
            assertEquals(97, bottom.getCenterX(), 0.01);
            assertEquals(702.5, bottom.getCenterY(), 0.01);
        }
    }

    /** With an EngravingPlan, any listed panel can be engraved, e.g. the drawer's front. */
    @Test
    public void engravingPlanCanTargetAnyPanel() {
        EngravingPlan plan = EngravingPlan.none()
                .add(SlidingBox.INNER_FRONT, new TextEngraving(List.of("Pull"), Fonts.defaultFont()));
        String svg = new SlidingBox(100, 80, 60, 5, plan).generateSVG();

        assertEquals(1, SvgAssert.countEngravings(svg));
        // inner front: (50, 450), 94 x 57
        SvgAssert.assertPathsInside(svg, new Bounds(50, 450, 94, 57).inset(EngraveStyle.PANEL_MARGIN_MM));
        assertFalse("The handle panel can't be engraved",
                SlidingBox.ENGRAVABLE_PANELS.contains("inner right side"));
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

    /**
     * A null monogram must not crash generateSVG() and should produce the
     * same sheet as an empty one, with no engraving on either panel.
     */
    @Test
    public void nullMonogramIsSkipped() {
        String svg   = new SlidingBox(100, 80, 60, 5, (String) null).generateSVG();
        String blank = new SlidingBox(100, 80, 60, 5, "").generateSVG();

        assertTrue(svg.endsWith("</svg>"));
        assertEquals(0, SvgAssert.countEngravings(svg));
        assertEquals(blank, svg);
        assertEquals(blank, new SlidingBox(100, 80, 60, 5, (EngravingPlan) null).generateSVG());
    }

    /**
     * The inner left side is 77 mm deep and 57 mm tall with a flat top
     * (the drawer is open). Its side teeth stick 3 mm out: 5 at x = 80 and
     * 5 at x = -3. Its bottom edge has 6 notches cut down to y = 60.
     */
    @Test
    public void innerLeftSideHasFlatTopAndOutwardSideTeeth() {
        String left = new SlidingBox(100, 80, 60, 5, "").makeInnerLeftSidePanelPath(0, 0);

        assertTrue(left.startsWith("M 0.000 0.000 H 77.000 H 80.000 "));
        assertFalse("Nothing should stick up above the top edge",
                left.contains("V -"));

        assertEquals(5, left.split("H 80\\.000 ", -1).length - 1);
        assertEquals(5, left.split("H -3\\.000 ", -1).length - 1);
        assertEquals(6, left.split("V 60\\.000 ", -1).length - 1);
        assertTrue(left.endsWith("Z"));
    }

    /**
     * The sheet width is set by the widest row. Making the outer box
     * 100 mm wider makes the drawer 100 mm wider too, so the drawer's
     * front + two sides row grows from 448 to 548 mm. The height only
     * depends on height and depth, so it stays at 791 mm.
     */
    @Test
    public void svgCanvasGrowsWithOuterWidth() {
        String narrow = new SlidingBox(100, 80, 60, 5, "").generateSVG();
        String wide   = new SlidingBox(200, 80, 60, 5, "").generateSVG();

        assertTrue(narrow.contains("viewBox='0 0 448 791' "));
        assertTrue(wide.contains(
                "<svg width='548mm' height='791mm' viewBox='0 0 548 791' "));
        assertTrue(wide.contains("Inner: 194 × 77 × 57 mm"));
    }

    /**
     * Every panel built only from M/H/V/Z commands must shift exactly with
     * its origin: moving it by (10, 20) adds 10 to every H value, 20 to
     * every V value and (10, 20) to every M point, with no change to the
     * commands themselves. Checked for the outer front, side and top and
     * the inner front, left side and bottom.
     */
    @Test
    public void panelsShiftExactlyWithOrigin() {
        SlidingBox box = new SlidingBox(100, 80, 60, 5, "");
        String[][] pairs = {
            {box.makeOuterFrontPanelPath(0, 0),    box.makeOuterFrontPanelPath(10, 20)},
            {box.makeOuterSidePanelPath(0, 0),     box.makeOuterSidePanelPath(10, 20)},
            {box.makeOuterTopPanelPath(0, 0),      box.makeOuterTopPanelPath(10, 20)},
            {box.makeInnerFrontPanelPath(0, 0),    box.makeInnerFrontPanelPath(10, 20)},
            {box.makeInnerLeftSidePanelPath(0, 0), box.makeInnerLeftSidePanelPath(10, 20)},
            {box.makeInnerBottomPanelPath(0, 0),   box.makeInnerBottomPanelPath(10, 20)}};

        for (String[] pair : pairs) {
            String[] original = pair[0].trim().split(" ");
            String[] shifted  = pair[1].trim().split(" ");
            assertEquals(original.length, shifted.length);

            char command = ' ';
            int moveArg = 0;
            for (int i = 0; i < original.length; i++) {
                if (original[i].matches("[MHVZ]")) {
                    assertEquals(original[i], shifted[i]);
                    command = original[i].charAt(0);
                    moveArg = 0;
                    continue;
                }
                double offset = command == 'H' ? 10
                              : command == 'V' ? 20
                              : (moveArg++ == 0 ? 10 : 20);
                assertEquals(Double.parseDouble(original[i]) + offset,
                        Double.parseDouble(shifted[i]), 0.0011);
            }
        }
    }
}