# Gleap — Design Reference

> Crisp canvas, magenta highlight

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://gleap.io](https://gleap.io) |
| Refero page | [https://styles.refero.design/style/2eab438d-32cd-40c2-b160-1e4127dac569](https://styles.refero.design/style/2eab438d-32cd-40c2-b160-1e4127dac569) |
| Theme | light |
| Industry | saas |

## Overview

Gleap's visual style operates on a high-contrast model, pairing crisp, modern sans-serifs with a single, highly saturated magenta-purple accent. The UI uses spacious layouts and soft, rounded containers on a light background, creating a calm yet energetic feel. Typography shifts from inviting, editorial display fonts for headlines to compact, functional system fonts for body text. Interaction elements prominently feature the brand's signature purple to guide user focus and denote primary actions.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Cloud Canvas | `#f5f2f0` | neutral | Primary page background, provides a soft, warm base for all content |
| Porcelain Surface | `#ffffff` | neutral | Card backgrounds, elevated content areas, ensuring high contrast with text |
| Graphite Text | `#333333` | neutral | Primary text color for body copy, links, and detailed information |
| Ink Text | `#000000` | neutral | Headlines, navigation items, and strong textual elements for maximum emphasis |
| Platinum Border | `#d6d6d6` | neutral | Subtle borders and dividers for UI separation without harsh lines |
| Silver Detail | `#bcbcbc` | neutral | Muted helper text, secondary borders, and subtle accent lines |
| Deep Plum | `#7b7b7b` | neutral | Tertiary text, less prominent links and meta information |
| Amethyst Accent | `#f1ccff` | brand | Primary action buttons, prominent links, and accents within cards — it’s the brand’s signature interaction color, signaling interactivity |
| Sky Blue Highlight | `#91e0ff` | accent | Decorative card backgrounds, highlighted sections or text within content — provides visual interest |

## Typography

### PP Editorial New

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 48px, 62px |
| lineHeight | 1.20, 1.25, 1.30 |
| letterSpacing | normal |
| substitute | Playfair Display |
| role | Display headlines and prominent section titles. Its classical, editorial feel contrasts the modern sans-serifs, lending an authoritative yet approachable tone. |

### Switzer

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 13px, 14px, 16px, 20px, 32px |
| lineHeight | 1.19, 1.20, 1.40, 1.43, 1.44 |
| letterSpacing | -0.025em for 32px, -0.020em for 20px, -0.010em for 16px, normal for 13-14px |
| substitute | Inter |
| role | All body text, subheadings, labels, and functional UI elements. Its clean, geometric form maintains readability and directness across the interface. Letter spacing is subtly tightened at larger sizes for visual density. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px, 16px |
| lineHeight | 1.25, 1.43 |
| letterSpacing | normal |
| substitute | Roboto |
| role | Fallback and specific UI components where extreme simplicity and system-level rendering is preferred, such as internal tools or complex data displays. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.00 |
| letterSpacing | normal |
| substitute | Segoe UI |
| role | Used for highly compact labels or system messages where minimal footprint is critical. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.4 | 0 |
| body | 16 |  | 1.4 | -0.16 |
| subheading | 20 |  | 1.2 | -0.4 |
| heading | 32 |  | 1.44 | -0.8 |
| heading-lg | 48 |  | 1.25 | 0 |
| display | 62 |  | 1.3 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| badges | 10px |
| buttons | 10px |
| largeElements | 42px |

- **elementGap** — 16px
- **sectionGap** — 30px
- **cardPadding** — 40px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Cloud Canvas | `#f5f2f0` | 0 | Base page background, creating a soft, warm foundation for the entire interface. |
| Porcelain Surface | `#ffffff` | 1 | Default background for UI cards, panels, and other contained content blocks resting on the base canvas. |

## Components

### Primary Action Button (Amethyst)

**Role:** Main call to action, filled with the brand's primary color.

Background: #f1ccff (Amethyst Accent), Text: #000000 (Ink Text), Border: #f1ccff, Padding: 10px vertical, 15px horizontal, Border-radius: 10px.

### Secondary Action Button (Outline)

**Role:** Less prominent actions, using only a border to differentiate.

Background: transparent, Text: #000000 (Ink Text), Border: 1px solid #000000, Padding: 17px vertical, 20px horizontal, Border-radius: 0px.

### Affirmative Action Button (Dark)

**Role:** Confirmation or direct action button, inverted color scheme from the page.

Background: #000000 (Ink Text), Text: #ffffff (Porcelain Surface), Border: #000000, Padding: 10px vertical, 15px horizontal, Border-radius: 10px.

### Icon Button (Dark Accent)

**Role:** Compact button, often used for single actions or icon-only controls, with a distinctive larger radius.

Background: #000000 (Ink Text), Text: #ffffff (Porcelain Surface), Border: #ffffff, Padding: 14px vertical, 16px horizontal, Border-radius: 16px.

### Product Feature Card

**Role:** Highlights key features or content, with subtle elevation.

Background: #ffffff (Porcelain Surface), Padding: 40px, Border-radius: 24px, Shadow: rgba(0, 0, 0, 0.04) 0px 8px 16px 0px.

### Informational Badge

**Role:** Categorizes or labels content with a soft, muted appearance.

Background: transparent, Text: #333333 (Graphite Text), Padding: 5px vertical, 12px horizontal, Border-radius: 10px, Border: 1px solid #f5f2f0 (Cloud Canvas).

## Layout

The page employs a max-width 1200px centered layout with a dominant light theme. Hero sections often feature a centered headline in the distinct serif font over atmospheric, organic gradient backgrounds. Vertical rhythm is established with a significant 30px gap between sections. Content sections frequently use an alternating text-left/visual-right pattern, or stacked centered blocks for feature descriptions. Pricing and feature comparisons often fall into multi-column (3-column) card grids. Navigation is a sticky top bar with a left-aligned logo and right-aligned actions, often using a distinct dark pill-shaped button for 'Sign up'.

## Imagery

The visual language predominantly features clean, product-focused screenshots of the software UI, often presented within rounded, slightly elevated containers. There's also use of abstract, gradient-rich backgrounds with subtle geometric shapes (like moons or soft hills) that provide atmospheric depth without competing with content. Iconography is generally monochromatic, using a filled style with clean lines. Imagery serves an explanatory and showcase role, demonstrating product functionality and providing decorative atmosphere rather than lifestyle context. Overall, it's a balance of functional UI demonstration and soft, atmospheric branding.

## Dos & Donts

### Do

- Use PP Editorial New (weight 400) for all display headlines, setting them at 48px or 62px with normal letter spacing for an elevated, editorial feel.
- Apply Switzer (weights 400, 500, 600) for all body text, subheadings, and UI labels, adjusting letter spacing to -0.010em for 16px, -0.020em for 20px, and -0.025em for 32px to maintain visual compactness.
- Elevate primary calls-to-action with Amethyst Accent (#f1ccff) for backgrounds, paired with Ink Text (#000000) for readability.
- Utilize a 10px border-radius for all buttons and badges, reserving 24px for cards and 42px for distinct large elements, to establish a consistent soft-rounded identity.
- Maintain a clear visual hierarchy by using Ink Text (#000000) for critical headlines and bold elements, and Graphite Text (#333333) for standard body copy and descriptions.
- Structure layout using a 1200px max-width container, centered on the Cloud Canvas (#f5f2f0) background, with a consistent 30px vertical gap between major sections.
- Apply subtle elevation to key UI components like Product Feature Cards using rgba(0, 0, 0, 0.04) 0px 8px 16px 0px shadows, while keeping default backgrounds clean and shadow-less.

### Don't

- Avoid using multiple chromatic colors for primary actions; Amethyst Accent (#f1ccff) is the singular brand color for interactive elements.
- Do not introduce sharp corners or unrounded containers; enforce the 10px, 24px, or 42px border-radius system meticulously.
- Refrain from using strong, colorful gradients or textures; the system thrives on clean, mostly flat surfaces and subtle, tonal backgrounds.
- Do not deviate from the defined type scale and letter spacing values, particularly for headlines and body text, to preserve the distinct typographic voice.
- Avoid overuse of shadows; reserve the rgba(0, 0, 0, 0.04) 0px 8px 16px 0px shadow for cards and key elevated components only, preventing visual clutter.
- Do not introduce new border colors for UI elements; stick to Platinum Border (#d6d6d6) or Ink Text (#000000) for subtle separation.
- Resist dense layouts; ensure generous use of the 16px elementGap and 40px cardPadding to maintain a comfortable reading experience and visual breathing room.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #333333
background: #f5f2f0
border: #d6d6d6
accent: #91e0ff
primary action: #f1ccff (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #f1ccff background, #000000 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a Product Feature Card: Porcelain Surface background, 40px padding, 24px border-radius, with a shadow rgba(0, 0, 0, 0.04) 0px 8px 16px 0px. Inside, use Graphite Text for a body paragraph (Switzer, 16px, weight 400, letter-spacing -0.16px), and a secondary action button with a transparent background, Ink Text color, 1px solid Ink Text border, 0px border-radius, 17px vertical and 20px horizontal padding, labeled 'Learn More'.
3. Construct a navigation bar item: Text 'Product' in Switzer weight 400, 16px, Ink Text color. When active, underline with a 2px border of Amethyst Accent. Use 16px elementGap for horizontal spacing between items.

---
_Source: https://styles.refero.design/style/2eab438d-32cd-40c2-b160-1e4127dac569_
