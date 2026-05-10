# Officevibe — Design Reference

> Electric Data Flow; a structured, approachable canvas where sharp insights meet soft edges.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://officevibe.com](https://officevibe.com) |
| Refero page | [https://styles.refero.design/style/ced1c98f-d489-48f7-a01f-1fa59a07b706](https://styles.refero.design/style/ced1c98f-d489-48f7-a01f-1fa59a07b706) |
| Theme | light |
| Industry | saas |

## Overview

This design system feels like a modern corporate solution, balancing trust with approachability. A deep, almost inky violet (`Boardroom Navy`) provides a stable foundation, contrasted by a vibrant, electric blue (`Brand Electric`) that visually energizes interactive elements. The interplay between the sans-serif clarity of Inter and the distinctive, slightly calligraphic AbcFavoritvariable for headings, alongside the unexpected Martinaplantijn script for accent headlines, creates a sophisticated yet human touch. The prevalent use of generous rounded corners (100px for buttons) against sharp content blocks softens the enterprise feel, making complex data feel more accessible.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Boardroom Navy | `#0c1754` | brand | Primary text, prominent headings, badge text, footer background — establishes a serious, trustworthy brand presence. |
| Brand Electric | `#2545ff` | brand | Primary call-to-action buttons, active navigation indicators, key interactive links — provides a vibrant, unmistakable focus for user interaction. |
| Lilac Accent | `#d9d4ff` | accent | Subtle background accents, decorative elements — a muted counterpoint to Brand Electric, adding depth without visual noise. |
| Feedback Yellow | `#ffc13a` | accent | Accents, potentially informational highlights or status indicators — a warm, vivid pop of color. |
| Soft Off-White | `#f9f8f6` | neutral | Page backgrounds, card surfaces, form inputs — the dominant background neutral, providing a soft, canvas-like base. |
| Pure White | `#ffffff` | neutral | Focal backgrounds, cards, button text on Brand Electric — used for highlighted foreground elements. |
| Pitch Black | `#171417` | neutral | Dominant text color for body, links, some headings — provides high contrast against light backgrounds. |
| Medium Gray | `#222222` | neutral | Secondary text, navigation items, ghost button text — a softer dark alternative for less prominent text. |
| Light Cool Gray | `#eaebf8` | neutral | Badge backgrounds, subtle borders — a very light, desaturated violet-gray for secondary background elements. |
| Input Border Gray | `#cccccc` | neutral | Input field borders — a mid-tone gray for subtle visual separation of form elements. |
| Accent Orange | `#ff5b22` | accent | Used as a stroke in illustrations, possibly for emphasis or minor error states — provides a sharp, energetic contrast. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 12px, 14px, 16px, 20px |
| lineHeight | 1.10, 1.20, 1.40, 1.43, 1.60 |
| letterSpacing | normal |
| fontFeatureSettings | "ss01" on, "ss04" |
| substitute | system-ui, sans-serif |
| role | Body text, navigation, buttons, lists, captions — the primary functional typeface, providing straightforward readability. |

### Abcfavoritvariable

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 12px, 14px, 16px, 20px, 24px, 32px, 40px, 48px, 64px |
| lineHeight | 1.00, 1.10, 1.40, 1.50 |
| letterSpacing | -0.0500em at 40-64px, -0.0200em at 24-32px |
| fontFeatureSettings | "ss01" on, "ss04" |
| substitute | Arial, sans-serif |
| role | Main headings, subheadings, badge text — imparts a distinctive, slightly geometric character to titles, differentiating them from pure system fonts. |

### Martinaplantijn

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 40px, 64px |
| lineHeight | 1.00, 1.10 |
| letterSpacing | normal |
| fontFeatureSettings | "ss01" on, "ss04" |
| substitute | cursive |
| role | Emphatic headline accents — used sparingly to highlight key phrases within larger headings, adding a unique, almost handwritten elegance. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.4 |  |
| body-sm | 14 |  | 1.43 |  |
| body | 16 |  | 1.6 |  |
| subheading | 20 |  | 1.4 |  |
| heading-sm | 24 |  | 1.1 | -0.48 |
| heading | 32 |  | 1.1 | -0.64 |
| heading-lg | 40 |  | 1.1 | -2 |
| display | 48 |  | 1.1 | -2.4 |
| display-lg | 64 |  | 1 | -3.2 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| badges | 16px |
| inputs | 0px |
| buttons | 100px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### Primary & Ghost Button Group

### AI Chat Widget

### FAQ Accordion

### Primary CTA Button

**Role:** Main interactive button

Solid Brand Electric (#2545ff) background with Pure White (#ffffff) text. Features a generous 100px border-radius, 11.2px vertical and 32px horizontal padding. Font: Inter, 500 weight.

### Ghost Button (Primary Brand)

**Role:** Secondary call-to-action

Transparent background with Brand Electric (#2545ff) text and 1px border. 100px border-radius, 11.2px vertical and 32px horizontal padding. Font: Inter, 500 weight.

### Ghost Button (Dark Neutral)

**Role:** Navigation or less prominent actions

Transparent background with Medium Gray (#222222) text and 1px border. 0px border-radius, 18px vertical and 16px horizontal padding. Font: Inter, 500 weight.

### Ghost Button (Light Neutral)

**Role:** Navigation or less prominent actions on dark backgrounds

Transparent background with Pure White (#ffffff) text and 1px border. 100px border-radius, 11.2px vertical and 32px horizontal padding. Font: Inter, 500 weight.

### Feature Card

**Role:** Content display block

backgroundColor=rgba(0, 0, 0, 0) with no box-shadow, 16px border-radius. Padding: 40px vertical, 80px horizontal. Typically uses the Soft Off-White (#f9f8f6) background if not implicitly transparent relative to the page.

### Informative Badge

**Role:** Categorization or status indicator

Light Cool Gray (#eaebf8) background with Boardroom Navy (#0c1754) text. 16px border-radius, 4px top/bottom, 12px left/right padding. Font: Inter, 400 weight, 14px.

### Input Field (Footer)

**Role:** Footer email subscription input

Transparent background, Pure White (#ffffff) text. Bottom border is Input Border Gray (#cccccc), 1px solid. 0px border-radius, 12px vertical and 0px left padding. Placeholder text is lighter gray.

## Layout

The page adheres to a mostly max-width contained model, centered on the screen, with sections flowing vertically. The hero section is a split-screen pattern, featuring a large headline and CTA on the left, balanced by a prominent product UI illustration on the right. Content sections generally maintain a consistent vertical rhythm of 64px spacing, often alternating between text-left/image-right and image-left/text-right arrangements. The footer is full-bleed with Boardroom Navy. Input fields are subtle, seamlessly integrated into the design. Card grids are not explicitly present, but data visualizations within product screenshots suggest a structured arrangement of content. The navigation is a classic top bar with a primary CTA button, sticky on scroll.

## Imagery

Imagery primarily consists of highly stylized, almost abstract product screenshots and UI diagrams, often featuring gradients and subtle 3D depth, contained within clean, rounded-corner elements, or floating freely. Data visualizations are a key visual element. There's a notable absence of lifestyle photography. Icons are filled, modern, and typically monochromatic, using either Pitch Black or occasionally Brand Electric. The visuals serve to explain complex concepts and data flow, acting as both decorative and explanatory content, occupying significant visual space in harmonious asymmetry.

## Dos & Donts

### Do

- Do use Brand Electric (#2545ff) exclusively for primary interactive elements like main CTA buttons and active navigation states.
- Do ensure all buttons employ a 100px border-radius for their signature pill shape, unless specifically a ghost button variant.
- Do use Abcfavoritvariable for prominent headings, applying negative letter-spacing for larger sizes (-0.05em at 40px and above).
- Do employ Soft Off-White (#f9f8f6) as the primary page background color to establish a soft, approachable base.
- Do ensure body text uses Inter 400 at 16px, in Pitch Black (#171417) for maximal readability against light backgrounds.
- Do use Martinaplantijn 400 for accentuating specific words or phrases within larger headings, providing a unique calligraphic touch.
- Do maintain a consistent vertical spacing of 64px between major sections on the page.

### Don't

- Don't use Brand Electric (#2545ff) for static text or non-interactive decorative elements.
- Don't introduce additional curved shapes; large 100px radii for buttons and 16px for cards are the system's defined roundedness.
- Don't use bold weights for Inter in headlines; only Abcfavoritvariable or Martinaplantijn should carry headline prominence.
- Don't embed images with strong, distracting background colors; favor clean product shots or highly stylized UI illustrations.
- Don't use hard shadows for elevation; rely on background color changes or subtle outlines for depth.
- Don't clutter layouts; prioritize generous white space with a base element gap of 8px and larger gaps for content sections.
- Don't use any other blues for brand elements other than Boardroom Navy (#0c1754) and Brand Electric (#2545ff).

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): #171417
- Background (Page): #f9f8f6
- CTA Button: #2545ff
- Accent (Lilac): #d9d4ff
- Border (Input): #cccccc

### 3-5 Example Component Prompts
1. Create a hero section: Soft Off-White background (#f9f8f6). Left side: Headline 'You can't fix issues if you're not paying attention'. Use Abcfavoritvariable 64px, weight 500, Pitch Black (#171417), letter-spacing -3.2px, with 'not paying attention' in Martinaplantijn 64px, weight 400. Subtext 'Catch issues sooner...' in Inter 16px, weight 400, Pitch Black (#171417). Button 'Request a demo': Primary CTA Button style. Right side: Placeholder for product UI illustration (product screenshots graphic should be prominent, possibly with a subtle gradient mask, contained within a rounded 16px border-radius shape).
2. Generate a 'Request a demo' Primary CTA Button: Background Brand Electric (#2545ff), text Pure White (#ffffff), 100px border-radius, 11.2px vertical padding, 32px horizontal padding. Font: Inter, 500 weight, 16px.
3. Design an Informative Badge: Background Light Cool Gray (#eaebf8), text Boardroom Navy (#0c1754), 16px border-radius, 4px top/bottom, 12px left/right padding. Font: Inter, 400 weight, 14px.
4. Produce a footer input field: Transparent background, Pure White text (#ffffff), 1px solid bottom border in Input Border Gray (#cccccc), 0px border-radius, 12px top/bottom padding, 0px left padding. Placeholder text should be a lighter grey.

---
_Source: https://styles.refero.design/style/ced1c98f-d489-48f7-a01f-1fa59a07b706_
