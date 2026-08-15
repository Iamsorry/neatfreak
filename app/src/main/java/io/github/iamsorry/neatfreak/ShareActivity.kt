package io.github.iamsorry.neatfreak

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.iamsorry.neatfreak.ui.theme.NeatFreakTheme

class ShareActivity : ComponentActivity() {
    private var incomingText by mutableStateOf("")
    private var incomingToken by mutableIntStateOf(0)
    private var incomingAction by mutableStateOf(AutoAction.NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            NeatFreakTheme {
                NeatFreakApp(
                    initialText = incomingText,
                    requestToken = incomingToken,
                    autoAction = incomingAction,
                    onReturnProcessedText = ::returnProcessedText,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_PROCESS_TEXT) return
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: return

        incomingAction = if (!intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)) {
            AutoAction.RETURN_TO_CALLER
        } else {
            AutoAction.COPY
        }
        incomingText = text
        incomingToken += 1
    }

    private fun returnProcessedText(value: String) {
        setResult(
            RESULT_OK,
            Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, value),
        )
        finish()
    }
}
