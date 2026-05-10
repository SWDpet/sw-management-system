# Motto® — Design Reference

> Architectural Blueprint on White Canvas. Black lines and precise typography articulate structure and ideas against an expansive, bright background.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://wearemotto.com](https://wearemotto.com) |
| Refero page | [https://styles.refero.design/style/6eb5fc89-d0db-4293-8bff-13c5aa530a28](https://styles.refero.design/style/6eb5fc89-d0db-4293-8bff-13c5aa530a28) |
| Theme | light |
| Industry | agency |

## Overview

The Motto® design system evokes an atmosphere of rigorous clarity and understated authority. Its visual language relies on stark contrasts between crisp black text and an expansive canvas of near-white, creating a sophisticated and professional impression. The dominance of bold, custom san-serif typography, particularly the expansive display font, dictates the site's strong visual rhythm. A distinct lack of ornamentation, shadows, or vibrant color accents streamlines the user experience to focus purely on content and impactful messaging.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Black | `#000000` | neutral | Primary text, essential icons, borders, interactive states for text links, navigation items, and button outlines. It grounds the design in high contrast and definitive presence. |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button text, and filled button text for contrast. Defines the vast, airy negative space. |
| Charcoal Surface | `#1b1b1c` | neutral | Solid button fills for primary calls to action. Provides a rich, deep counterpoint to Canvas White without introducing color. |
| Cloud Gray | `#f2f2f2` | neutral | Subtle background for hero sections or alternating content blocks, offering a slight visual break from pure Canvas White without significant tonal shift. |
| Stone Accent | `#d8d8d8` | neutral | Divider lines, subtle borders, and background accents, providing minimal separation. |
| Ash Text | `#4d5153` | neutral | Secondary body text and subtle link colors, used for less prominent information or tertiary content. |
| Silver Text | `#848484` | neutral | Lighter body text for lists or less emphasized links, contributing to the grayscale palette. |
| Input Border | `#c8cacd` | neutral | Border color for input fields, indicating interactive areas with a fine, light line. |
| Faint Gray | `#717476` | neutral | Tertiary body text, used for footnotes or less critical content where readability is still important but emphasis is lower. |
| Vivid Purple | `#9c98ef` | accent | Rare, decorative background graphic elements; not part of the primary UI palette. |
| Electric Violet | `#6980ff` | accent | Rare, decorative background graphic elements; not part of the primary UI palette. |
| Grass Green | `#beee98` | accent | Rare, decorative background graphic elements; not part of the primary UI palette. |

## Typography

### sans

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 14px, 15px, 17px, 18px, 20px, 25px, 34px, 48px |
| lineHeight | 1.14, 1.26, 1.30, 1.38, 1.40, 1.60 |
| letterSpacing | normal |
| substitute | Inter, Arial, Helvetica, sans-serif |
| role | UI elements, body text, subheadings, and captions. The consistent medium weight (500) across various sizes provides a clean, readable, and authoritative voice for all functional text. |

### disp

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 61px, 99px, 138px, 139px, 154px |
| lineHeight | 1.00, 1.10 |
| letterSpacing | normal |
| substitute | Oswald, Impact, sans-serif |
| role | Headlines and display text. Its large scale and medium weight dictate a commanding visual presence, allowing key messages to dominate without being overtly aggressive. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.4 |  |
| body | 17 |  | 1.38 |  |
| subheading | 25 |  | 1.14 |  |
| heading-sm | 34 |  | 1.14 |  |
| heading | 48 |  | 1.14 |  |
| heading-lg | 61 |  | 1.1 |  |
| display-sm | 99 |  | 1 |  |
| display | 154 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 0px |
| buttons | 9999px |

- **elementGap** — 
- **sectionGap** — 48px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Services Tag Strip

### Testimonial Card

### Section Header with CTA

### Navigation Link

**Role:** Standard interactive navigation item

Text link with default color Pitch Black (#000000), no background, no border, and 0px padding. Uses 'sans' font family at various sizes with weight 500. Active states are not explicitly styled but follow the black text over white canvas principle.

### Ghost Button (Text)

**Role:** Secondary action or informational link styled as a button

Transparent background, Pitch Black (#000000) text and border. 0px padding, 0px border-radius. Uses 'sans' font family.

### Ghost Button (White Text)

**Role:** Secondary action on a dark background

Transparent background, Canvas White (#ffffff) text and border. 0px padding, 0px border-radius. Uses 'sans' font family.

### Filled Button (Pill, Medium)

**Role:** Primary Call to Action

Charcoal Surface (#1b1b1c) background, Canvas White (#ffffff) text. 9999px border-radius, 0px vertical padding, 28.8px horizontal padding. Uses 'sans' font family.

### Ghost Button (Pill, Icon-only)

**Role:** Iconic secondary action

Transparent background, Canvas White (#ffffff) text/icon, Canvas White (#ffffff) border. 9999px border-radius, 15.36px padding on all sides. Uses 'sans' font family.

### Info Card (No Border/Shadow)

**Role:** Neutral content container

Transparent background, 0px border-radius, no box shadow. No intrinsic padding, content padding applied internally. Used for testimonials or feature blocks.

### Text Input (Underlined)

**Role:** User data entry field

Transparent background, Canvas White (#ffffff) text. Input Border (#c8cacd) bottom border. 0px border-radius, 24px top/bottom padding, 48px right padding. Uses 'sans' font family.

## Layout

The site employs a max-width contained layout, centered on a Canvas White background, creating a stable and focused content area. The hero section is full-width with a dramatic, oversized headline ('disp' font) vertically centered, setting an immediate tone of prominence. Sections maintain consistent vertical spacing, often with a large sectionGap of 48px, leading to a spacious feel. Content is primarily arranged in single-column stacks or simple two-column layouts for text alongside conceptual elements. There is no grid visible for cards or features; content blocks are isolated entities, emphasizing individual messages. The navigation is a persistent top bar, sticky or otherwise always available, maintaining minimal branding and focusing on key links.

## Imagery

The visual language is characterized by an absence of traditional imagery in the main UI areas. Photography is not present. Instead, the focus is on a stark, text-dominant interface, occasionally featuring abstract graphic elements like the asterisk or arrow. When present, graphics are inline and monochromatic (Pitch Black on Canvas White), functional rather than decorative, serving to guide or symbolize concepts. The visual weight is carried almost entirely by typography.

## Dos & Donts

### Do

- Prioritize high contrast between text (Pitch Black #000000) and background (Canvas White #ffffff or Cloud Gray #f2f2f2) for legibility.
- Use 'disp' font (weight 500) exclusively for large, impactful headlines (61px to 154px) to establish a clear hierarchy and brand voice.
- Maintain a sense of generous whitespace; sections should breathe with sectionGap of 48px and elementGap varying from 8px to 70px.
- Apply 9999px border-radius only to interactive buttons and similar atomic elements like tags, creating distinct 'pill' shapes.
- All non-interactive content containers (cards, content blocks) should have 0px border-radius, reinforcing a sharp, clean aesthetic.
- Use Charcoal Surface (#1b1b1c) for primary button fills to provide a strong visual anchor without introducing chromatic color.

### Don't

- Avoid using drop shadows or heavy gradients; the elevation philosophy is based on content hierarchy and spatial separation, not artificial depth.
- Do not introduce color to UI elements or text beyond the defined neutral palette for brand and accent; chromatic colors are strictly decorative.
- Do not deviate from the 'sans' font family (weight 500) for body text, links, and navigation; avoid mixing font weights that are not explicitly defined.
- Never use rounded corners on cards, content sections, or layout blocks; maintain sharp, angular forms.
- Do not use subtle variations in gray tones for text that reduce contrast below AAA accessibility levels; stick to defined text colors like Pitch Black, Ash Text, or Silver Text.
- Avoid decorative imagery that competes with the bold typography; stick to abstract, monochromatic, or product-focused visuals if present.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Pitch Black): #000000
- Background (Canvas White): #ffffff
- Primary Button (Charcoal Surface): #1b1b1c
- Button Text (Canvas White): #ffffff
- Secondary Text (Ash Text): #4d5153
- Subtle BG (Cloud Gray): #f2f2f2

### Example Component Prompts
1. Create a hero section: Canvas White (#ffffff) background. Headline 'Ideas Worth Rallying Around' using 'disp' font, weight 500, size 139px, line-height 1, in Pitch Black (#000000). Subtext 'Motto® is a strategic branding agency...' using 'sans' font, weight 500, size 17px, line-height 1.38, in Pitch Black (#000000). Add a large down arrow graphic (Pitch Black) below the subtext. Ensure generous vertical spacing.
2. "Create a primary call-to-action button: 'Work with us' text, Charcoal Surface (#1b1b1c) background, Canvas White (#ffffff) text. Use 'sans' font, weight 500. Set border-radius to 9999px. Add 0px vertical padding and 28.8px horizontal padding."
3. "Generate a testimonial card grid with two cards: Each card has a Canvas White (#ffffff) background, 0px border-radius, and no box-shadow. Inside, include a headline 'PRAISE FROM CLIENTS' in 'disp' font (size depends on context, but bold), and a body text quote in 'sans' font, weight 500, size 17px, line-height 1.38, in Pitch Black (#000000). Separate cards with a generous element gap. Card content should be left-aligned."
4. "Design a footer section: Background is likely Canvas White (#ffffff). Include navigation links (Pitch Black #000000 text, 'sans' font, weight 500, size 15px) and copyright information (Ash Text #4d5153, 'sans' font, weight 500, size 14px). Ensure ample padding and element gaps, consistent with the spacious theme."

---
_Source: https://styles.refero.design/style/6eb5fc89-d0db-4293-8bff-13c5aa530a28_
