package com.matttax.erica.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SqliteDatabaseManager @Inject constructor(
    @ApplicationContext context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createWordsTableQuery = "CREATE TABLE $WORDS_TABLE_NAME " +
                "($COLUMN_WORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_TERM_LANGUAGE VARCHAR(2), " +
                "$COLUMN_DEFINITION_LANGUAGE VARCHAR(2), " +
                "$COLUMN_TERM VARCHAR(100), " +
                "$COLUMN_DEFINITION VARCHAR(100), " +
                "$COLUMN_TIMES_ASKED INTEGER, " +
                "$COLUMN_TIMES_CORRECT INTEGER, " +
                "$COLUMN_LAST_ASKED DATE, " +
                "$COLUMN_SET_ID INTEGER" +
                ");"
        val createSetsTableQuery = "CREATE TABLE $SETS_TABLE_NAME " +
                "($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME VARCHAR(20), " +
                "$COLUMN_WORDS_COUNT INTEGER, " +
                "$COLUMN_SET_DESCRIPTION VARCHAR(100)" +
                ");"
        db.execSQL(createWordsTableQuery)
        db.execSQL(createSetsTableQuery)

        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, "All Words")
        contentValues.put(COLUMN_WORDS_COUNT, 0)
        contentValues.put(COLUMN_SET_DESCRIPTION, "Everything")
        db.insert(SETS_TABLE_NAME, null, contentValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $WORDS_TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "Erica.db"
        const val DATABASE_VERSION = 1

        const val WORDS_TABLE_NAME = "words"
        const val COLUMN_WORD_ID = "id"
        const val COLUMN_TERM_LANGUAGE = "term_language"
        const val COLUMN_DEFINITION_LANGUAGE = "definition_language"
        const val COLUMN_TERM = "term"
        const val COLUMN_DEFINITION = "definition"
        const val COLUMN_TIMES_ASKED = "times_asked"
        const val COLUMN_TIMES_CORRECT = "times_correct"
        const val COLUMN_LAST_ASKED = "last_asked"
        const val COLUMN_SET_ID = "set_id"

        const val SETS_TABLE_NAME = "sets"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_WORDS_COUNT = "words_count"
        const val COLUMN_SET_DESCRIPTION = "set_description"
    }
}
