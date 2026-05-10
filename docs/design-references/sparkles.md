# Sparkles — Design Reference

> Whiteboard of playful engineering

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://sparkles.dev](https://sparkles.dev) |
| Refero page | [https://styles.refero.design/style/eefc0305-818c-4f57-80ce-a4bc1f7aac92](https://styles.refero.design/style/eefc0305-818c-4f57-80ce-a4bc1f7aac92) |
| Theme | light |
| Industry | devtools |

## Overview

Sparkles adopts a playful yet precise technical aesthetic: a clean white canvas animated by expressive, cartoonish illustrations. Typography is grounded in a stark, confident black with selective use of lighter neutrals for secondary text. Components are minimal, relying on subtle backgrounds and rounded corners rather than shadows, suggesting a user-friendly system that prioritizes clarity and approachability.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#0a0a0a` | neutral | Primary text, prominent headings, key interface elements |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, form inputs |
| Steel Gray | `#18181b` | neutral | Button backgrounds for primary actions, secondary text, navigation links |
| Cloud Cover | `#e5e5e5` | neutral | Hairline borders, subtle dividers, inactive states |
| Pale Ash | `#f4f4f5` | neutral | Subtle background for hero sections and some card surfaces |
| Soft Fog | `#fafafa` | neutral | Subtle background for alternate card surfaces, hover states |
| Pale Stone | `#d4d4d8` | neutral | Muted body text, helper text, card borders |
| Cool Graphite | `#52525c` | neutral | Secondary body text, less prominent links |
| Icon Stroke | `#71717b` | neutral | Icon strokes, tertiary text, meta information |
| Input Fill | `#27272a` | neutral | Input background fill, minor body text |
| Muted Border | `#3f3f46` | neutral | Button borders, input borders, less prominent text |
| Success Green | `#54b16c` | accent | Green wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |

## Typography

### Articulat CF

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 12px, 14px, 16px, 18px, 20px |
| lineHeight | 1.33, 1.40, 1.43, 1.50, 1.56, 1.60, 1.75, 1.78 |
| letterSpacing | 0.28em |
| substitute | Inter |
| role | All body text, navigation items, buttons, and general UI elements. The consistent, slightly open letter spacing on this sans-serif font ensures readability even at smaller sizes. |

### Gelica

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 36px, 48px, 60px |
| lineHeight | 1.00, 1.11 |
| letterSpacing | normal |
| substitute | Georgia |
| role | Prominent headings and display text. Its serif nature and heavier weight provide a bold, distinctive contrast to the sans-serif body text, establishing clear visual hierarchy with authority. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.78 | 0.336 |
| body-sm | 14 |  | 1.75 | 0.392 |
| body | 16 |  | 1.56 | 0.448 |
| subheading | 18 |  | 1.5 | 0.504 |
| heading-sm | 20 |  | 1.43 | 0.56 |
| heading | 36 |  | 1.11 |  |
| heading-lg | 48 |  | 1.11 |  |
| display | 60 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 10px |
| forms | 10px |
| buttons | 10px |
| default | 10px |
| largeCards | 14px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background |
| Pale Ash | `#f4f4f5` | 1 | Subtle background for specific sections or cards to create visual separation without high contrast |
| Soft Fog | `#fafafa` | 2 | Alternative subtle background for cards or containers, slightly off-white |

## Components

### Primary Filled Button

**Role:** Call to action button

Solid Steel Gray background (#18181b), Canvas White text (#ffffff), 10px border radius, 8px vertical padding, 16px horizontal padding. Features a subtle shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px.

### Text Only Button

**Role:** Secondary action or ghost button

Transparent background, Midnight Ink text (#0a0a0a), 0px padding, 8px border radius. Used for less prominent actions within content.

### Navigation Link Button

**Role:** Header navigation item, text-based action

Transparent background, Steel Gray text (#18181b), 10px border radius, 0px vertical padding, 16px horizontal padding.

### Informational Card

**Role:** Content container for features or pricing

Canvas White background (#ffffff), 10px border radius, 24px top/side padding, 28px bottom padding, no shadow.

### Alt Background Card

**Role:** Content container with subtle background

Soft Fog background (#fafafa), 10px border radius, 24px top/side padding, 28px bottom padding, no shadow.

### Large Feature Card

**Role:** Prominent content section, e.g., hero or main feature

Canvas White background (#ffffff), 14px border radius, 40px all-around padding, no shadow.

### Form Input Field

**Role:** User input for forms

Input Fill background (#27272a), Canvas White text (#ffffff), Muted Border color (#3f3f46), 10px border radius, 0px vertical padding, 16px right/left padding, 40px left padding for icon. No shadow.

## Dos & Donts

### Do

- Use Gelica font with weight 600 for all primary headings, ensuring line heights of 1.0 or 1.11 for compact presentation.
- Apply Articulat CF for all body text, navigation, and button labels with a letter spacing of 0.28em for consistent readability.
- Prioritize Canvas White (#ffffff) and Pale Ash (#f4f4f5) for background surfaces, distinguishing content blocks with subtle shifts in brightness.
- Employ Midnight Ink (#0a0a0a) or Steel Gray (#18181b) for primary and secondary text to maintain strong contrast on light backgrounds.
- Utilize 10px border radius as the default for buttons, cards, and input fields to maintain a consistent soft edginess.
- Maintain a clear visual hierarchy with generous vertical spacing (40px) between sections and moderate spacing (16px) between UI elements.
- Reserve the Steel Gray (#18181b) filled button for primary calls to action, ensuring it appears as the most direct interactive element.

### Don't

- Avoid applying strong shadows to components; use subtle elevations only as specified for primary buttons or omit them entirely.
- Do not introduce new saturated primary colors; limit the palette to the established neutrals and the single semantic Success Green for decorative use.
- Do not use letter-spacing on display headings (Gelica font); let them maintain a normal tracking.
- Avoid using multiple border styles or colors for cards; maintain a clean, borderless appearance or use Pale Stone (#d4d4d8) for subtle borders.
- Do not use dark backgrounds for entire sections unless justified by a component that explicitly requires it.
- Refrain from using bold or heavy weights for Articulat CF body text; keep it at 400 or 500 for readability.
- Do not overcrowd sections; maintain the comfortable density with clear element and section gaps.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
text: #0a0a0a
background: #ffffff
border: #e5e5e5
accent: #54b16c
primary action: #18181b (filled action)

**3-5 Example Component Prompts:**
1. Create a Primary Action Button: #18181b background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a Feature Card on Canvas White background (#ffffff): Articulat CF weight 500 at 18px for a subheading, color Midnight Ink (#0a0a0a), letter-spacing 0.504px, line-height 1.5. Body text Articulat CF weight 400 at 16px, color Cool Graphite (#52525c), letter-spacing 0.448px, line-height 1.56. The card should have a 10px radius and 40px all-around padding.

---
_Source: https://styles.refero.design/style/eefc0305-818c-4f57-80ce-a4bc1f7aac92_
