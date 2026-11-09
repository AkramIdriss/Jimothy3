package engraving;

import java.nio.file.Path;

/** Menu option: engrave a logo from an .svg file. */
public class SvgLogoOption implements EngravingOption {

    @Override
    public String label() {
        return "Logo from an SVG file (sharpest; best for one-colour logos)";
    }

    @Override
    public Engraving prompt(Prompter in) throws Exception {
        Path file = in.path("SVG file path (you can drag the file here), or Enter to cancel: ");
        return file == null ? null : SvgLogoEngraving.fromFile(file);
    }
}
