# Notion — Design Reference

> Midnight machine hum — a deep indigo backdrop with precise, glowing elements that guide action.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://notion.so](https://notion.so) |
| Refero page | [https://styles.refero.design/style/2bf4c61f-de10-4614-ba1b-20c0453bd2a9](https://styles.refero.design/style/2bf4c61f-de10-4614-ba1b-20c0453bd2a9) |
| Theme | dark |
| Industry | saas |

## Overview

Notion's design system feels like a high-tech control panel, favoring a deep, almost inky blue background that provides a sophisticated setting for its functional UI. A restrained use of bright, vivid accent colors, primarily blues and an array of warm hues, pop against this dark canvas, indicating interactive elements and feature highlights. Typography is crisp and highly legible, with a meticulous approach to letter-spacing that implies precision, while subtly rounded corners on cards and buttons soften the otherwise sharp technical aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#02093a` | neutral | Primary background for dark sections, elevated card surfaces, and navigation elements. Provides depth as a foundational dark tone. |
| Canvas Dark | `#000000` | neutral | Base page background, navigation borders, and primary text on light surfaces. Serves as the darkest neutral in the system. |
| Ghost White | `#f6f5f4` | neutral | Light background for sections or secondary buttons. Provides contrast against darker neutrals. |
| Paper White | `#ffffff` | neutral | Text on dark backgrounds, interactive elements' text, and card backgrounds in light sections. Acts as the primary light element. |
| Deep Graphite | `#0b0b0b` | neutral | Dark text and borders on light surfaces, providing heavier visual weight than standard Canvas Dark. |
| Blue Frost | `#62aef0` | brand | Highlight elements, decorative accents, and subtle background fills. Evokes a cool, digital glow. |
| Ocean Glimmer | `#0075de` | accent | Interactive link text, borders for ghost buttons, and secondary action calls. A vibrant blue for clarity. |
| Sky Surge | `#097fe8` | semantic | Informational badges, active navigation states, and subtle decorative fills. Slightly brighter than Ocean Glimmer, for emphasis. |
| Action Indigo | `#455dd3` | brand | Primary Call-to-Action button background. A rich, saturated blue that stands out as the main interactive element. |
| Alert Red | `#f77463` | accent | Observed in heading backgroundColor, other backgroundColor. Semantic/state role was not supported by extracted badge/input evidence. |
| Team Teal | `#2a9d99` | accent | Accent backgrounds for features or categorization, offering a cool, composed highlight. |
| Sunny Yellow | `#ffc95e` | accent | Accent backgrounds for highlighting features or new content. A cheerful, bright yellow. |
| Warm Umber | `#b18164` | accent | Muted accent background for thematic categorization. Provides an organic contrast. |
| Coral Burst | `#ff8a33` | accent | Vibrant accent background for highlights, providing warmth and energy. |
| Fuchsia Flare | `#ff83dd` | accent | Playful accent background for special features or decorative elements. |
| Grape Glow | `#ad6ded` | accent | Vivid purple accent background for distinct categorization or decorative highlights. |
| Harvest Gold | `#ffb110` | accent | Slightly deeper yellow accent background for secondary emphasis. |
| Hot Chili | `#f64932` | accent | Intense red accent background for high-attention elements, more saturated than Alert Red. |
| Forest Green | `#1aae39` | accent | Vivid green accent background for positive indicators or highlights. |
| Cool Gray | `#c6c6c5` | neutral | Subtle border and text color for muted details. A soft, light gray. |
| Deep Slate | `#615d59` | neutral | Muted icon color and secondary text. A dark, slightly desaturated gray. |
| Input Border Gray | `#dddddd` | neutral | Border color specifically for input fields. Provides subtle definition. |
| Off White | `#f2f9ff` | neutral | Background for informational badges, offering a soft white tint. |

## Typography

### NotionInter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 12px, 14px, 15px, 16px, 20px, 22px, 24px, 26px, 40px, 42px, 54px, 64px |
| lineHeight | 0.83, 1.00, 1.04, 1.14, 1.20, 1.23, 1.27, 1.33, 1.35, 1.40, 1.43, 1.50 |
| letterSpacing | -0.036, -0.035, -0.033, -0.024, -0.011, -0.006, 0.01 |
| fontFeatureSettings | "lnum" on, "locl" 0 |
| substitute | Inter |
| role | The primary workhorse typeface for all UI elements, body text, and most headings. Its varied weights and precise letter-spacing allow for distinct hierarchy and high legibility across all sizes, from tiny labels to prominent headlines. The 'lnum' (lining numerals) feature ensures consistent number alignment in tables and data displays. |

### Lyon Text

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 32px |
| lineHeight | 1.25 |
| letterSpacing | normal |
| substitute | Georgia |
| role | Used sparingly for specific, stylized headings, potentially for editorial or feature-focused content. Its serif nature offers a tactile, almost printed feel compared to the geometric NotionInter. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0.01 |
| body-sm | 14 |  | 1.43 | -0.006 |
| subheading | 20 |  | 1.35 | -0.011 |
| heading-sm | 22 |  | 1.33 | -0.024 |
| heading | 26 |  | 1.27 | -0.033 |
| heading-lg | 40 |  | 1.23 | -0.035 |
| display | 64 |  | 0.83 | -0.036 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| badges | 9999px |
| inputs | 4px |
| buttons | 8px |
| general | 5px |

- **elementGap** — 8px
- **sectionGap** — 32px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Dark | `#000000` | 0 | Base page background, darkest foundational neutral. |
| Midnight Ink | `#02093a` | 1 | Primary background for hero sections and elevated dark cards, offering a rich, deep hue. |
| Paper White | `#ffffff` | 2 | Content cards and sections that stand out as bright focal points against the darker canvas. Also, input backgrounds. |
| Ghost White | `#f6f5f4` | 3 | Subtle background for specific sections or cards, lighter than Paper White but providing a soft contrast with Canvas Dark. |

## Components

### Primary Action Button

**Role:** Filled Button

Solid background button for primary actions. Uses Action Indigo (#455dd3) as background, Paper White (#ffffff) text (NotionInter, weight 400). Has 8px border-radius and 12px vertical, 16px horizontal padding.

### Ghost Header Navigation Button

**Role:** Ghost Button

Transparent background button for navigation items in the header. Uses Paper White (#ffffff) text (NotionInter, weight 400) and a transparent background. Has an 8px border-radius, with 6px vertical and 15px horizontal padding.

### Feature Card (Dark Background)

**Role:** Card

Card with a deep background for featuring product capabilities. Uses Midnight Ink (#02093a) as background, 12px border-radius, and 24px padding on all sides. No shadow.

### Feature Card (Light Background)

**Role:** Card

Card with a light background for various content blocks. Uses Paper White (#ffffff) as background, 12px border-radius, and 24px padding on all sides. No shadow.

### Ghost Link Button

**Role:** Ghost Button

Outlined button with Ghost White (#f6f5f4) text (NotionInter, weight 400), transparent background, and 4px border-radius. Padding is 5px vertical and 10px horizontal.

### Muted Navigation Button

**Role:** Ghost Button

Subtle ghost button for secondary navigation or actions. Uses Deep Slate (#615d59) text (NotionInter, weight 400), transparent background, and 8px border-radius. Padding is 12px vertical and 16px horizontal.

### White Card (No Padding)

**Role:** Card

A minimalist card variant, ideal for internal UI sections. Uses Paper White (#ffffff) as background, 12px border-radius, and no padding. No shadow.

### Off-White Section Card

**Role:** Card

Card for containing content in light sections, with a subtle off-white background. Uses Ghost White (#f6f5f4) as background, 12px border-radius, and no padding. No shadow.

### Basic Text Input

**Role:** Input

Standard text input field. Uses Paper White (#ffffff) background, transparent text color (Canvas Dark #000000 at 90% opacity), Input Border Gray (#dddddd) border, and 4px border-radius. Padding is 6px on all sides.

### Info Badge

**Role:** Badge

Informational badge. Uses Off White (#f2f9ff) background, Sky Surge (#097fe8) text (NotionInter, weight 400), 9999px radius (pill shape), and 4px vertical, 8px horizontal padding.

## Layout

The page primarily uses a max-width contained layout, centering content within a defined horizontal space, though the hero section breaks this rule, creating a full-bleed dark backdrop. The hero features a large, centered headline with associated text and call-to-action buttons over a deep indigo background. Sections generally alternate between dark full-bleed bands and contained light or off-white content blocks, creating a distinct visual rhythm. Content within these sections often uses a text-left/image-right or text-over-image pattern, often in 2-column or 3-column grids for feature lists or interactive elements. Spacing between sections is generous, allowing breathing room, and navigation is a persistent top bar (sticky header).

## Imagery

The visual language is split between functional UI elements and playful, abstract illustrated mascots. Product screenshots are typically contained within a light surface on the dark background, clearly showcasing the UI without heavy styling. Illustrations feature a flat, vector style with organic shapes and bright, vivid brand colors (pinks, blues, yellows, reds, greens) that contrast sharply with the dark UI, serving a decorative and atmospheric role rather than explanatory. Icons follow a filled, colorful style that matches the illustrative assets, adding personality. Density is text-dominant, with imagery used strategically to break up sections and highlight features.

## Dos & Donts

### Do

- Prioritize NotionInter typeface (#000000 on dark, #ffffff on light) for all body text and UI labels, ensuring consistent letter-spacing from the type scale.
- Use Midnight Ink (#02093a) as the default background for large hero sections and prominent cards in dark mode interfaces.
- Implement Action Indigo (#455dd3) exclusively for primary call-to-action buttons, pairing it with Paper White (#ffffff) text.
- Apply 12px border-radius to all cards and larger container elements, and 8px border-radius for buttons for visual consistency.
- Utilize Ocean Glimmer (#0075de) for all interactive link text and subtle button outlines to indicate interactivity without distraction.
- Maintain a clear visual hierarchy by limiting saturated accent colors (like Alert Red, Team Teal, Sunny Yellow) to small background fills for content categorization or highlights.

### Don't

- Do not use generic gray backgrounds; instead, leverage the nuanced shades of Midnight Ink (#02093a) and Canvas Dark (#000000) for visual depth in dark themes.
- Avoid using multiple vivid colors side-by-side without a clear functional purpose; accents should be deliberate and functional.
- Do not vary letter-spacing for NotionInter text unless it adheres strictly to the defined type scale's specific values for each role.
- Refrain from using Lyon Text for anything other than large, editorial-style headings, to preserve its distinct character.
- Do not introduce new border-radius values; stick to 12px for cards, 8px for buttons, 4px for inputs, and 9999px for pill-shaped elements.
- Avoid generic shadow effects; if elevation is needed, use the explicit box-shadow values provided, or rely on distinct background colors for depth.

## Notes

### Agent Prompt Guide

primary action: #ffffff (filled action)
Create a Primary Action Button: #ffffff background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Example Component Prompts:
Create a feature card: Paper White (#ffffff) background. 12px radius, 24px padding. Headline at 22px NotionInter weight 600, #000000, letter-spacing -0.0240em. Body text at 15px NotionInter weight 400, #0b0b0b, letter-spacing -0.0060em.
Create an informational badge: Off White (#f2f9ff) background. Sky Surge (#097fe8) text at 12px NotionInter weight 400, 9999px radius, 4px 8px padding.

---
_Source: https://styles.refero.design/style/2bf4c61f-de10-4614-ba1b-20c0453bd2a9_
