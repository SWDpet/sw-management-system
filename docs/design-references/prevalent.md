# Prevalent — Design Reference

> Indigo & Violet Precision

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://prevalent.ai](https://prevalent.ai) |
| Refero page | [https://styles.refero.design/style/b1e0a894-7440-44b8-9737-0ea4c988fc24](https://styles.refero.design/style/b1e0a894-7440-44b8-9737-0ea4c988fc24) |
| Theme | light |
| Industry | ai |

## Overview

Prevalent uses a confident, dark-to-light progression, blending a deep indigo hero with crisp white sections. Typography combines a classic serif for impactful headlines with a modern sans-serif for functional text. A single vivid violet acts as the primary accent, providing clear focal points against primarily monochrome UI elements, creating a sense of authority and precision without visual clutter.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Indigo | `#060b25` | brand | Hero and footer backgrounds, creating a deep, authoritative base for key sections |
| Action Violet | `#6360d8` | brand | Primary call-to-action buttons, active states, and decorative heading accents – the central interactive color |
| Canvas White | `#ffffff` | neutral | Main page and card backgrounds, primary surface for most content |
| Text Black | `#000000` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Shadow Gray | `#383c51` | neutral | Subtle borders and muted helper text, providing soft visual separation |
| Light Highlight | `#dad9ed` | neutral | Subtle background accents, like for highlighted text or small background areas on interactive elements |
| Muted Swiper Accent | `#007aff` | accent | Internal component accent (e.g., swiper pagination) — a secondary blue hue |

## Typography

### Tiempos Headline

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px, 30px, 45px, 48px, 50px, 80px |
| lineHeight | 0.98, 1.00, 1.10, 1.55 |
| letterSpacing | -0.02 |
| substitute | Playfair Display |
| role | Prominent headings and display text – the traditional serif conveys authority and importance. Weight 400 at larger sizes allows it to command attention without being aggressive. |

### Matter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px, 18px, 20px, 50px |
| lineHeight | 1.00, 1.22, 1.33, 1.40, 1.43, 1.55, 1.60 |
| letterSpacing | -0.01 |
| substitute | Inter |
| role | Body text, links, buttons, and functional UI elements – its clean sans-serif ensures clarity and legibility across various scales. The subtle negative letter-spacing creates a refined, compact feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.4 |  |
| body-sm | 14 |  | 1.4 |  |
| body | 16 |  | 1.4 |  |
| body-lg | 18 |  | 1.4 |  |
| heading-sm | 20 |  | 1.4 |  |
| heading | 50 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| hero | 60px |
| cards | 10px |
| links | 4px |
| footer | 20px |
| buttons | 4px |

- **elementGap** — 24px
- **sectionGap** — 48px
- **cardPadding** — 48px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Primary background for most page content. |
| Midnight Indigo | `#060b25` | 2 | Darker background for prominent sections like hero and footer, providing stark contrast. |
| Hero Transparent Overlay | `#ffffff1f` | 3 | Cards or containers within the hero, offering a subtle frosted appearance. |

## Components

### Primary Call to Action Button

**Role:** Key interactive elements for driving user action.

Filled with Action Violet (#6360d8), white text (#ffffff), 4px border-radius, and generous padding of 15px vertical, 48px horizontal. Text uses Matter, 500 weight.

### Header Navigation Link

**Role:** Top-level navigation items.

Ghost button style with transparent background, Text Black (#000000) or Canvas White (#ffffff) text (depending on header color), Matter font, 400 weight.

### Feature Card

**Role:** Content presentation for features or articles.

Canvas White (#ffffff) background, 10px border-radius, with no visible box shadow. No intrinsic padding, content manages its own spacing.

### Hero Section Card

**Role:** Transparent card elements within the hero section.

Transparent with a subtle white tint (rgba(255, 255, 255, 0.12)), 10px border-radius, no shadow.

### Inline Text Link

**Role:** Text-based navigation or references within body content.

Renders as Text Black (#000000) with no explicit decoration unless hovered/focused. When interactive, it may have a light background of Light Highlight (#dad9ed).

## Layout

The page transitions from a full-bleed dark hero with centered headlines and subtle illustrative overlays, to a max-width 1200px contained layout for subsequent sections. Content arranges primarily in centered stacks or two-column text-left/image-right (or vice-versa) alternating patterns. Sections are fluid, with consistent vertical spacing that suggests a 'spacious' feel, and no explicit visual dividers between white sections. The header is sticky, white, with a centered logo and nav items, and a prominent Action Violet 'Get a demo' button.

## Imagery

The site heavily uses custom, stylized vector illustrations, particularly in the hero section. These illustrations feature human figures in bubble-like environments, interacting with futuristic technology, against cityscapes, creating a narrative of security and control. They are brightly colored, with clear outlines and solid fills, focusing on conceptual metaphors rather than realism. Photography, when present in article previews, is realistic and varied, but serves a secondary role to the brand's primary illustrative style. Icons are outlined, minimal, and monochromatic, complementing the clean UI.

## Dos & Donts

### Do

- Use Tiempos Headline for all primary section titles and headings, leveraging its 400 weight for a refined impact.
- Apply Action Violet (#6360d8) exclusively for primary calls to action, interactive highlights, and brand accents; avoid using it for purely decorative, non-interactive elements.
- Set the main page background to Canvas White (#ffffff) for content sections, ensuring strong contrast with text.
- Maintain a clear visual hierarchy with Matter (400) for body text and Matter (500) for emphasized functional text like button labels.
- Implement a 4px border-radius for all primary interactive elements like buttons and input fields to ensure consistency.
- Utilize a 10px border-radius for cards and elevated containers to provide a soft, distinctive shape.
- Employ a base spacing unit of 4px, building up to 24px for element gaps and 48px for section and card padding.

### Don't

- Do not introduce new color hues; stick to the defined brand violet, neutrals, and the secondary swiper blue.
- Avoid using harsh shadows or heavy borders; prefer subtle separations and the transparent card style found in the hero.
- Do not deviate from the specified font families; avoid system fonts or other typefaces.
- Refrain from altering the letter-spacing values from the specified -0.02em and -0.01em for headings and body text respectively.
- Do not use generic gray values; adhere to Shadow Gray (#383c51) for muted text and borders.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #383c51
accent: #6360d8
primary action: #6360d8 (filled action)

Example Component Prompts:
1. Create a primary call to action button: filled Action Violet (#6360d8) background, Canvas White (#ffffff) text using Matter weight 500, 4px border-radius, 15px vertical padding, 48px horizontal padding.
2. Design a feature card: Canvas White (#ffffff) background, 10px border-radius, no shadow, with 48px internal padding. Title text is Matter 500 at 20px, body text is Matter 400 at 16px, both Text Black (#000000).
3. Generate a hero section 'Learn More' ghost button: transparent background, Canvas White (#ffffff) text using Matter 500, no border-radius. Include a subtle chevron icon next to the text. Use 12px vertical and 24px horizontal padding.

---
_Source: https://styles.refero.design/style/b1e0a894-7440-44b8-9737-0ea4c988fc24_
