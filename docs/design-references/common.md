# Common — Design Reference

> Whiteboard with digital neon accents

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.common.xyz](https://www.common.xyz) |
| Refero page | [https://styles.refero.design/style/54bfd692-3299-48ab-8cf5-784e632227b1](https://styles.refero.design/style/54bfd692-3299-48ab-8cf5-784e632227b1) |
| Theme | light |
| Industry | crypto |

## Overview

Common presents a clean, productivity-focused interface built on a nearly achromatic palette.  A playful gradient and two vibrant accent colors punctuate the UI, creating a dynamic contrast against the muted background. Components are lightweight and low-to-no shadow, emphasizing flat surfaces and a sense of speed. Typography is structured and compact, supporting a dense information display without visual clutter.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight | `#000000` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |
| Snowdrift | `#ffffff` | neutral | Page backgrounds, card surfaces, button backgrounds, input fields, providing a clean canvas |
| Silver Pine | `#e0dfe1` | neutral | Subtle borders for input fields and other UI elements, creating soft separation |
| Charcoal Slate | `#282729` | neutral | Headings, prominent text, and darker accents for icons, creating depth against lighter neutrals |
| Frost Gray | `#c1c0c2` | neutral | Hairline card borders, providing a light visual containment without harsh lines |
| Ash Cloud | `#a09da1` | neutral | Muted icon fills and navigation item borders, indicating secondary importance |
| Dark Granite | `#3d3a3e` | neutral | Filled button backgrounds for primary actions, providing a strong interactive target |
| Deep Ink | `#141315` | neutral | Very dark text or borders, used sparingly for emphasis |
| Stone Dust | `#757575` | neutral | Muted icon fills and secondary text |
| Gallery White | `#f0eff0` | neutral | Subtle background for certain body sections, providing a slight elevation from the pure white canvas |
| Sky Blue Spark | `#338fff` | accent | Link text, icon accents, and outlined interactive borders, injecting a crisp, functional color |
| Electric Violet | `#7a33ff` | accent | Link text, icon fill, and outlined interactive borders, providing a secondary vibrant accent |
| Spectrum Burst | `#0079cc` | brand | Used as a vibrant, multi-hued decorative element to highlight key sections or buttons |
| Soft Spectrum Burst | `#ff1f00` | brand | Translucent background overlay, adding a subtle touch of brand color without obscuring content |

## Typography

### NeueHaasUnica

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 14px, 16px, 24px, 32px, 36px |
| lineHeight | 1.00, 1.15, 1.43, 1.50, 1.71 |
| letterSpacing | 0.0100em at 14px, 0.0200em at 16px |
| fontFeatureSettings | "lnum" on, "tnum" on |
| substitute | system-ui, sans-serif |
| role | Primary UI font for body text, form inputs, buttons, navigation, and many interactive elements. Its precise tracking and numerical features support a compact, data-rich display. |

### Silka

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 20px, 24px, 28px |
| lineHeight | 1.17, 1.40, 1.43, 1.50 |
| letterSpacing | normal |
| substitute | system-ui, sans-serif |
| role | Used for larger headings and distinct sections to provide an alternative geometric feel. Its heavier weights offer a clear hierarchy. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.71 | 0.14 |
| body | 16 |  | 1.5 | 0.32 |
| subheading | 20 |  | 1.4 |  |
| heading-sm | 24 |  | 1.43 |  |
| heading | 28 |  | 1.17 |  |
| heading-lg | 32 |  | 1.15 |  |
| display | 36 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 5px |
| inputs | 6px |
| buttons | 6px |
| default | 6px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 8px
- **pageMaxWidth** — 

## Components

### Primary Filled Button

**Role:** Action

Solid Dark Granite (#3d3a3e) background with Midnight (#000000) text (or white for contrast). 6px border-radius, with 4px vertical padding and 16px horizontal padding. Example: 'Sign in' button.

### Ghost Button

**Role:** Action

Transparent background, Midnight (#000000) border, and Midnight (#000000) text. 6px border-radius, with 4px vertical padding and 16px horizontal padding. Example: 'About' button.

### Monochrome Card

**Role:** Container

Snowdrift (#ffffff) background, 5px border-radius, and a subtle drop shadow rgba(40, 39, 41, 0.05) 0px 1px 2px 0px. Internal content padding is 8px. Used for displaying individual items or grouped content.

### Outline Card

**Role:** Container

Snowdrift (#ffffff) background, 4px border-radius, no shadow, with 0px internal padding. Used for high-density content areas where visual layering is less important.

### Transparent Card

**Role:** Container

Transparent background, no border-radius, no shadow. 8px vertical padding, 0px horizontal. Used for sections that seamlessly integrate into the background while maintaining logical grouping.

### Text Input Field

**Role:** Input

Snowdrift (#ffffff) background, Silver Pine (#e0dfe1) border. 6px border-radius, Charcoal Slate (#282729) text, 10px vertical padding and 12px left padding. Placeholder text is lighter.

### Gradient CTA Button

**Role:** Action

Spectrum Burst gradient background (linear-gradient). Snowdrift (#ffffff) text. Used for prominent calls to action like 'Launch Token'.

### Small Interactive Card

**Role:** Navigation/Call-to-action

Snowdrift (#ffffff) background, 6px border-radius. A Charcoal Slate (#282729) border on hover. Padding is 8px. Used for compact navigation items like 'Create Your Community'.

## Layout

The page primarily uses a max-width contained layout, with content centered. The hero section is characterized by a prominent background element, potentially a gradient or a large card. Sections follow a consistent vertical rhythm, often delineated by distinct cards or transparent containers. Content is arranged in stacked text blocks or simple horizontal arrangements, like a text input field followed by action buttons. Navigation includes a sticky top bar and a collapsed left sidebar.

## Imagery

The site uses a 'no imagery, pure UI' approach with a strong emphasis on clean interface elements. Icons, where present, are monolinear and typically filled with neutral colors like Stone Dust (#757575) or Charcoal Slate (#282729), or occasionally accented with Sky Blue Spark (#338fff) or Electric Violet (#7a33ff). The visual density is higher on UI elements than on decorative graphics. Graphics are minimalist and functional, such as the two-dot pink/blue motif, conveying information or status rather than atmosphere.

## Dos & Donts

### Do

- Use Midnight (#000000) for all primary text and strong borders to ensure high contrast and readability.
- Apply Snowdrift (#ffffff) as the default background for pages and main content cards.
- Utilize Dark Granite (#3d3a3e) for filled primary action buttons with 6px border-radius.
- Employ Sky Blue Spark (#338fff) or Electric Violet (#7a33ff) for link text and small interactive accents.
- Maintain a compact visual density with default element spacing of 8px and card padding of 8px.
- Apply a consistent 6px border-radius for buttons, inputs, and most interactive cards.
- Use the Spectrum Burst gradient for high-priority, visually distinct calls-to-action or hero components.

### Don't

- Do not use highly saturated colors for large background areas; limit them to accents and interactive states.
- Avoid deep, heavy shadows; use the rgba(40, 39, 41, 0.05) 0px 1px 2px 0px shadow sparingly for subtle elevation on cards.
- Do not vary border-radius excessively; stick to 6px for most common interactive elements and 5px for cards.
- Avoid using multiple different shades of gray for primary text; default to Midnight (#000000) or Charcoal Slate (#282729).
- Do not introduce new font families beyond NeueHaasUnica and Silka.
- Refrain from using complex, decorative gradients outside of the defined Spectrum Burst style.
- Do not create dense layouts without clear visual breakpoints; prioritize consistent spacing and separation.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #000000
background: #ffffff
border: #e0dfe1
accent: #338fff
primary action: #3d3a3e (filled action)

Example Component Prompts:
1. Create a neutral card: Snowdrift (#ffffff) background, 5px border-radius, rgba(40, 39, 41, 0.05) 0px 1px 2px 0px shadow, 8px padding. Headline text 'Trending' in Charcoal Slate (#282729) Silka weight 700 at 24px, no letter-spacing. Link 'Tokens →' in Sky Blue Spark (#338fff) NeueHaasUnica weight 400 at 16px, 0.0200em letter-spacing.
2. Create a primary action button: Dark Granite (#3d3a3e) background, Midnight (#000000) text NeueHaasUnica weight 400 at 16px, 6px border-radius, 4px vertical padding, 16px horizontal padding. Example text: 'Sign in'.
3. Create an input field: Snowdrift (#ffffff) background, Silver Pine (#e0dfe1) border, 6px border-radius, 10px vertical padding, 12px left padding. Placeholder text in Stone Dust (#757575) NeueHaasUnica weight 400 at 16px. Example placeholder: 'Type your idea...'.
4. Create a Primary Action Button: #3d3a3e background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

---
_Source: https://styles.refero.design/style/54bfd692-3299-48ab-8cf5-784e632227b1_
