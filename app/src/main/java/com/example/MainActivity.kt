package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppDatabase
import com.example.data.PreferencesManager
import com.example.data.TranslationRepository
import com.example.ui.HistoryScreen
import com.example.ui.SettingsScreen
import com.example.ui.SetupWizardScreen
import com.example.ui.theme.AutoLingoTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var translationRepository: TranslationRepository
    private lateinit var database: AppDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesManager = PreferencesManager(this)
        translationRepository = TranslationRepository(this, preferencesManager)
        database = AppDatabase.getInstance(this)

        setContent {
            AutoLingoTheme {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "AutoLingo - Translator Keyboard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = "Setup & Test"
                                    )
                                },
                                label = { Text("Keyboard") },
                                modifier = Modifier.testTag("nav_tab_keyboard")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                },
                                label = { Text("Settings") },
                                modifier = Modifier.testTag("nav_tab_settings")
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "History"
                                    )
                                },
                                label = { Text("History") },
                                modifier = Modifier.testTag("nav_tab_history")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> SetupWizardScreen(
                                translationRepository = translationRepository
                            )
                            1 -> SettingsScreen(
                                preferencesManager = preferencesManager
                            )
                            2 -> HistoryScreen(
                                database = database
                            )
                        }
                    }
                }
            }
        }
    }
}
