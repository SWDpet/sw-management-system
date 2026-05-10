# Verse — Design Reference

> midnight command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.verse.sh](https://www.verse.sh) |
| Refero page | [https://styles.refero.design/style/486c3132-9ff7-4e27-9eef-ca2e130bd827](https://styles.refero.design/style/486c3132-9ff7-4e27-9eef-ca2e130bd827) |
| Theme | dark |
| Industry | design |

## Overview

Verse employs a stark, high-contrast dark mode aesthetic, reminiscent of a terminal interface or a hacker's workbench. The visual system is built on a foundation of pure black with minimal use of grays, allowing code-like typography and crisp borders to define the structure. Interactive elements are subtle, relying on text color changes and thin border lines rather than prominent fills. The overall impression is one of focused utility and technical precision, avoiding decorative flourishes.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space | `#171717` | neutral | Page background, surface backgrounds, UI borders |
| Cloud Gray | `#e5e7eb` | neutral | Decorative borders for content cards and subtle accents |
| Digital Silver | `#d4d4d4` | neutral | Primary text for headings, body, and links — provides strong contrast against Deep Space |
| Muted Ash | `#737373` | neutral | Secondary text, metadata, and subtle helper text, reducing visual hierarchy slightly |

## Typography

### Commit Mono

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| weights | 400, 600 |
| sizes | 11px, 14px, 16px |
| lineHeight | 1.43, 1.50 |
| letterSpacing | normal |
| substitute | IBM Plex Mono |
| role | The sole typeface, used across all elements from headings to body text and links. Its monospace nature reinforces the technical, code-centric aesthetic, with weight 600 providing subtle emphasis for titles without breaking the overall utilitarian feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 |  |
| body-sm | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 0px |
| buttons | 0px |

- **elementGap** — 16px
- **sectionGap** — 80px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Project Card

**Role:** Displays individual project entries with an image, title, and link.

A square or rectangular block with a 1px Cloud Gray border, containing an image or embedding. Text content uses Digital Silver. Background is Deep Space, with no padding or radius given the border is the primary visual separation. No discernible internal padding in the data so padding is 0.

### Ghost Button

**Role:** Interactive text or link, appearing within content blocks.

Text in Digital Silver (#d4d4d4) on a transparent background. Features a 1px Cloud Gray (#e5e7eb) border on hover or focus, defining interaction without a filled background. Border radius is 0px.

### Metadata Text

**Role:** Used for descriptive text like 'Twitter:' or project technologies.

Set in Commit Mono, Muted Ash (#737373) color, typically at 14px size. Used to provide secondary information without competing with primary content.

## Layout

The page uses a full-bleed black background from edge to edge. The main content is vertically stacked in a centered column, appearing as discrete blocks. The hero section features a centered headline and description. Subsequent sections are grid-based, primarily a 2-column or 3-column masonry-like grid for project showcases, with content cells having a 1px Cloud Gray border. Vertical rhythm is established by an 80px gap between these main content sections. Navigation is minimal, consisting of simple text links in the header area.

## Imagery

The site primarily uses product screenshots and embedded visual content to showcase work, rather than distinct branding imagery. When present, images maintain their original aspect ratios and coloring, embedded within card-like structures. There are no consistent icon styles or illustrative elements; visuals are content-driven, serving as direct examples of projects.

## Dos & Donts

### Do

- Use Deep Space (#171717) as the primary background for all surfaces and page canvas.
- Typography should exclusively use Commit Mono to maintain the consistent code-centric aesthetic, with weight 600 for subtle emphasis and 400 for all other body text.
- Render all borders, including those for interactive states and content cards, as 1px solid lines using Cloud Gray (#e5e7eb).
- Maintain a stark, no-radius aesthetic with all corners sharp at 0px radius for all components and containers.
- Employ Digital Silver (#d4d4d4) for all primary text content to ensure high contrast against the dark background.
- Utilize Muted Ash (#737373) for secondary text and metadata to create subtle hierarchy without introducing additional colors.
- Structure content with an 80px vertical section gap for clear separation between major content blocks.

### Don't

- Do not introduce any saturated colors for UI elements; color should be restricted to imagery or embedded content only.
- Avoid using any form of box-shadow or elevation as the design relies on flat, border-defined separation.
- Do not apply radius to any interactive elements, cards, or containers; all corners must be sharp 0px.
- Refrain from using prominent filled buttons or primary actions; all interactive elements should be styled as text links or ghost buttons.
- Do not deviate from the monochrome palette by introducing any chromatic text colors or background tints.
- Avoid decorative gradients or background patterns; surfaces should remain solid Deep Space (#171717).

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
text: #d4d4d4
background: #171717
border: #e5e7eb
accent: no distinct accent color
primary action: no distinct CTA color

**Example Component Prompts:**
1. Create a heading section: Centered text using Commit Mono weight 600, 16px, #d4d4d4, line-height 1.5. Subtext below it using Commit Mono weight 400, 14px, #737373, line-height 1.43, with an 80px bottom margin.
2. Create a Project Card component: 1px #e5e7eb border. Background #171717. No padding inside the card. Content title below the image should be Commit Mono weight 400, 14px, #d4d4d4. Links within the card in Commit Mono weight 400, 11px, #d4d4d4.
3. Create a Ghost Link: Text in Commit Mono weight 400, 14px, #d4d4d4, on a transparent background. When hovered or focused, show a 1px #e5e7eb border around the text area. No padding, no radius.

---
_Source: https://styles.refero.design/style/486c3132-9ff7-4e27-9eef-ca2e130bd827_
