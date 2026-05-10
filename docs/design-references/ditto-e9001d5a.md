# Ditto — Design Reference

> Parchment and Ink with Marigold Accents

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://trustditto.com](https://trustditto.com) |
| Refero page | [https://styles.refero.design/style/e9001d5a-504d-47ed-aef0-d0d35fa86418](https://styles.refero.design/style/e9001d5a-504d-47ed-aef0-d0d35fa86418) |
| Theme | light |
| Industry | saas |

## Overview

Ditto presents a grounded yet vibrant aesthetic, combining rich, deep purple-blue with a sunny yellow accent and a muted, almost parchment-like background. The visual system balances seriousness with approachability, featuring soft, rounded corners and a dense, information-rich layout. Typography leans towards a strong, modern sans-serif for functional elements and a traditional serif for impactful headlines, creating a subtle tension between heritage and forward-thinking. Color is used functionally, with neutral backgrounds forming a canvas for clear hierarchy and a bold accent for interactive elements.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#130e30` | brand | Primary text, deep surface elements, button backgrounds for secondary actions, interactive states — creates visual depth and anchors content |
| Marigold Burst | `#ffe228` | accent | Primary action buttons, accent details, interactive highlights — a vivid, warm contrast against neutrals and dark tones, signaling key interactions |
| Frosted Sage | `#eff2e5` | neutral | Card backgrounds, section separators, input fields — a soft, off-white neutral that adds warmth and texture as a primary surface |
| Paper White | `#f9fbf2` | neutral | Page background, light button backgrounds, light surface elements — provides a clean, bright canvas |
| Graphite | `#222222` | neutral | Secondary text, neutral button borders, active navigation text — provides strong contrast while remaining slightly softer than pure black |
| Subtle Ash | `#5f5c6e` | neutral | Muted text, helper text, subtle borders, inactive icon fills — a mid-tone gray for subtle visual separation and less prominent information |
| Pure Black | `#000000` | neutral | Input borders, separator lines where strong definition is needed — used sparingly for sharp edges |
| Vivid Shamrock | `#59e25d` | accent | Green decorative accent for icons, marks, and small graphic details. Use as a supporting accent, not as a status color |
| Electric Fuchsia | `#e261e5` | accent | Decorative accent for illustrations, highlight elements where strong visual interest is required |
| Sky Blue | `#3a93ff` | accent | Blue decorative accent for icons, marks, and small graphic details. Do not promote it to the primary CTA color |

## Typography

### Hedvig Letters Serif

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 22px, 32px, 48px, 64px |
| lineHeight | 1.00, 1.10, 1.15, 1.20, 1.25 |
| letterSpacing | -0.0100em |
| substitute | Georgia |
| role | Headlines and prominent display text — the serif style at larger sizes and subtle negative letter-spacing creates a sense of established authority and refined impact. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 10px, 14px, 16px, 18px, 22px |
| lineHeight | 1.20, 1.25, 1.50 |
| letterSpacing | -0.0200em at 22px, -0.0100em at 18px |
| substitute | system-ui |
| role | Body copy, navigation, buttons, and all functional UI elements — its neutral, highly legible character ensures clear information delivery across various content densities. Tighter tracking for larger sizes provides a compact, modern feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0 |
| body | 14 |  | 1.5 | 0 |
| body-lg | 16 |  | 1.5 | 0 |
| subheading | 18 |  | 1.25 | -0.18 |
| heading-sm | 22 |  | 1.25 | -0.44 |
| heading | 32 |  | 1.15 | -0.32 |
| heading-lg | 48 |  | 1.1 | -0.48 |
| display | 64 |  | 1 | -0.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| images | 24px |
| avatars | 1440px |
| buttons | 1440px |

- **elementGap** — 12px
- **sectionGap** — 48px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Canvas | `#f9fbf2` | 1 | The primary background, clean and bright, for the overall layout. |
| Content Card | `#eff2e5` | 2 | Slightly darker, warmer background for grouped content like cards and sections, providing subtle visual layering without elevation. |
| Prominent Accent | `#ffe228` | 3 | Used for primary interactive elements, standing out as a focal point. |
| Deep Surface | `#130e30` | 4 | Used for dark-themed components or sections, providing strong contrast and visual gravity. |

## Components

### Primary Action Button

**Role:** Call to action

Filled with Marigold Burst (#ffe228) background, Midnight Ink (#130e30) text, 1440px border-radius, 12px vertical padding, 22px horizontal padding. Uses Inter font.

### Secondary Filled Button

**Role:** Alternative action

Filled with Midnight Ink (#130e30) background, Paper White (#f9fbf2) text, 1440px border-radius, 12px vertical padding, 22px horizontal padding. Uses Inter font.

### Ghost Button

**Role:** Navigation, tertiary actions

Transparent background, Graphite (#222222) text and 1px border. 1440px border-radius, 14.08px vertical padding, 24px horizontal padding. Uses Inter font.

### Content Card

**Role:** Information container

Frosted Sage (#eff2e5) background, 24px border-radius, 24px padding on all sides. No shadow, creating a flat, layered appearance.

### Input Field

**Role:** User input

Transparent background, Midnight Ink (#130e30) text, 0px border-radius, with Pure Black (#000000) bottom border for emphasis. 0px vertical padding, 16px horizontal padding. Uses Inter font.

### Compact Button

**Role:** Within forms or small actions

Paper White (#f9fbf2) background, Midnight Ink (#130e30) text, 1440px border-radius, 1px vertical padding, 6px left padding, 12px right padding. Used for combined elements like input with an appended button.

## Layout

The page employs a max-width contained layout, likely around 1200px, with content centered. The hero section features a left-aligned, prominent headline and subtext, contrasted with a large, illustrative product screenshot on the right. Below the hero, sections alternate between a centered headline with supportive text and grids of content cards. There's a clear use of 3-column card grids for features and testimonials. Distinct, comfortable vertical spacing (48px section gap) creates a readable rhythm between blocks, while cards within grids maintain a consistent 24px padding. Navigation is a top bar, with 'Log in' and 'Get Started' buttons prominently displayed, indicating key user journeys.

## Imagery

The imagery strategy heavily features technical product screenshots, depicting dashboards and interfaces, often presented within a device mockup or floating freely on a textured background. These product visuals are clean, detailed, and serve an explanatory content role. When present, decorative elements are abstract, geometric shapes in brand accent colors (Electric Fuchsia, Sky Blue, Vivid Shamrock), often organic or flowing, and used as background embellishments. Photography is sparse, appearing mostly in testimonials as small, contained circular profile pictures. Icons are outlined, fine-lined, and monochrome, often using the Subtle Ash or Midnight Ink color, conveying a lightweight and modern feel. Overall, the visual density is moderate, with product imagery carrying significant informational weight.

## Dos & Donts

### Do

- Use Midnight Ink (#130e30) for all primary text and main headlines to establish visual weight.
- Apply Marigold Burst (#ffe228) exclusively for primary call-to-action buttons and key interactive highlights, emphasizing its role as the principal accent.
- Maintain a default border-radius of 1440px for all buttons and interactive elements, conveying a soft, approachable feel.
- Structure information using Frosted Sage (#eff2e5) as a background for grouped content like cards, distinct from the Paper White (#f9fbf2) page background.
- Pair Hedvig Letters Serif for all significant headlines (H1-H3) with generous line heights (1.1-1.15) to enhance its distinguished presence.
- Utilize Inter at 14px (weight 400 or 500) for body text with a lineHeight of 1.50 for optimal readability.
- Ensure a consistent 24px padding for all content cards, establishing a clean, structured internal rhythm.

### Don't

- Do not introduce new saturated primary colors; limit the palette to Midnight Ink, Marigold Burst, and the associated decorative accents.
- Avoid applying shadows or heavy borders to cards; maintain a flat, layered surface aesthetic using background colors alone.
- Do not deviate from the specified negative letter-spacing for Hedvig Letters Serif headlines; a tighter track is part of its character.
- Do not use Marigold Burst (#ffe228) for anything other than explicit interactive calls to action or small, intentional highlights, to preserve its impact.
- Do not use generic system fonts when Inter is available; Inter provides the specific modern and legible tone required.
- Do not create buttons with hard, square corners; all buttons must use the 1440px radius for consistency.
- Avoid dense text blocks by ensuring adequate line heights, particularly for Inter body text, and consistent element gaps of 12px.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #130e30
background: #f9fbf2
border: #000000
accent: #ffe228
primary action: #ffe228 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #ffe228 background, #222222 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a content card: Frosted Sage (#eff2e5) background, 24px border-radius, 24px padding. Contains a Hedvig Letters Serif headline at 32px, weight 700, Midnight Ink (#130e30) color, and Inter body text at 14px, weight 400, Midnight Ink (#130e30) color.
3. Create a navigation ghost button: Transparent background, Graphite (#222222) text and 1px border. 1440px border-radius, 14.08px vertical padding, 24px horizontal padding. Text is Inter font, weight 500, size 16px.
4. Create an input field: Transparent background, 0px border-radius, 0px vertical padding, 16px horizontal padding. Has a Pure Black (#000000) bottom border. Placeholder text in Subtle Ash (#5f5c6e), input text in Midnight Ink (#130e30). Uses Inter font, weight 400, size 16px.

---
_Source: https://styles.refero.design/style/e9001d5a-504d-47ed-aef0-d0d35fa86418_
