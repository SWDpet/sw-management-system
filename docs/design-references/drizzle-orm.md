# Drizzle ORM — Design Reference

> developer's spirited workbench

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://orm.drizzle.team](https://orm.drizzle.team) |
| Refero page | [https://styles.refero.design/style/972925c4-0caa-4dc2-9c00-798b5be0ad70](https://styles.refero.design/style/972925c4-0caa-4dc2-9c00-798b5be0ad70) |
| Theme | light |
| Industry | devtools |

## Overview

Drizzle ORM's design feels like an approachable developer workbench — functional and clear, with a dash of quirky personality. The dominant use of a system sans-serif for most text, coupled with the playful hand-drawn 'FirstTimeWriting' font for select headings and accents, creates a unique tension between serious engineering and whimsical creativity. Subtle gray tones provide structure and visual hierarchy, while thoughtful rounding on interactive elements hints at a user-friendly experience. Even the slight shadow on cards and inputs maintains a light, approachable feel rather than a stark, elevated presence.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Cloud White | `#ffffff` | neutral | Page backgrounds, card surfaces, form backgrounds. |
| Smoke Gray | `#e5e7eb` | neutral | Subtle borders, button outlines, inactive states to provide soft visual separation. |
| Steel Gray | `#f6f6f7` | neutral | Light background for interactive elements and secondary sections. |
| Ink Black | `#222222` | neutral | Primary text, prominent headings, strong calls to action. |
| Charcoal Text | `#444444` | neutral | Secondary text, body copy, less prominent labels. |
| Stone Text | `#909090` | neutral | Tertiary text, subtle labels, placeholder text, diagram annotations. |
| Deep Sea | `#282b3b` | brand | Interactive elements, buttons, selected states, and text for high contrast elements. |
| Sky Blue | `#006be6` | brand | Links, occasionally for highlight text, indicating interactive elements or important information. |
| Amethyst Accent | `#3e7ff0` | accent | Accent for stylized headings and occasional graphical elements, providing a playful but vibrant pop. |
| Action Green | `#4bb74b` | semantic | Success states, completion indicators, progress bars. |
| Alert Red | `#ef4444` | semantic | Error states, warnings, occasional accent in illustrations. |
| Slate Gray | `#334155` | neutral | Card borders, subtle separators for less emphasis than Ink Black. |

## Typography

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 10px, 11px, 12px, 14px, 16px, 18px, 20px, 32px, 40px |
| lineHeight | 1.00, 1.17, 1.20, 1.42, 1.50 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "rlig", "ss01" |
| substitute | system-ui, sans-serif |
| role | Primary text font for body copy, buttons, links, and all UI elements. Its clean, neutral presence ensures readability and professional utility. |

### FirstTimeWriting

| Key | Value |
| --- | --- |
| weight | 600, 700 |
| sizes | 14px, 16px, 20px, 30px |
| lineHeight | 1.50, 1.60 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "rlig", "ss01" |
| substitute | cursive, sans-serif |
| role | Decorative font used sparingly for unique headings and branding elements, providing a hand-drawn, playful contrast to the system sans-serif. It introduces visual whimsy. |

### ui-monospace

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 12px, 16px |
| lineHeight | 1.00, 1.50 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "rlig", "ss01" |
| substitute | monospace |
| role | Code snippets, technical details, and specific data displays where monospace alignment is crucial. It supports the technical nature of the product. |

### Menlo

| Key | Value |
| --- | --- |
| weight | 500, 700 |
| sizes | 10px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "rlig", "ss01" |
| substitute | monospace |
| role | Niche use for very small code or data displays, potentially within compact components. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1 |  |
| body | 14 |  | 1.5 |  |
| heading | 20 |  | 1.2 |  |
| heading-lg | 32 |  | 1.17 |  |
| display | 40 |  | 1.17 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| badges | 3px |
| images | 2px |
| inputs | 4px |
| buttons | 4px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### Quick Navigation Cards

### Release Progress & Stats Block

### Performance Benchmark Card

### Primary Action Button

**Role:** Calls to action across the site.

Solid Deep Sea (#282b3b) background with Cloud White (#ffffff) text. Padding: 16px vertical, 32px horizontal. Radius: 4px.

### Default Button

**Role:** Secondary actions, navigation links within content.

Background: rgba(241, 242, 243, 0.27). Text: Deep Sea (#282b3b). BorderTopColor: rgba(33, 39, 46, 0.12). Padding: 10px vertical, 20px horizontal. Radius: 3px.

### Ghost Button

**Role:** Navigation items with a subtle background.

Background: rgb(246, 246, 247). Text: Ink Black (#222222). No border or padding specified beyond zero, implying a text-like appearance with a hover state.

### Badge (Neutral background)

**Role:** Categorization, metadata tags.

Transparent background (rgba(0, 0, 0, 0)) with Ink Black (#222222) text. No explicit radius or padding in this variant.

### Badge (Subtle Fill)

**Role:** Highlighting key attributes or technologies.

Background: rgba(241, 242, 243, 0.27). Text: Ink Black (#222222). Padding: 2px vertical, 6px horizontal. Radius: 3px.

### Badge (Dark Gray Outline)

**Role:** Less prominent tags.

Transparent background. Text: Stone Text (#144, 144, 144). No explicit padding or radius.

### Badge (Light Gray Fill)

**Role:** Informational badges with reduced visual emphasis.

Background: #f0f0f0. Text: #888888. Padding: 3px vertical, 8px horizontal. Radius: 4px.

### Text Input

**Role:** User input fields.

Cloud White (#ffffff) background. Text: Ink Black (#222222). Border: Smoke Gray (#e5e7eb). Placeholder text is lighter gray. Padding: 0px vertical, 12px horizontal inside, 30px left. Radius: 4px. No explicit focus styles, relies on browser defaults or subtle outline.

### Informational Card

**Role:** Content segmentation and display of related data.

Cloud White background, slight shadow: rgba(0, 0, 0, 0.08) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 1px 2px 0px, rgba(0, 0, 0, 0.075) 0px 2px 4px 0px. Radius: 2px-8px depending on context.

### Navigation Link (Header)

**Role:** Primary site navigation.

Text: Deep Sea (#282b3b). Underlined on active state or hover. No background.

## Layout

The page primarily uses a max-width contained model, centered on the screen, creating a focused content area. The hero section breaks this with a full-bleed background, using a large, centered headline paired with the signature whimsical illustrations. Sections are defined by consistent vertical spacing, creating an organized rhythm. Content arrangement often employs a two-column layout, alternating between text on one side and a supporting visual (illustration or code snippet) on the other. A 3-column card grid is used for feature display. The density is spacious, providing ample breathing room around content blocks. Navigation is a sticky top bar, minimal and functional, featuring text links and a GitHub star count.

## Imagery

The site's visual language is a blend of clean UI and whimsical, hand-drawn vector illustrations. Imagery features stylized, anthropomorphic 'droplet' characters and abstract shapes, often colored with the playful violet and blue accents. These illustrations are generally contained, not full-bleed, and often interact with text elements. Product screenshots are minimal, focusing on code snippets or simplified interface examples, often presented within subtle gray containers with rounded corners, maintaining a clean, explanatory role. Icons are outlined, typically monochrome (Ink Black or Stone Text), and used functionally to enhance navigation or convey status effectively. The overall density leans towards text-dominant, with imagery serving a decorative and explanatory purpose rather than a primary content role.

## Dos & Donts

### Do

- Prioritize `ui-sans-serif` at weight 400 or 500 for all body and interface text to maintain clarity.
- Use Amethyst Accent (#3e7ff0) exclusively for stylized headings or playful visual emphasis, never for critical information or interactive states.
- Apply Deep Sea (#282b3b) for primary interactive elements and text requiring high contrast.
- Maintain a subtle visual hierarchy with Smoke Gray (#e5e7eb) for borders and backgrounds of secondary components.
- Ensure buttons utilize a `4px` radius for Primary Action Buttons and `3px` for Default and Subtle Buttons.
- Employ the 'FirstTimeWriting' font for headline elements where a relaxed, distinctive tone is desired, usually at `30px` or `20px`.

### Don't

- Avoid using FirstTimeWriting for long passages of text or critical UI elements like buttons and navigation, as its readability is limited.
- Do not introduce strong, saturated colors beyond the defined brand and accent palette; maintain the largely neutral, approachable tone.
- Refrain from heavy shadows or strong elevation; the design favors subtle separation with light outlines and minimal depth.
- Do not deviate from the established padding and radius values for interactive components; consistency is key for usability.
- Avoid using Sky Blue (#006be6) for non-interactive text elements, to preserve its role as a link indicator.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text Primary: #222222 (Ink Black)
- Background: #ffffff (Cloud White)
- CTA Background: #282b3b (Deep Sea)
- Border Subtle: #e5e7eb (Smoke Gray)
- Link/Accent: #006be6 (Sky Blue)

### 3-5 Example Component Prompts
1. Create a primary action button: text 'Get Started', background Deep Sea (#282b3b), text Cloud White (#ffffff), 4px border-radius, 16px 32px padding, font ui-sans-serif weight 400.
2. Create a feature card: Cloud White (#ffffff) background, 8px border-radius, box-shadow rgba(0, 0, 0, 0.08) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 1px 2px 0px, rgba(0, 0, 0, 0.075) 0px 2px 4px 0px. Internal content with 16px internal padding, title Ink Black (#222222) at 20px ui-sans-serif weight 600, body text Charcoal Text (#444444) at 14px ui-sans-serif weight 400.
3. Create a subtle badge: text Ink Black (#222222), background rgba(241, 242, 243, 0.27), 3px border-radius, 2px 6px padding, font ui-sans-serif weight 400 at 12px.
4. Generate a section title: 'Performance' in Ink Black (#222222) at 32px ui-sans-serif weight 600, centered. Below it, a descriptive subtitle in Charcoal Text (#444444) at 18px ui-sans-serif weight 400, centered.
5. Assemble a header navigation link: 'Documentation' as link text, Deep Sea (#282b3b) text color, 10px 20px padding, 3px border-radius, ui-sans-serif weight 400 at 16px. Background rgb(246, 246, 247).

---
_Source: https://styles.refero.design/style/972925c4-0caa-4dc2-9c00-798b5be0ad70_
