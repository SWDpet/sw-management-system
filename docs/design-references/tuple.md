# Tuple — Design Reference

> Crisp developer canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://tuple.app](https://tuple.app) |
| Refero page | [https://styles.refero.design/style/1003d7c5-536c-485d-b191-a34178415eac](https://styles.refero.design/style/1003d7c5-536c-485d-b191-a34178415eac) |
| Theme | light |
| Industry | devtools |

## Overview

Tuple uses a clean, developer-focused aesthetic with a predominantly achromatic palette, punctuated by functional, vivid violet for primary actions. The interface features crisp typography, deliberate use of rounded corners, and a comfortable density. Subtle shadows and borders define element relationships, while contrasting dark sections provide visual breaks. Color is used sparingly, primarily to highlight interactive elements and convey status.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, surface elements, text on dark backgrounds |
| Canvas Fog | `#f4f4f5` | neutral | Secondary section backgrounds, subtle surface distinction |
| Border Ash | `#e4e4e7` | neutral | Soft icon strokes, subtle dividers, and low-emphasis decorative details. Do not promote it to the primary CTA color |
| Content Graphite | `#71717a` | neutral | Secondary text, muted descriptions, inactive states |
| Text Slate | `#27272a` | neutral | Primary text for headings and body, strong contrast against light backgrounds; Dark mode sections, code block backgrounds, elevated card backgrounds in dark contexts |
| Dark Border Zinc | `#52525b` | neutral | Borders within dark mode surfaces |
| Dark Accent Charcoal | `#3f3f46` | neutral | Subtle background for elements within dark mode, code elements |
| Grape Action | `#6a5ed9` | brand | Primary call-to-action buttons, active navigation indicators — a vibrant accent against neutral tones |
| Sky Brand | `#3f71d4` | brand | Brand illustrations, icon accents, specific headings |
| Sunset Orange | `#db5434` | accent | Decorative strokes, some highlight text, illustration elements |
| Success Green | `#1bb152` | accent | Green text accent for links, tags, and emphasized short phrases. Use as a supporting accent, not as a status color |
| Soft Success | `#4ade80` | accent | Green text accent for links, tags, and emphasized short phrases. Use as a supporting accent, not as a status color |
| Strong Success | `#22863a` | accent | Green text accent for links, tags, and emphasized short phrases. Use as a supporting accent, not as a status color |
| Warning Yellow | `#ffb929` | accent | Yellow wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Error Red | `#ef4444` | accent | Red wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Gradient Sunset | `#e06c48` | accent | Decorative gradient for product showcases or special sections |
| Gradient Sky | `#38bdf8` | accent | Decorative gradient for product showcases or special sections |

## Typography

### Inter Variable

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 13px, 14px, 16px, 18px, 20px, 24px, 36px, 48px, 60px |
| lineHeight | 1.00, 1.11, 1.14, 1.20, 1.33, 1.40, 1.43, 1.50, 1.56, 1.63 |
| letterSpacing | normal |
| fontFeatureSettings | 'cv05' on, 'cv06' on, 'ss01' on |
| substitute | Inter |
| role | Primary UI text, headings, body copy, navigation. Features 'cv05', 'cv06', 'ss01' for nuanced typographic control. |

### DM Mono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 11px, 12px, 13px, 14px, 16px |
| lineHeight | 1.33, 1.43, 1.50, 1.63 |
| letterSpacing | 0.05em to 0.1em |
| fontFeatureSettings | 'ss03' on, 'ss04' on, 'ss05' on |
| substitute | Space Mono |
| role | Monospaced text for code snippets, technical details, and certain navigation elements. Features stylistic sets 'ss03', 'ss04', 'ss05' for code legibility. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.43 |  |
| body | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.4 |  |
| heading | 36 |  | 1.11 |  |
| heading-lg | 48 |  | 1.14 |  |
| display | 60 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 12px |
| input | 8px |
| large | 24px |
| small | 8px |
| buttons | 12px |
| default | 12px |
| navItem | 4px |

- **elementGap** — 8px
- **sectionGap** — 32px
- **cardPadding** — 32px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Base page background and default layer for most content and components. |
| Canvas Fog | `#f4f4f5` | 1 | Secondary background for distinct sections or subtle card surfaces, differentiating from the base canvas. |
| Dark Surface | `#27272a` | 2 | Background for dedicated dark mode sections or elevated components within a dark context, creating strong contrast. |

## Components

### Primary Action Button

**Role:** Main call-to-action

Filled button with 'Grape Action' (#6a5ed9) background and 'Canvas White' (#ffffff) text. Features 12px border-radius and generous 16px vertical, 32px horizontal padding. Font is Inter Variable, weight 700.

### Default Card

**Role:** Content container

White (#ffffff) background with 12px border-radius. Shadow: rgba(0, 0, 0, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.05) 0px 1px 2px 0px. Internal padding of 48px on all sides.

### Subtle Card

**Role:** Secondary content container

Subtle 'Canvas Fog' (#fafafa ~ #f4f4f5) background with 12px border-radius and no shadow. Internal padding of 32px on all sides.

### Dark Elevated Card

**Role:** Content container in dark sections

Dark Surface (#27272a) background with 12px border-radius. Features a subtle 1px border shadow: rgba(0, 0, 0, 0.75) 0px 0px 0px 1px. No explicit padding mentioned directly, implying content controls it. Text typically 'Canvas White'.

### Alert Banner

**Role:** Prominent announcements

Notification banner with light background (e.g., #dcfce7, a very light green hue not explicitly a token) and contrasting color text/icons. Example usage shows Green Accents for success-like notices.

### Floating Card

**Role:** Ephemeral or interactive card with strong elevation

Appears on light background (#ffffff) with 8px border-radius and a prominent shadow: rgba(0, 0, 0, 0.25) 0px 25px 50px -12px. No explicit padding detected, suggesting content may fill it.

## Layout

The page primarily uses a contained layout with a `pageMaxWidth` of approximately 1200px, centered horizontally. The hero section is full-bleed with a prominent, centered headline and a call-to-action. Content sections alternate between these full-width dark blocks and light, contained sections. Many content blocks feature a two-column layout, often with text on one side and a product visual on the other, or a three-column grid for features/logos. Vertical spacing is comfortable and consistent, using 32px section gaps to create a balanced rhythm between content blocks. A sticky top navigation bar provides consistent access to primary links and actions.

## Imagery

The site uses a mix of tightly cropped, realistic product screenshots (showing Mac and Windows UI elements) and stylized, conceptual vector illustrations. Product screenshots are often contained within rounded, elevated frames, highlighting the software's interface. Illustrations are flat, organic, and utilize the brand's chromatic colors like 'Sky Brand' and 'Sunset Orange', serving both decorative and explanatory roles. Icons are filled, with a medium stroke weight where outlines appear. Imagery density is moderate, providing visual breaks but not dominating the page, allowing text and UI elements to remain primary.

## Dos & Donts

### Do

- Prioritize 'Text Slate' (#27272a) for body and heading text on light backgrounds and 'Canvas White' (#ffffff) on dark backgrounds.
- Use 'Grape Action' (#6a5ed9) exclusively for primary interactive elements, reserving it for clear calls-to-action.
- Apply 12px border-radius for all main buttons and cards to maintain a consistent rounded aesthetic.
- Utilize 'Border Ash' (#e4e4e7) for all hairline borders, dividers, and subtle outlines on light surfaces.
- Maintain a clear hierarchy using Inter Variable for all primary UI text and DM Mono for code or technical content.
- Employ 32px for section vertical spacing and 8px for internal element gaps to ensure comfortable density.
- Use rgba(0, 0, 0, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.05) 0px 1px 2px 0px for subtle elevation, primarily on cards and navigation.

### Don't

- Avoid using multiple chromatic colors for primary calls-to-action; reserve 'Grape Action' (#6a5ed9) for this role.
- Do not deviate from the established 12px border-radius for buttons and cards without clear intent, as this is a core visual identifier.
- Do not use generic system fonts; always utilize Inter Variable and DM Mono with their specified `fontFeatureSettings`.
- Avoid excessive use of shadows; stick to the defined shadow tokens for light elevation and more pronounced shadows for overlays.
- Do not create arbitrary color accents; refer to 'Sky Brand', 'Sunset Orange', 'Success Green', and 'Error Red' for their specific decorative or semantic purposes.
- Do not overcrowd sections; ensure comfortable vertical spacing of 32px between major content blocks.
- Avoid text weights below 400 for body copy on any background, as legibility is paramount.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #27272a
- background: #ffffff
- border: #e4e4e7
- accent: #3f71d4
- primary action: #6a5ed9 (filled action)

Example Component Prompts:
- Create a testimonial card: 'Canvas White' (#ffffff) background, 12px border-radius, shadow rgba(0, 0, 0, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.05) 0px 1px 2px 0px. Inner padding 48px, text 'Text Slate' (#27272a) Inter Variable weight 400. Quote source 'Content Graphite' (#71717a) Inter Variable weight 500.
- Design a call-to-action button: 'Grape Action' (#6a5ed9) background, text 'Canvas White' (#ffffff) Inter Variable weight 700, 12px border-radius, 16px vertical padding, 32px horizontal padding.
- Build a feature section header: Headline 'Text Slate' (#27272a) at 48px Inter Variable weight 700, followed by a subheading 'Content Graphite' (#71717a) at 20px Inter Variable weight 400. Use 32px vertical spacing between sections.
- Create a code snippet block: 'Dark Surface' (#27272a) background with a 'Dark Elevated Card Border' (rgba(0, 0, 0, 0.75) 0px 0px 0px 1px) with 12px border-radius. Code text in DM Mono weight 400 with syntax highlighting using 'Strong Success' (#22863a), 'Sky Brand' (#3f71d4), and 'Sunset Orange' (#db5434).

---
_Source: https://styles.refero.design/style/1003d7c5-536c-485d-b191-a34178415eac_
