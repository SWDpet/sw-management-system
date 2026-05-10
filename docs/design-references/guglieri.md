# Guglieri — Design Reference

> Midnight Terminal, Razor Sharp

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://guglieri.com](https://guglieri.com) |
| Refero page | [https://styles.refero.design/style/5b7ecaf1-de2d-4fb9-9995-9f0665e77862](https://styles.refero.design/style/5b7ecaf1-de2d-4fb9-9995-9f0665e77862) |
| Theme | dark |
| Industry | design |

## Overview

Guglieri.com presents itself as a stark, high-contrast digital canvas for showcasing work. Its visual style is predominantly dark, minimal, and typography-driven, with subtle interactive elements. The design prioritizes clear information hierarchy through careful typographic variations and a constrained color palette of intense blacks and whites, accented by strategic use of fine gray lines and text. Components are kept lightweight, focusing on functionality over decoration, creating an immersive, focused browsing experience.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Absolute Zero | `#000000` | neutral | Primary background for pages and sections; also serves as input and link text, and borders for ghost buttons. Its pervasive use establishes the site's dark theme |
| Canvas White | `#ffffff` | neutral | Main text color for headings and body content, providing sharp contrast against the dark backgrounds. Also used sparingly for button backgrounds |
| Text Gray | `#454545` | neutral | Secondary text color for descriptive text and metadata, offering a slightly softer contrast than Canvas White while maintaining readability |
| Input Surface | `#111111` | neutral | Background for interactive elements like input fields and button backgrounds, subtly distinct from the primary page background |
| Subtle Surface | `#1c1c1c` | neutral | Tertiary background color for minor surface variations or less prominent headings, providing a slight elevation from the primary background |
| Hairline Gray | `#575757` | neutral | Subtle text and critical border outlines, such as dividers and helper text, acting as a visual separator without adding excessive weight |

## Typography

### Raveo Variable

| Key | Value |
| --- | --- |
| weight | 1000 |
| weights | 1000 |
| sizes | 12px, 14px, 22px, 32px, 64px |
| lineHeight | 1.00, 1.06, 1.18, 1.20, 1.60 |
| letterSpacing | -0.0400em at 64px, -0.0200em at 32px |
| fontFeatureSettings | "blwf", "cv03", "cv04", "cv09", "cv11" |
| substitute | system-ui, sans-serif |
| role | Primary display font for prominent headings. Its extra-bold weight (1000) and distinctive variable features create a strong, almost aggressive visual signature for key messages. The tight letter spacing enhances its impact, making the text feel dense and deliberate. |

### Inter Display

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 22px |
| lineHeight | 1.18 |
| substitute | Inter, sans-serif |
| role | Used for descriptive body text, balancing the intensity of the Raveo Variable headings with a more classic, readable sans-serif. It provides a neutral grounding for longer-form content. |

### Arial

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 14px |
| lineHeight | 1.20 |
| substitute | system-ui, sans-serif |
| role | Utilized for input fields, offering a standard and accessible font choice for user interaction elements. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | system-ui, sans-serif |
| role | Default font for various UI elements like navigation links and small labels. It recedes into the background, allowing the stronger typographic choices to dominate. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 |  |
| body-sm | 14 |  | 1.2 |  |
| body | 22 |  | 1.18 |  |
| subheading | 32 |  | 1.2 | -0.64 |
| heading | 64 |  | 1 | -2.56 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| links | 8px |
| buttons | 28px |
| other-ui-elements | 4px |
| pill-like-elements | 40px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 40px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Base Canvas | `#000000` | 1 | Dominant page background, establishing the overall dark theme. |
| Interactive Surface | `#111111` | 2 | Background for interactive components like filled buttons and input fields, providing a slight visual differentiation from the base canvas. |
| Subtle Accent Surface | `#1c1c1c` | 3 | Used for minor heading backgrounds or background elements that need minimal visual separation from interactive surfaces. |

## Components

### Navigation Link

**Role:** Simple text link

Text in Canvas White (#ffffff) on Absolute Zero (#000000). Radius of 8px for active states or highlighted items. Uses sans-serif at 12px.

### Primary Ghost Button

**Role:** Actionable button with minimal visual hierarchy

Transparent background with an Absolute Zero (#000000) border. Text color is Absolute Zero (#000000). Full rounded pill shape with a 40px radius. Padding is 40px on left/right for ample touch target.

### Secondary Filled Button

**Role:** Actionable button with subtle fill

Filled with Canvas White (#ffffff), text in Absolute Zero (#000000). Half-rounded pill shape with a 28px radius. Padding is 40px on left/right.

### Minimal Input Field

**Role:** Form input field

Transparent background with barely visible border color of Input Surface (#111111). Placeholder and text color is Input Surface (#111111). No explicit radius, appears square. Uses Arial at 14px.

### Description Card

**Role:** Container for secondary information

Implied container on Absolute Zero (#000000) background, with Text Gray (#454545) text. Uses 4px radius as a generic container. Padding of 12px or 16px is common.

## Layout

The page operates on a full-bleed model with content centered within implicit vertical divisions, rather than a fixed `pageMaxWidth`. The hero section is full-bleed black with a large, centered Raveo Variable headline and a prominent 3D graphic. Sections are demarcated by consistent vertical spacing, creating distinct information blocks without hard dividers. Content often appears in split layouts with text on one side and a supporting visual element or negative space on the other. A notable pattern is a centered, compact stack for contact information and navigation elements. The overall density is spacious between sections but compact within text blocks. Navigation is a simple, horizontal text menu embedded in the header.

## Imagery

This site uses highly polished 3D rendered graphics of UI elements, specifically computer cursors and app icons, as its primary visual identity. These are tightly cropped, appear against the 'Absolute Zero' background, and utilize subtle reflections and metallic sheens to add depth. Photography is used sparingly, primarily in contextual background shots that are desaturated (grayscale) and serve as atmospheric backdrops rather than focal points. Icons are not emphasized, but when present, they appear as outlined or filled vectors, maintaining UI clarity. The density is text-dominant in informational sections, with hero imagery dominating visual space to showcase core work.

## Dos & Donts

### Do

- Prioritize Absolute Zero (#000000) as the dominant background color to maintain the dark, high-contrast aesthetic.
- Use Canvas White (#ffffff) for all primary headings and body text to ensure sharp readability against dark backgrounds.
- Apply Raveo Variable with its 1000 weight and specified letter spacing for all major headlines to convey strong visual impact.
- Maintain minimal visual hierarchy for buttons, utilizing ghost or subtly filled variants with large border-radii (28px or 40px).
- Employ Text Gray (#454545) or Hairline Gray (#575757) for secondary information, metadata, or subtle dividers to add nuance without compromising contrast.
- Utilize an 8px radius for interactive link elements and navigation items.
- Ensure generous `elementGap` of 8px and `sectionGap` of 40px to create ample breathing room between content blocks.

### Don't

- Avoid introducing bright or saturated colors beyond functional accents; the system relies on a monochromatic palette.
- Do not use heavy shadows or multi-layered elevations, as the design philosophy leans towards flat, crisp surfaces.
- Refrain from using lightweight fonts for headings or prominent text; the Raveo Variable 1000 weight is critical for brand recognition.
- Do not deviate from the high-contrast pairing of Canvas White on Absolute Zero for primary content, as it's foundational to the visual identity.
- Avoid complex gradients or patterned backgrounds; keep surfaces clean and solid.
- Do not use generic square buttons or sharply angled corners; button radii should typically be 28px or 40px.
- Do not use Arial outside of input fields; body copy should use Inter Display or a geometric sans-serif substitute.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- text: #ffffff
- background: #000000
- border: #000000
- accent: no distinct accent color
- primary action: no distinct CTA color

### 3-5 Example Component Prompts
- Create a hero section: Absolute Zero (#000000) background. Headline 'Product Designer' using Raveo Variable, 64px, weight 1000, Canvas White (#ffffff) with letter-spacing -2.56px. Below it, a ghost button 'View Work': transparent background, Absolute Zero (#000000) border, Canvas White (#ffffff) text, 40px radius, 40px horizontal padding, sans-serif 12px.
- Create a navigation bar: Absolute Zero (#000000) background. Links 'About', 'Work', 'Contact' using sans-serif, 12px, Canvas White (#ffffff) text, 8px radius on active states. Element gap 8px between items.
- Create a contact form: Input Surface (#111111) text input fields with a 1px Input Surface (#111111) border, no radius. Placeholder Arial 14px Input Surface (#111111). A "Submit" button, filled with Canvas White (#ffffff), text Absolute Zero (#000000), 28px radius, 40px horizontal padding, Arial 14px.

---
_Source: https://styles.refero.design/style/5b7ecaf1-de2d-4fb9-9995-9f0665e77862_
