package io.github.iamsorry.neatfreak

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.iamsorry.neatfreak.domain.CleanResult
import io.github.iamsorry.neatfreak.domain.LinkCleaner
import io.github.iamsorry.neatfreak.domain.PlatformKind
import io.github.iamsorry.neatfreak.network.HttpRedirectResolver
import io.github.iamsorry.neatfreak.ui.theme.NeatFreakTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var incomingText by mutableStateOf("")
    private var incomingToken by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            NeatFreakTheme {
                NeatFreakApp(
                    initialText = incomingText,
                    requestToken = incomingToken,
                    autoAction = AutoAction.COPY,
                    onReturnProcessedText = {},
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
        if (intent?.action != Intent.ACTION_SEND) return
        val text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString() ?: return

        incomingText = text
        incomingToken += 1
    }
}

internal enum class AutoAction {
    NONE,
    COPY,
    RETURN_TO_CALLER,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NeatFreakApp(
    initialText: String,
    requestToken: Int,
    autoAction: AutoAction,
    onReturnProcessedText: (String) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    val clipboardLabel = stringResource(R.string.result_title)
    val cleaner = remember { LinkCleaner(HttpRedirectResolver()) }
    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf(initialText) }
    var result by remember { mutableStateOf<CleanResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isCleaning by remember { mutableStateOf(false) }
    var cleaningJob by remember { mutableStateOf<Job?>(null) }
    var cleaningGeneration by remember { mutableIntStateOf(0) }

    fun copy(value: String) {
        clipboard.setPrimaryClip(ClipData.newPlainText(clipboardLabel, value))
        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    fun startCleaning(actionAfterSuccess: AutoAction) {
        if (input.isBlank()) return

        val requestedInput = input
        val generation = cleaningGeneration + 1
        cleaningGeneration = generation
        cleaningJob?.cancel()
        cleaningJob = scope.launch {
            isCleaning = true
            error = null
            result = null
            try {
                val cleaned = cleaner.clean(requestedInput)
                if (generation != cleaningGeneration) return@launch
                result = cleaned
                when (actionAfterSuccess) {
                    AutoAction.NONE -> Unit
                    AutoAction.COPY -> copy(cleaned.cleanUrl)
                    AutoAction.RETURN_TO_CALLER -> onReturnProcessedText(cleaned.cleanUrl)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (throwable: Throwable) {
                if (generation == cleaningGeneration) {
                    error = throwable.message ?: throwable::class.java.simpleName
                }
            } finally {
                if (generation == cleaningGeneration) {
                    isCleaning = false
                    cleaningJob = null
                }
            }
        }
    }

    LaunchedEffect(requestToken) {
        if (requestToken > 0 && initialText.isNotBlank()) {
            input = initialText
            startCleaning(autoAction)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    result = null
                    error = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 148.dp),
                label = { Text(stringResource(R.string.input_label)) },
                placeholder = { Text(stringResource(R.string.input_hint)) },
                enabled = !isCleaning,
                minLines = 4,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val clip = clipboard.primaryClip
                        val pasted = clip?.takeIf { it.itemCount > 0 }
                            ?.getItemAt(0)
                            ?.coerceToText(context)
                            ?.toString()
                        if (pasted.isNullOrBlank()) {
                            Toast.makeText(context, R.string.paste_empty, Toast.LENGTH_SHORT).show()
                        } else {
                            input = pasted
                            result = null
                            error = null
                        }
                    },
                    enabled = !isCleaning,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.paste))
                }

                Button(
                    onClick = { startCleaning(AutoAction.COPY) },
                    enabled = input.isNotBlank() && !isCleaning,
                    modifier = Modifier.weight(1.5f),
                ) {
                    Text(stringResource(R.string.clean_and_copy))
                }
            }

            if (isCleaning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(28.dp))
                    Spacer(Modifier.padding(6.dp))
                    Text(stringResource(R.string.cleaning))
                }
            }

            error?.let { message -> ErrorCard(message) }
            result?.let { cleanResult ->
                ResultCard(
                    result = cleanResult,
                    onCopy = { copy(cleanResult.cleanUrl) },
                    onShare = { shareUrl(context, cleanResult.cleanUrl) },
                    onOpen = { openUrl(context, cleanResult.cleanUrl) },
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.error_title), fontWeight = FontWeight.SemiBold)
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ResultCard(
    result: CleanResult,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.result_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            SelectionContainer {
                Text(result.cleanUrl, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                stringResource(
                    R.string.platform_label,
                    stringResource(platformName(result.platform)),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                if (result.removedParameters.isEmpty()) {
                    stringResource(R.string.no_parameters_removed)
                } else {
                    stringResource(R.string.removed_parameters, result.removedParameters.joinToString())
                },
                style = MaterialTheme.typography.bodySmall,
            )
            if (result.redirectCount > 0) {
                Text(
                    pluralStringResource(
                        R.plurals.redirect_count,
                        result.redirectCount,
                        result.redirectCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onCopy, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.copy))
                }
                OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.share))
                }
                OutlinedButton(onClick = onOpen, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.open))
                }
            }
        }
    }
}

@StringRes
private fun platformName(platform: PlatformKind): Int = when (platform) {
    PlatformKind.THREADS -> R.string.platform_threads
    PlatformKind.FACEBOOK -> R.string.platform_facebook
    PlatformKind.FACEBOOK_EXTERNAL -> R.string.platform_facebook_external
    PlatformKind.INSTAGRAM -> R.string.platform_instagram
    PlatformKind.LINKEDIN -> R.string.platform_linkedin
    PlatformKind.SPOTIFY -> R.string.platform_spotify
    PlatformKind.STEAM -> R.string.platform_steam
    PlatformKind.YOUTUBE -> R.string.platform_youtube
    PlatformKind.AMAZON -> R.string.platform_amazon
    PlatformKind.GENERIC -> R.string.platform_generic
}

private fun shareUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: Exception) {
        Toast.makeText(context, R.string.unsupported_action, Toast.LENGTH_SHORT).show()
    }
}
