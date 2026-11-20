package engraving;

/**
 * Something that can be laser-engraved onto a box panel: text, a logo, a picture...
 *
 * <p>This is the only thing a box needs to know about engravings. A box hands an
 * engraving the rectangle it may use, and gets back SVG markup to put in its file.
 *
 * <h2>Rules every implementation must follow</h2>
 * <ol>
 *   <li><b>Stay inside the given area.</b> Everything drawn must fit inside {@code area}
 *       (scaled down and centred as needed). The shared test
 *       {@code EngravingContractTest} checks this for every type.</li>
 *   <li><b>Use the engrave style</b> from {@link EngraveStyle} so the laser software
 *       treats it as engraving, not cutting. Never use {@code class='cut'}.</li>
 *   <li><b>No terminal input or output.</b> Asking the user questions belongs in an
 *       {@link EngravingOption}. Keeping this class free of {@code Scanner} and
 *       {@code System.out} is what makes it easy to unit-test.</li>
 *   <li>Return an empty string if there is nothing to draw (e.g. an all-white image).</li>
 * </ol>
 *
 * <p>To add a new kind of engraving, see "Adding an engraving type" in the README.
 */
public interface Engraving {

    /**
     * Draws this engraving so it fits entirely inside {@code area}.
     *
     * @param area the rectangle (in SVG units, i.e. mm) this engraving may use
     * @return SVG markup, or an empty string if there is nothing to engrave
     */
    String toSvg(Bounds area);

    /** A short human-readable description, shown in the terminal summary. */
    String describe();
}
