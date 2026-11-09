/*

NoteHolderBoxTest.java
tests the note holder box type

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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class NoteHolderBoxTest {

    /**
     * The back panel's top edge runs the full noteWidth, so the style chosen
     * in the constructor should show up directly in the generated path.
     * Sticky = 86 mm wide, index card = 138 mm wide. An unknown style falls
     * back to the sticky-note size.
     */
    @Test
    public void backPanelWidthMatchesChosenStyle() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .makeBackPanelNHPath(0, 0);
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "")
                .makeBackPanelNHPath(0, 0);
        String fallback = new NoteHolderBox(99, "")
                .makeBackPanelNHPath(0, 0);

        assertTrue(sticky.startsWith("M 0.000 0.000 H 86.000 "));
        assertTrue(index.startsWith("M 0.000 0.000 H 138.000 "));
        assertEquals("Unknown style should default to sticky note holder",
                sticky, fallback);
    }

    /**
     * Engraving text should be engraved (uppercased) once, on the bottom
     * panel, and nothing should be engraved when it's blank.
     */
    @Test
    public void generateSvgHandlesEngraving() {
        String withText = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "Isaac")
                .generateSVG();
        String withoutText = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .generateSVG();

        assertTrue(withText.startsWith("<?xml"));
        assertTrue(withText.trim().endsWith("</svg>"));
        assertEquals(1, SvgAssert.countEngravings(withText));
        assertEquals("Lowercase is uppercased",
                new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "ISAAC").generateSVG(), withText);

        assertEquals(0, SvgAssert.countEngravings(withoutText));
    }

        /**
     * The front is cut as two half panels with a gap between them, so each
     * half's top edge is noteWidth / 2 - 13 mm long: 30 mm for the sticky
     * note holder and 56 mm for the index card holder. The inner edge then
     * drops straight to 10 mm above the bottom. Each half has one slot.
     */
    @Test
    public void frontHalfPanelWidthLeavesCentreGap() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .makeFrontPanelNHPath(0, 0);
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "")
                .makeFrontPanelNHPath(0, 0);

        assertTrue(sticky.startsWith("M 0.000 0.000 H 30.000 V 50.000 "));
        assertTrue(index.startsWith("M 0.000 0.000 H 56.000 V 50.000 "));

        assertEquals(1, sticky.split("Z", -1).length - 1);
        assertEquals(1, index.split("Z", -1).length - 1);
    }

    /**
     * Both styles are 86 mm deep, so the side panel must come out identical
     * whichever style is chosen. It has six slots for the bottom panel and
     * a foot cutout across the middle half of the bottom edge
     * (from x = 64.5 back to x = 21.5, 10 mm high).
     */
    @Test
    public void sidePanelIsSameForBothStyles() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .makeSidePanelNHPath(0, 0);
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "")
                .makeSidePanelNHPath(0, 0);

        assertEquals(sticky, index);
        assertEquals(6, sticky.split("Z", -1).length - 1);
        assertTrue(sticky.contains("H 64.500 V 50.000 H 21.500 V 60.000 "));
    }

    /**
     * The sheet lays out back + two sides in one row, so its width is
     * noteWidth + 332 mm (padding, spacing and two 86 mm sides): 418 mm for
     * sticky and 470 mm for index card. Height does not depend on width,
     * so it is 366 mm for both. Every sheet has six cut panels.
     */
    @Test
    public void svgCanvasGrowsWithNoteWidthOnly() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .generateSVG();
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "")
                .generateSVG();

        assertTrue(sticky.contains(
                "<svg width='418mm' height='366mm' viewBox='0 0 418 366' "));
        assertTrue(index.contains(
                "<svg width='470mm' height='366mm' viewBox='0 0 470 366' "));

        assertEquals(6, sticky.split("class='cut'", -1).length - 1);
        assertEquals(6, index.split("class='cut'", -1).length - 1);
    }

    /**
     * The bottom panel is one closed outline inset 3 mm from each edge,
     * with teeth that reach back out to the full size: six along the top
     * (y = 0), six on each side (x = 0 and x = noteWidth) and two on the
     * front edge (y = 86). Only the width changes between styles, so the
     * right-hand teeth sit at x = 86 for sticky and x = 138 for index card.
     */
    @Test
    public void bottomPanelTeethReachFullWidthForEachStyle() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .makeBottomPanelNHPath(0, 0);
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "")
                .makeBottomPanelNHPath(0, 0);

        assertTrue(sticky.startsWith("M 3.000 3.000 H 8.783 "));
        assertTrue(index.startsWith("M 3.000 3.000 H 8.783 "));
        assertEquals(1, sticky.split("Z", -1).length - 1);
        assertTrue(sticky.endsWith("Z"));

        assertEquals(6, sticky.split("V 0\\.000 ", -1).length - 1);
        assertEquals(6, sticky.split("H 0\\.000 ", -1).length - 1);
        assertEquals(6, sticky.split("H 86\\.000 ", -1).length - 1);
        assertEquals(2, sticky.split("V 86\\.000 ", -1).length - 1);

        assertEquals(6, index.split("H 138\\.000 ", -1).length - 1);
        assertFalse(index.contains("H 86.000 "));
    }

    /**
     * The back panel has six slots for the bottom panel's teeth, spaced a
     * seventh of the width apart, and a foot cutout across the middle half
     * of its bottom edge. On the sticky holder the first slot starts at
     * 86 / 7 - 2.9 = 9.386 and the cutout runs from x = 64.5 back to 21.5;
     * on the index card holder those are 16.814, 103.5 and 34.5.
     */
    @Test
    public void backPanelSlotsAndFootCutoutScaleWithWidth() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .makeBackPanelNHPath(0, 0);
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "")
                .makeBackPanelNHPath(0, 0);

        assertEquals(6, sticky.split("Z", -1).length - 1);
        assertEquals(6, index.split("Z", -1).length - 1);

        assertTrue(sticky.contains(
                "M 9.386 47.500 V 44.500 H 15.186 V 47.500 Z "));
        assertTrue(index.contains(
                "M 16.814 47.500 V 44.500 H 22.614 V 47.500 Z "));

        assertTrue(sticky.contains("H 64.500 V 50.000 H 21.500 V 60.000 "));
        assertTrue(index.contains("H 103.500 V 50.000 H 34.500 V 60.000 "));
    }

    /**
     * The engraving is centred on the bottom panel and fits inside it,
     * clear of the edges. The bottom panel is at (50, 230): 86 x 86 on the
     * sticky holder (centre 93, 273) and 138 x 86 on the index card holder
     * (centre 119, 273). The wider panel gets a wider engraving.
     */
    @Test
    public void engravingIsCentredAndSizedToBottomPanel() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "Isaac")
                .generateSVG();
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "Isaac")
                .generateSVG();

        SvgAssert.assertPathsInside(sticky, new Bounds(50, 230, 86, 86).inset(EngraveStyle.PANEL_MARGIN_MM));
        SvgAssert.assertPathsInside(index, new Bounds(50, 230, 138, 86).inset(EngraveStyle.PANEL_MARGIN_MM));

        Rectangle2D s = SvgAssert.engravePathBounds(sticky);
        Rectangle2D i = SvgAssert.engravePathBounds(index);
        assertEquals(93, s.getCenterX(), 0.01);
        assertEquals(273, s.getCenterY(), 0.01);
        assertEquals(119, i.getCenterX(), 0.01);
        assertEquals(273, i.getCenterY(), 0.01);
        assertTrue(i.getWidth() > s.getWidth());
    }

    /**
     * Engravings on the back panel stay above its row of slots, which sits
     * 12.5 to 15.5 mm up from the bottom edge. The back panel is at (50, 140)
     * and 86 x 60, so the slots start at y = 184.5.
     */
    @Test
    public void backPanelEngravingStaysAboveTheSlots() {
        EngravingPlan plan = EngravingPlan.none()
                .add(NoteHolderBox.BACK, new TextEngraving(List.of("NOTES"), Fonts.defaultFont()));
        String svg = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, plan).generateSVG();

        assertEquals(1, SvgAssert.countEngravings(svg));
        assertTrue(SvgAssert.engravePathBounds(svg).getMaxY() < 184.5);
    }

    /**
     * The bottom panel's teeth have to fit into the back panel's slots.
     * Slots are 5.8 mm wide and teeth 5.7 mm, both centred on the same x
     * (12.286 for the first pair, 73.714 for the last), which leaves
     * 0.05 mm of clearance on each side.
     */
    @Test
    public void bottomTeethFitCentredInBackSlots() {
        NoteHolderBox box = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "");
        String back   = box.makeBackPanelNHPath(0, 0);
        String bottom = box.makeBottomPanelNHPath(0, 0);

        assertTrue(back.contains("M 9.386 47.500 V 44.500 H 15.186 "));
        assertTrue(bottom.contains("H 9.436 V 0.000 H 15.136 V 3.000 "));

        assertTrue(back.contains("M 70.814 47.500 V 44.500 H 76.614 "));
        assertTrue(bottom.contains("H 70.864 V 0.000 H 76.564 V 3.000 "));
    }

    /**
     * The panel paths are hand-written, so moving the origin by (10, 20)
     * must shift every coordinate by exactly that much: H values by 10,
     * V values by 20, and M points by (10, 20). The commands themselves
     * must not change. This is checked for all four panels.
     */
    @Test
    public void everyPanelShiftsExactlyWithOrigin() {
        NoteHolderBox box = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "");
        String[][] pairs = {
            {box.makeFrontPanelNHPath(0, 0),  box.makeFrontPanelNHPath(10, 20)},
            {box.makeBackPanelNHPath(0, 0),   box.makeBackPanelNHPath(10, 20)},
            {box.makeSidePanelNHPath(0, 0),   box.makeSidePanelNHPath(10, 20)},
            {box.makeBottomPanelNHPath(0, 0), box.makeBottomPanelNHPath(10, 20)}};

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
                // H shifts by x, V by y, M takes an x then a y
                double offset = command == 'H' ? 10
                              : command == 'V' ? 20
                              : (moveArg++ == 0 ? 10 : 20);
                assertEquals(Double.parseDouble(original[i]) + offset,
                        Double.parseDouble(shifted[i]), 0.0011);
            }
        }
    }

    /**
     * A null engraving must not crash generateSVG() and should produce the
     * same sheet as an empty one, with all six panel labels and no red
     * engraving text.
     */
    @Test
    public void nullEngravingIsSkipped() {
        String svg   = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, (String) null).generateSVG();
        String blank = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "").generateSVG();

        assertTrue(svg.trim().endsWith("</svg>"));
        assertEquals(0, SvgAssert.countEngravings(svg));
        assertEquals(blank, svg);
        assertEquals(blank, new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, (EngravingPlan) null).generateSVG());

        String[] labels = {"Front Left", "Front Right", "Back Panel",
                           "Side Panel", "Side Panel 2", "Bottom Panel"};
        for (String label : labels) {
            assertTrue("Missing label: " + label,
                    svg.contains(">" + label + "</text>"));
        }
    }

    /**
     * Both front halves are the same shape. The left half sits at (50, 50)
     * and the right half is placed half a note width plus 30 mm spacing to
     * its right: x = 123 on the sticky holder and x = 149 on the index card
     * holder.
     */
    @Test
    public void frontHalvesAreSameShapeSideBySide() {
        NoteHolderBox sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "");
        NoteHolderBox index  = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "");
        String stickySvg = sticky.generateSVG();
        String indexSvg  = index.generateSVG();

        assertTrue(stickySvg.contains(sticky.makeFrontPanelNHPath(50, 50)));
        assertTrue(stickySvg.contains(sticky.makeFrontPanelNHPath(123, 50)));

        assertTrue(indexSvg.contains(index.makeFrontPanelNHPath(50, 50)));
        assertTrue(indexSvg.contains(index.makeFrontPanelNHPath(149, 50)));
    }

    /**
     * Each front half has four teeth cut 3 mm in on its outer (left) edge
     * and a single 5.8 x 3 mm slot near the bottom for the bottom panel,
     * starting at x = 8.783 and running up from y = 47.5 to 44.5.
     */
    @Test
    public void frontHalfHasFourTeethAndOneBottomSlot() {
        String front = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .makeFrontPanelNHPath(0, 0);

        assertEquals(4, front.split("H 3\\.000 ", -1).length - 1);
        assertTrue(front.endsWith(
                "M 8.783 47.500 V 44.500 H 14.583 V 47.500 Z "));
        assertFalse("Nothing should poke outside the left edge",
                front.contains("H -"));
    }

    /**
     * saveToFile() should write exactly what generateSVG() returns, so the
     * file sent to the laser cutter matches the preview.
     */
    @Test
    public void saveToFileWritesGeneratedSvg() throws IOException {
        NoteHolderBox box = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "Isaac");
        File file = File.createTempFile("noteholder", ".svg");
        file.deleteOnExit();

        box.saveToFile(file.getPath());
        String saved = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        assertEquals(box.generateSVG(), saved);
    }
}