# Changelog — Design Reference

> Midnight command center behind frosted glass.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://linear.app/changelog](https://linear.app/changelog) |
| Refero page | [https://styles.refero.design/style/a5cc9b0f-d274-458a-b990-d18482b70838](https://styles.refero.design/style/a5cc9b0f-d274-458a-b990-d18482b70838) |
| Theme | dark |
| Industry | devtools |

## Overview

This design system evokes a 'midnight command center behind frosted glass,' achieving a focused, high-tech atmosphere through a dark achromatic palette and precise typographic choices. A subtly layered grayscale background creates depth without relying on heavy shadows, while crisp text and restrained accenting maintain readability. Signature anti-conventional headlines use weight 500 at larger sizes, conveying authority through subtle refinement rather than bold weight. The system relies on precise border treatments and a dominant 9999px radius for interactive elements, contrasting with 8px radius for cards, to define interaction points within the otherwise serious interface.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Black | `#08090a` | neutral | Page backgrounds, base surface for main content areas. |
| Surface Dark | `#141516` | neutral | Slightly elevated surfaces, button backgrounds in hover states, subtle distinctions from Canvas Black. |
| Line Graphite | `#34343a` | neutral | Subtle dividers, borders on interactive elements, visual separation within dark surfaces. |
| Deep Charcoal | `#1c1c1f` | neutral | Darker interactive elements, further elevated background layers for specific components. |
| Border Carbon | `#23252a` | neutral | Input borders, subtle outlines for inactive states, separating elements on dark backgrounds. |
| Border Ash | `#2d2e31` | neutral | Border color for certain interactive components, providing a slightly lighter outline than Carbon. |
| Text Primary | `#f7f8f8` | neutral | Primary text, headings, icons, ensuring high contrast against dark backgrounds. Also used as background for ghost buttons. |
| Text Secondary | `#d0d6e0` | neutral | Secondary text, muted information, subtle accents, less prominent list items. Provides visual relief from primary text. |
| Text Muted | `#8a8f98` | neutral | Placeholder text, tertiary information, disabled states. Signifies lower importance or non-interactivity. |
| Highlight Fog | `#e4e5e9` | neutral | Light background highlight on interactive elements, subtle iconography against dark backgrounds. |
| Shadow Tint | `#3e3e44` | neutral | Border color for interactive elements and subtle shadow effects. Creates definition without heavy contrast. |

## Typography

### Inter Variable

| Key | Value |
| --- | --- |
| weight | 400, 500, 510, 590 |
| weights | 400, 500, 510, 590 |
| sizes | 12px, 13px, 14px, 15px, 16px, 17px, 24px, 32px, 48px |
| lineHeight | 1.00, 1.13, 1.20, 1.33, 1.40, 1.50, 1.60, 2.46, 2.67, 2.86 |
| letterSpacing | -0.01em |
| fontFeatureSettings | "cv01" on, "ss03" |
| substitute | Inter |
| role | Used for all primary UI text, body copy, headings, and interactive elements. Its variable nature allows fine-grained control over weight, with a specific emphasis on a mid-range weight (500) for headlines to convey modern, understated authority. The tight letter-spacing (-0.01em) maintains a compact, disciplined appearance. |

### Berkeley Mono

| Key | Value |
| --- | --- |
| weight | 400, 590 |
| weights | 400, 590 |
| sizes | 15px, 21px |
| lineHeight | 1.30 |
| letterSpacing | -0.014em |
| substitute | Space Mono |
| role | Exclusively for code blocks, timestamps, and technical notations. Its monospaced nature clearly differentiates technical content, and the slightly tighter letter-spacing hints at precision. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.4 | -0.12 |
| heading | 24 |  | 1.33 | -0.29 |
| heading-lg | 32 |  | 1.2 | -0.38 |
| display | 48 |  | 1.13 | -0.58 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| search | 100px |
| buttons | 9999px |
| interactive | 4.46px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Black | `#08090a` | 0 | Dominant page background, providing the base dark theme. |
| Surface Dark | `#1c1c1f` | 1 | Slightly elevated component backgrounds, such as the search input or interactive elements on hover. |
| Interactive Surface | `#141516` | 2 | Backgrounds for ghost buttons, tags, or other active interactive components, offering a minimal step up from the base canvas. |

## Components

### Text Link

**Role:** Interactive text, navigation items.

Text Primary #f7f8f8, Inter Variable weight 400. No underline by default; hover state adds underline or changes background.

### Ghost Button (Primary)

**Role:** Primary Call to Action, outlines interactiveness without filling.

Background transparent, text #f7f8f8 (Text Primary). Border 1px solid #f7f8f8. Border radius 9999px. Padding 0px top/bottom, 12px left/right for medium, 16px for large.

### Ghost Button (Secondary)

**Role:** Secondary actions that need less visual emphasis.

Background transparent, text #8a8f98 (Text Muted). Border 1px solid #8a8f98. Border radius 9999px. Padding 0px top/bottom, 12px left/right.

### Filled Button (Compact)

**Role:** Small, contained action buttons.

Background #141516 (Surface Dark), text #8a8f98 (Text Muted). Border 1px solid #23252a (Border Carbon). Border radius 100px. Padding 0px top/bottom, 16px left/right.

### Search Input

**Role:** Input elements for search functionality.

Background transparent. Placeholder text #8a8f98 (Text Muted). Border 1px solid #34343a (Line Graphite). Border radius 100px. Icon fill #8a8f98. Padding 0px vertical, 12px horizontal.

### Changelog Card

**Role:** Container for changelog entries.

Background transparent. Contents date in Berkeley Mono, title as Heading Small. Border 1px solid #34343a (Line Graphite) on inactive/hover. Border radius 8px. Inner padding around content section 16px.

### Navigation Tab

**Role:** Filtering or category navigation.

Text #f7f8f8 (Text Primary) for active, #8a8f98 (Text Muted) for inactive. Background transparent. Border radius 9999px. Padding 0px top/bottom, 12px left/right.

### Pill Tag

**Role:** Categorization or short labels.

Background #141516 (Surface Dark), text #f7f8f8 (Text Primary). Border radius 9999px. Padding 0px top/bottom, 12px left/right.

## Layout

The page maintains a max-width contained layout, with content centered within a defined vertical flow. The hero section is a full-bleed dark canvas with a left-aligned, prominent headline. Sections follow a consistent vertical rhythm, primarily using a text-heavy, single-column stack for changelog entries, occasionally broken by centered visual components like the grid of app icons. Navigation is provided by a sticky top bar with clearly segmented, low-prominence text links and a ghost button for primary action. The layout emphasizes clarity and direct information delivery, with ample vertical rhythm from the 24px section gap and compact element spacing.

## Imagery

The visual language is characterized by functional abstraction and product-focused graphics. It primarily uses icons and stylized, often monochrome, product screenshots or UI elements within dark, contained boxes. Icons are outlined, with a moderate stroke weight, and mostly monochromatic, occasionally using a subtle gradient. Imagery serves an explanatory role for product features or decorative atmosphere within the dark UI, always contained and never full-bleed. There's a high density of text-dominant content, punctuated by these visually precise, often button-like, graphics.

## Dos & Donts

### Do

- Prioritize Inter Variable font for all UI text, ensuring readability and consistency.
- Use Text Primary #f7f8f8 for all main text elements and headings on dark backgrounds to maintain strong contrast.
- Apply Canvas Black #08090a as the dominant background color across all pages.
- Use 9999px border radius for all actionable buttons and interactive pill components.
- Employ a 1px solid border using Line Graphite #34343a for subtle visual separation of components and containers, especially on hover.
- Leverage Berkeley Mono for all code snippets, timestamps, or technical text, keeping its distinct letter-spacing.
- Apply Surface Dark #141516 for subtle elevation for backgrounds of interactive components or hover states.

### Don't

- Avoid using multiple vibrant colors; restrict accent colors to functional elements if not present in the palette.
- Do not use heavy shadows for visual depth; rely on background color layering and subtle borders instead.
- Do not introduce new typefaces; the system is built on Inter Variable and Berkeley Mono.
- Avoid large hero imagery; prefer UI-focused elements or subtle graphic overlays.
- Do not use generic square corners; apply 8px radius to cards and containers for a softer, integrated feel.
- Do not use standard, bold font weights for large headlines; leverage Inter Variable weight 500-590 for a refined, modern approach.
- Avoid overly bright or pure white backgrounds; the system is designed for a dark interface.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #f7f8f8
- background: #08090a
- border: #34343a
- accent: no distinct accent color
- primary action: no distinct CTA color

### 3-5 Example Component Prompts
1. Create a changelog entry: Date in Berkeley Mono #d0d6e0, background transparent, border bottom 1px solid #34343a. Headline (weight 590, Inter Variable, size 24px, #f7f8f8, letter-spacing -0.29px). Body text (weight 400, Inter Variable, size 15px, #d0d6e0, letter-spacing -0.15px).
2. Create a ghost button: Text 'Log in' in Inter Variable weight 400, size 15px, #f7f8f8. Background transparent. Border 1px solid #f7f8f8. Radius 9999px. Padding 0px vertical, 12px horizontal.
3. Create a search input: Background transparent. Placeholder 'Search…' in Inter Variable weight 400, size 15px, #8a8f98. Icon (magnifying glass) fill #8a8f98. Border 1px solid #34343a. Radius 100px. Padding 0px vertical, 12px horizontal.
4. Create a pill tag: Text 'Fixes' in Inter Variable weight 400, size 13px, #f7f8f8. Background #141516. Radius 9999px. Padding 0px vertical, 12px horizontal.
5. Create a header navigation item: Text 'Customers' in Inter Variable weight 400, size 15px, #f7f8f8. Hover state changes text to #e4e5e9.

---
_Source: https://styles.refero.design/style/a5cc9b0f-d274-458a-b990-d18482b70838_
