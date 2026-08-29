package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ads.FakeAdsManager
import com.example.data.local.AppDatabase
import com.example.data.repository.GameRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.dataStore
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = androidx.room.Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "sudoku_db"
        ).build()
        
        val gameRepository = GameRepository(database.gameDao())
        val settingsRepository = SettingsRepository(applicationContext.dataStore)
        val viewModelFactory = AppViewModelProvider(gameRepository, settingsRepository)
        val adsManager = FakeAdsManager()
        
        setContent {
            val settingsViewModel: com.example.viewmodel.SettingsViewModel = viewModel(factory = viewModelFactory)
            val themeMode by settingsViewModel.themeMode.collectAsState()
            
            val isDarkTheme = when (themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                val showBottomBar = currentRoute in listOf("home", "statistics", "settings")
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(currentRoute, navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel(factory = viewModelFactory),
                                onPlayToday = {
                                    navController.navigate("play/daily")
                                },
                                onPractice = { diff ->
                                    navController.navigate("play/practice?difficulty=${diff.name}")
                                },
                                onResume = {
                                    navController.navigate("play/resume")
                                }
                            )
                        }
                        composable("play/{type}?difficulty={diff}") { backStackEntry ->
                            val type = backStackEntry.arguments?.getString("type") ?: "practice"
                            val diffStr = backStackEntry.arguments?.getString("diff") ?: "MEDIUM"
                            
                            val isDaily = type == "daily"
                            val isResume = type == "resume"
                            
                            val difficulty = try { com.example.data.models.Difficulty.valueOf(diffStr) } catch(e:Exception) { com.example.data.models.Difficulty.MEDIUM }
                            
                            PlayScreen(
                                viewModel = viewModel(factory = viewModelFactory),
                                settingsViewModel = viewModel(factory = viewModelFactory),
                                adsManager = adsManager,
                                isDaily = isDaily,
                                difficulty = difficulty,
                                isResume = isResume,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("statistics") {
                            StatisticsScreen(
                                viewModel = viewModel(factory = viewModelFactory)
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel(factory = viewModelFactory)
                            )
                        }
                    }
                }
            }
        }
    }
}
