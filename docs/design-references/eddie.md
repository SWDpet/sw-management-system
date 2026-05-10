# Eddie — Design Reference

> Playful tech-futurism on a clean canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://eddie.eco](https://eddie.eco) |
| Refero page | [https://styles.refero.design/style/5f446754-1fb7-4a99-8cee-7bd5de3bfd9e](https://styles.refero.design/style/5f446754-1fb7-4a99-8cee-7bd5de3bfd9e) |
| Theme | light |
| Industry | other |

## Overview

Eddie's design system creates a playful yet authoritative feel, blending a clean, energetic achromatic base with vibrant purple and lime green accents. The visual language centers on bold, compact typography and soft, rounded elements. Lightly elevated cards with subtle purple-tinted shadows add depth, while high-contrast text ensures legibility. The overall effect is efficient and approachable, emphasizing a modern tech aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink | `#000000` | neutral | Primary text, darkest surface elements, filled buttons, structural borders that define active areas |
| Canvas | `#ffffff` | neutral | Page backgrounds, card backgrounds, text on dark interactive elements |
| Pampas | `#f7f4f2` | neutral | Secondary surface background, typically for cards and list items |
| French Gray | `#cdcbd0` | neutral | Decorative borders, inactive link underlines, subtle dividers |
| Scarpa Flow | `#4d4c51` | neutral | Muted text, body copy on lighter backgrounds, ghost button borders |
| Lilac Haze | `#d8d1ff` | accent | Accent backgrounds for badges and cards, soft decorative fills |
| Vibrant Lime | `#d2e534` | accent | Energetic marketing section backgrounds, accent elements, highlight badges |
| Dull Lavender | `#b7b2ff` | accent | Violet state accent for badges, validation surfaces, and short status labels. Do not promote it to the primary CTA color |
| Aqua Island | `#89cdf3` | semantic | Informational badges, specific accent backgrounds; provides a cool contrast to the dominant purple |
| Whisper Glow | `#eeeafa` | neutral | Subtle shadow tint for elevated components, contributing to the soft, purple-hued depth |

## Typography

### Founders Grotesk

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| sizes | 14px, 16px, 18px, 22px, 26px, 30px, 66px, 67px, 119px |
| lineHeight | 0.70, 0.80, 1.00, 1.10, 1.30 |
| letterSpacing | normal |
| substitute | Inter |
| role | All text elements, including headings, body copy, navigation, and interactive component text. Its compact, confident appearance defines the brand's voice. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.3 |  |
| body-sm | 16 |  | 1.3 |  |
| body | 18 |  | 1.3 |  |
| subheading | 22 |  | 1.1 |  |
| heading-sm | 26 |  | 1.1 |  |
| heading | 30 |  | 1.1 |  |
| heading-lg | 66 |  | 0.8 |  |
| display | 119 |  | 0.7 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 40px |
| badges | 30px |
| buttons | 25px |
| interactiveElements | 15px |

- **elementGap** — 10px
- **sectionGap** — 100px
- **cardPadding** — 15px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#ffffff` | 0 | Primary page background. |
| Pampas | `#f7f4f2` | 1 | Secondary background for card components and specific content blocks. |
| Lilac Haze | `#d8d1ff` | 2 | Elevated accent cards or key information sections, often with larger radius. |
| Vibrant Lime | `#d2e534` | 3 | High-impact feature sections, often full-bleed, for visual demarcation and energy. |

## Components

### Filled Primary Button

**Role:** Main call to action, high importance.

Background: Ink (#000000). Text: Canvas (#ffffff). Border radius: 25px. Padding: 10px vertical, 20px horizontal. Shadow: rgba(198, 185, 237, 0.3) 5px 5px 20px 0px.

### Ghost Accent Button

**Role:** Secondary action that needs to stand out without being a primary CTA. Used in navigation or feature highlights.

Background: Canvas (#ffffff). Text: Ink (#000000). Border radius: 25px. Padding: 15px vertically and horizontally. Border: 1px solid Ink (#000000).

### Pampas Card with Soft Shadow

**Role:** Feature blocks or information containers requiring subtle elevation.

Background: Pampas (#f7f4f2). Border radius: 40px. Shadow: rgba(198, 185, 237, 0.3) 5px 5px 20px 0px. No internal padding specified from analysis.

### Lilac Haze Feature Card

**Role:** Prominent information card, often acting as a section header or key highlight.

Background: Lilac Haze (#d8d1ff). Border radius: 60px. Padding: 30px vertically and horizontally. No explicit shadow.

### Subtle Badge

**Role:** Categorization or short status labels.

Background: transparent. Text: Ink (#000000). No border radius set (0px). Minimal bottom padding of 0.8px.

### Accent Tag Badge

**Role:** Highlighting keywords, categories, or functional states.

Background: Dull Lavender (#b7b2ff). Text: Ink (#000000). Border Radius: 30px. Padding: 5px vertical, 10px horizontal.

### Info Tag Badge

**Role:** Indicating informational status or categories.

Background: Aqua Island (#89cdf3). Text: Ink (#000000). Border Radius: 30px. Padding: 5px vertical, 10px horizontal.

## Layout

The page structure is primarily contained with a maximum width, although the hero section appears full-bleed or extends wider. The hero features a centered headline and interactive elements. Sections often alternate between full-width content and structured 2-column or card grid layouts. Vertical spacing between sections is generous, creating distinct content blocks, but internal component density is compact. The navigation is a sticky top bar with a hidden mega-menu triggered by a hamburger icon on smaller screens or a more explicit 'Ça m'intéresse' button. Content is arranged with a clear hierarchy; alternating text-left/image-right is not a dominant pattern, rather emphasis is on distinct content blocks.

## Imagery

The imagery leans towards product-focused illustrations and subtle abstract graphics. Illustrations are dimensional and often incorporate the brand's purple and blue-green accent colors, depicting technology or smart devices in a clean, friendly style. These visuals are typically contained within rounded cards or sections, not full-bleed. Icons are primarily solid, in Ink (#000000) or accent colors, with a medium stroke weight where outlines are present, serving functional and decorative roles. The overall density of visuals is moderate, blending with text-heavy sections seamlessly.

## Dos & Donts

### Do

- Prioritize Founders Grotesk for all text at varying weights and sizes, ensuring consistent brand voice.
- Utilize Ink (#000000) for primary text and critical UI elements to maintain high contrast and legibility.
- Use Canvas (#ffffff) or Pampas (#f7f4f2) as dominant background colors for most sections.
- Apply a 25px border radius to all interactive buttons for a soft, approachable feel.
- Accent key sections or interactive elements with Lilac Haze (#d8d1ff), Vibrant Lime (#d2e534), or Dull Lavender (#b7b2ff).
- Employ the rgba(198, 185, 237, 0.3) 5px 5px 20px 0px shadow for cards and buttons that require subtle elevation.
- Maintain a compact density with 10px element gaps for most internal component spacing.

### Don't

- Avoid using highly saturated, primary colors for large background areas unless it is specifically one of the defined accent colors.
- Do not deviate from the Founders Grotesk typeface; introducing other fonts will compromise the brand's typographic identity.
- Never use sharp corners; adhere to the specified border radii for all elements, particularly 25px for buttons and 40px for cards.
- Refrain from heavy, dark shadows; the subtle, purple-tinted shadow is a core part of its visual language.
- Do not introduce complex gradients for UI elements; the system tends towards solid fills or very subtle accent gradients.
- Avoid excessive spacing that creates 'airy' layouts; the density is compact and efficient.
- Do not use multiple accent colors simultaneously in proximity unless they are part of a deliberately designed badge or illustration.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #4d4c51
accent: #d8d1ff
primary action: #000000 (filled action)

Example Component Prompts:
1. Create a Primary CTA Button: Background Ink (#000000), text Canvas (#ffffff), 25px border radius, 10px vertical and 20px horizontal padding, with rgba(198, 185, 237, 0.3) 5px 5px 20px 0px shadow.
2. Design a Feature Card: Lilac Haze (#d8d1ff) background, 60px border radius, 30px vertical and horizontal padding, with Founders Grotesk 600 weight text at 26px and Ink (#000000) color.
3. Implement a Ghost Button: Canvas (#ffffff) background, text Ink (#000000), 25px border radius, 15px vertical and horizontal padding, with a 1px solid Ink (#000000) border.
4. Create an Accent Badge: Dull Lavender (#b7b2ff) background, text Ink (#000000), 30px border radius, 5px vertical and 10px horizontal padding, using Founders Grotesk 400 weight at 14px.
5. Create a Primary Action Button: #000000 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

---
_Source: https://styles.refero.design/style/5f446754-1fb7-4a99-8cee-7bd5de3bfd9e_
