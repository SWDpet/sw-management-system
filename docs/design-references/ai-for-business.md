# AI for Business — Design Reference

> High-contrast precision tooling

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.dayos.com](https://www.dayos.com) |
| Refero page | [https://styles.refero.design/style/ee403055-480e-4bd4-9216-07c9ae2dde2e](https://styles.refero.design/style/ee403055-480e-4bd4-9216-07c9ae2dde2e) |
| Theme | light |
| Industry | ai |

## Overview

Dayos presents a bold, high-contrast digital canvas for enterprise AI. Its visual identity is built on stark black typography against light, almost achromatic surfaces, creating a sense of seriousness and directness. Minimal use of vivid accent colors punctuates key information and interactive elements, ensuring they stand out without overwhelming the authoritative tone. Components are sharp and defined, often using square or subtly rounded forms, reflecting a precise and functional aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Ice | `#e5e7eb` | neutral | Primary page and section backgrounds, subtle borders, inactive input fields |
| Midnight Ink | `#000000` | neutral | Primary headings, body text, and icon fills on light surfaces. Do not promote it to the primary CTA color |
| Paper White | `#ffffff` | neutral | Elevated card backgrounds, interactive elements, text on dark backgrounds |
| Fog Gray | `#979797` | neutral | Muted text, secondary button borders, placeholder text, subtle icons |
| Ash Gray | `#444444` | neutral | Secondary body text, navigation labels, and subdued headings. Do not promote it to the primary CTA color |
| Faint Mist | `#f3f3f3` | neutral | Subtle background for navigation, ghost button fills, hovered states |
| Deep Smoke | `#2f2f2f` | neutral | Darker background surface for specific sections or content blocks |
| Action Green | `#d1ffca` | accent | Subtle interactive highlights, link backgrounds for emphasis |
| Alert Yellow | `#fff100` | accent | Decorative accents, occasional link text for high visibility |

## Typography

### SuisseIntl

| Key | Value |
| --- | --- |
| weight | 400, 450, 500 |
| weights | 400, 450, 500 |
| sizes | 14px, 16px, 18px, 20px, 28px, 40px |
| lineHeight | 1.10, 1.14, 1.20, 1.25, 1.30, 1.33 |
| letterSpacing | -0.42px at 14px, -0.42px at 16px, -0.72px at 18px |
| substitute | Inter |
| role | Body text, navigation, links, and general UI elements. Its range of weights allows for structured hierarchy within prose and component labels. |

### SuisseIntlCond

| Key | Value |
| --- | --- |
| weight | 700 |
| weights | 700 |
| sizes | 48px, 64px, 80px, 130px |
| lineHeight | 0.90 |
| letterSpacing | -1.44px at 48px, -1.92px at 64px, -2.4px at 80px, -3.9px at 130px |
| substitute | Bebas Neue |
| role | Prominent headlines and display text, used for impactful, commanding statements. The condensed, bold nature paired with tight line-height and tracking creates a visually dense and powerful presence. |

### SuisseIntlMono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.30, 1.60 |
| letterSpacing | -0.36px at 12px |
| substitute | IBM Plex Mono |
| role | Captions, metadata, and specialized UI labels where a technical, precise voice is needed. The monospace quality suggests code or hard data. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.6 | -0.36 |
| body-sm | 14 |  | 1.3 | -0.42 |
| body | 16 |  | 1.25 | -0.42 |
| subheading | 18 |  | 1.2 | -0.72 |
| heading | 28 |  | 1.14 |  |
| heading-lg | 40 |  | 1.1 |  |
| display-sm | 48 |  | 0.9 | -1.44 |
| display | 130 |  | 0.9 | -3.9 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 32px |
| buttons | 8px |
| heroElements | 64px |
| navigationItems | 12px |

- **elementGap** — 24px
- **sectionGap** — 80px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Navigation Link Default

**Role:** Standard navigation item for top-level menus.

backgroundColor=rgba(0,0,0,0) color=rgb(68,68,68) borderTopColor=rgb(68,68,68) borderRadius=0px paddingTop=0px paddingRight=16px paddingBottom=0px paddingLeft=16px

### Navigation Link Active

**Role:** Active or selected navigation item, visually distinct.

backgroundColor=rgba(0,0,0,0) color=rgb(0,0,0) borderTopColor=rgb(0,0,0) borderRadius=0px paddingTop=0px paddingRight=0px paddingBottom=0px paddingLeft=0px

### Schedule Demo Button

**Role:** Primary call to action button.

Background #000000, text #ffffff, 8px border-radius, paddingRight=16px.

### Ghost Button

**Role:** Secondary action button with a transparent background.

Transparent background, color #979797, paddingRight=16px, borderRadius=0px.

### Solid Card

**Role:** Content card with a dark background.

backgroundColor=#000000, borderRadius=32px, no boxShadow.

### Paper Card

**Role:** Content card with a light background.

backgroundColor=#ffffff, borderRadius=24px, no boxShadow.

### Pill Button with Round Corners

**Role:** Generic rounded button for various actions.

backgroundColor=rgba(0,0,0,0) color=rgb(0,0,0) borderRadius=4px

### Hero Card Top-Rounded

**Role:** Specific card style for hero or introductory content.

backgroundColor=#000000, borderRadius=64px 64px 0px 0px, no boxShadow.

## Layout

The page primarily uses a full-bleed layout for background color, with content contained within a central, assumed `pageMaxWidth`. The hero section is full-bleed with a large, left-aligned headline in SuisseIntlCond 700, juxtaposed with a conceptual 3D graphic on the right, creating a clear visual split. Sections are generally distinguished by consistent vertical spacing (80px `sectionGap`) and often employ a two-column layout with text on one side and supporting visuals or details on the other. Content often stacks centrally beneath headings. Navigation is a sticky top bar with clearly delineated sections and a prominent 'Schedule a Demo' button.

## Imagery

The site uses a combination of abstract, somewhat playful 3D rendered forms and conceptual illustrations. The primary graphic in the hero section features geometric, block-like structures in muted and vivid colors (yellow, green, pink) with a reflective surface, suggesting modularity and construction. Imagery serves a decorative and conceptual purpose, rather than presenting lifestyle or product photography. Icons are generally filled, often in black or gray, with a moderate stroke weight, appearing functional and simple. The overall density is image-light, focusing more on bold typography and structured UI.

## Dos & Donts

### Do

- Prioritize SuisseIntlCond 700 with tight letter-spacing for all display headlines to maintain a bold, commanding tone.
- Use Midnight Ink (#000000) for primary text and major interactive elements against Canvas Ice (#e5e7eb) or Paper White (#ffffff) backgrounds.
- Employ Action Green (#d1ffca) or Alert Yellow (#fff100) only as functional accents, such as link backgrounds or callout text, not for large decorative areas.
- Apply a 32px border-radius consistently to all card elements for a substantial, modern feel.
- Maintain high contrast between text and background with a minimum WCAG AAA ratio to ensure visual impact and authority.
- Use a base `elementGap` of 24px between coherent blocks of content or UI elements.
- Use Canvas Ice (#e5e7eb) as the dominant background color to establish a bright, open canvas.

### Don't

- Avoid using multiple chromatic colors in close proximity; color should be used sparingly for impact, not decoration.
- Do not introduce soft shadows or gradients for elevation; surfaces should primarily be flat or differentiated by stark color shifts.
- Refrain from using lightweight fonts for primary headings; always opt for SuisseIntlCond 700 with its defined letter-spacing.
- Do not deviate from the established border radii; avoid introducing custom or intermediate radii values.
- Do not use subtle variations of neutral shades for functional differentiation; use distinct achromatic colors like Midnight Ink for strong elements and Fog Gray for muted ones.
- Avoid generic imagery or stock photography; visual assets should be highly relevant and often technical in nature.
- Do not use overly expressive or decorative typography; the system favors directness and clarity.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #e5e7eb
border: #e5e7eb (for subtle borders) / #000000 (for strong borders)
accent: #d1ffca
primary action: no distinct CTA color

Example Component Prompts:
No distinct primary action color was observed; use the extracted neutral button treatments instead of inventing a filled CTA color.
2. Create a navigation link: 'Platform' text in Ash Gray (#444444), transparent background, no border, 16px horizontal padding. Font SuisseIntl 400.
3. Create a dark content card: background Midnight Ink (#000000), 32px border-radius, with a heading 'Product Features' in Paper White (#ffffff) using SuisseIntlCond 700 at 48px, line-height 0.9, letter-spacing -1.44px and body text below in SuisseIntl 400 at 16px, line-height 1.25, color Fog Gray (#979797).
4. Create a hero headline: 'BORN FROM THE AI ERA.' using SuisseIntlCond 700 at 130px, letter-spacing -3.9px, line-height 0.9, color Midnight Ink (#000000).

---
_Source: https://styles.refero.design/style/ee403055-480e-4bd4-9216-07c9ae2dde2e_
