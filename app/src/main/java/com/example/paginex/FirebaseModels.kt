package com.example.paginex

import com.google.firebase.Timestamp

data class FireUser(
    val id: String = "",
    val username: String = "",
    val name: String = "",
    val surname: String = "",
    val password: String = "", // Note: For real apps, passwords should not be stored in Firestore
    val email: String = "",
    val birthday: Timestamp? = null,
    val lastLogin: Timestamp? = null,
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    // Additional fields for app UI compatibility
    val bio: String = "",
    val avatarUrl: String = "",
    val location: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val favoriteBooks: List<String> = emptyList() // Max 5 book IDs
)

data class FirePost(
    val id: String = "",
    val userId: String = "",
    val bookId: String = "", // Used for normal book posts
    val booklistId: String? = null, // Used for booklist posts
    val description: String = "",
    val rating: Double = 0.0,
    val status: String = "Okunacak",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireImage(
    val id: String = "",
    val path: String = "",
    val tableId: String = "", // tableId (ObjectId in image)
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireFollow(
    val id: String = "",
    val followerId: String = "",
    val followingId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class FireReadingStatus(
    val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val status: String = "", // Completed, Reading, Want to Read, On-Hold, Dropped
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireBookList(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val userId: String = "",
    val isPrivate: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireComment(
    val id: String = "",
    val parentId: String? = null, // UUID v7
    val comment: String = "",
    val userId: String = "",
    val tableId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireLike(
    val id: String = "",
    val userId: String = "",
    val tableId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class FireSave(
    val id: String = "",
    val userId: String = "",
    val tableId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

data class FireBook(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val genre: String = "",
    val summary: String = "",
    val isbn: String = "",
    val coverUrl: String = "",
    val rating: Double = 0.0,
    val publishYear: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class FireBookListBook(
    val id: String = "",
    val booklistId: String = "",
    val bookId: String = "",
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
