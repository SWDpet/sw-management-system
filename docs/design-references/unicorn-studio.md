# Unicorn Studio — Design Reference

> Midnight Command Center

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://www.unicorn.studio](https://www.unicorn.studio) |
| Refero page | [https://styles.refero.design/style/6bd8007f-01bc-46cc-8599-b396a39c1474](https://styles.refero.design/style/6bd8007f-01bc-46cc-8599-b396a39c1474) |
| Theme | dark |
| Industry | design |

## Overview

Unicorn Studio uses a dark, luminous aesthetic, reminiscent of a nocturnal command center. The design features deep, rich backgrounds contrasted with muted gray typography and vivid violet accents. Typography is compact and precise, emphasizing clarity in a dense information environment. Components are lightweight with softened corners, and subtle elevation is preferred over stark shadows, creating depth without visual clutter.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Midnight Ink | `#08080a` | neutral | Primary page and card backgrounds, deep canvas |
| Twilight Slate | `#0d0d12` | neutral | Secondary background surfaces, subtle separation from primary canvas |
| Soft Black | `#17171c` | neutral | Tertiary background surfaces, for nested elements or hover states |
| Ash Gray | `#25252d` | neutral | Button backgrounds, subtle fill for interactive elements |
| Steel Gray | `#31313a` | neutral | Subtle button shadow, hairline borders, and dividers |
| Ghostly Gray | `#aeaac0` | neutral | Primary text, prominent links, major headings, borders |
| Muted Silver | `#dad7de` | neutral | High-contrast text, particularly for body copy and button text on dark backgrounds |
| Subtle Charcoal | `#8b8e9c` | neutral | Secondary text, muted links, subtle element borders |
| Faded Stone | `#62626f` | neutral | Tertiary text, helper text, and less prominent borders |
| Cosmic Violet | `#8e6ce4` | brand | Branding, key accents, focus rings, subtle background gradient touches; Background radial gradient, creating a focal point or immersive hero effect |
| Starlight Violet | `#ab8ff1` | accent | Interactive element borders and hover states, providing a lighter accent |
| Midnight Violet | `#8960f0` | accent | Interactive element borders for active/hover states, indicating interaction |

## Typography

### Overused Grotesk

| Key | Value |
| --- | --- |
| weight | 400, 500 |
| weights | 400, 500 |
| sizes | 26px, 32px, 48px, 64px, 215px |
| lineHeight | 0.99, 1.00 |
| letterSpacing | -0.0630em at 215px, -0.0400em at 64px, -0.0300em at 48px |
| substitute | Inter |
| role | Display headings and primary marketing copy. Its tight tracking creates a dense, impactful statement. |

### Sprat

| Key | Value |
| --- | --- |
| weight | 500 |
| weights | 500 |
| sizes | 46px, 61px |
| lineHeight | 1.00 |
| letterSpacing | -0.0420em at 61px |
| substitute | Anton |
| role | Secondary display headings, used sparingly for emphasis or unique section titles. Its condensed nature provides a distinct contrast. |

### -apple-system

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 13px, 14px, 16px, 18px |
| lineHeight | 1.20, 1.40 |
| letterSpacing | -0.0100em |
| substitute | Inter |
| role | All body text, links, buttons, and general UI elements. Prioritizes legibility and system consistency for functional text. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.2 | -0.13 |
| body | 16 |  | 1.4 | -0.16 |
| subheading | 18 |  | 1.2 | -0.18 |
| heading-sm | 26 |  | 0.99 | -0.78 |
| heading | 32 |  | 0.99 | -1.28 |
| heading-lg | 46 |  | 1 | -1.93 |
| display | 64 |  | 0.99 | -2.56 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| tags | 3px |
| cards | 10px |
| buttons | 3px |

- **elementGap** — 20px
- **sectionGap** — 96px
- **cardPadding** — 30px
- **pageMaxWidth** — 1440px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Midnight Ink | `#08080a` | 0 | Base page background and primary card surface. |
| Twilight Slate | `#0d0d12` | 1 | Slightly elevated background for secondary content blocks or modals. |
| Soft Black | `#17171c` | 2 | Further elevated background, often used for nested UI components or hover states. |
| Ash Gray | `#25252d` | 3 | Interactive element backgrounds like buttons, providing a distinct clickable area. |

## Components

### Filled Action Button

**Role:** Primary Call to Action

Uses `Ash Gray` background (#25252d) with `Muted Silver` text (#dad7de). Has a `3px` border-radius and `6px` vertical, `12px` horizontal padding. A subtle `Steel Gray` (#31313a) inset shadow provides minimal depth.

### Outlined Button (Primary)

**Role:** Secondary action or link with brand emphasis

Text in system font, `Muted Silver` (#dad7de) on hover becomes `Starlight Violet` (#ab8ff1). Displays a `Starlight Violet` (#ab8ff1) border that shifts to `Midnight Violet` (#8960f0) on hover. `3px` border-radius, `6px` vertical, `12px` horizontal padding.

### Navigation Link

**Role:** Top navigation and inline links

Uses system font, `Ghostly Gray` text (#aeaac0) and `3px` border-radius. On hover, the text color subtly brightens and a `Starlight Violet` (#ab8ff1) border appears, transitioning to `Midnight Violet` (#8960f0) on active/focus.

### Content Card

**Role:** Container for features, testimonials, or product modules

Background is `Midnight Ink` (#08080a) with a `10px` border-radius and `30px` padding on all sides. No visible box shadow, relies on background contrast for separation.

### Info Tag

**Role:** Small, informative labels, often used for status or categories

Background can be inherited or `Ash Gray` (#25252d), with `Ghostly Gray` text (#aeaac0). Features `3px` border-radius and minimal padding (e.g., `5px` vertical, `10px` horizontal).

### Highlighted Text Link

**Role:** Inline text links within body copy that require visual emphasis

Text color defaults to `Muted Silver` (#dad7de) and gains a subtle `Starlight Violet` (#ab8ff1) underline on hover.

### Minimal Card

**Role:** Smaller content containers, often in grids

Background is `Twilight Slate` (#0d0d12) or `Soft Black` (#17171c), with `10px` border-radius. Padding of `15px` to `20px` to maintain a lighter feel.

## Layout

The page primarily uses a max-width contained layout of `1440px`, centered on the screen, though the hero section extends full-bleed with its background imagery. The hero features a centered headline over the background gradient. Section rhythm is driven by consistent vertical spacing of `96px` with no alternating light/dark bands, consistent with the dark theme. Content within sections tends towards centered stacks or `2-column` arrangements, particularly for text-heavy feature blocks. There's occasional use of a 3-column grid for pricing or feature listings. The overall density is comfortable, with ample breathing room between content blocks. Navigation is a sticky top bar, minimally styled with outlined ghost links and a single primary filled button.

## Imagery

This design system uses atmospheric, abstract imagery, primarily featuring a 'Nebula Glow' radial gradient that serves as a background for large sections or hero areas. The visuals are typically full-bleed, blending seamlessly into the dark background, or contained within cards with rounded corners. There is a strong absence of photography, relying instead on 3D rendered, luminous forms that evoke magic and technology. Icons are minimal, subtle, and outlined, matching the overall subdued and sophisticated tone. Imagery density is low, primarily used for large focal points rather than numerous small elements, allowing text to dominate information delivery.

## Dos & Donts

### Do

- Prioritize `Midnight Ink` (#08080a) for most backgrounds and `Ghostly Gray` (#aeaac0) or `Muted Silver` (#dad7de) for primary text to maintain the dark theme contrast.
- Use `Cosmic Violet` (#8e6ce4) and its lighter/darker variants (`Starlight Violet` #ab8ff1, `Midnight Violet` #8960f0) exclusively for calls to action, interactive states, and brand highlights.
- Apply a `3px` border-radius to all interactive elements like buttons and input fields for a consistent soft-edged feel.
- Headlines should leverage 'Overused Grotesk' with tight negative letter-spacing (`-0.0630em` for display sizes) to create a compact, commanding presence.
- Maintain comfortable 'elementGap' of `20px` and generous 'cardPadding' of `30px` to ensure spaciousness within components on the dark canvas.
- Use the system font `-apple-system` for all body text, links, and minor UI elements, ensuring maximum legibility and consistency across platforms.
- Employ the `Nebula Glow` radial gradient as a background for hero sections or prominent visual areas to inject dynamic brand color.

### Don't

- Avoid using bright or overly saturated colors outside the `Cosmic Violet` palette; maintain a subdued, near-achromatic color scheme for backgrounds and secondary elements.
- Do not use heavy, opaque box-shadows; prefer subtle elevation using `Steel Gray` (#31313a) for inset shadows, or rely on background color changes for hierarchy.
- Do not use generic, default system font styles for headlines; always apply 'Overused Grotesk' or 'Sprat' with their specific weights and letter-spacing for brand identity.
- Avoid excessive use of borders on non-interactive elements; rely on background color variations and padding to define areas.
- Do not break the `10px` border-radius for cards and `3px` for buttons/links; these radii are key to the system's character.
- Don't clutter the layout; maintain clear section gaps of `96px` and max-width constraints of `1440px` for a focused content experience.
- Never use `solid black (#000000)` or `solid white (#ffffff)` for primary text or backgrounds, as the system relies on subtle dark grays and light grays for its nuanced palette.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: #aeaac0
background: #08080a
border: #31313a
accent: #8e6ce4
primary action: #25252d (filled action)

Example Component Prompts:
Create a hero section: radial-gradient(circle, #8e6ce4, #724fc9) background. Headline at 64px Overused Grotesk weight 400, #aeaac0, letter-spacing -2.56px. Subtext at 16px -apple-system weight 400, #dad7de, letter-spacing -0.16px. Centered Filled Action Button: #25252d background, #dad7de text, 3px radius, 6px 12px padding.

Create a feature card: #08080a background, 10px radius, 30px padding. Headline at 32px Overused Grotesk weight 400, #aeaac0, letter-spacing -1.28px. Body text at 14px -apple-system weight 400, #8b8e9c, letter-spacing -0.14px.

Create an outlined button: #8b8e9c text, 1px #ab8ff1 border, 3px radius, 6px 12px padding, -apple-system font family, 14px size, 500 weight, normal letter spacing. On hover, text changes to #ab8ff1 and border changes to #8960f0.

Create a basic text input field: `Twilight Slate` (#0d0d12) background, `Ghostly Gray` (#aeaac0) text, `Steel Gray` (#31313a) 1px border, `3px` border-radius. `10px` padding. Focus state adds a `Cosmic Violet` (#8e6ce4) 2px border.

---
_Source: https://styles.refero.design/style/6bd8007f-01bc-46cc-8599-b396a39c1474_
