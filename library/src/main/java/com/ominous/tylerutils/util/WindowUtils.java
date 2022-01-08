/*
 *     Copyright 2020 - 2022 Tyler Williamson
 *
 *     This file is part of TylerUtils.
 *
 *     TylerUtils is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     TylerUtils is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

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
            w.setDecorFitsSystemWindows(!enable);
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
