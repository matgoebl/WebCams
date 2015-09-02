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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DeleteAllWebCams {

    public static void execute(Context context, boolean alsoCategories) {
        DatabaseHelper db = new DatabaseHelper(context);
        db.deleteAllWebCams(alsoCategories);
        db.closeDB();

        saveToPref(context);
    }

    private static void saveToPref(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("pref_last_fetch_popular", 0);
        editor.putLong("pref_last_fetch_latest", 0);
        editor.putInt("pref_last_category_pos", 0);
        editor.apply();
    }
}
