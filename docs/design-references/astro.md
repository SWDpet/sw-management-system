# Astro — Design Reference

> Deep-space console light show. Gradient-infused dark surfaces illuminated by precise, vibrant accents and high-contrast text.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://astro.build](https://astro.build) |
| Refero page | [https://styles.refero.design/style/e8c604cc-1c8d-42a3-aeca-fcfc25e70344](https://styles.refero.design/style/e8c604cc-1c8d-42a3-aeca-fcfc25e70344) |
| Theme | dark |
| Industry | devtools |

## Overview

Astro's design system evokes a digital cosmos, combining deep, expansive dark backgrounds with vibrant, carefully placed gradients and neon-like accents. The visual identity balances technical precision with a sense of wonder, achieved through high-contrast typography against dark surfaces and circular forms paired with sharp geometric edges. Gradient overlays create an illusion of depth and energy, while a consistent approach to spacing ensures legibility and a comfortable user experience even within information-dense segments.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space | `#060913` | neutral | Primary background for the application, providing a dark canvas for all content. |
| Cosmic Dust | `#1f232` | neutral | Elevated surface color for cards, sections, and borders, creating subtle depth against the primary background. |
| Void Shadow | `#0c0f19` | neutral | Slightly darker shade used for subtle button backgrounds and card shadows, indicating interactivity or a lower prominence. |
| Stardust | `#858b98` | neutral | Secondary text and subdued headings, providing a comfortable contrast on dark backgrounds. |
| Lunar Gray | `#545864` | neutral | Subtle borders and separators, subtly segmenting content without drawing too much attention. |
| Aurora | `#f2f6fa` | neutral | Primary text color for body copy and headings, ensuring maximum legibility on dark surfaces. |
| White Dwarf | `#ffffff` | neutral | Strongest contrast for critical text elements, active states, and icons. |
| Stellar Blue | `#162a4` | brand | Background for certain illustrative elements or features, providing a cool undertone. |
| Interstellar Gradient Alpha | `#b845ed` | brand | Primary brand gradient, used on hero sections and key calls to action, drawing immediate visual attention through its active, vibrant feel. |
| Interstellar Gradient Beta | `#f041ff` | brand | Secondary brand gradient, complementing the primary gradient with a warm, energetic streak for accent areas. |
| Interstellar Gradient Gamma | `#2f4cb3` | brand | Tertiary brand gradient, used for softer, more subdued accent backgrounds or decorative elements, adding depth. |
| Cosmic Sparkle Vivid | `#4bf3c8` | accent | Vivid accent for highlighting key information, code snippets, or interactive elements, appearing like a focused light source. |
| Cosmic Sparkle Blue | `#54b9ff` | accent | Vivid blue accent for links, interactive elements, and focused states, providing a clear visual cue that complements the gradient palette. |
| Asteroid Dust | `#ffd493` | accent | Highlight color for specific keywords or badge text, creating a warm contrast. |
| Galaxy Violet | `#acafff` | accent | Another vibrant accent for code, links, and specific highlight text. |

## Typography

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| sizes | 14px, 16px, 20px |
| lineHeight | 1.00, 1.40, 1.50, 1.65, 1.81 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "zero" |
| substitute | Inter |
| role | Default UI text, for body copy, buttons, navigation, and small headings. Its neutrality provides a stable foundation for the more expressive headings. |

### Obviously

| Key | Value |
| --- | --- |
| weight | 300, 400, 700 |
| sizes | 20px, 30px, 36px, 48px |
| lineHeight | 1.10, 1.11, 1.20, 1.40 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "cv09", "liga", "salt", "ss06", "ss11" |
| substitute | Poppins |
| role | Display and marketing headings. The custom features are crucial for its distinct character, creating a modern, slightly technical yet approachable feel. |

### ui-monospace

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 14px |
| lineHeight | 1.65 |
| letterSpacing | normal |
| fontFeatureSettings | "calt", "zero" |
| substitute | Fira Code |
| role | Code snippets and technical references, providing clear distinction and readability for developers. Its consistent width aligns with a tools-focused brand. |

### MDIO

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 12px, 16px |
| lineHeight | 1.00, 1.33, 1.50 |
| letterSpacing | 0.4px |
| fontFeatureSettings | "calt", "zero" |
| substitute | Space Mono |
| role | Small functional text, potentially for labels, tags, or iconography support. The letter spacing gives it a precise, technical feel, differentiating it from body text. |

### Inter

| Key | Value |
| --- | --- |
| weight | 200, 400 |
| sizes | 16px, 18px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| substitute | Inter |
| role | Alternative body text for specific passages or components, providing additional typographic texture and a slightly different x-height from the system default. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 16 | 0.48 |
| body-sm | 14 |  | 21 |  |
| body | 16 |  | 24 |  |
| subheading | 20 |  | 22 |  |
| heading | 30 |  | 33 |  |
| heading-lg | 36 |  | 40 |  |
| display | 48 |  | 53 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| badges | 8px |
| buttons | 9999px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 1280px

## Components

### Hero CTA Buttons + Version Badge

### Feature Cards — What is Astro?

### Themes Tab Selector + Cards

### Primary Filled Button

**Role:** Call to action

Rounded pill shape with Aurora text on Deep Space background, minimal padding. Used for high-emphasis actions like 'Get Started'. `backgroundColor: rgb(255, 255, 255)` (appears white but rendered with transparent overlay), `color: rgb(242, 246, 250)`, `borderRadius: 9999px`, `padding: 8px 16px`.

### Subtle Pill Button

**Role:** Secondary action

Ghost button with rounded pill shape, Cosmic Dust background with slight transparency, Aurora text. Used for less prominent actions, like framework selection buttons. `backgroundColor: rgba(44, 44, 44, 0.3)`, `color: rgb(242, 246, 250)`, `borderRadius: 9999px`, `padding: 8px 24px`.

### Text Link Button

**Role:** Tertiary action

Invisible button with only Aurora text, no background or border. For purely text-based actions or navigation within sections. `backgroundColor: rgba(0, 0, 0, 0)`, `color: rgb(242, 246, 250)`.

### Default Navigation Link

**Role:** Navigation element

Text link using Stardust color, no background or border, used for navigation items until interaction. `color: rgb(191, 193, 201)`.

### Astro Current Version Badge

**Role:** Informational badge

Small pill-shaped badge indicating a version number and 'Available now!'. Uses a Cosmic Dust background and Aurora text. `backgroundColor: rgba(44, 44, 44, 0.3)`, `color: rgb(242, 246, 250)`, `borderRadius: 9999px`, `padding: 8px 24px`.

## Layout

The page maintains a `1280px` max-width, with content centered. The hero section is full-bleed, showcasing the `Interstellar Gradient Alpha` background with a centered headline and CTA section. Subsequent sections alternate between the `Deep Space` (#060913) and `Cosmic Dust` (#1f232e) background colors, creating a subtle visual rhythm without hard dividers. Content within these sections often follows a two-column layout, typically text-left with an illustrative element or screenshot right, or a grid of `Feature Cards`. Vertical spacing between sections is generous (`sectionGap: 64px`), contributing to the comfortable reading density. Navigation is managed by a sticky top bar with links and a subtle GitHub icon.

## Imagery

The site predominantly uses abstract, gradient-infused graphics (Interstellar Gradient Alpha/Beta) as background elements, creating a sense of depth and energy. Product screenshots are typically contained within cards (Cosmic Dust background, 16px radius), appearing as embedded UI elements. Icons are simplified and often outlined or single-filled, consistently appearing in White Dwarf (#ffffff) or Stardust (#858b98), sometimes with a vibrant Cosmic Sparkle color (#4bf3c8, #54b9ff) as an accent. The imagery's role is primarily decorative and atmospheric, establishing a tech-forward, cosmic brand identity, with product shots serving an explanatory function without lifestyle photography.

## Dos & Donts

### Do

- Use Deep Space (#060913) as the default page background to establish the dark theme.
- Apply Interstellar Gradient Alpha (linear-gradient(83.21deg, rgb(50, 69, 255), rgb(184, 69, 237))) to hero sections and primary CTAs for high visual impact.
- Render all primary body and heading text in Aurora (#f2f6fa) or White Dwarf (#ffffff) for optimal contrast on dark backgrounds.
- Utilize 9999px border-radius for all interactive buttons and badges, creating a soft, approachable pill shape.
- Employ `Obviously` font (weights 300, 400, 700) for all display headings to leverage its distinctive custom features.
- Maintain an `elementGap` of 8px for vertical and horizontal spacing between adjacent small elements like buttons or icons.
- Use `MDIO` font specifically for technical labels and small functional text, taking advantage of its 0.4px letter spacing.

### Don't

- Avoid using bright, high-saturation colors for large background areas; reserve them strictly for accents and gradients.
- Do not deviate from the 9999px radius for buttons or the 16px radius for cards; these radii are key to the brand's aesthetic.
- Never use generic system monospace fonts when rendering code; always use `ui-monospace` (or `Fira Code`) for consistency.
- Do not introduce drop shadows for card elevation; rely on `Cosmic Dust` (#1f232e) background color difference to indicate layers.
- Do not use overly complex or multi-color iconography; stick to minimal, outline, or single-fill styles using accent colors like Cosmic Sparkle Vivid or White Dwarf.
- Avoid excessive text justification or wide line lengths; keep body text at `16px` with a `1.5` line height (24px) for readability.
- Do not use letter spacing on `Obviously` font for headings; its inherent `fontFeatureSettings` define its character.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): #f2f6fa
- Background (Primary): #060913
- CTA Button Background: #ffffff
- Border (Subtle): #e5e7eb
- Accent (Vivid): #4bf3c8

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Full-width background using `linear-gradient(83.21deg, rgb(50, 69, 255), rgb(184, 69, 237))`. Centered headline 'The web framework for content-driven websites' using `Obviously` font size 48px, weight 700, color `Aurora` (#f2f6fa), line-height 1.1. Below that, a secondary text 'Astro powers the world's fastest...' using `ui-sans-serif` font size 18px, weight 400, color `Stardust` (#858b98), line-height 1.5. A primary button 'Get Started' with `White Dwarf` (#ffffff) background, `Aurora` (#f2f6fa) text, 9999px border-radius, and `8px 16px` padding.
2. **Generate a Feature Card:** Background color `Cosmic Dust` (#1f232e), border-radius 16px, no shadow. Inside, a heading 'Server-First' using `Obviously` font size 20px, weight 400, color `Aurora` (#f2f6fa), line-height 1.2. Below, body text 'Astro improves website performance...' using `ui-sans-serif` font size 16px, weight 400, color `Stardust` (#858b98), line-height 1.5. Apply `elementGap` of `8px` between elements within the card.
3. **Design a Subtle Pill Button:** Background `rgba(44, 44, 44, 0.3)`, text color `Aurora` (#f2f6fa), border-radius 9999px, `8px 24px` padding. Text content 'React' using `ui-sans-serif` font size 16px, weight 400.
4. **Display a Code Snippet Block:** Background `Deep Space` (#060913) with a subtle `Cosmic Dust` (#1f232e) border (`1px solid #1f232e`), 16px border-radius, `24px` padding. Code text within using `ui-monospace` font size 14px, weight 400, color `Cosmic Sparkle Vivid` (#4bf3c8), line-height 1.65. Ensure the text `npm create astro@latest` is rendered like this.

---
_Source: https://styles.refero.design/style/e8c604cc-1c8d-42a3-aeca-fcfc25e70344_
