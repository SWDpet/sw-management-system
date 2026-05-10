# Glassnode — Design Reference

> Analytical Console on Dark Steel. The design feels like a sophisticated yet functional tool built for focused data analysis, emphasizing clarity with deliberate restraint.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://glassnode.com](https://glassnode.com) |
| Refero page | [https://styles.refero.design/style/a79b6a74-6b67-4c9f-9d69-bff80869b943](https://styles.refero.design/style/a79b6a74-6b67-4c9f-9d69-bff80869b943) |
| Theme | mixed |
| Industry | crypto |

## Overview

Glassnode's design evokes a sense of understated authority and analytical precision, like an institutional data terminal. Its visual character is built on stark achromatic contrasts, pairing deep charcoal backgrounds with white text and light gray surfaces. A single, cool ultramarine blue functions as the sole accent, appearing only to highlight interactive elements and subtle visual cues, reinforcing a no-nonsense, data-centric identity. The sparing use of rounded corners (2px) on buttons and inputs prevents a harsh, technical feel, while maintaining an overall sharp and deliberate aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Charcoal | `#1a1a1a` | neutral | Dark mode backgrounds, primary headings on dark backgrounds, active button states, immersive section backgrounds. |
| Arctic White | `#ffffff` | neutral | Page backgrounds, text on dark backgrounds, primary headings on light backgrounds. |
| Cloud Gray | `#f7f8fa` | neutral | Section backgrounds, subtle hover states, tertiary button backgrounds. Creates a soft separation against Arctic White. |
| Ash Concrete | `#edeff2` | neutral | Button backgrounds, light interactive elements. Provides a slightly darker, more substantial neutral than Cloud Gray. |
| Stone Whisper | `#dedfe1` | neutral | Borders, dividers, subtle inactive states. Its light touch defines boundaries without being visually dominant. |
| Slate Text | `#5a5a5a` | neutral | Secondary body text and descriptions. Provides lower contrast than pure black for improved reading comfort. |
| Granite Text | `#6f6f6f` | neutral | Badge text, muted links, caption text. A softer gray for less prominent information. |
| Mist Gray | `#a0a0a0` | neutral | Input placeholder text, disabled states. Indicates non-interactive or supplementary content. |
| Deep Marine | `#e2e7fc` | brand | Background for interactive elements, subtle highlights, chart fills. The most prominent saturated color, it signals 'interactive' or 'data point'. |
| Violet Signal | `#8fa5f6` | brand | Chart lines, interactive elements and highlights in darker sections. Provides key visual emphasis against dark backgrounds. |
| Ultramarine Ink | `#6888fb` | brand | Primary brand accent for iconography and subtle interactive states. Its vibrancy is used sparingly for maximum impact. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 12px, 14px, 16px, 20px, 24px, 32px |
| lineHeight | 1.00, 1.20, 1.30, 1.40, 1.50 |
| letterSpacing | normal |
| substitute | system-ui, sans-serif |
| role | Primary text font system-wide. Used for body copy, navigation, buttons, and most informational text. The variety of weights and sizes allows for a clear hierarchy within content sections. |

### Fraktion

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 56px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Inter, system-ui, sans-serif |
| role | Exclusive display typeface for hero headings. Its distinct bold character conveys gravitas and establishes the brand's key message with authority. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1 |  |
| body-sm | 14 |  | 1 |  |
| body | 16 |  | 1.2 |  |
| subheading | 20 |  | 1.3 |  |
| heading | 24 |  | 1.4 |  |
| heading-lg | 32 |  | 1.5 |  |
| display | 56 |  | 1.2 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| inputs | 2px |
| buttons | 2px |
| general | 2px |

- **elementGap** — 16px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Announcement Banner + CTA Buttons

### Feature List — Dark Section

### Research Cards + Email Subscribe

### Primary Navigation Link

**Role:** Interactive element

Text link with `color: #000000`, `padding: 0px 4px 0px 4px`, `border-radius: 2px`. Text uses Inter font, weight 400.

### Hero CTA Button

**Role:** Call to action

Background `backgroundColor: #1a1a1a`, `color: #ffffff`, `border-radius: 2px`, `padding: 9.6px 16px`. Text uses Inter font, weight 400.

### Secondary Outline Button

**Role:** Secondary action

Background `backgroundColor: #f7f8fa`, `color: #000000`, `border-color: #dedfe1`, `border-radius: 2px`, `padding: 32px 0px 0px 32px`. Text uses Inter font, weight 400.

### Feature Teaser Button

**Role:** Informational

Background `backgroundColor: #f7f8fa`, `color: #000000`, `border-color: #dedfe1`, `border-radius: 2px`, `padding: 32px 0px 0px 32px`. Text uses Inter font, weight 400.

### Email Input Field

**Role:** Data entry

Background `backgroundColor: rgba(0, 0, 0, 0)`, `color: #a0a0a0`, `border-color: #808080`, `border-radius: 2px 0px 0px 2px`, `padding: 0px 16px`. Placeholder text `color: #a0a0a0`.

### Research Badge

**Role:** Categorization

Background `backgroundColor: rgba(0, 0, 0, 0)`, `color: #6f6f6f`, `border-radius: 0px`, `padding: 0px`. Text uses Inter font, weight 400, size 12px.

### Subtle Callout Button

**Role:** Informational link

Background `backgroundColor: rgba(0, 0, 0, 0)`, `color: #000000`, `border-color: #000000`, `border-radius: 2px`, `padding: 0px 4px`. Text uses Inter font, weight 400.

## Layout

The site uses a mixed-mode layout, alternating between light and dark sections. The primary page model is max-width contained, with content centered within an implied `1200px` boundary, though the page itself is full-bleed. The hero section features a left-aligned headline and body text against a dark background with an illustrative product screenshot on the right. Subsequent sections alternate between light (`Arctic White`) and dark (`Midnight Charcoal`) backgrounds, primarily using a two-column text-left/visual-right arrangement, or grids for product cards and testimonials. Vertical spacing between these sections is generous at `64px`, providing ample breathing room. The navigation is a sticky top bar, centered, with clearly delineated interactive buttons and dropdowns, all in service of a direct and functional content presentation.

## Imagery

The site uses a combination of product screenshots, abstract data visualizations, and minimalist, brand-colored line illustrations. Product screenshots are contained within a browser-like frame, showcasing interactive charts and data, emphasizing functionality over aesthetic appeal. Abstract data visualizations, often in `Violet Signal` or `Ultramarine Ink` against dark backgrounds, serve a decorative but theme-aligned role, reinforcing the data-centric nature of the product. Illustrations are sparse, simple, and utilize the brand's accent colors, appearing as small explanatory graphics rather than hero images. The overall approach is functional and explanatory, rather than emotive or lifestyle-focused.

## Dos & Donts

### Do

- Prioritize text content against `Arctic White (#ffffff)` and `Midnight Charcoal (#1a1a1a)` for strong contrast.
- Use `Inter` at weight 400 for all body text, ensuring a consistent and clear reading experience.
- Apply `2px` to all interactive element border radii (buttons, inputs) for subtle softening.
- Reserve `Fraktion` weight 700, size 56px, exclusively for hero headings to establish grand emphasis.
- Highlight interactive elements and data points with `Deep Marine (#e2e7fc)` or `Violet Signal (#8fa5f6)` to guide user attention.
- Maintain `16px` as the standard horizontal padding and interior element spacing within layouts.
- Implement `64px` vertical spacing between major page sections to ensure clear content separation.

### Don't

- Avoid using `Deep Marine (#e2e7fc)` or `Violet Signal (#8fa5f6)` for large blocks of text; their vibracy is for accents only.
- Do not deviate from the `2px` border radius for buttons and inputs; maintain the sharp, precise aesthetic.
- Do not use shadows on cards or sections; the design relies on stark background color changes for separation instead of elevation.
- Never introduce additional accent colors outside of the defined ultramarine palette, to preserve the focused, limited palette.
- Do not use `Fraktion` for any text below 56px; it is a display font only, not suitable for body or subheadings.
- Avoid overly complex layouts or decorative elements; the design emphasizes direct presentation of information.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Text (dark theme):** #ffffff
- **Text (light theme):** #000000
- **Background (dark section):** #1a1a1a
- **Background (light section):** #ffffff
- **CTA Button:** #1a1a1a (background), #ffffff (text)
- **Border:** #dedfe1
- **Accent / Chart Line:** #8fa5f6

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Background to `Midnight Charcoal (#1a1a1a)`. Headline: `Fraktion` weight 700, size 56px, `Arctic White (#ffffff)`. Body text: `Inter` weight 400, size 16px, `Arctic White (#ffffff)`. CTA button: `Midnight Charcoal (#1a1a1a)` background, `Arctic White (#ffffff)` text, `2px` radius, `9.6px 16px` padding. Render a placeholder for a product screenshot on the right.
2. **Generate a Feature Card Grid:** `Arctic White (#ffffff)` background for entire section. Each card: `Cloud Gray (#f7f8fa)` background, `2px` radius, `16px` padding. Headline in `Inter` weight 700, size 24px, `Midnight Charcoal (#1a1a1a)`. Body text in `Inter` weight 400, size 16px, `Slate Text (#5a5a5a)`. Feature list items should use `Inter` weight 400, size 16px, `Midnight Charcoal (#1a1a1a) `with a `Violet Signal (#8fa5f6)` icon.
3. **Design a Global Navigation Bar:** Background `Arctic White (#ffffff)`. Logo position left, then navigation links. Links: `Inter` weight 400, size 16px, `Midnight Charcoal (#000000)`, `0px 4px 0px 4px` padding, `2px` radius. CTA button: `Midnight Charcoal (#1a1a1a)` background, `Arctic White (#ffffff)` text, `2px` radius, `9.6px 16px` padding. Ensure `64px` height.

---
_Source: https://styles.refero.design/style/a79b6a74-6b67-4c9f-9d69-bff80869b943_
