# Raise — Design Reference

> Architectural Blueprint Clarity

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://opencollective.com](https://opencollective.com) |
| Refero page | [https://styles.refero.design/style/63d44e1f-e3e9-40dc-bba4-1aa6efc4db87](https://styles.refero.design/style/63d44e1f-e3e9-40dc-bba4-1aa6efc4db87) |
| Theme | light |
| Industry | fintech |

## Overview

Open Collective builds upon a transparent, community-driven financial ecosystem aesthetic: a crisp white canvas paired with deep navy typography for clarity. Interface elements feel grounded yet accessible with subtle border treatments and soft shadows. The visual system prioritizes legibility and structure, using a single dominant brand blue to highlight critical actions and a carefully graded neutral palette for hierarchy. Components are clean and modern, favoring rounded shapes and clear spacing over ornate styling, while product views are kept neat and organized for easy data consumption.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button backgrounds, default icon fill |
| Navy Ink | `#0f172b` | neutral | Primary text, main headings, dark-filled buttons, dark navigation links, prominent icons |
| Ocean Blue | `#0c2764` | brand | Primary action buttons, prominent headings where the brand needs to own the message |
| Slate Text | `#1d293d` | neutral | Secondary body text, subheadings, less prominent button text |
| Light Slate | `#62748e` | neutral | Muted body text, helper text, disabled states, subtle icon fills |
| Medium Slate | `#314158` | neutral | Informational text in smaller UI elements like lists or data tables |
| Soft Gray Border | `#e2e8f0` | neutral | Hairline borders, dividers, subtle input outlines, card subtle accent strokes |
| Lightest Gray Surface | `#f1f5f9` | neutral | Subtle section backgrounds, slightly off-white surfaces for differentiation |
| Hint Gray | `#c3c6cb` | neutral | Placeholder text, very subtle informational text |
| Inactive Chip | `#90a1b9` | neutral | Background for inactive or secondary chips/badges |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700, 800 |
| weights | 400, 500, 600, 700, 800 |
| sizes | 14px, 16px, 18px, 20px, 32px, 40px, 60px, 72px |
| lineHeight | 1.00, 1.20, 1.25, 1.40, 1.43, 1.50, 1.56, 1.63, 1.71 |
| letterSpacing | -0.025, -0.025, -0.025, -0.025, -0.025, -0.025, -0.025, -0.025 |
| fontFeatureSettings | "calt", "rlig" |
| substitute | system-ui |
| role | The primary typeface for all textual content, from headings to body copy, chosen for its modern legibility and versatility across weights. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.43 | -0.025 |
| body-sm | 16 |  | 1.5 | -0.025 |
| body | 18 |  | 1.56 | -0.025 |
| subheading | 20 |  | 1.63 | -0.025 |
| heading-sm | 32 |  | 1.25 | -0.025 |
| heading | 40 |  | 1.2 | -0.025 |
| heading-lg | 60 |  | 1 | -0.025 |
| display | 72 |  | 1 | -0.025 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| buttons | 1.67772e+07px |
| general | 12px |

- **elementGap** — 16px
- **sectionGap** — 32px
- **cardPadding** — 32px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Dominant page background, primary canvas for content. |
| Lightest Gray Surface | `#f1f5f9` | 1 | Subtle background for distinct sections or very light card variations. |
| Inactive Chip | `#90a1b9` | 2 | Background for secondary or inactive visual elements, suggesting a lower hierarchy. |
| Ocean Blue | `#0c2764` | 3 | Elevated, prominent surfaces like primary action buttons. |

## Components

### Primary Filled Button

**Role:** Main calls to action

Filled with Ocean Blue (#0c2764) background, Canvas White (#ffffff) text, and ultra-rounded (1.67772e+07px) corners. Padding is 0px vertical and 12px horizontal.

### Ghost Header Button

**Role:** Secondary navigation actions in the header

Ghost style with Canvas White (#ffffff) background, Slate Text (#1d293d) for text, no explicit border, and sharp 0px corners. Padding is 10px.

### Ghost Card Button

**Role:** Interactive elements within cards, like approval options

Canvas White (#ffffff) background, Navy Ink (#0f172b) text, with an ultra-rounded (1.67772e+07px) radius. Padding is 0px vertical and 12px horizontal. Border is 1px solid Soft Gray Border (#e2e8f0).

### Hero Action Button

**Role:** Prominent calls to action in hero sections

Canvas White (#ffffff) background, Navy Ink (#0f172b) text, 12px border radius. Generous padding of 24px on all sides.

### Standard Card

**Role:** Content containers, feature blocks, data displays

Canvas White (#ffffff) background with a 12px border radius. Uses a subtle shadow: rgba(0, 0, 0, 0.1) 0px 10px 15px -3px, rgba(0, 0, 0, 0.1) 0px 4px 6px -4px. Internal padding is 32px on all sides.

## Layout

The page uses a maximum width contained layout, approximately 1200px wide, with content horizontally centered. The hero section is full-bleed but features a centered, prominent headline and descriptive text. Vertical rhythm is established through consistent section gaps, with content arranged in clear, often two-column text-and-visual patterns or stacked centered blocks. Cards are typically presented in grids for features. Navigation is a sticky top bar with clearly defined interactive elements.

## Imagery

This design system primarily uses minimal, stylized two-dimensional illustrations that often have a hand-drawn or watercolor aesthetic, as seen with the magnifying glass character. Photography and 3D renders appear to be absent. Icons are typically outlined or filled in a mono-color style, serving a functional purpose rather than decorative. Imagery is contained, rather than full-bleed, and often serves as explanatory content accompanying text blocks, maintaining a balanced density with text-dominant layouts.

## Dos & Donts

### Do

- Prioritize Canvas White (#ffffff) for all main backgrounds to maintain a clean, open feel.
- Use Navy Ink (#0f172b) for all primary body text and most prominent headings to ensure high contrast and readability.
- Apply Soft Gray Border (#e2e8f0) for all hairline borders and subtle dividers to create subtle visual separation without heavy lines.
- Adhere to an element spacing of 16px between smaller, related components to maintain a comfortable density.
- Use the 12px border radius for all cards and general grouped elements, reserving the ultra-rounded 1.67772e+07px for buttons only.
- Ensure all primary action buttons are Ocean Blue (#0c2764) with Canvas White (#ffffff) text to clearly distinguish calls to action.
- Employ the Inter font family for all typography, leveraging its various weights to establish hierarchy without introducing new typefaces.

### Don't

- Avoid using multiple accent colors; Ocean Blue (#0c2764) should be the singular vibrant brand color.
- Do not introduce heavy, opaque shadows; elements should feel subtly elevated with the existing shadow token if needed.
- Steer clear of gradients on buttons or primary UI elements; surfaces should remain flat and monochrome.
- Do not use highly saturated chromatic colors for information or status beyond defined semantic components.
- Avoid tight, compact layouts; maintain comfortable spacing, particularly 16px for element gaps and 32px for card padding.
- Do not use serif fonts or highly decorative typefaces; Inter's clean, sans-serif lines are fundamental to the system's clarity.
- Refrain from using bright, energetic colors for text; stick to the defined Navy Ink, Slate Text, and Light Slate for all textual content.

## Notes

### Agent Prompt Guide

primary action: #0c2764 (filled action)
Create a Primary Action Button: #0c2764 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Example Component Prompts:
2. Design a Standard Card: Canvas White (#ffffff) background, 12px border radius, using the defined shadow rgba(0, 0, 0, 0.1) 0px 10px 15px -3px, rgba(0, 0, 0, 0.1) 0px 4px 6px -4px. Internal padding of 32px. Use Navy Ink (#0f172b) for titles and Light Slate (#62748e) for descriptive text within.
3. Build a Navigation Bar: Canvas White (#ffffff) background, Navy Ink (#0f172b) for menu text. Include a Ghost Header Button 'Log In' with Canvas White (#ffffff) background, Slate Text (#1d293d) text, 0px radius, 10px padding.
4. Create an 'Approve' action within a card: Use a Ghost Card Button with Canvas White (#ffffff) background, Navy Ink (#0f172b) text, ultra-rounded (1.67772e+07px) radius, and 1px solid Soft Gray Border (#e2e8f0).

---
_Source: https://styles.refero.design/style/63d44e1f-e3e9-40dc-bba4-1aa6efc4db87_
