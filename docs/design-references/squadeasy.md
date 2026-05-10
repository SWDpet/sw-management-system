# SquadEasy — Design Reference

> Playful block playground

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.squadeasy.com](https://www.squadeasy.com) |
| Refero page | [https://styles.refero.design/style/3e5c272b-8d68-40d8-9726-b4d6914b4b16](https://styles.refero.design/style/3e5c272b-8d68-40d8-9726-b4d6914b4b16) |
| Theme | light |
| Industry | saas |

## Overview

SquadEasy's design system evokes a playful, high-contrast digital environment. It uses bold, unadorned typography set against vibrant, geometric color blocks and photo cutouts. The layout is dynamic, featuring angled visual elements and prominent typography to create a sense of directness and energy. Color is employed in large, flat regions and as vivid accents, while component styling is typically crisp with generous radii, prioritizing impact over subtle detail.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Amber Canvas | `#e1c19e` | brand | Primary page background for hero sections and expansive content zones, evoking a warm, inviting atmosphere |
| Deep Violet | `#adabff` | brand | Background for certain cards and content sections, adding depth and a distinct visual interruption to the warm canvas |
| Electric Lime | `#e4ff60` | accent | Primary action background, indicating interactivity with a high-energy pop, and used for decorative fills |
| Sky Blue | `#7fb6e6` | accent | Secondary button backgrounds and decorative elements, providing a cooler accent hue |
| Hot Pink | `#ea5da3` | accent | Highlight text, decorative fills, and border accents, drawing immediate attention to key phrases and elements |
| Forest Green | `#6fb853` | accent | Green accent for outlined action borders, linked labels, and lightweight interactive emphasis. Use as a supporting accent, not as a status color |
| Absolute Black | `#000000` | neutral | Primary text, borders, and solid button fills, providing strong contrast against all backgrounds |
| Pure White | `#ffffff` | neutral | Secondary text, button text on dark backgrounds, and footer background, acting as a clean counterpoint |
| Soft Gray | `#f6f6f6` | neutral | Subtle background for UI elements, offering a slight visual break from pure white |

## Typography

### Body

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 14px, 16px, 17px, 18px, 19px, 22px |
| lineHeight | 1.00, 1.20, 1.21 |
| letterSpacing | -0.0180em, -0.0160em, -0.0150em, -0.0140em, -0.0130em, -0.0110em |
| substitute | Inter |
| role | General body text, links, and various UI elements. Its range of weights and sizes provides versatility for content hierarchy and interactive states. |

### Black

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 16px, 50px, 56px, 62px, 80px, 110px, 220px |
| lineHeight | 0.87, 1.00, 1.05, 1.20 |
| letterSpacing | -0.0360em, -0.0320em, -0.0250em, -0.0180em, -0.0160em, -0.0050em, -0.0010em |
| substitute | Oswald |
| role | Dominant font for headings and impactful display text. Its inherent boldness, combined with tight line heights and negative letter-spacing, creates a commanding, space-efficient presence. |

### Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px, 18px, 22px |
| lineHeight | 1.20, 1.21 |
| letterSpacing | -0.0180em, -0.0160em, -0.0140em, -0.0110em |
| substitute | Open Sans |
| role | Used for specific button labels and navigation items, offering a slightly more relaxed feel than 'Body' to contrast interactive elements. |

### Medium

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 14px, 16px |
| lineHeight | 1.20 |
| letterSpacing | -0.0210em, -0.0180em, -0.0160em |
| substitute | Inter |
| role | Small text and button labels, maintaining legibility at smaller sizes with slightly tighter tracking. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Arial |
| role | Fallback or specific utility text, relying on system font accessibility. |

### Sharpie

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.20 |
| letterSpacing | -0.0160em |
| substitute | Comic Neue |
| role | Specific decorative text, providing a distinctive, hand-drawn aesthetic. |

### Epilogue

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 18px |
| lineHeight | 1.20 |
| letterSpacing | -0.0140em |
| substitute | Epilogue |
| role | Used for distinctive button labels, offering a subtle, elegant touch for important actions. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 | -0.252 |
| body-sm | 14 |  | 1.21 | -0.252 |
| body | 16 |  | 1.2 | -0.256 |
| subheading | 18 |  | 1.2 | -0.252 |
| heading-sm | 22 |  | 1.2 | -0.242 |
| heading | 50 |  | 1.05 | -1.6 |
| heading-lg | 56 |  | 1 | -1.4 |
| display | 80 |  | 0.87 | -2.88 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| misc | 10px |
| cards | 0px |
| buttons | 100px |
| navElements | 14px |

- **elementGap** — 16px
- **sectionGap** — 100px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Amber Canvas | `#e1c19` | 0 | Primary page background, providing a warm, foundational tone for expansive sections. |
| Soft Gray | `#f6f6f6` | 1 | Subtle background for specific content areas, offering a slight visual differentiation from pure white. |
| Pure White | `#ffffff` | 2 | Background for specific content blocks, footer, and internal elements requiring a clean, bright surface. |
| Deep Violet | `#adabff` | 3 | Elevated card backgrounds, creating strong visual segments and conveying importance. |

## Components

### Text Link Button

**Role:** Navigation and secondary actions.

Transparent background, 'Absolute Black' text or 'Pure White' on dark backgrounds. No border, 'Regular' or 'Body' font family at 16px, 0px border radius, 16px padding on all sides for clickable area.

### Pill Ghost Button (Black)

**Role:** Outlined secondary actions.

Transparent background, 'Absolute Black' text, 100px border radius, with a 1px 'Absolute Black' border. No horizontal/vertical padding detected, suggesting an icon button or minimal text treatment.

### Pill Ghost Button (White)

**Role:** Outlined secondary actions on dark backgrounds.

Transparent background, 'Pure White' text, 100px border radius, with a 1px 'Pure White' border. No horizontal/vertical padding detected, suggesting an icon button or minimal text treatment.

### Pill Filled Button (Black)

**Role:** Primary action within a neutral context.

'Absolute Black' background, 'Pure White' text, 100px border radius, 16px vertical padding, 14px horizontal padding on right and 16px on left.

### Pill Filled Button (Electric Lime)

**Role:** Prominent calls to action.

'Electric Lime' background, 'Absolute Black' text, 100px border radius, 16px padding. This is the most persuasive button style.

### Info Card (Squared)

**Role:** Content presentation with a distinctive background.

Background 'Deep Violet', 0px border radius, no box shadow, 40px top padding, 24px horizontal padding, 140px bottom padding. Large internal padding for generous content framing.

## Layout

The page primarily uses a full-bleed layout for background color blocks and hero sections, with text content often centered or presented in two-column arrangements. The hero features a bold, centered headline overlaying the 'Amber Canvas' background with dynamic, angled photo cutouts. Sections alternate between solid color backgrounds (like 'Amber Canvas' and 'Deep Violet') with strong vertical spacing provided by a section gap of 100px. Content blocks, such as testimonial cards, often use a grid-like structure. Elements within sections generally maintain a max-width for readability, but the backgrounds extend full-width. Navigation is a sticky top bar with a centered logo, text links, and a prominent pill-shaped CTA button.

## Imagery

Imagery primarily consists of high-contrast, candid lifestyle photography featuring diverse individuals, often cropped tightly and presented as angled, unmasked cutouts. These images are used decoratively to add a human element and dynamic energy, frequently layered over solid color blocks. Icons are minimal, utilizing bold strokes or fills in black or accent colors. Product screenshots are contained within device mockups, maintaining a clean, focused presentation.

## Dos & Donts

### Do

- Always use 'Absolute Black' (#000000) for primary text on light backgrounds and 'Pure White' (#ffffff) on dark backgrounds.
- Apply a 100px border radius to all interactive buttons for a consistent, soft pill shape.
- Use 'Electric Lime' (#e4ff60) specifically for primary call-to-action button backgrounds.
- Employ 'Deep Violet' (#adabff) for prominent content cards to differentiate them from the main canvas.
- Layer large, angled photographic cutouts to create a dynamic and energetic visual composition.
- Utilize 'Black' font family at large sizes for headlines (50-220px) with tight line heights to ensure a commanding presence.
- Maintain a comfortable density with a base spacing unit of 4px and elemental gaps around 16px.

### Don't

- Do not use generic gray tones for primary interactive elements; always use chromatic colors for emphasis.
- Avoid subtle shadows or gradients on component surfaces; stick to flat, vibrant color blocks or crisp borders.
- Do not use small, delicate fonts for headlines; always leverage the 'Black' font family for impact.
- Never use square corners for buttons; always apply the 100px border radius for a distinct visual identity.
- Do not place images in simple, contained boxes; allow them to break out of their bounds or appear dynamically angled.
- Avoid highly ornate or complex typography; the system favors bold, direct, and efficient typefaces.
- Do not introduce additional background colors outside of the defined 'Amber Canvas', 'Deep Violet', 'Electric Lime', and 'Soft Gray' for major sections.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- text: #000000
- background: #e1c19e
- border: #000000
- accent: #ea5da3
- primary action: #000000 (filled action)

**3-5 Example Component Prompts**
- Create a Primary Action Button: #000000 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Create a testimonial card: 'Deep Violet' background (#adabff), no border radius, 40px top padding, 24px horizontal padding, 140px bottom padding. Body text 'Body' 16px weight 400 #000000.
- Create a navigation link: No background, 'Absolute Black' text, 'Regular' 16px font, no border, no radius. Hover state: text changes to 'Hot Pink' (#ea5da3).
- Create a ghost button: Transparent background, 'Forest Green' (#6fb853) text, 1px 'Forest Green' (#6fb853) border, 100px radius. No padding specified by system, implying visual icon or minimal text.

---
_Source: https://styles.refero.design/style/3e5c272b-8d68-40d8-9726-b4d6914b4b16_
