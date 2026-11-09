/*

NoteHolderBox.java
makes the not holder box

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import engraving.Bounds;
import engraving.CommandLine;
import engraving.EngravingMenu;
import engraving.EngravingPlan;
import engraving.Prompter;
import engraving.SvgAsciiPreview;

/******************************************************************************

 * Produces: Complete SVG file with all note holder panels
 ******************************************************************************/

public class NoteHolderBox {

    // ========== ENGRAVABLE PANELS ==========
    // The front halves are left out: they are too narrow for a useful engraving.
    public static final String BOTTOM = "bottom";
    public static final String BACK = "back";
    public static final String SIDE = "side";
    public static final String SIDE_2 = "side 2";
    public static final List<String> ENGRAVABLE_PANELS = List.of(BOTTOM, BACK, SIDE, SIDE_2);

    /** The back and side panels have a row of slots this far (mm) up from their bottom edge; engravings stay above it. */
    private static final double SLOT_ROW_CLEARANCE = 16.0;
    
    private double noteWidth;
    private double noteDepth;
    private double noteHeight;
    private double toothWidth;
    private EngravingPlan engravings;
    
    // Note style constants
    public static final int STICKY_NOTE_HOLDER = 1;      // 86x86x60mm - Standard sticky notes
    public static final int INDEXCARD_HOLDER = 2;         // 138x86x60mm - Index cards
    
    private int noteStyle;
    
    /**
     * Original constructor used by the GUI: engraves {@code engraving} (uppercased) on the
     * bottom panel. A null or blank engraving means no engraving.
     */
    public NoteHolderBox(int noteStyle, String engraving) {
        this(noteStyle, EngravingPlan.fromText(engraving, BOTTOM));
    }

    /** Builds a note holder with whatever {@code engravings} puts on each panel. */
    public NoteHolderBox(int noteStyle, EngravingPlan engravings) {
        this.noteStyle = noteStyle;
        this.engravings = engravings == null ? EngravingPlan.none() : engravings;
        setDimensionsFromStyle(noteStyle);
    }
    
    // Set dimensions based on note style
    private void setDimensionsFromStyle(int style) {
        if (style == STICKY_NOTE_HOLDER) {
            noteWidth = 86;
            noteDepth = 86;
            noteHeight = 60;
            toothWidth = 5.8;
        }
        else if (style == INDEXCARD_HOLDER) {
            noteWidth = 138;
            noteDepth = 86;
            noteHeight = 60;
            toothWidth = 5.8;
        }
        else {
            // Default to sticky note holder
            noteWidth = 86;
            noteDepth = 86;
            noteHeight = 60;
            toothWidth = 5.8;
        }
    }
    
    // ========== FRONT PANEL (LEFT and RIGHT HALVES) ==========
    public String makeFrontPanelNHPath(double x, double y) {
        StringBuilder path = new StringBuilder();
        int toothDepth = 3;
        double cornerLength = 9.75;
        double gap = 13.0;
        
        // Start at top-left corner
        path.append(String.format("M %.3f %.3f ", x, y));
        
        // TOP EDGE
        double trueHalfWidth = noteWidth / 2.0;
        double panelRightEdgeX = x + trueHalfWidth - gap;
        path.append(String.format("H %.3f ", panelRightEdgeX));
        
        // RIGHT SIDE - Flat (Vertical line down the middle)
        double currentY = y + noteHeight - 10;
        path.append(String.format("V %.3f ", currentY));
        
        // BOTTOM EDGE
        double bridgeDropX = x + trueHalfWidth - 19.5;
        path.append(String.format("H %.3f ", bridgeDropX));
        path.append(String.format("V %.3f ", currentY + 10));
        currentY += 10;
        
        double footEndX = bridgeDropX - 19.5;
        path.append(String.format("H %.3f ", footEndX));
        
        // LEFT SIDE - TEETH
        currentY = y + noteHeight - cornerLength;
        
        // Tooth 1
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 2
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 3
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 4
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("V %.3f ", y));
        
        // HOLE 1
        double holeWidth = 5.8;
        double holeHeight = 3;
        double holeY = y + noteHeight - 12.5;
        double spacing = noteWidth / 7.0;
        double holeX = x + 5.783 + toothDepth;
        path.append(String.format("M %.3f %.3f ", holeX, holeY));
        path.append(String.format("V %.3f ", holeY - holeHeight));
        path.append(String.format("H %.3f ", holeX + holeWidth));
        path.append(String.format("V %.3f ", holeY));
        path.append("Z ");
        
        return path.toString();
    }
	
    // ========== BACK PANEL ==========
    public String makeBackPanelNHPath(double x, double y) {
        StringBuilder path = new StringBuilder();
        
        double backToothWidth = 5.8;
        int toothDepth = 3;
        double cornerLength = 10.5;
        double bridgeOne = noteWidth / 4;
        double bridgeTwo = noteWidth / 2;
        
        path.append(String.format("M %.3f %.3f ", x, y));
        path.append(String.format("H %.3f ", x + noteWidth));
        
        // RIGHT SIDE
        double currentY = y + cornerLength;
        
        // Tooth 1
        path.append(String.format("H %.3f ", x + noteWidth - toothDepth));
        path.append(String.format("V %.3f ", currentY + backToothWidth));
        path.append(String.format("H %.3f ", x + noteWidth));
        currentY += backToothWidth;
        path.append(String.format("V %.3f ", currentY + backToothWidth));
        currentY += backToothWidth;
        
        // Tooth 2
        path.append(String.format("H %.3f ", x + noteWidth - toothDepth));
        path.append(String.format("V %.3f ", currentY + backToothWidth));
        path.append(String.format("H %.3f ", x + noteWidth));
        currentY += backToothWidth;
        path.append(String.format("V %.3f ", currentY + backToothWidth));
        currentY += backToothWidth;
        
        // Tooth 3
        path.append(String.format("H %.3f ", x + noteWidth - toothDepth));
        path.append(String.format("V %.3f ", currentY + backToothWidth));
        path.append(String.format("H %.3f ", x + noteWidth));
        currentY += backToothWidth;
        path.append(String.format("V %.3f ", currentY + backToothWidth));
        currentY += backToothWidth;
        
        // Final tooth
        path.append(String.format("H %.3f ", x + noteWidth - toothDepth));
        currentY += backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("V %.3f ", y + noteHeight));
        currentY = y + noteHeight;
        
        // BOTTOM EDGE
        double currentX = x + noteWidth;
        path.append(String.format("H %.3f ", currentX - bridgeOne));
        currentX -= bridgeOne;
        path.append(String.format("V %.3f ", currentY - 10));
        currentY -= 10;
        path.append(String.format("H %.3f ", currentX - bridgeTwo));
        currentX -= bridgeTwo;
        path.append(String.format("V %.3f ", currentY + 10));
        currentY += 10;
        path.append(String.format("H %.3f ", currentX - bridgeOne));
        currentX -= bridgeOne;
        
        // LEFT SIDE
        currentY = y + noteHeight - cornerLength;
        
        // Tooth 1
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 2
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 3
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Final tooth
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= backToothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("V %.3f ", y));
        
        // HOLES
        double holeWidth = backToothWidth;
        double holeHeight = toothDepth;
        double holeY = y + noteHeight - 12.5;
        double spacing = noteWidth / 7.0;
        
        for (int i = 1; i <= 6; i++) {
            double holeX = x + spacing * i - holeWidth / 2.0;
            path.append(String.format("M %.3f %.3f ", holeX, holeY));
            path.append(String.format("V %.3f ", holeY - holeHeight));
            path.append(String.format("H %.3f ", holeX + holeWidth));
            path.append(String.format("V %.3f ", holeY));
            path.append("Z ");
        }
        
        return path.toString();
    }
    
    // ========== SIDE PANEL ==========
    public String makeSidePanelNHPath(double x, double y) {
        StringBuilder path = new StringBuilder();
        int toothDepth = 3;
        double cornerLength = 15.5;
        double bridgeOne = noteDepth / 4;
        double bridgeTwo = noteDepth / 2;
        
        path.append(String.format("M %.3f %.3f ", x, y));
        path.append(String.format("H %.3f ", x + noteDepth));
        
        // RIGHT SIDE
        double currentY = y + cornerLength;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 1
        path.append(String.format("H %.3f ", x + noteDepth - toothDepth));
        path.append(String.format("V %.3f ", currentY + toothWidth));
        path.append(String.format("H %.3f ", x + noteDepth));
        currentY += toothWidth;
        path.append(String.format("V %.3f ", currentY + toothWidth));
        currentY += toothWidth;
        
        // Tooth 2
        path.append(String.format("H %.3f ", x + noteDepth - toothDepth));
        path.append(String.format("V %.3f ", currentY + toothWidth));
        path.append(String.format("H %.3f ", x + noteDepth));
        currentY += toothWidth;
        path.append(String.format("V %.3f ", currentY + toothWidth));
        currentY += toothWidth;
        
        // Tooth 3
        path.append(String.format("H %.3f ", x + noteDepth - toothDepth));
        path.append(String.format("V %.3f ", currentY + toothWidth));
        path.append(String.format("H %.3f ", x + noteDepth));
        currentY += toothWidth;
        path.append(String.format("V %.3f ", currentY + toothWidth));
        currentY += toothWidth;
        
        path.append(String.format("V %.3f ", y + noteHeight));
        currentY = y + noteHeight;
        
        // BOTTOM EDGE
        double currentX = x + noteDepth;
        path.append(String.format("H %.3f ", currentX - bridgeOne));
        currentX -= bridgeOne;
        path.append(String.format("V %.3f ", currentY - 10));
        currentY -= 10;
        path.append(String.format("H %.3f ", currentX - bridgeTwo));
        currentX -= bridgeTwo;
        path.append(String.format("V %.3f ", currentY + 10));
        currentY += 10;
        path.append(String.format("H %.3f ", currentX - bridgeOne));
        currentX -= bridgeOne;
        
        // LEFT SIDE
        currentY = y + noteHeight - cornerLength;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 1
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 2
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Tooth 3
        path.append(String.format("H %.3f ", x + toothDepth));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", x));
        currentY -= toothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        path.append(String.format("V %.3f ", y));
        
        // HOLES
        double holeWidth = 5.8;
        double holeHeight = 3;
        double holeY = y + noteHeight - 12.5;
        double spacing = noteDepth / 7.0;
        
        for (int i = 1; i <= 6; i++) {
            double holeX = x + spacing * i - holeWidth / 2.0;
            path.append(String.format("M %.3f %.3f ", holeX, holeY));
            path.append(String.format("V %.3f ", holeY - holeHeight));
            path.append(String.format("H %.3f ", holeX + holeWidth));
            path.append(String.format("V %.3f ", holeY));
            path.append("Z ");
        }
        
        return path.toString();
    }
    
    // ========== BOTTOM PANEL ==========
    public String makeBottomPanelNHPath(double x, double y) {
        StringBuilder path = new StringBuilder();
        double toothDepth = 3;
        double bottomToothWidth = 5.7;
        double cornerLength = 5.783;
        double bridgeOne = ((noteWidth - 6) / 4) - cornerLength - bottomToothWidth;
        double bridgeTwo = (noteWidth - 6) / 2;
        double spacingWidth = (noteWidth) / 7.0;
        double spacingDepth = (noteDepth) / 7.0;
        
        path.append(String.format("M %.3f %.3f ", x + 3, y + 3));
        double currentX = x + 3;
        double currentY = y + 3;
        
        // TOP SIDE - male teeth using spacing formula
        path.append(String.format("H %.3f ", currentX + cornerLength));
        currentX += cornerLength;
        
        for (int i = 1; i <= 6; i++) {
            double toothX = x + spacingWidth * i - bottomToothWidth / 2.0;
            
            // Move to tooth start
            path.append(String.format("H %.3f ", toothX));
            path.append(String.format("V %.3f ", y));
            path.append(String.format("H %.3f ", toothX + bottomToothWidth));
            path.append(String.format("V %.3f ", currentY));
            currentX = toothX + bottomToothWidth;
        }
        
        path.append(String.format("H %.3f ", x + noteWidth - 3));
        currentX = x + noteWidth - 3;
        
        // RIGHT SIDE - using spacing formula
        path.append(String.format("V %.3f ", currentY + cornerLength));
        currentY += cornerLength;
        
        for (int i = 1; i <= 6; i++) {
            double toothY = y + spacingDepth * i - bottomToothWidth / 2.0;
            
            // Move to tooth start
            path.append(String.format("V %.3f ", toothY));
            path.append(String.format("H %.3f ", currentX + toothDepth));
            path.append(String.format("V %.3f ", toothY + bottomToothWidth));
            path.append(String.format("H %.3f ", currentX));
            currentY = toothY + bottomToothWidth;
        }
        
        path.append(String.format("V %.3f ", y + noteDepth - 3));
        currentY = y + noteDepth - 3;
        
        // BOTTOM SIDE
        // Bottom tooth 1
        path.append(String.format("H %.3f ", currentX - cornerLength));
        currentX -= cornerLength;
        path.append(String.format("V %.3f ", currentY + toothDepth));
        path.append(String.format("H %.3f ", currentX - bottomToothWidth));
        currentX -= bottomToothWidth;
        path.append(String.format("V %.3f ", currentY));
        
        // Bridge
        path.append(String.format("H %.3f ", currentX - bridgeOne));
        currentX -= bridgeOne;
        path.append(String.format("V %.3f ", currentY - 10));
        currentY -= 10;
        path.append(String.format("H %.3f ", currentX - bridgeTwo));
        currentX -= bridgeTwo;
        path.append(String.format("V %.3f ", currentY + 10));
        currentY += 10;
        path.append(String.format("H %.3f ", currentX - bridgeOne));
        currentX -= bridgeOne;
        
        // Bottom tooth 2
        path.append(String.format("V %.3f ", currentY + toothDepth));
        path.append(String.format("H %.3f ", currentX - bottomToothWidth));
        currentX -= bottomToothWidth;
        path.append(String.format("V %.3f ", currentY));
        path.append(String.format("H %.3f ", currentX - cornerLength));
        currentX -= cornerLength;
        
        // LEFT SIDE - using spacing formula
        path.append(String.format("V %.3f ", currentY - cornerLength));
        currentY -= cornerLength;
        
        for (int i = 6; i >= 1; i--) {
            double toothY = y + spacingDepth * i - bottomToothWidth / 2.0;
            
            // Move to tooth end (going backwards)
            path.append(String.format("V %.3f ", toothY + bottomToothWidth));
            path.append(String.format("H %.3f ", currentX - toothDepth));
            path.append(String.format("V %.3f ", toothY));
            path.append(String.format("H %.3f ", currentX));
            currentY = toothY;
        }
        
        path.append("Z");
        
        return path.toString();
    }
    
    // ========== GENERATE COMPLETE SVG ==========
    public String generateSVG() {
        double padding = 50;
        double spacing = 30;
        
        // Calculate panel positions
        double frontX = padding;
        double frontY = padding;
        
        double otherFrontX = frontX + (noteWidth / 2) + spacing;
        double otherFrontY = padding;
        
        double backX = padding;
        double backY = frontY + noteHeight + spacing;
        
        double sideX = backX + noteWidth + spacing;
        double sideY = backY;
        
        double side2X = sideX + noteDepth + spacing;
        double side2Y = sideY;
        
        double bottomX = padding;
        double bottomY = backY + noteHeight + spacing;
        
        // Calculate canvas size
        double totalWidth = side2X + noteDepth + padding;
        double totalHeight = bottomY + noteDepth + padding;
        
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        svg.append(String.format("<svg width='%.0fmm' height='%.0fmm' viewBox='0 0 %.0f %.0f' ",
                                totalWidth, totalHeight, totalWidth, totalHeight));
        svg.append("xmlns='http://www.w3.org/2000/svg'>\n");
        
        // Style
        svg.append("<style>\n");
        svg.append("  .cut { fill: none; stroke: black; stroke-width: 0.2; }\n");
        svg.append("  .label { font-size: 10px; font-family: sans-serif; fill: #333; }\n");
        svg.append("</style>\n");
        
        // Front Panel (Left)
        String frontPath = makeFrontPanelNHPath(frontX, frontY);
        svg.append("<path d='").append(frontPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Front Left</text>\n",
                                frontX + noteWidth / 4, frontY - 10));
        
        // Front Panel (Right)
        String front2Path = makeFrontPanelNHPath(otherFrontX, frontY);
        svg.append("<path d='").append(front2Path).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Front Right</text>\n",
                                otherFrontX + noteWidth / 4, frontY - 10));
        
        // Back Panel
        String backPath = makeBackPanelNHPath(backX, backY);
        svg.append("<path d='").append(backPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Back Panel</text>\n",
                                backX + noteWidth / 2, backY - 10));
        svg.append(engravings.render(BACK, new Bounds(backX, backY, noteWidth, noteHeight - SLOT_ROW_CLEARANCE)));
        
        // Side Panel 1
        String sidePath = makeSidePanelNHPath(sideX, sideY);
        svg.append("<path d='").append(sidePath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side Panel</text>\n",
                                sideX + noteDepth / 2, sideY - 10));
        svg.append(engravings.render(SIDE, new Bounds(sideX, sideY, noteDepth, noteHeight - SLOT_ROW_CLEARANCE)));
        
        // Side Panel 2
        String side2Path = makeSidePanelNHPath(side2X, side2Y);
        svg.append("<path d='").append(side2Path).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side Panel 2</text>\n",
                                side2X + noteDepth / 2, side2Y - 10));
        svg.append(engravings.render(SIDE_2, new Bounds(side2X, side2Y, noteDepth, noteHeight - SLOT_ROW_CLEARANCE)));
        
        // Bottom Panel
        String bottomPath = makeBottomPanelNHPath(bottomX, bottomY);
        svg.append("<path d='").append(bottomPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Bottom Panel</text>\n",
                                bottomX + noteWidth / 2, bottomY - 10));
        
        svg.append(engravings.render(BOTTOM, new Bounds(bottomX, bottomY, noteWidth, noteDepth)));
        
        svg.append("</svg>");
        return svg.toString();
    }
    
    // ========== SAVE TO FILE ==========
    public void saveToFile(String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(generateSVG());
        writer.close();
    }
    
    // ========== MAIN METHOD ==========
    private static final String USAGE = """
            One-line usage:
              java -cp target/classes NoteHolderBox sticky|index [options]
            Example:
              java -cp target/classes NoteHolderBox sticky --text "Notes" --on back
            """ + CommandLine.optionsHelp(ENGRAVABLE_PANELS);

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true"); // fonts/images only; never open a window
        if (args.length > 0) {
            runFromArguments(args);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Note Holder Box Generator ===");
        System.out.println("Tip: run with --help to do all of this on one line.\n");
        System.out.println("Choose your note holder style:\n");
        System.out.println("1. Sticky Note Holder (86 × 86 × 60 mm)");
        System.out.println("2. Indexcard Holder (138 × 86 × 60 mm)");
        System.out.print("\nEnter style (1-2): ");
        
        int noteStyle = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // Validate input
        if (noteStyle < 1 || noteStyle > 2) {
            System.out.println("Invalid style! Using Sticky Note Holder as default.");
            noteStyle = STICKY_NOTE_HOLDER;
        }
        
        // Engravings (text, logos, pictures) on any panel
        EngravingPlan engravings = EngravingMenu.ask(new Prompter(scanner), ENGRAVABLE_PANELS);
        scanner.close();
        
        save(noteStyle, engravings, true);
    }

    private static void runFromArguments(String[] args) {
        try {
            CommandLine line = CommandLine.parse(args, ENGRAVABLE_PANELS);
            if (line.help()) {
                System.out.println(USAGE);
                return;
            }
            if (line.positional().size() != 1) {
                throw new IllegalArgumentException("Expected one style: sticky or index");
            }
            save(parseStyle(line.positional().get(0)), line.plan(), line.preview());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + "\n");
            System.out.println(USAGE);
            System.exit(1);
        }
    }

    private static int parseStyle(String style) {
        switch (style.toLowerCase()) {
            case "sticky": case "1":
                return STICKY_NOTE_HOLDER;
            case "index": case "indexcard": case "2":
                return INDEXCARD_HOLDER;
            default:
                throw new IllegalArgumentException("Unknown style: " + style + " (use sticky or index)");
        }
    }

    private static void save(int noteStyle, EngravingPlan engravings, boolean preview) {
        NoteHolderBox box = new NoteHolderBox(noteStyle, engravings);
        
        String styleName = noteStyle == STICKY_NOTE_HOLDER ? "StickyNoteHolder" : "IndexcardHolder";
        String filename = String.format("%s_%.0fx%.0fx%.0f.svg", 
                                       styleName, box.noteWidth, box.noteDepth, box.noteHeight);
        
        try {
            box.saveToFile(filename);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
            return;
        }
        System.out.println("\nSVG file saved as: " + filename);
        System.out.println(String.format("Dimensions: %.0f × %.0f × %.0f mm", 
                                        box.noteWidth, box.noteDepth, box.noteHeight));
        for (String line : engravings.summary()) {
            System.out.println("Engraving on " + line);
        }
        if (preview) {
            SvgAsciiPreview.print(box.generateSVG());
        }
    }
}
