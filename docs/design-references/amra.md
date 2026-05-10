# amra — Design Reference

> Frosted glass clarity

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.amra.com](https://www.amra.com) |
| Refero page | [https://styles.refero.design/style/d5ed8712-0e42-4c6c-83b1-b3d7f27d1d10](https://styles.refero.design/style/d5ed8712-0e42-4c6c-83b1-b3d7f27d1d10) |
| Theme | light |
| Industry | media |

## Overview

Amra presents a clean, spacious interface built on a predominantly achromatic palette, punctuated by a single vibrant violet. Text hierarchy is established through size and subtle gray tones rather than bold weights. Components are lightweight with generous padding and distinct corner radii, often featuring frosted glass-like backdrops, creating a sense of digital clarity and depth.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#141414` | neutral | Primary text, heading text, strong borders |
| Canvas White | `#ffffff` | neutral | Page backgrounds, primary surfaces |
| Subtle Gray | `#a1a1a1` | neutral | Secondary text, muted headings, subtle borders |
| Platinum Mist | `#d0d0d0` | neutral | Medium-contrast borders, control outlines, and structural separators. Do not promote it to the primary CTA color |
| Ash Cloud | `#8a8a8a` | neutral | Muted navigation text, tertiary information |
| Future Violet | `#acafff` | brand | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |

## Typography

### Primary Font

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 20px, 30px, 50px |
| lineHeight | 1, 1.2 |
| letterSpacing | -0.01 |
| role | Primary Font — detected in extracted data but not described by AI |

### Inter

| Key | Value |
| --- | --- |
| letterSpacing | -0.0100em |
| substitute | Inter |
| role | Headings and prominent body text. The tight letter spacing at larger sizes gives a refined, composed feel. |

### Inter

| Key | Value |
| --- | --- |
| letterSpacing | 0.0290em |
| substitute | Inter |
| role | Navigation, body copy, and peripheral information. Slightly expanded letter spacing aids readability at smaller scales. |

### Secondary Font

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px |
| lineHeight | 1, 2.29 |
| letterSpacing | 0.029 |
| role | Secondary Font — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 14 |  | 1 | 0.464 |
| body | 16 |  | 2.29 | 0.464 |
| subheading | 20 |  | 1 | -0.2 |
| heading | 30 |  | 1.2 | -0.3 |
| display | 50 |  | 1.2 | -0.5 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 44px |
| forms | 8px |
| links | 24px |
| buttons | 8px |
| general | 16px |

- **elementGap** — 24px
- **sectionGap** — 100px
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background |
| Card Backwash | `#f3f4f5` | 1 | Translucent background for cards, creating a frosted effect with `rgba(243, 244, 245, 0.4)` |

## Components

### Primary Navigation Button

**Role:** Call to action button in the navigation bar.

Background in Future Violet (#acafff), text in Canvas White (#ffffff), with a corner radius of 8px. It's designed for strong visual impact.

### Navigation Link

**Role:** Standard navigation items.

Text in Midnight Ink (#141414) for current/active states, Ash Cloud (#8a8a8a) for inactive states. Uses Primary Font (Inter, 400, 16px).

### Ghost Button

**Role:** Secondary call to action, offering a less dominant interaction.

Border in Platinum Mist (#d0d0d0) with a 1px solid stroke, no background fill, text in Midnight Ink (#141414). Radius is 24px, indicating a softer, less urgent action.

### Feature Card with Backwash

**Role:** Container for showcasing key features or content blocks.

Background set to rgba(243, 244, 245, 0.4), appearing as a translucent white. Features a generous 44px border-radius, creating a soft, distinct shape. Inner content is positioned with 4px left padding, 32px right padding, and no top/bottom padding relative to the card's boundaries.

### Video Player Icon

**Role:** Interactive element to trigger video content.

A circular icon with a blur(35px) backdrop filter and a light blue to green gradient fill. Text is in Canvas White (#ffffff) set at 14px, indicating a caption or descriptor.

## Layout

The page adheres to a centered maximum-width layout for main content, though the hero section features a full-bleed background gradient graphic. The hero presents a centered headline stack, often with a large, volumetric gradient at the bottom. Sections maintain a consistent vertical rhythm, primarily using a `sectionGap` of 100px. Content within sections frequently features a two-column alternating text-right/image-left pattern or stacked, centered blocks. The navigation is a sticky top bar with links aligned left and a primary action button to the right.

## Imagery

The site predominantly uses abstract, volumetric digital renders, specifically large, blurred gradients that act as ethereal background elements or visual anchors. When photography is present, it's typically high-contrast silhouette imagery, focusing on human interaction with technology, often featuring cool color overlays. Icons are simple, outlined, and monochromatic, used sparingly for functional cues. Imagery serves an atmospheric and mood-setting role, providing a soft, artistic counterpoint to the sharp typography and clean UI.

## Dos & Donts

### Do

- Use Midnight Ink (#141414) for primary text and headings, employing Subtle Gray (#a1a1a1) for secondary text and muted descriptions.
- Maintain a spacious layout with a section gap of 100px, ensuring generous vertical breathing room between content blocks.
- Apply a 44px border-radius to card-like elements, establishing a distinct soft-edged visual identity.
- Utilize Future Violet (#acafff) strictly for primary interactive backgrounds (e.g., 'Sign In' button) to draw immediate attention.
- Implement backdrop-filter: blur() effects for overlays or selected background cards to evoke a frosted glass aesthetic.
- Ensure all headings use the Primary Font (Inter, 400) with a tight letter-spacing of -0.0100em to achieve a composed, authoritative appearance.

### Don't

- Do not introduce additional saturated colors beyond Future Violet (#acafff) for UI elements; maintain the monochromatic base.
- Avoid heavy shadows or strong elevation; the design favors flat surfaces and subtle, atmospheric blurs for depth.
- Do not use highly contrasting or bold font weights; rely on size and gray scale for typographic hierarchy.
- Do not clutter content within cards; respect the 32px padding and 44px radius, allowing elements within to breathe.
- Refrain from using square or sharp corners for interactive elements or cards; the system prefers soft, rounded edges.
- Do not use dark backgrounds across full sections; the theme is predominantly light, with white canvas and subtle transparent overlays.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #141414
background: #ffffff
border: #141414
accent: #acafff
primary action: no distinct CTA color

### 3-5 Example Component Prompts
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Create a hero section headline: 'Global royalties made simple' using `font-family: Inter, 400, 50px`, `color: #141414`, `line-height: 1.2`, `letter-spacing: -0.5px`.
3. Create a `Ghost Button`: 'Explore the platform' using `border: 1px solid #d0d0d0`, `color: #141414`, `border-radius: 24px`, `padding: 12px 16px`, `font-family: Inter, 400, 16px`, `letter-spacing: 0.464px`.
4. Design a `Feature Card with Backwash`: `background: rgba(243, 244, 245, 0.4)`, `backdrop-filter: blur(35px)`, `border-radius: 44px`, `padding: 0px 32px 0px 4px`.

---
_Source: https://styles.refero.design/style/d5ed8712-0e42-4c6c-83b1-b3d7f27d1d10_
