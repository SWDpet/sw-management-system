# Playful — Design Reference

> Gradient Playground

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://playful.software](https://playful.software) |
| Refero page | [https://styles.refero.design/style/f93ac72e-73b2-4b2c-80eb-351ddfa56f4d](https://styles.refero.design/style/f93ac72e-73b2-4b2c-80eb-351ddfa56f4d) |
| Theme | light |
| Industry | productivity |

## Overview

Playful adopts a vibrant yet refined aesthetic with a spacious layout that emphasizes content clarity. Backgrounds feature subtle gradients, creating soft washes of color. Typography is predominantly dark and compact, giving a serious counterpoint to the playful accent colors. Interactive elements utilize a vivid pink, contrasting against a canvas dominated by off-white, imparting an energetic yet sophisticated feel.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#0f172a` | neutral | Primary text, deep neutrals for icons and borders. This near-black provides strong contrast against light surfaces |
| Vivacious Pink | `#ff2e95` | brand | CTA buttons, accented links, and vibrant highlights — this vivid pink defines primary interactivity and branded elements |
| Frost Canvas | `#f6f2ee` | neutral | Primary page background, footer background, and subtle secondary button backgrounds. It provides a warm, soft base; Hero section and key visual backgrounds – a soft, multi-hued gradient establishing a playful and spacious atmosphere |
| Pitch Black | `#000000` | neutral | Deepest text for headlines and critical user interface elements, providing absolute contrast. Also used for dark component backgrounds and shadows |
| Deep Graphite | `#111111` | neutral | Card backgrounds, section headers, and dark surface accents |
| Coal Text | `#202126` | neutral | Headline text for secondary sections |
| Pale Ash | `#e8e5e0` | neutral | Subtle borders and dividers, providing visual structure without harsh lines |
| Cloud White | `#ffffff` | neutral | Input fields, content backgrounds within cards, and for text on dark backgrounds |
| Slate Gray | `#414040` | neutral | Input field text, placeholder text, and muted body copy |
| Light Taupe | `#e2dcd6` | neutral | Delicate borders and secondary structural elements |
| Light Graphite | `#353535` | neutral | Darkest background for subtle overlays or components |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700, 900 |
| weights | 400, 500, 600, 700, 900 |
| sizes | 14px, 16px, 18px, 26px, 30px, 70px, 79px |
| lineHeight | 1.00, 1.15, 1.19, 1.20, 1.23, 1.33, 1.70 |
| letterSpacing | -0.002 |
| substitute | system-ui, sans-serif |
| role | The primary typeface for all text. Its weights are used for impactful headlines at larger sizes and for clear, concise body text. The tight letter spacing for display sizes gives it a modern, compact feel. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400, 600, 700 |
| weights | 400, 600, 700 |
| sizes | 13px, 15px, 16px, 20px |
| lineHeight | 1.20, 1.40 |
| substitute | Helvetica Neue, Helvetica, sans-serif |
| role | Used for accessibility-focused elements like button labels and form inputs, providing robust legibility. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.4 | 0 |
| body | 16 |  | 1.2 | -0.002 |
| subheading | 26 |  | 1.33 | -0.002 |
| heading-sm | 30 |  | 1.23 | -0.002 |
| heading | 70 |  | 1.15 | -0.002 |
| display | 79 |  | 1 | -0.002 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 999px |
| cards | 44px |
| images | 16px |
| buttons | 99px |

- **elementGap** — 10px
- **sectionGap** — 113px
- **cardPadding** — 12px
- **pageMaxWidth** — 1500px

## Components

### Ghost Button

**Role:** Navigational or secondary actions where visual weight should be minimal.

Transparent background, 'Midnight Ink' (0f172a) text, no border or explicit radius. Padding: 12px vertical, 32px horizontal.

### Primary Action Button

**Role:** Main calls to action.

Filled with 'Vivacious Pink' (ff2e95), 'Frost Canvas' (f6f2ee) text, 99px border-radius, giving it a pill shape. Padding: 14px vertical, 24px horizontal. Arial 600 at 15px.

### Circular Secondary Button

**Role:** Icon-only or small, focused interactive elements.

Filled with 'Frost Canvas' (f6f2ee), 'Vivacious Pink' (ff2e95) text, 50% border-radius for a perfect circle. No padding specified (implied from 0px values).

### Feature Card

**Role:** Displaying product features or content blocks.

Background 'Deep Graphite' (111111), 44px border-radius. Shadow: rgba(0, 0, 0, 0.22) 0px 32px 80px 0px, rgba(0, 0, 0, 0.08) 0px 2px 8px 0px.

### Input Field

**Role:** Collecting user data.

Transparent background, 'Slate Gray' (414040) text, 'Slate Gray' (414040) border. No explicit border-radius (implied from 0px values). Arial 400 at 16px.

## Layout

The page adheres to a max-width of 1500px, centered on the screen. The hero section is full-bleed with a top custom gradient background, featuring a large, centered headline, subtext, and an input/button pair. Subsequent sections maintain consistent vertical spacing of 113px and alternate between centered text blocks and grids of feature cards. Navigation is a minimal top bar. Overall, the layout is spacious, allowing content to breathe.

## Imagery

The site uses a mix of playful, graphic-style illustrations for app icons and abstract, colorful gradients as hero and background elements. App icons are displayed as contained, rounded-corner elements, sometimes overlapping, showcasing a product-focused content. There are also simple, functional icons (like chevron arrows) that are outlined, and monochrome 'Midnight Ink' or 'Slate Gray'. Imagery serves primarily as decorative atmosphere and product showcase, with a medium density relative to text.

## Dos & Donts

### Do

- Always use a generous `sectionGap` of 113px to maintain spaciousness between primary content blocks.
- Apply `border-radius: 44px` to article cards and larger UI containers to soften surfaces and align with the playful aesthetic.
- Utilize 'Vivacious Pink' (#ff2e95) exclusively for primary calls to action, active navigation states, and key interactive highlights.
- Ensure all primary headings use Inter font with `letter-spacing: -0.002em` to achieve a compact, signature appearance.
- Prioritize 'Frost Canvas' (#f6f2ee) for main page backgrounds to maintain a light and airy feel.
- Employ the dual shadow `rgba(0, 0, 0, 0.22) 0px 32px 80px 0px, rgba(0, 0, 0, 0.08) 0px 2px 8px 0px` for elevated cards.
- Use 'Midnight Ink' (#0f172a) for body text and secondary element borders for strong contrast and clarity.

### Don't

- Do not use dark backgrounds for sections that contain extensive body copy; maintain a light background to preserve readability.
- Avoid arbitrary border radii; stick to the established system tokens: 44px for cards, 99px for buttons, 16px for images.
- Never introduce new chromatic colors outside of 'Vivacious Pink' (#ff2e95) and the brand gradient for UI elements.
- Do not use a default system font for any body text or headings; 'Inter' is paramount for brand identity.
- Avoid dense, information-heavy blocks without sufficient padding or `elementGap` of at least 10px.
- Do not add any additional box-shadows to elements other than cards; rely on the subtle elevation provided by background color differences.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #0f172a
background: #f6f2ee
border: #e8e5e0
accent: #ff2e95
primary action: #ff2e95 (filled action)

Example Component Prompts:
Create a Primary Action Button: #ff2e95 background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Create a Feature Card: Background 'Deep Graphite' (111111), 44px border-radius, shadow 'rgba(0, 0, 0, 0.22) 0px 32px 80px 0px, rgba(0, 0, 0, 0.08) 0px 2px 8px 0px'. Content inside should use Inter 16px weight 400 in 'Cloud White' (ffffff) for body, with 12px card padding. 

Create a Ghost Button: Background transparent, 'Midnight Ink' (0f172a) text using Arial 16px weight 400. Padding: 12px vertical, 32px horizontal. Used for secondary navigation like 'When will it be available?' 

---
_Source: https://styles.refero.design/style/f93ac72e-73b2-4b2c-80eb-351ddfa56f4d_
