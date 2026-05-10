# Apollo — Design Reference

> Yellow-green spotlight on warm concrete.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.apollo.io](https://www.apollo.io) |
| Refero page | [https://styles.refero.design/style/3963a727-8dec-4308-80c0-3ae198d15b87](https://styles.refero.design/style/3963a727-8dec-4308-80c0-3ae198d15b87) |
| Theme | light |
| Industry | saas |

## Overview

Apollo presents a refined, almost austere, sales platform aesthetic. It combines a warm off-white canvas with stark black typography and a single, vibrant yellow-green accent that acts as a beacon for primary actions. Surfaces are clean and unblemished, focusing user attention on content and functionality, complemented by subtle, tactile border treatments rather than heavy shadows. The overall impression is one of grounded efficiency and direct communication.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas | `#f7f5f2` | neutral | Primary page background, elevated neutral surfaces, ghost button backgrounds |
| Subtle Gray | `#e5e7eb` | neutral | Divider lines, subtle borders for cards and inputs, background for subtle UI elements |
| Ash Gray | `#ccc9c6` | neutral | Section backgrounds, nav bar background, footer background — a distinct, slightly darker neutral surface |
| Midnight Ink | `#000000` | neutral | Primary text, main headings, default button text, strong borders |
| Graphite | `#1a1a1a` | neutral | Input text, certain body text, slightly softer than Midnight Ink for secondary textual emphasis |
| Charcoal Text | `#47423d` | neutral | Body text for slightly muted or less prominent information, decorative icon fills |
| Faded Stone | `#736f6c` | neutral | Subtle body text for small print or less important information, ghost button text |
| Soft Stone | `#94918e` | neutral | Hairline borders, very subtle dividers |
| Crisp White | `#ffffff` | neutral | Card backgrounds, selected UI elements, active text against dark backgrounds |
| Apollo Gold | `#ebf212` | brand | Primary action buttons, prominent interactive elements. This vivid yellow-green is the system's singular accent |
| Accent Green | `#f8ff2c` | brand | Decorative fills and backgrounds, secondary UI highlights — a slightly brighter shade of the primary accent |
| Violet Headline | `#3f3653` | accent | Specific number-based headlines, providing a unique, muted visual texture |

## Typography

### Season Mix

| Key | Value |
| --- | --- |
| weight | 550 |
| sizes | 48px, 56px, 64px, 72px, 88px |
| lineHeight | 1.00, 1.05, 1.10 |
| letterSpacing | -0.0100em |
| substitute | Montserrat |
| role | Display and marketing headlines – a semi-bold custom font with tight tracking gives presence without shouting. |

### Soehne

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 18px |
| lineHeight | 1.00, 1.50 |
| letterSpacing | 0.0090em |
| substitute | Inter |
| role | Primary body text, input fields, navigation links, and button labels – a precise, readable sans-serif with subtle positive tracking. |

### Abc Diatype

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 12px, 14px, 16px, 20px, 24px |
| lineHeight | 1.20, 1.30 |
| letterSpacing | -0.0100em |
| substitute | Roboto Condensed |
| role | Secondary body text, smaller labels, and bolded elements – a compact sans-serif with tight negative tracking, complementing Soehne with a more condensed feel. |

### Founders Grotesk Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 14px |
| lineHeight | 1.20 |
| letterSpacing | 0.0120em, 0.0140em |
| substitute | IBM Plex Mono |
| role | Specific code-like or data-related text – provides a monospaced contrast and reinforces precision. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 14.4 | 0.168 |
| body-sm | 14 |  | 16.8 | 0.196 |
| body | 16 |  | 24 | 0.144 |
| body-lg | 18 |  | 18 | 0.162 |
| subheading | 20 |  | 24 | -0.2 |
| heading | 24 |  | 28.8 | -0.24 |
| heading-lg | 48 |  | 50.4 | -0.48 |
| display | 88 |  | 92.4 | -0.88 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| image | 8px |
| inputs | 0px |
| buttons | 8px |
| default | 8px |
| smallCard | 12px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#f7f5f2` | 1 | Base page background and elevated neutral elements. |
| Primary Card Surface | `#ffffff` | 2 | Default card backgrounds, creating a subtle lift from the canvas. |
| Section Background | `#ccc9c6` | 3 | Distinct background for major content sections, navigation, and footer, providing clear visual segmentation. |

## Components

### Navigation Link

**Role:** Primary navigation links in header.

Text in Soehne, weight 400, size 16px, line height 1.00, charcoal text (#47423d). No background, no border.

### Primary Action Button

**Role:** Main call-to-action button.

Solid Apollo Gold (#ebf212) background, Midnight Ink (#000000) text (color #000000), 8px border-radius, 16px horizontal and optional 0px vertical padding. Soehne font at 16px weight 400.

### Secondary Action Button

**Role:** Outline style button for less prominent actions.

Transparent background, Midnight Ink (#000000) border and text (color #000000), 8px border-radius, 16px horizontal padding. Soehne font at 16px weight 400.

### Ghost Button

**Role:** Minimalist buttons for secondary or tertiary actions.

Transparent background, Faded Stone (#736f6c) text, no border. Border-radius 8px. Soehne font at 16px weight 400.

### Social Sign-Up Button

**Role:** Buttons for third-party authentication.

Canvas (#f7f5f2) background, Midnight Ink (#000000) text, Subtle Gray (#e5e7eb) border, 8px border-radius, with 24px horizontal and 32px vertical padding. Soehne font at 16px weight 400.

### Default Card

**Role:** Content presentation boxes.

Crisp White (#ffffff) background, no visible shadow, 8px border-radius, 24px padding.

### Elevated Card

**Role:** Accentuated content presentation boxes, often for feature lists.

Canvas (#f7f5f2) background, no visible shadow, 8px border-radius, 40px padding.

### Callout Card

**Role:** Special content blocks, often for testimonials or quotes.

Ash Gray (#ccc9c6) background, no visible shadow, 12px border-radius, with 0px padding, allowing for custom internal spacing.

### Text Input Field

**Role:** Standard user input fields.

Transparent background, Subtle Gray (#e5e7eb) bottom border, 0px border-radius. Graphite (#1a1a1a) text (color #1a1a1a) in Soehne font. 12px horizontal padding.

## Layout

The page primarily uses a max-width centered container, but starts with a full-width header. The hero section features a centered headline over a background that transitions from the main canvas to a slightly darker Ash Gray, followed by a form with social sign-in options. Content sections alternate between the Canvas (#f7f5f2) and Ash Gray (#ccc9c6) backgrounds, creating a clear vertical rhythm. Content within these sections often appears in centered stacks or simple two-column text-left/image-right (or vice-versa) layouts. Navigation is a sticky top bar.

## Imagery

The site predominantly uses abstract, geometric illustration elements, often integrated as background texture or subtle decorative accents. Icons are monochrome, simple, and outlined, maintaining a lightweight feel. Product visuals are minimal, appearing as contained UI screenshots or simple illustrations when applicable. Photography is almost non-existent. The overall density of imagery is low, with visuals serving to punctuate textual content rather than dominate the layout.

## Dos & Donts

### Do

- Use Ash Gray (#ccc9c6) for section backgrounds and navigation bars to provide discrete visual breaks.
- Apply Apollo Gold (#ebf212) exclusively to primary calls to action to maintain its impact and direct user focus.
- Set primary headings in Season Mix, weight 550, with tight letter spacing of -0.0100em for a powerful yet composed presence.
- Divide content using Subtle Gray (#e5e7eb) hairline borders or sections backgrounds rather than pronounced shadows or thick lines.
- Employ Soehne font for all body text and interactive elements, ensuring legibility with its specified letter spacing of 0.0090em.
- Frame all interactive elements and most content cards with an 8px border-radius for a consistent, soft edge.
- Utilize Crisp White (#ffffff) for card backgrounds against the off-white canvas to create subtle layering and separation.

### Don't

- Avoid arbitrary use of Accent Green (#f8ff2c); reserve it for decorative purposes or very specific highlights, not primary actions.
- Do not introduce additional bold or semibold weights for Soehne; its 400 weight is sufficient for the system's intended clarity.
- Refrain from using hard shadows; elevation should be achieved through background color shifts and subtle borders.
- Do not deviate from the established typography's letter spacing; the specific tracking is core to the brand's visual tone.
- Avoid full-bleed sections that extend edge-to-edge if they contain primary content; content should be comfortably within a defined width.
- Do not use multiple accent colors; Apollo Gold (#ebf212) should be the single vibrant highlight.
- Do not apply padding indiscriminately; follow the structured spacing tokens: 8px for minimal element gaps, 24px for card padding, and 40px for section gaps.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #000000
- background: #f7f5f2
- border: #e5e7eb
- accent: #ebf212
- primary action: #ebf212 (filled action)

Example Component Prompts:
- Create a primary action button: Apollo Gold (#ebf212) background, Midnight Ink (#000000) text (color #000000), 8px border-radius, 16px horizontal padding. Soehne font at 16px weight 400.
- Generate a standard content card: Crisp White (#ffffff) background, 8px border-radius, 24px padding. Use Graphite (#1a1a1a) for body text and Abc Diatype (700) for subheadings.
- Design a text input field: Transparent background, Subtle Gray (#e5e7eb) bottom border (1px), 0px border-radius. Graphite (#1a1a1a) text (color #1a1a1a) in Soehne font, 12px horizontal padding.
- Build a secondary outline button: Transparent background, Midnight Ink (#000000) border and text, 8px border-radius, 16px horizontal padding. Soehne font at 16px weight 400.

---
_Source: https://styles.refero.design/style/3963a727-8dec-4308-80c0-3ae198d15b87_
