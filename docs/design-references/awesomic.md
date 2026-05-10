# Awesomic — Design Reference

> white canvas, bold monochrome

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.awesomic.io](https://www.awesomic.io) |
| Refero page | [https://styles.refero.design/style/8512e28d-5385-4c20-a336-214568c4370c](https://styles.refero.design/style/8512e28d-5385-4c20-a336-214568c4370c) |
| Theme | light |
| Industry | saas |

## Overview

Awesomic presents a crisp, modern aesthetic on a clean white canvas. It leverages high-contrast typography for impactful statements and a sophisticated palette of achromatic neutrals. Color is used sparingly, primarily as a vivid accent for critical content highlights and functional indicators. Components feature distinctive large radii, suggesting a friendly yet refined digital experience, with subtle shadows adding depth without overt skeuomorphism.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#09090b` | neutral | Primary text, prominent headings, solid interactive buttons – a deep near-black that grounds content |
| Graphite | `#3f3f46` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |
| Steel Gray | `#71717a` | neutral | Iconography and subtle decorative accents |
| Silver Mist | `#a1a1aa` | neutral | Helper text, subtle borders, and placeholder text |
| Faded Gray | `#d4d4d8` | neutral | Light borders and dividers for nuanced content separation |
| Cloud White | `#ffffff` | neutral | Page backgrounds, card surfaces, and text on dark backgrounds |
| Canvas White | `#f4f4f5` | neutral | Subtle background for UI elements, sections, and input fields |
| Whisper Gray | `#ececee` | neutral | Surface backgrounds for cards and sections, providing a slight elevation from the main canvas |
| Jet Black | `#18181b` | neutral | Text on white backgrounds where high contrast is needed, and icon fills |
| Deep Gray | `#222222` | neutral | Link colors and certain text elements for a slightly softer contrast than Midnight Ink |
| Vibrant Magenta | `#fe45e2` | accent | Accent for highlighting key information within cards, decorative flourishes |
| Ember Glow | `#ff5a00` | accent | Orange state accent for badges, validation surfaces, and short status labels. Do not promote it to the primary CTA color |

## Typography

### Cosmica

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| weights | 300, 400, 500, 600, 700 |
| sizes | 10px, 12px, 13px, 14px, 15px, 16px, 18px, 20px, 32px, 40px, 56px, 64px |
| lineHeight | 1.00, 1.12, 1.25, 1.28, 1.31, 1.35, 1.45, 1.48, 1.50, 1.55, 1.56, 1.62, 1.64, 1.68, 1.80 |
| letterSpacing | normal |
| substitute | Inter |
| role | The sole typeface for all content, from headings to body text and UI elements. Its confident weights and varied sizes establish hierarchy without needing multiple typefaces. The consistent use of a single font family provides a unified and predictable content experience. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 |  |
| body | 14 |  | 1.56 |  |
| body-lg | 16 |  | 1.62 |  |
| subheading | 18 |  | 1.64 |  |
| heading | 32 |  | 1.28 |  |
| heading-lg | 40 |  | 1.25 |  |
| display-sm | 56 |  | 1.12 |  |
| display | 64 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 36px |
| badges | 12px |
| inputs | 12px |
| buttons | 12px |
| imageMasks | 48px |
| navigation | 12px |
| smallerCards | 28px |
| largeElements | 64px |
| iconContainers | 40px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 28px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Cloud White Canvas | `#ffffff` | 0 | Primary page background, base layer. |
| Canvas White Panel | `#f4f4f5` | 1 | Secondary background for sections, a subtle shift from the base canvas. |
| Whisper Gray Card | `#ececee` | 2 | Background for elevated content cards, providing a distinct, but soft, separation. |

## Components

### Primary Ghost Button

**Role:** Low-prominence action button for navigation or secondary actions

White background, Graphite text and border (#3f3f46). Padding 20px on all sides, 36px radius. Text uses Cosmica, weight 400.

### Primary Filled Button

**Role:** High-prominence call to action

Midnight Ink background (#09090b), Cloud White text (#ffffff). Padding 14px vertical, 18px horizontal (smaller variant), or 12px vertical, 16px horizontal (larger variant). Radius 16px or 14px. Text uses Cosmica, weight 400.

### Secondary Ghost Button

**Role:** Minimalist button for filtering or tagging

Transparent background, Jet Black text (#18181b), 12px radius. Padding 4px vertical, 8px horizontal. No border.

### Filled Brand Card

**Role:** Content card with prominent background

Canvas White background (#f4f4f5), 36px border radius. Padding varies from 28px top to 32px bottom.

### Elevated Subtle Card

**Role:** Content card with slight background distinction

Whisper Gray background (#ececee), 28px border radius. Uniform 24px padding. No shadow.

### Default Input Field

**Role:** Standard user input field

Cloud White background (#ffffff), Faded Gray border on focus, 14px radius. Placeholder text is Steel Gray. Padding 12px vertical, 16px left, 12px right.

### Dark Themed Badge

**Role:** Categorization tag for dark backgrounds

Transparent background, Cloud White text (#ffffff), 12px radius. Padding 4px vertical, 8px horizontal.

### Accent Status Badge (Ember Glow)

**Role:** Highlighting status or key attributes with urgency

Ember Glow background (#ff5a00), Cloud White text (#ffffff), 12px radius. Padding 4px vertical, 8px horizontal.

## Layout

The page primarily uses a max-width contained layout, allowing for crisp edges against the white canvas. The hero section often features a large, impactful headline with associated descriptive text and a call to action. Section rhythm is driven by alternating white and light gray (Canvas White) backgrounds, creating a subtle visual break. Content is generally arranged in two-column layouts, often with text on one side and a supporting visual on the other, or feature grids composed of cards with significant corner radii. The navigation is a sticky top bar, providing persistent access.

## Imagery

Imagery largely consists of tight product crops, abstract visual representations of data/movement, and focused portrait photography. Product screenshots are typically presented without full-bleed backgrounds. Illustrations are flat, filled, and often abstract. Icons are primarily outlined or subtle fills, maintaining a minimalist aesthetic. Photography (e.g., team members) is staged and professional, with a mostly neutral color treatment, sometimes in circular masks. The overall density is balanced, allowing prominent typography to take center stage while visuals support messaging.

## Dos & Donts

### Do

- Use Cosmica with explicit weights for all text elements; do not use system fonts.
- Pair high-contrast text colors like Midnight Ink (#09090b) or Jet Black (#18181b) for primary content and navigation against Cloud White (#ffffff) or Canvas White (#f4f4f5) backgrounds.
- Apply significantly rounded corners (e.g., 36px for cards, 12px for buttons/inputs) to all contained elements for a consistent soft aesthetic.
- Reserve vibrant accent colors like Vibrant Magenta (#fe45e2) and Ember Glow (#ff5a00) for highly specific, functional highlights or decorative touches, not general UI elements.
- Structure sections with clear vertical rhythm using a base unit of 8px, particularly for element gaps, building up to section gaps of 40px.
- Maintain a clean, achromatic background palette using Cloud White (#ffffff), Canvas White (#f4f4f5), and Whisper Gray (#ececee) for distinct surface levels.
- Employ the outlined/ghost button style for secondary actions, using Graphite (#3f3f46) for text and border against a white background.

### Don't

- Avoid introducing additional saturated colors beyond Vibrant Magenta and Ember Glow; the palette is intentionally restrained.
- Do not use subtle shadows for elevation on cards or sections; the design relies on background color shifts and large radii for visual distinction.
- Refrain from using multiple font families or decorative typography; stick to the Cosmica family for all textual content.
- Do not use dark backgrounds for full sections; the theme is predominantly light with dark elements contained within white or light gray surfaces.
- Avoid small, tight radii; larger radii are a defining characteristic of the brand's friendliness.
- Do not vary line-height unless explicitly defined in the type scale data; maintain tight leading for headings and comfortable values for body text.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #09090b
background: #ffffff
border: #3f3f46
accent: #fe45e2
primary action: #09090b (filled action)

Example Component Prompts:
Create a Primary Action Button: #09090b background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
Create a feature card: Whisper Gray background (#ececee), 28px radius, uniform 24px padding. Headline uses Cosmica 32px weight 500 #09090b, body text uses Cosmica 16px weight 400 #3f3f46.
Create a content card with accent: Cloud White background (#ffffff), 36px radius. Contains a small square with Vibrant Magenta (#fe45e2) background for decorative highlighting. Text uses Cosmica 14px weight 400 #3f3f46.

---
_Source: https://styles.refero.design/style/8512e28d-5385-4c20-a336-214568c4370c_
