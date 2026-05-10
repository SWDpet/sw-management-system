# Shade — Design Reference

> High-contrast utility with violet accent. A clean white canvas underpins sharp black text and functional components, highlighted by a single, vivid violet.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://shade.inc](https://shade.inc) |
| Refero page | [https://styles.refero.design/style/e549766e-b8b1-48a2-bd72-8cc04e9e4e9d](https://styles.refero.design/style/e549766e-b8b1-48a2-bd72-8cc04e9e4e9d) |
| Theme | light |
| Industry | media |

## Overview

Shade employs a stark, high-contrast visual language with a focus on functional typography and precise spacing. The design uses a dominant neutral palette relieved by a single vibrant violet accent. Components feature sharp edges or large, bold radii, often with strong box shadows for emphasis, conveying a sense of directness and polished utility. Surfaces are generally flat, with subtle elevation coming from shadows rather than color shifts.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, UI elements requiring high contrast with text |
| Midnight Ink | `#000000` | neutral | Primary text, borders, high-contrast outlines, icons |
| Coal Black | `#131315` | neutral | Strong headings, button backgrounds, elevated surface backgrounds where text needs high contrast |
| Light Fog | `#f1f1f1` | neutral | Subtle background for UI elements, button box shadows, soft divider lines |
| Steel Gray | `#717173` | neutral | Muted body text, secondary information, inactive states, subtle element backgrounds |
| Stone Gray | `#a0a0a0` | neutral | Placeholder text, subtle box shadows, light borders |
| Warm Gray | `#d0d0d0` | neutral | Hairline separators, very light element backgrounds, subtle shadow tints |
| Deep Gray | `#444444` | neutral | Input text, input borders, less prominent body text |
| Dark Gray | `#333333` | neutral | Cookie banner text, secondary button borders, button text |
| Digital Violet Light | `#dacefd` | accent | Subtle border accent for UI elements, supporting visual cues |
| Digital Violet Bold | `#6941c6` | accent | Violet outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Digital Violet Vivid | `#855cf8` | accent | Decorative background fills, active states, conical gradients for specific elements |

## Typography

### Inter Display

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 10px, 14px, 16px, 18px, 20px, 21px, 24px, 28px, 32px, 36px, 40px, 48px, 56px, 72px |
| lineHeight | 1.00, 1.10, 1.15, 1.20, 1.22, 1.25, 1.30, 1.40, 1.43, 1.57 |
| letterSpacing | -0.0300em at large sizes, -0.0100em at smaller sizes |
| fontFeatureSettings | "blwf", "cv03", "cv04", "cv09", "cv11"; "ss01", "ss07", "ss08" |
| role | Primary headings and body text, providing a distinctive, tightly spaced feel for key content. The custom features tune its appearance for legibility and aesthetic. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| role | Fallback for general UI text, links, and buttons, ensuring basic content readability without demanding specific font support. Used where Inter Display is not explicitly applied. |

### Aux Mono Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.00, 1.29 |
| letterSpacing | -0.0400em at 14px, -0.0100em |
| role | Monospaced text for specific data points or code examples, offering a technical and precise feel. The tight letter-spacing emphasizes its distinct character. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px |
| lineHeight | 1.00, 1.20, 1.50 |
| letterSpacing | normal |
| substitute | Inter |
| role | General body text, card content, and secondary button labels, providing a highly legible and versatile textual layer. This standard variant of Inter balances readability with the custom Inter Display. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.43 |  |
| body | 14 |  | 1.29 | -0.48 |
| subheading | 18 |  | 1.22 |  |
| heading-sm | 24 |  | 1.25 |  |
| heading | 32 |  | 1.4 |  |
| heading-lg | 40 |  | 1.15 |  |
| display | 56 |  | 1.1 | -1.68 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 14px |
| input | 9px |
| buttons | 20px |
| default | 9px |
| largeButtons | 35px |

- **elementGap** — 10px
- **sectionGap** — 100px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Base page background and high-contrast surfaces. |
| Light Fog | `#f1f1f1` | 1 | Subtle background for grouped UI elements or light separators. |
| Default Card | `#ffffff` | 2 | Default card background, distinguished by its subtle inset border shadow. |
| Coal Black Elevated | `#131315` | 3 | Darker background for primary filled buttons and specific elevated UI sections. |

## Components

### Primary Filled Button

**Role:** Main call-to-action button for initiating key flows.

Background: Coal Black (#131315), Text: Canvas White (#ffffff). Padding: 20px all sides. Border Radius: 20px. Uses a sharp, pronounced box-shadow below.

### Secondary Outlined Button

**Role:** Secondary call-to-action, less prominent than the filled primary.

Background: Canvas White (#ffffff), Text: Midnight Ink (#000000). Border: 1px solid Midnight Ink (#000000). Padding: 20px all sides. Border Radius: 0px. A square, minimalist aesthetic.

### Ghost Accent Button

**Role:** Minimal impact button, often for navigation or secondary actions, using brand accent.

Background: transparent, Text: Digital Violet Bold (#6941c6). Border: 1px solid Digital Violet Bold (#6941c6). Padding: 10px top/bottom, 15px left/right. Border Radius: 0px. Used for 'Shade Raises $14M in New Funding' banner.

### Ghost Neutral Button

**Role:** Minimal impact button, for subtle interactive elements or navigation.

Background: transparent, Text: Midnight Ink (#000000). Border: 1px solid Midnight Ink (#000000). Padding: 10px top/bottom, 20px/10px left/right. Border Radius: 0px.

### Search/Navigation Tab

**Role:** Interactive tabs for filtering or navigating content sections.

Background: Canvas White (#ffffff), Text: Midnight Ink (#000000) for active, Steel Gray (#717173) for inactive. Border: 1px inset rgba(0,0,0,0.05). Underlines with a solid bar color.

### Product Feature Card

**Role:** Displaying product features or content blocks.

Background: Canvas White (#ffffff). Border Radius: 14px. Box Shadow: inset 0px 0px 0px 1px rgba(0, 0, 0, 0.05). No padding defined, content is expected to dictate internal spacing.

### Default Input Field

**Role:** Standard text input element.

Background: rgb(238, 238, 238), Text: Deep Gray (#444444), Border: 1px solid Deep Gray (#444444). Padding: 10px all sides. Border Radius: 8px.

### Small Pill Button

**Role:** Compact interactive element or tag, typically for meta-information or minor actions.

Background: Coal Black (#131315). Text: rgb(0,0,238) (browser default link blue, not a brand choice). Radius: 20px. Padding: 10px top/bottom, 20px left/right.

## Layout

The page primarily uses a max-width contained model, with a consistent vertical rhythm. The hero section often features a centered headline over a white background with a secondary subheading and two action buttons (one filled, one outlined). Sections alternate between simple centered stacks of text, and two-column layouts featuring text on one side and a visual element (like a product screenshot or video frame) on the other. A notable element is a horizontal tab navigation using ghost buttons. Feature grids, if present, are likely simple 3-column structures. Ample white space creates a comfortable, airy density between content blocks. Navigation is a sticky top bar with a left-aligned logo and right-aligned links and buttons.

## Imagery

This design system uses a blend of tightly cropped product screenshots and high-quality photography, often with a subtle film grain or warm color grade, alongside a highly functional icon set. Lifestyle photography is present but primarily contained, never full-bleed, depicting people using technology or interacting in studio environments. Illustrations are minimal to none. Icons are typically outlined or filled, mostly monochrome (Midnight Ink or Canvas White), with a consistent stroke weight. Imagery serves mostly an explanatory or product showcase role, occasionally decorative for atmosphere, but never heavily stylized or abstract. The density is balanced, with imagery breaking up text-dominant sections, but not overwhelming the page.

## Dos & Donts

### Do

- Prioritize Canvas White (#ffffff) for primary backgrounds and Coal Black (#131315) or Midnight Ink (#000000) for primary text to maintain high contrast.
- Use Inter Display for all headings, leveraging its full range of sizes and letter-spacing for visual impact and hierarchy.
- Apply a 20px border-radius for primary buttons and 14px for cards, ensuring consistent application of these distinct rounded corners.
- Emphasize interactive elements and call-to-actions with Digital Violet Bold (#6941c6) for borders or text, or Coal Black (#131315) for filled backgrounds.
- Maintain a clear elementGap of 10px for vertical rhythm between individual UI components.
- Employ the hard 8px 8px 0px 0px F1F1F1 shadow for buttons to give them a distinct, almost debossed appearance.
- Use Ghost Neutral buttons (transparent background, Midnight Ink border and text) for less prominent actions, reserving filled or accented buttons for key interactions.

### Don't

- Avoid using multiple chromatic colors; restrict accents primarily to the Digital Violet palette.
- Do not introduce soft or subtle shadows where a hard, offset shadow is expected; adhere to the distinct shadow style on buttons.
- Refrain from using generic font families or weights when Inter Display or Aux Mono Regular are available for specific roles.
- Do not deviate from the established border radii of 0px for some interactive elements and 14px/20px/35px for others, as these are signature elements.
- Avoid introducing background gradients outside of the specified conic-gradient for special accent areas.
- Do not use generic gray scales for text when Steel Gray (#717173), Deep Gray (#444444), or Dark Gray (#333333) are available for semantic text roles.
- Do not use transparent or blurry backgrounds when solid color surfaces are the default. Sharp contrasts are key.

## Notes

### Agent Prompt Guide

### Quick Color Reference
* text: #000000
* background: #ffffff
* border: #000000
* accent: #6941c6
* primary action: #131315 (filled action)

### 3-5 Example Component Prompts
* Create a Primary Action Button: #131315 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
* **Feature Card with Inset Border:** Create a card with background #ffffff, 14px radius, box-shadow rgba(0, 0, 0, 0.05) 0px 0px 0px 1px inset. Inside, add a 24px Inter Display heading and 14px Inter body text. 
* **Accent Banner with Ghost Button:** Create a banner section with subtle Digital Violet Light (#dacefd) background, containing a text label in Inter 14px #6941c6. Include a 'Shade Raises $14M in New Funding' Ghost Accent Button: background transparent, text #6941c6, border 1px solid #6941c6, 10px top/bottom, 15px left/right padding, 0px radius.
* **Standard Input Field:** Design an input field with background #eeeeee, text #444444, border 1px solid #444444, 8px radius, 10px padding.
* **Navigation Tab:** Create a navigation tab component. Active tab: background #ffffff, text #000000, border-bottom 2px solid #6941c6. Inactive tab: background #ffffff, text #717173, no border-bottom.

---
_Source: https://styles.refero.design/style/e549766e-b8b1-48a2-bd72-8cc04e9e4e9d_
