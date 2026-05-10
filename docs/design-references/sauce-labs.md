# Sauce Labs — Design Reference

> Dark teal command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://saucelabs.com](https://saucelabs.com) |
| Refero page | [https://styles.refero.design/style/d271a6c4-942f-4abf-a3de-66795f15f031](https://styles.refero.design/style/d271a6c4-942f-4abf-a3de-66795f15f031) |
| Theme | dark |
| Industry | devtools |

## Overview

Sauce Labs presents a command center aesthetic, blending a dark, deep canvas with vibrant, functional accent colors. Typography is crisp and purposeful, creating clear visual hierarchy against the subdued backgrounds. Components are lightweight yet defined by subtle borders and large, soft radii, suggesting approachability within a sophisticated technical environment. The overall impression is one of clarity and quiet power, where key information and actions are highlighted with precision.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Teal | `#132322` | neutral | Page backgrounds, prominent card surfaces, dark text on light backgrounds, outlined button borders and text |
| White Canvas | `#ffffff` | neutral | Content areas, light card surfaces, primary text on dark backgrounds, ghost button borders and text |
| Black Ink | `#000000` | neutral | Text on light backgrounds, decorative fills, some button borders |
| Fog | `#edf7f5` | neutral | Subtle surface elevation, alternative light card backgrounds |
| Charcoal Grey | `#0e1a19` | neutral | Dark body text, border color in some contexts |
| Medium Grey | `#666666` | neutral | Decorative fills primarily for icons and SVG elements |
| Light Grey | `#b2b6b4` | neutral | Muted text, subtle borders around elements |
| Silver Mist | `#d0d3d3` | neutral | Hairline separators and borders |
| Cool Stone | `#828786` | neutral | Distinct background for specific card types |
| Vibrant Green | `#3ddc91` | brand | Primary action buttons, active states, feature highlights – a vibrant activation color |
| Product Yellow | `#ffcd48` | accent | Accent for specific card backgrounds or illustrative elements |
| Muted Mint | `#97ddbc` | accent | Soft accent for card backgrounds, creating a gentle visual distinction |
| Slate Green | `#243b3a` | accent | Darker, secondary accent for card backgrounds |
| Soft Green | `#192b29` | accent | Even darker, tertiary accent for card backgrounds, close to the main Deep Teal |

## Typography

### Aeonik

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 15px, 16px, 18px, 20px, 24px |
| lineHeight | 1.20, 1.25, 1.40, 1.45, 1.50 |
| letterSpacing | -0.005em, 0.04em, 0.05em |
| substitute | Inter |
| role | Primary UI font for body text, links, and various UI elements. Its clean, contemporary feel underlies the functional aesthetic. |

### AeonikFono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 9px, 14px, 16px, 24px, 32px, 40px, 48px, 64px |
| lineHeight | 1.10, 1.12, 1.20, 1.22, 1.30, 1.75 |
| letterSpacing | 0.05em |
| substitute | Space Mono |
| role | Used for headings, buttons, and other prominent text elements, often in larger sizes. The geometric, almost monospace character, provides a technical, precise tone. Its generous tracking at larger sizes ensures crispness. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 14 |  | 1.25 | 0.04 |
| body-lg | 16 |  | 1.25 | -0.08 |
| subheading | 18 |  | 1.45 | -0.09 |
| heading-sm | 24 |  | 1.2 | -0.12 |
| heading | 32 |  | 1.22 | -0.16 |
| heading-lg | 40 |  | 1.1 | -0.2 |
| display | 64 |  | 1.12 | -0.32 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 20px |
| buttons | 32px |
| large-cards | 60px |
| round-elements | 56px |

- **elementGap** — 24px
- **sectionGap** — 24px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Teal Canvas | `#132322` | 0 | Primary page and section background, often providing the dark base. |
| Overlay Card (Deep Teal) | `#132322` | 1 | Slightly elevated, semi-transparent card surfaces on the primary background. |
| Accent Card (Slate Green) | `#243b3a` | 2 | Distinct, darker card backgrounds for specific content blocks. |
| White Canvas (Light) | `#ffffff` | 3 | Primary light surface for content sections, contrasting with dark elements. |
| Fog (Light Elevated) | `#edf7f5` | 4 | Subtly elevated light card backgrounds. |

## Components

### Ghost Button (Accent Border)

**Role:** Secondary action button for dark backgrounds.

Transparent background, 'Deep Teal' (#132322) border and text color, 56px border-radius, 14px vertical padding, 28px horizontal padding. Font: Aeonik weight 400, 16px. Creates a subtle, outlined presence.

### Ghost Button (White Border)

**Role:** Secondary action button for dark backgrounds.

Transparent background, 'White Canvas' (#ffffff) border and text color, 32px border-radius, 6px vertical padding, 10px horizontal padding. Font: Aeonik weight 400, 16px. Offers higher contrast on dark surfaces.

### Primary Action Button

**Role:** Main call-to-action.

Filled with 'Vibrant Green' (#3ddc91), 'Deep Teal' (#132322) text color, 32px border-radius, 6px vertical padding, 10px horizontal padding. Font: Aeonik weight 400, 16px. Signals a primary interactive element.

### Icon Button (Circular)

**Role:** Small, interactive icon container.

Transparent background, 'Light Grey' (#899191) border, 50% border-radius (circular), 8px padding all around. Text color 'Black Ink' (#000000, 54% opacity). Small, contained, and interactive.

### Overlay Card (Dark)

**Role:** Content card above dark section backgrounds.

Semi-transparent 'Deep Teal' (#132322, 51% opacity) background, 20px border-radius, no shadow, 24px vertical padding, 80px horizontal padding. Provides a slightly elevated, muted surface.

### Accent Card (Slate Green)

**Role:** Content card for visual distinction.

Solid 'Slate Green' (#243b3a) background, 16px border-radius, no shadow, 56px vertical padding, 32px horizontal padding. Creates a darker, more prominent surface.

### Badge Card (Vibrant Green)

**Role:** Decorative or status display card.

Solid 'Vibrant Green' (#3ddc91) background, 60px border-radius, no shadow, 0px padding. Used for small, highly visual elements.

### Accent Card (Muted Mint)

**Role:** Content card with soft green accent.

Semi-transparent 'Muted Mint' (#82c790, 60% opacity) background, 20px border-radius, no shadow, 40px padding all around. Offers a refreshing, softer visual break.

## Layout

The page primarily uses a max-content width layout, with elements often contained within a central column, but feature sections can extend to almost full-bleed with dark backgrounds. The hero section is dark and full-width, centered with a large headline and supporting text, alongside a prominent illustrative graphic. Section rhythm alternates between dark and light backgrounds, with consistent vertical spacing (approximately 24px for element gaps, 24px for card padding, and 24px for section gaps). Content arrangement frequently features centered stacks of text, or text to the left with visual elements (illustrations, screenshots, or card grids) to the right. Card grids (often 3-column) are used for feature presentation.

## Imagery

The site uses a combination of abstract, technical illustrations and tight product screenshots. Illustrations are dimensional, depicting UI elements floating in space with visible connections and abstract geometric shapes, often tinted with muted greens and yellows. Product screenshots are clean and focused, showing software interfaces. Icons are monochromatic, primarily outlined, with a moderate stroke weight. Imagery serves mostly an explanatory and atmospheric role, rather than decorative, creating a 'technical diagram' feel without being overly dense, often contained within cards or placed alongside text for direct explanation.

## Dos & Donts

### Do

- Use 'Deep Teal' (#132322) as the primary background for sections and cards to maintain the 'command center' feel.
- Apply 'Vibrant Green' (#3ddc91) exclusively for primary calls to action, active states, and critical highlights to ensure its impact.
- Utilize 20px or 60px border-radius for cards and 32px or 56px for buttons to maintain a soft, approachable aesthetic.
- Employ AeonikFono for all headings and prominent text elements, leveraging its crisp geometric form and calculated letter-spacing.
- Maintain a clear visual hierarchy by using 'White Canvas' (#ffffff) for primary text on dark backgrounds and 'Black Ink' (#000000) for text on light backgrounds.
- Leverage 24px for component and section gaps to ensure comfortable density and readability.
- Use light borders like 'Silver Mist' (#d0d3d3) for subtle separation rather than heavy dividers.

### Don't

- Do not introduce new saturated colors beyond the defined brand and accent palette; maintain a controlled chromatic presence.
- Avoid applying strong box-shadows; elevation is achieved through subtle background color shifts or minimal shadows like rgba(0, 0, 0, 0.04) 1px 0px 9px 2px.
- Do not use generic system fonts; stick to Aeonik for body text and AeonikFono for headlines to preserve the brand's typographic identity.
- Avoid arbitrary changes in border-radius; adhere to 20px for cards and 32px for buttons to maintain visual consistency.
- Do not use dark text on dark backgrounds; ensure sufficient contrast with 'White Canvas' (#ffffff) on 'Deep Teal' (#132322) and other dark neutrals.
- Do not overuse 'Vibrant Green'; preserve its role as a functional highlight, not a decorative element.
- Avoid tight spacing; always provide comfortable amounts of padding and margin, generally adhering to a 24px base for element and section gaps.

## Notes

### Agent Prompt Guide

primary action: #3ddc91 (filled action)
Create a Primary Action Button: #3ddc91 background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
• Generate a 'feature card' with an 'Overlay Card (Dark)' style at 24px vertical and 80px horizontal padding, 20px border-radius, background rgba(19,35,34,0.51). Inside, place a subheading: 'AI-Powered Insights' using AeonikFono weight 400, 24px, lineHeight 1.2, letterSpacing -0.12px, color 'White Canvas' (#ffffff).
• Design a secondary navigation link: 'Learn More' using Aeonik weight 400, 16px, lineHeight 1.25, letterSpacing -0.08px, color 'White Canvas' (#ffffff). This should appear as a simple text link.
• Create a client logo section on a 'White Canvas' (#ffffff) background. Display a company logo (e.g., Walmart) within a card using a 'Cool Stone' (#828786) background, 16px border-radius, and 56px vertical/32px horizontal padding. The logo itself should apply a CSS filter for monochrome display (brightness(0) invert(1)).

---
_Source: https://styles.refero.design/style/d271a6c4-942f-4abf-a3de-66795f15f031_
