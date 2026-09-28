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

- Java JDK 17 or newer
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

## Project Structure

```
src/
+-- main/
|   +-- java/
|   |   +-- MainApp.java         # JavaFX GUI (start screen, box selection, save dialog)
|   |   +-- SimpleBox.java       # Simple box SVG generator
|   |   +-- SlidingBox.java      # Sliding box + drawer SVG generator
|   |   +-- NoteHolderBox.java   # Sticky note / index card holder SVG generator
|   +-- resources/UI/images/     # Logos and box preview images
+-- test/java/                   # JUnit tests for each box type
```
## Glossary

New to laser cutting? Check out the [Glossary](glossary.md) for terms like kerf, vector, and raster.

## Team

- Isaac England
- Sarah Rawls
- Akram Idriss
