# Checkly — Design Reference

> Midnight Terminal Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.checklyhq.com](https://www.checklyhq.com) |
| Refero page | [https://styles.refero.design/style/78558b01-c101-4b8e-8401-db91269b1150](https://styles.refero.design/style/78558b01-c101-4b8e-8401-db91269b1150) |
| Theme | dark |
| Industry | devtools |

## Overview

Checkly employs a dark-mode command center aesthetic with a focus on code-centric visualization. Deep blues and near-black surfaces are punctuated by vivid blue accents for interactive elements and critical information. Typography is precise and utilitarian, prioritizing legibility within code blocks and clean hierarchy for display text. Components feature subtle elevation and controlled use of rounded corners, creating a sense of understated robustness.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space Blue | `#041734` | brand | Primary background for hero sections and key content blocks, dark button fills |
| Midnight Ink | `#002652` | brand | Text on lighter dark surfaces, secondary navigation text, active states |
| Charcoal Slate | `#0f172a` | neutral | Darkest card backgrounds, deeply nested surface elements |
| Storm Gray | `#374151` | neutral | Muted text for secondary information, card borders, inactive navigation links |
| Dark Steel | `#1f2937` | neutral | Dark borders and separators for elevated surfaces and inverted UI. Do not promote it to the primary CTA color |
| Light Ash | `#a7babe` | neutral | Body text on dark backgrounds, helper text, link text in paragraph content |
| Electric Blue | `#0075ff` | accent | Blue text accent for links, tags, and emphasized short phrases. Do not promote it to the primary CTA color |
| Subtle Gray | `#445458` | neutral | Icon fills, very subtle secondary text on dark surfaces |
| Navy Black | `#061220` | neutral | Deep background for elevated cards and distinct content containers |
| Code Block Gray | `#1a1f36` | neutral | Background for code snippets and console-like sections |
| Silver Pine | `#64748b` | neutral | Placeholder text, very subtle tertiary text details |
| Deep Ocean Blue | `#001027` | brand | Background for deeply embedded cards or sections requiring higher contrast from surrounding dark elements |
| Cloud Gray Border | `#cfdfec` | neutral | Light border for outline buttons |
| Vivid Blue | `#008ef0` | accent | Accent for filled action buttons, subtle highlight states |
| Success Green | `#4ade80` | accent | Green text accent for links, tags, and emphasized short phrases. Use as a supporting accent, not as a status color |
| Lightest Gray | `#e5e7eb` | neutral | Hairline borders, dividers, input outlines, and card edges on light surfaces. Do not promote it to the primary CTA color |
| Pure Black | `#000000` | neutral | Brand text on light backgrounds, icon fills where stark contrast is needed |
| Pure White | `#ffffff` | neutral | Primary text on dark backgrounds, backgrounds for light-themed sections, essential button fills; Specialized button background with a subtle gradient hint |
| Mid Gray | `#cccccc` | neutral | Muted text, subtle borders, inactive elements |
| Dark Overlay | `#bfbfbf` | neutral | Shadow color for card elevation |
| Darkest Card | `#0d1117` | neutral | Specific, very dark card background |
| Medium Gray Border | `#d1d5db` | neutral | Default input borders, inactive card borders |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 600, 700 |
| weights | 300, 400, 500, 600, 700 |
| sizes | 10px, 12px, 14px, 16px, 18px, 20px, 24px, 30px, 32px, 48px, 60px |
| lineHeight | 1.00, 1.20, 1.25, 1.33, 1.40, 1.43, 1.50, 1.56, 1.63, 2.40 |
| letterSpacing | -0.0400em at 60px, -0.0250em at 48px, 0.0500em at 10px, normal at 12px-32px |
| substitute | system-ui |
| role | Primary typeface for all brand copy, headings, body text, and UI components. It establishes a modern and professional tone. |

### ui-monospace

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 11px, 12px, 14px |
| lineHeight | 1.33, 1.43, 1.63 |
| letterSpacing | normal |
| substitute | monospace |
| role | Used for command-line instructions, code snippets, and areas where monospace formatting is essential for technical accuracy and readability. |

### JetBrains Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 10px, 12px, 16px |
| lineHeight | 1.50, 1.63 |
| letterSpacing | normal |
| substitute | monospace |
| role | Specialized monospace font primarily used for displaying code in product screenshots and dedicated code blocks, providing enhanced legibility for programming content. Its presence signifies the brand's developer-centric focus. |

### Consolas

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 10px |
| lineHeight | 1.5 |
| role | Consolas — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 | 0.5 |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.56 |  |
| heading-sm | 24 |  | 1.33 |  |
| heading | 32 |  | 1.25 |  |
| heading-lg | 48 |  | 1.2 | -0.48 |
| display | 60 |  | 1 | -0.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 12px |
| inputs | 4px |
| buttons | 8px |

- **elementGap** — 12px
- **sectionGap** — 48px
- **cardPadding** — 
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Deep Space Blue Canvas | `#041734` | 0 | Primary page background for hero sections and base content. |
| Dark Steel Card Surface | `#1f2937` | 1 | Default background for major content cards and information panels. |
| Charcoal Slate Code Surface | `#0f172a` | 2 | Elevated surface for code blocks and console-like displays. |
| Deep Ocean Blue Inset Card | `#001027` | 3 | Highest elevation for detailed data cards with an inset shadow effect. |

## Components

### Floating Action Button

**Role:** Key action buttons with a distinct, larger appearance.

Background: Gradient Button Light (#ffffff to rgba(96, 148, 193, 0.2)), Text: Pure Black (#000000), Radius: 16px, Padding: 32px all.

### Solid Action Button

**Role:** Primary interactive buttons for calls to action.

Background: Vivid Blue (#008ef0) or Deep Space Blue (#041734), Text: Pure White (#ffffff), Radius: 8px, Padding: 10px vertical, 16px horizontal.

### Ghost Outline Button

**Role:** Secondary or tertiary actions, often subtle.

Background: transparent, Text: Light Ash (#a7babe) or Lightest Gray (#e5e7eb), Border: 1px solid Cloud Gray Border (#cfdfec) or Lightest Gray (#e5e7eb), Radius: 8px, Padding: 6px vertical, 10px horizontal.

### Text Link Button

**Role:** Inline actions or navigation items that act as buttons.

Background: transparent, Text: Lightest Gray (#e5e7eb), No explicit border, Radius: 0px, Padding: 14px vertical, 0px horizontal.

### Default Card

**Role:** Base container for content, appearing within sections.

Background: Dark Steel (#1f2937), Radius: 12px, Shadow: rgba(0, 0, 0, 0.1) 0px 10px 15px -3px, rgba(0, 0, 0, 0.1) 0px 4px 6px -4px. No padding provided in data for this variant, implying content controls its own spacing.

### Elevated Code Card

**Role:** Cards specifically for displaying code or console-like output with a clear visual separation.

Background: Charcoal Slate (#0f172a), Radius: 8px, Shadow: rgba(0, 0, 0, 0.25) 0px 25px 50px -12px. No padding provided in data for this variant, implying content controls its own spacing.

### Inset Detail Card

**Role:** Cards used for detailed data or controls, with a subtle internal glow.

Background: Deep Ocean Blue (#001027), Radius: 12px, Shadow: rgba(0, 0, 0, 0.25) 0px 4px 12px 0px, rgba(0, 0, 0, 0.15) 0px 1px 3px 0px, rgba(255, 255, 255, 0.04) 0px 1px 0px 0px inset. Padding: 16px vertical, 20px horizontal.

### Inline Tag

**Role:** Small, informational labels.

Background: Deep Space Blue (#041734), Text: Pure White (#ffffff), Radius: 9999px (pill-shaped), often appears with minimal padding from contextual text.

## Layout

The page primarily uses a max-width contained layout, though the initial hero section spans full-bleed. The hero features a centered headline over a deep blue background with abstract, interconnected lines. Section rhythm alternates between full-bleed dark sections (Deep Space Blue) and slightly lighter dark sections (like Dark Steel or Charcoal Slate), creating distinct visual blocks. Content arrangement often employs a two-column layout with text on one side and product screenshots or graphics on the other, or centered stacks of information modules. While no explicit grid system is mentioned beyond card layouts, a consistent vertical spacing creates clear visual separation. Navigation is handled by a sticky top bar.

## Imagery

The site favors illustrative, abstract graphics that represent data flows and network connections. These are typically blue-hued outlines or filled shapes on dark backgrounds, often radiating from a central focal point. Product screenshots are prominent, featuring dark mode UI elements with vibrant accents, showcasing code and monitoring dashboards. Occasionally, there are simple, filled icons (like the raccoon mascot) and system-like icons (outlined, moderate stroke weight) used decoratively or functionally. Imagery plays an explanatory role for product features and adds atmospheric depth to the UI, maintaining a text-dominant feel with images serving as visual anchors.

## Dos & Donts

### Do

- Use Inter font family for all primary text content, adjusting weights (300-700) to create hierarchy.
- Apply Deep Space Blue (#041734) as the base background for major sections and hero components, ensuring text is Pure White (#ffffff) for contrast.
- Utilize Electric Blue (#0075ff) specifically for highlighting interactive elements, active states, and as an accent color in product graphics.
- Maintain a clear visual hierarchy by limiting shadows to card components, employing the specified rgba(0, 0, 0, 0.1) 0px 10px 15px -3px stack for subtle elevation.
- Employ a consistent 12px elementGap for default spacing between UI controls and related content blocks.
- Ensure all primary call-to-action buttons use a Solid Action Button style with Vivid Blue (#008ef0) or Deep Space Blue (#041734) background for clear interaction signals.
- Code snippets and technical content must use ui-monospace or JetBrains Mono for authenticity and specific rendering.

### Don't

- Avoid using Pure Black (#000000) for general text on dark backgrounds; prefer Pure White (#ffffff) or Light Ash (#a7babe).
- Do not introduce new color hues; stick to the approved palette of blues, grays, and the single vivid green for success states.
- Never arbitrarily change corner radii; use 8px for buttons, 12px for cards, and 9999px (pill shape) for tags.
- Do not deviate from the defined type scale; always map content to one of the specified roles (caption, body-sm, body, etc.) with their respective sizes and line heights.
- Refrain from using strong, colorful background gradients outside of the specified 'Gradient Button Light' component.
- Avoid excessive spacing or too many different spacing values; adhere to the 4px base unit and established element, card, and section gaps.
- Do not use shadows on elements that are not designated as cards or specifically requiring elevation.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #ffffff
- background: #041734
- border: #e5e7eb
- accent: #0075ff
- primary action: #041734 (filled action)

### 3-5 Example Component Prompts
- Create a hero section: background Deep Space Blue (#041734). Headline 'Detect. Communicate. Resolve.' using Inter 60px weight 700 with letter-spacing -0.6px, color Pure White (#ffffff). Subtext 'Checkly unifies testing...' using Inter 20px weight 400, color Light Ash (#a7babe).
- Create a Primary Action Button: #041734 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Create a Ghost Outline Button: 'Start for free' uses transparent background, Inter 16px weight 500, Lightest Gray (#e5e7eb) text, 1px solid Cloud Gray Border (#cfdfec), 8px radius, 6px vertical and 10px horizontal padding.
- Create a Default Card: background Dark Steel (#1f2937), radius 12px, box-shadow rgba(0, 0, 0, 0.1) 0px 10px 15px -3px, rgba(0, 0, 0, 0.1) 0px 4px 6px -4px. Content: heading using Inter 24px weight 600, Pure White (#ffffff), body text Inter 16px weight 400, Light Ash (#a7babe).
- Create a code block using `JetBrains Mono` at 12px weight 400, text Pure White (#ffffff), within an Elevated Code Card (background Charcoal Slate (#0f172a), radius 8px, box-shadow rgba(0, 0, 0, 0.25) 0px 25px 50px -12px).

---
_Source: https://styles.refero.design/style/78558b01-c101-4b8e-8401-db91269b1150_
