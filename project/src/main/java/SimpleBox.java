/*

SimpleBox.java
makes a simple box

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import java.util.Scanner;

public class SimpleBox {
    
    // ========== FRONT PANEL ==========
    public static String makeFrontPanelPath(double x, double y, double width, double height, 
                                           double toothWidth) {
        double thickness = 3.0;
        double toothDepth = thickness;
        
        if (toothWidth <= 0.0) toothWidth = 2.0;
        if (width <= 0.0) width = 1.0;
        if (height <= 0.0) height = 1.0;
        
        double corner = toothWidth * 2;
        double topLeftCorner = corner;
        double topRightCorner = corner;
        double bottomRightCorner = corner;
        double bottomLeftCorner = corner;
        
        StringBuilder path = new StringBuilder();
        path.append(String.format("M %.3f %.3f ", x, y));
        
        // TOP EDGE - FEMALE
        double remainingWidth = width - topLeftCorner - topRightCorner;
        if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
        
        int numTeethHorizontal = (int) Math.round(remainingWidth / (2.0 * toothWidth));
        if (numTeethHorizontal < 1) numTeethHorizontal = 1;
        int totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
        double segmentWidth = remainingWidth / totalSegmentsHorizontal;
        
        boolean isHoleIn = true;
        double currentX = x;
        
        path.append(String.format("H %.3f ", currentX + topLeftCorner));
        currentX += topLeftCorner;
        
        for (int i = 0; i < totalSegmentsHorizontal; i++) {
            if (isHoleIn) {
                path.append(String.format("V %.3f ", y + toothDepth));
                path.append(String.format("H %.3f ", currentX + segmentWidth));
                path.append(String.format("V %.3f ", y));
            } else {
                path.append(String.format("H %.3f ", currentX + segmentWidth));
            }
            currentX += segmentWidth;
            isHoleIn = !isHoleIn;
        }
        
        path.append(String.format("H %.3f ", x + width));
        
        // RIGHT EDGE - MALE
        double rightEdgeX = x + width;
        double rightEdgeY = y + topRightCorner;
        path.append(String.format("H %.3f ", rightEdgeX));
        path.append(String.format("V %.3f ", rightEdgeY));
        
        double remainingHeight = height - topRightCorner - bottomRightCorner;
        int numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        int totalSegmentsVertical = 2 * numTeethVertical - 1;
        double segmentHeight = remainingHeight / totalSegmentsVertical;
        
        boolean isToothIn = true;
        double currentY = rightEdgeY;
        
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isToothIn) {
                path.append(String.format("H %.3f ", rightEdgeX - toothDepth));
                path.append(String.format("V %.3f ", currentY + segmentHeight));
                path.append(String.format("H %.3f ", rightEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY + segmentHeight));
            }
            currentY += segmentHeight;
            isToothIn = !isToothIn;
        }
        
        // BOTTOM EDGE - FEMALE
        double bottomEdgeY = y + height;
        double bottomEdgeX = x + width - bottomRightCorner;
        path.append(String.format("V %.3f ", bottomEdgeY));
        path.append(String.format("H %.3f ", bottomEdgeX));
        
        remainingWidth = width - bottomLeftCorner - bottomRightCorner;
        if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
        
        numTeethHorizontal = (int) Math.round(remainingWidth / (2.0 * toothWidth));
        if (numTeethHorizontal < 1) numTeethHorizontal = 1;
        totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
        segmentWidth = remainingWidth / totalSegmentsHorizontal;
        
        isHoleIn = true;
        currentX = bottomEdgeX;
        
        for (int i = 0; i < totalSegmentsHorizontal; i++) {
            if (isHoleIn) {
                path.append(String.format("V %.3f ", bottomEdgeY + toothDepth));
                path.append(String.format("H %.3f ", currentX - segmentWidth));
                path.append(String.format("V %.3f ", bottomEdgeY));
            } else {
                path.append(String.format("H %.3f ", currentX - segmentWidth));
            }
            currentX -= segmentWidth;
            isHoleIn = !isHoleIn;
        }
        
        // LEFT EDGE - MALE
        double leftEdgeX = x;
        double leftEdgeY = y + height - bottomLeftCorner;
        path.append(String.format("H %.3f ", leftEdgeX));
        path.append(String.format("V %.3f ", leftEdgeY));
        
        remainingHeight = height - topLeftCorner - bottomLeftCorner;
        numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        totalSegmentsVertical = 2 * numTeethVertical - 1;
        segmentHeight = remainingHeight / totalSegmentsVertical;
        
        isToothIn = true;
        currentY = leftEdgeY;
        
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isToothIn) {
                path.append(String.format("H %.3f ", leftEdgeX + toothDepth));
                path.append(String.format("V %.3f ", currentY - segmentHeight));
                path.append(String.format("H %.3f ", leftEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY - segmentHeight));
            }
            currentY -= segmentHeight;
            isToothIn = !isToothIn;
        }
        
        path.append("Z");
        return path.toString();
    }
    
    // ========== SIDE PANEL ==========
    public static String makeSidePanelPath(double x, double y, double width, double height, 
                                          double toothWidth) {
        double thickness = 3.0;
        double toothDepth = thickness;
        
        if (toothWidth <= 0.0) toothWidth = 2.0;
        if (width <= 0.0) width = 1.0;
        if (height <= 0.0) height = 1.0;
        
        double corner = toothWidth * 2;
        double topLeftCorner = corner;
        double topRightCorner = corner;
        double bottomRightCorner = corner;
        double bottomLeftCorner = corner;
        
        StringBuilder path = new StringBuilder();
        path.append(String.format("M %.3f %.3f ", x, y));
        
        // TOP EDGE - FEMALE
        double remainingWidth = width - topLeftCorner - topRightCorner;
        if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
        
        int numTeethHorizontal = (int) Math.round(remainingWidth / (2.0 * toothWidth));
        if (numTeethHorizontal < 1) numTeethHorizontal = 1;
        int totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
        double segmentWidth = remainingWidth / totalSegmentsHorizontal;
        
        boolean isHoleIn = true;
        double currentX = x;
        
        path.append(String.format("H %.3f ", currentX + topLeftCorner));
        currentX += topLeftCorner;
        
        for (int i = 0; i < totalSegmentsHorizontal; i++) {
            if (isHoleIn) {
                path.append(String.format("V %.3f ", y + toothDepth));
                path.append(String.format("H %.3f ", currentX + segmentWidth));
                path.append(String.format("V %.3f ", y));
            } else {
                path.append(String.format("H %.3f ", currentX + segmentWidth));
            }
            currentX += segmentWidth;
            isHoleIn = !isHoleIn;
        }
        
        path.append(String.format("H %.3f ", x + width));
        
        // RIGHT EDGE - FEMALE (INVERTED from front panel)
        double rightEdgeX = x + width;
        double rightEdgeY = y + topRightCorner;
        path.append(String.format("H %.3f ", rightEdgeX));
        path.append(String.format("V %.3f ", rightEdgeY));
        
        double remainingHeight = height - topRightCorner - bottomRightCorner;
        int numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        int totalSegmentsVertical = 2 * numTeethVertical - 1;
        double segmentHeight = remainingHeight / totalSegmentsVertical;
        
        boolean isToothIn = true; // FEMALE (inverted)
        double currentY = rightEdgeY;
        
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isToothIn) {
                path.append(String.format("H %.3f ", rightEdgeX + toothDepth)); // OUT instead of IN
                path.append(String.format("V %.3f ", currentY + segmentHeight));
                path.append(String.format("H %.3f ", rightEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY + segmentHeight));
            }
            currentY += segmentHeight;
            isToothIn = !isToothIn;
        }
        
        // BOTTOM EDGE - FEMALE
        double bottomEdgeY = y + height;
        double bottomEdgeX = x + width - bottomRightCorner;
        path.append(String.format("V %.3f ", bottomEdgeY));
        path.append(String.format("H %.3f ", bottomEdgeX));
        
        remainingWidth = width - bottomLeftCorner - bottomRightCorner;
        if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
        
        numTeethHorizontal = (int) Math.round(remainingWidth / (2.0 * toothWidth));
        if (numTeethHorizontal < 1) numTeethHorizontal = 1;
        totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
        segmentWidth = remainingWidth / totalSegmentsHorizontal;
        
        isHoleIn = true;
        currentX = bottomEdgeX;
        
        for (int i = 0; i < totalSegmentsHorizontal; i++) {
            if (isHoleIn) {
                path.append(String.format("V %.3f ", bottomEdgeY + toothDepth));
                path.append(String.format("H %.3f ", currentX - segmentWidth));
                path.append(String.format("V %.3f ", bottomEdgeY));
            } else {
                path.append(String.format("H %.3f ", currentX - segmentWidth));
            }
            currentX -= segmentWidth;
            isHoleIn = !isHoleIn;
        }
        
        // LEFT EDGE - FEMALE (INVERTED from front panel)
        double leftEdgeX = x;
        double leftEdgeY = y + height - bottomLeftCorner;
        path.append(String.format("H %.3f ", leftEdgeX));
        path.append(String.format("V %.3f ", leftEdgeY));
        
        remainingHeight = height - topLeftCorner - bottomLeftCorner;
        numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        totalSegmentsVertical = 2 * numTeethVertical - 1;
        segmentHeight = remainingHeight / totalSegmentsVertical;
        
        isToothIn = true; // FEMALE (inverted)
        currentY = leftEdgeY;
        
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isToothIn) {
                path.append(String.format("H %.3f ", leftEdgeX - toothDepth)); // OUT instead of IN
                path.append(String.format("V %.3f ", currentY - segmentHeight));
                path.append(String.format("H %.3f ", leftEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY - segmentHeight));
            }
            currentY -= segmentHeight;
            isToothIn = !isToothIn;
        }
        
        path.append("Z");
        return path.toString();
    }
    
    // ========== BOTTOM PANEL ==========
    public static String makeBottomPanelPath(double x, double y, double width, double depth, double toothWidth) {
        double thickness = 3.0;
        double toothDepth = thickness;
        double brim = 6.0;
        final double edgeInset = 2.0;
        
        if (toothWidth <= 0.0) toothWidth = 2.0;
        if (width <= 0.0) width = 1.0;
        if (depth <= 0.0) depth = 1.0;
        
        double corner = toothWidth * 2;
        
        double topLeftCorner = corner;
        double topRightCorner = corner;
        double bottomRightCorner = corner;
        double bottomLeftCorner = corner;
        double rightTopCorner = corner;
        double rightBottomCorner = corner;
        double leftTopCorner = corner;
        double leftBottomCorner = corner;
        
        // Outer rectangle (with brim)
        double ox = x - brim;
        double oy = y - brim;
        double owidth = width + 2.0 * brim;
        double odepth = depth + 2.0 * brim;
        
        // Calculate teeth for FRONT/BACK edges (width dimension)
        double remainingWidth = width - corner - corner;
        if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
        
        int numTeethWidth = (int) Math.round(remainingWidth / (2.0 * toothWidth));
        if (numTeethWidth < 1) numTeethWidth = 1;
        int totalSegmentsWidth = 2 * numTeethWidth - 1;
        double segmentWidth = remainingWidth / totalSegmentsWidth;
        
        // Calculate teeth for LEFT/RIGHT edges (depth dimension)
        double remainingDepth = depth - corner - corner;
        if (remainingDepth < toothWidth) remainingDepth = Math.max(remainingDepth, toothWidth);
        
        int numTeethDepth = (int) Math.round(remainingDepth / (2.0 * toothWidth));
        if (numTeethDepth < 1) numTeethDepth = 1;
        int totalSegmentsDepth = 2 * numTeethDepth - 1;
        double segmentDepth = remainingDepth / totalSegmentsDepth;
        
        StringBuilder path = new StringBuilder();
        
        // Outer rectangle
        path.append(String.format("M %.3f %.3f ", ox, oy));
        path.append(String.format("H %.3f ", ox + owidth));
        path.append(String.format("V %.3f ", oy + odepth));
        path.append(String.format("H %.3f ", ox));
        path.append(String.format("V %.3f ", oy));
        
        // TOP EDGE HOLES (matches front panel bottom edge)
        double holeY = y + edgeInset;
        double holeX = x + topLeftCorner;
        boolean isHole = true;
        
        for (int i = 0; i < totalSegmentsWidth; i++) {
            if (isHole) {
                path.append(String.format("M %.3f %.3f ", holeX, holeY));
                path.append(String.format("V %.3f ", holeY + toothDepth));
                path.append(String.format("H %.3f ", holeX + segmentWidth));
                path.append(String.format("V %.3f ", holeY));
                path.append("Z ");
            }
            holeX += segmentWidth;
            isHole = !isHole;
        }
        
        // RIGHT EDGE HOLES (matches side panel bottom edge)
        holeX = x + width - edgeInset;
        holeY = y + rightTopCorner;
        isHole = true;
        
        for (int i = 0; i < totalSegmentsDepth; i++) {
            if (isHole) {
                path.append(String.format("M %.3f %.3f ", holeX - toothDepth, holeY));
                path.append(String.format("H %.3f ", holeX));
                path.append(String.format("V %.3f ", holeY + segmentDepth));
                path.append(String.format("H %.3f ", holeX - toothDepth));
                path.append("Z ");
            }
            holeY += segmentDepth;
            isHole = !isHole;
        }
        
        // BOTTOM EDGE HOLES
        holeY = y + depth - edgeInset;
        holeX = x + width - bottomRightCorner;
        isHole = true;
        
        for (int i = 0; i < totalSegmentsWidth; i++) {
            if (isHole) {
                path.append(String.format("M %.3f %.3f ", holeX - segmentWidth, holeY - toothDepth));
                path.append(String.format("H %.3f ", holeX));
                path.append(String.format("V %.3f ", holeY));
                path.append(String.format("H %.3f ", holeX - segmentWidth));
                path.append("Z ");
            }
            holeX -= segmentWidth;
            isHole = !isHole;
        }
        
        // LEFT EDGE HOLES
        holeX = x + edgeInset;
        holeY = y + depth - leftBottomCorner;
        isHole = true;
        
        for (int i = 0; i < totalSegmentsDepth; i++) {
            if (isHole) {
                path.append(String.format("M %.3f %.3f ", holeX, holeY - segmentDepth));
                path.append(String.format("H %.3f ", holeX + toothDepth));
                path.append(String.format("V %.3f ", holeY));
                path.append(String.format("H %.3f ", holeX));
                path.append("Z ");
            }
            holeY -= segmentDepth;
            isHole = !isHole;
        }
        
        return path.toString();
    }

    // ========== TOP PANEL ==========
    public static String makeTopPanelPath(double x, double y, double width, double depth, double toothWidth) {
        double toothDepth = 3.0;
        
        if (toothWidth <= 0.0) toothWidth = 2.0;
        if (width <= 0.0) width = 1.0;
        if (depth <= 0.0) depth = 1.0;
        
        double corner = toothWidth * 2;
        
        double topLeftCorner = corner;      
        double topRightCorner = corner;     
        double rightTopCorner = corner;      
        double rightBottomCorner = corner;  
        double bottomRightCorner = corner;  
        double bottomLeftCorner = corner;   
        double leftBottomCorner = corner;   
        double leftTopCorner = corner;       
        
        double remainingDepthLeft = depth - leftTopCorner - leftBottomCorner;
        if (remainingDepthLeft < toothWidth) remainingDepthLeft = Math.max(remainingDepthLeft, toothWidth);
        
        int numTeethVertical = (int) Math.round(remainingDepthLeft / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        int totalSegmentsVertical = 2 * numTeethVertical - 1;
        double segmentDepth = remainingDepthLeft / (double) totalSegmentsVertical;
        
        double remainingWidthTop = width - topLeftCorner - topRightCorner;
        if (remainingWidthTop < toothWidth) remainingWidthTop = Math.max(remainingWidthTop, toothWidth);
        
        int numTeethHorizontal = (int) Math.round(remainingWidthTop / (2.0 * toothWidth));
        if (numTeethHorizontal < 1) numTeethHorizontal = 1;
        int totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
        double segmentWidth = remainingWidthTop / (double) totalSegmentsHorizontal;
        
        StringBuilder path = new StringBuilder();
        
        path.append(String.format("M %.3f %.3f ", x, y));
        
        // TOP EDGE - Teeth project UP
        double curX = x + topLeftCorner;
        path.append(String.format("H %.3f ", curX));
        
        boolean toothOut = true;
        for (int i = 0; i < totalSegmentsHorizontal; i++) {
            if (toothOut) {
                path.append(String.format("V %.3f ", y - toothDepth));
                path.append(String.format("H %.3f ", curX + segmentWidth));
                path.append(String.format("V %.3f ", y));
            } else {
                path.append(String.format("H %.3f ", curX + segmentWidth));
            }
            curX += segmentWidth;
            toothOut = !toothOut;
        }
        curX += topRightCorner;
        path.append(String.format("H %.3f ", curX));
        
        // RIGHT EDGE - Teeth project RIGHT
        double curY = y + rightTopCorner;
        path.append(String.format("V %.3f ", curY));
        
        toothOut = true;
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (toothOut) {
                path.append(String.format("H %.3f ", curX + toothDepth));
                path.append(String.format("V %.3f ", curY + segmentDepth));
                path.append(String.format("H %.3f ", curX));
            } else {
                path.append(String.format("V %.3f ", curY + segmentDepth));
            }
            curY += segmentDepth;
            toothOut = !toothOut;
        }
        curY += rightBottomCorner;
        path.append(String.format("V %.3f ", curY));
        
        // BOTTOM EDGE - Teeth project DOWN
        curX -= bottomRightCorner;
        path.append(String.format("H %.3f ", curX));
        
        toothOut = true;
        for (int i = 0; i < totalSegmentsHorizontal; i++) {
            if (toothOut) {
                path.append(String.format("V %.3f ", curY + toothDepth));
                path.append(String.format("H %.3f ", curX - segmentWidth));
                path.append(String.format("V %.3f ", curY));
            } else {
                path.append(String.format("H %.3f ", curX - segmentWidth));
            }
            curX -= segmentWidth;
            toothOut = !toothOut;
        }
        curX -= bottomLeftCorner;
        path.append(String.format("H %.3f ", curX));
        
        // LEFT EDGE - Teeth project LEFT
        curY -= leftBottomCorner;
        path.append(String.format("V %.3f ", curY));
        
        toothOut = true;
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (toothOut) {
                path.append(String.format("H %.3f ", x - toothDepth));
                path.append(String.format("V %.3f ", curY - segmentDepth));
                path.append(String.format("H %.3f ", x));
            } else {
                path.append(String.format("V %.3f ", curY - segmentDepth));
            }
            curY -= segmentDepth;
            toothOut = !toothOut;
        }
        curY -= leftTopCorner;
        path.append(String.format("V %.3f ", curY));
        
        path.append("Z");
        
        return path.toString();
    }
    // Add this new method to your SimpleBox class
public static String generateBoxSVG(double width, double height, double depth, double toothWidth, String monogram) {
    StringBuilder svg = new StringBuilder();
    
    double padding = 50;
    double spacing = 50;
    double brim = 6.0;
    
    // Calculate positions
    double frontX = padding;
    double frontY = padding;
    
    double sideX = frontX + width + spacing;
    double sideY = padding;
    
    double bottomX = padding;
    double bottomY = frontY + height + spacing;
    
    double topX = sideX;
    double topY = sideY + height + spacing;
    
    double backX = padding;
    double backY = bottomY + (depth + 2*brim) + spacing;
    
    double side2X = sideX + depth + spacing;
    double side2Y = padding;
    
    // Calculate total canvas size
    double totalWidth = Math.max(side2X + depth, frontX + width) + padding;
    double totalHeight = Math.max(backY + height, topY + width) + padding;
    
    // SVG header
    svg.append("<?xml version='1.0' encoding='UTF-8'?>\n");
    svg.append(String.format("<svg viewBox='0 0 %.0f %.0f' ", totalWidth, totalHeight));
    svg.append("preserveAspectRatio='xMidYMid meet' ");
    svg.append("xmlns='http://www.w3.org/2000/svg'>\n");
    
    // Style - updated with monogram class
    svg.append("<style>\n");
    svg.append("  .cut { fill: none; stroke: black; stroke-width: 0.2; }\n");
    svg.append("  .label { font-size: 12px; font-family: Arial, sans-serif; fill: #333; }\n");
    svg.append("  .monogram { fill: #666; font-weight: bold; font-family: 'Times New Roman', serif; }\n");
    svg.append("</style>\n");
    
    // Generate panels with labels
    // Front
    svg.append("<path d='").append(makeFrontPanelPath(frontX, frontY, width, height, toothWidth)).append("' class='cut' />\n");
    svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Front</text>\n",
                            frontX + width/2, frontY - 10));
    
    // Side (right)
    svg.append("<path d='").append(makeSidePanelPath(sideX, sideY, depth, height, toothWidth)).append("' class='cut' />\n");
    svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side (Right)</text>\n",
                            sideX + depth/2, sideY - 10));
    
    // Side (left)
    svg.append("<path d='").append(makeSidePanelPath(side2X, side2Y, depth, height, toothWidth)).append("' class='cut' />\n");
    svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side (Left)</text>\n",
                            side2X + depth/2, side2Y - 10));
    
    // Bottom
    svg.append("<path d='").append(makeBottomPanelPath(bottomX, bottomY, width, depth, toothWidth)).append("' class='cut' />\n");
    svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Bottom</text>\n",
                            bottomX + width/2, bottomY - 10));
    
    // Top  
    svg.append("<path d='").append(makeTopPanelPath(topX, topY, width, depth, toothWidth)).append("' class='cut' />\n");
    svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Top</text>\n",
                            topX + width/2, topY - 10));
    
    // Add monogram to top panel if provided
    if (monogram != null && !monogram.trim().isEmpty()) {
        String name = monogram.trim();
        double panelX = topX;
        double panelY = topY;
        double panelWidth = width;
        double panelHeight = depth;
        
        // Auto-scaling font size
        double maxFont = panelHeight * 0.6;
        double minFont = 8.0;
        double scaledFont = Math.min(maxFont, (panelWidth / (name.length() * 0.6)));
        scaledFont = Math.max(minFont, scaledFont);
        
        svg.append(String.format(
            "<text x=\"%.3f\" y=\"%.3f\" font-size=\"%.3f\" " +
            "text-anchor=\"middle\" dominant-baseline=\"middle\" " +
            "class=\"monogram\">%s</text>\n",
            panelX + panelWidth / 2.0,
            panelY + panelHeight / 2.0,
            scaledFont,
            name
        ));
        
        // Optional: Add monogram to back panel as well (smaller)
        double backPanelX = backX;
        double backPanelY = backY;
        double backPanelWidth = width;
        double backPanelHeight = height;
        
        double backFont = Math.min(backPanelHeight * 0.3, scaledFont * 0.7);
        backFont = Math.max(5.0, backFont);
        
        svg.append(String.format(
            "<text x=\"%.3f\" y=\"%.3f\" font-size=\"%.3f\" " +
            "text-anchor=\"middle\" dominant-baseline=\"middle\" " +
            "class=\"monogram\" opacity=\"0.7\">%s</text>\n",
            backPanelX + backPanelWidth / 2.0,
            backPanelY + backPanelHeight / 2.0,
            backFont,
            name
        ));
    }
    
    // Back
    svg.append("<path d='").append(makeFrontPanelPath(backX, backY, width, height, toothWidth)).append("' class='cut' />\n");
    svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Back</text>\n",
                            backX + width/2, backY - 10));
    
    svg.append("</svg>");
    return svg.toString();
}
    // ========== MAIN METHOD ==========
    // ========== MAIN METHOD ==========
public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    
    System.out.println("=== Box Panel Generator ===");
    System.out.println("Generates Front, Side, and Bottom Panels in one SVG\n");
    
    // Get box dimensions
    System.out.print("Box width (mm): ");
    double width = scanner.nextDouble();
    
    System.out.print("Box height (mm): ");
    double height = scanner.nextDouble();
    
    System.out.print("Box depth (mm): ");
    double depth = scanner.nextDouble();
    
    System.out.print("Tooth width (mm): ");
    double toothWidth = scanner.nextDouble();
    scanner.nextLine(); // consume newline
    
    // Get monogram
    System.out.print("Enter name or monogram for the top panel (press Enter to skip): ");
    String monogram = scanner.nextLine().trim();
    
    scanner.close();
    
    // Validate inputs
    if (width <= 0 || height <= 0 || depth <= 0 || toothWidth <= 0) {
        System.out.println("Error: All values must be positive!");
        return;
    }
    
    // Generate SVG using the new method
    String svgContent = generateBoxSVG(width, height, depth, toothWidth, monogram);
    
    // Save to file
    String filename = String.format("box_%.0fx%.0fx%.0f_t%.0f.svg", width, height, depth, toothWidth);
    try {
        java.io.FileWriter writer = new java.io.FileWriter(filename);
        writer.write(svgContent);
        writer.close();
        System.out.println("\nSVG file saved as: " + filename);
        if (!monogram.isEmpty()) {
            System.out.println("Monogram: " + monogram.toUpperCase());
        }
    } catch (Exception e) {
        System.out.println("Error saving file: " + e.getMessage());
    }
}
}