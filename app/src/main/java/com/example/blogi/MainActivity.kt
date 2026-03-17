package com.example.blogi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.blogi.core.navigation.AppNavGraph
import com.example.blogi.core.settings.ThemePreferences
import com.example.blogi.feature_auth.logic.AuthViewModel
import com.example.blogi.feature_auth.ui.AuthScreen
import com.example.blogi.feature_blog.logic.BlogViewModel
import com.example.blogi.feature_home.logic.ApiDemoViewModel

// LISATUD OSA: API demo ViewModel import


import com.example.blogi.ui.theme.BlogiTheme
import com.example.blogiapp.feature_navbar.logic.BottomBarNavigator
import com.example.blogiapp.feature_navbar.ui.AppBottomBar
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val themePrefs = remember { ThemePreferences(context) }
            val darkTheme by themePrefs.darkModeFlow.collectAsState(initial = false)

            SideEffect {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }

            BlogiTheme(
                darkTheme = darkTheme,
                dynamicColor = false
            ) {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.uiState.collectAsState()

                if (authState.isLoggedIn) {
                    AppEntry(
                        darkTheme = darkTheme,
                        onDarkThemeChange = { enabled ->
                            lifecycleScope.launch {
                                themePrefs.setDarkMode(enabled)
                            }
                        },
                        onLogoutClick = {
                            authViewModel.logout()
                        }
                    )
                } else {
                    AuthScreen(authViewModel = authViewModel)
                }
            }
        }
    }
}

/* KOMMENTAAR
AppEntry seob kokku:
1) navController
2) navigator (eraldi loogikaklass)
3) AppBottomBar (UI)
4) AppNavGraph (route -> screen seos)
*/
@Composable
fun AppEntry(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
) {
    val navController = rememberNavController()
    val navigator = remember(navController) { BottomBarNavigator(navController) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val blogViewModel: BlogViewModel = viewModel()

    // LISATUD OSA: eraldi ViewModel API testandmete jaoks


    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentRoute = currentRoute,
                onHomeClick = { navigator.goHome() },
                onCreateClick = { navigator.goCreate() },
                onProfileClick = { navigator.goProfile() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(
                navController = navController,
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange,
                blogViewModel = blogViewModel,

                // LISATUD OSA: anna API demo ViewModel nav graphile edasi


                onLogoutClick = onLogoutClick
            )
        }
    }
}