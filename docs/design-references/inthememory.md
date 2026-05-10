# Inthememory — Design Reference

> white canvas, vibrant data points

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://inthememory.com](https://inthememory.com) |
| Refero page | [https://styles.refero.design/style/8872694d-261d-4eb4-b355-fb39ee4c37ad](https://styles.refero.design/style/8872694d-261d-4eb4-b355-fb39ee4c37ad) |
| Theme | light |
| Industry | fintech |

## Overview

Inthememory presents a high-contrast, data-driven aesthetic with a vibrant dual-accent approach. A stark white canvas is punctuated by deep charcoal text, while a vivid orange provides energetic highlights and a bright blue marks active states and notifications. Component treatments are minimal: soft rounded corners and ghost-like interactive elements keep the focus on content and data. The overall impression is one of clarity and functional precision.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button backgrounds |
| Deep Charcoal | `#102126` | neutral | Primary text, main headings, deep background surfaces, filled button backgrounds, accented borders |
| Ash Gray | `#e9eef0` | neutral | Subtle borders, divider lines, decorative strokes |
| Cloud Gray | `#f1f7f9` | neutral | Secondary background surfaces, subtle card backgrounds |
| Muted Slate | `#3d5761` | neutral | Secondary text, descriptive body copy, muted borders |
| Light Slate | `#677b82` | neutral | Muted body text, helper text, captions, less emphasized content |
| Data Orange Strong | `#fa4e1d` | brand | Decorative accents, illustration fills, high-impact highlights. Appears in the brand's data visualization |
| Data Orange Soft | `#fb6338` | brand | Card backgrounds for featured content, illustration fills — a softer variant of Data Orange Strong |
| UI Blue | `#0c67ff` | accent | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Badge Blue Text | `#0519d5` | semantic | Text color for information badges, decorative strokes in badges |
| Badge Blue Background | `#e4eeff` | semantic | Background color for information badges |

## Typography

### Graphik

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px, 18px, 20px, 40px, 56px |
| lineHeight | 1.00, 1.10, 1.18, 1.20, 1.33, 1.50, 1.70 |
| letterSpacing | normal |
| substitute | system-ui |
| role | The primary typeface for all textual content, from headings to body text and UI elements. Its clean, geometric sans-serif nature provides a modern and professional tone, supporting the data-driven brand identity. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.7 |  |
| body-sm | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.33 |  |
| heading-sm | 20 |  | 1.2 |  |
| heading | 40 |  | 1.18 |  |
| display | 56 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| links | 20px |
| badges | 6px |
| buttons | 100px |
| default | 12px |

- **elementGap** — 24px
- **sectionGap** — 48px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Components

### Primary Filled Button

**Role:** Calls to action

Solid Deep Charcoal (#102126) background with Canvas White (#ffffff) text. Features a fully rounded 'pill' shape with 100px border-radius and 10px vertical, 12px horizontal padding. Text uses Graphik 400.

### Outlined Ghost Button

**Role:** Secondary actions, filtering, navigation

Transparent background with Deep Charcoal (#102126) border and text. Fully rounded 'pill' shape with 100px border-radius and 10px vertical, 12px horizontal padding. Text uses Graphik 400.

### Action Icon Button

**Role:** Small interactive icons or controls

Deep Charcoal (#102126) background with Canvas White (#ffffff) icon. Circular with 50% border-radius and 0px padding. Icons are typically small, indicating action without text.

### Primary Card

**Role:** Content grouping, feature showcase

Canvas White (#ffffff) background with 12px border-radius. Padding is 24px vertical and 32px horizontal. No box shadow, relying on spacing for separation.

### Featured Orange Card

**Role:** Highlighting key information or stats

Data Orange Soft (#fb6338) background with 12px border-radius. Padding is 24px on all sides. No box shadow.

### Information Badge

**Role:** Status indicators, new feature tags

Badge Blue Background (#e4eeff) with Badge Blue Text (#0519d5). Has a 6px border-radius and 4px vertical, 8px horizontal padding. Text uses Graphik 400 at 12px.

## Layout

The page exhibits a max-width contained layout, likely around 1200px, centered on a white canvas. The hero section features a prominent, centered headline with integrated brand icons, followed by a lighter body text and a Dark Charcoal button. Sections below alternate between large text-dominant blocks and grid-based content, such as a 3-column card grid for features. Vertical rhythm is established by consistent section gaps, creating structured breathing room. Navigation is a fixed top bar on a white background, featuring a logo, links, and 'Book a demo' button.

## Imagery

Imagery primarily consists of clean, outlined vector illustrations with subtle fills in brand accent colors (Data Orange Strong and UI Blue). These illustrations are used decoratively and explanatorily within cards and feature sections, maintaining a light, organized feel. Product screenshots are contained within device mockups or abstract shapes, often featuring a vibrant blue play button, keeping the focus on the UI itself rather than lifestyle photography. Icons are generally outlined, matching the illustration style, with a moderate stroke weight.

## Dos & Donts

### Do

- Prioritize Deep Charcoal (#102126) for all primary text and calls to action.
- Use Canvas White (#ffffff) as the dominant background, ensuring high contrast with text.
- Apply a 100px border-radius to all interactive buttons and navigation pills for a consistent, soft, 'pill' shape.
- Utilize Cloud Gray (#f1f7f9) for subtle background shifts to differentiate sections or cards without strong visual breaks.
- Employ Data Orange Strong (#fa4e1d) sparingly for decorative illustration elements or key highlight accents, never as a primary button background unless outlined.
- Maintain a comfortable density with 24px as the standard element gap and card padding.
- Use UI Blue (#0c67ff) exclusively for active navigation states, small interactive icons, and notifications.

### Don't

- Avoid using multiple chromatic colors for primary actions; stick to Deep Charcoal for filled buttons.
- Do not introduce strong box shadows; rely on background color changes and spacing for visual hierarchy.
- Refrain from using heavily saturated colors for large surface areas; reserve them for accents and specific data points.
- Do not deviate from the Graphik typeface; no other fonts should be introduced.
- Avoid arbitrary border radii; adhere strictly to 6px for badges, 12px for cards, and 100px for buttons/pills.
- Do not use gradients for backgrounds or components, as the system relies on solid color blocks.
- Avoid centered text for body paragraphs; keep text left-aligned for readability.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #102126
background: #ffffff
border: #e9eef0
accent: #0c67ff
primary action: #102126 (filled action)

Example Component Prompts:
Create a Primary Action Button: #102126 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Generate an Information Badge: 'What’s new', using Badge Blue Background (#e4eeff) and Badge Blue Text (#0519d5), with 6px border-radius and 4px vertical, 8px horizontal padding.


Create a secondary navigation item: Outlined Ghost Button with 'For Retailers' text, Border Deep Charcoal (#102126), Text Deep Charcoal (#102126), 100px border-radius, 10px vertical, 12px horizontal padding, Graphik 16px weight 400.

---
_Source: https://styles.refero.design/style/8872694d-261d-4eb4-b355-fb39ee4c37ad_
