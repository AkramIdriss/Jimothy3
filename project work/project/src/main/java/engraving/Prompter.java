package engraving;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;

/**
 * Asks the user questions in the terminal. Every question has a default, which is used when
 * the user just presses Enter (or when input runs out), so the program can never get stuck.
 *
 * <p>Tests can create one with scripted input:
 * {@code new Prompter(new Scanner("1\nHello\n\n"), new PrintStream(new ByteArrayOutputStream()))}.
 */
public class Prompter {

    private final Scanner in;
    private final PrintStream out;

    public Prompter(Scanner in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    /** Uses System.out and the given scanner (share the scanner the rest of the program uses). */
    public Prompter(Scanner in) {
        this(in, System.out);
    }

    public void say(String text) {
        out.println(text);
    }

    /** Asks a question and returns the trimmed answer ("" if the user pressed Enter or input ended). */
    public String line(String question) {
        out.print(question);
        out.flush();
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }

    /** Asks for a whole number between min and max (inclusive); Enter gives {@code def}. */
    public int number(String question, int min, int max, int def) {
        while (true) {
            String answer = line(question + " [" + def + "]: ");
            if (answer.isEmpty()) {
                return def;
            }
            try {
                int n = Integer.parseInt(answer);
                if (n >= min && n <= max) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the message below
            }
            say("  Please enter a number from " + min + " to " + max + ".");
        }
    }

    /** Asks a yes/no question; Enter gives {@code def}. */
    public boolean yesNo(String question, boolean def) {
        while (true) {
            String answer = line(question + (def ? " (Y/n): " : " (y/N): ")).toLowerCase(Locale.ROOT);
            if (answer.isEmpty()) {
                return def;
            }
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            say("  Please answer y or n.");
        }
    }

    /**
     * Asks for a file path. Accepts paths dragged into the terminal: surrounding quotes and
     * backslash-escaped spaces are removed, and a leading ~ means the home folder.
     * Returns null if the user pressed Enter.
     */
    public Path path(String question) {
        String p = line(question);
        if (p.isEmpty()) {
            return null;
        }
        if (p.length() >= 2 && (p.startsWith("\"") && p.endsWith("\"") || p.startsWith("'") && p.endsWith("'"))) {
            p = p.substring(1, p.length() - 1);
        }
        p = p.replace("\\ ", " ");
        if (p.equals("~") || p.startsWith("~/")) {
            p = System.getProperty("user.home") + p.substring(1);
        }
        return Path.of(p);
    }
}
