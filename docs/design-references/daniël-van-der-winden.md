# Daniël van der Winden — Design Reference

> Ordered Editorial Ink

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.daniel.pizza](https://www.daniel.pizza) |
| Refero page | [https://styles.refero.design/style/e8eda526-d686-4e45-a60d-61b6503a8eda](https://styles.refero.design/style/e8eda526-d686-4e45-a60d-61b6503a8eda) |
| Theme | light |
| Industry | design |

## Overview

Daniël van der Winden's personal site embodies a focused, editorial aesthetic, reminiscent of a neatly organized journal or academic publication. It prioritizes clarity and directness through a largely monochrome palette, functional typography, and structured content presentation. Visual elements are sparse, with strong emphasis on text, creating an atmosphere of quiet intellectual authority. The design system is characterized by subtle greys, crisp lines, and a deliberate absence of vibrant color, allowing the content to take center stage.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Porcelain | `#e5e7eb` | neutral | Page backgrounds, surface base, subtle borders and dividers that define structure without visual weight |
| Ebony Text | `#111827` | neutral | Secondary body text, navigation labels, and subdued headings. Do not promote it to the primary CTA color |
| Graphite Text | `#374151` | neutral | Secondary text, descriptive paragraphs, list items, and subtle hero text, offering a softer reading experience than primary text |
| Midnight Ink | `#1a202c` | neutral | Background for primary call-to-action buttons, providing a deep, solid presence for key interactions |
| Jet Button | `#222222` | neutral | Background for secondary action buttons, providing a slightly lighter alternative to primary button backgrounds |
| Shadow Icon | `#000000` | neutral | Decorative icons and fills where pure black is used to create sharp, small accents |
| Slate Text | `#2a2a28` | neutral | Specific prominent headings and links, adding a slightly different shade to distinguish certain textual elements |
| Ash Text | `#676867` | neutral | Muted text in links and headings, serving as a lower-contrast visual for less emphasized information |
| Fog Border | `#c4c6c8` | neutral | Subtle borders and dividers, providing a slightly darker line than Canvas Porcelain for structural definition |
| Stone Text | `#7b7c7c` | neutral | Specific heading elements with a muted, understated tone |

## Typography

### Degular

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 22px, 24px, 28px, 32px, 40px |
| lineHeight | 1.25, 1.50 |
| letterSpacing | 0.0250em, 0.1000em |
| substitute | Inter |
| role | Primary grotesque sans-serif for headings, body text, and interactive elements. Its neutral yet confident presence provides the main voice of the content, with moderate tracking at larger sizes to maintain clarity. |

### Blanco

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px, 20px, 22px, 28px |
| lineHeight | 1.40, 1.50 |
| letterSpacing | normal |
| substitute | Lora |
| role | A serif font used for select headings, links, and specific long-form content. Its classic proportions provide an intellectual counterpoint to the sans-serif, signaling depth and traditional editorial quality. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.5 |  |
| heading | 22 |  | 1.25 |  |
| heading-lg | 24 |  | 1.25 |  |
| display-sm | 28 |  | 1.25 |  |
| display | 32 |  | 1.25 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 3px |
| other | 8px |
| buttons | 0px |
| navigation | 3px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 12px
- **pageMaxWidth** — 1600px

## Components

### Primary Filled Button

**Role:** Main call-to-action button, conveying prominence.

Background: Midnight Ink (#1a202c), Text: Canvas Porcelain (#e5e7eb), Padding: 8px vertical, 12px horizontal. Border radius: 0px. Font: Degular.

### Email Button

**Role:** Secondary action button for personal contacts, slightly softer than primary.

Background: Jet Button (#222222), Text: Canvas Porcelain (#ffffff), Padding: 8px vertical, 12px horizontal. Border radius: 4px. Font: Degular.

### Ghost Icon Button

**Role:** Subtle interactive button, often for icons or secondary actions, maintaining minimal visual footprint.

Background: transparent, Text: Ebony Text (#111827), Padding: 8px square. Border radius: 6px. Font: Degular.

### Navigation Link

**Role:** Interactive elements within the left-hand navigation, indicating selectable items.

Text: Ebony Text (#111827) for active, Graphite Text (#374151) for inactive. Underline on hover. Border radius: 3px for focused/active states.

### Text Input (Default)

**Role:** Standard input field for user data entry.

Background: Canvas Porcelain (#e5e7eb), Border: 1px solid Canvas Porcelain (#e5e7eb). Text: Ebony Text (#111827). Padding: likely 8-12px.

### Resume Entry Card

**Role:** Presenting individual job or project entries in a structured list.

No explicit card background, assumes Canvas Porcelain (#e5e7eb) as background. Text: Ebony Text (#111827) for roles, Graphite Text (#374151) for descriptions. Defined by vertical spacing.

### Image Wrapper

**Role:** Container for imagery, providing subtle shaping.

Border radius: 3px. No explicit border, image is contained directly.

## Layout

The page primarily uses a contained layout with a maximum width of 1600px, centering content within the Canvas Porcelain background. The hero section presents a combination of text and imagery, arranged as a split screen or with imagery serving as context for the text. Content sections are typically text-dominant, with a clear vertical flow and consistent spacing, avoiding busy or overlapping elements. A prominent left-hand navigation column remains fixed, echoing a desk-bound journal. Features and resume entries are presented in a clean, stacked list-like fashion, defined by strong typographic hierarchy and generous line spacing rather than visual containers. The density is comfortable, with ample breathing room around text blocks and between sections.

## Imagery

Imagery is used sparingly, primarily focusing on candid, slightly desaturated, or monochromatic photography and product shots, often contained within a 3px rounded border. The images serve to provide context or a personal touch rather than being decorative. There are no illustrations or elaborate graphics; the visual language is direct and non-intrusive. Icons are simple, outlined, and monochromatic (Shadow Icon or Ebony Text), acting as functional cues like external link indicators.

## Dos & Donts

### Do

- Prioritize text content: use Ebony Text (#111827) and Graphite Text (#374151) for all primary and secondary text, ensuring AAA contrast against Canvas Porcelain (#e5e7eb).
- Maintain a clear hierarchy with Degular for headings and interactive elements, and Blanco for specific editorial and long-form content.
- Utilize Canvas Porcelain (#e5e7eb) as the dominant background and base for all surfaces and subtly defining structural borders.
- Employ consistent vertical rhythm in content sections, defaulting to 27px for body paragraphs and 40px for list item separation.
- Apply 3px border radius to navigation items, links, and contained images for a subtle softening of edges, contrasting with the sharp 0px radius for primary buttons.
- Use Midnight Ink (#1a202c) exclusively for primary filled button backgrounds to signal key actions.
- Distribute elements with a base element gap of 8px, scaling up for larger section spacing where needed.

### Don't

- Avoid introducing vibrant or saturated colors; maintain the predominantly monochrome palette with subtle shifts in grey.
- Do not use heavy shadows or excessive elevation; the system relies on subtle borders and spacing for visual separation.
- Refrain from drastically altering font weights or letter spacing beyond the defined Degular and Blanco profiles, especially avoiding tight tracking on body text.
- Do not deviate from the strict 0px radius for primary filled buttons; this is a signature sharp detail.
- Avoid large, ornate, or busy graphical elements; visual communication should remain direct and content-focused.
- Do not use multiple background colors for successive sections; maintain a seamless flow or use Canvas Porcelain with subtle internal borders.
- Do not use generic system fonts; stick to Degular and Blanco to uphold the distinct brand voice.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #111827
background: #e5e7eb
border: #e5e7eb
accent: no distinct accent color
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
Create a navigation link for an active state: Text Ebony Text (#111827), Degular font weight 400, 3px border radius, no background.
Create a resume entry heading: Text Ebony Text (#111827), Degular font weight 500, size 22px, line height 1.25. Below it, a description: Text Graphite Text (#374151), Degular font weight 400, size 16px, line height 1.5.
Create a subtle interactive ghost button: Transparent background, Text Ebony Text (#111827), 8px square padding, 6px border radius, Degular font weight 400.
Create a content paragraph: Text Graphite Text (#374151), Degular font weight 400, size 16px, line height 1.5, with 27px bottom margin.

---
_Source: https://styles.refero.design/style/e8eda526-d686-4e45-a60d-61b6503a8eda_
