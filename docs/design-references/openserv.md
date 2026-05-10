# OpenServ — Design Reference

> White space innovation laboratory

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://openserv.ai](https://openserv.ai) |
| Refero page | [https://styles.refero.design/style/063be10d-593d-4c81-a99e-a7543737b9db](https://styles.refero.design/style/063be10d-593d-4c81-a99e-a7543737b9db) |
| Theme | light |
| Industry | ai |

## Overview

OpenServ embodies a sharp, technical elegance with abundant whitespace and precise typography. It balances the starkness of monochrome UI with vibrant digital blues and greens, creating an atmosphere of focused innovation. Components feature soft, rounded corners and a ghost-like transparency, contributing to a lightweight, almost ethereal feel across interactive elements. The overall impression is one of restraint and clarity, prioritizing content against a clean backdrop.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated card backgrounds, key UI elements requiring visual separation. Provides a sharp, bright foundation for content |
| Ink Black | `#000000` | neutral | Primary text, strong borders, dark overlay backgrounds. Provides high contrast against light surfaces, defining structure and readability |
| Fog Gray | `#f5f5f5` | neutral | Secondary card surfaces, subtle background distinctions for content blocks. Offers a slight visual break from the primary canvas without introducing saturation |
| Steel Gray | `#a6a6a6` | neutral | Muted text, navigation links, secondary borders. Provides hierarchical distinction for less critical information or inactive states |
| Graphite | `#4d4d4d` | neutral | Subtle text, accent borders, less prominent information. Slightly darker than Steel Gray for increased legibility in specific contexts |
| Deep Blue | `#5f79ff` | brand | Primary action buttons, interactive elements, accent borders, headers. A vivid, almost neon blue that signifies interaction and brand presence |
| Electric Green | `#01fe93` | accent | Green action color for filled buttons, selected navigation states, and focused conversion moments |

## Typography

### OS Studio Grotesk

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px, 13px, 15px, 20px, 22px, 24px, 30px, 40px, 72px |
| lineHeight | 0.90, 1.00, 1.10, 1.14, 1.18, 1.25, 1.40 |
| letterSpacing | -0.0200em at 72px, -0.0170em at 40px, 0.0080em at 12px |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| role | Primary UI text, navigation, links, and headings. Its distinct font features convey a modern, technical precision, while varied letter-spacing maintains legibility across sizes. |

### OS Chronik

| Key | Value |
| --- | --- |
| weight | 300 |
| weights | 300 |
| sizes | 18px, 24px, 34px, 72px |
| lineHeight | 0.90, 1.00, 1.10, 1.20 |
| letterSpacing | -0.0200em at 72px, -0.0170em at 34px, -0.0150em at 24px |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| role | Headlines and prominent messages. The light weight (300) for large text creates a sense of authority through sophisticated understatement, rather than bold shouting. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| role | Fallback and minimal text, typically system-level information. Used sparingly where custom branding is less critical. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| subheading | 18 |  | 1.2 | -0.27 |
| heading-sm | 24 |  | 1.1 | -0.36 |
| heading | 40 |  | 1 | -0.68 |
| display | 72 |  | 0.9 | -1.44 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| card | 16px |
| pill | 100px |
| buttons | 12px |
| default | 16px |
| heroElement | 40px |
| interactiveElements | 12px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background |
| Fog Gray | `#f5f5f5` | 1 | Secondary card surfaces and subtle content block backgrounds |
| Elevated Navigation | `#ffffffcc` | 2 | Floating navigation bar with blur effect |

## Components

### Filled Primary Button

**Role:** Main call to action

Filled with Deep Blue (#5f79ff), white text, 14px border radius. Padding: 12px vertical, 24px horizontal. Uses OS Studio Grotesk 400.

### Pill Primary Button

**Role:** Prominent action with soft, rounded aesthetics

Filled with Deep Blue (#5f79ff), white text, 100px border radius. Padding: 10px vertical, 18px horizontal. Uses OS Studio Grotesk 400.

### Ghost Card Button

**Role:** Secondary action within cards, transparent and subtle

Background rgba(244, 244, 244, 0.35) (a semi-transparent Fog Gray), primary text color, 12px border radius. Generous padding: 32px top, 48px right, 48px bottom, 48px left. Uses OS Studio Grotesk 400.

### Subtle Dark Button

**Role:** Call to action on dark backgrounds, minimal visual weight

Background rgba(0, 0, 0, 0.9), text uses Ink Black. 40px border radius. No explicit padding, relies on content spacing.

### Hero Feature Card

**Role:** Displays key features or content blocks with a clean appearance.

Background Fog Gray (#f5f5f5), 16px border radius, no shadow. Content padding 0px.

### Content Card

**Role:** Standard content containers with a subtle background.

Background Fog Gray (#f5f5f5), 12px border radius, no shadow. Padding: 24px vertical, 20px horizontal.

### Elevated Navigation

**Role:** Top navigation bar for global site access.

Blurred background with `backdrop-filter: blur(10px)`. rgba(0, 0, 0, 0.1) 0px 0px 48px 0px shadow. White text for active items, Steel Gray for inactive. 16px border radius.

## Layout

The page maintains a full-bleed layout for the background, with content typically centered and constrained within a comfortable width, though no explicit maximum width is detected, implied by the generous whitespace. The hero features a large, often dark, centered headline, setting a stark, modern tone. Sections generally follow a consistent vertical rhythm, often featuring alternating white and Fog Gray backgrounds to delineate content blocks. Content is arranged in alternating text-left/image-right patterns or multi-column card grids, providing structured information. Navigation is a simple sticky top bar with minimal links, complementing the overall sparse and clear aesthetic.

## Imagery

Imagery is functional and abstract, focusing on product-related visuals or iconic representations rather than photography. Illustrations are minimal, often geometric or abstract, using brand colors to highlight concepts. Icons are generally filled and monochromatic, or utilize the signature brand blue. Visuals are contained and typically have rounded corners, integrating seamlessly into the UI rather than breaking out as full-bleed elements. The density is moderate, with images serving as explanatory or decorative accents rather than dominating content.

## Dos & Donts

### Do

- Prioritize Canvas White (#ffffff) for all main page backgrounds to maintain a clean, expansive feel.
- Use Deep Blue (#5f79ff) exclusively for primary interactive elements, ensuring all main calls to action are immediately recognizable.
- Apply OS Chronik Light (300) for all major headlines at 34px or larger, with letter spacing from -0.015em to -0.02em, to create a sense of understated authority.
- Implement soft, rounded corners for all interactive elements and content containers, specifically 12px for buttons and 16px for cards, using the exact values from 'spacing.radius'.
- Employ Steel Gray (#a6a6a6) for navigation links and secondary body text to establish clear visual hierarchy without distracting from primary content.
- Maintain a comfortable density with a base unit of 4px for all spacing decisions, specifically using card padding of 24px and element gaps of 10px.
- Utilize rgba(0, 0, 0, 0.1) 0px 0px 48px 0px for any elevated elements, ensuring a consistent, diffused shadow effect.

### Don't

- Never use highly saturated colors outside the defined brand (#5f79ff) and accent (#01fe93) palettes; maintain a largely achromatic scheme.
- Avoid harsh, square corners; ensure all containers and interactive elements adhere to the specified radii (12px, 16px, 40px, 100px).
- Do not introduce strong, dark backgrounds beyond specific focal sections; the site's primary theme is light.
- Refrain from using heavily weighted fonts for large headlines; OS Chronik 300 should convey confidence, not aggression.
- Do not deviate from the defined letter-spacing values for OS Studio Grotesk and OS Chronik; precise tracking is key to the typographic identity.
- Avoid complex shadow systems; stick to the single, diffused shadow outlined for elevation to maintain a lightweight UI.
- Do not clutter layouts with too many competing elements; favor ample whitespace and clear visual separation for content blocks.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #000000
accent: #01fe93
primary action: #5f79ff (filled action)

Example Component Prompts:
1. Create a primary action button: Fill with #5f79ff, text #FFFFFF, 14px border radius, 12px vertical padding, 24px horizontal padding. Text 'Open App' in OS Studio Grotesk 400, 15px.
2. Create a content card: Background #f5f5f5, 16px border radius, no shadow. Content area OS Studio Grotesk 400, 13px, #000000. Text 'Whitepaper summary here.'
3. Create a hero headline: Text 'Build · Launch · Run' in OS Chronik 300, 72px, #000000, letter-spacing -1.44px.
4. Create a secondary navigation link: Text 'Builders' in OS Studio Grotesk 400, 12px, #a6a6a6.

---
_Source: https://styles.refero.design/style/063be10d-593d-4c81-a99e-a7543737b9db_
