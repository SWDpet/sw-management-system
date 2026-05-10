# Slite — Design Reference

> Warm parchment editorial desk — a workspace where knowledge feels handwritten, not enterprise-stamped.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://slite.com](https://slite.com) |
| Refero page | [https://styles.refero.design/style/607c2098-bbbb-40bb-b23e-adf2b72c63dd](https://styles.refero.design/style/607c2098-bbbb-40bb-b23e-adf2b72c63dd) |
| Theme | light |
| Industry | productivity |

## Overview

Slite's visual language feels like warm parchment under natural light — a cream-toned workspace where knowledge feels approachable rather than clinical. The dominant #f9efe4 background reads as aged paper without feeling retro, grounded by #3f434a ink-dark text that gives editorial weight to every line. The signature move is typographic contradiction: a serif-adjacent custom face (Garnett) at 64px for display, crossed out in black with 'goes to die' while 'thrives' renders in a hand-lettered cursive script in vivid blue — disruption-by-contrast rather than polish. Feature cards sit on near-white #fdfdfd against the warm cream ground, tagged with muted category badges in yellows, greens, and pinks that never compete with the overall softness. Pill buttons in near-black (#2d2f34) against cream create the sharpest contrast point on every screen, anchoring calls to action without resorting to saturated color.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Parchment | `#f9efe4` | neutral | Primary page background and hero — the defining warm cream that separates Slite from the cold-white SaaS default |
| Vellum | `#fdf9f4` | neutral | Secondary surface, slightly cooler than Parchment — used for card interiors and section alternation |
| Chalk | `#fdfdfd` | neutral | Highest-surface level — card backgrounds, modal surfaces, product UI previews |
| Ink | `#3f434a` | neutral | Primary text across all contexts — body, headings, labels, icons |
| Graphite | `#2d2f34` | neutral | Primary filled button background and high-contrast UI elements |
| Slate | `#656565` | neutral | Secondary body text, subheadings, list items |
| Ash | `#9da3af` | neutral | Tertiary text, placeholder labels, muted metadata |
| Silver Mist | `#d9dde6` | neutral | Borders, dividers, card outlines |
| Linen | `#f0e4d6` | neutral | Announcement banner background, muted input fills |
| Blueprint | `#2e77e5` | brand | Interactive text links, icon fills, decorative heading accent — the single chromatic voice in body text contexts |
| Electric Sky | `#176be5` | brand | SVG stroke accents in illustration work — technical/decorative, not interactive |
| Spectrum Gradient | `#ef91f7` | accent | Hero decorative gradient element — appears on accent strokes in illustration, not UI controls |
| Blossom | `#fae9f4` | accent | Category badge background for 'Customization' tags |
| Buttercup | `#fbf4d8` | accent | Category badge background for 'Security' tags |
| Sage | `#547358` | accent | Category badge text for 'Compliance' tags — muted green that reads as trustworthy without being a semantic success signal |
| Terracotta | `#f67748` | accent | Spot color in illustration and decorative icon backgrounds |
| Mauve | `#9d4d77` | accent | Category badge text for 'Customization' labels |
| Ochre | `#7f6c1f` | accent | Category badge text for 'Security' labels |
| Dusk Blue | `#446aa7` | accent | Inline link color in body text, muted enough to not compete with Blueprint |

## Typography

### Garnett

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 12px, 14px, 16px, 19px, 24px, 32px, 36px, 64px |
| lineHeight | 1.20–1.88 |
| fontFeatureSettings | "ss14", "ss15", "ss19" |
| substitute | DM Sans or Fraunces for warmth, though neither replicates Garnett's geometric-editorial balance |
| role | Display and heading font. At 64px weight 700 it carries the hero headline — a custom grotesque with editorial warmth missing from Inter or DM Sans. Weight 500 handles subheadings at 24–36px. The ss14/ss15/ss19 OpenType features are active and likely handle specific glyph alternates that distinguish it from substitutes. |

### UniversalSans

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| sizes | 10px, 11px, 12px, 14px, 16px, 24px |
| lineHeight | 1.00–1.88 |
| fontFeatureSettings | "ss14", "ss15", "ss19" |
| substitute | Plus Jakarta Sans or Instrument Sans |
| role | UI and body font. Handles all button labels, body copy, nav items, badges, captions. Weight 400 at 14–16px for body, 600 for button labels and emphasized UI text, 700 for numeric callouts. The custom face has more warmth than Inter — slightly wider apertures that match the cream-toned page feel. |

### Geist Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.50 |
| letterSpacing | 0.117em |
| fontFeatureSettings | "ss14", "ss15", "ss19" |
| substitute | Geist Mono (available via Vercel) or JetBrains Mono |
| role | Monospace face used sparingly for code snippets or technical labels. Letter-spacing at 0.117em (approximately 1.4px at 12px) — wider than default, creating a deliberate typewriter rhythm. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 16 |  |
| body | 14 |  | 22 |  |
| heading-sm | 19 |  | 28 |  |
| heading | 32 |  | 42 |  |
| heading-lg | 36 |  | 48 |  |
| display | 64 |  | 77 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| badges | 50px |
| modals | 24px |
| tooltips | 8px |
| buttons-pill | 42-50px |

- **elementGap** — 8-16px
- **sectionGap** — 80-120px
- **cardPadding** — 24-40px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Ground | `#f9efe4` | 0 | Primary page background — the warm cream canvas all content sits on |
| Card Surface | `#fdfdfd` | 1 | Feature cards, sidebar panels, elevated content blocks on the cream ground |
| Inner Surface | `#fdf9f4` | 2 | Nested containers within cards, alternating section backgrounds |
| Overlay | `#2d2f34` | 3 | Tooltips, dark popover backgrounds per CSS token --tooltip-background-color |

## Components

### CTA Button Group

### Feature Card Grid

### Testimonial + Rating Trust Block

### Primary Pill Button

**Role:** Main CTA — 'Start for free', nav primary action

Background #2d2f34, text #fdfdfd, border-radius 42px, padding 8px 16px. UniversalSans 600 at 14px. No border. The near-black pill against cream creates the sharpest contrast point on any page. Hover likely shifts toward true black.

### Outlined Pill Button

**Role:** Secondary CTA — 'Book a demo', secondary page actions

Background transparent, text #2f2f30, border 1px solid #2f2f30, border-radius 50px, padding 8px 24px. UniversalSans 600 at 14px. Sits beside the Primary Pill without competing — same shape language, less visual weight.

### Ghost Text Button

**Role:** Navigation dropdowns — 'Product', 'Solutions', 'Resources'

Background transparent, text #2f2f30, no border, border-radius 0, no padding. Pure text with dropdown caret. UniversalSans 400 at 14px. Used exclusively in the header nav for items with sub-menus.

### Rounded Tag Button

**Role:** In-page toggles and selectors — documentation type switchers

Background #fdfdfd, text #3f434a, border 1px solid #3f434a, border-radius 16px, padding 4px 12px. UniversalSans 400 at 12px. The 16px radius sits between pill and rectangle — softer than a badge, not as rounded as a pill.

### Feature Card

**Role:** Enterprise feature grid items — SSO, SCIM, GDPR etc.

Background #fdfdfd, border 1px solid #d9dde6, border-radius 12px, padding 24-32px. Shadow: rgba(0,0,0,0.01) 0 4px 12px, rgba(0,0,0,0.05) 0 2px 6px, rgba(0,0,0,0.1) 0 1px 3px. Contains a category badge (top-right), emoji icon, Garnett 500 heading at 19px, UniversalSans 400 body at 14px in #656565.

### Category Badge

**Role:** Feature card label — SECURITY, CUSTOMIZATION, COMPLIANCE, CONTROL

Border-radius 50px, padding 4px 12px. Four color variants: Security = background #fbf4d8, text #7f6c1f (Ochre). Customization = background #fae9f4, text #9d4d77 (Mauve). Compliance = background near-white, text #547358 (Sage). Control = background near-white, text #446aa7 (Dusk Blue). UniversalSans 600 at 11px uppercase.

### Product UI Preview Card

**Role:** Hero section embedded product screenshot

Background #fdfdfd, border-radius 12px, prominent layered shadow. Contains a simulated two-panel document UI — sidebar in light gray, content area in white with pink tinted illustration block (#eebacb). Acts as social proof without photography.

### Testimonial Quote Card

**Role:** Inline social proof floating over product preview

Background #fdfdfd, border-radius 12px, padding 16-24px, shadow matching Feature Card. Contains body quote text in Garnett 400 at 14px, attributed with avatar image + name in UniversalSans 600 at 13px + role in Ash (#9da3af) at 12px.

### Rating Chip

**Role:** G2/Capterra/ProductHunt score display in trust bar

No background, no border. Score '4.7/5' in Garnett 700 at 16px, #3f434a. Platform name 'on G2 Crowd' in UniversalSans 400 at 12px, #9da3af below. Minimal, typographic-only component.

### Navigation Bar

**Role:** Site-wide top header

Background #f9efe4 (matching page ground — no contrast separation from page). Logo + wordmark left, ghost nav links center, 'Sign in' link + 'Book a demo' outlined button + 'Start for free' pill button right. Height 72px. No border-bottom or shadow — floats seamlessly on cream.

### Tooltip

**Role:** Contextual hover labels

Background hsl(0,1%,23%) ≈ #3B3B3B, text hsl(231,1%,100%) ≈ #FDFDFD. Border-radius 8px, padding 4-8px. UniversalSans 400 at 12px. Per CSS design tokens.

## Layout

Max-width approximately 1200px, centered. The hero is full-bleed cream (#f9efe4) with a centered headline stack above a large contained product mockup card. Below the hero, sections alternate between cream background (page ground) and near-white (#fdf9f4) without hard dividers — color shift alone signals section breaks. The enterprise feature section uses a 3-column card grid with 24px gutters, each card uniform height. Trust/social-proof sections are single-row horizontal with logo lockups centered. The product detail sections use asymmetric 2-column split: text left (40%), product UI right (60%). Navigation is a flat single-row bar at 72px, no mega-menu visible — dropdowns implied by chevrons. Vertical section rhythm is generous: 80-120px between sections, creating breathing room that reinforces the editorial pacing over information density.

## Imagery

Slite uses a hybrid of product UI screenshots and simple line-art illustrations rather than photography. The hero features an embedded product interface mockup — a faithful recreation of the actual application sidebar and document canvas, presented in a rounded card at roughly 70% page width. Overlapping this mockup are floating testimonial cards and compliance badge graphics, creating a layered depth effect. Illustration style is minimal line-drawing: architectural/structural sketches (a building facade, a filing cabinet) rendered in single-weight strokes on colored background tiles (#eebacb pink, near-white). These illustrations are contained within grid cells, never full-bleed. Company logos in the trust bar are all displayed in a uniform dark gray (#3f434a), stripping brand colors for visual unity — a deliberate erasure of chaos in favor of calm. Icon style throughout is outlined, single-weight, monochrome, matching text color exactly. The overall image density is low — the page is text and UI-preview dominant, with illustration serving as spatial punctuation rather than atmosphere.

## Elevation philosophy

Shadow usage is minimal and intentionally recessive. The single shadow formula — rgba(0,0,0,0.01) 0px 4px 12px, rgba(0,0,0,0.05) 0px 2px 6px, rgba(0,0,0,0.1) 0px 1px 3px — appears only on cards and selected buttons. It reads as a whisper of lift against the warm background rather than a dramatic drop. Elevation is communicated primarily through background color contrast (cream → near-white) rather than shadow depth.

## Dos & Donts

### Do

- Use #f9efe4 as the default page background — never white or cold gray; the warmth is the entire atmospheric identity
- Apply border-radius 42-50px to all primary and secondary CTA buttons — the pill shape is non-negotiable brand geometry
- Use Garnett weight 700 for display sizes (36-64px) and weight 500 for section headings (24-32px); never use UniversalSans for headings
- Render category badges with border-radius 50px, padding 4px 12px, and the specific color pairs: Ochre text on Buttercup (#fbf4d8), Mauve text on Blossom (#fae9f4), Sage text on near-white — never swap pairs
- Keep feature cards at border-radius 12px with the three-layer shadow (rgba(0,0,0,0.01/0.05/0.10)) — this whisper-shadow is the only elevation used
- Use #2d2f34 filled pill buttons as the single primary CTA per screen; every other action should be outlined or ghost weight
- Restrict chromatic color (Blueprint #2e77e5) to interactive text links and decorative heading accents only — never for backgrounds or borders in feature UI

### Don't

- Don't use pure white (#ffffff) as a page background — it reads cold against Slite's warm typographic palette; use #fdfdfd or #fdf9f4 for card surfaces
- Don't introduce saturated color backgrounds for section bands — the warm cream is the only background color; category badges carry all color variety
- Don't use font weights below 400 or above 700, and don't apply Garnett at sizes below 12px where its editorial character is lost
- Don't add shadows beyond the established three-layer formula — heavier shadows break the lightweight elevation philosophy
- Don't use rectangular buttons (border-radius under 12px) for primary actions — all CTAs must be pill or near-pill shaped
- Don't display third-party logos in their brand colors in trust bars — convert all to #3f434a for visual unity
- Don't place category badges in any color combination outside the established four variants (Security/Customization/Compliance/Control) — ad-hoc badge colors fracture the muted palette

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- Page background: #f9efe4 (Parchment)
- Primary text: #3f434a (Ink)
- CTA button fill: #2d2f34 (Graphite)
- Card surface: #fdfdfd (Chalk)
- Border/divider: #d9dde6 (Silver Mist)
- Link/accent: #2e77e5 (Blueprint)
- Secondary text: #656565 (Slate)

**Example Component Prompts**

1. **Hero Section**: Background #f9efe4, centered layout max-width 1200px. Announcement pill at top: background #f0e4d6, border-radius 50px, padding 4px 12px, UniversalSans 600 12px #3f434a, 'NEW' label bold. Main headline Garnett 700 64px #3f434a, line-height 1.20. Below: two buttons side by side — 'Start for free' outlined pill (border 1px solid #2d2f34, border-radius 50px, padding 8px 24px, UniversalSans 600 14px #2d2f34) and 'Book a demo' filled pill (background #2d2f34, border-radius 42px, padding 8px 16px, text #fdfdfd). Below buttons: product UI mockup card at border-radius 12px, background #fdfdfd, box-shadow rgba(0,0,0,0.01) 0 4px 12px, rgba(0,0,0,0.05) 0 2px 6px, rgba(0,0,0,0.1) 0 1px 3px.

2. **Feature Card (Enterprise Grid)**: Background #fdfdfd, border 1px solid #d9dde6, border-radius 12px, padding 32px, box-shadow rgba(0,0,0,0.01) 0 4px 12px, rgba(0,0,0,0.05) 0 2px 6px, rgba(0,0,0,0.1) 0 1px 3px. Top-right: category badge (border-radius 50px, padding 4px 12px, UniversalSans 600 11px). Emoji icon + Garnett 500 19px #3f434a heading + UniversalSans 400 14px #656565 description. 3-column grid, 24px gap.

3. **Navigation Bar**: Background #f9efe4, height 72px, no border/shadow. Logo left (Garnett 700 16px). Center: ghost nav links UniversalSans 400 14px #3f434a with chevron. Right: 'Sign in' text link #3f434a + 'Book a demo' outlined pill (border 1px solid #2d2f34, border-radius 50px, padding 8px 16px) + 'Start for free' filled pill (background #2d2f34, text #fdfdfd, border-radius 42px, padding 8px 16px).

4. **Category Badge**: Border-radius 50px, padding 4px 12px, UniversalSans 600 11px. Security variant: background #fbf4d8, text #7f6c1f. Customization variant: background #fae9f4, text #9d4d77. Compliance variant: background #fdf9f4, text #547358. All-caps label text.

5. **Trust/Logo Bar**: Background #f9efe4, single centered row, logos converted to #3f434a (all brand colors stripped). Rating chips: score in Garnett 700 16px #3f434a, platform name in UniversalSans 400 12px #9da3af, stacked, no container.

### Typographic Signature Moves

Slite uses two fonts for distinct jobs with zero overlap: Garnett owns all display and heading sizes, UniversalSans owns all UI and body sizes. The hero headline uses a strikethrough effect on 'goes to die' in Garnett 700 64px, immediately replaced by 'thrives' in a cursive script at the same size in Blueprint blue (#2e77e5) — this editorial contradiction is the brand's most distinctive typographic moment and should be replicated on any hero that benefits from before/after framing. The Geist Mono face appears only for code-adjacent content at 12px with 0.117em letter-spacing — never stretch it beyond that context. OpenType features ss14, ss15, ss19 are active on all three faces — include font-feature-settings in all CSS declarations.

---
_Source: https://styles.refero.design/style/607c2098-bbbb-40bb-b23e-adf2b72c63dd_
