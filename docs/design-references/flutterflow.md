# FlutterFlow — Design Reference

> Deep Space Command Center. Expansive dark surfaces punctuated by vivid violet glows, like stars in a night sky.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://flutterflow.io](https://flutterflow.io) |
| Refero page | [https://styles.refero.design/style/4e1ac04c-02ae-41cf-b588-3f6226a882f8](https://styles.refero.design/style/4e1ac04c-02ae-41cf-b588-3f6226a882f8) |
| Theme | dark |
| Industry | devtools |

## Overview

This design system evokes a 'deep space command center' atmosphere, achieved through a predominantly dark mode with complex, subtle gradients creating depth and energy. The primary visual tension arises from the interplay of smooth, expansive dark surfaces and bursts of vivid, luminous violet. Type is restrained, leveraging specific letter-spacing for refined legibility, while rounded corners on interactive elements provide a friendly counterpoint to the overall high-tech mood.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#060311` | neutral | Primary background, deep cards, text on light elements. This foundational dark sets the mood. |
| Slate Deep | `#161320` | neutral | Secondary background, UI panels and subtle card elevations. A slightly lighter dark for hierarchy. |
| White Star | `#ffffff` | neutral | Primary text, critical UI elements, button text on dark backgrounds. High contrast against dark surfaces. |
| Mist Gray | `#9ba1ae` | neutral | Secondary text, subtle descriptions, disabled states. Provides gentle contrast and visual relief. |
| Dark Star | `#333333` | neutral | Tertiary text, less prominent links and subtle accents. |
| Deep Violet | `#5800fd` | brand | Interactive elements, primary links, active states, key visual accents. Its vividness signifies importance. |
| Cosmic Indigo | `#2415c6` | brand | Illustrative elements, background gradients. A cooler, more muted violet that adds depth. |
| Dawn Violet | `#7066ed` | brand | Illustrative elements, subtle highlights, background gradients. A brighter, more ethereal violet. |
| Flare Violet | `#882fe8` | brand | Minor illustrative accents. Used sparingly for visual pop. |
| Action Violet | `#6d5ef9` | brand | Background for certain interactive states or containers. |
| Hero Gradient Outer | `#4b39ef` | brand | Large background sections, hero banners. Creates a subtle radial emanation, drawing focus without overwhelming. |
| Hero Gradient Inner | `#4b39ef` | brand | Large background sections, hero banners. Creates a subtle radial emanation, focusing on specific points. |
| Dividing Gradient | `#161320` | neutral | Used for smooth transitions between dark sections, providing a subtle texture. |

## Typography

### Urbanist

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 14px, 16px, 18px, 20px, 24px, 32px, 72px, 100px, 120px |
| lineHeight | 1.00, 1.20, 1.30, 1.50, 1.60 |
| letterSpacing | -0.045em at 120px, -0.038em at 100px, -0.03em at 72px, normal at body sizes |
| substitute | system-ui |
| role | This sans-serif is the primary display and body font. Its geometric structure provides a clean, modern feel, while varied letter-spacing at display sizes adds a touch of precision and sophistication, making large headlines feel carefully crafted rather than shouting. |

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 700 |
| weights | 300, 400, 700 |
| sizes | 13px, 14px, 16px, 18px, 20px, 24px |
| lineHeight | 1.50, 1.60 |
| letterSpacing | 0.09em at 24px, 0.057em at 20px, 0.05em at 18px, normal at body sizes |
| substitute | system-ui |
| role | Used for smaller text, navigation, and detailed information. Its higher letter-spacing values ensure readability at smaller sizes, especially crucial in functional UI elements where clarity is paramount. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.5 | 0.5 |
| body | 16 |  | 1.6 |  |
| subheading | 18 |  | 1.5 | 0.57 |
| heading-sm | 20 |  | 1.5 | 0.9 |
| heading | 24 |  | 1.3 |  |
| heading-lg | 32 |  | 1.2 |  |
| display | 72 |  | 1 | -0.03 |
| display-lg | 100 |  | 1 | -0.038 |
| display-xl | 120 |  | 1 | -0.045 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| links | 16px |
| buttons | 1440px |
| minorElements | 12px |

- **elementGap** — 24px
- **sectionGap** — 48px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Testimonial Card

### Trusted By Banner

### Text Only Button (Nav)

**Role:** Navigation and secondary actions

backgroundColor=transparent, color=#ffffff, border=none, borderRadius=0px, paddingY=40px, paddingX=16px. Used for subtle, non-intrusive interactive elements.

### Basic Card

**Role:** Information grouping without visual distinction

backgroundColor=transparent, borderRadius=0px, boxShadow=none, padding=0px. A structural container without presentational styling.

### Deep Card

**Role:** Content sections needing clear boundaries and subtle elevation

backgroundColor=#060311, borderRadius=24px, boxShadow=none, padding=0px. Emphasizes content through distinct, rounded dark surfaces.

### Shadowed Card

**Role:** Interactive or highlighted cards requiring significant visual lift

backgroundColor=transparent, borderRadius=20%, boxShadow=rgba(0, 0, 0, 0.2) 0px 4px 24px 20px, padding=0px. The circular radius with a large, soft shadow makes this card appear to float prominently.

## Layout

The page primarily uses a full-bleed layout, particularly in hero sections, with content centered within an implied max-width when present. The hero features a large, centered headline over a dark, gradient background. Sections alternate between deep dark backgrounds and areas with more subtle gradients. Content is frequently arranged in multi-column grids or stacked, centered blocks. Feature sections often employ a 2-column text-left/image-right alternating pattern. The navigation is a sticky top bar, providing persistent access without encroaching on content.

## Imagery

The visual language is a blend of abstract graphics, product screenshots, and minimal, geometric icons. Product screenshots are contained within device mockups or card elements, often overlapping with the dark UI, making them feel integrated. Illustrations lean towards gradients of brand violets, abstract shapes, and subtle glow effects, contributing to the 'deep space' feel. Icons are typically monocolor, filled, and use the brand's violet or white for interaction. The overall density is balanced, with imagery serving both decorative atmosphere and explanatory content roles, often nestled within the dark, gradient-rich backgrounds.

## Dos & Donts

### Do

- Use Midnight Ink (#060311) for primary dark backgrounds and Slate Deep (#161320) for secondary elevated dark surfaces to create depth.
- Apply White Star (#ffffff) consistently for primary text and critical UI elements against dark backgrounds.
- Reserve Deep Violet (#5800fd) for interactive elements, links, and active states to highlight interaction points.
- Ensure large headlines (display-xl and display-lg) use Urbanist with the specified negative letter-spacing values to maintain a refined visual impact.
- Use a `max-width` on content areas to prevent stretching, maintaining readability and aesthetic balance.
- Round all interactive button elements with a 1440px border-radius to achieve a consistent pill shape and friendly interaction target.

### Don't

- Do not use generic gray tones for interactive elements; all actionable items should leverage Deep Violet (#5800fd) or White Star (#ffffff) for strong visual cues.
- Avoid using box-shadows on cards unless explicitly for the 'Shadowed Card' variant; rely on background color changes for surface distinction.
- Do not deviate from the specified type scale and letter-spacing for headlines; these choices define the distinctive typographic voice.
- Do not introduce highly saturated colors outside the brand's violet palette, as they will clash with the established atmosphere.
- Avoid small, square button shapes; instead, use either the Text Only Button or the Pill Button variants for consistency.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- Text (primary): #ffffff (White Star)
- Background (base): #060311 (Midnight Ink)
- CTA (primary action): #5800fd (Deep Violet)
- Border (pill button): #ffffff (White Star)
- Accent (interactive): #5800fd (Deep Violet)

**Example Component Prompts:**
1. Create a Hero Section: Background is a full-bleed radial-gradient(circle farthest-side at 0px -30%, rgb(75, 57, 239), rgba(6, 3, 17, 0) 84%). Centered headline 'Build Better. Launch Faster.' using Urbanist, size 120px, weight 400, color #ffffff, letter-spacing -0.045em. Below it, a Pill Button (Navigation) 'Start for Free': transparent background, white text (#ffffff), 1px white border, 1440px border-radius, paddingY 40px, paddingX 25.6px.
2. Design a Feature Card: Background #060311 (Midnight Ink), 24px border-radius. Inside, a heading 'Visual Development' using Urbanist, size 24px, weight 500, color #ffffff. Body text using Inter, size 16px, weight 400, color #9ba1ae. Card padding should be 40px on all sides.
3. Implement a Navigation Bar: Background #060311. Left aligned logo. Right aligned navigation items using Urbanist, size 16px, weight 400, color #ffffff. Include a 'Log in' button as Text Only Button and 'Start for Free' as a Pill Button (Navigation).

---
_Source: https://styles.refero.design/style/4e1ac04c-02ae-41cf-b588-3f6226a882f8_
