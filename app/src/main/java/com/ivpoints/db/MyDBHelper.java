package com.ivpoints.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/2/23.
 */
public class MyDBHelper extends SQLiteOpenHelper {

    private static final String CREATE_PROVINCE="create table Province(id integer primary key autoincrement,provinceName text,provinceCode integer)";
    private static final String CREATE_CITY="create table City(id integer primary key autoincrement, cityName text, cityCode integer, provinceId integer)";
    private static final String CREATE_COUNTY="create table County(id integer primary key autoincrement, countyName text, cityId integer, weatherId text)";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "china.db";

    public MyDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Province");
        db.execSQL("drop table if exists City");
        db.execSQL("drop table if exists County");
        onCreate(db);
    }
}
