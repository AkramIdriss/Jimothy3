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
}