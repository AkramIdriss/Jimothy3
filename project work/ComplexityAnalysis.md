/*

ComplexityAnalysis.md
McCabe (cyclomatic) complexity of every box in the project

Sarah Rawls - smr23a
Akram Idriss - ami22a
Isaac England - ile22a

*/

# Complexity Analysis

> **Note:** These numbers were measured on 2026-11-02. They will change as we refactor and add new boxes.

## What is McCabe Complexity?

McCabe complexity (also called **cyclomatic complexity**, or **CCN**) was created by Thomas McCabe in 1976. It counts the number of independent paths through a piece of code.

- Every function starts at **1**.
- Each decision point adds **1**: `if`, `else if`, `for`, `while`, `case`, `catch`, `&&`, `||`, and `?:`.

A higher number means the function has more paths to understand and more cases to test. A function with CCN 10 needs at least 10 test cases to cover every path.

| CCN | Meaning |
|-----|---------|
| 1–10 | Simple, low risk |
| 11–20 | Moderately complex, worth watching |
| 21–50 | Complex, high risk |
| 50+ | Untestable, should be split up |

## How We Measured It

We used [Lizard](https://github.com/terryyin/lizard), a free code complexity analyzer. From `project work/project`:

```bash
pip install lizard
lizard src/main/java
```

Lizard flags any function with a CCN over **15**.

| Column | Meaning |
|--------|---------|
| **NLOC** | Lines of code, not counting comments or blank lines |
| **CCN** | Cyclomatic (McCabe) complexity |
| **Params** | Number of parameters the function takes |

## Summary by Box

| Box | Functions | Total NLOC | Avg CCN | Highest CCN | Functions over 15 |
|-----|-----------|------------|---------|-------------|-------------------|
| Simple Box | 6 | 529 | **13.7** | 20 | 4 |
| Sliding Box | 18 | 776 | 6.5 | 19 | 3 |
| Note Holder | 10 | 441 | 2.7 | 7 | 0 |
| **All boxes** | **34** | **1746** | **6.6** | **20** | **7** |

## Simple Box (`SimpleBox.java`)

| Function | NLOC | CCN | Params |
|----------|------|-----|--------|
| `makeFrontPanelPath` | 108 | **20** ⚠️ | 5 |
| `makeSidePanelPath` | 108 | **20** ⚠️ | 5 |
| `makeBottomPanelPath` | 97 | **16** ⚠️ | 5 |
| `makeTopPanelPath` | 95 | **16** ⚠️ | 5 |
| `main` | 34 | 7 | 1 |
| `generateBoxSVG` | 84 | 3 | 5 |

## Sliding Box (`SlidingBox.java`)

| Function | NLOC | CCN | Params |
|----------|------|-----|--------|
| `makeFrontPanelPath` | 107 | **19** ⚠️ | 5 |
| `makeSidePanelPath` | 104 | **18** ⚠️ | 4 |
| `makeTopPanelPath` | 102 | **17** ⚠️ | 5 |
| `makeInnerBottomPanelPath` | 85 | 14 | 2 |
| `makeInnerFrontPanelPath` | 77 | 12 | 2 |
| `makeInnerLeftSidePanelPath` | 77 | 12 | 2 |
| `main` | 35 | 8 | 1 |
| `generateSVG` | 112 | 5 | 0 |
| `calculateFontSize` | 16 | 3 | 3 |
| `SlidingBox` (constructor) | 10 | 1 | 5 |
| `makeInnerRightSidePanelPath` | 14 | 1 | 2 |
| `makeOuterFrontPanelPath` | 3 | 1 | 2 |
| `makeOuterBackPanelPath` | 3 | 1 | 2 |
| `makeOuterSidePanelPath` | 3 | 1 | 2 |
| `makeOuterTopPanelPath` | 3 | 1 | 2 |
| `makeOuterBottomPanelPath` | 3 | 1 | 2 |
| `makeInnerBackPanelPath` | 3 | 1 | 2 |
| `saveToFile` | 5 | 1 | 1 |

## Note Holder (`NoteHolderBox.java`)

| Function | NLOC | CCN | Params |
|----------|------|-----|--------|
| `main` | 37 | 7 | 1 |
| `makeBottomPanelNHPath` | 71 | 4 | 2 |
| `setDimensionsFromStyle` | 20 | 3 | 1 |
| `calculateFontSize` | 16 | 3 | 3 |
| `generateSVG` | 65 | 3 | 0 |
| `makeBackPanelNHPath` | 81 | 2 | 2 |
| `makeSidePanelNHPath` | 76 | 2 | 2 |
| `NoteHolderBox` (constructor) | 5 | 1 | 2 |
| `makeFrontPanelNHPath` | 52 | 1 | 2 |
| `saveToFile` | 5 | 1 | 1 |

## What We Found

1. **All 7 functions over the limit build finger-joint panels.** Working out where each tooth goes along an edge takes many `if` and loop decisions.
2. **The Simple Box has the most complex code**, even though it is the simplest box. Its average CCN (13.7) is about twice the Sliding Box's and five times the Note Holder's.
3. **`SimpleBox.makeFrontPanelPath` and `makeSidePanelPath` look duplicated.** They match on every measurement (108 NLOC, CCN 20, 970 tokens). The four Simple Box panel functions probably repeat the same tooth logic.
4. **The Note Holder shows a lower-complexity approach works.** Its panel functions score 1–4 while building the same kind of joints.

## Plans to Lower Complexity

- Pull the shared finger-joint logic into one helper (for example, `makeToothedEdge(length, toothWidth, ...)`). Each panel can then call it once per edge.
- Merge `SimpleBox.makeFrontPanelPath` and `makeSidePanelPath` into one function.
- Re-run Lizard after each change and update this file. The goal is **no functions over 15**.
- Measure each new box (Crate, Hinge, Flex) with Lizard as it is added.
