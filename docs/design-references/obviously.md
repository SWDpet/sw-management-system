# Obviously — Design Reference

> Whiteboard command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.obviously.ai](https://www.obviously.ai) |
| Refero page | [https://styles.refero.design/style/c21fd0f0-1375-4094-83e7-0de484940100](https://styles.refero.design/style/c21fd0f0-1375-4094-83e7-0de484940100) |
| Theme | light |
| Industry | ai |

## Overview

Zams employs a clean, purposeful aesthetic built on a stark white canvas punctuated by vivid purple accents. Typography is compact and precise, driving information flow without visual clutter. Interactive elements use a subtle hover animation and a distinct purple to guide user focus, while components remain lightweight, prioritizing function over heavy adornment. The system balances a professional, data-driven feel with approachable, almost playful graphics.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button backgrounds |
| Ink Black | `#000000` | neutral | Primary text, icon fills, button text for ghost buttons |
| Graphite | `#272727` | neutral | Headings, strong text, footer backgrounds |
| Dark Wolf | `#5d5d5d` | neutral | Secondary text, subtle borders, placeholder text |
| Slate Gray | `#4f4f4f` | neutral | Muted text, navigation links |
| Silver Mist | `#e0e0e0` | neutral | Hairline separators, default button borders |
| Fog White | `#f6f6f6` | neutral | Subtle section backgrounds, alternate card backgrounds |
| Muted Stone | `#a69ea7` | neutral | Tertiary text, less prominent borders |
| Regal Violet | `#7451f2` | brand | Primary action buttons, active states, brand accent |
| Amethyst Border | `#5952a1` | brand | Border for primary action buttons |
| Azure Blue | `#0072c6` | semantic | Info badges, link accents |
| Sky Tint | `#d6e5ff` | semantic | Background for informational badges |

## Typography

### DM Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 9px, 12px, 14px, 15px, 16px |
| lineHeight | 1.00, 1.10, 1.20, 1.40, 1.60 |
| letterSpacing | -0.0200em at 16px, 0.0200em at 14px |
| substitute | system-ui |
| role | Body copy, subheadings, navigation links, button text, and badges. Its versatility makes it the workhorse for most UI elements, maintaining a compact yet readable appearance. |

### Martian Mono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 11px, 16px |
| lineHeight | 1.00, 1.10, 1.20, 1.27, 1.60 |
| letterSpacing | 0.0130em at 16px, 0.0220em at 11px |
| fontFeatureSettings | 'ss01' on, 'tnum' |
| substitute | SFMono-Regular, Consolas, Liberation Mono, Menlo, monospace |
| role | Specific, technical information, code snippets, or areas requiring precise, monospaced alignment. Its distinct character provides visual contrast for functional elements. |

### Lustria

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 20px, 32px, 36px, 42px, 52px |
| lineHeight | 1.10, 1.20 |
| letterSpacing | -0.0500em at 52px, -0.0480em at 42px, -0.0190em at 20px |
| fontFeatureSettings | 'swsh' 2 |
| substitute | serif |
| role | Bold headlines, high-impact titles, and prominent calls to attention. The varied letter-spacing creates a distinctive, almost editorial feel for main content blocks. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| legalDisclaimer | 11 |  | 1.27 | 0.24 |
| body-sm | 14 |  | 1.6 | 0.28 |
| heading-sm | 20 |  | 1.2 | -0.38 |
| heading | 32 |  | 1.1 | -1.54 |
| heading-lg | 36 |  | 1.1 | -1.73 |
| display | 52 |  | 1.1 | -2.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| badges | 100px |
| buttons | 4px |
| navItems | 4px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 1296px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Canvas | `#ffffff` | 0 | Primary page background, default content area |
| Subtle Section | `#f6f6f6` | 1 | Alternate background for distinct content sections |
| Info Card | `#d6e5ff` | 2 | Background for informational badges or temporary alerts |
| Elevated Control | `#e0e0e0` | 3 | Background for subtle controls or interactive elements |
| Dominant Action | `#7451f2` | 4 | Background for primary calls to action |

## Components

### Primary Action Button

**Role:** Call to action

Filled button with Regal Violet background (#7451f2), Canvas White text, and a slightly darker Amethyst Border (#5952a1). Rounded with 4px radius. Padding is 8px vertical, 16px horizontal.

### Secondary Ghost Button

**Role:** Alternative action

Ghost button with Canvas White background, Graphite text (#272727), and Silver Mist border (#e0e0e0). Rounded with 4px radius. Padding is 8px vertical, 16px horizontal. Shadow on hover rgba(0, 0, 0, 0.2) 0px 2px 4px 0px.

### Navigation Link Button

**Role:** Tertiary action with minimal styling

Text-only button with Slate Gray text (#4f4f4f), 0px radius, acting as an outlined link with a Silver Mist border (#e0e0e0) only visible on the bottom. Generous padding of 16px all around.

### Integration Grid Item

**Role:** Interactive logo display

Square item with Canvas White background, subtle Silver Mist border on all sides. Content (logo) is centered. Implicit 4px radius. Uses 16px padding on all sides for the inner elements.

### Basic Info Badge

**Role:** Informational label

Small badge with Sky Tint background (#d6e5ff), Azure Blue text (#0072c6), and 100px border radius for a pill shape. Tight 2px vertical, 6px horizontal padding.

### Text-only Badge

**Role:** Subtle categorization

Transparent background, Dark Wolf text (#5d5d5d), 0px radius. No padding, designed for inline labeling.

### Feature Card

**Role:** Content container for features

Canvas White background, subtle border of Silver Mist (#e0e0e0) on all sides. Internal padding is 16px. Implicit 4px radius.

### Stacked Footer Link

**Role:** Standard footer navigation

Link with Dark Wolf text (#5d5d5d) on a Graphite background (#272727). No border or radius, uses 8px padding vertically.

## Layout

The page adheres to a max-width 1296px contained layout, centered on a Canvas White background. The hero section is full-width, featuring a prominent centered headline and a large product screenshot set against the white canvas. Sections alternate between full-width and narrower content blocks, often employing a 2-column layout for text and imagery or an integration grid. Vertical spacing between sections is generous and consistent at 64px, contributing to a comfortable, uncrowded feel. The navigation is a sticky top bar, minimal and uncluttered, with Brand Black text links and a Regal Violet Primary Action Button on the far right.

## Imagery

The imagery features highly stylized, almost playful 3D illustrations of anthropomorphic geometric shapes with friendly faces. These are used primarily as decorative elements to provide character and visual warmth to otherwise sparse sections. Product screenshots of the UI are clean, flat, and contained within white panels, often showcasing internal tool interfaces with clear data presentation. Icons are predominantly filled, simple, and monochrome, with a few instances of multi-color brand icons for integrations. The overall density of imagery is balanced, with large, impactful illustrations contrasting clear, informative UI screenshots, avoiding photography for a more abstract, brand-controlled visual identity.

## Dos & Donts

### Do

- Always use Regal Violet (#7451f2) for primary calls to action, ensuring it remains the dominant interactive color.
- Maintain high contrast text: use Ink Black (#000000) or Graphite (#272727) on Canvas White (#ffffff) backgrounds for all body and heading copy.
- Implement a 4px `radius` for all interactive buttons and navigation items to convey a consistent soft crispness.
- Utilize `elementGap` of 8px for consistent spacing between distinct UI elements within a component or layout block.
- Ensure headings use the Lustria font with negative letter-spacing (-0.0500em to -0.0190em) to create a distinctive, impactful presentation.
- Leverage Fog White (#f6f6f6) and Canvas White (#ffffff) for subtle background variations between sections, creating visual rhythm without hard divisions.
- Apply `rgba(0, 0, 0, 0.2) 0px 2px 4px 0px` shadow exclusively to ghost buttons on hover for a minimal elevation effect.

### Don't

- Avoid using chromatic colors for large background areas; maintain a dominant Canvas White (#ffffff) or Fog White (#f6f6f6) base.
- Do not use Lustria for body text or small labels; reserve it for headlines to preserve its unique impact and legibility.
- Do not introduce new border radii beyond 4px for buttons/cards and 100px for badges; consistency is key.
- Do not use dark backgrounds without explicit consideration for text contrast, primarily limiting dark areas to the footer and ensuring text is Canvas White (#ffffff).
- Avoid arbitrary letter-spacing on DM Sans; use only the defined -0.0200em for specific elements and 0.0200em for others.
- Do not use any color other than Regal Violet (#7451f2) for primary filled buttons; this is the brand's main interactive color.
- Do not create heavy, ornate components; components should generally be lightweight with minimal borders and subtle shadows if any.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #000000
- background: #ffffff
- border: #e0e0e0
- accent: #7451f2
- primary action: #7451f2 (filled action)

Example Component Prompts:
- Create a Primary Action Button: #7451f2 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Design a feature card: Canvas White background, Silver Mist border. Headline 'Meeting Intelligence that makes every rep close like your top 10%' using Lustria weight 400 at 36px, Graphite text, letter-spacing -1.73px. Body text 'Best reps know their leads...' using DM Sans weight 400 at 15px, Dark Wolf text, letter-spacing -0.3px. Add an Azure Blue Info Badge 'COMING SOON' where appropriate.
- Create an integration grid: Fog White section background. Each grid item is a plain Canvas White square card with an elementGap 8px separating them. Use Muted Stone (#a69ea7) for less prominent border lines, if any.

---
_Source: https://styles.refero.design/style/c21fd0f0-1375-4094-83e7-0de484940100_
