# Oura Ring — Design Reference

> Warm, diffused elegance. Like light filtering through linen onto brushed metal.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://ouraring.com](https://ouraring.com) |
| Refero page | [https://styles.refero.design/style/9decde51-2a8b-4212-bba5-be9457efc62e](https://styles.refero.design/style/9decde51-2a8b-4212-bba5-be9457efc62e) |
| Theme | light |
| Industry | other |

## Overview

This design system evokes a sense of understated luxury and sophisticated technology, blending warm, earthy neutrals with crisp, architectural typography. The use of a muted, almost desaturated background palette creates a premium canvas, upon which product imagery takes center stage, imbued with a soft, inviting glow. The precise, sans-serif AkkuratLL counters the fluid, almost calligraphic Editorial New, establishing a deliberate tension between precision and human touch. Subtle gradients and large product photography integrate seamlessly into the soft color scheme, rather than competing, suggesting an experience that is both advanced and deeply personal.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Linen Mist | `#f7f1e8` | neutral | Primary background for pages and cards, lending a soft, almost tactile warmth. |
| Graphite | `#4a4741` | neutral | Primary text color for body copy, buttons, and navigation items, offering strong contrast against light backgrounds while maintaining a subdued feel. |
| Ebony | `#000000` | neutral | High-contrast text for critical headings, buttons, and iconic elements, providing moments of stark clarity. |
| Cloud Gray | `#ececec` | neutral | Subtle borders, dividers, and ghost button outlines, creating soft separation without harsh lines. |
| Stone | `#a8a5a0` | neutral | Secondary text and subtle accents, softening the visual hierarchy. |
| Off-White | `#ffffff` | neutral | Text on dark buttons and occasional backgrounds, providing essential contrast. |
| Deep Space | `#1c1b1a` | neutral | Used sparingly for deeply immersive product sections or background elements, creating dramatic contrast with white text. |
| Twilight Indigo | `#5b6550` | accent | Subtle accent for icons and descriptive text, a muted chromatic whisper |
| Ocean Glimmer | `#1f72cd` | accent | Specific interactive highlights or progress indicators, a vivid yet localized blue. |
| Warm Bronze Gradient | `#af751b` | brand | Decorative background for hero sections or brand moments, transitioning from deep charcoal to amber glow for visual richness. |
| Sky Veil Gradient | `#b5e4fe` | brand | Subtle background effect, adding a diffused, cool luminescence. |
| Sun Kissed Gradient | `#ffb648` | brand | Subtle background effect, adding a diffused, warm glow. |

## Typography

### AkkuratLL

| Key | Value |
| --- | --- |
| weight | 200, 300, 400, 500, 700 |
| sizes | 12px, 14px, 16px, 18px, 24px, 40px, 48px, 80px, 96px |
| lineHeight | 1.00, 1.25, 1.33, 1.38, 1.50 |
| letterSpacing | -0.05, -0.05, -0.025, 0.025, 0.025, -0.05, -0.05, -0.05, -0.05 |
| fontFeatureSettings | 'dlig' 0, 'kern', 'liga' 0, 'onum' 0, 'ss01' 0, 'tnum' 0 |
| substitute | Inter |
| role | The functional workhorse for all body text, navigation, buttons, and most headings — its precise, geometric sans-serif nature conveys technical sophistication. The condensed letter spacing at larger sizes tightens headlines, preventing airiness and maintaining a sense of focus. |

### Editorial New

| Key | Value |
| --- | --- |
| weight | 100, 300, 500 |
| sizes | 30px, 40px, 80px, 96px |
| lineHeight | 1.00, 1.25, 1.50 |
| letterSpacing | -0.025 |
| fontFeatureSettings | 'dlig' 0, 'kern', 'liga' 0, 'onum' 0, 'ss01' 0, 'tnum' 0 |
| substitute | Playfair Display |
| role | Used for hero messaging and large display headlines — its elegant, almost calligraphic serif letterforms introduce a humanistic, refined counterpoint to the AkkuratLL. The lighter weights provide a whisper print quality. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.6 |
| body-sm | 14 |  | 1.5 | -0.7 |
| body | 16 |  | 1.5 | -0.4 |
| subheading | 18 |  | 1.38 | 0.45 |
| heading | 24 |  | 1.33 | 0.6 |
| heading-lg | 40 |  | 1.25 | -2 |
| display | 80 |  | 1 | -4 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 8px |
| input | 12px |
| buttons | 16777215px |
| general | 4px |

- **elementGap** — 8px
- **sectionGap** — 40px
- **cardPadding** — 24px
- **pageMaxWidth** — 

## Components

### Product Display Cards

### Tab Bar — Activity Selector

### Button Group + Announcement Banner

### Primary Filled Button

**Role:** Call to action.

backgroundColor: #4a4741, color: #f7f1e8, borderRadius: 16777215px, paddingTop: 12px, paddingRight: 24px, paddingBottom: 12px, paddingLeft: 24px. Text uses AkkuratLL, weight 400.

### Secondary Outlined Button

**Role:** Secondary actions or navigation.

backgroundColor: #f7f1e8, color: #4a4741, border: 1px solid #4a4741, borderRadius: 16777215px, paddingTop: 20px, paddingRight: 20px, paddingBottom: 20px, paddingLeft: 20px. Text uses AkkuratLL, weight 400.

### Tertiary Ghost Button

**Role:** Minimalist interactive elements, often in navigation.

backgroundColor: rgba(0, 0, 0, 0), color: #000000, border: none, borderRadius: 0px, paddingTop: 20px, paddingRight: 48px, paddingBottom: 28px, paddingLeft: 48px. Text uses AkkuratLL, weight 400.

### Tertiary Ghost Pill Button

**Role:** Secondary navigation or subtle calls to action.

backgroundColor: rgba(0, 0, 0, 0), color: #000000, border: none, borderRadius: 16777215px, paddingTop: 12px, paddingRight: 24px, paddingBottom: 12px, paddingLeft: 24px. Text uses AkkuratLL, weight 400.

### Product Display Card

**Role:** Showcasing individual products.

backgroundColor: #f7f1e8, borderRadius: 8px, boxShadow: none, paddingTop: 24px, paddingRight: 24px, paddingBottom: 24px, paddingLeft: 24px. Contains images and text, often with buttons.

### Informational Card

**Role:** Grouping related content blocks.

backgroundColor: rgba(0, 0, 0, 0), borderRadius: 0px, boxShadow: none, paddingTop: 32px, paddingRight: 32px, paddingBottom: 32px, paddingLeft: 32px. Used for content sections that appear as logical blocks on the page.

### Dark Input Field

**Role:** Form input elements.

backgroundColor: #4a4741, color: #ffffff, border: 1px solid #ececec, borderRadius: 12px, paddingTop: 8px, paddingRight: 48px, paddingBottom: 8px, paddingLeft: 20px. Placeholder text uses AkkuratLL, weight 400, color #ffffff.

## Layout

The page primarily uses a full-bleed layout for hero sections, which often feature large-scale product imagery with centered headlines overlapping the visual. Subsequent sections tend to adopt a contained, centered content block approach, using generous vertical spacing between sections to create a comfortable, airy rhythm (sectionGap is 40px). Content is frequently arranged in multi-column layouts, such as two-column text-left/image-right or a grid for feature cards. The main body content respects a comfortable max-width, preventing lines from becoming too long. Navigation is a consistent sticky top bar with a clean, centered logo and right-aligned menu items. The overall density is comfortable, with ample breathing room, suggesting a premium and thoughtful experience.

## Imagery

The visual language focuses on high-quality, aspirational product photography, often incorporating hands or close-up shots of the Oura Ring itself. Images are typically full-bleed or presented within contained, soft-edged cards (8px radius) that blend into the warm neutral backgrounds. There's an intentional lack of busy lifestyle photography; instead, product shots are often artfully composed with diffused lighting, emphasizing the metallic sheen or ceramic texture. When illustrative elements appear, they are minimal, often line-based icons, maintaining a sleek, understated aesthetic. Photography is generally high-key with soft, diffused light, avoiding harsh shadows or vibrant saturation, making the product the focal point without overwhelming the senses. The overall density is balanced, allowing product visuals significant space.

## Dos & Donts

### Do

- Prioritize AkkuratLL for all body text, UI elements, and most headings to establish a technical, precise tone.
- Utilize Editorial New for prominent display text and hero headlines, leveraging its elegant forms to add a touch of humanistic sophistication.
- Implement the Linen Mist (#f7f1e8) as the predominant page and card background, fostering a soft, warm visual foundation.
- Use pill-shaped radii (16777215px) for all primary and secondary buttons, conveying approachability and a natural feel.
- Maintain a clear visual hierarchy with Graphite (#4a4741) for primary text and Stone (#a8a5a0) for secondary details and subheadings.
- Integrate gradient backgrounds for hero sections or brand moments, ensuring they complement the muted palette rather than dominating it, like Warm Bronze Gradient.

### Don't

- Avoid using harsh, saturated primary colors for UI elements; chromatic colors are reserved for subtle accents or complex brand graphics.
- Do not introduce sharp, angular edges where rounded forms are established for buttons and inputs; maintain the consistent use of 8px and 12px radii for cards and input fields.
- Refrain from heavy drop shadows or strong elevation effects; rely on subtle background color shifts and content arrangement for visual depth.
- Do not use highly decorative or illustrative imagery for product showcases; prefer clean, close-up photography of the product in muted contexts.
- Do not over-emphasize text with bold or heavy weights from AkkuratLL for body copy; stick to 300-400 for readability against the soft backgrounds.
- Avoid generic stock photography; all imagery should be bespoke, high-quality, and resonate with the product's understated elegance.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: #4a4741 (Graphite)
- Background: #f7f1e8 (Linen Mist)
- CTA Primary Background: #4a4741 (Graphite)
- CTA Primary Text: #f7f1e8 (Linen Mist)
- Border: #ececec (Cloud Gray)
- Accent: #5b6550 (Twilight Indigo)

### Example Component Prompts
1. Create a hero section: Full-bleed background with Warm Bronze Gradient linear-gradient(to right bottom in oklab, rgb(30, 36, 39) 0px, rgb(175, 117, 27) 100%). Centered headline 'Sleeker. Smarter. Made for you.' using Editorial New, weight 300, 80px size, 1.0 lineHeight, color #ffffff, letter-spacing -4px. Below it, add a Primary Filled Button 'Discover Oura Ring 4'.
2. Create a product card: Background Linen Mist #f7f1e8, borderRadius 8px, padding 24px. Include a product image and a headline 'Oura Ring 4 Ceramic' (AkkuratLL, weight 500, 24px size, color #4a4741). Add two buttons side-by-side: a Secondary Outlined Button 'Explore' and a Primary Filled Button 'Shop'.
3. Create an informational section: Background Linen Mist #f7f1e8. Headline 'Oura Membership gives your body a voice' (Editorial New, weight 300, 48px size, 1.25 lineHeight, letter-spacing -2px, color #000000). Below the headline, add a Primary Filled Button 'Why Oura' and ensure 40px padding below it.

---
_Source: https://styles.refero.design/style/9decde51-2a8b-4212-bba5-be9457efc62e_
