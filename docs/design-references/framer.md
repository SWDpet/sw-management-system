# Framer — Design Reference

> Inky command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://framer.com](https://framer.com) |
| Refero page | [https://styles.refero.design/style/d417b42f-824d-45ba-a02e-cbef3b8ea0d8](https://styles.refero.design/style/d417b42f-824d-45ba-a02e-cbef3b8ea0d8) |
| Theme | dark |
| Industry | design |

## Overview

Framer's design pulsates like a high-contrast digital canvas, where every element is deliberately placed against a deep, inky black. The interplay of brilliant white typography and electric blue accents (#0099ff) against the #000000 background creates an immediate sense of clarity and impact, reminiscent of a command-line interface or a designer's dark-mode workspace. Generously rounded elements, especially 100px pill buttons, introduce soft dynamism into an otherwise sharp, modern aesthetic, making complex tools feel approachable. Subtle layers of near-black surfaces (#080808) provide depth without relying on heavy shadows, maintaining a lightweight, performant feel.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | neutral | Page background, primary surface, deep canvas. Provides stark contrast for all content. |
| Charcoal Canvas | `#080808` | neutral | Secondary surface, subtle elevated panels, card backgrounds. Creates depth against Absolute Zero. |
| Vapor White | `#ffffff` | neutral | Primary text, heading text, button text, foreground elements. High contrast for readability on dark backgrounds. |
| Ash Gray | `#999999` | neutral | Muted text, secondary labels, disabled states. Provides subtler information. |
| Silver Mist | `#cccccc` | neutral | Sub-secondary text, dividing lines. Softer than Ash Gray for less prominent elements. |
| Slate Dust | `#666666` | neutral | Tertiary text, less important information, placeholder text. |
| Deep Space | `#171717` | neutral | Tertiary surface layer, slightly darker elevated elements. |
| Electric Blue | `#0099ff` | brand | Interactive elements, links, active states, indicators, primary accent. Draws attention and signifies action. |
| Sky Indigo | `#0055ff` | accent | Secondary accent for graphical elements, subtle interactive states where Electric Blue might be too strong for background. |
| Cyan Fade | `#05ff9f` | accent | Subtle top-to-bottom gradient for background elements, adding a hint of color variation. |
| Tangerine | `#fd5d5c` | accent | Highlight for specific cards or callouts, purely decorative. |
| Goldenrod | `#fac80a` | accent | Highlight for specific cards or callouts, purely decorative. |
| Emerald | `#33c659` | accent | Highlight for specific cards or callouts, purely decorative. |
| Amethyst | `#9869fd` | accent | Highlight for specific cards or callouts, purely decorative. |

## Typography

### GT Walsheim Framer Medium

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 110px |
| lineHeight | 0.85 |
| letterSpacing | -0.0500em |
| substitute | Montserrat |
| role | Display headline. Extremely large, tight kerning for maximum impact. |

### GT Walsheim Medium

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 32px, 62px, 85px |
| lineHeight | 0.95, 1.00, 1.13 |
| letterSpacing | -0.0500em at 85px, -0.0310em at 62px, normal at 32px |
| fontFeatureSettings | "ss02", "tnum" |
| substitute | Montserrat |
| role | Primary headings (H1-H3). Characterized by compact line height and slight negative letter-spacing for a bold, condensed feel. Features stylistic alternates and tabular numbers. |

### Mona Sans

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 12px, 13px, 17px, 18px, 62px |
| lineHeight | 1.00, 1.20, 1.30, 1.70, 2.28 |
| letterSpacing | -0.0500em at 62px, -0.0460em for 17px/18px, normal elsewhere |
| substitute | Inter |
| role | Secondary headings (H4-H6). Offers a more neutral counterpart to GT Walsheim for less prominent titles. |

### Inter Variable

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 8px, 12px, 13px, 14px, 15px, 18px, 24px |
| lineHeight | 0.80, 1.00, 1.07, 1.20, 1.30, 1.40 |
| letterSpacing | -0.0200em at 18px and 24px, -0.0180em, -0.0100em, -0.0010em, 0.0300em for specific contexts |
| fontFeatureSettings | "cv06", "cv11", "dlig", "ss03" |
| substitute | Inter |
| role | Body text, subheadings, labels. Highly versatile with broad stylistic controls, ensuring clarity across various information densities. Features a wide range of stylistic sets. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 10px, 12px, 13px, 14px, 15px, 16px, 20px, 22px |
| lineHeight | 0.83, 1.00, 1.10, 1.15, 1.20, 1.30, 1.40, 1.60 |
| letterSpacing | -0.0530em to 0.0300em |
| fontFeatureSettings | "cv01", "cv05", "cv09", "cv11", "dlig" |
| substitute | Inter |
| role | Paragraphs, navigation, general UI text. Provides robust readability with specific handling for tabular numbers and stylistic alternates. |

### Open Runde

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 9px |
| lineHeight | 1.11 |
| letterSpacing | normal |
| fontFeatureSettings | "cv01", "cv09" |
| substitute | Varela Round |
| role | Fine print, metadata. Distinctive small-caps style via stylistic sets. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.3 |  |
| body-lg | 18 |  | 1.4 | -0.02 |
| subheading | 24 |  | 1.07 | -0.02 |
| heading-sm | 32 |  | 1 |  |
| heading | 62 |  | 1 | -0.031 |
| heading-lg | 85 |  | 0.95 | -0.05 |
| display | 110 |  | 0.85 | -0.05 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| images | 8px |
| inputs | 8px |
| modals | 12px |
| buttons | 100px |

- **elementGap** — 4px
- **sectionGap** — 64px
- **cardPadding** — 10px
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Testimonial Card

### Trusted By — Logo Bar

### Primary Hero Button

**Role:** Call to action

backgroundColor=rgba(255, 255, 255, 0.15), color=rgb(0, 0, 238) (browser default link color, interpret as Electric Blue #0099ff), borderRadius=100px, paddingTop=10px, paddingRight=14px, paddingBottom=10px, paddingLeft=14px. Text uses Inter font family. Appears as a white ghost button on dark background.

### Secondary Ghost Button

**Role:** Secondary call to action

backgroundColor=rgba(0, 0, 0, 0), color=rgb(0, 0, 0) (browser default, interpret as Vapor White #ffffff against dark), borderRadius=0px, paddingTop=0px, paddingRight=0px, paddingBottom=0px, paddingLeft=0px. Used for subtle actions or navigation within sections.

### Solid Primary Button

**Role:** High-emphasis call to action

backgroundColor=rgb(255, 255, 255), color=rgb(0, 0, 238) (browser default, interpret as Electric Blue #0099ff), borderRadius=100px, paddingTop=10px, paddingRight=14px, paddingBottom=10px, paddingLeft=14px. Used for critical actions on lighter backgrounds or when extra prominence is needed.

### Icon-only Button

**Role:** Action without text label

backgroundColor=rgba(255, 255, 255, 0.1), color=rgb(0, 0, 0) (browser default, interpret as Vapor White #ffffff), borderRadius=40px, paddingTop=0px, paddingRight=0px, paddingBottom=0px, paddingLeft=0px. Used for subtle actions like closing modals or navigation controls.

### Navigation Link

**Role:** Top navigation item

color=Vapor White (#ffffff), font-size=14px, font-weight=Inter Regular (400), no explicit background, padding: 0. Configured for horizontal menu display.

### Case Study Card

**Role:** Showcase customer work

background-color: Charcoal Canvas (#080808) or a variety of vivid accent colors (Tangerine, Goldenrod, Emerald, Amethyst) for visual distinction. Border radius: 8px. Text content uses various Inter weights. Often contains images or abstract patterns.

### Small Feature Card

**Role:** Highlight key features

min-height: 200px (inferred). backgroundColor=rgba(255, 255, 255, 0.15) or Charcoal Canvas (#080808). borderRadius=8px. Padding is implied 10-15px. Contains a small title and short body text.

### Text Input / Field

**Role:** User data entry

Implicit background matching surrounding surface (e.g. Charcoal Canvas #080808). Text color: Vapor White (#ffffff). Placeholder text color: Ash Gray (#999999). Focus ring: rgb(9, 9, 9) 0px 0px 0px 2px (Dark Shadow).

## Layout

The layout primarily uses a full-bleed dark canvas for the background. Content is centered and constrained within an implied max-width (no explicit pageMaxWidth, but content blocks are visually centered and not full-width). The hero section features a centered, oversized headline on a full-bleed dark background, with prominent pill-shaped call-to-action buttons. Subsequent sections alternate between large, impactful text blocks and grids of featured content. There's a prominent 3-column card grid for 'Made with Framer' examples, showcasing various visual styles within a consistent structural framework. Vertical spacing between sections is generous but not excessively sprawling, contributing to a compact visual density.

## Imagery

The visual language is a blend of tight product screenshots, abstract geometric shapes with vibrant accent colors, and stylized photography. Product screenshots are typically contained within cards, often featuring a subtle background or drop shadow. Illustrations are geometric and abstract, using brand accent colors (Tangerine, Goldenrod, Emerald, Amethyst) to add visual interest and break up the dark interface. Photography is used sparingly for social proof, featuring desaturated or monochrome portraits within circular masks. Icons are monochrome, outlined, and minimal, primarily in Vapor White against dark backgrounds. Imagery primarily serves to showcase products, demonstrate features, or provide decorative color accents, balanced with a high density of textual content.

## Dos & Donts

### Do

- Use Absolute Zero (#000000) as the primary background for all page sections to enforce a consistent dark theme.
- Apply Vapor White (#ffffff) for all main headings and body text on dark backgrounds to ensure maximum contrast and readability.
- Reserve Electric Blue (#0099ff) exclusively for interactive elements like links, buttons, and active states to guide user attention.
- Utilize 100px border radius for all primary calls-to-action to create distinct pill-shaped buttons.
- Employ the GT Walsheim fonts (GT Walsheim Framer Medium, GT Walsheim Medium) for all major headlines to convey the brand's unique character.
- Maintain a compact line height for all display and heading typography (e.g., 0.85 for 110px, 0.95 for 85px) to achieve a dense, type-driven aesthetic.
- Layer surfaces using Absolute Zero (#000000) and Charcoal Canvas (#080808) to create depth without relying on prominent shadows.

### Don't

- Avoid using light backgrounds for entire page sections; the theme is predominantly dark.
- Do not use generic system fonts for prominent headings; GT Walsheim is critical for brand identity.
- Do not introduce additional accent colors beyond Electric Blue (#0099ff) and the designated card highlights (Tangerine, Goldenrod, Emerald, Amethyst).
- Avoid box shadows for general elevation; subtle background color shifts (e.g., #000000 to #080808) provide the primary sense of layering.
- Do not use letter-spacing: normal for headlines; specific negative letter-spacing values (-0.0500em, -0.0310em) are integral to the typographic style.
- Do not vary border radius for buttons; stick to 100px for pill shapes and 8px for cards/inputs.
- Do not introduce complex gradients or textures on primary surfaces; the background should remain clean and near-monochromatic.

## Notes

### Agent Prompt Guide

4. **Create a Testimonial Section:** Set background to Absolute Zero (#000000). Use a `heading-sm` sized GT Walsheim Medium (32px, #ffffff, lineHeight 1) for the main quote. Underneath, add two lines of `body` text (Inter, 14px, #a6a6a6) for the author's name and title. The testimonial card itself could use Charcoal Canvas (#080808) with 8px radius.

---
_Source: https://styles.refero.design/style/d417b42f-824d-45ba-a02e-cbef3b8ea0d8_
