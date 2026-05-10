# Decide AI — Design Reference

> Midnight Terminal, illuminated by a single green command prompt.

## Meta

| Key | Value |
| --- | --- |
| Source site | [https://decideai.xyz](https://decideai.xyz) |
| Refero page | [https://styles.refero.design/style/5d9e1cc2-4b81-40fe-aa92-640f2e1d7420](https://styles.refero.design/style/5d9e1cc2-4b81-40fe-aa92-640f2e1d7420) |
| Theme | dark |
| Industry | ai |

## Overview

Decide AI employs a 'digital ledger' aesthetic: a high-contrast dark theme with sharp typographic details and a singular, vibrant green accent. The interface feels structured and minimal, prioritizing clear information delivery through strong lines, subtle gray borders, and a stark black-and-white canvas. Visual weight is carried by typography and strategic pops of accent green, creating a sense of precision and focused energy. Components are lightweight, relying on outlines rather than heavy fills, and often feature square or aggressively rounded corners to delineate boundaries.

## Colors

| Name | Hex | Group | Role |
| --- | --- | --- | --- |
| Deep Space | `#030303` | neutral | Primary background for pages and card surfaces, heading text. Creates a high-contrast foundation for the UI |
| Canvas White | `#ffffff` | neutral | Primary text color for body and link text against dark backgrounds. Also used for inverse surfaces and some internal fills |
| System Gray | `#e5e7eb` | neutral | Subtle borders for cards, list items, and sections. Provides visual separation without introducing heavy elements |
| Terminal Green | `#73ffb9` | accent | Green outline accent for tags, dividers, and focused UI edges. Do not promote it to the primary CTA color |

## Typography

### PP Neue Montreal

| Key | Value |
| --- | --- |
| weight | 400, 500, 700 |
| weights | 400, 500, 700 |
| sizes | 13px, 14px, 16px, 17px, 18px, 22px, 23px, 30px, 40px, 46px, 50px, 54px, 110px, 220px |
| lineHeight | 1.00, 1.27, 1.38, 1.50, 1.56, 1.62 |
| letterSpacing | -0.0360em at large sizes, -0.0320em at smaller sizes |
| substitute | Inter |
| role | Primary typeface for all text. The subtle letter-spacing adjustment enhances readability for both large headlines and smaller body text, contributing to the crisp, modern feel of the interface. |

### Type scale

| Role | Size | Weight | Line height | Letter spacing |
| --- | --- | --- | --- | --- |
| caption | 13 |  | 1.5 |  |
| body-sm | 16 |  | 1.5 |  |
| body | 17 |  | 1.5 |  |
| body-lg | 22 |  | 1.5 |  |
| heading-sm | 30 |  | 1.3 |  |
| heading | 40 |  | 1.3 |  |
| heading-lg | 50 |  | 1.3 |  |
| display-sm | 54 |  | 1.3 |  |
| display | 220 |  | 1.3 |  |

## Spacing

### radius

| Key | Value |
| --- | --- |
| links | 8.64px |
| other | 9999px |

- **elementGap** — 20px
- **sectionGap** — 56px
- **cardPadding** — 20px
- **pageMaxWidth** — 

## Components

### Ghost Border Button

**Role:** Navigation links and secondary actions

Text in Canvas White (#ffffff) on a transparent background, with a 1px System Gray (#e5e7eb) border. Features a pill-shape radius (9999px).

### Horizontal Divider

**Role:** Visual separation of sections or content blocks

A 1px solid line using System Gray (#e5e7eb) to structure content within the Deep Space (#030303) background.

### Feature Card

**Role:** Displaying product features or services

Deep Space (#030303) background with a 1px System Gray (#e5e7eb) border. Features rounded corners of 8.64px and padding set to 20px on all sides. Headings in Canvas White (#ffffff), body text in System Gray (#e5e7eb), with Terminal Green (#73ffb9) for icons or status indicators.

### Inline State Text

**Role:** Highlighting status or category within content

Canvas White (#ffffff) text (e.g. '01. DATA') combined with a Terminal Green (#73ffb9) color for an associated visual indicator or initial characters. Letter-spacing follows the general typographic rules for its size.

### Pill Accent Tag

**Role:** Categorization or short descriptive tags

Small PP Neue Montreal text in Canvas White (#ffffff) set against a Deep Space (#030303) background, with a 9999px border-radius to create a pill shape. Outline in System Gray (#e5e7eb).

## Layout

The page primarily uses a full-bleed dark background. Hero sections feature a centered, large headline (e.g., 'The future of intelligence') directly on a Deep Space canvas. Content sections alternate between these large headlines and structured blocks. A notable pattern is the use of 3-column card grids, often with a 1px System Gray divider. Vertical rhythm is maintained by consistent section gaps. Navigation is a minimalist top-right menu with text links against the dark background, with the brand logo top-left.

## Imagery

The site uses minimal imagery, primarily relying on UI elements and typography. When visuals appear, they are abstract, geometric icons with thin outlines, often filled subtly with the brand's Terminal Green (#73ffb9) or appearing in monochrome against the dark background. The icons serve to explain content rather than decorate. No photography or complex illustrations are present. Image density is low; the page is text-dominant.

## Dos & Donts

### Do

- Prioritize a high-contrast combination of Deep Space (#030303) backgrounds and Canvas White (#ffffff) text for primary content.
- Use System Gray (#e5e7eb) exclusively for subtle borders, dividers, and secondary textual elements to maintain a clean, structured appearance.
- Apply Terminal Green (#73ffb9) sparingly as an accent color for interactive states, icons, and key highlights.
- Ensure all interactive elements, like buttons and links, use a pill-radius (9999px) for a consistent playful edge.
- Implement letter-spacing adjustments (-0.0360em / -0.0320em) for PP Neue Montreal across different sizes to maintain typographic crispness.
- Maintain a clear visual hierarchy by differentiating headings from body text through size and weight, always using PP Neue Montreal.
- Use a generous elementGap of 20px between content blocks and components to ensure comfortable density and visual separation.

### Don't

- Do not introduce additional saturated colors beyond Terminal Green (#73ffb9) that would dilute the brand's sharp, focused aesthetic.
- Avoid heavy drop shadows or complex gradients; prefer flat designs and subtle border treatments for elevation.
- Do not use generic system fonts; always specify PP Neue Montreal or its recommended substitute Inter.
- Do not use square corners where pill-radius is expected, particularly for buttons and tags.
- Avoid dense, unbroken blocks of text; break content into manageable chunks separated by generous element and section gaps.

## Notes

### Agent Prompt Guide

Quick Color Reference: 
text: #ffffff 
background: #030303 
border: #e5e7eb 
accent: #73ffb9 
primary action: no distinct CTA color

Example Component Prompts:
1. Create a primary headline section: Full-width Deep Space (#030303) background. Centered headline 'The Future of AI' (PP Neue Montreal, 110px, weight 700, #ffffff, letter-spacing -0.0360em). Below it, a sub-headline 'Building intelligent systems.' (PP Neue Montreal, 22px, weight 400, #ffffff).
2. Create a feature card: Deep Space (#030303) background, 1px System Gray (#e5e7eb) border, 8.64px radius. Inside, 20px padding. Top-aligned icon filled with Terminal Green (#73ffb9). Headline 'Decide Protocol' (PP Neue Montreal, 30px, weight 500, #ffffff). Body text 'Build & perfect the LLM from scratch.' (PP Neue Montreal, 16px, weight 400, #ffffff).
3. Create a ghost navigation link: 'White Paper' text in Canvas White (#ffffff) using PP Neue Montreal, 16px, weight 400. Transparent background, 1px System Gray (#e5e7eb) border, 9999px radius. Padding 6px vertical, 12px horizontal.

---
_Source: https://styles.refero.design/style/5d9e1cc2-4b81-40fe-aa92-640f2e1d7420_
