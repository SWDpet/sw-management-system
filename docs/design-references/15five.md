# 15five — Design Reference

> Violet-tinged sunrise on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.15five.com](https://www.15five.com) |
| Refero page | [https://styles.refero.design/style/88a00b3e-1c1e-49d3-ae0f-9fbc1f35ad99](https://styles.refero.design/style/88a00b3e-1c1e-49d3-ae0f-9fbc1f35ad99) |
| Theme | light |
| Industry | saas |

## Overview

15Five employs a vibrant, confident design language with clean white canvases, softened cards, and a striking violet-to-pink gradient. Typography is a blend of bold, impactful headlines and accessible body text. The system uses a specific 'hero' violet as its primary accent, contrasted with touches of warm orange and pink, creating an energetic yet grounded feel. Components are lightweight with generous rounded corners, emphasizing an approachable and modern presentation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button backgrounds, default text for dark elements |
| Warm Mist | `#fff2e8` | neutral | Subtle background for secondary cards and elevated surface sections, providing visual separation |
| Violet Shadow | `#5e5777` | brand | Muted text, secondary button text, decorative borders, helper text |
| Deep Violet | `#2f115b` | brand | Primary text, bold accents, outlined button borders, key interactive elements |
| Midnight Indigo | `#1a0d3f` | brand | Prominent headings and body text, providing high contrast on light backgrounds |
| Accent Violet | `#3b1572` | accent | Primary action button background fill, active states for interactive elements — a rich, vibrant brand identifier; Prominent background for hero sections and testimonials, establishing a vibrant brand tone |
| Pink Sunset | `#ffdacf` | accent | Highlight backgrounds, secondary action buttons, gradient accents |
| Fiery Orange | `#ff4b11` | accent | Outlined button borders, subtle highlights and decorative accents |

## Typography

### Manrope

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 22px, 24px |
| lineHeight | 1.00, 1.33, 1.43, 1.44, 1.50, 1.63, 1.64, 2.57 |
| letterSpacing | 0.0020em (at 14px), 0.3750em (for specific uppercase elements) |
| substitute | system-ui |
| role | Primary sans-serif for all body text, navigation, buttons, and helper content. Its varied weights offer flexibility for hierarchy, maintaining legibility and a contemporary feel across the interface. |

### DM Serif Display

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 24px, 40px, 52px, 66px |
| lineHeight | 1.15, 1.20, 1.25, 1.33 |
| letterSpacing | normal |
| substitute | serif |
| role | Distinguished serif typeface for prominent headings, creating a sophisticated and editorial impact with its elegant curves and generous scale. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.43 | 0.002 |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.63 |  |
| heading-sm | 22 |  | 1.64 |  |
| heading | 24 |  | 1.33 |  |
| heading-lg | 40 |  | 1.25 |  |
| display | 66 |  | 1.15 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| buttons | 68px |
| general | 10px |

- **elementGap** — 20px
- **sectionGap** — 24px
- **cardPadding** — 20px
- **pageMaxWidth** — 1363px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background, base layer. |
| Warm Mist | `#fff2e8` | 1 | Secondary background for sections, subtle elevation for content blocks. |
| Pink Sunset | `#ffdacf` | 2 | Highlight background for interactive elements or featured content like testimonials. |
| Accent Violet | `#3b1572` | 3 | Background for primary action buttons, signaling interaction points. |
| Hero Gradient | `#3b1572` | 4 | Prominent stylistic background for hero sections and testimonial blocks, providing visual depth and brand emphasis. |

## Components

### Primary Filled Button

**Role:** Call to action.

Filled with Accent Violet (#3b1572), white text, 68px border-radius, 16px padding on top/bottom, 40px on left/right. Commands attention for key actions.

### Light Outlined Button

**Role:** Secondary action or ghost button.

White background, Deep Violet (#2f115b) text, 68px border-radius, 16px padding on top/bottom, 40px on left/right. Provides a subtle interactive element that doesn't compete with primary actions.

### Subtle Nav Link

**Role:** Navigation or in-page links.

Transparent background, Violet Shadow (#5e5777) text, 16px border-radius, 20px padding. Minimally styled for navigation, with a soft border-radius that hints at interactivity.

### Text Only Link

**Role:** Inline textual links.

Transparent background, Midnight Indigo (#1a0d3f) text, no border-radius or padding. Used for simple text links within body content or lists.

### Information Card (Transparent)

**Role:** Content container for data or features.

Transparent background, 0px border-radius, 0px padding on top/bottom, 20px on left/right. Used for presenting information without strong visual boundaries, allowing content to breathe.

### Data Insight Card

**Role:** Displays data metrics or key insights.

White background, 12px border-radius, no padding. A clean, contained surface for showcasing performance metrics or brief information.

### Highlighted Testimonial Card

**Role:** Prominently features customer testimonials.

Pink Sunset (#ffdacf) background, 16px border-radius, with decorative gradient accents. No internal padding defined, relies on content spacing.

### Shadowed Link Box

**Role:** Interactive link blocks with elevation.

Features a white background, 10px border-radius, and a soft shadow (rgba(0, 0, 0, 0.1) 0px 12px 30px 0px). Used for clickable elements that require a distinct elevated presence.

## Layout

The page adheres to a max-width of 1363px, centered for most content, but the initial hero section appears full-bleed with a gradient background. The hero features a large, centered headline and subtext, followed by two prominent call-to-action buttons. Sections below alternate between a white canvas and a subtle Warm Mist (#fff2e8) background, often employing a two-column layout with text on one side and product UI screenshots or data visualizations on the other. A three or four-column card grid is used for feature showcases. Vertical spacing is consistent and comfortable, with a section gap of 24px fostering readability. The navigation is a sticky top bar with a 'Book a Demo' button prominently featured.

## Imagery

The site predominantly uses clean, product-focused illustrations and data visualizations. Illustrations are often abstract, geometric, and brand-colored, featuring soft gradients. Photography is largely absent, replaced by UI screenshots of the product. Icons are filled, with a consistent stroke weight, and mostly monochromatic or brand-colored. Imagery serves an explanatory and illustrative role, showcasing product features and data outputs rather than decorative atmosphere, contributing to a high-density, text-supported visual experience.

## Dos & Donts

### Do

- Use Midnight Indigo (#1a0d3f) for all main headings and primary body text to ensure high contrast and readability.
- Apply Canvas White (#ffffff) as the default background for all primary page content sections and cards.
- Implement the 68px border-radius for all button elements, including both filled and outlined variants, to maintain a consistent soft, approachable shape.
- Utilize Manrope for all functional typography (buttons, nav, body text) and DM Serif Display for major page headings to leverage their distinct roles.
- Incorporate the Hero Gradient (linear-gradient(90deg, #3b1572 0%, #1a0d3f 100%)) as a background for high-impact sections like hero banners and testimonials.
- Maintain a comfortable density with an element gap of 20px and a section gap of 24px between major content blocks.
- Use Accent Violet (#3b1572) as the primary fill color for active buttons and key interactive elements, drawing immediate attention.

### Don't

- Avoid using multiple border styles or weights for interactive elements; stick to 1px solid for subtle hints and no border for purely textual links.
- Do not introduce new typefaces; rely solely on Manrope and DM Serif Display to preserve typographic consistency.
- Resist using harsh or high-contrast shadows; only apply the soft, diffused shadow (rgba(0, 0, 0, 0.1) 0px 12px 30px 0px) for specific elevated components like link cards.
- Do not deviate from the established border radii; avoid sharp corners or excessively small radii as they contradict the system's approachable aesthetic.
- Refrain from using saturated colors other than Accent Violet, Pink Sunset, or Fiery Orange for UI elements; other vibrant hues are reserved for data visualization or illustrations.
- Do not expand the primary color palette with additional blues, greens, or reds unless they are specifically for semantic status indications (success, error).
- Avoid arbitrary letter-spacing adjustments; adhere to the defined Manrope letter-spacing values or normal for DM Serif Display.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #1a0d3f
- background: #ffffff
- border: #5e5777
- accent: #3b1572
- primary action: #ffdacf (filled action)

Example Component Prompts:
- Create a Primary Action Button: #ffdacf background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Create a data insight card: White background, 12px border-radius, showing a statistic with heading at 24px Manrope weight 700, #1a0d3f, and body text at 16px Manrope weight 400, #5e5777.
- Create a testimonial component: Pink Sunset (#ffdacf) background, 16px border-radius. Quote text at 18px DM Serif Display weight 400, #1a0d3f. A Light Outlined Button below with text 'Read full story'.

### Motion Philosophy

The system utilizes expressive motion, primarily with 0.4s and 0.3s durations, and 'ease' timing functions. Transitions are common on background, transform, padding, and box-shadow properties, creating fluid and responsive interactions without being overly animated. Subtle changes in state are preferred over jarring, immediate shifts.

---
_Source: https://styles.refero.design/style/88a00b3e-1c1e-49d3-ae0f-9fbc1f35ad99_
