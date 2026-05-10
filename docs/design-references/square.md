# Square — Design Reference

> Crisp Utility, Intuitive Flow. The UI emphasizes clarity and direct interaction, like operating a well-designed tool.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://squareup.com](https://squareup.com) |
| Refero page | [https://styles.refero.design/style/86a6814d-2485-4fad-b6fd-56c2d0a23620](https://styles.refero.design/style/86a6814d-2485-4fad-b6fd-56c2d0a23620) |
| Theme | light |
| Industry | fintech |

## Overview

Square's design system evokes the precision of a finely tuned instrument, balancing robust functionality with an accessible aesthetic. The interplay of a predominantly white background with crisp dark typography creates a high-contrast, information-first experience that feels direct and trustworthy. Strategic use of a vibrant blue accent color serves as a clear functional indicator, like a status light on a reliable machine, guiding the user without visual clutter, while subtle rounded corners on interactive elements soften the overall technical impression.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Sky Blue | `#006aff` | brand | Call to action buttons, active navigation indicators, key functional links — a punchy visual cue for interactivity. |
| Ink Black | `#1a1a1a` | neutral | Primary text color for hero headlines, major body text, and prominent UI elements. High contrast ensures immediate legibility. |
| Graphite | `#737373` | neutral | Secondary text, placeholder text in inputs, subtle borders. Provides hierarchy without stark contrast. |
| Pure White | `#ffffff` | neutral | Page backgrounds, card surfaces, button backgrounds in secondary states. Establishes a clean, expansive canvas. |
| Deep Black | `#030303` | neutral | Footer background, occasional high-contrast text elements. Creates strong visual breaks. |
| Light Fog | `#f2f2f2` | neutral | Alternate section backgrounds, subtle dividers, inactive states. Adds soft visual separation. |
| Ash Gray | `#cccccc` | neutral | Thin borders on inactive components, dividers. Provides structure without visual weight. |
| Silver Mist | `#d9d9d9` | neutral | Shadow accents on buttons, subtle boundary lines. Hints at subtle elevation. |
| Stone Gray | `#b3b3b3` | neutral | Supplemental body text, very light borders. Softens text where less emphasis is needed. |

## Typography

### Square Sans Display VF

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 16px, 20px, 24px, 32px, 50px, 62px, 86px |
| lineHeight | 0.97, 1.10, 1.12, 1.19, 1.25, 1.30, 1.33, 1.42, 1.50 |
| letterSpacing | -0.0120em |
| substitute | Inter |
| role | Used for all primary headings and prominent display text. The tight letter-spacing at larger sizes gives a visually compact, impactful feel, while the moderate weights convey authority without being overbearing. |

### Square Sans Text VF

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 12px, 14px, 16px |
| lineHeight | 1.38, 1.43, 1.50, 1.71, 1.75, 2.00, 2.63 |
| letterSpacing | normal |
| substitute | Inter |
| role | This family is used for all body text, navigation links, and smaller UI elements. Its slightly wider proportions and normal letter spacing optimize for readability at smaller scales, ensuring clarity in dense information blocks. |

### Cash Sans

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 18px |
| lineHeight | 1.00, 1.50 |
| letterSpacing | normal |
| substitute | IBM Plex Mono |
| role | A distinctive, fixed-width font used sparingly for button text and specific textual accents, adding a subtle touch of technical precision and differentiating interactive elements with its unique character. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 2 |  |
| body-sm | 14 |  | 1.71 |  |
| body | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.3 |  |
| heading-sm | 24 |  | 1.25 |  |
| heading | 32 |  | 1.19 |  |
| heading-lg | 50 |  | 1.12 | -0.5 |
| display | 62 |  | 1.1 | -0.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| none | 0px |
| large | 20px |
| small | 4px |
| medium | 5px |
| circular | 32px |
| extraLarge | 24px |

- **elementGap** — 10-20px
- **sectionGap** — 48px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Button Group

### Feature Cards

### Email Signup Form

### Primary Action Button

**Role:** Main call to action.

Background: Sky Blue (#006aff), Text: Pure White (#ffffff), Border: None, Radius: 5px, Padding: 13px vertical, 20px horizontal. Emphasizes primary user flow.

### Secondary Outline Button

**Role:** Secondary calls to action, navigation links.

Background: transparent (rgba(0, 0, 0, 0)), Text: Ink Black (#1a1a1a), Border: 1px solid Ink Black (#1a1a1a), Radius: 4px, Padding: 18px vertical, 10px horizontal. Offers interaction without demanding primary attention.

### Ghost Button

**Role:** Minimal interactive elements, textual links as buttons.

Background: transparent (rgba(0, 0, 0, 0)), Text: Ink Black (#1a1a1a), Border: 1px solid Ink Black (#1a1a1a), Radius: 0px, Padding: 0px. Used for sub-navigation or less prominent actions.

### Pill Button

**Role:** Categorization, filters, or internal navigation. Not used for primary actions.

Background: transparent (rgba(0, 0, 0, 0)), Text: Ink Black (#1a1a1a), Border: 1px solid Ink Black (#1a1a1a), Radius: 20px, Padding: 0px. The rounded shape sets it apart as a navigational or categorical element.

### Information Card (Light Fog)

**Role:** Showcasing features or information blocks.

Background: Light Fog (#f2f2f2), Border: None, Radius: 0px, Padding: 20px (inferred, based on sectionGap) . Provides a subtle break from the main white background, grouping related content.

### Information Card (Pure White)

**Role:** Emphasized feature cards or content blocks.

Background: Pure White (#ffffff), Border: None, Radius: 0px, Padding: 20px (inferred, based on sectionGap). Maintains the primary visual language while allowing for flexible content display.

### Text Input Field

**Role:** User input for forms.

Background: Pure White (#ffffff), Text: Ink Black (#1a1a1a), Placeholder: Graphite (#737373), Border: 1px solid Ink Black (#1a1a1a), Radius: 5px, Padding: 11px vertical, 17px horizontal. Clear visual affordance for data entry.

## Layout

The page primarily uses a max-width centered layout, containing content within a clear reading area against a continuous white background for most sections. The hero section breaks this slightly by using a two-column split: a left-aligned headline and CTA on a white canvas, paired with a right-aligned full-height photographic element. Sections alternate visually between Pure White and Light Fog (#f2f2f2) backgrounds, creating a clear vertical rhythm without hard dividers. Content is often arranged in symmetrical multi-column grids (typically 3-column for feature showcases) or left-aligned text blocks with associated visual content. Breathing room between sections is generous and consistent. The site uses a sticky top navigation bar with a centered logo and right-aligned action buttons, maintaining constant accessibility.

## Imagery

The visual language focuses on a mix of authentic, often close-up photography of people interacting with Square's physical payment devices, juxtaposed with clean digital product screenshots showing the software's interface. Photography is grounded and real, often featuring hands, creating a sense of accessibility and tangibility. Product screenshots are contained within device mockups or simple rectangular masks, emphasizing clarity of the UI. Icons are simple, filled, and monochromatic, primarily Ink Black or Pure White, serving an explanatory rather than decorative role. Imagery is always contained, never full-bleed, and generally in a 16:9 or similar aspect ratio, serving to illustrate features or user benefits rather than purely aesthetic embellishment. There is a strong absence of abstract or illustrative graphics, reinforcing a practical, business-focused approach.

## Dos & Donts

### Do

- Use Sky Blue (#006aff) exclusively for primary calls to action and active interactive states.
- Maintain a clear hierarchy with Square Sans Display VF for headlines and Square Sans Text VF for body copy.
- Always apply a 5px radius to primary buttons and input fields for a consistent tactile feel.
- Utilize Pure White (#ffffff) as the default background for content sections to maximize readability.
- Employ Light Fog (#f2f2f2) to subtly differentiate background sections without introducing strong visual breaks. It acts as a soft divider.
- Ensure input fields have a 1px solid Ink Black (#1a1a1a) border to clearly delineate the interactive area.

### Don't

- Do not use Sky Blue (#006aff) for decorative elements or non-interactive text.
- Avoid using Cash Sans for long passages of text; reserve it for specific button labels and distinct accents.
- Do not introduce strong visual shadows on cards or elements; rely on background color changes for depth.
- Do not use highly saturated colors other than Sky Blue; maintain a largely monochromatic palette with blue as the sole accent.
- Avoid arbitrary border radii; stick to 0px, 4px, 5px, 20px, 24px, or 32px for specific component types.
- Do not use highly decorative or script fonts; maintain a modern, sans-serif aesthetic throughout.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: Ink Black (#1a1a1a)
- Background: Pure White (#ffffff)
- CTA: Sky Blue (#006aff)
- Border: Ink Black (#1a1a1a)
- Secondary Background: Light Fog (#f2f2f2)

### Example Component Prompts
1. Create a Hero Section: Pure White (#ffffff) background. Left-aligned headline: 'Impulsa tu negocio con Square' at 62px Square Sans Display VF, weight 500, #1a1a1a, letter-spacing -0.6px, lineHeight 1.1. Subtext at 16px Square Sans Text VF, weight 400, #1a1a1a, lineHeight 1.5. Below, add a Primary Action Button: 'Empezar', Sky Blue (#006aff) background, Pure White (#ffffff) text, 5px radius, 13px vertical, 20px horizontal padding. Next to it, a Secondary Outline Button: 'Contactar con el equipo de Ventas', transparent background, Ink Black (#1a1a1a) text, 1px solid Ink Black border, 4px radius, 18px vertical, 10px horizontal padding.
2. Design a Feature Card: Light Fog (#f2f2f2) background, 0px radius, 20px padding. Headline: 'Impulsa tu negocio en línea' at 24px Square Sans Display VF, weight 500, #1a1a1a, letter-spacing -0.0120em, lineHeight 1.25. Body text at 16px Square Sans Text VF, weight 400, #1a1a1a, lineHeight 1.5. Link text 'Descubre todas las opciones para vender en línea →' in Sky Blue (#006aff), 16px Square Sans Text VF, weight 400.
3. Build a Footer Section: Deep Black (#030303) background, Pure White (#ffffff) text. Include an email input field: Pure White (#ffffff) background, 1px solid Ink Black (#1a1a1a) border, 5px radius, 11px vertical, 17px horizontal padding, placeholder text in Graphite (#737373).

---
_Source: https://styles.refero.design/style/86a6814d-2485-4fad-b6fd-56c2d0a23620_
