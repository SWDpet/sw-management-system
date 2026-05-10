# Apollographql — Design Reference

> Deep space operations center: dark, high-contrast UI with a single vibrant accent for critical actions, like indicator lights on a mission control panel.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://apollographql.com](https://apollographql.com) |
| Refero page | [https://styles.refero.design/style/65b30976-3663-40b2-8751-7a360ba74539](https://styles.refero.design/style/65b30976-3663-40b2-8751-7a360ba74539) |
| Theme | mixed |
| Industry | devtools |

## Overview

Apollo's design system evokes a 'deep space operations center' feel, using a dark, desaturated palette with a single vibrant accent. The dominant near-black background and off-white text establish a high-contrast, technical atmosphere. Subtle use of a vivid orange for primary calls to action creates focused points of visual energy, preventing the dark interface from feeling monotonous. Sharp edges combined with deep, pill-shaped buttons introduce a tension between precision and approachability, reinforcing an infrastructure brand that is both powerful and user-friendly.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#15252d` | neutral | Primary background for dark sections, body text in light sections, and card surfaces. This near-black provides a deep base for content. |
| Comet Dust | `#f8f8f8` | neutral | Default text color on dark backgrounds, primary background for light sections, and neutral UI elements. Its strong luminance provides readability. |
| Nebula Gray | `#e2e8f0` | neutral | Borders for cards and interactive elements across both light and dark backgrounds, providing subtle structural definition without harsh lines. Also used as background for light sections. |
| Crater Gray | `#9fb2bc` | neutral | Subtle borders and dividers, indicating secondary separation. Its cool tone harmonizes with the dark primary colors. |
| Off-White Cloud | `#efefef` | neutral | Secondary background for light sections, providing a subtle differentiation from Comet Dust. |
| Fusion Orange | `#e75e15` | accent | Prominent call-to-action buttons, active states, and key interactive elements. Its vividness provides an unmistakable focal point against the dark base. |
| Subtle Dark Gray | `#254250` | neutral | Background for secondary buttons, offering a muted interaction point that doesn't compete with Fusion Orange. |

## Typography

### Inter

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 14px, 16px, 20px, 24px, 30px, 38px, 50px |
| lineHeight | 0.94, 1.00, 1.10, 1.25, 1.30, 1.33, 1.40, 1.43, 1.50 |
| letterSpacing | normal |
| substitute | Inter |
| role | The primary typeface for all textual content, from body to display headings. Its wide range of weights and sizes supports a clear content hierarchy, maintaining a contemporary and readable aesthetic across the interface. |

### Fira Code

| Key | Value |
| --- | --- |
| weight | 500 |
| sizes | 18px |
| lineHeight | 1.25 |
| letterSpacing | 0.0800em |
| substitute | Fira Code |
| role | Used specifically for code snippets or technical annotations. Its monospace nature and distinct letter spacing clearly differentiates technical content from standard prose, reinforcing the developer-focused nature of the product. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 |  |
| body-sm | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |
| subheading | 20 |  | 1.4 |  |
| heading | 24 |  | 1.33 |  |
| heading-lg | 30 |  | 1.3 |  |
| display | 38 |  | 1.25 |  |
| display-lg | 50 |  | 1.25 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| inputs | 8px |
| buttons | 999px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### Hero CTA Button Group

### Testimonial Card

### Feature Content Card with Buttons

### Primary Call to Action Button

**Role:** Main interactive button

Fusion Orange (#e75e15) background with Comet Dust (#f8f8f8) text. Pill-shaped with a 999px border-radius, 8px vertical padding, 16px horizontal padding. Font is Inter 400.

### Secondary Outlined Button

**Role:** Secondary action or ghost button

Transparent background with Comet Dust (#f8f8f8) text. Pill-shaped with a 999px border-radius, 1px Fusion Orange (#e75e15) border, 8px vertical padding, 16px horizontal padding. Font is Inter 400.

### Tertiary Ghost Button

**Role:** Text-only button for less prominent actions, often within navigation

Transparent background with Comet Dust (#f8f8f8) text. Pill-shaped with a 999px border-radius. Font is Inter 400.

### Header Navigation Button

**Role:** Navigation items with a subtle background

Subtle Dark Gray (#254250) background with Comet Dust (#f8f8f8) text and a Nebula Gray (#e2e8f0) border. Rounded with an 8px border-radius, 4px padding. Font is Inter 400.

### Hero Headline

**Role:** Main page title

Inter 50px, weight 700 with Comet Dust (#f8f8f8) color and 1.25 line height.

### Hero Subheadline

**Role:** Secondary text in hero section

Inter 20px, weight 400 with Comet Dust (#f8f8f8) color and 1.5 line height.

### Content Card (Dark)

**Role:** Container for featured content

Midnight Ink (#15252d) background, Nebula Gray (#e2e8f0) border, 24px border-radius, 16px inner padding. Body text is Comet Dust (#f8f8f8).

### Client Logo Grid Item

**Role:** Individual logo container in 'Trusted by' section

Implied transparent background with a Comet Dust (#f8f8f8) fill for logos on light sections, or light backgrounds for dark section. Spacing of 48px between items.

## Layout

The site employs a mixed layout strategy: the hero section is full-width with text and calls-to-action centered over a dark, subtly textured background. Subsequent sections alternate between a dark background for feature blocks and a light background for 'trusted by' logos and testimonials, maintaining a consistent vertical rhythm. Content within sections is often structured with a maximum width, centered, or in two-column layouts featuring text on one side and a visual element (like a video player or card) on the other. A grid-like structure is used for client logos and testimonial cards. Vertical spacing is generous, creating breathing room between content blocks. The navigation is a sticky top bar, consistent across the site, with a search icon and prominent 'Contact us' and 'Start for free' buttons.

## Imagery

The visual language for imagery is primarily functional and supportive, not decorative. It includes: product screenshots or abstract graphics (like the faint 'shooting stars' pattern in the hero and video placeholders) which are contained within distinct sections. Photography consists of small, circular profile pictures for team members and testimonials, establishing human connection without large, lifestyle imagery. Icons appear as outlined or filled, primarily serving navigation and feature explanation. The overall density is text-dominant, with imagery acting as focused visual anchors for specific content blocks rather than full-bleed atmospheric elements.

## Dos & Donts

### Do

- Prioritize Midnight Ink (#15252d) for prominent dark sections and Comet Dust (#f8f8f8) for text, ensuring high contrast (14.8:1) for readability.
- Use Fusion Orange (#e75e15) exclusively for primary calls-to-action and active states to maintain strong visual hierarchy and guide user attention.
- Apply a 999px border-radius to all buttons for a distinctive pill shape, contrasting with the 24px radius used for content cards.
- Maintain a clear content hierarchy using the Inter typeface, reserving weight 700 for display headlines and weight 400 for body text.
- Utilize 48px vertical spacing between major sections and 16px for internal card padding to establish comfortable density.
- Employ the Nebula Gray (#e2e8f0) color for all border elements to imply structure without strong visual interruption.

### Don't

- Do not introduce additional vibrant colors; restrict accent colors to Fusion Orange (#e75e15) to preserve visual focus.
- Avoid using harsh shadows for elevation; rely on background color changes for surface differentiation if needed, or subtle borders.
- Do not deviate from the 999px border-radius for buttons or 24px for cards; these are signature shape identifiers.
- Refrain from using monospace fonts for general body text; Fira Code (500, 18px, 0.0800em letter spacing) is strictly for code examples.
- Do not use generic gray values; always refer to defined neutrals like Midnight Ink (#15252d), Nebula Gray (#e2e8f0), or Crater Gray (#9fb2bc).
- Avoid excessive use of text treatments like underlines or italics for emphasis; hierarchy is primarily managed through font size and weight.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text on dark: #f8f8f8
- Background dark: #15252d
- Text on light: #15252d
- Background light: #e2e8f0
- CTA primary: #e75e15
- Border subtle: #e2e8f0

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Dark background #15252d with subtle texture. Centered headline 'The API Orchestration Platform' using Inter 50px weight 700 #f8f8f8. Subtext 'Connect agents and apps...' using Inter 20px weight 400 #f8f8f8. Two buttons below, centered: primary 'Start for free' (Fusion Orange, pill-shaped) and secondary 'See how it works' (outlined Fusion Orange, pill-shaped).
2. **Generate a Testimonial Card:** Use Midnight Ink (#15252d) background, 24px border-radius, with a Nebula Gray (#e2e8f0) 1px border. Inside, include a circular image placeholder 64px diameter. Testimonial text using Inter 16px weight 400 #f8f8f8, followed by name and title in Inter 14px weight 400 #9fb2bc. Padding is 16px.
3. **Build a Navigation Bar:** Use Midnight Ink (#15252d) background. Include text links (e.g., 'Platform', 'Solutions') using Inter 16px weight 400 #f8f8f8. Add a 'Contact us' button with Subtle Dark Gray (#254250) background, Nebula Gray (#e2e8f0) border, 8px radius, and 'Start for free' button with Fusion Orange (#e75e15) background, 999px radius. Both buttons have 8px vertical and 16px horizontal padding.

---
_Source: https://styles.refero.design/style/65b30976-3663-40b2-8751-7a360ba74539_
