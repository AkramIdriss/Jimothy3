package engraving;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;

/** Turns the text of an SVG path's {@code d} attribute into a Java shape. Supports every path command. */
final class SvgPathParser {

    private final String d;
    private int pos = 0;

    private final Path2D.Double path = new Path2D.Double();
    private double x, y;              // current point
    private double startX, startY;    // start of the current sub-path
    private double ctrlX, ctrlY;      // last control point, for S and T
    private char previous = ' ';

    private SvgPathParser(String d) {
        this.d = d;
    }

    /** Parses path data. Anything after the first malformed part is ignored, as browsers do. */
    static Path2D parse(String d) {
        SvgPathParser p = new SvgPathParser(d);
        try {
            p.run();
        } catch (RuntimeException ignored) {
            // keep what was understood so far
        }
        return p.path;
    }

    private void run() {
        char command = ' ';
        while (true) {
            skipSeparators();
            if (pos >= d.length()) {
                return;
            }
            char c = d.charAt(pos);
            if (Character.isLetter(c) && c != 'e' && c != 'E') {
                command = c;
                pos++;
                if (c == 'Z' || c == 'z') {
                    path.closePath();
                    x = startX;
                    y = startY;
                    previous = c;
                    continue;
                }
            } else if (command == 'M') {
                command = 'L';     // extra coordinate pairs after a move are lines
            } else if (command == 'm') {
                command = 'l';
            } else if (command == ' ' || command == 'Z' || command == 'z') {
                throw new IllegalArgumentException("Bad path");
            }
            draw(command);
            previous = command;
        }
    }

    private void draw(char command) {
        boolean rel = Character.isLowerCase(command);
        double ox = rel ? x : 0;
        double oy = rel ? y : 0;
        switch (Character.toUpperCase(command)) {
            case 'M' -> {
                x = ox + number();
                y = oy + number();
                startX = x;
                startY = y;
                path.moveTo(x, y);
            }
            case 'L' -> {
                x = ox + number();
                y = oy + number();
                path.lineTo(x, y);
            }
            case 'H' -> {
                x = ox + number();
                path.lineTo(x, y);
            }
            case 'V' -> {
                y = oy + number();
                path.lineTo(x, y);
            }
            case 'C' -> cubic(ox + number(), oy + number(), ox, oy);
            case 'S' -> {
                boolean follows = "CcSs".indexOf(previous) >= 0;
                cubic(follows ? 2 * x - ctrlX : x, follows ? 2 * y - ctrlY : y, ox, oy);
            }
            case 'Q' -> {
                double cx = ox + number();
                double cy = oy + number();
                quad(cx, cy, ox + number(), oy + number());
            }
            case 'T' -> {
                boolean follows = "QqTt".indexOf(previous) >= 0;
                quad(follows ? 2 * x - ctrlX : x, follows ? 2 * y - ctrlY : y, ox + number(), oy + number());
            }
            case 'A' -> {
                double rx = number();
                double ry = number();
                double rotation = number();
                boolean large = flag();
                boolean sweep = flag();
                arc(rx, ry, rotation, large, sweep, ox + number(), oy + number());
            }
            default -> throw new IllegalArgumentException("Bad path command: " + command);
        }
    }

    private void cubic(double c1x, double c1y, double ox, double oy) {
        double c2x = ox + number();
        double c2y = oy + number();
        double ex = ox + number();
        double ey = oy + number();
        path.curveTo(c1x, c1y, c2x, c2y, ex, ey);
        ctrlX = c2x;
        ctrlY = c2y;
        x = ex;
        y = ey;
    }

    private void quad(double cx, double cy, double ex, double ey) {
        path.quadTo(cx, cy, ex, ey);
        ctrlX = cx;
        ctrlY = cy;
        x = ex;
        y = ey;
    }

    /** Converts SVG's endpoint arc form to Java's centre-and-angles form (SVG spec, appendix B.2.4). */
    private void arc(double rx, double ry, double rotation, boolean large, boolean sweep, double ex, double ey) {
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        if (rx == 0 || ry == 0) {
            path.lineTo(ex, ey);
            x = ex;
            y = ey;
            return;
        }
        double phi = Math.toRadians(rotation % 360);
        double cos = Math.cos(phi);
        double sin = Math.sin(phi);
        double dx2 = (x - ex) / 2;
        double dy2 = (y - ey) / 2;
        double x1 = cos * dx2 + sin * dy2;
        double y1 = -sin * dx2 + cos * dy2;

        double check = (x1 * x1) / (rx * rx) + (y1 * y1) / (ry * ry);
        if (check > 1) {
            rx *= Math.sqrt(check);
            ry *= Math.sqrt(check);
        }
        double prx = rx * rx, pry = ry * ry, px1 = x1 * x1, py1 = y1 * y1;
        double sq = Math.max(0, (prx * pry - prx * py1 - pry * px1) / (prx * py1 + pry * px1));
        double coef = (large == sweep ? -1 : 1) * Math.sqrt(sq);
        double cx1 = coef * (rx * y1 / ry);
        double cy1 = coef * -(ry * x1 / rx);
        double cx = (x + ex) / 2 + (cos * cx1 - sin * cy1);
        double cy = (y + ey) / 2 + (sin * cx1 + cos * cy1);

        double ux = (x1 - cx1) / rx, uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx, vy = (-y1 - cy1) / ry;
        double start = angle(1, 0, ux, uy);
        double extent = angle(ux, uy, vx, vy);
        if (!sweep && extent > 0) {
            extent -= 360;
        } else if (sweep && extent < 0) {
            extent += 360;
        }

        Arc2D.Double arc = new Arc2D.Double(cx - rx, cy - ry, rx * 2, ry * 2, -start, -extent, Arc2D.OPEN);
        path.append(AffineTransform.getRotateInstance(phi, cx, cy).createTransformedShape(arc), true);
        x = ex;
        y = ey;
    }

    private static double angle(double ux, double uy, double vx, double vy) {
        double n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        double a = Math.acos(Math.max(-1, Math.min(1, (ux * vx + uy * vy) / n)));
        return Math.toDegrees(ux * vy - uy * vx < 0 ? -a : a);
    }

    private void skipSeparators() {
        while (pos < d.length() && (Character.isWhitespace(d.charAt(pos)) || d.charAt(pos) == ',')) {
            pos++;
        }
    }

    private boolean flag() {
        skipSeparators();
        return d.charAt(pos++) == '1';
    }

    private double number() {
        skipSeparators();
        int start = pos;
        if (d.charAt(pos) == '-' || d.charAt(pos) == '+') {
            pos++;
        }
        boolean dot = false;
        while (pos < d.length()) {
            char c = d.charAt(pos);
            if (Character.isDigit(c)) {
                pos++;
            } else if (c == '.' && !dot) {
                dot = true;
                pos++;
            } else if ((c == 'e' || c == 'E') && pos > start) {
                pos++;
                if (pos < d.length() && (d.charAt(pos) == '-' || d.charAt(pos) == '+')) {
                    pos++;
                }
            } else {
                break;
            }
        }
        return Double.parseDouble(d.substring(start, pos));
    }
}
