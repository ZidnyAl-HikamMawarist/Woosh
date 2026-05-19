---
name: Velocity & Precision
colors:
  surface: '#f9f9f9'
  surface-dim: '#dadada'
  surface-bright: '#f9f9f9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f3'
  surface-container: '#eeeeee'
  surface-container-high: '#e8e8e8'
  surface-container-highest: '#e2e2e2'
  on-surface: '#1a1c1c'
  on-surface-variant: '#5e3f3b'
  inverse-surface: '#2f3131'
  inverse-on-surface: '#f1f1f1'
  outline: '#936e69'
  outline-variant: '#e9bcb6'
  surface-tint: '#c0000c'
  primary: '#b5000b'
  on-primary: '#ffffff'
  primary-container: '#e30613'
  on-primary-container: '#fff5f3'
  inverse-primary: '#ffb4aa'
  secondary: '#5f5e5e'
  on-secondary: '#ffffff'
  secondary-container: '#e4e2e1'
  on-secondary-container: '#656464'
  tertiary: '#575959'
  on-tertiary: '#ffffff'
  tertiary-container: '#707171'
  on-tertiary-container: '#f6f7f7'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad5'
  primary-fixed-dim: '#ffb4aa'
  on-primary-fixed: '#410001'
  on-primary-fixed-variant: '#930007'
  secondary-fixed: '#e4e2e1'
  secondary-fixed-dim: '#c8c6c6'
  on-secondary-fixed: '#1b1c1c'
  on-secondary-fixed-variant: '#474747'
  tertiary-fixed: '#e2e2e2'
  tertiary-fixed-dim: '#c6c6c7'
  on-tertiary-fixed: '#1a1c1c'
  on-tertiary-fixed-variant: '#454747'
  background: '#f9f9f9'
  on-background: '#1a1c1c'
  surface-variant: '#e2e2e2'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  margin-mobile: 20px
  margin-tablet: 32px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style
The design system is engineered for speed, reliability, and modern transport logistics. It targets a broad demographic of travelers—from business professionals to vacationing families—requiring a UI that feels both authoritative and effortless.

The aesthetic follows a **Corporate Modern** approach with **Minimalist** influences. By prioritizing clarity and reduced cognitive load, the design system ensures users can navigate complex booking flows with high confidence. The interface utilizes generous whitespace to convey a sense of "premium speed," mirroring the aerodynamic and clean nature of high-speed rail travel.

## Colors
The palette is rooted in high-contrast signals to ensure visibility in varied lighting conditions, such as bright platforms or dimly lit train cars.

- **Primary Red (#E30613):** Reserved for primary actions, branding, and critical status indicators (e.g., "Book Now", "Live Updates").
- **Deep Grey (#333333):** Used for primary typography and iconography to maintain high legibility and a professional tone.
- **Light Grey (#F5F5F5):** The foundational background color, providing a soft canvas that reduces screen glare.
- **Pure White (#FFFFFF):** Utilized for component surfaces (cards, inputs) to create clear "islands" of information against the neutral background.

## Typography
This design system utilizes **Inter** for all roles to ensure a systematic and utilitarian feel. The typeface’s high x-height and neutral character make it exceptionally readable for data-heavy schedules and ticket details.

Hierarchies are established primarily through weight (SemiBold/Bold for headers) and subtle letter-spacing adjustments on labels. Display and Headline sizes use tighter tracking to feel more "compact" and fast. Small labels (e.g., train numbers, platform details) should prioritize medium weights to ensure they don't disappear on high-resolution mobile displays.

## Layout & Spacing
The layout model follows a **Fluid Grid** logic optimized for mobile touch targets. It uses an 8px base unit to maintain a strict mathematical rhythm.

- **Mobile:** 4-column grid with 20px outer margins.
- **Tablet:** 8-column grid with 32px outer margins.
- **Desktop:** Fixed max-width of 1200px, centered, with a 12-column grid.

Components should utilize "Stacking" logic for vertical layouts, where `stack-md` (16px) is the standard gap between related elements in a list, and `stack-lg` (24px) separates distinct sections (e.g., "Search Results" from "Recent History").

## Elevation & Depth
To maintain a modern and professional feel, this design system uses **Tonal Layers** combined with **Ambient Shadows**.

Depth is created by placing White `#FFFFFF` cards on the Light Grey `#F5F5F5` background. To distinguish interactive elements (like a ticket or a promo card), apply a soft, diffused shadow:
- **Shadow-Sm:** `0px 2px 4px rgba(0, 0, 0, 0.05)` (for buttons and inputs).
- **Shadow-Md:** `0px 8px 16px rgba(0, 0, 0, 0.08)` (for cards and navigation bars).
- **Shadow-Lg:** `0px 16px 32px rgba(0, 0, 0, 0.12)` (for modals and bottom sheets).

Avoid heavy black shadows; the goal is a subtle lift that feels integrated into the environment rather than floating high above it.

## Shapes
The shape language is **Rounded**, striking a balance between friendly approachability and the structural precision of engineering.

A base radius of `0.5rem` (8px) is applied to buttons, input fields, and small UI components. Larger containers, such as ticket cards and promotional banners, utilize `rounded-lg` (16px) to emphasize their role as primary content containers. This consistency in curvature ensures that even with a high-contrast red-and-white color scheme, the interface feels sophisticated and safe.

## Components

### Buttons
- **Primary:** Solid `#E30613` background with White text. Bold weight.
- **Secondary:** White background with a 1px `#E30613` border or Light Grey background for low-priority actions.
- **State:** On press, darken the red by 10%.

### Input Fields
- **Search/Form Fields:** White background, 1px border in `#D1D1D1` (light-mid grey).
- **Focus State:** Border changes to `#E30613` with a subtle outer glow.
- **Icons:** Use Deep Grey icons for leading/trailing elements (e.g., Search icon, Calendar icon).

### Cards
- **Ticket Cards:** White surface, `#F5F5F5` background context. Must include a dashed "tear" line visual or a subtle shadow to indicate a physical ticket metaphor.
- **Promo Cards:** Use full-bleed imagery with a text overlay or a primary red accent bar on the left edge.

### Lists & Navigation
- **Schedules:** Use a vertical timeline-inspired list component. Stations are marked with Deep Grey dots, connected by a 2px vertical line.
- **Bottom Navigation:** Fixed White bar with `#E30613` active icons and Deep Grey inactive icons.

### Indicators
- **Seat Map:** Use `#E30613` for selected seats, `#D1D1D1` for occupied, and White with a light border for available.