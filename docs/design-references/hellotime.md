# Hellotime — Design Reference

> White canvas, graphite precision

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.hellotime.io](https://www.hellotime.io) |
| Refero page | [https://styles.refero.design/style/dbc5ecba-7309-456f-93b4-4356c6b0d293](https://styles.refero.design/style/dbc5ecba-7309-456f-93b4-4356c6b0d293) |
| Theme | light |
| Industry | productivity |

## Overview

Hellotime projects a focused, clean interface with strong typographic hierarchy on a bright, minimalist canvas. Key interactions are highlighted by a deep, dark graphite, while subtle accent colors are reserved for internal product elements. Rounded corners provide a friendly contrast to the otherwise structured layout, maintaining an accessible and efficient feel. The overall impression is one of clarity and quiet authority, supporting a productivity application.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Primary page backgrounds, default surface color, subtle highlights and text for dark buttons |
| Fog Gray | `#f3f3f5` | neutral | Secondary surface background for cards and prominent UI blocks, creating subtle visual separation |
| Border Ash | `#e1e2e5` | neutral | Hairline borders for containers, dividers, and subtle background shading for active components |
| Graphite Ink | `#151619` | neutral | Primary text, bold headlines, strong icon colors, and essential UI elements for high contrast |
| Steel Gray | `#7f8491` | neutral | Secondary text, muted links, and subtle borders, providing hierarchical contrast for less prominent information |
| Slate Blue | `#c8cad0` | neutral | Soft icon strokes, subtle dividers, and low-emphasis decorative details. Do not promote it to the primary CTA color |
| Charcoal Button | `#25272d` | neutral | Filled button backgrounds, creating strong, actionable calls to attention against the light canvas |
| Accent Green | `#059669` | accent | Green outline accent for tags, dividers, and focused UI edges |
| Link Gray | `#363940` | neutral | Navigation text color |
| Button Border | `#b0b3bb` | neutral | Medium-contrast borders, control outlines, and structural separators. Do not promote it to the primary CTA color |

## Typography

### SF Pro Display

| Key | Value |
| --- | --- |
| weight | 500, 600, 700 |
| weights | 500, 600, 700 |
| sizes | 24px, 40px, 48px, 64px, 80px |
| lineHeight | 0.90, 1.00, 1.33, 1.50 |
| letterSpacing | normal |
| substitute | Inter |
| role | Primary headlines and display text, used for impact and visual hierarchy. Its varied weights allow for flexible emphasis. |

### SF Pro Text

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 20px |
| lineHeight | 0.80, 1.00, 1.14, 1.20, 1.33, 1.50, 1.60 |
| letterSpacing | normal |
| substitute | Inter |
| role | Body text, links, navigation, and detailed interface labels. Its extensive range of sizes and line heights supports a comfortable reading experience across different information densities. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.5 | 0 |
| body | 16 |  | 1.5 | 0 |
| subheading | 24 |  | 1.33 | 0 |
| heading-sm | 40 |  | 1 | 0 |
| heading | 48 |  | 1 | 0 |
| heading-lg | 64 |  | 0.9 | 0 |
| display | 80 |  | 0.9 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| body | 12px |
| cards | 16px |
| links | 12px |
| buttons | 8px |
| headings | 20px |
| navigation | 8px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Base page background, light frames for main content areas. |
| Fog Gray | `#f3f3f5` | 1 | Secondary background for cards, feature blocks, and distinct content sections. |
| Border Ash | `#e1e2e5` | 2 | Very subtle background for hovered states or very light bordering elements; visually above Fog Gray but rarely a full surface. |

## Components

### Primary Filled Button

**Role:** Call-to-action button for initiating key user flows.

Background: Charcoal Button (#25272d), Text: Canvas White (#ffffff). Padding: 16px vertical, 16px horizontal. Border Radius: 8px. Font: SF Pro Text, weight 400.

### Ghost Button

**Role:** Secondary action or navigational link with minimal visual weight.

Background: transparent, Text: Graphite Ink (#151619). Padding: 8px vertical, 12px horizontal. No border radius. No border.

### Outlined Button

**Role:** Tertiary action with a clear boundary but less prominence than a filled button.

Background: Canvas White (#ffffff), Text: Graphite Ink (#151619). Border: 1px solid Button Border (#b0b3bb). Padding: 10px vertical, 10px horizontal. Border Radius: 8px.

### Content Card

**Role:** Container for grouped information, feedback, or features.

Background: Fog Gray (#f3f3f5). Padding: 32px all around. Border Radius: 16px. No shadow. Used for testimonials and feature blocks.

### Timeline Card

**Role:** Specific card variant for product UI, demonstrating a feature.

Background: Fog Gray (#f3f3f5). Padding: 56px vertical, 32px horizontal. Border Radius: 16px 0px 0px 16px. No shadow.

### Minimal Card

**Role:** Transparent container, often for layout purposes without strong visual separation.

Background: transparent. No padding, no border radius, no shadow.

## Layout

The page primarily uses a centered, max-width layout for content, though the exact max-width is not explicitly defined, it maintains generous horizontal padding. The hero section features a prominent, centered headline with a highlighted phrase over a clean white background. Sections alternate between full-width content blocks and more contained arrangements. Content commonly flows in 2-column text-left/visual-right or visual-left/text-right patterns, with ample vertical spacing (40px section gaps) between blocks. Testimonials are presented in a 2-column card grid. The navigation is a sticky top bar with left-aligned branding and right-aligned actions.

## Imagery

The site largely avoids decorative photography, focusing on product screenshots and minimalist icons. Product screenshots are contained within UI components, often featuring data visualizations on a white or light gray background, suggesting functional clarity. Icons are outlined, simple, and monochrome (Graphite Ink #151619), integrated directly into text. A few playful, stylized illustrations with thick outlines and simple forms (like the superhero mascot) offer brand personality without overwhelming the UI. Imagery serves an explanatory or functional role, product showcase, and is sparse relative to text.

## Dos & Donts

### Do

- Prioritize Graphite Ink (#151619) for all primary text and headlines to maintain high contrast with light backgrounds.
- Use Fog Gray (#f3f3f5) for distinct UI sections, such as cards or data display areas, maintaining visual separation from the Canvas White (#ffffff) page background.
- Apply 16px border radius to all content cards and larger container elements, and 8px to buttons and navigation items for consistency.
- Reserve Accent Green (#059669) exclusively for small, functional highlights within product UI, not for primary actions or branding.
- Employ consistent 32px padding for content cards and 16px for button vertical padding.
- Use Charcoal Button (#25272d, with Canvas White text) for primary calls-to-action, ensuring they stand out visually.
- Maintain a clear visual hierarchy: SF Pro Display for headlines and SF Pro Text for all body copy and UI labels.

### Don't

- Do not introduce new saturated colors for interactive elements; rely on variations of Graphite Ink or Charcoal Button for primary interactions.
- Avoid arbitrary shadow usage; the design system emphasizes flat surfaces and clear typographic hierarchy over dimensional elevation.
- Do not deviate from the established border radii; larger elements use 16px, interactive elements use 8px, and some decorative use 12px or 20px.
- Do not use Accent Green (#059669) for anything other than specific, non-actionable highlights within the product interface.
- Avoid using multiple font sizes/weights for body text within the same paragraph; stick to SF Pro Text at 16px/400 for general body content.
- Do not use full-width sections without a maximum content width; center content within a defined max-width.
- Avoid bold text as a substitute for semantic headings; use appropriate SF Pro Display sizes and weights for titles.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #151619
- background: #ffffff
- border: #e1e2e5
- accent: #059669
- primary action: #25272d (filled action)

### 3-5 Example Component Prompts
1. Create a Primary Action Button: #25272d background, #f3f3f5 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a content card for a testimonial: Fog Gray background (#f3f3f5), 16px border-radius. Inside, body text '"Hellotime is the best capacity planning tool I have ever used"' using SF Pro Text 16px, Graphite Ink (#151619). Padding 32px all sides.
3. Create a navigation bar: Canvas White background, sticky at top. Left-aligned brand logo (use a placeholder icon), right-aligned Ghost Button with text 'About' (Graphite Ink text, transparent background, 8px vertical 12px horizontal padding).

---
_Source: https://styles.refero.design/style/dbc5ecba-7309-456f-93b4-4356c6b0d293_
