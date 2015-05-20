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

package cz.yetanotherview.webcamviewer.app.listener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class SimpleIntentOnClickListener implements View.OnClickListener {

    Activity activity;
    String uriString;

    public SimpleIntentOnClickListener(Activity activity, String uriString) {
        this.activity = activity;
        this.uriString = uriString;
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse(uriString));
        activity.startActivity(i);
    }
}