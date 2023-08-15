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
                "($WORDS_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$WORDS_COLUMN_TEXT_LANGUAGE VARCHAR(3), " +
                "$WORDS_COLUMN_TRANSLATION_LANGUAGE VARCHAR(3), " +
                "$WORDS_COLUMN_TEXT VARCHAR(200), " +
                "$WORDS_COLUMN_TRANSLATION VARCHAR(200), " +
                "$WORDS_COLUMN_TIMES_ASKED INTEGER, " +
                "$WORDS_COLUMN_TIMES_CORRECT INTEGER, " +
                "$WORDS_COLUMN_LAST_ASKED_TIMESTAMP INTEGER, " +
                "$WORDS_COLUMN_SET_ID INTEGER" +
                ");"
        val createSetsTableQuery = "CREATE TABLE $SETS_TABLE_NAME " +
                "($SETS_COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$SETS_COLUMN_NAME VARCHAR(100), " +
                "$SETS_COLUMN_LAST_MODIFIED_TIMESTAMP INTEGER, " +
                "$SETS_COLUMN_DESCRIPTION VARCHAR(500)" +
                ");"
        db.execSQL(createWordsTableQuery)
        db.execSQL(createSetsTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $WORDS_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $SETS_TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "Erica.db"
        const val DATABASE_VERSION = 1

        const val WORDS_TABLE_NAME = "words"
        const val WORDS_COLUMN_ID = "id"
        const val WORDS_COLUMN_TEXT_LANGUAGE = "text_language"
        const val WORDS_COLUMN_TRANSLATION_LANGUAGE = "translation_language"
        const val WORDS_COLUMN_TEXT = "text"
        const val WORDS_COLUMN_TRANSLATION = "translation"
        const val WORDS_COLUMN_TIMES_ASKED = "times_asked"
        const val WORDS_COLUMN_TIMES_CORRECT = "times_correct"
        const val WORDS_COLUMN_LAST_ASKED_TIMESTAMP = "last_asked"
        const val WORDS_COLUMN_SET_ID = "set_id"

        const val SETS_TABLE_NAME = "sets"
        const val SETS_COLUMN_ID = "id"
        const val SETS_COLUMN_NAME = "name"
        const val SETS_COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified"
        const val SETS_COLUMN_DESCRIPTION = "description"
    }
}
