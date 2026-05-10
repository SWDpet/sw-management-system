# Ramp — Design Reference

> Deep Ocean Command Center – a stark, high-contrast control panel set against an endless dark expanse.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://ramp.com](https://ramp.com) |
| Refero page | [https://styles.refero.design/style/b38702a0-75ab-474c-9106-00b624535825](https://styles.refero.design/style/b38702a0-75ab-474c-9106-00b624535825) |
| Theme | dark |
| Industry | fintech |

## Overview

The Ramp design system evokes the precision and quiet authority of a financial control panel operating in a deep ocean environment. Its foundation is a dark theme, utilizing a rich `#0c0a08` background and surfaces that progressively lighten to reveal hierarchy, like submerged objects reflecting distant light. Typography, primarily Lausanne, sets a technical yet approachable tone with its clean geometry and a prominent use of 'ss01' font feature, ensuring figures and characters align perfectly. Vibrant yellow accents (`#e4f222`) serve as critical interaction indicators, cutting through the deep blue and near-black neutrals like sonar beacons, guiding the user through complex financial interfaces.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space Black | `#0c0a08` | neutral | Primary page background, base surface for components — establishes the dark theme depth. |
| Pure White | `#ffffff` | neutral | Primary text color, crucial for high contrast readability against dark backgrounds, interactive element text. |
| Ash Gray | `#1a1919` | neutral | Secondary surface background, used for lifted cards or subtle section breaks against the primary background. |
| Charcoal Black | `#000000` | neutral | Illustrative elements, icons, and occasionally deeper backgrounds for visual breaks. |
| Ivory White | `#f4f2f0` | neutral | Alternate background for specific white-themed cards or content sections, offering high contrast to deep blacks. |
| Slate Gray | `#999ba3` | neutral | Subtle text, inactive states, faint borders, and muted icons. |
| Iron Gray | `#4d505d` | neutral | Input field borders, secondary structural elements. |
| Midnight Ink | `#010412` | neutral | Subtle shadow color, creating depth on dark surfaces without being stark. |
| Ocean Abyss | `#0b0d1b` | neutral | Darkest button backgrounds, creating a sense of subtle elevation within the dark theme. |
| Silver Mist | `#d2cecb` | neutral | Light borders and dividers for cards and sections on lighter neutral backgrounds. |
| Sunbeam Yellow | `#e4f222` | accent | Primary calls to action, active navigation indicators, and key interactive elements. Provides strong visual focus. |
| Emerald Green | `#00d638` | semantic | Success states, positive indicators, and specific illustrative elements. |
| Deep Sea Blue | `#5683d2` | brand | Link text, informational elements, and subtle brand accents. |
| Electric Blue | `#0066ff` | brand | Interactive elements, graphical accents with high visibility. |
| Blaze Orange | `#ff492c` | semantic | Highlighting specific features or drawing attention to warnings. |

## Typography

### lausanne

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 10px, 13px, 14px, 16px, 18px, 20px, 24px, 28px, 40px, 48px, 64px |
| lineHeight | 0.74, 1.00, 1.04, 1.05, 1.14, 1.17, 1.20, 1.30, 1.33, 1.38, 1.43, 1.46, 1.50, 1.60 |
| letterSpacing | normal |
| fontFeatureSettings | "ss01" |
| substitute | system-ui, sans-serif |
| role | Primary typeface for all UI text, headings, body, and interactive elements. Its custom 'ss01' feature implies a focus on numerical precision and aligned tabular data, critical for a financial platform. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| fontFeatureSettings | "ss01" |
| substitute | sans-serif |
| role | Fallback typeface, used sparingly for specific legacy content or embedded imagery text, ensures broad compatibility. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 |  |
| subheading | 18 |  | 1.38 |  |
| heading-sm | 24 |  | 1.33 |  |
| heading | 40 |  | 1.17 |  |
| heading-lg | 48 |  | 1.14 |  |
| display | 64 |  | 1.05 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| nav | 4px |
| cards | 12px |
| input | 10px |
| buttons | 4px |
| default | 12px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Space Black Canvas | `#0c0a08` | 0 | Base canvas for the entire application, providing the foundational dark theme. |
| Ash Gray Surface | `#1a1919` | 1 | Secondary background for card-like elements or subtly elevated sections, adding slight visual depth. |
| Ocean Abyss Card | `#0b0d1b` | 2 | Background for prominent interactive elements and cards that require higher contrast against the canvas. |
| Translucent Accent | `#21212106` | 3 | Highly subtle background tint for specific card variants or overlays, often used on top of other surfaces to create a layered effect. |
| Ivory White Accent Area | `#f4f2f0` | 4 | Background for occasional light sections or specific content cards that require high contrast with dark elements. |

## Components

### Primary Call to Action Button

**Role:** Interactive

Filled with Sunbeam Yellow (`#e4f222`), text in Deep Space Black (`#0c0a08`), 6px border-radius, 10px vertical and 20px horizontal padding. This button is for primary actions and immediately draws attention.

### Secondary Ghost Button

**Role:** Interactive

Transparent background, text in Pure White (`#ffffff`), 6px border-radius, 10px vertical and 20px horizontal padding. Used for less prominent actions, maintaining visual hierarchy on dark backgrounds.

### Text Link Button

**Role:** Interactive

Transparent background, text in Pure White (`#ffffff`), no border-radius or padding. Used for inline or subtle actions where minimal visual weight is desired.

### Dark Filled Button

**Role:** Interactive

Filled with Ocean Abyss (`#0b0d1b`), text in Pure White (`#ffffff`), 6px border-radius, 16px all-around padding. Provides a high-contrast interaction button on lighter neutral areas.

### Product Feature Card

**Role:** Display

Background in Ivory White (`#f4f2f0`), 12px border-radius, no box shadow, 0px padding. Used for showcasing product features in a clean, contained block.

### Dark Marketing Card

**Role:** Display

Background in Ocean Abyss (`#0b0d1b`), 12px border-radius, no box shadow, 0px padding. Used for marketing content, offering deep contrast.

### Testimonial Card

**Role:** Display

Background in translucent black (`rgba(33, 33, 33, 0.024)`) on a dark background, 12px border-radius, no box shadow, 0px padding. For displaying social proof or customer stories.

### Outline Card

**Role:** Display

Transparent background, 0px border-radius, 32px vertical and 24px horizontal padding. Used for structural grouping with visible content, without adding a solid background.

### Input Field - Dark Background

**Role:** Interactive

Transparent background, text in Pure White (`#ffffff`), 10px border-radius, 16px horizontal padding. Placeholders are in Slate Gray (`#999ba3`). Used on dark page sections.

### Input Field - Light Background

**Role:** Interactive

Transparent background, text in Deep Space Black (`#0c0a08`), border in translucent black (`rgba(33, 33, 33, 0.1)`), 10px border-radius, 16px horizontal padding. Used on lighter page sections.

### Input Field - Focused/Active

**Role:** Interactive

Transparent background, text in Pure White (`#ffffff`), border in translucent white (`rgba(255, 255, 255, 0.3)`), 10px border-radius, 16px horizontal padding. Indicates active user input field.

## Imagery

The site heavily relies on product screenshots and 3D renders. Product screenshots are typically high-fidelity UI captures, often shown on modern devices (laptops, phones) with a clean, focused presentation. 3D renders feature abstract shapes or physical representations of credit cards, using monochromatic or deep-toned palettes that align with the dark theme. Photography is present in customer testimonial sections, showing professionals in office environments, often within a card-like container with rounded corners and muted or natural lighting. Iconography is generally outlined, monochromatic (white on dark, dark on light), with a consistent stroke weight, and serves primarily to explain features rather than purely decorate. The overall density of imagery is balanced, with large hero visuals and then smaller, contained images within feature sections.

## Elevation philosophy

The design system minimizes overt use of shadows, primarily relying on changing background colors of surfaces (Deep Space Black to Ash Gray to Ocean Abyss) and subtle borders to create depth. When shadows are present, they are either an almost imperceptible inset white line on the nav bar or implicitly soft, almost uncolored `box-shadow` values like `Midnight Ink` (`#010412`) to hint at elevation without heavy visual weight.

## Dos & Donts

### Do

- Prioritize text legibility by using Pure White (`#ffffff`) for primary text on Deep Space Black (`#0c0a08`) or Ocean Abyss (`#0b0d1b`) backgrounds.
- Use Sunbeam Yellow (`#e4f222`) exclusively for primary call-to-action buttons and active navigation states to clearly signal interaction points.
- Apply 12px border-radius to all cards and larger container elements for a consistent soft-edged feel.
- Maintain a clear visual hierarchy by utilizing the surface progression: Deep Space Black (`#0c0a08`) for canvas, Ash Gray (`#1a1919`) for elevated cards, and Ocean Abyss (`#0b0d1b`) for interactive elements on dark backgrounds.
- Ensure headings use the 'lausanne' font with its 'ss01' font feature for precise number and character alignment, crucial for financial data.
- Use 8px as the default `elementGap` for consistent spacing between UI elements, reserving smaller increments for fine-tuning dense interfaces.
- For all navigation and buttons, apply a 4px border-radius to create a distinct, slightly softer interaction target.

### Don't

- Do not use Sunbeam Yellow (`#e4f222`) for decorative purposes or non-interactive elements, as it dilutes its primary CTA role.
- Avoid arbitrary color choices; every color must map to a defined role within the palette to maintain cohesion.
- Do not introduce new shadow styles; adhere to the subtle inset white shadow (`rgba(255, 255, 255, 0.6) 0px 0px 2px 0px inset`) for nav and the default no shadows for cards.
- Do not use generic system fonts for primary UI text; always prefer 'lausanne' with its 'ss01' feature for brand consistency and precision.
- Avoid varying component padding; stick to the specified padding for buttons (e.g., 10px vertical, 20px horizontal) and cards (e.g., 24px for outlined cards) to maintain rhythmic spacing.
- Do not use pure black (`#000000`) for extensive text; reserve it for illustrative elements or very specific, high-contrast contexts.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Primary Text: `#ffffff` (Pure White)
- Page Background: `#0c0a08` (Deep Space Black)
- Primary CTA: `#e4f222` (Sunbeam Yellow)
- Input Border: `#4d505d` (Iron Gray)
- Link Text: `#5683d2` (Deep Sea Blue)

### Example Component Prompts
1. Create a hero section: Full-bleed Deep Space Black background. Headline 'Time is money. Save both.' using 'lausanne' font, 64px, weight 500, Pure White text, line-height 1.05. Subtext 'Easy-to-use corporate cards...' uses 'lausanne' 18px, weight 400, Pure White. Primary button 'Get started for free' is Sunbeam Yellow (`#e4f222`), text Deep Space Black (`#0c0a08`), 6px border-radius, 10px vertical 20px horizontal padding. Input field 'What's your work email?' has transparent background, Pure White text, Iron Gray border, 10px border-radius, 16px horizontal padding.
2. Design a feature card: Use Ivory White (`#f4f2f0`) for the background, 12px border-radius. Headline 'Ramp Intelligence' using 'lausanne' 24px, weight 500, Deep Space Black text. Accent text 'Put Ramp AI to work for you.' using 'lausanne' 14px, weight 400, Deep Space Black. Include a 'Learn more' link in Deep Sea Blue (`#5683d2`). Card padding 0px.
3. Implement a navigation bar: Sticky, 62px height, Deep Space Black background. Logo text 'ramp' in Pure White. Navigation links 'Products', 'Solutions' in Pure White, 'lausanne' 14px, weight 400. 'Sign in' button as transparent background, Pure White text, 6px border-radius, 10px vertical 20px horizontal padding. 'See a demo' button as Sunbeam Yellow (`#e4f222`) background, Deep Space Black text, 6px border-radius, 10px vertical 20px horizontal padding. Apply inset shadow `rgba(255, 255, 255, 0.6) 0px 0px 2px 0px inset`.
4. Construct a testimonial card within a horizontal carousel: Background `rgba(33, 33, 33, 0.024)`, 12px border-radius. Inner padding 32px vertical, 24px horizontal. Text 'Ramp is the only vendor...' in Pure White, 'lausanne' 16px, weight 400. Name 'Brandon Zell' in Pure White, 'lausanne' 14px, weight 500. Include a 'Read customer story' link in Deep Sea Blue (`#5683d2`).

---
_Source: https://styles.refero.design/style/b38702a0-75ab-474c-9106-00b624535825_
