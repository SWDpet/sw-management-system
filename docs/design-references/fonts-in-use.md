# Fonts In Use — Design Reference

> Typographic Archive on Vellum. A precise, ordered display of form and content, like a beautifully cataloged library of type specimens.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://fontsinuse.com](https://fontsinuse.com) |
| Refero page | [https://styles.refero.design/style/348c83bc-bed3-4562-841a-26a30ee19a9b](https://styles.refero.design/style/348c83bc-bed3-4562-841a-26a30ee19a9b) |
| Theme | light |
| Industry | design |

## Overview

This design system presents as an academic archive: minimal, almost utilitarian, with a relentless focus on content. The stark monochrome palette of pure black and white emphasizes the typographic examples, with subtle gray surfaces providing structure without distraction. Custom fonts (BentonSansRE and RelayCond) lend a distinct, authoritative voice, reinforcing the site's role as a trusted resource. The tight spacing and modest 2px radius on interactive elements communicate efficiency and precision, prioritizing information delivery over decorative flourishes.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Inkwell | `#000000` | neutral | Primary text, headings, links, borders for content areas — establishes a strong, definitive presence against lighter backgrounds. |
| Canvas | `#ffffff` | neutral | Page background, input fields, and elements requiring high contrast for readability. |
| Mist | `#f0f0f0` | neutral | Subtle background for list items, section separators, creating soft visual differentiation without breaking the monochrome theme. |
| Pewter | `#dddddd` | neutral | Input borders, light dividers — a near-achromatic gray that provides soft definition. |
| Stone | `#cccccc` | neutral | Less prominent borders and inactive states. |
| Ash | `#999999` | neutral | Secondary text, placeholder text, disabled states on buttons — indicates less importance or interactivity. |
| Granite | `#b3b3b3` | neutral | Subtle box-shadows if applied, for a very restrained sense of elevation. |

## Typography

### Benton Sans RE

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 9px, 10px, 12px, 14px, 16px |
| lineHeight | 1.10, 1.20, 1.60 |
| letterSpacing | normal |
| substitute | Inter |
| role | The workhorse sans-serif for body text, links, headings, and navigation. Its condensed nature allows for dense information display, and the varying line heights indicate clear hierarchy in text blocks. |

### Relay Cond

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 18px, 36px |
| lineHeight | 1.00, 1.60 |
| letterSpacing | normal |
| substitute | Oswald |
| role | Used for prominent page titles, navigation, and badges, its condensed, strong presence commands attention with an architectural feel against the more ubiquitous Benton Sans RE. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.1 |  |
| body | 14 |  | 1.6 |  |
| body-lg | 16 |  | 1.6 |  |
| subheading | 18 |  | 1 |  |
| heading | 36 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| inputs | 2px |
| buttons | 2px |

- **elementGap** — 10px
- **sectionGap** — 30px
- **cardPadding** — 20px
- **pageMaxWidth** — 1180px

## Components

### Font Entry Cards Grid

### Search Bar with Filter Controls

### Sponsor Card

### Primary Action Button

**Role:** Interactive element

Solid Ash background (#999999), Canvas text (#ffffff), 2px border radius, with 5px vertical and 10px horizontal padding. Borders match the button itself.

### Default Input Field

**Role:** User input

Canvas background (#ffffff), Inkwell text (#000000), 2px border radius, with 0px vertical and 10px horizontal padding. Border is Pewter (#dddddd).

### Small Button

**Role:** Pagination/Navigation

Small, square buttons with a subtle 2px radius. Ash background (#999999), Canvas text (#ffffff), 5px vertical and 10px horizontal padding. Borders match button color.

### Staff Pick Badge

**Role:** Decorative/Informational Tag

Transparent background, Inkwell text (#000000), 0px border radius, 0px padding. Uses Relay Cond font at 18px and bold weight 700 to stand out.

## Layout

The page adheres to a max-width 1180px, centered layout. The hero section is minimal, primarily text-based with navigation. Content is presented in a dense, multi-column grid, featuring square or rectangular 'cards' each displaying a type example. This grid maintains consistent column gutters (20px column gap) and row spacing (30px marginBottom). Navigation is predominantly horizontal at the top, supplemented by a secondary filter bar. The overall rhythm emphasizes content density and visual cataloging, with sections divided by consistent vertical spacing rather than alternating backgrounds.

## Imagery

The site's visual language is dominated by the type specimens themselves, acting as the primary imagery. These are typically contained within a grid, often cropped, and showcasing diverse typographic applications. There are no lifestyle photos, illustrations, or 3D renders. Icons are minimal, usually simple arrows or abstract shapes for navigation. The focus is entirely on showcasing typography, and the images serve as direct examples, not decorative elements. They are displayed without additional masking or bespoke treatments, appearing to be raw, clear representations of their content.

## Dos & Donts

### Do

- Use Benton Sans RE for all body, paragraph, and standard heading text, varying weight between 400 and 700, and sizes between 10px and 16px to maintain a dense, readable content block.
- Apply Relay Cond exclusively for prominent headings, navigation items, and badges, utilizing its 18px and 36px sizes with weights 400 or 700 to create impact.
- Maintain a strict monochrome palette with Inkwell (#000000) for text, Canvas (#ffffff) for backgrounds, and Mist (#f0f0f0) for subtle section differentiation.
- Implement 2px border radii for all interactive elements like buttons and input fields to imply functionality without softness.
- Separate distinct content sections with a 30px vertical gap, fostering a structured yet compact layout.
- Use 5-10px element spacing to create visual separation between adjacent UI elements without appearing sparse.

### Don't

- Do not introduce chromatic colors outside the established monochrome and sponsor-driven accents for any UI elements; reserve color for content itself.
- Avoid large, rounded corners or fluid, organic shapes; maintain the precise, rectilinear aesthetic with maximum 2px radii.
- Do not use letter-spacing other than 'normal' for any text, as the custom fonts are designed for specific impact without alteration.
- Do not deviate from the established 5px vertical and 10px horizontal padding for buttons; consistency reinforces the utilitarian feel.
- Refrain from using prominent box-shadows or complex elevation patterns; subtle differentiation with gray backgrounds suffices for depth.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: Inkwell (#000000)
- Background: Canvas (#ffffff)
- Button: Ash (#999999)
- Border (input): Pewter (#dddddd)
- Subtle Background: Mist (#f0f0f0)

### 3-5 Example Component Prompts
1. Create a `Primary Action Button` with the label 'Submit': Ash background (#999999), Canvas text (#ffffff), 2px border radius, 5px vertical padding, 10px horizontal padding, Benton Sans RE font weight 400, size 14px.
2. Create an `Input Field` component with placeholder 'Search...': Canvas background (#ffffff), Inkwell text (#000000), Pewter border (#dddddd), 2px border radius, 0px vertical padding, 10px horizontal padding, Benton Sans RE font size 14px.
3. Generate a `Staff Pick Badge` with the text 'Staff Pick': Transparent background, Inkwell text (#000000), 0px border radius, 0px padding, Relay Cond font weight 700, size 18px.
4. Design a `Gallery Item Card` placeholder, 200px by 200px. Below it, add a title 'Example Typeface': Text Inkwell (#000000), Benton Sans RE weight 700, size 16px, line height 1.2. Below that, 'Designer Name': Text Inkwell (#000000), Benton Sans RE weight 400, size 14px, line height 1.6. Use `elementGap` of 5px between title and designer name.

---
_Source: https://styles.refero.design/style/348c83bc-bed3-4562-841a-26a30ee19a9b_
