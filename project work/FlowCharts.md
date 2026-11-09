<!--

FlowCharts.md
How each box goes from the user's input to an SVG file

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

-->

# Flow Charts

> **Note:** GitHub draws these charts automatically. In other editors, paste them into [mermaid.live](https://mermaid.live) to see them.

## 1. Overall Flow

Every box runs from the terminal in one of two ways: everything typed on one line, or answering questions when run with no arguments. Both end the same way.

```mermaid
flowchart TD
    A([Start]) --> B[/"User runs SimpleBox, SlidingBox, or NoteHolderBox"/]
    B --> C{"Were arguments typed on the command line?"}
    C -- Yes --> D["Read the one-line options (chart 2)"]
    C -- No --> E[/"Ask for measurements or a preset style"/]
    E --> F{"Is the input valid?"}
    F -- No --> G["Show an error, or use the default style for the Note Holder"]
    F -- Yes --> H["Run the engraving menu (chart 3)"]
    D --> I["Build each panel's cut path with finger joints"]
    H --> I
    I --> J["Add a label and any engravings to each panel"]
    J --> K["Save the SVG file"]
    K --> L{"Did the save work?"}
    L -- No --> M[/"Print the save error"/]
    L -- Yes --> N[/"Print the file name, box details, and engravings"/]
    N --> O{"Is the preview on?"}
    O -- Yes --> P[/"Draw the terminal preview (chart 7)"/]
    O -- No --> Q([End])
    P --> Q
    M --> Q
    G --> Q
```

The Note Holder is the one exception to the error path: in question mode, an invalid style falls back to the sticky note holder and the program carries on.

## 2. One-Line Options (`CommandLine.java`)

Example: `java -cp target/classes SimpleBox 100 60 80 5 --text "Sarah's|Recipes" --font serif`

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

Used when a box is run with no arguments. Pressing Enter always takes the default.

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
    A([Start]) --> B{"Were arguments typed?"}
    B -- Yes --> C["Read width, height, depth, tooth width, and options"]
    C --> D{"Is everything valid?"}
    D -- No --> E[/"Print the error and usage, then exit"/]
    B -- No --> F[/"Enter width, height, depth, and tooth width in mm"/]
    F --> G{"Are all values greater than 0?"}
    G -- No --> H[/"Error: All values must be positive!"/]
    G -- Yes --> I["Run the engraving menu"]
    D -- Yes --> J["generateBoxSVG: work out where each panel goes on the page"]
    I --> J
    J --> K["Write the SVG header and styles"]
    K --> L["Front panel: makeFrontPanelPath"]
    L --> M["Right and left sides: makeSidePanelPath"]
    M --> N["Bottom panel: makeBottomPanelPath"]
    N --> O["Top panel: makeTopPanelPath"]
    O --> P["Back panel: makeFrontPanelPath"]
    P --> Q["Engravings are added right after each panel, except the slotted bottom"]
    Q --> R["Save as box_WxHxD_tT.svg"]
    R --> S[/"Print the file name and engravings, then the preview"/]
    S --> Z([End])
    E --> Z
    H --> Z
```

## 5. Sliding Box (`SlidingBox.java`)

```mermaid
flowchart TD
    A([Start]) --> B{"Were arguments typed?"}
    B -- Yes --> C["Read width, height, depth, tooth width, and options"]
    C --> D{"Is everything valid?"}
    D -- No --> E[/"Print the error and usage, then exit"/]
    B -- No --> F[/"Enter outer width, height, depth, and tooth width in mm"/]
    F --> G{"Are all values greater than 0?"}
    G -- No --> H[/"Error: All values must be positive!"/]
    G -- Yes --> I["Run the engraving menu"]
    D -- Yes --> J["Work out the inner drawer size, assuming 3 mm material"]
    I --> J
    J --> K["Outer box: front, side, back, top, and bottom panels"]
    K --> L["Inner drawer: front, left, back, right, and bottom panels, no top"]
    L --> M["The drawer's right side gets the half-circle handle"]
    M --> N["Engravings are added right after each panel, except the handle panel"]
    N --> O["Save as SlidingBox_WxDxH.svg"]
    O --> P[/"Print the outer and inner box sizes and engravings, then the preview"/]
    P --> Z([End])
    E --> Z
    H --> Z
```

## 6. Note Holder (`NoteHolderBox.java`)

```mermaid
flowchart TD
    A([Start]) --> B{"Were arguments typed?"}
    B -- Yes --> C["Read the style, sticky or index, and options"]
    C --> D{"Is everything valid?"}
    D -- No --> E[/"Print the error and usage, then exit"/]
    B -- No --> F[/"Pick a style: 1 for sticky notes, 2 for index cards"/]
    F --> G{"Is the style 1 or 2?"}
    G -- No --> H["Use the sticky note holder as the default"]
    G -- Yes --> I["Run the engraving menu"]
    H --> I
    D -- Yes --> J{"Which style?"}
    I --> J
    J -- "Sticky notes" --> K["86 x 86 x 60 mm, 5.8 mm teeth"]
    J -- "Index cards" --> L["138 x 86 x 60 mm, 5.8 mm teeth"]
    K --> M["Build the front, back, side, and bottom panels"]
    L --> M
    M --> N["Engravings are added to the bottom, back, or side panels"]
    N --> O["Save as StickyNoteHolder or IndexcardHolder SVG"]
    O --> P[/"Print the file name, dimensions, and engravings, then the preview"/]
    P --> Z([End])
    E --> Z
```

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
