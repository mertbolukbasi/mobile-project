package com.example.paginex

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreService(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // --- User Operations ---
    suspend fun saveUser(user: FireUser) {
        db.collection("users").document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): FireUser? {
        return db.collection("users").document(uid).get().await().toObject(FireUser::class.java)
    }

    // --- Post Operations ---
    suspend fun createPost(post: FirePost): String {
        val docRef = db.collection("posts").document()
        val postWithId = post.copy(id = docRef.id)
        docRef.set(postWithId).await()
        return docRef.id
    }

    suspend fun getPosts(): List<FirePost> {
        return db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().toObjects(FirePost::class.java)
    }

    // --- Interaction Operations (Like/Save/Follow) ---
    suspend fun toggleLike(userId: String, tableId: String, isLiked: Boolean) {
        val likeId = "${userId}_${tableId}"
        val likeRef = db.collection("likes").document(likeId)
        if (isLiked) {
            val like = FireLike(id = likeId, userId = userId, tableId = tableId)
            likeRef.set(like).await()
        } else {
            likeRef.delete().await()
        }
    }

    suspend fun toggleSave(userId: String, tableId: String, isSaved: Boolean) {
        val saveId = "${userId}_${tableId}"
        val saveRef = db.collection("saves").document(saveId)
        if (isSaved) {
            val save = FireSave(id = saveId, userId = userId, tableId = tableId)
            saveRef.set(save).await()
        } else {
            saveRef.delete().await()
        }
    }

    suspend fun toggleFollow(followerId: String, followingId: String, isFollowing: Boolean) {
        val followId = "${followerId}_${followingId}"
        val followRef = db.collection("follows").document(followId)
        if (isFollowing) {
            val follow = FireFollow(id = followId, followerId = followerId, followingId = followingId)
            followRef.set(follow).await()
        } else {
            followRef.delete().await()
        }
    }

    // --- Reading Status ---
    suspend fun updateReadingStatus(userId: String, bookId: String, status: String) {
        val statusId = "${userId}_${bookId}"
        val readingStatus = FireReadingStatus(id = statusId, userId = userId, bookId = bookId, status = status)
        db.collection("readingStatuses").document(statusId).set(readingStatus).await()
    }

    // --- Comment Operations ---
    suspend fun addComment(comment: FireComment): String {
        val docRef = db.collection("comments").document()
        val commentWithId = comment.copy(id = docRef.id)
        docRef.set(commentWithId).await()
        return docRef.id
    }

    suspend fun getCommentsForTable(tableId: String): List<FireComment> {
        return db.collection("comments")
            .whereEqualTo("tableId", tableId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get().await().toObjects(FireComment::class.java)
    }

    // --- Booklist Operations ---
    suspend fun createBookList(bookList: FireBookList): String {
        val docRef = db.collection("booklists").document()
        val listWithId = bookList.copy(id = docRef.id)
        docRef.set(listWithId).await()
        return docRef.id
    }

    suspend fun addBookToBookList(listId: String, bookId: String) {
        val entryId = "${listId}_${bookId}"
        val data = mapOf(
            "booklistId" to listId,
            "bookId" to bookId,
            "createdAt" to Timestamp.now()
        )
        db.collection("booklist_books").document(entryId).set(data).await()
    }

    // --- Favourites ---
    suspend fun updateFavouriteBooks(userId: String, bookIds: List<String>) {
        val favs = FireFavouriteBooks(userId = userId, bookIds = bookIds)
        db.collection("favouriteBooks").document(userId).set(favs).await()
    }
}
