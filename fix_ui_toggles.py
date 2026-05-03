import re
import os

files_to_check = [
    "app/src/main/java/com/example/paginex/UiScreens.kt",
    "app/src/main/java/com/example/paginex/CommentsComponent.kt"
]

for file_path in files_to_check:
    with open(file_path, "r") as f:
        content = f.read()

    # UiScreens.kt replacements
    # 1. toggleLike
    content = content.replace(
        "FirestoreService.toggleLike(post.id, AuthService.getUid())",
        "FirestoreService.toggleLike(post.id, AuthService.getUid(), newIsLiked)"
    )
    
    # 2. toggleSave
    content = content.replace(
        "FirestoreService.toggleSave(post.id, AuthService.getUid())",
        "FirestoreService.toggleSave(post.id, AuthService.getUid(), newIsSaved)"
    )
    
    # 3. toggleBookSave
    content = content.replace(
        "FirestoreService.toggleBookSave(book.id, AuthService.getUid())",
        "FirestoreService.toggleBookSave(book.id, AuthService.getUid(), !book.isSaved)"
    )

    # 4. toggleBookListLike
    content = content.replace(
        "FirestoreService.toggleBookListLike(list.id, AuthService.getUid())",
        "FirestoreService.toggleBookListLike(list.id, AuthService.getUid(), newIsLiked)"
    )
    
    # 4.1 toggleBookListLike with currentUid
    content = content.replace(
        "FirestoreService.toggleBookListLike(list.id, currentUid)",
        "FirestoreService.toggleBookListLike(list.id, currentUid, newIsLiked)"
    )

    # 5. toggleBookListSave
    content = content.replace(
        "FirestoreService.toggleBookListSave(list.id, AuthService.getUid())",
        "FirestoreService.toggleBookListSave(list.id, AuthService.getUid(), newIsSaved)"
    )

    # 5.1 toggleBookListSave with currentUid
    content = content.replace(
        "FirestoreService.toggleBookListSave(list.id, currentUid)",
        "FirestoreService.toggleBookListSave(list.id, currentUid, newIsSaved)"
    )

    # CommentsComponent.kt replacements
    content = content.replace(
        "FirestoreService.toggleCommentLike(comment.id, currentUserId)",
        "FirestoreService.toggleCommentLike(comment.id, currentUserId, !currentLiked)"
    )

    with open(file_path, "w") as f:
        f.write(content)

print("UI toggle calls updated.")
