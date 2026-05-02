package com.example.paginex

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(comments) { comment ->
                        val user = userCache[comment.userId]
                        CommentItem(comment, user)
                    }
                }
            }

            Surface(
                color = PaginexGalaxy,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .border(1.dp, PaginexGlassBorder.copy(alpha = 0.5f))
            ) {
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
                                val newComment = FireComment(
                                    id = "c_${System.currentTimeMillis()}",
                                    userId = "u1",
                                    comment = commentText,
                                    tableId = tableId,
                                    createdAt = com.google.firebase.Timestamp.now(),
                                    updatedAt = com.google.firebase.Timestamp.now()
                                )
                                // Add to local cache immediately
                                comments.add(newComment)
                                val savedText = commentText
                                commentText = ""
                                onCommentAdded()
                                // Write to Firestore in GlobalScope (survives sheet dismissal)
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.addComment(newComment)
                                    if (!userCache.containsKey("u1")) {
                                        userCache["u1"] = FirestoreService.getUserById("u1")
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

@Composable
fun CommentItem(comment: FireComment, user: FireUser? = null) {
    val displayName = if (user != null) "@${user.username}" else "@user_${comment.userId}"
    val avatarUrl = user?.avatarUrl ?: ""

    // Calculate relative time from createdAt
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

    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = avatarUrl.ifEmpty { "https://via.placeholder.com/200" },
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(1.dp, PaginexNeonTeal, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = displayName,
                fontSize = 12.sp,
                color = PaginexNeonTeal,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = comment.comment,
                fontSize = 14.sp,
                color = PaginexWhite,
                lineHeight = 20.sp
            )
            Text(
                text = timeText,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
