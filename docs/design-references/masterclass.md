# MasterClass — Design Reference

> Midnight Stage Presence

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.masterclass.com](https://www.masterclass.com) |
| Refero page | [https://styles.refero.design/style/4367b4cd-b002-4719-a418-cbce020f0d33](https://styles.refero.design/style/4367b4cd-b002-4719-a418-cbce020f0d33) |
| Theme | dark |
| Industry | media |

## Overview

MasterClass employs a dark, cinematic UI, reminiscent of a premium streaming platform. Dominant blacks and deep charcoals create a sophisticated environment, allowing vibrant accents to punctuate interactive elements and brand moments. Typography is bold and confident, commanding attention within the high-contrast setting. Component surfaces are subtle, often inset, maintaining a flat aesthetic that emphasizes content and celebrity figures over overt ornamentation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Black | `#000000` | neutral | Primary background for footers and expansive content sections, subtle borders, text for badges |
| Charcoal Canvas | `#222326` | neutral | Default page background, card backgrounds |
| Graphite Base | `#0d0d0e` | neutral | Background for subtle surface differentiation, text buttons, and darker borders |
| Deep Slate | `#272c33` | neutral | Card backgrounds, button backgrounds for secondary actions |
| Subtle Ash | `#191c21` | neutral | Secondary background color for body sections |
| Muted Stone | `#211d0d` | neutral | Text color for muted elements, borders on certain components |
| Iron Gray | `#43454c` | neutral | Button backgrounds, inset shadow on interactive elements |
| Silver Mist | `#9ea0a9` | neutral | Text color for secondary information, muted icons, disabled states |
| Light Steel | `#d4d5d9` | neutral | Subtle body text color, borders |
| Pure White | `#ffffff` | neutral | Primary text color, active states, badge backgrounds, clear button borders |
| Ghostly Gray | `#f4f4f5` | neutral | Hover states for text and icons, borders for ghost buttons, light text |
| Action Raspberry | `#e32652` | accent | Primary action buttons, prominent icons — a vibrant pop against dark backgrounds for conversion |
| Interactive Lime | `#dcff00` | accent | Green action color for filled buttons, selected navigation states, and focused conversion moments. Use as a supporting accent, not as a status color |
| Highlight Gold | `#eed37f` | accent | Accent borders for interactive cards or highlighted content, decorative elements |
| Subtle Cadet | `#596170` | neutral | Background for small, less prominent interactive elements |

## Typography

### Sohne

| Key | Value |
| --- | --- |
| weight | 400, 600 |
| weights | 400, 600 |
| sizes | 8px, 12px, 14px, 16px, 20px, 22px, 24px, 32px, 48px |
| lineHeight | 1.00, 1.25, 1.33, 1.45, 1.50, 1.60, 2.50 |
| letterSpacing | 0.0100em at 8px, 0.0120em at 12px, 0.0200em at 14px, 0.0230em at 16px, 0.0270em at 20px, 0.0300em at 22px, 0.0390em at 24px |
| substitute | Inter |
| role | Primary typeface for all body text, links, buttons, and most headings up to 48px. Its slightly condensed yet approachable nature supports information density without feeling cramped. |

### Sohne Schmal

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 64px, 80px |
| lineHeight | 0.85, 0.90 |
| letterSpacing | -0.0100em at 64px, 0.0100em at 80px |
| substitute | condensed sans-serif like Oswald or Anton |
| role | Used for large, impactful display headlines, where its 'schmal' (narrow) characteristic allows for bold statements in restricted vertical space. The tight letter-spacing adds to its direct, attention-grabbing feel. |

### Ivar Display Condensed

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 64px |
| lineHeight | 1.10 |
| letterSpacing | 0.0050em |
| substitute | Condensed serif like Playfair Display SC or IBM Plex Serif |
| role | An alternative serif for display headings, adding a touch of classic authority and gravitas to select feature titles. Its condensed form maintains visual economy typical of the system. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.45 |  |
| body-sm | 14 |  | 1.45 |  |
| body | 16 |  | 1.45 |  |
| body-lg | 20 |  | 1.45 |  |
| heading-sm | 22 |  | 1.45 |  |
| heading | 24 |  | 1.3 |  |
| heading-lg | 32 |  | 1.3 |  |
| display-sm | 48 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| links | 8px |
| badges | 20px |
| images | 4px |
| inputs | 0px |
| buttons | 8px |

- **elementGap** — 16px
- **sectionGap** — 64px
- **cardPadding** — 48px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Pitch Black Base | `#000000` | 0 | Expansive background for footer and full-width sections. |
| Charcoal Canvas | `#222326` | 1 | Primary page background and default card background. |
| Deep Slate Cards | `#272c33` | 2 | Elevated card surfaces and specific input containers. |
| Graphite Modules | `#0d0d0` | 3 | Background for navigation tabs and interactive modules. |

## Components

### Ghost Navigation Button

**Role:** Navigation and secondary actions that need to remain subtle.

backgroundColor: rgba(0,0,0,0), color: #f4f4f5, border: #f4f4f5 1px solid, radius: 4px, padding: 16px.

### Icon Button (filled)

**Role:** Functional icons that require a background.

backgroundColor: #596170, color: #ffffff, radius: 4px, padding: 0px.

### Navigation Tab Button

**Role:** Top-level navigation items or filters.

backgroundColor: #0d0d0, color: #9ea0a9, radius: 8px, padding: 12px 16px.

### Flat Interactive Input

**Role:** Search bars and form fields.

backgroundColor: rgba(0,0,0,0), color: #ffffff, border: #ffffff 1px solid, radius: 0px, padding: 0px.

### Hero Checkbox/Radio Button

**Role:** Choices within hero sections.

backgroundColor: #272c33, color: #ffffff, border: #ffffff 1px solid, radius: 0px, padding: 12px 16px.

### Primary Action Button

**Role:** High-priority calls to action.

backgroundColor: #e32652, color: #ffffff, radius: 8px, padding: 12px 16px.

### Secondary Action Button

**Role:** Prominent actions, but not primary conversion.

backgroundColor: #222326, color: #ffffff, radius: 0px, padding: 12px 16px.

### Content Feature Card

**Role:** Containers for featured content with generous padding.

backgroundColor: #272c33, radius: 8px, padding: 48px 96px.

### Visual Content Card

**Role:** Cards for showcasing visual content (e.g., instructors, classes).

backgroundColor: #222326, radius: 12px, padding: 0px.

### Informational Badge

**Role:** Small, informative labels.

backgroundColor: #ffffff, color: #000000, radius: 20px, padding: 4px 12px.

## Layout

The page primarily utilizes a max-width contained layout where content is centered, often with dynamic full-bleed hero sections. The hero frequently employs a dark background with a prominent, large headline and supporting text, flanked by high-impact instructor imagery. Sections alternate between full-width black backgrounds and slightly lighter charcoal surfaces, maintaining strong vertical rhythm with generous section gaps (64px). Content is arranged in alternating text-left/image-right patterns, centered stacks for feature declarations, and horizontal scrolling carousels or grids for content browsing. Navigation is a sticky top bar with a stark dark background, featuring minimalist text links and a prominent Action Raspberry call-to-action button.

## Imagery

The visual language is dominated by high-quality, often dramatic portrait photography of celebrity instructors. Images are typically tightly cropped, focusing on the individual's face or upper body, and serve a functional purpose of personalizing content rather than purely decorative. Photography is often full-bleed within sections or contained within cards with large radii (8px or 12px), creating a gallery-like feel. Iconography is minimalist, either outlined or filled, primarily in Pure White or Silver Mist, with occasional pops of Interactive Lime or Action Raspberry for status or interaction. There is a strong emphasis on product showcase – the instructors and their content are the primary visual elements.

## Dos & Donts

### Do

- Prioritize Pitch Black (#000000) for large background areas and Charcoal Canvas (#222326) for primary surfaces to maintain the dark theme.
- Use Action Raspberry (#e32652) exclusively for primary calls to action, maintaining its vivid impact.
- All interactive elements will have a minimum border-radius of 4px, with important buttons using 8px.
- Utilize Sohne Schmal at 64px or 80px for monumental headings with tight negative letter-spacing for dramatic effect.
- Employ consistent 4px padding on badges and 16px horizontal padding for most buttons.
- Ensure input fields have a 1px #ffffff border and a 0px radius for a stark, integrated look.
- Use the inset box-shadow `rgb(148, 154, 164) 0px 0px 0px 2px inset` for selected or active body elements to indicate focus.

### Don't

- Do not introduce light backgrounds for core content sections; the system is strictly dark-mode dominant.
- Avoid using multiple chromatic colors close together; allow Action Raspberry (#e32652) to stand out as the primary accent.
- Do not use generic system fonts; always specify Sohne for body and most headings, Sohne Schmal or Ivar Display Condensed for display.
- Do not deviate from the established spacing scale (multiples of 4px) to maintain consistent density.
- Do not use standard button shadows; rely on inset borders or subtle color shifts for interaction states.
- Avoid large imagery with distracting backgrounds; prioritize portraits or product shots with minimal context.
- Do not use highly rounded corners (e.g., >20px) on main cards or primary buttons, as this clashes with the structured feel.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #222326
border: #ffffff
accent: #e32652
primary action: #e32652 (filled action)

Example Component Prompts:
1. Create a Primary Action Button: #e32652 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a browsing filter button: Graphite Base background, Sohne, 14px, weight 600, #9ea0a9, letter-spacing 0.0200em. Radius 8px, padding 12px 16px. Example text: 'Business & Entrepreneurship'.
3. Construct a content card: Deep Slate background, radius 8px. Inside, for example, an instructor image (radius 12px) followed by a headline 'PHOTOGRAPHY' in Sohne, 24px, weight 600, #ffffff, line-height 1.33. The card should have 48px padding on all sides.

---
_Source: https://styles.refero.design/style/4367b4cd-b002-4719-a418-cbce020f0d33_
