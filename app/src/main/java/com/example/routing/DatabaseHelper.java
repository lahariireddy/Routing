package com.example.routing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String TABLE_NAME = "people_table";
    private static final String COL1 = "ID";
    private static final String COL2 = "Time";
    private static final String COL3 = "origin";
    private static final String COL4 = "originLatLng";
    private static final String COL5 = "destination";
    private static final String COL6 = "destinationLatLng";
    private static final String COL7 = "waypoints";



    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 +" DATETIME DEFAULT CURRENT_TIMESTAMP,"+
                COL3 +" TEXT,"+
                COL4 +" TEXT,"+
                COL5 +" TEXT,"+
                COL6 +" TEXT,"+
                COL7 +" TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String source, String sourcePoint, String dest, String destPoint, String waypoints) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL3, source);
        contentValues.put(COL4, sourcePoint);
        contentValues.put(COL5, dest);
        contentValues.put(COL6, destPoint);
        contentValues.put(COL7, waypoints);

        Log.d(TAG, "addData: Adding route" + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if date as inserted incorrectly it will return -1
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns all the data from database
     * @return
     */

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Returns only the latest entry
     */

    public Cursor getLatestItem(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT *  FROM " + TABLE_NAME +
                " s1 WHERE " + COL2 + "= "+ "(SELECT MAX("+ COL2 +") FROM "+TABLE_NAME+" s2 WHERE s1."+COL1+" = s2."+COL1+ " ORDER BY "+COL2+")";


        Cursor data = db.rawQuery(query, null);
        return data;
    }


    /**
     * Delete from database
     * @param id
     * @param name
     *//*

    public void deleteName(int id, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL1 + " = '" + id + "'" +
                " AND " + COL2 + " = '" + name + "'";
        Log.d(TAG, "deleteName: query: " + query);
        Log.d(TAG, "deleteName: Deleting " + name + " from database.");
        db.execSQL(query);
    }*/

}