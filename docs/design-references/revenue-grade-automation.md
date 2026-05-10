# Revenue-Grade Automation — Design Reference

> Engineered Control Panel: precise, monochromatic, with a focused electric accent.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.default.com](https://www.default.com) |
| Refero page | [https://styles.refero.design/style/eeeb6ac9-fc07-4965-935a-e1989ed831f1](https://styles.refero.design/style/eeeb6ac9-fc07-4965-935a-e1989ed831f1) |
| Theme | light |
| Industry | saas |

## Overview

Default's design system evokes a meticulous, high-performance operating environment. It uses a near-monochromatic palette grounded in stark black and white, punctuated by a single electric violet accent. This controlled use of color, paired with precise typography that emphasizes both structure and clarity, creates a highly functional, engineered aesthetic. Components are lightweight, favoring ghost states and subtle fills over heavy embellishment, maintaining a sense of efficiency and focus on data flow.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#202020` | neutral | Primary text, deep surface elements, button backgrounds for secondary actions |
| Cloud Canvas | `#f5f5f5` | neutral | Page backgrounds, card surfaces, ghost button backgrounds |
| Paper White | `#ffffff` | neutral | Elevated card backgrounds, input fields, internal component surfaces |
| Muted Ash | `#333333` | neutral | Secondary text, input text, link text in neutral contexts |
| Ghost Border | `#f7f5fd` | neutral | Hairline borders, subtle dividers, input field borders |
| Electric Violet | `#5757f8` | brand | Primary action buttons, active navigation indicators, brand accents, interactive elements — creating focused activation against the neutral palette |

## Typography

### NB International Pro

| Key | Value |
| --- | --- |
| weight | 500, 700 |
| weights | 500, 700 |
| sizes | 26px, 36px, 48px, 64px, 72px |
| lineHeight | 0.97, 1.00, 1.20 |
| letterSpacing | -0.96 |
| substitute | Montserrat |
| role | Headlines and large display text — the tight letter-spacing and strong weights convey an architectural precision. The 72px size at 500 weight feels authoritative without being shouty. |

### Saans Trial

| Key | Value |
| --- | --- |
| weight | 500, 700 |
| weights | 500, 700 |
| sizes | 14px, 16px, 18px, 20px |
| lineHeight | 1.20, 1.40, 1.43 |
| letterSpacing | 0 |
| substitute | Inter |
| role | Body text, navigation, buttons, and all functional interface elements. Its generous line-height ensures readability across varied UI contexts, from compact buttons to multi-line paragraphs. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.2 |  |
| body | 16 |  | 1.4 |  |
| subheading | 18 |  | 1.43 |  |
| heading-sm | 20 |  | 1.43 |  |
| heading | 26 |  | 1.2 | -0.52 |
| heading-lg | 36 |  | 1 | -0.72 |
| display | 48 |  | 0.97 | -0.96 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| images | 12px |
| inputs | 10px |
| buttons | 1425.6px |
| default | 8px |

- **elementGap** — 24px
- **sectionGap** — 40px
- **cardPadding** — 20px
- **pageMaxWidth** — 1400px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Cloud Canvas | `#f5f5f5` | 1 | Base page background, large content sections, primary card backgrounds. |
| Paper White | `#ffffff` | 2 | Elevated and interactive elements like input fields, header navigation backgrounds, and secondary card backgrounds. |
| Midnight Ink | `#202020` | 3 | Darker, functional surfaces like secondary button backgrounds or specific UI components (though not broadly used as a prominent surface). |

## Components

### Primary Action Button

**Role:** Main call to action

Electric Violet background (#5757f8), Paper White text (#ffffff). Fully rounded corners (1425.6px) with 10px vertical and 20px horizontal padding. Font: Saans Trial, 500 weight, 16px.

### Secondary Action Button

**Role:** Secondary call to action or ghost button

Midnight Ink background (#202020), Paper White text (#ffffff). Fully rounded corners (1425.6px) with 10px vertical and 20px horizontal padding. Font: Saans Trial, 500 weight, 16px.

### Navigation Link Button

**Role:** Small, informational links

Cloud Canvas background (#f5f5f5), Midnight Ink text (#202020). Default 8px radius with 5px vertical and 10px horizontal padding. Font: Saans Trial, 500 weight, 14px.

### Navigation Outlined Button

**Role:** Header navigation with subtle styling

Paper White background (#ffffff), Midnight Ink text (#202020). Fully rounded corners (1425.6px) with 5px vertical and 10px horizontal padding. Font: Saans Trial, 500 weight, 14px.

### Default Card

**Role:** Information container, background for content blocks

Cloud Canvas background (#f5f5f5), 8px border-radius, 20px padding on all sides. No shadow.

### Accent Card

**Role:** Feature cards or testimonials with minimal styling

Transparent background, 8px border-radius. No shadow, 20px padding on all sides.

### Form Input Field

**Role:** Standard user input fields

Paper White background (#ffffff), Muted Ash text (#333333), Ghost Border (#f7f5fd) 1px border. 10px border-radius, 16px vertical and 20px horizontal padding. Placeholder text is Muted Ash.

### Filled Input Field

**Role:** Input field with a distinct background

Cloud Canvas background (#f5f5f5), Muted Ash text (#333333), Ghost Border (#f7f5fd) 1px border. 10px border-radius, 16px vertical and 20px horizontal padding. Placeholder text is Muted Ash.

## Layout

The page adheres to a max-width 1400px contained layout, centered on a Cloud Canvas (#f5f5f5) background. The hero section features a centered headline over a subtle dotted grid background, creating an engineered feel. Content flows in distinct sections with consistent vertical padding, often alternating between text-centric blocks, logo grids, and product UI showcases. The interface showcases often use a two-column layout with a prominent visual on one side. A sticky top navigation bar with a clear primary action button maintains persistent access to key features.

## Imagery

The visual language focuses on technical iconography and product UI showcases. Icons are primarily outlined or filled monochrome, often in Midnight Ink or Electric Violet. Product screenshots are presented within contained, rounded (8px) frames, focusing on clear workflow diagrams and data representations. No photography or abstract graphics are used. Imagery serves an explanatory and functional role, demonstrating the product's capabilities rather than creating atmosphere. The density of imagery is balanced, appearing as focused examples within text-dominant sections.

## Dos & Donts

### Do

- Use NB International Pro for all headlines exceeding 20px text size, with letter-spacing of -0.02em.
- Prioritize Electric Violet (#5757f8) for primary calls to action, ensuring Paper White (#ffffff) text for legibility.
- Apply a full rounding of 1425.6px to all action buttons to create a distinctly pill-shaped appearance.
- Maintain a clear visual hierarchy with Cloud Canvas (#f5f5f5) as the primary background and Paper White (#ffffff) for elevated content surfaces.
- Employ Ghost Border (#f7f5fd) for all unpressed button borders, input borders, and subtle dividers.
- Use 20px padding inside all default content cards.
- Implement a 24px gap between most adjacent UI elements for consistent comfortable density.

### Don't

- Do not introduce new vibrant colors outside of the Electric Violet accent.
- Avoid heavy shadows or gradients; the design relies on flat planes and subtle borders for depth.
- Do not use generic square corners for interactive elements; buttons and inputs require specific rounded radii.
- Do not use text weights below 500; the system maintains a confident, legible tone.
- Avoid dense or cluttered layouts; prioritize clear spacing and visual separation between content blocks.
- Do not use any stroke widths other than 1px for borders and dividers.
- Do not let non-interactive elements have the same visual prominence as primary action buttons.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #202020
background: #f5f5f5
border: #f7f5fd
accent: #5757f8
primary action: #5757f8 (filled action)

### 3-5 Example Component Prompts
1. Create a hero section: Cloud Canvas background (#f5f5f5) with a subtle grid pattern. Headline 'Run revenue like an engineered system' using NB International Pro, 72px, 500 weight, Midnight Ink (#202020), letter-spacing -1.44px. Below it, a Primary Action Button 'Book a demo' (#5757f8 background, #ffffff text, 1425.6px radius, 10px vertical / 20px horizontal padding). Below that, a Secondary Action Button 'See an interactive demo' (#202020 background, #ffffff text, 1425.6px radius, 10px vertical / 20px horizontal padding).
2. Create a content card for a feature description: Default Card with Cloud Canvas background (#f5f5f5), 8px radius, 20px padding. Inside, a heading 'Stop firefighting' using NB International Pro, 36px, 500 weight, Midnight Ink (#202020), letter-spacing -0.72px. Followed by body text using Saans Trial, 16px, 500 weight, Muted Ash (#333333).
3. Create a Primary Action Button: #5757f8 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

---
_Source: https://styles.refero.design/style/eeeb6ac9-fc07-4965-935a-e1989ed831f1_
