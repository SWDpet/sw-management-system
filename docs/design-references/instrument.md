# Instrument — Design Reference

> High-contrast typographic canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.instrument.com](https://www.instrument.com) |
| Refero page | [https://styles.refero.design/style/dcd215e5-3511-4e40-87ff-95c095f44ad6](https://styles.refero.design/style/dcd215e5-3511-4e40-87ff-95c095f44ad6) |
| Theme | light |
| Industry | agency |

## Overview

Instrument's visual system evokes a stark, high-contrast, minimalist agency aesthetic. Dominated by a monochrome palette of deep nearly-black gray and pure white, it leverages strong typography as its primary visual identity. Typography is both commanding and refined, with distinctive custom typefaces varying dramatically in size and tracking. Components are lightweight and functional, often using ghosting or subtle background tints against large, open white canvases, emphasizing content and a clean, editorial layout.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#070708` | neutral | Primary text, surface backgrounds for negative space, strong borders, strong button fills |
| White Canvas | `#ffffff` | neutral | Page backgrounds, card surfaces, button text on filled backgrounds, ghost button borders |
| Midtone Gray | `#808080` | neutral | Muted text, secondary navigation, badge borders, subtle iconography |
| Faded Ink | `#0707081f` | neutral | Subtle badge backgrounds, input borders, ghost hover states |
| Translucent White | `#ffffff80` | neutral | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |

## Typography

### Instrument-Sans

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 11px, 12px, 14px, 15px, 16px, 18px, 19px, 23px, 28px, 36px, 352px |
| lineHeight | 0.85, 1.00, 1.11, 1.14, 1.22, 1.26, 1.33, 1.43, 1.45, 1.56, 1.60 |
| letterSpacing | -0.0500em, -0.0250em, -0.0100em, -0.0050em, 0.0050em, 0.0200em, 0.0300em, 0.0500em, 0.0800em, 0.1100em |
| fontFeatureSettings | "liga", "ss12" |
| substitute | Inter |
| role | Primary text for body, navigation, buttons, and most informational elements. Its diverse sizes and precise tracking allow it to span from compact utility text to expansive hero statements. |

### Instrument-Serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 64px, 352px |
| lineHeight | 0.85, 1.13 |
| letterSpacing | -0.0500em, -0.0200em |
| fontFeatureSettings | "liga", "ss12" |
| substitute | Playfair Display |
| role | For large display headlines and hero text. The serif provides a counterpoint to the sans-serif's modernity, adding an editorial gravitas. |

### Instrument-Serif-Italic

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 352px |
| lineHeight | 0.85 |
| letterSpacing | -0.0500em |
| fontFeatureSettings | "liga", "ss12" |
| substitute | Playfair Display Italic |
| role | Used for specific large, stylized headline accents, leveraging its italic form for dramatic effect. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.45 | 0.44 |
| body | 14 |  | 1.33 | 0.056 |
| subheading | 18 |  | 1.22 | -0.09 |
| heading-sm | 23 |  | 1.26 | -0.23 |
| heading | 28 |  | 1.14 | -0.28 |
| heading-lg | 36 |  | 1.11 | -0.36 |
| display | 64 |  | 1.13 | -1.28 |
| display-xl | 352 |  | 0.85 | -17.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 24px |
| inputs | 8px |
| buttons | 999px |
| minimal | 0px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | 0 | Dominant background for pages and sections, creating a spacious, clean feel. |
| Midnight Ink | `#070708` | 1 | Background for bold, immersive sections, video players, and header/footer blocks, providing strong contrast. |

## Components

### Pill Ghost Button

**Role:** Primary navigation and filter controls.

Rounded shape with `999px` border radius, translucent white background at `rgba(255, 255, 255, 0.5)`, `Midnight Ink` text. `0px` inner padding. Minimalist and soft.

### Outline Text Button

**Role:** Secondary actions and category filters.

Text in `Midnight Ink`, no background, with `Midnight Ink` border. Square, `0px` border radius. `0px` inner padding. Emphasizes text over button form.

### Soft Rectangular Button

**Role:** Filter controls on darker backgrounds or for a slightly more defined appearance.

Solid white background `#ffffff`, `Midnight Ink` text, with a `32px` border radius. `0px` inner padding. Offers a soft, distinct action area.

### Filled Square Button

**Role:** Interactive elements requiring strong visual presence on white backgrounds.

Solid `Midnight Ink` background `#070708`, `White Canvas` text, `0px` border radius, `20px` horizontal padding. Used for high-contrast actions.

### Transparent Card

**Role:** Content presentation where the background provides context.

No background color `rgba(0, 0, 0, 0)`, `0px` border radius, no shadow, `0px` padding. Acts as a content wrapper without visual boundaries.

### Rounded Card

**Role:** Visual highlights for individual content blocks.

No background color `rgba(0, 0, 0, 0)`, `24px` border radius, no shadow, `0px` padding. Subtly lifts content from the background.

### Input Field

**Role:** Data entry forms.

Translucent white background `rgba(255, 255, 255, 0.12)`, `White Canvas` text, `White Canvas` border, `8px` border radius, `12px` vertical and `16px` horizontal padding.

### Transparent Badge

**Role:** Minimalist labels and tags.

No background, `Midnight Ink` text, `0px` border radius, `0px` padding. Blends seamlessly with surrounding text.

### Pill Badge

**Role:** Category tags and filters.

Faded Ink background `rgba(7, 7, 8, 0.12)`, `Midnight Ink` text, `999px` border radius, `4px` vertical and `10px` horizontal padding. Provides a soft, distinguishable label.

## Layout

The page primarily uses a full-bleed layout, allowing sections to stretch edge-to-edge. Content within these sections often appears to be contained within an implicit maximum width, centrally aligned. The hero section features large, centered typography over a full-bleed black background. Section rhythm alternates between large, open white canvas areas and immersive, full-bleed black blocks. Content is arranged in alternating text-and-image two-column layouts, often with the text on the left and visual on the right, or in multi-column card grids for portfolio and articles. Navigation is a minimalist top bar, with sticky header behavior. Vertical spacing between sections is consistent, typically using `24px` as a base unit for vertical rhythm.

## Imagery

This system primarily uses product screenshots and abstract graphic artwork in a contained, card-like format. Photography is largely absent, focusing instead on digital renders and stylized brand visuals. Imagery is either contained within clean, rounded or sharp-edged card structures, or used as full-bleed background for hero sections. Icons are minimal, featuring outlined, mono-color designs that align with the stark aesthetic. The visual density is image-heavy in portfolio grids, alternating with text-dominant editorial blocks, where images serve as key content indicators rather than decorative elements.

## Dos & Donts

### Do

- Prioritize `Midnight Ink` text (`#070708`) on `White Canvas` (`#ffffff`) for readability and high contrast.
- Use `Instrument-Sans` for all body text, navigation, and button labels, adjusting sizes and letter-spacing for impact.
- Apply `999px` border-radius to all buttons and badges for a consistent pill-like shape.
- Break up page content with distinct blocks of `Midnight Ink` background for dramatic negative space and visual hierarchy.
- Employ `24px` for internal card padding and image corner rounding to maintain soft block definition.
- Utilize the `Instrument-Serif` typeface sparingly for large, editorial headlines to introduce a layer of sophisticated tension.
- Maintain a compact spacing density, generally utilizing multiples of `4px` with an `8px` base unit for element gaps.

### Don't

- Avoid generic border radii; stick to `0px`, `8px`, `24px`, `32px`, or `999px` as defined.
- Do not introduce strong accent colors; the palette is strictly monochrome with subtle translucent effects.
- Refrain from using shadows or heavy elevation styles; surfaces should remain flat or use minimal transparency.
- Do not deviate from the specified typefaces and their distinct letter-spacing values to preserve the brand's typographic identity.
- Avoid excessive padding or large gaps between elements; maintain a compact, information-dense layout where appropriate.
- Do not use generic system fonts; `Instrument-Sans` and `Instrument-Serif` are central to the brand.
- Do not dilute the high-contrast ethos with mid-tone backgrounds or multiple shades of gray for primary content areas.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #070708
background: #ffffff
border: #070708 (for ghost buttons, inputs)
accent: no distinct accent color
primary action: no distinct CTA color

Example Component Prompts:
1. Create a large display hero section: full-width Midnight Ink background. Headline 'We’re a digital-first design agency where' with 'Instrument-Serif' at 64px, negative letter-spacing, color White Canvas. Subtext 'creativity' with 'Instrument-Serif-Italic' at 352px, color White Canvas, negative letter-spacing and line-height. 
2. Create a 'Pill Ghost Button': text 'Discover More' in Instrument-Sans, weight 400, size 14px, color #070708. Background rgba(255, 255, 255, 0.5), border-radius 999px. No internal padding.
3. Create a 'Rounded Card' for a work entry: no background, 24px border radius. Inside, an image (replace with placeholder) followed by text 'Project Title' in Instrument-Sans, weight 400, size 18px, color #070708, with 24px bottom margin.
4. Create an 'Input Field' for an email address: background rgba(255, 255, 255, 0.12), border 1px solid #ffffff, 8px border radius. Placeholder text 'Email address' in Instrument-Sans, weight 400, size 16px, color #ffffff. Padding 12px vertical, 16px horizontal.

---
_Source: https://styles.refero.design/style/dcd215e5-3511-4e40-87ff-95c095f44ad6_
