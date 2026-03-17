package com.example.blogi.core.navigation

import AppDestinations
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogi.feature_blog.logic.BlogViewModel
import com.example.blogi.feature_home.logic.ApiDemoViewModel
import com.example.blogi.feature_home.ui.ApiDemoScreen
import com.example.blogi.feature_home.ui.HomeScreen
import com.example.blogi.feature_postdetail.ui.PostDetailScreen
import com.example.blogi.feature_profile.ui.ProfileScreen
import com.example.blogiapp.feature_create.ui.CreateScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    blogViewModel: BlogViewModel,
    onLogoutClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) {
            HomeScreen(
                blogViewModel = blogViewModel,
                onPostClick = { postId ->
                    navController.navigate(AppDestinations.postDetailRoute(postId))
                }
            )
        }

        composable(AppDestinations.CREATE) {
            CreateScreen(
                onSavePost = { title, content ->
                    blogViewModel.addPost(
                        title = title,
                        content = content,
                        onSuccess = {
                            navController.navigate(AppDestinations.HOME) {
                                popUpTo(AppDestinations.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onError = {
                        }
                    )
                }
            )
        }

        composable(AppDestinations.API_DEMO) {
            val apiDemoViewModel: ApiDemoViewModel = viewModel()
            ApiDemoScreen(viewModel = apiDemoViewModel)
        }

        composable(AppDestinations.POST_DETAIL) { backStackEntry ->
            val postId = backStackEntry.arguments
                ?.getString("postId")
                ?.toLongOrNull()

            val post = postId?.let { blogViewModel.getPostById(it) }

            PostDetailScreen(post = post)
        }

        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange,
                onLogoutClick = onLogoutClick,
                onApiDemoClick = {
                    navController.navigate(AppDestinations.API_DEMO)
                }
            )
        }
    }
}