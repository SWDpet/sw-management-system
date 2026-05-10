# Craft Docs — Design Reference

> Digital Scrapbook Collage. The design feels like a thoughtful, tactile scrapbook where structured digital notes meet soft, organic textures.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://craft.do](https://craft.do) |
| Refero page | [https://styles.refero.design/style/9f228e72-997a-4410-9190-68359028e3d0](https://styles.refero.design/style/9f228e72-997a-4410-9190-68359028e3d0) |
| Theme | light |
| Industry | productivity |

## Overview

The design aesthetic is gentle, creative, and tactile, evoking the feeling of a personal journal or artist's scrapbook. This is achieved through a warm, off-white canvas (#fff3e7), a palette of soft pastels (#9bd8a9, #fde99b), and organic, torn-paper collage elements. The primary visual signature is the typographic pairing: a sophisticated, tightly-tracked serif (`UntitledSerifFont`) for large, literary headlines, contrasted with a clean, functional sans-serif (`UntitledSansFont`) for all UI and body text. Rounded corners (14px, 24px) are prevalent, but are punctuated by pill-shaped buttons, creating a soft and approachable interface. Deep, multi-layered shadows provide a subtle, non-intrusive sense of depth, making elements feel floated rather than stacked.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Canvas | `#fff3e7` | neutral | Main page background, creating a warm, paper-like base. |
| Ink | `#030302` | neutral | Primary text, headlines, and filled UI elements. |
| White | `#ffffff` | neutral | Card surfaces, contrast text on dark backgrounds. |
| Linen | `#f7f7f7` | neutral | Secondary light backgrounds, subtle dividers. |
| Cloud | `#efefef` | neutral | Subtle container backgrounds, hover states. |
| Ash | `#e1e1e1` | neutral | Borders and dividers. |
| Stone | `#bebbba` | neutral | Secondary body text, disabled states. |
| Graphite | `#41413f` | neutral | Sub-headings and secondary text with more emphasis. |
| Mint | `#9bd8a9` | brand | Decorative background shapes, UI tags — a primary brand pastel. |
| Marigold | `#fde99b` | brand | Decorative background shapes, UI highlights — a warm primary brand pastel. |
| Periwinkle | `#b8caf5` | brand | Decorative background shapes, UI highlights — a cool primary brand pastel. |
| Sky | `#9ed4ef` | brand | Hero background textures and atmospheric elements. |
| Papaya | `#ff4500` | accent | Accent frames on cards, callouts within illustrations. |
| Azure | `#0087ff` | accent | Interactive icons and links within product mockups. |

## Typography

### UntitledSerifFont

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 24px, 28px, 30px, 32px, 36px, 46px, 54px, 56px, 66px |
| lineHeight | 1.05-1.2 |
| letterSpacing | Tight negative tracking at large sizes (e.g., -2.64px at 66px), normal at smaller sizes. |
| fontFeatureSettings | "liga" |
| substitute | Lora, Merriweather |
| role | Used exclusively for large, impactful headlines (H1/H2). Its literary feel provides a signature contrast to the functional sans-serif UI, making statements feel personal and crafted. |

### UntitledSansFont

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| sizes | 12px, 14px, 16px, 18px, 20px, 24px |
| lineHeight | 1.4-1.5 |
| letterSpacing | Slightly negative tracking at subheading sizes (e.g., -0.72px at 24px), slightly positive at small sizes. |
| fontFeatureSettings | "liga" |
| substitute | Inter, Figtree |
| role | The workhorse font for all UI elements, including buttons, navigation, body copy, and captions. Provides clarity and readability. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | 0.12 |
| body-sm | 14 |  | 1.5 | 0.14 |
| body | 16 |  | 1.5 | -0.24 |
| subheading | 24 |  | 1.4 | -0.72 |
| heading-sm | 36 |  | 1.2 | -0.72 |
| heading | 46 |  | 1.1 | -1.38 |
| heading-lg | 56 |  | 1.05 | -2.24 |
| display | 66 |  | 1.1 | -2.64 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 16-24px |
| pills | 9999px |
| inputs | 14px |
| buttons | 14px |

- **elementGap** — 8-16px
- **sectionGap** — 96-120px
- **cardPadding** — 24px
- **pageMaxWidth** — 1280px

## Components

### CTA Button Group

### Feature Category Pills

### Testimonial Quote Card

### Primary Pill Button

**Role:** The main 'Try Craft Free' call-to-action in the sticky navigation header.

A pill-shaped button (9999px radius) with an Ink (#030302) background and White (#ffffff) text. Features a subtle lift shadow on hover.

### Navigation Link

**Role:** Standard links within the main header navigation.

Unstyled text link using UntitledSansFont at 16px. Text color is Ink (#030302). A subtle underline or background color change appears on hover.

### Testimonial Quote

**Role:** Displays user feedback and social proof.

A large quotation in UntitledSerifFont, often italicized, in Ink (#030302). Attributed to a user with a small circular avatar and name in UntitledSansFont.

### Collage Background Element

**Role:** Decorative, atmospheric elements that make up the scrapbook aesthetic.

Abstract shapes with torn-paper edges, often filled with a brand pastel like Mint (#9bd8a9) or a simple dot/grid pattern. Layered behind content with low-opacity shadows.

## Layout

The page begins with a full-bleed, textured hero section that immerses the user. Below this, the layout transitions to a centered, max-width container (approx. 1280px) on a warm Canvas background. Generous vertical spacing (~96-120px) separates content sections, creating a calm, unhurried rhythm. Content is typically arranged in centered stacks for major headlines or simple, balanced grids for features (e.g., a five-column grid of icons and labels). The design avoids complex, asymmetric layouts in favor of clear, centered compositions that feel stable and easy to parse.

## Imagery

The visual language is a distinctive collage of clean UI screenshots and tactile, analog textures. Imagery includes torn paper edges, subtle grid patterns, and flat pastel color fields that frame or sit behind product mockups. This juxtaposition of digital precision and physical craft is central to the brand. Photography is used sparingly for user testimonials, appearing within small, rounded frames. The overall effect is approachable and creative, suggesting that the digital tool is a space for tangible, real-world ideas.

## Dos & Donts

### Do

- Use `UntitledSerifFont` for all major headlines (H1, H2) to establish the brand's literary, crafted feel.
- Apply tight negative letter-spacing (e.g., -2.24px at 56px) to serif headlines for a professional, refined look.
- Layer torn-paper textures and soft pastel color blocks (`Mint`, `Marigold`) to create the signature collage aesthetic.
- Use the complex, multi-layered shadow style on all key floating elements like cards.
- Set main page content on the warm `Canvas` (#fff3e7) background, not pure white.
- Combine generous corner radii (14px, 24px) with pill-shaped buttons for a friendly, approachable UI.
- Maintain generous vertical whitespace (96px+) between content sections.

### Don't

- Don't use the serif font for UI controls, body copy, or any text smaller than 24px.
- Don't use pure black text; always use the softer `Ink` (#030302).
- Don't use hard, single-source drop shadows. Stick to the soft, deep, multi-layered shadow style.
- Don't use sharp 0px corners on buttons or cards.
- Don't use a pure white (#ffffff) page background for long-form content.
- Don't clutter the layout; prioritize spaciousness and clear, centered compositions.
- Don't use vibrant accent colors for large background surfaces; reserve them for small highlights or illustrative frames.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Page Background:** `Canvas` (#fff3e7)
- **Primary Text:** `Ink` (#030302)
- **Card Background:** `White` (#ffffff)
- **Pill CTA Background:** `Ink` (#030302)
- **Brand Pastels:** `Mint` (#9bd8a9), `Marigold` (#fde99b), `Periwinkle` (#b8caf5)

### Example Component Prompts
1.  **Hero Section:** Create a full-width hero with a textured background using pastel blues like #9ed4ef. Add a centered headline 'Your space for big ideas' using `UntitledSerifFont`, 66px, weight 400, color `Ink` (#030302), and letter-spacing -2.64px. Below it, add a ghost button with 'Try Craft Free' text, transparent background, 1px `Ash` (#e1e1e1) border, 14px radius, and padding 16px 32px.

2.  **Floating UI Card:** Design a product feature card with a `White` (#ffffff) background, 16px border-radius, and the following box-shadow: `rgba(0, 0, 0, 0.01) 0px 50px 40px 0px, rgba(0, 0, 0, 0.02) 0px 50px 40px 0px, rgba(0, 0, 0, 0.05) 0px 20px 40px 0px, rgba(0, 0, 0, 0.08) 0px 3px 10px 0px`. Place a mockup image inside.

3.  **Feature Grid Section:** On the `Canvas` (#fff3e7) background, create a section with the heading 'Craft is for your things' in `UntitledSerifFont` at 46px, color `Ink`. Below it, create a 5-column grid of features. Each item should be a vertical stack with a 24px line-art icon and a label like 'Docs' in `UntitledSansFont` at 16px, color `Ink`.

---
_Source: https://styles.refero.design/style/9f228e72-997a-4410-9190-68359028e3d0_
