# fastht.ml — Design Reference

> Midnight dev playground

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://fastht.ml](https://fastht.ml) |
| Refero page | [https://styles.refero.design/style/786f99e5-8c40-4205-a878-bf006b330f4e](https://styles.refero.design/style/786f99e5-8c40-4205-a878-bf006b330f4e) |
| Theme | dark |
| Industry | devtools |

## Overview

FastHTML embraces a playful yet pragmatic developer tool aesthetic, combining a dark, sophisticated backdrop with vibrant, rounded 'blob' shapes that punctuate the UI with energy. Typography is crisp and minimal, providing clear information hierarchy. Component surfaces are often soft and pill-shaped, creating a friendly, approachable feel within the dark theme. The system uses a limited, high-contrast palette where color serves primarily as decorative accents or functional states, maintaining a sense of focused clarity.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Obsidian Canvas | `#3a2234` | neutral | Hero background, base background for content sections with contrasting light foreground elements |
| Deep Aubergine | `#333333` | neutral | Secondary surface for card backgrounds and elevated components, providing slight visual separation from the canvas |
| Charcoal Smoke | `#808080` | neutral | Subtle shadows and button focus rings |
| Bright White | `#ffffff` | neutral | Primary text, button text, and occasional full-fill button backgrounds |
| Ash Gray | `#f3f3f3` | neutral | Soft section background, alternate surface, and quiet card fill. |
| Off-White Mist | `#e5e7eb` | neutral | Predominant border color for all UI elements, creating a subtle, consistent structure |
| Soft Stone | `#e8e8fc` | neutral | Soft section background, alternate surface, and quiet card fill. |
| Muted Lavender | `#939eeb` | accent | Secondary text for helper content, metadata, and link accents, offering a slightly softer contrast than white |
| Electric Green | `#3cdd8c` | accent | Vivid accent for decorative shapes, icons, and card backgrounds. Signals energy and positivity |
| Goldenrod Pop | `#ffc435` | accent | Vivid accent for decorative shapes, icons, and card backgrounds. Adds a warm, energetic punctuation |
| Bubblegum Pink | `#e699d9` | accent | Vivid accent for decorative shapes, icons, and card backgrounds. Conveys playfulness |
| Lilac Jelly | `#7575f0` | accent | Vivid accent for decorative shapes, icons, and card backgrounds, similar in hue to Muted Lavender but more saturated |
| Mint Cream | `#d4f7e6` | accent | Background for lighter-toned cards, a very pale greenish tint |
| Fairy Floss | `#ffccf7` | accent | Background for lighter-toned cards, a very pale pinkish tint |
| Butter Cream | `#ffeecc` | accent | Background for lighter-toned cards, a very pale yellowish tint |
| Soft Mauve | `#eddee9` | accent | Background for lighter-toned cards, a very pale purplish tint |
| Midnight Ink | `#000000` | neutral | Filled button backgrounds, body text on light backgrounds, icon fills |
| Cloud Gray | `#cccccc` | neutral | Subtle secondary text, navigation links, and inactive UI elements providing less contrast than Bright White |
| Dark Silver | `#999999` | neutral | Background for tabs or inactive buttons |

## Typography

### Geist

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 16px, 20px, 24px, 32px, 60px, 72px |
| lineHeight | 1.10, 1.22, 1.25, 1.33, 1.40, 1.50 |
| letterSpacing | -0.24px at 16px, -0.3px at 20px, -0.24px at 24px, -0.32px at 32px, -0.6px at 60px, -0.72px at 72px |
| fontFeatureSettings | 'clig' off, 'liga' off |
| substitute | Inter |
| role | Primary typeface for all headings, body text, UI labels, and links. Its slightly condensed forms maintain density and clarity. |

### Geist Mono

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 14px, 16px |
| lineHeight | 1.80 |
| letterSpacing | normal |
| fontFeatureSettings | 'clig' off, 'liga' off |
| substitute | SF Mono |
| role | Used for code snippets, technical details, and table content, providing alignment and clear distinction from proportional text. |

### Arial Black

| Key | Value |
| --- | --- |
| weight | 400, 700 |
| weights | 400, 700 |
| sizes | 16px, 20px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| fontFeatureSettings | 'clig' off, 'liga' off |
| substitute | Impact |
| role | Auxiliary display text for specific expressive moments, offering strong visual contrast to Geist. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.8 | 0 |
| body-sm | 16 |  | 1.5 | -0.24 |
| body | 20 |  | 1.25 | -0.3 |
| subheading | 24 |  | 1.25 | -0.24 |
| heading | 32 |  | 1.33 | -0.32 |
| heading-lg | 60 |  | 1.1 | -0.6 |
| display | 72 |  | 1.1 | -0.72 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tabs | 1000px |
| cards | 24px |
| buttons | 9999px |
| accentShapes | 20px, 24px, 40px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 32px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Obsidian Canvas | `#3a2234` | 0 | Primary page background, especially for hero sections. |
| Deep Aubergine | `#333333` | 1 | General content section backgrounds, providing a subtle shift from the canvas. |
| Soft Stone | `#e8e8fc` | 2 | Elevated cards and interactive components, with a slight tint away from pure white. |
| Ash Gray | `#f3f3f3` | 3 | Lightest surface for specific cards and decorative fills, providing maximum contrast on a dark background. |

## Components

### Pill Button - Transparent

**Role:** Secondary action button, typically for 'Read docs' or similar navigation.

Nearly transparent background `rgba(255, 255, 255, 0.6)`, text `rgba(0, 0, 0, 0.8)`, `1px` border of `Off-White Mist`, `9999px` radius. Padding `8px` vertical, `16px` horizontal.

### Pill Button - Filled

**Role:** Primary action button, often for 'Learn more' or calls to action.

Solid `Midnight Ink` background, `Bright White` text, `1px` border of `Off-White Mist`, `9999px` radius. Padding `4px` vertical, `16px` horizontal.

### Pill Button - Light

**Role:** Interactive elements within specific modules, like 'Watch intro'.

Solid `Bright White` background, `rgba(0, 0, 0, 0.8)` text, `1px` border of `Off-White Mist`, `9999px` radius. Padding `8px` vertical, `8px` horizontal.

### Text Link Button (Ghost)

**Role:** Minimal interactive elements where focus is on text and not a prominent button shape.

Transparent background, `rgba(255, 255, 255, 0.8)` text, no border or radius. No explicit padding.

### Decorative Accent Card

**Role:** Large, irregularly shaped background elements used for visual interest rather than content containment.

Solid background of `Bubblegum Pink`, `Goldenrod Pop`, `Electric Green`, or `Lilac Jelly`. Radius `24px` or `16px` or `20px` or `40px`. No shadows. Padding `32px` or `48px`.

### Informational Card (Elevated)

**Role:** Content container for code samples or distinct feature blocks, providing visual lift.

Background `Soft Stone`, `20px` radius. Features an inset shadow `rgba(255, 255, 255, 0.1) 0px 2px 4px 0px` and a subtly cast shadow `rgba(0, 0, 0, 0.5) 0px 4px 8px 0px`.

### FAQ Accordion Item

**Role:** Interactive content accordion for questions and answers.

Background `Soft Stone` with a `20px` radius. Includes a subtle box shadow. Text color `Midnight Ink` and `Muted Lavender`.

## Layout

Pages use a full-bleed layout alternating darker `Obsidian Canvas` or `Deep Aubergine` sections with lighter `Soft Stone` elements. The hero section features a centered headline over a dark background, often with large, overlapping abstract shapes. Section rhythm is dictated by generous vertical spacing of `64px`. Content within sections is primarily organized in vertical stacks, often with `24px` gaps between elements. Feature sections frequently employ multi-column arrangements like a two-column text-left/image-right pattern or specific `Informational Cards`. Navigation is a sticky top bar with minimal links and calls to action.

## Imagery

The site uses abstract, geometric 'blob' illustrations in vibrant accent colors (`Electric Green`, `Goldenrod Pop`, `Bubblegum Pink`, `Lilac Jelly`) to add visual interest and a playful mood, often overlaid on hero sections or serving as background elements. Photography is minimal, appearing as small, circular profile pictures in social proof or inline video previews. Icons are typically filled, monochrome, and used functionally. Imagery density is low, with visuals serving primarily as decorative accents or concise product representations rather than extensive content. Product screenshots like code examples use the `Geist Mono` font and a simple, contained visual style.

## Dos & Donts

### Do

- Prioritize `Obsidian Canvas` and `Deep Aubergine` for backgrounds, using `Bright White` for primary text and `Muted Lavender` for secondary content.
- Use `Geist` for all primary text elements, and `Geist Mono` for code blocks and technical details.
- Apply `9999px` border-radius for all interactive buttons and small tags; use `24px` for main content cards and `20px` for elevated UI elements.
- Employ `Electric Green`, `Goldenrod Pop`, `Bubblegum Pink`, and `Lilac Jelly` judiciously for decorative 'blob' shapes and active state indicators, never as primary text or background for large content areas.
- Maintain a clear `8px` element gap between inline elements and a `32px` padding for card content, with `64px` vertical spacing between major sections.
- Use `Off-White Mist` as the consistent `1px` border for all UI components, including buttons, cards, and input fields.
- Apply the specific `rgba(255, 255, 255, 0.1) 0px 2px 4px 0px inset, rgba(0, 0, 0, 0.5) 0px 4px 8px 0px` shadow for elevated components like the `Informational Card`.

### Don't

- Avoid using `Electric Green`, `Goldenrod Pop`, `Bubblegum Pink`, or `Lilac Jelly` for large background areas or extensive body text; their role is purely accentual.
- Do not introduce sharp corners or low radii on interactive elements; `9999px` and `24px` are the primary radii.
- Refrain from using `Arial Black` for body text or general UI labels; reserve it for specific, high-impact display text only.
- Do not use generic gray values for text or borders when `Muted Lavender`, `Cloud Gray`, `Bright White`, or `Off-White Mist` are available and semantically appropriate.
- Avoid arbitrary changes in line-height or letter-spacing; adhere to the defined `Geist` and `Geist Mono` typography profiles.
- Do not use shadows indiscriminately; reserve them for interactive elements and designated elevated cards to maintain a clean aesthetic.
- Avoid cluttering the layout; maintain generous `64px` vertical section spacing and focus on clear, distinct component separation.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #3a2234
border: #e5e7eb
accent: #939eeb
primary action: #000000 (filled action)

Example Component Prompts:
1. Create a hero section: `Obsidian Canvas` background. Headline 'Modern web applications' in `Geist` font, 72px size, 1.1 lineHeight, -0.72px letterSpacing, `Bright White` color. Body text 'Built on solid web foundations...' in `Geist` font, 20px size, 1.25 lineHeight, -0.3px letterSpacing, `Muted Lavender` color. Include a `Pill Button - Filled` for 'Learn more' and a `Pill Button - Light` for 'Watch intro'.
2. Create an `Informational Card` showing a code snippet: `Soft Stone` background, `20px` radius, `rgba(255, 255, 255, 0.1) 0px 2px 4px 0px inset, rgba(0, 0, 0, 0.5) 0px 4px 8px 0px` shadow. Code uses `Geist Mono` font, 16px size, `Bright White` color. External `Midnight Ink` title using `Geist`, 24px size, 1.25 lineHeight, -0.24px letterSpacing.
3. Create an `FAQ Accordion Item`: `Soft Stone` background, `20px` radius. Question text in `Geist` font, 20px size, `Midnight Ink` color. Answer text in `Geist` font, 16px size, `Muted Lavender` color. Ensure `1px Off-White Mist` border and `8px` element gaps for internal spacing.

---
_Source: https://styles.refero.design/style/786f99e5-8c40-4205-a878-bf006b330f4e_
