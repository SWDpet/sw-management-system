# SpaceX — Design Reference

> Deep Space Command. A minimalist, dark-mode interface designed for high-stakes, information-dense environments, where every pixel counts.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://spacex.com](https://spacex.com) |
| Refero page | [https://styles.refero.design/style/13b74e34-b824-4d1d-bd2c-bb9bfbc2d6e1](https://styles.refero.design/style/13b74e34-b824-4d1d-bd2c-bb9bfbc2d6e1) |
| Theme | dark |
| Industry | other |

## Overview

This system projects an aura of ambitious precision, like a mission control screen against the vastness of space. The stark black background, illuminated by near-white text and subtle, dark-gray interactive states, creates a sense of focused technological endeavor. Typography is paramount, with hyper-legible, widely-tracked sans-serifs conveying information with clarity and a deliberate, almost uniform weight.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Void | `#000000` | neutral | Primary background, button backgrounds for ghost variant, navigation overlay. Provides a canvas for bright content. |
| Lunar Dust | `#f0f0fa` | neutral | Primary text color, iconographic elements, primary button text. Stands out distinctly against the dark background. |
| Obsidian Blend | `#00000080` | neutral | Primary button background, providing a subtle interactive surface that is distinct from the pure black background. |
| Starfield Gray | `#f0f0fa59` | neutral | Button borders for ghost buttons, subtle dividers. Creates definition without visual weight. |
| Interstellar Gray | `#545457` | neutral | Subtle border color for non-primary interactive elements, providing a low-contrast indication. |
| Cosmic Gray | `#404040` | neutral | Border color for select navigation elements, signifying separation or interactive zones. |
| Dark Overlay | `#000000cc` | neutral | Used for background overlays, modals, and hover states, providing a deeper, more occlusive layer above content. |

## Typography

### D-DIN

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 10px, 12px, 13px, 16px |
| lineHeight | 0.94, 1.00, 1.20, 1.50, 1.70, 2.00 |
| letterSpacing | 0.09em, 0.10em |
| substitute | Arial, sans-serif |
| role | Primary text font for body, navigation, buttons, and most informational elements. Its wide tracking at smaller sizes enforces legibility in a dark UI, creating a precise, technical feel. |

### D-DIN-Bold

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 48px |
| lineHeight | 1.00, 1.25 |
| letterSpacing | 0.02em |
| substitute | Arial Bold, sans-serif |
| role | Used exclusively for prominent headings. Despite being 'bold', its weight is 400 (normal) and the reduced letter-spacing compared to the regular D-DIN, combined with the large size, creates a strong, commanding presence without shouting. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0.09 |
| button | 16 |  | 1.2 | 0.1 |
| heading-lg | 48 |  | 1 | 0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 4px |
| navigation | 4px |
| pillButton | 32px |

- **elementGap** — 18px
- **sectionGap** — 60px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Ghost Primary Button Group

### Upcoming Launch Card

### Mission Stat Block

### Ghost Primary Button

**Role:** Primary call to action.

Features 'Obsidian Blend' (rgba(0, 0, 0, 0.5)) background, 'Lunar Dust' (#f0f0fa) text, and 'Starfield Gray' (rgba(240, 240, 250, 0.35)) top border. Corner radius is 4px. Padding is 0px top/bottom, 20px left/right. Typography uses D-DIN at 16px with 0.10em letter-spacing.

### Pill Accent Button

**Role:** Secondary or alternative call to action, often for reservation or ordering.

Has a background of rgba(240, 240, 250, 0.1) which is a transparent 'Lunar Dust' tint, with 'Midnight Void' (#000000) text. Features a prominent 32px border radius, creating a pill shape. Padding is 18px all around. Typography uses D-DIN at 16px with 0.10em letter-spacing.

### Navigation Link

**Role:** Top-level navigation items.

Text is 'Lunar Dust' (#f0f0fa) using the D-DIN font, likely at 13px with 0.10em letter-spacing. Interaction states might involve 'Cosmic Gray' (#404040) borders or subtle background changes.

### Body Text Block

**Role:** Informational paragraphs.

Set in 'Lunar Dust' (#f0f0fa) using the D-DIN font at 16px, with a line height of 1.5 or 1.7 to ensure readability against the dark background. Letter spacing is normal to slightly increased for legibility.

### Display Heading

**Role:** Main page titles and prominent section headings.

Set in 'Lunar Dust' (#f0f0fa) using D-DIN-Bold at 48px, with a line height of 1.0. Letter spacing is 0.02em, giving it a slightly tighter, more impactful appearance than other text.

## Layout

The page model is primarily full-bleed, with content anchored to a `pageMaxWidth` that dynamically adjusts to the viewport, ensuring large dramatic visuals. The hero sections feature full-viewport background images/videos with centered or left-aligned 'Display Heading' text in 'Lunar Dust'. Sections are vertically stacked with significant `sectionGap` (60px), creating spacious breathing room. Content is often presented in a simple, centered stack or a two-column layout with text on one side and an image or video on the other. Navigation is a sticky top bar, minimal and unobtrusive, dissolving into text links only. The overall density is spacious, allowing visuals to take precedence.

## Imagery

The site uses high-resolution, full-bleed photography. Images feature dramatic celestial bodies (Mars, Earth), rocket launches, and advanced spacecraft. The treatment is realistic and often features dark, moody backdrops blending seamlessly with the site's 'Midnight Void' background. There is no masking or excessive graphic treatment; images are presented as raw, powerful, and central to the narrative. Icons are minimalist, outlined, and monochromatic ('Lunar Dust'), appearing only on functional interactive elements, not for decoration. The imagery serves as both atmosphere and direct product showcase, dominating sections to convey the scale and ambition of space exploration.

## Dos & Donts

### Do

- Prioritize 'Midnight Void' (#000000) for backgrounds and 'Lunar Dust' (#f0f0fa) for primary text to maintain stark contrast.
- Use D-DIN for all body text, navigation, and buttons, with D-DIN-Bold exclusively for hero headings at 48px.
- Apply a 0.09em or 0.10em letter-spacing to D-DIN text to enforce a deliberate, spacious feel.
- Utilize 20px horizontal padding for most interactive elements and buttons, such as the Ghost Primary Button.
- Ensure all interactive buttons feature a 4px `radius` unless specifically designed as a 'Pill Accent Button' (32px radius).
- Employ `Obsidian Blend` (rgba(0, 0, 0, 0.5)) for backgrounds of primary action buttons, providing a subtle interactive layer.

### Don't

- Avoid using highly saturated colors; maintain a monochromatic palette with only a hint of transparent `Lunar Dust` for subtle accents.
- Do not deviate from the D-DIN font family for any text elements; custom fonts are a core identifier.
- Never use drop shadows or complex elevation schemes; depth is created through background changes and subtle borders.
- Do not use generic button styles; always apply either the 'Ghost Primary Button' or 'Pill Accent Button' specifications.
- Avoid tight letter-spacing for standard text; the liberal use of letter-spacing is a signature choice.
- Do not introduce gradients or complex background imagery in areas where text readability is paramount.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #f0f0fa (Lunar Dust)
- Background: #000000 (Midnight Void)
- Primary Button Background: rgba(0, 0, 0, 0.5) (Obsidian Blend)
- Button Border: rgba(240, 240, 250, 0.35) (Starfield Gray)
- Accent (Pill Btn Background): rgba(240, 240, 250, 0.1)

### 3-5 Example Component Prompts
1. Create a hero section: full-bleed background image `path/to/mars.jpg`. Centered headline `Making Life Multiplanetary` using D-DIN-Bold at 48px, #f0f0fa, line-height 1.0, letter-spacing 0.02em. Below it, body text `SpaceX was founded under the belief...` in D-DIN 16px, #f0f0fa, line-height 1.5. Below that, a button labeled `EXPLORE ->` with background rgba(0, 0, 0, 0.5), text #f0f0fa, border rgba(240, 240, 250, 0.35), 4px radius, 0px top/bottom padding, 20px left/right padding, D-DIN 16px, letter-spacing 0.1em.
2. Design a navigation header: background #000000, height 74px. Links `VEHICLES`, `STARLINK` etc. are #f0f0fa, D-DIN 13px, letter-spacing 0.1em. Align text links to the left and a `UPCOMING LAUNCHES` dropdown to the right. Use 4px radius for any dropdown elements.
3. Implement a 'Pill Accent Button': text `RESERVE YOUR RIDE` in #000000, background rgba(240, 240, 250, 0.1), 32px border-radius, 18px padding all around. Typography D-DIN 16px, letter-spacing 0.1em.
4. Generate a footer: background #000000. Text links `TWITTER`, `YOUTUBE` etc. are #f0f0fa, D-DIN 12px, letter-spacing 0.1em. Links should be arranged horizontally with `columnGap` of 18px and `rowGap` of 12px.

---
_Source: https://styles.refero.design/style/13b74e34-b824-4d1d-bd2c-bb9bfbc2d6e1_
