/*

SimpleBoxTest.java
tests the simple box type

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

public class SimpleBoxTest {

    /**
     * Non-positive inputs are clamped inside the panel methods:
     * toothWidth <= 0 becomes 2.0, and width/height <= 0 become 1.0.
     * So a bad value should produce exactly the same path as the clamped value.
     */
    @Test
    public void frontPanelClampsInvalidInputs() {
        String badTooth  = SimpleBox.makeFrontPanelPath(0, 0, 100, 60, 0);
        String goodTooth = SimpleBox.makeFrontPanelPath(0, 0, 100, 60, 2.0);
        assertEquals(goodTooth, badTooth);

        String badWidth  = SimpleBox.makeFrontPanelPath(0, 0, -5, 60, 5);
        String goodWidth = SimpleBox.makeFrontPanelPath(0, 0, 1.0, 60, 5);
        assertEquals(goodWidth, badWidth);

        assertTrue(badTooth.startsWith("M 0.000 0.000 "));
        assertTrue(badTooth.endsWith("Z"));
    }

    /** Top and back panels of a 100 x 60 x 80 box (see the layout tests below). */
    private static final Bounds TOP_PANEL  = new Bounds(200, 160, 100, 80);
    private static final Bounds BACK_PANEL = new Bounds(50, 302, 100, 60);

    /** The engraving elements of an SVG sheet, in the order they were drawn. */
    private static List<String> engravings(String svg) {
        return Arrays.stream(svg.split("\n")).filter(l -> l.contains("class='engrave'")).toList();
    }

    /**
     * A monogram should be engraved twice (top panel + back panel), trimmed of
     * surrounding whitespace and uppercased. A blank monogram should add nothing.
     */
    @Test
    public void generateBoxSvgHandlesMonogram() {
        String withName = SimpleBox.generateBoxSVG(100, 60, 80, 5, "  JDE  ");
        String blank    = SimpleBox.generateBoxSVG(100, 60, 80, 5, "   ");

        assertTrue(withName.startsWith("<?xml"));
        assertTrue(withName.endsWith("</svg>"));

        assertEquals("Monogram should appear on top and back panels", 2, engravings(withName).size());
        assertEquals("Surrounding spaces are trimmed",
                SimpleBox.generateBoxSVG(100, 60, 80, 5, "JDE"), withName);

        assertEquals(0, SvgAssert.countEngravings(blank));

        // Lowercase input is uppercased, like the other boxes
        assertEquals(SimpleBox.generateBoxSVG(100, 60, 80, 5, "JDE"),
                     SimpleBox.generateBoxSVG(100, 60, 80, 5, "jde"));
    }

        /**
     * Front and side panels mate at the corners, so their vertical edges
     * must be opposites: the front panel's teeth cut 3 mm INTO the panel
     * (x = 97 and x = 3 on a 100 mm panel), while the side panel's teeth
     * stick 3 mm OUT past it (x = 103 and x = -3).
     */
    @Test
    public void frontAndSidePanelsHaveOppositeVerticalTeeth() {
        String front = SimpleBox.makeFrontPanelPath(0, 0, 100, 60, 5);
        String side  = SimpleBox.makeSidePanelPath(0, 0, 100, 60, 5);

        assertTrue(front.contains("H 97.000 "));
        assertTrue(front.contains("H 3.000 "));
        assertFalse(front.contains("H 103.000 "));
        assertFalse(front.contains("H -3.000 "));

        assertTrue(side.contains("H 103.000 "));
        assertTrue(side.contains("H -3.000 "));
        assertFalse(side.contains("H 97.000 "));
        assertFalse(side.contains("H 3.000 "));
    }

    /**
     * The bottom panel is a plain rectangle with a 6 mm brim on every side,
     * plus one closed slot per tooth. For 100 x 80 with 5 mm teeth:
     *   width  edges: (100 - 20) / 10 = 8 slots each
     *   depth  edges: ( 80 - 20) / 10 = 6 slots each
     * so 8 + 6 + 8 + 6 = 28 slots, each closed with its own Z.
     */
    @Test
    public void bottomPanelHasBrimAndOneSlotPerTooth() {
        String bottom = SimpleBox.makeBottomPanelPath(10, 10, 100, 80, 5);

        assertTrue(bottom.startsWith(
                "M 4.000 4.000 H 116.000 V 96.000 H 4.000 V 4.000 "));

        int slots = bottom.split("Z", -1).length - 1;
        assertEquals("One slot per tooth on all four edges", 28, slots);
    }

    /**
     * The top panel's teeth all point outward by 3 mm, and opposite edges
     * must match. For 100 x 80 with 5 mm teeth that is 8 teeth on the
     * top and bottom edges (y = -3 and y = 83) and 6 on the left and
     * right edges (x = -3 and x = 103).
     */
    @Test
    public void topPanelTeethProjectOutwardOnAllFourEdges() {
        String top = SimpleBox.makeTopPanelPath(0, 0, 100, 80, 5);

        assertEquals(8, top.split("V -3\\.000 ", -1).length - 1);
        assertEquals(8, top.split("V 83\\.000 ", -1).length - 1);
        assertEquals(6, top.split("H 103\\.000 ", -1).length - 1);
        assertEquals(6, top.split("H -3\\.000 ", -1).length - 1);
        assertTrue(top.endsWith("Z"));
    }

    /**
     * Panels are drawn at whatever (x, y) the layout gives them, so moving
     * the origin must shift every coordinate without changing the shape.
     * A 100 x 60 front panel at (10, 20) has a 10 mm corner, then its first
     * top notch 3 mm deep and 80 / 15 = 5.333 mm wide. Its side teeth cut in
     * to x = 107 and x = 13, and its bottom notches reach y = 83.
     */
    @Test
    public void frontPanelShiftsWithOrigin() {
        String atOrigin = SimpleBox.makeFrontPanelPath(0, 0, 100, 60, 5);
        String shifted  = SimpleBox.makeFrontPanelPath(10, 20, 100, 60, 5);

        assertTrue(shifted.startsWith(
                "M 10.000 20.000 H 20.000 V 23.000 H 25.333 V 20.000 "));
        assertTrue(shifted.contains("H 107.000 "));
        assertTrue(shifted.contains("H 13.000 "));
        assertTrue(shifted.contains("V 83.000 "));
        assertTrue(shifted.endsWith("Z"));

        assertEquals("Moving the panel must not add or remove path commands",
                atOrigin.split(" ").length, shifted.split(" ").length);
    }

    /**
     * The sheet holds six cut panels, each with its own label. For a
     * 100 x 60 x 80 box the widest row is front + two sides
     * (50 + 100 + 50 + 80 + 50 + 80 + 50 = 460) and the tallest column is
     * front + bottom with its brim + back
     * (50 + 60 + 50 + 92 + 50 + 60 + 50 = 412).
     */
    @Test
    public void svgSheetHasSixLabelledPanelsAndFitsThem() {
        String svg = SimpleBox.generateBoxSVG(100, 60, 80, 5, "");

        assertTrue(svg.contains("<svg viewBox='0 0 460 412' "));
        assertEquals(6, svg.split("class='cut'", -1).length - 1);

        String[] labels = {"Front", "Side (Right)", "Side (Left)",
                           "Bottom", "Top", "Back"};
        for (String label : labels) {
            assertTrue("Missing label: " + label,
                    svg.contains(">" + label + "</text>"));
        }
    }

    /**
     * The monogram is scaled to fit its panel, keeping clear of the finger
     * joints (EngraveStyle.PANEL_MARGIN_MM on every side). A single letter is
     * limited by height and a 30-letter name by width, but both stay inside
     * the top and back panels.
     */
    @Test
    public void monogramFitsInsideTopAndBackPanels() {
        for (String name : new String[] {"A", "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD"}) {
            List<String> parts = engravings(SimpleBox.generateBoxSVG(100, 60, 80, 5, name));
            SvgAssert.assertPathsInside(parts.get(0), TOP_PANEL.inset(EngraveStyle.PANEL_MARGIN_MM));
            SvgAssert.assertPathsInside(parts.get(1), BACK_PANEL.inset(EngraveStyle.PANEL_MARGIN_MM));
        }
    }

    /**
     * The monogram is centred on its panels. For a 100 x 60 x 80 box the
     * top panel sits at (200, 160) and is 100 x 80, so its centre is
     * (250, 200). The back panel sits at (50, 302) and is 100 x 60, so
     * its centre is (100, 332).
     */
    @Test
    public void monogramIsCentredOnTopAndBackPanels() {
        List<String> parts = engravings(SimpleBox.generateBoxSVG(100, 60, 80, 5, "JDE"));
        Rectangle2D top  = SvgAssert.engravePathBounds(parts.get(0));
        Rectangle2D back = SvgAssert.engravePathBounds(parts.get(1));

        assertEquals(250, top.getCenterX(), 0.01);
        assertEquals(200, top.getCenterY(), 0.01);
        assertEquals(100, back.getCenterX(), 0.01);
        assertEquals(332, back.getCenterY(), 0.01);
    }

    /**
     * With an EngravingPlan, any listed panel can be engraved. Text on the
     * front panel (at (50, 50), 100 x 60) lands there and nowhere else.
     */
    @Test
    public void engravingPlanCanTargetAnyPanel() {
        EngravingPlan plan = EngravingPlan.none()
                .add(SimpleBox.FRONT, new TextEngraving(List.of("Front", "text"), Fonts.defaultFont()));
        String svg = SimpleBox.generateBoxSVG(100, 60, 80, 5, plan);

        assertEquals(1, SvgAssert.countEngravings(svg));
        SvgAssert.assertPathsInside(svg, new Bounds(50, 50, 100, 60).inset(EngraveStyle.PANEL_MARGIN_MM));
        assertTrue(SimpleBox.ENGRAVABLE_PANELS.contains(SimpleBox.FRONT));
        assertFalse("The slotted bottom panel can't be engraved",
                SimpleBox.ENGRAVABLE_PANELS.contains("bottom"));
    }

    /**
     * Wider teeth mean fewer of them. With 10 mm teeth the corners are
     * 20 mm, leaving 60 mm on the top edge: 3 notches 12 mm wide. Each
     * side edge has 20 mm left, so just 1 tooth. With 5 mm teeth the top
     * edge gets 8 notches.
     *
     * BUG: if a panel is so small that the corners use up the whole side
     * edge (e.g. makeFrontPanelPath(0, 0, 20, 20, 5)), the side edges
     * draw a zero-height tooth ("H 17.000 V 10.000 H 20.000"). The top
     * and bottom edges are clamped to at least one toothWidth, but the
     * side edges are not.
     */
    @Test
    public void toothCountScalesWithToothWidth() {
        String fine   = SimpleBox.makeFrontPanelPath(0, 0, 100, 60, 5);
        String coarse = SimpleBox.makeFrontPanelPath(0, 0, 100, 60, 10);

        assertEquals(8, fine.split("V 3\\.000 ", -1).length - 1);

        assertEquals(3, coarse.split("V 3\\.000 ", -1).length - 1);
        assertEquals(3, coarse.split("V 63\\.000 ", -1).length - 1);
        assertEquals(1, coarse.split("H 97\\.000 ", -1).length - 1);
        assertEquals(1, coarse.split("H 3\\.000 ", -1).length - 1);
        assertTrue(coarse.startsWith(
                "M 0.000 0.000 H 20.000 V 3.000 H 32.000 V 0.000 "));
    }

    /**
     * Regression check for the small-panel fix. On a 20 x 20 panel with
     * 5 mm teeth the corners use up the whole side edge, so the side edge is
     * now given one full toothWidth (5 mm) to work with. The tooth should run
     * from y = 10 down to y = 15 instead of collapsing to zero height at
     * y = 10. This applies to both the front panel (teeth in, x = 17) and
     * the side panel (teeth out, x = 23).
     */
    @Test
    public void smallPanelSideTeethNeverCollapse() {
        String front = SimpleBox.makeFrontPanelPath(0, 0, 20, 20, 5);
        String side  = SimpleBox.makeSidePanelPath(0, 0, 20, 20, 5);

        assertTrue(front.contains("H 17.000 V 15.000 H 20.000 "));
        assertFalse(front.contains("H 17.000 V 10.000 H 20.000 "));

        assertTrue(side.contains("H 23.000 V 15.000 H 20.000 "));
        assertFalse(side.contains("H 23.000 V 10.000 H 20.000 "));
    }

    /**
     * The bottom panel's slots sit 2 mm in from the box edge (inside the
     * 6 mm brim) and start after the 10 mm corner. For a 100 x 80 panel at
     * (10, 10) with 5 mm teeth, the first front slot is at (20, 12), 3 mm
     * deep and 80 / 15 = 5.333 mm wide, lining up with the front panel's
     * bottom notches.
     */
    @Test
    public void bottomPanelSlotsAreInsetFromEdge() {
        String bottom = SimpleBox.makeBottomPanelPath(10, 10, 100, 80, 5);

        assertTrue(bottom.contains(
                "M 20.000 12.000 V 15.000 H 25.333 V 12.000 Z "));
        assertTrue(bottom.contains(
                "M 30.667 12.000 V 15.000 H 36.000 V 12.000 Z "));
        assertFalse("No slot should start on the brim edge",
                bottom.contains("M 20.000 10.000 "));
    }

    /**
     * The back panel reuses the front panel shape. In a 100 x 60 x 80 sheet
     * the back panel sits at (50, 302), so the SVG must contain exactly the
     * front-panel path generated at that position.
     */
    @Test
    public void backPanelIsFrontPanelShapeAtBackPosition() {
        String svg  = SimpleBox.generateBoxSVG(100, 60, 80, 5, "");
        String back = SimpleBox.makeFrontPanelPath(50, 302, 100, 60, 5);

        assertTrue(svg.contains("<path d='" + back + "' class='cut' />"));
        assertTrue(svg.contains(">Back</text>"));
    }

    /**
     * A null monogram must be treated the same as a blank one: the sheet is
     * still generated with all six panels and no monogram text.
     */
    @Test
    public void nullMonogramIsSkipped() {
        String svg   = SimpleBox.generateBoxSVG(100, 60, 80, 5, (String) null);
        String blank = SimpleBox.generateBoxSVG(100, 60, 80, 5, "");

        assertTrue(svg.endsWith("</svg>"));
        assertEquals(0, SvgAssert.countEngravings(svg));
        assertEquals(6, svg.split("class='cut'", -1).length - 1);
        assertEquals(blank, svg);
        assertEquals(blank, SimpleBox.generateBoxSVG(100, 60, 80, 5, (EngravingPlan) null));
    }
}