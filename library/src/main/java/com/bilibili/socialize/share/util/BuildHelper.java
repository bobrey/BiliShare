/*
 * Copyright (c) 2015. BiliBili Inc.
 */

package com.bilibili.socialize.share.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.List;

/**
 * @author Jungly
 * @email jungly.ik@gmail.com
 * @since 2016/4/11
 */
public class BuildHelper {
    public static int HONEYCOMB = 11;

    public static boolean isApi11_HoneyCombOrLater() {
        return getSDKVersion() >= HONEYCOMB;
    }

    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean isClientInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pkgInfos = packageManager.getInstalledPackages(0);
        if (pkgInfos == null || pkgInfos.isEmpty()) {
            return false;
        }

        for (PackageInfo packageInfo : pkgInfos) {
            if (packageName.equalsIgnoreCase(packageInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

}
