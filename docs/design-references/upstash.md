# Upstash — Design Reference

> Crisp alpine air and digital green fields. A refreshing clarity guides every element, grounded by natural gradients.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://upstash.com](https://upstash.com) |
| Refero page | [https://styles.refero.design/style/e050061c-346d-44cc-92ba-6b22beb4a91f](https://styles.refero.design/style/e050061c-346d-44cc-92ba-6b22beb4a91f) |
| Theme | light |
| Industry | devtools |

## Overview

This design system projects a clean, high-tech sensibility with judicious use of a vibrant primary green against a predominantly light, neutral canvas. Subtle gradients and sophisticated typography choices, including a custom, tightly-tracked headline font, prevent the aesthetic from becoming sterile, instead imparting an approachable authority. The interplay of soft, rounded elements (pill buttons, large card radii) with the sharp text contrasts with the precision implied by the serverless technology, making complex infrastructure feel accessible.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Forest Pine | `#022c22` | neutral | Primary text across the site, interactive elements in their default state, navigation links. Creates a deep, serious foundation. |
| Spring Bud | `#00bc7d` | brand | Primary action buttons, prominent icons, and key highlights. Its vividness signals interaction and success against the calm backgrounds. |
| Evergreen | `#007a55` | brand | Subtle interactive states for links and icons, offering a slightly muted version of Spring Bud for secondary actions or hover states. |
| Sky Mist | `#e5e7eb` | neutral | Borders for cards, buttons, and other UI elements, creating soft definition. Also used as background for subtle secondary containers. |
| Paper White | `#ffffff` | neutral | Dominant page background, text on dark backgrounds, and card surfaces. Establishes the light theme. |
| Graphite | `#71717b` | neutral | Secondary text, muted interactive elements, and navigation items. Provides softer contrast than Forest Pine for less critical information. |
| Obsidian | `#000000` | neutral | Code block backgrounds and specific high-contrast text elements. Used sparingly for maximum impact. |
| Gradient Aura | `#00bc7d` | brand | Used for significant hero headings, subtly transitioning brand colors to a warm accent. Commands attention with a modern, dynamic flair. |
| Whisper Green | `#b4e6d6` | accent | Subtle visual accents and illustrations, adding a touch of verdant hue without overwhelming the primary green. |
| Alert Red | `#dc2626` | semantic | Semantically reserved for error states or caution, its vividness ensures immediate notice. |
| Warm Gold | `#f59e0b` | accent | Secondary accent for highlighting specific words or achieving a warm component background sheen. |
| Dark Code Background | `#09090b` | neutral | Background for code snippets, offering a high-contrast surface for monospace text. |
| Ghost Button Background | `#f5f5f5` | neutral | Background for secondary ghost buttons or subtle badges. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 12px, 13px, 14px, 16px, 20px, 24px, 48px |
| lineHeight | 1.00, 1.33, 1.40, 1.43, 1.50, 1.85 |
| letterSpacing | normal |
| substitute | Inter |
| role | Default body text, navigation items, buttons, and general UI elements. Its neutrality and readability provide a clean, functional base for the entire system. |

### __Inter_Tight_a3c0d3

| Key | Value |
| --- | --- |
| weight | 500, 600, 700 |
| sizes | 22px, 24px, 30px, 48px, 128px, 180px |
| lineHeight | 1.00, 1.20, 1.25, 1.33, 1.48 |
| letterSpacing | -0.0250em |
| substitute | Inter Tight |
| role | Headlines and prominent display text. The tight letter-spacing at larger sizes, combined with the slightly condensed form, conveys a modern, efficient, and direct tone without sacrificing legibility. |

### ui-monospace

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.50 |
| letterSpacing | -0.0730em |
| substitute | Menlo, Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace |
| role | Code snippets and technical data presentation. The negative letter spacing ensures code blocks remain compact and readable, aligning with the precision of a serverless platform. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.43 |  |
| body | 14 |  | 1.5 |  |
| body-lg | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.5 |  |
| heading-sm | 22 |  | 1.48 | -0.48 |
| heading | 24 |  | 1.25 | -0.6 |
| heading-lg | 30 |  | 1.2 | -0.75 |
| display | 48 |  | 1 | -1.2 |
| display-lg | 128 |  | 1 | -3.2 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| input | 6px |
| badges | 12px |
| buttons | 9999px |
| general | 12px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Components

### Product Tab Selector

### Code Example Block

### Testimonial Card with Customer Logos

### Primary Action Button

**Role:** Main call to action throughout the site.

oklch(0.552 0.016 285.938) text on oklch(0.552 0.016 285.938) background (visually renders as transparent). No explicit padding in style, text content determines size. 9999px border-radius. Primarily for utility-style interactive elements in the header area.

### Ghost Variant Button

**Role:** Secondary action button for highlighting features or complementary choices.

oklch(0.508 0.118 165.612) text on rgba(4, 120, 87, 0.08) background. 12px border-radius, 8px vertical padding, 16px horizontal padding. Prominently used for feature selection and interactive controls.

### Tab Header Button

**Role:** Used for content categorization and switching tabs.

oklch(0.552 0.016 285.938) text on oklch(0.97 0 0) background. Only top border-radius of 16px. No padding defined in style, implies content-driven layout. Used in code example sections for language selection.

### Header Navigation Button

**Role:** Compact interactive elements within the header, typically small utility links.

White text on transparent background. 6px border-radius, 0px vertical padding, 12px horizontal padding. Used for the 'Login' button in non-hero sections, and similar utility buttons.

### Feature Card

**Role:** Containers for showcasing product features or testimonials.

rgba(4, 120, 87, 0.08) background with 16px border-radius. No box-shadow, creating a flat, modern appearance. 32px padding on all sides.

### Icon Button (with image)

**Role:** Used to represent technologies, frequently in a list or grid.

Transparent background, oklch(0.552 0.016 285.938) text. 9999px border-radius (pill shape). Minimal horizontal/vertical padding (6px/12px). Features an inline icon/image.

## Layout

The page adheres to a max-width contained layout, with content centered. The hero section is full-bleed, featuring a large, gradient-colored headline over a subtle green-tinted background. Subsequent sections generally alternate between light backgrounds and sections with a light green tinted background (rgba(4, 120, 87, 0.08)), creating a soft visual rhythm. Content is arranged in alternating text-left/image-right (or vice-versa) patterns, and frequently uses a 3-column card grid for feature showcases. There's a notable use of code block sections that span the full width of the main content area. Vertical spacing between sections is consistently comfortable, emphasizing breathing room. The navigation is a sticky top bar, containing branding, key links, and a prominent login button.

## Imagery

The visual language for imagery is primarily functional and illustrative, focusing on product capabilities rather than human elements. For technologies represented (Redis, Vector, etc.), simple iconographic logos are used. Photography is absent. Illustrations are stylized, flat, and often incorporate the brand's Spring Bud color, sometimes with subtle green gradients. They serve an explanatory or decorative role, breaking up text with visual metaphors. Icons are monochromatic or duotone, utilizing the brand green consistently. Product shots are typically abstract, featuring technology or UI snippets, often against a subtle, tinted gradient background. Density is moderate, with images used to punctuate sections and explain features, rather than as dominant full-bleed hero elements.

## Elevation philosophy

This design intentionally avoids strong shadows for elevation. Instead, depth and layering are achieved through a combination of distinct background color changes (e.g., Paper White vs. rgba(4, 120, 87, 0.08)) and subtle borders (Sky Mist #e5e7eb). This creates a sense of clean separation and organization without the visual weight of drop shadows, reinforcing a light and airy feel.

## Dos & Donts

### Do

- Prioritize Inter Tight for all display headings large than 24px, using its characteristic -0.0250em letter-spacing for a modern, condensed feel.
- Use Spring Bud (#00bc7d) for primary calls to action, ensuring visual prominence and signaling interaction.
- Apply Forest Pine (#022c22) for all main body text and prominent links to maintain deep readability.
- Utilize 9999px border-radius for all buttons and interactive badges to create a consistent 'pill' aesthetic, pairing soft shapes with sharp typography.
- Maintain a comfortable information density by using 8px as the elemental spacing unit and 40px as the standard section gap.
- Employ the rgba(4, 120, 87, 0.08) filled background with 16px radius for secondary content groupings and interactive ghost buttons.
- Leverage the Gradient Aura for hero section headlines to introduce a dynamic, multi-tonal brand presence.

### Don't

- Avoid using box-shadows on main cards or elements, the design relies on background color shifts and borders for depth, not elevation.
- Do not deviate from the specified typefaces; Inter and Inter Tight are core to the brand's voice.
- Refrain from introducing other vibrant colors outside the established brand and accent palette (Spring Bud, Evergreen, Warm Gold) to maintain visual focus.
- Do not use generic system colors for text; adhere strictly to Forest Pine (#022c22) and Graphite (#71717b) for text hierarchies.
- Avoid using sharp corners for interactive elements; 9999px (pill) or 12px/16px radii are preferred for buttons and cards.
- Do not use more than two distinct fonts on a single page, with ui-monospace reserved exclusively for code representation.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #022c22 (Forest Pine)
- Background: #ffffff (Paper White)
- CTA: #00bc7d (Spring Bud)
- Border: #e5e7eb (Sky Mist)
- Accent Background: rgba(4, 120, 87, 0.08)

### Example Component Prompts
1. Create a hero section: Full-bleed background `#ffffff` with a subtle linear gradient from `oklch(0.508 0.118 165.612)` to `rgb(253, 224, 71)` at the bottom. Centered headline 'Serverless Data Platform' using `__Inter_Tight_a3c0d3` 128px weight 700 with `-0.0250em` letter-spacing, using `linear-gradient(to right, oklch(0.508 0.118 165.612), oklch(0.696 0.17 162.48), rgb(245, 158, 11))` as text fill. Subheading 'The single platform for all your data needs' in Inter 24px weight 400, color `#71717b`. Below, a primary action button 'Start for Free' with `#00bc7d` background, `#ffffff` text, 9999px radius, and 16px 24px padding.
2. Create a 'Ghost Feature Button': Text 'Redis ®*' using Inter 16px weight 400, color `#022c22`. Transparent `oklch(0.552 0.016 285.938)` background and `rgba(4, 120, 87, 0.08)` border, 9999px border-radius, with 0px vertical and 0px horizontal padding, plus a suitable icon. Center the text and icon.
3. Create a 'Feature Card': Background `rgba(4, 120, 87, 0.08)`, 16px border-radius, no box-shadow. Content inside should have 32px padding on all sides. Use Inter 20px weight 600 color `#022c22` for its title, and Inter 14px weight 400 color `#71717b` for its description.
4. Create a code block wrapper: Background `#09090b`, 16px border-radius. Code text should use `ui-monospace` 14px weight 400, `#ffffff` color, and `-0.0730em` letter-spacing. Include tab buttons for selecting languages like 'TypeScript' and 'Python', using the 'Tab Header Button' variant.

---
_Source: https://styles.refero.design/style/e050061c-346d-44cc-92ba-6b22beb4a91f_
