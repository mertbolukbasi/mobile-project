package com.example.paginex

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String,
    val bio: String = "",
    val location: String = "",
    val joinDate: String = "",
    val followingCount: Int = 0,
    val followersCount: Int = 0,
    val favoriteBooks: List<String> = emptyList()
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val genre: String = "Genel",
    val publishYear: Int = 2020,
    val summary: String = "",
    val isbn: String = "",
    val isSaved: Boolean = false
)

data class Post(
    val id: String,
    val userId: String,
    val book: Book, // If it's a booklist post, this will hold a dummy book for UI compatibility
    val status: String,
    val rating: Float,
    val review: String,
    val timestamp: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isBooklistPost: Boolean = false,
    val booklist: BookList? = null
)

data class BookList(
    val id: String,
    val userId: String = "",
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val isPrivate: Boolean = false,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val books: MutableList<Book> = mutableListOf()
)

data class ReadingStatus(
    val id: String,
    val userId: String,
    val book: Book,
    val status: String,
    val addedAt: Long = System.currentTimeMillis()
)

// --- REACTIVE LOCAL CACHE (populated from Firestore via syncMockData) ---
object MockData {
    var currentUser by mutableStateOf(User(
        id = "",
        username = "",
        fullName = "",
        avatarUrl = ""
    ))

    val sampleBooks = mutableStateListOf<Book>()
    val feedPosts = mutableStateListOf<Post>()
    val explorePosts = mutableStateListOf<String>()
    val sampleBookLists = mutableStateListOf<BookList>()
    val sampleUsers = mutableStateListOf<User>()
    val drafts = mutableStateListOf<Post>()
    val readingStatuses = mutableStateListOf<ReadingStatus>()
}
