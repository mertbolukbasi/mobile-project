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
    onDismiss: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<FireComment>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(tableId) {
        val fetchedComments = FirestoreService.getComments(tableId)
        comments.clear()
        comments.addAll(fetchedComments)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PaginexGlassBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Yolculuk Notları",
                style = MaterialTheme.typography.titleLarge,
                color = PaginexWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(comments) { comment ->
                    CommentItem(comment)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comment Input
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Bir şeyler fısılda...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
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
                                tableId = tableId
                            )
                            scope.launch {
                                if (FirestoreService.addComment(newComment)) {
                                    comments.add(newComment)
                                    commentText = ""
                                }
                            }
                        }
                    },
                    modifier = Modifier.background(PaginexNeonPurple, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gönder", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: FireComment) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
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
                text = "@kullanıcı_${comment.userId}",
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
                text = "12 dk önce",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
