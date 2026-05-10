# Fal — Design Reference

> Pixelated digital playground

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://fal.ai](https://fal.ai) |
| Refero page | [https://styles.refero.design/style/14cc44e6-41bf-4178-b834-fc61bfeed4ae](https://styles.refero.design/style/14cc44e6-41bf-4178-b834-fc61bfeed4ae) |
| Theme | light |
| Industry | ai |

## Overview

Fal.ai employs a vibrant, digital-first aesthetic, evoking a playful yet powerful generative AI platform. High-contrast typography is layered over a base of clean white and subtle gray surfaces, occasionally interrupted by striking graphic patterns and colorful background washes. The visual system balances strong, angular geometric shapes with pixel-art style details, creating a distinctive blend of retro-tech and contemporary design. Accent colors are used sparingly for interactive elements and abstract background fills, maintaining a focus on content.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#1b1b1d` | neutral | Primary text, deep surface elements, button text |
| Carbon | `#000000` | neutral | Strongest text, critical information, contrast elements |
| Cloud White | `#ffffff` | neutral | Component backgrounds, card surfaces, ghost button backgrounds |
| Stone Gray | `#383a42` | neutral | Body text, secondary text |
| Soft Mist | `#e5e7eb` | neutral | Page backgrounds, dividers, borders |
| Silver Dust | `#f4f4f5` | neutral | Subtle background shifts, card surfaces |
| Deep Plum | `#252527` | neutral | Primary action button background, dark surface accent |
| Sky Aqua | `#99ecff` | accent | Hero section background, footer background, decorative fills |
| Vivid Violet | `#4a17b0` | accent | Decorative graphic fills, card backgrounds |
| Lime Spritz | `#f1ffd2` | accent | Decorative graphic fills, card backgrounds |
| Royal Purple | `#6120ee` | accent | Link text, active state indicator |
| Lavender Haze | `#d0b7f9` | accent | Subtle card backgrounds, decorative fills |
| Rose Petal | `#ffddfa` | accent | Subtle card backgrounds, decorative fills |

## Typography

### focal

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 10px, 12px, 14px, 16px, 18px, 24px, 30px, 36px, 40px, 48px, 60px, 80px, 120px |
| lineHeight | 0.83, 0.88, 0.90, 1.00, 1.11, 1.20, 1.33, 1.40, 1.43, 1.50, 1.56, 1.60 |
| letterSpacing | -0.0400em, -0.0250em, -0.0200em |
| substitute | Inter |
| role | Primary typeface for all headings, body text, and UI elements. Tight letter spacing is characteristic for larger sizes; it creates a modern, condensed feel. |

### Chivo Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.14, 1.43, 1.50 |
| substitute | Space Mono |
| role | Monospace typeface for code snippets and technical labels, providing a distinct developer-centric tone. |

### publicSansRounded

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| sizes | 14px, 16px, 24px |
| lineHeight | 1.00, 1.43, 1.50 |
| letterSpacing | -0.0200em, -0.0100em |
| fontFeatureSettings | 'ss01' on |
| substitute | Public Sans |
| role | Used for specific link and button text, offering a slightly softer, rounded alternative for interactive elements. The stylistic set 'ss01' provides unique character forms. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.6 |  |
| body | 14 |  | 1.5 |  |
| body-lg | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.4 |  |
| heading-sm | 24 |  | 1.33 |  |
| heading | 30 |  | 1.2 |  |
| heading-lg | 36 |  | 1.11 | -0.72 |
| display-sm | 40 |  | 1 | -0.8 |
| display | 48 |  | 0.9 | -1.2 |
| display-lg | 60 |  | 0.88 | -1.5 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| buttons | 4px |
| default | 4px |

- **elementGap** — 16px
- **sectionGap** — 113px
- **cardPadding** — 16px
- **pageMaxWidth** — 1440px

## Components

### Primary Filled Button

**Role:** Call to action button

Solid Deep Plum background (#252527) with Cloud White text (#ffffff). Features a 4px border-radius and generous 0px vertical, 16px horizontal padding. Text uses publicSansRounded font, typically 16px or 14px.

### Ghost Button

**Role:** Secondary action button

Transparent background with Midnight Ink text (#1b1b1d) and a 1px Soft Mist border (#e5e7eb). 4px border-radius, 0px vertical, 16px horizontal padding. For less prominent actions where a fill would be too heavy. Text uses publicSansRounded font.

### Standard Card

**Role:** Content container

Cloud White background (#ffffff) with a 4px border-radius. Features 32px padding on all sides. No shadow, relying on background contrast for separation.

### Accent Card - Lime Spritz

**Role:** Highlighted content container

Lime Spritz background (#f1ffd2) with a 12px border-radius. Features 16px padding on all sides. Used for sections requiring visual emphasis or thematic alignment.

### Accent Card - Lavender Haze

**Role:** Highlighted content container

Lavender Haze background (#d0b7f9) with a 12px border-radius. Features 16px padding on all sides. Similar to Lime Spritz, provides visual distinction for content blocks.

### Testimonial Card

**Role:** Social proof container

Silver Dust background (#f4f4f5) with an 8px border-radius. Features 24px padding on all sides. Accommodates slightly denser content than larger cards.

### Navigation Link

**Role:** Header and footer navigation items

Midnight Ink text (#1b1b1d) at 16px focal font weight 400. Hover state likely involves the Royal Purple accent color.

### Small Text Button

**Role:** Compact action button

Cloud White background (#ffffff) with Midnight Ink text (#1b1b1d). 4px border-radius, padding 0px vertical, 16px horizontal. Similar to Primary Filled Button but with lighter styling for secondary use.

## Layout

The page maintains a max-width of 1440px, centered on the screen, creating a structured and contained feel. The hero section is a full-bleed vibrant canvas, featuring a prominent centered headline over dynamic, pixelated background graphics. Vertical rhythm between sections is established with a significant section gap, sometimes signaled by alternating background colors (e.g., Sky Aqua hero transitioning to a Cloud White main content area). Content often uses two-column layouts, particularly for text-left/image-right or text-right/image-left arrangements, and a three-column card grid for feature showcases. The page is moderately spacious, with generous padding within components and between sections. Navigation is a consistent top bar with primary links and contained action buttons.

## Imagery

The visual language combines abstract, pixelated geometric graphics with focused product display. The abstract graphics, often in vivid accent colors like Sky Aqua, Vivid Violet, and Lime Spritz, are used as atmospheric background elements or decorative overlays. Product imagery consists of tight crops of AI-generated media (video frames, 3D models) contained within distinct card surfaces, showcasing the output of the platform. Iconography appears to be outlined with a consistent stroke weight, emphasizing clarity and a lightweight feel. Overall density is balanced, with imagery serving both decorative and explanatory roles without overwhelming the text-dominant interface sections.

## Dos & Donts

### Do

- Prioritize Cloud White (#ffffff) and Soft Mist (#e5e7eb) as primary background surfaces, reserving accent colors for graphic elements and thematic sections.
- Use focal for all primary text content and headings, leveraging its various weights and condensed letter spacing for visual impact at larger sizes.
- Apply a 4px border-radius to all interactive elements like buttons and input fields for a consistent subtle rounding.
- Implement Deep Plum (#252527) as the background for primary action buttons, ensuring a high contrast with Cloud White text (#ffffff).
- Maintain a clear visual hierarchy with element gaps, using 16px as a default for spacing between interactive elements and body text blocks.
- Incorporate accent colors like Sky Aqua (#99ecff), Vivid Violet (#4a17b0), and Lime Spritz (#f1ffd2) primarily as background washes or decorative fills, not for extensive UI components.
- Ensure all interactive text, such as links and button text, is in Midnight Ink (#1b1b1d) or Cloud White (#ffffff) against contrasting backgrounds, with Royal Purple (#6120ee) for active links.

### Don't

- Avoid using drop shadows for elevation; rely on clear background color changes and borders for surface distinction.
- Do not introduce new typefaces outside of focal, Chivo Mono, and publicSansRounded to maintain typographic consistency.
- Resist using highly saturated accent colors for extensive blocks of body text, as they are reserved for decorative elements.
- Do not deviate from the established 4px, 8px, and 12px border-radii for components; arbitrary rounding breaks pattern.
- Avoid generic, full-width content blocks. All content should be constrained by the 1440px page max-width, centrally aligned.
- Do not use dark backgrounds for entire page sections unless for explicit hero or footer areas, as the theme is predominantly light.
- Do not treat every decorative graphic fill color as a UI token; focus on the consistent palette for interactive and functional elements.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #1b1b1d
background: #e5e7eb
border: #e5e7eb
accent: #99ecff
primary action: #252527 (filled action)

Example Component Prompts:
1. Create a Primary Filled Button: background #252527, text #ffffff, focal font weight 600, 16px, 4px radius, 0px vertical and 16px horizontal padding.
2. Design a Standard Card: background #ffffff, 4px radius, 32px padding on all sides. Body text #383a42, focal font weight 400, 14px.
3. Implement a Hero Section Headline: 'Generative media platform for developers.' using focal font, weight 700, size 80px, color #000000, line-height 0.83, letter-spacing -2px. Background of #99ecff with abstract pixelated graphics.
4. Create a Testimonial Card for 'Perplexity': background #f4f4f5, 8px radius, 24px padding. Quote text #383a42, focal font weight 400, 16px. Author name #1b1b1d, focal font weight 600, 14px.

---
_Source: https://styles.refero.design/style/14cc44e6-41bf-4178-b834-fc61bfeed4ae_
