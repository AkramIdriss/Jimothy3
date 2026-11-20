package engraving;

import java.util.List;

/**
 * The terminal menu that lets the user add any number of engravings to a box's panels.
 *
 * <pre>
 *   Add an engraving?
 *     1) Text
 *     2) Logo from an SVG file
 *     3) Picture (PNG / JPG)
 *     0) No engraving
 * </pre>
 */
public final class EngravingMenu {

    private EngravingMenu() { }

    /**
     * Every kind of engraving the user can choose, in menu order.
     * <b>To add a new engraving type, add its option here</b> (nothing else needs to change).
     */
    public static final List<EngravingOption> OPTIONS = List.of(
            new TextOption(),
            new SvgLogoOption(),
            new ImageOption()
    );

    /**
     * Runs the menu until the user chooses "Done".
     *
     * @param in     where to ask questions
     * @param panels the panels of this box that can be engraved (first one is the default)
     * @return the engravings the user chose (empty if none)
     */
    public static EngravingPlan ask(Prompter in, List<String> panels) {
        EngravingPlan plan = EngravingPlan.none();
        while (true) {
            in.say("");
            in.say(plan.isEmpty() ? "Add an engraving?" : "Add another engraving?");
            for (int i = 0; i < OPTIONS.size(); i++) {
                in.say("  " + (i + 1) + ") " + OPTIONS.get(i).label());
            }
            in.say("  0) " + (plan.isEmpty() ? "No engraving" : "Done"));
            int choice = in.number("Choose", 0, OPTIONS.size(), 0);
            if (choice == 0) {
                return plan;
            }

            String panel = choosePanel(in, panels);
            try {
                Engraving engraving = OPTIONS.get(choice - 1).prompt(in);
                if (engraving == null) {
                    in.say("Cancelled.");
                } else {
                    plan.add(panel, engraving);
                    in.say("Added to " + panel + ": " + engraving.describe());
                }
            } catch (Exception e) {
                in.say("Could not add that engraving: " + e.getMessage());
            }
        }
    }

    private static String choosePanel(Prompter in, List<String> panels) {
        if (panels.size() == 1) {
            return panels.get(0);
        }
        in.say("Which panel?");
        for (int i = 0; i < panels.size(); i++) {
            in.say("  " + (i + 1) + ") " + panels.get(i));
        }
        return panels.get(in.number("Choose", 1, panels.size(), 1) - 1);
    }
}
