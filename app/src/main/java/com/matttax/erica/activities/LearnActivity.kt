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
import com.matttax.erica.R
import com.matttax.erica.databinding.ActivityLearnBinding
import com.matttax.erica.dialogs.impl.AfterBatchDialog
import com.matttax.erica.dialogs.impl.WordAnsweredDialog
import com.matttax.erica.domain.config.SetId
import com.matttax.erica.domain.config.StudyConfig
import com.matttax.erica.domain.config.WordGroupConfig
import com.matttax.erica.domain.config.WordsSorting
import com.matttax.erica.presentation.states.StudyState
import com.matttax.erica.presentation.viewmodels.StudyViewModel
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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val batchSize = intent.getIntExtra(BATCH_SIZE_EXTRA_NAME, 5)
        val setId = intent.getIntExtra(SET_ID_EXTRA_NAME, -1)
        val wordsCount = intent.getIntExtra(WORDS_COUNT_EXTRA_NAME, -1)
        val wordsSorting = intent.getIntExtra(WORDS_SORTING_EXTRA_NAME, -1).toWordSorting()
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
                    batchSize = batchSize
                )
            )
            studyViewModel.onGetNewBatchAction()
        }
        studyViewModel.onGetNextWordAction()


        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.definitionInputField.setOnKeyListener(View.OnKeyListener { _, i, keyEvent ->
            if ((keyEvent.action == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                scope.launch {
                    studyViewModel.onWordAnswered(binding.definitionInputField.text.toString())
                }
                return@OnKeyListener true
            }
            false
        })
        binding.close.setOnClickListener {
            finish()
        }
        binding.doNotKnow.setOnClickListener {
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
            ?: return

        if (studyState.isFinished == true) {
            finish()
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
                    isCorrect = studyState.isLastCorrect ?: false
                ) {
                    dialogOnScreen = false
                    if (studyState.currentAskedPosition?.plus(1) != studyState.currentBatch?.size) {
                        studyViewModel.onGetNextWordAction()
                    } else {
                        AfterBatchDialog(
                            context = this,
                            results = studyState.batchResult ?: emptyList(),
                        ) {
                            studyViewModel.onGetNewBatchAction()
                        }.showDialog()
                    }
                }.showDialog()
            }
        } else {
            binding.definitionInputField.text?.clear()
            binding.termAskedField.text = currentWord.text
            with(currentWord) {
                wordSpeller.spellText(
                    text,
                    textLanguage.locale
                )
            }
        }
    }

    companion object {
        private const val BATCH_SIZE_EXTRA_NAME = "batch_size"
        private const val SET_ID_EXTRA_NAME = "set_id"
        private const val WORDS_COUNT_EXTRA_NAME = "words_count"
        private const val WORDS_SORTING_EXTRA_NAME = "words_sorting"

        fun start(
            context: Context,
            setId: Int,
            batchSize: Int,
            wordsCount: Int,
            wordsSorting: WordsSorting
        ) {
            val intent = Intent(context, LearnActivity::class.java).apply {
                putExtra(SET_ID_EXTRA_NAME, setId)
                putExtra(BATCH_SIZE_EXTRA_NAME, batchSize)
                putExtra(WORDS_COUNT_EXTRA_NAME, wordsCount)
                putExtra(WORDS_SORTING_EXTRA_NAME, wordsSorting.toInt())
            }
            context.startActivity(intent)
        }

        private fun WordsSorting.toInt(): Int {
            return when(this) {
                WordsSorting.RANDOM -> 0
                WordsSorting.LAST_ADDED_FIRST -> 1
                WordsSorting.FIRST_ADDED_FIRST -> 2
                WordsSorting.WORST_ANSWERED_FIRST -> 3
                WordsSorting.BEST_ANSWERED_FIRST -> 4
                WordsSorting.LONG_AGO_ASKED_FIRST -> 5
                WordsSorting.RECENTLY_ASKED_FIRST -> 6
            }
        }

        private fun Int.toWordSorting(): WordsSorting {
            return when(this) {
                1 -> WordsSorting.LAST_ADDED_FIRST
                2 -> WordsSorting.FIRST_ADDED_FIRST
                3 -> WordsSorting.WORST_ANSWERED_FIRST
                4 -> WordsSorting.BEST_ANSWERED_FIRST
                5 -> WordsSorting.LONG_AGO_ASKED_FIRST
                6 -> WordsSorting.RECENTLY_ASKED_FIRST
                else -> WordsSorting.RANDOM
            }
        }
    }
}
