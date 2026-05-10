# Postevand — Design Reference

> Architectural blueprint on stark white canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://postevand.com](https://postevand.com) |
| Refero page | [https://styles.refero.design/style/76bfda6b-125f-4d9b-96c0-356de1e9fc10](https://styles.refero.design/style/76bfda6b-125f-4d9b-96c0-356de1e9fc10) |
| Theme | light |
| Industry | other |

## Overview

Postevand presents a stark, almost architectural aesthetic using monochrome tones, precise typography, and a grid-based layout. The primary canvas is bright white, often overlaid with fine black lines, creating a blueprint-like impression. Components are flat and boxy, emphasizing content and function over decorative elements, with a notable absence of shadows or rounded corners. The visual system feels grounded and serious, utilizing strong contrast and clear visual hierarchy for a direct and impactful user experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, form input backgrounds, navigation text. Provides a clean, expansive base |
| Midnight Ink | `#000000` | neutral | Primary text, headings, buttons, borders, and navigation elements. Creates strong contrast and visual anchors |
| Whisper Gray | `#f0f1ef` | neutral | Subtle background for secondary sections or cards, offering a slight visual break from pure white without introducing color |
| Ash Slate | `#333333` | neutral | Secondary text, muted borders, and less prominent UI elements. Offers a softer contrast than Midnight Ink while maintaining readability |
| Border Silver | `#d7d7d7` | neutral | Hairline borders for inputs and fine dividers, adding minimal separation |

## Typography

### Nimbus Sans D OT

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 10px, 12px, 16px, 32px, 56px, 80px |
| lineHeight | 1.00, 1.20, 1.40 |
| letterSpacing | -0.0100em |
| substitute | Inter |
| role | Primary typeface for headings, body text, navigation, and button labels. Its clean, geometric form supports the site's structured aesthetic, with a slightly condensed feel at certain sizes. |

### Helvetica

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 13px, 16px, 30px |
| lineHeight | 1.00, 1.54, 1.60 |
| letterSpacing | 0.0060em, 0.0080em |
| substitute | Arial |
| role | Used for input fields and specific body text. Integrates a classic, readable sans-serif for functional text elements, presenting a lighter touch at weight 300. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1 |  |
| body | 16 |  | 1.4 | -0.16 |
| subheading | 32 |  | 1.2 | -0.32 |
| heading | 56 |  | 1.2 | -0.56 |
| display | 80 |  | 1.2 | -0.8 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| all | 0px |

- **elementGap** — 20px
- **sectionGap** — 60px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Primary Filled Button

**Role:** Submit actions, main calls to action.

Solid Midnight Ink background, Canvas White text. No border-radius, 12px vertical padding, 20px horizontal padding. Font: Nimbus Sans D OT, weight 400.

### Ghost Header Button

**Role:** Header links and navigation elements.

Transparent background, Midnight Ink text and a 1px Midnight Ink top border for subtle separation. No border-radius, 0px vertical padding, 20px horizontal padding. Font: Nimbus Sans D OT, weight 400.

### Text Link Button

**Role:** Inline actions and secondary navigation at the bottom of sections.

Transparent background, Midnight Ink text. No padding, no borders, no border-radius. Font: Nimbus Sans D OT, weight 400.

### Default Card

**Role:** Content grouping without visual emphasis.

Transparent background, no borders, no shadows, 0px radius. No internal padding defined, relies on content spacing.

### Subtle Section Card

**Role:** Grouping related content with a slight background distinction.

Whisper Gray background, no borders, no shadows, 0px radius. No internal padding defined, relies on content spacing.

### Elevated Content Card

**Role:** Promising focused content or interactive elements, visually separating from the background.

Canvas White background, no borders, no shadows, 0px radius. Includes 20px padding on all sides. Font: Nimbus Sans D OT.

### Form Input Field

**Role:** User input fields.

Transparent background, Midnight Ink text, Border Silver 1px top border. No radius. 12px vertical padding, 20px horizontal padding. Font: Helvetica, weight 400.

### Newsletter Modal

**Role:** Interstitials for user engagement.

Canvas White background with no borders or shadows, 0px radius. Contains a heading, body copy, an input field (Form Input Field), and a Primary Filled Button.

## Layout

The page structure follows a max-width contained model, centered on the screen, though the hero section can be full-bleed. The hero pattern often involves a large visual (photography) with minimal text overlaid or positioned clearly. Sections maintain a consistent vertical rhythm, primarily using a 60px section gap. Content is arranged in alternating patterns, such as text on one side and imagery/product on the other, or stacked centered blocks. A strong underlying grid is evident through the use of fine 1px Midnight Ink borders, which sometimes define content areas or act as visual dividers. The navigation is a minimalist top bar, likely sticky, with simple text links and a 'Bag' indicator. The overall density is comfortable, with ample white space.

## Imagery

This site predominantly features high-contrast product photography, often tightly cropped or floating against a white background or atmospheric sky. Product images showcase the packaging itself with clean lines and minimal distractions. There are also abstract background images (like blue sky with clouds) used as hero elements, providing atmospheric context. Icons, where present, appear to be outlined and monochromatic (Midnight Ink). Imagery serves to illustrate product details and create a restrained, clean atmosphere, rather than providing extensive lifestyle context.

## Dos & Donts

### Do

- Maintain a strict 0px border-radius across all components to preserve the rectilinear aesthetic.
- Use Midnight Ink (#000000) for all primary text, headings, and borders to ensure strong contrast on Canvas White (#ffffff) backgrounds.
- Utilize Whisper Gray (#f0f1ef) sparingly for subtle background differentiation in secondary content areas, not as a primary accent color.
- Apply 1px borders using Midnight Ink (#000000) for grid lines and key navigation separators, emphasizing structure.
- Prioritize text and button padding of 12px vertical and 20px horizontal to maintain consistent information density.
- Employ Nimbus Sans D OT for all prominent text elements and maintain Helvetica for functional input fields.
- Ensure all interactive elements have a clear visual state (e.g., color change on hover/focus) as elevation is absent.

### Don't

- Do not introduce any rounded corners; maintain hard edges for all elements.
- Avoid using drop shadows or complex elevation techniques; rely on stark borders and background color changes for visual hierarchy.
- Do not introduce additional vibrant colors; stick to the monochrome palette with minimal semantic color usage.
- Never use less than a 20px element gap between major content blocks to preserve a sense of comfortable density.
- Do not use generic system borders for inputs; always specify a Border Silver (#d7d7d7) 1px border at the top.
- Avoid decorative imagery that competes with the strict grid and monochrome aesthetic; imagery should be contained or stark.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #000000
- background: #ffffff
- border: #000000
- accent: no distinct accent color
- primary action: no distinct CTA color

Example Component Prompts:
- Create a 'Subscribe' button: #000000 background, #ffffff text, 0px radius, 12px 20px padding, Nimbus Sans D OT font, weight 400.
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
- Create a product card: #ffffff background, 20px padding all sides, 0px radius, no shadow. This card should contain a headline (Nimbus Sans D OT, 32px, lh 1.2), a product image, and a Text Link Button.

---
_Source: https://styles.refero.design/style/76bfda6b-125f-4d9b-96c0-356de1e9fc10_
