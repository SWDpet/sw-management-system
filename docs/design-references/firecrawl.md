# Firecrawl — Design Reference

> Whiteboard blueprints, with a single neon 'active' indicator.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.firecrawl.dev](https://www.firecrawl.dev) |
| Refero page | [https://styles.refero.design/style/78fec83e-4b27-44ab-9f64-31e9dee53e46](https://styles.refero.design/style/78fec83e-4b27-44ab-9f64-31e9dee53e46) |
| Theme | light |
| Industry | ai |

## Overview

Firecrawl employs a crisp, data-centric interface with a strong emphasis on clean surfaces and a single vibrant accent color. Typography is confident and precise, prioritizing legibility and structure over decorative flair. Components are lightweight and interaction focused, using subtle borders and shadows to define hierarchy rather than heavy fills. The overall impression is one of efficiency and directness, designed for developers engaging with AI tools.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Fire Orange | `#ff4d00` | brand | Orange action color for filled buttons, selected navigation states, and focused conversion moments |
| Code Blue | `#006fff` | accent | Code snippet syntax highlighting and informational text, offering a distinct visual cue for technical content |
| Cloud Canvas | `#e5e7eb` | neutral | Dominant background for the page, setting a light, open tone. Also used for neutral borders, ensuring divisions are subtle |
| Ink Black | `#262626` | neutral | Secondary body text, navigation labels, and subdued headings. Do not promote it to the primary CTA color |
| Paper White | `#f9f9f9` | neutral | Card surfaces and elevated elements, creating a subtle lift from the main canvas without harsh contrast |
| Slate Gray | `#727272` | neutral | Secondary text, disabled states, and subtle icon detailing, providing a softer visual presence than primary text |
| Stone Gray | `#616161` | neutral | Muted link text and less prominent body copy, contributing to visual hierarchy |
| Silver Mist | `#949494` | neutral | Helper text, placeholders, and subtle strokes in icons, receding into the background for less critical information |
| Frost Gray | `#c7c7c7` | neutral | Decorative lines, very faint borders, and subtle separating elements |
| Pale Sienna | `#fcddcc` | neutral | Gray supporting accent for decorative details and low-frequency emphasis. Do not promote it to the primary CTA color |
| Powder Pink | `#febec2` | neutral | Accent in subtle shadow layers, providing a soft, almost imperceptible warmth |

## Typography

### suisse

| Key | Value |
| --- | --- |
| weight | 400, 450, 500 |
| weights | 400, 450, 500 |
| sizes | 10px, 12px, 13px, 14px, 15px, 16px, 20px, 24px, 40px, 52px, 60px |
| lineHeight | 1.00, 1.07, 1.08, 1.10, 1.20, 1.33, 1.40, 1.43, 1.50, 1.54, 1.60, 1.67 |
| letterSpacing | -0.0100em at 60px, -0.0050em at 52px, 0.0100em at 10-15px, 0.0200em at 14px |
| substitute | Inter |
| role | The primary typeface for all interface text, from headings to body copy, offering a clean and structured appearance. Its range of weights and optical adjustments for larger sizes ensure flexibility and impact. |

### GeistMono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 13px, 14px |
| lineHeight | 1.33, 1.54, 1.57 |
| letterSpacing | normal |
| substitute | SF Mono |
| role | Used for code snippets and technical examples, providing a clear, fixed-width presentation that distinguishes it from other interface text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.4 | 0.1 |
| body | 14 |  | 1.54 |  |
| body-lg | 16 |  | 1.6 |  |
| subheading | 20 |  | 1.43 |  |
| heading | 24 |  | 1.33 |  |
| heading-lg | 40 |  | 1.1 |  |
| display | 52 |  | 1.07 | -0.52 |
| display-lg | 60 |  | 1 | -0.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 16px |
| inputs | 8px |
| buttons | 999px |
| menuItems | 8px |

- **elementGap** — 12px
- **sectionGap** — 164px
- **cardPadding** — 16px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#e5e7eb` | 0 | The primary background for the entire page, providing a consistent, light base. |
| Card Base | `#f9f9f9` | 1 | Used for standard card backgrounds and slightly elevated sections, creating gentle separation from the canvas. |
| Elevated Card | `#ffffff` | 2 | Applied to more prominent interactive cards or panels, enhanced with shadows for distinct layering. |

## Components

### Primary Action Button

**Role:** Calls to action, signup, and key interactive elements.

Filled with Fire Orange (#ff4d00), white text, fully rounded (999px radius), with padding varied by context but commonly 12px vertical, 24px horizontal.

### Ghost Button

**Role:** Secondary actions that need less emphasis, often next to a primary button.

Transparent background, Ink Black (#262626) text, 999px radius, 12px vertical, 24px horizontal padding, with a subtle Cloud Canvas (#e5e7eb) border upon interaction or focus.

### Navigation Link Button

**Role:** Navigation items in headers and footers.

Transparent background, Ink Black (#262626) text at rgba(0, 0, 0, 0.56) for inactive, 0px radius, 6px padding.

### Feature Card

**Role:** Showcasing product features or benefits.

Paper White (#f9f9f9) background, 16px border-radius, with a subtle multi-layer shadow effect for elevation: rgba(0, 0, 0, 0.02) 0px 40px 48px -20px, rgba(0, 0, 0, 0.03) 0px 32px 32px -20px, rgba(0, 0, 0, 0.03) 0px 16px 24px -12px, rgba(0, 0, 0, 0.03) 0px 0px 0px 1px.

### Hero Input Field

**Role:** Main input for search/URL, prominent on hero section.

Transparent background, Ink Black (#262626) text, Cloud Canvas (#e5e7eb) border, no explicit border-radius (default 0px is observed), but contained within a larger card that provides overall curvature.

### Code Block Card

**Role:** Displaying code examples, with syntax highlighting.

Paper White (#f9f9f9) background, 20px border-radius, with a comprehensive multi-layer shadow stack (including a 10px rgb(249, 249, 249) inner shadow) for significant elevation and visual separation.

## Layout

The page maintains a centered, max-width layout, approximately 1200px wide, with generous horizontal padding. The hero section features a centered headline and a large input with call-to-action, set against the Cloud Canvas background. Sections are separated by consistent vertical spacing, often 164px, with subtle borders or faint background shifts. Content is arranged in alternating single-column stacks and two-to-three column card grids for features and examples. The navigation is a sticky top bar with clearly defined links and a prominent Fire Orange primary action button.

## Imagery

The visual language focuses on minimalist product-centric graphics. Photography is absent. Illustrations are stylized, flat, and often abstract, emphasizing geometric forms and subtle outlines. Icons are outlined, conveying functionality without visual weight. Product screenshots or code blocks are presented within clean card elements. Imagery serves to explain content or showcase product interfaces rather than for decorative atmosphere. The overall density is text-dominant, with graphics used sparingly and purposefully.

## Dos & Donts

### Do

- Always use Fire Orange (#ff4d00) for primary interactive elements and key brand callouts.
- Maintain a clear visual hierarchy using Ink Black (#262626) for primary text and Slate Gray (#727272) or Stone Gray (#616161) for secondary text.
- Apply Cloud Canvas (#e5e7eb) as the page background and Paper White (#f9f9f9) for elevated card surfaces.
- Use Suisse typeface for all UI text, selecting weights 400 for body, 450 for subheadings, and 500 for main headings.
- Round all buttons and tags with a 999px border-radius for distinct pill-shaped components.
- Utilize a 1px Cloud Canvas (#e5e7eb) border for subtle visual separation of elements and boundaries.
- Implement the multi-layer shadow for Feature Cards to create a feeling of subtle elevation and depth.

### Don't

- Avoid introducing additional saturated colors; maintain the Fire Orange (#ff4d00) as the sole vibrant accent for consistency.
- Do not use heavy, opaque backgrounds for cards or panels; leverage transparency and subtle shadows for layering.
- Never compromise text contrast; ensure all text meets AAA accessibility standards against its background.
- Do not use generic square buttons; all actionable buttons should have a 999px border radius.
- Avoid excessively ornate or pictorial imagery; prefer simple icons, product mockups, or abstract data visualizations.
- Do not use arbitrary spacing; adhere to the 4px base unit and established element, card, and section gaps.
- Avoid bolding or italicizing large blocks of text; rely on font weight and size hierarchy with the Suisse typeface.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #262626
background: #e5e7eb
border: #e5e7eb
accent: #ff4d00
primary action: #ff4d00 (filled action)

Example Component Prompts:
1. Create a Primary Action Button labeled 'Sign up': Fire Orange (#ff4d00) background, white text, suisse weight 450 at 16px, 999px radius, padding 12px vertical, 24px horizontal.
2. Design a Feature Card with title 'Search': Paper White (#f9f9f9) background, 16px radius, Ink Black (#262626) title in suisse weight 500 at 20px, Slate Gray (#727272) body text in suisse weight 400 at 14px, and the multi-layer shadow effect.
3. Build a Ghost Button labeled 'Playground': Transparent background, Ink Black (#262626) text in suisse weight 450 at 16px, 999px radius, 12px vertical, 24px horizontal padding, with a 1px Cloud Canvas (#e5e7eb) border.
4. Construct a Code Block Card: Paper White (#f9f9f9) background, 20px radius, GeistMono weight 400 at 14px for code text, with a Code Blue (#006fff) syntax highlight, and the full multi-layer shadow stack including the rgb(249, 249, 249) 0px 0px 0px 10px inner shadow.

---
_Source: https://styles.refero.design/style/78fec83e-4b27-44ab-9f64-31e9dee53e46_
