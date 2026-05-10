# Paste — Design Reference

> Amber lantern on white marble — the brand's warm gradient logo floats in vast white space, like a single lit window in a snow-covered building.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://pasteapp.io](https://pasteapp.io) |
| Refero page | [https://styles.refero.design/style/742b500d-3e10-4daa-bb89-d0d26272e5f6](https://styles.refero.design/style/742b500d-3e10-4daa-bb89-d0d26272e5f6) |
| Theme | light |
| Industry | productivity |

## Overview

Feels like sunlight through a minimalist gallery — vast white space with black typography and a single warm-amber focal point that draws the eye like a lantern in snow. The page is dominated by pure white (#ffffff) and near-white (#f5f5f7) surfaces with near-black (#101010) text, creating extreme contrast. system-ui at display sizes (54-80px) with tight letter-spacing (-0.013em) and weight 400-700 gives headlines a native-OS feel that reinforces the Mac-utility identity. The amber-orange gradient logo (rgb(240,100,19) → rgb(254,171,48)) is the only warm element on an otherwise monochrome canvas, making it impossibly magnetic. Blue CTA buttons (#0088ff) with 100px pill radius are the sole call to action — warm brand, cool CTA, white field.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Amber Flame | `#f06413` | brand | Logo, brand mark, gradient start — the warm orange anchors the entire identity as the only chromatic element on a monochrome canvas |
| Honey Glow | `#feab30` | brand | Logo gradient end, warm highlight — lifts the amber into golden territory, visible in section headings and brand accents |
| Signal Blue | `#0088ff` | accent | Primary CTA buttons, interactive links — cool blue against warm-amber brand creates intentional temperature contrast that separates identity from action |
| Bright Blue | `#1c95ff` | accent | Hover/active state for blue CTAs, secondary interactive highlights |
| Pure White | `#ffffff` | neutral | Primary page background, card surfaces, hero sections |
| Snow Gray | `#f5f5f7` | neutral | Alternating section backgrounds, subtle surface differentiation from white |
| Mist | `#f0f0f0` | neutral | Divider backgrounds, subtle containers |
| Silver | `#d0d0d3` | neutral | Borders, decorative dividers |
| Pewter | `#ababb0` | neutral | Secondary body text, captions, muted labels |
| Smoke | `#6e6e73` | neutral | Tertiary text, metadata, footnotes |
| Charcoal | `#272727` | neutral | Dark surface backgrounds in dark sections |
| Ink | `#101010` | neutral | Primary heading and body text color |
| True Black | `#000000` | neutral | Maximum contrast text, nav links, icon color |
| Vivid Green | `#34c759` | semantic | Feature category indicator, privacy/security highlights |
| Electric Magenta | `#cb30e0` | semantic | Feature category indicator, collaboration highlights |
| Alert Red | `#ff383c` | semantic | Feature category indicator, emphasis highlights |

## Typography

### system-ui

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 15px, 16px, 18px, 22px, 24px, 40px, 54px, 60px, 80px |
| lineHeight | 0.96–1.67 (tight at display sizes, relaxed at body) |
| letterSpacing | -1.04px at 80px, -0.78px at 60px, -0.70px at 54px; positive +0.36–1.01px tracking at small sizes (15-18px) for legibility at caption scale |
| substitute | SF Pro Display / SF Pro Text (system default on Apple), Inter on non-Apple systems |
| role | Primary typeface for all content — headlines, body, subheadings. Using the system font stack is a deliberate choice that makes the app feel native to macOS/iOS, reinforcing the clipboard-manager-as-OS-extension identity. Weight 400 for body, 600-700 for headlines. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.29 |
| letterSpacing | -0.41px at 14px — tight tracking for compact labels |
| substitute | Inter (Google Fonts) |
| role | Used for press/media logos section labels — small metadata text where system-ui's metrics may not be optimal |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 18 | -0.41 |
| body | 16 |  | 24 |  |
| subheading | 18 |  | 24 |  |
| heading-sm | 22 |  | 28 |  |
| heading | 40 |  | 44 | -0.24 |
| heading-lg | 54 |  | 56 | -0.7 |
| display | 80 |  | 80 | -1.04 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16-20px |
| badges | 100px |
| images | 16-24px |
| buttons | 100px |
| containers | 24-40px |

- **elementGap** — 16-20px
- **sectionGap** — 80-120px
- **cardPadding** — 20-30px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Canvas | `#ffffff` | 0 | Primary page background |
| Section Alternate | `#f5f5f7` | 1 | Alternating section backgrounds for visual rhythm |
| Elevated Card | `#ffffff` | 2 | Cards and containers that float above Section Alternate with ambient shadow |

## Components

### Primary CTA Button Group

### Social Proof / Press Logos Bar

### Feature Cards Grid

### Primary CTA Button (Filled Pill)

**Role:** Main call-to-action across hero and sections

Background #0088ff, white text, 100px border-radius (full pill). Padding 8px 20px. system-ui weight 600, ~16px. No border. Hover state shifts to #1c95ff. The pill shape at 100px radius is a defining visual — every button is fully rounded.

### Ghost Pill Button (Outline)

**Role:** Secondary actions, alternative CTAs

Transparent background, border color matching text. 100px border-radius. Padding 10px 30px — slightly larger than filled variant. system-ui weight 500-600.

### Navigation Bar

**Role:** Top-level site navigation, sticky header

White background, horizontally centered. Logo (amber gradient icon + 'Paste' in black) on left. Nav links in #000000, system-ui weight 400-500 at ~16px. Right-aligned 'Try for free' pill button in #0088ff. Links include dropdowns (e.g. 'Use Cases ▾').

### Hero Section

**Role:** Primary landing area with product showcase

Pure white (#ffffff) background. Centered layout. Product screenshots (Mac, iPhone, iPad) composited together as hero image. Headline at 54-60px, system-ui weight 700, #101010, tight letter-spacing. Body text at 18px, weight 400, #6e6e73. CTA button below body text.

### Feature Section (Amber Headline)

**Role:** Section introduction with brand-colored headline

Background #f5f5f7 (Snow Gray). Large headline at 54-80px in the amber-orange brand gradient — this is the signature move: display-size text rendered in the brand gradient against a light gray surface. Body text in #101010 or #6e6e73.

### Feature Card

**Role:** Individual feature highlight within grid layouts

White (#ffffff) or Snow Gray (#f5f5f7) background. Border-radius 16-20px. Padding 20-30px. Shadow: rgba(16,16,16,0.1) 0px 0px 30px — soft ambient glow, not directional. Headline in system-ui weight 600, 22-24px. Body in weight 400, 16px, #6e6e73.

### Category Color Indicator

**Role:** Visual markers for feature categories (privacy, collaboration, etc.)

Four chromatic accents used as category identifiers: #0088ff (productivity), #34c759 (privacy/security), #cb30e0 (collaboration), #ff383c (power features). Applied as text color or border-color on body elements, never as backgrounds.

### Product Screenshot Container

**Role:** Device mockup display for product imagery

Product screenshots shown within device frames (MacBook, iPhone, iPad). Images have 16-24px border-radius when not in device frames. Composed in overlapping arrangements — devices overlap slightly to show ecosystem. No drop shadow on device frames themselves.

### Section Divider (Surface Shift)

**Role:** Visual separation between page sections

No visible divider lines — sections are separated by background color alternation between #ffffff and #f5f5f7 with large 80-120px vertical spacing. The transition itself IS the divider.

### Pricing CTA Block

**Role:** Conversion-focused pricing section

Contains 'Buy Now' and 'Try for Free' pill buttons. Likely centered layout with price information in system-ui weight 600-700 at heading scale. Blue filled button for primary action, ghost/outline variant for secondary.

## Layout

Max-width ~1200px centered container. Hero is full-width white with centered headline, centered body text, and a composed multi-device product screenshot below. CTA button centered below body copy. Sticky navigation bar at top with logo left, links center, CTA right. Below hero: a thin press-logos bar (social proof). Sections alternate between #ffffff and #f5f5f7 backgrounds with 80-120px vertical gaps. Feature sections use large amber-gradient headlines centered, followed by explanatory content. Content is predominantly centered single-column — no sidebars, minimal multi-column grids. Section rhythm: hero → social proof → feature intro (amber headline on gray) → feature details → next feature section. The page reads as a vertical scroll with clear section breaks via background shifts.

## Imagery

Product-focused device mockups dominate — MacBook, iPhone, and iPad shown together in composed arrangements where devices overlap slightly to communicate ecosystem unity. Screenshots show the actual app UI with colorful clipboard items (photos, text snippets, maps, messages) providing visual interest against the monochrome page. No lifestyle photography, no abstract illustrations. The hero image is a composite of three device frames centered on white, establishing a 'product showcase in a gallery' feel. Press logos are displayed in muted gray. The amber-orange gradient appears only in the logo icon and as headline text color in feature sections — it's treated like a precious material used sparingly. Icon style mirrors Apple's SF Symbols: mono-weight, single-color, functional. Overall density is text-dominant with large product imagery as section anchors.

## Dos & Donts

### Do

- Use 100px border-radius for ALL buttons, badges, and pill-shaped elements — this is non-negotiable and defines the visual identity
- Alternate page sections between #ffffff and #f5f5f7 backgrounds to create rhythm without visible dividers
- Set display headlines (40px+) in system-ui weight 600-700 with negative letter-spacing (-0.7px to -1.04px) — tight tracking at large sizes is essential
- Reserve the amber-orange gradient (rgb(240,100,19) → rgb(254,171,48)) for brand mark and occasional headline accents — never for backgrounds or large surfaces
- Keep all CTA buttons in #0088ff with white text — the warm brand / cool CTA temperature split is the core interaction pattern
- Use #6e6e73 or #ababb0 for secondary/body text to maintain the high-contrast headline / low-contrast body hierarchy
- Apply the soft ambient shadow (rgba(16,16,16,0.1) 0px 0px 30px) to elevated cards — never sharp directional shadows

### Don't

- Never use the amber-orange gradient as a button fill — it is reserved for the logo and decorative headline accents only
- Never mix sharp-corner containers (0px radius) with the pill-radius system — minimum radius for any container is 8px, with 16-20px for cards
- Never use more than one chromatic accent color (#0088ff) in a single CTA context — the four category colors (#34c759, #cb30e0, #ff383c) are for indicators, not buttons
- Never set body text in weight 700 — reserve 700 for headlines at 40px+; body stays at 400-500
- Never add visible border lines between sections — use background color shifts (#ffffff ↔ #f5f5f7) and spacing instead
- Never use directional or hard-edged shadows — the only shadow in the system is the ambient 30px blur at 10% opacity
- Never apply positive letter-spacing to headlines — display type always uses negative tracking; positive spacing is only for small (14-18px) labels

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- Text (primary): #101010
- Text (secondary): #6e6e73
- Text (muted): #ababb0
- Background (primary): #ffffff
- Background (alternate): #f5f5f7
- CTA: #0088ff
- Brand gradient: linear-gradient(0deg, rgb(240,100,19) -29%, rgb(254,171,48) 100%)

**Example Component Prompts:**

1. "Create a hero section: white (#ffffff) background. Centered headline at 54px system-ui weight 700, color #101010, letter-spacing -0.7px, line-height 56px. Body text below at 18px weight 400, color #6e6e73, line-height 24px. Blue pill CTA button (#0088ff, white text, 100px border-radius, 8px 20px padding, system-ui weight 600). 80px spacing below."

2. "Create a feature intro section: background #f5f5f7. Large headline at 60-80px with text rendered in the amber-orange gradient (linear-gradient(0deg, rgb(240,100,19), rgb(254,171,48)), -webkit-background-clip: text). system-ui weight 700, letter-spacing -1.04px. Center-aligned with 120px top/bottom padding."

3. "Create a navigation bar: white background, max-width 1200px centered. Left: amber gradient icon (20px square, 8px radius) + 'Paste' in #000000 system-ui weight 600 at 18px. Center: nav links at 16px weight 400 #000000 with 30px gap. Right: pill button 'Try for free' with #0088ff background, white text, 100px radius, 8px 20px padding."

4. "Create a feature card: white (#ffffff) background, 20px border-radius, 24px padding. Ambient shadow rgba(16,16,16,0.1) 0px 0px 30px. Headline at 22px system-ui weight 600 #101010. Body at 16px weight 400 #6e6e73, line-height 24px. 16px gap between headline and body."

5. "Create a press logos bar: centered row on white background. 5 gray (#ababb0) logo placeholders spaced 30px apart. Apple logo + '⭐ 4.5' rating badge at left. Inter 14px weight 400, letter-spacing -0.41px, color #ababb0."

### Gradient System

The amber-orange gradient (linear-gradient(0deg, rgb(240,100,19) -29.375%, rgb(254,171,48) 100%)) is the singular brand gradient. It appears in exactly two contexts: (1) the logo icon as a background fill, and (2) large display headlines via -webkit-background-clip: text to create gradient text. It is NEVER used as a section background, button fill, or decorative element. The gradient flows from deep burnt orange at the bottom to golden amber at top — when applied to text, it creates a warm metallic shimmer effect. No other gradients exist in the system.

### Category Color System

Four vivid chromatic colors are used as feature-category indicators, applied as text color or border accents on body-level elements — never as backgrounds or button fills:
- Blue (#0088ff): productivity/general features
- Green (#34c759): privacy and security features
- Magenta (#cb30e0): collaboration features
- Red (#ff383c): power/advanced features
These colors mirror Apple's SF Symbols palette, reinforcing the native-OS aesthetic. They appear at body text scale (16-18px) only.

---
_Source: https://styles.refero.design/style/742b500d-3e10-4daa-bb89-d0d26272e5f6_
