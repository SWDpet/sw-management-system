# Dyson — Design Reference

> engineered precision, clean displays

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://dyson.com](https://dyson.com) |
| Refero page | [https://styles.refero.design/style/96845df2-7ddb-420a-814e-c339f95a6554](https://styles.refero.design/style/96845df2-7ddb-420a-814e-c339f95a6554) |
| Theme | light |
| Industry | other |

## Overview

This Dyson design system exudes an aura of understated, high-tech precision, much like the engineering of its products. It achieves this through a largely monochromatic palette of distinct dark grays and crisp whites, punctuated by a vibrant green for primary calls to action. The strong focus on clear product imagery is supported by a clean, geometric typeface, ensuring a direct and unembellished presentation of technology.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Dyson Ink | `#333333` | neutral | Primary text, headers, prominent UI elements. |
| Ghost Gray | `#999999` | neutral | Secondary text, subtle borders, inactive states. |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card backgrounds, contrast for dark text. |
| Deep Graphite | `#000000` | neutral | Strong section backgrounds, darkest text for maximum contrast. |
| Aluminum | `#919191` | neutral | Dividers, subtle outlines. |
| Medium Gray | `#555555` | neutral | Tertiary text, less prominent details. |
| Light Silver | `#ebebeb` | neutral | Hover states, subtle background accents. |
| Warm Paper | `#fff8e6` | neutral | Accent backgrounds, often paired with product imagery, creating a softer backdrop. |
| Steel Gray | `#dadada` | neutral | Subtle borders, input field outlines. |
| Success Green | `#79b928` | brand | Primary call-to-action buttons – signifies positive action and completion. |
| Dyson Blue | `#0066cc` | brand | Informational links, secondary interactive elements, communicates product benefits. |
| Azure Link | `#149ecc` | brand | Secondary brand links, indicating a distinct interactive path. |
| Product Highlight Orange | `#ac5d00` | accent | Badge labels for 'NEW' or special offers, drawing attention to key features. |

## Typography

### DysonFutura

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| weights | 300, 400, 500 |
| sizes | 12px, 14px, 16px, 18px, 24px, 28px, 32px, 36px |
| lineHeight | 1.20-2.00 |
| letterSpacing | normal |
| substitute | Futura Std |
| role | The primary typeface for all content, from headings to body text, maintaining a consistent, technical, and clean aesthetic without being overly sterile. Dyson's custom font reinforces brand identity. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0 |
| body-sm | 14 |  | 1.43 | 0 |
| body | 16 |  | 1.25 | 0 |
| subheading | 18 |  | 1.33 | 0 |
| heading-sm | 24 |  | 1.2 | 0 |
| heading | 28 |  | 1.22 | 0 |
| heading-lg | 32 |  | 1.29 | 0 |
| display | 36 |  | 1.2 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 0px |
| badges | 2px |
| images | 8px |
| buttons | 0px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Product Card — Destacados

### Promotion Banner

### Button Group — CTA Variants

### Primary CTA Button

**Role:** Main call to action

Solid Success Green (#79b928) background with Deep Graphite (#000000) text. No border-radius. Padding of 20px vertical and 24px horizontal. Example: 'Comprar ahora'.

### Text Only Button

**Role:** Secondary action in dark contexts

Transparent background with Canvas White (#ffffff) text. No border-radius. Used for navigation elements or subtle actions where the background provides contrast.

### Light Text Only Button

**Role:** Secondary action in light contexts

Transparent background with Deep Graphite (#000000) text. No border-radius. Used for navigation or very subtle actions on light backgrounds.

### Product Card

**Role:** Displays product information

Canvas White (#ffffff) background, no border-radius or shadow. Product name and price in Dyson Ink (#333333). Contains a 'NUEVO' badge and a Primary CTA Button. Inner padding is 16px.

### Main Navigation Link

**Role:** Primary site navigation

Dyson Ink (#333333) text, no background. Displays DysonFutura at 16px, weight 400. Found in top navigation bars like 'Tiendas', 'Empresas'.

### Search Input Field

**Role:** Site search functionality

Transparent background with Dyson Ink (#333333) placeholder text. Border color Dyson Ink (#333333) on focus. No border-radius. Left padding of 10px.

### New Badge

**Role:** Highlights new products

Solid Deep Graphite (#333333) background with Canvas White (#ffffff) text. Border-radius 2px 2px 0px 0px. Padding 0px vertical, 12px horizontal. Text is DysonFutura 12px weight 500.

### Promotion Badge

**Role:** Highlights special promotions

Transparent background with Product Highlight Orange (#ac5d00) text. No border-radius. Padding 0px vertical, 15px horizontal. Used for promotional text like 'Promesa de precio'.

### Footer Link

**Role:** Secondary navigational links in footer

Ghost Gray (#999999) text on a Deep Graphite (#000000) background. DysonFutura 14px, weight 400.

## Layout

The layout follows a primarily max-width contained model, but with a full-bleed blue banner appearing at the very top. The hero section often presents a split layout: a compelling headline on the left alongside a product grid with individual product images on the right, frequently featuring abstract or subtly tinted backgrounds. Sections are clearly delineated by consistent vertical spacing of 48px and alternating background colors (Canvas White vs. Deep Graphite, or Canvas White vs. Warm Paper). Product grids typically use a 3-column layout. The overall arrangement emphasizes clarity and directness, with content stacked centrally or in alternating text-image blocks. The navigation is a sticky top bar, providing persistent access to search and main categories.

## Imagery

The site uses a mix of high-key product photography and lifestyle imagery, with a strong emphasis on clean, isolated product shots on either pure white or subtly colored (Warm Paper #fff8e6) backgrounds. Photography is frequently cropped tightly to showcase product details. Lifestyle shots tend to feature models with a natural, unposed aesthetic. Imagery is always contained, often within rectangular frames with 8px radius, contributing to a structured and precise feel. Icons are monochrome, often Deep Graphite (#000000) or Canvas White (#ffffff), and are outlined, with a consistent stroke weight, serving primarily as functional UI elements rather than decorative ones.

## Dos & Donts

### Do

- Use Success Green (#79b928) exclusively for primary call-to-action buttons.
- Prioritize Dyson Ink (#333333) for all primary body text and most headings to maintain brand consistency.
- Ensure headings utilize DysonFutura at a heavier weight (500) and larger sizes (24px, 28px, 32px, 36px) to create a clear visual hierarchy.
- Apply 0px border-radius to all buttons and cards, maintaining a sharp, engineered aesthetic.
- Embed images within Canvas White (#ffffff) or Warm Paper (#fff8e6) backgrounds when presented in product grids.
- Maintain a clear page section separation using Canvas White (#ffffff) and Deep Graphite (#000000) for distinct visual blocks.

### Don't

- Do not use saturated colors other than Success Green (#79b928) or Dyson Blue (#0066cc) for interactive elements.
- Avoid using shadows or excessive gradients; rely on color contrast and spacing for visual depth.
- Do not introduce rounded corners larger than 8px, which are reserved for specific product images, to avoid softening the brand's sharp identity.
- Refrain from using thin weights for body text; maintain DysonFutura weight 400 for optimal readability.
- Do not use light text on light backgrounds; ensure a minimum contrast ratio of 7:1 for text readability (e.g., Dyson Ink on Canvas White).

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): #333333 (Dyson Ink)
- Background (Page): #ffffff (Canvas White)
- CTA (Primary): #79b928 (Success Green)
- Border (Subtle): #919191 (Aluminum)
- Accent (Links): #0066cc (Dyson Blue)

### 3-5 Example Component Prompts
1. Create a `Primary Call to Action Button`: background #79b928, text #000000, font DysonFutura weight 500 size 16px, padding 20px 24px, border-radius 0px. Text: 'Comprar ahora'.
2. Design a `Product Card`: background #ffffff, border-radius 0px, padding 16px. Place an image with 8px border-radius at the top. Below the image, include a 'NUEVO' badge: background #333333, text #ffffff, border-radius 2px 2px 0px 0px, padding 0px 12px. Add a product title in DysonFutura weight 400 size 18px #333333, and a price in DysonFutura weight 500 size 24px #333333. Include a 'Discover More' button (using `Primary Call to Action Button` specifications) at the bottom.
3. Build a `Header Navigation Link`: font DysonFutura weight 400 size 16px, color #333333, no background, no border-radius. Text: 'Tiendas'.
4. Create a `New Product Badge`: background #333333, text #ffffff, font DysonFutura weight 500 size 12px, border-radius 2px 2px 0px 0px, padding 0px 12px. Text: 'NUEVO'.

---
_Source: https://styles.refero.design/style/96845df2-7ddb-420a-814e-c339f95a6554_
