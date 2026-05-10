# Handsome Frank — Design Reference

> Artistic Jungle Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.handsomefrank.com](https://www.handsomefrank.com) |
| Refero page | [https://styles.refero.design/style/19d4103a-9f4a-49f0-ad7d-af6588bab904](https://styles.refero.design/style/19d4103a-9f4a-49f0-ad7d-af6588bab904) |
| Theme | light |
| Industry | design |

## Overview

Handsome Frank's visual system evokes a vibrant, artistic jungle, leveraging bold colorful blocks against clean, spacious backgrounds. Typography is a confident mix of a bespoke serif for headlines, featuring tight letter-spacing for impact, and a versatile sans-serif for body text. The overall feel is one of creative energy and precision, using color as an active, functional element rather than mere decoration. Layouts are structured but dynamic, allowing hero illustrations to breathe color into the composition.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#160572` | brand | Primary brand color, secondary text against light backgrounds, interactive navigation elements, and decorative strokes in hero sections. Also defines subtle bounding boxes around content |
| Fiery Red | `#ea0706` | brand | Accent for headings and other decorative elements. Provides a warm, energetic contrast |
| Warm Ginger | `#e29675` | accent | Decorative accent for headings, links, and icons, adding a subtle, earthy vibrancy |
| Terracotta Orange | `#d64e2e` | accent | Decorative accent for headings, links, and icons. A bolder, more saturated version of Warm Ginger |
| Crimson Edge | `#df1a19` | accent | Accent for headings, a slightly deeper red than Fiery Red |
| Aqua Glow | `#24e3dc` | accent | Decorative accent for links and other interactive elements, providing a cool, modern highlight |
| Sunny Marigold | `#f9e44d` | accent | Decorative accent for headings, links, and icons, bringing cheerful energy |
| Warm Canvas Yellow | `#fffac2` | accent | Background color for headings where a subtle, muted yellow tint is desired |
| Illustrator Blue | `#2544a0` | accent | Background for primary call-to-action buttons, signaling a direct interaction point |
| Pumpkin Swirl | `#ff7701` | accent | Background for specific call-to-action buttons, adding variety to calls to action |
| Violet Berry | `#4b0f4d` | accent | Background for specific call-to-action buttons, offering a dramatic, deep hue |
| Rose Bloom | `#d98199` | accent | Background for specific call-to-action buttons, a soft yet distinct accent |
| Emerald Green | `#24e34c` | accent | Decorative accent for links, similar to Aqua Glow but in a green tone |
| Melon Hue | `#eea883` | accent | Decorative accent for links, icons, and headings, a softer orange that complements Warm Ginger |
| Absolute Black | `#000000` | neutral | Primary text color, link color, hero background, and general surface borders |
| Fog Canvas | `#f2ebe6` | neutral | Light background for sections, offering a subtle off-white contrast to pure white |
| Pure White | `#ffffff` | neutral | Default page background, heading text on dark backgrounds, icon fill, input and button text |
| Sky Haze | `#eef4fb` | neutral | Background for headings, providing a very light, cool-toned contrast |
| Cream Canvas | `#fef9ee` | neutral | Background for headings and sections, a subtle off-white with a warm undertone |
| Midtone Gray | `#909090` | neutral | Muted text or badge background for secondary information like copyrights |
| Dark Wolf | `#2c2c2c` | neutral | Secondary text color, slightly softer than absolute black |
| Ash Gray | `#636160` | neutral | Background for specific neutral ghost buttons |
| Warning Yellow | `#ffff00` | semantic | Semantic color for warning badges |

## Typography

### Arial

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 13px |
| lineHeight | 1.2 |
| role | Arial — detected in extracted data but not described by AI |

### Millik

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 20px, 22px, 32px, 38px, 42px, 54px, 70px, 80px, 88px |
| lineHeight | 0.95, 0.96, 0.98, 1.00, 1.10, 1.36 |
| letterSpacing | -0.05, -0.042, -0.042, -0.023, -0.021, -0.018, 0.038, 0.02, 0.02 |
| substitute | Playfair Display |
| role | Primary display and heading typeface, characterized by its bespoke serif forms. It sets a distinctive, artistic tone, utilizing tight negative letter-spacing for large sizes to create a dense, impactful visual block. For smaller headings, letter spacing becomes positive for clarity. |

### Klarheit Grotesk

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| weights | 400, 600, 700 |
| sizes | 12px, 14px, 16px, 22px, 24px |
| lineHeight | 1.20, 1.28, 1.36 |
| letterSpacing | normal |
| substitute | Inter |
| role | Versatile sans-serif for body text, navigation items, badges, and smaller headings. Provides clarity and readability. The semibold weight at 22px is used for prominent subheadings. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 |  |
| body-sm | 14 |  | 1.2 |  |
| body | 16 |  | 1.2 |  |
| subheading | 22 |  | 1.28 |  |
| heading-sm | 24 |  | 1.36 |  |
| heading | 32 |  | 1 | 1.216 |
| heading-lg | 42 |  | 0.98 | -0.882 |
| display | 88 |  | 0.95 | -4.4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| inputs | 30px |
| buttons | 30px |
| bodyElements | 10px |
| circularElements | 50% |

- **elementGap** — 20px
- **sectionGap** — 24px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Pure White Canvas | `#ffffff` | 0 | Default page background, providing a clean base for most content. |
| Cream Canvas | `#fef9ee` | 1 | Slightly warmer off-white background for secondary sections, offering subtle visual separation. |
| Fog Canvas | `#f2ebe6` | 2 | Cool-toned off-white background for content blocks, providing an alternative subtle contrast. |
| Accent Surface | `#ffff00` | 3 | Vivid yellow surface used for warning badges or highlight elements. |

## Components

### Ghost Button

**Role:** Interactive elements that blend into the background, often for discrete actions or navigation.

Background: rgba(0,0,0,0), Color: rgb(0,0,0), Border: none, Radius: 0px, Padding: 0px.

### Circular Ghost Button

**Role:** Small, discreet interactive elements, often used for icon-only actions.

Background: rgba(0,0,0,0), Color: rgb(0,0,0), Border: none, Radius: 50%, Padding: 0px.

### Pill Accent Button

**Role:** Prominent calls to action with rounded edges, standing out against darker backgrounds.

Background: rgba(0, 0, 0, 0.6), Text: Pure White (#ffffff), Border: 1px solid Pure White (#ffffff), Radius: 30px, Padding: 16px 32px 15px 32px.

### Illustrator Block Button

**Role:** Primary action button for navigating to illustrator portfolios, using specific brand colors.

Background: Illustrator Blue (#2544a0) or other accent colors (Pumpkin Swirl #ff7701, Violet Berry #4b0f4d, Rose Bloom #d98199), Text: Absolute Black (#000000), Border: none, Radius: 0px, Padding: 24px 24px 38px 24px.

### Illustration Card

**Role:** Container for showcasing artist work, designed to let the artwork be the focus.

Background: rgba(0, 0, 0, 0), Radius: 0px, Shadow: none, Padding: 0px.

### Rounded Input Field

**Role:** Text input areas with softened corners.

Background: rgba(0, 0, 0, 0.1), Text: Pure White (#ffffff), Border: none, Radius: 30px, Padding: 16px 32px 15px 32px.

### Warning Dot Badge

**Role:** Small, circular indicator for alerts or status.

Background: Warning Yellow (#ffff00), Text: Absolute Black (#000000), Radius: 50%, Padding: 0px.

### Copyright Notice Badge

**Role:** Muted text for legal information.

Background: rgba(0, 0, 0, 0), Text: Midtone Gray (#909090), Radius: 0px, Padding: 0px.

## Layout

The page primarily uses a contained layout with no explicit pageMaxWidth, allowing sections to adapt while text content often aligns within a comfortable reading width. The hero section is a full-bleed, dynamic illustration with centered headline text overlaid. Subsequent sections toggle between clean backgrounds (Fog Canvas, Cream Canvas) to highlight grids of illustration cards. Content arrangement for feature sections includes image-left/text-right or vise-versa implied by the card layouts. A prominent 4-column card grid is used for showcasing illustrators. Vertical rhythm is maintained by consistent section spacing, making the flow comfortable and unhurried. Navigation is a sticky top bar with a hidden menu triggered by a hamburger icon.

## Imagery

The site primarily features vibrant, detailed illustrations as its visual language. These are often full-bleed or large-scale, acting as hero backgrounds or framed within content blocks. The illustrations are diverse in style but consistently rich in color and detail, serving as product showcases and setting an artistic atmosphere. Icons, if present, are minimal and secondary to the main illustrative content. The density is image-heavy, with illustrations taking up significant visual space to emphasize the agency's offerings.

## Dos & Donts

### Do

- Use Millik for all primary headings, applying tight negative letter-spacing for visual impact at larger sizes and subtle positive spacing for smaller headings to enhance clarity.
- Utilize Klarheit Grotesk for all body text, navigation elements, and secondary information, prioritizing its legibility and modern feel.
- Accentuate interactive elements and key information with the brand's vibrant accent colors (Fiery Red, Illustrator Blue, Aqua Glow), ensuring they stand out against neutral backgrounds.
- Maintain a comfortable density with consistent elementGap of 20px and sectionGap of 24px to provide breathing room around content.
- Apply a 30px border-radius to all input fields and primary interactive buttons for a consistent soft, approachable shape.
- Employ Absolute Black (#000000) for primary text on light backgrounds and Pure White (#ffffff) for text on dark backgrounds to ensure high contrast.
- Structure pages with Fog Canvas (#f2ebe6) or Cream Canvas (#fef9ee) as base backgrounds for sections, providing soft transitions.

### Don't

- Avoid using generic blue for links; instead, use brand accent colors like Aqua Glow (#24e3dc) or Emerald Green (#24e34c) when a chromatic link is needed.
- Do not introduce new border radii values beyond 0px, 10px, 30px, and 50% to maintain shape consistency.
- Refrain from adding box shadows to elements unless explicitly defined for a specific component, as the system largely relies on flat surfaces and color contrast.
- Do not use Absolute Black (#000000) as a solid background color for extended sections unless it is the hero with an illustrative overlay.
- Avoid arbitrary changes in letter-spacing for Klarheit Grotesk; it should remain 'normal' unless a specific stylistic reason dictates otherwise.
- Do not use generic button styles; always choose from the defined button variants to ensure consistency in interaction and appearance.
- Do not introduce new typefaces; the system relies exclusively on Millik and Klarheit Grotesk for brand coherence.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #160572
accent: #160572
primary action: #ff7701 (filled action)

Example Component Prompts:
1. Create a Hero Headline: 'Home to the world’s greatest illustrators.' using Millik regular, size 88px, line-height 0.95, letter-spacing -4.4px, color Pure White (#ffffff), centered on a background of Absolute Black (#000000).
2. Create an Illustrator Block Button: 'Browse Illustrators.' using Illustrator Blue (#2544a0) as background, Absolute Black (#000000) text, Klarheit Grotesk regular, size 24px, no border, 0px radius, padding 24px 24px 38px 24px.
3. Create a Pill Accent Button: 'Subscribe Now' using rgba(0, 0, 0, 0.6) background, Pure White (#ffffff) text, 1px solid Pure White (#ffffff) border, 30px radius, padding 16px 32px 15px 32px.

---
_Source: https://styles.refero.design/style/19d4103a-9f4a-49f0-ad7d-af6588bab904_
