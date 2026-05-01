package com.example.paginex

import com.google.firebase.Timestamp

data class FireUser(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val birthday: Timestamp? = null,
    val bio: String = "",
    val avatarUrl: String = "",
    val location: String = "",
    val favouriteBookIds: List<String> = emptyList(),
    val isActive: Boolean = true,
    val lastLogin: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FirePost(
    val id: String = "",
    val userId: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val imageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireBook(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val summary: String = "",
    val isbn: String = "",
    val publishYear: Int = 0,
    val rating: Float = 0f,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireComment(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val tableId: String = "", // Polymorphic: postId or bookId
    val parentId: String? = null, // For nested comments
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireBookList(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val isPrivate: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireReadingStatus(
    val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val status: String = "", // Completed, Reading, Want to Read, On-Hold, Dropped
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireFollow(
    val id: String = "",
    val followerId: String = "",
    val followingId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class FireLike(
    val id: String = "",
    val userId: String = "",
    val tableId: String = "", // post or book
    val createdAt: Timestamp = Timestamp.now()
)

data class FireSave(
    val id: String = "",
    val userId: String = "",
    val tableId: String = "", // post or book
    val createdAt: Timestamp = Timestamp.now()
)

data class FireImage(
    val id: String = "",
    val path: String = "",
    val tableId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireFavouriteBooks(
    val id: String = "",
    val userId: String = "",
    val bookIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
