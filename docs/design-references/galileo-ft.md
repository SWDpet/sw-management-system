# Galileo-ft — Design Reference

> Deep-space command center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.galileo-ft.com](https://www.galileo-ft.com) |
| Refero page | [https://styles.refero.design/style/10a77cbd-7847-4e1b-a09e-447ebad0f7c6](https://styles.refero.design/style/10a77cbd-7847-4e1b-a09e-447ebad0f7c6) |
| Theme | dark |
| Industry | fintech |

## Overview

Galileo projects a deep-space command center aesthetic: a dark canvas punctuated by stark whites and energetic, vibrant blue-violet accents. The visual system balances substantial, wide type with delicate ghost elements, creating an impression of technical authority and precision. Elevation is minimal, giving way to smooth transitions and subtle background shifts. The overall feel is restrained and deliberate, with color reserved for functional highlights and interaction – guiding the user through complex financial information with clarity and a sense of controlled power.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#03081a` | neutral | Primary background, deep surface color for cards and sections |
| Void Shadow | `#020626` | neutral | Secondary background layer, subtle surface elevation, text color against very light backgrounds |
| Galileo Violet | `#3d50fc` | brand | Primary brand accent, interactive elements like buttons and links, icon fills; Gradient for hero sections and prominent interactive elements, creating dynamic visual interest; Gradient for calls to action, drawing attention with a soft, engaging color transition |
| Cosmic Gray | `#292f66` | neutral | Muted UI elements like secondary text, dividers, and subtle borders. Provides depth against darker backgrounds |
| Starlight Blue | `#aab1f2` | accent | Subtle light text, link underlines, and decorative accents against dark backgrounds, providing visual lightness |
| Neon Aqua | `#05cee0` | accent | Decorative highlights, specific icon fills, or secondary accents to add a contrasting pop of color |
| Nebula Blue | `#4d5499` | neutral | Tertiary text, less prominent borders and dividers, for content segmentation without strong contrast |
| Deep Space Blue | `#080f4d` | neutral | Subtle decorative elements and fills within the dark theme, for gradients that add visual texture without distraction |
| Lunar Dust | `#d9ddff` | neutral | Subtle text and UI elements on lighter secondary surfaces, providing a near-white contrast |
| Flux Orange | `#d80c9a` | accent | Vivid decorative accent color, used sparingly in graphics or unique UI elements for high impact |
| Electric Cyan | `#05e0e0` | accent | Accent for links and icon highlights, signalling interactivity or important information on dark backgrounds |
| Hazy Iris | `#7a83cc` | neutral | Subtle background for UI elements, less prominent text, and slight shifts in surface tone |
| Cleanroom White | `#ffffff` | neutral | Key text on dark backgrounds, primary navigation items, and button text for high contrast |
| Panel Gray | `#f5f6ff` | neutral | Background for secondary content panels and light-themed sections, offering a soft, almost white surface |

## Typography

### Plain Ultrathin

| Key | Value |
| --- | --- |
| weight | 100 |
| sizes | 42px, 56px, 83px, 147px |
| lineHeight | 0.80, 1.00, 1.10, 1.20 |
| letterSpacing | -0.02em at 42px and 56px, -0.01em at 83px and 147px |
| substitute | Montserrat, sans-serif |
| role | Hero headlines. The ultrathin weight, usually reserved for display text, is applied for headlines to create a sense of expansive, light authority rather than forceful shouting. |

### Plain Ultralight

| Key | Value |
| --- | --- |
| weight | 100 |
| sizes | 28px |
| lineHeight | 1.30 |
| letterSpacing | -0.02em |
| substitute | Montserrat, sans-serif |
| role | Sub-headlines and emphasized body sections. Maintains the light, airy feel of the display type while providing more substance for readability. |

### Plain Light

| Key | Value |
| --- | --- |
| weight | 300 |
| sizes | 12px, 14px |
| lineHeight | 1.40, 1.50, 1.80 |
| letterSpacing | normal |
| substitute | Montserrat, sans-serif |
| role | Body copy and descriptive text. The light weight ensures a consistent brand voice without overwhelming the visual hierarchy. |

### Plain

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 10px, 13px, 14px, 16px |
| lineHeight | 1.20, 1.30 |
| letterSpacing | 0.25em |
| substitute | Montserrat, sans-serif |
| role | Navigation items, labels, and small functional text. The moderate letter-spacing creates distinct visual blocks for these elements. |

### Plain

| Key | Value |
| --- | --- |
| weight | 300, 400 |
| sizes | 10px, 13px, 14px, 16px |
| lineHeight | 1.20, 1.30 |
| letterSpacing | 0.25em |
| substitute | Montserrat, sans-serif |
| role | Emphasized small text, button labels, and secondary navigation for clear hierarchy. |

### Times

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.20 |
| letterSpacing | normal |
| substitute | serif |
| role | Fallback and specific content sections, providing a traditional contrast to the modern sans-serifs. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.2 | 2.5 |
| body | 14 |  | 1.5 |  |
| subheading | 28 |  | 1.3 | -0.56 |
| heading | 42 |  | 1.2 | -0.84 |
| heading-lg | 56 |  | 1.2 | -1.12 |
| display | 83 |  | 1 | -0.83 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 34.704px |
| buttons | 47.7072px |
| interactive | 17.352px |

- **elementGap** — 9px
- **sectionGap** — 26px
- **cardPadding** — 35px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#03081a` | 0 | The deepest background layer for the entire page, setting the dark theme. |
| Base Surface | `#020626` | 1 | Used for sections and cards that visually lift slightly from the canvas, providing a subtle layer differentiation. |
| Elevated Surface | `#FFFFFF` | 2 | A contrasting light surface for distinct content blocks, often used in alternating sections to break the dark theme. |

## Components

### Ghost Navigation Button

**Role:** Primary navigation and links within dark context.

Transparent background, Cleanroom White text, no border, 0px radius, 0px padding. Text uses Plain font, 16px, weight 400.

### Primary Action Button

**Role:** Key calls to action requiring prominence.

Galileo Violet background (#3d50fc), Cleanroom White text (#ffffff), no border. Fully rounded corners (47.7072px radius). Padding: 14px vertical, 26px horizontal. Text uses Plain font, 16px, weight 400.

### Secondary Action Button

**Role:** Calls to action on lighter backgrounds that need to stand out.

Cleanroom White background (#ffffff), Galileo Violet text (#3d50fc), Galileo Violet border (#3d50fc). Fully rounded corners (47.7072px radius). Padding: 14px vertical, 26px horizontal. Text uses Plain font, 16px, weight 400.

### Subtle Card

**Role:** Container for secondary content or feature blocks.

Transparent background, no shadow. Rounded corners (34.704px radius). Padding: 35px in all directions. Uses Void Shadow text and Cosmic Gray elements inside.

### Product Insight Card

**Role:** Informational cards within a feature section.

Cleanroom White background (#ffffff), no shadow. Rounded corners (34.704px radius). Padding: 87px top, 0px right and bottom, 104px left. Contains Void Shadow text.

### Chat Bubble Initiator

**Role:** Persistent call-to-action for chat support.

Midnight Ink background (#03081a), 50% border radius for a circular shape. Located at the bottom center of the screen. Incorporates a small gradient for visual interest.

### Cyberbank Core Tag

**Role:** Filter or category tags for content segmentation.

Galileo Violet background (#3d50fc), Cleanroom White text (#ffffff). Rounded corners (34.704px radius). Padding: 14px vertical, 26px horizontal. Text uses Plain font, 16px, weight 400.

### Info Banner

**Role:** Top-level announcements or system messages.

Deep Space Blue background, Cleanroom White text. Minimal padding. Contains a 'Learn More' link.

## Layout

The layout follows a max-width contained model (effectively ~1200px), with some background elements extending full-bleed. The hero section is full-bleed, dark-themed, featuring a large, centered headline over abstract 3D visuals. Subsequent sections alternate between dark and light backgrounds, creating a clear vertical rhythm. Content is arranged in flexible grid patterns, often using 2-column or 3-column structures for text and corresponding visuals (product screenshots, descriptive text blocks). Vertical spacing between sections feels generous. Primary navigation is a top bar with minimalist ghost buttons. A persistent chat bubble is affixed to the bottom center of the viewport.

## Imagery

The visual language blends abstract, volumetric 3D shapes with detailed product screenshots. The 3D elements, often transparent or translucent with blue-violet tints, are primarily decorative, creating atmospheric depth in hero sections. Product screenshots are contained, precise, and often feature dark-mode UI, showcasing the platform's functionality within its own aesthetic. Icons are primarily outlined or filled with a single accent color, maintaining a clean, technical appearance. Imagery serves to establish a futuristic, high-tech atmosphere and showcase product capabilities, rather than featuring lifestyle or candid photography. Density is moderate, with imagery providing visual anchors surrounded by ample dark space.

## Dos & Donts

### Do

- Prioritize the Midnight Ink (#03081a) as the canvas background for most sections to maintain the dark theme.
- Use Galileo Violet (#3d50fc) for all primary interactive elements, ensuring it is the sole vibrant accent color for action.
- Apply Plain Ultrathin (weight 100) at large sizes for all main headlines to establish a sense of modern authority.
- Ensure buttons and primary interactive elements have a 47.7072px border radius for a distinct, soft pill shape.
- Space elements with a base unit of 4px; use 9px for element gaps and 35px for card padding.
- Structure page content using transparent cards with 34.704px radius on dark backgrounds, using subtle color #020626 where a distinct surface is needed.
- Employ the linear gradient `linear-gradient(90deg, rgb(5, 161, 201) 0%, rgb(61, 80, 252) 100%)` for hero backgrounds or significant visual emphasis.

### Don't

- Avoid using multiple vibrant chromatic colors; actively restrict accent colors to Galileo Violet, Neon Aqua, and Electric Cyan, with the latter two used sparingly for decoration.
- Do not introduce heavy drop shadows or strong elevation; prefer subtle background color shifts or transparent cards for depth.
- Refrain from using Times font for headlines or primary UI text; reserve it for specific content blocks if at all.
- Do not deviate from the high border radius for buttons; rounded corners are a signature element.
- Avoid excessive text density; maintain comfortable spacing and liberal use of negative space.
- Do not use dark text on dark backgrounds; ensure sufficient contrast with Cleanroom White (#ffffff) or Starlight Blue (#aab1f2) for legibility.
- Do not make every interactive element a filled button. Utilize ghost buttons and text links for secondary actions.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #ffffff
background: #03081a
border: #292f66
accent: #3d50fc
primary action: #3d50fc (filled action)

3-5 Example Component Prompts:
1. Create a Hero Section: background is `linear-gradient(90deg, rgb(5, 161, 201) 0%, rgb(61, 80, 252) 100%)`. Headline is 'Expanding the financial frontier' in Plain Ultrathin weight 100, 83px, #ffffff, letter-spacing -0.83px. Sub-text is 'We help banks...' in Plain Light weight 300, 14px, #aab1f2. Include a 'Let's Talk' Primary Action Button with Galileo Violet background (#3d50fc), Cleanroom White text (#ffffff), 47.7072px radius, 14px vertical and 26px horizontal padding.
2. Create a Feature Card: background is #020626, border 1px solid #292f66, 34.704px radius, 35px padding. Title is 'Engagement Channels' in Plain Ultralight weight 100, 28px, #ffffff. Body text is 'Deliver personalized...' in Plain Light weight 300, 14px, #aab1f2.
3. Create a Navigation Link: 'Deposits' in Plain weight 300, 16px, #ffffff, normal letter-spacing, on a transparent background with 0px padding and radius. Active state shows a subtle underline using Galileo Violet (#3d50fc).
4. Create a Secondary Action Button: 'Explore Cyberbank Core' with Cleanroom White background (#ffffff), Galileo Violet text (#3d50fc), Galileo Violet border (#3d50fc), 47.7072px radius, 14px vertical and 26px horizontal padding. Include an arrow icon in Galileo Violet.

---
_Source: https://styles.refero.design/style/10a77cbd-7847-4e1b-a09e-447ebad0f7c6_
