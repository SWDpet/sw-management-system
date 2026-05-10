# Rootly — Design Reference

> Violet-tinged command center behind frosted glass.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://rootly.com](https://rootly.com) |
| Refero page | [https://styles.refero.design/style/a037c352-4315-4650-a16c-08392ffca597](https://styles.refero.design/style/a037c352-4315-4650-a16c-08392ffca597) |
| Theme | light |
| Industry | ai |

## Overview

Rootly presents a calm yet capable aesthetic, balancing robust AI functionality with clear, user-centric design. Muted violet and near-black create a professional, deep canvas, punctuated by a brighter, vivid violet that signifies action and focus. Surfaces are typically soft and rounded, offering subtle visual cues rather than harsh divisions. Typography is precise and efficient, maintaining clarity across data-rich interfaces, while gradients offer a touch of sophisticated atmosphere without visual noise.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#100f12` | neutral | Primary text, major headings, background for dark elements like primary buttons. A near-black that provides high contrast |
| Canvas White | `#ffffff` | neutral | Background for primary interactive elements like buttons and navigation. Creates a stark contrast against darker text elements |
| Haze White | `#fbfaff` | neutral | Default page background. A very slight off-white, providing a soft base layer |
| Cadet Violet | `#d9cffa` | brand | Violet state accent for badges, validation surfaces, and short status labels. Do not promote it to the primary CTA color |
| Deep Space Violet | `#8d6fde` | brand | Primary accent for interactive states, icons, and borders. A vivid, saturated violet that draws attention |
| Lavender Mist | `#ebe5ff` | brand | Subtle card backgrounds and low-prominence button fills. A very light, desaturated violet |
| Graphite Feather | `#787685` | neutral | Muted body text, secondary descriptions, and borders for subtle UI separation |
| Nightfall Violet | `#4a3e8a` | brand | Darker shade of violet used for text within interactive elements and borders for emphasis |
| Ash Grey | `#65646e` | neutral | Auxiliary body text and less prominent content, offering slightly softer contrast than Midnight Ink |
| Faded Stone | `#aaa9ae` | neutral | Placeholder text and subtle secondary information, appearing as a light gray |
| Sky Veil Gradient | `#e9e2ff` | accent | Decorative background gradient used for atmospheric sections, transitioning from a light violet to a pale pink |
| Twilight Horizon Gradient | `#7748f6` | accent | Hero section background, conveying depth and sophistication by blending vibrant violet with soft rose |

## Typography

### Ppmori

| Key | Value |
| --- | --- |
| weight | 200, 500, 600 |
| weights | 200, 500, 600 |
| sizes | 10px, 12px, 14px, 16px, 17px, 31px, 52px |
| lineHeight | 1.00, 1.03, 1.20, 1.30, 1.40, 1.43, 1.45, 1.46 |
| letterSpacing | -0.0150em, -0.0130em, -0.0100em, -0.0090em |
| substitute | Inter |
| role | Primary typeface for all text elements. Its sharp, modern lines and range of weights, particularly lighter weights for headlines, signal precision and a technical yet refined brand identity. A custom font, it brings a distinctive character that system fonts lack. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.45 | -0.15 |
| body | 14 |  | 1.46 | -0.14 |
| heading | 31 |  | 1.2 | -0.31 |
| display | 52 |  | 1.03 | -0.78 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| full | 1440px |
| large | 34.6px |
| small | 13.84px |
| medium | 17.3px |
| xsmall | 6.92px |

- **elementGap** — 7px
- **sectionGap** — 42px
- **cardPadding** — 14px
- **pageMaxWidth** — 1600px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Haze White Canvas | `#fbfaff` | 0 | Base page background, providing a soft, almost imperceptible off-white foundation. |
| Lavender Mist Card | `#ebe5ff` | 1 | Background for secondary content cards or sections, offering a gentle visual lift from the canvas. |
| Canvas White Element | `#ffffff` | 2 | Background for primary interactive elements like buttons, header, and active states, creating distinct contrast and visual hierarchy. |

## Components

### Primary Filled Button

**Role:** Main call to action button.

Background: Midnight Ink (#100f12). Text: Canvas White (#ffffff). Corner radius: 17.3px. Padding: 15.57px vertical, 27.68px horizontal.

### Secondary Filled Button (Canvas)

**Role:** Secondary call to action, often paired with primary button.

Background: Canvas White (#ffffff). Text: Midnight Ink (#100f12). Corner radius: 17.3px. Padding: 20.76px vertical, 20.76px horizontal.

### Ghost Button

**Role:** Tertiary action or navigational button within text.

Background: transparent. Text: Midnight Ink (#100f12). Border: 1px solid Midnight Ink (#100f12). Corner radius: 13.84px. Padding: 8.65px top, 13.84px horizontal, 7.266px bottom.

### Light Card

**Role:** Content container with a soft background.

Background: Haze White (#fbfaff). Corner radius: 34.6px. Padding: 6.92px. No shadow.

### Accent Card

**Role:** Highlighted content container.

Background: Lavender Mist (#ebe5ff). Corner radius: 17.3px. No shadow. No padding.

### Text Input Field

**Role:** Standard input for user data.

Background: transparent. Text: Nightfall Violet (#4a3e8a). Border: 1px solid Cadet Violet (#d9cffa). Corner radius: 1440px (full pill shape). Padding: 13.84px top, 17.3px horizontal, 12.11px bottom.

## Layout

The page primarily employs a max-width contained layout of 1600px, centered on the screen. The hero section is full-bleed, featuring a centered headline over a gradient background, setting an atmospheric tone. Subsequent sections alternate between light backgrounds (Haze White) and gradient backgrounds (Sky Veil Gradient), creating a rhythmic flow down the page. Content within sections is arranged in a fluid, often two-column layout with text on one side and product visuals or abstract graphics on the other, occasionally reversing the order. There's a notable use of a 3-column grid for partner logos below the hero. Navigation is handled by a sticky top bar with text links and two distinct pill-shaped buttons in the top right corner.

## Imagery

The site uses a combination of abstract, atmospheric imagery and product-focused UI screenshots. Backgrounds often feature stylized, muted purple-and-pink gradients depicting mountainous or cloud-like formations, providing a soft, almost ethereal backdrop. Product screenshots are typically tightly cropped and presented with clean, often transparent, backgrounds, highlighting the UI elements themselves. Icons are monochromatic, using Midnight Ink (#100f12) or Deep Space Violet (#8d6fde), with a clear, outlined style, some showing rounded square containers. Imagery serves both as decorative atmosphere for brand identity and explanatory content for product features, balancing visual appeal with clear communication. The image density is moderate, allowing ample space for text.

## Dos & Donts

### Do

- Prioritize Ppmori weight 600 for main headings, Midnight Ink (#100f12) text color, and a line height of 1.03 for impact. Apply tracking like -0.0150em for large sizes.
- Use Haze White (#fbfaff) for default page backgrounds to maintain a soft, light canvas.
- Accent interactive elements (icons, borders, active states) with Deep Space Violet (#8d6fde) to draw focus and provide visual feedback.
- Ensure all buttons and interactive elements apply a rounded corner, with a full pill shape (1440px) or 17.3px for other prominent elements.
- Utilize a 7px element gap for comfortable spacing between general UI elements.
- Apply Cadet Violet (#d9cffa) for input borders to indicate interactive fields without high visual noise.
- Employ Lavender Mist (#ebe5ff) for subtle card backgrounds to differentiate content blocks gently.

### Don't

- Avoid using highly saturated colors for large background areas, reserving them for small, functional accents.
- Do not introduce sharp corners on interactive components; maintain the rounded aesthetic across cards, buttons, and inputs.
- Do not use dark backgrounds for general page content; maintain a light theme for readability and brand consistency.
- Avoid overly bold or heavy typography for body text; rely on the lighter weights of Ppmori and Midnight Ink for primary content.
- Do not use box-shadows for elevation; rely on background color changes or subtle borders for surface distinction.
- Avoid generic blue for links or buttons; use Canvas White (#ffffff) or Midnight Ink (#100f12) for filled buttons, or Deep Space Violet (#8d6fde) for outlined accents.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #100f12
background: #fbfaff
border: #d9cffa
accent: #8d6fde
primary action: #100f12 (filled action)

Example Component Prompts:
Create a Primary Action Button: #100f12 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Create a product feature card: Accent Card (#ebe5ff, 17.3px radius, no padding). Inside, a Ppmori weight 500, 16px subheading (#100f12) with a short Ppmori weight 500, 14px body text (#65646e) below it. Add 14px padding to the content inside the card.

Create an input field: Text Input Field type="text" with placeholder 'Enter your email' (Ppmori weight 500, 14px, Faded Stone #aaa9ae). Border is Cadet Violet (#d9cffa), radius 1440px. Text color Nightfall Violet (#4a3e8a).

---
_Source: https://styles.refero.design/style/a037c352-4315-4650-a16c-08392ffca597_
