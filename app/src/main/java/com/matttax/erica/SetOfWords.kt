package com.matttax.erica

import java.util.*
import kotlin.math.ceil

class SetOfWords(val id: Int, val name: String, val description: String, val wordsCount: Int)

class WordGroup(val words: MutableList<QuizWord>, val batchSize: Int) {

    var batchesCount: Int = 0
    lateinit var batches: MutableList<Queue<QuizWord>>

    init {
        val batchesCount = ceil(words.size / batchSize.toDouble()).toInt()
        val batches = mutableListOf<Queue<QuizWord>>()
    }

    fun split() {
        words.shuffle()
    }


}

class Word(val term: String, val definition: String)

class LanguagePair(val termLanguage: String, val definitionLanguage: String)

class QuizWord(val id: Int,
               val langPair: LanguagePair,
               val word: Word,
               val timesAsked: Int,
               val timesCorrect: Int,
               val lastAsked: Date) {

    fun getCorrectPercentage() = timesCorrect.toDouble() / timesAsked
}