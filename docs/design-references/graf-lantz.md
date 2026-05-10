# Graf Lantz — Design Reference

> White canvas, sharp monochrome details.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://glob.land](https://glob.land) |
| Refero page | [https://styles.refero.design/style/f1a690c7-234d-4ee9-9806-5790934e7043](https://styles.refero.design/style/f1a690c7-234d-4ee9-9806-5790934e7043) |
| Theme | light |
| Industry | ecommerce |

## Overview

Graf Lantz embodies a clean, material-focused aesthetic with a high-contrast monochromatic palette grounded in deep grays and precise typography. Surfaces are predominantly white, providing a crisp canvas for content, while subtle off-white tones introduce minimal depth. A single, vivid violet accent is reserved for primary interactive elements, providing a focused point of action against the otherwise subdued interface. Components are lightweight with strong outlines and sharp corners generally, though some interactive elements feature a playful, overtly rounded form.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, primary content areas |
| Text Black | `#212121` | neutral | Primary text, prominent links, footer text. Provides strong contrast against white backgrounds |
| Graphite Text | `#474747` | neutral | General body text, default input text, secondary button text, and navigational links. This forms the most common text color |
| Subtle Gray | `#eeeeee` | neutral | Input backgrounds, secondary button backgrounds, subtle dividers, and light strokes |
| Deepest Gray | `#000000` | neutral | Strong borders, specific icon fills, and high-emphasis text snippets, often used for structural outlines |
| Muted Silver | `#7b7b7b` | neutral | Helper text, less important links, and soft borders, offering a tertiary level of visual hierarchy |
| Violet Action | `#574cd5` | brand | Primary Call To Action backgrounds, active interactive elements — a vivid punctuation mark that guides user interaction |

## Typography

### NeueHaasUnicaW1G

| Key | Value |
| --- | --- |
| weight | 400, 500, 900 |
| weights | 400, 500, 900 |
| sizes | 12px, 14px, 16px |
| lineHeight | 1.00, 1.20, 1.40, 1.50 |
| letterSpacing | normal |
| substitute | Helvetica Neue |
| role | Primary typeface for most text content including body, links, navigation, and footer text. Its precise forms contribute to the system's clean, structured feel. |

### Instrument Sans

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 14px, 16px, 32px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Inter |
| role | Used for headings and input fields, providing clear, readable titles that maintain the overall modern aesthetic. |

### accessibly

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px, 35px |
| lineHeight | 1.00 |
| letterSpacing | normal |
| substitute | System UI |
| role | A distinct display font used sparingly for high-impact buttons or decorative headings, injecting a unique personality. |

### Source Sans Pro

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.2 |
| role | Source Sans Pro — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.4 |  |
| body-sm | 14 |  | 1.4 |  |
| body | 16 |  | 1.5 |  |
| heading | 32 |  | 1.2 |  |
| display | 35 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 50px |
| inputs | 0px |
| buttons | 0px |
| elements | 0px |

- **elementGap** — 15px
- **sectionGap** — 50px
- **cardPadding** — 15px
- **pageMaxWidth** — 

## Components

### Primary Action Button

**Role:** Main interactive button

Filled button with 'Violet Action' background (#574cd5), 'Graphite Text' (#474747), and a 100% border radius creating a pill shape. Text is medium weight (500) Instrument Sans at 16px.

### Search Input Field

**Role:** User input for search queries

Input field with 'Subtle Gray' background (#eeeeee), 'Graphite Text' (#474747) for text, no border radius (0px). Padding is 9px vertical and 16px horizontal. Uses Instrument Sans.

### Secondary Ghost Button

**Role:** Secondary action or navigational button

Ghost button with 'Subtle Gray' background (#eeeeee), 'Graphite Text' (#474747) for text, and no border radius (0px). Padded 0px vertical and 15px horizontal. Uses NeueHaasUnicaW1G.

### Header Navigation Link

**Role:** Top-level navigation items

Text link using NeueHaasUnicaW1G, size 16px, 'Graphite Text' (#474747). Active/focus states likely involve border or color changes.

### Pill Button

**Role:** Small, high-contrast action or tag.

Button with 'Canvas White' background (#ffffff), 'Graphite Text' (#474747), and a 50px border radius, typically containing uppercase text. Example: 'SHOP COASTERS'.

## Layout

The page structure favors a max-width contained layout, likely centered, rather than full-bleed. The hero section is characterized by a prominent visual (like a background image with text overlay) and a focused call to action. Section rhythm appears to be consistent with ample vertical spacing. Content arrangement frequently uses stacked elements with clear headings and accompanying text. Navigation is a simple top bar with text links, likely sticky or prominent. The density is on the comfortable side, prioritizing readability and visual breathing room over information packing.

## Imagery

This system primarily uses product photography, often close-cropped to showcase material texture and detail. Imagery is presented within contained sections, with a tendency toward raw edges over masking. The photography appears brightly lit, focusing on the product itself with minimal background context. Icons are minimal, likely outlined, and contribute to the functional UI rather than serving as decorative elements. The overall density of imagery is balanced, with imagery serving both explanatory and atmospheric roles, breaking up text-dominant sections.

## Dos & Donts

### Do

- Use 'Canvas White' (#ffffff) as the default background for all primary page sections and elevated component surfaces.
- Apply 'Graphite Text' (#474747) for most body text, links, and form field content to ensure consistent readability.
- Reserve 'Violet Action' (#574cd5) exclusively for primary interactive elements, such as filled call-to-action buttons, to maintain its impact.
- Form input fields should utilize 'Subtle Gray' (#eeeeee) backgrounds and maintain a 0px border radius for a sharp, neutral appearance.
- Headlines should primarily use Instrument Sans font with variable weights and sizes (e.g., 32px), with careful attention to line height 1.2 to 1.0.
- Maintain a default padding of 15px for cards and primary element spacing, with sections separated by 50px of vertical space.
- Employ a 0px border radius for most container elements and buttons to preserve the sharp, modern aesthetic, except for specific pill-shaped elements that use 50px.

### Don't

- Do not introduce new vibrant colors outside of 'Violet Action' (#574cd5) for interactive elements; color should remain predominantly monochromatic.
- Avoid arbitrary uses of 'Deepest Gray' (#000000) for text; reserve it for structural borders or specific high-contrast icon details.
- Do not vary border radius extensively; stick to 0px for most elements and 50px for specific pill-like shapes.
- Refrain from using lightweight fonts for primary headings. Instrument Sans at weights 400 or 500 should be used for clarity.
- Do not apply shadows or heavy elevation styles; the system relies on flat surfaces and high color contrast for hierarchy.
- Avoid using multiple font families for body text; NeueHaasUnicaW1G should be the standard for general content.
- Do not deviate from the established spacing units of 4px increments, particularly for element gaps (15px) and section gaps (50px).

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #474747
background: #ffffff
border: #000000
accent: #574cd5
primary action: #574cd5 (filled action)

Example Component Prompts:
Create a Primary Action Button: #574cd5 background, #7b7b7b text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
Create a sub-heading: Instrument Sans 32px 500, 'Graphite Text' (#474747), lineHeight 1.2.

---
_Source: https://styles.refero.design/style/f1a690c7-234d-4ee9-9806-5790934e7043_
