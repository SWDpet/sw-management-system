# cthdrl — Design Reference

> Black canvas, stark typography

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://cthdrl.co](https://cthdrl.co) |
| Refero page | [https://styles.refero.design/style/565bfc50-3a19-4224-9a4c-125edaeb7bef](https://styles.refero.design/style/565bfc50-3a19-4224-9a4c-125edaeb7bef) |
| Theme | dark |
| Industry | design |

## Overview

Cthdrl presents a stark, high-contrast dark mode aesthetic, leveraging deep black backgrounds for dramatic visual impact. Typography is central, utilizing two bespoke sans-serif fonts in varied sizes to establish a strong hierarchical rhythm and distinctive brand voice. Interactive elements are minimalist, appearing as ghost controls or text links with a subtle off-white highlight, maintaining the overall severe and sophisticated atmosphere. The visual system emphasizes spaciousness, relying on precise typographic arrangement and discreet dividing lines rather than heavy components or decorative flourishes.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Void | `#000000` | neutral | Page backgrounds, card surfaces, all primary backgrounds. Creates a high-contrast foundation for content |
| Ghost Sand | `#e7ded1` | neutral | Primary text color, link text, navigation text, ghost button borders, and decorative accents. Provides stark contrast against the dark background |

## Typography

### NB Akademie

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 32px, 35px, 121px |
| lineHeight | 0.85, 1.00, 1.20 |
| letterSpacing | -0.016, -0.01 |
| substitute | Montserrat |
| role | Headlines and prominent display text. Its wide character forms at large sizes define the brand's assertive yet clean typographic voice, with tight tracking enhancing its impact. |

### NB Akademie Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 11px, 32px |
| lineHeight | 1.00, 1.20 |
| letterSpacing | -0.045 |
| substitute | Space Mono |
| role | Body text, navigation, links, and various informational elements. Its monospace nature adds a technical, precise feel, with negative letter-spacing for a compact appearance. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| heading-sm | 32 |  | 1.2 | -0.512 |
| heading | 35 |  | 1 | -0.56 |
| display | 121 |  | 0.85 | -1.936 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 0px |

- **elementGap** — 10px
- **sectionGap** — 26px
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Ghost Navigation Link

**Role:** Top navigation and inline links

Uses Ghost Sand text on Midnight Void background, with 10px right padding for spacing between items. No border or radius, relying on text color for interaction states.

### Ghost Primary Button

**Role:** Call to action buttons

Text in Ghost Sand, with a 1px solid Ghost Sand border. Background is Midnight Void. No border radius or padding, creating a minimal, text-focused interactive element. The border is a subtle underline.

## Layout

The page model is full-bleed, with a centered content approach that does not impose a strict max-width but rather uses horizontal white space as part of its design. The hero section is characterized by a full-bleed dark background with a large, centered headline that dominates the viewport. The section rhythm is primarily seamless, relying on typographic shifts and subtle dividing lines rather than alternating background colors. Content is often presented in a single column stack, emphasizing large headlines with smaller, supporting text blocks. Navigation is a minimal top bar, with elements precisely spaced and aligned.

## Imagery

The site uses minimal imagery, primarily relying on abstract, subtle line work and geometric arcs that weave across the dark background as decorative elements. Iconography, when present in the header, is monochromatic (Ghost Sand outline on Midnight Void fill), featuring a delicate stroke weight. The visual language is text-dominant, with graphics serving as atmospheric accents rather than prominent content showcases, contributing to a disciplined and structural feel.

## Dos & Donts

### Do

- Prioritize Midnight Void (#000000) for all background surfaces to maintain high contrast and the dark theme.
- Use Ghost Sand (#e7ded1) as the primary color for all text, links, and interactive element outlines.
- Employ NB Akademie for large, impactful headlines, ensuring letter-spacing values (-0.0160em to -0.0100em) are applied for proper visual density.
- Reserve NB Akademie Mono for body text, navigation, and detailed information, using its distinctive negative letter-spacing (-0.0450em) to create a compact, structured feel.
- Maintain a spacious density in layouts, utilizing 26px for section gaps and 10px for element gaps to provide ample breathing room between content blocks.
- Implement Ghost Primary Buttons using Ghost Sand (#e7ded1) text and a 1px solid Ghost Sand border on a Midnight Void background, with no padding or radius.
- Utilize 1px Ghost Sand (#e7ded1) lines for subtle visual dividers, such as those seen in the header and alongside body text blocks.

### Don't

- Avoid using saturated or vivid colors for primary UI elements; color is reserved for decorative gradients or specific content within page sections.
- Do not introduce heavy shadows or elevation; the design relies on flat surfaces and high contrast for visual hierarchy.
- Refrain from using rounded corners on any UI elements; all corners should be sharp and angular (0px radius).
- Do not use generic system fonts; NB Akademie and NB Akademie Mono are essential to the brand's typographic identity.
- Avoid dense, information-heavy blocks of text without sufficient line breaks or spacing; maintain the spacious and minimalist aesthetic.
- Do not use filled buttons for primary calls to action; ghost buttons with a border are characteristic of this design system.
- Minimize the use of multiple font weights; the system primarily uses weight 400 for both typefaces.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
text: #e7ded1
background: #000000
border: #e7ded1
accent: none observed
primary action: no distinct CTA color

**3-5 Example Component Prompts**
1. Create a primary headline with supporting text: Headline 'Future Architectures' at 121px NB Akademie weight 400, #e7ded1, letter-spacing -1.936px. Subtext 'Building the next era' at 11px NB Akademie Mono weight 400, #e7ded1, letter-spacing -0.495px.
2. Create a ghost navigation link: Text 'WORK' at 11px NB Akademie Mono weight 400, #e7ded1, letter-spacing -0.495px, with 10px right padding.
3. Design a descriptive text block: 'We are a collection of designers, strategists, and creative technologists architecting the future.' at 11px NB Akademie Mono weight 400, #e7ded1, letter-spacing -0.495px.
4. Create a ghost button: Text 'DISCOVER' at 32px NB Akademie weight 400, #e7ded1, letter-spacing -0.512px. Add a 1px solid #e7ded1 border.

---
_Source: https://styles.refero.design/style/565bfc50-3a19-4224-9a4c-125edaeb7bef_
