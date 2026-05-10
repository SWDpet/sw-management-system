# dope.security — Design Reference

> Celestial command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://dope.security](https://dope.security) |
| Refero page | [https://styles.refero.design/style/e1f18a7e-5af1-46b3-8f89-bce6c78b80d4](https://styles.refero.design/style/e1f18a7e-5af1-46b3-8f89-bce6c78b80d4) |
| Theme | dark |
| Industry | saas |

## Overview

Dope.security establishes a celestial-tech aesthetic: dark, ethereal backgrounds evoke a night sky, contrasted by sharp, almost glowing typography. Translucent frosted glass elements and subtle, vibrant gradients create a sense of advanced technology. The system balances highly stylized display fonts for impact with clean, legible sans-serifs for detail, and uses a single strong violet accent to highlight critical actions, ensuring focus within the dark UI.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Eclipse | `#090909` | neutral | Page backgrounds, card containers, dark base for interactive elements |
| Cloud Whisper | `#f7f9fa` | neutral | Primary text, headers, icon fills, and borders on dark backgrounds. Creates bright contrast |
| Code Ghost | `#f0f0f0` | neutral | Secondary text in lists, code snippets, and specific heading styles. Slightly softer than Cloud Whisper |
| Slate Hint | `#6b6b6b` | neutral | Muted text, iconography, and subtle borders for outlines or inactive states |
| Steel Accent | `#475467` | neutral | Borders for ghost buttons and subtle accent text, especially in navigation |
| Deep Violet | `#af50ff` | brand | Primary action background, interactive element highlights, and brand accents. Provides a vivid focal point |
| Cosmic Gradient A | `#6c4bd6` | accent | Hero section background, atmospheric graphic elements. Creates a deep, dramatic sky effect |
| Cosmic Gradient B | `#401860` | accent | Background for feature cards, creating a dynamic, blended surface |

## Typography

### Whyte Inktrap

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| sizes | 10px, 12px, 14px, 16px, 18px, 20px, 24px, 28px, 32px, 40px, 48px, 49px, 50px, 64px, 80px, 88px |
| lineHeight | 0.80, 0.90, 1.00, 1.11, 1.20, 1.25, 1.50, 1.56, 1.60 |
| letterSpacing | -0.07em at 88px, -0.04em at 64px, -0.03em at 50px, -0.01em at 24px, normal at 16px and below |
| substitute | Montserrat, sans-serif |
| role | Primary UI text, body copy, subheadings, and some display text. The variable letter-spacing provides a compressed, technical feel at larger sizes but remains legible at smaller scales. |

### Whyte Inktrap Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 74px |
| lineHeight | 0.90, 1.50 |
| letterSpacing | 0.2em |
| substitute | Space Mono, monospace |
| role | Monospaced text for specific section headings, code examples, and technical annotations. The wide letter-spacing gives a distinct, almost coded aesthetic. |

### GrandSlang

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 32px, 50px, 64px, 88px, 146px |
| lineHeight | 0.80, 1.20, 1.25, 1.50 |
| letterSpacing | -0.03em |
| substitute | Playfair Display, serif |
| role | Hero headlines and large display marketing text. The lighter weights and tight tracking create a distinguished, authoritative presence that feels modern yet substantial. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 16px |
| lineHeight | 1.5 |
| role | system-ui — detected in extracted data but not described by AI |

### Karla

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 16px |
| lineHeight | 1, 1.2 |
| role | Karla — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0 |
| body-sm | 14 |  | 1.5 | 0 |
| body | 16 |  | 1.5 | 0 |
| subheading | 24 |  | 1.5 | -0.24 |
| heading-sm | 32 |  | 1.25 | -0.96 |
| heading | 50 |  | 0.9 | -1.5 |
| heading-lg | 74 |  | 0.9 | 1.48 |
| display | 88 |  | 0.8 | -6.16 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 99px |
| cards | 19.2px |
| buttons | 8px |
| circular | 10000px |
| pillButtons | 1584px |
| smallWidgets | 10.8px |

- **elementGap** — 16px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Space Canvas | `#090909` | 0 | Primary page background, base for all content sections. |
| Frosted Pane | `#00000000` | 1 | Translucent elements within sections, often with backdrop blur, such as informational cards or content containers in headers. |
| Gradient Field | `#401860` | 2 | Layered backgrounds for feature cards or interactive modules, providing subtle color variation through gradients. |

## Components

### Primary Filled Button

**Role:** Call to action.

Filled with Deep Violet (#af50ff), white text (#f7f9fa), 8px border-radius, 16px vertical and 16px horizontal padding. Font: Whyte Inktrap, weight 700.

### Ghost Button (Primary)

**Role:** Secondary call to action.

Transparent background, Steel Accent (#475467) text and border, 0px border-radius, 10.4px top/bottom padding, 0px left/right padding. Used for subtle navigation or inline actions.

### Pill Button

**Role:** Interactive filters or small, rounded CTAs.

Background color rgba(237, 195, 196, 0.05), Cloud Whisper text (#f7f9fa), 1584px border-radius, 20px vertical and 32px horizontal padding. Creates a soft, distinct interactive element.

### Small Ghost Button

**Role:** Tertiary actions or compact navigation items.

Background rgba(247, 249, 250, 0.08), Cloud Whisper text (#f7f9fa), 6px border-radius, 9px vertical and 15px horizontal padding. Used for less prominent actions.

### Frosted Card (Large)

**Role:** Content segmentation and informational display, primarily in hero sections.

Transparent background (rgba(0,0,0,0)), 19.2px border-radius, no shadow. Content within uses Cloud Whisper and Slate Hint for text.

### Frosted Card (Small)

**Role:** Nested content or smaller feature blocks.

Transparent background (rgba(0,0,0,0)), 10.8px border-radius, no shadow.

### Navigation Link

**Role:** Primary navigation.

Cloud Whisper (#f7f9fa) text, Whyte Inktrap, 16px, weight 400. Inactive text uses Slate Hint (#6b6b6b). Active state indicated by a 1px bottom border in Deep Violet (#7f56d9).

### Text Block Heading (Mono)

**Role:** Section titles presenting technical concepts or lists.

Code Ghost (#f0f0f0) text, Whyte Inktrap Mono, 74px, weight 400, letter-spacing 0.2em. Rendered all caps to enhance the technical, coded feel.

## Layout

The page uses a maximum-width contained layout, typically setting content within a 1200px constraint, though some hero elements breach this for full-bleed atmospheric effects. The hero section is full-bleed, featuring a dark, gradient background with a prominent centered headline and interactive elements (frosted cards for calls to action). Subsequent sections alternate between uniform dark backgrounds and gradient-infused card arrays. Content arrangement often features centered stacks for headlines and subtext, and symmetrical multi-column grids for feature comparison or lists, with visual dividers enhancing flow. Vertical rhythm is consistent with a 64px section gap. A sticky top navigation bar provides continuous access to main categories and action buttons.

## Imagery

The imagery aesthetic is characterized by abstract, ethereal graphics and heavily stylized product illustrations/icons. Photography is minimal, if present. Visuals like the 'vapor trail' airplane and cloud formations reinforce the celestial-tech theme. Icons are predominantly outlined (`stroke-width` suggests a thin weight), monochromatic, and often integrated into button-like elements. Imagery is used decoratively to establish atmosphere rather than convey explicit product features, often interacting with text or UI elements through transparency. Density is text-dominant, with imagery serving as atmospheric backdrops or small, functional accents.

## Dos & Donts

### Do

- Prioritize Midnight Eclipse (#090909) for all large background areas and surfaces to maintain the dark theme.
- Use Cloud Whisper (#f7f9fa) for primary text and critical information to ensure high contrast and legibility.
- Apply Deep Violet (#af50ff) exclusively for primary calls to action, active states, and small, potent brand accents.
- Employ Whythe Inktrap with negative letter-spacing for large headlines to create a condensed, forceful typography style.
- Utilize GrandSlang for hero headlines, leveraging its light weights and tight tracking for a sophisticated, high-impact statement.
- Integrate frosted glass effects (using `backdrop-filter: blur(10px)` or similar) on modals or prominent UI elements to complement the celestial-tech aesthetic.
- Maintain consistent 8px for button radii and 19.2px for card radii to reinforce the subtle rounding throughout the UI.

### Don't

- Avoid using saturated colors other than Deep Violet without specific design system approval, to preserve the monochrome base and accent strategy.
- Do not introduce heavy drop shadows or strong borders on cards; surfaces should primarily be transparent or use subtle blur effects.
- Refrain from using generic sans-serifs for headlines; GrandSlang and Whyte Inktrap are essential for brand typographic identity.
- Do not use generic system fonts for any primary content or UI elements; use Whyte Inktrap for all standard text roles.
- Avoid arbitrary text colors; all text should derive from Cloud Whisper, Code Ghost, Slate Hint, or Steel Accent based on hierarchy.
- Do not clutter layouts with excessive imagery; the aesthetic is text-dominant with strategic visual elements.
- Avoid inconsistent spacing; adhere to the 16px element and card padding, and 64px section gap for vertical rhythm.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #f7f9fa
background: #090909
border: #6b6b6b
accent: #af50ff
primary action: #af50ff (filled action)

Example Component Prompts:
1. Create a Hero Headline: Text 'Your new Secure Web Gateway' using GrandSlang, weight 300, size 88px, #f7f9fa, letter-spacing -6.16px, line-height 0.8. Position centrally over Cosmic Gradient A background.
2. Build a Primary Action Button for 'Get Started': Filled with Deep Violet (#af50ff), Cloud Whisper text (#f7f9fa), 8px border-radius, 16px padding on all sides. Font: Whyte Inktrap, weight 700.
3. Design a Frosted Feature Card: Transparent background (rgba(0,0,0,0)), 19.2px border-radius. Inner heading (Whyte Inktrap, 24px, #f7f9fa, letter-spacing -0.24px).
4. Create a Monospace Section Title: Text 'BLOCK PERSONAL EMAIL' using Whyte Inktrap Mono, weight 400, size 74px, #f0f0f0, letter-spacing 1.48px. Background is Midnight Eclipse (#090909).

---
_Source: https://styles.refero.design/style/e1f18a7e-5af1-46b3-8f89-bce6c78b80d4_
