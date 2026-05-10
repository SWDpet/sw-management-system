# Lightdash — Design Reference

> Codebase blueprint on frosted glass

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.lightdash.com](https://www.lightdash.com) |
| Refero page | [https://styles.refero.design/style/d0f65d12-a8e6-4631-99f7-bb7cdcd5b6c5](https://styles.refero.design/style/d0f65d12-a8e6-4631-99f7-bb7cdcd5b6c5) |
| Theme | light |
| Industry | ai |

## Overview

Lightdash employs a precise, tech-forward aesthetic using a monochromatic palette punctuated by a single vibrant accent. Typography is a key identifier, balancing strong, impactful sans-serif headlines with clean, readable body text. Components favor soft curves and subtle elevation, appearing lightweight yet defined, maintaining a sense of digital clarity and focused productivity. The overall design feels robust and intelligent, reflecting its AI/BI platform nature.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#1a1b25` | neutral | Primary text for headings and interactive elements, button backgrounds for ghost/outlined variants |
| Charcoal Slate | `#272835` | neutral | Hero section text, secondary button backgrounds, and subtle borders |
| Deep Indigo | `#36394a` | neutral | Input backgrounds, primary body text, and some borders |
| Steel Gray | `#666d80` | neutral | Secondary body text and muted informational details |
| Cloud Gray | `#818898` | neutral | Tertiary text, decorative elements, and subtle icon strokes |
| Stone Wash | `#a4abb8` | neutral | Light helper text, link borders, and subtle descriptive text |
| Off-White | `#f8fafb` | neutral | Subtle background for UI elements, light cards |
| Canvas White | `#ffffff` | neutral | Page backgrounds, primary card surfaces, high-contrast text |
| Lava Cloud | `#eceff3` | neutral | Subtle background for alternating sections or distinct UI areas |
| Ghost Fill | `#f6f8fa` | neutral | Background for subtle surface differentiation, such as active states or hovering elements |
| Electric Violet | `#5e4cff` | brand | Primary action buttons, interactive highlights, and brand accents to draw attention |
| Lavender Mist | `#dfdbff` | accent | Subtle button backgrounds, decorative accents, and soft focus states |
| Pixel Purple | `#c8ccf3` | accent | Decorative background fills, hinting at digital patterns and data visualizations |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.2 |
| role | sans-serif — detected in extracted data but not described by AI |

### Britti Sans Trial Semibold

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| weights | 400, 600 |
| sizes | 18px, 48px, 56px, 57px, 76px |
| lineHeight | 0.90, 0.95, 1.00, 1.56 |
| letterSpacing | -0.025em, -0.020em |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| substitute | Montserrat |
| role | Primary headlines and impactful display text. The tight letter-spacing at larger sizes gives a sense of modern precision and weight without being bold, authoritative through restraint. |

### Britti Sans Medium

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 14px, 16px, 18px, 20px, 24px, 32px |
| lineHeight | 1.11, 1.14, 1.20, 1.25, 1.30, 1.80, 2.00 |
| letterSpacing | -0.020em, -0.010em, 0.010em |
| substitute | Montserrat |
| role | Subheadings, section titles, and prominent UI labels. A slightly more relaxed tracking than display text, maintaining legibility at various intermediate sizes. |

### Britti Sans Trial Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 48px |
| lineHeight | 1.00, 1.20 |
| letterSpacing | -0.020em |
| substitute | Montserrat |
| role | Specific display text, often used for numerical data or secondary large-scale headings, offering a slightly lighter visual presence than semibold. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 9px, 12px, 13px, 14px, 15px, 16px, 17px, 18px, 24px |
| lineHeight | 1.00, 1.19, 1.20, 1.33, 1.38, 1.43, 1.50, 1.54, 1.63, 1.78 |
| letterSpacing | -0.020em, -0.015em, -0.010em |
| substitute | Inter |
| role | Body text, navigation, input fields, and most UI elements. Its variable weights provide flexibility for hierarchy without introducing additional type families. |

### Inter Variable

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 13px, 14px, 16px, 18px |
| lineHeight | 1.25, 1.43, 1.54, 1.67, 1.70 |
| letterSpacing | -0.010em |
| substitute | Inter |
| role | Specific body text instances, often for slightly larger or more prominent paragraph content, leveraging the variable font features for fine-tuned density. |

### IBM Plex Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.50 |
| letterSpacing | -0.020em |
| substitute | IBM Plex Mono |
| role | Monospaced text for code snippets, data displays, or precise technical information. Maintains a consistent visual rhythm. |

### Micro 5

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 31px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | Micro 5 (Google Fonts) |
| role | Highly stylized, decorative headlines or special numeric displays, used sparingly for specific visual impact that evokes digital code or retro computing. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body | 14 |  | 1.43 | -0.01 |
| subheading | 18 |  | 1.3 | -0.01 |
| heading-sm | 24 |  | 1.25 | -0.01 |
| heading | 32 |  | 1.14 |  |
| heading-lg | 48 |  | 1 | -0.025 |
| display | 76 |  | 0.9 | -0.025 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 12px |
| inputs | 0px |
| buttons | 8px |

- **elementGap** — 8px
- **sectionGap** — 40-64px
- **cardPadding** — 24-32px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background and default surface for content. |
| Ghost Fill | `#f6f8fa` | 1 | Subtle background for distinct sections, hover states, or input wrappers. |
| Lava Cloud | `#eceff3` | 2 | Alternating section backgrounds and more pronounced content dividers. |
| Off-White | `#f8fafb` | 3 | Background for specific UI components or elevated informational blocks. |

## Components

### Primary Action Button

**Role:** Call to action.

Filled button with Electric Violet (#5e4cff) background, white text, 8px border-radius, and 12px vertical, 20px horizontal padding. Uses Inter font weight 500.

### Secondary Ghost Button

**Role:** Secondary action or subtle interaction.

Ghost button with a transparent background, Midnight Ink (#1a1b25) border, a minimum 1px border stroke, 8px border-radius, and 12px vertical, 20px horizontal padding. Text color is Midnight Ink (#1a1b25). Uses Inter font weight 500.

### Tertiary Filled Button

**Role:** Informational actions or less prominent calls to action.

Filled button with Lava Mist (#dfdbff) background, Midnight Ink (#1a1b25) text, 8px border-radius, and 12px vertical, 20px horizontal padding.

### Interactive Chip Button

**Role:** Filter chips or toggleable options.

Card-like button with a light background (rgba(5, 5, 19, 0.04)), 26px border-radius for a pill shape, and 12px padding all around. No shadow.

### Elevated Feature Card

**Role:** Showcasing product features or key information.

Card with Canvas White (#ffffff) background, 20px border-radius, and a prominent shadow stack (rgba(39, 40, 53, 0.05) 0px 0px 0px 1px, rgba(39, 40, 53, 0.01) 0px 50px 20px 0px, etc.). Padding of 24px top, 32px horizontal, 20px bottom.

### Simple Information Card

**Role:** Containing lists, text, or less emphasized content.

Card with Canvas White (#ffffff) background, 12px border-radius, and a subtle 1px border shadow (rgba(39, 40, 53, 0.1) 0px 0px 0px 1px). Padding of 28px top/bottom, 22px horizontal.

### Segmented Control Item

**Role:** Selectable options within a group.

Segmented control item with a background of rgba(5, 5, 19, 0.06), 26px border-radius, and 16px top, 8px horizontal, 8px bottom padding. No shadow.

### Dark Input Field

**Role:** User input for forms in a dark context.

Input field with a Deep Indigo (#36394a) background, white text, and a white border (rgb(255, 255, 255)). Standard 10px vertical and 20-24px horizontal padding. No border-radius, implying sharp corners or system default.

## Layout

The page adheres to a max-width contained layout with content centered, though specific sections may break this for full-bleed effects. The hero section features a prominent headline centered over a nuanced background of dark neutrals and pixelated violet accents. Section rhythm is primarily defined by consistent vertical spacing, creating breathing room, and alternating background colors (Canvas White vs. Lava Cloud) to delineate content blocks. Content arrangement often utilizes two-column layouts with text-left/image-right or centered stacks. Navigation is a sticky top bar, providing persistent access to key links.

## Imagery

Imagery treatment is primarily focused on abstract, pixelated backgrounds and product UI screenshots. The pixelated patterns, often in shades of violet or dark grays (Pixel Purple #c8ccf3), provide a technical, data-driven atmosphere. Product screenshots feature clean, contained UI elements on dark backgrounds, often with simple, illustrative icons. Icons are predominantly outlined, thin stroke weight, maintaining the minimalist and precise aesthetic. The visual density is balanced, allowing prominent text to lead, with imagery serving as supportive illustration or product showcase rather than decorative flourish for its own sake.

## Dos & Donts

### Do

- Use Electric Violet (#5e4cff) exclusively for primary calls-to-action and critical interactive highlights.
- Apply Britti Sans Trial Semibold for all primary headings, using the specified letter-spacing for each size.
- Maintain a clear visual hierarchy by limiting saturated colors to Electric Violet and its softer accent tints.
- Utilize Canvas White (#ffffff) as the dominant page background and primary card surfaces for visual breathability.
- Implement 8px border-radius for all buttons and 12px for cards to maintain consistent soft-edged elements.
- Differentiate UI surfaces using Charcoal Slate (#272835) for darker elements and Lava Cloud (#eceff3) for subtle background variations.
- Employ IBM Plex Mono for all code blocks or technical data displays to ensure typographic distinction and readability.

### Don't

- Do not introduce new saturated hues outside of the established Electric Violet and its related accent shades.
- Avoid using drop shadows on functional UI elements unless it's a card from the 'Elevated Feature Card' component.
- Do not vary letter-spacing for Britti Sans headlines from the specified -0.025em for large sizes and -0.020em for smaller sizes.
- Never use generic system fonts for branding or primary content where Britti Sans or Inter are specified.
- Do not use Electric Violet (#5e4cff) for body text or large blocks of content; it is reserved for action and accent.
- Avoid excessive use of very dark backgrounds; the system leans heavily on light mode with dark neutral accents.
- Do not apply large, rounded corners (e.g., 20px) to anything other than the specific Elevated Feature Card and pill-shaped elements.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #272835
background: #ffffff
border: #b4acff
accent: #c8ccf3
primary action: #5e4cff (filled action)

Example Component Prompts:
Create a hero section: Canvas White (#ffffff) background. Headline 'Agentic BI. Analytics at the speed of code' using Britti Sans Trial Semibold 76px, Charcoal Slate (#272835), letter-spacing -0.025em. Subtext 'The only open source AI-native BI platform...' using Inter 18px, Steel Gray (#666d80). Primary call-to-action button 'Start for free' with Electric Violet (#5e4cff) background, white text, 8px radius, 12px vertical, 20px horizontal padding.

Create a Simple Information Card: Canvas White (#ffffff) background, 12px border-radius, subtle 1px border shadow (rgba(39, 40, 53, 0.1) 0px 0px 0px 1px). Padding 28px top/bottom, 22px horizontal. Headline 'Documentation' using Britti Sans Medium 24px, Midnight Ink (#1a1b25). Body text 'Explore our API guides...' using Inter 16px, Deep Indigo (#36394a).

Create a Dark Input Field: Deep Indigo (#36394a) background, with a 1px solid white border, no border-radius. Placeholder text 'Enter your email' should be Cloud Gray (#818898) using Inter 16px. Text input should be Canvas White (#ffffff).

---
_Source: https://styles.refero.design/style/d0f65d12-a8e6-4631-99f7-bb7cdcd5b6c5_
