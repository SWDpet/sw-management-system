# Prismic — Design Reference

> High-contrast digital canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://prismic.io](https://prismic.io) |
| Refero page | [https://styles.refero.design/style/cc20715c-e9ab-42a7-a34b-d43d76677219](https://styles.refero.design/style/cc20715c-e9ab-42a7-a34b-d43d76677219) |
| Theme | light |
| Industry | saas |

## Overview

Prismic embraces a high-contrast, functionally colorful aesthetic where sharp, modern sans-serif typography grounds an otherwise energetic palette. Neutrals provide a strong foundation for interface elements, while saturated blues, purples, and greens punctuate interactive states and draw attention to key elements. The system balances visual density with comfortable spacing, promoting a clear hierarchy and an approachable feel without sacrificing professionalism.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Charcoal | `#151515` | neutral | Page backgrounds, deep surface backgrounds, primary text, filled button backgrounds |
| Canvas White | `#ffffff` | neutral | Card surfaces, primary section backgrounds, inverted text, ghost button text |
| Ash Gray | `#505050` | neutral | Muted text, secondary button text, subtle borders |
| Moonlight Gray | `#eeeeee` | neutral | Hairline borders, subtle dividers, ghost button borders |
| Light Taupe | `#f7f7f7` | neutral | Subtle background panels, light card surfaces |
| Sky Burst | `#59b5f8` | brand | Blue accent for outlined action borders, linked labels, and lightweight interactive emphasis. Do not promote it to the primary CTA color |
| Deep Plum | `#8e44ec` | brand | Violet accent for outlined action borders, linked labels, and lightweight interactive emphasis. Do not promote it to the primary CTA color |
| Jade Glow | `#3bbb96` | brand | Green decorative accent for icons, marks, and small graphic details |
| Cool Mint | `#e8f8f3` | accent | Lightest background for accent cards, subtle highlights |
| Pale Peach | `#fef1e9` | accent | Light background for accent cards |
| Soft Lavender | `#f5e6ff` | accent | Light background for accent cards |
| Powder Blue | `#e6f7fe` | accent | Light background for accent cards |
| Faded Lilac | `#e8c7ff` | accent | Accent card backgrounds, subtle list item backgrounds |
| Icy Blue | `#c3eefe` | accent | Accent card backgrounds, subtle list item backgrounds |
| Warm Beige | `#fcdac4` | accent | Accent card borders |
| Delicate Green | `#d4f2e9` | accent | Accent card borders |

## Typography

### copyFont

| Key | Value |
| --- | --- |
| weight | 500, 600, 700 |
| sizes | 14px, 16px, 18px, 22px |
| lineHeight | 1, 1.14, 1.43, 1.45, 1.5, 1.56 |
| role | copyFont — detected in extracted data but not described by AI |

### Inter

| Key | Value |
| --- | --- |
| letterSpacing | normal |
| substitute | system-ui, sans-serif |
| role | General body text, navigation items, buttons, links, and list items. Text is usually compact but highly legible. |

### Inter

| Key | Value |
| --- | --- |
| letterSpacing | -0.025em |
| substitute | system-ui, sans-serif |
| role | Headlines throughout the site, characterized by tighter tracking at larger sizes to maintain a sleek, modern appearance. |

### headingsFont

| Key | Value |
| --- | --- |
| weight | 500, 700 |
| sizes | 16px, 18px, 22px, 28px, 32px, 40px, 56px |
| lineHeight | 1.1, 1.13, 1.14, 1.27, 1.33, 1.38 |
| letterSpacing | -0.025 |
| role | headingsFont — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.43 | 0 |
| body | 16 |  | 1.5 | 0 |
| subheading | 18 |  | 1.45 | 0 |
| heading-sm | 22 |  | 1.14 | 0 |
| heading | 28 |  | 1.27 | -0.7 |
| heading-lg | 32 |  | 1.13 | -0.8 |
| display | 56 |  | 1.1 | -1.4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| icons | 9999px |
| links | 2px |
| buttons | 8px |

- **elementGap** — 16px
- **sectionGap** — 32px
- **cardPadding** — 40px
- **pageMaxWidth** — 1280px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Primary page background, base for content sections |
| Light Taupe | `#f7f7f7` | 2 | Subtle background for alternating sections or elevated content blocks |
| Accent Card Backgrounds | `#fff` | 3 | Specialized cards and testimonials, using hues like #f5e6ff, #e8f8f3, #fef1e9, #e6f7fe for thematic grouping |
| Midnight Charcoal Deep | `#151515` | 4 | Overlapping dark sections, footer, or focused informational panels |

## Components

### Primary Filled Button

**Role:** Main call-to-action button for initiating key actions.

Background: Midnight Charcoal (#151515), Text: Canvas White (#ffffff), Padding: 12px vertical, 24px horizontal, Radius: 8px, Font: Inter weight 500, 16px.

### Ghost Button

**Role:** Secondary calls-to-action, or less emphasized actions.

Background: transparent, Text: Canvas White (#ffffff) or Ash Gray (#505050), Border: 1px transparent, Radius: 0px or 8px depending on context, Padding: 0px or 16px vertical depends on context, no horizontal padding.

### Navigation Link

**Role:** Primary navigation items in the header and footer.

Text: Midnight Charcoal (#000000) or Canvas White (#ffffff), Font: Inter weight 500, 16px. Underline on hover.

### Branded Accordion Card

**Role:** Informational cards presenting features or testimonials, often with a unique accent background.

Backgrounds: Soft Lavender (#f5e6ff), Cool Mint (#e8f8f3), Pale Peach (#fef1e9), or Powder Blue (#e6f7fe). Radius: 12px. Padding: 40px all sides. No shadow, flat surface.

### Outlined Accent Button

**Role:** Action buttons emphasizing connection to specific brand colors.

Background: transparent, Text: Deep Plum (#8e44ec) or Sky Burst (#59b5f8), Border: 1px currentColor, Radius: 8px. Font: Inter weight 500, 16px. Padding: 12px vertical, 24px horizontal.

## Layout

The page layout follows a centered, max-width (1280px) container model with the exception of the hero section which is mostly full-bleed. The hero features a large, centered headline over a dark background with illustrative abstract graphics in the corners, immediately establishing the brand's visual identity. Vertical rhythm is established through consistent section gaps (32px), often alternating between a Canvas White background and a slightly darker Midnight Charcoal or Light Taupe. Content sections frequently use two-column layouts, often with text on one side and a visual element or card on the other, sometimes alternating. A grid of accent-colored cards is used for testimonials. Navigation is a sticky top bar, with prominent buttons and links.

## Imagery

The site primarily uses flat, outlined, geometric illustrations in brand colors (Sky Burst, Deep Plum, Jade Glow) which have a clean, technical feel. These abstract shapes often have connecting lines, suggesting flow and data. Product screenshots are minimal, focused, and integrated subtly. Icons are either outlined or solid, simple, and monochromatic or tinted with brand colors. Imagery serves an explanatory and decorative role, suggesting functionality and process rather than showcasing product UIs or lifestyle photography. Image density is moderate, used to break up text and add visual interest rather than dominate the layout.

## Dos & Donts

### Do

- Prioritize Midnight Charcoal (#151515) for all primary text and dark backgrounds and Canvas White (#ffffff) for light backgrounds and inverted text to maintain high contrast.
- Use Inter font family exclusively, applying a negative letter-spacing of -0.025em for all headings to create a distinctive, sleek appearance.
- Apply a consistent border-radius of 8px for buttons and 12px for cards, reserving 9999px for small circular accents like icons.
- Utilize Sky Burst (#59b5f8), Deep Plum (#8e44ec), and Jade Glow (#3bbb96) sparingly as functional accents for interactive elements, icons, and significant headings.
- Maintain comfortable spacing: 32px for section gaps, 40px for card interior padding, and 16px for vertical spacing between elements within sections.
- Structure pages within a 1280px max-width container, ensuring content is centered and never spans the full viewport unless explicitly designed as a full-bleed hero.
- Employ the light accent background colors like Soft Lavender (#f5e6ff) or Cool Mint (#e8f8f3) for cards or highlighted sections to add visual interest without heavy reliance on shadows.

### Don't

- Do not use highly saturated colors for large background areas or extensive body text; reserve them for accents and interactive elements.
- Avoid generic border-radii; adhere strictly to 2px for links, 8px for buttons, 12px for cards, and 9999px for icons.
- Do not introduce additional font families; maintain consistency with the Inter font for all typographic elements.
- Refrain from heavy drop shadows or elaborate elevation effects; surfaces are predominantly flat, and dimension comes from color contrast and varying background tints.
- Do not vary letter-spacing for body text; only use the specified negative tracking for headlines.
- Avoid dense, information-heavy layouts; ensure ample negative space using the defined element, card, and section gaps for a comfortable user experience.
- Do not default to standard black for all text; differentiate with Ash Gray (#505050) for muted or secondary text.

## Notes

### Agent Prompt Guide

primary action: #151515 (filled action)
Create a Primary Action Button: #151515 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.


Create a testimonial card on a Canvas White background: Background Soft Lavender (#f5e6ff). Text 'This is a great product.' using Inter, 16px, weight 500, #151515. Subtext 'User Name, Company' using Inter, 14px, weight 500, #505050. Radius 12px, padding 40px all sides.

Create a dark feature panel: Background Midnight Charcoal (#151515). Heading 'Developers: Create a custom page builder' using Inter, 28px, weight 700. Accent color Deep Plum (#8e44ec) for 'Developers:'. Body text 'Use our developer tool...' using Inter, 16px, weight 500, #ffffff. Outlined Accent Button 'Explore your Prismic Journey' using Deep Plum as border and text color, 8px radius, 12px vertical padding, 24px horizontal padding.

---
_Source: https://styles.refero.design/style/cc20715c-e9ab-42a7-a34b-d43d76677219_
