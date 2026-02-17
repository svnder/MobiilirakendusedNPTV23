package com.example.blogiapp.core.navigation

/* KOMMENTAAR
Siin hoiame kõik route nimed ühes kohas.
Nii väldime kirjavigu navigeerimisel.
*/






object AppDestinations {
    const val HOME = "home"
    const val CREATE = "create"
    const val PROFILE = "profile"
    const val POST_DETAIL = "post_detail/{postId}"

    fun postDetailRoute(postId: Long): String = "post_detail/$postId"
}