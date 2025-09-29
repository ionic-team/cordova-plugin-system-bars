package org.apache.cordova.systemBars;

import android.graphics.Color;
import android.os.Build;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

//    we need to inject the existing --status-bar-height css variable (since we are removing the status bar)
//    we are adding a new extensibility SystemBarsStyle

public class SystemBars extends CordovaPlugin {
    private String currentStyle = "dark";

    @Override
    protected void pluginInitialize() {
        Activity activity = this.cordova.getActivity();
        Window window = activity.getWindow();
        View decorView = window.getDecorView();

        this.setStyle(preferences.getString("SystemBarsStyle", currentStyle).toLowerCase(), null);

        decorView.setOnApplyWindowInsetsListener((v, insets) -> setupSafeAreaInsets(insets));
    }

    private WindowInsets setupSafeAreaInsets(WindowInsets insets) {
        WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets,
                this.cordova.getActivity().getWindow().getDecorView());
        androidx.core.graphics.Insets systemBars = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
        androidx.core.graphics.Insets displayCutout = insetsCompat.getInsets(WindowInsetsCompat.Type.displayCutout());

        int top = Math.max(systemBars.top, displayCutout.top);
        int bottom = Math.max(systemBars.bottom, displayCutout.bottom);
        int left = Math.max(systemBars.left, displayCutout.left);
        int right = Math.max(systemBars.right, displayCutout.right);

        injectSafeAreaCSS(top, right, bottom, left);
        return insets;
    }

    private void injectSafeAreaCSS(int top, int right, int bottom, int left) {
        Activity activity = this.cordova.getActivity();
        float density = activity.getResources().getDisplayMetrics().density;
        int topPx = (int) (top / density);
        int rightPx = (int) (right / density);
        int bottomPx = (int) (bottom / density);
        int leftPx = (int) (left / density);

        String js = getCssInsetJsString("top", topPx)
                + getCssInsetJsString("right", rightPx)
                + getCssInsetJsString("bottom", bottomPx)
                + getCssInsetJsString("left", leftPx);
        activity.runOnUiThread(() -> webView.loadUrl("javascript:" + js));
    }

    private String getCssInsetJsString(String inset, int size) {
        return "document.documentElement.style.setProperty('--safe-area-inset-" + inset + "', '" + size + "px');";
    }

    private void initialSetup() {
        Activity activity = this.cordova.getActivity();
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        WindowInsets currentInsets = decorView.getRootWindowInsets();
        Log.d("SystemBars", "SystemBars ready");
        if (currentInsets != null) {
            Log.d("SystemBars", "Insets have been injected");
            setupSafeAreaInsets(currentInsets);
        }

        activity.runOnUiThread(() -> {
            if (Build.VERSION.SDK_INT < 30) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }

            WindowCompat.setDecorFitsSystemWindows(window, false);
            if (Build.VERSION.SDK_INT <= 35) {
                int uiOptions = decorView.getSystemUiVisibility();
                uiOptions = uiOptions | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                window.getDecorView().setSystemUiVisibility(uiOptions);

                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);

            }
        });

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("setHidden")) {
            boolean hideInset = args.getBoolean(0);
            String inset = args.isNull(1) ? null : args.getString(1);
            this.setHidden(hideInset, inset);
            callbackContext.success("setHidden executed");
            return true;
        } else if (action.equals("setStyle")) {
            String style = args.getString(0).toLowerCase();
            String inset = args.isNull(1) ? null : args.getString(1);
            this.setStyle(style, inset);
            callbackContext.success("setStyle executed");
            return true;
        } else if (action.equals("_ready")) {
            initialSetup();
            return true;
        }
        return false;
    }

    private void setHidden(boolean hideInset, String inset) {
        Activity activity = this.cordova.getActivity();
        Window window = activity.getWindow();

        activity.runOnUiThread(() -> {
            WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window,
                    window.getDecorView());

            int insetTypes = 0;

            if (inset == null) {
                insetTypes = WindowInsetsCompat.Type.systemBars();
            } else if ("top".equalsIgnoreCase(inset)) {
                insetTypes = WindowInsetsCompat.Type.statusBars();
            } else if ("bottom".equalsIgnoreCase(inset)) {
                insetTypes = WindowInsetsCompat.Type.navigationBars();
            }

            if (hideInset) {
                insetsController.hide(insetTypes);
            } else {
                insetsController.show(insetTypes);
            }

            if ("left".equalsIgnoreCase(inset) || "right".equalsIgnoreCase(inset)) {
                Log.d("SystemBars", "Inset '" + inset + "' not yet supported on Android");
            }
        });
    }

    private void setStyle(String style, String inset) {
        // Android sets content the opposite of statusBarAppearance, so this is flipped
        // for that reason to make the api consistent vs ios
        currentStyle = style;
        boolean setIconsLight = "dark".equalsIgnoreCase(style);
        Activity activity = this.cordova.getActivity();
        Window window = activity.getWindow();

        activity.runOnUiThread(() -> {
            WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window,
                    window.getDecorView());
            if (inset == null || "top".equalsIgnoreCase(inset)) {
                insetsController.setAppearanceLightStatusBars(setIconsLight);
            }

            if (inset == null || "bottom".equalsIgnoreCase(inset)) {
                insetsController.setAppearanceLightNavigationBars(setIconsLight);
            }
        });
    }
}
