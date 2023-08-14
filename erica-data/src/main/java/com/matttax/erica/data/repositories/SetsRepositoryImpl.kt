package com.matttax.erica.data.repositories

import android.content.ContentValues
import com.matttax.erica.data.database.SqliteDatabaseManager
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_DEFINITION
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_DEFINITION_LANGUAGE
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_LAST_ASKED
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_NAME
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_SET_DESCRIPTION
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_SET_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TERM
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TERM_LANGUAGE
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TIMES_ASKED
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TIMES_CORRECT
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_WORDS_COUNT
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_WORD_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_TABLE_NAME
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_TABLE_NAME
import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.config.SetSorting
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.repositories.SetsRepository

class SetsRepositoryImpl(
    private val sqliteDatabaseManager: SqliteDatabaseManager,
): SetsRepository {

    override fun addSet(name: String, description: String): Long {
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, name)
        contentValues.put(COLUMN_WORDS_COUNT, 1)
        contentValues.put(COLUMN_SET_DESCRIPTION, description)
        val result = sqliteDatabaseManager.writableDatabase.insert(SETS_TABLE_NAME, null, contentValues)
        contentValues.clear()
        contentValues.put(COLUMN_TERM_LANGUAGE, "null")
        contentValues.put(COLUMN_DEFINITION_LANGUAGE, "null")
        contentValues.put(COLUMN_TERM, "")
        contentValues.put(COLUMN_DEFINITION, "")
        contentValues.put(COLUMN_TIMES_ASKED, 0)
        contentValues.put(COLUMN_TIMES_CORRECT, 0)
        contentValues.put(COLUMN_LAST_ASKED, "1970-01-01")
        contentValues.put(COLUMN_SET_ID, result)
        sqliteDatabaseManager.writableDatabase.insert(WORDS_TABLE_NAME, null, contentValues)
        return result
    }

    override fun updateSet(id: Long, name: String, description: String) {
        val query = "UPDATE $SETS_TABLE_NAME " +
                "SET $COLUMN_NAME=\"$name\" " +
                "$COLUMN_SET_DESCRIPTION=${"\"$description\"}".ifEmpty { " " }} " +
                "WHERE $COLUMN_ID=$id"
        sqliteDatabaseManager.writableDatabase.execSQL(query)
    }

    override fun getSets(setGroupConfig: SetGroupConfig): List<SetDomainModel> {
        val currentSets = mutableListOf<SetDomainModel>()
        val cursor = sqliteDatabaseManager.writableDatabase.rawQuery(
            "SELECT $COLUMN_SET_ID, $COLUMN_NAME, $COLUMN_WORDS_COUNT, $COLUMN_SET_DESCRIPTION " +
                "FROM $WORDS_TABLE_NAME " +
                "JOIN $SETS_TABLE_NAME ON $WORDS_TABLE_NAME.$COLUMN_SET_ID = $SETS_TABLE_NAME.$COLUMN_ID " +
                "GROUP BY $COLUMN_SET_ID " +
                "ORDER BY ${sortingToQuery(setGroupConfig.sorting)} " +
                "LIMIT ${setGroupConfig.limit}", null)
        if (cursor.count != 0) {
            while (cursor.moveToNext()) {
                currentSets += SetDomainModel(
                    id = cursor.getLong(0),
                    name = cursor.getString(1),
                    description = cursor.getString(3),
                    wordsCount = cursor.getInt(2)
                )
            }
        }
        cursor.close()
        return currentSets
    }

    override fun removeById(id: Long) {
        sqliteDatabaseManager.writableDatabase.execSQL("DELETE FROM $SETS_TABLE_NAME WHERE id=$id")
        sqliteDatabaseManager.writableDatabase.execSQL("DELETE FROM $WORDS_TABLE_NAME WHERE set_id=$id")
    }

    private fun sortingToQuery(setSorting: SetSorting): String {
        return when(setSorting) {
            SetSorting.ALPHABETICALLY -> COLUMN_NAME
            SetSorting.LAST_ADDED -> "max($WORDS_TABLE_NAME.$COLUMN_WORD_ID) DESC"
        }
    }
}
