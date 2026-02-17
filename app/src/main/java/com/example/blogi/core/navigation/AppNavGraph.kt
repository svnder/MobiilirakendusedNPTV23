package com.example.blogi.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.blogi.feature_blog.logic.BlogViewModel
import com.example.blogi.feature_postdetail.ui.PostDetailScreen
import com.example.blogi.feature_profile.ui.ProfileScreen
import com.example.blogiapp.core.navigation.AppDestinations
import com.example.blogiapp.feature_create.ui.CreateScreen
import com.example.blogiapp.feature_home.ui.HomeScreen


@Composable
fun AppNavGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    blogViewModel: BlogViewModel

    ) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) {
            HomeScreen(
                posts = blogViewModel.posts,
                onPostClick = { postId ->
                    navController.navigate(AppDestinations.postDetailRoute(postId))
                }
            )
        }

        composable(AppDestinations.CREATE) {
            CreateScreen(
                onSavePost = { title, content ->
                    blogViewModel.addPost(title, content)
                    navController.navigate(AppDestinations.HOME) {
                        launchSingleTop = true
                    }
                }
            )
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
                onDarkThemeChange = onDarkThemeChange
            )
        }
    }
}
