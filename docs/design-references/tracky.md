# Tracky — Design Reference

> Playful productivity, high-contrast clarity

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://tracky.so](https://tracky.so) |
| Refero page | [https://styles.refero.design/style/34788d94-1147-4d38-8df7-6f47ef7efb12](https://styles.refero.design/style/34788d94-1147-4d38-8df7-6f47ef7efb12) |
| Theme | light |
| Industry | productivity |

## Overview

Tracky employs a playful yet focused productivity aesthetic, drawing strongly from a high-contrast dark navy (#151b31) and white (#ffffff) palette. Typography is a key differentiator, mixing a bold, wide sans-serif for impact with a highly readable modern sans for content. Accents are used sparingly as vital punctuation, injecting vivid red (#ff5858) for emphasis and a soft green (#86e0c1) for status, creating clear functional signals. Surfaces are generally flat and minimal, with subtle elevation used only for interactive elements and key cards.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#151b31` | brand | Primary text, deep surface backgrounds, interactive button fills, borders, badge backgrounds — establishes a strong, focused presence |
| Spark Red | `#ff5858` | accent | Accent for emphasized headlines, active links, and highlighting critical information, adding a dynamic and attention-grabbing element |
| Success Green | `#86e0c1` | semantic | Feedback for success states, badges, and positive indicators, providing a soft, reassuring signal |
| Harvest Orange | `#ffb07b` | accent | Orange wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| White Canvas | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button backgrounds/text, primary text on dark surfaces — provides a clean, expansive base |
| Carbon Text | `#333333` | neutral | Secondary body text and general content text, providing strong readability against light backgrounds |
| Soft Fog | `#f2f2f2` | neutral | Subtle page section backgrounds and muted borders, offering a gentle visual separation without stark contrast |
| Slate Text | `#6d6f75` | neutral | Muted helper text, secondary information, and subtle link text, creating a softer presence than primary text |
| Shadow Tint | `#e8e7e5` | neutral | Subtle shadow tint for cards, providing depth without heaviness |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 16px, 18px, 20px, 22px, 24px, 36px, 108px |
| lineHeight | 1.00, 1.10, 1.16, 1.18, 1.30, 1.40, 1.50, 1.55, 1.74 |
| letterSpacing | -0.0100em |
| substitute | system-ui |
| role | Body text, UI labels, links, and general content — chosen for its modern readability and versatility across many sizes and weights. |

### GRIFTER

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 12px, 24px, 36px, 48px, 64px |
| lineHeight | 1.00, 1.10, 1.20, 1.41, 2.00 |
| letterSpacing | 0.0300em |
| substitute | Impact |
| role | Primary headlines and emphasized display text — its bold, wide character and slightly positive letter-spacing convey a confident, energetic brand voice. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.12 |
| body-sm | 16 |  | 1.5 | -0.16 |
| body | 18 |  | 1.5 | -0.18 |
| subheading | 20 |  | 1.4 | -0.2 |
| heading-sm | 22 |  | 1.3 | -0.22 |
| heading | 24 |  | 1.1 | 0.72 |
| heading-lg | 36 |  | 1.1 | 1.08 |
| display | 108 |  | 1 | -1.08 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 100% |
| cards | 16px |
| image | 8px |
| input | 8px |
| badges | 8px |
| buttons | 8px |
| default | 8px |

- **elementGap** — 20px
- **sectionGap** — 40px
- **cardPadding** — 15px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Soft Fog Canvas | `#f2f2f2` | 0 | Primary page background and subtle section dividers, offering a gentle, breathable foundation. |
| White Card Surface | `#ffffff` | 1 | Default background for content cards and panels, serving as a bright, clean surface for information. |
| Midnight Ink Panel | `#151b31` | 2 | Elevated card backgrounds and prominent sections, creating strong visual contrast and emphasis. |

## Components

### Ghost Button

**Role:** Secondary action control

Transparent background with a Midnight Ink (#151b31) border, Midnight Ink text (Inter, weight 400), 8px border-radius, and 8px vertical / 24.5px horizontal padding. Features a subtle soft shadow on hover.

### Primary Filled Button

**Role:** Key action control

Solid Midnight Ink (#151b31) background with White Canvas (#ffffff) text (Inter, weight 400), matching Midnight Ink border, 8px border-radius, and 13px vertical / 65px horizontal padding. Used for prominent calls to action.

### Elevated Tool Card

**Role:** Interactive/featured content container

White Canvas (#ffffff) background, 8px border-radius, and a soft card shadow rgba(138, 133, 125, 0.2) 0px 1px 4px 0px. Features 9px vertical / 20px horizontal padding for contained elements.

### Solid Section Card

**Role:** Content grouping container

Midnight Ink (#151b31) background, 16px border-radius, and no shadow. Provides 48px top padding, 120px horizontal padding, and 0px bottom padding for content.

### Feature Badge - Success

**Role:** Informational tag for positive status

Success Green (#86e0c1) background with Midnight Ink (#151b31) text (Inter, weight 400), 8px border-radius, and 8px all-around padding.

### Pill Accent Badge

**Role:** Decorative or small status badge

Midnight Ink (#151b31) background with matching text color, 100% border-radius for a pill shape, and no padding. Used for small, contained visual indicators.

## Layout

The page primarily uses a `max-width: 1200px` contained layout, with content centered. The hero section often breaks this, featuring full-bleed dark backgrounds with centered, expressive headlines. Section rhythm is created through alternating background colors (Soft Fog and White Canvas) or by using large Midnight Ink panels for key feature blocks. Content is typically arranged in clear, often two-column, text-left/image-right (or vice-versa) sections, with larger headlines and concise body copy. The layout feels spacious between sections but compact within content blocks, balancing breathability with information density. Navigation is a minimal top bar with simple text links and a ghost 'Login / Sign up' button.

## Imagery

Imagery features a mix of abstract, doodle-like illustrations and stylized product mockups. Illustrations are single-color outlines (gray, black) with occasional brand-color fills (e.g., blue, green, orange shapes) that appear scattered or in small, contained forms. Product screenshots are typically tight crops on simple, often dark or neutral backgrounds, highlighting the UI functionality directly without additional lifestyle context. Icons are simple, outlined, and monochromatic, used for decorative accents or functional navigation markers. The overall role is to add a playful, approachable personality and to clearly showcase product features in a clean, graphic manner.

## Dos & Donts

### Do

- Prioritize the high-contrast pairing of Midnight Ink (#151b31) and White Canvas (#ffffff) for primary text and backgrounds to maintain visual clarity.
- Use GRIFTER for all major headlines to convey brand confidence, ensuring consistent positive letter-spacing of 0.03em.
- Apply Inter for all body text, UI elements, and labels, opting for a 1.5 line height for optimal readability.
- Use Spark Red (#ff5858) specifically for points of emphasis in headlines or active link states to draw immediate attention without overwhelming the design.
- Maintain a comfortable density with a base spacing unit of 4px and an `elementGap` of 20px for consistent visual rhythm between components.
- Apply 8px global border-radius for buttons, badges, and default elements, reserving 16px for larger content cards to differentiate structural surfaces.
- Employ subtle box shadows only for interactive components and key elevated cards to provide depth without making surfaces heavy.

### Don't

- Avoid using multiple chromatic colors for primary actions; Midnight Ink (#151b31) with White text is the established filled primary button style.
- Do not vary letter-spacing for Inter font family unless specifically defined in the type scale, as its default tracking is optimized for readability.
- Refrain from heavy, multi-layered shadows; elevation is minimal and serves to indicate interactivity or hierarchy, not decoration.
- Do not introduce new decorative elements or overly complex background textures; the design system favors clean surfaces and a direct, functional aesthetic.
- Avoid using Spark Red (#ff5858) for semantic 'error' states, as its primary role is brand highlighting and emphasis.
- Do not deviate from the defined border-radius values; the 8px/16px system is critical for consistent component identity.
- Do not fill Ghost Buttons with color; their transparent background with a distinct border is their defining characteristic.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #151b31
- background: #f2f2f2
- border: #151b31
- accent: #ff5858
- primary action: #151b31 (filled action)

### 3-5 Example Component Prompts
1. Create a hero section: full-bleed Midnight Ink (#151b31) background. Centered headline 'A new project management tool you'll actually love.' using GRIFTER weight 700. The word 'actually love.' should be Spark Red (#ff5858). Add a Primary Filled Button below it, 'Get started', with a Midnight Ink (#151b31) background, White Canvas (#ffffff) text (Inter weight 400), 8px border-radius, and 13px vertical / 65px horizontal padding.
2. Design a feature card: White Canvas (#ffffff) background, 8px border-radius, and the special card shadow rgba(138, 133, 125, 0.2) 0px 1px 4px 0px. Inner contents (e.g., text and icons) should have 9px vertical / 20px horizontal padding.
3. Implement a secondary navigation item: ghost button 'Login / Sign up' with a transparent background, Midnight Ink (#151b31) text (Inter weight 400), a 1px solid Midnight Ink (#151b31) border, 8px border-radius, and 8px vertical / 24.5px horizontal padding.
4. Show a positive status indicator: Feature Badge with Success Green (#86e0c1) background, Midnight Ink (#151b31) text (Inter weight 400), 8px border-radius, and 8px all-around padding.

---
_Source: https://styles.refero.design/style/34788d94-1147-4d38-8df7-6f47ef7efb12_
