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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;

public class Utils {

    public static final String defaultSortOrder = "position";
    public static final String nameSortOrder = "webcam_name COLLATE UNICODE ASC";
    public static final String folderWCVPath = Environment.getExternalStorageDirectory() + "/WebCamViewer/";
    public static final String folderWCVPathTmp = folderWCVPath + "Tmp/";
    public static final String extension = ".wcv";
    public static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss, zzzz";

    public static final String YAV = "http://www.yetanotherview.cz/";

    public static final String HELP_PRESENTATION_ = "ZCMKV54rQVM";
    public static final String HELP_MANUALLY_ADDING = "tAwHqEK4IpI";
    public static final String HELP_ANALYZER_TOOL = "j6wa-xpoBYs";
    public static final String HELP_CATEGORY_MANAGEMENT = "SZGEwhPltJk";
    public static final String HELP_BACKUP_RESTORE = "qVdhrAGEmp4";

    public static final String SUPPORT_LIBRARIES_VERSION = "23.0.1";
    public static final String GLIDE_VERSION = "3.6.1";
    public static final String LIB_VLC_VERSION = "3.0.0-1.5.90";
    public static final String MATERIAL_DIALOGS_VERSION = "0.8.1.0";
    public static final String GOOGLE_GSON_VERSION = "2.3.1";
    public static final String JSOUP_VERSION = "1.8.2";
    public static final String MAPBOX_VERSION = "0.7.4"; //ToDo: Replace
    public static final String FAB_VERSION = "1.6.1"; //ToDo

    private static final String CORE = "https://api.yetanotherview.cz/api/v4/";
    private static final String PHP = ".php";
    public static final String JSON_FILE_A3L1Y8QFXHPG = CORE + "a3L1Y8QfxhpG" + PHP;
    public static final String JSON_FILE_NPOEMOWQCPPO = CORE + "NPOEmowqCppo" + PHP;
    public static final String JSON_FILE_OZOBFUTXJVXO = CORE + "OZObFutXjVxo" + PHP;
    public static final String JSON_FILE_JDWUFOYXLOYY = CORE + "jDWUFOyxlOyY" + PHP;

    public static String basicAuth = "sample:sample";

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
    public static String getCustomDateString(String pattern) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(c.getTime());
    }

    /**
     * Get current used pattern in system
     * @return Time pattern as String
     */
    public static String getPattern(Context context) {
        if (!DateFormat.is24HourFormat(context)) {
            return "K:mm a";
        } else return "HH:mm:ss";
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
     * Get All files from given Directory with wcv extension
     * @param DirectoryPath Directory patch
     * @return An ArrayList of files only with wcv extension
     */
    public static ArrayList<File> getFiles(String DirectoryPath) {
        File directory = new File(DirectoryPath);
        if (!directory.mkdirs()) {
            Log.i("Info", "Folder exist.");
        }
        File[] files = directory.listFiles();
        ArrayList<File> arrayList = new ArrayList<>();
        for (File file : files) {
            if (!file.isDirectory() && (file.getAbsolutePath().endsWith(extension))) {
                arrayList.add(file);
            }
        }
        return arrayList;
    }

    /**
     * Get the resources Id.
     */
    public static int getResId(String resourceName, Class<?> c) {
        try {
            // AAPT hack
            if (resourceName.equals("do")) {
                resourceName = "doo";
            }
            Field idField = c.getDeclaredField(resourceName);
            return idField.getInt(idField);
        } catch (Exception e) {
            return R.drawable.unknown;
        }
    }

    /**
     * Round double
     */
    private static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Clear Cache and Tmp folder
     */
    public static void deleteCache(Context context) {

        PackageManager pm = context.getPackageManager();
        // Get all methods on the PackageManager
        Method[] methods = pm.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals("freeStorageAndNotify")) {
                // Found the method I want to use
                try {
                    m.invoke(pm, Long.MAX_VALUE, null);
                } catch (Exception e) {
                    Log.d("", "Method invocation failed. Could be a permission problem");
                }
                break;
            }
        }

        deleteTmpCache();
    }

    /**
     * Clear Tmp folder
     */
    public static void deleteTmpCache() {
        try {
            File tmpFolder = new File(folderWCVPathTmp);
            if (tmpFolder.isDirectory()) {
                String[] children = tmpFolder.list();
                for (String aChildren : children) {
                    if (!new File(tmpFolder, aChildren).delete()) {
                        Log.d("Error", "File cannot be deleted.");
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Get last know location
     */
    public static KnownLocation getLastKnownLocation(Activity activity) {
        KnownLocation location;

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if (bestProvider == null) {
            bestProvider = LocationManager.NETWORK_PROVIDER;
        }

        Location mLastLocation = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.

                // Should we show an explanation?
                //if (shouldShowRequestPermissionRationale(
                //        Manifest.permission.READ_CONTACTS)) {
                    // Explain to the user why we need to read the contacts
                //}

                //activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                //        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant
                //return;
            }
            else mLastLocation = locationManager.getLastKnownLocation(bestProvider);
        }
        else mLastLocation = locationManager.getLastKnownLocation(bestProvider);

        if (mLastLocation != null) {
                location =  new KnownLocation(Utils.roundDouble(mLastLocation.getLatitude(), 6),
                        Utils.roundDouble(mLastLocation.getLongitude(), 6), false);
                Log.i("KnownLocation", String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude()));
        } else {
            Log.i("KnownLocation", "No last location detected");
            location = new KnownLocation(0,0, true);
        }

        return location;
    }

    /**
     * Calculate image height
     */
    public static int getImageHeight(Context context, int layoutId) {
        int minHeight = 0;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int mWidth = size.x;

        if (layoutId == 1) {
            minHeight = (int) (mWidth * 0.67);
        }
        else if (layoutId == 2) {
            minHeight = (int) ((mWidth * 0.67) / 2);
        }
        else if (layoutId == 3) {
            minHeight = (int) ((mWidth * 0.67) / 3);
        }

        return minHeight;
    }

    /**
     * Human readable byte count
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Test if Array of Strings contain given text
     */
    public static boolean stringContainsItem(String inputString, String[] items) {
        for (String item : items) {
            if (inputString.contains(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get color without multiple deprecations
     */
    public static int getColor(Resources res, int id)
            throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(id, null);
        } else {
            return res.getColor(id);
        }
    }
}
