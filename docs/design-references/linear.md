# Linear — Design Reference

> Midnight Command Center: A dark, layered interface lit by precise accents, like a high-tech control panel.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://linear.app](https://linear.app) |
| Refero page | [https://styles.refero.design/style/90ce5883-bb24-4466-93f7-801cd617b0d1](https://styles.refero.design/style/90ce5883-bb24-4466-93f7-801cd617b0d1) |
| Theme | dark |
| Industry | ai |

## Overview

Linear presents a sophisticated and focused dark-mode experience, reminiscent of a command center dashboard. A deep charcoal base creates a serious, immersive canvas, while subtle gradients and layered surfaces build depth without harsh contrasts. Distinctive muted text colors (#8a8f98 for secondary, #62666d for tertiary) maintain readability against the dark backdrop. Critically, interaction is marked by a single vivid lime green (#e4f222), applied selectively to primary calls to action, preventing visual clutter and guiding the user's eye with precision.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Pitch Black | `#08090a` | neutral | Page background, primary surface for base elements, subtly integrated into shadows for depth. |
| Graphite | `#0f1011` | neutral | Elevated card backgrounds, slightly lighter than the canvas to denote layering. |
| Deep Slate | `#161718` | neutral | Secondary elevated card backgrounds, providing another layer of visual hierarchy. |
| Charcoal Grey | `#23252a` | neutral | Borders and some shadowed card surfaces, framing elements with a subtle distinction. |
| Muted Ash | `#323334` | neutral | Subtle borders and dividers, indicating soft separations within the dark theme. |
| Gunmetal | `#383b3f` | neutral | Tertiary background elements and input borders, a darker neutral for functional elements. |
| Porcelain | `#f7f8f8` | neutral | Primary text and icons, providing strong contrast for readability against dark backgrounds. |
| Light Steel | `#d0d6e0` | neutral | Secondary text and borders, for less prominent information or structural lines. |
| Storm Cloud | `#8a8f98` | neutral | Tertiary text, descriptive labels, and inactive states, recedes into the background for low-priority details. |
| Fog Grey | `#62666d` | neutral | Muted text for metadata, timestamps, and further de-emphasized content. |
| Alabaster | `#e5e5e6` | neutral | Informational borders and subtle fills, often seen in code blocks or explanatory components. |
| Neon Lime | `#e4f222` | brand | Primary action indicators, active states, and focus elements — a high-energy focal point. |
| Aether Blue | `#5e6ad2` | accent | Decorative highlights and occasional background elements, suggesting a technological or informational context. |
| Forest Green | `#008d2c` | semantic | Positive status indicators, success messages, and related iconography. |
| Cyan Spark | `#02b8cc` | accent | Informational highlights and unique icon fills, providing a cool accent. |
| Emerald | `#27a644` | semantic | Success and completion states, often paired with green text. |
| Warning Red | `#eb5757` | semantic | Observed in icon fill, body borderColor, other fill. Extracted usage does not support a distinct primary control color. |
| Deep Violet | `#6366f1` | accent | Background accents in specific content blocks, indicating a distinct informational category. |
| Amethyst | `#8b5cf6` | accent | Another variant of violet for backgrounds, used interchangeably with Deep Violet for visual diversity. |

## Typography

### Inter Variable

| Key | Value |
| --- | --- |
| weight | 300, 400, 510, 590 |
| weights | 300, 400, 510, 590 |
| sizes | 10px, 11px, 12px, 13px, 14px, 15px, 16px, 17px, 20px, 24px, 32px, 48px, 64px, 72px |
| lineHeight | 1.00, 1.13, 1.20, 1.33, 1.40, 1.47, 1.50, 1.60, 2.00, 2.46, 2.75 |
| letterSpacing | -0.22, -0.15, -0.13, -0.12, -0.11, -0.1 |
| fontFeatureSettings | "cv01", "ss03" |
| substitute | Inter |
| role | Primary UI typeface for all content including headings, body text, and interactive elements. Its variable weights provide a clean, modern aesthetic with strong technical readability. |

### Berkeley Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 12px, 13px, 14px |
| lineHeight | 1.30, 1.40, 1.50, 1.71 |
| letterSpacing | -0.15 |
| substitute | IBM Plex Mono |
| role | Monospaced font for code snippets, technical details, and certain data displays, ensuring consistent character alignment and technical clarity. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.4 | -0.1 |
| body | 14 |  | 1.4 | -0.13 |
| heading | 24 |  | 1.33 | -0.22 |
| heading-lg | 48 |  | 1.2 | -0.22 |
| display | 72 |  | 1 | -0.22 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 9999px |
| tags | 2px |
| cards | 6px |
| badges | 4px |
| inputs | 6px |
| buttons | 6px |
| default | 6px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Pitch Black Canvas | `#08090a` | 0 | Base page background and deepest surface level. |
| Graphite Card | `#0f1011` | 1 | Primary card surface for general content, slightly elevated from the canvas. |
| Deep Slate Elevated Card | `#161718` | 2 | More prominent card surface, used for focused content sections or lists. |
| Charcoal Grey Overlay | `#23252a` | 3 | Accent surface for borders, shadows, and subtle overlays, providing clear separation. |

## Components

### Issue Card

### CTA Button Group

### Issue List Board

### Primary Action Button

**Role:** Call to action button

Filled button with 'Neon Lime' background (#e4f222), 'Pitch Black' text (#08090a), 6px border-radius, and variable padding. Used for primary user actions.

### Ghost Navigation Button

**Role:** Navigation and secondary actions

Ghost button with transparent background, 'Porcelain' text (#f7f8f8), no explicit padding, and 0px border-radius. Navigational links or simple interactive elements.

### Subtle Link Button

**Role:** Tertiary actions and links

Ghost button with transparent background, 'Light Steel' text (#d0d6e0), 6px border-radius, and minimal padding (0px top/bottom, 6px left/right). Used for less prominent interactive elements or textual links.

### Navigation Item Button

**Role:** Sidebar navigation items

Ghost button with transparent background, 'Storm Cloud' text (#8a8f98), 2px border-radius, and no explicit padding. Used for items in a navigation list.

### Default Card

**Role:** Content container

Card with 'Graphite' background (#0f1011), 6px border-radius, and an outer shadow of rgba(0, 0, 0, 0.4) 0px 2px 4px 0px. Padding is 8px on all sides.

### Elevated Card

**Role:** Prominent content container

Card with 'Deep Slate' background (#161718), 12px top border-radius (0px bottom), and an inset shadow of rgb(35, 37, 42) 0px 0px 0px 1px. Padding is 24px vertical and 0px horizontal.

### Nested Card

**Role:** Internal content grouping

Card with 'Pitch Black' background (#08090a) and 12px border-radius, no shadow. Padding 8px on all sides, used for containing sub-elements within larger cards.

### Input Field

**Role:** User input fields

Input field with transparent background, 'Porcelain' text (#f7f8f8), 'Charcoal Grey' border (#23252a), and 6px border-radius. Padding is 12px vertical and 14px horizontal.

### Subtle Input Field

**Role:** Search or secondary input fields

Input field with 'Gunmetal' background (#383b3f), 'Porcelain' text (#f7f8f8), no explicit border, and 0px border-radius. Used for less emphasized data entry.

### Badge

**Role:** Label or tag

Badge with a 'Gunmetal' background (#383b3f), 'Storm Cloud' text (#8a8f98), 4px border-radius, and padding of 0px vertical and 6px horizontal. Used for small categorical labels.

## Layout

The page primarily uses a full-bleed structure for background content, with main content sections constrained by a centered maximum width (not explicitly defined but visually present). The hero section features a full-bleed 'Pitch Black' background with a centered, prominent headline. Subsequent sections alternate between dark backgrounds for narrative content and embedded UI examples, often featuring split layouts (text on one side, product UI on the other). Content is generally arranged in vertical stacks or multi-column grids for feature display. Navigation consists of a sticky top bar and frequently observed left-hand sidebar for application-like structures. Spacing is compact yet deliberate, creating a dense but organized information flow.

## Imagery

The site's visual language is dominated by UI elements and product screenshots, emphasizing functionality over decorative imagery. Where images appear, they are often contained within realistic product mockups or embedded application frames. Abstract graphics are minimal, primarily serving as subtle background textures or data visualizations. Icons are filled, minimalist, and mono-color, often adopting the 'Porcelain' (#f7f8f8) or 'Storm Cloud' (#8a8f98) neutral palette, enhancing the dashboard aesthetic. The overall density of imagery is low; it serves an explanatory or product showcase role rather than a decorative one.

## Dos & Donts

### Do

- Use 'Pitch Black' (#08090a) for the primary page background to establish the dark theme.
- Apply 'Porcelain' (#f7f8f8) for all primary text and important icons to ensure readability.
- Highlight primary interactive elements exclusively with 'Neon Lime' (#e4f222) as a background, restricting its use to guide user attention.
- Create depth and hierarchy by layering surfaces using 'Pitch Black' (#08090a), 'Graphite' (#0f1011), and 'Deep Slate' (#161718) backgrounds.
- Employ the Inter Variable font family with specific letter-spacing adjustments for all UI text, such as -0.22px for display sizes and -0.11px for body text, to maintain a tight, precise feel.
- Utilize 6px border-radius for all primary buttons, cards, and input fields to maintain a consistent, subtly rounded aesthetic.
- Use 'Storm Cloud' (#8a8f98) for secondary text and descriptive labels to recede into the background.

### Don't

- Do not introduce additional bright or saturated colors beyond 'Neon Lime' (#e4f222) for interactive elements; maintain its singular role.
- Avoid using harsh white backgrounds or light-themed patterns, as the system is anchored in a dark mode aesthetic.
- Do not deviate from the specified typeface choices; 'Inter Variable' and 'Berkeley Mono' are fundamental to the visual identity.
- Refrain from using strong, diffuse shadows; elevation is achieved through subtle layering and sharp, contained shadows like rgba(0, 0, 0, 0.4) 0px 2px 4px 0px.
- Do not apply broad, decorative background gradients across large sections of the UI; gradients are subtle and contained to specific functional areas.
- Do not use generic border-radii; adhere to 6px for key components like cards and buttons, and 2px for smaller tags, to preserve the signature balance of softness and precision.
- Avoid large amounts of white space; the design is compact, leveraging an 8px element gap as a standard measurement.

## Notes

### Agent Prompt Guide

Quick Color Reference:
- text: #f7f8f8 (Porcelain)
- background: #08090a (Pitch Black)
- border: #23252a (Charcoal Grey)
- accent: #5e6ad2 (Aether Blue)
- primary action: #e4f222 (filled action)

3-5 Example Component Prompts:
- Create a call-to-action button: 'Neon Lime' background (#e4f222), 'Pitch Black' text (#08090a), Inter Variable font weight 590 at 15px, 6px border-radius, 12px vertical and 24px horizontal padding.
- Create a default card with content: 'Graphite' background (#0f1011), 6px border-radius, rgba(0, 0, 0, 0.4) 0px 2px 4px 0px shadow. Inside, use Inter Variable font weight 400 at 14px with 'Porcelain' text (#f7f8f8), and a subsection headline at 17px weight 510 with 'Porcelain' text (#f7f8f8). Apply 8px padding internally.
- Create a sidebar navigation item: Ghost button with transparent background, 'Storm Cloud' text (#8a8f98), Inter Variable font weight 400 at 14px, 2px border-radius, no padding.
- Create an input field: transparent background with a 'Gunmetal' fill (#383b3f), 'Light Steel' text (#d0d6e0) using Inter Variable font weight 400 at 14px, 6px border-radius. Inset with a 1px 'Charcoal Grey' border (#23252a). Padding 12px vertical and 14px horizontal.

---
_Source: https://styles.refero.design/style/90ce5883-bb24-4466-93f7-801cd617b0d1_
