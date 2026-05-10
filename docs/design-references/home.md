# Home — Design Reference

> Warm Canvas, Restrained Focus

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.useparallel.com](https://www.useparallel.com) |
| Refero page | [https://styles.refero.design/style/2230ba53-445e-411d-b483-16410a072639](https://styles.refero.design/style/2230ba53-445e-411d-b483-16410a072639) |
| Theme | light |
| Industry | productivity |

## Overview

Parallel presents a subdued, almost contemplative design system: a predominantly light, warm-toned canvas serves as a backdrop for high-contrast dark text and interactive elements. Product surfaces and cards are subtly shaded or near-transparent, using elevation sparingly to create focus. Typography is confident and clear, balancing a custom headline font with a versatile sans-serif. The overall feeling is one of considered calm, where interactions are precise rather than flashy, allowing content and functionality to take precedence.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Sand | `#e4dfd9` | neutral | Page backgrounds, large section fills |
| White Cloud | `#ffffff` | neutral | Elevated card surfaces, input backgrounds, active button fills, card backgrounds |
| Midnight Ink | `#000000` | neutral | Primary text, strong headings, borders for ghost buttons and icons |
| Slate Gray | `#737373` | neutral | Secondary text, muted headings, helper text |
| Dark Onyx | `#050505` | neutral | Heading text, robust body copy |
| Ash Mist | `#c7c7c7` | neutral | Subtle background surfaces, decorative elements |
| Charcoal Tone | `#4b4b4b` | neutral | Muted links, ghost button text, decorative accents |
| Deep Plum | `#171717` | neutral | Filled button backgrounds for primary actions |
| Amber Pop | `#ffc42c` | accent | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |

## Typography

### Rules Font

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 28px, 69px |
| lineHeight | 1.10, 1.40 |
| letterSpacing | -0.0200em |
| substitute | Playfair Display |
| role | Brand headlines — its unique character at larger sizes defines the brand's voice |

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 14px, 16px, 19px, 23px |
| lineHeight | 1.20, 1.40, 1.50 |
| letterSpacing | -0.0200em |
| substitute | Inter |
| role | General UI text, body copy, navigation, and input fields — prioritizing legibility and clarity |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.2 | -0.28 |
| body | 16 |  | 1.4 | -0.32 |
| subheading | 19 |  | 1.4 | -0.38 |
| heading-sm | 23 |  | 1.4 | -0.46 |
| heading | 28 |  | 1.1 | -0.56 |
| display | 69 |  | 1.1 | -1.38 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 20px |
| inputs | 0px |
| buttons | 12px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 32px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Sand | `#e4dfd9` | 0 | Dominant page background |
| White Cloud | `#ffffff` | 1 | Elevated cards, main information containers |
| Ash Mist | `#c7c7c7` | 2 | Subtle background surfaces, decorative elements that are not interactive |
| Deep Plum | `#171717` | 3 | Interactive elements: button backgrounds, search bar backgrounds in hero |

## Components

### Ghost Navigation Button

**Role:** Header navigation, secondary actions

Transparent background, White Cloud text at 0.88 opacity, subtle 10px 18px horizontal padding, 10px radius.

### Talent/Employers Toggle

**Role:** Segmented control or primary category selection

Background #171717 (Deep Plum), medium weight text (likely 500 or 600), 9999px radius for pill shape, 10px 18px padding. The active state has a White Cloud background with Charcoal Tone text and 12px border radius.

### Primary Action Button (Filled)

**Role:** Main calls to action

Solid #171717 (Deep Plum) background, White Cloud text, 12px border radius to soften edges, variable padding 10px 18px for interaction states. Example 'Read more'.

### Search Input (Hero)

**Role:** Prominent search fields

Transparent background with White Cloud text at 0.9 opacity, 0px border radius (sharp top/bottom edge), likely a subtle bottom border or shadow for definition.

### Feature Card

**Role:** Content grouping, feature display

White Cloud background, 20px border radius for soft corners, no explicit border visible but uses rgba(0, 0, 0, 0.07) 0px 6px 27px 0px shadow for lift, 0px 0px 32px 0px inner padding for bottom spacing.

### Soft Highlight Button

**Role:** Interactive elements with soft visual emphasis

rgba(255, 255, 255, 0.96) background, rgba(5, 5, 5, 0.72) text, 12px border radius, no padding in variant data (implying text only or inherited).

## Layout

The layout is primarily a max-width 1200px centered container, with the hero section often being full-bleed. The hero initially features a large, dark-toned background image overlaid with white, centered headlines and a prominent input field. Subsequent sections employ a consistent vertical rhythm with minimal visual dividers, mostly implied by alternating subtle background shades (Canvas Sand and White Cloud for cards). Content is often arranged in a 2-column grid for text-image pairings or a 3-column card grid for features. The navigation is a sticky top bar with logo, ghost links, and a distinctive pill-shaped toggle for 'Talent'/'Employers'.

## Imagery

The site predominantly uses dramatic, high-contrast photography in its hero sections, often with a desaturated or single-tone filter applied, focusing on human subjects in motion or contemplation. Product screenshots are contained within White Cloud cards, typically showing detailed UI elements with clean, sharp edges and minimal context. Icons are minimal, either solid or outlined, often in black or white. Imagery serves both decorative atmosphere and explanatory content, balancing large-scale emotional impact with precise product showcases.

## Dos & Donts

### Do

- Prioritize a Canvas Sand background for main content areas, with White Cloud for elevated cards and feature blocks.
- Use Rules Font weight 500 for all major headlines and titles (69px, 28px) with -0.0200em letter-spacing to maintain brand voice.
- Apply 12px border radius to all interactive buttons and soft highlight elements, and 20px radius for all cards, for a consistent soft-edged feel.
- Ensure primary text is set in Midnight Ink or Dark Onyx, with secondary information and helper text using Slate Gray or Charcoal Tone.
- Utilize a subtle rgba(0, 0, 0, 0.07) 0px 6px 27px 0px shadow on cards and elevated elements to provide depth without harshness.
- Maintain a comfortable information density, with 32px vertical padding on card elements and a consistent 8px element gap within components.

### Don't

- Avoid strong, saturated colors for UI elements; color should only be used as a subtle accent or functional indicator.
- Do not use sharp, 0px border radii on interactive buttons, as the system favors rounded corners for interactives.
- Do not introduce heavy borders or strong gradients on cards or surfaces; the aesthetic relies on subtle elevation and clean backgrounds.
- Avoid dense, compact text blocks; ensure generous line heights and paragraph spacing to maintain readability.
- Do not use dark backgrounds for main content sections outside of the hero, as the system is primarily light-themed.
- Do not rely on outline or ghost buttons for primary calls to action; use the Deep Plum filled button instead.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #000000 (Midnight Ink)
- background: #e4dfd9 (Canvas Sand)
- border: #000000 (Midnight Ink)
- accent: #ffc42c (Amber Pop)
- primary action: no distinct CTA color

### 3-5 Example Component Prompts
- Create a hero section: Canvas Sand background fading visually into a desaturated human photo. Centered headline 'Find your life's work' at 69px Rules Font weight 500, Midnight Ink, letter-spacing '-0.0200em'. Subtext 'Land a job that feels like freedom.' at 19px ui-sans-serif weight 400, Slate Gray. Below this, a search input with transparent background, White Cloud text (0.9 opacity), 0px radius. A 'Read more' button immediately below the input: Deep Plum background, White Cloud text, 12px radius, 10px 18px padding.
- Create a feature card: White Cloud background, 20px border radius, shadow rgba(0, 0, 0, 0.07) 0px 6px 27px 0px. Heading 'Hyper personal matches' at 23px ui-sans-serif weight 500, Dark Onyx. Body text 'No fake jobs, just fresh direct roles...' at 16px ui-sans-serif weight 400, Slate Gray. Bottom internal padding 32px.
- Create a navigation bar: Midnight Ink logo. Ghost navigation buttons ('Explore', 'Sign in') with transparent background, White Cloud text (0.88 opacity), 10px 18px padding, 10px radius. Segmented toggle ('Talent', 'Employers') in Deep Plum with White Cloud text, 9999px radius and 10px 18px padding for each segment.

---
_Source: https://styles.refero.design/style/2230ba53-445e-411d-b483-16410a072639_
