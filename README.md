# cordova-plugin-system-bars

A Cordova plugin that is aimed to replace the existing status bar plugin and support a more modern approach to handling of system bars. This plugin allows you to manage the appearance of the system bars (status and navigation bars) on Android and iOS, including setting their style, visibility, and handling insets for edge-to-edge displays.

## Installation

```
npx cordova plugin add cordova-plugin-system-bars
```

## Supported Platforms

- Android
- iOS

## API

### `SystemBars.setStyle(insetGlyphStyle, inset)`

Sets the style of the system bar glyphs (e.g., clock, battery, navigation icons).

- `insetGlyphStyle`: `"light"` or `"dark"`.
- `inset` (optional): `"top"` or `"bottom"`. If not provided, the style is applied to all system bars. If styling bottom, it only works if you are using three button navigation

**Example:**

```javascript
// Set status bar icons style to dark, which will make the icons light
SystemBars.setStyle('dark', 'top');

// Set navigation bar icons to be light
SystemBars.setStyle('light', 'bottom');
```

### `SystemBars.setHidden(hideInset, inset)`

Hides or shows the system bars.

- `hideInset`: `true` to hide, `false` to show.
- `inset` (optional): `"top"`, `"bottom"`, `"left"`, or `"right"`. If not provided, all system bars are affected.

**Example:**

```javascript
// Hide the status bar
SystemBars.setHidden(true, 'top');

// Show all system bars
SystemBars.setHidden(false);
``` 

### Properties

These properties reflect the current visibility state of the system bars.

- `SystemBars.topVisible: boolean`
- `SystemBars.leftVisible: boolean`
- `SystemBars.rightVisible: boolean`
- `SystemBars.bottomVisible: boolean`