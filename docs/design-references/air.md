# Air — Design Reference

> Expansive sky, clean canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://air.inc](https://air.inc) |
| Refero page | [https://styles.refero.design/style/d3289fe7-a85e-42d8-96b7-eb7faa62a104](https://styles.refero.design/style/d3289fe7-a85e-42d8-96b7-eb7faa62a104) |
| Theme | dark |
| Industry | productivity |

## Overview

Air's design system evokes a digital canvas under an expansive, almost dreamlike sky. It marries a high-contrast dark text with clean, slightly rounded white and muted gray surfaces. Typography mixes confident, weighty statements with lighter, more fluid headings, creating a dynamic yet composed visual hierarchy. Accents are sparingly applied but impactful, often appearing as subtle outlines or functional indicators, maintaining an overall atmosphere of refined utility.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Cloud Canvas | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, active states — acts as a luminous digital canvas that captures details |
| Midnight Ink | `#1b1b1b` | neutral | Primary text, prominent icons, dark outlined elements – provides strong contrast against lighter surfaces |
| Vapor Gray | `#f5f5f5` | neutral | Subtle background for input fields, secondary card surfaces — a soft, almost imperceptible base layer |
| Charcoal Void | `#000000` | neutral | Strongest textual contrast, borders for ghost buttons, deepest shadow effects — sparingly used for maximum impact |
| Sky Blue | `#426188` | brand | Heading accents, decorative border elements — a muted, sophisticated blue that hints at depth without being overtly bold |
| Vivid Azure | `#2b7fff` | accent | Outlined action buttons, active link states, interactive indicators — a bright, functional blue that makes interactive elements accessible and clear |

## Typography

### Control

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 12px, 13px, 14px, 16px, 20px |
| lineHeight | 1.10, 1.40, 1.50 |
| letterSpacing | normal |
| substitute | Inter |
| role | Dominant body text, buttons, navigation items across various sizes. Its 500 weight provides a clear, confident voice for everyday UI. |

### Control Compressed

| Key | Value |
| --- | --- |
| weight | 900 |
| weights | 900 |
| sizes | 259px |
| lineHeight | 0.85 |
| letterSpacing | normal |
| substitute | Oswald |
| role | Extremely large, impactful display text for hero sections or brand statements. Its compressed heavy weight creates a monumental, almost architectural feel. |

### Control Cursive

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 20px, 32px, 56px |
| lineHeight | 1.00, 1.10, 1.50 |
| letterSpacing | normal |
| substitute | Dancing Script |
| role | Distinctive, flowing script for stylistic flourishes, brand elements, or unique headings. Its cursive nature adds a touch of organic elegance against the structured UI. |

### Control TNT

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 20px, 32px |
| lineHeight | 1.10, 1.50 |
| letterSpacing | normal |
| substitute | Montserrat |
| role | Specialized headings and prominent body text. The name 'TNT' suggests an underlying impactful quality, used to draw attention to key messages. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.1 |  |
| body | 14 |  | 1.5 |  |
| heading | 20 |  | 1.5 |  |
| heading-lg | 32 |  | 1.1 |  |
| display | 259 |  | 0.85 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 14px |
| images | 11px |
| inputs | 4px |
| buttons | 8px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Base | `#fff8dc` | 0 | The foundational, immersive background color that gives the landing page its atmospheric feel. Note: This color is not part of the UI component system and only applies to hero/backgrounds. |
| Haze Surface | `#f5f5f5` | 1 | Subtle backgrounds for input fields and secondary card surfaces, providing a gentle lift from the page base. |
| Cloud Canvas | `#ffffff` | 2 | Primary surface for cards, modals, and other prominent UI elements, providing a clean, bright canvas against text. |

## Components

### Ghost Navigation Button

**Role:** Primary navigation and subtle calls to action.

backgroundColor: rgba(0,0,0,0), color: #ffffff, borderColor: #ffffff, borderRadius: 0px, padding: 0px 16px 0px 16px. Underlines on hover/active.

### Outlined Action Button

**Role:** Primary calls to action for interactive elements.

backgroundColor: rgba(0,0,0,0), color: #1b1b1b, borderColor: #2b7fff, borderRadius: 8px, padding: 0px 0px 0px 0px. The outline provides visual punch.

### Basic Card

**Role:** Content grouping for informational blocks with minimal visual adornment.

backgroundColor: rgba(0,0,0,0), borderRadius: 0px, boxShadow: none, padding: 0px.

### Elevated Content Card

**Role:** Card for featuring key content sections.

backgroundColor: #f5f5f5, borderRadius: 12px, boxShadow: none, padding: 20px. Uses a soft background for distinction.

### Hero Image Card

**Role:** Large, immersive card for images or visual content.

backgroundColor: rgba(0,0,0,0), borderRadius: 14px, boxShadow: none, padding: 0px.

### Standard Input Field

**Role:** User entry fields for forms.

backgroundColor: #f5f5f5, color: #1b1b1b, borderTopColor: rgba(0,0,0,0.1), borderRadius: 4px, padding: 10px.

## Layout

The overarching page layout is full-bleed, using an atmospheric gradient background behind a centered content container for most sections. The hero features an expansive full-viewport background with large, centered marketing text. Section rhythm is marked by consistent vertical spacing of 48px, often with alternating background colors or distinct visual treatments separating blocks. Content arranges into clear, often two-column text-left/visual-right sections, with features presented in grid-like structures featuring cards. Navigation is a sticky top bar with ghost buttons, transitioning to outlined buttons or color changes on interaction, maintaining a relatively minimal profile.

## Imagery

The site uses a mix of abstract 3D renders with soft, atmospheric gradients for hero sections, and clean, contained product screenshots for demonstrating functionality. Photography is minimal, if present. Icons are primarily outlined or ghost-style with a moderate stroke weight, often in 'Midnight Ink' or 'Cloud Canvas' to match the overall monochrome UI, with 'Vivid Azure' accents for interactive states. Imagery is generally contained within defined areas or sections, serving both atmospheric and explanatory roles without excessive layering or full-bleed treatment outside the hero.

## Dos & Donts

### Do

- Use 'Cloud Canvas' (#ffffff) for all main page backgrounds and elevated card surfaces.
- Apply 'Midnight Ink' (#1b1b1b) for primary text elements to ensure strong readability.
- Employ 'Control' font at 500 weight for all body text and common UI labels.
- Utilize 'Vivid Azure' (#2b7fff) exclusively for borders of primary outlined actions and active link states.
- Round corners with 8px radius for all buttons and navigation items.
- Implement a base element gap of 8px (2 base units) between most UI elements for consistent comfortable density.
- Use 'Control Compressed' at 259px size and 900 weight for high-impact display headlines to command attention.

### Don't

- Do not use saturated brand colors (#426188, #2b7fff) for large background areas or extensive text blocks.
- Avoid arbitrary border radii; stick to 4px for inputs, 8px for buttons, and 14px for significant cards.
- Do not introduce heavy drop shadows, as the system relies on subtle surface changes and crisp contrast for hierarchy.
- Refrain from using 'Charcoal Void' (#000000) for body text; reserve it for distinct headings or strong outlines.
- Do not deviate from the specified font families or weights for their intended roles to maintain typographic identity.
- Avoid tight spacing; maintain a minimum 8px element gap and 48px section gap for comfortable visual separation.
- Do not create filled primary buttons; the system emphasizes outlined chromatic actions with 'Vivid Azure' borders.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #1b1b1b
background: #ffffff
border: #1b1b1b
accent: #426188
primary action: #2b7fff (outlined action border)

Example Component Prompts:
1. Create an Outlined Primary Action: Transparent background, #2b7fff border and text, 9999px radius, compact pill padding. Use it for the main CTA instead of a filled button.
2. Design an elevated content card: background-color: #f5f5f5, border-radius: 12px, padding: 20px. Place a 'Midnight Ink' #1b1b1b heading (Control 500, 20px, 1.5 line-height) and 'Midnight Ink' #1b1b1b body text (Control 500, 14px, 1.5 line-height) inside.
3. Implement an input field: background-color: #f5f5f5, text color: #1b1b1b, border-color: rgba(0,0,0,0.1), border-radius: 4px, padding: 10px. Placeholder text color should be a lighter neutral from the palette (e.g., a subdued gray version of #1b1b1b).
4. Create a hero section: Full-bleed background with a subtle gradient (no specific color available from tokens, suggest a light sky purple to orange, e.g., linear-gradient(135deg, #a7b7e8, #f5d7b5)). Overlay a large 'Control Compressed' 900 weight 259px headline in #1b1b1b, normal letter-spacing, line-height 0.85. Below, add a secondary 'Control Cursive' 500 weight 56px heading in #1b1b1b, normal letter-spacing, line-height 1.1.

---
_Source: https://styles.refero.design/style/d3289fe7-a85e-42d8-96b7-eb7faa62a104_
