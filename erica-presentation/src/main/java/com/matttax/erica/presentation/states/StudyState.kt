package com.matttax.erica.presentation.states

import com.matttax.erica.domain.model.WordDomainModel
import com.matttax.erica.presentation.model.study.StudiedWord

data class StudyState(
    val allWords: List<WordDomainModel>? = null,
    val currentBatch: List<WordDomainModel>? = null,
    val currentAskedPosition: Int? = null,
    val isFinished: Boolean? = null,
    val isLastCorrect: Boolean? = null,
    val batchResult: List<StudiedWord>? = null,
) {
    override fun toString(): String {
        return "\n" +
                "all_size=${allWords?.size.toString()}\n" +
                "current_word=${currentBatch?.getOrNull(currentAskedPosition ?: -1)?.text}\n" +
                "correct=$isLastCorrect\n" +
                "current_batch=${currentBatch?.map { it.text }}, pos=$currentAskedPosition\n" +
                "finished=$isFinished\n" +
                "results=${batchResult}"
    }
}
