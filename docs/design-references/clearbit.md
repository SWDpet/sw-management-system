# Clearbit — Design Reference

> White canvas, muted indigo typography, focused blue accents.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://clearbit.com](https://clearbit.com) |
| Refero page | [https://styles.refero.design/style/6221ba67-26e7-4657-91b7-efd77cbb1f12](https://styles.refero.design/style/6221ba67-26e7-4657-91b7-efd77cbb1f12) |
| Theme | light |
| Industry | saas |

## Overview

Clearbit employs a spacious, light-themed interface centered around a clean white canvas and subtle elevated surfaces. Typography is compact and confident, primarily in a muted indigo, setting a tone of understated authority. Visual hierarchy is established through precise spacing and a single vivid blue accent color that highlights interactive elements and draws attention to key information, making the interface feel precise and purposeful.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, text on dark elements |
| Inkwell Indigo | `#091135` | brand | Primary text, headings, navigational elements, icon fills — the dominant content color |
| Powder Gray | `#e1e9f0` | neutral | Subtle borders, dividers, ghost button outlines, subtle background accents |
| HubSpot Blue | `#127ee3` | brand | Blue wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Focus Blue | `#0f77ff` | accent | Blue outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Slate Text | `#36394a` | neutral | Secondary and helper text, descriptive labels |
| Lavender Mist | `#f5f3ff` | neutral | Section backgrounds, subtle background washes |

## Typography

### InterVar

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 32px, 56px, 64px |
| lineHeight | 1.25, 1.43, 1.50 |
| letterSpacing | -0.96px at 64px, -0.8px at 56px, -0.44px at 32px, normal at 18px, -0.26px at 16px, -0.22px at 14px |
| substitute | Inter |
| role | The primary typeface for all text content, from headings to body text. Its subtle variations in letter spacing across sizes lend refinement and control to the overall text blocks. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.5 | -0.22 |
| body | 16 |  | 1.5 | -0.26 |
| subheading | 18 |  | 1.5 |  |
| heading-sm | 32 |  | 1.25 | -0.44 |
| heading | 56 |  | 1.25 | -0.8 |
| display | 64 |  | 1.25 | -0.96 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| misc | 24px |
| cards | 12px |
| buttons | 8px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Default page background. |
| Lavender Mist | `#f5f3ff` | 1 | Alternating section backgrounds and subtle content block differentiation. |
| Elevated Card | `#ffffff` | 2 | Card backgrounds, visually separated by elevation. |

## Components

### Navigation Login Button

**Role:** Primary Call to Action in the header.

A filled button with 'HubSpot Blue' background, 'Canvas White' text, 8px border-radius, and a prominent focus shadow. Padding is 16px horizontal and no vertical padding, letting the text dictate height slightly. Text is InterVar, size 16px, weight 400.

### Ghost Navigation Link

**Role:** Secondary navigation and utility links.

A transparent button/link with 'Inkwell Indigo' text. No background, no visible border, giving it a ghost-like appearance. Padding is 16px horizontal and 8px vertical. Text is InterVar, size 16px, weight 400.

### Feature Card

**Role:** Container for showcasing product features or data snippets.

Rectangular cards with a 'Canvas White' background and 12px border-radius. Features no explicit border but relies on elevation for visual separation. Internal content uses 24px padding.

### Accent Bordered Card

**Role:** Highlighting a specific card or interactive element with a strong focus.

A card with 12px border-radius, featuring a 1px 'Focus Blue' border and elevated shadow. Background is 'Canvas White'. Internal content uses 24px padding. The shadow is rgb(15, 119, 255) 0px 0px 0px 1px, rgba(12, 43, 100, 0.32) 0px 1px 2px 0px, rgba(12, 43, 100, 0.32) 0px 6px 16px 0px.

## Layout

The page adheres to a contained layout with a '1200px' max-width, maintaining centered content. The hero section can be full-bleed with a centered headline over a subtle background. Content sections flow vertically with consistent '64px' vertical spacing, utilizing alternating 'Canvas White' and 'Lavender Mist' backgrounds to create visual rhythm. Sections frequently employ two-column layouts pairing text and illustrative elements, or multi-column card grids for features. Navigation is a sticky top bar with a primary 'HubSpot Blue' CTA and ghost links.

## Imagery

Imagery primarily consists of abstract, translucent UI elements and product screens that subtly reflect the brand's 'HubSpot Blue' and 'Focus Blue' accents. Illustrations involve geometric shapes, often with blurred gradients, creating a fluid, modern impression rather than hard-edged graphics. Icons are minimal, outlined, and monochromatic, primarily using 'Inkwell Indigo' for content or 'Focus Blue' for accents. Visuals are decorative and illustrative, serving to add atmosphere and context rather than being the primary content vehicle, ensuring text remains dominant.

## Dos & Donts

### Do

- Prioritize 'Inkwell Indigo' (#091135) for all primary text content and headings.
- Use 'Canvas White' (#ffffff) as the default background for pages and main content blocks.
- Apply 'Powder Gray' (#e1e9f0) for all hairline borders and subtle visual separations.
- Reserve 'HubSpot Blue' (#127ee3) for solid CTAs and primary interactive elements.
- Maintain consistent vertical spacing between sections using '64px' where visual dividers are absent, and '20px' or '24px' for vertical rhythm within content blocks.
- Apply a border-radius of '12px' for cards and content containers, and '8px' for interactive elements like buttons.
- Use 'Focus Blue' (#0f77ff) primarily for interactive element borders or subtle UI accents, not as a primary button fill.
- Ensure headings use a generous letter-spacing correction, specifically -0.8px for 56px and -0.96px for 64px, to maintain a tight visual appearance.

### Don't

- Avoid using highly saturated colors for large background areas, stick to neutrals or 'Lavender Mist' (#f5f3ff).
- Do not deviate from the InterVar font family; do not introduce additional typefaces.
- Avoid heavy drop shadows; use the provided subtle elevation shadow for focus and interactive states sparingly.
- Do not use dark backgrounds for primary content areas; the theme is predominantly light.
- Do not use generic button styles; stick to the ghost link or filled button as defined.
- Do not cram elements; maintain '8px' as the minimum 'elementGap' for clear separation.
- Do not use 'Inkwell Indigo' (#091135) for button backgrounds; it is reserved for text.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #091135
background: #ffffff
border: #e1e9f0
accent: #0f77ff
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Create a feature card with a bold border highlight: background #ffffff, border 1px solid #0f77ff, radius 12px, padding 24px. Include a title in Inkwell Indigo weight 600 at 32px and body text in Slate Text weight 400 at 16px.
3. Create a hero section headline: 'A data foundation you can build on', text #091135 in InterVar weight 700 at 64px, line-height 1.25, letter-spacing -0.96px, centered on a #ffffff background section.

---
_Source: https://styles.refero.design/style/6221ba67-26e7-4657-91b7-efd77cbb1f12_
