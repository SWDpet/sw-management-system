# Orderful — Design Reference

> Precision on White Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.orderful.com](https://www.orderful.com) |
| Refero page | [https://styles.refero.design/style/d95a35c2-d3f7-49e7-9ba7-282d52d3211d](https://styles.refero.design/style/d95a35c2-d3f7-49e7-9ba7-282d52d3211d) |
| Theme | light |
| Industry | saas |

## Overview

Orderful employs a design language of precision and vibrant urgency, utilizing a bright, spacious canvas punctuated by sharp, functional typography. A single vivid orange accent color drives primary actions and highlights, creating immediate visual focus against a largely achromatic interface. Components lean towards lightweight forms with subtle elevation, promoting a sense of speed and efficiency in a data-heavy environment.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Fog | `#f5f5f5` | neutral | Page background, subtle card backgrounds, navigational elements |
| Surface Snow | `#ffffff` | neutral | Primary card surfaces, button backgrounds, input fields, overlay elements |
| Ink Black | `#000000` | neutral | Primary text, strong headings, button text, borders, and UI icons, establishing high contrast |
| Graphite Text | `#4a5565` | neutral | Secondary text, body copy, and muted borders. A slightly softer alternative to Ink Black |
| Slate Gray | `#364153` | neutral | Navigation text and specific UI elements, slightly darker than Graphite Text |
| Whisper Gray | `#99a1af` | neutral | Tertiary text, decorative icons, and subtle borders, for low-priority information |
| Subdued Gray | `#676767` | neutral | Section headings with a softer presence than Ink Black |
| Border Ash | `#d4d4d4` | neutral | Hairline borders, subtle dividers, and input field outlines |
| Deep Shadow | `#1f1f1f` | neutral | Background for elevated cards or sections, indicating depth |
| Action Blaze | `#e42b0c` | brand | Primary call-to-action buttons, active states, and critical highlights – the sole vivid accent |
| Gradient Aura | `#ff7802` | brand | Decorative gradients in hero sections and prominent visual elements, evoking energy and dynamism |

## Typography

### telegraf

| Key | Value |
| --- | --- |
| weight | 100, 300, 400, 500, 600, 700 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px, 30px, 48px, 60px, 72px |
| lineHeight | 1.00, 1.20, 1.33, 1.38, 1.40, 1.43, 1.50, 1.56, 1.63 |
| letterSpacing | 0, 0, 0, 0, 0, 0, -0.75, -1.44, -1.5, -2.16 |
| fontFeatureSettings | "case", "dlig" |
| substitute | Montserrat, Raleway |
| role | Primary brand typeface for all headings, body text, buttons, and UI elements. Its sharp, modern geometry supports the authoritative and efficient brand image. The variable letter-spacing, particularly at larger sizes, creates a precise, almost condensed feel for headlines. |

### modernGothic

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 14px |
| lineHeight | 1.43 |
| letterSpacing | 0 |
| fontFeatureSettings | "ss02" |
| substitute | IBM Plex Sans Light |
| role | Secondary typeface predominantly used for subtle hints, links, or specific decorative text. Its light weight and 'ss02' stylistic set add a touch of unique character. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0 |
| body-sm | 14 |  | 1.43 | 0 |
| body | 16 |  | 1.5 | 0 |
| subheading | 18 |  | 1.5 | 0 |
| heading-sm | 20 |  | 1.4 | 0 |
| heading | 24 |  | 1.33 | 0 |
| heading-lg | 30 |  | 1.2 | -0.75 |
| display | 48 |  | 1 | -1.44 |
| display-lg | 60 |  | 1 | -1.5 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| all | 8px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 16px
- **pageMaxWidth** — 1440px

## Components

### Primary Action Button

**Role:** CTA button

Filled with Action Blaze (#e42b0c), white text, 8px border-radius, 12px vertical padding, 24px horizontal padding. Prominently calls to action.

### Secondary Action Button

**Role:** Secondary button

Filled with Ink Black (#000000), white text, 6px border-radius, 12px vertical padding, 24px horizontal padding. Used for important but not primary actions.

### Ghost Button

**Role:** Tertiary button

Transparent background, Ink Black (#000000) text, 8px border-radius, 32px vertical padding, 16px horizontal padding. Used for less prominent actions, often within sections.

### Card - Default

**Role:** Content container

backgroundColor=Surface Snow (#ffffff), 8px border-radius, 32px padding, subtle shadow (rgba(0,0,0,0.1) 0px 1px 3px 0px, rgba(0,0,0,0.1) 0px 1px 2px -1px). Used for feature blocks and testimonial cards.

### Card - Elevated

**Role:** Accentuated content container

backgroundColor=Deep Shadow (#1f1f1f), 8px border-radius, 96px vertical padding, 64px horizontal padding, no shadow. Used for visually distinct sections or hero content on deeper backgrounds.

### Card - Section

**Role:** Full-width section container

backgroundColor=Canvas Fog (#f5f5f5), 8px border-radius, 4px padding. Used to segment page content visually.

### Input Field

**Role:** Data entry control

backgroundColor=Surface Snow (#ffffff), Border Ash (#d4d4d4) 1px border, 4px border-radius, 16px padding. Text color is Ink Black (#000000).

## Layout

The page maintains a centered, max-width contained layout at 1440px. The hero section features a prominent headline to the left, paired with a dynamic, dark-themed product UI visual on the right. Content sections alternate between subtle gray bands (Canvas Fog) and crisp white (Surface Snow), creating a predictable vertical rhythm. Feature blocks are presented in a two-column text-left/image-right alternating pattern or a three-column card grid, providing clear visual organization. Navigation is a sticky top bar, providing persistent access to key sections. The overall density is comfortable, ensuring sufficient breathing room between content blocks and sections.

## Imagery

The imagery consists primarily of abstract, tech-oriented graphics and stylized product UI screenshots. Graphics use a limited color palette, often incorporating transparent overlays of the brand's orange-red gradient, making them feel integrated rather than decorative. Product UI screenshots are contained within structured, often dark, frames, presenting data and system flows with a focused, informational purpose. There are no lifestyle photos or complex illustrations; the visual language is direct and functional. Icons are simple, monochrome, and filled, used semantically to reinforce meaning without adding visual noise. Image density is moderate, used to break up text-heavy sections or highlight key features.

## Dos & Donts

### Do

- Always use telegraf for all text elements; prefer weights 400 and above for readability and impact, reserving lighter weights (300) for subheadings only.
- Utilize Action Blaze (#e42b0c) exclusively for primary calls-to-action to maximize its impact and differentiate it from other interactive elements.
- Maintain a clear visual hierarchy of surfaces: Canvas Fog (#f5f5f5) for the base, then Surface Snow (#ffffff) for cards and inputs, and Deep Shadow (#1f1f1f) for prominent, darker content blocks.
- Apply an 8px border-radius to all interactive elements, cards, and input fields to maintain a consistent, slightly softened aesthetic.
- Use Ink Black (#000000) for all primary text and critical UI elements, ensuring maximum contrast and readability on light backgrounds.
- Implement consistent internal padding of 16px and 24px for component elements and text blocks to define clear content areas.
- Employ the mild elevation shadow (rgba(0,0,0,0.1) 0px 1px 3px 0px, rgba(0,0,0,0.1) 0px 1px 2px -1px) sparingly, primarily for interactive cards or elements that require subtle projection.

### Don't

- Do not introduce new chromatic colors; Action Blaze (#e42b0c) is the singular brand accent.
- Avoid using multiple border-radii values; the consistent 8px radius is a core part of the system's shape language.
- Do not apply strong shadows or complex gradients to buttons or cards unless explicitly defined; elements should feel lightweight.
- Refrain from deviating from the defined telegraf and modernGothic font families or their specified weights and letter-spacing values.
- Do not use dark backgrounds interchangeably with light ones; adhere strictly to the light theme model where Canvas Fog (#f5f5f5) is the dominant base.
- Avoid dense packing of content; ensure ample white space and adhere to the 16px element gap and 40px section gap to maintain a comfortable reading experience.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #f5f5f5
border: #d4d4d4
accent: #e42b0c
primary action: #e42b0c (filled action)

Example Component Prompts:
1. Create a Primary Action Button: filled with Action Blaze (#e42b0c), white text (#ffffff), 8px border-radius, 12px vertical padding, 24px horizontal padding, 'telegraf' font family, weight 700.
2. Create a Card - Default: background Surface Snow (#ffffff), 8px border-radius, 32px padding, with elevation shadow rgba(0,0,0,0.1) 0px 1px 3px 0px, rgba(0,0,0,0.1) 0px 1px 2px -1px. Inside, use Graphite Text (#4a5565) for body, and Ink Black (#000000) for heading, both 'telegraf' font.
3. Create a Ghost Button: transparent background, Ink Black (#000000) text, 8px border-radius, 32px vertical padding, 16px horizontal padding, 'telegraf' font family, weight 400.
4. Create an Input Field: background Surface Snow (#ffffff), Border Ash (#d4d4d4) 1px border, 4px border-radius, 16px padding, Ink Black (#000000) text for input, 'telegraf' font family, weight 400.

---
_Source: https://styles.refero.design/style/d95a35c2-d3f7-49e7-9ba7-282d52d3211d_
