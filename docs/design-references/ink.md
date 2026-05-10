# INK — Design Reference

> Architectural blueprint on white marble. Precision, clarity, spaciousness, and carefully chosen details unfold across a pristine, light-drenched surface.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://weareink.co.uk](https://weareink.co.uk) |
| Refero page | [https://styles.refero.design/style/6262b0bb-ea6f-481b-b706-65df29507b6c](https://styles.refero.design/style/6262b0bb-ea6f-481b-b706-65df29507b6c) |
| Theme | light |
| Industry | agency |

## Overview

This system projects an image of understated luxury, prioritizing spaciousness and meticulous typography over vibrant visuals. The canvas of near-white (#E6DCD4) or pure white allows the refined, custom sans-serif to take center stage, creating a highly legible and authoritative tone. A distinct absence of shadows and geometric borders focuses attention on content through scale and precise placement, rather than overt styling.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink Black | `#2e2a2b` | neutral | Primary text, interactive elements, navigation links, and button outlines — defines the core legible content against light backgrounds. |
| Frost Canvas | `#ffffff` | neutral | Dominant page and component background. Its starkness emphasizes content and creates breathing room. |
| Warm Paper | `#e6dcd4` | neutral | Alternative light background for subtle section shifts or specific footer areas, providing visual relief from pure white without introducing chromatic distraction. |
| Cool Stone | `#afa697` | neutral | Secondary text, subheadings, and subtle accents. This near-gray provides a softer contrast than Ink Black, used for descriptive text. |
| Joby Aviation Sky | `#b6d0e2` | accent | Specific background color appearing in a unique asset (Joby Aviation section/image), not a system-wide accent. |

## Typography

### Good Sans

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 18px, 22px, 27px, 60px |
| lineHeight | 1.00, 1.09, 1.14, 1.25, 1.31, 1.60, 1.75 |
| letterSpacing | normal |
| substitute | Inter |
| role | The singular typeface for all content roles, from body to display. Its custom nature supports the brand's identity through consistent, sophisticated readability. The moderate weights maintain an understated authority, avoiding heavy-handedness. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| body-sm | 18 |  | 1.75 |  |
| body | 22 |  | 1.6 |  |
| subheading | 27 |  | 1.31 |  |
| display | 60 |  | 1 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 0px |
| inputs | 0px |
| buttons | 0px |
| navPills | 100px |

- **elementGap** — 32px
- **sectionGap** — 160px
- **cardPadding** — 64px
- **pageMaxWidth** — 

## Components

### Ghost Button Group

### Project Card — Portfolio Item

### Email Subscription Input

### Ghost Button

**Role:** Interactive elements, navigation, and 'Back to top' calls to action.

Transparent background with 'Ink Black' (#2e2a2b) text and a 'Ink Black' (#2e2a2b) bottom border (1px). No border radius, emphasizing a crisp, direct interaction.

### Navigation Pill Indicator

**Role:** Active state or selected item indicator within navigation menus.

Rendered as a 100px border-radius shape, likely for visual highlighting of the current page or selection. Color is inferred to be a subtle fill given its high radius in 'nav' context.

### Standard Input Field

**Role:** Form inputs such as email subscription fields.

Background is transparent (rgba(0,0,0,0)), text is 'Ink Black' (#000000). A 1px top border of 'Ink Black' (#000000) is present, paired with padding of 1px top/bottom and 2px left/right. No border radius, maintaining the system's sharp aesthetic.

## Layout

The layout is primarily centered and contained, although hero sections can be full-bleed with large, impactful imagery. Content is arranged with ample vertical spacing, giving a sense of gravitas and making each section feel distinct. The page model appears to be a sequence of large, horizontally aligned content blocks, often showcasing a project title and description, followed by a full-width image or video. There's a clear header with minimal navigation and a generous footer, emphasizing content over persistent UI elements.

## Imagery

The site primarily uses high-quality, product-focused or project-specific photography displayed full-bleed in hero sections and content blocks. Images are contained with sharp edges, often acting as showcases for client work. There's an absence of playful illustrations or excessive icons, reinforcing a serious, professional tone. The images serve to illustrate and contextualize the studio's projects rather than merely decorate.

## Dos & Donts

### Do

- Prioritize 'Frost Canvas' (#ffffff) or 'Warm Paper' (#e6dcd4) as primary backgrounds for all layouts to maintain a bright, expansive feel.
- Use 'Ink Black' (#2e2a2b) for all primary text elements, interactive components, and critical information for maximum legibility.
- Employ spacious vertical gaps between sections, defaulting to '160px' to create significant breathing room and separation.
- Maintain consistently sharp 0px border radii for all primary UI components like buttons and cards, enforcing a precise, architectural aesthetic.
- Utilize 'Cool Stone' (#afa697) for secondary textual elements such as subheadings or descriptive text to introduce visual hierarchy without high contrast.
- Ensure all interactive elements and navigation links use the 'Good Sans' typeface at designated weights and sizes, reinforcing brand consistency.

### Don't

- Avoid the integration of heavy shadows or complex elevation systems; the design relies on spacing and color contrast for depth.
- Do not introduce overtly rounded elements beyond the specific '100px' radius navigation pill, to preserve the system's sharp, precise nature.
- Refrain from using highly saturated accent colors in prominent UI roles; colorful elements are primarily decorative content, not interactive indicators.
- Do not use multiple font families; all textual elements must strictly adhere to the 'Good Sans' typeface.
- Avoid dense, information-packed sections; embrace the generous spacing guidelines to ensure content feels open and uncluttered.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #2e2a2b
- Background: #ffffff
- Secondary Background: #e6dcd4
- Secondary Text: #afa697
- Accent Background (example, often project-specific): #b6d0e2

### 3-5 Example Component Prompts
1. Create a primary headline: 'Good Sans' 60px, weight 400, color '#2e2a2b', line-height 1.0, on a '#ffffff' background.
2. Generate a ghost button: Text 'Good Sans' 18px, weight 400, color '#2e2a2b', with a transparent background, 1px bottom border '#2e2a2b', and 0px border radius. Padding 0px all around.
3. Design a descriptive body paragraph: 'Good Sans' 18px, weight 400, color '#afa697', line-height 1.75, on a '#ffffff' background, with 32px margin-bottom.
4. Construct an input field for email: Placeholder text 'Good Sans' 18px, weight 400, color '#2e2a2b', transparent background, 1px top border '#000000', with 1px vertical and 2px horizontal padding, 0px border radius.

---
_Source: https://styles.refero.design/style/6262b0bb-ea6f-481b-b706-65df29507b6c_
