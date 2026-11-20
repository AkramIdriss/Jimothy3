package jimothypackage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import engraving.SvgAsciiPreview;
import org.junit.Test;

/** The terminal preview on the real sheets from every box. */
public class BoxPreviewTest {

    private static final int COLUMNS = 80;

    private static void check(String svg, String... labels) {
        String text = SvgAsciiPreview.render(svg, COLUMNS, 60);
        for (String line : text.split("\n")) {
            assertTrue("Too wide: " + line, line.length() <= COLUMNS);
            assertTrue("Not plain ASCII: " + line, line.chars().allMatch(c -> c < 127));
        }
        for (String label : labels) {
            assertTrue("Missing " + label + " in\n" + text, text.contains(label));
        }
    }

    @Test
    public void simpleBox() {
        check(SimpleBox.generateBoxSVG(100, 60, 80, 5, "JDE"), "Front", "Bottom", "Back", "JDE");
    }

    @Test
    public void slidingBoxIncludingItsHandleArc() {
        check(SlidingBox.generateBoxSVG(100, 60, 80, 5, "Pens"), "OUTER BOX", "PENS");
    }

    @Test
    public void noteHolder() {
        check(NoteHolderBox.generateBoxSVG(2, "Isaac"),
                "Back Panel", "ISAAC");
    }

    /** Your terminal can be any width, so outlines must be clean at all of them. */
    @Test
    public void simpleBoxOutlinesAreCleanAtEveryTerminalWidth() {
        for (int width = 40; width <= 160; width++) {
            String text = SvgAsciiPreview.render(SimpleBox.generateBoxSVG(100, 60, 80, 5, ""), width, 60);
            assertFalse("Slanted outline at width " + width + ":\n" + text,
                    text.contains("/") || text.contains("\\"));
            assertFalse("Doubled corner at width " + width + ":\n" + text, text.contains("++"));
            assertFalse("Doubled edge at width " + width + ":\n" + text, text.contains("||"));
            for (String line : text.split("\n")) {
                assertTrue("Too wide at " + width, line.length() <= width);
            }
        }
    }

    @Test
    public void slotsDoNotTurnIntoDiagonalNoise() {
        String text = SvgAsciiPreview.render(SimpleBox.generateBoxSVG(100, 60, 80, 5, ""), COLUMNS, 60);
        assertFalse(text, text.contains("\\"));
    }
}
