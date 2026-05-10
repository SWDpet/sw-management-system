# Whimsical — Design Reference

> Plush Violet Cloud

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://whimsical.com](https://whimsical.com) |
| Refero page | [https://styles.refero.design/style/8e153a14-40a9-4793-b94b-c144d325c730](https://styles.refero.design/style/8e153a14-40a9-4793-b94b-c144d325c730) |
| Theme | light |
| Industry | productivity |

## Overview

Whimsical evokes a vibrant, playful productivity studio: plush, rounded surfaces and a dynamic color palette built on deep violets and fuchsia gradients. Typography is clean and highly readable, grounding the energetic visuals. Components are generous in padding and radius, fostering an inviting tactile feel. The overall impression is one of approachable sophistication, designed for creative flow and seamless interaction.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Plum | `#250835` | brand | Primary text, prominent headings, primary action background — a deep, rich violet serving as a anchor for the vibrant system |
| Soft Mauve | `#473054` | neutral | Muted text, secondary body copy, subtle borders and outlines — a softer, desaturated variant of the primary brand color |
| Faded Violet | `#ab2fed` | accent | Interactive link text, accent icons, highlighted borders — a vivid violet used for emphasis and interactive states |
| Sky Blue | `#0283ec` | accent | Blue decorative accent for icons, marks, and small graphic details. Do not promote it to the primary CTA color |
| Luminous Grape | `#4b38ee` | accent | Violet decorative accent for icons, marks, and small graphic details. Do not promote it to the primary CTA color |
| Fuschia Delight Gradient | `#ba59ff` | brand | Decorative background gradients, highlight elements for hero sections — creates a sense of depth and dynamism |
| Cosmic Bloom Gradient | `#3ca1ff` | brand | Supporting palette color for small decorative accents when the core palette needs contrast. Do not promote it to the primary CTA color |
| Canvas White | `#ffffff` | neutral | Page background, primary card surfaces, ghost button backgrounds — provides a crisp, clean foundation for content |
| Ash Gray | `#ebe6ee` | neutral | Secondary surface background, section dividers, subtle card backgrounds — slightly off-white for visual separation without harsh contrast |
| Silver Mist | `#f5f4f5` | neutral | Lightest surface background, subtle card variations — a very light gray for minimal surface elevation |
| Washed Lavender | `#decaff` | neutral | Decorative card backgrounds, accent washes — a very light, desaturated violet for background textures |
| Blush Pink | `#e9bded` | neutral | Decorative background surfaces, accent panels reminiscent of the brand's playful side |
| Slate Shadow | `#cdc7d1` | neutral | Supporting neutral for secondary UI, dividers, and muted labels. Do not promote it to the primary CTA color |
| Dusty Lilac | `#6a5b72` | neutral | Muted secondary text, subtle borders, inactive link states — a mid-tone desaturated violet for hierarchy |
| Pewter Tone | `#918499` | neutral | Tertiary text, subtle separators, copyright text — a lighter, desaturated gray for unobtrusive details |

## Typography

### Agrandir

| Key | Value |
| --- | --- |
| weight | 700 |
| weights | 700 |
| sizes | 24px, 48px, 64px, 96px |
| lineHeight | 1.00, 1.10 |
| letterSpacing | -0.0100em |
| substitute | Montserrat |
| role | Display headings, hero text, section titles — a bold, expansive sans-serif with subtle negative tracking for a confident, modern statement at large sizes. |

### Manrope

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 9px, 10px, 12px, 13px, 14px, 16px, 32px |
| lineHeight | 1.00, 1.20, 1.40, 2.67 |
| letterSpacing | 0.0090em |
| substitute | Inter |
| role | Body copy, UI elements, navigation, buttons, small text — a versatile, highly readable sans-serif with varied weights and positive tracking for clarity and approachability. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.4 | 0.15 |
| body-sm | 14 |  | 1.4 | 0.01 |
| body | 16 |  | 1.4 | 0.009 |
| subheading | 24 |  | 1.1 | -0.24 |
| heading | 48 |  | 1.1 | -0.48 |
| display | 64 |  | 1 | -0.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| buttons | 12px |
| containers | 16px |
| largeElements | 120px |

- **elementGap** — 16px
- **sectionGap** — 60px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Canvas | `#ffffff` | 0 | Dominant background for the entire page, providing a bright, clean foundation. |
| Base Surface | `#f5f4f5` | 1 | Slightly elevated background for minor sections or subtle container separation, providing minimal visual break. |
| Card Surface | `#ebe6ee` | 2 | Primary background for cards and feature blocks, offering a clear visual boundary against the page canvas. |
| Accent Surface | `#decaff` | 3 | Decorative background for specific cards or sections, adding a soft, branded color wash. |
| Decorative Highlight | `#e9bded` | 4 | Stronger decorative background to draw attention to key sections, often paired with gradients. |

## Components

### Primary Filled Button

**Role:** Main call to action, interactive controls

Uses Midnight Plum (#250835) background with Canvas White (#ffffff) text, a generous 12px border radius, and 8px vertical, 40px horizontal padding. Features a subtle elevated shadow (rgba(37, 8, 53, 0.06) 0px 16px 32px -4px).

### Menu Ghost Button

**Role:** Secondary navigation links, discreet actions

Transparent background with Midnight Plum (#250835) text and border, 12px radius, and 0px vertical, 18px horizontal padding. Used for subtle, non-intrusive actions.

### Large Feature Button

**Role:** Prominent feature calls to action within content sections

Canvas White (#ffffff) background with Midnight Plum (#250835) text, a generous 16px border radius, and 24px padding all around. Creates a spacious, inviting target.

### Standard Content Card

**Role:** Organizes content blocks, visual features

Ash Gray (#ebe6ee) background with 8px border radius. Padding is 24px left, 0px right, 0px top, 0px bottom. Used for visual distinction of content.

### Decorative Background Card (Washed Lavender)

**Role:** Background visual elements, abstract shapes

Washed Lavender (#decaff) background with a large 120px border radius, no padding or shadow. Functions as a soft, rounded accent shape.

### Hero Message Bar

**Role:** Top-banner message, alert, or navigation

Deep gradient from Fuschia Delight Fade (#ba59ff) to Pink Fade, with a 12px border radius. Contains Canvas White text and a link to a new feature.

### Text Link - Faded Violet

**Role:** Inline links, secondary calls to action

Uses Faded Violet (#ab2fed) for text color. No explicit background, serves as a chromatic accent for navigation within content.

## Layout

The page uses a contained layout with a maximum width of approximately 1200px, centering content within a generous horizontal margin. The hero section is full-bleed, featuring a large, rounded background panel with a bold, centered headline and a prominent primary action. Sections below alternate between white and a very light gray (#f5f4f5 or #ebe6ee) backgrounds, separated by a consistent 60px vertical gap. Content is arranged in alternating two-column text-left/image-right patterns, and three-column grids for feature cards, each with ample internal padding. Navigation is a sticky top bar with a branded logo, links, and distinct sign-up/login buttons.

## Imagery

Whimsical employs product screenshots and abstract, geometric illustrations. Product screenshots are contained within heavily rounded 'device' frames and gently elevated with shadows, showcasing the UI in a friendly, approachable manner. Illustrations are flat, often outlined, and utilize the brand's vibrant violet and blue accent colors, appearing as decorative elements to enhance content. Icons are outlined, fine-lined, and mono-color, primarily using the Faded Violet and Sky Blue accents. Imagery serves to explain capabilities, showcase the product, and add a playful, modern atmosphere without being overwhelming, balancing density with spacious UI.

## Dos & Donts

### Do

- Prioritize Midnight Plum (#250835) for primary textual elements, including body copy and main headings, ensuring high contrast against light backgrounds.
- Use Manrope font with positive letter spacing (e.g., 0.0090em for body text) to maintain readability across all UI elements.
- Apply generous border radii, typically 12px for buttons and 8px for cards, to reinforce the soft, approachable aesthetic.
- Implement the linear-gradient(142deg, #ba59ff 0%, #ba59ff 30%, #ff59f1) for hero banner backgrounds and brand-defining visual accents.
- Maintain comfortable spacing with a 16px element gap and 24px card vertical padding to prevent visual clutter.
- Utilize a subtle, diffused shadow rgba(37, 8, 53, 0.06) 0px 16px 32px -4px for primary buttons to give a gentle lift.
- Feature large, bold Agrandir display headings (e.g., 64px, -0.01em letter spacing) against the soft background surfaces to command attention.

### Don't

- Avoid sharp corners; all interactive elements and contained surfaces should have visible rounding.
- Do not use highly saturated colors for large background areas; reserve vivid tones for accents, interactive states, and gradients.
- Steer clear of aggressive, high-contrast shadows; elevation should be subtle and hint at depth rather than creating harsh lines.
- Do not neglect the Manrope font's positive letter spacing; tighter tracking compromises the friendly, readable aesthetic of body text.
- Avoid using multiple chromatic colors for primary actions; maintain Midnight Plum (#250835) as the dominant call-to-action color.
- Do not introduce square or rectangular brand imagery; all product mockups and illustrations should align with the rounded aesthetic.
- Refrain from dense, information-heavy layouts; give elements and sections ample breathing room dictated by the 60px section gap.

## Notes

### Agent Prompt Guide

### Quick Color Reference
text: #250835
background: #ffffff
border: #6a5b72
accent: #ab2fed
primary action: #250835 (filled action)

### 3-5 Example Component Prompts
1. Create a Primary Action Button: #250835 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a feature card: Ash Gray (#ebe6ee) background, 8px radius, 24px left padding. Title uses Manrope weight 600 at 16px, Midnight Plum (#250835) text. Body text is Manrope weight 400 at 14px, Soft Mauve (#473054) text. Include a Faded Violet (#ab2fed) text link for 'Explore'.
3. Construct a navigation bar: Canvas White (#ffffff) background. Left-aligned brand logo. Right-aligned ghost buttons using Midnight Plum (#250835) text and border, 12px radius, 0px vertical 18px horizontal padding. The 'Sign up' button uses Midnight Plum (#250835) background, Canvas White (#ffffff) text, 12px radius, 8px vertical 40px horizontal padding, with a subtle shadow.

---
_Source: https://styles.refero.design/style/8e153a14-40a9-4793-b94b-c144d325c730_
