# Frame.io — Design Reference

> Midnight control panel, glowing with purpose.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://frame.io](https://frame.io) |
| Refero page | [https://styles.refero.design/style/30c3aa18-4323-4448-8ddd-3ca933fe5780](https://styles.refero.design/style/30c3aa18-4323-4448-8ddd-3ca933fe5780) |
| Theme | dark |
| Industry | saas |

## Overview

Frame.io's design evokes a sophisticated, high-tech command center with a dark, immersive background that immediately draws focus to content. Dark, deeply saturated purples and blues provide an atmospheric stage, accented by electric blue interactive elements that pulse like system indicators. Sharp typography with generous letter-spacing on display elements contrasts with soft-cornered cards, creating a tension between precision and approachability, suitable for creative professionals who value both. The overall impression is one of quiet power and focused efficiency.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | neutral | Deepest dark for backgrounds and text when used against lightest accents, like a black hole absorbing light. |
| Void Black | `#0a0a13` | neutral | Primary surface background, creating the foundational darkness for the UI. |
| Dark Nebula | `#08080c` | neutral | Secondary surface background, subtly lighter than Void Black for nested elements, suggesting layered depth. |
| Twilight Graphite | `#757580` | neutral | Subtle text and icon color, providing contrast against dark backgrounds without being stark white. |
| Cloud Whisper | `#fcfcfc` | neutral | High-contrast text color against dark backgrounds, for primary headlines and interactive elements. |
| Pale Ash | `#a3a3b3` | neutral | Subtle supporting text and soft borders, offering a muted contrast. |
| Frost Gleam | `#dedfee` | neutral | Very light text and borders, almost white, used for highlights and delicate details. |
| Space Blue Gradient | `#0a001` | brand | Hero section background, establishing the deep, cosmic aesthetic. |
| Electric Indigo | `#6199f6` | accent | Primary accent for interactive elements, links, and icons, drawing immediate attention. |
| Cosmic Violet | `#5b53ff` | accent | Distinctive accent for key call-to-actions, signaling prime interactivity. |
| Peripheral Haze Gradient | `#000b35` | brand | Subtle background gradient for atmospheric depth, hinting at distant light sources. |
| Violet Hue Radial | `#4e55a8` | brand | Localized background glow, adding subtle color variation and depth. |

## Typography

### FrameGothic

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 10px, 11px, 12px, 13px, 14px, 15px, 16px, 18px, 24px, 30px, 38px, 48px, 80px |
| lineHeight | 0.96, 1.00, 1.02, 1.04, 1.16, 1.20, 1.25, 1.30, 1.45, 1.50 |
| letterSpacing | -0.045em at 80px, -0.04em at 48px, -0.035em at 38px, -0.03em at 30px, 0.01em for body text |
| substitute | Inter |
| role | Primary brand typeface for all headings, body text, and UI elements. Its sharp, modern sans-serif structure underpins the tech-forward identity. Condensed letter spacing at large sizes creates a focused, intense feel unlike typical web fonts. |

### NeueMachinaInktrap

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 0.90 |
| letterSpacing | 0.06em |
| substitute | IBM Plex Mono |
| role | Monospace-style font used for small, technical labels or secondary information where a 'data' look is desired. Its very tight line height and wide letter spacing contribute to its distinct, machine-like character. |

### Times

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 10px |
| lineHeight | 1.00 |
| substitute | Lora |
| role | Incidental system font for browser-default elements or extremely small print, indicating minimal brand intervention in these areas. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px |
| lineHeight | 1.20 |
| substitute | Helvetica Neue |
| role | Fallback font for general button text and other UI elements, ensuring readability across diverse environments. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0.01 |
| body | 14 |  | 1.5 | 0.01 |
| subheading | 18 |  | 1.45 |  |
| heading | 24 |  | 1.3 |  |
| heading-lg | 30 |  | 1.25 | -0.48 |
| display | 80 |  | 0.96 | -3.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| buttons | 100px |
| small-elements | 10px |

- **elementGap** — 16px
- **sectionGap** — 
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Void Black | `#0a0a13` | 0 | Base page background, the deepest layer. |
| Dark Nebula | `#08080c` | 1 | Primary card and content block background, slightly raised from the base. |
| Nested UI Card Background | `#10101780` | 2 | Overlay for interactive or highlighted content, creating significant visual separation and depth. |

## Components

### CTA Button Group

### Feature Cards Grid

### Announcement Banner + Section Label

### Primary Ghost Button

**Role:** Default interactive element for secondary actions.

backgroundColor: rgba(0, 0, 0, 0), color: #fcfcfc, borderTopColor: #fcfcfc, borderRadius: 100px (fully rounded ends), paddingTop: 0px, paddingRight: 24px, paddingBottom: 0px, paddingLeft: 24px. Text uses FrameGothic 400. This is the main button style for calls to action against dark backgrounds.

### Tertiary Ghost Button (Dark Text)

**Role:** Subtle interactive elements on lighter backgrounds or when contrast to white text needs adjustment.

backgroundColor: rgba(0, 0, 0, 0), color: #000000, borderTopColor: #000000, borderRadius: 100px, paddingTop: 0px, paddingRight: 24px, paddingBottom: 0px, paddingLeft: 24px. Text uses FrameGothic 400. Used sparingly.

### Navigation Link Button

**Role:** Styling for interactive text navigation elements.

backgroundColor: rgba(0, 0, 0, 0), color: #fcfcfc, borderTopColor: #fcfcfc, borderRadius: 0px, paddingTop: 0px, paddingRight: 0px, paddingBottom: 0px, paddingLeft: 0px. Text uses FrameGothic 400 at small sizes. This is a text-only link that gains button characteristics dynamically.

### Feature Card

**Role:** Used to group related content, often with an icon or small visual.

backgroundColor: #090a18, borderRadius: 24px, boxShadow: none, paddingTop: varies (e.g., 0px), paddingRight: 0px, paddingBottom: varies (e.g., 0px), paddingLeft: 0px. This card is visually distinct with its rounded corners and subtle background.

### Nested UI Card

**Role:** Interior application-like cards for displaying specific content blocks.

backgroundColor: rgba(16, 16, 23, 0.5), borderRadius: 10px, boxShadow: inset 12px 12px 50px 0px #181826, with softer outer shadows. paddingTop: 24px, paddingRight: 24px, paddingBottom: 24px, paddingLeft: 24px. This card creates a strong sense of depth and separation from the background.

## Layout

The page primarily follows a max-width contained layout, likely around 1200px, but with full-bleed hero sections that leverage atmospheric gradients. The hero section is characterized by a large, centered headline over a dark, gradient background, paired with a prominent product screenshot or video. Section rhythms alternate between full-bleed dark gradient backgrounds and more structured dark panels with content. Content arrangements often feature large headlines with supportive body text, followed by multi-column grids (e.g., 4-column feature grids, 2-column text+image layouts). There's a spacious feel between sections, with subtle visual dividers achieved through background shifts. The navigation is a sticky top bar, providing persistent access.

## Imagery

This design primarily uses dark product screenshots and UI mockups, often presented within 'virtual screens' or cards that appear to float in the dark space. The treatment is contained within defined shapes, usually with slightly rounded corners (10px). When photography is present, it is dark, moody, and often features human subjects in a somewhat stylized, perhaps technical, context. Icons are typically monoline, filled with Electric Indigo or Cloud Whisper, and serve an explanatory or navigational role. The overall density of imagery is balanced with text, allowing for product features to be visually demonstrated without overwhelming the UI.

## Dos & Donts

### Do

- Use Cosmic Violet (#5b53ff) for primary call-to-action buttons, such as 'Start Free Trial'.
- Apply FrameGothic at 80px, weight 400, with -0.045em letter-spacing and 0.96 line-height for main display headlines.
- Utilize the Space Blue Gradient for the hero section background to establish the immersive dark aesthetic.
- Ensure all interactive elements like buttons and navigation links use a borderRadius of 100px for a fully rounded capsule shape.
- Maintain a primary text color of Cloud Whisper (#fcfcfc) on dark backgrounds for readability.
- For nested UI elements, apply the Nested UI Card styling with its distinct inset and outer shadows.
- Employ Pale Ash (#a3a3b3) for secondary informational text and muted borders against darker backgrounds.

### Don't

- Avoid using bright, high-saturation colors beyond Electric Indigo or Cosmic Violet; maintain the muted, dark palette.
- Do not use hard-edged rectangles for buttons or interactive cards; consistently apply the rounded corner radii.
- Refrain from shadows on basic static cards (like Feature Card) unless they are specifically designed for 'nested UI' components.
- Do not vary line-height significantly for body text; stick to 1.5 for FrameGothic at body sizes to preserve readability.
- Avoid generic system fonts for any primary content; always use our custom FrameGothic or NeueMachinaInktrap when appropriate.
- Do not use small letter-spacing on small body text; maintain a 0.01em or normal letter-spacing for readability.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #fcfcfc (Cloud Whisper)
- Background: #0a0a13 (Void Black)
- CTA: #5b53ff (Cosmic Violet)
- Border: #a3a3b3 (Pale Ash)
- Accent: #6199f6 (Electric Indigo)

### 3-5 Example Component Prompts
1. Create a Hero Section: Use 'Space Blue Gradient' as the background. Place a 'display' headline (FrameGothic 80px, 400, -0.045em, #fcfcfc) centered, followed by a 'body' text (FrameGothic 14px, 400, 0.01em, #a3a3b3). Below, add two 'Primary Ghost Buttons' (backgroundColor: rgba(0,0,0,0), color: #fcfcfc, borderTopColor: #fcfcfc, borderRadius: 100px, paddingRight: 24px).
2. Design a Feature Card: Set backgroundColor to 'Dark Nebula' (#08080c), borderRadius to 24px. Inside, use a 'heading' (FrameGothic 24px, 400, 1.3, #fcfcfc) for the title and 'body-sm' (FrameGothic 12px, 400, 1.5, #a3a3b3) for the description.
3. Implement a Nested UI Card: Use backgroundColor: rgba(16, 16, 23, 0.5), borderRadius: 10px, and the provided boxShadow. Ensure internal padding of 24px. Use 'subheading' (FrameGothic 18px, 400, 1.45, #fcfcfc) for a title and 'body' (FrameGothic 14px, 400, 1.5, #a3a3b3) for content.
4. Generate a Navigation Bar: Set background to 'Void Black' (#0a0a13). Include 'Navigation Link Buttons' with text color #fcfcfc and 'Tertiary Ghost Button (Dark Text)' for a sign-in or special action, both on the navigation bar.

---
_Source: https://styles.refero.design/style/30c3aa18-4323-4448-8ddd-3ca933fe5780_
