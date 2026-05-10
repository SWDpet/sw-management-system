# Geniestudio — Design Reference

> White canvas, friendly illustrations.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://geniestudio.app](https://geniestudio.app) |
| Refero page | [https://styles.refero.design/style/2ffd50d4-93b7-4acf-9bc2-e86e61b63f27](https://styles.refero.design/style/2ffd50d4-93b7-4acf-9bc2-e86e61b63f27) |
| Theme | light |
| Industry | ai |

## Overview

Geniestudio presents a friendly, open design language against a bright, airy backdrop. Softly rounded elements and a distinctive dark button color punctuate a largely monochromatic interface. The design uses gradients sparingly for visual flair, primarily on illustration assets, maintaining a neat and approachable aesthetic. Typography feels modern and clean, with nuanced letter spacing creating a premium yet comfortable reading experience. Overall, the system feels inviting and lightweight, focusing on clarity and ease of use.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#181d27` | brand | Primary action buttons, prominent interactive elements — a deep, near-black that grounds interactive components |
| Arctic Mist | `#fafdff` | neutral | Card backgrounds, secondary container surfaces — a subtle off-white providing depth on the canvas |
| Canvas White | `#ffffff` | neutral | Main page background, default text color for dark buttons, subtle borders |
| Obsidian | `#0a0d12` | neutral | Primary headings and prominent text — a rich, dark tone for high contrast |
| Silver Pine | `#535862` | neutral | Secondary text, muted links, detailed descriptions and iconography |
| Ash Gray | `#93979f` | neutral | Muted text, form placeholders, minor divider lines, inactive states |
| Sky Wash | `#ebf5ff` | neutral | Base canvas background, light decorative fills — provides a soft, cool tint to the overall page |
| Ghostly Blue | `#cce7ff` | accent | Light background fills for secondary cards and decorative elements |
| Electric Blue | `#0069e0` | accent | Accent borders, infographic elements, decorative strokes — a vibrant hue often used in illustrations |
| Lavender Mist | `#f1e6ff` | accent | Background for accent cards, soft decorative fills |
| Mint Glaze | `#d3f6e3` | accent | Background for accent cards, soft decorative fills |
| Sunburst Yellow | `#bb9915` | accent | Decorative highlights, infographic elements, specific accent text/icons |
| Deep Violet | `#9552e0` | accent | Decorative highlights, infographic elements, specific accent text/icons |
| Ocean Spray | `#4fbeff` | accent | Decorative highlights, infographic elements, specific accent text/icons |
| Zesty Orange | `#f26110` | accent | Decorative highlights, infographic elements, specific accent text/icons |
| Luminous Blue | `#0099ff` | accent | Blue outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Whisper Fade Yellow | `#fff9e0` | accent | Soft gradient background for decorative features |
| Whisper Fade Violet | `#f4ebff` | accent | Soft gradient background for decorative features |
| Whisper Fade Blue | `#e5f6ff` | accent | Soft gradient background for decorative features |
| Whisper Fade Orange | `#fff2eb` | accent | Soft gradient background for decorative features |

## Typography

### Geist

| Key | Value |
| --- | --- |
| weight | 500, 600 |
| sizes | 10px, 12px, 14px, 16px, 18px, 20px |
| lineHeight | 1.14, 1.33, 1.35, 1.40, 1.50 |
| letterSpacing | -0.01 |
| fontFeatureSettings | 'case' |
| substitute | Inter |
| role | Body text, navigation links, button text, and all supportive details. The consistent, slightly tight letter spacing at -0.01em gives text a concise appearance, maintaining readability while feeling modern. |

### Aeonik

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 20px, 24px, 32px, 48px, 72px, 148px |
| lineHeight | 1.05, 1.11, 1.17, 1.20, 1.25 |
| letterSpacing | -0.02 |
| fontFeatureSettings | 'case' |
| substitute | Montserrat |
| role | Headlines and prominent display text. The consistent tighter letter spacing at -0.02em creates compact, impactful headers, drawing attention without visual clutter. Its lower x-height compared to Geist differentiates its use for display content. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-lg | 14 |  | 1.33 | -0.14 |
| heading-sm | 18 |  | 1.5 | -0.18 |
| heading | 20 |  | 1.25 | -0.4 |
| heading-lg | 24 |  | 1.2 | -0.48 |
| display | 32 |  | 1.17 | -0.64 |
| display-lg | 48 |  | 1.11 | -0.96 |
| display-xl | 72 |  | 1.05 | -1.44 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 32px |
| icons | 16px |
| badges | 90px |
| images | 16px |
| buttons | 32px |

- **elementGap** — 24px
- **sectionGap** — 24px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Sky Wash Canvas | `#ebf5ff` | 0 | The foundational background color for the entire page, providing an ethereal, cool tint. |
| Canvas White Base | `#ffffff` | 1 | The primary background for most sections and content areas, offering a clean, bright foundation. |
| Arctic Mist Card | `#fafdff` | 2 | Elevated card backgrounds, creating subtle visual separation and depth. |

## Components

### Primary Action Button

**Role:** Call to action

Filled button with a dark background '#181d27', white text '#ffffff', 32px border-radius, and generous padding (12px vertical, 32px horizontal). Acts as the main interactive element.

### Small Primary Action Button

**Role:** Secondary call to action

Compact filled button with a dark background '#181d27', white text '#ffffff', 16px border-radius, and reduced padding (8px vertical, 16px horizontal).

### Header Action Button

**Role:** Navigation action

Medium-sized filled button for header actions with a dark background '#181d27', white text '#ffffff', 36px border-radius, and moderate padding (8px vertical, 24px horizontal).

### Feature Card

**Role:** Informational display

Card with 'Arctic Mist' background '#fafdff', 32px border-radius, and 40px internal padding. Used for features and testimonials.

### Decorative Icon Card (Round)

**Role:** Visual accent container

Transparent card with a 50% border-radius (circular). Holds decorative icons and illustrations without a background fill.

### Standard Content Card

**Role:** General content container

Transparent card with 8px border-radius for grouping textual content and images.

## Layout

The page structure favors a max-width contained layout, likely around 1200-1400px, against a full-width background. The hero section is characterized by a centered headline and button over a light, expansive background with decorative illustrations. Sections maintain consistent vertical spacing, often indicated by a 24px element gap, with subtle background color changes ('Canvas White' alternating with 'Sky Wash' or 'Arctic Mist') for visual rhythm. Content is arranged in flexible patterns, including centered stacks for calls to action, alternating text-left/visual-right compositions, and responsive card grids for features and testimonials. Navigation is a minimal top bar with a primary action button on the far right.

## Imagery

The site uses a combination of abstract, whimsical illustrations with organic shapes and cloud motifs, and contained product-focused imagery. Illustrations are flat, graphic, and often outlined, using a palette of soft pastel accents and instances of vibrant brand colors. They serve a decorative and atmospheric role, adding a friendly, approachable mood. Product screenshots, when present, appear to be tightly cropped within rounded containers. Icons are primarily outlined or have a light stroke, maintaining the lightness of the interface. Imagery density is moderate, allowing ample white space, ensuring a text-dominant but visually engaging experience.

## Dos & Donts

### Do

- Prioritize 'Obsidian' (#0a0d12) for all main headings and primary text, ensuring a high contrast ratio of at least 18:1 against light backgrounds.
- Utilize 'Midnight Ink' (#181d27) exclusively for primary button backgrounds; never apply it to text directly.
- Apply 'Arctic Mist' (#fafdff) as the background for most cards and secondary content blocks, providing subtle visual separation from the main page canvas.
- Use a border-radius of 32px for all primary action buttons, feature cards, and larger decorative elements to maintain a consistent soft aesthetic.
- Implement the 'Aeonik' font for all headings from size 20px upwards, ensuring consistent letter-spacing of -0.02em.
- Maintain 24px as the standard vertical and horizontal gap between major layout elements and within card grids.
- Employ Geist font (weights 500-600) for all body text, links, and minor interface labels, with a consistent letter-spacing of -0.01em.

### Don't

- Avoid using highly saturated colors for large background areas or extensive text blocks; keep these for small accents or illustrations.
- Do not deviate from the specified Geist and Aeonik font stacks and their letter-spacing; custom type is a core identifier.
- Never use #0000ee (browser default link blue); instead, use 'Luminous Blue' (#0099ff) for interactive link text.
- Do not introduce sharp corners; all interactive elements and content containers should adhere to the established border-radius values (e.g., 32px, 16px, 8px).
- Avoid heavy drop shadows or multiple layered shadows on UI elements; elevation is subtle, primarily using rgba(4, 69, 144, 0.08) 0px 14px 20px 4px for depth.
- Do not use dark backgrounds for content sections; the system is built around a light theme, with dark used only for specific UI like buttons.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #0a0d12
background: #ebf5ff
border: #535862
accent: #0069e0
primary action: #181d27 (filled action)

Example Component Prompts:
Create a Primary Action Button: #181d27 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Create a 'Feature Card': 'Arctic Mist' background, 32px border-radius, with 40px padding. Inside, 'Aeonik' weight 500, 24px, '#0a0d12' for a title and 'Geist' weight 500, 16px, '#535862' for the description, with an 'Electric Blue' (#0069e0) decorative icon above.


Create a testimonial card: 'Arctic Mist' background, 32px border-radius, with 40px internal padding. Caption text 'Geist' weight 500, 16px, '#0a0d12'. Below, muted attribution text 'Geist' weight 500, 14px, '#93979f'.

---
_Source: https://styles.refero.design/style/2ffd50d4-93b7-4acf-9bc2-e86e61b63f27_
