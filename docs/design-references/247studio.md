# 247Studio — Design Reference

> Black & White Blueprint: A stark, high-contrast canvas where information is meticulously laid out like an architectural plan.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://247studio.co](https://247studio.co) |
| Refero page | [https://styles.refero.design/style/c43237e3-dae2-4fc6-a917-519b25c870e5](https://styles.refero.design/style/c43237e3-dae2-4fc6-a917-519b25c870e5) |
| Theme | light |
| Industry | agency |

## Overview

This design system presents as a crisp, high-contrast, and almost clinical aesthetic, focusing heavily on legibility and minimalist presentation. The absence of color, save for pure black and white, forces attention onto typography and spatial relationships. It creates an atmosphere of precise, no-nonsense communication, suggesting an emphasis on clear information delivery over decorative flourishes.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Storm Black | `#000000` | neutral | Primary text, interactive elements, button backgrounds, primary navigation — grounding the entire visual experience in stark contrast. |
| Cloud White | `#ffffff` | neutral | Page backgrounds, button text, card backgrounds — serving as the expansive, clean canvas for all content. |
| Deep Graphite | `#1f1f1f` | neutral | Secondary text, subtle background shifts for content segmentation. |
| Slate Gray | `#808080` | neutral | Subtle body text, descriptive labels, secondary headings— providing a quieter vocal tone than pure black. |
| Light Concrete | `#f2f2f2` | neutral | Subtle background for card surfaces or distinct content blocks, creating a gentle separation from the primary white background. |
| Shadow Ash | `#666666` | neutral | Less prominent body text or supporting information, indicating less hierarchy than Slate Gray. |
| Silver Mist | `#999999` | neutral | Faint text, placeholder text or subtle labels for tertiary information. |
| Border Grey | `#cccccc` | neutral | Divisions and borders, providing structure without visual weight. |

## Typography

### 247 grotesk

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 10px, 11px, 14px, 16px, 19px, 21px, 28px, 31px, 35px, 48px, 52px, 55px, 73px |
| lineHeight | 1.00, 1.14, 1.20, 1.25, 1.30, 1.89 |
| letterSpacing | normal |
| fontFeatureSettings | "ss01" |
| substitute | Inter |
| role | The primary workhorse for all UI elements, headlines, body text, and interactive components. Its prominent usage across all visual hierarchy levels, especially for large, tight headlines, defines the modern, direct tone. |

### Ntbau

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 10px, 11px, 12px, 14px, 18px |
| lineHeight | 1.00, 1.14, 1.20, 1.30 |
| letterSpacing | 0.0190em |
| substitute | Roboto Mono |
| role | Used for specific body text and links, its presence with a consistent positive letter-spacing suggests a subtle, technically precise contrast against the often tightly-spaced 247 grotesk. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1 |  |
| body | 14 |  | 1.2 |  |
| heading-sm | 19 |  | 1.3 |  |
| heading | 21 |  | 1.25 |  |
| heading-lg | 28 |  | 1.25 |  |
| display | 73 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| default | 0px |

- **elementGap** — 
- **sectionGap** — 
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Announcement Banner

### Client Logos Grid

### Project Cards Grid

### Primary Button

**Role:** Call to action, navigation

Solid black background (#000000), white text (#ffffff) with 247 grotesk, weight 400. No border-radius, maintaining sharp edges. Padding is 0px top/bottom and 30.08px left/right.

### Secondary Button

**Role:** Alternative actions, subtle interaction

Transparent background with black text (#000000) and a black border (#000000), 247 grotesk, weight 400. No border-radius. Padding is 0px top/bottom and 30.08px left/right.

### Basic Card

**Role:** Content grouping, visual separation

Transparent background (rgba(0,0,0,0)), no border-radius or shadow, padding is 0. Primarily used as a structural container, relying on typography and spacing for definition.

### Elevated Card

**Role:** Highlighted content, distinct sections

White background (#ffffff), no border-radius or shadow, padding is 0. Used for content blocks that need to stand out against a potentially darker or patterned section.

### Subtle Background Card

**Role:** Softly delineated content areas

Light Concrete background (#f2f2f2), no border-radius or shadow, padding is 0. Offers a slight visual break without strong borders or shadows.

### Navigation Link

**Role:** Site navigation, internal links

Black text (#000000) using 247 grotesk. Typically uppercase, with specific positive letter-spacing, providing a strong, defined feel without underlines.

### Small Text Link

**Role:** Supporting links, footnotes

Black text (#000000) using Ntbau at smaller sizes, with 0.0190em letter spacing, often found in ancillary content areas.

## Layout

The page adheres to a full-bleed model, with content often stretching edge-to-edge. However, text and core UI elements are generally confined to an implied central content area, creating a sense of spaciousness around them. The hero section is a full-bleed white background with a large, centered headline that bleeds off the right side, immediately establishing the typographic emphasis. Sections alternate between pure white and subtle off-white backgrounds (#f2f2f2), creating a gentle rhythm. Content arrangement frequently features centered text blocks or alternating text-left/image-right (or vice-versa) compositions. There's dense vertical spacing between sections (around 400px), indicating a desire for significant breathing room. Navigation is primarily a top bar with a sticky header, augmented by a 'Kontakt +' button always present on the right edge.

## Imagery

The visual language is characterized by an absence of traditional photography or illustrations. Instead, it features product screenshots or 3D renders that are either monochrome or desaturated, contained within solid blocks of black or white. These visuals serve an explanatory/product showcase role, stripped of context, focusing solely on the object or interface itself. The density of imagery is low, with text dominating the page. Iconography, when present, is starkly outlined or filled in monochrome, reinforcing the precise, functional aesthetic.

## Elevation philosophy

This design intentionally avoids any form of elevation (box-shadows) or subtle dimensional cues. Its philosophy relies purely on the stark contrast between black and white, and subtle changes in background color, to differentiate elements and create visual hierarchy. The absence of shadows contributes to a flat, print-like aesthetic, emphasizing clarity and directness over simulated depth.

## Dos & Donts

### Do

- Prioritize pure black (#000000) and pure white (#ffffff) for dominant text and background elements to maintain high contrast.
- Use 247 grotesk for all headlines and significant UI text, manipulating size and weight for hierarchy rather than color.
- Apply Ntbau (with its 0.0190em letter-spacing) for specific technical or descriptive text, offering visual variety.
- Structure content primarily through generous vertical spacing (sectionGap) and typography, avoiding strong visual dividers or shadows.
- Maintain hard edges and 0px border-radius for almost all elements except specific brand assets to preserve the stark, precise aesthetic.
- Ensure interactive elements (buttons, links) are clearly distinguishable by their #000000/#ffffff color inversions or slight background shifts.

### Don't

- Do not introduce any chromatic colors into the main UI; the system relies strictly on a grayscale palette.
- Avoid using box-shadows or complex elevation styles; depth should be created through surface color changes and spacing.
- Do not use heavily rounded elements; stick to the hard-edged aesthetic defined by 0px border radii.
- Do not vary letter-spacing for 247 grotesk unless explicitly defined for a specific type role; it is generally 'normal'.
- Avoid decorative imagery that introduces color or excessive visual noise that would conflict with the minimalist, high-contrast theme.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Text Primary**: #000000
- **Background Primary**: #ffffff
- **Background Secondary**: #f2f2f2
- **Button Background**: #000000
- **Button Text**: #ffffff
- **Subtle Text**: #808080

### 3-5 Example Component Prompts
1. **Create a hero section**: full-width with a Cloud White background. Headline 'Zaufany partner w budowaniu' at 73px 247 grotesk weight 500, #000000. Subheading 'skalowalnych systemów.' at 73px 247 grotesk weight 400, #808080. Ensure `sectionGap` is used above and below for ample spacing.
2. **Generate a Primary Button**: Text 'Poznaj usługi' in 247 grotesk weight 400, color #ffffff. Button background #000000. Border-radius 0px. Padding 0px top/bottom, 30.08px left/right.
3. **Design a text content block**: Place body text 'Sprawdź :' in Ntbau weight 400, #000000, 14px size, 1.20 lineHeight, 0.0190em letter-spacing. Following it, a link 'Brand Growth Workshop' in 247 grotesk weight 400, #000000, 14px size.
4. **Create a client logo grid**: Use transparent cards (rgba(0,0,0,0), radius 0px, no shadow) as containers for client logos. Arrange them in a grid using a high `columnGap` and `rowGap` (e.g., 15px-16px) to maintain the spacious layout, with logos in #808080 color.
5. **Build a horizontal navigation item**: Text 'USŁUGI' in 247 grotesk weight 500, #000000, 16px size, 1.25 lineHeight. No underline or background, relying on typography and spacing for clarity.

---
_Source: https://styles.refero.design/style/c43237e3-dae2-4fc6-a917-519b25c870e5_
