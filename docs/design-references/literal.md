# Literal — Design Reference

> Literary white canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://literal.club](https://literal.club) |
| Refero page | [https://styles.refero.design/style/7e698bf6-6b31-40ed-a89a-40f4c37ade38](https://styles.refero.design/style/7e698bf6-6b31-40ed-a89a-40f4c37ade38) |
| Theme | light |
| Industry | media |

## Overview

Literal presents a clean, content-focused system with a light, airy canvas. Typography is direct and legible, with a subtle serif accent for key headings. Surfaces are mostly flat with minimal elevation, emphasizing readable content over glossy UI. A single vibrant green serves as the primary accent, providing clear calls to action and punctuation within an otherwise neutral palette.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Forest Green | `#278458` | brand | Primary action backgrounds, brand accents, interactive elements — a vibrant, natural green that signifies progression and active states |
| Inkwell | `#444340` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |
| Book Page White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button text and borders |
| Shelf Gray | `#f8f8f8` | neutral | Secondary surface backgrounds, subtle UI panels |
| Parchment Gray | `#9a988b` | neutral | Muted text, secondary borders, discreet icons and link states |
| Divider Gray | `#eeeeee` | neutral | Hairline borders, subtle UI dividers, outlines for inactive elements |
| Pitch Black | `#000000` | neutral | Hero headings, specific button backgrounds, strong text emphasis |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| weights | 400, 500, 700 |
| sizes | 14px, 15px, 16px, 22px, 40px |
| lineHeight | 1.35, 1.40, 1.43, 1.45, 1.50 |
| substitute | system-ui |
| role | Primary text font for body, links, buttons, and most UI elements. Its clean sans-serif form ensures readability across various sizes. Used at 400 for body, 500 for captions and subtle prompts, 700 for button text and feature headings. |

### Libre Baskerville

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 28px |
| lineHeight | 1.35 |
| substitute | Georgia |
| role | Distinctive serif font used sparingly for main headings (e.g., 'Discover, organise & discuss books'). Its classic, refined character provides a literary feel against the modern sans-serif. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.43 |  |
| body-lg | 16 |  | 1.5 |  |
| subheading | 22 |  | 1.35 |  |
| heading | 28 |  | 1.35 |  |
| display | 40 |  | 1.35 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| buttons | 5px |
| default | 4px |

- **elementGap** — 20px
- **sectionGap** — 133px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Book Page White | `#ffffff` | 0 | Primary page background, base for content. |
| Shelf Gray | `#f8f8f8` | 1 | Secondary background for distinct content sections or panels. |

## Components

### Primary Action Button

**Role:** Filled button indicating a main action.

Background: Forest Green (#278458), Text: Book Page White (#ffffff), Border Radius: 5px, Padding: 0px vertical, 24px horizontal. Text is Inter 700.

### Compact Action Button

**Role:** Smaller, filled button for secondary calls to action.

Background: Forest Green (#278458), Text: Book Page White (#ffffff), Border Radius: 0px, Padding: 0px vertical, 28px horizontal. Used for 'Sign up now' in a compact form.

### Outline Ghost Button

**Role:** Subtle button with a transparent background.

Background: transparent, Text: Inkwell (#444340), Border: 1px solid Inkwell (#444340), Border Radius: 5px, Padding: 8px vertical, 14px horizontal. Used for 'Sign in' and other less prominent actions.

### Feature Card

**Role:** Card with white background for displaying features or content blocks.

Background: Book Page White (#ffffff), Border Radius: 5px, Padding: 20px on all sides. No shadow.

### Subtle Highlight Card

**Role:** Card with a light gray background for differentiating content blocks.

Background: Shelf Gray (#f8f8f8), Border Radius: 8px, No padding detected (content fills to edges).

### Search Input

**Role:** Input field for search functionality in the header.

Background: transparent, Text: Inkwell (#444340), Border: 1px solid Inkwell (#444340), Border Radius: 0px, Padding: 14px vertical, 43px left, 20px right.

## Layout

The page primarily uses a max-width contained model, with content centered within a clear horizontal constraint. The hero section features a centered headline over a white background, flanked by book covers as visual interest. Section rhythm is driven by consistent vertical spacing (133px section gap) often with alternating white (Book Page White #ffffff) and light gray (Shelf Gray #f8f8f8) background blocks. Content is arranged in alternating image-left/text-right or text-left/image-right patterns, as well as vertically stacked feature blocks and a 3-column card grid for testimonials. The navigation is a sticky top bar, containing the logo, search bar, and calls to action.

## Imagery

Literal primarily uses clean, contained product imagery—specifically, book cover art displayed as a visual grid or isolated. Imagery does not overlap or use complex masking; it adheres to a clear rectangular presentation. Icons are outlined, with a moderate stroke weight, and mostly monochrome (Inkwell #444340 or Parchment Gray #9a988b). The role of visuals is primarily decorative atmosphere (book covers) and explanatory content (product screenshots of the app UI), used to showcase the product's function rather than social proof or lifestyle. Density is moderate, balancing text-dominant sections with visual clusters.

## Dos & Donts

### Do

- Use Forest Green (#278458) exclusively for primary action backgrounds and brand-aligned interactive elements.
- Employ Libre Baskerville (500, 28px, 1.35 lineHeight) only for high-level page section headings to maintain its distinct character.
- Maintain a clear distinction between white (Book Page White #ffffff) and light gray (Shelf Gray #f8f8f8) backgrounds for different surface levels.
- Apply a 5px border radius to all filled buttons, and 8px to cards when a distinct corner is desired.
- Utilize Divider Gray (#eeeeee) for all subtle, unaccented borders and separators.
- Ensure primary text uses Inkwell (#444340) for high contrast and readability on light backgrounds.
- Use a minimum element gap of 20px where possible to maintain comfortable information density.

### Don't

- Do not use Forest Green (#278458) for decorative purposes or extensive text blocks; reserve it for functional actions.
- Avoid applying shadows extensively; surfaces in this system are predominantly flat, and card elevation is minimal or absent.
- Do not mix Libre Baskerville with body text; reserve it strictly for key headings to preserve its impact.
- Avoid introducing additional chromatic colors; the system relies on a single green accent within a neutral base.
- Do not vary border radius arbitrarily; stick to 5px for buttons and 8px for specific card types without exception.
- Avoid dense layouts; prioritize comfortable spacing, particularly the 20px element gap.
- Do not use highly decorative imagery; visuals should be clean, contained, and supportive of the reading/tracking theme.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #444340
background: #ffffff
border: #eeeeee
accent: #278458
primary action: #278458 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #278458 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a Feature Card: Background #ffffff, border-radius 5px, padding 20px on all sides. Headline 'Individual reading goals' in Inter 700, 22px, #000000. Body text 'Elizabeth Weber set a new goal' in Inter 400, 15px, #444340. Include a subtle border of Divider Gray #eeeeee.
3. Create a Primary Action Button in the header: 'Join' in Inter 700, 16px, Book Page White #ffffff. Background: Forest Green #278458. Border radius: 0px. Padding: 0px vertical, 28px horizontal.

---
_Source: https://styles.refero.design/style/7e698bf6-6b31-40ed-a89a-40f4c37ade38_
