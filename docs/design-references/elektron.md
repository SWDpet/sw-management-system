# Elektron — Design Reference

> Digital console in midnight. A world of precise, glowing information contained within a dark, structured shell.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://elektron.se](https://elektron.se) |
| Refero page | [https://styles.refero.design/style/6b5a0bf4-3d2a-4c3b-aa2e-652f1acb82c0](https://styles.refero.design/style/6b5a0bf4-3d2a-4c3b-aa2e-652f1acb82c0) |
| Theme | dark |
| Industry | other |

## Overview

This design system evokes a high-fidelity control panel, where functional elements are precisely rendered against a deep, uniform darkness. The visual atmosphere is serious and technical, achieved through minimal color and sharp contrasts between almost-black backgrounds and near-white text. This stark aesthetic prioritizes clarity within a complex interface, using a restricted palette to focus attention on interactive elements and critical information, allowing for subtle visual depth through varied text weights and button styles.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | neutral | Page backgrounds, critical borders, deepest shadows. |
| Control Panel Black | `#151515` | neutral | Primary surface background for cards and interactive components, providing subtle differentiation from page background. Implies a slightly raised or interactive surface. |
| Deep Graphite | `#222222` | neutral | Alternative background for sections or distinct content blocks, slightly lighter than 'Control Panel Black'. |
| Input Surface Gray | `#333337` | neutral | Background for secondary input fields, indicating an active or editable state within a confined area. |
| Icon Gray | `#6a6a6` | neutral | Default color for inactive or secondary icons, receding into the background. |
| Input Text Gray | `#b4b4b8` | neutral | Placeholder text and secondary informational text within dark input fields. |
| Off White Text | `#eeeef2` | neutral | Primary text color for body copy, links, icons, and interactive elements against dark backgrounds. Provides high contrast without being pure white, reducing eye strain. |
| White Glow | `#ffffff` | neutral | High-contrast text for critical headings, active links, and prominent calls to action, drawing immediate attention. |
| Amber Indicator | `#ffcc00` | accent | Used for specific icon states or to highlight minor interactive elements — a subtle warning or attention signal. |

## Typography

### Neue Haas Grotesk Text Pro

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 11px, 14px, 16px, 18px, 28px, 35px |
| lineHeight | 1.20, 1.50 |
| letterSpacing | -0.013em, -0.017em, -0.026em, -0.033em, -0.040em, -0.042em |
| substitute | Helvetica Neue, Arial |
| role | General text, body copy, subheadings, and some hero elements. Its slightly condensed feel at smaller sizes allows for information density within the dark interface. |

### Neue Haas Grotesk Display Pro

| Key | Value |
| --- | --- |
| weight | 400, 450, 500 |
| sizes | 24px, 31px, 64px |
| lineHeight | 0.95, 1.33, 1.50 |
| letterSpacing | -0.010em, -0.040em |
| substitute | Helvetica Neue, Arial |
| role | Headlines and prominent display text. The tighter letter spacing at larger sizes (e.g., -0.010em at 24px) creates an imposing yet elegant presence, reinforcing the technical and precise aesthetic. |

### NHaasGroteskDSPro

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.00, 1.20 |
| letterSpacing | normal |
| substitute | Helvetica Neue, Arial |
| role | Specific for input fields and button text, prioritizing readability and functional clarity at interactive sizes. |

### digi one v2

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.50 |
| letterSpacing | -0.033em |
| substitute | Dot Digital-7 |
| role | Specialized digital display font for unique branding or data representation, mimicking a retro digital screen, particularly for system readouts or version numbers. Its negative letter spacing is a key characteristic. |

### Analog One V2.

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px |
| lineHeight | 1.25 |
| letterSpacing | normal |
| substitute | Share Tech Mono |
| role | Used for specific input fields, suggesting a monospaced, technical data entry environment. This is a subtle nod to older hardware interfaces. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 | -0.013 |
| body-sm | 14 |  | 1.5 | -0.017 |
| body | 16 |  | 1.5 | -0.026 |
| subheading | 18 |  | 1.2 | -0.033 |
| heading-sm | 24 |  | 1.5 | -0.01 |
| heading | 28 |  | 1.2 | -0.04 |
| heading-lg | 31 |  | 1.33 | -0.04 |
| display | 64 |  | 0.95 | -0.04 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| inputs | 7px |
| buttons | 0px |

- **elementGap** — 4px
- **sectionGap** — 100-140px
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Product Accessory Cards

### Search Input Field

### Call to Action Button

**Role:** Primary user interaction.

Background: Off White Text (#eeeef2), Text: Control Panel Black (#151515). Border Radius: 0px. Padding-top/bottom: 0px. Padding-left/right: 10px. Uses NHaasGroteskDSPro, weight 400, size 16px.

### Default Input Field

**Role:** User data entry.

Background: Control Panel Black (#151515), Text: Off White Text (#eeeef2). Border Radius: 7px. Padding: 10.5px 17.5px. Uses Analog One V2., weight 400, size 14px.

### Search Input Field

**Role:** Site search functionality.

Background: Input Surface Gray (#333337), Text: Input Text Gray (#b4b4b8). Border Radius: 0px. Padding-left: 16px. Uses NHaasGroteskDSPro, weight 400, size 16px.

### Navigation Badge

**Role:** Informational marker for navigation or categories.

Background: transparent (rgba(0, 0, 0, 0)), Text: Off White Text (#eeeef2). Border Radius: 0px. Padding-right: 1.4px. Uses Neue Haas Grotesk Text Pro, weight 400, size 11px.

## Layout

The site uses a full-bleed dark background for the entire page, creating a unified canvas. Content is generally displayed within sections that stack vertically with consistent large gaps (implied 100-140px). Hero sections often feature a full-width image with subtle overlays or prominent typography. Content within sections tends to be left-aligned, often featuring a headline followed by text or interactive elements. There are instances of implied 2-column or 3-column grids for presenting accessories, but the overall presentation is sequential and block-based rather than complex grid layouts. Navigation is a persistent top bar with minimal links and icons, sitting above the full-bleed content.

## Imagery

The visual language focuses on close-up product photography with a strong engineering aesthetic, occasionally featuring a digital display element (e.g. blue waveforms on black screens). Images are often dark, moody, and serve primarily to showcase product details or technical interfaces rather than lifestyle. They are contained, typically not full-bleed, and integrated with the dark theme through color and lighting, reinforcing the high-tech, precision instrument feel. Icons are monochrome, generally filled, with a consistent stroke weight, and have a function-first role for navigation and utility.

## Dos & Donts

### Do

- Use Absolute Zero (#000000) for page backgrounds to establish the deep, dark canvas.
- Apply Control Panel Black (#151515) for main interactive surfaces like cards and elevated sections to create subtle depth.
- Prioritize Off White Text (#eeeef2) for body text and navigation elements against dark backgrounds for optimal readability.
- Employ White Glow (#ffffff) sparingly for critical headlines and active states that demand immediate attention.
- Maintain 0px border-radius for most elements like buttons, reinforcing the sharp, technical aesthetic.
- Utilize Neue Haas Grotesk Text Pro for body text at various sizes (11px-35px) with specific negative letter spacing to achieve a modern, dense textual presentation.
- Use specific digital fonts (digi one v2, Analog One V2.) for any element requiring a retro-tech or numerical display, preserving the unique brand character.

### Don't

- Avoid using bright or overly saturated colors, except for the defined 'Amber Indicator' accent, to maintain the control panel aesthetic.
- Do not introduce rounded corners beyond the 7px radius for specific input fields; maintain sharp edges elsewhere.
- Steer clear of gradients or soft shadows that would soften the hard-edged, precise visual identity.
- Do not use generic system fonts for headlines or special numbers; Neue Haas Grotesk Display Pro and the unique digital fonts are integral to brand recognition.
- Avoid large, airy padding or excessive white space between elements where information density is intended, as the system favors a compact layout.
- Do not break the dark theme with light sections unless specifically defined by brand guidelines, as the overall mood is consistently dark.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #eeeef2 (Off White Text)
- Background: #000000 (Absolute Zero)
- Surface: #151515 (Control Panel Black)
- CTA Background: #eeeef2 (Off White Text)
- CTA Text: #151515 (Control Panel Black)
- Icon: #6a6a6 (Icon Gray)

### Example Component Prompts
1. Create a primary call to action button: `background-color: #eeeef2; color: #151515; border-radius: 0px; padding: 0px 10px; font-family: NHaasGroteskDSPro; font-weight: 400; font-size: 16px; line-height: 1.2;`
2. Design a default input field: `background-color: #151515; color: #eeeef2; border-radius: 7px; padding: 10.5px 17.5px; font-family: Analog One V2.; font-weight: 400; font-size: 14px; line-height: 1.25;`
3. Generate a main page headline for a dark section: `color: #ffffff; font-family: Neue Haas Grotesk Display Pro; font-weight: 400; font-size: 64px; line-height: 0.95; letter-spacing: -0.040em;`
4. Create body text for detailed information: `color: #eeeef2; font-family: Neue Haas Grotesk Text Pro; font-weight: 400; font-size: 14px; line-height: 1.5; letter-spacing: -0.017em;`
5. Implement a branding badge/caption: `background-color: rgba(0, 0, 0, 0); color: #eeeef2; border-radius: 0px; padding: 0px 1.4px; font-family: Neue Haas Grotesk Text Pro; font-weight: 400; font-size: 11px; line-height: 1.5;`

---
_Source: https://styles.refero.design/style/6b5a0bf4-3d2a-4c3b-aa2e-652f1acb82c0_
