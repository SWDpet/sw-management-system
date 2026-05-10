# Huly — Design Reference

> Midnight Command Center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://huly.io](https://huly.io) |
| Refero page | [https://styles.refero.design/style/d018e81d-6bb6-4445-86d7-39fd6be7e74d](https://styles.refero.design/style/d018e81d-6bb6-4445-86d7-39fd6be7e74d) |
| Theme | dark |
| Industry | saas |

## Overview

Huly uses a luminous Dark Mode aesthetic, pairing deep, almost black backgrounds with stark white text and electric accent colors. Product surfaces are treated as frosted glass overlays with subtle elevation. Typography is precise and utilitarian, with compact headlines creating a sense of density. Accents are used sparingly to highlight interactive elements and create a dynamic, glowing experience against the dark canvas.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Black | `#090a0c` | neutral | Primary background for the page canvas and prominent sections, creating a deep, immersive dark mode |
| Charcoal Grey | `#111111` | neutral | Background for elevated cards and section headers, providing a subtle lift from the canvas |
| Shadow Ink | `#303236` | neutral | General text, input borders, and dividers where contrast with lighter elements is needed |
| Ash Cloud | `#4a4b50` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Storm Grey | `#61656b` | neutral | Muted helper text and hairline borders, offering gentle visual separation |
| Battleship Grey | `#95979e` | neutral | Secondary body text and subtle icon fills for less prominent information |
| Silver Mist | `#a9a9aa` | neutral | Placeholder text and disabled states, a lighter shade for low-priority elements |
| Cloud Burst | `#d1d1d1` | neutral | Ghost button background or border for secondary actions, appearing as a soft highlight |
| Canvas White | `#ffffff` | neutral | Primary text, critical icons, and active states, standing out starkly against dark backgrounds |
| Electric Blue | `#5683da` | brand | Call-to-action button backgrounds and primary interactive elements, providing a strong visual magnet |
| Sunset Orange | `#ff8964` | accent | Decorative highlights and secondary accent where a warm glow is desired |
| Warm Ivory Gradient | `#f6f6f6` | accent | Subtle background gradient for certain sections or cards, contributing to an ethereal glow |
| Peach Bloom Gradient | `#ffebaa` | accent | Decorative background gradient used for soft, warm visual interest |

## Typography

### Esbuild

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 28px, 32px, 80px, 84px |
| lineHeight | 0.80, 0.90, 1.00 |
| letterSpacing | -0.0500em, -0.0400em, -0.0300em, -0.0200em |
| substitute | Montserrat |
| role | Display and main section headings. The tight line heights and significant letter-spacing control create a dense, impactful statement, giving these headlines a commanding but not overwhelming presence. It’s a custom-selected font that provides distinctive character. |

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| weights | 300, 400, 500, 600, 700 |
| sizes | 10px, 11px, 12px, 14px, 15px, 16px, 18px, 22px, 24px |
| lineHeight | 1.00, 1.13, 1.25, 1.38, 1.50 |
| letterSpacing | -0.0400em, -0.0200em, -0.0150em, -0.0100em |
| substitute | system-ui |
| role | All body text, subheadings, navigation, and UI elements. Inter is chosen for its clarity and legibility at various sizes, offering functional precision across the interface. Varied weights handle hierarchy in smaller text blocks. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | -0.4 |
| body-lg | 14 |  | 1.5 | -0.56 |
| heading-sm | 22 |  | 1.25 | -0.44 |
| heading | 28 |  | 1 | -1.4 |
| heading-lg | 32 |  | 0.9 | -1.6 |
| display | 80 |  | 0.8 | -4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 12px |
| lists | 30px |
| inputs | 4px |
| buttons | 9999px |

- **elementGap** — 12px
- **sectionGap** — 73px
- **cardPadding** — 12px
- **pageMaxWidth** — 1280px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Pitch Black Canvas | `#090a0c` | 0 | Base background for the entire application, creating a deep dark mode foundation. |
| Charcoal Grey Card | `#111111` | 1 | Primary surface for cards, content blocks, and subtle segmentation within the dark canvas. |
| Ash Cloud Overlay | `#4a4b50` | 2 | Used for interactive elements or modal backgrounds that appear on top of cards, indicating a higher interaction layer. |
| Silver Mist Panel | `#d1d1d1` | 3 | Ghost button backgrounds and elements that require a stark contrast or highlighted status within the dark UI. |

## Components

### Primary Action Button

**Role:** Main call-to-action

Filled with Electric Blue (#5683da), text in Canvas White (#ffffff), with a 4px border-radius. Padding is 10px right/left, 0px top/bottom. Represents the primary user action on the page.

### Ghost Primary Button

**Role:** Secondary call-to-action

Transparent background with Canvas White (#ffffff) text and a Canvas White (#ffffff) 0px border. Padding is 12px top/bottom and 12px right/left. Used for less prominent but still important actions.

### Pill Button

**Role:** Tertiary action or filter tag

Filled with Cloud Burst (#d1d1d1), text in Shadow Ink (#303236), with a 9999px border-radius (pill shape). Generous horizontal padding (64px left/right), 0px vertical. Used for broad, visually distinct actions.

### Navigation Link

**Role:** Top navigation item

Transparent background button with Shadow Ink (#303236) text and border, 0px radius. Minimal padding and no distinctive background, for navigation. On hover, text becomes Canvas White (#ffffff).

### Standard Card

**Role:** Content container

Charcoal Grey (#111111) background with a 12px border-radius and no visible box shadow. Provides contained sections for content organization.

### Elevated Tooltip Card

**Role:** Interactive detail container

Appears to be a Charcoal Grey (#111111) background with a 12px border-radius, accompanied by a soft, diffused shadow `rgba(0, 0, 0, 0.35) 0px 4px 16px 0px` to suggest interactivity or temporary focus.

### Accent Bordered List Item

**Role:** Highlighted list item

Charcoal Grey (#111111) background with a 30px border-radius. Features a glowing border effect using `rgba(255, 255, 255, 0.4) 0px 0px 0px 6px` to draw attention.

## Layout

The page primarily uses a max-width contained layout of 1280px, with content centered. The hero section is full-bleed, a deep dark background with a striking central glowing abstract graphic. Subsequent sections alternate between dark backgrounds with product UI examples and lighter, almost white, background sections for informational content. Content frequently uses a two-column text-left/image-right or centered stacked sections. Spacing between sections is generous (73px). Navigation is a sticky top bar with minimal styling and calls to action.

## Imagery

This site features product screenshots and abstract glowing visual effects. Product screenshots are contained within dark UI elements, often with soft internal shadows, presented in a minimal, focused manner. The abstract visuals, characterized by luminous beams and soft gradients, are used decoratively, often as full-bleed background elements behind hero sections or to add ethereal depth to UI blocks, creating a futuristic and dynamic atmosphere. Icons are primarily outlined or mono-colored, designed for clarity and integration into the dark UI.

## Dos & Donts

### Do

- Use Pitch Black (#090a0c) for primary page backgrounds and Canvas White (#ffffff) for primary text to maintain high contrast.
- Apply Electric Blue (#5683da) exclusively for primary call-to-action buttons, ensuring high visibility and consistency for interactive elements.
- Utilize Esbuild for all main headings (80px, 84px) with tight line heights (0.8, 0.9) and generous negative letter-spacing (-0.05em to -0.02em) for compact, impactful typography.
- Set all interactive buttons and tags to a 9999px border-radius for a distinct pill-like shape.
- Employ Charcoal Grey (#111111) for all card backgrounds, providing a subtle elevation from the main page body.
- Maintain an element gap of 12px for consistent vertical and horizontal spacing between discrete UI elements.
- Implement softer shadows like `rgba(0, 0, 0, 0.15) 0px 4px 6px 0px` for general elevated elements, and `rgba(0, 0, 0, 0.35) 0px 4px 16px 0px` for more pronounced interactions like tooltips or focus states.

### Don't

- Do not use saturated colors for large background areas or extensive text blocks; restrict them to accents and interactive elements.
- Avoid using default system shadows; use the defined custom shadow tokens for all elevation effects.
- Do not vary border-radius for buttons and tags outside of 9999px; this is a signature aesthetic.
- Never use line heights greater than 1.5, especially for headlines, as the typography is designed for compaction.
- Do not introduce new color gradients; adhere strictly to the defined brand and accent gradients.
- Avoid breaking the dark mode aesthetic by introducing large sections of light backgrounds unless explicitly for contrasting content. When using lighter backgrounds for sections (e.g. #f6f6f6), ensure they are still within the neutral palette.
- Do not use letter spacing >= 0; the system prefers negative tracking to keep text compact and intentional.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #090a0c
border: #303236
accent: #ff8964
primary action: #5683da (filled action)

Example Component Prompts:
1. Create a primary action button: Electric Blue (#5683da) background, Canvas White (#ffffff) text, 4px border-radius, 0px vertical padding, 10px horizontal padding.
2. Create a standard card: Charcoal Grey (#111111) background, 12px border-radius, no shadow, with Shadow Ink (#303236) as an internal divider.
3. Create a navigation link: Transparent background, Shadow Ink (#303236) text (Inter, weight 500, size 16px), 0px radius, 12px vertical/horizontal padding. On hover, change text color to Canvas White (#ffffff).
4. Create a hero headline: Text 'Everything App for your teams' (Esbuild, weight 600, size 80px), Canvas White (#ffffff) color, line height 0.8, letter-spacing -4px.
5. Create a secondary text block: Text 'Huly, an open-source platform...' (Inter, weight 400, size 18px), Battleship Grey (#95979e) color, line height 1.5, letter-spacing -0.15em.

---
_Source: https://styles.refero.design/style/d018e81d-6bb6-4445-86d7-39fd6be7e74d_
