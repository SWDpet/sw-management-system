# Haley Park — Design Reference

> Gothic manuscript on dark parchment

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://haleys.design](https://haleys.design) |
| Refero page | [https://styles.refero.design/style/c47654a9-7d7a-4b2c-8e0a-cd9296719c69](https://styles.refero.design/style/c47654a9-7d7a-4b2c-8e0a-cd9296719c69) |
| Theme | dark |
| Industry | design |

## Overview

Haley Park employs a 'Gothic manuscript' aesthetic: a deeply saturated, muted forest green canvas provides a rich backdrop for delicate, antiqued typography, often accompanied by thin, almost calligraphic underlines or borders. Surface treatments are minimal, favoring ghostly lines and subtle background patterns over solid cards or elevation. The visual system feels quietly scholarly and meticulously crafted, with a restrained use of contrast to maintain a calm, focused atmosphere. The typography is a key expressive element, mixing classical serifs with a distinctive, almost whimsical display font.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Forest Canopy | `#143930` | neutral | Page background, heading background fills, primary surface. Creates a deep, contemplative atmosphere |
| Parchment White | `#f8f2de` | neutral | Primary text, link text, borders, decorative accents. Its slight warmth prevents a stark contrast with the dark background, maintaining the antique feel. Used for outlined ghost buttons |
| Moss Line | `#456859` | neutral | Subtle borders, secondary decorative elements, icon outlines. Offers just enough contrast for structural lines without disrupting the soft visual texture |

## Typography

### EditorialOld

| Key | Value |
| --- | --- |
| weight | 200 |
| weights | 200 |
| sizes | 16px, 19px, 21px, 22px, 27px, 32px |
| lineHeight | 1.20, 1.30, 1.50 |
| letterSpacing | 0.0040em, 0.0050em |
| substitute | Lora |
| role | Primary body text, links, list items, and smaller headings. Its classic serif forms contribute to the traditional, manuscript-like character. |

### Wispy

| Key | Value |
| --- | --- |
| weight | 100 |
| weights | 100 |
| sizes | 96px |
| lineHeight | 1.00 |
| letterSpacing | -0.96px at 96px |
| substitute | Recursive Sans |
| role | Display headlines – extremely light weight and tight tracking create an ethereal, elegant title that feels whispered rather than shouted. |

### Old Standard TT

| Key | Value |
| --- | --- |
| weight | 200 |
| weights | 200 |
| sizes | 13px, 16px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| substitute | Old Standard TT |
| role | Auxiliary body text, helper text, and secondary information presented in a highly readable, classic serif. |

### GTA

| Key | Value |
| --- | --- |
| weight | 300 |
| weights | 300 |
| sizes | 15px, 21px |
| lineHeight | 1.60 |
| letterSpacing | 0.0040em |
| substitute | Playfair Display |
| role | Secondary textual content, possibly for smaller annotations or details where a slightly more contemporary serif is desired. |

### bodoni-classic-ornaments

| Key | Value |
| --- | --- |
| weight | 200, 300, 400 |
| weights | 200, 300, 400 |
| sizes | 16px, 21px |
| lineHeight | 1.44, 1.50 |
| letterSpacing | 0.0110em |
| substitute | Decorative Dingbats or Zapf Dingbats |
| role | Decorative icons and ornamental text elements, evoking historical printing. |

### altesse-std-24pt

| Key | Value |
| --- | --- |
| weight | 300 |
| weights | 300 |
| sizes | 27px |
| lineHeight | 1.10 |
| letterSpacing | normal |
| substitute | Perpetua Titling MT |
| role | A distinct heading font for specific sections, offering a unique, elegant serif character that complements the primary heading style. |

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 100, 400 |
| sizes | 16px |
| lineHeight | 1.5 |
| role | ui-sans-serif — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 16 |  | 1.3 |  |
| body-sm | 19 |  | 1.3 |  |
| body | 21 |  | 1.3 |  |
| body-lg | 22 |  | 1.3 |  |
| heading-sm | 27 |  | 1.3 |  |
| heading | 32 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| circle | 64px |
| default | 4.8px |

- **elementGap** — 11px
- **sectionGap** — 26px
- **cardPadding** — 26px
- **pageMaxWidth** — 

## Components

### Ghost Navigation Link

**Role:** Header navigation items and project links

Text in Parchment White (#f8f2de) on a transparent background, with a 1px bottom border in Parchment White. No padding beyond content spacing. Letter spacing matches the corresponding font style. Interactivity is subtle, focused on text and border changes rather than solid fills. Uses EditorialOld font.

### Project Card (Outlined)

**Role:** Containers for project listings, often appearing in grid layouts.

Transparent background with a 1px Parchment White (#f8f2de) border. Text in Parchment White (#f8f2de). Uses body-level typography from EditorialOld. Radius is 0px, reinforcing a traditional, unembellished aesthetic.

### Section Divider with Text

**Role:** Visual separation between content sections, enhanced with centered text

A thin line, 1px solid Parchment White (#f8f2de), horizontally spanning the content width. Text (e.g., 'Work projects') is centered on this line using Parchment White (#f8f2de) on a Forest Canopy (#143930) background as needed to create a clear break. Spacing includes `elementGap` vertically.

### Ornamental Icon

**Role:** Decorative crosses or geometric patterns

Rendered as SVG fills or strokes in Moss Line (#456859). Used sparingly as visual anchors or section elaborations, drawing from the `bodoni-classic-ornaments` typeface for specific shapes. Adds to the historical, manuscript aesthetic.

## Layout

The page uses a full-bleed layout for its background, but content is visually constrained to a central column (though `pageMaxWidth` is null, the arrangement suggests a conceptual max-width with ample horizontal padding). The hero section features a centered headline over a textured background. Sections are delineated by subtle horizontal dividers often accompanied by centered textual titles. Content is primarily arranged in multi-column grids for project listings (e.g., 2-column or 3-column), using the 'Project Card (Outlined)' component. The overall density is comfortable, with consistent vertical spacing and minimal visual clutter, emphasizing typography and subtle graphic textures. Navigation is a simple, horizontal top bar with ghost links, consistent throughout the experience.

## Imagery

This site features a 'graphics-only' visual language. Imagery consists primarily of highly stylized, almost architectural line art and geometric forms, reminiscent of stained glass or gothic arches, rendered in Moss Line (#456859) or as subtle grayscale patterns on the dark background. Icons are minimal, outlined, and monochromatic, often using complex, antique-inspired symbols derived from `bodoni-classic-ornaments`. Visuals serve a decorative and atmospheric role, adding texture and historical depth rather than conveying explicit content or product functionalities. They are dense in pattern but low in color, maintaining the site's quiet, scholarly mood.

## Dos & Donts

### Do

- Prioritize Forest Canopy (#143930) for all main backgrounds to maintain the deep, dark ambiance.
- Use Parchment White (#f8f2de) exclusively for primary text, links, and borders, reserving it for elements that need to stand out from the dark background.
- Apply Moss Line (#456859) for secondary decorative strokes, subtle outlines, and subdued graphic elements to add detail without high contrast.
- Employ the 'Wispy' font (96px, 100 weight, -0.96px letter-spacing) for primary hero headlines to achieve an ethereal, elegant feel.
- Structure interactive elements like navigation and project listings with text on transparent backgrounds, using only a Parchment White (#f8f2de) border or underline for definition.
- Maintain a comfortable density with element gaps at `11px` and section gaps at `26px` to allow content to breathe without feeling sparse.
- Utilize 0px border-radius for components like project cards and buttons to reinforce a sharp, traditional print aesthetic.

### Don't

- Avoid using bright, saturated colors for UI elements; stick to the muted palette provided.
- Do not introduce heavy shadows or prominent elevation; maintain a flat, layered visual approach with subtle outlines.
- Refrain from using bold or heavy font weights for headlines or primary text where lightness and elegance are key.
- Do not use large, solid background fills for interactive components; prefer ghost styles with borders or underlines.
- Avoid decorative imagery that clashes with the site's 'Gothic manuscript' aesthetic; prioritize line art, subtle textures, or classic ornaments.
- Do not deviate from the specified typefaces and their distinct letter-spacing values to preserve the unique typographic voice.
- Avoid arbitrary border radii; use 0px by default, and 4.8px very sparingly for elements that require a slight softening.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #f8f2de
background: #143930
border: #f8f2de
accent: no distinct accent color
primary action: no distinct CTA color

### 3-5 Example Component Prompts
1. Create a primary headline for a new section: 'Latest Works' using 'Wispy' font, 96px, weight 100, letter-spacing -0.96px, color Parchment White (#f8f2de).
2. Generate a ghost navigation link for 'Contact' using 'EditorialOld' font, 19px, weight 200, color Parchment White (#f8f2de), with a 1px bottom border in Parchment White (#f8f2de).
3. Design a sub-heading: 'Productivity Tools' using 'altesse-std-24pt' font, 27px, weight 300, color Parchment White (#f8f2de), against a Forest Canopy (#143930) background.
4. Create a project card entry with the text 'AI Integration' (EditorialOld, 16px, Parchment White (#f8f2de)) and a sub-text '2023-2024' (Old Standard TT, 13px, Parchment White (#f8f2de)), inside a transparent container with a 1px Parchment White (#f8f2de) border and 0px radius.

---
_Source: https://styles.refero.design/style/c47654a9-7d7a-4b2c-8e0a-cd9296719c69_
