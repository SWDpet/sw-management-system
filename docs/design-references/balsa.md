# Balsa — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.balsa.com](https://www.balsa.com) |
| Refero page | [https://styles.refero.design/style/ebbfbc6a-988b-4f33-b261-d431b2327545](https://styles.refero.design/style/ebbfbc6a-988b-4f33-b261-d431b2327545) |
| Theme | light |
| Industry | productivity |

## Overview

Balsa employs a modern, structured aesthetic, blending a pristine white canvas with soft, elevated cards to organize content. Typography is deliberately dense and confident, using strong impactful headlines and compact body text to convey information efficiently. A vibrant yellow and deep violet act as functional highlights, drawing attention to calls-to-action and important information within a predominantly achromatic interface. The overall impression is one of clarity, precision, and productivity.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Ice | `#f7f7f7` | neutral | Page backgrounds, subtle surface differentiation |
| Surface White | `#ffffff` | neutral | Card backgrounds, primary interactive surfaces |
| Ink Black | `#000000` | neutral | Primary text, strong headings, primary interactive elements (buttons, links) |
| Graphite | `#313131` | neutral | Secondary text, subheadings, supporting information |
| Storm Gray | `#686868` | neutral | Muted text, helper text, subtle borders |
| Silver Mist | `#bbbbbb` | neutral | Decorative card backgrounds, subtle dividers |
| Purple Haze | `#914db2` | accent | Pink outline accent for tags, dividers, and focused UI edges |
| Goldenrod | `#ffb700` | accent | Highlight cards, callout backgrounds, primary accent for important information — adds focus and urgency |
| Midnight Ink Blue | `#003399` | accent | Link text, outlined button borders — a secondary interactive color distinct from black |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | system-ui |
| role | Fallback or system font for small UI elements and metadata, ensuring broad compatibility. |

### Van Condensed Pro Bold

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 24px, 32px, 48px |
| lineHeight | 1.20, 1.50 |
| letterSpacing | -0.0470em at 48px, -0.0310em at 32px |
| substitute | Bebas Neue |
| role | Impactful headings and display text. Its condensed nature allows for larger sizes without consuming excessive horizontal space, while the strong weight ensures immediate attention. The aggressive negative letter-spacing at larger sizes contributes to a tight, assertive visual feel. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| weights | 300, 400, 500, 600, 700, 900 |
| sizes | 11px, 14px, 16px |
| lineHeight | 1.18, 1.20, 1.29, 1.30, 1.43, 1.50, 1.64, 1.73 |
| letterSpacing | -0.0100em |
| substitute | Inter |
| role | The primary typeface for body copy, subheadings, and UI elements. Its neutrality and high legibility support densely packed information, while various weights provide a clear typographic hierarchy. The consistent slight negative tracking keeps text blocks compact. |

### Roboto Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 11px, 13px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Roboto Mono |
| role | Monospaced font for code snippets, technical details, and any content requiring fixed-width alignment. |

### Noto Sans Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 11px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Noto Sans Mono |
| role | Supplementary monospaced font, likely for specific code or data display. Used sparingly. |

### Inter-Bold

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 11px, 14px, 18px |
| lineHeight | 1.2, 1.43, 1.64, 2 |
| letterSpacing | -0.01 |
| role | Inter-Bold — detected in extracted data but not described by AI |

### Inter-Medium

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 14px |
| lineHeight | 1.43, 1.5, 2 |
| role | Inter-Medium — detected in extracted data but not described by AI |

### Inter-Light

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 11px |
| lineHeight | 1.73 |
| letterSpacing | -0.01 |
| role | Inter-Light — detected in extracted data but not described by AI |

### Inter-Black

| Key | Value |
| --- | --- |
| weight | 900 |
| sizes | 24px |
| lineHeight | 1.2 |
| role | Inter-Black — detected in extracted data but not described by AI |

### Inter-SemiBold

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 32px |
| lineHeight | 1.2 |
| role | Inter-SemiBold — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.73 | -0.11 |
| body | 14 |  | 1.43 | -0.14 |
| heading-sm | 18 |  | 1.43 | -0.18 |
| heading | 24 |  | 1.2 | -1.13 |
| heading-lg | 32 |  | 1.2 | -0.99 |
| display | 48 |  | 1.2 | -2.26 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 16px |
| cards | 12px |
| buttons | 6px |
| minimal | 1px |

- **elementGap** — 8px
- **sectionGap** — 60px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Ice | `#f7f7f7` | 0 | Base page background |
| Surface White | `#ffffff` | 1 | Primary interactive cards, content blocks, and UI elements |

## Components

### Primary Filled Button

**Role:** Main call-to-action

Solid Ink Black background with Surface White text. Uses a 6px border radius and generous padding of 12px vertical, 8-12px horizontal. Text is typically 12px system sans-serif.

### Outline Accent Button (Midnight Ink Blue)

**Role:** Secondary call-to-action or informational link

Transparent background with a 1px Midnight Ink Blue border and Midnight Ink Blue text. Uses a 6px border radius, 12px vertical padding, and 8-12px horizontal padding. Text is typically 12px system sans-serif.

### Base Card (No Shadow)

**Role:** Content grouping, pricing tiers in neutral state

Background of rgba(255, 255, 255, 0.8) with a 12px border radius. No shadows, giving a flat, clean appearance. Padding is typically absent for these cards, relying on internal element spacing.

### Elevated Content Card

**Role:** Prominent content blocks, feature descriptions

Surface White background, 12px border radius, and a complex, subtle multi-layer shadow stack (rgba(0,0,0,0.09) starting at 0px 2.15px 3px). Provides significant visual lift without adding heavy dark tones. No internal padding defined at the card level.

### Callout Card (Goldenrod)

**Role:** Highlighting key information, warnings, or tips

Goldenrod background, 12px border radius. Uses a subtle, warm shadow tint (rgba(112, 94, 0, 0.05) starting at 0px 2.15px 1.72px). Features consistent internal padding of 20px horizontal and 20-32px vertical. Text in Ink Black.

### Callout Card (Purple Haze)

**Role:** Highlighting secondary key information or status updates

Purple Haze background, 12px border radius. Uses a subtle, cool-toned shadow (rgba(61, 32, 75, 0.05) starting at 0px 2.15px 1.72px). Features consistent internal padding of 20px horizontal and 20-32px vertical. Text in Ink Black.

## Layout

The page model is primarily centered content within a contained width, though the hero section spans full-bleed. The hero presents a centered headline and subtext, followed by a product screenshot on a clean Canvas Ice background. Sections follow a consistent vertical rhythm, with `sectionGap` (60px) providing ample separation. Content is often arranged in simple, centered stacks, or in 3-column card grids for features and pricing. Navigation is a minimal top-right 'Log in' link.

## Imagery

This site uses product screenshots and contained graphical elements. Product screenshots are typically clean, isolated views of the Balsa interface, often shown within a browser frame on a white background, highlighting specific features or workflows. Illustrations are flat, geometric, and follow brand colors, primarily used for decorative accents or simple icons. Iconography is generally outlined or filled, with varying stroke weights, maintaining a functional purpose rather than being purely ornamental. Imagery serves to explain product features and add visual interest in a contained, text-dominant layout.

## Dos & Donts

### Do

- Always use Canvas Ice (#f7f7f7) as the base page background.
- Apply Surface White (#ffffff) for all elevated cards and primary UI elements that require a clean, bright backdrop.
- Prioritize Ink Black (#000000) for all primary text and bold headlines to ensure high contrast and readability.
- Reserve Goldenrod (#ffb700) and Purple Haze (#914db2) exclusively for accent cards, callouts, or small functional indicators, never for large background areas or primary text.
- Maintain a clear visual hierarchy by utilizing Van Condensed Pro Bold for main headings and Inter for all body text, with Inter's various weights for subheadings and emphasis.
- Use 12px border-radius for all cards and container elements to maintain the consistent soft, modern feel.
- Ensure generous vertical spacing between sections, using the defined sectionGap of 60px to provide breathing room.

### Don't

- Avoid using multiple different accent colors on a single screen; limit to Goldenrod or Purple Haze for focused attention.
- Do not use dark backgrounds for main content areas; maintain the light theme with Canvas Ice and Surface White providing the primary surfaces.
- Refrain from using strong, opaque shadows; instead, apply the subtle, tiered shadow stacks seen on Elevated Content Cards for a soft lift.
- Do not vary border radius arbitrarily; adhere to 12px for cards and 6px for buttons and interactive elements.
- Avoid decorative imagery that competes with UI elements; imagery should be contained, product-focused, or purely illustrative.
- Do not introduce new typefaces outside of Van Condensed Pro Bold, Inter, Roboto Mono, Noto Sans Mono, or the system sans-serif fallback.
- Exclude heavy gradients or complex background patterns, maintaining the flat and clean aesthetic of the primary surfaces.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- text: #000000
- background: #f7f7f7
- border: no distinct chromatic border color
- accent: #ffb700
- primary action: #000000 (filled action)

**3-5 Example Component Prompts:**
1. Create a large section headline: 'Docs for building software' using Van Condensed Pro Bold, size 48px, weight 400, color Ink Black, letter-spacing -2.26px. Place it above body text 'Balsa is for organizing ideas...' using Inter, size 16px, weight 400, color Graphite, letter-spacing -0.16px, centered, with 32px vertical spacing between the headline and body text.
2. Create a Primary Action Button: #000000 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
3. Design an Elevated Content Card: 12px border radius, Surface White background, applying the 'Elevated Content Card' shadow style. Inside, include a heading 'Starter' (Inter, 18px, weight 700, Ink Black, letter-spacing -0.18px) and a body text paragraph (Inter, 14px, weight 400, Graphite, letter-spacing -0.14px). Internal padding for main content should be 20px.
4. Create a Callout Card (Goldenrod): Goldenrod background, 12px border radius, applying the 'Callout Card (Goldenrod)' shadow style. Text inside should be Ink Black, using Inter 14px weight 400, letter-spacing -0.14px, with 20px horizontal and 20px top padding and 32px bottom padding.
5. Assemble a list item: using Inter, 14px, weight 400, color Ink Black, letter-spacing -0.14px. Pair it with a small icon (say, a checkmark) and ensure 8px `elementGap` between the icon and text.

---
_Source: https://styles.refero.design/style/ebbfbc6a-988b-4f33-b261-d431b2327545_
