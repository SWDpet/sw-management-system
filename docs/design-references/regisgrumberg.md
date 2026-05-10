# Regisgrumberg — Design Reference

> Black canvas, white light

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.regisgrumberg.com](https://www.regisgrumberg.com) |
| Refero page | [https://styles.refero.design/style/1a2ca4fb-1087-4fd0-83ba-590bc63f54ee](https://styles.refero.design/style/1a2ca4fb-1087-4fd0-83ba-590bc63f54ee) |
| Theme | dark |
| Industry | design |

## Overview

Regisgrumberg embraces a conceptual, dark-mode aesthetic with high contrast and stark white typography as the primary visual element. The design uses very little color, focusing instead on dynamic typographic arrangements and a sense of depth and movement created by shifting text. The overall impression is digital, experimental, and direct, with UI elements acting as functional accents rather than decorative flourishes.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space | `#000000` | neutral | Page backgrounds, text for certain components, primary canvas |
| Star Dust | `#ffffff` | neutral | Primary text, interactive elements, accent borders, and background for featured interactive elements |
| Ghost Gray | `#c4c4c4` | neutral | Secondary text, subtle borders, and inactive UI elements |
| Iron Oxide | `#363636` | neutral | Subtle surface variation for navigation or secondary containers |

## Typography

### monospace

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px |
| lineHeight | 1.00, 1.20 |
| letterSpacing | normal |
| substitute | 'IBM Plex Mono', 'Fira Code', 'JetBrains Mono' |
| role | Functional text, small labels, code snippets. Its fixed-width character reinforces a technical, structured feel. |

### Times

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| lineHeight | 1.00 |
| letterSpacing | normal |
| substitute | 'Times New Roman', 'Georgia' |
| role | Likely used for specific stylistic text or as a fallback; its serif nature would provide a high contrast to the primary sans-serifs, creating visual tension. |

### Montserrat

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| weights | 400, 600 |
| sizes | 11px, 14px, 18px, 24px, 30px, 36px |
| lineHeight | 1.00, 1.20, 2.78 |
| letterSpacing | normal |
| substitute | 'Inter', 'Lato' |
| role | General body text and subheadings. Its clean, geometric form provides excellent readability against dark backgrounds. |

### Anton

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 18px, 24px, 48px, 90px, 150px |
| lineHeight | 1.00, 1.20 |
| letterSpacing | normal |
| substitute | 'Oswald', 'Impact' |
| role | Bold, impactful headlines and display text. Its condensed and heavy style demands attention, creating strong typographic statements. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1 |  |
| subheading | 18 |  | 1.2 |  |
| heading-sm | 24 |  | 1.2 |  |
| heading | 30 |  | 1.2 |  |
| heading-lg | 36 |  | 1.2 |  |
| display | 150 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| circle | 100% |
| elements | 2px |

- **elementGap** — 23px
- **sectionGap** — 42px
- **cardPadding** — 23px
- **pageMaxWidth** — 100px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Space Canvas | `#000000` | 0 | Primary page background and default surface for most content. |
| Iron Oxide Secondary | `#363636` | 1 | Subtle background for UI elements like navigation bars, providing minimal differentiation. |
| Star Dust Interactive | `#ffffff` | 2 | Background for isolated interactive elements like circular buttons, creating a focal point. |

## Components

### Text Only Button - default

**Role:** Minimal interactive element, common for navigation or secondary actions.

Background: transparent (rgba(0,0,0,0)), Text: Deep Space (#000000), Border: none, Radius: 0px, Padding: 0px.

### Text Only Button - ghost

**Role:** Minimal interactive element for navigation or secondary actions on dark backgrounds.

Background: transparent (rgba(0,0,0,0)), Text: Ghost Gray (#c4c4c4), Border: none, Radius: 0px, Padding: 0px.

### Circular Interactive Button

**Role:** Prominent interactive element for primary calls to action, like 'Enter'.

Background: Star Dust (#ffffff), Text: Deep Space (#000000), Border: none, Radius: 100%, Padding: 0px. The tight padding implies only text inside the circle.

### Navigation Link

**Role:** Interactive text link within navigation areas.

Text: Deep Space (#000000), typically uses Montserrat or monospace font families.

### Hero Headline

**Role:** Dominant display text for main page sections.

Font: Anton, sizes from 48px to 150px, weight 500, lineHeight 1.0 or 1.2, Text: Star Dust (#ffffff).

### Body Text

**Role:** Standard paragraph text.

Font: Montserrat, size 14px or 18px, weight 400, lineHeight 1.2 or 2.78, Text: Ghost Gray (#c4c4c4).

### Small Text / Caption

**Role:** Small descriptive text or labels.

Font: monospace, size 13px, weight 400, lineHeight 1.0 or 1.2, Text: Ghost Gray (#c4c4c4).

## Layout

The page primarily utilizes a full-bleed dark canvas, with content structured in a max-width of 100px indicated for certain sections, giving a very tight and controlled inner content area or implying highly specific contextual layouts given the overall page width. The hero features a centered, interactive typographic element. Sections appear to flow seamlessly with consistent vertical spacing and no distinct visual dividers. The overall density is spacious, contrasting minimal content against a vast, dark background.

## Imagery

The site uses minimal imagery, focusing almost exclusively on text as its primary visual element. The interactive spiral typography on the hero is the main graphical feature, transforming text into a dynamic visual. If any imagery were to be introduced, it should be highly conceptual, abstract, or purely functional (e.g., product screenshots) to align with the stark, high-contrast, text-dominant aesthetic. There is no evidence of photography, illustrative styles, or icons on the provided page, suggesting a preference for type over traditional imagery.

## Dos & Donts

### Do

- Use Deep Space (#000000) as the predominant background color for all page sections.
- Employ Star Dust (#ffffff) for all primary text, headlines, and interactive elements for maximum contrast.
- Utilize Montserrat for general body copy and subheadings, adjusting weight between 400 and 600 for emphasis.
- Apply Anton font for all impactful headlines, using its large sizes (48px, 90px, 150px) to command attention.
- Maintain a spacious density with element and section gaps centered around 23px and 42px respectively.
- For primary call-to-action buttons, use the Circular Interactive Button style: Star Dust (#ffffff) background with Deep Space (#000000) text and a 100% border radius.
- All interactive elements should have 0px border radius and 0px padding unless a specific component dictates otherwise (e.g., circular button).

### Don't

- Avoid introducing additional saturated colors unless explicitly for a semantic state (which are not defined in this system).
- Do not use subtle gradients or complex shadows for elevation; keep surfaces mostly flat and rely on typographic hierarchy and spaciousness.
- Refrain from using Times font for general UI; reserve it for specific decorative or content-driven contexts, if at all.
- Do not use generic square buttons; default to text-only or the prescribed circular button for primary actions.
- Avoid heavy borders or dividers; rely on color contrast and spacing to define UI areas.
- Do not vary letter-spacing; all typography uses normal letter-spacing unless explicitly stated.
- Do not allow text elements to be set with a background color different from the main surface color, maintaining high contrast.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000000
border: #000000
accent: #ffffff
primary action: no distinct CTA color

Example Component Prompts:
Create a hero section: Deep Space (#000000) background. Headline 'WEIRD WEBSITE' using Anton font, 90px, weight 500, lineHeight 1.0, Star Dust (#ffffff) text.
Create a navigation link: 'WORK' using Montserrat font, 18px, weight 400, lineHeight 1.2, Deep Space (#000000) text (on a lighter surface if applicable, otherwise Star Dust).
Create a primary 'ENTER' button: Star Dust (#ffffff) background, Deep Space (#000000) text centered, 100% border-radius, using Montserrat font, 13px, weight 400.
Create a body text block: Ghost Gray (#c4c4c4) text, Montserrat font, 14px, weight 400, lineHeight 2.78, with 23px padding-top, 42px padding-right and padding-left, centered horizontally.

---
_Source: https://styles.refero.design/style/1a2ca4fb-1087-4fd0-83ba-590bc63f54ee_
