# Lpalo — Design Reference

> Blush Playground: A soft, inviting canvas for bold, playful content.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://lpalo.com](https://lpalo.com) |
| Refero page | [https://styles.refero.design/style/79b4ebc4-30f6-45b6-b2d2-922e28e05ca9](https://styles.refero.design/style/79b4ebc4-30f6-45b6-b2d2-922e28e05ca9) |
| Theme | light |
| Industry | media |

## Overview

Lpalo exudes a playful, yet bold aesthetic, centered around a light, blush pink canvas that evokes warmth and imagination. Prominent, heavy typography anchors content, while a cast of vibrant, sometimes unexpected, accent colors punctuates the interface without overwhelming. Elements are rounded and friendly, suggesting an approachable, creative space for children's content. The visual system balances strong typographic presence with whimsical, illustrative details.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Pink | `#f6e0db` | neutral | Primary page background, light surface |
| Surface White | `#ffffff` | neutral | Card backgrounds, secondary surface |
| Charcoal Text | `#000000` | neutral | Primary text, borders, active navigation outlines |
| Pumpkin Accent | `#ef724f` | accent | Orange wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Lemon Highlight | `#e7db4c` | accent | Decorative splashes, card accents, playful iconography |
| Bubblegum Pink | `#981082` | accent | Card backgrounds, decorative accents, secondary navigation hover states |
| Spring Green | `#6ed311` | accent | Decorative iconography, small accent fills |
| Seafoam Accent | `#ace2df` | accent | Background for subtle accents and illustrations, card backgrounds |
| Lavender Glow | `#e69dff` | accent | Illustrative details, card backgrounds, soft visual punctuation |
| Sky Blue | `#84bfff` | accent | Card backgrounds, illustrative elements, subtle background fills |
| Deep Blue | `#5196ff` | accent | Used sparingly for precise illustrative accents |

## Typography

### Alfa Slab One

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 35px, 46px |
| lineHeight | 1.09, 1.20 |
| letterSpacing | normal |
| substitute | Bebas Neue |
| role | Display headlines and prominent text areas where a heavy, impactful presence is desired. Its singular weight ensures consistent visual density. |

### Manrope

| Key | Value |
| --- | --- |
| weight | 400, 500, 700, 800 |
| sizes | 12px, 25px, 34px, 50px, 120px |
| lineHeight | 1.00, 1.12, 1.16, 1.24, 1.50, 1.60 |
| letterSpacing | normal |
| substitute | Inter |
| role | Body text, navigation, and detailed information. The range of weights provides flexibility, with heavier weights used for subheadings and emphasis, complementing the display font's boldness. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 |  |
| body-sm | 16 |  | 1.2 |  |
| body | 25 |  | 1.24 |  |
| subheading | 34 |  | 1.16 |  |
| heading-lg | 46 |  | 1.09 |  |
| display | 120 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 10px |
| default | 47px |
| largeCards | 40px |
| navigation | 47px |

- **elementGap** — 10px
- **sectionGap** — 46px
- **cardPadding** — 15px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Pink | `#f6e0db` | 0 | Base page background. Provides a soft, inviting base color across the site. |
| Surface White | `#ffffff` | 1 | Primary card and interactive element background. Sits directly on the Canvas Pink. |

## Components

### Navigation Button

**Role:** Primary navigation links and interactive controls.

Buttons with a rounded-pill shape (47px radius), outlined with Charcoal Text (#000000, 2px stroke). Background is white (#ffffff) or Pumpkin Accent (#ef724f) when active, creating clear visual feedback. Text is Manrope, Charcoal Text (#000000), 20px, medium weight.

### Default Card

**Role:** Content containers for information display.

White (#ffffff) background card with a subtle 10px border-radius, often appearing without a visible stroke. Padding varies, with common values of 16px bottom padding to create comfortable spacing for content.

### Accent Card - Rounded

**Role:** Highlighting specific content or categories with a playful aesthetic.

Cards feature bold, moderate to vivid accent colors like Sky Blue (#84bfff) or Bubblegum Pink (#981082) with a distinctive 40px border-radius. Inner padding is generous at 15px top/bottom and 25px left/right, providing ample breathing room for contents like text.

### Hero Headline

**Role:** Main page title or section headline.

Uses Alfa Slab One, 46-120px, weight 400, Charcoal Text (#000000), normal letter spacing. Delivered on the Canvas Pink (#f6e0db) background, it establishes a bold and direct tone.

## Layout

The page uses a full-bleed layout, filling the entire viewport with the Canvas Pink background. The hero section features a centered, oversized headline using Alfa Slab One, acting as a strong visual anchor. Content sections appear to follow a consistent vertical rhythm, though specific section gaps are moderate. Navigation is a horizontal bar at the top, employing the pill-shaped buttons. Content arrangement likely uses a combination of stacked elements and potential two or three-column card grids further down the page to showcase different podcast categories. Density is comfortable, with ample breathing room around elements.

## Imagery

The site favors outline illustrations with a soft, playful, and slightly quirky style. These illustrations often feature robots, headphones, and abstract objects rendered with thin black strokes and minimal, bright fills pulled from the accent color palette (Sky Blue, Pumpkin Accent, Spring Green). Imagery is integrated as decorative elements scattered across the canvas, avoiding full-bleed or contained hero imagery. There are no photographs or realistic product renders, maintaining a whimsical, illustrative identity.

## Dos & Donts

### Do

- Prioritize Canvas Pink (#f6e0db) as the default background for new pages to maintain brand warmth.
- Use Alfa Slab One for all main headlines and prominent display text to leverage its bold, heavy character.
- Apply a 47px border-radius to all interactive buttons and navigation elements for a consistently soft, approachable feel.
- Introduce accent colors like Pumpkin Accent (#ef724f) or Bubblegum Pink (#981082) primarily as background fills for interactive elements or distinct card sections, not just decorative flourishes.
- Ensure all primary text uses Charcoal Text (#000000) for high contrast and legibility against light backgrounds.
- Maintain a comfortable density with element gaps of 10px between inline elements and 15px vertical padding within cards.

### Don't

- Avoid using saturated accent colors for large text blocks; reserve them for backgrounds and smaller, functional elements.
- Do not introduce sharp corners or small radii; the design language consistently applies generous rounding (10px, 40px, or 47px).
- Refrain from using shadows for elevation; surfaces are defined by background color changes and border outlines, not depth.
- Do not deviate from the Manrope and Alfa Slab One font pairing; these are the only sanctioned typefaces.
- Avoid monochromatic designs; the system relies on a playful, varied accent palette to provide visual interest and categorize content.
- Do not implement gradients; the styling relies on solid color blocks and outlines.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #f6e0db
border: #000000
accent: #ef724f
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Design a feature card for 'Emotions & Intimacy': Sky Blue (#84bfff) background, 40px border-radius, 15px top/bottom padding, 25px left/right padding, text Manrope 25px, Charcoal Text (#000000).
3. Generate a main hero headline: Alfa Slab One, 120px, weight 400, Charcoal Text (#000000), normal letter spacing, placed on a Canvas Pink (#f6e0db) background.

---
_Source: https://styles.refero.design/style/79b4ebc4-30f6-45b6-b2d2-922e28e05ca9_
