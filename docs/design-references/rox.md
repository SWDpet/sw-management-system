# Rox — Design Reference

> Analytical Blueprint on Pure White. An interface that feels like a meticulously charted course on a pristine, well-lit canvas.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://rox.com](https://rox.com) |
| Refero page | [https://styles.refero.design/style/66eb1c37-a8e5-4e6c-b17f-a75385b462e7](https://styles.refero.design/style/66eb1c37-a8e5-4e6c-b17f-a75385b462e7) |
| Theme | light |
| Industry | fintech |

## Overview

Rox exudes a focused, data-driven clarity, presenting complex financial automation with understated confidence. The design leverages a monochrome palette with strategic pops of a vibrant blue and a scattered, almost playful set of bright accent colors in secondary elements. Dominating the visual landscape is a unique pairing of a classic serif display font for impactful headlines and a clean, modern sans-serif for body text, creating a formal yet approachable feel. The near-achromatic backgrounds and lack of strong shadows contribute to a flat, spacious interface, elevating content through thoughtful typography rather than heavy visual effects. A subtle brand blue is employed sparingly, primarily to highlight interactive elements, guiding user attention with precision.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Page Canvas | `#f5f5f4` | neutral | Primary background for pages and major sections, providing a clean, bright foundation. |
| Surface White | `#ffffff` | neutral | Used for cards, panels, and elements needing to stand out slightly from the main background, often appearing as content containers. |
| Blueprint Blue | `#0b64e9` | brand | Primary brand accent, used for all calls-to-action, interactive states, and key navigational elements to draw attention without being overwhelming. |
| Text Primary | `#0c0a09` | neutral | Main body text, headlines, and critical information for maximum readability against light backgrounds. |
| Text Secondary | `#1c1917` | neutral | Subheadings, supporting text, and less emphasized information, a subtle step lighter than primary text but still high contrast. |
| Text Muted | `#a6a09b` | neutral | Placeholder text, minor labels, and supplementary details, providing a softer visual presence. |
| Text Subtle | `#57534d` | neutral | Less prominent text like captions or descriptions, visually receding while remaining legible. |
| Subtle Gray | `#ececea` | neutral | Backgrounds for subtle containers like badges or minor card elements, offering a hint of differentiation. |
| Border Light | `#f0efef` | neutral | Distinguishes UI elements with a subtle border, especially for form fields and interactive elements. |
| Disabled Gray | `#d4d2d1` | neutral | Used for disabled states of interactive components, indicating non-interactability. |
| Status Red | `#f24149` | semantic | Indicator for errors or important alerts, drawing quick attention. |
| Status Orange | `#f97006` | semantic | Highlighting warnings or moderate priority information. |
| Status Yellow | `#f9b703` | semantic | For informational highlights or less critical status indicators. |
| Status Violet | `#6b4aff` | semantic | Likely for specific status tags or categories, providing visual distinction. |

## Typography

### FH Total Display Regular

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 106px, 183px |
| lineHeight | 0.80 |
| substitute | Playfair Display |
| role | Hero and display headings — the signature typeface for brand impact, creating an elegant, authoritative presence with extreme size and tight line height. |

### Geist

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 14px, 16px, 18px, 20px, 24px, 28px |
| lineHeight | 1.20, 1.30, 1.40 |
| letterSpacing | -0.02 |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| substitute | Inter |
| role | Primary body and subheadings — a modern, geometric sans-serif that balances the classic display font with clarity and digital readability. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 10px, 12px, 14px |
| lineHeight | 1.30 |
| letterSpacing | -0.03 |
| role | Secondary body text and utility content — ensures broad compatibility and performance for smaller text blocks, leveraging system fonts for efficiency. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| role | Smallest UI text, labels, and metadata — a fallback simple sans-serif for minimal text elements where space is constrained. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.3 | -0.03 |
| body | 14 |  | 1.3 | -0.02 |
| heading | 18 |  | 1.3 | -0.02 |
| heading-lg | 20 |  | 1.2 | -0.02 |
| display-sm | 24 |  | 1.2 | -0.02 |
| display | 28 |  | 1.2 | -0.02 |
| hero-headline-1 | 106 |  | 0.8 |  |
| hero-headline-2 | 183 |  | 0.8 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 100px |
| large | 12px |
| buttons | 8px |
| default | 6px |

- **elementGap** — 4-16px
- **sectionGap** — 
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Workflow Tab Bar

### Outbound Agent Dashboard Card

### CTA Button Group with Announcement Badge

### Primary Filled Button

**Role:** Interactive element

Solid Blueprint Blue background (#0b64e9), white text (#ffffff), 8px border radius, 12px vertical padding, 12-16px horizontal padding. Prominently signals primary actions.

### Secondary Outlined Button

**Role:** Interactive element

Subtle Gray background (#f0efef), Blueprint Blue text (#0000ee - browser default, inferred brand blue of #0b64e9), with a 1px border matching the text color or Blueprint Blue if no border color is specified. 8px border radius, 8px vertical padding, 12px horizontal padding. Used for less prominent actions.

### Text Link

**Role:** Navigation/Interactive text

Blueprint Blue text (#0b64e9, although data shows browser default #0000ee for links), typically Geist font family. Used for in-line navigation and clickable text.

### Header Navigation Item

**Role:** Global Navigation

Typically Geist font, weight 400, size 14px, #a6a09b text transforming to #000000 on hover. Simple text link for top-level navigation.

### Display Headline - 'The Grand Statement'

**Role:** Hero content

FH Total Display Regular, 183px, lineHeight 0.8, color #1c1917, typically followed by a muted equivalent at 106px in #d4d2d1, creating a layered, emphasized headline effect for hero sections.

### Card Container

**Role:** Content grouping

Surface White background (#ffffff) with a subtle shadow (rgba(0, 0, 0, 0.06) 0px 2px 4px 0px or rgba(0, 0, 0, 0.1) 0px 1px 2px 0px). Default border radius is 6px. Padding for content is not explicitly defined but visually appears to be around 16px.

### Status Tag

**Role:** Categorization/Label

Small text (system-ui, 12px, weight 400), with varied background colors like Status Red (#f24149), Orange (#f97006), Yellow (#f9b703), or Violet (#6b4aff). Likely has small padding and a 6px border radius, similar to buttons.

## Layout

The page primarily uses a max-width contained layout, likely centered, though specific max-width is not defined. The hero section features a prominent, centered display headline over a clean 'Page Canvas' background. Sections are generally vertically stacked with visible spacing, creating a spacious feel. Content arrangement often appears as a centered stack or a simple column with text-heavy information. There are instances of multi-column arrangements, such as a feature list or a card grid, but the overall presentation emphasizes clear, uncongested blocks of information. The navigation is a standard top bar, sticky or otherwise. The second screenshot shows a contained application UI with tabs and internal card-like structures, indicating an application-like interface.

## Imagery

The visual language is primarily UI-focused, featuring crisp product screenshots and abstract, geometric graphics. Product screenshots are typically contained within device mockups or simple rectangular frames, presenting the software functionality clearly. There's an absence of traditional photography or complex illustrations. Iconography is minimalist, outlined or filled in monochrome or brand blue, serving mostly as functional cues. The density of imagery is balanced, supporting the textual content rather than dominating it, aiming for explanatory clarity over decorative atmosphere. There are small, scattered instances of vivid color (yellow, red, orange, violet) used in elements that appear as status indicators or small data points, implying a data visualization or tagging purpose.

## Dos & Donts

### Do

- Use 'FH Total Display Regular' solely for hero-level headlines (106px, 183px) to establish brand gravitas; reserve serif usage for maximum impact.
- Apply 'Blueprint Blue' (#0b64e9) exclusively for primary calls-to-action and active states to maintain clear visual hierarchy.
- Employ the '#f5f5f4' 'Page Canvas' for all primary page backgrounds to ensure an expansive, clean aesthetic.
- Utilize Geist font with a -0.02em letter-spacing for all body text and subheadings to maintain the distinct digital typography.
- Standardize on 6px default radii for all general elements and 8px for buttons, except for pill shapes which use 100px.
- Always use 'Text Primary' (#0c0a09) for main body copy and 'Text Secondary' (#1c1917) for sub-content on light backgrounds for optimal contrast.

### Don't

- Do not use multiple saturated colors for primary interactive elements; Blueprint Blue (#0b64e9) serves as the singular brand identifier.
- Avoid strong, heavy drop shadows; instead, use subtle shadows like rgba(0, 0, 0, 0.06) 0px 2px 4px 0px for minimal elevation.
- Do not use generic system fonts for prominent headings; FH Total Display Regular is reserved for brand distinction.
- Refrain from using color to signify hierarchy on text elements; instead, rely on font weights, sizes, and the specified neutral color scale (Text Primary, Secondary, Muted).
- Do not introduce new border radii beyond 1px, 6px, 8px, 12px, 16px, 20px, 30px, 36px, and 100px to maintain consistent geometric rhythm.

## Notes

### Agent Prompt Guide

1. **Quick Color Reference:**
   - Text Primary: #0c0a09
   - Page Background: #f5f5f4
   - CTA Blue: #0b64e9
   - Surface White: #ffffff
   - Text Muted: #a6a09b

2. **Example Component Prompts:**
   - Create a primary filled button: Blueprint Blue background (#0b64e9), white text (#ffffff), 8px radius, with 12px vertical padding and 16px horizontal padding. Text in Geist, 16px, weight 500.
   - Generate a card container: Surface White background (#ffffff), 6px radius, with a subtle shadow (rgba(0, 0, 0, 0.1) 0px 1px 2px 0px). Content padding 16px.
   - Design a hero section headline: 'Revenue.' in FH Total Display Regular, 183px, weight 400, color #1c1917, lineHeight 0.8. Below it, 'On autopilot.' in FH Total Display Regular, 106px, weight 400, color #d4d2d1, lineHeight 0.8.
   - Produce a status tag: Status Red background (#f24149), white text, 6px radius, small padding (e.g., 4px vertical, 8px horizontal). Text in system-ui, 12px, weight 400.
   - Create a secondary outlined button: Subtle Gray background (#f0efef), Blueprint Blue text (#0b64e9), 1px border in Blueprint Blue, 8px radius, 8px vertical padding, 12px horizontal padding. Text in Geist, 14px, weight 400.

---
_Source: https://styles.refero.design/style/66eb1c37-a8e5-4e6c-b17f-a75385b462e7_
