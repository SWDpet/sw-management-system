# Dennis Snellenberg — Design Reference

> midnight command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://dennissnellenberg.com](https://dennissnellenberg.com) |
| Refero page | [https://styles.refero.design/style/28e8e762-8d8c-4e88-84ed-858f9917cb58](https://styles.refero.design/style/28e8e762-8d8c-4e88-84ed-858f9917cb58) |
| Theme | dark |
| Industry | design |

## Overview

Dennis Snellenberg's design system uses a confident, dark-mode aesthetic with a strong emphasis on spaciousness and precise typography. Surfaces are predominantly dark, with crisp white text providing high contrast. A single vivid violet hue serves as the primary brand accent, thoughtfully deployed to highlight interactive elements and create moments of visual interest. The overall impression is one of meticulous craft and understated digital elegance.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#1c1d20` | neutral | Page background, primary text, prominent borders for both dark and light surfaces, and neutral interactive states |
| Frosted Glass | `#ffffff` | neutral | Primary text on dark backgrounds, secondary background surfaces, and borders that delineate content on darker cards |
| Neutral Stone | `#999d9e` | neutral | Muted background elements, serving as a softer dark surface |
| Graphite Shadow | `#494a4d` | neutral | Subtle background surfaces, card shadows, suggesting depth without high contrast |
| Vivid Violet | `#455ce9` | brand | Main accent color for interactive elements like links and card backgrounds, drawing attention to clickable areas and brand-specific content |
| Deep Violet | `#334bd3` | brand | A darker shade of violet for subtle variations in accent elements or states |

## Typography

### Dennis Sans

| Key | Value |
| --- | --- |
| weight | 450 |
| weights | 450 |
| sizes | 10px, 12px, 14px, 15px, 17px, 33px, 60px, 76px, 88px, 216px |
| lineHeight | 1.00, 1.06, 1.07, 1.20, 1.40, 1.45, 1.60, 1.66 |
| letterSpacing | 0.0500em |
| substitute | Inter |
| role | The single typeface for all interface elements, from large display headings to body text. Its consistent weight and geometric clarity establish a strong, modern, and readable foundation across the system, contributing to the site's confident voice. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.66 | 0.05 |
| body-lg | 15 |  | 1.06 | 0.05 |
| heading-sm | 33 |  | 1.45 | 0.05 |
| heading | 60 |  | 1.2 | 0.05 |
| heading-lg | 76 |  | 1 | 0.05 |
| display | 216 |  | 1 | 0.05 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| body | 10px |
| links | 36.72px |

- **elementGap** — 12px
- **sectionGap** — 
- **cardPadding** — 
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Base | `#1c1d20` | 0 | Primary background for the entire page, providing a foundational dark canvas. |
| Card Surface | `#494a4d` | 1 | Slightly elevated background for cards or distinct content blocks. |
| Accent Surface | `#334bd3` | 2 | A vivid violet background for interactive elements or highlighted cards. |
| Highlighted Accent Surface | `#455ce9` | 3 | A brighter vivid violet for primary interactive elements, standing out boldly. |
| Muted Background | `#999d9` | 4 | A lighter neutral background for specific sections or elements requiring less contrast. |

## Components

### Located Tag

**Role:** Informational tag showing location context.

Background: Midnight Ink (#1c1d20). Text: Frosted Glass (#ffffff). Padding: 17px vertical, 18px horizontal. Border radius: 36.72px. Contains an icon.

### Navigation Link

**Role:** Primary navigation item in headers and footers.

Text: Frosted Glass (#ffffff) on dark backgrounds, Midnight Ink (#1c1d20) on light. Padding: 8px vertical. Margin: 12px horizontal. Border radius: 36.72px for interactive states, often exhibiting an inset white shadow `rgba(255, 255, 255, 0.2) 0px 0px 0px 1px inset` on hover/focus.

### Circular Brand Card - Vivid Violet

**Role:** Highlighting specific project categories or services with brand color.

Background: Vivid Violet (#455ce9). Border radius: 50%. No box shadow. Padding: 0px.

### Circular Brand Card - Deep Violet

**Role:** Supporting brand elements with a darker variation of the accent.

Background: Deep Violet (#334bd3). Border radius: 50%. No box shadow. Padding: 0px.

### Circular Neutral Card - Midnight Ink

**Role:** General purpose neutral card for categorization or grouping.

Background: Midnight Ink (#1c1d20). Border radius: 50%. No box shadow. Padding: 0px.

### Circular Neutral Card - Frosted Glass

**Role:** Highlighting elements on a dark background where full contrast is desired.

Background: Frosted Glass (#ffffff). Border radius: 50%. No box shadow. Padding: 0px.

## Layout

The page exhibits a max-width contained layout, approximately 1200px, with content centered. The hero section is full-bleed, featuring a person offset to the right against a gray background with large typography on the left. Section rhythm is implied by strong negative space and the dark theme, rather than distinct alternating bands. Content arrangement seems to favor asymmetric compositions with large typographic elements balanced by visual components. Navigation is a minimalist top-right header, indicating a non-sticky, simple structure.

## Imagery

The site primarily uses photography, featuring a candid product shot of the brand's owner on a neutral gray background. The imagery is product-focused (the person as the 'product' representation) with a clean, unadorned treatment. Icons are minimal, represented by a clear, outlined style for global context (globe icon). Density is low, with imagery serving as a focal point rather than information conveyance, leaving ample space for typography.

## Dos & Donts

### Do

- Prioritize Midnight Ink (#1c1d20) for primary page backgrounds and dark surface elements, establishing the core theme.
- Use Frosted Glass (#ffffff) for all main text on dark backgrounds and as a crisp accent for elements requiring high contrast.
- Apply Vivid Violet (#455ce9) sparingly for interactive elements, links, and card backgrounds to draw attention and reinforce brand identity.
- Ensure generous padding and spacing, using 18px as a consistent vertical and horizontal padding for most major content blocks and 12px for smaller element gaps.
- Utilize the Dennis Sans font with its consistent weight 450 across all text, relying on size changes for hierarchical differentiation.
- Employ a border radius of 36.72px for interactive links and tags, creating a distinct pill-like shape, contrasting with 10px for more structural body elements.
- Implement an inset shadow `rgba(255, 255, 255, 0.2) 0px 0px 0px 1px inset` for interactive link states to provide subtle feedback without heavy elevation.

### Don't

- Avoid using multiple accent colors; Vivid Violet (#455ce9) is the sole chromatic accent.
- Do not introduce heavy drop shadows or strong skeuomorphic elements; use subtle inset borders or background color shifts for depth.
- Refrain from using overly bold or light font weights, as Dennis Sans weight 450 is the primary typographic expression.
- Do not clutter layouts with dense information; maintain comfortable spacing values (12px element gap, 18px padding for sections) to ensure readability.
- Avoid arbitrary border radii; stick to the specified 36.72px for interactive elements and 10px for structural containers.
- Do not use Frosted Glass (#ffffff) as a full-page background; its primary role is for text and elevated surfaces on dark pages.

## Notes

### Agent Prompt Guide

Quick Color Reference: text: #ffffff, background: #1c1d20, border: #1c1d20, accent: #455ce9, primary action: no distinct CTA color

Example Component Prompts:
1. Create a `Location Tag`: Midnight Ink (#1c1d20) background, Frosted Glass (#ffffff) text, 17px vertical and 18px horizontal padding, 36.72px border radius, with a Frosted Glass globe icon.
2. Design a `Hero Headline`: Use Dennis Sans, size 216px, weight 450, Frosted Glass (#ffffff) color. Place on a Midnight Ink (#1c1d20) background.
3. Implement a `Navigation Link`: Use Dennis Sans, size 15px, weight 450, Frosted Glass (#ffffff) color, 8px vertical padding, 12px horizontal margin. On hover, apply `rgba(255, 255, 255, 0.2) 0px 0px 0px 1px inset` shadow and a transition of 0.3s ease.
4. Create a `Vivid Violet Circular Card`: Vivid Violet (#455ce9) background, 50% border radius, 0px padding. Place Frosted Glass (#ffffff) content inside.

---
_Source: https://styles.refero.design/style/28e8e762-8d8c-4e88-84ed-858f9917cb58_
