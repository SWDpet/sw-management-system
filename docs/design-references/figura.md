# Figura — Design Reference

> Midnight Command Console: high-contrast text on deep black surfaces, punctuated by precise interactions and measured spacing.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.figura.digital](https://www.figura.digital) |
| Refero page | [https://styles.refero.design/style/5e427a82-a223-4b69-be7f-01c6656ce823](https://styles.refero.design/style/5e427a82-a223-4b69-be7f-01c6656ce823) |
| Theme | dark |
| Industry | design |

## Overview

Figura employs a stark, high-contrast dark mode aesthetic, reminiscent of a command line interface or developer tool. The visual system prioritizes clarity and directness through a predominantly monochrome palette, precise typography, and a compact yet considered spacing system. Interaction is marked by subtle glowing effects and crisp outlines, creating a sense of understated confidence. The design feels sharp, functional, and devoid of superfluous decoration, focusing entirely on speed and outcome.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Canvas White | `#ffffff` | neutral | Primary text color, occasional borders, and the background for elements that flip the dominant dark mode contrast. Used sparingly to provide points of focus and readability |
| Medium Gray | `#8f8f8f` | neutral | Subtler text, helper text, and secondary borders. Offers a lower contrast than Canvas White against Absolute Zero, ideal for less prominent information |
| Surface Dark | `#1f1f1f` | neutral | Slightly elevated surface backgrounds, creating a subtle layer over the Absolute Zero canvas without introducing significant visual noise |
| Sky Glint | `#d4eaef` | accent | Subtle, ethereal gradient used for atmospheric background effects or abstract visual elements. A cooler blend of blues and greens |
| Ocean Calm | `#b3d5df` | accent | Softer, more cohesive background gradient, providing a gentle shift in tone. Used for large, less pronounced background areas |

## Typography

### Inter Display

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 14px, 15px, 16px, 18px, 20px, 22px, 32px, 36px |
| lineHeight | 1.22, 1.33, 1.36, 1.38, 1.40, 1.43, 1.50, 1.87 |
| letterSpacing | -0.0300em at 36px, -0.0200em at 32px, -0.0100em at 22px, 0.0200em for smaller text, 0.1000em for uppercase elements |
| substitute | Inter |
| role | Primary typeface for all headings and body text, establishing a modern, readable tone. The tight letter spacing for larger sizes adds to the brand's sharp and efficient feel. |

### Geist Mono

| Key | Value |
| --- | --- |
| weight | 500, 600 |
| weights | 500, 600 |
| sizes | 10px, 12px |
| lineHeight | 1.20, 1.33, 1.40 |
| letterSpacing | 0.0200em, 0.0400em |
| substitute | Fira Code |
| role | Monospace typeface primarily used for code snippets, metadata, or decorative, technical accents. Its presence hints at a precision-oriented, developer-friendly brand. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Arial |
| role | Fallback for system text and certain very small utility elements, ensuring readability across diverse environments. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.2 | 0.4 |
| heading | 18 |  | 1.43 | -0.36 |
| heading-lg | 20 |  | 1.38 | -0.4 |
| display-sm | 22 |  | 1.22 | -0.22 |
| display | 32 |  | 1.22 | -0.64 |
| display-lg | 36 |  | 1.22 | -1.08 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 200px |
| input | 8px |
| badges | 40px |
| buttons | 56px |
| default | 8px |
| largeElement | 32px |

- **elementGap** — 8px
- **sectionGap** — 96px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Ghost Border Button

**Role:** Action Element

Ghost button with an extremely generous border radius of 56px, an 8px vertical padding, 16px horizontal padding. Text color is white, background is transparent. Its outlined aesthetic creates a sense of interactive opportunity without visual dominance.

### Navigation Link

**Role:** Navigation Element

Standard navigation links use Canvas White text against Absolute Zero, 6px vertical padding, 10px horizontal padding, with a system sans-serif font at 12px weight 400. Text transform is uppercase, with a 72px border-radius, giving it a soft, pill-like background hover state.

## Layout

The page uses a full-bleed layout, primarily driven by the Absolute Zero background. Content is centrally stacked, featuring a prominent hero section with a centered headline and ghost buttons. Subsequent sections alternate between full-width black backgrounds and sections containing detailed product imagery. Vertical spacing between sections is generous, around 96px, but internal element spacing is compact with an 8px `elementGap`. There is no fixed max-width for the overall page, allowing content to stretch edge-to-edge. Navigation is a simple top bar with left-aligned branding and right-aligned text links leading to distinct pages.

## Imagery

This site features product screenshots of mobile applications, presented raw without masks or heavy embellishments, indicating a focus on functionality over stylized marketing. The images are contained within the white phone frames, effectively bringing lighter 'screen' content into the dark interface environment. There are no full-bleed marketing images or abstract illustrations. Iconography is assumed to be minimal and functional, not decorative. Imagery serves a direct explanatory or product showcase role, contributing to a text-dominant layout.

## Dos & Donts

### Do

- Prioritize high contrast: Canvas White (#ffffff) on Absolute Zero (#000000) for all primary text and backgrounds.
- Use Inter Display for all marketing and UI text, applying negative letter-spacing (-0.03em at 36px) to large headlines for a precisely crafted feel.
- Round corners generously for interactive elements: apply 56px radius to all buttons and a 40px radius to smaller, badge-like elements for a soft, approachable feel amidst the stark theme.
- Maintain a compact overall density by adhering to an 8px `elementGap` for stacking elements where breathing room is desired but space is conserved.
- Employ Geist Mono at 10px or 12px with a 0.04em letter-spacing for any technical, code-like, or metadata content.
- Use Medium Gray (#8f8f8f) for secondary, less prominent text, providing visual relief without breaking the monochrome palette.
- Apply Surface Dark (#1f1f1f) for subtle background shifts to indicate layered content or distinct sections, as opposed to heavy shadows or borders.

### Don't

- Avoid generic blue for interactive elements; there is no explicit blue primary action color – rely on ghost button styles and high-contrast text for interactivity.
- Do not introduce heavy shadows; the system relies on subtle background color shifts (`Surface Dark`) for layering, not elevation via drop shadows.
- Refrain from using excessively decorative typography or bright, saturated colors that are not part of the defined accent gradients.
- Avoid uneven spacing; adhere to the 4px base unit and established `elementGap` for internal component spacing to maintain consistency.
- Do not use generic, default corner radii; always reference the specified radii like 56px for buttons and 40px for badges to maintain brand distinctiveness.
- Do not use the primary sans-serif system font (Arial/Helvetica) for anything other than specific utility text as a last resort; default to Inter Display.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
text: #ffffff
background: #000000
border: #000000
accent: no distinct accent color
primary action: no distinct CTA color

**3-5 Example Component Prompts:**
1. Create a primary hero section: background Absolute Zero, with a display-lg headline in Canvas White. Below the headline, include a Ghost Border Button (Canvas White text, transparent background, #ffffff border of 1px, 56px radius, 8px vertical padding, 16px horizontal padding) labeled 'Book a call'.
2. Design a navigation bar: background Absolute Zero, with the brand logo on the left and a 'Manifesto' Navigation Link (Canvas White text, 12px sans-serif, 72px radius, 6px vertical padding, 10px horizontal padding) on the right.
3. Implement a section intro: background Surface Dark (#1f1f1f), centered heading-lg in Canvas White. Below, add a small, descriptive caption in Medium Gray (#8f8f8f) using body-sm text.

---
_Source: https://styles.refero.design/style/5e427a82-a223-4b69-be7f-01c6656ce823_
