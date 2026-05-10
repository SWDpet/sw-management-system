# 21 TSI — Design Reference

> Minimalist Cinematic Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://21tsi.com](https://21tsi.com) |
| Refero page | [https://styles.refero.design/style/7b28c99e-ed17-451b-a39e-bd4722bc43f5](https://styles.refero.design/style/7b28c99e-ed17-451b-a39e-bd4722bc43f5) |
| Theme | dark |
| Industry | other |

## Overview

21 TSI employs a bold, minimal dark aesthetic focusing on full-bleed imagery and stark typography. Interactivity is suggested through subtle hovers and outlines, reserving visual weight for large, impactful headlines. The system balances a comfortable density with strong visual statements, using a limited color palette to maintain a serious, focused tone. Motion is expressive, reinforcing dynamic content.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | neutral | Page backgrounds, element borders, default button borders — a true black canvas for high contrast |
| Canvas White | `#ffffff` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Iron Gray | `#4d4d4d` | neutral | Secondary borders and subtle background treatments, providing a slightly softer black for layered surfaces |
| Base Background | `#161a1c` | neutral | Primary background for sections and larger content blocks |
| Text Primary | `#ebf3f6` | neutral | Main body text, providing high readability against dark backgrounds |
| Text Secondary | `#aebbc5` | neutral | Muted text, helper text, and secondary information |
| Button Control | `#c2d0e0` | neutral | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |
| Button Hover | `#98a7b6` | neutral | Hover state for primary buttons, indicating interactivity |
| Secondary Button | `#242c31` | neutral | Background for secondary buttons, a darker, more subdued option |
| Secondary Button Hover | `#353d43` | neutral | Hover state for secondary buttons and subtle border highlights |
| Separator Line | `#222a30` | neutral | Divider lines and subtle structural borders |
| Toggle Off | `#525f6b` | neutral | Background color for off-state toggles |
| Toggle Knob | `#5f6b72` | neutral | Knob color for read-only toggles |
| Cookie Block | `#1e2428` | neutral | Background and border for cookie category blocks |

## Typography

### Saans

| Key | Value |
| --- | --- |
| weight | 300, 380, 570, 790 |
| weights | 300, 380, 570, 790 |
| sizes | 12px, 14px, 16px, 18px, 20px, 47px, 79px, 106px, 130px, 136px, 245px |
| lineHeight | 1.00, 1.11, 1.14, 1.15, 1.22, 1.30, 1.38, 1.40, 1.43 |
| letterSpacing | -0.0250em for large headlines, 0.0500em for smaller text. |
| substitute | Inter |
| role | Primary typeface across all elements. Its custom nature and very light weights (300, 380) for large display settings, balanced with heavier weights for emphasis, create a distinctively airy yet authoritative feel. Letter spacing is subtly adjusted for readability at varying scales: condensed for large headlines, normal for body text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.22 |  |
| body-sm | 14 |  | 1.22 |  |
| body | 18 |  | 1.22 |  |
| body-lg | 20 |  | 1.22 |  |
| heading-sm | 47 |  | 1.22 |  |
| heading | 79 |  | 1.22 |  |
| heading-lg | 130 |  | 1.22 |  |
| display-sm | 136 |  | 1.22 |  |
| display | 245 |  | 1.22 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 70px |
| default | 8px |
| circularElements | 594px |

- **elementGap** — 20px
- **sectionGap** — 30px
- **cardPadding** — 20px
- **pageMaxWidth** — 1350px

## Components

### Navigation Link

**Role:** Top-level navigation items.

Text in Canvas White (#ffffff) with 0px border-radius and 0px padding. Hover states are implied by text color change or subtle underlines.

### Outline Pill Button (Small)

**Role:** Interactive controls with a distinct pill shape.

Background transparent, text Canvas White (#ffffff), border 1px solid Absolute Zero (#000000). Border-radius 70px. Padding 7px top, 20px right, 8px bottom, 35px left.

### Outline Pill Button (Medium)

**Role:** Primary interactive controls with a distinct pill shape and contrasting outline.

Background transparent, text Canvas White (#ffffff), border 1px solid Canvas White (#ffffff). Border-radius 70px. Padding 10px top, 20px right, 10px bottom, 20px left.

### Text Link

**Role:** Inline textual links and footers.

Text in Canvas White (#ffffff) with no background or border.

## Imagery

The site heavily features high-quality, full-bleed photography, often with a blurred or motion-streaked effect against monochromatic backgrounds of deep reds or oranges. The subjects are typically human figures in dynamic poses, hinting at sports or activity, often silhouetted or with dramatic lighting to enhance mood over detailed clarity. Imagery serves as atmospheric backdrop rather than literal explanation, creating an immersive, cinematic feel. Icons, if present, are minimal and likely outlined, aligning with the clean UI.

## Dos & Donts

### Do

- Prioritize high contrast between text and background, typically using Canvas White (#ffffff) on Absolute Zero (#000000) or rich dark neutrals.
- Use Saans weight 300 or 380 for large headlines to maintain an airy, understated authority.
- Implement the 70px border-radius for all interactive buttons to create a consistent pill-shaped aesthetic.
- Maintain comfortable spacing with `elementGap` at 20px and `sectionGap` at 30px to prevent visual clutter within the dark theme.
- Apply `blur(15px)` for any background elements intended for atmospheric effect or content obscuration.
- For all interactive elements, ensure smooth transitions over 0.2s to 0.6s with `ease` timing functions.
- All page content should be contained within a `pageMaxWidth` of 1350px, ensuring a focused content area.

### Don't

- Avoid using highly saturated colors for anything other than specific, small accent elements, as they contrast with the minimalist dark palette.
- Do not use generic square buttons; always apply the 70px border-radius for pill shapes or 0px for minimal, unadorned links.
- Do not dilute the stark contrast; avoid low-contrast text on dark backgrounds that isn't explicitly intended as secondary or muted.
- Avoid excessive use of borders; borders should be minimal, 1px thin, and either match the background or provide a subtle highlight (e.g., Canvas White or Iron Gray).
- Do not break the expressive animation philosophy; transitions should primarily involve `transform`, `opacity`, and `filter`, with non-linear `cubic-bezier` timing functions for dynamic movement.
- Do not introduce unnecessary shadows; the design relies on flat planes and subtle blurs for depth.
- Avoid breaking the established letter-spacing; use condensed spacing for larger headlines and normal for body text as per Saans typeface specification.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #ebf3f6
- background: #000000
- border: #ffffff
- accent: no distinct accent color
- primary action: no distinct CTA color

Example Component Prompts:
- Create a minimal navigation link: 'THE SPHERE LAB' in Saans weight 380, Canvas White (#ffffff) text, no background or border, 0px padding.
- Create a small outlined pill button for contact: 'CONTACT' in Saans weight 380, Canvas White (#ffffff) text, transparent background, 1px solid Canvas White (#ffffff) border, 70px border-radius, 7px top padding, 20px right padding, 8px bottom padding, 35px left padding.
- Create a section headline: 'Everyone Hates Change But...' in Saans weight 790, Canvas White (#ffffff) text (size will vary based on scale), with -0.0250em letter spacing, centered on a Base Background (#161a1c).
- Create a paragraph of body text: 'Global Revenue' in Saans weight 300, Text Primary (#ebf3f6) text, over a Base Background (#161a1c).

---
_Source: https://styles.refero.design/style/7b28c99e-ed17-451b-a39e-bd4722bc43f5_
