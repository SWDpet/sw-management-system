# Tomorro — Design Reference

> Deep Forest AI Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.gotomorro.com](https://www.gotomorro.com) |
| Refero page | [https://styles.refero.design/style/bc7458ba-6b81-4f3e-ab5a-4126ee1eaf80](https://styles.refero.design/style/bc7458ba-6b81-4f3e-ab5a-4126ee1eaf80) |
| Theme | light |
| Industry | saas |

## Overview

Tomorro employs a 'Vibrant Efficiency' product language, combining a deep forest green and vivid lime green with a primarily monochrome palette. Typography is robust and functional, often in all-caps, commanding attention while maintaining clarity. Surfaces are clean and unburdened by heavy shadows, favoring subtle elevation and soft rounded corners. The design signals an authoritative yet approachable AI-powered solution.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Forest Canopy | `#122314` | brand | Primary background for dark sections, footer background — sets a deep, calming tone |
| Deep Moss | `#273f2b` | brand | Dark text for accents and interactive elements on light backgrounds, border color for ghost buttons, occasional card background — provides a strong contrast to lighter greens |
| AI Lime | `#68ef3f` | accent | Primary action button fill, highlight text for AI keywords, input borders, active badges — a clear, vivid indicator of interaction and innovation |
| Dark Olive | `#7e8371` | neutral | Muted supporting text, secondary link color, subtle borders — provides a soft contrast against lighter neutrals |
| Light Stone | `#b7bda5` | neutral | Lighter body text on white backgrounds, hints of borders — used for less prominent textual content |
| Pale Mint | `#e7f9dd` | brand | Background for subtle accents and badges on a light canvas — a very light extension of brand green |
| Soft Gray | `#d9deca` | neutral | Light borders for interactive elements like links — provides subtle definition without strong visual breaks |
| Evergreen Glow | `#26a200` | accent | Decorative icon accents, subtle brand elements in line with the main AI Lime, button borders — provides a deeper, more saturated green highlight |
| White | `#ffffff` | neutral | Default page background, card surfaces, primary text on dark backgrounds, icon fills — establishes a clean, open foundation |
| Ink | `#30322a` | neutral | Primary text color on light backgrounds, dark nav backgrounds, card text, strong header text — provides consistent readability across light surfaces |
| Carbon | `#000000` | neutral | Deepest background for certain sections, accent for high-contrast elements; Background gradient for hero sections or brand elements, transitioning from black to Deep Moss |
| Light Canvas | `#f2f5eb` | neutral | Secondary card background, subtle background sections on light themes — provides a soft, off-white alternative to pure white |
| Mid Gray | `#d6d6d6` | neutral | Subtle card backgrounds and dividers — provides minimal visual separation |
| UI Gray | `#dcdfe3` | neutral | Supporting UI text, subtle header accents — a neutral tone for less emphasis |

## Typography

### Aeonik

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 12px, 13px, 14px, 16px, 18px, 20px, 24px, 32px, 40px |
| lineHeight | 1.00, 1.10, 1.20, 1.33, 1.43, 1.50, 1.67, 1.70, 1.71 |
| letterSpacing | -0.031em at 40px, -0.021em at 32px, -0.015em at 24px, -0.012em at 20px, -0.003em at 18px, -0.001em at 16px, 0.013em at 14px, 0.143em at 12px |
| substitute | Inter |
| role | Primary typeface for body text, UI elements, navigation, and smaller headings. Its contemporary sans-serif character provides clarity and a slightly technical feel. Used across various weights to establish hierarchy. |

### Instrument Serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px, 18px, 32px |
| lineHeight | 1.25, 1.50, 1.56 |
| letterSpacing | -0.050em at 32px, 0.028em at 18px, 0.063em at 16px |
| substitute | Lora |
| role | Used for specific badge text and some headings, its serif style provides a distinctive, editorial contrast to Aeonik, adding a classic yet modern touch. |

### Ozik

| Key | Value |
| --- | --- |
| weight | 700 |
| weights | 700 |
| sizes | 56px, 80px |
| lineHeight | 0.86, 0.90 |
| letterSpacing | -0.014em at 56px, -0.010em at 80px |
| substitute | Oswald |
| role | Exclusive to large, impactful headings. Its heavy, bold character with tight tracking projects confidence and emphasizes key messages, creating powerful visual statements. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.71 | 0.143 |
| body | 14 |  | 1.67 | 0.013 |
| heading-sm | 18 |  | 1.43 | -0.003 |
| heading | 20 |  | 1.33 | -0.012 |
| heading-lg | 24 |  | 1.2 | -0.015 |
| display-sm | 32 |  | 1.1 | -0.021 |
| display-md | 40 |  | 1 | -0.031 |
| display-lg | 56 |  | 0.9 | -0.014 |
| display | 80 |  | 0.86 | -0.01 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| links | 20px |
| badges | 40px |
| inputs | 24px |
| buttons | 28px |
| containers | 16px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | 0 | Default page background, provides a clean and open foundation. |
| Light Canvas Card | `#f2f5eb` | 1 | Primary card background for content on light sections, secondary background layer. |
| Pale Mint Accent | `#e7f9dd` | 2 | Subtle background for smaller elements or badges, slight elevation in color. |
| Mid Gray Surface | `#d6d6d6` | 3 | Background for minimal visual separation or subtle information displays. |
| Forest Canopy Base | `#122314` | 4 | Deepest background for hero sections or footers, establishes a contrasting dark base. |

## Components

### Primary Filled Button

**Role:** Main call-to-action button, signaling key interactions.

Filled with AI Lime (#68ef3f), text in Deep Moss (#273f2b), padding 12px vertical, 20px horizontal, with a 28px border-radius. High visibility and clear action.

### Ghost Button (Dark)

**Role:** Secondary action on dark backgrounds, providing emphasis without distraction.

Transparent background, white text (#ffffff), white 1px border. Padding 12px vertical, 20px horizontal, with a 28px border-radius. Subtle interaction on dark surfaces.

### Header Navigation Button

**Role:** Compact navigation item with minimal styling.

Transparent background, white text (#ffffff), 4px border-radius. Padding 7px vertical, 7px horizontal. Used for navigation links that are not primary calls to action.

### Neutral Card

**Role:** Information container on light backgrounds.

Background in Light Canvas (#f2f5eb), 8px border-radius. Padding 40px on all sides. No shadow, emphasizing a flat, clean aesthetic.

### Dark Accented Card

**Role:** Elevated information container within dark sections, featuring a subtle highlight.

Background in Deep Moss (#273f2b), with 24px border-radius. Padding 8px on all sides. Primarily used for highlighted content in a darker context.

### White Card

**Role:** Clean, standard content container.

Background in White (#ffffff), 16px border-radius. Padding 8px on all sides. Used for general content and UI elements in light sections.

### Floating Content Card

**Role:** Prominent information display with significant spacing.

Background in Light Canvas (#f2f5eb), 12px border-radius. Padding 64px vertical, 80px horizontal. Used for sections requiring more visual weight and separation.

### Input Field

**Role:** Data entry control with a distinct active state.

Transparent background, text in AI Lime (#68ef3f), with an AI Lime 1px border, 26px border-radius. Focus on simplicity and color for interaction.

### Dark Accent Badge

**Role:** Highlighting short, descriptive labels.

Transparent background, white text (#ffffff), 40px border-radius. Padding 4px vertical, 12px horizontal. Used for tags and categories on dark surfaces.

### Event Highlight Badge

**Role:** Prominent, informational banner for special announcements.

Background with 8% opacity white (rgba(255, 255, 255, 0.08)), text in White (#ffffff), 20px border-radius. Padding 8px vertical, 12px horizontal. Utilized for important, high-level notifications.

## Layout

The page uses a full-bleed structure for background sections, but content is largely constrained within a centered max-width container, appearing to be around 1200px wide. The hero section is a full-width dark background band with a prominent centered headline, supporting text, and AI Lime buttons. Subsequent sections alternate backgrounds between light canvas (#ffffff) and Light Canvas (#f2f5eb), creating distinct visual blocks. Content is arranged in alternating text-left/image-right or centered stacks. Card grids are used for features, typically in a 3-column layout. The vertical rhythm is spacious, with a consistent 64px `sectionGap` between primary content blocks. Navigation is a sticky top bar with a logo, text links, and two distinct buttons (Sign In, Schedule a demo).

## Imagery

Imagery primarily consists of abstract, organic 3D shapes with subtle gradient or noise textures, sometimes in brand colors. Product screenshots are rendered with slight perspective and clean UIs. Photography is minimal, if present, and focused on clean, professional contexts. Icons are typically filled, monochrome (white on dark, dark on light), with a consistent stroke weight. The overall visual language is atmospheric and clean, using abstract forms to represent complex AI concepts rather than literal depictions. Images are generally contained within their sections, not full-bleed, but some overlap with other elements is present, creating visual depth.

## Dos & Donts

### Do

- Use AI Lime (#68ef3f) as the exclusive color for primary calls to action, ensuring it always stands out.
- Employ the Ozik font at sizes 56px and 80px, with its tight letter spacing (`-0.014em` and `-0.010em` respectively), for all main page headlines to convey authority and impact.
- Maintain a clear visual hierarchy by consistently using Deep Moss (#273f2b) for strong text on light backgrounds and White (#ffffff) for text on Forest Canopy (#122314) areas.
- Apply a 28px border-radius to all primary and secondary action buttons to maintain a consistent soft, confident interactive element.
- Ensure section vertical spacing is consistently 64px to create a sense of comfortable density.
- Use Light Canvas (#f2f5eb) for secondary light-themed content cards and White (#ffffff) for primary light-themed cards, providing subtle background variations.
- Utilize fine 1px borders in Soft Gray (#d9deca) or White (#ffffff) for ghost elements or subtle distinctions, avoiding heavy lines.

### Don't

- Do not introduce new saturated primary colors beyond AI Lime (#68ef3f) and Evergreen Glow (#26a200); the system relies on a restrained palette.
- Avoid using drop shadows on cards or buttons; the design system favors flat surfaces or minimal, integrated elevation.
- Do not deviate from the specified letter spacing for Ozik and Aeonik fonts, as precise tracking is critical for their distinct character.
- Do not use dark backgrounds for body text sections unless explicitly within a Forest Canopy (#122314) context, and always use white text.
- Never use generic square buttons; all interactive buttons should incorporate the brand's rounded corners (28px or 4px for minimal).
- Avoid combining multiple bold text styles or colors unnecessarily; hierarchy is achieved through size, weight, and the distinct Ozik typeface, not excessive adornment.
- Do not break the consistent 64px vertical section gap; maintaining this rhythm is key to the page's structure and density.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #30322a (Ink)
- background: #ffffff (White)
- border: #d9deca (Soft Gray)
- accent: #68ef3f (AI Lime)
- primary action: #68ef3f (filled action)

### 3-5 Example Component Prompts
- Create a hero section: Forest Canopy (#122314) background, with the Deep Fade Gradient. Centered headline 'MANAGING CONTRACTS IN THE AGE OF AI' using Ozik font, weight 700, 80px size, -0.010em letter-spacing, white text. Subheading 'Accelerate your contractual processes...' using Aeonik, weight 400, 24px size, Ink (#30322a). Two action buttons below: 'Schedule a demo' as a Primary Filled Button (AI Lime #68ef3f, 28px radius, 12px vertical/20px horizontal padding) and 'Watch a video demo' as a Ghost Button (Dark) (transparent, white text, white 1px border, 28px radius, 12px vertical/20px horizontal padding), separated by 24px horizontal gap.
- Create a pricing tier card: White Canvas (#ffffff) background, 8px border-radius, 40px padding. Heading 'Basic Plan' using Aeonik, weight 600, 24px size, Ink (#30322a). Price '€49/month' using Ozik, weight 700, 56px size, Evergreen Glow (#26a200). Feature list as Aeonik, weight 400, 16px size, Ink (#30322a).
- Create a testimonial section: Light Canvas (#f2f5eb) background. Headline 'What our customers say' using Aeonik, weight 600, 32px size, Ink (#30322a). Testimonial text 'Tomorro has transformed our workflow...' using Instrument Serif, weight 400, 18px size, Dark Olive (#7e8371).
- Create a navigation link: Aeonik font, weight 500, 16px size, Ink (#30322a) color, transparent background.

---
_Source: https://styles.refero.design/style/bc7458ba-6b81-4f3e-ab5a-4126ee1eaf80_
