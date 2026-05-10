# ElevenReader — Design Reference

> diffused light on a minimalist stage

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://elevenreader.io](https://elevenreader.io) |
| Refero page | [https://styles.refero.design/style/c51c8371-0e42-4bdf-9766-c9eac5eee9a5](https://styles.refero.design/style/c51c8371-0e42-4bdf-9766-c9eac5eee9a5) |
| Theme | light |
| Industry | productivity |

## Overview

This design evokes a sense of airy professionalism, like a digital stage bathed in diffused light presenting focused information. The near-monochromatic palette uses subtle off-whites and grays to create spaciousness, punctuated by sharp black text and interactive elements. The deliberate contrast between full-round pill shapes and sharp 16px corners on cards establishes a nuanced balance between approachable softness and precise functionality.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#000000` | neutral | Primary text, buttons, brand elements — provides stark contrast on light backgrounds. |
| Cloud White | `#ffffff` | neutral | Page backgrounds, card surfaces, button text — ensures clarity and an open feel. |
| Ash Gray | `#e5e5e5` | neutral | Subtle borders, dividers, inactive states — defines structure without distraction. |
| Pale Mist | `#f2f2f2` | neutral | Secondary button backgrounds, section backgrounds — provides a gentle visual break from stark white. |
| Faded Sky | `#f2f5fc` | neutral | Subtle background shading — introduces a hint of cool without overt color. |
| Steel Gray | `#767676` | neutral | Secondary text, descriptive elements — lighter than primary text for hierarchy. |
| Charcoal Haze | `#6e6e6` | neutral | Tertiary text, subtle hints — even less prominent than secondary text. |
| Muted Lavender | `#c8d5f4` | neutral | Decorative background accent — provides a very subtle chromatic undertone without becoming a brand color. |
| Verdant Aura | `#243f2b` | accent | Accent gradient start, contributing to atmospheric background effects. |
| Forest Dew | `#c6e7d6` | accent | Subtle background gradient end, creating a soft, natural transition. |

## Typography

### WaldenburgHF

| Key | Value |
| --- | --- |
| weight | 700 |
| sizes | 28px, 32px, 48px |
| lineHeight | 1.10 |
| letterSpacing | 0.01 |
| substitute | Inter |
| role | Display and primary section headings — custom font lending a distinct, modern digital signature. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| sizes | 12px, 14px, 16px, 18px |
| lineHeight | 1.10, 1.40, 1.43, 1.60 |
| letterSpacing | 0.01 |
| substitute | system-ui |
| role | All body text, navigation, buttons, and secondary headings — provides high readability and a clean, technical feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.1 | 0.01 |
| body-sm | 14 |  | 1.4 | 0.01 |
| body | 16 |  | 1.43 | 0.01 |
| subheading | 18 |  | 1.6 | 0.01 |
| heading | 28 |  | 1.1 | 0.01 |
| heading-lg | 32 |  | 1.1 | 0.01 |
| display | 48 |  | 1.1 | 0.01 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| links | 2px |
| buttons | 9999px |
| ctaCard | 30px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 0px
- **pageMaxWidth** — 1304px

## Components

### CTA Button Group

### FAQ Accordion

### App Download QR Card

### Secondary Action Button

**Role:** Filled button for secondary actions.

Background: Pale Mist (#f2f2f2), Text: Midnight Ink (#000000), Border: Ash Gray (#e5e5e5), Radius: 9999px, Padding: 0px 16px.

### Tertiary Action Button

**Role:** Outline button for less prominent actions.

Background: Cloud White (#ffffff), Text: Midnight Ink (#000000), Border: Ash Gray (#e5e5e5), Radius: 9999px, Padding: 0px 12px.

### Ghost Button

**Role:** Minimal button for subtle interactions.

Background: transparent (rgba(0,0,0,0)), Text: Midnight Ink (#000000), Border: Ash Gray (#e5e5e5), Radius: 9999px, Padding: 2px 12px.

### Transparent Card

**Role:** Card with no background, for organizing content.

Background: transparent (rgba(0,0,0,0)), Border: none, Radius: 0px, Shadow: none, Padding: 0px.

### Rounded Information Card

**Role:** Card for containing distinct information blocks.

Background: transparent (rgba(0,0,0,0)), Border: none, Radius: 16px, Shadow: none, Padding: 0px 64px.

### Navigation Link

**Role:** Top navigation item.

Text: Midnight Ink (#000000), Underline: none, Font: Inter, Weight: 400, Size: 16px, Line Height: 1.43.

### Hero Headline

**Role:** Primary page title.

Text: Midnight Ink (#000000), Font: WaldenburgHF, Weight: 700, Size: 48px, Line Height: 1.1, Letter Spacing: 0.01em.

### QR Code Callout

**Role:** Promotional card for app download.

Background: Cloud White (#ffffff), Border: 1px solid Ash Gray (#e5e5e5), Radius: 16px, Padding: 20px, Shadow: rgba(16, 24, 40, 0.05) 0px 1px 2px 0px.

## Layout

The page adheres to a max-width contained model (1304px), with content centered within this boundary. The hero section is a full-width presentation featuring a bold, centered headline and subtext, often laid over a subtle background gradient. Section rhythm is predominantly defined by consistent vertical spacing, creating spacious breathing room between content blocks. Content arrangement frequently uses horizontally aligned elements, such as multi-column layouts for features or news articles. Navigation is a sticky top bar, containing branding, main links, and clear calls-to-action on the right. Sidebars are not present, maintaining a clean, linear flow.

## Imagery

The visual language is dominantly product-focused, featuring tight crops of mobile device screens showcasing the ElevenReader app UI. These are typically contained and isolated, without decorative masks or overlapping elements. Photography appears absent. Illustrations consist of 2D, filled icons with a light stroke weight, used primarily for functional purposes (e.g., QR code). Abstract gradients occasionally provide decorative background context, but they are soft and desaturated, never overwhelming the UI. The density is text-dominant, with imagery serving to explain or demonstrate product features rather than create atmosphere.

## Dos & Donts

### Do

- Use Cloud White (#ffffff) as the primary page background to maintain an open and airy aesthetic.
- Apply Midnight Ink (#000000) for all primary text elements and main CTA backgrounds for maximum contrast.
- Round all interactive buttons and navigation elements with a 9999px radius for visual softness and approachability.
- Utilize WaldenburgHF font for headlines (28px, 32px, 48px) at weight 700 to establish a bold yet refined presence.
- Employ Ash Gray (#e5e5e5) for thin borders and dividers to structure content subtly.
- Maintain generous vertical spacing between sections, primarily using the 40px `sectionGap` token.
- Group related information within cards with a 16px border-radius and minimal or no background (#ffffff or transparent).

### Don't

- Avoid strong, saturated colors; stick to the neutral palette and only use subtle chromatic hints for atmosphere.
- Do not use sharp corners for interactive elements; buttons and navigation should always be fully rounded (9999px).
- Refrain from heavy shadows; subtle shadows like `rgba(16, 24, 40, 0.05) 0px 1px 2px 0px` are acceptable only for floating elements.
- Do not vary from the Inter font family for body text, navigation, and secondary UI elements to maintain consistency.
- Avoid tight element spacing; ensure a minimum `elementGap` of 8px to preserve an uncluttered layout.
- Do not use dark backgrounds for main content sections; the system is designed for a light theme with ample whitespace.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- Text: Midnight Ink (#000000)
- Background: Cloud White (#ffffff)
- CTA Primary: Midnight Ink (#000000)
- Border: Ash Gray (#e5e5e5)
- Secondary BG: Pale Mist (#f2f2f2)

Example Component Prompts:
1. Create a primary call-to-action button: background Midnight Ink (#000000), text Cloud White (#ffffff), border Ash Gray (#e5e5e5), radius 9999px, padding 0px 16px, with text 'Start Free Trial'.
2. Design a section headline: text Midnight Ink (#000000), font WaldenburgHF, weight 700, size 48px, line height 1.1, letter spacing 0.01em, with content 'Listen to anything'.
3. Generate a secondary button: background Pale Mist (#f2f2f2), text Midnight Ink (#000000), border Ash Gray (#e5e5e5), radius 9999px, padding 0px 16px, with text 'Try It Now'.
4. Create a standard body paragraph: text Midnight Ink (#000000), font Inter, weight 400, size 16px, line height 1.43, letter spacing 0.01em.
5. Design an information card with 16px radius: background transparent (rgba(0,0,0,0)), border none, padding 0px 64px.

---
_Source: https://styles.refero.design/style/c51c8371-0e42-4bdf-9766-c9eac5eee9a5_
