# Rainbow — Design Reference

> Vibrant dreamscape on cloudnine. The dominant palette of soft, often radial, gradients creates an otherworldly, playful backdrop for bold UI elements.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://rainbow.me](https://rainbow.me) |
| Refero page | [https://styles.refero.design/style/1680693c-aed8-47e8-917a-04eb89497b09](https://styles.refero.design/style/1680693c-aed8-47e8-917a-04eb89497b09) |
| Theme | light |
| Industry | crypto |

## Overview

This design system is a high-energy, playful expression of crypto. The visual theme is a vibrant, dream-like landscape of soft gradients and stylized cartoon clouds, punctuated by bold, rounded typography. It leverages a maximalist approach to color, with a spectrum of vivid hues appearing dynamically across elements. Transparency and soft white-tinted shadows give components a floating, ethereal quality, contrasting with the solid, impactful call-to-action buttons.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink | `#0f101a` | neutral | Primary text, prominent headings, key UI components. This near-black provides strong contrast while avoiding harshness, anchoring the playful colors. |
| Cloudburst | `#ffffff` | neutral | Page backgrounds, card surfaces, internal borders. It's not a stark white, but a soft, slightly luminous canvas for the colorful elements. |
| Whisper Gray | `#777885` | neutral | Secondary text, subtle borders, inactive states. Provides a delicate contrast against Cloudburst, maintaining an airy feel. |
| Pale Mist | `#f1f3f6` | neutral | Subtle background sections, light fills. Acts as an optical white without the intensity, contributing to the overall softness. |
| Sunset Orange | `#ff8a00` | brand | Primary call-to-action buttons, key interactive elements — a warm, inviting color that stands out against lighter backgrounds. |
| Neon Pink | `#ff54bb` | brand | Secondary call-to-action buttons, accent states — pairs with Sunset Orange to offer alternative but equally energetic interaction points. |
| Sky Blue | `#33aaff` | accent | Decorative elements, occasional highlights, or interactive states where a cool tone is desired. Appears sparingly, but with high saturation. |
| Cyan Tint | `#99eeff` | accent | Background gradients, subtle decorative areas. Provides a cool, ethereal visual layer. |
| Radiant Violet | `#8c64ff` | accent | Decorative splashes or unique element backgrounds. Enhances the playful, gradient-heavy aesthetic. |
| Teal Glow | `#00fff0` | accent | Rare, vibrant accent for impact. Found in gradients or small decorative elements to add a touch of electric energy. |
| Joyful Red | `#ff0f0f` | accent | Small, impactful accent that signals attention or warning. Used sparingly for maximum effect within a multi-color palette. |
| Pink Sunset Gradient | `#ff8564` | accent | Decorative background for key UI elements or sections. The transition from orange to pink adds dynamic flow. |
| Rainbow Burst Gradient | `#75e6ff` | brand | Large, immersive background for hero sections or brand moments. The multi-stop linear gradient creates a spectrum effect. |
| Ethereal Aqua Radial | `#94ffe8` | neutral | Subtle background element for cards or containers, adding depth and a soft glow without harsh lines. |

## Typography

### SF Pro Rounded

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 16px, 20px, 24px, 32px, 36px, 42px, 56px, 80px, 96px, 100px |
| lineHeight | 1.00, 1.10, 1.20 |
| letterSpacing | -0.0310em, -0.0280em, -0.0180em, -0.0100em, -0.0300em, -0.0200em |
| substitute | system-ui, 'Avenir Next Rounded Std', 'Inter Display' |
| role | The primary typeface for headings and prominent body text. Its rounded edges align with the playful, soft aesthetic. The liberal use of bold, heavy, and black weights, combined with tight letter-spacing for display sizes, creates a friendly yet impactful presence. |

### sans-serif

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px |
| lineHeight | 1.20 |
| substitute | system-ui |
| role | Smallest body text, links, and minor UI elements. Provides a clean, highly legible foundation for detailed information, contrasting with the rounded display type. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.2 |  |
| body | 16 |  | 1.2 | -0.01 |
| body-lg | 20 |  | 1.2 | -0.031 |
| subheading | 24 |  | 1.1 | -0.031 |
| heading | 32 |  | 1 | -0.031 |
| heading-lg | 36 |  | 1.1 | -0.028 |
| display | 56 |  | 1.1 | -0.018 |
| display-xl | 80 |  | 1 | -0.03 |
| display-xxl | 96 |  | 1 | -0.02 |
| display-xxxl | 100 |  | 1 | -0.02 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| pill | 50px |
| cards | 32px |
| image | 32px-100px |
| buttons | 40px |

- **elementGap** — 8-24px
- **sectionGap** — 48-60px
- **cardPadding** — 20-32px
- **pageMaxWidth** — 

## Components

### Download CTA Button Group

### Wallet Token List Card

### Network Changed Notification Banner

### Primary Call-to-Action Button (Orange)

**Role:** Main user action for downloads and critical steps.

Rounded button with Sunset Orange (#ff8a00) background, default text color of browser's blue link color (use #0f101a or a vivid contrast for production, as #0000ee is browser default), 40px border-radius, and generous padding of 12px top/bottom, 32px left and 12px right. Features an embedded icon on the left by default.

### Secondary Call-to-Action Button (Pink)

**Role:** Alternative user action for downloads or prominent selections.

Rounded button with Neon Pink (#ff54bb) background, default text color of browser's blue link color (use #0f101a or a vivid contrast for production), 40px border-radius, and generous padding of 12px top/bottom, 32px left and 12px right. Features an embedded icon on the left by default.

### Ghost Navigation Link

**Role:** Top navigation items and auxiliary links.

Transparent background, Whisper Gray (#777885) text color, no border, with a 50px border-radius making them soft pill shapes on hover/active. Padding is 24px all around for a spacious, clickable area.

### Elevated Wallet Card

**Role:** Displays primary wallet information and active states.

Cloudburst (#ffffff) background, with transparency. Features a complex inset shadow (rgba(255, 255, 255, 0.32) 0px 5px 32px 12px inset, rgba(255, 255, 255, 0.44) 0px 1px 4px 0px inset) which makes it appear luminous and floating. Border-radius is 32px. Text color is Ink (#0f101a) for headings and Whisper Gray (#777885) for details. Padding is varied but generous, often around 20-32px.

### Small Pill Status Tag

**Role:** Used for 'Network changed' or other brief notifications.

Soft pink background (#ff9396), small rounded corners (implied from pill shape, around 16px), dense content with minimal padding to create a compact, informative tag.

### System Utility Text Link

**Role:** Informational or secondary navigation links, often with an arrow icon.

Ink (#0f101a) text color, default 12px font size, regular sans-serif weight 400. Letter-spacing is normal. Used for 'See more FAQs →'.

## Layout

The page primarily uses a full-bleed layout, allowing large background gradients and abstract graphics to fill the screen rather than being confined to a max-width container. The hero section is full-bleed with animated 3D elements and a centered headline. Sections maintain consistent vertical spacing between 48px and 60px. Content is arranged flexibly, often with centered stacks for headlines and subtext, or with asymmetric compositions where product mockups float next to text blocks. Navigation is a simple top-bar, sticky header at page top with minimal links and a primary 'Download' button. Element density is comfortable and spacious, allowing the large, vibrant graphics ample room to breathe.

## Imagery

The visual language is dominantly 3D abstract graphics and stylized product mockups. Photography is absent. Abstract forms, often with rainbow gradients (like the cloud and planet visuals), create a playful, almost cartoonish, yet high-fidelity atmosphere. Product mockups (e.g., phone displaying crypto wallet, browser extension windows) are rendered with soft, diffused lighting and slightly transparent, gradient-filled backgrounds that blend into the overall aesthetic. Icons are simple, filled, and often incorporate brand colors. Imagery serves a decorative, atmospheric role rather than strict informational one, enhancing the brand's 'color' and 'fun' identity.

## Elevation philosophy

Elevation is achieved not through traditional drop shadows but via a unique white inset shadow that creates a luminous, floating effect for cards and layered elements. This maintains the airy, dream-like quality of the UI, giving components a subtle sense of being suspended and illuminated from within.

## Dos & Donts

### Do

- Prioritize SF Pro Rounded for all headings and prominent text, using its Black, Heavy, or Semibold weights for visual impact.
- Apply 40px or 50px border-radius to all buttons and key interactive elements to maintain a uniformly friendly, approachable aesthetic.
- Use Sunset Orange (#ff8a00) and Neon Pink (#ff54bb) as primary and secondary calls to action, ensuring high visual contrast against light backgrounds.
- Implement the complex inset white shadow on elevated cards and interactive elements for a luminous, floating effect.
- Employ soft radial gradients like Ethereal Aqua Radial (#94ffe8) for background textures to establish the dreamlike visual atmosphere.
- Maintain a clear hierarchy with Ink (#0f101a) for main content and Whisper Gray (#777885) for secondary information.
- Utilize -0.03em or -0.02em letter-spacing for headlines 56px and above to create a dramatic, compressed visual effect.

### Don't

- Avoid sharp corners; the minimum radius for most UI elements should be 10px, with larger elements featuring 32px or 40px.
- Do not use solid background colors for large sections; instead, apply subtle gradients or Pale Mist (#f1f3f6) to maintain softness.
- Refrain from using stark blacks or harsh whites; Ink (#0f101a) and Cloudburst (#ffffff) are the darkest and lightest neutrals.
- Do not introduce strong, desaturated grays outside the Whisper Gray (#777885) family; maintain the soft, slightly chromatic neutral palette.
- Avoid heavy drop shadows below elements; the preferred elevation is via the ethereal white inset shadow or subtle background changes.
- Do not use generic sans-serif fonts for display or heading text; SF Pro Rounded is crucial for brand identity.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text (primary): #0f101a
- Background (page): #ffffff
- CTA (primary): #ff8a00
- Border (general): #777885
- Accent (secondary CTA): #ff54bb

### 3-5 Example Component Prompts
1. Create a hero section: Full-bleed background with 'Rainbow Burst Gradient'. Centered headline 'Experience Crypto in Color' using SF Pro Rounded Black, 96px, #0f101a, letter-spacing -0.02em. Subtext 'Fun, powerful, and secure wallets for everyday use' SF Pro Rounded Semibold, 32px, #0f101a, letter-spacing -0.031em. Two CTA buttons below: 'Download Rainbow Extension' (Sunset Orange background, 12px 32px padding, 40px border-radius, white text, left icon) and 'Download Rainbow Mobile' (Neon Pink background, same padding/radius/text/icon).
2. Create an elevated wallet card: Cloudburst (#ffffff) background, 32px border-radius. Apply the inset shadow 'rgba(255, 255, 255, 0.32) 0px 5px 32px 12px inset, rgba(255, 255, 0.44) 0px 1px 4px 0px inset'. Inside, display a token list: each item with Ink (#0f101a) for coin name and amount (16px SF Pro Rounded Medium), and Whisper Gray (#777885) for details (12px sans-serif). Use 20px padding around the content.
3. Create a navigation header: Full-width sticky header. Logo on left. Links on right ('Get Support', 'Updates', 'Learn') using Ghost Navigation Link style, Ink (#0f101a) text, 24px padding, 50px border-radius. 'Download' button is a Ghost Navigation Link but with a Rainbow Burst Gradient background on hover/active.

---
_Source: https://styles.refero.design/style/1680693c-aed8-47e8-917a-04eb89497b09_
