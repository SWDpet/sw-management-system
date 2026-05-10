# Podscan.fm — Design Reference

> Whiteboard with digital highlights

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://podscan.fm](https://podscan.fm) |
| Refero page | [https://styles.refero.design/style/542d4d5c-fd8f-4a8b-a4f7-4694728f7e12](https://styles.refero.design/style/542d4d5c-fd8f-4a8b-a4f7-4694728f7e12) |
| Theme | light |
| Industry | media |

## Overview

Podscan employs a productivity-focused aesthetic with a clean white canvas and soft, elevated components. Typography is clear and direct, using both a system sans-serif for body text and a tighter custom sans-serif for headings, creating distinct information hierarchies. A spectrum of saturated, functional colors provides visual disambiguation and branding for unique features or states, puncturing the otherwise minimalist gray palette.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#f4f4f5` | neutral | Soft icon strokes, subtle dividers, and low-emphasis decorative details. Do not promote it to the primary CTA color |
| Graphite | `#52525b` | neutral | Secondary text, muted helper text, table content |
| Mid Grey | `#71717a` | neutral | Tertiary text, link text, lighter body copy |
| Silver Pine | `#a1a1aa` | neutral | Muted body text, placeholder text, inactive icons |
| Cloud Whisper | `#d4d4d8` | neutral | Light borders, inactive input borders, subtle dividers |
| Ash Felt | `#e5e7eb` | neutral | Hairline borders, subtle separators, card borders |
| White Smoke | `#fafafe` | neutral | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |
| Pure White | `#ffffff` | neutral | Page backgrounds, card surfaces, primary button backgrounds |
| Midnight Ink | `#18181b` | neutral | Primary text, headers, strong body copy |
| Emerald Spark | `#059669` | brand | Green action color for filled buttons, selected navigation states, and focused conversion moments |
| Radiant Green | `#10b981` | brand | Distinct icon accent, interactive elements' stroke and fill |
| Vivid Violet | `#9333ea` | brand | Violet action color for filled buttons, selected navigation states, and focused conversion moments. |
| Hyper Blue | `#2563eb` | brand | Accent color for features, icon color and stroke |
| Magenta Burst | `#ec4899` | brand | Icon stroke and fill, accent background for specific sections |
| Crimson Flux | `#db2777` | brand | Accent for features, icon color and stroke |
| Azure Haze | `#a5b4fc` | accent | Decorative border for pricing cards, inactive states |
| Amethyst Tint | `#e9d5ff` | accent | Decorative border for pricing cards, muted accents |
| Leafy Green | `#16a34a` | accent | Green text accent for links, tags, and emphasized short phrases. Use as a supporting accent, not as a status color |
| Warning Yellow | `#eab308` | accent | Yellow wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Scarlet Alert | `#b91c1c` | accent | Red text accent for links, tags, and emphasized short phrases. Use as a supporting accent, not as a status color |

## Typography

### Inter Tight

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| weights | 400, 600, 700 |
| sizes | 14px, 20px, 24px, 30px, 36px, 60px |
| lineHeight | 1.00, 1.11, 1.20, 1.33, 1.40, 1.43 |
| letterSpacing | -0.029em at 14px to -0.011em at 60px |
| substitute | system-ui |
| role | Headings and key callouts. Its tightness creates a modern, condensed feel suitable for prominent textual elements, commanding attention without being overly bold. |

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 14px, 16px, 18px, 20px, 30px |
| lineHeight | 1.20, 1.33, 1.38, 1.40, 1.43, 1.50, 1.56 |
| letterSpacing | -0.033em at 12px to -0.013em at 30px |
| substitute | system-ui |
| role | Body text, navigation, buttons, and general UI elements. Its clean, readable nature ensures clarity across various UI contexts, preferring system fonts for efficiency. |

### ui-monospace

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.33 |
| letterSpacing | -0.033em |
| substitute | monospace |
| role | Used sparingly for code snippets or specific data displays, providing a technical contrast to the primary sans-serifs. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.33 | -0.396 |
| body-sm | 14 |  | 1.43 | -0.406 |
| body | 16 |  | 1.5 | -0.4 |
| subheading | 18 |  | 1.56 | -0.36 |
| heading-sm | 20 |  | 1.4 | -0.4 |
| heading | 24 |  | 1.33 | -0.48 |
| heading-lg | 30 |  | 1.2 | -0.6 |
| display | 60 |  | 1 | -0.66 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 12px |
| icons | 8px |
| inputs | 12px |
| buttons | 8px |

- **elementGap** — 16px
- **sectionGap** — 72px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Pure White Canvas | `#ffffff` | 0 | Primary page background and default component surface |
| White Smoke Surface | `#fafafe` | 1 | Subtle background for specific UI elements like inputs and tertiary buttons, slightly distinct from canvas |
| Absolute Zero Background | `#f4f4f5` | 2 | Alternative background for sections and larger containers, providing a slight contrast to the main canvas without introducing significant color |

## Components

### Primary Call to Action Button

**Role:** Filled button indicating the primary path forward.

Background: Emerald Spark (#059669), text: Pure White (#ffffff). Padding: 14px vertical, 32px horizontal. Border radius: 8px. Text uses ui-sans-serif, weight 400.

### Secondary Ghost Button

**Role:** Subtle action, less prominent than primary.

Background: Pure White (#ffffff), text: Midnight Ink (#18181b). Border: 1px solid Ash Felt (#e5e7eb). Padding: 12px vertical, 16px horizontal. Border radius: 12px. Text uses ui-sans-serif, weight 400.

### Tertiary Tab Button

**Role:** Small, information-based button for categories or filtering.

Background: White Smoke (#fafafe), text: Graphite (#52525b). Border: 1px solid Ash Felt (#e5e7eb). Padding: 4px vertical, 10px horizontal. Border radius: 9999px (pill-shaped). Text uses ui-sans-serif, weight 400.

### Floating Card

**Role:** Elevated container for content sections, usually informative.

Background: Pure White (#ffffff). Padding: 32px. Border radius: 12px. Shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px.

### Clean Card

**Role:** Basic container for grouped content, minimal elevation.

Background: Pure White (#ffffff). Padding: 16px. Border radius: 8px. Shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px.

### Flat Card

**Role:** Simple background grouping without elevation.

Background: Absolute Zero (#f4f4f5). Padding: 16px. Border radius: 8px. No shadow.

### Pricing Tier Card (Professional)

**Role:** Highlighting a specific pricing tier with brand accent.

Background: Pure White (#ffffff). Padding: 32px. Border radius: 12px. Border: 2px solid Emerald Spark (#059669). Shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px. Features a 'Most Popular' tag with Emerald Spark (#10b981) background.

### Search Input

**Role:** Text input field for searching content.

Background: Pure White (#ffffff), text: Midnight Ink (#18181b), placeholder: Silver Pine (#a1a1aa). Border: 1px solid Ash Felt (#e5e7eb). Padding: 12px vertical, 48px left, 112px right. Border radius: 12px. Icon is part of the input, ui-sans-serif font.

### Pill Tag

**Role:** Small, interactive tag for filtering or highlighting terms.

Background: White Smoke (#fafafa), text: Mid Grey (#71717a). Border: 1px solid Ash Felt (#e5e7eb). Padding: 4px vertical, 10px horizontal. Border radius: 9999px. Text uses ui-sans-serif, weight 400.

## Layout

The page adheres to a max-width contained layout, approximately 1200px, with content consistently centered. The hero section features a prominent, centered headline and subtext, followed by two primary call-to-action buttons. Sections alternate between pure white and light gray backgrounds, creating a subtle visual rhythm. Content is often arranged in card grids (3-column, 4-column) or text-over-icon blocks. Navigation is a sticky top bar with a left-aligned logo and right-aligned primary links and one primary action button.

## Imagery

This site predominantly uses clean, color-accented icons with solid fills or precise strokes for functional visual communication. There is minimal use of photography or complex illustrations. Product screenshots are typically presented as contained within clean, rounded frames. Iconography is a mix of mono-color (often using accent colors like Radiant Green or Vivid Violet) and occasional two-tone designs, maintaining clear, legible forms, and serving an explanatory and decorative role.

## Dos & Donts

### Do

- Use Pure White (#ffffff) as the default canvas for primary content blocks.
- Prioritize Emerald Spark (#059669) for all primary calls to action, ensuring full background fill.
- Apply Ash Felt (#e5e7eb) for fine borders, separating UI elements and defining input fields.
- Utilize Inter Tight for all prominent headings above 24px, adjusting letter-spacing according to its scale.
- Maintain an 8px border-radius for all interactive buttons that require a defined boundary.
- Use a minimum element space of 8px (often 16px) around interactive elements.
- Employ the subtle shadow rgba(0, 0, 0, 0.05) 0px 1px 2px 0px for elevated cards to create minimal depth.

### Don't

- Avoid using highly saturated colors for large surface areas; reserve them for functional accents and icons.
- Do not use dark backgrounds for full sections; the theme is predominantly light and airy.
- Do not deviate from the established border radii; maintain 9999px for tags, 8px for buttons, and 12px for cards and inputs.
- Never introduce new font families; rely strictly on ui-sans-serif, Inter Tight, and ui-monospace.
- Avoid deep, heavy shadows; rely on minimal elevation provided by rgba(0, 0, 0, 0.05) 0px 1px 2px 0px.
- Do not use different weights of Inter Tight for body text; reserve it for headings and key phrases.
- Do not use brand or accent colors (e.g., Vivid Violet, Hyper Blue) for text unless it is for an icon, link, or specific header described in the tokens, to maintain neutral content readability.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #18181b
background: #ffffff
border: #e5e7eb
accent: #10b981
primary action: #059669 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #059669 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a pricing card 'Starter': Pure White (#ffffff) background, 12px border radius, rgba(0, 0, 0, 0.05) 0px 1px 2px 0px shadow. Heading 'Starter' at 24px Inter Tight weight 700, #18181b, letter-spacing -0.48px. Feature text in Graphite (#52525b) at 16px ui-sans-serif weight 400. Button 'Get Started' with Pure White (#ffffff) background, Midnight Ink (#18181b) text, 8px radius, 12px 16px padding, ui-sans-serif weight 400.
3. Create a search input field: Pure White (#ffffff) background, 12px border radius, 1px solid Ash Felt (#e5e7eb). Placeholder text 'Search' in Silver Pine (#a1a1aa) at 16px ui-sans-serif weight 400. Current input text 'podcast' in Midnight Ink (#18181b) at 16px ui-sans-serif weight 400. Include a search icon in Silver Pine (#a1a1aa).

---
_Source: https://styles.refero.design/style/542d4d5c-fd8f-4a8b-a4f7-4694728f7e12_
