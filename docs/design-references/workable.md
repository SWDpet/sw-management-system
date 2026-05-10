# Workable — Design Reference

> Clean canvas, purposeful accents

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.workable.com](https://www.workable.com) |
| Refero page | [https://styles.refero.design/style/0ab4c544-6147-4998-8365-3a0f6191e54f](https://styles.refero.design/style/0ab4c544-6147-4998-8365-3a0f6191e54f) |
| Theme | light |
| Industry | saas |

## Overview

Workable employs a direct and dynamic visual language, built on a clean canvas with a distinctive teal and dark forest green palette. Components are designed to be lightweight, with rounded corners that soften the strong typographic choices. Color is used purposefully for clear accents, interactive states, and to differentiate content blocks, while maintaining an overall sense of order and professionalism. The system balances functional clarity with subtle visual interest, avoiding heavy ornamentation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Porcelain | `#fff5ee` | neutral | Page background, primary light surface |
| White | `#ffffff` | neutral | Card backgrounds, elevated UI elements |
| Midnight Ink | `#0f161e` | neutral | Primary text, strong headings, dark UI elements |
| Harbor Mist | `#333942` | neutral | Subtle text, muted links, secondary information |
| Forest Canopy | `#012620` | brand | Dark section backgrounds, hero background, decorative fills |
| Deep Teal | `#004038` | brand | Primary text color for navigation and headings, outlined button borders, active states |
| Fresh Teal | `#00f5dc` | accent | Card backgrounds, tag backgrounds, vibrant accents; Key product graphic fills, vibrant UI elements |
| Muted Sage | `#00544c` | brand | Secondary text, sub-brand accents, borders |
| Soft Peach | `#fde8ce` | accent | Informational card backgrounds, subtle highlight surfaces |
| Muted Mandarin | `#ffdcbf` | accent | Accent card backgrounds |
| Sky Haze | `#bee9f4` | accent | Accent card backgrounds |
| Lime Glow | `#d5ff4d` | accent | Decorative stroke, vibrant highlighting in illustrations |
| Spring Bud | `#7edcaf` | accent | Highlight text, decorative fills and borders |

## Typography

### Proxima Nova

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 16px, 18px, 20px, 24px, 32px, 56px, 72px |
| lineHeight | 1.00, 1.13, 1.14, 1.17, 1.20, 1.22, 1.25, 1.38, 1.50, 1.56, 1.75 |
| letterSpacing | normal |
| substitute | Open Sans |
| role | Primary UI typeface for all content including navigation, body text, headings, and buttons. Its clean, sans-serif structure provides clarity and a modern feel. |

### Source Serif Pro

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 24px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| substitute | Merriweather |
| role | Used sparingly for specific body copy elements, offering a contrasting serif touch. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 16 |  | 1.5 | 0 |
| body | 18 |  | 1.5 | 0 |
| subheading | 20 |  | 1.25 | 0 |
| heading-sm | 24 |  | 1.22 | 0 |
| heading | 32 |  | 1.17 | 0 |
| heading-lg | 56 |  | 1.13 | 0 |
| display | 72 |  | 1 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| badges | 25px |
| buttons | 16px |
| navigation | 8px |

- **elementGap** — 8px
- **sectionGap** — 32px
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Porcelain | `#fff5ee` | 0 | Base page background |
| White | `#ffffff` | 1 | Primary card and elevated component background |
| Soft Peach | `#fde8ce` | 2 | Accentuated card backgrounds for differentiation |
| Muted Mandarin | `#ffdcbf` | 3 | Secondary accent card background |
| Sky Haze | `#bee9f4` | 4 | Tertiary accent card background |

## Components

### Primary Ghost Button

**Role:** Call to action with minimal visual weight

Background transparent, text color #0f161, 0px border-radius, no padding defined. Best for inline actions or secondary CTA when a filled button is elsewhere.

### Secondary Ghost Button

**Role:** Outlined action with rounded corners

Background transparent, text color #0f161, 16px border-radius. Often used for navigation CTAs.

### Default Card

**Role:** Content container for features or information blocks

Background #ffffff, 16px border-radius, 32px padding on all sides. No shadow.

### Highlight Card - Soft Peach

**Role:** Emphasized content container with a warm background tint

Background #fde8ce, 16px border-radius, 32px padding on all sides. No shadow.

### Highlight Card - Fresh Teal

**Role:** Emphasized content container with a vivid background tint

Background #00f5dc, 16px border-radius, 32px padding on all sides. No shadow.

### Highlight Card - Muted Mandarin

**Role:** Emphasized content container with a warm orange background tint

Background #ffdcbf, 16px border-radius, 32px padding on all sides. No shadow.

### Ghost Badge

**Role:** Informational tag or label

Background transparent, text color #0f161, 0px border-radius, no padding defined. Used for meta-information.

### Navigation Link Button

**Role:** Actionable link within navigation

Text color #0f161, 16px border-radius, 0px padding. Used for 'Log in' and 'Request a demo'.

### Contained Navigation Button

**Role:** The primary call to action in the navigation bar

Background #004038, text color white, 16px border-radius. This is a filled button, contrasting with the ghost type.

## Layout

The page primarily uses a full-bleed structure, with content sections extending across the viewport width, though a clear implicit max-width ensures readability. Hero sections often feature a full-bleed background (e.g., Forest Canopy) with centered headings. Content typically alternates between two-column layouts (text left, image right) and centered stacks. Feature sections use a 3-column card grid. Vertical spacing between sections is consistent at 32px, creating a comfortable yet information-dense rhythm. The navigation is a persistent top bar featuring a logo, product/pricing links, and two call-to-action buttons, maintaining a fixed presence.

## Imagery

This system primarily uses photography for human elements (diverse faces in cards) and abstract, colorful gradients for product-focused graphics. Photography is typically tightly cropped to faces, conveying a relatable human connection. Illustrations are characterized by abstract shapes and bold gradients, often resembling fluid organic forms rather than hard-edged geometry. Icons (when visible) are typically outlined with a moderate stroke weight, emphasizing clarity and lightness. Imagery serves both decorative atmosphere, product showcase, and providing human context within the UI. The density of imagery is moderate, used to break up text and add visual interest, rather than overwhelming the layout.

## Dos & Donts

### Do

- Use Proxima Nova for all text elements to maintain typographic consistency.
- Apply 16px border-radius to all cards and buttons for a unified, soft edge.
- Utilize Forest Canopy (#012620) for dark section backgrounds and Deep Teal (#004038) for primary action outlines or filled navigation buttons.
- Employ 32px padding for internal card content and around main section elements.
- Maintain an 8px elementGap between smaller UI components for comfortable dense layouts.
- Prioritize Canvas Porcelain (#fff5ee) as the primary page background to create a clean, light base.
- Use Fresh Teal (#00f5dc) and Soft Peach (#fde8ce) as background tints for cards to visually group or highlight content.

### Don't

- Avoid arbitrary color usage; reserve brand and accent colors for functional roles or distinct highlights, not general decoration.
- Do not introduce complex shadows or extreme elevation; the design favors flat surfaces and subtle distinctions.
- Refrain from using overly decorative fonts; stick to Proxima Nova and Source Serif Pro for a clear, modern appearance.
- Do not deviate from the established 16px and 8px border-radii; random smaller or larger radii will break visual cohesion.
- Avoid dense, unbroken blocks of text; break content with headings, lists, and visual components.
- Do not use dark backgrounds for general page content; restrict them to hero sections or distinct visual breaks.
- Refrain from using system default link colors; ensure all links use either Midnight Ink (#0f161e) or Harbor Mist (#333942) unless an explicit accent link style is defined.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #0f161e
background: #fff5ee
border: #0f161e
accent: #00f5dc
primary action: #004038 (outlined action border)

### 3-5 Example Component Prompts
1. Create a hero section with a Forest Canopy (#012620) background. Headline 'The future-ready HR platform' using Proxima Nova, size 56px, weight 700, color white, lineHeight 1.13. Subtext 'Redefining HR...' using Proxima Nova, size 18px, weight 400, color white, lineHeight 1.5. Include a ghost button 'Request a demo' (background transparent, text #0f161e, 0px radius) and a contrasting filled button 'Start a free trial' (background #004038, text white, 16px radius, padding 11px 24px).
2. Design a feature card: Default Card (background #ffffff, 16px radius, 32px padding). Heading 'Talent CRM database' using Proxima Nova, size 24px, weight 700, color #0f161e. Body text 'Manage your talent pool...' using Proxima Nova, size 16px, weight 400, color #333942.
3. Implement a navigation bar: Background transparent. Logo in Deep Teal (#004038). Navigation links ('Product', 'Pricing') text in Midnight Ink (#0f161e), Proxima Nova, size 16px, weight 400. 'Log in' button as a secondary ghost button (background transparent, text #0f161e, 16px radius, padding 12px 24px). 'Request a demo' button as a secondary ghost button (background transparent, text #0f161e, 16px radius, padding 12px 24px). 'Start a free trial' button as a contained navigation button (background #004038, text white, 16px radius, padding 12px 24px).
4. Create a highlight card showing an applicant: Highlight Card - Soft Peach (background #fde8ce, 16px radius, 32px padding). Text 'Applicant tracking system' using Proxima Nova, size 16px, weight 700, color #0f161e. Use an image of a person (16px radius).

---
_Source: https://styles.refero.design/style/0ab4c544-6147-4998-8365-3a0f6191e54f_
