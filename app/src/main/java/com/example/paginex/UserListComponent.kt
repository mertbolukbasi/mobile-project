package com.example.paginex

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListSheet(
    title: String,
    targetUserId: String = AuthService.getUid(),
    onDismiss: () -> Unit
) {
    val users = remember { mutableStateListOf<FireUser>() }

    LaunchedEffect(title, targetUserId) {
        val fetchedUsers = if (title == "Followers") {
            FirestoreService.getFollowers(targetUserId)
        } else {
            FirestoreService.getFollowing(targetUserId)
        }
        users.clear()
        users.addAll(fetchedUsers)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PaginexGlassBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.7f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = PaginexWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Nobody here yet.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(users) { user ->
                        UserListItem(user)
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: FireUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PaginexGlass)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = avatarModel(user.avatarUrl),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.dp, PaginexNeonPurple, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${user.name} ${user.surname}", color = PaginexWhite, fontWeight = FontWeight.Bold)
            Text("@${user.username}", color = Color.Gray, fontSize = 12.sp)
        }
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Profil", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLibrarySheet(
    targetUserId: String = AuthService.getUid(),
    onDismiss: () -> Unit
) {
    val isOwner = targetUserId == AuthService.getUid()
    val library = remember(targetUserId, MockData.readingStatuses.size, MockData.readingStatuses.toList()) {
        MockData.readingStatuses.filter { it.userId == targetUserId }.sortedByDescending { it.addedAt }
    }
    var deleteError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PaginexGlassBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Bookshelf",
                style = MaterialTheme.typography.titleLarge,
                color = PaginexWhite,
                fontWeight = FontWeight.Bold
            )
            if (deleteError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(deleteError!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                LaunchedEffect(deleteError) { delay(3000); deleteError = null }
            }
            Spacer(modifier = Modifier.height(20.dp))

            if (library.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("No books added yet.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(library, key = { it.id + it.status }) { status ->
                        LibraryBookItem(
                            status = status,
                            isOwner = isOwner,
                            onStatusChange = { newStatus ->
                                // Update local cache immediately
                                val idx = MockData.readingStatuses.indexOfFirst { it.id == status.id }
                                if (idx != -1) {
                                    MockData.readingStatuses[idx] = status.copy(status = newStatus)
                                }
                                // Write to Firestore in GlobalScope (survives sheet dismissal)
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.addBookToLibrary(targetUserId, status.book.id, newStatus)
                                }
                            },
                            onDelete = {
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    val success = FirestoreService.deleteBookFromLibrary(targetUserId, status.book.id)
                                    if (success) {
                                        MockData.readingStatuses.removeAll { it.userId == targetUserId && it.book.id == status.book.id }
                                    } else {
                                        deleteError = "Cannot delete \"${status.book.title}\" — you have posts or it is in a booklist."
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryBookItem(status: ReadingStatus, isOwner: Boolean = false, onStatusChange: (String) -> Unit = {}, onDelete: () -> Unit = {}) {
    var showDropdown by remember { mutableStateOf(false) }
    val statuses = listOf("Reading", "Read", "To Read", "On Hold", "Dropped")
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PaginexGlassBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = status.book.coverUrl.ifEmpty { R.drawable.ic_paginex_icon },
                contentDescription = null,
                modifier = Modifier.width(70.dp).fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(status.book.title, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(status.book.author, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                
                val statusColor = when (status.status) {
                    "Okunuyor", "Reading" -> PaginexNeonPurple
                    "Okunacak", "To Read", "Want to Read" -> PaginexOrbit
                    "Okundu", "Completed", "Read" -> PaginexNeonTeal
                    "On Hold" -> Color(0xFFD97706)
                    else -> Color.Gray
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(status.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    if (isOwner) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "✎",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { showDropdown = true }
                        )
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            statuses.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        showDropdown = false
                                        onStatusChange(s)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            if (isOwner) {
                IconButton(onClick = onDelete, modifier = Modifier.padding(end = 4.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444).copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySelectorSheet(
    targetUserId: String,
    onDismiss: () -> Unit,
    onBookSelected: (ReadingStatus) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val library = remember(targetUserId, MockData.readingStatuses.size) {
        MockData.readingStatuses.filter { it.userId == targetUserId }
    }
    
    val filteredLibrary = library.filter { 
        it.book.title.contains(searchQuery, ignoreCase = true) 
    }.sortedByDescending { it.addedAt }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PaginexGlassBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Select a Book",
                style = MaterialTheme.typography.titleLarge,
                color = PaginexWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                placeholder = { Text("Search your bookshelf...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaginexNeonPurple,
                    unfocusedBorderColor = PaginexGlassBorder,
                    focusedTextColor = PaginexWhite,
                    unfocusedTextColor = PaginexWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredLibrary.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No books found on your shelf.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredLibrary, key = { it.id }) { status ->
                        Box(modifier = Modifier.clickable { onBookSelected(status) }) {
                            LibraryBookItem(status)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListSelectorSheet(
    targetUserId: String,
    onDismiss: () -> Unit,
    onListSelected: (BookList) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val bookLists = remember(targetUserId, MockData.sampleBookLists.size) {
        MockData.sampleBookLists.filter { it.userId == targetUserId }
    }
    
    val filteredLists = bookLists.filter { 
        it.name.contains(searchQuery, ignoreCase = true) 
    }.sortedByDescending { it.createdAt }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PaginexGlassBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Select a Book List",
                style = MaterialTheme.typography.titleLarge,
                color = PaginexWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                placeholder = { Text("Search your lists...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaginexNeonPurple,
                    unfocusedBorderColor = PaginexGlassBorder,
                    focusedTextColor = PaginexWhite,
                    unfocusedTextColor = PaginexWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredLists.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No book lists found.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredLists, key = { it.id }) { list ->
                        Box(modifier = Modifier.clickable { onListSelected(list) }) {
                            BookListCard(
                                bookList = list,
                                isOwner = false,
                                onListClick = { onListSelected(list) },
                                onAddBook = { },
                                onLikeClick = { },
                                onSaveClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
