# Vetric — Design Reference

> Crystal clarity, data flow

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://vetric.io](https://vetric.io) |
| Refero page | [https://styles.refero.design/style/d51a3a30-965c-427e-9e40-ff177786889f](https://styles.refero.design/style/d51a3a30-965c-427e-9e40-ff177786889f) |
| Theme | light |
| Industry | ai |

## Overview

Vetric conveys data clarity through a light, expansive canvas, punctuated by crisp, dark typography and an expressive gradient system. The visual style balances technical precision with a soft, almost ethereal quality, using subtle card treatments and a distinct type pairing. Key information is delivered with restraint, allowing the vibrant, dynamic gradients to provide visual interest and signify data flow.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#090a1e` | brand | Primary text, heading text, critical UI elements, main call to action backgrounds, icon fills. Establishes a strong, grounded foundation against the light canvas |
| Arctic Canvas | `#ffffff` | neutral | Primary page background, card surfaces, ghost button backgrounds. Provides a clean, minimalist backdrop for all content |
| Cloud Gray | `#e0e0e0` | neutral | Subtle borders, inactive elements, secondary iconography. Creates visual separation without harsh lines |
| Smoke Gray | `#222222` | neutral | Navigation text, secondary button text, subtle link text. Provides contrast while remaining distinct from body text |
| Deep Slate | `#444557` | neutral | Secondary body text, detailed descriptions, helper text. A darker gray for robust information density |
| Misty Blue | `#c3dae3` | neutral | Hairline card borders, soft dividers. A very light, cool-tinted gray adds a subtle technical feel |
| Sky Flow | `#2969ff` | semantic | Info badges, decorative accents, subtle icon fills. Represents information and data visualization segments; Decorative visual elements, section separators. Transitions between key brand hues |
| Rose Haze | `#f75cc3` | brand | Decorative accents, illustrative elements. A vivid pink for visual flourish and brand recognition; Hero section background blend, decorative visual elements. Creates a dynamic, flowing feel |
| Lime Glow | `#5ab040` | semantic | Success states, positive indicators, badge backgrounds. Signifies positive outcomes |
| Sunbeam | `#ffd363` | semantic | Warning states, attention indicators, badge backgrounds. Draws attention to important information; Subtle background accents for abstract shapes. Completes the gradient flow with a warm, energetic tone |
| Lavender Mist | `#fff3fb` | accent | Light background for themed cards and badge backgrounds. A very light pink that hints at brand colors |
| Spring Bud | `#eaffed` | accent | Light background for themed cards and badge backgrounds. A pale green for success-related surfaces |
| Vanilla Cream | `#fffdea` | accent | Light background for themed cards and badge backgrounds. A soft yellow for warning-related surfaces |

## Typography

### Noto Serif

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 26px, 35px, 42px, 49px, 63px, 77px |
| lineHeight | 1.00, 1.06, 1.20, 1.33, 1.40, 1.43 |
| letterSpacing | -0.0500em, -0.0400em |
| substitute | Georgia |
| role | Display and primary headings. The serif typeface at large sizes provides gravitas and a classic, established feel, contrasting with the more modern sans-serif body. |

### Manrope

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 11px, 14px, 16px, 18px, 19px, 23px, 26px, 70px |
| lineHeight | 0.79, 1.00, 1.10, 1.30, 1.33, 1.50 |
| letterSpacing | -0.0600em, -0.0200em, 0.0600em |
| substitute | Inter |
| role | Body copy, subheadings, navigation, buttons, and all functional text. Its clean, geometric form ensures readability and a contemporary tone across the interface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 | 0.66 |
| body-sm | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.5 |  |
| heading-sm | 26 |  | 1.33 |  |
| heading | 35 |  | 1.33 | -1.4 |
| heading-lg | 49 |  | 1.2 | -1.96 |
| display | 77 |  | 1.06 | -3.08 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 7px |
| badges | 50% |
| buttons | 7px |
| interactive | 7px |

- **elementGap** — 9px
- **sectionGap** — 53px
- **cardPadding** — 28px
- **pageMaxWidth** — 

## Components

### Primary Filled Button

**Role:** Main call to action, critical interaction.

Background: Midnight Ink (#090a1e). Text: Arctic Canvas (#ffffff). Radius: 7px. Padding: 10.5px vertical, 28px horizontal. Manrope font.

### Ghost Button

**Role:** Secondary actions, navigation links where no strong call to action is needed.

Background: transparent. Text: Smoke Gray (#222222). No border. No explicit radius, appears as text link. Padding: 0.

### Neutral Outlined Button

**Role:** Ghost button with an explicit border. Secondary actions, team/jobs link.

Background: Arctic Canvas (#ffffff). Text: Midnight Ink (#090a1e). Border: 1px solid Cloud Gray (#d9d9d9 - derived from CSS tokens). Radius: 7px. Padding: 10.5px vertical, 28px horizontal.

### Info Card (Frosted Transparency)

**Role:** Displaying digestible chunks of information, often featuring subtle contextual data.

Background: Arctic Canvas with 70% opacity (rgba(255, 255, 255, 0.7)). Radius: 7px. Padding: 8.59px. No shadow. Border: 1px solid Misty Blue (#c3dae3).

### Themed Data Card - Pink

**Role:** Highlighting specific data points or features with a branded context.

Background: Lavender Mist (#fff3fb). Radius: 24.5px. Padding: 24.5px vertical, 17.5px horizontal. Text color often Rose Haze (#f75cc3). No shadow. Has a blur filter: blur(14px).

### Themed Data Card - Blue

**Role:** Highlighting specific data points or features with a branded context.

Background: #f1f5ff (from `--color--blue-light` token). Radius: 24.5px. Padding: 24.5px vertical, 17.5px horizontal. Text color often Sky Flow (#2969ff). No shadow. Has a blur filter: blur(14px).

### Semantic Badge - Pink

**Role:** Categorization or small highlight for data points.

Background: Lavender Mist (#fff3fb). Text: Rose Haze (#f75cc3). Radius: 50% (circle). Minimal padding. Used for small, circular indicators.

### Semantic Badge - Blue

**Role:** Categorization or small highlight for data points.

Background: #f1f5ff (from `--color--blue-light` token). Text: Sky Flow (#2969ff). Radius: 50% (circle). Minimal padding. Used for small, circular indicators.

## Layout

The page utilizes a max-width contained layout rather than full-bleed, though the hero section spans full-width with a gradient background. The hero features a large, centered headline and a distinct abstract graphic below it. Content sections alternate between visually distinct blocks: sometimes a full-width abstract gradient background, other times a clean white background with information structured in clear, responsive card grids or two-column text-left/image-right arrangements. The rhythm is spacious and flowing, with generous vertical section gaps and a clear visual hierarchy. Navigation is a simple, right-aligned top bar.

## Imagery

Imagery predominantly consists of abstract, glowing gradients and geometric shapes, particularly a prominent, faceted diamond graphic with a soft, iridescent glow. Photography is minimal, mostly confined to small, functional icons. When present, graphics are treated with transparency and soft blurs (blur(14px)) to maintain an ethereal, data-flow aesthetic. Icons are simple, outlined, and monochromatic, used sparsely for conceptual reinforcement rather than decorative flair. Density is low, allowing significant white space to dominate.

## Dos & Donts

### Do

- Use Noto Serif for all headings and display text, applying its characteristic negative letter-spacing for visual impact.
- Apply a 7px border-radius consistently to all buttons and cards for a softened, approachable feel.
- Maintain generous vertical spacing between sections, using the implicit sectionGap of approximately 53px.
- Prioritize Midnight Ink (#090a1e) for primary text and call-to-action fills, ensuring high contrast against light backgrounds.
- Employ the gradient system (Vetric Gradient 1, 2, 3) for hero backgrounds and large decorative elements to signify data dynamism and brand energy.
- Pair Manrope for body text and functional UI elements, ensuring readability and a modern, compact presence.
- Utilize faint, near-gray tints (Misty Blue #c3dae3, Cloud Gray #e0e0e0) for borders and subtle element separation to prevent visual heaviness.

### Don't

- Avoid using harsh shadows; prefer subtle transparency and very light borders for depth.
- Do not introduce new saturated primary colors; strictly adhere to Sky Flow, Rose Haze, Lime Glow, and Sunbeam for all chromatic accents.
- Never use generic black or white for text or backgrounds; always opt for Midnight Ink (#090a1e) for dark text and Arctic Canvas (#ffffff) for light backgrounds for brand consistency.
- Do not deviate from the established type scale and letter-spacing for Noto Serif and Manrope; these are critical for brand voice.
- Refrain from dense, information-heavy blocks; break content into digestible cards and maintain spacious layouts.
- Do not use full-bleed imagery without ample negative space; imagery should feel integrated and airy, not overwhelming.
- Avoid bright, contrasting borders; use muted, achromatic tones like Cloud Gray (#e0e0e0) or Misty Blue (#c3dae3) for card and element outlines.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #090a1e
background: #ffffff
border: #e0e0e0
accent: #f75cc3
primary action: #090a1e (filled action)

Example Component Prompts:
1. Create a Primary Filled Button: background Midnight Ink (#090a1e), text Arctic Canvas (#ffffff), 7px radius, 10.5px vertical padding, 28px horizontal padding, Manrope font.
2. Create an Info Card: background rgba(255, 255, 255, 0.7), border 1px solid Misty Blue (#c3dae3), 7px radius, 8.59px padding. Body text Deep Slate (#444557).
3. Create a Display Headline: 'Turning Data Chaos Into Clarity' using Noto Serif weight 500, size 77px, line height 1.06, letter spacing -3.08px, color Midnight Ink (#090a1e).

---
_Source: https://styles.refero.design/style/d51a3a30-965c-427e-9e40-ff177786889f_
