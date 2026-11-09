package engraving;

import java.awt.Font;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Menu option: engrave one or more lines of text in a chosen font. */
public class TextOption implements EngravingOption {

    /** Most installed fonts to list for one search, so the list fits on screen. */
    private static final int MAX_FONT_RESULTS = 20;

    @Override
    public String label() {
        return "Text";
    }

    @Override
    public Engraving prompt(Prompter in) throws Exception {
        in.say("Type the text to engrave, one line at a time. Press Enter on an empty line to finish.");
        List<String> lines = new ArrayList<>();
        while (true) {
            String line = in.line("  line " + (lines.size() + 1) + ": ");
            if (line.isEmpty()) {
                break;
            }
            lines.add(line);
        }
        if (lines.isEmpty()) {
            return null;
        }

        if (in.yesNo("Make it UPPERCASE?", false)) {
            lines.replaceAll(s -> s.toUpperCase(Locale.ROOT));
        }
        return new TextEngraving(lines, chooseFont(in));
    }

    private Font chooseFont(Prompter in) throws Exception {
        in.say("Font:");
        in.say("  1) Serif (like Times New Roman)");
        in.say("  2) Sans-serif (like Arial)");
        in.say("  3) Monospaced (like Courier)");
        in.say("  4) Search the fonts installed on this computer");
        in.say("  5) Load a font file (.ttf or .otf)");
        int choice = in.number("Choose", 1, 5, 2);

        switch (choice) {
            case 1: return Fonts.named(Font.SERIF);
            case 3: return Fonts.named(Font.MONOSPACED);
            case 4: return searchInstalled(in);
            case 5:
                Path file = in.path("Font file path (you can drag the file here): ");
                return file == null ? Fonts.defaultFont() : Fonts.fromFile(file);
            default: return Fonts.defaultFont();
        }
    }

    private Font searchInstalled(Prompter in) {
        while (true) {
            String query = in.line("Part of the font name (e.g. \"garamond\"), or Enter for sans-serif: ");
            if (query.isEmpty()) {
                return Fonts.defaultFont();
            }
            List<String> matches = Fonts.installedMatching(query, MAX_FONT_RESULTS);
            if (matches.isEmpty()) {
                in.say("  No installed font matches \"" + query + "\". Try another name.");
                continue;
            }
            for (int i = 0; i < matches.size(); i++) {
                in.say("  " + (i + 1) + ") " + matches.get(i));
            }
            int pick = in.number("Choose", 1, matches.size(), 1);
            return Fonts.named(matches.get(pick - 1));
        }
    }
}
