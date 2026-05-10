# Tedy — Design Reference

> Vibrant Pastel Softness

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.tedy.app](https://www.tedy.app) |
| Refero page | [https://styles.refero.design/style/9fd54812-6eb1-4445-83e6-124af21387db](https://styles.refero.design/style/9fd54812-6eb1-4445-83e6-124af21387db) |
| Theme | light |
| Industry | fintech |

## Overview

Tedy's design system evokes a warm, vibrant professionalism, balancing clarity with approachable pops of color. Muted pastel shades of purple, green, and yellow serve as soft background washes or semantic badges, while a vivid berry red provides energetic accents and distinct calls to action. Strong, tracked-in typography (Montreal Neue) anchors content, set against clean white surfaces with rounded rectangles that soften the overall feel. The system prioritizes a friendly but secure experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#030712` | neutral | Primary text, darkest borders, icons, and some card elements – establishing a deep, authoritative base |
| Cloud White | `#ffffff` | neutral | Canvas background, primary surface color for cards and buttons, providing a clean and expansive backdrop |
| Muted Silver | `#6b7280` | neutral | Secondary text, muted borders, and subtle fills, offering a softer tone than primary text |
| Pale Ash | `#e5e7eb` | neutral | Interface dividers, subtle button borders, and list item separators |
| Berry Burst | `#fd1774` | brand | Primary call to action button backgrounds, active interaction states, and distinctive icon accents – the system's most vivid and energetic accent |
| Lavender Mist | `#e5d6ff` | accent | Soft card backgrounds, link highlights, and decorative badge fills |
| Lemon Chiffon | `#fff1cd` | semantic | Accent backgrounds for badges signalling warnings or informational statuses |
| Mint Whisper | `#e3f7d6` | semantic | Accent backgrounds for badges signalling success or positive statuses |
| Sky Haze | `#ddf5fb` | accent | Subtle background wash for content sections |
| Bubblegum Pop | `#fe74ac` | accent | Red supporting accent for decorative details and low-frequency emphasis. Do not promote it to the primary CTA color |
| Turquoise Dream | `#4bb7cf` | accent | Decorative icon accents and illustrative elements |
| Fresh Sprout | `#64d71e` | accent | Decorative icon accents and illustrative elements |
| Lavender Glow Gradient | `#e7d6ff` | accent | Subtle background gradient for hero sections or prominent content blocks, creating a soft atmospheric effect |

## Typography

### Montreal Neue

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 14px, 15px, 16px, 17px, 19px, 23px, 28px, 56px, 59px, 61px, 64px, 70px, 80px, 85px, 86px, 104px |
| lineHeight | 0.90, 0.92, 0.95, 0.96, 0.98, 1.00, 1.02, 1.05, 1.38, 1.40, 1.43, 1.45, 1.50 |
| letterSpacing | -0.0650em, -0.0550em, -0.0500em, -0.0450em, -0.0400em, -0.0300em, -0.0250em, -0.0200em, -0.0050em |
| substitute | system-ui, sans-serif |
| role | The primary typeface for all headings, body text, and UI elements. Its confident, subtly tracked-in appearance maintains a modern and precise tone, especially at larger sizes where tracking tightens. |

### Georgia

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 80px |
| role | Georgia — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.5 | -0.005 |
| body | 16 |  | 1.45 | -0.005 |
| subheading | 23 |  | 1.4 | -0.02 |
| heading-sm | 28 |  | 1.38 | -0.025 |
| heading | 56 |  | 1.05 | -0.04 |
| heading-lg | 70 |  | 1.02 | -0.05 |
| display | 86 |  | 0.96 | -0.065 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| badges | 32px |
| images | 32px |
| buttons | 999px |
| general | 999px |

- **elementGap** — 18px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Background | `#ffffff` | 1 | The primary page background, providing a clean white foundation. |
| Accent Wash | `#ddf5fb` | 2 | Soft, light blue background tint for specific content sections to provide visual separation without heavy contrast. |
| Tinted Surface | `#e5d6ff` | 3 | Soft lavender background for prominent cards or sections, indicating a special feature or input area. |

## Components

### Primary Action Button

**Role:** Critical calls to action.

Filled button with a Berry Burst background, Cloud White text, and a full pill-shaped radius. Padding: 10px vertical, 18px horizontal. Has a soft Berry Burst shadow: rgba(253, 23, 116, 0.6) 0px 14px 35px -18px.

### Ghost Button

**Role:** Secondary actions or navigation.

Transparent background with Midnight Ink text. No border, no padding indicated from extracted data (defaults to browser). Used for discrete inline actions.

### Outline Neutral Button

**Role:** Tertiary actions or navigation.

Cloud White background with Midnight Ink text and a Pale Ash border. Full pill-shaped radius (999px). Padding: 10px vertical, 18px horizontal.

### Outline Small Button

**Role:** Compact secondary actions.

Cloud White background with Midnight Ink text and a Pale Ash border. Full pill-shaped radius (999px). Padding: 8px vertical, 14px horizontal.

### Content Card

**Role:** Information grouping, feature presentation, testimonials.

Transparent background, 0px padding, no shadow by default, 0px border radius, indicating it often acts as a container for other elements.

### Elevated Content Card

**Role:** Prominent information blocks or data visualizations.

Cloud White background with 20px border radius. This variant has a soft shadow: rgba(255, 255, 255, 0.08) 0px 1px 0px 0px inset, rgba(255, 255, 255, 0.03) 0px -1px 0px 0px inset, rgba(0, 0, 0, 0.35) 0px 30px 80px 0px and a backdrop blur of 24px.

### Accent Highlight Card

**Role:** Highlighting key information or user inputs.

Lavender Mist background with 16px border radius. 32px padding on all sides. Used for sections like 'Choose the categories that are important to you'.

### Testimonial Card (Hover)

**Role:** Engaging user testimonials.

Transparent background, 16px radius, with a strong shadow for emphasis: rgba(17, 24, 39, 0.16) 0px 28px 52px 0px. Padding variable: 41.6px top, 43.2px right, 35.2px bottom, 36.8px left.

### Status Badge - Warning

**Role:** Indicating warning or informational states.

Lemon Chiffon background with Midnight Ink text. Rounded rectangle shape, 0px top-left/bottom-right, 21.6px top-right, 32px bottom-left. Padding: 12.8px top, 12.8px right, 14.4px bottom, 18.4px left. Includes a 1px Lemon Chiffon outline.

### Status Badge - Success

**Role:** Indicating successful actions or positive states.

Mint Whisper background with Midnight Ink text. Rounded rectangle shape, 0px top-left/bottom-right, 21.6px top-right, 32px bottom-left. Padding: 12.8px top, 12.8px right, 14.4px bottom, 18.4px left.

### Status Badge - Accent

**Role:** Highlighting specific categories or features.

Lavender Mist background with Midnight Ink text. Rounded rectangle shape, 0px top-left/bottom-right, 21.6px top-right, 32px bottom-left. Padding: 12.8px top, 12.8px right, 14.4px bottom, 18.4px left.

## Layout

The page primarily uses a max-width contained layout, likely around 1200px, with content centered. The hero section is a split layout: a large, off-center Lavender Mist background block with dominant text on the left, juxtaposed with a collage of lifestyle photography on the right. Content sections generally feature alternating text-left/image-right (or product UI) patterns, maintaining a comfortable vertical rhythm with consistent spacing. Testimonials are presented in a two-column grid format with large, rounded cards. The navigation is a sticky top bar with a left-aligned logo and right-aligned buttons and links.

## Imagery

The visual language mixes authentic photography with clean UI examples and illustrative icons. Photography features diverse groups of happy, laughing people in candid, lifestyle-oriented shots, often contained within rounded rectangular masks. Product screenshots are clean, functional, and presented flat on white or subtly tinted backgrounds, sometimes with a soft shadow or transparency effect. Icons are minimal, outlined, and monochromatic, usually in Midnight Ink, occasionally adopting moderate accent colors like Turquoise Dream or Fresh Sprout. Imagery serves to establish an approachable, human-centered atmosphere and clarify product functionality simultaneously. Image density is moderate, with visuals often balanced against text blocks.

## Dos & Donts

### Do

- Use Midnight Ink (#030712) for all primary body text and headings.
- Apply Cloud White (#ffffff) as the default background for all large sections and surface elements.
- Utilize Berry Burst (#fd1774) exclusively for primary call-to-action buttons and interactive highlights to maintain its impact.
- Ensure all buttons and navigational elements use a 999px border-radius for a distinct pill shape.
- Employ Montreal Neue weight 500 for headings, with letter spacing adjusted according to the type scale for impact.
- Apply Lavender Mist (#e5d6ff) as a subtle background for card-like elements that require a gentle accent.
- Maintain a clear visual hierarchy with element gaps of 18px and card padding at 24px.

### Don't

- Do not use Berry Burst (#fd1774) for non-interactive elements or large background areas.
- Avoid using a border-radius less than 16px for cards, exceptions are images or badges with specific rounding.
- Do not introduce new shadow styles; adhere to the existing definition for Elevated Content Cards and Primary Action Buttons.
- Never use generic system fonts when Montreal Neue is available; always prioritize its specific weights and letter-spacing.
- Do not vary paragraph line-height from the specified Montreal Neue values (e.g., 1.45 at 16px) to avoid readability issues.
- Avoid using strong, saturated colors for backgrounds; reserve them for accents and semantic indicators only.
- Do not add additional decorative gradients beyond the defined Lavender Glow or radial accents; maintain clean primary surfaces.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #030712
background: #ffffff
border: #e5e7eb
accent: #e5d6ff
primary action: #fd1774 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: background #fd1774, text #ffffff, radius 999px, padding 10px 18px. The text should be 'Book a Demo'.
2. Create an Accent Highlight Card: background #e5d6ff, radius 16px, padding 32px. Place a heading 'Custom Benefits' (Montreal Neue, weight 500, size 28px, #030712, letter-spacing -0.025em) and body text 'Flexible plans for specific categories' (Montreal Neue, weight 400, size 16px, #030712, letter-spacing -0.005em) inside.
3. Create a Testimonial Card in hover state: transparent background, radius 16px, box-shadow rgba(17, 24, 39, 0.16) 0px 28px 52px 0px. The text should be '“ Tedy helped us increase our team's engagement...' (Montreal Neue, weight 400, size 19px, #030712, letter-spacing -0.02em).

---
_Source: https://styles.refero.design/style/9fd54812-6eb1-4445-83e6-124af21387db_
