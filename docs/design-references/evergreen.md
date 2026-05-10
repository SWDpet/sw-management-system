# Evergreen — Design Reference

> Warm parchment, dark ink, verdant accent

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.evergreen.so](https://www.evergreen.so) |
| Refero page | [https://styles.refero.design/style/a4314209-7688-4750-842a-432c3918a21b](https://styles.refero.design/style/a4314209-7688-4750-842a-432c3918a21b) |
| Theme | light |
| Industry | saas |

## Overview

Evergreen's visual system evokes a natural, grounded feel with a dominant warm, off-white canvas and dark, authoritative text. The interface maintains a spacious rhythm, using broad sections of soft background colors, rather than strong dividers. Typography is confident and editorial, with a custom headline font providing gravitas, while interactions are punctuates by a subtle, verdant green.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Rich Earth | `#000000` | neutral | Primary text, headline text, action buttons — a deep, grounding black that provides strong contrast |
| Parchment | `#edede2` | neutral | Page background, main canvas |
| Cloud Whisper | `#ffffff` | neutral | Input backgrounds, button text on dark backgrounds, subtle highlights |
| Forest Fern | `#beedc0` | brand | Decorative background elements, subtle card surfaces, active states — a muted green that connects to the brand's ecological mission |
| Deep Slate | `#333333` | neutral | Secondary text, muted links, borders, icon fills — a softer alternative to Rich Earth for less prominent elements |
| Soft Vanilla | `#fffff3` | neutral | Card backgrounds, elevated container surfaces — a warm, creamy off-white |

## Typography

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.6 |
| role | Arial — detected in extracted data but not described by AI |

### ivypresto-headline

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 54px, 74px |
| lineHeight | 1.40, 1.48, 1.49 |
| substitute | Playfair Display |
| role | Main headlines and prominent display text: the weight 600 combined with generous line height creates an editorial, luxurious feel, drawing immediate attention with a classic serif presence. |

### Rubik

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 12px, 17px, 18px, 19px, 20px, 21px, 23px, 28px, 30px |
| lineHeight | 1.00, 1.41, 1.54, 1.70, 1.90 |
| substitute | Inter |
| role | Body text, subheadings, interface labels, and buttons: a highly legible sans-serif for functional elements, balancing legibility with a modern, approachable feel at various sizes. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.6 |  |
| body | 18 |  | 1.54 |  |
| subheading | 28 |  | 1 |  |
| heading | 54 |  | 1.48 |  |
| display | 74 |  | 1.49 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 10px |
| input | 7px |
| links | 30px |
| other | 46px |
| buttons | 40.5px |

- **elementGap** — 14px
- **sectionGap** — 165px
- **cardPadding** — 17px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Parchment | `#edede2` | 0 | Dominant page background, main canvas. |
| Soft Vanilla | `#fffff3` | 1 | Card backgrounds, elevated content containers. |
| Cloud Whisper | `#ffffff` | 2 | Input fields, foreground elements against Soft Vanilla. |
| Forest Fern | `#beedc0` | 3 | Circular accent cards, decorative background areas for visual interest. |
| Rich Earth | `#000000` | 4 | Top banner background. |

## Components

### Filled Primary Button

**Role:** Primary calls to action

Filled with Rich Earth (#000000) and Cloud Whisper (#ffffff) text, featuring generous 40.5px border-radius for a soft, approachable pill shape. Padding is 14.4px vertical, 26.4px horizontal.

### Navigation Link Button

**Role:** Secondary calls to action or navigation links within headers

Black text on Parchment (#edede2) background with a 30px border-radius or 10px borderRadius for rounded rectangular action. Padding is 9px vertical, 29.4px horizontal.

### Product Feature Card

**Role:** Highlighting features or testimonials effectively.

Background of Soft Vanilla (#fffff3) with a 10px border-radius. Features 17px padding on horizontal sides for content, and 72px top, 36px bottom.

### Circular Accent Card

**Role:** Decorative elements or image containers.

Forest Fern (#beedc0) background, 100% border-radius to create a perfect circle.

### Text Input Field

**Role:** Forms and data entry.

Cloud Whisper (#ffffff) background with a Rich Earth (#000000) border, 7px 0px 0px 7px border-radius, giving it a soft, slightly asymmetrical rounded left edge. Padding is 0 vertical, 18.9px horizontal.

### Top Banner Alert

**Role:** Important, non-dismissible site-wide messages.

Rich Earth (#000000) background with Cloud Whisper (#ffffff) text (Arial, 14px, 400 weight). Padding 10px vertical.

## Layout

The site uses a mostly full-bleed layout for background elements, but content is typically contained with a subtle max-width and centered alignment. The hero section features a centered headline over the Parchment (#edede2) background, complemented by circular portrait photography. Main content sections alternate between the primary Parchment background and sections with soft, decorative Forest Fern (#beedc0) accents or the richer purple of the integration sidebar. Vertical spacing between these sections is notably generous at 165px, creating a relaxed, spacious rhythm. Content often appears in single-column stacks or simple two-column layouts, with elements such as feature cards or user recognition flows, enhancing readability and minimizing visual clutter. Navigation is a standard top bar, with prominent branding and a rounded action button.

## Imagery

The site uses a combination of subtle illustrations and candid, circular portrait photography. Illustrations appear in muted, natural tones with botanical leaf motifs, often as background elements or borders. Photography consists of single circular headshots, often with a subtle border, placed as decorative accents within text or as avatar representations, rather than full-bleed hero images. Icons are minimalist, monochrome (Deep Slate #333333 or Cloud Whisper #ffffff on Rich Earth #000000) and functional. The overall visual language is light with a focus on human connection and nature.

## Dos & Donts

### Do

- Use Parchment (#edede2) as the default page background to maintain the foundational warm, inviting canvas.
- Apply Rich Earth (#000000) for all primary body and headline text to ensure strong contrast and readability.
- Utilize ivypresto-headline (Playfair Display) 600 weight for all major headings to convey an editorial and weighty presence.
- Implement 40.5px border-radius for all primary action buttons for a distinctive pill-like shape.
- Use 'Rubik' (Inter) font for all functional UI elements, body text, and button labels, at various weights from 400 to 700 to provide consistent legibility.
- Incorporate Forest Fern (#beedc0) as a subtle background for decorative cards or callout sections, leveraging its muted green to reinforce brand values.
- Maintain generous vertical spacing (165px) between main content sections to create a spacious and unhurried reading experience.

### Don't

- Avoid harsh white backgrounds; always opt for Soft Vanilla (#fffff3) or Parchment (#edede2) for surfaces and canvas.
- Do not use highly saturated or bright colors for backgrounds or large areas; maintain a muted, natural palette.
- Do not use overly strong shadows; the design relies on subtle background shifts and borders for depth.
- Avoid tight layouts or compact spacing; prioritize breathing room between elements and sections.
- Do not introduce additional font families; adhere strictly to ivypresto-headline (Playfair Display) and Rubik (Inter).
- Never use square, sharp-cornered buttons or cards; maintain the system's characteristic soft, rounded edges.
- Do not use arbitrary padding values; stick to the established system of 14.4px or 9px vertical padding for buttons, and 17px horizontal for cards.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
text: #000000
background: #edede2
border: #333333
accent: #beedc0
primary action: #000000 (filled action)

**3-5 Example Component Prompts**
1. Create a Primary Action Button: #000000 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a feature card: Soft Vanilla (#fffff3) background, 10px radius, 17px horizontal padding (72px top, 36px bottom). Headline in Rich Earth (#000000) with Rubik 28px, 600 weight. Body text in Deep Slate (#333333) with Rubik 18px, 400 weight.
3. Design a navigation bar: Parchment (#edede2) background. Left aligned 'Evergreen' logo (Deep Slate #333333). Right aligned navigation links: Deep Slate (#333333) text (Rubik, 17px, 400 weight), element gap 14px. A 'Schedule a demo' Navigation Link Button: Rich Earth (#000000) text (Rubik, 17px, 400 weight), 30px radius, 9px vertical, 29.4px horizontal padding.

---
_Source: https://styles.refero.design/style/a4314209-7688-4750-842a-432c3918a21b_
