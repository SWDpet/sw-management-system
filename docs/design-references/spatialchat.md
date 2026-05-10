# SpatialChat — Design Reference

> Violet accented white canvas. Pure white surfaces are the backdrop for precise charcoal text and interactive violet elements, recalling an architect's blueprint highlighted with a single, crucial color.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://spatial.chat](https://spatial.chat) |
| Refero page | [https://styles.refero.design/style/5b90e218-b325-4901-a1c5-ea1134339826](https://styles.refero.design/style/5b90e218-b325-4901-a1c5-ea1134339826) |
| Theme | light |
| Industry | productivity |

## Overview

This design system projects a feeling of modern clarity and understated robustness, achieved through a stark contrast of dark text on clean white surfaces, accentuated by a single vibrant violet. The generous use of space and subtle elevation creates a breathable, organized interface. Rounded corners on interactive elements and cards introduce an approachable softness to an otherwise precise and functional aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#ffffff` | neutral | Page backgrounds, card surfaces, form fields, primary button text — forms the expansive, clean base. |
| Charcoal Black | `#000000` | neutral | Primary text, prominent headings, solid borders on buttons — provides strong contrast and legibility. |
| Deep Space Charcoal | `#030712` | neutral | Main headings, important text, menu items — slightly softer than pure black, but still commanding. |
| Slate Gray | `#4b5563` | neutral | Secondary text, descriptive body copy, input placeholders — provides visual hierarchy without competing with primary text. |
| Whisper Gray | `#f9fafb` | neutral | Subtle background for UI elements, light separation layers — provides slight visual depth from pure white. |
| Fog | `#e5e7eb` | neutral | Default input borders, subtle dividers — defines boundaries gently. |
| Periwinkle Mist | `#f2f2ff` | neutral | Background for accent badges, subtle highlights — a very light, almost white tint of the primary color. |
| Steel Gray | `#d1d5db` | neutral | Input borders (focused state, or certain variants) — a slightly darker border than Fog. |
| Majestic Violet | `#5727e7` | brand | Primary CTA buttons, active states, accent badges, interactive elements — the sole vibrant brand color, drawing immediate attention. |

## Typography

### Satoshi

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 20px, 24px, 32px, 40px, 44px, 48px, 60px |
| lineHeight | 1.30, 1.38, 1.40, 1.43, 1.50, 1.56, 1.60 |
| letterSpacing | normal |
| substitute | Inter |
| role | The sole typeface, Satoshi, is used across all text elements. Its clean, geometric yet humanist forms provide a modern and friendly professionalism at all sizes. Heading sizes leverage higher weights (500-700) for presence, while body text remains legible at 400. Letter spacing remains neutral for open readability. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |
| body-lg | 18 |  | 1.5 |  |
| subheading | 20 |  | 1.5 |  |
| heading | 24 |  | 1.43 |  |
| heading-lg | 32 |  | 1.38 |  |
| display-sm | 40 |  | 1.38 |  |
| display | 48 |  | 1.3 |  |
| display-lg | 60 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| badges | 8px |
| inputs | 8px |
| buttons | 12px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 12-24px
- **pageMaxWidth** — 

## Components

### Hero CTA Button Group with Social Proof

### Feature Section — Spatial Interaction

### Announcement Banner + Badge Group

### Primary Action Button

**Role:** Main call to action

Solid Majestic Violet (#5727e7) background, Absolute Zero (#ffffff) text, 12px border radius, 12px 15px padding. This button stands out as the primary interactive element.

### Outline Accent Button

**Role:** Secondary action or navigation

Transparent background, Charcoal Black (#000000) text and 1px border, 12px border radius, 12px 15px padding. Used for alternative actions where emphasis is not as high as primary.

### Dark Outline Neutral Button

**Role:** Tertiary action, often in headers

Transparent background, Charcoal Black (#222222) text and 1px border, 8px border radius, 15px padding. A more prominent outline button used for login/demo in the header area.

### Hero White Button

**Role:** Prominent utility button in hero

Absolute Zero (#ffffff) background, Charcoal Black (#000000) text, 12px border radius, 12px 24px padding. Used for high-visibility secondary actions within hero sections.

### Default Input Field

**Role:** Standard text input

Absolute Zero (#ffffff) background, Slate Gray (#6b7280) placeholder text, 1px Fog (#e5e7eb) border, 8px border radius, 12px padding all around. Clean and unobtrusive.

### Premium Input Field

**Role:** Enhanced or larger text input

Absolute Zero (#ffffff) background, Slate Gray (#6b7280) placeholder text, 1px Fog (#e5e7eb) border, 14px border radius, 18px padding vertical, 16px horizontal. Features a slightly larger size and more rounded corners.

### Violet New Badge

**Role:** Indicator for new features/items

Solid Majestic Violet (#5727e7) background, Absolute Zero (#ffffff) text, 8px border radius, 6px 10px padding. Boldly highlights new content.

### Periwinkle Info Badge

**Role:** General informational tag

Periwinkle Mist (#f2f2ff) background, Deep Space Charcoal (#030712) text, 8px border radius, 6px 8px padding. A softer, background-compatible tag for information.

### Outline Subtle Badge

**Role:** Contextual, less obtrusive tag

Transparent background, RGBA(51, 51, 51, 0.8) text, 8px border radius, 8px padding all around. Used for informational tags or categories where a background is not desired.

### Client Logo Card

**Role:** Display partner or client logos

Absolute Zero (#ffffff) background, 12px border radius, with a subtle rgba(0, 0, 0, 0.1) box-shadow. Standard padding for contained logos. Used in grids.

## Layout

The page primarily uses a max-width contained layout, centered on the screen, providing ample negative space on wider viewports. The hero section is full-width with a centered headline and subtext, followed by centered CTA buttons. Content sections typically alternate between a two-column layout of text on one side and an illustrative product screenshot on the other, or feature grids like the 3-column client logo display. Vertical rhythm is established by consistent section gaps, creating distinct, breathable content blocks. Navigation is a fixed top bar with a left-aligned logo and right-aligned action buttons, maintaining visibility throughout scrolling. The layout emphasizes clarity and easy scannability of information.

## Imagery

The visual language focuses on a mix of product screenshots, abstract geometric graphics, and crisp, minimal icons. Product screenshots are contained within rounded frames (12px radius) and sometimes feature subtle elevation via shadows, showing the interface clearly and often in a context of use (e.g., small user profile circles). Illustration is minimal, leaning towards very abstract, simple forms or clean line art. Icons are primarily outlined or solid in achromatic colors, with occasionally Majestic Violet accents for interactive states. The overall role of imagery is explanatory and showcases the product's functionality in a clean, non-distracting manner, rather than decorative high-key photography.

## Dos & Donts

### Do

- Use Deep Space Charcoal (#030712) for all primary headings and key text to maintain visual impact.
- Apply Majestic Violet (#5727e7) exclusively to primary call-to-action buttons and critical interactive elements.
- Prioritize Absolute Zero (#ffffff) for all main backgrounds and card surfaces to ensure visual spaciousness.
- Maintain 12px border radius for all buttons and image containers, with 8px for badges and inputs, and 16px for cards.
- Utilize Clear Shadow Subtle (rgba(0,0,0,0.06) 0px 1px 2px 0px) for buttons or very light elevation, and Large Shadow Subtle (rgba(0, 0, 0, 0.06) 0px 4px 28px 0px) for cards for visible yet soft depth.
- Employ Satoshi font with normal letter spacing across all text for a consistent, accessible typographic voice.

### Don't

- Do not introduce additional vibrant colors; the system relies on a focused palette of neutrals and a single Majestic Violet accent.
- Avoid using hard, sharp corners; all interactive and container elements should adhere to the established radii.
- Do not deviate from the Satoshi typeface or its specified weights; it is the brand's sole typographic identity.
- Refrain from heavy, dark shadows; elevation is achieved through subtle, light shadow effects.
- Do not cluster elements without adequate spacing; maintain generous internal padding (12px, 15px) and external spacing (16px, 24px) for visual comfort.
- Do not use highly saturated colorful icons; all icons should be achromatic or carry the Majestic Violet accent.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): #030712
- Background (Page): #ffffff
- CTA (Button): #5727e7
- Border (Input): #e5e7eb
- Accent (Badge background): #f2f2ff

### 3-5 Example Component Prompts
1. Create a Hero Section: Absolute Zero (#ffffff) background. Centered primary heading 'SpatialChat is an All-in-One Virtual Events & Meetings Platform' (Satoshi, 60px, weight 700, #030712, lineHeight 1.3). Below it, centered body text 'Create an engaging virtual environment where attendees can connect, interact, and move freely just like a real-life event.' (Satoshi, 18px, weight 400, #4b5563, lineHeight 1.5). Two centered buttons below: Primary Action Button ('Book a Demo'), and Outline Accent Button ('Try it Free ->', text #000000).
2. Design a Client Logo Card: Create a card with an Absolute Zero (#ffffff) background, 16px border radius, and the shadow 'rgba(0, 0, 0, 0.06) 0px 4px 28px 0px'. Inside, center a placeholder logo with 12px padding vertically and 24px horizontally. Label this 'ClientLogoCard' for a grid layout.
3. Make a Default Input Field: Use Absolute Zero (#ffffff) background, 1px Fog (#e5e7eb) border, 8px border radius. Placeholder text 'Enter your email' should be Slate Gray (#6b7280) and use Satoshi 16px weight 400. Apply 12px padding all around.
4. Build a Majestic Violet New Badge: Utilize a Majestic Violet (#5727e7) background, with Absolute Zero (#ffffff) text 'New'. Set 8px border radius, and 6px 10px padding.

---
_Source: https://styles.refero.design/style/5b90e218-b325-4901-a1c5-ea1134339826_
