/*
 * Aurora Droid
 * Copyright (C) 2019-20, Rahul Kumar Patel <whyorean@gmail.com>
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
 *
 */

package com.aurora.adroid.ui.fragment.preference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.aurora.adroid.R;
import com.aurora.adroid.manager.BlacklistManager;
import com.aurora.adroid.model.items.BlacklistItem;
import com.aurora.adroid.ui.view.ViewFlipper2;
import com.aurora.adroid.util.Log;
import com.aurora.adroid.util.ViewUtil;
import com.aurora.adroid.viewmodel.BlackListedAppsModel;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class BlacklistFragment extends Fragment {

    @BindView(R.id.viewFlipper)
    ViewFlipper2 viewFlipper;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeLayout;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.btn_clear_all)
    Button btnClearAll;
    @BindView(R.id.txt_blacklist)
    TextView txtBlacklist;

    private BlacklistManager blacklistManager;
    private BlackListedAppsModel model;
    private FastItemAdapter<BlacklistItem> fastItemAdapter;
    private SelectExtension<BlacklistItem> selectExtension;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_blacklist, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupRecycler();

        //Fetch Blacklisted Items
        model = new ViewModelProvider(this).get(BlackListedAppsModel.class);
        model.getBlacklistedItems().observe(getViewLifecycleOwner(), blacklistItems -> {
            final List<BlacklistItem> sortedList = sortBlackListedApps(blacklistItems);
            fastItemAdapter.add(sortedList);
            updatePageData();
            updateCount();
            swipeLayout.setRefreshing(false);
        });

        swipeLayout.setOnRefreshListener(() -> model.fetchBlackListedApps());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        swipeLayout.setRefreshing(false);
        super.onPause();
    }

    @OnClick(R.id.btn_clear_all)
    public void clearAll() {
        blacklistManager.clear();
        fastItemAdapter.notifyAdapterDataSetChanged();
        updateCount();
    }

    /*
     * Sorts the Blacklisted Items in order Blacklisted -> Whitelisted (Alphabetically)
     */
    private List<BlacklistItem> sortBlackListedApps(List<BlacklistItem> blacklistItems) {
        final List<BlacklistItem> blackListedItems = new ArrayList<>();
        final List<BlacklistItem> whiteListedItems = new ArrayList<>();
        final List<BlacklistItem> sortedList = new ArrayList<>();

        //Sort Apps by Names
        Collections.sort(blacklistItems, (blacklistItem1, blacklistItem2) ->
                blacklistItem1.getApp().getName()
                        .compareToIgnoreCase(blacklistItem2.getApp().getName()));

        //Sort Apps by blacklist status
        for (BlacklistItem blacklistItem : blacklistItems) {
            if (blacklistManager.isBlacklisted(blacklistItem.getApp().getPackageName()))
                blackListedItems.add(blacklistItem);
            else
                whiteListedItems.add(blacklistItem);
        }

        sortedList.addAll(blackListedItems);
        sortedList.addAll(whiteListedItems);
        return sortedList;
    }

    private void updatePageData() {
        if (fastItemAdapter != null && fastItemAdapter.getAdapterItems().size() > 0) {
            viewFlipper.switchState(ViewFlipper2.DATA);
        } else {
            viewFlipper.switchState(ViewFlipper2.EMPTY);
        }
    }

    private void setupRecycler() {
        blacklistManager = new BlacklistManager(requireContext());
        fastItemAdapter = new FastItemAdapter<>();
        selectExtension = new SelectExtension<>(fastItemAdapter);

        fastItemAdapter.setOnClickListener((view, blacklistItemIAdapter, blacklistItem, position) -> false);
        fastItemAdapter.setOnPreClickListener((view, blacklistItemIAdapter, blacklistItem, position) -> true);

        fastItemAdapter.addExtension(selectExtension);
        fastItemAdapter.addEventHook(new BlacklistItem.CheckBoxClickEvent());

        selectExtension.setMultiSelect(true);
        selectExtension.setSelectionListener((item, selected) -> {
            if (blacklistManager.isBlacklisted(item.getApp().getPackageName())) {
                blacklistManager.removeFromBlacklist(item.getApp().getPackageName());
                Log.d("Whitelisted : %s", item.getApp().getName());
            } else {
                blacklistManager.addToBlacklist(item.getApp().getPackageName());
                Log.d("Blacklisted : %s", item.getApp().getName());
            }
            updateCount();
        });

        recyclerView.setAdapter(fastItemAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void updateCount() {
        final int count = blacklistManager.getBlacklistedPackages().size();
        final String txtCount = StringUtils.joinWith(" : ", getString(R.string.list_blacklist), count);
        txtBlacklist.setText(count > 0 ? txtCount : getString(R.string.list_blacklist_none));
        ViewUtil.setVisibility(btnClearAll, count > 0, true);
    }
}
