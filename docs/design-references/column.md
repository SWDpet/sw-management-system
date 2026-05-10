# Column — Design Reference

> Architectural blueprint on white marble. Subtle background patterns convey structure beneath a pristine, luminous surface, punctuated by precise, high-contrast markers.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://column.com](https://column.com) |
| Refero page | [https://styles.refero.design/style/a76ec6ba-20b3-495c-9d89-1e58281e79e7](https://styles.refero.design/style/a76ec6ba-20b3-495c-9d89-1e58281e79e7) |
| Theme | light |
| Industry | fintech |

## Overview

Column's design is a blend of corporate authority and digital precision. It uses a very light, almost invisible dotted grid background to evoke a technical blueprint, while maintaining a clean, spacious feel. The deep-seated violet and stark orange accents provide clear interactive points set against a largely monochromatic text palette, suggesting seriousness and innovation. Strategic use of subtle box shadows and inner borders adds dimensionality without heavy handedness, like layers of frosted glass on a complex instrument.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink Blue | `#011821` | neutral | Primary text color for headings and body copy, grounding the design. |
| Code Black | `#000000` | neutral | Used for critical text elements and strong outlines, providing maximum contrast and emphasis. |
| Ghost White | `#ffffff` | neutral | Primary background for pages and cards, fostering a sense of openness and purity. |
| Fog Gray | `#f6f6f8` | neutral | Secondary background for sections and subtle content groupings, almost imperceptibly off-white to provide visual separation. |
| Steel Gray | `#e3e4e8` | neutral | Subtle borders and separators, defining boundaries without starkness. |
| Charcoal Text | `#232730` | neutral | Text on lighter backgrounds for softer contrast than pure black, often used in navigation and body text. |
| Slate Text | `#7c7f88` | neutral | Contextual body text and secondary labels, offering readability without competing with primary elements. |
| Graphite | `#12161` | neutral | Darker shades for text and icons, particularly on hero sections, for depth. |
| Deep Plum | `#111a4a` | brand | Brand accent for interactive elements, icons, and significant textual highlights, establishing Column's brand identity. |
| Action Orange | `#ec652b` | accent | Primary call-to-action buttons and key interactive states, drawing immediate attention. |
| Soft Horizon Gradient | `#d65620` | brand | Decorative background for banners, imbuing sections with a sense of expansive, technological potential. |
| Faded Grid Blue | `#023247` | brand | Illustrative elements and background patterns, adding a subtle technical depth. |
| Success Moss | `#44b48b` | semantic | Status indicators and affirmation icons, providing a clear visual cue for positive outcomes. |
| Radial Twilight Gradient | `#771c86` | brand | Subtle, localized background gradient to highlight specific sections with a deep, cosmic feel. |
| Info Blue | `#7ea7e9` | semantic | Informational graphics and secondary accents, providing a cooler tonal balance. |
| Callout Cyan | `#167e6c` | accent | Highlight elements and secondary interactive states, offering a complementary accent to the orange. |
| Notification Teal | `#88deeb` | accent | Subtle highlights and supporting iconography, a brighter counterpart to Callout Cyan. |

## Typography

### SuisseIntl

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600 |
| weights | 300, 400, 500, 600 |
| sizes | 11px, 12px, 14px, 16px, 18px, 20px, 24px, 28px, 40px, 48px, 52px, 60px |
| lineHeight | 1.00, 1.10, 1.30, 1.33, 1.38, 1.40, 1.43, 1.50 |
| letterSpacing | -0.03em, -0.02em, -0.01em |
| fontFeatureSettings | "salt" 2 |
| substitute | Inter |
| role | Primary typeface for all main content, headings, and UI elements. Its clean, slightly compressed letterforms convey efficiency and sophistication. Weight 600 at 48px headlines commands attention with quiet confidence; weight 300 for subheadings at 28px adds a subtle, approachable gravity. The variable letter-spacing (up to -0.03em at larger sizes) tightens display text for visual impact. |

### SFMono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 10px, 12px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| fontFeatureSettings | "cv11"; "salt" 2 |
| substitute | IBM Plex Mono |
| role | Monospaced font for code snippets, financial figures, and technical labels, ensuring precise alignment and clarity for data-driven content. Its consistent width emphasizes the accuracy inherent in banking operations. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 10px, 12px, 14px, 16px, 20px, 24px |
| lineHeight | 1.00, 1.10, 1.14, 1.33, 1.50 |
| letterSpacing | -0.03em, -0.02em |
| fontFeatureSettings | "cv11" |
| role | Secondary typeface, primarily for detailed body text and supporting UI elements. Provides similar legibility to SuisseIntl but with slightly more open letterforms at smaller sizes, aiding readability in high-information densities. |

### SuisseIntlMono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 10px, 12px, 14px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| fontFeatureSettings | "cv11"; "salt" 2 |
| substitute | IBM Plex Mono |
| role | Specialized monospaced font for technical outputs and code display within the UI, complementing SFMono for specific interactive code contexts. Its presence reinforces the developer-centric aspect of Column. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0 |
| body | 14 |  | 1.5 | -0.28 |
| subheading | 18 |  | 1.4 | -0.36 |
| heading-sm | 24 |  | 1.33 | -0.48 |
| heading | 40 |  | 1.1 | -0.8 |
| display | 48 |  | 1 | -1.44 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| subtle | 2px |
| buttons | 8px |
| default | 8px |

- **elementGap** — 
- **sectionGap** — 48px
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Account Balance Card

### Testimonial + Feature Badge Card

### Primary Navigation Link

**Role:** Navigation

Text link with `SuisseIntl` 16px weight 400, `Ink Blue` (#011821) text. No explicit padding or background, relying on layout for spacing. Hover state changes color to a darker shade.

### Ghost Button - Inverted

**Role:** Secondary interaction in dark contexts

Button with `rgba(1, 24, 33, 0.01)` background, `c1e8ef` text, `SuisseIntl` 14px weight 400. `8px` border-radius, `8px` vertical padding, `12px` horizontal padding. Subtle `Steel Gray` (#e3e4e8) border.

### Ghost Button - Light

**Role:** Secondary interaction in light contexts

Button with `rgba(255, 255, 255, 0.25)` background, `Code Black` (#000000) text, `SuisseIntl` 12px weight 400. `8px` border-radius, `6px` vertical padding, `12px` horizontal padding. No border, relies on transparency and light text.

### Nav Button - Light Text

**Role:** Interaction in transparent sections

Button with `rgba(255, 255, 255, 0.5)` background, `Charcoal Text` (#232730) text, `SuisseIntl` 14px weight 400. `8px` border-radius, `0px` vertical padding, `16px` horizontal padding. `Steel Gray` (#e3e4e8) border.

### Card - Callout Orange

**Role:** Prominent information highlight

Card with `Action Orange` (#ec652b) background, `6px` border-radius. Shadow `rgba(0, 0, 0, 0.1) 0px 4px 8px 0px, rgba(0, 0, 0, 0.1) 0px 2px 4px 0px, rgba(0, 0, 0, 0.25) 0px 1px 1px 0px`. `20px` padding on all sides. Designed to draw attention to key metrics or statements.

### Badge - Transparent

**Role:** Categorization or small label

Transparent background badge, `Code Black` (#000000) text. No border-radius or padding specified, implying inline usage or minimal styling.

### Secondary Button - Outlined

**Role:** Alternative interaction

Button with `rgba(0, 0, 0, 0)` background, `Deep Plum` (#111a4a) text, `SuisseIntl` 16px weight 400. `8px` border-radius, `12px` vertical padding, `32px` horizontal padding. Border color matches `Deep Plum`.

## Layout

The page uses a `max-width` contained layout, centered on the screen, around a primary content width of approximately 1200px based on observable component widths and spacing. The hero section is a full-bleed, almost white background with a subtle dotted grid pattern creating a technical blueprint aesthetic (`Faded Grid Blue`). Content sections alternate between `Ghost White` and `Fog Gray` backgrounds, maintaining a consistent vertical `sectionGap` of 48px. Content is typically arranged in left-aligned blocks or two-column layouts where text is on one side and a product screenshot or graphic is on the other. Feature sections often use a grid of cards (implied 2-3 column from screenshots). The overall impression is spacious and organized, prioritizing information clarity over dense visual elements.

## Imagery

This site prominently features abstract, technical graphics and product UI screenshots. The graphics often depict dotted world maps or wireframe-like structures, usually in subdued blues and grays (`Faded Grid Blue`). Product UI screenshots are contained within card-like components, often with subtle drop shadows, showcasing financial data and code snippets with `SFMono` or `SuisseIntlMono` for a developer-centric feel. There are no lifestyle photos or complex illustrations; the imagery is functional and explanatory, reinforcing the brand's focus on backend infrastructure and developer tools. Visual density is low, with imagery serving more an atmospheric and explanatory role rather than a dominant content one.

## Dos & Donts

### Do

- Use `Fog Gray` (#f6f6f8) for secondary section backgrounds to create subtle visual breaks, not just `Ghost White` (#ffffff).
- Apply `SuisseIntl` with a negative letter-spacing (-0.02em to -0.03em) for all headlines 28px and larger, tightening the text for a refined, modern feel.
- Borders on interactive elements should primarily use `Steel Gray` (#e3e4e8), providing definition without harshness.
- All cards and buttons should consistently use an `8px` border-radius for a soft, approachable geometry, except when specific components dictate otherwise.
- Emphasize critical actions with the `Action Orange` (#ec652b) background, reserving `Deep Plum` (#111a4a) primarily for non-primary interactive elements and brand accents.
- Use `SFMono` or `SuisseIntlMono` at 10-12px for all numerical data and code snippets to ensure alignment and technical precision.
- Enhance surface depth with the subtle card shadow: `rgba(17, 26, 74, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 1px 2px 0px, rgba(255, 255, 255, 0.5) 0px 0px 0px 1px inset`.

### Don't

- Do not use generic blue for primary interactive elements; save `Info Blue` (#7ea7e9) for graphics and non-actionable information to prevent dilution of `Deep Plum` and `Action Orange`.
- Avoid arbitrary uses of vivid colors; `Success Moss` (#44b48b) and `Notification Teal` (#88deeb) are reserved for semantic feedback, not decorative elements.
- Do not deviate from the `8px` default border-radius for primary UI elements across buttons and cards; exceptions are only for badges or specific component variants.
- Do not apply heavy, opaque box-shadows; the system relies on subtle, layered shadows to suggest depth and elevation.
- Avoid using `Code Black` (#000000) for large blocks of text; opt for `Ink Blue` (#011821) or `Charcoal Text` (#232730) for better readability and a softer appearance.
- Do not introduce new typefaces; `SuisseIntl` is for visual impact and headings, `Inter` for general readability, and monospaced fonts for technical context.
- Do not break the established vertical rhythm of 48px `sectionGap` and `24px` `elementGap` in content arrangements; maintain spaciousness.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (primary): `#011821` (Ink Blue)
- Background (page): `#ffffff` (Ghost White)
- Background (section): `#f6f6f8` (Fog Gray)
- CTA Button: `#ec652b` (Action Orange)
- Accent (brand): `#111a4a` (Deep Plum)
- Border (neutral): `#e3e4e8` (Steel Gray)

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Use `Ghost White` as background, overlay with a subtle `Faded Grid Blue` (`#023247`) map graphic. Headline: `SuisseIntl` 48px, weight 600, `Ink Blue` text, `lineHeight` 1.1, `letterSpacing` -1.44px. Subtext: `Inter` 18px, weight 400, `Slate Text` (`#7c7f88`). Include a `Call To Action Button - Filled` and a `Secondary Button - Outlined` below the text, aligned left with 24px horizontal spacing.
2. **Generate a Product Feature Card:** Background `Ghost White`, `8px` border-radius, using `rgba(17, 26, 74, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 1px 2px 0px, rgba(255, 255, 255, 0.5) 0px 0px 0px 1px inset` shadow. `24px` internal padding. Title: `SuisseIntl` 20px, weight 500, `Ink Blue`. Body text: `Inter` 14px, weight 400, `Slate Text`.
3. **Design a Header with Navigation:** Transparent background. Logo on left. Navigation links (`Primary Navigation Link`) to the right using `SuisseIntl` 16px weight 400, `Ink Blue` text. On far right, include `Ghost Button - Light` for 'Sign in' and `Call To Action Button - Filled` for 'Get started', horizontally spaced by 16px.
4. **Create a Testimonial Section:** Background `Fog Gray` (#f6f6f8). Quote text with `SuisseIntl` 28px, weight 300, `Ink Blue`, `lineHeight` 1.3, `letterSpacing` -0.56px. Attribution with `SuisseIntl` 14px, weight 500, `Deep Plum`, followed by `Slate Text` for title, both with `letterSpacing` -0.28px. Place an `Action Orange` `Card - Callout Orange` with a relevant metric adjacent to the text.

---
_Source: https://styles.refero.design/style/a76ec6ba-20b3-495c-9d89-1e58281e79e7_
