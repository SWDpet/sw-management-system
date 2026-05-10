# Google for Education — Design Reference

> Academic blueprint on a clean whiteboard. Clarity and structure in a digitally-enhanced learning space.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://classroom.google.com](https://classroom.google.com) |
| Refero page | [https://styles.refero.design/style/c57ba3f8-1d76-4660-8ba4-48ddce26e759](https://styles.refero.design/style/c57ba3f8-1d76-4660-8ba4-48ddce26e759) |
| Theme | light |
| Industry | other |

## Overview

This design system feels like a thoughtfully organized learning environment, clean and accessible, with a strong institutional identity. Its visual precision is built on harmonious Google Sans typography and a distinctive palette of blue and green accents on a stark white background. The use of soft border radii and subtle shadow planes prevents the interface from feeling sterile, instead fostering an approachable and encouraging atmosphere.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Classroom Blue | `#1a73e8` | brand | Primary brand color for interactive elements, links, CTAs, and key iconography. Creates visual energy and guides user action. |
| Educator Green | `#188038` | brand | Secondary brand accent, used for prominent iconography and success indicators. Signals positive engagement and growth. |
| Interactive Blue | `#1967d2` | brand | Slightly darker blue, often seen on buttons and interactive text secondary to Classroom Blue. Provides depth for interactive states. |
| Sky Tint | `#e8f0fe` | accent | Very pale blue background for subtle section breaks or highlight areas. Adds a touch of softness to the white. |
| Mint Glaze | `#ceead6` | accent | Light green background used sparingly for success-oriented contexts or soft accent panels. Evokes natural growth and support. |
| Page White | `#f8f9fa` | neutral | Dominant background for the entire page, providing a clean, expansive canvas. |
| Text Dark | `#202124` | neutral | Primary text color for headlines and most body content. High contrast for readability. |
| Text Medium | `#3c4043` | neutral | Secondary text color for navigation, less prominent headings, and some body text. Slightly softer than Text Dark. |
| Text Subtle | `#5f6368` | neutral | Subtle text for assistive copy, descriptions, and inactive states. Maintains readability without overpowering primary content. |
| Border Light | `#dadce0` | neutral | Light gray for borders, dividers, and outlines on non-interactive elements. Defines boundaries without harshness. |

## Typography

### Google Sans Display

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 16px, 18px, 20px, 22px, 28px, 48px, 80px |
| lineHeight | 1.09, 1.15, 1.17, 1.20, 1.29, 1.40, 1.50, 1.56, 1.75 |
| letterSpacing | -0.0100em, -0.0060em, 0.0010em, 0.0310em |
| substitute | system-ui |
| role | Used for all major headings and display text. Its sans-serif clarity and relatively generous letter-spacing at larger sizes ensure prominent yet approachable statements. The careful weight distinctions from 400 to 700 help establish a clear hierarchy. |

### Google Sans Text

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| sizes | 12px, 14px, 16px, 18px |
| lineHeight | 1.00, 1.44, 1.50, 1.56, 1.63, 1.67, 1.71 |
| letterSpacing | 0.0060em, 0.0170em, 0.0360em |
| substitute | system-ui |
| role | The primary font for body text, navigation, buttons, and other UI elements. Its legibility across various sizes and weights (especially the lighter 300) contributes to the system's clean, understated feel, making information easy to process. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0.072 |
| body-sm | 14 |  | 1.57 | 0.05 |
| body | 16 |  | 1.5 | 0.0096 |
| subheading | 18 |  | 1.56 | 0.009 |
| heading | 28 |  | 1.29 | -0.01 |
| heading-lg | 48 |  | 1.2 | -0.006 |
| display | 80 |  | 1.09 | -0.008 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 200px |
| default | 8px |

- **elementGap** — 8px
- **sectionGap** — 64px
- **cardPadding** — 24-32px
- **pageMaxWidth** — 

## Components

### Button Group — Primary + Secondary CTAs

### AI Feature Cards — Educator + Student

### Region Selector Modal Dialog

### Primary Call to Action Button

**Role:** Main interactive element

Text: Google Sans Text, weight 500, 16px, color #ffffff. Background: #1a73e8. Border radius: 200px (fully rounded). Padding: 2px vertical, 24px horizontal. This button immediately signals a primary action with its vivid blue and pill shape.

### Secondary Outline Button

**Role:** Alternative interactive element

Text: Google Sans Text, weight 500, 16px, color #1a73e8. Background: #ffffff. Border: 1px solid #dadce0. Border radius: 200px. Padding: 2px vertical, 24px horizontal. This button provides a lighter alternative, drawing less attention while still being clearly actionable.

### Navigation Link Button

**Role:** Navigation or secondary action

Text: Google Sans Text, weight 400, 16px, color #5f6368. Background: transparent. No border or radius. Padding: 12px vertical, 8px horizontal. A low-emphasis interactive element for navigation menus or secondary actions, relying on text color for subtle interaction.

### Inline Text Link

**Role:** Contextual navigation

Text: Google Sans Text or Google Sans Display, color #1967d2. Background: transparent. No border or radius. No padding beyond text. Appears inline with body text, indicating clickable content like 'Learn more' or internal navigation within a paragraph.

### Feature Card

**Role:** Content container

Background: #ffffff. Border radius: 8px. Shadow: subtle, not explicitly defined but visually present to lift from background. Padding: Varies, often 24px. Displays product features with screenshots and descriptions.

### Modal Dialog

**Role:** Overlay content

Background: #ffffff. Border radius: 8px. Shadow: visually present to create elevation. Padding: 24px. Contains forms or critical information, shown centrally over a blurred background.

### Language Selector Input

**Role:** Form element

Text: Google Sans Text, color #5f6368. Background: transparent. Border: 1px solid #5f6368 on bottom only. Padding: 0px vertical, 24px right (for dropdown icon). Minimalist, blending into the UI while indicating input functionality.

### Chip / Tag

**Role:** Categorization or filtration

Text: Google Sans Text, weight 500, 14px, color #3c4043. Background: #ffffff or light accent color. Border: 1px solid #dadce0. Border radius: 200px. Padding: 2px vertical, 24px horizontal. Used for filtering or categorizing content, with a soft pill shape.

## Layout

The page model is primarily max-width contained, centered on the screen, creating a sense of order. The hero section often features a large, centered headline (80px Google Sans Display) over a clean white background. Content sections follow a consistent rhythm of alternating white and light-accent bands (e.g., Sky Tint), with ample vertical spacing (64px section gap). Content is arranged in alternating text-left/image-right or text-right/image-left patterns, often in two columns, creating a dynamic yet balanced flow down the page. Feature areas sometimes use a 2-column or 3-column card grid. The navigation is a persistent top bar with a 'Contact Sales' primary button, and secondary navigation elements are minimal or contextual within sections.

## Imagery

The visual language is characterized by clean, context-free product screenshots of the Google Classroom interface. These are typically contained within rounded-corner frames (8px or 24px radius), presented against either the Page White background or the subtle accent backgrounds like Sky Tint. Icons are primarily filled or outlined, using Classroom Blue or Educator Green, with a consistent, relatively thin stroke weight. Imagery serves an explanatory and product-showcase role, demonstrating functionality rather than creating atmosphere, leading to a text-dominant layout with images serving as clear, illustrative anchors.

## Dos & Donts

### Do

- Prioritize Google Sans Display for all hierarchical headlines to maintain authoritative, clear communication.
- Use Classroom Blue (#1a73e8) exclusively for primary calls to action and critical interactive elements.
- Employ the 200px border-radius for all primary buttons and interactive 'chips' to maintain the signature pill shape.
- Maintain a clear visual hierarchy by differentiating text with Text Dark (#202124), Text Medium (#3c4043), and Text Subtle (#5f6368).
- Apply Border Light (#dadce0) for all subtle dividers, borders, and outlined states to ensure a soft, unobtrusive separation.
- Utilize a base spacing unit of 8px, scaling up consistently for element, card, and section spacing (e.g., 8px, 16px, 24px, 32px, 64px).
- Employ Sky Tint (#e8f0fe) or Mint Glaze (#ceead6) for subtle background shifts to break content monotony without harsh transitions.

### Don't

- Avoid using harsh shadows; instead, opt for subtle, barely-there elevations to maintain the light, modern aesthetic.
- Do not introduce new typefaces; strictly adhere to Google Sans Display and Google Sans Text.
- Refrain from using saturated colors other than Classroom Blue or Educator Green to prevent visual clutter and maintain brand consistency.
- Do not use sharp corners; the soft 8px radius for containers and the 200px radius for buttons are fundamental to the system's character.
- Avoid dense, text-heavy blocks without visual breaks; incorporate generous white space and imagery to maintain readability and visual comfort.
- Do not deviate from the established type scale; maintain consistent font sizes and line heights for defined roles (e.g., headline, body, caption).
- Do not use dark backgrounds for main content areas; maintain Page White (#f8f9fa) as the dominant background color to reinforce the light theme.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #202124
- Background: #f8f9fa
- CTA: #1a73e8
- Border: #dadce0
- Accent: #188038

### 3-5 Example Component Prompts
1. Create a hero section: background #f8f9fa. Centered headline 'Where teaching and learning come together' in Google Sans Display, 80px, weight 500, #202124, letter-spacing -0.008em, line-height 1.09. Below, a body paragraph in Google Sans Text, 18px, weight 400, #5f6368, line-height 1.56. Followed by two buttons: one primary 'Contact sales' (background #1a73e8, text #ffffff, 200px radius, 2px vertical 24px horizontal padding) and one secondary 'Sign in to Classroom' (background #ffffff, text #1a73e8, 1px solid #dadce0 border, 200px radius, 2px vertical 24px horizontal padding), with 24px horizontal gap between them.
2. Design a feature card: background #f8f9fa, 8px border-radius, subtle box-shadow. Inside, an image with 16px radius. Below the image, a heading 'AI tools built for educators' in Google Sans Display, 22px, weight 500, #202124. Then body text in Google Sans Text, 16px, weight 400, #5f6368. Conclude with an inline text link 'Try now' in Google Sans Text, 16px, color #1967d2.
3. Build a modal dialog: background #ffffff, 8px border-radius, significant box-shadow. Inside, a toggle switch for 'Español' (Google Sans Text, 16px, #202124). Below, a dropdown input for 'España (Español)' with bottom border #5f6368, no vertical padding, 24px right padding. Followed by a 'Stay here' link (Google Sans Text, 16px, #1a73e8) and a 'Go' button (background #1a73e8, text #ffffff, 200px radius, 2px vertical 24px horizontal padding), 8px gap between them.

---
_Source: https://styles.refero.design/style/c57ba3f8-1d76-4660-8ba4-48ddce26e759_
