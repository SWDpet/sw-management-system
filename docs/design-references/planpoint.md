# Planpoint — Design Reference

> Architectural blueprint on white marble

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.planpoint.io](https://www.planpoint.io) |
| Refero page | [https://styles.refero.design/style/fc96be71-d71b-4fc1-a041-f13b3eae7dd5](https://styles.refero.design/style/fc96be71-d71b-4fc1-a041-f13b3eae7dd5) |
| Theme | light |
| Industry | other |

## Overview

Planpoint employs a bright, professional workspace aesthetic with a primary focus on clean UI and a strong accent blue. The design is characterized by generous spacing, soft rounded corners on interactive elements, and robust typography that balances readability with impact. Colors are predominantly achromatic, allowing the vivid brand blue to punctuate key actions and information, while subtle surface differentiation maintains visual hierarchy.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#00051a` | neutral | Primary text, dark surface backgrounds, deep accent for iconography and illustrations |
| Royal Blue | `#0f68ea` | brand | Primary action buttons, interactive elements, significant brand accents, and energetic backgrounds in hero sections. This vivid blue drives user focus |
| Sky Blue | `#007aff` | accent | Link text, borders for outlined icons, and secondary interactive components. A lighter, more active shade of blue |
| Sunburst Yellow | `#ffcb00` | accent | Yellow action color for filled buttons, selected navigation states, and focused conversion moments. |
| White Canvas | `#ffffff` | neutral | Page backgrounds, card surfaces, ghost button text and borders, and primary text on dark backgrounds |
| Pewter Gray | `#1d1d1f` | neutral | Secondary text, subtle borders, and inactive icon fills. A dark gray that offers contrast to primary text without harshness |
| White Smoke | `#f0f2f4` | neutral | Secondary surface backgrounds, button fills for tertiary actions, and background for featured content blocks. Provides a soft visual break from White Canvas |
| Slate Blue | `#000a3b` | neutral | Darker card backgrounds and occasional deep blue contextual surfaces |
| Soft Gray | `#e5e6e8` | neutral | Lightest gray, used for subtle borders and as a background for less prominent headings |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 14px, 16px, 18px, 22px, 23px, 29px, 32px, 43px, 50px, 72px, 144px |
| lineHeight | 0.90, 1.00, 1.10, 1.20, 1.25, 1.30, 1.55, 1.60 |
| letterSpacing | -0.071em at 144px, -0.067em at 72px, -0.060em at 50px, -0.044em at 43px, -0.033em at 32px, -0.031em at 29px, -0.027em at 23px, -0.041em at 14px |
| substitute | system-ui |
| role | The sole typeface for all text elements, ranging from navigation to body copy and headlines. Its consistent application across a wide range of weights and sizes contributes to the system's clarity and modern feel. Specific heavy negative letter-spacing for large headlines makes them distinctive and compact, while tighter tracking for smaller text ensures dense information remains readable. |

### system-ui

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 14px |
| lineHeight | 1.5 |
| letterSpacing | -0.041 |
| role | system-ui — detected in extracted data but not described by AI |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.55 | -0.41 |
| body | 16 |  | 1.6 |  |
| subheading | 18 |  | 1.55 |  |
| heading-sm | 22 |  | 1.25 |  |
| heading | 29 |  | 1.3 | -0.9 |
| heading-lg | 43 |  | 1.2 | -1.9 |
| display | 72 |  | 1.1 | -4.82 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 28.8px |
| links | 160px |
| badges | 16px |
| buttons_chip | 46.8px |
| buttons_pill | 57.6px |

- **elementGap** — 14px
- **sectionGap** — 72px
- **cardPadding** — 72px
- **pageMaxWidth** — 

## Components

### Primary Action Button

**Role:** Filled button for primary calls to action

Background: Royal Blue (#0f68ea), Text: White Canvas (#ffffff), Border-radius: 57.6px, Padding: 14.4px vertical, 21.6px horizontal. Creates a prominent, clickable target.

### Secondary Action Button

**Role:** Ghost button for secondary actions or links

Background: transparent, Text: Pewter Gray (#1d1d1f) or Midnight Ink (#00051a), Border: 1px solid Pewter Gray (#1d1d1f), Border-radius: 57.6px, Padding: 0px vertical, 72px horizontal (large). Emphasizes text with a minimal border for less aggressive calls to action.

### Tertiary Action Button

**Role:** Filled button for less prominent actions, often within content blocks

Background: White Smoke (#f0f2f4), Text: Midnight Ink (#00051a), Border-radius: 57.6px, Padding: 0px vertical, 72px horizontal (large). Provides a soft, neutral action without competing with the primary blue.

### Navigation Link Button

**Role:** Minimal button within navigation or content-dense areas

Background: transparent, Text: Midnight Ink (#00051a), No border, Border-radius: 0px, Padding: 20px vertical, 18-32.4px horizontal. Blends into content flow while maintaining clickability.

### Promotional Card

**Role:** Informational card with a strong background accent

Background: Royal Blue (#0f68ea) or Slate Blue (#000a3b), Border-radius: 28.8px, Padding: 86.4px around. Used for highlighting key features or offerings.

### Feature Card

**Role:** Neutral card for displaying features or content blocks

Background: White Smoke (#f0f2f4), Border-radius: 0px, Padding: 72px around. Creates distinct content sections.

### Ghost Card

**Role:** Minimal, borderless card for subtle grouping

Background: transparent, Border-radius: 0px, No shadow, No padding. Used for logo sections or subtle organizational breaks.

## Layout

The page maintains a centered max-width content area with generous vertical spacing between sections. The hero features a centered headline over a video, setting a dynamic yet contained tone. Content progresses through alternating sections that use either a White Canvas background or a White Smoke background, creating visual rhythm. Within sections, content often uses a split layout (text left, image/video right) or centered stacks. A ghost card grid is used for displaying client logos, maintaining a clean, spacious feel. The navigation is a sticky top bar, providing persistent access to key links.

## Imagery

Imagery primarily features product screenshots of the Planpoint UI, displayed within clean browser frames. These are often flat and isolated, focusing on functionality. Photography of real estate projects and logos of partner brands are also present, treated without special effects and integrated cleanly within sections. Icons are minimal, outlined, and monochromatic, primarily using Pewter Gray or Midnight Ink, occasionally accented by Sky Blue for interactive states. The overall density of imagery is balanced, serving an explanatory role rather than purely decorative.

## Dos & Donts

### Do

- Use Royal Blue (#0f68ea) exclusively for primary calls to action and significant interactive states.
- Apply 57.6px border-radius to all interactive buttons for a consistent pill-shaped appearance.
- Maintain a clear visual hierarchy by using Midnight Ink (#00051a) for primary text and Pewter Gray (#1d1d1f) for secondary text.
- Utilize White Smoke (#f0f2f4) for secondary page/card backgrounds to create subtle distinction and visual breaks.
- Ensure large headlines employ Inter with significant negative letter-spacing, e.g., -0.067em at 72px, to achieve a compact, statement-making effect.
- Use Sunburst Yellow (#ffcb00) sparingly for 'new' indicators or active status badges, never as a primary background or action color.
- Implement 72px padding for content cards and sections to create ample breathing room between content blocks.

### Don't

- Do not introduce new saturated colors outside of Royal Blue, Sky Blue, and Sunburst Yellow.
- Avoid using a border-radius less than 16px as the system relies on soft rounding for interactivity.
- Do not use generic system fonts; always specify Inter with its defined weights and letter-spacing values.
- Do not add additional box-shadows beyond the single defined rgba(0, 0, 0, 0.06) 0px 8px 48px 0px for elevated elements.
- Do not use Royal Blue (#0f68ea) for simple text links; Sky Blue (#007aff) is reserved for this purpose.
- Avoid tight spacing; maintain the defined element gaps and section padding to preserve the spacious feel.
- Do not make text links look like buttons; all text links should be a plain text color with no background or border.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
text: #00051a
background: #ffffff
border: #1d1d1f
accent: #0f68ea
primary action: #0f68ea (filled action)

**3-5 Example Component Prompts:**
1. Create a hero section with a centered headline: 'Accelerate your sales and rentals' using Inter 72px weight 700 with letter-spacing -4.82px, color Midnight Ink (#00051a). Add subtext 'with the leading interactive plan viewer;' using Inter 22px weight 400, color Midnight Ink (#00051a).
2. Design a Primary Action Button labeled 'Free trial': background Royal Blue (#0f68ea), text White Canvas (#ffffff), border-radius 57.6px, padding 14.4px vertical, 21.6px horizontal. Use Inter 16px weight 500.
3. Implement a Feature Card: background White Smoke (#f0f2f4), border-radius 0px, padding 72px. Inside, place a heading 'Easy to create & manage' using Inter 43px weight 700 letter-spacing -1.9px, color Midnight Ink (#00051a), and body text 'Made for any skill level' using Inter 18px weight 400, color Midnight Ink (#00051a).
4. Create a 'New' badge: background Sunburst Yellow (#ffcb00), text Midnight Ink (#00051a), border-radius 16px, padding 2px vertical, 10px horizontal. Use Inter 14px weight 500.

---
_Source: https://styles.refero.design/style/fc96be71-d71b-4fc1-a041-f13b3eae7dd5_
