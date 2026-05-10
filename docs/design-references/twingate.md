# Twingate — Design Reference

> Midnight Terminal, Pulsing Neon. A dark, digital interface illuminated by precise, electric glows.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://twingate.com](https://twingate.com) |
| Refero page | [https://styles.refero.design/style/0acef011-07da-4416-b874-ccdd675140f6](https://styles.refero.design/style/0acef011-07da-4416-b874-ccdd675140f6) |
| Theme | dark |
| Industry | saas |

## Overview

This system projects an image of controlled innovation, like a high-tech console in a dimly lit server room. Dark, near-black surfaces create a sophisticated backdrop for critical information, highlighted by precise, vibrant accents. The interplay between the whispering 'TT Hoves Light' headlines and the sharp, functional 'TT Hoves Medium' body copy establishes a tone of serious capability. Strategic splashes of saturated violet and vivid green draw attention to key interactive elements and provide a subtle, energetic flicker against the subdued palette.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Carbon | `#0e0f11` | neutral | Primary background for pages and main content areas, providing a deep, consistent canvas for the UI. |
| Basalt | `#141617` | neutral | Background for certain elevated components and sections, creating subtle depth against the Carbon base. |
| Obsidian | `#1d2023` | neutral | Background for secondary elements and interactive components, offering a slightly lighter dark tone. |
| Platinum | `#ffffff` | neutral | Primary text color for headings and key information against dark backgrounds, ensuring high contrast and immediate legibility. |
| Graphite | `#a1a1aa` | neutral | Secondary text color, subtle links, and supporting descriptive text, providing contrast without visual dominance. |
| Ash | `#cfcfd3` | neutral | Tertiary text and subtle distinctions, used for less prominent information or disabled states. |
| Spectral Violet | `#b6abff` | brand | Primary interactive accent color for CTA buttons, active states, and highlighted elements — adding an authoritative yet inviting feel. |
| Electric Lime | `#eef35f` | brand | Secondary accent color for spotlighting headlines, badges, or specific product features, signaling innovation and freshness. |
| System Teal | `#00cbaa` | accent | Used for specific body text elements and borders, offering a cool, technical highlight. |
| Deep Violet | `#6350dd` | accent | Background for certain decorative elements or internal blocks. |

## Typography

### TT Hoves Light

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| weights | 300, 400 |
| sizes | 22px, 24px, 48px, 54px, 55px, 62px, 68px |
| lineHeight | 1.10, 1.20, 1.30, 1.50 |
| letterSpacing | -0.018em at 68px, -0.016em at 62px, -0.010em at 54px/55px, -0.009em at 48px, -0.007em at 22px/24px |
| substitute | Open Sans Light |
| role | Headlines and display text. The distinctive light weights at large sizes create an impression of quiet authority and modern elegance, contrasting with the often bolder choices of competitors. This 'whisper, don't shout' approach is a core brand identifier. |

### TT Hoves Medium

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 11px, 12px, 13px, 14px, 15px, 16px, 17px, 20px, 32px |
| lineHeight | 1.04, 1.12, 1.13, 1.14, 1.20, 1.27, 1.29, 1.50 |
| letterSpacing | -0.020em at 12px, -0.010em at 14px, 0.012em at 11, 0.023em at 13px |
| substitute | Open Sans Medium |
| role | Sub-headings, prominent body text, and key UI labels. Provides legibility and clear hierarchy due to its moderate weight, supporting the whisper-weight headlines without competing with them. |

### TT Hoves Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 11px, 12px, 14px, 15px, 16px, 17px |
| lineHeight | 1.14, 1.20, 1.31, 1.50, 1.70 |
| letterSpacing | -0.010em at 16px, 0.014em at 12px |
| substitute | Open Sans Regular |
| role | Standard body text, descriptive copy, and general UI elements. Its neutrality ensures readability for extended passages. |

### Basis Grotesque Mono Pro Medium

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px |
| lineHeight | 1.50 |
| substitute | IBM Plex Mono Medium |
| role | Used for code snippets, technical details, or unique data representations, leveraging a mono typeface for clarity and distinction. |

### Basis Grotesque Mono Pro Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px, 21px |
| lineHeight | 1.20, 1.60 |
| substitute | IBM Plex Mono Regular |
| role | Complementary to the Medium weight mono font, used for inline code or smaller technical annotations. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.14 |  |
| body | 16 |  | 1.7 | -0.16 |
| subheading | 22 |  | 1.3 | -0.154 |
| heading-sm | 32 |  | 1.27 |  |
| heading | 48 |  | 1.2 | -0.432 |
| heading-lg | 54 |  | 1.1 | -0.54 |
| display | 68 |  | 1.1 | -1.224 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 20px |
| cards | 12px |
| buttons | 50px |
| default | 8px |

- **elementGap** — 4px
- **sectionGap** — 80px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Hero CTA Button Group

### Feature Cards — Security, Performance, Simplicity

### Testimonial Card with G2 Rating

### Primary CTA Button

**Role:** Key interaction for conversions

Background: Spectral Violet (#b6abff), Text: Platinum (#ffffff), Padding: 0px vertical, 14px horizontal. Border Radius: 50px. Subtle inset shadow rgba(255, 255, 255, 0.1) 0px 2.5px 0px -2px.

### Secondary CTA Button (Outlined)

**Role:** Alternative call to action, less prominent than primary

Background: Carbon Light (rgba(245, 245, 255, 0.1)), Text: Platinum (#ffffff), Border color: Spectral Violet (#b6abff), Border Radius: 50px, Padding: 0px vertical, 14px horizontal.

### Tertiary CTA Button (Ghost)

**Role:** Further action, or navigation, minimal visual weight

Background: Transparent, Text: Platinum (#ffffff), Border color: Transparent, Border Radius: 50px, Padding: 0px vertical, 14px horizontal. Text has an underline effect that appears on hover/active state (implied by `color=rgb(0,0,238)` in data).

### Feature Tag Button

**Role:** Small, informative labels or interactive filters

Background: Obsidian (#1d2023), Text: Platinum (#ffffff). Border Radius: 20px, Padding: 4px vertical, 14px horizontal.

### Hero Headline

**Role:** Primary page title

Font: TT Hoves Light, Weight: 300, Size: 68px, Line-height: 1.1, Letter-spacing: -0.018em. Color: Electric Lime (#eef35f) or Platinum (#ffffff) for emphasis.

### Body Text

**Role:** Standard paragraph text

Font: TT Hoves Regular, Weight: 400, Size: 16px, Line-height: 1.7. Color: Graphite (#a1a1aa).

### Navigation Link

**Role:** Primary site navigation items

Font: TT Hoves Regular, Weight: 400, Size: 14px, Line-height: 1.5. Color: Platinum (#ffffff). Hover/active state involves a subtle text color change or underline.

## Layout

The layout is primarily centered and contained, with a clear max-width for content blocks, creating a structured and information-focused presentation. The hero section is full-bleed, featuring a dark background with a prominent, left-aligned headline and a stylized product graphic on the right. Content sections generally alternate between text-heavy and visual-heavy arrangements, often with two-column split layouts (text left, image right, or vice-versa). Vertical spacing between sections is generous and consistent, creating clear breaks. A sticky top navigation bar provides constant access to primary links and CTAs. Feature sections often employ multi-column grids (e.g., 3-column cards for benefits or testimonials) within the contained width.

## Imagery

The visual language focuses on abstract, geometric diagrams and stylized product screenshots, rather than photography. These graphics often feature a monochrome (Carbon/Platinum) base with glowing highlights in System Teal (#00cbaa) or Spectral Violet (#b6abff), emphasizing connectivity, security, and data flow. Icons are outlined, with a moderate stroke weight, and monochromatic or tinted to match the accent colors. Full-bleed backgrounds carry abstract patterns subtly. Imagery functions primarily to explain complex concepts and to visually reinforce the brand's technical and secure identity, rather than decorative purposes. There's a minimal use of real-world imagery, prioritizing UI and conceptual diagrams.

## Dos & Donts

### Do

- Prioritize the TT Hoves Light font (weights 300, 400) for all major headlines (48px and above) to maintain the signature 'whisper-weight' aesthetic.
- Use a color palette anchored in Carbon (#0e0f11) for backgrounds and Platinum (#ffffff) for primary text to ensure consistent dark-mode visuals.
- Apply Spectral Violet (#b6abff) as the primary accent for all interactive elements and high-priority CTAs.
- Employ Electric Lime (#eef35f) specifically for highlighting key words within headlines or for secondary feature spotlighting to create focused impact.
- Round all primary buttons to 50px radius and information tags to 20px radius to maintain a distinctive soft yet technical feel.
- Utilize a baseline of 4px for all spacing units, scaling up in multiples to ensure a comfortable but structured density.
- Implement the subtle inset shadow rgba(255, 255, 255, 0.1) 0px 2.5px 0px -2px for buttons to provide depth without heavy drop shadows.

### Don't

- Avoid using heavy font weights (e.g., 600+) for headlines; the system's identity relies on the lightness of TT Hoves Light.
- Do not introduce new primary accent colors outside of Spectral Violet (#b6abff) and Electric Lime (#eef35f) to prevent diluting brand recognition.
- Refrain from using hard, sharp corners unless explicitly part of a visual component (e.g., specific data visualizations), defaulting to 8px or 12px where appropriate.
- Do not deviate from the established 'Carbon' (#0e0f11) to 'Basalt' (#141617) to 'Obsidian' (#1d2023) dark surface progression for background depth.
- Avoid generic blue for links or interactive elements; all interactive text and borders should default to Spectral Violet (#b6abff) or Platinum (#ffffff).
- Do not use box-shadows that create strong, external elevation; prefer subtle inset shadows or shifts in background color for layering.
- Do not mix 'sans-serif' fonts with 'TT Hoves' for primary content; stick to the specified font system to maintain visual coherence.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #ffffff
- Background: #0e0f11
- CTA: #b6abff
- Border: #a1a1aa
- Accent: #eef35f

### 3-5 Example Component Prompts
1. Create a hero section: background #0e0f11. Left side has a headline 'Security, Performance, Simplicity. Pick Three.' using TT Hoves Light weight 300, size 68px, #eef35f, letter-spacing -1.224px. Below is body text 'Identity-based access for users...' using TT Hoves Regular, weight 400, size 16px, #a1a1aa, line-height 1.7. Below are two buttons: 'Try Twingate' (#b6abff background, #ffffff text, 50px radius, 0px 14px padding) and 'Request a Demo' (transparent background, #b6abff text, 50px radius, 0px 14px padding, border #b6abff).
2. Generate a feature card: background #141617, 12px border radius. Title 'Zero Trust Network Access' using TT Hoves Medium, weight 500, size 22px, #ffffff. Body text 'Remote access built...' using TT Hoves Regular, weight 400, size 16px, #a1a1aa.
3. Design a header navigation bar: background solid #0e0f11. Logo 'Twingate' is #ffffff. Nav links 'Product', 'Docs', 'Customers' use TT Hoves Regular, weight 400, size 14px, #ffffff. Right side has a 'Request Demo' button (transparent background, #b6abff text, 50px radius, 0px 14px padding, border #b6abff) and 'Try for Free' button (#b6abff background, #ffffff text, 50px radius, 0px 14px padding).
4. Create a small informational badge: background #1d2023, 20px border-radius. Text 'New Twingate MSP Portal' using TT Hoves Medium, weight 500, size 13px, #ffffff. Padding 4px vertical, 14px horizontal.

---
_Source: https://styles.refero.design/style/0acef011-07da-4416-b874-ccdd675140f6_
