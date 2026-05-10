# Limitless — Design Reference

> Architectural blueprint on white marble.  The visual identity relies on precise lines, muted tones, and selective accents to convey controlled innovation.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://limitless.ai](https://limitless.ai) |
| Refero page | [https://styles.refero.design/style/626ae2de-c402-4805-b859-2c6adca41022](https://styles.refero.design/style/626ae2de-c402-4805-b859-2c6adca41022) |
| Theme | light |
| Industry | devtools |

## Overview

This design system presents a serious and understated aesthetic, hinting at a modern, high-tech product without resorting to overt flashiness. A constrained palette of near-gray shades dominates, creating a subdued, focused environment. The choice of a custom sans-serif typeface with fine weight variations, especially at display sizes, allows headlines to communicate authority through subtle presence rather than bold declaration. The single, vivid violet accent is sparingly applied, acting as a precise indicator in an otherwise achromatic landscape.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Page Graphite | `#0f172a` | neutral | Primary text, main headings, and significant icons – anchoring the visual hierarchy with a deep, almost black tone. |
| Body Slate | `#475569` | neutral | Body copy, navigation links, and less prominent text – providing high readability with softened contrast against light backgrounds. |
| Subtle Gray | `#334155` | neutral | Secondary headings and supporting text elements, offering a slightly lighter alternative to Page Graphite while maintaining visual weight. |
| Link Ash | `#64748b` | neutral | Standard link color that recedes into the background palette, emphasizing content over interactive elements until hovered. |
| Violet Signal | `#8a53e1` | brand | Brand accent, used deliberately for the brand logo and specific interactive iconography, to signify brand presence and key actions. |
| Porcelain White | `#e5e7eb` | neutral | Primary background for pages and default borders, creating a clean, expansive canvas. |
| Snowdrift | `#f2f3f5` | neutral | Subtle background for UI elements, slightly off-white to provide minimal distinction from the main page background. |
| Divider Silver | `#d1d5db` | neutral | Divider lines and subtle borders, providing clear separation without harshness. |

## Typography

### Greycliff

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 30px, 36px, 60px |
| lineHeight | 1.00, 1.11, 1.20, 1.43, 1.50, 1.56, 1.63 |
| letterSpacing | -0.025 |
| substitute | Inter |
| role | All textual content on the site, from headings to body text and links. The -0.025em letter-spacing throughout creates a subtle tightness that feels precise and modern. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.56 | -0.025 |
| body | 16 |  | 1.5 | -0.025 |
| body-lg | 18 |  | 1.5 | -0.025 |
| subheading | 30 |  | 1.43 | -0.025 |
| heading | 36 |  | 1.2 | -0.025 |
| display | 60 |  | 1.11 | -0.025 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| buttons | 9999px |
| default | 8px |

- **elementGap** — 8px
- **sectionGap** — 48-64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Announcement Hero Heading

### CEO Message Content Card

### Navigation Link Group + Sign In Button

### Navigation Link

**Role:** Primary navigation items in the header.

Text uses Body Slate (#475569) in Greycliff, weight 400 at 16px, with a line height of 1.5. No background, transparent borders. Slightly desaturated text color suggests understated navigation.

### Pill Ghost Button

**Role:** Subtle, interactive elements and secondary actions.

Transparent background (rgba(0,0,0,0)), text color Body Slate (#475569). Border radius 9999px. Padding is 8px top/bottom, 16px left/right. The presence of a borderTopColor (rgb(229, 231, 235)) in the data suggests a subtle, almost invisible border that might be activated on hover or specific states, or is an artifact.

### Section Heading

**Role:** Titles for major content blocks.

Greycliff weight 600, 36px size, line height 1.20, -0.025em letter spacing. Color Subtle Gray (#334155).

### Body Paragraph

**Role:** General informational text.

Greycliff weight 400, 18px size, line height 1.5, -0.025em letter spacing. Color Body Slate (#475569), providing a comfortable reading experience on light backgrounds.

### Card Container

**Role:** Groups related content or interactive elements.

Background is not explicitly defined but is assumed to be Snowdrift (#f2f3f5) or Porcelain White (#e5e7eb) based on the image. Border radius 16px. Features a subtle, deep shadow: rgba(30, 41, 59, 0.15) 0px 25px 50px -12px, giving it a distinctive 'hovering' presence.

## Layout

The layout is primarily centered and contained within a content width that isn't explicitly maxed, allowing for flexible responsiveness. The hero section features a prominent, centered headline over a white background. Sections are delineated by clear vertical spacing, typically 48px to 64px, creating comfortable breathing room. Content within sections is often stacked centrally, as seen with the main message, favoring directness. Navigation is a simple top bar, with links aligning to the right of the brand logo, maintaining a clean and traditional structure.

## Imagery

The site predominantly uses abstract or placeholder images, shown as gray blocks with a subtle error icon. This suggests a content-focused UI with minimal decorative imagery. The brand logo itself is a simple, abstract icon in violet, embodying the precision and technical focus of the brand. There is an absence of photography or complex illustrations, pointing to a 'no-fuss' approach, relying on typography and layout to convey information.

## Dos & Donts

### Do

- Prioritize Greycliff (Inter) for all text elements to maintain a consistent, modern typographic voice.
- Use Page Graphite (#0f172a) for primary text and headings, grounding the content with a strong, deep tone.
- Apply Body Slate (#475569) for all body copy and default links, ensuring comfortable readability and a subdued feel.
- Employ a border-radius of 9999px for all interactive buttons to establish a consistent 'pill' shape.
- Maintain a tight letter spacing of -0.025em for all text, contributing to the refined and precise aesthetic.
- Use Porcelain White (#e5e7eb) as the primary page background color for a clean and expansive canvas.
- Where elevation is needed, use the distinct shadow rgba(30, 41, 59, 0.15) 0px 25px 50px -12px, creating a subtle lift.

### Don't

- Do not introduce highly saturated or bright colors beyond the Violet Signal (#8a53e1) accent, to preserve the restrained palette.
- Avoid using generic button shapes; all buttons should have a 9999px border-radius.
- Do not deviate from the Greycliff (Inter) typeface for any textual content.
- Avoid heavy drop shadows or glows; the elevation should be subtle, created with rgba(30, 41, 59, 0.15) 0px 25px 50px -12px.
- Do not use letter spacing greater than 0; the design relies on a slightly tighter -0.025em for its specific aesthetic.
- Do not use decorative borders or backgrounds that detract from the clean, achromatic base.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
- Text: #0f172a 
- Body Text: #475569 
- Background: #e5e7eb 
- Accent: #8a53e1 
- Border: #e5e7eb

Example Component Prompts:
- Create a primary page headline: Text 'Limitless Intelligence' using Greycliff, weight 600, 60px size, line height 1.11, letter spacing -0.025, color #0f172a. Center align.
- Create a pill-shaped ghost button: Text 'Sign In' using Greycliff, weight 400, 16px size, line height 1.5, letter spacing -0.025, color #475569. Background rgba(0,0,0,0), border radius 9999px, padding 8px vertical, 16px horizontal.
- Create a content card: Background #e5e7eb, border-radius 16px. Apply shadow rgba(30, 41, 59, 0.15) 0px 25px 50px -12px. Inside, include a heading 'Feature Highlight' (Greycliff weight 600, 36px, #334155) and a body paragraph 'This is a key feature description.' (Greycliff weight 400, 18px, #475569), with 16px padding inside.

---
_Source: https://styles.refero.design/style/626ae2de-c402-4805-b859-2c6adca41022_
