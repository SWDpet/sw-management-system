# Slash — Design Reference

> Midnight Ledger, Obsidian Surfaces

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.slash.com](https://www.slash.com) |
| Refero page | [https://styles.refero.design/style/7c38e84b-aea0-4c8f-b3e9-60b994ee6c6b](https://styles.refero.design/style/7c38e84b-aea0-4c8f-b3e9-60b994ee6c6b) |
| Theme | dark |
| Industry | fintech |

## Overview

Slash adopts a high-contrast dark mode aesthetic for business finance: black canvases, subtly layered dark gray surfaces, and a striking white primary text. Inter typography provides utilitarian clarity, while Ivy Presto adds a touch of classic sophistication to display headlines. The system minimizes color, primarily using off-white for interaction and a warm, golden gradient for subtle accents and visual depth on elevated elements.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Page background, deepest surface level. Creates a sense of depth and seriousness |
| Obsidian Surface | `#030304` | neutral | Elevated card backgrounds, deeper shadows. A subtle lift from the main background |
| Charcoal Canvas | `#08080a` | neutral | Primary surface background, such as panels and main content areas |
| Pewter Accent | `#121317` | neutral | Subtle background for card, button, and other interactive elements, providing a soft lift |
| Slate Gray | `#1c1d22` | neutral | Component backgrounds, such as card details and selected states. Less saturated than Charcoal Canvas |
| Ash Text | `#5e616e` | neutral | Muted secondary text, placeholder text, subtle borders. For less prominent information |
| Stone Text | `#777a88` | neutral | Tertiary text, subtle icons, inactive navigation items. Lower hierarchy content |
| Silver Text | `#acafb9` | neutral | Helper text, slightly more prominent than Stone Text |
| Porcelain Text | `#cdcdcd` | neutral | Less important body text, slight relief from pure white |
| White Frost | `#e2e3e9` | neutral | Dominant body text, strong contrast against dark backgrounds. Also used for outlined borders |
| Pure White | `#ffffff` | neutral | Primary headings, active states, emphasized text, and button backgrounds. The brightest element |
| Golden Gradient | `#cc9166` | accent | Decorative highlights, subtle hover effects, linear gradients for elevated elements. Provides a touch of luxury and prestige |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| weights | 300, 400, 500, 600, 700 |
| sizes | 12px, 13px, 14px, 15px, 16px, 18px, 20px, 24px, 32px, 48px, 52px |
| lineHeight | 1.00, 1.13, 1.25, 1.33, 1.38, 1.43, 1.50, 1.56 |
| letterSpacing | -0.007, -0.007, -0.013, -0.013, -0.02, -0.02, -0.022, -0.025, -0.025, -0.04, -0.04 |
| substitute | system-ui |
| role | Primary typeface for all UI elements, body text, and functional components. Its clean, sans-serif design maintains legibility and a modern, technical feel across various densities and scales. |

### Ivy Presto

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 32px, 52px, 64px, 88px |
| lineHeight | 1.00, 1.13, 1.25 |
| letterSpacing | 0.01, 0.01, 0.01, 0.01 |
| substitute | Playfair Display |
| role | Decorative display typeface for large, impactful headlines and hero text. The serif design provides an air of traditional luxury and authority, contrasting with the utilitarian Inter. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.007 |
| body-sm | 14 |  | 1.43 | -0.013 |
| body | 16 |  | 1.38 | -0.02 |
| subheading | 20 |  | 1.33 | -0.022 |
| heading | 32 |  | 1.25 | -0.025 |
| heading-lg | 48 |  | 1.13 | -0.04 |
| display | 88 |  | 1 | 0.01 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| md | 10px |
| sm | 2px |
| none | 0px |
| pill | 9999px |

- **elementGap** — 6px
- **sectionGap** — 160px
- **cardPadding** — 32px
- **pageMaxWidth** — 1216px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | 0 | Base page background |
| Obsidian Surface | `#030304` | 1 | Primary elevated surface, subtle lift |
| Charcoal Canvas | `#08080a` | 2 | Secondary elevated surface, general content panels |
| Pewter Accent | `#121317` | 3 | Tertiary elevated surface, interactive components |
| Slate Gray | `#1c1d22` | 4 | Further elevated component backgrounds, deeper interactive elements |

## Components

### Ghost Navigation Link

**Role:** Navigation links and subtle interactive text.

backgroundColor: rgba(0, 0, 0, 0), color: Pure White (#ffffff), border: none, borderRadius: 0px, padding: 20px 0px.

### Pill Primary Button

**Role:** Main call-to-action buttons.

backgroundColor: Pure White (#ffffff), color: Charcoal Canvas (#08080a), border: none, borderRadius: 9999px, padding: 0px 32px.

### Pill Ghost Button

**Role:** Secondary action buttons, less prominent than primary.

backgroundColor: rgba(0, 0, 0, 0), color: Pure White (#ffffff), border: 1px solid Pure White (#ffffff), borderRadius: 9999px, padding: 6px 10px.

### Sharp Ghost Text Button

**Role:** Minimal interactive elements without strong visual weight.

backgroundColor: rgba(0, 0, 0, 0), color: Pure White (#ffffff), border: none, borderRadius: 4px, padding: 0px 8px.

### Card Standard

**Role:** Container for content sections.

backgroundColor: rgba(0, 0, 0, 0), borderRadius: 10px, boxShadow: none, padding: 0px.

### Pill Input Field

**Role:** Form input fields.

backgroundColor: rgba(0, 0, 0, 0), color: Pure White (#ffffff), border: 1px solid Pure White (#ffffff), borderRadius: 9999px, padding: 10px 20px.

## Layout

The page adheres to a max-width of 1216px, primarily featuring a centered content model. The hero section often presents a centered headline (Ivy Presto) over a dark, full-bleed background, with a prominent product screenshot or abstract graphic. Sections are demarcated by consistent vertical spacing of 160px, maintaining a spacious rhythm. Content often alternates between text-dominant blocks and graphical elements, including 2-column layouts for text-plus-visuals, and 3-column card grids for features. Navigation is a sticky top bar with a left-aligned logo and right-aligned actions.

## Imagery

This system primarily uses product screenshots and abstract, ethereal graphics. Product screenshots are typically contained within dark, rounded cards, sometimes with a soft blur. Abstract graphics often feature metallic or translucent elements with soft, golden gradients and refractions, creating depth and a premium feel. Icons are monochrome, generally outlined or filled in White Frost. Imagery serves both decorative atmosphere (abstracts) and explanatory content (product UI examples). The density is balanced, with imagery often integrated into information blocks rather than dominating the page.

## Dos & Donts

### Do

- Use Midnight Ink (#000000) as the base background for most pages to establish the dark theme.
- Prioritize Inter for all functional text: body, navigation, buttons, and forms, ensuring high legibility.
- Apply Ivy Presto for large headlines (above 32px) to introduce a classic, authoritative counterpoint to Inter.
- Maintain a compact density using 6px for elementGap and 16px as a multiplier for internal component spacing.
- Incorporate subtle Golden Gradient (#cc9166) on hover states or as a background for elevated, interactive elements, never as a solid background color.
- Use Pure White (#ffffff) for primary text and active states to ensure strong contrast and draw attention.
- Round corners with a 10px radius for cards and general content blocks, and 9999px for pill-shaped buttons and inputs.

### Don't

- Avoid using bright, saturated colors for backgrounds or large areas, as the system relies on subtle tonal differences.
- Do not use Ivy Presto for body text or small UI elements; reserve it for large display headings.
- Do not break the dark canvas and dark surface hierarchy by introducing light-mode components.
- Refrain from heavy shadows or strong outlines; elevate elements through subtle background shifts and internal padding.
- Do not introduce additional font families or decorative elements that disrupt the high-contrast, professional aesthetic.
- Avoid arbitrary uses of Pure White (#ffffff) for backgrounds; reserve it for primary text and calls to action.
- Do not use gradients for body text or small icons; they are reserved for decorative accents and elevated visuals.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000000
border: #e2e3e9
accent: #cc9166
primary action: #ffffff (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #ffffff background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a Feature Card: Slate Gray (#1c1d22) background, borderRadius 10px. Title 'Marketing Agencies' in Inter weight 600, 24px, lineHeight 1.25, letterSpacing -0.025, Pure White (#ffffff). Body text ‘Transaction History’ in Inter weight 400, 16px, lineHeight 1.38, letterSpacing -0.02, White Frost (#e2e3e9). Add an icon rendered in Silver Text (#acafb9).
3. Build a Pill Input Field: Background transparent, border 1px solid Pure White (#ffffff), borderRadius 9999px, padding 10px 20px. Placeholder text 'What's your email?' in Ash Text (#5e616e) Inter weight 400, 16px. User input text in Pure White (#ffffff) Inter weight 400, 16px.

---
_Source: https://styles.refero.design/style/7c38e84b-aea0-4c8f-b3e9-60b994ee6c6b_
