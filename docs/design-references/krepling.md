# Krepling — Design Reference

> Architectural fluid canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.krepling.com](https://www.krepling.com) |
| Refero page | [https://styles.refero.design/style/055f12af-b7b9-46af-81b7-93e0ed6d5ce2](https://styles.refero.design/style/055f12af-b7b9-46af-81b7-93e0ed6d5ce2) |
| Theme | light |
| Industry | ecommerce |

## Overview

Krepling employs a fluid, modern aesthetic with a spacious canvas and crisp typography. Its primary design elements are lightweight, often transparent surfaces and a singular vibrant purple accent for interactive elements. Imagery features abstract, almost architectural 3D forms, contrasting with the minimalist UI components. The system emphasizes clear hierarchy and ample whitespace, allowing content and subtle interactivity to take precedence.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | neutral | Page backgrounds, card surfaces, text on dark backgrounds |
| Midnight Text | `#000000` | neutral | Primary headings, body text, icon fills |
| Soft Gray | `#f1f1f1` | neutral | Secondary card surfaces, subtle background shifts |
| Muted Ash | `#6c6b6b` | neutral | Secondary text, muted borders, placeholder text |
| Steel Gray | `#b3b3b3` | neutral | Hairline borders, subtle dividers |
| Deep Midnight | `#171717` | neutral | Footer backgrounds, high-contrast dark surfaces |
| Krepling Violet | `#b154f9` | brand | Primary action buttons, interactive element borders, accent graphics |
| Rose Bloom | `#f9c2cf` | accent | Decorative card background |
| Luminous Grape | `#635bff` | accent | Decorative card background |
| Sunburst Yellow | `#ffe01b` | accent | Decorative card background |
| Deep Plum | `#4b154c` | accent | Decorative card background |
| Sky Blue | `#99e1f4` | accent | Decorative card background |
| Grass Green | `#93d33e` | accent | Decorative card background |
| Vivid Crimson | `#f22f46` | accent | Decorative card background |
| Ocean Teal | `#03363d` | accent | Decorative card background |
| Electric Blue | `#1ab4d7` | accent | Decorative card background |
| Goldenrod | `#ffc439` | accent | Decorative card background |
| Sunset Orange | `#ff7a59` | accent | Decorative card background |
| Fiery Red | `#e43225` | accent | Decorative card background |
| Gradient Dawn | `#f8d1c9` | accent | Decorative background gradient |
| Gradient Twilight | `#b78aff` | accent | Decorative background gradient |
| Gradient Sky | `#8dc8ff` | accent | Decorative background gradient |
| Gradient Horizon | `#a8dafa` | accent | Decorative background gradient |
| Gradient Cosmic | `#ccedff` | accent | Decorative background gradient |
| Gradient Nebula | `#77b5ff` | accent | Decorative background gradient |

## Typography

### Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 18px, 24px, 32px, 48px, 64px, 80px |
| lineHeight | 1.1, 1.2, 1.3 |
| letterSpacing | -0.01, 0.01, 0.02 |
| role | Regular — detected in extracted data but not described by AI |

### Inter

| Key | Value |
| --- | --- |
| letterSpacing | -0.1, 0.1, 0.2 |
| substitute | system-ui |
| role | Primary font for all text elements: headings, body copy, links, buttons, and navigation. Features subtle negative letter spacing at larger sizes and expanded tracking for smaller elements, creating both impact and legibility. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 16 |  | 1.3 | 0.1 |
| body | 18 |  | 1.3 | 0.1 |
| subheading | 24 |  | 1.2 | -0.1 |
| heading | 32 |  | 1.2 | -0.1 |
| heading-lg | 48 |  | 1.1 | -0.1 |
| display | 64 |  | 1.1 | -0.1 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 14px |
| large | 30px |
| buttons | 10px |
| default | 8px |

- **elementGap** — 8px
- **sectionGap** — 32px
- **cardPadding** — 20px
- **pageMaxWidth** — 733px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | 0 | Primary page background and default surface for most content. |
| Soft Gray | `#f1f1f1` | 1 | Secondary background for sections or subtle elevation of content blocks. |
| Deep Midnight | `#171717` | 2 | Used for specific high-contrast areas like footers. |

## Components

### Primary Action Button

**Role:** Call-to-action button for critical actions

Filled with Krepling Violet (#b154f9), white text (#ffffff), and a 10px border radius. Padding is 14px vertical, 16px horizontal.

### Ghost Button

**Role:** Secondary or textual actions within a flow

Transparent background, Midnight Text (#000000) for text, 0px border radius, 20px horizontal padding, 48px vertical padding. No distinct border color.

### White Card

**Role:** Content container for features or information blocks

White Canvas (#ffffff) background, 14px border radius. Padding is 0px. No box shadow.

### FAQ Accordion Item

**Role:** Interactive list item for expandable content

Transparent background, 0px border radius. No distinct padding, uses page text styling with subtle indicators for interactivity. The surrounding container uses White Canvas and 14px radius.

### Navigation Link

**Role:** Interactive elements within the main navigation

Primary text color (#000000), no background; padding 12px vertical, 16px horizontal. Underlined on hover or active.

### Workflow Nav Item

**Role:** Contextual navigation within a dashboard or feature section

Default is transparent background with Midnight Text (#000000). Active state uses Soft Gray (#f1f1f1) background with Midnight Text (#000000), 8px border radius. Padding is 14px vertical, 16px horizontal.

## Layout

The site employs a max-width contained layout set at 733px, with content predominantly centered. The hero section features a unique composition with large abstract 3D elements that break the bounds of the content area, creating a full-bleed visual effect while the text remains centered. Sections generally follow a consistent vertical rhythm with a 32px `sectionGap`. Content is arranged in alternating patterns, often text on one side and a large abstract visual element on the other, or stacked centered blocks. There's an evident card grid structure for feature display. Navigation consists of a clear top bar with primary links and a 'Get Started' button, which remains sticky at the top.

## Imagery

The visual language features abstract 3D forms, often appearing as frosted or translucent pipes and floating amorphous shapes. These elements interact with colorful, icon-like product 'cards' that have rounded corners. Photography is absent; instead, the site uses expressive, somewhat whimsical illustrations that are dimensional and organic, often rendered with soft gradients. Icons are filled, with a consistent, heavier stroke appearance, and are often monochromatic but sometimes incorporate subtle color. Imagery serves a decorative, atmospheric role, creating a sense of dynamic flow and modern capability rather than direct product showcase or informational utility. The overall density of imagery is balanced, with large-scale graphics coexisting with text-dominant sections, giving significant visual space to these abstract elements.

## Dos & Donts

### Do

- Use Midnight Text (#000000) for all primary body copy and headings on White Canvas (#ffffff).
- Apply Krepling Violet (#b154f9) exclusively for primary interactive elements and brand accents.
- Maintain a clear visual hierarchy by utilizing the spacious 32px `sectionGap` between primary content blocks.
- Ensure all card and surface elements use a 14px border radius for visual consistency across content containers.
- Utilize Ghost Buttons for all actions that are not the primary call-to-action, maintaining transparency and standard text coloring.
- Employ the `Inter` font family at 400 weight for all text; adjust letter spacing according to `letterSpacing` values based on type scale role.
- For decorative elements (cards or UI graphics), leverage the accent color palette and gradients to add visual interest, always contained within distinct shapes.

### Don't

- Do not introduce new saturated colors outside of the defined accent palette; rely on the existing blues, reds, yellows, and purples as decorative elements.
- Avoid using box shadows; the design system relies on flat surfaces and color changes for depth and distinction.
- Do not deviate from the 733px `pageMaxWidth` for primary content columns; content should always be centered within this constraint.
- Refrain from using strong borders for interactive elements; most borders are subtle hairline (`#b3b3b3`) or defined by the Krepling Violet accent.
- Do not clutter components with excessive padding; adhere to the specified `cardPadding` of 20px and element-specific padding values.
- Avoid arbitrary changes to font weights or styles; only the 400 weight of Inter is used, with size and letter spacing variations for hierarchy.
- Do not use dark backgrounds globally; restrict them to specific sections like the footer or contained expressive elements.

## Notes

### Agent Prompt Guide

primary action: #b154f9 (filled action)
Create a Primary Action Button: #b154f9 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Example Component Prompts:
1. Create a Primary Action Button: Krepling Violet background (#b154f9), White Canvas text (#ffffff), Inter 400 at 16px, 10px radius, 14px vertical and 16px horizontal padding.
2. Create a White Card: White Canvas background (#ffffff), 14px radius, 0px padding. Inside, place a 'subheading' in Midnight Text (#000000) at 24px Inter 400 and a 'body' text in Midnight Text (#000000) at 18px Inter 400.
3. Create a Ghost Button: Transparent background, Midnight Text (#000000), Inter 400 at 16px, 0px radius, 48px vertical and 20px horizontal padding.
4. Create a Navigation Link: Midnight Text (#000000), Inter 400 at 16px, 12px vertical and 16px horizontal padding, no background, 8px radius.

---
_Source: https://styles.refero.design/style/055f12af-b7b9-46af-81b7-93e0ed6d5ce2_
