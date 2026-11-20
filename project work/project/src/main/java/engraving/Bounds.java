package engraving;

/**
 * A rectangle on the SVG sheet, in SVG units (mm). {@code (x, y)} is the top-left corner.
 */
public record Bounds(double x, double y, double width, double height) {

    public double centerX() {
        return x + width / 2.0;
    }

    public double centerY() {
        return y + height / 2.0;
    }

    public boolean isEmpty() {
        return width <= 0 || height <= 0;
    }

    /** Shrinks the rectangle by {@code margin} on every side (useful to stay clear of finger joints). */
    public Bounds inset(double margin) {
        return new Bounds(x + margin, y + margin,
                Math.max(0, width - 2 * margin), Math.max(0, height - 2 * margin));
    }

    /** Scales the rectangle about its centre, e.g. {@code scaled(0.8)} keeps the middle 80%. */
    public Bounds scaled(double factor) {
        double w = width * factor;
        double h = height * factor;
        return new Bounds(centerX() - w / 2.0, centerY() - h / 2.0, w, h);
    }

    /** Splits the rectangle into {@code count} equal rows stacked top to bottom, separated by {@code gap}. */
    public Bounds[] rows(int count, double gap) {
        Bounds[] rows = new Bounds[count];
        double rowHeight = Math.max(0, (height - gap * (count - 1)) / count);
        for (int i = 0; i < count; i++) {
            rows[i] = new Bounds(x, y + i * (rowHeight + gap), width, rowHeight);
        }
        return rows;
    }

    /** True if the point is inside this rectangle, allowing {@code tolerance} for rounding. */
    public boolean contains(double px, double py, double tolerance) {
        return px >= x - tolerance && px <= x + width + tolerance
            && py >= y - tolerance && py <= y + height + tolerance;
    }
}
