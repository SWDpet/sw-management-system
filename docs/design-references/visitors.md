# Visitors — Design Reference

> Analytical canvas vibrant spectrum

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://visitors.now](https://visitors.now) |
| Refero page | [https://styles.refero.design/style/e7876363-181a-44a9-9e5c-2255cf98aea5](https://styles.refero.design/style/e7876363-181a-44a9-9e5c-2255cf98aea5) |
| Theme | light |
| Industry | saas |

## Overview

The Visitors design system employs a crisp, analytical aesthetic with a playful edge. It balances substantial negative space and achromatic surfaces with a vibrant, gradient-infused purple as its primary brand color, punctuated by a suite of vivid secondary accents. Typography is compact and precise, maintaining readability while maximizing information density. Interactive elements often manifest as ghost buttons or subtle, rounded containers, giving a lightweight feel until the accent color 'switches on' for primary actions. The pervasive use of rounded corners softens the otherwise direct, functional presentation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button backgrounds |
| Slate Ink | `#181925` | neutral | Primary text, prominent headings, strong borders |
| Medium Gray | `#666666` | neutral | Body text, secondary headings, default icon color |
| Muted Gray | `#999999` | neutral | Muted text, helper text, inactive navigation items, dividers |
| Light Gray | `#e8e8e8` | neutral | Subtle borders, table dividers, ghost button borders |
| Whisper Purple | `#dad9fc` | brand | Subtle highlights, decorative borders, background accents |
| Radiant Violet | `#918df6` | brand | Primary action backgrounds, interactive indicators, brand highlight color for icons and accent borders |
| Electric Blue | `#2c78fc` | accent | Violet accent for outlined action borders, linked labels, and lightweight interactive emphasis; Decorative background gradients, hero element backgrounds |
| Success Green | `#33c758` | accent | Green outline accent for tags, dividers, and focused UI edges. Use as a supporting accent, not as a status color |
| Warning Yellow | `#ffa600` | accent | Yellow decorative accent for icons, marks, and small graphic details. Use as a supporting accent, not as a status color |
| Accent Pink | `#d6409f` | accent | Decorative icons, secondary brand accents |
| Pale Mint | `#def6e4` | neutral | Soft section background, alternate surface, and quiet card fill |
| Deep Purple | `#9580ff` | brand | Accent buttons, interactive elements (darker shade of brand purple) |
| Vivid Orange | `#ff3e00` | accent | Decorative SVG fills, minor accents (used in product screenshots) |

## Typography

### OpenRunde

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| sizes | 12px, 13px, 14px, 16px, 18px, 20px, 24px, 36px, 48px, 60px |
| lineHeight | 1.00, 1.11, 1.13, 1.17, 1.22, 1.33, 1.40, 1.43, 1.50, 1.56 |
| letterSpacing | -0.0500em, -0.0270em, -0.0250em, -0.0230em, -0.0200em, -0.0180em, -0.0170em, -0.0160em, -0.0130em, -0.0090em, -0.0070em |
| substitute | system-ui, sans-serif |
| role | Primary typeface for all UI elements, headings, and body text. Its compact metrics and varied letter spacing across sizes reinforce the analytical yet modern feel of the interface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.56 | -0.05 |
| body | 14 |  | 1.43 | -0.025 |
| heading-sm | 18 |  | 1.33 | -0.02 |
| heading | 20 |  | 1.22 | -0.018 |
| heading-lg | 24 |  | 1.17 | -0.017 |
| display | 48 |  | 1.13 | -0.013 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 9999px |
| cards | 16px |
| large | 24px |
| buttons | 1.67772e+07px |
| default | 8px |

- **elementGap** — 16px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Base page background |
| Subtle Accent | `#fafafa` | 1 | Slightly off-white sections, card backgrounds, or backgrounds for neutral elements |
| Light Card | `#00000008` | 2 | Default card backgrounds with minimal visual weight |
| Pale Mint Overlay | `#def6e4` | 3 | Highlight or success-related background washes for specific content blocks |

## Components

### Navigation Link

**Role:** Menu items, simple textual links

Typography: OpenRunde weight 400 at 16px, color: #181925. No background, no border, no padding. Hover state indicated by color change or underline.

### Ghost Button

**Role:** Secondary actions, tertiary navigation items

Background: transparent (rgba(0,0,0,0)). Text color: #181925. No border. Padding: 0px vertical, 12px horizontal. Radius: 0px. Text is OpenRunde 16px weight 400.

### Pill Ghost Button

**Role:** Subtle filtering, small secondary actions

Background: transparent (rgba(0,0,0,0)). Text color: #666666. Border: none. Padding: 0px. Radius: 1.67772e+07px (effectively full pill). Text is OpenRunde 14px weight 400.

### Primary Action Button

**Role:** Main calls to action, clear user intent

Background: #918df6. Text color: #ffffff. Radius: 8px to 1.67772e+07px. Padding varies (e.g., 6px vertical, 10-12px horizontal). Text is OpenRunde 16px weight 500. Box shadow: rgba(0, 0, 0, 0.08) 0px 1px 1px 1px, rgba(0, 0, 0, 0.06) 0px 0px 0px 0.5px. Example: 'Start 14 day free trial'.

### Accent Pill Button

**Role:** Prominent but compact actions like 'Register'

Background: #9580ff. Text color: #ffffff. Radius: 1.67772e+07px. Padding: 0px vertical, 6px horizontal. Text is OpenRunde 16px weight 400. Example: 'Register' in nav.

### Subtle Text Button

**Role:** Small, informative labels, often with an icon

Background: #ffffff. Text color: #181925. Radius: 1.67772e+07px. Padding: 0px vertical, 6-10px horizontal. Text is OpenRunde 12px weight 400. Example: 'new We hit $1K MRR'.

### Feature Card

**Role:** Displaying key features or content blocks

Background: rgba(0,0,0,0.03) or #fafafa. Radius: 16px. No box shadow. Padding: 0px initially, but inner content typically uses 20-24px. Features a soft, nearly invisible background.

### Elevated Content Card

**Role:** Prominent information display, often with more substantial content

Background: #ffffff or #fafafa. Radius: 24px. Box shadow: none for content isolation. Padding: 64px vertical, 32px horizontal. This card uses more internal space.

## Layout

The page primarily employs a max-width contained layout, approximately 1200px, horizontally centered. The hero section features a centered headline and description over a full-bleed gradient background that extends across the viewport. Below the hero, content typically alternates between full-width banner-like sections and narrower, contained blocks. Feature sections often use a multi-column grid (e.g., 3-column) for concise information. Vertical rhythm is established by consistent section gaps of 64px, with components and cards featuring internal padding. Navigation is a sticky top bar with a left-aligned brand logo, right-aligned navigation links, and accent-colored 'Login'/'Register' buttons, indicating a clear, un-cluttered approach to utility.

## Imagery

The imagery style is a mix of product screenshots, abstract gradient backgrounds, and line art icons. Product screenshots are clean and direct, showcasing UI elements without heavy stylization. Abstract graphics are characterized by smooth, organic gradients, primarily in blues and purples, providing decorative atmosphere. Icons are minimalist, outlined, and often monochromatic or subtly tinted with accent colors like #33c758 (green), #ffa600 (yellow), or #d6409f (pink), serving as explanatory content or functional indicators. They possess a moderate stroke weight. The visual density is balanced, with imagery serving to break up text-heavy sections or highlight key data, rather than being overwhelming.

## Dos & Donts

### Do

- Use Radiant Violet (#918df6) exclusively for primary action backgrounds and brand-aligned interactive elements; avoid using it for decorative purposes.
- Apply OpenRunde with specific letter-spacing values: -0.013em for 48px headlines, scaling down to -0.05em for 12px caption text.
- Maintain comfortable visual distance between elements using a base unit of 4px; common `elementGap` is 16px, and `cardPadding` is 16px.
- Utilize 1.67772e+07px (effectively 9999px) border-radius for all pill-shaped buttons and tags to create a consistent soft, rounded edge.
- Implement the soft box shadow `rgba(0, 0, 0, 0.08) 0px 1px 1px 1px, rgba(0, 0, 0, 0.06) 0px 0px 0px 0.5px` sparingly for interactive buttons.
- Use Slate Ink (#181925) for all high-contrast, structural text, including main headings and primary body content, ensuring readability on light backgrounds.
- Separate sections with a substantial 64px vertical `sectionGap` unless content dictates a continuous flow.

### Don't

- Do not use saturated colors for large background areas or extensive text; reserve them for accents and actionable components.
- Avoid sharp corners; ensure all interactive elements and content containers have a minimum border-radius of 8px.
- Do not introduce new typefaces; OpenRunde is the sole family for all textual content.
- Do not use heavy, opaque shadows; leverage the prescribed subtle shadows for depth or rely on background color changes for hierarchy.
- Do not clutter layouts; prioritize negative space and clear visual hierarchy on a bright, minimalist canvas.
- Do not apply `letter-spacing: normal` to display or large heading sizes; always use the specified negative tracking for OpenRunde.
- Do not use multiple instances of distinct purple hues for primary actions; Radiant Violet (#918df6) is the definitive choice for filled buttons.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #181925
background: #ffffff
border: #e8e8e8
accent: #918df6
primary action: #918df6 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #918df6 background, #181925 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a Feature Card: background rgba(0,0,0,0.03), 16px radius, content padding 20px. Inside, place a heading 'Lightweight script.' using OpenRunde 24px weight 600, #181925, letter-spacing -0.017em. Below, body text 'Under 1KB. Won't hurt your page speed or Core Web Vitals.' using OpenRunde 16px weight 400, #666666, letter-spacing -0.023em.
3. Create a Navigation Bar: background #ffffff. Left-aligned logo. Right-aligned ghost buttons for 'Features', 'Pricing', 'Blog', 'Docs' using OpenRunde 16px weight 400, #181925, padding 0px vertical, 12px horizontal. Followed by a 'Login' Ghost Button (same style) and a 'Register' Accent Pill Button with background #9580ff, text #ffffff, 9999px radius, OpenRunde 16px weight 400.
4. Create a Dashboard Tab Bar: background transparent. Tabs for 'Dashboard', 'Profiles', 'Funnels', 'Performance', 'Realtime'. Active tab has a bottom border 2px solid #918df6, inactive tabs have #e8e8e8 bottom border. Text for tabs is OpenRunde 16px weight 500, active #181925, inactive #666666.

---
_Source: https://styles.refero.design/style/e7876363-181a-44a9-9e5c-2255cf98aea5_
