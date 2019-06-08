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

package com.aurora.adroid.manager;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.util.PrefUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class FavouriteListManager {

    private Context context;
    private ArrayList<String> favouriteList;

    public FavouriteListManager(Context context) {
        this.context = context;
        favouriteList = PrefUtil.getListString(context, Constants.PREFERENCE_FAVOURITE_LIST);
    }

    public boolean add(String s) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(s);
        boolean result = addAll(arrayList);
        save();
        return result;
    }

    public boolean addAll(ArrayList<String> arrayList) {
        boolean result = favouriteList.addAll(arrayList);
        Set<String> mAppSet = new HashSet<>(favouriteList);
        favouriteList.clear();
        favouriteList.addAll(mAppSet);
        save();
        return result;
    }

    public ArrayList<String> get() {
        return favouriteList;
    }

    public boolean contains(String packageName) {
        return favouriteList.contains(packageName);
    }

    public boolean remove(String packageName) {
        boolean result = favouriteList.remove(packageName);
        save();
        return result;
    }

    private void save() {
        PrefUtil.putListString(context, Constants.PREFERENCE_FAVOURITE_LIST, favouriteList);
    }
}
