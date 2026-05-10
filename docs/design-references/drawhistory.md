# DrawHistory — Design Reference

> Warm parchment manifesto

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://drawhistory.com](https://drawhistory.com) |
| Refero page | [https://styles.refero.design/style/25246b10-83a4-4f51-a411-cf85503b94a8](https://styles.refero.design/style/25246b10-83a4-4f51-a411-cf85503b94a8) |
| Theme | light |
| Industry | agency |

## Overview

DrawHistory utilizes a subdued, grounded aesthetic with a primary focus on dark text on light, almost parchment-like backgrounds. The visual system balances strong typographic hierarchy with soft, substantial UI elements. A distinctive palette of desaturated 'earthy' neutrals and a single vivid orange accent punctuates functional elements without overwhelming the calm, editorial feel. Components are notably chunky, with generous padding and rounded corners, creating a sense of weight and importance.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Parchment | `#e5e7eb` | neutral | Primary page backgrounds, subtle borders, and muted divider lines |
| Near Black | `#000000` | neutral | Primary headings, body text, and icon fills on light surfaces. Do not promote it to the primary CTA color |
| Onyx Card | `#1d1d1b` | neutral | Background for elevated card surfaces, providing a deep contrast to text |
| Sage Dark | `#3d3b2f` | neutral | Background for prominent card surfaces, icon strokes, and button backgrounds |
| Paper White | `#ffffff` | neutral | Inverse text for dark backgrounds, button text, and icon fills on dark surfaces |
| Wet Sand | `#e1d3c7` | neutral | Subtle background for specific sections or elements, adding warmth to the neutral palette |
| Steel Gray | `#999999` | neutral | Decorative borders or secondary, less prominent UI outlines |
| Fog Input | `#b3b3b3` | neutral | Default input borders and subtle background shades |
| Pale Clay | `#faf6f3` | neutral | Very light borders and occasional body text, complementing the parchment canvas |
| Flame Orange | `#ff6714` | accent | Small, vivid accent for attention-grabbing elements or indicators, contrasting sharply with the subdued neutrals |

## Typography

### Eloquia Text

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 14px, 16px, 18px, 20px, 22px, 35px, 36px, 40px, 44px, 55px, 100px, 104px |
| lineHeight | 0.95, 1.00, 1.09, 1.10, 1.11, 1.13, 1.14, 1.18, 1.40, 1.43, 1.50, 1.56 |
| letterSpacing | -0.0500em at 104px, -0.0300em at 55px, -0.0250em at 44px, -0.0180em at 40px, -0.0090em at 36px, normal for smaller sizes |
| substitute | Source Serif Pro |
| role | Primary brand typeface for all headings, body text, and interactive elements. Its versatility across weights and generous line heights supports a comfortable reading experience for content and strong impact for headlines. |

### Roboto Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px, 15px, 16px |
| lineHeight | 1.25, 1.33, 1.43, 1.50 |
| letterSpacing | normal |
| substitute | Space Mono |
| role | Used for specific contextual elements like timestamps or code snippets, providing a technical, distinct contrast to the primary typeface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.5 | 0 |
| body-sm | 16 |  | 1.43 | 0 |
| body | 18 |  | 1.43 | 0 |
| subheading | 20 |  | 1.4 | 0 |
| heading-sm | 22 |  | 1.14 | 0 |
| heading | 35 |  | 1.13 | -0.315 |
| heading-lg | 40 |  | 1.1 | -0.72 |
| display | 104 |  | 0.95 | -5.2 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| images | 16px |
| buttons | 8px |

- **elementGap** — 8px
- **sectionGap** — 80px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Parchment | `#e5e7eb` | 0 | Base page background, light and airy foundation. |
| Wet Sand | `#e1d3c7` | 1 | Subtle background for specific content sections, providing a slight warmth and visual rhythm. |
| Sage Dark | `#3d3b2f` | 2 | Elevated card surfaces, creating a heavy, grounded visual block for key information. |
| Onyx Card | `#1d1d1b` | 3 | Deepest card background, used for maximum contrast against white text in specific contexts. |

## Components

### Ghost Navigation Link

**Role:** Primary navigation links and subtle inline calls to action.

Transparent background, Near Black text, no border. Appears as pure text link within layouts.

### Standard Button

**Role:** General action buttons, such as 'Read More' or 'Learn More'.

Background #000000, white text, 8px radius, 8px vertical padding, 12px horizontal padding. Delivers a clear, understated call to action.

### Dark Card Button

**Role:** Buttons within Sage Dark colored cards, offering a distinct visual cue.

Background Sage Dark (#3d3b2f), Near Black text, 16px border-radius, 40px padding on all sides. Used for important interactions on darker surfaces.

### Onyx Card Button

**Role:** Buttons within Onyx Card colored cards, ensuring visibility.

Background Onyx Card (#1d1d1b), Near Black text, 16px border-radius, 40px padding on all sides. Similar to the Dark Card Button but for the deepest card background.

### Card Surface - Default

**Role:** Generic display panels for content, often used without internal padding for media.

Transparent background, 0px border-radius, no shadow. Designed for flexible content presentation where visual separation is minimal.

### Card Surface - Elevated

**Role:** Prominent content blocks that require optical separation and visual weight.

Background Sage Dark (#3d3b2f) or Onyx Card (#1d1d1b), 16px border-radius, 40px internal padding. Creates a substantial, deliberate content container.

### Text Input

**Role:** Standard user input fields.

Paper White background, Near Black text, Fog Input (#b3b3b3) border with 0px radius, 8px vertical padding. Provides a clean and functional input area.

## Layout

The page primarily employs a max-width contained layout, with significant horizontal padding creating a framed effect. Sections feature a strong vertical rhythm, often separating content blocks with generous white space or subtle background color changes (e.g., from Canvas Parchment to Wet Sand). Content arrangement often alternates between full-width content stacks, and two-column layouts where text and imagery are paired. There's a particular emphasis on chunky content cards, frequently centered, serving as key interaction points. The navigation is a top bar, with ghost-style links.

## Imagery

Imagery largely consists of tight product crops, specific brand logos, and occasional professional, staged photography of individuals, treated with a natural color palette. Images are typically contained within card-like structures with 16px rounded corners, often serving as a focal point within content blocks. The imagery is explanatory and supportive, enhancing content without being purely decorative.Icons are simple, often outlined, with a medium stroke weight, in either Near Black or Paper White.

## Dos & Donts

### Do

- Prioritize Eloquia Text for all text content, selecting weights 300 or 400 to achieve a calm, editorial feel.
- Use Canvas Parchment (#e5e7eb) as the dominant background color for most sections, establishing a foundational light tone.
- Apply 16px border-radius to card elements and images for a consistent soft, chunky aesthetic.
- Utilize 40px internal padding for cards or prominent blocks to ensure generous content separation and visual weight.
- Employ Near Black (#000000) for all primary text and headings against lighter backgrounds for maximum contrast.
- Introduce Flame Orange (#ff6714) sparingly, primarily for small, functional accents like status indicators or emphasis rather than large blocks of color.
- Maintain a clear visual hierarchy by varying type sizes from the Eloquia Text profile, using larger sizes with tighter line heights for headings and smaller sizes with greater line heights for body text.

### Don't

- Avoid excessive use of highly saturated colors; the palette focuses on desaturated neutrals with a single accent.
- Do not deviate from the established border radii of 16px for cards and 8px for buttons; this defines the component's substance.
- Refrain from thin or ghosted interface elements; components should feel substantial with generous padding and clear backgrounds.
- Do not introduce additional font families beyond Eloquia Text and Roboto Mono, or deviate from their specified weights and sizes.
- Avoid complex shadow effects; the system relies on backgroundColor changes and generous spacing for visual separation instead of elevation.
- Do not use small padding values; elements need breathing room, favoring 8px as a minimum for small gaps and 40px for component padding.
- Do not use full-width text blocks, ensure content is contained within a comfortable reading measure, typically allowing for generous horizontal spacing.

## Notes

### Agent Prompt Guide

Quick Color Reference: text: #000000, background: #e5e7eb, border: #e5e7eb, accent: #ff6714, primary action: no distinct CTA color

Example Component Prompts:
1. Create a large text block with a Sage Dark background: background is `Sage Dark (#3d3b2f)`, text is `Paper White (#ffffff)` using `Eloquia Text` weight `400` at `18px` with `1.43` lineHeight, and 40px internal padding.
2. Build a prominent headline section: `Canvas Parchment (#e5e7eb)` background. Headline: `Near Black (#000000)`, `Eloquia Text` weight `400` at `55px`, `lineHeight 1.11`, `letterSpacing -0.0300em`. Body text: `Near Black (#000000)`, `Eloquia Text` weight `400` at `18px` with `1.43` lineHeight.
3. Design a standard action button: Background `Near Black (#000000)`, text `Paper White (#ffffff)`, `Eloquia Text` weight `400` at `16px` with `1.43` lineHeight, `8px` border-radius, `8px` vertical padding, `12px` horizontal padding.

---
_Source: https://styles.refero.design/style/25246b10-83a4-4f51-a411-cf85503b94a8_
