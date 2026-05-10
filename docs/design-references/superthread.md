# Superthread — Design Reference

> Whiteboard with vivid highlighter

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.superthread.com](https://www.superthread.com) |
| Refero page | [https://styles.refero.design/style/aa87c258-0eb8-4c14-9f2f-96f16fb926b8](https://styles.refero.design/style/aa87c258-0eb8-4c14-9f2f-96f16fb926b8) |
| Theme | light |
| Industry | productivity |

## Overview

Superthread employs a focused, productive canvas aesthetic: a light, near-white background paired with substantial dark typography. Interactions are defined by crisp borders and a singular vivid yellow accent color, signaling active states and primary calls to action through underlays and button fills. The design prioritizes visual clarity and information density, using soft, contained cards and minimal shadows to structure content without added visual weight. Typography is the workhorse, providing structure and clear hierarchy.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Stormy Night | `#1d2939` | neutral | Primary text, dark surface backgrounds, button background, critical borders – provides strong contrast against light canvas |
| Midnight Ink | `#101828` | neutral | Hero headlines, critical body text – for ultimate emphasis |
| Cadet Gray | `#667085` | neutral | Secondary text, muted headings, placeholder text |
| Pale Indigo | `#1d2a53` | accent | Violet outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Superthread Yellow | `#ffaa00` | brand | Yellow accent for outlined action borders, linked labels, and lightweight interactive emphasis. Do not promote it to the primary CTA color |
| Cool Stone | `#475467` | neutral | Subtle background for UI elements and decorative fills |
| Canvas White | `#f9fafb` | neutral | Page background, primary surface |
| Pure White | `#ffffff` | neutral | Card backgrounds, elevated UI surfaces, button text on dark backgrounds |
| Ash Mist | `#e4e7ec` | neutral | Subtle button backgrounds, light borders, subtle dividers |
| Cloud Gray | `#F2F4F7` | neutral | Subtle background for alternating sections or input fields |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 12px, 16px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### Vela Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px, 32px, 80px |
| lineHeight | 1.10, 1.20, 1.33, 1.40, 1.50, 1.70 |
| letterSpacing | -0.01, 0.02 |
| fontFeatureSettings | 'blwf', 'cv03', 'cv04', 'cv09', 'cv11' |
| substitute | Inter, 'Helvetica Neue', Arial, sans-serif |
| role | Primary headings and subheadings, brand elements. Distinctive letter-spacing and varied weights give it a confident, modern feel. Semibold at 80px for display, regular and medium for subheadings and body. Includes unique font features for character variation and precise control. |

### Noto Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 16px, 18px |
| lineHeight | 1.40 |
| letterSpacing | 0 |
| substitute | Open Sans, sans-serif |
| role | Supporting body text and informational content where a reliable, legible sans-serif is needed. |

### SF Pro Display

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 12px, 16px |
| lineHeight | 1.2 |
| role | SF Pro Display — detected in extracted data but not described by AI |

### gs-inter-2020-11

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| sizes | 12px, 15px |
| lineHeight | 1.2 |
| role | gs-inter-2020-11 — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 1.2 |  |
| body-lg | 18 |  | 1.4 |  |
| subheading | 20 |  | 1.4 |  |
| heading | 24 |  | 1.33 |  |
| heading-lg | 32 |  | 1.2 |  |
| display | 80 |  | 1.1 | -0.8 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| small | 4px |
| default | 10px |
| cardLarge | 16px |
| buttonPill | 50px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 25px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#f9fafb` | 0 | Primary page background |
| Pure White | `#ffffff` | 1 | Default card backgrounds and main content panels |
| Cloud Gray | `#F2F4F7` | 2 | Alternate section backgrounds or subtle content containers |

## Components

### Primary Action Button

**Role:** Main call to action button.

Filled with Superthread Yellow (#ffaa00), text in Stormy Night (#1d2939), 10px border radius, 13px vertical padding, 20px horizontal padding. Typography: Vela Sans, weight 600.

### Secondary Ghost Button

**Role:** Outlined button for secondary actions.

Transparent background, text in Stormy Night (#1d2939), 1px border in Stormy Night (#1d2939), 8px border radius, 8px vertical padding, 16px horizontal padding. Typography: Vela Sans, weight 600.

### Tertiary Tab Button

**Role:** Buttons for filtering or switching views, often in groups.

Ash Mist (#e4e7ec) background, text in Stormy Night (#1d2939), 10px border radius, 13px vertical padding, 20px horizontal padding. Typography: Vela Sans, weight 600.

### Navigation Link

**Role:** Interactive text link in navigation.

Text in Stormy Night (#1d2939), with an active state signified by a 3px solid underline in Superthread Yellow (#ffaa00).

### Elevated Content Card

**Role:** Used for featured content or panels requiring emphasis.

Pure White (#ffffff) background, 10px border radius, with a subtle box-shadow: rgba(0, 0, 0, 0.18) 0px 0.6px 0.6px -1.25px, rgba(0, 0, 0, 0.16) 0px 2.3px 2.3px -2.5px, rgba(0, 0, 0, 0.06) 0px 10px 10px -3.75px. Internal padding varies from 24px-25px.

### Muted Background Card

**Role:** Used for grouping related content with less visual prominence.

Transparent background with a slight tint from rgba(152, 162, 179, 0.07), 10px border radius, no shadow. Padding 25px on all sides.

### Hero Section Tab

**Role:** Interactive tab for feature selection in the hero section.

Stormy Night (#1d2939) background for active state, Pure White (#ffffff) text, 12px border radius. Inactive tabs are transparent with Stormy Night text.

## Layout

The page primarily uses a contained layout with content centered within a logical maximum width (though not explicitly defined, sections imply a consistent inner container). The hero section is full-width with a centered headline and subheading, followed by interactive tabs. Content sections are typically alternating bands of Canvas White and Cloud Gray, employing a text-left/image-right or centered stack pattern. Card grids are used for features, showcasing multiple content blocks in an organized fashion. The vertical rhythm relies on consistent sectionGap values for clear separation, while navigation is a sticky top bar.

## Imagery

This system features a mix of product screenshots and abstract graphics. Product screenshots are typically contained within cards with rounded corners, showcasing the UI directly. Imagery acts as direct contextual evidence or provides a clean aesthetic, rather than heavy decorative elements. Icons are typically single-color, filled style, using Stormy Night (#1d2939) or Cadet Gray (#667085), with a moderate stroke weight. The overall density is balanced, with imagery serving an explanatory or showcase role alongside text-dominant layouts.

## Dos & Donts

### Do

- Prioritize text in Stormy Night (#1d2939) or Midnight Ink (#101828) against Canvas White (#f9fafb) or Pure White (#ffffff) for readability.
- Use Superthread Yellow (#ffaa00) exclusively for primary calls to action, active navigation states, and headline underlines to maintain focus.
- Apply a default border radius of 10px to most interactive elements and cards for a consistent, soft-edged feel.
- Maintain consistent vertical spacing between sections using 'sectionGap' at 40px for a clean flow.
- Employ Vela Sans for all headings and brand-critical text, leveraging its specific letter-spacing settings and alternative glyphs ('blwf', 'cv03', 'cv04', 'cv09', 'cv11').
- Use a default elementGap of 10px for internal spacing within components and between closely related elements.
- Structure content with either Elevated Content Cards (Pure White, #ffffff, with shadow) or Muted Background Cards (transparent tinted, no shadow) based on required prominence.

### Don't

- Do not introduce new vivid colors; keep the palette disciplined around neutrals, Superthread Yellow, and hints of Pale Indigo.
- Avoid heavy shadows or gradients on surfaces except for the defined Elevated Content Card shadow style.
- Do not deviate from the specified typography families; avoid system fonts for headings and brand elements where Vela Sans is intended.
- Do not overuse Superthread Yellow; reserve it for truly primary interactions to prevent visual noise.
- Avoid arbitrary border-radius values; stick to the defined 4px, 10px, 16px, or 50px tokens.
- Do not use dark backgrounds for large content blocks; the system is optimized for a light canvas.
- Do not use letter-spacing on Noto Sans or other body text; reserve for Vela Sans headlines as specified.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #1d2939 (Stormy Night)
- background: #f9fafb (Canvas White)
- border: #1d2939 (Stormy Night)
- accent: #1d2a53 (Pale Indigo)
- primary action: #1d2939 (filled action)

### 3-5 Example Component Prompts
1. **Create a Hero Headline:** Centered text "Where work gets done" in Midnight Ink (#101828), Vela Sans SemiBold, size 80px, lineHeight 1.1, letter-spacing -0.01em. Underline 'done' with a 3px solid Superthread Yellow (#ffaa00) border. Include a subheading below it in Stormy Night (#1d2939), Vela Sans Regular, size 24px, lineHeight 1.33.
2. Create a Primary Action Button: #1d2939 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
3. **Design an Interactive Tab Group:** Two tabs. Active tab: Stormy Night (#1d2939) background, Pure White (#ffffff) text, Vela Sans SemiBold, size 16px, borderRadius 12px. Inactive tab: transparent background, Stormy Night (#1d2939) text, Vela Sans Regular, size 16px, borderRadius 12px. Both use 8px vertical padding, 16px horizontal padding.
4. **Create an Elevated Content Card:** Pure White (#ffffff) background, 10px border radius, with box-shadow rgba(0, 0, 0, 0.18) 0px 0.6px 0.6px -1.25px, rgba(0, 0, 0, 0.16) 0px 2.3px 2.3px -2.5px, rgba(0, 0, 0, 0.06) 0px 10px 10px -3.75px. Internal padding of 25px. Headline inside card: Stormy Night (#1d2939), Vela Sans SemiBold, size 24px, lineHeight 1.33.

---
_Source: https://styles.refero.design/style/aa87c258-0eb8-4c14-9f2f-96f16fb926b8_
