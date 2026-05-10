# Specify — Design Reference

> Subdued white canvas, violet sparks

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://specifyapp.com](https://specifyapp.com) |
| Refero page | [https://styles.refero.design/style/fa8ecd97-5052-4909-a4b4-50419ad9d00a](https://styles.refero.design/style/fa8ecd97-5052-4909-a4b4-50419ad9d00a) |
| Theme | light |
| Industry | devtools |

## Overview

Specify employs a light-themed interface with subtle gray surfaces that provide depth without harsh contrast. Prominent, controlled typography, primarily in a dark charcoal, guides the user, punctuated by a single vibrant violet accent color that denotes interactive elements and brand presence. Components are gently rounded and softly shadowed, creating a friendly yet precise tool-like aesthetic for a design token engine.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, primary text on dark backgrounds |
| Charcoal Text | `#000000` | neutral | Primary text, prominent borders, icons |
| Carbon Surface | `#1a1d1e` | neutral | Secondary dark surfaces, subtle dark buttons, dark section backgrounds |
| Ash Gray | `#f6f7f9` | neutral | Section backgrounds, subtle input fields, utility components |
| Cloud Gray | `#ebedef` | neutral | Muted section backgrounds, subtle dividers |
| Medium Gray | `#8d8e8f` | neutral | Secondary text, helper text, borders for secondary elements |
| Graphite | `#5f6162` | neutral | Muted text, less prominent dividers |
| Subtle Dark Background | `#151718` | neutral | Hero section background, dark mode surfaces when switching themes or for specific components |
| Specify Violet | `#624de3` | brand | Violet outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Highlight Violet | `#8d4af7` | accent | Accent for links and decorative elements, often in gradients; Decorative background gradient, typically in hero sections or feature blocks |
| Highlight Blue | `#1d58c0` | accent | Accent for links and decorative elements; Decorative background gradient, typically in hero sections or feature blocks |
| Highlight Green | `#009639` | accent | Accent for links and decorative elements; Decorative background gradient, typically in hero sections or feature blocks |
| Linear Gradient Violet | `#6e56cf` | accent | Decorative background gradient for banner elements, adds a subtle highlight |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### Inter

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400, 500, 600, 700 |
| sizes | 8px, 10px, 12px, 14px, 16px, 20px, 24px, 32px, 48px, 64px |
| lineHeight | 1.00, 1.13, 1.17, 1.20, 1.25, 1.50, 1.60, 1.71, 2.00, 2.40, 3.00 |
| substitute | system-ui, sans-serif |
| role | Primary typeface for all UI text, headings, and body copy. Its functional yet friendly geometry anchors the interface. Weights 600 and 700 are used for emphasized elements and larger headings respectively, while 400 and 500 cover general text and subheadings. |

### Fira Code

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.57 |
| substitute | monospace |
| role | Monospace typeface exclusively for code snippets, token values, or technical labels where consistent character width is key. |

### Inter-Medium

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 8px, 12px, 14px, 16px |
| lineHeight | 1.5, 1.71, 2, 3 |
| role | Inter-Medium — detected in extracted data but not described by AI |

### Inter-SemiBold

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 8px, 12px, 14px, 16px, 20px, 24px, 32px |
| lineHeight | 1, 1.2, 1.25, 1.5, 1.6, 1.71, 2, 3 |
| letterSpacing | -0.031 |
| role | Inter-SemiBold — detected in extracted data but not described by AI |

### Inter-Bold

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 48px, 64px |
| lineHeight | 1.13, 1.17 |
| letterSpacing | -0.021, -0.016 |
| role | Inter-Bold — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-lg | 14 |  | 1.71 |  |
| heading-sm | 20 |  | 2.4 |  |
| heading | 24 |  | 1.25 | -0.62 |
| heading-lg | 32 |  | 1.2 | -0.99 |
| display | 48 |  | 1.17 | -0.76 |
| display-lg | 64 |  | 1.13 | -1.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| small | 6px |
| buttons | 6px |
| default | 6px |
| circular | 100px |
| pillButtons | 40px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 16px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | 0 | Primary page background and base surface. |
| Ash Gray | `#f6f7f9` | 1 | Secondary background for sections and content blocks, providing subtle visual separation. |
| Standard Card | `#ffffff` | 2 | Elevated card surfaces with shadows, used for distinct content containers. |
| Subtle Dark Background | `#151718` | 3 | Hero sections or banners, creating a strong visual break and contrast. |

## Components

### Primary Filled Button

**Role:** Main call to action button.

Background: Charcoal Text (#000000), Text: White Canvas (#ffffff), Padding: 12px vertical, 20px horizontal. Radius: 6px.

### Ghost Button (dark text)

**Role:** Secondary action button for less prominent interactions.

Background: transparent, Text: Charcoal Text (#000000), Border: 1px solid Charcoal Text (#000000), Padding: 10px vertical, 5px horizontal. Radius: 0px.

### Pill Button (Dark Background)

**Role:** Specialized button for small actions or tags.

Background: Subtle Dark Background (#151718), Text: Specify Violet (#624de3), Padding: 4px vertical, 16px horizontal. Radius: 6px.

### Standard Card

**Role:** Information container, feature display.

Background: White Canvas (#ffffff), Padding: 16px all sides. Radius: 16px. Shadow: Medium elevation.

### Ghost Card

**Role:** Decorative or less prominent information display, often on dark backgrounds.

Background: rgba(255, 255, 255, 0.2), Padding: 24px vertical, 40px horizontal. Radius: 16px. No shadow.

### Testimonial Card

**Role:** Displaying quotes or user feedback

Background: Cloud Gray (#ebedef), Radius: 8px, No padding detected.

### Header Navigation Link

**Role:** Primary navigation element in the header.

Text: Medium Gray (#8d8e8f). On hover, text becomes Specify Violet (#624de3).

## Layout

The page primarily uses a max-width contained layout of approximately 1200px, centered horizontally. The hero section often breaks this containment, exhibiting a full-bleed dark background with a centered headline and abstract graphics. Section rhythm alternates between full-bleed dark sections and contained light sections. Content arrangement typically follows a left-aligned, almost magazine-like flow in lighter sections, with alternating text-left/visual-right patterns, and feature grids. Darker sections often feature centered content stacks. An 8px base unit is used for padding and gaps, creating a compact density, but overall sections have significant vertical spacing (40px) providing ample breathing room. The navigation is a sticky top bar.

## Imagery

This design system uses a mix of three types of visual elements: detailed product illustrations and abstract 3D renders for showcasing the product's technical nature, minimal line icons for UI clarity, and occasional partner logos. The product illustrations feature a dark, atmospheric background with glowing, geometrically structured elements (cubes, lines, circuitry) that visually represent data flow and modularity. These are not full-bleed but contained within discrete sections. Icons are line-based (outlined style), monochromatic, and used functionally. Photography is absent, placing full emphasis on UI and conceptual graphics. Imagery density is moderate, used strategically to break up text-dominant sections and explain concepts.

## Dos & Donts

### Do

- Use Charcoal Text (#000000) for all primary body and heading text for maximum contrast on light backgrounds.
- Apply Specify Violet (#624de3) exclusively for interactive elements like links, buttons, and active states to guide user interaction.
- Implement Standard Cards with White Canvas (#ffffff) background, 16px radius, and the medium elevation shadow for displaying content blocks.
- Maintain a clear visual hierarchy using the Inter font family with judicious application of weights: 600 or 700 for headings, 500 for subheadings, and 400 for body text.
- Utilize Ash Gray (#f6f7f9) for section backgrounds to break up White Canvas (#ffffff) areas cleanly.
- Apply 6px border-radius as the default for small interactive elements such as buttons and tags.
- Ensure consistent 8px element gaps to maintain a compact yet breathable density between components.

### Don't

- Do not use Charcoal Text (#000000) on dark backgrounds where White Canvas (#ffffff) should be used for readability.
- Avoid using multiple vibrant accent colors; stick to Specify Violet (#624de3) as the dominant brand accent.
- Do not introduce sharp corners; maintain the overall soft and approachable feel with prescribed border radii.
- Do not use shadows on Ghost Cards or elements intended to appear flat against a background.
- Avoid arbitrary changes in line-height; adhere to the typographic scale's defined values to ensure vertical rhythm.
- Do not use Fira Code (monospace) for general UI text; it is reserved for code-like content only.
- Do not create new background gradients; use the provided Hero Gradient variations for decorative impacts.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- text: #000000
- background: #ffffff
- border: #000000
- accent: #624de3
- primary action: #1a1d1e (filled action)

**3-5 Example Component Prompts:**
- Create a section with an Ash Gray background: #f6f7f9. Inside, add a Standard Card: background #ffffff, 16px padding, 16px radius, shadow rgba(0, 0, 0, 0.08) 0px 1px 2px 0px, rgba(0, 0, 0, 0.06) 0px 4px 8px 0px.
- Create a Ghost Button: transparent background, text Charcoal Text (#000000), 10px vertical and 5px horizontal padding, 0px radius, 1px solid Charcoal Text (#000000) border.
- Create a Hero section: Subtle Dark Background (#151718), a centered Inter display-lg heading at 64px, weight 700, #ffffff, letter-spacing -1.02px. Below it, add an Inter body-lg text at 14px, weight 400, #8d8e8f.
- Create a Primary Action Button: #1a1d1e background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

---
_Source: https://styles.refero.design/style/fa8ecd97-5052-4909-a4b4-50419ad9d00a_
