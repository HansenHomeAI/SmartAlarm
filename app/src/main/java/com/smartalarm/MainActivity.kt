package com.smartalarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.smartalarm.ui.theme.SmartAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartAlarmTheme {
                SmartAlarmPlaceholder()
            }
        }
    }
}

@Composable
fun SmartAlarmPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.hello_smart_alarm),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.next_alarm_placeholder),
            fontSize = 20.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SmartAlarmPreview() {
    SmartAlarmTheme {
        SmartAlarmPlaceholder()
    }
}
