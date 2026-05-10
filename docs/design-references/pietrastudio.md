# Pietrastudio — Design Reference

> Warm digital canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.pietrastudio.com](https://www.pietrastudio.com) |
| Refero page | [https://styles.refero.design/style/577eb7d8-3555-4378-83df-0cebebc4782f](https://styles.refero.design/style/577eb7d8-3555-4378-83df-0cebebc4782f) |
| Theme | light |
| Industry | saas |

## Overview

Pietra exudes a dynamic, clean, digital canvas aesthetic. Its visual system is built on a crisp white backdrop, punctuated by a vibrant, warm orange action color and muted, pastel gradients that provide visual interest and a sense of depth without overwhelming the UI. Typography is compact and understated, allowing the strong contrasts of the brand orange and deep charcoal text to command attention. Components tend towards soft, rounded shapes and subtle shadow effects, creating a friendly yet capable interface.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, primary text on dark backgrounds |
| Stone Grey | `#f8f6f2` | neutral | Subtly elevated card surfaces, background for certain sections |
| Midnight Ink | `#1f2026` | neutral | Primary text, heading text |
| Ink Wash | `#141414` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Slate Text | `#6b6b6b` | neutral | Muted body text, icon fills, helper text |
| Silver Link | `#c4c4c4` | neutral | Disabled text, decorative borders, inactive link text |
| Input Border Gray | `#e8e8ea` | neutral | Subtle borders for input fields and dividers |
| Action Orange | `#ff5c3c` | brand | Primary call-to-action buttons, active states, brand highlights – a vivid, energetic accent |
| Slightly Yellowed | `#fffbe7` | accent | Subtle background for specific card states or emphasis |
| Amber Dot | `#f9e070` | accent | Decorative dots, specific badge backgrounds, highlights for certain content categories |
| Forest Green | `#57ad6a` | accent | Green action color for filled buttons, selected navigation states, and focused conversion moments. Use as a supporting accent, not as a status color |
| Lavender Sky Gradient | `#7d32f7` | accent | Atmospheric background for hero sections or prominent content blocks |
| Sunset Blush Gradient | `#e9aa4b` | accent | Warm, inviting background for decorative elements or contextual overlays. Primarily decorative |
| Ocean Bloom Gradient | `#4865ff` | accent | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |
| Emerald Coast Gradient | `#32cb8b` | accent | Fresh, natural background for positive feedback or growth-oriented content |
| Orchid Haze Gradient | `#f732ef` | accent | Playful, energetic background for dynamic content or showcases |
| Paper Tint Gradient | `#fddfe3` | neutral | Very subtle background for blending different sections or soft containers |

## Typography

### Labil Grotesk

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 12px, 14px, 15px, 16px, 20px, 24px |
| lineHeight | 1.00, 1.20, 1.57 |
| letterSpacing | -0.01em |
| substitute | Inter |
| role | Primary UI font for body text, navigation items, buttons, and form labels. Its compact form and slight negative letter-spacing make it feel efficient and modern. |

### Labil-Regular

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px |
| lineHeight | 1.00, 1.13, 1.20, 1.50 |
| letterSpacing | -0.01em, 0.143em |
| substitute | Inter |
| role | Used for specific body text needs, links, and subtle details. The 300 weight allows for a lighter touch than the standard Labil Grotesk, which helps differentiate visual hierarchy. |

### Labil-Bold

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px, 20px, 48px |
| lineHeight | 1.00, 1.13, 1.20 |
| letterSpacing | -0.02em, -0.01em |
| substitute | Inter |
| role | Used for emphasis in body and button text, and for smaller feature headlines. The 48px size with a tighter letter spacing creates a strong, condensed impact for key statements. |

### Attila-Bold

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 32px, 40px, 48px |
| lineHeight | 1.00, 1.10, 1.20 |
| letterSpacing | -0.02em, -0.013em |
| substitute | Archivo Black |
| role | Reserved for prominent headings and titles. Its distinct bold character ensures hierarchy and a strong brand voice. |

### Attila Sans Uniform

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 48px, 50px |
| lineHeight | 1.20 |
| letterSpacing | -0.02em |
| substitute | Archivo Black |
| role | Used for the most impactful display headings. The heavy weight and very tight letter-spacing create a dominant visual statement. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.57 | -0.12 |
| body-sm | 14 |  | 1.57 | -0.14 |
| body | 16 |  | 1.57 | -0.16 |
| subheading | 20 |  | 1.2 | -0.2 |
| heading | 24 |  | 1.2 | -0.24 |
| heading-lg | 32 |  | 1 | -0.64 |
| display | 48 |  | 1 | -0.96 |
| display-xl | 50 |  | 1.2 | -1 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| inputs | 8px |
| buttons | 8px |
| largeElements | 20px |

- **elementGap** — 8px
- **sectionGap** — 120px
- **cardPadding** — 12px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Default page background. |
| Default Card Surface | `#ffffffe6` | 1 | Main content cards, slightly translucent. |
| Subtle Elevated Surface | `#f8f6f2` | 2 | Background for secondary sections or subtly elevated container groups. |

## Components

### Primary Filled Button

**Role:** The main call-to-action button, signaling key interactions.

Background: #ff5c3c, Text: #ffffff, Border radius: 8px, Padding: 12px vertical, 20px horizontal. Emphasizes urgency and action.

### Secondary Filled Button (Dark)

**Role:** Alternative action button, often for less critical actions.

Background: #141414, Text: #ffffff, Border radius: 8px, Padding: 12px vertical, 20px horizontal. Provides a strong, contrasting alternative.

### Outline Ghost Button (Light Text)

**Role:** Subtle button for secondary actions or links where a filled button is too heavy.

Background: transparent, Text: #1f2026, Border radius: 8px, Padding: 12px vertical, 20px horizontal. Typically paired with a stronger CTA.

### Outlined Ghost Button (Yellow Accent)

**Role:** Contextual action button with a hint of accent color.

Background: transparent, Text: #141414, Border radius: 8px, Padding: 12px vertical, 20px horizontal. Border: 1px #f9e070 solid. Used for actions related to 'learning more' or specific categories.

### Default Card

**Role:** General content container for features, information blocks.

Background: rgba(255, 255, 255, 0.9), Border radius: 12px, Padding: 10px vertical, 12px horizontal. Shadow: rgba(193, 194, 222, 0.2) 1px 1px 1px 0px inset, rgba(255, 255, 255, 0.8) -1px -1px 1px 0px inset, rgb(255, 255, 255) 1px 1px 1px 0px inset, rgba(221, 223, 228, 0.5) 5px 5px 24px 0px. Features a soft, inward/outward shadow for subtle depth.

### Input Field

**Role:** Standard user input fields.

Background: transparent, Text: rgba(0, 0, 0, 0.88), Border radius: 6px, Border: 1px solid rgba(0, 0, 0, 0.88), Padding: 4px vertical, 11px horizontal. A dark, solid border gives clear definition.

### Elevated Card (Large Padding)

**Role:** Prominent content card, often for hero content or key features.

Background: rgba(255, 255, 255, 0.9), Border radius: 20px, Padding: 16px. Shadow: rgba(193, 194, 222, 0.2) 1px 1px 1px 0px inset, rgba(255, 255, 255, 0.8) -1px -1px 1px 0px inset, rgb(255, 255, 255) 1px 1px 1px 0px inset, rgba(221, 223, 228, 0.5) 5px 5px 24px 0px. Features more rounded corners and generous padding.

## Layout

The page primarily uses a max-width contained layout, likely around 1200px, centered on the screen. The hero section often features a large, centered headline paired with a call-to-action and either an abstract gradient background or a product illustration. Section rhythm is driven by consistent vertical spacing, with a calculated section gap around 120px. Content is often arranged in symmetrical stacks or 2-column layouts for text and visuals, and recurring 3-column card grids for features. Components within sections follow a comfortable density with 8px element gaps. The navigation is a typical top bar, fixed or sticky, with the brand logo, navigation links, and primary action buttons.

## Imagery

The visual language for imagery is a mix of product illustrations and abstract, gradient-infused graphics. Illustrations are flat, often depicting stylized people interacting with UI elements, using a limited color palette that aligns with brand accents. Abstract graphics feature soft gradients (like Lavender Sky and Sunset Blush) that provide depth and a dreamy, digital feel without being overly complex. Icons are simple, outlined, often in black or a muted gray to maintain the clean UI. Imagery serves a decorative and explanatory role, providing context for the AI features and creating an inviting, modern atmosphere. Content is focused, with imagery often contained within cards or as background washes, rather than full-bleed photography. Density is balanced, with imagery breaking up text-heavy sections rather than dominating the page.

## Dos & Donts

### Do

- Use Labil Grotesk (or Inter) weight 400 at 16px with line height 1.57 and -0.01em letter spacing for all body copy to maintain legibility and a compact feel.
- Apply Canvas White #ffffff as the default background for the main canvas and most card surfaces.
- Reserve Action Orange #ff5c3c for primary call-to-action buttons and critical interactive elements, ensuring high visibility.
- Utilize a soft, subtle 'neuromorphic' shadow effect from rgba(221, 223, 228, 0.5) 5px 5px 24px 0px for cards and elevated components, paired with inset highlights for depth.
- Employ consistent 8px border radius for all buttons and input fields, with cards using 12px or 20px for a softer, more approachable aesthetic.
- Maintain a clear vertical rhythm using 24px spacing below secondary elements and 12px padding within cards.
- Use Attila-Bold (or Archivo Black) for headlines, especially at 48px with -0.02em letter spacing, to create a strong, condensed visual impact.

### Don't

- Avoid using highly saturated colors outside the defined brand and accent palette; maintain a largely achromatic UI.
- Do not use sharp, square corners on interactive elements; all buttons, inputs, and cards should have a minimum of 8px border radius.
- Refrain from dense text blocks without sufficient line height; ensure body text maintains a line height of 1.57 for readability.
- Do not introduce strong, dark shadows without the complementary light inset shadows; the visual system relies on a subtle, luminous depth.
- Avoid large variations in text letter spacing other than the defined tight values; excessive spacing breaks the compact typographic style.
- Do not use generic system fonts for prominent text; stick to Labil Grotesk or its substitutes for brand consistency.
- Do not add additional decorative borders or heavy outlines to elements; surfaces gain definition through subtle shadows and internal highlights.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #1f2026
- background: #ffffff
- border: #e8e8ea
- accent: #ff5c3c
- primary action: #ff5c3c (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #ff5c3c background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a feature card: Default Card with soft shadow. Headline 'AI Sourcing agents' using Attila-Bold, weight 400, 24px, #1f2026, letter-spacing -0.24px. Body text 'Automate sourcing, fulfillment...' using Labil Grotesk, weight 400, 15px, #6b6b6b, line-height 1.57. Bottom-aligned text link 'Learn more' using Labil Grotesk, weight 400, 14px, #141414.
3. Build an input field: Input Field component. Placeholder text 'e.g. Can you' in Slate Text (#6b6b6b), Labil Grotesk, weight 400, 16px. Border #e8e8ea. Background Canvas White. Right-aligned submit icon (use a neutral icon color like #1f2026) in a circular button with Canvas White background.

---
_Source: https://styles.refero.design/style/577eb7d8-3555-4378-83df-0cebebc4782f_
