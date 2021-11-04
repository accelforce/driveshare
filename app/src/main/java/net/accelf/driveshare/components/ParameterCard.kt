package net.accelf.driveshare.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ParameterCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
        ) {
            icon?.let { icon ->
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    icon.invoke()
                }
            }

            Column(
                modifier = Modifier.padding(4.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.subtitle2)
                content.invoke()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewParameterCard() {
    ParameterCard(
        title = "Param",
        modifier = Modifier.padding(4.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Param",
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
            )
        }
    ) {
        Text(
            text = "Very\nVery\nVery\nVery\nLong Value",
            style = MaterialTheme.typography.subtitle1,
        )
    }
}
