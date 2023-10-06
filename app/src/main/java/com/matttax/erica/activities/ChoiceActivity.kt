package com.matttax.erica.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.matttax.erica.R
import com.matttax.erica.fragments.SetsFragment
import com.matttax.erica.fragments.WordsFragment
import com.matttax.erica.model.WordSet
import com.matttax.erica.utils.ChoiceNavigator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChoiceActivity : AppCompatActivity(), ChoiceNavigator {

    private var wordsAreSelected = false
    private var currentSet: WordSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)

        backToSets()
        currentSet = savedInstanceState?.getParcelable(OPENED_SET_KEY)
        currentSet?.let { showWords(it) }
    }

    override fun showWords(set: WordSet) {
        removeOldWordsFragment()
        currentSet = set
        supportFragmentManager
            .beginTransaction()
            .replace(
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    R.id.wordsFragment
                else R.id.setsFragment,
                WordsFragment.getInstance(set),
                WORDS_FRAGMENT_TAG
            )
            .commit()
    }

    override fun backToSets() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.setsFragment, SetsFragment())
            .commit()
    }

    override fun notifyWordsSelected(areSelected: Boolean) {
        wordsAreSelected = areSelected
    }

    override fun listenBackPressed(lifecycleOwner: LifecycleOwner, listener: () -> Unit) {
        supportFragmentManager.setFragmentResultListener(BACK_PRESSED_EVENT, lifecycleOwner) { _, _ ->
            listener()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (wordsAreSelected) {
            supportFragmentManager.setFragmentResult(BACK_PRESSED_EVENT, Bundle())
            return
        }
        val wordsFragment = supportFragmentManager.findFragmentByTag(WORDS_FRAGMENT_TAG)
        if (wordsFragment == null) {
            super.onBackPressed()
        } else {
            supportFragmentManager
                .beginTransaction()
                .remove(wordsFragment)
                .commit()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                backToSets()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentSet?.let { outState.putParcelable(OPENED_SET_KEY, it) }
    }

    private fun removeOldWordsFragment() {
        supportFragmentManager.findFragmentByTag(WORDS_FRAGMENT_TAG)?.let {
            supportFragmentManager
                .beginTransaction()
                .remove(it)
                .commit()
        }
    }

    companion object {
        const val WORDS_FRAGMENT_TAG = "WORDS"
        const val OPENED_SET_KEY = "OPENED_SET"
        const val BACK_PRESSED_EVENT = "BACK_PRESSED"
    }
}
