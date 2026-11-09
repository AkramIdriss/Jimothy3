package engraving;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import org.junit.Test;

public class SvgAsciiPreviewTest {

    private static String sheet(String body) {
        return "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'>" + body + "</svg>";
    }

    private static final String PANEL = "<path d='M 0 0 H 80 V 40 H 0 Z' class='cut' />";

    @Test
    public void cutLinesUseDashesBarsAndCorners() {
        String text = SvgAsciiPreview.render(sheet(PANEL), 41, 60);
        String[] rows = text.split("\n");
        assertEquals('+', rows[0].charAt(0));
        assertEquals('-', rows[0].charAt(10));
        assertEquals('|', rows[3].charAt(0));
        assertEquals('+', rows[0].charAt(rows[0].length() - 1));
    }

    @Test
    public void labelsAppearAsText() {
        String text = SvgAsciiPreview.render(sheet(PANEL
                + "<text x='40' y='-10' class='label' text-anchor='middle'>Front</text>"), 41, 60);
        assertTrue(text, text.contains("Front"));
    }

    @Test
    public void engravingsShowOnTheSheetAndInACloseUp() {
        String text = SvgAsciiPreview.render(sheet(PANEL
                + "<text x='40' y='-10' class='label' text-anchor='middle'>Top</text>"
                + "<path class='engrave' d='M 20 10 H 60 V 30 H 20 Z' />"), 41, 60);
        assertTrue(text, text.contains("#"));
        assertTrue(text, text.contains("Close-up: Top"));
    }

    /** Engraved text is shown as words, never as letters made of hashtags. */
    @Test
    public void engravedTextIsShownAsWords() {
        String text = SvgAsciiPreview.render(sheet(PANEL
                + new TextEngraving(java.util.List.of("Tom & Jerry's", "Box"), Fonts.defaultFont())
                        .toSvg(new Bounds(10, 5, 60, 30))), 41, 60);
        assertTrue(text, text.contains("Tom & Jerry's"));
        assertTrue(text, text.contains("Box"));
        assertFalse(text, text.contains("#"));
        assertFalse(text, text.contains("Close-up"));
    }

    @Test
    public void textTooWideForThePanelIsShortened() {
        String text = SvgAsciiPreview.render(sheet(PANEL
                + new TextEngraving(java.util.List.of("A very long name that cannot fit"), Fonts.defaultFont())
                        .toSvg(new Bounds(10, 5, 60, 30))), 21, 60);
        assertTrue(text, text.contains(".."));
    }

    @Test
    public void noEngravingMeansNoCloseUp() {
        assertFalse(SvgAsciiPreview.render(sheet(PANEL), 41, 60).contains("Close-up"));
    }

    @Test
    public void logosInsideTransformedGroupsAreDrawn() {
        String text = SvgAsciiPreview.render(sheet(PANEL
                + "<g class='engrave' transform='matrix(2 0 0 2 20 10)'><rect width='20' height='10'/></g>"), 41, 60);
        assertTrue(text, text.contains("#"));
    }

    /** Finger joints smaller than a character are smoothed into a clean straight edge. */
    @Test
    public void tinyZigZagsAreSmoothedAway() {
        StringBuilder d = new StringBuilder("M 0 0 ");
        for (int x = 2; x <= 80; x += 2) {
            d.append("H ").append(x).append(' ').append("V ").append(x % 4 == 0 ? 0 : 0.5).append(' ');
        }
        d.append("V 40 H 0 Z");
        String[] rows = SvgAsciiPreview.render(sheet("<path d='" + d + "' class='cut' />"), 41, 60).split("\n");
        assertEquals("+---------------------------------------+", rows[0]);
    }

    @Test
    public void nothingToDrawGivesEmptyText() {
        assertEquals("", SvgAsciiPreview.render(sheet(""), 41, 60));
    }

    /** The bounds of parsed path text, for checking each kind of path command. */
    private static Rectangle2D bounds(String d) {
        return SvgPathParser.parse(d).getBounds2D();
    }

    @Test
    public void pathParserHandlesAbsoluteAndRelativeCommands() {
        assertEquals(new Rectangle2D.Double(1, 2, 10, 5), bounds("M 1 2 H 11 V 7 H 1 Z"));
        assertEquals(new Rectangle2D.Double(1, 2, 10, 5), bounds("m1,2 h10 v5 h-10 z"));
        assertEquals(new Rectangle2D.Double(0, 0, 4, 3), bounds("M0 0 4 0 4 3"));   // extra pairs are lines
    }

    @Test
    public void pathParserDrawsArcsOnTheCorrectSide() {
        Rectangle2D up = bounds("M 0 0 A 10 10 0 0 1 20 0");
        assertEquals(-10, up.getMinY(), 0.01);
        assertEquals(0, up.getMaxY(), 0.01);
        Rectangle2D down = bounds("M 0 0 A 10 10 0 0 0 20 0");
        assertEquals(10, down.getMaxY(), 0.01);
    }

    @Test
    public void pathParserHandlesCurvesAndBadInput() {
        assertEquals(10, bounds("M 0 0 C 0 5 10 5 10 0").getMaxX(), 0.01);
        assertEquals(20, bounds("M 0 0 Q 5 5 10 0 T 20 0").getMaxX(), 0.01);
        assertEquals(5, bounds("M 0 0 L 5 5 oops").getMaxX(), 0.01);       // keeps what it understood
    }
}
