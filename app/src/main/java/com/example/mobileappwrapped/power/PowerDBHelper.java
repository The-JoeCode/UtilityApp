package com.example.mobileappwrapped.power;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PowerDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PowerDB.db";
    private static final int DATABASE_VERSION = 2; // Bump version if schema changed

    public PowerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE PowerRecords (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "CustomerName TEXT, " +
                "Consumption REAL, " +
                "Category TEXT, " +
                "TotalBill REAL, " +
                "Bill REAL, " +
                "Tax REAL, " +
                "DateTime TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS PowerRecords");
        onCreate(db);
    }

    public boolean insertRecords(String customerName, double consumption, String category, double totalBill, double bill, double tax) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        cv.put("CustomerName", customerName);
        cv.put("Consumption", consumption);
        cv.put("Category", category);
        cv.put("TotalBill", totalBill);
        cv.put("Bill", bill);
        cv.put("Tax", tax);
        cv.put("DateTime", dateTime);

        long res = db.insert("PowerRecords", null, cv);
        return res != -1;
    }

    public Cursor getAllRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM PowerRecords ORDER BY DateTime DESC", null);
    }

    public boolean clearAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("PowerRecords", null, null);
        return rowsDeleted > 0;
    }
}