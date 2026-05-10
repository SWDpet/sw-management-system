# Solana — Design Reference

> Galactic Command Center. Surfaces are deep space, accents are cosmic signals.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://solana.com](https://solana.com) |
| Refero page | [https://styles.refero.design/style/f493133e-e289-4fb1-9729-f611d9816aae](https://styles.refero.design/style/f493133e-e289-4fb1-9729-f611d9816aae) |
| Theme | dark |
| Industry | crypto |

## Overview

Solana's design embodies a 'cyber-dark' aesthetic, where deep, rich blacks and near-blacks define the primary surfaces, creating a stage for technological elegance. A vibrant, almost holographic purple gradient often provides atmospheric depth, suggesting digital flow. White typography and sparse, vivid chromatic accents (lime, violet, orange) act like data points or signal lights, providing crucial contrast and indicating interactivity within the dark interface. The interplay of fluid gradients and sharp, contained UI elements gives it a feeling of advanced, yet approachable, digital infrastructure.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space | `#000000` | neutral | Page background, primary dark UI elements – creates an immersive, infinite dark canvas. |
| Dark Matter | `#121212` | neutral | Secondary surface background, card surfaces, navigation background – slightly lighter than Deep Space to provide subtle layering without harsh divisions. |
| Void Shadow | `#0d0c11` | neutral | Subtle background for UI components, offering a slightly softer black than the absolute #000000. |
| Vapor Gray | `#ababba` | neutral | Primary body text, icon fills – a very light gray that appears white against the dark backgrounds, ensuring readability without harshness. |
| Polar White | `#ffffff` | neutral | Headlines, critical text, selected interactive elements – acts as a spotlight against dark backgrounds, drawing immediate attention. |
| Border Plasma | `#eaecf0` | neutral | Subtle borders and separators, providing structure on dark elements without strong visual breaks. |
| Subtle Stone | `#848895` | neutral | Secondary text, descriptive elements (likely --body-text-secondary based on token hint) – a slightly darker gray for less prominent information. |
| Infrared Gradient | `#000000` | brand | Hero section background, atmospheric graphic elements – provides a sense of depth and digital energy, particularly in the hero. The starting black is #000000, fading into #14001d. |
| Cyber Lime | `#55e9ab` | accent | Small interactive elements, success indicators – a vibrant, highly saturated green that stands out as a focal point. |
| Digital Violet | `#ca9ff5` | accent | Illustrative accents, subtle interactive states – a softer, yet still vibrant violet for visual interest. |
| Neon Orange | `#f48252` | accent | Highlight elements, specific icons – a punchy orange for high-visibility accents. |
| Muted Lilac | `#dfcdf5` | accent | Subtle background for specific interactive states or highlighted sections – provides a hint of color that is less intense than direct accents. |

## Typography

### Diatype

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| sizes | 16px, 18px, 20px, 21px, 24px, 28px, 36px, 40px, 64px, 88px |
| lineHeight | 1.00, 1.13, 1.14, 1.20, 1.22, 1.31, 1.33, 1.40, 1.50, 1.56 |
| letterSpacing | -0.0400em at 88px, -0.0300em at 64px, -0.0200em at 40px, -0.0100em at 28px and larger, tapering to normal at smaller sizes |
| substitute | Inter |
| role | Primary typeface for all content. The varied weights and precise letter-spacing create a highly legible yet contemporary feel, crucial for conveying technical information clearly against dark backgrounds. |

### DSemi

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 12px, 14px |
| lineHeight | 1.14, 1.33 |
| substitute | Roboto Slab |
| role | Used for navigational items and small, assertive labels. Its bold, serif-like weight provides clarity and distinction for functional elements within the predominantly sans-serif interface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.14 |  |
| body-sm | 14 |  | 1.33 |  |
| body | 16 |  | 1.5 | 0 |
| body-lg | 18 |  | 1.5 |  |
| subheading | 20 |  | 1.31 |  |
| heading-lg | 28 |  | 1.2 | -0.28 |
| display-sm | 36 |  | 1.14 | -0.72 |
| display | 40 |  | 1.13 | -0.8 |
| display-lg | 64 |  | 1 | -1.92 |
| display-xl | 88 |  | 1 | -3.52 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| buttons | 9999px |
| containers | 12px |
| smallElements | 4px |

- **elementGap** — 4px
- **sectionGap** — 
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Components

### Promotion Card (Hackathon)

### Event Cards — Meet Solana IRL

### News Article Card — Latest on Solana

### Primary Ghost Button

**Role:** Call to action, navigation items

backgroundColor=transparent, color=rgb(255, 255, 255), borderTopColor=rgba(236, 228, 253, 0.2), borderRadius=9999px, paddingTop=0px, paddingRight=24px, paddingBottom=0px, paddingLeft=24px. Text uses Diatype, 16px.

### Secondary Ghost Button

**Role:** Subtle actions, secondary navigation

backgroundColor=transparent, color=rgba(255, 255, 255, 0.64), borderTopColor=rgb(234, 236, 240), borderRadius=9999px, paddingTop=4px, paddingRight=16px, paddingBottom=4px, paddingLeft=16px. Text uses Diatype, 16px.

### Icon-Only Ghost Button

**Role:** Tertiary actions, social links, compact navigation

backgroundColor=rgba(0, 0, 0, 0.7), color=rgb(255, 255, 255), borderTopColor=rgb(234, 236, 240), borderRadius=9999px, paddingTop=0px, paddingRight=0px, paddingBottom=0px, paddingLeft=0px.

### Card Button

**Role:** Interactive clickable cards featuring content

backgroundColor=rgba(236, 228, 253, 0.12), color=rgb(255, 255, 255), borderTopColor=rgb(234, 236, 240), borderRadius=12px, paddingTop=32px, paddingRight=24px, paddingBottom=32px, paddingLeft=24px. This button style elevates card content to be clickable.

### Transparent Content Card

**Role:** Showcasing content blocks, events, images with minimal visual chrome.

backgroundColor=transparent, borderRadius=0px, boxShadow=none, paddingTop=32px, paddingRight=32px, paddingBottom=32px, paddingLeft=32px. Features white text (Diatype, various sizes) against the dark page background, emphasizing content over container.

### Fixed Navigation Bar

**Role:** Global navigation, search, language toggle

Fixed header. backgroundColor=rgb(18, 21, 29) (apparent from screenshot, slightly darker than main BG), height 71px. Contains ghost buttons and search input. Text is Polar White (Diatype 16px).

### Promotion Card (Hackathon)

**Role:** Highlighting specific events or features

backgroundColor=rgba(236, 228, 253, 0.12), borderRadius=12px. Padding is generous. Contains heading text (Diatype 21px, Polar White), body text (Diatype 16px, Vapor Gray), and a Ghost Button (Diatype 14px, Polar White), creating a self-contained informational block.

## Layout

The page primarily employs a max-width contained layout for content sections, set against full-bleed dark backgrounds. The hero section is full-bleed with a prominent abstract gradient graphic and a large, centered headline, immediately establishing the brand's aesthetic. Subsequent sections alternate between full-width content bands and cards grids. Content arrangement frequently uses large, centered text blocks (headings) followed by multi-column card grids for features and events. Spacing between sections is generous, creating a sense of expansiveness. The navigation is a sticky top bar, providing consistent access to global actions.

## Imagery

The visual language is characterized by abstract, swirling 3D graphics in deep purples, blues, and electric greens, suggesting data flow and connectivity. These graphics are often full-bleed in hero sections, creating immersive backgrounds that establish a futuristic, technological atmosphere. Product screenshots are absent; instead, the focus is on the underlying infrastructure and abstract representations of its power. Icons are minimalist, using mono-color fills or strokes, primarily in white or accent colors, complementing the UI rather than distracting from it.

## Elevation philosophy

This design largely eschews traditional shadows, instead relying on layered dark backgrounds and subtle inner glows (like those seen on some buttons) to create depth. The primary method for conveying hierarchy and distinction is through varying shades of dark background colors and high-contrast typography, giving a floating, almost holographic feel to elements rather than a grounded, shadowed one. The single prominent shadow found on a specific button variant appears more as an ethereal glow and inner highlight than a true light-source shadow, reinforcing the digital, non-physical aesthetic.

## Dos & Donts

### Do

- Do use Deep Space (#000000) as the primary page background for ultimate contrast.
- Do apply 9999px borderRadius for all interactive buttons and pill-shaped elements to maintain brand consistency.
- Do utilize Diatype (or Inter) at heavier weights (300-500) and Polar White (#ffffff) for all headlines.
- Do leverage the Infrared Gradient (linear-gradient(rgb(0, 0, 0), rgb(20, 0, 29))) for hero backgrounds to establish atmospheric depth.
- Do maintain a comfortable elementGap of 4px for tight UI element separation, scaling up as needed for larger blocks.
- Do use Border Plasma (rgba(236, 228, 253, 0.2)) for subtle borders on ghost buttons against dark backgrounds.
- Do use Cyber Lime (#55e9ab) sparingly as an accent color for key interactive states or indicators.

### Don't

- Don't use pronounced box shadows; subtle insets or glows are preferred, if any, as the design relies on color and gradient depth.
- Don't introduce additional bright or pastel backgrounds; maintain the dark theme with Dark Matter (#121212) or Deep Space (#000000) for all surfaces.
- Don't use standard square buttons; all primary interaction buttons should be pill-shaped (9999px radius).
- Don't use overly saturated colors for body text; rely on Vapor Gray (#ababba) for readability against dark surfaces.
- Don't use generic system fonts; always prioritize Diatype (or its substitute Inter) to maintain the brand's typographic identity.
- Don't break the established padding of 32px for cards; this ensures consistent content breathing room.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- Text (Primary): #ababba
- Text (Headline): #ffffff
- Background (Page): #000000
- Background (Card): #121212
- Accent (Interactive): #55e9ab
- Border (Subtle): rgba(236, 228, 253, 0.2)

Example Component Prompts:
1. Create a `Primary Ghost Button`: backgroundColor=transparent, color=#ffffff, borderTopColor=rgba(236, 228, 253, 0.2), borderRadius=9999px, paddingTop=0px, paddingRight=24px, paddingBottom=0px, paddingLeft=24px, Diatype font (Inter) 16px.
2. Design a `Promotion Card`: backgroundColor=rgba(236, 228, 253, 0.12), borderRadius=12px, padding=32px. Use Diatype 21px Polar White for the headline, Diatype 16px Vapor Gray for body, and add a Secondary Ghost Button.
3. Build a `Content Card` with the following properties: backgroundColor=transparent, borderRadius=0px, boxShadow=none, paddingTop=32px, paddingRight=32px, paddingBottom=32px, paddingLeft=32px. Place a Diatype 40px Polar White headline and Diatype 18px Vapor Gray paragraph inside.
4. Create a `Fixed Navigation Bar`: background-color=rgb(18, 21, 29), height=71px. Include two `Secondary Ghost Buttons` and a `Icon-Only Ghost Button` (search) on the right side.

---
_Source: https://styles.refero.design/style/f493133e-e289-4fb1-9729-f611d9816aae_
