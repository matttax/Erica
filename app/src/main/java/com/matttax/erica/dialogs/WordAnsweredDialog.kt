package com.matttax.erica.dialogs

import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.google.android.material.card.MaterialCardView
import com.matttax.erica.StudyCard
import com.matttax.erica.activities.LearnActivity
import com.matttax.erica.R
import com.matttax.erica.StudyItem

@RequiresApi(Build.VERSION_CODES.N)
class WordAnsweredDialog(val context: LearnActivity, resource: Int, private val answer: StudyItem, private val wordId: Int, private val asked: StudyCard): Dialog(context, resource) {

    private val answeredHeader: TextView = dialogView.findViewById(R.id.answeredHeader)
    private val answeredCorrectWord: TextView = dialogView.findViewById(R.id.answeredCorrectWord)
    private val notIncorrectButton: TextView = dialogView.findViewById(R.id.notIncorrect)
    private val dialogCard: MaterialCardView = dialogView.findViewById(androidx.core.R.id.notification_main_column_container)

    init {
        initDismissButton(R.id.answerNext)
        incr(1)
        answeredCorrectWord.text = answer.translation
        dialog.setOnDismissListener {
            if (context.words.size == 1) {
                val a = AfterBatchDialog(context, R.layout.after_batch, context.incorrectWords, context.correctWords)
                a.dialog.setOnDismissListener {
                    kill()
                    incr(0)
                }
                a.showDialog()
            } else {
                kill()
            }

        }

        setHeader()
        setNotIncorrectListener()
    }

    private fun setHeader() {
        if (answer.word != answer.translation) {
            answeredHeader.setBackgroundColor(ContextCompat.getColor(context, R.color.crimson))
            answeredHeader.text = "Incorrect"
            dialogCard.strokeColor = ContextCompat.getColor(context, R.color.crimson)
            context.incorrectWords.add(asked)
            db.wordAnsweredIncorrectly(asked.id)
        } else {
            dialogCard.strokeColor = ContextCompat.getColor(context, R.color.green)
            db.wordAnsweredCorrectly(asked.id)
            context.correctWords.add(asked)
        }
    }

    private fun setNotIncorrectListener() {
        if (answer.word == null || answer.word == answer.translation) {
            notIncorrectButton.isInvisible = true
        } else if (answer.word != answer.translation) {
            notIncorrectButton.setOnClickListener {
                context.incorrectWords.remove(asked)
                context.correctWords.add(asked)
                db.incrementWordAskedColumn(wordId)
                dialog.dismiss()
            }
        }
    }

    fun kill() {
        context.updateQuestion()
        if (context.words.isEmpty()) {
            context.finish()
        } else {
            context.words.peek().spellTerm(context)
        }
    }

    fun incr(answered: Int) {
        context.answered += answered
        context.answeredProgressBar.incrementProgressBy(answered)
        context.answeredTextInfo.text = "${context.answered}/${context.studying.nextBatchStart}"
    }
}