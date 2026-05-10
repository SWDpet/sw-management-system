# Aninix — Design Reference

> Crisp Utility Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.aninix.com](https://www.aninix.com) |
| Refero page | [https://styles.refero.design/style/653b7b25-f610-4a46-90e1-e3eed98b1f24](https://styles.refero.design/style/653b7b25-f610-4a46-90e1-e3eed98b1f24) |
| Theme | light |
| Industry | design |

## Overview

Aninix presents an airy, contemporary aesthetic for a productivity tool, characterized by a predominantly achromatic canvas that highlights focused typographic elements. The interface uses rounded card surfaces and ghost-like interactive controls, with a single vivid violet accent color providing functional punctuation for key actions and branding. Typography is sharp and compact, ensuring information density without visual clutter, while subtle shadows offer a sense of lightweight elevation rather than heavy layering.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, body text on dark backgrounds |
| Midnight Text | `#0b1118` | neutral | Primary heading and body text, ensuring high contrast on light backgrounds |
| Charcoal Text | `#171717` | neutral | Secondary heading text, slightly softer than Midnight Text for varied hierarchy |
| Ink Grey | `#333333` | neutral | Tertiary text, used for less prominent content and subtle branding |
| Figma Grey | `#f0f0f0` | neutral | Background for secondary content blocks and cards on the main canvas |
| Muted Slate | `#5c6574` | neutral | Supportive body text, descriptive labels, and softer visual strokes |
| Ghost Shadow | `#ced3d9` | neutral | Subtle border and shadow accents, contributing to a lightweight visual feel |
| Dusty Blue | `#c4d2db` | neutral | Decorative highlights and very soft shadows, creating a gentle separation |
| Subtle Grey | `#89909a` | neutral | Secondary link text and very muted decorative elements |
| Action Violet | `#374fd5` | brand | Primary actionable elements like button backgrounds, branded links, and active states |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | Arial, Helvetica, sans-serif |
| role | Utility text, small labels, and iconography labels. This compact font reinforces the minimal aesthetic. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 400, 500, 700, 800 |
| sizes | 16px, 18px |
| lineHeight | 1.44 |
| substitute | Segoe UI, Roboto, Ubuntu, Cantarell, Noto Sans, sans-serif |
| role | General body text and navigation links, providing strong readability and a modern feel. |

### P22 Mackinac Pro Medium

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 20px |
| lineHeight | 1.40 |
| substitute | Georgia, serif |
| role | Specialized body text with italic variants, often used for quotes or emphasized content. |

### P22 Mackinac Pro Medium

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 20px |
| lineHeight | 1.40 |
| substitute | Georgia, serif |
| role | Introductions and prominent body text sections, offering a slightly more formal touch with its serif appearance. |

### Euclid Circular A

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 18px, 20px |
| lineHeight | 1.20, 1.30 |
| letterSpacing | -0.0100em |
| substitute | Panton, Montserrat, sans-serif |
| role | Headlines and prominent marketing taglines where a bold, subtly tracked, modern sans-serif is desired to convey impact. |

### P22 Mackinac Pro Medium Italic

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.5 |
| role | P22 Mackinac Pro Medium Italic — detected in extracted data but not described by AI |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| buttons | 8px |
| largeCards | 24px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Components

### Ghost Primary Button

**Role:** Main call to action for the plugin installation.

Background: Canvas White (#ffffff). Text color: default browser link blue (not specified). Border: 1px solid default browser link blue. Radius: 10px. Padding: 10px vertical, 24px horizontal. Shadow: rgba(104, 116, 123, 0.15) 0px 2px 7px 0px, rgba(104, 116, 123, 0.3) 0px 5px 15px 0px.

### Feature Content Card

**Role:** Displaying individual features or content blocks.

Background: Figma Grey (#f0f0f0). Radius: 16px. No shadow. No internal padding.

### Elevated Content Card

**Role:** Prominent content card, often used for testimonials or key information.

Background: Canvas White (#ffffff). Radius: 24px. No shadow. Padding: 40px on all sides.

### Branded Label

**Role:** Small, distinct labels with brand recognition.

Background: Action Violet (#374fd5). Text color: Canvas White (#ffffff). Radius: 8px. Padding: 4px vertical, 8px horizontal. Uses sans-serif 12px.

## Layout

The page uses a maximum-width contained layout rather than full-bleed, with content centered. The hero section features a large, centered headline and subheading, followed by a call-to-action button, all stacked vertically. Subsequent sections employ a mixed grid approach: a dominant card grid (likely 2-3 columns on wider screens) for features, and alternating text-left/image-right or text-right/image-left content blocks. Vertical rhythm is established by consistent spacing between sections, primarily 40px gaps. Navigation is a sticky top bar with left-aligned brand logo and right-aligned links and actions.

## Imagery

The site predominantly uses product screenshots and abstract, colorful Figma plugin-like illustrations. Photography is scarce and, when present, is tightly cropped and serves as background texture within content cards, maintaining a focused, functional aesthetic rather than a lifestyle one. Icons are minimal, featuring a clean outlined style for navigation, with a filled variant for the brand logo. Imagery generally serves to explain features and showcase the product in action, rather than for decorative atmosphere.

## Dos & Donts

### Do

- Prioritize Canvas White (#ffffff) as the dominant background for body content and primary surfaces.
- Use Action Violet (#374fd5) exclusively for primary interactive elements, active states, and small brand accents.
- Apply a 16px border-radius to content cards for a universally soft and approachable appearance.
- Maintain a compact information layout by generally using 10px for element spacing and 40px for section gaps.
- Employ Euclid Circular A with -0.0100em letter-spacing for all headlines to ensure a sharp, modern feel.
- Restrict elevation effects to ghost buttons, using rgba(104, 116, 123, 0.15) 0px 2px 7px 0px, rgba(104, 116, 123, 0.3) 0px 5px 15px 0px, to visually highlight interaction points without heavy layering.
- Use Midnight Text (#0b1118) and Charcoal Text (#171717) for main headings and body copy on light backgrounds.

### Don't

- Avoid introducing additional saturated colors unless explicitly assigned a functional role (e.g., semantic states) to preserve the brand's minimalist color scheme.
- Do not use heavy, opaque shadows; stick to the subtle, diffused ghost button shadow for all elevated elements, or no shadow at all.
- Refrain from altering the letter-spacing of sans-serif and system-ui fonts, as their `normal` tracking contributes to readability.
- Do not break the established 8px, 16px, and 24px border radius pattern across components; consistency is key to the system's soft aesthetic.
- Avoid using multiple accent colors; rely on Action Violet (#374fd5) as the sole chromatic element.
- Do not use dark backgrounds for major content sections; maintain the light theme with Canvas White and Figma Grey.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #0b1118
background: #ffffff
border: #ced3d9
accent: #374fd5
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Design a `Feature Content Card` with a `Figma Grey` background (#f0f0f0), 16px border-radius, and no internal padding for an image.
3. Implement an `Elevated Content Card` using `Canvas White` (#ffffff), 24px border-radius, and 40px internal padding on all sides, suitable for a testimonial block.
4. Produce a `Branded Label` displaying 'by Aninix', using `Action Violet` (#374fd5) background, `Canvas White` (#ffffff) text, 8px border-radius, and 4px vertical, 8px horizontal padding, with sans-serif 12px type.

---
_Source: https://styles.refero.design/style/653b7b25-f610-4a46-90e1-e3eed98b1f24_
