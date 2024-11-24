/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intelligence.browser.downloads;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.intelligence.browser.R;
import com.intelligence.browser.base.CombinedBookmarksCallbacks;
import com.intelligence.browser.ui.media.PhotoViewPagerActivity;
import com.intelligence.browser.view.moplbutton.MorphAction;
import com.intelligence.browser.view.moplbutton.MorphingButton;

public class BrowserDownloadPage extends Fragment implements DownloadedFragment.OnDataChangeListener{
    private DownloadingFragment mDownloadingFragment;
    private DownloadedFragment mDownloadedFragment;
    private View mNoDownLoadDataLayout;
    private MorphingButton mMorphClear;

    public static final int DOWNLOADING = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browser_download_page, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {

        mNoDownLoadDataLayout = view.findViewById(R.id.nodownload);
        mMorphClear = view.findViewById(R.id.morph_clear);

        mMorphClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMorphButtonClicked(mMorphClear);
            }
        });

        MorphAction.morphToSquare(getActivity(), mMorphClear, 0, R.string.remove_items_all);

        FragmentManager fragmentManager = getChildFragmentManager();
        mDownloadingFragment = (DownloadingFragment) fragmentManager.findFragmentById(R.id.browser_downloading_fragment);
        mDownloadedFragment = (DownloadedFragment) fragmentManager.findFragmentById(R.id.browser_downloaded_fragment);
        mDownloadedFragment.setOnDataChangeListener(this);

    }

    private void onMorphButtonClicked(final MorphingButton btnMorph) {
        deleteAllData();
        onSelectNull(true);
    }

    public void onSelectNull(Boolean select) {
        MorphAction.morphMoveOut(mMorphClear, MorphAction.integer(getActivity(), R.integer
                .mb_animation));
    }

    @Override
    public void onDataChangeListener() {
        if ((mDownloadingFragment != null && !mDownloadingFragment.isEmpty()) || (mDownloadedFragment != null && !mDownloadedFragment.isEmpty())) {
            if(mNoDownLoadDataLayout != null) {
                mNoDownLoadDataLayout.setVisibility(View.GONE);
            }
        } else {
            if(mNoDownLoadDataLayout != null) {
                mNoDownLoadDataLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private CombinedBookmarksCallbacks mDownloadBack;
    public void setDownloadBack(CombinedBookmarksCallbacks downloadEditCall){
        mDownloadBack = downloadEditCall;
    }

    @Override
    public void onEditMode(boolean isEdit) {
        try {
            if (mDownloadBack != null) {
                mDownloadBack.onBookmarkEditMode(isEdit);
            }
            if (isEdit) {
                mMorphClear.setVisibility(View.VISIBLE);
                FragmentActivity activity = (getActivity());
                MorphAction.morphMove(mMorphClear, MorphAction.integer(activity, R.integer
                        .mb_animation));
            } else {
                onSelectNull(true);
            }
        }catch (Exception e){
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PhotoViewPagerActivity.FINISH_CODE && data != null && resultCode == Activity.RESULT_OK){
            String[] url = data.getStringArrayExtra(PhotoViewPagerActivity.EXTRA_ALL);
            deleteFile(url);
        }
    }


    public void deleteFile(String[] url) {
        if (mDownloadedFragment != null) {
            mDownloadedFragment.deleteFile(url);
        }
    }

    public void deleteAllData() {
        if (mDownloadingFragment != null) {
        }

        if (mDownloadedFragment != null){
            mDownloadedFragment.deleteSelectItem();
        }
    }



    public void selectAll(){
        if(mDownloadedFragment != null){
            mDownloadedFragment.selectAll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    public boolean exitEditMode(){
        if(mDownloadedFragment != null){
            return mDownloadedFragment.exitEditMode();
        }
        return false;
    }

}
