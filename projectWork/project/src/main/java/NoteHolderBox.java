/*

NoteHolderBox.java
makes the not holder box

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/******************************************************************************

 * Produces: Complete SVG file with all note holder panels
 ******************************************************************************/

public class NoteHolderBox {
    
    private double noteWidth;
    private double noteDepth;
    private double noteHeight;
    private double toothWidth;
    private String engraving;
    
    // Note style constants
    public static final int STICKY_NOTE_HOLDER = 1;      // 86x86x60mm - Standard sticky notes
    public static final int INDEXCARD_HOLDER = 2;         // 138x86x60mm - Index cards
    
    private int noteStyle;
    
    // Constructor with preset styles
    public NoteHolderBox(int noteStyle, String engraving) {
        this.noteStyle = noteStyle;
        this.engraving = engraving;
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
    
    // Helper method to calculate optimal font size for text to fit in panel
    private double calculateFontSize(String text, double panelWidth, double panelDepth) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // Leave 20% padding on each side (60% usable width)
        double usableWidth = panelWidth * 0.6;
        double usableHeight = panelDepth * 0.6;
        
        // Rough approximation: each character is about 0.6em wide for bold sans-serif
        double charWidth = 0.6;
        int textLength = text.length();
        
        // Calculate font size based on width constraint
        double fontSizeByWidth = usableWidth / (textLength * charWidth);
        
        // Calculate font size based on height constraint (text height is roughly 1em)
        double fontSizeByHeight = usableHeight;
        
        // Use the smaller of the two to ensure it fits
        double fontSize = Math.min(fontSizeByWidth, fontSizeByHeight);
        
        // Set reasonable bounds
        double minFontSize = 8.0;   // Minimum readable size
        double maxFontSize = 50.0;  // Maximum size
        
        fontSize = Math.max(minFontSize, Math.min(maxFontSize, fontSize));
		
        return fontSize;
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
        
        // Side Panel 1
        String sidePath = makeSidePanelNHPath(sideX, sideY);
        svg.append("<path d='").append(sidePath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side Panel</text>\n",
                                sideX + noteDepth / 2, sideY - 10));
        
        // Side Panel 2
        String side2Path = makeSidePanelNHPath(side2X, side2Y);
        svg.append("<path d='").append(side2Path).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side Panel 2</text>\n",
                                side2X + noteDepth / 2, side2Y - 10));
        
        // Bottom Panel
        String bottomPath = makeBottomPanelNHPath(bottomX, bottomY);
        svg.append("<path d='").append(bottomPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Bottom Panel</text>\n",
                                bottomX + noteWidth / 2, bottomY - 10));
        
        // Add engraving to bottom panel if provided
        if (engraving != null && !engraving.isEmpty()) {
            double centerX = bottomX + noteWidth / 2.0;
            double centerY = bottomY + noteDepth / 2.0;
            double fontSize = calculateFontSize(engraving, noteWidth, noteDepth);

            svg.append(
                "<text dominant-baseline=\"middle\" font-size=\"" + String.format("%.1fpx", fontSize) +
                "\" style=\"font-family: sans-serif; font-weight: bold; fill: rgb(255,0,0)\" " +
                "text-anchor=\"middle\" transform=\"matrix(1 0 0 1 " +
                String.format("%.3f %.3f", centerX, centerY) + ")\">" +
                engraving.toUpperCase() + "</text>\n"
            );
        }
        
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
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Note Holder Box Generator ===");
        System.out.println("Choose your note holder style:\n");
        System.out.println("1. Sticky Note Holder (86 × 86 × 60 mm)");
        System.out.println("2. Indexcard Holder (138 × 86 × 60 mm)");
        System.out.print("\nEnter style (1-2): ");
        
        int noteStyle = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        // Get engraving text (can be full name now)
        System.out.print("Enter text for bottom panel engraving (or press Enter to skip): ");
        String engraving = scanner.nextLine().trim();
        
        scanner.close();
        
        // Validate input
        if (noteStyle < 1 || noteStyle > 2) {
            System.out.println("Invalid style! Using Sticky Note Holder as default.");
            noteStyle = STICKY_NOTE_HOLDER;
        }
        
        // Create box with chosen style and engraving
        NoteHolderBox box = new NoteHolderBox(noteStyle, engraving);
        
        // Generate filename based on style
        String styleName;
        if (noteStyle == STICKY_NOTE_HOLDER) {
            styleName = "StickyNoteHolder";
        } else{
            styleName = "IndexcardHolder";
        }
        
        String filename = String.format("%s_%.0fx%.0fx%.0f.svg", 
                                       styleName, box.noteWidth, box.noteDepth, box.noteHeight);
        
        try {
            box.saveToFile(filename);
            System.out.println("\nSVG file saved as: " + filename);
            System.out.println(String.format("Dimensions: %.0f × %.0f × %.0f mm", 
                                            box.noteWidth, box.noteDepth, box.noteHeight));
            if (engraving != null && !engraving.isEmpty()) {
                System.out.println("Engraving: " + engraving.toUpperCase());
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
}