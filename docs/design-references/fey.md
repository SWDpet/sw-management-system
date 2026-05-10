# Fey — Design Reference

> Deep-space observatory control panel. Functionality, precision, and high-contrast data visualization on a near-black canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://feyapp.com](https://feyapp.com) |
| Refero page | [https://styles.refero.design/style/733e6475-892a-4138-8835-bf40344df317](https://styles.refero.design/style/733e6475-892a-4138-8835-bf40344df317) |
| Theme | dark |
| Industry | devtools |

## Overview

This design system evokes a sense of focused intelligence, like a deep-space observatory on a moonless night. Its near-black backgrounds provide a canvas for crisp white text and subtle, precise data visualizations. The deliberate restraint in chromatic color, limited to a single vivid blue for interaction and a warm orange for highlighting, creates a high-contrast experience where functionality takes aesthetic precedence. Fine typographical details and varying shades of dark gray give depth to the UI elements, preventing visual fatigue inherent in purely flat dark interfaces.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#0b0b0b` | neutral | Major surface backgrounds, card bases. |
| Obsidian Deep | `#131313` | neutral | Elevated card backgrounds, modal backdrops, adding subtle surface differentiation. |
| Coal Dust | `#191919` | neutral | Accent backgrounds, subtle dividers. |
| Slate Text | `#868f97` | neutral | Secondary body text, disabled states, iconography. |
| Ash Gray | `#999999` | neutral | Tertiary text, subtle descriptions. |
| Silver Accents | `#cccccc` | neutral | Subtle interactive elements, subtle outlines. |
| Light Smoke | `#e6e6e6` | neutral | Near-white elements in dark mode, button text for dark buttons. |
| Pure White | `#ffffff` | neutral | Primary text, prominent iconography, active states, key data readouts. |
| Cosmic Blue | `#479ffa` | accent | Interactive elements, active navigation items, primary calls to action — signifying state changes and focus. |
| Solar Flare | `#ffa16c` | brand | Prominent headings and highlights, drawing immediate attention to key information without being intrusive. |
| Emerald Profit | `#4ebe96` | semantic | Positive data indicators, success states. |
| Warn Gradient 1 | `#ffa16c` | brand | Decorative gradient often used for feature highlights. |
| Cool Gradient 1 | `#b6d6ff` | brand | Decorative gradient, often paired with data visualizations or abstract elements. |
| Vibrant Gradient 1 | `#d6fe51` | brand | Decorative gradient, suggesting energy and growth. |

## Typography

### calibre

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 10px, 11px, 12px, 14px, 15px, 16px, 18px, 24px, 26px, 48px, 54px |
| lineHeight | 1.00, 1.10, 1.20, 1.25, 1.30, 1.32, 1.36, 1.40, 1.50, 1.58 |
| letterSpacing | -0.08em, -0.08em, -0.053em |
| substitute | Inter, sans-serif |
| role | The primary typeface for all textual content. Its clean, slightly technical geometry is chosen for data legibility in financial contexts and its modern character. Varied weights ensure clear hierarchy from high-impact headlines to detailed data tables. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0 |
| body | 14 |  | 1.36 | 0 |
| heading-sm | 18 |  | 1.25 | 0 |
| heading | 24 |  | 1.2 | -0.053 |
| heading-lg | 26 |  | 1.1 | -0.053 |
| display | 48 |  | 1 | -0.08 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| general | 10px |
| input-sm | 6px |
| buttons-pill | 99px |
| buttons-square | 6px |

- **elementGap** — 
- **sectionGap** — 
- **cardPadding** — 
- **pageMaxWidth** — 1220px

## Components

### Morning Recap News Feed

### Insider Transactions List

### Earnings Alert Notification Banner

### Primary Navigation Link

**Role:** Interactive element

Text only, Pure White (#ffffff) on hover, otherwise Slate Text (#868f97). Uses Calibre 14px weight 400.

### Navigation Button - Ghost

**Role:** Interactive element

Circular button with 50% border radius. Background transparent, border #ffffff, text Pure White (#ffffff). On hover, background rgba(255, 255, 255, 0.08). Used for quick actions in nav.

### Navigation Button - Filled

**Role:** Interactive element

Pill-shaped button with 99px border radius. Background Light Smoke (#e6e6e6), text Black (#000000). Padding 7px vertical, 16px horizontal. Used for primary CTAs in nav.

### Notification Bubble Button

**Role:** System feedback

Rounded rectangular button with 6px border radius. Background rgba(255, 255, 255, 0.05). Border bottom rgba(255, 255, 255, 0.1). Text Light Smoke (#e6e6e6). Padding 3.75px vertical, 8-10px horizontal. Shadow: rgba(0, 0, 0, 0.85) 0px 1px 0px 0px. Used for compact alerts.

### App Preview Card

**Role:** Content display

Background Obsidian Deep (#131313). Border radius 16px. Shadow: Rgba(0, 0, 0, 0.8) 0px 0px 44px 0px. Used for displaying UI previews effectively with elevation.

### Pill Accent Tag

**Role:** Categorization/state

Pill-shaped with 99px border radius. Background transparent, text Light Smoke (#e6e6e6) or Emerald Profit (#4ebe96). Calibre 12px weight 400. Used for labels like 'Buy', 'Sell', 'Gainers', 'Losers'.

### Profile Avatar

**Role:** User identification

Circular graphic with 50px border-radius, often containing a small image. Used for user or entity representation in lists.

## Layout

The page primarily uses a max-width contained layout of 1220px, with content centered. The hero section is full-bleed, featuring a large, dark visual with a prominent centered headline and descriptive text. Subsequent sections alternate between full-bleed dark backgrounds and slightly lighter dark gray backgrounds (Obsidian Deep) to create visual rhythm. Content within sections often employs a prominent left-aligned headline followed by a flexible grid or alternating text-left/image-right (or product screenshot) arrangements. Card grids are used for features, showcasing clear divisions. Vertical spacing between sections is generous (900-1100px), contributing to a spacious feel despite the information density.

## Imagery

The visual language focuses on a mix of product screenshots and abstract, moody photography. Product screenshots are contained within device mockups (laptops, possibly tablets) and showcase the UI in context, often with a slight perspective to imply depth. Photography is dark and atmospheric, focusing on solitary figures or close-ups, often with obscured faces, suggesting introspection or focused work. Imagery serves to establish mood and showcase the product's interface rather than being purely decorative. Icons are mono-color and outline-based, or filled and simple, reinforcing the clean, functional aesthetic.

## Dos & Donts

### Do

- Use Midnight Ink (#0b0b0b) for primary page backgrounds to establish the dark theme.
- Apply Calibre font family with weight 400 for body text and 600 for prominent subheadings.
- Employ Cosmic Blue (#479ffa) exclusively for interactive elements and active states.
- Utilize Solar Flare (#ffa16c) for decorative headings and key value highlights.
- Maintain a clear visual hierarchy by differentiating surfaces with Obsidian Deep (#131313) for elevated elements on Midnight Ink (#0b0b0b) backgrounds.
- Apply 99px border radius for all primary calls-to-action buttons for a distinct pill shape.
- Space elements using multiples of 4px and 8px, reserving larger increments for section gaps (900-1100px) and card padding (18px vertical, 20px horizontal).

### Don't

- Do not use chromatic colors other than Cosmic Blue (#479ffa) or Solar Flare (#ffa16c) for branding or interactive elements.
- Avoid using drop shadows on elements that are not meant to signify elevation, like primary page backgrounds.
- Do not use border radii smaller than 6px for interactive elements; for cards and larger containers, use 16px.
- Never lighten text color for emphasis in a dark theme; rely on Pure White (#ffffff) for primary text and Light Smoke (#e6e6e6) or Slate Text (#868f97) for secondary/tertiary.
- Avoid dense information blocks without sufficient elementGap (min 4px, avg 8-20px) to maintain legibility.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #ffffff (Pure White)
- Background: #0b0b0b (Midnight Ink)
- CTA: #e6e6e6 (Light Smoke) with #000000 text
- Accent: #479ffa (Cosmic Blue)
- Elevated Surface: #131313 (Obsidian Deep)

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Full-bleed background #0b0b0b. Centered headline 'Make better investments.' using Calibre 54px weight 700 letterSpacing -0.08em #ffffff. Subtext 'Explore a new era of effortless financial research at your fingertips.' using Calibre 18px weight 400 #868f97. Include a 'Learn more' button: background #e6e6e6, text #000000, 99px border radius, 7px 16px padding.
2. **Generate a Feature Card:** Background #131313, 16px border radius, padding 18px 20px. Headline 'Morning recap' using Calibre 16px weight 600 #ffffff. Body text 'Trump's renewed tariff push is shaking markets...' using Calibre 14px weight 400 #868f97. Add an inactive ghost button in the top right, 50% border radius, background rgba(0,0,0,0), border #ffffff, text #ffffff.
3. **Design a Navigation Bar:** Background #0b0b0b. Left-aligned brand logo (use a placeholder icon, e.g., square). Right-aligned menu items: 'Features', 'Pricing', 'Updates', 'Students', 'App' (Calibre 14px weight 400 #868f97, #ffffff on hover/active). Include a 'Learn more' button: Background #e6e6e6, text #000000, 99px border radius, 7px 16px padding.
4. **Create a Pill-shaped Tag:** Background transparent, border #ffa16c (Solar Flare). Text 'Buy' (Calibre 12px weight 500 #ffa16c), 99px border radius. Padding 4px 8px. Simulate for a stock transaction list.

---
_Source: https://styles.refero.design/style/733e6475-892a-4138-8835-bf40344df317_
