package com.example.paginex

import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// Utility function: converts an avatarUrl (which may be a regular URL or a Base64 data URI)
// into a model that Coil's AsyncImage can display.
@Composable
fun avatarModel(url: String, placeholder: String = "https://via.placeholder.com/200"): Any {
    if (url.isBlank()) return placeholder
    
    return remember(url) {
        if (url.startsWith("data:image/")) {
            try {
                val base64Part = url.substringAfter("base64,")
                val bytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: placeholder
            } catch (e: Exception) {
                placeholder
            }
        } else {
            url
        }
    }
}

// --- COLORS & THEME STATE ---
val LocalIsDarkTheme = compositionLocalOf { true }
val LocalThemeToggle = staticCompositionLocalOf<() -> Unit> { {} }

val PaginexSpace: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFF020408) else Color(0xFFF8F9FA)
val PaginexGalaxy: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFF0A0E1A) else Color(0xFFFFFFFF)
val PaginexNeonPurple: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFF918EF4) else Color(0xFF6C63FF)
val PaginexNeonTeal: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFF72C8C0) else Color(0xFF00BFA5)
val PaginexNeonPink: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFFE48C8C) else Color(0xFFFF5252)
val PaginexWhite: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFFF0F0F0) else Color(0xFF1A1A2E)
val PaginexGlass: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFFFFFFFF).copy(alpha = 0.08f) else Color(0xFF000000).copy(alpha = 0.04f)
val PaginexGlassBorder: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFFFFFFFF).copy(alpha = 0.15f) else Color(0xFF000000).copy(alpha = 0.08f)
val PaginexOrbit: Color @Composable get() = if (LocalIsDarkTheme.current) Color(0xFFFFFFFF).copy(alpha = 0.15f) else Color(0xFF000000).copy(alpha = 0.15f)

// --- THEME ---
@Composable
fun PaginexTheme(isDarkTheme: Boolean = LocalIsDarkTheme.current, content: @Composable () -> Unit) {
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = PaginexNeonPurple,
            onPrimary = Color.White,
            secondary = PaginexNeonTeal,
            background = PaginexSpace,
            surface = PaginexGalaxy,
            onBackground = PaginexWhite,
            onSurface = PaginexWhite
        )
    } else {
        lightColorScheme(
            primary = PaginexNeonPurple,
            onPrimary = Color.White,
            secondary = PaginexNeonTeal,
            background = PaginexSpace,
            surface = PaginexGalaxy,
            onBackground = PaginexWhite,
            onSurface = PaginexWhite
        )
    }
    CompositionLocalProvider(LocalIsDarkTheme provides isDarkTheme) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

@Composable
fun GenreChip(genre: String, isSelected: Boolean = false, onClick: () -> Unit) {
    val borderColor = if (isSelected) PaginexNeonPurple else PaginexGlassBorder
    val backgroundColor = if (isSelected) PaginexNeonPurple.copy(alpha = 0.2f) else PaginexGlass
    
    Surface(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = genre,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (isSelected) PaginexNeonPurple else PaginexWhite
            )
        }
    }
}

@Composable
fun SocialAction(icon: ImageVector, count: String, isSelected: Boolean = false, color: Color = PaginexWhite) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.Red else color,
            modifier = Modifier.size(24.dp)
        )
        if (count.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CosmicPlanet(
    book: Book,
    size: Dp = 100.dp,
    glowColor: Color = PaginexNeonPurple,
    isShattered: Boolean = false,
    isPulseEnabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseValue by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val pulse = if (isPulseEnabled) pulseValue else 1f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(size * pulse)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Planet Glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.4f), Color.Transparent),
                        center = center,
                        radius = size.toPx() / 2
                    ),
                    radius = size.toPx() / 2,
                    center = center
                )
            }
            
            // Book Cover as Planet Skin
            AsyncImage(
                model = book.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .clip(CircleShape)
                    .alpha(if (isShattered) 0.4f else 1f),
                contentScale = ContentScale.Crop
            )
            
            if (isShattered) {
                // Shatter effect lines
                Canvas(modifier = Modifier.fillMaxSize(0.7f)) {
                    val radius = size.toPx() / 2 * 0.7f
                    drawLine(Color.White, center, Offset(center.x + radius, center.y + radius), strokeWidth = 2f)
                    drawLine(Color.White, center, Offset(center.x - radius, center.y + radius), strokeWidth = 2f)
                }
            }
        }
        Text(
            text = book.title,
            color = PaginexWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PreviewBadge() {
    Surface(
        color = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Text(
            text = "Preview",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (glowColor, text) = when (status) {
        "Read" -> PaginexNeonTeal to status
        "Reading" -> PaginexNeonPurple to status
        "To Read" -> PaginexNeonPurple.copy(alpha = 0.5f) to status
        "On Hold" -> Color(0xFFD97706) to status
        "Dropped" -> PaginexNeonPink to status
        else -> Color.Gray to status
    }

    Surface(
        color = glowColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = glowColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookPostCard(
    post: Post, 
    onBookClick: (String) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleteClick: (Post) -> Unit = {},
    onLikeClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onCommentAdded: () -> Unit = {}
) {
    var postAuthor by remember { mutableStateOf<FireUser?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(post.userId) {
        postAuthor = FirestoreService.getUserById(post.userId)
        isFollowing = FirestoreService.isFollowing("u1", post.userId)
    }

    val timeAgo = remember(post.createdAt) {
        val diff = System.currentTimeMillis() - post.createdAt
        when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            diff < 2592000_000 -> "${diff / 86400_000}d ago"
            diff < 31536000_000 -> "${diff / 2592000_000}mo ago"
            else -> "${diff / 31536000_000}y ago"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = avatarModel(postAuthor?.avatarUrl ?: ""),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, PaginexNeonTeal, CircleShape)
                        .clickable { onUserClick(post.userId) },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onUserClick(post.userId) }
                ) {
                    Text(
                        text = postAuthor?.let { "${it.name} ${it.surname}" } ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PaginexWhite
                    )
                    Text(
                        text = postAuthor?.let { "@${it.username}" } ?: "@user",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                if (post.userId != "u1") {
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (isFollowing) {
                                    isFollowing = false
                                    MockData.currentUser = MockData.currentUser.copy(
                                        followingCount = (MockData.currentUser.followingCount - 1).coerceAtLeast(0)
                                    )
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.unfollowUser("u1", post.userId)
                                    }
                                } else {
                                    isFollowing = true
                                    MockData.currentUser = MockData.currentUser.copy(
                                        followingCount = MockData.currentUser.followingCount + 1
                                    )
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.followUser("u1", post.userId)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Followed" else "Follow",
                            color = if (isFollowing) Color.Gray else PaginexNeonTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                var menuExpanded by remember { mutableStateOf(false) }
                val isOwnPost = post.userId == MockData.currentUser.id

                if (isOwnPost) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E1E2E))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = PaginexWhite) },
                                onClick = {
                                    menuExpanded = false
                                    onEditClick(post.id)
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = PaginexNeonTeal) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick(post)
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = PaginexNeonPink) }
                            )
                        }
                    }
                } else {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Book Content Row
            Row(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = post.book.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, PaginexGlassBorder, RoundedCornerShape(12.dp))
                        .clickable { onBookClick(post.book.id) },
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    PreviewBadge()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.book.title,
                        fontSize = 20.sp,
                        color = PaginexWhite,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.book.author,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PaginexWhite.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, PaginexWhite.copy(alpha = 0.1f))
                        ) {
                            Text(
                                post.book.genre,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = PaginexWhite.copy(alpha = 0.7f)
                            )
                        }
                        if (!post.isBooklistPost) {
                            StatusBadge(post.status)
                        }
                        
                        if (!post.isBooklistPost && post.rating > 0f) {
                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${post.rating.toInt()}/10", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Review
            Text(
                text = post.review,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = PaginexWhite,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Social Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (post.isLiked) Color.Red else PaginexWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(post.likesCount.toString(), color = PaginexWhite, fontSize = 16.sp)
                
                Spacer(modifier = Modifier.width(24.dp))
                
                var showComments by remember { mutableStateOf(false) }
                IconButton(onClick = { showComments = true }) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline, 
                        contentDescription = null, 
                        tint = PaginexWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
                if (showComments) {
                    CommentsSheet(
                        tableId = post.id, 
                        onDismiss = { showComments = false },
                        onCommentAdded = onCommentAdded
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(post.commentsCount.toString(), color = PaginexWhite, fontSize = 16.sp)
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (!post.isBooklistPost) {
                    IconButton(onClick = onSaveClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (post.isSaved) PaginexNeonPurple else PaginexWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = timeAgo,
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = PaginexGlassBorder, thickness = 0.5.dp)
        }
    }
}
