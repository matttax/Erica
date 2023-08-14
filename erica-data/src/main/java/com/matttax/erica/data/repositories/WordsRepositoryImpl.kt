package com.matttax.erica.data.repositories

import android.content.ContentValues
import com.matttax.erica.data.database.SqliteDatabaseManager
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_DEFINITION
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_DEFINITION_LANGUAGE
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_LAST_ASKED
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_SET_ID
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TERM
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TERM_LANGUAGE
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TIMES_ASKED
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_TIMES_CORRECT
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_WORDS_COUNT
import com.matttax.erica.data.database.SqliteDatabaseManager.Companion.COLUMN_WORD_ID
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
        val db = sqliteDatabaseManager.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TERM_LANGUAGE, wordDomainModel.textLanguage.code)
        contentValues.put(COLUMN_DEFINITION_LANGUAGE, wordDomainModel.translationLanguage.code)
        contentValues.put(COLUMN_TERM, wordDomainModel.text)
        contentValues.put(COLUMN_DEFINITION, wordDomainModel.translation)
        contentValues.put(COLUMN_TIMES_ASKED, 0)
        contentValues.put(COLUMN_TIMES_CORRECT, 0)
        contentValues.put(COLUMN_LAST_ASKED, "1970-01-01")
        contentValues.put(COLUMN_SET_ID, wordDomainModel.setId)
        val result = db.insert(WORDS_TABLE_NAME, null, contentValues)
        db.execSQL("UPDATE sets SET words_count = words_count + 1 WHERE id=${wordDomainModel.setId}")
        return result != -1L
    }

    override fun getWords(wordGroupConfig: WordGroupConfig): List<WordDomainModel> {
        val query = "SELECT * FROM $WORDS_TABLE_NAME " +
                "WHERE $COLUMN_TERM_LANGUAGE<>\"null\" ${setIdToQuery(wordGroupConfig.setId)} " +
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
        sqliteDatabaseManager.writableDatabase.execSQL("UPDATE $SETS_TABLE_NAME SET $COLUMN_WORDS_COUNT=$COLUMN_WORDS_COUNT-1 WHERE id=$setId")
    }

    override fun moveToSet(fromSetId: Long, toSetId: Long, vararg wordIds: Long) {
        sqliteDatabaseManager.writableDatabase.execSQL("UPDATE $WORDS_TABLE_NAME SET $COLUMN_SET_ID=$toSetId " +
                "WHERE $COLUMN_WORD_ID IN " +
                wordIds.asList().toString().replace('[', '(').replace(']', ')')
        )
        sqliteDatabaseManager.writableDatabase.execSQL("UPDATE $SETS_TABLE_NAME SET $COLUMN_WORDS_COUNT=$COLUMN_WORDS_COUNT-${wordIds.size} " +
                "WHERE id=$fromSetId")
        sqliteDatabaseManager.writableDatabase.execSQL("UPDATE $SETS_TABLE_NAME SET $COLUMN_WORDS_COUNT=$COLUMN_WORDS_COUNT+${wordIds.size} " +
                "WHERE id=$toSetId")
    }

    override fun onWordAnswered(wordId: Long, isCorrect: Boolean) {
        sqliteDatabaseManager.writableDatabase.execSQL("UPDATE $WORDS_TABLE_NAME " +
                "SET $COLUMN_TIMES_CORRECT=$COLUMN_TIMES_CORRECT+1 " +
                "WHERE id=$wordId")
        if (isCorrect) {
            sqliteDatabaseManager.writableDatabase.execSQL(
                "UPDATE $WORDS_TABLE_NAME SET $COLUMN_TIMES_ASKED=$COLUMN_TIMES_ASKED+1, " +
                    "$COLUMN_LAST_ASKED = CURRENT_TIMESTAMP, " +
                    "$COLUMN_TIMES_CORRECT=$COLUMN_TIMES_CORRECT+1 " +
                    "WHERE id=$wordId"
            )
        } else {
            sqliteDatabaseManager.writableDatabase.execSQL(
                "UPDATE $WORDS_TABLE_NAME SET $COLUMN_TIMES_ASKED=$COLUMN_TIMES_ASKED+1, " +
                    "$COLUMN_LAST_ASKED = CURRENT_TIMESTAMP " +
                    "WHERE id=$wordId"
            )
        }
    }

    private fun setIdToQuery(setId: SetId): String {
        return when(setId) {
            is SetId.None -> ""
            is SetId.One -> "AND $COLUMN_SET_ID=${setId.id}"
            is SetId.Many -> "AND $COLUMN_SET_ID IN ${setId.ids.toString().replace('[','(').replace(']',')')}"
        }
    }

    private fun sortingToQuery(wordsSorting: WordsSorting): String {
        return when(wordsSorting) {
            WordsSorting.LAST_ADDED_FIRST -> "$COLUMN_ID ASC"
            WordsSorting.FIRST_ADDED_FIRST -> "$COLUMN_ID DESC"
            WordsSorting.BEST_ANSWERED_FIRST -> "$COLUMN_TIMES_CORRECT / CAST(${COLUMN_TIMES_ASKED} as float) DESC"
            WordsSorting.WORST_ANSWERED_FIRST -> "$COLUMN_TIMES_CORRECT / CAST(${COLUMN_TIMES_ASKED} as float) ASC"
            WordsSorting.LONG_AGO_ASKED_FIRST -> "$COLUMN_LAST_ASKED DESC"
            WordsSorting.RECENTLY_ASKED_FIRST -> "$COLUMN_LAST_ASKED ASC"
            WordsSorting.RANDOM -> "RANDOM()"
        }
    }
}
