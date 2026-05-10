# Gumroad — Design Reference

> High-contrast digital playground. A stark black and white digital canvas splashed with vivid, almost neon, color accents.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://gumroad.com](https://gumroad.com) |
| Refero page | [https://styles.refero.design/style/d67e1407-6d16-47e8-89cf-22f5c5f2dd88](https://styles.refero.design/style/d67e1407-6d16-47e8-89cf-22f5c5f2dd88) |
| Theme | light |
| Industry | ecommerce |

## Overview

This design system feels like a digital playground, blending a stark, utilitarian aesthetic with unexpected bursts of vibrant color. The liberal use of pure black and white creates a high-contrast canvas, punctuated by a signature hot pink and other vivid, almost neon, hues. Geometric illustrations and sharp angles are softened only by pill-shaped buttons and subtle rounded card edges, creating a playful yet focused environment for digital creators.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Black | `#000000` | neutral | Primary text, critical buttons, borders, and main icons — establishing a strong, authoritative presence. |
| Light Linen | `#ffffff` | neutral | Page backgrounds, card surfaces, and text on dark elements — provides a clean, extensive canvas. |
| Marketplace Gray | `#f4f4f0` | neutral | Subtle background for input fields and secondary content blocks, providing slight visual separation from pure white. |
| Graphite Border | `#242423` | neutral | Input borders, secondary text, and outline strokes — a slightly softer alternative to Pitch Black for structural elements. |
| Subtle Ash | `#d1d5dc` | neutral | Soft button borders, creating a barely-there outline for less prominent actions. |
| Creator Pink | `#ff90e8` | brand | Illustrative elements, decorative accents, and subtle background hints — injecting a signature playful and energetic brand identity. |
| Sunshine Yellow | `#ffc900` | accent | Used for occasional graphic accents and background elements, bringing warmth and vibrancy. |
| Lime Glow | `#f1f333` | accent | Secondary illustrative accents, signaling growth or fresh ideas. |
| Firecracker Orange | `#dc341` | accent | Tertiary illustrative accents, adding a dynamic, bold pop of color. |

## Typography

### ABC Favorit

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 14px, 16px, 18px, 20px, 24px, 30px, 36px, 48px, 72px, 96px, 192px |
| lineHeight | 0.90, 1.00, 1.11, 1.20, 1.25, 1.33, 1.38, 1.40, 1.43, 1.50, 1.56, 1.63 |
| letterSpacing | -0.0290em, -0.0250em, -0.0220em, -0.0200em, -0.0170em, -0.0130em, -0.0110em, -0.0080em, -0.0060em, -0.0040em, -0.0020em |
| fontFeatureSettings | "ss04", "ss11" |
| substitute | Inter, Arial |
| role | The primary typeface for all content. Its subtle geometric qualities and high x-height contribute to the system's clarity and modern feel. Varied line heights and negative letter-spacing for larger sizes prevent text from feeling too open, maintaining visual density. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.43 | -0.406 |
| body | 16 |  | 1.4 | -0.32 |
| subheading | 20 |  | 1.33 | -0.34 |
| heading | 36 |  | 1.25 | -0.396 |
| heading-lg | 72 |  | 1 | -1.44 |
| display | 96 |  | 0.9 | -1.92 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| buttons | 1.67772e+07px |
| default | 4px |
| largeElements | 24px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### Button Group

### Search Input Field

### Feature Cards

### Primary Black Button

**Role:** Critical CTAs

Solid Pitch Black background, Light Linen text, 4px border radius. Padding 0px top/bottom, 40px left/right. Uses ABC Favorit at 16px.

### Ghost Header Button

**Role:** Navigation links, secondary actions in headers

Transparent background, Pitch Black text, 1.67772e+07px (pill) border radius. Padding 8px top/bottom, 16px left/right. Uses ABC Favorit at 16px.

### Outline Pill Button

**Role:** Tertiary actions, filters, tags

White background, Pitch Black text, Subtle Ash border, 1.67772e+07px (pill) border radius. Padding 8px top/bottom, 16px left/right. Uses ABC Favorit at 16px.

### GitHub Link Button

**Role:** External links to code repositories

Transparent background, Pitch Black text, Subtle Ash border with 1.67772e+07px (pill) border radius. Internal padding 6px. Uses ABC Favorit at 14px.

### Search Input Field

**Role:** Primary search functionality

Marketplace Gray background, Pitch Black text, Graphite Border with 4px radius. Large horizontal padding 32px left/80px right (for icon). Uses ABC Favorit at 16px.

### Ghost Input Field (Dark)

**Role:** Placeholder for dark-themed inputs

Transparent background, Light Linen text at 30% opacity for placeholder, 4px radius. Padding 0px top/bottom, 16px left/right. Uses ABC Favorit at 16px.

## Layout

The page primarily follows a max-width contained layout, centering content within the viewport against a Light Linen background. The hero section features a centered, large headline and subtext, flanked by the signature floating coin illustrations, and is followed by a horizontal arrangement of primary and secondary CTA buttons. Subsequent sections appear to alternate between large, impactful type elements and more structured content blocks, likely arranged in multi-column grids or distinct feature sections. Vertical spacing is consistent and generous, creating a comfortable density.

## Imagery

The site uses flat, outlined illustrations with a clear geometric style. The key visual motif is a pastel pink coin with a 'G' emblazoned on it, often depicted in a dynamically floating, isometric perspective, adding a sense of movement and playfulness. These illustrations are decorative, establishing brand identity rather than explaining complex concepts. The overall density of imagery is moderate, primarily acting as background accents or small, focused product illustrations.

## Dos & Donts

### Do

- Prioritize Pitch Black (#000000) for all primary text and calls to action.
- Use Light Linen (#ffffff) as the default background for most page sections and card surfaces.
- Apply 1.67772e+07px (pill shape) border-radius only for buttons and small interactive elements like tags.
- Maintain ABC Favorit for all typography, leveraging its specific weights and letter-spacing values from the type scale.
- Utilize Creator Pink (#ff90e8) exclusively for brand iconography and illustrative elements, not for interactive states or text.
- Introduce Marketplace Gray (#f4f4f0) for subtle background differentiation in input fields or less prominent content sections.

### Don't

- Avoid using chromatic brand/accent colors for large blocks of text or primary UI elements; they are strictly for accents.
- Do not deviate from the specified ABC Favorit letter-spacing values, especially at larger type sizes, to preserve typographic character.
- Never introduce shadows as an elevation method; rely on color planes and borders for depth.
- Under no circumstances use default browser link styles; all links must adopt Pitch Black (#000000) or Light Linen (#ffffff) as appropriate contextually.
- Do not round corners to 24px if the element is not a larger, self-contained interactive block or card.
- Avoid mixing button styles; for primary actions, use the solid black button to maintain visual hierarchy.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): #000000
- Background (Page): #ffffff
- CTA (Primary): #000000
- Border (Input): #242423
- Accent (Brand): #ff90e8

### 3-5 Example Component Prompts
1. Create a primary call-to-action button: solid Pitch Black background, Light Linen text, 'Start selling' label. Use ABC Favorit, 16px, weight 400. Apply 4px border-radius and 40px horizontal padding. The button text should be 16px ABC Favorit, weight 400.
2. Design a header navigation link: transparent background, Pitch Black text, 'Discover' label. Use ABC Favorit, 16px, weight 400. Apply 1.67772e+07px (pill) border-radius and 8px vertical, 16px horizontal padding.
3. Implement a search input field: Marketplace Gray background, Graphite Border, 4px radius. Placeholder text 'Search marketplace...' in Pitch Black. Ensure 32px left padding and 80px right padding (for a search icon). Use ABC Favorit, 16px, weight 400.
4. Generate a floating decorative icon: Use the Creator Pink color (#ff90e8) for the background fill and Pitch Black (#000000) for the outline of the 'G' coin illustration. Ensure the coin has a distinct 3D isometric perspective, rotating slightly from horizontal.

---
_Source: https://styles.refero.design/style/d67e1407-6d16-47e8-89cf-22f5c5f2dd88_
