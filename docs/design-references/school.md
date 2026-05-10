# School — Design Reference

> Digital scrapbook on a gray desktop.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://schoooool.com](https://schoooool.com) |
| Refero page | [https://styles.refero.design/style/a521abb9-d84b-4870-b5a8-363be7c3f94a](https://styles.refero.design/style/a521abb9-d84b-4870-b5a8-363be7c3f94a) |
| Theme | light |
| Industry | other |

## Overview

This design system evokes a digital scrapbook or a quirky desktop environment, blending structured content with playful, almost childlike elements. The visual language is defined by a gridded card layout, each card a slightly rounded container. Eclectic color accents, often within illustrations or as background highlights, punctuate a predominantly neutral gray canvas. IBM Plex Mono's technical precision paired with Helvetica's classic readability creates a subtle tension, mirroring the creative studio's 'conceptual yet practical' ethos.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Ink | `#303030` | neutral | Primary text, headings, icons, borders, input text. |
| Paper White | `#FFFFFF` | neutral | Selected text, card background within certain contexts, heading overlay. |
| Desktop Gray | `#F2F2F2` | neutral | Page background; the primary canvas for all content. |
| Card Surface | `#DBDBDB` | neutral | Background for most content cards, providing a raised appearance against Desktop Gray. |
| Input Fill | `#EDEDED` | neutral | Background for input fields, offering a slightly darker, recessed feel than Card Surface. |
| Border Grey | `#808080` | neutral | Subtle borders and shadow elements, differentiating interactive states and structural lines. |
| Badge Base | `#C2C2C2` | neutral | Background for secondary badges, providing a low-contrast anchor. |
| Attention Yellow | `#F9F5A2` | brand | Highlight and accent for specific text, like 'your' in headings, adding a playful, emphasizing warmth. |
| Sky Blue | `#648FE0` | brand | Background for certain cards or visual elements, introducing a cool, digital hue. |
| Blush Sand | `#E2CEB8` | brand | Background for conversational elements or unique cards, adding a soft, warm undertone. |
| Desert Peach | `#FDCEB1` | brand | Another warm, earthy background accent, often used for smaller content blocks. |
| System Blue | `#0064E2` | accent | Primary interactive color, used sparingly for selected states and active indicators. |
| Alert Red | `#F4625D` | semantic | Stroke color for error states or warnings. |

## Typography

### IBM Plex Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 11px, 14px, 16px, 18px, 25px, 43px |
| lineHeight | 1.40 |
| substitute | monospace |
| role | Primary content font for body text, links, smaller headings, badges, and image captions. Its monospace nature grounds the design in a technical, almost code-like aesthetic, offering a contrast to the organic content. |

### Helvetica

| Key | Value |
| --- | --- |
| weight | 300, 400, 700 |
| sizes | 11px, 18px, 44px |
| lineHeight | 1.40 |
| substitute | sans-serif |
| role | Used for prominent headlines and main titles. The lighter weight 300 for display sizes adds an unexpected elegance, preventing the headlines from shouting, while weight 700 provides clear emphasis on key information. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 11 |  | 1.4 |  |
| body-sm | 14 |  | 1.4 |  |
| body | 16 |  | 1.4 |  |
| subheading | 18 |  | 1.4 |  |
| heading | 25 |  | 1.4 |  |
| heading-lg | 43 |  | 1.4 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 10px |
| images | 10px |
| inputs | 25px |
| buttons | 20px |

- **elementGap** — 8px
- **sectionGap** — 24px
- **cardPadding** — 12px
- **pageMaxWidth** — 

## Components

### Featured Project Card

### Your Button — Clock Widget

### Hero Headline with Highlight + Contact Badges

### Primary Card

**Role:** Container for distinct content blocks.

Background #DBDBDB (Card Surface), border-radius 10px, box-shadow rgba(0, 0, 0, 0.16) 0px 3px 6px 0px. Internal padding generally 8px or 12px, creating consistent breathing room.

### Badge (Secondary)

**Role:** Categorization and meta-information.

Background #C2C2C2 (Badge Base), text Ink (#303030), border-radius 20px (full pill shape), padding 4px 12px. Uses IBM Plex Mono, 400 weight.

### Large Input Field

**Role:** Text input areas.

Background #EDEDED (Input Fill), text Ink (#303030), border-top 1px solid Ink (#303030), border-radius 25px. Minimal padding around text: 1px top/bottom, 2px left/right. Uses IBM Plex Mono.

### Headline Highlight

**Role:** Emphasizing specific words within a headline.

Background Attention Yellow (#F9F5A2), applied to text using Helvetica family. No padding or radius specifically, it's a inline text highlight.

### Image Card

**Role:** Displaying images with titles.

Image contained within a card with 10px radius, often featuring a subtle shadow rgba(0, 0, 0, 0.16) 0px 3px 6px 0px. Text is IBM Plex Mono 400 at 14px, Ink (#303030).

### Selected Tab Indicator

**Role:** Highlighting active navigation or selection.

Active state for navigation items, using System Blue (#0064E2) as a background for the 'Selected Work' badge, indicating current view.

## Layout

The page adheres to a mostly full-bleed layout on Desktop Gray (#F2F2F2), but within this, content is structured as a dense, responsive grid of cards. There isn't a strict max-width; instead, cards adapt to available space. The hero section is characterized by a prominent left-aligned heading ('Hi J.S., School is (y)our creative studio.') often featuring a yellow highlight, accompanied by various smaller informational cards. Section rhythm is created by the grid of distinct cards, which vary in size and background color. Content arrangement is dynamic, with no fixed column structure beyond the cascading card layout. Navigation is minimal, a top-left 'Selected Work' badge acting as the primary entry point.

## Imagery

The site uses a highly eclectic mix of visual content, creating a 'scrapbook' or 'mood board' feel. This includes surreal 3D character illustrations (e.g., the profile avatar), real-world product shots (e.g., weather icon), candid photography (e.g., people in top right, news snippets), pixel art, and graphic design samples. Images are often contained within cards with 10px rounded corners, sometimes with soft shadows. The treatment is varied; some images are tightly cropped, others feature a more artistic mask or full-bleed within their card. Role is primarily decorative and atmospheric, showcasing diverse creative outputs rather than a singular product, creating a dense, visually rich experience.

## Dos & Donts

### Do

- Use Desktop Gray (#F2F2F2) as the default page background to establish the primary canvas.
- Apply Card Surface (#DBDBDB) and 10px border-radius to all primary content cards.
- Headlines should primarily use Helvetica, varying weights (300, 400, 700) and sizes (18px, 44px) for hierarchy.
- Body text and secondary information must use IBM Plex Mono, 400 weight, with varying sizes (11px, 14px, 16px).
- Employ Attention Yellow (#F9F5A2) as a background highlight for specific words or phrases to draw playful attention.
- All interactive input fields should have Input Fill (#EDEDED) background, a 1px Ink (#303030) top border, and 25px border-radius.
- Use 8px or 12px for internal padding within cards to ensure consistent element spacing.

### Don't

- Avoid generic button styles; prefer a pill shape (20px radius) for badges and inputs to maintain the system's character.
- Do not use highly saturated colors for large background areas; save vivid colors for illustrations or small, deliberate accents.
- Do not introduce new shadow styles; adhere to rgba(0, 0, 0, 0.16) 0px 3px 6px 0px for elevated cards.
- Refrain from using System Blue (#0064E2) as a general background or text color; reserve it for distinct interactive states.
- Do not use letter-spacing values other than 'normal' for either font; the expressive quality comes from font choice and weight variation.
- Avoid sharp corners on interactive or card elements; the system prefers soft rounding (10px, 20px, 25px).

## Notes

### Agent Prompt Guide

### Quick Color Reference
- Text: Ink (#303030)
- Background: Desktop Gray (#F2F2F2)
- Card Background: Card Surface (#DBDBDB)
- Accent (Highlight): Attention Yellow (#F9F5A2)
- Interactive: System Blue (#0064E2)

### 3-5 Example Component Prompts
1. Create a primary content card: background Card Surface (#DBDBDB), 10px border-radius, box-shadow rgba(0, 0, 0, 0.16) 0px 3px 6px 0px, with 12px padding. Inside, place a 'Featured News' heading (IBM Plex Mono 400, 16px, Ink #303030).
2. Generate a large headline: 'Our Creative Studio' using Helvetica 300, 44px, Ink (#303030). Highlight the word 'Creative' with background Attention Yellow (#F9F5A2).
3. Design an input field: background Input Fill (#EDEDED), border-top 1px solid Ink (#303030), 25px border-radius, text Ink (#303030) using IBM Plex Mono 400, 16px. Add placeholder 'Input your name'.
4. Create a small badge for categorization: background Badge Base (#C2C2C2), border-radius 20px, padding 4px 12px. Text 'Campaign' in Ink (#303030), IBM Plex Mono 400, 11px.
5. Assemble a body text block: two paragraphs of dummy text in IBM Plex Mono 400, 14px, Ink (#303030), with a line-height of 1.4. Apply 8px bottom margin to the first paragraph.

---
_Source: https://styles.refero.design/style/a521abb9-d84b-4870-b5a8-363be7c3f94a_
