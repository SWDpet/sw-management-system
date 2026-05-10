# Intercom — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://intercom.com](https://intercom.com) |
| Refero page | [https://styles.refero.design/style/12255b63-e506-4bc1-a4cd-d05487de32f3](https://styles.refero.design/style/12255b63-e506-4bc1-a4cd-d05487de32f3) |
| Theme | light |
| Industry | ai |

## Overview

Intercom's design system evokes a sense of understated innovation, like an architectural blueprint on white marble.  Lightness and clarity are established through an expansive white canvas contrasted by fine-lined black typography and minimal, precise UI elements. The tension between the nearly achromatic palette and a single vibrant violet accent for primary calls to action creates a focused, forward-thinking feel without visual clutter. Custom typography with subtle letter-spacing variations adds a layer of quiet sophistication, preventing the sparse aesthetic from feeling sterile.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, primary surfaces, overlay base. |
| Background Off-White | `#faf9f6` | neutral | Subtle background for sections or softer UI elements. |
| Surface Cream | `#f1eee9` | neutral | Slightly elevated surface, subtle background differentiation. |
| Border Sand | `#dedbd6` | neutral | Delicate borders for subtle UI separation, active tab indicators. |
| Subtle Gray | `#e7e3db` | neutral | More pronounced background for distinct content blocks, light dividers. |
| Canvas Beige | `#d3cec6` | neutral | Background for secondary content areas or muted sections. |
| Headline Black | `#111111` | neutral | Primary heading text, strong impactful body copy. |
| Body Text Black | `#000000` | neutral | General body text, links, primary UI elements, icons. |
| Subtle Graphite | `#414141` | neutral | Muted text for less prominent information, secondary links. |
| Mid Gray | `#585858` | neutral | Secondary text, descriptive elements with slightly less emphasis than body text. |
| Footer Gray | `#666666` | neutral | Tertiary text, footer links, less critical information. |
| Icon Gray | `#707070` | neutral | Icons and very subtle UI elements. |
| Button Text Gray | `#888888` | neutral | Text for secondary buttons, disabled states. |
| Placeholder Gray | `#a0a0a0` | neutral | Placeholder text in input fields, further muted text elements. |
| Inactive Icon Gray | `#b8b8b8` | neutral | Icons in inactive or secondary states. |
| Accent Violet | `#0007cb` | brand | Primary interactive elements, call-to-action buttons, active indicators — a vivid modern accent against the neutral palette. |
| Accent Orange | `#ff5600` | accent | Highlighting specific words or small interactive elements, used sparingly for emphasis. |

## Typography

### Saans

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 14px, 16px, 20px, 24px, 32px, 40px, 54px, 80px |
| lineHeight | 0.95, 1.00, 1.25, 1.40, 1.43, 1.50 |
| letterSpacing | -2.4, -1.62, -0.8, -0.16 |
| substitute | system-ui, sans-serif |
| role | The primary typeface for headings, body text, and general UI. Weight 300 for display sizes provides a refined, non-aggressive presence. |

### SaansMono

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| sizes | 12px, 14px |
| lineHeight | 1.00, 1.30, 1.40 |
| letterSpacing | 1.2, 0.7 |
| substitute | SFMono-Regular, monospace |
| role | Used for technical information, code snippets, or any content requiring monospace presentation. The increased letter-spacing distinguishes it. |

### Serrif

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 16px |
| lineHeight | 1.40 |
| letterSpacing | -0.16 |
| substitute | serif |
| role | A decorative serif font used sparingly, likely for emphasis within body text or specific callouts. Its light weight keeps it subtle. |

### MediumLL

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px |
| lineHeight | 0.94, 1.00, 1.29 |
| letterSpacing | -0.4, -0.35, 0.56 |
| substitute | system-ui, sans-serif |
| role | Used for navigation elements and specific UI labels, characterized by tight letter-spacing for concise presentation. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.4 | 0.7 |
| body-sm | 14 |  | 1.5 | -0.16 |
| body | 16 |  | 1.5 | -0.16 |
| subheading | 20 |  | 1.4 | -0.4 |
| heading-sm | 24 |  | 1.25 | -0.48 |
| heading | 32 |  | 1.25 | -0.8 |
| heading-lg | 40 |  | 1 | -0.8 |
| display | 54 |  | 1 | -1.62 |
| display-lg | 80 |  | 0.95 | -2.4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 4px |
| navItems | 4px |

- **elementGap** — 16px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background, base layer. |
| Background Off-White | `#faf9f6` | 1 | Subtle background for distinct content sections or cards, offering minimal elevation. |
| Surface Cream | `#f1eee9` | 2 | More pronounced background for feature blocks or secondary content areas, indicating a slight lift. |
| Subtle Gray | `#e7e3db` | 3 | Used for banners or highly differentiated content blocks, suggesting a higher level of separation. |

## Components

### Alert Banner

### Tab Bar

### Button Group

### Primary Action Button

**Role:** Call to action

Solid black background (#111111), white text (#ffffff), 4px border-radius, 14px horizontal padding, 0px vertical padding. Focus and hover states use Accent Violet (#0007cb) for the background.

### Secondary Outline Button

**Role:** Secondary action

Transparent background, Headline Black (#111111) text, Border Sand (#dedbd6) border, 4px border-radius, 14px horizontal padding, 0px vertical padding. Text color matches Body Text Black (#111111). Hover state uses Border Sand (#dedbd6) for border.

### Ghost Navigation Button

**Role:** Navigation link

Transparent background, Body Text Black (#000000) text (color #111111 for main nav, #000000 for sub nav), no border, no radius. Tight padding; active state indicated by underline or Border Sand (#dedbd6) hover effect.

### Tab Button

**Role:** Tab selection

Transparent background, muted gray text (oklab(0.177637 / 0.5) for default, #111111 for active), Border Sand (#dedbd6) bottom border for active state only, 16px padding on all sides. No border-radius.

### Primary Navigation Item

**Role:** Main navigation

Text in Headline Black (#111111), Saans weight 400 at 16px, line-height 1.25. Has a 4px border-radius on hover/active states with Background Off-White (#faf9f6) as background color.

### Header Alert Banner

**Role:** Informational banner

Background #e7e3db. Text in Headline Black (#111111), Saans weight 400 at 14px. Padding of 8px vertically and 16px horizontally.

### Logo Grid Item

**Role:** Client/partner logo display

Logo typically in Inactive Icon Gray (#b8b8b8) or Body Text Black (#000000). Displayed on page Canvas White (#ffffff) or Background Off-White (#faf9f6) background within a grid, often with no explicit padding on logo itself, relying on grid spacing.

### Input Field

**Role:** Data entry

Assumed white background with a subtle border in Border Sand (#dedbd6) or a similar muted neutral. Placeholder text in Placeholder Gray (#a0a0a0). Focus ring would likely be Accent Violet (#0007cb) or a darker neutral.

## Layout

The page primarily uses a max-width contained layout, though the hero section spans full-bleed to establish a grander scale. The hero features a centered oversized headline (Saans 80px, weight 300) with a secondary text block right-aligned. Sections often alternate between full-width content blocks and contained layouts. Content arrangement frequently uses a split-screen pattern (text column left, visual/product screenshot right, or vice versa) with consistent vertical spacing between sections. There's a minimal use of explicit grids for feature showcasing, but logo sections use a fluid, adaptive grid. The layout prioritizes spaciousness, using ample whitespace around elements and between sections to keep information digestible. The navigation is a sticky header with primary links on the left and action buttons (Start free trial, Fin AI Agent) on the right.

## Imagery

The visual language focuses on a mix of product screenshots, abstract and illustrative graphics, and subtly treated photography. Product screenshots are clean, showcasing the UI in action, often on a slight perspective. Abstract illustrations feature monochromatic human-like figures or organic shapes (like the flower graphic) in black against the dominant white background, serving as decorative atmosphere rather than direct content. Photography is minimal, often cropped tightly or treated with filters/desaturation, focusing on human elements (eyes, silhouettes) without being overtly vibrant or lifestyle-oriented. Iconography is primarily outlined and monochromatic, aligning with the refined, clean aesthetic. The role is a blend of product showcase, conceptual atmosphere, and light visual breaks, with a strong emphasis on maintaining a text-dominant layout.

## Dos & Donts

### Do

- Use Headline Black (#111111) for all major headings and impactful statements to maintain strong contrast.
- Apply Saans font with a weight of 300 for display-sized headlines (54px, 80px) to achieve an authoritative yet understated feel.
- Implement 4px border-radius for all interactive buttons and navigation items, creating a subtle visual softness.
- Reserve Accent Violet (#0007cb) strictly for primary interactive elements, such as CTA buttons and active state indicators.
- Differentiate sections using the neutral background progression: Canvas White (#ffffff) > Background Off-White (#faf9f6) > Surface Cream (#f1eee9) for subtle visual hierarchy.
- Maintain standard element spacing of 16px for comfortable content flow, adjusting vertically with multiples of 8px as needed.
- Utilize SaansMono for any technical or explicit code-like content, with its distinctive increased letter-spacing.

### Don't

- Avoid using saturated colors other than Accent Violet (#0007cb) and Accent Orange (#ff5600) to preserve the clean, neutral aesthetic.
- Do not introduce sharp, unrounded corners on interactive elements, as this contradicts the established 4px radius pattern.
- Refrain from heavy drop shadows or complex gradients; the system relies on subtle background shifts and crisp lines for depth.
- Do not deviate from the specified Saans, SaansMono, or MediumLL typefaces; ensure consistency in typographic personality.
- Avoid dense, information-heavy blocks without adequate spacing; prioritize comfortable content density and readability.
- Do not use Body Text Black (#000000) for large, prominent headlines; Headline Black (#111111) should be preferred for impact.
- Do not use highly saturated photography; imagery should align with the muted or monochromatic style.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- `text-primary`: #111111
- `bg-canvas`: #ffffff
- `bg-card`: #faf9f6
- `cta-primary`: #0007cb
- `border-subtle`: #dedbd6

**3-5 Example Component Prompts:**
1. Create a hero section: Canvas White (#ffffff) background. Headline 'The only helpdesk designed for the AI Agent era' using Saans weight 300, 80px size, line-height 0.95, letter-spacing -2.4px, color Headline Black (#111111). Subtext 'Intercom is the only helpdesk with a natively integrated AI Agent.' using Saans weight 400, 16px size, line-height 1.5, color Body Text Black (#000000). Include a Primary Action Button 'Start free trial' and a Secondary Outline Button 'View demo', both with 4px radius and 14px horizontal padding.
2. Design a feature card: Background Off-White (#faf9f6) background. Heading 'AI-powered Insights' in Saans weight 400, 24px size, line-height 1.25, letter-spacing -0.48px, color Headline Black (#111111). Body text in Saans weight 400, 16px size, line-height 1.5, color Body Text Black (#000000). Pad content with 16px on all sides.
3. Construct a navigation bar: Canvas White (#ffffff) background. Logo on left. Navigation items 'Product', 'Customers', 'Resources', 'Pricing' using MediumLL weight 400, 16px size, color Headline Black (#111111), letter-spacing -0.4px. Right side has 'Contact sales' (Ghost Navigation Button), 'View demo' (Secondary Outline Button), and 'Start free trial' (Primary Action Button). All elements should have 8px horizontal gap.

---
_Source: https://styles.refero.design/style/12255b63-e506-4bc1-a4cd-d05487de32f3_
