package com.farmledger.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmledger.app.R

private val Ink = Color(0xFF3D2A1A)

/**
 * SoS whisper tip（A2c_landscape_label_spec §3 模式 B）：
 * `whisper_label.png` 底＋≤4 字、10–11sp；同時最多 1 個（由呼叫端保證）。
 */
@Composable
fun WhisperLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    val short = text.take(4)
    Box(
        modifier
            .height(26.dp)
            .widthIn(min = 48.dp, max = 88.dp)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.whisper_label),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            short,
            color = Ink.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
