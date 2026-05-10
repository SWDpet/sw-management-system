# Modern companies are built on Capital — Design Reference

> Midnight Ledger, Ember Accent

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://capital.xyz](https://capital.xyz) |
| Refero page | [https://styles.refero.design/style/37e20981-1f35-4314-87c1-fdb61ab2f0c0](https://styles.refero.design/style/37e20981-1f35-4314-87c1-fdb61ab2f0c0) |
| Theme | dark |
| Industry | fintech |

## Overview

Capital evokes a stark, sophisticated fintech experience operating in a dark, high-contrast environment. Monochrome canvases are punctuated by a single vivid red accent for calls to action, creating immediate emphasis. Typography is confident and generously sized, ensuring clarity against the deep backgrounds. Components maintain a minimal, almost invisible presence with subtle borders or ghost styles, letting the content and actions take center stage. The overall impression is one of restrained power and clear directives.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary background for pages and main content areas, providing a dark, immersive canvas |
| Ash Canvas | `#efece6` | neutral | Alternating background sections, surface fills for cards, and primary text color on dark backgrounds, offering high contrast and readability |
| Charcoal Surface | `#1a1a1a` | neutral | Subtle background for interactive elements and secondary container surfaces, maintaining a dark theme while offering visual progression |
| Onyx Shadow | `#131413` | neutral | Deeper background or shadow color for elements requiring minimal elevation or separation from the primary dark canvas |
| Slate Text | `#bfbcb7` | neutral | Secondary text color, muted descriptive text, and subtle borders on lighter background sections |
| Cadet Grey | `#8e8c87` | neutral | Supportive text, helper text, and decorative borders, providing a softer contrast |
| Deep Graphite | `#302f2f` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Ember Glow | `#ed5145` | brand | Primary call-to-action buttons and critical interactive elements — a vivid red that provides immediate focus and urgency against dark neutrals |
| Crimson Shadow | `#82403a` | brand | Subtle tint for shadows or decorative accents, echoing the primary brand color in a more subdued tone |
| Sunset Blush | `#ff7a70` | brand | Red supporting accent for decorative details and low-frequency emphasis. Do not promote it to the primary CTA color |

## Typography

### Muoto

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| weights | 300, 400, 500 |
| sizes | 12px, 14px, 16px, 20px, 28px, 32px, 72px, 90px, 115px |
| lineHeight | 1.15, 1.20, 1.40, 1.43, 1.50, 1.67 |
| letterSpacing | -0.0200em, -0.0120em, -0.0100em |
| substitute | Open Sans |
| role | Primary typeface for all headings, body text, and interactive elements. Its range of weights and nuanced letter-spacing contributes to the distinct modern and authoritative feel, with subtle tracking for larger sizes. |

### GT America Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.15, 1.60 |
| letterSpacing | 0.0800em |
| substitute | Space Mono |
| role | Used for smaller functional text such as labels, metadata, and code-like snippets, providing a technical contrast with its monospace styling and generous letter-spacing for legibility. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 |  |
| body-sm | 14 |  | 1.43 |  |
| body | 16 |  | 1.5 | -0.16 |
| subheading | 20 |  | 1.4 |  |
| heading-sm | 28 |  | 1.2 |  |
| heading | 32 |  | 1.2 |  |
| heading-lg | 72 |  | 1.15 | -0.86 |
| display | 90 |  | 1.15 | -1.08 |
| display-xl | 115 |  | 1.15 | -0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 3px |
| cards | 16px |
| buttons | 8px |
| ghostControls | 24px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | 0 | Base page background and primary dark canvas. |
| Onyx Shadow | `#131413` | 1 | Slightly elevated dark surfaces, providing subtle differentiation from the base background. |
| Charcoal Surface | `#1a1a1a` | 2 | Secondary container backgrounds for interactive elements or distinct content blocks within dark sections. |
| Ash Canvas | `#efece6` | 3 | Light background for cards and alternating content sections, creating visual breaks and high contrast. |

## Components

### Accent Filled Button

**Role:** Primary call-to-action button

Solid Ember Glow (#ed5145) background with white text (#ffffff), 8px border-radius, and 8px vertical padding, 24px horizontal padding. A distinct visual indicator for critical actions.

### Subtle Outlined Button

**Role:** Secondary action button/link

Transparent background with Deep Graphite (#302f2f) 1px border and Charcoal Surface (#1a1a1a) text. Has 24px border-radius, and 10px vertical padding, 24px horizontal padding. Conveys clickable actions without visual dominance.

### Monochrome Ghost Button With Border

**Role:** Tertiary action with emphasis

Transparent background with a 1px border of Slate Text (#bfbcb7) and text in the same color. Features 24px border-radius, and 10px vertical padding, 24px horizontal padding. Used for less prominent actions that still require a clear boundary.

### Product Feature Card

**Role:** Content grouping for features or informational blocks

Ash Canvas (#efece6) background with 16px border-radius and no box shadow. Content internal padding varies, but cards themselves are unpadded on initial render, relying on internal component spacing. Used for displaying feature sets or related information.

### Mini Tag

**Role:** Informational labels or status indicators

Small background with 3px border-radius text labels. The specific background color (e.g., #1a1a1a) and text color (e.g., #efece6) can vary depending on context, often using the Charcoal Surface and Ash Canvas combination. Padded for minimal visual footprint.

## Layout

The page structure alternates between full-width dark sections and contained, often lighter, content blocks. The hero section is full-bleed black with a large, centered white headline, establishing immediate brand presence. Content sections frequently adopt a max-width layout, centered on the page body. A recurring pattern involves a two-column arrangement, often text-left/visual-right or vice-versa, or stacked content for features within distinct sections. Card grids, like one displaying founder images, are used to organize visual content. Vertical spacing between sections is generous at 40px, creating comfortable breathing room. A sticky bottom bar provides tertiary navigation and a prominent 'Start' button.

## Dos & Donts

### Do

- Use Midnight Ink (#000000) as the default page background for sections, creating a dark, immersive experience.
- Apply Ash Canvas (#efece6) for primary body text on dark backgrounds, and as an alternating background to break up dark sections or for card surfaces.
- Reserve Ember Glow (#ed5145) exclusively for primary calls-to-action, such as filled buttons, to draw immediate attention.
- Employ Muoto at weight 300 for large headings and weight 400 or 500 for body text, maintaining legibility and a contemporary tone.
- Structure layouts with a consistent 40px `sectionGap` between major content blocks and a 16px `elementGap` for internal element separation.
- Utilize 8px border-radius for primary buttons and 16px for content cards, establishing clear visual boundaries and a measured softness.
- Introduce Charcoal Surface (#1a1a1a) for secondary container backgrounds or interactive UI elements within dark sections.

### Don't

- Do not introduce additional vibrant colors; maintain the high-contrast dark palette with red as the sole accent.
- Avoid heavy drop shadows on elevated elements; rely on subtle background color shifts or minimal box-shadows like rgba(26, 26, 26, 0.4) 0px 8px 24px 0px for depth.
- Do not deviate from the Muoto and GT America Mono font families; these are central to the brand's typographic identity.
- Avoid excessive use of outlined or ghost buttons when a primary action is available; prioritize clear calls to action with Ember Glow where intent is high.
- Do not use generic padding or spacing values; adhere to the 8px base unit and established tokens like 16px for element gaps.
- Never use text colors that create low contrast against the background; always ensure AAA contrast ratio, especially with light text on dark backgrounds.
- Avoid large imagery that breaks the UI's focus; imagery should be contained, functional, and support the content, not dominate.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #efece6
background: #000000
border: #302f2f
accent: #ed5145
primary action: #ed5145 (filled action)

### 3-5 Example Component Prompts
1. Create a Hero Section: Midnight Ink (#000000) background. Heading 'Modern companies are built on Capital' using Muoto, size 90px, weight 400, color #efece6, letter-spacing -1.08px. Centered content with a 40px bottom sectionGap.
2. Create a Call-to-Action Button: 'Start' button with Ember Glow (#ed5145) background, color #ffffff, 8px border-radius, 8px vertical padding, 24px horizontal padding, using Muoto weight 500.
3. Create a Feature Card: Ash Canvas (#efece6) background, 16px border-radius, no shadow, with 16px internal padding. The card should display a heading using Muoto, size 20px, weight 400, color #000000 and body text using Muoto, size 14px, weight 400, color #bfbcb7.
4. Create a Ghost Navigation Link: Text 'Founders' with transparent background, 1px border in Deep Graphite (#302f2f), text color Ash Canvas (#efece6), 24px border-radius, 10px vertical padding, 24px horizontal padding, using Muoto weight 400.
5. Create a Small Code-like Label: 'AVAILABLE NOW' using GT America Mono, size 12px, weight 400, color Cadet Grey (#8e8c87), with 0.08em letter-spacing. Use a small Charcoal Surface (#1a1a1a) background with 3px border-radius and minimal padding (e.g., 6px horizontal, 4px vertical).

---
_Source: https://styles.refero.design/style/37e20981-1f35-4314-87c1-fdb61ab2f0c0_
