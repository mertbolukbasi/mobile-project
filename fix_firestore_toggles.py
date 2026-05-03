import re

file_path = "app/src/main/java/com/example/paginex/FirestoreService.kt"
with open(file_path, "r") as f:
    content = f.read()

def replace_toggle_comment_like(match):
    return """suspend fun toggleCommentLike(commentId: String, userId: String, isLiked: Boolean): Boolean {
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
    }"""

def replace_toggle_like(match):
    return """suspend fun toggleLike(postId: String, userId: String, isLiked: Boolean): Boolean {
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
    }"""

def replace_toggle_save(match):
    return """suspend fun toggleSave(postId: String, userId: String, isSaved: Boolean): Boolean {
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
    }"""

def replace_toggle_bl_like(match):
    return """suspend fun toggleBookListLike(listId: String, userId: String, isLiked: Boolean): Boolean {
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
    }"""

def replace_toggle_book_save(match):
    return """suspend fun toggleBookSave(bookId: String, userId: String, isSaved: Boolean): Boolean {
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
    }"""

def replace_toggle_bl_save(match):
    return """suspend fun toggleBookListSave(listId: String, userId: String, isSaved: Boolean): Boolean {
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
    }"""

content = re.sub(r'suspend fun toggleCommentLike.*?false\n        }\n    }', replace_toggle_comment_like, content, flags=re.DOTALL)
content = re.sub(r'suspend fun toggleLike.*?false\n        }\n    }', replace_toggle_like, content, flags=re.DOTALL)
content = re.sub(r'suspend fun toggleSave.*?false\n        }\n    }', replace_toggle_save, content, flags=re.DOTALL)
content = re.sub(r'suspend fun toggleBookListLike.*?false\n        }\n    }', replace_toggle_bl_like, content, flags=re.DOTALL)
content = re.sub(r'suspend fun toggleBookSave.*?false\n        }\n    }', replace_toggle_book_save, content, flags=re.DOTALL)
content = re.sub(r'suspend fun toggleBookListSave.*?false\n        }\n    }', replace_toggle_bl_save, content, flags=re.DOTALL)

with open(file_path, "w") as f:
    f.write(content)

print("FirestoreService updated.")
