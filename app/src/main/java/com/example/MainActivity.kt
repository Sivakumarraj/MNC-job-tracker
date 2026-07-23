package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AiMatcherScreen
import com.example.ui.screens.DirectoryScreen
import com.example.ui.screens.FeasibilityScreen
import com.example.ui.screens.LiveFeedScreen
import com.example.ui.screens.SavedJobsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.JobViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: JobViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val selectedTab by viewModel.selectedTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (selectedTab) {
                                        AppNavTab.DIRECTORY -> "MNC Careers Directory"
                                        AppNavTab.LIVE_JOBS -> "5-Min Live Job Scanner"
                                        AppNavTab.AI_MATCHER -> "Gemini AI Resume Matcher"
                                        AppNavTab.SAVED -> "Saved Applications Tracker"
                                        AppNavTab.FEASIBILITY -> "Free Tier Feasibility Report"
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            AppNavTab.values().forEach { tab ->
                                val icon = when (tab) {
                                    AppNavTab.DIRECTORY -> Icons.Default.Business
                                    AppNavTab.LIVE_JOBS -> Icons.Default.Radar
                                    AppNavTab.AI_MATCHER -> Icons.Default.AutoAwesome
                                    AppNavTab.SAVED -> Icons.Default.Bookmark
                                    AppNavTab.FEASIBILITY -> Icons.Default.Assessment
                                }

                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = { Icon(imageVector = icon, contentDescription = tab.label) },
                                    label = { Text(text = tab.label) },
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            AppNavTab.DIRECTORY -> DirectoryScreen(viewModel)
                            AppNavTab.LIVE_JOBS -> LiveFeedScreen(viewModel)
                            AppNavTab.AI_MATCHER -> AiMatcherScreen(viewModel)
                            AppNavTab.SAVED -> SavedJobsScreen(viewModel)
                            AppNavTab.FEASIBILITY -> FeasibilityScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
