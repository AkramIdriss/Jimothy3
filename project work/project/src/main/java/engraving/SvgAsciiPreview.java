package engraving;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Draws a finished SVG sheet as text in the terminal: {@code - | +} for cut lines, engraved text as
 * readable words, {@code #} for engraved pictures and logos, and the panel labels.
 *
 * <p>It reads the SVG itself rather than asking the boxes anything, so it shows what is really in
 * the file and works for any new box without changes.
 */
public final class SvgAsciiPreview {

    private SvgAsciiPreview() { }

    /** Terminal characters are about twice as tall as they are wide. */
    private static final double CELL_ASPECT = 2.0;
    private static final int DEFAULT_COLUMNS = 80;
    private static final int MIN_COLUMNS = 40;
    private static final int MAX_COLUMNS = 160;
    private static final int MAX_ROWS = 60;

    /** Sub-pixels per character used to measure how much of a character an engraving covers. */
    private static final int SUB_X = 4;
    private static final int SUB_Y = 8;

    /** How much of a character must be covered to show '#': little on the sheet, more in a close-up. */
    private static final double SHEET_COVERAGE = 0.12;
    private static final double CLOSE_UP_COVERAGE = 0.40;

    private static final int CLOSE_UP_MAX_COLUMNS = 72;
    private static final int CLOSE_UP_MAX_ROWS = 24;

    /** Outline detail smaller than this many characters is always smoothed away. */
    private static final double SMOOTHING = 1.0;

    /** Finger joints are about this deep or less (mm); they are smoothed away however wide the terminal is. */
    private static final double FINGER_JOINT_MM = 5.0;

    private static final int HORIZONTAL = 1, VERTICAL = 2, SLASH = 4, BACKSLASH = 8, HOLE = 16;

    /** A closed outline smaller than this many characters each way (like a slot) is drawn as one dot. */
    private static final double HOLE_SIZE = 3.0;

    private static final Pattern TRANSFORM = Pattern.compile("(matrix|translate|scale|rotate)\\(([^)]*)\\)");

    /** {@code text} is the engraved words, or null for anything that isn't text. */
    private record Item(Shape shape, boolean cut, boolean filled, String text) { }

    private record Label(double x, double y, String text, boolean centered) { }

    /** Prints the preview, sized to the terminal, with a one-line key. */
    public static void print(String svg) {
        System.out.println();
        System.out.println("Preview (not to scale):  - | + cut lines   . slots   # engraved picture or logo");
        System.out.println("Words inside a panel are the text that will be engraved there.");
        System.out.println();
        System.out.println(render(svg, terminalColumns(), MAX_ROWS));
    }

    /**
     * Draws {@code svg} as text: the whole sheet, then a close-up of each engraved panel.
     *
     * @return the picture; the sheet is at most {@code maxColumns} wide and {@code maxRows} tall
     */
    public static String render(String svg, int maxColumns, int maxRows) {
        List<Item> items = new ArrayList<>();
        List<Label> labels = new ArrayList<>();
        Element root = SvgLogoEngraving.parse(svg).getDocumentElement();
        collect(root, new AffineTransform(), false, items, labels);

        Rectangle2D area = boundsOf(items);
        if (area == null || area.getWidth() <= 0 || area.getHeight() <= 0) {
            return "";
        }
        StringBuilder out = new StringBuilder(draw(items, labels, area, maxColumns, maxRows, SHEET_COVERAGE));
        appendCloseUps(out, items, labels, maxColumns);
        return out.toString();
    }

    /** A picture or logo, as opposed to a cut line or text (which is shown as words). */
    private static boolean isDrawing(Item item) {
        return !item.cut() && item.text() == null;
    }

    /** Adds a larger view of every panel with a picture or logo, since the sheet view is small. */
    private static void appendCloseUps(StringBuilder out, List<Item> items, List<Label> labels, int maxColumns) {
        Set<Item> panels = new LinkedHashSet<>();
        for (Item item : items) {
            if (isDrawing(item)) {
                Item panel = panelHolding(item, items);
                if (panel != null) {
                    panels.add(panel);
                }
            }
        }
        for (Item panel : panels) {
            List<Item> inside = new ArrayList<>();
            for (Item item : items) {
                if (isDrawing(item) && panelHolding(item, items) == panel) {
                    inside.add(item);
                }
            }
            Rectangle2D area = boundsOf(inside);
            out.append("\nClose-up: ").append(titleOf(panel.shape().getBounds2D(), labels)).append("\n\n");
            out.append(draw(inside, List.of(), area, Math.min(maxColumns, CLOSE_UP_MAX_COLUMNS),
                    CLOSE_UP_MAX_ROWS, CLOSE_UP_COVERAGE));
        }
    }

    /** The smallest cut outline around an engraving, i.e. the panel it is on. */
    private static Item panelHolding(Item engraving, List<Item> items) {
        Rectangle2D b = engraving.shape().getBounds2D();
        Item best = null;
        double bestArea = Double.MAX_VALUE;
        for (Item item : items) {
            Rectangle2D p = item.shape().getBounds2D();
            double size = p.getWidth() * p.getHeight();
            if (item.cut() && p.contains(b.getCenterX(), b.getCenterY()) && size < bestArea) {
                best = item;
                bestArea = size;
            }
        }
        return best;
    }

    /** The label closest above a panel, which is that panel's name. */
    private static String titleOf(Rectangle2D panel, List<Label> labels) {
        String title = "panel";
        double closest = Double.MAX_VALUE;
        for (Label label : labels) {
            double above = panel.getMinY() - label.y();
            if (label.centered() && above >= 0 && above < closest
                    && label.x() >= panel.getMinX() && label.x() <= panel.getMaxX()) {
                title = label.text();
                closest = above;
            }
        }
        return title;
    }

    private static Rectangle2D boundsOf(List<Item> items) {
        Rectangle2D area = null;
        for (Item item : items) {
            Rectangle2D b = item.shape().getBounds2D();
            if (area == null) {
                area = (Rectangle2D) b.clone();
            } else {
                area.add(b);
            }
        }
        return area;
    }

    private static String draw(List<Item> items, List<Label> labels, Rectangle2D area,
                               int maxColumns, int maxRows, double coverage) {
        // Make room for labels and headings above the first panels.
        area = (Rectangle2D) area.clone();
        for (Label label : labels) {
            area.add(area.getX(), label.y());
        }
        int topRows = labels.isEmpty() ? 0 : 1;
        double perUnit = Math.min((maxColumns - 1) / area.getWidth(),
                (maxRows - 1 - topRows) * CELL_ASPECT / area.getHeight());
        double sy = perUnit / CELL_ASPECT;
        int cols = (int) Math.ceil(area.getWidth() * perUnit) + 1;
        int rows = (int) Math.ceil(area.getHeight() * sy) + 1 + topRows;

        AffineTransform toCells = new AffineTransform();
        toCells.scale(perUnit, sy);
        toCells.translate(-area.getX(), -area.getY() + topRows / sy);

        char[][] grid = new char[rows][cols];
        drawCuts(items, toCells, grid, Math.max(SMOOTHING, FINGER_JOINT_MM * perUnit));
        drawEngravings(items, toCells, grid, coverage);
        for (Label label : labels) {
            drawLabel(label, toCells, grid);
        }
        for (Item item : items) {
            if (item.text() != null) {
                drawWords(item, items, toCells, grid);
            }
        }
        return toText(grid);
    }

    /** Current terminal width, or a safe default if it can't be found (e.g. output is a file). */
    static int terminalColumns() {
        int columns = DEFAULT_COLUMNS;
        try {
            String env = System.getenv("COLUMNS");
            if (env != null && !env.isBlank()) {
                columns = Integer.parseInt(env.trim());
            } else {
                Process p = new ProcessBuilder("sh", "-c", "stty size < /dev/tty").redirectErrorStream(true).start();
                String out = new String(p.getInputStream().readAllBytes()).trim();
                if (p.waitFor() == 0) {
                    columns = Integer.parseInt(out.split("\\s+")[1]);
                }
            }
        } catch (Exception ignored) {
            // keep the default
        }
        return Math.max(MIN_COLUMNS, Math.min(MAX_COLUMNS, columns));
    }

    // ---------- reading the SVG ----------

    private static void collect(Element el, AffineTransform parent, boolean inEngraving,
                                List<Item> items, List<Label> labels) {
        AffineTransform t = new AffineTransform(parent);
        applyTransform(t, el.getAttribute("transform"));
        boolean engrave = inEngraving || hasClass(el, EngraveStyle.CSS_CLASS);

        String name = el.getLocalName();
        switch (name) {
            case "defs", "style", "script", "clipPath", "mask", "symbol", "title", "desc", "metadata" -> {
                return;
            }
            case "text" -> {
                if (!engrave) {
                    labels.add(new Label(number(el, "x"), number(el, "y"), el.getTextContent().trim(),
                            "middle".equals(el.getAttribute("text-anchor"))));
                }
                return;
            }
            default -> { }
        }

        Shape shape = shapeOf(el, name);
        if (shape != null && (engrave || hasClass(el, "cut"))) {
            String words = el.getAttribute("data-text");
            items.add(new Item(t.createTransformedShape(shape), !engrave, !noFill(el), words.isBlank() ? null : words));
        }
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element child) {
                collect(child, t, engrave, items, labels);
            }
        }
    }

    private static Shape shapeOf(Element el, String name) {
        switch (name) {
            case "path":
                return SvgPathParser.parse(el.getAttribute("d"));
            case "rect":
                return new Rectangle2D.Double(number(el, "x"), number(el, "y"), number(el, "width"), number(el, "height"));
            case "circle": {
                double r = number(el, "r");
                return new Ellipse2D.Double(number(el, "cx") - r, number(el, "cy") - r, 2 * r, 2 * r);
            }
            case "ellipse": {
                double rx = number(el, "rx"), ry = number(el, "ry");
                return new Ellipse2D.Double(number(el, "cx") - rx, number(el, "cy") - ry, 2 * rx, 2 * ry);
            }
            case "line":
                return new Line2D.Double(number(el, "x1"), number(el, "y1"), number(el, "x2"), number(el, "y2"));
            case "polygon", "polyline": {
                String points = el.getAttribute("points").trim();
                if (points.isEmpty()) {
                    return null;
                }
                return SvgPathParser.parse("M " + points + ("polygon".equals(name) ? " Z" : ""));
            }
            default:
                return null;
        }
    }

    private static void applyTransform(AffineTransform t, String transform) {
        Matcher m = TRANSFORM.matcher(transform);
        while (m.find()) {
            String[] parts = m.group(2).trim().split("[\\s,]+");
            double[] v = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                v[i] = Double.parseDouble(parts[i]);
            }
            switch (m.group(1)) {
                case "matrix" -> t.concatenate(new AffineTransform(v[0], v[1], v[2], v[3], v[4], v[5]));
                case "translate" -> t.translate(v[0], v.length > 1 ? v[1] : 0);
                case "scale" -> t.scale(v[0], v.length > 1 ? v[1] : v[0]);
                default -> {
                    if (v.length == 3) {
                        t.rotate(Math.toRadians(v[0]), v[1], v[2]);
                    } else {
                        t.rotate(Math.toRadians(v[0]));
                    }
                }
            }
        }
    }

    private static boolean hasClass(Element el, String cssClass) {
        return (" " + el.getAttribute("class") + " ").contains(" " + cssClass + " ");
    }

    private static boolean noFill(Element el) {
        return el.getAttribute("fill").trim().equalsIgnoreCase("none")
                || el.getAttribute("style").replace(" ", "").toLowerCase().contains("fill:none");
    }

    private static double number(Element el, String attribute) {
        Matcher m = Pattern.compile("^\\s*(-?[0-9]*\\.?[0-9]+)").matcher(el.getAttribute(attribute));
        return m.find() ? Double.parseDouble(m.group(1)) : 0;
    }

    // ---------- drawing ----------

    private static void drawCuts(List<Item> items, AffineTransform toCells, char[][] grid, double smoothing) {
        int[][] bits = new int[grid.length][grid[0].length];
        for (Item item : items) {
            if (item.cut()) {
                for (List<double[]> line : polylines(toCells.createTransformedShape(item.shape()))) {
                    if (markIfTinyHole(line, bits)) {
                        continue;
                    }
                    List<double[]> smooth = simplify(line, smoothing);
                    squareUp(smooth, smoothing);
                    for (int i = 1; i < smooth.size(); i++) {
                        double[] a = smooth.get(i - 1), b = smooth.get(i);
                        trace(bits, a[0], a[1], b[0], b[1]);
                    }
                }
            }
        }
        for (int r = 0; r < grid.length; r++) {
            for (int col = 0; col < grid[r].length; col++) {
                grid[r][col] = charFor(bits[r][col]);
            }
        }
    }

    private static boolean markIfTinyHole(List<double[]> line, int[][] bits) {
        double[] first = line.get(0), last = line.get(line.size() - 1);
        if (first[0] != last[0] || first[1] != last[1]) {
            return false;
        }
        double minX = first[0], maxX = first[0], minY = first[1], maxY = first[1];
        for (double[] p : line) {
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
        }
        if (maxX - minX >= HOLE_SIZE || (maxY - minY) * CELL_ASPECT >= HOLE_SIZE) {
            return false;
        }
        int row = (int) Math.floor((minY + maxY) / 2);
        int col = (int) Math.floor((minX + maxX) / 2);
        if (row >= 0 && row < bits.length && col >= 0 && col < bits[0].length) {
            bits[row][col] |= HOLE;
        }
        return true;
    }

    /** Splits a shape into lists of straight-line points, one list per separate outline. */
    private static List<List<double[]>> polylines(Shape shape) {
        List<List<double[]>> lines = new ArrayList<>();
        List<double[]> current = null;
        double[] c = new double[6];
        for (PathIterator it = new FlatteningPathIterator(shape.getPathIterator(null), 0.1); !it.isDone(); it.next()) {
            switch (it.currentSegment(c)) {
                case PathIterator.SEG_MOVETO -> {
                    current = new ArrayList<>();
                    lines.add(current);
                    current.add(new double[] {c[0], c[1]});
                }
                case PathIterator.SEG_LINETO -> current.add(new double[] {c[0], c[1]});
                case PathIterator.SEG_CLOSE -> current.add(current.get(0));   // same point object: moving one moves both
                default -> { }
            }
        }
        return lines;
    }

    /** Removes detail too small to draw (Ramer-Douglas-Peucker), so finger joints become a clean edge. */
    private static List<double[]> simplify(List<double[]> points, double smoothing) {
        int last = points.size() - 1;
        if (last < 2) {
            return points;
        }
        boolean[] keep = new boolean[points.size()];
        keep[0] = true;
        keep[last] = true;
        // A closed outline starts and ends on the same point, so also split it at its farthest point.
        int far = 0;
        double farthest = -1;
        for (int i = 1; i < last; i++) {
            double d = distance(points.get(i), points.get(0));
            if (d > farthest) {
                farthest = d;
                far = i;
            }
        }
        keep[far] = true;
        simplify(points, 0, far, keep, smoothing);
        simplify(points, far, last, keep, smoothing);

        List<double[]> kept = new ArrayList<>();
        for (int i = 0; i <= last; i++) {
            if (keep[i]) {
                kept.add(points.get(i));
            }
        }
        return kept;
    }

    private static void simplify(List<double[]> points, int from, int to, boolean[] keep, double smoothing) {
        int worst = -1;
        double worstDistance = smoothing;
        for (int i = from + 1; i < to; i++) {
            double d = distanceToLine(points.get(i), points.get(from), points.get(to));
            if (d > worstDistance) {
                worstDistance = d;
                worst = i;
            }
        }
        if (worst >= 0) {
            keep[worst] = true;
            simplify(points, from, worst, keep, smoothing);
            simplify(points, worst, to, keep, smoothing);
        }
    }

    /** Makes almost-straight edges exactly horizontal or vertical, so they don't step between rows or columns. */
    private static void squareUp(List<double[]> points, double smoothing) {
        for (int i = 1; i < points.size(); i++) {
            double[] a = points.get(i - 1), b = points.get(i);
            double dx = Math.abs(a[0] - b[0]);
            double dy = Math.abs(a[1] - b[1]) * CELL_ASPECT;
            if (dy <= smoothing * 1.5 && dx > 3 * dy) {
                a[1] = b[1] = (a[1] + b[1]) / 2;
            } else if (dx <= smoothing * 1.5 && dy > 3 * dx) {
                a[0] = b[0] = (a[0] + b[0]) / 2;
            }
        }
    }

    /** Distances are measured as they look on screen, where a row is twice as tall as a column is wide. */
    private static double distance(double[] a, double[] b) {
        return Math.hypot(a[0] - b[0], (a[1] - b[1]) * CELL_ASPECT);
    }

    private static double distanceToLine(double[] p, double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = (b[1] - a[1]) * CELL_ASPECT;
        double px = p[0] - a[0];
        double py = (p[1] - a[1]) * CELL_ASPECT;
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) {
            return Math.hypot(px, py);
        }
        double t = Math.max(0, Math.min(1, (px * dx + py * dy) / lengthSquared));
        return Math.hypot(px - t * dx, py - t * dy);
    }

    /** Marks every character a cut line passes through with the direction of the line. */
    private static void trace(int[][] bits, double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double tallDy = dy * CELL_ASPECT;    // what the slope looks like on screen
        int direction;
        if (Math.abs(dx) >= 2 * Math.abs(tallDy)) {
            direction = HORIZONTAL;
        } else if (Math.abs(tallDy) >= 2 * Math.abs(dx)) {
            direction = VERTICAL;
        } else {
            direction = (dx > 0) == (dy > 0) ? BACKSLASH : SLASH;
        }

        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) * 4) + 1;
        for (int i = 0; i <= steps; i++) {
            int col = (int) Math.floor(x0 + dx * i / steps);
            int row = (int) Math.floor(y0 + dy * i / steps);
            if (row >= 0 && row < bits.length && col >= 0 && col < bits[0].length) {
                bits[row][col] |= direction;
            }
        }
    }

    private static char charFor(int b) {
        if ((b & HORIZONTAL) != 0 && (b & VERTICAL) != 0) {
            return '+';
        }
        if ((b & HORIZONTAL) != 0) {
            return '-';
        }
        if ((b & VERTICAL) != 0) {
            return '|';
        }
        if ((b & SLASH) != 0) {
            return '/';
        }
        if ((b & BACKSLASH) != 0) {
            return '\\';
        }
        return (b & HOLE) != 0 ? '.' : ' ';
    }

    /** Draws engravings at a higher resolution, then picks a character by how much of each cell is covered. */
    private static void drawEngravings(List<Item> items, AffineTransform toCells, char[][] grid, double coverage) {
        int rows = grid.length, cols = grid[0].length;
        BufferedImage image = new BufferedImage(cols * SUB_X, rows * SUB_Y, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.scale(SUB_X, SUB_Y);
        g.setStroke(new BasicStroke(0.25f));
        for (Item item : items) {
            if (isDrawing(item)) {
                Shape shape = toCells.createTransformedShape(item.shape());
                if (item.filled()) {
                    g.fill(new Path2D.Double(shape));
                } else {
                    g.draw(shape);
                }
            }
        }
        g.dispose();

        for (int r = 0; r < rows; r++) {
            for (int col = 0; col < cols; col++) {
                int covered = 0;
                for (int sy = 0; sy < SUB_Y; sy++) {
                    for (int sx = 0; sx < SUB_X; sx++) {
                        if ((image.getRGB(col * SUB_X + sx, r * SUB_Y + sy) & 0xff) != 0) {
                            covered++;
                        }
                    }
                }
                if (covered / (double) (SUB_X * SUB_Y) >= coverage) {
                    grid[r][col] = '#';
                }
            }
        }
    }

    /** Writes a label where the SVG has it, moving up a row if that would cover a cut line. */
    private static void drawLabel(Label label, AffineTransform toCells, char[][] grid) {
        String text = plain(label.text());
        double[] p = {label.x(), label.y()};
        toCells.transform(p, 0, p, 0, 1);
        int col = (int) Math.round(p[0]) - (label.centered() ? text.length() / 2 : 0);
        col = Math.max(0, col);
        int row = Math.max(0, (int) Math.floor(p[1]));
        if (row >= grid.length) {
            return;
        }
        if (row > 0 && !isBlank(grid[row], col, text.length())) {
            row--;
        }
        for (int i = 0; i < text.length() && col + i < grid[row].length; i++) {
            grid[row][col + i] = text.charAt(i);
        }
    }

    /** Writes engraved text as readable words, centred where it will be engraved. */
    private static void drawWords(Item item, List<Item> items, AffineTransform toCells, char[][] grid) {
        Rectangle2D area = toCells.createTransformedShape(item.shape()).getBounds2D();
        Item panel = panelHolding(item, items);
        double room = panel == null ? area.getWidth()
                : toCells.createTransformedShape(panel.shape()).getBounds2D().getWidth() - 2;

        String[] lines = item.text().split("\n");
        int row = (int) Math.round(area.getCenterY() - lines.length / 2.0);
        for (String line : lines) {
            String text = plain(line);
            int max = Math.max(1, (int) room);
            if (text.length() > max) {
                text = max > 2 ? text.substring(0, max - 2) + ".." : text.substring(0, max);
            }
            int col = Math.max(0, (int) Math.round(area.getCenterX() - text.length() / 2.0));
            if (row >= 0 && row < grid.length) {
                for (int i = 0; i < text.length() && col + i < grid[row].length; i++) {
                    grid[row][col + i] = text.charAt(i);
                }
            }
            row++;
        }
    }

    /** The terminal preview is plain ASCII, so anything else is replaced. */
    private static String plain(String text) {
        return text.replace('×', 'x').replaceAll("[^\\x20-\\x7e]", "?");
    }

    private static boolean isBlank(char[] row, int from, int length) {
        for (int i = from; i < from + length && i < row.length; i++) {
            if (row[i] != ' ') {
                return false;
            }
        }
        return true;
    }

    private static String toText(char[][] grid) {
        StringBuilder out = new StringBuilder();
        for (char[] row : grid) {
            out.append(new String(row).stripTrailing()).append('\n');
        }
        return out.toString().stripTrailing() + "\n";
    }
}
