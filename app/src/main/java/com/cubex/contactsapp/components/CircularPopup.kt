package com.cubex.contactsapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cubex.contactsapp.ui.theme.popUpCentreBackground
import com.cubex.contactsapp.ui.theme.popUpCorrectIconBackground
import com.cubex.contactsapp.ui.theme.popUpOuterBackground
import com.cubex.contactsapp.ui.theme.whiteColor


@Composable
@Preview
fun CircularPopup(
    label: String = "Contact added \nSuccessfully",
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Check,
    onDismiss: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val circleSize = 240.dp

        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            popUpCentreBackground,
                            popUpOuterBackground
                        ),
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(popUpCorrectIconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Success Icon",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                            .clickable(enabled = onDismiss != null) { onDismiss?.invoke() },
                    )
                }


            }
        }
    }
}