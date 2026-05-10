# Paragraph — Design Reference

> Whisper-soft sepia canvas with quiet elevation.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://mirror.xyz](https://mirror.xyz) |
| Refero page | [https://styles.refero.design/style/37dd9612-4df0-4cdd-b942-bd97dd0efbd2](https://styles.refero.design/style/37dd9612-4df0-4cdd-b942-bd97dd0efbd2) |
| Theme | light |
| Industry | media |

## Overview

Paragraph employs a gentle, almost monastic aesthetic, built on a warm, off-white canvas and a subtle palette of muted grays. Typography is key; classic serifs for headlines against modern sans-serifs for body text create an intellectual yet inviting tone. Components are crafted with generous corner radii and speak with soft, layered shadows rather than bold color or heavy borders.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas | `#dbd5d2` | neutral | Page background, subtle borders, UI backgrounds |
| Inkwell | `#271f1b` | neutral | Primary text color for headings and body content, prominent icons |
| Snow | `#ffffff` | neutral | Card backgrounds, elevated surfaces, button text on blue accents |
| Silver Mist | `#ededed` | neutral | Secondary card backgrounds, badge backgrounds, muted UI dividers |
| Stone Gray | `#888786` | neutral | Muted secondary text, helper text, subtle inactive states |
| Cloud Wash | `#f1f5fe` | neutral | Softest background nuance, very subtle shadows |
| Distant Sky | `#c9c7c6` | neutral | Subtle background for navigation elements |
| Shadow Blue | `#524c49` | neutral | Darker subtle background |
| Action Azure | `#4a83f5` | brand | Violet wash for highlight backgrounds, decorative bands, and soft emphasis behind content. Do not promote it to the primary CTA color |
| Horizon Glow | `#b1cafb` | accent | Violet supporting accent for decorative details and low-frequency emphasis. Do not promote it to the primary CTA color |
| Pale Blue | `#cedaf3` | accent | Supporting neutral for secondary UI, dividers, and muted labels. Do not promote it to the primary CTA color |

## Typography

### Google Sans Flex

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| weights | 400, 500, 600 |
| sizes | 14px, 16px, 18px |
| lineHeight | 1.25, 1.43, 1.50, 1.56, 1.63 |
| letterSpacing | normal |
| substitute | system-ui |
| role | Primary sans-serif for body text, buttons, navigation, and interface elements. Its multiple weights support a clear information hierarchy without needing extra fonts. |

### IvyOra

| Key | Value |
| --- | --- |
| weight | 400 |
| weights | 400 |
| sizes | 18px, 24px, 64px |
| lineHeight | 1.00, 1.33, 1.56 |
| letterSpacing | -0.0300em at 64px, -0.0220em at 24px, -0.0200em at 18px |
| substitute | serif |
| role | Headline font, providing a classic, editorial feel with distinct tracking for different sizes. Its generous kerning at large sizes creates an opulent feel. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.56 | 0 |
| body | 16 |  | 1.5 | 0 |
| subheading | 18 |  | 1.56 | -0.36 |
| heading | 24 |  | 1.33 | -0.53 |
| display | 64 |  | 1 | -1.92 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 28px |
| badges | 24px |
| images | 16px |
| buttons | 24px |

- **elementGap** — 8px
- **sectionGap** — 48px
- **cardPadding** — 16px
- **pageMaxWidth** — 1400px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Canvas | `#dbd5d2` | 1 | Base page background |
| Silver Mist | `#ededed` | 2 | Secondary card backgrounds, badge backgrounds |
| Snow | `#ffffff` | 3 | Primary card and elevated element backgrounds |

## Components

### Ghost Navigation Item

**Role:** Neutral, interactive navigation menu item.

backgroundColor: rgba(0, 0, 0, 0), color: Inkwell, border-bottom: 1px solid Canvas, padding: 4px. Radius is 0px.

### Pill Button (Outlined)

**Role:** Secondary action button with rounded corners.

backgroundColor: rgba(0, 0, 0, 0), color: Snow, border-color: Canvas, border-radius: 24px, padding: 8px 16px.

### Feature Card (Primary)

**Role:** Content card for displaying articles or features.

backgroundColor: Snow, border-radius: 28px, padding: 8px 8px 24px 8px. No explicit shadow, relies on surrounding contrast.

### Callout Card (Secondary)

**Role:** Highlight card with muted background.

backgroundColor: Silver Mist, border-radius: 28px, padding: 20px. No explicit shadow.

### Article Card (Minimal)

**Role:** Minimal container for article listings, using transparent background.

backgroundColor: rgba(0, 0, 0, 0), border-radius: 0px, padding: 0px.

### Pill Badge

**Role:** Informational tag or status indicator.

backgroundColor: rgba(237, 237, 237, 0.8), color: Inkwell, border-radius: 24px, padding: 0px 8px.

### Action Button (Filled)

**Role:** Main call-to-action button, highlighted.

backgroundColor: Action Azure, color: Snow, border-radius: 24px, padding: 8px 16px.

## Layout

The page structure is primarily max-width contained at 1400px, creating a refined, column-based presentation. The hero section often features a centered headline over a background that flows slightly outside the main content area, providing subtle visual depth. Content sections typically flow with consistent vertical spacing (48px separation) and present information in multi-column grids, such as 4-column cards for articles or alternating text-left/image-right blocks. Navigation features a sticky top bar with minimal links and a central logo. The rhythm is intentional and calm, with a focus on clear content presentation.

## Imagery

Imagery on Paragraph is characterized by a mix of full-bleed, stylized photography within article previews and compact, soft-edged product screenshots or illustrations. Photography tends to be editorial, often with subtle desaturation, while product shots are clean, contained within rounded frames, and integrated seamlessly into the UI. Icons are typically single-color, filled, and minimalistic, supporting UI functions rather than acting as decorative elements. Overall, visuals serve to introduce or explain content, with a moderate density, allowing text to remain dominant.

## Dos & Donts

### Do

- Use IvyOra for all headings (h1-h6) at weight 400, adjusting letter-spacing according to type scale.
- Apply Google Sans Flex for all body text, UI labels, and buttons, maintaining normal letter spacing.
- Elevate primary surfaces with Snow (#ffffff), secondary with Silver Mist (#ededed), and the base canvas with Canvas (#dbd5d2).
- Utilize 28px border-radius for cards and larger containers, and 24px for buttons and badges to ensure consistent softness.
- Employ Inkwell (#271f1b) for primary text and Stone Gray (#888786) for all secondary or helper text.
- Emphasize interactive elements through Action Azure (#4a83f5) for backgrounds, paired with Snow for text.
- Maintain a compact element spacing of 8px, escalating to 48px for vertical section gaps within the 1400px page max-width.

### Don't

- Avoid harsh, saturated colors; stick to the muted, earthy palette with Action Azure as the sole vivid accent.
- Do not introduce sharp corners; all interactive and card-like elements should respect the 24px or 28px radii.
- Refrain from heavy, opaque shadows; utilize the provided soft, layered shadows for subtle depth and elevation.
- Do not use generic system fonts; always specify Google Sans Flex for sans-serif and IvyOra for serif elements.
- Avoid dense, unbroken blocks of text; break up content using varied type sizes, deliberate line heights, and ample surrounding space.
- Do not use full-width, uncontained layouts; maintain content within the 1400px pageMaxWidth.
- Avoid decorative gradients or intricate background patterns; surfaces should remain clean and uniform.

## Notes

### Agent Prompt Guide

Quick Color Reference:
text: Inkwell (#271f1b)
background: Canvas (#dbd5d2)
border: Canvas (#dbd5d2)
accent: Action Azure (#4a83f5)
primary action: no distinct CTA color

Example Component Prompts:
1. Create a primary hero section with a centered headline: 'Where Ideas Thrive' in IvyOra 64px, Inkwell, letter-spacing -1.92px. Below this, a body text 'Reach more readers and give them meaningful ways to support you.' in Google Sans Flex 16px, Inkwell. Include a 'Sign in' Action Button (Filled) and 'Start reading' Ghost Navigation Item nearby. The section should be on Canvas background.
2. Design a 'Feature Card (Primary)' titled 'The Visibility Problem' with a subtle image thumbnail inset at the top. The card should have a Snow (#ffffff) background, 28px radius, 8px padding overall. The title should be IvyOra 24px, Inkwell, letter-spacing -0.53px. A Pill Badge '16h ago' should be in the top right of the thumbnail.
3. Implement a navigation bar for the top of a page. Include 'Our Story', 'Explore', 'Agents', 'Docs' as 'Ghost Navigation Item's and a 'Start writing' 'Action Button (Filled)'. The navigation bar should have a Canvas (#dbd5d2) background with a 'Subtle UI Shadow'.

---
_Source: https://styles.refero.design/style/37dd9612-4df0-4cdd-b942-bd97dd0efbd2_
