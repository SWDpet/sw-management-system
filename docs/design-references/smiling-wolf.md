# Smiling Wolf — Design Reference

> monochromatic editorial precision

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.smilingwolf.co.uk](https://www.smilingwolf.co.uk) |
| Refero page | [https://styles.refero.design/style/75be52f8-4dbe-45da-9a0e-a11bc92f6927](https://styles.refero.design/style/75be52f8-4dbe-45da-9a0e-a11bc92f6927) |
| Theme | light |
| Industry | agency |

## Overview

Smiling Wolf employs a minimalist, high-contrast aesthetic reminiscent of print editorial design. A stark visual dialogue between near-black and off-white creates a sense of gravitas and directness. Typography, featuring custom fonts, is treated with precision, utilizing subtle letter-spacing adjustments and specific ligatures to convey detail and craft. The UI is largely monochromatic, relying on robust typographic hierarchy and precise spacing to define structure rather than color or extensive visual ornamentation. Components are understated, often presented as text-based interactive elements or subtle outlined cards.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink Black | `#000000` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Paper White | `#f7f3f0` | neutral | Page backgrounds, card surfaces, secondary text on dark backgrounds. Creates a soft, almost tactile canvas |
| Charcoal | `#131713` | neutral | Dark section backgrounds, primary text on light backgrounds, elevated surface background. Provides strong contrast against lighter text and elements |
| Muted Ash | `#bebcb9` | neutral | Subtle border colors, secondary text elements. Offers a lighter shade within the dark tonal range |
| Stone Gray | `#858582` | neutral | Tertiary text, subtle separators, disabled states. A low-contrast neutral for supporting information |

## Typography

### Bagoss Standard

| Key | Value |
| --- | --- |
| weight | 385, 400, 520 |
| sizes | 14px, 20px, 59px, 63px |
| lineHeight | 0.98, 1.00, 1.15, 1.40 |
| letterSpacing | 0.0100em, 0.0100em, -0.0100em, -0.0200em |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| substitute | serif |
| role | Primary headings and body text, conveying a distinctive and precise brand voice. The tight letter-spacing and custom features contribute to a refined, almost type-specimen feel. Weights 400 and 520 are used for prominent headings, while 385 is for lighter body text. |

### General Grotesque Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 9px, 10px |
| lineHeight | 1.60 |
| letterSpacing | 0.1000em, 0.1000em |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| substitute | monospace |
| role | Small labels, metadata, and structured information where monospaced precision is desired. The generous letter-spacing enhances readability at small sizes. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | Arial |
| role | System fallback for small utility text like navigation links or ancillary information where a robust, universally available sans-serif is appropriate. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| label | 10 |  | 1.6 | 1 |
| body | 14 |  | 1.4 | 0.14 |
| subheading | 20 |  | 1.15 | 0.2 |
| display-sm | 59 |  | 1 | -0.59 |
| display | 63 |  | 0.98 | -1.26 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 4px |
| buttons | 4px |
| default | 4px |

- **elementGap** — 10px
- **sectionGap** — 60px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Paper White Canvas | `#f7f3f0` | 1 | Dominant background for the overall page and most content sections, providing a soft, almost tactile base. |
| Charcoal Surface | `#131713` | 2 | Used for distinct dark sections, such as hero blocks or footers, creating a strong visual contrast against the Paper White canvas. Content surfaces are elevated on this dark base. |

## Components

### Text Link

**Role:** Navigation, inline actions, and footer links

Uses Ink Black (#000000) or Paper White (#f7f3f0) text, with no explicit underline by default. On interaction, it may underline or change color to reinforce clickability. Radius is implicitly 4px when used as a wider interactive block.

### Ghost Button

**Role:** Secondary calls to action and interactive elements displayed as text links with subtle borders.

No background fill. Uses Ink Black (#000000) for text and a 1px border. Padding is implicitly driven by content and line height, with inner vertical spacing of 4px and horizontal of 8px. Border radius is 4px. Example: 'Start a Project' buttons.

### Footer Item

**Role:** Structured list items in the footer

Text in Ink Black (#000000) on a Charcoal (#131713) background, with a 1px Ink Black (#000000) border separating items. Internal row gap is 10px. Elements have a 4px radius.

### Featured Project Card

**Role:** Showcasing project previews

A container with no explicit border or shadow, relying on content and negative space (#f7f3f0 background implied for surrounding canvas). Images inside have a 4px border radius. Padding around content in the card is 20px on left/right. Body text within the card uses Charcoal (#131713).

### Navigation Bar

**Role:** Main site navigation

Text links in Ink Black (#000000) or Paper White (#f7f3f0) on a contrasting background. Uses `sans-serif` at 12px weight 400. Horizontal element gaps are 10px.

## Layout

The page primarily employs a max-width contained layout, though specific sections (like the 'Work with us' section) can be full-bleed with a dark background. The hero section features a centered, large headline over a dark background. Content arrangement often utilizes contrasting background blocks, with text-heavy sections or lists having defined vertical rhythm. A fluid column-based layout is implied for project showcases, adapting to available space for image and description blocks. The navigation is a sticky top bar with minimal links and a footer that uses a stacked, bordered list of links.

## Imagery

This site predominantly uses embedded product images and stylized photography within content areas rather than large decorative hero visuals. Imagery, when present, tends to be contained and presented with a 4px border radius. There's a suggestive use of highly stylized visual content in project showcases, often with a subtle desaturated or tinted quality, making the UI itself the primary visual focus. Icons are minimalistic, likely outline-based, colored with Ink Black.

## Dos & Donts

### Do

- Use Ink Black (#000000) for primary text on Paper White (#f7f3f0) backgrounds and Paper White (#f7f3f0) on Charcoal (#131713) backgrounds to maintain high contrast.
- Apply `Bagoss Standard` `letter-spacing: -0.0100em` for all large headings (59px and 63px) to create a tight, editorial feel.
- Maintain a default border radius of 4px for all interactive elements and media containers.
- Implement consistent vertical spacing of 60px between major sections to ensure ample breathing room.
- Use `General Grotesque Mono` with `letter-spacing: 0.1000em` for small utilitarian text like tags or meta information to enhance legibility at small sizes.
- Utilize a ghost button style with a 1px Ink Black (#000000) border for secondary actions, avoiding filled buttons unless explicitly defined.

### Don't

- Avoid arbitrary color usage; stick to the defined black, white, and gray palette for UI elements.
- Do not use generic sans-serif fonts for headlines; `Bagoss Standard` is essential for brand identity.
- Resist adding unnecessary shadows or complex gradients; the design relies on flat surfaces and high contrast.
- Do not vary border radii; a consistent 4px radius is applied across all shapely elements.
- Avoid tight vertical spacing in sections; ensure a minimum `sectionGap` of 60px.
- Do not introduce strong accent colors for interactive elements unless proven by explicit brand guidelines outside this system. The system uses achromatic feedback.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #f7f3f0
border: #000000
accent: no distinct accent color
primary action: no distinct CTA color

Example Component Prompts:
1. Create a dark hero section with a large heading: `Charcoal (#131713)` background. Headline: `Work with us or say hello.` in `Paper White (#f7f3f0)`, `Bagoss Standard` `display` size (63px), weight 520, `letter-spacing -1.26px`. Below it, a ghost button: `Start a Project` in `Paper White (#f7f3f0)`, 1px `Paper White (#f7f3f0)` border, 4px radius, `Bagoss Standard` `subheading` size (20px), weight 400.
2. Create a footer section: `Charcoal (#131713)` background. Inner content: Vertical list of links, each using `Ink Black (#000000)` border, 1px thick, font `General Grotesque Mono` `label` size (10px), `letter-spacing 1px`, `Paper White (#f7f3f0)` text. The list items have `rowGap: 10px`.

---
_Source: https://styles.refero.design/style/75be52f8-4dbe-45da-9a0e-a11bc92f6927_
