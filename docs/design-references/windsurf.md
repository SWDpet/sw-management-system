# Windsurf — Design Reference

> Deep Space Command Center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://windsurf.com](https://windsurf.com) |
| Refero page | [https://styles.refero.design/style/cfab7b43-ed24-41e9-9272-c858700b865b](https://styles.refero.design/style/cfab7b43-ed24-41e9-9272-c858700b865b) |
| Theme | dark |
| Industry | devtools |

## Overview

Windsurf operates with a high-contrast, dark-mode scheme, using a deep navy background that evokes a professional, sophisticated environment. A vibrant, almost neon cyan serves as the primary accent, signaling interactive elements and highlighting key information, complemented by a softer magenta for secondary accents. Typography is a blend of impactful, large-scale headlines with subtly tracked small text, creating a dynamic yet precise visual hierarchy. Components are lightweight and often borderless, preferring subtle color shifts and soft corners over heavy shadows or solid fills.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#011c42` | neutral | Primary page background, footer, deep surface elements. Creates a high-contrast canvas for bright accents; Background gradient for hero sections and large content blocks, adding depth to the primary dark canvas |
| Arctic Mist | `#f8f1e5` | neutral | Subtle light background color for cards and sections, providing visual breathing room on the dark canvas |
| Platinum White | `#ffffff` | neutral | Primary text color for headlines and core content on dark backgrounds, selected button text within accents |
| Slate Text | `#696962` | neutral | Muted secondary text color, body copy, and helper text. Provides sufficient contrast against lighter backgrounds |
| Ash Border | `#c0c1c6` | neutral | Subtle borders, dividers, ghost button outlines, and inactive iconography. Offers soft visual separation without harsh lines |
| Charcoal Black | `#0b100f` | neutral | Text color against very light backgrounds, such as button text on the cyan accent |
| Neon Cyan | `#34e8bb` | brand | Teal action color for filled buttons, selected navigation states, and focused conversion moments. |
| Electric Magenta | `#fb9ce5` | brand | Secondary brand accent for ghost button borders, decorative icons, and call-to-action text that requires a different emphasis |
| Plasma Pink Gradient | `#a95af8` | accent | Vivid decorative element, background in specific highlight cards, and an essential part of abstract background graphics |

## Typography

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px |
| lineHeight | 1.43, 1.5 |
| role | ui-sans-serif — detected in extracted data but not described by AI |

### tomatoGrotesk

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| weights | 300, 400, 500 |
| sizes | 40px, 48px, 64px, 72px, 96px |
| lineHeight | 1.00 |
| letterSpacing | -1.44px at 40px, -0.86px at 72px, -0.64px at 64px |
| substitute | system-ui |
| role | Primary display font for hero headlines and impactful section titles. Its light weight (300) at large sizes creates authority through restraint, avoiding the shouting typical of bold headings. |

### DM Sans

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 10px, 12px, 14px, 16px, 18px, 20px, 28px, 40px |
| lineHeight | 1.00, 1.30, 1.33, 1.50, 1.56, 1.71, 2.40 |
| letterSpacing | -0.72px at 40px, -0.4px at 20px, -0.28px at 14px |
| substitute | Arial, sans-serif |
| role | Used for all subheadings, body copy, and navigation labels. Its versatility in weights and sizes manages hierarchy across various content types, keeping readability high. |

### DM Mono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px, 18px |
| lineHeight | 1.20, 1.43, 1.56, 1.71 |
| letterSpacing | 0.8px at 16px, 0.7px at 14px |
| substitute | monospace |
| role | Monospaced font for code snippets, tags, and secondary action buttons, providing a technical, precise feel. The letter-spacing adds clarity to often dense content. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.71 |  |
| body | 14 |  | 1.5 | -0.28 |
| heading-sm | 20 |  | 1.5 | -0.4 |
| heading | 40 |  | 1 | -1.44 |
| heading-lg | 48 |  | 1 |  |
| display | 64 |  | 1 | -0.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 9999px |
| cards | 8px |
| buttons | 2px |
| navigation | 9999px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Components

### Primary Action Button

**Role:** Calls to action that drive key user behavior (e.g., download, submit).

Filled rectangular button with Neon Cyan background (#34e8bb), Charcoal Black text (#0b100f), 2px border radius, and padding of 16px vertical, 24px horizontal. Uses DM Sans font, typically weight 500.

### Ghost Accent Button

**Role:** Secondary actions or links that need visual emphasis without overwhelming the primary action.

Transparent background with a border and text in Electric Magenta (#fb9ce5). Text is DM Sans, 16px, with no padding or border radius. Used for 'Explore Features' or 'Learn more' links.

### Download Banner Button

**Role:** Prominent, often full-width or oversized, call to action in hero sections.

Large button with Neon Cyan background (#34e8bb), Charcoal Black text (#003326 - a very dark teal close to black), 0px radius, with generous horizontal padding (40px) and no vertical padding. Uses DM Mono font for a technical feel.

### Content Card

**Role:** Container for feature descriptions, process steps, or grouped information.

Cards can have a subtle Arctic Mist background (#f8f1e5) or transparent. Corner radius is 8px. Internal padding is 12px. No visible shadow by default.

### Tag/Pill

**Role:** Small, distinct labels for categories, statuses, or new features.

Features a full border radius (9999px) making it pill-shaped. Examples include 'New Introducing Windsurf 2.0' where the background is transparent and text is Electric Magenta.

### Code Input Field

**Role:** User input fields, primarily for code or technical information.

Transparent background with White text (#ffffff). Border is Ash Border (#c0c1c6). No radius, offering a sharp, functional edge.

## Layout

The page structure is full-bleed with content often centered or presented in distinct, spacious horizontal blocks. The hero section is full-width, featuring a prominent centered headline over the abstract dark gradient background, accompanied by a cluster of call-to-action buttons. Subsequent sections alternate between clear content blocks and sometimes integrate background graphics seamlessly. Features and descriptions typically use a two-column layout (text-left/visual-right or vice-versa) or stacked centered content. Navigation is a persistent top bar, with interactive elements highlighted by the brand's accent colors. Vertical spacing is generous between sections.

## Imagery

The visual language predominantly features abstract, generative graphics with undulating wave patterns, often with colorful gradients (like the Plasma Pink Gradient) against the deep navy backdrop. These serve a decorative, atmospheric role rather than explanatory. Product screenshots, when present, are clean, full-bleed, and often framed directly into the dark UI without additional ornamentation, showcasing the software itself. Icons are minimal, either solid white or using the Electric Magenta or Neon Cyan accents, maintaining a high-tech, functional aesthetic. The density is image-light, focusing more on crisp UI and typography.

## Elevation philosophy

This system minimizes traditional drop shadows, primarily relying on flat surfaces and color contrast for hierarchy. When shadows are present, they are subtle and appear mostly for tooltips or modal elements, often with colored tints like rgba(52, 211, 153, 0.5) to maintain the brand's vibrant feel, enhancing elements rather than creating heavy stacking. The preferred method for perceived depth is through subtle background color changes (e.g., Arctic Mist for cards on Midnight Ink).

## Dos & Donts

### Do

- Always use Midnight Ink (#011c42) for large background surfaces to maintain the deep, dark theme.
- Apply Neon Cyan (#34e8bb) consistently as the background for primary call-to-action buttons.
- Headlines should leverage tomatoGrotesk (weights 300-500) for impactful presence, especially the distinctive light 300 weight at larger sizes.
- Text against dark backgrounds should primarily use Platinum White (#ffffff) for readability, reserving Slate Text (#696962) for secondary, less prominent text.
- Utilize Electric Magenta (#fb9ce5) for ghost button borders and decorative elements to provide secondary visual interest without overshadowing the primary cyan.
- Implement a 2px border radius for all filled buttons and a 8px radius for cards to distinguish interaction elements from content blocks.
- Ensure generous vertical spacing between sections (64px) to provide ample breathing room between content blocks on the dark canvas.

### Don't

- Do not introduce new saturated primary colors; stick to the Neon Cyan and Electric Magenta for brand accents.
- Avoid heavy drop shadows on individual elements; prefer subtle surface shifts or border accents for depth.
- Do not use bold weights for tomatoGrotesk headlines; the system relies on lighter weights for its distinctive authority.
- Refrain from using excessively small text sizes (below 12px) for DM Sans to maintain legibility on the dark background.
- Do not apply large, rounded corners globally; save 9999px radius primarily for small tags and pills.
- Avoid dense, unbroken blocks of text; break content with imagery or concise paragraphs to suit the high-contrast display.
- Do not default to generic monospace fonts; specifically use DM Mono for code-related text to maintain brand consistency.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
text: #ffffff
background: #011c42
border: #c0c1c6
accent: #fb9ce5
primary action: #34e8bb (filled action)

**3-5 Example Component Prompts:**
1. Create a Primary Action Button: #34e8bb background, #0b100f text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
2. Design a feature card: background "Arctic Mist" (#f8f1e5), 8px border-radius, 12px internal padding. Title should be "Cascade" using DM Sans, weight 700, size 40px, text Charcoal Black (#0b100f). Body text in DM Sans, weight 400, size 16px, text Slate Text (#696962).
3. Create a navigation link for "Pricing" using DM Sans, weight 500, size 16px, text Platinum White (#ffffff), with no background or border, and 9999px radius if applicable for very active/highlighted state as a visual indicator similar to tags.
4. Produce a pill-shaped tag reading "New Introducing Windsurf 2.0" with transparent background, Electric Magenta (#fb9ce5) text, and 9999px border radius, using DM Mono, weight 500, size 14px, letter-spacing 0.7px.

---
_Source: https://styles.refero.design/style/cfab7b43-ed24-41e9-9272-c858700b865b_
