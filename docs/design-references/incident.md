# Incident — Design Reference

> Engineering blueprint on stark white

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://incident.io](https://incident.io) |
| Refero page | [https://styles.refero.design/style/d9a60077-619a-4cb7-95ed-0c428c2b51ed](https://styles.refero.design/style/d9a60077-619a-4cb7-95ed-0c428c2b51ed) |
| Theme | light |
| Industry | saas |

## Overview

Incident.io presents a utilitarian, text-heavy interface on a bright canvas, prioritizing clarity and directness over decorative elements. Its visual identity relies on strong typographic contrast and a minimal use of color, primarily reserving a vivid orange for attention-grabbing accents or brand indicators. Components are lightweight with sharp corners, reflecting an engineering-focused approach to design where function dictates form.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, default UI element fill |
| Platinum Mist | `#efefef` | neutral | Button backgrounds, subtle background differentiation |
| Ink Black | `#000000` | neutral | Primary text, borders, iconography |
| Deep Graphite | `#161618` | neutral | Secondary text, subtle borders, iconography |
| Desert Sand | `#e4d9c8` | neutral | Decorative graphical elements (background type, illustrations) |
| Alert Red | `#ff492c` | accent | Orange decorative accent for icons, marks, and small graphic details. Use as a supporting accent, not as a status color |
| Flamingo Orange | `#f25533` | brand | Brand accents, decorative iconography, visual highlights |
| Vivid Hue | `#f1641e` | accent | Highlighting text, decorative illustration fill, borders |
| Light Gray Divider | `#dadada` | neutral | Subtle dividers, image box shadows |
| Medium Gray Divider | `#cccccc` | neutral | Faint dividers, image box shadows |

## Typography

### Times

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 16px, 19px, 24px, 32px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | serif |
| role | Primary headings, body text, and links – a classic serif choice that grounds the technical content with a sense of established authority rather than modern digital slickness. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | sans-serif |
| role | Used for smaller functional text such as button labels and navigation, providing a universally legible sans-serif counterpoint to the serif body. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 1.2 |  |
| subheading | 19 |  | 1.2 |  |
| heading | 24 |  | 1.2 |  |
| display | 32 |  | 1.2 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| all | 0px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background |
| Platinum Mist | `#efefef` | 1 | Component backgrounds (e.g., buttons), subtle content separators |
| Subtle Background | `#000000` | 2 | Highly subtle translucent dark backgrounds for content cards, often used with transparency (rgba(0,0,0,0.15)) |

## Components

### Default Button

**Role:** Interactive element

Standard interactive button, filled with Platinum Mist (#efefef) and Ink Black (#000000) text. Has a sharp 0px border-radius and minimal 1px vertical, 6px horizontal padding. No border or shadow.

### Ghost Card

**Role:** Container, content grouping

A transparent card with no background color (rgba(0, 0, 0, 0)), 0px border-radius, and no box-shadow. Used for seamless content integration.

### Subtle Background Card

**Role:** Container, content grouping

A card with a very slight translucent black background (rgba(0, 0, 0, 0.15)), 0px border-radius, and no box-shadow. Provides a subtle visual separation without strong outlines.

## Layout

The page maintains a centered max-width content area, providing a structured, contained experience without full-bleed elements dominating the display. The hero section often features a centered headline over a plain background. Section rhythm is marked by consistent vertical spacing (section gap of 40px), maintaining visual separation. Content is arranged in alternating text-left/visual-right patterns or simple centered stacks for clarity. Grid usage is implied for feature listings or team showcases, leveraging the consistent element gap for column spacing. The layout prioritizes information density and readability over expansive visual gestures. Navigation is a standard top bar, likely with a sticky behavior, and a clear, minimal structure.

## Imagery

The site uses a mix of utilitarian visuals: primarily product screenshots or internal tool interfaces, often presented without heavy styling. When present, abstract graphics or icons are flat, typically filled with brand accents like Flamingo Orange or Ink Black, occasionally lightened by Desert Sand. Photography is absent. Icons, when visible, are filled and have a moderate stroke weight, serving an explanatory or decorative role rather than a functional one. Imagery is used to break up text-heavy sections or to illustrate concepts, occupying a moderate visual space. The overall density is text-dominant.

## Dos & Donts

### Do

- Prioritize Times serif for all long-form content and primary headings to maintain a traditional, authoritative voice.
- Use Arial sans-serif exclusively for small functional text like button labels and navigation items, at a size of 13px with normal letter spacing.
- Employ Ink Black (#000000) as the default color for all text, borders, and iconography unless a specific accent is required.
- Apply Platinum Mist (#efefef) for interactive button backgrounds, ensuring text remains Ink Black for maximum contrast.
- Maintain a strict 0px border-radius across all components (buttons, cards) to promote a sharp, angular aesthetic.
- Utilize 16px as the primary element gap for vertical and horizontal spacing between content blocks and interactive elements.
- Reserve Flamingo Orange (#f25533) and Vivid Hue (#f1641e) for brand accents and visual highlights, not for general UI elements like buttons or text.

### Don't

- Avoid using rounded corners; maintain the 0px border-radius for all elements.
- Do not introduce new primary colors; stick to the defined neutral palette with specific semantic and brand accents.
- Refrain from heavy drop shadows or elaborate elevation. Subtle box-shadows should be minimal and used sparingly, as seen on images.
- Do not deviate from the specified serif and sans-serif font assignments; mixing them freely will disrupt the visual rhythm.
- Avoid large, impactful background imagery on content sections; the system favors stark white space and clear typography.
- Do not use accent colors for primary CTA backgrounds where a neutral background is defined; accents are for visual punctuation.
- Do not introduce extra padding on cards; maintain the 0px padding for a dense, content-focused presentation.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
- text: #000000
- background: #ffffff
- border: #000000
- accent: #f25533
- primary action: no distinct CTA color

Example Component Prompts:
- Create a status alert banner: Alert Red (#ff492c) background with Ink Black (#000000) text (16px Times, weight 400). Maintain 0px radius, with 16px internal padding. 
- Design a primary navigation link: Ink Black (#000000) text (16px Times, weight 400) on a Canvas White (#ffffff) background, with a subtle 2px outset Ink Black (#000000) border on hover. 
- Build a feature card with a subtle background: Subtle Background (rgba(0, 0, 0, 0.15)) fill, 0px border-radius, and no shadow. Title in Ink Black (#000000) at 19px Times, weight 700. Body text in Ink Black (#000000) at 16px Times, weight 400.
- Construct a ghost button: Transparent background with Ink Black (#000000) text (13px Arial, weight 400). Apply a 2px outset Ink Black (#000000) border, 0px border-radius, and 1px vertical, 6px horizontal padding.

---
_Source: https://styles.refero.design/style/d9a60077-619a-4cb7-95ed-0c428c2b51ed_
