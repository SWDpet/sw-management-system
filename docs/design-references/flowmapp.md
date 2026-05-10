# Flowmapp — Design Reference

> Architectural blueprint on white marble, accented with soft, glowing energy.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://flowmapp.com](https://flowmapp.com) |
| Refero page | [https://styles.refero.design/style/caca412f-7fc7-4510-aacc-5664d4f8ce9f](https://styles.refero.design/style/caca412f-7fc7-4510-aacc-5664d4f8ce9f) |
| Theme | light |
| Industry | design |

## Overview

This design evokes a feeling of structured clarity and playful dynamism, balancing functional utility with a touch of modern vibrancy. The primarily white-bodied interface provides a blank canvas, punctuated by a deep blue accent (#0080ff) that guides interaction. Organic, blurred background shapes add a soft, futuristic atmosphere, while the crisp black typography and rounded components (#1600px, #12px) create a friendly yet precise tool.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, primary button backgrounds. |
| Ink Black | `#000000` | neutral | Primary text, prominent icons, dark button backgrounds. |
| Steel Gray | `#8c9baa` | neutral | Secondary text, subtle borders, inactive elements. |
| Highlight Blue | `#0080ff` | brand | Call-to-action buttons, interactive elements, key iconography – signaling action and attention. |
| Subtle Blue | `#c5e0fb` | brand | Light background details, subtle button fills, card elements, supporting the primary blue without competing. |
| Gradient Violet | `#0050ff` | accent | Used as the border for cards, it provides a subtle visual lift and defines interactive sections with a touch of color. |
| Pebble Gray | `#dee0e4` | neutral | Subtle borders, dividers, and background accents. |
| Charcoal Text | `#222222` | neutral | Navigation text, less prominent black text for hierarchical distinction. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 10px, 12px, 13px, 14px, 16px, 18px, 24px, 30px, 36px, 48px, 64px, 72px, 84px, 96px, 118px |
| lineHeight | 1.0 to 1.67 |
| letterSpacing | -0.0600em at largest sizes, less at smaller sizes |
| substitute | Inter |
| role | All text elements: headlines, body copy, navigation, buttons. Its broad range of weights and sizes supports a clear typographic hierarchy, making it versatile for both display and utilitarian text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1 |  |
| body | 14 |  | 1.4 |  |
| subheading | 18 |  | 1.4 |  |
| heading-sm | 24 |  | 1.25 |  |
| heading | 36 |  | 1.14 | -0.48 |
| heading-lg | 48 |  | 1.14 | -0.48 |
| display | 72 |  | 1.09 | -0.48 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| buttons | 1600px |
| smallUI | 6px |
| largeCards | 32px |
| accentedCards | 20px |
| interactiveElements | 1600px |

- **elementGap** — 8px
- **sectionGap** — 48-80px
- **cardPadding** — 24px
- **pageMaxWidth** — 1560px

## Components

### CTA Button Group

### Feature Cards Grid

### Social Proof — Company Logos Bar

### Secondary Button (Dark Fill)

**Role:** Secondary Action

Filled with Ink Black (#000000), white text, with 12px radius. Offers a strong alternative action without competing with the primary blue. Padding: 7.04px vertical, 10.08px horizontal.

### Secondary Button (Light Fill)

**Role:** Secondary Action

Filled with Canvas White (#ffffff), Ink Black (#000000) text, with 12px radius. Used for less prominent actions, often within navigation. Padding: 9.92px vertical, 12px horizontal.

### Ghost Button (Blue Accent)

**Role:** Tertiary Action

Transparent background, white text, 1600px radius. Highlighted with rgba(0, 128, 255, 0.2) as background, providing a subtle interactive cue. Padding: 12px all around.

### Standard Card

**Role:** Content Container

Canvas White (#ffffff) background, 12px border-radius, no shadow. Internal padding: 24px.

### Accent Border Card

**Role:** Highlight Container

Transparent background, 20px border-radius, no shadow, with a distinct Gradient Violet (#0050ff) border detected in patterns. Internal padding: 24px.

### Page Header Navigation

**Role:** Global Navigation

Features Canvas White background for text and active links, Steel Gray text for other links. Includes a white secondary button and a pill-shaped primary CTA.

### Callout Icon Card

**Role:** Feature Highlight

Transparent background with 32px radius, no shadow. Padding: 24px. Houses icons or small informational blocks.

## Layout

The page operates on a max-width contained model of 1560px, maintaining a centered content block throughout. The hero section is full-bleed with a large, centered headline and a prominent primary CTA, visually supported by abstract, blurred gradient shapes that hint at depth and dynamism without direct interaction. Section rhythm is consistent, with clear vertical spacing and sections primarily occupying a single column stack for headlines followed by multi-column arrangements (e.g., 2-column text+icon features, 3-column card grids for features). There's no alternating light/dark banding; the design maintains a mostly white background. Navigation is a sticky top bar with a focused left-aligned logo and right-aligned interactive elements including the primary CTA.

## Imagery

The site uses a mix of light, playful illustrations and product screenshots, with abstract, organic gradient shapes (#98cafc, #3298fe, #82bffc) creating a vibrant background. Illustrations are simple, two-dimensional, often depicting tiny, functional elements or user interface snippets. Product screenshots are contained within rounded frames, showcasing the application's UI on mobile devices or in flow diagrams. Icons are primarily filled, black, and utilize the rounded aesthetic present throughout the UI. The overall density is balanced, allowing significant whitespace around visual elements, making the images feel airy and decorative rather than information-heavy.

## Dos & Donts

### Do

- Use Highlight Blue (#0080ff) exclusively for primary calls-to-action and critical interactive feedback.
- Apply 1600px radius for all primary buttons and interactive elements to maintain the 'pill' aesthetic.
- Maintain a clear visual hierarchy by using Ink Black (#000000) for main headings and Canvas White (#ffffff) for card/page backgrounds.
- Utilize the Inter font family across all typographic elements, leveraging its full range of weights (400-700) for distinct hierarchy.
- Ensure consistent internal padding of 24px for all `Standard Card` and `Accent Border Card` components.
- Implement the Steel Gray (#8c9baa) for secondary text and subtle navigational elements to soften visual noise.

### Don't

- Do not introduce new color tones; stick to the defined palette of blues, grays, black, and white.
- Avoid using sharp corners; all significant UI elements like buttons and cards must use a defined border-radius (12px, 20px, 32px, or 1600px).
- Do not apply drop shadows to cards or primary UI elements; visual depth is created through background blur gradients and borders.
- Refrain from using color to denote semantic states (e.g., red for error, green for success) unless explicitly defined within the system; use established brand colors instead.
- Do not deviate from the Inter typeface; avoid introducing any additional font families.
- Avoid excessive use of bold typography; reserve font weight 700 for headlines, and 600 for important subheadings or call-to-action text.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- Text: #000000
- Background: #ffffff
- CTA: #0080ff
- Border/Accent: #8c9baa
- Card Surface: #ffffff

**Example Component Prompts**
- Create a `Primary CTA Button`: blue background (#0080ff), white text, 1600px radius, 16px 32px padding, Inter size 16 weight 700.
- Generate a `Standard Card`: white background (#ffffff), 12px radius, no shadow, 24px padding. Headline: Inter size 24 weight 600, Ink Black (#000000). Body: Inter size 14 weight 400, Steel Gray (#8c9baa).
- Design a `Secondary Button (Light Fill)`: white background (#ffffff), black text (#000000), 12px radius, 9.92px 12px padding. Text should be Inter size 16 weight 500.
- Construct a `Page Header Navigation` bar: white background (#ffffff), height 70px. Left-aligned logo, right-aligned 'Log in' link (Inter size 14 weight 500, Ink Black), and 'Try for free' button (Primary CTA Button style).

---
_Source: https://styles.refero.design/style/caca412f-7fc7-4510-aacc-5664d4f8ce9f_
