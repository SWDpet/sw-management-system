# Qatchup — Design Reference

> Whiteboard doodle transparency.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.qatchup.com](https://www.qatchup.com) |
| Refero page | [https://styles.refero.design/style/1b010453-80df-406a-8b1a-72630c4a5165](https://styles.refero.design/style/1b010453-80df-406a-8b1a-72630c4a5165) |
| Theme | light |
| Industry | productivity |

## Overview

Qatchup employs a whiteboard-chic aesthetic: a bright white canvas paired with high-contrast dark text and controls. Its visual identity relies on precise typography with subtle negative letter-spacing, a playful doodle illustration style, and softly shadowed cards that lift content just above the stark background. Interaction elements are clearly defined through solid fills, outlined variants, and generous radius values, creating a friendly yet direct user experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#fafafa` | neutral | Page backgrounds, header backgrounds, card surfaces |
| Absolute Zero | `#292929` | neutral | Primary text, darkest surface elements, filled button backgrounds, primary navigation text |
| Graphite | `#696969` | neutral | Secondary text, icons, helper text, subdued borders |
| Ink Black | `#080808` | neutral | Headline text, high-contrast text elements |
| Cloud White | `#f4f4f5` | neutral | Subtle background for ghost button states, card surfaces |
| Silver Mist | `#cccccc` | neutral | Muted borders and button text |
| Cool Gray | `#e4e4e7` | neutral | Light borders for images and textual elements |
| Light Steel | `#b2b2b2` | neutral | Tertiary text for card descriptions and subtle details |
| Stone Gray | `#999999` | neutral | Minor textual details, pricing suffixes |
| Deep Gray | `#222222` | neutral | Darker iconography and text accents |
| Blue Pop | `#0bA5EC` | accent | Decorative card background for specific feature highlights. This is the only vivid color, making it a powerful accent |

## Typography

### Aspekta

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px, 32px, 40px, 48px, 56px |
| lineHeight | 1.14, 1.17, 1.20, 1.25, 1.33, 1.38, 1.40, 1.43, 1.50, 1.56, 2.00 |
| letterSpacing | -0.0350em at 56px, -0.0100em at 12px |
| substitute | Inter |
| role | All primary interface text, including headings, body, buttons, and links. The consistent use of a single font family creates typographic unity, while varied weights and precise letter-spacing sculpt hierarchy. |

### Fasthand

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 32px |
| lineHeight | 1.38 |
| letterSpacing | -0.0100em |
| substitute | Kalam |
| role | Decorative introductory text and subtle section labels, providing a handwritten, informal counterpoint to the primary typeface. |

### Aspekta 500

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.5 |
| role | Aspekta 500 — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 2 | -0.12 |
| body-sm | 14 |  | 1.5 | -0.16 |
| body | 16 |  | 1.5 | -0.18 |
| subheading | 18 |  | 1.43 | -0.29 |
| heading-sm | 20 |  | 1.4 | -0.32 |
| heading | 24 |  | 1.33 | -0.38 |
| heading-lg | 32 |  | 1.25 | -0.64 |
| display | 56 |  | 1.14 | -1.96 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 32px |
| images | 16px |
| buttons | 100px |

- **elementGap** — 10px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#fafafa` | 0 | Primary page background and base layer for all content. |
| Cloud White | `#f4f4f5` | 1 | Slightly off-white for subtle background variations or ghost button states, creating minimal depth. |
| Absolute Zero | `#292929` | 2 | Darkest background for content blocks requiring high contrast or visual weight, often in monochromatic sections. |

## Components

### Primary Filled Button

**Role:** Key interactions and calls to action.

Filled with Absolute Zero (#292929) background, Canvas White (#fafafa) text, 100px border-radius, and 10px vertical, 16px horizontal padding.

### Ghost Button

**Role:** Secondary actions or navigation items.

Transparent background with Absolute Zero (#292929) text and a 1px Absolute Zero (#292929) border, with 100px border-radius and 10px vertical, 16px horizontal padding.

### Disabled Button

**Role:** Non-interactive elements indicating future availability.

Cloud White (#f4f4f5) background with muted Silver Mist (#cccccc) text, 100px border-radius and 11px vertical, 22px horizontal padding.

### Elevated Content Card

**Role:** Displaying key information with subtle separation from the background.

Canvas White (#fafafa) background, 32px border-radius, and a soft shadow (rgba(0, 0, 0, 0.2) 0px 0px 0.5px 0.5px, rgba(0, 0, 0, 0.08) 0px 1px 1px -0.5px, rgba(0, 0, 0, 0.1) 0px 2px 4px 0px). Padding is 32px vertical, 24px horizontal.

### Section Divider Card

**Role:** Visually distinct blocks within a section, often serving as backgrounds for content groupings.

Absolute Zero (#292929) background with 32px border-radius. No padding or shadows.

### Badge/Tag

**Role:** Small, informational labels.

Solid Blue Pop (#0bA5EC) background with a 999px border-radius. No padding or content specific styles are present, indicating it's primarily a decorative container.

### List Item Card

**Role:** Individual items in lists, often with no distinct background for a minimal appearance.

Transparent background with 0px border-radius and no shadows or padding, relying on surrounding layout for structure.

## Layout

The page primarily uses a full-width canvas that contains content sections within a logical, often centered, horizontal alignment. The hero section features a centered headline over a background that combines a white canvas with a playful full-width doodle illustration. Sections alternate between full-width content blocks and those with specific max-widths for legibility. Content is arranged in flexible patterns, including centered stacks and implicit 2-column layouts (text left, text right). Navigation is a minimal top bar with right-aligned links and buttons.

## Imagery

The site uses line-based, cartoon-style illustrations that resemble hand-drawn doodles, characterized by organic shapes and a multi-color palette that contrasts with the monochrome UI. These illustrations are decorative, providing atmosphere and breaking up text-heavy sections. Product UI elements are presented as clean, white cards with subtle shadows, hinting at product screenshots but rendered in a clean, elevated style. Icons are simple, outlined, and monochromatic, maintaining a light visual weight.

## Dos & Donts

### Do

- Prioritize Canvas White (#fafafa) for all backgrounds and primary surfaces.
- Use Absolute Zero (#292929) as the default color for most text, buttons, and prominent UI elements.
- Apply Aspekta (Inter) for all functional text; use Fasthand (Kalam) sparingly for decorative headers or accents as seen in the hero section.
- Utilize 100px border-radius for buttons and 32px for cards to maintain a consistently soft, approachable aesthetic.
- Employ the subtle shadow (rgba(0, 0, 0, 0.2) 0px 0px 0.5px 0.5px, rgba(0, 0, 0, 0.08) 0px 1px 1px -0.5px, rgba(0, 0, 0, 0.1) 0px 2px 4px 0px) to elevate key content cards.
- Maintain negative letter-spacing for headings and larger text sizes to ensure a tight, intentional typographic appearance.
- Use Blue Pop (#0bA5EC) exclusively as a high-impact accent color for small, functional elements or decorative blocks, not for primary text or extensive fills.

### Don't

- Avoid introducing additional saturated colors; maintain the predominantly achromatic palette with Blue Pop (#0bA5EC) as the sole accent.
- Do not use sharp corners; all interactive and content-holding components should employ significant border-radii.
- Refrain from heavy, multi-layered shadows; the design relies on subtle, single-layer elevation where present.
- Do not use generic system fonts; stick to Aspekta (Inter) and Fasthand (Kalam) for brand consistency.
- Avoid complex gradients or patterns; the aesthetic is clean, flat, and uses solid color fields.
- Do not vary line-height significantly from the established typographic scale; maintain the compact, readable vertical rhythm.
- Do not use a narrow page maximum width; content should comfortably expand horizontally within a flexible layout.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #080808
background: #fafafa
border: #e4e4e7
accent: #0bA5EC
primary action: #292929 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #292929 background, #fafafa text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create an elevated feedback card: Canvas White (#fafafa) background, 32px border-radius, soft shadow (rgba(0, 0, 0, 0.2) 0px 0px 0.5px 0.5px, rgba(0, 0, 0, 0.08) 0px 1px 1px -0.5px, rgba(0, 0, 0, 0.1) 0px 2px 4px 0px). Use 32px vertical and 24px horizontal padding. Inside, place a 24px Aspekta weight 500 Ink Black (#080808) title 'Give Feedback', and 16px Aspekta weight 400 Graphite (#696969) description 'Share your thoughts & insights'.
3. Create a ghost navigation button: Transparent background, Aspekta 16px weight 400 Absolute Zero (#292929) text, 100px border-radius, 1px Absolute Zero (#292929) border, and 10px vertical, 16px horizontal padding.

---
_Source: https://styles.refero.design/style/1b010453-80df-406a-8b1a-72630c4a5165_
