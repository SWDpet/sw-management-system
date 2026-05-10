# Sandclock — Design Reference

> Midnight Command Center: Tightly tracked typography and a single digital-green accent on a deep, dark canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.sandclock.org](https://www.sandclock.org) |
| Refero page | [https://styles.refero.design/style/ccbb774f-d1a9-4cc6-b1be-31379ba0baf1](https://styles.refero.design/style/ccbb774f-d1a9-4cc6-b1be-31379ba0baf1) |
| Theme | dark |
| Industry | fintech |

## Overview

Sandclock applies a 'midnight command center' aesthetic, designed for focus and serious financial operations. It balances a deep, near-black canvas with crisp white typography and a vibrant green accent to highlight key data and interactive elements. Surfacing is achieved through subtle tonal shifts rather than heavy shadows, maintaining a lightweight feel. Typography is functional and modern, with generous letter-spacing for standard text and tightly tracked headings for immediate impact.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#0a0a0a` | neutral | Page backgrounds, card containers, dark button text – creates deep, immersive canvas |
| Deep Space | `#171717` | neutral | Elevated card backgrounds, inner surface containers – provides subtle layering on top of Absolute Zero |
| Void Black | `#222222` | neutral | Default card backgrounds, secondary button background, borders – provides primary surface elevation and structure |
| Cosmic Dust | `#9b9b9b` | neutral | Muted body text, secondary headings, helper text – for less prominent information |
| Starlight White | `#ffffff` | neutral | Primary text, prominent headings, button backgrounds, interactive elements – provides high contrast against dark surfaces |
| Digital Green | `#3fe280` | accent | Primary action buttons, accented text, active navigation indicators – provides positive confirmation and calls to action |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 14px, 16px, 20px, 24px, 36px |
| lineHeight | 1.11, 1.33, 1.40, 1.43, 1.50 |
| letterSpacing | 0.05em |
| substitute | system-ui |
| role | Body text, navigation, buttons, subheadings – provides clear, readable content with slightly open tracking. |

### DM Sans

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 16px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | sans-serif |
| role | Secondary body text emphasis – a slightly more condensed option for specific emphasis without heavy tracking. |

### Aeonik

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 72px |
| lineHeight | 1.00 |
| letterSpacing | -0.025em |
| substitute | Arial |
| role | Display headings – large size and tight negative letter-spacing (-0.025em) provide a distinctive, contemporary headline aesthetic that feels authoritative through its precise compression. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.5 | 0.05 |
| body-sm | 16 |  | 1.43 | 0.05 |
| body | 20 |  | 1.4 | 0.05 |
| subheading | 24 |  | 1.33 | 0.05 |
| heading | 36 |  | 1.11 | 0.05 |
| display | 72 |  | 1 | -0.025 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 64px |
| cards | 12px |
| buttons | 16px |
| bodyMask | 9999px |

- **elementGap** — 16px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Pill Ghost Button (Connect Wallet)

**Role:** Main navigation action

Rounded button with Starlight White text on a transparent background, 64px border radius, 8px vertical and 12px horizontal padding. The transparent background implies a less 'committing' action until a connection is made.

### Primary Action Button (Digital Green)

**Role:** Key call to action

Filled button with Digital Green background and Absolute Zero text, 16px border radius, 8px vertical and 16px horizontal padding. Signals the most important user action.

### Secondary Action Button (Void Black)

**Role:** Alternative action button

Filled button with Void Black background and Starlight White text, 16px border radius, 8px vertical and 16px horizontal padding. Offers a clear secondary choice without competing visually with the primary action.

### Feature Card

**Role:** Content grouping

Rectangular card with Void Black background, 12px border radius, and 16px of padding on all sides. Used for grouping related content blocks.

### Vault Performance Card

**Role:** Key data display

Container with Deep Space background, 10px border radius, and 32px vertical padding (0px horizontal). Emphasizes key financial metrics.

## Layout

The page maintains a centered, max-width layout, though the exact max-width is not strictly enforced, allowing sections to breathe. The hero section is full-bleed, dark, featuring a large, centered headline and subtext, followed by two action buttons nested below. Content sections typically alternate between full-width horizontal content blocks (like partner logos) and structured responsive grids (e.g., two-column metric displays or three-column announcement cards). Vertical rhythm is established with consistent 48px section gaps. The navigation is a classic top bar, sticky in appearance, keeping key links and the 'Connect Wallet' button accessible.

## Imagery

This site uses a mix of practical, functional iconography and occasional abstract graphics. Icons are monochrome (Starlight White or Cosmic Dust) and mostly filled, with a medium stroke weight where outlines appear, used for functional clarity and status. Graphic elements are minimal and serve more as background textures or subtle decorative touches rather than prominent visuals. There is an absence of photography or complex illustrations, emphasizing a serious, data-focused, and UI-driven product experience. Imagery is low-density, focusing on supporting UI elements rather than being decorative.

## Dos & Donts

### Do

- Prioritize Absolute Zero (#0a0a0a) or Void Black (#222222) as background for deep, immersive surfaces, reserving Deep Space (#171717) for subtle elevation.
- Use Starlight White (#ffffff) for all primary text and critical information to maintain high contrast and readability on dark backgrounds.
- Apply Digital Green (#3fe280) exclusively for primary calls to action, active states, and select data highlights to maintain its impact.
- Ensure headings use Aeonik at weight 400 with tight letter-spacing (-0.025em) for distinct branding.
- Use Inter font family for all body text, navigation, and button labels, applying a slightly open letter-spacing of 0.05em.
- Apply a 12px border radius to all cards and content containers for a consistent, subtly rounded aesthetic.
- Maintain comfortable spacing: 16px between elements and 48px between major sections.

### Don't

- Do not introduce additional vibrant colors; the color palette should remain limited to the specified neutrals and the single Digital Green accent.
- Avoid heavy drop shadows; elevation should primarily be communicated through background color shifts between Absolute Zero, Deep Space, and Void Black.
- Do not use generic system fonts for prominent headings; Aeonik's specific weight and tight tracking are critical to the brand voice.
- Don't use highly dense layouts; ensure ample 16px element spacing and 48px section spacing to maintain readability and focus.
- Avoid small, hard-to-read text. Use Inter 14px as the minimum body text size with a line height of 1.5 for optimal readability.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #0a0a0a
border: #222222
accent: #3fe280
primary action: #3fe280 (filled action)

Example Component Prompts:
Create a hero section: Absolute Zero background. Headline at 72px Aeonik weight 400, #ffffff, letter-spacing -0.025em. Subtext at 20px Inter weight 400, #9b9b9b, letter-spacing 0.05em. Two buttons: 'Start Earning' (Digital Green background, Absolute Zero text, 16px radius, 8px 16px padding) and 'Buy QUARTZ' (Void Black background, Starlight White text, 16px radius, 8px 16px padding).

Create a Feature Card: Void Black background, 12px border radius, 16px padding. Title at 24px Inter weight 500, #ffffff, letter-spacing 0.05em. Body text at 16px Inter weight 400, #9b9b9b, letter-spacing 0.05em. Icon (Starlight White).

Create a Pill Ghost Button: Starlight White text, 64px border radius, 8px vertical and 12px horizontal padding. Border 1px solid #ffffff.

---
_Source: https://styles.refero.design/style/ccbb774f-d1a9-4cc6-b1be-31379ba0baf1_
