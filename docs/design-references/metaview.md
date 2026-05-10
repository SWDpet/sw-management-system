# Metaview — Design Reference

> Midnight Command Center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://metaview.ai](https://metaview.ai) |
| Refero page | [https://styles.refero.design/style/f99856e1-3627-4624-a811-f6053a978b62](https://styles.refero.design/style/f99856e1-3627-4624-a811-f6053a978b62) |
| Theme | dark |
| Industry | ai |

## Overview

Metaview employs a high-contrast dark aesthetic that feels like a command center: deep black backgrounds, subtly glowing dark surfaces, and text in crisp white or muted gray. A singular vivid green piercing through the darkness emphasizes key interactive elements and creates an immediate sense of action. Typography is clean and functional, with ample spacing creating a sense of calm authority within the otherwise intense color scheme.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Void Black | `#000000` | neutral | Page backgrounds, primary text for light elements, major borders providing structure |
| Ghost White | `#ffffff` | neutral | Primary text on dark backgrounds, interactive elements, button backgrounds, and card surfaces for contrast |
| Carbon | `#161818` | neutral | Subtle background for elevated dark cards and secondary content blocks |
| Deep Space | `#0a1a14` | neutral | Darker card backgrounds, slightly recessed elements within the dark UI |
| Twilight Slate | `#01051b` | neutral | Muted text, secondary button borders, and decorative icon strokes. This dark blue-gray acts as a subtle accent against the black |
| Ash Gray | `#5e6262` | neutral | Helper text, less prominent body copy, and secondary meta information |
| Slate Border | `#828282` | neutral | Subtle borders and dividers within the dark interface |
| Cloud Gray | `#d9d9d9` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Mint Glare | `#7affb4` | brand | Primary call-to-action button backgrounds and active state indicators—this vivid green is the sole splash of strong color |
| Soft Mint | `#e3ffef` | accent | Light highlight for specific text elements or subtle decorative borders |
| Dark Forest | `#000a06` | neutral | Deep background colors or subtle textual cues in a near-black tone |
| Subtle Green | `#0a2119` | neutral | Secondary body text, navigation labels, and subdued headings. Do not promote it to the primary CTA color |
| Digital Glow Radial Gradient | `#006446` | accent | Subtle background element, creating a diffused green light effect |
| Digital Glow Linear Gradient | `#0f3a2b` | accent | Background gradient often used at section breaks or for subtle depth |

## Typography

### Euclid Circular A

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| weights | 300, 400, 500, 700 |
| sizes | 12px, 14px, 15px, 16px, 18px, 20px, 22px, 28px, 36px, 48px, 68px, 72px |
| lineHeight | 1.00, 1.04, 1.10, 1.12, 1.16, 1.20, 1.30, 1.34, 1.42, 1.48, 1.50, 1.60 |
| letterSpacing | -0.06em at 72px, -0.04em at 48px, -0.03em at 36px, -0.02em at 28px, -0.01em at 22px, 0.01em at 12px |
| substitute | system-ui, sans-serif |
| role | Primary brand typeface for all headings, body text, and UI elements. Its clean, geometric forms maintain clarity and efficiency, reinforcing the AI-driven identity. The variable letter-spacing provides visual precision at different sizes. |

### Onsite SemiMono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px, 16px |
| lineHeight | 1.48, 1.60 |
| letterSpacing | 0.01em |
| substitute | Menlo, Consolas, monospace |
| role | Used for code snippets, technical details, or specific data displays, offering a technical and precise counterpoint to the primary typeface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.6 | 0.12 |
| body-sm | 14 |  | 1.48 |  |
| body | 16 |  | 1.6 |  |
| subheading | 18 |  | 1.48 |  |
| heading-sm | 22 |  | 1.34 | -0.22 |
| heading | 28 |  | 1.2 | -0.56 |
| heading-lg | 36 |  | 1.12 | -1.08 |
| display | 48 |  | 1.04 | -1.92 |
| display-lg | 72 |  | 1 | -4.32 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| misc | 12px |
| tags | 8px |
| cards | 16px |
| icons | 4px |
| buttons | 999px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Primary Action Button

**Role:** Call to action

Filled Mint Glare button with Ghost White text. Fully rounded for approachability. Uses a 999px radius and 8px vertical, 16px horizontal padding. Example: 'Start for free'.

### Secondary Action Button

**Role:** Alternative action

Ghost White filled button with Void Black text. Fully rounded for approachability. Uses a 999px radius and 8px vertical, 16px horizontal padding. Example: 'Book a demo'.

### Ghost Button

**Role:** Tertiary action, navigation

Transparent background with Ghost White text. Minimal padding (8px all around) and 8px border radius. Used for less prominent actions or navigation items. Example: Navigation links like 'Sign in'.

### Navigation Link

**Role:** Primary navigation

Ghost White text on a Void Black background, no explicit button styling. Simple padding 8px to 16px horizontally. Example: 'Platform', 'Customers'.

### Dark Card (Product View)

**Role:** Content container for product features/data

Deep Space background with 16px border radius and 16px padding on all sides. Used for displaying specific feature details or data snippets within the product interface.

### Dark Elevated Card (Product Content)

**Role:** Elevated content container

Carbon background with 16px border radius. Padding varies (16px, 24px) based on content. No shadow by default to maintain flat dark aesthetic. Used for larger content blocks within the product UI, like the Sourcing list. Note uses a single inset shadow to differentiate states.

### Light Card (Feature Showcase)

**Role:** Showcase content, testimonials

Ghost White background with 16px border radius. Generous 24px padding on all sides. Used for sections that require higher contrast hero content such as testimonials or key feature callouts where the content itself is the highlight.

### Information Tag

**Role:** Categorization, metadata

Faint gray background (#D9D9D9 with low opacity) with Twilight Slate text, 8px border radius. Padding is not explicitly defined but consistent with small elements. Used for showing categories like 'Source', 'Research', 'Web and ATS'.

## Layout

The page primarily uses a full-bleed layout, allowing the dark backgrounds and atmospheric gradients to span the entire viewport. Content is horizontally centered within a logical maximum width (likely around 1200-1400px, though not explicitly defined). The hero section features a centered headline and subtext over a deep black background. Sections exhibit consistent vertical spacing, often divided by large, full-bleed color changes or subtle gradients. Content is frequently arranged in multi-column grids or alternating text-left/image-right configurations, especially for feature showcases. The navigation is a sticky top bar with a left-aligned logo and right-aligned links and buttons.

## Imagery

This site uses product screenshots and abstract vector iconography. Product screenshots are contained within dark cards, showcasing the UI directly without external context. Icons are typically outlined or filled in monochromatic Ghost White or Twilight Slate, maintaining a lightweight, functional feel. The overall imagery density is image-heavy in sections showcasing the product, but text-dominant around explanatory content. Large atmospheric gradients (e.g., Digital Glow Radial Gradient) are used as background elements, providing subtle color shifts rather than explicit images.

## Dos & Donts

### Do

- Prioritize the Void Black background (#000000) for primary canvases and use Ghost White (#ffffff) for primary text on these surfaces to maintain strong contrast.
- Use Mint Glare (#7affb4) exclusively for primary calls-to-action to highlight critical interactions.
- Employ Euclid Circular A for all textual elements. Use heavier weights (500, 700) sparingly for emphasis, leaning on 300-400 for most content and headings. Use specified letter-spacing values from the type scale.
- Apply a 999px border radius to all primary and secondary buttons, and a 16px radius to cards to achieve the brand's distinct shape language.
- Use elementGap (8px) for consistent spacing between logical UI elements like text and buttons or icon and text.
- Utilize dark background variants like Deep Space (#0a1a14) and Carbon (#161818) for secondary surfaces to create subtle visual depth within the dark theme.
- Ensure all interactive elements have a clear Hover/Focus state that references Mint Glare for visual feedback.

### Don't

- Avoid introducing additional saturated colors; maintain Mint Glare (#7affb4) as the sole vivid accent.
- Do not use box-shadows for elevation; instead, differentiate surfaces using changes in background color (Void Black, Deep Space, Carbon) and subtle inset borders (e.g., rgba(1, 5, 27, 0.06) 0px 0px 0px 1px inset).
- Do not use generic system fonts; Euclid Circular A and Onsite SemiMono are critical to the brand identity.
- Avoid tight spacing; maintain a 'comfortable' density using the established `elementGap` (8px), `cardPadding` (16px), and `sectionGap` (40px).
- Do not apply rounded corners indiscriminately; adhere to the specified radii for buttons (999px), cards (16px), and tags (8px).
- Do not use large, decorative gradients; only the provided radial and linear gradients are permitted, and they should be used subtly for atmosphere, not as primary design elements.
- Avoid making body text too prominent; use Ash Gray (#5e6262) for less important body copy to maintain hierarchy.

## Notes

### Agent Prompt Guide

**Quick Color Reference**
- text: #ffffff
- background: #000000
- border: #01051b (for secondary elements), #000000 (for strong separation)
- accent: #e3ffef
- primary action: #7affb4 (filled action)

**3-5 Example Component Prompts**
- Create a Primary Action Button: #7affb4 background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Design a Dark Card (Product View): Deep Space (#0a1a14) background, 16px border radius, 16px padding. Inside, use Ghost White text (Euclid Circular A, 16px, weight 400) for labels like 'Sourcing' and 'Application Review'.
- Create a Ghost Button: Transparent background, Ghost White (#ffffff) text (Euclid Circular A, 16px, weight 400), 8px border radius, 8px padding. Example: 'Sign in'.
- Construct an Information Tag: Use Cloud Gray (#d9d9d9) with 20% opacity as background, Twilight Slate (#01051b) text (Euclid Circular A, 12px, weight 500), 8px border radius, 4px vertical, 8px horizontal padding. Example: 'Source'.

---
_Source: https://styles.refero.design/style/f99856e1-3627-4624-a811-f6053a978b62_
