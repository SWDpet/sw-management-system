# Hardworkclub — Design Reference

> Midnight Stage Black: content bathed in light, surrounded by deep, velvety darkness.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.hardworkclub.com](https://www.hardworkclub.com) |
| Refero page | [https://styles.refero.design/style/1775bf6a-afdd-48b1-8435-b92ec585c674](https://styles.refero.design/style/1775bf6a-afdd-48b1-8435-b92ec585c674) |
| Theme | dark |
| Industry | agency |

## Overview

Hardworkclub presents a dark, minimalist aesthetic, using deep black as the primary canvas for content. Typography is dense and commanding, often rendered in stark white against the dark background. A single, vivid violet accent color is reserved for subtle interactive elements, making its infrequent appearance impactful. The design emphasizes content through strong contrasts, creating a sophisticated and focused visual experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary background, text on light surfaces |
| Ghost White | `#ffffff` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |
| Violet Bloom | `#1200e3` | accent | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Slate Whisper | `#313130` | neutral | Muted borders |

## Typography

### Founders Grotesk

| Key | Value |
| --- | --- |
| weight | 300, 500, 700 |
| weights | 300, 500, 700 |
| sizes | 16px, 18px, 22px, 31px |
| lineHeight | 0.78, 1.00, 1.10, 1.20, 1.21, 1.50, 1.67 |
| letterSpacing | -0.0300em |
| substitute | Inter |
| role | Primary typeface for all text content including headings, body, and interactive elements. Its versatility across weights supports hierarchical differentiation. |

### Maligna

| Key | Value |
| --- | --- |
| weight | 300, 500 |
| weights | 300, 500 |
| sizes | 31px, 61px |
| lineHeight | 1.10, 1.20 |
| letterSpacing | -0.0300em |
| substitute | Playfair Display |
| role | Reserved for large, impactful headings. Its distinct character provides a decorative counterpoint to Founders Grotesk, making headlines feel artistic. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 1.5 | -0.48 |
| subheading | 18 |  | 1.21 | -0.54 |
| heading-sm | 22 |  | 1.1 | -0.66 |
| heading | 31 |  | 1.1 | -0.93 |
| display | 61 |  | 1.2 | -1.83 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 1440px |
| buttons | 1440px |
| default | 8px |

- **elementGap** — 8px
- **sectionGap** — 96px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#000000` | 1 | The foundational background for all pages and sections. |
| Interactive Surface | `#1200e3` | 2 | Subtle background for specific interactive elements when they require a distinct, non-textual highlight. |
| Accent Surface | `#ffffff` | 3 | Used sparingly for inverted sections or to highlight full-bleed content, creating strong visual breaks. |

## Components

### Ghost Button

**Role:** Call to action button for secondary actions or navigation.

Transparent background with a 1px 'Slate Whisper' border, 'Ghost White' text. Padding is 7px bottom, 4.5px top, 13px horizontal. Features a large 1440px border-radius for a pill shape.

### Pill Link

**Role:** Interactive elements that appear as text but have a distinct pill-shaped hover state or background.

'Ghost White' text on 'Midnight Ink' background, often with an 8px border-radius. On hover, background fills with 'Midnight Ink' and border appears 'Ghost White'. Radius is 1440px.

### Work Card

**Role:** Container for showcasing featured work items.

Implied dark background with text ('Ghost White') overlaid. Features rounded corners at 8px radius and a soft 'Slate Whisper' border on hover/focus states, or as a subtle divider.

## Layout

The page structure is primarily max-width contained, centered on a deep black canvas. The hero section is full-bleed, often featuring a video or large image with centered, stark white text. Sections are clearly delineated with consistent vertical spacing (96px). Content arrangement frequently uses a two-column grid for images and text, often alternating sides. A card-grid layout is used for 'Featured Work' items. The overall density is comfortable, allowing elements to breathe within the dark space.

## Imagery

The site uses a mix of evocative, dark-toned product and lifestyle photography, often with a cinematic quality. Imagery is typically contained within semi-rounded or pill-shaped masks, emphasizing the object or subject directly. There's an absence of overly vibrant or desaturated images, maintaining a consistent subdued mood. Icons are minimal, likely outlined or simple solid shapes.

## Dos & Donts

### Do

- Use 'Midnight Ink' (#000000) as the default background, ensuring high contrast with 'Ghost White' text.
- Apply Founders Grotesk for all primary text, varying weight and size for hierarchy.
- Reserve Maligna for prominent headlines at 31px or 61px, maintaining its -0.0300em letter spacing.
- Utilize 'Violet Bloom' (#1200e3) sparingly for interactive feedback or subtle visual accents, never for large blocks of color.
- Maintain a comfortable density with an 8px base unit for most element gaps and 96px for section spacing.
- Apply a 1440px border-radius for all buttons and pill-shaped interactive elements to achieve a distinctive rounded look.
- Use 'Ghost White' (#ffffff) for borders on interactive elements against dark backgrounds.

### Don't

- Avoid using 'Violet Bloom' (#1200e3) for primary call-to-action button backgrounds or large text areas, as it isn't used for such a purpose.
- Do not introduce additional saturated colors; stick to 'Violet Bloom' as the sole chromatic accent.
- Avoid box-shadows or heavy elevation effects; rely on color contrast and subtle borders for visual separation.
- Do not deviate from the specified Malinga or Founders Grotesk font families for any text roles.
- Avoid using radii less than 8px, as the system favors either very subtle rounding or full pill shapes.
- Do not use generic system fonts; Founders Grotesk and Maligna are integral to the brand's typographic identity.
- Do not employ solid black borders; use either 'Slate Whisper' (#313130) for subtle definition or 'Ghost White' (#ffffff) for interactive elements.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000000
border: #313130
accent: #1200e3
primary action: no distinct CTA color

Example Component Prompts:
1. Create a header navigation link: 'Ghost White' text, 16px Founders Grotesk weight 500, letter-spacing -0.48px, with a 1440px border-radius, and a 'Midnight Ink' background. This typically appears as plain text until interaction.
2. Design a 'Ghost Button': Transparent background, 1px 'Ghost White' border, 1440px border-radius. Text is 'Ghost White', Founders Grotesk 16px weight 500, with 4.5px top, 7.2px bottom, and 12.6px horizontal padding.
3. Create a 'Work Card' title: 'Ghost White' text, 31px Maligna, letter-spacing -0.93px. This is usually presented over a dark image or solid 'Midnight Ink' background with an 8px corner radius for the overall card.

---
_Source: https://styles.refero.design/style/1775bf6a-afdd-48b1-8435-b92ec585c674_
