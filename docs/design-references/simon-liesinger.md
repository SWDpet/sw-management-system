# Simon Liesinger — Design Reference

> Architectural blueprint on warm grey

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.liesingers.com](https://www.liesingers.com) |
| Refero page | [https://styles.refero.design/style/a90dbcb6-e42c-4992-a83b-94879699dd4f](https://styles.refero.design/style/a90dbcb6-e42c-4992-a83b-94879699dd4f) |
| Theme | light |
| Industry | design |

## Overview

Simon Liesinger's design system evokes an architectural blueprint on a muted canvas, utilizing precise typography and a stark achromatic palette. It emphasizes clear visual boundaries and a spacious layout, highlighted by subtle hairline borders. A single vivid red accent is reserved exclusively for navigation cues, drawing attention without visual clutter. The overall effect is one of restrained precision, where information is carefully presented with minimal adornment.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Fog | `#f2f2f2` | neutral | Page backgrounds, subtle surface elevation, text for subtle distinctions |
| Midnight Graphite | `#181818` | neutral | Primary text, section backgrounds, strong surface accents |
| Deep Ink | `#000000` | neutral | Headlines, strong body text, primary borders |
| Muted Slate | `#6e6e6e` | neutral | Subtle borders, secondary text in specific contexts |
| Navigation Ember | `#fc523b` | accent | Orange outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |

## Typography

### Times

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 10px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | serif |
| role | Captions, annotations, and subtle metadata text. Its classic feel grounds the starkness elsewhere. |

### GaramondRg

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 25px, 29px, 36px |
| lineHeight | 0.86, 1.00, 1.22, 1.28 |
| letterSpacing | 0.016em |
| substitute | Garamond |
| role | Headlines and prominent body text. The slightly generous letter-spacing provides an open, refined feel typical of editorial design. |

### HelveticaRg

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 0.83, 1.20, 1.67 |
| letterSpacing | 0.017em |
| fontFeatureSettings | 'case' on, 'kern' on, 'ss04' on, 'ss07' on, 'tnum' on, 'zero' on |
| substitute | Helvetica Neue |
| role | Interactive links and compact body text. Specific font features enhance legibility for numerical and capitalized content, fitting for a portfolio site. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.2 |  |
| subheading | 25 |  | 1.28 | 0.4 |
| heading | 29 |  | 1.22 | 0.464 |
| display | 36 |  | 0.86 | 0.576 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| default | 0px |

- **elementGap** — 10-40px
- **sectionGap** — 160px
- **cardPadding** — 30px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Fog | `#f2f2f2` | 0 | Base page background, light sections, and default component surface. |
| Midnight Graphite | `#181818` | 1 | Footer background, distinct content blocks, and areas of higher visual prominence. |

## Components

### Navigation Link

**Role:** Primary site navigation links at the footer.

Each navigation item appears as `HelveticaRg` 12px, Midnight Graphite text, with an `↗` icon in Navigation Ember. The active state or interaction reveals a 1px solid Navigation Ember border below, while the inactive state has a 1px solid Midnight Graphite border. The navigation section itself has a Canvas Fog background and generous vertical padding.

### Contact Link

**Role:** Interactive contact information links.

Contact information like email and phone number are displayed in `HelveticaRg` 12px, Midnight Graphite text, with the `↗` icon also in Midnight Graphite, distinguished only by its underline on hover rather than an accent color or border.

### Section Divider Panel

**Role:** Separating content sections and categories.

Content is often presented within distinct panels defined by 1px solid Midnight Graphite borders, with a Canvas Fog background. These panels use padding of 30px on left/right and 10px or 12px vertical padding where content is listed. The effect is one of contained modules on the page.

### Work/Award List Item

**Role:** Displaying entries in lists of work or awards.

Each item within a list like 'services', 'awards', or 'partners' is rendered in `HelveticaRg` 12px Midnight Graphite, with a 1px solid Midnight Graphite border on its left edge and a 10-12px padding below. This creates a visually structured ledger effect.

## Layout

The page primarily uses a full-bleed, vertically stacking layout. The hero section features large, centered text elements over the Canvas Fog background, with abstract graphics. Subsequent sections alternate between full-width content panes and structured grid-like displays (e.g., service/award lists) using the defined border patterns. The footer is a full-width Midnight Graphite bar. The overall rhythm is spacious with significant section gaps and consistent padding for encased content, creating a well-organized, almost grid-paper feel.

## Imagery

The site uses abstract, red-lined wireframe 3D graphics (like the down arrow) as the primary visual element, serving as decorative atmosphere rather than conveying content. Icons, such as the '↗' for external links, are minimalistic, either line-drawn in text color or the accent color for navigation. There are no photographs or complex illustrations, contributing to a text-dominant, clean interface where graphics serve as geometric punctuation.

## Dos & Donts

### Do

- Use Deep Ink (#000000) for all headings to establish strong visual presence, with at least 30px left/right padding.
- Employ Canvas Fog (#f2f2f2) as the primary page background and for subtle, recessed content areas.
- Define clear content boundaries using 1px solid Midnight Graphite (#181818) borders, especially for section dividers and list elements.
- Reserve Navigation Ember (#fc523b) exclusively for the arrow icon within footer navigation links and their hover/active states.
- Maintain a spacious rhythm between content blocks by applying section gaps of 160px vertically.
- Apply `GaramondRg` with '0.016em' letter-spacing for all significant body text and subheadings to ensure a refined editorial aesthetic.
- Use `HelveticaRg` with its specific font features for all interactive links and compact informational text (e.g., contact info, small notes).

### Don't

- Do not use Navigation Ember (#fc523b) for any purpose other than navigation directional cues or their associated borders.
- Avoid using drop shadows or heavy elevation; prefer flat surfaces and hairline borders for visual separation.
- Do not introduce new typefaces; rely solely on Times, GaramondRg, and HelveticaRg.
- Refrain from using strong, solid background colors other than Midnight Graphite (#181818) or Canvas Fog (#f2f2f2) for structural elements.
- Avoid decorative imagery; stick to minimalist wireframe-style graphics or pure UI elements.
- Do not use highly saturated colors for text or backgrounds outside of the defined accent color for navigation.
- Do not introduce rounded corners; maintain sharp, crisp edges for all UI elements to uphold the architectural aesthetic.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #f2f2f2
border: #181818
accent: #fc523b
primary action: no distinct CTA color

Example Component Prompts:
1. Create a hero text section: Background #f2f2f2, 'Hello,' heading: GaramondRg 36px, #000000, 0.016em letter-spacing. Subheading 'I am Simon Liesinger…': GaramondRg 29px, #000000, 0.016em letter-spacing.
2. Design a footer navigation bar: Background #181818. Each link 'about' uses HelveticaRg 12px, #181818. The '↗' icon and bottom border for active links are #fc523b. Padding above and below navigation links is 25px and 29px respectively.
3. Implement a 'services' list item: Text 'Brand Design & Strategy' using HelveticaRg 12px, #000000, with a 1px solid #181818 left border and 12px bottom padding. This item should be contained within a panel with 30px horizontal padding.
4. Compose a paragraph: Text 'Design is ever evolving...' using GaramondRg 25px, #181818, 0.016em letter-spacing, with 40px margin bottom separating it from the next element.

---
_Source: https://styles.refero.design/style/a90dbcb6-e42c-4992-a83b-94879699dd4f_
