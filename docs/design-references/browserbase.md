# Browserbase — Design Reference

> Pixelated digital landscape

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.browserbase.com](https://www.browserbase.com) |
| Refero page | [https://styles.refero.design/style/34d438ad-0647-471e-9a6f-7c1fa29d5df6](https://styles.refero.design/style/34d438ad-0647-471e-9a6f-7c1fa29d5df6) |
| Theme | light |
| Industry | ai |

## Overview

Browserbase evokes a digital landscape with a stark white canvas and minimalist, pixel-inspired graphics. Typography uses custom sans-serif and monospaced fonts, emphasizing functional clarity and a slightly technical aesthetic. Orange and muted near-grays introduce moments of warm contrast, while primary UI elements are grounded in achromatic black and white, presenting a clean, high-contrast, and buttoned-down developer tool experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated surfaces, card surfaces, text elements where inverted against dark backgrounds |
| Ink Black | `#000000` | neutral | Primary text, button backgrounds, borders, strokes, icon fills — establishes strong contrast |
| Cloud Gray | `#f8fafc` | neutral | Subtle background for UI elements, very light surface differentiation |
| Powder Blue | `#e2e9f3` | neutral | Card backgrounds, section separators, background for selected UI states |
| Pale Sage | `#fffde6` | neutral | Alternative soft background for cards or sections, creating a subtle warmth |
| Charcoal Text | `#686562` | neutral | Secondary body text, muted details, hints of softer contrast |
| Impact Orange | `#ff4500` | brand | Key branding accent, highlighted text, decorative elements, focal points in hero sections — signals importance and brand identity |
| Sky Tint | `#c4edff` | accent | Badge backgrounds, subtle accents, background for informational elements |
| Pixel Mountain | `#c5d3e8` | accent | Decorative background fills, button backgrounds for secondary actions — adds muted, textured feel |

## Typography

### plain

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 16px, 24px |
| lineHeight | 1.00, 1.10, 1.16, 1.30, 1.41, 1.50 |
| letterSpacing | 0.007em at 16px, 0.010em at 24px |
| substitute | Inter |
| role | Primary text for body copy, links, navigation items, and button labels. Its clean and neutral sans-serif form ensures readability for functional content. |

### gtPlanar

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 16px, 24px, 34px, 45px, 189px |
| lineHeight | 1.00, 1.10, 1.15, 1.50 |
| letterSpacing | -0.050em at 189px, -0.020em at 45px |
| fontFeatureSettings | "ss05" |
| substitute | DM Sans |
| role | Dominant font for headings and prominent display text. Its distinct character and tighter letter spacing at larger sizes contribute to a modern, confident tone, subtly hinting at its custom nature. |

### gtStandardMono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.00, 1.20 |
| letterSpacing | 0.030em at 14px, 0.060em for other uses |
| substitute | IBM Plex Mono |
| role | Used for code snippets, badges, and technical labels. The monospaced nature reinforces the developer-centric focus of the product. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 14 |  | 1.2 | 0.42 |
| body | 16 |  | 1.5 | 0.112 |
| subheading | 24 |  | 1.16 | 0.24 |
| heading | 34 |  | 1.15 |  |
| heading-lg | 45 |  | 1.1 | -0.9 |
| display | 189 |  | 1 | -9.45 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| lists | 999px |
| buttons | 50px |
| roundedElements | 4px |

- **elementGap** — 8px
- **sectionGap** — 75px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Primary Action Button

**Role:** Call to action button for essential actions

Ink Black background (#000000), Canvas White text (#ffffff), 50px border-radius, 11px vertical padding, 9.936px horizontal padding. Prominent and easily identifiable.

### Ghost Navigation Button

**Role:** Navigation items and secondary actions within headers/menus

Transparent background, Ink Black text (#000000), no border-radius or border, 10px vertical and 20.8px horizontal padding. Subtle and integrates into flow.

### Accent Pill Button

**Role:** Secondary action button with specific highlight

Pixel Mountain background (#e2e9f3), Ink Black text (#000000), 50px border-radius, 11.2px vertical padding, 16-20.8px horizontal padding. Draws attention without being primary.

### Compact Info Button

**Role:** Small, information-dense button often used in feature lists

Pale Sage background (#fffde6), Ink Black text (#000000), no border-radius. 7.2px vertical and 12-47px horizontal padding. Visually distinct but not as attention-grabbing as primary.

### Transparent Content Card

**Role:** Container for content sections without visual separation

Transparent background, no box shadow or border, 0px border-radius, generous 75px vertical padding and 16px horizontal padding. Used for large, flowing sections.

### Pale Gray Feature Card

**Role:** Container for feature blocks or grouped content

Powder Blue background (#e2e9f3), no box shadow or border, 0px border-radius, 16px padding on top. Used to subtly differentiate sections of content.

### White Section Card

**Role:** Standard content card, visually distinct from background

Canvas White background (#ffffff), no box shadow or border, 0px border-radius, 75px vertical and 16px horizontal padding. Provides a clean content surface.

### Text Badge

**Role:** Minimalist textual indicator or tag

Transparent background, Ink Black text (#000000), no border-radius or padding. Used for simple categorization.

### Sky Tint Badge

**Role:** Informational or emphasized tag

Sky Tint background (#c4edff), Ink Black text (#000000), no border-radius, `gtStandardMono` font, minimal horizontal padding (3.5px). Used for accentuating specific keywords or statuses.

## Layout

The page follows a max-width contained model, centrally aligning content within a clear visual boundary. The hero section is full-bleed, featuring a striking pixelated mountain graphic with a centered headline and action buttons. Subsequent sections alternate between full-width transparent cards and slightly tinted, contained feature cards, maintaining a consistent vertical rhythm with generous 75px section gaps. Content is arranged in flexible patterns, sometimes centered stacks, sometimes text-left/image-right or multi-column card grids. The navigation is a classic top bar, sticky on scroll, with log in/sign up links and a distinct primary action button. The overall density is balanced, providing breathing room between sections while presenting information in clear, organized blocks.

## Imagery

The imagery primarily consists of simplified, abstract graphical representations and pixel art, rather than photography or complex illustrations. These graphics often feature clean, geometric shapes and an outlined style. Color is used sparingly within these graphics, often leveraging the brand's accent colors or muted tones. Imagery serves an explanatory and atmospheric role, showcasing product concepts or creating an abstract digital environment. The density is moderate, allowing text to dominate while visuals provide conceptual anchors.

## Dos & Donts

### Do

- Prioritize Ink Black (#000000) for primary text and interactive elements to maintain high contrast and clarity.
- Use Impact Orange (#ff4500) sparingly for emphasis, branding accents, or key headlines to draw attention.
- Apply 50px border-radius to all primary and accent buttons to create a consistent pill-shaped aesthetic.
- Segment content using Powder Blue (#e2e9f3) and Pale Sage (#fffde6) as background colors for cards and feature blocks.
- Maintain generous vertical spacing for sections (75px) with narrower horizontal padding for text containers (16px) to ensure readability.
- Employ `gtPlanar` for all headings and `plain` for body text, reserving `gtStandardMono` for technical labels and badges.
- Ensure interactive elements such as buttons and navigation items have clear Ink Black (#000000) text against light backgrounds.

### Don't

- Do not introduce new primary action colors; stick to Ink Black for filled buttons or transparent for ghost buttons.
- Avoid using multiple high-saturation colors at once, as the system relies on a mostly achromatic palette with specific accents.
- Do not deviate from the specified font families for their respective roles; `gtPlanar`, `plain`, and `gtStandardMono` define the typographic voice.
- Do not add heavy box shadows or decorative gradients; the design emphasizes flat surfaces and minimal elevation.
- Avoid using a single large border-radius for all elements; adapt radius based on component (50px for buttons, 4px for other UI elements).
- Do not use generic system fonts; the custom 'plain', 'gtPlanar', and 'gtStandardMono' are crucial for brand identity.
- Do not create dense layouts; maintain compact element spacing (8px) while allowing ample section separation (75px).

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #000000
- background: #ffffff
- border: #000000
- accent: #ff4500
- primary action: #000000 (filled action)

### 3-5 Example Component Prompts
- Create a hero callout: Background Powder Blue (#e2e9f3). Headline 'Give your agents access to the whole web.' in gtPlanar, 45px weight 500, Ink Black (#000000), letter-spacing -0.9px. Primary Action Button 'Get API key' with Ink Black (#000000) background, Canvas White (#ffffff) text, 50px radius, 11px vertical padding.
- Design a feature card: Powder Blue (#e2e9f3) background, 0px radius, 16px top padding. Body text 'Build agents that never sleep' in plain, 16px weight 400, Ink Black (#000000). At the bottom, a Primary Action Button 'Get started' with Ink Black (#000000) background, Canvas White (#ffffff) text, 50px radius.
- Create a Primary Action Button: #000000 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

---
_Source: https://styles.refero.design/style/34d438ad-0647-471e-9a6f-7c1fa29d5df6_
