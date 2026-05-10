# Busuu — Design Reference

> Vibrant learning portal on a clean canvas. A spacious, friendly digital environment where interactive elements pop against a minimal backdrop.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://busuu.com](https://busuu.com) |
| Refero page | [https://styles.refero.design/style/72b85d0a-1ff8-4dd3-b33a-f55aad6df5c9](https://styles.refero.design/style/72b85d0a-1ff8-4dd3-b33a-f55aad6df5c9) |
| Theme | light |
| Industry | other |

## Overview

Busuu's design creates an educational and encouraging atmosphere through its vibrant, optimistic color palette and clear, structured layout. A dominant hero section with a gradient background grounds the brand's primary visual identity, which then transitions into a clean, spacious white canvas. Vivid blue and a unique, bright green act as key interactive accents, indicating engagement and positive action, while the consistent use of rounded corners on buttons and navigation elements softens the overall feel, enhancing approachability.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ocean Blue | `#116eee` | brand | Primary brand color for interactive text, links, and secondary buttons, creating a sense of clarity and trustworthiness. |
| Spring Green | `#11ee92` | brand | Prominent action color for primary CTA buttons, signaling progress and success. Its high saturation ensures immediate visibility. |
| Sky Tint | `#87b6f6` | brand | Secondary brand accent, used for subtle visual cues and illustrations, supporting the primary blue without competing. |
| Light Bluewash | `#b8d4fa` | brand | Muted blue accent for background elements or illustrations, adding depth to the brand palette. |
| Electric Cyan | `#06d2ff` | accent | Tertiary accent, provides an additional dynamic pop of color in illustrations. |
| Action Red | `#fa3746` | accent | Accent color for specific, high-attention elements within illustrations. |
| Mellow Yellow | `#ffcf00` | accent | Accent color, provides friendly contrast in illustrations or minor highlights. |
| Pure White | `#ffffff` | neutral | Dominant background color for content sections, cards, and prominent text elements for excellent readability. |
| Cloud Gray | `#f2f7fd` | neutral | Subtle background color for alternating section bands or very light surface elevation. |
| Border Fog | `#d6dee6` | neutral | Light gray for input borders and subtle UI separators, maintaining a soft aesthetic. |
| Text Anthracite | `#252b2f` | neutral | Primary text color for headlines, body copy, and form elements, offering strong contrast against light backgrounds. |
| Slate Blue | `#666e7` | neutral | Secondary text color for less prominent information, links, and subheadings, providing visual hierarchy. |
| Disabled Ash | `#b3b3b3` | neutral | Color for disabled states or subtle placeholder text, indicating non-interactivity. |
| Deep Black | `#000000` | neutral | Used for most icons and some instances of body text, providing maximum contrast and clarity where needed. |
| Hero Gradient | `#116eee` | neutral | Background for the primary hero section, creating an immersive and dynamic introduction to the brand. |

## Typography

### Nista

| Key | Value |
| --- | --- |
| weight | 400, 700, 800 |
| sizes | 10px, 12px, 14px, 16px, 18px, 24px, 36px, 40px |
| lineHeight | 1.14, 1.20, 1.30, 1.33, 1.50, 1.70 |
| letterSpacing | normal |
| substitute | system-ui, sans-serif |
| role | The primary typeface for all text elements, from body copy to headlines, maintaining a consistent, approachable brand voice across all sizes and weights. Weight 400 is standard for body text, 700 and 800 for emphasizing headlines and navigation. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.5 |  |
| body | 14 |  | 1.5 |  |
| body-lg | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.33 |  |
| heading | 24 |  | 1.2 |  |
| heading-lg | 36 |  | 1.14 |  |
| display | 40 |  | 1.14 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| inputs | 5px |
| buttons | 45.04px |
| navItems | 16px |

- **elementGap** — 8-20px
- **sectionGap** — 40-60px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Primary CTA Button Group

### Language Selector Carousel

### Display Language Selector Form

### Primary Call to Action Button

**Role:** Interactive element

Rounded pill button with 'Spring Green' background (#11ee92) and 'Text Anthracite' (#252b2f) text, providing clear visual hierarchy for primary actions. Padding is 12px vertical, 40px horizontal, with a 45.04px border-radius.

### Hero Section Headline

**Role:** Information display

Uses Nista font, typically at 40px, weight 800, with Pure White (#ffffff) color, set against the 'Hero Gradient' background to immediately capture attention.

### Text Input Field

**Role:** Form element

A text input with 'Border Fog' (#d6dee6) border, 'Text Anthracite' (#252b2f) text color, and a 5px border-radius. Padding of 10px vertical and 10px left/right, with 25px additional padding on the right for implied icons.

### General Body Text

**Role:** Information display

Uses Nista font, typically at 16px, weight 400, in 'Text Anthracite' (#252b2f) for primary content and 'Slate Blue' (#666e7e) for supporting information.

## Layout

The page primarily follows a max-width contained model after an initial full-bleed hero section. The hero is a dynamic gradient with a centered headline and CTA. Subsequent sections alternate between a 'Pure White' background and a 'Cloud Gray' or similar light neutral background, creating a gentle visual rhythm. Content within sections is often structured with left-aligned text and right-aligned visuals (or vice-versa) or as centered stacks for key messages. Navigation is a sticky top bar with clearly defined 'Learn for free' and 'Log in' actions, utilizing both a primary accent button and a secondary outlined button. A prominent feature is the use of horizontal scrollable carousels for content like language selection, indicating more content availability without cluttering the initial view.

## Imagery

The site uses a combination of abstract, friendly vector illustrations with a flat, clean aesthetic, and flags as small, functional icons. Illustrations feature simplified human figures engaged in learning activities, often within subtle outlines and filled with brand colors (like 'Sky Tint', 'Electric Cyan', 'Action Red', and 'Mellow Yellow') against a world map motif. Imagery is primarily explanatory and decorative, supporting the educational content rather than product showcases. There's a minimal use of complex photography, shifting focus to the UI and illustrations. Icons are outlined or filled, generally monochrome in 'Deep Black' or 'Text Anthracite', with a consistent visual weight.

## Dos & Donts

### Do

- Prioritize 'Spring Green' (#11ee92) for all primary call-to-action buttons, ensuring a 45.04px border-radius and 'Text Anthracite' (#252b2f) text.
- Use 'Ocean Blue' (#116eee) exclusively for interactive elements like links and secondary navigation items, either as text color or outline.
- Maintain a clear visual hierarchy by setting headlines in Nista 800 and body text in Nista 400 at their respective semantic sizes.
- Apply the `rgba(0, 0, 0, 0.1) 0px 1px 2px 0px` shadow consistently for subtle elevation on cards or modal elements.
- Utilize 'Pure White' (#ffffff) as the predominant background color for content sections to maximize readability.
- Employ a 4px base unit for all spacing, with common increments like 8px, 16px, 20px, and 40px for larger gaps and padding.
- Align all input fields with a 5px border-radius and 'Border Fog' (#d6dee6) border for a unified form aesthetic.

### Don't

- Do not use dark or highly saturated colors for large background areas, except for the hero section's specific gradient.
- Avoid using any border-radius value other than 5px for inputs, 16px for nav items, or 45.04px for buttons, to preserve brand consistency.
- Refrain from using strong, contrasting colors for general body text; stick to 'Text Anthracite' (#252b2f) and 'Slate Blue' (#666e7e).
- Do not introduce new shadow styles; adhere to the specified `rgba(0, 0, 0, 0.1) 0px 1px 2px 0px` for all elevation.
- Do not add unnecessary decorative elements or strong graphical treatments that compete with the clean, friendly aesthetic.
- Avoid using the 'Spring Green' (#11ee92) or 'Ocean Blue' (#116eee) colors for non-interactive textual content; reserve them for active elements.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text Anthracite: #252b2f
- Pure White: #ffffff
- Spring Green: #11ee92
- Ocean Blue: #116eee
- Border Fog: #d6dee6

### 3-5 Example Component Prompts
1. Create a primary call-to-action button: 'Spring Green' background (#11ee92), 'Text Anthracite' text (#252b2f), 'Nista' font weight 700, 12px vertical / 40px horizontal padding, 45.04px border-radius.
2. Design a navigation link button: transparent background, 'Ocean Blue' text (#116eee), 'Ocean Blue' border (#116eee, 1px solid), 'Nista' font weight 700, 12px vertical / 40px horizontal padding, 45.04px border-radius.
3. Generate a section subheading: 'Text Anthracite' (#252b2f), 'Nista' font weight 700, 24px size, 1.2 line-height.
4. Produce a standard input field: 'Border Fog' border (#d6dee6, 1px solid), 'Pure White' background (#ffffff), 'Text Anthracite' placeholder text (#252b2f), 5px border-radius, 10px vertical / 10px left / 25px right padding.
5. Create a language selection card: 'Pure White' background (#ffffff), `rgba(0, 0, 0, 0.1) 0px 1px 2px 0px` box-shadow, 16px corner radius, 'Nista' font weight 400 at 16px, 'Slate Blue' (#666e7e) text, with an image slot above the text.

---
_Source: https://styles.refero.design/style/72b85d0a-1ff8-4dd3-b33a-f55aad6df5c9_
