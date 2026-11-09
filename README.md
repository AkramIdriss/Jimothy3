# Jimothy3

The repo for our team in **SE-1**, taught by the legendary **Dr. Reeves**!

Jimothy3 is a JavaFX desktop app that generates **laser-cut box templates** as SVG files.
Pick a box style, enter your measurements and an optional name to engrave, and the app
builds every panel with interlocking finger joints, then ready to send to a laser cutter.

## Box Styles

| Box | Description |
|-----|-------------|
| **Simple Box** | A basic finger-jointed box with a custom width, height, depth, and tooth width. It can have a monogram on the top panel. |
| **Sliding Box** | An outer box plus an inner drawer that slides out. The drawer is sized automatically from the outer box,
assuming 3 mm material. |
| **Note Holder** | A holder for sticky notes (86 mm wide) or index cards (138 mm wide). It can have text engraved on the
bottom panel. |

## Requirements

- Java JDK 17 through 25
- [Apache Maven](https://maven.apache.org/)

## Getting Started

Run these commands from the project root folder.

### Launch the app (GUI) (make sure you have the right versions in pom.xml)
```bash
mvn javafx:run
```

### Run the unit tests
```bash
mvn test
```

### Run a box generator from the terminal (no GUI)
Each box class has its own command-line version:
```bash
mvn compile
java -cp target/classes SimpleBox
java -cp target/classes SlidingBox
java -cp target/classes NoteHolderBox
```
The SVG file is saved in the folder you ran the command from. To preview it, open it in a web browser.

### Do it all on one line
Put the measurements and engravings straight on the command:
```bash
java -cp target/classes SimpleBox 100 60 80 5 --text "Sarah's|Recipes" --font serif
java -cp target/classes SlidingBox 100 60 80 5 --text "Pencils" --on outer-top --logo logo.svg --on inner-front
java -cp target/classes NoteHolderBox sticky --picture cat.png --on back
```

| Option | Meaning |
|--------|---------|
| `--text "Line 1\|Line 2"` | Engrave text; `\|` starts a new line |
| `--font NAME` | Font for the `--text` before it: `serif`, `sans`, `mono`, an installed font, or a `.ttf`/`.otf` file |
| `--logo FILE.svg` | Engrave an SVG logo |
| `--picture FILE.png` | Engrave a picture in black and white |
| `--on PANEL` | Panel for the engraving before it (`--help` lists each box's panels) |
| `--no-preview` | Skip the terminal preview |

Run a box with no arguments to be asked questions instead.

### Preview in the terminal
Both ways finish by drawing the sheet in the terminal: `- | +` are cut lines, `.` are small slots, words inside a
panel are the text that will be engraved there, and `#` is an engraved picture or logo (which also gets a close-up). It's an approximate view for checking the layout, so
open the SVG in a browser for the exact result.

### Engraving (terminal version)
After the measurements, the terminal asks what to engrave. Add as many engravings as you like, each on any panel:

| Option | What you give it | Notes |
|--------|------------------|-------|
| **Text** | Any number of lines, then a font: serif, sans-serif, monospaced, any font installed on your computer (search by name), or a `.ttf`/`.otf` file | Letters are converted to outlines, so the laser cutter's computer doesn't need the font |
| **SVG logo** | Path to an `.svg` file | Sharpest result. Every colour becomes the engrave colour, so use one-colour logos |
| **Picture** | Path to a PNG/JPG/GIF/BMP | Turned into pure black and white (you choose the darkness cut-off) and shown as an ASCII preview first |

You can drag a file into the terminal to type its path. Engravings are drawn in **red fill** (`#ff0000`)
and cut lines in thin black strokes, so the laser software can tell them apart. If your cutter needs a
different colour, change `ENGRAVE_COLOR` in `engraving/EngraveStyle.java`.

The GUI still uses the original single-name engraving.

## Project Structure

```
src/
+-- main/
|   +-- java/
|   |   +-- MainApp.java         # JavaFX GUI (start screen, box selection, save dialog)
|   |   +-- SimpleBox.java       # Simple box SVG generator
|   |   +-- SlidingBox.java      # Sliding box + drawer SVG generator
|   |   +-- NoteHolderBox.java   # Sticky note / index card holder SVG generator
|   |   +-- engraving/           # Text / logo / picture engravings shared by all boxes
|   +-- resources/UI/images/     # Logos and box preview images
+-- test/java/                   # JUnit tests for each box type
|   +-- engraving/               # Tests for the engraving package (incl. EngravingContractTest)
```

## Adding an engraving type

Everything about engravings lives in `src/main/java/engraving/`. A box only asks an engraving to
draw itself inside a rectangle, so **a new engraving type never needs changes to the box classes.**

| Class | Job |
|-------|-----|
| `Engraving` | The interface: `toSvg(Bounds area)` returns SVG that fits inside `area` |
| `EngravingOption` | One menu entry: asks the user questions in the terminal and builds an `Engraving` |
| `EngravingMenu` | The terminal menu; `OPTIONS` lists every option |
| `EngravingPlan` | Which engravings go on which panel; boxes call `render(panel, bounds)` |
| `EngraveStyle` | Shared colour, margins and spacing |
| `CommandLine` | Reads the one-line options like `--text` and `--on` into an `EngravingPlan` |
| `SvgAsciiPreview` | Draws a finished SVG in the terminal; it reads the SVG, so new boxes work automatically |
| `SvgUtil`, `Fonts`, `Prompter` | Helpers: shape-to-path and fitting, font loading, terminal questions |

To add one (for example a QR code):

1. **Drawing:** write `QrEngraving implements Engraving`. Draw only inside the area you are given, use
   `EngraveStyle.attributes()` (or `SvgUtil.fitAsPath(shape, area)` if you have a Java `Shape`), and
   don't read input or print anything.
2. **Questions:** write `QrOption implements EngravingOption`, using `Prompter` to ask questions.
3. **Menu:** add `new QrOption()` to `EngravingMenu.OPTIONS`.
4. **Tests:** add a sample `QrEngraving` to `EngravingContractTest.samples()`. That checks the output
   is valid SVG, uses the engrave style and stays inside the area. Then add tests specific to your type.

To make another **panel** engravable, add its name to the box's `ENGRAVABLE_PANELS` and call
`engravings.render(NAME, new Bounds(x, y, width, height))` right after the panel is drawn.
## Glossary

New to laser cutting? Check out the [Glossary](glossary.md) for terms like kerf, vector, and raster.

## Team

- Isaac England
- Sarah Rawls
- Akram Idriss
