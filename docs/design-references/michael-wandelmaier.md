# Michael Wandelmaier — Design Reference

> Dark canvas, bold blobs

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://wandelmaier.com](https://wandelmaier.com) |
| Refero page | [https://styles.refero.design/style/5b405eec-67ba-4dd0-8dab-ace000151a78](https://styles.refero.design/style/5b405eec-67ba-4dd0-8dab-ace000151a78) |
| Theme | dark |
| Industry | design |

## Overview

This design system presents a dark canvas with strong typographical elements and sporadic, vibrant accents. Large, custom typefaces create a bold, editorial feel, while custom-shaped, brightly colored interactive elements punctuate the monochrome background. The overall impression is one of confident, art-directed simplicity, eschewing typical interface conventions for a more graphic, playful approach.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |
| Canvas Dark | `#191816` | neutral | Page and section backgrounds, primary surface for content |
| Cloud White | `#ffffff` | neutral | Primary text, ghost button borders, button text on accent colors |
| Soft Gray | `#a9a9a9` | neutral | Muted text, secondary information, divider lines |
| Deep Graphite | `#302f2d` | neutral | Subtle borders and dividers on dark backgrounds |
| Spring Bud | `#34a847` | brand | Primary action background, active badge, success states. Provides a vivid, high-contrast focal point |
| Flamingo Pink | `#fbcbcb` | accent | Decorative card background, visual interest element. Creates a soft, warm highlight |
| Sunset Orange | `#f27851` | accent | Decorative card background, visual interest element. Adds a strong, vibrant pop |

## Typography

### PolySans

| Key | Value |
| --- | --- |
| weight | 100 |
| sizes | 12px, 24px, 32px, 187px |
| lineHeight | 0.90, 1.00, 1.33, 2.50, 2.67 |
| letterSpacing | 0.2000em |
| substitute | system-ui |
| role | Headings and prominent display text that demand attention with a wide, almost whispering character. Its extreme lightness is a signature element. |

### Canela Web

| Key | Value |
| --- | --- |
| weight | 100 |
| sizes | 40px, 50px, 80px |
| lineHeight | 1.20, 1.30, 2.40 |
| substitute | Playfair Display |
| role | Editorial headings, creating an elegant, serif counterpoint to the sans-serif. Its light weight keeps it refined. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.20 |
| substitute | Arial |
| role | Body text and functional UI elements where clarity and neutrality are prioritized. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 1.2 |  |
| subheading | 24 |  | 1.33 | 4.8 |
| heading | 32 |  | 1.33 | 6.4 |
| heading-lg | 40 |  | 1.2 |  |
| display-sm | 50 |  | 1.3 |  |
| display-md | 80 |  | 1.2 |  |
| display-lg | 187 |  | 0.9 | 37.4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 10000px |
| badges | 48px |
| buttons | 72px |

- **elementGap** — 22px
- **sectionGap** — 96px
- **cardPadding** — 32px
- **pageMaxWidth** — 1760px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Dark | `#191816` | 0 | Primary page and section backgrounds, dominant theme for the entire experience. |
| Accent Blob Surface | `#fbcbcb` | 1 | Decorative, irregularly shaped background for specific content blocks, introducing bursts of color. |

## Components

### Primary Action Button

**Role:** Main call to action, visually distinct and interactive.

Filled with Spring Bud (#34a847), text is Midnight Ink (#000000), 72px radius. Padding 29px vertical, 48px horizontal. Uses PolySans at 24px.

### Ghost Outline Button

**Role:** Secondary action or navigation link, subtle and integrates with the dark theme.

Transparent background, Cloud White (#ffffff) text and 2.5px border, 72px radius. Padding 29px vertical, 48px horizontal. Uses PolySans at 24px.

### Accent Blob Card - Flamingo Pink

**Role:** Decorative background for featured items or visual breaks.

Flamingo Pink (#fbcbcb) background, extreme 10000px radius to create a 'blob' shape. No padding, acting purely as a background visual.

### Accent Blob Card - Sunset Orange

**Role:** Decorative background for featured items or visual breaks.

Sunset Orange (#f27851) background, extreme 10000px radius to create a 'blob' shape. No padding, acting purely as a background visual.

### Accent Blob Card - Spring Bud

**Role:** Decorative background for featured items or visual breaks.

Spring Bud (#34a847) background, extreme 10000px radius to create a 'blob' shape. No padding, acting purely as a background visual.

### Standard Badge (Ghost)

**Role:** Categorization or tagging, visually lightweight.

Transparent background, Cloud White (#ffffff) text and 2.5px border, 48px radius. Padding 29px vertical, 61px horizontal. Uses PolySans at 24px.

### Accent Badge (Spring Bud)

**Role:** Highlighted categorization or tag, draws attention.

Spring Bud (#34a847) background, Midnight Ink (#000000) text, 48px radius. Padding 29px vertical, 61px horizontal. Uses PolySans at 24px.

### List Item Divider

**Role:** Separates content lines in lists, subtle presence.

1px solid Deep Graphite (#302f2d) border.

## Layout

The page operates on a contained max-width of 1760px, centered on the screen. The hero section features large, custom typography and vibrant 'blob' components, creating a bold, graphic introduction. Section rhythm is driven by generous vertical spacing (96px section gaps) and the alternation between text-dominant blocks and photo grids. Content often appears in a simple two-column layout for text and images, or in a multi-column grid for work showcases. Navigation is minimal and fixed at the top, integrating subtly into the dark canvas.

## Imagery

This system primarily uses embedded photography treated as full-bleed, unmasked rectangles set against the dark canvas for product and team shots. There's a mix of candid lifestyle and focused close-ups. Icons are minimal, subtle, and appear as outlined strokes in Cloud White. Imagery, especially the full-bleed photos, acts as content showcases rather than purely decorative elements, often integrated into larger grid layouts with generous spacing.

## Dos & Donts

### Do

- Use Canvas Dark (#191816) as the base background for most sections.
- Apply Cloud White (#ffffff) for all primary text elements, including headings and body copy.
- Utilize PolySans Bold (weight 100) and Canela Web (weight 100) at large sizes and Cloud White for impactful headings.
- Embed accent colors (Flamingo Pink #fbcbcb, Sunset Orange #f27851, Spring Bud #34a847) in large, irregular 10000px radius 'blob' shapes for visual interest where appropriate.
- Implement 72px radius for all interactive buttons and 48px for badges to maintain the rounded, soft aesthetic.
- Maintain a clear visual hierarchy by using Spring Bud (#34a847) only for primary actions and key highlights.
- Ensure consistent spaciousness with column/element gaps of 22px and section gaps of 96px.

### Don't

- Avoid using multiple accent colors in close proximity; let one color pop per visual cluster.
- Do not use box-shadows or significant elevation; maintain a flat, graphic aesthetic.
- Refrain from tight, dense layouts; prioritize generous spacing for a breathy, premium feel.
- Do not use generic system fonts for prominent headings; PolySans and Canela Web are critical for brand identity.
- Avoid sharp corners; all interactive elements and decorative shapes should feature extreme rounding (48px, 72px, 10000px).
- Do not vary font weights for PolySans headers; the consistent weight 100 is a signature choice.
- Avoid using any other color than Cloud White (#ffffff) for primary text on dark backgrounds.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- text: #ffffff
- background: #191816
- border: #a9a9a9
- accent: #fbcbcb
- primary action: #34a847 (filled action)

**Example Component Prompts:**
- Create a ghost button for navigation: text 'Explore Projects', PolySans, weight 100, 24px, #ffffff, 2.5px #ffffff border, 72px radius, 29px vertical/48px horizontal padding.
- Build a hero headline: 'everyday ideas' using PolySans, weight 100, 187px, lineHeight 0.9, #ffffff, letter-spacing 37.4px. Pair it with a 'work - 2023' accent blob in Flamingo Pink (#fbcbcb), 10000px radius, 0 padding.
- Design a list item: left-aligned text 'Upwork' in Cloud White (#ffffff), secondary text 'METALAB • 2021' in Soft Gray (#a9a9a9), on Canvas Dark (#191816) background. Separate with a 1px Deep Graphite (#302f2d) border at the bottom.
- Create an accent badge: Text 'People & Purpose', PolySans, weight 100, 24px, #ffffff, 2.5px #ffffff border, 48px radius, 29px vertical/61px horizontal padding.

---
_Source: https://styles.refero.design/style/5b405eec-67ba-4dd0-8dab-ace000151a78_
