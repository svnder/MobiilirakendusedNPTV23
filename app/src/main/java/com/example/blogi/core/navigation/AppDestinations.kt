object AppDestinations {
    const val HOME = "home"
    const val CREATE = "create"
    const val PROFILE = "profile"
    const val API_DEMO = "api_demo"
    const val POST_DETAIL = "post_detail/{postId}"

    fun postDetailRoute(postId: Long) = "post_detail/$postId"
}