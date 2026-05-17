package com.example.paginex

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Lightweight client for the Open Library Search API.
 *
 * **Design decisions:**
 * - Uses [HttpURLConnection] + [org.json] so no extra dependency is needed.
 * - Requests only the fields we actually use (`fields` param) to minimize payload.
 * - Hard‑limits results to 10 (`limit=10`) to keep responses fast.
 * - All network I/O runs on [Dispatchers.IO].
 */
object OpenLibraryService {

    private const val TAG = "OpenLibraryService"
    private const val BASE_URL = "https://openlibrary.org/search.json"
    private const val FIELDS = "title,author_name,cover_i,first_publish_year,subject,isbn,key"
    private const val RESULT_LIMIT = 10

    /** Parsed result from a single Open Library search doc. */
    data class OpenLibraryBook(
        val key: String,            // e.g. "/works/OL27448W"
        val title: String,
        val author: String,
        val coverId: Int?,          // cover_i — build URL via covers.openlibrary.org
        val isbn: String,
        val publishYear: Int?,
        val genre: String           // first subject or "General"
    ) {
        /** Medium‑sized cover URL (180 px wide).  Falls back to empty string when no cover exists. */
        val coverUrl: String
            get() = if (coverId != null) "https://covers.openlibrary.org/b/id/$coverId-M.jpg" else ""
    }

    /**
     * Searches Open Library for [query] and returns up to [RESULT_LIMIT] parsed results.
     * Returns an empty list on any error (network, parse, etc.) without crashing.
     */
    suspend fun searchBooks(query: String): List<OpenLibraryBook> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = URL("$BASE_URL?q=$encoded&fields=$FIELDS&limit=$RESULT_LIMIT")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("User-Agent", "Paginex/1.0 (Android)")

            try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.w(TAG, "Open Library returned HTTP $responseCode for query: $query")
                    return@withContext emptyList()
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseSearchResponse(body)
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching Open Library for '$query'", e)
            emptyList()
        }
    }

    // ── Internal helpers ────────────────────────────────────────────────

    private fun parseSearchResponse(json: String): List<OpenLibraryBook> {
        val root = JSONObject(json)
        val docs = root.optJSONArray("docs") ?: return emptyList()

        val results = mutableListOf<OpenLibraryBook>()
        for (i in 0 until docs.length()) {
            val doc = docs.getJSONObject(i)

            val title = doc.optString("title", "")
            if (title.isBlank()) continue
            val authorArray = doc.optJSONArray("author_name")
            val author = authorArray?.optString(0, "Unknown Author") ?: "Unknown Author"
            val coverId = if (doc.has("cover_i")) doc.optInt("cover_i") else null
            val year = if (doc.has("first_publish_year")) doc.optInt("first_publish_year") else null
            val key = doc.optString("key", "")

            // Pick first isbn if available
            val isbnArray = doc.optJSONArray("isbn")
            val isbn = isbnArray?.optString(0, "") ?: ""

            // Collect all subjects and map to a clean genre
            val subjectArray = doc.optJSONArray("subject")
            val subjects = mutableListOf<String>()
            if (subjectArray != null) {
                for (j in 0 until subjectArray.length()) {
                    subjects.add(subjectArray.optString(j, ""))
                }
            }
            val genre = mapSubjectsToGenre(subjects)

            results.add(
                OpenLibraryBook(
                    key = key,
                    title = title,
                    author = author,
                    coverId = coverId,
                    isbn = isbn,
                    publishYear = year,
                    genre = genre
                )
            )
        }
        return results
    }

    /**
     * Maps raw Open Library subject strings to a single clean genre label
     * by delegating to the shared [normalizeGenre] utility.
     */
    private fun mapSubjectsToGenre(subjects: List<String>): String {
        for (subject in subjects) {
            val mapped = normalizeGenre(subject)
            if (mapped != "General") return mapped
        }
        return "General"
    }
}

// ── Shared genre normaliser ─────────────────────────────────────────────

/** All clean genre labels recognised by the app. */
private val KNOWN_GENRES = setOf(
    "Sci-Fi", "Fantasy", "Dystopian", "Post-Apocalyptic", "Horror", "Thriller",
    "Mystery", "Romance", "Historical Fiction", "History", "Biography", "Memoir",
    "Philosophy", "Psychology", "Self-Help", "Science", "Poetry", "Drama",
    "Adventure", "Children", "Young Adult", "Comic", "Humor", "Religion",
    "Classic", "Fiction", "General", "Booklist", "Genel"
)

/** Priority-ordered keyword map: more specific genres come first. */
private val GENRE_KEYWORDS: List<Pair<String, List<String>>> = listOf(
    "Sci-Fi"           to listOf("science fiction", "sci-fi", "scifi", "space opera", "cyberpunk", "interstellar"),
    "Fantasy"          to listOf("fantasy", "magic", "wizards", "dragons", "sorcery", "mythical", "fairy tale", "fairy tales"),
    "Dystopian"        to listOf("dystopia", "dystopian", "totalitarian", "orwellian"),
    "Post-Apocalyptic" to listOf("post-apocalyptic", "apocalyptic", "post apocalyptic"),
    "Horror"           to listOf("horror", "gothic", "supernatural", "paranormal", "ghost stories", "vampires", "zombies"),
    "Thriller"         to listOf("thriller", "suspense", "espionage", "spy"),
    "Mystery"          to listOf("mystery", "detective", "crime fiction", "whodunit", "noir"),
    "Romance"          to listOf("romance", "love stories", "romantic"),
    "Historical Fiction" to listOf("historical fiction", "historical novel"),
    "History"          to listOf("history", "historical", "civilization", "ancient", "medieval", "world war"),
    "Biography"        to listOf("biography", "autobiograph", "biographies"),
    "Memoir"           to listOf("memoir", "memoirs", "personal narrative"),
    "Philosophy"       to listOf("philosophy", "philosophical", "stoicism", "existentialism", "ethics"),
    "Psychology"       to listOf("psychology", "psychological", "behavioral", "cognitive", "mental"),
    "Self-Help"        to listOf("self-help", "self help", "personal development", "productivity", "motivation", "habits"),
    "Science"          to listOf("science", "physics", "biology", "chemistry", "astronomy", "evolution", "neuroscience"),
    "Poetry"           to listOf("poetry", "poems", "verse"),
    "Drama"            to listOf("drama", "plays", "theatrical"),
    "Adventure"        to listOf("adventure", "exploration", "quest", "survival"),
    "Children"         to listOf("children", "juvenile", "kids", "picture book", "young readers"),
    "Young Adult"      to listOf("young adult", "ya fiction", "teen", "adolescen"),
    "Comic"            to listOf("comic", "graphic novel", "manga", "superhero"),
    "Humor"            to listOf("humor", "humour", "comedy", "satire", "satirical", "funny"),
    "Religion"         to listOf("religion", "religious", "spiritual", "theology", "bible", "quran", "buddhis"),
    "Classic"          to listOf("classic", "classics", "literary fiction", "literature"),
    "Fiction"          to listOf("fiction", "novel", "stories", "short stories")
)

/** Noise subjects to completely skip. */
private val NOISE_PATTERNS = listOf(
    "accessible book", "protected daisy", "in library", "internet archive",
    "overdrive", "lending library", "large type", "nyt:", "new york times",
    "reading level", "lexile", "open library staff picks"
)

/**
 * Normalises a raw genre / subject string into one of the app's recognised genre labels.
 *
 * - If the input already matches a known genre exactly (case-insensitive), it is returned as-is.
 * - Otherwise the input is scanned against [GENRE_KEYWORDS] to find a match.
 * - Falls back to `"General"` when nothing matches.
 *
 * Safe to call from any layer (Firestore reads, API results, UI display).
 */
fun normalizeGenre(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return "General"

    // Fast path: already a known clean genre (case-insensitive match, return canonical casing)
    KNOWN_GENRES.firstOrNull { it.equals(trimmed, ignoreCase = true) }?.let { return it }

    val lower = trimmed.lowercase()
    if (lower.length < 3) return "General"
    if (NOISE_PATTERNS.any { lower.contains(it) }) return "General"

    for ((genre, keywords) in GENRE_KEYWORDS) {
        if (keywords.any { lower.contains(it) }) {
            return genre
        }
    }

    return "General"
}
