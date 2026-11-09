package engraving;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.Test;

public class ImageEngravingTest {

    private static final Bounds AREA = new Bounds(0, 0, 40, 40);

    @Test
    public void allWhiteImageEngravesNothing() {
        BufferedImage white = solid(10, 10, Color.WHITE);
        assertEquals("", new ImageEngraving("white", white, 128, false).toSvg(AREA));
    }

    /** Inverting swaps which parts are engraved: white becomes "engrave everything". */
    @Test
    public void invertEngravesTheLightParts() {
        BufferedImage white = solid(10, 10, Color.WHITE);
        String svg = new ImageEngraving("white", white, 128, true).toSvg(AREA);
        SvgAssert.assertPathsInside(svg, AREA);
        assertEquals(40, SvgAssert.engravePathBounds(svg).getWidth(), 0.01);
    }

    /** Grey (brightness 128) is engraved only when the threshold is above it. */
    @Test
    public void thresholdDecidesWhatCountsAsDark() {
        BufferedImage grey = solid(4, 4, new Color(128, 128, 128));
        assertEquals("", new ImageEngraving("g", grey, 100, false).toSvg(AREA));
        assertTrue(new ImageEngraving("g", grey, 200, false).toSvg(AREA).contains("<path"));
    }

    /** The whole picture is fitted (not just the dark part), so a centred dot stays centred. */
    @Test
    public void picturePositionIsKept() {
        BufferedImage img = solid(30, 30, Color.WHITE);
        for (int y = 10; y < 20; y++) {
            for (int x = 10; x < 20; x++) {
                img.setRGB(x, y, Color.BLACK.getRGB());
            }
        }
        var box = SvgAssert.engravePathBounds(new ImageEngraving("dot", img, 128, false).toSvg(AREA));
        assertEquals(20, box.getCenterX(), 0.01);
        assertEquals(20, box.getCenterY(), 0.01);
        assertEquals(40.0 / 3, box.getWidth(), 0.01);
    }

    @Test
    public void bigImagesAreShrunk() {
        ImageEngraving big = new ImageEngraving("big", solid(1000, 500, Color.BLACK), 128, false);
        assertEquals(ImageEngraving.MAX_RESOLUTION, big.widthPx());
        assertEquals(ImageEngraving.MAX_RESOLUTION / 2, big.heightPx());
    }

    @Test
    public void asciiPreviewShowsEngravedPixels() {
        String preview = new ImageEngraving("disc", EngravingContractTest.disc(40, 40), 128, false).asciiPreview(20);
        String[] rows = preview.split("\n");
        assertEquals(10, rows.length);              // 40 px tall, 2 px per column, halved for the terminal
        assertEquals(20, rows[0].length());
        assertEquals(' ', rows[0].charAt(0));       // corner is white
        assertEquals('#', rows[5].charAt(10));      // centre is black
    }

    @Test
    public void readsImageFiles() throws IOException {
        Path file = Files.createTempFile("engrave", ".png");
        try {
            ImageIO.write(EngravingContractTest.disc(20, 20), "png", file.toFile());
            ImageEngraving img = ImageEngraving.fromFile(file, 128, false);
            assertEquals(20, img.widthPx());
            assertTrue(img.describe().startsWith("Image " + file.getFileName()));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test(expected = IOException.class)
    public void notAnImageIsAnError() throws IOException {
        Path file = Files.createTempFile("engrave", ".png");
        try {
            Files.writeString(file, "definitely not a PNG");
            ImageEngraving.fromFile(file, 128, false);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static BufferedImage solid(int w, int h, Color c) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.setRGB(x, y, c.getRGB());
            }
        }
        return img;
    }
}
