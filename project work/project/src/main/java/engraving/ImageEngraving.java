package engraving;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * A picture (PNG, JPG, GIF, BMP) turned into pure black and white and engraved as vector shapes.
 *
 * <p>A laser either burns a spot or it doesn't, so each pixel becomes "engrave" (dark) or
 * "leave alone" (light) using a brightness threshold. Runs of dark pixels in each row are
 * turned into rectangles, which every laser program can engrave.
 */
public class ImageEngraving implements Engraving {

    /** Default brightness cut-off (0 = black, 255 = white). Darker pixels than this are engraved. */
    public static final int DEFAULT_THRESHOLD = 128;

    /** Images are shrunk so their longer side is at most this many pixels (keeps the SVG small). */
    public static final int MAX_RESOLUTION = 200;

    private final String name;
    private final boolean[][] dark;   // [row][column], true = engrave
    private final int threshold;
    private final boolean inverted;

    /** Loads an image file. */
    public static ImageEngraving fromFile(Path file, int threshold, boolean invert) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("Image file not found: " + file);
        }
        BufferedImage image = ImageIO.read(file.toFile());
        if (image == null) {
            throw new IOException("Not a supported image (use PNG, JPG, GIF or BMP): " + file);
        }
        return new ImageEngraving(file.getFileName().toString(), image, threshold, invert);
    }

    /**
     * @param name      shown in the summary (usually the file name)
     * @param image     the picture
     * @param threshold 0-255; pixels darker than this are engraved
     * @param invert    if true, the <i>light</i> parts are engraved instead
     */
    public ImageEngraving(String name, BufferedImage image, int threshold, boolean invert) {
        this.name = name;
        this.threshold = Math.max(0, Math.min(255, threshold));
        this.inverted = invert;
        this.dark = toBlackAndWhite(shrink(image), this.threshold, invert);
    }

    public int widthPx() {
        return dark.length == 0 ? 0 : dark[0].length;
    }

    public int heightPx() {
        return dark.length;
    }

    @Override
    public String toSvg(Bounds area) {
        Path2D.Double pixels = new Path2D.Double();
        for (int row = 0; row < dark.length; row++) {
            int col = 0;
            while (col < dark[row].length) {
                if (!dark[row][col]) {
                    col++;
                    continue;
                }
                int start = col;
                while (col < dark[row].length && dark[row][col]) {
                    col++;
                }
                pixels.moveTo(start, row);
                pixels.lineTo(col, row);
                pixels.lineTo(col, row + 1);
                pixels.lineTo(start, row + 1);
                pixels.closePath();
            }
        }
        if (SvgUtil.controlBounds(pixels) == null || area.isEmpty()) {
            return "";   // nothing dark enough to engrave
        }
        // Fit the whole picture (not just its dark parts) so the layout matches the original image.
        AffineTransform fit = SvgUtil.fitTransform(new Rectangle2D.Double(0, 0, widthPx(), heightPx()), area);
        return "<path " + EngraveStyle.attributes() + " d='"
                + SvgUtil.toPathData(fit.createTransformedShape(pixels)) + "' />\n";
    }

    /**
     * A rough text preview of what will be engraved ('#' = engraved), at most {@code columns}
     * characters wide. Terminal characters are about twice as tall as wide, so rows are halved.
     */
    public String asciiPreview(int columns) {
        int w = widthPx(), h = heightPx();
        if (w == 0 || h == 0) {
            return "";
        }
        int cols = Math.min(columns, w);
        double step = (double) w / cols;
        int rows = Math.max(1, (int) Math.round(h / (step * 2)));
        StringBuilder out = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            int y = Math.min(h - 1, (int) (r * step * 2));
            for (int c = 0; c < cols; c++) {
                int x = Math.min(w - 1, (int) (c * step));
                out.append(dark[y][x] ? '#' : ' ');
            }
            out.append('\n');
        }
        return out.toString();
    }

    @Override
    public String describe() {
        return "Image " + name + " (" + widthPx() + "x" + heightPx() + " px, threshold " + threshold
                + (inverted ? ", inverted" : "") + ")";
    }

    /** Scales the image down to at most MAX_RESOLUTION on its longer side, on a white background. */
    private static BufferedImage shrink(BufferedImage src) {
        double scale = Math.min(1.0, (double) MAX_RESOLUTION / Math.max(src.getWidth(), src.getHeight()));
        int w = Math.max(1, (int) Math.round(src.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(src.getHeight() * scale));
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.WHITE);   // transparent areas count as "not engraved"
        g.fillRect(0, 0, w, h);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static boolean[][] toBlackAndWhite(BufferedImage img, int threshold, boolean invert) {
        boolean[][] dark = new boolean[img.getHeight()][img.getWidth()];
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
                double brightness = 0.299 * r + 0.587 * g + 0.114 * b;
                dark[y][x] = (brightness < threshold) != invert;
            }
        }
        return dark;
    }
}
