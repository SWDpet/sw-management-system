# Microsoft — Design Reference

> Retail shelf behind plate glass — products float forward, chrome recedes.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://microsoft.com](https://microsoft.com) |
| Refero page | [https://styles.refero.design/style/5f39e778-d204-42a9-8b8b-a1519dbd3971](https://styles.refero.design/style/5f39e778-d204-42a9-8b8b-a1519dbd3971) |
| Theme | light |
| Industry | other |

## Overview

Microsoft.com reads like a department store catalog translated into pixels — functional, product-forward, and deliberately unshowy. The near-achromatic palette (colorfulness 2%) lets product photography and brand imagery carry all chromatic weight, while the UI itself stays out of the way. A single vivid blue (#0067b8) does everything — links, buttons, borders — appearing nowhere decorative, only interactive. Cards use a dual-layer shadow (rgba(0,0,0,0.13) 3px 7px + rgba(0,0,0,0.11) 1px 2px) against sharp 0px radius corners, a deliberate anti-trend choice that makes the surface feel like printed catalog tiles rather than floated app cards. Segoe UI at weight 400/600 across 11px–37px is the only typeface — no display cuts, no italics, no variable axes — enforcing a utilitarian uniformity across 56 interactive elements in a single header.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Microsoft Blue | `#0067b8` | brand | All interactive elements — links, primary buttons, icon accents. A single saturated hue against a near-grayscale UI creates maximum clickability signal without a second accent color. |
| Deep Azure | `#004a7f` | brand | Hover and active states for links and card link colors — darkened variant of Microsoft Blue creating depth without hue shift. |
| Fluent Icon Blue | `#0078d4` | brand | Card image icon color per CSS token; slightly lighter than Microsoft Blue, used in Fluent UI icon contexts. |
| Amber Badge | `#ffb900` | accent | Badge background (CSS token sc-badge-bg-yellow) with black text — the only warm accent in the system, reserved for promotional or notification badges. |
| Void | `#000000` | neutral | Primary text, borders, SVG fills across heading, body, nav, footer. |
| Graphite | `#171717` | neutral | Ghost button text and border color; near-black for secondary interactive labels. |
| Charcoal | `#262626` | neutral | Body copy, nav text, secondary button labels. |
| Iron | `#333333` | neutral | Tertiary text contexts, muted body copy. |
| Slate | `#616161` | neutral | Secondary nav links, icon strokes, footer body text, caption-level text. |
| Fog | `#f2f2f2` | neutral | Footer background, alternate section backgrounds. |
| White | `#ffffff` | neutral | Page background, card surfaces, button text on filled blue buttons. |

## Typography

### Segoe UI

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| sizes | 11px, 13px, 15px, 16px, 29px, 37px |
| lineHeight | 1.00–1.50 (1.00 at large display sizes, 1.20–1.33 for headings, 1.45–1.50 for body/nav) |
| letterSpacing | normal across all sizes |
| substitute | Segoe UI (system stack: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif) |
| role | Single typeface for all text — nav at 13-15px/400, body at 15-16px/400, card headings at 29px/600, hero headlines at 37px/600. No decorative cuts, no variable-weight axes. The absence of a separate display face makes even 37px headlines feel utilitarian — authority through recurrence, not grandeur. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 16 |  |
| heading | 29 |  | 35 |  |
| display | 37 |  | 37 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 0px |
| badges | 0px |
| inputs | 2px |
| buttons | 2px |

- **elementGap** — 8-16px
- **sectionGap** — 48px
- **cardPadding** — 48px
- **pageMaxWidth** — 1600px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Base | `#ffffff` | 0 | Default page background for all main content sections |
| Fog Surface | `#f2f2f2` | 1 | Footer background and alternate section backgrounds |
| Elevated Card | `#ffffff` | 2 | White product cards with dual-layer drop shadow to lift from page base |

## Components

### Product Cards Grid

### Category Icon Links

### Primary Button Group & Amber Badge

### Primary Filled Button

**Role:** Main CTA across hero and product cards

backgroundColor #0067b8, color #ffffff, borderRadius 2px, padding 10px 12px, no border (transparent). 11 instances sitewide. The 2px radius is the only rounding in the entire system — just enough to soften without becoming pill-shaped. Font: Segoe UI 15px/400.

### Ghost Outline Button

**Role:** Secondary actions, alternative CTAs

backgroundColor transparent, color #171717, border 1px solid #171717, borderRadius 0px, padding 0px 16px. Sharp-cornered, text-height-only padding — visually lighter than filled but still rigid. Font: Segoe UI 15px/400.

### Transparent Card — Zero State

**Role:** Layout containers, hero content panels, image-backed sections

backgroundColor transparent, borderRadius 0px, boxShadow none, padding 0px. Acts as invisible structural grouping — content floats within without visual box. Used 41 times as the dominant card pattern.

### Global Navigation Bar

**Role:** Primary site navigation, persistent header

White background, height 54px, contains Microsoft logo (SVG multicolor), text nav links at 13px/400 #616161, and right-aligned utility icons (Search, Cart, Sign In). 68 interactive elements. Flat — no shadow, no border-bottom. Divides into product nav (left) and utility nav (right).

### Category Icon Link

**Role:** Quick-shop shortcut links below hero carousel

Outlined SVG icon at ~24px above a link label at 13px/400 #0067b8. Transparent background, no border, no card surface. 5 items in a horizontal row. Icon stroke style matches #616161; label uses brand blue to signal clickability.

### Hero Carousel Panel

**Role:** Full-width promotional banner with auto-advancing slides

Full-bleed background image (dark blue for tech products, landscape photography for lifestyle), overlaid white card panel at left (transparent background, no radius, 48px padding) containing headline 37px/600, body 15px/400, and a primary CTA button. Carousel nav uses pagination dots + previous/next arrow buttons + pause control below the slide area.

### Amber Notification Badge

**Role:** Promotional label or new-feature callout

backgroundColor #ffb900, color #000000, borderRadius 0px, padding 2px 8px. Font Segoe UI 11px/400. Used sparingly as the only warm-colored element in the achromatic system — creates immediate visual contrast on white or gray surfaces.

### Footer Multi-Column Nav

**Role:** Site-level footer with category link columns

backgroundColor #f2f2f2, 6-column grid, column headers at 13px/600 #000000, links at 13px/400 #616161 hover #0067b8. No borders between columns, 16px row gap. Social follow icons in a horizontal strip above footer nav.

### Carousel Pagination Control

**Role:** Navigation for hero and mid-page carousels

Row of filled/outlined dot indicators (filled = active, outlined = inactive), flanked by left/right arrow buttons and a pause button. Controls appear below the carousel band on a white background. Arrow and pause buttons use 0px radius, transparent background, #171717 border, padding 15px 9px–16px 20px.

## Layout

Max-width approximately 1600px, centered. Hero is full-bleed with a colored/photo background occupying 100vw, text floated in a left-aligned white overlay panel (~300px wide). Below hero: a 5-column icon shortcut row on white. Then a 4-column product card grid at equal widths with 48px-padded elevated white cards. Mid-page: second full-bleed carousel (lifestyle photo + white left panel). Below: a second feature card or full-bleed branded section. Footer: full-bleed #f2f2f2 with 6-column link nav. Page alternates between full-bleed photo/color bands and white card-grid sections — never gray-on-gray stacking. Navigation is a flat sticky top bar with logo left, product nav center, utility icons right.

## Imagery

Three visual registers coexist: high-production product photography (Surface devices on desaturated blue-gradient backgrounds, Xbox controllers on dark surfaces), full-bleed lifestyle/landscape photography (Edge browser section with aerial lake landscape, warm bokeh backgrounds), and 3D illustration (Microsoft Reflect's fuzzy monster character). Product shots are tight-cropped with environment color matching the brand context — Surface blue, Xbox black, Edge teal. Photography never bleeds into the text card overlay — a white or semi-transparent panel always isolates copy from image. Icons are outlined style, monochrome #616161, approximately 24px, using minimal stroke weight. Image sections alternate with pure-white card grid sections, making imagery feel like editorial breaks rather than continuous texture.

## Elevation philosophy

Elevation appears only on white product cards — the dual-layer shadow (rgba(0,0,0,0.13) 3px 7px + rgba(0,0,0,0.11) 1px 2px) is the sole depth signal. No shadow hierarchy across levels: either a surface has this single card shadow or it has none. Navigation, hero panels, footers, and icon-link rows are all completely flat. The system treats shadow as a product-tile affordance, not a general UI metaphor.

## Dos & Donts

### Do

- Use #0067b8 exclusively for all interactive text links, button backgrounds, and icon accent colors — never apply it decoratively to non-interactive elements.
- Set borderRadius 2px on all buttons and form inputs; use 0px on all card containers, badges, and dividers.
- Apply padding 48px to all elevated white cards (boxShadow rgba(0,0,0,0.13) 0px 3px 7px + rgba(0,0,0,0.11) 0px 1px 2px); transparent container cards get padding 0px and no shadow.
- Use Segoe UI weight 600 only at heading sizes (29px+); all body, nav, caption, and button labels use weight 400.
- Place product photography in full-bleed hero and carousel bands; always isolate copy in a separate white overlay panel rather than compositing text directly over images.
- Limit the amber badge (#ffb900 / #000000 text) to promotional or notification-only contexts — it is the single warm-chromatic element and loses meaning if used broadly.
- Maintain #f2f2f2 as the footer background and any secondary section band; never use a third gray value for backgrounds.

### Don't

- Do not use any border-radius value other than 0px or 2px — rounded cards, pill buttons, or circular badges are outside this system.
- Do not introduce a second typeface — Segoe UI is the only font. No display serifs, no geometric sans, no variable-weight hero text.
- Do not add colored backgrounds to UI sections (other than #f2f2f2 and full-bleed photo bands) — white is the only neutral surface for cards and content areas.
- Do not use #0066ff or any violet-shifted blue as a link or button color — the brand blue is #0067b8 exclusively.
- Do not apply box-shadow to navigation, header, footer, hero panels, or icon rows — shadow belongs only to the elevated white product card.
- Do not use letter-spacing adjustments at any size — Segoe UI renders at normal tracking across all 11px–37px steps.
- Do not add gradient overlays, color washes, or duotone treatments to photography — images appear as captured, with no CSS filter or color-blend layer.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- Text (primary): #000000
- Text (secondary): #616161
- Page background: #ffffff
- Alternate section background: #f2f2f2
- CTA / links: #0067b8
- CTA hover: #004a7f
- Button text (on blue): #ffffff
- Ghost button border/text: #171717
- Promo badge: #ffb900 on #000000

**Example Component Prompts**

1. **Primary CTA Button**: Rectangle, backgroundColor #0067b8, color #ffffff, borderRadius 2px, padding 10px 12px, Segoe UI 15px weight 400, no border. Label: 'Comprar ahora'.

2. **Product Card**: White rectangle (backgroundColor #ffffff), borderRadius 0px, padding 48px, boxShadow rgba(0,0,0,0.13) 0px 3px 7px + rgba(0,0,0,0.11) 0px 1px 2px. Inside: product image top, heading 29px/600 #000000, body copy 15px/400 #262626, then a primary CTA button. No card border.

3. **Hero Section**: Full-bleed band with dark blue gradient or product photo background. Left-aligned white overlay panel (no background color on panel itself, just padding 48px), headline 37px Segoe UI weight 600 #000000, body 15px/400 #262626, primary CTA button. Product image right-aligned within the band.

4. **Category Icon Shortcut Row**: 5-column flex row on white background. Each item: outlined SVG icon ~24px color #616161 centered, below it a link label 13px/400 #0067b8. No card border, no background. 16px gap between icon and label.

5. **Footer**: Full-width band backgroundColor #f2f2f2, 6-column grid. Each column: header label 13px/600 #000000, below it a vertical list of links 13px/400 #616161 (hover #0067b8). No column dividers. 48px top/bottom padding.

---
_Source: https://styles.refero.design/style/5f39e778-d204-42a9-8b8b-a1519dbd3971_
