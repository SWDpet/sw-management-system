# Pitch — Design Reference

> Vibrant Violet Gradient Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://pitch.com](https://pitch.com) |
| Refero page | [https://styles.refero.design/style/da332394-784c-4df2-9e66-c3f7b1d28f28](https://styles.refero.design/style/da332394-784c-4df2-9e66-c3f7b1d28f28) |
| Theme | light |
| Industry | productivity |

## Overview

Pitch embodies an 'energetic professional' mood, achieved through a vibrant violet spectrum contrasted with clean whites and subtle grays. The hero section bursts with rich gradients, while content sections maintain clarity with ample negative space. A blend of sharp geometric forms and soft, rounded elements creates a dynamic yet approachable feel, balancing technical precision with modern friendliness. Custom typography provides distinct voices for headlines and body text, contributing to an overall sense of refined dynamism.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Violet | `#8d49f7` | brand | Primary brand color, used for active links, icons, and significant accents. It provides visual energy and indicates interactivity. |
| Deep Violet | `#6b53ff` | brand | Secondary brand color, appearing in badges, card elements, and as a strong accent. It provides depth to the violet palette. |
| Midnight Graphite | `#1e1d28` | neutral | Darkest text and card backgrounds, providing high contrast against light surfaces. Used where information density or visual weight is desired. |
| Action Yellow | `#ffd02c` | accent | Accentuating specific card backgrounds or illustrative elements that require high visibility, often paired with gradients. |
| Faded Shadow | `#b5b3cd` | neutral | Used for subtle card shadows, providing a soft lift without harshness. |
| Medium Gray | `#3f4250` | neutral | Mid-tone text and borders, offering a slightly softer alternative to Midnight Graphite for less prominent text. |
| Button Gray | `#6f7387` | neutral | Default text color for secondary buttons, balancing visibility with a muted appearance. |
| Bright Violet | `#586ee0` | accent | Used for illustrative fills and minor accent details within graphics, adding vibrancy. |
| Dark Violet Text | `#371789` | brand | Specific text instances on lighter backgrounds, providing a contrasting, rich violet tone. |
| Sunset Orange | `#ffa000` | accent | Used in fills and as a background accent in some graphic elements, pairing with Action Yellow to create a warm highlight. |
| Sky Blue | `#81d4fa` | accent | Specific graphic fills and background accents, contributing to a broader vibrant palette. |
| Rich Dark | `#2b2a35` | neutral | Dominant text color for headings and body copy across most light backgrounds, providing strong legibility. |
| Pure White | `#ffffff` | neutral | Canvas background, text on dark elements, and primary button backgrounds. |
| Canvas Faint | `#f0eff4` | neutral | Subtle background for main page canvas and sections, providing a near-white base without being stark. |
| Border Light Gray | `#dddfe5` | neutral | Default border color for inputs and subtle dividers, creating clear separation without being heavy. |
| Mid-tone Border | `#cfcfd0` | neutral | Slightly darker border choice for elements requiring more definition, like some button states. |
| Coal Black | `#000000` | neutral | Used for some link text and specific icons, offering an alternative to Rich Dark where maximum contrast is required. |
| Faint Shadow Gray | `#95959a` | neutral | Used for subtle button shadows, providing depth without visual weight. |
| Hero Gradient Purple | `#5318eb` | brand | Primary gradient for hero sections and prominent background elements, creating a dynamic and immersive brand experience. Dominant color is #5318eb from `rgb(83, 24, 235)`. |
| Light Gradient Overlay | `#e4cfff` | brand | Subtle background gradient for internal sections, adding a soft, branded layer. Dominant color is #e4cfff from `rgb(228, 207, 255)`. |
| Dark Gradient Accent | `#2b2a35` | brand | Used for internal graphic accents and backgrounds, providing a dark-to-violet transition. Dominant color is #2b2a35 from `rgb(43, 42, 53)`. |
| Warm Gradient Highlight | `#ffd02c` | accent | Used for specific illustrative elements, creating a warm and inviting highlight effect. Dominant color is #ffd02c from `rgb(255, 208, 44)`. |

## Typography

### Eina01

| Key | Value |
| --- | --- |
| weight | 400, 600, 700, 800 |
| sizes | 13px, 14px, 15px, 16px, 18px, 22px, 24px, 28px |
| lineHeight | 1.00, 1.10, 1.20, 1.30, 1.40, 1.60, 1.69, 2.00, 2.25 |
| letterSpacing | 0.1000em |
| substitute | Inter |
| role | Primary UI and body text font. Its regular weights (400, 600) ensure readability for extensive copy, while its extended size range supports diverse UI elements from caption to sub-heading. Used for links, icons, buttons, and general informational text. |

### Mark Pro

| Key | Value |
| --- | --- |
| weight | 700, 800 |
| sizes | 13px, 27px, 28px, 42px, 60px, 80px |
| lineHeight | 1.00, 1.10, 1.30, 1.40, 1.85 |
| letterSpacing | -0.0200em |
| substitute | Montserrat |
| role | Distinctive display font for headings and prominent card titles. Its heavier weights (700, 800) and tighter letter-spacing create a bold, modern statement without being aggressive, contributing significantly to brand identity. Used across major headings and highlighted content. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | sans-serif |
| role | System fallback for specific button text, ensuring accessibility if custom fonts are unavailable. Used sparingly. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.2 | 0.1 |
| body | 16 |  | 1.6 | 0.1 |
| subheading | 22 |  | 1.4 | 0.1 |
| heading-sm | 27 |  | 1.3 | -0.02 |
| heading | 42 |  | 1.1 | -0.02 |
| display | 80 |  | 1 | -0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 26px |
| large | 56px |
| small | 3px |
| badges | 20px |
| buttons | 20px |
| default | 6px |

- **elementGap** — 20px
- **sectionGap** — 40px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas Faint | `#f0eff4` | 0 | Base page background, light and airy foundation. |
| Pure White | `#ffffff` | 1 | Primary content surfaces like cards, modal backgrounds, providing high contrast. |
| Midnight Graphite | `#1e1d28` | 2 | Elevated card backgrounds, dark sections, creating prominent content blocks. |
| Action Yellow | `#ffd02c` | 3 | Highlighting specific cards or illustrative elements, indicating special content. |

## Components

### Primary Button

**Role:** Action

Filled button with Pure White background and Deep Violet text. Uses 20px border-radius, with 20px padding. Text is Eina01 400.

### Ghost Button

**Role:** Action

Transparent button with Button Gray text and border, no border-radius defined (inherits 0px). Uses 16px vertical and 20px horizontal padding. Text is Eina01 400.

### Inline Text Button

**Role:** Navigation/Action

Small, transparent button for inline interactions. Text is Pure White at 60% opacity with a matching border. No padding or border-radius.

### Navigation Button

**Role:** Navigation

White background button with Deep Violet text for primary navigation calls to action. Uses 20px border-radius, with 20px padding. Features a subtle Light Gray border (#dddfe5).

### Feature Card

**Role:** Display

Standard content card with Pure White background, a subtle shadow of rgba(103, 110, 144, 0.2) 0px 8px 26px 0px, and 26px border-radius. Internal padding is 20px. Content elements within are spaced by 20px.

### Badge Default

**Role:** Information

Transparent background badge with Midnight Graphite text, no border-radius. Displays concise information. Padding is 0px.

### Violet Tag Badge

**Role:** Information/Categorization

Light Deep Violet background (rgba(107, 83, 255, 0.05)) with Deep Violet text. Has a 20px border-radius and 4px vertical, 12px horizontal padding. Text is Eina01 400.

### Hero Headline

**Role:** Display

Large, eye-catching text using Mark Pro 800 with Riverbank Violet color, sizes from 42px to 80px and letter-spacing -0.0200em.

### Input Field

**Role:** Form Element

Implicitly Pure White background with the default Border Light Gray (#dddfe5) border ensuring clear definition against the canvas.

### Rounded Icon Button

**Role:** Action

Small, Pure White circular button with 50% border-radius for icons. Minimal 1px padding for internal spacing.

## Layout

The page uses a maximum content width centered model, subtly implied by generous side padding on most sections. The hero section is a full-bleed, vibrant gradient background with centered, prominent headline and call-to-action. Content sections alternate between the Canvas Faint background and occasional gradient overlays. There's a consistent vertical rhythm created by explicit section gaps. Content is arranged using a mix of centered stacks for headlines and subtext, and alternating text-left/image-right or text-right/image-left for feature sections. A clear grid structure appears for displaying cards and badges, often in 3-column layouts where applicable. The layout prioritizes spaciousness, using ample negative space around content blocks. Navigation is a sticky top bar with a left-aligned logo and right-aligned actions (login, sign up).

## Imagery

The visual language primarily uses abstract 3D renders with soft, organic shapes and occasional product screenshots. The 3D elements, often in shades of violet and gray, are used decoratively to add a sense of depth and modernity, frequently appearing as background accents or overlapping UI elements. Product screenshots are contained within UI frameworks, highlighting features, and are treated with sharp edges, contrasting the softer 3D elements. Icons are filled, predominantly in Pitch Violet or white, with a consistent, moderate stroke weight, serving an explanatory and navigational role. The overall density is balanced, allowing imagery to complement text without overwhelming it.

## Dos & Donts

### Do

- Prioritize Deep Violet (#6b53ff) for all primary interactive elements and brand accents.
- Use Eina01 for all body text, UI labels, and secondary information, maintaining consistency at weights 400-600.
- Apply Mark Pro 700 or 800 for all headlines and subheadings to maintain the brand's bold typographic voice.
- Utilize 26px border-radius for all primary display cards, and 20px for buttons and badges to enforce soft geometry.
- Implement Canvas Faint (#f0eff4) as the default page background to provide a consistent light theme base.
- Space elements with a default 20px elementGap, and content cards with 20px internal padding for comfortable density.
- Apply the Hero Gradient Purple to full-width hero sections to create visual impact and reinforce brand identity.

### Don't

- Avoid using raw black (#000000) for body text; use Rich Dark (#2b2a35) or Midnight Graphite (#1e1d28) for softer contrast.
- Do not introduce new border-radius values beyond 3px, 6px, 10px, 16px, 20px, 26px, 56px to preserve shape consistency.
- Avoid using highly saturated colors for large text blocks; reserve them for accents and interactive elements.
- Do not deviate from Eina01 and Mark Pro font families; avoid using system default fonts unless for accessibility fallbacks.
- Refrain from adding arbitrary shadows; restrict shadow usage to the specified values and contexts for deliberate depth.
- Avoid tight spacing; maintain comfort and legibility by adhering to 20px element gaps and section separations.
- Do not use multiple accent colors simultaneously in proximity; allow Pitch Violet and specific accents (like Action Yellow) to stand out.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Text Primary:** #2b2a35 (Rich Dark)
- **Background:** #f0eff4 (Canvas Faint)
- **CTA Background:** #6b53ff (Deep Violet)
- **CTA Text:** #ffffff (Pure White)
- **Border Default:** #dddfe5 (Border Light Gray)
- **Accent:** #8d49f7 (Pitch Violet)

### Example Component Prompts
1. **Create a Hero Section:** Use `Hero Gradient Purple` as the background. Centered headline 'Don’t just present. Pitch.' using `Mark Pro` 800 at 80px, color `Pure White`. Below it, a subtext at `Eina01` 18px 400, color `Pure White`. Include two buttons: a 'Sign up for free' `Primary Button`, and a 'Get a demo' `Ghost Button` with `Pure White` text and `Pure White` border.
2. **Generate a Feature Card:** Use `Pure White` background with `26px` border-radius and the `Feature Card` shadow. Apply `20px` internal padding. Inside, place a headline 'Powerful Integrations' using `Mark Pro` 700 at 27px, color `Midnight Graphite`, and a paragraph of body text using `Eina01` 16px 400, color `Midnight Graphite`.
3. **Design a Navigation Bar Button:** Create a `Navigation Button` with 'Sign up' as its label. This button has a `Pure White` background with `Deep Violet` text, `20px` border-radius. Position it within a sticky header on `Canvas Faint` background.
4. **Create a Tag Badge:** Use the `Violet Tag Badge` for a categorical label 'New Feature'. It should have a background of `rgba(107, 83, 255, 0.05)`, `Deep Violet` text (`#6b53ff`), `20px` border-radius, and `4px` vertical, `12px` horizontal padding.

---
_Source: https://styles.refero.design/style/da332394-784c-4df2-9e66-c3f7b1d28f28_
