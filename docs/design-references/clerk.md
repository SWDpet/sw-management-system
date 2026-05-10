# Clerk — Design Reference

> Developer dashboard behind frosted violet glass — surfaces feel like a live IDE preview: dark product cards floating inside a light documentation shell, with one electric violet pulse marking the path forward.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://clerk.com](https://clerk.com) |
| Refero page | [https://styles.refero.design/style/ed10ae04-24ec-4e42-9bf2-ea12a4b58d67](https://styles.refero.design/style/ed10ae04-24ec-4e42-9bf2-ea12a4b58d67) |
| Theme | mixed |
| Industry | devtools |

## Overview

Clerk uses a split-personality canvas: the marketing shell is near-white (#f7f7f8) with barely-there borders and generous whitespace, while product demo surfaces drop into near-black (#212126) dark cards that showcase UI components in context. The single brand color — a mid-range violet (#6c47ff) — appears with surgical restraint: only on primary CTAs and the occasional heading accent, making every appearance feel deliberate. Typography defaults to Geist, a custom numeric-optimized face at tight negative tracking (-0.035em), giving headlines a technical density that signals developer-first thinking without heavy weight. The gradient system layers violet with stray yellow (#fff963) and cyan (#5de3ff) halos at low opacity, creating an atmospheric shimmer behind hero content rather than decorative noise.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Void Black | `#131316` | neutral | Primary text, headings, and high-contrast body copy across light surfaces |
| Fog White | `#f7f7f8` | neutral | Page canvas and light section backgrounds |
| Pure White | `#ffffff` | neutral | Card surfaces, modal backgrounds, form inputs on light sections |
| Graphite | `#5e5f6e` | neutral | Secondary body text, muted labels, icon fills on light surfaces |
| Ash | `#747686` | neutral | Tertiary text, nav punctuation, disabled state text |
| Hairline | `#d9d9de` | neutral | Borders, dividers, input outlines, table rules |
| Mist | `#eeeef0` | neutral | Subtle section dividers, link icon backgrounds, hover wash backgrounds |
| Stone | `#9394a1` | neutral | Placeholder text, code comments, muted helper copy |
| Obsidian Card | `#212126` | neutral | Dark product demo card backgrounds in mixed-mode sections |
| Dark Surface | `#2f3037` | neutral | Elevated dark panel backgrounds, code block surfaces |
| Slate Mid | `#696a78` | neutral | Mid-tone icon fills and body copy inside dark card contexts |
| Electric Violet | `#6c47ff` | brand | Primary CTA button fill, active heading accents — the single chromatic action color; its mid-spectrum violet reads developer-brand without borrowing blue from infrastructure tools; Atmospheric hero background gradient blending violet and yellow; Brand gradient for decorative text highlights and horizontal accent rules |
| Violet Soft | `#615cf6` | brand | Decorative list and tag backgrounds, subtle brand surface tints |
| Cyan Spark | `#5de3ff` | accent | Heading accent text inside dark sections, code string highlights, decorative icon glow |

## Typography

### soehneMono

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 10px, 11px, 12px |
| lineHeight | 1.33, 1.4, 1.45, 1.64, 1.82, 2 |
| letterSpacing | 0.1 |
| role | soehneMono — detected in extracted data but not described by AI |

### Geist

| Key | Value |
| --- | --- |
| weight | 400, 450, 500, 600, 700 |
| sizes | 10px, 11px, 12px, 13px, 15px, 16px, 18px, 20px, 32px, 64px |
| lineHeight | 1.0–1.85 depending on size; tighter (1.0–1.25) at display sizes, looser (1.4–1.6) at body sizes |
| letterSpacing | -0.035em at display/headline sizes, -0.015em at subheading sizes |
| fontFeatureSettings | "tnum" for numeric variant |
| substitute | Inter, DM Sans |
| role | Primary typeface for all marketing copy, UI labels, headings, and CTAs. The custom numeric tables (geistNumbers variant) keep digit spacing consistent inside product demos. Negative tracking at -0.035em at large sizes and -0.015em at mid sizes compresses display headlines into a dense technical block — a deliberate contrast to softer marketing type. |

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 10px, 11px, 12px, 13px, 16px, 17px |
| lineHeight | 1.27–1.82 |
| letterSpacing | -0.01em to +0.01em |
| substitute | system-ui, -apple-system |
| role | System fallback font used in browser-rendered components, OS UI widgets, and the Clerk-embedded auth modal. Its presence signals authentic native-UI feel in interactive demos. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 8px, 10px, 11px, 12px, 16px, 18px, 22px |
| lineHeight | 1.22–1.82 |
| letterSpacing | normal |
| substitute | Inter (same, freely available via Google Fonts) |
| role | UI micro-labels, product card annotations, and icon captions within embedded component previews. |

### Söhne Mono

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 10px, 11px, 12px |
| lineHeight | 1.33–2.00 |
| letterSpacing | +0.10em |
| substitute | JetBrains Mono, Fira Code |
| role | Code snippets, JSX component labels (<SignUp />, <UserButton />), and terminal output. The +0.10em positive tracking at mono sizes makes angle-bracket syntax air out instead of colliding — a purposeful readability choice at 10-12px. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.4 | 0 |
| heading-sm | 18 |  | 1.4 | -0.27 |
| heading | 20 |  | 1.25 | -0.3 |
| heading-lg | 32 |  | 1.13 | -1.12 |
| display | 64 |  | 1 | -2.24 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 16px |
| badges | 9999px |
| inputs | 6px |
| modals | 16px |
| buttons | 6px |
| cardSmall | 8px |
| codeBlocks | 12px |

- **elementGap** — 8px
- **sectionGap** — 80px
- **cardPadding** — 
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Canvas | `#f7f7f8` | 0 | Base page background for light marketing sections |
| Card Surface | `#ffffff` | 1 | Default card and form surface on light backgrounds |
| Recessed Surface | `#f7f7f8` | 2 | Input backgrounds, table row fills, and secondary card insets |
| Dark Canvas | `#212126` | 3 | Product demo sections and dark-mode showcase panels |
| Elevated Dark | `#2f3037` | 4 | Elevated panels and code blocks within dark sections |

## Components

### Primary Violet Button

**Role:** Main CTA — 'Start building for free', 'Start building'

Background #6c47ff, white text (#ffffff), 6px border-radius, 0px vertical padding with 12px horizontal padding. Box-shadow uses violet glow. Geist font weight 500, 15-16px size. Transitions background-color and box-shadow at 0.15s ease.

### White Outlined Button

**Role:** Secondary action, navigation-adjacent controls

Background #ffffff, text #131316, 1px solid #d9d9de border, 6px radius, 0px vertical / 10px horizontal padding. Subtle shadow: rgb(217,217,222) 0px 0px 0px 0.5px. Geist weight 500.

### Ghost Navigation Button

**Role:** Top-level nav items and inline text-links with icons

Background transparent, text #131316, no border, 0px radius. Right padding 8px. On hover, border-color transitions to #d9d9de. Used for Products, Docs, Changelog, Company nav items.

### Pill Tag / Badge

**Role:** Category labels, section eyebrow labels (e.g. 'Clerk Components', 'User Authentication')

9999px border-radius, background transparent or #eeeef0, text #6c47ff at 11-12px Geist weight 500. 4px vertical / 10px horizontal padding. The violet text on near-white background signals section taxonomy without using a filled badge.

### Light Marketing Card

**Role:** Feature cards and pricing sections on white/fog surfaces

Background #ffffff, 16px radius, shadow stack: rgba(0,0,0,0.05) 0px 1px 1px, rgba(34,42,53,0.04) 0px 4px 6px, rgba(47,48,55,0.05) 0px 24px 68px. Padding 16px 20px. 1px hairline border at #d9d9de at 0.5px weight.

### Dark Obsidian Card

**Role:** Product demo feature cards in dark sections (auth UIs, session management)

Background #212126, 16px radius, inset white shimmer: rgba(255,255,255,0.024) 0px 1px 0px inset + rgba(255,255,255,0.024) 0px 0px 0px 1px inset. No explicit padding (content-driven). Text #ffffff and #9394a1 for body. Creates glass-like border without actual border.

### Auth Component Card

**Role:** Embedded Clerk sign-in / sign-up modal preview

Background #ffffff, 8px radius (inner form elements), outer wrapper uses 16px radius. Deep shadow: rgba(47,48,55,0.2) 0px 15px 35px -5px. Inner inputs: #f7f7f8 background, 6px radius, 1px #d9d9de border. Primary action inside uses Electric Violet (#6c47ff) full-width button.

### Code Component Label

**Role:** JSX component name pills in documentation sidebar

Söhne Mono weight 500, 10-12px, +0.10em letter-spacing, text #131316 or #6c47ff, background transparent. Formatted as '<ComponentName />' with angle brackets in violet accent color.

### Form Input (Light)

**Role:** Email, password fields in auth card demos

Background #f7f7f8, 6px radius, 1px solid #d9d9de border, padding 8px 12px. Placeholder text #9394a1 (Stone). Focus ring uses violet: rgba(108,71,255,0.2) box-shadow ring. Label text #131316 Geist weight 500 12px.

### OAuth Social Button

**Role:** 'Continue with Google', 'Continue with GitHub' inside auth modal

Background #ffffff, 1px solid #d9d9de, 6px radius, full-width, 10px 12px padding, text #131316 Geist weight 500 14px, leading OAuth provider icon left-aligned. Shadow: rgba(0,0,0,0.06) 0px 0px 0px 1px.

### Accordion Feature Row

**Role:** Collapsible feature list in the component explorer sidebar (User Authentication, B2B Authentication, Billing)

Background transparent, text #747686 when collapsed, #131316 when expanded, 9999px radius on expand indicator dot. Dashed 1px #eeeef0 separator. Expanded state shows sub-item code labels indented 16px.

### Announcement Banner

**Role:** Top-of-page news strip ('Clerk raises $50m Series C')

Full-width, background #131316, text #ffffff Geist weight 500 13px, centered. Inline 'Learn more →' link in #d9d9de. 8px 0px vertical padding.

## Layout

Max-width 1200px centered container, full-bleed colored bands at the section level. Hero is full-viewport-height centered stack: announcement strip at top → sticky nav → display headline with gradient background halo → two inline CTAs → logo strip. Below the hero, sections alternate between full-width light (#f7f7f8) and full-width dark (#1a1a1f) bands with consistent 80px top/bottom vertical padding. Feature sections use a 2-column text-left / interactive-demo-right layout. The component explorer section uses a 2-column layout: accordion list on the left, live auth modal preview on the right. Dark authentication feature grid uses a 3-column card grid at desktop with bento-style spanning cards (one wide center, two flanking). Navigation is a fixed top bar with logo left, text nav center, sign-in + CTA right. No sidebar navigation at the marketing level.

## Imagery

The site relies almost entirely on live UI product screenshots and recreated Clerk component demos rather than photography or illustration. Auth modals (sign-in card, account chooser) are shown as floating white cards with deep shadows, photographed at slight perspective or presented flat. Dark feature sections use stylized product mockups of phones, browser windows, and session management tables rendered at pixel-density on #212126 surfaces. Icons are monochromatic outlined glyphs at ~16-20px with 1.5px apparent stroke weight, shifting between #131316 on light and #9394a1 on dark surfaces. There is no lifestyle photography and no abstract illustration — the visual evidence IS the product. Image density is medium: each feature section uses one contained product mockup surrounded by text, never full-bleed. Mockup edges are sharp with soft drop shadows, not rounded or masked.

## Dos & Donts

### Do

- Use #6c47ff exclusively for filled primary CTAs and heading accent text — never for decorative backgrounds larger than a tag-sized element.
- Apply Geist at -0.035em letter-spacing for any headline 32px and above; fall to -0.015em for 18-24px subheadings.
- Use 16px radius for large cards and modals, 6px for buttons and inputs, and 9999px strictly for pill badges and tags.
- On dark product sections (#212126 canvas), use the inset white shimmer shadow (rgba(255,255,255,0.024)) as the sole border treatment — no explicit border-color.
- Apply the hero radial gradient (violet → yellow → cyan → transparent) only as a background wash behind full-width sections, keeping opacity below 0.25.
- Format all code component references in Söhne Mono with +0.10em tracking and angle-bracket syntax in violet (#6c47ff).
- Keep all page content within a 1200px max-width container; section vertical gaps should be 64-80px.

### Don't

- Do not use Electric Violet (#6c47ff) as a background fill for anything larger than a button or badge — its power comes from scarcity.
- Do not use pure #000000 for body text — always use #131316 (Void Black) or #5e5f6 (Graphite) for reading comfort on white surfaces.
- Do not flatten the dual-surface system into a single light or dark theme — light marketing sections and dark product preview cards must coexist on the same page.
- Do not apply the deep card shadow (rgba(47,48,55,0.2) 0px 15px 35px -5px) to dark obsidian cards — they use only the inset shimmer and have no drop shadow.
- Do not set border-radius to anything other than 6px, 8px, 12px, 16px, or 9999px — the design has no 4px or 24px radii.
- Do not use Söhne Mono outside of code snippets, component labels, and terminal strings — body copy is always Geist.
- Do not add decorative color fills behind section headings on light backgrounds — the hero gradient is the only zone where color washes apply.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- text (primary): #131316
- text (secondary): #5e5f6e
- background (light): #f7f7f8
- background (dark section): #212126
- border / hairline: #d9d9de
- brand accent / primary action: #6c47ff (filled action)

**Example Component Prompts**

1. **Hero section (light):** Full-width section with radial gradient halo (violet→yellow→cyan, max opacity 0.24) over #f7f7f8 canvas. Center-aligned headline at 64px Geist weight 700 #131316 letter-spacing -2.24px, two-line. Subheading at 18px weight 400 #5e5f6e. Two inline buttons: filled violet (#6c47ff, white text, 6px radius, 0px/12px padding) + ghost white (#ffffff border #d9d9de, text #131316, same radius). Vertical gap between headline and buttons: 32px.

2. **Dark obsidian feature card:** Background #212126, 16px radius, inset shadow rgba(255,255,255,0.024) 0px 1px 0px inset + rgba(255,255,255,0.024) 0px 0px 0px 1px inset. Heading 16px Geist weight 600 #ffffff. Body 14px weight 400 #9394a1. No explicit border-color — the inset shimmer IS the border. 24px internal padding.

3. Create a Primary Action Button: #6c47ff background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

4. **Code component label (sidebar):** Söhne Mono 11px weight 500 +0.10em tracking. Format: `<ComponentName />` with `<`, `/>` in #6c47ff, `ComponentName` in #131316. Transparent background. 4px 8px padding. Used in accordion list items.

5. **Feature section (text + demo, 2-col):** Max-width 1200px container. Left column: section eyebrow label (pill badge, #6c47ff text, #eeeef0 bg, 9999px radius, 11px Geist weight 500); heading 32px weight 700 #131316 letter-spacing -1.12px; body 15px weight 400 #5e5f6e line-height 1.56; 8px gap between elements. Right column: white or dark product demo card with 16px radius and appropriate shadow tier. Section top/bottom padding 80px.

### Gradient System

Three gradient tiers serve distinct purposes:

1. **Hero Halo (atmospheric):** `radial-gradient(189.26% 126.1% at 49.27% 0%, rgba(108,71,255,0) 10%, rgba(255,249,99,0.15) 34%, rgba(98,72,246,0.24) 67.53%, rgba(98,72,246,0) 95.38%)` — applied behind hero headlines only, at full section width. The yellow stop (#fff963 at 15% opacity) creates a warm center bloom that prevents the violet halo from reading as plain purple.

2. **Brand Sweep (decorative text/lines):** `linear-gradient(to right, #6c47ff 25%, #5de3ff 75%)` — used for gradient text highlights and horizontal decorative rule accents. Violet-to-cyan mimics a dev-tool terminal palette.

3. **Dark Card Radial (depth):** `radial-gradient(79.87% 92.91% at 81.25% 12.5%, #ffffff 0%, #19191b 100%)` — used inside individual dark cards to add depth without explicit lighting. The white hot-spot is offset top-right, implying a single ambient light source.

### Dark Section Rules

When building sections on #212126 or darker surfaces:
- Card borders: use ONLY inset box-shadow (rgba(255,255,255,0.024)) — no CSS border property
- Text hierarchy: #ffffff for headings, #9394a1 (Stone) for body, #696a78 (Slate Mid) for captions
- Accent color (#6c47ff) remains the same in both light and dark contexts — it has sufficient contrast against both
- Cyan (#5de3ff) is exclusively a dark-section accent — never use it on light backgrounds
- Code blocks inside dark sections use #2f3037 background with the same inset shimmer pattern
- Apply backdrop-filter blur(4-8px) on floating cards within dark sections for glass depth

---
_Source: https://styles.refero.design/style/ed10ae04-24ec-4e42-9bf2-ea12a4b58d67_
