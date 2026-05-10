# Switch-Lit — Design Reference

> Type-driven collaborative canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://switch-lit.com](https://switch-lit.com) |
| Refero page | [https://styles.refero.design/style/97f7787e-bba0-4d37-8b74-4b0cb8d5a57c](https://styles.refero.design/style/97f7787e-bba0-4d37-8b74-4b0cb8d5a57c) |
| Theme | light |
| Industry | media |

## Overview

Switch-Lit creates a collaborative writing experience through a playful, slightly tactile, and monochrome design. Its visual identity relies on dynamic typography, abundant whitespace, and a limited color palette featuring muted, vibrant color washes as accent backdrops. Surfaces are predominantly light with subtle borders, while interactive elements subtly animate, giving the interface a responsive, engaging feel.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary text, borders, button outlines, and key icon strokes — offering sharp contrast against light backgrounds |
| Cloud Canvas | `#f9f9f7` | neutral | Page backgrounds, card surfaces, and filled buttons — forming the primary light canvas of the UI |
| Stone Whisper | `#d2ddd2` | neutral | Subtle card borders and secondary button outlines, providing gentle visual separation without starkness |
| Mist Gray | `#edf0e9` | neutral | Background for secondary buttons and subtle surface accents |
| Lunar Dust | `#dee5dd` | neutral | Soft icon strokes, subtle dividers, and low-emphasis decorative details. Do not promote it to the primary CTA color |
| Sky Tint | `#bed4fb` | accent | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Lime Glow | `#edfe5e` | accent | Green wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Mint Burst | `#31e992` | accent | Small decorative background accents |

## Typography

### ABCArizonaSans

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| weights | 400, 500, 700 |
| sizes | 11px, 13px, 15px, 16px, 18px |
| lineHeight | 1.00, 1.15, 1.18, 1.25, 1.27 |
| letterSpacing | 0.067em at 11px, 0.046em at 13px, 0.040em at 15px, 0.036em at 16px, 0.033em at 18px |
| substitute | Inter |
| role | General body text, button labels, navigation items, and various UI elements, providing a consistent, legible base with slightly tighter letter-spacing for denser information. |

### ABCArizonaSansVariable

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 11px, 15px, 18px |
| lineHeight | 1.00, 1.11, 1.18, 1.36, 1.40 |
| letterSpacing | 0.091em at 11px, 0.040em at 15px, 0.036em at 18px |
| substitute | Inter Variable |
| role | Card titles, navigation links, and specific body text where a more expressive, variable font is desired, often with slightly wider tracking. |

### ABCArizonaFlare-Regular

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 24px, 27px, 28px, 57px |
| lineHeight | 0.93, 1.04, 1.07, 1.11 |
| letterSpacing | 0.011em at 24px, 0.004em at 27px, -0.002em at 57px |
| substitute | Lora |
| role | Declarative headings and badges, featuring moderate sizes and a 'whisper-weight' 300 that conveys authority through elegance and restraint, not volume. |

### ABCCamera-Heavy

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 64px, 68px |
| lineHeight | 0.74, 0.94, 1.00 |
| letterSpacing | -0.001em at 64px and 68px |
| substitute | Anton |
| role | Large, impactful display headings that utilize a bold, almost blocky aesthetic with tight tracking to create a strong visual presence. |

### ABCArizonaMixVariable

| Key | Value |
| --- | --- |
| weight | 700 |
| weights | 700 |
| sizes | 20px |
| lineHeight | 1.15 |
| letterSpacing | 0.025em at 20px |
| substitute | Inter Variable |
| role | Distinct small headings, using a bold weight and subtle letter-spacing for emphasis. |

### ABCArizonaText

| Key | Value |
| --- | --- |
| weight | 700 |
| weights | 700 |
| sizes | 20px |
| lineHeight | 1.00 |
| letterSpacing | 0em |
| substitute | Inter |
| role | Prominent button text and emphasized body text, leveraging boldness for clear calls to action and important information. |

### ABCArizonaMix

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 20px |
| lineHeight | 1.25 |
| letterSpacing | 0.100em at 20px |
| substitute | Inter |
| role | Specific body text requiring a distinctive, open feel due to slightly wider letter-spacing. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1 | 0.067 |
| body | 15 |  | 1.18 | 0.04 |
| subheading | 18 |  | 1.25 | 0.033 |
| heading-sm | 20 |  | 1 | 0.025 |
| heading | 24 |  | 1.04 | 0.011 |
| heading-lg | 27 |  | 1.07 | 0.004 |
| display | 64 |  | 0.74 | -0.001 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 6px |
| forms | 6px |
| links | 3px |
| buttons | 24px |
| elements | 6px |

- **elementGap** — 20px
- **sectionGap** — 40px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Cloud Canvas | `#f9f9f7` | 1 | Primary page and card backgrounds, the base layer of the UI. |
| Mist Gray | `#edf0e9` | 2 | Background for secondary buttons and subtle accents, slightly differentiated from the main canvas. |
| Accent Wash | `#bed4fb` | 3 | Sectional background washes (e.g., Sky Tint, Lime Glow) to visually segment content or highlight areas. |

## Components

### Primary Filled Button

**Role:** Main calls to action

Solid button with Cloud Canvas (#f9f9f7) background, Midnight Ink (#000000) text and border, featuring a soft 24px border-radius and a subtle shadow. Padding is 0px vertical, 29px horizontal.

### Secondary Filled Button

**Role:** Secondary actions or navigation

Solid button with Mist Gray (#edf0e9) background, Midnight Ink (#000000) text, and Stone Whisper (#d2ddd2) border with a 6px border-radius. Padding is 0px vertical, unspecified horizontal.

### Ghost Outlined Button

**Role:** Tertiary actions or navigation

Transparent button with rgba(210, 221, 210, 0.2) background, Lunar Dust (#dee5dd) text and border, featuring a 6px border-radius. Padding is 0px vertical, 12px horizontal.

### Basic Card

**Role:** Content grouping without visual elevation

Transparent card with no background or shadow, 0px border-radius. Padding is 0px.

### Sectional Card (subtle background)

**Role:** Content grouping with slight visual separation

Card with rgba(233, 238, 231, 0.5) background and 12px border-radius, no shadow. Padding is 0px.

### Circular Element

**Role:** Decorative or specific interactive elements

Transparent element with 50% border-radius, creating a circular shape. No background or shadow. Padding is 0px.

### Accent Card

**Role:** Container with specific branding implications

Card with no background, 6px border radius, and a tall aspect ratio set by a large bottom padding (421.125px).

## Layout

The layout is primarily a full-bleed design, allowing accent color washes to extend edge-to-edge. Content appears to be centered within a comfortable, unrestrained width, allowing for ample whitespace. Hero sections feature large, bold typography from ABCCamera-Heavy. Sections are rhythmically organized with consistent vertical spacing and alternating background washes (e.g., Sky Tint, Lime Glow) or the default Cloud Canvas. Content often appears in stacked blocks or implied two-column arrangements for feature descriptions. Navigation is standard top-bar style, and components feel comfortable rather than dense.

## Imagery

Imagery is minimal and primarily serves as decorative background washes or to illustrate conceptual points rather than showcasing products or people. When present, it features abstract, muted, or vivid color blocks that integrate seamlessly with the light theme. Icons are outlined, typically Midnight Ink (#000000) or Lunar Dust (#dee5dd), with a clean, functional style.

## Dos & Donts

### Do

- Use Midnight Ink (#000000) for all primary text and critical borders to maintain sharp legibility.
- Apply Cloud Canvas (#f9f9f7) as the primary background for pages and card surfaces.
- Prioritize ABCArizonaSans for general UI text and ABCArizonaFlare-Regular for heads, employing their specific letter-spacing values.
- Maintain a comfortable density with a 20px `elementGap` between UI components and `cardPadding` inside content blocks.
- Utilize the 24px `radius` for prominent buttons and 6px `radius` for cards and secondary interactive elements.
- Incorporate subtle motion (0.3s ease) for property changes like `transform`, `box-shadow`, and `background-color` to provide responsive feedback.
- Employ Sky Tint (#bed4fb) or Lime Glow (#edfe5e) as background washes for entire sections to subtly differentiate content areas or introduce accent.
- Use 0px vertical padding for all button types, relying on line height for vertical sizing.

### Don't

- Avoid arbitrary border-radius values; stick to 3px, 6px, 12px, or 24px defined in the system.
- Do not introduce new saturated colors beyond Sky Tint, Lime Glow, and Mint Burst, as the system relies on a restrained palette.
- Do not use heavy, opaque shadows; if shadows are needed, use `rgba(0, 0, 0, 0.2) 0px 2px 6px 0px` sparingly for interactive buttons.
- Do not deviate from the specified font families, weights, and letter-spacing for consistency in brand voice and readability.
- Avoid tightly packed layouts; ensure adequate spacing, defaulting to a 20px `elementGap`.
- Do not use dark backgrounds for full sections without strong justification; the system is designed explicitly for a light theme.
- Do not introduce gradients; the system relies on solid colors or background washes for visual depth.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #f9f9f7
border: #000000
accent: #bed4fb
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Design a Ghost Outlined Button: background transparent, text #dee5dd, border #d2ddd2, 6px radius, 0px vertical padding, 12px horizontal padding.
3. Create a section heading using ABCArizonaFlare-Regular, weight 300, 57px size, lineHeight 0.93, letterSpacing -0.002em, color #000000. Underneath, add body text using ABCArizonaSans, weight 400, 15px size, lineHeight 1.18, letterSpacing 0.040em, color #000000.
4. Design a Sectional Card: background rgba(233, 238, 231, 0.5), 12px radius, with 20px card padding and a default 20px element gap for internal components.

---
_Source: https://styles.refero.design/style/97f7787e-bba0-4d37-8b74-4b0cb8d5a57c_
