# Steep — Design Reference

> Warm, Crisp Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://steep.app](https://steep.app) |
| Refero page | [https://styles.refero.design/style/75fdb89f-ca64-41b3-af36-7a78bd09448e](https://styles.refero.design/style/75fdb89f-ca64-41b3-af36-7a78bd09448e) |
| Theme | light |
| Industry | ai |

## Overview

Steep captures a 'white canvas with depth' aesthetic: crisp backgrounds, carefully chosen neutrals, and a warm, inviting accent. The overall impression is one of restrained elegance and confident clarity, suitable for an analytics platform. Typography plays a central role with a high-contrast serif for display elements paired with a clean sans-serif for UI, creating both gravitas and legibility. Subtle shadows provide gentle elevation, preventing the interface from feeling flat.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas | `#ffffff` | neutral | Primary page and surface background, text color for dark elements. Used for the main content area, cards, and prominent UI elements |
| Ink | `#17191c` | neutral | Primary text color, background for filled buttons, navigation elements. A dominant dark neutral that provides strong contrast |
| Graphite | `#000000` | neutral | Fallback dark text, outline for ghost buttons, decorative borders |
| Warm Mist | `#fbe1d1` | brand | Subtle background for specific cards and sections, providing a soft, almost imperceptible warmth to the interface |
| Terracotta | `#5d2a1a` | brand | Accent for borders, strokes, and decorative elements within cards. A warm brown-orange that serves as a secondary brand accent |
| Fog | `#f7f7f8` | neutral | Secondary surface background, used for subtle differentiation of cards or sections that are slightly less prominent than the primary canvas |
| Muted Stone | `#4c4c4c` | neutral | Secondary text and icon color, used for less prominent information or subtle UI details |
| Light Steel | `#777b86` | neutral | Muted link color, subtle icon fills, and quiet UI elements — used for non-essential text that aids readability without drawing too much attention |
| Hint of Grey | `#a3a6af` | neutral | Placeholder text color for input fields, soft borders |
| Dusk Link | `#8b8c8d` | neutral | Muted icon strokes, separators, and secondary graphic details. Do not promote it to the primary CTA color |

## Typography

### Sohne

| Key | Value |
| --- | --- |
| weight | 400, 430, 450, 480, 500 |
| sizes | 14px, 15px, 16px, 17px, 18px, 22px, 26px |
| lineHeight | 1.00, 1.18, 1.25, 1.31, 1.35, 1.38, 1.43, 1.50 |
| letterSpacing | -0.0090em |
| substitute | system-ui, sans-serif |
| role | The primary typeface for all UI elements, body text, buttons, and navigation. Its condensed nature and precise tracking maintain clarity and a compact feel across the interface. |

### Signifier

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 44px, 64px, 90px |
| lineHeight | 1.10 |
| letterSpacing | -0.0250em, -0.0150em |
| substitute | serif |
| role | Used exclusively for prominent headings, this serif typeface provides a sophisticated and editorial counterpoint to the utilitarian Sohne. Its generous tracking at larger sizes gives it an open, authoritative presence. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.43 | 0 |
| heading | 22 |  | 1.18 | 0 |
| heading-lg | 26 |  | 1 | -0.23 |
| display | 44 |  | 1.1 | -0.66 |
| display-lg | 64 |  | 1.1 | -0.96 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| images | 12px |
| inputs | 16px |
| buttons | 1.67772e+07px |
| default | 24px |

- **elementGap** — 8px
- **sectionGap** — 80px
- **cardPadding** — 20px
- **pageMaxWidth** — 1280px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#ffffff` | 1 | Primary page background and default card surface. Bright, high-contrast base. |
| Fog | `#f7f7f8` | 2 | Secondary background for subtly differentiated sections or cards, offering a slight visual break from the main canvas. |
| Warm Mist | `#fbe1d1` | 3 | Accent background for featured cards or UI elements, introducing a soft, warm hue. |

## Components

### Filled Primary Button

**Role:** Main call-to-action

Solid 'Ink' (#17191c) background with 'Canvas' (#ffffff) text, using a very large border-radius to create a pill shape. Padding is 0px vertical, 20px horizontal.

### Ghost Button (Dark Text)

**Role:** Secondary action or link

Transparent background with 'Ink' (#17191c) text. Border is 1px 'Ink' (#17191c). Pill-shaped with large border-radius. Padding is 0px vertical, 20px horizontal.

### Ghost Button (Light Text)

**Role:** Secondary action on dark backgrounds

Transparent background with 'Canvas' (#ffffff) text. Border is 1px 'Canvas' (#ffffff). Pill-shaped with large border-radius. Padding is 0px vertical, 20px horizontal.

### Text Link Button

**Role:** Inline actions or navigational links

Transparent background with 'Ink' (#17191c) text, no border. Typically used for less prominent actions, often without horizontal padding.

### Default Card

**Role:** Content container

Background is 'Canvas' (#ffffff) with 20px border-radius. Features a prominent shadow: rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20px 25px -5px, rgba(0, 0, 0, 0.1) 0px 8px 10px -6px. Internal padding varies from 16px to 20px.

### Subtle Background Card

**Role:** Secondary content container or data visualization background

Background is 'Fog' (#f7f7f8) with 24px border-radius and no shadow. Internal content padding is typically 0.

### Accent Background Card

**Role:** Decorative or featured content container

Background is 'Warm Mist' (#fbe1d1) with 24px border-radius and no shadow. Internal content padding is typically 0. May feature 'Terracotta' borders on embedded elements.

### Input Field

**Role:** User input element

Placeholder text in 'Hint of Grey' (#a3a6af). Background is 'Canvas' (#ffffff) with a 1px border in 'Ink' (#17191c) on focus and 16px border-radius.

## Layout

The page model alternates between a full-bleed hero and a max-width 1280px contained layout for content sections. The initial hero features a centered headline over a soft, almost imperceptible gradient background, accompanied by floating UI elements. Subsequent sections typically follow a consistent vertical rhythm with minimal visual dividers, emphasizing a seamless flow of information. Content is primarily arranged in grid-like patterns, often with asymmetric compositions or alternating text-left/image-right (or UI element) sections. Card grids are frequently used for features and data visualization. The layout maintains a spacious density, with ample breathing room around elements. Navigation consists of a sticky top bar with clearly delineated product and action buttons.

## Imagery

This site prominently uses clean, abstract 3D elements and UI screenshots to convey complex data with a sense of clarity and modern polish. Photography is absent. Illustrations are minimalistic, focusing on geometric shapes and soft gradients that complement the palette. Icons are outlined, lightweight, and mono-color, typically rendered in 'Ink' (#17191c) or 'Muted Stone' (#4c4c4c). Imagery serves an explanatory and product showcase role rather than decorative. Visuals are carefully contained within rounded cards or sections, maintaining the spacious, organized feel, making the design feels image-heavy in its content sections rather than text-dominant.

## Dos & Donts

### Do

- Use 'Ink' (#17191c) for primary text and filled buttons to ensure strong contrast and clear calls to action.
- Apply 'Canvas' (#ffffff) as the default background for most page sections and card surfaces, creating a bright and airy feel.
- Pair the 'Signifier' font at large sizes for headlines with 'Sohne' for all body text and UI elements to leverage their distinct roles.
- Utilize a 24px border-radius for cards and main content blocks to maintain a soft, approachable aesthetic.
- Ensure generous spacing, with `elementGap` of 8px and `sectionGap` of 80px, to support content readability and perceived quality.
- Implement the full shadow stack (rgba(4, 23, 43, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 20px 25px -5px, rgba(0, 0, 0, 0.1) 0px 8px 10px -6px) for elevated cards to provide subtle depth without heaviness.
- Use 'Warm Mist' (#fbe1d1) sparingly as a background accent to introduce a subtle warmth and highlight featured content.

### Don't

- Avoid using highly saturated colors; the palette favors muted neutrals with a single warm accent.
- Do not use 'Signifier' for body text, UI labels, or buttons; its distinctive character is reserved for large display headings.
- Refrain from sharp, 0px border-radii for interactive elements or containers; maintain the consistent rounded aesthetic.
- Do not introduce heavy, opaque shadows; elevation should be subtle and air-like, not dense or dark.
- Avoid tight spacing between elements; maintain adequate `elementGap` and `sectionGap` values to preserve visual order and reduce cognitive load.
- Do not use dark backgrounds for primary page sections; the system is built around a light, high-contrast canvas.
- Resist using multiple font families beyond 'Sohne' and 'Signifier'; maintain typographic consistency.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #17191c
background: #ffffff
border: #17191c
accent: #fbe1d1
primary action: #17191c (filled action)

### Example Component Prompts
1. Create a Primary Action Button: #17191c background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a Feature Card: Use a 'Default Card' with 'Canvas' (#ffffff) background. Place a small 'Terracotta' (#5d2a1a) line at the top. The title is 'Sohne' 22px weight 500, color 'Ink' (#17191c). Body text is 'Sohne' 15px weight 400, color 'Muted Stone' (#4c4c4c). Include 20px padding.

---
_Source: https://styles.refero.design/style/75fdb89f-ca64-41b3-af36-7a78bd09448e_
