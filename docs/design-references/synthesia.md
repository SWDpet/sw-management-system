# Synthesia — Design Reference

> Indigo-accented data canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.synthesia.io](https://www.synthesia.io) |
| Refero page | [https://styles.refero.design/style/36a7e1ed-8d14-456b-b828-ff4f47797a74](https://styles.refero.design/style/36a7e1ed-8d14-456b-b828-ff4f47797a74) |
| Theme | light |
| Industry | ai |

## Overview

Synthesia presents a bright, spacious interface with a focus on powerful functionality. A clean, almost monochromatic base of white and soft grays provides an expansive canvas, punctuated by a vibrant, deep indigo. This accent color serves as a beacon for primary actions and brand elements, making the UI feel purposeful and energetic without visual clutter. Components favor soft, rounded corners and subtle shadows, maintaining an approachable yet sophisticated aesthetic for a high-tech product.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, elevated card surfaces, main panel fills |
| Subtle Ash | `#e6eaf4` | neutral | Subtle background for navigation elements, muted card backgrounds, and very light borders |
| Ghost Fog | `#f5f8ff` | neutral | Lightest background for cards and secondary badges, creating a slight visual separation |
| Slate Text | `#333b52` | neutral | Primary body text, neutral action borders, general UI elements |
| Midnight Indigo | `#0d0f2c` | brand | Dominant text for headlines, strong accents, and footer backgrounds — signifying brand presence and authority |
| Muted Gray | `#656c86` | neutral | Secondary text, navigation links, ghost button text and borders — used for less prominent information |
| Input Border | `#d1d6e5` | neutral | Input field borders, divider lines, subtle button borders |
| Action Indigo | `#3e57da` | brand | Primary call-to-action buttons, active states, key interactive icons — the site's main interactive color |
| Sky Veil | `#c6d7fe` | accent | Soft accent in card backgrounds and decorative borders, suggesting a subtle glow |
| Deep Accent Indigo | `#1f235b` | brand | Darker accent for badge text and outlines, providing deep contrast against lighter backgrounds |
| Hint Gray | `#969bb5` | neutral | Placeholder text in input fields, supporting detail text |
| Lime Green Success | `#1a280b` | semantic | Text for success badges |
| Vivid Azure | `#8098f9` | brand | Prominent link text and secondary interactive elements, providing a lighter indigo option |
| Soft Green Background | `#ebf6df` | semantic | Background for success badges |
| Warm Orange Error | `#42230a` | semantic | Text for warning badges |
| Fuzzy Yellow Background | `#fffadb` | semantic | Background for warning badges |
| Blue Gradient Base | `#e3ebff` | accent | Starting color for decorative linear gradients |

## Typography

### Basiersquare Webfont

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 15px, 16px, 18px, 20px, 28px, 40px, 56px, 88px |
| lineHeight | 1.00, 1.04, 1.10, 1.20, 1.21, 1.40, 1.43, 1.44, 1.50, 1.57, 1.60 |
| letterSpacing | -0.0500em at 88px, -0.0020em at 12px |
| role | Custom geometric sans-serif for all text. Its clean, sharp lines contribute to a modern, technical feel. Tight letter-spacing on larger sizes gives headlines a precise, compact presence. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.24 |
| body-sm | 14 |  | 1.43 | -0.49 |
| subheading | 18 |  | 1.44 | -0.72 |
| heading-sm | 20 |  | 1.4 | -0.8 |
| heading | 28 |  | 1.2 | -0.98 |
| heading-lg | 40 |  | 1.1 | -1.4 |
| display | 56 |  | 1.04 | -1.96 |
| display-xl | 88 |  | 1 | -4.4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 24px |
| badges | 999px |
| inputs | 12px |
| buttons | 12px |
| smallComponents | 16px |

- **elementGap** — 24px
- **sectionGap** — 40px
- **cardPadding** — 32px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 0 | Primary page background and base for most content sections. |
| Ghost Fog | `#f5f8ff` | 1 | Slightly elevated background for cards and secondary panels, offering subtle differentiation from the main canvas. |
| Canvas White Elevated | `#ffffff` | 2 | Card surfaces and modal backgrounds that receive a distinct shadow for elevation. |

## Components

### Primary Filled Button

**Role:** Main call-to-action button, signaling key interactions.

Background: Action Indigo (#3e57da), Text: Canvas White (#ffffff), Border Radius: 12px, Padding: 16px vertical, 24px horizontal. Typography: Basiersquare Webfont, 16px, weight 500.

### Secondary Outlined Button

**Role:** Subtle alternative actions, often paired with a primary button.

Background: Canvas White (#ffffff), Text: Midnight Indigo (#0d0f2c), Border: 1px solid Input Border (#d1d6e5), Border Radius: 10px, Padding: 12px vertical, 18px horizontal. Typography: Basiersquare Webfont, 15px, weight 400.

### Ghost Link Button

**Role:** Discreet, text-based actions found in navigation or inline.

Background: transparent, Text: Muted Gray (#656c86), Border: none, Border Radius: 0px, Padding: 0px. Typography: Basiersquare Webfont, 14px, weight 400.

### Elevated Card

**Role:** Content container that gently floats above the page background, drawing attention.

Background: Canvas White (#ffffff), Border Radius: 24px, Padding: 32px, Shadow: rgba(16, 24, 40, 0.08) 0px 2px 16px 0px.

### Subtle Background Card

**Role:** Content container with a soft background, used for grouping related information.

Background: Ghost Fog (#f5f8ff), Border Radius: 16px, Padding: variable (e.g. 0px for image cards). No shadow.

### Text Input Field

**Role:** Standard input for user data.

Background: Canvas White (#ffffff), Text: Slate Text (#333b52), Placeholder: Hint Gray (#969bb5), Border: 1px solid Input Border (#d1d6e5), Border Radius: 12px, Padding: 12px vertical, 16px horizontal.

### Success Badge

**Role:** Indicates positive status or categorization.

Background: Soft Green Background (#ebf6df), Text: Lime Green Success (#1a280b), Border Radius: 999px (pill-shaped), Padding: 2px vertical, 8px horizontal. Typography: Basiersquare Webfont, 12px, weight 400.

### Promotional Badge

**Role:** Highlights features or special states.

Background: Ghost Fog (#f5f8ff), Text: Deep Accent Indigo (#1f235b), Border Radius: 999px (pill-shaped), Padding: 8px vertical, 16px horizontal. Typography: Basiersquare Webfont, 14px, weight 500.

## Layout

The page adheres to a max-width contained layout, typically centered at 1200px. The hero section is full-bleed, often featuring a subtle background gradient or dark panel with a centered headline and description, highlighting the primary call to action. Section rhythm is primarily consistent, with generous vertical spacing (40px between sections) and alternating light/dark (white/indigo gradient) backgrounds to visually break up content. Content arrangement frequently uses alternating text-left/image-right or vertical stacks for features, and common 3-column card grids. Overall density is comfortable, providing ample breathing room. Navigation is a sticky top bar with a clear brand logo, primary navigation links, and distinct 'Log in' and 'Get started' buttons.

## Imagery

Imagery style is product-focused, featuring tightly cropped product screenshots and AI avatars presented on clean, often white or lightly textured backgrounds. Photography of people tends to be professional, well-lit, and directly engaging. Illustrations are minimal, flat, and serve to support UI elements rather than dominate. Icons are filled, with a medium stroke weight appearance, contributing to the clean and functional aesthetic. Imagery acts primarily to explain product features, showcase AI capabilities, and demonstrate social proof, maintaining a high level of polish and visual clarity against the predominantly white UI.

## Dos & Donts

### Do

- Prioritize Canvas White (#ffffff) and Ghost Fog (#f5f8ff) for large background areas to maintain a bright, open feel.
- Use Action Indigo (#3e57da) exclusively for primary calls-to-action and key interactive elements to ensure visual clarity and drive user action.
- Apply a 12px border radius for buttons and input fields, and 24px for main content cards to achieve a consistent soft, modern look.
- Reserve Midnight Indigo (#0d0f2c), 88px, -0.0500em for display headlines to create a bold, commanding presence with precise tracking.
- Implement the rgba(16, 24, 40, 0.08) 0px 2px 16px 0px shadow for elevated cards to provide subtle depth without heaviness.
- Maintain a clear hierarchy with Slate Text (#333b52) for main body content and Muted Gray (#656c86) for secondary information or helper text.
- Ensure generous spacing, using 24px as the primary element gap and 32px for card padding, to enhance readability and visual comfort.

### Don't

- Do not introduce new saturated primary colors beyond Action Indigo (#3e57da); the brand relies on a single dominant accent.
- Avoid sharp corners; all interactive and content-holding components should embrace a rounded aesthetic (min. 12px radius).
- Do not use heavy, opaque shadow layers; stick to the light, diffuse shadows provided for elevation.
- Refrain from using thin, light fonts for body copy; ensure sufficient weight (400 or 500) and contrast for readability against light backgrounds.
- Do not overcrowd sections; maintain the generous 40px section gap to provide breathing room between content blocks.
- Avoid using multiple border treatments on the same component; stick to either solid borders or shadows for definition.
- Do not deviate from Basiersquare Webfont; its distinct character is central to the brand's typographic identity.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #333b52
- background: #ffffff
- border: #d1d6e5
- accent: #3e57da
- primary action: #3e57da (filled action)

Example Component Prompts:
1. Create a hero section: full-width dark indigo gradient background (linear-gradient(90deg, rgb(128, 152, 249), rgb(62, 87, 218) 50%, rgb(44, 67, 184))). Headline 'Accelerate Video Creation with AI' in Basiersquare Webfont, 88px, weight 500, #ffffff, letter-spacing -4.4px. Subtext 'Generate studio-quality videos in minutes' in Basiersquare Webfont, 20px, weight 400, #c6d7fe. A primary button 'Get Started' with background #3e57da, text #ffffff, 12px radius, 16px 24px padding.
2. Create a feature card: background #f5f8ff, 16px radius, 32px padding on all sides. Headline 'Personalized Content' in Basiersquare Webfont, 28px, weight 500, #0d0f2c, letter-spacing -0.98px. Body text 'Dynamically create videos for individual customer segments.' in Basiersquare Webfont, 15px, weight 400, #333b52.
3. Create an input field for email: background #ffffff, border 1px solid #d1d6e5, 12px radius, 12px 16px padding. Placeholder text 'Enter your email' in Basiersquare Webfont, 15px, weight 400, #969bb5. Label above 'Email Address' in Basiersquare Webfont, 14px, weight 400, #333b52.
4. Create a success badge: background #ebf6df, text #1a280b, 999px radius, 2px 8px padding. Text: 'Live Now' in Basiersquare Webfont, 12px, weight 400.

---
_Source: https://styles.refero.design/style/36a7e1ed-8d14-456b-b828-ff4f47797a74_
