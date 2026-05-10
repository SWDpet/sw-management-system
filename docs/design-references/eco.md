# Eco — Design Reference

> Architectural tech blueprint. Polished surfaces and precise typography overlay an expansive, slightly muted cityscape.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://eco.com](https://eco.com) |
| Refero page | [https://styles.refero.design/style/9cc537fc-97d8-4632-8703-f9aa296c2206](https://styles.refero.design/style/9cc537fc-97d8-4632-8703-f9aa296c2206) |
| Theme | mixed |
| Industry | fintech |

## Overview

Eco presents a refined, almost architectural aesthetic that blends high-tech precision with a subtle, underlying warmth. The visual impression is one of confident authority, conveyed through a largely achromatic palette punctuated by a single, vibrant gradient. Large, impactful typography with precise letter-spacing anchors sections, while soft, rounded elements provide a counterpoint to the generally sharp interface, creating a balanced and approachable digital experience for complex financial technology.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| White Smoke | `#ffffff` | neutral | Primary page background, text on dark backgrounds, active states. |
| Off-White Mist | `#efefef` | neutral | Secondary backgrounds, subtly lifted UI elements, button fills. |
| Midnight Ink | `#0f111a` | neutral | Primary text on light backgrounds, dark mode backgrounds. |
| Dark Charcoal | `#000000` | neutral | Deepest dark backgrounds, strong text contrast. |
| Near Black | `#141414` | neutral | Subtle background depth in dark sections. |
| Light Steel | `#aeaeae` | neutral | Subtle text emphasis, inactive states, borders in light themes. |
| Graphite Grey | `#2a2a2a` | neutral | Button backgrounds, dark neutral elements. |
| Mid Grey | `#222222` | neutral | Text on very light backgrounds, subtle borders. |
| Pale Ash | `#a0a0a0` | neutral | Muted headings, secondary text, placeholders. |
| Skybound Gradient | `#1c53bd` | brand | Highlight elements, interactive indicators, brand-specific imagery – draws attention and signifies modernity. |
| Cloud Grey | `#a6b8d1` | accent | Subtle interactive link color, non-critical emphasis – a soft, cool blue-gray that hints at interaction without aggressive saturation. |

## Typography

### Interdisplay

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px, 24px, 40px, 84px, 90px, 96px |
| lineHeight | 0.90, 0.95, 1.06, 1.20, 1.43, 1.50 |
| letterSpacing | -0.0400em, -0.0300em, -0.0200em, -0.0100em, 0.0180em |
| substitute | Inter |
| role | Primary display and heading font, used for impactful statements. The tight letter-spacing at larger sizes gives it a contemporary and precise feel. |

### Roobert

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 48px, 84px, 96px |
| lineHeight | 0.95, 1.06, 1.20, 1.33 |
| letterSpacing | -0.0400em, -0.0300em, -0.0200em, -0.0100em |
| substitute | Montserrat |
| role | Secondary display and heading font, also used for navigation and important interactive elements. Its strong presence at larger scales complements Interdisplay. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px, 22px |
| lineHeight | 1.00, 1.20, 1.50 |
| letterSpacing | -0.0200em, -0.0100em |
| substitute | Inter |
| role | Standard body text and links. Slightly tighter letter-spacing ensures readability and a modern appearance. |

### Fragmentmono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px |
| lineHeight | 1.00, 1.50, 1.71 |
| letterSpacing | -0.0100em |
| substitute | Space Mono |
| role | Used for code snippets, secondary labels, and data points, providing a technical contrast to the primary sans-serifs. |

### Aeonik Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.10 |
| letterSpacing | -0.0400em |
| substitute | IBM Plex Mono |
| role | Specialized monospaced text, likely for unique technical labels or data. Its tight letter-spacing reinforces a precise data-driven feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1 | -0.01 |
| body | 16 |  | 1.5 |  |
| subheading | 22 |  | 1.5 | -0.01 |
| heading | 24 |  | 1.2 | -0.02 |
| heading-lg | 40 |  | 1.2 | -0.02 |
| display | 96 |  | 0.95 | -0.04 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| large | 128px |
| inputs | 8px |
| buttons | 8px |
| default | 8px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Feature Stat Block

### Feature Cards Row

### Primary Filled Button

**Role:** Main call to action for interactive steps.

Filled with Graphite Grey (#2a2a2a), White Smoke text (#efefef), 8px border-radius, 8px vertical padding, 16px horizontal padding. No border, for a soft, inviting feel.

### Secondary Filled Button

**Role:** Alternative call to action, less prominent than primary.

Filled with Dark Gray (#302f2c), White Smoke text (#ffffff), 8px border-radius, 8px vertical padding, 20px horizontal padding. No border, offering a slightly bolder presence than primary.

### Text Link Button

**Role:** Minimalistic interactive elements, inline with text.

Transparent background, Mid Grey text (#222222), no border-radius. 5px vertical padding, 4px horizontal padding, for a subtle click target.

### Input Field

**Role:** Data entry fields across the application.

Background rgba(240, 240, 235, 0.18), White Smoke text (#ffffff), 8px border-radius. 16px vertical padding, 24px horizontal padding, promoting clear data input.

### Navigation Link

**Role:** Primary navigation items in headers and footers.

White Smoke text (#ffffff) on dark backgrounds, or Midnight Ink text (#0f111a) on light backgrounds. No background, typically 4px right margin, using Interdisplay font.

### Hero Headline

**Role:** Largest, most prominent text on key landing sections.

Roobert font, 96px size, 0.95 line-height, -0.0400em letter spacing, White Smoke color (#ffffff). Centered, commanding attention.

### Subheadline / Body Intro

**Role:** Supportive text for headlines or section introductions.

Interdisplay font, 24px size, 1.20 line-height, White Smoke color (#ffffff). Provides context to the large headlines.

## Layout

The site employs a mixed layout model, featuring full-bleed sections that transition into contained content sections. The hero section is a full-viewport, dark-themed area with a centered headline over a muted cityscape background, establishing a grand and serious tone. Following sections alternate between light and dark backgrounds, creating a clear visual rhythm. Content within these sections tends to be centrally aligned or uses a simple two-column arrangement, often text-left/image-right or vice-versa, for clear informational flow. Vertical spacing between sections is generous and consistent, using a `48px` `sectionGap`. Card grids, when present, likely feature `0px` padding, emphasizing their content directly. The navigation is a fixed top bar on light backgrounds, becoming active/visible on scroll but initially transparent against the dark hero, suggesting a subtle, less intrusive hierarchy.

## Imagery

The visual language for imagery is characterized by large, full-bleed photography, predominantly cityscapes and architectural scenes that serve as atmospheric backdrops rather than explicit content. Treatment involves a muted, often cool-toned filter applied to photographs, creating a mood that is serious and expansive. Images are typically contained full-bleed within sections, without rounded corners or masks, allowing them to fill the visible space. In other instances, there are abstract, subtle graphics or gradients used decoratively, providing visual interest without distracting from textual content. Product screenshots or specific UI elements are integrated cleanly within sections, usually with a dark, muted background, emphasizing the digital interface itself without much surrounding decoration. Icons are generally small, monochrome, and functional, adhering to an outlined style for subtle interaction hints. The overall density of imagery is balanced; it supports the text and theme but doesn't overwhelm the information.

## Dos & Donts

### Do

- Do use `Roobert` with `96px` size and `-0.0400em` letter spacing for hero headlines to maintain visual impact and precision.
- Do apply `8px` border-radius uniformly to all interactive elements like buttons and input fields.
- Do leverage the `Skybound Gradient` (`linear-gradient(90deg, rgb(28, 83, 189) 71%, rgb(83, 173, 254))`) only for key calls-to-action or distinct visual indicators.
- Do use `48px` as the standard `sectionGap` to ensure consistent vertical rhythm between major content blocks.
- Do prioritize `Midnight Ink (#0f111a)` for text on light backgrounds and `White Smoke (#ffffff)` for text on dark backgrounds for accessibility and brand consistency.
- Do use `Fragmentmono` for all technical code-like text or data displays, ensuring it is `14px` with a `1.00` line-height.
- Do align major content centrally within `pageMaxWidth` when an explicit max-width is later established, or maintain a full-bleed layout as seen in initial hero sections.

### Don't

- Don't use `Skybound Gradient` merely for decorative purposes; reserve it for functional or brand-critical highlights.
- Don't introduce additional border-radius values beyond `8px`, `12px`, and `128px` to preserve the established shape vocabulary.
- Don't deviate from the specified tight letter-spacing for `Interdisplay` and `Roobert` at larger sizes; it is a signature trait.
- Don't use highly saturated colors for backgrounds or large areas; maintain the largely achromatic base palette.
- Don't apply `box-shadow` for elevation on cards or containers; the system relies on background color shifts for depth.
- Don't use `Off-White Mist (#efefef)` for primary text, as it's intended for secondary backgrounds and subtle lifts, not high-contrast text.
- Don't introduce new font families; the current selection (Interdisplay, Roobert, Inter, Fragmentmono, Aeonik Mono) is curated for purpose.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: `#0f111a` (Midnight Ink)
- Background: `#ffffff` (White Smoke)
- CTA: `#2a2a2a` (Graphite Grey)
- Border: `rgba(0,0,0,0)` (Transparent)
- Accent: `linear-gradient(90deg, rgb(28, 83, 189) 71%, rgb(83, 173, 254))` (Skybound Gradient)

### 3-5 Example Component Prompts
1. **Create a Primary Filled Button:** `background-color: #2a2a2a; color: #efefef; border-radius: 8px; padding: 8px 16px; font-family: Interdisplay; font-weight: 400; font-size: 14px; line-height: 1.43; letter-spacing: -0.0100em;`
2. **Generate a Hero Headline:** `font-family: Roobert; font-weight: 400; font-size: 96px; line-height: 0.95; letter-spacing: -0.0400em; color: #ffffff; text-align: center;`
3. **Design an Input Field:** `background-color: rgba(240, 240, 235, 0.18); color: #ffffff; border-radius: 8px; padding: 16px 24px; font-family: Inter; font-weight: 400; font-size: 16px; line-height: 1.50;`
4. **Build a Standard Body Text Paragraph:** `font-family: Inter; font-weight: 400; font-size: 16px; line-height: 1.50; color: #0f111a; letter-spacing: -0.0100em;`
5. **Construct a Secondary Section Subheading:** `font-family: Interdisplay; font-weight: 400; font-size: 24px; line-height: 1.20; letter-spacing: -0.0200em; color: #0f111a;`

---
_Source: https://styles.refero.design/style/9cc537fc-97d8-4632-8703-f9aa296c2206_
