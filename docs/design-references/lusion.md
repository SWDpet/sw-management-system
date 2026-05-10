# Lusion — Design Reference

> Computational Laboratory Blueprint — high-contrast text on bright surfaces, accented by vivid blue and lime green.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://lusion.co](https://lusion.co) |
| Refero page | [https://styles.refero.design/style/1b44386e-31a8-40b0-a577-27c088b51264](https://styles.refero.design/style/1b44386e-31a8-40b0-a577-27c088b51264) |
| Theme | light |
| Industry | agency |

## Overview

This design system conjures a computational laboratory aesthetic, balancing stark functionality with precise, vivid accents. The dominant bright off-white surfaces provide a clean canvas for sharp black typography, creating a high-contrast, information-focused layout. Carefully placed vivid blue and lime green appear in interactive elements, like focused indicators or calls to action, injecting focused energy against the predominantly neutral backdrop. Subtle elevation and generous border radii on active components soften the underlying hard-edged forms, implying a blend of digital precision and user-friendly interaction.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Black | `#000000` | neutral | Primary text, critical UI elements, button text on light-colored buttons. |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, button text on dark buttons. |
| Whisper Off-White | `#f0f1fa` | neutral | Input fields, secondary backgrounds, subtle background differentiation. |
| Storm Gray | `#2b2e3a` | neutral | Background for primary interactive elements, like the 'Let's Talk' button. |
| Button White | `#e4e6ef` | neutral | Secondary button backgrounds, offering a slightly muted white option. |
| Dark Surface | `#121416` | neutral | Base background for sections where a dark context is required, likely for visuals. |
| Deep Space Blue | `#1a2ffb` | accent | Interactive elements, active states, borders around focused components. This vivid blue serves as the primary accent, signaling interaction. |
| Electric Lime | `#c1ff00` | accent | Highlights, specific attention-grabbing elements. Its lower prominence suggests a secondary accent, used sparingly. |

## Typography

### Aeonik

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| sizes | 12px, 13px, 14px, 16px, 18px, 20px, 22px, 26px, 36px, 38px, 43px, 49px, 50px, 108px, 115px, 144px |
| lineHeight | 0.90, 1.00, 1.10, 1.15, 1.20, 1.40, 1.50 |
| letterSpacing | -0.02 |
| substitute | system-ui (sans-serif) |
| role | All textual content: headings, body text, buttons, links, and inputs. The consistent application of Aeonik across all roles contributes to the system's coherent, slightly technical feel, with negative letter spacing adding to the precise aesthetic. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.48 |
| body-sm | 14 |  | 1.5 | -0.48 |
| body | 16 |  | 1.5 | -0.48 |
| subheading | 22 |  | 1.4 | -0.48 |
| heading | 50 |  | 1 | -0.48 |
| heading-lg | 108 |  | 0.9 | -0.48 |
| display | 144 |  | 0.9 | -0.48 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 15px |
| inputs | 18px |
| buttons | 87.5px |
| pillForms | 100px |

- **elementGap** — 13px
- **sectionGap** — 
- **cardPadding** — 25px
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Hero Tagline with Scroll Bar

### Contact Form Input Card

### Primary Action Button

**Role:** Primary Call to Action

Rounded pill-shaped button with 'Storm Gray' background (#2b2e3a), 'Canvas White' text (#ffffff). Uses 87.5px border-radius, with 22.75px left padding and 15.75px right padding for specific visual balance, and no vertical padding.

### Secondary Action Button

**Role:** Secondary Call to Action

Rounded pill-shaped button with 'Button White' background (#e4e6ef), 'Absolute Black' text (#000000). Uses 87.5px border-radius, with 22.75px left padding and 15.75px right padding for specific visual balance, and no vertical padding.

### Tertiary Button

**Role:** Further Actions / Navigation

Rounded pill-shaped button with 'Canvas White' background (#ffffff), 'Absolute Black' text (#000000). Uses 76.5px border-radius, with 21.6px horizontal padding and 14.4px vertical padding.

### Text Input Active

**Role:** User input fields

Input field with 'Whisper Off-White' background (#f0f1fa), 'Absolute Black' text (#000000). Features an 18px border-radius and 25px uniform padding. Border color is #000000 at 0px width.

### Interactive Card Container

**Role:** Content grouping

Container with a 15px border-radius, likely on a 'Canvas White' background or 'Storm Gray'. Features a subtle shadow: rgba(0, 0, 0, 0.04) 0px 6px 10px 0px, rgba(0, 0, 0, 0.04) 0px 2px 4px 0px, suggesting an interactive or elevated state.

## Layout

The site uses a max-width contained layout, not full-bleed, with ample horizontal padding visible through the main content area. The hero section features 3D abstract graphics centrally aligned, framed by a card-like container with rounded corners. The overall section rhythm appears to alternate between bright white backgrounds for textual content and darker backgrounds for visual elements. Content arrangement leans towards centered stacks for headlines and calls to action, with text and potentially visuals arranged in a balanced, open manner. Navigation is a top bar with clear 'LET'S TALK' and 'MENU' buttons, maintaining ample space.

## Imagery

The site uses hero section imagery dominated by 3D abstract renders: interlocking shapes in white, black, and 'Deep Space Blue' against a dark background. These are contained within a main content area, with rounded corners (15px radius) rather than full-bleed. The role of these visuals is primarily decorative atmosphere and brand showcase, indicating expertise in 3D and interactive storytelling, rather than explanatory content. Image density is high in the hero, but otherwise the site appears to be text-dominant.

## Dos & Donts

### Do

- Prioritize 'Absolute Black' (#000000) for all primary body text, headings, and critical UI labels.
- Use 'Canvas White' (#ffffff) as the default background for main content areas and cards, creating a bright, spacious canvas.
- Apply 'Deep Space Blue' (#1a2ffb) as the primary accent color, specifically for interactive elements, links, and focused states.
- Ensure all buttons use an 87.5px border-radius unless specified, maintaining a soft, pill-like form.
- Utilize Aeonik at `weight: 500` for headings and important UI labels, combined with a negative letter-spacing for a modern, compact feel.
- Enforce a base padding unit of 16px around interactive elements like buttons and 25px for inputs to ensure ample breathing room.
- Introduce 'Whisper Off-White' (#f0f1fa) specifically for less prominent interactive elements or background differentiation in forms.

### Don't

- Avoid using multiple accent colors in close proximity; 'Deep Space Blue' and 'Electric Lime' should be used sparingly and distinctly.
- Do not deviate from the specified negative letter-spacing for Aeonik, as it is a core characteristic of the typographic identity.
- Do not use sharp corners for interactive elements; maintain the consistent application of generous border radii for buttons and inputs.
- Avoid generic or default shadow values; use the specified shadow `rgba(0, 0, 0, 0.04) 0px 6px 10px 0px, rgba(0, 0, 0, 0.04) 0px 2px 4px 0px` for subtle elevation.
- Do not introduce highly saturated primary colors beyond 'Deep Space Blue' and 'Electric Lime' without specific justification.
- Avoid dense information blocks; maintain generous inter-element and inter-section spacing, aligning with the spacious density.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- Text: #000000
- Background: #ffffff
- CTA: #2b2e3a
- Border: #1a2ffb (for active/focus)
- Accent: #1a2ffb

Example Component Prompts:
1. Create a hero section with a "Canvas White" (#ffffff) background. Include a headline using Aeonik weight 500, size 144px, line-height 0.9, letter-spacing -0.02em, color "Absolute Black" (#000000). Below it, place a "Primary Action Button".
2. Build a card with a 'Canvas White' background, 15px border-radius, and the standard button/card shadow. Inside, include descriptive text using Aeonik weight 400, size 16px, line-height 1.5, color 'Absolute Black' (#000000).
3. Design a form input field using the 'Text Input Active' component specifications: 'Whisper Off-White' (#f0f1fa) background, 18px border-radius, 25px uniform padding. The placeholder text should be Aeonik weight 400, size 16px, color 'Absolute Black' (#000000) with 50% opacity.
4. Generate a navigation bar item: Text 'Home' with Aeonik weight 500, size 16px, color 'Absolute Black' (#000000), letter spacing -0.02em. On hover, apply 'Deep Space Blue' (#1a2ffb) as the text color.

---
_Source: https://styles.refero.design/style/1b44386e-31a8-40b0-a577-27c088b51264_
