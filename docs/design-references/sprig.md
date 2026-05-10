# Sprig — Design Reference

> White canvas, thoughtful function

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://sprig.com](https://sprig.com) |
| Refero page | [https://styles.refero.design/style/cbd8a058-6ecb-4f1b-9b5a-2bf2597826ee](https://styles.refero.design/style/cbd8a058-6ecb-4f1b-9b5a-2bf2597826ee) |
| Theme | light |
| Industry | ai |

## Overview

Sprig employs a soft, minimalist aesthetic with a focus on clear information hierarchy, using a primarily achromatic palette grounded by a deep navy. Strategic use of subtle gradients and large radii on imagery introduces a touch of warmth and visual interest. Typography is compact and precise, maintaining a high information density while generous vertical spacing creates an airy, uncrowded feel. Interactive elements are softly delineated with rounded corners and muted borders, emphasizing functionality over ornamentation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#faf9f8` | neutral | Page backgrounds, light surfaces, header backdrop. The primary visual canvas |
| Midnight Ink | `#0b2330` | neutral | Primary text, informational icons, button text, accented borders. The main dark anchor of the UI |
| Jet Black | `#000000` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Fog Gray | `#f3f3f3` | neutral | Subtle background for badges and secondary button backgrounds |
| Graphite | `#1c1a17` | neutral | Tertiary backgrounds, dark card fills |
| Slate Text | `#6e6d6a` | neutral | Muted secondary text, descriptive copy, ghost button text |
| Charcoal Button | `#272420` | neutral | Filled button background for primary actions. Provides strong contrast |
| Border Ash | `#e8e7e6` | neutral | Subtle borders, dividers, ghost button borders |
| Deep Space | `#141312` | neutral | Heading text, strong links. Darker, more authoritative text than Midnight Ink |
| Medium Gray | `#575653` | neutral | Body text, navigation links, secondary button borders |
| Ghost Button | `#8f8d8b` | neutral | Background for certain ghost buttons, creating very low contrast |
| Subtle Link | `#9a9a91` | neutral | Subtle link text and decorative strokes |
| Light Mauve Gradient | `#efdcb6` | accent | Decorative gradient for product showcases and section backgrounds. Adds a touch of atmospheric color |
| Peach Sunset Gradient | `#ffd9a0` | accent | Decorative gradient for headers and hero sections. Infuses warmth |
| Sea Mist Gradient | `#bad4d5` | accent | Decorative gradient for imagery or backgrounds. Cool, soothing effect |

## Typography

### ABC Diatype

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 14px, 16px, 24px, 32px, 40px |
| lineHeight | 1.20, 1.30, 1.40 |
| substitute | Inter |
| role | Headings, body text, links, and buttons. Its clean, geometric forms provide a modern, functional voice. |

### TT Commons Pro

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px, 18px, 40px |
| lineHeight | 1.50 |
| substitute | Montserrat |
| role | Used for specific body text, hero text, and badges, complementing ABC Diatype with geometric simplicity. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.4 |  |
| body-sm | 16 |  | 1.5 |  |
| body | 18 |  | 1.5 |  |
| subheading | 24 |  | 1.3 |  |
| heading | 32 |  | 1.2 |  |
| display | 40 |  | 1.2 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 100px |
| badges | 4px |
| buttons | 32px |
| deepCurve | 1600px |

- **elementGap** — 8px
- **sectionGap** — 80px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#faf9f8` | 1 | Primary page background and default surface for most content. |
| Fog Gray | `#f3f3f3` | 2 | Slightly elevated surfaces for badges and minor background accents. |
| Border Ash | `#e8e7e6` | 3 | Used for subtle borders and as a background for certain secondary elements. |

## Components

### Primary Filled Button

**Role:** Call to action button for key conversions.

Background: Charcoal Button (#272420), Text: Canvas White (#faf9f8), Border: 1px solid Canvas White (#faf9f8), Padding: 8px vertical, 16px horizontal, Radius: 32px.

### Secondary Outlined Button

**Role:** Supporting actions or navigation links.

Background: transparent, Text: Medium Gray (#575653), Border: 1px solid Medium Gray (#575653), Padding: 6px vertical, 12px horizontal, Radius: 32px.

### Ghost Header Button

**Role:** Subtle button with minimal styling for navigation or secondary actions in header.

Background: transparent, Text: Midnight Ink (#0b2330), Border: 1px solid transparent, Padding: 6px vertical, 12px horizontal, Radius: 32px.

### Subtle Information Badge

**Role:** Used for categorization or small labels.

Background: Fog Gray (#f3f3f3), Text: Midnight Ink (#0b2330), Padding: 48px vertical, 48px horizontal, Radius: 4px.

### Product Display Card

**Role:** Highlights features or product screenshots with a curved frame.

Background: transparent, Border: none, Shadow: none, Padding: 0px, Radius: 100px or 1600px for larger shapes.

## Layout

The page uses a contained layout with a maximum content width, centered on a Canvas White background. The hero section features a centered headline and description over the Canvas White, occasionally accented by a full-width subtle gradient at the bottom. Sections are separated by generous vertical spacing (80px), often alternating between content blocks on white and subtle gradient backdrops. Content is arranged predominantly in centered stacks for headlines and descriptions, with occasional two-column layouts featuring text on one side and a visual on the other, or multi-column card grids for features. Navigation is a consistent top bar that remains visible.

## Imagery

The site uses a mix of photographic and abstract elements. Photography often features tight product crops or individuals in professional settings, sometimes with a desaturated or subtly tinted color overlay (e.g., cool blue tones). Abstract graphics primarily consist of subtle linear gradients (Light Mauve Gradient, Peach Sunset Gradient) that serve as atmospheric backgrounds or design accents, often with large, organic, or significantly rounded shapes that spill out of bounds or create soft visual containers. Iconography is minimal, outlined, and monochromatic, used for functional clarity. Imagery primarily serves a decorative or atmospheric role.

## Dos & Donts

### Do

- Prioritize Canvas White (#faf9f8) as the dominant background color for body sections and interface elements.
- Use Midnight Ink (#0b2330) for primary text and strong link states, ensuring high contrast and legibility.
- Apply 32px radius to all buttons and navigation elements for a consistent soft, approachable feel.
- Separate content sections with generous vertical spacing (sectionGap: 80px) to create an open and spacious layout.
- Integrate subtle linear gradients like Light Mauve Gradient or Peach Sunset Gradient for decorative backgrounds or imagery, not for functional UI elements.
- Use ABC Diatype for all headings and general body text, with TT Commons Pro for specific hero text or badges.
- Utilize Charcoal Button (#272420) for filled primary CTAs and Border Ash (#e8e7e6) for ghost button borders.

### Don't

- Avoid using highly saturated colors for functional UI elements; reserve them for decorative imagery or gradients.
- Do not use dark gray/black backgrounds (Jet Black #000000 or Graphite #1c1a17) for large sections; they are for specific element contrasts.
- Do not break the 32px or 4px radius pattern for buttons and badges, respectively; maintain consistent corner treatments.
- Avoid excessive use of borders; lean on spacing and background color shifts to delineate sections.
- Do not introduce new font families; the system relies on ABC Diatype and TT Commons Pro for its typographic identity.
- Do not add drop shadows to components; the system relies on flat surfaces and subtle background shifts.
- Do not compress vertical spacing; maintain the spacious rhythm established by the 80px section gap and element gaps.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #0b2330
background: #faf9f8
border: #e8e7e6
accent: #efdcb6 (Light Mauve Gradient)
primary action: #272420 (filled action)

Example Component Prompts:
1. Create a primary call-to-action button: background Charcoal Button (#272420), text Canvas White (#faf9f8), 1px solid Canvas White (#faf9f8) border, 32px radius, 8px vertical padding, 16px horizontal padding, ABC Diatype 400 at 16px.
2. Create a navigation link in the header: text Medium Gray (#575653), transparent background, no border, 32px radius, ABC Diatype 400 at 16px.
3. Design a hero section headline: text Deep Space (#141312), ABC Diatype 500 at 40px, lineHeight 1.2, centered.
4. Produce a subtle information badge: background Fog Gray (#f3f3f3), text Midnight Ink (#0b2330), 4px radius, TT Commons Pro 400 at 16px, 48px padding all sides.
5. Create a secondary outlined button: background transparent, text Midnight Ink (#0b2330), 1px solid Border Ash (#e8e7e6) border, 32px radius, 6px vertical padding, 12px horizontal padding, ABC Diatype 400 at 16px.

---
_Source: https://styles.refero.design/style/cbd8a058-6ecb-4f1b-9b5a-2bf2597826ee_
