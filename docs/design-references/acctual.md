# Acctual — Design Reference

> Architectural blueprint on white marble. Precision, clarity, and transparent flow of information.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://acctual.com](https://acctual.com) |
| Refero page | [https://styles.refero.design/style/aeefc294-a8f7-443d-b76a-538dddc29afe](https://styles.refero.design/style/aeefc294-a8f7-443d-b76a-538dddc29afe) |
| Theme | light |
| Industry | fintech |

## Overview

Acctual is a clean, sharp accounting software interface, defined by abundant whitespace, crisp typography, and an almost entirely achromatic palette punctuated by a single vibrant teal accent. It feels like an impeccably organized digital ledger, where the strict logical layout and high contrast ensure every piece of financial data is immediately comprehensible. The signature element is the interplay of system sans-serif for secondary text with the bespoke Open Runde, creating a dual texture of approachability and structured precision.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, primary text contrast. |
| Ink Black | `#000000` | neutral | Primary text, critical headings, strong brand emphasis. Its absolute blackness provides uncompromising legibility against white. |
| Graphite | `#0f0f0f` | neutral | Prominent headings and body text, a slightly softer variant of Ink Black. |
| Deep Slate | `#1e1e1` | neutral | Secondary body text and descriptions, offering a subtle visual break from pure black without sacrificing contrast. |
| Ash Gray | `#8d8d8d` | neutral | Subtle text, metadata, disabled states. Provides gentle visual hierarchy. |
| Button Black | `#0d111b` | neutral | Primary action buttons, providing a solid, grounded feel against the white background. |
| Sky Teal | `#0098f2` | accent | Interactive elements, links, checkmarks, highlights — the sole vibrant accent for key user actions and positive indications. |
| Hot Pink | `#f200ca` | brand | Decorative elements or specific brand highlights within icons, a secondary accent for visual interest. |
| Vivid Violet | `#6d56fc` | brand | Decorative elements or specific brand highlights within icons, alongside Hot Pink. |
| Subtle Cream | `#f7fafc` | neutral | Alternative background for sections, creating subtle depth on the mostly white page. |

## Typography

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | system-ui, 'Segoe UI', Roboto, Helvetica Neue, Arial, Noto Sans, sans-serif |
| role | Smallest text elements, UI labels, and highly functional information. It has a utilitarian feel which contrasts with the custom font for core content. |

### Open Runde

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 14px, 16px, 20px, 32px, 40px, 64px |
| lineHeight | 1.13, 1.20, 1.21, 1.25, 1.40, 1.43, 1.50, 1.71, 1.75 |
| letterSpacing | -0.037em at 64px, -0.030em at 40px, -0.020em at 32px, -0.012em at 20px, normal at smaller sizes |
| fontFeatureSettings | "blwf" on, "cv03" on, "cv04" on, "cv09" on, "cv11" on |
| substitute | Inter |
| role | Primary display font for headings, body text, and key content. Its distinctive features and precise letter-spacing contribute to the crisp, modern feel. The feature settings indicate careful typographic attention. |

### Caveat

| Key | Value |
| --- | --- |
| weight | 600 |
| sizes | 16px, 24px |
| lineHeight | 1.33, 1.50 |
| substitute | Sacramento |
| role | Handwritten script used for subtle accents like testimonials or decorative elements, providing a human touch to an otherwise structured interface. |

### Inter

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 14px |
| lineHeight | 1.43 |
| substitute | Inter |
| role | Used for specific body text elements, likely in contexts where Open Runde might be too decorative or less performant. It fills a very specific role, avoiding the system sans-serif. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 |  |
| body-sm | 14 |  | 1.43 |  |
| body | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.25 | -0.24 |
| heading | 32 |  | 1.21 | -0.64 |
| heading-lg | 40 |  | 1.2 | -1.2 |
| display | 64 |  | 1.13 | -2.368 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 20px |
| badges | 1250px |
| images | 32px |
| buttons | 100px |

- **elementGap** — 4-24px
- **sectionGap** — 40-80px
- **cardPadding** — 24px
- **pageMaxWidth** — 1200px

## Components

### Hero CTA with Feature Badges

### Testimonial Card

### FAQ Accordion

### Primary Action Button

**Role:** Call to action, e.g. 'Create an invoice'

Text: Open Runde, weight 400, color Canvas White. Background: Button Black (#0d111b). Hover/Active: No explicit hover state, implies primary color changes or subtle elevation. Radius: 100px (fully pill-shaped). Padding: 6px vertical, 14px horizontal. Shadow: Subtle dark shadow rgb(36, 38, 40) 0px 0px 0px 1px, rgba(27, 28, 29, 0.48) 0px 1px 2px 0px.

### Navigation Link

**Role:** Top navigation items

Text: Open Runde, weight 400, color Ash Gray (#8d8d8d) for inactive, Ink Black (#000000) for active/hover. No explicit styling beyond color change, relying on surrounding elements for distinction.

### Invoice Card

**Role:** Displaying invoice previews

Background: Canvas White (#ffffff). Radius: 20px. Shadow: subtle rgba(0, 0, 0, 0.06) 0px 2.5px 2.5px 0px. Internal content uses Deep Slate and Open Runde.

### Service Feature List Item

**Role:** Highlighting key features below hero

Text: Open Runde, weight 400, color Graphite (#0f0f0f). Icon: Sky Teal (#0098f2) vector checkmark. Uses 6px elementGap between icon and text.

### Small Header Button

**Role:** Secondary action in header, e.g. 'Log in'

Text: Open Runde, weight 400, color Ash Gray (#8d8d8d). No background or border, relies on text color to differentiate from primary button.

### Sign Up Header Button

**Role:** Primary action in header

Text: Open Runde, weight 400, color Canvas White (#ffffff). Background: Button Black (#0d111b). Radius: 100px. Padding: 6px vertical, 14px horizontal.

## Layout

The page model is a max-width 1200px centered container, creating generous margins on larger screens. The hero section features a centered headline over a white background, flanked by decorative product photography. Sections primarily follow a consistent vertical rhythm with ample spacing (40-80px), often presenting content as centered stacks or simple two-column layouts. A subtle alternating background color ('Subtle Cream') differentiates some sections. Navigation is a fixed top bar with a logo, standard links, and 'Log in'/'Sign up' buttons. Content density is comfortable, prioritizing readability and visual breathing room.

## Imagery

The site uses a mix of tightly cropped, top-down product photography featuring office supplies (paperclips, pens, keyboard snippets) on a clean white desk, serving as decorative framing for UI elements. Embedded product screenshots show the invoice interface directly, providing clear, functional examples of the software. Icons are primarily line-based, monochrome (Ink Black), with occasional small fills of the brand's accent colors (Sky Teal, Hot Pink, Vivid Violet). The imagery's role is decorative atmosphere, combined with direct product showcasing to explain functionality, rather than lifestyle or abstract concepts.

## Dos & Donts

### Do

- Use '#ffffff' Canvas White as the dominant page and card background color; establish visual hierarchy through subtle shade differences like '#f7fafc' for alternating sections.
- Apply Open Runde for all main headings and body text, varying weights (400, 500, 600) and sizes according to the type scale for clear hierarchy and visual appeal.
- Employ the 100px radius for all interactive buttons and pill-shaped elements to maintain a consistent soft, approachable shape.
- Utilize Sky Teal (#0098f2) exclusively for interactive elements like links and checkmarks; avoid using it for decorative purposes to preserve its accent meaning.
- Maintain generous padding, particularly 24px and 40px for section separators and major element spacing, creating a spacious and comfortable information density.
- When emphasizing short, impactful statements or testimonials, use the Caveat font for a personalized, handwritten touch.

### Don't

- Do not introduce new chromatic colors beyond Sky Teal, Hot Pink, and Vivid Violet; the design strictly adheres to an achromatic base with minimal, deliberate color accents.
- Avoid box shadows for general elevation; leverage subtle background color changes (e.g., #f7fafc) or thin borders to suggest depth and separation.
- Do not use system sans-serif for headlines or prominent body text; reserve it for small, functional UI labels where its simplicity is an asset.
- Do not deviate from the established letter-spacing values for Open Runde; these are carefully calibrated for optimal legibility at different sizes.
- Avoid dense, information-heavy sections without adequate whitespace; the design's strength lies in its spacious and clear presentation.
- Do not use multiple font sizes or weights within a single line of text unless it's a clearly defined component. Maintain typographic consistency.

## Notes

### Agent Prompt Guide

## Quick Color Reference
- Text: #000000 Ink Black
- Background: #ffffff Canvas White
- CTA Background: #0d111b Button Black
- CTA Text: #ffffff Canvas White
- Links/Accent: #0098f2 Sky Teal
- Secondary Text/Disabled: #8d8d8d Ash Gray

## 3-5 Example Component Prompts
1. Create a hero section: 'Canvas White' background. Headline 'Invoices that work harder' using 'display' role of Open Runde, weight 600, color '#000000' Ink Black, letter-spacing -2.368px. Subtext 'Acctual offers more payment methods and no monthly fees...' at 'body' role Open Runde, weight 400, color '#1e1e1' Deep Slate. Primary button 'Create an invoice': background '#0d111b' Button Black, text '#ffffff' Canvas White, 100px radius, 6px 14px padding. Below the button, add three feature indicators: 'Sky Teal' checkmark icon, followed by text 'Same day payouts' using 'body-sm' role Open Runde, weight 400, color '#0f0f0f' Graphite.
2. Design a Testimonial Card: 'Canvas White' background, 20px radius, rgba(0, 0, 0, 0.06) 0px 2.5px 2.5px 0px shadow. Headline text '"Acctual makes invoices so easy."' using 'heading' role of Open Runde, weight 600, color '#000000' Ink Black. Attributed text 'Marcel Kargul, founder of Kargul Studio' using 'body' role of Caveat, weight 600, color '#8d8d8d' Ash Gray.
3. Implement a Navigation Bar: 'Canvas White' background. Logo at left. Navigation links 'Invoicing', 'Pricing', 'Blog', 'Guides' using Open Runde, weight 400, color '#8d8d8d' Ash Gray for inactive and '#000000' Ink Black for active/hover. 'Log in' button as text-only, Open Runde, weight 400, color '#8d8d8d' Ash Gray. 'Sign up for free' button: background '#0d111b' Button Black, text '#ffffff' Canvas White, 100px radius, 6px 14px padding.

---
_Source: https://styles.refero.design/style/aeefc294-a8f7-443d-b76a-538dddc29afe_
