package engraving;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The "everything on one line" way of running a box, for example:
 * <pre>
 *   java -cp target/classes SimpleBox 100 60 80 5 --text "Sarah's|Recipes" --font serif --logo logo.svg --on back
 * </pre>
 * Plain values (like the measurements) are collected in {@link #positional()}. Each
 * {@code --text}, {@code --logo} or {@code --picture} adds an engraving; a {@code --font} or
 * {@code --on} after it applies to that engraving.
 */
public record CommandLine(List<String> positional, EngravingPlan plan, boolean preview, boolean help) {

    private static final class Spec {
        final String kind;
        final String value;
        String panel;
        String font;

        Spec(String kind, String value) {
            this.kind = kind;
            this.value = value;
        }
    }

    /**
     * @param panels the panels this box can engrave; the first is used when there is no {@code --on}
     * @throws IllegalArgumentException if the options make no sense
     * @throws IOException if a logo, picture or font can't be read
     */
    public static CommandLine parse(String[] args, List<String> panels) throws IOException {
        List<String> positional = new ArrayList<>();
        List<Spec> specs = new ArrayList<>();
        boolean preview = true;
        boolean help = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help", "-h" -> help = true;
                case "--no-preview" -> preview = false;
                case "--text", "--logo", "--picture" -> specs.add(new Spec(arg, value(args, ++i, arg)));
                case "--on" -> last(specs, arg).panel = resolvePanel(value(args, ++i, arg), panels);
                case "--font" -> {
                    Spec spec = last(specs, arg);
                    if (!spec.kind.equals("--text")) {
                        throw new IllegalArgumentException("--font only applies to --text");
                    }
                    spec.font = value(args, ++i, arg);
                }
                default -> {
                    if (arg.startsWith("--")) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
                    positional.add(arg);
                }
            }
        }

        EngravingPlan plan = EngravingPlan.none();
        if (!help) {
            for (Spec spec : specs) {
                plan.add(spec.panel != null ? spec.panel : panels.get(0), build(spec));
            }
        }
        return new CommandLine(positional, plan, preview, help);
    }

    /** Reads the plain values as positive numbers; {@code names} says how many are expected and what they are. */
    public double[] positiveNumbers(String... names) {
        if (positional.size() != names.length) {
            throw new IllegalArgumentException("Expected " + names.length + " values (" + String.join(", ", names)
                    + ") but got " + positional.size());
        }
        double[] numbers = new double[names.length];
        for (int i = 0; i < names.length; i++) {
            try {
                numbers[i] = Double.parseDouble(positional.get(i));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(names[i] + " must be a number, got: " + positional.get(i));
            }
            if (numbers[i] <= 0) {
                throw new IllegalArgumentException(names[i] + " must be positive, got: " + positional.get(i));
            }
        }
        return numbers;
    }

    /** The shared part of every box's --help text. */
    public static String optionsHelp(List<String> panels) {
        return """
                Engraving options (repeat as often as you like):
                  --text "Line 1|Line 2"   engrave text; | starts a new line
                    --font NAME            serif, sans, mono, an installed font, or a .ttf/.otf file
                  --logo FILE.svg          engrave an SVG logo
                  --picture FILE.png       engrave a picture (PNG, JPG, GIF, BMP) in black and white
                  --on PANEL               which panel the engraving before it goes on
                Panels: %s (default: %s)
                Other:
                  --no-preview             don't draw the preview in the terminal
                  --help                   show this help
                Run with no arguments to be asked questions instead."""
                .formatted(String.join(", ", panels.stream().map(p -> p.replace(' ', '-')).toList()),
                        panels.get(0).replace(' ', '-'));
    }

    private static String value(String[] args, int i, String option) {
        if (i >= args.length || args[i].startsWith("--")) {
            throw new IllegalArgumentException(option + " needs a value after it");
        }
        return args[i];
    }

    private static Spec last(List<Spec> specs, String option) {
        if (specs.isEmpty()) {
            throw new IllegalArgumentException(option + " must come after --text, --logo or --picture");
        }
        return specs.get(specs.size() - 1);
    }

    private static String resolvePanel(String typed, List<String> panels) {
        String wanted = typed.toLowerCase(Locale.ROOT).replace('-', ' ').replace('_', ' ').trim();
        if (panels.contains(wanted)) {
            return wanted;
        }
        throw new IllegalArgumentException("Unknown panel: " + typed + " (choose from "
                + String.join(", ", panels.stream().map(p -> p.replace(' ', '-')).toList()) + ")");
    }

    private static Engraving build(Spec spec) throws IOException {
        return switch (spec.kind) {
            case "--text" -> new TextEngraving(List.of(spec.value.split("\\|")),
                    spec.font != null ? Fonts.find(spec.font) : Fonts.defaultFont());
            case "--logo" -> SvgLogoEngraving.fromFile(Path.of(spec.value));
            default -> ImageEngraving.fromFile(Path.of(spec.value), ImageEngraving.DEFAULT_THRESHOLD, false);
        };
    }
}
