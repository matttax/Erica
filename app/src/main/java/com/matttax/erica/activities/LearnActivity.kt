package com.matttax.erica.activities

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.matttax.erica.*
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

    lateinit var binding: ActivityLearnBinding

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var dialogOnScreen = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val batchSize = intent.getIntExtra("batch_size", 5)

        studyViewModel.observeState()
            .flowOn(Dispatchers.Main)
            .onEach {
                Log.i("viewstate", it.toString())
                runOnUiThread {
                    it?.let { data -> setData(data) }
                }
            }
            .launchIn(scope)

        scope.launch {
            studyViewModel.onGetWords(
                StudyConfig(
                    wordGroupConfig = WordGroupConfig(
                        setId = SetId.One(intent.getLongExtra("setId", -1)),
                        sorting = WordsSorting.WORST_ANSWERED_FIRST
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
                with(currentWord){
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
            with(currentWord){
                wordSpeller.spellText(
                    text,
                    textLanguage.locale
                )
            }
        }
    }

}