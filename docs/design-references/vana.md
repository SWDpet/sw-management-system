# Vana — Design Reference

> Midnight Terminal Interface

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.vana.com](https://www.vana.com) |
| Refero page | [https://styles.refero.design/style/2a3cdb2e-effe-406b-932e-37e4dc88ab1d](https://styles.refero.design/style/2a3cdb2e-effe-406b-932e-37e4dc88ab1d) |
| Theme | dark |
| Industry | ai |

## Overview

Vana adopts a dark interface language, reminiscent of a command-line terminal, where deep charcoal surfaces provide the stage for data-centric content. Typography is precise and slightly expanded, reinforcing a technical, controlled aesthetic. Interactivity is highlighted by a vivid violet and subtle outlined elements, injecting a digital pulse into the otherwise muted palette. The system balances a sense of gravity and restraint with clear, functional accents that guide user interaction.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space | `#161616` | neutral | Primary page background, elevated surfaces, default button background in dark mode |
| Void Black | `#000000` | neutral | Illustrative fills, deep accents within graphic elements |
| Cloud White | `#ffffff` | neutral | Primary text, navigation links, outlined button borders, icon outlines |
| Slate Gray | `#3b3b3b` | neutral | Card backgrounds, secondary button backgrounds |
| Ash Gray | `#eaeaea` | neutral | Muted body text, decorative fills |
| Subtle Ash | `#b8ad97` | neutral | Less prominent decorative strokes and fills |
| Ultraviolet Blue | `#0000ff` | brand | Primary action background, active states, key data highlights. Its intensity provides high contrast against dark backgrounds |
| Neon Violet | `#4141fc` | accent | Interactive element borders (e.g., card outlines on hover), button background on hover, accent for focus states |
| Lavender Glow | `#8b8bfe` | accent | Subtle border accents for interactive cards or components, highlighting without dominance |
| Status Mint | `#7fd579` | accent | Green accent for outlined action borders, linked labels, and lightweight interactive emphasis. Use as a supporting accent, not as a status color |
| Status Orchid | `#d896ff` | accent | Pink accent for outlined action borders, linked labels, and lightweight interactive emphasis. Use as a supporting accent, not as a status color |
| Accent Green | `#00c600` | accent | Isolated decorative fill for specific graphics or icons |

## Typography

### Cofo Sans

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 13px, 14px, 16px, 24px, 64px |
| lineHeight | 1.00, 1.10, 1.15, 1.25 |
| letterSpacing | -0.0200em at larger sizes (e.g. 64px), -0.0150em for others, normal at 16px |
| substitute | system-ui, sans-serif |
| role | Primary typeface for headings, body text, and interactive elements. Its clean, slightly technical geometry supports the data-oriented theme, with a subtle letter-spacing adjustment to maintain legibility in larger sizes. |

### Cofo Sans Pixel

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 10px, 13px, 14px |
| lineHeight | 1.00, 1.10, 1.25 |
| letterSpacing | -0.0200em at 13px, -0.0020em (nearly normal) at 10px |
| substitute | monospace, sans-serif |
| role | Used for fine details, helper text, and monospace-style elements, reinforcing the digital aesthetic with its pixelated character at smaller sizes. Its constrained letter spacing provides density to technical information. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.1 | -0.002 |
| heading | 24 |  | 1.15 | -0.015 |
| display | 64 |  | 1 | -0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 1440px |
| cards | 2px |
| buttons | 2px |

- **elementGap** — 16px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 1440px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Space Canvas | `#161616` | 1 | Primary page background and base surface for most sections. |
| Slate Gray Card | `#3b3b3b` | 2 | Background for feature cards, providing a distinct, slightly elevated surface. |
| Ultraviolet Interactive | `#0000ff` | 3 | Background for primary action buttons, signifying the highest level of interaction. |

## Components

### Primary Filled Button

**Role:** Call-to-action button, conveying primary interaction.

Background: Ultraviolet Blue (#0000ff), Text: Cloud White (#ffffff), Border: 2px radius, 14px vertical padding, 16px horizontal padding. Font: Cofo Sans 400.

### Secondary Outlined Button

**Role:** Secondary action button, providing a less prominent alternative to filled buttons.

Background: Deep Space (#161616), Text: Cloud White (#ffffff), Border: 1px solid Lavender Glow (#8b8bfe), Border Radius: 2px, 14px vertical padding, 16px horizontal padding. Font: Cofo Sans 400.

### Ghost Circular Button

**Role:** Decorative or tertiary action, often embedded in navigation or for subtle interaction.

Background: transparent, Text: Cloud White (#ffffff), Border: 1px solid rgba(255, 255, 255, 0.1), BorderRadius: 1440px (pill shape), 11px padding. Font: Cofo Sans 400.

### Navigation Link

**Role:** Top-level navigation items.

Background: transparent, Text: Cloud White (#ffffff), no distinct border, 0px padding. Font: Cofo Sans 400, 14px.

### Feature Card

**Role:** Container for distinct conceptual features or points of interest.

Background: Slate Gray (#3b3b3b), Border: varies (can be Neon Violet, Status Mint, Status Orchid), Radius: 2px, 20px padding around content. Text: Cloud White (#ffffff), Muted Text: Ash Gray (#eaeaea).

### Connect Feature Card

**Role:** Specific feature card for 'Connect' action.

Background: Slate Gray (#3b3b3b), Border: 1px solid Neon Violet (#4141fc), Radius: 2px, 20px padding. Text: Cloud White, Muted Text: Ash Gray.

### Move Feature Card

**Role:** Specific feature card for 'Move' action.

Background: Slate Gray (#3b3b3b), Border: 1px solid Status Mint (#7fd579), Radius: 2px, 20px padding. Text: Cloud White, Muted Text: Ash Gray.

### Pill Button

**Role:** Informational or filtering tags, or subtle secondary actions.

Background: transparent (often), Text: Cloud White (#ffffff), Border: 1px solid rgba(255, 255, 255, 0.1), Radius: 1440px (full pill).

## Layout

The page adheres to a max-width of 1440px, centered on the screen. The hero section is full-bleed, featuring a darker background #161616 with a centered headline and subtext, and prominent call-to-action buttons. Subsequent sections alternate between Deep Space (#161616) and a slightly lighter dark background, creating a subtle vertical rhythm. Content is arranged in flexible grid systems, featuring four-column card grids for features and occasionally a split text-left/visual-right pattern. Overall density is comfortable, with ample breathing room between major sections. Navigation is a minimalist top bar, sticky at the top, with subtle text links.

## Imagery

The site primarily uses abstract, data-centric vector graphics and icons. Photography is absent. Illustrations are geometric and often depict networks or data flows, with a distinct pixelated character often rendered in a muted blue or white against dark backgrounds. Iconography is clean, outlined, with a moderate stroke weight, and monochromatic (white or light gray). Imagery serves to explain complex technical concepts and provide atmospheric context, often acting as a background pattern or illustrative accent rather than a primary content focus. The density is moderate, with illustrations occupying significant visual space in some sections to establish mood.

## Dos & Donts

### Do

- Use Deep Space (#161616) for primary backgrounds and Cloud White (#ffffff) for primary text to maintain high contrast in the dark theme.
- Apply Ultraviolet Blue (#0000ff) exclusively for primary action buttons and meaningful interactive highlights.
- For cards, utilize Slate Gray (#3b3b3b) as the background with a 2px border radius, differentiating from the main canvas.
- Emphasize content hierarchy by using Cofo Sans 64px for display, 24px for headings, and 14px for body text, all with precise letter spacing adjustments.
- Maintain a comfortable density with 16px element gaps and 20px padding within feature cards.
- Ensure all interactive elements have a subtle 2px border radius, with primary buttons having a more pronounced solid fill, and secondary buttons an outlined style.
- Reserve Neon Violet (#4141fc), Status Mint (#7fd579), and Status Orchid (#d896ff) for specific accent borders on feature cards or as dynamic interactive highlights.

### Don't

- Avoid using highly saturated colors for large surface areas; maintain the dark, muted aesthetic.
- Do not deviate from the Cofo Sans or Cofo Sans Pixel font families to preserve the distinct typography.
- Do not use generic square buttons; always apply either a 2px radius for standard buttons or a 1440px radius for pill-shaped elements.
- Avoid excessive use of shadows; elevation is primarily conveyed through background color shifts and subtle borders.
- Do not create dense blocks of text; use the defined type scale and line heights to ensure readability in the dark interface.
- Do not introduce new primary CTA colors; Ultraviolet Blue (#0000ff) is the singular action color.
- Do not use generic gray for borders that require semantic meaning; use specific accent colors like Neon Violet, Status Mint, or Status Orchid.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #ffffff
background: #161616
border: #ffffff
accent: #4141fc
primary action: #3b3b3b (filled action)

### 3-5 Example Component Prompts
1. Create a Primary Action Button: #3b3b3b background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a 'Connect' feature card: background Slate Gray (#3b3b3b), border 1px solid Neon Violet (#4141fc), 2px radius, 20px padding. Title "Connect" in Cofo Sans 24px, weight 400, Cloud White (#ffffff). Description "Link your apps and manage permissions..." in Cofo Sans 14px, weight 400, Ash Gray (#eaeaea).
3. Implement a navigation bar: Deep Space (#161616) background. Links "BUILD", "ABOUT", "COMMUNITY", "TOOLS" in Cofo Sans 14px, weight 400, Cloud White (#ffffff). Nav links have no padding and 0px radius, with 16px horizontal spacing between items.

---
_Source: https://styles.refero.design/style/2a3cdb2e-effe-406b-932e-37e4dc88ab1d_
