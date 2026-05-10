# Planhat — Design Reference

> Crisp monochrome command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.planhat.com](https://www.planhat.com) |
| Refero page | [https://styles.refero.design/style/94c4fc51-4323-4f06-a4a4-27517e190445](https://styles.refero.design/style/94c4fc51-4323-4f06-a4a4-27517e190445) |
| Theme | light |
| Industry | saas |

## Overview

Planhat employs a precise, high-contrast digital interface that balances serious subject matter with subtle visual intrigue. Dark, almost textured backgrounds underpin crisp white foregrounds, with typography serving as the primary design element through its varying weights and exact spacing. Minimal color accents are used strategically, highlighting interactive elements and brand elements without overwhelming the monochrome palette. The overall impression is one of clarity and focused information delivery.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Twilight Ink | `#000000` | neutral | Primary text, surface backgrounds, borders for outlined components |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, primary text on dark backgrounds |
| Coal Text | `#121211` | neutral | Secondary text, input text |
| Icon Gray | `#575551` | neutral | Subtle decorative icons, muted graphic elements |
| Slate Border | `#958d7e` | neutral | Default link text, outlined button borders |

## Typography

### Geigy LL Duplex Var Variable Reg

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 18px, 24px, 32px, 48px, 60px, 113px |
| lineHeight | 1.00, 1.10, 1.20, 1.25, 1.40 |
| letterSpacing | -0.0600em at 113px, -0.0450em at 60px, -0.0350em at 48px, -0.0300em at 32px, -0.0200em at 24px |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| substitute | Montserrat |
| role | Primary brand typeface for headings and prominent body text. Its variable nature and specific letter spacing create a precise, engineered feel. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 10px, 12px, 14px, 16px |
| lineHeight | 1.00, 1.20, 1.40, 1.43, 1.50 |
| letterSpacing | -0.0200em at 16px, -0.0150em at 14px, -0.0100em at 12px, 0.1000em at 10px |
| substitute | Inter |
| role | Functional typeface for body copy, links, and form inputs. Its clean, legible structure supports the primary typeface for information density. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | Arial |
| role | Fallback font primarily used for small, utility-level text and contexts where Inter's specific features are not required. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.43 | -0.21 |
| subheading | 18 |  | 1.4 | -0.36 |
| heading | 32 |  | 1.1 | -0.96 |
| heading-lg | 48 |  | 1.1 | -1.68 |
| display | 60 |  | 1 | -2.7 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 999px |
| links | 4px |
| images | 4px |
| inputs | 4px |
| buttons | 4px |

- **elementGap** — 5px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Base page background |
| Subtle Background | `#f6f6f8` | 2 | Background for input fields and subtle content containers |
| Twilight Ink | `#000000` | 3 | Dark contrast sections and overlays |

## Components

### Primary Outlined Button

**Role:** Call to action button for significant actions

Background transparent, text 'Twilight Ink' (#000000), border 'Twilight Ink' (#000000) 1px thick, 4px border-radius, padding 10px vertical, 18px horizontal (left), 16px horizontal (right). Font 'sans-serif' weight 400, 12px.

### Secondary Outlined Button

**Role:** Subtle call to action

Background transparent, text 'Twilight Ink' (#000000), border 'Twilight Ink' (#000000) 1px thick, 4px border-radius, padding 6px vertical, 14px horizontal. Font 'sans-serif' weight 400, 12px.

### Pill Card

**Role:** Decorative card for badges or small content blocks

Background 'Twilight Ink' (#000000), border-radius 999px. No padding or shadow.

### Ghost Input Field

**Role:** Input element with minimal styling, typically for search or filter

Transparent background, text 'Coal Text' (#121211), border 'Coal Text' (#121211) 1px thick, 0px border-radius. No explicit padding.

### Subtle Background Input

**Role:** Input field for data entry in a form

Background light gray (#F6F6F8, derived from hints), text 'Twilight Ink' (#000000), border 'Twilight Ink' (#000000) 1px thick, 4px border-radius. No explicit padding.

## Layout

The page structure uses a centered max-width container, contrasting with full-bleed hero sections that set the initial tone. The hero features a centered headline over a dark, textured background. Subsequent sections alternate between light and dark backgrounds, with consistent vertical spacing creating a rhythmic flow. Content is often arranged in multi-column grids, including alternating text-left/image-right patterns and 3-column feature grids for showcases. A particularly dense customer logo grid showcases social proof. Navigation is a sticky top bar with clearly defined 'Log In' and 'Request a Demo' buttons.

## Imagery

The visual language predominantly features abstract, dark, and textured photography or graphics for hero sections, creating a mood of depth and technology. For interior content, product screenshots are presented in clean, contained frames, often with rounded corners, emphasizing functionality and conceptual diagrams. Icons are monochrome, following an outlined style with a consistent stroke weight, and primarily serve to illustrate or highlight features rather than add decorative color. Imagery is always contained, never full-bleed, and integrated to explain content rather than solely for atmospheric effect across the entire page.

## Dos & Donts

### Do

- Use 'Twilight Ink' (#000000) for all primary body text and main headings to maintain high contrast and readability.
- Apply 'Canvas White' (#ffffff) as the dominant background for all content sections, ensuring a clean and expansive feel.
- Utilize 'Geigy LL Duplex Var Variable Reg' for all headline levels, leveraging its unique letter spacing and variable features for distinctiveness.
- Employ 'Inter' for all functional text such as body copy, UI labels, and form inputs, prioritizing legibility and information density.
- Maintain a compact element gap of 5px between adjacent UI elements to keep interfaces tight and focused.
- Apply a 4px border-radius consistently to all interactive elements including buttons, input fields, and small cards.
- Use 'Slate Border' (#958d7e) only for outlined buttons and links, never as a background color.

### Don't

- Avoid using multiple chromatic colors; limit color accents to functional highlights only.
- Do not introduce heavy shadows or gradients; rely on defined borders and background shifts for surface differentiation.
- Do not deviate from the specified letter-spacing values for 'Geigy LL Duplex Var Variable Reg' as they are critical to its signature appearance.
- Do not use generic system sans-serif for body text or headlines where 'Inter' or 'Geigy LL Duplex Var Variable Reg' are specified.
- Avoid large, uncontained images; all imagery should be treated and contained within defined boundaries.
- Do not use border-radius values other than 4px, 999px, or 100px for component shaping.
- Do not use animated transitions with durations less than 0.3s; ensure moderate pace for UI changes.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #000000
accent: no distinct accent color
primary action: no distinct CTA color

Example Component Prompts:
1. Create a hero section with a dark textured background. Headline: 'Deploy AI with confidence' using 'Geigy LL Duplex Var Variable Reg' at 60px, weight 400, 'Canvas White' (#ffffff) color, letter spacing -2.7px. Subtext: 'Planhat provides B2B enterprises...' in 'Inter' at 18px, weight 400, 'Canvas White' (#ffffff) color. Include a 'Request a Demo' button: transparent background, 'Canvas White' text, 'Canvas White' border, 4px radius, 10px vertical 18px horiz (left) 16px horiz (right) padding, font 'sans-serif' 12px weight 400.
2. Create a feature card: 'Canvas White' (#ffffff) background, 4px border-radius, 'Coal Text' (#121211) primary text, 'Icon Gray' (#575551) secondary text in 'Inter' 14px weight 400. Use 16px padding on all sides. 
3. Create an outlined navigation link: 'Slate Border' (#958d7e) text color, no background, 4px border-radius, 6px vertical 14px horizontal padding. Font 'sans-serif' 12px weight 400.

---
_Source: https://styles.refero.design/style/94c4fc51-4323-4f06-a4a4-27517e190445_
