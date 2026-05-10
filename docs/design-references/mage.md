# Mage — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.mage.ai](https://www.mage.ai) |
| Refero page | [https://styles.refero.design/style/ba07accb-b2cc-4ad9-a25f-c50b0f90f34e](https://styles.refero.design/style/ba07accb-b2cc-4ad9-a25f-c50b0f90f34e) |
| Theme | light |
| Industry | ai |

## Overview

Mage.ai presents a clear, functional interface resembling an architectural blueprint on a light canvas. The visual style prioritizes information and direct interaction through subtle surface variations and a single primary blue accent color. Typography is precise and organized, creating clear hierarchy without excessive embellishment. Components are lightweight with defined edges and moderate rounded corners, contributing to an overall structured and modern feel.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button backgrounds, primary text on dark elements |
| Greige Canvas | `#f7f7f1` | neutral | Subtle background for alternating sections and secondary card surfaces, providing visual separation without strong contrast |
| Ink Black | `#000000` | neutral | Primary text, headings, icons, and strong borders. Provides high contrast |
| Storm Gray | `#2b2b2b` | neutral | Secondary text, subheadings, and muted borders |
| Silver Mist | `#b0b0b0` | neutral | Tertiary text, helper text, and inactive UI elements. Soft, low-contrast information |
| Ocean Blue | `#244cff` | brand | Primary action buttons, active links, and interactive elements – a clear signal for user engagement |
| Sky Tint | `#e8f8ff` | accent | Light background for informational cards or highlighted sections, adding a subtle touch of color |
| Pale Aqua | `#d6f2ff` | accent | Background for specific card variants or content blocks |
| Lavender Haze | `#c3aeff` | accent | Decorative illustration accents, adding a soft, colorful touch to graphics |
| Soft Yellow | `#ffffbd` | accent | Background for specific card variants or content blocks |
| Bright Cyan Gradient | `#e3e8ff` | accent | Subtle background gradient for hero sections or prominent feature blocks, creating a soft, luminous feel |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 13px, 15px, 16px, 17px, 18px, 21px, 22px, 24px |
| lineHeight | 1.20, 1.30, 1.40, 1.50, 1.65 |
| letterSpacing | normal |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| substitute | system-ui, sans-serif |
| role | Primary typeface for all body text, links, and most headings. Its varied weights and legibility provide a robust foundation for content hierarchy. |

### Inter Variable

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 20px, 22px, 30px, 38px, 60px |
| lineHeight | 1.30, 1.40 |
| letterSpacing | normal |
| substitute | Inter, system-ui, sans-serif |
| role | Used for larger headings and display text, allowing for granular control over sizing and giving a modern, slightly expanded feel without being overly decorative. |

### Geist

| Key | Value |
| --- | --- |
| weight | 600 |
| weights | 600 |
| sizes | 14px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| substitute | Inter, system-ui, sans-serif |
| role | A distinct, heavier weight for specific callouts or labels, providing emphasis and a subtly different textual texture from the main Inter body. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.5 |  |
| heading-sm | 22 |  | 1.4 |  |
| heading | 30 |  | 1.4 |  |
| heading-lg | 38 |  | 1.3 |  |
| display | 60 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 6px |
| pills | 100px |
| buttons | 17px |
| default | 6px |
| smallComponents | 16px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Primary page background and default card surface. |
| Greige Canvas | `#f7f7f1` | 2 | Secondary page and section backgrounds, subtly differentiating content blocks. |
| Sky Tint | `#e8f8ff` | 3 | Highlight backgrounds for specific informational cards or feature areas. |
| Accent Card Colors | `#d6f2ff` | 4 | Varied background for unique card variants or decorative elements within content. |

## Components

### Primary Action Button

**Role:** Call-to-action button for initiating key user flows.

Filled button with `Ocean Blue` background, `Canvas White` text. `Inter` font, bold (`600` weight), `16px` size. `17px` border radius creating a slightly rounded rectangular shape. `12px` vertical padding, `26px` horizontal padding.

### Ghost Secondary Button

**Role:** Secondary action button, providing a less prominent option.

Ghost button with `Canvas White` background and `Ink Black` border and text. `Inter` font, bold (`600` weight), `16px` size. `17px` border radius. `12px` vertical padding, `26px` horizontal padding.

### Default Content Card

**Role:** General purpose card for grouping content and features.

Card with `Greige Canvas` background, `6px` border radius, no shadow. `40px` padding on all sides. Content uses `Ink Black` for headings and `Storm Gray` for body text.

### Compact Info Card

**Role:** Smaller card for lists or minor content blocks.

Card with `Greige Canvas` background. `8px` border radius, no shadow. `16px` padding on all sides.

### Highlight Card

**Role:** Card with a subtle accent colored background.

Card with `Sky Tint` background, `6px` border radius, no shadow. `40px` padding on all sides. Primarily used for informational prompts or feature highlights.

### Feature Card with Mixed Backgrounds

**Role:** Modular cards for distinctive feature presentation, using various accent colors.

Cards with `d6f2ff` (Pale Aqua), `ffffbd` (Soft Yellow), `294dba` (dark violet), `ba9ffc` (bright violet), `fcc2cd` (muted red), `fced9f` (moderate yellow), `3388ff` (blue) backgrounds. All use `6px` border radius and `40px` padding. This provides a diverse palette for feature differentiation.

## Layout

The layout is primarily a contained, centered `max-width` structure with `Greige Canvas` (#f7f7f1) as the dominant background for body content, interspersed with `Canvas White` (#ffffff) sections. The hero features a split design: prominent centered headline and body text left, and a large, colorful, organic illustration on the right. Content sections generally alternate between `Canvas White` and `Greige Canvas` backgrounds, with consistent vertical `40px` section gaps. Text-heavy blocks often appear alongside product screenshots in a two-column `text-left/image-right` arrangement. Navigation is a sticky top bar with `Ink Black` text and a `Ghost Secondary Button` alongside a `Primary Action Button` for key actions. A grid of company logos is utilized for social proof, maintaining the centered content structure.

## Imagery

The site uses a mix of flat, slightly dimensional illustrations and high-fidelity product screenshots. Illustrations feature organic shapes, often depicting stylized characters working with abstract data elements, using bright, moderate accent colors like `Lavender Haze`. Product screenshots are dark-themed UI captures, placed within rounded-corner device mocks, acting as literal content anchors. Imagery is contained within cards or defined sections, never full-bleed, and serves both decorative and explanatory roles. Icons are predominantly filled and monochromatic, consistent with the `Ink Black` or `Silver Mist` palette, indicating a functional approach.

## Dos & Donts

### Do

- Always use `Ink Black` (#000000) for primary headlines and body text to ensure maximum readability and brand consistency.
- Utilize `Greige Canvas` (#f7f7f1) for alternating section backgrounds and `Canvas White` (#ffffff) for primary page backgrounds and cards to create clear visual separation.
- Reserve `Ocean Blue` (#244cff) exclusively for primary call-to-action button backgrounds and active states to guide user interaction.
- Apply `6px` border radius for generic cards and content containers, `17px` for buttons, and `100px` for pill-shaped elements.
- Employ `10px` as the default `elementGap` for horizontal and vertical spacing between small UI elements, establishing a comfortable density.
- Maintain `40px` padding within cards and for major section gaps to give content adequate breathing room.
- Use `Inter Variable` at larger sizes for headings to leverage its precise shaping and expressive range.

### Don't

- Do not use `Ocean Blue` (#244cff) for decorative elements or non-interactive text; its purpose is strictly functional for actions.
- Avoid arbitrary color choices for backgrounds; stick to `Canvas White`, `Greige Canvas`, `Sky Tint`, or `Pale Aqua` for content surfaces.
- Do not use letter-spacing on body text or standard links; maintain `normal` letter-spacing for these elements.
- Do not introduce sharp corners or excessively large radii; adhere to the `6px`, `17px`, and `100px` radii for a consistent, approachable feel.
- Avoid cluttering the layout; maintain `40px` section gaps and `10px` element gaps for comfortable content presentation.
- Do not use generic system fonts for critical UI elements if `Inter` or `Geist` are available, as they define the brand's typographic tone.
- Do not deviate from the established type scale; respect predefined `size` and `lineHeight` for each text role to maintain hierarchy.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #000000
- background: #ffffff
- border: #000000
- accent: #c3aeff
- primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
- Create a `Default Content Card` with the headline 'Power analytics...' at `heading-sm` size, `Ink Black` text, and body copy at `body` size, `Storm Gray` text. The card should have `40px` padding and sit on a `Greige Canvas` background.
- Create a `Highlight Card` with a heading like 'Key Feature' at `subheading` size, `Ink Black` text, and body copy at `body` size, `Storm Gray` text, using a `Sky Tint` background. The card padding should be `40px` and radius `6px`.

---
_Source: https://styles.refero.design/style/ba07accb-b2cc-4ad9-a25f-c50b0f90f34e_
