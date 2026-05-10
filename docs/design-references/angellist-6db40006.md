# Angellist — Design Reference

> Deep Space Luminescence – A dark, expansive canvas illuminated by subtle glows and precise typographic detail.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.angellist.com/2024](https://www.angellist.com/2024) |
| Refero page | [https://styles.refero.design/style/6db40006-05ac-4770-b63e-dfd9ebfd534c](https://styles.refero.design/style/6db40006-05ac-4770-b63e-dfd9ebfd534c) |
| Theme | dark |
| Industry | fintech |

## Overview

AngelList employs a sophisticated, dark-mode-first aesthetic that juxtaposes a muted, deep teal canvas with soft, glowing text. The system leverages custom typography with subtle ligatures and tabular numbers, creating an elevated informational tone. Accents are primarily provided by a luminous violet and selective gradients, used sparingly to highlight key content or create atmospheric transitions. Components favor soft, rounded forms and minimal borders, maintaining a sense of approachability within the otherwise serious palette.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Teal | `#001d21` | neutral | Primary text, deep surface backgrounds, navigation elements, text button borders. This deep, almost black teal establishes the dark theme |
| Ghost Marble | `#f9f9f7` | neutral | Light surface backgrounds, default text in light sections, button text in dark sections. Provides contrast against darker elements |
| Stone Slate | `#ccd5d6` | neutral | Subtle borders, dividers, ghost button borders, background of light sections. Provides minimal visual separation without harsh lines |
| Halo Violet | `#bdbbff` | accent | Accent text, highlighted body text, decorative icon strokes. This moderate violet provides a distinct, luminous brand accent |
| Inky Gray | `#002b31` | neutral | Card backgrounds within the dark theme. Offers a slightly lighter shade than Midnight Teal for subtle layering |
| Success Mint | `#cdeed3` | accent | Green wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Info Sky | `#ceddec` | accent | Muted UI surface for disabled controls, low-emphasis panels, and placeholder blocks. Use as a supporting accent, not as a status color |
| Warning Dusk | `#dbb3c8` | accent | Red wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Danger Peach | `#fed6cf` | accent | Gray wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Midnight Glow Gradient | `#898ee5` | accent | Decorative background gradients, often used subtly behind text or as a section divider. It evokes a cosmic, deep space feel |

## Typography

### angellist

| Key | Value |
| --- | --- |
| weight | 400, 500, 550 |
| sizes | 13px, 14px, 16px, 18px, 20px, 144px |
| lineHeight | 0.80, 1.00, 1.40, 1.50, 1.60 |
| letterSpacing | -0.050em at 144px, -0.004em at 18px |
| fontFeatureSettings | 'lnum', 'ss01', 'ss02', 'ss03', 'tnum' |
| role | Body text, navigation links, and smaller UI elements. The custom font with its character set and tabular figures provides a precise, legible experience even at small sizes. |

### angellistDisplay

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 28px, 38px, 60px, 80px, 144px, 260px, 288px |
| lineHeight | 0.75, 0.80, 1.00, 1.05, 1.10, 1.15, 1.25, 1.30 |
| letterSpacing | -0.025em at 288px, -0.015em at 60px |
| fontFeatureSettings | 'lnum', 'ss01', 'ss02', 'ss03', 'tnum' |
| role | Large display headlines. Its substantial sizes and precise letter-spacing ensure impactful yet controlled visual presence, making information feel important without shouting. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.6 |  |
| body-lg | 18 |  | 1.5 | -0.004 |
| subheading | 28 |  | 1.25 |  |
| heading | 60 |  | 1.05 | -0.015 |
| display-sm | 144 |  | 0.8 | -0.05 |
| display | 288 |  | 0.75 | -0.025 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 9999px |
| buttons | 9999px |
| default | 2px |

- **elementGap** — 8px
- **sectionGap** — 80px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Teal Canvas | `#001d21` | 0 | Primary page background for dark sections. Establishes the core visual theme. |
| Inky Gray Card | `#002b31` | 1 | Background for cards and elevated content blocks within the dark theme. |
| Ghost Marble Panel | `#f9f9f7` | 2 | Background for light sections, offering a sharp contrast to the darker palette for emphasis or content shifts. |

## Components

### Ghost Navigation Button

**Role:** Navigation links or secondary actions

Transparent background, 'Midnight Teal' text, no padding on hover. Text uses 'angellist' font at default link sizes. Borders appear only when in a ghost state, using 'Stone Slate' for subtle emphasis.

### Inverted Navigation Button

**Role:** Primary action in dark navigation header

Filled with 'Midnight Teal', 'Ghost Marble' text. This is a filled button usually seen in the navigation for calls to action. Has a 9999px border-radius and no internal padding as it acts as a container for text.

### Light Secondary Button

**Role:** Secondary action in light sections

Background 'Ghost Marble', text 'Midnight Teal', 'Stone Slate' border. This button uses 4px 10px padding and a 9999px radius, suitable for small, contextual actions.

### Rounded Information Card

**Role:** Content container for features or articles

Background 'Midnight Teal', large 9999px border-radius, 24px padding. Used for grouping related content in a soft, approachable container.

### Inline Tag

**Role:** Categorization or status indicator

Backgrounds like 'Success Mint', 'Info Sky', 'Warning Dusk', 'Danger Peach'. These tags have a 9999px radius and 8px 24px padding, used for inline context.

### Header Divider

**Role:** Visual separator in the main navigation or content sections

Simple 1px solid line using 'Stone Slate' or 'Ghost Marble' for subtle visual breaks.

## Layout

The page primarily employs a max-width contained layout, though specific hero sections and gradient backgrounds can appear full-bleed. The hero pattern often features a large, dark background with centered, large-scale headlines using 'angellistDisplay' font, and a single tagline. Section rhythm is marked by consistent vertical spacing of 80px and occasional subtle gradient dividers. Content is arranged in alternating two-column text and image blocks or centered stacks for feature descriptions. A limited use of 3-column card grids is present for showcasing features. The overall density is spacious, allowing content to breathe. Navigation is a sticky top bar, minimalist with ghost links, and a distinctive filled 'Join Us' button.

## Imagery

This design system uses a combination of abstract gradients and tightly cropped product screenshots. Photography is absent. Illustrations are stylized, flat, and often contain subtle gradients, serving a decorative atmosphere rather than explanatory content. Icons are minimal, outlined, and monochromatic, typically in 'Halo Violet' or white. Imagery is used sparingly, primarily in a contained rather than full-bleed manner, keeping the text-dominant layout clean and focused on information.

## Dos & Donts

### Do

- Prioritize 'Midnight Teal' (#001d21) for primary text and critical backgrounds to maintain the dark-first aesthetic.
- Utilize 'angellistDisplay' (weight 400) for all headings, leveraging its full size range and precise letter-spacing to create distinct visual hierarchy.
- Apply a 9999px border-radius to all buttons, cards, and interactive chips unless otherwise specified, maintaining a soft and approachable component feel.
- Use 'Halo Violet' (#bdbbff) exclusively as an accent for links, highlighted text, and small decorative icons to ensure its distinct visual impact.
- Maintain generous vertical spacing between sections, adhering to the 80px 'sectionGap' to provide breathing room on the page.
- Implement 'lnum', 'ss01', 'ss02', 'ss03', 'tnum' font features for both 'angellist' and 'angellistDisplay' to preserve the unique typographic character.
- Ensure all interactive elements have a clear :hover state transition for `transform`, `padding`, `color`, `background-color`, and `border-color` over 0.3s ease.

### Don't

- Avoid using saturated background colors for large sections; primary canvases should remain in the 'Midnight Teal' or 'Ghost Marble' neutral tones.
- Do not deviate from the specified custom `angellist` and `angellistDisplay` fonts or their defined letter-spacing and line heights; system fonts would compromise brand identity.
- Refrain from using hard, sharp angles; all significant UI elements should inherit the predominant 9999px radius or the default 2px minimal radius.
- Do not introduce additional accent colors beyond 'Halo Violet' (#bdbbff) or the defined semantic colors, as this will dilute the brand's controlled palette.
- Avoid heavy drop shadows or strong elevation; prefer subtle border treatments or slight background shifts for layering elements.
- Do not use generic typography (e.g., body, heading) without explicitly mapping to the `angellist` or `angellistDisplay` font definitions and their specific sizes/weights.
- Never use automatic or default browser link styling; always explicitly style links with 'Halo Violet' (#bdbbff) and the 'angellist' font.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #f9f9f7
background: #001d21
border: #ccd5d6
accent: #bdbbff
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Create a feature card: background #002b31, 'angellistDisplay' heading text #f9f9f7 size 28px, 'angellist' body text #ccd5d6 size 14px, 9999px radius, 24px padding.
3. Create a ghost navigation link: text #f9f9f7 (on dark background) or #001d21 (on light background), 'angellist' font weight 400 size 14px, no background, 9999px radius, no padding.
4. Create an inline status tag: background 'Success Mint' (#cdeed3), text #001d21, 9999px radius, 8px 24px padding, 'angellist' font weight 400 size 13px.

---
_Source: https://styles.refero.design/style/6db40006-05ac-4770-b63e-dfd9ebfd534c_
