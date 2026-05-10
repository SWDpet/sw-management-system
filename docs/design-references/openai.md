# OpenAI — Design Reference

> Blank page before the first word — a design that treats white space as the most powerful element, reserving all color for user-generated and editorial content.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://openai.com](https://openai.com) |
| Refero page | [https://styles.refero.design/style/dc541737-8bf2-4b31-b729-0352f696e82f](https://styles.refero.design/style/dc541737-8bf2-4b31-b729-0352f696e82f) |
| Theme | light |
| Industry | ai |

## Overview

OpenAI.com reads like a blank page waiting to be written on — pure white, near-zero chromatic saturation (1%), and typography that does everything. The custom OpenAI Sans carries the entire visual weight: tightly tracked at -0.03em for large display text, it condenses space so headlines feel carved rather than set. Black (#000000) and border-gray (#e5e7eb) are the only tools; no accent colors, no gradients on the core UI, no decorative illustration. Color arrives exclusively through editorial imagery — soft-focus flower macros, pastel gradient thumbnail cards — making those images feel explosive against the white canvas. The signature tension is 9999px pills for interactive chips and inputs sitting inside a layout where cards use a very specific 6.08px radius, creating a system that pairs one extreme roundness with one precise near-flat radius.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Void | `#000000` | brand | Primary text, nav labels, filled CTA button background, icon fills — the singular chromatic anchor of the entire system |
| Fog Border | `#e5e7eb` | neutral | All dividing lines, card outlines, input borders, nav underlines — the lightest possible mark that still reads as a separator on white |
| Chalk | `#f1f1f1` | neutral | Hover-state button backgrounds, subtle surface fills — one step off pure white without introducing warmth |
| Graphite | `#666666` | neutral | Supporting body text, icon strokes, secondary labels — muted but still readable |
| Ash | `#8f8f8f` | neutral | Tertiary labels, disabled states, fine-grain icon strokes |
| Canvas | `#ffffff` | neutral | Page background, card surfaces, all primary surfaces — absolute white with no warm or cool tint |

## Typography

### OpenAI Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 13px, 14px, 16px, 17px, 18px, 22px, 28px, 48px |
| lineHeight | 1.00–1.65 (tighter at large sizes ~1.16, looser at body ~1.50–1.65) |
| letterSpacing | -0.03em at display sizes (48px), -0.01em at mid sizes, +0.011em at smallest sizes (13px caps/tags) |
| fontFeatureSettings | "calt", "liga" |
| substitute | Inter, DM Sans |
| role | The single typeface for the entire site across every context — nav, body, headlines, buttons, inputs. At 48px display it runs at roughly -0.03em letter-spacing, making large text feel compressed and precise rather than airy. Weight 600 for headlines, 500 for UI labels, 400 for body. The custom cuts provide 'calt' and 'liga' features for text composition. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.64 |  |
| heading | 22 |  | 1.26 |  |
| heading-lg | 28 |  | 1.21 |  |
| display | 48 |  | 1.16 | -1.44 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 6.08px |
| chips | 9999px |
| input | 9999px |
| links | 4px |
| buttons | 9999px |
| softButton | 40px |

- **elementGap** — 8-16px
- **sectionGap** — 64-80px
- **cardPadding** — 32px
- **pageMaxWidth** — 1200px

## Components

### Conversational Input with Category Chips

### Editorial News Card — Featured + Sidebar Stack

### Button Group — Primary + Ghost + Soft Chips

### Primary Navigation Bar

**Role:** Top-level site navigation

White background, 64px tall. OpenAI logo left-aligned. Nav links in OpenAI Sans 14px weight 500, #000000, no underline, zero border-radius. 'Log in' ghost button with rgba(0,0,0,0.12) border, 9999px radius, 12px horizontal padding. 'Try ChatGPT' filled pill button: #000000 background, #ffffff text, 9999px radius, 12px horizontal padding. Search icon right of nav links. No sticky shadow — only #e5e7eb bottom border on scroll.

### Filled Pill CTA Button

**Role:** Primary call-to-action

#000000 background, #ffffff text, OpenAI Sans 14px weight 500, 9999px border-radius, 10px vertical padding, 16px horizontal padding. External link indicator (arrow icon) inline with label. Box-shadow: rgba(0,0,0,0.02) 0px 4px 6px, rgba(0,0,0,0.05) 0px 0px 2px — almost invisible elevation.

### Ghost Pill Button

**Role:** Secondary actions, navigation chips

Transparent background, #000000 text, rgba(0,0,0,0.12) border, 9999px radius, 10px vertical padding, 12px horizontal padding. Hover state shifts background to #f1f1f1.

### Soft Rounded Button

**Role:** Feature category chips (Search with ChatGPT, Talk with ChatGPT, Sora)

rgba(0,0,0,0.04) background, #000000 text, OpenAI Sans 14px weight 500, 40px border-radius, 8px vertical padding, 16px horizontal padding. Separated by #e5e7eb borders between group items.

### Conversational Input

**Role:** ChatGPT-style prompt entry field

Transparent background, #000000 text, #e5e7eb border, 9999px border-radius, 10px vertical padding, 24px right padding, 52px left padding (icon offset). Placeholder text in #666666. Submit arrow button in bottom-right corner. Full-width within a centered container max ~640px.

### Editorial News Card

**Role:** Article thumbnails in news/stories grids

Transparent background, 6.08px border-radius, no box-shadow, 0px padding. Full-bleed thumbnail image on top with 6.08px radius clipping. Below: category label in OpenAI Sans 13px weight 500 #666666 with letter-spacing +0.011em, headline in 18-22px weight 600 #000000, read-time in 13px weight 400 #666666. No card border — image and type float on white.

### Image Overlay Badge

**Role:** Model or product name displayed over hero images

#ffffff background, 9999px border-radius (very large ~24px for readable badge). Text in OpenAI Sans weight 600 — bold portion in #000000, variable portion in #666666. Appears centered over editorial hero image with subtle shadow: rgba(0,0,0,0.05) 0px 2px 4px.

### Nav Text Link

**Role:** Top-level navigation items

Transparent background, #000000 text, OpenAI Sans 14px weight 500, borderRadius 0px, no padding. Hover state adds underline. No border. Active state unspecified — likely underline weight change.

### Inline Text Link

**Role:** Body copy hyperlinks and 'View more' actions

#000000 text, OpenAI Sans 16px weight 400, 4px border-radius on focus ring. Underline on hover. No background color. 'View more' uses no underline at rest, underline on hover.

### Category Label Badge

**Role:** Content taxonomy tags (Product, Company, Research)

No background, no border. OpenAI Sans 13px weight 500, letter-spacing +0.011em, #666666 color. Uppercase or title-case. Placed inline before read-time, separated by a middot or space.

### Footer Column Block

**Role:** Site footer navigation

White background, full-width. Column headers in OpenAI Sans 13px weight 600 #000000, letter-spacing +0.011em. Links in 13px weight 400 #666666. Row gap 8px between links. Column gap 64px between groups. Top border #e5e7eb 1px.

## Layout

Max-width centered layout (~1200px) on a pure white canvas. Hero section is minimal-centered: headline at 48px centered horizontally, input box centered below, chip buttons centered below that — no hero image, no background treatment. Below hero: asymmetric two-column editorial grid (large featured card left ~60% width, vertical stack of smaller cards right ~35%). Further sections use consistent top-to-bottom stacking with 64-80px section gaps. Navigation is a fixed top bar at 64px, logo left, links center-left, CTAs right. Footer is a multi-column link grid. No alternating dark/light bands — the entire page is white with content as the only visual differentiation.

## Imagery

Editorial photography is the only source of color on the page — soft-focus macro flower shots in warm oranges/pinks, pastel gradient abstract tiles for sidebar cards in blue/purple/teal. These images are contained within 6.08px rounded-corner tiles, never full-bleed on the page. No lifestyle photography, no people, no UI screenshots in news cards. The color in images feels deliberate and curated — always soft, always gradient-adjacent, never harsh or literal. Icons are monochrome: outlined/filled in #000000 or #666666, 1.5px apparent stroke weight. Image density is low — images appear only in editorial card grids, leaving vast white space across the page.

## Elevation philosophy

Elevation is functionally absent. The only shadow in the system — rgba(0,0,0,0.02) 0px 4px 6px, rgba(0,0,0,0.05) 0px 0px 2px — appears on the CTA button and is so faint it reads as a print artifact rather than depth. Cards have no shadow; separation comes from whitespace and the 6.08px image clip radius, not shadow stacking. This is a flat surface philosophy where z-axis is communicated through opacity and spatial distance, not shadow.

## Dos & Donts

### Do

- Use #000000 as the only filled button background color — no colored CTAs
- Apply 9999px border-radius to all pill buttons, ghost buttons, and the conversational input field
- Use 6.08px border-radius on all image-containing card elements and thumbnail clips
- Set display headlines (48px) with letter-spacing -0.03em; omit tracking overrides below 22px unless using caps labels
- Use #e5e7eb as the only border/divider color — never darken it or tint it
- Introduce color only through photography or editorial imagery — never through UI backgrounds or button fills
- Maintain minimum 64px vertical gap between page sections

### Don't

- Do not use any colored accent (blue, green, orange) on interactive elements or backgrounds
- Do not apply box-shadows to cards — separation comes from whitespace, not depth
- Do not mix border-radius values between pill (9999px) and card (6.08px) contexts — the contrast is intentional and the values must be exact
- Do not use weight below 400 or above 600 in OpenAI Sans — the 400/500/600 triad defines all typographic hierarchy
- Do not use background colors on section blocks — the page stays #ffffff wall-to-wall
- Do not add letter-spacing at body sizes (16-18px) — tracking is only for display (negative) and caps labels (positive +0.011em)
- Do not use more than two typographic colors: #000000 for primary and #666666 for secondary — #8f8f8f is reserved for disabled/tertiary only

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- Text primary: #000000
- Text secondary: #666666
- Page background: #ffffff
- All borders/dividers: #e5e7eb
- Button hover surface: #f1f1f1
- CTA button fill: #000000 (text: #ffffff)

**Example Component Prompts**

1. **Hero Section**: Pure white background. Center-aligned headline: OpenAI Sans 48px weight 600, #000000, letter-spacing -1.44px. Conversational input below at max-width 640px: transparent background, #e5e7eb border 1px, 9999px radius, 10px/52px/10px/24px padding (top/left/bottom/right), placeholder text #666666. Below input: row of chip buttons at 40px radius, rgba(0,0,0,0.04) background, #000000 text, 8px/16px padding, OpenAI Sans 14px weight 500.

2. **News Card Grid**: Two-column asymmetric layout. Left card (60% width): image full-width with 6.08px radius, below: category label OpenAI Sans 13px weight 500 #666666 letter-spacing +0.143px, headline 28px weight 600 #000000, read-time 13px weight 400 #666666. Right column: two stacked cards, image full-width 6.08px radius, same label/headline/readtime pattern at 18px headline. No borders, no shadows on cards.

3. **Navigation Bar**: 64px height, white background, #e5e7eb bottom border 1px. Left: OpenAI wordmark. Center-left: nav links OpenAI Sans 14px weight 500 #000000, horizontal gap 24px. Right: 'Log in' button (transparent bg, rgba(0,0,0,0.12) border, 9999px radius, 12px horizontal padding) + 'Try ChatGPT' button (#000000 bg, white text, 9999px radius, 10px vertical / 16px horizontal padding, arrow icon right).

4. **Category Label + Read Time Inline**: OpenAI Sans 13px weight 500, #666666, letter-spacing +0.143px. Format: 'Product · 16 min read' — category runs into a centered-dot separator then read time, all same color and size, no background.

5. **Footer**: White background, top border #e5e7eb 1px. Column headers OpenAI Sans 13px weight 600 #000000 letter-spacing +0.143px. Links 13px weight 400 #666666, row-gap 8px. Columns separated by 64px gap.

### Color Through Content

OpenAI.com's UI is intentionally achromatic — 1% colorfulness across the entire token set. Color is exclusively editorial: soft-focus macro photography (orange, pink, coral), and gradient thumbnail tiles (blue-to-purple, teal-to-lavender) appear only inside image containers with 6.08px radius. This means any new page section that needs visual differentiation must do so through typography size/weight changes, whitespace variation, or image selection — never through background color blocks or UI accent colors. Adding even a muted blue CTA would break the system's defining constraint.

### Radius Philosophy

Two radii define the entire shape system: 9999px (pill) and 6.08px (near-square). There is no middle ground. Pills appear on all interactive affordances: buttons, chips, input fields. The 6.08px radius appears on all image containers and card clips — it is precise enough to suggest intentionality (not a generic 8px) but flat enough to read as almost-rectangular. The 40px variant on soft-chip buttons is a transitional form — rounder than cards but not full pill — used only for the feature-mode selector chips. Do not introduce 12px, 16px, or 24px radii into new components.

---
_Source: https://styles.refero.design/style/dc541737-8bf2-4b31-b729-0352f696e82f_
