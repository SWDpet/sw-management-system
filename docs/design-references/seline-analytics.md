# Seline Analytics — Design Reference

> Crisp Data Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://seline.so](https://seline.so) |
| Refero page | [https://styles.refero.design/style/7967c6d9-e50c-42b5-b4d1-74003ba41781](https://styles.refero.design/style/7967c6d9-e50c-42b5-b4d1-74003ba41781) |
| Theme | light |
| Industry | saas |

## Overview

Seline Analytics employs a focused, lightweight analytics dashboard aesthetic with a crisp, monochromatic base and a single vivid blue for active states and brand accent. Surfaces are airy with soft shadows, creating a sense of clarity and organization. Typography is precise and utilitarian, prioritizing readability with subtle letter-spacing. Components emphasize functionality over heavy styling, presenting data and controls in an approachable, streamlined manner.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Cloud White | `#ffffff` | neutral | Primary surface background, card surfaces, button backgrounds in nav, input backgrounds |
| Canvas Fog | `#fafaf9` | neutral | Page background, secondary surface where a subtle lift from Cloud White is needed |
| Slate Text | `#0c0a09` | neutral | Primary text, headings, strong text elements |
| Ash Gray | `#78716c` | neutral | Secondary text, muted helper text, iconography, default button text |
| Stone Border | `#e5e7eb` | neutral | Subtle borders, dividers, ghost button borders, inactive input borders |
| Platinum Outline | `#d6d3d1` | neutral | Input field borders, light separators, less prominent borders than Stone Border |
| Steel Gray | `#a8a29e` | neutral | Tertiary text, less important details, subtle icons |
| Hover Stone | `#c9c5c2` | neutral | Subtle hover states for text or borders |
| Ghost Ink | `#1c1917` | neutral | Background for certain ghost buttons on hover |
| Chartwell Blue | `#3ba6f1` | brand | Primary action background, active navigation indicators, key data points, brand accents in icons and links |
| Sky Tint | `#c1e1f7` | accent | Subtle background for certain body sections, providing a slight cool tint |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 12px, 13px, 14px, 15px, 16px, 18px |
| lineHeight | 1.00, 1.29, 1.33, 1.43, 1.50, 1.53, 1.54, 1.56, 1.64, 1.67, 1.69, 1.77, 1.92 |
| letterSpacing | -0.005em at 18px, 0.003em at 16px, 0.004em at 12px |
| substitute | system-ui |
| role | Body text, UI labels, small captions, navigation items, and descriptions. Prioritizes legibility over expressive flair. |

### roobert

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 16px, 18px, 20px, 32px, 52px |
| lineHeight | 1.00, 1.12, 1.20, 1.22, 1.25, 1.69 |
| letterSpacing | -0.025em for 52px, -0.021em for 32px, -0.017em for 20px |
| substitute | sans-serif |
| role | Headings and prominent display text. Its distinct letter-spacing at larger sizes adds a subtle character while maintaining a technical feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0.048 |
| heading-sm | 18 |  | 1.25 | -0.016 |
| heading | 20 |  | 1.2 | -0.017 |
| heading-lg | 32 |  | 1.12 | -0.021 |
| display | 52 |  | 1 | -0.025 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 10px |
| inputs | 4px |
| buttons | 9999px |
| largeCard | 16px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Fog | `#fafaf9` | 0 | Base page background. Provides a warm achromatic canvas for content. |
| Cloud White | `#ffffff` | 1 | Primary content surface for cards, modals, and primary UI elements. Sits directly above Canvas Fog. |

## Components

### Primary Filled Button

**Role:** Call to action.

Background: Chartwell Blue (#3ba6f1), Text: Cloud White (#ffffff), Radius: 9999px. Padding: 0px.

### Ghost Button - Light

**Role:** Secondary actions or navigation items.

Background: rgba(0,0,0,0) (transparent), Text: Ash Gray (#78716c), Border: 1px solid Stone Border (#e5e7eb) or transparent. Radius: 0px. Used for navigation and dashboard filters.

### Ghost Button - Dark Text

**Role:** Secondary actions with higher text prominence.

Background: rgba(0,0,0,0) (transparent), Text: Slate Text (#0c0a09), Border: 1px solid Stone Border (#e5e7eb). Radius: 4px. Used for 'Live demo' button.

### Subtle Ghost Button

**Role:** Tertiary actions, tabs, or selections within confined spaces.

Background: rgba(120,114,109,0.1) (Ash Gray tinted transparent), Text: Slate Text (#0c0a09), Border: 1px solid Stone Border (#e5e7eb). Radius: 4px. Minor internal actions and tab switching.

### Dashboard Card

**Role:** Container for data visualizations and content blocks.

Background: Cloud White (#ffffff), Border Radius: 10px, Shadow: rgba(0,0,0,0.05) 0px 4px 16px 0px. Padding: 24px.

### Pill Card

**Role:** Small, contained content chip.

Background: Cloud White (#ffffff), Border Radius: 9999px, Shadow: rgba(0,0,0,0.05) 0px 4px 16px 0px. Padding: 4px 12px.

### Elevated Feature Card

**Role:** Prominent feature block.

Background: Cloud White (#ffffff), Border Radius: 16px, Shadow: rgba(17,12,46,0.12) 0px 12px 45px 0px. Padding: 8px.

### Standard Input Field

**Role:** Text input areas.

Background: Cloud White (#ffffff), Text: Slate Text (#0c0a09), Border: 1px solid Platinum Outline (#d6d3d1). Radius: 0px. Placeholder text: Ash Gray (#78716c).

### Rounded Input Field

**Role:** Filter input or search fields.

Background: Cloud White (#ffffff), Text: Ash Gray (#78716c), Border: 1px solid Platinum Outline (#d6d3d1). Radius: 6px. Padding: 4px 12px.

## Layout

The page uses a maximum width containment for its main content, centered on the screen, but the navigation bar extends full-bleed across the top. The hero section features a large centered headline and calls to action over a Canvas Fog background, with an illustrative element visually balancing the right side. Content sections generally follow a vertical rhythm with consistent section gaps of 48px, often containing card grids or alternating text-and-image blocks. The layout is spaced and uncluttered, with a clear hierarchy established by abundant negative space and light visual dividers.

## Imagery

The site uses a combination of product screenshots, abstract illustrations, and minimal iconography. Product screenshots are contained within cards, depicting a clean, functional dashboard UI. Abstract illustrations are sparse, featuring organic, cloud-like shapes in a light gray tone that blends with the background, acting as subtle atmospheric elements rather than focal points. Icons are outlined, typically in Ash Gray (#78716c) or Slate Text (#0c0a09), with some instances of Chartwell Blue (#3ba6f1) for emphasis. Imagery serves primarily to explain product functionality or provide decorative atmosphere at a low visual density.

## Dos & Donts

### Do

- Prioritize Cloud White (#ffffff) for card backgrounds and Canvas Fog (#fafaf9) for main page backgrounds to reinforce a light, airy feel.
- Use Chartwell Blue (#3ba6f1) exclusively for primary calls to action, active states, and brand iconography to maintain its impact.
- Apply Slab Text (#0c0a09) for all headings and primary body text, ensuring high contrast and readability.
- Utilize Inter for all body copy and UI elements, paired with roobert for headlines to provide distinct typographic roles.
- Maintain a compact button design with minimal vertical padding and 9999px border-radius for a consistent modern pill shape.
- Implement 10px border-radius for all data cards and larger containers, providing soft, approachable corners.
- Use the rgba(0,0,0,0.05) 0px 4px 16px 0px shadow for most card elevations, reserving the deeper rgba(17,12,46,0.12) 0px 12px 45px 0px for features that require significant visual prominence.

### Don't

- Avoid introducing additional saturated colors beyond Chartwell Blue (#3ba6f1) to maintain the focused monochromatic aesthetic.
- Do not use heavy, dark backgrounds for sections or components; the system relies on lighter surfaces for visual clarity.
- Refrain from using strong, angular shapes or hard edges; stick to the established subtle curves and rounded corners.
- Do not use font weights above 500 for roobert or 600 for Inter; the system avoids heavy typography to feel lightweight.
- Avoid deep, dark shadows for components. Surface elevation should be subtle, using light, diffused shadows.
- Do not use generic system fonts in place of Inter or roobert for new UI elements; their specific letter-spacing and proportions are key to the brand's typographic identity.
- Do not vary line-height significantly from the established values; consistent line-height (1.5 for Inter, 1.25 for roobert) ensures a uniform content density.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #0c0a09
background: #fafaf9
border: #e5e7eb
accent: #c1e1f7
primary action: #3ba6f1 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #3ba6f1 background, #0c0a09 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a data card: Cloud White (#ffffff) background, 10px radius, shadow rgba(0,0,0,0.05) 0px 4px 16px 0px. Inside, use Inter 14px weight 400, #0c0a09 for data labels and Inter 18px weight 600, #0c0a09 for metric values. Ensure 24px padding on all sides.
3. Implement a navigation bar item: 'Pricing' with Inter 16px weight 500, Ash Gray (#78716c). On hover, text color should be Slate Text (#0c0a09). For an active item, include a Chartwell Blue (#3ba6f1) underline.

---
_Source: https://styles.refero.design/style/7967c6d9-e50c-42b5-b4d1-74003ba41781_
