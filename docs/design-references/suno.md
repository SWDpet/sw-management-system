# Suno — Design Reference

> Dark studio, neon pulses. An expansive, dim canvas where vibrant light flickers to life.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://suno.com](https://suno.com) |
| Refero page | [https://styles.refero.design/style/3b5ff57f-a371-4c7f-82ae-5fc5485da073](https://styles.refero.design/style/3b5ff57f-a371-4c7f-82ae-5fc5485da073) |
| Theme | dark |
| Industry | ai |

## Overview

Suno's design evokes a dark, pulsating club atmosphere, where digital magic happens. The expansive, low-contrast color palette, punctuated by fleeting, vivid neon accents, creates a sense of cutting-edge technology merging with artistic expression. Subtle dark gradients on backgrounds and elevated elements add depth without sharp divisions. White, bold typography against these dark canvases commands attention, establishing a stage for creative output.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Black | `#101012` | neutral | Page backgrounds, deeply recessed surfaces, primary text on light elements. |
| Void Black | `#17171a` | neutral | Subtle background for card elevations or grouped content, creating hierarchy within dark surfaces. |
| Ghost White | `#f7f4ef` | neutral | Primary text color for headlines and body copy on dark backgrounds, selected interactive elements. |
| Pure White | `#ffffff` | neutral | Strongest contrast for key headings, input text, and active states, providing distinct visual pop. |
| Graphite | `#000000` | neutral | Text on light backgrounds (e.g. 'Sign Up' button), icon fills. |
| Ash Gray | `#c2c2c1` | neutral | Minor background tint or subtle borders. |
| Muted Steel | `#a3a3a3` | neutral | Subtle icons and secondary UI elements, creating a desaturated feel to non-critical information. |
| Sunset Yellow | `#f5d907` | accent | Icons and occasional decorative elements, appearing as a point of visual interest against the dark theme. |
| Vivid Pink | `#fd429c` | brand | Used sparingly for interactive elements, such as the 'Create' button, indicating primary action with an energetic glow. |
| Electric Green | `#02d95c` | accent | Small interactive elements like icons or indicators, signaling status or a secondary action without overwhelming the primary palette. |

## Typography

### Neue Montreal

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| weights | 300, 400, 500 |
| sizes | 10px, 11px, 12px, 13px, 14px, 15px, 16px, 17px, 18px, 20px, 24px, 40px, 72px, 140px |
| lineHeight | 0.87, 0.89, 1.00, 1.14, 1.20, 1.27, 1.33, 1.40, 1.41, 1.43, 1.50, 1.55, 1.60, 1.63, 1.78 |
| letterSpacing | -0.0260em at display sizes, to 0.0000em at body sizes |
| substitute | Inter |
| role | Primary typeface for all UI elements, headings, body text. Its modern, sans-serif structure holds legibility across a wide range of sizes and weights, supporting both robust headlines and subtle metadata. Tightly tracked at large sizes for a contemporary feel. |

### Editorial New

| Key | Value |
| --- | --- |
| weight | 300 |
| weights | 300 |
| sizes | 24px |
| lineHeight | 1.50 |
| letterSpacing | normal |
| substitute | Playfair Display |
| role | Used sparingly for specific heading elements at 24px, weight 300, it provides a light, elegant contrast to the primary sans-serif, adding a touch of sophisticated flair without sacrificing modernity. Its unique character functions as an occasional alternative voice. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.78 |  |
| button | 16 |  | 1.5 | -0.48 |
| subheading | 24 |  | 1.5 |  |
| heading | 40 |  | 1.2 |  |
| heading-lg | 72 |  | 1.14 | -1.44 |
| display | 140 |  | 0.89 | -3.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 12px |
| pills | 16777200px |
| small | 6px |
| images | 12px |
| inputs | 12px |
| buttons | 6px |
| default | 6px |

- **elementGap** — 4-16px
- **sectionGap** — 100-150px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Page Base | `#101012` | 0 | The foundational background for the entire page, providing a deep, dark canvas. |
| Card Surface | `#17171a` | 1 | Used for card backgrounds and grouped content, visually lifting elements slightly from the base with a subtle darker tone. |
| Interactive Overlay | `#00000040` | 2 | A semi-transparent dark shade used for interactive elements like input containers, creating a sense of depth and interactivity over the base. |

## Components

### Music Prompt Input

### Song Card

### Button Group

### Primary Ghost Button

**Role:** Action button

Background transparent, text #F7F4EF. Used for secondary actions like 'Sign In' or 'Advanced'. Displays a subtle inner shadow on hover/focus that reveals a light tint.

### Pill Ghost Button

**Role:** Filter/Tag button

Background oklab(0.999994 0.0000455678 0.0000200868 / 0.05), text #F7F4EF, with an extremely large border radius (16777200px) creating a pill shape. Used for tags or subtle navigation.

### Primary Filled Button

**Role:** Main CTA Button

Solid #F7F4EF background with #101012 text and 16777200px border radius, 8px padding vertical, 16px horizontal. Used for primary calls to action like 'Sign Up'.

### Interactive Input Field

**Role:** Text input

Background transparent, text #FFFFFF, border radius 12px. Used for user input fields, placeholder text is typically #A3A3A3.

### Create Action Button

**Role:** Primary interactive button with gradient

Background linear-gradient(90deg, #fd429c, #f5d907), text #F7F4EF, border radius 6px. Central interactive element for content generation, featuring a vibrant gradient to draw focus.

### Prompt Input Container

**Role:** Input wrapper

Background oklab(0.173939 0.00113384 -0.00393198 / 0.25) rgba(0,0,0,0.25), text #FCFBF9, border-top rgba(255,255,255,0.1), border-radius 6px, padding 4px vertical, 8px horizontal. Encapsulates input fields for music creation.

## Layout

The page primarily employs a full-bleed layout with content sections centered, but without a strict `pageMaxWidth` constraint, giving a sense of expansiveness. The hero section is full-bleed, featuring a dominant, centrally aligned headline on a dark, subtly textured gradient background, with interactive elements positioned below. Subsequent sections often feature a mix of centered stacked content and fluid arrangements of cards or media, sometimes flowing freely across the width. There's a subtle rhythm created by changes in background tint or the introduction of card-based content, rather than strong visual dividers. Navigation is handled by a minimal top bar with 'Sign In' and 'Sign Up' buttons.

## Imagery

The visual language for imagery is characterized by contained, slightly rounded media cards displaying diverse content such as album art or user-generated visualizers. These are often dark-toned or moody, showcasing a mix of abstract and human elements, implying artistic creation. The treatment involves a 12px border radius, with elements appearing in a somewhat scattered, dynamic arrangement rather than a rigid grid, reinforcing a creative, less formal atmosphere. The images serve a primarily functional and atmospheric role, decorating the dark interface and providing visual context to the AI-generated music tracks.

## Dos & Donts

### Do

- Prioritize text legibility on dark backgrounds by using Ghost White (#F7F4EF) or Pure White (#FFFFFF) for body and headline text.
- Use Pitch Black (#101012) as the primary page background to maintain the dark, atmospheric tone across all sections.
- Employ the 6px border radius for all interactive elements like buttons and input containers to ensure a consistent, slightly softened aesthetic.
- Apply the gradient from Vivid Pink (#FD429C) to Sunset Yellow (#F5D907) exclusively for key 'Create' actions to highlight primary functionality.
- Maintain generous vertical spacing between sections (100-150px) to provide breathing room and prevent visual clutter on the dark canvas.
- Use Neue Montreal at a weight of 300 or 400 for most text, reserving 500 for emphasized elements, without overusing bolding.
- Ensure interactive elements like buttons have a minimum padding of 4px vertical and 8px horizontal, or 8px vertical and 16px horizontal for primary CTAs.

### Don't

- Avoid using bright, saturated colors for large background areas or extensive text blocks, as this will disrupt the intended dark and low-key aesthetic.
- Do not introduce hard shadows or strong elevation effects that could break the subtle, layered depth created by varying dark tones.
- Refrain from deviating from the established border radii; maintain 6px for buttons and 12px for cards/inputs.
- Do not use more than one distinct primary brand color in any single section to avoid visual competition and maintain focus on the content.
- Do not use generic system fonts; Neue Montreal is critical to the brand's typographic identity.
- Avoid excessive use of the Editorial New typeface; it is a decorative accent for specific headings, not a general body or UI font.
- Do not introduce stark white backgrounds for entire sections, as the design system is fundamentally dark-themed with occasional light elements for high contrast.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Text Primary (dark bg):** #F7F4EF
- **Text Secondary (light bg):** #101012
- **Background Primary:** #101012
- **CTA Gradient:** linear-gradient(90deg, #fd429c, #f5d907)
- **Button Text (Primary):** #101012
- **Accent Icon:** #f5d907

### 3-5 Example Component Prompts
1. **Create a Hero Section**: Background #101012, with a subtle gradient texture. Centered headline 'Make a house song about quitting your job' using Neue Montreal, 72px, weight 500, #F7F4EF, letter-spacing -1.44px. Subtext 'Start with a simple prompt...' Neue Montreal, 24px, weight 400, #F7F4EF, line-height 1.5. Below, a text input 'Chat to make music' with transparent background, #FFFFFF text, 12px radius, and a 'Create' button with background linear-gradient(90deg, #fd429c, #f5d907), #F7F4EF text, 6px radius, 4px vertical 8px horizontal padding.
2. **Generate a Ghost Button**: 'Sign In' ghost button: background transparent, text #F7F4EF, Neue Montreal, 15px, weight 400, 6px border-radius, 4px vertical 8px horizontal padding.
3. **Design a Media Card**: Background #17171a, 12px border radius, with an image inside that also has a 12px radius. Title text 'Moth Among the Stars (Live)' Neue Montreal, 15px, weight 400, #F7F4EF. Small text '@ SunoUser' Neue Montreal, 12px, weight 300, #A3A3A3. Include a play icon using #F5D907.
4. **Create a Filled Primary Button**: 'Sign Up' button: background #F7F4EF, text #101012, Neue Montreal, 16px, weight 500, 16777200px border-radius, 8px vertical 16px horizontal padding.

---
_Source: https://styles.refero.design/style/3b5ff57f-a371-4c7f-82ae-5fc5485da073_
