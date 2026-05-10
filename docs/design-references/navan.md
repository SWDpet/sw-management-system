# Navan — Design Reference

> Deep Violet Efficiency – like a meticulously organized business travel brief, dark and precise yet subtly inviting.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://navan.com](https://navan.com) |
| Refero page | [https://styles.refero.design/style/a5089389-4220-4fc2-82d9-973203d2e2f5](https://styles.refero.design/style/a5089389-4220-4fc2-82d9-973203d2e2f5) |
| Theme | light |
| Industry | saas |

## Overview

This design system projects an image of sophisticated efficiency, using a deep violet primary, Navan Ink, grounded by a crisp white and muted grays. It achieves a balance between corporate authority and modern approachability through its meticulous typography, showcasing delicate letter-spacing on display fonts and precise line-heights. The overall impression is one of trusted expertise, made accessible by subtle gradients and refined component styling.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Navan Ink | `#15002c` | brand | Primary brand color, darkest text, primary navigation background in footer – establishes a deep, authoritative base. |
| Action Violet | `#6307f8` | accent | Dominant CTA button background – provides a vivid, energetic contrast against backgrounds to drive interaction. |
| Hero Gradient Purple | `#410566` | brand | Deepest color in the header radial gradient, establishing a rich foundation. |
| Highlight Pink | `#da9eff` | accent | Used for specific heading highlights – adds a touch of modern vibrancy. |
| Subtle Violet | `#e1e2fe` | accent | Background for less prominent buttons or interactive elements – a soft, supportive hue. |
| Paper White | `#ffffff` | neutral | Page backgrounds, card surfaces, critical text on dark backgrounds – ensures maximum contrast and visual space. |
| Border Ash | `#c6c6d2` | neutral | Default border color, subtle dividers – provides structure without harshness. |
| Graphite Text | `#000000` | neutral | Dominant text color for body copy and headings on light backgrounds – ensures readability. |
| Body Black | `#060000` | neutral | Alternate dark text color, body text — a slightly softer black for extended reading. |
| Slate Gray | `#5a5a72` | neutral | Secondary border color, subtle accents – provides a cool, subdued detail. |
| Whisper Gray | `#8d8da5` | neutral | Subtle text, contextual information – communicates secondary priority. |
| Muted Stone | `#70708f` | neutral | Icon fills, disabled text – blends into the background without disappearing. |
| Warm Cream | `#f7eee8` | neutral | Accent background color for specific sections – introduces a subtle warmth. |
| Soft Off-White | `#f1f1f9` | neutral | Lightest background for elevated elements or hover states – minimal visual difference from Paper White. |
| Gradient Transition Purple | `#7c51fa` | brand | Mid-point in the main header gradient, adding depth and chromatic range. |
| Gradient End Magenta | `#ffb5ce` | brand | Lightest point in the header radial gradient, providing a soft edge. |

## Typography

### Neue Hass Grotesk Text

| Key | Value |
| --- | --- |
| weight | 300, 400, 500 |
| sizes | 10px, 12px, 14px, 15px, 16px, 18px, 20px, 22px, 23px |
| lineHeight | 1.00, 1.20, 1.27, 1.31, 1.36, 1.40, 1.50, 1.56, 1.78 |
| substitute | Inter |
| role | Primary typeface for body text, UI elements, navigation. Its clean, humanist sans-serif forms maintain a professional yet approachable tone across all information densities. |

### Neue Haas Grotesk Display Pro

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| sizes | 12px, 14px, 15px, 16px, 18px, 20px, 22px, 135px |
| lineHeight | 0.87, 1.21, 1.25, 1.31, 1.33, 1.36, 1.40, 1.44, 1.50, 1.58, 1.71 |
| letterSpacing | 0.0400em |
| substitute | Inter |
| role | Used for display headings and emphasized text, where the subtle letter-spacing contributes to a refined, almost architectural feel for large typography. |

### Sanomat

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 30px, 40px, 42px, 45px |
| lineHeight | 1.00, 1.12, 1.20, 1.27, 1.35, 2.13 |
| substitute | Montserrat |
| role | Distinctive display font for main page headings, providing a unique personality and strong visual presence that differentiates prominent section titles. The varied weights allow for hierarchy within these larger forms. |

### Karla

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px |
| lineHeight | 1.50 |
| substitute | Karla |
| role | Used for specific contextual or descriptive text, providing a slightly softer, more approachable feel than Grotesk without clashing. |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 14px |
| small | 4px |
| badges | 10px |
| buttons | 9999px |

- **elementGap** — 
- **sectionGap** — 
- **cardPadding** — 
- **pageMaxWidth** — 

## Components

### Flight Booking Card

### Hero Goal Selector Cards

### G2 Rating & Awards Banner

### Primary Action Button

**Role:** Main call-to-action

Filled with Action Violet (#6307f8), white text, fully rounded (9999px radius), with 10px vertical and 28px horizontal padding. Font is Neue Hass Grotesk Text, weight 500.

### Outline Ghost Button

**Role:** Secondary action or navigation

Transparent background, Navan Ink (#15002c) text and border, fully rounded (9999px radius), with 10px vertical and 54px horizontal padding. Font is Neue Hass Grotesk Text.

### Subtle Information Button

**Role:** Tertiary action or information display

Filled with Subtle Violet (#e1e2fe), Graphite Text (#000000) text, 12px radius, zero padding for inline use. Font is Neue Hass Grotesk Text.

### Inline Text Link

**Role:** Navigation or contextual links

Transparent background, Graphite Text (#000000) text, no border or radius, zero padding. Font is Neue Hass Grotesk Text.

### Basic Content Card

**Role:** Container for grouped content

Paper White (#ffffff) background, 14px border radius, no shadow. Padding varies by content, typically 16px-20px.

### New Feature Badge

**Role:** Highlighting new features

Transparent background, white text, 10px border radius, with 6px vertical and 16px horizontal padding. This specific badge uses a unique zero radius, suggesting inline application.

### Hero Section Gradient Background

**Role:** Main visual element for banner sections

A radial gradient from Hero Gradient Purple (#410566) to Gradient End Magenta (#ffb5ce), positioned to create depth.

## Layout

The page model is primarily max-width contained, centered on the screen, with sections flowing vertically. The hero section is full-bleed, showcasing a dark, atmospheric gradient background with a prominent centered headline and product mockups. Subsequent sections alternate between two-column layouts (text on left, image/card on right or vice-versa) and centered text blocks. Card grids appear for features or choices. Vertical spacing between sections is generous, contributing to a spacious feel. Navigation is a sticky top bar, with prominent, distinct CTA buttons.

## Imagery

The visual language combines contextual product mockups and staged, professional photography. Photography often features diverse individuals in business settings, focusing on collaboration or travel scenarios, with a semi-desaturated, naturalistic color treatment. Mockups are contained, often overlapping, and feature realistic app interfaces. Icons appear filled, monochromatic, and with a moderate stroke weight, serving an explanatory and decorative role. The overall density of imagery is balanced, supporting text without dominating.

## Dos & Donts

### Do

- Use Navan Ink (#15002c) for primary dark text and key background elements.
- Apply Action Violet (#6307f8) exclusively for primary interactive elements like main CTA buttons.
- Maintain a 9999px border-radius for all primary and secondary buttons to ensure a consistent pill-shape.
- Apply 14px border-radius to all card components and elevated containers.
- Utilize Neue Haas Grotesk Display Pro with 0.0400em letter-spacing for large, impactful headlines to maintain refinement.
- Ensure all body text uses Neue Hass Grotesk Text with a line-height appropriate to its size for optimal readability, such as 1.50 for 16px.
- Use Border Ash (#c6c6d2) for all subtle divders and non-interactive borders.

### Don't

- Avoid using highly saturated colors for large content blocks; reserve them for accents and interactive elements.
- Do not introduce additional font families; stick to Neue Hass Grotesk (Text/Display) and Sanomat.
- Never use hard-edged rectangles without a radius for interactive components; apply at least 10px radius to badges and 14px to most cards.
- Do not use shadows for elevation except where explicitly defined; rely primarily on background color differentiation.
- Avoid using custom letter-spacing on Neue Hass Grotesk Text body copy; it should remain 'normal'.
- Don't deviate from the established padding values for interactive buttons (10px vertical, 28px-54px horizontal).

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #000000 (Graphite Text)
- Background: #ffffff (Paper White)
- CTA: #6307f8 (Action Violet)
- Border: #c6c6d2 (Border Ash)
- Accent: #15002c (Navan Ink)

### 3-5 Example Component Prompts
1. Create a primary call-to-action button: background Action Violet (#6307f8), text Paper White (#ffffff), border-radius 9999px, padding 10px vertical, 28px horizontal. Text is "Get Started" using Neue Hass Grotesk Text weight 500, size 16px.
2. Design a basic content card: background Paper White (#ffffff), border-radius 14px, no box-shadow, with a 20px padding. Content title uses Graphite Text (#000000), Sanomat weight 500, size 30px, line-height 1.20.
3. Implement a 'New' feature badge: transparent background, text Paper White (#ffffff), border-radius 10px, padding 6px vertical, 16px horizontal. Text is "New" using Neue Hass Grotesk Text weight 400, size 12px.
4. Construct an outline ghost button: transparent background, text Navan Ink (#15002c), border 1px solid Navan Ink (#15002c), border-radius 9999px, padding 10px vertical, 54px horizontal. Text is "Request a demo" using Neue Hass Grotesk Text weight 500, size 16px.

---
_Source: https://styles.refero.design/style/a5089389-4220-4fc2-82d9-973203d2e2f5_
