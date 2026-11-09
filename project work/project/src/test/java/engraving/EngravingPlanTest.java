package engraving;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.List;
import org.junit.Test;

public class EngravingPlanTest {

    @Test
    public void blankOrNullTextMeansNoEngraving() {
        assertTrue(EngravingPlan.fromText(null, "top").isEmpty());
        assertTrue(EngravingPlan.fromText("   ", "top").isEmpty());
    }

    /** The original monogram behaviour: trimmed, uppercased, same text on each named panel. */
    @Test
    public void fromTextUppercasesAndUsesEveryPanel() {
        EngravingPlan plan = EngravingPlan.fromText("  jde ", "top", "back");
        assertEquals(List.of("JDE"), ((TextEngraving) plan.get("top").get(0)).lines());
        assertEquals(1, plan.get("back").size());
        assertEquals(0, plan.get("front").size());
    }

    @Test
    public void panelsWithoutEngravingsRenderNothing() {
        assertEquals("", EngravingPlan.fromText("A", "top").render("front", new Bounds(0, 0, 100, 100)));
    }

    /** Engravings keep clear of the panel edges, where the finger joints are. */
    @Test
    public void engravingsKeepAMarginFromTheEdges() {
        Bounds panel = new Bounds(0, 0, 100, 60);
        String svg = EngravingPlan.fromText("WIDE NAME HERE", "top").render("top", panel);
        SvgAssert.assertPathsInside(svg, panel.inset(EngraveStyle.PANEL_MARGIN_MM));
    }

    /** Two engravings on one panel are stacked: the second one is entirely below the first. */
    @Test
    public void severalEngravingsOnAPanelAreStacked() {
        TextEngraving first = new TextEngraving(List.of("FIRST"), Fonts.defaultFont());
        TextEngraving second = new TextEngraving(List.of("SECOND"), Fonts.defaultFont());
        EngravingPlan plan = EngravingPlan.none().add("top", first).add("top", second);

        String svg = plan.render("top", new Bounds(0, 0, 100, 100));
        String[] parts = svg.split("\n");
        assertEquals(2, parts.length);
        Rectangle2D a = SvgAssert.engravePathBounds(parts[0]);
        Rectangle2D b = SvgAssert.engravePathBounds(parts[1]);
        assertTrue(a.getMaxY() < b.getMinY());
    }

    @Test
    public void summaryListsPanelAndDescription() {
        EngravingPlan plan = EngravingPlan.fromText("abc", "top");
        assertEquals(List.of("top: Text \"ABC\" (SansSerif)"), plan.summary());
    }
}
