# 70Materia — Design Reference

> industrial minimalism

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://70materia.com](https://70materia.com) |
| Refero page | [https://styles.refero.design/style/f22a5ad1-2770-48d5-aff4-d1aaf0b789b8](https://styles.refero.design/style/f22a5ad1-2770-48d5-aff4-d1aaf0b789b8) |
| Theme | light |
| Industry | design |

## Overview

70Materia crafts a visual system of industrial minimalism: a stark black-and-white canvas provides an austere backdrop for carefully textured visual content. Typography is compact and precise, maintaining a high information density without feeling cramped. Component borders are razor-thin, and elements are primarily monochromatic, relying on strong contrast and subtle material variations to define structure rather than complex color palettes or deep shadows.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Black | `#000000` | neutral | Primary text, heading text, critical borders, icons — provides crisp definition against light surfaces |
| Pure White | `#ffffff` | neutral | Page backgrounds, card surfaces, button backgrounds, inverse text for dark elements — a clean, expansive canvas |
| Off-Black | `#1e1e1e` | neutral | Dark surface backgrounds (e.g., footer), primary filled button background — a slightly softer alternative to Absolute Black for larger dark areas |
| Light Concrete | `#bababa` | neutral | Subtle background panels, light surface separators — imparts a sense of concrete or stone texture |
| Alpha Black | `#0000004d` | neutral | Subtle overlays, contextual background tints — used for transparent dark effects |
| Dark Sand | `#876a30` | accent | Implicit in imagery; potentially for subtle highlights or decorative accents if introduced into UI components |
| Light Sand | `#d2c5aa` | accent | Implicit in imagery; potentially for subtle highlights or decorative accents if introduced into UI components |

## Typography

### Matter

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 15px, 19px, 20px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| fontFeatureSettings | "tnum" |
| substitute | Inter |
| role | General sans-serif for headings, body text, links, and various UI elements. Its clean, sharp forms underpin the industrial aesthetic. |

### Matter Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 10px, 12px |
| lineHeight | 1.20, 1.30 |
| letterSpacing | -0.24px at 12px, -0.4px at 10px |
| fontFeatureSettings | "tnum" |
| substitute | IBM Plex Mono |
| role | Monospaced font for buttons, small labels, and precise data display. The tighter tracking at smaller sizes maintains legibility and a compact, technical feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.2 | -0.4 |
| body | 15 |  | 1.2 |  |
| subheading | 19 |  | 1.2 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 3px |

- **elementGap** — 8px
- **sectionGap** — 100px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Light Concrete Base | `#bababa` | 1 | The foundational background for general content blocks, offering a subtle, material-like base. |
| Pure White Canvas | `#ffffff` | 2 | Primary content surfaces for cards, detail panels, and sections that require high contrast for text and interaction elements. |
| Off-Black Footer | `#1e1e1` | 3 | A contrasting, dark background for the page footer, visually anchoring the end of the content. |

## Components

### Primary Filled Button

**Role:** Interactive action button

Solid Off-Black background (#1e1e1e) with Pure White text (#ffffff), 3px border-radius, and 8px vertical / 24px horizontal padding. Uses Matter Mono at weight 400 with a slight letter-spacing.

### Secondary Outlined Button

**Role:** Interactive action button

Pure White background (#ffffff) with Absolute Black text (#000000) and a 1px Absolute Black (#000000) border, 3px border-radius, and 8px vertical / 24px horizontal padding. Uses Matter Mono at weight 400 with a slight letter-spacing.

### Navigation Link

**Role:** Top-level navigation items

Absolute Black text (#000000) with no specific background. Uses Matter Mono weight 400 with tight letter-spacing. Inherently interactive with hover state suggested by motion profile.

### Footer Dark Section

**Role:** Background for copyright and secondary links

Off-Black background (#1e1e1e) contrasting with the main content area. Contains Pure White text and links.

### Text Input / Form Field

**Role:** User input

Hypothesized: Absolute Black text (#000000) on a Pure White background (#ffffff) with a 1px Absolute Black (#000000) border. 3px border-radius based on general component rounding. Padding is likely 8px vertical and 12-16px horizontal to match button density.

## Layout

The page uses a maximum-width contained layout, though specific measurements for `pageMaxWidth` are not provided, content is clearly constrained and centered. The hero section employs a full-width photographic background with understated text overlay. Section rhythm is driven by large vertical separations (100px) and a mix of full-width content blocks and two-column layouts. The content arrangement frequently alternates text-left/image-right or image-left/text-right, creating a structured flow. There's a clear emphasis on large, impactful imagery balanced with concise textual descriptions. Navigation is a minimal, top-aligned text menu.

## Imagery

The imagery focuses on materials and product applications: highly textural photography of concrete, stone, and raw surfaces. Treatments are typically full-bleed or large contained blocks, showcasing the intricate details of the material. Product shots are often close-ups, highlighting texture and finish rather than full product context. There are abstract compositions with measuring tools, reinforcing the 'laboratory of ideas' theme. Icons are absent from the overall design, reinforcing a clean, purely visual aesthetic for content. Imagery is dominant and essential for conveying the brand's core offering.

## Dos & Donts

### Do

- Use Absolute Black (#000000) for all primary text and critical UI borders.
- Maintain a compact information density; use 1.2 line height for Matter font families.
- Apply 3px border-radius consistently for all interactive elements like buttons and input fields.
- Utilize Matter Mono exclusively for buttons and micro-text, employing its specific letter-spacing for legibility.
- Employ Off-Black (#1e1e1e) as a rich contrasting background for footer sections or prominent content blocks.
- Ensure all primary interactive elements are framed by a 1px border or clear background differentiation.
- Prioritize explicit contrast for text legibility; avoid low-contrast text on Light Concrete (#bababa).

### Don't

- Do not introduce strong accent colors into the main layout; stick to monochrome for structural elements and text.
- Avoid deep or complex shadows; elevation is primarily achieved through stark color contrast and flat surface changes.
- Do not deviate from the specified Matter and Matter Mono fonts; no system fonts or alternatives for core text.
- Desist from large, decorative hero typography; headlines should be Matter 400 at given sizes, compact and precise.
- Avoid fluid or organic shapes in UI components; maintain sharp edges and minimal radii for an industrial feel.
- Do not use generic padding for buttons; adhere strictly to 8px vertical and 24px horizontal padding.
- Refrain from using `normal` letter-spacing for Matter Mono; always apply its defined tighter tracking.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #000000
accent: no distinct accent color
primary action: #1e1e1e (filled action)

Example Component Prompts:
1. Create a Primary Filled Button: Off-Black background (#1e1e1e), Pure White text (#ffffff), 3px radius, 8px vertical / 24px horizontal padding, Matter Mono weight 400, 12px size, -0.24px letter-spacing.
2. Create a Secondary Outlined Button: Pure White background (#ffffff), Absolute Black text (#000000), 1px Absolute Black border (#000000), 3px radius, 8px vertical / 24px horizontal padding, Matter Mono weight 400, 12px size, -0.24px letter-spacing.
3. Create a Navigation Link: Absolute Black text (#000000), Matter Mono weight 400, 12px size, -0.24px letter-spacing. No background or border.

---
_Source: https://styles.refero.design/style/f22a5ad1-2770-48d5-aff4-d1aaf0b789b8_
