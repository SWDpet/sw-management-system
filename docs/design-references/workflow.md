# Workflow — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.workflow.design](https://www.workflow.design) |
| Refero page | [https://styles.refero.design/style/71451d9e-9a8a-4858-9a91-fbe44047e110](https://styles.refero.design/style/71451d9e-9a8a-4858-9a91-fbe44047e110) |
| Theme | light |
| Industry | design |

## Overview

Workflow adopts a clean, functional workspace aesthetic with a predominantly achromatic palette, emphasizing content and interaction. Subtle surface variations and hairline borders create a sense of depth without heavy shadows. Typography is precise and utilitarian, with a single serif accent for display headlines. Interactive elements are lightly styled, relying on hover states and clear hierarchy rather than bold color to guide user attention.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas | `#ffffff` | neutral | Primary page background, elevated card surfaces |
| Ink | `#1a1a1a` | neutral | Primary text color for headings and body content, strong borders, dark icons, and inverted surface backgrounds |
| Ash | `#6a6a6a` | neutral | Secondary text for helper content, weaker border lines, and muted icons |
| Fog | `#f6f6f6` | neutral | Subtle background for buttons, badges, and alternative section backgrounds |
| Pebble | `#ececec` | neutral | Tertiary background for cards and feature blocks, subtly distinct from Canvas |
| Frost | `#e3e3e3` | neutral | Lightest border lines for dividing elements and weak outlines |
| Driftwood | `#4d3f32` | brand | Decorative highlights, text accents in specific content sections |
| Success Green | `#547e69` | accent | Green outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Success Pale | `#60886f` | accent | Green wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Error Red | `#923d56` | accent | Red outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Success Wash | `#f1fcf6` | neutral | Primary page canvas and white card surfaces. Use as a supporting accent, not as a status color |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 11px, 12px, 13px, 14px, 15px, 16px |
| lineHeight | 1.00 - 1.91 |
| letterSpacing | -0.0040em, 0.0020em, 0.0030em, 0.0040em |
| substitute | system-ui, sans-serif |
| role | Primary typeface for all body text, UI labels, navigation, and button text. Its versatility and geometric neutrality support the functional aesthetic. |

### Crimson Pro

| Key | Value |
| --- | --- |
| weight | 300 |
| weights | 300 |
| sizes | 26px, 32px |
| lineHeight | 1.00 |
| letterSpacing | normal |
| substitute | Georgia, serif |
| role | Display typeface for prominent headlines and calls to action. The lighter weight (300) delivers authority through restraint rather than visual density, contrasting with the utilitarian sans-serif. |

### Georgia

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px |
| lineHeight | 1.35, 1.62 |
| role | Georgia — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 | 0.003 |
| heading-lg | 26 |  | 1 | 0 |
| display | 32 |  | 1 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 9999px |
| cards | 12px |
| input | 8px |
| badges | 6px |
| buttons | 8px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#ffffff` | 1 | Base page background and principal surface for content. |
| Fog | `#f6f6f6` | 2 | Secondary background for sections, buttons, and badges to add subtle differentiation. |
| Pebble | `#ececec` | 3 | Tertiary background for elevated cards and feature blocks, providing a slightly darker context. |

## Components

### Primary Ghost Button

**Role:** Interactive element

Ghost button for primary actions: #f6f6f6 background, Ink (#1a1a1a) text, 8px border radius, 16px horizontal and 8px vertical padding. Subtle, yet clearly interactive.

### Pill Button

**Role:** Interactive element

Circular or pill-shaped button for secondary actions or toggles: rgba(26,26,26,0.65) text and border, 9999px border radius, 16px padding on all sides. Used for minimal, iconic actions.

### Feature Card

**Role:** Container

Information or feature display card: Canvas (#ffffff) background, 12px border radius, 24px padding on all sides. Features a subtle elevation with rgba(0,0,0,0.06) 0px 2px 6px 0px shadow.

### Section Card

**Role:** Container

Minimalist card to group content sections: Pebble (#ececec) background, 12px border radius, no padding in all sides. Used for subtle content separation.

### Pill Badge

**Role:** Content label

Small, informative label: Fog (#f6f6f6) background, Ink (#1a1a1a) text, 6px border radius, 7px horizontal and 4px vertical padding. For tags and status indicators.

### Inline Text Badge

**Role:** Content label

Simple text badge without background: Ink (#1a1a1a) text, minimal padding and no radius. Integrates cleanly into body copy.

### Input Field

**Role:** Form element

Standard text input or selection field: 8px border radius, Ink (#1a1a1a) text, and a 1px border. Placeholder text in Ash (#6a6a6a).

## Layout

The page maintains a centered max-width of 1200px, creating a contained and focused experience. The hero section features a prominent, centered headline with supporting text and a primary action button against the Canvas background. Sections follow a consistent rhythm of alternating white and light gray backgrounds, creating visual segmentation without explicit dividers. Content is generally arranged in stacks or two-column layouts, often with text on one side and a visual element (screenshot, card) on the other. Navigation consists of a clear top bar with text links and subtle ghost buttons, maintaining a clean, unobtrusive presence.

## Imagery

The visual language for imagery is primarily functional and integrated with the UI. Product screenshots or abstract graphics are contained within cards or sections, often with rounded corners (8px or 12px) and rarely overlapping. If photography is present, it appears as tight product crops or profile images within UI elements. Icons are predominantly outlined, using Ink (#1a1a1a) or Ash (#6a6a6a), with a consistent medium stroke weight; they serve an explanatory or decorative role rather than a primary content focus. The density is text-dominant, with imagery serving as supportive evidence or visual breaks.

## Dos & Donts

### Do

- Prioritize Ink (#1a1a1a) for primary text and Canvas (#ffffff) for backgrounds to ensure AAA contrast.
- Use Inter font for all functional text elements and Crimson Pro weight 300 for display headlines.
- Employ consistent 8px border radius for interactive elements (buttons, inputs) and 12px for cards.
- Adhere to an element gap of 16px to maintain a comfortable density.
- Utilize Fog (#f6f6f6) for secondary button backgrounds and subtle background shifts.
- Apply hairline borders using Frost (#e3e3e3) or Ink (#1a1a1a) for structural separation over heavy dividers.
- Implement the 'Pill Button' style using 9999px border radius for isolated actions or toggles.

### Don't

- Avoid strong, saturated colors for UI backgrounds or primary actions; reserved for semantic feedback or decorative accents.
- Do not deviate from Inter or Crimson Pro for typography; do not introduce additional font families.
- Refrain from using heavy drop shadows; implement subtle elevation with single rgba(0,0,0,0.06) 0px 2px 6px 0px shadows when depth is required.
- Do not use generic gray values; always refer to the defined neutral palette (Ink, Ash, Fog, Pebble, Frost).
- Avoid over-animating; transitions should be subtle, with 0.2s duration and ease timing.
- Do not break the established spacing hierarchy; maintain 16px element gaps and 24px card padding.
- Refrain from using color to indicate interaction unless it's a semantic state (success/error); rely on hover states and subtle background changes.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #1a1a1a
background: #ffffff
border: #e3e3e3
accent: #4d3f32
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Design a feature card: #ffffff background, 12px border radius, box-shadow rgba(0, 0, 0, 0.06) 0px 2px 6px 0px, 24px padding on all sides, heading text #1a1a1a Crimson Pro weight 300 size 26px, body text #1a1a1a Inter weight 400 size 14px.
3. Implement a pill badge: #f6f6f6 background, #1a1a1a text, 6px border radius, 7px horizontal padding, 4px vertical padding, Inter font weight 400, size 12px.
4. Build an input field: 8px border radius, 1px #e3e3e3 border, placeholder text #6a6a6a Inter weight 400, size 14px, label text #1a1a1a Inter weight 500, size 14px.

---
_Source: https://styles.refero.design/style/71451d9e-9a8a-4858-9a91-fbe44047e110_
