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

package cz.yetanotherview.webcamviewer.app.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.View;

public class ImmersiveMode {

    private static final String TAG = "Immersive mode";

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void goFullScreen(Activity activity) {

        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }
}
