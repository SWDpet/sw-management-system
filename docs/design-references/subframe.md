# Subframe — Design Reference

> engineered clarity on frosted glass. A clean, bright surface for complex tools.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.subframe.com](https://www.subframe.com) |
| Refero page | [https://styles.refero.design/style/c65db621-7faa-45f3-8e30-dc3ef9ffe660](https://styles.refero.design/style/c65db621-7faa-45f3-8e30-dc3ef9ffe660) |
| Theme | light |
| Industry | ai |

## Overview

Subframe employs a minimalist, high-contrast aesthetic that feels precise and engineered. The design emphasizes clarity through a largely achromatic palette, crisp typography, and subtle component treatments. Interactive elements use dark fills or light borders to stand out against the pervasive light gray surfaces, creating a focused experience suitable for a developer-centric tool. The system avoids overt ornamentation, relying on strong typographic hierarchy and restrained spatial relationships.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas Fog | `#e5e7eb` | neutral | Primary page background, structural borders, subtle neutral elements |
| Inkwell | `#242424` | neutral | Primary text, darkest UI elements, filled button backgrounds — signifies interaction and primary content |
| Ghost White | `#fafafa` | neutral | Secondary surface background, icon strokes, active states |
| Surface Frost | `#ededed` | neutral | Card backgrounds, slightly elevated surfaces |
| Code Black | `#171717` | neutral | Dominant headings, high-contrast body text |
| Muted Ash | `#a3a3a3` | neutral | Secondary body text, placeholder text, subtle contextual information |
| Subtle Gray | `#bdbdbd` | neutral | Subtle button shadows, low-contrast UI details |
| Helper Gray | `#5c5c5c` | neutral | Helper text, code comments, less prominent body copy |
| Whisper Gray | `#737373` | neutral | Muted captions, helper text, and de-emphasized UI labels |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 500, 600, 700 |
| weights | 500, 600, 700 |
| sizes | 12px, 14px, 18px, 24px, 28px, 48px, 96px |
| lineHeight | 1.00, 1.13, 1.14, 1.17, 1.33, 1.43 |
| letterSpacing | -0.0500em at 96px, -0.0250em at 48px, -0.0050em at 24px and 28px |
| substitute | system-ui |
| role | Primary UI font for all headings, body text, navigation, and buttons. Its versatile weights and condensed nature optimize for a compact, information-dense display. |

### ui-monospace

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 10px |
| lineHeight | 1.70 |
| letterSpacing | -0.0060em |
| substitute | monospace |
| role | System monospace fallback for code blocks and technical output. |

### Fragment Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 18px |
| lineHeight | 1.33 |
| letterSpacing | -0.0250em |
| substitute | monospace |
| role | Specific monospace font for highlighted code snippets or technical labels, providing a distinct, engineered aesthetic where precision is paramount. |

### Instrument Serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 80px, 128px |
| lineHeight | 1.00 |
| letterSpacing | -0.0250em |
| substitute | Lora |
| role | Display font used for extremely large, impactful headings, reserved for hero sections and key messaging to create a sense of scale and authority. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.7 | -0.06 |
| body | 14 |  | 1.43 |  |
| body-lg | 18 |  | 1.33 | -0.025 |
| heading-sm | 24 |  | 1.17 | -0.005 |
| heading | 28 |  | 1.14 | -0.005 |
| heading-lg | 48 |  | 1.13 | -0.025 |
| display | 96 |  | 1 | -0.05 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 24px |
| buttons | 16px |
| navigation | 9999px |

- **elementGap** — 8px
- **sectionGap** — 24-64px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#fafafa` | 0 | Base background for sections and general page canvas, providing a bright, airy foundation. |
| Base Surface | `#e5e7eb` | 1 | Dominant background for the overall page, creating a slightly desaturated, muted base. |
| Card Surface | `#ededed` | 2 | Background for cards and elevated content blocks, providing a subtle visual distinction from the base page. |

## Components

### Filled Primary Button

**Role:** Main call to action button.

Solid Inkwell background (#242424), Ghost White text (#fafafa), 16px border-radius. Padding of 0px vertical, 16px horizontal. Uses Inter font.

### Outlined Secondary Button

**Role:** Secondary action button, less prominent than primary.

Transparent background, Inkwell text (#242424), 16px border-radius, 1px Canvas Fog border (#e5e7eb). Padding 0px vertical, 16-20px horizontal. Uses Inter font.

### Subtle Interaction Button

**Role:** Ghost button with a light hover state.

Transparent background for default, light gray background on hover (rgba(0, 0, 0, 0.06)). Inkwell text (#242424), 16px border-radius, 1px Canvas Fog border (#e5e7eb). Padding 0px vertical, 20px horizontal. Uses Inter font.

### Nav Button

**Role:** Navigation links styled as buttons.

Transparent background, Inkwell text (#242424), 9999px border-radius (pill shape). Minimal horizontal padding. Uses Inter font.

### Feature Card

**Role:** Container for showcasing product features or content blocks.

Surface Frost background (#ededed), 24px border-radius. No box shadow evident. Generous internal padding and spacing to contain content.

### Tool Selector Toggle

**Role:** Segmented control for tool selection, seen in the hero.

A complex button with a layered shadow and inset effect. Primarily Inkwell (#242424), with subtle lighter elements for active state. Rounded corners. Uses a multi-layered inset shadow: rgba(255, 255, 255, 0.25) 0px 4px 4px -2px inset, rgba(0, 0, 0, 0.25) 0px -4px 4px -2px inset, rgba(255, 255, 255, 0.1) -2px 0px 2px -2px inset, rgba(255, 255, 255, 0.1) -2px 0px 2px -2px inset.

### Small Pill Tag

**Role:** Small, rounded labels for categories or status.

Utilizes a 9999px radius (pill shape) with Canvas Fog background (#e5e7eb) and Inkwell text (#242424). Used for filtering or categorization in navigation contexts.

## Layout

The page maintains a centered, contained layout with a maximum content width that appears to be around 1200px. The hero section frequently uses a prominent headline centered above a key product visual, often with minimal background styling. Sections primarily flow vertically with consistent, comfortable vertical spacing, evidenced by 24-64px section gaps. Content is typically arranged in stacked blocks or 1-2 column text/image layouts, alternating positions for visual interest. Navigation is handled by a sticky top bar with functional links and distinct 'Log in'/'Start for free' buttons, maintaining a clean header throughout the scroll.

## Imagery

The site's imagery is defined by high-fidelity product screenshots and minimalist 3D renders. These visuals are typically contained within cards or placed centrally, often against a neutral, diffused background. There is a strong emphasis on showcasing the product UI directly, with a clean and almost stark presentation. Icons are outlined, leveraging the system's precise line work and monochrome palette, with a moderately heavy stroke weight. The overall impression is one of functionality and directness, with imagery primarily serving to explain concepts or demonstrate the product, rather than for decorative atmosphere.

## Dos & Donts

### Do

- Prioritize Inter font for all UI text, varying weight (500-700) and sizing to establish clear hierarchy.
- Use Inkwell (#242424) for primary text and calls to action, maintaining high contrast against light backgrounds.
- Apply Canvas Fog (#e5e7eb) as the dominant page background and for subtle borders to delineate sections.
- Reserve the complex inset shadow for interactive controls like the Tool Selector Toggle, indicating depth and functionality.
- Utilize 24px border-radius for cards and content containers, conveying a refined, rounded aesthetic.
- Employ a 9999px border-radius for navigation items and small tags, creating a distinct pill-shaped interactive element.
- Maintain a clear visual hierarchy by limiting prominent colors to achromatic tones, saving any potential accent colors for highly functional or branded elements not present in this dataset.

### Don't

- Avoid introducing saturated colors unless they serve a specific brand or semantic function not supported by the current palette.
- Do not use heavy, complex shadows for general elevation – the system favors a flatter, almost shadowless aesthetic.
- Refrain from using more than two font families on a single page, with specific monospace fonts reserved for technical contexts.
- Do not introduce decorative gradients or texture, as the system relies on clean, flat surfaces.
- Avoid dense, unbroken blocks of text; break up content with generous spacing and strong headlines.
- Do not vary border-radius arbitrarily; stick to 16px for buttons, 24px for cards, and 9999px for pill-shaped elements.
- Resist using small font sizes (below 12px) for any critical information, even for captions, to maintain readability.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #242424
- background: #e5e7eb
- border: #e5e7eb
- accent: no distinct accent color
- primary action: #242424 (filled action)

Example Component Prompts:
- Create a Primary Action Button: #242424 background, #fafafa text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Generate a Feature Card with the title 'Real design control.' using Inter weight 600 at 28px, color Code Black (#171717), letter-spacing -0.0050em. The card should have a Surface Frost background (#ededed), 24px border-radius, and no box shadow. Provide ample internal padding of 16px.

---
_Source: https://styles.refero.design/style/c65db621-7faa-45f3-8e30-dc3ef9ffe660_
