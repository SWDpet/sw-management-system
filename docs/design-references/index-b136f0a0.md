# Index — Design Reference

> Deep space command console.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://index.inc](https://index.inc) |
| Refero page | [https://styles.refero.design/style/b136f0a0-8064-4978-a18e-db54b9362c24](https://styles.refero.design/style/b136f0a0-8064-4978-a18e-db54b9362c24) |
| Theme | dark |
| Industry | productivity |

## Overview

Index employs a deep space command console aesthetic, combining dark, muted violet and near-black surfaces with luminous accents. The interface prioritizes organized information density, utilizing crisp borders and subtle holographic-like shadows to define interactive elements. Typography is precise and utilitarian, while occasional vibrant gradients and glows infuse points of interest into the otherwise controlled, dark environment.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Void Black | `#02030b` | neutral | Deepest canvas background, elevated card surfaces |
| Eclipse Gray | `#04061c` | neutral | Primary canvas background |
| Twilight Violet | `#0c0a2b` | neutral | Card backgrounds, secondary surface fills |
| Ash Slate | `#11132b` | neutral | Subtle surface accents and divider lines |
| Slate Blue | `#152e58` | neutral | Violet decorative accent for icons, marks, and small graphic details. Do not promote it to the primary CTA color |
| Gunmetal Gray | `#202333` | neutral | Subtle button backgrounds, deeper card borders |
| Dark Star | `#242444` | neutral | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Lunar Violet | `#292a4d` | neutral | Prominent card borders and section separators |
| Nebula Blue | `#2c2b52` | neutral | Contrasting borders and section backgrounds |
| Graphite Border | `#353545` | neutral | Outlined button borders, subtle UI demarcations |
| Deep Iris | `#404267` | neutral | Decorative border accents, illustration strokes |
| Steel Gray | `#4f5160` | neutral | Subtle shadow color for UI elements |
| Faded Gray | `#9b9ba4` | neutral | Muted text, secondary labels, subtle borders |
| Light Ghost | `#fbf1ff` | neutral | Primary body text, headings, and icon fills |
| Vibrant Blue | `#0067ff` | accent | Blue wash for highlight backgrounds, decorative bands, and soft emphasis behind content |
| Electric Cyan | `#02e5ef` | brand | Hero gradient start (part of Aurora Borealis gradient) |
| Plasma Pink | `#512141` | brand | Subtle decorative background fills, highlights |
| Crimson Bloom | `#ff3a63` | brand | Red wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Galactic Rainbow | `#665eff` | brand | Decorative elements, internal card highlights |
| Status Green | `#22a06b` | accent | Green decorative accent for icons, marks, and small graphic details. Use as a supporting accent, not as a status color |
| Warning Yellow | `#ffff00` | accent | Yellow wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Use as a supporting accent, not as a status color |
| Golden Glow | `#ffe684` | accent | Yellow decorative accent for icons, marks, and small graphic details. Use as a supporting accent, not as a status color |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 11px, 13px, 14px, 15px, 16px, 17px, 18px, 24px |
| lineHeight | 1.50, 1.60 |
| letterSpacing | normal |
| fontFeatureSettings | "tnum" |
| substitute | system-ui |
| role | Body text, navigation links, secondary labels, and button text, providing a reliable and clear baseline for information. Uses tabular figures for data alignment. |

### Index

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 12px, 13px, 14px, 15px, 16px, 17px, 18px, 20px, 22px, 23px, 32px, 42px, 56px, 72px |
| lineHeight | 1.20, 1.30, 1.50, 1.60 |
| letterSpacing | 0.0030em at 72px, 0.0040em at 56px, 0.0050em at 42px, 0.0060em at 32px, 0.0090em at 23px, 0.0110em at 22px, 0.0130em at 20px, 0.0140em at 18px, 0.0150em at 17px |
| fontFeatureSettings | "tnum" |
| substitute | system-ui |
| role | Headlines, emphasizes large, impactful statements, using a custom font for distinct brand presence. The tighter letter spacing on larger sizes creates a cohesive, modern title feel. Uses tabular figures. |

### -apple-system

| Key | Value |
| --- | --- |
| weight | 500, 600 |
| sizes | 13px, 15px |
| lineHeight | 1.6 |
| role | -apple-system — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 | 0 |
| body-lg | 16 |  | 1.5 | 0 |
| subheading | 18 |  | 1.5 | 0.25 |
| heading-sm | 24 |  | 1.5 | 0 |
| heading | 32 |  | 1.2 | 0.19 |
| heading-lg | 42 |  | 1.2 | 0.21 |
| display | 56 |  | 1.2 | 0.22 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| lg | 20px |
| md | 12px |
| sm | 6px |
| xl | 24px |
| none | 0px |
| pill | 30px |
| round | 50px |

- **elementGap** — 8px
- **sectionGap** — 60px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Eclipse Gray Canvas | `#04061c` | 1 | Primary page background, base layer. |
| Twilight Violet Surface | `#0c0a2b` | 2 | Secondary backgrounds, default card level for content blocks. |
| Deep Space Panel | `#02030b` | 3 | Elevated card surfaces, product UI elements, embedded applications. |

## Components

### Navigation Link (Ghost)

**Role:** Top navigation items, secondary actions within complex UIs.

Text link, uses Inter 15px weight 400 '#fbf1ff' with no background or border, transitioning to '#9b9ba4' on hover. Radius is 12px for padding areas.

### Primary Action Button (Filled)

**Role:** Main call to action for the site and product onboarding.

Background: '#ff3a63' (Crimson Bloom), text: '#fbf1ff' (Light Ghost), border radius: 30px, padding: 5px vertical, 26px horizontal. Font is Inter 16px weight 500.

### Subtle Action Button (Pill)

**Role:** Informational prompts or secondary, less urgent actions.

Background: #0c0a2b (Twilight Violet) with 23% opacity, text: #fbf1ff (Light Ghost), border radius: 50px, padding: 5px vertical, 12px horizontal. Font is Inter 16px weight 500.

### Feature Card (Subdued)

**Role:** Container for features, testimonials, or content blocks.

Background: #0c0a2b (Twilight Violet), border: 1px solid #292a4d (Lunar Violet), border radius: 24px. Internal padding is 12px. Features a subtle radial gradient light source inside for emphasis.

### Data Row Card

**Role:** Items within a table or list view, emphasizing content over visual flourish.

Background: #13151f (Eclipse Gray), border: 1px solid #202333 (Gunmetal Gray), border radius: 6px. No significant shadow, prioritizing a flat, data-centric presentation.

### Interactive Tag

**Role:** Categorization, status labels, or embedded filter options.

Background: rgba(0,0,0,0) (transparent), text: '#fbf1ff' (Light Ghost), border: 1px solid '#fbf1ff' (Light Ghost), border radius: 12px. Padding: 2px vertical, 20px horizontal. Font is Inter 13px weight 500.

## Layout

The page maintains a centered, contained layout with a maximum width, allowing content to breathe within the dark theme. The hero section features a full-bleed, gradient-infused background acting as a cosmic backdrop for a centered, bold headline and subtext. Vertical rhythm is established through consistent section gaps anchored by 60px. Content sections alternate between visually distinct panels, often employing two-column text-left/visual-right arrangements or multi-column card grids for features. The overall impression is structured and spacious, with a sticky top navigation bar providing global access points.

## Imagery

The visual language focuses on abstract graphics and product screenshots within a UI context. Photography is minimal, appearing only for 'about us' style content. Illustrations are either iconic, simple geometric shapes (e.g., within buttons/tags), or abstract, volumetric forms rendered with brand gradients. Product screenshots are contained within dark, slightly elevated cards, often featuring subtle internal glow effects. Icons are usually outlined or filled, appearing crisp and often monocolor in white or a brand accent. Imagery serves primarily to explain product features or create a sense of atmospheric depth rather than decorative flourish. Density is balanced, with imagery typically occupying defined panels or sections, framed by surrounding UI.

## Dos & Donts

### Do

- Prioritize '#fbf1ff' for all primary text and '#9b9ba4' for supporting text and neutral links to maintain hierarchy and readability on dark backgrounds.
- Use '#04061c' as the default page background and '#02030b' for elevated sections and card backgrounds to create a clear surface hierarchy.
- Apply 30px border radius for all prominent buttons and input fields to achieve the brand's 'pill' shape, contrasting with the sharper 6px for tabular data cards.
- Use 12px as the internal padding for most card types, and 8px for spacing elements within content blocks for a compact layout.
- Enhance interactive elements like call-to-action buttons with the '#ff3a63' (Crimson Bloom) background, reserving this vibrant color for primary actions.
- Implement the 'Aurora Borealis' gradient (`linear-gradient(135deg, rgb(2, 229, 239) 0%, rgb(72, 89, 235) 71.5%, rgb(138, 56, 244) 100%)`) for hero sections or significant brand showcases only.
- Apply a 1px solid border using '#292a4d' for major card containers to define boundaries against similar dark backgrounds.

### Don't

- Avoid using light backgrounds. The system is designed for a dark mode experience; light themes will break the visual hierarchy.
- Do not introduce sharp corners on interactive components like buttons or tags; they should adhere to the established 30px or 12px radii.
- Do not deviate from the Inter for body text and Index for headlines; these fonts define the brand's typographic voice.
- Refrain from adding arbitrary shadows; use the defined subtle `oklch(0.2 0 0 / 0.4) 1px 2px 6px 0px` for minor elevation and avoid heavy box shadows.
- Do not use '#ff3a63' or '#22a06b' for decorative purposes; these are reserved for primary actions and semantic status indicators respectively.
- Avoid excessive spacing. Maintain a compact density, using 8px for element gaps to keep information focused.
- Do not use bold or extra-bold weights for headlines; the brand's headlines maintain impact through large sizing and precise tracking, at weights up to 600.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #fbf1ff
background: #04061c
border: #292a4d
accent: #0067ff
primary action: #04061c (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #04061c background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a Feature Card: '#0c0a2b' background, 1px solid '#292a4d' border, '24px' radius. Use Index 32px weight 600 headlines in '#fbf1ff' and Inter 16px weight 400 body text in '#9b9ba4'.
3. Build a Navigation Link: Inter 15px weight 400 text '#fbf1ff', '0px' background, '0px' border, and a hover state text color of '#9b9ba4'.

---
_Source: https://styles.refero.design/style/b136f0a0-8064-4978-a18e-db54b9362c24_
