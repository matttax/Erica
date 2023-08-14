package com.matttax.erica.domain.config

data class StudyConfig (
    val wordGroupConfig: WordGroupConfig,
    val studyMode: StudyMode = StudyMode.PRACTICE,
    val batchSize: Int = 7,
    val askMode: AskMode = AskMode.TEXT
)
