# Promova — Design Reference

> layered pastel blocks on a dark canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://promova.com](https://promova.com) |
| Refero page | [https://styles.refero.design/style/dae5e893-ca18-44c3-8f83-358cb52af237](https://styles.refero.design/style/dae5e893-ca18-44c3-8f83-358cb52af237) |
| Theme | dark |
| Industry | other |

## Overview

Promova's visual identity balances playful, almost illustrative color blocks with grounded, sharp typography. The use of a custom display font with wide characters and prominent Manrope for body text creates a distinctly readable and approachable feel, while the generous rounded corners on cards and buttons soften the overall aesthetic. Occasional bright, muted color panels are layered behind content, adding a sense of depth and energetic contrast against the otherwise neutral black-and-white core.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary text, button backgrounds, general UI elements. The foundational dark color for text and interactive components. |
| Cloud White | `#ffffff` | neutral | Content backgrounds, button text. Provides strong contrast against Midnight Ink for readability. |
| Pebble Gray | `#595959` | neutral | Secondary text and subtle borders. Used for less prominent content to reduce visual hierarchy. |
| Soft Mist | `#f5f5f5` | neutral | Light background sections, subtly contrasting with Cloud White. Provides slight visual separation in some areas. |
| Limoncello | `#fff050` | brand | Dynamic accents and interactive states. Its vividness draws attention as a hero accent. |
| Sky Haze | `#eceeff` | accent | Background panels and decorative blocks, contributing to the layered, playful aesthetic. |
| Misty Meadow | `#f4f9e7` | accent | Background panels and decorative blocks, adding a soft, natural accent. |
| Lavender Dream | `#dfe3ff` | accent | Background panels and decorative blocks, creating gentle visual interest. |
| Periwinkle Charm | `#bec8ff` | accent | Background panels and decorative blocks, a muted violet tone that adds depth without overwhelming. |

## Typography

### Manrope

| Key | Value |
| --- | --- |
| weight | 200, 400, 500, 700 |
| sizes | 10px, 14px, 15px, 16px, 18px, 20px, 24px, 25px, 140px |
| lineHeight | 1.00, 1.20, 1.40, 1.42, 1.44, 1.50, 1.67 |
| letterSpacing | normal |
| substitute | system-ui |
| role | Primary text font, used widely for body copy, navigation, buttons, and detailed information. Its geometric clarity ensures readability across all weights and sizes. |

### Nekst

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 15px, 16px, 18px, 19px, 24px, 40px, 50px, 60px, 70px, 100px, 120px |
| lineHeight | 1.00, 1.20, 1.67 |
| letterSpacing | normal |
| role | Display font used primarily for prominent headings and titles. Its distinct character provides a strong brand voice and visual differentiation, especially at larger sizes where it demands attention. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 14 |  | 1.4 |  |
| body | 16 |  | 1.5 |  |
| body-lg | 18 |  | 1.67 |  |
| subheading | 24 |  | 1.2 |  |
| heading | 40 |  | 1 |  |
| heading-lg | 70 |  | 1 |  |
| display | 120 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 30px |
| small | 4px |
| buttons | 20px |
| general | 10px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 30px
- **pageMaxWidth** — 

## Components

### Promotional Modal Card

### Button Group — Primary & Secondary

### FAQ Accordion

### Secondary Outlined Button

**Role:** Secondary action button with border

Background: rgba(0,0,0,0). Text: #000000 (Midnight Ink). Border: #000000 (Midnight Ink). Padding: 20px all sides. Border Radius: 20px. Used for 'Sign Up' or other less prominent actions.

### Ghost Navigation Button

**Role:** Minimal navigation item or tertiary action

Background: rgba(0,0,0,0). Text: #000000 (Midnight Ink). Border: #000000 (Midnight Ink). Padding: 0px all sides. Border Radius: 0px. Used for navigation links or text-based actions.

### Abstract Background Panel

**Role:** Decorative background element

Backgrounds: varies between #eceeff (Sky Haze), #f4f9e7 (Misty Meadow), #dfe3ff (Lavender Dream), #bec8ff (Periwinkle Charm). Often full-bleed or large blocks. Border Radius: 30px. No shadows. These serve as a visual bed for content cards, adding color and depth.

## Layout

The layout is primarily full-bleed, with content sections extending to the edges of the viewport before narrowing to a comfortable reading width internally for text blocks. The hero section often features large, impactful headings using the custom 'Nekst' font over a colored background or abstract graphic. Sections often alternate between dark and light background themes, achieved by large, rounded-corner background panels of muted colors. Content is typically arranged in centered stacks or two-column layouts (text left, image/graphic right). Vertical spacing between sections is generous (approximately 40px), creating clear visual breaks. The navigation is a sticky top bar with prominent 'GET STARTED' and 'Sign Up' buttons.

## Imagery

The site uses a combination of tight product screenshots, profile-style circular headshots, and abstract illustrative background graphics. Product shots of phones are clean, isolated, and often displayed at an angle, focusing purely on the UI. Photography of individuals is contained within circular masks, implying a focus on people and community without full-bleed lifestyle images. Abstract graphic panels in muted pastel tones (#eceeff, #f4f9e7, #dfe3ff, #bec8ff) serve as decorative backgrounds, often layered behind content cards, contributing to a soft, inviting atmosphere. The overall density is balanced, allowing imagery to complement text rather than dominate, primarily serving as decorative context or product showcasing.

## Dos & Donts

### Do

- Use Manrope for all body text and descriptions at various weights, ensuring crisp legibility.
- Apply Nekst font specifically for large headings and display text to deliver brand identity.
- Maintain a clear visual hierarchy by using #000000 (Midnight Ink) for primary text and #595959 (Pebble Gray) for secondary information.
- Implement a 30px border-radius on cards, 20px on prominent buttons to maintain a soft, approachable aesthetic.
- Utilize #fff050 (Limoncello) sparingly for key interactive elements or highlights to draw attention.
- Structure page sections with padding that results in 40px vertical gaps between major content blocks.

### Don't

- Avoid using shadows for elevation; instead, create depth through background color layering and contrasting panels.
- Do not deviate from the Manrope and Nekst font families; avoid mixing in other typefaces.
- Refrain from using overly saturated colors outside of the defined accent palette.
- Do not use generic square corners on content cards or primary buttons; maintain the specified rounded radii.
- Avoid tight spacing; ensure comfortable 'elementGap' of at least 10px between interactive elements and textual content.
- Do not use #000000 (Midnight Ink) on a #595959 (Pebble Gray) background, as it fails AAA contrast.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #000000 (Midnight Ink)
- Background: #ffffff (Cloud White)
- Call to Action: #000000 (Midnight Ink)
- Border (secondary button): #000000 (Midnight Ink)
- Accent: #fff050 (Limoncello)

### Example Component Prompts
1. Create a hero section: 'Midnight Ink' (#000000) background. Headline 'Learn a language your way' with 'Nekst' font, size 120px, weight 400, line-height 1.0, color #ffffff (Cloud White). Subtext 'Learn a new language with an AI-powered language platform...' using 'Manrope' font, size 18px, weight 400, line-height 1.67, color #ffffff (Cloud White). Below the text, add a button 'TRY PROMOVA' with background #fff050 (Limoncello), text #000000 (Midnight Ink), padding 20px all sides, border-radius 20px.
2. Design a content block featuring a 'Rounded Content Card': background #ffffff (Cloud White), 30px border-radius, 30px padding. Inside, place a heading 'Find the right course for you' using 'Manrope' font, size 24px, weight 700, line-height 1.2, color #000000 (Midnight Ink). Below, add body text 'Not a game — a tool that helps learners...' using 'Manrope' font, size 16px, weight 400, line-height 1.5, color #595959 (Pebble Gray).
3. Generate a 'Secondary Outlined Button': background rgba(0,0,0,0), text '#000000' (Midnight Ink), border '#000000' (Midnight Ink), 20px border-radius, 20px padding. Show a hover state with background '#000000' (Midnight Ink) and text '#ffffff' (Cloud White).

---
_Source: https://styles.refero.design/style/dae5e893-ca18-44c3-8f83-358cb52af237_
