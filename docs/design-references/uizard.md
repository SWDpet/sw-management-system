# Uizard — Design Reference

> Midnight AI Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://uizard.io](https://uizard.io) |
| Refero page | [https://styles.refero.design/style/8065e38a-cdb7-4645-b168-98e6f80e7118](https://styles.refero.design/style/8065e38a-cdb7-4645-b168-98e6f80e7118) |
| Theme | dark |
| Industry | ai |

## Overview

Uizard employs a dark, futuristic aesthetic with a strong emphasis on vivid purple accents. Interfaces are generally monochrome with ample negative space. Typography is confident and slightly condensed, guiding the user through AI-powered flows. Buttons and interactive elements are clearly defined by distinct radii and color choices, creating a functional, tech-forward experience. Surface depth is subtle, achieved more through slight tinting and border treatments than heavy shadows.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#0b0b0b` | neutral | Primary page and card backgrounds transitioning into deeper shadows |
| Deep Space | `#000000` | neutral | Base canvas for darker sections, card backgrounds, and subtle shadow tints |
| Lunar Surface | `#f5f5f5` | neutral | Primary body text, neutral borders, and iconography |
| Asteroid Dust | `#aeaeae` | neutral | Muted secondary text and less prominent borders |
| Cosmic Gray | `#212121` | neutral | Divider lines, subtle card borders, and ghost button outlines |
| Eclipse Gray | `#2e2e2e` | neutral | Focused card borders and dividers |
| Starlight | `#ffffff` | neutral | Headlines, critical text, input fields, and active state accents |
| AI Violet | `#a881fe` | brand | Primary calls to action, interactive elements, highlights, and subtle background accent graphics |
| AI Violet Gradient | `#6419ff` | brand | Background gradients for hero sections and brand-focused elements, providing depth and energy |
| Call to Action Blue | `#1e90ff` | accent | Secondary calls to action, distinct from the primary AI Violet, for specific conversion paths |

## Typography

### Satoshi-Variable

| Key | Value |
| --- | --- |
| weight | 400, 480, 540, 560, 640 |
| weights | 400, 480, 540, 560, 640 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px, 40px |
| lineHeight | 1.00, 1.15, 1.20, 1.25, 1.29, 1.33, 1.38, 1.40, 1.43 |
| letterSpacing | -1px at 40px, -0.45px at 24px, -0.29px at 18px, -0.24px at 16px, -0.19px at 14px, -0.16px at 12px, 1px for 0.083em |
| substitute | Inter |
| role | Primary functional typeface for most UI elements, body text, and subheadings. Its variable nature allows for a range of expressiveness from compact readability to confident emphasis. The slightly condensed forms maintain a tech-forward feel. |

### ClashGrotesk-Variable

| Key | Value |
| --- | --- |
| weight | 540 |
| weights | 540 |
| sizes | 72px |
| lineHeight | 1.00 |
| letterSpacing | -0.58px at 72px |
| substitute | Space Mono |
| role | Display font for large, impactful headlines, conveying a modern and bold brand voice. Its distinct character shapes set it apart for hero content. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.43 | -0.16 |
| body-sm | 14 |  | 1.4 | -0.19 |
| body | 16 |  | 1.38 | -0.24 |
| subheading | 18 |  | 1.33 | -0.29 |
| heading-sm | 20 |  | 1.29 | -0.29 |
| heading | 24 |  | 1.25 | -0.45 |
| heading-lg | 40 |  | 1.15 | -1 |
| display | 72 |  | 1 | -0.58 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 4px |
| cards | 16px |
| inputs | 10px |
| buttons | 12px |

- **elementGap** — 12px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Space | `#000000` | 0 | Base page background for large sections and deep elevation. |
| Midnight Ink | `#0b0b0b` | 1 | Primary page background, slightly lighter than Deep Space, and for card surfaces. |
| Transparent Card | `#21212152` | 2 | Subtly elevated card surfaces with a translucent effect, allowing the background to show through. |
| Starlight | `#ffffff` | 3 | Highest contrast surface for interactive elements like input fields, standing out against dark backgrounds. |

## Components

### Primary Action Button

**Role:** Filled button for primary calls to action, leveraging the brand violet.

Background: AI Violet (#a881fe), Text: Starlight (#ffffff), Border Radius: 12px, Padding: 0px vertical, 24px horizontal. Uses Satoshi-Variable font.

### Secondary Action Button

**Role:** Filled button for secondary actions, using a vivid blue.

Background: Call to Action Blue (#1e90ff), Text: Starlight (#ffffff), Border Radius: 8px, Padding: 0px vertical, 16px horizontal. Uses Satoshi-Variable font.

### Ghost Navigation Button

**Role:** Text-only button for navigation, blending into the dark background.

Background: transparent, Text: Starlight (#ffffff), Border Radius: 0px, Padding: 0px. Uses Satoshi-Variable font.

### Testimonial Card

**Role:** Transparent card for displaying customer testimonials.

Background: rgba(33, 33, 33, 0.32), Border Radius: 16px, Padding: 24px. No shadow. Uses Satoshi-Variable font for content.

### Implicit Card

**Role:** Card defined by padding and rounded corners on the base background, without explicit background color.

Background: transparent, Border Radius: 16px, Padding: 40px 0px 40px 40px. No shadow. Used for complex content blocks that subtly emerge from the main background.

### Primary Input Field

**Role:** Input field for user text entry.

Background: Starlight (#ffffff), Text: Midnight Ink (#111111), Border Radius: 10px, Padding: 12px. Placeholder text is Muted Secondary Text.

## Layout

The page primarily uses a max-width, center-aligned layout, creating a contained and focused experience. The hero section is full-bleed, featuring a large, centered headline over a dramatic AI Violet Gradient background. Sections below alternate between dark tones, maintaining consistent vertical spacing at 40px for major section breaks. Content within sections is often structured in two columns, alternating media and text. A prominent feature is a card grid for testimonials, presenting information in a structured, digestible format. Navigation is a consistent top bar that remains visible, suggesting a sticky header, with prominent 'Sign up for free' button as the primary action.

## Imagery

This site uses a blend of illustrative and abstract graphics, alongside implied product screenshots. The imagery is primarily dark-themed, echoing the UI. Product visuals are presented as sleek, contained representations within device mockups, often with a subtle glowing aura of the AI Violet. Icons are typically filled with 'Starlight' or 'Lunar Surface' against dark backgrounds, maintaining a clean yet visible presence. Abstract gradients, particularly using the AI Violet Gradient, serve as decorative backdrops for hero sections and key messaging. Imagery density is moderate, used to emphasize core product features or add atmospheric depth rather than for exhaustive product tours or heavy content.

## Dos & Donts

### Do

- Prioritize AI Violet (#a881fe) for all primary calls to action, ensuring prominence and brand consistency.
- Use Satoshi-Variable for all body text and interface elements, varying weight according to hierarchy but favoring 400-540 for readability.
- Apply 12px border radius to all primary buttons and 10px to input fields for a consistent interactive element feel.
- Maintain a clear visual hierarchy by contrasting Starlight (#ffffff) for headlines and primary text against Midnight Ink (#0b0b0b) backgrounds.
- Use Cosmic Gray (#212121) or Meteor Gray (#2e2e2e) for subtle borders and dividers, avoiding heavy strokes.
- Employ the AI Violet Gradient for hero sections and illustrative backgrounds to add depth and brand energy.
- Utilize ClashGrotesk-Variable for display headlines (72px, weight 540) to convey a bold, modern brand voice.

### Don't

- Avoid using bright, saturated colors other than AI Violet (#a881fe) or Call to Action Blue (#1e90ff) for functional UI elements.
- Don't apply heavy, opaque shadows; opt for subtle, tinted shadows like rgba(3, 3, 3, 0.12) or the brand-specific AI Violet inset shadow.
- Do not deviate from the established type scale; maintain consistent line heights and letter spacing found in Satoshi-Variable and ClashGrotesk.
- Avoid excessive use of different border radii; stick to 12px for primary buttons, 10px for inputs, and 16px for cards.
- Don't introduce additional achromatic colors that are visually close to Lunar Surface (#f5f5f5) or Asteroid Dust (#aeaeae), to prevent visual clutter.
- Never use full-width, uncontained layouts for main content; always adhere to the implied max-width and central alignment for text blocks.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #f5f5f5
background: #0b0b0b
border: #212121
accent: #1e90ff
primary action: #1e90ff (filled action)

Example Component Prompts:
1. Create a hero section: AI Violet Gradient (linear-gradient(0deg, rgb(100, 25, 255), rgb(168, 129, 254))) background. Headline 'Turn product ideas into concepts' ClashGrotesk-Variable weight 540, 72px, #ffffff, letter-spacing -0.58px. Subtext 'Visualize, communicate, and iterate' Satoshi-Variable weight 400, 18px, #f5f5f5, letter-spacing -0.29px. Input field: background #ffffff, text #111111, padding 12px, radius 10px; beside it, a button: AI Violet (#a881fe), text #ffffff, padding 0px 24px, radius 12px.
2. Create a testimonial card: Background rgba(33, 33, 33, 0.32), radius 16px, padding 24px. Text body-sm Satoshi-Variable weight 400, #f5f5f5, letter-spacing -0.19px. Author name caption Satoshi-Variable weight 400, #aeaeae, letter-spacing -0.16px.
3. Create a Primary Action Button: #1e90ff background, #212121 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

---
_Source: https://styles.refero.design/style/8065e38a-cdb7-4645-b168-98e6f80e7118_
