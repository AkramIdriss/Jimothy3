# Glossary for the Laser Box Project

> **Note:** This glossary is a work in progress. It will change as we learn more, and the list of terms is not complete yet.

| Term | Definition |
|------|------------|
| **Cyclomatic Complexity (CCN)** | A number that counts how many different paths the code can take through a function. Each function starts at 1, and every decision point adds 1. Higher numbers mean the code is harder to understand and needs more tests. See our [Complexity Analysis](ComplexityAnalysis.md). |
| **Decision Point** | A place where the code can go more than one way, such as an `if`, `else if`, `for`, `while`, `case`, `catch`, `&&`, or `\|\|`. Each one adds 1 to the cyclomatic complexity. |
| **Engraving / Monogram** | Text (such as a name or initials) that the laser marks on a panel without cutting through it. In our app, this text is converted to uppercase. |
| **Finger Joint (Tooth)** | The interlocking rectangular "teeth" along a panel's edges that let the panels slot together into a box. |
| **Kerf** | The width of material the laser beam burns away as it cuts. In practice, this is the thickness of the laser beam's cut line. |
| **Kerf Compensation** | Shifting the laser's cutting path to make up for the kerf, so the parts come out the correct size and the joints fit snugly. |
| **Laser Cutting (Vector Cutting)** | Cutting all the way through the wood along a path. |
| **Lizard** | A free tool that measures the cyclomatic complexity and length of every function in our code. We use it for our Complexity Analysis. |
| **Material Thickness** | How thick the wood or sheet is. The Sliding Box assumes 3 mm material. |
| **McCabe Complexity** | Another name for cyclomatic complexity, named after Thomas McCabe, who created it in 1976. |
| **NLOC** | Number of Lines Of Code, not counting comments or blank lines. Lizard reports it for each function. |
| **Panel** | One flat piece of the box (front, back, side, top, or bottom). The SVG lays out all the panels together. |
| **Raster** | An image made of pixels. Raster engraving moves the laser back and forth line by line to engrave an image or filled area onto the surface. |
| **Refactoring** | Changing how code is written without changing what it does, such as merging duplicate functions to lower their complexity. |
| **Scoring (Laser Vector Scoring)** | Marking the wood along a path without cutting all the way through it. |
| **SVG** | Scalable Vector Graphics, the file type the laser cutter accepts and the format our app outputs. |
| **Tooth Width** | How wide each finger-joint tooth is, in millimeters. |
| **Vector** | A drawing made of lines and paths (not pixels) that tells the laser exactly where to cut or score. SVG is a vector format. |
