# AngelList — Design Reference

> Midnight Ledger, luminous script.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.angellist.com](https://www.angellist.com) |
| Refero page | [https://styles.refero.design/style/c75603c7-492d-4c26-9744-9acc22fe6225](https://styles.refero.design/style/c75603c7-492d-4c26-9744-9acc22fe6225) |
| Theme | dark |
| Industry | fintech |

## Overview

AngelList employs a sophisticated, dark-mode financial intelligence aesthetic. Deep, almost-black surfaces create a sense of gravitas, punctuated by crisp, light typography that stands out with authoritative clarity. Subtle green and muted violet accents are sparingly used for functional elements, guiding the eye without overwhelming the serious tone. Components feature soft, rounded edges and light borders, maintaining a sense of approachability within the otherwise robust system.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#001d21` | neutral | Primary surface background, dark text on lighter surfaces. Conveys a deep, foundational tone |
| Canvas White | `#ffffff` | neutral | Lightest surface background, reverse text on dark surfaces, border accents. Creates high contrast and visual breaks |
| Ash Cloud | `#e1e3e3` | neutral | Secondary text variant, subtle separators and disabled states. Offers a softer contrast |
| Smoke Gray | `#68706f` | neutral | Muted body text, helper text, and subtle icon strokes. For less prominent information |
| Pale Stone | `#f1f3f2` | neutral | Subtle background for UI elements like navigation or ghost buttons. Softens primary dark backgrounds |
| Charcoal Tint | `#002b31` | neutral | Card backgrounds, primary button fills, interactive element borders. A slightly lighter shade of Midnight Ink |
| Silver Thread | `#ccd5d6` | neutral | Hairline borders, subtle dividers, and inactive states. Provides internal structure without stark lines |
| Mint Glaze | `#e0fee6` | brand | Gray text accent for links, tags, and emphasized short phrases |
| Verdant Mist | `#cdeed3` | brand | Navigation text, icons, and subtle headings. A muted green for clear yet unobtrusive signaling |
| Lavender Haze | `#cdcbff` | brand | Decorative fills and accent text for a soft, modern touch. Provides a gentle contrast to dark backgrounds |
| Royal Violet | `#bdbbff` | brand | Accent for interactive elements, such as button backgrounds. A soft, approachable violet |
| Deep Plum | `#3a25f5` | brand | Interactive link colors and borders. A vivid, almost electric violet for clickable elements |
| Harvest Gold | `#6e5c3b` | accent | Specific background sections to convey richness or importance. A warm, earthy tone |
| Patina | `#5d5337` | accent | Subtle border accents for cards or specific data displays. A desaturated, metallic hue |
| Bronze Slate | `#544a2e` | accent | Alternative muted text or border color. A deeper, more earthy gray with a hint of warmth |
| Cosmic Gradient | `#6258ff` | brand | Decorative background for brand sections, creating flow and depth. Incorporates multiple brand hues |

## Typography

### angellist

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 11px, 12px, 13px, 14px, 16px, 18px, 90px, 216px |
| lineHeight | 0.90, 1.10, 1.20, 1.40, 1.45, 1.50, 1.60, 2.64 |
| letterSpacing | -0.0500em at 216px, -0.0050em at 90px, normal at 11-18px |
| fontFeatureSettings | "lnum" on, "ss01" on, "tnum" on |
| substitute | Inter, Arial, sans-serif |
| role | Primary typeface for body text, interface elements, and smaller headings. Features stylistic alternates and tabular figures for precise data display, embodying a structured yet clean feel. |

### angellistDisplay

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 28px, 38px, 60px, 90px, 112px, 216px |
| lineHeight | 0.90, 1.10, 1.15, 1.25, 1.30 |
| letterSpacing | -0.0400em at 216px, -0.0150em at 90px |
| substitute | Inter, Arial, sans-serif |
| role | Used exclusively for large, impactful display headings. Its slightly tighter tracking at larger sizes maintains legibility and prominence without feeling bloated. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.4 |  |
| body | 14 |  | 1.5 |  |
| subheading | 18 |  | 1.2 | -0.09 |
| heading | 38 |  | 1.15 | -0.57 |
| heading-lg | 90 |  | 1.1 | -1.35 |
| display | 216 |  | 0.9 | -8.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| buttons | 9999px |
| default | 4px |

- **elementGap** — 4px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Ink Canvas | `#001d21` | 1 | Primary page background, providing a deep, consistent base. |
| Charcoal Tint Card | `#002b31` | 2 | Elevated cards and contextual content containers, offering a subtle visual lift from the canvas. |
| Pale Stone Accent | `#f1f3f2` | 3 | Used for navigation elements or subtle ghost button backgrounds, providing a lighter contrast without being stark white. |
| Canvas White Overlay | `#ffffff` | 4 | Rarely used on body elements for high contrast sections or specific content framing, visible in footer/cookie consent. |

## Components

### Navigation Link (Active/Hover)

**Role:** Top navigation item, highlighted state

Text in Mint Glaze (#e0fee6) over a transparent background, transitioning to Verdant Mist (#cdeed3) on hover, with a 9999px border radius for a pill shape.

### Primary Ghost Button

**Role:** Call to action, less prominent

Background Charcoal Tint (rgba(0, 29, 33, 0.15)), text Verdant Mist (#cdeed3), with border radius 9999px. Padding typically 0px.

### Outlined Button

**Role:** Secondary action, distinctive outline

Background Charcoal Tint (#002b31), text Canvas White (#ffffff), with border radius 6px and 1px border in Charcoal Tint (#002b31). Padding 12px vertical, 16px horizontal.

### Primary Filled Button

**Role:** Primary call to action (e.g. 'Contact sales')

Background Royal Violet (#bdbbff), text Canvas White (#ffffff), with border radius 9999px. Padding 10px vertical, 24px horizontal.

### Card - Dark Surface

**Role:** Content container with elevated dark surface

Background Charcoal Tint (#002b31), border radius 12px, no shadow. Padding is typically 0, content within provides structure.

### Card - Transparent

**Role:** Content container for featured sections

Transparent background, no border radius, no shadow. Bottom padding 16px.

### Card - Light Accent

**Role:** Small informational card, e.g. cookie settings

Background rgb(243, 244, 242) (near Canvas White), border radius 3px, no shadow. Padding 2px vertical, 4px horizontal.

## Layout

The page primarily uses a full-bleed dark background for impact, with content sections constrained to a central max-width (likely 1200px based on component widths) that is not fixed, but implied. The hero section is full-bleed, featuring a large, centered headline on a dark canvas. Content flows in distinct blocks, often alternating background colors or using large, dark cards (#002b31) to break up sections. There's a common pattern of 4-column card grids for features. The rhythm is comfortable, with generous vertical spacing between sections. Navigation is handled by a fixed top bar on a dark background, with main links and a primary button.

## Imagery

The site uses a mix of tightly cropped, process-oriented photography showing people interacting with technology or collaborating, and abstract product screenshots. Photography includes candid, mid-tone, slightly desaturated images of people in professional settings, often with hands-on focus, serving to ground the abstract financial concepts in human activity. Product screenshots are clean, showcasing UI against dark backgrounds. Visuals are contained within cards, often with soft transitions or subtle overlays, without raw edges or heavy masking. Icons are generally outlined with a medium stroke weight, in monochromatic or brand accent colors, providing functional clarity.

## Dos & Donts

### Do

- Use Midnight Ink (#001d21) as the default page background for most sections, establishing a dark, serious foundation.
- Employ angellistDisplay font at weight 400 with a negative letter-spacing between -0.015em and -0.04em for all large headings (90px and above) to maintain a compact, impactful look.
- Prioritize Canvas White (#ffffff) text on all Midnight Ink (#001d21) and Charcoal Tint (#002b31) backgrounds for maximum legibility.
- Apply a 9999px border-radius to all primary action buttons and navigation pills to create a distinct, approachable, 'soft' interactive element.
- Use Silver Thread (#ccd5d6) for hairline borders and subtle dividers, ensuring visual separation without heavy lines.
- Introduce Mint Glaze (#e0fee6) or Verdant Mist (#cdeed3) sparingly as accent colors for active states, small highlights, or positive indicators.

### Don't

- Avoid using multiple chromatic colors in close proximity; maintain a largely monochromatic palette with selective accents.
- Do not use default system fonts; always utilize angellist or angellistDisplay variants for brand consistency.
- Do not introduce heavy shadows; the system favors subtle elevation through background color shifts and light borders.
- Avoid tight vertical spacing in content sections; ensure generous vertical buffer with a section gap of at least 48px.
- Do not place large blocks of text in accent colors; reserve them for highlights, interactive elements, or very short headings.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #001d21
border: #ccd5d6
accent: #e0fee6
primary action: #002b31 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #002b31 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a feature card: Charcoal Tint (#002b31) background, 12px radius, 16px padding. Heading at 18px angellist weight 700, Canvas White (#ffffff). Body text at 14px angellist weight 400, Smoke Gray (#68706f). Include a ghost button with background rgba(0, 29, 33, 0.15), Verdant Mist (#cdeed3) text, 9999px radius.
3. Create a navigation item: Verdant Mist (#cdeed3) text at 16px angellist weight 500. On hover, change text to Mint Glaze (#e0fee6) and apply a transparent background.

---
_Source: https://styles.refero.design/style/c75603c7-492d-4c26-9744-9acc22fe6225_
