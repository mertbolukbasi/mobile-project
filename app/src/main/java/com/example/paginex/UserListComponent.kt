package com.example.paginex

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListSheet(
    title: String,
    onDismiss: () -> Unit
) {
    // Mocking users
    val users = listOf(
        FireUser(id = "u2", username = "kitapkurdu", name = "Can", surname = "Demir", avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200"),
        FireUser(id = "u3", username = "deniz_okur", name = "Deniz", surname = "Ak", avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&q=80&w=200")
    )

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
            model = user.avatarUrl,
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
