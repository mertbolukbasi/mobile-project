package com.example.paginex

import androidx.compose.runtime.mutableStateListOf
data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val avatarUrl: String,
    val bio: String = "",
    val location: String = "",
    val joinDate: String = "",
    val followingCount: Int = 0,
    val followersCount: Int = 0
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val genre: String = "Genel",
    val publishYear: Int = 2020,
    val summary: String = "",
    val isbn: String = ""
)

data class Post(
    val id: String,
    val userId: String,
    val book: Book,
    val status: String, // "Okundu", "Okunuyor", etc.
    val rating: Float,
    val review: String,
    val timestamp: String = "2 saat önce",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookList(
    val id: String,
    val name: String,
    val description: String = "",
    val coverUrl: String = "",
    val books: MutableList<Book> = mutableListOf()
)

// --- MOCK DATA SAMPLES ---
object MockData {
    val currentUser = User(
        id = "u1",
        username = "ayseyilmaz",
        fullName = "Ayşe Yılmaz",
        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
        bio = "Kitap okumayı ve yeni dünyalar keşfetmeyi seviyorum. 📚 Fantastik ve distopya türlerinin hayranıyım.",
        location = "İstanbul, Türkiye",
        joinDate = "Katılma: Ocak 2024",
        followingCount = 234,
        followersCount = 512
    )

    val sampleBooks = listOf(
        Book("b1", "1984", "George Orwell", "https://images.unsplash.com/photo-1541963463532-d68292c34b19?auto=format&fit=crop&q=80&w=400", "Distopya", 1949, "Totaliter bir distopyada geçen ve bireyselliğin yok edildiği korkutucu bir gelecek tasviri."),
        Book("b2", "Sapiens", "Yuval Noah Harari", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&q=80&w=400", "Tarih", 2011, "İnsan türünün ortaya çıkışından günümüze kadar olan evrimini ve kültürel gelişimini inceleyen başyapıt."),
        Book("b3", "Küçük Prens", "Antoine de Saint-Exupéry", "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&q=80&w=400", "Klasik", 1943, "Bir çocuğun gözünden büyüklerin dünyasının anlamsızlıklarını saf bir dille anlatan zamansız bir masal."),
        Book("b4", "Dune", "Frank Herbert", "https://images.unsplash.com/photo-1592496431122-23492f92273e?auto=format&fit=crop&q=80&w=400", "Bilim Kurgu", 1965, "Çöl gezegeni Arrakis'te geçen, siyaset, din ve ekolojinin mükemmel harmanlandığı kült bilimkurgu eseri."),
        Book("b5", "Suç ve Ceza", "Fyodor Dostoyevski", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&q=80&w=400", "Klasik", 1866, "Cinayet işleyen bir gencin çektiği vicdan azabı ve ruhsal çöküntüsünü derin psikolojik analizlerle sunan roman."),
        Book("b6", "Harry Potter ve Felsefe Taşı", "J.K. Rowling", "https://images.unsplash.com/photo-1589998059171-988d887df646?auto=format&fit=crop&q=80&w=400", "Fantastik", 1997, "Genç bir büyücünün Hogwarts Cadılık ve Büyücülük Okulu'ndaki ilk yılını ve sihirli dünyayla tanışmasını anlatan başlangıç kitabı."),
        Book("b7", "Cesur Yeni Dünya", "Aldous Huxley", "https://images.unsplash.com/photo-1532012197367-68bf563a34a1?auto=format&fit=crop&q=80&w=400", "Distopya", 1932, "Bebeklerin fabrikalarda üretildiği, duyguların bastırıldığı ve herkesin sahte bir mutluluğa mahkum edildiği bir gelecek öngörüsü."),
        Book("b8", "Simyacı", "Paulo Coelho", "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&q=80&w=400", "Felsefe", 1988, "Kişisel menkıbesini arayan çoban Santiago'nun Mısır Piramitleri'ne uzanan felsefi ve ruhsal yolculuğu.")
    )

    val feedPosts = mutableStateListOf(
        Post(
            id = "p1",
            userId = "u1",
            book = sampleBooks[0],
            status = "Okundu",
            rating = 10f,
            review = "Muhteşem bir distopya klasiği. Orwell'ın yarattığı dünya bugünün dünyasına çok benziyor. Büyük Birader'in gözetimi altındaki toplum, günümüzün dijital gözetim toplumuna bir uyarı niteliğinde.",
            likesCount = 42,
            commentsCount = 8,
            isLiked = false,
            isSaved = true,
            createdAt = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000)
        ),
        Post(
            id = "p2",
            userId = "u2",
            book = sampleBooks[1],
            status = "Okunuyor",
            rating = 9f,
            review = "İnsanlık tarihine bakış açımı değiştiren bir eser.",
            likesCount = 128,
            commentsCount = 15,
            isLiked = true,
            isSaved = false,
            createdAt = System.currentTimeMillis() - (40L * 24 * 60 * 60 * 1000)
        ),
        Post(
            id = "p3",
            userId = "u3",
            book = sampleBooks[2],
            status = "Askıda",
            rating = 8f,
            review = "Biraz ara verdim ama kesinlikle geri döneceğim.",
            likesCount = 15,
            commentsCount = 2,
            isLiked = false,
            isSaved = true,
            createdAt = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000)
        ),
        Post(
            id = "p4",
            userId = "u1",
            book = sampleBooks[3],
            status = "Okunuyor",
            rating = 0f, // Henüz bitmediği için puan yok
            review = "Dune evrenine yeni adım attım ve şimdiden büyülendim. Bilim kurgu severler için bir başyapıt.",
            likesCount = 78,
            commentsCount = 10,
            isLiked = true,
            isSaved = false,
            createdAt = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000)
        ),
        Post(
            id = "p5",
            userId = "u4",
            book = sampleBooks[5],
            status = "Okundu",
            rating = 10f,
            review = "Çocukluğumun en güzel anılarını canlandıran bir kitap. Her yaştan okuyucuya tavsiye ederim.",
            likesCount = 201,
            commentsCount = 30,
            isLiked = false,
            isSaved = true,
            createdAt = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000)
        )
    )

    val explorePosts = listOf(
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

    val sampleBookLists = mutableListOf(
        BookList(
            id = "bl1",
            name = "Favori Distopyalarım",
            description = "Karanlık gelecek hikayeleri",
            coverUrl = "https://images.unsplash.com/photo-1541963463532-d68292c34b19?auto=format&fit=crop&q=80&w=400",
            books = mutableListOf(sampleBooks[0], sampleBooks[6])
        ),
        BookList(
            id = "bl2",
            name = "Okunacaklar Listesi",
            description = "Sıradakiler...",
            coverUrl = "https://images.unsplash.com/photo-1506466010722-395aa2bef877?auto=format&fit=crop&q=80&w=400",
            books = mutableListOf(sampleBooks[3], sampleBooks[4], sampleBooks[7])
        ),
        BookList(
            id = "bl3",
            name = "Klasik Eserler",
            description = "Zamanı aşan kitaplar",
            coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&q=80&w=400",
            books = mutableListOf(sampleBooks[2], sampleBooks[4])
        )
    )

    val drafts = mutableStateListOf<Post>()
}

// --- REPOSITORY INTERFACE ---
interface PaginexRepository {
    suspend fun getFeed(): List<Post>
    suspend fun getUserProfile(userId: String): User
    suspend fun createPost(post: Post): Boolean
}

// --- MOCK IMPLEMENTATION (Backendless) ---
class MockPaginexRepository : PaginexRepository {
    override suspend fun getFeed(): List<Post> = MockData.feedPosts
    
    override suspend fun getUserProfile(userId: String): User = MockData.currentUser
    
    override suspend fun createPost(post: Post): Boolean = true
}
