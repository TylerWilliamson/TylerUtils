/*
 * Copyright 2020 - 2025 Tyler Williamson
 *
 * This file is part of TylerUtils.
 *
 * TylerUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TylerUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

public class ApkUtils {
    public static String getReleaseVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressLint("PackageManagerGetSignatures") //Issue resolved in Android 4.4
    public static String[] getSignatures(Context context) {
        try {
            Signature[] signatures = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
            String[] signatureStrings = new String[signatures.length];

            for (int i = 0, l = signatures.length; i < l; i++) {
                signatureStrings[i] = signatures[i].toCharsString();
            }

            return signatureStrings;
        } catch (PackageManager.NameNotFoundException e) {
            //Will not occur
        }

        return null;
    }
}
