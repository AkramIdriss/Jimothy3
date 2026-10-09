/*

NoteHolderBoxTest.java
tests the note holder box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import static org.junit.Assert.*;

import org.junit.Test;

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
     * Engraving text should appear uppercased in the SVG when provided,
     * and no engraving <text> element should be emitted when it's blank.
     */
    @Test
    public void generateSvgHandlesEngraving() {
        String withText = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "Isaac")
                .generateSVG();
        String withoutText = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "")
                .generateSVG();

        assertTrue(withText.startsWith("<?xml"));
        assertTrue(withText.trim().endsWith("</svg>"));
        assertTrue(withText.contains(">ISAAC</text>"));
        assertFalse(withText.contains(">Isaac</text>"));

        assertFalse(withoutText.contains("fill: rgb(255,0,0)"));
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
     * The engraving is centred on the bottom panel and sized to fit 60% of
     * it, clamped to 8 to 50 px. A five-letter name is limited by width:
     * 0.6 * 86 / 3 = 17.2 px on the sticky holder and 0.6 * 138 / 3 =
     * 27.6 px on the wider index card holder. A single letter hits the
     * 50 px maximum and a 30-letter name the 8 px minimum.
     */
    @Test
    public void engravingIsCentredAndSizedToBottomPanel() {
        String sticky = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "Isaac")
                .generateSVG();
        String index = new NoteHolderBox(NoteHolderBox.INDEXCARD_HOLDER, "Isaac")
                .generateSVG();
        String oneLetter = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER, "A")
                .generateSVG();
        String longName = new NoteHolderBox(NoteHolderBox.STICKY_NOTE_HOLDER,
                "ABCDEFGHIJKLMNOPQRSTUVWXYZABCD").generateSVG();

        assertTrue(sticky.contains("font-size=\"17.2px\""));
        assertTrue(sticky.contains("matrix(1 0 0 1 93.000 273.000)"));

        assertTrue(index.contains("font-size=\"27.6px\""));
        assertTrue(index.contains("matrix(1 0 0 1 119.000 273.000)"));

        assertTrue(oneLetter.contains("font-size=\"50.0px\""));
        assertTrue(longName.contains("font-size=\"8.0px\""));
    }
}