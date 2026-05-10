# Auros — Design Reference

> Deep-sea radiant data

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://auros.global](https://auros.global) |
| Refero page | [https://styles.refero.design/style/21cfe0c1-778d-4613-9f47-a5718eb929b3](https://styles.refero.design/style/21cfe0c1-778d-4613-9f47-a5718eb929b3) |
| Theme | dark |
| Industry | fintech |

## Overview

Auros employs a deep-sea command center aesthetic: dark, absorbing tones of teal and blue-black serve as the canvas, punctuated by a radiant, almost neon gradient of pink, purple, and green. Typography is modern and precise, with a strong emphasis on spaciousness and clarity against the dark backdrop. Components are generally dark, with subtle variations in background teal for surface hierarchy, and liberal use of large radius values for a soft-edged feel. Accents are reserved for interactive elements and highlights, creating a focused, high-tech impression.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Teal | `#012624` | neutral | Primary page background, text on light contrasting elements, subtle borders for ghost buttons and links. It creates a deep, immersive environment |
| Deep Ocean | `#011d1c` | neutral | Elevated card backgrounds, providing a slightly darker surface than the main background without significant contrast |
| Accent Teal | `#003734` | accent | Background for secondary interactive elements and prominent cards, adding a subtle touch of color depth within the dark theme |
| Soft Silver | `#bbc7c6` | neutral | Muted body text, helper text, and inactive icon strokes, providing secondary information without competing with headings |
| Frost White | `#F2F2F2` | neutral | Subtle text for quotes or less emphasized content, almost white but slightly dulled for dark contrast. Also serves as secondary border color for certain elements |
| Near White | `#EDFFFE` | neutral | Highly contrasting text and icon elements on dark backgrounds, offering maximum readability for main content |
| Slate Gray | `#333333` | neutral | Decorative borders for ghost buttons and dividers where a darker accent is preferred, and occasionally for text on very light elements |
| Off Black | `#222222` | neutral | Border color for ghost buttons, providing a subtle outline against the dark background |
| Pale Pink Glow | `#FDE9FF` | accent | Text for emphasized statistics or small, highlighted numerical data, providing a soft, almost ethereal accent |
| Soft Gray | `#707777` | neutral | Background for very subtle, less prominent UI elements, providing minimal visual weight. Used for small background areas |
| Vivid Aqua to Gold Gradient | `#00827c` | brand | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |
| Ethereal Spectrum | `#cbfffc` | brand | Decorative background gradient creating a soft, shifting light effect, signifying dynamic motion or technological flow |
| Dynamic Flow Gradient | `#fadde6` | brand | Complex background element, giving a sense of depth and energetic movement through color transitions; Used for large, captivating hero backgrounds, emitting a soft, centered glow |
| Tech Surge Gradient | `#CBE0F9` | brand | Highlight elements, often hinting at digital infrastructure or data streams |
| Soft Transition Gradient | `#edfefe` | brand | Subtle background variations or overlays, providing a gentle break from solid dark tones |

## Typography

### Matter

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 10px, 12px, 13px, 14px, 16px, 20px, 24px, 36px, 61px, 86px, 96px, 295px |
| lineHeight | 1.00, 1.30, 1.40, 1.50 |
| letterSpacing | -0.046em at 295px, -0.040em at 96px, -0.020em at 86px, -0.013em at 61px, -0.012em at 36px, normal at 24px, 0.055em at 14px, 0.080em at 12px, 0.120em at 10px, 0.150em at 20px, 0.240em at 13px |
| substitute | Inter |
| role | Primary brand typeface. Its modern, sans-serif clarity provides a technological, forward-thinking feel. Used for all headings, body text, and UI elements. The extremely tight letter spacing at large sizes creates a sense of precise, deliberate communication, while wider spacing for smaller text ensures readability. Varied line heights and weights allow for detailed hierarchy. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.43 |
| substitute | Helvetica Neue |
| role | Fallback font for general interface elements, ensuring broad compatibility while maintaining readability across various operating systems when the Matter font is unavailable. Primarily seen in navigation and small UI text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0.12 |
| body | 14 |  | 1.4 | 0.055 |
| body-lg | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.5 | 0.15 |
| heading-sm | 24 |  | 1.3 |  |
| heading | 36 |  | 1.3 | -0.012 |
| heading-lg | 61 |  | 1.3 | -0.013 |
| display | 86 |  | 1 | -0.02 |
| display-lg | 96 |  | 1 | -0.04 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| forms | 6px |
| links | 6px |
| badges | 6px |
| images | 6px |
| buttons | 6px |

- **elementGap** — 20px
- **sectionGap** — 68px
- **cardPadding** — 40px
- **pageMaxWidth** — 1440px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Teal Surface | `#012624` | 0 | Base page background and foundational canvas. |
| Deep Ocean Surface | `#011d1c` | 1 | Slightly elevated cards, providing minimal textural difference from the base. |
| Accent Teal Surface | `#003734` | 2 | Prominent interactive cards and background for certain components, adding a subtle touch of color and depth. |
| Soft Gray Surface | `#707777` | 3 | Very subtle background for small, less emphasized UI elements or dividers. |

## Components

### Ghost Button - Light Text

**Role:** Secondary action button for low-priority interactions or link-like behavior.

Transparent background, 'Matter' font at 14px Weight 400, #222222 text color (for very light backgrounds, for dark backgrounds it would be a light color), 6px border radius, 0px padding. Border style would be 1px solid #222222 if on a light background, or #EDFFFE on a dark background.

### Primary Action Button - Gradient Fill

**Role:** Prominent calls to action, drawing attention with its distinctive gradient.

Background uses 'Vivid Aqua to Gold Gradient' (linear-gradient(90deg, rgb(0, 130, 124) 0%, rgb(203, 255, 252) 100%)), text is usually a dark neutral (e.g., #222222 as seen in the prompt), 'Matter' font, 6px border radius, with generous padding (32px vertical, 22px horizontal).

### Action Card - Accent Teal Background

**Role:** Interactive cards highlighting specific services or key information.

Background of 'Accent Teal' (#003734), with generous padding (36px all sides) and a 16px border radius. This card uses a slightly elevated background color to differentiate from the base canvas.

### Ghost Card - Minimal

**Role:** Informational cards that blend with the background, using internal content for visual hierarchy.

Transparent background, 16px border radius, and generous padding (48px vertical, 36px horizontal).

### Navigation Link

**Role:** Interactive navigation items in the header or footer.

Text in 'Arial' 14px, Weight 400, on a transparent background, typically white text on dark backgrounds. 6px border radius on hover/active states, with 0px padding.

## Layout

The page employs a max-width 1440px centered layout for most content, maintaining readability and structure. The hero section, however, is full-bleed, featuring a dark background with a pronounced celestial radial gradient and large, centered typography. The section rhythm is consistent, separated by substantial vertical spacing of 68px between blocks. Content is arranged in alternating patterns, often featuring text on the left and abstract visuals or cards on the right, or vertically stacked centered content for key messages. There's a subtle 3-column card grid for features. The overall density is spacious, emphasizing individual content blocks. Navigation is a sticky top bar with clearly defined links and a prominent gradient-filled 'Partner With Us' button.

## Imagery

This design system uses a minimal approach to imagery, focusing primarily on abstract digital graphics and subtle animations for atmosphere rather than specific photography or product illustrations. Where present, it features scattered particle effects and molecular-like structures (e.g., the large sphere of dots and the connected 'blob' shapes), rendered in colors that subtly shift between shades of the brand's teal and soft pink. These are primarily decorative, contributing to the high-tech, liquid data feel. Icons are typically outlined and monochromatic, matching the overall sleek and understated UI. The density of imagery is low, allowing typography and spacious layouts to dominate.

## Elevation philosophy

The design system explicitly avoids traditional shadow-based elevation. Instead, visual hierarchy and depth are created through variations in background color (using 'Midnight Teal', 'Deep Ocean', and 'Accent Teal' for successive surface levels) and soft background gradients. Components acquire visual weight or distinction through these subtle color shifts and generous padding, rather than cast shadows, contributing to a modern, almost flat aesthetic with rich background textures.

## Dos & Donts

### Do

- Use 'Midnight Teal' (#012624) as the default background for all main page content areas to maintain the consistent dark theme.
- Apply 'Matter' font family for all text elements, prioritizing appropriate weight and letter spacing for readability at specific sizes.
- Utilize 16px border radius for all cards and visually distinct information blocks to ensure a consistent soft-edged aesthetic.
- For primary call-to-action buttons, apply the 'Vivid Aqua to Gold Gradient' (linear-gradient(90deg, rgb(0, 130, 124) 0%, rgb(203, 255, 252) 100%)) as a background fill.
- Ensure headings use 'Near White' (#EDFFFE) for maximum contrast and prominence on dark backgrounds.
- Maintain a clear visual hierarchy by differentiating interactive cards with 'Accent Teal' (#003734) backgrounds from more subtle, transparent cards.
- Implement a generous vertical spacing of 68px between main sections to provide visual breathing room and clarity.

### Don't

- Do not use highly saturated or light colors for large background areas; maintain the integrity of the dark theme.
- Avoid using low contrast text on dark backgrounds; ensure main text uses 'Near White' (#EDFFFE) or 'Soft Silver' (#bbc7c6) as appropriate.
- Do not use generic square corners for interactive elements or cards; always apply the defined border radii.
- Refrain from introducing additional font families; 'Matter' and 'Arial' are the only approved typefaces.
- Avoid excessive use of strong shadows; the design relies on color and background variations for surface differentiation rather than elevation effects.
- Do not break the established spacing rhythm; consistently apply 68px for section gaps and 20px for element gaps.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #EDFFFE
background: #012624
border: #333333
accent: #003734
primary action: #003734 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #003734 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Create a feature card: 'Accent Teal' (#003734) background, 16px border radius, 36px padding on all sides. Heading 'Proprietary Trading' at 24px 'Matter' Weight 500, #EDFFFE. Body text 'Rooted in algorithmic precision and powered by advanced technology...' at 14px 'Matter' Weight 400, 'Soft Silver' (#BBC7C6).
3. Create a navigation button: Text 'Proprietary Trading' at 14px 'Arial' Weight 400, #EDFFFE, 6px border radius, 0px padding. On hover, apply a subtle background change to a transparent variant of 'Accent Teal' or a light border of #EDFFFE.

### Gradient System

The gradients are a crucial part of the Auros brand identity, representing fluidity, data streams, and cutting-edge technology. They are primarily linear, evolving through shades of teal, aqua, and soft pink/purple. The 'Vivid Aqua to Gold Gradient' (linear-gradient(90deg, rgb(0, 130, 124) 0%, rgb(203, 255, 252) 100%)) is reserved for primary interactive elements, while more complex, multi-stop gradients like 'Ethereal Spectrum' and 'Dynamic Flow Gradient' are utilized for large atmospheric backgrounds or decorative elements to create visual depth and a sense of movement. The 'Celestial Radial' is specifically for hero sections to emanate a focal glow. Ensure gradients are smooth and avoid harsh color blocks.

---
_Source: https://styles.refero.design/style/21cfe0c1-778d-4613-9f47-a5718eb929b3_
