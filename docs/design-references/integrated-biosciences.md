# Integrated Biosciences — Design Reference

> Dark Academia Laboratory: A meticulously organized workbench under a cool, precise spotlight.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://integratedbiosciences.com](https://integratedbiosciences.com) |
| Refero page | [https://styles.refero.design/style/80099f79-72b7-4367-b2e9-6a3d4a3e9e6a](https://styles.refero.design/style/80099f79-72b7-4367-b2e9-6a3d4a3e9e6a) |
| Theme | mixed |
| Industry | ai |

## Overview

Integrated Bio's design system presents a serious, academic aesthetic with a striking dark canvas and intentionally muted chromatic accents. Typography is the primary visual communicator, featuring a custom sans-serif with generous scaling and tight tracking that commands attention without overpowering. Surfaces are predominantly dark or off-white, acting as clean backdrops for content, with borders serving to define interactive elements or subtle divisions. The overall impression is one of modern scientific rigor, where visual flourish is minimal, and focus remains on data and information.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Page backgrounds, footer backgrounds, primary text on light surfaces |
| Carbon | `#222f30` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Fog | `#4d5757` | neutral | Muted body text, subtle borders |
| Sage Mist | `#c9cbbe` | neutral | Subtle headlines, decorative borders – imparts a soft, weathered quality to dark backgrounds |
| Cloud Canvas | `#e7e8e1` | neutral | Card backgrounds, secondary surface backgrounds – a slightly warm off-white |
| Light Gray | `#eeeeee` | neutral | Card backgrounds, elevated surface backgrounds – a cool, pale gray for contrast |
| Off-White | `#f7f7f5` | neutral | Dominant page background in light sections, hero background – a clean, nearly canvas-like base |
| Polar White | `#ffffff` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Bio-Green | `#cef79e` | accent | Green wash for highlight backgrounds, decorative bands, and soft emphasis behind content |
| Deep Sea | `#445e5f` | accent | Decorative fills, subtle background accents |

## Typography

### Aspekta

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 18px, 19px, 22px, 24px, 36px, 42px, 58px, 75px, 89px, 111px, 158px |
| lineHeight | 1.00, 1.10, 1.20, 1.30 |
| letterSpacing | -0.001, -0.001, -0.001, -0.006, -0.006, -0.02, -0.02, -0.02, -0.03, -0.03, -0.03, -0.03 |
| substitute | Inter |
| role | Headline, body text, navigation — the primary typeface, used across all major text elements. The custom font provides a distinctive, modern-without-being-trendy character. |

### Roboto Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px, 14px, 15px |
| lineHeight | 1.00, 1.23 |
| letterSpacing | -0.007, -0.008, -0.02 |
| substitute | IBM Plex Mono |
| role | Small labels, code snippets, metadata — used sparingly to provide a technical, structured counterpoint to Aspekta. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 16 |  | 1.2 |  |
| body-sm | 18 |  | 1.2 |  |
| body | 22 |  | 1.2 |  |
| body-lg | 24 |  | 1.2 |  |
| heading-sm | 42 |  | 1.2 |  |
| heading | 58 |  | 1.2 |  |
| heading-lg | 75 |  | 1.2 |  |
| display-sm | 111 |  | 1.2 |  |
| display | 158 |  | 1.2 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 20px |
| cards | 40px |
| buttons | 8px |
| default | 8px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Base | `#000000` | 0 | Primary page background, especially in hero sections and immersive experiences. |
| Off-White Canvas | `#f7f7f5` | 1 | Dominant background for content sections in light mode, primary light canvas. |
| Cloud Card | `#e7e8e1` | 2 | Background for secondary content cards or containers, offering a warm subtle contrast to Off-White. |
| Light Gray Card | `#eeeeee` | 3 | Background for additional content cards, providing a cooler, more neutral variation. |

## Components

### Navigation Button

**Role:** Outlined Button

Ghost button with an 8px border-radius, 4px padding on all sides, 1px border in Carbon (#222f30) or Polar White (#ffffff), with text in Aspekta weight 400.

### Header Action Button

**Role:** Filled Button

Solid Carbon (#222f30) button with Polar White (#ffffff) text (Aspekta weight 400), 8px border-radius, and 4px padding.

### Primary Action CTA Button (Text-only)

**Role:** Text Button

Text link on dark background, Aspekta weight 400, Polar White (#ffffff) color. Accompanied by a square Bio-Green (#cef79e) arrow icon.

### News Article Card

**Role:** Content Grid Item

A card with Off-White (#f7f7f5) background, 40px border-radius, and 40px internal padding. Content includes small-size Roboto Mono labels and Aspekta headlines, with a 1px Polar White border for definition. Image is full-bleed within the card's width atop the padding.

### Text Input

**Role:** Form Element

An input field with a 1px Polar White (#ffffff) border, 8px border-radius, and generous internal padding (17px left/right, 4px top/bottom).

### Monospace Data Tag

**Role:** Informational Label

Small text elements using Roboto Mono, such as '01 / 02' pagination or 'WHAT WE DO' labels. Text color is Carbon (#222f30) or Polar White (#ffffff), with a subtle rectangular background of Bio-Green (#cef79e) for emphasis.

## Layout

The page primarily uses a full-bleed layout, allowing sections to fill the viewport width. The hero section features a dark background with a large, centered headline and a secondary text block at the bottom left. Content sections alternate between dark and light backgrounds, creating a clear vertical rhythm. Inner content is often constrained, with text-heavy sections using a single column or a broad central column for readability. Card grids, like the 'Newsroom' section, use a multi-column layout with generous spacing. Navigation is a simple top bar, with elements flushed to the right, maintaining a minimal and unobtrusive presence.

## Imagery

This system primarily uses scientific and abstract imagery. Visuals are typically product-focused or show magnified biological structures, often with internal glow effects or stylized connections. Photography is minimal, if present, and tends to be clinical or abstract rather than lifestyle. Illustrations are geometric and clean, often outlined or using filled shapes that hint at molecular or data structures. Icons are minimal, outlined, and monochromatic, usually in Polar White on dark backgrounds or Carbon on light. Imagery serves to explain complex concepts or showcase scientific processes, rather than purely decorative purposes. Density is moderate, with images often contained within cards or as full-width elements in alternating sections.

## Dos & Donts

### Do

- Prioritize Aspekta weight 400 for all primary text, adjusting size to establish hierarchy—large headlines (75-158px) use tight tracking (-0.03em), body text (16-19px) uses minimal tracking (-0.001em).
- Employ the Midnight Ink (#000000) or Carbon (#222f30) for dark backgrounds, contrasting with Polar White (#ffffff) for primary text where readability is paramount.
- Use Off-White (#f7f7f5) or Cloud Canvas (#e7e8e1) as secondary backgrounds for content sections and cards in lighter contexts, framed by 1px Polar White (#ffffff) borders.
- Define interactive elements and sections with subtle 1px borders using Carbon (#222f30) or Polar White (#ffffff), or Sage Mist (#c9cbbe) for a softer presence on dark interfaces.
- Apply Bio-Green (#cef79e) sparingly as an accent for functional indicators, small decorative elements, or subtle background highlights, ensuring it acts as a precise punctuation mark.
- Maintain a comfortable density with 40px section gaps and card padding, with smaller 8px element gaps for details, creating clear content separation.
- Ensure all buttons and navigational elements utilize an 8px border-radius, while cards and larger containers feature a more generous 40px radius, providing a distinct shape language.

### Don't

- Avoid generic drop shadows or heavy elevation; surface differentiation primarily occurs through background color changes (#f7f7f5, #e7e8e1, #eeeeee) and subtle borders.
- Do not introduce additional vibrant colors; adhere strictly to the established Bio-Green (#cef79e) and Deep Sea (#445e5f) accents for chromatic emphasis.
- Refrain from using excessively open letter-spacing for headlines or body text; the system relies on a tightly tracked, confident typographic voice.
- Do not neglect the 1px border for interactive elements; it is crucial for defining clickable areas and maintaining the system's precise aesthetic.
- Avoid using Bio-Green (#cef79e) as a primary action background; it functions as an accent, not a call-to-action fill.
- Do not use high-fidelity, busy photography; prefer scientific or abstract imagery that complements the academic tone, or product-focused clean graphics.
- Avoid dense, unbroken blocks of text; break content into digestible sections with clear headings and sufficient inter-element spacing.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #222f30 (on light), #ffffff (on dark)
background: #000000 (dark mode), #f7f7f5 (light mode)
border: #222f30 (dark interactive), #ffffff (light interactive)
accent: #cef79e
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Design a 'What We Do' section with an Off-White (#f7f7f5) background. Include a title at 58px Aspekta weight 400, Carbon (#222f30), letter-spacing -0.02em. Below, add a Monospace Data Tag: 'WHAT WE DO' in Roboto Mono weight 400, 14px, Carbon (#222f30), with a Bio-Green (#cef79e) rectangular background.
3. Create a News Article Card on a Light Gray (#eeeeee) background with a 40px border-radius. Inside, use 40px padding. Place a small label, 'Publications', at 13px Roboto Mono weight 400, Carbon (#222f30). Below, a headline at 42px Aspekta weight 400, Carbon (#222f30), letter-spacing -0.02em. The card will contain a full-width image and a 'Read Article' link.

---
_Source: https://styles.refero.design/style/80099f79-72b7-4367-b2e9-6a3d4a3e9e6a_
