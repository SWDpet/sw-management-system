# Sublime — Design Reference

> Whiteboard of Ideas: bold strokes on a bright, uncluttered surface, punctuated by sharp, focused annotations.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://sublime.app](https://sublime.app) |
| Refero page | [https://styles.refero.design/style/80392d80-3970-46f0-a7ed-f213f316c933](https://styles.refero.design/style/80392d80-3970-46f0-a7ed-f213f316c933) |
| Theme | light |
| Industry | productivity |

## Overview

Sublime uses a stark, high-contrast visual language with a dominant white canvas and confident, large typography. Its visual identity is built on oversized headings, minimal UI elements, and a subtle interplay of soft grays, creating a pristine, almost academic feel. The occasional hint of vivid green acts as a functional accent, drawing attention to calls-to-action and active states without overwhelming the restrained palette. Components are lightweight and often borderless, emphasizing content over chrome.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Dominant page background, primary card surfaces |
| Midnight Ink | `#000000` | neutral | Primary text, strong borders |
| Charcoal Text | `#181816` | neutral | Heading text, icon fills |
| Fog Gray | `#efefef` | neutral | Subtle background for UI elements, button fills, soft card borders |
| Zinc Gray | `#908f8e` | neutral | Muted text, button borders, secondary heading text for contrast |
| Ash Gray | `#a29e9c` | neutral | Helper text, ghost button text, decorative borders |
| Sage Green | `#38744d` | accent | Green outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Mint Accent | `#e0f5ff` | accent | Subtle background wash for related content sections |
| Lime Highlight | `#cbffa6` | accent | Green wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Slate Text | `#6a6967` | neutral | Link text, sub-headings |

## Typography

### Control Upright

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px, 14px, 16px, 18px, 24px, 28px, 64px, 142px |
| lineHeight | 0.90, 1.00, 1.13, 1.16, 1.39 |
| letterSpacing | -0.0200em at 142px, -0.0150em at 64px, 0.0080em at 12px |
| fontFeatureSettings | "ss12" on, "ss09" on |
| substitute | Arial Bold |
| role | Primary font for all UI text, headings, and body content. Its custom features give it a distinct, almost monospace-like character, lending an intellectual yet approachable feel. The extremely tight letter spacing at larger sizes (#-0.0200em at 142px, -0.0150em at 64px) creates an intentional, dense typographic block. |

### Times New Roman

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px, 16px, 18px |
| lineHeight | 1.20, 1.39 |
| letterSpacing | -0.0300em at 18px, -0.0200em at 14px |
| fontFeatureSettings | "ss12" on |
| substitute | serif |
| role | Used for specific body content, possibly quotes or detailed explanatory text. The inclusion of a serif font provides a subtle textural contrast to the dominant sans-serif. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.39 | 0.008 |
| body | 14 |  | 1.39 | -0.02 |
| label | 16 |  | 1.39 |  |
| subheading | 18 |  | 1.39 | -0.03 |
| heading-sm | 24 |  | 1.16 |  |
| heading | 28 |  | 1.13 |  |
| heading-lg | 64 |  | 1 | -0.015 |
| display | 142 |  | 0.9 | -0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 5px |
| footer | 40px |
| buttons | 5px |

- **elementGap** — 15px
- **sectionGap** — 200px
- **cardPadding** — 18px
- **pageMaxWidth** — 

## Components

### Primary Action Button (Filled)

**Role:** Call-to-action button

Background: Lime Highlight (#cbffa6). Text: Sage Green (#38744d). Border: none. Radius: 5px. Padding: 0px vertical, 17.5px horizontal (derived from 0px height, 17.5px horizontal).

### Secondary Action Button (Ghost)

**Role:** Subtle button for secondary actions or navigation

Background: transparent. Text: Ash Gray (#a29e9c). Border: none. Radius: 0px. Padding: 0px. Used for login or navigation links.

### Neutral Button

**Role:** General utility button

Background: Fog Gray (#efefef). Text: Zinc Gray (#908f8e). Border: 1px solid Zinc Gray (#908f8e). Radius: 5px. Padding: 0px vertical, 17.5px horizontal (derived from 0px height, 17.5px horizontal).

### Text Link Button

**Role:** Interactive text link that behaves like a button

Background: transparent. Text: Midnight Ink (#000000). Border: none. Radius: 0px. Padding: 0px. Used for internal navigation or specific prompts.

### Small Pill Tag

**Role:** Informational tag for related content

Background: Mint Accent (#e0f5ff). Text: Midnight Ink (#000000). Border: 1px solid Midnight Ink (#000000). Radius: 999px. Padding: derived from line height of 16px, 13px horizontal.

### White Card

**Role:** Content container for various information types

Background: Canvas White (#ffffff). Border: 1.5px solid Zinc Gray (#908f8e). Radius: 5px. Padding: 18px on all sides.

## Layout

The page primarily uses a full-bleed layout on a white canvas, with content often centered or arranged in multi-column grids within distinct sections. The hero section is full-bleed, featuring large centered text and a prominent visual motif. Subsequent sections maintain a generous vertical rhythm with a 200px section gap. Content structures often involve alternating text and image blocks, or 2-3 column card grids, all contained within the full-width white background, rather than a fixed-width container. Navigation is a minimalist top bar, with 'login' and 'try now' buttons right-aligned.

## Imagery

Imagery is used sparingly, focusing on conceptual renders or highly stylized assets. The hero features a distinctive 3D moss-covered rock formation forming the brand name, lending an organic, whimsical, yet grounded feel. Other visuals include tight crops of product-related content and abstract illustrations within minimalist 'card' containers. Icons are monochromatic and simplistic, typically outlined or filled with Charcoal Text (#181816). The overall impression is illustrative and conceptual rather than photographic or product-heavy.

## Dos & Donts

### Do

- Always use the Control Upright font with its specific font feature settings ('ss12' on, 'ss09' on) for all primary text and headings.
- Prioritize Canvas White (#ffffff) as the dominant page background, maintaining a spacious, bright environment.
- Apply a 5px border-radius consistently for all interactive elements like buttons and cards, creating a unified soft edge.
- Use Midnight Ink (#000000) or Charcoal Text (#181816) for primary text content to ensure high contrast against light backgrounds.
- Employ Lime Highlight (#cbffa6) exclusively for primary action buttons, ensuring visual distinction and clear calls-to-action.
- Maintain a clear element gap of 15px between horizontal components and a section gap of 200px for vertical rhythm.
- Utilize a 999px border-radius for all small, informational tags and pill-shaped elements for a distinct rounded aesthetic.

### Don't

- Do not introduce new saturated colors; limit the chromatic palette to Sage Green (#38744d), Mint Accent (#e0f5ff), and Lime Highlight (#cbffa6).
- Avoid heavy shadows or gradients; the design relies on flat surfaces and subtle border treatments.
- Do not use generic system fonts as substitutes for Control Upright without explicit font feature settings; this compromises the brand's unique typographic feel.
- Do not vary paragraph text sizes or line heights excessively; stick to the defined typographic scale for consistency.
- Do not apply large, ornate borders or decorative elements to components; keep the UI lean and content-focused.
- Avoid dense, complex layouts; favor generous white space and clean, balanced compositions with ample section gaps.
- Do not deviate from the defined border-radii; the specific values (5px, 999px, 40px) are critical to the visual identity.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- text: #000000
- background: #ffffff
- border: #908f8e
- accent: #38744d
- primary action: no distinct CTA color

**3-5 Example Component Prompts:**
- Create a hero section: Canvas White background. Headline 'the knowledge tool that sparks creativity' using Control Upright at 142px, Charcoal Text (#181816), line height 0.9, letter-spacing -0.02em. Below that, subtext 'save one thing discover a hundred more' using Control Upright at 28px, Zinc Gray (#908f8e), line height 1.13.
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
- Create a White Card: Background Canvas White (#ffffff), 1.5px solid Zinc Gray (#908f8e) border, 5px radius, 18px padding. Inside, a heading 'björk said that' using Control Upright at 18px, Charcoal Text (#181816). Below, a Small Pill Tag 'related' with Mint Accent (#e0f5ff) background, Midnight Ink (#000000) text, 1px solid Midnight Ink (#000000) border, 999px radius, 13px horizontal padding.

---
_Source: https://styles.refero.design/style/80392d80-3970-46f0-a7ed-f213f316c933_
