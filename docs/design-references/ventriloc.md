# Ventriloc — Design Reference

> Analytical architecture on a clean canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://ventriloc.ca/en](https://ventriloc.ca/en) |
| Refero page | [https://styles.refero.design/style/f99aca3e-5289-4595-a7cc-77a72052f4b8](https://styles.refero.design/style/f99aca3e-5289-4595-a7cc-77a72052f4b8) |
| Theme | light |
| Industry | ai |

## Overview

Ventriloc employs an analytical architecture aesthetic: a pristine gray-on-white canvas for data visualization. The design relies on sharp contrasts between dark text and light backgrounds, with a singular warm accent color, 'Sunset Orange', used to highlight key data points and interactive elements. Components are lightweight, often outlined or ghost-like, conveying a sense of precision and responsiveness without heavy ornamentation. The overall impression is one of clarity and focused information delivery.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Graphite | `#202020` | neutral | Primary text, darkest surface elements, active button text, strong borders |
| Canvas White | `#ffffff` | neutral | Pure backgrounds, key card surfaces, navigation background |
| Slate Mist | `#efefef` | neutral | Secondary card backgrounds, subtle section breaks |
| Cloud Whisper | `#f5f5f5` | neutral | Muted backgrounds, navigation item backgrounds, light borders |
| Warm Ivory | `#ebe6dd` | neutral | Subtle background shifts for content separation |
| Dark Shale | `#4d4d4d` | neutral | Secondary text, muted links, subtle dividers |
| Silver Ash | `#828282` | neutral | Tertiary text, inactive navigation items, placeholder text |
| Light Pearl | `#e8e8e8` | neutral | Thin dividers and subtle background accents |
| Sunset Orange | `#ff682c` | accent | Decorative accents, data visualization elements, highlight color for UI components |
| Data Gold | `#816729` | accent | Data visualization elements, secondary icon color, subtle branding accents |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 12px, 13px, 14px, 15px, 16px, 18px |
| lineHeight | 1.15, 1.20, 1.25, 1.33, 1.38, 1.43, 1.50 |
| letterSpacing | normal |
| substitute | system-ui |
| role | Body text, navigation items, button labels, small data points, most UI elements. Inter provides a highly legible, modern feel for dense information. |

### PolySans

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px, 13px, 16px, 32px, 40px, 66px |
| lineHeight | 0.91, 1.00, 1.13, 1.19, 1.20, 1.38 |
| letterSpacing | -0.0200em |
| substitute | Montserrat |
| role | Headlines, featured text, and select emphasis. Its slightly condensed and tightly spaced nature gives a distinctive, sharp, and authoritative voice. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0 |
| body | 15 |  | 1.33 | 0 |
| subheading | 18 |  | 1.25 | 0 |
| heading-sm | 32 |  | 1.19 | -0.64 |
| heading | 40 |  | 1.13 | -0.8 |
| display | 66 |  | 0.91 | -1.32 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| large | 20px |
| inputs | 20px |
| avatars | 200px |
| buttons | 20px |
| default | 8px |

- **elementGap** — 20px
- **sectionGap** — 80px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Base page background |
| Cloud Whisper | `#f5f5f5` | 1 | Lightest card backgrounds, very subtle background shifts |
| Slate Mist | `#efefef` | 2 | Primary card backgrounds, distinct content containers |
| Warm Ivory | `#ebe6dd` | 3 | Alternative subtle background for visual separation |

## Components

### Standard Nav Link

**Role:** Navigation item within headers and footers.

Text in Midnight Graphite (#202020), no background, 2px bottom border in Midnight Graphite on hover/active. Font is Inter 400 at 16px, line-height 1.25, normal letter-spacing, 0px padding. Vertical spacing of 8px around items.

### Muted Nav Link

**Role:** Navigation item, typically for less prominent sections or inactive states.

Text in Silver Ash (#828282), no background, no border. Font is Inter 400 at 16px, line-height 1.25, normal letter-spacing.

### Primary Ghost Button

**Role:** Call to action, typically in sections with high visual contrast.

Transparent background, Midnight Graphite (#202020) text, 2px bottom border in Midnight Graphite. Font is Inter 400 at 16px, line-height 1.25, normal letter-spacing. Padding is 0px top/bottom, 18px left/right. 20px border radius.

### Secondary Ghost Button

**Role:** Discreet calls to action or secondary actions.

Transparent background, Silver Ash (#828282) text, no border. Font is Inter 400 at 16px, line-height 1.25, normal letter-spacing. Padding 0px top/bottom, 0px left/right. 20px border radius.

### Highlight Card

**Role:** Information display, often for data visualizations or key summaries.

Background #efefef, 6px top-left border radius, 0px for others. Padding 70px top, 0px other sides. No shadow.

### Rounded Info Card

**Role:** General content container with softer edges.

Background #efefef, 20px border radius for all corners. No shadow.

### Metric Badge

**Role:** Small data labels or status indicators.

Transparent background, Midnight Graphite (#202020) text. No border radius. Inter 400 at 16px, normal letter spacing. No explicit padding.

## Layout

The page primarily employs a contained layout with a comfortable maximum width, centered on a Canvas White background. The hero section is a split two-column layout, featuring a large PolySans headline and body text on the left, juxtaposed with product screenshots displaying analytics on the right. Sections alternate between full-width content and max-width containers, often with a consistent 80px vertical section gap. Content arrangement frequently uses two-column layouts, either text alongside imagery or data cards. There's a minimal, sticky top navigation bar with text links and a 'Contact us' button. The overall density is comfortable, ensuring sufficient breathing room between elements.

## Imagery

The visual language focuses on a mix of product screenshots and abstract, data-driven illustrations. Product screenshots are typically clean, depicting dashboard interfaces with charts and metrics against a light background, sometimes slightly cropped or angled. Illustrations are organic, abstract data visualizations using the accent colors of Sunset Orange and Data Gold. Icons are outlined, simple, and functional, with a consistent stroke weight, occasionally filled with accent colors for emphasis. Imagery serves an explanatory and informative role, showcasing the product's capabilities and providing visual context to the data-driven narrative.

## Dos & Donts

### Do

- Prioritize Midnight Graphite (#202020) for all primary text and strong interactive elements to maintain high legibility.
- Utilize Canvas White (#ffffff) and Slate Mist (#efefef) as primary background and card surface colors, respectively, to establish clear visual hierarchy.
- Employ PolySans exclusively for headlines and featured content at larger sizes (32px, 40px, 66px) with a consistent letter spacing of -0.0200em to define distinct brand voice.
- Use Inter for all body text, smaller UI elements, and navigation, ensuring high readability across functional components.
- Apply Sunset Orange (#ff682c) sparingly for data visualization highlights, icon accents, and as a decorative accent for interactive states, never as a background for primary buttons.
- Maintain a default border-radius of 8px for most cards, and a more pronounced 20px for buttons and prominent interactive elements, creating a soft, approachable feel.
- Structure layouts with a comfortable 20px element gap for internal component spacing and a more generous 80px section gap for clear content block separation.

### Don't

- Avoid using highly saturated colors for large backgrounds or extensive text; the palette is predominantly neutral with targeted accents.
- Do not deviate from the established PolySans for headlines or Inter for body text; typographic variations should be limited to weight and size within these families.
- Refrain from adding arbitrary shadows or complex gradients; the system prioritizes flat surfaces and subtle elevation.
- Do not use accent colors like Sunset Orange (#ff682c) or Data Gold (#816729) for general body text or navigational elements where primary text color is required.
- Avoid tight spacing: ensure generous padding (e.g., 40px for cards) and element gaps (20px) to maintain a comfortable reading experience.
- Do not introduce additional border radii other than 3px, 8px, 12px, 20px, and 200px (for avatars/pills); consistency in corner rounding is key.
- Avoid using multiple font weights or styles within a single sentence or small interactive component, beyond what is defined in the typography section, to prevent visual clutter.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #202020
background: #ffffff
border: #202020
accent: #ff682c
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Design a feature card: Slate Mist (#efefef) background, 8px border-radius (top-left 6px others 0px), 70px top padding. Headline 'Finance Dashboard' using PolySans 32px, #202020, letter-spacing -0.64px. Body text 'Track revenues and profits' using Inter 14px, #4d4d4d.

---
_Source: https://styles.refero.design/style/f99aca3e-5289-4595-a7cc-77a72052f4b8_
