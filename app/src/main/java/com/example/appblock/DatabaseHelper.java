package com.example.appblock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="Password.db";
    public static final String TABLE_NAME="password_table";

    public static final String COL1="Password";

    // this constructor will create the database
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    // here we are creating table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (" + COL1 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // this method will insert the data into the table
    public boolean insertData(String password){
        SQLiteDatabase db=this.getWritableDatabase();

        // dropping the table if it is exits
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

        ContentValues contentValues=new ContentValues();
        contentValues.put(COL1,password);

        long result=db.insert(TABLE_NAME,null,contentValues);
        return result != -1;
    }

    // this method will return all the data related to the query
    public Cursor getAllData(){
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor result=db.rawQuery("select * from " + TABLE_NAME,null);
        return result;
    }

    public void deleteData(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
