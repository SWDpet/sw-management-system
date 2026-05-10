# Letter — Design Reference

> Sublime Depth, Minimal Interface

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://letter.co](https://letter.co) |
| Refero page | [https://styles.refero.design/style/bcfc6cb0-1b39-4f3f-a95e-bd7b563b0efc](https://styles.refero.design/style/bcfc6cb0-1b39-4f3f-a95e-bd7b563b0efc) |
| Theme | light |
| Industry | fintech |

## Overview

Letter employs a 'dark academia meets finance' aesthetic, characterized by dramatic, almost painterly dark backgrounds with glowing abstract forms that provide depth and visual interest. Over this, a minimalist, high-contrast typography system ensures clarity and seriousness. Components are unfussy, with sharp corners and subtle borders, allowing the rich backgrounds to take center stage, creating an atmosphere of understated luxury and technological sophistication. The overall impression is one of grounded authority with a hint of digital mystique.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight | `#000000` | neutral | Page backgrounds, hero sections, primary text for dark backgrounds |
| Canvas White | `#ffffff` | neutral | Card backgrounds, primary text on dark elements, button fills |
| Carbon | `#191b1f` | neutral | Primary text on light backgrounds, hero subheadings, button backgrounds |
| Ghost Gray | `#f6f9f9` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Whisper Gray | `#e6ebec` | neutral | Lightest grey for borders and disabled text |
| Stone Whisper | `#9fabad` | neutral | Muted secondary text, copyright information |
| Deep Teal | `#186f64` | brand | Teal action color for filled buttons, selected navigation states, and focused conversion moments. |
| Royal Violet | `#536eff` | accent | Violet action color for filled buttons, selected navigation states, and focused conversion moments. |
| Grape Dusk | `#644bc4` | accent | Violet action color for filled buttons, selected navigation states, and focused conversion moments. |
| Ocean Gaze | `#154ea5` | accent | Blue action color for filled buttons, selected navigation states, and focused conversion moments. |
| Lilac Mist | `#e6def0` | accent | Hairline borders, dividers, input outlines, and card edges on light surfaces. |
| Mint Glaze | `#eefcef` | accent | Hairline borders, dividers, input outlines, and card edges on light surfaces. |
| Canyon Haze | `#fcede1` | accent | Hairline borders, dividers, input outlines, and card edges on light surfaces. |

## Typography

### Albra Sans

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 22px, 46px, 80px |
| lineHeight | 1.10, 1.20 |
| letterSpacing | 0.0200em |
| substitute | Playfair Display |
| role | Display and large headings — evokes a sense of established authority and refined impact through its robust weight and subtle tracking. |

### Neufile Grotesk Extended

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 13px, 16px, 28px |
| lineHeight | 1.00, 1.30, 1.40, 2.00 |
| letterSpacing | normal |
| substitute | Inter |
| role | Body copy, navigation, buttons, and subheadings — a compact, strong sans-serif that provides modern legibility without sacrificing character. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 2 |  |
| body | 16 |  | 1.4 |  |
| subheading | 22 |  | 1.1 | 0.44 |
| heading-sm | 28 |  | 1.3 |  |
| heading | 46 |  | 1.2 | 0.92 |
| display | 80 |  | 1.2 | 1.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| links | 2px |
| buttons | 2px |

- **elementGap** — 16px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 1440px

## Components

### Primary Dark Button

**Role:** Call to action for joining or getting started.

Background: Carbon (#191b1f), Text: Canvas White (#ffffff), Border: Canvas White (#ffffff), Radius: 2px, Padding: 12px vertical, 27.2px horizontal. Uses Neufile Grotesk Extended, weight 400.

### Secondary Light Button

**Role:** Alternative call to action, often for signing in or less prominent actions.

Background: Canvas White (#ffffff), Text: Carbon (#191b1f), Border: Carbon (#191b1f), Radius: 2px, Padding: 12px vertical, 27.2px horizontal. Uses Neufile Grotesk Extended, weight 400.

### Teal Action Button

**Role:** Primary action for 'Invest' or 'Browse' related features.

Background: Deep Teal (#186f64), Text: Canyon Haze (#fcede1), Border: Canyon Haze (#fcede1), Radius: 2px, Padding: 12px vertical, 27.2px horizontal. Uses Neufile Grotesk Extended, weight 400.

### Violet Action Button

**Role:** Primary action for 'Borrow' related features.

Background: Royal Violet (#536eff), Text: Whisper Gray (#e6ebec), Border: Whisper Gray (#e6ebec), Radius: 2px, Padding: 12px vertical, 27.2px horizontal. Uses Neufile Grotesk Extended, weight 400.

### Ghost Card

**Role:** Decorative card used as a background or for subtle visual separation without strong outlines.

Background: transparent (rgba(0,0,0,0)), Border: none, Radius: 0px, Padding: 0px.

### Surface Card

**Role:** Content card providing a slightly raised, clean surface for information.

Background: Ghost Gray (#f6f9f9), Border: none, Radius: 0px, Padding: 0px.

### Navigation Link

**Role:** Top-level navigation items.

Text: Carbon (#191b1f), Neufile Grotesk Extended.

## Layout

The site uses a contained page model with a maximum width of 1440px, centered on the screen. The hero section is full-bleed and dramatic, featuring a large centered headline and subtext over an abstract dark background. Subsequent sections often alternate between two primary content arrangements: a full-width header over a light background, and two-column layouts where text is left-aligned and paired with a visual element on the right (or vice-versa). Vertical rhythm is established through consistent section gaps, creating a spacious and unhurried feel. Navigation is a minimalist top bar, sticky at the top, integrating branding with key menu items and action buttons.

## Imagery

The visual language is characterized by abstract 3D rendered forms with metallic or translucent surfaces, often set against a gradient-heavy, dark, moody background. These visuals act primarily as decorative atmosphere and brand representation, rather than explanatory content. They are typically contained within sections, not full-bleed images, and may feature subtle blur filters. On lighter sections, these forms appear to be more crystalline and less moody. The overall effect is sophisticated and slightly mysterious, fitting for a high-net-worth financial institution.

## Dos & Donts

### Do

- Prioritize Neufile Grotesk Extended for all body text, buttons, and navigation, maintaining normal letter-spacing for high legibility.
- Use Albra Sans with 0.0200em letter-spacing for all headings and display text (`h1`-`h3`) to convey sophistication and impact.
- Apply a consistent 2px border radius to all interactive elements like buttons and links to maintain a sharp, deliberate feel.
- Employ Midnight (#000000) for hero section backgrounds and large textual content over them, creating a dramatic, immersive experience.
- Use Ghost Gray (#f6f9f9) as a subtle background for content sections or light cards, contrasting with the dramatic dark hero.
- Reserve Deep Teal (#186f64) for primary calls to action, ensuring attention is drawn to key interactive elements.
- Maintain maximum content width at 1440px, centering all major content blocks to ensure consistent presentation.

### Don't

- Avoid using curved or soft shapes; all radii should be sharp and minimal (2px) or absent (0px).
- Do not introduce strong drop shadows; the design relies on background gradients and subtle surface changes for depth, not elevation.
- Steer clear of overly saturated, single-color backgrounds for main content areas; maintain the subdued, high-contrast palette.
- Do not use generic system fonts; stick to Neufile Grotesk Extended and Albra Sans for brand continuity.
- Refrain from using thin weights for body or navigation text; Neufile Grotesk Extended should be used at 400 or 500 weight.
- Do not deviate from the established 12px vertical and 27.2px horizontal padding for primary actions; consistency is key.
- Avoid overly dense information layouts; utilize the 16px element gap and ample section gaps to create a sense of breathing room and exclusivity.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #191b1f
background: #ffffff
border: #e6ebec
accent: #536eff
primary action: #186f64 (filled action)

Example Component Prompts:
1. Create a hero section: Midnight (#000000) background. Headline 'Beyond banking' in Albra Sans, 80px, weight 600, Canvas White text, letter-spacing 1.6px. Subtext 'Thoughtful finance and modern wealth management for high net worth individuals.' in Neufile Grotesk Extended, 28px, weight 400, Canvas White text.
2. Create a Teal Action Button: Deep Teal (#186f64) background, Canyon Haze (#fcede1) text and border, 2px radius, 12px vertical / 27.2px horizontal padding, 'Invest Now' label in Neufile Grotesk Extended weight 400.
3. Create a Surface Card: Ghost Gray (#f6f9f9) background, 0px radius, 0px padding. Inside, a heading 'FDIC insured accounts' in Albra Sans, 22px, weight 600, Carbon (#191b1f) text, letter-spacing 0.44px. Below it, body text 'Checking accounts and cards for family and staff.' in Neufile Grotesk Extended, 16px, weight 400, Carbon (#191b1f) text.
4. Create a Secondary Light Button: Canvas White (#ffffff) background, Carbon (#191b1f) text and border, 2px radius, 12px vertical / 27.2px horizontal padding, 'Sign In' label in Neufile Grotesk Extended weight 400.

---
_Source: https://styles.refero.design/style/bcfc6cb0-1b39-4f3f-a95e-bd7b563b0efc_
