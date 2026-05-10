# Public — Design Reference

> Crisp Editorial Clarity: a high-contrast financial journal on a pristine, structured page.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://public.com](https://public.com) |
| Refero page | [https://styles.refero.design/style/9d16aa65-cef7-4bf7-83c8-91837a248cd9](https://styles.refero.design/style/9d16aa65-cef7-4bf7-83c8-91837a248cd9) |
| Theme | light |
| Industry | fintech |

## Overview

Public's design system evokes a confident, editorial demeanor, balancing sharp, sophisticated typography with subtle, functional use of color. The primary palette is a clean, high-contrast monochrome, serving as a neutral canvas for data and product interfaces. Visual accents are delivered through a singular, vivid violet for interactive elements and an electric blue for subtle highlights, creating focal points without visual clutter. Surfaces are predominantly light and spacious, grounded by slim borders and minimal shadow, suggesting precision and clarity in financial tooling.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink | `#000000` | neutral | Primary text, darkest backgrounds, prominent borders |
| Canvas | `#ffffff` | neutral | Page backgrounds, card surfaces, UI elements requiring high contrast |
| Smoke | `#e9edf3` | neutral | Subtle borders, dividers, card outlines, background separation |
| Graphite | `#262626` | neutral | Dark card backgrounds, secondary dark text, subtle dark interface elements |
| Ash | `#dce2ea` | neutral | Muted borders and text, outlines for secondary elements |
| Jet | `#1b2128` | neutral | Dark navigation text, dark UI element borders, secondary dark backgrounds |
| Slate | `#516880` | neutral | Secondary text, muted links, subtle informational accents |
| Cloud | `#a8b4bf` | neutral | Subtle border accents, light informational text, placeholder content |
| Ultramarine | `#0027b3` | brand | Active link text, interactive element borders, functional highlights |
| Azure Glow | `#95d0ff` | accent | Decorative icons, light accent fills, subtle hover states |
| Lilac Mist | `#fff0fe` | accent | Light supporting surface for subtle backgrounds and section separation. Do not promote it to the primary CTA color |
| Sky Veil | `#cdf3ff` | accent | Gray supporting accent for decorative details and low-frequency emphasis. Do not promote it to the primary CTA color |
| Ocean Deep | `#00379a` | accent | Subtle icon fills, deeper blue decorative elements |
| Nav Blue | `#566e8c` | accent | Navigation active states, subtle header text |

## Typography

### Denton

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 48px, 52px, 80px |
| lineHeight | 1.00, 1.11, 1.12, 1.13 |
| substitute | Playfair Display |
| role | Display and primary headings – its light weight at large sizes conveys authority through understatement, setting an intellectual tone. |

### Invest Pro

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 12px, 14px, 16px, 20px, 24px, 32px |
| lineHeight | 1.00, 1.13, 1.14, 1.15, 1.17, 1.20, 1.28, 1.29, 1.31, 1.37, 1.38, 1.50 |
| substitute | IBM Plex Serif |
| role | Secondary headings, subheadings, and emphasized text segments – providing a structured, confident contrast to Inter for key informational blocks. |

### Invest Pro

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 12px, 14px, 16px, 20px, 24px, 32px |
| lineHeight | 1.00, 1.13, 1.14, 1.15, 1.17, 1.20, 1.28, 1.29, 1.31, 1.37, 1.38, 1.50 |
| substitute | IBM Plex Serif |
| role | More emphatic secondary headings and callouts, providing a slightly bolder touch while retaining the core character of Invest Pro. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| sizes | 10px, 11px, 12px, 14px, 16px |
| lineHeight | 1.00, 1.15, 1.42, 1.43, 1.50, 1.54, 1.60, 2.00 |
| letterSpacing | normal |
| substitute | Inter |
| role | Body copy, navigation, and detailed information – its neutral, legible form supports dense data and long-form content. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| sizes | 10px, 11px, 12px, 14px, 16px |
| lineHeight | 1.00, 1.15, 1.42, 1.43, 1.50, 1.54, 1.60, 2.00 |
| letterSpacing | normal |
| substitute | Inter |
| role | Strong emphasis within body text, labels, and small headings, maintaining high readability at smaller sizes. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0 |
| body-lg | 14 |  | 1.43 | 0 |
| heading-sm | 20 |  | 1.2 | 0 |
| heading | 24 |  | 1.17 | 0 |
| heading-lg | 32 |  | 1.28 | 0 |
| display | 48 |  | 1.11 | 0 |
| display-lg | 80 |  | 1.13 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 16px |
| buttons | 100px |
| default | 4px |
| minimal | 1px |
| partial | 12px |
| smallInteractive | 8px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#ffffff` | 0 | Dominant page background, base for light sections. |
| Section Card | `#fafa_fafa` | 1 | Light background for grouping content segments within the main canvas. |
| Monochrome Elevated Card | `#262626` | 2 | Darker, slightly elevated card backgrounds for focused information or interactive elements. |

## Components

### Primary Ghost Button

**Role:** Call to action

An outlined button with transparent background, white text (#ffffff), and a subtle border of Ultramarine (#0027b3) or rgba(255, 255, 255, 0.1). Rounded with 100px radius and 40px horizontal padding. Features a dual colored shadow to hint at interactivity: rgba(255, 196, 250, 0.25) 8px -8px 20px 0px, rgba(56, 205, 255, 0.25) -8px 8px 30px 0px.

### Filled Action Button

**Role:** Call to action

A solid black button (#000000) with white text (#ffffff). Rounded corners set at 100px, with 40px horizontal padding. Used for primary actions like 'Get started'.

### Section Card

**Role:** Content container

A light gray background card (#fafa_fafa) with 16px border-radius. No shadow, flush padding for internal content. Used to group related features or information.

### Monochrome Elevated Card

**Role:** Accent / Elevated content.

A dark gray background card (#262626) with a subtle shadow (rgba(0, 0, 0, 0.08) 0px 1px 10px 0px). Has 6px border-radius and 16px padding on all sides. Used for focused content blocks, such as AI agent details or market briefs.

### Informational Badge

**Role:** Metadata / Status

Transparent background badge with black text (#000000) and no border radius. Used for small, contextual labels like '3.30% APY*'.

### Feature Link Card

**Role:** Navigation / Feature overview

Transparent background with no border-radius or shadow, 22px left padding. Utilized for introductory feature links like 'Stocks Build your portfolio'.

## Layout

The page primarily uses a contained layout with elements centered within a maximum width, though the hero section can be full-bleed. The hero features a large, light-weighted headline centered over a background that integrates product imagery. Sections exhibit a consistent vertical rhythm, often alternating between centered stacks of text, or two-column layouts pairing text with a visual. Content is arranged with a sense of order and precision, frequently utilizing visual separations for clarity. Navigation is a sticky top bar with clearly delineated links and distinct login/signup buttons.

## Imagery

The visual language for imagery is primarily product-focused and functional. It features clean product screenshots of UI interfaces, often displayed within device mockups. These are presented without heavy filters or dramatic lighting, maintaining a sense of realism and direct utility. Imagery primarily serves an explanatory role, showcasing the product's functionality rather than purely decorative atmosphere. The density is moderate; images are used to break up text-heavy sections and illustrate concepts, but the layout remains text-dominant.

## Dos & Donts

### Do

- Prioritize Ink (#000000) for all primary text and critical headlines to maintain high contrast and legibility.
- Use Ultramarine (#0027b3) exclusively for interactive elements like links and button borders, ensuring functional color remains a distinct accent.
- Apply Denton at Weight 300 for all large headings (48px and above) to convey a sense of understated authority.
- Maintain a default border-radius of 4px for most UI elements, reserving 100px for buttons and 16px for prominent cards.
- Employ Smoke (#e9edf3) for hairline borders and subtle dividers to structure content without heavy visual lines.
- Ensure generous spacing with an 8px element gap and 16px card padding to maintain a spacious, uncluttered layout.
- Use Invest Pro for secondary titles and emphasized text, ensuring it complements Denton without competing for attention.
- Leverage the dual-tinted shadow (rgba(255, 196, 250, 0.25) 8px -8px 20px 0px, rgba(56, 205, 255, 0.25) -8px 8px 30px 0px) for highly interactive ghost buttons to add distinct visual depth.

### Don't

- Avoid using highly saturated colors outside of the defined Ultramarine (#0027b3) and Azure Glow (#95d0ff) accents.
- Do not introduce heavy drop shadows or decorative gradients on surfaces, as the design maintains minimal elevation.
- Refrain from mixing font families randomly; adhere strictly to Inter for body text, Invest Pro for structured text, and Denton for display headings.
- Do not use border-radius values other than 1px, 4px, 8px, 12px, 16px, 100px, or 999px.
- Do not use #0000ee (browser default link blue) for any link states; use Ultramarine (#0027b3) instead.
- Do not use an element gap smaller than 8px for vertical or horizontal spacing between related items.
- Avoid excessive use of uppercase text; reserve it only for specific, highly constrained labels or navigation items where it's explicitly observed.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #000000
background: #ffffff
border: #e9edf3
accent: #0027b3
primary action: #0027b3 (outlined action border)

### 3-5 Example Component Prompts
1. Create an Outlined Primary Action: Transparent background, #0027b3 border and text, 9999px radius, compact pill padding. Use it for the main CTA instead of a filled button.
2. Design a feature card for 'Bonds': Section Card (#fafa_fafa) background, 16px radius. Title 'Bonds' Invest Pro weight 400, 32px, Ink (#000000). Body text 'Fractionalized access to corporate bonds.' Inter weight 400, 16px, Slate (#516880).
3. Build a navigation menu item for 'Products': Inter weight 400, 16px, Ink (#000000) text. On hover, Ultramarine (#0027b3) text color. Use an 8px element gap between items.
4. Create a 'Market Briefing' block: Monochrome Elevated Card (#262626) background, 6px radius, 16px padding. Headline 'Market Briefing' Invest Pro weight 500, 20px, white text (#ffffff). Body text 'what’s moving the markets each day.' Inter weight 400, 14px, Cloud (#a8b4bf).
5. Construct a cookie consent banner: Canvas (#ffffff) background, 8px radius. Text 'We use cookies...' Inter weight 400, 12px, Slate (#516880). A 'Got it' button using a Filled Action Button style (#000000 background, #ffffff text, 100px radius).

---
_Source: https://styles.refero.design/style/9d16aa65-cef7-4bf7-83c8-91837a248cd9_
