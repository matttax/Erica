package com.matttax.erica.data.repositories

import android.content.ContentValues
import android.util.Log
import com.matttax.erica.data.database.SqliteDatabaseManager
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_COLUMN_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_COLUMN_NAME
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_COLUMN_DESCRIPTION
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_COLUMN_LAST_MODIFIED_TIMESTAMP
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_TABLE_NAME
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_LAST_ASKED_TIMESTAMP
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_SET_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_TABLE_NAME
import com.matttax.erica.domain.config.SetGroupConfig
import com.matttax.erica.domain.config.SetSorting
import com.matttax.erica.domain.model.SetDomainModel
import com.matttax.erica.domain.repositories.SetsRepository

class SetsRepositoryImpl(
    private val sqliteDatabaseManager: SqliteDatabaseManager,
): SetsRepository {

    override fun addSet(name: String, description: String): Long {
        val contentValues = ContentValues().apply {
            put(SETS_COLUMN_NAME, name)
            put(SETS_COLUMN_LAST_MODIFIED_TIMESTAMP, System.currentTimeMillis())
            put(SETS_COLUMN_DESCRIPTION, description)
        }
        return sqliteDatabaseManager.writableDatabase.insert(SETS_TABLE_NAME, null, contentValues)
    }

    override fun updateSet(id: Long, name: String, description: String) {
        val query = "UPDATE $SETS_TABLE_NAME " +
                "SET $SETS_COLUMN_NAME=\"$name\", " +
                "$SETS_COLUMN_DESCRIPTION=${"\"$description\"".ifEmpty { " " }}," +
                "$SETS_COLUMN_LAST_MODIFIED_TIMESTAMP=${System.currentTimeMillis()} " +
                "WHERE $SETS_COLUMN_ID=$id"
        sqliteDatabaseManager.writableDatabase.execSQL(query)
    }

    override fun getSets(setGroupConfig: SetGroupConfig): List<SetDomainModel> {
        val currentSets = mutableListOf<SetDomainModel>()
        val cursor = sqliteDatabaseManager.writableDatabase.rawQuery(
            "SELECT $SETS_COLUMN_ID, $SETS_COLUMN_NAME, $SETS_COLUMN_DESCRIPTION, " +
                    "(SELECT COUNT(*) FROM $WORDS_TABLE_NAME WHERE $WORDS_COLUMN_SET_ID=$SETS_TABLE_NAME.$SETS_COLUMN_ID) " +
                "FROM $SETS_TABLE_NAME " +
                "ORDER BY ${sortingToQuery(setGroupConfig.sorting)} " +
                "LIMIT ${setGroupConfig.limit}", null)
        if (cursor.count != 0) {
            while (cursor.moveToNext()) {
                currentSets += SetDomainModel(
                    id = cursor.getLong(0),
                    name = cursor.getString(1),
                    description = cursor.getString(2),
                    wordsCount = cursor.getInt(3)
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
            SetSorting.ALPHABETICALLY -> SETS_COLUMN_NAME
            SetSorting.LAST_MODIFIED -> "$SETS_COLUMN_LAST_MODIFIED_TIMESTAMP DESC"
        }
    }
}
