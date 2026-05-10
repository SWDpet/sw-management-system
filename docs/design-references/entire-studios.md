# entire studios — Design Reference

> Gallery White Box – stark, unadorned surfaces presenting content with minimalist precision.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://entirestudios.com](https://entirestudios.com) |
| Refero page | [https://styles.refero.design/style/cef25151-078c-4631-8f02-4f204f071b8b](https://styles.refero.design/style/cef25151-078c-4631-8f02-4f204f071b8b) |
| Theme | light |
| Industry | ecommerce |

## Overview

This system feels like a minimalist gallery space, emphasizing raw visual impact and an almost ascetic reduction of UI ornamentation. The stark monochromatic palette of deep black and pale slate, punctuated only by occasional bright white elements, directs focus entirely to the content. Type is exclusively monospaced, lending a technical, almost archival quality, while the complete absence of rounded corners or shadows reinforces a sharp, uncompromising aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary text, icon fill, button borders — defining the core contrasts and visual anchors of the UI. |
| Pale Slate | `#e7ecea` | neutral | Section backgrounds and subtle dividers, providing a minimal softening against pure white or black. |
| Arctic White | `#ffffff` | neutral | Page backgrounds and sometimes as text for maximum contrast against dark elements. |

## Typography

### Space Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 16px |
| lineHeight | 1.33, 1.50 |
| letterSpacing | normal |
| substitute | Space Mono |
| role | All textual elements: body text, links, buttons, headers. Its monospaced, fixed-width nature is a signature choice, lending a precise, technical, and almost retro-digital atmosphere to all copy. It dictates a structured, grid-like rhythm even in paragraphs. |

## Spacing

### radius

| Key | Value |
| --- | --- |
| all | 0px |

- **elementGap** — 6px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Announcement Banner

### Ghost Navigation Button Group

### Product Card Grid

### Ghost Navigation Button

**Role:** Interactive element (e.g. navigation, filters)

Transparent background, Midnight Ink text and border, 0px border-radius. Padding: 0px top/bottom, 6px left/right. Font: Space Mono 12px, weight 400, normal letter spacing. This button is designed to be minimal to not distract from content.

## Layout

The page maintains a full-bleed structure without a fixed `pageMaxWidth` container, allowing imagery to extend edge-to-edge. The hero section features a centered, large headline over a full-viewport background image. Content sections appear to flow with consistent vertical spacing, creating an airy, gallery-like feel. Navigation is a minimal top bar with left-aligned 'shop' and right-aligned utility links, all in small, uppercase Space Mono. The overall impression is information-dense but visually sparse, driven by strong photography and precise typography.

## Imagery

Imagery consists primarily of high-fashion and editorial photography, either full-bleed as main hero content or tightly integrated with text. The treatment is raw and untamed, with a distinct absence of masks, overlays, or structured framing. Photography drives the mood and ambiance, acting as large, immersive backdrops or focal points, rather than contained decorative elements. Photography is dominant, often full-screen, making the site feel image-heavy despite minimalist UI elements. No specific icons are visible in the provided data beyond generic UI elements.

## Dos & Donts

### Do

- Maintain a strictly monochromatic palette, primarily utilizing Midnight Ink (#000000), Pale Slate (#e7ecea), and Arctic White (#ffffff).
- Use 'Space Mono' exclusively for all typography at 12px or 16px with respective line heights.
- Apply 0px border-radius to all elements, maintaining sharp, clean edges.
- Utilize 6px as the base unit for horizontal padding and gaps between small inline elements.
- Prioritize text-based UI elements over graphical icons for navigation and actions.

### Don't

- Avoid using any colors outside the defined monochromatic palette for UI elements.
- Do not introduce any rounded corners or soft edges on buttons, cards, or other interactive components.
- Refrain from using drop shadows or complex elevation effects; depth is created through color contrast and plane changes.
- Do not deviate from 'Space Mono' as the sole typeface. No sans-serif or serif fonts.
- Avoid decorative imagery; content visuals should be functional product shots or abstract textural backgrounds.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #000000 (Midnight Ink)
- Background (Primary): #ffffff (Arctic White)
- Background (Secondary): #e7ecea (Pale Slate)
- Button Border: #000000 (Midnight Ink)

### 3-5 Example Component Prompts
1. Create a hero section: Full-bleed background image. Centered headline 'entire studios' at 16px Space Mono, weight 400, color #000000. Display a transparent ghost navigation button 'Shop' at the top left and 'Search', 'EUR', 'Bag (0)' on the top right, all text #000000, Space Mono 12px, weight 400, with 0px padding-top/bottom and 6px padding-left/right.
2. Create a product listing card: Arctic White background. Image takes 70% height at top, 0px radius. Below image, product name 'Product X' Space Mono 16px weight 400, color #000000, padding 16px. Price '€XXX' Space Mono 12px weight 400, color #000000, padding 16px. No border or shadow, 0px radius for card.
3. Create a secondary information section: Pale Slate background. Centered title 'About Us' Space Mono 16px weight 400, color #000000. Body text below, Space Mono 12px weight 400, color #000000. All text elements vertically spaced by 16px, all elements have 0px radius and 0px padding.

---
_Source: https://styles.refero.design/style/cef25151-078c-4631-8f02-4f204f071b8b_
