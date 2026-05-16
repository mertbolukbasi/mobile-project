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

            // Pick first subject as genre
            val subjectArray = doc.optJSONArray("subject")
            val genre = subjectArray?.optString(0, "General") ?: "General"

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
}
