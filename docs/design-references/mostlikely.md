# mostlikely — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://mostlikely.at](https://mostlikely.at) |
| Refero page | [https://styles.refero.design/style/4040f97b-42cf-49ef-ab2d-a77c00fe8285](https://styles.refero.design/style/4040f97b-42cf-49ef-ab2d-a77c00fe8285) |
| Theme | light |
| Industry | design |

## Overview

The mostlikely design system presents a stark, high-contrast aesthetic reminiscent of architectural blueprints. It uses a minimal palette of pure black and white, creating a dense, graphical quality. Typography is custom and compact, lending a utilitarian yet refined feel. Components are defined by sharp lines and simple forms, emphasizing structure and clarity over elaborate ornamentation. The overall impression is one of directness and essentialism, where every visual element serves a clear functional purpose.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated surfaces like cards, provides a crisp base for all content |
| Ink Black | `#000000` | neutral | Primary text, borders, dividers, and solid background elements. Defines structure and provides strong contrast |

## Typography

### Rondelle

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 30px |
| lineHeight | 1.40 |
| substitute | Arial |
| role | Primary typeface for all text content, including navigation, headings, and body text. Its custom nature gives the site a unique, almost hand-drawn or technical feel. Weight 400 is consistently used, contributing to the system's uniform visual density. |

## Spacing

### radius

| Key | Value |
| --- | --- |
| default | 0px |

- **elementGap** — 10px
- **sectionGap** — 70px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Primary Navigation Link

**Role:** Top navigation items

Text in Ink Black (Rondelle, 14px, 400 weight) with a 1px solid Ink Black border on the bottom. Padding of 5px top/bottom and 20px left/right. The underline appears on hover/active states, creating a clear visual cue without adding visual weight when inactive.

### Header Bar

**Role:** Persistent top-level navigation and branding container

A horizontal bar with a white background and a 1px Ink Black border along its bottom. Contains navigation links and possibly a logo. Height is approximately 55px (5px padding top + content height + 5px padding bottom + 1px border).

### Interactive Box with Border

**Role:** Interactive elements, image containers, or content blocks

An element with a 1px solid Ink Black border. Can contain text, images, or icons. The background typically defaults to Canvas White when content is present, or Ink Black for specific content areas or backgrounds as shown in the screenshot for large masked shapes. Padding varies, with 70px left/right observed on some wider elements.

## Layout

The page uses a maximum-width contained layout, with a prominent header that is full-width but visually contains elements. The hero section, as implied by the large black shapes, appears to break out of this containment, using full-bleed graphical elements. Content is arranged in a fluid manner, with clear vertical spacing between implied sections. The primary navigation is a horizontal bar at the top, emphasizing a clear, linear flow. The overall density feels open, with significant white space surrounding content.

## Imagery

The site employs a combination of abstract, granular black shapes and minimal iconography. There is no photography or complex illustrations. Graphics appear as large, sometimes masked, black forms with a textured, noisy fill, contrasting sharply with the white canvas. Icons are strictly outlined and monochrome, matching the overall stark aesthetic. Imagery serves a decorative and atmospheric role, adding a raw, tactile quality without distracting from the UI.

## Dos & Donts

### Do

- Use Canvas White (#ffffff) as the default background for all page sections and elevated content.
- Apply Ink Black (#000000) for all primary text, borders, and significant structural lines.
- Maintain a consistent 1px solid Ink Black border for all interactive elements, navigation markers, and content dividers.
- Employ the Rondelle typeface at weight 400 for all textual content, prioritizing clarity and directness.
- Utilize 5px vertical padding and 20px horizontal padding for navigation items to create a spacious, balanced feel.
- Ensure all elements maintain sharp, 0px border-radii; rounded corners are not part of this visual system.
- Structure UI with generous horizontal spacing, using 70px padding on the left and right for main content areas.

### Don't

- Avoid introducing any colors beyond Canvas White and Ink Black, as the system relies on a strictly monochrome palette.
- Refrain from using shadows or elevation effects; the design emphasizes flatness and clean lines.
- Do not apply rounded corners to any UI elements; all corners should be sharp and right-angled.
- Avoid decorative gradients or complex backgrounds; stick to solid color fills.
- Do not use generic system fonts; always specify 'Rondelle' for text.
- Do not use letter-spacing adjustments unless explicitly defined for a specific text style; default to normal.
- Avoid overly dense information layouts; prioritize comfortable spacing and clear segmentation of content.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #000000
accent: no distinct accent color
primary action: no distinct CTA color

Example Component Prompts:
Create a header bar: Canvas White background, 1px solid Ink Black bottom border, height around 55px. Content includes a 'Mostlikely' branding text (Rondelle, 14px, 400 weight, Ink Black) and navigation links (Rondelle, 14px, 400 weight, Ink Black, 5px vertical padding, 20px horizontal padding, 1px Ink Black bottom border on hover).
Create a basic interactive box: Canvas White background, 1px solid Ink Black border, 0px border-radius, contains text (Rondelle, 14px, 400 weight, Ink Black) and a small outlined Ink Black icon.
Create a full-width background section: Fill the entire width with a pure Canvas White background. Then, within this section, create large abstract shapes using Ink Black as a background color, ensuring they maintain the textured, noisy fill evident in the original image.

---
_Source: https://styles.refero.design/style/4040f97b-42cf-49ef-ab2d-a77c00fe8285_
