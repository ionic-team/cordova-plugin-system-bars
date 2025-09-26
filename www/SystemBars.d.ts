declare namespace SystemBars {
  interface Insets {
    top?: number;
    right?: number;
    bottom?: number;
    left?: number;
  }

  let topVisible: boolean;
  let leftVisible: boolean;
  let rightVisible: boolean;
  let bottomVisible: boolean;

  /**
   * Sets the style of the system bar glyphs.
   * @param insetGlyphStyle The style to set ('light' or 'dark').
   * @param inset The specific inset to style. If not provided, all insets are styled.
   */
  function setStyle(
    insetGlyphStyle: "light" | "dark",
    inset?: "top" | "bottom"
  ): void;

  /**
   * Hides or shows the system bars.
   * @param hideInset Whether to hide the insets.
   * @param inset The specific inset to hide/show. If not provided, all insets are affected.
   */
  function setHidden(
    hideInset: boolean,
    inset?: "top" | "bottom" | "left" | "right"
  ): void;
}

export = SystemBars;
