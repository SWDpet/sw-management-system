# Monad — Design Reference

> Softly lit control panel

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.monad.com](https://www.monad.com) |
| Refero page | [https://styles.refero.design/style/fc84e9f0-2058-4a0a-8d26-9cc1ba84ec9c](https://styles.refero.design/style/fc84e9f0-2058-4a0a-8d26-9cc1ba84ec9c) |
| Theme | light |
| Industry | ai |

## Overview

Monad employs a high-contrast, minimalist design with a soft, almost ephemeral quality achieved through understated typography and subtle, diffused gradients. The color palette is predominantly achromatic, allowing a single soft blue-gray wash to introduce a hint of color, primarily in background elements. Components generally favor soft curves and thin borders over heavy fills, maintaining an open, airy feel. The overall impression is one of clarity and focused information delivery, avoiding visual noise.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink | `#000000` | neutral | Primary text, primary icon, strong borders, dark surface backgrounds |
| Paper Canvas | `#f6f3f1` | neutral | Page backgrounds, card backgrounds, light element borders |
| Off-Black | `#242424` | neutral | Secondary text, button backgrounds, accent borders for ghost elements, navigation text |
| Pale Stone | `#4e4d4d` | neutral | Muted body text, secondary heading text, subtle borders |
| Whisper Gray | `#3d3d3d` | neutral | Decorative lines, subtle text elements |
| Atmosphere Wash | `#cfdaf5` | accent | Soft card backgrounds, decorative background fills – provides a cool, airy presence |
| Subtle Link | `#333333` | neutral | Understated link text and associated borders |
| Faint Text | `#797776` | neutral | Tertiary text or secondary link text for reduced emphasis |
| Sunset Violet gradient | `#ffa773` | accent | Decorative visual element; evokes warmth and transition. Primary hue is a soft orange |
| Sky Mint Gradient | `#a0b5eb` | accent | Decorative visual element; suggests freshness and clarity. Primary hue is a soft blue |
| Amber Glow Gradient | `#e2c161` | accent | Decorative visual element; adds a bright and energetic accent. Primary hue is a soft yellow |

## Typography

### ABC Diatype Mono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 16px, 18px, 20px, 28px |
| lineHeight | 1.00, 1.20, 1.30, 1.35 |
| letterSpacing | -0.0330em, -0.0250em, -0.0220em, -0.0200em, -0.0140em, 0.0500em |
| substitute | IBM Plex Mono |
| role | Primary UI font for all interactive elements, body text, and monospaced content. Its fixed-width character evokes precision and technicality, while its range of tracking values allows for both compact and airy presentations. |

### Untitled Serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 24px, 28px, 32px, 40px, 80px |
| lineHeight | 1.20 |
| letterSpacing | -0.0200em |
| substitute | Noto Serif |
| role | Display and section headings. The serif typeface provides a touch of classic authority, contrasting subtly with the monospace UI elements, and its specific negative letter-spacing contributes to its restrained, sophisticated presence. |

### Untitled Sans

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px |
| lineHeight | 1.35 |
| letterSpacing | -0.0200em |
| substitute | Inter |
| role | Used for specific body text instances, acting as a clean, highly readable alternative to the monospace text for longer content blocks. Its tight letter-spacing ensures a compact feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.35 | 0.05 |
| body-sm | 14 |  | 1.3 | -0.014 |
| body | 16 |  | 1.35 | -0.02 |
| subheading | 20 |  | 1.2 | -0.02 |
| heading-sm | 24 |  | 1.2 | -0.02 |
| heading | 28 |  | 1.2 | -0.02 |
| heading-lg | 40 |  | 1.2 | -0.02 |
| display | 80 |  | 1.2 | -0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 2000px |
| cards | 40px |
| other | 100px |
| buttons | 100px |

- **elementGap** — 16px
- **sectionGap** — 40px
- **cardPadding** — 40px
- **pageMaxWidth** — 1432px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Paper Canvas | `#f6f3f1` | 0 | Base page background |
| Atmosphere Wash | `#cfdaf5` | 1 | Elevated card and section backgrounds |
| Off-Black | `#242424` | 2 | Primary filled button backgrounds, dark UI elements |
| Ink | `#000000` | 3 | Top-level notification bar, highest contrast elements |

## Components

### Primary Filled Button

**Role:** Call-to-action button for initiating primary actions.

Background: 'Off-Black' (#242424), Text: 'Paper Canvas' (#f6f3f1). Padding: 16px horizontal, 16px vertical. Radius: 100px. Uses ABC Diatype Mono, weight 400.

### Secondary Ghost Button

**Role:** Call-to-action button for secondary actions.

Background: transparent. Border: 1px solid 'Off-Black' (#242424). Text: 'Off-Black' (#242424). Padding: 16px horizontal, 16px vertical. Radius: 100px. Uses ABC Diatype Mono, weight 400.

### Inline Text Link

**Role:** Navigational or contextual links within body text.

Text: 'Ink' (#000000) or 'Subtle Link' (#333333). Padding: 5px horizontal. No border or background. Uses ABC Diatype Mono.

### Hero Headline

**Role:** Prominent display text for main page sections.

Text: 'Ink' (#000000). Font: Untitled Serif, weight 400, size 80px, lineHeight 1.2, letterSpacing -0.0200em.

### Feature Card

**Role:** Container for showcasing features or information blocks.

Background: 'Atmosphere Wash' (#cfdaf5). Border-radius: 40px. Padding: 40px. No explicit shadow, relies on background color differentiation.

### Navigation Item

**Role:** Top navigation links.

Text: 'Off-Black' (#242424). Underlined on hover with 'Off-Black'. Uses ABC Diatype Mono. Padding: 8px vertical, 10px horizontal.

### Notification Bar

**Role:** Top-level information banner.

Background: 'Ink' (#000000). Text: 'Paper Canvas' (#f6f3f1). Button text: 'Paper Canvas' (#f6f3f1), button border: 'Paper Canvas' (#f6f3f1). Font: ABC Diatype Mono, size 14px.

### FAQ Accordion Item

**Role:** Interactive question and answer section.

Background: transparent. Border-bottom: 1px solid 'Pale Stone' (#4e4d4d). Text: 'Ink' (#000000) or 'Pale Stone' (#4e4d4d). Font: ABC Diatype Mono, weight 400.

### Diagram Node Tag

**Role:** Small, descriptive labels within visual diagrams.

Background: transparent. Border: 1px solid 'Ink' (#000000) or 'Off-Black' (#242424) or 'Faint Text' (#797776). Text: matching border color. Radius: 2000px (pill shape). Font: ABC Diatype Mono, size 12px, letterSpacing 0.0500em.

## Layout

The page uses a contained layout with a maximum width of 1432px, centered for content. The hero section features a centered headline and subtext over whitespace, followed by primary and secondary call-to-action buttons. Sections maintain a consistent vertical rhythm with 40px section gaps. Content is arranged in alternating patterns, typically featuring text on one side and a visual element or card on the other, or stacked elements for FAQs. Navigation is a sticky top bar with left-aligned branding and right-aligned actions, featuring a distinct black notification bar above it. Overall density is comfortable and spacious.

## Imagery

This site features abstract, somewhat ethereal illustrations and small product-like diagrams rather than photography. Illustrations are colorful, often incorporating linear gradients, and appear contained within sections as decorative elements or to explain concepts. Icons are outlined, fine-lined, and predominantly 'Ink' (#000000) or 'Pale Stone' (#4e4d4d), used minimally for functionality rather than decoration. Imagery is used to provide visual breathing room and metaphoric explanation, not direct product showcase or social proof, and keeps a relatively low density on the page.

## Dos & Donts

### Do

- Prioritize 'ABC Diatype Mono' for all UI elements and body text to maintain the brand's precision-oriented voice.
- Use 'Untitled Serif' exclusively for primary section headings and display text to establish visual hierarchy and gravitas.
- Apply 'Ink' (#000000) for all primary text and critical interactive elements, ensuring high contrast against light backgrounds.
- Employ 'Paper Canvas' (#f6f3f1) as the default background for general page content and 'Atmosphere Wash' (#cfdaf5) for subtle visual differentiation on cards or specific content blocks.
- Ensure all buttons have a 100px border-radius, creating a consistent 'pill' shape.
- Utilize a minimal 1px border for ghost buttons and tags, defaulting to 'Off-Black' (#242424) or 'Ink' (#000000) for definition.
- Maintain 40px of internal padding for cards and primary CTAs to create generous breathing room.

### Don't

- Avoid using bright, saturated colors for primary UI elements; reserve them strictly for decorative gradients or illustrations and only as an accent.
- Do not introduce sharp corners into interactive elements or cards; all elements should adhere to the established radii of 40px, 100px, or 2000px.
- Do not use heavy shadows or gradients on interactive elements; rely on color contrast and subtle borders for distinction.
- Refrain from using multiple font families beyond the defined 'ABC Diatype Mono', 'Untitled Serif', and 'Untitled Sans'.
- Do not deviate from the specified letter-spacing for headings; the negative tracking is a signature visual characteristic.
- Avoid dense or cluttered layouts; prioritize ample whitespace and consistent section gaps (40px) to maintain clarity.
- Do not use dark backgrounds for sections without explicit reasoning; maintain the dominant light theme.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #000000
background: #f6f3f1
border: #242424
accent: #cfdaf5
primary action: no distinct CTA color

Example Component Prompts:

1. Create a Hero Section with a primary headline: 'Security Data Pipelines, Made Easy' using Untitled Serif weight 400, 80px, #000000, letter-spacing -0.0200em. Below it, a monochromatic filled button 'Get Started' with background #242424, text #f6f3f1, 100px radius, 16px vertical padding, 16px horizontal padding, using ABC Diatype Mono 400.

2. Design a Feature Card for 'Managed Data Pipelines'. Background #cfdaf5, border-radius 40px, internal padding 40px. The feature title 'Managed Data Pipelines' uses ABC Diatype Mono weight 500, 20px, #000000. Underneath, a short description 'Connect any security tool in minutes...' uses ABC Diatype Mono weight 400, 16px, #4e4d4d.

3. Construct a Navigation Bar. Background #f6f3f1. On the left, brand logo. On the right, a ghost button link 'Get A Demo >' with text #242424, a 1px border #242424, 100px radius, 16px vertical padding, 16px horizontal padding. Other navigation links like 'Platform' use ABC Diatype Mono weight 400, 16px, #242424, with no background or border.

---
_Source: https://styles.refero.design/style/fc84e9f0-2058-4a0a-8d26-9cc1ba84ec9c_
