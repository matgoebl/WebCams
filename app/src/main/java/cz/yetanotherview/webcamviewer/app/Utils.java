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

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Utils {

    public static String folderWCVPath = Environment.getExternalStorageDirectory() + "/WebCamViewer/";
    public static String folderWCVPathTmp = folderWCVPath + "/Tmp/";
    public static String extension = ".wcv";
    public static String oldExtension = ".db";

    /**
     * Get current date
     * @return Date
     */
    public static long getDate() {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis();
    }

    /**
     * Get current date based on location
     * @return Date based on location
     */
    public static String getDateString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat();
        return df.format(c.getTime());
    }

    /**
     * Get current date for files in specific format
     * @return Date in specific format
     */
    public static String getCustomDateString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH.mm_d.M.yy");
        return df.format(c.getTime());
    }

    /**
     * Return String with Stripped Accents
     * @param name Input String
     * @return Stripped String
     */
    public static String getNameStrippedAccents(String name) {
        String normalizedName = name;
        normalizedName = Normalizer.normalize(normalizedName, Normalizer.Form.NFD);
        normalizedName = normalizedName.replaceAll("[^\\p{ASCII}]", "");
        return normalizedName;
    }

    /**
     * Get All files from given Directory
     * @param DirectoryPath Directory patch
     * @return An array of files
     */
    public static File[] getFiles(String DirectoryPath) {
        File f = new File(DirectoryPath);
        f.mkdirs();
        return f.listFiles();
    }

    /**
     * Get file names
     * @param file An array list of files
     * @return An array list of strings file names
     */
    public static ArrayList<String> getFileNames(File[] file){
        ArrayList<String> arrayFiles = new ArrayList<>();
        if (file.length == 0)
            return null;
        else {
            for (File aFile : file) {
                if (aFile.isFile()) {
                    arrayFiles.add(aFile.getName());
                }
            }
        }
        return arrayFiles;
    }

    /**
     * Remove old database file
     * @param fileDB old file
     * @return statement
     */
    public static boolean removeDB(File fileDB) {
        Log.d("", "Delete is running...");
        Log.d("", String.valueOf(fileDB));
        return fileDB.delete();
    }

    /**
     * Clean backup folder.
     */
    public static void cleanBackupFolder() {
        File backupFolder = new File(folderWCVPath);
        if (backupFolder.isDirectory()) {
            String[] children = backupFolder.list();
            for (String aChildren : children) {
                new File(backupFolder, aChildren).delete();
            }
        }
    }

    /**
     * Get the resources Id.
     */
    public static int getResId(String resourceName, Class<?> c) {

        List<String> us_states = Arrays.asList("alabama", "alaska", "arizona", "arkansas", "california",
                "colorado", "connecticut", "delaware", "florida", "georgia", "hawaii", "idaho", "illinois",
                "indiana", "iowa", "kansas", "kentucky", "louisiana", "maine", "maryland", "massachusetts",
                "michigan", "minnesota", "mississippi", "missouri", "montana", "nebraska", "nevada",
                "new_hampshire", "new_jersey", "new_mexico", "new_york", "north_carolina", "north_dakota",
                "ohio", "oklahoma", "oregon", "pennsylvania", "rhode_island", "south_carolina",
                "south_dakota", "tennessee", "texas", "utah", "vermont", "virginia", "washington",
                "washington_dc", "west_virginia", "wisconsin", "wyoming");
        boolean contains = us_states.contains(resourceName);

            try {
                int resId;
                if (contains) {
                    resId = R.drawable.united_states;
                }
                else {
                    Field idField = c.getDeclaredField(resourceName);
                    resId = idField.getInt(idField);
                }
                return resId;

            } catch (Exception e) {
                return R.drawable.unknown;
            }

    }
}
