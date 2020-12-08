package com.ominous.tylerutils.util;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public class WindowUtils {

    public static void setLightNavBar(Window w, boolean enable) {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = w.getInsetsController();

            if (controller != null) {
                controller.setSystemBarsAppearance(
                        enable ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);

                controller.setSystemBarsAppearance(
                        enable ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else if (Build.VERSION.SDK_INT >= 26) {
            int flags = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

            w.getDecorView().setSystemUiVisibility(enable ?
                    w.getDecorView().getSystemUiVisibility() | flags :
                    w.getDecorView().getSystemUiVisibility() & ~flags);
        }
    }

    public static void setImmersive(Window w, boolean enable) {

        if (Build.VERSION.SDK_INT >= 30) {
            w.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = w.getInsetsController();

            if (controller != null) {
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_SWIPE);

                if (enable) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                } else {
                    controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                }
            }
        } else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    (Build.VERSION.SDK_INT >= 19 ? View.SYSTEM_UI_FLAG_IMMERSIVE : 0);

            w.getDecorView().setSystemUiVisibility(enable ?
                    w.getDecorView().getSystemUiVisibility() | flags :
                    w.getDecorView().getSystemUiVisibility() & ~flags);
        }
    }
}
