# Copy — Design Reference

> Strategic blueprint on polished steel. Organized hierarchy and vibrant violet accents against a muted canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://copy.ai](https://copy.ai) |
| Refero page | [https://styles.refero.design/style/1ae4bcfe-c613-42fc-aab7-f9583381e7cc](https://styles.refero.design/style/1ae4bcfe-c613-42fc-aab7-f9583381e7cc) |
| Theme | light |
| Industry | saas |

## Overview

This design system projects a high-tech, enterprise feel through its stark contrasts and precise typography. Dominant dark and vivid violet accents create a sense of digital sophistication, while a meticulous hierarchy of grays and precise spacing ensure clarity. The interplay of strong, weighted headlines against more muted body text establishes authority, characteristic of B2B SaaS platforms.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#171717` | neutral | Primary text, deep backgrounds, icon fills. |
| Cloud Burst | `#f6fafb` | neutral | Primary page and card backgrounds, input fields. |
| Slate Echo | `#e4edf1` | neutral | Subtle borders, form field outlines, divider lines. |
| Ash Veil | `#e2e8eb` | neutral | Button borders, subtle highlights. |
| Graphite Tone | `#5d5d5d` | neutral | Secondary text, descriptive labels. |
| Violet Impulse | `#693edf` | brand | Primary Call-to-Action buttons, active navigation, key interactive elements. Its vividness provides a clear focal point against the neutral palette. |
| Deep Space Violet | `#3b0d96` | brand | Darker accent for buttons and specific background sections, providing depth within the brand's violet spectrum. |
| Dawn Violet | `#c1b9f4` | brand | Lighter accent used for subtle indicators or progress elements, softening the brand's primary color when needed. |

## Typography

### ABC Normal

| Key | Value |
| --- | --- |
| weight | 500, 600 |
| sizes | 24px, 26px, 28px, 32px, 48px, 56px, 88px |
| lineHeight | 1.00, 1.18, 1.31, 1.40, 1.42, 1.50 |
| letterSpacing | -0.0200em at display sizes (88px, 56px), 0.0050em at 24px and 26px font sizes |
| substitute | Montserrat |
| role | Display and marketing headlines. The custom font elevates the brand's visual identity, especially with its precise letterSpacing and tight line heights, giving titles a sophisticated, controlled feel. |

### ABC Normal Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 26px |
| lineHeight | 1.31 |
| letterSpacing | 0.0100em |
| substitute | Montserrat |
| role | Descriptive body text for prominent statements or feature descriptions, maintaining the brand's unique typographic voice at a slightly more readable weight. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 12px, 14px, 16px, 22px, 24px |
| lineHeight | 1.17, 1.33, 1.43, 1.45, 1.50, 1.57, 1.69, 2.00 |
| letterSpacing | -0.0200em at 24px, -0.0050em at 22px |
| substitute | Inter Variable |
| role | All functional text, including navigation, body copy, button labels, and captions. Its broad weight and size range provide versatility for information hierarchy, while slight negative letter spacing on larger sizes maintains a polished, modern feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 2 | 0 |
| body-sm | 14 |  | 1.5 | 0 |
| body | 16 |  | 1.43 | 0 |
| subheading | 22 |  | 1.57 | -0.22 |
| heading | 24 |  | 1.45 | -0.48 |
| heading-lg | 26 |  | 1.31 | 0.26 |
| display | 88 |  | 1 | -1.76 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 4px |
| default | 4px |

- **elementGap** — 16px
- **sectionGap** — 72px
- **cardPadding** — 30px
- **pageMaxWidth** — 

## Components

### GTM AI Playbook Feature Card

### GTM AI Platform Architecture Block

### Hero Email CTA with Process Steps

### Login Button

**Role:** Secondary call to action

Outline button with `Midnight Ink` (#171717) text and a 2px stroke border in `Ash Veil` (#e2e8eb). Has a 4px border radius. Padding is 0px top/bottom, 16px left/right.

### Input Field

**Role:** User data entry

Background `Cloud Burst` (#f6fafb), `Midnight Ink` (#171717) text. Border `Slate Echo` (#e4edf1) 0px radius. Padding 0px top/bottom, 18px left, 8px right. This input visually blends with the background suggesting a clear, unobstructed path for data.

### Process Step

**Role:** Sequential information display

Transparent background, no border radius, no shadow. Padding 0px top/bottom, 32px right, 0px left for the content block. Highlighted by `Violet Impulse` (#693edf) and its shades through the background shape, serving as active state indicators.

### Sub-Navigation Button

**Role:** Internal navigation

`Midnight Ink` (#171717) text on a transparent background with no border radius. Explicitly styled with padding 8px top/bottom, 24px left/right. Used for secondary navigation elements within sections.

### Tag Badge

**Role:** Categorization label

Transparent background, `Midnight Ink` (#171717) text. No border radius, no padding. Used for simple categorization and filtering.

### Primary Navigation Link

**Role:** Main site navigation

`Midnight Ink` (#171717) text on a transparent background, no border radius. These links have a hover state that brings out the `Violet Impulse` color, often as an underline.

## Layout

The site employs a max-width contained layout, typically centered, with a default `sectionGap` of 72px creating ample vertical breathing room between content blocks. The hero section is full-bleed with a dark background and a central, large headline, immediately grounding the user. Most content sections alternate between text-left/image-right and image-left/text-right patterns, often within a 2-column grid. Feature lists are presented in 3-column card grids. The navigation is a sticky top bar, providing persistent access. The overall density is spacious, prioritizing readability and clear information hierarchy over packed content.

## Imagery

The site uses a combination of abstract, geometric illustrations and product screenshots. Illustrations are brand-colored (shades of violet) and appear to abstract complex processes into clear, interconnected visual pathways (e.g., the branching flow on the hero section). Product screenshots, when present implicitly via card examples, are tightly framed and seem to showcase UI elements rather than full contexts. The overall approach is to use visuals for conceptual explanation or function demonstration, not for emotional connection or lifestyle imagery. Icons are filled, mono-color (Midnight Ink or Violet Impulse) and appear simple and illustrative rather than highly detailed, reinforcing clarity and directness. Imagery density is low, making text and UI elements dominant.

## Dos & Donts

### Do

- Use `Violet Impulse` (#693edf) exclusively for primary CTAs and active states.
- Apply `Midnight Ink` (#171717) for all primary text, ensuring maximum contrast against light backgrounds.
- Maintain 4px `radius` for all general UI elements like cards and secondary buttons.
- Ensure input fields use `Cloud Burst` (#f6fafb) background and `Slate Echo` (#e4edf1) borders for consistent form styling.
- Prioritize `ABC Normal` font for all display headings to leverage its distinctive letter spacing and authoritative presence.
- Implement -0.0200em letter spacing for `ABC Normal` headlines at 48px and larger sizes.

### Don't

- Do not use shadows for elevation; emphasize depth through varied background colors and precise spacing.
- Avoid using `Violet Impulse` (#693edf) for body text or non-interactive elements, to preserve its impact as an accent.
- Do not introduce additional border radii; consistently use 4px or 0px.
- Refrain from using `Inter` for major headlines. Its role is for functional and extended text.
- Do not deviate from the established spacing unit; use multiples of 4px for all padding and margins.
- Avoid introducing additional visual accents or graphical elements that compete with the brand's violet spectrum.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #171717
- Page Background: #f6fafb
- CTA Button: #693edf
- Border/Divider: #e4edf1
- Accent: #3b0d96

### 3-5 Example Component Prompts
1. Create a primary call to action button: `Violet Impulse` #693edf background, `White` #ffffff text, no border radius (0px), padding 18px vertical, 24px horizontal. Text is Inter Bold 700 at 16px.
2. Design a feature card: `Cloud Burst` #f6fafb background, 4px border radius. Padding 30px on all sides. Headline uses ABC Normal 500 at 28px, `Midnight Ink` #171717. Body text uses Inter 400 at 16px, `Midnight Ink` #171717.
3. Implement an input field: `Cloud Burst` #f6fafb background, `Slate Echo` #e4edf1 border, no border radius. Text input uses Inter 400 at 16px, `Midnight Ink` #171717. Padding 0px vertical, 18px left, 8px right.
4. Generate a section subheading: ABC Normal 600 at 56px, `Midnight Ink` #171717, letter-spacing -0.0200em.

---
_Source: https://styles.refero.design/style/1ae4bcfe-c613-42fc-aab7-f9583381e7cc_
