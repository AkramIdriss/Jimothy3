/*

NoteHolderBoxTest.java
tests the note holder box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile 22a

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
}