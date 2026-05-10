# Jitter — Design Reference

> Graphic design studio on bleached white paper. A brightly lit, expansive canvas with meticulously placed, rounded interface elements and sharp, bold typography.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://jitter.video](https://jitter.video) |
| Refero page | [https://styles.refero.design/style/ab1e41e9-7d21-4762-b498-51b8c63ae7ce](https://styles.refero.design/style/ab1e41e9-7d21-4762-b498-51b8c63ae7ce) |
| Theme | light |
| Industry | design |

## Overview

Jitter's design system feels like a playful yet precise workspace, balancing modern utility with a hint of creative energy. The stark contrast between near-black text and a luminous white background creates immediate clarity, while the prominent use of rounded forms, especially large 40-50px radii, softens the otherwise sharp presentation. Energetic violet and blue accents punctuate key interactions, guiding the user through a clean, spacious interface.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Carbon | `#19171C` | neutral | Primary text, deep backgrounds for inverted elements like the 'Try for free' button headers. |
| White Canvas | `#FFFFFF` | neutral | Page backgrounds, card surfaces, primary button text color. |
| Lunar Dust | `#F2F1F3` | neutral | Subtle background for UI elements, very light card surfaces. |
| Gravel | `#E5E4E7` | neutral | Subtle borders, inactive element backgrounds. |
| Ash | `#97979B` | neutral | Secondary text, descriptive body copy. |
| Slate | `#6E6E73` | neutral | Tertiary text, less prominent information, card detail text. |
| Jitter Violet | `#7A40ED` | brand | Primary Call-to-Action buttons, interactive elements — a vibrant, playful invitation. |
| Sky Blue | `#00B2FF` | accent | Secondary accent for interactive elements, links, and highlighted content. Signifies progress or new features. |
| Lemon Drop | `#F5FF63` | accent | Background for 'new' badges, bringing a sharp, attention-grabbing highlight. |
| Soft Violet | `#A981FF` | accent | Subtle background for cards or highlighted sections. |
| Lavender Mist | `#CAB3F8` | accent | Lightest accent for subtle highlighting or hovers. |
| Nebula Gradient | `#711DE2` | brand | Background for bold, immersive sections or significant calls-to-action. |
| Aurora Gradient | `#D0BAFE` | accent | Subtle background gradient for elevated UI components or soft transitions. |

## Typography

### TWK Lausanne

| Key | Value |
| --- | --- |
| weight | 500, 600, 700, 750, 800 |
| sizes | 16px, 18px, 20px, 21px, 24px, 36px, 40px, 48px, 72px, 80px, 200px |
| lineHeight | 0.85, 0.90, 0.95, 1.00, 1.20, 1.38, 1.50, 2.50 |
| letterSpacing | -0.044em, -0.040em, -0.037em, -0.032em, -0.030em, -0.022em, -0.020em, -0.017em, -0.010em |
| substitute | Montserrat |
| role | Display and primary headings. The extremely tight letter-spacing (-0.044em at 200px) creates a dense, impactful feel, while the range of heavy weights provides gravitas without excessive width. This custom font provides a distinctive, almost technical yet modern character. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700, 800 |
| sizes | 12px, 13px, 14px, 15px, 16px, 17px, 18px, 26px |
| lineHeight | 1.10, 1.14, 1.15, 1.20, 1.25, 1.40, 1.41, 1.50, 1.60 |
| letterSpacing | -0.044em, -0.030em, -0.023em, -0.022em, -0.021em, -0.020em, -0.017em |
| substitute | Inter |
| role | Body text, navigation, secondary headings, and UI elements. Its high legibility at smaller sizes and various weights make it a versatile workhorse, maintaining clarity while supporting the visual hierarchy established by TWK Lausanne. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.44 |
| body-sm | 14 |  | 1.5 | -0.32 |
| body | 16 |  | 1.5 | -0.32 |
| subheading | 18 |  | 1.41 | -0.44 |
| heading | 26 |  | 1.25 | -0.57 |
| heading-lg | 48 |  | 0.95 | -1.92 |
| display | 80 |  | 0.9 | -2.88 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| badges | 40px |
| inputs | 0px 50px 50px 0px |
| buttons | 50px |
| default | 40px |

- **elementGap** — 12px
- **sectionGap** — 64px
- **cardPadding** — 
- **pageMaxWidth** — 860px

## Components

### Announcement Badge + CTA Button Group

### Templates Section — Pill Badge + Description Block

### Feature Statement Card

### Primary Call-to-Action Button

**Role:** Interactive element

Rounded button with Jitter Violet (#7A40ED) background and White Canvas (#FFFFFF) text. No border, large 50px border-radius, 13px vertical padding. Bold, inviting. Example: 'Try Jitter for free'.

### Secondary Call-to-Action Button

**Role:** Interactive element

Ghost button with Carbon (#19171C) text and transparent background. No border, 0px border-radius, minimum padding. Used for less prominent actions. Example: 'Log in'.

### Light Elevated Card

**Role:** Content container

White Canvas (#FFFFFF) background with a layered shadow: rgba(25, 23, 28, 0.01) 0px 152px 61px, rgba(25, 23, 28, 0.05) 0px 85px 51px, rgba(25, 23, 28, 0.09) 0px 38px 38px, rgba(25, 23, 28, 0.1) 0px 9px 21px. No border, 0px border-radius. Used for prominent content blocks requiring visual lift.

### Subtle Feature Card

**Role:** Content container

Lunar Dust (#F2F1F3) background, 40px border-radius. No shadow, 65px top padding. Used for displaying feature blocks or grouped content with a soft, distinct background.

### Search Input with Radius

**Role:** Form element

Dark background (rgba(45, 41, 51)), White Canvas (#FFFFFF) placeholder text. Unique 0px 50px 50px 0px border-radius (rounded on right side only). 13px vertical padding, 24px left padding. Used for focused input fields.

### 'New' Badge

**Role:** Informational label

Lemon Drop (#F5FF63) background, Carbon (#19171C) text. 0px border-radius, 4px horizontal and 2px vertical padding. Used for highlighting new features or content.

## Layout

The page maintains a centered, fixed-width model, predominantly 860px, providing a structured and readable experience. The hero section often features a centered headline over a background that shifts between White Canvas and the Nebula Gradient. Content is arranged in alternating sections, commonly featuring a clear heading followed by text and then a visual or interactive element. There's a consistent vertical rhythm of spacing, creating ample breathing room, especially with generous top padding for components. Navigation is a sticky top bar, containing essential links and a prominent 'Try for free' button, suggesting ease of access and clear calls-to-action.

## Imagery

The site's imagery is primarily product-focused, featuring abstract or simplified visuals that represent motion design elements. There are no photographs or complex illustrations. Instead, it utilizes clean icon outlines and abstract graphic shapes, often rendered in shades of gray or accented with Jitter Violet, Sky Blue, and Lemon Drop. These visuals are contained, rarely full-bleed, and serve an explanatory or decorative role to punctuate text blocks, maintaining the spacious, UI-centric feel. Iconography is minimal, using simple geometric shapes and clear lines.

## Dos & Donts

### Do

- Prioritize TWK Lausanne for all display text and primary headings to maintain a bold, modern voice, using the tight letter-spacing as found in the typography specification.
- Utilize Jitter Violet (#7A40ED) exclusively for primary calls-to-action to ensure maximum visual hierarchy and user guidance.
- Apply a 40px border-radius to almost all interactive elements and contained content blocks to maintain the system's signature soft, approachable shape.
- Use White Canvas (#FFFFFF) as the dominant background color for page sections and primary content areas, contrasted by deep Carbon (#19171C) text.
- Implement the layered shadow rgba(25, 23, 28, 0.01) 0px 152px 61px, rgba(25, 23, 28, 0.05) 0px 85px 51px, rgba(25, 23, 28, 0.09) 0px 38px 38px, rgba(25, 23, 28, 0.1) 0px 9px 21px for elements requiring significant visual elevation.
- Employ Inter font for all body copy and UI element text for optimal readability across various sizes and contexts.
- Maintain a clear page structure with a maximum content width of 860px, ensuring consistent alignment and readability.

### Don't

- Avoid using flat, square buttons or cards; all interactive elements should incorporate the generous 40-50px border-radius.
- Do not introduce new vibrant colors beyond the defined Jitter Violet and Sky Blue accents; the color palette is intentionally refined.
- Refrain from using excessive or varied drop shadows; stick to the defined layered shadow for elevation or no shadow for flat elements.
- Do not deviate from the specified TWK Lausanne and Inter fonts or their respective letter-spacing values, especially for headings, to preserve typographic identity.
- Avoid centering large blocks of body text; left-alignment is preferred for readability within the narrow max-width constraint.
- Do not use dark backgrounds for entire page sections unless leveraging the specific Nebula Gradient, preserving the light theme's clarity.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #19171C
- Background: #FFFFFF
- CTA: #7A40ED
- Border/Inactive: #E5E4E7
- Accent: #00B2FF

### 3-5 Example Component Prompts
1. Create a Hero Section: White Canvas background, page max-width 860px. Headline 'Super fast motion for every team' using TWK Lausanne weight 800, size 80px, line-height 0.9, color #19171C, letter-spacing -2.88px. Immediately below, a Jitter Violet CTA button (hex: #7A40ED, text: #FFFFFF, radius: 50px, padding: 13px 24px) with text 'Try Jitter for free' using Inter weight 500, size 16px.
2. Design a Feature Card: Lunar Dust background (hex: #F2F1F3), 40px border-radius, 65px top padding. Content inside should use Inter font, Slate (#6E6E73) for descriptions and Carbon (#19171C) for feature titles (Inter weight 600, size 18px).
3. Implement a 'New' Badge: Lemon Drop background (hex: #F5FF63), Carbon text (hex: #19171C), 0px border-radius, 4px horizontal padding, 2px vertical padding. Text content 'new' using Inter font weight 600, size 12px.
4. Create a Navigation Bar: Sticky top, White Canvas background. Left aligned logo (replace with Jitter logo asset if available). Right aligned links 'Product', 'Customers', 'Templates', 'Pricing' using Inter font weight 500, size 16px, color #19171C. Far right, the Header 'Try for free' Button (Carbon background #000000, White Canvas text, 50px radius).

---
_Source: https://styles.refero.design/style/ab1e41e9-7d21-4762-b498-51b8c63ae7ce_
