# Stocketa — Design Reference

> Soft-edged transparency on cloud-white

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://stocketa.com](https://stocketa.com) |
| Refero page | [https://styles.refero.design/style/c1f7749f-319b-491b-8243-22050e85994f](https://styles.refero.design/style/c1f7749f-319b-491b-8243-22050e85994f) |
| Theme | light |
| Industry | fintech |

## Overview

Stocketa presents a focused financial interface with a light, almost ethereal feel. Its design language is dominated by soft, rounded forms and transparent components, creating a sense of lightness and openness. Color is used sparingly, primarily for functional accents and a subtle brand gradient, letting the content breathe. Typography is compact and precise, maintaining clarity within dense information displays, while nuanced shadows and inset effects give components a sophisticated, layered depth without heaviness.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas | `#e0dde2` | neutral | Primary page background, provides a soft, warm-neutral base |
| Ash | `#f0f0f0` | neutral | Subtle background for UI elements, creates a slight separation from the canvas |
| Graphite | `#000000` | neutral | Dominant text color for headings and body content, ensures high contrast on light backgrounds |
| Stone Gray | `#abbdcf` | neutral | Decorative fills primarily for subtle background graphics and UI elements, contributes to the light, desaturated aesthetic |
| Slate | `#9aa1b2` | neutral | Muted border color, helper text, and secondary link states, offering softer visual weight than Graphite |
| Cloud Mist | `#a5afcb` | neutral | Light borders for subtle separation and decorative elements, harmonizes with the soft UI palette |
| Blue Violet | `#5b638c` | accent | Accent stroke for graphics and illustrations, hints at technical sophistication |
| Luminescent Violet | `#995bb9` | brand | Accent color for important headings, icons, and decorative fills — a primary brand splash |
| Midnight Indigo Outline | `#3a4766` | accent | Border and text for ghost buttons and interactive elements, a deep, muted action color |
| Highlight Gradient | `#60eb8c` | accent | Vivid linear gradient used for dynamic background effects or highlighting specific content areas with vibrant color transitions |
| Faint Blue Overlay | `#84a1d0` | accent | Subtle linear gradient used for soft overlays or to add depth to background elements, enhancing the light and airy feel |

## Typography

### averta standard

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700, 800 |
| weights | 400, 500, 600, 700, 800 |
| sizes | 14px, 15px, 16px, 17px, 19px, 27px, 28px, 50px, 53px, 62px, 98px |
| lineHeight | 1.00, 1.10, 1.15, 1.20, 1.25, 1.35, 1.40 |
| letterSpacing | -0.21, -0.23, -0.26, -0.28, -0.32, -0.44, -0.45, -0.8, -0.85, -1, -1.57 |
| substitute | system-ui, sans-serif |
| role | Primary typeface for all elements, from headings to body text and buttons. It offers a compact, modern feel with precise letter-spacing, ensuring readability and a sleek aesthetic across all scales. The controlled letter-spacing on larger sizes gives headlines a confident, understated presence. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.4 | -0.21 |
| body | 16 |  | 1.4 | -0.26 |
| subheading | 19 |  | 1.25 | -0.32 |
| heading | 27 |  | 1.2 | -0.44 |
| heading-lg | 53 |  | 1.15 | -0.85 |
| display | 98 |  | 1 | -1.57 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 100px |
| cards | 18px |
| buttons | 100px |
| default | 22px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 14px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#e0dde2` | 1 | Primary background for the entire application, serving as the base layer. |
| Ash Surface | `#f0f0f0` | 2 | A slightly elevated background color for larger UI elements or sections, providing subtle differentiation. |
| Soft Card | `#537498` | 3 | Interactive cards or information panels that float above the base, distinguished by a subtle shadow and rounded corners. |

## Components

### Ghost Action Button

**Role:** Secondary call to action or navigation link.

Text: Midnight Indigo Outline (#3a4766) on a transparent background, bordered with Midnight Indigo Outline (#3a4766), very rounded corners (100px radius). Padding: 13px vertical, 30px horizontal. Font: averta standard, weight 400.

### Soft Card

**Role:** Container for content, elevated slightly from the background.

Background: rgba(83, 116, 152, 0.07). Border Radius: 18px. Shadow: rgba(97, 110, 124, 0.114) 0px 4px 15px 0px, rgba(255, 255, 255, 0.39) 0px 1px 1px 0px inset, rgba(34, 50, 94, 0.08) 0px 1px 1px 0px. No internal padding.

### Feature List Item

**Role:** Padded container for individual features or information blocks.

Padded with 16px vertically. Icon in Luminescent Violet (#995bb9). Text in Graphite (#000000) for main points, Slate (#9aa1b2) for descriptions. Uses averta standard font.

### Content Card (No Shadow)

**Role:** Simple, flat content area on the page background.

Background: transparent. No border radius, no shadow. Padding varies by content, no intrinsic styling.

## Layout

The page primarily uses a max-width contained layout, centered on a light canvas. The hero section features a prominent, large headline in the Brand Gradient area, positioned centrally on the left, juxtaposed with a product showcase on the right. Subsequent sections follow a consistent vertical rhythm with minimal content density, using comfortable spacing. Content is largely presented as centered stacks of text with occasional feature lists, each item often accompanied by an icon. There's a subtle grid implied by the arrangement of features, but without hard lines. Navigation appears to be a minimal top bar, suggesting an application-focused approach.

## Imagery

The site uses a blend of abstract 3D elements and realistic product screenshots. Product screenshots are cleanly displayed within device frames, demonstrating functionality in a direct, uncluttered manner. Abstract 3D elements, characterized by soft, rounded, light gray forms, mimic organic shapes like pebbles or softly worn artifacts. These are used decoratively in the background, interacting with subtle light and shadow to create a sense of depth and modernity. Icons are outlined, featuring smooth, continuous strokes and are typically monochromatic or highlighted with Luminescent Violet. Visuals play a subtle, atmospheric role rather than a dominant, content-heavy one.

## Dos & Donts

### Do

- Use Canvas (#e0dde2) for primary backgrounds to maintain the light and airy theme.
- Apply a 100px border radius to all buttons and tags to create a consistent, soft, 'pill' shape.
- For primary headings, use Luminescent Violet (#995bb9) with averta standard weight 800 at appropriate scale sizes.
- Employ the Soft Card shadow (rgba(97, 110, 124, 0.114) 0px 4px 15px 0px with insets) for all elevated containers to achieve delicate depth.
- Ensure letter-spacing is applied precisely from the Typography tokens, especially for larger text, to maintain the sophisticated, compact feel (e.g., -1.57px at 98px).
- Separate sections with a `sectionGap` of 40px to provide comfortable visual breaks.

### Don't

- Avoid heavy drop shadows or opaque backgrounds that would counteract the light and transparent aesthetic.
- Do not use saturated colors for large UI areas; chromatic colors are reserved for accents and small functional elements.
- Do not deviate from the averta standard font family or its specified weights and letter-spacing for UI text.
- Avoid sharp corners; all UI components should feature rounded corners, preferably 18px or 22px defaults, or 100px for pill shapes.
- Do not introduce new border colors for interactive elements; use Midnight Indigo Outline (#3a4766) for ghost action borders.
- Do not overuse bold weights; reserve averta standard 800 primarily for main headings.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #e0dde2
border: #9aa1b2
accent: #995bb9
primary action: #3a4766 (outlined action border)

Example Component Prompts:
1. Create an Outlined Primary Action: Transparent background, #3a4766 border and text, 9999px radius, compact pill padding. Use it for the main CTA instead of a filled button.
2. Create a content section with a Soft Card: Background is Canvas (#e0dde2). Inside the Soft Card, place a subheading (averta standard, 19px, weight 600, #000000, letter-spacing -0.32px) and a body element (averta standard, 16px, weight 400, #000000, letter-spacing -0.26px). The card's border radius is 18px and uses the Soft Card shadow.
3. Create a feature list: Use Canvas (#e0dde2) background. Each feature item should have an outlined icon in Luminescent Violet (#995bb9), followed by a body text description (averta standard, 16px, weight 400, #000000, letter-spacing -0.26px). Each item should have 16px vertical padding, with 16px `elementGap` between items.

---
_Source: https://styles.refero.design/style/c1f7749f-319b-491b-8243-22050e85994f_
