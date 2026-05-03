package com.example.paginex

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    tableId: String,
    onDismiss: () -> Unit,
    onCommentAdded: () -> Unit = {}
) {
    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<FireComment>() }
    val userCache = remember { mutableStateMapOf<String, FireUser?>() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Replying state
    var replyingTo by remember { mutableStateOf<FireComment?>(null) }
    var replyingToUser by remember { mutableStateOf<FireUser?>(null) }

    // Likes cache
    val likeCounts = remember { mutableStateMapOf<String, Int>() }
    val currentUserId = AuthService.getUid()
    val isLikedByUser = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(tableId) {
        val fetchedComments = FirestoreService.getComments(tableId)
        comments.clear()
        comments.addAll(fetchedComments)
        // Pre-fetch user data for all commenters
        fetchedComments.map { it.userId }.distinct().forEach { uid ->
            if (!userCache.containsKey(uid)) {
                userCache[uid] = FirestoreService.getUserById(uid)
            }
        }
        
        // Fetch likes for all comments
        fetchedComments.forEach { comment ->
            likeCounts[comment.id] = FirestoreService.getLikeCount(comment.id)
            isLikedByUser[comment.id] = FirestoreService.isLikedByUser(comment.id, currentUserId)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PaginexGalaxy,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PaginexGlassBorder) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    "Journey Notes",
                    color = PaginexWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                val sortedComments = remember(comments.toList()) {
                    val parents = comments.filter { it.parentId == null }
                    val result = mutableListOf<FireComment>()
                    parents.forEach { parent ->
                        result.add(parent)
                        val children = comments.filter { it.parentId == parent.id }
                        result.addAll(children)
                    }
                    result
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(sortedComments, key = { it.id }) { comment ->
                        val user = userCache[comment.userId]
                        CommentItem(
                            comment = comment,
                            user = user,
                            isReply = comment.parentId != null,
                            likeCount = likeCounts[comment.id] ?: 0,
                            isLiked = isLikedByUser[comment.id] ?: false,
                            onReplyClick = {
                                // Instagram style: always reply to the parent
                                val parentComment = if (comment.parentId == null) comment else {
                                    comments.find { it.id == comment.parentId } ?: comment
                                }
                                replyingTo = parentComment
                                replyingToUser = userCache[comment.userId]
                                // Auto-append @username
                                val tag = userCache[comment.userId]?.let { "@${it.username} " } ?: ""
                                if (!commentText.startsWith(tag)) {
                                    commentText = tag + commentText
                                }
                            },
                            onLikeClick = {
                                val currentLiked = isLikedByUser[comment.id] ?: false
                                val currentCount = likeCounts[comment.id] ?: 0
                                
                                // Optimistic UI
                                isLikedByUser[comment.id] = !currentLiked
                                likeCounts[comment.id] = if (currentLiked) currentCount - 1 else currentCount + 1
                                
                                scope.launch {
                                    FirestoreService.toggleCommentLike(comment.id, currentUserId, !currentLiked)
                                    isLikedByUser[comment.id] = !(isLikedByUser[comment.id] ?: false)
                                }
                            }
                        )
                    }
                }
            }

            // Input Section
            Surface(
                color = PaginexGalaxy,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .border(1.dp, PaginexGlassBorder.copy(alpha = 0.5f))
            ) {
                Column {
                    // Replying Indicator
                    AnimatedVisibility(
                        visible = replyingTo != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PaginexNeonPurple.copy(alpha = 0.1f))
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Replying to ${replyingToUser?.let { "@${it.username}" } ?: "user"}",
                                color = PaginexNeonPurple,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(
                                onClick = { 
                                    replyingTo = null
                                    replyingToUser = null
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = PaginexNeonPurple, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Whisper something...", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PaginexNeonPurple,
                                unfocusedBorderColor = PaginexGlassBorder,
                                focusedTextColor = PaginexWhite,
                                unfocusedTextColor = PaginexWhite,
                                unfocusedContainerColor = PaginexGlass,
                                focusedContainerColor = PaginexGlass
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    val currentUserId = AuthService.getUid()
                                    val commentObj = FireComment(
                                        id = "c_${System.currentTimeMillis()}",
                                        userId = currentUserId,
                                        comment = commentText,
                                        tableId = tableId,
                                        parentId = replyingTo?.id,
                                        createdAt = com.google.firebase.Timestamp.now(),
                                        updatedAt = com.google.firebase.Timestamp.now()
                                    )
                                    // Add to local cache
                                    comments.add(commentObj)
                                    likeCounts[commentObj.id] = 0
                                    isLikedByUser[commentObj.id] = false
                                    
                                    commentText = ""
                                    replyingTo = null
                                    replyingToUser = null
                                    onCommentAdded()
                                    
                                    scope.launch {
                                        FirestoreService.addComment(commentObj)
                                        if (!userCache.containsKey(currentUserId)) {
                                            userCache[currentUserId] = FirestoreService.getUserById(currentUserId)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(PaginexNeonPurple, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: FireComment, 
    user: FireUser? = null,
    isReply: Boolean = false,
    likeCount: Int = 0,
    isLiked: Boolean = false,
    onReplyClick: () -> Unit = {},
    onLikeClick: () -> Unit = {}
) {
    val displayName = if (user != null) "@${user.username}" else "@user_${comment.userId}"
    val avatarUrl = user?.avatarUrl ?: ""

    val timeText = remember(comment.createdAt) {
        val now = System.currentTimeMillis()
        val diff = now - comment.createdAt.toDate().time
        when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 2592000_000 -> "${diff / 86400_000}d ago"
            diff < 31536000_000 -> "${diff / 2592000_000}mo ago"
            else -> "${diff / 31536000_000}y ago"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 48.dp else 0.dp)
    ) {
        AsyncImage(
            model = avatarModel(avatarUrl),
            contentDescription = null,
            modifier = Modifier
                .size(if (isReply) 28.dp else 36.dp)
                .clip(CircleShape)
                .border(1.dp, PaginexNeonTeal, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    fontSize = 12.sp,
                    color = PaginexNeonTeal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeText,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = comment.comment,
                fontSize = 14.sp,
                color = PaginexWhite,
                lineHeight = 20.sp
            )
            
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reply",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onReplyClick() }
                )
                if (likeCount > 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$likeCount likes",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        IconButton(
            onClick = onLikeClick,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Color.Red else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
