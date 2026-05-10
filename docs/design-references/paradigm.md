# Paradigm — Design Reference

> Strategic Intelligence Platform: dark obsidian meets stark white

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.paradigmai.com](https://www.paradigmai.com) |
| Refero page | [https://styles.refero.design/style/73cd9a6a-f861-4376-a3e1-e12f83c8960e](https://styles.refero.design/style/73cd9a6a-f861-4376-a3e1-e12f83c8960e) |
| Theme | light |
| Industry | ai |

## Overview

Paradigm AI presents a high-contrast visual system featuring a stark dark hero section that transitions into an expansive, bright canvas for content. The aesthetic combines precise, technical typography with subtle graphical flourishes and a functional, restrained color palette, primarily leveraging deep neutrals for structure and specific, controlled accents for interaction and status. Components are intentionally lightweight, favoring soft borders, minimal elevation, and small radii, emphasizing content clarity over heavy ornamentation. Visual hierarchy is established through thoughtful typographic scaling and strategic use of negative space.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#080b12` | neutral | Primary text, deep backgrounds for hero sections, prominent borders – forming the base dark canvas |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, interactive elements (buttons, links). Serves as the primary light surface |
| Gridline Gray | `#d9d9d9` | neutral | Table borders, subtle dividers – providing visual structure without distraction |
| Subtle Ash | `#4f535e` | neutral | Secondary text, muted borders, footer text – for less prominent information and structural outlines |
| Surface Off-White | `#f6f7f8` | neutral | Alternative background for content sections, subtle card elevation – distinguishing content blocks without high contrast |
| Cool Stone | `#b5b9c4` | neutral | Tertiary text, inactive element borders, light backgrounds for elements like disabled states |
| Steel Gray | `#808080` | neutral | Placeholder text, icons, and muted UI elements within a dark context |
| Dark Overlay | `#35373e` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |
| Border Haze | `#edeef1` | neutral | Light borders and backgrounds for specific UI elements, like subtle button fills |
| Cerulean Accent | `#0a33ff` | brand | Interactive elements, focus indicators, highlight accents – a clear, vivid blue for emphasis |
| Deep Sea Blue | `#061353` | brand | Badge text on lighter backgrounds – indicating a specific type or category |
| Lavender Touch | `#dbdbee` | accent | Soft button backgrounds, table row separators – adding a slight tint to neutral surfaces |
| Slate Border | `#848a9c` | accent | Subtle borders within complex data tables – maintaining structure without heavy lines |
| Twilight Indigo | `#7f90ce` | accent | Background for secondary action buttons – a muted, purposeful blue |
| Success Green | `#03350f` | semantic | Text for success-state badges – indicating positive status |
| Success Background | `#d6ffe0` | semantic | Background for success-state badges |
| Warning Yellow | `#423301` | semantic | Text for warning-state badges – indicating caution |
| Warning Background | `#faeed1` | semantic | Background for warning-state badges |
| Error Red | `#580101` | semantic | Text for error-state badges – indicating negative status |
| Linear Gradient Blue | `#103b91` | brand | Decorative hero background, emphasizing depth and technology |
| Accent Gradient Blue | `#7b95c4` | accent | Subtle background accents, possibly for large graphical elements |

## Typography

### PP Neue Montreal

| Key | Value |
| --- | --- |
| weight | 400, 450, 500 |
| weights | 400, 450, 500 |
| sizes | 12px, 13px, 14px, 15px, 16px, 17px, 18px, 21px, 22px, 160px |
| lineHeight | 1.00, 1.10, 1.20, 1.28, 1.30, 1.40, 1.50, 1.60, 1.80 |
| letterSpacing | -0.1200em, -0.0100em, 0.0100em, 0.1400em, 0.1500em |
| fontFeatureSettings | 'tnum' |
| substitute | System UI (e.g. SF Pro Text, Roboto, Noto Sans) |
| role | Primary UI font for navigation, body text, buttons, and badges. Its varied weights and negative letter spacing on larger sizes create a precise, modern feel, while numeral tabular lining ensures consistent data display. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 9px, 10px, 11px, 12px, 13px, 14px, 15px |
| lineHeight | 1.20, 1.40, 1.60 |
| letterSpacing | -0.0200em, -0.0140em, -0.0120em, -0.0100em, -0.0080em, -0.0060em, 0.0200em |
| substitute | Roboto, Noto Sans |
| role | Secondary sans-serif for detailed information, tables, and tighter spaces. Offers a strong range of smaller sizes and weights for data-heavy interfaces, ensuring legibility at scale. |

### Atacama VAR

| Key | Value |
| --- | --- |
| weight | 317, 370, 400 |
| weights | 317, 370, 400 |
| sizes | 44px, 48px, 54px |
| lineHeight | 1.00, 1.10, 1.15, 1.20 |
| letterSpacing | -0.0600em, -0.0500em |
| substitute | Montserrat, Open Sans Light |
| role | Display font for large headlines. Its very light weights combined with strong negative letter spacing create a distinctive, authoritative yet refined presence for hero text and major section titles. |

### Suisse Intl

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.20 |
| letterSpacing | 0.0100em, 0.0800em |
| substitute | Helvetica Neue |
| role | Specific utility text, likely for small informational phrases or labels where a slightly wider tracking is desired compared to the main UI font. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.6 | 0.14 |
| subheading | 22 |  | 1.4 | -0.22 |
| heading | 44 |  | 1.15 | -2.64 |
| heading-lg | 48 |  | 1.1 | -2.88 |
| display | 160 |  | 1 | -19.2 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 100px |
| cards | 4px |
| input | 4px |
| badges | 4px |
| buttons | 4px |
| largeCard | 16px |

- **elementGap** — 14px
- **sectionGap** — 32px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background and default layer for content. |
| Surface Off-White | `#f6f7f8` | 1 | Slightly elevated background for distinct sections or very light card variations. |
| Card Surface | `#ffffff` | 2 | Default background for UI cards, typically with a subtle shadow for definition. |
| Modal/Elevated Content | `#ffffff` | 3 | Higher Z-index elements like modals or dropdowns, with a more pronounced shadow. |

## Components

### Primary Ghost Button

**Role:** Main call to action for the hero section and prominent interactions.

Text: Midnight Ink (#080b12), PP Neue Montreal, weight 400. Background: transparent. Border: 1px solid Midnight Ink (#080b12). Padding: 10px vertical, 24px horizontal. Radius: 4px.

### Secondary Ghost Button

**Role:** Auxiliary calls to action, less prominent than the primary.

Text: Cool Stone (#b5b9c4), PP Neue Montreal, weight 400. Background: transparent. Border: 1px solid Cool Stone (#b5b9c4). Padding: 14px vertical, 16px horizontal. Radius: 4px.

### Filled Action Button

**Role:** Interactive button for discrete actions, like 'Enrich'.

Text: Canvas White (#ffffff). Background: Twilight Indigo (#7f90ce). Padding: 10px vertical, 24px horizontal. Radius: 4px.

### Navigation Link

**Role:** Top-level navigation items and in-page links.

Text: Midnight Ink (#080b12) on light backgrounds, Canvas White (#ffffff) on dark backgrounds, PP Neue Montreal, various weights. Background: transparent. No border. No explicit padding.

### Simple Card

**Role:** Standard container for content, features, or data blocks.

Background: Canvas White (#ffffff). Radius: 4px. Shadow: rgba(0, 0, 0, 0.06) 0px 1px 3px 0px. Padding: 12px.

### Sectional Card

**Role:** Larger, more distinct content sections, providing visual separation.

Background: Surface Off-White (#f6f7f8). Radius: 16px. No shadow. Padding: 0px.

### Success Badge

**Role:** Visual indicator for successful or positive statuses.

Text: Success Green (#03350f), Inter, weight 400. Background: Success Background (#e4f3e2). Radius: 4px. Padding: 2px vertical, 6px horizontal.

### Warning Badge

**Role:** Visual indicator for warning or yellow statuses.

Text: Warning Yellow (#423301), Inter, weight 400. Background: Warning Background (#faeed1). Radius: 4px. Padding: 2px vertical, 6px horizontal.

### Error Badge

**Role:** Visual indicator for error or negative statuses.

Text: Error Red (#580101), Inter, weight 400. Background: Error Background (#ffe8ed). Radius: 4px. Padding: 2px vertical, 6px horizontal.

### Table Cell

**Role:** Individual cells within data tables.

Text: Midnight Ink (#080b12), Inter. Border: 1px solid Gridline Gray (#d9d9d9). Background: various, often Canvas White (#ffffff) or Lavender Touch (#dbdbee) for alternating rows. Padding varies between 6-12px.

## Layout

The page structure combines a full-bleed dark hero section with a distinct linear gradient at the top, transitioning into a predominantly max-width (likely 1200px) contained layout for the rest of the content, centered on the page. Sections alternate between Canvas White (#ffffff) and Surface Off-White (#f6f7f8) backgrounds, creating a clear vertical rhythm. Content often arranges in two-column layouts where text and visuals alternate positions, establishing a clear visual flow. Feature lists are clean, often presented as checkmark-led bullet points in single columns. Interactive data tables use fine Gridline Gray (#d9d9d9) borders and subtle row highlighting. Navigation is a sticky top bar with clearly delineated links and a strong 'Request a demo' call to action.

## Imagery

This design system uses a combination of direct product screenshots, stylized illustrations, and functional icons. Product screenshots are typically contained within cards, presented on white or subtly tinted backgrounds, often layered or shown from a slight isometric perspective to showcase multi-panel interfaces. Illustrations are minimal and abstract, using flattened shapes and subtle gradients that echo the brand's blues and grays, serving a decorative or conceptual purpose without being overly detailed. Icons are outlined, with a moderate stroke weight and primarily monochromatic, using Midnight Ink or Subtle Ash for clarity. Imagery density is moderate, used to break up text-heavy sections or to visually explain complex features, without dominating the overall layout.

## Dos & Donts

### Do

- Prioritize Midnight Ink (#080b12) for all primary text and dark UI backgrounds to maintain a strong, authoritative contrast.
- Use Canvas White (#ffffff) as the dominant background for content sections and surfaces that require high readability and a sense of openness.
- Apply 4px border radius for all interactive elements like buttons, badges, and card corners to ensure a consistent, subtle softening.
- Employ Atacama VAR (weights 317-400, with aggressive negative letter spacing) exclusively for large headings (44px and up) to create a distinctive, restrained impact.
- Utilize Lavender Touch (#dbdbee) for subtle background variations in tables or to create a mild distinction for interactive elements.
- Use the Cerulean Accent (#0a33ff) sparingly for focus states, interactive highlights, and brand-specific iconography.
- Maintain a clear visual hierarchy by differentiating text tones: Midnight Ink (#080b12) for primary, Subtle Ash (#4f535e) for secondary, and Cool Stone (#b5b9c4) for tertiary content.

### Don't

- Avoid using highly saturated colors for large surface areas; reserve them strictly for functional accents and status indicators.
- Do not use heavy shadows or gradients on typical cards or buttons; surfaces should appear flat or with minimal, subtle elevation (rgba(0, 0, 0, 0.06) 0px 1px 3px 0px).
- Do not introduce additional font families or weights beyond the defined PP Neue Montreal, Inter, Atacama VAR, and Suisse Intl to preserve typographic consistency.
- Avoid arbitrary border radii; stick to 4px for most elements, 16px for large sectional cards, and 100px for pill-shaped elements.
- Do not clutter content with excessive borders or dividers; use Gridline Gray (#d9d9d9) or Subtle Ash (#4f535e) sparingly for necessary structural separation.
- Do not rely solely on color to convey status; always pair semantic colors with meaningful icons or text labels.
- Avoid inconsistent spacing; adhere to the defined elementGap of 14px, cardPadding of 12px, and sectionGap of 32px for vertical rhythm.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- primary text: #080b12
- background: #ffffff
- border: #d9d9d9
- accent: #0a33ff
- primary action: #7f90ce (filled action)

### 3-5 Example Component Prompts
1. Create a Primary Action Button: #7f90ce background, #020202 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a simple feature card: Background #ffffff, 4px border-radius, shadow: rgba(0, 0, 0, 0.06) 0px 1px 3px 0px, 12px padding. Heading: PP Neue Montreal, 22px size, weight 500, #080b12. Body text: Inter, 14px size, weight 400, #4f535e.
3. Implement a success badge: Background #e4f3e2, text #03350f, Inter, 12px size, weight 400, 4px radius, 2px vertical, 6px horizontal padding. Include a simple outlined checkmark icon before text if space allows (icon color: #03350f).

---
_Source: https://styles.refero.design/style/73cd9a6a-f861-4376-a3e1-e12f83c8960e_
