# Parallel Web Systems — Design Reference

> Architectural Blueprint; Grid-based precision with sparse, functional color accents.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://parallel.ai](https://parallel.ai) |
| Refero page | [https://styles.refero.design/style/32845f27-6b24-48be-af25-8e664f826b30](https://styles.refero.design/style/32845f27-6b24-48be-af25-8e664f826b30) |
| Theme | light |
| Industry | devtools |

## Overview

This design system presents as a highly structured, data-driven interface, echoing the precision required in AI systems. Its palette is almost entirely achromatic, with a deliberate and minimal use of vivid orange and moderate blue to highlight interactive elements and key information. The signature move is the pairing of a classic humanist sans-serif with a mono-spaced font, creating an aesthetic that feels both approachable and technically rigorous. Underlying the visual clarity are subtle shadows and crisp borders, conveying a sense of engineered robustness.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Cloud White | `#ffffff` | neutral | Page backgrounds, card surfaces, active states for subtle contrast. |
| Smoke Gray | `#f6f6f6` | neutral | Subtle background sections, hover states on neutral elements. |
| Whisper Gray | `#eeeeee` | neutral | Alternate background for subtle section breaks. |
| Light Concrete | `#e5e5e5` | neutral | Borders, dividers, and subtle outlines on UI elements. |
| Medium Ash | `#858483` | neutral | Secondary text, descriptive labels, and subtle icons – providing visual hierarchy without stark contrast. |
| Charcoal Black | `#181818` | neutral | Primary text, headlines, and key UI elements for clear readability. |
| Obsidian | `#000000` | neutral | Critical text, strong accents, and high-contrast iconography, especially for logos. |
| Ignite Orange | `#fb631b` | brand | Primary call-to-action buttons, active navigation indicators, specific highlight text – a vivid mark against the neutral palette to drive action. |
| Sky Blueprint | `#0d6ea5` | brand | Secondary interactive elements, links, and system indicators – a cooler complement to the primary orange, maintaining a professional tone. |
| Ocean Tint | `#6fa2e8` | brand | Used for background coloration in specific sections, creating a subtle visual break. |
| Sea Mist | `#8fb6cc` | brand | Subtle background shading or very light accents. |
| Success Green | `#69be78` | semantic | Semantic success indicators and notification backgrounds. |

## Typography

### gerstnerProgramm

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 11px, 13px, 14px, 16px, 26px, 36px |
| lineHeight | 1.11, 1.23, 1.50 |
| letterSpacing | 0.16, 0.14 |
| substitute | Inter |
| role | Primary text font for body copy, navigational elements, and most headers. Its slightly humanist curves temper the overall technical aesthetic, making information digestible. |

### ftSystemMono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 10px, 11px, 12px, 13px, 14px, 16px |
| lineHeight | 1.00, 1.23, 1.33, 1.50 |
| letterSpacing | 0.18, 0.24, 0.21 |
| substitute | Roboto Mono |
| role | Used for code snippets, data readouts, and UI elements like 'Human/Machine' toggles. The monospace nature reinforces the technical and data-driven identity of the platform. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1 |  |
| heading-lg | 26 |  | 1.11 | 0.26 |
| display | 36 |  | 1.11 | 0.36 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 4px |
| default | 2px |
| card_large | 8px |
| card_small | 4px |

- **elementGap** — 8px
- **sectionGap** — 80px
- **cardPadding** — 8px
- **pageMaxWidth** — 1600px

## Components

### Button Group

### Feature Cards Grid

### Announcement Banner + Status Bar

### Secondary Ghost Button

**Role:** Interactive element

Subtle interactive button, uses brand blue for text. `backgroundColor: rgba(0, 0, 0, 0)`, `color: #0d6ea5`, `borderRadius: 0px`, `padding: 8px 8px`, `borderTopColor: #e5e5e5`.

### Neutral Button

**Role:** Interactive element

General purpose button with a light background. `backgroundColor: #ffffff`, `color: #181818`, `borderRadius: 4px`, `padding: 8px 16px`, `borderTopColor: #e5e5e5`.

### Navigation Button

**Role:** Interactive element

Slightly more rounded neutral button for navigation. `backgroundColor: #ffffff`, `color: #181818`, `borderRadius: 8px`, `padding: 8px 12px`, `borderTopColor: #e5e5e5`.

### Elevated Information Card

**Role:** Information container

Displays content with a subtle shadow for elevation. `backgroundColor: #ffffff`, `borderRadius: 4px`, `boxShadow: rgba(0, 0, 0, 0.02) 0px 13px 8px 0px, rgba(0, 0, 0, 0.03) 0px 6px 6px 0px, rgba(0, 0, 0, 0.04) 0px 1px 3px 0px`, `padding: 8px`.

### Ghost Content Card

**Role:** Information container

Transparent content card with no borders or shadows. `backgroundColor: rgba(0, 0, 0, 0)`, `borderRadius: 0px`, `boxShadow: none`, `padding: 0px`.

## Layout

The page adheres to a max-width of `1600px`, centrally aligned, creating a contained and structured feel. The hero section is a full-width blue banner containing animated, code-like graphics, with a centered headline stack. Content sections typically alternate between a background of `Cloud White` and a subtle `Whisper Gray`, providing a clear visual rhythm. Information is presented in organized blocks, often with left-aligned text and accompanying elements, or in distinct feature grids. Dense content blocks are offset by generous vertical spacing of `80px` between sections. The navigation is a fixed top bar, housing utilitarian links and distinctly colored CTA buttons.

## Imagery

This design primarily uses abstract graphics and iconography. The hero section features an animated code-like graphic of small randomized characters on a blue background, conveying a sense of data processing and complex systems. Brand logos are flat, monochrome where possible, and minimally treated, serving a functional rather than decorative role. Photography is absent; visuals are either abstract representations of data/code or clean UI elements. Density is moderate, with images/graphics serving as section dividers or thematic accents rather than illustrative content.

## Dos & Donts

### Do

- Prioritize `Charcoal Black (#181818)` for all primary headings and body text, ensuring high readability against light backgrounds.
- Use `Ignite Orange (#fb631b)` exclusively for primary calls-to-action to maximize visual impact and direct user attention.
- Apply `Light Concrete (#e5e5e5)` for all non-functional borders and dividers to maintain a discreet, structured appearance.
- Maintain `8px` as the base unit for all spacing, including `cardPadding`, `elementGap`, and `sectionGap` multiples, to ensure rhythm and consistency.
- Employ the `ftSystemMono` font for all technical data, code snippets, and UI toggles like 'Human/Machine' to reinforce the platform's technical nature.
- Utilize `borderRadius: 4px` for primary buttons and elevated cards, and `borderRadius: 2px` for smaller controls and elements for a consistent, subtle softening of edges.

### Don't

- Do not introduce new saturated colors outside of `Ignite Orange (#fb631b)` and `Sky Blueprint (#0d6ea5)` to preserve the system's focused chromatic palette.
- Avoid large, soft shadows; stick to the specified subtle box-shadows `rgba(0, 0, 0, 0.02) 0px 13px 8px 0px` for elevation, or thin borders for definition.
- Do not deviate from the `gerstnerProgramm` and `ftSystemMono` font families, as their interplay is central to the brand's identity.
- Refrain from using `Sky Blueprint (#0d6ea5)` for primary calls-to-action; its role is for secondary interactive elements and links.
- Do not use `borderRadius: 0px` on interactive elements unless it is specifically a `Ghost Button` or part of a component designed for a stark, flat look.
- Avoid dense sections of text; use `Medium Ash (#858483)` for secondary content and ensure adequate line-heights (e.g., 1.5 for body text) to maintain readability within the compact density.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: `#181818` (Charcoal Black)
- Background: `#ffffff` (Cloud White)
- CTA Button: `#fb631b` (Ignite Orange)
- Border: `#e5e5e5` (Light Concrete)
- Link/Secondary Accent: `#0d6ea5` (Sky Blueprint)

### Example Component Prompts
1. **Create a hero section:** Full-width `Ocean Tint (#6fa2e8)` background with `ftSystemMono` characters on it. Centered headline 'AIs are the web's new user' (gerstnerProgramm, weight 500, 36px, #181818). Below it, a sub-headline 'The highest accuracy web search for your AI' (gerstnerProgramm, weight 400, 26px, #181818, 'for your AI' in #fb631b). Below that, a Neutral Button: 'Start building P' (`backgroundColor: #ffffff`, `color: #181818`, `borderRadius: 4px`, `padding: 8px 16px`, `borderTopColor: #e5e5e5`).
2. **Generate an Elevated Information Card:** `backgroundColor: #ffffff`, `borderRadius: 4px`, `boxShadow: rgba(0, 0, 0, 0.02) 0px 13px 8px 0px, rgba(0, 0, 0, 0.03) 0px 6px 6px 0px, rgba(0, 0, 0, 0.04) 0px 1px 3px 0px`, `padding: 8px`. Inside, a heading 'Highest accuracy' (gerstnerProgramm, weight 500, 16px, #181818) and body text 'Production-ready outputs built on cross-referenced facts...' (gerstnerProgramm, weight 400, 13px, #858483).
3. **Design a Human/Machine Toggle:** Two `ftSystemMono` text labels, 'Human' and 'Machine' (weight 400, 13px, `letterSpacing: 0.195px`). When 'Human' is active, its `backgroundColor: #181818` and `color: #ffffff`. 'Machine' is then `backgroundColor: rgba(0, 0, 0, 0)` and `color: #858483`. The entire component has `borderRadius: 2px` on its segmented ends.
4. **Create a Top Banner Notification:** `backgroundColor: #f5f4f1`. Text 'We've raised $100M to build infrastructure for the web's second user.' with font `gerstnerProgramm`, weight 400, 13px, color `#181818`. An inline link 'Read more' within the text, using `color: #0d6ea5` and no underline.

---
_Source: https://styles.refero.design/style/32845f27-6b24-48be-af25-8e664f826b30_
