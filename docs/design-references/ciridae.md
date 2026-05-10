# Ciridae — Design Reference

> Monochrome Grid, Abstract Glow. Precision-engineered UI on a dark, almost black canvas, with typography as the primary visual texture and light playing like reflections off dark metal.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.ciridae.com](https://www.ciridae.com) |
| Refero page | [https://styles.refero.design/style/a1b78a21-a304-482b-8ce5-f612d95d44fe](https://styles.refero.design/style/a1b78a21-a304-482b-8ce5-f612d95d44fe) |
| Theme | dark |
| Industry | ai |

## Overview

This AI transformation firm's design identity is a stark, high-contrast monochrome world, like a precision instrument in a dark laboratory. The design consistently avoids color beyond white and black, using the absence of color to convey seriousness and focus. The only chromatic hint is a subtle, almost unnoticeable orange, creating an atmosphere of contained power. This system focuses on sharp typography and generous padding around interactive elements, suggesting careful deliberation in every interaction.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Black | `#000000` | neutral | Backgrounds, prominent text, icon fills. |
| Deep Charcoal | `#0B0B0B` | neutral | Secondary backgrounds, subtle elevated surfaces. |
| Warm Graphite | `#272A2A` | neutral | Tertiary backgrounds, deeper surface layers. |
| Pure White | `#FFFFFF` | neutral | Primary text, button labels, interactive elements, prominent iconography. Creates high contrast against dark backgrounds. |
| Ash Gray | `#CECECE` | neutral | Subtle borders, inactive text, secondary iconography. |
| Steel Gray | `#858585` | neutral | Placeholder text, minor borders, tertiary text. |
| Subtle Orange | `#CC6437` | accent | Rare accent color for specific information hints or subtle emphasis. Its limited use makes any appearance notable. |
| White to Ash Gradient | `#FFFFFF` | neutral | Used for a subtle linear fade, providing a soft transition effect. |

## Typography

### Pragmatica Cond

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px, 20px, 32px, 62px |
| lineHeight | 0.90, 1.00, 1.05, 1.10, 1.43 |
| letterSpacing | -0.02em |
| substitute | Open Sans Condensed |
| role | Primary headings, navigation, prominent statements, and general UI labels. Its condensed form and tight letter-spacing create a sharp, efficient aesthetic. |

### Pragmatica

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 15px, 24px |
| lineHeight | 1.10, 1.20 |
| letterSpacing | -0.02em, -0.01em |
| substitute | Open Sans |
| role | Secondary headings and larger body text blocks where slightly more breath is needed compared to Pragmatica Cond. |

### Roboto Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 11px, 14px |
| lineHeight | 0.90, 1.00, 1.10, 1.20 |
| letterSpacing | -0.02em |
| substitute | Roboto Mono |
| role | Body copy and informational text. The monospaced nature subtly reinforces a technical, code-adjacent brand identity. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.1 | -0.22 |
| body-sm | 14 |  | 1 | -0.28 |
| heading | 20 |  | 1.05 | -0.4 |
| heading-lg | 24 |  | 1.2 | -0.24 |
| display | 32 |  | 1.1 | -0.64 |
| display-lg | 62 |  | 1.43 | -1.24 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| badges | 1440px |
| subtle | 4px |
| buttons | 1440px |
| general | 10px |

- **elementGap** — 4px
- **sectionGap** — 48px
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### Ghost Button Group

### Mission Statement Block

### Team & Backers Block

### Ghost Button

**Role:** Primary Action

Transparent background, Pure White text, Pure White 1px border. Extremely large 1440px border-radius for a pill shape. Padding 10px vertical, 18px horizontal. Uses Pragmatica Cond 400.

### Circular Badge

**Role:** Informational Tag

Transparent background, Pure White text. Extremely large 1440px border-radius. Padding 5px vertical, 11px horizontal. Uses Pragmatica Cond 400.

### Monochrome Badge

**Role:** Neutral Tag

Steel Gray background, Deep Charcoal text. Extremely large 1440px border-radius. Padding 7px vertical, 10px horizontal. Uses Pragmatica Cond 400.

### Navigation Link

**Role:** Primary Navigation

Pure White text. Uses Pragmatica Cond 400 at 14px with -0.02em letter-spacing. Minimal padding around text.

## Layout

The page primarily uses a full-bleed dark background, creating an expansive, borderless feel. The hero section is full-screen, featuring the brand logo centrally over the abstract, atmospheric background, with minimal surrounding text. Content sections are primarily centered, with a strong emphasis on large, singular text blocks or simple two-column arrangements that alternate textual explanations with subtle graphic elements. There's no immediately apparent grid for complex content, favoring a stacked, spacious approach. Navigation is minimal, consisting of top-left and top-right ghost buttons. Vertical spacing between content blocks is generous, providing significant breathing room and contributing to the overall sense of minimalist luxury.

## Imagery

The visual language is abstract and conceptual. It predominantly features blurred, out-of-focus light flares and bokeh effects against a dark background, evoking a sense of depth, atmosphere, and advanced technology without being explicit. There are no conventional photographs or detailed illustrations. Product representation is limited to minimalist, iconic renderings of the brand's star-shaped logo, often with glossy or reflective finishes. Icons are outlines, leveraging the Pure White on dark contrast. The density is extremely low, with imagery serving as decorative atmosphere rather than explanatory content, enhancing the perception of a sophisticated, almost mysterious, high-tech entity.

## Elevation philosophy

This design intentionally avoids all traditional drop shadows for elevation. Instead, depth and hierarchy are established solely through subtle shifts in background colors: from Absolute Black to Deep Charcoal to Warm Graphite. This creates a layered, recessed effect, as if UI elements are embedded into distinct planes of a dark, machined surface, maintaining a flat yet textured aesthetic.

## Dos & Donts

### Do

- Prioritize high contrast text-on-dark backgrounds using Pure White (#FFFFFF) text on Deep Charcoal (#0B0B0B) or Absolute Black (#000000).
- Use pill-shaped radii (1440px) for all buttons and badges to create a distinct, modern form.
- Apply -0.02em letter-spacing to all text elements using Pragmatica Cond for a uniform, crisp typographic texture.
- Maintain generous internal padding of 10px vertical and 18px horizontal for buttons, and 5-7px vertical, 10-11px horizontal for badges.
- Reserve Subtle Orange (#CC6437) for extremely limited, high-impact accent moments, such as subtle borders or interactive states, to maximize its effect.

### Don't

- Do not introduce additional chromatic colors; the palette is strictly monochrome with a single, subtle orange accent.
- Avoid using box-shadows or drop-shadows for elevation; rely on background color changes from Deep Charcoal to Warm Graphite.
- Do not vary letter-spacing for Pragmatica Cond; it rigidly adheres to -0.02em.
- Do not use conventional square or slightly rounded corners; elements should either be perfectly sharp or pill-shaped (1440px radius).
- Avoid decorative imagery or large photography; rely on abstract graphics and a minimal UI approach.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #FFFFFF
- Background: #0B0B0B
- CTA: #FFFFFF (border/text on transparent background)
- Border: #FFFFFF
- Accent: #CC6437

### 3-5 Example Component Prompts
1. Create a Hero Section: Full-bleed background with abstract blurred light effect. Center the main brand logo (white outline). Below it, display the `display-lg` headline 'CIRIDAE' in Pure White (#FFFFFF), Pragmatica Cond 400, 62px, 1.43 lineHeight, -1.24px letterSpacing. Add a 'START NOW' ghost button (Pure White border and text, 1440px radius, 10px vertical 18px horizontal padding) in the top left, and a 'MENU' ghost button (Pure White border and text, 1440px radius, 10px vertical 18px horizontal padding) in the top right.
2. Create a feature block: Warm Graphite (#272A2A) background with 48px vertical sectionGap. Display a `heading` 'WORKFLOW REDESIGN' in Pure White (#FFFFFF), Pragmatica Cond 400, 20px, 1.05 lineHeight, -0.4px letterSpacing. Below it, add body text 'We map how work... end-to-end.' in Pure White (#FFFFFF), Roboto Mono 400, 14px, 1.0 lineHeight, -0.28px letterSpacing. Ensure ample 20px horizontal padding.
3. Design a Call-to-Action button: 'EXPLORE ALL' text in Pure White (#FFFFFF), Pragmatica Cond 400, 14px, 0.90 lineHeight, -0.28px letterSpacing. Transparent background, 1px Pure White (#FFFFFF) border, 1440px border-radius, 10px vertical and 18px horizontal padding. The element gap around it should be 4px.
4. Create a small informational badge: 'NEW' text in Pure White (#FFFFFF), Pragmatica Cond 400, 14px, 0.9 lineHeight, -0.28px letterSpacing. Transparent background, 1440px border-radius, 5px vertical and 11px horizontal padding.

---
_Source: https://styles.refero.design/style/a1b78a21-a304-482b-8ce5-f612d95d44fe_
