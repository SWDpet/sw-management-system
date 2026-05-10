# Supabase — Design Reference

> Midnight Terminal Interface — a dark, organized, and quietly powerful workspace.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://supabase.com](https://supabase.com) |
| Refero page | [https://styles.refero.design/style/632249f1-fd78-4c77-9b34-7bae37ff3e9b](https://styles.refero.design/style/632249f1-fd78-4c77-9b34-7bae37ff3e9b) |
| Theme | dark |
| Industry | devtools |

## Overview

Supabase embodies a technical, deeply functional aesthetic like a high-performance terminal where every element serves a clear purpose. Dark, layered surfaces create a sense of depth and focus, akin to illuminated code on a dark screen. The primary green accent color, `Supabase Green`, acts as a confident highlight, drawing attention to interactive elements without being overwhelming. Typography prioritizes clarity and a dense information hierarchy, ensuring readability even with intricate data displays.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Abyss | `#000000` | neutral | Deepest background elements, occasional graphic fill. |
| Ebony Canvas | `#121212` | neutral | Primary page background, base surface for components. |
| Graphite Base | `#242424` | neutral | Alternative surface background, button background for secondary actions, subtle borders. |
| Steel Surface | `#2e2e2` | neutral | Card backgrounds, elevated UI elements, default input backgrounds. The subtle deviation from pure black creates visual layering. |
| Carbon Border | `#393939` | neutral | Input borders, dividers, subtle separators. |
| Iron Outline | `#4d4d4d` | neutral | Subtle text, icon strokes, secondary graphic elements, subtle borders. |
| Mid-Gray Text | `#898989` | neutral | Muted body text, secondary information, disabled states. |
| Silver Highlight | `#b4b4b4` | neutral | Lightest neutral text for contrast on dark backgrounds, secondary navigation items. |
| Whiteout | `#fafafa` | neutral | Primary headings, body text, interactive text on buttons, icons. High contrast for critical information. |
| Supabase Green | `#3ecf8` | brand | Key interaction accents, prominent headings, success indicators, interactive links. The signature brand color. |
| Deep Sea Green | `#1f4b37` | brand | Subtle border for primary action buttons, hover states that deepen the brand green. |
| Forest Call to Action | `#006239` | brand | Primary call-to-action button background. Opaque and declarative. |
| Glow Green | `#00c573` | brand | Link hover states, subtle accent in decorative elements if needed. |
| Shadow Green | `#002918` | brand | Rare background color, likely for specific brand moments or deep elements. |

## Typography

### Circular

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px, 18px, 24px, 36px, 72px |
| lineHeight | 1.00, 1.11, 1.20, 1.25, 1.33, 1.38, 1.43, 1.50, 1.56 |
| letterSpacing | -0.0070em |
| substitute | Inter |
| role | Primary UI font for all headings, body text, labels, and buttons. Offers excellent readability and a strong character with its subtly humanist touch. |

### Source Code Pro

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.33 |
| letterSpacing | 0.1000em |
| substitute | Menlo |
| role | Monospaced font for code snippets, technical data, or whenever a fixed-width, precise feel is required. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.56 | -0.007 |
| body-sm | 14 |  | 1.5 | -0.007 |
| body | 16 |  | 1.43 | -0.007 |
| subheading | 18 |  | 1.38 | -0.007 |
| heading | 24 |  | 1.33 | -0.007 |
| heading-lg | 36 |  | 1.25 | -0.007 |
| display | 72 |  | 1.11 | -0.007 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16px |
| inputs | 6px |
| buttons | 6px |
| pillButtons | 9999px |

- **elementGap** — 
- **sectionGap** — 
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Ebony Canvas | `#121212` | 0 | Primary page background |
| Steel Surface | `#2e2e2` | 1 | Elevated cards, modals, and distinct content sections |
| Graphite Base | `#242424` | 2 | Input backgrounds, secondary button backgrounds, subtle hover states |

## Components

### CTA Button Group

### Feature Cards Grid

### Testimonial Cards

### Ghost Button

**Role:** secondary action

backgroundColor: transparent, color: #fafafa, border: none, borderRadius: 6px, padding: 8px.

### Secondary Button

**Role:** secondary action

backgroundColor: #242424, color: #fafafa, borderColor: #393939, borderRadius: 6px, padding: 8px 16px.

### Primary Call to Action Button

**Role:** primary action

backgroundColor: #006239, color: #fafafa, borderColor: rgba(62, 207, 142, 0.3), borderRadius: 6px, padding: 8px 16px.

### Alert Pill Button (State of Startups)

**Role:** informational alert

backgroundColor: #121212, color: #fafafa, borderColor: #2e2e2, borderRadius: 9999px, padding: 8px 32px.

### Feature Card

**Role:** content container

backgroundColor: #121212, borderRadius: 16px, boxShadow: none, padding: 24px.

### Text Input (with focus ring)

**Role:** data entry

backgroundColor: rgba(250, 250, 250, 0.027), color: #fafafa, borderColor: #393939, borderRadius: 6px, padding: 8px. Focus state likely uses a #3ecf8 border or shadow.

## Layout

The page adheres to a mostly full-bleed layout, particularly in hero sections, with content centered within an implied max-width. The hero section is characterized by a full-width dark background with a large, centered headline that uses two distinct colors (`Whiteout` and `Supabase Green`) for visual emphasis. Content sections alternate between visually seamless dark backgrounds and slightly lighter dark surfaces for cards. There's a clear rhythm of stacked content blocks and feature sections that often alternate between text on one side and a visual or conceptual graphic on the other. Card grids, typically 3-column, are used to present features, maintaining consistent padding. Navigation is a sticky top bar, minimal and persistent. The overall density is comfortable, providing breathing room around critical information.

## Imagery

The visual language for imagery is primarily functional and technical. Product shots are minimalist, showcasing UI directly against dark backgrounds, often with code-like visual elements. Illustrations, where present, are abstract and geometric, utilizing line art and flat fills in shades of gray and `Supabase Green`. Icons are outlined, minimal, and mono-colored, complementing the overall technical aesthetic. Photography is absent, replaced by stylized graphics or product UI snippets. Images serve an explanatory or demonstrative role rather than decorative one, maintaining a high density of visual information without clutter. The elephant logo is abstracted and used as a subtle background texture.

## Elevation philosophy

Supabase intentionally avoids traditional box shadows for elevation. Instead, depth and hierarchy are communicated through distinct background color shifts. Elements on a higher 'layer' utilize a lighter, less saturated dark tone (#2e2e2e) against the base page background (#121212), creating a clean, high-contrast separation without blur or visual noise. The only shadow detected is a subtle one on buttons, likely for a minimal pressed state, not for general elevation.

## Dos & Donts

### Do

- Use 'Circular' font at weight 400 or 500 for all UI text, except code blocks.
- Adopt #fafafa for primary text and headings against dark backgrounds to ensure AAA contrast.
- Apply #3ecf8 ('Supabase Green') exclusively for active states, primary links, and key brand affirmations.
- Maintain a clear visual hierarchy by utilizing #121212 as the base surface and #2e2e2 for elevated cards and modals.
- Utilize 6px radius for interactive elements like buttons and inputs, transitioning to 16px for larger content cards.
- Employ the 9999px radius sparingly, only for 'Pill Buttons' that act as prominent announcements or unique navigational elements.
- Ensure all interactive elements have a clear visual feedback using either a color change to 'Glow Green' or a subtle border change to 'Deep Sea Green'.

### Don't

- Avoid arbitrary color usage; every color should map to a defined role in the palette.
- Do not use shadows for elevation; rely on background color changes from #121212 to #2e2e2 to differentiate surface levels.
- Do not apply `Source Code Pro` for general body text or headlines; reserve it specifically for code or data display.
- Refrain from using mixed color backgrounds or gradients unless explicitly defined as a brand graphical element.
- Do not break the established spacing scale; maintain 8px increments or derived values (e.g., 24px, 32px) for consistency.
- Avoid excessive text decoration; links are identified by color ('Supabase Green') rather than underlines or heavy styling.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (Primary): `#fafafa`
- Background (Canvas): `#121212`
- CTA (`Supabase Green`): `#006239`
- Border (Input/Secondary): `#393939`
- Accent (Heading/Link): `#3ecf8e`

### 3 Example Component Prompts
1. Create a hero section with `Ebony Canvas` background. Headline 'Build in a weekend' in `Whiteout` and 'Scale to millions' in `Supabase Green`, both `Circular` font, 72px size, 1.11 lineHeight, -0.007em letterSpacing. Below, create a `Primary Call to Action Button` and a `Secondary Button`.
2. Design a feature card: Use `Steel Surface` background, 16px borderRadius, 24px padding. Inside, place a 'Circular' font, 18px size, 1.38 lineHeight heading in `Whiteout`, followed by 'Circular' font, 14px size, 1.5 lineHeight body text in `Mid-Gray Text`.
3. Implement a navigation bar item: Text 'Pricing' `Circular` font, 16px size, 1.43 lineHeight in `Whiteout`. On hover, the text color changes to `Supabase Green` (`#3ecf8e`).

---
_Source: https://styles.refero.design/style/632249f1-fd78-4c77-9b34-7bae37ff3e9b_
