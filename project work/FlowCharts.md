<!--

FlowCharts.md
How each box goes from the user's input to an SVG file

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

-->

# Flow Charts

> **Note:** GitHub draws these charts automatically. In other editors, paste them into [mermaid.live](https://mermaid.live) to see them.

## 1. Overall Flow (`MainApp.java`)

The app runs from the terminal in one of two ways: everything typed on one line, or answering questions when run with no arguments. Both end the same way. The files are saved in the user's Downloads folder.

```mermaid
flowchart TD
    A([Start]) --> B[/"User runs MainApp"/]
    B --> C{"Were arguments typed on the command line?"}
    C -- Yes --> D["Read the box type, measurements, and options (chart 2)"]
    C -- No --> E[/"Pick a box: simple, note holder, sliding, crate, hinge, or flex"/]
    E --> F{"Which box?"}
    F -- "Not a box" --> G[/"That is not an applicable response"/]
    F -- "Simple, sliding, crate, hinge, or flex" --> H[/"Enter length, width, height, and tooth width; add the word engraving to engrave"/]
    F -- "Note holder" --> I[/"Ask whether to engrave, then the style"/]
    H --> J{"Did they ask to engrave?"}
    I --> J
    J -- Yes --> K["Run the engraving menu (chart 3)"]
    J -- No --> L["Build the box's SVG (charts 4 to 6)"]
    K --> L
    D --> L
    L --> M[/"Ask for a file name, or make one up"/]
    M --> N["Save the SVG in Downloads"]
    N --> O{"Did the save work?"}
    O -- No --> P[/"Print the save error"/]
    O -- Yes --> Q[/"Print where it was saved and the engravings"/]
    Q --> R{"Is the preview on?"}
    R -- Yes --> S[/"Draw the terminal preview (chart 7)"/]
    R -- No --> Z([End])
    S --> Z
    P --> Z
    G --> Z
```

Crate, hinge, and flex boxes aren't built yet, so for now they make a simple box. In one-line mode the file name is made up from the box and its size, so there is no file name question.

## 2. One-Line Options (`CommandLine.java`)

Example: `java -cp target/classes jimothypackage.MainApp simple 100 80 60 5 --text "Sarah's|Recipes" --font serif`

The first argument picks the box: `simple`, `sliding`, or `note`. The rest are read like this:

```mermaid
flowchart TD
    A([Start]) --> B["Go through the arguments from left to right"]
    B --> C{"What is this argument?"}
    C -- "A plain value" --> D["Keep it as a measurement or style"]
    C -- "Text, logo, or picture option" --> E["Start a new engraving"]
    C -- "Font or panel option" --> F{"Is there an engraving before it?"}
    F -- Yes --> G["Apply it to that engraving"]
    F -- No --> X[/"Print the error and usage, then exit"/]
    C -- "Help or no-preview option" --> H["Remember it"]
    C -- "Unknown option" --> X
    D --> I{"Any arguments left?"}
    E --> I
    G --> I
    H --> I
    I -- Yes --> C
    I -- No --> J{"Was help asked for?"}
    J -- Yes --> K[/"Print the usage"/]
    J -- No --> L["Load the fonts and files and build each engraving"]
    L --> M{"Did everything load?"}
    M -- No --> X
    M -- Yes --> N{"Are the measurements or style valid?"}
    N -- No --> X
    N -- Yes --> O["Build and save the box"]
    K --> Z([End])
    O --> Z
    X --> Z
```

## 3. Engraving Menu (`EngravingMenu.java`)

Used in question mode when the user asks to engrave. Pressing Enter always takes the default.

```mermaid
flowchart TD
    A([Start]) --> B[/"Show the menu: Text, SVG logo, Picture, or Done"/]
    B --> C{"What did the user choose?"}
    C -- "Done" --> Z([Return the engravings])
    C -- "An engraving type" --> D[/"Ask which panel"/]
    D --> E{"Which type?"}
    E -- Text --> F[/"Ask for lines, uppercase, and font"/]
    E -- "SVG logo" --> G[/"Ask for the file path"/]
    E -- Picture --> H[/"Ask for the file path, darkness cut-off, and invert"/]
    H --> I[/"Show the picture preview and ask to use it"/]
    F --> J{"What happened?"}
    G --> J
    I --> J
    J -- "Built an engraving" --> K["Add it to the chosen panel"]
    J -- Cancelled --> L[/"Print Cancelled"/]
    J -- "File or font problem" --> M[/"Print the problem"/]
    K --> B
    L --> B
    M --> B
```

## 4. Simple Box (`SimpleBox.java`)

```mermaid
flowchart TD
    A([Start: generateBoxSVG]) --> B{"Are all values greater than 0?"}
    B -- No --> C[/"Error: All values must be positive! and return an empty SVG"/]
    B -- Yes --> D["Work out where each panel goes on the page"]
    D --> E["Write the SVG header and styles"]
    E --> F["Front panel: makeFrontPanelPath"]
    F --> G["Right and left sides: makeSidePanelPath"]
    G --> H["Bottom panel: makeBottomPanelPath"]
    H --> I["Top panel: makeTopPanelPath"]
    I --> J["Back panel: makeFrontPanelPath"]
    J --> K["Engravings are added right after each panel, except the slotted bottom"]
    K --> Z([Return the SVG])
    C --> Z
```

## 5. Sliding Box (`SlidingBox.java`)

```mermaid
flowchart TD
    A([Start: generateBoxSVG]) --> B{"Are all values greater than 0?"}
    B -- No --> C[/"Error: All values must be positive! and return an empty SVG"/]
    B -- Yes --> D["Work out the inner drawer size, assuming 3 mm material"]
    D --> E["Outer box: front, side, back, top, and bottom panels"]
    E --> F["Inner drawer: front, left, back, right, and bottom panels, no top"]
    F --> G["The drawer's right side gets the half-circle handle"]
    G --> H["Engravings are added right after each panel, except the handle panel"]
    H --> I["Add the outer and inner box sizes as text"]
    I --> Z([Return the SVG])
    C --> Z
```

## 6. Note Holder (`NoteHolderBox.java`)

```mermaid
flowchart TD
    A([Start: generateBoxSVG]) --> B{"Which style?"}
    B -- "1: Sticky notes" --> C["86 x 86 x 60 mm, 5.8 mm teeth"]
    B -- "2: Index cards" --> D["138 x 86 x 60 mm, 5.8 mm teeth"]
    B -- "Anything else" --> C
    C --> E["Build the two front, back, two side, and bottom panels"]
    D --> E
    E --> F["Engravings are added to the bottom, back, or side panels"]
    F --> Z([Return the SVG])
```

`MainApp` only accepts style 1 or 2 (or the words sticky, note, index, card); anything else prints an error.

## 7. Terminal Preview (`SvgAsciiPreview.java`)

The preview reads the finished SVG, so it shows what is really in the file and works for any box.

```mermaid
flowchart TD
    A([Start]) --> B["Read the shapes and labels from the SVG"]
    B --> C["Fit the sheet to the terminal width"]
    C --> D["Smooth away the finger joints and square up the edges"]
    D --> E["Draw the cut lines, with small slots as dots"]
    E --> F["Draw pictures and logos as hashtag shapes"]
    F --> G["Write the panel labels"]
    G --> H["Write engraved text as readable words"]
    H --> I[/"Print the sheet"/]
    I --> J{"Is there a picture or logo?"}
    J -- Yes --> K[/"Print a close-up of it for each panel"/]
    J -- No --> Z([End])
    K --> Z
```
