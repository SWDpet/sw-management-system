# Aboard — Design Reference

> Warm earth against dark steel

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://aboard.com](https://aboard.com) |
| Refero page | [https://styles.refero.design/style/7b083729-e694-4b66-82a3-befb08451722](https://styles.refero.design/style/7b083729-e694-4b66-82a3-befb08451722) |
| Theme | dark |
| Industry | ai |

## Overview

Aboard projects a grounded, sophisticated competence with a dark canvas, warm earthy accents, and a distinctive serif typeface for headlines. Monochrome textures and subtle borders create visual depth and structure, ensuring content feels substantial without being heavy. The design balances a serious, technical impression with approachable elements, using bold orange as a precise highlight for interactive elements and brand accents.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ember Glow | `#de4c00` | brand | Primary action backgrounds, link accents, functional icons — a vivid, warm orange that cuts through the dark palette |
| Peach Fuzz | `#efa680` | brand | Orange outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Deep Forest | `#040c06` | neutral | Primary page background, extensive dark surfaces, and text on light surfaces |
| Warm Black | `#271503` | neutral | Main text color on light parchment backgrounds, subheadings, and muted body text on dark backgrounds |
| Parchment | `#eadfcf` | neutral | Accent backgrounds for cards and highlighted content sections. Also used for some body and heading text on dark backgrounds |
| Stone Slate | `#a49784` | neutral | Subtle background for navigation and secondary UI elements |
| Light Steel | `#e5e7eb` | neutral | Border colors for inputs, cards, and image outlines. Used widely for structural delineation |
| White Canvas | `#fffefb` | neutral | Prominent surface accents and primary text color on dark backgrounds, offering high contrast |
| Pale Gray | `#c1bdba` | neutral | Subtle border color, distinct from Light Steel, for layered elements |
| True Black | `#000000` | neutral | Used for text over light backgrounds and occasional structural outlines |

## Typography

### Tobias

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 24px, 32px, 72px |
| lineHeight | 1.10, 1.30 |
| letterSpacing | -0.48, 0.48 |
| substitute | Playfair Display |
| role | Display and major headings — its elegant structure and light 300 weight command attention without shouting, providing sophistication. |

### Atkinson Hyperlegible Mono

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 10px, 12px, 14px, 16px |
| lineHeight | 1.57, 1.60, 1.63, 1.83 |
| letterSpacing | -0.2, 0.5 |
| substitute | IBM Plex Mono |
| role | Specialized text, code snippets, and certain list items — its monospaced nature anchors technical references and offers a distinct textural contrast. |

### Work Sans

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 16px |
| lineHeight | 1.63, 1.83 |
| letterSpacing | -0.2, -0.17 |
| substitute | Inter |
| role | Navigation, body text, and button labels — a clean, legible workhorse that maintains consistency across interactive elements. |

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.50 |
| letterSpacing | 0 |
| substitute | system-ui |
| role | Default system font for general UI elements, ensuring broad compatibility and accessibility for supplementary text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.57 | 0.5 |
| body | 16 |  | 1.83 | 0 |
| heading-sm | 24 |  | 1.3 | 0.48 |
| heading | 32 |  | 1.1 | -0.48 |
| display | 72 |  | 1.1 | -0.48 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| links | 8px |
| buttons | 9999px |
| navigation | 30px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Forest Canvas | `#040c06` | 0 | Primary page and main section backgrounds. |
| Stone Slate Base | `#a49784` | 1 | Subtle background for persistent elements like navigation bars. |
| Parchment Highlight | `#eadfcf` | 2 | Accent backgrounds for cards, embedded forms, or informational panels, providing visual pop against the primary dark background. |

## Components

### Primary Action Button

**Role:** Calls to action that drive core user flow.

Filled with Ember Glow (#de4c00) background and White Canvas (#fffefb) text. Features 9999px border-radius for a pill shape and 10px vertical, 16px horizontal padding.

### Parchment Card Button

**Role:** Secondary action or informational button within content areas.

Filled with Parchment (#f3f2ee) background and True Black (#000000) text. Features 8px border-radius and 24px padding on all sides.

### Navigation Link

**Role:** Top-level navigation items.

Text in Work Sans, weight 400. Text color is White Canvas (#fffefb). Active states may show an Ember Glow accent. Padding is 6px vertical, 16px horizontal.

### Image Card

**Role:** Container for imagery, often with textual overlay.

Features a 1px Light Steel (#e5e7eb) border. Image content is often contained with crisp borders.

### Hero Headline

**Role:** Prominent, attention-grabbing titles for key sections.

Utilizes Tobias font, weight 300, typically at 72px with a letter-spacing of -0.48px. Text color is White Canvas (#fffefb).

## Layout

The page primarily uses a full-bleed layout, particularly for hero sections and full-width image displays. Content often centers with implied wide margins, suggesting a max-width approach despite the full-bleed visual. The hero pattern features a full-viewport image with a large, centered headline in White Canvas (#fffefb) and a prominent Ember Glow (#de4c00) call-to-action. Section rhythm alternates between full-bleed imagery and content blocks with generous vertical spacing (64px sectionGap). Content is arranged in alternating text-left, image-right patterns or as stacked, centered text blocks. Navigation is a sticky top bar, minimally styled, with the Ember Glow Primary Action Button highlighted.

## Imagery

This site features a blend of high-definition, somewhat desaturated photography and product-focused visuals. Photography (warehouses, office environments) generally serves as full-bleed, atmospheric backgrounds, often slightly darkened or tinted to allow text overlays. Product-related imagery appears as contained, crisp visuals with subtle Light Steel (#e5e7eb) borders. The treatment is functional and explanatory, aiming for clarity and a sense of scale, rather than highly stylized or abstract graphics. Icons appear as simple, functional elements, often filled with Ember Glow (#de4c00) or White Canvas (#fffefb).

## Dos & Donts

### Do

- Use Deep Forest (#040c06) as the default background for large page sections to maintain the system's dark theme.
- Apply Ember Glow (#de4c00) exclusively for primary interactive elements, ensuring its impact as a functional accent.
- Employ the Tobias font for all primary headlines, leveraging its elegant 300 weight at larger sizes (e.g., 72px display, 32px heading) for a distinctive brand voice.
- Maintain comfortable element spacing with an 8px base unit using 8px elementGap for small separations and 64px for sectionGap between major content blocks.
- Use 9999px border-radius for all primary buttons to achieve a consistent pill-shaped aesthetic.
- Structure content with clear, subtle delineation using Light Steel (#e5e7eb) borders, especially around interactive and visual components.
- Incorporate Parchment (#eadfcf) backgrounds for highlighted informational cards or secondary content blocks to introduce a warm neutral contrast.

### Don't

- Avoid using Ember Glow (#de4c00) for purely decorative purposes; reserve it for actionable elements to prevent visual noise.
- Do not deviate from the specified font families or their prescribed weights and letter spacing; unique type choices define the brand's readability and tone.
- Refrain from introducing arbitrary background colors; stick to the approved color palette and surface hierarchy to maintain visual coherence.
- Do not use hard, sharp angles for interactive elements; buttons and links should adhere to the established radii of 8px or 9999px.
- Avoid using multiple chromatic colors in close proximity; the design relies on a single accent hue to guide user attention.
- Do not assume default system font usage for body text; Work Sans should be preferred for readability in content areas.
- Do not overcrowd sections; utilize the 64px sectionGap to provide ample breathing room between major content blocks.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #fffefb
background: #040c06
border: #e5e7eb
accent: #de4c00
primary action: #de4c00 (filled action)

Example Component Prompts:
1. Create a Hero Section: Deep Forest (#040c06) background with a full-bleed, desaturated photograph. Overlay a 72px Tobias font (weight 300, #fffefb, letter-spacing -0.48px) headline. Below it, add a primary action button (Ember Glow #de4c00 background, White Canvas #fffefb text, 9999px radius, 10px vertical 16px horizontal padding).
2. Design a Testimonial Card: Parchment (#eadfcf) background with 8px radius. Use Warm Black (#271503) for body text in Work Sans, weight 400. Include a subtle 1px Light Steel (#e5e7eb) border at its base.
3. Create a Primary Action Button: #de4c00 background, #fffefb text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
4. Craft a Feature Block: Deep Forest (#040c06) background. Apply a heading with Tobias font, 32px, weight 300, and White Canvas (#fffefb) text. Content features Atkinson Hyperlegible Mono for emphasized points and Work Sans for body text, both in White Canvas (#fffefb).

---
_Source: https://styles.refero.design/style/7b083729-e694-4b66-82a3-befb08451722_
