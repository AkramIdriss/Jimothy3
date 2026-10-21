/*

SlidingBox.java
makes a sliding box

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/******************************************************************************
 * Sliding Box - Outer box + Inner sliding drawer
 * 
 * OUTER BOX (5 panels):
 * - Front, Back, Left Side, Top, Bottom
 * - Right edges flat for sliding
 * 
 * INNER BOX (5 panels):
 * - Front, Back, Left Side, Right Side, Bottom
 * - NO TOP (open)
 * - All top edges FLAT
 * - Semicircle handle on right side panel
 ******************************************************************************/

public class SlidingBox {
    
    private double outerWidth;
    private double outerDepth;
    private double outerHeight;
    private double toothWidth;
    private double thickness = 3.0;
	private String monogram;
    
    // Inner box dimensions (auto-calculated)
    private double innerWidth;
    private double innerDepth;
    private double innerHeight;
	
    
    // Constructor
    public SlidingBox(double width, double depth, double height, double toothWidth, String monogram) {
        this.outerWidth = width;
        this.outerDepth = depth;
        this.outerHeight = height;
        this.toothWidth = toothWidth;
		this.monogram = monogram;
        
        // Calculate inner box dimensions to fit inside outer box
        this.innerWidth = outerWidth - (2 * thickness);  // Clearance on left
        this.innerDepth = outerDepth - thickness;        // Slides in from right
        this.innerHeight = outerHeight - thickness;      // Clearance on bottom
    }
    
    // ========== OUTER BOX PANELS ==========
    
    // OUTER FRONT PANEL (NO teeth on right side)
    public String makeOuterFrontPanelPath(double x, double y) {
        return makeFrontPanelPath(x, y, outerWidth, outerHeight, true);
    }
    
    // OUTER BACK PANEL (NO teeth on right side)
    public String makeOuterBackPanelPath(double x, double y) {
        return makeFrontPanelPath(x, y, outerWidth, outerHeight, true);
    }
    
    // OUTER SIDE PANEL (Left only)
    public String makeOuterSidePanelPath(double x, double y) {
        return makeSidePanelPath(x, y, outerDepth, outerHeight);
    }
    
    // OUTER TOP PANEL (NO teeth on right side)
    public String makeOuterTopPanelPath(double x, double y) {
        return makeTopPanelPath(x, y, outerWidth, outerDepth, true);
    }
    
    // OUTER BOTTOM PANEL (Same as top)
    public String makeOuterBottomPanelPath(double x, double y) {
        return makeTopPanelPath(x, y, outerWidth, outerDepth, true);
    }
    
    // ========== INNER BOX PANELS ==========
    
    // INNER FRONT PANEL (NO teeth on top - flat top, MALE left/right, FEMALE bottom)
    public String makeInnerFrontPanelPath(double x, double y) {
        double toothDepth = thickness;
        
        if (toothWidth <= 0.0) toothWidth = 2.0;
        
        double corner = toothWidth * 2;
        double bottomRightCorner = corner;
        double bottomLeftCorner = corner;
        
        StringBuilder path = new StringBuilder();
        path.append(String.format("M %.3f %.3f ", x, y));
        
        // TOP EDGE - FLAT (no teeth)
        path.append(String.format("H %.3f ", x));
        
        // RIGHT EDGE - MALE (teeth OUT)
        double rightEdgeX = x + innerWidth;
        double remainingHeight = innerHeight - bottomRightCorner;
        int numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        int totalSegmentsVertical = 2 * numTeethVertical - 1;
        double segmentHeight = remainingHeight / totalSegmentsVertical;
        
        boolean isToothOut = true;
        double currentY = y;
        
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isToothOut) {
                path.append(String.format("H %.3f ", rightEdgeX - toothDepth));
                path.append(String.format("V %.3f ", currentY + segmentHeight));
                path.append(String.format("H %.3f ", rightEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY + segmentHeight));
            }
            currentY += segmentHeight;
            isToothOut = !isToothOut;
        }
        
        path.append(String.format("V %.3f ", y + innerHeight));
        
        // BOTTOM EDGE - FEMALE (holes IN)
        double bottomEdgeY = y + innerHeight;
        double bottomEdgeX = x + innerWidth - bottomRightCorner;
        path.append(String.format("H %.3f ", bottomEdgeX));
        
        double remainingWidth = innerWidth - bottomLeftCorner - bottomRightCorner;
        if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
        
        int numTeethHorizontal = (int) Math.round(remainingWidth / (2.0 * toothWidth));
        if (numTeethHorizontal < 1) numTeethHorizontal = 1;
        int totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
        double segmentWidth = remainingWidth / totalSegmentsHorizontal;
        
        boolean isHoleIn = true;
        double currentX = bottomEdgeX;
        
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
        
        // LEFT EDGE - FEMALE (INVERTED from male)
        double leftEdgeX = x;
        double leftEdgeY = y + innerHeight - bottomLeftCorner;
        path.append(String.format("H %.3f ", leftEdgeX));
        path.append(String.format("V %.3f ", leftEdgeY));

        remainingHeight = innerHeight - bottomLeftCorner;
        numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        totalSegmentsVertical = 2 * numTeethVertical - 1;
        segmentHeight = remainingHeight / totalSegmentsVertical;

        isToothOut = true;
        currentY = leftEdgeY;

        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isToothOut) {
                // CHANGE: leftEdgeX + toothDepth (RIGHT) instead of leftEdgeX - toothDepth (LEFT)
                path.append(String.format("H %.3f ", leftEdgeX + toothDepth)); // RIGHT for female
                path.append(String.format("V %.3f ", currentY - segmentHeight));
                path.append(String.format("H %.3f ", leftEdgeX)); // LEFT back
            } else {
                path.append(String.format("V %.3f ", currentY - segmentHeight));
            }
            currentY -= segmentHeight;
            isToothOut = !isToothOut;
        }

        path.append(String.format("V %.3f ", y));
        path.append("Z");
        return path.toString();
            }
    // INNER BACK PANEL (identical to front)
    public String makeInnerBackPanelPath(double x, double y) {
        return makeInnerFrontPanelPath(x, y);
    }
    
    // INNER LEFT SIDE PANEL (NO teeth on top - flat top, FEMALE left/right, FEMALE bottom)
    public String makeInnerLeftSidePanelPath(double x, double y) {
        double toothDepth = thickness;
        
        if (toothWidth <= 0.0) toothWidth = 2.0;
        
        double corner = toothWidth * 2;
        double bottomRightCorner = corner;
        double bottomLeftCorner = corner;
        
        StringBuilder path = new StringBuilder();
        path.append(String.format("M %.3f %.3f ", x, y));
        
        // TOP EDGE - FLAT (no teeth)
        path.append(String.format("H %.3f ", x + innerDepth));
        
        // RIGHT EDGE - FEMALE (holes IN)
        double rightEdgeX = x + innerDepth;
        double remainingHeight = innerHeight - bottomRightCorner;
        int numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        int totalSegmentsVertical = 2 * numTeethVertical - 1;
        double segmentHeight = remainingHeight / totalSegmentsVertical;
        
        boolean isHoleIn = true;
        double currentY = y;
        
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isHoleIn) {
                path.append(String.format("H %.3f ", rightEdgeX + toothDepth));
                path.append(String.format("V %.3f ", currentY + segmentHeight));
                path.append(String.format("H %.3f ", rightEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY + segmentHeight));
            }
            currentY += segmentHeight;
            isHoleIn = !isHoleIn;
        }
        
        path.append(String.format("V %.3f ", y + innerHeight));
        
        // BOTTOM EDGE - FEMALE (holes IN)
        double bottomEdgeY = y + innerHeight;
        double bottomEdgeX = x + innerDepth - bottomRightCorner;
        path.append(String.format("H %.3f ", bottomEdgeX));
        
        double remainingWidth = innerDepth - bottomLeftCorner - bottomRightCorner;
        if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
        
        int numTeethHorizontal = (int) Math.round(remainingWidth / (2.0 * toothWidth));
        if (numTeethHorizontal < 1) numTeethHorizontal = 1;
        int totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
        double segmentWidth = remainingWidth / totalSegmentsHorizontal;
        
        isHoleIn = true;
        double currentX = bottomEdgeX;
        
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
        
        // LEFT EDGE - FEMALE (holes IN)
        double leftEdgeX = x;
        double leftEdgeY = y + innerHeight - bottomLeftCorner;
        path.append(String.format("H %.3f ", leftEdgeX));
        path.append(String.format("V %.3f ", leftEdgeY));
        
        remainingHeight = innerHeight - bottomLeftCorner;
        numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        totalSegmentsVertical = 2 * numTeethVertical - 1;
        segmentHeight = remainingHeight / totalSegmentsVertical;
        
        isHoleIn = true;
        currentY = leftEdgeY;
        
        for (int i = 0; i < totalSegmentsVertical; i++) {
            if (isHoleIn) {
                path.append(String.format("H %.3f ", leftEdgeX - toothDepth));
                path.append(String.format("V %.3f ", currentY - segmentHeight));
                path.append(String.format("H %.3f ", leftEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY - segmentHeight));
            }
            currentY -= segmentHeight;
            isHoleIn = !isHoleIn;
        }
        
        path.append(String.format("V %.3f ", y));
        path.append("Z");
        return path.toString();
    }
    
    // INNER RIGHT SIDE PANEL (identical to left + SEMICIRCLE HANDLE)
    public String makeInnerRightSidePanelPath(double x, double y) {
        String basePath = makeInnerLeftSidePanelPath(x, y);
        
        // Add semicircle handle cutout (INWARD curve at top)
        double handleWidth = innerDepth * 0.5;   // 50% of depth
        double handleDepth = innerDepth * 0.25;  // 25% of depth
        
        // Center the handle on the top edge
        double handleCenterX = x + (innerDepth / 2.0);
        double handleY = y;
        
        // Create semicircle path going DOWN (inward) from top edge
        StringBuilder handle = new StringBuilder();
        handle.append(String.format("M %.3f %.3f ", handleCenterX - handleWidth/2, handleY));
        // Arc command: rx ry x-axis-rotation large-arc-flag sweep-flag x y
        handle.append(String.format("A %.3f %.3f 0 0 0 %.3f %.3f ", 
                                    handleWidth/2, handleDepth,
                                    handleCenterX + handleWidth/2, handleY));
                                    
        handle.append("Z ");
        
        return basePath + handle.toString();
    }
    
    // INNER BOTTOM PANEL (All edges MALE to fit into FEMALE holes)
    public String makeInnerBottomPanelPath(double x, double y) {
    double toothDepth = thickness;
    
    if (toothWidth <= 0.0) toothWidth = 2.0;
    
    double corner = toothWidth * 2;
    
    // Calculate remaining space for teeth
    double remainingWidth = innerWidth - 2 * corner;
    if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);
    
    int numTeethWidth = (int) Math.round(remainingWidth / (2.0 * toothWidth));
    if (numTeethWidth < 1) numTeethWidth = 1;
    int totalSegmentsWidth = 2 * numTeethWidth - 1;
    double segmentWidth = remainingWidth / totalSegmentsWidth;
    
    double remainingDepth = innerDepth - 2 * corner;
    if (remainingDepth < toothWidth) remainingDepth = Math.max(remainingDepth, toothWidth);
    
    int numTeethDepth = (int) Math.round(remainingDepth / (2.0 * toothWidth));
    if (numTeethDepth < 1) numTeethDepth = 1;
    int totalSegmentsDepth = 2 * numTeethDepth - 1;
    double segmentDepth = remainingDepth / totalSegmentsDepth;
    
    StringBuilder path = new StringBuilder();
    path.append(String.format("M %.3f %.3f ", x, y));
    
    // TOP EDGE (front) - FEMALE (teeth IN) - INVERTED!
    double curX = x + corner;
    path.append(String.format("H %.3f ", curX));
    
    boolean toothOut = false; // Changed from true to false - start with SPACE (female)
    for (int i = 0; i < totalSegmentsWidth; i++) {
        if (toothOut) {
            // WAS: Male tooth (protrude UP) - NOW: Space (just move right)
            path.append(String.format("H %.3f ", curX + segmentWidth));
        } else {
            // WAS: Space (just move right) - NOW: Female tooth (indent DOWN)
            path.append(String.format("V %.3f ", y + toothDepth)); // DOWN instead of UP
            path.append(String.format("H %.3f ", curX + segmentWidth));
            path.append(String.format("V %.3f ", y)); // Back up
        }
        curX += segmentWidth;
        toothOut = !toothOut;
    }
    curX += corner;
    path.append(String.format("H %.3f ", curX));
    
    // RIGHT EDGE - FEMALE (teeth IN) - INVERTED!
    double curY = y + corner;
    path.append(String.format("V %.3f ", curY));
    
    toothOut = false; // Changed from true to false - start with SPACE (female)
    for (int i = 0; i < totalSegmentsDepth; i++) {
        if (toothOut) {
            // WAS: Male tooth (protrude RIGHT) - NOW: Space (just move down)
            path.append(String.format("V %.3f ", curY + segmentDepth));
        } else {
            // WAS: Space (just move down) - NOW: Female tooth (indent LEFT)
            path.append(String.format("H %.3f ", curX - toothDepth)); // LEFT instead of RIGHT
            path.append(String.format("V %.3f ", curY + segmentDepth));
            path.append(String.format("H %.3f ", curX)); // Back right
        }
        curY += segmentDepth;
        toothOut = !toothOut;
    }
    curY += corner;
    path.append(String.format("V %.3f ", curY));
    
    // BOTTOM EDGE (back) - FEMALE (teeth IN) - INVERTED!
    curX -= corner;
    path.append(String.format("H %.3f ", curX));
    
    toothOut = false; // Changed from true to false - start with SPACE (female)
    for (int i = 0; i < totalSegmentsWidth; i++) {
        if (toothOut) {
            // WAS: Male tooth (protrude DOWN) - NOW: Space (just move left)
            path.append(String.format("H %.3f ", curX - segmentWidth));
        } else {
            // WAS: Space (just move left) - NOW: Female tooth (indent UP)
            path.append(String.format("V %.3f ", curY - toothDepth)); // UP instead of DOWN
            path.append(String.format("H %.3f ", curX - segmentWidth));
            path.append(String.format("V %.3f ", curY)); // Back down
        }
        curX -= segmentWidth;
        toothOut = !toothOut;
    }
    curX -= corner;
    path.append(String.format("H %.3f ", curX));
    
    // LEFT EDGE - FEMALE (teeth IN) - INVERTED!
    curY -= corner;
    path.append(String.format("V %.3f ", curY));
    
    toothOut = false; // Changed from true to false - start with SPACE (female)
    for (int i = 0; i < totalSegmentsDepth; i++) {
        if (toothOut) {
            // WAS: Male tooth (protrude LEFT) - NOW: Space (just move up)
            path.append(String.format("V %.3f ", curY - segmentDepth));
        } else {
            // WAS: Space (just move up) - NOW: Female tooth (indent RIGHT)
            path.append(String.format("H %.3f ", x + toothDepth)); // RIGHT instead of LEFT
            path.append(String.format("V %.3f ", curY - segmentDepth));
            path.append(String.format("H %.3f ", x)); // Back left
        }
        curY -= segmentDepth;
        toothOut = !toothOut;
    }
    curY -= corner;
    path.append(String.format("V %.3f ", curY));
    
    path.append("Z");
    return path.toString();
}
    
    // ========== HELPER METHODS ==========
    
    // Generic front/back panel (can disable right teeth)
    private String makeFrontPanelPath(double x, double y, double width, double height, boolean flatRight) {
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
        
        // RIGHT EDGE - FLAT or with teeth
        if (flatRight) {
            path.append(String.format("V %.3f ", y + height));
        } else {
            // MALE teeth on right
            double rightEdgeX = x + width;
            double rightEdgeY = y + topRightCorner;
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
            
            path.append(String.format("V %.3f ", y + height));
        }
        
        // BOTTOM EDGE - FEMALE
        // BOTTOM EDGE - MALE (INVERTED from female)
        double bottomEdgeY = y + height;
        double bottomEdgeX = x + width - bottomRightCorner;
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
                // CHANGE THIS LINE: + toothedepth to - toothedepth
                path.append(String.format("V %.3f ", bottomEdgeY - toothDepth)); // UP (male)
                path.append(String.format("H %.3f ", currentX - segmentWidth));
                path.append(String.format("V %.3f ", bottomEdgeY)); // Back DOWN
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
        
        double remainingHeight = height - topLeftCorner - bottomLeftCorner;
        int numTeethVertical = (int) Math.round(remainingHeight / (2.0 * toothWidth));
        if (numTeethVertical < 1) numTeethVertical = 1;
        int totalSegmentsVertical = 2 * numTeethVertical - 1;
        double segmentHeight = remainingHeight / totalSegmentsVertical;
        
        boolean isToothIn = true;
        double currentY = leftEdgeY;
        
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
    
    // Side panel
    private String makeSidePanelPath(double x, double y, double depth, double height) {
        double toothDepth = thickness;
        
        if (toothWidth <= 0.0) toothWidth = 2.0;
        if (depth <= 0.0) depth = 1.0;
        if (height <= 0.0) height = 1.0;
        
        double corner = toothWidth * 2;
        double topLeftCorner = corner;
        double topRightCorner = corner;
        double bottomRightCorner = corner;
        double bottomLeftCorner = corner;
        
        StringBuilder path = new StringBuilder();
        path.append(String.format("M %.3f %.3f ", x, y));
        
        // TOP EDGE - FEMALE
        double remainingWidth = depth - topLeftCorner - topRightCorner;
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
        
        path.append(String.format("H %.3f ", x + depth));
        
        // RIGHT EDGE - FEMALE
        double rightEdgeX = x + depth;
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
                path.append(String.format("H %.3f ", rightEdgeX + toothDepth));
                path.append(String.format("V %.3f ", currentY + segmentHeight));
                path.append(String.format("H %.3f ", rightEdgeX));
            } else {
                path.append(String.format("V %.3f ", currentY + segmentHeight));
            }
            currentY += segmentHeight;
            isToothIn = !isToothIn;
        }
        
        // BOTTOM EDGE - FEMALE
        // BOTTOM EDGE - MALE (converted from female)
double bottomEdgeY = y + height;
double bottomEdgeX = x + depth - bottomRightCorner;
path.append(String.format("V %.3f ", bottomEdgeY));
path.append(String.format("H %.3f ", bottomEdgeX));

remainingWidth = depth - bottomLeftCorner - bottomRightCorner;
if (remainingWidth < toothWidth) remainingWidth = Math.max(remainingWidth, toothWidth);

numTeethHorizontal = (int) Math.round(remainingWidth / (2.0 * toothWidth));
if (numTeethHorizontal < 1) numTeethHorizontal = 1;
totalSegmentsHorizontal = 2 * numTeethHorizontal - 1;
segmentWidth = remainingWidth / totalSegmentsHorizontal;

// Start with tooth out (male starts with a protrusion)
isHoleIn = false;  // Changed from true
currentX = bottomEdgeX;

for (int i = 0; i < totalSegmentsHorizontal; i++) {
    if (isHoleIn) {
        // This creates a flat segment (gap between male teeth)
        path.append(String.format("H %.3f ", currentX - segmentWidth));
    } else {
        // This creates a male tooth (protrusion)
        path.append(String.format("V %.3f ", bottomEdgeY - toothDepth));  // Go up for protrusion
        path.append(String.format("H %.3f ", currentX - segmentWidth));   // Move horizontally
        path.append(String.format("V %.3f ", bottomEdgeY));               // Go back down
    }
    currentX -= segmentWidth;
    isHoleIn = !isHoleIn;
}
        // LEFT EDGE - FEMALE
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
                path.append(String.format("H %.3f ", leftEdgeX - toothDepth));
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
    
    // Top/Bottom panel (can disable right teeth)
    private String makeTopPanelPath(double x, double y, double width, double depth, boolean flatRight) {
        double toothDepth = thickness;
        
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
        
        // RIGHT EDGE - FLAT or with teeth
        if (flatRight) {
            path.append(String.format("V %.3f ", y + depth));
            double curY = y + depth;
            curX -= bottomRightCorner;
        } else {
            // Teeth project RIGHT
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
            curX -= bottomRightCorner;
        }
        
        // BOTTOM EDGE - Teeth project DOWN
        double curY = y + depth;
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
	
	// ========== MAIN METHOD ==========
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Sliding Box Generator ===");
        System.out.println("Generates OUTER box + INNER sliding drawer\n");
        
        System.out.print("Outer box width (mm): ");
        double width = scanner.nextDouble();
        
        System.out.print("Outer box height (mm): ");
        double height = scanner.nextDouble();
        
        System.out.print("Outer box depth (mm): ");
        double depth = scanner.nextDouble();
        
        System.out.print("Tooth width (mm): ");
        double toothWidth = scanner.nextDouble();
        
		// MUST clear the leftover newline from nextDouble()
		scanner.nextLine();
		
		// Get monogram
        System.out.print("Enter name or monogram for the outer top panel (press Enter to skip): ");
		String monogram = scanner.nextLine().trim();
		
        scanner.close();
        
        if (width <= 0 || height <= 0 || depth <= 0 || toothWidth <= 0) {
            System.out.println("Error: All values must be positive!");
            return;
        }
        
        SlidingBox box = new SlidingBox(width, depth, height, toothWidth, monogram);
        String filename = String.format("SlidingBox_%.0fx%.0fx%.0f.svg", width, depth, height);
        
        try {
            box.saveToFile(filename);
            System.out.println("\nSVG file saved as: " + filename);
            System.out.printf("Outer Box: %.0f × %.0f × %.0f mm\n", width, depth, height);
            System.out.printf("Inner Box: %.0f × %.0f × %.0f mm\n", 
                             box.innerWidth, box.innerDepth, box.innerHeight);
			
			if (monogram != null && !monogram.isEmpty()) {
				System.out.println("Monogram: " + monogram.toUpperCase() + " (on outer top & inner bottom)");
			}
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
	
    // ========== GENERATE COMPLETE SVG ==========
    public String generateSVG() {
        double padding = 50;
        double spacing = 50;
        
        // OUTER BOX panel positions
        double outerFrontX = padding;
        double outerFrontY = padding;
        
        double outerSideX = outerFrontX + outerWidth + spacing;
        double outerSideY = padding;
        
        double outerBackX = outerFrontX;
        double outerBackY = outerFrontY + outerHeight + spacing;
        
        double outerTopX = outerSideX;
        double outerTopY = outerSideY + outerHeight + spacing;
        
        double outerBottomX = padding;
        double outerBottomY = outerBackY + outerHeight + spacing;
        
        // INNER BOX panel positions (below outer box)
        double innerStartY = outerBottomY + outerDepth + spacing * 2;
        
        double innerFrontX = padding;
        double innerFrontY = innerStartY;
        
        double innerLeftX = innerFrontX + innerWidth + spacing;
        double innerLeftY = innerStartY;
        
        double innerBackX = innerFrontX;
        double innerBackY = innerFrontY + innerHeight + spacing;
        
        double innerRightX = innerLeftX + innerDepth + spacing;
        double innerRightY = innerLeftY;
        
        double innerBottomX = padding;
        double innerBottomY = innerBackY + innerHeight + spacing;
        
        // Calculate canvas size
        double totalWidth = Math.max(outerSideX + outerDepth, 
                                     Math.max(innerRightX + innerDepth, innerBottomX + innerWidth)) + padding;
        double totalHeight = innerBottomY + innerDepth + padding;
        
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        svg.append(String.format("<svg width='%.0fmm' height='%.0fmm' viewBox='0 0 %.0f %.0f' ",
                                totalWidth, totalHeight, totalWidth, totalHeight));
        svg.append("xmlns='http://www.w3.org/2000/svg'>\n");
        
        // Style
        svg.append("<style>\n");
        svg.append("  .cut { fill: none; stroke: black; stroke-width: 0.2; }\n");
        svg.append("  .label { font-size: 10px; font-family: sans-serif; fill: #333; }\n");
        svg.append("  .section-label { font-size: 14px; font-weight: bold; font-family: sans-serif; fill: #000; }\n");
        svg.append("</style>\n");
        
        // OUTER BOX SECTION LABEL
        svg.append(String.format("<text x='%.1f' y='%.1f' class='section-label'>OUTER BOX</text>\n",
                                padding, padding - 30));
        
        // Outer Front Panel
        String outerFrontPath = makeOuterFrontPanelPath(outerFrontX, outerFrontY);
        svg.append("<path d='").append(outerFrontPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Front Panel (Outer)</text>\n",
                                outerFrontX + outerWidth/2, outerFrontY - 10));
        
        // Outer Side Panel
        String outerSidePath = makeOuterSidePanelPath(outerSideX, outerSideY);
        svg.append("<path d='").append(outerSidePath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side Panel (Outer Left)</text>\n",
                                outerSideX + outerDepth/2, outerSideY - 10));
        
        // Outer Back Panel
        String outerBackPath = makeOuterBackPanelPath(outerBackX, outerBackY);
        svg.append("<path d='").append(outerBackPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Back Panel (Outer)</text>\n",
                                outerBackX + outerWidth/2, outerBackY - 10));
        
        // Outer Top Panel
        String outerTopPath = makeOuterTopPanelPath(outerTopX, outerTopY);
        svg.append("<path d='").append(outerTopPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Top Panel (Outer)</text>\n",
                                outerTopX + outerWidth/2, outerTopY - 10));
        
		// ADD MONOGRAM TO OUTER TOP PANEL
		if (monogram != null && !monogram.isEmpty()) {
			double centerX = outerTopX + outerWidth / 2.0;
			double centerY = outerTopY + outerDepth / 2.0;
			double fontSize = calculateFontSize(monogram, outerWidth, outerDepth);

			svg.append(
				"<text dominant-baseline=\"middle\" font-size=\"" + String.format("%.1fpx", fontSize) +
				"\" style=\"font-family: sans-serif; font-weight: bold; fill: rgb(255,0,0)\" " +
				"text-anchor=\"middle\" transform=\"matrix(1 0 0 1 " +
				String.format("%.3f %.3f", centerX, centerY) + ")\">" +
				monogram.toUpperCase() + "</text>\n"
			);
		}
		
        // Outer Bottom Panel
        String outerBottomPath = makeOuterBottomPanelPath(outerBottomX, outerBottomY);
        svg.append("<path d='").append(outerBottomPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Bottom Panel (Outer)</text>\n",
                                outerBottomX + outerWidth/2, outerBottomY - 10));
        
        // INNER BOX SECTION LABEL
        svg.append(String.format("<text x='%.1f' y='%.1f' class='section-label'>INNER BOX (Sliding Drawer)</text>\n",
                                padding, innerStartY - 30));
        
        // Inner Front Panel
        String innerFrontPath = makeInnerFrontPanelPath(innerFrontX, innerFrontY);
        svg.append("<path d='").append(innerFrontPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Front Panel (Inner)</text>\n",
                                innerFrontX + innerWidth/2, innerFrontY - 10));
        
        // Inner Left Side Panel
        String innerLeftPath = makeInnerLeftSidePanelPath(innerLeftX, innerLeftY);
        svg.append("<path d='").append(innerLeftPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side Panel (Inner Left)</text>\n",
                                innerLeftX + innerDepth/2, innerLeftY - 10));
        
        // Inner Back Panel
        String innerBackPath = makeInnerBackPanelPath(innerBackX, innerBackY);
        svg.append("<path d='").append(innerBackPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Back Panel (Inner)</text>\n",
                                innerBackX + innerWidth/2, innerBackY - 10));
        
        // Inner Right Side Panel (with handle)
        String innerRightPath = makeInnerRightSidePanelPath(innerRightX, innerRightY);
        svg.append("<path d='").append(innerRightPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Side Panel (Inner Right) - WITH HANDLE</text>\n",
                                innerRightX + innerDepth/2, innerRightY - 10));
        
        // Inner Bottom Panel
        String innerBottomPath = makeInnerBottomPanelPath(innerBottomX, innerBottomY);
        svg.append("<path d='").append(innerBottomPath).append("' class='cut' />\n");
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label' text-anchor='middle'>Bottom Panel (Inner)</text>\n",
                                innerBottomX + innerWidth/2, innerBottomY - 10));
        
		// ADD MONOGRAM TO INNER BOTTOM PANEL
		if (monogram != null && !monogram.isEmpty()) {
			double centerX = innerBottomX + innerWidth / 2.0;
			double centerY = innerBottomY + innerDepth / 2.0;
			double fontSize = calculateFontSize(monogram, innerWidth, innerDepth);

			svg.append(
				"<text dominant-baseline=\"middle\" font-size=\"" + String.format("%.1fpx", fontSize) +
				"\" style=\"font-family: sans-serif; font-weight: bold; fill: rgb(0,0,255)\" " +  // Blue for inner
				"text-anchor=\"middle\" transform=\"matrix(1 0 0 1 " +
				String.format("%.3f %.3f", centerX, centerY) + ")\">" +
				monogram.toUpperCase() + "</text>\n"
			);
		}
		
        // Add dimension info
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label'>Outer: %.0f × %.0f × %.0f mm</text>\n",
                                padding, totalHeight - 30, outerWidth, outerDepth, outerHeight));
        svg.append(String.format("<text x='%.1f' y='%.1f' class='label'>Inner: %.0f × %.0f × %.0f mm</text>\n",
                                padding, totalHeight - 15, innerWidth, innerDepth, innerHeight));
        
        svg.append("</svg>");
        return svg.toString();
    }
    
    // ========== SAVE TO FILE ==========
    public void saveToFile(String filename) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(generateSVG());
        writer.close();
    }
}