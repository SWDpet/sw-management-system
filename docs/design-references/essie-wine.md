# Essie Wine — Design Reference

> Vintage Bookplate Serenity — a quiet, reserved aesthetic like an aged paper with delicate script.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.essiewine.com](https://www.essiewine.com) |
| Refero page | [https://styles.refero.design/style/07f5281d-2a18-4e12-a8ff-d54d3e03d198](https://styles.refero.design/style/07f5281d-2a18-4e12-a8ff-d54d3e03d198) |
| Theme | light |
| Industry | other |

## Overview

Essie Wine cultivates a classic, understated visual identity through a sparse palette of off-whites, murky greens, and a singular, muted ochre. Its spacious, text-dominant layout employs a traditional serif for its primary messaging, harmoniously paired with a legible sans-serif for functional text. Components are minimalist, often appearing as subtle underlines or text-only elements, reflecting a design ethos of quiet sophistication rather than overt display. The overall aesthetic feels like a well-worn book or a vintage poster—timeless and unpretentious.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Inkwell | `#062d32` | brand | Primary text, headers, navigation links, input borders, ghost button borders. This dark, desaturated teal establishes a grounded, vintage feel |
| Parchment | `#e9e9e2` | neutral | Canvas background for primary content sections, subtle text elements |
| White Linen | `#ffffff` | neutral | Page backgrounds in select sections, pristine component surfaces |
| Old Gold | `#aa9e54` | accent | Background for specific sections like forms or featured content. Its moderate saturation provides a warm, earthy contrast |
| Dusty Rose | `#c9a9b5` | neutral | Secondary background sections, footer background. A muted, desaturated rose that contributes to the vintage atmosphere |
| Slate Grain | `#767676` | neutral | Default input border color for a subtle, unobtrusive form |
| Deep Pewter | `#344b52` | neutral | Subtle border color for links and decorative accents |
| Pitch Black | `#000000` | neutral | Highest contrast text, borders for ghost components, decorative lines |

## Typography

### BasicCommercial LT Com Roman

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 19px, 38px, 49px |
| lineHeight | 1.16, 1.18, 1.20 |
| letterSpacing | 0.003, 0.004, 0.008, 0.026 |
| substitute | Inter |
| role | Main navigation, larger headings, and body text. Its clean, condensed sans-serif provides modern legibility against the serif. |

### Adobe Caslon Pro

| Key | Value |
| --- | --- |
| weight | 300 |
| weights | 300 |
| sizes | 49px |
| lineHeight | 1.18 |
| letterSpacing | 0.003 |
| substitute | Libre Baskerville |
| role | Primary headings, subheadings, and featured text blocks. The traditional serif conveys authority and elegance, creating an established, classic brand feel. Its lightness at 300 weight is distinctive, suggesting understated professionalism. |

### Elementa

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 16px, 19px |
| lineHeight | 1.00, 1.16 |
| letterSpacing | 0.008, 0.042 |
| substitute | Graphik |
| role | Paragraph text, small labels, and detailed content. A highly readable sans-serif for practical information. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 19 |  | 1.18 |  |
| body-sm | 38 |  | 1.18 |  |
| body | 49 |  | 1.18 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| inputs | 0px |
| buttons | 0px |

- **elementGap** — 23px
- **sectionGap** — 136px
- **cardPadding** — 27px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Parchment | `#e9e9e2` | 1 | Primary content canvas, providing a soft background for text-heavy sections. |
| Old Gold | `#aa9e54` | 2 | Elevated background for distinct content blocks, like forms or highlighted features, offering a warm contrast. |
| Dusty Rose | `#c9a9b5` | 3 | Background for less textual, more illustrative elements such as the hero or footer, serving a decorative role. |
| White Linen | `#ffffff` | 4 | Background for pristine, clean content areas or specific nested components. |

## Components

### Ghost Text Button

**Role:** Default interactive element for navigation and actions.

Text in Inkwell (#062d32), no background, no border, with a padding of 27px symmetrically. Underlined on hover or active state with Inkwell (#062d32).

### Underlined Call-to-Action

**Role:** Semantic action for submissions or primary interactions.

Text in Pitch Black (#000000), no background, with a 1px solid Pitch Black (#000000) border bottom, 0px border radius, and 27px padding. Uses the 'Elementa' font at 16px, weight 400.

### Form Input Field

**Role:** Standard input for user data collection.

No background, text in Pitch Black (#000000), with a 1px solid Slate Grain (#767676) border bottom. No padding or border radius. Focus state changes border to Inkwell (#062d32). Placeholder text uses the same style but in a lighter shade.

### Form Input Field (Active)

**Role:** Active state for form inputs.

No background, text in Inkwell (#062d32), with a 1px solid Inkwell (#062d32) border bottom. No padding or border radius.

### Content Section (Old Gold)

**Role:** Distinct background for specific content blocks like forms.

Background color Old Gold (#aa9e54), typically with text in Pitch Black (#000000), with generous vertical padding (e.g., 100-150px) to provide ample breathing room.

### Content Section (Dusty Rose)

**Role:** Background for hero sections or footers.

Background color Dusty Rose (#c9a9b5), with text in Inkwell (#062d32), featuring flexible padding that allows for large illustrations or minimal content.

## Layout

The page structure favors a max-width contained layout, though specific hero sections or illustrations can expand. The hero pattern features a centered, large-scale line illustration against a Dusty Rose (#c9a9b5) background, with minimal text, setting an immediate atmospheric tone. Section rhythm alternates between Parchment (#e9e9e2) and Old Gold (#aa9e54) backgrounds, sometimes introducing White Linen (#ffffff). Content is typically arranged in centered stacks or simple two-column layouts for text-dominant areas. There is ample white space and consistent vertical spacing (136px for sections), creating a feeling of spaciousness. Navigation is a minimalist top bar with ghost text links.

## Imagery

The visual language relies heavily on line illustrations, primarily monochrome (Inkwell #062d32 on pale backgrounds). These illustrations are detailed, depicting scenes with human figures and architectural elements, often in a whimsical, narrative style. They are generally contained compositions, not full-bleed, and serve a decorative and atmospheric role rather than explanatory. There are no photographs or product shots. Icons are simple, outlined, sharing the same stroke weight as the illustrations. Imagery plays a significant role in creating a specific, charming brand mood, complementing the text-heavy layout without overwhelming it.

## Dos & Donts

### Do

- Prioritize Elementa for all body text and small labels at 16px, weight 400, for practical legibility.
- Use Adobe Caslon Pro at 49px (weight 300) for major headings to establish a classic, refined tone.
- Implement Inkwell (#062d32) as the primary accent for active states, link underlines, and subtle borders.
- Maintain a monochromatic palette for most UI elements, reserving Old Gold (#aa9e54) and Dusty Rose (#c9a9b5) for distinct background sections.
- Apply 27px padding uniformly to all buttons for consistent visual weight.
- Ensure all interactive elements and underlines use `0px` radius for a sharp, traditional finish.
- Utilize white Linen (#ffffff) and Parchment (#e9e9e2) as the default canvas colors, switching based on content hierarchy or section distinction.

### Don't

- Avoid using saturated or bright colors beyond the defined Old Gold (#aa9e54) for any UI elements.
- Do not introduce sharp shadows or significant elevation; the design relies on flat, understated surfaces.
- Refrain from using any border radius on buttons, inputs, or interactive elements; all corners should be sharp 0px.
- Do not break the line-based input field style with filled backgrounds or alternative border treatments.
- Avoid crowding elements; ensure generous spacing (e.g., 23px element gap, 136px section gap) to maintain an airy, considered layout.
- Do not use highly decorative fonts or vary font weights beyond 300 and 400; stick to the defined Elementa, Adobe Caslon Pro, and BasicCommercial families.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #062d32
background: #e9e9e2
border: #062d32
accent: #aa9e54
primary action: #062d32 (outlined action border)

### 3-5 Example Component Prompts
1. Create an Outlined Primary Action: Transparent background, #062d32 border and text, 9999px radius, compact pill padding. Use it for the main CTA instead of a filled button.
3. Create a hero section: Background Dusty Rose (#c9a9b5). Centered large responsive line illustration. No text immediately visible, allowing the illustration to dominate. Ensure significant vertical padding top and bottom (e.g., 100px top, 150px bottom) to frame the illustration.

---
_Source: https://styles.refero.design/style/07f5281d-2a18-4e12-a8ff-d54d3e03d198_
