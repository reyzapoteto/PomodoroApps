package com.pomodoroapps.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pomodoroapps.Database.DTO.PomodoroDTO

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    private val CREATE_TABLE_TODO =
        " CREATE TABLE $COLUMN_TODO (" +
                "$COLUMN_ID integer PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME text," +
                "$COLUMN_ITEM_PRIORITY text," +
                "$COLUMN_ITEM_ISCOMPLETED integer);"

    override fun onCreate(p0: SQLiteDatabase?) {
        onUpgrade(p0, 0, DATABASE_VERSION)
    }


    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        if (p1 < 1) {
            p0!!.execSQL(CREATE_TABLE_TODO)
        } else {
            p0!!.execSQL(CREATE_TABLE_TODO)
        }
    }

    fun deleteDatabaseData() {
        val db = writableDatabase as SQLiteDatabase
        db.delete(DATABASE_NAME, null, null)
        db.close()
    }

    fun addTask(activity: PomodoroDTO): Boolean {
        val db = writableDatabase as SQLiteDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, activity.name)
        contentValues.put(COLUMN_ITEM_PRIORITY, activity.priority)
        if (activity.isCompleted) {
            contentValues.put(COLUMN_ITEM_ISCOMPLETED, true) // true = 1
        } else {
            contentValues.put(COLUMN_ITEM_ISCOMPLETED, false) // false = 0
        }
        val result = db.insert(COLUMN_TODO, null, contentValues)

        return result != (-1).toLong()
    }

    fun updateTask(activity: PomodoroDTO) {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_NAME, activity.name)
        cv.put(COLUMN_ITEM_PRIORITY, activity.priority)
        if (activity.isCompleted) {
            cv.put(COLUMN_ITEM_ISCOMPLETED, true)
        } else {
            cv.put(COLUMN_ITEM_ISCOMPLETED, false)
        }

        db.update(COLUMN_TODO, cv, "$COLUMN_ID=?", arrayOf(activity.id.toString()))
    }

    fun getTask(): MutableList<PomodoroDTO> {
        val result: MutableList<PomodoroDTO> = ArrayList()
        val db = readableDatabase as SQLiteDatabase
        val queryResult = db.rawQuery("SELECT * FROM $COLUMN_TODO", null) as Cursor
        if (queryResult.moveToFirst()) {
            do {
                val task = PomodoroDTO()
                task.id = queryResult.getLong(queryResult.getColumnIndex(COLUMN_ID))
                task.name = queryResult.getString(queryResult.getColumnIndex(COLUMN_NAME))
                task.priority = queryResult.getString(
                    queryResult.getColumnIndex(
                        COLUMN_ITEM_PRIORITY
                    )
                )
                task.isCompleted = queryResult.getInt(
                    queryResult.getColumnIndex(
                        COLUMN_ITEM_ISCOMPLETED
                    )
                ) == 1
                result.add(task)
            } while (queryResult.moveToNext())
        }
        queryResult.close()
        db.close()
        return result
    }

    fun deleteTask(task: Long) {
        val db = writableDatabase as SQLiteDatabase
        db.delete(COLUMN_TODO, "$COLUMN_ID=?", arrayOf(task.toString()))
        db.close()
    }
}