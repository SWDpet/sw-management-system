# Rive — Design Reference

> Precision Night Vision. The interface is a specialized lens, cutting through darkness to focus on technical details with absolute clarity.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://rive.app](https://rive.app) |
| Refero page | [https://styles.refero.design/style/6323a42b-3b47-4774-92e4-15651a9ba2ac](https://styles.refero.design/style/6323a42b-3b47-4774-92e4-15651a9ba2ac) |
| Theme | dark |
| Industry | design |

## Overview

The design feels like a high-end piece of creative software, a focused digital workshop cloaked in darkness. A pure black #000000 canvas eliminates all distraction, forcing attention onto the content and UI. The technical, wide-set 'Tomorrow' typeface is the defining visual signature, establishing a futuristic, blueprint-like quality for all headings. A single, warm 'Ignition Orange' accent appears only on primary CTAs, acting as a functional guidance point in the monochromatic environment. Elevation is conveyed not by shadows but by subtle shifts in dark gray surfaces, creating depth like nested panels on a piece of precision hardware.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ignition Orange | `#ffa41c` | brand | Primary CTAs and eyebrow labels — the sole warm accent, providing a clear, unmissable call to action. |
| Obsidian | `#000000` | neutral | Primary page background. |
| Charcoal | `#111111` | neutral | Secondary surfaces, like contained button backgrounds. |
| Graphite | `#1d1d1d` | neutral | Tertiary surfaces, small button backgrounds. |
| Tarmac | `#262626` | neutral | Interactive element backgrounds. |
| Stark White | `#ffffff` | neutral | Headline text, icon fills. |
| Cloud | `#f1f1f1` | neutral | High-emphasis body text. |
| Ash | `#aaaaaa` | neutral | Standard body text. |
| Dusk Gray | `#999999` | neutral | Secondary text, navigation links. |

## Typography

### Tomorrow

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 11px, 12px, 14px, 20px, 24px, 40px |
| lineHeight | 1.00, 1.20, 1.30, 1.40, 1.60 |
| letterSpacing | Negative at large sizes (-1.32px at 40px) for tight headlines, positive at small sizes (+1.1px at 11px) for legible UI labels. |
| substitute | Share Tech Mono, Roboto Mono |
| role | All headings, subheadings, and prominent UI labels. Its technical, semi-monospace structure and distinctive letter-spacing define the brand's futuristic, tool-like identity. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 13px, 16px |
| lineHeight | 1.20, 1.60, 1.70 |
| substitute | Inter, Roboto, sans-serif |
| role | All body copy and long-form text. Its neutrality provides a readable foundation that complements the stylized 'Tomorrow' headlines. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | System UI |
| role | Fallback and utilitarian text in less prominent contexts. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.1 | 1.1 |
| body | 16 |  | 1.6 |  |
| subheading | 20 |  | 1.3 | 0.2 |
| heading-sm | 24 |  | 1.2 | 0.19 |
| heading-lg | 40 |  | 1 | -1.32 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 4-8px |
| cards | 8px |
| inputs | 8px |
| buttons | 8px |

- **elementGap** — 8-16px
- **sectionGap** — 80-120px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Base | `#000000` | 0 | Page background |
| Raised | `#111111` | 1 | Secondary interactive surfaces |
| Overlay | `#1d1d1d` | 2 | Tertiary buttons and interactive tags |

## Components

### CTA Button Group

### Rive Editor Feature Card

### Category Tag Cards Grid

### Primary CTA Button

**Role:** The main call to action in a view.

Background: Ignition Orange (#ffa41c). Text: Stark White (#ffffff). Padding: 12px 24px. Radius: 8px. Font: Inter Bold.

### Secondary CTA Button

**Role:** Secondary action, like downloads or alternative options.

Background: Charcoal (#111111) or Graphite (#1d1d1d). Text: Stark White (#ffffff). Padding: 12px 24px. Radius: 8px. Font: Inter.

### Tertiary Button

**Role:** Small, supplemental actions like 'Learn More' or 'Features'.

Background: Graphite (#1d1d1d). Text: Stark White (#ffffff) or Ash (#aaaaaa). Padding: 8px 10px. Radius: 8px. Font: Tomorrow or Inter, often with an icon.

### Navigation Link

**Role:** Header and footer navigation items.

Text-only button. Text color: Dusk Gray (#999999). Hover/Active color: Stark White (#ffffff). Font: Tomorrow 500.

### Eyebrow Label

**Role:** A small label preceding a major headline.

Text color: Ignition Orange (#ffa41c). Font: Tomorrow 500, all-caps. Often paired with a small icon.

### Category Tag Card

**Role:** Interactive tags for filtering or navigation, seen in a grid.

Background: Graphite (#1d1d1d). Border: 1px solid Tarmac (#262626). Text: Dusk Gray (#999999). Radius: 8px. On hover, background lightens and text turns white.

### Hero Headline

**Role:** The main H1 on the page.

Font: Tomorrow 500 at a large size (e.g., 40px+). Color: Stark White (#ffffff). Tightly tracked letter-spacing.

## Layout

The layout is full-bleed and centered, creating an immersive, focused environment. The hero section occupies the entire viewport with a large, centered headline stack. Subsequent content is presented in simple, centered text blocks or contained grid-like structures for tags and categories. Vertical rhythm is spacious, allowing each piece of information to stand alone in the expansive dark space.

## Imagery

Imagery is minimal to non-existent, placing full emphasis on the product's UI and capabilities. The visual language is defined by typography and stark color contrasts, not photography or illustration. Dark, empty containers act as placeholders, suggesting potential for embedded animations or product demos, but the core identity remains text- and UI-centric.

## Elevation philosophy

Elevation is achieved through layered, near-black surfaces (#111111, #1d1d1d) on a pure black (#000000) base, not box shadows. This creates a subtle, self-contained depth reminiscent of physical hardware panels rather than floating UI cards.

## Dos & Donts

### Do

- Use the 'Tomorrow' font for all headings, subheadings, and nav items.
- Reserve 'Ignition Orange' (#ffa41c) exclusively for a single primary CTA per view.
- Build all layouts on a pure 'Obsidian' (#000000) background.
- Differentiate interactive surfaces using the neutral palette progression: #111111, #1d1d1d, #262626.
- Maintain an 8px border-radius on all primary interactive components like buttons and cards.
- Apply specific letter-spacing rules for the 'Tomorrow' font: negative for large sizes, positive for small.
- Use 'Stark White' (#ffffff) for headlines and 'Ash' (#aaaaaa) or 'Dusk Gray' (#999999) for body copy.

### Don't

- Don't use drop shadows. Use colored surfaces for elevation.
- Don't introduce any new saturated colors. The palette is strictly monochromatic plus one orange accent.
- Don't use 'Ignition Orange' for text, borders, or non-interactive decorations.
- Don't use pill-shaped or fully-rounded buttons. Stick to the 8px radius.
- Don't use light backgrounds. The entire brand experience is dark.
- Don't use 'Tomorrow' for long paragraphs of body text; use 'Inter'.
- Don't use gradients. The aesthetic is solid color blocks.

## Notes

### Agent Prompt Guide

### Quick Color Reference:
- **Background:** `Obsidian (#000000)`
- **Text (Headlines):** `Stark White (#ffffff)`
- **Text (Body):** `Ash (#aaaaaa)`
- **Primary CTA:** `Ignition Orange (#ffa41c)`
- **Secondary Button BG:** `Charcoal (#111111)`
- **Borders/Dividers:** `Tarmac (#262626)`

### Example Component Prompts:
1.  **Hero Section:** "Create a hero section with a full-screen `Obsidian (#000000)` background. Add a headline 'DESIGN, CODE, ANIMATE' set in `Tomorrow` font at `40px` with `-1.32px` letter-spacing, color `Stark White (#ffffff)`. Below it, add a primary CTA button: 'Get Started', background `Ignition Orange (#ffa41c)`, text `Stark White (#ffffff)`, `8px` radius, `12px 24px` padding."
2.  **Eyebrow + Heading Block:** "Create a text block. Start with an eyebrow label 'RIVE EDITOR' in all-caps, `Tomorrow` font, `12px`, `Ignition Orange (#ffa41c)` color, `+0.84px` letter-spacing. Below it, add the main heading 'The Real Thing' in `Tomorrow` font, `24px`, `Stark White (#ffffff)`, `+0.19px` letter-spacing. Finally, add paragraph text in `Inter`, `16px`, `Ash (#aaaaaa)` color."
3.  **Tertiary Button Group:** "Create a horizontal group of two small buttons with 16px spacing between them. Button style: `Graphite (#1d1d1d)` background, `Stark White (#ffffff)` text, `8px 10px` padding, `8px` radius. Label them 'Learn More' and 'Features'."

---
_Source: https://styles.refero.design/style/6323a42b-3b47-4774-92e4-15651a9ba2ac_
