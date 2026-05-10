# Mintlify — Design Reference

> Digital librarian's desk. Precise information architecture meets subtle brand identity through selective color accents.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://mintlify.com](https://mintlify.com) |
| Refero page | [https://styles.refero.design/style/80d7ef36-ed7e-48bb-b558-f772eb40106f](https://styles.refero.design/style/80d7ef36-ed7e-48bb-b558-f772eb40106f) |
| Theme | light |
| Industry | devtools |

## Overview

Mintlify captures an atmosphere of serene, intelligent efficiency, like navigating a well-organized digital library. The predominant use of highly desaturated grays and stark black-on-white provides a clean, information-focused base. A single vivid green (#0c8c5e) acts as a digital beacon, highlighting interactive elements and key information without visual clutter. The visual restraint in color and a subtle elevation system communicate authority through clarity, making complex documentation feel approachable.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink | `#000000` | neutral | Primary text, core UI elements, strong contrasts against light backgrounds |
| White Canvas | `#ffffff` | neutral | Page backgrounds, card surfaces, inverted text on dark elements |
| Coal | `#08090a` | neutral | Prominent headings, button backgrounds, elevated text |
| Platinum | `#f2f2f2` | neutral | Subtle borders, dividers, subtle background variations |
| Steel | `#dddddd` | neutral | Thin stroke borders, less noticeable dividers |
| Sage Mark | `#0c8c5` | brand | Primary brand accent, interactive elements like CTA buttons, active states, key icons, and selected links. Its vividness provides clear visual cues in an otherwise neutral palette. |
| Emerald Glow | `#00dc8d` | accent | Secondary accent for icons or subtle graphic highlights, complementing the primary Sage Mark. |
| Deep Cobalt | `#0052ff` | accent | Specific graphic elements, illustrations where a strong, vibrant blue is needed. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 13px, 14px, 15px, 16px, 18px, 20px, 24px, 40px, 57px |
| lineHeight | 1.10, 1.15, 1.30, 1.33, 1.50, 1.71 |
| letterSpacing | -0.0200em at 57px, -0.0100em at 40px/24px, 0.0500em at 13px |
| substitute | system-ui |
| role | The sole typeface, used across all elements from headings to body text. Its clean, sans-serif design maintains legibility and a modern feel. The slight negative letter spacing on larger sizes adds a touch of sophistication to headlines, while positive spacing on smaller text ensures clarity, creating a subtly formal yet efficient tone. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.71 | 0.65 |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.33 |  |
| heading-sm | 20 |  | 1.3 |  |
| heading | 24 |  | 1.15 | -0.24 |
| heading-lg | 40 |  | 1.15 | -0.4 |
| display | 57 |  | 1.1 | -1.14 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 4px |
| cards | 16px |
| inputs | 0px |
| buttons | 1.67772e+07px |
| largeElements | 24px |

- **elementGap** — 
- **sectionGap** — 64px
- **cardPadding** — 16-24px
- **pageMaxWidth** — 

## Components

### Email CTA Input

### Feature Cards — Built for the Intelligence Age

### Customer Story Cards

### Text Link

**Role:** Navigation, inline links, secondary actions

backgroundColor=rgba(0,0,0,0), color=rgb(0,0,0) / lab(2.42579 -0.165291 -0.470081), borderRadius=16777216px, paddingTop=0px, paddingRight=0px, paddingBottom=0px, paddingLeft=0px. Appears as pure text initially, relying on context for interactivity.

### Outline Button

**Role:** Call to action, navigation items

backgroundColor=rgba(0,0,0,0), color=rgb(0,0,0), borderTopColor=rgb(0,0,0), borderRadius=4px, paddingTop=0px, paddingRight=0px, paddingBottom=0px, paddingLeft=0px. A subtle interactive element, contrasting with the filled button through its transparent background and border.

### Pill Ghost Button - Light Text

**Role:** Navigation, secondary actions on dark backgrounds

backgroundColor=rgba(0, 0, 0, 0), color=lab(100 0 0), borderTopColor=lab(100 0 0), borderRadius=1.67772e+07px, paddingTop=4px, paddingRight=12px, paddingBottom=4px, paddingLeft=12px. Used for subtle interactions, particularly within headers or on contrasting sections.

### Pill Filled Button - Dark Text

**Role:** Primary call to action in light contexts

backgroundColor=lab(100 0 0), color=lab(2.42579 -0.165291 -0.470081), borderTopColor=lab(100 0 0), borderRadius=1.67772e+07px, paddingTop=4.5px, paddingRight=12px, paddingBottom=4.5px, paddingLeft=12px. Provides strong visual emphasis for conversions when paired with a light background.

### Email Input Field

**Role:** Data entry, form submissions

backgroundColor=rgba(0, 0, 0, 0), color=lab(100 0 0), borderTopColor=lab(100 0 0), borderRadius=0px, paddingTop=0px, paddingRight=12px, paddingBottom=0px, paddingLeft=12px. A minimal input style with no background or border, relying on context and focus states for discoverability.

### Elevated Button (Light)

**Role:** Key interactive elements where a subtle lift is desired.

background: White Canvas (#ffffff), text: Coal (#08090a), shadow: lab(100 0 0 / 0.05) 0px 2px 4px 0px. This variant provides a subtle elevated visual for interactions without being visually heavy.

### Elevated Button (Dark)

**Role:** Key interactive elements on darker backgrounds where a subtle lift is desired.

background: Coal (#08090a), text: White Canvas (#ffffff), shadow: lab(2.42579 -0.165291 -0.470081 / 0.03) 0px 2px 4px 0px. Offers elevation on darker elements, maintaining visual hierarchy.

## Layout

The site uses a max-width contained layout rather than full-bleed, centering content to enhance focus. The hero section is full-width with large background illustrations, overlaid by a centered headline and call to action. Subsequent sections alternate between clear, distinct blocks with generous vertical spacing (sectionGap of ~64px). Content within sections often defaults to centered stacking or a 2-column layout with text on one side and a visual element or card grid on the other. Card grids often feature 3-5 columns. The navigation is a sticky top bar, providing persistent access without intruding on content. The overall density feels comfortable and spacious, allowing for easy readability.

## Imagery

The visual language for imagery leans heavily into abstract illustrations and product-focused visuals. Illustrations feature soft-edged, almost cloud-like shapes with subtle gradients and muted colors (like the light blue and orange clouds in the hero). Product screenshots, when present, are often cleanly cropped and contained within UI elements or cards, focusing on function. Photography is absent. Icons are typically monoline in style, either black on white or the brand's Sage Mark green, providing clear visual cues for functionality. The overall role of imagery is decorative and atmospheric in hero sections, illustrative for concepts, and clearly explanatory for product features, maintaining a text-dominant layout punctuated by strategic visual aids.

## Elevation philosophy

Elevation is sparingly used to create subtle visual hierarchy rather than strong dimensional separation. Shadows are soft, low-opacity, and diffused, providing a gentle lift to interactive elements like buttons. The intentional lightness of the shadows prevents the interface from feeling heavy or cluttered, reinforcing the clean and efficient aesthetic. Surfacesprimarily rely on background color changes for distinction rather than deep shadows.

## Dos & Donts

### Do

- Prioritize Inter font at all sizes and weights; use negative letter spacing on display sizes (57px: -1.14px, 40px: -0.4px) for a refined look and positive spacing (13px: 0.65px) for captions to ensure legibility.
- Use Sage Mark (#0c8c5e) exclusively for primary interactive elements, active states, and essential brand indicators to maintain its visual impact and clarity.
- Apply White Canvas (#ffffff) strictly as the default page background and for card surfaces, allowing content to breathe and accent colors to pop.
- Form inputs should be minimal, using a borderRadius of 0px and a transparent background (rgba(0,0,0,0)), allowing the surrounding design to define their presence.
- Employ the `lab(2.42579 -0.165291 -0.470081 / 0.03) 0px 2px 4px 0px` shadow for subtle content elevation against dark backgrounds and `lab(100 0 0 / 0.05) 0px 2px 4px 0px` for light backgrounds.
- Maintain a consistent elementGap of 4px, 6px, 8px, 10px, or 12px between adjacent UI elements to ensure a harmonious and comfortable density.
- Round all buttons, and other interactive pill-shaped elements to 1.67772e+07px for a modern, distinct soft edge that contrasts with the sharper 4px and 16px radii found elsewhere.

### Don't

- Do not introduce new typefaces; rely solely on Inter to preserve the visual identity.
- Avoid using highly saturated colors other than Sage Mark (#0c8c5e) or Emerald Glow (#00dc8d) for primary UI elements; reserve others for illustrative accents only.
- Do not deviate from the specified borderRadius values (1.67772e+07px for pills, 0px for inputs, 4px for tags, 16px for cards) to maintain shape consistency.
- Do not use dark backgrounds where White Canvas (#ffffff) is expected, as this would break the light theme consistency and intended contrast ratios.
- Do not use letter-spacing: normal. Always apply precise letter-spacing according to the type scale to maintain typographic rhythm.
- Avoid excessive decoration or complex gradients on UI elements; the design emphasizes clarity and directness.
- Do not add additional shadows beyond the specified `lab(2.42579 -0.165291 -0.470081 / 0.03) 0px 2px 4px 0px` or `lab(100 0 0 / 0.05) 0px 2px 4px 0px` to maintain a light and intentional elevation profile.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: Ink (#000000)
- Background: White Canvas (#ffffff)
- CTA Primary: Sage Mark (#0c8c5e)
- Border Subtle: Platinum (#f2f2f2)
- Heading: Coal (#08090a)

### 3-5 Example Component Prompts
1. Create a Hero Section: Background with abstract cloud illustration. Centered headline 'The Intelligent Knowledge Platform' in Inter weight 600, size 57px, Coal (#08090a), letterSpacing -1.14px. Subhead below it 'Helping teams create and maintain world-class documentation built for both humans and AI' in Inter weight 400, size 18px, Ink (#000000). Below that, a horizontal stack of an Email Input Field on left with a Pill Filled Button - Dark Text (White Canvas background, Coal text) on the right.
2. Create a Navigation Bar: Background White Canvas (#ffffff). Logo 'mintlify' Sage Mark (#0c8c5e). Top-right navigation links: 'Resources', 'Documentation', 'Customers', 'Blog', 'Pricing' as Text Links (Ink #000000). Alongside 'Contact sales' as an Outline Button, and 'Start for free' as a Pill Filled Button - Dark Text (White Canvas background, Coal text, borderRadius 1.67772e+07px, paddingTop 4.5px, paddingRight 12px).
3. Create a Feature Card: White Canvas (#ffffff) background, borderRadius 16px. Padding 24px all sides. Headline 'Quickstart Guide' in Inter weight 600, size 24px, Coal (#08090a). Body text beneath 'Start building intelligent documentation in five minutes' in Inter weight 400, size 16px, Ink (#000000).

---
_Source: https://styles.refero.design/style/80d7ef36-ed7e-48bb-b558-f772eb40106f_
