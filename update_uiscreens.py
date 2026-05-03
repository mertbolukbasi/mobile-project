import re

file_path = "app/src/main/java/com/example/paginex/UiScreens.kt"
with open(file_path, "r") as f:
    content = f.read()

# Replace MockData.currentUser.id.ifEmpty { "u1" }
content = content.replace('MockData.currentUser.id.ifEmpty { "u1" }', 'AuthService.getUid()')

# Replace exact "u1" strings
content = content.replace('userId == "u1" || userId == MockData.currentUser.id', 'userId == AuthService.getUid()')
content = content.replace('FirestoreService.toggleLike(post.id, "u1")', 'FirestoreService.toggleLike(post.id, AuthService.getUid())')
content = content.replace('FirestoreService.toggleSave(post.id, "u1")', 'FirestoreService.toggleSave(post.id, AuthService.getUid())')
content = content.replace('it.userId == "u1"', 'it.userId == AuthService.getUid()')
content = content.replace('FirestoreService.toggleBookSave(book.id, "u1")', 'FirestoreService.toggleBookSave(book.id, AuthService.getUid())')
content = content.replace('FirestoreService.toggleBookListLike(list.id, "u1")', 'FirestoreService.toggleBookListLike(list.id, AuthService.getUid())')
content = content.replace('FirestoreService.toggleBookListSave(list.id, "u1")', 'FirestoreService.toggleBookListSave(list.id, AuthService.getUid())')
content = content.replace('FirestoreService.updateFavoriteBooks("u1", selectedIds)', 'FirestoreService.updateFavoriteBooks(AuthService.getUid(), selectedIds)')
content = content.replace('user.id.ifEmpty { "u1" }', 'AuthService.getUid()')
content = content.replace('targetUserId != MockData.currentUser.id && targetUserId != "u1"', 'targetUserId != AuthService.getUid()')
content = content.replace('targetUserId == "u1" || targetUserId == MockData.currentUser.id', 'targetUserId == AuthService.getUid()')
content = content.replace('FirestoreService.getFollowing("u1")', 'FirestoreService.getFollowing(AuthService.getUid())')
content = content.replace('FirestoreService.unfollowUser("u1", userId)', 'FirestoreService.unfollowUser(AuthService.getUid(), userId)')
content = content.replace('FirestoreService.followUser("u1", userId)', 'FirestoreService.followUser(AuthService.getUid(), userId)')
content = content.replace('MockData.currentUser.id.ifEmpty { "u1" }', 'AuthService.getUid()') # Just in case

with open(file_path, "w") as f:
    f.write(content)

print("Replacement complete.")
