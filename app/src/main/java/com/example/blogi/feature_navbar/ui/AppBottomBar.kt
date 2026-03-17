package com.example.blogiapp.feature_navbar.ui

import AppDestinations
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/* KOMMENTAAR
Bottom bar on eraldi UI komponent.
Ta EI navigeeri ise, vaid saab callbackid parentilt.
*/
@Composable
fun AppBottomBar(
    currentRoute: String?,
    onHomeClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == AppDestinations.HOME,
            onClick = onHomeClick,
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == AppDestinations.CREATE,
            onClick = onCreateClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = "Create") },
            label = { Text("Create") }
        )

        NavigationBarItem(
            selected = currentRoute == AppDestinations.PROFILE,
            onClick = onProfileClick,
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}