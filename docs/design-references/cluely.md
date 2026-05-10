# Cluely — Design Reference

> Radiant cloud-native intelligence: ethereal gradients, frosted glass, and digital blue accents against a bright canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://cluely.com](https://cluely.com) |
| Refero page | [https://styles.refero.design/style/72da35d5-1cfd-41a3-94f6-cb6b8c07a670](https://styles.refero.design/style/72da35d5-1cfd-41a3-94f6-cb6b8c07a670) |
| Theme | light |
| Industry | ai |

## Overview

Cluely employs a radiant cloud-native aesthetic: light surfaces against a vibrant sky-blue hero, with crisp typography and subtle shadows. The design emphasizes lightness and integration, with ethereal gradients and frosted glass elements creating a sense of an AI assisting from 'within' the system rather than as a heavy external application. Interaction is guided by a singular vivid blue accent, making key actions feel responsive and digitally integrated.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated surfaces for cards and modals, primary text for dark backgrounds |
| Subtle Gray | `#e4e4e7` | neutral | Background for secondary sections, input borders, ghost button borders, subtle dividers |
| Surface Text | `#000000` | neutral | Primary text on light backgrounds, strong headings, active state icons |
| Paragraph Gray | `#2e3038` | neutral | Body text, navigation items, secondary headings, muted icons |
| Muted Text | `#777a88` | neutral | Helper text, footer links, small annotations, less prominent body copy |
| Sky Canvas | `#f3f8ff` | neutral | Subtle background for UI elements, light emphasis on certain sections |
| Digital Blue | `#3c83f6` | brand | Primary call-to-action buttons, interactive links, highlights |
| Deep Ocean Gradient Start | `#0544a9` | brand | Base color for prominent background gradients and elevated button shadows |
| Frost Morning Gradient Start | `#ddefe6` | accent | Subtle background gradient for cards and containers, creating a light, airy feel |
| Process Blue Gradient Start | `#1e82e0` | accent | Hero background gradient, creating a vibrant and welcoming visual |
| System Green | `#00ff26` | accent | Green outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Teal Tint | `#7df0f8` | accent | Highlight backgrounds for text blocks, subtle decorative elements |

## Typography

### Geist

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600 |
| sizes | 9px, 10px, 11px, 12px, 13px, 14px, 16px, 18px, 19px, 20px, 24px, 28px, 30px, 36px, 42px, 48px, 56px |
| lineHeight | 1.00, 1.11, 1.13, 1.20, 1.25, 1.33, 1.38, 1.40, 1.43, 1.50, 1.56, 1.60 |
| letterSpacing | -0.0400em at 56px, -0.0360em at 48px, -0.0250em at 36px, normal at 16px |
| substitute | Inter |
| role | The primary typeface for all UI elements, body text, and most headings. Its clean, modern lines balance approachability with technical clarity, used across a wide range of weights and sizes to establish hierarchy. |

### EB Garamond

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 80px |
| lineHeight | 1.02 |
| letterSpacing | -0.0120em |
| substitute | Garamond |
| role | Used for hero headings, providing a sophisticated, almost editorial counterpoint to Geist. The larger size and slight tracking create a distinctive, impactful display. |

### ui-monospace

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 10px |
| lineHeight | 1.5 |
| role | ui-monospace — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.43 | -0.14 |
| subheading | 18 |  | 1.33 | -0.036 |
| heading-sm | 24 |  | 1.25 | -0.025 |
| heading | 42 |  | 1.13 | -0.036 |
| heading-lg | 56 |  | 1 | -0.04 |
| display | 80 |  | 1.02 | -0.012 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 4px |
| cards | 24px |
| inputs | 4px |
| buttons | 1.67772e+07px |

- **elementGap** — 8px
- **sectionGap** — 73px
- **cardPadding** — 22px
- **pageMaxWidth** — 1200px

## Components

### Pill Button - Ghost

**Role:** Secondary action button, often paired with primary actions or for discrete functionalities.

Transparent background, 'Canvas White' text, 'Subtle Gray' 1px border. Extremely rounded corners at 1.67772e+07px for a pill shape. Padding: 0px vertical, 12px horizontal.

### Pill Button - Primary

**Role:** Main call-to-action button, visually distinct and inviting interaction.

Solid 'Digital Blue' background, 'Canvas White' text. Extremely rounded corners at 1.67772e+07px for a pill shape.

### Pill Button - Shadowed Overlay

**Role:** Interactive elements within product screenshots, mimicking OS-level controls.

Semi-transparent background (rgba(24, 23, 28, 0.1)), 'Canvas White' text (oklab(0.999994 0.0000455678 0.0000200868 / 0.4)). Border: oklab(0.999994 0.0000455678 0.0000200868 / 0.15). Pill shaped with 1.67772e+07px radius. Padding: 0px vertical, 8px horizontal.

### Feature Card - Transparent

**Role:** Displaying product features or information blocks without heavy visual presence.

Transparent background, 24px border radius. 22px padding on all sides. No box shadow, relying instead on layout and subtle background variations.

### Content Input Field

**Role:** Standard text input for forms, often with a product-like dark aesthetic.

Transparent background, 'Canvas White' text within. 'Subtle Gray' 1px border at the bottom only for a subtle demarcation. No border radius. Padding: 10px vertical, 10px horizontal.

### Frosted Card - Dark

**Role:** Presenting nested content or highlighting specific information within a dark context.

Semi-transparent dark background (oklab(0.257093 0.000663139 -0.0134566 / 0.5)), 12px border radius. Padding: 12px vertical, 28px horizontal. No box shadow.

## Layout

The page model is a max-width 1200px centered container, creating a structured and controlled experience. The hero section is full-bleed, featuring a vibrant sky-blue radial gradient background with a centered, large EB Garamond headline. Subsequent sections alternate between white and 'Subtle Gray' backgrounds with consistent vertical spacing. Content is frequently arranged in two-column text-left/visual-right or visual-left/text-right patterns, often embedding product screenshots within transparent or frosted cards. A 3-column card grid is used for features. Navigation is a minimalist top bar, likely sticky, with clear branding and key external links.

## Imagery

The visual language for imagery is primarily product screenshots presented within a 'frosted glass' macOS-like frame. These screenshots occupy significant visual space, often full-bleed or presented as large, contained components. Photography is minimal, often cropped tightly on product elements. Icons are typically outlined or mono-color (achromatic), with a thin stroke weight, used functionally rather than decoratively. The density is text-dominant for explanatory content, but imagery takes precedence in showcasing the product's UI, serving explanatory and demonstrative roles.

## Dos & Donts

### Do

- Use Geist at weight 300 for large headings to convey authority via restraint, not volume.
- Apply 1.67772e+07px border radius for all interactive pill-shaped buttons to maintain a soft, modern feel.
- Employ 'Digital Blue' (#3c83f6) exclusively for primary action buttons and prominent interactive links.
- Structure page sections using 'Subtle Gray' (#e4e4e7) as a background for secondary content blocks.
- Incorporate subtle linear gradients like 'Deep Ocean Gradient Start' for elevated button shadows and background depth.
- Prioritize 'Canvas White' (#ffffff) for all main card and modal backgrounds, ensuring ample contrast for text.
- Use 'Frosted Card - Dark' with its oklab(0.257093 0.000663139 -0.0134566 / 0.5) background for content nested within darker product UI components.

### Don't

- Avoid generic square buttons; all standalone actions should adopt the pill shape (radius 1.67772e+07px).
- Refrain from using strong, opaque background colors on cards; prefer transparent or very light neutrals like 'Subtle Gray' or 'Canvas White'.
- Do not introduce new color accents outside of 'Digital Blue' (#3c83f6) for primary interactive elements.
- Do not use heavy, opaque shadows; opt for subtle, often colored, and semi-transparent elevation patterns like those found on buttons and product UI.
- Avoid dense, clustered layouts; maintain generous 'elementGap' of 8px and 'sectionGap' of 73px to preserve spaciousness.
- Do not use system fonts for headings; always use Geist or EB Garamond as specified for distinctiveness.
- Do not use dark backgrounds for main page content; the theme is predominantly light, with dark elements reserved for embedded product UI.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #000000
background: #ffffff
border: #e4e4e7
accent: #3c83f6
primary action: #3c83f6 (filled action)

Example Component Prompts:
Create a hero section: radial-gradient(114.65% 114.65% at 9.73% 17.27%, rgb(30, 130, 224) 0px, rgb(28, 56, 234) 100%) background. Headline 'Undetectable AI for Meetings' at 80px EB Garamond weight 500, #ffffff, letter-spacing -0.012em. Subtext at 18px Geist weight 400, #ffffff. Primary pill button 'Get for Mac' with Digital Blue (#3c83f6) background, white text, 1.67772e+07px radius.

Create a feature card: Transparent background, 24px border radius, 22px padding. Heading at 24px Geist weight 500, #000000. Body text at 14px Geist weight 400, #2e3038. Small pill tag for categorization with 'System Green' (#00ff26) background and #ffffff text, 1.67772e+07px radius.

Create a product input field: Transparent background, #ffffff text. Bottom border 1px solid #e4e4e7. No border radius. Padding 10px.

---
_Source: https://styles.refero.design/style/72da35d5-1cfd-41a3-94f6-cb6b8c07a670_
