# Surface — Design Reference

> Minimalist gallery canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://surface.arcticvolume.com](https://surface.arcticvolume.com) |
| Refero page | [https://styles.refero.design/style/e6c20874-c6f9-4c31-b6b2-2cb27cbf15f2](https://styles.refero.design/style/e6c20874-c6f9-4c31-b6b2-2cb27cbf15f2) |
| Theme | light |
| Industry | media |

## Overview

Arctic Volume presents a bold, editorial aesthetic. It emphasizes high contrast typography on vast, unobtrusive white space. Visuals are treated as distinct collage elements, drawing attention without being enclosed. The overall impression is one of stark graphic clarity, where content is king and design serves to amplify rather than decorate.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Arctic Snow | `#f7f7f7` | neutral | Primary page canvas and white card surfaces. Do not promote it to the primary CTA color |
| Midnight Ink | `#000000` | neutral | Primary text, headings, outlines for buttons, active navigation items, filled button backgrounds, decorative borders |

## Typography

### Munken Sans Web

| Key | Value |
| --- | --- |
| weight | 400, 700, 900 |
| weights | 400, 700, 900 |
| sizes | 16px, 20px, 30px, 40px, 230px |
| lineHeight | 1.00, 1.20, 1.35, 1.50 |
| substitute | Inter |
| role | All text elements, from body copy and links to navigation and massive display headings. Its clean, sans-serif geometry with distinct proportions establishes the site's editorial and art-forward feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.35 |  |
| heading-sm | 30 |  | 1.2 |  |
| heading | 40 |  | 1.2 |  |
| display | 230 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| none | 0px |

- **elementGap** — 10px
- **sectionGap** — 48px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Ghost Button

**Role:** Secondary actions and navigation links

Text in Midnight Ink (#000000) on an Arctic Snow (#f7f7f7) background, with a 1px Midnight Ink (#000000) border. Padding is 6px around the text. Radius is 0px.

### Filled Primary Button

**Role:** Primary calls to action

Text in Arctic Snow (#f7f7f7) on a Midnight Ink (#000000) background, with a 1px Midnight Ink (#000000) border. Padding is 14px vertically and 38px horizontally. Radius is 0px.

### Content Card (Image/Text)

**Role:** Displaying images and associated text in a grid or collage layout

No background color, no borders, no box shadow, and no padding. Content is presented directly. Radius is 0px. Text inside cards uses Midnight Ink (#000000).

## Layout

The page uses a full-bleed layout without a fixed max-width, allowing content to span the browser's width, though elements frequently align to loose vertical columns or grids. The hero section features unusually large, overlapping text with an irregular arrangement of small image cards, setting a dynamic, non-uniform tone. Section rhythm is primarily driven by generous vertical spacing between content blocks. Content arrangement is fluid; sometimes text-left/image-right, but more often a collage of visual elements surrounding central textual content. The navigation is minimal, a simple top-bar with a hamburger menu.

## Imagery

This site features a heavy reliance on photography and occasional abstract graphics, treated with a collage-like sensibility. Images are typically contained within their own bounding boxes, but arranged in dynamic, overlapping, and seemingly unconstrained layouts, creating a 'mood board' effect. Photography styles vary, from product-focused to artistic lifestyle shots. The imagery serves primarily as decorative atmosphere and content showcase, often appearing in high density relative to text, contributing to the visual-first editorial feel.

## Dos & Donts

### Do

- Always use Munken Sans Web for all text elements.
- Implement Midnight Ink (#000000) for all primary text and active states.
- Utilize Arctic Snow (#f7f7f7) as the dominant background color for clarity.
- Adhere to a 0px border-radius for all elements (buttons, cards) to maintain a sharp, editorial edge.
- Maintain high contrast between text and background, typically Midnight Ink on Arctic Snow.
- Employ consistent 48px vertical spacing between major page sections and headings.
- Design with a spacious layout, allowing ample white space around elements.

### Don't

- Avoid rounded corners; all interface elements should have a 0px radius.
- Do not introduce additional saturated colors; the palette is strictly achromatic for UI elements.
- Refrain from using drop shadows or complex elevation; surfaces are flat and direct.
- Do not add padding or borders to primary content cards; they should float freely or be placed directly.
- Avoid decorative gradients or background images for UI components; stick to solid colors.
- Do not use letter-spacing other than normal, except where explicitly specified by type scale.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
text: #000000
background: #f7f7f7
border: #000000
accent: no distinct accent color
primary action: no distinct CTA color

**3-5 Example Component Prompts**
Create a ghost button: text 'Learn More' in Midnight Ink (#000000), background Arctic Snow (#f7f7f7), 1px border in Midnight Ink (#000000), 6px padding on all sides, 0px border-radius.
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
Create a heading: 'Surface N° 3' using Munken Sans Web weight 900 at 230px size, line-height 1.00, color Midnight Ink (#000000).

---
_Source: https://styles.refero.design/style/e6c20874-c6f9-4c31-b6b2-2cb27cbf15f2_
