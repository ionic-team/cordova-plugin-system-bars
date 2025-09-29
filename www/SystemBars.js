var exec = require("cordova/exec");

var SystemBars = {
  topVisible: true,
  leftVisible: true,
  rightVisible: true,
  bottomVisible: true,

  setStyle: function (insetGlyphStyle, inset) {
    exec(null, null, "SystemBars", "setStyle", [insetGlyphStyle, inset]);
  },

  setHidden: function (hideInset, inset) {
    exec(null, null, "SystemBars", "setHidden", [hideInset, inset]);
    if (inset === "top") SystemBars.topVisible = !hideInset;
    if (inset === "left") SystemBars.leftVisible = !hideInset;
    if (inset === "right") SystemBars.rightVisible = !hideInset;
    if (inset === "bottom") SystemBars.bottomVisible = !hideInset;
    if (!inset) {
      SystemBars.topVisible = !hideInset;
      SystemBars.leftVisible = !hideInset;
      SystemBars.rightVisible = !hideInset;
      SystemBars.bottomVisible = !hideInset;
    }
  },
};

window.setTimeout(function () {
  exec((_) => _, null, "SystemBars", "_ready", []);
}, 0);

module.exports = SystemBars;
