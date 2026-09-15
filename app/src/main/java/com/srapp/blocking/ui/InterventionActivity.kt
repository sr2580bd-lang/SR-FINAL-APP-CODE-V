package com.srapp.blocking.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.srapp.SrApplication
import com.srapp.blocking.data.BlockAttemptEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InterventionActivity : ComponentActivity() {

    private val viewModel: InterventionViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as SrApplication
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "unknown"
                return InterventionViewModel(app, packageName) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Intentionally NOT calling super.onBackPressed()-style bypass anywhere —
        // back press is intercepted below so the screen can't just be dismissed.
        setContent {
            com.srapp.core.ui.theme.SrAppTheme {
                InterventionScreen(
                    state = viewModel.state,
                    onAnswerReflection = viewModel::submitReflection,
                    onMathAnswer = viewModel::submitMathAnswer,
                    onFinished = { finishAndGoHome() }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Deliberately swallow back-press: user must complete the flow.
        // (Standard revoke-path is Settings > Accessibility, by design —
        // see MODULE 2 for the multi-layer deactivation flow, not this screen.)
    }

    private fun finishAndGoHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

enum class InterventionStep { WAIT, REFLECTION, MATH, DONE }

data class InterventionState(
    val packageName: String,
    val step: InterventionStep = InterventionStep.WAIT,
    val secondsRemaining: Int = 60,
    val reflectionAnswer: String = "",
    val mathProblem: MathProblem = MathProblem.generate(1),
    val mathAttemptIndex: Int = 0,
    val mathTotal: Int = 5,
    val mathWrongLastTry: Boolean = false
)

data class MathProblem(val a: Int, val b: Int, val op: Char) {
    val answer: Int get() = if (op == '+') a + b else a - b
    val prompt: String get() = "$a $op $b = ?"

    companion object {
        fun generate(difficulty: Int): MathProblem {
            val range = 10 + (difficulty * 15)
            val a = (1..range).random()
            val b = (1..range).random()
            val op = if ((0..1).random() == 0) '+' else '-'
            return MathProblem(a, b, op)
        }
    }
}

class InterventionViewModel(
    private val app: SrApplication,
    packageName: String
) : ViewModel() {

    private val _state = MutableStateFlow(InterventionState(packageName = packageName))
    val state: StateFlow<InterventionState> = _state.asStateFlow()

    init {
        runCountdown()
    }

    private fun runCountdown() {
        viewModelScope.launch {
            while (_state.value.secondsRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                _state.value = _state.value.copy(secondsRemaining = _state.value.secondsRemaining - 1)
            }
            _state.value = _state.value.copy(step = InterventionStep.REFLECTION)
        }
    }

    fun submitReflection(text: String) {
        _state.value = _state.value.copy(reflectionAnswer = text, step = InterventionStep.MATH)
    }

    fun submitMathAnswer(userAnswer: Int) {
        val current = _state.value
        val correct = userAnswer == current.mathProblem.answer
        if (!correct) {
            _state.value = current.copy(mathWrongLastTry = true)
            return
        }
        val nextIndex = current.mathAttemptIndex + 1
        if (nextIndex >= current.mathTotal) {
            completeIntervention()
        } else {
            _state.value = current.copy(
                mathAttemptIndex = nextIndex,
                mathProblem = MathProblem.generate(nextIndex + 1),
                mathWrongLastTry = false
            )
        }
    }

    private fun completeIntervention() {
        _state.value = _state.value.copy(step = InterventionStep.DONE)
        viewModelScope.launch {
            app.database.blockingDao().logAttempt(
                BlockAttemptEntity(
                    timestamp = System.currentTimeMillis(),
                    packageName = _state.value.packageName,
                    interventionCompleted = true,
                    interventionType = "wait+reflection+math",
                    triggerNote = _state.value.reflectionAnswer.takeIf { it.isNotBlank() }
                )
            )
        }
    }
}
