# Depot — Design Reference

> Deep space console: focused intensity.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://depot.dev](https://depot.dev) |
| Refero page | [https://styles.refero.design/style/f4636c5b-1342-48b2-b9b1-a82e2182440e](https://styles.refero.design/style/f4636c5b-1342-48b2-b9b1-a82e2182440e) |
| Theme | dark |
| Industry | devtools |

## Overview

Depot’s aesthetic is a high-contrast dark mode, evoking a command center's precision. Deep, near-black backgrounds frame content, punctuated by vivid green accents that immediately draw the eye to critical interactive elements. A subtle progression of dark grays creates layered surfaces, adding depth and structure without reliance on heavy shadows. The overall impression is one of efficiency and focused power without distraction, like a purpose-built tool designed for speed.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Carbon Black | `#04040b` | neutral | Primary page background, default text color on light elements. |
| Night Sky | `#121113` | neutral | Card backgrounds, section dividers, slightly elevated surfaces. |
| Ash Gray | `#323035` | neutral | Button backgrounds, form inputs, secondary content containers. |
| Storm Gray | `#3c393f` | neutral | Border colors, subtle dividers, inactive states. |
| Cloud Burst | `#b5b2bc` | neutral | Secondary text, subtle icon fill, placeholder text. |
| Snow Drift | `#eeeef0` | neutral | Primary text on dark backgrounds, active states for text and icons. |
| Terminal Green | `#71d083` | brand | Call-to-action text, primary interactive element highlights, success states. |
| Canopy Green | `#366740` | brand | Call-to-action button backgrounds when paired with Terminal Green text, active indicators. |
| Link Blue | `#70b8ff` | accent | Hyperlinks, secondary interactive text. |
| Twilight Purple | `#473876` | accent | Border accents for specialized cards or highlight elements. |
| Crimson Spark | `#ff9592` | semantic | Alerts or error states, used sparingly for emphasis. |
| Green Burst Gradient | `#46a758` | brand | Decorative hero background accent, draws focus to key sections. |
| Radial Glow Gradient | `#64c878` | brand | Subtle background glow for featured content blocks. |

## Typography

### Red Hat Display Variable

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 20px, 36px, 48px, 60px |
| lineHeight | 1.00, 1.11, 1.38, 1.40, 1.50 |
| letterSpacing | -0.0250em |
| substitute | Segoe UI, sans-serif |
| role | Headlines and prominent display text. The tight letter-spacing gives a modern, compressed feel, making large text feel sharp and precise. |

### Red Hat Text Variable

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 10px, 12px, 14px, 15px, 16px, 18px, 20px |
| lineHeight | 1.00, 1.33, 1.40, 1.43, 1.50, 1.56, 1.63 |
| letterSpacing | 0.0250em |
| substitute | Roboto, Arial, sans-serif |
| role | Body copy, navigation, buttons, and most UI elements. The slightly expanded letter-spacing provides excellent readability on dark backgrounds. |

### Red Hat Mono Variable

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 18px |
| lineHeight | 1.43, 1.56 |
| substitute | SFMono-Regular, monospace |
| role | Code snippets, technical details, and specific data displays. Its monospace nature ensures alignment for structured information. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0.25 |
| body | 14 |  | 1.43 | 0.35 |
| heading | 20 |  | 1.33 | -0.5 |
| heading-lg | 36 |  | 1.11 | -0.9 |
| display | 60 |  | 1 | -1.5 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 6px |
| buttons | 6px |
| default | 6px |
| smallElements | 2px |
| interactiveElements | 10px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Product Tab Selector Cards

### Announcement Banner + Highlight Badge

### Primary Action Button

**Role:** Call to action

Filled button for primary actions. Background `Canopy Green` (#366740), text `Terminal Green` (#71d083), border `Canopy Green` (#366740). Rounded corners at 10px.

### Secondary Action Button

**Role:** Secondary call to action

Ghost button with `Carbon Black` (#04040b) background, `Snow Drift` (#eeeef0) text, and `Storm Gray` (#3c393f) border. Rounded corners at 6px. Padding: 16px vertical, 16px horizontal.

### Tertiary Ghost Button

**Role:** Mildly emphasized action

Transparent background button (`rgba(0, 0, 0, 0)`), `Cloud Burst` (#b5b2bc) text, no visible border (`oklch(0.922 0 0)`). Radius 4px. Padding: 8px vertical, 12px horizontal.

### Navigation Link Button

**Role:** Navigation and internal links

Minimal button for navigation. Background `Ash Gray` (#323035), text `Snow Drift` (#eeeef0), no explicit border. Sharp corners (0px radius). Padding: 20px all sides. Used in header/footer.

### Feature Card

**Role:** Content grouping

Card with `Night Sky` (#121113) background, a subtle inset shadow (`rgba(255, 255, 255, 0.06) 0px 1px 0px 0px inset`), and `Ash Gray` (#323035) border. 6px border radius. Internal padding likely 16px.

### Highlight Badge

**Role:** Small, informative label

Small, informative badge with `Canopy Green` (#366740) background, `Terminal Green` (#71d083) text, and `Storm Gray` (#3c393f) border. Rounded corners at 10px. Padding 12px vertical, 16px horizontal for text `Red Hat Text Variable` 16px.

### Hero Headline

**Role:** Primary page title

Uses `Red Hat Display Variable` at 60px size, weights 600 or 700, with `Snow Drift` (#eeeef0) color and tight letter-spacing -0.0250em.

### Secondary Headline

**Role:** Section titles

Uses `Red Hat Display Variable` at 36px size, weights 400 or 500, with `Snow Drift` (#eeeef0) color and tight letter-spacing -0.0250em.

## Layout

The layout is predominantly full-bleed dark, with content constrained within a centered max-width, though the exact `pageMaxWidth` is not explicitly defined it appears to allow generous negative space. The hero section features a centered headline over a dark background, with clear calls to action below. Sections are visually distinct, often with solid color backgrounds for separation, like the alternating `Night Sky` (#121113) and `Carbon Black` (#04040b) sections. Content is frequently arranged in two-column layouts, with text and descriptions on one side and a visual (code example, product screenshot, or illustration) on the other. A structured card grid appears for feature showcases. The navigation is a sticky top bar, providing persistent access to key links.

## Imagery

This design primarily uses product screenshots and brand-colored illustrations. Product screenshots are typically contained within card-like structures, showing UI elements and code, often with a subtle `Terminal Green` (#71d083) highlight to draw attention to key features. Illustrations are flat, geometric, and follow the brand's green and dark gray palette, used decoratively to break up sections or emphasize concepts, rather than depicting real-world scenarios. Small, mono-color icons, usually `Snow Drift` (#eeeef0) or `Terminal Green` (#71d083), are extensively used to denote features, actions, and navigation elements. The overall density of imagery is balanced, supporting the text-heavy technical content without overwhelming it, acting as visual cues rather than dominant storytelling elements.

## Elevation philosophy

Depot deliberately avoids strong, drop-shadow-based elevation. Instead, depth and layering are achieved through a carefully considered progression of background colors, from `Carbon Black` (#04040b) for the base canvas to `Night Sky` (#121113) for cards and `Ash Gray` (#323035) for interactive elements. This approach maintains a flat, precise, and high-tech feel, with only a very subtle inset white shadow (`rgba(255, 255, 255, 0.06) 0px 1px 0px 0px inset`) on certain cards to denote a slight raised surface, enhancing the 'screen-like' quality.

## Dos & Donts

### Do

- Use `Carbon Black` (#04040b) for primary page backgrounds to maintain a consistent dark theme.
- Apply `Canopy Green` (#366740) for all primary call-to-action button backgrounds, paired with `Terminal Green` (#71d083) text.
- Implement 6px border radius as the default for most interface elements, with 10px used for key interactive elements like primary buttons.
- Ensure `Red Hat Text Variable` with 0.0250em letter-spacing is used for all body text and secondary UI elements for optimal legibility.
- Maintain a clear visual hierarchy using `Red Hat Display Variable` for headlines, particularly with its tight -0.0250em letter-spacing to distinguish it from body copy.
- Utilize `Ash Gray` (#323035) for button backgrounds and secondary content blocks to create subtle elevation on dark surfaces.

### Don't

- Avoid using bright, high-saturation colors for large background areas; colors like `Terminal Green` (#71d083) are strictly for accents and interactive elements.
- Do not deviate from the specified letter-spacing values across `Red Hat Display Variable` and `Red Hat Text Variable` as they are integral to the system's character.
- Avoid excessive use of shadows; the system relies on layer colors (`Night Sky`, `Ash Gray`) and subtle inset shadows for depth.
- Do not introduce new typefaces; `Red Hat Display`, `Red Hat Text`, and `Red Hat Mono` are the only approved families.
- Refrain from using color in body text that does not meet the specified `Snow Drift` (#eeeef0) or `Cloud Burst` (#b5b2bc) for readability against dark backgrounds.
- Do not use generic square or fully rounded (pill) shapes; adhere to the 6px and 10px radii for a consistent, structured appearance.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: `#eeeef0` (Snow Drift)
- Background: `#04040b` (Carbon Black)
- CTA Background: `#366740` (Canopy Green)
- CTA Text: `#71d083` (Terminal Green)
- Border: `#3c393f` (Storm Gray)
- Secondary Text: `#b5b2bc` (Cloud Burst)

### 3-5 Example Component Prompts
1. **Create a Hero Section**: Background `Carbon Black` (#04040b). Headline: 'Build faster. Waste less time.' using `Red Hat Display Variable` at 60px, weight 700, color `Snow Drift` (#eeeef0), letter-spacing -1.5px. Below, a `Primary Action Button` with text 'Get started'.
2. **Generate a Feature Card**: Background `Night Sky` (#121113), with a `Storm Gray` (#3c393f) border and 6px border-radius. Include a subtle inset shadow: `rgba(255, 255, 255, 0.06) 0px 1px 0px 0px inset`. Content text should use `Snow Drift` (#eeeef0) with `Red Hat Text Variable` 16px, letter-spacing 0.4px.
3. **Design a Navigation Bar**: Background same as page, `Carbon Black` (#04040b). Navigation links use `Cloud Burst` (#b5b2bc) text (e.g., 'Products', 'Resources'), `Red Hat Text Variable` 16px. Include two right-aligned buttons: a `Navigation Link Button` for 'Sign In' and a `Primary Action Button` for 'Get started'.
4. **Display a Code Block**: Background `Night Sky` (#121113), text `Cloud Burst` (#b5b2bc), using `Red Hat Mono Variable` 14px. Ensure code line spacing is 1.43, with no letter-spacing. Use 6px border-radius for the block.

---
_Source: https://styles.refero.design/style/f4636c5b-1342-48b2-b9b1-a82e2182440e_
