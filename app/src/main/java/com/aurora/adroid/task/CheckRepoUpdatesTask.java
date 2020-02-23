package com.aurora.adroid.task;

import android.content.Context;
import android.content.ContextWrapper;

import com.aurora.adroid.AuroraApplication;
import com.aurora.adroid.Constants;
import com.aurora.adroid.event.LogEvent;
import com.aurora.adroid.model.RepoHeader;
import com.aurora.adroid.model.RepoRequest;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.PrefUtil;
import com.aurora.adroid.util.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Response;

public class CheckRepoUpdatesTask extends ContextWrapper {

    private Context context;
    private List<RepoRequest> filteredList = new ArrayList<>();
    private List<RepoHeader> savedRepoHeaderList;
    private List<RepoHeader> newRepoHeaderList;

    public CheckRepoUpdatesTask(Context context) {
        super(context);
        this.context = context;
        this.savedRepoHeaderList = fetchRepoHeadersFromCache();
        this.newRepoHeaderList = new ArrayList<>();
    }

    public static void removeRepoHeader(Context context, String repoID) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<RepoHeader>>() {
        }.getType();
        String jsonString = PrefUtil.getString(context, Constants.PREFERENCE_REPO_HEADERS);
        List<RepoHeader> repoHeaderList = gson.fromJson(jsonString, type);
        List<RepoHeader> filteredHeaderList = new ArrayList<>();
        for (RepoHeader repoHeader : repoHeaderList) {
            if (repoHeader.getRepoId().equals(repoID))
                continue;
            else
                filteredHeaderList.add(repoHeader);
        }
        String filteredString = gson.toJson(filteredHeaderList);
        PrefUtil.putString(context, Constants.PREFERENCE_REPO_HEADERS, filteredString);
    }

    public List<RepoRequest> filterList(List<RepoRequest> requestList) {
        final OkHttpClient client = new OkHttpClient();
        for (RepoRequest request : requestList) {
            AuroraApplication.rxNotify(new LogEvent("Checking update for " + request.getRepoName()));
            RepoHeader repoHeader = getRepoHeader(request.getRepoId());
            okhttp3.Request okhttpRequest = new okhttp3.Request.Builder().url(request.getUrl()).head().build();
            try (Response response = client.newCall(okhttpRequest).execute()) {
                Long lastModified = Util.getMilliFromDate(response.header("Last-Modified"), Calendar.getInstance().getTimeInMillis());
                if (repoHeader == null) {
                    repoHeader = new RepoHeader();
                    repoHeader.setRepoId(request.getRepoId());
                    repoHeader.setLastModified(lastModified);
                    filteredList.add(request);
                } else {
                    if (repoHeader.getLastModified() < lastModified) {
                        filteredList.add(request);
                    }
                    repoHeader.setLastModified(lastModified);
                }
                newRepoHeaderList.add(repoHeader);
            } catch (Exception e) {
                AuroraApplication.rxNotify(new LogEvent("Unable to reach " + request.getRepoName()));
                Log.e("Unable to reach %s", request.getRepoUrl());
            }
        }
        saveRepoHeadersToCache(newRepoHeaderList);
        return filteredList;
    }

    private RepoHeader getRepoHeader(String repoId) {
        for (RepoHeader repoHeader : savedRepoHeaderList)
            if (repoHeader.getRepoId().equals(repoId))
                return repoHeader;
        return null;
    }

    private void saveRepoHeadersToCache(List<RepoHeader> appList) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(appList);
        PrefUtil.putString(context, Constants.PREFERENCE_REPO_HEADERS, jsonString);
    }

    private List<RepoHeader> fetchRepoHeadersFromCache() {
        Gson gson = new Gson();
        Type type = new TypeToken<List<RepoHeader>>() {
        }.getType();
        String jsonString = PrefUtil.getString(context, Constants.PREFERENCE_REPO_HEADERS);
        List<RepoHeader> repoHeaderList = gson.fromJson(jsonString, type);
        if (repoHeaderList == null || repoHeaderList.isEmpty())
            return new ArrayList<>();
        else
            return repoHeaderList;
    }
}
