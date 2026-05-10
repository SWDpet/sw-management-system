# Control — Design Reference

> Terminal aesthetic, industrial, high-contrast, no-frills

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://cntrl.site](https://cntrl.site) |
| Refero page | [https://styles.refero.design/style/f4e3f701-0fa0-4601-b652-ecfc5c573f86](https://styles.refero.design/style/f4e3f701-0fa0-4601-b652-ecfc5c573f86) |
| Theme | light |
| Industry | design |

## Overview

Control presents a stark, high-contrast digital canvas, reminiscent of a developer's IDE or terminal, prioritizing clear information display over decorative flourishes. Its aesthetic is defined by a dominant black-on-white text, punctuated by precise, functional pops of vivid green and orange. Typefaces are chosen for their technical, almost glitch-art quality, and components adhere to a sharp, unrounded aesthetic, mirroring command-line interfaces or early web design. The overall impression is one of directness, utility, and a slight retro-futuristic edge.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, primary content areas |
| Ink Black | `#000000` | neutral | Primary text, strong borders, dark overlay backgrounds |
| Smoke Gray | `#f5f5f4` | neutral | Subtle background for secondary sections or elevated cards |
| Ghost Gray | `#d2d2d2` | neutral | Muted text, subtle borders, inactive elements |
| Utility Gray | `#3d3d3d` | neutral | Secondary text, component borders, subtle backgrounds for content blocks |
| Subtle Gray | `#c6c6c0` | neutral | Sub-footer text, fine lines, subtle ghost element borders |
| Muted Gray | `#babab9` | neutral | Helper text, less prominent information, subtle borders |
| Inert Gray | `#acaca6` | neutral | Inactive interface elements, placeholder text, lightest accent details |
| Action Orange | `#ff5c02` | accent | Orange action color for filled buttons, selected navigation states, and focused conversion moments. |
| Highlight Green | `#01ea40` | accent | Green accent for outlined action borders, linked labels, and lightweight interactive emphasis |
| Accent Yellow | `#ffdb4d` | accent | Decorative highlights, specific labels, component borders — adds a secondary visual pop |
| Dim Gray | `#7a7a7a` | neutral | Neutral button treatment for secondary actions and selected controls. |

## Typography

### Melange

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 16px, 25px, 36px, 71px, 146px |
| lineHeight | 1.01, 1.03, 1.06, 1.07, 1.52, 2.38 |
| letterSpacing | -0.058em at 146px, -0.055em at 71px, -0.051em at 36px, -0.050em at 25px, -0.037em at 16px |
| substitute | Georgia Pro |
| role | Headlines and display text — its sharp, angular forms and tight tracking give a bold, almost 'sliced' aesthetic |

### Favorit Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 9px, 10px, 16px |
| lineHeight | 1.20, 1.33, 2.00, 2.20, 2.38, 2.40 |
| letterSpacing | -0.03em at 16px, 0.02em at 10px, -0.037em for general use |
| substitute | Space Mono |
| role | Interface labels, navigation, secondary body text, and any element requiring a monospaced, technical feel; its uniform width aligns with the grid-based visual language |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Helvetica Neue |
| role | Default body text and general interface elements where a sans-serif is needed for legibility, providing a baseline of clarity against the more stylized typefaces |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 1.2 | -0.48 |
| subheading | 25 |  | 1.07 | -1.25 |
| heading | 36 |  | 1.06 | -1.84 |
| heading-lg | 71 |  | 1.03 | -3.9 |
| display | 146 |  | 1.01 | -8.47 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| none | 0px |
| pill | 273px |
| large | 40px |
| small | 8px |
| button | 0px |

- **elementGap** — 15px
- **sectionGap** — 64px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background, base layer for all content. |
| Smoke Gray | `#f5f5f4` | 1 | Slightly elevated card backgrounds, secondary sections for visual separation without direct elevation. |

## Components

### Primary Action Button (Filled)

**Role:** CTA button for primary actions

Solid Action Orange (#ff5c02) background with Canvas White (#ffffff) text. No border radius, sharp corners for a direct interaction feel. Padding as per content, but typically 0px implicitly around text.

### Secondary Action Button (Filled)

**Role:** CTA button for secondary actions

Solid Dim Gray (#7a7a7a) background with Ink Black (#000000) text. No border-radius. Used for 'Reject' type actions.

### Ghost Link Button

**Role:** Tertiary navigation or interactive elements

Transparent background with Highlight Green (#01ea40) text and border. No border-radius. Emphasizes interaction without visual weight.

### Info Card

**Role:** Content container for information grouping

Canvas White (#ffffff) background with an 8px border-radius, giving a slightly softer edge than buttons. No box-shadow for a flat, un-elevated appearance. Implicit padding for content.

### Elevated Card

**Role:** Content container with slight visual separation

Smoke Gray (#f5f5f4) background, 8px border-radius. Again, no shadow. Used for distinguishing content blocks from the main Canvas White background.

### Cookie Consent Panel

**Role:** System message overlay

A dark, somewhat translucent panel (not precisely specified, but visually #3d3d3d or #000000) with no border radius, containing buttons for user choices. Displays a stark, functional warning.

## Layout

The page primarily uses a full-bleed layout, particularly in the hero section, which features a large, centered headline and subtext over the Canvas White background. Content then transitions into a series of full-width blocks, often alternating between stark black text on white and areas incorporating the distinct grid graphic. There's a strong emphasis on consistent vertical spacing between sections. The overall presentation is somewhat sparse, giving breathing room to the bold typography and contained product visuals. Navigation is a simple, right-aligned text menu in the header, with social links also following this minimalist approach.

## Imagery

The site primarily uses product screenshots and abstract, grid-based graphics that mimic a design tool interface. Screenshots appear within a faux browser/canvas frame, often cropped to show key UI elements. Photography, when present, is usually contained within these frames, often with a somewhat distressed or low-fi quality. Icons are minimal, outlined, or solid black, maintaining the utilitarian, high-contrast theme. Imagery serves to demonstrate the product's functionality and aesthetic rather than decorative atmosphere, often appearing within a green grid overlay, reinforcing the idea of a design workspace.

## Dos & Donts

### Do

- Use Ink Black (#000000) for all primary text and strong borders against white backgrounds.
- Implement sharp, unrounded corners (radius: 0px) for all interactive buttons and inputs to maintain a utilitarian aesthetic.
- Utilize Type Melange for prominent headlines, applying its tight negative letter-spacing for visual impact.
- Incorporate Favorit Mono for all navigation, small labels, and any text that benefits from a consistent, technical monospaced feel.
- Reserve Action Orange (#ff5c02) exclusively for primary 'accept' or 'start' actions; use Highlight Green (#01ea40) only for active links and positive states.
- Maintain a clear visual hierarchy by limiting color pops to functional accents; keep main content and backgrounds in the neutral palette.
- Ensure generous use of Canvas White (#ffffff) for backgrounds to create a highly readable, open design reminiscent of a blank workspace.

### Don't

- Do not use rounded corners on any buttons or inputs; maintain the 0px radius for a consistent, sharp aesthetic.
- Avoid gradients or complex shadows for elevation; rely on solid colors and borders for visual separation.
- Do not introduce additional chromatic colors beyond Action Orange, Highlight Green, and Accent Yellow, to preserve the stark, focused palette.
- Do not use overly decorative imagery or soft, organic shapes; stick to geometric forms and high-contrast visuals.
- Do not use multiple font sizes or weights haphazardly; adhere strictly to the defined type scale and its associated styles.
- Avoid soft, desaturated background tones for primary content areas; ensure strong contrast with Ink Black (#000000) text on Canvas White (#ffffff).
- Do not use subtle gray (#babab9, #acaca6) text on white backgrounds for primary content, as it lacks sufficient contrast and dilutes the bold aesthetic.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
- text: #000000
- background: #ffffff
- border: #000000
- accent: #01ea40
- primary action: #ff5c02 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #ff5c02 background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a header navigation link: using Favorit Mono at 16px, weight 400, Ink Black (#000000) text. On hover, change text to Highlight Green (#01ea40).
3. Construct an Info Card: Canvas White (#ffffff) background, 8px border-radius, no shadow. Content to use Ink Black (#000000) for titles (Melange, 36px, weight 500, letter-spacing -1.84px) and Utility Gray (#3d3d3d) for body text (Arial, 16px, weight 400).

---
_Source: https://styles.refero.design/style/f4e3f701-0fa0-4601-b652-ecfc5c573f86_
