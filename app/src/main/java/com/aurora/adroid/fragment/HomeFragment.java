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

package com.aurora.adroid.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurora.adroid.activity.AuroraActivity;
import com.aurora.adroid.R;
import com.aurora.adroid.adapter.CategoriesAdapter;
import com.aurora.adroid.adapter.ClusterAppsAdapter;
import com.aurora.adroid.adapter.LatestUpdatedAdapter;
import com.aurora.adroid.task.CategoriesTask;
import com.aurora.adroid.task.FetchAppsTask;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    @BindView(R.id.recycler_cat)
    RecyclerView recyclerViewCat;
    @BindView(R.id.recycler_latest)
    RecyclerView recyclerViewLatest;
    @BindView(R.id.recycler_new)
    RecyclerView recyclerViewNew;

    private Context context;
    private BottomNavigationView bottomNavigationView;
    private CompositeDisposable disposable = new CompositeDisposable();

    private CategoriesAdapter categoriesAdapter;
    private ClusterAppsAdapter clusterAppsAdapter;
    private LatestUpdatedAdapter latestUpdatedAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCategories();
        setupUpdatedApps();
        setupNewApps();
        if (getActivity() instanceof AuroraActivity)
            bottomNavigationView = ((AuroraActivity) getActivity()).getBottomNavigationView();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCategories();
        fetchNewApps();
        fetchLatestApps();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    private void setupCategories() {
        categoriesAdapter = new CategoriesAdapter(context);
        recyclerViewCat.setAdapter(categoriesAdapter);
        recyclerViewCat.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerViewCat.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.anim_slideright));
    }

    private void setupNewApps() {
        clusterAppsAdapter = new ClusterAppsAdapter(context);
        recyclerViewNew.setAdapter(clusterAppsAdapter);
        recyclerViewNew.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerViewNew.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.anim_slideright));
    }

    private void setupUpdatedApps() {
        latestUpdatedAdapter = new LatestUpdatedAdapter(context);
        recyclerViewLatest.setAdapter(latestUpdatedAdapter);
        recyclerViewLatest.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerViewLatest.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(context, R.anim.anim_falldown));
        recyclerViewLatest.setOnFlingListener(new RecyclerView.OnFlingListener() {
            @Override
            public boolean onFling(int velocityX, int velocityY) {
                if (velocityY < 0) {
                    if (bottomNavigationView != null)
                        ViewUtil.showBottomNav(bottomNavigationView, true);
                } else if (velocityY > 0) {
                    if (bottomNavigationView != null)
                        ViewUtil.hideBottomNav(bottomNavigationView, true);
                }
                return false;
            }
        });
    }

    private void fetchCategories() {
        disposable.add(Observable.fromCallable(() -> new CategoriesTask(context)
                .getCategories())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((categoryList) -> {
                    if (!categoryList.isEmpty()) {
                        categoriesAdapter.addData(categoryList);
                    }
                }, err -> {
                    Log.e(err.getMessage());
                    err.printStackTrace();
                }));
    }

    private void fetchNewApps() {
        disposable.add(Observable.fromCallable(() -> new FetchAppsTask(context)
                .getLatestAddedApps(3))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    if (!appList.isEmpty()) {
                        clusterAppsAdapter.addData(appList);
                    }
                }, err -> {
                    Log.e(err.getMessage());
                    err.printStackTrace();
                }));
    }

    private void fetchLatestApps() {
        disposable.add(Observable.fromCallable(() -> new FetchAppsTask(context)
                .getLatestUpdatedApps(1))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((appList) -> {
                    if (!appList.isEmpty()) {
                        latestUpdatedAdapter.addData(appList);
                    }
                }, err -> {
                    Log.e(err.getMessage());
                    err.printStackTrace();
                }));
    }

}
