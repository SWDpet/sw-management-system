# Voiceflow — Design Reference

> AI Blueprint on White Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.voiceflow.com](https://www.voiceflow.com) |
| Refero page | [https://styles.refero.design/style/03b3d707-2a30-4f53-a524-347d1b70eb2c](https://styles.refero.design/style/03b3d707-2a30-4f53-a524-347d1b70eb2c) |
| Theme | light |
| Industry | ai |

## Overview

Voiceflow presents a light and airy interface, built on a spacious canvas of off-white and crisp backgrounds, punctuated by a single vibrant blue for primary actions. Typography is precise and clear, with a preference for slightly condensed sans-serifs for body text and a light, elegant serif for dramatic headlines. Cards and elements feature large, comfortable padding and rounded corners, creating a friendly yet professional feel. The overall impression is one of confident, approachable AI technology, with visual complexity reserved for product demonstrations.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Skybound Blue | `#397dff` | brand | Primary Call-to-Action buttons and interactive elements, representing activation and core functionality |
| Amber Pop | `#f55c15` | accent | Accent for badges, status indicators, and subtle highlights — a burst of energy to draw attention to key information |
| Impact Red | `#ff0000` | accent | Red outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Ultramarine | `#1956f3` | accent | A deeper, more saturated blue used sparingly for large background fills, providing a dramatic accent |
| Inkwell | `#171717` | neutral | Primary heading text and critical information, providing strong contrast on light backgrounds |
| Deep Graphite | `#262626` | neutral | Secondary text, dark backgrounds for subtle distinctions, and focused elements like secondary buttons |
| Anchor Gray | `#333333` | neutral | Muted text, borders, and dividers where a softer presence is desired than Deep Graphite |
| Slate Text | `#525252` | neutral | Body copy and standard informational text, ensuring readability without being overly stark |
| Quiet Stone | `#737373` | neutral | Helper text, subtle borders, and placeholder content. Creates a whisper of presence |
| Silver Thread | `#a1a1a1` | neutral | Hairline borders, disabled states, and very subtle UI elements, offering minimum contrast; Background for transparent UI elements, providing a frosted effect |
| Platinum Mist | `#d4d4d4` | neutral | Light borders, inactive states, and structural dividers that define content regions without drawing focus |
| Canvas White | `#ffffff` | neutral | Primary page backgrounds, card surfaces, and text on dark backgrounds, providing a clean default surface |
| Whisper White | `#f5f5f4` | neutral | Subtle background for UI elements or sections to provide a minimal visual break from Canvas White |
| Light Cloud | `#edeeee` | neutral | Alternate background for sections or secondary surface treatments, providing a slightly darker canvas than Whisper White |
| Border Ash | `#e5e5e5` | neutral | Default borders and subtle separators for cards and input fields. Clearly defines boundaries |

## Typography

### Tiempos Headline

| Key | Value |
| --- | --- |
| weight | 300 |
| weights | 300 |
| sizes | 20px, 36px, 40px, 48px, 56px, 64px |
| lineHeight | 1.00, 1.13, 1.14, 1.16, 1.20, 1.30 |
| letterSpacing | -0.0400em for larger sizes like 56px, tapering to -0.0170em at 20px |
| substitute | Playfair Display |
| role | Primary display and section headings. The light weight (300) conveys authority through restraint, preferring elegance over shouting. Used for the prominent product name and key messages. |

### Selecta

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 8px, 13px, 14px, 15px, 16px, 18px |
| lineHeight | 0.80, 1.00, 1.16, 1.38, 1.44, 1.47, 1.50, 1.55, 1.75 |
| letterSpacing | Ranges from 0.0400em at 8px to 0.0180em at 18px |
| substitute | Inter |
| role | Body copy, UI labels, buttons, navigation, and detailed information. Its slightly condensed forms maintain a compact yet readable presence across the interface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-lg | 16 |  | 1.47 | 0.25 |
| subheading | 20 |  | 1.3 | -0.34 |
| heading | 36 |  | 1.14 | -0.72 |
| heading-lg | 48 |  | 1.13 | -0.96 |
| display | 56 |  | 1.16 | -1.68 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 100px |
| cards | 20px |
| buttons | 999px |
| productImageCard | 14px |

- **elementGap** — 24px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Primary page background and default container surface. |
| Whisper White | `#f5f5f4` | 2 | Subtle secondary background for alternate sections or elevated UI elements. |
| Light Cloud | `#edeeee` | 3 | Tertiary background for visually distinct sections or more pronounced surface variations. |
| Frosted Glass | `#a1a1a180` | 4 | Semi-transparent overlay or background for modal-like components, utilizing a backdrop blur. |

## Components

### Primary Action Button

**Role:** Main call to action

Filled button with Skybound Blue (#397dff) background, Canvas White (#ffffff) text, and a 999px border-radius for a soft, pill-like shape. Padding is generous: 10px vertical, 20px horizontal.

### Secondary Ghost Button

**Role:** Alternative action

Ghost button with transparent background, Quiet Stone (#737373) text and border, featuring a 999px border-radius. Padding is 10px vertical, 20px horizontal, matching the primary action for size parity but reduced visual weight.

### Small Dark Tag

**Role:** Informational tag

Used for quick labels, with Deep Graphite (#262626) background and Canvas White (#ffffff) text. Features minimal 2px vertical, 4px horizontal padding, and a 999px border-radius.

### Content Card

**Role:** Container for information

Background is Canvas White (#ffffff) with 20px border-radius. Features 24px internal padding for comfortable content separation. No default shadow for a light aesthetic.

### Product Feature Card

**Role:** Showcasing product screens

Transparent background with a distinct 14px border-radius (top-right corner), sometimes featuring a subtle blur effect on its backdrop. Used for displaying product UI snippets.

### Informational Badge

**Role:** Highlighting keywords or categories

Transparent background with Amber Pop (#f55c15) text, no padding. Used for concise, high-visibility labels like 'Omnichannel Support'.

## Layout

The page uses a maximum-width contained layout, likely around 1200px, centered on a Canvas White background. The hero section features a centered, visually strong headline on a white canvas above a subtle full-width gradient or image background, followed by prominent calls-to-action. Content sections alternate between single-column centered text blocks, two-column text-left/image-right or image-left/text-right arrangements. There is a distinct three-column card grid used for features and testimonials. Vertical spacing between sections is generous and consistent, creating comfortable reading rhythm. The navigation is a sticky top bar, providing persistent access to key links and calls to action.

## Imagery

The visual language predominantly features product screenshots and UI mockups, often presented on transparent or semi-transparent backgrounds. These are used to explain functionality and showcase the product directly, rather than relying on abstract concepts or lifestyle photography. When photography is used, it's typically environmental (e.g., aerial views of landscapes) as a background for text or product displays, or professional headshots for social proof. Icons are minimal, outlined, and mostly monochrome, reinforcing a focus on UI clarity. The density of imagery is balanced, with product shots interspersed with text-heavy sections to explain features.

## Dos & Donts

### Do

- Prioritize Canvas White (#ffffff) for primary backgrounds and Content Card surfaces, maintaining a bright and open feel.
- Use Skybound Blue (#397dff) exclusively for primary interactive elements, ensuring all core actions are distinctly colored.
- Apply a 999px border-radius to all buttons and tags, creating a consistent, friendly pill shape.
- Employ Tiempos Headline (weight 300) for all major headings to convey quiet authority and elegance.
- Use 24px as the primary value for `elementGap` and `cardPadding` to ensure a consistent, comfortable density.
- Define UI borders and dividers using Platinum Mist (#d4d4d4) for soft separation, or Quiet Stone (#737373) for slightly more emphasis.
- Allow imagery, especially product mockups, to feature transparent or semi-transparent backgrounds to integrate seamlessly with the light canvas.

### Don't

- Avoid using saturated colors in large blocks unless specifically for brand accents like Ultramarine (#1956f3).
- Do not introduce heavy shadows or gradients; maintain the light and airy surface treatment.
- Refrain from using Tiempos Headline for body text or small labels; reserve it for large display typography.
- Do not deviate from the established border-radius values (999px, 20px, 14px) for interactive elements and cards.
- Avoid tightly packed content where `elementGap` is less than 8px; ensure generous breathing room.
- Do not use multiple chromatic colors for primary calls to action; Skybound Blue (#397dff) is the sole accent for interactive elements.
- Never use generic system fonts; always specify 'Selecta' for body and UI, and 'Tiempos Headline' for headings.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #171717 (Inkwell)
- background: #ffffff (Canvas White)
- border: #e5e5e5 (Border Ash)
- accent: #f55c15 (Amber Pop)
- primary action: #397dff (filled action)

### 3-5 Example Component Prompts
1. Create a Primary Action Button: #397dff background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a `content card`: Canvas White (#ffffff) background, 20px border-radius, with 24px internal padding. Display body text at 14px Selecta weight 400, Slate Text (#525252). Highlight a keyword using an Informational Badge with transparent background, Amber Pop (#f55c15) text, no padding.
3. Create a `product feature section`: Light Cloud (#edeeee) background. Two columns: left is a heading at 36px Tiempos Headline weight 300, Inkwell (#171717), letter-spacing -0.72px. Right is an image contained within a Product Feature Card with a 14px top-right border-radius, transparent background, and backdrop-filter blur(24px).

---
_Source: https://styles.refero.design/style/03b3d707-2a30-4f53-a524-347d1b70eb2c_
