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

package com.aurora.adroid.download;

import android.content.Context;

import com.aurora.adroid.Constants;
import com.aurora.adroid.util.Util;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class DownloadManager {

    private static volatile DownloadManager instance;
    private static Fetch fetch;

    public DownloadManager() {
        if (instance != null) {
            throw new RuntimeException("Use get() method to get the single instance of RxBus");
        }
    }

    public static Fetch getFetchInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                    fetch = getFetch(context);
                }
            }
        }
        return fetch;
    }

    private static Fetch getFetch(Context context) {
        FetchConfiguration.Builder fetchConfiguration = new FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(Util.getActiveDownloadCount(context))
                .setHttpDownloader(new OkHttpDownloader(getOkHttpClient(context), Util.getDownloadStrategy(context)))
                .setNamespace(Constants.TAG)
                .enableLogging(Util.isFetchDebugEnabled(context))
                .enableHashCheck(true)
                .enableFileExistChecks(true)
                .enableRetryOnNetworkGain(true)
                .enableAutoStart(true)
                .setAutoRetryMaxAttempts(3)
                .setProgressReportingInterval(3000)
                .setInternetAccessUrlCheck("https://ddg.co");
        return Fetch.Impl.getInstance(fetchConfiguration.build());
    }

    private static OkHttpClient getOkHttpClient(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .writeTimeout(6, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies) {
                        cookieStore.put(url, cookies);
                    }

                    @NotNull
                    @Override
                    public List<Cookie> loadForRequest(@NotNull HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                });
        if (Util.isNetworkProxyEnabled(context))
            builder.proxy(Util.getNetworkProxy(context));
        return builder.build();
    }
}
