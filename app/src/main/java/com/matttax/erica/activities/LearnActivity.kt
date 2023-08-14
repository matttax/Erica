package com.matttax.erica.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.matttax.erica.R
import com.matttax.erica.databinding.ActivityLearnBinding
import com.matttax.erica.dialogs.impl.AfterBatchDialog
import com.matttax.erica.dialogs.impl.WordAnsweredDialog
import com.matttax.erica.domain.config.*
import com.matttax.erica.presentation.states.StudyState
import com.matttax.erica.presentation.viewmodels.StudyViewModel
import com.matttax.erica.presentation.viewmodels.impl.StudyViewModelImpl
import com.matttax.erica.speechtotext.WordSpeller
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LearnActivity : AppCompatActivity() {

    @Inject
    lateinit var studyViewModel: StudyViewModel

    @Inject
    lateinit var wordSpeller: WordSpeller

    private lateinit var binding: ActivityLearnBinding

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var dialogOnScreen = false
    private var doNotKnowFlag = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val batchSize = intent.getIntExtra(BATCH_SIZE_EXTRA_NAME, 7)
        val setId = intent.getIntExtra(SET_ID_EXTRA_NAME, -1)
        val wordsCount = intent.getIntExtra(WORDS_COUNT_EXTRA_NAME, -1)
        val wordsSorting = intent.getIntExtra(WORDS_SORTING_EXTRA_NAME, -1).toWordSorting()
        val askMode = intent.getIntExtra(ASK_MODE_EXTRA_NAME, -1).toAskMode()
        if (setId == -1 || wordsCount == -1) {
            finish()
        }

        studyViewModel.observeState()
            .flowOn(Dispatchers.Main)
            .onEach {
                runOnUiThread {
                    it?.let { data -> setData(data) }
                }
            }
            .launchIn(scope)

        scope.launch {
            studyViewModel.onGetWords(
                StudyConfig(
                    wordGroupConfig = WordGroupConfig(
                        setId = SetId.One(setId),
                        sorting = wordsSorting,
                        limit = wordsCount
                    ),
                    batchSize = batchSize,
                    askMode = askMode
                )
            )
            studyViewModel.onGetNewBatchAction()
        }
        studyViewModel.onGetNextWordAction()


        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.definitionInputField.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                scope.launch {
                    val text = binding.definitionInputField.text.toString()
                    if (text.isBlank()) {
                        doNotKnowFlag = true
                    }
                    studyViewModel.onWordAnswered(text)
                }
                return@OnKeyListener true
            }
            false
        })
        binding.close.setOnClickListener {
            finish()
        }
        binding.doNotKnow.setOnClickListener {
            doNotKnowFlag = true
            scope.launch {
                studyViewModel.onWordAnswered("")
            }
        }
        binding.answeredProgressBar.progressDrawable.setColorFilter(
            ContextCompat.getColor(this@LearnActivity, R.color.blue),
            PorterDuff.Mode.SRC_IN
        )
        binding.answeredProgressBar.max = batchSize
    }

    @SuppressLint("SetTextI18n")
    private fun setData(studyState: StudyState) {
        val currentWord = studyState.currentBatch?.getOrNull(studyState.currentAskedPosition ?: -1)
        if (studyState.isFinished == true) {
            finish()
        }
        if (currentWord == null) {
            return
        }
        val newPosition = studyState.currentAskedPosition ?: 0
        val newMax = studyState.currentBatch?.size ?: 0
        binding.answeredTextInfo.text = "$newPosition/$newMax"
        binding.answeredProgressBar.apply {
            progress = newPosition
            max = newMax
        }
        if (studyState.isLastCorrect != null) {
            if (!dialogOnScreen) {
                dialogOnScreen = true
                with(currentWord) {
                    wordSpeller.spellText(
                        translation,
                        translationLanguage.locale
                    )
                }
                WordAnsweredDialog(
                    context = this,
                    correctAnswer = currentWord.translation,
                    isCorrect = studyState.isLastCorrect ?: false,
                    showNotIncorrect = studyState.isLastCorrect == false && !doNotKnowFlag,
                    onOk = {
                        dialogOnScreen = false
                        doNotKnowFlag = false
                        if (studyState.currentAskedPosition?.plus(1) != studyState.currentBatch?.size) {
                            studyViewModel.onGetNextWordAction()
                        } else {
                            AfterBatchDialog(
                                context = this,
                                results = studyState.batchResult ?: emptyList(),
                                wordSpeller = wordSpeller,
                                remainingCount = studyState.remainingWords,
                            ) {
                                studyViewModel.onGetNewBatchAction()
                            }.showDialog()
                        }
                    },
                    onNotIncorrect = {
                        scope.launch {
                            studyViewModel.onWordForceCorrectAnswer()
                        }
                    }
                ).showDialog()
            }
        } else {
            binding.definitionInputField.text?.clear()
            binding.termAskedField.text = currentWord.text
            if (studyState.remainingWords > 0) {
                with(currentWord) {
                    wordSpeller.spellText(
                        text,
                        textLanguage.locale
                    )
                }
            }
        }
    }

    companion object {
        private const val BATCH_SIZE_EXTRA_NAME = "batch_size"
        private const val SET_ID_EXTRA_NAME = "set_id"
        private const val WORDS_COUNT_EXTRA_NAME = "words_count"
        private const val WORDS_SORTING_EXTRA_NAME = "words_sorting"
        private const val ASK_MODE_EXTRA_NAME = "ask_mode"

        fun start(
            context: Context,
            setId: Int,
            batchSize: Int = 7,
            wordsCount: Int = Int.MAX_VALUE,
            wordsSorting: WordsSorting = WordsSorting.RANDOM,
            askMode: AskMode = AskMode.TEXT
        ) {
            val intent = Intent(context, LearnActivity::class.java).apply {
                putExtra(SET_ID_EXTRA_NAME, setId)
                putExtra(BATCH_SIZE_EXTRA_NAME, batchSize)
                putExtra(WORDS_COUNT_EXTRA_NAME, wordsCount)
                putExtra(WORDS_SORTING_EXTRA_NAME, wordsSorting.toInt())
                putExtra(ASK_MODE_EXTRA_NAME, askMode.toInt())
            }
            context.startActivity(intent)
        }

        fun Int.toWordSorting(): WordsSorting {
            return when(this) {
                0 -> WordsSorting.WORST_ANSWERED_FIRST
                1 -> WordsSorting.LONG_AGO_ASKED_FIRST
                2 -> WordsSorting.RECENTLY_ASKED_FIRST
                3 -> WordsSorting.LAST_ADDED_FIRST
                4 -> WordsSorting.FIRST_ADDED_FIRST
                else -> WordsSorting.RANDOM
            }
        }

        fun Int.toAskMode(): AskMode {
            return when(this) {
                1 -> AskMode.TRANSLATION
                2 -> AskMode.BOTH
                else -> AskMode.TEXT
            }
        }

        private fun WordsSorting.toInt(): Int {
            return when(this) {
                WordsSorting.WORST_ANSWERED_FIRST -> 0
                WordsSorting.LONG_AGO_ASKED_FIRST -> 1
                WordsSorting.RECENTLY_ASKED_FIRST -> 2
                WordsSorting.LAST_ADDED_FIRST -> 3
                WordsSorting.FIRST_ADDED_FIRST -> 4
                else -> 5
            }
        }

        private fun AskMode.toInt(): Int {
            return when(this) {
                AskMode.TEXT -> 0
                AskMode.TRANSLATION -> 1
                AskMode.BOTH -> 2
            }
        }
    }
}
