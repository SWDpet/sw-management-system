# Clutch Security — Design Reference

> Minimalist digital blueprint

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.clutch.security](https://www.clutch.security) |
| Refero page | [https://styles.refero.design/style/802bdc0e-ec2e-4d2d-bf5d-de98c0899f66](https://styles.refero.design/style/802bdc0e-ec2e-4d2d-bf5d-de98c0899f66) |
| Theme | light |
| Industry | fintech |

## Overview

Clutch uses a stark, high-contrast visual language with a focus on deep blacks and crisp whites, punctuated by a vivid electric blue. Typography is tight and strong, often appearing in gradients or with subtle chromatic accents within content. Surfaces are mostly flat, with minimal shadows used only for interactive elements, contributing to a sense of digital precision. The overall impression is one of serious, modern technology with a clear visual hierarchy.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, dominant backdrop for all content |
| Ink Black | `#000000` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color; Backgrounds for decorative elements and visual flourishes, enhancing the high-tech aesthetic |
| Storm Gray | `#e5e7eb` | neutral | Subtle borders, dividers, ghost button outlines, and background accents for secondary elements |
| Muted Gray | `#dfdfdf` | neutral | Slightly darker borders, subtle background accents, and card outlines, often for quieter sections |
| Whisper Gray | `#c9c9c9` | neutral | Faded heading text, secondary text, and less prominent background fills, creating visual depth without strong contrast |
| Dim Gray | `#6e6e6e` | neutral | Muted link text, secondary body copy, sub-labels, conveying information without demanding primary attention |
| Electric Blue | `#004dff` | brand | Primary calls to action, interactive elements, accent for key information, indicating activation and importance; Complex background fills for hero sections and illustrative elements, blending brand blue with neutrals for visual interest |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| weights | 400, 500, 700 |
| sizes | 12px, 14px, 15px, 16px, 17px |
| lineHeight | 1.30, 1.40, 1.50 |
| letterSpacing | -0.02, 0.12 |
| substitute | system-ui |
| role | General body text, navigation elements, form inputs, and secondary UI labels. Provides clarity and legibility for informational content. |

### PP Radio Grotesk

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 10px, 12px, 13px, 14px, 20px, 24px, 26px, 32px, 38px, 56px, 68px |
| lineHeight | 1.10, 1.20, 1.30, 1.40, 1.50 |
| letterSpacing | -0.01, 0.01, 0.02, 0.12, 0.16 |
| substitute | sans-serif |
| role | Primary headlines, subheadings, and emphasized content. Its custom nature and varying tracking create strong visual impact and brand distinctiveness. |

### Times

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.2 |
| role | Times — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0.02 |
| body | 14 |  | 1.5 | -0.02 |
| subheading | 24 |  | 1.3 | -0.01 |
| heading | 38 |  | 1.2 | -0.01 |
| heading-lg | 56 |  | 1.1 | -0.01 |
| display | 68 |  | 1.1 | -0.01 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 6px |
| pills | 80px |
| buttons | 6px |
| default | 6px |

- **elementGap** — 16px
- **sectionGap** — 80px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background and default surface for most content. |
| Muted Gray | `#dfdfdf` | 1 | Subtle background for UI elements like cards or sections, providing minimal visual separation. |

## Components

### Primary Action Button

**Role:** Filled call to action

Electric Blue background with Ink Black text, rectangular shape for bold interaction. Uses #004dff background, #000000 text, and 6px border-radius. Padding is 16px around.

### Ghost Button

**Role:** Call to action variant

Transparent background with Ink Black text, outlined by Storm Gray. Used for secondary actions, allowing primary content to dominate. Uses #000000 text, #e5e7eb border, with 16px padding.

### Navigation Link

**Role:** Header and footer navigation

Ink Black text on a Canvas White background. Hover states are implied by text color change or subtle underlines. Uses PP Radio Grotesk, 16px, 400 weight.

### Detail Card

**Role:** Informational container

Canvas White background with 6px border-radius. No explicit border, relies on surrounding contrast for definition. Padding of 16px horizontally. Shares same background as page.

### Input Field

**Role:** User entry fields

Transparent background for text input, with Ink Black text. Defined by implicit context rather than explicit borders. Placeholder or label text in Ink Black. Padding 12px horizontal, 16px bottom.

### Pill Badge

**Role:** Categorization or status label

Rounded badge with a tight radius. Transparent background with Ink Black text. Primarily used for small labels or tags, with 80px border-radius to create a pill shape.

## Layout

The page layout is primarily full-bleed with content centered within a clear max-width, creating a structured yet expansive feel. The hero section often features a large, impactful headline over an abstract background graphic. Sections typically maintain consistent vertical spacing, often with alternating background shades implicit through subtle changes in neutral color usage or through the placement of visual elements. Content tends to be arranged in centered stacks or two-column text-left/visual-right patterns. A persistent top navigation bar provides site entry points. The rhythm is open and spacious, prioritizing visual impact and clarity.

## Imagery

Imagery primarily consists of abstract 3D renders and stylized iconography, often using gradients of black, white, and Electric Blue, implying a complex, interconnected digital ecosystem. Product screenshots are minimal or non-existent, focusing instead on symbolic representations. Icons are generally outlined or filled in monochromatic tones with occasional brand color accents, maintaining a high-tech, minimalist aesthetic. The density is moderate to low, with visuals serving as decorative atmosphere or conceptual explanations rather than direct product showcases.

## Dos & Donts

### Do

- Adhere to the high contrast between Ink Black (#000000) and Canvas White (#ffffff) for primary text and backgrounds.
- Use Electric Blue (#004dff) exclusively for primary calls to action, active states, and critical brand accents.
- Apply Storm Gray (#e5e7eb) for subtle borders, dividers, and background elements to maintain a clean aesthetic.
- Utilize PP Radio Grotesk for all headlines and significant textual emphasis to maintain brand voice and crispness.
- Ensure interactive elements such as buttons and links use a 6px border-radius for a consistent, subtle rounding.
- Maintain a clear page hierarchy by using the largest PP Radio Grotesk sizes for section titles, stepping down to Inter for body copies.
- Employ the rgba(0, 0, 0, 0.16) 0px 2px 6px 0px shadow selectively for interactive links to signify depth without clutter.

### Don't

- Avoid introducing additional saturated colors unless for semantic feedback (success/error) explicitly defined elsewhere.
- Do not use shadows on non-interactive elements or widespread across the page; keep surfaces flat for a modern feel.
- Refrain from using overly decorative fonts; stick to Inter for readability and PP Radio Grotesk for impact.
- Do not vary border-radius values beyond the specified 6px and 80px; consistency in shape is crucial.
- Avoid dense sections of content without adequate Storm Gray (#e5e7eb) dividers or Canvas White (#ffffff) breathing room.
- Do not use Electric Blue (#004dff) as a text color for general body copy or navigation, reserve it for actions.
- Do not use generic system fonts for headlines; PP Radio Grotesk is key to the brand's typographic identity.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #e5e7eb
accent: #004dff
primary action: #004dff (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #004dff background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a clean Detail Card: #ffffff background, 6px radius, 16px horizontal padding, with a heading in PP Radio Grotesk 24px 500 weight #000000, and body text in Inter 14px 400 weight #000000.
3. Implement a Navigation Link: Inter 16px 400 weight #000000 text, with a subtle #e5e7eb underline on hover, 12px padding around icon and text if present.
4. Show a Pill Badge: Transparent background, 80px border-radius, Inter 12px 400 weight #000000 text, 4px vertical and 8px horizontal padding.
5. Create a Hero Headline: PP Radio Grotesk 68px 600 weight, #000000 color, line-height 1.1, letter-spacing -0.01em. Add a subtext of Inter 17px 400 weight, #000000, line-height 1.5, letter-spacing -0.02em.

---
_Source: https://styles.refero.design/style/802bdc0e-ec2e-4d2d-bf5d-de98c0899f66_
