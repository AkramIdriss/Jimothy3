package engraving;

/**
 * Shared look-and-feel for every engraving. Change values here, not in individual classes.
 *
 * <p>Laser software (LightBurn, Inkscape, the Glowforge app...) assigns cut / engrave
 * operations by colour. Cut lines are drawn as thin black strokes, so engravings use a
 * different solid fill colour. If your cutter expects another colour, change
 * {@link #ENGRAVE_COLOR} and every box follows.
 */
public final class EngraveStyle {

    private EngraveStyle() { }

    /** Fill colour for everything that gets engraved. */
    public static final String ENGRAVE_COLOR = "#ff0000";

    /** CSS class put on every engraving element, so engravings are easy to find in the SVG. */
    public static final String CSS_CLASS = "engrave";

    /** Space (mm) kept clear around a panel's edge, so engravings never touch the finger joints. */
    public static final double PANEL_MARGIN_MM = 6.0;

    /** Fraction of the remaining panel area an engraving may fill (the rest is breathing room). */
    public static final double FILL_FRACTION = 0.85;

    /** Gap (mm) between engravings when several share one panel. */
    public static final double STACK_GAP_MM = 3.0;

    /** Attributes to put on an engraving element: class, fill colour and no stroke. */
    public static String attributes() {
        return "class='" + CSS_CLASS + "' fill='" + ENGRAVE_COLOR + "' stroke='none'";
    }
}
