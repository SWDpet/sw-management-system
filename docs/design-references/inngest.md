# Inngest — Design Reference

> Midnight Grid Console — where precision meets a soft amber glow.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://inngest.com](https://inngest.com) |
| Refero page | [https://styles.refero.design/style/62e8e59e-17a5-4eba-a6c6-1c7f67ded518](https://styles.refero.design/style/62e8e59e-17a5-4eba-a6c6-1c7f67ded518) |
| Theme | dark |
| Industry | devtools |

## Overview

This design system evokes a sense of ordered complexity within a high-contrast dark environment, much like an advanced control panel. The muted black and deep charcoal surfaces provide a sophisticated backdrop, against which subtle geometric patterns and a restrained, precise typography establish a serious, tech-forward identity. Strategic use of a single amber accent color guides the eye to interactive elements, while the generous application of rounded corners on buttons softens the otherwise sharp, grid-like aesthetic, making the technical feel approachable.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Background Charcoal | `#0c0a09` | neutral | Page backgrounds, card surfaces, primary dark UI elements. |
| Surface Dark Gray | `#1c1917` | neutral | Elevated card surfaces, input fields, and slightly lighter dark UI elements. |
| Text White | `#ffffff` | neutral | Primary text color for headlines and important body copy on dark backgrounds. |
| Text Light Gray | `#f6f6f6` | neutral | Secondary text and content on dark backgrounds, providing slightly less contrast. |
| Text Medium Gray | `#a89984` | neutral | Tertiary text, subtle descriptions, and less prominent information. |
| Border Light Gray | `#e5e7eb` | neutral | Prominent borders, separators, and outlines on light backgrounds (if present) or as high-contrast separators on dark. |
| Border Accent Gray | `#44403c` | neutral | Subtle borders and dividers within dark components, providing definition without distraction. |
| Amber Glow | `#cab16a` | brand | Primary accent for interactive elements, CTA buttons, active navigation, and key highlights – a warm counterpoint to the dark neutrals. |
| Highlight Green | `#59a569` | accent | Illustrative elements and secondary highlights. |
| Muted Red | `#ea6962` | semantic | Error states or distinctive highlights in content. |
| Vivid Orange | `#cc5b33` | accent | Secondary brand accent; used sparingly for illustrative details. |

## Typography

### CircularXX

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600 |
| weights | 300, 400, 500, 600 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px, 72px |
| lineHeight | 1.00, 1.20, 1.33, 1.40, 1.43, 1.50, 1.56, 1.63, 1.64, 1.71, 1.75 |
| letterSpacing | -0.019em |
| substitute | Inter |
| role | Primary content font for body text, navigation, and most interactive elements. Its modern, clean aesthetic provides clarity on dark backgrounds. |

### CircularXXMono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px, 14px, 18px |
| lineHeight | 1.33, 1.43, 1.56, 1.63 |
| substitute | Space Mono |
| role | Monospace text for code snippets, technical labels, and distinct inline elements, indicating programmatic content. |

### Whyte

| Key | Value |
| --- | --- |
| weight | 300, 400, 600, 700 |
| weights | 300, 400, 600, 700 |
| sizes | 16px, 24px, 30px, 36px, 48px, 72px |
| lineHeight | 1.00, 1.11, 1.20, 1.30, 1.33, 1.50 |
| letterSpacing | -0.056em |
| substitute | Montserrat |
| role | Display headlines for large, impactful headings. Its distinct letterforms and tighter letter-spacing at larger sizes give headlines a condensed, authoritative presence. |

### Whyte Inktrap

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 18px, 30px, 36px, 48px, 60px |
| lineHeight | 1.00, 1.11, 1.20, 1.30, 1.56 |
| letterSpacing | -0.056em |
| substitute | Cabinet Grotesk |
| role | Specialized body & headlines for emphasized text and headlines, providing a sharper, more technical edge in select areas. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.71 |  |
| body-sm | 14 |  | 1.63 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.56 |  |
| heading-sm | 24 |  | 1.33 |  |
| heading | 36 |  | 1.2 | -0.56 |
| heading-lg | 48 |  | 1.11 | -0.96 |
| display | 72 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 0px |
| buttons | 9999px |
| default | 4px |

- **elementGap** — 4-24px
- **sectionGap** — 24-40px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Feature Cards — Infraless / Agnostic / Observable

### Enterprise Trust — Built for Trust Feature Block

### Navigation Link

**Role:** Primary navigation item

Transparent background, #FAFAFA text, no border, 0px border-radius, 0px padding. Uses CircularXX for text. Hover state likely changes color to Amber Glow.

### Primary Call to Action Button

**Role:** Key interactive element

Background: #0C0A09, Text: #FAFAFA, 0px border-radius (effectively square), 8px vertical padding, 16px horizontal padding. Uses CircularXX, weight 400. Text is visually distinct from background, commanding attention.

### Secondary Button

**Role:** Secondary action or link button

Transparent background, Text: #FAFAFA, no border-radius by default but specific instances have 6px border-radius, 16px top padding, 0px other padding. Text is CircularXX, often with an arrow icon.

### Tertiary Button

**Role:** Subtle button for minor actions

Background: #1C1917, Text: #FAFAFA, 0px border-radius, 8px vertical padding, 16px horizontal padding. Offers a slightly elevated appearance compared to the primary button, useful for grouped actions where one needs to stand out slightly more.

### Dark Overlay Card

**Role:** Information container, features

Background: rgba(28, 25, 23, 1), 0px border-radius. No explicit padding detected on the card itself, content dictates inner spacing. Shadow: rgba(0, 0, 0, 0.1) 0px 1px 3px 0px, rgba(0, 0, 0, 0.1) 0px 1px 2px -1px - a very subtle dark shadow giving a slight lift.

### Highlight Card

**Role:** Featured information container

Background: rgb(12, 10, 9), 0px border-radius. Similar subtle shadow to Dark Overlay Card. Used for prominent features or sections that need to feel slightly more embedded into the background without disappearing.

### Pill Button

**Role:** Status indicator or filter button

9999px border-radius, providing a rounded, pill-like shape. Backgrounds and text colors vary based on context, but the shape indicates a specific type of interactive or informational element. e.g., 'Open Source' button.

## Layout

The site employs a primarily max-width contained layout, centering content blocks on a dark canvas, though the hero section often uses a full-bleed dark background. The hero features a centered headline over an abstract geometric background. Section rhythm is generally consistent with vertical spacing, often introducing alternating content arrangements like text-left/image-right or centered stacks. Card grids are used to display features or trusted brands, typically in 3-column layouts. The navigation is a sticky top bar, providing persistent access across the dark interface. The overall density is comfortable, with ample breathing room between sections, creating a sense of clarity and focus.

## Imagery

The visual language predominantly features abstract, geometric shapes and patterns, emphasizing structure and technicality. These graphics are often composed of solid, brand-colored blocks (green, amber, orange) or subtle gradients, placed in a grid-like arrangement that occasionally breaks bounds for visual interest. There's a notable absence of photography or detailed illustrations. Icons are simple, outlined, and monochromatic, used functionally to represent concepts or features. The imagery's role is primarily decorative and atmospheric, reinforcing the 'control panel' metaphor rather than conveying specific product details, contributing to a high-tech, somewhat abstracted feel. Density is moderate, with images typically contained within sections rather than full-bleed.

## Dos & Donts

### Do

- Prioritize `Background Charcoal` (#0C0A09) for primary dark surfaces and `Surface Dark Gray` (#1C1917) for elevated content containers.
- Use `Amber Glow` (#CAB16A) exclusively for primary calls to action, active states, and critical highlights to maintain its impact.
- Employ `Whyte` or `Whyte Inktrap` for all display and large headlines, utilizing their tight letter spacing (`-0.0560em`, `-0.0250em`) for a condensed, impactful look.
- Apply `9999px` border-radius for all interactive buttons and pill-shaped elements to create a consistent soft touch amidst sharper elements.
- Maintain generous vertical spacing between content sections, using `24px` to `40px` padding to provide breathing room on the dark canvas.
- Layer cards atop `Background Charcoal` using `Surface Dark Gray` and the subtle shadow `rgba(0, 0, 0, 0.1) 0px 1px 3px 0px, rgba(0, 0, 0, 0.1) 0px 1px 2px -1px`.

### Don't

- Do not introduce new primary accent colors; `Amber Glow` (#CAB16A) is the singular visual indicator for key interactions.
- Avoid standard rectangular shapes for buttons; either use `0px` radius for a sharp, embedded look or `9999px` for pill forms.
- Do not apply strong, opaque shadows; leverage subtle, low-opacity dark shadows (`rgba(0, 0, 0, 0.1)`) to create depth without visual weight.
- Refrain from using heavily saturated images or illustrations; opt for abstract, geometric shapes or desaturated imagery that complements the dark theme.
- Do not use generic system fonts; always map to `CircularXX` for body text and `Whyte` for headlines to preserve brand distinctiveness.
- Avoid highly expressive or decorative typography for body copy, stick to the clear and professional feel of `CircularXX`.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text Primary: #FFFFFF
- Background Primary: #0C0A09
- CTA Accent: #CAB16A
- Card Surface: #1C1917
- Border High-Contrast: #E5E7EB

### 3-5 Example Component Prompts
1. **Create a hero section:** Full-width `Background Charcoal` (#0C0A09) with a centered headline 'Make any code durable by default' using Whyte family, 72px, weight 700, #FFFFFF, letter-spacing -0.056em. Below it, a body paragraph 'Workflows, agents, endpoints...' using CircularXX, 18px, weight 400, #F6F6F6. Include a primary button 'Start building for free' (Background: #CAB16A, Text: #0C0A09, 8px vertical padding, 16px horizontal padding, 9999px border-radius, CircularXX, weight 500) and a secondary button 'Get a demo →' (Transparent background, Text: #CAB16A, 0px border-radius, 8px vertical padding, 16px horizontal padding, CircularXX, weight 400).
2. **Generate a feature card:** Use `Surface Dark Gray` (#1C1917) as background with 0px border-radius. Apply a shadow `rgba(0, 0, 0, 0.1) 0px 1px 3px 0px, rgba(0, 0, 0, 0.1) 0px 1px 2px -1px`. Inside, a heading (Whyte Inktrap, 24px, weight 600, #FFFFFF, letter-spacing -0.025em), and body text (CircularXX, 16px, weight 400, #F6F6F6).
3. **Design a navigation item:** Text 'PLATFORM' using CircularXX, 16px, weight 500, #FAFAFA. When active, underline with 2px solid line in `Amber Glow` (#CAB16A) and change text color to `Amber Glow`.
4. **Compose a 'Trusted by' logo section:** On a `Background Charcoal` (#0C0A09) section, display a row of monochrome logo icons (all #FFFFFF stroke/fill for visibility) with 24px horizontal `elementGap` between them and 32px vertical `elementGap` from surrounding content.

---
_Source: https://styles.refero.design/style/62e8e59e-17a5-4eba-a6c6-1c7f67ded518_
