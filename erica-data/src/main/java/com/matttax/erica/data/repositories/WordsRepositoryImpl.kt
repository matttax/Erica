package com.matttax.erica.data.repositories

import android.content.ContentValues
import com.matttax.erica.data.database.SqliteDatabaseManager
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_TRANSLATION
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_TRANSLATION_LANGUAGE
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_COLUMN_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_LAST_ASKED_TIMESTAMP
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_SET_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_TEXT
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_TEXT_LANGUAGE
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_TIMES_ASKED
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_TIMES_CORRECT
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_COLUMN_LAST_MODIFIED_TIMESTAMP
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_COLUMN_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.SETS_TABLE_NAME
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.WORDS_TABLE_NAME
import com.matttax.erica.domain.config.SetId
import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.config.WordsSorting
import com.matttax.erica.domain.model.Language
import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.domain.repositories.WordsRepository
import javax.inject.Inject

class WordsRepositoryImpl @Inject constructor(
    private val sqliteDatabaseManager: SqliteDatabaseManager,
): WordsRepository {

    override fun addWord(wordDomainModel: WordDomainModel): Boolean {
        val contentValues = ContentValues()
        contentValues.put(WORDS_COLUMN_TEXT_LANGUAGE, wordDomainModel.textLanguage.code)
        contentValues.put(WORDS_COLUMN_TRANSLATION_LANGUAGE, wordDomainModel.translationLanguage.code)
        contentValues.put(WORDS_COLUMN_TEXT, wordDomainModel.text)
        contentValues.put(WORDS_COLUMN_TRANSLATION, wordDomainModel.translation)
        contentValues.put(WORDS_COLUMN_TIMES_ASKED, 0)
        contentValues.put(WORDS_COLUMN_TIMES_CORRECT, 0)
        contentValues.put(WORDS_COLUMN_LAST_ASKED_TIMESTAMP, 0)
        contentValues.put(WORDS_COLUMN_SET_ID, wordDomainModel.setId)
        return sqliteDatabaseManager.writableDatabase.insert(WORDS_TABLE_NAME, null, contentValues) != -1L
    }

    override fun getWords(wordGroupConfig: WordGroupConfig): List<WordDomainModel> {
        val query = "SELECT * FROM $WORDS_TABLE_NAME " +
                "WHERE ${setIdToQuery(wordGroupConfig.setId)} " +
                "ORDER BY ${sortingToQuery(wordGroupConfig.sorting)} " +
                "LIMIT ${wordGroupConfig.limit ?: Int.MAX_VALUE}"

        val currentWords = mutableListOf<WordDomainModel>()
        val cursor = sqliteDatabaseManager.writableDatabase.rawQuery(query, null)
        if (cursor.count != 0) {
            while (cursor.moveToNext()) {
                currentWords += WordDomainModel(
                    id = cursor.getLong(0),
                    textLanguage = Language(cursor.getString(1)),
                    translationLanguage = Language(cursor.getString(2)),
                    text = cursor.getString(3),
                    translation = cursor.getString(4),
                    askedCount = cursor.getInt(5),
                    answeredCount = cursor.getInt(6),
                    lastAskedTimestamp = cursor.getLong(7),
                    setId = cursor.getLong(8)
                )
            }
        }
        cursor.close()
        return currentWords
    }

    override fun remove(wordId: Long, setId: Long) {
        sqliteDatabaseManager.writableDatabase.execSQL("DELETE FROM $WORDS_TABLE_NAME WHERE id=$wordId")
    }

    override fun moveToSet(fromSetId: Long, toSetId: Long, vararg wordIds: Long) {
        sqliteDatabaseManager.writableDatabase.execSQL("UPDATE $WORDS_TABLE_NAME SET $WORDS_COLUMN_SET_ID=$toSetId " +
                "WHERE $WORDS_COLUMN_ID IN " +
                wordIds.asList().toQuery()
        )
    }

    override fun onWordAnswered(wordId: Long, isCorrect: Boolean) {
        sqliteDatabaseManager.writableDatabase.execSQL("UPDATE $WORDS_TABLE_NAME " +
                "SET $WORDS_COLUMN_TIMES_CORRECT=$WORDS_COLUMN_TIMES_CORRECT+1 " +
                "WHERE id=$wordId")
        if (isCorrect) {
            sqliteDatabaseManager.writableDatabase.execSQL(
                "UPDATE $WORDS_TABLE_NAME SET $WORDS_COLUMN_TIMES_ASKED=$WORDS_COLUMN_TIMES_ASKED+1, " +
                    "$WORDS_COLUMN_LAST_ASKED_TIMESTAMP = CURRENT_TIMESTAMP, " +
                    "$WORDS_COLUMN_TIMES_CORRECT=$WORDS_COLUMN_TIMES_CORRECT+1 " +
                    "WHERE id=$wordId"
            )
        } else {
            sqliteDatabaseManager.writableDatabase.execSQL(
                "UPDATE $WORDS_TABLE_NAME SET $WORDS_COLUMN_TIMES_ASKED=$WORDS_COLUMN_TIMES_ASKED+1, " +
                    "$WORDS_COLUMN_LAST_ASKED_TIMESTAMP = CURRENT_TIMESTAMP " +
                    "WHERE id=$wordId"
            )
        }
    }

    private fun setIdToQuery(setId: SetId): String {
        return when(setId) {
            is SetId.None -> ""
            is SetId.One -> "$WORDS_COLUMN_SET_ID=${setId.id}"
            is SetId.Many -> "$WORDS_COLUMN_SET_ID IN ${setId.ids.toString().replace('[','(').replace(']',')')}"
        }
    }

    private fun sortingToQuery(wordsSorting: WordsSorting): String {
        return when(wordsSorting) {
            WordsSorting.LAST_ADDED_FIRST -> "$SETS_COLUMN_ID DESC"
            WordsSorting.FIRST_ADDED_FIRST -> "$SETS_COLUMN_ID ASC"
            WordsSorting.BEST_ANSWERED_FIRST -> "$WORDS_COLUMN_TIMES_CORRECT / CAST(${WORDS_COLUMN_TIMES_ASKED} as float) DESC"
            WordsSorting.WORST_ANSWERED_FIRST -> "$WORDS_COLUMN_TIMES_CORRECT / CAST(${WORDS_COLUMN_TIMES_ASKED} as float) ASC"
            WordsSorting.RECENTLY_ASKED_FIRST -> "$WORDS_COLUMN_LAST_ASKED_TIMESTAMP DESC"
            WordsSorting.LONG_AGO_ASKED_FIRST -> "$WORDS_COLUMN_LAST_ASKED_TIMESTAMP ASC"
            WordsSorting.RANDOM -> "RANDOM()"
        }
    }

    private fun <T> List<T>.toQuery(): String {
        return toString().replace('[', '(').replace(']', ')')
    }
}
