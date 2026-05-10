# Memorisely — Design Reference

> Whiteboard Clarity, focused on sharp text and understated components.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.memorisely.com](https://www.memorisely.com) |
| Refero page | [https://styles.refero.design/style/e1497817-b9f1-4d7a-842f-58a08dd9e455](https://styles.refero.design/style/e1497817-b9f1-4d7a-842f-58a08dd9e455) |
| Theme | light |
| Industry | design |

## Overview

Memorisely presents a clean, productivity-focused interface with a strong sense of clarity. The visual language emphasizes crisp typography, a muted, near-achromatic palette, and subtle elevation primarily through soft, almost invisible borders and minimal shadows. Component shapes lean towards softened rectangles and generous 'pill' forms, creating an approachable yet structured feel. The design system prioritizes content legibility and functional interaction over decorative flair, with a sparse use of color limited to text links and one distinct button style that appears as a muted accent.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Primary page and card backgrounds, button fills |
| Charcoal Black | `#000000` | neutral | Primary text, strong headings, icon fills |
| Paper White | `#faf9f6` | neutral | Secondary page backgrounds, subtle card surfaces |
| Smoke Gray | `#171717` | neutral | Heavy text, darker button backgrounds. Despite its dark value, it appears as more of a surface color due to context |
| Ghost Ivory | `#f2f0e9` | neutral | Subtle border colors, secondary background distinction |
| Muted Stone | `#878787` | neutral | Secondary text, descriptive body copy |
| Steel Gray | `#6e6e6e` | neutral | Muted text, hairline borders, ghost button borders |
| Deep Graphite | `#414141` | neutral | Subheadings, slightly darker body text |
| Ink Black | `#212121` | neutral | Mid-dark body text, often for descriptive labels |
| Soft Pewter | `#c9c6bd` | neutral | Decorative illustration fill, subtle inactive states |
| Ocean Blue | `#0000ee` | accent | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 10px, 11px, 13px, 14px, 15px, 16px, 18px, 22px, 32px, 56px |
| lineHeight | 1.14, 1.23, 1.25, 1.27, 1.33, 1.38, 1.40, 1.43, 1.44, 1.45, 1.50 |
| letterSpacing | -1.68px at 56px, -1.28px at 32px, -0.48px at 16px |
| substitute | system-ui, sans-serif |
| role | The primary typeface for all content, from bold headlines to fine print. It maintains readability across various sizes and weights, with tighter tracking for larger display text and normal spacing for body text, creating a modern and efficient feel. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | system-ui, sans-serif |
| role | Fallback for system-level UI elements where specific font styling is less critical, ensuring broad compatibility. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.43 |  |
| body | 16 |  | 1.5 | -0.48 |
| subheading | 22 |  | 1.45 |  |
| heading | 32 |  | 1.43 | -1.28 |
| display | 56 |  | 1.14 | -1.68 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 100px |
| cards | 12px |
| small | 4px |
| medium | 8px |
| buttons | 24px |

- **elementGap** — 10px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Base page background, brightest surface for cards and buttons. |
| Paper White | `#faf9f6` | 2 | Secondary page and section backgrounds, providing subtle visual breaks. |
| Ghost Ivory | `#f2f0e9` | 3 | Subtle differentiators for borders or very light accents. |

## Components

### Primary Filled Button

**Role:** Main call-to-action button, dark and commanding.

Background: #171717, Text: #ffffff, Border Radius: 24px, Padding: 16px 16px. Typography: Inter 400.

### Outline Ghost Button

**Role:** Secondary action or subtle interaction button.

Background: rgba(255, 255, 255, 0), Text: #0000ee, Border: 1px solid #0000ee, Border Radius: 10px, Padding: 6px 12px. Typography: Inter 400.

### Standard Card

**Role:** Content containers for features or listings.

Background: #ffffff, Border Radius: 12px, Box Shadow: none, Padding: 16px. No visible borders, relying on background contrast.

### Hero Card

**Role:** Prominent card for showcasing key information.

Background: #faf9f6, Border Radius: 44px 44px 0px 0px (top rounded), Box Shadow: none, Padding: 40px top/right/left. Content is full width at the bottom.

### Navigation Link

**Role:** Interactive text link within navigation menus.

Text: #0000ee (for active/interactive state), otherwise #000000, Underline appears on hover/active. Typography: Inter 400.

### Small Pill Button

**Role:** Compact interactive element, often for tags or minor actions.

Background: #ffffff, Text: #0000ee, Border: 1px solid #0000ee, Border Radius: 100px, Padding: 8px 20px. Typography: Inter 400.

## Layout

The page primarily follows a contained layout with content organized into distinct sections featuring consistent vertical spacing. The hero section is full-width with a centered headline and subtext over a plain background. Content typically alternates between stacked blocks and two-column layouts featuring text on one side and a visual on the other. Navigation is a simple top bar, with internal links leading to specific sections. The density is moderate to compact, with clear separation between content blocks.

## Imagery

Imagery primarily consists of tight product shots or candid-style photography of people engaged in work, often featuring screens or AI-related tools. These visuals are typically contained within cards with rounded corners, rather than full-bleed. Icons are outlined, minimal, and monochromatic, with a soft grey fill for decorative elements. The overall visual tone is explanatory and product-focused, with images supporting the textual content rather than dominating the layout.

## Dos & Donts

### Do

- Use Inter for all typography, adjusting letter-spacing based on size: -1.68px for 56px text, -1.28px for 32px, and -0.48px for 16px.
- Maintain a subtle, near-achromatic palette, using Canvas White (#ffffff) for primary surfaces and Paper White (#faf9f6) for secondary backgrounds.
- Apply Charcoal Black (#000000) for primary headlines and body text to ensure strong contrast.
- Utilize Ocean Blue (#0000ee) exclusively for interactive text links and ghost button borders, making it the primary accent color.
- Employ a 24px border-radius for primary buttons like Browse bootcamps, and a generous 100px radius for compact pill-shaped elements.
- Prefer soft, contained vertical spacing: 10px for element gaps, 16px for card padding, and approximately 64px for section breaks.
- Elevate components via subtle shadow strokes like `rgba(110, 110, 110, 0.1) 0px 0px 0px 1px, rgba(110, 110, 110, 0.2) 0px 1px 0px 0px` rather than heavy dropshadows.

### Don't

- Do not introduce new saturated colors outside of the defined Ocean Blue (#0000ee) accent.
- Avoid strong, bold decorative borders on cards; prefer subtle background color shifts or nearly invisible hair-line strokes.
- Do not use dark backgrounds for large content blocks or primary navigation; the system is strongly light-themed.
- Refrain from heavy drop shadows; prefer very subtle, single-direction shadows and thin borders for visual separation.
- Do not use varied typefaces; Inter is the sole brand font, with sans-serif as a system fallback.
- Avoid overtly decorative imagery; prefer tightly cropped product or human shots, or clean, minimal icons.
- Do not create very sharp, angular components; prioritize soft, rounded corners with radii of 8px, 12px, or 24px.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
- text: #000000
- background: #ffffff
- border: #6e6e6e
- accent: #0000ee
- primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Design an Outline Ghost Button: Background rgba(255, 255, 255, 0), text #0000ee, border 1px solid #0000ee, border-radius 10px, padding 6px vertically, 12px horizontally. Text: Inter 400.
3. Build a Standard Card: Background #ffffff, border-radius 12px, no box-shadow, padding 16px all sides. Add text using Charcoal Black (#000000) and Muted Stone (#878787).
4. Assemble a Hero Section Headline: Text 'Actionable AI Design Engineering' using Charcoal Black (#000000), Inter 56px, line-height 1.14, letter-spacing -1.68px. Below it, a subtext 'We help product designers become design engineers' using Muted Stone (#878787), Inter 18px, line-height 1.4.

---
_Source: https://styles.refero.design/style/e1497817-b9f1-4d7a-842f-58a08dd9e455_
