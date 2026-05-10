# monopo saigon — Design Reference

> Shifting gradient depths on frosted glass

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://monopo.vn](https://monopo.vn) |
| Refero page | [https://styles.refero.design/style/3e52dd36-6ab1-48c6-bc40-47ef6d33abc2](https://styles.refero.design/style/3e52dd36-6ab1-48c6-bc40-47ef6d33abc2) |
| Theme | dark |
| Industry | agency |

## Overview

monopo saigon operates with a sophisticated dark aesthetic, employing a backdrop of organic, shifting gradients that give the impression of fluid, sculpted surfaces. The UI elements are minimal and translucent, appearing as if etched onto or floating within this rich, atmospheric background. Typography is stark white against dark, providing a strong contrast for readability while maintaining an understated elegance. The overall impression is one of artful depth and a restrained, almost ethereal digital presence.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Canvas | `#000000` | neutral | Primary background for pages, cards, and dark-themed sections |
| Frost White | `#ffffff` | neutral | Primary text color, link defaults, borders for ghost components, and accents against dark backgrounds. Used for text on primary buttons |
| Deep Shadow | `#181818` | neutral | Secondary text in footers and less prominent information. Subtly darker borders |
| Whisper Gray | `#6d6d6d` | neutral | Muted body text and auxiliary text where lower contrast is desired |
| Misty Gray | `#636363` | neutral | Background for subtle, low-contrast interactive elements like the cookie consent button |
| Deep Ocean Gradient | `#a0e0ab` | brand | Atmospheric background for hero sections and full-bleed visual elements, creating an immersive, fluid environment |

## Typography

### Roobert

| Key | Value |
| --- | --- |
| weight | 300, 400, 600 |
| weights | 300, 400, 600 |
| sizes | 11px, 12px, 16px, 18px, 29px, 30px, 39px, 45px, 54px, 78px, 94px, 225px |
| lineHeight | 0.70, 0.76, 1.10, 1.15, 1.19, 1.21, 1.22, 1.24, 1.25, 1.36, 1.39, 1.58, 1.82 |
| letterSpacing | normal |
| substitute | system-ui, sans-serif |
| role | Primary brand typeface for all headings, body text, links, and buttons. Its wide range of weights and sizes supports a detailed typographic hierarchy, from subtle metadata to commanding display text. The default 'normal' letter spacing keeps it highly legible. |

### Raleway

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 54px |
| lineHeight | 1.39 |
| letterSpacing | normal |
| substitute | serif |
| role | Used for specific heading elements, providing an alternative, slightly more classic feel than Roobert. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 9px, 16px |
| lineHeight | 1.15, 1.32 |
| role | system-ui — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.58 |  |
| body | 16 |  | 1.25 |  |
| subheading | 18 |  | 1.22 |  |
| heading-sm | 29 |  | 1.21 |  |
| heading | 39 |  | 1.15 |  |
| heading-lg | 54 |  | 1.39 |  |
| display | 225 |  | 0.7 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 10px |
| buttons | 75.024px |

- **elementGap** — 14px
- **sectionGap** — 46px
- **cardPadding** — 34px
- **pageMaxWidth** — 1078px

## Components

### Ghost Button - Light Border

**Role:** Interactive element allowing light interaction without dominating the dark background. Features a very subtle border.

Background: rgba(0, 0, 0, 0), Text: #000000, Border: 1px solid rgba(255, 255, 255, 0.3) on top only, Radius: 75.024px, Padding: 1px 6px.

### Text Link Button

**Role:** Minimal interactive element, appearing as simple text. Used for navigation and tertiary actions.

Background: rgba(0, 0, 0, 0), Text: #000000, No border, Radius: 0px, Padding: 0px.

### Filled Cookie Consent Button

**Role:** Primary action button within transient UI elements like cookie consent pop-ups. Offers a clear, actionable contrast.

Background: rgba(55, 55, 55, 0.78), Text: #ffffff, Border: 1px solid #ffffff, Radius: 75.024px, Padding: 11.232px 33.696px.

### Dark Text Link Button

**Role:** Minimal interactive element for navigation and tertiary actions, in context where text color should be white.

Background: rgba(0, 0, 0, 0), Text: #ffffff, No border, Radius: 0px, Padding: 0px.

### Information Card Overlay

**Role:** Base card element for presenting content. Designed to blend seamlessly into the background, letting content take precedence.

Background: rgba(0, 0, 0, 0), Border-radius: 0px, No shadow, Padding: 0px.

## Layout

The page primarily uses a full-bleed layout for its immersive background gradients, with content contained within a consistent max-width of 1078px, centered on the screen. The hero section features a large, centered headline directly on the animated gradient background. Sections maintain a consistent vertical rhythm, with generous section gaps of 46px. Content elements are typically stacked or arranged in minimal two-column text-left/image-right patterns (where 'image' refers to the abstract gradient visuals), but all elements appear to live within the main content well. The header is a sticky top navigation bar with minimal text links.

## Imagery

The site deploys abstract, organic gradients and translucent spherical shapes as primary background visuals, creating a sense of depth and fluid motion. There is an absence of traditional photography or illustrations. Icons (if present) are minimal, outlined, and monochromatic, matching the overall dark and understated UI. The visual density is image-heavy in terms of atmospheric graphics, but text-dominant for content presentation, allowing the background to provide mood without distracting from information.

## Elevation philosophy

The design intentionally avoids traditional box-shadows. Instead, depth and hierarchy are communicated through the use of rich, organic background gradients and nuanced color tints on textual and interactive elements, giving the impression of elements floating or subtly recessed within a volumetric space rather than casting shadows upon a flat surface.

## Dos & Donts

### Do

- Prioritize Roobert as the primary typeface for all textual content, utilizing its diverse weights to establish hierarchy.
- Maintain a spacious layout with a base unit of 4px and aim for an element gap of 14px to ensure visual breathing room.
- Use Midnight Canvas (#000000) for all primary backgrounds and Frost White (#ffffff) for primary text to ensure high contrast.
- Apply a border-radius of 75.024px to all buttons for a distinctly rounded, pill-like appearance.
- Implement the Deep Ocean Gradient (linear-gradient(90deg, rgb(160, 224, 171), rgb(255, 172, 46) 50%, rgb(165, 45, 37))) as a background for hero and large interactive sections.
- Ensure interactive elements like buttons and links use Frost White (#ffffff) text against dark backgrounds unless a specific muted tone (Whisper Gray #6d6d6d) is explicitly called for.
- Use a subtle 1px border with rgba(255, 255, 255, 0.3) for ghost buttons to maintain a minimalist aesthetic.

### Don't

- Avoid using harsh, saturated accent colors that would disrupt the site's subdued and atmospheric palette.
- Do not introduce square or sharp borders on interactive elements; button radii should always be 75.024px.
- Refrain from using strong box-shadows or heavy elevation, as the design relies on gradient depth rather than layered elements.
- Do not deviate from the specified Roobert and Raleway font families; avoid generic system fonts for branding elements.
- Avoid tight information density; maintain spacious relationships between elements and sections.
- Do not treat #636363 as a primary action color; reserve it for specific, muted interactive elements like secondary consent buttons.
- Never use solid color backgrounds in feature sections where the organic gradient is intended to create atmosphere.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000000
border: rgba(255, 255, 255, 0.3)
accent: none observed
primary action: no distinct CTA color

Example Component Prompts:
1. Create a primary page section: Full-bleed background with Deep Ocean Gradient. Headline 'United, Unbound' using Roobert weight 400 at 54px, color Frost White (#ffffff), centered within the 1078px max-width content area.
2. Create a ghost navigation button: Text 'WORK' using Roobert weight 400 at 16px, color #000000. Background transparent. Top border 1px solid rgba(255, 255, 255, 0.3). Radius 75.024px. Padding 1px 6px.
3. Create a cookie consent dialog button: Text 'Accept' using Roobert weight 400 at 16px, color Frost White (#ffffff). Background Misty Gray (#636363). Border 1px solid Frost White (#ffffff). Radius 75.024px. Padding 11.232px 33.696px.

---
_Source: https://styles.refero.design/style/3e52dd36-6ab1-48c6-bc40-47ef6d33abc2_
