# Udemy — Design Reference

> Ordered campus noticeboard.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.udemy.com](https://www.udemy.com) |
| Refero page | [https://styles.refero.design/style/c03afcbd-96ed-4b7f-8d0a-277fc0042ba7](https://styles.refero.design/style/c03afcbd-96ed-4b7f-8d0a-277fc0042ba7) |
| Theme | light |
| Industry | other |

## Overview

Udemy's design system portrays a confident, organized learning platform. It uses a predominantly light theme with stark, dark gray typography for high contrast, ensuring readability for educational content. The visual signature comes from the judicious use of a vibrant violet accent for interaction and brand elements, creating moments of activation against the otherwise neutral canvas. Components are structured and purposeful, featuring soft-cornered cards, pill-shaped buttons, and a clear visual hierarchy that guides the user through course content.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Background | `#e9eaf2` | neutral | Page backgrounds and secondary surface fills, providing a subtle off-white base |
| Surface White | `#ffffff` | neutral | Primary card backgrounds, prominent content blocks, and interactive element fills |
| Deep Graphite | `#2a2b3f` | neutral | Primary text, headings, icons, and strong borders – the core dark color for content |
| Dark Overlay | `#202230` | neutral | Background for elevated content sections, often paired with light text for contrast |
| Dusty Blue | `#b7b9cd` | neutral | Muted text, subheadings, and secondary icons, providing a softer alternative to Deep Graphite |
| Medium Gray | `#9194ac` | neutral | Hairline borders, dividers, and ghost element outlines |
| Muted Indigo | `#3d4055` | neutral | Background for subtle elevated cards or content groupings |
| Steel Gray | `#595c73` | neutral | Descriptive text and card body text |
| Subtle Dark Background | `#33364a` | neutral | Background for large content sections, creating visual segmentation |
| Regal Violet | `#6d28d2` | accent | Interactive elements, primary links, active states, and decorative brand accents. Creates a sense of focus and action |
| Success Orange | `#c4710d` | accent | Orange accent for outlined action borders, linked labels, and lightweight interactive emphasis. Use as a supporting accent, not as a status color |
| Subtle Violet Link | `#c0c4fc` | accent | Light mode links or secondary interactive text, providing a less assertive interactive cue |
| Skeleton Shine | `#d1d2e0` | neutral | Border color for placeholder loading states |

## Typography

### Udemy Sans

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| weights | 300, 400, 500, 700 |
| sizes | 12px, 14px, 16px, 18px, 24px, 32px |
| lineHeight | 1.10, 1.20, 1.40, 1.50, 1.60 |
| substitute | system-ui |
| role | The primary typeface for all text content, from headings to body text. Its subtle variations in weight and size provide clear hierarchy while maintaining a consistent brand voice. Weights 300 (light) and 400 (regular) are commonly used for body copy and UI elements, while 500 (medium) and 700 (bold) are reserved for headings and emphasized text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.6 |  |
| body-sm | 14 |  | 1.6 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.5 |  |
| heading | 24 |  | 1.4 |  |
| display | 32 |  | 1.2 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 16px |
| cards | 8px |
| inputs | 4px |
| buttons | 1000px |
| general | 8px |

- **elementGap** — 16px
- **sectionGap** — 24px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Background | `#e9eaf2` | 0 | Base page background |
| Surface White | `#ffffff` | 1 | Primary content areas, cards |
| Muted Indigo | `#3d4055` | 2 | Elevated cards or distinct content groupings within dark sections |
| Dark Overlay | `#202230` | 3 | Sections with strong visual separation, often with reversed text color |

## Components

### Solid Button

**Role:** Primary action button, often for categories or filters.

Background: Canvas Background (#e9eaf2), Text/Border: Regal Violet (#6d28d2). Rounded corners at 16px. Padding 16px all sides. Emphasizes active selection or categorisation.

### Pill Button

**Role:** Filter or tag button, indicating selectable options without high emphasis.

Background: Surface White (#ffffff), Text/Border: Deep Graphite (#2a2b3f). Fully rounded corners at 1000px radius. Minimal vertical padding and 10px horizontal padding. Promotes a compact, selectable field.

### Ghost Button (Text Primary)

**Role:** Secondary action or link-style button, often within cards or content areas.

Background: transparent, Text/Border: Success Orange (#c4710d). Rounded corners at 8px. Minimal padding at 12px horizontal. Used for less prominent actions, drawing attention via color accent.

### Ghost Button (Text Accent)

**Role:** Subtle interactive element, typically used for navigation or in-context links.

Background: transparent, Text/Border: Regal Violet (#6d28d2). Rounded corners at 8px. Minimal padding at 10px horizontal. Visually lightweight, emphasizing navigation through text color.

### Quote Card

**Role:** Testimonial or review card.

Background: Surface White (#ffffff). Rounded corners at 8px. Shadow: none. Padding: 16px all sides. Creates a clean, contained space for text-heavy content.

### Topic Card (Neutral Background)

**Role:** Category or topic display card in a light section.

Background: Canvas Background (#e9eaf2). Rounded corners at 8px. Shadow: none. No padding by default. Provides a subtle visual grouping without strong borders.

### Topic Card (Dark Background)

**Role:** Category or topic display card in a dark section.

Background: Muted Indigo (#3d4055). Rounded corners at 16px. Shadow: none. No padding by default. Used for contrast within darker sections, creating distinct content blocks.

### Search Input

**Role:** Primary search field.

Background: transparent, Text/Border: Deep Graphite (#2a2b3f). Rounded corners at 4px. Placeholder text in Deep Graphite. Padding 4px left. Designed for clear, functional input.

## Layout

The page layout is primarily section-based, featuring a max-width contained grid for most content, though the exact max-width is not strictly fixed but generally spacious. The hero section is often a full-bleed dark block with a prominent, centered headline and descriptive text. Subsequent sections alternate between Canvas Background (#e9eaf2) and Surface White (#ffffff) or occasionally Dark Overlay (#202230). Content is typically arranged in 2 or 3-column card grids, or alternating text-left / image-right patterns. Vertical spacing between sections is generous, around 24px, creating a comfortable rhythm. Navigation is handled by a sticky top bar with a search input.

## Imagery

The visual language on Udemy combines realistic 3D rendered illustrations with clean product-focused imagery. Illustrations are geometric and dimensional, often depicting abstract concepts related to learning and technology in energetic compositions with brand colors. Product imagery consists of tight crops of objects or devices, presented without lifestyle context and sometimes featuring duotone effects. Icons are simple, outlined, and monochromatic, primarily in Deep Graphite, serving as functional UI elements rather than decoration. The overall density is balanced, with imagery often confined to hero sections or distinct content blocks, supporting the text-heavy educational nature of the site.

## Dos & Donts

### Do

- Use Deep Graphite (#2a2b3f) for primary text and main headings to ensure high contrast and readability on light backgrounds.
- Apply Regal Violet (#6d28d2) for all interactive link text and primary UI element accents, always maintaining a consistent visual cue for action.
- Round corners of most cards and content blocks to 8px for a soft and approachable aesthetic.
- Prioritize Canvas Background (#e9eaf2) for general page backgrounds and Surface White (#ffffff) for elevated or interactive card surfaces.
- Implement pill-shaped buttons (1000px radius) for filter and tag elements, paired with Deep Graphite text on Surface White.
- Utilize a 16px element gap for consistent vertical rhythm between distinct UI components such as cards and text blocks.
- Apply elevation shadows of oklch(0.6295 0.0204 306.5 / 0.08) 0px 2px 8px 0px, oklch(0.6295 0.0204 306.5 / 0.12) 0px 4px 16px 0px only for truly elevated or interactive elements, like focused buttons.

### Don't

- Avoid using highly saturated colors for large background areas; maintain the integrity of the neutral canvas.
- Do not deviate from the established typography hierarchy; avoid using 'Udemy Sans' at unapproved sizes or weights for headings or body copy.
- Refrain from introducing custom shadows or border radii. All elevation and shape properties must adhere to the defined tokens.
- Do not use black or pure white for text or backgrounds unless explicitly specified; opt for the richer neutral shades like Deep Graphite or Canvas Background.
- Avoid excessive use of borders; let surface color and subtle spacing define boundaries between content sections.
- Do not create new interaction colors; Regal Violet is the primary interactive accent, and Success Orange is reserved for semantic feedback.
- Avoid decorative imagery that competes with the UI; visuals should support the content without overwhelming it.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #2a2b3f 
background: #e9eaf2 
border: #9194ac 
accent: #6d28d2 
primary action: #6d28d2 (outlined action border)

Example Component Prompts:
Create a Hero Section with a dark background: Dark Overlay (#202230) background. Headline 'Learn essential career and life skills' at 32px Udemy Sans weight 700, Surface White (#ffffff). Body text 'Udemy helps you build in-demand skills fast and advance your career' at 16px Udemy Sans weight 400, Dusty Blue (#b7b9cd). 

Create a Testimonial Card: Surface White (#ffffff) background, 8px radius. Body text 'The course did a great job explaining AI...' at 16px Udemy Sans weight 400, Deep Graphite (#2a2b3f). Muted text for author 'Cro M.' at 14px Udemy Sans weight 400, Dusty Blue (#b7b9cd). Link 'View AI courses' in Regal Violet (#6d28d2) at 14px Udemy Sans weight 500. 

Create a Category Filter Button: Solid Button style. Text 'Generative AI' in Regal Violet (#6d28d2) at 16px Udemy Sans weight 500. Background Canvas Background (#e9eaf2), 16px radius, with a 1px Regal Violet border. 

Create a Secondary Information Card in a dark section: Muted Indigo (#3d4055) background, 16px radius. Heading 'CompTIA' at 24px Udemy Sans weight 700, Surface White (#ffffff). Body text 'Cloud, Networking, Cybersecurity' at 14px Udemy Sans weight 400, Dusty Blue (#b7b9cd). No padding by default.

Create an Icon Button for pagination: transparent background, icon outline and fill in Regal Violet (#6d28d2). Circle shape with 1000px radius. Padding 0px.

---
_Source: https://styles.refero.design/style/c03afcbd-96ed-4b7f-8d0a-277fc0042ba7_
