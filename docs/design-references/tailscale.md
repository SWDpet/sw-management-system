# Tailscale — Design Reference

> Cloud control panel on pristine paper.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://tailscale.com](https://tailscale.com) |
| Refero page | [https://styles.refero.design/style/5b679fb6-8d53-402d-a77b-c88bfb397623](https://styles.refero.design/style/5b679fb6-8d53-402d-a77b-c88bfb397623) |
| Theme | light |
| Industry | devtools |

## Overview

Tailscale employs a pragmatic, enterprise-focused interface aesthetic: a spacious, bright white canvas with strong dark typography. Components are functional and subtly elevated, using soft shadows over hard borders, creating a sense of reliability without being overly decorative. A singular vivid red serves as a confident accent for primary actions and key highlights, puncturing an otherwise monochrome scheme of grays and off-whites. The overall impression is one of clarity, efficiency, and understated authority.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Primary page background, card surfaces, interactive elements like pop-ups and modals |
| Surface Frost | `#f7f5f4` | neutral | Subtle background for navigation or secondary content blocks, providing a soft lift from the main canvas |
| Canvas Pale | `#eeebea` | neutral | General page background for most sections, offering a warm off-white foundation |
| Graphite Black | `#181717` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Storm Gray | `#2e2d2d` | neutral | Secondary text for body copy, subheadings, and muted link states. Slightly softer than primary text |
| Stone Gray | `#575555` | neutral | Muted helper text, secondary navigation items, and less prominent information |
| Smoke Gray | `#706e6d` | neutral | Tertiary text, copyright information, and borders requiring minimal emphasis |
| Cloud Mist | `#d5d3d2` | neutral | Subtle borders and dividers, indicating separation without visual weight |
| Border Light | `#232222` | neutral | Ghost button text and borders, secondary action backgrounds |
| Action Red | `#d04841` | brand | Primary call-to-action buttons, active statuses, and key iconography requiring immediate attention. Provides a vivid focal point |
| Action Blue Gradient | `#5a82de` | accent | Decorative background gradient, used for visual highlights or product showcases |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600 |
| weights | 300, 400, 500, 600 |
| sizes | 12px, 14px, 16px, 20px, 32px, 48px, 64px |
| lineHeight | 1.20, 1.43, 1.50 |
| letterSpacing | -0.36, -0.42, -0.48, -0.6, -0.96, -1.44, -1.92 |
| substitute | system-ui, sans-serif |
| role | The primary typeface for all UI text, body copy, and most headings. Its range of weights and subtle tracking adjustments support both readability and hierachy. Letter spacing is consistently tightened for all sizes. |

### MDIO

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 12px, 14px, 20px |
| lineHeight | 1.20, 1.50 |
| letterSpacing | 0.516, 0.602, 0.86 |
| substitute | system-ui, monospace |
| role | A custom monospace-style typeface used sparingly for specific headings, code snippets, or data labels where a distinct, technical feel is desired. Wider tracking distinguishes it from Inter. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.36 |
| body-sm | 14 |  | 1.5 | -0.42 |
| body | 16 |  | 1.5 | -0.48 |
| subheading | 20 |  | 1.5 | -0.6 |
| heading | 32 |  | 1.2 | -0.96 |
| heading-lg | 48 |  | 1.2 | -1.44 |
| display | 64 |  | 1.2 | -1.92 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 16px |
| buttons | 8px |
| largeElements | 32px |

- **elementGap** — 12px
- **sectionGap** — 24-64px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Pale | `#eeebea` | 0 | Base page background, providing a light, warm foundation. |
| Canvas White | `#ffffff` | 1 | Elevated surfaces like cards, panels, and modals, providing contrast against the base canvas. |
| Dark Content Panel | `#2e2d2d` | 2 | Specific content blocks requiring a dark background for emphasis or thematic break. Does not typically stack on other elevated elements. |

## Components

### Primary Filled Button

**Role:** Hero actions, main CTA buttons.

Background: Action Red (#d04841). Text: Canvas White (#FFFFFF). Radius: 8px. Padding: 10px vertical, 12px horizontal.

### Neutral Ghost Button

**Role:** Secondary calls to action, text links that behave as buttons.

Background: transparent. Text: Graphite Black (#181717). Border: 1px solid Graphite Black (#181717). Radius: 8px. Padding: 10px vertical, 12px horizontal.

### Secondary Ghost Button

**Role:** Tertiary actions, usually within a card or alongside a primary button.

Background: transparent. Text: Storm Gray (#2e2d2d). Border: 1px solid Canvas Pale (#eeebea). Radius: 16px. Padding: 24px.

### Status Tag

**Role:** Small informational labels, categories, or active state indicators.

Background: Canvas White (#FFFFFF). Text: Storm Gray (#2e2d2d). Border: 1px solid Canvas Pale (#eeebea). Radius: 9999px. Padding: 0px vertical, 18px horizontal.

### Feature Card

**Role:** Displaying product features or testimonials in a structured grid.

Background: Canvas White (#FFFFFF). Radius: 16px. Shadow: rgba(24, 23, 23, 0.02) 0px 4px 8px 0px. Padding: 32px.

### Large Feature Card

**Role:** Prominent feature display or marketing content with more visual emphasis.

Background: Canvas White (#FFFFFF). Radius: 32px. Shadow: rgba(24, 23, 23, 0.02) 0px 4px 8px 0px. Padding: 64px.

### Dark Content Panel

**Role:** Highlighting specific content sections with a darker background.

Background: Storm Gray (#2e2d2d). Radius: 32px. No shadow. Padding: 64px vertical, 48px horizontal.

## Layout

The page uses a maximum width containment for its primary content, centered on a light background. The hero section is characterized by a central headline and subtext, followed by a double button CTA group. Subsequent sections alternate between full-width simple card grids (e.g., trust signals like '30,000 businesses') and more complex arrangements that combine text and image/mockup panels. The overall rhythm maintains consistent vertical spacing between major sections, with content delivered in digestible blocks. Navigation is a sticky top bar with clearly delineated primary links and discrete action buttons.

## Imagery

The site primarily uses product screenshots and technical icons. Product screenshots are typically tight crops showcasing interface details, often within device mockups. Iconography is clean, outlined, and minimal, focusing on clarity and functionality; some decorative iconography uses flat, bold shapes with subtle color accents. Photography is absent, emphasizing a functional, software-centric approach. Imagery serves an explanatory or product showcase role, with a lean density that prioritizes text content.

## Dos & Donts

### Do

- Prioritize Inter for all typographic needs; reserve MDIO for specific labels or code-like elements.
- Use Graphite Black (#181717) for primary text and Storm Gray (#2e2d2d) for secondary body copy and subheadings.
- Apply Canvas Pale (#eeebea) as the default background for most page sections, and Canvas White (#ffffff) for card surfaces and modals.
- Utilize Action Red (#d04841) exclusively for primary action buttons and unambiguous active states; avoid its use for mere decoration.
- Employ soft shadow rgba(24, 23, 23, 0.02) 0px 4px 8px 0px for cards and elevated elements to provide subtle depth.
- Maintain a default border-radius of 16px for cards and 8px for buttons; use 9999px for pill-shaped elements like tags.
- Ensure consistent element spacing of 12px and card padding of 24px to maintain a comfortable density.

### Don't

- Do not introduce new vibrant colors beyond Action Red (#d04841) or the Action Blue Gradient (#5a82de).
- Avoid using hard borders on cards or primary UI elements; prefer a subtle shadow or light border for separation.
- Do not deviate from the established letter-spacing values, especially the negative tracking for Inter and positive tracking for MDIO.
- Do not use dark backgrounds for large content sections unless it's a specific, contained content panel with a distinct purpose.
- Do not clutter layouts; maintain ample white space around elements and between sections.
- Avoid heavy drop shadows or multiple layers of shadows; stick to the defined soft shadow for elevation.
- Do not use MDIO font for body text or large blocks of content; it is for specific, short labels or code.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
text: #181717
background: #eeebea
border: #d5d3d2
accent: #5a82de
primary action: #d04841 (filled action)

**Example Component Prompts:**
1. Create a Primary Filled Button: background Action Red (#d04841), text Canvas White (#FFFFFF), 8px radius, 10px vertical 12px horizontal padding.
2. Create a Feature Card: background Canvas White (#FFFFFF), 16px radius, shadow rgba(24, 23, 23, 0.02) 0px 4px 8px 0px, 32px padding, with a heading using Inter 32px weight 600 Graphite Black (#181717).
3. Create a Secondary Ghost Button: transparent background, text Storm Gray (#2e2d2d), 1px solid Canvas Pale (#eeebea) border, 16px radius, 24px padding.

---
_Source: https://styles.refero.design/style/5b679fb6-8d53-402d-a77b-c88bfb397623_
