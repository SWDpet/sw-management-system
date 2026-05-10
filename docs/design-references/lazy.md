# Lazy — Design Reference

> Subdued command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://lazy.so](https://lazy.so) |
| Refero page | [https://styles.refero.design/style/2b939c70-c08e-4bb6-8fac-ade99b0d1cf0](https://styles.refero.design/style/2b939c70-c08e-4bb6-8fac-ade99b0d1cf0) |
| Theme | dark |
| Industry | productivity |

## Overview

Lazy operates with a 'stealth dark mode' aesthetic, prioritizing content through deep, muted surfaces and delicate typography. A subtle play of light against dark is achieved through soft, localized gradients and sharp, clean borders. The overall impression is one of quiet efficiency and focused productivity, where UI elements recede until interaction, brought to life by crisp text and carefully applied elevation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#0a0a0a` | neutral | Deepest surface background, text on light elements, subtle shadows |
| Carbon Gray | `#26272c` | neutral | Primary page background, card backgrounds |
| Slate Surface | `#1a1b1f` | neutral | Elevated card backgrounds, input fields |
| Ash Gray | `#3b3c3e` | neutral | Subtle elevated surface (e.g., input active state) |
| Ghost Border | `#8a8c93` | neutral | Medium-contrast borders, control outlines, and structural separators. Do not promote it to the primary CTA color |
| Whisper Gray | `#62646a` | neutral | Secondary text, link borders, icon strokes |
| Muted Text | `#535355` | neutral | Placeholder text, very subtle body text |
| Callout Text | `#e0e0e0` | neutral | Prominent body text, card content text |
| Pure White | `#ffffff` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Cloud Burst Gradient Primary | `#b8b8b8` | accent | Decorative gradients from near-white to medium gray |
| Deep Space Gradient | `#6c6d77` | accent | Subtle background gradients for panels, header backgrounds |
| Graphite Gradient | `#8c8c93` | accent | Background gradients for large feature sections |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600 |
| sizes | 11px, 12px, 13px, 14px, 16px, 20px, 24px, 28px, 55px |
| lineHeight | 1.09, 1.10, 1.20, 1.23, 1.25, 1.38, 1.43, 1.50, 1.57, 1.60, 1.83, 2.00 |
| letterSpacing | -0.045em at 55px, -0.042em at 28px, -0.036em at 24px, -0.031em at 20px, -0.025em at 16px, -0.021em at 14px, -0.017em at 13px, -0.005em at 12px, -0.004em at 11px |
| substitute | system-ui, sans-serif |
| role | The primary typeface for all UI elements, body text, and most informational display. Its range of weights and precise letter-spacing contribute to the crisp, compact feel of the interface. |

### Migra

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 21px, 58px |
| lineHeight | 1.09, 1.25 |
| letterSpacing | 0.048em at 58px, 0.026em at 21px |
| substitute | serif |
| role | A decorative serif font used for select hero headlines and impactful statements. Its slightly wider tracking and unique character shapes provide a touch of distinctive elegance against the otherwise geometric UI. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 | -0.004 |
| heading | 20 |  | 1.25 | -0.031 |
| heading-lg | 24 |  | 1.23 | -0.036 |
| display | 58 |  | 1.09 | 0.048 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 48px |
| badge | 22px |
| cards | 8px |
| input | 4px |
| buttons | 4px |
| formControl | 22px |

- **elementGap** — 14px
- **sectionGap** — 40px
- **cardPadding** — 14px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Base Canvas | `#26272c` | 0 | Main page background, primary canvas for content display. |
| Input / Elevated Base | `#1a1b1f` | 1 | Default background for interactive inputs and slightly elevated content blocks. |
| Interactive Card | `#3b3c3` | 2 | Background for interactive cards, receiving subtle shadow and border. |
| Overlay / Gradient Start | `#ffffff` | 3 | Used primarily as a starting point for gradients that lighten toward the top, or for highlight elements against very dark backgrounds. |

## Components

### Input Field

**Role:** Primary interactive input element

Background #1a1b1f, border 1px solid #8a8c93, text Pure White. Subtle blur(10px) backdrop effect.

### Elevated Tool Card

**Role:** Interactive content container with distinction

Background #3b3c3, border 1px solid #8a8c93, radius 8px, padding 14px. Shadow: `rgba(0, 0, 0, 0.12) 0px 12px 12px 0px`.

### Primary Ghost Button

**Role:** Call to action or navigation link

Text Pure White (or #e0e0e0) on transparent background, border 1px solid #8a8c93, radius 4px, padding 12px horizontal. Hover applies a gradient background.

### Product Feature Card

**Role:** Neutral content display card

Background transparent, radius 8px, no explicit border on background. Text Callout Text (#e0e0e0) with supporting text in Whisper Gray (#62646a).

### Pill Badge

**Role:** Categorization or short label

Background transparent, text Muted Text (#535355), border 1px solid #535355, radius 22px, padding varies 3px-6px.

### Navigation Link

**Role:** Header navigation item

Text Whisper Gray (#62646a), interactive on hover/active with Pure White text. No distinct background or explicit padding, relies on elementGap for spacing.

## Layout

The page primarily employs a max-width 1200px contained layout, centered on a dark background. The hero section is full-bleed, featuring a grand centered headline over an atmospheric radial gradient. Subsequent sections maintain a consistent vertical rhythm with Carbon Gray (#26272c) backgrounds, interspersed with subtle gradients or slightly lighter surface treatments. Content sections often feature centered typographic stacks for headlines and subheadings, followed by a product screenshot or input simulation. Navigation is a minimal top bar, sticky at scroll, with primary links and a subtle login/signup action.

## Imagery

The imagery is sparse, focusing on abstract graphic elements like blurred, light-emitting circles and radial gradients that give a sense of depth and focus, akin to a lens flare or spotlight. Product screenshots, when present, are tightly cropped, presented with a slight perspective, and encased within UI elements, never full-bleed. Icons are minimal, outlined, and monochromatic, maintaining a subtle presence. The visual language emphasizes UI over decorative graphics, with any non-UI visuals serving to enhance atmosphere rather than convey explicit information.

## Dos & Donts

### Do

- Use Carbon Gray (#26272c) as the default background for all new sections.
- Apply Ghost Border (#8a8c93) for hairline separation and subtle element distinction.
- Headlines should leverage Migra 500 for high impact, with letter-spacing 0.048em at 58px and 0.026em at 21px.
- All interactive elements like buttons and inputs should have a border-radius of 4px.
- For card backgrounds, use Slate Surface (#1a1b1f) or Ash Gray (#3b3c3e) and apply a 1px solid #8a8c93 border.
- Text hierarchy should progress from Pure White (#ffffff) for primary headers to Callout Text (#e0e0e0) for body, and Whisper Gray (#62646a) or Muted Text (#535355) for secondary/helper text.
- Ensure large headlines and display text receive negative letter-spacing from Inter, e.g., -0.045em at 55px, for visual density.

### Don't

- Do not introduce highly saturated colors for functional UI elements; reserve them for subtle decorative gradients or brand accents.
- Avoid heavy drop shadows or strong visual separation between elements; prefer thin borders and subtle background shifts.
- Do not use generic system fonts; Inter and Migra are critical for the brand's typographic identity.
- Do not use a default padding or margin of less than 6px for interactive elements; elementGap should be at least 14px.
- Do not use solid backgrounds for primary CTA buttons; they should rely on text, outlined borders, or subtle gradients.
- Avoid large blocks of plain, uncommented text; text should be broken up with clear headings and thoughtful spacing.
- Do not use bright white as a general text color on dark backgrounds without specific contrast considerations; Callout Text (#e0e0e0) is preferred for most body content.

## Notes

### Agent Prompt Guide

Quick Color Reference:text: #ffffff, background: #26272c, border: #8a8c93, accent: #6c6d77, primary action: no distinct CTA color
Example Component Prompts:
Create a hero section: radial gradient background starting from Ghost Border, with a centered headline: Migra 58px, #ffffff, letter-spacing 0.048em, line-height 1.09. Subheading: Inter 24px, #e0e0e0, letter-spacing -0.036em, line-height 1.23.
Create an input field: background #1a1b1f, border 1px solid #8a8c93, radius 4px, padding 14px. Placeholder text #535355. Text #ffffff.
Create an elevated tool card: background #3b3c3e, border 1px solid #8a8c93, radius 8px, padding 14px. Shadow: `rgba(0, 0, 0, 0.12) 0px 12px 12px 0px`. Body text #e0e0e0, caption text #62646a.
Create a ghost navigation link: text #62646a, Inter 14px weight 400, no background or border. On hover, text becomes #ffffff.
Create a pill badge: background transparent, border 1px solid #535355, radius 22px, padding 6px horizontal. Text #535355, Inter 12px weight 400.

---
_Source: https://styles.refero.design/style/2b939c70-c08e-4bb6-8fac-ade99b0d1cf0_
