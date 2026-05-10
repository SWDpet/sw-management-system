# Bibliothèque — Design Reference

> Monochrome Editorial Command: Pure black and white, sharp text, and border-defined interactions create an atmosphere of focused, high-contrast authority.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://bibliothequedesign.com](https://bibliothequedesign.com) |
| Refero page | [https://styles.refero.design/style/5acff005-1871-4237-bc25-cdddf50edc70](https://styles.refero.design/style/5acff005-1871-4237-bc25-cdddf50edc70) |
| Theme | dark |
| Industry | design |

## Overview

Bibliothèque embraces a stark, high-contrast dark mode aesthetic where pure black canvas meets crisp white typography. The design emphasizes content-forward presentation with minimal UI distraction, relying on a restrained typographic hierarchy and ample negative space to define structure. Interactions are subtle, often border-based, maintaining the monochrome integrity, with a focus on presenting strong editorial content without overt visual flair.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Noir | `#000000` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |
| Canvas White | `#ffffff` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |

## Typography

### Unica77 LL

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 14px, 18px, 80px |
| lineHeight | 1.00, 1.20, 1.22, 1.33 |
| letterSpacing | normal |
| substitute | Helvetica Neue |
| role | The singular typeface used across all textual elements, from small body text to large headings and navigation, contributing to a unified, disciplined aesthetic. Its consistent weight maintains a calm, authoritative voice. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.22 |  |
| body-sm | 14 |  | 1.22 |  |
| body | 18 |  | 1.22 |  |
| body-lg | 80 |  | 1.22 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| none | 0px |

- **elementGap** — 5px
- **sectionGap** — 47px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Noir Canvas | `#000000` | 0 | The primary background for all page content and base for components. |

## Components

### Project Card

**Role:** Displaying project thumbnails and titles.

A frameless, borderless container with `0px` border-radius and `0px` padding, designed for full-bleed content presentation. Background is transparent, letting the `Midnight Noir` canvas show through. Text is `Canvas White` using `Unica77 LL`.

### Navigation Link

**Role:** Interactive menu items in the main navigation.

Text rendered in `Canvas White` using `Unica77 LL` at `18px`, with `15px` horizontal padding. No background or explicit border, relying on hover states (not extracted) for interaction feedback.

## Layout

The page adheres to a full-bleed layout, where content spans the entire viewport width, creating an immersive experience. The hero section, if present, is likely a full-width block with centered typographically-driven content. Section rhythm is driven by consistent vertical spacing, creating distinct yet seamlessly flowing content blocks without overt dividers. Content arrangement primarily appears to be vertical stacks of text, with projects perhaps arranged in a grid that respects the `0px` card padding. Navigation is a minimal top bar, suggesting a subtle, always-present control without being intrusive.

## Imagery

The site's visual language is characterized by an almost complete absence of decorative imagery; instead, it uses the UI itself as the primary visual element. When present, images are content-driven project showcases, likely presented without additional borders or visual treatments, blending seamlessly into the dark canvas. Icons are minimal, likely monochromatic line icons if any, maintaining the stark aesthetic. The density is extremely text-dominant, allowing information and typography to command attention.

## Dos & Donts

### Do

- Maintain a strict monochrome palette using only `Midnight Noir` (#000000) and `Canvas White` (#ffffff).
- Utilize `Unica77 LL` weight 400 for all typography, varying only size and line-height for hierarchy.
- Implement `0px` border-radius for all elements, maintaining a sharp, angular aesthetic.
- Prioritize generous negative space; the canvas is a prominent design element.
- Use subtle border changes or text color shifts for interactive states, avoiding fills or robust shadows and leveraging `ACTION_BORDER` colors where appropriate.
- Structure content with clear, compact sections and consistent `5px` element gaps.

### Don't

- Introduce any saturated or chromatic colors; the system is strictly achromatic.
- Use any custom border-radius values other than `0px`.
- Apply elevation via box-shadows; the design favors a flat aesthetic.
- Employ multiple font families or weights beyond `Unica77 LL` 400.
- Break the consistent vertical rhythm provided by the `5px` element gap and other specified spacing tokens.
- Use large, decorative imagery that distracts from the core content and typographic focus.

## Notes

### Agent Prompt Guide

Quick Color Reference: text: #ffffff, background: #000000, border: #ffffff, accent: no distinct accent color, primary action: no distinct CTA color

Example Component Prompts:
Create a header navigation link: text `Canvas White`, `Unica77 LL` 18px weight 400, `15px` horizontal padding.
Create a project title heading: text `Canvas White`, `Unica77 LL` 80px weight 400, line-height 1.0.
Create body text: text `Canvas White`, `Unica77 LL` 14px weight 400, line-height 1.22.
Create a list item: text `Canvas White`, `Unica77 LL` 12px weight 400, `5px` bottom margin, bordered with a `1px` `Canvas White` line at the bottom.
Create an input field: transparent background, `Midnight Noir` text, `0px` border-radius, `0px` padding, with a `1px` `Midnight Noir` bottom border.

---
_Source: https://styles.refero.design/style/5acff005-1871-4237-bc25-cdddf50edc70_
