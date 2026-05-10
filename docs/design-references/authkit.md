# Authkit — Design Reference

> Midnight Command Center. Imagine a high-tech dashboard glowing softly in a dark room, with frosted glass elements reflecting subtle light.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://authkit.com](https://authkit.com) |
| Refero page | [https://styles.refero.design/style/e80231a2-e4d6-406a-a2c9-2e6109679690](https://styles.refero.design/style/e80231a2-e4d6-406a-a2c9-2e6109679690) |
| Theme | dark |
| Industry | devtools |

## Overview

AuthKit's design evokes an 'internal dashboard' feel — polished and functional without being sterile. Dark, translucent surfaces glow with subtle inner shadows and a restrained use of soft, muted blues. The sharp contrast between the deep `Midnight Abyss` background and crisp white text, paired with a limited palette of nearly achromatic blues, creates a sense of digital precision. The deliberate mix of round (`Pill`) and subtly rounded (`Subtle`) corners on different elements adds a tactile quality to the otherwise sleek, high-tech aesthetic, hinting at user-friendliness within a complex system.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Abyss | `#05060f` | neutral | Page backgrounds, elevated card backgrounds, deep shadows. |
| Ghost White | `#ffffff` | neutral | Primary text for headings and high-contrast elements, icon fills. |
| Storm Gray | `#2f343` | neutral | Subtle shadows, secondary background for interactive states. |
| Comet | `#d8ecf8` | brand | Primary body text, prominent links, and headings; provides high readability against dark backgrounds. |
| Arctic Mist | `#d1e4fa` | brand | Secondary body text, icon outlines, button text; a cool, muted off-white for softer emphasis. |
| Celestial Light | `#b6d9fc` | brand | Focus states for interactive elements, subtle highlights. |
| Azure Glow | `#c7d3ea` | neutral | Less prominent body text, disabled states, and subtle borders; a desaturated blue-gray that recedes gracefully. |
| Slate Dew | `#3f4959` | neutral | Outline for informational badges and subtle accents. |
| Whisper Blue | `#9da7ba` | neutral | Placeholder text in inputs, less important body text. |
| Neon Violet | `#663af3` | accent | Action buttons and primary interactive elements; a vibrant, focused color against the dark backdrop. |
| Interstellar Gray | `#81899b` | neutral | Faint text color for small captions or secondary labels. |
| Twilight Gradient Overlay | `#d8ecf8` | brand | Subtle background element for atmospheric effect, indicating light source. |
| System Highlight Border | `#bacff7` | brand | Interactive element borders and inner glows, creating a subtle sci-fi effect. |

## Typography

### Untitled Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 14px, 16px, 18px, 24px |
| lineHeight | 1.17, 1.20, 1.33, 1.43, 1.50, 2.29, 2.57 |
| letterSpacing | -0.01 |
| substitute | Inter |
| role | Used for all general body text, form elements, button labels, and secondary information. The slight negative letter spacing creates a compact, refined feel. |

### aeonikPro

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 28px, 44px, 48px |
| lineHeight | 1.14, 1.16, 1.17, 1.20 |
| letterSpacing | 0 |
| substitute | Space Grotesk |
| role | Exclusively for prominent headings and display text. Its distinct, clean geometry defines the primary content hierarchy and brand voice, appearing slightly wider than Untitled Sans for visual contrast. |

### dotDigital

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 15px |
| lineHeight | 1.20 |
| letterSpacing | 0.1 |
| fontFeatureSettings | "tnum" |
| substitute | IBM Plex Mono |
| role | Used for specific, small informational text. The `tnum` font feature setting ensures consistent monospaced numbers, ideal for technical details or data display. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.01 |
| body | 14 |  | 1.5 | -0.01 |
| body-lg | 16 |  | 1.5 | -0.01 |
| subheading | 18 |  | 1.43 | -0.01 |
| heading | 24 |  | 1.33 | -0.01 |
| heading-lg | 28 |  | 1.2 | 0 |
| display | 44 |  | 1.16 | 0 |
| display-xl | 48 |  | 1.14 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 999px |
| cards | 12-16px |
| badges | 6px |
| inputs | 2-4px |
| buttons | 999px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Components

### Login Form Card

### Dark/Light Mode Toggle + Feature Badges

### Section Heading + Glassy Feature Cards

### Primary Pill Button

**Role:** Action

Ghost White text on a translucent background (rgba(186, 214, 247, 0.06)) with a subtle Ghost White border. Radius is `Pill` (999px) for a soft, approachable action element. Padding: 0px vertical, 16px horizontal. Background rgba(186, 214, 247, 0.06), text #ffffff, border color #ffffff.

### Secondary Outline Button

**Role:** Secondary action

Arctic Mist text on a transparent background with a subtle border (rgba(186, 215, 247, 0.12)). Radius is `Pill` (999px). Padding: 0px vertical, 16px horizontal. Text #d1e4fa, border color rgba(186, 215, 247, 0.12).

### Solid Primary Button

**Role:** Call to Action

White text on `Neon Violet` background (#663af3) with 0px padding and 6px border radius. Used for critical actions like 'Continue'.

### Glassy Feature Card

**Role:** Information Display

Transparent background (rgba(186, 214, 247, 0.03)) with `Subtle` (12px) border radius. Features a complex inner shadow: `rgba(199, 211, 234, 0.12) 0px 1px 1px 0px inset, rgba(199, 211, 234, 0.05) 0px 24px 48px 0px inset, rgba(6, 6, 14, 0.7) 0px 24px 32px 0px` to create a deep, frosted glass effect.

### Login Form Card

**Role:** Data Input Container

Near-black background (rgba(5, 6, 15, 0.97)) and `Subtle` (16px) border radius. Distinct inner shadows: `rgba(216, 236, 248, 0.2) 0px 1px 1px 0px inset, rgba(168, 216, 245, 0.06) 0px 24px 48px 0px inset, rgba(0, 0, 0, 0.3) 0px 16px 32px 0px` to suggest depth and a light source from above.

### Minimal Input Field

**Role:** Text Input

White text and Ghost White border on translucent background (rgba(199, 211, 234, 0.06)). Border radius varies from 2px to 4px. Padding: 0px vertical, 10px horizontal. Placeholder text uses `Whisper Blue` (#9da7ba).

### Status Badge

**Role:** Metatag / Categorization

Arctic Mist text on `Midnight Abyss` background, with `Subtle` (6px) border radius. Padding: 4px vertical, 8px horizontal. Text #d1e4fa, background #05060f.

### Icon Button

**Role:** Small interactive element

Transparent background with a subtle border matching `Secondary Outline Button` (rgba(186, 215, 247, 0.12)) and `Pill` (999px) border radius. Contains icons which are typically `Ghost White`.

## Layout

The page uses a full-bleed dark background (`Midnight Abyss`) with content contained to a logical max-width. The hero section features a prominent, centered headline over a subtle background gradient and atmospheric glow. Sections follow a consistent top-to-bottom flow, with `sectionGap` providing generous vertical rhythm. Content often appears as centered stacks, sometimes with embedded UI components (like the login form) serving as visual focal points. Navigation is a simple, sticky top bar.

## Imagery

Minimalist and highly stylized. Photography is absent. Illustrations are abstract, geometric forms with subtle glows and transparent layers, often depicting UI components or data structures in a 'blueprint' or 'schematic' style against the dark background. Icons are simple, filled or outlined glyphs with high contrast, often set within softly rounded or pill-shaped containers. The imagery's role is primarily decorative atmosphere and conceptual explanation rather than literal depiction, with images sparsely used and serving as visual anchors in content sections. Overall density is text-dominant, allowing UI elements and graphics to 'breathe' in the dark space.

## Dos & Donts

### Do

- Prioritize `Midnight Abyss` (#05060f) as the primary background color for all main page sections and large surface areas.
- Use `aeonikPro` (sub. Space Grotesk) for all marketing headlines (28-48px) and `Untitled Sans` (sub. Inter) for all body copy and UI elements (12-24px).
- Apply `Pill` (999px) radius to all primary and secondary action buttons, and `Subtle` (12-16px) radius to cards and containers.
- Employ the complex inner shadow `rgba(199, 211, 234, 0.12) 0px 1px 1px 0px inset, rgba(199, 211, 234, 0.05) 0px 24px 48px 0px inset, rgba(6, 6, 14, 0.7) 0px 24px 32px 0px` on elevated cards to create visual depth.
- Reserve `Neon Violet` (#663af3) exclusively for critical call-to-action buttons, maintaining its impact.
- Use subtle linear gradients for decorative elements, such as `linear-gradient(90deg, rgba(0, 0, 0, 0), rgba(186, 215, 247, 0.12), rgba(0, 0, 0, 0))` for dividers or highlights.

### Don't

- Avoid using highly saturated, non-brand colors outside of the designated `Neon Violet` accent.
- Do not use generic drop shadows; instead, utilize the specified `inset` shadows and soft outer glows to achieve depth.
- Do not deviate from the specified font families; their visual distinction is core to the brand identity.
- Do not apply standard rectangular shapes to buttons; all interactive buttons should use `Pill` (999px) radius.
- Avoid using flat, opaque background colors for cards; instead, use translucent backgrounds with subtle inner shadows to maintain the 'frosted glass' effect.
- Do not use letter-spacing on display headings; `aeonikPro` should maintain `normal` letter spacing at larger sizes, while `Untitled Sans` uses a subtle negative spacing.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Text (Primary):** #d8ecf8
- **Background (Primary):** #05060f
- **CTA (Primary):** #663af3
- **Border (Subtle):** rgba(186, 215, 247, 0.12)
- **Accent (Highlight):** #b6d9fc

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Set background to `Midnight Abyss` (#05060f). Center a headline: 'AuthKit' using `aeonikPro` 48px, `Ghost White` (#ffffff), no letter spacing. Below it, a subheadline: 'The world’s best login box, powered by WorkOS + Radix.' using `Comet` (#d8ecf8) 24px `Untitled Sans`. Add a 'Get started' button: `Primary Pill Button` style.
2. **Generate a Login Form Card:** Use `Login Form Card` component style with `Midnight Abyss` background and 16px radius, including its specific inner shadow `rgba(216, 236, 248, 0.2) 0px 1px 1px 0px inset, rgba(168, 216, 245, 0.06) 0px 24px 48px 0px inset, rgba(0, 0, 0, 0.3) 0px 16px 32px 0px`. Inside, include an 'Email' input: `Minimal Input Field` style. Follow with a 'Continue' button: `Solid Primary Button` style. Stack elements vertically with an `elementGap` of 16px.
3. **Design a Feature Grid Section:** Use a transparent background (rgba(186, 214, 247, 0.01)) within a main section (section gap: 48px from top/bottom). Create a 3-column grid of `Glassy Feature Card` components. Each card should contain a headline using `Untitled Sans` 18px 600, Arctic Mist text, and a `Ghost White` icon. Place a `Status Badge` (e.g., 'Single Sign-On') below the headline inside each card. `elementGap` between title, text, and badge should be 8px.

---
_Source: https://styles.refero.design/style/e80231a2-e4d6-406a-a2c9-2e6109679690_
