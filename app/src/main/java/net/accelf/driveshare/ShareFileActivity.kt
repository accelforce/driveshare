package net.accelf.driveshare

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.accelf.driveshare.components.ParameterCard
import net.accelf.driveshare.ui.theme.DriveShareTheme
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class ShareFileActivity : ComponentActivity() {
    private var openResult: Result<Uri>? by mutableStateOf(null)

    private val openDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { pickedUri ->
            openResult = pickedUri?.let { Result.success(it) }
                ?: Result.failure(Exception("cancelled"))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mimeType = intent.type ?: error("No mimetype provided")
        val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) ?: error("No file provided")

        setContent {
            DriveShareTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ShareFileActivityContent(
                        mimeType = mimeType,
                        uri = uri,
                        onOverwrite = { onOverwrite(mimeType) },
                        openResult = openResult,
                        onFinish = {
                            openResult = null
                            finish()
                        },
                    )
                }
            }
        }
    }

    private fun onOverwrite(mimeType: String) {
        openDocument.launch(arrayOf(mimeType))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewShareFileActivityContent() {
    ShareFileActivityContent(
        mimeType = "application/pdf",
        uri = Uri.EMPTY,
        onOverwrite = {},
        openResult = Result.success(Uri.EMPTY),
        onFinish = {},
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun ShareFileActivityContent(
    mimeType: String,
    uri: Uri,
    onOverwrite: () -> Unit,
    openResult: Result<Uri>?,
    onFinish: () -> Unit,
) {
    Column {
        ParameterCard(
            title = "MIME Type",
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            icon = {
                   Icon(
                       imageVector = Icons.Default.Sell,
                       contentDescription = "MIME Type",
                       modifier = Modifier
                           .padding(8.dp)
                           .fillMaxSize(),
                   )
            },
        ) {
            Text(text = mimeType, style = MaterialTheme.typography.subtitle1)
        }
        ParameterCard(
            title = "File name",
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            icon = {
                Icon(
                    imageVector = Icons.Default.DriveFileRenameOutline,
                    contentDescription = "File name",
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                )
            },
        ) {
            uri.lastPathSegment?.let { fileName ->
                Text(text = fileName, style = MaterialTheme.typography.subtitle1)
            } ?: Text(
                text = "New file",
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.60f),
            )
        }
        ParameterCard(
            title = "URI",
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            icon = {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "URI",
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                )
            },
        ) {
            uri.toString().takeIf { it.isNotEmpty() }?.let { uri ->
                Text(text = uri, style = MaterialTheme.typography.subtitle1)
            } ?: Text(
                text = "Empty",
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.60f),
            )
        }

        Row(
            modifier = Modifier.padding(4.dp),
        ) {
            Button(
                onClick = onOverwrite,
                modifier = Modifier.fillMaxWidth(),
                enabled = openResult?.isFailure ?: true,
            ) {
                Text(text = "Overwrite existing file")
            }
        }

        openResult?.onSuccess { targetUri ->
            val contentResolver = LocalContext.current.contentResolver
            var writing: Boolean? by mutableStateOf(null)

            LaunchedEffect(uri, targetUri) {
                launch(Dispatchers.IO) {
                    contentResolver.openOutputStream(targetUri, "w")?.use { output ->
                        contentResolver.openInputStream(uri)?.use { input ->
                            writing = true
                            output.write(input.readBytes())
                            writing = false
                        } ?: TODO("cannot open input")
                    } ?: TODO("cannot open output")
                }
            }

            ParameterCard(
                title = "Upload",
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = "Upload",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                    )
                },
            ) {
                Text(text = targetUri.toString(), style = MaterialTheme.typography.subtitle1)

                writing?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (it) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(32.dp),
                            )
                            Text(text = "Writing to the file", style = MaterialTheme.typography.subtitle1)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "done",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(32.dp),
                                tint = MaterialTheme.colors.primary,
                            )
                            Text(text = "Done!", style = MaterialTheme.typography.subtitle1)

                            LaunchedEffect(writing) {
                                delay(Duration.seconds(2))
                                writing = null
                                onFinish()
                            }
                        }
                    }
                }
            }
        }?.onFailure {
            ParameterCard(
                title = "Information",
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Information",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                    )
                },
            ) {
                Text(text = "File picker was cancelled", style = MaterialTheme.typography.subtitle1)
            }
        }
    }
}
