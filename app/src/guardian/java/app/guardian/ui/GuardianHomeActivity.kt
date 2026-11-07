package app.guardian.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.guardian.engine.GuardianEngine
import app.guardian.engine.GuardianEngine.Protection

/** Spike placeholder for the senior home screen: one big status and one button. */
class GuardianHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface { HomeScreen() } } }
    }
}

@Composable
private fun HomeScreen() {
    val context = LocalContext.current
    // re-read the state whenever the engine reports a change
    val tick by GuardianEngine.changes.observeAsState()
    val protection = remember(tick) { GuardianEngine.protection() }
    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (GuardianEngine.consentIntent(context) == null) GuardianEngine.start(context)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = when (protection) {
                Protection.ON -> "You're protected"
                Protection.STARTING -> "Turning on protection…"
                Protection.PAUSED -> "Protection is paused"
                Protection.OFF -> "Protection is off"
            },
            fontSize = 34.sp,
        )
        if (protection == Protection.OFF) {
            Button(
                modifier = Modifier.padding(top = 32.dp),
                onClick = {
                    val ask = GuardianEngine.consentIntent(context)
                    if (ask != null) consent.launch(ask) else GuardianEngine.start(context)
                },
            ) { Text("Turn on protection", fontSize = 24.sp) }
        }
    }
}
