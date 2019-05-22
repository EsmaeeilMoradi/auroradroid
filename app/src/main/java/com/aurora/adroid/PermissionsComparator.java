/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Droid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora Droid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Droid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aurora.adroid;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.aurora.adroid.model.App;
import com.aurora.adroid.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PermissionsComparator {

    private Context context;

    public PermissionsComparator(Context context) {
        this.context = context;
    }

    public boolean isSame(App app) {
        Log.i("Checking %s", app.getPackageName());
        Set<String> oldPermissions = getOldPermissions(app.getPackageName());
        if (null == oldPermissions) {
            return true;
        }
        Set<String> newPermissions = new HashSet<>(app.getPermissions());
        newPermissions.removeAll(oldPermissions);
        Log.i(newPermissions.isEmpty()
                ? app.getPackageName() + " requests no new permissions"
                : app.getPackageName() + " requests new permissions: " + TextUtils.join(", ", newPermissions));
        return newPermissions.isEmpty();
    }

    private Set<String> getOldPermissions(String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            return new HashSet<>(Arrays.asList(
                    null == pi.requestedPermissions
                            ? new String[0]
                            : pi.requestedPermissions
            ));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("Package " + packageName + " doesn't seem to be installed");
        }
        return null;
    }
}
