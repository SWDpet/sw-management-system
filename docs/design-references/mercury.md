# Mercury — Design Reference

> Mountain Top Command Center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://mercury.com](https://mercury.com) |
| Refero page | [https://styles.refero.design/style/3172cd4d-118a-4a16-a259-6b634d32322e](https://styles.refero.design/style/3172cd4d-118a-4a16-a259-6b634d32322e) |
| Theme | dark |
| Industry | fintech |

## Overview

The design feels like a command center at twilight, expansive and focused. A deep, near-black neutral palette (#1e1e2a, #171721) creates an immersive, cinematic canvas where glowing off-white text (#ededf3) provides crisp clarity. All energy is channeled into a single, vibrant violet-blue accent (#5266eb) reserved strictly for primary calls-to-action, like indicator lights on a high-tech console. The typography is a defining feature, with custom fonts used at light weights for headlines, creating an authoritative yet approachable voice. The contrast between spacious, atmospheric hero imagery and the stark, text-driven UI below creates a journey from aspiration to action.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Mercury Blue | `#5266eb` | brand | Primary CTA buttons — the single, vivid accent in a muted palette, focusing user action. |
| Ghost Blue | `#cdddff` | accent | Secondary button backgrounds, hover states — a desaturated, ethereal blue suggesting interaction. |
| Deep Space | `#171721` | neutral | Outermost page background layer, providing depth. |
| Midnight Slate | `#1e1e2a` | neutral | Primary page and section backgrounds. |
| Graphite | `#272735` | neutral | Subtle button backgrounds and interactive surfaces. |
| Lead | `#70707d` | neutral | Borders, dividers, subtle UI accents. |
| Starlight | `#ededf3` | neutral | Primary text color for headlines, body, and navigation. |
| Silver | `#c3c3cc` | neutral | Secondary text, footer copy, disabled states. |
| Pure White | `#ffffff` | neutral | Text on primary CTA buttons (#5266eb). |

## Typography

### arcadiaDisplay

| Key | Value |
| --- | --- |
| weight | 360, 480, 530 |
| sizes | 21px, 24px, 28px, 32px, 42px, 49px, 65px |
| lineHeight | 1.10-1.20 |
| letterSpacing | Subtle positive tracking (0.01-0.02em) for an open feel. |
| fontFeatureSettings | "ss01" on |
| substitute | Inter, Manrope |
| role | All major headlines. Use of the light 360 weight at large sizes is a signature choice creating authority through restraint, not volume. |

### arcadia

| Key | Value |
| --- | --- |
| weight | 360, 400, 420, 480 |
| sizes | 12px, 14px, 16px, 18px, 21px |
| lineHeight | 1.20-1.50 |
| letterSpacing | Subtle positive tracking (0.005-0.02em) for readability. |
| fontFeatureSettings | "ss01" on |
| substitute | Inter, Manrope |
| role | Body copy, UI labels, navigation, legal text, and smaller headings. The workhorse font for all content and interface text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0.24 |
| body-sm | 14 |  | 1.5 | 0.28 |
| body | 16 |  | 1.5 | 0.16 |
| subheading | 18 |  | 1.4 |  |
| heading-sm | 21 |  | 1.35 |  |
| heading | 32 |  | 1.2 |  |
| heading-lg | 49 |  | 1.15 |  |
| display | 65 |  | 1.1 | 0.65 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 0px |
| inputs | 32px |
| buttons | 32px, 40px |
| containers | 4px |

- **elementGap** — 12-32px
- **sectionGap** — 80-120px
- **cardPadding** — 
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Abyss | `#171721` | 0 | Outermost page background layer. |
| Surface | `#1e1e2a` | 1 | Main content section background. |
| Interactive | `#272735` | 2 | Hover states or contained interactive elements. |

## Components

### Hero Email CTA

### Feature List — Everything in one place

### Button Group — Primary + Secondary + Ghost

### Primary Pill Button

**Role:** The main call-to-action on the page.

Solid 'Mercury Blue' (#5266eb) background with 'Pure White' (#ffffff) text. Fully rounded with a 32px radius. Padding is around 16px vertically and 24px horizontally. Uses 'arcadia' font.

### Header Pill Button

**Role:** The secondary call-to-action in the navigation bar.

Translucent 'Ghost Blue' (#cdddff at ~20%) background with 'Starlight' (#ededf3) text. Fully rounded with a 40px radius. Padding is 8px 20px. Uses 'arcadia' font.

### Ghost Nav Link

**Role:** Navigation items and non-primary links in the header.

Transparent background with 'Starlight' (#ededf3) text. No visible border. Generous hit area implied by padding, visually appears as text-only. Uses 'arcadia' font.

### Hero Email Input

**Role:** The email capture field in the hero section.

Transparent background with 'Starlight' (#ededf3) text and placeholder. Left side is fully rounded (32px radius), right side is sharp (0px radius) to join with the button. A thin 'Lead' (#70707d) border is implied for definition.

### Interactive Feature Link

**Role:** Selectable items in a feature list.

Transparent background with 'Starlight' (#ededf3) text. A 1px 'Lead' (#70707d) border on the bottom separates items. No radius. Uses 'arcadiaDisplay' for the title text.

### Footer Link

**Role:** Tertiary links in the page footer.

Transparent background with 'Silver' (#c3c3cc) text. Lighter text color indicates lower priority. Uses 'arcadia' font.

## Layout

The layout uses a full-bleed hero that occupies the entire viewport, featuring a centered headline and CTA over a background image. Below the hero, the page transitions to a max-width (approx. 1200px) centered layout on a dark background. Content is organized in simple, single-column stacks with generous vertical spacing, creating a calm, linear reading flow. Navigation is a minimal, semi-transparent top bar that likely becomes sticky. The overall structure is spacious and uncluttered.

## Imagery

The visual language is bifurcated. It opens with a full-bleed, atmospheric photograph of a solitary desk in a vast natural landscape, establishing a mood of boundless ambition and serene focus. This imagery is purely atmospheric, not product-focused. Beyond the hero, the site is starkly text-dominant, with no additional photography or illustration. This contrast positions the brand's 'vibe' first, then transitions to a purely functional, information-driven experience.

## Elevation philosophy

Elevation is achieved through light and color, not shadow. Interactive elements brighten or adopt the brand accent color on hover or focus, appearing to 'light up' from within the dark interface. Layered surfaces are differentiated by subtle shifts in the neutral gray palette (e.g., Midnight Slate #1e1e2a on Deep Space #171721).

## Dos & Donts

### Do

- Use 'arcadiaDisplay' at a light weight (360) for all major headlines to maintain an airy, sophisticated tone.
- Reserve the 'Mercury Blue' (#5266eb) accent exclusively for primary, action-oriented CTAs.
- Employ the deep neutral palette (#171721, #1e1e2a) for all backgrounds to create a focused, immersive environment.
- Utilize extreme corner radii (32px, 40px) for all primary and secondary buttons, creating a signature 'pill' shape.
- Maintain high contrast with 'Starlight' (#ededf3) text on dark backgrounds for all primary content.
- Use generous vertical spacing (80px+) between content sections.
- Differentiate interactive list items with a simple 1px bottom border in 'Lead' (#70707d).

### Don't

- Don't use 'Mercury Blue' (#5266eb) for text, backgrounds, or decorative elements.
- Don't use heavy font weights (>530) for any typography.
- Don't apply shadows for elevation. Use color and opacity shifts instead.
- Don't introduce new saturated colors. The palette is monochrome plus one blue accent.
- Don't use small corner radii on buttons. They should always be pills.
- Don't use 'Pure White' (#ffffff) for body text; reserve it for text on the primary blue CTA.
- Don't create dense, cluttered layouts. Prioritize breathing room.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Page Background:** Midnight Slate (#1e1e2a)
- **Primary Text:** Starlight (#ededf3)
- **Secondary Text:** Silver (#c3c3cc)
- **Primary CTA:** Mercury Blue (#5266eb)
- **Border/Divider:** Lead (#70707d)

### Example Component Prompts
1. `Create a hero section with a full-bleed atmospheric mountain photo background. Center a display headline: 65px arcadiaDisplay weight 360, color Starlight (#ededf3). Below it, add a sub-headline: 21px arcadia weight 400, color Starlight. Finally, add a CTA button group: an email input with a 32px left radius joined to a primary pill button with a 32px radius.`

2. `Build a primary action button with the text 'Open account'. The button should have a 'Mercury Blue' (#5266eb) background, 'Pure White' (#ffffff) text, a 32px corner radius, and 16px 24px padding. Font is 16px arcadia weight 480.`

3. `Design a feature list section on a 'Midnight Slate' (#1e1e2a) background. Each item is a link with heading text at 28px arcadiaDisplay weight 480 in 'Starlight' (#ededf3), with a 1px solid bottom border in 'Lead' (#70707d) with 24px of bottom padding.`

---
_Source: https://styles.refero.design/style/3172cd4d-118a-4a16-a259-6b634d32322e_
