/*

SimpleBoxTest.java
tests the simple box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import static org.junit.Assert.*;

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

    /**
     * A monogram should be drawn twice (top panel + faded copy on the back),
     * trimmed of surrounding whitespace. A blank monogram should add nothing.
     */
    @Test
    public void generateBoxSvgHandlesMonogram() {
        String withName = SimpleBox.generateBoxSVG(100, 60, 80, 5, "  JDE  ");
        String blank    = SimpleBox.generateBoxSVG(100, 60, 80, 5, "   ");

        assertTrue(withName.startsWith("<?xml"));
        assertTrue(withName.endsWith("</svg>"));

        int count = withName.split(">JDE</text>", -1).length - 1;
        assertEquals("Monogram should appear on top and back panels", 2, count);
        assertFalse(withName.contains(">  JDE  </text>"));

        assertFalse(blank.contains("class=\"monogram\""));
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
     * The top monogram is sized to fit the panel: at most 60% of the depth,
     * at least 8 px. The back copy is 70% of that, capped at 30% of the
     * height, and never under 5 px. On a 100 x 60 x 80 box a single letter
     * gets 48 px on top and 18 px on the back; a 30-letter name would need
     * 5.556 px, so it is raised to 8 px on top and 5.6 px on the back.
     */
    @Test
    public void monogramFontSizeFitsPanelAndHasMinimum() {
        String oneLetter = SimpleBox.generateBoxSVG(100, 60, 80, 5, "A");
        String longName  = SimpleBox.generateBoxSVG(100, 60, 80, 5,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD");

        assertTrue(oneLetter.contains("font-size=\"48.000\""));
        assertTrue(oneLetter.contains("font-size=\"18.000\""));

        assertTrue(longName.contains("font-size=\"8.000\""));
        assertTrue(longName.contains("font-size=\"5.600\""));
    }
}