# Langbase — Design Reference

> Midnight Terminal, Pulsing Data

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://langbase.com](https://langbase.com) |
| Refero page | [https://styles.refero.design/style/ad48f4ad-42c0-4c91-a189-fa7a73a7a9e9](https://styles.refero.design/style/ad48f4ad-42c0-4c91-a189-fa7a73a7a9e9) |
| Theme | dark |
| Industry | ai |

## Overview

Langbase employs a 'dark screen, vibrant code' aesthetic, featuring a predominantly dark canvas illuminated by sharp white text and subtle, precise borders. Gradients appear as dynamic, granular data visualizations rather than soft background fills, implying digital activity. Typography is assertive yet clean, with high contrast serving functional clarity against the deep background. Components are minimalist, relying on subtle borders and high-contrast text for visual definition, reinforcing a technical and focused user experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Screen | `#000000` | neutral | Page backgrounds, card surfaces, UI elements, button backgrounds for ghost buttons |
| Ghost Ink | `#232324` | neutral | Primary UI borders, subtle surface accents, structural dividers |
| Canvas White | `#fafafa` | neutral | Primary text color for headlines and body, filled button backgrounds, high-contrast UI elements |
| Ash Gray | `#a1a1aa` | neutral | Secondary text for body copy, muted icons, disabled states |
| Slate Text | `#454546` | neutral | Muted text elements, helper text, subtle interface details |
| Smoke Border | `#5c5c61` | neutral | Fine element borders, subtle iconography strokes |
| Subtle Cream | `#ebeced` | neutral | Lightest UI text for rare highlights or specific text states |
| Deep Plum Gradient | `#f6d1ac` | accent | Decorative code block gradients, visualization elements, representing data flow and transformation; Dynamic accents in gradients, representing data processing. Used as base for various process gradients |
| Code Block Highlight | `#a1a1a1` | accent | Subtle highlight gradient for code block backgrounds or simulated active lines |

## Typography

### GeistSans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 14px, 16px, 18px, 20px, 48px |
| lineHeight | 1.00, 1.17, 1.33, 1.40, 1.43, 1.50, 1.56, 1.71, 1.75 |
| letterSpacing | -0.025 |
| substitute | Inter |
| role | Primary typeface for all UI elements, headings, body text, and links. Its slightly condensed and precise form maintains consistency across varied content, with a subtle letter-spacing adjustment to enhance impact in larger sizes and readability in smaller. |

### GeistMono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 13px, 16px, 60px, 72px |
| lineHeight | 1.00, 1.30, 1.33, 1.50, 1.52 |
| letterSpacing | -0.3 |
| substitute | JetBrains Mono |
| role | Mono-spaced typeface used for representing code, data, and technical specifications. The aggressive negative letter-spacing ensures a compact, engineered feel, distinguishing technical content from standard UI text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.43 |  |
| body-sm | 14 |  | 1.43 |  |
| body | 16 |  | 1.43 |  |
| body-lg | 18 |  | 1.43 |  |
| heading-sm | 20 |  | 1.43 |  |
| heading | 48 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pills | 9999px |
| default | 4px |
| buttonsSmall | 12px |
| buttonsRounded | 100px |

- **elementGap** — 12px
- **sectionGap** — 24px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Components

### Filled Primary Button

**Role:** Call to action button for primary actions.

White text (#fafafa) on a Canvas White (#fafafa) background, with a #232324 border, 100px border-radius, and 10px vertical, 20px horizontal padding.

### Ghost Button

**Role:** Secondary action or navigational link button.

White text (#fafafa) with a transparent background, a #27272a border, 100px border-radius, and 10px vertical, 20px horizontal padding.

### Subtle Tag Button

**Role:** Small, informational tags within a dark section.

Ghost Ink (#000000) background with Canvas White (#fafafa) text, 0px border-radius, and 4px padding on all sides.

### Card Button

**Role:** Prominent interactive elements for larger sections, such as feature cards or navigation options.

Charcoal transparent background (rgba(28, 28, 30, 0.8)) with Canvas White (#fafafa) text, a #27272a border, 12px border-radius, and 16px vertical, 24px horizontal padding.

### Header Navigation Link

**Role:** Primary navigation links in the header.

GeistSans font, #a1a1aa text color with a #0e0e10 background. No distinct border, padding 4px and margin 24px as a stack.

### Grid Cell

**Role:** Container for partner logos or similar structured content.

Uses a #232324 border with a #000000 background. Text is #fafafa with a 12px padding.

## Layout

The page primarily uses a full-bleed dark page model, with content centered within implicit maximum widths. The hero section features a centered headline and subtext over an extensive abstract gradient graphic. Sections maintain consistent vertical spacing, often indicated by the grid lines on the dark canvas, and transition seamlessly without explicit visual dividers. Content is arranged in alternating patterns, frequently showcasing text alongside abstract visualizations or a grid of partner logos. The navigation is a sticky top bar, minimalist and high-contrast against the dark background. The layout is spacious, allowing crucial information and dynamic graphics to breathe.

## Imagery

The visual language predominantly features abstract, dynamic gradients resembling data processing or code streams, set against deep dark backgrounds. These elements are not purely decorative but give the impression of complex systems at work. Iconography is minimalist, outlined, and monochromatic, maintaining a purely functional role. There is a strong absence of photography or illustrative characters, signaling a focus on pure UI and technical functionality. Imagery serves to explain or enhance the UI through abstract visual representations rather than literal depictions, with a high density of these 'data' elements.

## Dos & Donts

### Do

- Use GeistSans for all text content, reserving GeistMono only for explicit code or data rendering.
- Maintain high contrast foreground/background relationships; aim for Canvas White (#fafafa) text on Midnight Screen (#000000) or Ghost Ink (#232324) surfaces.
- Utilize Ghost Ink (#232324) for all primary borders and dividers to maintain the dark, structured aesthetic.
- Apply 9999px for pill-shaped elements and 100px for main buttons, using 12px for larger interactive cards/buttons and 4px as a global default for small UI elements.
- Employ the gradient effects only for data visualizations or background textures that imply activity, not as primary CTA fills.
- Prioritize visual hierarchy through text size and weight changes with consistent letter-spacing adjustments, not through color saturation.
- Use 4px as the base unit for all spacing, ensuring subtle gaps are 4px, content spacing is 12px, and component internal padding at 24px.

### Don't

- Avoid using highly saturated colors for backgrounds or large fills; color should primarily be used for accents or functional highlights.
- Do not deviate from GeistSans or GeistMono for any textual content.
- Do not use generic box-shadows without specifying rgba(0, 0, 0, 0.1) 0px 1px 3px 0px, rgba(0, 0, 0, 0.1) 0px 1px 2px -1px or otherwise ensuring a subtle, dark-mode appropriate style.
- Do not introduce new border radii beyond 4px, 12px, 100px, and 9999px.
- Do not use more than two levels of text color density per section to prevent visual clutter; stick to Canvas White (#fafafa) and Ash Gray (#a1a1aa).
- Do not use gradients as primary solid colors in components; they are for dynamic, textural effects.
- Avoid large hero imagery; prefer abstract graphics or code-like visualizations over photographs.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
text: #fafafa
background: #000000
border: #232324
accent: #f6d1ac (gradient base)
primary action: #fafafa (filled action)

**3-5 Example Component Prompts**
1. Create a Primary Action Button: #fafafa background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
3. Create a partner logo grid: Midnight Screen (#000000) background. Each logo container is a Grid Cell with a #232324 border, #000000 background, 12px padding, displaying a #fafafa logo. The text 'Trusted by developers at 5000+ companies' uses GeistSans at 16px weight 400, #a1a1aa.

---
_Source: https://styles.refero.design/style/ad48f4ad-42c0-4c91-a189-fa7a73a7a9e9_
