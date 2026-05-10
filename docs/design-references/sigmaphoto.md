# Sigmaphoto — Design Reference

> Precision engineered, high-contrast monochrome

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.sigmaphoto.com](https://www.sigmaphoto.com) |
| Refero page | [https://styles.refero.design/style/67c60ee4-ac38-41ee-834e-ed2a92146417](https://styles.refero.design/style/67c60ee4-ac38-41ee-834e-ed2a92146417) |
| Theme | light |
| Industry | ecommerce |

## Overview

Sigma presents a sophisticated, high-contrast aesthetic emphasizing product photography against a mostly achromatic interface. Typography is reserved yet impactful, combining custom sans-serif and serif fonts for a classic yet modern feel. Visual hierarchy is achieved through stark black and white contrasts, with a single vivid blue for primary interactive elements, reserving color for action and brand emphasis. Components are sharp-edged and minimalist, reflecting precision and technical excellence.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, text elements on dark backgrounds |
| Ink Black | `#000000` | neutral | Primary text, critical borders, icons, input text. Used for strong contrast |
| Charcoal Gray | `#333333` | neutral | Secondary text, dark navigation backgrounds, subtle borders, input placeholder text |
| Cadet Blue | `#0048ff` | brand | Primary action backgrounds, interactive elements, branding accents — a singular vibrant color to signify interactivity |
| Cool Gray | `#707070` | neutral | Muted text, helper text, less prominent links, input borders |
| Silver Mist | `#999999` | neutral | Disabled states, faint icon details, ghost button borders |

## Typography

### Sigma Sans

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px, 16px |
| lineHeight | 1.10, 1.20, 1.25, 1.54 |
| letterSpacing | -0.31em at 13px, 0.005em at 16px |
| role | General body text, navigation links, and button labels. Its clean, sans-serif form ensures readability in functional UI areas. |

### Sigma Serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px, 24px |
| lineHeight | 1.10, 1.25 |
| letterSpacing | 0.003em at 16px, 0.005em at 24px |
| role | Subheadings and featured product descriptions, adding a touch of classic sophistication without being ornate. The serif grounds longer text blocks. |

### Sigma Serif head

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 48px, 88px |
| lineHeight | 1.10, 1.25 |
| letterSpacing | 0.001em at 48px, 0.002em at 88px |
| substitute | Palatino |
| role | Prominent headings and display text for hero sections, conveying authority and product focus. Its relatively loose tracking maintains a premium feel. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| role | Accessibility text and fallback for small UI elements, ensuring universal legibility. |

### Metropolis

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 14px |
| lineHeight | 1.14 |
| letterSpacing | -0.004em |
| substitute | Open Sans |
| role | Specific, smaller body text applications where a slightly more condensed feel is desired, with a subtle negative letter-spacing for tightness. |

### Times

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.2 |
| role | Times — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.2 | -0.31 |
| body | 16 |  | 1.25 | 0.005 |
| subheading | 24 |  | 1.25 | 0.005 |
| heading | 48 |  | 1.1 | 0.001 |
| display | 88 |  | 1.25 | 0.002 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| inputs | 1px |

- **elementGap** — 16px
- **sectionGap** — 24px
- **cardPadding** — 15px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Background | `#ffffff` | 1 | Dominant page background for most content sections. |
| Card Surface | `#ffffff` | 2 | Background for self-contained content blocks, often within the main canvas. |
| Interactive Background | `#333333` | 3 | Dark backgrounds for interactive components like filled buttons or navigation bars that require white text for contrast. |

## Components

### Ghost Button

**Role:** Secondary action or discrete navigation trigger.

Transparent background, Charcoal Gray (#333333) text and border. No padding, giving it a minimalist, text-link like appearance. Border radius 0px.

### Solid Button (Dark)

**Role:** Standard action button for primary or important actions.

Solid Charcoal Gray (#333333) background with Canvas White (#ffffff) text. Padding of 11px vertical, 15px horizontal. Border radius 0px.

### Blue Circular Button

**Role:** Distinctive interactive element, possibly for a specific function like an accessibility toggle.

Solid Cadet Blue (#0048ff) background. Content color Ink Black (#000000). Circular shape with 50% border radius. No explicit padding.

### Muted Ghost Button

**Role:** An action button with lower visual emphasis, for less critical functions.

Transparent background, Silver Mist (#999999) text and border. No padding, Border radius 0px.

### Standard Input Field

**Role:** Form input element for text entry.

Canvas White (#ffffff) background, Ink Black (#000000) text. Border is a 1px solid Cool Gray (#707070) for normal state. Padding 7px vertical, 11px horizontal. Border radius 1px.

### Header Navigation Link

**Role:** Primary navigation item in the header.

Uses Sigma Sans, weight 400, size 13px. Charcoal Gray text (#333333), no explicit background or border, implying a text-only link within a navigation bar.

## Layout

The page primarily employs a max-width contained layout for content, but hero sections frequently use a full-bleed dark background with centered product imagery. There's a consistent vertical rhythm with sections clearly delineated by background color changes (white to dark). Content is often arranged in centered stacks for headlines and body text, emphasizing product information. Feature sections may use implicit grids for product display. A sticky top navigation bar remains present.

## Imagery

Imagery primarily features product photography: tight crops of lenses and cameras, often against dark, stark, or reflective monochrome backgrounds. The products are presented in high detail, well-lit, and isolated, emphasizing their form and precision. No lifestyle photography is used. Icons are monochromatic, either Ink Black (#000000) or Cool Gray (#707070), with a clean, outlined style. Imagery serves to showcase products as the central focus.

## Dos & Donts

### Do

- Prioritize high visual contrast between text (Ink Black #000000, Charcoal Gray #333333) and backgrounds (Canvas White #ffffff) for all informational elements.
- Use Cadet Blue (#0048ff) exclusively for primary interactive elements, ensuring it stands out against the achromatic palette.
- Maintain a border-radius of 0px for most buttons and visual containers to reinforce a precise, engineered aesthetic.
- Apply Sigma Serif head for all primary marketing headlines (sizes 48px, 88px) with their specified letter-spacing to convey impact.
- Use 1px borders sparingly for subtle separation, favoring hard edges and direct contrast over extensive use of outlines.
- Employ consistent 16px element gaps for spacing between distinct inline items, ensuring a comfortable data density.
- Ensure all input fields have a 1px border radius and a Cool Gray (#707070) border when not focused, signaling a slightly framed but clean input area.

### Don't

- Avoid using multiple chromatic colors; limit color accents strictly to Cadet Blue (#0048ff) for critical interaction.
- Do not use soft, rounded corners for primary UI components or buttons, as the system favors sharp, defined edges.
- Do not introduce extensive drop shadows or complex elevation schemes; rely on direct background changes and contrast for visual layering.
- Do not use highly decorative or script fonts; stick to the defined Sigma Sans and Sigma Serif families for all text.
- Avoid large, uncontained images; always ensure product photography is precisely cropped and presented against clear backgrounds.
- Do not create excessive visual noise with gradients or complex background patterns; keep surfaces clean and solid.
- Do not use overly large padding or element gaps that would reduce the sense of precision and density.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #333333
accent: #0048ff
primary action: #0048ff (filled action)

Example Component Prompts:
1. Create a hero section: full-width dark background. Headline 'EXPLORE THE NEW GENERATION' using Sigma Serif head at 88px, Ink Black (#000000), letter-spacing 0.002em, centered. Subtext 'Unleashing unparalleled optical performance' using Sigma Serif at 24px, Ink Black (#000000), centered. Include a prominent product image centered within the dark background.
2. Create a Call to Action button: Solid Cadet Blue (#0048ff) background, Canvas White (#ffffff) text 'LEARN MORE' using Sigma Sans at 16px, weight 400, border-radius 0px, padding 11px vertical, 15px horizontal.
3. Create a secondary navigation link: Text 'PRODUCT CATEGORIES' using Sigma Sans at 13px, weight 400, Charcoal Gray (#333333), border-radius 0px, no background.
4. Create a text input field: Canvas White (#ffffff) background, 1px solid Cool Gray (#707070) border, 1px border-radius. Placeholder text 'Search' in Cool Gray (#707070) using Sigma Sans 16px, weight 400. Text input should be Ink Black (#000000).

---
_Source: https://styles.refero.design/style/67c60ee4-ac38-41ee-834e-ed2a92146417_
