# Descript — Design Reference

> Broadcast booth meets editorial press — deep burgundy theater dark, editorial serif headlines, coral on-air signals.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://descript.com](https://descript.com) |
| Refero page | [https://styles.refero.design/style/fe955d4a-c56d-4ab0-a6b3-8d985ab9570c](https://styles.refero.design/style/fe955d4a-c56d-4ab0-a6b3-8d985ab9570c) |
| Theme | mixed |
| Industry | media |

## Overview

Descript's visual language is deep burgundy darkness cut by coral-red action — like the interior of a recording booth where the walls absorb everything and only the signal glows. The #390a1a near-black burgundy dominates 70% of the hero, creating a theater-dark immersion that makes the coral-red CTA (#f73b3b) feel like an on-air indicator light. Headlines use Gamuth Display, a custom editorial serif at 88px — an unusual choice for a SaaS product that signals craft and content creation rather than enterprise utility. The light sections (#faf8f7, a warm off-white) provide contrast between dark bands without ever going pure white, keeping the palette unified in warmth. Tag labels like 'AI VIDEO EDITOR' use Brett, a custom typeface with wide 0.04em tracking that mimics broadcast chyron styling.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Broadcast Burgundy | `#390a1a` | brand | Hero backgrounds, primary dark surface — creates the theater-dark immersion that makes coral CTAs read as on-air signals |
| On-Air Coral | `#f73b3b` | brand | Primary CTA buttons, active section labels — the single vivid signal against deep burgundy dark |
| Hot Take Red | `#ff5340` | accent | Inline text highlights, secondary accent emphasis in body copy |
| Plum Mid | `#651a39` | brand | Nav hover states, secondary button backgrounds — one step lighter than Broadcast Burgundy for interactive depth |
| Deep Violet | `#0c0b5f` | accent | Inline links and callout text in light sections — a cool contrast to the otherwise warm palette |
| Soft Violet | `#8787e0` | accent | Underlord AI feature accent — used on AI chat interface send button |
| Pale Peach | `#ffe8db` | neutral | Card backgrounds for feature cards in warm sections |
| Blush Mist | `#f1eaed` | neutral | Card backgrounds for testimonial and feature content cards in light sections |
| Studio Black | `#190308` | neutral | Deepest background layer, nav overlay backgrounds |
| Ink Dark | `#1a1a1a` | neutral | Body text, nav text — used across all surfaces |
| Warm Parchment | `#faf8f7` | neutral | Light section page backgrounds, button text on dark — deliberately off-white to maintain warmth against burgundy |
| Pure White | `#ffffff` | neutral | Button text on coral CTAs, card surfaces in chat UI |
| Dusty Rose | `#a28993` | neutral | Secondary body text, de-emphasized labels on dark surfaces |
| Border Mauve | `#907580` | neutral | Card borders, dividers, input borders on dark surfaces |
| Fog Gray | `#e5e7eb` | neutral | Borders and dividers on light surfaces, button borders |
| Muted Plum | `#583f4a` | neutral | Mid-dark card backgrounds in dark sections |
| Steel Mauve | `#d1c7cb` | neutral | Shadows, secondary text on light surfaces |

## Typography

### Booton

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| sizes | 16px, 18px, 20px, 24px, 56px |
| lineHeight | 1.10–1.56 |
| letterSpacing | -0.32px at 16px (−0.02em applied across all sizes) |
| fontFeatureSettings | "calt", "liga" |
| substitute | Source Sans Pro, IBM Plex Sans |
| role | Primary workhorse across UI — nav, body copy, buttons, captions, and feature labels. Weight 600 for subheadings and card titles; weight 400 for body and nav. The custom design gives it a warmth that distinguishes it from commodity grotesques like Inter. |

### Gamuth Display

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 40px, 56px, 88px |
| lineHeight | 1.10–1.20 |
| fontFeatureSettings | "calt", "liga" |
| substitute | Playfair Display, Fraunces |
| role | Hero and section headings only. A custom editorial serif — the signature choice that marks Descript as a content creation tool, not enterprise SaaS. Weight 400 at 88px is confident restraint; most SaaS hero fonts lean heavy. |

### Brett

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 18px |
| lineHeight | 1.00–1.56 |
| letterSpacing | 0.72px at 18px (0.04em) |
| fontFeatureSettings | "calt", "liga" |
| substitute | Courier Prime, Space Mono |
| role | Section eyebrow labels only (e.g. 'AI VIDEO EDITOR', 'AI VIDEO AGENT'). Wide 0.04em tracking mimics broadcast chyron or print section slugs — the only place positive letter-spacing appears in the system. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 16 |  | 1.5 | -0.32 |
| body-sm | 18 |  | 1.56 | -0.36 |
| body | 20 |  | 1.5 | -0.4 |
| subheading | 24 |  | 1.33 | -0.48 |
| heading-sm | 40 |  | 1.2 |  |
| heading | 56 |  | 1.1 |  |
| display | 88 |  | 1.1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 4px |
| badges | 100px |
| inputs | 8px |
| buttons | 12px |
| buttonsPill | 9999px |

- **elementGap** — 16px
- **sectionGap** — 80-120px
- **cardPadding** — 
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Studio Dark | `#190308` | 0 | Deepest background — used behind logo strip and page footer |
| Broadcast Burgundy | `#390a1a` | 1 | Hero and primary dark sections — dominant dark surface |
| Muted Plum | `#583f4a` | 2 | Cards and elevated containers on dark backgrounds |
| Warm Parchment | `#faf8f7` | 3 | Light section backgrounds, nav bar — the opposing pole to the dark surfaces |
| Blush Mist | `#f1eaed` | 4 | Cards and containers on light section backgrounds |
| Pure White | `#ffffff` | 5 | UI components like chat cards that need maximum contrast on any surface |

## Components

### Primary CTA Button Group

### Testimonial Card Grid

### Awards Recognition Card with Category Tags

### Primary CTA Button

**Role:** Main conversion action — 'Get started for free'

Background #f73b3b, text #fff7fd (warm white), border-radius 12px, padding 16px 32px. Booton weight 600 at 18px. No border. The coral against dark backgrounds is the loudest element in the hierarchy.

### Dark Pill Icon Button

**Role:** Media controls and compact icon actions in dark sections

Background #390a1a, border-radius 9999px, padding 12px all sides. Square-ish pill with icon only. #651a39 variant used for hover/active state.

### Sign Up Nav Button

**Role:** Primary nav conversion CTA

Background #651a39 (Plum Mid), text white, border-radius 12px, padding 12px 20px. Sits in sticky white nav — creates a warm burgundy-to-plum contrast against #faf8f7 nav background.

### Eyebrow Label

**Role:** Section category tags above hero headings

Brett typeface, 18px, 0.04em letter-spacing, #f73b3b (On-Air Coral) on dark surfaces, uppercase. No background, no border. Used exclusively to introduce section headings.

### Feature Card — Blush

**Role:** Feature description cards in light sections

Background #f1eaed (Blush Mist), border-radius 4px, padding 24px 24px 32px, no shadow. Booton 600 for card title at 20px, Booton 400 for body at 16px, #1a1a1a text.

### Feature Card — Peach

**Role:** Alternate feature cards with warmer tint

Background #ffe8db (Pale Peach), border-radius 4px, padding 24px 24px 32px, no shadow. Same typography treatment as Blush card.

### Testimonial Card

**Role:** Social proof quotes in grid layout on dark background

Background #583f4a (Muted Plum), border-radius 4px, padding 24px. Booton 400 at 16px, text #faf8f7. Subtle border-color #907580. No shadow.

### AI Chat Interface Card

**Role:** Product UI preview showing Underlord AI assistant

Background #ffffff, border-radius 12px, box-shadow rgba(0,0,0,0.16) 0px 2px 4px. Inner message bubbles use #cdcdfe (muted violet) for AI messages, #8787e0 for send button. Booton 400 at 16px.

### Nav Bar

**Role:** Sticky top navigation

Background #faf8f7, border-bottom #e5e7eb 1px. Text #1a1a1a at 16px Booton 400. Dropdown chevrons as inline icons. Max-height 50px. Contains wordmark (Descript logo with stacked-lines icon in #f73b3b), text nav links, and Sign Up pill button in #651a39.

### Social Proof Logo Strip

**Role:** Trust logos (Canva, Figma, Spotify, etc.)

Full-width band on #390a1a background. Logos rendered in #faf8f7 or desaturated white at reduced opacity. Booton 400 16px label text above in #a28993.

### Category Tag Badge

**Role:** Filter tags and feature category pills

Border 1px solid #907580, background transparent, border-radius 100px, padding 6px 12px. Booton 400 at 14-16px, text #faf8f7 on dark / #1a1a1a on light. Used in Awards card to show product category labels.

### Awards Recognition Card

**Role:** G2 award display in testimonials section

Background #faf8f7, border-radius 4px, padding 24px. Contains G2 badge images, year label in #f73b3b (Brett 18px), category tag badges with transparent background and #907580 borders.

## Layout

Max-width approximately 1200px, centered with generous horizontal padding. Hero is full-bleed #390a1a burgundy dark spanning 100vh with centered headline stack and single CTA. Below hero, alternating dark (#390a1a) and light (#faf8f7) horizontal bands create a clear rhythm — dark for brand immersion, light for feature explanation. Feature sections use 2-3 column card grids with 24px gaps. Testimonials use a 2-column card grid on dark background with an awards card occupying the left column. Social proof logo strip is full-bleed single row between hero and feature sections. Navigation is sticky white bar at top with left-aligned wordmark and right-aligned utility links + CTA button. Section headings are always centered with eyebrow label above and subtext below, max-width ~640px for readability.

## Imagery

Product UI screenshots are the primary visual asset — shown as floating cards partially cropped, overlapping, presented at perspective to simulate depth. No lifestyle photography in screenshots; the product interface IS the visual. UI previews show the Descript editor with real transcript text, building credibility through product exposure. G2 badge art (flat, illustrative award icons) appears in the social proof section. Icons use a filled-with-outline hybrid style at approximately 20-24px, monocolor matching the surface text color. The Underlord AI chat component is showcased as a standalone card with a clean white background floating against the dark hero — product-as-hero framing. All imagery is contained within rounded-corner cards (12px) rather than full-bleed, maintaining the 'exhibit behind glass' feel.

## Elevation philosophy

Shadows appear only on floating UI elements (chat card, nav dropdowns) using a single system: rgba(0,0,0,0.16) 0px 2px 4px. Cards and section containers use background-color contrast alone for separation — no shadow hierarchy. This keeps the dark sections feeling like material surfaces rather than layered interfaces.

## Dos & Donts

### Do

- Use Gamuth Display weight 400 for all H1/H2 hero headlines — never bold or weight 700
- Precede every major section heading with a Brett 18px, 0.04em tracked eyebrow label in #f73b3b
- Apply #f73b3b background with 12px border-radius and 16px/32px padding for all primary CTAs
- Pair #390a1a dark sections immediately with #faf8f7 light sections — maintain alternating band rhythm
- Use 4px border-radius for all cards; reserve 12px for floating UI components and modals
- Set Booton letter-spacing to −0.02em (negative) across all body sizes — never neutral or positive tracking except in Brett eyebrows
- Use #583f4a as the elevated card surface on dark #390a1a backgrounds — never pure black or white cards on dark sections

### Don't

- Never use a pure white (#ffffff) page background — all light surfaces use #faf8f7 (Warm Parchment)
- Never apply Gamuth Display to body copy, captions, or UI labels — it is heading-only at 40px minimum
- Never use the coral CTA (#f73b3b) for more than one button per screen — it functions as a single focal signal
- Never add drop shadows to section cards — background-color contrast alone defines elevation on card surfaces
- Never use a generic sans-serif substitute for Booton without applying −0.02em letter-spacing — positive or zero tracking breaks the warmth
- Never place green, blue, or teal semantic colors in the UI — the system has no cool-hued semantic states; use only the warm coral/burgundy/violet palette
- Never use border-radius above 12px on cards or sections — 9999px is reserved exclusively for compact icon pill buttons

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- Page background (light): #faf8f7
- Page background (dark): #390a1a
- Primary body text: #1a1a1a
- Primary CTA: #f73b3b
- Border / divider: #e5e7eb (light surfaces), #907580 (dark surfaces)
- Accent / AI feature: #8787e0

**Example Component Prompts**

1. Hero Section (dark): Full-width #390a1a background. Eyebrow label in Brett 18px #f73b3b, 0.04em letter-spacing, uppercase. Headline in Gamuth Display weight 400 at 88px, #faf8f7, line-height 1.1, centered. Subtext in Booton 400 18px #a28993, max-width 560px centered. Single CTA button: #f73b3b background, #ffffff text, Booton 600 18px, 12px radius, 16px top/bottom 32px left/right padding.

2. Feature Card (light section): Background #f1eaed, border-radius 4px, padding 24px 24px 32px. Title in Booton 600 20px #1a1a1a. Body in Booton 400 16px #1a1a1a, line-height 1.5. No shadow, no border.

3. Testimonial Card (dark section): Background #583f4a, border-radius 4px, padding 24px, border 1px #907580. Quote text in Booton 400 16px #faf8f7, line-height 1.56. Author name in Booton 600 16px #faf8f7 with small avatar circle.

4. Nav Bar: Background #faf8f7, 1px bottom border #e5e7eb. Left: Descript wordmark with #f73b3b icon. Center: Booton 400 16px #1a1a1a nav links with dropdown arrows. Right: 'Sign in' in Booton 400 16px #1a1a1a, 'Sign up' button in #651a39 background, #ffffff text, 12px radius, 12px 20px padding.

5. Section Eyebrow + Heading (light section): Eyebrow in Brett 400 18px #f73b3b, 0.04em tracking, centered. Heading in Gamuth Display 400 56px #1a1a1a, line-height 1.1, centered, margin-top 8px. Subtext in Booton 400 18px #1a1a1a at 70% opacity, max-width 560px centered, margin-top 16px.

### Three-Font Discipline

This system uses exactly three typefaces with non-overlapping roles — a strict hierarchy:

1. **Gamuth Display** — headings only (40px+). Never in UI, never in body, never in captions.
2. **Brett** — eyebrow labels only (18px, tracked wide). Never for body, never for headings.
3. **Booton** — everything else: nav, body, buttons, cards, captions, UI text.

Violating these boundaries — e.g. using Gamuth Display for a card title at 24px, or Brett for a body paragraph — breaks the system's editorial authority. Substitute fonts should maintain this same three-tier structure.

---
_Source: https://styles.refero.design/style/fe955d4a-c56d-4ab0-a6b3-8d985ab9570c_
