# Calendly.com — Design Reference

> Sky Blueprint on Bright Paper – Clarity and precision conveyed through deep blues and crisp whites, like an architect's plan on a fresh sheet.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://calendly.com](https://calendly.com) |
| Refero page | [https://styles.refero.design/style/9946887b-ffa9-4276-af81-ae6352795afb](https://styles.refero.design/style/9946887b-ffa9-4276-af81-ae6352795afb) |
| Theme | light |
| Industry | productivity |

## Overview

Calendly presents a bright, approachable interface with a strong focus on clear information hierarchy and intuitive interaction. The dominant visual pairing of deep indigo (#0B3558) for prominent text and an active blue (#006BFF) for interactive elements establishes a professional yet friendly tone. Subtle shadows and consistent corner radii on cards and buttons create a sense of tangible depth, grounding the digital experience in a visually comfortable space.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Indigo | `#0B3558` | brand | Primary text, prominent headings, inactive navigation items — provides a strong, authoritative anchor against the light background without being harsh black. |
| Action Blue | `#006BFF` | brand | Primary call-to-action buttons, active navigation links, highlighted interactive elements — signifies interactivity and positive action. |
| Lavender Glow | `#e55cff` | accent | Illustrative accents and background elements — adds an energetic, modern flair without competing with primary UI elements. |
| Royal Amethyst | `#8247f5` | accent | Secondary accent for graphical elements and supporting links — provides visual diversity while maintaining a vibrant, modern palette. |
| Sunset Gold | `#ffa600` | accent | Secondary accent for graphical elements and supporting links — offers warmth and contrast within the accent palette. |
| Skybound Blue | `#0099ff` | accent | Tertiary accent for graphical elements — contributes to the overall energetic and varied visual language. |
| Ocean Glimmer | `#BB32D5` | accent | Tertiary accent for graphical elements and supporting links — adds depth and variation to the accent tones. |
| Glacier Blue | `#004EBA` | semantic | Informational badges and alerts — a distinct blue hue for communicating status or details without being a primary CTA. |
| Snow White | `#ffffff` | neutral | Page backgrounds, card surfaces, primary content containers — the canvas upon which all elements reside. |
| Cloud Mist | `#F8F9FB` | neutral | Subtle background for UI elements, slightly off-white to create minimal contrast with pure white. |
| Pale Gray | `#E7EDF6` | neutral | Badge backgrounds, subtle borders, background elements — provides a soft, structural separation. |
| Slate Blue | `#476788` | neutral | Secondary text, subtle icon fills, supporting information — offers a softer voice than Midnight Indigo. |
| Steel Gray | `#A6BBD1` | neutral | Tertiary text, subtle borders, disabled states — indicates less emphasis and provides fine detail. |
| Platinum Tint | `#D4E0ED` | neutral | Borders for inactive fields, subtle dividers — defines boundaries with minimal visual weight. |
| Outline Gray | `#E6E6E6` | neutral | Separators, subtle borders, dividing lines — provides minimal contrast for visual structure. |
| Text Black | `#0A0A0A` | neutral | Base color for general body text and default links — ensures high contrast and readability on light backgrounds. |

## Typography

### Gilroy

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px, 28px, 38px, 50px, 68px, 80px |
| lineHeight | 1.00, 1.10, 1.20, 1.21, 1.40, 1.50, 1.60, 1.64, 1.71, 2.00 |
| letterSpacing | normal |
| substitute | Montserrat |
| role | The utilitarian workhorse of the system, Gilroy is used for all text from petite captions to commanding display headlines. Its clear, geometric forms contribute to the system's overall sense of clarity and modern professionalism. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.71 |  |
| body-lg | 16 |  | 1.6 |  |
| subheading | 18 |  | 1.6 |  |
| heading | 24 |  | 1.4 |  |
| heading-lg | 28 |  | 1.2 |  |
| display-sm | 38 |  | 1.21 |  |
| display | 50 |  | 1.1 |  |
| display-lg | 68 |  | 1.1 |  |
| display-xl | 80 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| large | 24px |
| small | 4px |
| badges | 50px |
| medium | 12px |
| buttons | 8px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Sign-up Button Group

### Booking Time Slot Card

### Feature Section — Connect your calendars

### Primary Call-to-Action Button

**Role:** Interactive Element

Filled button with Action Blue (#006BFF) background and Snow White (#ffffff) text. Features a moderate 8px border-radius and generous 6px vertical, 16px horizontal padding. Signals primary user actions.

### Ghost Button (Dark Text)

**Role:** Interactive Element

Transparent background button with Midnight Indigo (#0B3558) text and an implied underline or border (0px defined, relies on text color for prominence). Used for secondary actions that shouldn't compete with primary CTAs.

### Ghost Button (Neutral Text)

**Role:** Interactive Element

Transparent background button with Text Black (#0A0A0A) text, without explicit borders or padding in its base state. Used for less prominent actions, often in navigation or tertiary contexts.

### Light-Text Ghost Button

**Role:** Interactive Element

Transparent background button with Snow White (#ffffff) text, often used on dark backgrounds or headers where it needs to stand out without a fill. Features a 4px border-radius without explicit padding.

### Floating Content Card

**Role:** Content Container

Snow White (#ffffff) background card with a substantial 16px border-radius and a triple-layer shadow: rgba(71, 103, 136, 0.04) 0px 4px 5px 0px, rgba(71, 103, 136, 0.03) 0px 8px 15px 0px, rgba(71, 103, 136, 0.08) 0px 30px 50px 0px. Used for featured UI elements like booking forms, giving them visual prominence and a sense of depth.

### Informational Badge

**Role:** Tag/Label

Badge with Pale Gray (#E7EDF6) background, Glacier Blue (#004EBA) text, and a rounded 50px border-radius. Small 4px vertical, 8px horizontal padding. Used for concise labels or status indicators.

### Text Link

**Role:** Navigation/Interaction

Typically uses Text Black (#0A0A0A) for body text links, and Midnight Indigo (#0B3558) or Action Blue (#006BFF) for prominent links, often without an explicit underline in its default state, relying on color for differentiation. Interactive links may use Action Blue (#006BFF).

### Navigation Link

**Role:** Navigation/Interaction

Midnight Indigo (#0B3558) text in its default state, transitioning to Action Blue (#006BFF) on hover or active state. No explicit background or border, relying purely on text color for interaction feedback.

## Layout

The page structure predominantly follows a max-width contained model, centered on Snow White (#ffffff) or Cloud Mist (#F8F9FB). Hero sections feature a large, centered headline (Gilroy 68px/80px weight 700, Midnight Indigo) and descriptive text, often accompanied by a prominent call-to-action button, and a product mock-up often with background fluid shapes. Content sections alternate between centered stacks and two-column layouts (text on left, image/mockup on right, or vice versa) with consistent vertical spacing of 40px as detected for sectionGap. Navigation is a sticky top bar with a clear 'Get started' CTA. The overall density is comfortable, providing ample breathing room around content blocks.

## Imagery

The visual language for imagery is characterized by either product interface mockups or abstract, fluid shapes in vibrant accent colors (Lavender Glow, Royal Amethyst, Sunset Gold, Skybound Blue). Product mockups are typically contained within cards with soft shadows, showcasing the UI in context. Abstract shapes serve a decorative and energetic role, overlapping slightly and adding dynamism without being overwhelming. There are also simple, outlines and filled icons which supplement text, typically in Midnight Indigo or Slate Blue. Photography is minimal, appearing primarily in social proof sections as small, circular profile images. The overall density is balanced, with imagery serving to break up text and showcase product functionality rather than dominating the layout.

## Dos & Donts

### Do

- Use Gilroy font family for all typography to maintain a consistent visual voice.
- Prioritize Action Blue (#006BFF) for primary calls to action to guide user flow.
- Apply a 16px border-radius and the distinct triple-layer shadow (rgba(71, 103, 136, 0.04) 0px 4px 5px 0px, rgba(71, 103, 136, 0.03) 0px 8px 15px 0px, rgba(71, 103, 136, 0.08) 0px 30px 50px 0px) to all prominent, elevated cards.
- Maintain a clear hierarchy using Midnight Indigo (#0B3558) for headlines and primary text, and Slate Blue (#476788) for secondary information.
- Use Pale Gray (#E7EDF6) for subtle background separations or badge fills to maintain a light aesthetic.

### Don't

- Do not use highly saturated colors for large blocks of text; reserve them for interactive elements or accents.
- Avoid applying heavy, deep shadows on elements not intended to be interactive or highly emphasized.
- Do not introduce additional font families or decorative typefaces; adhere strictly to Gilroy.
- Do not deviate from the established 8px base unit for spacing, as it ensures comfortable density.
- Avoid using pure black (#000000) for text; use Text Black (#0A0A0A) for readability or Midnight Indigo (#0B3558) for a branded tone.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): #0B3558
- Background (Primary): #ffffff
- CTA (Active): #006BFF
- Border (Subtle): #D4E0ED
- Accent (Graphical): #e55cff

### 3-5 Example Component Prompts
1. Create a hero section: background Snow White (#ffffff), centered headline 'Easy scheduling ahead' (Gilroy 68px, weight 700, #0B3558, lineHeight 1.1), followed by body text 'Join 20 million professionals...' (Gilroy 20px, weight 400, #0A0A0A, lineHeight 1.6), and a Primary Call-to-Action Button 'Sign up for free'. Position a Floating Content Card with an example UI on the right.
2. Design a feature card: Snow White (#ffffff) background, 16px border-radius, and the triple-layer shadow (rgba(71, 103, 136, 0.04) 0px 4px 5px 0px, rgba(71, 103, 136, 0.03) 0px 8px 15px 0px, rgba(71, 103, 136, 0.08) 0px 30px 50px 0px). Include a Gilroy 24px weight 600 headline in Midnight Indigo (#0B3558) and body text in Slate Blue (#476788), with an informational badge 'New Feature' (Pale Gray background, Glacier Blue text, 50px radius, 4px vertical, 8px horizontal padding).
3. Generate a navigation bar: Snow White (#ffffff) background, with 'Log In' as a Ghost Button (Dark Text) and 'Get started' as a Primary Call-to-Action Button, both positioned right. Primary navigation links (Product, Solutions, Resources, Pricing) should use Midnight Indigo (#0B3558) text, Gilroy 16px weight 500.

---
_Source: https://styles.refero.design/style/9946887b-ffa9-4276-af81-ae6352795afb_
