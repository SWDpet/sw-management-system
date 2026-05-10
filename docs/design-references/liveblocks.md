# Liveblocks — Design Reference

> Midnight data stream. Dark surfaces meet sharp textual readouts and subtle, glowing accents.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://liveblocks.io](https://liveblocks.io) |
| Refero page | [https://styles.refero.design/style/9b9b0ca1-5067-4115-b62f-ee0e43d1f37f](https://styles.refero.design/style/9b9b0ca1-5067-4115-b62f-ee0e43d1f37f) |
| Theme | dark |
| Industry | devtools |

## Overview

This design evokes a high-tech control room aesthetic, blending deep darkness with minimalist precision. Dominant #000000 backgrounds create an immersive, focused environment. Subtle gradients and white text act as illuminated data readouts, while crisp, achromatic grays define interactive elements and containers, maintaining visual order. The interplay of clean typography and precise component definition projects an image of robust, technical reliability.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Void | `#000000` | neutral | Page backgrounds, significant section backgrounds and containers. |
| Ghostly White | `#ffffff` | neutral | Primary text, visible borders, icons, and interactive elements. Its high contrast on dark backgrounds drives readability. |
| Graphite Base | `#111111` | neutral | Secondary backgrounds, subtle elevated surfaces within dark sections. |
| Ash Gray | `#918d8d` | neutral | Muted text, secondary button text, inactive icons – providing support without competing with primary content. |
| Light Concrete | `#edecee` | neutral | Subtle light backgrounds for specific sections or elements, offering visual relief against darker tones. |
| Distant Gray | `#b7b4b4` | neutral | Tertiary text, subtle separators, and less prominent UI elements. |
| Slate Text | `#635f5f` | neutral | Secondary text for details and supplementary information. |
| Digital Blue | `#0090ff` | brand | Call-to-action buttons, primary interactive elements – a vivid hue that stands out within the dark palette. |
| Cosmic Violet | `#9f8dfc` | accent | Accent color for specific UI indicators, links, or highlighted information, adding a touch of digital luminescence. |
| System Teal | `#70e1c8` | accent | Secondary accent for interactive states or status indicators, balancing the violet with a cool, technological feel. |
| Terminal Red | `#f76e99` | accent | Subtle accent for highlighted information or specific UI elements where an alert or active state is needed. |
| Void Radiance | `#000000` | neutral | Subtle background gradient for background elements, providing a deep, immersive feel without harsh lines. |

## Typography

### suisse

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 10px, 11px, 12px, 14px, 16px, 20px, 24px, 32px, 48px, 52px, 60px, 64px |
| lineHeight | 1.00, 1.05, 1.10, 1.20, 1.25, 1.33, 1.38, 1.43, 1.50 |
| letterSpacing | -0.0200em, -0.0100em, 0.0040em, 0.0060em, 0.0100em, 0.0250em |
| substitute | system-ui |
| role | Primary typeface for all UI elements, headings, body text, and navigation. Its condensed nature at smaller sizes paired with expansive line heights for headlines creates a modern, precise feel. The use of custom letter-spacing across different sizes provides refined typographic control. |

### JetBrains Mono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 10px, 14px |
| lineHeight | 1.00, 1.43 |
| letterSpacing | 0.0250em |
| substitute | monospace |
| role | Monospaced font for code snippets, technical details, and any output requiring precise alignment. Its uniform character width reinforces the data-driven aesthetic. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.43 | 0.25 |
| heading | 24 |  | 1.33 |  |
| heading-lg | 32 |  | 1.25 | -0.02 |
| display | 48 |  | 1.2 | -0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| large | 12px |
| buttons | 6px |
| default | 4px |
| containers | 8px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 1280px

## Components

### CTA Button Group

### Trusted By Logo Strip

### Comment Thread Card

### Navigation Link

**Role:** Interactive elements in the header and footers.

Text in Ghostly White (#ffffff) on Midnight Void (#000000) background, with a 6px border radius, 4px padding on all sides. On hover, text color remains white, but an underline might appear (not fully specified).

### Primary Ghost Button

**Role:** Secondary call-to-action.

Transparent background (rgba(0, 0, 0, 0)), Ghostly White (#ffffff) text color, with a matching border of 6px radius. Padding is 8px top/bottom, 12px left/right. Provides a clear secondary action without competing with primary CTAs.

### Primary Filled Button

**Role:** Main call-to-action.

Midnight Void (#000000) background, Ghostly White (#ffffff) text. No visible border initially, 8px border radius. Padding is 0px top/bottom, 16px left/right. High contrast and solid fill for prominent action.

### Text Link Button

**Role:** Inline or subordinate actions.

Transparent background, Ash Gray (#918d8d) text. No border, no specific radius. Padding 0px top/bottom, 16px left/right. Used for simple navigation or supplementary actions.

### Tag / Badge

**Role:** Categorization or small status indicators.

Dark background (rgba(0,0,0,0) with some examples), Ash Gray (#918d8d) text. 6px border radius, 4px padding. Often appears with a 1px border matching text color for definition.

### Feature Card

**Role:** Container for showcasing features or content blocks.

Uses Graphite Base (#111111) for background. Padding of 16px on all sides. Features a 4px border radius for a subtle rounded edge against the sharp overall aesthetic. Text and interactive elements within will use Ghostly White or Ash Gray.

### Inline Code Block

**Role:** Displaying short code snippets.

JetBrains Mono font, 14px size, Ghostly White (#ffffff) text. Contained within surrounding text, with a subtle background and border to differentiate it.

## Layout

The page primarily employs a max-width contained layout, centering content within a 1280px constraint, though the hero section frequently uses a full-bleed dark background. The hero pattern features a centered, multi-line headline with a concise subtext, flanked by prominent call-to-action buttons. Section rhythm is driven by consistent vertical spacing, creating ample breathing room, occasionally breaking into alternating data panels or code examples. Content arrangement often utilizes centered stacks for headlines and CTAs, transitioning into 2-column layouts for features (text on one side, visual on the other, alternating). The page is text-dominant in places, with product screenshots serving to break up sections and highlight functionality. A standard top navigation bar persists, maintaining a cohesive header.

## Imagery

This design primarily utilizes dark-themed, abstract visuals and product screenshots. Product screenshots are typically full-bleed, showcasing the UI of the 'Liveblocks' product, often with a slightly distorted, vibrant magenta/pink gradient or pattern as a background element. These graphical backdrops provide a raw, energetic contrast to the otherwise structured UI. Illustrations are minimal and functional, acting as icons or small annotations. Photography is absent. The overall visual language for imagery is one of showing the product in action against a dynamic, abstract backdrop that hints at data and energy. Iconography is primarily monochromatic, using Ghostly White or Ash Gray outlines or fills, maintaining the precise and clean UI aesthetic.

## Elevation philosophy

This system minimizes traditional shadows, favoring crisp 1px borders and subtle inset effects to define boundaries and states. Instead of projecting depth via blur, it uses fine outlines and changes in background color (e.g., from Midnight Void to Graphite Base) to differentiate layered elements, reinforcing a flat, precise, and high-tech aesthetic.

## Dos & Donts

### Do

- Prioritize Midnight Void (#000000) as the primary background for full-bleed sections, establishing the dark aesthetic.
- Use Ghostly White (#ffffff) for all primary text elements, ensuring maximum contrast and readability on dark backgrounds.
- Apply Digital Blue (#0090ff) exclusively to primary call-to-action buttons for clear visual hierarchy and impact.
- Maintain 6px as the standard border radius for interactive elements like buttons and tags, with containers using 8px.
- Utilize the Suisse font for all headings and body text, varying weights (400, 500) and sizes according to the type scale.
- Incorporate specific letter-spacing values (-0.0200em to 0.0250em) derived from the Suisse font profile for precise typographic control.
- Employ JetBrains Mono for all code snippets and technical text to clearly distinguish it from UI copy.

### Don't

- Avoid introducing additional saturated colors beyond Digital Blue, Cosmic Violet, System Teal, and Terminal Red to preserve the controlled palette.
- Do not use high-saturation colors for large background areas or extensive textual content; reserve them for accents and interactive states.
- Refrain from using strong, opaque shadows; utilize the provided rgb(39, 38, 38) 0px 0px 0px 1px for subtle outlining only.
- Do not deviate from the defined border radii; maintain 6px for buttons and 8px for containers to ensure visual consistency.
- Avoid using decorative fonts or excessive font variations that would disrupt the precise and technical typographic identity.
- Do not add unnecessary padding or margin around elements; adhere to the compact spacing tokens (e.g., 8px element gap, 16px card padding) for content density.
- Never use red as a primary success indicator, as its allocated role as 'Terminal Red' is for specific accents.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #ffffff (Ghostly White)
- Background: #000000 (Midnight Void)
- CTA: #0090ff (Digital Blue)
- Border: #918d8d (Ash Gray)
- Accent: #9f8dfc (Cosmic Violet)

### Example Component Prompts
1. Create a primary hero section with a centered headline: Background Midnight Void (#000000). Headline 'Realtime infrastructure for multiplayer apps' in Suisse font, weight 400, size 48px, line-height 1.2, letter-spacing -0.02em, color Ghostly White (#ffffff). Subtext 'Empower collaboration with Liveblocks' in Suisse font, weight 400, size 16px, line-height 1.5, color Ash Gray (#918d8d).
2. Generate a 'Get Started' button: Digital Blue (#0090ff) background, Ghostly White (#ffffff) text. Suisse font, weight 400, size 16px, line-height 1.5. Border radius 6px. Padding 8px top/bottom, 12px left/right. No border. 
3. Design a feature card: Graphite Base (#111111) background. Border radius 8px. Padding 16px. Headline 'Presence Indicators' in Suisse font, weight 500, size 24px, line-height 1.33, color Ghostly White (#ffffff). Body text 'Show who's online and active in real-time' in Suisse font, weight 400, size 14px, line-height 1.43, color Ash Gray (#918d8d).
4. Create a secondary ghost button: Transparent background, Ghostly White (#ffffff) text color. Suisse font, weight 400, size 16px, line-height 1.5. Border 1px solid Ghostly White (#ffffff). Border radius 6px. Padding 8px top/bottom, 12px left/right.
5. Render a code snippet: Wrap the text `useLiveblocks()` in a JetBrains Mono font, weight 400, size 14px, line-height 1.43, color Ghostly White (#ffffff). Add a Graphite Base (#111111) background with 4px padding and a 4px border radius.

---
_Source: https://styles.refero.design/style/9b9b0ca1-5067-4115-b62f-ee0e43d1f37f_
