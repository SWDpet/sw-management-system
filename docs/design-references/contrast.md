# Contrast — Design Reference

> Vivid pulse on a static canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.getcontrast.io](https://www.getcontrast.io) |
| Refero page | [https://styles.refero.design/style/effca480-81fb-4b8f-ab9c-3aa8c219c82a](https://styles.refero.design/style/effca480-81fb-4b8f-ab9c-3aa8c219c82a) |
| Theme | light |
| Industry | saas |

## Overview

Contrast employs a clean, high-contrast visual system with energetic pops of vivid color on a predominantly achromatic canvas. Sharp, direct typography in black creates a confident and clear hierarchy. Components are lightweight and float subtly with soft shadows, prioritizing information and interaction over heavy ornamentation. The overall impression is one of modern, efficient clarity with a focused urgency for action.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Red Action | `#ff5065` | brand | Primary action buttons, active states, and focus indicators — generates urgency and draws immediate attention |
| Orange Highlight | `#ff7a59` | accent | Accent for headings, decorative icons, and subtle text highlights. Creates a secondary focal point with energetic warmth |
| Amber Detail | `#ff5c35` | accent | Used for smaller decorative elements, icons, and some text, adding a vibrant detail layer |
| Jet Black | `#000000` | neutral | Primary text, strong contrasts, icon fills, and defining borders. Provides anchor points in the bright UI |
| Canvas White | `#ffffff` | neutral | Base page backgrounds, card surfaces, and primary button text. Creates a spacious, bright environment |
| Ebony Text | `#0e0f10` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Slate Gray | `#2f3133` | neutral | Subtle card backgrounds, badge backgrounds, and tertiary text or borders |
| Whisper Gray | `#f4f4f8` | neutral | Subtle background for sections, creating visual separation without strong contrast |
| Dark Neutral Button | `#1c1d1e` | neutral | Background for secondary or ghost buttons, offering a strong alternative to the primary Red Action |
| Stone Text | `#7a7b7c` | neutral | Muted text, helper text, and less prominent iconography or borders, maintaining legibility with reduced visual weight |
| Mid Gray | `#666666` | neutral | Supporting text for bodies and links, providing a slightly softer contrast than Jet Black |
| Pewter Accent | `#4a4b4c` | neutral | Accent color for specific card text and borders, signaling subtle detail or secondary information |

## Typography

### Gilroy

| Key | Value |
| --- | --- |
| weight | 500, 600, 700 |
| sizes | 14px, 16px, 18px, 20px, 48px, 56px |
| lineHeight | 0.80, 1.00, 1.11, 1.17, 1.20, 1.25, 1.33, 1.43, 1.50, 1.56, 1.60, 1.78, 1.80, 1.88, 1.90 |
| letterSpacing | normal |
| substitute | system-ui |
| role | The primary typeface for all text content, from navigation and body text to prominent headings and buttons. Its varied weights are used to establish information hierarchy, with bolder weights for headlines and calls to action, and lighter weights for body text, maintaining a confident, direct tone. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.43 |  |
| body | 16 |  | 1.5 |  |
| body-lg | 18 |  | 1.56 |  |
| subheading | 20 |  | 1.2 |  |
| heading | 48 |  | 1.17 |  |
| display | 56 |  | 1.11 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 100px |
| cards | 24px |
| small | 4px |
| buttons | 100px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 32px
- **pageMaxWidth** — 1200px

## Components

### Primary Action Button

**Role:** High-priority call to action

Filled button with Red Action background (#ff5065), Canvas White text (#ffffff), and a 20px (large) or 24px (hero) border-radius. Padding is generous: 24px vertical, 40px horizontal.

### Dark Secondary Button

**Role:** Secondary action or ghost button

Filled button with Dark Neutral Button background (#1c1d1e), Canvas White text (#ffffff), and a 24px border-radius. Features large padding: 40px vertical, 40px horizontal.

### Light Outlined Button

**Role:** Tertiary action button or subtly interactive element

White background (#ffffff), Ebony Text (#0e0f10) text, with a light border rgba(14, 15, 16, 0.15). Radius is 20px for larger instances, 16px for smaller. Padding is 24px vertical, 40px horizontal (large) or 16px vertical, 24px horizontal (small).

### Elevated Card

**Role:** Content container that stands out

Canvas White background (#ffffff), 24px border-radius, and a subtle shadow (rgba(38, 42, 62, 0.06) 0px 5px 25px 0px). Inner content is padded by 32px on all sides.

### Ghost Card

**Role:** Content container with minimal visual impact

Transparent background, 16px or 24px border-radius, no shadow, and no internal padding from the card itself (content dictates padding). Used for structural grouping rather than visual prominence.

### Feature Badge

**Role:** Small, informational label

Slate Gray background (#2f3133), Canvas White text (#ffffff), with a large 100px border-radius (pill shape). Horizontal padding of 12px, no vertical padding.

## Layout

The page maintains a centered, max-width layout, approximately 1200px, with content contained within this boundary. The hero section presents a prominent centered headline and body text over a clean white background, flanked by call-to-action buttons. Sections alternate between pure white and subtle Whisper Gray (#f4f4f8) backgrounds, creating a clear vertical rhythm. Content is generally arranged in two-column layouts, often with text on one side and a visual element (like a card or image) on the other. Card grids, typically 3 or 4 columns, are used to showcase features or testimonials. Navigation is a persistent top bar, with interactive elements clearly marked.

## Imagery

The visual language for imagery primarily features product screenshots or candid business photography, often tightly cropped or contained within rounded card components. Images are typically integrated directly into content blocks, serving an explanatory or testimonial role rather than purely decorative. Icons are often simple, outlined, and monochromatic, occasionally featuring a brand accent color. Density is moderate; imagery supports the textual content without overwhelming it.

## Dos & Donts

### Do

- Use Red Action (#ff5065) exclusively for primary calls-to-action or critical indicators, ensuring it is a singular focal point.
- Maintain a clear visual hierarchy by differentiating headings with Jet Black (#000000) and body text with Ebony Text (#0e0f10) or Stone Text (#7a7b7c).
- Apply a minimal shadow (rgba(38, 42, 62, 0.06) 0px 5px 25px 0px) to elevated cards, ensuring components feel light and layered, not heavy.
- Utilize the 100px border-radius for all buttons and badges to consistently maintain a modern, continuous pill shape.
- Structure page sections with varying backgrounds like Canvas White (#ffffff) and Whisper Gray (#f4f4f8) to create visual breaks with a comfortable density.
- Apply a consistent 32px padding within elevated card components to give content breathing room.
- Pair Gilroy 700 with Jet Black (#000000) or Gilroy 600 with Ebony Text (#0e0f10) for headings to achieve a direct, commanding tone.

### Don't

- Do not introduce additional saturated colors beyond Red Action, Orange Highlight, and Amber Detail; maintain the clean achromatic base.
- Avoid heavy borders or solid color backgrounds on cards unless it's a specific, interactive action, to preserve the light and floating aesthetic.
- Do not vary letter-spacing from 'normal'; the Gilroy typeface already provides sufficient distinction.
- Refrain from using shadows on elements other than elevated cards; elevation should be a subtle indicator, not pervasive.
- Do not use generic system fonts; always specify Gilroy to uphold the brand's typographic signature.
- Avoid creating new text sizes or line heights; adhere strictly to the established type scale to maintain rhythm and hierarchy.
- Do not over-use the Orange Highlight (#ff7a59) or Amber Detail (#ff5c35); these are accents for emphasis, not general-purpose colors.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #7a7b7c
accent: #ff7a59
primary action: #ff5065 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #ff5065 background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create an Elevated Card: Canvas White background (#ffffff), 24px radius, shadow rgba(38, 42, 62, 0.06) 0px 5px 25px 0px. Internal content should have 32px padding on all sides. Heading (Gilroy 20px, weight 600, Jet Black #000000). Body text (Gilroy 16px, weight 500, Stone Text #7a7b7c).
3. Create a Dark Secondary Button: Dark Neutral Button background (#1c1d1e), Canvas White text (#ffffff), 24px radius, 40px vertical padding, 40px horizontal padding. Text (Gilroy 16px, weight 600).
4. Create a Feature Badge: Slate Gray background (#2f3133), Canvas White text (#ffffff), 100px radius, 12px horizontal padding, 0px vertical padding. Text (Gilroy 14px, weight 500).

---
_Source: https://styles.refero.design/style/effca480-81fb-4b8f-ab9c-3aa8c219c82a_
