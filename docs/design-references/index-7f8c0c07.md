# Index — Design Reference

> Midnight Grid Command

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://index.app](https://index.app) |
| Refero page | [https://styles.refero.design/style/7f8c0c07-86e9-4b7c-a042-a7563b169143](https://styles.refero.design/style/7f8c0c07-86e9-4b7c-a042-a7563b169143) |
| Theme | dark |
| Industry | ai |

## Overview

Index employs a 'dark void' aesthetic, presenting information on deep, near-black surfaces. Thin, outlined elements and subtle grid lines define structure, while ample letter spacing and precise typography create a sense of calm authority. Interactivity is signaled by high-contrast text and a single muted violet accent, which appears sparingly to prevent visual overhead. The system prioritizes clarity and focus, allowing content to emerge from a dark, minimal backdrop.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Void Black | `#000000` | neutral | Page backgrounds, deepest surface layers, primary text on light backgrounds, hairline borders, ghost button strokes |
| Deep Graphite | `#1c1c1c` | neutral | Secondary surface backgrounds, elevated card backgrounds, subtle icon outlines |
| Alabaster | `#ffffff` | neutral | Primary text on dark backgrounds, interactive link text, button backgrounds for ghost buttons, emphasized headings |
| Medium Gray | `#808080` | neutral | Secondary text, muted borders, placeholder text, subtle UI elements |
| Light Gray | `#ababab` | neutral | Muted helper text, tertiary surface contrast, very subtle borders |
| Dark Gray | `#4d4d4d` | neutral | Finer details, very subtle borders or small text where slightly more presence than Light Gray is needed |
| Soft Violet | `#7089ba` | accent | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |

## Typography

### Raveo Variable

| Key | Value |
| --- | --- |
| weight | 400, 500, 1000 |
| sizes | 12px, 14px, 16px, 24px, 32px, 70px |
| lineHeight | 1.10, 1.20, 1.40, 1.50, 1.60, 1.70, 1.80 |
| letterSpacing | -0.04em at 70px, -0.01em at 32px |
| fontFeatureSettings | 'tnum' |
| substitute | Inter Variable |
| role | Primary typeface for all headings and most body text. The variable weight contributes to precise visual hierarchy where size and weight are carefully balanced. 'tnum' feature ensures consistent number alignment. |

### Geist Mono

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 9px, 12px |
| lineHeight | 1.60 |
| letterSpacing | 0.02em |
| substitute | JetBrains Mono |
| role | Monospaced typeface for small annotations, code snippets, and specific UI labels where fixed-width alignment is desired. The slightly positive letter spacing creates a readable, airy feel. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | Helvetica Neue |
| role | Fallback sans-serif for minimal UI elements and basic text where specific branding is less critical. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.2 | -0.14 |
| heading-sm | 24 |  | 1.4 |  |
| heading | 32 |  | 1.1 | -0.32 |
| display | 70 |  | 1.1 | -2.8 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| max | 188px |
| fluid | 100px |
| small | 6px |
| larger | 20px |
| buttons | 50px |

- **elementGap** — 10px
- **sectionGap** — 50px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Navigation Link

**Role:** Interactive text link in header, footer, and inline content.

Text uses Raveo Variable, weight 400 or 500, size 12px, line height 1.2. Color is Alabaster (#ffffff) on Void Black, or Light Gray (#ababab) for less prominent links. Padding: 5px horizontal, 8px vertical.

### Ghost Button

**Role:** Primary and secondary actions with minimal visual weight.

Text is Alabaster (#ffffff) with Void Black (#000000) border, 1px thick. Background is transparent or a dark neutral. Rounded with 50px radius. Text is Raveo Variable, weight 500, 14px size, line height 1.2, letter spacing -0.01em. Example: 'Book demo' button.

### Early Preview Badge

**Role:** Small informational tag for new features.

Text uses Geist Mono, size 9px, weight 500, letter spacing 0.02em. Background is Void Black (#000000), text is Alabaster (#ffffff). Border is thin, 1px Void Black. Radius 6px or 20px, with padding of 9px horizontally and 5px vertically. Features a small bullet point graphic.

### Feature Card

**Role:** Container for showcasing product benefits or steps.

Background is Deep Graphite (#1c1c1c) or Void Black (#000000), using Alabaster (#ffffff) for prominent headings and Alabaster or Medium Gray (#808080) for body text. Inner padding is 16px to 24px. Features subtle border lines of Void Black or Medium Gray for separation between elements.

### Interactive Icon Circle

**Role:** Used for 'get started' steps, showcasing a visual cue.

Circular background using Deep Graphite (#1c1c1c) with a thin Void Black (#000000) border. Contains a white (#ffffff) line-art icon. Size 48px square, border radius 100px.

## Layout

The page primarily uses a full-bleed dark background, setting a continuous 'void' theme. Content sections are centered with an implicit maximum width, likely around 1000-1200px, maintaining ample negative space on wider screens. The hero section features a large, centered headline over a dark, subtly textured background. Sections maintain consistent vertical spacing, often indicated by faint background grids or thin dividing lines rather than distinct surface layers. Content is arranged in alternating patterns, such as text-left/visual-right, or in 3-column card grids for features. Navigation is a sticky top bar, minimal and dark, with ghost buttons and text links.

## Imagery

The imagery aesthetic is minimalist and technical, focusing on abstract line-art illustrations and subtle background patterns. These visuals are typically monochromatic or near-monochromatic, using fine lines and geometric forms to suggest data, networks, and technical processes. Illustrations serve a decorative and explanatory role, subtly enhancing content without distracting. Icons are also line-art, matching the overall sparse aesthetic, contributing to the feeling of a sophisticated, data-driven system. Imagery density is low; the visual system is text-dominant.

## Dos & Donts

### Do

- Prioritize Void Black (#000000) and Deep Graphite (#1c1c1c) for backgrounds and surfaces to maintain the dark theme rigidity.
- Use Alabaster (#ffffff) for primary text and headings against dark backgrounds, ensuring maximum contrast.
- Apply Raveo Variable with subtle negative letter spacing (-0.04em for large heads, -0.01em for smaller heads) for headlines to create a sophisticated, restrained appearance.
- Employ Geist Mono for all small, functional labels and annotations, especially where a fixed-width, precise feel is needed.
- Utilize Soft Violet (#7089ba) only as a minimal accent — for small indicators, hover states, or subtle highlights, never for large background areas or primary buttons.
- Maintain a compact spacing density, with 10px as a common element gap and minimal padding around interactive elements.
- Use rounded corners consistently: 50px for primary ghost buttons and smaller circles, 6px for subtle interface elements like badges.

### Don't

- Avoid using saturated colors other than Soft Violet (#7089ba); the design relies on a near-monochromatic palette.
- Do not introduce heavy shadows or overt elevation effects; the design emphasizes flat surfaces and subtle outlines.
- Refrain from large blocks of text; break content into manageable, well-spaced lines with clear hierarchy.
- Do not use generic system fonts for prominent text; Raveo Variable and Geist Mono are essential to the brand's typographic identity.
- Avoid wide page layouts; content should be centered and organized within an implied maximum width, even without a strict pageMaxWidth.
- Do not randomly vary border radii; stick to the specified 6px, 20px, 50px, 100px, and 188px for deliberate shape language.
- Do not use solid background buttons with chromatic colors; primary interactive elements should remain ghosted or neutral.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000000
border: #000000
accent: #7089ba
primary action: no distinct CTA color

Example Component Prompts:
1. Create a hero section: Void Black background with subtle texture. Centered headline 'Analytics done for you.' in Raveo Variable, size 70px, weight 1000, white (#ffffff), letter-spacing -2.8px. Below it, a subtext 'Surface the exact levers...' in Raveo Variable, size 16px, weight 400, Light Gray (#ababab), centered. Below that, a ghost button 'Book demo' with Alabaster (#ffffff) text, Void Black (#000000) border, 50px radius, 14px text size, 12px vertical and 24px horizontal padding.
2. Design a feature card: Deep Graphite (#1c1c1c) background, 188px radius. Headline 'Onboard call' in Raveo Variable, size 24px, weight 500, Alabaster (#ffffff), centered. Body text 'Share current setup & metrics.' in Raveo Variable, size 14px, weight 400, Medium Gray (#808080). Include a thin Alabaster (#ffffff) circular icon with a Deep Graphite (#1c1c1c) background, 100px radius, 48x48px size, and a 1px Void Black (#000000) border containing a white line-art icon.
3. Implement a navigation link: 'Features' in Raveo Variable, size 12px, weight 400, Alabaster (#ffffff). Padding of 5px horizontal and 8px vertical, no distinct background, on a Void Black (#000000) navigation bar. On hover, apply a subtle text color change to Soft Violet (#7089ba).

---
_Source: https://styles.refero.design/style/7f8c0c07-86e9-4b7c-a042-a7563b169143_
