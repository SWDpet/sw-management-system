# Arcadia — Design Reference

> Evergreen data canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.arcadia.com](https://www.arcadia.com) |
| Refero page | [https://styles.refero.design/style/b52981ce-fdb2-4ba0-86bb-f5ce0c27a9c6](https://styles.refero.design/style/b52981ce-fdb2-4ba0-86bb-f5ce0c27a9c6) |
| Theme | light |
| Industry | saas |

## Overview

Arcadia employs a grounded, sophisticated aesthetic, blending the precision of a data platform with the organic feel of clean energy. Its visual signature lies in its use of deep, muted teal (`Evergreen Deep`) as a primary brand color, paired with a soft, warm cream (`Pampas`) and an invigorating spring green (`Verdant Accent`). Typography is sharp and clear, providing authority without harshness, while spacious layouts and soft gradients hint at a natural, expansive energy landscape. Components are generally soft-edged and minimalist, emphasizing usability and understated confidence.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Evergreen Deep | `#104336` | brand | Primary brand color. Used for key navigation elements, dark text on light backgrounds, and borders of ghost buttons. Conveys authority and environmental connection |
| Mid-Tone Charcoal | `#101f1e` | neutral | Primary text color for headings and body copy, subtle card backgrounds, and dark button fills. Provides strong contrast and a sense of depth |
| Electric Violet | `#7c18d3` | accent | Decorative accent for visual elements, specific text highlights, and borders within data visualizations. Adds small pops of energetic color |
| Verdant Accent | `#0fff87` | accent | Vivid accent for primary call-to-action buttons providing a strong visual cue for interaction and indicating positive action. Also used for some decorative purposes |
| Pale Sage | `#afc4bf` | neutral | Subtle border color for cards and other UI elements, creating soft definition in minimalist surfaces; Subtle background gradient for hero sections and key visual blocks, blending natural muted tones to evoke a sense of calm and innovation; Expansive background gradient using multiple subtle color changes, creating a soft, almost atmospheric backdrop for key content |
| White Canvas | `#ffffff` | neutral | Dominant background color for most page content, card surfaces, and text on dark backgrounds. Creates an open, airy feel |
| Pampas | `#f3f1ec` | neutral | Secondary background color, used for alternating sections and subtle card backgrounds. Adds warmth and depth to the overall light theme |
| Graphite | `#535e5d` | neutral | Secondary text color for body copy, helper text, and less prominent information |
| Dark Carbon | `#333333` | neutral | Deep secondary text color for body copy and specific content areas, offering strong contrast |
| Silver Pine | `#c2cec8` | neutral | Light border color for UI elements, offering a delicate separation |
| Fog Gray | `#798281` | neutral | Muted text color for tertiary information, links, and borders |
| Input Gray | `#848c88` | neutral | Neutral form states, badge text, and quiet UI feedback where color should stay understated. Do not promote it to the primary CTA color |

## Typography

### DM Sans

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| weights | 300, 400, 500, 700 |
| sizes | 10px, 13px, 14px, 15px, 16px, 18px, 20px, 24px, 36px, 48px, 56px |
| lineHeight | 1.00, 1.10, 1.15, 1.20, 1.29, 1.50 |
| letterSpacing | -0.0200em, 0.0100em, 0.0700em |
| substitute | system-ui |
| role | Primary typeface for all UI elements, headings, and body text. Its clean, geometric forms and varied weights provide clarity and a modern, authoritative tone. Specific letter-spacing maintains legibility at different scales. |

### Helvetica

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 13px |
| lineHeight | 1.20 |
| substitute | Arial |
| role | Secondary typeface, primarily for small, utilitarian text where system font fallback is acceptable and high performance is prioritized. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-lg | 16 |  | 1.5 | 0.16 |
| subheading | 20 |  | 1.2 | 0.2 |
| heading | 36 |  | 1.15 | -0.72 |
| heading-lg | 48 |  | 1.1 | -0.96 |
| display | 56 |  | 1.1 | -1.12 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 18px |
| cards | 16px |
| image | 8px |
| input | 4px |
| buttons | 8px |

- **elementGap** — 24px
- **sectionGap** — 24px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | 0 | Base page background and default elevated surface for cards. |
| Pampas | `#f3f1ec` | 1 | Secondary background for alternating sections, providing subtle visual separation and warmth without stark contrast. |
| Gradient Aura | `#afc4bf` | 2 | Soft gradient background for hero sections or prominent content blocks, adding atmospheric depth. |

## Components

### Ghost Bordered Button

**Role:** Secondary action button

Transparent background with a border of `Evergreen Deep` (#104336) and text color `Mid-Tone Charcoal` (#101f1e). Features a large radius of 18px for a distinct, rounded appearance. Padding is 16px horizontal, 0px vertical.

### Request Demo Button (Filled)

**Role:** Primary Call-to-Action

Filled with `Verdant Accent` (#0fff87) background color, `Evergreen Deep` (#104336) text. Features 8px border-radius and 16px horizontal padding. Signals the most important action with vibrant color.

### Sign In Button (Filled)

**Role:** Secondary Call-to-Action

Filled with `Mid-Tone Charcoal` (#101f1e) background, `White Canvas` (#ffffff) text. Features 8px border-radius and 24px horizontal padding. Provides a strong, high-contrast action for key secondary interactions.

### Default Card

**Role:** Content container

Uses `White Canvas` (#ffffff) background with 16px border-radius. Padding of 24px on all sides. No shadow, creating a flat, modern aesthetic.

### Pampas Section Card

**Role:** Section background container

Applies `Pampas` (#f3f1ec) as background. Has 0px border-radius, acting as a full-width background panel for distinct content sections.

### Circular Callout Card

**Role:** Decorative content element

A visually distinct card with 50% border-radius, typically used for decorative or icon-based content. Transparent background and no shadow.

### Text Input Field

**Role:** User input element

Has a `White Canvas` (#ffffff) background, `Mid-Tone Charcoal` (#101f1e) text, and a `Input Gray` (#848c88) border. Rounded with a 4px border-radius. Padding is 16px horizontal, 0px vertical.

## Layout

The page primarily uses a full-bleed structure with content often contained within logical sections. The hero section leverages a full-width background gradient with centered headlines and calls-to-action. Content sections alternate between `White Canvas` and `Pampas` backgrounds, creating a clear vertical rhythm. Inner content is often arranged in a two-column or three-column grid for features and cards. Navigation is handled by a sticky top bar with clearly delineated primary and secondary actions. The layout feels spacious and organized, guiding the eye through a progression of information.

## Imagery

The site uses a mix of muted, abstract 3D illustrations and product screenshots. Abstract elements feature organic shapes, soft light, and a color palette aligned with the brand's greens and grays, sometimes with a pop of violet. Product screenshots are clean, showcasing UI against a light background with a subtle border or within a card. Icons are outlined, simple, and monochrome, often in `Evergreen Deep`. Imagery is primarily decorative and atmospheric, rather than strictly explanatory, conveying a sense of sophisticated technology and clean energy.

## Dos & Donts

### Do

- Prioritize `DM Sans` for all textual content, adjusting weight and letter-spacing according to the type scale for optimal legibility and brand tone.
- Use `Evergreen Deep` (#104336) for primary interactive elements, ensuring a consistent brand presence.
- Apply a 16px `radius` for cards and a 8px `radius` for buttons, maintaining a soft, approachable feel across major components.
- Utilize `White Canvas` (#ffffff) and `Pampas` (#f3f1ec) as primary section backgrounds, alternating to create visual rhythm on long pages.
- Integrate `Verdant Accent` (#0fff87) exclusively for crucial calls-to-action to maximize visual impact and guide user focus.
- Maintain comfortable element spacing using multiples of 8px, with a default `elementGap` of `24px` for consistent information density.
- When using gradients for large visual sections, always prefer the `Gradient Aura` or `Gradient Horizon` tokens to ensure brand consistency.

### Don't

- Avoid using `Electric Violet` (#7c18d3) as a primary action color; reserve its vividness for decorative accents or specific data highlights.
- Do not introduce new border radii beyond 4px, 8px, 16px, or 18px to preserve the system's consistent soft-edged aesthetic.
- Refrain from adding hard shadows; the design relies on flat planes and subtle background shifts rather than prominent elevation effects.
- Do not use highly saturated or dark backgrounds for entire page sections; maintain the dominant `light` theme using `White Canvas` (#ffffff) or `Pampas` (#f3f1ec).
- Avoid generic system fonts where `DM Sans` is specified; the custom typeface is integral to the brand's identity.
- Do not deviate from the established letter-spacing values for `DM Sans` to prevent degradation of typographic harmony and legibility.
- Do not use black (#000000) for body text; `Mid-Tone Charcoal` (#101f1e) or `Graphite` (#535e5d) should be used for readability and warmth.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #101f1e
- background: #ffffff
- border: #104336
- accent: #7c18d3
- primary action: #0fff87 (filled action)

### 3-5 Example Component Prompts
- Create a hero section: `Gradient Aura` background, centered headline 'Solutions to power the new energy era' in `DM Sans` weight 700 at 56px, `Mid-Tone Charcoal` text, `letterSpacing` -1.12px. Add a `Request Demo Button (Filled)` below.
- Create a feature card: `Default Card` with a `White Canvas` background and 16px radius. Inside, use `DM Sans` weight 500 at 24px, `Mid-Tone Charcoal` text for the title, `DM Sans` weight 400 at 16px, `Graphite` text for the description.
- Create a ghost button for navigation: `Ghost Bordered Button` with `Evergreen Deep` border and text. `DM Sans` weight 400 at 16px, using 18px radius.
- Create an input form: `Text Input Field` for email entry, with `Mid-Tone Charcoal` placeholder text 'Enter your email'. Follow with a `Request Demo Button (Filled)` to submit.

---
_Source: https://styles.refero.design/style/b52981ce-fdb2-4ba0-86bb-f5ce0c27a9c6_
