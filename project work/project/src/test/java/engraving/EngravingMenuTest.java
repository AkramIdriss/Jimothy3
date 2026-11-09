package engraving;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import org.junit.Test;

/** Drives the terminal menu with scripted answers, exactly as a user would type them. */
public class EngravingMenuTest {

    private static final List<String> PANELS = List.of("top", "front", "back");

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private EngravingPlan run(String... answers) {
        Scanner in = new Scanner(String.join("\n", answers) + "\n");
        return EngravingMenu.ask(new Prompter(in, new PrintStream(output, true, StandardCharsets.UTF_8)), PANELS);
    }

    private String printed() {
        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void choosingNothingGivesAnEmptyPlan() {
        assertTrue(run("0").isEmpty());
        assertTrue(run("").isEmpty());     // just pressing Enter
    }

    @Test
    public void runningOutOfInputNeverHangs() {
        assertTrue(run().isEmpty());
        assertTrue(run("1").isEmpty());    // picked Text, then input ended
    }

    @Test
    public void addsMultiLineTextToTheChosenPanel() {
        EngravingPlan plan = run(
                "1",                 // Text
                "3",                 // back panel
                "Sarah's", "Recipes", "",
                "y",                 // uppercase
                "1",                 // serif
                "0");                // done
        TextEngraving text = (TextEngraving) plan.get("back").get(0);
        assertEquals(List.of("SARAH'S", "RECIPES"), text.lines());
        assertEquals("Serif", text.font().getFamily());
        assertTrue(plan.get("top").isEmpty());
        assertFalse("Bold is no longer asked", printed().contains("Bold?"));
    }

    @Test
    public void invalidAnswersAreAskedAgain() {
        EngravingPlan plan = run("9", "abc", "1", "7", "1", "Hi", "", "maybe", "", "", "0");
        assertEquals(1, plan.get("top").size());
        assertTrue(printed().contains("Please enter a number from 0 to"));
        assertTrue(printed().contains("Please answer y or n."));
    }

    @Test
    public void aMissingFileIsReportedAndTheMenuContinues() {
        EngravingPlan plan = run("2", "1", "/no/such/logo.svg", "0");
        assertTrue(plan.isEmpty());
        assertTrue(printed().contains("Could not add that engraving: SVG file not found"));
    }

    @Test
    public void pressingEnterForAFileCancels() {
        assertTrue(run("3", "1", "", "0").isEmpty());
        assertTrue(printed().contains("Cancelled."));
    }

    /** Paths dragged into a terminal come quoted or with escaped spaces. */
    @Test
    public void draggedPathsAreCleanedUp() {
        Prompter p = new Prompter(new Scanner("'/a b/c.svg'\n/a\\ b/c.svg\n~/x.png\n"),
                new PrintStream(output, true, StandardCharsets.UTF_8));
        assertEquals(Path.of("/a b/c.svg"), p.path("?"));
        assertEquals(Path.of("/a b/c.svg"), p.path("?"));
        assertEquals(Path.of(System.getProperty("user.home"), "x.png"), p.path("?"));
    }
}
