# Super — Design Reference

> Black text on parchment-white, illuminated by a single amber light.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://super.so](https://super.so) |
| Refero page | [https://styles.refero.design/style/25863c92-a287-491e-bae7-4da37a1f9a98](https://styles.refero.design/style/25863c92-a287-491e-bae7-4da37a1f9a98) |
| Theme | mixed |
| Industry | saas |

## Overview

This system projects an image of focused utility and quiet confidence, using a stark black-on-white text, highlighted by a single vibrant yellow accent. The design leans into sharp contrasts and subtle textural variations in its neutral palette, allowing content to take center stage without visual noise. Generous padding around elements creates a sense of airiness, emphasizing the digital-first nature of the product.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Amber Glow | `#ffbe3c` | brand | CTA buttons, active navigation indicators — a warm, inviting beacon against the muted interface. |
| Obsidian | `#111111` | neutral | Headline text, primary button backgrounds, navigation text — serves as the primary dark element. |
| Charcoal Black | `#262a2` | neutral | Primary body text, card backgrounds in dark mode, significant elements across the UI. |
| Warm Gray | `#37352f` | neutral | Secondary text, borders, and subtle background elements for visual separation. |
| Cool Stone | `#4d545c` | neutral | Muted text for inactive states or less prominent information, like button text on light backgrounds. |
| Dusk Gray | `#66707a` | neutral | Supportive body text, footnotes, and descriptive copy where full contrast isn't required. |
| Pale Ash | `#808c99` | neutral | Tertiary text, subtle borders, and placeholder text, indicating secondary information. |
| Light Mist | `#ccd1d6` | neutral | Light borders, dividers, and background elements, providing subtle structure. |
| Frosted Linen | `#f9f9f8` | neutral | Base background for light-themed sections, offering a soft white canvas. |
| Pure White | `#ffffff` | neutral | Card backgrounds, navigation bar background, general UI surfaces requiring maximum luminosity. |
| Midnight Void | `#090909` | neutral | Used for the dark footer background, providing a deep, grounding anchor to the page. |

## Typography

### Beausite

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 12px, 14px, 16px, 18px, 20px, 26px, 38px, 64px |
| lineHeight | 1.00, 1.07, 1.10, 1.13, 1.20, 1.30, 1.33, 1.50, 1.60, 1.71 |
| letterSpacing | -0.0390em at 64px, -0.0310em at 38px, -0.0290em at 26px, -0.0260em at 20px |
| substitute | Inter |
| role | The primary typeface for all text elements. The extensive weight range allows for precise typographic hierarchy, while the subtly negative letter-spacing for larger sizes gives headlines a polished, composed appearance that feels intentional rather than just 'big'. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.6 |  |
| body-sm | 14 |  | 1.6 |  |
| body | 16 |  | 1.6 |  |
| subheading | 18 |  | 1.5 | -0.28 |
| heading-sm | 20 |  | 1.33 | -0.52 |
| heading | 26 |  | 1.2 | -0.75 |
| heading-lg | 38 |  | 1.13 | -1.2 |
| display | 64 |  | 1 | -2.5 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| badges | 70px |
| buttons | 8px |
| general | 4px |

- **elementGap** — 4-16px
- **sectionGap** — 45-96px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Trusted By Logos Banner

### How It Works — Dark Feature Cards

### Primary Action Button

**Role:** Calls to action

Background: Amber Glow (#ffbe3c), Text: Obsidian (#111111), Border: 1px solid Obsidian, Radius: 8px, Padding: 13px top, 16px right, 14px bottom, 16px left.

### Outline Ghost Button

**Role:** Secondary actions

Background: Pure White (#ffffff), Text: Cool Stone (#4d545c), Border: 1px solid Frosted Linen (#f2f4f5), Radius: 8px, Padding: 13px top, 16px right, 14px bottom, 16px left.

### Subtle Link Button

**Role:** Tertiary actions, inline links acting as buttons

Background: Pure White (#ffffff), Text: Charcoal Black (#262a2e), Border: 1px solid Charcoal Black, Radius: 8px, Padding: 13px top, 16px right, 14px bottom, 16px left.

### Dark Mode Button

**Role:** Actions within dark sections

Background: Obsidian (#111111), Text: Pure White (#ffffff), Border: 1px solid Pure White, Radius: 8px, Padding: 13px top, 16px right, 14px bottom, 16px left.

### Feature Card - Light

**Role:** Showcasing product features or benefits in a grid

Background: Pure White (#ffffff), Border: none, Radius: 8px, Shadow: rgba(0, 0, 0, 0.008) 2.6px 4px 2.6px 0px, rgba(0, 0, 0, 0.01) 7.3px 11px 7.1px 0px, rgba(0, 0, 0, 0.01) 17.5px 26.5px 17.2px 0px, rgba(0, 0, 0, 0.02) 58px 88px 57px 0px, Padding: 0px. Internal content determines padding.

### Feature Card - Dark

**Role:** Showcasing product features or benefits in a grid within dark sections

Background: Charcoal Black (#33383d), Border: none, Radius: 8px, Shadow: rgba(0, 0, 0, 0.008) 2.6px 4px 2.6px 0px, rgba(0, 0, 0, 0.01) 7.3px 11px 7.1px 0px, rgba(0, 0, 0, 0.01) 17.5px 26.5px 17.2px 0px, rgba(0, 0, 0, 0.02) 58px 88px 57px 0px, Padding: 0px. Internal content determines padding.

### Circular Badge

**Role:** Small informational labels or status indicators

Background: Frosted Linen (#fdfdfc - from hint), Text: #0000EE (browser default link color, should be mapped to a brand color if intended for links), Radius: 70px, Padding: 8px vertical, 12px horizontal.

## Layout

The layout primarily adheres to a centered maximum width (implied, not explicit in data for pageMaxWidth but evident in visual alignment). The hero section is full-width, utilizing a soft, abstract background while anchoring content centrally. Sections alternate between light (Frosted Linen / Pure White) and dark (Midnight Void / Charcoal Black) backgrounds, creating a clear vertical rhythm and delineation of content blocks. Elements within sections often adopt multi-column layouts, such as two-column text-and-image, or grid-based card displays for features. Ample vertical spacing is used between sections, ensuring breathability and reducing perceived density. Navigation is a sticky top bar with a primary CTA.

## Imagery

The site uses a combination of abstract, subtle background graphics in the hero that appear almost like frosted glass, and prominent product screenshots. Product screenshots are contained within device mockups or card-like structures, showcasing the UI in a practical context. Logos for 'Trusted by teams at' are monochrome (Obsidian or Charcoal Black) ensuring they integrate seamlessly. Icons are minimal, likely filled, and black, maintaining functional clarity. The overall impression is content-heavy but balanced with clear visual examples, focusing on product function rather than decorative imagery.

## Dos & Donts

### Do

- Prioritize Charcoal Black (#262a2e) for all primary body text, ensuring high contrast on light backgrounds.
- Use Amber Glow (#ffbe3c) exclusively for primary call-to-action buttons and active state indicators.
- Apply 8px corner radius consistently to all cards and buttons, promoting a unified soft-edged appearance.
- Maintain a clear visual hierarchy by utilizing Beausite's varied weights and sizes, especially -0.0390em letter spacing for 64px headlines.
- Implement the card shadow `rgba(0, 0, 0, 0.02) 58px 88px 57px 0px` for all elevated content cards to provide subtle depth.
- Employ Frosted Linen (#f9f9f8) or Pure White (#ffffff) as the dominant background colors for all light-themed content sections.

### Don't

- Do not introduce new saturated colors; maintain Amber Glow (#ffbe3c) as the sole accent.
- Avoid using flat shadows; instead, apply the multi-layer progressive shadow for all elevated components.
- Do not deviate from the specified padding for buttons (e.g., 13px top, 16px horizontal, 14px bottom) to maintain consistent click targets.
- Do not use letter spacing greater than 0; the inherent negative letter-spacing for larger type sizes is a brand characteristic.
- Do not use default browser link colors like #0000EE for styled elements; map to an appropriate brand or neutral color if links are intended to be interactive secondary elements.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: Charcoal Black (#262a2e)
- Background: Frosted Linen (#f9f9f8)
- CTA: Amber Glow (#ffbe3c)
- Border: Light Mist (#ccd1d6)
- Accent: Amber Glow (#ffbe3c)

### Example Component Prompts
1. Create a Primary Action Button: Background: #ffbe3c, Text: #111111, Border: 1px solid #111111, Radius: 8px, Padding: 13px 16px 14px 16px. Text font: Beausite, weight 500, size 16px, line-height 1.6.
2. Generate a Feature Card - Light: Background: #ffffff, Radius: 8px, Shadow: rgba(0, 0, 0, 0.008) 2.6px 4px 2.6px 0px, rgba(0, 0, 0, 0.01) 7.3px 11px 7.1px 0px, rgba(0, 0, 0, 0.01) 17.5px 26.5px 17.2px 0px, rgba(0, 0, 0, 0.02) 58px 88px 57px 0px. Content inside with 20px bottom margin for elements, heading font Beausite weight 600, size 26px, letter-spacing -0.75px. Body text Beausite weight 400, size 16px, line-height 1.6.
3. Design a Hero section: Background: Frosted Linen (#f9f9f8). Centered headline text: 'The #1 tool for turning a Notion document into a website' in Charcoal Black (#262a2e), Beausite font weight 600, size 64px, line-height 1.0, letter-spacing -2.5px. Subtext: 'Transform your Notion pages...' in Dusk Gray (#66707a), Beausite font weight 400, size 18px, line-height 1.5, letter-spacing -0.28px. Below subtext, add a Primary Action Button with 'Get started for free' label.
4. Create a Dark Mode Feature Card: Background: #33383d, Radius: 8px, Shadow: rgba(0, 0, 0, 0.008) 2.6px 4px 2.6px 0px, rgba(0, 0, 0, 0.01) 7.3px 11px 7.1px 0px, rgba(0, 0, 0, 0.01) 17.5px 26.5px 17.2px 0px, rgba(0, 0, 0, 0.02) 58px 88px 57px 0px. Internal text should be Pure White (#ffffff) for headlines and Light Mist (#ccd1d6) for body text.

---
_Source: https://styles.refero.design/style/25863c92-a287-491e-bae7-4da37a1f9a98_
