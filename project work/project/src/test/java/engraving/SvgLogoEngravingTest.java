package engraving;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SvgLogoEngravingTest {

    private static final String NS = "xmlns='http://www.w3.org/2000/svg'";

    /** Every colour (attributes, inline styles and <style> blocks) becomes the engrave colour. */
    @Test
    public void allColoursBecomeTheEngraveColour() {
        String svg = new SvgLogoEngraving("logo", "<svg " + NS + " viewBox='0 0 10 10'>"
                + "<style>.a{fill:#123456;stroke:green}</style>"
                + "<rect class='a' width='5' height='5'/>"
                + "<circle r='2' fill='blue' stroke='none'/>"
                + "<path d='M0 0 L1 1' style='stroke: #abcdef; fill: none'/></svg>")
                .toSvg(new Bounds(0, 0, 10, 10));

        assertFalse(svg.contains("#123456"));
        assertFalse(svg.contains("green"));
        assertFalse(svg.contains("blue"));
        assertFalse(svg.contains("#abcdef"));
        assertTrue("'none' must stay none", svg.contains("stroke=\"none\""));
        assertTrue(svg.contains("fill:none"));
        SvgAssert.assertWellFormed(svg);
    }

    /** Scripts and event handlers from the logo file are not copied into the box file. */
    @Test
    public void scriptsAndEventHandlersAreRemoved() {
        String svg = new SvgLogoEngraving("logo", "<svg " + NS + " viewBox='0 0 10 10'>"
                + "<script>alert(1)</script><rect width='5' height='5' onclick='alert(2)'/></svg>")
                .toSvg(new Bounds(0, 0, 10, 10));
        assertFalse(svg.contains("script"));
        assertFalse(svg.contains("alert"));
        assertTrue(svg.contains("<rect"));
    }

    /** A 20x10 logo in a 100x100 area is scaled 5x and centred vertically. */
    @Test
    public void logoIsScaledAndCentred() {
        String svg = new SvgLogoEngraving("logo", "<svg " + NS + " viewBox='0 0 20 10'><rect width='20' height='10'/></svg>")
                .toSvg(new Bounds(0, 0, 100, 100));
        assertTrue(svg, svg.contains("matrix(5.000 0 0 5.000 0.000 25.000)"));
    }

    /** Without a viewBox, width/height (with units) give the logo's size. */
    @Test
    public void widthAndHeightAreUsedWithoutViewBox() {
        String svg = new SvgLogoEngraving("logo", "<svg " + NS + " width='40mm' height='20mm'><rect width='40' height='20'/></svg>")
                .toSvg(new Bounds(0, 0, 80, 80));
        assertTrue(svg, svg.contains("matrix(2.000 0 0 2.000 0.000 20.000)"));
    }

    /** Files exported from Illustrator/Inkscape often have a DOCTYPE; they must still load. */
    @Test
    public void doctypeIsAllowed() {
        new SvgLogoEngraving("logo", "<?xml version='1.0'?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" "
                + "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"><svg " + NS + " viewBox='0 0 1 1'/>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void noSizeInformationIsAnError() {
        new SvgLogoEngraving("logo", "<svg " + NS + "><rect width='5' height='5'/></svg>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notSvgIsAnError() {
        new SvgLogoEngraving("logo", "<html><body/></html>");
    }

    @Test(expected = IllegalArgumentException.class)
    public void brokenXmlIsAnError() {
        new SvgLogoEngraving("logo", "<svg viewBox='0 0 1 1'><rect></svg>");
    }
}
