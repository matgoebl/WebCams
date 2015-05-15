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

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DeleteAllWebCams {

    // Object for intrinsic lock
    public static final Object sDataLock = new Object();

    public static void execute(Context context, boolean alsoCategories) {

        DatabaseHelper db = new DatabaseHelper(context);

        synchronized (DeleteAllWebCams.sDataLock) {
            db.deleteAllWebCams(alsoCategories);
            db.closeDB();
        }
        BackupManager backupManager = new BackupManager(context);
        backupManager.dataChanged();

        saveToPref(context);
    }

    private static void saveToPref(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("pref_last_fetch_popular", 0);
        editor.putLong("pref_last_fetch_latest", 0);
        editor.apply();
    }
}
