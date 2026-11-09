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


public class MainApp
{
    public static void main(String[] args)
    {
        // variables for all boxes
        double length; // length of the box in mm
        double width; // width of the box in mm
        double height; // height of the box in mm
        double teeth; // length of the teeth in mm
        String engraving = ""; // dont forget to trim() later!
        // there may be an image var here later!
        
        //scanner - to take
        Scanner scanner = new Scanner(System.in);
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

            // this currently relates to nothing! fix it later!
            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                System.out.println("What would you like to engrave?");
            }


            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engraving);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
        }

        // sticky note / notecard box
        else if(whatIsIt.contains("2") || whatIsIt.contains("sticky") || whatIsIt.contains("note"))
        {
            System.out.println("Would you like to engrave it?\n");
            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("yes")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                System.out.println("What would you like to engrave?");
            }
            
            // determine what type of holder it is
            System.out.println("Choose your note holder style:\n");
            System.out.println("[1] Sticky Note Holder (86 × 86 × 60 mm)");
            System.out.println("[2] Index Card Holder (138 × 86 × 60 mm)\n");
            String style = scanner.nextLine().trim().toLowerCase();
            System.out.println("\n");
            
            // actually make an SVG
            generateNoteHolderSVG(style, engraving);

            String svgContent = generateNoteHolderSVG(style, engraving);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
        }

        // sliding box
        else if(whatIsIt.contains("3") || whatIsIt.contains("slid")) // this is "slid" incase they write a variant of the word, it is still differentiable this way.
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            // this currently relates to nothing! fix it later!
            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                System.out.println("What would you like to engrave?");
            }
            
            
            // actually make an SVG 

            String svgContent = generateSlidingBoxSVG(width, length, height, teeth, engraving);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
        }

        // crate - does not yet work
        else if(whatIsIt.contains("4") || whatIsIt.contains("crate"))
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            // this currently relates to nothing! fix it later!
            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                System.out.println("What would you like to engrave?");
            }
            
            
            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engraving);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
        }

        // hinge box - does not yet work
        else if(whatIsIt.contains("5") || whatIsIt.contains("hinge"))
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            // this currently relates to nothing! fix it later!
            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                System.out.println("What would you like to engrave?");
            }
            
            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engraving);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");

            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
        }

        // flex box - does not yet work
        else if(whatIsIt.contains("6") || whatIsIt.contains("flex"))
        {
            System.out.println("Enter all necessary measurements:\nFormat: \"length width height toothWidth (optional) engraving\"\n Note: typing the word \"engraving\" will take you to a different screen.\n");
            
            length = scanner.nextDouble();
            width = scanner.nextDouble();
            height = scanner.nextDouble();
            teeth = scanner.nextDouble();

            // this currently relates to nothing! fix it later!
            boolean engraveQuestion = ((scanner.nextLine().trim().toLowerCase()).contains("engrav")) ? true : false; // if the user wants engraving, this will be true; else false
            System.out.println("\n");

            if(engraveQuestion)
            {
                System.out.println("What would you like to engrave?");
            }
            
            
            // actually make an SVG 

            String svgContent = generateSimpleBoxSVG(width, length, height, teeth, engraving);
            
            System.out.println("Enter a file name, or type nothing to automatically generate one:\n");
            String fileName = scanner.nextLine().trim();
            System.out.println("\n");
            
            if(fileName.equals("")) // if it is empty, give it a name
            {
                fileName = "fileFinalRealV2-DoNotOverwrite(2).svg";
            }

            saveSVG(fileName, svgContent);
        }
        else
        {
            System.out.println("That is not an applicable response");
        }

        scanner.close();
    }



    private static String generateNoteHolderSVG(String style, String engraving) 
    {
        if (style.contains("1") || style.contains("sticky") || style.contains("note")) 
        {
            return NoteHolderBox.generateBoxSVG(1, engraving);
        }
        else if (style.contains("2") || style.contains("index") || style.contains("card")) 
        {
            return NoteHolderBox.generateBoxSVG(2, engraving);
        }
        else
        {
            System.out.println("Not a valid input (NoteHolderBox style).");
            return "";
        }
    }

    private static String generateSlidingBoxSVG(double width, double depth, double height, double toothWidth, String engraving) 
    {
        return SlidingBox.generateBoxSVG(width, height, depth, toothWidth, engraving);
    }

    private static String generateSimpleBoxSVG(double width, double depth, double height, double toothWidth, String engraving) 
    {
        return SimpleBox.generateBoxSVG(width, height, depth, toothWidth, engraving);
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