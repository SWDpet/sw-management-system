# Readymag — Design Reference

> Vibrant digital gallery. Each content block is a self-contained, high-contrast visual statement.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://readymag.com](https://readymag.com) |
| Refero page | [https://styles.refero.design/style/1287abc9-da90-410d-a997-96b8b11ad646](https://styles.refero.design/style/1287abc9-da90-410d-a997-96b8b11ad646) |
| Theme | light |
| Industry | design |

## Overview

This design system is a dynamic canvas, blending stark, high-contrast typography with an unpredictable, vibrant color palette. It feels like a digital art gallery, designed for maximal visual impact where each content block is an independent exhibit. The deliberate clash of intense, vivid colors with a predominantly achromatic base creates a playful yet assertive atmosphere, eschewing traditional corporate polish for expressive, almost rebellious, visual communication.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Primary page and card backgrounds, primary body text background. |
| Type Black | `#000000` | neutral | Primary text color for headings and body, icon fills. |
| Charcoal Text | `#282828` | neutral | Secondary text and body copy, subtle borders. |
| Surface Gray | `#f4f4f4` | neutral | Button backgrounds, subtle background accents. |
| Light Gray | `#e7e7e7` | neutral | Section backgrounds, graphic elements. |
| UI Gray | `#808080` | neutral | Subtle icons, supporting text. |
| Accent Grape | `#8800ff` | brand | Prominent headings and graphic elements, indicating key brand messages with a bold stroke. |
| Electric Violet | `#2c0fb1` | brand | Borders and graphic accents, providing a deep, energetic contrast. |
| Sunset Orange | `#ec520b` | brand | Primary call-to-action buttons, drawing immediate attention. |
| Warning Orange | `#ff5000` | accent | Highlights within graphic elements, secondary buttons. |
| Vivid Yellow | `#ffcc00` | accent | Backgrounds for distinctive sections, graphic elements, imparting a cheerful, active feel. |
| Forest Green | `#b2cc00` | accent | Decorative backgrounds, graphic elements, introducing organic vitality. |
| Deep Teal | `#00362b` | accent | Section backgrounds, lending depth and sophistication to content areas. |
| Warning Red | `#ff0000` | accent | Highlighting specific graphic elements, drawing emphatic attention. |

## Typography

### -apple-system

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.00, 1.25 |
| letterSpacing | -0.006em |
| fontFeatureSettings | "dlig" 0, "hlig" 0, "liga" 0, "rlig" 0, "smcp" 0 |
| substitute | system-ui |
| role | Default system font for body text, links, and various UI elements. Its neutrality allows the custom fonts to stand out. Default letter spacing is slightly tighter than normal for a concise appearance. |

### custom_37866

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 12px, 14px, 18px, 30px, 32px, 40px, 80px |
| lineHeight | 1.00, 1.43, 1.83, 2.67, 2.70 |
| letterSpacing | -0.025em, -0.027em, -0.014em, -0.053em, -0.044em, -0.067em, -0.050em |
| fontFeatureSettings | "dlig" 0, "hlig" 0, "liga" 0, "rlig" 0, "smcp" 0 |
| substitute | Helvetica Neue, Arial |
| role | Primary brand typeface for all headlines, subheadings, and emphasized text. Its unique character, especially with the aggressively tight letter spacing at larger sizes, creates a distinct, modern, and sometimes avant-garde feel. The varied letter spacing across sizes is a key aesthetic choice. |

### Graphik

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 16px |
| lineHeight | 1.50, 2.00 |
| letterSpacing | -0.002em, -0.003em |
| substitute | Graphik |
| role | Used for specific body text and links, providing a clean, geometric sans-serif contrast to the more expressive primary custom font. Its generous line-heights make smaller text blocks readable. |

### xgnl

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 16px |
| lineHeight | 0.88 |
| letterSpacing | -0.056em |
| fontFeatureSettings | "dlig" 0, "hlig" 0, "liga" 0, "rlig" 0, "smcp" 0 |
| substitute | Impact, Anton |
| role | A highly distinctive, bold, and condensed typeface used sparingly for impactful small text elements or experimental typographic treatments, creating moments of visual surprise due to its extremely tight letter spacing and low line height. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 2.7 | -0.25 |
| detail | 14 |  | 2.67 | -0.378 |
| body-alt | 16 |  | 2 | -0.048 |
| subheading | 18 |  | 1.83 | -0.252 |
| heading-sm | 30 |  | 1 | -1.59 |
| heading | 32 |  | 1.43 | -1.408 |
| heading-lg | 40 |  | 1 | -2.68 |
| display | 80 |  | 1 | -4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 10px |
| buttons | 200px |
| modules | 20px |
| illustrations | 16px |

- **elementGap** — 6px
- **sectionGap** — 
- **cardPadding** — 0px
- **pageMaxWidth** — 

## Components

### Call-to-Action Hero Headline Block

### Feature Cards Row — Attract & Streamline

### Navigation Pill Bar

### Primary Call to Action Button

**Role:** Critical user actions to advance through the site.

Rounded pill shape with `200px` border-radius. Background is `Sunset Orange (#ec520b)` with `Type Black (#000000)` text, shifting to `Canvas White (#ffffff)` on hover. Padding is `0px` top/bottom, `24px` left/right.

### Pill Navigation Button

**Role:** Main navigation and secondary actions in headers.

Rounded pill shape with `200px` border-radius. Background is `Surface Gray (#f4f4f4)` with `Type Black (#000000)` text. Padding is `0px` top/bottom, `24px` left/right.

### Ghost Navigation Link

**Role:** Minimalist interactive text links.

No background, `Charcoal Text (#282828)` text, no border. Padding is `0px` all around. Uses system font `-apple-system`.

### Unstyled Card

**Role:** Content containers that rely on background color for differentiation.

Background `Canvas White (#ffffff)`, `0px` border-radius, no box-shadow. Padding is `0px` all around, content provides its own spacing.

### Hero Headline

**Role:** Prominent, attention-grabbing titles.

Uses `custom_37866` font, `80px` size, `weight 400`, `line-height 1.00`, `letter-spacing -0.050em`. Color is `Type Black (#000000)`.

### Interactive Block Headline

**Role:** Headlines within interactive cards or sections.

Uses `custom_37866` font, `40px` size, `weight 400`, `line-height 1.00`, `letter-spacing -0.067em`. Color is `Type Black (#000000)` or `Canvas White (#ffffff)` depending on background.

## Layout

The page exhibits a highly dynamic and sectioned layout, alternating between full-bleed background sections and content blocks with varying implicit maximum widths. The hero section often features a centered headline over a visually dense, collage-like background. Sections are delineated by dramatic shifts in background color, providing visual breaks rather than relying on consistent vertical spacing. Content arrangement frequently uses a mix of centered stacks, text blocks, and full-width banners. There is no rigid grid system explicitly visible, rather a fluid arrangement of diverse content types. Navigation is a sticky top bar, minimal for core actions. A floating '5' interactive element suggests strong spatial interaction.

## Imagery

Imagery is highly abstract and often serves as a backdrop or decorative element rather than a direct content focus. Product screenshots are minimal, focusing on UI elements in isolation. There is a strong emphasis on stark, geometric shapes and typographic art as visual content. Photography, when present, is often part of a collage or a design sample within the product showcase, not lifestyle imagery. Icons are minimal, mostly `Type Black (#000000)` filled, with a blocky, bold outline. The visual density of images varies, with some sections being very image-heavy and others completely text-dominant, creating a dynamic rhythm.

## Dos & Donts

### Do

- Always use `200px` border-radius for primary and secondary action buttons to maintain the pill shape.
- Utilize `custom_37866` with its distinctive tight letter spacing (e.g., `-0.050em` at `80px`) for all major headlines to create an impactful typographic statement.
- Employ the vivid brand and accent colors (e.g., `Accent Grape #8800ff`, `Vivid Yellow #ffcc00`, `Deep Teal #00362b`) as full-bleed section backgrounds to delineate content blocks.
- Prioritize `Sunset Orange (#ec520b)` for all high-priority call-to-action buttons.
- Maintain high contrast text on backgrounds: `Type Black (#000000)` on `Canvas White (#ffffff)` and `Canvas White (#ffffff)` on any vivid background color.
- Use `0px` padding on card components, allowing internal content to define its own layout and spacing.

### Don't

- Never use soft, muted accent colors; the palette is defined by high-chroma, vivid hues.
- Avoid generic box-shadows for elevation; rely on stark shifts in background color between sections for visual depth.
- Do not use subtle letter-spacing for headlines; the aggressive negative letter spacing of `custom_37866` is a signature of this system.
- Do not constrain content to a fixed page width globally; sections should alternate between full-bleed and contained elements.
- Avoid excessive use of `-apple-system` for headlines; reserve it for body text where neutrality is desired.
- Do not use subtle border-radius values below `10px` for UI components; the system favors either sharp 0px corners or distinctly rounded forms like `10px`, `16px`, `20px` or `200px`.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: `#000000` (Type Black)
- Background: `#ffffff` (Canvas White)
- CTA: `#ec520b` (Sunset Orange)
- Border (accent): `#2c0fb1` (Electric Violet)
- Accent (graphic): `#8800ff` (Accent Grape)

### 3-5 Example Component Prompts
1. Create a primary call to action button: `Sunset Orange (#ec520b)` background, `Canvas White (#ffffff)` text, `200px` border-radius, `0px` top/bottom padding, `24px` left/right padding. Text label 'Get Started'.
2. Design a hero section headline: `custom_37866` font, `80px` size, `weight 400`, `line-height 1.00`, `letter-spacing -0.050em`, `Type Black (#000000)` color. Text content 'Design Freely'.
3. Generate a section background div: `Vivid Yellow (#ffcc00)` as the background color, with `86px` right margin and `86px` left margin. No border, `0px` padding.
4. Create a minimalist navigation link: `-apple-system` font, `16px` size, `weight 400`, `line-height 1.25`, `Charcoal Text (#282828)` color. Text 'Solutions'.
5. Implement an interactive block headline: `custom_37866` font, `40px` size, `weight 400`, `line-height 1.00`, `letter-spacing -0.067em`, `Canvas White (#ffffff)` color.

---
_Source: https://styles.refero.design/style/1287abc9-da90-410d-a997-96b8b11ad646_
