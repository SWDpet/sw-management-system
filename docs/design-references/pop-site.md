# Pop Site — Design Reference

> Crisp canvas, bold headlines, electric blue

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://pop.site](https://pop.site) |
| Refero page | [https://styles.refero.design/style/e7d4a7de-aeaf-4d49-8c0c-0dedd05a8992](https://styles.refero.design/style/e7d4a7de-aeaf-4d49-8c0c-0dedd05a8992) |
| Theme | light |
| Industry | saas |

## Overview

Pop Site uses a crisp, high-contrast light-mode system, emphasizing strong typographic hierarchy and a singular vibrant blue accent. Wide spacing and soft-cornered cards establish an approachable feel, while the dominant dark text against pure white canvases ensures immediate clarity. The design prioritizes visual directness and ease of use through consistent control styling rather than heavy visual flair.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, primary surface elevation |
| Coal Black | `#000000` | neutral | Primary body text, bold headings, icon fills, strong borders |
| Ink Black | `#171717` | neutral | Secondary headings, prominent link text, strong borders; Decorative background gradient, typically for visual sections (linear) |
| Slate Gray | `#5e5e5e` | neutral | Muted body text, helper text, secondary borders |
| Ash Gray | `#6e6e73` | neutral | Subtle body text, footer text, hairline borders |
| Fog Gray | `#d2d2d7` | neutral | Subtle background tones, dividers |
| Action Blue | `#3b82f6` | brand | Primary call-to-action buttons, active states, brand links, interactive borders |
| Outline Blue | `#6ea5ff` | brand | Outlined link borders, secondary interactive accents |
| Eclipse Gradient | `#363836` | accent | Decorative background gradient with slight radial glow |
| Ember Gradient | `#c72b00` | accent | Decorative background gradient with warm radial glow |
| Sky Wash Gradient | `#e0e9ff` | accent | Soft background wash, typically within sections or as subtle visual interest |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### Satoshi

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| weights | 400, 500, 700 |
| sizes | 12px, 13px, 15px, 21px, 53px, 93px, 120px |
| lineHeight | 0.90, 1.00, 1.20, 1.30, 1.40 |
| letterSpacing | -0.0590em at 120px, -0.0500em at 93px, -0.0460em at 53px, -0.0300em at 21px |
| substitute | Inter |
| role | Primary brand typeface for headings and prominent text. Its compact, almost compressed letterforms and aggressive negative tracking create a modern, direct tone. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Inter |
| role | Functional typeface for input fields, ensuring legibility at smaller sizes and within interactive elements. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 | -0.03 |
| body | 15 |  | 1.2 |  |
| subheading | 21 |  | 1.3 | -0.03 |
| heading | 53 |  | 1 | -0.046 |
| heading-lg | 93 |  | 0.9 | -0.05 |
| display | 120 |  | 0.9 | -0.059 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 9999px |
| cards | 10px |
| image | 8px |
| buttons | 26px |
| default | 8px |

- **elementGap** — 20px
- **sectionGap** — 60px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Primary Action Button

**Role:** Filled call to action button

Background: Action Blue (#3b82f6), Text: Coal Black (#000000) (overridden by browser default link color in data, but visually white), Padding: 16px vertical, 24px horizontal. Corner radius: 26px for a soft pill shape.

### Ghost Input Segment Button

**Role:** Segmented control component part or input accessory

Ghost-style button. Background: Canvas White (#fafafc or transparent), Text: Coal Black (#000000), Border: 1px Coal Black (#000000). Specific radius for segmented left component: 0px top-left, 9999px top-right, 9999px bottom-right, 0px bottom-left. Padding is 16px vertical, 12px left, 20px right.

### Ghost Link Button

**Role:** Outlined or text-only button for secondary actions or navigation

Transparent background, Coal Black (#000000) text and border. No explicit padding in data, often used for text links or minimal interactive elements. Radius 0px.

### Content Card

**Role:** Container for features, testimonials, or content blocks

Background: Canvas White (#ffffff), Border radius: 10px. No box-shadow for a flat aesthetic. Padding: 20px on all sides.

### Full-Bleed Media Card

**Role:** Card container for imagery or full-bleed elements

Transparent background with a border radius of 20px. Serves to crop or frame content without adding an extra visual layer.

### Text Input (Base)

**Role:** Styling for text input fields

Transparent background, text color Coal Black (#000000), border color Coal Black (#000000). Radius 0px. No explicit padding mentioned in data, implies system default or inherited.

## Layout

The site employs a primarily max-width contained layout, likely around 1200px, with content centered. The hero section features a large, centered headline over a white background, flanked by call-to-action buttons. Sections below often alternate between full-width product imagery (specifically iPhone mockups) and content blocks with strong headings and body text. There is a consistent vertical rhythm with generous section gaps, suggesting distinct content areas. Feature sections frequently use a grid of cards (often 3-column), each with a specific image or illustration and descriptive text. Navigation is a simple top bar with text links.

## Imagery

The visual language for imagery is primarily product-focused, featuring tightly cropped device mockups (iPhones) displaying the Pop Site interface. These are typically rendered with soft, subtle depth and often feature contextual elements like hands or desks to indicate use. Photography, where present, appears to be clean and minimal, focused on individuals or teams. Iconography is minimalist, outlined, and monochromatic, often using Coal Black (#000000) against white backgrounds. The overall density is balanced, allowing UI elements and typography to dominate, with imagery serving to illustrate product functionality rather than create atmospheric mood.

## Dos & Donts

### Do

- Prioritize Satoshi for all headlines and prominent text, using its specific letter-spacing values to create a tight, impactful visual.
- Use Coal Black (#000000) for primary body text and most UI elements against Canvas White (#ffffff) backgrounds for maximum contrast.
- Reserve Action Blue (#3b82f6) exclusively for primary call-to-actions, active states, and brand-aligned interactive elements to maintain its impact.
- Apply a 10px border-radius for content cards and a 26px radius for primary buttons to maintain the brand's soft, approachable form language.
- Maintain generous 60px vertical section gaps to ensure a clear visual separation between content blocks.
- Keep card surfaces flat without shadows, relying on Canvas White (#ffffff) as the background and Coal Black (#000000) for text and borders to define content areas.
- Utilize transparent backgrounds for ghost buttons and input elements, defining them with high-contrast text and border colors like Coal Black (#000000).

### Don't

- Do not introduce additional vibrant colors; the system relies on a single dominant Action Blue (#3b82f6) for brand accents.
- Avoid heavy drop shadows or complex elevation states; the design prefers a clean, flat aesthetic for cards and surfaces.
- Do not use generic sans-serif fonts for headlines; Satoshi's distinctive letter-spacing is key to the brand's typographic identity.
- Do not vary border radii significantly for interactive elements, stick to 26px for primary buttons and 9999px for pill-shaped segments.
- Do not use subtle gray text (#5e5e5 or #6e6e73) for important calls to action; always use contrast-rich Coal Black (#000000) or Canvas White (#ffffff) against colored backgrounds.
- Do not create complex multi-color gradients for functional UI elements; the existing gradients are for decorative backgrounds only.
- Do not deviate from the established spacing units (based on 4px increments); maintain a comfortable, consistent density throughout the pages.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #000000
accent: #171717
primary action: #3b82f6 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #3b82f6 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a content card: background #ffffff, border-radius 10px, padding 20px on all sides. Headline: Satoshi weight 700, size 21px, color #171717. Body text: #000000, Satoshi weight 400, size 15px.
3. Implement a segmented input field: left segment is ghost button style with background transparent, text #000000, border #000000, border-radius 0px 9999px 9999px 0px, padding 16px vertical, 12px left, 20px right. Text: 'yourname'.
4. Create a hero headline: text 'Make a stunning site, easily', font Satoshi weight 700, size 93px, color #000000, line-height 0.9, letter-spacing -0.05em.

---
_Source: https://styles.refero.design/style/e7d4a7de-aeaf-4d49-8c0c-0dedd05a8992_
