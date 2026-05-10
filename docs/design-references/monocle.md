# Monocle — Design Reference

> Ink-on-paper minimalist; a finely printed journal on pristine stock.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://monocle.com](https://monocle.com) |
| Refero page | [https://styles.refero.design/style/9165ecb1-f068-4093-8783-1f3c98898b8a](https://styles.refero.design/style/9165ecb1-f068-4093-8783-1f3c98898b8a) |
| Theme | light |
| Industry | media |

## Overview

Monocle's design evokes a classic, authoritative editorial feel, grounded in high-contrast typography on a clean white canvas. The system prioritizes crisp lines, clear hierarchy, and restraint over flourish, creating an environment where content takes center stage. Signature elements include the tight letter-spacing on headlines, the use of a custom serif font for primary content, and a sparse accent palette that highlights interactivity and key features without visual noise.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, body text background. |
| Printer's Black | `#000000` | neutral | Primary text, headers, borders, active navigation elements – ensuring maximum contrast and legibility. |
| Sterling Gray | `#6e6e6` | neutral | Secondary text, metadata, disabled states, subtle borders – providing depth against brighter neutrals. |
| Zinc Gray | `#d9d9d9` | neutral | Muted borders, dividers, subtle inactive states – defining structure without visual weight. |
| Cloud Gray | `#e7e7e7` | neutral | Background for subtle card variants and distinct UI sections. |
| Parchment Cream | `#fdfcf3` | neutral | Subtle background for specific UI elements, adding a hint of warmth. |
| Editorial Yellow | `#ffc500` | brand | Call-to-action buttons, active navigation indicators, key interactive elements – a vivid punctuation mark. |
| Sky Blue | `#64d5ff` | accent | Card backgrounds for specific content categories, offering a cool counterpoint to the neutral palette. |

## Typography

### Plantin

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 13px, 16px, 18px, 20px, 24px, 28px, 32px, 34px, 40px |
| lineHeight | 1.00, 1.13, 1.15, 1.20, 1.25, 1.30, 1.38, 1.44, 1.50 |
| letterSpacing | -0.0200em, 0.0100em, 0.0500em, 0.0750em |
| substitute | Georgia |
| role | Primary content, article headlines, body text, and any long-form narrative. The custom serif embodies the brand's traditional editorial authority. |

### Helvetica Neue

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 13px, 14px, 16px, 24px |
| lineHeight | 1.00, 1.13, 1.25, 1.29, 1.38, 1.50 |
| letterSpacing | 0.0100em |
| substitute | Arial |
| role | UI elements, navigation, buttons, and short descriptive text – providing a clean, modern contrast to the serif content. |

### Chanel

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 9px |
| lineHeight | 1.00 |
| letterSpacing | normal |
| substitute | Playfair Display |
| role | Distinctive custom display font, likely reserved for branded elements like the main Monocle logo. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.5 | 0.01 |
| body | 16 |  | 1.5 | 0.01 |
| subheading | 24 |  | 1.25 | -0.48 |
| heading | 32 |  | 1.15 | -0.64 |
| display | 40 |  | 1.13 | -0.8 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| buttons | 0px |
| navBadges | 50% |

- **elementGap** — 4px
- **sectionGap** — 32px
- **cardPadding** — 16px
- **pageMaxWidth** — 1296px

## Components

### Monocle Radio Podcast Player Card

### Article Feature Card with Category Label and Read Time

### Podcast Episode Cards Grid

### Primary Navigation Link

**Role:** Interactive element

Text in Printer's Black (#000000), Helvetica Neue, weight 400. Underlined with a 1px solid Printer's Black (#000000) border when active or hovered. No padding.

### Call-to-Action Button

**Role:** Primary action

Background Editorial Yellow (#ffc500), text Printer's Black (#000000), Helvetica Neue, weight 700. No border, no radius. Content based on current context.

### Icon Button (Circular Outline)

**Role:** Secondary action/icon toggle

Transparent background, Printer's Black (#000000) text (icon), 1px Printer's Black (#000000) circular border, 50% border-radius. No padding.

### Standard Content Card

**Role:** Content container

Background Canvas White (#ffffff), 8px border-radius, no box shadow. Internal padding of 16px around content.

### Category Label Card

**Role:** Thematic content grouping

Background Sky Blue (#64d5ff), 8px border-radius, no box shadow. Internal padding of 16px around content.

### Neutral Background Card

**Role:** Subtle content grouping

Background Cloud Gray (#e7e7e7), 8px border-radius, no box shadow. Internal padding of 16px around content.

### Podcast Player Card

**Role:** Dynamic audio content display

Background Printer's Black (#000000), 8px border-radius, no box shadow. Features an Editorial Yellow (#ffc500) 'Listen Live' button.

### Search Input Field

**Role:** User input

Canvas White (#ffffff) background, Printer's Black (#000000) text. No border, no radius. Padding of 16px.

## Layout

The page adheres to a max-width 1296px centered layout, creating a contained reading experience. The hero section features a prominent brand marque and headline, often with a large, editorial photograph beneath it, setting an immediate authoritative tone. Content is arranged in a grid-like fashion, employing a combination of single-column article previews and multiple-column card grids for features and related content. The rhythm is established by consistent vertical spacing of 32px between major sections and 16px padding within cards. Navigation is persistent at the top, splitting into a utility bar and a primary category navigation. The layout is information-dense but organized, resembling a structured print publication.

## Imagery

Imagery is primarily editorial photography and stylized illustrations. Photography is typically tightly cropped, showcasing subjects directly in a high-key or natural light. Illustrations are often line-drawn with minimal flat colors, or possess a distinctive, quirky style (like the plane diagram or the Nic Monisse portrait). Images are usually contained within card structures or embedded directly into the content stream, not used as full-bleed hero elements. They serve an explanatory or illustrative role rather than purely decorative, balancing text-heavy layouts.

## Dos & Donts

### Do

- Prioritize Plantin for all article bodies and headlines to maintain editorial voice.
- Use Printer's Black (#000000) on Canvas White (#ffffff) for all primary text and background combinations.
- Apply 8px border-radius for all content cards and UI blocks where a soft edge is needed.
- Reserve Editorial Yellow (#ffc500) exclusively for calls to action and critical interactive states.
- Maintain tight letter-spacing for headlines (e.g., -0.64px for 32px Plantin headings) to enhance visual density.
- Utilize Helvetica Neue for navigation and utility text, setting it at 13px weight 400 for consistency.
- Implement 16px internal padding for all card components to provide sufficient breathing room for content.

### Don't

- Do not introduce additional color accents beyond Editorial Yellow (#ffc500) and Sky Blue (#64d5ff).
- Avoid box shadows or elevations; establish depth through background color changes (Canvas White, Cloud Gray, Sky Blue).
- Do not use rounded corners on primary buttons or navigation elements; maintain sharp, defined edges.
- Never use less than 4px spacing between elements unless for iconography or nested micro-interactions.
- Do not deviate from the specified font families; avoid system defaults for major text blocks.
- Avoid large, impactful hero images; focus on contained, editorial photography or illustrations.
- Do not use highly decorative UI elements; stick to functional and minimal design patterns.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #000000 (Printer's Black)
- Background: #ffffff (Canvas White)
- CTA: #ffc500 (Editorial Yellow)
- Border/Divider: #d9d9d9 (Zinc Gray)
- Secondary Text: #6e6e6 (Sterling Gray)

### 3-5 Example Component Prompts
1. Create a `Primary Navigation Link`: text 'Magazine', font Helvetica Neue weight 400, size 16px, color #000000. Underline in #000000 on hover.
2. Design a `Call-to-Action Button`: text 'Subscribe', font Helvetica Neue weight 700, size 16px, background #ffc500, text color #000000, no border, no radius. Padding 6px horizontal, 0px vertical.
3. Build a `Standard Content Card`: background #ffffff, 8px border-radius, no shadow. Internal padding 16px. Add a headline: Plantin weight 700, size 24px, color #000000, letter-spacing -0.48px. Follow with body text: Plantin weight 400, size 16px, color #000000, line-height 1.5.
4. Generate a `Category Label Card`: background #64d5ff, 8px border-radius, 16px padding. Include a title like 'The Weekend Opener' using Plantin weight 700, size 20px, color #000000.
5. Create an `Input Field`: background #ffffff, text color #000000, padding 16px. Placeholder text 'Search' in Sterling Gray #6e6e6e.

---
_Source: https://styles.refero.design/style/9165ecb1-f068-4093-8783-1f3c98898b8a_
