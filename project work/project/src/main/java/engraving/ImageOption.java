package engraving;

import java.nio.file.Path;

/** Menu option: engrave a picture (PNG/JPG/...) converted to black and white. */
public class ImageOption implements EngravingOption {

    /** Width (characters) of the terminal preview. */
    private static final int PREVIEW_COLUMNS = 48;

    @Override
    public String label() {
        return "Picture (PNG / JPG), converted to black and white";
    }

    @Override
    public Engraving prompt(Prompter in) throws Exception {
        Path file = in.path("Image file path (you can drag the file here), or Enter to cancel: ");
        if (file == null) {
            return null;
        }
        int threshold = in.number("Darkness cut-off, 0-255 (higher engraves more)", 0, 255,
                ImageEngraving.DEFAULT_THRESHOLD);
        boolean invert = in.yesNo("Engrave the LIGHT parts instead of the dark parts?", false);

        ImageEngraving image = ImageEngraving.fromFile(file, threshold, invert);
        in.say("Preview ('#' = engraved):");
        in.say(image.asciiPreview(PREVIEW_COLUMNS));
        return in.yesNo("Use this image?", true) ? image : null;
    }
}
