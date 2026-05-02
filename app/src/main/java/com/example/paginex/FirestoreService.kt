package com.example.paginex

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "FirestoreService"

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
                val collectionsToClear = listOf("books", "users", "posts", "booklists", "booklist_books", "favourite_books", "reading_statuses", "explore_images", "follows", "likes", "saves", "comments")
                collectionsToClear.forEach { col ->
                    val docs = db.collection(col).get().await()
                    docs.forEach { it.reference.delete() } // Wait not needed for bulk deletes, but keep it simple
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

            // 2. Users
            val seedUsers = listOf(
                FireUser(id = "u1", username = "ayseyilmaz", name = "Ayşe", surname = "Yılmaz",
                    email = "ayse@example.com",
                    bio = "Kitap okumayı ve yeni dünyalar keşfetmeyi seviyorum. 📚 Fantastik ve distopya türlerinin hayranıyım.",
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
                    location = "İstanbul, Türkiye"),
                FireUser(id = "u2", username = "mehmet_okur", name = "Mehmet", surname = "Okur",
                    email = "mehmet@example.com",
                    bio = "Tarih ve felsefe meraklısı. Her kitap yeni bir ufuk.",
                    avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200",
                    location = "Ankara, Türkiye"),
                FireUser(id = "u3", username = "canan_dag", name = "Canan", surname = "Dağ",
                    email = "canan@example.com",
                    bio = "Klasik edebiyat tutkunu. Dostoyevski ve Tolstoy benim kahramanlarım.",
                    avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=200",
                    location = "İzmir, Türkiye"),
                FireUser(id = "u4", username = "elif_kitap", name = "Elif", surname = "Kaya",
                    email = "elif@example.com",
                    bio = "Fantastik dünyaların gezgini. Hogwarts mektubu hâlâ gelmedi ama umut var! ✨",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                    location = "Bursa, Türkiye")
            )
            seedUsers.forEach { db.collection("users").document(it.id).set(it).await() }

            // 3. Posts
            val now = System.currentTimeMillis()
            val seedPosts = listOf(
                hashMapOf(
                    "id" to "p1", "userId" to "u1", "bookId" to "b1",
                    "description" to "A brilliant dystopian classic. The world Orwell created feels startlingly similar to today's reality. The society under Big Brother's surveillance serves as a stark warning.",
                    "rating" to 10.0, "status" to "Read",
                    "createdAt" to Timestamp(Date(now - 5L * 24 * 60 * 60 * 1000)),
                    "updatedAt" to Timestamp.now()
                ),
                hashMapOf(
                    "id" to "p2", "userId" to "u2", "bookId" to "b2",
                    "description" to "This book fundamentally changed my perspective on human history. Harari presents complex topics in an incredibly accessible way.",
                    "rating" to 9.0, "status" to "Reading",
                    "createdAt" to Timestamp(Date(now - 40L * 24 * 60 * 60 * 1000)),
                    "updatedAt" to Timestamp.now()
                ),
                hashMapOf(
                    "id" to "p3", "userId" to "u3", "bookId" to "b3",
                    "description" to "Taking a little break right now, but I will definitely return to it. Hidden in every page of The Little Prince are deep profound messages about life.",
                    "rating" to 8.0, "status" to "On Hold",
                    "createdAt" to Timestamp(Date(now - 10L * 24 * 60 * 60 * 1000)),
                    "updatedAt" to Timestamp.now()
                ),
                hashMapOf(
                    "id" to "p4", "userId" to "u1", "bookId" to "b4",
                    "description" to "Just stepped into the Dune universe and I'm already fascinated. A true masterpiece for sci-fi lovers.",
                    "rating" to 0.0, "status" to "Reading",
                    "createdAt" to Timestamp(Date(now - 2L * 24 * 60 * 60 * 1000)),
                    "updatedAt" to Timestamp.now()
                ),
                hashMapOf(
                    "id" to "p5", "userId" to "u4", "bookId" to "b6",
                    "description" to "A book that revives the best memories of my childhood. I highly recommend it to readers of all ages.",
                    "rating" to 10.0, "status" to "Read",
                    "createdAt" to Timestamp(Date(now - 60L * 24 * 60 * 60 * 1000)),
                    "updatedAt" to Timestamp.now()
                )
            )
            seedPosts.forEach { db.collection("posts").document(it["id"] as String).set(it).await() }

            // 4. Booklists
            val seedLists = listOf(
                FireBookList(id = "bl1", name = "My Favorite Dystopias", description = "Dark future stories", userId = "u1"),
                FireBookList(id = "bl2", name = "To Read List", description = "Up next...", userId = "u1"),
                FireBookList(id = "bl3", name = "Classic Works", description = "Timeless books", userId = "u1")
            )
            seedLists.forEach { db.collection("booklists").document(it.id).set(it).await() }

            // 5. Booklist-Book junctions
            val junctions = listOf(
                "bl1" to "b1", "bl1" to "b7",
                "bl2" to "b4", "bl2" to "b5", "bl2" to "b8",
                "bl3" to "b3", "bl3" to "b5"
            )
            junctions.forEach { (listId, bookId) ->
                val junctionId = "${listId}_${bookId}"
                db.collection("booklist_books").document(junctionId).set(
                    FireBookListBook(id = junctionId, booklistId = listId, bookId = bookId)
                ).await()
            }

            // 6. FavouriteBooks
            db.collection("favourite_books").document("fav1").set(
                FireFavouriteBooks(id = "fav1", userId = "u1", bookIds = listOf("b1", "b2", "b3"))
            ).await()

            // 7. ReadingStatus
            db.collection("reading_statuses").document("rs1").set(
                FireReadingStatus(id = "rs1", userId = "u1", bookId = "b1", status = "Completed")
            ).await()

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

            // 9. Follow relationships
            val follows = listOf(
                FireFollow(id = "f1", followerId = "u1", followingId = "u2"),
                FireFollow(id = "f2", followerId = "u1", followingId = "u3"),
                FireFollow(id = "f3", followerId = "u2", followingId = "u1"),
                FireFollow(id = "f4", followerId = "u3", followingId = "u1"),
                FireFollow(id = "f6", followerId = "u2", followingId = "u4")
            )
            follows.forEach { db.collection("follows").document(it.id).set(it).await() }

            Log.d(TAG, "Full seed initialization complete!")
        } catch (e: Exception) {
            Log.e(TAG, "Error during seed initialization", e)
        }
    }

    // ========================
    // READ OPERATIONS
    // ========================

    suspend fun getFeed(): List<Post> {
        return try {
            val postsSnap = db.collection("posts").orderBy("createdAt").get().await()
            val posts = mutableListOf<Post>()
            val currentUserId = "u1"

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
                val status = doc.getString("status") ?: "To Read"
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()

                val fireBook = booksMap[bookId]
                val isLiked = userLikedTableIds.contains(id)
                val isSaved = userSavedTableIds.contains(id)

                val totalLikes = likesCountMap[id] ?: 0
                val totalComments = commentsCountMap[id] ?: 0

                if (fireBook != null) {
                    val book = Book(
                        id = fireBook.id,
                        title = fireBook.title,
                        author = fireBook.author,
                        coverUrl = fireBook.coverUrl,
                        genre = fireBook.genre,
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
                }
            }
            Log.d(TAG, "getFeed loaded ${posts.size} posts successfully")
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
                // Get follower/following counts
                val followersCount = db.collection("follows")
                    .whereEqualTo("followingId", userId)
                    .get().await().size()
                val followingCount = db.collection("follows")
                    .whereEqualTo("followerId", userId)
                    .get().await().size()

                User(
                    id = fireUser.id,
                    username = fireUser.username,
                    fullName = "${fireUser.name} ${fireUser.surname}",
                    avatarUrl = fireUser.avatarUrl,
                    bio = fireUser.bio,
                    location = fireUser.location,
                    joinDate = "Joined: ${fireUser.createdAt.toDate().let {
                        val cal = Calendar.getInstance(); cal.time = it
                        val months = listOf("January","February","March","April","May","June","July","August","September","October","November","December")
                        "${months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
                    }}",
                    followersCount = followersCount,
                    followingCount = followingCount,
                    favoriteBooks = fireUser.favoriteBooks
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }

    suspend fun getUserById(userId: String): FireUser? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(FireUser::class.java)
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
                    genre = fb.genre,
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
                    genre = fireBook.genre,
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
                    status = doc.getString("status") ?: "To Read",
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

    suspend fun toggleCommentLike(commentId: String, userId: String): Boolean {
        return try {
            val likeRef = db.collection("likes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("tableId", commentId)
                .get().await()

            if (likeRef.isEmpty) {
                val newLike = FireLike(
                    id = "c_like_${userId}_${commentId}",
                    userId = userId,
                    tableId = commentId,
                    createdAt = Timestamp.now()
                )
                db.collection("likes").document(newLike.id).set(newLike).await()
            } else {
                db.collection("likes").document(likeRef.documents[0].id).delete().await()
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

    suspend fun toggleLike(postId: String, userId: String): Boolean {
        return try {
            val likeRef = db.collection("likes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("tableId", postId)
                .get().await()

            if (likeRef.isEmpty) {
                val newLike = FireLike(
                    id = "${userId}_${postId}",
                    userId = userId,
                    tableId = postId,
                    createdAt = Timestamp.now()
                )
                db.collection("likes").document(newLike.id).set(newLike).await()
            } else {
                db.collection("likes").document(likeRef.documents[0].id).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
            false
        }
    }

    suspend fun toggleSave(postId: String, userId: String): Boolean {
        return try {
            val saveRef = db.collection("saves")
                .whereEqualTo("userId", userId)
                .whereEqualTo("tableId", postId)
                .get().await()

            if (saveRef.isEmpty) {
                val newSave = FireSave(
                    id = "post_save_${userId}_${postId}",
                    userId = userId,
                    tableId = postId,
                    createdAt = Timestamp.now()
                )
                db.collection("saves").document(newSave.id).set(newSave).await()
            } else {
                db.collection("saves").document(saveRef.documents[0].id).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling save", e)
            false
        }
    }

    suspend fun toggleBookListLike(listId: String, userId: String): Boolean {
        return try {
            val likeRef = db.collection("likes")
                .whereEqualTo("userId", userId)
                .whereEqualTo("tableId", listId)
                .get().await()

            if (likeRef.isEmpty) {
                val newLike = FireLike(
                    id = "bl_like_${userId}_${listId}",
                    userId = userId,
                    tableId = listId,
                    createdAt = Timestamp.now()
                )
                db.collection("likes").document(newLike.id).set(newLike).await()
            } else {
                db.collection("likes").document(likeRef.documents[0].id).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling book list like", e)
            false
        }
    }

    suspend fun toggleBookListSave(listId: String, userId: String): Boolean {
        return try {
            val saveRef = db.collection("saves")
                .whereEqualTo("userId", userId)
                .whereEqualTo("tableId", listId)
                .get().await()

            if (saveRef.isEmpty) {
                val newSave = FireSave(
                    id = "bl_save_${userId}_${listId}",
                    userId = userId,
                    tableId = listId,
                    createdAt = Timestamp.now()
                )
                db.collection("saves").document(newSave.id).set(newSave).await()
            } else {
                db.collection("saves").document(saveRef.documents[0].id).delete().await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling book list save", e)
            false
        }
    }

    suspend fun isFollowing(followerId: String, followingId: String): Boolean {
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
        return try {
            val id = "f_${System.currentTimeMillis()}"
            val follow = FireFollow(id = id, followerId = followerId, followingId = followingId)
            db.collection("follows").document(id).set(follow).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error following user", e)
            false
        }
    }

    suspend fun unfollowUser(followerId: String, followingId: String): Boolean {
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
                "bookId" to post.book.id,
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

    suspend fun createBookList(name: String, description: String, userId: String, isPrivate: Boolean = false): BookList? {
        return try {
            val id = "bl_${System.currentTimeMillis()}"
            val fireList = FireBookList(id = id, name = name, description = description, userId = userId, isPrivate = isPrivate)
            db.collection("booklists").document(id).set(fireList).await()
            BookList(id = id, userId = userId, name = name, description = description, coverUrl = "", isPrivate = isPrivate, books = mutableListOf())
        } catch (e: Exception) {
            Log.e(TAG, "Error creating book list", e)
            null
        }
    }

    suspend fun updateBookList(listId: String, name: String, description: String, isPrivate: Boolean): Boolean {
        return try {
            db.collection("booklists").document(listId).update(
                "name", name,
                "description", description,
                "isPrivate", isPrivate,
                "updatedAt", Timestamp.now()
            ).await()
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
                val newId = "rs_${System.currentTimeMillis()}"
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
    // SYNC (Firestore → MockData cache)
    // ========================

    suspend fun syncMockData() {
        try {
            withContext(kotlinx.coroutines.NonCancellable) {
                val booksDef = async { getBooks() }
                val postsDef = async { getFeed() }
                val userDef = async { getUserProfile("u1") }
                val listsSnapDef = async { db.collection("booklists").get().await() }
                val exploreSnapDef = async { db.collection("explore_images").get().await() }
                val statusesDef = async { getReadingStatuses() }

                val books = booksDef.await()
                MockData.sampleBooks.clear()
                MockData.sampleBooks.addAll(books)

                val posts = postsDef.await()
                MockData.feedPosts.clear()
                MockData.feedPosts.addAll(posts)

                val user = userDef.await()
                if (user != null) {
                    MockData.currentUser = user
                }

                val listsSnap = listsSnapDef.await()
                val fireLists = listsSnap.toObjects(FireBookList::class.java)
                
                // Optimization: fetch likes and saves for current user
                val currentUserId = "u1"
                val userLikesSnap = db.collection("likes").whereEqualTo("userId", currentUserId).get().await()
                val userLikedIds = userLikesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()
                
                val userSavesSnap = db.collection("saves").whereEqualTo("userId", currentUserId).get().await()
                val userSavedIds = userSavesSnap.documents.mapNotNull { it.getString("tableId") }.toSet()

                // Optimization: fetch all likes to get counts
                val allLikesSnap = db.collection("likes").get().await()
                val listLikesCount = allLikesSnap.documents.groupBy { it.getString("tableId") ?: "" }.mapValues { it.value.size }

                MockData.sampleBookLists.clear()
                
                // Fetch all unique list IDs that are either owned, public, or saved by user
                val savedListIds = userSavedIds.filter { it.startsWith("bl_") }.toSet()
                
                // We need to fetch lists that are saved by the user but not necessarily owned or public in the first snap
                // Actually, the listsSnap has all lists. In a real app we'd filter, but here it seems it fetches all.
                // Let's refine the lists fetching if needed. For now, assume listsSnap contains everything we might need.
                
                fireLists.forEach { fl ->
                    val isOwner = fl.userId == currentUserId
                    val isSaved = savedListIds.contains(fl.id)
                    
                    // Show if: Owner, OR (Public AND (not private)), OR (Saved)
                    if (isOwner || !fl.isPrivate || isSaved) {
                        val booksInList = mutableListOf<Book>()
                        val junctionSnap = db.collection("booklist_books").whereEqualTo("booklistId", fl.id).get().await()
                        junctionSnap.documents.forEach { jDoc ->
                            val bId = jDoc.getString("bookId")
                            MockData.sampleBooks.find { it.id == bId }?.let { booksInList.add(it) }
                        }

                        MockData.sampleBookLists.add(BookList(
                            id = fl.id,
                            userId = fl.userId,
                            name = fl.name,
                            description = fl.description,
                            coverUrl = booksInList.firstOrNull()?.coverUrl ?: "",
                            isPrivate = fl.isPrivate,
                            likesCount = listLikesCount[fl.id] ?: 0,
                            isLiked = userLikedIds.contains(fl.id),
                            isSaved = isSaved,
                            books = booksInList
                        ))
                    }
                }

                val exploreSnap = exploreSnapDef.await()
                val exploreImages = exploreSnap.toObjects(FireImage::class.java).map { it.path }
                MockData.explorePosts.clear()
                MockData.explorePosts.addAll(exploreImages)
                
                val statuses = statusesDef.await()
                MockData.readingStatuses.clear()
                MockData.readingStatuses.addAll(statuses)

                // Harmonization (Dune Bug Fix):
                // If a post exists but the book isn't formally on the user's shelf, add it organically to the shelf.
                MockData.feedPosts.forEach { post ->
                    val onShelf = MockData.readingStatuses.any { it.userId == post.userId && it.book.id == post.book.id }
                    if (!onShelf) {
                        val generatedStatus = ReadingStatus(
                            id = "rs_auto_${post.id}",
                            userId = post.userId,
                            book = post.book,
                            status = post.status,
                            addedAt = System.currentTimeMillis()
                        )
                        MockData.readingStatuses.add(generatedStatus)
                        launch(Dispatchers.IO) { addBookToLibrary(post.userId, post.book.id, post.status) }
                    }
                }
            }
            Log.d(TAG, "MockData fully synced with Firestore in parallel")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing MockData", e)
        }
    }
}
