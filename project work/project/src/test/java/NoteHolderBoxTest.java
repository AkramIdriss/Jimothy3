/*

NoteHolderBoxTest.java
tests the note holder box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
package jimothypackage;

import static org.junit.Assert.*;

import java.awt.geom.Rectangle2D;

import engraving.Bounds;
import engraving.SvgAssert;
import org.junit.Test;

public class NoteHolderBoxTest {

    // style codes accepted by NoteHolderBox.generateBoxSVG (same as MainApp uses)
    private static final int STICKY = 1;
    private static final int INDEX  = 2;

    // panel sizes generateBoxSVG picks for each style
    private static final double STICKY_W = 86;
    private static final double INDEX_W  = 138;
    private static final double DEPTH    = 86;
    private static final double HEIGHT   = 60;
    private static final double TOOTH    = 5.8;

    /**
     * The back panel's top edge runs the full noteWidth.
     * Sticky = 86 mm wide, index card = 138 mm wide.
     */
    @Test
    public void backPanelWidthMatchesNoteWidth() {
        String sticky = NoteHolderBox.makeBackPanelNHPath(0, 0, STICKY_W, DEPTH, HEIGHT, TOOTH);
        String index  = NoteHolderBox.makeBackPanelNHPath(0, 0, INDEX_W,  DEPTH, HEIGHT, TOOTH);

        assertTrue(sticky.startsWith("M 0.000 0.000 H 86.000 "));
        assertTrue(index.startsWith("M 0.000 0.000 H 138.000 "));
    }

    /**
     * An unknown style code should fall back to the sticky-note holder.
     */
    @Test
    public void unknownStyleDefaultsToStickyNote() {
        assertEquals("Unknown style should default to sticky note holder",
                NoteHolderBox.generateBoxSVG(STICKY, ""),
                NoteHolderBox.generateBoxSVG(99, ""));
    }

    /**
     * Engraving text should appear uppercased in the SVG when provided,
     * and no engraving should be emitted when it's blank.
     */
    @Test
    public void generateSvgHandlesEngraving() {
        String withText    = NoteHolderBox.generateBoxSVG(STICKY, "Isaac");
        String withoutText = NoteHolderBox.generateBoxSVG(STICKY, "");

        assertTrue(withText.startsWith("<?xml"));
        assertTrue(withText.trim().endsWith("</svg>"));
        assertTrue(withText.contains("data-text='ISAAC'"));
        assertFalse(withText.contains("data-text='Isaac'"));

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
        String sticky = NoteHolderBox.makeFrontPanelNHPath(0, 0, STICKY_W, DEPTH, HEIGHT, TOOTH);
        String index  = NoteHolderBox.makeFrontPanelNHPath(0, 0, INDEX_W,  DEPTH, HEIGHT, TOOTH);

        assertTrue(sticky.startsWith("M 0.000 0.000 H 30.000 V 50.000 "));
        assertTrue(index.startsWith("M 0.000 0.000 H 56.000 V 50.000 "));

        assertEquals(1, sticky.split("Z", -1).length - 1);
        assertEquals(1, index.split("Z", -1).length - 1);
    }

    /**
     * Both styles are 86 mm deep, so the side panel must come out identical
     * whichever width is used. It has six slots for the bottom panel and
     * a foot cutout across the middle half of the bottom edge
     * (from x = 64.5 back to x = 21.5, 10 mm high).
     */
    @Test
    public void sidePanelIsSameForBothStyles() {
        String sticky = NoteHolderBox.makeSidePanelNHPath(0, 0, STICKY_W, DEPTH, HEIGHT, TOOTH);
        String index  = NoteHolderBox.makeSidePanelNHPath(0, 0, INDEX_W,  DEPTH, HEIGHT, TOOTH);

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
        String sticky = NoteHolderBox.generateBoxSVG(STICKY, "");
        String index  = NoteHolderBox.generateBoxSVG(INDEX, "");

        assertTrue(sticky.contains(
                "<svg width='418mm' height='366mm' viewBox='0 0 418 366' "));
        assertTrue(index.contains(
                "<svg width='470mm' height='366mm' viewBox='0 0 470 366' "));

        assertEquals(6, sticky.split("class='cut'", -1).length - 1);
        assertEquals(6, index.split("class='cut'", -1).length - 1);
    }

    /**
     * The bottom panel is a single closed outline (no separate slots), inset
     * 3 mm from its origin, with six male teeth reaching out to the panel
     * edge on the top, right and left sides to fit the side/back slots.
     */
    @Test
    public void bottomPanelIsOneOutlineWithSixTeethPerSide() {
        String bottom = NoteHolderBox.makeBottomPanelNHPath(0, 0, STICKY_W, DEPTH, HEIGHT, TOOTH);

        assertTrue(bottom.startsWith("M 3.000 3.000 "));
        assertEquals(1, bottom.split("Z", -1).length - 1);

        assertEquals(6, bottom.split("V 0\\.000 ", -1).length - 1);   // top edge
        assertEquals(6, bottom.split("H 86\\.000 ", -1).length - 1);  // right edge
        assertEquals(6, bottom.split("H 0\\.000 ", -1).length - 1);   // left edge
    }

    /**
     * The back panel has six slots for the bottom panel's teeth whatever its
     * width, and its path is drawn from the (x, y) passed in.
     */
    @Test
    public void backPanelHasSixSlotsAndFollowsOffset() {
        String back = NoteHolderBox.makeBackPanelNHPath(10, 20, INDEX_W, DEPTH, HEIGHT, TOOTH);

        assertTrue(back.startsWith("M 10.000 20.000 H 148.000 "));
        assertEquals(6, back.split("Z", -1).length - 1);
    }

    /**
     * The engraving is centred on the bottom panel. The bottom panel sits at
     * (50, 230) on both sheets, so its centre is (93, 273) for the 86 mm
     * sticky holder and (119, 273) for the 138 mm index card holder.
     */
    @Test
    public void engravingCentredOnBottomPanel() {
        String sticky = NoteHolderBox.generateBoxSVG(STICKY, "Isaac");
        String index  = NoteHolderBox.generateBoxSVG(INDEX, "Isaac");

        Rectangle2D s = SvgAssert.engravePathBounds(sticky);
        Rectangle2D i = SvgAssert.engravePathBounds(index);
        assertEquals(93.0,  s.getCenterX(), 0.5);
        assertEquals(273.0, s.getCenterY(), 0.5);
        assertEquals(119.0, i.getCenterX(), 0.5);
        assertEquals(273.0, i.getCenterY(), 0.5);
    }

    /**
     * The engraving is scaled to the panel: "Isaac" is width-limited, so it
     * comes out wider on the 138 mm index card holder than on the 86 mm
     * sticky holder. Short or long, it always stays inside the bottom panel.
     */
    @Test
    public void engravingScalesWithWidthAndStaysOnPanel() {
        String sticky = NoteHolderBox.generateBoxSVG(STICKY, "Isaac");
        String index  = NoteHolderBox.generateBoxSVG(INDEX,  "Isaac");
        assertTrue(SvgAssert.engravePathBounds(index).getWidth() > SvgAssert.engravePathBounds(sticky).getWidth());

        for (String text : new String[] { "A", "Isaac" }) {
            SvgAssert.assertPathsInside(NoteHolderBox.generateBoxSVG(STICKY, text), new Bounds(50, 230, STICKY_W, DEPTH));
            SvgAssert.assertPathsInside(NoteHolderBox.generateBoxSVG(INDEX,  text), new Bounds(50, 230, INDEX_W,  DEPTH));
        }
    }
}