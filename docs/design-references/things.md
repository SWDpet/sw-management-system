# Things — Design Reference

> organized desktop, clean and bright

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://culturedcode.com/things](https://culturedcode.com/things) |
| Refero page | [https://styles.refero.design/style/ec0f5bca-8367-49e7-b8aa-73b3fa09a4a0](https://styles.refero.design/style/ec0f5bca-8367-49e7-b8aa-73b3fa09a4a0) |
| Theme | light |
| Industry | productivity |

## Overview

This design system evokes a sense of calm efficiency, like a meticulously organized desktop with clean lines and subtle visual cues. The primary visual identity is built around shades of cool gray and a vibrant, yet restrained, blue. High contrast text on light backgrounds prioritizes readability, while carefully applied rounded corners and a singular, subtle shadow provide a touch of visual softness without compromising the overall crispness. The experience feels grounded and clear, guiding the user with precision.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#303336` | neutral | Primary text, deep accents, interactive states for headings. |
| Charcoal Text | `#44474b` | neutral | Secondary text, descriptive body copy. |
| Storm Gray | `#55606` | neutral | Tertiary text, subtle borders, inactive link states. |
| Subtle Ash | `#838b96` | neutral | Placeholder text, minor labels, light gray borders for contrast. |
| Silver Mist | `#9299a4` | neutral | Fainter secondary text, subtle dividers. |
| Off-White Canvas | `#ffffff` | neutral | Main page backgrounds, input fields, primary elevated surfaces. |
| Cloud Gray | `#f2f5f7` | neutral | Card backgrounds, section separators, subtle background shifts. |
| Frost Border | `#dfe3e8` | neutral | Input element borders, subtle separators. |
| Ocean Blue | `#2576eb` | brand | Active link states, primary interactive elements, icon accents — a vivid anchor. |
| Sky Link Blue | `#5c9cf5` | brand | Hover states for links, secondary interactive elements — a slightly softer, yet still vibrant, indicator. |
| Action Button Blue | `#4f91fb` | brand | Primary Call-to-Action button background — a stable, deep blue that signifies action. |

## Typography

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400, 600, 700, 800 |
| sizes | 13px, 14px, 15px, 16px, 17px, 18px, 20px, 24px, 36px |
| lineHeight | 1.00, 1.20, 1.25, 1.30, 1.35, 1.40, 1.60 |
| letterSpacing | normal |
| substitute | system-ui, sans-serif |
| role | The sole typeface, maintaining visual unity across all elements from headings to body text and UI components. Its system-font nature provides native OS feel and fast loading. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.4 |  |
| body | 16 |  | 1.6 |  |
| subheading | 18 |  | 1.35 |  |
| heading | 24 |  | 1.25 |  |
| display | 36 |  | 1.2 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 18px |
| icons | 3px |
| inputs | 6px |
| buttons | 6px |
| hero-label | 12.8px |

- **elementGap** — 4-14px
- **sectionGap** — 48px
- **cardPadding** — 18px
- **pageMaxWidth** — 900px

## Components

### Watch Introduction Video Button

### Simply Powerful Section Card

### App Sidebar Navigation Card

### Primary Action Button

**Role:** Call to action.

Background: Action Button Blue (#4f91fb), text: Off-White Canvas (#ffffff), border-radius: 6px, padding: 7px 17px.

### Section Separator Card

**Role:** Organizing content blocks.

Background: Cloud Gray (#f2f5f7), border-radius: 18px, padding: 18px.

### Default Card

**Role:** General content display.

Background: transparent, border-radius: 0px, no box-shadow, no padding.

### Text Input Field

**Role:** User data input.

Background: Off-White Canvas (#ffffff), text color: Midnight Ink (#000000), border-color: Frost Border (#dfe3e8), border-radius: 6px, padding: 8px.

### Navigation Link

**Role:** Top navigation.

Text color: Storm Gray (#55606e) for inactive, Ocean Blue (#2576eb) for active/hover. Font size 15px, weight 600, line height 1.3.

### Watch Video Button

**Role:** Auxiliary action, media launch.

Text: Ocean Blue (#2576eb), includes an icon, font size 18px, weight 600, line height 1.35.

## Layout

The page adheres to a centered maximum-width model of 900px, providing ample negative space on wider screens. The hero section features a large, centered product icon above a headline and subtext, followed by a video link, establishing a clear hierarchy. Subsequent sections alternate between text-heavy descriptions and product screenshots, often in a stacked, centered arrangement. The overall section rhythm is consistent, separated by substantial vertical spacing (approximately 48px), creating a sense of calm and clarity. Navigation is a simple top bar with minimal links, complementing the uncluttered feel.

## Imagery

The visual language consists primarily of highly polished, dimensional 3D app icons and product screenshots. These visuals are meticulously crafted, featuring a subtle gloss and depth that convey precision and quality. Photography is absent; instead, product screenshots showcase the UI in-situ on devices like Macs and iPhones, often with a slightly angled perspective. Illustrations are used sparingly, for instance, a small, multi-colored abstract graphic for 'Simply Powerful' — these are flat, geometric, and isolated, acting as decorative flourishes rather than primary content. The imagery's role is to explicitly showcase the product and its interface, emphasizing functionality and aesthetic quality.

## Elevation philosophy

Elevation is used sparingly and subtly, providing just enough visual separation to indicate a raised element without heavy shadows. The primary technique involves a soft, diffused shadow to gently lift content, maintaining the design's overall lightness and crispness rather than creating deep spatial depth.

## Dos & Donts

### Do

- Use Midnight Ink (#303336) for all primary headings and major text elements to maintain high contrast and visual weight.
- Apply Cloud Gray (#f2f5f7) as a background for distinct content sections or cards to create clear visual separation.
- Reserve Ocean Blue (#2576eb) for critical interactive elements like active links, primary icons, and CTA text to guide user focus.
- Use a consistent border-radius of 6px for all functional elements like buttons and input fields to ensure a cohesive interactive experience.
- Ensure generous vertical spacing between sections, using multiples of the 12px token to establish a comfortable content density.

### Don't

- Do not introduce new color hues; strictly adhere to the defined grays and blues to preserve the system’s crisp and minimalist aesthetic.
- Avoid arbitrary border-radii; use 3px for icons, 6px for buttons/inputs, and 18px for cards, and 12.8px for select larger elements.
- Do not use box-shadows liberally; limit to the subtle rgba(0, 0, 0, 0.1) 0px 2px 8px 0px for single-level elevation.
- Do not vary font families; maintain the system's reliance on ui-sans-serif for all text to ensure consistent typography and OS integration.
- Avoid dense packing of content; ensure minimum element gaps of 4px and significant section gaps with 48px to allow content to breathe.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #303336
- Background: #ffffff
- CTA Button: #4f91fb
- Card Background: #f2f5f7
- Link: #2576eb

### Example Component Prompts
1. Create a primary action button: text 'Learn More', background #4f91fb, text color #ffffff, border-radius 6px, padding 7px 17px.
2. Design a feature card: background #f2f5f7, border-radius 18px, padding 18px. Place a heading at 24px weight 700 and body text at 16px weight 400, both using #303336.
3. Implement a text input field: background #ffffff, border-color #dfe3e8, border-radius 6px, padding 8px. Placeholder text in #838b96.
4. Build a hero section (max-width 900px, centered): main heading 'Things' at 36px weight 800 #303336. Subtext 'Things is the award-winning...' at 18px weight 400 #44474b. Below, a 'Watch Introduction Video' link: text #2576eb, 18px weight 600, with a play icon.

---
_Source: https://styles.refero.design/style/ec0f5bca-8367-49e7-b8aa-73b3fa09a4a0_
