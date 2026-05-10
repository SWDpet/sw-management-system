# Xbox.com — Design Reference

> Gaming console interface on a bright white canvas. Bold green accents against a stark white backdrop, with minimal rounded elements.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://xbox.com](https://xbox.com) |
| Refero page | [https://styles.refero.design/style/3792d0ca-6c74-4667-a64d-76efe9f87076](https://styles.refero.design/style/3792d0ca-6c74-4667-a64d-76efe9f87076) |
| Theme | light |
| Industry | media |

## Overview

The Xbox.com design system is a high-contrast, energetic experience that balances deep digital visuals with striking lime accents. Dominant dark imagery is anchored by a stark white background and punctuated by vivid green calls to action, creating an immediate sense of urgency and gaming excitement. A rigid, square aesthetic through hard edges pervades the interface, reflecting the precision and digital nature of console gaming.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Lumi Green | `#107c10` | brand | Primary brand color, used for prominent buttons, interactive elements, and branding accents. Conveys energy and interactivity. |
| Rich Meadow | `#054b16` | brand | Darker tone of Lumi Green, used for text on Lumi Green backgrounds and in combination with other greens to add depth. |
| Electric Lime | `#9bf00b` | brand | Background for secondary interactive elements, providing a vibrant, almost neon highlight. Often paired with Rich Meadow text. |
| Cyber Yellow | `#ffd800` | accent | Accents for badges and warning-like notifications, drawing immediate attention. |
| Deep Space Blue | `#0066ff` | accent | Illustration accent, appearing as an indicator or highlight within complex graphics. |
| Absolute Zero | `#000000` | neutral | Primary text, headers, and icon color. Provides maximum contrast against light backgrounds. |
| Snowfield White | `#ffffff` | neutral | Page backgrounds, card surfaces, and primary button text. The dominant background color. |
| Charcoal Depth | `#333333` | neutral | Secondary text and backgrounds in dark content blocks. Used for subtle contrast against darker photographic elements. |
| Whisper Gray | `#f2f2f2` | neutral | Subtle background for footer and other less prominent sections, acting as an off-white contrast. |
| Dark Steel | `#616161` | neutral | Subtle text and icon elements in navigation and body text, for a less stark contrast than Absolute Zero. |
| Onyx Shadow | `#262626` | neutral | Link colors and button text on light green backgrounds, for high readability with a touch of softness compared to pure black. |

## Typography

### Segoe UI

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| sizes | 11px, 13px, 15px, 16px, 20px, 24px, 46px |
| lineHeight | 1.20, 1.22, 1.25, 1.33, 1.45, 1.50, 2.27 |
| letterSpacing | -1.0em at 46px, 0.75em at 11px, normal otherwise |
| substitute | system-ui, sans-serif |
| role | Primary type for all UI elements: navigation, body text, general links, buttons, and most headings. Its clean, geometric form supports both readability and a digital aesthetic, with subtle letter-spacing variations to optimize readability at extreme sizes. |

### SegoeProBlack

| Key | Value |
| --- | --- |
| weight | 600, 900 |
| sizes | 15px |
| lineHeight | 1.23, 1.30, 1.53 |
| substitute | system-ui, sans-serif |
| role | Used for specific call-to-action buttons and emphasized links. Its heavier weight provides immediate visual pop, contrasting with the lighter Segoe UI for key interactive elements. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.45 | 0.75 |
| heading | 20 |  | 1.22 |  |
| heading-lg | 24 |  | 1.22 |  |
| display | 46 |  | 1.2 | -1 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| default | 0px |

- **elementGap** — 4-12px
- **sectionGap** — 48-80px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Components

### Game Pass Promotion Banner

### Xbox Category Navigation Bar

### Seasonal Sale Promo Card with Badge

### Primary Navigation Link

**Role:** Top-level navigation items

Segoe UI 400 at 15px, Absoulte Zero text. No background, no border, with 16px vertical and 12-30px horizontal padding. Underlines on hover/active.

### Text Link Button

**Role:** Secondary action button, typically below a primary CTA

Background: transparent. Text: Absolute Zero (#000000). Segoe UI 600 at 16px. 15px top, 30px bottom padding. 9px horizontal padding. No border-radius. Visible bottom border on hover/active.

### Hero Overlay Button

**Role:** Call-to-action over dark hero imagery

Background: rgba(0, 0, 0, 0.4). Text: Snowfield White (#ffffff). Segoe UI 400 at 15px. 6px top, 8px bottom padding. 0px horizontal padding. No border, 0px border-radius.

## Layout

The site uses a full-bleed layout for its hero section, featuring a large, dark background image with central text and calls to action. Subsequent sections alternate between full-width content blocks and contained grids, effectively segmenting information. Content arrangement is often asymmetrical, with text blocks adjacent to large visuals (text-left/image-right is common) creating dynamic compositions. A 3-column card grid is frequently used for displaying game titles, creating a dense yet organized presentation. The navigation is a sticky header bar at the top, maintaining constant access to core functionality. Overall density is compact, ensuring a wealth of information is visible without excessive scrolling, particularly in product grids.

## Imagery

The visual language of Xbox.com is dominated by high-fidelity, dramatic game key art and product photography. Photography is full-bleed in hero sections, often dark and moody, setting an immersive tone. Further down the page, game covers are presented in a grid-like structure, tightly cropped with sharp edges. Illustrations, when present, are colorful and detailed, serving to explain features or showcase game worlds. Icons are simple, outlined, and monochromatic (Lumi Green or black), functioning as functional navigation elements. The imagery's role is primarily decorative for atmosphere and explanatory for product features, featuring a high density of visual content to captivate the user, with product shots often featuring gaming hardware like controllers, emphasizing the tactile experience.

## Dos & Donts

### Do

- Prioritize Lumi Green (#107c10) for primary calls to action to maintain brand visibility and urgency.
- Use Electric Lime (#9bf00b) purely for secondary interactive backgrounds, such as 'Game Pass' buttons, ensuring high contrast with Rich Meadow (#054b16) text.
- Maintain a rigid 0px border-radius for all interactive components (buttons, badges) to reinforce the sharp, digital aesthetic.
- Employ Absolute Zero (#000000) for all main text and headings on light backgrounds to maximize readability and impact.
- Leverage the Segoe UI font family for most textual content, varying weights from 400 to 700 to establish hierarchy without introducing new typefaces.
- Utilize a compact spacing density, frequently using 4px to 12px for element gaps to keep information tightly packed and immersive.

### Don't

- Avoid using rounded corners on any primary UI elements; the brand relies on a sharp, zero-radius aesthetic.
- Do not use generic blue as an interactive color; reserve Deep Space Blue (#0066ff) for specific illustration accents only.
- Refrain from using shadow-based elevation; depth is created through color contrast and layered sections, not drop shadows.
- Do not introduce additional font families beyond Segoe UI and SegoeProBlack; the type system is intentionally constrained for a consistent brand voice.
- Avoid using low-contrast text-on-background combinations; always ensure AAA or at least AA accessibility standards are met with high contrast pairs.
- Do not soften or desaturate brand greens; the vibrant, vivid nature of Lumi Green and Electric Lime is key to the energetic gaming feel.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #000000 (Absolute Zero)
- Background: #ffffff (Snowfield White)
- CTA Primary: #107c10 (Lumi Green)
- CTA Secondary: #9bf00b (Electric Lime)
- Badge: #ffd800 (Cyber Yellow)

### Example Component Prompts
1. Create a primary navigation item: text 'Juegos' in Segoe UI weight 400, size 15px, color Absolute Zero (#000000). No background or border. On hover, apply an underline.
2. Design a 'Game Pass' call-to-action button: background Electric Lime (#9bf00b), text 'ÚNETE A GAME PASS' in SegoeProBlack weight 900, size 15px, color Rich Meadow (#054b16). It has 5px vertical padding and 20px horizontal padding. No border or border-radius.
3. Generate a 'New Season' badge: background Cyber Yellow (#ffd800), text 'NUEVA TEMPORADA' in Segoe UI weight 400, size 13px, color Absolute Zero (#000000). Padding is 1px top/bottom and 7px left/right. 0px border-radius.
4. Create a hero section with a dark image background that has a rgba(0,0,0,0.4) overlay. The heading is 'Call of Duty®: Black Ops 7' in Segoe UI weight 600, size 46px, color Snowfield White (#ffffff), letter-spacing -1.0px. Below it is a description in Segoe UI weight 400, size 20px, color Snowfield White (#ffffff). Include the 'Game Pass CTA Button' centered below the text ensemble.

---
_Source: https://styles.refero.design/style/3792d0ca-6c74-4667-a64d-76efe9f87076_
