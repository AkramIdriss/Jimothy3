/*

FlowCharts.md
How each box goes from the user's input to an SVG file

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/

# Flow Charts

> **Note:** These charts show how the boxes run from the terminal (no UI). GitHub draws them automatically. In other editors, paste them into [mermaid.live](https://mermaid.live) to see them.

## 1. Overall Flow

Every box follows the same steps: get input, check it, build the panels, add the engraving, and save the SVG.

```mermaid
flowchart TD
    A([Start]) --> B[/"User picks a box: Simple, Sliding, or Note Holder"/]
    B --> C[/"User enters measurements or a preset style"/]
    C --> D[/"User enters engraving text, or presses Enter to skip"/]
    D --> E{"Is the input valid?"}
    E -- No --> F["Show an error or use the default"]
    E -- Yes --> G["Build each panel's cut path with finger joints"]
    G --> H{"Was engraving text entered?"}
    H -- Yes --> I["Size the text to fit the panel and add it"]
    H -- No --> J["Add a label to each panel"]
    I --> J
    J --> K["Save the SVG file"]
    K --> L{"Did the save work?"}
    L -- Yes --> M[/"Print the file name and box details"/]
    L -- No --> N[/"Print the save error"/]
    M --> O([End])
    N --> O
    F --> O
```

## 2. Simple Box (`SimpleBox.java`)

```mermaid
flowchart TD
    A([Start]) --> B[/"Enter width, height, depth, and tooth width in mm"/]
    B --> C[/"Enter monogram, or press Enter to skip"/]
    C --> D{"Are all values greater than 0?"}
    D -- No --> E[/"Error: All values must be positive!"/]
    E --> Z([End])
    D -- Yes --> F["generateBoxSVG: work out where each panel goes on the page"]
    F --> G["Write the SVG header and styles"]
    G --> H["Front panel: makeFrontPanelPath"]
    H --> I["Right and left sides: makeSidePanelPath"]
    I --> J["Bottom panel: makeBottomPanelPath"]
    J --> K["Top panel: makeTopPanelPath"]
    K --> L{"Was a monogram entered?"}
    L -- Yes --> M["Scale the font and add the monogram to the top, plus a smaller one on the back"]
    L -- No --> N["Back panel: makeFrontPanelPath"]
    M --> N
    N --> O["Save as box_WxHxD_tT.svg"]
    O --> P[/"Print the file name and monogram"/]
    P --> Z
```

## 3. Sliding Box (`SlidingBox.java`)

```mermaid
flowchart TD
    A([Start]) --> B[/"Enter outer width, height, depth, and tooth width in mm"/]
    B --> C[/"Enter monogram, or press Enter to skip"/]
    C --> D{"Are all values greater than 0?"}
    D -- No --> E[/"Error: All values must be positive!"/]
    E --> Z([End])
    D -- Yes --> F["Work out the inner drawer size, assuming 3 mm material"]
    F --> G["Outer box: front, back, sides, top, and bottom panels"]
    G --> H["Inner drawer: front, back, left, right, and bottom panels, no top"]
    H --> I["Add the half-circle handle to the drawer's right side"]
    I --> J{"Was a monogram entered?"}
    J -- Yes --> K["Add the monogram to the outer top and inner bottom"]
    J -- No --> L["Save as SlidingBox_WxDxH.svg"]
    K --> L
    L --> M[/"Print the outer and inner box sizes"/]
    M --> Z
```

## 4. Note Holder (`NoteHolderBox.java`)

```mermaid
flowchart TD
    A([Start]) --> B[/"Pick a style: 1 for sticky notes, 2 for index cards"/]
    B --> C[/"Enter engraving text, or press Enter to skip"/]
    C --> D{"Is the style 1 or 2?"}
    D -- No --> E["Use the sticky note holder as the default"]
    D -- Yes --> F["Set the preset size"]
    E --> F
    F --> G{"Which style?"}
    G -- "Sticky notes" --> H["86 x 86 x 60 mm, 5.8 mm teeth"]
    G -- "Index cards" --> I["138 x 86 x 60 mm, 5.8 mm teeth"]
    H --> J["Build the front, back, side, and bottom panels"]
    I --> J
    J --> K{"Was engraving text entered?"}
    K -- Yes --> L["calculateFontSize, then add the text to the bottom panel"]
    K -- No --> M["Save as StickyNoteHolder or IndexcardHolder SVG"]
    L --> M
    M --> N[/"Print the file name and dimensions"/]
    N --> Z([End])
```
