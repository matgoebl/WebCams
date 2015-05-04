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

package cz.yetanotherview.webcamviewer.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class HelpActivity extends AppCompatActivity {

    private Intent browserIntent;

    private static final String mainVideo = "Xcp0j2vwbxI";
    private static final String manuallyAdding = "liYtvXE0JTI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_help);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void openMainVideo(View view) {
        try {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + mainVideo));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(browserIntent);
        } catch(ActivityNotFoundException e) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://youtu.be/" + mainVideo));
            startActivity(browserIntent);
        }
    }

    public void openManuallyAdding(View view) {
        try {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + manuallyAdding));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(browserIntent);
        } catch(ActivityNotFoundException e) {
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://youtu.be/" + manuallyAdding));
            startActivity(browserIntent);
        }
    }
}