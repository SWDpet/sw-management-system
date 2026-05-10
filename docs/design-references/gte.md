# GTE — Design Reference

> High-contrast digital trading floor.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.gte.xyz](https://www.gte.xyz) |
| Refero page | [https://styles.refero.design/style/5d273906-0110-48cf-99cd-63a72eb9c586](https://styles.refero.design/style/5d273906-0110-48cf-99cd-63a72eb9c586) |
| Theme | mixed |
| Industry | fintech |

## Overview

GTE embodies a stark, high-contrast digital trading floor: deep backgrounds meet bright surfaces and crisp typography. A single vibrant orange accent serves as the primary call to action, cutting through the monochrome with functional urgency. The design prioritizes clear information hierarchy and lightweight components, reflecting efficiency and speed without heavy ornamentation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Carbon | `#09090b` | neutral | Deep background for dark sections, card surfaces, and primary text in light-on-dark contexts |
| Midnight Ink | `#000000` | neutral | Primary text color in light mode, prominent borders, and icons |
| Ash | `#18181b` | neutral | Secondary dark card surfaces and subtle borders in dark mode |
| Polar White | `#ffffff` | neutral | Primary page canvas for light sections, text on dark backgrounds, and active state highlights |
| Fog | `#ebebeb` | neutral | Subtle light card surfaces and elevated sections in light mode |
| Parchment | `#e5e7eb` | neutral | Secondary light card backgrounds and subtle divider fill colors |
| Steel | `#71717a` | neutral | Muted helper text and subtle borders in light mode |
| Stone Grey | `#a1a1aa` | neutral | Muted text, placeholder text, and inactive icon fills |
| Ghost | `#d4d4d8` | neutral | Lightest background for dividers or subtle UI elements in light mode |
| Turbo Orange | `#ff7817` | brand | Primary call-to-action button background, interactive accents, and functional highlights. Its vivid presence signals action and importance |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | Arial |
| role | Smallest UI labels and utility text, defaulting to system sans-serif for optimal legibility at compact sizes. |

### Ogg Text Light

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 28px, 40px, 80px |
| lineHeight | 0.95, 1.00, 1.10, 1.20 |
| letterSpacing | -0.02em |
| fontFeatureSettings | "blwf", "cv03", "cv04", "cv09", "cv11" |
| substitute | serif system font |
| role | Display and large headings. The light weight and calligraphic features ('blwf', 'cv03', 'cv04', 'cv09', 'cv11') convey a refined, authoritative tone, balancing the technical subject matter with an artisanal feel. |

### PP Supply Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400, 500 |
| sizes | 18px |
| lineHeight | 1.20 |
| letterSpacing | -0.04em |
| substitute | monospace system font |
| role | Subheadings, navigational links, and emphasized body text. Its monospaced nature evokes a programmatic, precise feel, reinforcing the core product identity. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px, 18px |
| lineHeight | 1.00, 1.20, 1.30 |
| letterSpacing | -0.0200em |
| substitute | Inter |
| role | Body copy, input fields, and smaller informational text. Its clean, utilitarian nature ensures readability for dense information. |

### PP Supply Mono Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px |
| lineHeight | 1, 1.2, 1.4 |
| letterSpacing | -0.04, -0.02 |
| role | PP Supply Mono Regular — detected in extracted data but not described by AI |

### PP Supply Mono Medium

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 14px, 16px, 20px |
| lineHeight | 0.75, 1.1, 1.2, 1.4 |
| letterSpacing | -0.04, -0.02 |
| role | PP Supply Mono Medium — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 |  |
| body-sm | 14 |  | 1.2 | -0.48 |
| body | 16 |  | 1.2 | -0.32 |
| body-lg | 18 |  | 1.2 | -0.36 |
| subheading | 20 |  | 1.4 | -0.4 |
| heading | 28 |  | 1.2 | -0.56 |
| heading-lg | 40 |  | 1.1 | -0.8 |
| display | 80 |  | 0.95 | -1.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 600px |
| links | 56px |
| small | 4px |
| inputs | 8px |
| buttons | 8px |
| default | 12px |
| largerCards | 24px |
| cookieBanner | 28px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Primary Action Button

**Role:** Call to action.

Filled button with Turbo Orange background (#ff7817) and Midnight Ink text (#000000). Features 8px border-radius and 16px horizontal padding. Text uses Inter font at 16px, weight 400.

### Dark Card (Level 1)

**Role:** Container for content.

Uses Carbon (#09090b) as background, with 12px border-radius. Inset white shadow: rgba(255, 255, 255, 0.1) 0px 0px 0px 1px inset to denote interactivity/depth.

### Light Card (Level 1)

**Role:** Container for content.

Uses Fog (#ebebeb) as background, with 12px border-radius. No distinct shadow for elevation; relies on background color contrast.

### Dark Card (Level 2)

**Role:** Container for content, elevated.

Uses Ash (#18181b) as background, with 24px border-radius. No distinct shadow for elevation; relies on background color contrast.

### Cookie Banner Card

**Role:** System feedback or disclaimer.

Large, dark surface using Carbon (#09090b) with a prominent 28px border-radius. Acts as a modal or persistent footer element.

### Text Input (Dark)

**Role:** User input.

Translucent background rgba(255, 255, 255, 0.1) with Polar White (#ffffff) text and border. Features 8px border-radius and 10px padding.

### Accent Input (Orange Fill)

**Role:** Submit or action input.

Uses Turbo Orange (#ff7817) background with Carbon (#09090b) text and border. Features 8px border-radius and 10px padding.

### Feature Box (Light)

**Role:** Informational container.

Parchment (#e5e7eb) background with 12px border-radius. Serves as a muted background for illustrative content or feature descriptions.

## Layout

The page primarily uses a full-bleed structure for hero sections, which feature a large, centered headline over an immersive visual, then transitions to a max-width contained layout (implied around 1200px based on content blocks). Sections alternate between dark backgrounds (Carbon #09090b) and light backgrounds (Polar White #ffffff or Parchment #e5e7eb) to create distinct visual rhythm. Content often arranges in two-column layouts, frequently with text on one side and a product illustration or card on the other. Card grids are used for presenting features. The navigation is a sticky top bar, minimal in its presence, with a strong Turbo Orange button for 'Launch App'.

## Imagery

The visual language combines abstract 3D renders with illustrative product screenshots. Photography is absent. Abstract graphics feature metallic or coin-like objects with a warm, amber glow against dark backgrounds, suggesting value and motion. These visuals are primarily decorative and atmospheric, full-bleed in hero sections. Product screenshots are contained within light cards, using a monochromatic outlined style for UI elements. Icons are minimal, either solid or outlined, typically monochromatic, with PP Supply Mono font often integrated into their design. The overall density is image-heavy in hero sections, transitioning to text-dominant with illustrative support in feature sections.

## Dos & Donts

### Do

- Use Turbo Orange (#ff7817) exclusively for primary calls to action and critical interactive elements.
- Maintain a high contrast ratio between text and background, adhering to AAA accessibility standards where possible (e.g., Midnight Ink #000000 on Polar White #ffffff; Polar White #ffffff on Carbon #09090b).
- Apply 12px border-radius for most cards and default containers, with 8px for interactive elements like buttons and inputs, and 24px for visually distinct, larger cards.
- Utilize PP Supply Mono for headings and navigational elements to emphasize a technical, precise tone.
- Employ Polar White (#ffffff) and Carbon (#09090b) as dominant background colors, defining distinct light and dark sections or components.
- Keep element spacing consistent with increments of 10px and padding with 16px to maintain a compact yet readable density.
- Ensure large display headings use Ogg Text Light at weight 300 to create an atmosphere of restrained authority with unique typographic flourishes.

### Don't

- Avoid using multiple chromatic colors; limit accents strictly to Turbo Orange (#ff7817) unless for semantic status (e.g., success/error not present in data).
- Do not introduce heavy shadows or gradients on surfaces; rely on distinct background colors and subtle inset shadows (rgba(255, 255, 255, 0.1) 0px 0px 0px 1px inset) for depth.
- Refrain from using overly decorative or illustrative elements that detract from the stark, functional UI.
- Do not deviate from the established type scale; maintain consistent font families, weights, and letter spacing for their defined roles.
- Avoid large amounts of whitespace around content sections; the design favors a 'compact' density.
- Do not use generic sans-serif fonts where Ogg Text Light, PP Supply Mono, or Inter are specified, as they define the brand's unique typographic voice.
- Do not apply large, rounded corners globally; save specific larger radii for distinct components like cookie banners (28px) or pill shapes (600px).

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #18181b
accent: #ff7817
primary action: #ff7817 (filled action)

Example Component Prompts:
1. Create a hero section: full-bleed Carbon (#09090b) background. Headline 'Trade fast. Win faster ///' at 80px Ogg Text Light weight 300, Polar White (#ffffff), letter-spacing -1.6px. Place a 'Launch App' Primary Action Button (#ff7817 background, #000000 text, 8px radius, Inter 16px weight 400). 
2. Create a feature card: Light Card (Level 1) with Parchment (#e5e7eb) background, 12px border-radius, 16px padding. Title 'Unlock the best possible prices.' using PP Supply Mono 20px weight 400, Midnight Ink (#000000). Body text Inter 16px weight 400, Midnight Ink (#000000).
3. Create a dark section: Carbon (#09090b) background. Subheading 'Key Features' with PP Supply Mono 20px weight 500, Polar White (#ffffff). Followed by two Dark Card (Level 1) components side-by-side (Carbon #09090b background, 12px radius, inset shadow), each with Polar White (#ffffff) body text (Inter 16px weight 400).

---
_Source: https://styles.refero.design/style/5d273906-0110-48cf-99cd-63a72eb9c586_
