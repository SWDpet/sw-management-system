# Relate — Design Reference

> Blueprint on frosted linen — Inter headlines with tight negative tracking over a pale blue-white canvas, product UI surfacing as quiet white cards, the single vivid blue accent used like a highlighter pen rather than paint.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.relate.so](https://www.relate.so) |
| Refero page | [https://styles.refero.design/style/337ade6a-4bae-49ba-b4aa-8994ac805a81](https://styles.refero.design/style/337ade6a-4bae-49ba-b4aa-8994ac805a81) |
| Theme | light |
| Industry | saas |

## Overview

Relate uses a light, airy canvas with a near-white #fcfcfc base that gives the product a paper-like stillness. The hero section features a soft blue-lavender radial wash that bleeds into white, making the product feel approachable without relying on heavy gradients. Typography is Inter-first with aggressive negative tracking at display sizes — headlines tighten toward -2px at 80px, conveying focus and density without visual noise. The accent blue #145aff appears as inline text color and focused-element emphasis rather than filled button backgrounds, so the interface stays quiet and the brand color reads as a signal, not decoration. Product screenshots embedded in rounded cards with 8px radius and feather-light shadows (rgba(0,0,0,0.1)) create the impression of a real app hovering just above the page surface.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Linen Canvas | `#fcfcfc` | neutral | Primary page background and default card surface — the near-white that reads as paper rather than pure white, reducing harshness on body-length reading |
| Sky Wash | `#f0f4fe` | neutral | Subtle blue-tinted section backgrounds, secondary card fills, and hero gradient endpoint — the one step between canvas and pure white that creates soft depth |
| Midnight Ink | `#020520` | neutral | Primary hero and display headline color — the deepest text tone on light backgrounds, with a slight violet shift that prevents flat-black harshness |
| Graphite | `#14141e` | neutral | Secondary headline and body text at high prominence — slightly lighter than Midnight Ink, used for section headings and strong body content |
| Slate | `#374151` | neutral | Product UI labels, sidebar navigation text, and medium-emphasis body content inside app screenshots |
| Ash | `#696a72` | neutral | Secondary body text, subheadings, and caption-level content that needs to recede behind primary text |
| Fog | `#95959b` | neutral | Placeholder text, disabled states, timestamp labels, and tertiary metadata |
| Steel | `#6b7280` | neutral | Muted helper text inside product cards and customer company descriptions |
| Signal Blue | `#145aff` | brand | Violet outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |
| Periwinkle Glow | `#b6cbfd` | accent | Hero background wash and decorative shadow bloom — the diffused halo that gives the hero section its soft blue atmosphere |
| Hero Blue Fade | `#3b82f6` | accent | Blue outline accent for tags, dividers, and focused UI edges |
| Emerald Status | `#16ca2e` | accent | Green outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Coral Alert | `#f26052` | accent | Red outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Azure Info | `#0099ff` | accent | Blue supporting accent for decorative details and low-frequency emphasis. Use as a supporting accent, not as a status color |
| Amber Tag | `#ffa64d` | accent | Orange outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Primary Action Accent | `#0f1f3d` | brand | Violet accent for outlined action borders, linked labels, and lightweight interactive emphasis. Use as the primary outlined action border and text |

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
| weight | 400, 500, 600 |
| sizes | 10px, 12px, 14px, 15px, 16px, 18px, 20px, 22px, 40px, 56px, 80px |
| lineHeight | 1.00–1.50 (tighter at display, looser at body) |
| letterSpacing | -2.96px at 80px, -1.51px at 56px, -0.76px at 40px; slightly positive (+0.09–0.21px) at 10–14px caption/label sizes |
| fontFeatureSettings | normal |
| substitute | Inter (Google Fonts) |
| role | Primary typeface for all headings, body, navigation, and buttons. The signature move is aggressive negative letter-spacing at display sizes: -2.96px at 80px, -1.51px at 56px, -0.76px at 40px — headlines compress toward the left edge while body text runs at normal or slightly positive tracking. This creates two distinct visual registers: dense display and airy body. |

### Pretendard

| Key | Value |
| --- | --- |
| weight | 400 (Regular, Medium, Bold variants) |
| sizes | 12px, 14px, 15px, 16px, 18px |
| lineHeight | 1.00–1.71 |
| letterSpacing | +0.23–0.43px across all sizes |
| substitute | Noto Sans KR, SUIT |
| role | Korean-language body and UI text used in the product screenshots and localized content. Slightly positive tracking (0.019–0.03em) to compensate for CJK glyph density. Present in the product UI cards, not the marketing copy. |

### Roboto Mono

| Key | Value |
| --- | --- |
| weight | 500, 700 |
| sizes | 12px, 22px |
| lineHeight | 1.00–1.27 |
| letterSpacing | -0.99px at 22px, -0.54px at 12px |
| substitute | JetBrains Mono, IBM Plex Mono |
| role | Monospaced accent font for deal values, pipeline counts, and data labels — the tight -0.054em tracking at 22px keeps numeric displays compact. |

### Font Awesome 6 Pro

| Key | Value |
| --- | --- |
| weight | 400 (Light, Regular, Solid variants) |
| sizes | 8px–18px |
| lineHeight | 1.00–2.50 |
| substitute | Heroicons, Lucide Icons |
| role | Icon system used throughout product UI and navigation. Light variant at 8–12px for dense UI rows, Solid variant for emphasis. Monochromatic, matching surrounding text color. |

### Font Awesome 6 Pro Solid

| Key | Value |
| --- | --- |
| weight | 400, 900 |
| sizes | 8px, 10px, 11px |
| lineHeight | 1, 1.09, 1.25 |
| role | Font Awesome 6 Pro Solid — detected in extracted data but not described by AI |

### Font Awesome 6 Pro Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 11px, 12px, 14px, 18px |
| lineHeight | 1, 1.09, 1.2 |
| letterSpacing | 0.011 |
| role | Font Awesome 6 Pro Regular — detected in extracted data but not described by AI |

### Font Awesome 6 Brands Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 18px |
| lineHeight | 1 |
| role | Font Awesome 6 Brands Regular — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.2 | 0.13 |
| body | 14 |  | 1.43 | 0.06 |
| heading-sm | 18 |  | 1.4 | -0.16 |
| heading | 22 |  | 1.25 | -0.22 |
| heading-lg | 40 |  | 1.08 | -0.76 |
| display | 56 |  | 1.05 | -1.51 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 100px |
| cards | 8px |
| badges | 4px |
| images | 16px |
| inputs | 12px |
| modals | 32px |
| buttons | 12px |
| cardLarge | 40px |

- **elementGap** — 8-12px
- **sectionGap** — 80px
- **cardPadding** — 
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#fcfcfc` | 0 | Page background — the base layer everything sits on |
| Tinted Wash | `#f0f4fe` | 1 | Alternate section backgrounds and hero gradient zone — one step above canvas with a blue tint |
| Card Surface | `#ffffff` | 2 | Product UI cards, feature cards, and elevated content containers with subtle shadow |
| Overlay | `#fcfcfc` | 3 | Large feature cards with 40px radius (rgba(252,252,252,0.2) for glassmorphic hero overlays) |

## Components

### Primary Outlined Button

**Role:** Main call-to-action navigation button (Book a demo, Get started free outline variant)

Background rgba(255,255,255,0.8), text #0f1f3d, border 1px solid #0f1f3d, border-radius 12px, padding 15px. Inter 500 at 14–16px. The outlined style makes this the de-facto CTA without using a filled colored background — authority comes from the sharp dark border against the light surface.

### Primary Filled Button

**Role:** High-emphasis action (Get started free, filled navbar variant)

Background linear-gradient(rgb(59,130,246) 0%, rgb(20,90,255) 100%), text #ffffff, border-radius 12px, padding 15px 20px. Inter 600 at 14px. The blue gradient fill is the only filled button on the page, reserved for the single top-priority action.

### Hero Ghost Button

**Role:** Secondary CTA inside the hero gradient zone

Background #fcfcfc, text #145aff (note: this is the browser-artifact-adjacent value — use #0f1f3d for non-link text), border 1px solid with blue tint, border-radius 50px pill shape, padding 14px 32px. Appears as a pill over the soft blue hero wash. Inter 500 at 16px.

### Compact Product Card

**Role:** CRM pipeline deal rows, contact list items, and inbox entries

Background #ffffff, border-radius 8px, box-shadow rgba(0,0,0,0.1) 0px 0px 4px -2px, padding 12px top/bottom and 16px left. Contains company logo (20px circle), company name in Inter 500 14px #14141, deal value in Roboto Mono 500 12px, and last-activity note in Inter 400 12px #6b7280.

### Large Feature Section Card

**Role:** Marketing page feature showcase blocks with embedded product screenshot

Background #fcfcfc, border-radius 40px, multi-layer shadow (0.082 → 0.07 → 0.016 opacity cascade for a floating feel), padding 52px top/bottom and 72px left/right. Houses product screenshot images with 16px radius inside the card.

### Glass Overlay Card

**Role:** Floating UI elements layered over hero gradient or colored backgrounds

Background rgba(252,252,252,0.2), border-radius 28px (standard) or 48px (larger variant), no box-shadow, backdrop-filter blur(5–15px). Text inherits from container. Used for frosted-glass UI previews in hero and feature sections.

### Navigation Bar

**Role:** Top-level site navigation

Background #fcfcfc with backdrop-filter blur(2px), full-width, 0–90px height zone. Logo left, nav links center (Inter 400–500 14px #374151), auth actions right. Nav links use 100px pill radius on hover states. 'Book a demo' = outlined 12px-radius button, 'Get started free' = filled gradient button.

### Sidebar Navigation Item

**Role:** Product UI left-rail navigation (People, Pipelines, Views)

Inter 400 14px, text #374151 default, active state uses #14141 weight 500 with a 100px pill background at #f0f4fe. Icons via Font Awesome 6 Pro Light at 12px. Row height 32px, gap 2–4px between items.

### Status Badge

**Role:** Deal stage, contact status, and pipeline state indicators

Background tint of semantic color (Emerald #16ca2 at 10% opacity, Coral #f26052 at 10% opacity, Azure #3b82f6 at 10% opacity), text at full semantic color, border-radius 4px, padding 2px 6px. Inter 500 12px. Compact single-word labels: 'Lost', 'Prospecting', 'Active'.

### Text Input

**Role:** Form fields and search inputs

Background rgba(255,255,255,0.08) on dark contexts or #ffffff on light, border 1px solid #cfcfcf default / #0099ff focus ring (box-shadow rgba(0,153,255,1) 0px 0px 0px 3px), border-radius 12px, padding 15px. Inter 400 14px #14141e. Placeholder in #95959b.

### Y Combinator Trust Badge

**Role:** Social proof label above hero headline

Small inline pill: YC orange square icon + 'Backed by Y Combinator' text in Inter 500 12px #374151. Background transparent or very light, no border, acts as a micro-social-proof element that anchors before the headline lands.

### Customer Logo Strip

**Role:** Social proof logo grid in grayscale

Customer/partner logos rendered in #696a72 or near-black at reduced opacity (grayscale filter). Arranged in 4–5 column grid. No borders, no cards — logos float on the canvas. Spacing 32–48px between logos.

## Layout

Max-width ~1200px centered on the viewport with generous side padding. The hero is a centered-stack pattern: YC badge → large display headline (two lines, centered) → subtext paragraph → CTA button, all vertically stacked over the blue radial background wash. The hero CTA floats above a partially visible product screenshot that bleeds into the next section, creating a scroll incentive. Sections below alternate between full-white bands and pale blue (#f0f4fe) tinted bands. Feature sections use a centered headline + subtext above a single large card (40px radius, 52/72px padding) containing a full-width product screenshot — no two-column text+image splits. The customer logo strip is a flat two-row grid of grayscale logos. The pipeline demo section uses a 3–4 column product kanban visible through a cropped viewport card. Navigation is a sticky top bar with blur backdrop, logo-left, links-center, actions-right layout. Density alternates: the nav and product cards are compact (8–12px gaps), while marketing sections breathe at 80px vertical gaps between them.

## Imagery

Relate's visual language centers entirely on product screenshots — no photography, no abstract illustration, no lifestyle imagery. The product UI is the hero visual: CRM pipeline kanban columns, inbox message threads, and contact detail panels are captured at high resolution and embedded inside large rounded cards (40px radius) with feather-light shadows. Screenshots are contained within the card boundaries with 16px inner radius, never full-bleed. The product captures use realistic data (company names like Figma, Framer, Stripe, Deel) to read as authentic product usage rather than designed mockups. Icons throughout the UI use Font Awesome 6 Pro Light — thin-stroked, monochrome, matched to surrounding text color. The overall visual weight is text-dominant with imagery serving as product demonstration rather than decoration; a single screenshot per section carries the entire visual load.

## Elevation philosophy

Elevation is whisper-quiet: the system uses only 4px-spread, near-zero-opacity shadows rather than layered depth stacks. Cards float via rgba(0,0,0,0.1) at 4px spread, not through strong borders or high-contrast outlines. The brand blue #145aff appears in glow shadows (rgba(20,90,255,0.1) 0px 0px 100px) as ambient light rather than hard lift — making elevation feel atmospheric rather than structural.

## Dos & Donts

### Do

- Use Inter with letter-spacing -1.51px at 56px and -2.96px at 80px for all display headlines — the tight tracking is the signature.
- Apply #145aff exclusively as a text accent or focus-ring color, never as a filled button background; the brand blue is a highlighter, not paint.
- Use border-radius 8px for compact product UI cards and 40px for large marketing feature cards — never mix these within the same section.
- Render the hero section background as a soft blue radial wash using #b6cbfd and #f0f4fe, not a solid color or photograph.
- Keep card shadows at rgba(0,0,0,0.1) 0px 0px 4px -2px — the near-invisible shadow creates lift without drama.
- Use #fcfcfc (not pure #ffffff) as the default page canvas and card background — the slight off-white maintains paper warmth.
- Apply Roboto Mono 500 at 12px with -0.54px letter-spacing for all numeric deal values and pipeline counts inside product UI.

### Don't

- Don't fill any button with a solid #145aff background — that color exists only as text, borders, and focus rings in this system.
- Don't use border-radius values between 8px and 40px for cards — the jump from compact (8px) to large-feature (40px) is intentional; values like 16–24px for cards break the rhythm.
- Don't use font-weight 700 or 800 for headlines — Inter at 600 is the maximum weight; heavier weights fight the negative-tracking elegance.
- Don't use pure #000000 or pure #ffffff for primary text and backgrounds respectively — use #020520/#14141 for text and #fcfcfc for backgrounds.
- Don't add colorful decorative gradients to section backgrounds — only the hero section carries the blue wash; all other sections use flat #fcfcfc or #f0f4fe.
- Don't apply box shadows to navigation items or sidebar elements — elevation lives only on cards and product UI surfaces.
- Don't use semantic colors (#16ca2, #f26052, #ffa64d) outside of status badges and deal-stage labels — they are data-state indicators, not decorative accents.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- text (primary): #020520
- text (secondary): #696a72
- background: #fcfcfc
- border / hairline: #cfcfcf
- accent (inline / focus): #145aff
- primary action: #0f1f3d (outlined action border)

**Example Component Prompts**

1. Create an Outlined Primary Action: Transparent background, #0f1f3d border and text, 9999px radius, compact pill padding. Use it for the main CTA instead of a filled button.

2. **Compact CRM Deal Card**: White (#ffffff) background, border-radius 8px, box-shadow rgba(0,0,0,0.1) 0px 0px 4px -2px, padding 12px 12px 12px 16px. Row: 20px company logo circle, company name Inter 500 14px #14141e, deal value Roboto Mono 500 12px -0.54px tracking #374151, last-activity note Inter 400 12px #6b7280, timestamp Inter 400 10px #95959b right-aligned.

3. **Feature Section Card**: Background #fcfcfc, border-radius 40px, box-shadow 3-layer soft (0.082 → 0.07 → 0.016 opacity), padding 52px top/bottom 72px left/right. Section headline above card: Inter 600 40px #14141e letter-spacing -0.76px centered. Inside card: product screenshot with 16px inner radius, full width.

4. **Outlined Navigation Button (Book a demo)**: Background rgba(255,255,255,0.8), border 1px solid #0f1f3d, border-radius 12px, padding 15px 20px, Inter 500 14px #0f1f3d. No shadow. Sits left of the filled gradient button in the navbar.

5. **Status Badge**: Background color at 10% opacity of semantic color (e.g. rgba(22,202,46,0.1) for active, rgba(242,96,82,0.1) for lost), text at full semantic color, border-radius 4px, padding 2px 6px, Inter 500 12px. Examples: 'Active' in #16ca2e, 'Lost' in #f26052, 'Prospecting' in #3b82f6.

### Gradient System

Three gradient contexts exist in this design system:
1. **Hero Background Wash**: Soft radial bloom using #b6cbfd and #f0f4fe, applied as a background-image behind the hero headline. Stays within the hero zone and fades to #fcfcfc at page edges.
2. **Primary Filled Button**: linear-gradient(rgb(59,130,246) 0%, rgb(20,90,255) 100%) — the blue shift from lighter to deeper blue on the single filled CTA.
3. **Dark Mode Section Gradient** (used sparingly for demo/feature overlays): radial-gradient from #0f1f3d to rgba(0,0,0,0.87) or from #000714 to #0f1f3d — deep navy backgrounds for high-contrast product demo contexts. Never apply to more than one section per page.

---
_Source: https://styles.refero.design/style/337ade6a-4bae-49ba-b4aa-8994ac805a81_
