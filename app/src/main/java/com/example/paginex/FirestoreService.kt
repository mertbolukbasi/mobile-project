package com.example.paginex

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "FirestoreService"

    /**
     * Initializes Firestore with mock data based on the provided schema image.
     */
    suspend fun initializeData() {
        try {
            val usersSnap = db.collection("users").get().await()
            if (!usersSnap.isEmpty) {
                Log.d(TAG, "Database already initialized. Skipping.")
                return
            }

            Log.d(TAG, "Initializing Firestore with schema-aligned mock data...")

            // 1. Books
            val fireBooks = MockData.sampleBooks.map { book ->
                FireBook(
                    id = book.id,
                    title = book.title,
                    author = book.author,
                    genre = book.genre,
                    summary = book.summary,
                    isbn = book.isbn,
                    rating = 0.0, // Initial rating
                    publishYear = Timestamp(Date(book.publishYear - 1900, 0, 1)), // Rough conversion
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
            }
            fireBooks.forEach { db.collection("books").document(it.id).set(it).await() }

            // 2. Users
            val fireUsers = listOf(
                FireUser(id = "u1", username = "ayseyilmaz", name = "Ayşe", surname = "Yılmaz", email = "ayse@example.com"),
                FireUser(id = "u2", username = "mehmet_okur", name = "Mehmet", surname = "Okur", email = "mehmet@example.com"),
                FireUser(id = "u3", username = "canan_dağ", name = "Canan", surname = "Dağ", email = "canan@example.com"),
                FireUser(id = "u4", username = "elif_kitap", name = "Elif", surname = "Kaya", email = "elif@example.com")
            )
            fireUsers.forEach { db.collection("users").document(it.id).set(it).await() }

            // 3. Posts (Mapping UI Post to FirePost)
            // Note: Adding bookId to FirePost to maintain app functionality
            MockData.feedPosts.forEach { post ->
                val firePost = hashMapOf(
                    "id" to post.id,
                    "userId" to post.userId,
                    "description" to post.review,
                    "rating" to post.rating.toDouble(),
                    "bookId" to post.book.id, // Extension to schema for app compatibility
                    "createdAt" to Timestamp(Date(post.createdAt)),
                    "updatedAt" to Timestamp.now()
                )
                db.collection("posts").document(post.id).set(firePost).await()
            }

            // 4. Booklists
            MockData.sampleBookLists.forEach { list ->
                val fireList = FireBookList(
                    id = list.id,
                    name = list.name,
                    description = list.description,
                    userId = "u1",
                    isPrivate = false
                )
                db.collection("booklists").document(list.id).set(fireList).await()
                
                // 5. Booklist-Book (Junction)
                list.books.forEach { book ->
                    val junctionId = "${list.id}_${book.id}"
                    val junction = FireBookListBook(
                        id = junctionId,
                        booklistId = list.id,
                        bookId = book.id
                    )
                    db.collection("booklist_books").document(junctionId).set(junction).await()
                }
            }

            // 6. FavouriteBooks
            val favs = FireFavouriteBooks(
                id = "fav1",
                userId = "u1",
                bookIds = listOf("b1", "b2", "b3")
            )
            db.collection("favourite_books").document(favs.id).set(favs).await()

            // 7. ReadingStatus
            val status = FireReadingStatus(
                id = "rs1",
                userId = "u1",
                bookId = "b1",
                status = "Completed"
            )
            db.collection("reading_statuses").document(status.id).set(status).await()

            Log.d(TAG, "Full schema initialization complete!")
        } catch (e: Exception) {
            Log.e(TAG, "Error during schema initialization", e)
        }
    }

    suspend fun getFeed(): List<Post> {
        return try {
            val postsSnap = db.collection("posts").orderBy("createdAt").get().await()
            val posts = mutableListOf<Post>()
            
            for (doc in postsSnap.documents) {
                val id = doc.getString("id") ?: ""
                val userId = doc.getString("userId") ?: ""
                val description = doc.getString("description") ?: ""
                val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                val bookId = doc.getString("bookId") ?: ""
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                
                // Fetch book details to reconstruct UI Post object
                val bookDoc = db.collection("books").document(bookId).get().await()
                val fireBook = bookDoc.toObject(FireBook::class.java)
                
                if (fireBook != null) {
                    val book = Book(
                        id = fireBook.id,
                        title = fireBook.title,
                        author = fireBook.author,
                        coverUrl = MockData.sampleBooks.find { it.id == fireBook.id }?.coverUrl ?: "",
                        genre = fireBook.genre,
                        publishYear = 2020, // Simplified
                        summary = fireBook.summary,
                        isbn = fireBook.isbn
                    )
                    
                    posts.add(Post(
                        id = id,
                        userId = userId,
                        book = book,
                        status = "Okundu", // Placeholder
                        rating = rating,
                        review = description,
                        createdAt = createdAt
                    ))
                }
            }
            posts
        } catch (e: Exception) {
            Log.e(TAG, "Error getting feed", e)
            emptyList()
        }
    }

    suspend fun getUserProfile(userId: String): User? {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            val fireUser = userDoc.toObject(FireUser::class.java)
            if (fireUser != null) {
                User(
                    id = fireUser.id,
                    username = fireUser.username,
                    fullName = "${fireUser.name} ${fireUser.surname}",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
                    bio = "Paginex kullanıcısı"
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }

    suspend fun createPost(post: Post): Boolean {
        return try {
            val firePost = hashMapOf(
                "id" to post.id,
                "userId" to post.userId,
                "description" to post.review,
                "rating" to post.rating.toDouble(),
                "bookId" to post.book.id,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            db.collection("posts").document(post.id).set(firePost).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post", e)
            false
        }
    }

    suspend fun syncMockData() {
        try {
            // 1. Sync Books
            val booksSnap = db.collection("books").get().await()
            val fireBooks = booksSnap.toObjects(FireBook::class.java)
            MockData.sampleBooks.clear()
            MockData.sampleBooks.addAll(fireBooks.map { fb ->
                Book(
                    id = fb.id,
                    title = fb.title,
                    author = fb.author,
                    coverUrl = "https://images.unsplash.com/photo-1541963463532-d68292c34b19?auto=format&fit=crop&q=80&w=400", // Fallback
                    genre = fb.genre,
                    publishYear = fb.publishYear?.toDate()?.let { 
                        val cal = Calendar.getInstance()
                        cal.time = it
                        cal.get(Calendar.YEAR)
                    } ?: 2020,
                    summary = fb.summary,
                    isbn = fb.isbn
                )
            })

            // 2. Sync Feed Posts
            val posts = getFeed()
            MockData.feedPosts.clear()
            MockData.feedPosts.addAll(posts)
            
            // 3. Sync Current User
            val user = getUserProfile("u1")
            if (user != null) {
                MockData.currentUser = user
            }

            // 4. Sync BookLists
            val listsSnap = db.collection("booklists").get().await()
            val fireLists = listsSnap.toObjects(FireBookList::class.java)
            MockData.sampleBookLists.clear()
            fireLists.forEach { fl ->
                val booksInList = mutableListOf<Book>()
                // Fetch junction data
                val junctionSnap = db.collection("booklist_books").whereEqualTo("booklistId", fl.id).get().await()
                junctionSnap.documents.forEach { jDoc ->
                    val bId = jDoc.getString("bookId")
                    MockData.sampleBooks.find { it.id == bId }?.let { booksInList.add(it) }
                }
                
                MockData.sampleBookLists.add(BookList(
                    id = fl.id,
                    name = fl.name,
                    description = fl.description,
                    coverUrl = booksInList.firstOrNull()?.coverUrl ?: "",
                    books = booksInList
                ))
            }

            Log.d(TAG, "MockData fully synced with Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing MockData", e)
        }
    }
}
