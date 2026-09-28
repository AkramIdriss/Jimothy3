/*

SlidingBoxTest.java
tests the sliding box type

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile 22a

*/
import static org.junit.Assert.*;

import org.junit.Test;

public class SlidingBoxTest {

    /**
     * The drawer is sized from the outer box using 3 mm material:
     *   innerWidth  = outerWidth  - 2 * 3
     *   innerDepth  = outerDepth  - 3
     *   innerHeight = outerHeight - 3
     * An outer box of 100 x 80 x 60 should give an inner box of 94 x 77 x 57,
     * which generateSVG() prints in its dimension label.
     */
    @Test
    public void innerDrawerSizedFromOuterBox() {
        SlidingBox box = new SlidingBox(100, 80, 60, 5, "");
        String svg = box.generateSVG();

        assertTrue(svg.contains("Outer: 100 × 80 × 60 mm"));
        assertTrue(svg.contains("Inner: 94 × 77 × 57 mm"));
    }

    /**
     * A monogram is engraved twice, uppercased: red on the outer top panel
     * and blue on the inner bottom panel. An empty monogram adds neither.
     */
    @Test
    public void monogramOnOuterTopAndInnerBottom() {
        String withName = new SlidingBox(100, 80, 60, 5, "abc").generateSVG();
        String blank    = new SlidingBox(100, 80, 60, 5, "").generateSVG();

        assertTrue(withName.startsWith("<?xml"));
        assertTrue(withName.endsWith("</svg>"));

        int count = withName.split(">ABC</text>", -1).length - 1;
        assertEquals("Monogram should appear on outer top and inner bottom", 2, count);
        assertTrue(withName.contains("fill: rgb(255,0,0)"));
        assertTrue(withName.contains("fill: rgb(0,0,255)"));
        assertFalse(withName.contains(">abc</text>"));

        assertFalse(blank.contains("fill: rgb(255,0,0)"));
        assertFalse(blank.contains("fill: rgb(0,0,255)"));
    }
}