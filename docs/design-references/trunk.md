# Trunk — Design Reference

> Midnight command center, energetic pulse.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://trunk.io](https://trunk.io) |
| Refero page | [https://styles.refero.design/style/48971df7-919d-453c-9d0b-4600cd24c583](https://styles.refero.design/style/48971df7-919d-453c-9d0b-4600cd24c583) |
| Theme | dark |
| Industry | devtools |

## Overview

Trunk employs a 'digital engineering blueprint' aesthetic: a dark, technical canvas dominated by deep grays and blacks, accented by circuit-like line art and subtle energetic color highlights. Typography is concise and functional, emphasizing density and clarity within constrained blocks. Components feature soft, rounded edges and minimal elevation, promoting a lightweight and efficient user experience typical of development tooling.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Carbon | `#08090b` | neutral | Primary surface background, card backgrounds, dark text on light elements |
| Obsidian | `#000000` | neutral | Page background, footer background, deepest dark elements |
| Frost | `#ffffff` | neutral | Primary text, button backgrounds, interactive elements on dark surfaces |
| Steel Gray | `#232323` | neutral | Hover states on cards, subtle borders, slightly elevated surface backgrounds |
| Outline Gray | `#e2e8f0` | neutral | Primary light borders, dividers, subtle inactive element strokes |
| Muted Silver | `#b3b3b5` | neutral | Secondary text, descriptive body copy, muted icons |
| Ghost Gray | `#c1c5c8` | neutral | Tertiary text, helper text, subtle inactive states |
| Whisper White | `#dddddd` | neutral | Least prominent text, legal copy, very light accent details |
| Slate Shadow | `#89898d` | neutral | Subtler background accents, visual texture |
| Vivid Blue | `#2f6eeb` | brand | Violet text accent for links, tags, and emphasized short phrases |
| Verdant Green | `#62c772` | brand | Green text accent for links, tags, and emphasized short phrases |
| Flame Orange | `#ec592e` | brand | Orange text accent for links, tags, and emphasized short phrases |

## Typography

### neue

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| weights | 300, 400, 500, 700 |
| sizes | 11px, 12px, 14px, 15px, 16px, 17px, 18px, 24px, 36px, 45px, 56px, 60px |
| lineHeight | 0.94, 1.00, 1.18, 1.20, 1.25, 1.30, 1.40, 1.50, 1.60 |
| letterSpacing | -0.0280em at 60px, -0.0260em at 56px, -0.0180em at 45px, -0.0130em at 36px, -0.0090em at 24px and below |
| substitute | system-ui, sans-serif |
| role | Primary typeface for all text elements. Features a broad range of weights and compact line heights to allow for dense, yet readable information display, fitting a developer-focused tool. Tighter letter spacing on larger headings creates a refined, intentional feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.5 | 0 |
| body | 14 |  | 1.5 | 0 |
| subheading | 18 |  | 1.3 | -0.16 |
| heading | 24 |  | 1.25 | -0.21 |
| heading-lg | 36 |  | 1.2 | -0.47 |
| display | 56 |  | 0.94 | -1.46 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| buttons | 18px |
| heroAccent | 999px |
| interactiveElements | 12px |

- **elementGap** — 12px
- **sectionGap** — 48px
- **cardPadding** — 18px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Obsidian Canvas | `#000000` | 0 | Base page background, deepest interactive zones. |
| Carbon Surface | `#08090b` | 1 | Primary component backgrounds like cards, default dark container background. |
| Steel Gray Component | `#232323` | 2 | Subtle interactive element backgrounds, specific hovered states, background for circular buttons. |
| Slate Shadow Accent | `#89898d` | 3 | Minor accent element backgrounds, subtle visual anchors. |
| Frost Hover | `#ffffff` | 4 | Primary button fill on dark, elevated hover states for interactive elements. |

## Components

### Primary Action Button

**Role:** Main call to action

Filled button with Frost background (#ffffff), Carbon text (#08090b), and 18px border radius. Padding is effectively 9px vertical, 12px horizontal after internal element spacing. Used for 'Book a demo'.

### Ghost Secondary Button

**Role:** Secondary action or navigation

Ghost button with transparent background, Carbon text (#08090b), and 0px border radius. Padding is 9px vertical, 12px horizontal. Used for navigation links within the main menu.

### Dark Filled Accent Button

**Role:** Contextual action on dark surfaces

Filled button with Carbon background (#08090b), Frost text (#ffffff), and 18px border radius. Padding is effectively 9px vertical, 12px horizontal. Used for 'Book a demo' in hero.

### Circular Icon Button

**Role:** Navigation or small interactive elements

Small circular button with Steel Gray background (#232323), Frost icon/text (#ffffff), and 50% border radius. Minimal padding, designed to be a compact visual element.

### Elevated Content Card

**Role:** Displaying features or testimonials with visual separation

Card with Carbon background (#08090b) and 24px border radius. Internal padding is generous at 36px vertical, 18px horizontal. No explicit shadow, relies on background contrast for definition.

### Testimonial Card

**Role:** Displaying short quotes or partner logos

Card with transparent background and 18px border radius. Internal padding 24px all around. Used for testimonial cards with a subtle dark border.

### Text Input Field

**Role:** Collecting user input

Transparent background input with Frost text (#ffffff). Border is an Outline Gray (#e2e8f0) bottom border, or fully bordered with same color. Padding is 0px vertical, 14.25px horizontal. No explicit border radius on main input.

## Layout

The page uses a contained layout with some full-bleed sections, maintaining an overall max-width although a specific `pageMaxWidth` value is not provided, the content remains centrally aligned with ample side margins. The hero section is full-bleed, dark, featuring a centered headline and subtle background line art. Content sections alternate between visually distinct areas (e.g., darker backgrounds with product screenshots, then lighter-on-dark content with testimonial cards). Most content blocks follow a stacked or two-column text-right/image-left pattern. Navigation is a sticky top bar with a left-aligned logo and right-aligned links and buttons. Card grids, particularly for testimonials, are present and appear to be 3 or 4 columns.

## Imagery

The imagery consists of technical line-art illustrations depicting abstract infrastructure, possibly showing CI/CD pipelines or network flows, rendered in white on dark backgrounds. These graphics are contained within sections and serve a decorative and atmospheric purpose, hinting at the product's technical nature without literal depictions. The imagery reinforces a 'blueprint' or 'schematic' feel. Icons are monochrome outlined or filled, with a consistent stroke weight, and typically appear alongside text for clarity.

## Dos & Donts

### Do

- Prioritize Carbon (#08090b) or Obsidian (#000000) for all primary backgrounds and surfaces.
- Use Frost (#ffffff) for primary text on dark backgrounds and for primary button fills.
- Apply 18px border radius to all interactive buttons and 24px to main content cards.
- Utilize interne bold for headlines and body text to ensure high legibility and presence.
- Employ Verdant Green (#62c772), Vivid Blue (#2f6eeb), and Flame Orange (#ec592e) sparingly, primarily for accenting keywords or conveying status, not for general UI elements.
- Maintain tight letter spacing, especially for larger text sizes, as specified in the typography section, to enhance visual density.
- Divide content sections with generous vertical spacing (48px default) to create clear reading rhythm, even on dark backgrounds.

### Don't

- Avoid using bright, saturated colors for backgrounds or large areas; maintain the dark, muted aesthetic.
- Do not introduce strong shadows or excessive elevation; rely on background color contrast and subtle borders for depth.
- Refrain from using overly decorative fonts or varying typefaces; stick to the specified 'neue' family to maintain consistency.
- Do not use small border radii (under 12px) for primary elements; soft rounded corners are a key identifier.
- Avoid generic icon styles; prefer outlined, mono-color icons with a technical aesthetic.
- Do not use Verdant Green, Vivid Blue, or Flame Orange as primary button background colors; these are accent colors for text and small indicators only.
- Do not center-align large blocks of body text; left-alignment is preferred for readability.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #000000
border: #e2e8f0
accent: #62c772
primary action: #08090b (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #08090b background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a dark content card: background #08090b, border radius 24px, 36px vertical padding and 18px horizontal padding. Title 'Quarantine flaky tests' in Frost (#ffffff) neue weight 500 at 24px, body text 'Stay on top of flakes' in Muted Silver (#b3b3b5) neue weight 400 at 14px.
3. Create a ghost navigation button: transparent background, text 'Docs' in Carbon (#08090b) neue weight 400 at 16px, 9px vertical padding and 12px horizontal padding, no border radius.
4. Produce a section headline: text 'How Trunk improves developer productivity' in Frost (#ffffff) neue weight 700 at 45px, letter-spacing -0.81px, with a top margin of 48px from the previous section.

---
_Source: https://styles.refero.design/style/48971df7-919d-453c-9d0b-4600cd24c583_
