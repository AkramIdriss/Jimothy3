package engraving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Which engravings go on which panel of a box.
 *
 * <p>Panels are identified by name (e.g. {@code "top"}); each box class lists the names it
 * supports (see e.g. {@code SimpleBox.ENGRAVABLE_PANELS}). When drawing a panel, the box calls
 * {@link #render(String, Bounds)} with the panel's rectangle and gets back the SVG markup.
 */
public class EngravingPlan {

    private final Map<String, List<Engraving>> byPanel = new LinkedHashMap<>();

    /** An empty plan: nothing gets engraved. */
    public static EngravingPlan none() {
        return new EngravingPlan();
    }

    /**
     * The classic "type a name" engraving: uppercased text in the default font on each of the
     * given panels. A null or blank {@code text} gives an empty plan. Used by the older
     * {@code String monogram} constructors/methods (and the GUI).
     */
    public static EngravingPlan fromText(String text, String... panels) {
        EngravingPlan plan = new EngravingPlan();
        if (text == null || text.isBlank()) {
            return plan;
        }
        TextEngraving engraving = new TextEngraving(List.of(text.trim().toUpperCase()), Fonts.defaultFont());
        for (String panel : panels) {
            plan.add(panel, engraving);
        }
        return plan;
    }

    /** Adds an engraving to a panel. Several engravings on one panel are stacked top to bottom. */
    public EngravingPlan add(String panel, Engraving engraving) {
        byPanel.computeIfAbsent(panel, p -> new ArrayList<>()).add(engraving);
        return this;
    }

    /** The engravings on a panel, in the order they were added (empty if none). */
    public List<Engraving> get(String panel) {
        return Collections.unmodifiableList(byPanel.getOrDefault(panel, List.of()));
    }

    public boolean isEmpty() {
        return byPanel.isEmpty();
    }

    /**
     * SVG markup for every engraving on {@code panel}, drawn inside {@code panelArea}.
     * Keeps {@link EngraveStyle#PANEL_MARGIN_MM} clear around the edge; several engravings share
     * the space as equal rows. Returns "" if the panel has no engravings.
     */
    public String render(String panel, Bounds panelArea) {
        List<Engraving> engravings = get(panel);
        if (engravings.isEmpty()) {
            return "";
        }
        Bounds usable = panelArea.inset(EngraveStyle.PANEL_MARGIN_MM).scaled(EngraveStyle.FILL_FRACTION);
        Bounds[] rows = usable.rows(engravings.size(), EngraveStyle.STACK_GAP_MM);

        StringBuilder svg = new StringBuilder();
        for (int i = 0; i < engravings.size(); i++) {
            svg.append(engravings.get(i).toSvg(rows[i]));
        }
        return svg.toString();
    }

    /** One line per engraving, e.g. {@code "top: Text \"JDE\" (SansSerif)"}, for terminal output. */
    public List<String> summary() {
        List<String> lines = new ArrayList<>();
        byPanel.forEach((panel, list) -> list.forEach(e -> lines.add(panel + ": " + e.describe())));
        return lines;
    }
}
