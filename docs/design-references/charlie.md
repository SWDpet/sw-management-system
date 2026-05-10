# Charlie — Design Reference

> High-contrast editorial experimentalism

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://charlielemaignan.com](https://charlielemaignan.com) |
| Refero page | [https://styles.refero.design/style/34aa811f-6084-484c-b4c0-f587b514e970](https://styles.refero.design/style/34aa811f-6084-484c-b4c0-f587b514e970) |
| Theme | dark |
| Industry | design |

## Overview

Charlie Le Maignan showcases a bold, high-contrast visual identity, blending classic editorial typography with experimental letterforms. The system operates on a stark black canvas, punctuated by intense red and crisp white. Typography takes center stage, acting as both content and artistic expression, with custom fonts driving a distinctive, almost art-gallery aesthetic. Component design is minimal, emphasizing outlines and high contrast over complex surfaces or shadows.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Page backgrounds, headings (on white surfaces), button text on filled buttons |
| Paper White | `#ffffff` | neutral | Contrasting surfaces, button backgrounds, text, borders, active states — the primary highlight color against the dark canvas |
| Concrete Gray | `#838383` | neutral | Subtle border accents, muted text, secondary link text, for a softer contrast |
| Alert Red | `#ff0000` | accent | Striking background for hero sections, strong visual emphasis, full-bleed content blocks |

## Typography

### NeueHaas

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 19px, 20px, 40px |
| lineHeight | 1.08, 1.25, 1.32 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "case", "liga", "ss01", "ss02" |
| substitute | Helvetica Neue |
| role | Primary UI text, body copy, navigation, and footer links. Its classic structure provides stability for content sections. |

### Brasparz Variable

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 145px, 360px |
| lineHeight | 0.70 |
| letterSpacing | -0.0790em at 360px, -0.0770em at 360px, -0.0720em at 360px, -0.0690em at 360px, -0.0610em at 360px, -0.0510em at 360px, -0.0200em at 145px |
| fontFeatureSettings | "ss01" 0; "ss02" 0 |
| substitute | Bebas Neue |
| role | Dominant display headlines and stylistic elements. Its highly experimental variable nature and dramatic negative letter-spacing define the brand's 'wizardry' aesthetic. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 19 |  | 1.32 |  |
| subheading | 40 |  | 1.08 |  |
| heading | 145 |  | 0.7 | -0.02 |
| display | 360 |  | 0.7 | -0.079 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 100px |

- **elementGap** — 30px
- **sectionGap** — 59px
- **cardPadding** — 20px
- **pageMaxWidth** — 1306px

## Components

### Ghost Navigation Button

**Role:** Ghost button for primary navigation

Text: NeueHaas, 19px, weight 400, Paper White. Border: 2px solid Paper White. Padding: 7px vertical, 20px horizontal. Radius: 100px. Background: transparent.

### Filled Navigation Button

**Role:** Filled button for active navigation states

Text: NeueHaas, 19px, weight 400, Midnight Ink. Background: Paper White. Padding: 7px vertical, 20px horizontal. Radius: 100px.

### Footer Link

**Role:** Secondary interactive elements in the footer

Text: NeueHaas, 19px, weight 400, Concrete Gray. Underlined on hover (implied).

### Hero Banner - Red

**Role:** Full-width background section for dramatic visual impact

Background: Alert Red. Content usually includes large-format Brasparz Variable typography in Midnight Ink.

## Layout

The page primarily uses a full-bleed layout for sections, but content within these sections is constrained to a `pageMaxWidth` of 1306px, centered. The hero pattern features large-format typography (Brasparz Variable) over a full-bleed Alert Red background. Sections maintain consistent vertical spacing of 59px. Content is arranged in alternating blocks, often a full-bleed color block followed by a dark background section. Navigation is a minimal top bar with ghost or filled pill-shaped buttons.

## Imagery

The site primarily uses bold, custom typography as its main imaginal and graphic element. When present, imagery (implied in background content) is likely treated with high contrast and minimal adornment, often acting as a canvas for text. Icons are implied to be minimal and monochromatic (Paper White on Midnight Ink) with defined strokes. The overall density is image-light, text-dominant, with typeforms themselves providing visual richness.

## Dos & Donts

### Do

- Prioritize text as a primary visual element, using Brasparz Variable for large, impactful headlines.
- Maintain high contrast with a strict black and white base palette; use Alert Red sparingly for strong statements.
- Apply a 100px border radius to all buttons to create a distinct pill shape.
- Utilize NeueHaas for all functional UI elements, ensuring readability against the expressive display typography.
- Implement the large negative letter-spacing provided for Brasparz Variable headlines to achieve the signature condensed aesthetic.
- Use Paper White for borders and text on dark backgrounds to define interactive elements and important information.

### Don't

- Avoid using multiple chromatic colors; limit color accents strictly to Alert Red.
- Do not introduce complex UI shadows, as the system relies on high-contrast borders and solid color blocks for depth.
- Refrain from using moderate or low-contrast text on dark backgrounds; always ensure text elements are Paper White or Concrete Gray for legibility.
- Do not deviate from the specified custom typefaces; their unique characteristics are central to the brand identity.
- Avoid generic button shapes; buttons must maintain the distinct pill shape with a 100px border radius.
- Do not use subtle background patterns or textures; surfaces should remain flat and monochromatic.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #ffffff
- background: #000000
- border: #ffffff
- accent: #ff0000
- primary action: #ffffff (filled action)

Example Component Prompts:
- Create a ghost navigation button: Text 'Work' (NeueHaas, 19px, weight 400, #ffffff), border 2px solid #ffffff, padding 7px 20px, radius 100px, background transparent.
- Create a filled navigation button: Text 'Type' (NeueHaas, 19px, weight 400, #000000), background #ffffff, padding 7px 20px, radius 100px.
- Create a hero section with a red background: Full-bleed background #ff0000. Headline 'DEMO' (Brasparz Variable, 360px, weight 400, #000000, letter-spacing -0.079em). Subtext 'A COLLECTION OF INDEPENDENT & STUDIO WORK.' (NeueHaas, 19px, weight 400, #000000).

---
_Source: https://styles.refero.design/style/34aa811f-6084-484c-b4c0-f587b514e970_
