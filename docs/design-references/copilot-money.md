# Copilot Money — Design Reference

> Midnight ocean with glowing buoys

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://copilot.money](https://copilot.money) |
| Refero page | [https://styles.refero.design/style/91b110da-902b-4d09-8bf0-26bd1f25f8b2](https://styles.refero.design/style/91b110da-902b-4d09-8bf0-26bd1f25f8b2) |
| Theme | dark |
| Industry | fintech |

## Overview

Copilot Money employs a sophisticated dark-mode aesthetic with a spacious canvas, primarily leveraging shades of deep blue and near-black. Dynamic, multi-colored 'tag' elements float across the interface, adding vibrant, soft-glow accents against the dark backdrop. Typography balances a modern geometric sans with a lighter, custom variable font, creating a refined information hierarchy. Components are soft-edged, often featuring inset shadows and subtle transparency, giving a sense of depth without harshness.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000814` | neutral | Primary page background, elevated surface base, deep shadows |
| Deep Space | `#010d1e` | neutral | Secondary background, subtle dark surfaces, layered shadows |
| Obsidian | `#11263b` | neutral | Accent text, outlined element borders, subtle separation lines |
| Mist Gray | `#ccced0` | neutral | Subtle headlines and body text, secondary interface elements |
| Shadow Blue | `#29303a` | neutral | Shadow base for elevated components |
| Deep Shadow | `#303741` | neutral | Darker shadow base for more prominent elevated components |
| Muted Stone | `#7f8ba4` | neutral | Muted body text for secondary information and tooltips |
| Platinum Ghost | `#ffffff` | neutral | Primary text, icon fill, accessible foreground on dark backgrounds |
| Teal Glow | `#00cc4b` | accent | Green outline accent for tags, dividers, and focused UI edges |
| Crimson Beam | `#ff4433` | accent | Red outline accent for tags, dividers, and focused UI edges |
| Cosmic Blue | `#426088` | accent | Blue outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Ocean Glimmer | `#597caa` | accent | Link text, borders for ghost buttons, subtle interactive element highlights |
| Iris Pop | `#1c6cff` | brand | Primary action background, prominent interactive elements — a bright, inviting call to interaction |
| Sunbeam | `#fece4c` | accent | Category tags, icon accents, highlights |
| Amethyst Flare | `#9019e6` | accent | Category tags, decorative iconography, ambient glows |
| Slate Blue | `#5c6f8a` | neutral | Subtle background for category tags, secondary cards |
| Cherry Blossom | `#ff33aa` | accent | Category tags, vibrant decorative accents |
| Pumpkin Zest | `#f27f0c` | accent | Category tags, vibrant decorative accents |
| Golden Harvest | `#ff9900` | accent | Category tags, vibrant decorative accents |
| Lime Soda | `#59cc33` | accent | Category tags, vibrant decorative accents |
| Indigo Wash | `#00215e` | neutral | Subtle background for links, dark page sections |
| Blue Gradient Overlay | `#5b7395` | accent | Decorative background gradient used sparingly for visual depth |

## Typography

### Matter Variable Thin

| Key | Value |
| --- | --- |
| weight | 100 |
| weights | 100 |
| sizes | 11px, 12px, 13px, 14px, 15px, 16px, 18px, 22px, 38px, 56px |
| lineHeight | 1.20, 1.30, 1.40, 1.60 |
| letterSpacing | -0.0200em at 56px, -0.0100em at 38px, 0.0100em at 18px, 0.0200em at 11-16px |
| fontFeatureSettings | 'ss07', 'blwf', 'cv03', 'cv04', 'cv09', 'cv11' |
| role | Primary body and sub-heading text, creating a light, modern feel with its variable weight and precise tracking. Its thinness makes larger headlines feel restrained. |

### Jokker Medium

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 12px, 14px, 16px, 18px, 22px, 24px, 32px, 64px |
| lineHeight | 1.00, 1.09, 1.10, 1.20, 1.40, 1.60 |
| letterSpacing | -0.0100em |
| fontFeatureSettings | 'blwf', 'cv03', 'cv04', 'cv09', 'cv11' |
| role | Accent headings and prominent body text; its medium weight provides structure and readability for key information. |

### Matter-TRIAL SemiBold

| Key | Value |
| --- | --- |
| weight | 670 |
| weights | 670 |
| sizes | 28px |
| lineHeight | 1.00 |
| letterSpacing | 0.0400em |
| role | Feature headings and callouts, designed for impact and distinctiveness with its tighter tracking. |

### Jokker Semibold

| Key | Value |
| --- | --- |
| weight | 600 |
| weights | 600 |
| sizes | 24px, 64px, 148px |
| lineHeight | 0.90, 1.10, 1.20 |
| letterSpacing | -0.0200em |
| role | Large display headings and section titles, commanding attention with its semi-bold weight and generous scale. |

### Jokker Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px, 24px |
| lineHeight | 1.40 |
| fontFeatureSettings | 'blwf', 'cv03', 'cv04', 'cv09', 'cv11' |
| role | Standard body text, providing clarity and readability at smaller sizes for detailed information blocks. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| role | Fallback for system text, ensuring basic readability where custom fonts might not load. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.2 | 0.22 |
| heading-sm | 22 |  | 1.2 | -0.22 |
| heading | 24 |  | 1 | -0.48 |
| heading-lg | 38 |  | 1.3 | -0.38 |
| display | 64 |  | 0.9 | -1.28 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 40px |
| cards | 24px |
| buttons | 16px |
| smallElements | 8px |
| inlineElements | 12px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#000814` | 0 | Primary page background, base for all content. |
| Base Surface | `#010d1` | 1 | Slightly elevated backgrounds, subtle background differences within sections. |
| Interactive Card | `#000814` | 2 | Container for featured content, distinguished by soft shadows and rounded corners. |
| Vibrant Tag Surface | `#ff33aa` | 3 | Dynamic, colored containers for categorization, featuring prominent inset shadows and rounded forms. |

## Components

### Primary Filled Button

**Role:** Call to action button for primary user flows.

Background: Iris Pop (#1c6cff), Text: Platinum Ghost (#ffffff), Border Radius: 16px, Padding: 12px vertical, 16px horizontal. Font: Jokker Medium, 12px, 500 weight.

### Ghost Button

**Role:** Secondary action or navigation link.

Background: transparent, Text: Platinum Ghost (#ffffff), Border: 1px solid Ocean Glimmer (#597caa), Border Radius: 16px, Padding: 12px vertical, 16px horizontal. Font: Jokker Medium, 12px, 500 weight.

### Floating Content Card

**Role:** Displaying featured content or information blocks.

Background: Midnight Ink (#000814), Border Radius: 20px, Shadow: rgba(0, 0, 0, 0.07) 0px 0.36px 0.65px -1.5px, rgba(0, 0, 0, 0.05) 0px 3px 5.4px -3px, Padding: 14px vertical, 0px horizontal.

### Vibrant Category Tag

**Role:** Categorization of items, dynamically colored.

Background: Dynamic vivid color (e.g., Cherry Blossom #ff33aa, Pumpkin Zest #f27f0c), Text: Platinum Ghost (#ffffff), Border Radius: 40px (pill-shaped), Padding: 16px vertical, 24px-32px horizontal. Shadow: rgba(255, 255, 255, 0.16) inset 0px 24px 64px -12px, rgba(0, 0, 0, 0.16) inset 0px -24px 40px -12px, plus multiple inset highlights.

### Navigation Link

**Role:** Primary navigation elements in header or footer.

Text: Platinum Ghost (#ffffff) for active/hover, Ocean Glimmer (#597caa) for inactive. Font: Matte Variable Thin, 14px, 100 weight.

### Message Banner

**Role:** Alerts or promotional messages at the top of the page.

Background: Indigo Wash (#00215e), Text: Platinum Ghost (#ffffff), Border Radius: 0px. Padding: 5px vertical, 10px horizontal. Font: sans-serif, 12px, 400 weight.

## Layout

The page primarily uses a full-bleed structure for its dark background, combined with a contained maximum width for core content, which appears centered. The hero section features a large, centered headline overlaid with dynamically arranged, glowing 'tag' elements, creating an immediate sense of activity and organization. Sections are separated by consistent vertical spacing, and there's a pattern of presenting information in centered stacks or simple text-aligned-left compositions with a strong visual element on the right (though not strongly represented in the provided screenshots, suggested by the 'money organized' layout). The navigation is a sticky top bar, minimal and un-intrusive, designed to blend into the overall dark theme, with a primary call to action in the top right. Content density is generally spacious, allowing elements to breathe on the dark canvas.

## Imagery

The visual language focuses on abstract, illustrative 'tag' elements rendered with soft glows and a shallow depth of field, positioned against a dark, minimalist UI. These tags are color-varied, featuring clear and vivid hues like #00cc4b, #ff4433, #9019e6, and #fece4c. Icons are outlines, rendered in white against the dark background, maintaining a clean and functional appearance. Photography is notably absent, with the visual emphasis placed entirely on UI elements and stylized graphic accents. The density of these visuals is moderate, allowing them to serve as engaging focal points without overwhelming the sparse textual information. There are product screenshots of UI embedded in devices that are presented flat, consistent with the minimal aesthetic. These serve to showcase product functionality in context.

## Dos & Donts

### Do

- Prioritize Midnight Ink (#000814) or Deep Space (#010d1e) for all background canvases and card surfaces to maintain the dark theme.
- Use Platinum Ghost (#ffffff) for all primary body and headline text to ensure legibility and brand consistency.
- Apply Iris Pop (#1c6cff) exclusively for primary calls to action to create clear interaction points.
- Implement a 16px border-radius for all interactive elements like buttons and primary inputs.
- Use Matter Variable Thin with precise letter-spacing adjustments for headlines to achieve a light, modern typographic presence.
- Incorporate vibrant accent colors like Teal Glow (#00cc4b) or Crimson Beam (#ff4433) for category tags and small decorative elements, ensuring they stand out against the dark background.
- Employ inset shadows (like rgba(255, 255, 255, 0.16) inset) on vibrant tags and cards to give elements a subtle, glowing depth.
- Maintain an element gap of 16px (4 units) and card padding of 16px for consistent visual rhythm and content separation.

### Don't

- Avoid using light backgrounds for primary content areas; maintain the dark theme's integrity.
- Do not introduce new saturated primary colors; limit vibrant accents to the established palette of Teal Glow, Crimson Beam, Iris Pop, etc.
- Do not use sharp 0px corners on any card-like or interactive UI elements; maintain soft radii (16px, 20px, 24px, 40px).
- Avoid heavy drop shadows; prefer the subtle, inset shadow styles or minimal elevation visible in the system.
- Do not use generic system fonts in place of Matter Variable Thin or Jokker; preserve the distinctive brand typography.
- Do not create dense, text-heavy blocks without sufficient padding or line height; ensure ample white space for readability within the dark context.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000814
border: #11263b
accent: #00cc4b
primary action: #1c6cff (filled action)

Example Component Prompts:
1. Create a Primary Filled Button: background #1c6cff, text #ffffff, 16px radius, 12px 16px padding. Text 'Get started' using Jokker Medium, 12px, 500 weight.
2. Create a Vibrant Category Tag for 'Groceries': background #ff4433, text #ffffff, 40px radius, 16px 32px padding, with the specified inset shadow stack. Text 'GROCERIES' using Matter Variable Thin, 14px, 100 weight.
3. Create a Navigation Link for 'Pricing': text Ocean Glimmer (#597caa), 0px radius, 4px vertical 8px horizontal padding. Text 'Pricing' using Matter Variable Thin, 14px, 100 weight.
4. Create a Floating Content Card: background Midnight Ink (#000814), 20px radius, 14px vertical 0px horizontal padding, with the specified rgba(0,0,0) shadow. Content text: Platinum Ghost (#ffffff).

---
_Source: https://styles.refero.design/style/91b110da-902b-4d09-8bf0-26bd1f25f8b2_
