# Buddy — Design Reference

> Crisp digital blueprint

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://buddy.works](https://buddy.works) |
| Refero page | [https://styles.refero.design/style/1329b661-39d8-4f0b-a12a-11ed13671ccb](https://styles.refero.design/style/1329b661-39d8-4f0b-a12a-11ed13671ccb) |
| Theme | light |
| Industry | devtools |

## Overview

Buddy Works presents a clean, pragmatic SaaS identity with a dominant white canvas contrasting dark, bold typography. The system utilizes subtle grays for component borders and secondary text, punctuated by a vibrant lime green for calls to action, creating a sense of efficiency and clarity. Light shadows with subtle color tints from the brand palette add depth without heaviness. The overall visual tone is structured and functional, with an emphasis on readable information hierarchy and quick identification of interactive elements.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#1d2130` | neutral | Primary text, deep gray borders, dark accents for UI elements |
| Cloud White | `#fcfcfd` | neutral | Primary page background, card surfaces, ghost button backgrounds |
| Slate Gray | `#d5d9e8` | neutral | Muted borders, subtle text for supporting information, light shadows |
| Frost | `#ebeef7` | neutral | Subtle borders, list item backgrounds, fills for inactive states |
| Coal Black | `#151720` | neutral | Darkest text for prominent headings, high-contrast icons |
| Deep Space | `#0a0d16` | neutral | Ultra-dark text for hero headlines, strong icon fills |
| Sky Dust | `#d8fbed` | neutral | Very faint background wash, decorative fills, subtle highlights |
| Emerald Echo | `#0b3d28` | brand | Green outline accent for tags, dividers, and focused UI edges |
| Sour Apple | `#bfff5a` | brand | Green action color for filled buttons, selected navigation states, and focused conversion moments |
| Hydro Blue | `#1a67fd` | accent | Violet text accent for links, tags, and emphasized short phrases. Do not promote it to the primary CTA color |
| Sunny Glow | `#ffd888` | accent | Feedback card background, decorative gradient for emphasis |
| Pink Sugar | `#ffb5f1` | accent | Decorative gradient for thematic sections or elements |
| Volt Yellow | `#ecef74` | accent | Decorative gradient for thematic sections or elements |
| Mint Blast | `#5df7b6` | accent | Decorative gradient for thematic sections or elements |
| Sky Surge | `#7ae3ff` | accent | Decorative gradient for thematic sections or elements |

## Typography

### IBM Plex Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 13px, 14px, 16px, 17px, 18px, 22px, 48px, 80px |
| lineHeight | 0.90, 1.00, 1.33, 1.43, 1.45, 1.50, 1.56, 1.71, 1.85, 1.88, 2.29 |
| letterSpacing | -0.0450em at 80px, -0.0210em at 48px, -0.0120em at 22px and 17px, normal at other sizes |
| fontFeatureSettings | "liga" 0 |
| substitute | Inter |
| role | Used for all primary text, navigation, body copy, and headings. Variable weights maintain contrast and hierarchy, with tighter tracking on larger display sizes for impact while maintaining legibility on smaller text. |

### IBM Plex Mono

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 12px |
| lineHeight | 2.00 |
| letterSpacing | 0.1670em |
| fontFeatureSettings | "liga" 0 |
| substitute | Fira Code |
| role | Monospaced font for code snippets, technical terms, and labels where fixed-width aesthetic is desired. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.71 |  |
| heading | 22 |  | 1.33 | -0.264 |
| heading-lg | 48 |  | 1 | -0.96 |
| display | 80 |  | 0.9 | -3.6 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| images | 8px |
| buttons | 56px |
| navItems | 44px |
| textContainers | 9999px |

- **elementGap** — 24px
- **sectionGap** — 64px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Cloud White Canvas | `#fcfcfd` | 0 | Primary page background, foundation for all content. |
| Frosted Card | `#fcfcfd` | 1 | Default card and container surfaces, subtle lift from canvas using border. |
| Elevated Accent Surface | `#00000000` | 2 | Cards and interactive elements that receive depth through border and tinted shadow. |
| Feedback Highlight | `#ffd888` | 3 | Distinct, highly visible accent surfaces for testimonials or important notices using gradients. |

## Components

### Primary Action Button

**Role:** Interactive element

Filled button with 'Sour Apple' (#bfff5a) background and 'Midnight Ink' (#1d2130) text. Uses 56px border-radius, 4px vertical padding, 24px horizontal padding, with a subtle top/bottom shadow.

### Ghost Button

**Role:** Secondary action or navigation

Transparent background with 'Midnight Ink' (#1d2130) border and text. Uses 56px border-radius, 8px vertical padding, 34px horizontal padding.

### Default Card

**Role:** Content container

White background (#fcfcfd) with 14px border-radius and 6px internal padding. No visible shadow, relying on subtle borders for separation.

### Elevated Feature Card

**Role:** Prominent content container

Transparent background with 24px border-radius. Features a 1px 'Midnight Ink' (#1d2130) border and a complex shadow stack hinting at depth, with a subtle 'Sky Surge' (#7ae3ff) blue tint to the lowest shadows.

### Feedback Card

**Role:** Testimonial or distinct message

Uses 'Sunny Glow' (#ffd888) radial gradient background with 'Deep Space' (#0a0d16) text. Features a 24px border-radius, no internal padding. Distinguished by its strong accent color background.

### Navigation Link

**Role:** Primary global and section navigation

Displays 'Midnight Ink' (#1d2130) text on a 'Cloud White' (#fcfcfd) background. On hover or active, it becomes 'Hydro Blue' (#1a67fd). Uses a 44px border-radius for pill-shaped active state indicator.

### Tab Pill

**Role:** Segmented control for content filtering

Pill-shaped containers with a 9999px border radius. Inactive tabs have 'Midnight Ink' (#1d2130) text on a 'Cloud White' (#fcfcfd) background. Active tabs use either a ghost style with 'Midnight Ink' text and border, or a 'Cloud White' background with 'Deep Space' text.

## Layout

The page primarily employs a max-width contained layout, allowing content to breathe within a central column. The hero section is full-width with a dark background for contrast, featuring a large, centered headline. Subsequent sections alternate between light backgrounds and occasionally a strong background gradient, with consistent vertical spacing of 64px. Content is often presented in a two-column layout that alternates text and visuals, or in feature grids. Navigation is a persistent top bar, with prominent 'Sour Apple' primary action button, and a pill-shaped secondary navigation for filtering content within sections.

## Imagery

The visual language is split between abstract UI-like illustrations and minimal, clean product screenshots. Illustrations use a combination of transparent fills and subtly colored, flowing lines (Mimic Mint Blast, Sky Surge, Volt Yellow, Pink Sugar) to represent data flow or connections, maintaining an ethereal, light feel. Product screenshots are contained within card-like structures, often angled to convey dynamism, with a strong focus on the software UI itself rather than real-world context. Icons are typically outlined or monochromatic fills, matching the neutral palette with occasional Hydro Blue accents. Imagery is primarily explanatory and decorative, supporting textual content rather than dominating the layout, maintaining a density that favors text.

## Dos & Donts

### Do

- Use 'Cloud White' (#fcfcfd) as the primary canvas for most sections to maintain a bright, open feel.
- Apply 'Sour Apple' (#bfff5a) exclusively for primary calls to action, drawing immediate attention to key interactive elements.
- Employ 'Midnight Ink' (#1d2130) for primary text content to ensure high readability and crisp contrast.
- Structure layout with a 24px elementGap between distinct UI components and 64px vertical sectionGap for comfortable separation.
- Utilize IBM Plex Sans with a -0.0450em letter-spacing for display headings (80px) and -0.0210em for large headings (48px) to achieve a modern, tight feel for impactful statements.
- Round all primary action buttons and tab pills with a 56px or 9999px radius respectively, emphasizing their interactive nature with soft, approachable edges.
- Elevate significant content blocks (e.g., feature cards) with a 24px border-radius and a full shadow stack tinted with subtle accent colors (e.g. 'Sky Surge' or 'Mint Blast') for a layered effect.

### Don't

- Avoid using highly saturated colors for large background areas; reserve chromatic colors for functional accents and small decorative elements.
- Do not deviate from the specified 'Sour Apple' (#bfff5a) for primary action backgrounds; any other filled chromatic button will feel off-brand.
- Do not introduce new border-radius values; stick to 56px for buttons, 24px for cards, and 9999px for pill-shaped elements.
- Avoid generic box-shadows; when shadows are employed, ensure they include the subtle colored tints specified in the component definitions (e.g., from 'Sky Surge' or 'Sour Apple' family).
- Do not use IBM Plex Mono for standard body text or headlines; its usage is strictly for technical or code-like content to maintain its specialized role.
- Do not introduce heavy borders or background gradients on primary navigation links; maintain their light, ghost-like appearance until active.
- Do not use large, decorative, full-bleed imagery that breaks the UI-focused, 'blueprint' aesthetic.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #1d2130
background: #fcfcfd
border: #d5d9e8
accent: #1a67fd
primary action: #bfff5a (filled action)

3-5 Example Component Prompts:
Create a Primary Action Button: #bfff5a background, #0a0d16 text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.

Create a product feature card: Transparent background with a 'Sky Surge'-tinted shadow stack. 24px border-radius, no internal padding. Headline 'Remote Deployments DXed' at 22px IBM Plex Sans weight 600, #151720, letter-spacing -0.264px. Include a product screenshot of Buddy's UI, angled to show depth.

Create a secondary navigation pill: 'Cloud White' (#fcfcfd) background, 'Deep Space' (#0a0d16) text, 9999px border-radius, 8px vertical padding, 24px horizontal padding. Ensure inactive pills use 'Midnight Ink' (#1d2130) text and border.

---
_Source: https://styles.refero.design/style/1329b661-39d8-4f0b-a12a-11ed13671ccb_
