# Tana — Design Reference

> infinite dark canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://tana.inc](https://tana.inc) |
| Refero page | [https://styles.refero.design/style/f7bf0618-817c-4b7d-9568-cbd9c476c599](https://styles.refero.design/style/f7bf0618-817c-4b7d-9568-cbd9c476c599) |
| Theme | dark |
| Industry | productivity |

## Overview

Tana cultivates a focused, dark-canvas experience where content takes center stage, punctuated by a single soft green accent for interaction. The design emphasizes clear information hierarchy through careful typographic choices and ample negative space. Components are lightweight with subtle borders that emerge from the dark background, maintaining a high contrast and serious tone despite the single approachable accent color. The overall impression is one of calm, concentrated work.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Midnight | `#000000` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Cloud Whisper | `#f0eded` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Graphite Base | `#0e0e0e` | neutral | Secondary background, subtle surface separation, borders |
| Muted Silver | `#b3b3b3` | neutral | Secondary text for body copy and supporting information, list item borders |
| Stone Gray | `#808080` | neutral | Tertiary text for citations and fine print, subtle decorative borders |
| Soft Ash | `#606060` | neutral | Lowest contrast text for disclaimers and meta-information |
| Lush Meadow | `#e1f0bd` | accent | Primary action button background, interactive highlights — a soft, muted green that signals progression without harshness |

## Typography

### SF Pro

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 13px, 15px, 16px, 17px, 18px, 19px, 20px, 21px, 24px, 26px, 29px |
| lineHeight | 1.20, 1.40, 1.50, 1.75 |
| letterSpacing | 0.0100em |
| substitute | system-ui |
| role | General body text, navigation elements, button labels, and secondary headings. Its consistent, legible structure supports dense information on a dark background. |

### tanaClassic

| Key | Value |
| --- | --- |
| weight | 350, 400 |
| weights | 350, 400 |
| sizes | 17px, 19px, 38px, 42px, 48px, 84px |
| lineHeight | 1.10, 1.20, 1.25, 1.75 |
| letterSpacing | -0.0450em at 84px, -0.0300em at 48px |
| substitute | serif |
| role | Display headlines and prominent section titles. The custom serif typeface provides a literary, classic feel, while the tight negative letter-spacing for larger sizes enhances its distinctiveness and compact impact. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 17px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | system-ui |
| role | Specific button text, providing a highly legible and functional complement to the custom headline font. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.4 |  |
| body-sm | 15 |  | 1.4 |  |
| subheading | 20 |  | 1.25 |  |
| heading-sm | 24 |  | 1.25 |  |
| heading | 48 |  | 1.1 | -0.96 |
| display | 84 |  | 1.1 | -3.78 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 8px |
| image | 10px |
| buttons | 12px |

- **elementGap** — 6px
- **sectionGap** — 24px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Midnight Canvas | `#000000` | 0 | Dominant page and module background. |
| Graphite Base Layer | `#0e0e0` | 1 | Secondary background for containers or subtle section variations, borders. |

## Components

### Primary Action Button

**Role:** Call to action

Filled button with a muted green background (Lush Meadow, #e1f0bd) and black text (Deep Midnight, #000000). Has a generous 12px border radius. Padding is 10px vertical, 16px horizontal.

### Ghost Outline Button (small)

**Role:** Secondary action or tag

Transparent background with a Cloud Whisper (#f0eded) border and text. Has an 8px border radius. Padding is 6px vertical, 12px horizontal.

### Ghost Text Link (minimal)

**Role:** Inline or tertiary action link

Transparent background, no visible border, with Cloud Whisper (#f0eded) text. No explicit radius or padding, blends seamlessly into copy or navigation.

### Navigation Link

**Role:** Main navigation item

Plain text link using SF Pro, Cloud Whisper (#f0eded) color, appearing primarily in headers and footers without explicit borders or background. Uses a 16px padding on the left, right of specific items.

### Hero Headline

**Role:** Prominent page title

Uses tanaClassic font, typically at 84px or 48px, with weights 350-400, and a tight letter-spacing of -0.045em to -0.03em. Color is Cloud Whisper (#f0eded) against a Deep Midnight background.

### Feature Tag Bubble

**Role:** Informational tag or categorized item

Small, rounded bubbles with 8px radius, transparent background, and a Cloud Whisper (#f0eded) border. Text color is Cloud Whisper. Padding is 6px vertical, 12px horizontal.

## Layout

The page maintains a full-bleed dark background (Deep Midnight) throughout, with content centrally aligned and implicitly max-width contained by ample horizontal padding. The hero section features a centered headline and action buttons. Subsequent sections alternate between centered text blocks and asymmetric text-left/image-right (or vice-versa) layouts. A grid of abstract 'bubbled' tags illustrates connectivity. Vertical rhythm is established by consistent section gaps of 24px creating a comfortable, unhurried density. Navigation is a minimal top-bar, visually integrated with the dark background.

## Imagery

The site uses a mix of candid, dark-toned photography for humans in work settings and abstract graphical bubbles (like speech bubbles or connected nodes) to highlight concepts in the 'knowledge graph' section. Photography is generally desaturated, focusing on the human element and product interaction rather than vibrant colors. Icons are minimal, outlined, and monochromatic (Cloud Whisper) against the dark background. Imagery is primarily explanatory and decorative, occupying significant visual space to break up text-heavy sections, but always contained within the flow rather than full-bleed.

## Dos & Donts

### Do

- Prioritize Deep Midnight (#000000) for all primary background surfaces to maintain the dark canvas theme.
- Use Cloud Whisper (#f0eded) as the default text color for primary content and interactive elements against dark backgrounds.
- Apply Lush Meadow (#e1f0bd) exclusively for primary action button backgrounds and clear calls-to-action.
- Maintain a clear visual hierarchy using tanaClassic for display headings with tight letter-spacing, and SF Pro for all body text and UI elements.
- Utilize 12px border radius for all primary buttons and 8px for smaller interactive tags or ghost buttons.
- Employ a 10px vertical and 16px horizontal padding for main action buttons.
- Use Muted Silver (#b3b3b3) for secondary body copy and supporting text against Deep Midnight (#000000).

### Don't

- Avoid using bright, saturated chromatic colors outside of the defined Lush Meadow accent for primary actions.
- Do not introduce strong shadows or excessive elevation that would contradict the flat, minimalist surface treatment.
- Minimize the use of multiple border radii; stick to 12px for primary buttons, 8px for tags, and 10px for images.
- Do not apply broad letter-spacing to headings; use the specified negative tracking for tanaClassic to maintain its distinct character.
- Refrain from using background patterns or gradients, as the design relies on solid, high-contrast dark surfaces.
- Do not use SF Pro for display-level headlines; reserve tanaClassic for this role to preserve typographic identity.
- Avoid using Stone Gray (#808080) for essential body text as its contrast is intended for tertiary information only.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #f0eded
background: #000000
border: #f0eded
accent: #e1f0bd
primary action: #e1f0bd (filled action)

### 3-5 Example Component Prompts
1. Create a Primary Action Button: #e1f0bd background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a feature block: background Deep Midnight (#000000). Heading 'Tana turns meetings into' with tanaClassic, 42px, weight 400, Cloud Whisper (#f0eded), letter-spacing -1.26px. Below, a photo of a person working, 10px border-radius. Flanking the photo, 'Feature Tag Bubble' components, e.g., 'Prototypes' and 'Storyboards'.
3. Create a testimonial section: background Deep Midnight (#000000). Quotes in Muted Silver (#b3b3b3), SF Pro 20px, lineHeight 1.5. Author/source in Stone Gray (#808080), SF Pro 17px, lineHeight 1.5. Each quote block is separated by 24px sectionGap.

---
_Source: https://styles.refero.design/style/f7bf0618-817c-4b7d-9568-cbd9c476c599_
