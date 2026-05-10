# Ditto — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.dittowords.com](https://www.dittowords.com) |
| Refero page | [https://styles.refero.design/style/2db41cd9-c898-4f59-b704-3042c0d87f45](https://styles.refero.design/style/2db41cd9-c898-4f59-b704-3042c0d87f45) |
| Theme | light |
| Industry | productivity |

## Overview

Ditto uses an 'architectural blueprint on white marble' visual language, combining a refined, nearly monochromatic canvas with precise geometric typography and a vibrant, playful accent color for highlights and interactive elements. Surfaces are clean and untextured, serving as a bright foundation for highly legible black text. Strategic splashes of saturated color are used for emphasis, badges, or outlined elements, creating an energetic yet organized feel. Components are lightweight and often feature large border-radii, giving a soft, approachable edge to the otherwise crisp design.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary text, deep backgrounds for contrast, primary filled buttons, strong borders for UI elements |
| Canvas White | `#f7f5f3` | neutral | Dominant page background, card surfaces, ghost button text |
| Graphite | `#6a6559` | neutral | Muted text, secondary navigation, subtle button borders and iconography |
| Warm Gray | `#e2e2e2` | neutral | Subtle dividers, borders, and light interaction states |
| Pure White | `#ffffff` | neutral | Text on dark backgrounds, badges, and internal accents |
| Harvest Gold | `#ffdd33` | accent | Accent for highlighted content, badges, and occasional backgrounds |
| Power Red | `#ff6137` | accent | Striking accents, outlines, and decorative elements to draw attention |
| Royal Blue | `#0097e6` | accent | Accent for links, outlines, and decorative elements |
| Fresh Green | `#3e6b15` | semantic | Success states, badges, and outlined elements for positive reinforcement |
| Rich Violet | `#b26dc2` | accent | Decorative highlights and abstract graphic accents |
| Mustard Seed | `#bbb809` | accent | Decorative outlines and highlights |
| Blush Pink | `#f5c4cc` | accent | Decorative outlines and highlights |
| Electric Blue | `#1345eb` | accent | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |

## Typography

### ABC Social

| Key | Value |
| --- | --- |
| weight | 300, 400, 700 |
| sizes | 13px, 16px, 18px, 25px, 26px, 35px, 72px, 108px |
| lineHeight | 0.89, 1.00, 1.05, 1.10, 1.20, 1.43 |
| letterSpacing | -0.0400em at 108px, -0.0200em at 72px, -0.0140em at 35px, -0.0120em at 26px, -0.0110em at 25px, -0.0100em at 18px, 0.0080em at 16px, 0.0100em at 13px |
| fontFeatureSettings | 'ss07' |
| substitute | Inter |
| role | Primary typeface for most UI elements including body text, links, buttons, and smaller headings. The light weights (300, 400) maintain an airy, modern feel, while the generous letter-spacing for smaller text ensures readability. |

### ABC Social Extended

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 16px, 18px, 24px, 36px, 40px, 43px, 86px |
| lineHeight | 0.88, 1.00, 1.05, 2.63 |
| letterSpacing | -0.0230em at 86px, -0.0220em at 43px, -0.0100em at 36px, 0.0200em at 16px |
| fontFeatureSettings | 'ss07' |
| substitute | Inter |
| role | Reserved for large, impactful headlines, where its extended proportions demand attention. Its tight letter-spacing at larger sizes contributes to a bold, confident statement, often appearing as fragmented highlights. |

### ABC Social Condensed

| Key | Value |
| --- | --- |
| weight | 900 |
| sizes | 35px |
| lineHeight | 1.20 |
| letterSpacing | -0.0100em |
| fontFeatureSettings | 'ss07' |
| substitute | Inter Condensed |
| role | Used sparingly for very specific, emphasized text such as badges or callouts. Its heavy weight and condensed form provide strong visual punch and contrast against the lighter primary typeface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.43 | 0.01 |
| body-sm | 16 |  | 1.43 | 0.008 |
| body | 18 |  | 1.43 | -0.01 |
| subheading | 25 |  | 1.2 | -0.011 |
| heading | 35 |  | 1.2 | -0.014 |
| heading-lg | 43 |  | 1.05 | -0.022 |
| display-sm | 72 |  | 1 | -0.02 |
| display | 86 |  | 0.88 | -0.023 |
| display-lg | 108 |  | 0.89 | -0.04 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 1000px |
| images | 12px |
| inputs | 28px |
| buttons | 100px |

- **elementGap** — 20px
- **sectionGap** — 60px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#f7f5f3` | 0 | Dominant page background. |
| Pure White | `#ffffff` | 1 | Background for elevated elements like certain inputs or overlay content. |
| Harvest Gold | `#ffdd33` | 2 | Background for specific highlighted cards or decorative elements. |

## Components

### Primary Filled Button

**Role:** Main call to action button.

Solid Midnight Ink (#000000) background with Pure White (#ffffff) text. Features a full pill shape with 1000px border-radius, 15px vertical and horizontal padding, using ABC Social Regular at 16px.

### Ghost Outline Button

**Role:** Secondary call to action, less prominent than primary.

Transparent background with Graphite (#6a6559) text and a matching 1px border. Pill-shaped with 100px border-radius, 8px vertical and 10px horizontal padding, using ABC Social Regular at 16px.

### Ghost Border Button (Pill)

**Role:** Tertiary action or navigational elements requiring minimal visual weight.

Transparent background with Graphite (#6a6559) text and a matching 1px border. Full pill shape with 1000px border-radius, 15px vertical and horizontal padding, using ABC Social Regular at 16px.

### Badge with Background

**Role:** Highlighting status or keyword.

Harvest Gold (#ffdd33) background with Midnight Ink (#000000) text. No border-radius or padding observed directly as a consistent component style, appearing as an inline visual highlight.

### Success Status Badge

**Role:** Indicating a positive status.

Fresh Green (#3e6b15) background with Pure White (#ffffff) text. No border-radius, 6px vertical and 10px horizontal padding, using ABC Social Regular.

### Circular Card Highlight

**Role:** Decorative element or visual accent.

Harvest Gold (#ffdd33) background with a 1000px border-radius, appearing as a large circular shape with no discernible padding, creating a cut-out or background effect rather than a contained card.

### Text Input Field

**Role:** User input for forms.

Pure White (#ffffff) background with Midnight Ink (#000000) text. Border is 1px Midnight Ink (#000000). Features a generous 28px border-radius, 0px vertical and 20px horizontal padding.

## Layout

The page primarily uses a max-width contained layout, with content segments often centered or adopting clean two-column text-left/image-right (or vice-versa) arrangements. The hero section establishes a clear centered headline over a subtle background. Sections alternate between a base Canvas White and occasional Harvest Gold backgrounds for emphasis, creating a clear visual rhythm. Vertical spacing between sections is generous, contributing to a comfortable density. Navigation is a sticky top bar with clearly defined links and primary/secondary call-to-action buttons.

## Imagery

This site prominently features highly stylized, abstract graphic elements and product screenshots. The graphics are often playful and dimensional, using the accent color palette to create visual interest rather than realistic representations. Overlapping, torn-paper, or distressed effects are common for accent shapes, giving a handcrafted feel. Product screenshots are clean, high-fidelity, and typically contained within a well-defined UI, focusing on clarity over extensive context. Icons are simple, outlined, and monochromatic, used for navigation or feature delineation, prioritizing function without visual distraction.

## Dos & Donts

### Do

- Use Midnight Ink (#000000) for all primary text and important headings to ensure high legibility against the light canvas.
- Apply Canvas White (#f7f5f3) as the default background for all page sections and elevated surfaces.
- Utilize 1000px border-radius for all primary buttons and large decorative elements to create a soft, pill-shaped aesthetic.
- Employ Harvest Gold (#ffdd33) and Power Red (#ff6137) strategically and sparingly for emphasis, such as badges, outlined boxes, or text highlights, never as primary UI colors.
- Maintain a clear visual hierarchy using ABC Social for body text (16-18px), ABC Social Extended for major headlines (43-86px), and ABC Social Condensed for specific emphasized text like badges.
- Structure content with a consistent 20px element gap and 60px section gap for a comfortable, organized layout.
- Use ABC Social weights 300 and 400 for most text, reserving 700 for strong emphasis within body copy or bold components.

### Don't

- Avoid using multiple accent colors in close proximity; stick to one or two per view to maintain visual clarity.
- Do not introduce heavy shadows or gradients on surfaces; components should remain flat and minimal.
- Never use the accent colors for full background sections, as they are intended for highlights, not foundational colors.
- Avoid using default system fonts. Always use ABC Social variants or their specified substitutes.
- Do not deviate from the established border radii; maintain 1000px for pills, 100px for secondary buttons, and 28px for inputs.
- Resist adding additional padding or margins that break the 4px grid and established spacing tokens.
- Do not use dark backgrounds for large content areas; the system is designed for a light theme with dark text.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #f7f5f3
border: #6a6559
accent: #ffdd33
primary action: #000000 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #000000 background, #f7f5f3 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a ghost outline button for secondary action: Transparent background, Graphite (#6a6559) text, 1px Graphite (#6a6559) border, 100px border-radius, 8px vertical and 10px horizontal padding, ABC Social regular 16px text.
3. Create a success badge: Fresh Green (#3e6b15) background, Pure White (#ffffff) text, no border-radius, 6px vertical and 10px horizontal padding, ABC Social regular 16px text.
4. Create a text input field: Pure White (#ffffff) background, Midnight Ink (#000000) text, 1px Midnight Ink (#000000) border, 28px border-radius, 0px vertical and 20px horizontal padding, ABC Social regular 16px placeholder text.

---
_Source: https://styles.refero.design/style/2db41cd9-c898-4f59-b704-3042c0d87f45_
