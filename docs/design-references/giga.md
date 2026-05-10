# Giga — Design Reference

> Deep night, mountain vista – a cosmic, digital calm.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://giga.ai](https://giga.ai) |
| Refero page | [https://styles.refero.design/style/607e0dbf-e2fc-45c9-b939-946b8981c156](https://styles.refero.design/style/607e0dbf-e2fc-45c9-b939-946b8981c156) |
| Theme | dark |
| Industry | ai |

## Overview

Giga's design system evokes a sense of deep, expansive calm, contrasting vast dark landscapes with focused, luminous information. Typography is understated and elegant, prioritizing legibility and a sense of gravitas. Interactive elements offer subtle feedback, and surfaces vary from transparent overlays to opaque dark canvases, creating depth and dimension without heavy use of shadows. The overall aesthetic is one of quiet power and precision, mirroring complex AI capabilities with a refined visual language.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Obsidian | `#000000` | neutral | Primary text, icon outlines, dark surface background, borders |
| Ghost | `#ffffff` | neutral | Light surface background, light text, highlights |
| Graphite | `#cccccc` | neutral | Card backgrounds, subtle dividers |
| Shadow | `#4d4d4d` | neutral | Subtle shadow tint for depth |
| Pebble Gray | `#969696` | neutral | Muted secondary text |
| Night Sky | `#161717` | neutral | Filled button backgrounds |
| Steel Gray | `#808080` | neutral | Tertiary text, subtle borders |
| Cosmic Dust | `#8a8f98` | neutral | Placeholder text, low-contrast information text |
| Ember Glow | `#fe2c02` | accent | Decorative link accents, alerts — a vivid, urgent highlight |
| Growth Green | `#49de80` | accent | Green outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Subtle Violet Gradient | `#ff9aea` | accent | Decorative background gradient used in product sections |
| Soft Mint Gradient | `#c3eaca` | accent | Decorative background gradient used in product sections |
| Sky Blue Gradient | `#88bbf5` | accent | Decorative background gradient used in product sections |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| role | System fallback, minimal UI text. |

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| weights | 300, 400, 500 |
| sizes | 8px, 9px, 10px, 11px, 12px, 13px, 14px, 16px, 29px, 44px |
| lineHeight | 1.00, 1.36, 1.50, 1.54, 1.64, 1.70, 1.71, 1.75, 1.85, 1.93, 2.00, 2.22 |
| letterSpacing | -0.0300em, 0.0070em |
| role | Primary UI text, body copy, and secondary headlines. Its variable letter-spacing for different sizes provides an adaptive, precise feel without being overtly tracked. |

### Geist Mono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 11px, 12px, 14px |
| lineHeight | 1.20, 2.00, 2.18 |
| letterSpacing | 0.0090em |
| role | Monospaced text for data, code snippets, or subtle highlights where a technical, precise voice is needed. The positive tracking provides visual breathing room. |

### Giga Sans Text Trial 500

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 14px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| role | Content body text and detailed descriptive text. |

### Giga Sans Display Trial 500

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 22px |
| lineHeight | 0.86 |
| letterSpacing | normal |
| role | Small display headings, where a compact, almost stacked look is desired due to the tight line height. |

### Inter Display

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 16px |
| lineHeight | 1.50, 1.51, 2.50 |
| letterSpacing | 0.0060em |
| role | Sub-headings and emphasized body text. The slight tracking enhances readability at smaller display sizes. |

### Emilio Light

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 24px, 41px, 48px |
| lineHeight | 1.20, 1.30 |
| letterSpacing | -0.0300em, -0.0200em |
| role | Primary headlines and hero text. The light weight and negative letter-spacing for larger sizes lend a sense of understated sophistication and calm, rather than shouting. |

### Giga Sans Display Trial 400

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 30px |
| lineHeight | 1.33, 1.50 |
| letterSpacing | normal |
| role | Medium-sized display headings. |

### Giga Sans Text Trial 400

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| role | Standard body text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.2 |  |
| heading | 22 |  | 0.86 |  |
| heading-lg | 30 |  | 1.33 |  |
| display | 48 |  | 1.2 | -1.44 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| icons | 9999px |
| buttons | 1000px |
| default | 10px |

- **elementGap** — 10px
- **sectionGap** — 48px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Space | `#000000` | 0 | Primary page background, creating a vast, dark canvas. |
| Subtle Nebula | `#cccccc` | 1 | Base background for sections and cards, providing a barely-there structural layer. |
| Whisper Card | `#00000033` | 2 | Elevated card background with slight transparency, allowing underlying elements to subtly show through. |
| Ghost Panel | `#00000005` | 3 | Secondary card backgrounds, an even lighter, more transparent layer. |

## Components

### Ghost Pill Button

**Role:** Secondary action or navigation with minimal visual weight.

Background: rgba(255, 255, 255, 0.05), Text: Obsidian, Border: Obsidian, Padding: 16px all sides, Radius: 1000px.

### Primary Filled Pill Button

**Role:** Main call to action, standing out with a solid background.

Background: Ghost, Text: Obsidian, Border: Obsidian, Padding: 9px vertical, 20px horizontal, Radius: 1000px.

### Dark Filled Pill Button

**Role:** Call to action on lighter backgrounds, inverted version of the primary.

Background: Night Sky, Text: Obsidian, Border: Obsidian, Padding: 9px vertical, 20px horizontal, Radius: 1000px.

### Small Decorative Pill Button

**Role:** Informational or tag-like elements with a subtle, dark background.

Background: rgba(0, 0, 0, 0.1), Text: Obsidian, Border: Obsidian, Padding: 0px vertical, 8px right, 14px left, Radius: 9999px.

### Transparent Card

**Role:** Content container with a very subtle, almost ghost-like presence.

Background: rgba(0, 0, 0, 0.2), Border: none, Radius: 16px, Padding: 24px vertical, 24px right, 0px left.

### Subtle Inset Card

**Role:** Small, nested content area or decorative container.

Background: rgba(0, 0, 0, 0.02), Border: none, Radius: 8px, Padding: 2px all sides.

### Standard Card

**Role:** Primary content grouping container.

Background: rgba(0, 0, 0, 0.02), Border: none, Radius: 16px, Padding: 24px all sides.

## Layout

The page maintains a centered content model with an implied max-width, while hero sections can be full-bleed with panoramic imagery. The initial hero features a full-viewport, dark, landscape photograph with a centered headline stack and a prominent call-to-action. Subsequent sections often alternate between dark backgrounds (#000000) and slightly lighter, more textured dark surfaces (like rgba(0,0,0,0.2) or #cccccc). Content is typically arranged in left-aligned stacks or a two-column layout with text to the left and product visuals/cards to the right. Vertical rhythm is maintained with consistent section gaps of approximately 48px, creating a spacious but deliberate flow. Navigation is primarily a top sticky header with minimal links and a pill-shaped 'Talk to us' button.

## Imagery

This system primarily uses atmospheric, wide-angle photography featuring natural landscapes, often mountains or abstract patterns, with a dark, moody overlay. Imagery is used as a full-bleed background for hero sections, setting a grand, serene tone. When product interfaces are shown, they are clean, contained rectangles with sharp edges against photographic backdrops. Icons are minimal, outlined (Obsidian outline on dark backgrounds, Ghost on light), suggesting a focus on clarity rather than heavy decoration. The overall impression is image-heavy in hero sections, with imagery serving decorative atmosphere and product showcase, usually contained within a content area on darker backgrounds.

## Dos & Donts

### Do

- Use Obsidian (#000000) for primary headlines and body text to establish a strong, contrasted foundation.
- Apply Emilio Light font with negative letter-spacing for large headlines (e.g., 48px, weight 300, ls = -0.0300em) to create an elegant, restrained presence.
- Utilize Ghost Pill Button (background: rgba(255, 255, 255, 0.05), text: Obsidian, 1000px radius) for secondary actions or subtle navigational elements.
- Implement a 10px elementGap for consistent tight spacing between most inline and block elements.
- Apply a 1000px border-radius to all buttons for a distinctly pill-shaped, soft appearance.
- Employ Graphite (#cccccc) for card backgrounds and subtle dividers to introduce hierarchical breaks without harsh lines.
- Use Ember Glow (#fe2c02) sparingly as a vivid accent for critical links or calls to attention, ensuring high impact.

### Don't

- Avoid using harsh, opaque borders; prefer subtle transparency or no border at all for containers.
- Do not introduce strong, colored backgrounds for full sections; rely on the subtle neutral palette for depth.
- Refrain from using heavily tracked body text; the Inter font with its specific letter-spacing adjustments should be applied as specified.
- Do not deviate from the pill-shaped radius (1000px) for buttons; it's a signature visual cue.
- Avoid standard box shadows; for elevation, use subtle, diffused shadows like rgba(0, 0, 0, 0.7) 0px 12px 32px -16px.
- Do not use highly saturated colors for large text blocks or backgrounds; reserve them for small, impactful accents.
- Resist using bold font weights for large headlines; the light weight of Emilio Light is key to the system's character.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #000000
- background: #000000
- border: #000000
- accent: #fe2c02
- primary action: #161717 (filled action)

### 3-5 Example Component Prompts
1. Create a Primary Action Button: #161717 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a feature card: use the Transparent Card with background rgba(0, 0, 0, 0.2), 16px radius, 24px vertical padding. Inside, an icon with Obsidian (#000000) fill and stroke. Below the icon, a heading 'Built to handle complexity' in Inter, weight 500, 29px size, line-height 1.5, color Ghost (#ffffff). Underneath, body text 'Explore Agent Canvas' in Giga Sans Text Trial 500, weight 400, 14px size, color Pebble Gray (#969696).
3. Generate a call-to-action banner: background Night Sky (#161717) spanning full width. Place a heading 'Join the conversation' in Inter, weight 500, 44px size, line-height 1, color Ghost (#ffffff). To its right, a Dark Filled Pill Button with text 'Learn more', background Night Sky (#161717), text Ghost (#ffffff), 9px vertical and 20px horizontal padding, 1000px radius.

---
_Source: https://styles.refero.design/style/607e0dbf-e2fc-45c9-b939-946b8981c156_
