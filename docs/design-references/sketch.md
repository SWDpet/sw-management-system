# Sketch — Design Reference

> Architectural Digital Canvas. A pristine white canvas with soft, glowing digital light, and precise typography.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://sketch.com](https://sketch.com) |
| Refero page | [https://styles.refero.design/style/4caadb3c-3865-4a4d-9e1a-46478ac71078](https://styles.refero.design/style/4caadb3c-3865-4a4d-9e1a-46478ac71078) |
| Theme | light |
| Industry | design |

## Overview

This design system feels like a softly illuminated, modern creative studio, balancing precise Swiss design principles with a playful, digital-native warmth. The extensive use of achromatic values creates a clean, almost stark foundation, elevated by a single vibrant gradient that appears subtly in the background and on key interactive elements. A bespoke serif font, 'Reckless', introduces an unexpected, artful touch for major headlines, contrasting with the utilitarian 'InterVariable' for all other text, grounding the aesthetic between craft and computation. The juxtaposition of highly rounded 'pill' elements and more structured 20px radii hints at a system that values both approachability and considered form.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Black | `#000000` | neutral | Major text, icons, borders, input text — provides high contrast for legibility on light backgrounds. |
| Graphite | `#212123` | neutral | Primary body text, headers, and key UI elements — a softer alternative to pure black for improved visual comfort. |
| Stone Grey | `#4a4a4a` | neutral | Secondary text, subheadings, icon fills — for reduced emphasis. Provides clear hierarchy below Graphite. |
| Ash Grey | `#5c5c5c` | neutral | Tertiary text, subtle descriptions, and less prominent UI elements. |
| Silver Mist | `#a7a7a7` | neutral | Placeholder text, disabled states, divider lines — for very low emphasis. |
| Canvas White | `#fafafa` | neutral | Page backgrounds, card backgrounds, button backgrounds for ghost buttons, primary surface color. |
| Cloud White | `#e6e6e6` | neutral | Input field backgrounds, subtle borders, and background accents — provides a slight visual separation from Canvas White without introducing color. |
| Deep Space | `#151515` | neutral | Filled button backgrounds for primary actions — provides a strong, authoritative call to action against light backgrounds. |
| Sky Blue | `#555dff` | accent | Status badges, interactive element highlights — signals 'new' or 'info' effectively. |
| Teal Glow | `#03cbbc` | accent | Status badges, subtle accent for specific content categories. |
| Ocean Blue | `#4389e6` | accent | Status badges, alternative accent for information or categories. |
| Aurora Gradient | `#744bd0` | brand | Subtle background element of the hero section and for card backgrounds — instills a sense of modern digital creativity and wonder. |
| Sunset Gradient | `#b47eee` | brand | Accent gradient for specific UI elements or illustrations, adding visual flair. |

## Typography

### InterVariable

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600 |
| sizes | 11px, 12px, 14px, 15px, 16px, 17px, 20px, 22px, 24px |
| lineHeight | 1.00, 1.09, 1.15, 1.17, 1.25, 1.33, 1.40, 1.41, 1.43, 1.50, 1.60, 1.65 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "liga" |
| substitute | Inter |
| role | Body text, navigation items, buttons, form inputs, and badges — a highly legible, functional sans-serif for all UI elements. 'calt' and 'liga' features ensure typographic precision. |

### Reckless

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 44px, 76px |
| lineHeight | 1.00, 1.09 |
| letterSpacing | -0.0230em at 76px, -0.0200em at 44px |
| fontFeatureSettings | 'calt', 'liga' |
| substitute | Playfair Display |
| role | Large display headlines — provides a distinctive and elegant contrast to the utilitarian sans-serif, suggesting a craft or artistic sensibility for key messages. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.43 |  |
| body-sm | 14 |  | 1.41 |  |
| body | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.4 |  |
| heading | 44 |  | 1.09 | -0.88 |
| display | 76 |  | 1 | -1.748 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 20px |
| badges | 9999px |
| inputs | 16px |
| buttons | 24px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 78px
- **pageMaxWidth** — 

## Components

### Primary CTA Button Group

### Announcement Banner Card

### Status Badge Collection

### Primary Filled Button

**Role:** Main call to action

backgroundColor: #151515, color: #fafafa, borderRadius: 24px, paddingTop: 11px, paddingRight: 24px, paddingBottom: 12px, paddingLeft: 24px.

### Ghost Button with Outline

**Role:** Secondary action or link

backgroundColor: #fafafa, color: #0000ee (browser default link color, likely intended as a brand blue or an un-styled link, for this system treat as #4389e6), border: 1px solid #4389e6, borderRadius: 20px, paddingTop: 52px, paddingRight: 52px, paddingBottom: 52px, paddingLeft: 52px. Note the unusually large padding for a button, indicating a hero-level element.

### Information Card with Shadow

**Role:** Container for secondary content or announcements

backgroundColor: rgba(255, 255, 255, 0.48), borderRadius: 20px, boxShadow: rgba(0, 0, 0, 0.08) 0px 0px 4px 0px, rgba(0, 0, 0, 0.02) 0px 4px 8px 0px, rgba(0, 0, 0, 0.08) 0px 1px 2px 0px, paddingTop: 100px, paddingRight: 100px, paddingBottom: 78px, paddingLeft: 78px. Transparent background allows background gradients to show through.

### Default Input Field

**Role:** Standard user input

backgroundColor: #f9f9f9, color: #000000, borderRadius: 16px, paddingTop: 12px, paddingRight: 14px, paddingBottom: 12px, paddingLeft: 14px, border: 1px solid #000000.

### Line Input Field

**Role:** Minimalist input field

backgroundColor: #ffffff, color: #000000, borderBottom: 1px solid rgba(0, 0, 0, 0.1), borderRadius: 3px, minimal padding allowing content to define size.

### Vivid Blue Badge

**Role:** Highlighting 'new' or 'info' status

backgroundColor: #555dff, color: #ffffff, borderRadius: 9999px (pill shape), paddingTop: 4px, paddingRight: 6px, paddingBottom: 3px, paddingLeft: 6px.

### Teal Badge

**Role:** Highlighting secondary status or category

backgroundColor: #03cbbc, color: #ffffff, borderRadius: 9999px (pill shape), paddingTop: 4px, paddingRight: 6px, paddingBottom: 3px, paddingLeft: 6px.

### Ocean Blue Badge

**Role:** Highlighting tertiary status or category

backgroundColor: #4389e6, color: #ffffff, borderRadius: 9999px (pill shape), paddingTop: 4px, paddingRight: 6px, paddingBottom: 3px, paddingLeft: 6px.

## Layout

The page uses a full-bleed background model, with content largely contained within an implicit maximum width, centrally aligned. The hero section features a large, full-bleed gradient background ('Aurora Gradient') with a centered, prominent headline in 'Reckless' and a multi-line value proposition using 'InterVariable'. Subsequent sections follow a consistent alternating pattern of text-left/image-right or vice-versa, maintaining generous vertical spacing (likely around 64px sectionGap). Content blocks are generally stacked, and information cards tend to be centered. The navigation is a classic top bar, adhering to the main content width, featuring text links and distinct 'Sign In' (text) and 'Get Started' (pill button) actions. The overall density is spacious, allowing typography and UI elements to breathe.

## Imagery

Imagery is minimal and highly abstracted. The site primarily uses UI screenshots of the Sketch application, presented as clean, functional demonstrations. When present, graphics are abstract, soft gradients like Aurora Gradient or Sunset Gradient, serving as large background elements to add a sense of digital atmosphere rather than conveying specific information. There's an absence of lifestyle photography or complex illustrations, keeping the focus squarely on the product UI and its functionality. Icons are outline-based, monochromatic (Graphite #212123 or Pitch Black #000000), contributing to the clean, technical aesthetic.

## Dos & Donts

### Do

- Do use Reckless (weight 500) for all display headlines (44px, 76px) to establish a distinctive brand voice.
- Do apply InterVariable (weight 400) at 16px with Graphite (#212123) for all primary body text.
- Do use Canvas White (#fafafa) as the primary page background color for all main content sections.
- Do ensure interactive elements like buttons and badges utilize either 24px or 9999px border radii, never using intermediate values.
- Do apply the Aurora Gradient (linear-gradient(90deg, rgb(50, 173, 247) 20%, rgb(116, 75, 208) 40%, rgb(233, 127, 66) 50%, rgb(50, 173, 247) 75%)) as a background element to convey a digital, artistic atmosphere.
- Do reserve Deep Space (#151515) filled buttons for the most prominent calls to action only, pairing with Canvas White (#fafafa) text.

### Don't

- Don't use generic system fonts for any text; adhere strictly to InterVariable and Reckless fonts.
- Don't clutter layouts; maintain spaciousness with elementGap of 8px and larger section gaps where appropriate.
- Don't introduce shadows that are not specified; adhere to the subtle, dark shadow palette for icons and cards.
- Don't use highly saturated colors for backgrounds or large content blocks; reserve vivid hues for small accent elements like badges.
- Don't use rounded corners for cards or buttons that deviate from 20px or 24px respectively, or 9999px for pills.
- Don't use any color other than Canvas White (#fafafa), Cloud White (#e6e6e6), or transparent for card backgrounds.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): Graphite #212123
- Background (Primary): Canvas White #fafafa
- CTA (Filled Button): Deep Space #151515
- Border (Input/Ghost Button): Pitch Black #000000 (for inputs), Sky Blue #555dff (for ghost buttons)
- Accent (Badge): Sky Blue #555dff

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Full-width container with `linear-gradient(90deg, rgb(50, 173, 247) 20%, rgb(116, 75, 208) 40%, rgb(233, 127, 66) 50%, rgb(50, 173, 247) 75%)` background. Centered headline 'Designers, welcome home.' in Reckless, weight 500, size 76px, lineHeight 1.0, letterSpacing -1.748px, color Graphite #212123. Below, body text 'Sketch is a toolkit made by designers, for designers, that puts the focus on you and your work.' in InterVariable, weight 400, size 24px, lineHeight 1.33, color Graphite #212123. Include a primary filled button: 'Get started for free' (Deep Space #151515 background, Canvas White #fafafa text, 24px radius, 11px 24px padding), and text description 'Requires macOS Sonoma (14.0.0) or newer' in InterVariable, weight 400, size 14px, lineHeight 1.41, color Ash Grey #5c5c5c.

2. **Generate an Info Card:** backgroundColor `rgba(255, 255, 255, 0.48)`, borderRadius `20px`, boxShadow `rgba(0, 0, 0, 0.08) 0px 0px 4px 0px, rgba(0, 0, 0, 0.02) 0px 4px 8px 0px, rgba(0, 0, 0, 0.08) 0px 1px 2px 0px`, paddingTop `100px`, paddingRight `100px`, paddingBottom `78px`, paddingLeft `78px`. Headline 'New in Sketch: 150+ improvements and fixes' in InterVariable, weight 600, size 24px, lineHeight 1.33, color Graphite #212123. Body text 'Quality-of-life improvements, from cleaner image drops on the Canvas to faster fill, border, and shortcut workflows.' in InterVariable, weight 400, size 17px, lineHeight 1.41, color Graphite #212123. Add a link 'Learn more' in InterVariable, weight 500, size 16px, lineHeight 1.5, color #4389e6.

3. **Create a Header Navigation Bar:** Background Canvas White #fafafa. Left-aligned brand logo (diamond icon). Right-aligned navigation links: 'Product', 'Explore', 'Pricing', 'Support', using InterVariable, weight 500, size 16px, lineHeight 1.5, color Stone Grey #4a4a4a. Right-most: 'Sign In' link (InterVariable, weight 500, size 16px, lineHeight 1.5, color Graphite #212123) and 'Get started' button (Deep Space #151515 background, Canvas White #fafafa text, 24px radius, 11px 16px padding on left/right, 12px padding-bottom, 11px padding-top, plus a download icon).

---
_Source: https://styles.refero.design/style/4caadb3c-3865-4a4d-9e1a-46478ac71078_
