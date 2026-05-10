# Clyde — Design Reference

> Iridescent gradients on obsidian glass

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://joinclyde.com](https://joinclyde.com) |
| Refero page | [https://styles.refero.design/style/39098e34-b911-4ad3-bcb5-ee80a392cd95](https://styles.refero.design/style/39098e34-b911-4ad3-bcb5-ee80a392cd95) |
| Theme | dark |
| Industry | saas |

## Overview

Clyde deploys a dark, atmospheric interface punctuated by subtle gradients and an emphasis on dimensional product rendering. The design system uses sparse, highly contrasted typography with a blend of classic serif and modern grotesk fonts, creating a 'luxury tech' feel. Surfaces are dark, with elements softly lit by internal shadows and occasional, vibrant iridescent gradients. The overall density is comfortable, providing generous breathing room for content.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight | `#000000` | neutral | Page background, primary text on light surfaces, icon fills, strong borders |
| Ghost Ash | `#f6f6f4` | neutral | Primary text on dark backgrounds, ghost button text, accent borders, light surface elements |
| Slate | `#7d7d7d` | neutral | Secondary background surfaces, ghost button backgrounds, muted text |
| White Smoke | `#ffffff` | neutral | Subtle inset shadows, light accents |
| Dark Charcoal | `#1a1a1a` | neutral | Subtle dark borders |
| Iridescent Glow | `#ff8400` | accent | Decorative gradient accent, hero product highlighting; Decorative gradient accent, hero product highlighting with vertical flow |

## Typography

### Recoleta

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 36px, 47px, 61px, 80px, 125px |
| lineHeight | 1.00, 1.10 |
| letterSpacing | -0.0300em at 125px, -0.0200em at 80px, -0.0100em at 36px |
| fontFeatureSettings | "clig", "liga" |
| substitute | Playfair Display |
| role | Display headlines and page titles — the large, highly tracked text creates a distinct, sophisticated visual statement. |

### Oldschool Grotesk

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 15px, 16px, 21px, 80px, 138px |
| lineHeight | 1.00, 1.30, 1.40, 1.60 |
| letterSpacing | -0.0300em at 138px, -0.0200em at 80px, 0.0100em at 15px |
| fontFeatureSettings | "clig", "liga" |
| substitute | Inter |
| role | Body text, subheadings, and UI elements — its subtle tracking and modern sans-serif form balance the Recoleta headlines, providing clear readability. |

### Times

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.20, 1.25 |
| letterSpacing | normal |
| fontFeatureSettings | "clig", "liga" |
| substitute | Times New Roman |
| role | System fallback for various UI elements where a classic serif provides visual texture. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 15 |  | 1.6 | 0.15 |
| subheading | 21 |  | 1.4 | 0.21 |
| heading | 36 |  | 1.1 | -0.36 |
| heading-lg | 47 |  | 1.1 | -0.94 |
| display | 61 |  | 1.1 | -1.22 |
| display-lg | 80 |  | 1 | -1.6 |
| display-xl | 125 |  | 1 | -3.75 |
| display-xxl | 138 |  | 1 | -4.14 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| links | 12px |
| inputs | 4px |
| buttons | 100px |
| largeElements | 38px |

- **elementGap** — 20px
- **sectionGap** — 50px
- **cardPadding** — 22px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Canvas | `#000000` | 0 | Dominant page background, providing a deep, dark atmospheric base. |
| Slate Module | `#7d7d7d` | 1 | Secondary background for cards and ghost buttons, offering a slightly lighter, distinct surface within the dark theme. |

## Components

### Ghost Header Button

**Role:** Navigation and secondary actions

Text in Ghost Ash (#f6f6f4) on a transparent background, with no border and 0px radius. Used for discreet navigation items.

### Pill Button

**Role:** Primary Call to Action

Background in Slate (#7d7d7d) with Ghost Ash (#f6f6f4) text. Has a 100px border-radius, creating a pill shape. Padding is generous at 22px all around, making it a prominent interaction point.

### Feature Card

**Role:** Content container for features or case studies

Transparent background with a subtle inset shadow (rgba(255, 255, 255, 0.25) 0px 0.636826px 3.82096px 0px inset). Rounded corners of 16px. Content padding is 10px.

### Large Radius Card

**Role:** Prominent content blocks

Similar to Feature Card but with a larger 40px border-radius and slightly different boxShadow. No internal padding specified, implying full-bleed content.

### Inline Text Link

**Role:** Linked text within content

Text color in Ghost Ash (#f6f6f4) on a transparent background, with 0px radius. No discernible padding, appears as simple underlined or distinct text.

### Announcement Bar Link

**Role:** High-visibility information link

Transparent background with text in Ghost Ash (#f6f6f4), appearing on the Midnight background. Text likely uses a smaller font size.

## Layout

The page primarily employs a full-bleed layout on a Midnight (#000000) canvas. The hero section features a centered, large Recoleta headline over an abstract 3D iridescent background. Main content sections alternate between full-width blocks and content contained within fluid sections that appear centered. A common pattern is a two-column layout with large typography on one side and a product visual or smaller text block on the other, occasionally reversing. Feature lists utilize a grid of card-like elements. Navigation is a minimalist top bar, generally un-sticky, with ghost buttons and a pill-shaped primary action.

## Imagery

The site utilizes 3D abstract graphics with an iridescent, glowing quality, serving as decorative atmosphere rather than direct content. These are often full-bleed backgrounds or large, contained elements. Product visuals are crisp and dimensional, appearing either isolated against the dark backdrop or within simulated screen interfaces, demonstrating a clear focus on showcasing objects with a realistic, high-fidelity render. Icons are minimal, outlined, and monochromatic, primarily in Ghost Ash (#f6f6f4). Imagery density is moderate; illustrative elements occupy significant visual space but are balanced with generous typography.

## Dos & Donts

### Do

- Use Midnight (#000000) for all page backgrounds to maintain atmospheric depth.
- Apply Recoleta for all primary headlines, leveraging its unique tracking values for distinct visual impact.
- Implement 100px border-radius for all primary buttons to achieve a soft, pill-shaped aesthetic.
- Employ the Iridescent Glow gradient (linear-gradient(90deg, #feed7a, #ff8400, #df91f7)) for any elements requiring unique visual emphasis or product highlights.
- Ensure Ghost Ash (#f6f6f4) is the default text color on dark backgrounds, providing maximum contrast and legibility.
- Maintain generous spacing with a base unit of 4px and elemental gaps around 20px, fostering a comfortable content density.

### Don't

- Avoid harsh, solid borders or backgrounds, except for functional elements that require clear separation.
- Do not use highly saturated colors for text except for specific accent elements; stick to Ghost Ash (#f6f6f4) and Midnight (#000000) for content.
- Do not introduce sharp corners or small radii on interactive elements; favor the 16px and 100px radii for organic, approachable shapes.
- Avoid generic system fonts for any primary heading or body text; always use Recoleta or Oldschool Grotesk.
- Do not use explicit drop shadows for elevation; rely on subtle inset shadows (rgba(255, 255, 255, 0.25) 0px 0.636826px 3.82096px 0px inset) for depth on dark surfaces.
- Do not introduce competing gradients; the Iridescent Glow is the signature gradient.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #f6f6f4
background: #000000
border: #1a1a1a
accent: #ff8400
primary action: no distinct CTA color

Example Component Prompts:
1. Create a hero section with a Midnight (#000000) background and an 'Iridescent Glow' gradient abstract visual. Headline: 'Protect the whole customer journey' using Recoleta, size 125px, weight 400, Ghost Ash (#f6f6f4), letter-spacing -3.75px. Subtext: 'Available at Amazon, eBay, Wayfair and thousands more stores.' using Oldschool Grotesk, size 16px, weight 400, Ghost Ash (#f6f6f4).
2. Design a 'Pill Button' component: background Slate (#7d7d7d), text 'Request a Demo' in Ghost Ash (#f6f6f4), using Oldschool Grotesk, size 16px, weight 400. Apply a 100px border-radius and 22px padding all around.
3. Implement a 'Feature Card': transparent background with inset shadow (rgba(255, 255, 255, 0.25) 0px 0.636826px 3.82096px 0px inset), 16px border-radius, and 10px padding. Content text: 'Hassle-free claims, instant payouts and global repairs' in Ghost Ash (#f6f6f4), Oldschool Grotesk, size 16px, weight 400.
4. Create a top navigation bar: Midnight (#000000) background. 'Product' and 'Resources' links in Ghost Ash (#f6f6f4), Oldschool Grotesk, size 16px, weight 400, with 0px border-radius and transparent background (Ghost Header Button style). Include a 'Demo' button in the 'Pill Button' style from example 2.

---
_Source: https://styles.refero.design/style/39098e34-b911-4ad3-bcb5-ee80a392cd95_
