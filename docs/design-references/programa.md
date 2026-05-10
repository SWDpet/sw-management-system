# Programa — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://programa.design](https://programa.design) |
| Refero page | [https://styles.refero.design/style/41af8353-6a8f-416d-947b-57932f591497](https://styles.refero.design/style/41af8353-6a8f-416d-947b-57932f591497) |
| Theme | light |
| Industry | design |

## Overview

Programa employs a refined white-canvas aesthetic with crisp, confident typography as its primary visual communicator. A single vibrant yellow accent color is reserved exclusively for key calls to action, creating high contrast and immediate focus against the otherwise monochrome interface. Components are lightweight and unobtrusive, featuring thin borders and soft rounded corners, allowing content and functionality to take precedence without visual noise.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Storm Graphite | `#1a1a1a` | neutral | Primary text, heading text, neutral button backgrounds, active states, dark borders |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card backgrounds, inverse text for dark elements, light borders |
| Ash Gray | `#a3a3a3` | neutral | Muted secondary text, helper text, inactive link text, subtle borders |
| Midas Yellow | `#fbff2b` | brand | Primary call-to-action buttons, interactive highlights, brand accent — signals key actions and captures attention against neutrals |

## Typography

### neueHaasGroteskText

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 14px, 16px, 17px, 20px, 24px, 42px |
| lineHeight | 1.10, 1.20, 1.40 |
| letterSpacing | -0.03 |
| substitute | Helvetica Neue |
| role | Primary typeface for all UI elements, headings, body text, and interactive components. Its precision and varied weights establish a clear informational hierarchy. |

### neue-haas-grotesk-text

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 20px |
| lineHeight | 1.4 |
| role | neue-haas-grotesk-text — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.4 | -0.42 |
| body | 16 |  | 1.4 | -0.48 |
| subheading | 20 |  | 1.4 | -0.6 |
| heading | 24 |  | 1.2 | -0.72 |
| display | 42 |  | 1.1 | -1.26 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| inputs | 10px |
| buttons | 10px |
| navigation | 10px |

- **elementGap** — 12px
- **sectionGap** — 96px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Components

### Neutral Ghost Button

**Role:** Secondary action or navigation item

Transparent background, Storm Graphite text (#1a1a1a), 10px border radius, 8px vertical and 12px horizontal padding. Borders are implied by context, not explicitly rendered for a 'ghost' effect.

### Primary Action Button

**Role:** Main call to action

Midas Yellow background (#fbff2b), Storm Graphite text (#1a1a1a), 12px border radius, 0px vertical and 16px horizontal padding. Highly visible and contrasts sharply with neutral backgrounds.

### Dark Filled Button

**Role:** Alternative primary action or prominent secondary action on light backgrounds

Storm Graphite background (#1a1a1a), Canvas White text (#ffffff), 12px border radius, 0px vertical and 16px horizontal padding. Provides a strong alternative to the Midas Yellow.

### Light Input Field

**Role:** Standard input field on dark backgrounds

Background rgba(255, 255, 255, 0.2), Canvas White text (#ffffff), 12px border radius, 6px vertical and 12px horizontal padding. Has a subtle transparent fill and light border.

### Dark Input Field

**Role:** Standard input field on light backgrounds

Background rgba(26, 26, 26, 0.05), Storm Graphite text (#1a1a1a), 12px border radius, 6px vertical and 12px horizontal padding. Features a very subtle dark transparent fill.

## Layout

The page maintains a centered, contained layout for its main content, framed by responsive margins. The header is a sticky top bar with minimal navigation. Sections flow vertically with a consistent rhythm of 'comfortable' spacing and some sections having a larger 96px bottom padding. Content is primarily text-based, often presented in clear, readable stacks, with interactive elements like buttons and input fields seamlessly integrated rather than heavily framed. A primary content column often has left and right margins of 111px, or 236px for narrower sections, creating a focused reading area. There are no clear alternating color bands or complex grid structures beyond simple content blocks.

## Imagery

This site predominantly uses UI-focused content rather than decorative imagery. Where visible (e.g., product screenshots), these are typically clean, direct captures of the software interface, presented without heavy styling or complex compositions. Icons, if present, tend to be minimal outline styles, supporting functionality rather than aesthetic flourish. The visual density is text-dominant, allowing the typography and clean layout to convey information directly.

## Dos & Donts

### Do

- Use neueHaasGroteskText-500 for all headings to maintain concise visual impact.
- Apply Midas Yellow (#fbff2b) strictly for primary call-to-action buttons or critical interactive highlights.
- Ensure input fields use 6px vertical and 12px horizontal padding, with a 12px border radius.
- Maintain a default letter-spacing of -0.03em for all text, conveying a tightly set, modern feel.
- Utilize Canvas White (#ffffff) as the dominant page and card background for an expansive, clean canvas.
- Reserve Storm Graphite (#1a1a1a) for primary text, ensuring strong contrast against light backgrounds.
- Implement 10px radius for all buttons and interactive navigation elements for a consistent soft-rounded touch.

### Don't

- Do not introduce new saturated colors; adhere to the Midas Yellow for accents and brand identity.
- Avoid heavy shadows or gradients; rely on minimal borders and background color changes for surface differentiation.
- Do not use letter-spacing other than the specified -0.03em or normal, to preserve typographic rhythm.
- Never use Ash Gray (#a3a3a3) for primary action text or backgrounds; it's reserved for muted elements.
- Do not use font weights other than 400 or 500 for neueHaasGroteskText; these are the only available weights for the system.
- Avoid arbitrary padding or margin values; stick to multiples of the 6px base unit or specified element/section gaps.
- Do not use large, decorative imagery; visual content should be contained and functional to support the UI-first approach.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #1a1a1a
background: #ffffff
border: #a3a3a3
accent: #fbff2b
primary action: #fbff2b (filled action)

Example Component Prompts:
1. Create a primary call-to-action button: Midas Yellow background (#fbff2b), Storm Graphite text (#1a1a1a), neueHaasGroteskText-400, 12px border radius, 0px vertical and 16px horizontal padding.
2. Design a standard form input field for a light background: Background rgba(26, 26, 26, 0.05), Storm Graphite text (#1a1a1a), neueHaasGroteskText-400, 12px border radius, 6px vertical and 12px horizontal padding.
3. Create a navigation link: Storm Graphite text (#1a1a1a), neueHaasGroteskText-400, no background. On hover, ensure text remains #1a1a1a.

---
_Source: https://styles.refero.design/style/41af8353-6a8f-416d-947b-57932f591497_
