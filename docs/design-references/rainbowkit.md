# RainbowKit — Design Reference

> Midnight Nebula Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.rainbowkit.com](https://www.rainbowkit.com) |
| Refero page | [https://styles.refero.design/style/7421c174-a1b1-4695-a9e7-a82dc6f5ea3b](https://styles.refero.design/style/7421c174-a1b1-4695-a9e7-a82dc6f5ea3b) |
| Theme | dark |
| Industry | devtools |

## Overview

RainbowKit orchestrates a cosmic dark-mode experience with glowing accents. Most surfaces are deep, rich dark grays and blacks, providing a canvas for vibrant, saturated blues, violets, and a spectrum of other hues to punctuate interactive elements and provide visual interest. Typography is compact and precise, maintaining clarity against the dark backdrop. Components favor soft curves and subtle inner shadows, creating a sense of depth and dimensionality without harsh outlines, prioritizing a confident, playful developer-tool aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Eclipse Black | `#000000` | neutral | Page background, primary text (in light mode contexts), most interactive element borders, shadow foundations; Subtle background gradient for elevated sections, providing textural variation on dark surfaces |
| Cloud White | `#ffffff` | neutral | Text on dark backgrounds, icon fills, card surfaces when a light theme is temporarily invoked (e.g. popups) |
| Slate Deep | `#1b1c1e` | neutral | Elevated background surfaces, secondary text, subtle dividers |
| Charcoal Grey | `#25292e` | neutral | Tertiary background surfaces, muted text details |
| Vivid Blue | `#0e76fd` | brand | Primary action backgrounds, interactive links, selected states, key iconography — provides a strong, energetic focal point |
| Deep Violet | `#38228f` | brand | Decorative card backgrounds, accent elements, providing depth and a premium feel |
| Sky Blue | `#3898ff` | brand | Alternative action backgrounds, secondary branding elements, gradient top-color; Decorative gradient for headers and branded elements, creating a luminous effect |
| Azure Glow | `#5f5afa` | accent | Accent buttons, interactive states, part of the multi-color brand spectrum |
| Flamingo Pink | `#ff5ca0` | accent | Accent buttons, interactive states, part of the multi-color brand spectrum |
| Volcanic Red | `#fa423c` | accent | Accent buttons, interactive states, part of the multi-color brand spectrum |
| Sunset Orange | `#ff801f` | accent | Accent buttons, interactive states, part of the multi-color brand spectrum |
| Emerald Green | `#1db847` | accent | Accent buttons, interactive states, part of the multi-color brand spectrum |

## Typography

### SFRounded

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700, 800 |
| weights | 400, 500, 600, 700, 800 |
| sizes | 11px, 14px, 16px, 18px, 20px, 24px, 40px, 52px |
| lineHeight | 1.00, 1.05, 1.17, 1.20, 1.25, 1.29, 1.31, 1.33 |
| letterSpacing | 0.0070em, 0.0090em, 0.0150em, 0.0170em, 0.0180em, 0.0190em, 0.0220em, 0.0250em, 0.0320em |
| role | Primary brand typeface for all headings, body text, and UI elements. Its rounded humanist sans-serif aesthetic contributes to the friendly, contemporary feel of the interface across all weights and sizes. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 13px, 16px |
| lineHeight | 1.20 |
| role | Fallback system font primarily for button and icon labels where a generic sans-serif is sufficient and SFRounded might not be loaded, maintaining basic readability. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 20px |
| lineHeight | 1.05 |
| letterSpacing | 0.0170em |
| role | System fallback for general body text at display sizes, ensuring content remains legible and consistent even without custom fonts. |

### SFMono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.00 |
| letterSpacing | 0.0250em |
| role | Monospace font for code snippets and technical text, ensuring consistent character width important for programming output and command line interfaces. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.33 | 0.35 |
| body-sm | 14 |  | 1.29 | 0.28 |
| body | 16 |  | 1.25 | 0.26 |
| subheading | 18 |  | 1.2 | 0.32 |
| heading-sm | 20 |  | 1.17 | 0.34 |
| heading | 24 |  | 1.05 | 0.29 |
| heading-lg | 40 |  | 1 | 0.36 |
| display | 52 |  | 1 | 0.36 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| icons | 12px |
| buttons | 9999px |
| default | 6px |

- **elementGap** — 12px
- **sectionGap** — 40px
- **cardPadding** — 18px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Canvas | `#000000` | 0 | Dominant background for the entire application, creating a deep, immersive dark mode experience. |
| Base Surface | `#1b1c1` | 1 | Used for sections or panels that rest directly on the page canvas, providing a slight elevation without heavy shadows. |
| Elevated Surface | `#25292` | 2 | Further elevated elements like cards or modals, offering a distinct visual separation from the base surface. |
| Light Modal Surface | `#ffffff` | 3 | A deliberate contrast for pop-ups or specific modals, creating a focused, light-themed interaction layer within the dark environment. |

## Components

### Ghost Button

**Role:** Ghost interactive element for secondary actions.

Transparent background (`rgba(0, 0, 0, 0)`), Cloud White text, with a subtle white inset border (`rgba(245, 248, 255, 0.12) 0px 0px 0px 1px inset`). Uses a full pill radius (`9999px`) and symmetrical horizontal padding (24px right/left).

### Primary Action Button

**Role:** Main call-to-action button, conveying primary interaction.

Filled with Vivid Blue (`#0e76fd`) background, Cloud White text, with `9999px` corner radius. Padding is `5px` top/bottom and `5px` right/left, with 12px horizontal element spacing.

### Icon Button (Circular)

**Role:** Small, circular button for actions related to icons or status.

Semi-transparent white background (`rgba(255, 255, 255, 0.32)`), Eclipse Black text, and a `100%` border-radius for a perfect circle. Padding: `1px` top/bottom, `6px` right/left.

### Modal Card (Light)

**Role:** Elevated container primarily for interactive dialogs or pop-ups.

Cloud White background (`#ffffff`), `24px` border-radius, and a soft, prominent shadow (`rgba(0, 0, 0, 0.32) 0px 8px 32px 0px`). Typically contains UI elements on a light canvas, contrasting with the dark page background.

### Code Snippet Container

**Role:** Container for showcasing code or command-line instructions.

Background color `rgba(0, 0, 0, 0)` with a subtle inner border `rgba(245, 248, 255, 0.12) 0px 0px 0px 1px inset`. Text is monospaced using SFMono.

### Logo Icon

**Role:** Used for partner logos and small brand identifiers.

Circular shape with `100%` radius, typically filled with opaque Deep Violet (`#38228f`) or other brand colors. Contains single-color or simple multi-color logos.

## Layout

The page primarily uses a max-width contained layout, approximately 1200px, centered on the screen. The hero section is full-bleed with a dark background, featuring a centered headline and subtext. Content flows in distinct vertical sections, often featuring alternating left-aligned text with right-aligned visuals, or vice-versa, creating a dynamic Z-pattern. Feature grids utilize a multi-column card layout, specifically a 3-column grid for partner logos. Navigation is a minimal top bar, featuring a primary 'Connect Wallet' action button, providing a clear and sparse interface.

## Imagery

The site predominantly relies on product screenshots and custom illustrations for visual content rather than photography. Product screenshots are typically contained within device mocks or clean cards, showing the UI in context. Illustrations are simple, often monochromatic or using a limited brand palette, with a flat and slightly geometric style. Iconography is primarily outlined, using a medium stroke weight, and integrates with the brand's vibrant accent colors for decorative and functional purposes. Imagery serves primarily to explain product features and showcase brand partners, and is well-balanced with text, avoiding an image-heavy aesthetic.

## Dos & Donts

### Do

- Prioritize Eclipse Black (#000000) for base backgrounds and Cloud White (#ffffff) for primary text on dark themes.
- Use Vivid Blue (#0e76fd) as the default for all primary calls to action and interactive elements.
- Apply `9999px` border-radius for all button elements to create a distinctive pill shape.
- Maintain comfortable vertical rhythm with `12px` element gaps and `40px` section gaps.
- Utilize SFRounded as the primary typeface for all textual content, adjusting weights for hierarchy.
- Apply the inner shadow `rgba(255, 255, 255, 0.12) 0px 0px 0px 1px inset` to interactive elements on dark backgrounds for a subtle raised effect.
- Reserve specific vibrant colors (like Azure Glow, Flamingo Pink, Volcanic Red) for accent buttons to diversify call-to-action options without diluting the primary brand blue.

### Don't

- Avoid harsh, contrasting outlines on interactive elements; prefer subtle inner shadows or transparent backgrounds.
- Do not use generic system fonts for display headings; leverage SFRounded weights for brand consistency.
- Do not use #4bd166 for UI elements; it is reserved for decorative SVG fills and not interactive components.
- Avoid dense informational blocks; ensure ample spacing (`12px` elementGap, `40px` sectionGap) between components and content.
- Do not use box-shadows excessively; only apply the specific tokenized shadows for elevation or interactive states.
- Do not introduce new color palettes outside of the defined Brand and Accent colors; decorative gradients should be limited to the defined Gradient Aura Blue and Gradient Ocean.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #ffffff
background: #000000
border: rgba(245, 248, 255, 0.12)
accent: #0e76fd
primary action: #0e76fd (filled action)

### 3-5 Example Component Prompts
1. Create a hero section: Eclipse Black background. Headline 'The best way to connect a wallet' at SFRounded 52px weight 800, Cloud White (#ffffff), letter-spacing 0.36px. Subheading 'Designed for everyone. Built for developers.' at SFRounded 24px weight 400, Cloud White (#ffffff). Include a primary action button 'View the Docs': Vivid Blue (#0e76fd) background, Cloud White text, SFRounded 16px weight 700, 9999px radius, 5px top/bottom, 5px right/left padding.
2. Create a 'Connect Wallet' modal: Light Modal Surface card (#ffffff), 24px radius, with shadow rgba(0, 0, 0, 0.32) 0px 8px 32px 0px. Inside, place a list of wallet options. Each option should be a row with an icon and text 'Rainbow' at SFRounded 16px, Eclipse Black (#000000).
3. Create a secondary ghost button: Transparent background, Cloud White text (SFRounded 16px weight 400), with an inner border rgba(245, 248, 255, 0.12) 0px 0px 0px 1px inset, 9999px radius, 24px right/left padding. Label it 'Learn More'.
4. Design a 'Code Snippet' component for `npm init` instructions: Background is transparent with `rgba(245, 248, 255, 0.12) 0px 0px 0px 1px inset` border, `9999px` radius, SFMono 14px text in Cloud White. Place a circular Icon Button 'Copy' with `rgba(255, 255, 255, 0.32)` background, `100%` radius, and Eclipse Black icon/text.
5. Create a partner logo grid section: Base Surface (#1b1c1e) background. Each logo is a circular 'Logo Icon' with Deep Violet (#38228f) background, 100% radius, containing an Eclipse Black SVG logo, arranged in a 3-column grid with 12px element gaps.

---
_Source: https://styles.refero.design/style/7421c174-a1b1-4695-a9e7-a82dc6f5ea3b_
