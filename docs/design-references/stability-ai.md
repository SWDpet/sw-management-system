# Stability AI — Design Reference

> Midnight command center; a sophisticated dark UI with precise information delivery.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://stability.ai](https://stability.ai) |
| Refero page | [https://styles.refero.design/style/f532c703-1179-465d-9933-7736df44d0ae](https://styles.refero.design/style/f532c703-1179-465d-9933-7736df44d0ae) |
| Theme | dark |
| Industry | ai |

## Overview

Stability AI's design evokes a sophisticated, high-tech command center with its deep, dark palette and vibrant purple accents. The dominant use of Achromatic Raven for backgrounds provides a sleek, near-black foundation, while crisp Archivo typography in white and light gray ensures clarity. Generous spacing and the playful use of fully rounded buttons contrast with otherwise sharp edges, creating a balance between serious technicality and modern approachability.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Achromatic Raven | `#1e1e1` | neutral | Primary background for pages and most elevated surfaces, providing a deep, consistent visual anchor. |
| Storm Cloud | `#383838` | neutral | Secondary background for cards, list items, and subtle differentiations between sections, adding depth to the dark theme. |
| Off-White Text | `#e5e7e6` | neutral | Primary text color for body copy, ensuring high contrast against dark backgrounds. |
| Polar White | `#ffffff` | neutral | Headline text, interactive element states, and high-emphasis information, drawing immediate attention. |
| Deep Plum Accent | `#a381ff` | brand | Primary brand accent for CTA buttons, active navigation items, and interactive links, providing a burst of energetic color against the dark theme. |
| Luminous Violet | `#776cff` | brand | Variant accent for borders and outlines, maintaining brand consistency while offering subtle visual hierarchy. |
| Slate Gray | `#bbbbbb` | neutral | Secondary text for less prominent information, such as captions or helper text, offering a softer presence than white. |
| Lavender Mist | `#b6a9c6` | neutral | Subtle accent for selected type elements and borders, hinting at the brand's chromatic palette within a neutral context. |

## Typography

### Archivo

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| sizes | 12px, 13px, 14px, 15px, 16px, 18px, 20px, 25px, 30px, 32px, 40px, 42px, 50px, 72px |
| lineHeight | 0.98-2.22 |
| letterSpacing | normal |
| substitute | Open Sans |
| role | Primary font for all textual content, from headlines to body text and UI elements. Its clean, sans-serif structure underpins the system's technical yet approachable feel. |

### Figtree

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 14px, 16px |
| lineHeight | 1.20 |
| substitute | Montserrat |
| role | Used specifically for button text, Figtree's slightly more geometric and contemporary feel adds a subtle distinction to interactive elements. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 |  |
| body-sm | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.5 |  |
| heading | 40 |  | 1.05 |  |
| heading-lg | 50 |  | 1 |  |
| display | 72 |  | 0.98 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 15px |
| input | 99px |
| buttons | 300px |
| default | 11px |

- **elementGap** — 10-18px
- **sectionGap** — 
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Announcement Banner

### CTA Button Group

### Mailing List Signup Form

### Primary Call to Action Button

**Role:** Primary interactive element for key actions

Background: Deep Plum Accent (#a381ff), Text: Polar White (#ffffff), Border Radius: 300px (full pill shape). Padding: 16px top/bottom, 20.8px left/right. Font: Figtree, weight 600, size 16px.

### Navigation Link Button

**Role:** Navigation items in header and footer

Background: transparent, Text: Polar White (#ffffff), Border: 1px Polar White (#ffffff) when active or hovered. Padding: 20px top/bottom, 0px left/right. Font: Archivo, weight 400.

### Email Input Field

**Role:** User input for email addresses

Background: Achromatic Raven (#1e1e1e), Text: Polar White (#ffffff), Border: 1px Polar White (#ffffff), Border Radius: 99px (pill shape). Padding: 12px top/bottom, 10px left/right. Placeholder text is Polar White (#ffffff).

### Select Input Field

**Role:** Dropdown select fields

Background: Achromatic Raven (#1e1e1e) in focus state (initially transparent), Text: Polar White (#ffffff), Border: 1px Polar White (#ffffff), Border Radius: 99px (pill shape). Padding: 12px top/bottom, 10px left/right.

### Hero Section Headline

**Role:** Large, impactful headlines

Font: Archivo, weight 400 (implied, as 72px is present), size 72px, line-height 0.98-1.07. Color: Polar White (#ffffff).

### Informational Card Base

**Role:** Container for content; appears as a content division rather than a distinct card

Background: transparent, Border Radius: 0px. No padding or box-shadow by default. Content defines its internal structure.

### Rounded Corner Card Variant

**Role:** Container for specific content that requires subtle rounding for visual grouping

Background: transparent, Border Radius: 15px (only on specific corners, e.g., 15px 0px 0px 15px or 0px 15px 15px 0px). No padding or box-shadow.

## Layout

The layout primarily uses a full-bleed background for sections, creating a cohesive, unbroken visual experience, within which content is usually centered, but not strictly constrained by a max-width, allowing for fluid arrangements. The hero section is characterized by a full-viewport image with centered, large headlines and a primary CTA. Content sections follow a rhythm of alternating text and visual elements, sometimes split into two columns (text-left/image-right or vice-versa). The page emphasizes spaciousness with generous vertical gaps between sections, avoiding dense information blocks. Navigation is handled by a sticky top bar, ensuring consistent access.

## Imagery

The visual language relies on high-resolution, artistic photography of diverse individuals, often featuring tech accessories like headphones. These images are typically full-bleed or large background elements, creating an immersive, aspirational atmosphere, and are heavily integrated into the UI rather than being isolated. Product screenshots are woven into the hero and other sections, showcasing the UI of the software in context. There's an absence of traditional cartoon-style illustrations, favoring realism and a sophisticated, professional aesthetic. Small, minimalist icons appear as outline-style for navigation and social media. The overall density is image-heavy in key sections like the hero, creating an immediate visual impact, while other sections are more text-dominant.

## Dos & Donts

### Do

- Use Achromatic Raven (#1e1e1e) as the primary background color for all main content areas.
- Apply Deep Plum Accent (#a381ff) for all primary call-to-action buttons and interactive links.
- Adopt a 300px border radius for all buttons and fully-rounded elements to maintain the signature pill shape.
- Utilize Archivo font consistently across all text elements, leveraging its various weights for hierarchy.
- Ensure high contrast by using Polar White (#ffffff) for headlines and Off-White Text (#e5e7e6) for body copy against dark backgrounds.
- Prioritize transparency for background elements (cards, badges) to allow base backgrounds to show through, creating depth.

### Don't

- Avoid using bright or pastel colors outside of the defined accent palette to maintain the sophisticated dark theme.
- Do not introduce sharp corners on interactive buttons; they must maintain the full pill (300px) or near-pill (99px) radius.
- Refrain from using drop shadows on cards or containers; depth is achieved primarily through background color variations and strategic spacing.
- Do not deviate from the Archivo and Figtree font families; they are central to the brand's typographic identity.
- Avoid excessive use of bold text – rely on size, contrast, and color to establish visual hierarchy, rather than heavy weights.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #e5e7e6 (Off-White Text)
- Background: #1e1e1 (Achromatic Raven)
- CTA: #a381ff (Deep Plum Accent)
- Border: #ffffff (Polar White)
- Accent: #776cff (Luminous Violet)

### Example Component Prompts
1. Create a primary CTA button: Background Deep Plum Accent (#a381ff), text Polar White (#ffffff), Border Radius 300px, padding 16px vertical, 20.8px horizontal. Text is Figtree, weight 600, size 16px.
2. Design a hero section headline: Text Polar White (#ffffff), Archivo font, size 72px, line-height 0.98. Background uses a full-bleed image.
3. Make an email input field: Background Achromatic Raven (#1e1e1e), text Polar White (#ffffff), border 1px Polar White (#ffffff), Border Radius 99px. Padding 12px vertical, 10px horizontal. Placeholder color is Polar White (#ffffff).
4. Generate a navigation link: Text Polar White (#ffffff), background transparent. On hover, apply a 1px bottom border of Polar White (#ffffff). Font is Archivo, weight 400.
5. Assemble a footer link: Text Off-White Text (#e5e7e6), background transparent. On hover, change text color to Deep Plum Accent (#a381ff). Set font to Archivo, size 15px.

### Elevation Philosophy

This design system explicitly avoids traditional drop shadows to indicate elevation. Instead, depth and hierarchy are achieved through subtle shifts in background tones (e.g., Achromatic Raven to Storm Cloud for subtle layered elements), generous white space, and the strategic layering of full-bleed imagery. The absence of shadows contributes to a clean, almost flat aesthetic that reinforces the high-tech and precise brand identity.

---
_Source: https://styles.refero.design/style/f532c703-1179-465d-9933-7736df44d0ae_
