package com.renovartech.soundapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.renovartech.soundapp.karaoke.KaraokeEngine
import com.renovartech.soundapp.ui.theme.RenovarTechSoundAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var karaokeEngine: KaraokeEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        karaokeEngine = KaraokeEngine(applicationContext)

        setContent {
            RenovarTechSoundAppTheme {
                AppRoot(karaokeEngine = karaokeEngine)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        karaokeEngine.stop()
    }
}

private val tabs = listOf(
    "Ferramentas de Áudio",
    "Sons Relaxantes",
    "Karaokê",
    "Sincronizar",
    "Teste Auditivo",
    "Assistência & Loja",
    "Assinatura",
    "Legal",
    "Meus Dispositivos",
    "Calculadora"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(karaokeEngine: KaraokeEngine) {
    var selectedTab by remember { mutableStateOf(2) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Renovar Tech Sound App") })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    2 -> KaraokeScreen(karaokeEngine)
                    else -> PlaceholderScreen(tabs[selectedTab])
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Column {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Em construção nesta versão nativa. Continua disponível no site enquanto migramos aos poucos, módulo por módulo.")
    }
}

@Composable
fun KaraokeScreen(engine: KaraokeEngine) {
    val context = LocalContext.current
    var micActive by remember { mutableStateOf(engine.isActive) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            engine.start()
            micActive = true
        }
    }

    Column {
        Text("Karaokê 🎤", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Toque a música em qualquer app (YouTube, Spotify, etc.) e ative o microfone abaixo. " +
                "Este app nativo pede ao Android para apenas abaixar o volume da música em vez de pausar."
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (micActive) {
                engine.stop()
                micActive = false
            } else if (hasPermission) {
                engine.start()
                micActive = true
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }) {
            Text(if (micActive) "Desativar microfone" else "🎤 Ativar microfone ao vivo")
        }

        Spacer(modifier = Modifier.height(12.dp))
        if (micActive) {
            Text("Microfone ativo — cante à vontade!")
        }
    }
}
