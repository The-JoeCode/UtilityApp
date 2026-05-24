package com.example.mobileappwrapped.bmi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BMIDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BMI_DB.db";
    private static final int DATABASE_VERSION = 2; // Bump version if schema changed

    public BMIDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE BMIRecords (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Height REAL, " +
                "Weight REAL, " +
                "BMI REAL, " +
                "Category TEXT, " +
                "DateTime TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Only drop if structure changed significantly — else use ALTER TABLE
        db.execSQL("DROP TABLE IF EXISTS BMIRecords");
        onCreate(db);
    }

    public boolean insertRecords(double height, double weight, double bmi, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        // Format current date and time
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        cv.put("Height", height);
        cv.put("Weight", weight);
        cv.put("BMI", bmi);
        cv.put("Category", category);
        cv.put("DateTime", dateTime);

        long res = db.insert("BMIRecords", null, cv);
        return res != -1;
    }

    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM BMIRecords ORDER BY DateTime DESC", null);
    }

    public boolean clearAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("BMIRecords", null, null);
        return rowsDeleted > 0;
    }

}