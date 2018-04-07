package com.example.josephbill.cheddar.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Joseph Bill on 4/7/2018.
 */

public class PlaceDbHelper extends SQLiteOpenHelper {
    //the database name
    private static final String DATABASE_NAME = "location.db";
    //if you change the database schema , you must increment the database version
    private static final int DATABASE_VERSION = 1;
    //constructor
    public PlaceDbHelper(Context context) {super (context, DATABASE_NAME, null,DATABASE_VERSION);}

    @Override
    public void onCreate (SQLiteDatabase sqLiteDatabase){
        //creating table to hold the places data
        final String SQL_CREATE_PLACES_TABLE = "CREATE TABLE" + PlaceContract.PlaceEntry.TABLE_NAME + "(" +
                PlaceContract.PlaceEntry._ID + "INTEGER PRIMARY KEY AUTOINCREMENT," +
                PlaceContract.PlaceEntry.COLUMN_PLACE_ID + "TEXT NOT NULL," +
                "UNIQUE(" + PlaceContract.PlaceEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE" +
                "); ";

        sqLiteDatabase.execSQL(SQL_CREATE_PLACES_TABLE);
    }

    @Override
    public void onUpgrade (SQLiteDatabase sqLiteDatabase, int i, int il){
        //for now simply drop the table and create a new one
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + PlaceContract.PlaceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
