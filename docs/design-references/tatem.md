# Tatem — Design Reference

> Midnight Terminal, cool and precise. This visual evokes a dark, command-line interface with subtle interactive glows.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://tatem.com](https://tatem.com) |
| Refero page | [https://styles.refero.design/style/cb6e4ab0-b8fe-45b0-bd22-6339b073e26d](https://styles.refero.design/style/cb6e4ab0-b8fe-45b0-bd22-6339b073e26d) |
| Theme | dark |
| Industry | productivity |

## Overview

Tatem's design offers a sophisticated and focused dark-mode experience, reminiscent of a command line interface or focused developer tool. Its visual atmosphere is calm, quiet, and highly systematic. The primary visual impression is created by a clean, neutral dark background punctuated by purposeful text and subtle interactive elements. Very sparing use of color, primarily for status or accent in a muted blue, maintains serious functionality while avoiding distraction. The design prioritizes readability and information hierarchy through careful typographic sizing and achromatic value contrasts.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Twilight Ink | `#000000` | neutral | Page backgrounds, section backgrounds, primary dark neutral. |
| Polar White | `#ffffff` | neutral | Primary text, interactive elements in light themes (though used sparingly here), header text in dark mode for high contrast. |
| Pewter Mist | `#919191` | neutral | Secondary text, subtle borders, inactive states, icon strokes. Creates visual hierarchy by reducing prominence. |
| Silver Tone | `#b5b5b5` | neutral | Muted headings and body text, providing a slightly softer contrast against dark backgrounds than pure white. |
| Obsidian Grey | `#606060` | neutral | Tertiary text, subtle backgrounds for elements needing slight separation, borders. |
| Charcoal Black | `#3b3b3b` | neutral | Subtle card or element backgrounds, dividers, deeper shadows on light elements. |
| Mist Grey | `#c2c2c2` | neutral | Hover states on neutral elements, subtle accent backgrounds. |
| Cerulean Accent | `#007eed` | accent | Interactive elements, links, active states, indicators. This vivid blue is the primary functional highlight. |
| Sky Gradient | `#5a7694` | brand | Hero section background, establishing a cool, expansive feel for the product. Its gentle progression from deeper blue-grey to lighter grey-blue contributes significantly to the site's calm atmosphere. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 13px, 14px, 16px, 20px, 40px |
| lineHeight | 1.20, 1.43, 1.50 |
| letterSpacing | -0.007 |
| substitute | system-ui, sans-serif |
| role | All textual content, embodying clarity and directness without visual distraction. The consistent medium weight across sizes supports a flat, functional aesthetic. Letter spacing is subtly tight for a compact feel while maintaining readability. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.5 | -0.1 |
| body | 16 |  | 1.5 | -0.1 |
| subheading | 20 |  | 1.2 | -0.14 |
| display | 40 |  | 1.2 | -0.28 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| large | 10px |
| default | 6px |
| hero_element | 16px |

- **elementGap** — 8px
- **sectionGap** — 28px
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### CTA Button Group

### Feature Section — Split Inbox

### Keyboard Shortcuts Section

### Primary Ghost Button

**Role:** Call to action, navigation items.

Background: rgba(252, 252, 252, 0.08), Text: rgba(255, 255, 255, 0.95), Border: rgba(255, 255, 255, 0.06). Radius: 8px. Padding: 10px 16px. This button style provides an ethereal, translucent feel that integrates well with the dark theme and gradient hero.

### Navigation Link Button

**Role:** Secondary actions, internal navigation.

Background: rgba(0, 0, 0, 0) (transparent), Text: rgba(255, 255, 255, 0.55), Border: rgba(255, 255, 255, 0.06). Radius: 0px. Padding: 0px. Used for subtle, text-based interactions that do not require strong visual emphasis, blending seamlessly into the interface.

### Accent Label

**Role:** Categorization, status indicators.

Text color: #007eed, Background: transparent. Font: Inter 13px weight 400. Letter spacing -0.1px. Used to highlight classifications with the Cerulean Accent color without adding background bulk.

### Section Divider

**Role:** Visual separation between content blocks.

Height: 1px, Background: #3b3b3b. Provides subtle structural cues in a dark environment without harsh lines.

## Layout

The page primarily uses a full-bleed layout for background elements, with content contained within implied horizontal margins, giving it an expansive feel. The hero section is full-bleed with the Sky Gradient background, featuring a centered headline and button group over the abstract product visual. Subsequent sections mostly alternate between full-width black backgrounds and contained content. Content arrangement is primarily stacked, with some sections using a text-left/visual-right pattern, maintaining relatively even vertical spacing. There are no obvious grid patterns beyond simple stacked blocks. The navigation is a sticky top bar, minimal and semi-transparent on the hero, becoming opaque black on scroll, emphasizing content over persistent chrome.

## Imagery

The site uses a mix of subtle product illustrations and abstract, atmospheric graphics. The hero section features a stylized, translucent rendering of an email interface, presented in an isometric, floating perspective within the Sky Gradient. Further down, product illustrations, such as the keyboard visual, are flat, monochromatic, and contained within subtly rounded dark shapes. These visuals are explanatory rather than decorative, showcasing product features with a clean, almost diagrammatic quality. Icons are minimal, outlined, and monochromatic, primarily using Pewter Mist (#919191), serving a functional role without visual excess. The overall density of imagery is low, with text remaining dominant.

## Dos & Donts

### Do

- Use Inter weight 400 for all text to maintain visual consistency and clarity.
- Apply Twilight Ink (#000000) for primary page backgrounds, anchoring the dark theme.
- Use Polar White (#ffffff) for primary headlines and significant UI text against dark backgrounds.
- Utilize Cerulean Accent (#007eed) exclusively for interactive elements like links and active states to signal interactivity.
- Maintain a primary border radius of 6px for most interactive elements and a larger 8px for buttons, with 16px for unique hero elements.
- Employ a base spacing unit of 8px for element gaps and 28px for vertical section separation.
- Integrate the Sky Gradient (linear-gradient(rgb(90, 118, 148), rgb(171, 183, 181))) for large, atmospheric hero backgrounds.

### Don't

- Avoid introducing additional saturated colors beyond the established Cerulean Accent and its subtle variations to maintain the focused atmosphere.
- Do not use heavy shadows or artificial light sources; rely on value contrast and subtle borders for depth.
- Do not vary font weights or families; Inter 400 is the only typeface for this system.
- Avoid busy backgrounds or textures in content areas; stick to solid dark neutrals or the approved gradient.
- Do not use overly expressive or decorative typography; the system prioritizes functional readability.
- Steer clear of radii smaller than 6px for interactive components, and avoid overly sharp, unrounded edges where a softer form is intended.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (primary): #ffffff
- Background (primary): #000000
- CTA (text): rgba(255, 255, 255, 0.95)
- CTA (background): rgba(252, 252, 252, 0.08)
- Accent: #007eed

### Example Component Prompts
1. Create a hero section: full-bleed gradient background (linear-gradient(rgb(90, 118, 148) 0%, rgb(171, 183, 181) 100%)). Centered headline 'The email experience you've been waiting for.' at 40px Inter weight 400, #ffffff, letter-spacing -0.28px. Below it, a button with background rgba(252, 252, 252, 0.08), text rgba(255, 255, 255, 0.95), border rgba(255, 255, 255, 0.06), 8px radius, 10px 16px padding, text 'Get started'.
2. Create a secondary content section with background #000000. Start with a subheading 'Split inbox' at 20px Inter weight 400, #b5b5b5, letter-spacing -0.14px. Below that, body text 'Prioritize what's important. Split your inbox so you can focus on what matters, when it matters. Never miss an urgent email again.' at 16px Inter weight 400, #ffffff, line-height 1.5, letter-spacing -0.1px. Add 28px vertical spacing between sections.
3. Design a navigation bar item: Text 'Pricing' at 14px Inter weight 400, #919191. On hover, text color #ffffff. On active, text color #007eed. No background or borders, 0px padding. This should align with the navigation link button pattern.
4. Produce a featured block to illustrate keyboard shortcuts: Background #3b3b3b, 16px border-radius, 24px horizontal and vertical padding. Inside, place a 'Command menu' button with the Primary Ghost Button style.

---
_Source: https://styles.refero.design/style/cb6e4ab0-b8fe-45b0-bd22-6339b073e26d_
