/*
* ******************************************************************************
* Copyright (c) 2013-2015 Tomas Valenta.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* *****************************************************************************
*/

package cz.yetanotherview.webcamviewer.app.help;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.HelpAdapter;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.helper.YouTubeIntent;
import cz.yetanotherview.webcamviewer.app.model.HelpItem;

public class HelpActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private HelpAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        initHomeButton();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.helpRecyclerView);
        List<HelpItem> helpData = new ArrayList<>();
        helpData.add(new HelpItem(getString(R.string.presentation), R.drawable.help_preview_0, Utils.HELP_PRESENTATION_));
        helpData.add(new HelpItem(getString(R.string.manually_adding),R.drawable.help_preview_1, Utils.HELP_MANUALLY_ADDING));

        mAdapter = new HelpAdapter(helpData);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter.setClickListener(new HelpAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                HelpItem helpItem = (HelpItem) mAdapter.getItem(position);
                new YouTubeIntent(HelpActivity.this, helpItem.getVideoUrl()).open();
            }
        });
    }

    private void initHomeButton() {
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}