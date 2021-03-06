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

import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat category
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 7;

    // Database Name
    private static final String DATABASE_NAME = "webCamDatabase.db";

    // Table Names
    private static final String TABLE_WEBCAM = "webcams";
    private static final String TABLE_CATEGORY = "categories";
    private static final String TABLE_WEBCAM_CATEGORY = "webcam_categories";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // WEBCAM Table - column names
    private static final String KEY_UNI_ID = "uni_id";
    private static final String KEY_IS_STREAM = "is_stream";
    private static final String KEY_WEBCAM = "webcam_name";
    private static final String KEY_WEBCAM_URL = "webcam_url";
    private static final String KEY_WEBCAM_THUMB_URL = "webcam_thumb_url";
    private static final String KEY_POSITION = "position";
    private static final String KEY_STATUS = "status";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_DATE_MODIFIED = "date_modified";

    // CATEGORY'S Table - column names
    private static final String KEY_CATEGORY_ICON = "category_icon";
    private static final String KEY_CATEGORY_NAME = "category_name";

    // WEBCAM_CATEGORY'S Table - column names
    private static final String KEY_WEBCAM_ID = "webcam_id";
    private static final String KEY_CATEGORY_ID = "category_id";

    // Table Create Statements
    // WebCam table create statement
    private static final String CREATE_TABLE_WEBCAM = "CREATE TABLE IF NOT EXISTS "
            + TABLE_WEBCAM + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_UNI_ID + " INTEGER,"
            + KEY_IS_STREAM + " INTEGER," + KEY_WEBCAM + " TEXT," + KEY_WEBCAM_URL + " TEXT,"
            + KEY_WEBCAM_THUMB_URL + " TEXT,"  + KEY_POSITION + " INTEGER," + KEY_STATUS + " INTEGER,"
            + KEY_LATITUDE + " REAL,"  + KEY_LONGITUDE + " REAL," + KEY_DATE_MODIFIED + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    // Category table create statement
    private static final String CREATE_TABLE_CATEGORY = "CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORY
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CATEGORY_ICON + " TEXT," + KEY_CATEGORY_NAME + " TEXT,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    // WebCam category table create statement
    private static final String CREATE_TABLE_WEBCAM_CATEGORY = "CREATE TABLE IF NOT EXISTS "
            + TABLE_WEBCAM_CATEGORY + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_WEBCAM_ID + " INTEGER," + KEY_CATEGORY_ID + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WEBCAM);
        db.execSQL(CREATE_TABLE_CATEGORY);
        db.execSQL(CREATE_TABLE_WEBCAM_CATEGORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 3:
                    migrateOldTables(db);
                    break;
                case 5:
                    db.execSQL("ALTER TABLE " + TABLE_CATEGORY + " ADD COLUMN " + KEY_CATEGORY_ICON + " TEXT");
                    break;
                case 6:
                    db.execSQL("ALTER TABLE " + TABLE_WEBCAM + " ADD COLUMN " + KEY_DATE_MODIFIED + " INTEGER");
                    break;
                case 7:
                    db.execSQL("ALTER TABLE " + TABLE_WEBCAM + " ADD COLUMN " + KEY_IS_STREAM + " INTEGER");
                    db.execSQL("ALTER TABLE " + TABLE_WEBCAM + " ADD COLUMN " + KEY_WEBCAM_THUMB_URL + " TEXT");
                    break;
            }
            upgradeTo++;
        }
    }

    private void migrateOldTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + "CardCursorTableCategory");

        onCreate(db);

        String oldWebCamTable = "CardCursorTable";
        db.execSQL("INSERT INTO " + TABLE_WEBCAM +"(" + KEY_WEBCAM + "," + KEY_WEBCAM_URL + ")"
                + " SELECT header,thumb FROM " + oldWebCamTable);
        db.execSQL("DROP TABLE IF EXISTS " + oldWebCamTable);
    }

    // ------------------------ "WebCams" table methods ----------------//

    /**
     * Creating a webCam
     */
    public long createWebCam(WebCam webCam, List<Integer> category_ids) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UNI_ID, webCam.getUniId());
        values.put(KEY_IS_STREAM, (webCam.isStream())? 1 : 0);
        values.put(KEY_WEBCAM, webCam.getName());
        values.put(KEY_WEBCAM_URL, webCam.getUrl());
        values.put(KEY_WEBCAM_THUMB_URL, webCam.getThumbUrl());
        values.put(KEY_POSITION, webCam.getPosition());
        values.put(KEY_STATUS, webCam.getStatus());
        values.put(KEY_LATITUDE, webCam.getLatitude());
        values.put(KEY_LONGITUDE, webCam.getLongitude());
        values.put(KEY_DATE_MODIFIED, webCam.getDateModifiedMillisecond());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long webCam_id = db.insert(TABLE_WEBCAM, null, values);

        if (category_ids != null) {
            // insert category_ids
            for (int category_id : category_ids) {
                createWebCamCategory(webCam_id, category_id);
            }
        }

        return webCam_id;
    }

    /**
     * get single WebCam
     */
    public WebCam getWebCam(long webCam_id) {
        WebCam wc = new WebCam();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_WEBCAM + " WHERE "
                + KEY_ID + " = " + webCam_id;

        Log.d(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                wc.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                wc.setUniId(c.getLong(c.getColumnIndex(KEY_UNI_ID)));
                wc.setIsStream(c.getInt(c.getColumnIndex(KEY_IS_STREAM)) == 1);
                wc.setName(c.getString(c.getColumnIndex(KEY_WEBCAM)));
                wc.setUrl(c.getString(c.getColumnIndex(KEY_WEBCAM_URL)));
                wc.setThumbUrl(c.getString(c.getColumnIndex(KEY_WEBCAM_THUMB_URL)));
                wc.setPosition(c.getInt(c.getColumnIndex(KEY_POSITION)));
                wc.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                wc.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
                wc.setDateModified(loadDate(c, c.getColumnIndex(KEY_DATE_MODIFIED)));
                wc.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));
            } while (c.moveToNext());
        }
        c.close();

        return wc;
    }

    /**
     * getting all WebCams
     * */
    public List<WebCam> getAllWebCams(String orderBy) {

        List<WebCam> webCams = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_WEBCAM + " ORDER BY " + orderBy;

        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                WebCam wc = new WebCam();
                wc.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                wc.setUniId(c.getLong(c.getColumnIndex(KEY_UNI_ID)));
                wc.setIsStream(c.getInt(c.getColumnIndex(KEY_IS_STREAM)) == 1);
                wc.setName(c.getString(c.getColumnIndex(KEY_WEBCAM)));
                wc.setUrl(c.getString(c.getColumnIndex(KEY_WEBCAM_URL)));
                wc.setThumbUrl(c.getString(c.getColumnIndex(KEY_WEBCAM_THUMB_URL)));
                wc.setPosition(c.getInt(c.getColumnIndex(KEY_POSITION)));
                wc.setStatus(c.getInt(c.getColumnIndex(KEY_STATUS)));
                wc.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                wc.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
                wc.setDateModified(loadDate(c, c.getColumnIndex(KEY_DATE_MODIFIED)));
                wc.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

                // adding to WebCam list
                webCams.add(wc);
            } while (c.moveToNext());
        }
        c.close();

        return webCams;
    }

    /**
     * getting all WebCams under single category
     * */
    public List<WebCam> getAllWebCamsByCategory(int category_id, String orderBy) {

        List<WebCam> webCams = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_WEBCAM + " td, "
                + TABLE_CATEGORY + " tg, " + TABLE_WEBCAM_CATEGORY + " tt WHERE tg."
                + KEY_ID + " = " + category_id + " AND tg." + KEY_ID
                + " = " + "tt." + KEY_CATEGORY_ID + " AND td." + KEY_ID + " = "
                + "tt." + KEY_WEBCAM_ID + " ORDER BY " + orderBy;

        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                WebCam wc = new WebCam();
                // Care about this hack, zero value instead KEY_ID is important!
                wc.setId(c.getLong(0));
                wc.setUniId(c.getLong(c.getColumnIndex(KEY_UNI_ID)));
                wc.setIsStream(c.getInt(c.getColumnIndex(KEY_IS_STREAM)) == 1);
                wc.setName(c.getString(c.getColumnIndex(KEY_WEBCAM)));
                wc.setUrl(c.getString(c.getColumnIndex(KEY_WEBCAM_URL)));
                wc.setThumbUrl(c.getString(c.getColumnIndex(KEY_WEBCAM_THUMB_URL)));
                wc.setPosition(c.getInt(c.getColumnIndex(KEY_POSITION)));
                wc.setStatus(c.getInt(c.getColumnIndex(KEY_STATUS)));
                wc.setLatitude(c.getDouble(c.getColumnIndex(KEY_LATITUDE)));
                wc.setLongitude(c.getDouble(c.getColumnIndex(KEY_LONGITUDE)));
                wc.setDateModified(loadDate(c, c.getColumnIndex(KEY_DATE_MODIFIED)));
                wc.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

                // adding to WebCam list
                webCams.add(wc);
            } while (c.moveToNext());
        }
        c.close();

        return webCams;
    }

    /**
     * getting WebCam count
     */
    public int getWebCamCount() {
        String countQuery = "SELECT * FROM " + TABLE_WEBCAM;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a WebCam
     */
    public void updateWebCam(WebCam webCam, List<Integer> category_ids) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_IS_STREAM, (webCam.isStream())? 1 : 0);
        values.put(KEY_WEBCAM, webCam.getName());
        values.put(KEY_WEBCAM_URL, webCam.getUrl());
        values.put(KEY_WEBCAM_THUMB_URL, webCam.getThumbUrl());
        values.put(KEY_POSITION, webCam.getPosition());
        values.put(KEY_STATUS, webCam.getStatus());
        values.put(KEY_LATITUDE, webCam.getLatitude());
        values.put(KEY_LONGITUDE, webCam.getLongitude());

        long webCam_id = webCam.getId();

        // updating row
        db.update(TABLE_WEBCAM, values, KEY_ID + " = ?",
                new String[] { String.valueOf(webCam_id) });

        //remove all assigned categories
        deleteWebCamCategory(webCam_id);

        //assign new categories
        if (category_ids != null) {
            // insert category_ids
            for (int category_id : category_ids) {
                createWebCamCategory(webCam_id, category_id);
            }
        }
    }

    /**
     * Updating a WebCam data from Json
     */
    public void updateWebCamFromJson(WebCam currentWebCam, WebCam newData, int category_id) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_IS_STREAM, (newData.isStream())? 1 : 0);
        values.put(KEY_WEBCAM, newData.getName());
        values.put(KEY_WEBCAM_URL, newData.getUrl());
        values.put(KEY_WEBCAM_THUMB_URL, newData.getThumbUrl());
        values.put(KEY_STATUS, newData.getStatus());
        values.put(KEY_LATITUDE, newData.getLatitude());
        values.put(KEY_LONGITUDE, newData.getLongitude());
        values.put(KEY_DATE_MODIFIED, newData.getDateModifiedMillisecond());

        long currentWebCamId = currentWebCam.getId();

        // updating row
        db.update(TABLE_WEBCAM, values, KEY_ID + " = ?",
                new String[] { String.valueOf(currentWebCamId) });

        createWebCamCategory(currentWebCamId, category_id);
    }

    /**
     * Updating a WebCam position
     */
    public void updateWebCamPosition(WebCam webCam) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POSITION, webCam.getPosition());

        // updating row
        db.update(TABLE_WEBCAM, values, KEY_ID + " = ?",
                new String[]{String.valueOf(webCam.getId())});
    }

    /**
     * Deleting a WebCam
     */
    public void deleteWebCam(long webCam_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEBCAM, KEY_ID + " = ?",
                new String[]{String.valueOf(webCam_id)});

        //remove all assigned categories
        deleteWebCamCategory(webCam_id);
    }

    /**
     * Undo Deleting a WebCam
     */
    public void undoDeleteWebCam(WebCam webCam, List<Integer> category_ids) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, webCam.getId());
        values.put(KEY_UNI_ID, webCam.getUniId());
        values.put(KEY_IS_STREAM, (webCam.isStream())? 1 : 0);
        values.put(KEY_WEBCAM, webCam.getName());
        values.put(KEY_WEBCAM_URL, webCam.getUrl());
        values.put(KEY_WEBCAM_THUMB_URL, webCam.getThumbUrl());
        values.put(KEY_POSITION, webCam.getPosition());
        values.put(KEY_STATUS, webCam.getStatus());
        values.put(KEY_LATITUDE, webCam.getLatitude());
        values.put(KEY_LONGITUDE, webCam.getLongitude());
        values.put(KEY_DATE_MODIFIED, webCam.getDateModifiedMillisecond());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long webCam_id = db.insert(TABLE_WEBCAM, null, values);

        if (category_ids != null) {
            // insert category_ids
            for (int category_id : category_ids) {
                createWebCamCategory(webCam_id, category_id);
            }
        }
    }

    /**
     * Delete all WebCams
     */
    public void deleteAllWebCams(boolean alsoCategories) {
        SQLiteDatabase db = this.getWritableDatabase();

        //WebCams table
        db.delete(TABLE_WEBCAM, null, null);

        //WebCam categories table
        db.delete(TABLE_WEBCAM_CATEGORY, null, null);

        //Category table
        if (alsoCategories) {
            db.delete(TABLE_CATEGORY, null, null);
        }
    }

    // ------------------------ "categories" table methods ----------------//

    /**
     * Creating category
     */
    public int createCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_ICON, category.getCategoryIcon());
        values.put(KEY_CATEGORY_NAME, category.getCategoryName());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        return (int) db.insert(TABLE_CATEGORY, null, values);
    }

    /**
     * get single Category
     */
    public Category getCategory(int categoryId) {
        Category category = new Category();

        String selectQuery = "SELECT * FROM " + TABLE_CATEGORY + " WHERE "
                + KEY_ID + " = " + categoryId;

        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                category.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                category.setCategoryIcon(c.getString(c.getColumnIndex(KEY_CATEGORY_ICON)));
                category.setCategoryName(c.getString(c.getColumnIndex(KEY_CATEGORY_NAME)));
            } while (c.moveToNext());
        }
        c.close();

        return category;
    }

    /**
     * getting all categories
     * */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CATEGORY;

        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Category t = new Category();
                t.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                t.setCategoryIcon(c.getString(c.getColumnIndex(KEY_CATEGORY_ICON)));
                t.setCategoryName(c.getString(c.getColumnIndex(KEY_CATEGORY_NAME)));

                // adding to categories list
                categories.add(t);
            } while (c.moveToNext());
        }

        c.close();

        return categories;
    }

    /**
     * Updating a category
     */
    public void updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_ICON, category.getCategoryIcon());
        values.put(KEY_CATEGORY_NAME, category.getCategoryName());

        // updating row
        db.update(TABLE_CATEGORY, values, KEY_ID + " = ?",
                new String[] { String.valueOf(category.getId()) });
    }

    /**
     * Deleting a category
     */
    public void deleteCategory(int categoryId, boolean should_delete_all_category_WebCams) {
        SQLiteDatabase db = this.getWritableDatabase();

        // before deleting category
        // check if WebCams under this category should also be deleted
        if (should_delete_all_category_WebCams) {
            // get all WebCams under this category
            List<WebCam> allCategoryWebCams = getAllWebCamsByCategory(categoryId,"id ASC");

            // working with all WebCams
            for (WebCam webCam : allCategoryWebCams) {

                if (getWebCamCategoriesCount(webCam.getId()) <= 1) {
                    // delete WebCam
                    deleteWebCam(webCam.getId());
                }
                else db.delete(TABLE_WEBCAM_CATEGORY, KEY_CATEGORY_ID + " = ?",
                        new String[] { String.valueOf(categoryId) });
            }
        }

        // now delete the category
        db.delete(TABLE_CATEGORY, KEY_ID + " = ?",
                new String[] { String.valueOf(categoryId) });

        // and WebCam categories table
        db.delete(TABLE_WEBCAM_CATEGORY, KEY_CATEGORY_ID + " = ?",
                new String[] { String.valueOf(categoryId) });
    }

    /**
     * Getting WebCam categories Count
     */
    private int getWebCamCategoriesCount(long webCam_id) {

        String selectQuery = "SELECT * FROM " + TABLE_WEBCAM_CATEGORY + " WHERE "
                + KEY_WEBCAM_ID + " = " + webCam_id;
        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        int count = c.getCount();
        c.close();

        return count;
    }

    /**
     * Getting Category items count
     */
    public int getCategoryItemsCount(int categoryId) {

        String selectQuery = "SELECT * FROM " + TABLE_WEBCAM + " td, "
                + TABLE_CATEGORY + " tg, " + TABLE_WEBCAM_CATEGORY + " tt WHERE tg."
                + KEY_ID + " = " + categoryId + " AND tg." + KEY_ID
                + " = " + "tt." + KEY_CATEGORY_ID + " AND td." + KEY_ID + " = "
                + "tt." + KEY_WEBCAM_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);
        int count = c.getCount();
        c.close();

        return count;
    }

    // ------------------------ "WebCam_categories" table methods ----------------//

    /**
     * Getting WebCam categories Ids
     */
    public List<Integer> getWebCamCategoriesIds(long webCam_id) {

        String selectQuery = "SELECT * FROM " + TABLE_WEBCAM_CATEGORY + " WHERE "
                + KEY_WEBCAM_ID + " = " + webCam_id;
        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        List<Integer> categories_ids = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                categories_ids.add(c.getInt(c.getColumnIndex(KEY_CATEGORY_ID)));
            } while (c.moveToNext());
        }
        c.close();

        return categories_ids;
    }

    /**
     * Creating WebCam_category
     */
    public void createWebCamCategory(long webCam_id, int category_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WEBCAM_ID, webCam_id);
        values.put(KEY_CATEGORY_ID, category_id);
        values.put(KEY_CREATED_AT, getDateTime());

        db.insert(TABLE_WEBCAM_CATEGORY, null, values);
    }

    /**
     * Deleting a WebCam category
     */
    private void deleteWebCamCategory(long webCam_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEBCAM_CATEGORY, KEY_WEBCAM_ID + " = ?",
                new String[] { String.valueOf(webCam_id) });
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * load Date
     * */
    private static Date loadDate(Cursor cursor, int index) {
        if (cursor.isNull(index)) {
            return null;
        }
        return new Date(cursor.getLong(index));
    }
}