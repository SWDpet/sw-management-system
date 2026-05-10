# Neon — Design Reference

> Server Room After Dark. A deep black environment where data and interactions are the only sources of light.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://neon.tech](https://neon.tech) |
| Refero page | [https://styles.refero.design/style/cc38369a-41e3-4bcd-b619-230ccffe7e8e](https://styles.refero.design/style/cc38369a-41e3-4bcd-b619-230ccffe7e8e) |
| Theme | dark |
| Industry | devtools |

## Overview

The design feels like a high-end server room after dark — a pure black void where information glows. A strict monochrome palette of pure black (#000000) and white (#ffffff) creates maximum contrast, ensuring text and UI are starkly legible. All visual energy comes from a single, electric green (#34d59a) that mimics terminal output and data visualizations, used exclusively for accents and decorative, code-like background graphics. The system achieves depth not with shadows but with subtle, layered near-black surfaces. A unique tension exists between the pill-shaped buttons and the sharp, 4px corners of all other UI containers.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Neon Glow | `#34d59a` | brand | Key brand accent, active state indicators, data visualizations — injects a vibrant, code-like energy. |
| Neon Muted | `#285d49` | brand | Subtle background tones in visualizations, less prominent brand elements. |
| Scanline Fade | `#39a57d` | brand | Special effect for highlighting code or UI elements, mimicking a terminal scanline. |
| System Warning | `#ff3621` | accent | Used sparingly for icons or highlights requiring urgent attention. |
| Whiteout | `#ffffff` | neutral | Primary text, primary CTA button backgrounds, icons. |
| Ash | `#797d86` | neutral | Secondary text, descriptive copy, inactive navigation links. |
| Pewter | `#94979` | neutral | Tertiary text, metadata, placeholder text. |
| Cloud | `#c9cbcf` | neutral | Hover states on dark elements, subtle highlights. |
| Graphite Light | `#303236` | neutral | Borders, dividers, subtle UI structure. |
| Graphite | `#242628` | neutral | Secondary surfaces floating on the background. |
| Graphite Deep | `#151617` | neutral | Card backgrounds, code block surfaces. |
| Depth | `#0a0a0b` | neutral | The darkest surface color before pure black, for subtle elevation. |
| Blackout | `#000000` | neutral | The absolute page background. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 10px, 12px, 13px, 14px, 15px, 16px, 18px, 20px, 24px, 28px, 32px, 40px, 44px, 48px, 60px, 80px |
| lineHeight | 1.00, 1.13, 1.25, 1.38, 1.50 |
| letterSpacing | Tight negative tracking on all display and heading sizes (-3.2px at 80px, -1.2px at 48px), becoming normal at body copy sizes. |
| substitute | Inter |
| role | Headlines and primary marketing copy. Its clean, neutral geometry provides high readability, contrasting with the more stylized monospaced font. |

### GeistMono

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 12px, 14px, 16px, 18px, 20px |
| lineHeight | 1.00, 1.13, 1.38, 1.50, 1.65 |
| letterSpacing | Slight negative tracking enhances density in UI contexts (-0.7px at 14px, -0.43px at 16px). |
| substitute | Fira Code, Source Code Pro |
| role | Code snippets, UI labels, and data displays. Its monospaced form adds a technical, typewriter-like precision, reinforcing the developer-centric identity. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.7 |
| body-sm | 14 |  | 1.5 | -0.7 |
| body | 16 |  | 1.5 | -0.43 |
| subheading | 18 |  | 1.38 | -0.36 |
| heading-sm | 24 |  | 1.25 | -0.24 |
| heading | 32 |  | 1.25 | -0.64 |
| heading-lg | 48 |  | 1.13 | -1.2 |
| display | 80 |  | 1 | -3.2 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 4px |
| inputs | 4px |
| buttons | 9999px |
| containers | 4px |

- **elementGap** — 8-16px
- **sectionGap** — 96-128px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Components

### CTA Button Group

### Terminal Code Block

### Feature Navigation List

### Primary Pill Button

**Role:** The main call-to-action, e.g., 'Get started', 'Sign up'.

A pill-shaped button with a Whiteout (#ffffff) background and Graphite Deep (#151617) text. Uses Inter font. Padding is H: 28px, V: 12px. Radius is 9999px.

### Ghost Pill Button

**Role:** Secondary actions, e.g., 'Read the docs', 'Log in'.

A pill-shaped button with a transparent background, Whiteout (#ffffff) text, and a 1px solid border in Graphite Light (#303236). Uses Inter font. Padding is H: 18px, V: 12px. Radius is 9999px.

### Feature List Item

**Role:** Bulleted items in feature sections.

Whiteout (#ffffff) text using Inter. Preceded by a small dot or icon colored with Neon Glow (#34d59a).

### Navigation Link

**Role:** Links in the main site header.

Text in Ash (#797d86) using Inter font. On hover or active state, text becomes Whiteout (#ffffff).

### Tag Badge

**Role:** Small informational tags, like 'A DATABRICKS COMPANY'.

Small, all-caps text using GeistMono in Ash (#797d86) or a similar gray. Often preceded by a Neon Glow (#34d59a) icon or symbol.

### Announcement Bar

**Role:** A persistent top bar for site-wide announcements.

Full-width bar with a Blackout (#000000) background. Text uses Inter font in a legible color like Whiteout (#ffffff) or Neon Glow (#34d59a).

### Logo Bar

**Role:** A section displaying logos of partner or client companies.

A row of logos rendered in a monochrome Ash (#797d86) or Pewter (#94979e) color on a Blackout (#000000) background.

## Layout

The page structure is full-bleed black, creating an immersive, infinite canvas. A centered headline over an abstract data-viz graphic defines the hero. Below the hero, content is organized within a centered max-width container (approx. 1200px), creating focus. Sections flow seamlessly into one another without visual dividers, relying on generous vertical spacing (96-128px) to create rhythm. Content is arranged in simple, symmetrical layouts: centered stacks for headlines, two-column grids for feature lists, and multi-column grids for logos. A sticky header provides persistent navigation.

## Imagery

Visuals are exclusively abstract, generative graphics resembling data streams, server activity, or glitch art. Composed of thin vertical lines in Neon Glow (#34d59a) and other muted tones, they serve as atmospheric backdrops rather than informational content. Product visuals are limited to stylized screenshots of terminal windows and code blocks, treated as UI components. Photography and traditional illustrations are absent. This text-and-abstract-graphic approach creates a purely digital, code-native environment.

## Elevation philosophy

Elevation is achieved through layered, near-black surfaces, not traditional box-shadows. Surfaces like Graphite Deep (#151617) float on the pure Blackout (#000000) background, creating depth through contrast without relying on blurs. This reinforces a flat, digital-native aesthetic.

## Dos & Donts

### Do

- Use pure Blackout (#000000) for all main section backgrounds.
- Reserve Neon Glow (#34d59a) for interactive highlights, data visualizations, and small decorative accents only.
- Employ the Whiteout (#ffffff) pill button for all primary calls-to-action.
- Use GeistMono for all code snippets, terminal simulations, and compact UI labels.
- Apply tight negative letter-spacing (-1.2px or more) to all headlines 48px and larger.
- Achieve depth by layering near-black surfaces (e.g., #151617 on #000000), not with box-shadows.
- Maintain a strict dichotomy of shapes: 9999px radius for buttons, 4px for all other containers.

### Don't

- Don't use gradients or background colors on main page sections.
- Don't use traditional box-shadows for elevation.
- Don't use Neon Glow (#34d59a) for body copy or headlines.
- Don't use saturated colors other than the primary brand green and the occasional red alert accent.
- Don't mix Inter and GeistMono within the same sentence or headline.
- Don't use rounded corners larger than 4px on cards, code blocks, or input fields.
- Don't create buttons that aren't pill-shaped.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Page Background:** Blackout (`#000000`)
- **Primary Text:** Whiteout (`#ffffff`)
- **Secondary Text:** Ash (`#797d86`)
- **Accent / Highlight:** Neon Glow (`#34d59a`)
- **CTA Button:** Whiteout (`#ffffff`) background, Graphite Deep (`#151617`) text
- **Border / Divider:** Graphite Light (`#303236`)

### Example Component Prompts
1.  **Hero Section:** "Create a full-screen hero section with a `Blackout` #000000 background. Add a large display headline: text 'Fast Postgres Databases', font `Inter` 80px weight 500, color `Whiteout` #ffffff, line-height 1.0, and letter-spacing -3.2px. Below it, add a primary CTA button: 'Get started' in a `Whiteout` #ffffff pill with `Graphite Deep` #151617 text, 9999px radius, and 12px 28px padding."
2.  **Code Block:** "Design a terminal code block component. Use a `Graphite Deep` #151617 background with 4px rounded corners and 24px padding. The text inside should use the `GeistMono` font at 14px. Default text color is `Whiteout` #ffffff. Highlight specific keywords or outputs with `Neon Glow` #34d59a."
3.  **Feature Section:** "Create a two-column section on a `Blackout` #000000 background. In the left column, create a list of features with `Whiteout` #ffffff text and a `Neon Glow` #34d59a dot prefix. In the right column, add a heading 'Integrate with a single command' using `Inter` 48px, `Whiteout` #ffffff color, and -1.2px letter-spacing."

---
_Source: https://styles.refero.design/style/cc38369a-41e3-4bcd-b619-230ccffe7e8e_
