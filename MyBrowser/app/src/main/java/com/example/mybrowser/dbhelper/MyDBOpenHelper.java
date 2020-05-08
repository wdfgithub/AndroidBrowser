package com.example.mybrowser.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MyDBOpenHelper extends SQLiteOpenHelper {
    public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {super(context, name, null, 1); }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE recent(recentid INTEGER PRIMARY KEY AUTOINCREMENT,site VARCHAR(200) ,title VARCHAR(200)) ");
        db.execSQL("CREATE TABLE book(bookid INTEGER PRIMARY KEY AUTOINCREMENT,site VARCHAR(200) ,title VARCHAR(200)) ");
        db.execSQL("CREATE TABLE search(searchid INTEGER PRIMARY KEY AUTOINCREMENT,item VARCHAR(200)) ");
        db.execSQL("CREATE TABLE IF NOT EXISTS filedownlog " +
                "(id integer primary key autoincrement," +
                " downpath varchar(100)," +
                " threadid INTEGER, downlength INTEGER)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
