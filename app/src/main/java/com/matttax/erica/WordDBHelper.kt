package com.matttax.erica

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class WordDBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val context: Context? = null
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
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
        sqLiteDatabase.execSQL(createWordsTableQuery)
        sqLiteDatabase.execSQL(createSetsTableQuery)

        val cv = ContentValues()
        cv.put(COLUMN_NAME, "All Words")
        cv.put(COLUMN_WORDS_COUNT, 0)
        cv.put(COLUMN_SET_DESCRIPTION, "Everything")
        sqLiteDatabase.insert(SETS_TABLE_NAME, null, cv)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS $WORDS_TABLE_NAME")
        onCreate(sqLiteDatabase)
    }

    fun addWord(termLanguage: String, defLanguage: String,
                term: String, def: String, setId: Int) {
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(COLUMN_TERM_LANGUAGE, termLanguage)
        cv.put(COLUMN_DEFINITION_LANGUAGE, defLanguage)
        cv.put(COLUMN_TERM, term)
        cv.put(COLUMN_DEFINITION, def)
        cv.put(COLUMN_TIMES_ASKED, 0)
        cv.put(COLUMN_TIMES_CORRECT, 0)
        cv.put(COLUMN_LAST_ASKED, "1970-01-01")
        cv.put(COLUMN_SET_ID, setId)

        val result = db.insert(WORDS_TABLE_NAME, null, cv)
//        if (result == -1L)
//            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
//        else Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show()
    }

    fun addSet(name: String, description: String) {
        val db = this.writableDatabase
        val cv = ContentValues()

        cv.put(COLUMN_NAME, name)
        cv.put(COLUMN_WORDS_COUNT, 0)
        cv.put(COLUMN_SET_DESCRIPTION, description)

        val result = db.insert(SETS_TABLE_NAME, null, cv)
//        if (result == -1L)
//            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
//        else Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show()
    }

    fun getSet(): Cursor? {
        val query = "SELECT * FROM $SETS_TABLE_NAME"
        val db = this.writableDatabase
        return db.rawQuery(query, null)
    }

    fun getWords(setId: Int): Cursor? {
        val query = "SELECT * FROM $WORDS_TABLE_NAME WHERE set_id=$setId"
        val db = this.writableDatabase
        return db.rawQuery(query, null)
    }

    fun deleteAll() {
        val db = this.writableDatabase
        db.delete(SETS_TABLE_NAME, null, null)
        db.delete(WORDS_TABLE_NAME, null, null)
    }

    fun deleteSet(setId: Int) {
        this.writableDatabase.execSQL("DELETE FROM $SETS_TABLE_NAME WHERE id=$setId")
        this.writableDatabase.execSQL("DELETE FROM $WORDS_TABLE_NAME WHERE set_id=$setId")
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