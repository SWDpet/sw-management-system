# Letters — Design Reference

> Blue Sky Productivity

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://letters.app](https://letters.app) |
| Refero page | [https://styles.refero.design/style/04109c48-f591-4110-9739-622243d4ecc2](https://styles.refero.design/style/04109c48-f591-4110-9739-622243d4ecc2) |
| Theme | light |
| Industry | ai |

## Overview

Letters projects a calm, intelligent authority with its cool-toned blue gradients, abundant whitespace, and precise typography. The interface uses a clean, multi-layered card system with deeply rounded corners, creating a soft, approachable feel. Strategic use of a deep charcoal primary action color grounds the light theme, providing a confident focal point without harsh contrast. Subtle shadows add depth without weight, maintaining an airy and modern aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Cloud Canvas | `#ffffff` | neutral | Page backgrounds, elevated surfaces, card backgrounds |
| Ghost Gray | `#f5f5f5` | neutral | Secondary card backgrounds, surface accents |
| Deep Sea Blue | `#2597d0` | brand | Blue outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Sky Wash | `#c0eaff` | accent | Subtle background accents, soft highlights |
| Coal Black | `#000000` | neutral | Primary headline and body text, strong borders |
| Charcoal Action | `#070709` | neutral | Primary call-to-action button backgrounds, important body text |
| Slate Text | `#60606c` | neutral | Secondary body text, supporting information |
| Mid-Gray Text | `#8b8b8b` | neutral | Placeholder text, tertiary information |
| Light Gray Text | `#bebecc` | neutral | Muted helper text, disabled states |
| Cool Steel | `#99a0ae` | neutral | Subtle borders, inactive states |
| Hover Slate | `#efeff1` | neutral | Hover state backgrounds for transparent buttons |
| Blue Gradient | `#779bc1` | brand | Hero section background, large brand areas |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### Open Runde

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700, 900 |
| sizes | 10px, 12px, 14px, 16px, 17px, 18px, 20px, 28px, 44px, 80px |
| lineHeight | 0.90, 1.10, 1.20, 1.40, 1.49, 1.50 |
| letterSpacing | -0.010em, -0.010em, -0.010em, -0.009em, -0.011em, -0.010em, -0.040em, -0.040em, -0.040em, -0.040em |
| substitute | Source Sans Pro |
| role | Primary brand typeface for headings and prominent text. Features a range of weights to establish clear hierarchy. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 14px |
| lineHeight | 1.20, 1.40, 1.50 |
| letterSpacing | -0.010em |
| substitute | system-ui |
| role | Secondary sans-serif for body copy and navigational elements, providing high readability. |

### The Doctor FreeVersion

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 38px |
| lineHeight | 0.90, 1.40 |
| letterSpacing | -0.010em, -0.010em |
| substitute | Dancing Script |
| role | Decorative/script font used for specific brand moments, typically small to add character without dominating. |

### Open Runde Semibold

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 17px, 18px, 44px |
| lineHeight | 1.1, 1.32, 1.4, 1.49 |
| letterSpacing | -0.04, -0.011, -0.01, -0.009 |
| role | Open Runde Semibold — detected in extracted data but not described by AI |

### Open Runde Medium

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 11px, 12px, 14px, 17px, 28px, 40px |
| lineHeight | 1.09, 1.2, 1.3, 1.33, 1.4, 1.43 |
| letterSpacing | -0.08, -0.04, -0.012, -0.01, -0.007, -0.006, -0.001, 0.02 |
| role | Open Runde Medium — detected in extracted data but not described by AI |

### Open Runde Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 13px, 14px, 17px |
| lineHeight | 1.14, 1.33, 1.4, 1.43, 1.45 |
| letterSpacing | -0.012, -0.01, -0.006 |
| role | Open Runde Regular — detected in extracted data but not described by AI |

### Open Runde Bold

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 20px, 28px |
| lineHeight | 1.2, 1.4 |
| letterSpacing | -0.04, -0.01 |
| role | Open Runde Bold — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | -0.1 |
| body | 14 |  | 1.4 | -0.14 |
| heading | 20 |  | 1.2 | -0.8 |
| heading-lg | 28 |  | 1.2 | -1.12 |
| display | 80 |  | 0.9 | -3.2 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 100px |
| cards | 48px |
| inputs | 18px |
| buttons | 100px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Cloud Canvas | `#ffffff` | 0 | Base page background and default content surfaces |
| Ghost Gray | `#f5f5f5` | 1 | Secondary background for card groups or subtle content sections |
| Sky Wash | `#c0eaff` | 2 | Lightly tinted background for specific, non-critical elevated content |

## Components

### Primary Action Button

**Role:** The main call to action on the page.

Background: Charcoal Action (#070709), Text: Cloud Canvas (#ffffff). Border radius: 100px. Padding: 12px vertical, 24px horizontal. Text: Open Runde Regular 14px, lineHeight 1.2, letterSpacing -0.14px.

### Ghost Button

**Role:** Secondary action or link, visually minimal.

Background: Cloud Canvas (#ffffff), Text: Deep Sea Blue (#2597d0). Border radius: 100px. Padding: 12px vertical, 24px horizontal. Text: Open Runde Regular 14px, lineHeight 1.2, letterSpacing -0.14px.

### Subtle Accent Button

**Role:** Tertiary actions or interactive tags.

Background: Hover Slate (#efeff1), Text: Deep Sea Blue (#2597d0). Border radius: 100px. Padding: 12px vertical, 18px horizontal. Text: Open Runde Regular 14px, lineHeight 1.2, letterSpacing -0.14px.

### Elevated Card

**Role:** Primary content container, creating visual separation.

Background: Cloud Canvas (#ffffff). Border radius: 18px. Shadow: subtle blue tint, rgba(22, 107, 197, 0.05) 0px 7px 7px 0px. Padding: 28px top, 20px horizontal, 20px bottom.

### Flat Card

**Role:** Secondary content containers, less emphasis than Elevated Card.

Background: Ghost Gray (#f5f5f5). Border radius: 48px. No shadow. Padding: 40px all sides.

### Clean Input Field

**Role:** Standard input for user text or data.

Background: Cloud Canvas (#ffffff). Border: 1px solid Cool Steel (#99a0ae). Border radius: 18px. Padding: 12px vertical, 16px horizontal. Placeholder text: Mid-Gray Text (#8b8b8b).

### Header Navigation Link

**Role:** Top-level navigation items.

Text: Coal Black (#000000). Font family: Inter, 14px, weight 500. Underline on hover (Deep Sea Blue). No background or explicit padding, but uses 8px horizontal elementGap.

## Layout

The page model is a max-width centered container for most content, contrasted with full-bleed hero sections that set the atmospheric tone. The initial hero features a large, gradient background with a centered, prominent headline and a primary action button. Subsequent sections alternate between light backgrounds and use a consistent vertical rhythm (sectionGap: 40px). Content arrangement frequently employs two-column layouts featuring text on one side and a supporting visual or product screenshot on the other. Card grids are used for feature listings, typically with three columns, and feature soft, rounded cards. Navigation is a sticky top bar with a left-aligned logo and right-aligned links and a prominent 'Sign up' button. Content density is comfortable, with ample breathing room around elements.

## Imagery

The visual language for imagery is minimal and functional, focusing on product demonstration and conceptual clarity. Photography is absent. Illustrations are simple, flat, and vector-based, often using the brand's blue tones or achromatic grays, serving an explanatory or decorative role rather than a detailed one. Icons are outlined, with a moderate stroke weight, and monochromatic (Deep Sea Blue or Coal Black). Product screenshots are often contained within soft-edged cards or abstracted as 'paperwork' elements, emphasizing the software's functionality over a busy UI. Imagery density is moderate; visuals are used to break up text and explain concepts, but the interface remains text-dominant.

## Dos & Donts

### Do

- Use Charcoal Action (#070709) for all primary call-to-action button backgrounds.
- Apply Cloud Canvas (#ffffff) for all main page backgrounds and primary card surfaces.
- Implement a 48px border-radius for large content cards to maintain a soft, friendly aesthetic.
- Employ the Open Runde family for all headlines and brand-specific text, adjusting weights to create hierarchy.
- Use a subtle elevation shadow (rgba(22, 107, 197, 0.05) 0px 7px 7px 0px) to give depth to interactive elements and important cards.
- Maintain generous padding of at least 40px around card content and between major sections.
- Use Deep Sea Blue (#2597d0) as the primary accent for icons, links, and active states.

### Don't

- Avoid harsh, saturated colors; stick to the desaturated blue gradients and a mostly achromatic palette.
- Do not use sharp corners; maintain a consistent radius of at least 18px for most interactive elements and surfaces.
- Do not overload sections with heavy shadows; elevation should be subtle and air-like.
- Do not use dark backgrounds for full sections; the theme is predominantly light and airy.
- Avoid using multiple decorative fonts; reserve 'The Doctor FreeVersion' sparingly for special graphical elements.
- Do not break the established type scale; ensure all text adheres to defined sizes and line heights.
- Avoid using a page-wide max-width; sections should use the available width, with centered content blocks.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #99a0ae
accent: #2597d0
primary action: #070709 (filled action)

Example Component Prompts:
Create a Primary Action Button: #070709 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Create a feature card: Flat Card background (Ghost Gray #f5f5f5), border-radius 48px, padding 40px. Inside, a heading (Open Runde heading-lg, 28px, weight 700, Coal Black #000000) and body text (Inter body, 14px, weight 400, Slate Text #60606c).


Create an input field: Clean Input Field with a placeholder 'Enter your email' (Mid-Gray Text #8b8b8b). Focus state border: Deep Sea Blue (#2597d0).

Create an article snippet with a subtle shadow: Elevated Card with padding 40px. Inside, a subheading (Open Runde heading-sm, 18px, weight 600, Coal Black #000000) and brief body text (Inter body-sm, 12px, weight 400, Slate Text #60606c).

---
_Source: https://styles.refero.design/style/04109c48-f591-4110-9739-622243d4ecc2_
