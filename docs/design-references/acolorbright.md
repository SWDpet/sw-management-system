# Acolorbright — Design Reference

> Black-on-white architectural blueprint.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://acolorbright.com](https://acolorbright.com) |
| Refero page | [https://styles.refero.design/style/b1ec0888-1dbe-439c-bccd-ba2c39effb70](https://styles.refero.design/style/b1ec0888-1dbe-439c-bccd-ba2c39effb70) |
| Theme | light |
| Industry | agency |

## Overview

Acolorbright employs a sophisticated, monochromatic design palette, focusing on a stark contrast between near-black text and crisp white surfaces, occasionally interrupted by subtle gray accents. Typography is compact and deliberate, using a single proprietary sans-serif family with precise letter-spacing to optimize information density. Components are lightweight, favoring thin borders and minimal padding, contributing to an open, breathable layout that prioritizes content display over heavy decorative elements.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#1a1a1a` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Graphite | `#666666` | neutral | Muted body text, secondary information, subtle backgrounds and borders |
| Storm Gray | `#999999` | neutral | Decorative text, inactive navigation items, faint borders |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, button backgrounds, primary navigation text |
| Snowdrift | `#f2f2f2` | neutral | Subtle background for list items or grouped content, providing slight differentiation from Canvas White |
| Onyx | `#000000` | neutral | Icon fills, specific button text against lighter backgrounds, heavy borders |
| Charcoal Button | `#262626` | neutral | Solid button background for specific calls to action, providing strong contrast |
| Ghost Border | `#e6e6e6` | neutral | Light border for ghost buttons and subtle separations |
| Case Study Violet | `#6a00ff` | accent | Accent color for case study cards — a vibrant visual identifier |
| Case Study Lime | `#a3ff00` | accent | Accent color for case study cards — a vibrant visual identifier |
| Swiper Blue | `#007aff` | accent | Internal UI accent for components like carousels (swiper theme) |

## Typography

### RiformaLLWeb

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 16px, 18px, 24px, 40px, 64px |
| lineHeight | 1.00, 1.10, 1.15, 1.20, 1.25 |
| letterSpacing | -0.01em |
| fontFeatureSettings | "liga" 0 |
| substitute | Inter |
| role | The primary typeface for all textual content, from body to headlines. Its compact forms and subtle negative letter-spacing contribute to an efficient, modern aesthetic. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| role | Used for specific, small informational text, possibly in legacy components or specific UI elements where system font fallback is preferred. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1 | -0.12 |
| body | 16 |  | 1.2 | -0.16 |
| subheading | 18 |  | 1.2 | -0.18 |
| heading | 24 |  | 1.15 | -0.24 |
| heading-lg | 40 |  | 1.1 | -0.4 |
| display | 64 |  | 1 | -0.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| image | 12px |
| lists | 8px |
| buttons | 8px |
| default | 12px |
| navigation | 8px |
| interactiveIcon | 16px |

- **elementGap** — 12px
- **sectionGap** — 32px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background and default card surface. |
| Snowdrift | `#f2f2f2` | 1 | Subtlety elevated background for list items or distinct content sections. |
| Charcoal Button | `#262626` | 2 | Background for focused interactive elements like primary buttons. |

## Components

### Outline Ghost Button

**Role:** Secondary action or informational button.

Text in Graphite (#999999) or Onyx (#000000) on a transparent background, with a 1px border of Ghost Border (#e6e6e6) or Midnight Ink (#1a1a1a). Padding is 1px top/bottom, 6px left/right. Radius is 8px.

### Solid Dark Button

**Role:** Primary Call to Action button.

Background Charcoal Button (#262626) with text in Storm Gray (#999999). Padding is 12px top/bottom, 16px left/right. Radius is 8px.

### Navigation Link

**Role:** Main navigation items.

Text in Midnight Ink (#1a1a1a) or Canvas White (#ffffff). Interactivity may involve a border of Midnight Ink (#1a1a1a) or Canvas White (#ffffff). Radius is 8px for the containing element, potentially 16px for interactive icons.

### Informational Card

**Role:** Content grouping, such as case studies or feature descriptions.

Transparent background with no distinct box-shadow. Radius is 12px. Padding is implicitly flexible depending on content, often 0px, with internal element gaps typically 4px or 8px.

### Case Study Thumbnail

**Role:** Visual representation for portfolio items.

Contains an image or solid color background (e.g., Case Study Violet #6a00ff, Case Study Lime #a3ff00) with a 12px border radius. Often appears without explicit padding.

## Layout

The page adheres to a centered maximum width with responsive margins, providing a clean frame for the content. The hero section introduces a prominent, centered headline over a white background, setting a calm, informative tone. Content sections primarily follow a stacked, linear flow with consistent vertical spacing. Feature and portfolio sections often employ a grid layout, typically three columns for case study thumbnails, with alternating visual styles (product screenshots vs. vibrant color blocks). Navigation is a sticky top bar with a minimal hamburger menu icon, suggesting an uncluttered user experience.

## Imagery

Imagery primarily consists of high-quality product screenshots, often presented within mockups of devices, along with abstract branding visuals for case studies. Photography is rare and appears to be candid, event-focused, or lifestyle-oriented when used, with a bright, natural color treatment. Illustrations are minimal, tending towards abstract, geometric shapes for branding elements with flat, filled styles using the accent colors. Icons are outlined, monochromatic (typically black or dark gray), and serve a functional rather than decorative role, maintaining a consistent stroke weight. The overall density is balanced, allowing significant text content to coexist with visual examples, but imagery is always contained and never full-bleed.

## Dos & Donts

### Do

- Prioritize RiformaLLWeb at 400 weight for all text elements to maintain a consistent tone, using negative letter-spacing of -0.01em.
- Use Midnight Ink (#1a1a1a) for primary text and headings, offering high contrast against Canvas White (#ffffff) backgrounds.
- Employ Canvas White (#ffffff) as the dominant background for all main content areas and cards, creating an open, bright feel.
- Utilize an 8px border-radius for buttons and navigation elements, and a 12px radius for larger content blocks like images or general containers.
- Maintain a comfortable density with an `elementGap` of 12px for vertical rhythm between small items and `sectionGap` of 32px for major content blocks.
- Subtly differentiate background layers using Snowdrift (#f2f2f2) for list items or grouped content, distinct from the primary Canvas White.
- Use thin borders (1px) in Midnight Ink (#1a1a1a) or Ghost Border (#e6e6e6) to define interactive elements or subtle card outlines.

### Don't

- Avoid using highly saturated colors for large background areas; reserve vivid hues like #6a00ff or #a3ff00 for selective accents in case study blocks or graphics.
- Do not deviate from the RiformaLLWeb font family for core interface text; Arial is for specific, small utility text only.
- Do not introduce heavy box shadows or gradients for elevation; maintain a flat aesthetic, using slight background color differentiation for depth when needed.
- Avoid excessive padding within cards; prefer a light touch to keep content compact and allow for generous negative space between components.
- Do not use highly contrasting or varied button styles for primary actions; stick to Solid Dark Button (#262626) or the subtly outlined ghost variants.
- Never use text colors lighter than Graphite (#666666) for body text on Canvas White (#ffffff) backgrounds, as readability will be compromised.
- Do not introduce complex, multi-color icon systems; keep icons monochromatic, typically in Onyx (#000000) or Midnight Ink (#1a1a1a).

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #1a1a1a
background: #ffffff
border: #1a1a1a
accent: #6a00ff
primary action: no distinct CTA color

Example Component Prompts:
1. Create a primary headline: 'We’re a brand and product design studio.' using RiformaLLWeb, 64px, weight 400, color Midnight Ink (#1a1a1a), lineHeight 1.0, letterSpacing -0.64px.
2. Create a secondary information paragraph: 'Leading brands choose us.' using RiformaLLWeb, 16px, weight 400, color Graphite (#666666), lineHeight 1.2, letterSpacing -0.16px.
3. Design a Solid Dark Button: Background Charcoal Button (#262626), text Storm Gray (#999999), 12px vertical padding, 16px horizontal padding, 8px border-radius, using RiformaLLWeb 16px weight 400 text.
4. Construct an Informational Card with a Case Study Thumbnail: A 12px border-radius container with a transparent background, containing a graphic with a #6a00ff background and 12px radius. Above the graphic, a descriptive text in RiformaLLWeb 16px, weight 400, color Midnight Ink (#1a1a1a).

---
_Source: https://styles.refero.design/style/b1ec0888-1dbe-439c-bccd-ba2c39effb70_
