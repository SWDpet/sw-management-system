# Linktr — Design Reference

> Vibrant digital playground: link in bio as a personal, colorful hub

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://linktr.ee](https://linktr.ee) |
| Refero page | [https://styles.refero.design/style/b04c379c-7c33-4d69-8882-8d85d7768654](https://styles.refero.design/style/b04c379c-7c33-4d69-8882-8d85d7768654) |
| Theme | light |
| Industry | productivity |

## Overview

Linktree presents a playful, high-energy digital identity built on vibrant, contrasting color blocks and rounded, organic shapes. The design emphasizes clear calls to action and distinct content zones through a dynamic interplay of saturated hues. Typography is primarily functional, leaning into a friendly, approachable tone with varying weights and subtle tracking adjustments to maintain a conversational cadence. Components are often expressed with maximal border-radii, contributing to an overall soft, engaging user experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Forest Green | `#254f1a` | brand | Decorative card backgrounds, accent text on light backgrounds — conveys growth and natural energy |
| Chartreuse Pop | `#d2e823` | brand | Primary action button backgrounds, highlighted card accents, energetic background fill. This vivid lime serves as the main interactive accent |
| Hydrangea Blue | `#2665d6` | brand | Hero section backgrounds, prominent links, accent for specific UI elements — a complementary, strong color that frames content |
| Dahlia Purple | `#502274` | brand | Decorative background sections, signaling depth and richness |
| Currant Red | `#780016` | brand | Decorative card backgrounds, strong accent where high contrast and intensity are desired |
| Lavender Mist | `#e9c0e9` | brand | Elevated card backgrounds, soft button fills, decorative section backgrounds |
| Iris Blue | `#061492` | brand | Button backgrounds, card backgrounds, a profound, darker blue for functional elements |
| Goldenrod | `#d6a337` | accent | Distinct card backgrounds for callouts or special features |
| Graphite | `#1e2330` | neutral | Primary body text, headers, neutral button backgrounds, prominent borders — the core dark text color |
| White | `#ffffff` | neutral | Component backgrounds, form fields, text on dark backgrounds, icon fills |
| Marble | `#f3f3f1` | neutral | Page background, container backgrounds — the base canvas of the application |
| Lunar Dust | `#e8efd6` | neutral | Subtle background surfaces, slight variation on the main canvas |
| Slate Gray | `#676b5f` | neutral | Muted text, secondary links, subtle borders |
| Whisper Gray | `#adadad` | neutral | Card backgrounds, placeholder text |
| Text Gray | `#757575` | neutral | Placeholder text in input fields |
| Black | `#000000` | neutral | Dominant text color, borders, icon fills, utility elements |

## Typography

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| substitute | Helvetica Neue |
| role | Fallback for general body text and default page elements, providing a robust system font base. |

### Linksans Linksansvf

| Key | Value |
| --- | --- |
| weight | 400, 500, 700, 800 |
| sizes | 14px, 16px, 18px, 24px, 28px, 56px, 80px |
| lineHeight | 1.00, 1.06, 1.07, 1.20, 1.30, 1.50 |
| letterSpacing | -0.0200em at 80px, -0.0100em at 56px, 0.0100em at 14px |
| substitute | Inter |
| role | Primary brand typeface for headings, interactive elements, and key informational text. Its variable font nature allows for precise weight control, contributing to distinct visual hierarchy. |

### Linksans

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 12px, 13px, 14px, 15px, 20px, 24px, 25px, 51px |
| lineHeight | 1.06, 1.20, 1.31, 1.50, 2.32 |
| letterSpacing | -0.0430em at 51px, -0.0240em at 25px, -0.0200em at 24px, -0.0130em at 15px |
| substitute | Inter |
| role | Supporting brand typeface for navigation, labels, and smaller details, reinforcing the primary Linksans aesthetic with slightly tighter tracking on larger sizes. |

### yerk

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.5 |
| role | yerk — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 |  |
| body-sm | 14 |  | 1.5 | 0.1 |
| body | 16 |  | 1.5 |  |
| subheading | 24 |  | 1.3 |  |
| heading-sm | 28 |  | 1.2 |  |
| heading | 56 |  | 1.07 | -0.56 |
| display | 80 |  | 1 | -1.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 64px |
| inputs | 8px |
| buttons | 99px |
| pillForms | 99px |
| accentBlobs | 1000px |

- **elementGap** — 10px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 1504px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Marble | `#f3f3f1` | 0 | Base page background or canvas. |
| Lunar Dust | `#e8efd6` | 1 | Slightly elevated background surface for sections or larger content blocks. |
| White | `#ffffff` | 2 | Card and input field backgrounds, primary interactive surfaces. |
| Tinted Overlay | `#00000052` | 3 | Elevated content cards, creating a sense of depth without traditional shadows. |

## Components

### Primary Action Button

**Role:** Main call-to-action button, highly visible.

Background: Chartreuse Pop (#d2e823). Text: Graphite (#1e2330). Border Radius: 99px. Padding: 20px vertical, 26px horizontal. Font: Linksans Linksansvf, 16px, weight 500.

### Navigation Link Button

**Role:** Transparent button for navigation items.

Background: transparent. Text: Black (#000000). No border. No explicit padding, implied by content.

### Input Field

**Role:** Standard input for user data entry.

Background: White (#ffffff). Text: Gray Text (#757575). Border: 1px solid Black (#000000). Border Radius: 8px. Padding: 8px vertical, 16px horizontal.

### Tertiary Ghost Button

**Role:** Alternative action button with minimal visual hierarchy.

Background: Marble (#f3f3f1). Text: Graphite (#1e2330). Border: 1px solid Graphite (#1e2330). Border Radius: 8px. Padding: 22px vertical, 26px horizontal.

### Pill Accent Button

**Role:** Decorative button with high curvature for specific actions.

Background: Lavender Mist (#e9c0e9). Text: Black (#000000). Border: 1px solid Black (#000000). Border Radius: 99px. Padding: 20px vertical, 26px horizontal.

### Default Card

**Role:** Standard container for content.

Background: transparent. Border Radius: 0px. No shadow. Padding: 0px.

### Elevated Content Card

**Role:** Card with subtle elevation and rounded corners.

Background: rgba(0, 0, 0, 0.32). Border Radius: 32px. No explicit shadow, the background tint provides a sense of depth.

### Feature Highlight Card

**Role:** Cards designed to stand out with bold colors and maximal rounding.

Background: Goldenrod (#d6a337), Iris Blue (#061492), Forest Green (#254f1a), or Lavender Mist (#e9c0e9). Border Radius: 64px or 1000px. No shadow.

## Layout

The page maintains a centered, contained layout with a maximum width of 1504px. Heroes often feature full-width color backgrounds (e.g., Hydrangea Blue or Chartreuse Pop) with centrally aligned headline text. Sections are distinct, often using alternating vibrant color blocks to break vertical rhythm rather than subtle dividers. Content is arranged in alternating text-left/image-right patterns or centered stacks for feature descriptions. Card grids are prevalent, showcasing features or examples within the highly rounded feature highlight cards. The navigation is a sticky top bar with prominent 'Log in' and 'Sign up free' buttons, maintaining a tight, accessible structure.

## Imagery

Linktree's imagery palette is a mix of product-in-use photography and abstract, organic shapes. Photography features diverse individuals or product shots, often with a vibrant, playful filter or color overlay, integrated into color-blocked sections rather than being full-bleed. Illustrations are minimal, generally functional icons or simple, bold shapes rather than complex scenes. The emphasis is on product showcase and community representation, with a high density of visual elements carefully choreographed within the colorful blocks. Icons are typically filled and monochromatic in either black or white, varying based on background contrast.

## Dos & Donts

### Do

- Prioritize Chartreuse Pop (#d2e823) for all primary calls to action, ensuring it is the brightest element on the screen.
- Utilize 99px border-radius generously for all buttons and rounded interactive elements, for a soft, friendly feel.
- Employ the full spectrum of brand colors (Hydrangea Blue, Dahlia Purple, Currant Red, etc.) for distinct section backgrounds and decorative panels.
- Maintain high visual contrast for text elements, preferring Graphite (#1e2330) on light backgrounds and White (#ffffff) on saturated or dark backgrounds.
- Apply Linksans Linksansvf or Linksans for all headings and key UI text, adjusting letter-spacing according to the type scale for subtle refinement.
- Use Marble (#f3f3f1) as the default page canvas, creating a consistent light foundation.
- Structure layouts with a 1504px max-width, centering content horizontally for coherence.

### Don't

- Avoid using flat, desaturated background colors for major sections; Linktree's identity relies on vibrant, distinct color blocks.
- Do not use sharp 0px corners for interactive elements or containers; leverage the specified border radii to maintain the organic, friendly aesthetic.
- Refrain from introducing system default shadows; Linktree uses tinted backgrounds and color blocks to create visual depth instead of drop shadows.
- Do not rely solely on neutral colors for visual hierarchy; use brand colors as primary indicators for interaction and distinction.
- Do not use generic, default system fonts for headings or prominent text; always use Linksans or Linksans Linksansvf to uphold brand typography.
- Avoid tight element spacing; ensure at least 10px element gaps and 16px card padding for a comfortable density.
- Do not apply excessive letter spacing to body text; maintain normal tracking for readability, using negative tracking points only for display headings.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #000000
- background: #f3f3f1
- border: #000000
- accent: #2665d6
- primary action: #d2e823 (filled action)

Example Component Prompts:
- Create a Primary Action Button: #d2e823 background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Create a feature card: Forest Green (#254f1a) background, 64px border radius. Title 'Unlimited Links' in Linksans, 24px, weight 700, #ffffff. Description 'Add as many links as you need, no limits.' in Arial, 16px, weight 400, #ffffff. Padding: 16px.
- Create an input field: White (#ffffff) background, 1px solid Black (#000000) border, 8px border radius. Placeholder text 'Your Linktree username' in Text Gray (#757575), 16px. Padding: 8px vertical, 16px horizontal.
- Create a navigation link: Black (#000000) text (Arial, 16px, weight 400). No background or border. On hover, change text color to Hydrangea Blue (#2665d6).

---
_Source: https://styles.refero.design/style/b04c379c-7c33-4d69-8882-8d85d7768654_
