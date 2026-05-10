# Dub — Design Reference

> Crisp Utility on White Canvas

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://dub.co](https://dub.co) |
| Refero page | [https://styles.refero.design/style/b0d80806-b724-4ed1-a1d1-074edd3c9bc9](https://styles.refero.design/style/b0d80806-b724-4ed1-a1d1-074edd3c9bc9) |
| Theme | light |
| Industry | saas |

## Overview

Dub presents a high-contrast, functionally transparent productivity aesthetic. Surfaces range from pure white to subtle light grays, often paired with crisp dark text. Typography is precise and clear, with a prominent serif display font for impact and a neutral sans-serif for content. Accent colors appear as small functional highlights rather than large blocks, creating a dynamic yet understated feel. Components are lightweight, featuring soft border radii and minimal, diffused shadows, emphasizing content over heavy ornamentation.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | neutral | Page backgrounds, card surfaces, form fills, primary button text for dark buttons |
| Jet Black | `#000000` | neutral | Icon fills, outlines for ghost buttons, decorative strokes to establish boundaries |
| Ink Black | `#0a0a0a` | neutral | Primary text color for headings, body text, and prominent links |
| Thunder Gray | `#171717` | neutral | Secondary text for informational blocks, subtle accents |
| Shadow Gray | `#262626` | neutral | Tertiary text, less prominent details, active ghost button text |
| Steel Gray | `#404040` | neutral | Muted text, less emphasis on secondary elements |
| Subtle Ash | `#f5f5f5` | neutral | Subtle background for UI elements, hover states, secondary card surfaces |
| Border Light | `#e5e5e5` | neutral | Hairline borders, dividers, subtle outlines for cards and inputs |
| Border Muted | `#d4d4d4` | neutral | Default input borders, inactive states |
| Accent Blue | `#3b82f6` | brand | Decorative icons, interactive elements, button background for a primary action |
| Fresh Green | `#16a34a` | accent | Green text accent for links, tags, and emphasized short phrases. Use as a supporting accent, not as a status color |
| Warm Orange | `#ea580c` | accent | Highlight text, specific icon accents, informative badges |
| Deep Violet | `#7c3aed` | accent | Link highlights, decorative icons, secondary brand accents |
| System Info | `#111827` | neutral | Secondary body text, navigation labels, and subdued headings |
| Focus Ring Blue | `#1e40af` | brand | Primary action background, focus state indication |
| Highlight Green | `#4ade80` | accent | Background for specific callouts or positive elements |
| Highlight Violet | `#c084fc` | accent | Background for specific callouts or visual accents |
| Highlight Orange | `#fb923c` | accent | Background for specific callouts or visual accents |
| Linear Gray Dark | `#525252` | neutral | Darker shade for linear gradients, often in backgrounds or decorative elements |
| Linear Gray Light | `#737373` | neutral | Lighter shade for linear gradients, often in backgrounds or decorative elements |

## Typography

### Satoshi

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 36px, 40px, 48px |
| lineHeight | 1.00, 1.11, 1.15 |
| letterSpacing | normal |
| substitute | Montserrat |
| role | Display headlines and prominent marketing text. The semi-bold weight and clean, geometric forms provide a modern, confident voice. |

### Inter

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 8px, 10px, 11px, 12px, 13px, 14px, 16px, 18px, 20px, 24px, 30px |
| lineHeight | 1.00, 1.33, 1.38, 1.40, 1.43, 1.50, 1.56, 2.15, 2.80 |
| letterSpacing | normal |
| substitute | Inter |
| role | The primary typeface for all UI elements, body text, navigation, and detailed information. Its legibility across various sizes ensures clarity in product contexts. |

### GeistMono

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 12px, 14px, 24px |
| lineHeight | 1.00, 1.33, 1.43 |
| letterSpacing | normal |
| substitute | Roboto Mono |
| role | Used for code snippets, data displays, and elements requiring a fixed-width, precise appearance, such as tracking IDs or numeric metrics. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.4 |  |
| body | 14 |  | 1.43 |  |
| subheading | 18 |  | 1.56 |  |
| heading-sm | 24 |  | 1.33 |  |
| heading | 30 |  | 1.33 |  |
| heading-lg | 36 |  | 1.11 |  |
| display | 48 |  | 1.15 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| lg | 12px |
| md | 8px |
| xl | 16px |
| xxl | 20px |
| full | 9999px |

- **elementGap** — 16px
- **sectionGap** — 64px
- **cardPadding** — 16px
- **pageMaxWidth** — 1200px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas White | `#ffffff` | 1 | Primary page background, base for most content sections. |
| Subtle Ash | `#f5f5f5` | 2 | Secondary background for cards, recessed sections, or hover states. |
| Info Tint | `#dcfce7` | 3 | Background for specific callouts, badges, or areas needing a subtle colored highlight. |

## Components

### Primary Action Button

**Role:** Filled button for primary calls to action.

Background: Accent Blue (#3b82f6) or Focus Ring Blue (#1e40af), Text: Canvas White (#ffffff), Border Radius: 8px, Padding: 6px 12px.

### Ghost Button

**Role:** Interactive elements that blend into the background, typically for secondary actions.

Background: transparent (rgba(0,0,0,0)), Text: Ink Black (#0a0a0a) or Thunder Gray (#171717), Border: none, Border Radius: 9999px, Padding: 0px 16px.

### Outlined Card

**Role:** Informational containers with a subtle visual boundary.

Background: Canvas White (#ffffff), Border: 1px solid Border Light (#e5e5e5), Border Radius: 12px, Padding: 8px.

### Raised Card

**Role:** Elevated cards for prominent content or interactive sections.

Background: Canvas White (#ffffff), Border Radius: 16px, Shadow: rgba(0, 0, 0, 0.1) 0px 0px 0px 4px, Padding: 0px.

### Subtle Background Card

**Role:** Cards with a slightly off-white background, often used for grouped information or light contrast.

Background: Subtle Ash (#f5f5f5) or #dcfce7, Border Radius: 16px, Padding: 16px.

### Text Input

**Role:** Standard input fields for user data.

Background: Canvas White (#ffffff), Text: System Info (#111827), Border: 1px solid Border Muted (#d4d4d4) on top with clear bottom/sides, Border Radius: 0px 6px 6px 0px (asymmetric). Focus state uses a blue ring.

### Pill Tag

**Role:** Informative labels or categories.

Background: #dcfce7, Text: Fresh Green (#16a34a), Border Radius: 9999px, Padding: 6px 12px.

### Navigation Link

**Role:** Top-level navigation items.

Text: Ink Black (#0a0a0a), Hover: Underlined with Ink Black (#0a0a0a), Padding: 0px 24px.

### Featured Badge

**Role:** Small, colorful highlights for features or status.

Background: #fb923c, #c084fc, or #4ade80. Text: Ink Black (#0a0a0a) or similar dark neutral. Border radius: 9999px. Padding: dynamic based on content.

## Layout

The page typically follows a max-width contained model, centered at around 1200px, creating a structured and professional feel. The hero section often presents a centered headline and subtext, followed by primary call-to-action buttons. Sections alternate between full-width content blocks and contained content, maintaining a rhythm of visual interest. Content arrangement leans towards alternating text-left/image-right compositions, or feature grids. There's a consistent vertical spacing between sections, primarily using a large section gap, creating a sense of spaciousness. Navigation is a sticky top bar with logo, main links, and distinct login/signup buttons.

## Imagery

The visual language for imagery is primarily product-focused, featuring clean, cropped screenshots of the Dub UI. These screenshots often appear against a slightly muted background, sometimes with a soft blur or slight elevation. Icons are minimal, featuring outline styles with thin strokes in black or occasionally brand accent colors (green, orange, violet), serving as decorative or explanatory elements. There is a general absence of lifestyle photography or complex illustrations, keeping the focus squarely on the product's interface and functionality. Density is moderate, with images used to break up text blocks and demonstrate product features rather than purely decorative purposes.

## Dos & Donts

### Do

- Use Satoshi (or Montserrat) at 48px, weight 500, line-height 1.15 for primary page headings.
- Apply Canvas White (#ffffff) for all main page and card backgrounds to maintain a bright, open feel.
- Employ Ink Black (#0a0a0a) for all primary body text, headings, and crucial interactive text for maximum readability.
- Utilize Border Light (#e5e5e5) for all hairline borders and subtle dividers to define content areas without adding visual weight.
- Apply a 9999px (full pill shape) border-radius to all ghost buttons and tags for a distinctive soft, approachable quality.
- Use Element Gap of 16px to separate most inline elements and Card Padding of 16px for content within containers.
- Employ Accent Blue (#3b82f6) or Focus Ring Blue (#1e40af) as the background for primary call-to-action buttons, with Canvas White (#ffffff) text.

### Don't

- Avoid using heavy drop shadows; prefer diffused, subtle shadows like rgba(0, 0, 0, 0.05) 0px 1px 2px 0px.
- Do not deviate from Inter for body text, links, and most UI elements; consistency is key for readability and brand recognition.
- Refrain from using saturated colors as large background blocks; reserve them for small accents, highlights, or semantic indicators.
- Avoid tight line spacing for larger text; ensure line-heights are generous, especially for headings, to enhance scannability.
- Do not overcrowd sections; maintain a Section Gap of at least 64px to provide clear visual separation and breathing room.
- Do not use arbitrary border radii; adhere strictly to the defined scales: 9999px for pills, 8px for small components, 12px for cards, and 16px for larger containers.
- Do not introduce new typefaces; the combination of Satoshi (display), Inter (UI), and Geist Mono (monospace) is comprehensive.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #0a0a0a
- background: #ffffff
- border: #e5e5e5
- accent: #3b82f6
- primary action: #000000 (filled action)

Example Component Prompts:
- Create a Primary Action Button: #000000 background, #ffffff text, 9999px radius, compact pill padding. Use this filled treatment for the main CTA.
- Create a feature card: Background Canvas White (#ffffff), 12px radius, 1px solid Border Light (#e5e5e5). Title 'Advanced Analytics' at 20px Inter 600, Ink Black (#0a0a0a). Body text 'Gain deep insights into your link performance with real-time data.' at 14px Inter 400, Ink Black (#0a0a0a).

---
_Source: https://styles.refero.design/style/b0d80806-b724-4ed1-a1d1-074edd3c9bc9_
