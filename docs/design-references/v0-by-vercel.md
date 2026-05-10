# v0 by Vercel — Design Reference

> A Machinist's Blueprint. Precision and function are paramount, with every element serving a clear purpose on a clean, technical surface.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://v0.dev](https://v0.dev) |
| Refero page | [https://styles.refero.design/style/50aa2b8e-4760-4379-a3c1-59b65d8576a7](https://styles.refero.design/style/50aa2b8e-4760-4379-a3c1-59b65d8576a7) |
| Theme | light |
| Industry | ai |

## Overview

The design feels like a functional schematic on a stark white drafting table. Its nearly monochrome palette — #FFFFFF, #FAFAFA, #EAEAEA, #171717 — creates a utility-first atmosphere where user input is the only source of color. Typography is the main architectural element; a custom sans-serif is used everywhere, with tight negative letter-spacing at large sizes creating dense, impactful headlines. The UI is built from simple primitives: solid black CTAs with an 8px radius and subtly bordered white chips, distinguishing primary commands from secondary suggestions.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Paper White | `#ffffff` | neutral | Text on dark buttons, pill button backgrounds. |
| Canvas | `#fafafa` | neutral | Primary page background. |
| Line | `#eaeaea` | neutral | Borders for headers, ghost buttons, and dividers. |
| Subtext | `#666666` | neutral | Secondary text, navigation links, placeholder text. |
| Icon | `#7d7d7d` | neutral | Inactive icons and tertiary UI elements. |
| Ink | `#171717` | neutral | Primary text, headlines, and primary button backgrounds. |
| Onyx | `#000000` | neutral | Logo, icons, highest contrast text. |

## Typography

### GeistSans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 13px, 14px, 15px, 16px, 18px, 20px, 24px, 32px, 48px |
| lineHeight | 1.00, 1.17, 1.25, 1.30, 1.33, 1.43, 1.50, 1.56 |
| letterSpacing | Ranges from -2.88px at 48px to normal at 16px. The progressively tighter tracking on larger sizes is a key brand identifier. |
| fontFeatureSettings | "zero", "ss09", "ss05" |
| substitute | Inter |
| role | The universal font for all UI text, from body copy to display headings. Weight 600 is used for major headlines, 500 for buttons, and 400 for body text. Its signature is the aggressive negative letter-spacing at large sizes, creating dense, block-like headlines. |

### GeistMono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 10px |
| lineHeight | 1.50 |
| fontFeatureSettings | "zero" |
| substitute | IBM Plex Mono |
| role | Used for small, technical annotations or user statistics where tabular alignment is beneficial. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 |  |
| body-sm | 14 |  | 1.43 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.56 |  |
| heading-sm | 20 |  | 1.25 |  |
| heading | 24 |  | 1.33 | -0.48 |
| heading-lg | 32 |  | 1.17 | -1.28 |
| display | 48 |  | 1 | -2.88 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| pills | 9999px |
| inputs | 12px |
| buttons | 8px |

- **elementGap** — 8px
- **sectionGap** — 96px
- **cardPadding** — 16px
- **pageMaxWidth** — 1440px

## Components

### Main Prompt Input

### Template Filter Pills

### Button Group — Action Hierarchy

### Primary CTA Button

**Role:** The main action on a page, like 'Sign Up'.

Solid Ink (#171717) background with Paper White (#ffffff) text. Font is 14px GeistSans at 500 weight. Padding is ~8px vertically and 12px horizontally, with an 8px border radius.

### Ghost Navigation Link

**Role:** Secondary navigation actions like 'Sign In'.

Transparent background with Subtext (#666666) text. No border in its default state. Font is 14px GeistSans at 400 weight.

### Prompt Suggestion Chip

**Role:** Clickable suggestions below the main input.

Transparent background with a 1px Line (#eaeaea) border. Text is Subtext (#666666) at ~13px. Padding is 4px vertically and 8px horizontally, with a 6px border radius.

### Filter Pill Button

**Role:** Filtering content categories like 'Landing Pages'.

A pill-shaped button (9999px radius) with a Paper White (#ffffff) background and Ink (#171717) text. Features a faint 1px border of `rgba(0, 0, 0, 0.08)`.

### Main Prompt Input

**Role:** The primary interaction point for user queries.

A large input field with a 12px border radius and a subtle 1px Line (#eaeaea) border. Placeholder text is Subtext (#666666). On focus, it gets a subtle outer glow.

### Header Divider

**Role:** Separates the sticky header from page content.

A full-width 1px solid border using the Line color (#eaeaea).

## Layout

The layout is clean and centered within a generous max-width container (approx. 1440px). The header is full-width with a 1px bottom border. The hero area is a simple centered stack comprising a headline, a large input field, and suggestion chips. Body sections are separated by large vertical whitespace (~96px) on the Canvas (#fafafa) background, creating a spacious rhythm. Content grids, such as the 3-column template gallery, are the primary structure for displaying information.

## Imagery

This design uses no decorative imagery. Visuals are strictly confined to user-generated content previews within the Template Cards. These thumbnails are presented as raw, unstyled content inside a 12px rounded container. The page is UI-dominant, with imagery serving only to showcase product output, not to build atmosphere.

## Dos & Donts

### Do

- Use GeistSans for all text, without exception.
- Apply aggressive negative letter-spacing to headings 24px and larger.
- Adhere strictly to the achromatic palette; all color comes from content, not chrome.
- Use 8px radius for buttons and 12px for cards and inputs.
- Use 1px solid #eaeaea for all visual dividers.
- Differentiate action hierarchy using fills and borders: solid for primary, bordered for secondary, text-only for tertiary.
- Maintain generous whitespace (min. 96px) between content sections.

### Don't

- Do not introduce any saturated colors to the UI chrome.
- Do not use system fonts or other brand fonts.
- Do not use shadows on interactive elements like buttons or inputs; reserve them for cards.
- Do not use any border-radius values other than 6px, 8px, 12px, or 9999px (for pills).
- Do not use gradients or background images.
- Do not use bold (700+) font weights; rely on 600 weight and size for emphasis.
- Do not create dense layouts; prioritize clarity and space.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Page Background**: `#fafafa` (Canvas)
- **Primary Text**: `#171717` (Ink)
- **Subtle Text**: `#666666` (Subtext)
- **Border**: `#eaeaea` (Line)
- **CTA Background**: `#171717` (Ink)
- **CTA Text**: `#ffffff` (Paper White)

### Example Component Prompts
1. **Primary Button:** `Create a button with 'Get Started' text. It needs a #171717 background, #FFFFFF text, 8px corner radius, and font size 14px.`
2. **Display Headline:** `Generate a headline 'Start with a template'. Use GeistSans 32px weight 600, color #171717, and letter-spacing of -1.28px.`
3. **Template Card:** `Design a card container with a 12px border-radius, a white background, and a box-shadow of '0px 0px 0px 1px rgba(0,0,0,0.08), 0px 2px 1px 0px rgba(0,0,0,0.04)'.`

---
_Source: https://styles.refero.design/style/50aa2b8e-4760-4379-a3c1-59b65d8576a7_
