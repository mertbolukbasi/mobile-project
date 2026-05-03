import re

file_path = "app/src/main/java/com/example/paginex/UiScreens.kt"
with open(file_path, "r") as f:
    content = f.read()

# 1. toggleLike (post)
content = content.replace("""
                                val index = MockData.feedPosts.indexOfFirst { it.id == post.id }
                                if (index != -1) {
                                    val currentPost = MockData.feedPosts[index]
                                    val newIsLiked = !currentPost.isLiked
                                    val newLikesCount = if (newIsLiked) currentPost.likesCount + 1 else (currentPost.likesCount - 1).coerceAtLeast(0)
                                    MockData.feedPosts[index] = currentPost.copy(
                                        isLiked = newIsLiked,
                                        likesCount = newLikesCount
                                    )
                                }
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleLike(post.id, AuthService.getUid(), newIsLiked)
                                }""", """
                                val index = MockData.feedPosts.indexOfFirst { it.id == post.id }
                                val newIsLiked = if (index != -1) !MockData.feedPosts[index].isLiked else !post.isLiked
                                if (index != -1) {
                                    val currentPost = MockData.feedPosts[index]
                                    val newLikesCount = if (newIsLiked) currentPost.likesCount + 1 else (currentPost.likesCount - 1).coerceAtLeast(0)
                                    MockData.feedPosts[index] = currentPost.copy(
                                        isLiked = newIsLiked,
                                        likesCount = newLikesCount
                                    )
                                }
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleLike(post.id, AuthService.getUid(), newIsLiked)
                                }""")

# 2. toggleSave (post)
content = content.replace("""
                                val index = MockData.feedPosts.indexOfFirst { it.id == post.id }
                                if (index != -1) {
                                    val currentPost = MockData.feedPosts[index]
                                    MockData.feedPosts[index] = currentPost.copy(
                                        isSaved = !currentPost.isSaved
                                    )
                                }
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleSave(post.id, AuthService.getUid(), newIsSaved)
                                }""", """
                                val index = MockData.feedPosts.indexOfFirst { it.id == post.id }
                                val newIsSaved = if (index != -1) !MockData.feedPosts[index].isSaved else !post.isSaved
                                if (index != -1) {
                                    val currentPost = MockData.feedPosts[index]
                                    MockData.feedPosts[index] = currentPost.copy(
                                        isSaved = newIsSaved
                                    )
                                }
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleSave(post.id, AuthService.getUid(), newIsSaved)
                                }""")

# 3. toggleBookListLike
content = content.replace("""
                                        val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                        if (index != -1) {
                                            val currentList = MockData.sampleBookLists[index]
                                            val newIsLiked = !currentList.isLiked
                                            val newLikesCount = if (newIsLiked) currentList.likesCount + 1 else (currentList.likesCount - 1).coerceAtLeast(0)
                                            MockData.sampleBookLists[index] = currentList.copy(
                                                isLiked = newIsLiked,
                                                likesCount = newLikesCount
                                            )
                                        }
                                        kotlinx.coroutines.MainScope().launch {
                                            FirestoreService.toggleBookListLike(list.id, AuthService.getUid(), newIsLiked)
                                        }""", """
                                        val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                        val newIsLiked = if (index != -1) !MockData.sampleBookLists[index].isLiked else !list.isLiked
                                        if (index != -1) {
                                            val currentList = MockData.sampleBookLists[index]
                                            val newLikesCount = if (newIsLiked) currentList.likesCount + 1 else (currentList.likesCount - 1).coerceAtLeast(0)
                                            MockData.sampleBookLists[index] = currentList.copy(
                                                isLiked = newIsLiked,
                                                likesCount = newLikesCount
                                            )
                                        }
                                        kotlinx.coroutines.MainScope().launch {
                                            FirestoreService.toggleBookListLike(list.id, AuthService.getUid(), newIsLiked)
                                        }""")

# 4. toggleBookListSave
content = content.replace("""
                                        val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                        if (index != -1) {
                                            val currentList = MockData.sampleBookLists[index]
                                            MockData.sampleBookLists[index] = currentList.copy(
                                                isSaved = !currentList.isSaved
                                            )
                                        }
                                        kotlinx.coroutines.MainScope().launch {
                                            FirestoreService.toggleBookListSave(list.id, AuthService.getUid(), newIsSaved)
                                        }""", """
                                        val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                        val newIsSaved = if (index != -1) !MockData.sampleBookLists[index].isSaved else !list.isSaved
                                        if (index != -1) {
                                            val currentList = MockData.sampleBookLists[index]
                                            MockData.sampleBookLists[index] = currentList.copy(
                                                isSaved = newIsSaved
                                            )
                                        }
                                        kotlinx.coroutines.MainScope().launch {
                                            FirestoreService.toggleBookListSave(list.id, AuthService.getUid(), newIsSaved)
                                        }""")

# 5. toggleBookListLike (with currentUid)
content = content.replace("""
                                val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                if (index != -1) {
                                    val currentList = MockData.sampleBookLists[index]
                                    val newIsLiked = !currentList.isLiked
                                    val newLikesCount = if (newIsLiked) currentList.likesCount + 1 else (currentList.likesCount - 1).coerceAtLeast(0)
                                    MockData.sampleBookLists[index] = currentList.copy(
                                        isLiked = newIsLiked,
                                        likesCount = newLikesCount
                                    )
                                }
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListLike(list.id, currentUid, newIsLiked)
                                }""", """
                                val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                val newIsLiked = if (index != -1) !MockData.sampleBookLists[index].isLiked else !list.isLiked
                                if (index != -1) {
                                    val currentList = MockData.sampleBookLists[index]
                                    val newLikesCount = if (newIsLiked) currentList.likesCount + 1 else (currentList.likesCount - 1).coerceAtLeast(0)
                                    MockData.sampleBookLists[index] = currentList.copy(
                                        isLiked = newIsLiked,
                                        likesCount = newLikesCount
                                    )
                                }
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListLike(list.id, currentUid, newIsLiked)
                                }""")

# 6. toggleBookListSave (with currentUid)
content = content.replace("""
                                val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                if (index != -1) {
                                    val currentList = MockData.sampleBookLists[index]
                                    MockData.sampleBookLists[index] = currentList.copy(
                                        isSaved = !currentList.isSaved
                                    )
                                }
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListSave(list.id, currentUid, newIsSaved)
                                }""", """
                                val index = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                val newIsSaved = if (index != -1) !MockData.sampleBookLists[index].isSaved else !list.isSaved
                                if (index != -1) {
                                    val currentList = MockData.sampleBookLists[index]
                                    MockData.sampleBookLists[index] = currentList.copy(
                                        isSaved = newIsSaved
                                    )
                                }
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListSave(list.id, currentUid, newIsSaved)
                                }""")

with open(file_path, "w") as f:
    f.write(content)

print("Scopes fixed in UiScreens.kt.")
