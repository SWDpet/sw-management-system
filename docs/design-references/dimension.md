# Dimension — Design Reference

> Deep-space command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.dimension.dev](https://www.dimension.dev) |
| Refero page | [https://styles.refero.design/style/fbcf9cbb-7c6b-449d-862a-bce521a8ab1d](https://styles.refero.design/style/fbcf9cbb-7c6b-449d-862a-bce521a8ab1d) |
| Theme | dark |
| Industry | ai |

## Overview

Dimension employs a dark, immersive interface, reminiscent of a command center displaying critical information. Its surfaces range from a deep, nearly black base to subtle, translucent grays, often paired with blurred backgrounds to create depth. Typography is crisp and compact, using a blend of modern sans-serifs with precise letter-spacing for high legibility in a low-light setting. Interactions are defined by ghost-like controls, soft white accents, and carefully calibrated radii that hint at physical buttons, maintaining a sense of focused utility over decorative flourish.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Base | `#0a0a0a` | neutral | Page canvas, primary dark background for all interface elements |
| Storm Gray | `#161616` | neutral | Muted text, subtle borders, accent text in navigation |
| Gunmetal | `#282828` | neutral | Element borders, subtle strokes, icon outlines |
| Slate Text | `#686868` | neutral | Secondary body text, disabled states, helper text |
| Ash Text | `#b2b2b2` | neutral | Tertiary text, subtle button labels, less prominent links |
| Silver Whisper | `#c2c2c2` | neutral | Muted body text, placeholder text |
| Ghost White | `#e5e5e5` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Canvas White | `#ffffff` | neutral | Active text, primary button backgrounds, default icon fills |
| Gradient Aura | `#4867af` | accent | Background gradient for initial landing experience, creating an atmospheric, deep-space visual |
| Interactive Glow | `#6b62f2` | accent | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color; Subtle indicator lines, decorative background lines, and ambient light effects |

## Typography

### DM Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| weights | 400, 500, 700 |
| sizes | 14px, 15px, 16px, 18px, 40px, 72px |
| lineHeight | 1.00, 1.11, 1.20, 1.25, 1.43, 1.50, 1.56 |
| letterSpacing | -0.035em at 72px, 0.025em elsewhere |
| substitute | Inter |
| role | Used for body text, links, and detailed information. Its slight contrast to Geist provides clear distinction while maintaining overall readability thanks to subtle tracking adjustments for larger sizes. |

### Geist

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 14px, 16px, 18px, 24px, 32px, 36px, 48px |
| lineHeight | 1.00, 1.11, 1.14, 1.33, 1.43, 1.50, 1.71 |
| letterSpacing | normal |
| substitute | system-ui |
| role | Primary typeface for headings and select interface elements. Its clean, technical aesthetic reinforces the AI-driven product identity. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 18px |
| lineHeight | 1.5 |
| role | system-ui — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.5 | 0.35 |
| body | 16 |  | 1.5 | 0.4 |
| subheading | 18 |  | 1.5 | 0.45 |
| heading-sm | 24 |  | 1.33 |  |
| heading | 32 |  | 1.14 |  |
| heading-lg | 36 |  | 1.11 |  |
| display | 48 |  | 1 | -0.672 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| buttons | 9999px |
| callouts | 10px |
| app-icons-ui | 10px |
| hero-sections | 40px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Base | `#0a0a0a` | 0 | Dominant page and main container background. |
| Translucent Accent | `#00000033` | 1 | Soft, translucent cards that overlay the base, allowing background gradients to show through, often paired with a backdrop blur. |
| Semi-Transparent Overlay | `#d4d4d41a` | 2 | Subtle, ghost-like cards or containers with minimal visual presence. |

## Components

### Ghost Button

**Role:** Secondary action button

backgroundColor=rgba(0, 0, 0, 0), color=rgb(255, 255, 255), border=1px solid rgb(229, 229, 229), borderRadius=10px, padding=8px 8px.

### Navigation Link Button

**Role:** Navigation items within a menu

backgroundColor=rgba(0, 0, 0, 0), color=rgb(255, 255, 255), border=1px solid rgb(229, 229, 229), borderRadius=0px, padding=6px 14px. These lack typical button padding and borders, resembling links more than buttons.

### Pill Button

**Role:** Primary action button, tags, or small interactive elements

backgroundColor=rgb(255, 255, 255), color=rgb(22, 22, 22), borderRadius=9999px, padding=8px 12px.

### Floating Pill Bar Button

**Role:** Buttons within the floating navigation/action bar

backgroundColor=rgb(255, 255, 255), color=rgb(0, 0, 0), borderRadius=9999px, padding=0px 0px. Minimal padding indicates these are inner elements of a larger component.

### Translucent Spotlight Card

**Role:** Displaying featured content or application icons

backgroundColor=rgba(0, 0, 0, 0.2), borderRadius=42px, no shadow, padding=22px 28px. Its translucency allows the background gradient to subtly show through.

### Section Bottom Card

**Role:** Separating content blocks at the bottom of a scrollable section

backgroundColor=rgb(255, 255, 255), borderRadius=40px 40px 0px 0px, no shadow, no padding.

### Ghost Corner Radius Card

**Role:** Subtle containers for information, minimal visual weight

backgroundColor=rgba(212, 212, 212, 0.1), borderRadius=24px, no shadow, no padding. Provides a faint, almost invisible grouping.

## Layout

The page employs a full-bleed layout, where background gradients extend edge-to-edge, but content is contained within an implied max-width column, centered on the screen. The initial hero section features a dual-column layout: left for primary headline and feature list, right for a large product visual (app icons within a translucent card). Vertical rhythm is established by distinct sections, sometimes subtly delineated by an atmospheric background gradient. Navigation is a sticky bottom bar, featuring pill-shaped buttons with both text and icons, blending into the ambient background with subtle backdrop blurring. Content often appears in stacked blocks or side-by-side text/image arrangements.

## Imagery

The imagery on Dimension is primarily interface-driven, featuring tightly cropped product screenshots of app icons and functional UI elements (like a task list). These are often contained within softly rounded, translucent cards. The aesthetic is clean and focused on showcasing product utility directly, rather than relying on lifestyle photography or abstract illustrations. When icons are present, they are either filled, multi-color app icons or simple, outlined, monochromatic interface symbols. The imagery serves an explanatory and product showcase role, presented with minimal decorative clutter.

## Dos & Donts

### Do

- Use Midnight Base (#0a0a0a) as the default background for large sections and the page canvas.
- Apply Ghost White (#e5e5e5) for all primary body text and essential UI borders.
- Utilize Geist typeface for all headings (H1-H6) and DM Sans for body text, ensuring proper letter-spacing based on size.
- Every interactive element, especially buttons, must have a border-radius of 9999px for pill-shaped elements or 10px for standard rects.
- Create depth with the `backdrop-filter: blur(4px)` property on translucent UI elements, rather than heavy box-shadows.
- Maintain uniform element gaps of 8px between closely related UI elements, like buttons in a cluster or form fields.
- Emphasize content through contrast with Canvas White (#ffffff) for active states and primary action button fills.
- Use Storm Gray (#161616) for muted text and secondary UI strokes to provide subtle hierarchy.

### Don't

- Do not use strong, opaque background colors; darker, translucent, or gradient backgrounds are preferred.
- Avoid deep, dark shadows; elevation is achieved through subtle opacity changes, backdrop blurs, or very light, diffused shadows.
- Do not use generic system fonts in place of Geist or DM Sans for any main text content.
- Avoid hard, sharp corners; all cards, buttons, and significant containers require specific, deliberate rounding (radius 10px, 24px, 42px, or 9999px).
- Do not introduce strong chromatic colors for primary UI elements; reserve them for subtle accents, gradients, or as background atmosphere.
- Do not use a light theme; the interface always defaults to a dark aesthetic.
- Avoid dense, tightly packed layouts; prefer comfortable spacing with a base unit of 4px and elemental gaps of 8px.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #e5e5e5
background: #0a0a0a
border: #e5e5e5
accent: #6b62f2
primary action: #ffffff (filled action)

Example Component Prompts:
Create a Primary Action Button: #ffffff background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
Create a translucent app icon card: background rgba(0,0,0,0.2), 42px corner radius, padding 22px vertical and 28px horizontal. Apply backdrop-filter: blur(4px).
Create a top-level heading: 'Geist' font, size 48px, color #e5e5e5, line height 1.0, letter spacing -0.672px.
Create a navigation bar item: 'DM Sans' font, size 16px, color #ffffff, background rgba(0,0,0,0), border 1px solid #e5e5e5, 0px radius, 6px vertical and 14px horizontal padding. 

---
_Source: https://styles.refero.design/style/fbcf9cbb-7c6b-449d-862a-bce521a8ab1d_
