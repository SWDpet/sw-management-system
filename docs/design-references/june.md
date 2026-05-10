# June — Design Reference

> Whiteboard with purple ink

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.june.so](https://www.june.so) |
| Refero page | [https://styles.refero.design/style/40e4d3ef-cd28-483b-8c8a-b9cf44281b03](https://styles.refero.design/style/40e4d3ef-cd28-483b-8c8a-b9cf44281b03) |
| Theme | light |
| Industry | saas |

## Overview

June presents a clean, approachable SAAS aesthetic built on a foundation of white space and subtle surface variations. Typography, primarily SF Pro Rounded, strikes a balance between professional clarity and a hint of friendly roundness. A single vivid violet accent color is used sparingly to highlight key information and interactive elements, creating points of visual interest without overwhelming the interface. Components are lightweight with soft shadows and rounded corners, conveying a sense of digital product, not corporate rigidity.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#fafafa` | neutral | Primary page background, often paired with darker text elements for strong contrast. Provides an airy, open feel |
| Cloud Gray | `#e9ecef` | neutral | Soft icon strokes, subtle dividers, and low-emphasis decorative details. Do not promote it to the primary CTA color |
| Haze White | `#f0f0fe` | neutral | Secondary surface background, used for lifted cards or content blocks to create a subtle depth layer on the Canvas White |
| Inkwell Text | `#151531` | neutral | Primary body text, link text, and prominent content. Provides high readability against light backgrounds |
| Midnight Violet | `#2a2a63` | neutral | Headings and bolder text elements. Also appears as a border color, adding emphasis |
| Shadow Blue-Gray | `#343a40` | neutral | Secondary text, muted helper text, and subtle icon coloring. Provides a softer textual contrast than Inkwell Text |
| Purple Accent | `#6868f7` | accent | The primary brand accent, used for prominent cards, or occasionally as a decorative background flourish. Creates a vibrant, technology-focused impression |
| Cloud Shadow | `#cfd0d1` | neutral | Light shadow tint, contributes to elevation effects for cards |
| Soft Shadow | `#c3c4c6` | neutral | Slightly deeper shadow tint, used in combination with Cloud Shadow for card elevation |

## Typography

### SF Pro Rounded

| Key | Value |
| --- | --- |
| weight | 400, 700, 900 |
| weights | 400, 700, 900 |
| sizes | 12px, 14px, 16px, 18px, 24px, 40px, 60px |
| lineHeight | 1.00, 1.17, 1.19, 1.25, 1.43, 1.50, 1.56 |
| letterSpacing | -0.015em at 12-14px, 0.025em for other sizes |
| fontFeatureSettings | "kern" |
| substitute | Avenir Next Rounded |
| role | Primary user interface typeface. Its rounded terminals provide a friendly yet modern feel. Used for all headings, body text, and UI elements. The wide range of weights supports a clear typographic hierarchy. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 16px |
| lineHeight | 1.00, 1.50 |
| letterSpacing | normal |
| fontFeatureSettings | "kern" |
| substitute | system-ui |
| role | Used for specific secondary content or as a fallback. Maintains readability with a more standard, geometric sans-serif quality. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.18 |
| body-sm | 14 |  | 1.43 | -0.21 |
| body | 16 |  | 1.5 | 0.4 |
| subheading | 18 |  | 1.56 | 0.45 |
| heading | 24 |  | 1.5 | 0.6 |
| heading-lg | 40 |  | 1.25 | 1 |
| display | 60 |  | 1 | 1.5 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| prominentCard | 20px |

- **elementGap** — 24px
- **sectionGap** — 24-80px
- **cardPadding** — 48px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#fafafa` | 0 | Base page background |
| Haze White | `#f0f0fe` | 1 | Default slightly elevated card backgrounds |
| Purple Accent | `#6868f7` | 2 | Prominent accent cards |

## Components

### Navigation Link

**Role:** Global navigation item

Text uses Inkwell Text at 16px. Border appears underneath on hover with Cloud Gray. Element gap of 8px to adjacent items.

### Subtle Elevated Card

**Role:** Content container for secondary information

Background is Canvas White, with a 12px border radius. Shadows are soft: rgba(13, 19, 27, 0.1) 0px 2px 10px 0px, rgba(13, 19, 27, 0.2) 0px 0px 2px 0px. No internal padding specified, implying content manages its own spacing.

### Default Content Card

**Role:** Primary content container within sections

Background is Haze White, with a 12px border radius. Features an inset shadow: rgba(13, 19, 27, 0.05) 0px 0px 0px 1px inset. Provides substantial internal padding of 48px on all sides.

### Prominent Accent Card

**Role:** Highlighting key messages or calls to action

Features a solid Purple Accent background with a larger 20px border radius. Accompanied by a soft, diffused shadow: rgba(13, 19, 27, 0.25) 0px 0px 1px 0px, rgba(13, 19, 27, 0.05) 0px 2px 1px 0px. Generous internal padding of 64px on all sides.

### Signature Line Divider

**Role:** Used in the 'The June Team' section for visual separation

These are effectively decorative images of signatures acting as visual dividers, implying personalized connection.

## Layout

The page uses a maximum-width contained layout rather than full-bleed. The hero section is centered with a prominent headline and subtitle. Content sections follow a consistent vertical rhythm with varying section gaps. The main content often appears within a card-like structure, centered on the page. There's a single, persistent top navigation bar. The overall density is comfortable, with ample whitespace creating clear separation between content blocks.

## Imagery

The site predominantly uses custom hand-drawn 'signature' style imagery for emotional connection, paired with a simple SVG logo. There's an absence of photography or detailed product illustrations. Icons, like the June logo, are monochromatic, filled, and use the brand's primary text colors, maintaining a minimal and functional aesthetic. Imagery acts more as decorative punctuation and brand identity rather than explanatory content or product showcases, making the design very text-dominant.

## Dos & Donts

### Do

- Prioritize SF Pro Rounded for all text elements to maintain brand consistency and friendliness.
- Use Canvas White (#fafafa) as the dominant background for all main page canvases.
- Apply Haze White (#f0f0fe) for lifted content cards to create subtle differentiation and hierarchy.
- Employ Cloud Gray (#e9ecef) exclusively for borders and subtle dividers, avoiding heavy outlines.
- Reserve Purple Accent (#6868f7) for high-impact cards or crucial interactive states, ensuring it stands out.
- Maintain generous padding of 48px for default content cards and 64px for prominent accent cards.
- Utilize 12px border radius for most cards and 20px for high-attention accent cards.

### Don't

- Avoid using multiple chromatic colors; stick to Purple Accent as the primary brand color.
- Do not introduce strong, heavy borders; prefer subtle Cloud Gray or soft shadow emphasis.
- Do not deviate from the SF Pro Rounded typeface for headings or primary body text.
- Avoid sharp corners on components; use 12px or 20px radii for all card-like elements.
- Do not use dark backgrounds for large sections of content; maintain the light theme dominance.
- Avoid heavy or complex shadow effects; stick to the specified soft, subtle card shadows.
- Do not adjust letter-spacing on elements outside of the specified values provided for SF Pro Rounded sizes.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #151531
background: #fafafa
border: #e9ecef
accent: #6868f7
primary action: no distinct CTA color

Example Component Prompts:
1. Create a primary content section: background Haze White (#f0f0fe), 12px border radius, with 48px padding. Inside, a heading 'A new chapter for June' (SF Pro Rounded Bold 60px, #2a2a63, letter-spacing 1.5px) and body text (SF Pro Rounded Regular 16px, #151531, letter-spacing 0.4px).
2. Design a navigation bar: background Canvas White (#fafafa), with 'Product', 'Customers', 'Pricing', 'Content', and 'Changelog' links. Links use Inkwell Text (#151531) at 16px, with an 8px element gap. Active links have a bottom border of 1px Cloud Gray (#e9ecef).
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.

---
_Source: https://styles.refero.design/style/40e4d3ef-cd28-483b-8c8a-b9cf44281b03_
