package com.example.filepersistencetest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper(
    context: Context,
    name: String,
    version: Int
) : SQLiteOpenHelper(context, name, null, version) {
    private val createBook = "create table Book(" +
            "id integer primary key autoincrement," +
            "author text," +
            "price real," +
            "pages integer, " +
            "name text," +
            "category_id integer)"

    private val createCategory = "create table Category(" +
            "id integer primary key autoincrement," +
            "category_name text," +
            "category_code integer)"

    override fun onCreate(p0: SQLiteDatabase?) {
        p0?.execSQL(createBook)
        p0?.execSQL(createCategory)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        if (p1 <= 1) {
            p0?.execSQL(createCategory)
        }
        if (p1 <= 2) {
            p0?.execSQL("alter table Book add column category_id integer")
        }
    }
}