# Deta Surf — Design Reference

> Sky-bound clarity

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://deta.surf](https://deta.surf) |
| Refero page | [https://styles.refero.design/style/51752cfb-4fd4-464f-8b78-ecbc813830e1](https://styles.refero.design/style/51752cfb-4fd4-464f-8b78-ecbc813830e1) |
| Theme | light |
| Industry | productivity |

## Overview

Deta Surf uses a refreshing, sky-inspired aesthetic, where a vivid blue gradient forms the primary background, evoking expanse and clarity. UI elements are typically white, rounded cards, often with subtle shadows that provide a soft lift from the blue canvas. Typography blends elegant, traditional serifs for headlines with clean, modern sans-serifs for body text, creating a contrast that feels both authoritative and accessible. Interactive elements feature a bright, vivid blue, acting as functional punctuation against the otherwise serene palette.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Sky Canvas | `#a8d5ff` | brand | Page background, hero section background — its gradient brings depth and a sense of open space |
| Cloud White | `#ffffff` | neutral | Primary surface for cards, input fields, and some button backgrounds — providing a clean, bright contrast to the blue |
| Fog Gray | `#f3f4f6` | neutral | Secondary surface for cards and subtle background accents, offering a slightly muted alternative to Cloud White |
| Deep Space Blue | `#009afc` | brand | Primary action color for filled buttons and interactive accents — a vivid hue that stands out as active |
| Ocean Shadow | `#006dc8` | brand | Deep blue used for button and card shadows, tying elevation effects back to the brand's blue palette instead of generic gray; Inset shadow for active button states, providing a depressed effect |
| Graphite Text | `#000000` | neutral | Primary text color for headings and body content, offering high contrast against light surfaces |
| Slate Text | `#5b6882` | neutral | Muted text for secondary information, icons, and helper text |
| Light Steel Border | `#e5e7eb` | neutral | Hairline borders, dividers, and ghost button outlines – providing subtle separation |
| Faint Blue Border | `#cfe9fd` | neutral | Subtle border color for ghost buttons and some card outlines against lighter backgrounds |
| Ash Gray | `#808080` | neutral | Tertiary text color for fine print and less prominent content |
| Deta Pink Glow | `#ed008d` | accent | Brand icon accent, creating a small, bright visual pop |

## Typography

### Switzer

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 12px, 14px, 15px, 16px, 17px, 18px, 20px, 60px |
| lineHeight | 1.00, 1.11, 1.20, 1.40, 1.43, 1.45, 1.50, 1.56, 1.60 |
| letterSpacing | -0.0090em |
| substitute | Inter |
| role | Body text, navigation, and most UI elements. A clean, modern sans-serif that balances readability with a slightly condensed feel at smaller sizes, providing a practical counterpoint to Gambarino's expressive nature. |

### Gambarino

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 16px, 18px, 20px, 36px, 40px, 48px, 60px |
| lineHeight | 1.00, 1.11, 1.20, 1.40, 1.50, 1.56 |
| letterSpacing | -0.0100em |
| substitute | Georgia |
| role | Headlines and emphasis text. Its classic, confident serif forms give authority and distinction, particularly at larger sizes. Not a generic sans-serif, giving it a distinctive voice. |

### Tanker

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 19px |
| lineHeight | 1.30, 1.50 |
| letterSpacing | 0.0500em |
| substitute | Monospace |
| role | Specialized for 'Open Source' badge and potentially code snippets. A distinct, wide-tracked typeface that creates a bold, technical aesthetic for specific highlights. |

### Inter

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 16px |
| lineHeight | 1.5 |
| letterSpacing | -0.009 |
| role | Inter — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.108 |
| body-sm | 14 |  | 1.43 | -0.126 |
| body | 16 |  | 1.5 | -0.144 |
| subheading | 20 |  | 1.4 | -0.2 |
| heading | 48 |  | 1.11 | -0.48 |
| display | 60 |  | 1 | -0.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| buttons | 9999px |
| general | 8px |
| inputField | 16.2px |

- **elementGap** — 16px
- **sectionGap** — 32px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Sky Canvas | `#a8d5ff` | 0 | Dominant background for the page, setting the atmospheric tone with its gradient. |
| Fog Gray | `#f3f4f6` | 1 | Subtle background for less prominent cards, providing a soft contrast to the primary canvas. |
| Cloud White | `#ffffff` | 2 | Primary surface for interactive elements, content cards, and input fields, offering high contrast and perceived cleanliness. |

## Components

### Primary Filled Button

**Role:** Main call-to-action

Background: Deep Space Blue (#009afc). Text: Cloud White (#ffffff). Radius: 9999px (pill shape). Padding: 12px vertical, 48px horizontal. Shadow: rgba(0, 0, 0, 0.1) 0px 10px 15px -3px, rgba(0, 0, 0, 0.1) 0px 4px 6px -4px for soft elevation.

### Ghost Open Source Button

**Role:** Secondary action or status indicator

Background: transparent. Text: Graphite Text (#000000). Border: Light Steel Border (#e5e7eb) 1px solid. Radius: 9999px (pill shape). Padding minimal/content-driven.

### Search/Notebook Input

**Role:** Interactive input field

Background: Cloud White (#ffffff). Text: Graphite Text (#000000). Placeholder text: Slate Text (#5b6882). Border: none. Radius: 16.2px. Inset shadow: Ocean Shadow (#006dc8) 0px -4.8px 0px 0px for a subtle depth effect.

### Standard Card

**Role:** Content container

Background: Cloud White (#ffffff). Radius: 12px. Padding: 16px. Shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px for a subtle lift.

### Muted Card Background

**Role:** Secondary content container

Background: Fog Gray (#f3f4f6). Radius: 16px. No shadow, keeping it visually grounded.

### Header Navigation Link

**Role:** Global navigation item

Text: Cloud White (#ffffff). No background, minimal padding. Subtle underline on hover. Font: Switzer, 16px, weight 500.

### Text Link in Body

**Role:** Interactive text element

Text: Slate Text (#5b6882). Underlined on hover. Font: Switzer or Gambarino based on context, default is 16px, weight 400.

## Layout

The page uses a full-bleed layout for the hero section, featuring a prominent blue gradient background and centered, large typography. Subsequent sections appear to use a max-width content container, centered on the page. Content arrangement often alternates between text-dominant blocks and visual elements, creating a balanced rhythm. A visual grid is subtly suggested by the placement of multiple floating card-like elements in the hero. Vertical spacing is comfortable, with clear separation between sections. The navigation is a minimal top bar with brand logo and utilitarian links (Students, GitHub).

## Imagery

Imagery primarily consists of conceptual 3D renders (e.g., notebook, floating elements) and stylized, high-key photography of hands interacting with small, digital-like objects. The renders are often slightly tilted or floating to convey lightness and innovation. Photos are clean and bright, frequently using water or reflective surfaces, featuring product elements rather than lifestyle scenes. Icons are simple, outlined or filled, with a consistent stroke weight and occasionally feature the Deta Pink Glow accent. Visuals are used decoratively to enhance the 'elevate your thinking' metaphor.

## Dos & Donts

### Do

- Use Sky Canvas (#a8d5ff) with its default linear gradient for primary backgrounds to establish an expansive feel.
- Employ Cloud White (#ffffff) for all interactive cards and input surfaces to create bright, readable content blocks.
- Apply a 9999px border-radius to all buttons for a friendly, pill-shaped aesthetic.
- Set primary action buttons with Deep Space Blue (#009afc) background and Cloud White (#ffffff) text to guide user interaction.
- Maintain a clear visual hierarchy by using Gambarino for headlines (sizes 20px-60px) and Switzer for body text (sizes 12px-18px).
- Use a subtle shadow rgba(0, 0, 0, 0.05) 0px 1px 2px 0px for cards to provide minimal depth without visual heaviness.
- Leverage Faint Blue Border (#cfe9fd) for ghost button borders or secondary outlines to soften interactive elements.

### Don't

- Avoid sharp corners; all UI elements should have a radius of at least 8px, with interactive elements preferring 16.2px or 9999px.
- Do not introduce strong, dark backgrounds other than the hero gradient; maintain a predominantly light aesthetic for content sections.
- Refrain from using generic gray shadows for interactive elements; instead, use Ocean Shadow (#006dc8) for blue-tinted elevation.
- Do not apply excessive letter-spacing to body text; maintain the subtle negative tracking specified in Switzer and Gambarino for dense readability.
- Avoid using multiple vivid accent colors; Deep Space Blue (#009afc) should be the primary chromatic accent for user interaction.
- Do not deviate from the specified font families; Switzer and Gambarino are key to the brand's typographic identity.
- Do not use transparent backgrounds for text-heavy content cards; always provide a solid Cloud White or Fog Gray background for readability.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #a8d5ff
border: #e5e7eb
accent: #ed008d
primary action: #009afc (filled action)

Example Component Prompts:
Create a hero section with a gradient background using linear-gradient(rgb(74, 158, 255) 0%, rgb(123, 184, 255) 50%, rgb(168, 213, 255) 100%). Headline: 'Elevate your thinking.' (Gambarino, 60px, Graphit Text #000000, letter-spacing -0.6px). Subtitle: 'Surf is an intelligent notebook...' (Switzer, 20px, Slate Text #5b6882, letter-spacing -0.18px).

Create a Primary Action Button: #009afc background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Create a Standard Card: background Cloud White #ffffff, radius 12px, padding 16px. Shadow: rgba(0, 0, 0, 0.05) 0px 1px 2px 0px. Text: 'Notes for "Surf Demo" Video' (Switzer, 18px, Graphite Text #000000).

Implement an interactive Search Input: Cloud White #ffffff background, radius 16.2px, inset shadow rgb(0, 109, 200) 0px -4.8px 0px 0px, placeholder text Slate Text (#5b6882) 'Web Search' (Switzer, 16px).

---
_Source: https://styles.refero.design/style/51752cfb-4fd4-464f-8b78-ecbc813830e1_
