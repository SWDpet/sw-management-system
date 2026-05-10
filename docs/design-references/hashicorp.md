# HashiCorp — Design Reference

> Deep Space Command Center. A dark, expansive digital environment where critical data is precisely displayed and interactive elements glow with focused purpose.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://hashicorp.com](https://hashicorp.com) |
| Refero page | [https://styles.refero.design/style/834ce97f-61f2-4b12-bf5c-e9fad2544456](https://styles.refero.design/style/834ce97f-61f2-4b12-bf5c-e9fad2544456) |
| Theme | dark |
| Industry | devtools |

## Overview

This system projects an aura of understated power and technical mastery, like a high-end server silently performing complex operations. Dark, expansive backgrounds (#0D0E12) provide an immersive canvas for crisp white typography (#EFEFF1), creating a high-contrast experience that is clear without being harsh. Strategic use of a vibrant blue (#2B89FF) and subtle internal gradients (#FF8791 to #F9B571, #6C81FF to #C08DFF) on data visualizations and interactive elements provides dynamic focal points against the deep, calm palette. Softly rounded corners (4px, 5px) on interactive elements add a subtle touch of approachability to an otherwise precise and rigorous aesthetic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Core | `#0D0E12` | neutral | Global page background, primary dark surface for most elements. |
| Carbon Gray | `#000000` | neutral | Border colors, text with less prominence than white, subtle button backgrounds. |
| Silver Text | `#EFEFF1` | neutral | Primary text color for headlines and body copy on dark backgrounds. |
| Ash Accent | `#B2B6BD` | neutral | Secondary text, input outlines, inactive icon color. |
| Slate Text | `#D5D7DB` | neutral | Tertiary text, navigation items, less prominent body copy. |
| Stone Gray | `#656A76` | neutral | Input placeholder text, subtle borders, descriptive secondary text. |
| Ghost Button | `#3B3D45` | neutral | Ghost button text and borders, less prominent icon colors. |
| Bright White | `#FFFFFF` | neutral | Active navigation backgrounds, primary button backgrounds. |
| Interactive Blue | `#2B89FF` | brand | Primary interactive elements: links, CTA buttons, active states, indicators. |
| Link Blue | `#2264D6` | brand | High-prominence links, distinct from standard interactive blue for specific use cases. |
| Accent Purple | `#42225B` | accent | Badge backgrounds, used for specific categorization or highlighting. |
| Cosmic Violet Gradient | `#6C81FF` | accent | Header background, internal card element fills — creates a sense of depth and energy. |
| Sunset Peach Gradient | `#FF8791` | accent | Internal card element fills, data visualization elements — provides a warm, dynamic visual contrast. |

## Typography

### system-ui

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 13px, 14px, 15px, 16px, 17px |
| lineHeight | 1.14, 1.20, 1.35, 1.38, 1.50, 1.60, 1.63, 1.69, 1.71 |
| letterSpacing | 0 |
| substitute | Inter |
| role | Default UI text, small body copy, navigation links, button labels. Its neutrality underpins the content-focused approach. |

### Hashicorp Sans

| Key | Value |
| --- | --- |
| weight | 600, 700 |
| sizes | 13px, 17px, 19px, 26px, 34px, 42px, 52px, 82px |
| lineHeight | 1.17, 1.18, 1.19, 1.21, 1.35, 1.69 |
| letterSpacing | -0.0100em, 0.1000em |
| fontFeatureSettings | 'kern' |
| substitute | Inter |
| role | Headlines and prominent marketing text. The tight letter spacing for larger sizes (-0.01em) provides a serious, professional edge, while looser spacing (0.10em) is used for specific uppercase treatments, creating a distinctive modern feel. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.20 |
| letterSpacing | 0 |
| substitute | Helvetica Neue |
| role | Input field text and some button text. A utilitarian font choice ensuring clarity in interactive forms. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.14 |  |
| body | 16 |  | 1.69 |  |
| subheading | 19 |  | 1.19 | -0.19 |
| heading | 34 |  | 1.21 | -0.34 |
| heading-lg | 42 |  | 1.19 | -0.42 |
| display | 82 |  | 1.17 | -0.82 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| input | 5px |
| badges | 5px |
| buttons | 5px |
| default | 4px |

- **elementGap** — 12px
- **sectionGap** — 64px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Base Surface | `#0D0E12` | 0 | Primary page background and foundational dark surface. |
| Elevated Component Surface | `#000000` | 1 | Used for some button backgrounds and areas requiring slightly more depth against the base. |

## Components

### Announcement Banner

### Button Group

### Resources Card List

### Primary Button

**Role:** cta

Background: #FFF, Text: #3B3D45, Border: rgba(59, 61, 69, 0.4), Radius: 5px, Padding: 6px 11px.

### Secondary Button

**Role:** secondary cta

Background: #FFF, Text: #3B3D45, Border: #000, Radius: 4px, Padding: 8px 12px.

### Ghost Link Button

**Role:** tertiary action

Background: transparent, Text: #D5D7DB, Border: #D5D7DB, Radius: 5px, Padding: 0px.

### Interactive Link Button

**Role:** interactive link

Background: transparent, Text: #2B89FF, Border: #2B89FF, Radius: 5px, Padding: 0px.

### Main Input Field

**Role:** form input

Background: #0D0E12, Text: #EFEFF1, Placeholder: #656A76, Border: #616875, Radius: 5px, Padding: 11px 11px. Focus ring: rgba(97, 104, 117, 0.1) 0px 1px 2px 1px inset.

### Badge

**Role:** categorization, tag

Background: #42225B, Text: #EFEFF1, Radius: 5px, Padding: 3px 7px.

### Hero Message Tag

**Role:** hero decorative

Background: #42225B, Text: #EFEFF1, Radius: 5px, Padding: 3px 7px. Used to highlight specific content or series announcements.

### Gradient Cube Card

**Role:** featured content card

Background: transparent, Border-radius: 0px, No box shadow. Imagery inside contains Cosmic Violet and Sunset Peach gradients.

## Layout

The layout primarily uses a max-width contained grid for content, though the hero section extends full-bleed with a dark, atmospheric background and a centrally aligned headline. Sections alternate between purely dark backgrounds (#0D0E12) and sections with subtle gradient imagery. Content is typically arranged in clear, organized blocks, often featuring a half-width text column paired with a half-width visual element, or structured as multi-column card grids for features. Vertical spacing between sections is generous and consistent. The header features a top bar navigation that becomes sticky, providing access to primary links and CTAs. The overall feel is spacious and information-dense without being cluttered.

## Imagery

This site features a blend of abstract 3D renders and technical illustrations. The renders often depict glowing, geometric forms (like the prominent cube) set against dark, atmospheric backgrounds, serving as decorative and conceptual hero imagery. These visuals use the Cosmic Violet and Sunset Peach gradients. Product-focused illustrations are clean, often showing network diagrams or subtle interface elements. Imagery is typically contained within sections or cards, rarely full-bleed, but forms dynamic focal points. Iconography is clean, outlined, and monochromatic, primarily in Silver Text (#EFEFF1) or Ash Accent (#B2B6BD), switching to Interactive Blue (#2B89FF) for active states. The imagery's role is primarily atmospheric and explanatory, reinforcing the technical and sophisticated brand identity.

## Dos & Donts

### Do

- Use Midnight Core (#0D0E12) as the primary background color for all main content areas.
- Apply Inter (system-ui) for all body text at 16px weight 400 with a line-height of 1.69, in Silver Text (#EFEFF1).
- Employ Hashicorp Sans at 82px weight 700 with letter-spacing -0.01em for all main display headings.
- Reserve the Interactive Blue (#2B89FF) for all primary interactive elements like call-to-action buttons and active links.
- Ensure input fields use a 5px border-radius and a #616875 border, with #0D0E12 background.
- Maintain a clear visual hierarchy using high contrast typography: #EFEFF1 on #0D0E12, and #3B3D45 on #FFFFFF.
- Use 5px border radius consistently for all buttons and badges for a unified interactive element feel.

### Don't

- Do not use box shadows for general content cards; maintain a flat, layered surface aesthetic for non-interactive elements.
- Avoid introducing additional saturated colors outside of the defined Brand and Accent groups; keep the palette disciplined.
- Do not use generic system fonts for display headings; Hashicorp Sans with its unique letter-spacing is crucial for brand identity.
- Disperse or combine interactive blue (#2B89FF) with neutral gray text; it must stand out clearly as an action indicator.
- Do not deviate from the specified type scale and line-heights; optical adjustments for headings are baked into Hashicorp Sans letter-spacing.

## Notes

### Agent Prompt Guide

## Quick Color Reference
- Text (primary): #EFEFF1
- Background (primary dark): #0D0E12
- CTA Button (background): #FFFFFF
- CTA Button (text): #3B3D45
- Interactive Link: #2B89FF
- Border (input): #616875

## Example Component Prompts
1. Create a Hero Section: Full-bleed background using Cosmic Violet Gradient. Centered headline 'Do Cloud Right' in Hashicorp Sans, 82px, weight 700, #EFEFF1, letter-spacing -0.01em. Subtext 'Take a unified approach...' in Inter (system-ui), 17px, weight 400, #D5D7DB, line-height 1.69. Below, a Primary Button with text 'Meet The Infrastructure Cloud'.
2. Design a Feature Card: 50% width on desktop. Background #0D0E12. Headline 'A smarter approach to infrastructure operations' in Hashicorp Sans, 34px, weight 700, #EFEFF1, letter-spacing -0.01em. Body text in Inter (system-ui), 16px, weight 400, #D5D7DB, line-height 1.69. Include an Interactive Link Button 'Learn more'. Place a 3D gradient cube image alongside the text, utilizing the Cosmic Violet Gradient and Sunset Peach Gradient.
3. Create a Navigation Bar: Fixed at top. Background #0D0E12, height 64px. Logo and main navigation links (Inter (system-ui), 16px, weight 500, #D5D7DB). Right-aligned Secondary Button 'Log In' and Primary Button 'Get started'.
4. Develop a Badge Component: Background #42225B, text 'Do Cloud Right, Explained' in Inter (system-ui), 13px, weight 400, #EFEFF1. Padding 3px 7px, border-radius 5px. Position alongside a heading.

---
_Source: https://styles.refero.design/style/834ce97f-61f2-4b12-bf5c-e9fad2544456_
