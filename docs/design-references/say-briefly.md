# Say Briefly — Design Reference

> Olive Canvas, Highlighted Briefs, Warm Accents

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://saybriefly.com](https://saybriefly.com) |
| Refero page | [https://styles.refero.design/style/8b91f4c9-74e5-4925-90a3-3dd31fd5725e](https://styles.refero.design/style/8b91f4c9-74e5-4925-90a3-3dd31fd5725e) |
| Theme | light |
| Industry | productivity |

## Overview

Say Briefly employs a light, airy canvas with a strong emphasis on a deep olive green for primary textual hierarchy and interactive elements, delivering a confident, grounded feel. Accents of warm yellow and soft pastels punctuate the monochromatic base to highlight key information and provide visual differentiation for cards and functional buttons. Its typography balances a bold, display-oriented headline font with a highly legible sans-serif for body text, creating a system that is both engaging and understated. Surfaces are mostly flat with minimal shadows, allowing color and text to carry the design.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#fcfaf5` | neutral | Page background, primary surface for most content |
| Deep Olive | `#1a3300` | brand | Primary text, major headings, borders, and main call-to-action buttons. This color defines the brand's core visual identity |
| Highlight Yellow | `#ffe95c` | accent | Highlighting key text sections, card backgrounds, and button text on Deep Olive backgrounds |
| Charcoal Black | `#000000` | neutral | Secondary text, fine print, and decorative SVG fills for visual depth |
| Soft Teal | `#a8e5e5` | accent | Decorative card backgrounds and distinct secondary buttons |
| Muted Sage | `#d5f5c2` | accent | Soft button backgrounds, subtle surface variations |
| Warm Orange | `#cb5521` | accent | Distinct card and section backgrounds, lending warmth |
| Pale Pink | `#f6d0ff` | accent | Decorative button backgrounds for visual variety |
| Light Gray | `#f1f1f1` | neutral | Minor text, subtle borders, and background accents where faint contrast is needed |
| Border Gray | `#b6b6b6` | neutral | Hairline borders, dividers, subtle UI outlines for structure |

## Typography

### Bricolage Grotesque

| Key | Value |
| --- | --- |
| weight | 800 |
| sizes | 55px, 66px, 90px |
| lineHeight | 1.00, 1.15, 1.20 |
| letterSpacing | 0.04em, 0.05em |
| substitute | system-ui |
| role | Hero headlines and major theatrical titles. Its heavy weight and generous letter-spacing create an impactful, editorial presence. |

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| sizes | 11px, 12px, 14px, 16px, 17px, 18px, 20px, 24px, 28px, 30px, 32px, 36px, 38px, 40px, 64px |
| lineHeight | 0.93, 1.03, 1.10, 1.20, 1.25, 1.30, 1.38, 1.40, 1.50, 1.56, 1.63 |
| substitute | Arial |
| role | Primary typeface for all body text, navigation, and most UI elements. Its broad range of weights handles information hierarchy across the interface. |

### Roboto Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 15px, 16px |
| lineHeight | 1.00, 1.33, 1.38 |
| substitute | monospace |
| role | Used for specific functional elements or data display, providing a distinct, technical contrast to the main sans-serif. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.3 |  |
| body-sm | 14 |  | 1.3 |  |
| body | 17 |  | 1.3 |  |
| body-lg | 18 |  | 1.3 |  |
| heading-sm | 24 |  | 1.3 |  |
| heading | 30 |  | 1.3 |  |
| heading-lg | 36 |  | 1.3 |  |
| display-sm | 38 |  | 1.3 |  |
| display | 64 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 6px |
| buttons | 6px |
| navItems | 16px |
| highlightedElements | 9999px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 16px
- **pageMaxWidth** — 1320px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#fcfaf5` | 0 | Primary page background, foundation for all content. |
| Soft Accent Surfaces | `#d5f5c2` | 1 | Subtle background for specific sections or button groups, providing a slight visual break. |
| Highlighted Cards | `#ffe95c` | 2 | Prominent content blocks that demand attention, or functional cards. |
| Decorative Thematic Cards | `#a8e5e5` | 3 | Distinct content containers using alternate brand accent colors for visual variety. |

## Components

### Navigation Link

**Role:** Text link within the top navigation.

Inter, weight 400, Deep Olive text. No background or padding, appearing as simple text. Nav border is #b6b6b6 with 16px radius.

### Primary Call-to-Action Button

**Role:** Main interactive element, driving user conversion.

Deep Olive background (#1a3300), Highlight Yellow text (#ffe95c), 6px border-radius, 12px vertical padding, 40px horizontal padding. Shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px.

### Secondary Outlined Button

**Role:** Less prominent interactive actions.

Transparent background, Deep Olive text (#1a3300), 2px Deep Olive border, 6px border-radius, 12px vertical padding, 19.14px horizontal padding. Shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px.

### Highlight Card

**Role:** Prominent information display, often with a subtle background color.

Highlight Yellow background (#ffe95c), 6px border-radius (top-left, bottom-left) or 16px border-radius (all sides), 48px top padding, 28px horizontal padding, 36px bottom padding. No shadow.

### Decorative Card - Warm Orange

**Role:** visually distinct sections or feature highlights.

Warm Orange background (#cb5521), 12px border-radius, 50px vertical padding, 120px horizontal padding. No shadow.

### Decorative Card - Soft Teal

**Role:** Visually distinct sections or feature highlights.

Soft Teal background (#a8e5e5), 16px border-radius, 56px vertical padding, 75px horizontal padding. No shadow.

### Subtle Accent Badge

**Role:** Small, informational tags.

Transparent background, Deep Olive text, 16px vertical and horizontal padding, 8px border-radius. Highlight Yellow background (#ffe95c) is used when the badge text is meant to be highlighted.

## Layout

The page uses a maximum width of 1320px, with content centered. The hero section features a centered headline over a light background. Section rhythm is primarily defined by consistent vertical spacing, often with text-focused content blocks. Layouts include multi-column feature grids (seen in card variants) and standard stacked content for text-heavy areas. Navigation is a fixed top bar with simple text links and prominent right-aligned action buttons.

## Imagery

The imagery aesthetic is minimal and functional. It primarily features simple, line-drawn illustrations or subtle background patterns using the brand's core colors. When present, product screenshots are clean and focused. Icons are typically filled, utilizing Deep Olive on light backgrounds. Imagery serves decorative and explanatory roles without dominating the UI, maintaining a text-dominant layout.

## Dos & Donts

### Do

- Always use Deep Olive (#1a3300) for primary text and main call-to-action buttons.
- Apply 6px border-radius for buttons and typical cards, 16px for rounded sections and navigation elements, and 9999px for pill-shaped accents.
- Maintain a comfortable density with 16px for element gaps and minimum 40px for section gaps.
- Utilize Highlight Yellow (#ffe95c) for textual emphasis and as an active background color for cards and button text on dark backgrounds.
- Structure headlines with Bricolage Grotesque weight 800, using generous letter-spacing (0.04em-0.05em) and sizes 55px-90px.
- Prioritize flat design with minimal shadows; use rgba(0, 0, 0, 0.05) 0px 1px 2px 0px for subtle button elevation.
- Ensure all body text uses Inter font for readability, with weights varying from 300 to 700 to establish clear hierarchy.

### Don't

- Avoid using highly saturated colors for large background areas; stick to the light Canvas White or softer accent pastels.
- Do not introduce heavy drop shadows or complex gradients; the system relies on flat surfaces and color contrast.
- Do not deviate from the specified font families or their usage roles; maintain the distinct visual voice of Bricolage Grotesque for display and Inter for body.
- Avoid arbitrary border-radius values; adhere strictly to 6px, 16px, or 9999px for consistent shape language.
- Do not use dark backgrounds for entire page sections; the overall theme is light with occasional colored cards.
- Do not use generic gray for borders when Deep Olive or Border Gray exist for functional or neutral lines respectively.
- Avoid complex, multi-color illustrations; imagery should be clean, line-based, or product-focused to complement the UI.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #1a3300
- background: #fcfaf5
- border: #1a3300
- accent: #ffe95c
- primary action: #1a3300 (filled action)

Example Component Prompts:
1. Create a Hero Section: Background Canvas White (#fcfaf5). Main headline 'Project success starts with a clear brief.' using Bricolage Grotesque, weight 800, size 90px, letter-spacing 0.05em, Deep Olive (#1a3300) color. Highlight 'brief.' with a solid Highlight Yellow (#ffe95c) background under the text. Subtext 'Turn your client calls or meetings into organized summaries...' using Inter, weight 400, size 18px, Charcoal Black (#000000) color, line-height 1.5.
2. Create a Primary Action Button: #1a3300 background, #fcfaf5 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
3. Create a Navigation Item: 'Product' with transparent background, Deep Olive (#1a3300) text, Inter font, weight 400. No padding/border, appearing as plain text. The overall navigation bar has a Border Gray (#b6b6b6) border and 16px border-radius.
4. Create a Highlight Card: Background Highlight Yellow (#ffe95c), 16px border-radius, 32px top padding, 75px horizontal padding, 0px bottom padding. Text content using Deep Olive (#1a3300), Inter weight 600, size 30px.

---
_Source: https://styles.refero.design/style/8b91f4c9-74e5-4925-90a3-3dd31fd5725e_
