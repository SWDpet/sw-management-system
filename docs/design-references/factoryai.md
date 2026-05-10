# Factory.ai — Design Reference

> Architectural blueprint on white marble. Lines are crisp, colors are limited, and every element serves a clear, functional purpose.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://factory.ai](https://factory.ai) |
| Refero page | [https://styles.refero.design/style/13d6fc89-eba2-4724-ac37-20f4f2e5efec](https://styles.refero.design/style/13d6fc89-eba2-4724-ac37-20f4f2e5efec) |
| Theme | light |
| Industry | ai |

## Overview

Factory.ai embraces a 'technical brutalism meets digital precision' aesthetic, prioritizing informational clarity and directness. The near-monochromatic palette, dominated by light grays and deep charcoals, provides a stark, high-contrast backdrop for technical content. A single vivid orange accent color is deployed sparingly as a functional indicator, highlighting interactive elements and key information without visual noise. The strong typographic voice, characterized by precise letter-spacing and a monospace variant for code, reinforces the structured, engineering-focused identity.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Factory Black | `#020202` | neutral | Primary text, darkest surface background (e.g., active navigation items), critical interactive elements. |
| Factory Light Gray | `#eeeeee` | neutral | Page background, light surface elements (card backgrounds), default button backgrounds. Provides a clean, spacious canvas. |
| Faded Silver | `#fafafa` | neutral | Slightly lighter alternative to Factory Light Gray, used for subtle differentiation of card backgrounds and elements. |
| Cool Gray | `#b8b3b0` | neutral | Subtle borders, inactive button outlines, secondary text. Establishes divisions without harshness. |
| Graphite | `#3d3a39` | neutral | Strong borders, dark icons, secondary text. A darker gray for depth and contrast. |
| Ash Gray | `#a49d9a` | neutral | Subtle interactive borders and backgrounds, similar to Cool Gray but with a touch more warmth. |
| Code Orange | `#ef6f2` | accent | Accent color for 'NEW' badges, interactive indicators, and small, high-attention elements. Its vividness cuts through the neutral palette. |

## Typography

### Geist

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 14px, 16px, 18px, 24px, 48px, 60px |
| lineHeight | 1.00, 1.20, 1.50 |
| letterSpacing | -0.0480em, -0.0300em |
| substitute | Inter |
| role | Primary typeface for all headings, body text, navigation, and general UI. The carefully tuned negative letter-spacing, particularly at larger sizes, creates a composed, intentional feel, preventing headlines from feeling loose. |

### Geist Mono

| Key | Value |
| --- | --- |
| weight | 400 |
| sizes | 12px, 14px, 16px, 18px |
| lineHeight | 1.00, 1.20, 1.38, 1.50 |
| letterSpacing | -0.0200em |
| substitute | JetBrains Mono |
| role | Used for code snippets, CLI instructions, and any content requiring a fixed-width, precise presentation. Its subtle negative letter-spacing maintains a tight, readable block structure. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 12 |  | 1.5 | -0.24 |
| body-sm | 14 |  | 1.5 |  |
| body | 16 |  | 1.5 |  |
| subheading | 18 |  | 1.2 |  |
| heading | 24 |  | 1.2 |  |
| heading-lg | 48 |  | 1.2 | -2.3 |
| display | 60 |  | 1 | -2.88 |

## Spacing

### radius

| Key | Value |
| --- | --- |
| cards | 6px |
| header | 0px |
| buttons | 4px |
| default | 4px |

- **elementGap** — 4px
- **sectionGap** — 72px
- **cardPadding** — 16px
- **pageMaxWidth** — 

## Components

### CLI Install Block

### News Article Cards

### Product Section Nav

### Text Link

**Role:** Navigation, inline links, 'Learn More' buttons

Color Factory Black (#020202) for primary links, transitioning to Graphite (#3d3a39) for secondary. No explicit underline until hover, relying on contrast and context for discoverability. Uses Geist, 14-16px, weight 400.

### Navigation Link

**Role:** Top navigation menu items

Color Factory Black (#020202) on Factory Light Gray (#eeeeee) background. No special styling, relying solely on typography (Geist, 14px, 400 weight) for visual presence. Active items use the same styling with a subtle visual cue or background change.

### Ghost Button

**Role:** Secondary actions, grouped options (macOS / Linux)

Transparent background with text color Factory Black (#020202). Has a subtle Cool Gray (#b8b3b0) border. Padding 0 for inline context. Radius 0px.

### Outlined Button

**Role:** Download buttons, secondary calls to action

Transparent background with Factory Black (#020202) text. Border color Cool Gray (#b8b3b0), 1px solid. Padding 0 vertically, 12px horizontally. Radius 4px. Font Geist, 16px, 400 weight.

### Filled Button (Light)

**Role:** Download buttons, primary calls to action

Background Factory Light Gray (#eeeeee), text Factory Black (#020202). Border color Ash Gray (#a49d9a). Padding 0 vertically, 12px horizontally. Radius 4px. Font Geist, 16px, 400 weight.

### Filled Button (Dark)

**Role:** Download buttons, primary calls to action (alternative)

Background Factory Black (#020202), text Factory Black (#020202). Border color Ash Gray (#a49d9a). Padding 6px vertically, 12px horizontally. Radius 8px. The dark background with dark text is counter-intuitive for contrast, suggesting a specific functional or state-based context.

### List Item Card

**Role:** Content blocks in feature sections

Transparent background, no shadow, 0px border radius. Padding 0 vertically, 16px horizontally. Used as a container for grouped information. Text uses Factory Black (#020202).

### Elevated Content Card

**Role:** Featured content blocks, forms, interactive elements

Background Faded Silver (#fafafa), no shadow, 6px border radius. Padding 16px vertically, 0 horizontally. Provides a slight visuallift from the main background.

### Code Input Block

**Role:** CLI instruction display, interactive code examples

Background Factory Light Gray (#eeeeee), no shadow, 6px border radius. Padding 0. Contains monospaced text for commands. May feature interactive elements like a copy button.

### 'NEW' Badge

**Role:** Highlights new features or content

Transparent background, text Code Orange (#ef6f2e). Radius 0px, padding 0. Appears as a small, vivid text label next to titles, using Geist Mono 12px.

## Layout

The page structure employs a full-width layout with a primary content area constrained by a clear maximum width, centered on the screen. The hero section is a split two-column design: text-dominant on the left with a headline and descriptive copy, and abstract/UI visuals on the right, punctuated by sparse dot patterns. Sections generally follow a consistent vertical rhythm, often alternating between text-heavy content and content paired with product screenshots or conceptual graphics, typically in a two-column arrangement (text left, image right, or vice versa). There are occasional three-column card grids for presenting features or articles. The navigation is a persistent top bar, clean and functional, with a clear separation of branding and menu items. The layout emphasizes clarity and content organization, feeling spacious yet structured.

## Imagery

The visual language for imagery is primarily functional and technical, leaning heavily on abstract conceptual graphics, UI screenshots, and code blocks. Product screenshots are contained within precise, slightly rounded frames, often featuring stylized UI elements rather than raw interfaces. Graphics are typically monochromatic or use a limited palette, often employing dotted patterns (like the 'grid' in the hero section) and stark lines. There's an absence of photography or human elements, focusing instead on the tools and concepts of software development. Imagery serves an explanatory role, illustrating functionality or abstracting complex ideas, with a high density relative to other pure UI sites.

## Dos & Donts

### Do

- Prioritize Factory Black (#020202) for primary text and Factory Light Gray (#eeeeee) for background, ensuring AAA contrast.
- Apply Geist font consistently for all UI text, utilizing negative letter-spacing for large headlines (e.g., -0.0480em at 60px) to achieve a condensed, precise appearance.
- Use Geist Mono for all code snippets and CLI instructions at weights 400 and sizes 12-18px for clear distinction.
- Implement Cool Gray (#b8b3b0) for subtle borders and dividers to maintain visual structure without heavy lines.
- Reserve Code Orange (#ef6f2e) strictly for highlighting functional elements like 'NEW' badges and active indicators.
- Maintain a default border radius of 4px for buttons and form elements, extending to 6px for elevated cards.
- Ensure consistent vertical spacing of 24px and horizontal elements gaps of 12px or 8px using the base 4px unit.

### Don't

- Avoid using chromatic colors beyond Code Orange (#ef6f2e) to maintain the stark, technical aesthetic.
- Do not introduce shadows or complex gradients; rely on color and typography for hierarchy and depth.
- Do not use generic system fonts; always specify Geist or Geist Mono for design consistency.
- Avoid excessive padding or large border radii; the design favors a compressed, precise feel.
- Do not use underlines for links unless on hover, rely on color and context (Factory Black on light backgrounds).
- Avoid arbitrary text styling (bolding, italics); rely on the established type scale (Geist, 400 weight) for hierarchy.
- Do not deviate from the specified negative letter-spacing values, especially for headlines, as it is a core characteristic of the brand's typography.

## Notes

### Agent Prompt Guide

### Quick Color Reference
- **Text Primary:** #020202
- **Page Background:** #eeeeee
- **Card Background:** #fafafa
- **Border/Divider:** #b8b3b0
- **Accent:** #ef6f2e

### Example Component Prompts
1. **Create a Hero Section:** Set page background to Factory Light Gray (#eeeeee). Left half: headline 'Agent-Native Software Development' in Geist, 60px, weight 400, letter-spacing -0.0480em, color Factory Black (#020202). Subheading 'The only software development agents that work everywhere you do.' in Geist, 18px, weight 400, color Graphite (#3d3a39). Right half: an abstract graphic with subtle dotted patterns.
2. **Generate an Outlined Button:** Label 'Download macOS (Apple Silicon)'. Use transparent background, Factory Black (#020202) text, Cool Gray (#b8b3b0) 1px border. Padding top/bottom 0px, left/right 12px. Border radius 4px. Font Geist, 16px, weight 400.
3. **Design a Code Input Block:** Use background Factory Light Gray (#eeeeee), no border, 6px border-radius. Inside, display code `curl -fsSL https://app.factory.ai/cli | sh` in Geist Mono, 16px, weight 400, letter-spacing -0.0200em, color Factory Black (#020202). Add a copy icon next to it.
4. **Create an Elevated Content Card:** Use background Faded Silver (#fafafa), no box-shadow, 6px border-radius. Padding 16px top/bottom, 0px left/right. Insert a 'NEW' badge next to a section title. The 'NEW' badge should be text 'NEW' in Geist Mono, 12px, weight 400, color Code Orange (#ef6f2e).
5. **Build a Navigation Bar:** Use background Factory Light Gray (#eeeeee) with no border. Nav links like 'Product', 'Enterprise' use Geist, 14px, weight 400, color Factory Black (#020202). Include 'Log In' button as a Filled Button (Dark) variant and 'Contact Sales' as an Outlined Button.

---
_Source: https://styles.refero.design/style/13d6fc89-eba2-4724-ac37-20f4f2e5efec_
