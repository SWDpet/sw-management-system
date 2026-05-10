# Bang & Olufsen — Design Reference

> Gallery of precise objects. A dark, velvet-lined showcase where each product rests, spotlighted with refined exactitude.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://bang-olufsen.com](https://bang-olufsen.com) |
| Refero page | [https://styles.refero.design/style/27a4a4fa-4b1a-4e7e-b2c3-3e5bf57f00e5](https://styles.refero.design/style/27a4a4fa-4b1a-4e7e-b2c3-3e5bf57f00e5) |
| Theme | mixed |
| Industry | ecommerce |

## Overview

This design system balances classic luxury with contemporary minimalism. Rich, deep indigo (#060daa) and elegant black (#191817) create a sophisticated backdrop, punctuated by a delicate, almost cream-colored off-white (#fcfaee) for textual contrast. The signature element is the custom BeoSupreme typeface, used across all text sizes, which brings a unique, refined character, with precise letter-spacing adjustments at every size. The dominant visual language is clean, centered product photography on stark backgrounds, framed by generous negative space and a strict typographic hierarchy.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Indigo | `#060daa` | brand | Footer background, primary accent for deep sections – creating a luxurious, immersive foundation. |
| Carbon Black | `#191817` | neutral | Dominant text color for headings and body content on light backgrounds, input borders – provides stark contrast and grounded presence. |
| Barely White | `#fcfaee` | neutral | Primary text color on dark backgrounds, selected button text – a creamy off-white that softens the high contrast. |
| Ash Gray | `#555555` | neutral | Secondary text, subtle link color – offers a muted informational tone against white. |
| Pure White | `#ffffff` | neutral | Page backgrounds, card backgrounds, input backgrounds – provides clean, expansive canvas. |
| Pale Silver | `#e5e5e5` | neutral | Subtle border colors for inputs – an almost imperceptible divider. |
| Pure Black | `#000000` | neutral | Primary icon color, borders on ghost buttons – a hard, crisp edge or fill. |

## Typography

### BeoSupreme

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 12px, 14px, 16px, 24px, 36px |
| lineHeight | 1.00, 1.15, 1.25, 1.33, 1.43, 1.50, 1.63, 1.67, 1.71, 2.19 |
| letterSpacing | -0.056em at 36px, -0.014em at 24px, 0.006em at 16px, 0.007em at 14px, 0.008em at 12px, then other specific values for a finely tuned optical balance |
| substitute | Open Sans |
| role | Primary typeface for all headings, body text, and UI elements. Its broad range of weights and precise letter-spacing across sizes is a core visual identity feature, conveying understated luxury. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.43 | 0.007 |
| heading | 24 |  | 1.25 | -0.014 |
| display | 36 |  | 1.15 | -0.056 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| badges | 2px |
| buttons | 40px |

- **elementGap** — 4px
- **sectionGap** — 48px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Product Cards — Explore Superventas

### Hero CTA — Beo Grace

### Button Group — B&O Style System

### Primary Button (Honey Tone CTA)

**Role:** Call to action

Rounded pill button with 'Carbon Black' (#191817) background and 'Barely White' (#fcfaee) text. Has a 40px border-radius, 8px vertical padding, and 32px horizontal padding. Uses BeoSupreme text.

### Ghost Button (Menu/Search)

**Role:** Navigation/Utility

Transparent background with 'Carbon Black' (#000000) text and border. No border-radius, 0px padding. Used for minimal UI controls.

### Text Link Button

**Role:** Tertiary action/Navigation

Transparent background with 'Barely White' (#fcfaee) text and a 'Barely White' (#fcfaee) bottom border of 1px. 4px vertical padding, 0px horizontal padding. Typically used in dark sections like the hero or footer.

### Feature Card

**Role:** Product display

A completely transparent card with no padding, border, or shadow. It acts as a container for product images and descriptive text. Text is 'Carbon Black' (#191817) and headings use BeoSupreme.

### Input Field

**Role:** User entry

White background (#ffffff) with 'Carbon Black' (#191817) text and a 1px 'Carbon Black' (#191817) bottom border. No border-radius. 1px vertical padding and 2px right padding.

### New Product Badge

**Role:** Highlight new items

Rectangular badge with 'Pure White' (#ffffff) background and 'Carbon Black' (#191817) text. Has a 2px border-radius, 4px vertical padding, and 8px horizontal padding. Uses BeoSupreme text.

## Layout

The page uses a mixed layout approach, blending full-bleed sections with constrained content. The hero prominently features a full-bleed dark background ('Midnight Indigo') with a large, centered product image and left-aligned headline/CTA. Subsequent sections alternate between full-bleed white backgrounds for product listings (often displaying items in a clean, centered grid of 4) and some potentially full-bleed sections with strong, singular background colors like the red observed. Content is generally centered within a comfortable maximum width when not full-bleed. Vertical rhythm is maintained by consistent spacing between sections (around 48px), creating a spacious and unhurried browsing experience. The navigation is a minimalist sticky top bar, providing persistent access without visual clutter.

## Imagery

The visual language focuses on meticulously staged product photography. Products are often isolated or tightly cropped, centered on pure white backgrounds or against deep, velvety textiles like the 'Midnight Indigo' in the hero. The treatment is clean and raw-edged, with no masking or overlapping effects. Photography is clearly high-key for white backgrounds and moody/dark for dramatic impact on colored backgrounds, emphasizing the texture and material of the products. Images are explanatory and showcase the product as the hero, occupying significant visual space in sections to convey luxury and technical precision. Icons are minimal, monochromatic, and outlined, primarily in 'Pure Black' or 'Barely White', complementing the UI's precision.

## Dos & Donts

### Do

- Prioritize the custom 'BeoSupreme' font for all textual content, leveraging its unique character and precise letter-spacing.
- Use 'Midnight Indigo' (#060daa) exclusively for foundational elements like the footer to establish a luxurious, deep anchor.
- Maintain a clear visual hierarchy by contrasting 'Carbon Black' (#191817) text on light backgrounds (#ffffff, #fcfaee) and 'Barely White' (#fcfaee) on dark backgrounds (#060daa).
- Employ the 40px border-radius strictly for primary CTA buttons, ensuring they stand out as the sole 'soft' element.
- Utilize generous negative space around product imagery and text blocks to convey a sense of premium quality and focus, with section gaps around 48px.
- Ensure all interactive elements, especially primary CTAs, meet a minimum contrast ratio of 4.5:1 against their background.
- Use a subtle 1px border for ghost button states and text links to provide definition without visual weight.

### Don't

- Do not introduce additional font families; 'BeoSupreme' defines the typographic identity.
- Avoid using multiple accent colors; 'Midnight Indigo' is reserved for specific, prominent sectional backgrounds.
- Do not deviate from the established border-radius values (0px, 2px, 40px); rounded corners are intentional and scarce.
- Do not use box-shadows; elevation is handled through background color changes and spatial separation.
- Avoid decorative elements or busy backgrounds; the aesthetic emphasizes product clarity and clean UI.
- Do not create dense content blocks; the comfortable density principle with a 4px base unit should be consistently applied.
- Never use the browser default blue for links; control all link colors with 'Carbon Black', 'Ash Gray', or 'Barely White'.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text on light: #191817
- Text on dark: #fcfaee
- Page Background: #ffffff
- CTA Background: #191817
- Footer Background: #060daa

### Example Component Prompts
1. Create a hero section: 'Midnight Indigo' (#060daa) full-bleed background. Centered product image. Above it, a 'display' headline: 'Beo Grace' (BeoSupreme, 36px, 1.15lh, -0.056em ls, #fcfaee). Below, a body text: '100 años de artesanía. Un futuro icono.' (BeoSupreme, 16px, 1.33lh, 0.006em ls, #fcfaee). Underneath, a Primary Button: 'Disponible en Honey Tone' (40px radius, 8px 32px padding, #191817 background, #fcfaee text).
2. Generate a product listing grid: 'Pure White' (#ffffff) background. 'subheading' title 'Explore nuestros superventas' (BeoSupreme, 24px, 1.25lh, -0.014em ls, #191817). Display three Feature Cards horizontally, each with a centered product image, 'subheading' product name (BeoSupreme, 16px, 1.33lh, #191817) and price below it, separated by around 24px element gap.
3. Design a form input: 'Pure White' (#ffffff) background. 'Carbon Black' (#191817) text using 'body' style (BeoSupreme, 14px, 1.43lh), with a 1px 'Carbon Black' (#191817) bottom border. Placeholder text color: #555555. Padding: 1px vertical, 2px right.

---
_Source: https://styles.refero.design/style/27a4a4fa-4b1a-4e7e-b2c3-3e5bf57f00e5_
