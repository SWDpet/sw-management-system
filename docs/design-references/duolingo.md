# Duolingo — Design Reference

> Playground Starter Kit

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://duolingo.com](https://duolingo.com) |
| Refero page | [https://styles.refero.design/style/7088d695-362b-4e09-b325-fa8136d4f350](https://styles.refero.design/style/7088d695-362b-4e09-b325-fa8136d4f350) |
| Theme | light |
| Industry | other |

## Overview

The design feels like an energetic, gamified classroom. Its core is built on a trinity of exuberant choices: the plump, ultra-rounded 'Feather' headline font, the vibrant 'Duo Green' that saturates all primary actions and logos, and the cast of charming, blob-like character illustrations. The layout uses vast white space as a clean canvas, making these colorful elements pop. A signature detail is the 3D-style button, which uses a solid bottom shadow to feel tactile and pressable, a stark contrast to the otherwise flat UI. The entire experience is crafted to feel fun, friendly, and encouraging, turning language learning from a chore into a game.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Duo Green | `#58cc02` | brand | Primary CTAs, logos, headlines, interactive highlights — the brand's key signifier of action and identity. |
| Sky Blue | `#1cb0f6` | accent | Secondary outline buttons, inline text links — provides a clear, alternative interactive cue. |
| Duo Green Light | `#d7ffb8` | brand | Background tints for highlighted or active states, often paired with Duo Green. |
| Sunshine Yellow | `#ffc700` | accent | Used exclusively within illustrations for pops of warmth and energy. |
| Grape Soda | `#a570ff` | accent | Used exclusively within illustrations as a cool, playful accent. |
| Bubblegum Pink | `#cc348d` | accent | Used exclusively within illustrations for vibrant, friendly details. |
| Snow White | `#ffffff` | neutral | Page backgrounds, button text, card surfaces. |
| Cloud Gray | `#e5e5e5` | neutral | Borders for secondary buttons and dividers. |
| Silver | `#afafaf` | neutral | Placeholder text, disabled states, secondary info text. |
| Graphite | `#777777` | neutral | Body copy, descriptive text. |
| Charcoal | `#4b4b4b` | neutral | Subheadings, secondary headlines. |
| Almost Black | `#3c3c3c` | neutral | Primary body and UI text. |

## Typography

### feather

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 48px, 64px |
| lineHeight | 1.20 |
| letterSpacing | -0.02em |
| substitute | Fredoka One, Baloo 2 |
| role | Used exclusively for large, impactful headlines (H1, H2). Its extremely rounded, heavy, and slightly condensed character gives the brand its signature playful and confident voice. |

### din-round

| Key | Value |
| --- | --- |
| weight | 500, 700 |
| sizes | 13px, 14px, 15px, 17px, 19px, 32px |
| lineHeight | 1.15-1.47 |
| letterSpacing | 0.053em |
| substitute | Nunito Sans, Varela Round |
| role | The workhorse font for all UI text, body copy, and buttons. The noticeably wide letter-spacing (`0.053em`) is a key trait, creating a very open and readable texture. Weight 700 is used for buttons and emphasized text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.4 | 0.69 |
| body | 15 |  | 1.4 | 0.8 |
| heading-sm | 19 |  | 1.2 | 1.01 |
| heading | 32 |  | 1.2 | 1.7 |
| heading-lg | 48 |  | 1.2 | -0.96 |
| display | 64 |  | 1.2 | -1.28 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| inputs | 12px |
| buttons | 12px |

- **elementGap** — 16px
- **sectionGap** — 80-120px
- **cardPadding** — 24px
- **pageMaxWidth** — 1140px

## Components

### CTA Button Group

### Language Selector Bar

### Feature Section — Free Fun Effective

### Green Headline

**Role:** Feature section titles like 'free. fun. effective.'

Uses the 'feather' font at 48px or 64px with '700' weight and tight letter-spacing (-0.02em). The color is 'Duo Green' ('#58cc02'), making it a vibrant focal point.

### Character Illustration

**Role:** Visual anchors for every major page section.

Large, organic vector illustrations featuring diverse, playful characters. Built with a palette of 'Duo Green', 'Sunshine Yellow', 'Grape Soda', and 'Bubblegum Pink'. They are flat but use simple layering and occasional gradients for depth.

### Inline Text Link

**Role:** Clickable text within a paragraph.

Text is colored 'Sky Blue' ('#1cb0f6') and often includes a subtle underline on hover. It uses the standard 'din-round' body font.

### Language Flag Item

**Role:** Used in the language selector list.

A small rectangular flag icon followed by uppercase text (e.g., 'ENGLISH') in 'Graphite' ('#777777'). Uses the 'din-round' font. The whole item is a link.

## Layout

The site uses a centered, max-width layout (approx. 1140px) on an expansive white background. The hero section is asymmetric, with a large illustration on the left and a text block with CTAs on the right. Below the hero, the page flows in generous, vertically-spaced sections. Most sections are either single-column centered text blocks or two-column layouts that alternate between `illustration-left, text-right` and vice-versa. There are no colored background containers; the content and illustrations 'float' on the white page, creating an open, airy, and focused structure.

## Imagery

The visual language is defined by a universe of custom vector illustrations. These are not decorative; they are central characters. The style is flat, friendly, and organic, featuring blobby shapes, simple features, and a diverse cast of people and mascots. The color palette for illustrations is an extension of the brand's core colors, adding warm yellows, purples, and pinks. These illustrations are large, often taking up half the screen width, and are always paired with a key message or feature, making the abstract concepts of learning feel tangible and fun.

## Elevation philosophy

The system is intentionally flat, avoiding traditional shadows for elevation. Depth is created exclusively on primary buttons using a solid, darker-hue bottom border (emulated via `box-shadow`) that mimics a physical button pad. All other elements like cards and containers remain flat on the Snow White page background.

## Dos & Donts

### Do

- Use 'Duo Green' '#58cc02' for all primary CTAs and brand-voice headlines.
- Apply a 12px border-radius to all interactive UI components like buttons and inputs.
- Use the 'feather' font exclusively for large, impactful headlines (48px+).
- Create depth on primary buttons with a solid, darker green bottom 'shadow' (e.g., `box-shadow: 0 4px 0 #3f8f01`).
- Pair every major content section with a large, on-brand character illustration.
- Use 'Sky Blue' '#1cb0f6' for all secondary interactive elements like outline buttons and text links.
- Set body copy and UI text with 'din-round' and its distinctive `letter-spacing: 0.053em`.

### Don't

- Don't use sharp corners on any UI element.
- Don't use any color other than 'Duo Green' '#58cc02' for the main 'Get Started' action.
- Don't use the 'feather' headline font for small text or body copy.
- Don't apply traditional `box-shadow` for elevation on panels or cards.
- Don't create text links in any color other than 'Sky Blue' '#1cb0f6'.
- Don't use system fonts; the custom 'feather' and 'din-round' styles are integral to the brand.
- Don't design a section without considering its accompanying illustration.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Page Background**: `#ffffff` (Snow White)
- **Primary Text**: `#3c3c3c` (Almost Black)
- **Primary CTA**: `#58cc02` (Duo Green)
- **Secondary Action**: `#1cb0f6` (Sky Blue)
- **Borders**: `#e5e5e5` (Cloud Gray)

### Example Component Prompts
1. **Primary Button**: "Create a button with 'GET STARTED' text. Background is '#58cc02', text is '#ffffff'. Use a 12px border-radius. Font is 'din-round' at 15px, weight 700. Padding is 16px 32px. Add a `box-shadow: 0 4px 0 #3f8f01`."
2. **Headline**: "Create a headline 'free. fun. effective.'. Font is 'feather' at 64px, weight 700. Color is '#58cc02'. Set letter-spacing to -1.28px."
3. **Outline Button**: "Create an outline button with 'I ALREADY HAVE AN ACCOUNT' text. Background is transparent. Text color is '#1cb0f6'. Border is 2px solid '#e5e5e5'. Use a 12px border-radius. Font is 'din-round' at 15px, weight 700. Padding is 14px 24px."

---
_Source: https://styles.refero.design/style/7088d695-362b-4e09-b325-fa8136d4f350_
