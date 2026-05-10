# Humble — Design Reference

> Precise White Lab

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://humbleops.ai](https://humbleops.ai) |
| Refero page | [https://styles.refero.design/style/a6950b49-8ce4-4330-9499-26ca08061599](https://styles.refero.design/style/a6950b49-8ce4-4330-9499-26ca08061599) |
| Theme | light |
| Industry | ai |

## Overview

Humble's design system embraces a 'light AI lab' aesthetic: a pristine white canvas contrasted with sharp, functional typography. A singular vibrant orange acts as a dynamic accent, highlighting key actions and brand elements. Components are lightweight with subtle, diffused shadows that lift elements gently from the background rather than anchoring them with heavy forms. The overall impression is one of clarity, precision, and restrained energy.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#fafafa` | neutral | Primary page background, default surface for most cards and UI elements. Provides a clean, bright foundation |
| Obsidian Text | `#1c1c1c` | neutral | Primary text for headlines and critical information. Near-black for maximal contrast on light surfaces |
| Granite Gray | `#6e6e6e` | neutral | Secondary text, navigation items, and muted interface elements. Provides visual relief from primary text |
| Ink Black | `#000000` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color; Background gradient primarily for hero sections or full-width elements where text needs to stand out against imagery or product graphics |
| Ghost White | `#f1f1f1` | neutral | Subtly darker background for alternative card surfaces or subtle section separation |
| Alabaster Gray | `#ecebe8` | neutral | Tertiary background for cards or distinct content blocks, providing minimal contrast to the main canvas |
| Electric Orange | `#ff4000` | accent | Orange outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### Bricolage Grotesque

| Key | Value |
| --- | --- |
| weight | 500, 600 |
| weights | 500, 600 |
| sizes | 16px, 18px, 20px, 24px, 32px, 42px, 44px, 50px, 58px |
| lineHeight | 0.70, 1.10, 1.20, 1.40, 1.50 |
| letterSpacing | -0.0520em (58px), -0.0500em (50px), -0.0360em (44px), -0.0300em (42px), -0.0200em (32px) |
| fontFeatureSettings | "cv01", "cv05", "cv09", "cv11", "ss03"; "blwf", "cv03", "cv04", "cv09", "cv11", "ss01", "ss03", "ss04" |
| role | Primary display and heading font. Its generous x-height and slightly condensed structure give it a modern, capable feel. Letter spacing is consistently tight, especially at larger sizes, to pull words together. |

### Geist

| Key | Value |
| --- | --- |
| weight | 500, 600 |
| weights | 500, 600 |
| sizes | 13px, 14px, 15px, 16px, 24px |
| lineHeight | 1.30, 1.40 |
| letterSpacing | -0.0200em (24px, 16px), -0.0070em (14px) |
| fontFeatureSettings | "blwf", "cv03", "cv04", "cv09", "cv11", "ss01", "ss03", "ss04"; "cv01", "cv05", "cv09", "cv11", "ss03" |
| role | Secondary sans-serif for UI elements, navigation, and detailed body text. Its clean, technical aesthetic complements the primary heading font while maintaining readability at smaller sizes. Features like 'ss01' are activated for a distinct geometric feel. |

### Geist Variable

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px, 14px, 17px |
| lineHeight | 1.00, 1.20, 1.30, 1.40 |
| letterSpacing | normal |
| fontFeatureSettings | "blwf", "cv03", "cv04", "cv09", "cv11", "ss01", "ss03", "ss04"; "ss01", "ss02", "ss03", "ss04" |
| role | A more flexible variant of Geist, used for compact informational text like captions, labels, and possibly code snippets due to its variable nature. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 14px, 16px, 18px |
| lineHeight | 1.20, 1.40 |
| letterSpacing | -0.0400em (18px), -0.0200em (16px), -0.0100em (14px) |
| fontFeatureSettings | "blwf", "cv03", "cv04", "cv09", "cv11" |
| substitute | system-ui |
| role | Fallback and utility font for general body text and links where a more standard, highly readable sans-serif is preferred. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.4 | 0 |
| body | 14 |  | 1.4 | -0.007 |
| body-lg | 16 |  | 1.4 | -0.02 |
| subheading | 18 |  | 1.4 | -0.04 |
| heading-sm | 24 |  | 1.2 | -0.02 |
| heading | 32 |  | 1.5 | -0.02 |
| heading-lg | 42 |  | 0.7 | -0.03 |
| display | 58 |  | 0.7 | -0.052 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 30px |
| images | 40px |
| buttons | 100px |
| controls | 6px |
| largeBlock | 70px |

- **elementGap** — 10px
- **sectionGap** — 64px
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Components

### Ghost Button

**Role:** Primary call to action in hero sections or when a less prominent action is needed. Its inverse color scheme suggests a 'build' action for dark backgrounds.

Background rgba(255, 255, 255, 0.08), Ink Black text, 100px border-radius for a pill shape.

### Product Features Card

**Role:** Standard card for content grouping, features, or product showcases. Characterized by soft elevation.

Background Canvas White (#fafafa), 30px border-radius, diffused shadow: rgba(0, 0, 0, 0.03) 0px 30px 30px -2.5px.

### Enclosed Content Block

**Role:** A variant for structured content with internal padding, like forms or detailed information panels, maintaining the elevated card aesthetic.

Background Canvas White (#fafafa), 24px border-radius, padded 30px all sides, diffused shadow: rgba(0, 0, 0, 0.03) 0px 30px 30px -2.5px.

### Subtle Background Card

**Role:** Used for secondary information panels or a background for elements that need less visual weight, blending more into the canvas.

Background Alabaster Gray (#ecebe8), 48px border-radius, 16px padding all sides, no shadow.

### Hero Section Callout

**Role:** A distinct background shape within a hero section, often for a title or subtitle. Its rounded and translucent nature makes it suitable for overlays.

Background rgba(204, 204, 204, 0.2), 60px border-radius, 26px padding all sides, no shadow.

### Navigation Link

**Role:** Top-level navigation item, subtly styled to integrate into the header.

Granite Gray (#6e6e6e) text in Geist font, 14px size, 6px border-radius for interaction states, 10px internal spacing.

### Main Navigation Button

**Role:** Prominent action in the header, signaling a primary user journey.

Background Ink Black (#000000), Canvas White (#fafafa) text, 100px border-radius.

## Layout

The page primarily uses a max-width contained layout section by section, centered on the screen, adhering to a light, open grid. The hero section breaks this with a full-bleed dark background and a central illustration flanked by textual content. Sections often alternate between full-width content blocks and more contained textual descriptions paired with product UI illustrations or screenshots occupying a two-column layout. Vertical rhythm is established by defined section gaps, creating breathing room between distinct content areas. The navigation is a sticky top bar with a primary 'Book a Call' button.

## Imagery

The visual language combines stylized vector illustrations of people and abstract product graphics with embedded product UI screenshots. Illustrations are often dimensional, with subtle shading, and feature a light, almost ethereal quality, often set against the pristine white background or within a contained dark gradient. Product screenshots are typically clean, full UI captures, often presented within mock devices or floating as elevated cards with soft shadows, showcasing the interface directly. Icons are minimalist, outlined, and monochromatic, maintaining a precise and functional style.

## Dos & Donts

### Do

- Prioritize Canvas White (#fafafa) for all primary backgrounds and surfaces.
- Use Bricolage Grotesque (weights 500, 600) for all headings, applying appropriate tight letter-spacing for each size.
- Apply Geist (weights 500, 600) for UI labels, navigation, and body text where precision is key.
- Elevate cards with a subtle, diffused shadow: rgba(0, 0, 0, 0.03) 0px 30px 30px -2.5px.
- Apply 30px border-radius to main content cards and 100px to all buttons for a consistent rounded feel.
- Reserve Electric Orange (#ff4000) strictly for accents, interactive links, and strong highlights, never for large background areas or primary text.
- Maintain comfortable spacing with 10px element gaps and 32px card padding, ensuring visual breathability.

### Don't

- Avoid heavy or high-contrast shadows; only use the subtle, diffused dark shadow.
- Do not introduce new display fonts; stick to Bricolage Grotesque for all headings.
- Never use Electric Orange (#ff4000) as a primary button background unless it's a small, icon-centric interaction.
- Do not use dark backgrounds for sections unless explicitly defined as a full-bleed hero or product showcase with the Dark Overlay Gradient.
- Avoid strong border strokes on cards; rely on elevation shadows or background color variations for differentiation.
- Do not deviate from the established border radii; 30px for cards, 100px for buttons, 6px for controls, and 40px for images ensures consistency.
- Refrain from using bold typefaces for body text; rely on weight 500 or 600 from Geist for emphasis.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #1c1c1c
- background: #fafafa
- border: #000000
- accent: #ff4000
- primary action: no distinct CTA color

Example Component Prompts:
1. Create a hero section headline: 'Your Factory's Next 10 Years of Productivity.' in Bricolage Grotesque, weight 600, size 58px, Obsidian Text (#1c1c1c), letter-spacing -0.052em.
2. Build a footer button: Button text 'Book a Call' in Geist, weight 600, size 16px, Ink Black (#000000) text on a Canvas White (#fafafa) background, 100px border-radius, 16px vertical padding, 20px horizontal padding.
3. Design a product feature card: Main content on Canvas White (#fafafa) background, 30px border-radius, with the specified diffused shadow (rgba(0, 0, 0, 0.03) 0px 30px 30px -2.5px), 32px padding inside.
4. Create a navigation link: 'Pricing' in Geist, weight 500, size 14px, Granite Gray (#6e6e6e), with a 6px border-radius for hover states and 10px padding.
5. Assemble a Ghost button: 'Start my 24h Build' text in Ink Black (#000000), on a rgba(255, 255, 255, 0.08) background, with a 100px border-radius.

---
_Source: https://styles.refero.design/style/a6950b49-8ce4-4330-9499-26ca08061599_
