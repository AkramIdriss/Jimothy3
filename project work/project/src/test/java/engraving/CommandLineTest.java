package engraving;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Test;

public class CommandLineTest {

    private static final List<String> PANELS = List.of("top", "right side");

    private static CommandLine parse(String... args) throws IOException {
        return CommandLine.parse(args, PANELS);
    }

    private interface Action {
        void run() throws Exception;
    }

    /** Runs {@code action}, which must throw {@code type}, and returns what it threw. */
    private static <T extends Exception> T assertThrows(Class<T> type, Action action) {
        try {
            action.run();
        } catch (Exception e) {
            assertTrue("Wrong exception: " + e, type.isInstance(e));
            return type.cast(e);
        }
        fail("Expected " + type.getSimpleName());
        return null;
    }

    private static String errorFrom(String... args) {
        return assertThrows(IllegalArgumentException.class, () -> parse(args)).getMessage();
    }

    @Test
    public void measurementsAloneMeanNoEngravingAndAPreview() throws IOException {
        CommandLine line = parse("100", "60", "80", "5");
        assertArrayEquals(new double[] {100, 60, 80, 5},
                line.positiveNumbers("width", "height", "depth", "tooth"), 0);
        assertTrue(line.plan().isEmpty());
        assertTrue(line.preview());
    }

    @Test
    public void barInTextStartsANewLineAndDefaultPanelIsTheFirst() throws IOException {
        TextEngraving t = (TextEngraving) parse("--text", "Sarah's|Recipes").plan().get("top").get(0);
        assertEquals(List.of("Sarah's", "Recipes"), t.lines());
        assertEquals("SansSerif", t.font().getFamily());
    }

    @Test
    public void fontAndPanelApplyToTheEngravingBeforeThem() throws IOException {
        EngravingPlan plan = parse("--text", "A", "--font", "serif", "--on", "right-side",
                "--text", "B").plan();
        TextEngraving a = (TextEngraving) plan.get("right side").get(0);
        TextEngraving b = (TextEngraving) plan.get("top").get(0);
        assertEquals("Serif", a.font().getFamily());
        assertEquals("SansSerif", b.font().getFamily());
    }

    @Test
    public void logoIsLoadedFromAFile() throws IOException {
        Path file = Files.createTempFile("logo", ".svg");
        try {
            Files.writeString(file, "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1 1'><rect width='1' height='1'/></svg>");
            assertEquals(1, parse("--logo", file.toString(), "--on", "TOP").plan().get("top").size());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void flagsAreRecognised() throws IOException {
        assertFalse(parse("--no-preview").preview());
        assertTrue(parse("--help").help());
        assertTrue(parse("-h").help());
    }

    @Test
    public void helpDoesNotTryToReadFiles() throws IOException {
        assertTrue(parse("--logo", "/no/such/file.svg", "--help").help());
    }

    @Test
    public void mistakesGetClearMessages() {
        assertTrue(errorFrom("--text").contains("needs a value"));
        assertTrue(errorFrom("--oops").contains("Unknown option"));
        assertTrue(errorFrom("--on", "top").contains("must come after"));
        assertTrue(errorFrom("--text", "A", "--on", "lid").contains("Unknown panel"));
        assertTrue(errorFrom("--font", "serif").contains("must come after"));
    }

    @Test
    public void fontOnlyAppliesToText() throws IOException {
        Path file = Files.createTempFile("logo", ".svg");
        try {
            assertTrue(errorFrom("--logo", file.toString(), "--font", "serif").contains("only applies to --text"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void missingFilesAndFontsAreIoErrors() {
        assertThrows(IOException.class, () -> parse("--logo", "/no/such/logo.svg"));
        assertThrows(IOException.class, () -> parse("--picture", "/no/such/pic.png"));
        assertThrows(IOException.class, () -> parse("--text", "A", "--font", "nosuchfontxyz"));
    }

    @Test
    public void numbersAreValidated() throws IOException {
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> parse("1", "2").positiveNumbers("a", "b", "c")).getMessage().contains("Expected 3"));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> parse("x").positiveNumbers("width")).getMessage().contains("width must be a number"));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> parse("-5").positiveNumbers("width")).getMessage().contains("width must be positive"));
    }
}
