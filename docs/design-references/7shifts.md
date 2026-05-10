# 7shifts — Design Reference

> Crisp Utility, Rounded Edges

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.7shifts.com](https://www.7shifts.com) |
| Refero page | [https://styles.refero.design/style/736830b5-90b1-47b0-99dd-d79454a0d22a](https://styles.refero.design/style/736830b5-90b1-47b0-99dd-d79454a0d22a) |
| Theme | light |
| Industry | saas |

## Overview

7shifts employs a functional and approachable design system, combining a crisp white and dark gray achromatic base with a singular vivid blue accent for key interactions. Typography provides clear hierarchy, with a custom `Regular` font family for all content up to large headlines. Components are lightweight and often borderless, emphasizing content over chrome, while subtle rounding creates a friendly, modern feel across cards and buttons. The system balances directness with a degree of visual softness.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, dominant interactive elements text against blues |
| Blueprint Blue | `#4570ff` | brand | Primary Call-to-Action buttons, active states, decorative accents — a vivid, energetic blue that highlights key actions |
| Ink Black | `#000000` | neutral | Primary text, prominent headlines, dark section backgrounds, card backgrounds |
| Divider Gray | `#e5e7eb` | neutral | Hairline borders, subtle separators, inactive elements |
| Whisper White | `#f1f0ec` | neutral | Subtle background for secondary sections, card backgrounds within white areas |
| Muted Text Gray | `#6e6d6c` | neutral | Secondary body text, helper text, captions — provides contrast without competing with main headlines |
| Soft Orange Highlight | `#ff6808` | accent | Outlined button borders (e.g. navigation tabs), occasional decorative fills, text underlines – a secondary accent for subtle emphasis |
| Spring Green | `#c6ff94` | accent | Green text accent for links, tags, and emphasized short phrases |
| Lavender Mist | `#ebdcff` | accent | Decorative background of some cards, subtle highlight for secondary content blocks |
| Sky Blue Light | `#d6e0ff` | accent | Decorative background of some cards, subtle highlight for secondary content blocks |
| Midnight Forest | `#244f47` | accent | Dark card backgrounds (usually paired with light text) |
| Deep Plum | `#c293f1` | accent | Dark card backgrounds (usually paired with light text) |
| Rich Indigo | `#193f78` | accent | Dark card backgrounds (usually paired with light text) |

## Typography

### Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 11px, 12px, 14px, 16px, 18px, 36px, 64px |
| lineHeight | 1.00, 1.11, 1.14, 1.20, 1.30, 1.33, 1.38, 1.40, 1.50, 1.56 |
| letterSpacing | -0.0500em at 64px, -0.0300em at 36px, -0.0250em at 18px, -0.0220em at 16px, -0.0200em at 14px |
| substitute | Inter |
| role | Body text, navigation items, descriptive content, and primary headlines — a versatile, compact sans-serif that balances readability with a contemporary feel across all textual elements. |

### Medium

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 11px, 12px, 14px, 16px, 18px, 24px, 28px, 36px, 40px, 48px, 64px, 120px, 150px |
| lineHeight | 0.75, 0.90, 0.93, 1.00, 1.10, 1.11, 1.33, 1.40, 1.43, 1.50 |
| letterSpacing | -0.0300em at 150px, -0.0200em at 48px, -0.0180em at 24px, -0.0120em at 18px |
| substitute | Inter |
| role | Call-to-action text, sub-headings, and emphasized content — providing subtle weight distinction for prominence without heavy boldness. |

### Nanum Pen Script

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 28px, 30px |
| lineHeight | 1.00, 1.50 |
| letterSpacing | -0.0300em at 30px |
| substitute | Kalam |
| role | Decorative headlines and accent text — a handwritten script that adds a personal, informal touch to selected titles. |

### Bold

| Key | Value |
| --- | --- |
| weight | 700 |
| weights | 700 |
| sizes | 14px |
| lineHeight | 1.14 |
| letterSpacing | 0.0250em at 14px |
| substitute | Inter |
| role | Strong emphasis on small-form headings or labels — used sparingly for high impact within compact UI elements. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 | 0 |
| body | 14 |  | 1.5 | -0.28 |
| heading-sm | 18 |  | 1.33 | -0.45 |
| heading | 36 |  | 1.33 | -1.08 |
| heading-lg | 48 |  | 1.1 | -0.96 |
| display | 64 |  | 1.11 | -3.2 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 20px |
| icons | 8px |
| buttons | 9999px |
| general | 12px |
| headerTabs | 16px |
| largeElements | 40px |
| emphasizedCard | 24px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 28px
- **pageMaxWidth** — 

## Components

### Primary Call-to-Action Button

**Role:** High-priority action trigger

Filled with Blueprint Blue (#4570ff), Canvas White (#ffffff) text, and a 9999px (pill-shaped) border-radius. Padding of 0px vertical and 16px horizontal. Uses 'Medium' font, 500 weight.

### Ghost Navigation Button

**Role:** Secondary navigation and internal links within headers.

Transparent background (rgba(0,0,0,0)), Ink Black (#000000) text, 20px border-radius, no vertical/horizontal padding for text-only appearance. Uses 'Regular' font.

### White Pill Button

**Role:** General secondary action or tag.

Canvas White (#ffffff) background, Ink Black (#000000) text, Divider Gray (#e5e7eb) border, 9999px border-radius. Padding 4px vertical, 10px horizontal. Uses 'Regular' font.

### Whisper White Tag Button

**Role:** Informational tags or filter options.

Whisper White (#f1f0ec) background, Ink Black (#000000) text, Divider Gray (#e5e7eb) border, 9999px border-radius. Padding 0px vertical, 16px horizontal. Uses 'Regular' font.

### Standard Card

**Role:** Content grouping, feature display, informational blocks.

Canvas White (#ffffff) background, 20px border-radius, no shadow. Padding 28px on all sides. Content is self-contained.

### Dark Overlay Card

**Role:** Elevated content overlay or feature block on dark backgrounds.

Ink Black (#000000) background, 20px border-radius (rounded bottom and left corners on some variants), no shadow. Padding 40px on all sides. Used often for featured content or comparisons.

### Full-Width Dark Feature Panel

**Role:** Hero section content, prominent feature showcase.

Ink Black (#000000) background, 40px border-radius (bottom corners only on some variants), no shadow. Top padding 60px; horizontal and bottom padding 0px to allow content to bleed. Often full-width background.

### Frosted Glass Card

**Role:** Background for overlaid content, subtle visual interest.

Semitransparent black (rgba(0,0,0,0.5)) background, 20px border-radius, no shadow. Padding 40px. Used for content that sits visually on top of imagery, allowing background to show through.

## Layout

The page primarily uses a contained layout model, with content centered within a maximum width (implied, not explicitly stated as it's null, but visually constrained). The hero section typically features a large, centered headline over a white background, occasionally transitioning to a full-bleed dark feature panel at the bottom of the hero area. Section rhythm is driven by alternating background colors, moving between Canvas White (#ffffff) and Whisper White (#f1f0ec), punctuated by occasional full-bleed dark sections (Ink Black #000000) or colorful accent sections (e.g. Spring Green or Lavender Mist cards). Content is often arranged in symmetrical stacks or two-column text-left/image-right patterns. Card grids are utilized for features, maintaining consistent spacing. The navigation is a sticky top bar, containing text links, a primary CTA button, and a secondary login button.

## Imagery

The imagery style is a mix of product screenshots, often presented within mock UI frames, and abstract flat graphic illustrations for decorative purposes. Product screenshots are typically sharp, clear, and focused on showing the interface without distracting backgrounds. Illustrations are primarily flat, organic shapes, relying on brand colors (like Spring Green, Lavender Mist, and Sky Blue Light) to add a modern, soft visual texture. Icons are filled, with a consistent stroke weight where outlines exist, and are monochromatic in Ink Black or Canvas White, or tinted with Blueprint Blue for interactive states. Imagery primarily serves to explain or enhance content, acting as visual support and product showcases rather than decorative atmosphere. Density is moderate; imagery is used to break up text-heavy sections or highlight features.

## Dos & Donts

### Do

- Prioritize Blueprint Blue (#4570ff) for all primary calls-to-action to establish a consistent interactive focal point.
- Apply 9999px border-radius to all buttons for a friendly, pill-shaped aesthetic.
- Use Ink Black (#000000) for primary headlines and Canvas White (#ffffff) for card backgrounds, maintaining high contrast.
- Employ Divider Gray (#e5e7eb) for all hairline borders and subtle visual separations within components and between sections.
- Maintain a clear visual hierarchy with 'Regular' for body copy and 'Medium' for button text and subheadings.
- Utilize 20px border-radius for all standard cards to reinforce the consistent soft-edged design language.
- Implement the fixed vertical element gap of 8px when arranging discrete UI elements within components or sections.

### Don't

- Avoid using multiple vivid accent colors simultaneously; limit to Blueprint Blue for primary actions and occasional Soft Orange Highlight (#ff6808) for secondary emphasis.
- Do not introduce hard, sharp corners; use the established radii of 20px, 40px, or 9999px where appropriate.
- Refrain from using strong box-shadows or drop shadows; the system relies on flat surfaces and minimal elevation.
- Do not deviate from the 'Regular' and 'Medium' font families for core UI text; 'Nanum Pen Script' is reserved for highly decorative titles.
- Avoid using primary text in any color other than Ink Black (#000000) on light backgrounds or Canvas White (#ffffff) on dark backgrounds.
- Do not use overly complex gradients; the system favors solid colors and subtle accent washes.
- Avoid dense, information-heavy blocks; ensure generous padding (28px minimum for cards) and vertical spacing between elements.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #ffffff
border: #e5e7eb
accent: #ff6808
primary action: #4570ff (filled action)

Example Component Prompts:
1. Create a primary CTA button: Blueprint Blue background (#4570ff), Canvas White text (#ffffff), 9999px border-radius, 0px vertical and 16px horizontal padding. Text 'Get started now', 16px 'Medium' font, 500 weight.
2. Create a standard content card: Canvas White background (#ffffff), 20px border-radius, 28px padding on all sides. Headline 'Flexible Scheduling', 36px 'Regular' font (#000000), body text 'Easily manage shifts and team availability', 16px 'Regular' font (#000000).
3. Create a dark feature panel (full width): Ink Black background (#000000), 40px border-radius (bottom corners only visually), 60px top padding. Headline 'Unified Platform', 48px 'Medium' font (#ffffff), body text 'Streamline operations from a single dashboard', 18px 'Regular' font (#ffffff).

---
_Source: https://styles.refero.design/style/736830b5-90b1-47b0-99dd-d79454a0d22a_
