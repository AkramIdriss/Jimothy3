/*

MainApp.java
makes the thing run

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle; 
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition; 
import javafx.animation.Timeline;
import java.io.ByteArrayInputStream;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;


import java.io.File;
import java.io.IOException;
import javafx.animation.KeyFrame;           
import javafx.animation.KeyValue;          
import javafx.util.Duration;    


public class MainApp
{

    // Track the selected box
    private StackPane selectedBox = null;
    private String selectedStyle = null;

    @Override
    public void start() 
    {
       
    }

    // Add this method to MainApp.java
    private void selectBox(StackPane toSelect, StackPane... others) 
    {
        selectedBox = toSelect;
        for (StackPane box : others) box.setStyle("");
        toSelect.setStyle("-fx-border-color: white; -fx-border-width: 2;");
    }

    // --------------------------- BOX SELECTION SCENE ---------------------------
    private Scene box_selection(Stage stage) 
{   
    // Title - moved higher
    Text pickStyle = new Text("Please choose a style");         
    pickStyle.setFont(Font.font("Garamond", 30));
    pickStyle.setFill(Color.WHITE);
    
    // Create a spacer to push title up
    Region topSpacer = new Region();
    VBox.setVgrow(topSpacer, Priority.ALWAYS);

    // Load images
    Image aBoxImg = new Image(getClass().getResourceAsStream("/UI/images/BasedBox.jpg"));
    Image basedBoxImg = new Image(getClass().getResourceAsStream("/UI/images/sliding.jpg"));
    Image electronicsBoxImg = new Image(getClass().getResourceAsStream("/UI/images/note.jpg"));

    // Create image containers with labels
    HBox imagesBox = new HBox(30);
    imagesBox.setAlignment(Pos.CENTER);
    
    // Box A - Based Box
    VBox boxAContainer = createBoxWithLabel(aBoxImg, "Based Box", 300, 20);
    StackPane boxA = (StackPane) boxAContainer.getChildren().get(0);
    
    // Box B - Sliding Box (disabled)
    VBox boxBContainer = createBoxWithLabel(basedBoxImg, "Sliding Box", 300, 20);
    StackPane boxB = (StackPane) boxBContainer.getChildren().get(0);
    
    // Box C - Note Holder
    VBox boxCContainer = createBoxWithLabel(electronicsBoxImg, "Note Holder", 300, 20);
    StackPane boxC = (StackPane) boxCContainer.getChildren().get(0);

    // Add to images box
    imagesBox.getChildren().addAll(boxAContainer, boxBContainer, boxCContainer);

    // Selection handlers
    boxA.setOnMouseClicked(e -> {
        selectedStyle = "basic";
        selectBox(boxA, boxB, boxC);
    });

    boxB.setOnMouseClicked(e -> {
        selectedStyle = "sliding";
        selectBox(boxB, boxA, boxC);
    });

    boxC.setOnMouseClicked(e -> {
        selectedStyle = "note";
        selectBox(boxC, boxA, boxB);
    });

    // Bind image sizes to stage width
    stage.widthProperty().addListener((obs, oldVal, newVal) -> {
        double stageWidth = newVal.doubleValue();
        double imageSize = Math.min(300, stageWidth * 0.15);
        
        // Update all image containers
        updateImageSize(boxA, imageSize);
        updateImageSize(boxB, imageSize);
        updateImageSize(boxC, imageSize);
        
        // Adjust spacing
        imagesBox.setSpacing(stageWidth * 0.03);
        
        // Update label fonts based on size
        double labelFontSize = Math.max(40, imageSize * 0.05);
        for (javafx.scene.Node node : imagesBox.getChildren()) {
            VBox container = (VBox) node;
            Label label = (Label) container.getChildren().get(1);
            label.setFont(Font.font("Garamond", labelFontSize));
        }
    });

    // Initial scaling
    double initialImageSize = Math.min(300, stage.getWidth() * 0.15);
    updateImageSize(boxA, initialImageSize);
    updateImageSize(boxB, initialImageSize);
    updateImageSize(boxC, initialImageSize);

    // Buttons - create spacer to push them down
    Region buttonSpacer = new Region();
    VBox.setVgrow(buttonSpacer, Priority.ALWAYS);
    
    Button backBtn = new Button("Back");
    Button nextBtn = new Button("Next");
    backBtn.setFont(Font.font(20));
    nextBtn.setFont(Font.font(20));
    backBtn.setPrefWidth(120);
    nextBtn.setPrefWidth(120);

    HBox navBox = new HBox(300, backBtn, nextBtn);
    navBox.setAlignment(Pos.CENTER);
    navBox.setPadding(new Insets(20, 0, 40, 0)); // Added bottom padding

    // Main layout with spacers
    VBox layout = new VBox(20, topSpacer, pickStyle, imagesBox, buttonSpacer, navBox);
    layout.setAlignment(Pos.CENTER);
    layout.setStyle("-fx-background-color: linear-gradient(to bottom, #00008B, #1d2d41ff);");
    layout.setPadding(new Insets(30, 50, 30, 50)); // Reduced top padding, increased bottom

    Scene scene = new Scene(layout);
    layout.prefWidthProperty().bind(stage.widthProperty());
    layout.prefHeightProperty().bind(stage.heightProperty());

    //Back goes to start scene
    backBtn.setOnAction(e -> stage.setScene(startScene));

    // Next goes to dimension scene
    nextBtn.setOnAction(e -> 
    {
        if (selectedStyle == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a box first!");
            alert.showAndWait();
            return;
        }

        if (selectedStyle.equals("note")) {
            stage.setScene(createNotePresetScene(stage));
        }
        else {
            stage.setScene(createDimensionScene(stage));
        }
    });

    return scene;
}

// Helper method to create box with label
private VBox createBoxWithLabel(Image img, String labelText, double fitWidth, double arcSize) {
    StackPane imagePane = createRoundedImage(img, fitWidth, arcSize);
    
    // Create label
    Label label = new Label(labelText);
    label.setFont(Font.font("Garamond", 30));
    label.setTextFill(Color.WHITE);
    label.setPadding(new Insets(10, 0, 0, 0));
    
    // Center label
    VBox container = new VBox(5, imagePane, label);
    container.setAlignment(Pos.CENTER);
    
    return container;
}

// Helper method to update image size
private void updateImageSize(StackPane imageContainer, double newSize) {
    ImageView iv = (ImageView) imageContainer.getChildren().get(0);
    iv.setFitWidth(newSize);
    iv.setPreserveRatio(true);
    
    Rectangle clip = (Rectangle) iv.getClip();
    clip.setWidth(newSize);
    clip.setHeight(iv.getImage().getHeight() * (newSize / iv.getImage().getWidth()));
}
    // --------------------------- DIMENSION SCENE ---------------------------
    private Scene createDimensionScene(Stage stage) 
    {
        Text title = new Text("Enter Dimensions");
        title.setFont(Font.font("Garamond", 30));
        title.setFill(Color.WHITE);

        double fieldWidth = 200; // fixed short width
        double fieldHeightFactor = 0.07; // scales with window height

        TextField lengthField = new TextField();
        lengthField.setPromptText("Length (depth in mm)");
        lengthField.setMaxWidth(fieldWidth);
        lengthField.prefHeightProperty().bind(stage.heightProperty().multiply(fieldHeightFactor));
        lengthField.setFont(Font.font("Garamond", 18)); 

        TextField widthField = new TextField();
        widthField.setPromptText("Width (mm)");
        widthField.setMaxWidth(fieldWidth);
        widthField.prefHeightProperty().bind(stage.heightProperty().multiply(fieldHeightFactor));
        widthField.setFont(Font.font("Garamond", 18));

        TextField heightField = new TextField();
        heightField.setPromptText("Height (mm)");
        heightField.setMaxWidth(fieldWidth);
        heightField.prefHeightProperty().bind(stage.heightProperty().multiply(fieldHeightFactor));
        heightField.setFont(Font.font("Garamond", 18));

        TextField teethField = new TextField();
        teethField.setPromptText("Tooth Width (mm)");
        teethField.setMaxWidth(fieldWidth);
        teethField.prefHeightProperty().bind(stage.heightProperty().multiply(fieldHeightFactor));
        teethField.setFont(Font.font("Garamond", 18));
        
        // ADD MONOGRAM FIELD
        TextField monogramField = new TextField();
        monogramField.setPromptText("Monogram (2 letters, optional)");
        monogramField.setMaxWidth(fieldWidth);
        monogramField.prefHeightProperty().bind(stage.heightProperty().multiply(fieldHeightFactor));
        monogramField.setFont(Font.font("Garamond", 18));

        // Back and Next buttons
        Button backBtn = new Button("Back");
        Button nextBtn = new Button("Next");
        backBtn.setFont(Font.font(16));
        nextBtn.setFont(Font.font(16));
        backBtn.setPrefWidth(120);
        nextBtn.setPrefWidth(120);

        HBox navBox = new HBox(300, backBtn, nextBtn);
        navBox.setAlignment(Pos.CENTER);
        navBox.setPadding(new Insets(20));

        // Back goes to box selection scene
        backBtn.setOnAction(e -> stage.setScene(boxScene));

        // Next button - UPDATE THIS
        // Next button - UPDATE THIS
        nextBtn.setOnAction(e -> 
        {
            try {
                double length = Double.parseDouble(lengthField.getText());
                double width = Double.parseDouble(widthField.getText());
                double height = Double.parseDouble(heightField.getText());
                double teeth = Double.parseDouble(teethField.getText());
                String monogram = monogramField.getText().trim();
                
                // Validate inputs
                if (length <= 0 || width <= 0 || height <= 0 || teeth <= 0) {
                    throw new IllegalArgumentException("All dimensions must be positive numbers");
                }
                
                // Check which box type was selected
                if (selectedStyle != null && selectedStyle.equals("sliding")) {
                    // Use sliding box preview - note: length = depth for sliding box
                    Scene previewScene = createSlidingBoxPreviewScene(stage, width, length, height, teeth, monogram);
                    stage.setScene(previewScene);
                } else if (selectedStyle != null && selectedStyle.equals("basic")) {
                    // Use regular box preview
                    Scene previewScene = createPreviewScene(stage, length, width, height, teeth, monogram);
                    stage.setScene(previewScene);
                } else {
                    // Default to regular box preview (fallback)
                    Scene previewScene = createPreviewScene(stage, length, width, height, teeth, monogram);
                    stage.setScene(previewScene);
                }
            }
            catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter valid numbers for all dimensions.");
                alert.showAndWait();
            }
            catch (IllegalArgumentException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
                alert.showAndWait();
            }
        });
        // UPDATE LAYOUT TO INCLUDE MONOGRAM FIELD
        VBox layout = new VBox(20, title, lengthField, widthField, heightField, teethField, monogramField, navBox);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #00008B, #1d2d41ff);");
        layout.setPadding(new Insets(50));

        Scene dimScene = new Scene(layout);
        layout.prefWidthProperty().bind(stage.widthProperty());
        layout.prefHeightProperty().bind(stage.heightProperty());
        return dimScene;
    }

    // --------------------------- HELPER: CREATE ROUNDED IMAGE ---------------------------
    private StackPane createRoundedImage(Image img, double fitWidth, double arcSize) 
    {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(fitWidth);
        iv.setPreserveRatio(true);

        Rectangle clip = new Rectangle(fitWidth, img.getHeight() * (fitWidth / img.getWidth()));
        clip.setArcWidth(arcSize);
        clip.setArcHeight(arcSize);
        iv.setClip(clip);

        StackPane container = new StackPane(iv);
        container.setPadding(new Insets(10));

        DropShadow normalShadow = new DropShadow(10, 5, 5, Color.rgb(0,0,0,0.5));
        DropShadow hoverShadow = new DropShadow(15, 0, 0, Color.rgb(255,255,255,0.7));

        container.setEffect(normalShadow);
        container.setOnMouseEntered(e -> container.setEffect(hoverShadow));
        container.setOnMouseExited(e -> container.setEffect(normalShadow));

        return container;
    }

    public static void main(String[] args) 
    {
        launch(args);
    }

    // --------------------------- PREVIEW SCENE (#4) ---------------------------
private Scene createPreviewScene(Stage stage, double length, double width, double height, double teeth, String monogram) 
{
    Text title = new Text("Box Pattern Preview - Ready for Laser Cutting");
    title.setFont(Font.font("Garamond", 30));
    title.setFill(Color.WHITE);
    
    // Generate SVG
    String svgContent = generateSimpleBoxSVG(width, length, height, teeth, monogram);
    
    // Create a web view
    WebView webView = new WebView();
    WebEngine engine = webView.getEngine();
    
    // HTML with zoom control
    String html = "<html><head><style>"
                + "body, html { margin:0; padding:0; width:100%; height:100%; overflow:auto; background:white; }"
                + "#svg-container { width:100%; height:100%; text-align:center; padding:20px; }"
                + "svg { transition: transform 0.3s; }"
                + "</style></head><body>"
                + "<div id='svg-container'>" + svgContent + "</div>"
                + "<script>"
                + "let scale = 1.0;"
                + "function zoomIn() { scale *= 1.2; updateScale(); }"
                + "function zoomOut() { scale /= 1.2; updateScale(); }"
                + "function resetZoom() { scale = 1.0; updateScale(); }"
                + "function updateScale() { document.querySelector('svg').style.transform = 'scale(' + scale + ')'; }"
                + "</script>"
                + "</body></html>";
    
    engine.loadContent(html);
    
    // Zoom buttons
    Button zoomInBtn = new Button("+");
    Button zoomOutBtn = new Button("-");
    Button resetZoomBtn = new Button("Reset");
    
    zoomInBtn.setFont(Font.font(20));
    zoomOutBtn.setFont(Font.font(20));
    resetZoomBtn.setFont(Font.font(14));
    
    zoomInBtn.setPrefWidth(50);
    zoomOutBtn.setPrefWidth(50);
    resetZoomBtn.setPrefWidth(80);
    
    // Zoom actions
    zoomInBtn.setOnAction(e -> {
        engine.executeScript("zoomIn()");
    });
    
    zoomOutBtn.setOnAction(e -> {
        engine.executeScript("zoomOut()");
    });
    
    resetZoomBtn.setOnAction(e -> {
        engine.executeScript("resetZoom()");
    });
    
    // Zoom button container
    HBox zoomBox = new HBox(10, zoomOutBtn, resetZoomBtn, zoomInBtn);
    zoomBox.setAlignment(Pos.CENTER);
    zoomBox.setPadding(new Insets(5));
    
    // Scroll pane
    ScrollPane scrollPane = new ScrollPane(webView);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setPrefViewportWidth(800);
    scrollPane.setPrefViewportHeight(500);
    scrollPane.setStyle("-fx-background: white; -fx-border-color: #ccc;");
    
    // Navigation buttons
    Button backBtn = new Button("Back");
    Button generateBtn = new Button("Download SVG");
    
    backBtn.setFont(Font.font(16));
    generateBtn.setFont(Font.font(16));
    backBtn.setPrefWidth(120);
    generateBtn.setPrefWidth(160);
    generateBtn.setOpacity(1.0);
    
    HBox navBox = new HBox(20, backBtn, generateBtn);
    navBox.setAlignment(Pos.CENTER);
    navBox.setPadding(new Insets(20));
    
    // Back action
    backBtn.setOnAction(e -> stage.setScene(boxScene));
    
    // Download action
    generateBtn.setOnAction(e -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SVG File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SVG Files", "*.svg")
        );
        
        String defaultName = String.format("Box_%.0fx%.0fx%.0fmm.svg", width, length, height);
        fileChooser.setInitialFileName(defaultName);
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(svgContent);
                writer.close();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("SVG File Saved");
                success.setContentText("Box pattern saved to:\n" + file.getAbsolutePath());
                success.showAndWait();
            } catch (IOException ex) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Failed to save file");
                error.setContentText(ex.getMessage());
                error.showAndWait();
            }
        }
    });
    
    // Main layout
    VBox layout = new VBox(20, title, scrollPane, zoomBox, navBox);
    layout.setAlignment(Pos.CENTER);
    layout.setStyle("-fx-background-color: linear-gradient(to bottom, #00008B, #1d2d41ff);");
    layout.setPadding(new Insets(20));
    
    Scene previewScene = new Scene(layout);
    layout.prefWidthProperty().bind(stage.widthProperty());
    layout.prefHeightProperty().bind(stage.heightProperty());
    return previewScene;
}


private Scene createNotePresetScene(Stage stage) 
{
    Text title = new Text("Choose Notecard Size");
    title.setFont(Font.font("Garamond", 30));
    title.setFill(Color.WHITE);
    
    // ADD MONOGRAM FIELD - Initially disabled
    TextField monogramField = new TextField();
    monogramField.setPromptText("Monogram (optional)");
    monogramField.setMaxWidth(300);
    monogramField.setPrefHeight(40);
    monogramField.setFont(Font.font("Garamond", 16));
    monogramField.setDisable(true); // Initially disabled

    // Three preset buttons with measurements
    Button stickyBtn = new Button("Sticky Note (78 × 78 × 60 mm)");
    Button cardBtn   = new Button("Notecard (130 × 78 × 35 mm)");
    Button backBtn   = new Button("Back");
    Button nextBtn   = new Button("Next");

    // Set widths and fonts
    stickyBtn.setPrefWidth(300);
    cardBtn.setPrefWidth(300);
    backBtn.setPrefWidth(120);
    nextBtn.setPrefWidth(120);

    stickyBtn.setFont(Font.font(18));
    cardBtn.setFont(Font.font(18));
    backBtn.setFont(Font.font(16));
    nextBtn.setFont(Font.font(16));

    // Layout - ADD MONOGRAM FIELD TO LAYOUT
    VBox presetLayout = new VBox(20, title, monogramField, stickyBtn, cardBtn);
    presetLayout.setAlignment(Pos.CENTER);

    HBox navBox = new HBox(20, backBtn, nextBtn);
    navBox.setAlignment(Pos.CENTER);
    navBox.setPadding(new Insets(20));

    VBox layout = new VBox(30, presetLayout, navBox);
    layout.setAlignment(Pos.CENTER);
    layout.setPadding(new Insets(40));
    layout.setStyle("-fx-background-color: linear-gradient(to bottom, #00008B, #1d2d41ff);");

    Scene scene = new Scene(layout);
    layout.prefWidthProperty().bind(stage.widthProperty());
    layout.prefHeightProperty().bind(stage.heightProperty());

    // ----- Button actions -----
    backBtn.setOnAction(e -> stage.setScene(boxScene));

    // Preset buttons store selection AND enable monogram field
    stickyBtn.setOnAction(e -> {
        selectedStyle = "sticky";
        monogramField.setDisable(false); // Enable when preset is selected
        monogramField.setStyle("-fx-opacity: 1.0;"); // Make it fully visible
    });
    
    cardBtn.setOnAction(e -> {
        selectedStyle = "notecard";
        monogramField.setDisable(false); // Enable when preset is selected
        monogramField.setStyle("-fx-opacity: 1.0;"); // Make it fully visible
    });
    
    // Next button goes to preview scene using selected preset
    nextBtn.setOnAction(e -> {
        if (selectedStyle == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a preset first!");
            alert.showAndWait();
            return;
        }
        // GET MONOGRAM FROM TEXT FIELD AND PASS IT
        String monogram = monogramField.getText().trim();
        loadNoteMeasurements(selectedStyle, stage, monogram); // FIXED: Added monogram parameter
    });

    return scene;
}



private void loadNoteMeasurements(String style, Stage stage, String monogram) {
    // Create preview scene with note holder SVG
    Scene previewScene = createNoteHolderPreviewScene(stage, style, monogram);
    stage.setScene(previewScene);
}

private String generateNoteHolderSVG(String style, String monogram) {
    try {
        int noteStyle;
        
        // Map your style names to NoteHolderBox constants
        if (style.equals("sticky")) {
            noteStyle = NoteHolderBox.STICKY_NOTE_HOLDER;  // 86x86x60mm
        } else if (style.equals("notecard")) {
            noteStyle = NoteHolderBox.INDEXCARD_HOLDER;    // 138x86x60mm
        } else {
            noteStyle = NoteHolderBox.STICKY_NOTE_HOLDER;  // default
        }
        
        // Create NoteHolderBox instance
        NoteHolderBox noteHolder = new NoteHolderBox(noteStyle, monogram);
        
        // Return the generated SVG
        return noteHolder.generateSVG();
    } catch (Exception e) {
        e.printStackTrace();
        return "<svg><text x='20' y='50'>Error generating note holder SVG</text></svg>";
    }
}

private Scene createNoteHolderPreviewScene(Stage stage, String style, String monogram) {
    Text title = new Text("Note Holder Preview - Ready for Laser Cutting");
    title.setFont(Font.font("Garamond", 30));
    title.setFill(Color.WHITE);
    
    // Generate SVG using NoteHolderBox
    String svgContent = generateNoteHolderSVG(style, monogram);
    
    // Create a web view
    WebView webView = new WebView();
    WebEngine engine = webView.getEngine();
    
    // HTML with zoom control (same as before)
    String html = "<html><head><style>"
                + "body, html { margin:0; padding:0; width:100%; height:100%; overflow:auto; background:white; }"
                + "#svg-container { width:100%; height:100%; text-align:center; padding:20px; }"
                + "svg { transition: transform 0.3s; }"
                + "</style></head><body>"
                + "<div id='svg-container'>" + svgContent + "</div>"
                + "<script>"
                + "let scale = 1.0;"
                + "function zoomIn() { scale *= 1.2; updateScale(); }"
                + "function zoomOut() { scale /= 1.2; updateScale(); }"
                + "function resetZoom() { scale = 1.0; updateScale(); }"
                + "function updateScale() { document.querySelector('svg').style.transform = 'scale(' + scale + ')'; }"
                + "</script>"
                + "</body></html>";
    
    engine.loadContent(html);
    
    // Zoom buttons (same as before)
    Button zoomInBtn = new Button("+");
    Button zoomOutBtn = new Button("-");
    Button resetZoomBtn = new Button("Reset");
    
    zoomInBtn.setFont(Font.font(20));
    zoomOutBtn.setFont(Font.font(20));
    resetZoomBtn.setFont(Font.font(14));
    
    zoomInBtn.setPrefWidth(50);
    zoomOutBtn.setPrefWidth(50);
    resetZoomBtn.setPrefWidth(80);
    
    // Zoom actions
    zoomInBtn.setOnAction(e -> {
        engine.executeScript("zoomIn()");
    });
    
    zoomOutBtn.setOnAction(e -> {
        engine.executeScript("zoomOut()");
    });
    
    resetZoomBtn.setOnAction(e -> {
        engine.executeScript("resetZoom()");
    });
    
    // Zoom button container
    HBox zoomBox = new HBox(10, zoomOutBtn, resetZoomBtn, zoomInBtn);
    zoomBox.setAlignment(Pos.CENTER);
    zoomBox.setPadding(new Insets(5));
    
    // Scroll pane
    ScrollPane scrollPane = new ScrollPane(webView);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setPrefViewportWidth(800);
    scrollPane.setPrefViewportHeight(500);
    scrollPane.setStyle("-fx-background: white; -fx-border-color: #ccc;");
    
    // Navigation buttons
    Button backBtn = new Button("Back");
    Button generateBtn = new Button("Download SVG");
    
    backBtn.setFont(Font.font(16));
    generateBtn.setFont(Font.font(16));
    backBtn.setPrefWidth(120);
    generateBtn.setPrefWidth(160);
    generateBtn.setOpacity(1.0);
    
    HBox navBox = new HBox(20, backBtn, generateBtn);
    navBox.setAlignment(Pos.CENTER);
    navBox.setPadding(new Insets(20));
    
    // Back action - goes back to note preset scene
    backBtn.setOnAction(e -> stage.setScene(createNotePresetScene(stage)));
    
    // Download action
    generateBtn.setOnAction(e -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SVG File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SVG Files", "*.svg")
        );
        
        // Create filename based on style
        String defaultName;
        if (style.equals("sticky")) {
            defaultName = "Sticky_Note_Holder.svg";
        } else {
            defaultName = "Notecard_Holder.svg";
        }
        
        fileChooser.setInitialFileName(defaultName);
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(svgContent);
                writer.close();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("SVG File Saved");
                success.setContentText("Note holder pattern saved to:\n" + file.getAbsolutePath());
                success.showAndWait();
            } catch (IOException ex) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Failed to save file");
                error.setContentText(ex.getMessage());
                error.showAndWait();
            }
        }
    });
    
    // Main layout
    VBox layout = new VBox(20, title, scrollPane, zoomBox, navBox);
    layout.setAlignment(Pos.CENTER);
    layout.setStyle("-fx-background-color: linear-gradient(to bottom, #00008B, #1d2d41ff);");
    layout.setPadding(new Insets(20));
    
    Scene previewScene = new Scene(layout);
    layout.prefWidthProperty().bind(stage.widthProperty());
    layout.prefHeightProperty().bind(stage.heightProperty());
    return previewScene;
}
private String generateSlidingBoxSVG(double width, double depth, double height, double toothWidth, String monogram) {
    try {
        // Create SlidingBox instance
        SlidingBox slidingBox = new SlidingBox(width, depth, height, toothWidth, monogram);
        
        // Return the generated SVG
        return slidingBox.generateSVG();
    } catch (Exception e) {
        e.printStackTrace();
        return "<svg><text x='20' y='50'>Error generating sliding box SVG</text></svg>";
    }
}

private Scene createSlidingBoxPreviewScene(Stage stage, double width, double depth, double height, double toothWidth, String monogram) {
    Text title = new Text("Sliding Box Preview - Ready for Laser Cutting");
    title.setFont(Font.font("Garamond", 30));
    title.setFill(Color.WHITE);
    
    // Generate SVG using SlidingBox
    String svgContent = generateSlidingBoxSVG(width, depth, height, toothWidth, monogram);
    
    // Create a web view
    WebView webView = new WebView();
    WebEngine engine = webView.getEngine();
    
    // HTML with zoom control (same as before)
    String html = "<html><head><style>"
                + "body, html { margin:0; padding:0; width:100%; height:100%; overflow:auto; background:white; }"
                + "#svg-container { width:100%; height:100%; text-align:center; padding:20px; }"
                + "svg { transition: transform 0.3s; }"
                + "</style></head><body>"
                + "<div id='svg-container'>" + svgContent + "</div>"
                + "<script>"
                + "let scale = 1.0;"
                + "function zoomIn() { scale *= 1.2; updateScale(); }"
                + "function zoomOut() { scale /= 1.2; updateScale(); }"
                + "function resetZoom() { scale = 1.0; updateScale(); }"
                + "function updateScale() { document.querySelector('svg').style.transform = 'scale(' + scale + ')'; }"
                + "</script>"
                + "</body></html>";
    
    engine.loadContent(html);
    
    // Zoom buttons (same as before)
    Button zoomInBtn = new Button("+");
    Button zoomOutBtn = new Button("-");
    Button resetZoomBtn = new Button("Reset");
    
    zoomInBtn.setFont(Font.font(20));
    zoomOutBtn.setFont(Font.font(20));
    resetZoomBtn.setFont(Font.font(14));
    
    zoomInBtn.setPrefWidth(50);
    zoomOutBtn.setPrefWidth(50);
    resetZoomBtn.setPrefWidth(80);
    
    // Zoom actions
    zoomInBtn.setOnAction(e -> {
        engine.executeScript("zoomIn()");
    });
    
    zoomOutBtn.setOnAction(e -> {
        engine.executeScript("zoomOut()");
    });
    
    resetZoomBtn.setOnAction(e -> {
        engine.executeScript("resetZoom()");
    });
    
    // Zoom button container
    HBox zoomBox = new HBox(10, zoomOutBtn, resetZoomBtn, zoomInBtn);
    zoomBox.setAlignment(Pos.CENTER);
    zoomBox.setPadding(new Insets(5));
    
    // Scroll pane
    ScrollPane scrollPane = new ScrollPane(webView);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setPrefViewportWidth(800);
    scrollPane.setPrefViewportHeight(500);
    scrollPane.setStyle("-fx-background: white; -fx-border-color: #ccc;");
    
    // Navigation buttons
    Button backBtn = new Button("Back");
    Button generateBtn = new Button("Download SVG");
    
    backBtn.setFont(Font.font(16));
    generateBtn.setFont(Font.font(16));
    backBtn.setPrefWidth(120);
    generateBtn.setPrefWidth(160);
    generateBtn.setOpacity(1.0);
    
    HBox navBox = new HBox(20, backBtn, generateBtn);
    navBox.setAlignment(Pos.CENTER);
    navBox.setPadding(new Insets(20));
    
    // Back action - goes back to dimension scene
    backBtn.setOnAction(e -> stage.setScene(createDimensionScene(stage)));
    
    // Download action
    generateBtn.setOnAction(e -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SVG File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SVG Files", "*.svg")
        );
        
        // Create filename based on dimensions
        String defaultName = String.format("SlidingBox_%.0fx%.0fx%.0fmm.svg", width, depth, height);
        fileChooser.setInitialFileName(defaultName);
        
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(svgContent);
                writer.close();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setHeaderText("SVG File Saved");
                success.setContentText("Sliding box pattern saved to:\n" + file.getAbsolutePath());
                success.showAndWait();
            } catch (IOException ex) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Failed to save file");
                error.setContentText(ex.getMessage());
                error.showAndWait();
            }
        }
    });
    
    // Main layout
    VBox layout = new VBox(20, title, scrollPane, zoomBox, navBox);
    layout.setAlignment(Pos.CENTER);
    layout.setStyle("-fx-background-color: linear-gradient(to bottom, #00008B, #1d2d41ff);");
    layout.setPadding(new Insets(20));
    
    Scene previewScene = new Scene(layout);
    layout.prefWidthProperty().bind(stage.widthProperty());
    layout.prefHeightProperty().bind(stage.heightProperty());
    return previewScene;
}

private void generateSVGFile(int boxStyle, double width, double depth, double height, double toothWidth) {
    // Build the input string for svgFileGenerator
    String input = boxStyle + "\n" + width + "\n" + depth + "\n" + height + "\n" + toothWidth + "\n";

    // Convert the string into an InputStream
    ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
    System.setIn(in);

    // Run svgFileGenerator in a separate thread
    new Thread(() -> {
        try {
            SimpleBox.main(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}
private String generateSimpleBoxSVG(double width, double height, double depth, double toothWidth, String monogram) {
    try {
        // Call the new method
        return SimpleBox.generateBoxSVG( width,  height,  depth,  toothWidth,  monogram);
    } catch (Exception e) {
        e.printStackTrace();
        return "<svg><text x='20' y='50'>Error generating SVG</text></svg>";
    }
}
}