# The Verge — Design Reference

> Digital Newsprint, Laser-Etched Text. A dark, information-dense canvas with sharp typographic contrasts and electric accents.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://theverge.com](https://theverge.com) |
| Refero page | [https://styles.refero.design/style/e8c4206d-9a2a-4c08-9524-6f14a25e792f](https://styles.refero.design/style/e8c4206d-9a2a-4c08-9524-6f14a25e792f) |
| Theme | dark |
| Industry | media |

## Overview

This design system evokes a 'digital newsprint' feel, balancing raw information density with striking, almost industrial accentuation. A deep, near-black background provides a stark canvas for crisp, often condensed typography and the electrifying 'Voltage Teal' accent. The unusual mix of a narrow sans-serif, a classic display serif, and a monospaced font creates a unique typographic voice that is both modern and slightly nostalgic, preventing the dark theme from becoming generic.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink Black | `#000000` | neutral | Page backgrounds, major text color for light elements, default borders. The foundational dark canvas of the system. |
| Deep Graphite | `#131313` | neutral | Card backgrounds, secondary container fills, subtle dividers. Provides a slight visual lift from the deepest background. |
| Light Ash | `#313131` | neutral | Tertiary backgrounds, subtle shadows, image placeholders. Slightly lighter than Deep Graphite for further distinction without losing coherence. |
| Ghost White | `#ffffff` | neutral | Primary text on dark backgrounds, key icons, essential outlines. High contrast for maximum readability against the dark canvas. |
| Silver Mist | `#e9e9e9` | neutral | Secondary text on dark backgrounds, active states for subtle elements. A slightly softer white for less emphasis. |
| Faded Steel | `#949494` | neutral | Tertiary text, metadata, disabled states. Reduced prominence for supporting information. |
| Voltage Teal | `#3cffd0` | brand | Interactive elements like primary links, active navigation tabs, selected buttons. Its vividness provides an instant focal point against the dark neutrals. |
| Neon Violet | `#5200ff` | accent | Special content blocks, featured categories, or distinctive callouts. Highly saturated for high impact, used sparingly. |
| Blaze Orange | `#ff3d00` | accent | Call-to-action buttons in specific contexts, badges, or error states. Demands attention without being aggressive. |

## Typography

### Poly Sans

| Key | Value |
| --- | --- |
| weight | 300, 400, 500, 700 |
| weights | 300, 400, 500, 600, 700 |
| sizes | 10px, 11px, 12px, 13px, 15px, 16px, 18px, 19px, 20px, 24px, 34px |
| lineHeight | 1.00, 1.10, 1.20, 1.30, 1.40, 1.50, 1.60 |
| letterSpacing | 0.0200em, 0.0600em, 0.1000em, 0.1500em |
| substitute | Inter |
| role | Primary text across the site; a condensed sans-serif that balances information density with modern readability. Used for body copy, subheadings, and navigation items, its tighter letter spacing at larger sizes contributes to the 'laser-etched' feel. |

### Manuka

| Key | Value |
| --- | --- |
| weight | 900 |
| weights | 900 |
| sizes | 60px, 90px, 107px |
| lineHeight | 0.80 |
| letterSpacing | 0.0100em |
| substitute | Playfair Display |
| role | Display headlines and hero text. This ultra-bold, high-contrast serif typeface is specifically chosen for its dramatic visual impact, carving out a bold, editorial presence on the dark background. |

### Fk Roman Standard

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px, 20px, 24px |
| lineHeight | 1.20, 1.30 |
| letterSpacing | -0.0100em |
| substitute | Libre Baskerville |
| role | Editorial body copy and article titles. A traditional serif with slight negative letter spacing, this font provides a classic, authoritative voice for longer-form content, contrasting with the modern sans-serifs. |

### Poly Sans Mono

| Key | Value |
| --- | --- |
| weight | 400, 500, 600, 700 |
| weights | 400, 500, 600, 700 |
| sizes | 11px, 12px |
| lineHeight | 1.18, 1.20, 1.30, 2.00 |
| letterSpacing | 0.1000em, 0.1250em |
| substitute | Space Mono |
| role | Metadata, timestamps, and compact informational tags. The monospaced nature and wide letter spacing give these elements a technical, almost code-like feel, reinforcing the tech focus of the brand. |

### ui-sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 16px |
| lineHeight | 1.10, 1.20, 1.30, 1.50 |
| letterSpacing | normal |
| substitute | System Sans-serif |
| role | Fallback and utility text. Used for less prominent UI elements, ensuring basic readability where custom fonts are not critical. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 10 |  | 1.4 |  |
| body | 16 |  | 1.5 |  |
| subheading | 19 |  | 1.2 |  |
| heading | 24 |  | 1.2 | 0.48 |
| heading-lg | 34 |  | 1 | 0.68 |
| display-sm | 60 |  | 0.8 | 0.6 |
| display-lg | 107 |  | 0.8 | 1.07 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| buttons | 24px |
| default | 0px |
| card_image_corners | 3px |

- **elementGap** — 10px
- **sectionGap** — 40px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Top Stories List

### Today's Stream Tab Bar + Feed Item

### Podcasts Most Popular Block

### Navigation Link

**Role:** Primary navigation elements

Ghost White text on Ink Black background, using Poly Sans at various weights and sizes. Hover state might introduce an underline or a subtle background shift, but no explicit hover color detected.

### Primary Action Button

**Role:** Call To Action

Filled button with Voltage Teal background and Ink Black text. Has a 24px border-radius, giving it a pill shape, and 10px vertical, 24px horizontal padding. Uses Poly Sans font.

### Secondary Action Button

**Role:** Alternative Call To Action

Filled button with Deep Graphite background and Silver Mist text. Has a 24px border-radius, pill shape, with 10px vertical, 24px horizontal padding. Uses Poly Sans font.

### Hamburger Navigation Button (Invisible)

**Role:** Menu toggle

Transparent background, Ghost White icon/text, 0px border-radius and padding. Designed to be unobtrusive until activated, signaling it's a clickable area primarily by its content and typical placement.

### Article Card

**Role:** Content previews

Transparent background (inherits page background), 0px border-radius, no box-shadow. Relies on negative space and typography for structure. Padding of 0px top/bottom, 40px right, 0px left.

### The Verge Wordmark

**Role:** Site Identity

Ghost White text, rotated 90 degrees. Uses Manuka font 900 weight at 107px with a negative letter-spacing for a bold, distinctive side-pylon effect.

### Trending List Item

**Role:** Highlights popular content

Deep Graphite background for the container, with Neon Violet background for the individual list items, contrasting sharply. Ghost White text for titles, Faded Steel for metadata. Utilizes Poly Sans for text. Each item has generous vertical spacing.

### Hero Headline

**Role:** Main feature story title

Ghost White text utilizing the Manuka font at a large size (60px) and ultra-bold weight (900), setting a strong editorial tone with tight line-height (0.8) for power.

## Layout

The page primarily uses a max-width contained model for readability, with main content centered. The hero section often features a large image with a bold, Manuka font headline overlaid. Sections flow largely vertically with consistent spacing. Content arrangement often employs a two-column primary layout for feature stories (large image with adjacent text block) and a sidebar for 'Top Stories' or 'Most Popular' lists. There is a strong use of a grid for presenting article snippets and card-like elements. Navigation is a prominent top bar, typically using Ghost White text on an Ink Black background, with a distinctive vertical 'The Verge' wordmark pylon on the left side, slightly overlapping the content.

## Imagery

This site utilizes a mixed visual language heavily dependent on photography and product screenshots. Photography includes tight, often abstract crops of technology and scientific subjects, sometimes with a raw, documentary feel, or product shots on clean backgrounds. Images are typically contained within rectangular bounds, often with a subtle 3px border-radius or no radius. They serve both decorative atmosphere and explanatory content, integrated directly into article layouts. The density is high, with images being integral to breaking up dense text blocks, often full-bleed within their content containers.

## Dos & Donts

### Do

- Use Ink Black (#000000) for the primary page background.
- Apply Deep Graphite (#131313) for card and content block backgrounds to establish clear hierarchy.
- Style interactive elements and primary links with Voltage Teal (#3cffd0) for high visibility.
- Always use 'Manuka' font (or substitute) at 900 weight for display headlines to create a dramatic impact, setting lineHeight to 0.8.
- Implement 24px border-radius for all primary and secondary buttons for a pill-shaped aesthetic.
- Utilize Poly Sans Mono with wide letter spacing (0.1000em or 0.1250em) for all metadata and timestamps.
- Maintain a default border-radius of 0px for most elements to reinforce the sharp, industrial aesthetic.

### Don't

- Do not use subtle shadows or gradients for elevation; distinguish surfaces primarily through color changes from Ink Black (#000000) to Deep Graphite (#131313) and Light Ash (#313131).
- Avoid generic 'modern sans-serif' for headlines; the distinctive display typefaces like Manuka and Fk Roman Standard are key to the brand.
- Do not introduce additional accent colors, as Voltage Teal, Neon Violet, and Blaze Orange are carefully deployed and highly impactful.
- Never use `ui-sans-serif` system font for prominent text or headlines; it's reserved for utility and fallback only.
- Do not apply padding or borders to article cards; their structure is defined by typography and background colors.
- Do not use soft, organic shapes; stick to the hard edges and specific rounded radii (3px or 24px) for distinct elements.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Text:** #ffffff (Ghost White)
- **Background:** #000000 (Ink Black)
- **CTA:** #3cffd0 (Voltage Teal)
- **Card Background:** #131313 (Deep Graphite)
- **Secondary Text:** #e9e9e9 (Silver Mist)

### 3-5 Example Component Prompts
1. **Create a Hero Section:** Full-width Ink Black background. Overlay a 600px wide image of a high-tech interior. Below the image, place a headline 'Interior design at 25,000 mph' using Manuka font, 900 weight, size 60px, lineHeight 0.8, letterSpacing 0.6px, color Ghost White. Below, add a subtext 'How the Artemis II spacecraft was designed with (relative) comfort in mind.' using Fk Roman Standard, 400 weight, size 24px, lineHeight 1.2, letterSpacing -0.24px, color Ghost White.
2. **Generate a Primary Action Button:** Text 'Subscribe'. Background Voltage Teal (#3cffd0), text color Ink Black (#000000). Border radius 24px. Padding 10px vertical, 24px horizontal. Font Poly Sans, 500 weight, size 16px, letterSpacing 0.32px.
3. **Design a 'Top Stories' Card:** Background Deep Graphite (#131313) with 0px radius. Inside, arrange a list of articles. Each article title should use Poly Sans, 500 weight, size 18px, lineHeight 1.2, color Ghost White. Metadata (e.g., author, time) should use Poly Sans Mono, 400 weight, size 11px, lineHeight 1.18, letterSpacing 1.1px, color Faded Steel (#949494).
4. **Create a 'Most Popular' Block:** Container with Neon Violet (#5200ff) background, 0px radius. Inside, list popular articles. Article titles styled with Poly Sans, 700 weight, size 20px, lineHeight 1.2, color Ghost White. Metadata uses Poly Sans Mono, 400 weight, size 12px, lineHeight 1.2, letterSpacing 1.5px, color Faded Steel (#949494).
5. **Build a Secondary Action Button:** Text 'Following'. Background Deep Graphite (#131313), text color Silver Mist (#e9e9e9). Border radius 24px. Padding 10px vertical, 24px horizontal. Font Poly Sans, 500 weight, size 16px, letterSpacing 0.32px.

---
_Source: https://styles.refero.design/style/e8c4206d-9a2a-4c08-9524-6f14a25e792f_
