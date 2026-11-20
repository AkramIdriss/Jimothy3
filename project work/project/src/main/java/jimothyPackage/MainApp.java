/*

MainApp.java
makes the thing run

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/
package jimothypackage;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.Scanner;

import java.nio.charset.StandardCharsets;

import java.util.List;

import engraving.CommandLine;
import engraving.EngravingMenu;
import engraving.EngravingPlan;
import engraving.Prompter;
import engraving.SvgAsciiPreview;


public class MainApp
{
    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true"); // engraving uses fonts/images only; never open a window

        // everything typed on one line, e.g. "simple 100 80 60 5 --text Hello"
        if(args.length > 0)
        {
            runOneLine(args);
            return;
        }

        // variables for all boxes
        double length; // length of the box in mm
        double width; // width of the box in mm
        double height; // height of the box in mm
        double teeth; // length of the teeth in mm
        EngravingPlan engravings = EngravingPlan.none(); // text, logos and pictures, and which panel each goes on
        
        //scanner - to take
        Scanner scanner = new Scanner(System.in);
        Prompter prompter = new Prompter(scanner); // asks the engraving questions using the same scanner
        // Scanner scanner = new Scanner(System.in);

        System.out.println("Pick a style of box:\r\n" + "[1] based (simple) box\r\n" + "[2] sticky note / notecard box\r\n" + "[3] sliding box\r\n" + "[4] crate\r\n" + "[5] hinge box\r\n" + "[6] flex box\n");
        String whatIsIt = scanner.nextLine().toLowerCase(); // reads what box the user wants
        System.out.println("\n");

        // having whatIsIt be read to determine the box type, each is realted to the println above
        
        // based (simple) box
        if(whatIsIt.contains("1") || whatIsIt.contains("based") || whatIsIt.contains("simple"))
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\nNote: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                engravings = EngravingMenu.ask(prompter, SimpleBox.ENGRAVABLE_PANELS);
            }


            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engravings);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
            showResult(svgContent, engravings, true);
        }

        // sticky note / notecard box
        else if(whatIsIt.contains("2") || whatIsIt.contains("sticky") || whatIsIt.contains("note"))
        {
            System.out.println("Would you like to engrave it?\n");
            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("yes")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                engravings = EngravingMenu.ask(prompter, NoteHolderBox.ENGRAVABLE_PANELS);
            }
            
            // determine what type of holder it is
            System.out.println("Choose your note holder style:\n");
            System.out.println("[1] Sticky Note Holder (86 × 86 × 60 mm)");
            System.out.println("[2] Index Card Holder (138 × 86 × 60 mm)\n");
            String style = scanner.nextLine().trim().toLowerCase();
            System.out.println("\n");
            
            // actually make an SVG
            String svgContent = generateNoteHolderSVG(style, engravings);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
            showResult(svgContent, engravings, true);
        }

        // sliding box
        else if(whatIsIt.contains("3") || whatIsIt.contains("slid")) // this is "slid" incase they write a variant of the word, it is still differentiable this way.
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                engravings = EngravingMenu.ask(prompter, SlidingBox.ENGRAVABLE_PANELS);
            }
            
            
            // actually make an SVG 

            String svgContent = generateSlidingBoxSVG(width, length, height, teeth, engravings);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
            showResult(svgContent, engravings, true);
        }

        // crate - does not yet work
        else if(whatIsIt.contains("4") || whatIsIt.contains("crate"))
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                engravings = EngravingMenu.ask(prompter, SimpleBox.ENGRAVABLE_PANELS);
            }
            
            
            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engravings);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
            showResult(svgContent, engravings, true);
        }

        // hinge box - does not yet work
        else if(whatIsIt.contains("5") || whatIsIt.contains("hinge"))
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                engravings = EngravingMenu.ask(prompter, SimpleBox.ENGRAVABLE_PANELS);
            }
            
            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engravings);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");

            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
            showResult(svgContent, engravings, true);
        }

        // flex box - does not yet work
        else if(whatIsIt.contains("6") || whatIsIt.contains("flex"))
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                engravings = EngravingMenu.ask(prompter, SimpleBox.ENGRAVABLE_PANELS);
            }
            
            
            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engravings);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
            showResult(svgContent, engravings, true);
        }
        else
        {
            System.out.println("That is not an applicable response");
        }

        scanner.close();
    }



    private static String generateNoteHolderSVG(String style, EngravingPlan engravings) 
    {
        if (style.contains("1") || style.contains("sticky") || style.contains("note")) 
        {
            return NoteHolderBox.generateBoxSVG(1, engravings);
        }
        else if (style.contains("2") || style.contains("index") || style.contains("card")) 
        {
            return NoteHolderBox.generateBoxSVG(2, engravings);
        }
        else
        {
            System.out.println("Not a valid input (NoteHolderBox style).");
            return "";
        }
    }

    private static String generateSlidingBoxSVG(double width, double depth, double height, double toothWidth, EngravingPlan engravings) 
    {
        return SlidingBox.generateBoxSVG(width, height, depth, toothWidth, engravings);
    }

    private static String generateSimpleBoxSVG(double width, double depth, double height, double toothWidth, EngravingPlan engravings) 
    {
        return SimpleBox.generateBoxSVG(width, height, depth, toothWidth, engravings);
    }

    // one-line mode: MainApp BOX numbers... [engraving options]
    private static final String ONE_LINE_USAGE = """
            One-line usage:
              simple  LENGTH WIDTH HEIGHT TOOTH [options]
              sliding LENGTH WIDTH HEIGHT TOOTH [options]
              note    STYLE [options]          (STYLE: 1 = sticky note, 2 = index card)
            Example:
              simple 100 80 60 5 --text "Sarah's|Recipes" --font serif
            The file is saved in your Downloads folder.
            Panels for --on: simple: %s
                             sliding: %s
                             note: %s
            """.formatted(String.join(", ", dashed(SimpleBox.ENGRAVABLE_PANELS)),
                          String.join(", ", dashed(SlidingBox.ENGRAVABLE_PANELS)),
                          String.join(", ", dashed(NoteHolderBox.ENGRAVABLE_PANELS)));

    private static List<String> dashed(List<String> panels)
    {
        return panels.stream().map(p -> p.replace(' ', '-')).toList();
    }

    private static void runOneLine(String[] args)
    {
        String box = args[0].toLowerCase();
        String[] rest = java.util.Arrays.copyOfRange(args, 1, args.length);

        try
        {
            if(box.equals("--help") || box.equals("-h") || box.equals("help"))
            {
                System.out.println(ONE_LINE_USAGE + "\n" + CommandLine.optionsHelp(SimpleBox.ENGRAVABLE_PANELS));
                return;
            }

            String svgContent;
            String fileName;
            CommandLine line;

            if(box.contains("simple") || box.equals("1"))
            {
                line = CommandLine.parse(rest, SimpleBox.ENGRAVABLE_PANELS);
                if(line.help()) { System.out.println(ONE_LINE_USAGE + "\n" + CommandLine.optionsHelp(SimpleBox.ENGRAVABLE_PANELS)); return; }
                double[] n = line.positiveNumbers("length", "width", "height", "tooth width");
                svgContent = generateSimpleBoxSVG(n[1], n[0], n[2], n[3], line.plan());
                fileName = String.format("simple_box_%.0fx%.0fx%.0f_t%.0f", n[0], n[1], n[2], n[3]);
            }
            else if(box.contains("slid") || box.equals("3"))
            {
                line = CommandLine.parse(rest, SlidingBox.ENGRAVABLE_PANELS);
                if(line.help()) { System.out.println(ONE_LINE_USAGE + "\n" + CommandLine.optionsHelp(SlidingBox.ENGRAVABLE_PANELS)); return; }
                double[] n = line.positiveNumbers("length", "width", "height", "tooth width");
                svgContent = generateSlidingBoxSVG(n[1], n[0], n[2], n[3], line.plan());
                fileName = String.format("sliding_box_%.0fx%.0fx%.0f_t%.0f", n[0], n[1], n[2], n[3]);
            }
            else if(box.contains("note") || box.contains("sticky") || box.equals("2"))
            {
                line = CommandLine.parse(rest, NoteHolderBox.ENGRAVABLE_PANELS);
                if(line.help()) { System.out.println(ONE_LINE_USAGE + "\n" + CommandLine.optionsHelp(NoteHolderBox.ENGRAVABLE_PANELS)); return; }
                if(line.positional().size() != 1)
                {
                    throw new IllegalArgumentException("note needs one STYLE: 1 (sticky note) or 2 (index card)");
                }
                String style = line.positional().get(0).toLowerCase();
                svgContent = generateNoteHolderSVG(style, line.plan());
                if(svgContent.isEmpty())
                {
                    throw new IllegalArgumentException("unknown note holder style: " + style);
                }
                fileName = "note_holder_" + style;
            }
            else
            {
                throw new IllegalArgumentException("unknown box type: " + args[0] + " (use simple, sliding or note)");
            }

            saveSVG(fileName, svgContent);
            showResult(svgContent, line.plan(), line.preview());
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e.getMessage() + "\n");
            System.out.println(ONE_LINE_USAGE);
            System.exit(1);
        }
    }

    // lists the engravings and draws the box in the terminal
    private static void showResult(String svgContent, EngravingPlan engravings, boolean preview)
    {
        if(svgContent == null || svgContent.isEmpty()) // nothing was made (bad measurements)
        {
            return;
        }
        for(String line : engravings.summary())
        {
            System.out.println("Engraving on " + line);
        }
        if(preview)
        {
            SvgAsciiPreview.print(svgContent);
        }
    }

    private static void saveSVG(String filePath, String svgContent) 
    {
        if (!filePath.toLowerCase().endsWith(".svg")) // if it doesn't end in .svg, make it do so
        {
            filePath = filePath + ".svg";
        }

        filePath = System.getProperty("user.home") + "/Downloads/" + filePath; // get the absolute path of the item and send it to "Downloads" - make sure it is forward slashes (/), Windows will translate but Mac doesnt and it uses forward

        File file = new File(filePath);
        File parentDir = file.getParentFile();

        // Automatically create parent folders if they don't exist yet
        if (parentDir != null && !parentDir.exists()) 
        {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) 
        {
            writer.write(svgContent);
            System.out.println("SVG saved successfully to: " + file.getAbsolutePath());

        } 
        catch (IOException e) 
        {
            System.err.println("Failed to save SVG file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}