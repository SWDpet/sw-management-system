# Granola — Design Reference

> Field notes on warm parchment — Granola reads like a well-worn Moleskine open on a sunlit desk: serifed headlines with editorial gravity, organic green accents like ink from a plant-based pen, and hairline-ruled card borders that suggest hand-drawn note margins.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.granola.ai](https://www.granola.ai) |
| Refero page | [https://styles.refero.design/style/d6c2a911-45ed-4860-a992-43df22793c2a](https://styles.refero.design/style/d6c2a911-45ed-4860-a992-43df22793c2a) |
| Theme | light |
| Industry | productivity |

## Overview

Granola pairs a serif editorial headline face (Quadrant) with a humanist mono-inflected UI face (Melange) on a near-white canvas — the tension between old-print warmth and digital utility is the defining atmosphere. The chromatic system is almost entirely olive-green: a single muted-yellow-green (#b2c248 surface, #5b6f00 filled CTA, #788c15 border accents) against hairline-bordered white cards and an off-white page. Everything else is achromatic — dark ink, mid-gray text, and 0.5px solid borders that whisper rather than rule. Typography does the heavy lifting: Quadrant at -0.015em tracking for display sizes drops weight at 68px to feel like editorial print, while Melange's 0.01em tracking at UI sizes keeps body text crisp and warm. The result is a notepad-like surface — quiet, high-contrast text on parchment-adjacent white — punctuated by bursts of organic chartreuse green.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Parchment Canvas | `#f7f7f2` | neutral | Page background and disabled input surfaces — the barely-warm off-white that reads as paper, not screen |
| Pure Surface | `#ffffff` | neutral | Card backgrounds, nav surface, input fills, elevated overlays |
| Midnight Ink | `#0e0f0c` | neutral | Primary heading and body text — near-black with the faintest warm undertone that keeps it from feeling harsh |
| Deep Charcoal | `#292929` | neutral | Nav text, footer text, secondary headings, filled icon fills; Decorative gradient on AI-enhanced copy indicators — transitions from dark charcoal through amber, pink, lavender, sky, chartreuse, then fades transparent |
| Carbon | `#454745` | neutral | Medium-emphasis link and body text, supporting secondary labels |
| Slate | `#72726e` | neutral | Tertiary nav labels, metadata, muted helper text, secondary icon fills |
| Graphite | `#818179` | neutral | List secondary text, overlay-context captions |
| Ash Border | `#e3e3e3` | neutral | Hairline dividers, card borders, rule lines across all UI contexts — the dominant structural border color |
| Stone Border | `#d5d5d2` | neutral | Button borders on white-fill controls, card-level contained borders |
| Fog Surface | `#eaebe5` | neutral | Soft hover state fills, subtle input surface overlays |
| Olive Press | `#b2c248` | brand | Large decorative accent surfaces in product UI mockups — the vivid chartreuse that signals AI-enhanced state |
| Forest CTA | `#5b6f00` | brand | Primary download/action button fill — deep olive-green that feels botanical and grounded against white, not techy |
| Fern Border | `#788c15` | brand | Accent borders, focus-adjacent decorative strokes, icon tints in the green family |
| Sprout Tint | `#e5eacd` | brand | Secondary pill button backgrounds (announcement badge, iPhone download) — the lightest olive wash |
| Twitter Blue | `#1d9bf0` | accent | Social proof badge icons, X/Twitter brand color used in testimonial cards |
| Link Cobalt | `#006fd6` | accent | Inline text links within body copy and social testimonial cards |
| Ember Fill | `#e95d3d` | accent | Orange wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Ember Border | `#bd4a30` | accent | Orange outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |

## Typography

### Melange

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600 |
| sizes | 13px, 14px, 15px, 16px, 20px, 24px |
| lineHeight | 1.00–1.50 |
| letterSpacing | 0.01em across all UI sizes |
| fontFeatureSettings | normal |
| substitute | DM Sans, Instrument Sans |
| role | All UI text: navigation, body copy, buttons, labels, card content, footer. Its 0.01em tracking at 13-16px sizes keeps small text from collapsing — the slightly wider-than-normal tracking is the fingerprint of this face at body sizes. Weight 300 appears for light supporting text, 600 for active nav states and strong labels. |

### Quadrant

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 18px, 20px, 24px, 36px, 48px, 68px |
| lineHeight | 1.00–1.40 |
| letterSpacing | -0.015em at display sizes (68px, 48px), +0.01em at smaller heading sizes |
| substitute | Playfair Display, Lora |
| role | All display headings and hero text. The sole serif in the system — its old-style proportions at 68px with -0.015em tracking give headlines editorial gravity that sans-serif alternatives cannot replicate. At smaller sizes (16-24px) it serves section subheadings with the same print warmth. Weight 400 throughout — heavier weights would destroy the handwritten-note quality. |

### -apple-system / system-ui

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| sizes | 14px, 15px, 16px, 20px |
| lineHeight | 1.14–1.50 |
| letterSpacing | normal |
| substitute | Inter, system-ui |
| role | Fallback and embedded-content contexts (social card content, badge text, Twitter embed surfaces). Not a deliberate brand choice — follows OS defaults in embedded third-party content zones. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 13px, 16px |
| lineHeight | 1.23, 1.5 |
| letterSpacing | 0.01 |
| role | system-ui — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.23 | 0.13 |
| heading-sm | 20 |  | 1.33 | 0.2 |
| heading | 36 |  | 1.25 | -0.54 |
| heading-lg | 48 |  | 1.11 | -0.72 |
| display | 68 |  | 1 | -1.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| inputs | 8px |
| buttons | 9999px |
| cardsXL | 16px |
| tooltips | 4px |
| pillBadge | 9999px |
| cardsLarge | 12px |
| decorativeBlobs | 64px |

- **elementGap** — 16px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#f7f7f2` | 0 | Page background — warm off-white, never pure white at page level |
| Card | `#ffffff` | 1 | White card surfaces, nav background, input fill backgrounds |
| Tinted Surface | `#eaebe5` | 2 | Hover fills, soft opaque states, subtle inset surfaces |
| Accent Tint | `#e5eacd` | 3 | Sprout-tinted button backgrounds for secondary actions and announcement pills |

## Components

### Primary Download Button

**Role:** Main CTA for app download actions

Background #5b6f00 (Forest CTA), text #ffffff or #fcfcf8, font Melange 16px weight 500, radius 9999px, padding 10px 20px. Hover shifts to #4c5616. Contains Apple logo icon inline-left. The deep olive against white pages is the brand's primary interaction fingerprint.

### Announcement Pill Button

**Role:** Secondary/news banner linked badge

Background #e5eacd (Sprout Tint), text #0e0f0c, border 1px solid #d5d5d2, radius 9999px, padding 8px 16px, Melange 14px weight 400. Trailing arrow icon. Used for 'Announcing our Series C' style callouts — softest action in the hierarchy.

### White Surface Button

**Role:** Tertiary action on tinted or colored backgrounds

Background #ffffff, text #0e0f0c, border 1px solid #d5d5d2, radius 8px, padding 8px 12px, Melange 14px weight 400. Appears on product UI mockups and as in-app controls.

### Ghost Text Button

**Role:** Inline links within social/testimonial content

Background transparent, text #006fd6 (Link Cobalt), no border, radius 9999px, padding 0px 16px. Used for 'Read 25 replies' / 'Read more on X' — follows Twitter's own link convention inside embedded-style cards.

### Social Testimonial Card

**Role:** Twitter/X embed social proof blocks

Background #ffffff, border 1px solid #e3e3e3, radius 12px, padding 16px, box-shadow rgba(0,0,0,0.15) 0px 8px 48px 0px. Contains avatar 40px circle, username in Melange 15px weight 500 #0f1419, handle in Melange 14px #536471, body in system-ui 15px #0f1419, timestamp in #536471, action row with #536471 icons. Inline brand links appear in #006fd6.

### Feature Card

**Role:** Product UI preview cards with tinted background

Background #f7f7f2 (Parchment Canvas), radius 8px, padding 24px, no shadow, border 1px solid #e3e3e3. Contains product screenshot or illustration at full card width. Used in 3-column feature grids.

### Product Note Card

**Role:** In-product UI mockup showing the notepad itself

Background #ffffff, radius 12px, no box-shadow at card level, border 0.5px solid #d5d5d2. Internal rows use 0.5px solid #e3e3e3 hairline dividers. Olive-green (#b2c248) highlight fills span full-width rows to indicate AI-enhanced sections. Text in Melange 13-15px, primary #0e0f0c, metadata #72726e.

### Navigation Bar

**Role:** Top sticky site navigation

Background #ffffff with backdrop-filter blur(16px), border-bottom 0.5px solid #d5d5d2, box-shadow rgba(0,0,0,0.03) 0px 0px 36px. Logo + wordmark left, nav links center in Melange 15px weight 400 #292929, Download pill button right in Forest CTA (#5b6f00) with 9999px radius. Gap between nav items: 16px.

### Logo Marquee Strip

**Role:** Social proof partner/customer logo scrolling band

Background #ffffff, border-top and border-bottom 1px solid #e3e3e3, padding 16px 0. Logos rendered in #292929 or #72726 (desaturated to match brand neutrality). Animated with CSS marquee infinite scroll. No hover states — purely decorative trust signal.

### AI Enhanced Badge

**Role:** Inline label indicating AI processing state

Pill shape, background #e5eacd, border 1px solid #788c15 (Fern Border), radius 9999px, padding 4px 10px, Melange 13px weight 500 #5b6f00. Sparkle/AI icon at 14px in #b2c248 preceding label text. Signals the product's core value prop within the UI mockup.

### Recording Indicator Dot

**Role:** Live recording state status indicator

Circle 8px diameter, background #e95d3d (Ember Fill), border 1px solid #bd4a30 (Ember Border), CSS animation 'blink' at 0.9s ease. Appears in product UI mockups within the note-taking interface to signal active recording.

### Meeting Type Tag

**Role:** Categorical label for meeting template types

Background #ffffff, border 1px solid #d5d5d2, radius 8px, padding 6px 12px, Melange 14px weight 400 #0e0f0c. Examples: 'Customer discovery', '1 on 1', 'Pitch', 'Standup'. Horizontal scrollable row — functions as a filter/tab row inside the product UI preview.

## Layout

Max-width approximately 1200px, centered with symmetric horizontal padding. Hero is centered-stack: large Quadrant headline (68px) above a single-line subhead, then one CTA button, then a split product mockup panel below the fold — not full-bleed, the mockup floats on white with a thin border. The page flows in a single continuous white-to-parchment alternation: white nav → white hero → white product demo strip → white logo marquee strip (bordered top and bottom) → feature section on #f7f7f2 → testimonials grid on white → CTA footer. No dark sections at all. Testimonials use a 3-column masonry-like card grid with 16px column gaps. The navigation is a slim centered bar (~56px tall) with blur backdrop, logo left, 4-5 nav links center-spaced at 16px gaps, single pill Download button right. Vertical section gaps are 64px. No mega-menu — hoverable dropdowns with 8px radius white panels and 0.5px borders.

## Imagery

Granola uses product screenshot composites as its primary visual language — no lifestyle photography, no abstract illustration. The hero shows split-panel UI mockups: a raw notes view beside an AI-enhanced view, both contained in borderless floating card frames on the parchment canvas. Video feed thumbnails (user avatar crops at ~60×80px rounded 8px) appear inline with the product UI to simulate a live meeting context. The social proof section is image-free, relying entirely on text-based Twitter card reproductions. The logo strip uses monochrome partner wordmarks (Ramp, Linear, Brex, Replit, Index Ventures) desaturated to #292929 or #72726, no color logos. Icons are minimal and outlined/mono at 16-20px, used sparingly in navigation dropdowns and UI controls. Imagery density is very low — most screen area is white or parchment text surface, with product UI mockups occupying one contained mid-page strip.

## Elevation philosophy

Granola uses near-zero elevation: almost all cards are flat with hairline borders only. The system reserves meaningful shadow for two specific cases — the testimonial card (large diffuse shadow at 48px blur for the social proof gravity effect) and the sticky nav bar (barely perceptible 36px blur at 3% opacity). Everything else — feature cards, note cards, UI mockup panels — is borderline only. This restraint keeps the parchment-paper reading atmosphere intact; shadows would make it feel like a product dashboard, not a notepad.

## Dos & Donts

### Do

- Use Quadrant weight 400 at -0.015em tracking for all hero and section headings (48px and above); switch to Melange for every UI control, label, and body text instance.
- Apply #5b6f00 (Forest CTA) exclusively for filled primary action buttons — no other UI element should use this deep olive as a background fill.
- Use 9999px border-radius for all pill buttons, announcement badges, and tag chips; use 8px for card containers, inputs, and rectangular controls.
- Maintain 0.5px solid #d5d5d2 or 1px solid #e3e3e3 as the dominant border weight — hairline rules only, never 2px+ borders in the UI.
- Set page background to #f7f7f2 (Parchment Canvas) not pure #ffffff — white is reserved for card surfaces and elevated components that need contrast against the canvas.
- Apply the olive-green spectrum (#b2c248 surface, #5b6f00 text/CTA, #788c15 borders, #e5eacd tints) as a family — never mix with unrelated chromatic accents except Twitter Blue (#1d9bf0) in social embed contexts.
- Keep section vertical rhythm at 64px gaps; use 24px card padding for feature cards and 16px for compact note/list cards.

### Don't

- Don't use Quadrant for UI labels, buttons, or body text — it belongs only to headings and display-scale editorial moments.
- Don't add drop shadows to cards beyond the single social proof card exception (rgba(0,0,0,0.15) 0px 8px 48px) — flat hairline-bordered cards are the default surface treatment.
- Don't use #b2c248 (Olive Press) as a button background or text color — it is a decorative highlight surface in product mockups only, never an interactive element fill.
- Don't apply color backgrounds to full page sections — the page alternates white and parchment (#f7f7f2) only; chromatic backgrounds exist only as small card-level accents.
- Don't letter-space Quadrant headings positively — only negative tracking (-0.015em) or the slightly positive 0.01em for smaller sizes; wide-tracked display serifs would break the editorial compactness.
- Don't use the rainbow gradient (amber→pink→lavender→sky→chartreuse) as a UI element background — it exists solely as an animated text/icon decoration within AI-enhanced copy indicators.
- Don't substitute system fonts (San Francisco, Segoe UI) for Melange in navigation or button labels — the custom face's 0.01em tracking at small sizes is what separates the brand UI from generic system chrome.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- text (primary): #0e0f0c
- text (secondary): #72726e
- background (canvas): #f7f7f2
- background (card): #ffffff
- border (hairline): #e3e3e3
- border (card/button): #d5d5d2
- accent (olive brand): #b2c248
- primary action: #5b6f00 (filled action)

**Example Component Prompts**

1. **Hero Section**: #f7f7f2 page background. Quadrant 68px weight 400 #0e0f0c headline, letter-spacing -0.015em, line-height 1.0, centered. Melange 20px weight 400 #72726e subheadline below at 16px margin-top. Primary CTA: #5b6f00 background, #ffffff text, Melange 16px weight 500, radius 9999px, padding 12px 24px, Apple icon inline-left. Gap between headline and CTA: 32px.

2. **Social Testimonial Card**: #ffffff background, 1px solid #e3e3e3 border, 12px radius, 16px padding, box-shadow rgba(0,0,0,0.15) 0px 8px 48px. Avatar 40px circle top-left. Username: Melange 15px weight 600 #0f1419. Handle: Melange 14px weight 400 #536471. Body: system-ui 15px weight 400 #0f1419, line-height 1.4. Inline links #006fd6. Bottom row: timestamp #536471, heart count #536471 at 13px. 'Read N replies' ghost link: #006fd6 Melange 14px, no background, radius 9999px.

3. **Navigation Bar**: #ffffff background, backdrop-filter blur(16px), border-bottom 0.5px solid #d5d5d2, box-shadow rgba(0,0,0,0.03) 0px 0px 36px. Granola wordmark left. Nav links: Melange 15px weight 400 #292929, 16px horizontal gap. Right: Download pill button #5b6f00 background #ffffff text radius 9999px padding 8px 16px.

4. **Feature Card (Parchment)**: #f7f7f2 background, 8px radius, 24px padding, 1px solid #e3e3e3 border, no shadow. Label: Melange 14px weight 500 #5b6f00 uppercase tracking 0.01em. Headline: Quadrant 24px weight 400 #0e0f0c. Body: Melange 15px weight 400 #72726e, line-height 1.5. Product UI screenshot below label row, full card width.

5. **Meeting Type Tag Row**: Row of pill tags, each #ffffff background, 1px solid #d5d5d2 border, 8px radius, 6px 12px padding, Melange 14px weight 400 #0e0f0c. Horizontally scrollable at 8px gap. Active tag: #e5eacd background, #5b6f00 text, #788c15 border.

### Animation Philosophy

Motion is utilitarian and fast. Primary transitions run at 0.15s ease (hover color, border, fill changes) and 0.2s ease (layout shifts, opacity fades). The one slow animation is the 75s infinite CSS marquee for the logo scroll strip — deliberately imperceptible speed so logos feel ambient rather than animated. The recording indicator uses a 0.9s 'blink' keyframe at ease timing. The rainbow AI gradient uses a 'bg-slide' animation. Avoid any entrance animations or scroll-triggered reveals — the design language is static and document-like, with motion reserved for live-state indicators only.

---
_Source: https://styles.refero.design/style/d6c2a911-45ed-4860-a992-43df22793c2a_
