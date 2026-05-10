# Asana — Design Reference

> Productivity whiteboard lit by a coral lamp — authoritative black type on a breathing white field, with a warm coral flash and a deep-navy flicker to signal where action lives.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://asana.com](https://asana.com) |
| Refero page | [https://styles.refero.design/style/6b2a0513-df80-4140-87a8-38b1fef34313](https://styles.refero.design/style/6b2a0513-df80-4140-87a8-38b1fef34313) |
| Theme | light |
| Industry | productivity |

## Overview

Asana reads as a white canvas under a coral-and-violet editorial system — generous whitespace punctuated by a near-black (#0d0d0d) headline weight that commands attention, then released into light gray body copy. The signature move is two coexisting type voices: Ghost at 60-72px with -0.0070em tracking for display headlines that compress letter forms into dense, authoritative blocks, and TWK Lausanne at 300-500 weight handling everything else with a humanist openness. Pill buttons (100px+ radius) float as soft capsules against sharp-cornered cards (10-16px), creating gentle contrast between interaction surfaces and content containers. The coral tint (#ffeaec background, #690031 deep text) acts as a warm semantic accent for the product's brand-colored UI elements, while a saturated dark violet (#222875) marks interactive and informational states — both restrained to small, intentional doses against an otherwise achromatic canvas.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink Black | `#0d0d0d` | neutral | Body text, headings, button fill, icon strokes — near-black rather than true black keeps contrast high without harshness |
| Pure White | `#ffffff` | neutral | Page background, button text on dark fills, nav surface |
| Mist | `#f3f3f3` | neutral | Secondary button background, alternating section background fill |
| Cloud | `#e7e7e7` | neutral | Card borders, dividers, section background tint |
| Stone | `#e0dedc` | neutral | Subtle component borders, input strokes |
| Graphite | `#6e6e6` | neutral | Nav text, secondary UI labels, ghost button labels |
| Slate | `#646f79` | neutral | Body text secondary, border colors in nav and footer contexts |
| Charcoal | `#3d3d3d` | neutral | Body copy at normal reading weight |
| Iron | `#474748` | neutral | List items, link text in content areas |
| Ash | `#9ca6af` | neutral | Placeholder text, disabled state indicators |
| Asana Violet | `#222875` | brand | Hero product UI, informational badges, branded accent blocks — deep indigo-navy grounds AI and premium feature messaging |
| Coral Blush | `#ffeaec` | brand | Active filter chip background, soft badge backgrounds, warm section fills — coral warmth signals interaction state without aggression |
| Deep Coral | `#690031` | brand | Coral-family high-contrast text on Coral Blush backgrounds, brand icon accents |
| Coral Ember | `#ff584a` | accent | Icon fills in use-case cards, logo mark accent — vivid red-coral appears only in small illustration contexts |
| Coral Dark | `#710c3a` | accent | Hover/pressed state for coral-family brand elements |
| Sky Ice | `#cbefff` | accent | Blue-tinted soft background for feature callouts and informational chips |
| Coral Petal | `#e1bbc7` | accent | Icon strokes, subtle link decoration, light illustrative borders in coral family |

## Typography

### Ghost

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 60px, 72px |
| lineHeight | 1.00 |
| letterSpacing | -0.0070em (-0.42px at 60px, -0.50px at 72px) |
| fontFeatureSettings | normal |
| substitute | Neue Haas Grotesk Display, Aktiv Grotesk Ex |
| role | Display headlines only — the hero statement and major section titles. Weight 500 at 60-72px with tight -0.007em tracking compresses letter forms into dense editorial blocks. No other font on the site operates at this scale, making Ghost the unambiguous voice of authority. |

### TWK Lausanne

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| sizes | 11px, 12px, 13px, 14px, 16px, 20px, 22px, 24px, 30px, 36px |
| lineHeight | 1.20 – 2.57 depending on size (large sizes ~1.20, body ~1.50, captions ~1.75) |
| letterSpacing | Ranges from -0.01em at large sizes to +0.04em at caption/label sizes |
| substitute | Lausanne (Weltkern), Inter, DM Sans |
| role | Everything except hero display text: nav labels (weight 400, 14px), body copy (weight 300–400, 16px), subheadings (weight 500, 20-30px), section headings (weight 500, 24-36px). Weight 300 for body imparts an open, non-urgent reading texture. Weight 500 for UI labels and mid-hierarchy headings. Positive letter-spacing at small sizes (+0.025–0.04em) prevents optical crowding in captions and badges. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.75 | 0.44 |
| body | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.3 |  |
| heading-sm | 24 |  | 1.2 |  |
| heading | 36 |  | 1.2 | -0.36 |
| heading-lg | 60 |  | 1 | -0.42 |
| display | 72 |  | 1 | -0.5 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 10-16px |
| inputs | 4px |
| modals | 20px |
| buttons-pill | 100px |
| buttons-pill-xl | 146px |

- **elementGap** — 16px
- **sectionGap** — 80-120px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Components

### CTA Button Group

### Filter Chip Tab Bar

### Use Case Feature Cards

### Primary CTA Button (Pill)

**Role:** Main calls to action: 'Get started', primary conversion points

Background #0d0d0d, text #ffffff, border-radius 100px, padding 16px 32px. TWK Lausanne weight 500 at 16px. No border. The extreme pill radius against square card containers is the site's primary tension — the softness of the button against the geometry of surrounding content.

### Secondary Outline Button (Pill)

**Role:** Secondary actions: 'View demo', alternate CTAs alongside primary

Background #f3f3f3, text #0d0d0d, border 1px solid #0d0d0d, border-radius 146px, padding 16px 32px. Matches primary pill shape but inverted surface — light fill with a defined border. Near-identical radius to primary (146px vs 100px) keeps the two visually paired.

### Ghost Nav Button

**Role:** Navigation dropdown triggers and tertiary utility actions

Background transparent, text #6e6e6, border-color #6e6e6 (1px), border-radius 3px, padding 0 12px. No vertical padding. TWK Lausanne weight 400 at 14px. The 3px radius is the sharpest shape on the site — nav triggers are deliberately not pill-shaped to distinguish them from CTAs.

### Filter Chip (Active)

**Role:** Active category filter in tabbed product sections: 'Marketing', 'Operations', etc.

Background #ffeaec, text #690031, border-radius 999px, padding 8-12px horizontal. Active state surfaces the coral brand family — warm against the white section background. Inactive sibling chips use #f3f3f3 background with #0d0d0d text.

### Product UI Screenshot Card

**Role:** Hero and section feature showcases — framed product app screenshots

Background #222875 (Asana Violet), border-radius 16px, contains a rounded inner app window (radius 10px, white background) dropped inside the colored frame. The violet container makes the white app UI float as if lit from within. Used at large scale in the hero section (full-width up to 760px tall).

### Logo Bar / Social Proof Strip

**Role:** Fortune 100 trust signal section with partner logos

White background, no border, no shadow. Logos displayed in grayscale at medium opacity, horizontally spaced at 32-48px columnGap. Stat text ('85% of Fortune 100') at TWK Lausanne weight 500, 30px, #0d0d0d. Logos: Amazon, Accenture, Johnson & Johnson, Dell, Merck — all rendered in flat black/gray.

### Arrow Circle Icon Button

**Role:** Carousel navigation and card-level CTA links

Circular button 40px diameter, background #0d0d0d (active) or #e7e7e7 (inactive), icon #ffffff or #0d0d0d respectively, border-radius 100px. Appears both as standalone navigation controls (carousel prev/next) and as inline link embellishments at the base of feature cards.

### Sticky Navigation Bar

**Role:** Global top navigation, persistent on scroll

Background #ffffff, bottom border 1px solid #e7e7e7, height 56px. Logo left-aligned. Nav items (Ghost button style, transparent, #6e6e6e) center-grouped. Right side: 'Contact sales' text link (#0d0d0d), 'Log in' text link, 'Get started' primary pill button (#0d0d0d fill). TWK Lausanne 14px weight 400 for all nav text.

### Section Heading Block

**Role:** Large section titles introducing content clusters

Ghost font weight 500, 60px, lineHeight 1.00, letter-spacing -0.42px, color #0d0d0d. Left-aligned on content sections, centered on hero. Subhead in TWK Lausanne weight 300, 20px, color #3d3d3d, max-width ~560px. Vertical gap between heading and subhead: 16-24px.

### Informational Tint Chip

**Role:** Small inline labels and tag badges with semantic color tints

Background #cbefff (blue tint) or #ffeaec (coral tint), text #222875 or #690031 respectively, border-radius 999px, padding 4px 12px, TWK Lausanne weight 500 at 12px with letter-spacing +0.03em. Used to label feature categories inside product UI screenshots and callout blocks.

## Layout

Max-width approximately 1200px centered on all content, but product UI screenshot cards bleed to wider containers. Hero is a centered-headline pattern: Ghost headline centered, subhead centered at ~560px max-width, two pill buttons in a row, then a full-width product UI screenshot card below. Sections below alternate between: full-width white logo bar (no containers), left-text + tabbed-filter + image right (2-column split), and horizontally scrolling card rails (4 cards visible, 10px gap, overflow hinted). Navigation is a fixed top bar at 56px with left logo, center nav, right utility links. Vertical section rhythm is generous — 80-120px between sections — giving each content cluster breathing room. The feature card carousel section uses explicit prev/next circular icon buttons for navigation control.

## Imagery

Two visual registers coexist. Product UI screenshots dominate: flat, pixel-accurate app window captures dropped inside rounded violet frames, shown at large scale (full-width hero) and medium scale (section features). These are isolated, never lifestyle-contextualised — the interface IS the subject. The second register is a set of outline-only line illustrations used in use-case cards: single-color coral stroke illustrations on white (a campaign chart, an easel, a rocket) with minimal fill, geometric-organic hybrid style, approximately 1.5px apparent stroke weight. Icons across the UI are monochrome outlines, matching the TWK Lausanne light weight in visual mass. No photography anywhere on the visible page. The density balance is text-dominant with product UI serving as the single large visual anchor per section.

## Elevation philosophy

Asana uses zero box-shadow elevation across all content components. Cards are defined by 1px solid #e7e7e7 borders on a white background — depth comes from color contrast between container and surface, not shadow. The only visual elevation effect is the Product UI Screenshot Card, where a #222875 violet frame makes a white inner window appear to float — achieved through color contrast, not shadow. This approach keeps the page feeling like a flat printed surface.

## Dos & Donts

### Do

- Use Ghost font at 60-72px with letter-spacing -0.0070em exclusively for top-level display headlines — no other element should compete at this scale
- Apply 100px+ border-radius on all CTA buttons (primary and secondary) and filter chips — pill shapes are the only soft element in an otherwise angular component set
- Reserve #222875 (Asana Violet) for product UI frames and informational containers — never use it as a button fill or section background
- Pair Coral Blush (#ffeaec) backgrounds exclusively with Deep Coral (#690031) text — they are a locked semantic pair; do not substitute either half
- Maintain a near-achromatic page canvas (#ffffff primary, #f3f3f3 secondary) and introduce coral or violet only as small contained accents, never as full-section backgrounds
- Use TWK Lausanne weight 300 for all body copy to preserve the open, non-urgent reading texture — weight 400 is for UI labels and nav, weight 500 for subheadings and CTAs
- Set card borders to 1px solid #e7e7e7 with 10-16px radius — no box shadows on content cards; elevation is conveyed through color contrast alone

### Don't

- Never use Ghost font below 60px — TWK Lausanne at weight 500 handles all sub-display heading sizes
- Never apply the pill button radius (100px+) to cards, inputs, or modal containers — the shape language is exclusive to interactive action elements
- Never fill a full page section with #222875 or any saturated color — the violet and coral appear only in contained UI frames or chip-sized elements
- Never use positive letter-spacing on headings — letter-spacing is negative or zero for all sizes 20px and above
- Never mix the coral and violet accent families in the same component — they operate as separate semantic threads
- Never add box-shadow elevation to feature cards — borders alone define card boundaries; shadows break the flat, print-like surface quality
- Never set body copy weight above 400 — weight 500 is reserved for labels, CTAs, and subheading roles only

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- Text (primary): #0d0d0d
- Text (body): #3d3d3d
- Background (page): #ffffff
- Background (secondary section): #f3f3f3
- CTA button fill: #0d0d0d
- CTA button text: #ffffff
- Brand accent (violet): #222875
- Brand accent (coral bg): #ffeaec
- Brand accent (coral text): #690031
- Border (cards): #e7e7e7

**Example Component Prompts**

1. **Hero Section**: White background, max-width 1200px centered. Headline in Ghost font 72px weight 500, #0d0d0d, letter-spacing -0.50px, line-height 1.00, centered. Subhead in TWK Lausanne 20px weight 300, #3d3d3d, centered, max-width 560px, 16px below headline. Two buttons in a row centered below (24px gap): primary pill (#0d0d0d fill, #ffffff text, 100px radius, 16px 32px padding) + secondary pill (#f3f3f3 fill, #0d0d0d text, 1px solid #0d0d0d, 146px radius, 16px 32px padding). Product UI screenshot card below buttons in a #222875 frame with 16px radius.

2. **Use-Case Feature Card**: White background, 1px solid #e7e7e7 border, 10px radius, 24px padding. Coral stroke line illustration at top (~80px tall). Heading TWK Lausanne 20px weight 500 #0d0d0d, 16px below illustration. Body TWK Lausanne 16px weight 300 #3d3d3d, 14px below heading. 'See [X]' text link #0d0d0d weight 400, with a 40px circular #0d0d0d arrow icon button at bottom-left of card.

3. **Filter Chip Row**: Horizontally arranged pill chips (999px radius). Active chip: #ffeaec background, #690031 text, TWK Lausanne 14px weight 500. Inactive chips: #f3f3f3 background, #0d0d0d text, same font. 8px horizontal gap between chips. 16px 24px padding per chip.

4. **Logo / Social Proof Bar**: White background, full-width, 80px vertical padding. Left side: stat text TWK Lausanne 30px weight 500 #0d0d0d ('85% of Fortune 100 companies choose Asana'). Right side: 5 company logos in a horizontal row at 48px gap, rendered flat gray (#646f79 or desaturated). No border, no shadow.

5. **Section Header Block**: Left-aligned. Ghost font 60px weight 500 #0d0d0d, letter-spacing -0.42px, line-height 1.00. Below: TWK Lausanne 16-20px weight 300 #3d3d3d, max-width 480px, 16px margin-top. Section gap above: 96px.

### Coral + Violet Accent System

Asana operates two distinct chromatic accent families that never mix within the same component:

**Coral Family** (warm, interaction-focused): #ffeaec (Coral Blush — soft backgrounds), #e1bbc7 (Coral Petal — strokes/borders), #690031 (Deep Coral — text on coral backgrounds), #ff584a (Coral Ember — illustration fills), #710c3a (Coral Dark — hover states). Used in: filter chips, icon illustrations in feature cards, active UI state indicators.

**Violet Family** (cool, informational): #222875 (Asana Violet — product UI frames, feature containers), #cbefff (Sky Ice — informational chip backgrounds), #879fc8 / #6678ac (Blue-300/600 — focus rings and border states). Used in: hero product frame, AI feature callouts, trust/information signals.

Rule: Coral signals interaction and warm brand energy. Violet signals product, AI, and information architecture. Never apply coral to a purely informational element or violet to a CTA.

---
_Source: https://styles.refero.design/style/6b2a0513-df80-4140-87a8-38b1fef34313_
