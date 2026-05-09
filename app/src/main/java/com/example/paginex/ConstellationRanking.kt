package com.example.paginex

/**
 * Ranks library books for the constellation map so each genre shows at most [TOP_BOOKS_PER_GENRE]
 * stars, preferring books with more owner posts/ratings, viewer likes/saves (own profile only),
 * and profile favorites.
 *
 * Like/save counts use only posts present in [feedPosts] (session cache); they are omitted when
 * viewing another user's map ([viewerUid] != [targetUserId]).
 */
object ConstellationRanking {

    const val TOP_BOOKS_PER_GENRE = 5

    private const val W_OWN_POST = 8f
    private const val W_RATING_SUM = 1.5f
    private const val W_LIKED = 3f
    private const val W_SAVED = 4f
    private const val W_FAVORITE = 25f

    fun dedupeReadingList(readingList: List<ReadingStatus>): List<ReadingStatus> =
        readingList
            .groupBy { it.book.id }
            .values
            .map { statuses -> statuses.maxBy { it.addedAt } }

    private fun scoreForBook(
        bookId: String,
        feedPosts: List<Post>,
        favoriteBookIds: Set<String>,
        targetUserId: String,
        viewerUid: String
    ): Float {
        val ownPosts = feedPosts.filter {
            it.userId == targetUserId && it.book.id == bookId && !it.isBooklistPost
        }
        val ownCount = ownPosts.size.toFloat()
        val ratingSum = ownPosts.filter { it.rating > 0f }.sumOf { it.rating.toDouble() }.toFloat()

        val canUseViewerSignals = viewerUid.isNotBlank() && viewerUid == targetUserId
        val liked = if (canUseViewerSignals) feedPosts.count { it.book.id == bookId && it.isLiked } else 0
        val saved = if (canUseViewerSignals) feedPosts.count { it.book.id == bookId && it.isSaved } else 0
        val fav = if (bookId in favoriteBookIds) 1f else 0f

        return ownCount * W_OWN_POST +
            ratingSum * W_RATING_SUM +
            liked * W_LIKED +
            saved * W_SAVED +
            fav * W_FAVORITE
    }

    data class RankedResult(
        val rankedByGenre: Map<String, List<ReadingStatus>>,
        /** Hidden distinct-book count per genre beyond [TOP_BOOKS_PER_GENRE] (for "+N more"). */
        val extraBooksPerGenre: Map<String, Int>
    )

    fun rankForConstellation(
        readingList: List<ReadingStatus>,
        feedPosts: List<Post>,
        favoriteBookIds: Collection<String>,
        targetUserId: String,
        viewerUid: String
    ): RankedResult {
        val deduped = dedupeReadingList(readingList)
        val favSet = favoriteBookIds.toSet()
        val scores = deduped.associate { rs ->
            rs.book.id to scoreForBook(rs.book.id, feedPosts, favSet, targetUserId, viewerUid)
        }
        val byGenre = deduped.groupBy { it.book.genre }
        val ranked = mutableMapOf<String, List<ReadingStatus>>()
        val extras = mutableMapOf<String, Int>()
        for ((genre, list) in byGenre) {
            val sorted = list.sortedWith(
                compareByDescending<ReadingStatus> { scores[it.book.id] ?: 0f }
                    .thenByDescending { it.addedAt }
            )
            val total = sorted.size
            val top = sorted.take(TOP_BOOKS_PER_GENRE)
            ranked[genre] = top
            val hidden = (total - top.size).coerceAtLeast(0)
            if (hidden > 0) extras[genre] = hidden
        }
        return RankedResult(ranked, extras)
    }
}
