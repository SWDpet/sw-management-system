# Aaron Poe & Co — Design Reference

> White canvas, typographic precision

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://aaronpoeandco.com](https://aaronpoeandco.com) |
| Refero page | [https://styles.refero.design/style/3240fdc0-ffea-4054-a996-a5f6b942eff0](https://styles.refero.design/style/3240fdc0-ffea-4054-a996-a5f6b942eff0) |
| Theme | light |
| Industry | design |

## Overview

Aaron Poe & Co. presents a minimalist, high-contrast system on a stark white canvas. Typography is the primary visual element, using precise letter-spacing and restrained weights to establish a confident, almost academic tone. A single vibrant red accent color is used sparingly as conceptual punctuation, highlighting key information rather than driving interaction. Surfaces are almost uniformly flat, with subtle background tints providing hierarchy without significant elevation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Primary page and card backgrounds, clean backdrop for content |
| Deep Graphite | `#282828` | neutral | Primary text color for headlines and body text, providing strong contrast |
| Muted Ash | `#f2f2f2` | neutral | Subtle background for navigation elements and secondary surface layers |
| Ghost Gray | `#d9d8d4` | neutral | Tertiary background color for very subtle section separation |
| Charcoal Black | `#000000` | neutral | Secondary text color for body text, borders, and dark card backgrounds |
| Midnight Ink | `#121212` | neutral | Muted text, borders, and some navigation text elements |
| Ruby Red | `#ea587d` | accent | Conceptual accent for emphasis, used as a subtle icon stroke or specific heading text to draw attention |
| Light Gray | `#cccccc` | neutral | Subtler text, decorative borders |
| Pale Stone | `#b3b3b3` | neutral | Muted secondary text, often for descriptive captions or inactive states |

## Typography

### -apple-system

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.00 |
| letterSpacing | normal |
| fontFeatureSettings | "dlig" 0, "hlig" 0, "liga" 0, "rlig" 0, "smcp" 0 |
| substitute | system-ui |
| role | Default system font for generic UI elements and fallback text, ensuring broad compatibility. |

### Geist

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| sizes | 8px, 10px, 16px, 18px |
| lineHeight | 1.00, 1.17, 1.38, 2.67 |
| letterSpacing | -0.0560em at 8px, -0.0370em at 10px, -0.0250em at 16px, -0.0120em at 18px |
| fontFeatureSettings | 'ss02', 'lnum' |
| role | Primary brand typeface for body copy and some navigation, characterized by precisely tracked, readable text, particularly effective at smaller sizes due to consistent numeric alignment. |

### custom_166638

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 8px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| fontFeatureSettings | 'dlig' 0, 'hlig' 0, 'liga' 0, 'rlig' 0, 'smcp' 0 |
| role | Specialized light-weight text for small annotations or subtle details where a delicate presence is desired. |

### wtqc

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 12px, 30px |
| lineHeight | 1.07, 1.33 |
| letterSpacing | -0.0330em at 12px, -0.0330em at 30px |
| fontFeatureSettings | 'lnum' |
| role | Headline typeface, providing a slightly more structured feel with a consistent, tight letter-spacing for concise titles. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 22.08 | -0.4 |
| subheading | 18 |  | 20.16 | -0.22 |
| heading | 30 |  | 40 | -0.99 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 90px |
| cards | 0px |
| forms | 4px |
| buttons | 4px |
| default | 4px |
| interactive | 10px |

- **elementGap** — 
- **sectionGap** — 48px
- **cardPadding** — 0px
- **pageMaxWidth** — 900px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Primary page background and base for most content sections. |
| Muted Ash | `#f2f2f2` | 2 | Subtle background for navigation and secondary content containers, indicating a slight shift in hierarchy. |
| Ghost Gray | `#d9d8d4` | 3 | Tertiary background for very faint separation of elements or sections. |
| Midnight Ink (Dark Card) | `#000000` | 4 | Dramatic, high-contrast surface for specific elements requiring strong visual presence on a light canvas. |

## Components

### Navigation Tab Active

**Role:** Primary navigation element for current page

White background (#ffffff) with 90px border-radius, #282828 (Deep Graphite) text at 16px wtqc font, letter-spacing -0.033em, 500 weight. Inset shadow with #f2f2f2 at 1px.

### Navigation Tab Inactive

**Role:** Primary navigation element for unselected page

Transparent background with #282828 (Deep Graphite) text at 16px wtqc font, letter-spacing -0.033em, 500 weight.

### Minimal Card

**Role:** Content container with subtle presence

0px border-radius, no shadow, #ffffff (Canvas White) background for a seamless integration with the page, or #000000 (Charcoal Black) for a strong contrast element. No internal padding.

### Rounded Accent Card

**Role:** Elevated visual content area

24px border-radius, no shadow, #ffffff (Canvas White) background. No internal padding.

## Layout

The page structure is contained, set to a `900px` maximum width, and centrally aligned, giving content a focused presentation. The hero section follows this pattern with a central text block. Section rhythm is maintained through consistent vertical spacing, often using subtle background color shifts (like `Muted Ash` or `Ghost Gray`) rather than hard dividers. Content is arranged in compact, centered stacks, with no evident grid usage for cards or features, reinforcing the minimalist approach. Navigation is a simple horizontal bar at the top, integrating pill-shaped tabs.

## Imagery

This design system predominantly avoids complex imagery, opting for a text-dominant interface. When present, visual elements like icons are outlined, simple in form, and monochromatic, largely rendered in `Ruby Red` for specific highlight. There is no evidence of photography, illustration, or product screenshots being a core part of the aesthetic; focus is on clean UI and typography to convey meaning.

## Dos & Donts

### Do

- Prioritize typography as the primary means of conveying hierarchy and brand personality, using precise letter-spacing from the `Geist` and `wtqc` fonts.
- Maintain a stark, high-contrast aesthetic with `Canvas White` for backgrounds and `Deep Graphite` or `Charcoal Black` for most text.
- Employ `Ruby Red` (#ea587d) only for specific, semantic emphasis or decorative accents for minimal impact.
- Use 0px padding and 0px border-radius for cards when a flush, integrated content block is desired.
- Adopt `Muted Ash` (#f2f2f2) or `Ghost Gray` (#d9d8d4) for subtle background shifts to indicate different sections or elements without heavy visual breaks.
- Apply a 90px border-radius for pill-shaped elements like navigation tabs, ensuring uniformity for interactive ghost components.
- Ensure all body text uses a line height of at least 1.38 and tight letter-spacing to optimize readability at varying font sizes.

### Don't

- Avoid using multiple accent colors; `#ea587d` (`Ruby Red`) is the single functional chromatic accent.
- Do not introduce heavy shadows or gradients, as the system relies on flat surfaces and subtle background color changes for depth.
- Do not use default browser link colors; all interactive elements should adhere to the defined `Deep Graphite` text color or `Ruby Red` accent for specific emphasis.
- Do not deviate from the specified font families (`-apple-system`, `Geist`, `custom_166638`, `wtqc`) and their associated weights for consistency.
- Refrain from large, immersive imagery; the system emphasizes minimalist UI over visual content showcase.
- Avoid excessive padding within components; prioritize a compact, information-dense layout where appropriate.
- Do not use `custom_166638` for body text; its light weight is reserved for highly specific, subtle annotations and details below 8px.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #282828
background: #ffffff
border: #282828
accent: #ea587d
primary action: no distinct CTA color

Example Component Prompts:
1. Create a Header Navigation: Use 'Muted Ash' background without padding. Include a 'Navigation Tab Active' with the label 'Work' and a 'Navigation Tab Inactive' with the label 'Information'. All text in `wtqc`, size 16, weight 500, letter-spacing -0.033em.
2. Design a Main Content Block: 'Canvas White' background at 900px max-width, with body text content. Text should use 'Geist' font at 18px, weight 400, line-height 1.38, letter-spacing -0.012em, color 'Deep Graphite'.
3. Create a Dark Highlight Card: Use 'Minimal Card' variant with `Charcoal Black` background. Inside, place a 'caption' text in `custom_166638` at 8px, weight 300, normal letter-spacing, color `Pale Stone`.

---
_Source: https://styles.refero.design/style/3240fdc0-ffea-4054-a996-a5f6b942eff0_
