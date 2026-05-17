package com.example.paginex

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "FirestoreService"
    private var usersListener: ListenerRegistration? = null
    
    var lastVisibleFeedDoc: DocumentSnapshot? = null
    var isFeedEndReached = false

    private fun deletedUserPlaceholder(userId: String) = User(
        id = userId,
        username = "deleted_user",
        fullName = "Deleted user",
        avatarUrl = "",
        bio = "",
        location = "",
        joinDate = "",
        followingCount = 0,
        followersCount = 0,
        favoriteBooks = emptyList(),
        isActive = false
    )

    private fun userFromFirestoreDoc(doc: DocumentSnapshot): User? {
        val fu = doc.toObject(FireUser::class.java) ?: return null
        val id = fu.id.takeIf { it.isNotBlank() } ?: doc.id
        if (id.isBlank()) return null
        val active = fu.isActive
        return User(
            id = id,
            username = if (active) fu.username else "deleted_user",
            fullName = if (active) "${fu.name} ${fu.surname}" else "Deleted user",
            avatarUrl = if (active) fu.avatarUrl else "",
            bio = if (active) fu.bio else "",
            location = if (active) fu.location else "",
            joinDate = "",
            followingCount = 0,
            followersCount = 0,
            favoriteBooks = if (active) fu.favoriteBooks else emptyList(),
            isActive = active
        )
    }

    /** Keeps [AppCache.users] in sync when users are added/removed in Firestore (e.g. console delete). */
    fun attachUsersRealtimeListener() {
        usersListener?.remove()
        usersListener = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "users snapshot listener error", error)
                return@addSnapshotListener
            }
            val snap = snapshot ?: return@addSnapshotListener
            val list = snap.documents.mapNotNull { userFromFirestoreDoc(it) }.filter { it.isActive }
            AppCache.users.clear()
            AppCache.users.addAll(list)
        }
    }

    fun detachUsersRealtimeListener() {
        usersListener?.remove()
        usersListener = null
    }

    // ========================
    // SEED DATA INITIALIZATION
    suspend fun initializeData(forceReset: Boolean = false) {
        try {
            if (!forceReset) {
                val usersSnap = db.collection("users").get().await()
                if (!usersSnap.isEmpty) {
                    Log.d(TAG, "Database already initialized. Skipping.")
                    return
                }
            } else {
                Log.d(TAG, "Force resetting database. Deleting all seed data...")
                val collectionsToClear = listOf("books", "users", "posts", "drafts", "booklists", "booklist_books", "favourite_books", "reading_statuses", "explore_images", "follows", "likes", "saves", "comments")
                collectionsToClear.forEach { col ->
                    val docs = db.collection(col).get().await()
                    docs.forEach { it.reference.delete() }
                }
            }

            Log.d(TAG, "Initializing Firestore with seed data...")

            val seedBooks = listOf(
                FireBook(id = "b1", title = "1984", author = "George Orwell",
                    genre = "Dystopian", summary = "A chilling depiction of a totalitarian future where individuality is erased and every move is monitored.",
                    isbn = "978-0451524935",
                    coverUrl = "https://images.unsplash.com/photo-1541963463532-d68292c34b19?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1949 - 1900, 0, 1))),
                FireBook(id = "b2", title = "Sapiens", author = "Yuval Noah Harari",
                    genre = "History", summary = "A masterpiece exploring the evolution and cultural development of humanity from its origins to the present.",
                    isbn = "978-0062316097",
                    coverUrl = "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(2011 - 1900, 0, 1))),
                FireBook(id = "b3", title = "The Little Prince", author = "Antoine de Saint-Exupéry",
                    genre = "Classic", summary = "A timeless tale showing the absurdity of the adult world through the innocent eyes of a child.",
                    isbn = "978-0156012195",
                    coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1943 - 1900, 0, 1))),
                FireBook(id = "b4", title = "Dune", author = "Frank Herbert",
                    genre = "Sci-Fi", summary = "A cult classic sci-fi masterpiece blending politics, religion, and ecology on the desert planet of Arrakis.",
                    isbn = "978-0441013593",
                    coverUrl = "https://images.unsplash.com/photo-1592496431122-23492f92273e?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1965 - 1900, 0, 1))),
                FireBook(id = "b5", title = "Crime and Punishment", author = "Fyodor Dostoevsky",
                    genre = "Classic", summary = "A deep psychological analysis of a young man tormented by guilt and spiritual breakdown after committing murder.",
                    isbn = "978-0486415871",
                    coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1866 - 1900, 0, 1))),
                FireBook(id = "b6", title = "Harry Potter and the Sorcerer's Stone", author = "J.K. Rowling",
                    genre = "Fantasy", summary = "The beginning of a magical journey following a young wizard's first year at Hogwarts School of Witchcraft and Wizardry.",
                    isbn = "978-0439708180",
                    coverUrl = "https://images.unsplash.com/photo-1589998059171-988d887df646?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1997 - 1900, 0, 1))),
                FireBook(id = "b7", title = "Brave New World", author = "Aldous Huxley",
                    genre = "Dystopian", summary = "A vision of a future where babies are produced in factories, emotions are suppressed, and everyone is condemned to fake happiness.",
                    isbn = "978-0060850524",
                    coverUrl = "https://images.unsplash.com/photo-1532012197367-68bf563a34a1?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1932 - 1900, 0, 1))),
                FireBook(id = "b8", title = "The Alchemist", author = "Paulo Coelho",
                    genre = "Philosophy", summary = "The philosophical and spiritual journey of the shepherd Santiago seeking his destiny by the Egyptian Pyramids.",
                    isbn = "978-0061122415",
                    coverUrl = "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1988 - 1900, 0, 1))),
                // --- 20 NEW BOOKS ---
                FireBook(id = "b9", title = "To Kill a Mockingbird", author = "Harper Lee",
                    genre = "Classic", summary = "A powerful exploration of racial injustice in the American South through the eyes of young Scout Finch.",
                    isbn = "978-0061120084",
                    coverUrl = "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1960 - 1900, 0, 1))),
                FireBook(id = "b10", title = "The Great Gatsby", author = "F. Scott Fitzgerald",
                    genre = "Classic", summary = "A tragic story of wealth, love, and the American Dream set in the Jazz Age.",
                    isbn = "978-0743273565",
                    coverUrl = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1925 - 1900, 0, 1))),
                FireBook(id = "b11", title = "The Hobbit", author = "J.R.R. Tolkien",
                    genre = "Fantasy", summary = "Bilbo Baggins embarks on an unexpected adventure with a company of dwarves to reclaim a lost kingdom.",
                    isbn = "978-0547928227",
                    coverUrl = "https://images.unsplash.com/photo-1524578271613-d550eabcad51?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1937 - 1900, 0, 1))),
                FireBook(id = "b12", title = "Pride and Prejudice", author = "Jane Austen",
                    genre = "Romance", summary = "A witty and romantic tale of Elizabeth Bennet and Mr. Darcy navigating society and their own prejudices.",
                    isbn = "978-0141439518",
                    coverUrl = "https://images.unsplash.com/photo-1550399105-05c4a7641adb?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1813 - 1900, 0, 1))),
                FireBook(id = "b13", title = "The Catcher in the Rye", author = "J.D. Salinger",
                    genre = "Classic", summary = "A teenage boy navigates the complexities of growing up in a world he sees as phony.",
                    isbn = "978-0316769488",
                    coverUrl = "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1951 - 1900, 0, 1))),
                FireBook(id = "b14", title = "Fahrenheit 451", author = "Ray Bradbury",
                    genre = "Dystopian", summary = "In a future where books are banned and burned, one fireman begins to question everything.",
                    isbn = "978-1451673319",
                    coverUrl = "https://images.unsplash.com/photo-1509021436665-8f37df706ae7?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1953 - 1900, 0, 1))),
                FireBook(id = "b15", title = "The Lord of the Rings", author = "J.R.R. Tolkien",
                    genre = "Fantasy", summary = "An epic high-fantasy tale of the quest to destroy the One Ring and save Middle-earth.",
                    isbn = "978-0618640157",
                    coverUrl = "https://images.unsplash.com/photo-1491843343513-39908cf2ce6f?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1954 - 1900, 0, 1))),
                FireBook(id = "b16", title = "Animal Farm", author = "George Orwell",
                    genre = "Dystopian", summary = "A satirical allegory about power and corruption using a farm of rebellious animals.",
                    isbn = "978-0451526342",
                    coverUrl = "https://images.unsplash.com/photo-1543005127-b01676674291?auto=format&fit=crop&q=80&w=400",
                    publishYear = Timestamp(Date(1945 - 1900, 0, 1))),
                FireBook(id = "b17", title = "The Road", author = "Cormac McCarthy",
                    genre = "Post-Apocalyptic", summary = "A father and son traverse a desolate landscape in a harrowing tale of survival and love.",
                    isbn = "978-0307387899",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2006 - 1900, 0, 1))),
                FireBook(id = "b18", title = "Educated", author = "Tara Westover",
                    genre = "Memoir", summary = "A remarkable memoir of a woman who grows up in a survivalist family and eventually earns a PhD from Cambridge.",
                    isbn = "978-0399590504",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2018 - 1900, 0, 1))),
                FireBook(id = "b19", title = "Atomic Habits", author = "James Clear",
                    genre = "Self-Help", summary = "A practical guide to building good habits, breaking bad ones, and mastering tiny behaviors that lead to remarkable results.",
                    isbn = "978-0735211292",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2018 - 1900, 0, 1))),
                FireBook(id = "b20", title = "Project Hail Mary", author = "Andy Weir",
                    genre = "Sci-Fi", summary = "A lone astronaut must save Earth from an extinction-level threat in this thrilling interstellar adventure.",
                    isbn = "978-0593135204",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2021 - 1900, 0, 1))),
                FireBook(id = "b21", title = "The Midnight Library", author = "Matt Haig",
                    genre = "Fiction", summary = "Between life and death, Nora Seed discovers a library containing books of every life she could have lived.",
                    isbn = "978-0525559474",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2020 - 1900, 0, 1))),
                FireBook(id = "b22", title = "Thinking, Fast and Slow", author = "Daniel Kahneman",
                    genre = "Psychology", summary = "A groundbreaking exploration of the two systems that drive the way we think and make decisions.",
                    isbn = "978-0374533557",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2011 - 1900, 0, 1))),
                FireBook(id = "b23", title = "Frankenstein", author = "Mary Shelley",
                    genre = "Horror", summary = "The original science fiction novel about a scientist who creates life and faces the monstrous consequences.",
                    isbn = "978-0486282114",
                    coverUrl = "",
                    publishYear = Timestamp(Date(1818 - 1900, 0, 1))),
                FireBook(id = "b24", title = "The Name of the Wind", author = "Patrick Rothfuss",
                    genre = "Fantasy", summary = "A legendary figure tells the true story of his life, from orphaned childhood to his rise as the most notorious wizard.",
                    isbn = "978-0756404741",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2007 - 1900, 0, 1))),
                FireBook(id = "b25", title = "Meditations", author = "Marcus Aurelius",
                    genre = "Philosophy", summary = "Personal writings of the Roman Emperor reflecting on Stoic philosophy and the human condition.",
                    isbn = "978-0140449334",
                    coverUrl = "",
                    publishYear = Timestamp(Date(180 - 1900, 0, 1))),
                FireBook(id = "b26", title = "Gone Girl", author = "Gillian Flynn",
                    genre = "Thriller", summary = "A wickedly sharp thriller about a marriage gone terribly wrong and the dark secrets both partners hide.",
                    isbn = "978-0307588371",
                    coverUrl = "",
                    publishYear = Timestamp(Date(2012 - 1900, 0, 1))),
                FireBook(id = "b27", title = "The Handmaid's Tale", author = "Margaret Atwood",
                    genre = "Dystopian", summary = "In a totalitarian theocracy, women are stripped of their rights and forced into servitude.",
                    isbn = "978-0385490818",
                    coverUrl = "",
                    publishYear = Timestamp(Date(1985 - 1900, 0, 1))),
                FireBook(id = "b28", title = "Slaughterhouse-Five", author = "Kurt Vonnegut",
                    genre = "Sci-Fi", summary = "A satirical novel about a soldier who becomes unstuck in time, blending war, free will, and absurdity.",
                    isbn = "978-0385333849",
                    coverUrl = "",
                    publishYear = Timestamp(Date(1969 - 1900, 0, 1)))
            )
            seedBooks.forEach { db.collection("books").document(it.id).set(it).await() }

            // 8. Explore Images
            val exploreUrls = listOf(
                "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1589998059171-988d887df646?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1543005127-b01676674291?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1532012197367-68bf563a34a1?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1524578271613-d550eabcad51?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1550399105-05c4a7641adb?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1509021436665-8f37df706ae7?auto=format&fit=crop&q=80&w=400",
                "https://images.unsplash.com/photo-1491843343513-39908cf2ce6f?auto=format&fit=crop&q=80&w=400"
            )
            exploreUrls.forEachIndexed { index, url ->
                db.collection("explore_images").document("exp_$index").set(
                    FireImage(id = "exp_$index", path = url, tableId = "explore")
                ).await()
            }
            Log.d(TAG, "Full seed initialization complete!")
        } catch (e: Exception) {
            Log.e(TAG, "Error during seed initialization", e)
        }
    }

    // ========================
    // READ OPERATIONS
    // ========================

    suspend fun getFeed(limit: Int = 10, isNextPage: Boolean = false): List<Post> {
        return try {
            var query = db.collection("posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (isNextPage && lastVisibleFeedDoc != null) {
                query = query.startAfter(lastVisibleFeedDoc!!)
            } else if (!isNextPage) {
                lastVisibleFeedDoc = null
                isFeedEndReached = false
            }

            val postsSnap = query.get().await()
            if (postsSnap.isEmpty) {
                isFeedEndReached = true
                return emptyList()
            }

            lastVisibleFeedDoc = postsSnap.documents.lastOrNull()
            if (postsSnap.size() < limit) {
                isFeedEndReached = true
            }

            val posts = mutableListOf<Post>()
            val currentUserId = AuthService.getUid()

            // Optimization: Fetch all books once and cache them
            val booksSnap = db.collection("books").get().await()
            val booksMap = booksSnap.toObjects(FireBook::class.java).associateBy { it.id }

            // Optimization: Fetch all likes, saves for the current user once
            val userLikesSnap = db.collection("likes").whereEqualTo("userId", currentUserId).get().await()
            val userLikedTableIds = userLikesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()

            val userSavesSnap = db.collection("saves").whereEqualTo("userId", currentUserId).get().await()
            val userSavedTableIds = userSavesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()

            // Optimization: Fetch all likes and comments to get counts in bulk
            val allLikesSnap = db.collection("likes").get().await()
            val likesCountMap = allLikesSnap.documents.groupBy { it.getString("tableId") ?: "" }.mapValues { it.value.size }

            val allCommentsSnap = db.collection("comments").get().await()
            val commentsCountMap = allCommentsSnap.documents.groupBy { it.getString("tableId") ?: "" }.mapValues { it.value.size }
            
            for (doc in postsSnap.documents) {
                val id = doc.getString("id") ?: ""
                val userId = doc.getString("userId") ?: ""
                val description = doc.getString("description") ?: ""
                val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                val bookId = doc.getString("bookId") ?: ""
                val status = doc.getString("status") ?: "Plan To Read"
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                val booklistId = doc.getString("booklistId")

                val fireBook = booksMap[bookId]
                val isLiked = userLikedTableIds.contains(id)
                val isSaved = userSavedTableIds.contains(id)

                val totalLikes = likesCountMap[id] ?: 0
                val totalComments = commentsCountMap[id] ?: 0

                if (fireBook != null && booklistId.isNullOrEmpty()) {
                    val book = Book(
                        id = fireBook.id,
                        title = fireBook.title,
                        author = fireBook.author,
                        coverUrl = fireBook.coverUrl,
                        genre = normalizeGenre(fireBook.genre),
                        publishYear = fireBook.publishYear?.toDate()?.let {
                            val cal = Calendar.getInstance()
                            cal.time = it
                            cal.get(Calendar.YEAR)
                        } ?: 2020,
                        summary = fireBook.summary,
                        isbn = fireBook.isbn
                    )

                    posts.add(Post(
                        id = id,
                        userId = userId,
                        book = book,
                        status = status,
                        rating = rating,
                        review = description,
                        createdAt = createdAt,
                        isLiked = isLiked,
                        isSaved = isSaved,
                        likesCount = totalLikes,
                        commentsCount = totalComments
                    ))
                } else if (!booklistId.isNullOrEmpty()) {
                    // Try to fetch booklist
                    val listDoc = try { db.collection("booklists").document(booklistId).get().await() } catch (e: Exception) { null }
                    if (listDoc != null && listDoc.exists()) {
                        val fireList = listDoc.toObject(FireBookList::class.java)
                        if (fireList != null) {
                            // Skip private booklist posts from other users
                            if (fireList.isPrivate && userId != currentUserId) {
                                continue
                            }
                            val booksInList = mutableListOf<Book>()
                            val junctionSnap = try { db.collection("booklist_books").whereEqualTo("booklistId", fireList.id).get().await() } catch(e: Exception) { null }
                            junctionSnap?.documents?.forEach { jDoc ->
                                val bId = jDoc.getString("bookId")
                                booksMap[bId]?.let { fb -> 
                                    booksInList.add(Book(
                                        id = fb.id, title = fb.title, author = fb.author, coverUrl = fb.coverUrl
                                    ))
                                }
                            }
                            val resolvedCoverUrl = fireList.coverUrl.ifEmpty { booksInList.firstOrNull()?.coverUrl ?: "" }
                            val loadedList = BookList(
                                id = fireList.id,
                                userId = fireList.userId,
                                name = fireList.name,
                                description = fireList.description,
                                coverUrl = resolvedCoverUrl,
                                isPrivate = fireList.isPrivate,
                                likesCount = 0,
                                isLiked = false,
                                isSaved = false,
                                createdAt = fireList.createdAt.toDate().time,
                                books = booksInList
                            )
                            val dummyBook = Book(
                                id = loadedList.id,
                                title = loadedList.name,
                                author = "List by author",
                                coverUrl = loadedList.coverUrl,
                                genre = "Booklist"
                            )
                            posts.add(Post(
                                id = id,
                                userId = userId,
                                book = dummyBook,
                                status = status,
                                rating = rating,
                                review = description,
                                createdAt = createdAt,
                                isLiked = isLiked,
                                isSaved = isSaved,
                                likesCount = totalLikes,
                                commentsCount = totalComments,
                                isBooklistPost = true,
                                booklist = loadedList
                            ))
                        }
                    }
                }
            }
            Log.d(TAG, "getFeed loaded ${posts.size} posts successfully")
            posts
        } catch (e: Exception) {
            Log.e(TAG, "Error getting feed", e)
            emptyList()
        }
    }

    suspend fun loadMoreFeed() {
        if (isFeedEndReached) return
        val newPosts = getFeed(isNextPage = true)
        if (newPosts.isNotEmpty()) {
            val existingIds = AppCache.feedPosts.map { it.id }.toSet()
            AppCache.feedPosts.addAll(newPosts.filter { !existingIds.contains(it.id) })
        }
    }

    suspend fun getUserPosts(userId: String, limit: Int = 10, startAfterDoc: DocumentSnapshot? = null): Pair<List<Post>, DocumentSnapshot?> {
        return try {
            var query = db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())

            if (startAfterDoc != null) {
                query = query.startAfter(startAfterDoc)
            }

            val postsSnap = query.get().await()
            if (postsSnap.isEmpty) {
                return Pair(emptyList(), startAfterDoc)
            }

            val lastDoc = postsSnap.documents.lastOrNull()

            val posts = mutableListOf<Post>()
            val currentUserId = AuthService.getUid()

            val booksSnap = db.collection("books").get().await()
            val booksMap = booksSnap.toObjects(FireBook::class.java).associateBy { it.id }

            val userLikesSnap = db.collection("likes").whereEqualTo("userId", currentUserId).get().await()
            val userLikedTableIds = userLikesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()

            val userSavesSnap = db.collection("saves").whereEqualTo("userId", currentUserId).get().await()
            val userSavedTableIds = userSavesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()

            val allLikesSnap = db.collection("likes").get().await()
            val likesCountMap = allLikesSnap.documents.groupBy { it.getString("tableId") ?: "" }.mapValues { it.value.size }

            val allCommentsSnap = db.collection("comments").get().await()
            val commentsCountMap = allCommentsSnap.documents.groupBy { it.getString("tableId") ?: "" }.mapValues { it.value.size }

            for (doc in postsSnap.documents) {
                val id = doc.getString("id") ?: ""
                val postUserId = doc.getString("userId") ?: ""
                val description = doc.getString("description") ?: ""
                val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                val bookId = doc.getString("bookId") ?: ""
                val status = doc.getString("status") ?: "Plan To Read"
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                val booklistId = doc.getString("booklistId")

                val fireBook = booksMap[bookId]
                val isLiked = userLikedTableIds.contains(id)
                val isSaved = userSavedTableIds.contains(id)

                val totalLikes = likesCountMap[id] ?: 0
                val totalComments = commentsCountMap[id] ?: 0

                if (fireBook != null && booklistId.isNullOrEmpty()) {
                    val book = Book(
                        id = fireBook.id, title = fireBook.title, author = fireBook.author, coverUrl = fireBook.coverUrl,
                        genre = normalizeGenre(fireBook.genre), publishYear = fireBook.publishYear?.toDate()?.let {
                            val cal = Calendar.getInstance(); cal.time = it; cal.get(Calendar.YEAR)
                        } ?: 2020, summary = fireBook.summary, isbn = fireBook.isbn
                    )
                    posts.add(Post(
                        id = id, userId = postUserId, book = book, status = status, rating = rating, review = description,
                        createdAt = createdAt, isLiked = isLiked, isSaved = isSaved, likesCount = totalLikes, commentsCount = totalComments
                    ))
                }
            }
            Pair(posts, lastDoc)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user posts", e)
            Pair(emptyList(), startAfterDoc)
        }
    }

    suspend fun getUserProfile(userId: String): User? {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                return deletedUserPlaceholder(userId)
            }
            val fireUser = userDoc.toObject(FireUser::class.java)
                ?: return deletedUserPlaceholder(userId)
            val active = fireUser.isActive
            val followersCount = db.collection("follows")
                .whereEqualTo("followingId", userId)
                .get().await().size()
            val followingCount = db.collection("follows")
                .whereEqualTo("followerId", userId)
                .get().await().size()

            User(
                id = fireUser.id.ifBlank { userId },
                username = if (active) fireUser.username else "deleted_user",
                fullName = if (active) "${fireUser.name} ${fireUser.surname}" else "Deleted user",
                avatarUrl = if (active) fireUser.avatarUrl else "",
                bio = if (active) fireUser.bio else "",
                location = if (active) fireUser.location else "",
                joinDate = if (active) {
                    "Joined: ${fireUser.createdAt.toDate().let {
                        val cal = Calendar.getInstance(); cal.time = it
                        val months = listOf("January","February","March","April","May","June","July","August","September","October","November","December")
                        "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
                    }}"
                } else "",
                followersCount = followersCount,
                followingCount = followingCount,
                favoriteBooks = if (active) fireUser.favoriteBooks else emptyList(),
                isActive = active
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }

    suspend fun getUserById(userId: String): FireUser? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (!doc.exists()) return null
            val u = doc.toObject(FireUser::class.java) ?: return null
            if (u.id.isBlank()) u.copy(id = doc.id) else u
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by id", e)
            null
        }
    }

    suspend fun getBooks(): List<Book> {
        return try {
            val snap = db.collection("books").get().await()
            snap.toObjects(FireBook::class.java).map { fb ->
                Book(
                    id = fb.id,
                    title = fb.title,
                    author = fb.author,
                    coverUrl = fb.coverUrl,
                    genre = normalizeGenre(fb.genre),
                    publishYear = fb.publishYear?.toDate()?.let {
                        val cal = Calendar.getInstance(); cal.time = it
                        cal.get(Calendar.YEAR)
                    } ?: 2020,
                    summary = fb.summary,
                    isbn = fb.isbn
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting books", e)
            emptyList()
        }
    }

    suspend fun getReadingStatuses(): List<ReadingStatus> {
        return try {
            val snap = db.collection("reading_statuses").get().await()
            val booksSnap = db.collection("books").get().await()
            val booksMap = booksSnap.toObjects(FireBook::class.java).associateBy { it.id }

            snap.documents.mapNotNull { doc ->
                val fireBook = booksMap[doc.getString("bookId") ?: ""] ?: return@mapNotNull null
                val book = Book(
                    id = fireBook.id,
                    title = fireBook.title,
                    author = fireBook.author,
                    coverUrl = fireBook.coverUrl,
                    genre = normalizeGenre(fireBook.genre),
                    publishYear = fireBook.publishYear?.toDate()?.let {
                        val cal = Calendar.getInstance(); cal.time = it
                        cal.get(Calendar.YEAR)
                    } ?: 2020,
                    summary = fireBook.summary,
                    isbn = fireBook.isbn
                )
                ReadingStatus(
                    id = doc.getString("id") ?: "",
                    userId = doc.getString("userId") ?: "",
                    book = book,
                    status = doc.getString("status") ?: "Plan To Read",
                    addedAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reading statuses", e)
            emptyList()
        }
    }

    suspend fun getFollowers(userId: String): List<FireUser> {
        return try {
            val followsSnap = db.collection("follows")
                .whereEqualTo("followingId", userId)
                .get().await()
            val followerIds = followsSnap.documents.mapNotNull { it.getString("followerId") }
            followerIds.mapNotNull { getUserById(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting followers", e)
            emptyList()
        }
    }

    suspend fun getFollowing(userId: String): List<FireUser> {
        return try {
            val followsSnap = db.collection("follows")
                .whereEqualTo("followerId", userId)
                .get().await()
            val followingIds = followsSnap.documents.mapNotNull { it.getString("followingId") }
            followingIds.mapNotNull { getUserById(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting following", e)
            emptyList()
        }
    }

    suspend fun getComments(tableId: String): List<FireComment> {
        return try {
            val commentsSnap = db.collection("comments")
                .whereEqualTo("tableId", tableId)
                .get().await()

            commentsSnap.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FireComment::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deserializing comment ${doc.id}", e)
                    null
                }
            }.sortedBy { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting comments", e)
            emptyList()
        }
    }

    suspend fun toggleCommentLike(commentId: String, userId: String, isLiked: Boolean): Boolean {
        return try {
            val likeId = "c_like_${userId}_${commentId}"
            if (isLiked) {
                val newLike = FireLike(id = likeId, userId = userId, tableId = commentId, createdAt = Timestamp.now())
                db.collection("likes").document(likeId).set(newLike).await()
            } else {
                db.collection("likes").document(likeId).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling comment like", e)
            false
        }
    }

    suspend fun getLikeCount(tableId: String): Int {
        return try {
            val snap = db.collection("likes").whereEqualTo("tableId", tableId).get().await()
            snap.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun isLikedByUser(tableId: String, userId: String): Boolean {
        return try {
            val snap = db.collection("likes")
                .whereEqualTo("tableId", tableId)
                .whereEqualTo("userId", userId)
                .get().await()
            !snap.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // ========================
    // WRITE OPERATIONS
    // ========================

    suspend fun toggleLike(postId: String, userId: String, isLiked: Boolean): Boolean {
        return try {
            val likeId = "${userId}_${postId}"
            if (isLiked) {
                val newLike = FireLike(id = likeId, userId = userId, tableId = postId, createdAt = Timestamp.now())
                db.collection("likes").document(likeId).set(newLike).await()
            } else {
                db.collection("likes").document(likeId).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
            false
        }
    }

    suspend fun toggleSave(postId: String, userId: String, isSaved: Boolean): Boolean {
        return try {
            val saveId = "post_save_${userId}_${postId}"
            if (isSaved) {
                val newSave = FireSave(id = saveId, userId = userId, tableId = postId, createdAt = Timestamp.now())
                db.collection("saves").document(saveId).set(newSave).await()
            } else {
                db.collection("saves").document(saveId).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling save", e)
            false
        }
    }

    suspend fun toggleBookListLike(listId: String, userId: String, isLiked: Boolean): Boolean {
        return try {
            val likeId = "bl_like_${userId}_${listId}"
            if (isLiked) {
                val newLike = FireLike(id = likeId, userId = userId, tableId = listId, createdAt = Timestamp.now())
                db.collection("likes").document(likeId).set(newLike).await()
            } else {
                db.collection("likes").document(likeId).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling book list like", e)
            false
        }
    }

    suspend fun toggleBookSave(bookId: String, userId: String, isSaved: Boolean): Boolean {
        return try {
            val saveId = "save_${userId}_${bookId}"
            if (isSaved) {
                val newSave = FireSave(id = saveId, userId = userId, tableId = bookId, createdAt = Timestamp.now())
                db.collection("saves").document(saveId).set(newSave).await()
            } else {
                db.collection("saves").document(saveId).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling book save", e)
            false
        }
    }

    suspend fun toggleBookListSave(listId: String, userId: String, isSaved: Boolean): Boolean {
        return try {
            val saveId = "bl_save_${userId}_${listId}"
            if (isSaved) {
                val newSave = FireSave(id = saveId, userId = userId, tableId = listId, createdAt = Timestamp.now())
                db.collection("saves").document(saveId).set(newSave).await()
            } else {
                db.collection("saves").document(saveId).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling book list save", e)
            false
        }
    }

    suspend fun isFollowing(followerId: String, followingId: String): Boolean {
        if (followerId.isBlank() || followingId.isBlank() || followerId == followingId) return false
        return try {
            val snap = db.collection("follows")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get().await()
            !snap.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking following status", e)
            false
        }
    }

    suspend fun followUser(followerId: String, followingId: String): Boolean {
        if (followerId.isBlank() || followingId.isBlank() || followerId == followingId) {
            Log.w(TAG, "followUser skipped: blank ids or self-follow")
            return false
        }
        return try {
            val deterministicId = "follow_${followerId}_${followingId}"
            val existing = db.collection("follows")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get().await()
            existing.documents.forEach { snap ->
                if (snap.id != deterministicId) snap.reference.delete().await()
            }
            val follow = FireFollow(id = deterministicId, followerId = followerId, followingId = followingId)
            db.collection("follows").document(deterministicId).set(follow).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error following user", e)
            false
        }
    }

    suspend fun unfollowUser(followerId: String, followingId: String): Boolean {
        if (followerId.isBlank() || followingId.isBlank()) return false
        return try {
            val snap = db.collection("follows")
                .whereEqualTo("followerId", followerId)
                .whereEqualTo("followingId", followingId)
                .get().await()
            snap.documents.forEach { it.reference.delete().await() }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unfollowing user", e)
            false
        }
    }

    suspend fun addComment(comment: FireComment): Boolean {
        return try {
            db.collection("comments").document(comment.id).set(comment).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment", e)
            false
        }
    }

    suspend fun createUserProfile(userId: String, email: String, name: String, surname: String): Boolean {
        return try {
            val newUser = FireUser(
                id = userId,
                username = email.substringBefore("@"),
                name = name,
                surname = surname,
                email = email,
                isActive = true,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            db.collection("users").document(userId).set(newUser).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user profile", e)
            false
        }
    }

    suspend fun updateUserProfile(userId: String, fields: Map<String, Any>): Boolean {
        return try {
            val updates = fields.toMutableMap()
            updates["updatedAt"] = Timestamp.now()
            db.collection("users").document(userId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile", e)
            false
        }
    }

    suspend fun deleteUserData(userId: String): Boolean {
        return try {
            // 1. Anonymize user profile (Soft Delete)
            val updates = mapOf(
                "name" to "Deleted",
                "surname" to "Account",
                "username" to "deleted_account",
                "avatarUrl" to "",
                "bio" to "",
                "isActive" to false,
                "updatedAt" to Timestamp.now()
            )
            db.collection("users").document(userId).update(updates).await()

            // 2. Delete personal reading statuses
            val statusesSnap = db.collection("reading_statuses").whereEqualTo("userId", userId).get().await()
            statusesSnap.documents.forEach { it.reference.delete() } // Without await for speed

            // 3. Delete personal booklists (and their junction records)
            val listsSnap = db.collection("booklists").whereEqualTo("userId", userId).get().await()
            listsSnap.documents.forEach { listDoc ->
                val listId = listDoc.id
                val junctionSnap = db.collection("booklist_books").whereEqualTo("booklistId", listId).get().await()
                junctionSnap.documents.forEach { it.reference.delete() }
                listDoc.reference.delete()
            }

            // 4. Delete follows (where user is follower or following)
            val followingSnap = db.collection("follows").whereEqualTo("followerId", userId).get().await()
            followingSnap.documents.forEach { it.reference.delete() }
            val followersSnap = db.collection("follows").whereEqualTo("followingId", userId).get().await()
            followersSnap.documents.forEach { it.reference.delete() }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up user data during soft delete", e)
            false
        }
    }

    suspend fun uploadProfileImage(userId: String, imageUri: android.net.Uri, context: android.content.Context): String? {
        return try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                // Read bytes from the content URI
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Cannot open input stream for URI: $imageUri")
                val originalBytes = inputStream.readBytes()
                inputStream.close()
                
                Log.d(TAG, "uploadProfileImage: read ${originalBytes.size} bytes for user $userId")
                
                // Compress the image to a reasonable size for profile photo
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                    ?: throw Exception("Cannot decode image bytes")
                val maxDim = 300
                val scale = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height, 1f)
                val scaledBitmap = if (scale < 1f) {
                    android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
                } else bitmap
                
                val compressedStream = java.io.ByteArrayOutputStream()
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, compressedStream)
                val compressedBytes = compressedStream.toByteArray()
                Log.d(TAG, "uploadProfileImage: compressed to ${compressedBytes.size} bytes")
                
                // Try Firebase Storage first
                var downloadUrl: String? = null
                try {
                    val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                        .reference.child("avatars/$userId.jpg")
                    storageRef.putBytes(compressedBytes).await()
                    downloadUrl = storageRef.downloadUrl.await().toString()
                    Log.d(TAG, "uploadProfileImage: Storage upload success, URL = $downloadUrl")
                } catch (storageError: Exception) {
                    Log.w(TAG, "Firebase Storage upload failed, falling back to Base64 data URI", storageError)
                }
                
                // If Storage failed, use Base64 data URI as fallback
                if (downloadUrl == null) {
                    val base64 = android.util.Base64.encodeToString(compressedBytes, android.util.Base64.NO_WRAP)
                    downloadUrl = "data:image/jpeg;base64,$base64"
                    Log.d(TAG, "uploadProfileImage: using Base64 data URI (${downloadUrl.length} chars)")
                }
                
                // Update user's avatarUrl in Firestore
                db.collection("users").document(userId).update("avatarUrl", downloadUrl, "updatedAt", Timestamp.now()).await()
                Log.d(TAG, "uploadProfileImage: Firestore updated for user $userId")
                downloadUrl
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image", e)
            null
        }
    }

    suspend fun uploadBookListCoverImage(listId: String, imageUri: android.net.Uri, context: android.content.Context): String? {
        return try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("Cannot open input stream for URI: $imageUri")
                val originalBytes = inputStream.readBytes()
                inputStream.close()

                Log.d(TAG, "uploadBookListCoverImage: read ${originalBytes.size} bytes for list $listId")

                val bitmap = android.graphics.BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                    ?: throw Exception("Cannot decode image bytes")
                val maxDim = 600
                val scale = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height, 1f)
                val scaledBitmap = if (scale < 1f) {
                    android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
                } else bitmap

                val compressedStream = java.io.ByteArrayOutputStream()
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, compressedStream)
                val compressedBytes = compressedStream.toByteArray()
                Log.d(TAG, "uploadBookListCoverImage: compressed to ${compressedBytes.size} bytes")

                var downloadUrl: String? = null
                try {
                    val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance()
                        .reference.child("booklist_covers/$listId.jpg")
                    storageRef.putBytes(compressedBytes).await()
                    downloadUrl = storageRef.downloadUrl.await().toString()
                    Log.d(TAG, "uploadBookListCoverImage: Storage upload success, URL = $downloadUrl")
                } catch (storageError: Exception) {
                    Log.w(TAG, "Firebase Storage upload failed, falling back to Base64 data URI", storageError)
                }

                if (downloadUrl == null) {
                    val base64 = android.util.Base64.encodeToString(compressedBytes, android.util.Base64.NO_WRAP)
                    downloadUrl = "data:image/jpeg;base64,$base64"
                    Log.d(TAG, "uploadBookListCoverImage: using Base64 data URI (${downloadUrl.length} chars)")
                }

                db.collection("booklists").document(listId).update("coverUrl", downloadUrl, "updatedAt", Timestamp.now()).await()
                Log.d(TAG, "uploadBookListCoverImage: Firestore updated for list $listId")
                downloadUrl
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading booklist cover image", e)
            null
        }
    }

    suspend fun hasPostsForBooklist(booklistId: String): Boolean {
        return try {
            val snap = db.collection("posts").whereEqualTo("booklistId", booklistId).get().await()
            !snap.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking posts for booklist", e)
            false
        }
    }

    suspend fun updateFavoriteBooks(userId: String, bookIds: List<String>): Boolean {
        return try {
            db.collection("users").document(userId).update("favoriteBooks", bookIds).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating favorite books", e)
            false
        }
    }

    suspend fun createPost(post: Post): Boolean {
        Log.d(TAG, "createPost invoked for post: ${post.id}")
        return try {
            val firePost = hashMapOf(
                "id" to post.id,
                "userId" to post.userId,
                "description" to post.review,
                "rating" to post.rating.toDouble(),
                "bookId" to if (post.isBooklistPost) "" else post.book.id,
                "booklistId" to if (post.isBooklistPost) post.booklist?.id else null,
                "status" to post.status,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            db.collection("posts").document(post.id).set(firePost).await()
            Log.d(TAG, "createPost SUCCESS for post: ${post.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post", e)
            false
        }
    }

    suspend fun createDraft(draft: Post): Boolean {
        Log.d(TAG, "createDraft invoked for draft: ${draft.id}")
        return try {
            val fireDraft = hashMapOf(
                "id" to draft.id,
                "userId" to draft.userId,
                "description" to draft.review,
                "rating" to draft.rating.toDouble(),
                "bookId" to if (draft.isBooklistPost) "" else draft.book.id,
                "booklistId" to if (draft.isBooklistPost) draft.booklist?.id else null,
                "status" to draft.status,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
            db.collection("drafts").document(draft.id).set(fireDraft).await()
            Log.d(TAG, "createDraft SUCCESS for draft: ${draft.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating draft", e)
            false
        }
    }

    suspend fun deleteDraft(draftId: String): Boolean {
        return try {
            db.collection("drafts").document(draftId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting draft", e)
            false
        }
    }

    suspend fun getDrafts(userId: String): List<Post> {
        return try {
            val draftsSnap = db.collection("drafts")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val booksSnap = db.collection("books").get().await()
            val booksMap = booksSnap.toObjects(FireBook::class.java).associateBy { it.id }

            val drafts = mutableListOf<Post>()
            for (doc in draftsSnap.documents) {
                val id = doc.getString("id") ?: continue
                val description = doc.getString("description") ?: ""
                val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                val status = doc.getString("status") ?: "Plan To Read"
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                val bookId = doc.getString("bookId") ?: ""
                val booklistId = doc.getString("booklistId")

                if (booklistId.isNullOrEmpty()) {
                    val fireBook = booksMap[bookId] ?: continue
                    val book = Book(
                        id = fireBook.id,
                        title = fireBook.title,
                        author = fireBook.author,
                        coverUrl = fireBook.coverUrl,
                        genre = normalizeGenre(fireBook.genre),
                        publishYear = fireBook.publishYear?.toDate()?.let {
                            val cal = Calendar.getInstance()
                            cal.time = it
                            cal.get(Calendar.YEAR)
                        } ?: 2020,
                        summary = fireBook.summary,
                        isbn = fireBook.isbn
                    )
                    drafts.add(
                        Post(
                            id = id,
                            userId = userId,
                            book = book,
                            status = status,
                            rating = rating,
                            review = description,
                            createdAt = createdAt
                        )
                    )
                } else {
                    val listDoc = try { db.collection("booklists").document(booklistId).get().await() } catch (e: Exception) { null }
                    if (listDoc != null && listDoc.exists()) {
                        val fireList = listDoc.toObject(FireBookList::class.java)
                        if (fireList != null) {
                            val booksInList = mutableListOf<Book>()
                            val junctionSnap = try { db.collection("booklist_books").whereEqualTo("booklistId", fireList.id).get().await() } catch (e: Exception) { null }
                            junctionSnap?.documents?.forEach { jDoc ->
                                val bId = jDoc.getString("bookId")
                                booksMap[bId]?.let { fb ->
                                    booksInList.add(Book(id = fb.id, title = fb.title, author = fb.author, coverUrl = fb.coverUrl))
                                }
                            }
                            val loadedList = BookList(
                                id = fireList.id,
                                userId = fireList.userId,
                                name = fireList.name,
                                description = fireList.description,
                                coverUrl = fireList.coverUrl.ifEmpty { booksInList.firstOrNull()?.coverUrl ?: "" },
                                isPrivate = fireList.isPrivate,
                                createdAt = fireList.createdAt.toDate().time,
                                books = booksInList
                            )
                            val dummyBook = Book(
                                id = loadedList.id,
                                title = loadedList.name,
                                author = "List by author",
                                coverUrl = loadedList.coverUrl,
                                genre = "Booklist"
                            )
                            drafts.add(
                                Post(
                                    id = id,
                                    userId = userId,
                                    book = dummyBook,
                                    status = status,
                                    rating = rating,
                                    review = description,
                                    createdAt = createdAt,
                                    isBooklistPost = true,
                                    booklist = loadedList
                                )
                            )
                        }
                    }
                }
            }
            drafts.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting drafts", e)
            emptyList()
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            db.collection("posts").document(postId).delete().await()
            // Clean up likes
            val likes = db.collection("likes").whereEqualTo("tableId", postId).get().await()
            likes.documents.forEach { it.reference.delete().await() }
            // Clean up saves
            val saves = db.collection("saves").whereEqualTo("tableId", postId).get().await()
            saves.documents.forEach { it.reference.delete().await() }
            // Clean up comments
            val comments = db.collection("comments").whereEqualTo("tableId", postId).get().await()
            comments.documents.forEach { it.reference.delete().await() }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post", e)
            false
        }
    }

    suspend fun createBookList(name: String, description: String, userId: String, isPrivate: Boolean = false, coverUrl: String = ""): BookList? {
        return try {
            val id = "bl_${System.currentTimeMillis()}"
            val fireList = FireBookList(id = id, name = name, description = description, userId = userId, coverUrl = coverUrl, isPrivate = isPrivate)
            db.collection("booklists").document(id).set(fireList).await()
            val ownerName = AppCache.users.find { it.id == userId }?.let { "@${it.username}" } ?: ""
            BookList(id = id, userId = userId, name = name, description = description, coverUrl = coverUrl, isPrivate = isPrivate, createdAt = fireList.createdAt.toDate().time, books = mutableListOf(), ownerName = ownerName)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating book list", e)
            null
        }
    }

    suspend fun updateBookList(listId: String, name: String, description: String, isPrivate: Boolean, coverUrl: String? = null): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "description" to description,
                "isPrivate" to isPrivate,
                "updatedAt" to Timestamp.now()
            )
            if (coverUrl != null) {
                updates["coverUrl"] = coverUrl
            }
            db.collection("booklists").document(listId).update(updates).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating book list", e)
            false
        }
    }

    suspend fun deleteBookList(listId: String): Boolean {
        return try {
            db.collection("booklists").document(listId).delete().await()
            // Clean up books in list
            val books = db.collection("booklist_books").whereEqualTo("booklistId", listId).get().await()
            books.documents.forEach { it.reference.delete().await() }
            // Clean up likes/saves
            val likes = db.collection("likes").whereEqualTo("tableId", listId).get().await()
            likes.documents.forEach { it.reference.delete().await() }
            val saves = db.collection("saves").whereEqualTo("tableId", listId).get().await()
            saves.documents.forEach { it.reference.delete().await() }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting book list", e)
            false
        }
    }

    suspend fun removeBookFromList(listId: String, bookId: String): Boolean {
        return try {
            val junctionId = "${listId}_${bookId}"
            db.collection("booklist_books").document(junctionId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing book from list", e)
            false
        }
    }

    suspend fun addBookToList(booklistId: String, bookId: String): Boolean {
        return try {
            val junctionId = "${booklistId}_${bookId}"
            db.collection("booklist_books").document(junctionId).set(
                FireBookListBook(id = junctionId, booklistId = booklistId, bookId = bookId)
            ).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding book to list", e)
            false
        }
    }

    /**
     * Persists a book from an external source (Open Library API) into the Firestore `books`
     * collection.  Avoids duplicates by checking ISBN first, then title+author.
     *
     * @return The Firestore document ID of the (possibly pre-existing) book.
     */
    suspend fun addBookToFirestore(book: FireBook): String {
        return try {
            // 1. Duplicate check — by ISBN
            if (book.isbn.isNotBlank()) {
                val byIsbn = db.collection("books")
                    .whereEqualTo("isbn", book.isbn)
                    .limit(1)
                    .get().await()
                if (!byIsbn.isEmpty) {
                    val existingId = byIsbn.documents[0].getString("id") ?: byIsbn.documents[0].id
                    Log.d(TAG, "addBookToFirestore: duplicate ISBN detected → $existingId")
                    return existingId
                }
            }

            // 2. Duplicate check — by title + author (case-insensitive not feasible in Firestore,
            //    so we do a simple equality check on the stored fields)
            val byTitle = db.collection("books")
                .whereEqualTo("title", book.title)
                .whereEqualTo("author", book.author)
                .limit(1)
                .get().await()
            if (!byTitle.isEmpty) {
                val existingId = byTitle.documents[0].getString("id") ?: byTitle.documents[0].id
                Log.d(TAG, "addBookToFirestore: duplicate title+author detected → $existingId")
                return existingId
            }

            // 3. No duplicate — write a new document
            val newId = "b_${java.util.UUID.randomUUID()}"
            val toSave = book.copy(id = newId)
            db.collection("books").document(newId).set(toSave).await()
            Log.d(TAG, "addBookToFirestore: new book saved → $newId (${book.title})")
            newId
        } catch (e: Exception) {
            Log.e(TAG, "Error adding book to Firestore", e)
            // Fallback: generate an ID so the caller can still proceed
            "b_${java.util.UUID.randomUUID()}"
        }
    }

    suspend fun addBookToLibrary(userId: String, bookId: String, status: String): Boolean {
        return try {
            val existQuery = db.collection("reading_statuses")
                .whereEqualTo("userId", userId)
                .whereEqualTo("bookId", bookId)
                .get().await()
            
            if (!existQuery.isEmpty) {
                val docId = existQuery.documents[0].id
                db.collection("reading_statuses").document(docId).update("status", status, "updatedAt", Timestamp.now()).await()
            } else {
                val newId = "rs_${java.util.UUID.randomUUID()}"
                val rs = FireReadingStatus(
                    id = newId,
                    userId = userId,
                    bookId = bookId,
                    status = status
                )
                db.collection("reading_statuses").document(newId).set(rs).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding book to library", e)
            false
        }
    }

    suspend fun deleteBookFromLibrary(userId: String, bookId: String): Boolean {
        return try {
            // Check if there are posts referencing this book by this user
            val postsSnap = db.collection("posts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("bookId", bookId)
                .get().await()
            if (!postsSnap.isEmpty) {
                Log.d(TAG, "Cannot delete book $bookId — user has posts referencing it")
                return false
            }
            
            // Check if this book is in any of the user's booklists
            val listsSnap = db.collection("booklists").whereEqualTo("userId", userId).get().await()
            val userListIds = listsSnap.documents.map { it.id }
            if (userListIds.isNotEmpty()) {
                val junctionsSnap = db.collection("booklist_books").whereEqualTo("bookId", bookId).get().await()
                val isInUserList = junctionsSnap.documents.any { it.getString("booklistId") in userListIds }
                if (isInUserList) {
                    Log.d(TAG, "Cannot delete book $bookId — it is present in one of the user's booklists")
                    return false
                }
            }
            val rsSnap = db.collection("reading_statuses")
                .whereEqualTo("userId", userId)
                .whereEqualTo("bookId", bookId)
                .get().await()
            rsSnap.documents.forEach { it.reference.delete().await() }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting book from library", e)
            false
        }
    }

    // ========================
    // SYNC: Firestore → AppCache (in-memory session mirror)
    // ========================

    suspend fun refreshSessionCacheFromFirestore() {
        try {
            val currentUserId = AuthService.getUid()
            withContext(kotlinx.coroutines.NonCancellable) {
                val booksDef = async { getBooks() }
                val postsDef = async { getFeed() }
                val userDef = async { getUserProfile(currentUserId) }
                val listsSnapDef = async { db.collection("booklists").get().await() }
                val exploreSnapDef = async { db.collection("explore_images").get().await() }
                val statusesDef = async { getReadingStatuses() }
                val draftsDef = async { getDrafts(currentUserId) }
                val allUsersDef = async { db.collection("users").get().await() }

                val userSavesSnapInitial = db.collection("saves").whereEqualTo("userId", currentUserId).get().await()
                val userSavedIdsInitial = userSavesSnapInitial.documents.mapNotNull { it.getString("tableId") }.toSet()
                
                // We need the actual booklist IDs first to separate book saves from booklist saves
                val listsSnapEarly = listsSnapDef.await()
                val fireListsEarly = listsSnapEarly.toObjects(FireBookList::class.java)
                val allListIdSet = fireListsEarly.map { it.id }.toSet()
                
                // Book saves = saved IDs that are NOT a booklist ID
                val savedBookIds = userSavedIdsInitial.filter { !allListIdSet.contains(it) }.toSet()

                val books = booksDef.await().map { it.copy(isSaved = savedBookIds.contains(it.id)) }
                AppCache.books.clear()
                AppCache.books.addAll(books)

                val allUsersSnap = allUsersDef.await()
                AppCache.users.clear()
                AppCache.users.addAll(
                    allUsersSnap.documents.mapNotNull { userFromFirestoreDoc(it) }.filter { it.isActive }
                )

                val posts = postsDef.await()
                AppCache.feedPosts.clear()
                AppCache.feedPosts.addAll(posts)

                val user = userDef.await()
                if (user != null) {
                    AppCache.currentUser = user
                }

                val listsSnap = listsSnapEarly
                val fireLists = fireListsEarly
                
                // Optimization: fetch likes and saves for current user
                val userLikesSnap = db.collection("likes").whereEqualTo("userId", currentUserId).get().await()
                val userLikedIds = userLikesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()
                
                val userSavesSnap = db.collection("saves").whereEqualTo("userId", currentUserId).get().await()
                val userSavedIds = userSavesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()

                // Optimization: fetch all likes to get counts
                val allLikesSnap = db.collection("likes").get().await()
                val listLikesCount = allLikesSnap.documents.groupBy { it.getString("tableId") ?: "" }.mapValues { it.value.size }

                AppCache.bookLists.clear()
                
                // All booklist IDs loaded from Firestore
                val allListIds = fireLists.map { it.id }.toSet()
                
                // IDs saved by the user that correspond to a real booklist
                val savedListIds = userSavedIds.filter { allListIds.contains(it) }.toSet()
                
                // Build a user lookup map for owner names
                val allUsersMap = AppCache.users.associateBy { it.id }
                
                fireLists.forEach { fl ->
                    val isOwner = fl.userId == currentUserId
                    val isSaved = savedListIds.contains(fl.id)
                    
                    // Determine if this is a "Secret Booklist" for the current user
                    // Secret = saved by current user, but made private by the owner, and current user is NOT owner
                    val isSecret = isSaved && fl.isPrivate && !isOwner
                    
                    // Show if: Owner, OR public, OR saved (even if private → shows as secret)
                    if (isOwner || !fl.isPrivate || isSaved) {
                        val booksInList = mutableListOf<Book>()
                        val junctionSnap = db.collection("booklist_books").whereEqualTo("booklistId", fl.id).get().await()
                        junctionSnap.documents.forEach { jDoc ->
                            val bId = jDoc.getString("bookId")
                            AppCache.books.find { it.id == bId }?.let { booksInList.add(it) }
                        }
                        
                        // Resolve owner display name (hide for secret booklists)
                        val ownerDisplayName = if (isSecret) "" else {
                            allUsersMap[fl.userId]?.let { "@${it.username}" } ?: ""
                        }
                        
                        // Use user-uploaded coverUrl from Firestore; fall back to first book cover
                        val resolvedCoverUrl = fl.coverUrl.ifEmpty { booksInList.firstOrNull()?.coverUrl ?: "" }

                        AppCache.bookLists.add(BookList(
                            id = fl.id,
                            userId = fl.userId,
                            name = fl.name,
                            description = fl.description,
                            coverUrl = resolvedCoverUrl,
                            isPrivate = fl.isPrivate,
                            likesCount = listLikesCount[fl.id] ?: 0,
                            isLiked = userLikedIds.contains(fl.id),
                            isSaved = isSaved,
                            createdAt = fl.createdAt.toDate().time,
                            books = booksInList,
                            ownerName = ownerDisplayName,
                            isSecret = isSecret,
                            isDeleted = false
                        ))
                    }
                }

                val exploreSnap = exploreSnapDef.await()
                val exploreImages = exploreSnap.toObjects(FireImage::class.java).map { it.path }
                AppCache.explorePosts.clear()
                AppCache.explorePosts.addAll(exploreImages)
                
                val statuses = statusesDef.await()
                AppCache.readingStatuses.clear()
                AppCache.readingStatuses.addAll(statuses)

                val drafts = draftsDef.await()
                AppCache.drafts.clear()
                AppCache.drafts.addAll(drafts)

                // Harmonization:
                // If a post exists but the book isn't formally on the user's shelf, add it organically to the shelf.
                // Skip booklist type posts — their book is a dummy and should never go to the shelf.
                AppCache.feedPosts.filter { !it.isBooklistPost }.forEach { post ->
                    val onShelf = AppCache.readingStatuses.any { it.userId == post.userId && it.book.id == post.book.id }
                    if (!onShelf) {
                        val generatedStatus = ReadingStatus(
                            id = "rs_auto_${post.id}",
                            userId = post.userId,
                            book = post.book,
                            status = post.status,
                            addedAt = System.currentTimeMillis()
                        )
                        AppCache.readingStatuses.add(generatedStatus)
                        launch(Dispatchers.IO) { addBookToLibrary(post.userId, post.book.id, post.status) }
                    }
                }

                // One-time migration for old statuses
                launch(Dispatchers.IO) {
                    try {
                        val postsSnap = db.collection("posts").get().await()
                        for (doc in postsSnap.documents) {
                            val status = doc.getString("status") ?: continue
                            val newStatus = when (status) {
                                "Read", "Okundu" -> "Completed"
                                "To Read", "Want to Read", "Okunacak" -> "Plan To Read"
                                "On Hold" -> "On-hold"
                                "Okunuyor" -> "Reading"
                                else -> status
                            }
                            if (newStatus != status) {
                                db.collection("posts").document(doc.id).update("status", newStatus).await()
                            }
                        }
                        val rsSnap = db.collection("reading_statuses").get().await()
                        for (doc in rsSnap.documents) {
                            val status = doc.getString("status") ?: continue
                            val newStatus = when (status) {
                                "Read", "Okundu" -> "Completed"
                                "To Read", "Want to Read", "Okunacak" -> "Plan To Read"
                                "On Hold" -> "On-hold"
                                "Okunuyor" -> "Reading"
                                else -> status
                            }
                            if (newStatus != status) {
                                db.collection("reading_statuses").document(doc.id).update("status", newStatus).await()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Migration error", e)
                    }
                }
            }
            Log.d(TAG, "AppCache fully synced with Firestore in parallel")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing AppCache", e)
        }
    }
}
