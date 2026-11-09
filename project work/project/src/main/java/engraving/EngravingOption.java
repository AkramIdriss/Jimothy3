package engraving;

/**
 * One entry in the terminal engraving menu: its label, and the questions that build the
 * {@link Engraving}.
 *
 * <p>Keep all terminal input/output here, and all drawing in the {@link Engraving} class.
 * To make a new option show up in the menu, add it to {@link EngravingMenu#OPTIONS}.
 */
public interface EngravingOption {

    /** Menu text, e.g. "Text". */
    String label();

    /**
     * Asks the user whatever this option needs and builds the engraving.
     *
     * @return the engraving, or null if the user cancelled (e.g. pressed Enter with no input)
     * @throws Exception with a user-friendly message if something went wrong (file not found,
     *                   unreadable image...); the menu shows the message and lets the user try again
     */
    Engraving prompt(Prompter in) throws Exception;
}
