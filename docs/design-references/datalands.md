# Datalands — Design Reference

> Monochromatic Canvas, Vivid Punctuation

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://datalands.co](https://datalands.co) |
| Refero page | [https://styles.refero.design/style/a7530405-e523-4268-bba5-ef13549fd61c](https://styles.refero.design/style/a7530405-e523-4268-bba5-ef13549fd61c) |
| Theme | dark |
| Industry | design |

## Overview

Datalands embraces a stark, high-contrast dark mode aesthetic, where deep black functions as a vast canvas for bold, expressive typography and selective pops of chromatic color. The design system emphasizes strong visual statements through custom, often unconventional typefaces and generously spaced layouts. Interaction elements such as buttons and inputs feature large radii, giving them a distinct soft, almost friendly, tactile quality against the severe background. Color is used sparingly but impactfully as an accent for calls to action or to highlight specific UI elements, breaking the monochrome with vibrant hues.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Page backgrounds, primary text, borders, dark overlay for content visibility |
| Ghost White | `#ffffff` | neutral | Primary text on dark backgrounds, icon fills, card backgrounds |
| Storm Gray | `#111212` | neutral | Input text and input borders on light surfaces |
| Mist | `#f3f3ef` | neutral | Input field backgrounds, light surface elements |
| Charcoal | `#3d3d3d` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Light Gray | `#d9d9d9` | neutral | Muted body text on dark backgrounds |
| Twilight Black | `#1d1a1a` | neutral | Subtle dark background for distinct interactive elements or cards |
| Deep Space Blue | `#122d8b` | brand | Decorative illustration accents, SVG fills |
| Cyan Sky | `#94bcee` | brand | Highlight backgrounds, brand accent for specific sections |
| Fuchsia Pulse | `#fc74dd` | accent | Primary call-to-action button background — a vibrant pop against the dark UI |
| Lava Orange | `#ff4c33` | accent | Orange wash for highlight backgrounds, decorative bands, and soft emphasis behind content |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### OZIK Black

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 358px |
| lineHeight | 0.83, 1.50 |
| letterSpacing | -0.0100em |
| substitute | Black Ops One |
| role | Display headlines and hero text. The singular heavy weight and massive size create an immediate, impactful visual statement, making text into an architectural element. |

### Basis Grotesque Pro

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 18px, 22px, 35px, 38px, 42px, 60px, 80px |
| lineHeight | 1.00, 1.10, 1.20, 1.25, 1.45 |
| letterSpacing | -0.0400em, -0.0360em, -0.0100em, -0.0080em, -0.0030em, -0.0020em, 0.0200em |
| role | Primary body text, subheadings, and larger content headings. Its wide range of sizes allows it to establish clear hierarchy from detailed text to prominent section titles, maintaining a consistent, refined feel. |

### Martian Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px, 16px |
| lineHeight | 1.25, 1.45 |
| letterSpacing | normal, 0.0500em, 0.1000em, -0.0500em, 0.0200em |
| role | Used for navigation, metadata, labels, and specific interactive elements. The monospace nature introduces a technical, precise undertone, offering a clear contrast to the more expressive primary headings. |

### Martian Mono Std xLt Extra-light

| Key | Value |
| --- | --- |
| weight | 200, 600 |
| sizes | 18px |
| lineHeight | 1.4 |
| role | Martian Mono Std xLt Extra-light — detected in extracted data but not described by AI |

### Martian Mono Std xLt

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.45 |
| letterSpacing | -0.05 |
| role | Martian Mono Std xLt — detected in extracted data but not described by AI |

### Martian Mono Nr xLt

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | Martian Mono Nr xLt — detected in extracted data but not described by AI |

### Martian Mono Extra-light

| Key | Value |
| --- | --- |
| weight | 200 |
| sizes | 12px |
| lineHeight | 1 |
| role | Martian Mono Extra-light — detected in extracted data but not described by AI |

### Martian Mono ExtraLight

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 15px |
| lineHeight | 1.25 |
| letterSpacing | 0.02 |
| role | Martian Mono ExtraLight — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1 | 0.05 |
| body | 14 |  | 1.45 | -0.003 |
| body-lg | 18 |  | 1.4 | -0.008 |
| heading-sm | 22 |  | 1.25 | -0.01 |
| heading | 35 |  | 1.25 | -0.036 |
| heading-lg | 60 |  | 1.2 | -0.04 |
| display | 358 |  | 0.83 | -0.01 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 30px |
| links | 30px |
| inputs | 96px |
| buttons | 96px |
| decorativeElements | 300px |

- **elementGap** — 10px
- **sectionGap** — 80px
- **cardPadding** — 48px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Base Canvas | `#000000` | 0 | Dominant background for the entire application, creating a deep, immersive dark mode. |
| Input Surface | `#f3f3ef` | 1 | Background for interactive input fields, providing clear contrast against the dark canvas. |
| Actionable Surface | `#1d1a1a` | 2 | Background for certain interactive elements or cards, offering a slight visual elevation from the canvas. |

## Components

### Ghost Navigation Button

**Role:** Navigation and secondary actions.

Transparent background with #000000 text. Padding: 10px vertical, 4px horizontal. No border, no border-radius. Minimalist, designed to blend with the background until hovered.

### Primary Action Button

**Role:** Key interaction calls to action.

Solid #fc74dd background with #3d3d3d text. Padding: 48px vertical, 36px horizontal. Large border-radius of 96px creates a pill-shaped button. High contrast and soft edges for prominent actions.

### Dark Overlay Button

**Role:** Interactive elements within dark sections, often containing media.

Semi-transparent dark background rgba(56, 50, 50, 0.51) with #000000 text. No padding, no border. Small 8px border-radius. Subtly interactive.

### Circular Link Button

**Role:** Decorative and functional links, often with icons.

Transparent background, #0000ee text (browser default). Large border-radius of 300px, making it circular. Generous 48px padding creates a substantial clickable area. Used for eye-catching navigation.

### Pill Input Field

**Role:** Data entry fields requiring user input.

#f3f3ef background, #111212 text and border. Padding: 48px vertical, 36px horizontal. Large border-radius of 96px for a soft, pill-shaped input. The light background stands out against the dark canvas.

## Layout

The page maintains a full-bleed dark canvas, with content primarily flowing vertically. The hero section features monumental, centered typography over a stark black background. Subsequent sections follow a rhythm of consistent vertical spacing, creating an uncrowded feel. Content arrangement often appears as centered stacks of text, or less frequently, as split sections with text and abstract visuals. There's a subtle grid usage for showcasing case studies or features, often within a card-like structure. Navigation is a minimalist top bar, displaying concise information and subtle interactive elements, ensuring the main content remains the focus.

## Imagery

The visual language for imagery is minimal and functional. Photography is not present. Illustrations appear as abstract, geometric shapes (circles, rectangles) with bold, vivid colors (like neon green or orange) on dark backgrounds, often used as data visualizations. Icons are simple, outlined, and monochromatic (white on black or black on colored backgrounds) with a medium stroke weight. Imagery serves primarily as explanatory content or decorative atmosphere, providing visual interest without distracting from the highly typographic focus. Image density is low, making way for ample negative space and large typography.

## Dos & Donts

### Do

- Prioritize #000000 as the dominant background for most sections to maintain the system's dark theme.
- Use OZIK Black at its largest sizes for hero text and major headings, establishing text as a monumental visual element.
- Apply 96px border-radius to all primary interactive elements like buttons and input fields for a soft, pill-like aesthetic.
- Emphasize actions with the #fc74dd Fuchsia Pulse as a vibrant background color for primary CTA buttons.
- Utilize Martian Mono for all navigation items, labels, and meta-information to introduce a technical, structured feel.
- Ensure generous vertical spacing between sections, using at least '80px' sectionGap for a comfortable, uncrowded layout.
- Use #ffffff for primary titles and body text against dark backgrounds, ensuring maximum contrast and readability.

### Don't

- Avoid using light backgrounds extensively; the system relies on a predominantly dark canvas.
- Do not use subtle variations of gray for backgrounds; maintain the stark contrast between #000000 and the few light elements.
- Do not introduce small, sharp-edged buttons or input fields; maintain the generous 96px border-radius for softness.
- Avoid generic sans-serif fonts for display or body text; the custom typefaces are integral to the brand's unique voice.
- Do not use muted or desaturated colors for primary calls to action; vibrancy is key for accent colors.
- Do not clutter layouts; maintain ample elementGap and cardPadding for clear, purposeful visual hierarchy.
- Avoid using default browser link colors unless specifically for unstyled, secondary links within body text.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000000
border: #111212 (for inputs), #000000 (for ghost buttons)
accent: #fc74dd
primary action: #fc74dd (filled action)

Example Component Prompts:
1. Create a hero section: #000000 background. Centered headline 'Datalands' at 358px OZIK Black weight 400, #ffffff, letter-spacing -0.01em. Top-right 'Let’s talk' button: #fc74dd background, #3d3d3d text, 96px border-radius, 48px vertical/36px horizontal padding.
2. Create an input field: #f3f3ef background, #111212 text and border, 96px border-radius, 48px vertical/36px horizontal padding. Placeholder text in Martian Mono weight 200, 18px.
3. Create a secondary navigation link: Transparent background, #000000 text, 10px vertical/4px horizontal padding, no border, no border-radius. Font is sans-serif 12px weight 400.

---
_Source: https://styles.refero.design/style/a7530405-e523-4268-bba5-ef13549fd61c_
