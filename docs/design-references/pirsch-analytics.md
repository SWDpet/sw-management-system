# Pirsch Analytics — Design Reference

> Warm, grounded analytics

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://pirsch.io](https://pirsch.io) |
| Refero page | [https://styles.refero.design/style/e4b9d41a-8165-47dd-818a-5f6810046ea9](https://styles.refero.design/style/e4b9d41a-8165-47dd-818a-5f6810046ea9) |
| Theme | light |
| Industry | saas |

## Overview

Pirsch Analytics presents a friendly, grounded data interface with a palette of soft neutrals and two distinct, warm accent colors: a sunny yellow and a fresh green. Its visual identity centers on legibility and a comfortable, almost tactile feel, achieved through rounded forms, ample spacing, and a consistent preference for black text on light, slightly off-white backgrounds. Components feel light and approachable, with minimal bordering and an emphasis on subtle background differentiation over strong elevation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary text, headings, icons, dark button text, borders for outlines and cards — defines the core structural elements against light backgrounds |
| Ghostly Gray | `#f8f5ed` | neutral | Background for cards and subtly differentiated surface elements — provides a soft, warm canvas contrasting with the stark white page background |
| Muted Stone | `#707070` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Sunbeam Yellow | `#ffda6e` | brand | Primary call-to-action button backgrounds and illustrative elements — injects a vibrant, energetic visual cue for key interactions |
| Leafy Green | `#6ece9d` | brand | Green action color for filled buttons, selected navigation states, and focused conversion moments |

## Typography

### DM Sans

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 14px, 16px, 18px, 20px, 24px, 28px, 64px |
| lineHeight | 1.25, 1.50, 2.22 |
| letterSpacing | -0.016, 0.286 |
| fontFeatureSettings | "ss03", "ss04" |
| substitute | Inter |
| role | The sole typeface, offering clean readability for all content. Weight 500 for headings, and 400 for body text achieves a modern, approachable feel. Distinctive letter-spacing choices for larger display text and smaller metadata create hierarchy. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 16 |  | 1.5 | -0.016 |
| body-lg | 18 |  | 1.5 | -0.016 |
| subheading | 20 |  | 1.25 | -0.016 |
| heading-sm | 24 |  | 1.25 | -0.016 |
| heading | 28 |  | 1.25 | -0.016 |
| display | 64 |  | 1.25 | -0.016 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 24px |
| cards | 24px |
| input | 6px |
| buttons | 12px |

- **elementGap** — 16px
- **sectionGap** — 48px
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#FFFFFF` | 0 | Dominant page background, providing a clean, bright foundation. |
| Card Surface | `#f8f5ed` | 1 | Background for content cards, offering subtle visual depth and warmth against the pure white canvas. |

## Components

### Primary Action Button - Yellow

**Role:** Filled button

Background: Sunbeam Yellow (#ffda6e). Text: Midnight Ink (#000000). Radius: 12px. Padding: 0px vertical, 24px horizontal. Used for primary calls to action.

### Secondary Action Button - Green

**Role:** Filled button

Background: Leafy Green (#6ece9d). Text: Midnight Ink (#000000). Radius: 12px. Padding: 0px vertical, 24px horizontal. Used for secondary or alternative calls to action.

### Small Tag Button

**Role:** Informational tag

Background: rgba(0, 0, 0, 0.04). Text: Midnight Ink (#000000). Radius: 24px. Padding: 0px for compact sizing. Used for categories or status labels, like the 'EASY START' tag.

### Content Card

**Role:** Surface for grouped content

Background: Ghostly Gray (#f8f5ed). Radius: 24px. Padding: 48px on all sides. No box shadow, relying on background color for subtle separation from the canvas.

### Text Input Field

**Role:** User data entry

Background: rgba(0, 0, 0, 0.04). Text/Placeholder: Midnight Ink (#000000). Radius: 6px. Padding: 10px vertical, 12px left, 24px right. No visible border in default state.

## Layout

The page adheres to a max-width, center-aligned container model, but the exact value is not explicitly defined, allowing for a flexible appearance. Sections are typically full-width background blocks (implied by the screenshot's white canvas extending to edges), but content within them is contained. The hero section features a prominent headline centered over a background that appears to be the main canvas. Content sections alternate between major textual headers and descriptive paragraphs, often followed by a two-column layout presenting features or benefits with text and supporting visual elements (icons, small sections of UI). Vertical rhythm is created by consistent section gaps of 48px, providing ample breathing room. The layout avoids complex grid structures, favoring stacked blocks and simple column arrangements that underscore clarity and directness. Navigation is implied to be a top bar.

## Imagery

This site predominantly uses icons and UI elements. Where present, imagery appears as simple, clean product screenshots or abstract graphics. Product graphics often display compact, focused UI snippets, sometimes with a subtle yellow or green accent matching the brand. Icons are generally filled, black, and have a moderate stroke weight, conveying clarity and function. The overall density of imagery is low to moderate, with a strong emphasis on content and clear text, using visuals to explain or highlight rather than to dominate the visual space. Logos of partner brands are grayscale, underscoring the focus on content and analytics rather than visual fanfare.

## Dos & Donts

### Do

- Use DM Sans exclusively for all typography, leveraging weights 400 and 500 for primary text and headings respectively.
- Implement Sunbeam Yellow (#ffda6e) for primary interactive elements and Leafy Green (#6ece9d) for secondary actions or positive cues.
- Adhere to a 24px border radius for cards and larger tags, and a 12px radius for buttons to maintain a consistent soft, rounded aesthetic.
- Employ Ghostly Gray (#f8f5ed) as the background for content cards to provide subtle visual separation from the main page canvas.
- Ensure generous spacing, using 'elementGap': 16px for vertical and horizontal separation of sibling elements, and 'sectionGap': 48px between major content blocks.
- Prioritize Midnight Ink (#000000) for all primary text and headings to ensure high contrast and readability on light backgrounds.
- Use Muted Stone (#707070) for secondary descriptive text and subheadings to create a clear hierarchy without resorting to lighter weights of Midnight Ink.

### Don't

- Avoid using harsh shadows or strong borders for elevation; rely on background color differences or ample padding for visual separation.
- Do not deviate from the DM Sans typeface; introducing other fonts would compromise the brand's unified typographic voice.
- Refrain from using heavily saturated or dark background colors; the system is built on a light, subtle achromatic foundation.
- Do not use generic square or overly sharp corners; the brand aesthetic is defined by its soft, rounded edges.
- Avoid dense, information-packed sections without breathing room; maintain the 'comfortable' spacing density with 'elementGap': 16px and 'sectionGap': 48px.
- Do not introduce new accent colors; maintain consistency with Sunbeam Yellow (#ffda6e) and Leafy Green (#6ece9d) for all chromatic interactive elements.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- text: #000000
- background: #FFFFFF (implied canvas)
- border: #000000 (for strong outlines), #707070 (for soft outlines)
- accent: #ffda6e
- primary action: #ffda6e (filled action)

**3-5 Example Component Prompts:**
- Create a section divider: Width 100%, height 1px, background color Midnight Ink (#000000), opacity 0.1, centered, with 48px vertical spacing above and below.
- Design a primary call-to-action button: Text 'Start Your Analytics Journey', font DM Sans weight 500, color Midnight Ink (#000000). Background Sunbeam Yellow (#ffda6e). Radius 12px. Padding 0px vertical, 24px horizontal. Minimum height 48px.
- Create a feature card: Background Ghostly Gray (#f8f5ed), border radius 24px. Inside, a heading 'Easy to Integrate' with DM Sans weight 500, #000000, size 24px, followed by body text 'Supports all major platforms and frameworks without hassle' with DM Sans weight 400, #707070, size 16px. Padding inside the card should be 48px.
- Generate an input field: Label 'Your Email' above the field with DM Sans weight 400, #000000, size 16px. Input box background rgba(0, 0, 0, 0.04), text color Midnight Ink (#000000), border radius 6px. Padding 10px vertical, 12px left, 24px right. Placeholder text 'info@example.com' in Muted Stone (#707070).

---
_Source: https://styles.refero.design/style/e4b9d41a-8165-47dd-818a-5f6810046ea9_
