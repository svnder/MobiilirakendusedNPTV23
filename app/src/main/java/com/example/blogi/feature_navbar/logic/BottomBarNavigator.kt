package com.example.blogiapp.feature_navbar.logic

import AppDestinations
import androidx.navigation.NavHostController

/* KOMMENTAAR
Navigeerimise loogika on eraldi klassis.
UI jääb puhtamaks ja testitavamaks.
*/
class BottomBarNavigator(
    private val navController: NavHostController
) {
    fun goHome() {
        navController.navigate(AppDestinations.HOME) {
            launchSingleTop = true
        }
    }

    fun goCreate() {
        navController.navigate(AppDestinations.CREATE) {
            launchSingleTop = true
        }
    }

    fun goProfile() {
        navController.navigate(AppDestinations.PROFILE) {
            launchSingleTop = true
        }
    }
}