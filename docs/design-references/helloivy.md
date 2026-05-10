# Helloivy — Design Reference

> Architectural blueprint on creamy paper. Black lines precisely segment content on a soft background.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://helloivy.co](https://helloivy.co) |
| Refero page | [https://styles.refero.design/style/5af6b791-6cad-4497-9e94-ace28e4fbd51](https://styles.refero.design/style/5af6b791-6cad-4497-9e94-ace28e4fbd51) |
| Theme | light |
| Industry | productivity |

## Overview

Hello Ivy presents as an architectural blueprint rendered on thick, creamy paper, conveying both structure and a subtle warmth. Heavy, bold titles anchor content, while a high-contrast monochromatic palette creates stark visual hierarchy. The signature feature is the strong grid system defined by fine black lines, segmenting content into clear, distinct blocks, as if each piece of information is a precisely cut and placed element within a larger, deliberate composition. Sparse use of color and a focus on sharp edges for everything but interactive elements contributes to a feeling of focused utility and quiet authority.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Greige Canvas | `#faf6f0` | neutral | Page backgrounds, large content sections — provides a warm, inviting canvas for the high-contrast black elements. |
| Midnight Ink | `#000000` | neutral | Primary text, borders, icons, button backgrounds — the dominant accent, creating strong contrast and defining structure. |
| Soft Graphite | `#5e697f` | neutral | Secondary body text, subtle section headings — provides a softer alternative to Midnight Ink for less prominent textual elements, maintaining readability against Greige Canvas. |
| Pure White | `#ffffff` | neutral | Icon fills, occasional inverse text on dark backgrounds — used sparingly to create highlights and visual breaks. |

## Typography

### Unbounded

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 20px, 34px, 62px, 82px |
| lineHeight | 1.10, 1.20, 1.40 |
| letterSpacing | 0.01em, 0.02em |
| substitute | Montserrat, Poppins |
| role | Display headlines and section titles — its geometric, strong character provides a modern, impactful presence, especially when rendered in high weights and large sizes. The increased letter-spacing at larger sizes adds an airy, deliberate feel. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 12px, 14px, 16px, 18px, 24px, 27px, 32px |
| lineHeight | 1.20, 1.40, 1.50, 1.57, 1.60 |
| letterSpacing | -0.02em |
| substitute | Inter |
| role | Primary body text, subheadings, and UI labels — its legibility and modern geometric construction make it ideal for dense information. The consistent slight negative letter-spacing (-0.02em) across all sizes enhances text density without sacrificing readability. |

### system-ui (sans-serif fallback)

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | inherit |
| role | Small utility text, inline links, and discreet UI elements where Inter might be too strong — provides a lighter touch for less critical information or when a subtle system font is preferred. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 |  |
| body-sm | 14 |  | 1.5 | -0.28 |
| body | 16 |  | 1.5 | -0.32 |
| body-lg | 18 |  | 1.6 | -0.36 |
| heading-sm | 20 |  | 1.2 | 0.2 |
| heading | 24 |  | 1.4 | -0.48 |
| heading-lg | 34 |  | 1.2 | 0.34 |
| display | 82 |  | 1.1 | 1.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 8px |
| generic | 8px |
| pillButtons | 9999px |

- **elementGap** — 10px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Components

### CTA Button Group

### Feature Highlight Card

### Social Proof / Client Logo Grid

### Primary Navigation Link

**Role:** Main navigation items

Text uses Inter, 400 weight, 16px, Midnight Ink (#000000). No background or explicit padding, but positioned within clear horizontal spacing.

### Hero Headline

**Role:** Main page title

Unbounded font, 82px size, 1.10 line-height, 0.02em letter-spacing, Midnight Ink (#000000).

### Hero Body Text

**Role:** Introductory paragraph

Inter font, 18px size, 1.60 line-height, -0.02em letter-spacing, Soft Graphite (#5e697f).

### Feature Section Heading

**Role:** Section titles for feature blocks

Unbounded font, 20px size, 1.20 line-height, 0.01em letter-spacing, Midnight Ink (#000000). Often appears uppercase and centrally aligned.

### Footer 'Try it for Free' Button

**Role:** Persistent contact/CTA in footer

Solid Pill Button variant but with Pure White text color for consistency. Midnight Ink (#000000) background, 9999px border-radius. Positioned in a subtle fixed element.

## Layout

The layout is max-width contained at approximately 1200px, centered on a Greige Canvas background. The hero section features a centered, enormous headline over the background. Section rhythm is created through a strong, visible grid system using fine 1px Midnight Ink borders that divide the page into distinct, almost cell-like blocks. Content often occupies full-width blocks or alternates between two-column text+visual arrangements. A clear client logo grid demonstrates social proof. Navigation is a simple top bar, with a persistent fixed 'Try it for free' button at the bottom right. The overall density is comfortable, with ample whitespace creating breathing room within the strict grid.

## Imagery

The site uses minimal imagery, primarily focusing on clean UI mockups and desaturated client logos. Product screenshots are contained within distinct bordered sections, appearing like functional 'windows' into the application. Photography is absent. Client logos are grayscale and often subtly outlined, adhering to the monochromatic scheme. The visual language emphasizes product function over lifestyle or abstract art, acting mainly as explanatory content or social proof, contained and never full-bleed.

## Dos & Donts

### Do

- Always define layout sections with subtle 1px Midnight Ink (#000000) borders against Greige Canvas (#faf6f0) to create a structured, architectural feel.
- Use Unbounded (weight 400-500) for all display headlines, setting letter-spacing to 0.02em for large titles (82px) and 0.01em for smaller section titles (20-34px).
- Apply Inter (weight 400-700) with a consistent -0.02em letter-spacing for all body text, subheadings, and UI labels.
- Utilize Greige Canvas (#faf6f0) as the primary background color for all page sections and content blocks.
- Implement 9999px border-radius for primary action buttons (Solid Pill Buttons) and 8px border-radius for secondary interactive elements (Outline Buttons).
- Maintain high contrast by using Midnight Ink (#000000) for all primary text and interactive elements on Greige Canvas (#faf6f0) backgrounds.
- Isolate Pure White (#ffffff) for subtle highlights, such as icon fills or inverse text on dark button backgrounds.

### Don't

- Avoid using highly saturated, chromatic colors; the palette is strictly monochromatic with a single warm neutral base.
- Do not deviate from the established 1px Midnight Ink (#000000) border style for section dividers and containers.
- Refrain from using drop shadows or complex elevation; depth is created through borders and color contrast.
- Do not introduce rounded corners on non-interactive content blocks or imagery; maintain sharp, defined edges for structure.
- Avoid decorative whitespace or intricate patterns; layout should be clean, grid-based, and functional.
- Do not use generic system fonts for body or heading text where Inter or Unbounded are specified; adhere to the defined typography for brand consistency.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): #000000
- Background (Primary): #faf6f0
- CTA Button (Solid): #000000
- Border: #000000
- Text (Secondary): #5e697f

### 3-5 Example Component Prompts
1. **Create a hero section:** Set background to Greige Canvas (#faf6f0). Add a centered headline using Unbounded, 82px, weight 400, #000000, line-height 1.1, letter-spacing 1.64px. Below it, add a body paragraph using Inter, 18px, weight 400, #5e697f, line-height 1.6, letter-spacing -0.36px. Include a Solid Pill Button centered below the text: Midnight Ink (#000000) background, Pure White (#ffffff) text, 9999px radius, 15px 30px padding.
2. **Generate an Outline Button:** Text 'Log In' using Inter, 16px, weight 400, #000000, line-height 1.2. Set background transparent, border 1px solid #000000, border-radius 8px, padding 15px 6px.
3. **Design a Client Logo Grid item:** Create a square container with a 1px solid Midnight Ink (#000000) border. Within, place a desaturated, grayscale logo (e.g., opacity 0.6) centered. Background should be Greige Canvas (#faf6f0).
4. **Produce a Feature Sub-section:** Start with a 1px solid Midnight Ink (#000000) horizontal rule. Below, place a heading 'Improve visibility, stay in the loop.' using Unbounded, 20px, weight 400, #000000, line-height 1.2, letter-spacing 0.2px. Follow with a body paragraph 'Hello Ivy helps creative teams...' using Inter, 16px, weight 400, #5e697f, line-height 1.5, letter-spacing -0.32px.

---
_Source: https://styles.refero.design/style/5af6b791-6cad-4497-9e94-ace28e4fbd51_
