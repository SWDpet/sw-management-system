# 11x– Digital workers — Design Reference

> High-contrast digital command station

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.11x.ai](https://www.11x.ai) |
| Refero page | [https://styles.refero.design/style/850ee61c-4ecd-4558-9c0c-fab99721b34c](https://styles.refero.design/style/850ee61c-4ecd-4558-9c0c-fab99721b34c) |
| Theme | light |
| Industry | ai |

## Overview

The 11x aesthetic centers on a functional, high-contrast digital workspace. It combines stark monochrome surfaces with compact, direct typography. A muted, earthy undertone provides subtle warmth beneath the UI, while a single, vivid teal accent punctuates key information and calls to action. Components are lightweight, favoring ghost elements and subtle elevation, creating a sense of efficiency and focus.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary text, button backgrounds, strong borders — foundation of the high-contrast UI |
| Canvas White | `#ffffff` | neutral | Page background, card surfaces, ghost button text — core light surface |
| Desert Dune | `#d7cecc` | neutral | Footer background, secondary card surfaces, and subtle section dividers – provides a soft, warm achromatic base; Subtle background gradient for soft transitions between surfaces |
| Misty Rose | `#d2b596` | accent | Subtle background washes, decorative elements — provides a muted, warm accent |
| Digital Teal | `#406e7a` | accent | Badge backgrounds, active indicators — a distinct and vivid accent for functional elements |
| Cloud Gray | `#f6f5f5` | neutral | Subtle card borders and alternative background for cards — acts as a very light neutral surface |
| Soft Peach | `#f5ece5` | neutral | Internal card surfaces, subtle background shifts — a slightly warmer, off-white neutral |
| Lavender Mist | `#efe5f9` | neutral | Decorative card background — provides a subtle, near-neutral tint |
| Rocky Beige | `#ede2d7` | neutral | Decorative card background — provides a subtle, near-neutral tint |
| Slate Gray | `#afaeae` | neutral | Muted button backgrounds, inactive states — a mid-tone neutral |
| Warm Linen | `#e1dad9` | neutral | Internal card surfaces, background hover states — a slightly darker neutral than Canvas White but still very light |
| Glacial Blue | `#c5d5e8` | neutral | Decorative card background — provides a subtle, near-neutral tint |
| Arctic Teal | `#d4e6eb` | neutral | Decorative card background — provides a subtle, near-neutral tint |
| Error Umber | `#4c312b` | semantic | Error badge text/border — used for semantic feedback |

## Typography

### ES Allianz

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| weights | 400, 500, 700 |
| sizes | 8px, 14px, 16px, 18px, 19px, 28px, 32px, 46px, 56px, 64px, 74px, 152px |
| lineHeight | 1.00 |
| letterSpacing | -0.0450em, -0.0450em, -0.0450em, -0.0400em, -0.0400em, -0.0200em |
| substitute | Inter |
| role | All textual elements from headings to body copy and interface labels. The consistent use of a custom sans-serif font across all scales establishes a unified, crisp, and direct tone. Its variable letter-spacing, particularly tighter at larger sizes, enhances its clean, modern feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 14 |  | 1.4 |  |
| body | 16 |  | 1.4 |  |
| subheading | 18 |  | 1.2 |  |
| heading-sm | 28 |  | 1.2 |  |
| heading | 46 |  | 1.1 | -0.04 |
| heading-lg | 56 |  | 1.1 | -0.04 |
| display | 152 |  | 0.85 | -0.045 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| badges | 8px |
| buttons | 999px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Primary page background |
| Cloud Gray | `#f6f5f5` | 2 | Secondary card surfaces, subtle background shifts |
| Soft Peach | `#f5ece5` | 3 | Tertiary card surfaces, slightly warmer off-white |
| Desert Dune | `#d7cecc` | 4 | Muted card backgrounds, footer background, and subtle section separators |

## Components

### Primary Filled Button

**Role:** Primary action

Solid Midnight Ink background with Canvas White text, fully rounded (999px). Padding is 12px vertical, 24px horizontal, signifying a prominent call to action.

### Ghost Nav Link Button

**Role:** Navigation and secondary actions

Transparent background with Midnight Ink text and border, no explicit radius (0px). Padding is 8px vertical, 14px horizontal, used for menu items and less prominent actions maintaining the high-contrast feel.

### Floating Action Button

**Role:** Contextual help

Semi-transparent dark background (rgba(34, 34, 34, 0.4)) with Canvas White text, circular shape (100% radius). This button appears fixed and provides immediate assistance.

### Secondary Filled Button

**Role:** Secondary action

Canvas White background with Midnight Ink text, fully rounded (999px). Padding is 12px vertical, 24px horizontal, offering a visual alternative to the primary button.

### Card, No Padding

**Role:** Container, unpadded

Transparent background, 0px radius, no padding, no shadow. Used for content that extends to edges or for structural grouping.

### Card, Standard Padding

**Role:** Standard content container

Transparent background, 16px radius, 24px padding on all sides, no shadow. A common content block with clear boundaries.

### Card, Muted Background

**Role:** Emphasized content container

Muted Desert Dune background (rgba(215, 206, 204, 0.75)), 16px radius, 16px padding on all sides, no shadow. Used for visually distinct sections or featured content.

### Text Badge

**Role:** Labels, tags, status indicators

Transparent background with Canvas White text, no radius, no padding. Used for simple textual labels embedded within dark sections.

### Digital Teal Badge

**Role:** Informational tags, active states

Digital Teal background (rgba(0, 0, 0, 0.1)) with Canvas White text, 999px radius, 4px vertical, 12px horizontal padding. A visually soft badge for metadata.

### Segmented Badge

**Role:** Category labels, filter tags

Light gray background (rgba(243, 244, 246, 0.1)) with Midnight Ink text, 8px radius, 8px vertical, 16px horizontal padding. Used for filtering and categorization.

### Semantic Error Badge

**Role:** Error or warning tags

Digital Teal background (#406e7a) with Midnight Ink text, 2px radius, no padding. This badge stands out for critical information.

## Layout

The page primarily uses a max-width contained layout, though the hero section spans full-bleed with a dramatic, dark photographic background. Sections mainly alternate between a standard white background and a subtle Desert Dune (#d7cecc) or linear gradient background, creating clear visual segmentation. Content is typically arranged in left-aligned stacks or two-column text-left/image-right configurations. Card grids feature heavily, often with 3-column layouts. The overall density is compact but with generous vertical section spacing, ensuring readability while presenting substantial information. Navigation is a sticky top bar with a prominent 'Get started' button.

## Imagery

The visual language features crisp product-focused screenshots of 'digital workers' with human faces, presented in clear, isolated frames. Photography is contained, often with 16px rounded corners, emphasizing the product without lifestyle distractions. Icons are subtle, simple, and monochrome, integrating seamlessly into the UI. Overall, imagery is treated as explanatory content rather than decorative atmosphere, supporting a text-dominant layout with specific visual anchors.

## Dos & Donts

### Do

- Use Midnight Ink (#000000) for all primary text and button backgrounds to maintain high contrast.
- Apply Canvas White (#ffffff) as the primary page background and secondary button fills.
- Always use ES Allianz font with specified weights and letter-spacing for all text elements.
- Implement 999px border-radius for all primary and secondary buttons, and 8px for badges, for a consistently rounded aesthetic.
- Maintain high visual contrast throughout the UI, primarily between Midnight Ink and Canvas White.
- Utilize 16px padding for standard content cards and 24px for more prominent content blocks.
- Incorporate Desert Dune (#d7cecc) for footer backgrounds and subtle section separators to provide a neutral base.

### Don't

- Avoid arbitrary color choices; restrict to the defined palette, using Digital Teal (#406e7a) sparingly for key accents only.
- Do not introduce new font families or weights outside of ES Allianz (400, 500, 700).
- Avoid excessive use of shadows; components should feel lightweight and flat.
- Do not deviate from the established spacing scale (multiples of 8px) to introduce new element or section gaps.
- Disregard strict rounding rules: buttons are 999px, badges 8px, cards 16px.
- Do not place multiple elements too close together; maintain at least an 8px element gap for readability.
- Avoid complex gradients, except for the subtle Terracotta Sunset linear gradient for background transitions.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #000000
accent: #406e7a
primary action: #000000 (filled action)

Example Component Prompts:
1. Create a Primary Filled Button: background Midnight Ink (#000000), text Canvas White (#ffffff), 999px radius, 12px vertical padding, 24px horizontal padding, font ES Allianz 500 weight 16px.
2. Design a Standard Content Card: background Canvas White (#ffffff), 16px radius, 24px padding, Midnight Ink (#000000) borders of 1px solid, font ES Allianz 400 weight 16px for body text.
3. Implement a Digital Teal Badge: background Digital Teal (#406e7a), text Canvas White (#ffffff), 999px radius, 4px vertical padding, 12px horizontal padding, font ES Allianz 400 weight 14px.
4. Build a Ghost Nav Link Button: transparent background, text Midnight Ink (#000000), 0px radius, 8px vertical padding, 14px horizontal padding, font ES Allianz 500 weight 16px.
5. Create a Hero Headline: text Canvas White (#ffffff), font ES Allianz 700 weight 74px, lineHeight 1.1, letterSpacing -0.045em.

---
_Source: https://styles.refero.design/style/850ee61c-4ecd-4558-9c0c-fab99721b34c_
