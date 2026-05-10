# Branding — Design Reference

> Midnight atelier of digital craftsmanship

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.svz.io](https://www.svz.io) |
| Refero page | [https://styles.refero.design/style/4d4772a3-e1da-415f-a6d7-658dcefdcecd](https://styles.refero.design/style/4d4772a3-e1da-415f-a6d7-658dcefdcecd) |
| Theme | dark |
| Industry | agency |

## Overview

SVZ orchestrates a dramatic, high-contrast dark mode aesthetic, building 'digital worlds' with deep blacks and stark whites. Typography embraces expressive, bespoke typefaces with tight tracking, creating an impactful, almost architectural text presence. The system employs subtle textural overlays and an unexpected, vibrant red accent that pierces through the monochrome, signaling energy and action.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | neutral | Page backgrounds, deepest surface layers, primary text on light backgrounds — creates a theatrical black canvas |
| Deep Space | `#080808` | neutral | Default interactive backgrounds, hover states on dark surfaces, secondary backgrounds — provides subtle relief from pure black |
| Nightfall Gray | `#171617` | neutral | Footer backgrounds, deeper UI elements contrasting with Deep Space |
| Charcoal Surface | `#262525` | neutral | Elevated card backgrounds, distinct content blocks |
| Slate Highlight | `#393939` | neutral | Subtle inset shadow on cards, minor UI accents |
| Cloud White | `#fcfcfc` | neutral | Primary text, prominent borders, high-contrast UI elements — essential for readability on dark backgrounds |
| Glacial White | `#f3efef` | neutral | Secondary text, muted text elements, alternative default text color |
| Ash Accent | `#d4d2d2` | neutral | Muted icon fills, tertiary text, subtle borders |
| Dim Gray | `#b5b2b2` | neutral | Helper text, legal text, disabled states |
| Steel Border | `#525252` | neutral | Fine line dividers, subtle visual separation for navigation elements |
| Inferno Red | `#fe1e34` | brand | Primary action backgrounds, brand accents, interactive highlights — a single, vivid color creating urgency and focus; Subtle background overlay for decorative hero sections or impactful visual elements, transitioning from red to transparent |
| Vivid Crimson | `#ff0000` | accent | Decorative highlights, large-scale heading accents, graphic elements |

## Typography

### Kmr Waldenburg

| Key | Value |
| --- | --- |
| weight | 300, 400, 700 |
| sizes | 10px, 12px, 14px, 24px, 32px, 42px, 64px, 80px, 160px |
| lineHeight | 0.90, 1.00, 1.05, 1.10, 1.20, 1.50 |
| letterSpacing | -0.0800em, -0.0750em, -0.0500em, -0.0470em, -0.0380em, -0.0370em, -0.0170em, 0.0710em, 0.0800em, 0.3080em |
| substitute | Montserrat |
| role | Primary typeface for headings, navigation, and key textual elements. Its variable weights and tight letter-spacing contribute to a deliberate, crafted feel, demanding attention without screaming. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 12px, 14px |
| lineHeight | 1.20, 1.50 |
| letterSpacing | normal |
| substitute | Inter |
| role | System font used for utilitarian body text, icons, and supporting information, providing clarity and legibility without competing with the custom display fonts. |

### Editorialnew

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 14px, 20px, 32px |
| lineHeight | 1.00, 1.10, 1.50 |
| letterSpacing | -0.0500em, -0.0200em |
| substitute | Playfair Display |
| role | Supplementary display font for select body text and headings, offering a refined, slightly more traditional counterpoint, often with negative tracking to maintain a sleek density. |

### Dirtyline 36 Daysoftype 2022

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 24px, 64px, 80px |
| lineHeight | 0.90, 1.05, 1.10 |
| letterSpacing | -0.1000em, -0.0370em, -0.0300em |
| substitute | Oswald |
| role | Distinctive display font reserved for hero headlines and impactful, artistic typography, characterized by extremely tight tracking and condensed forms for a bold, almost sculptural presence. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0.308 |
| body | 14 |  | 1.5 | -0.02 |
| subheading | 24 |  | 1.2 |  |
| heading-sm | 32 |  | 1.1 | -0.047 |
| heading | 42 |  | 1.1 | -0.037 |
| heading-lg | 64 |  | 1.05 | -0.075 |
| display | 80 |  | 0.9 | -0.08 |
| editorial-display | 160 |  | 0.9 | -0.08 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| buttons | 8px |
| default | 3px |
| interactive | 3px |
| texturedCard | 14.4px |

- **elementGap** — 24px
- **sectionGap** — 48px
- **cardPadding** — 12px
- **pageMaxWidth** — 1440px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | 0 | Dominant page background, deepest stage for content. |
| Deep Space | `#080808` | 1 | Default interactive backgrounds, subtle base for cards and content sections. |
| Nightfall Gray | `#171617` | 2 | Footer background, slightly elevated content blocks. |
| Charcoal Surface | `#262525` | 3 | Elevated card backgrounds requiring distinct separation. |

## Components

### Primary Action Button

**Role:** Filled button for critical calls to action.

Filled with Inferno Red (#fe1e34), text in Cloud White (#fcfcfc), 8px border-radius, 32px padding on all sides. Tight letter-spacing for Kmr Waldenburg font.

### Secondary Ghost Button

**Role:** Outlined button for secondary actions, blending into dark contexts.

Transparent background, 1px Cloud White (#fcfcfc) border, text in Cloud White (#fcfcfc), 8px border-radius, 32px padding on all sides.

### Textured Card

**Role:** Card with subtle background texture and inset shadow.

Background color rgba(0, 0, 0, 0.2), 14.4px border-radius, with an rgba(255, 255, 255, 0.2) 0px 2px 5px 0px inset box-shadow. 11.52px padding on all sides.

### Feature Content Card

**Role:** Solid background card for feature blocks.

Background Charcoal Surface (#373535), 8px border-radius, no shadow. 56px vertical padding, 32px horizontal padding.

### Navigation Link

**Role:** Interactive text link within navigation menus.

Text in Glacial White (#f3efef), 3px border-radius for interactive states, no explicit padding, tight Kmr Waldenburg typography.

## Layout

The page primarily uses a max-width contained layout, likely around 1440px, centered on a deep black canvas. Hero sections frequently feature full-bleed black backgrounds with large, centered, expressive typography and abstract graphics or stylized 3D elements. Vertical rhythm is established through consistent section gaps, often alternating between solid dark backgrounds and sections with subtle textural overlays. Content is typically arranged in centered stacks or alternating left/right text-image blocks where visuals are highly integrated. Card grids are present for features. A sticky top navigation bar with minimal elements and a prominent 'Discovery Call' button maintains constant access.

## Imagery

The visual language focuses on abstract, geometric shapes and bold typography. When photography or 3D renders are present (like the furry 'V' letter), they are highly stylized and often isolated against deep black backgrounds, serving as artistic statements rather than informative imagery. Images are full-bleed within sections, and occasionally masked or integrated with text. Iconography, when visible, is outlined and monochromatic, aligning with the stark aesthetic. The density is image-heavy in hero sections, but otherwise text-dominant with key visuals used sparingly for impact and atmosphere.

## Dos & Donts

### Do

- Prioritize Kmr Waldenburg for all headings and navigational elements, applying negative letter-spacing for visual impact (e.g., -0.0800em at 80px, -0.0750em at 64px, -0.0380em at 42px).
- Maintain a stark contrast using Absolute Zero (#000000) for backgrounds and Cloud White (#fcfcfc) or Glacial White (#f3efef) for text.
- Use Inferno Red (#fe1e34) exclusively for primary calls to action or brand-specific interactive elements, limiting its usage to maintain its visual power.
- Apply 8px border-radius to all buttons and prominent cards, and 14.4px specifically for Textured Cards, to introduce a subtle softness to the otherwise hard-edged aesthetic.
- Employ consistent padding: 32px for button padding, 12px for card inner padding, and 24px for general element spacing.
- Incorporate the rgba(255, 255, 255, 0.2) 0px 2px 5px 0px inset shadow for Textured Cards to add depth while integrating into the dark theme.
- Utilize Dirtyline 36 Daysoftype 2022 for expressive, oversized hero text with extreme negative tracking (e.g., -0.1000em at 80px) to signify artistic branding.

### Don't

- Avoid using bright or pastel colors; the palette is strictly monochrome with one primary red accent.
- Do not use generic system fonts for display text or navigational elements; always use the specified custom typefaces.
- Refrain from applying soft, external box-shadows; elevation is achieved through subtle inset shadows and variations in background darkness or textural overlays.
- Do not deviate from the aggressive letter-spacing defined for headings and prominent text; loose tracking will diminish the established aesthetic.
- Do not use the Inferno Red (#fe1e34) for decorative purposes or as a general UI color for elements other than primary actions; its impact relies on scarcity.
- Avoid using large blocks of pure white as backgrounds; the primary canvas is Absolute Zero (#000000).
- Do not use multiple contrasting accent colors; the visual system relies on the singular punch of Inferno Red.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #fcfcfc
background: #000000
border: #fcfcfc
accent: #fe1e34
primary action: #fe1e34 (filled action)

Example Component Prompts:
1. Create a Hero Headline: Text 'DIGITAL WORLDS' in Dirtyline 36 Daysoftype 2022, 80px, weight 400, #fcfcfc, letter-spacing -0.1000em, line-height 0.9. Positioned centrally on an Absolute Zero (#000000) background.
2. Design a Primary Action Button: 'DISCOVERY CALL' label in Kmr Waldenburg, 14px, #fcfcfc, weight 400. Button background Inferno Red (#fe1e34), 8px border-radius, 32px padding all sides.
3. Implement a Feature Content Card: Charcoal Surface (#262525) background, 8px border-radius. Headline (Kmr Waldenburg, 32px, #fcfcfc, weight 700, line-height 1.1). Body text (system-ui, 14px, #f3efef, normal tracking). Padding 56px vertical, 32px horizontal.
4. Build a global navigation link: Text 'AGENCY' in Kmr Waldenburg, 12px, #fcfcfc, weight 400, letter-spacing 0.3080em. No background, transparent border.

---
_Source: https://styles.refero.design/style/4d4772a3-e1da-415f-a6d7-658dcefdcecd_
