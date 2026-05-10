# Airtable — Design Reference

> Polished Workflow, Vibrant Efficiency — like a perfectly organized, brightly lit command center.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://airtable.com](https://airtable.com) |
| Refero page | [https://styles.refero.design/style/f4ef80f4-f6e5-4aea-8045-f99efaf208b8](https://styles.refero.design/style/f4ef80f4-f6e5-4aea-8045-f99efaf208b8) |
| Theme | light |
| Industry | productivity |

## Overview

The Airtable design system conveys an approachable enterprise workflow platform. Its light theme and ample spacing provide a sense of clarity, grounded by a distinctive dark blue (#181D26) for strong text elements and primary actions. Functional interactivity is highlighted by a bright blue (#1B61C9) while a palette of vivid, multi-colored accents introduces a playful, almost illustrative quality, suggesting flexibility and creativity within a structured environment. Rounded corners and subtle shadows soften the experience, making complex AI tools feel less intimidating.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| White Canvas | `#ffffff` | neutral | Page background, primary surface color for cards and major sections. |
| Cloud Whisper | `#f8fafc` | neutral | Secondary background, subtle visual break for content cards and sections. |
| Warm Parchment | `#faf5e8` | neutral | Dominant canvas color, offers a slight warmth compared to pure white, establishing the overall page tone. |
| Slate Ink | `#181d26` | neutral | Primary heading color, strong body text, and background for primary action buttons. The core dark neutral. |
| Deep Graphite | `#333333` | neutral | General text color, provides strong contrast against light backgrounds. |
| Soft Steel | `#333840` | neutral | Default body text, slightly softer than Deep Graphite for secondary text information. |
| Silver Mist | `#e0e2e6` | neutral | Subtle borders, dividers, and disabled button backgrounds. |
| Muted Stone | `#41454d` | neutral | Less prominent body text, link text in paragraphs. |
| Cool Gray | `#9297a0` | neutral | Navigation text, inactive icons, subtle borders. |
| Frost | `#c7e5f2` | neutral | Light background shade, used on some interactive elements when hovered. |
| Dark Shadow | `#040e20` | neutral | Very dark text color, appears for headings and body text, a deeper contrast option. |
| Ocean Accent | `#1b61c9` | accent | Call-to-action buttons, active navigation links, and interactive elements. Stands out clearly. |
| Sky Veil | `#c4dbfd` | accent | Subtle accent, often appears as a box shadow for active elements or focus states. |
| Amethyst | `#254fad` | brand | Brand accent, used for specific interactive elements and highlighting key information, hinting at information or success. |
| Burnt Sienna | `#aa2d00` | brand | Brand accent, used in specific illustrative contexts and text highlighting, indicates urgency or warning. |
| Forest Nudge | `#0a2e00` | brand | Brand accent, used for success indications and positive feedback, often in illustrative elements. |
| Sweet Pink | `#fa91e0` | brand | Brand accent, used decoratively in illustrations and data visualizations for visual separation. |
| Sunburst | `#fcb42a` | brand | Brand accent, used for highlighting and decorative elements, adds a vibrant pop. |
| Mars Red | `#912e1f` | brand | Background for specific card variations, provides a strong, warm contrast. |
| Tropical Orange | `#fcab79` | brand | Background for specific interactive elements or badges, a softer version of the orange accent. |
| Dark Forrest | `#214224` | brand | Darker shade of green, for text against lighter green backgrounds or specific brand elements. |
| Warm Gold | `#423719` | brand | Darker shade of yellow-brown, primarily for headings in certain branded contexts. |

## Typography

### Haas Groot Disp

| Key | Value |
| --- | --- |
| weight | 400, 900 |
| sizes | 20px, 48px |
| lineHeight | 1.50 |
| letterSpacing | 0 |
| substitute | IBM Plex Sans |
| role | Headline font. Used for prominent headings and titles like 'All your teams, all their workflows'. The thick weight at 48px creates a bold, authoritative statement without being overly aggressive due to the spacious line height. |

### Haas

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 14px, 16px, 18px, 20px, 24px, 32px, 40px |
| lineHeight | 1.15, 1.20, 1.25, 1.30, 1.35, 1.50 |
| letterSpacing | 0.007 |
| substitute | Inter |
| role | Primary UI font for body text, navigation, and most interactive elements. Weight 400 maintains approachability. Sizes range from small captions to larger subheadings, ensuring versatility. Letter spacing is slightly positive on smaller text to enhance legibility, tightening to normal at larger sizes. |

### Haas

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 14px, 16px, 18px, 20px, 24px, 32px, 40px |
| lineHeight | 1.15, 1.20, 1.25, 1.30, 1.35, 1.50 |
| letterSpacing | 0.006 |
| substitute | Inter |
| role | Medium weight for secondary interactive elements, button text, and emphasized body copy. Provides a subtle distinction from regular body text without becoming too heavy. |

### Haas

| Key | Value |
| --- | --- |
| weight | 400, 500, 600 |
| sizes | 14px, 16px, 18px, 20px, 24px, 32px, 40px |
| lineHeight | 1.15, 1.20, 1.25, 1.30, 1.35, 1.50 |
| letterSpacing | 0.005 |
| substitute | Inter |
| role | Semibold weight used for navigation headings, strong labels, and section titles where additional emphasis is paramount. It provides hierarchy and readability against lighter backgrounds. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 14 |  | 1.35 | 0.007 |
| body | 16 |  | 1.3 | 0.007 |
| subheading | 18 |  | 1.25 | 0.007 |
| heading-sm | 20 |  | 1.2 | 0.006 |
| heading | 24 |  | 1.2 | 0.006 |
| heading-lg | 32 |  | 1.15 | 0.005 |
| display | 48 |  | 1.5 | 0 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| nav | 12px |
| cards | 16px |
| links | 12px |
| buttons | 12px |

- **elementGap** — 16px
- **sectionGap** — 40-80px
- **cardPadding** — 24-48px
- **pageMaxWidth** — 1280px

## Surfaces

| Name | Hex | Level | Purpose |
| --- | --- | --- | --- |
| Warm Parchment | `#faf5e8` | 0 | Dominant page canvas, providing a slightly off-white, warm base. |
| White Canvas | `#ffffff` | 1 | Primary content surface, used for main sections, navigation, and primary cards, sitting directly on the canvas. |
| Cloud Whisper | `#f8fafc` | 2 | Secondary, slightly elevated content surface, for cards or content blocks that need a subtle visual separation or grouping. |

## Components

### Primary Filled Button

**Role:** Call to Action

Solid background in Slate Ink (#181D26), text in White Canvas (#FFFFFF). Rounded with 12px border-radius. Padding 16px vertical, 24px horizontal. Used for primary actions like 'Get started for free'.

### Secondary Outline Button

**Role:** Secondary Action

Transparent background, text and border in Slate Ink (#181D26), no border-radius. Padding 0px. Used for navigation items or subtle secondary actions like 'Book demo'.

### White Filled Button

**Role:** Tertiary/Alternate Action

Solid background in White Canvas (#FFFFFF), text in Slate Ink (#181D26). Rounded with 12px border-radius. Padding 16px vertical, 24px horizontal. Used when a light background is needed against a dark section.

### Compact Nav Link

**Role:** In-line Navigation Link/Tag

Transparent background, text color in Muted Stone (rgba(7, 12, 20, 0.82)), no border-radius. Minimal padding 1px vertical, 6px horizontal. Used for compact links within text or less prominent actions.

### Base Card

**Role:** Content container

Transparent background, no border-radius, no shadow. Padding 0px vertical, 48px horizontal. Used for content sections where the background provides the visual separation.

### Subtle Card

**Role:** Content container

Background in Cloud Whisper (#F8FAFC), 16px border-radius, no shadow. Padding 0px. Used for grouping related content with a soft visual lift.

### Accent Card - Mars Red

**Role:** Highlighted Content Container

Background in Mars Red (#912E1F), 24px border-radius, no shadow. Padding 0px. Used for prominent, branded content blocks.

### White Elevated Card

**Role:** Prominent Content Container

Background in White Canvas (#ffffff), 24px border-radius, no shadow. Padding 0px. Used for main content sections that stand out.

### Input / Search Bar

**Role:** Data Entry Field

Background in White Canvas (#ffffff), text in Slate Ink (#181D26). Border is implied or very subtle. Active state likely shows a subtle blue shadow (#C4DBFD) with `rgba(0, 0, 0, 0.32) 0px 0px 1px 0px` for depth. Rounded corners with a 12px radius.

### Navigation Bar

**Role:** Site Navigation

White Canvas (#ffffff) background, with a subtle shadow (`rgba(15, 48, 106, 0.05) 0px 0px 20px 0px`) for elevation. Contains various links and buttons. Height is around 90-120px.

## Layout

The page primarily follows a max-width contained layout, centering content within a flexible width, likely around 1280px. The hero section features a full-width background (often a solid color or subtle gradient) behind a centered headline and subtext, followed by a large product demonstration graphic that visually breaks the top fold. Sections below alternate between varying background shades (White Canvas, Cloud Whisper, Warm Parchment) and feature common patterns: centered content stacks, text-left/image-right (or vice-versa) modules, and multi-column grid layouts for features and testimonials. Vertical spacing between sections is generous (40-80px), creating a comfortable, open rhythm. The navigation is a sticky top bar, providing persistent access.

## Imagery

The visual language blends product screenshots with abstract, illustrative elements. Product screenshots are typically centered, slightly elevated as if floating, and feature an inner shadow suggesting depth and interaction. Illustrations are often in a flat, geometric style, using the full spectrum of brand accent colors (Amethyst, Burnt Sienna, Forest Nudge, Sweet Pink, Sunburst) to depict data flow, connections, or metaphorical concepts related to workflows and AI. Icons are minimal, either outlined or filled in a mono color (Slate Ink or a brand accent), used functionally rather than decoratively to guide UI. The density is image-moderate, with illustrations breaking up text-heavy sections and product shots showcasing key features directly.

## Dos & Donts

### Do

- Prioritize Slate Ink (#181D26) for headlines and primary actions against light backgrounds for strong contrast.
- Use Ocean Accent (#1B61C9) exclusively for interactive elements like primary CTAs and active navigation links.
- Apply 12px border-radius consistently to all buttons and similar interactive elements for a softened feel.
- Maintain comfortable spacing with 16px for elementGap and 24-48px for cardPadding to ensure visual breathing room.
- Utilize Haas Groot Disp (or IBM Plex Sans fallback) at its 900 weight for eye-catching, high-impact headlines.
- Employ the full palette of vivid brand accents (Amethyst, Burnt Sienna, Forest Nudge, Sweet Pink, Sunburst) for illustrative elements, badges, or specific content blocks, not for core UI text or backgrounds.
- Elevate primary navigation and critical interactive buttons with subtle box shadows to indicate hierarchy and interactivity.

### Don't

- Do not use Gray Mist (#E0E2E6) for primary text or backgrounds that require strong contrast.
- Avoid using the vivid brand accent colors for standard text or backgrounds; reserve them for highlighting and illustration.
- Do not introduce sharp corners on interactive components; maintain the established 12px or 16px radius for buttons and cards.
- Avoid excessive use of shadows; most elevation is achieved through color contrast and subtle background shades.
- Do not use letter-spacing on display-sized text; keep it normal for Haas Groot Disp at 48px.
- Do not condense spacing beyond 16px for element gaps; preserve the comfortable density.

## Notes

### Agent Prompt Guide

**Quick Color Reference:**
- Text (Strong): `#181D26` (Slate Ink)
- Text (Body): `#333840` (Soft Steel)
- Background (Canvas): `#faf5e8` (Warm Parchment)
- Background (Surface 1): `#ffffff` (White Canvas)
- CTA Button (Background): `#181D26` (Slate Ink)
- Accent (Interactive): `#1b61c9` (Ocean Accent)
- Border (Subtle): `#e0e2e6` (Silver Mist)

**3-5 Example Component Prompts:**
1. **Create a hero section:** Background is Warm Parchment (#faf5e8). Centered headline: 'All your teams, all their workflows—connected in one workspace' using Haas Groot Disp, 48px, weight 900, Slate Ink (#181D26). Subtext: 'Build AI-powered workflows...' using Haas, 18px, weight 400, Soft Steel (#333840). Follow with a Primary Filled Button: 'Get started for free' (Slate Ink background, White Canvas text, 12px radius, 16px/24px padding). Below this, an image of the product UI, featuring a search bar component (White Canvas background, 12px radius, subtle shadow, containing an input with Slate Ink text).
2. **Generate a feature card:** Background Cloud Whisper (#f8fafc), 16px border-radius, 24px internal padding. Title: 'Production apps at prototype speed' using Haas, 24px, weight 600, Slate Ink (#181D26). Body text 'Streamline your team’s critical data...' using Haas, 16px, weight 400, Soft Steel (#333840). Include a 'Learn more' Primary Filled Button (Ocean Accent background, White Canvas text, 12px radius, 16px/24px padding) aligned right.
3. **Design a customer logo band:** White Canvas (#ffffff) background, with a top border of Silver Mist (#E0E2E6). Section padding 40px top/bottom. Centered heading 'Trusted by 500,000 leading teams' using Haas, 20px, weight 600, Dark Shadow (#040e20). Display 5-6 customer logos, each treated as a transparent image against the white background, with 32px column and row gaps between them.

---
_Source: https://styles.refero.design/style/f4ef80f4-f6e5-4aea-8045-f99efaf208b8_
