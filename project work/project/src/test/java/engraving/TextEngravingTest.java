package engraving;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.junit.Test;

public class TextEngravingTest {

    private static final Bounds AREA = new Bounds(50, 20, 100, 40);

    /** Text is drawn as outlines, so the SVG doesn't need the font installed on the cutter's PC. */
    @Test
    public void textBecomesOutlinesNotTextElements() {
        String svg = new TextEngraving(List.of("Hello"), Fonts.defaultFont()).toSvg(AREA);
        assertTrue(svg.startsWith("<path "));
        assertFalse(svg.contains("<text"));
        assertFalse(svg.contains(">Hello<"));
    }

    /** The text is scaled up until it touches the area on at least one side, and is centred. */
    @Test
    public void textFillsAndIsCentredInTheArea() {
        Rectangle2D box = SvgAssert.engravePathBounds(
                new TextEngraving(List.of("A much longer name"), Fonts.defaultFont()).toSvg(AREA));
        boolean fillsWidth = Math.abs(box.getWidth() - AREA.width()) < 0.01;
        boolean fillsHeight = Math.abs(box.getHeight() - AREA.height()) < 0.01;
        assertTrue("should fill the width or the height, got " + box, fillsWidth || fillsHeight);
        assertEquals(AREA.centerX(), box.getCenterX(), 0.01);
        assertEquals(AREA.centerY(), box.getCenterY(), 0.01);
    }

    /** Each extra line makes the block taller, so the same text gets narrower in a short panel. */
    @Test
    public void moreLinesMeansSmallerText() {
        Rectangle2D one = SvgAssert.engravePathBounds(
                new TextEngraving(List.of("BOX"), Fonts.defaultFont()).toSvg(AREA));
        Rectangle2D three = SvgAssert.engravePathBounds(
                new TextEngraving(List.of("BOX", "BOX", "BOX"), Fonts.defaultFont()).toSvg(AREA));
        assertTrue(three.getWidth() < one.getWidth());
    }

    /** Characters that are special in XML can't break the file, since text is turned into shapes. */
    @Test
    public void xmlSpecialCharactersAreSafe() {
        String svg = new TextEngraving(List.of("Tom & Jerry <3 \"quotes\" 'apos'"), Fonts.defaultFont()).toSvg(AREA);
        SvgAssert.assertWellFormed(svg);
    }

    @Test
    public void differentFontsGiveDifferentShapes() {
        String serif = new TextEngraving(List.of("Rg"), Fonts.named(Font.SERIF)).toSvg(AREA);
        String mono = new TextEngraving(List.of("Rg"), Fonts.named(Font.MONOSPACED)).toSvg(AREA);
        assertNotEquals(serif, mono);
    }

    @Test
    public void blankLinesAreDroppedAndSpacesTrimmed() {
        TextEngraving t = new TextEngraving(List.of("  Hi  ", "", "   ", "there"), Fonts.defaultFont());
        assertEquals(List.of("Hi", "there"), t.lines());
        assertTrue(t.describe().contains("\"Hi / there\""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void onlyBlankLinesIsAnError() {
        new TextEngraving(List.of("", "  "), Fonts.defaultFont());
    }
}
