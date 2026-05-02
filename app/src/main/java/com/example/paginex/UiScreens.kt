package com.example.paginex

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.text.BasicTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState

// --- NAVIGATION ROUTES ---
sealed class Screen(val route: String, val icon: ImageVector? = null, val label: String? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home", Icons.Default.Home, "Home")
    object Explore : Screen("explore", Icons.Default.Search, "Explore")
    object CreatePost : Screen("create-post?postId={postId}", Icons.Default.AddCircle, "Add")
    object Saved : Screen("saved", Icons.Default.Bookmark, "Saved")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object BookDetail : Screen("detail/{postId}?ownerOnly={ownerOnly}")
    object EditProfile : Screen("edit_profile")
    object BookLists : Screen("lists")
    object Drafts : Screen("drafts")
    object Constellation : Screen("constellation")
    object Settings : Screen("settings")
    object PublicProfile : Screen("public_profile/{userId}") {
        fun createRoute(userId: String) = "public_profile/$userId"
    }
}

// --- APP COMPONENT ---
@Composable
fun PaginexApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Explore.route, Screen.CreatePost.route, 
        Screen.Saved.route, Screen.Profile.route
    )

    Scaffold(
        containerColor = PaginexSpace,
        bottomBar = {
            if (showBottomBar) {
                val items = listOf(Screen.Home, Screen.Explore, Screen.CreatePost, Screen.Saved, Screen.Profile)
                
                val bColor = PaginexSpace.copy(alpha = 0.85f)
                val neonPurple = PaginexNeonPurple
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp) // Slightly taller for more presence
                        .drawBehind {
                            // Neon Top Border with Glow
                            val shadowRadius = 10.dp.toPx()
                            val strokeWidth = 1.5.dp.toPx()
                            
                            // Stronger glow effect
                            drawRect(
                                color = neonPurple.copy(alpha = 0.3f),
                                topLeft = Offset(0f, 0f),
                                size = size.copy(height = strokeWidth * 3)
                            )
                            
                            drawLine(
                                color = neonPurple,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = strokeWidth
                            )
                        },
                    color = bColor, // Glassy base
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().blur(12.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)) // Subtle background depth
                    
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            val iconGlowScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.2f else 1f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            )
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { 
                                        navController.navigate(screen.route) { 
                                            popUpTo(navController.graph.startDestinationId); launchSingleTop = true 
                                        } 
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isSelected) {
                                        // Pulsing Active Background Glow
                                        val infiniteTransition = rememberInfiniteTransition()
                                        val bgAlpha by infiniteTransition.animateFloat(
                                            initialValue = 0.1f,
                                            targetValue = 0.25f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(1500),
                                                repeatMode = RepeatMode.Reverse
                                            )
                                        )
                                        Surface(
                                            modifier = Modifier.size(40.dp),
                                            shape = CircleShape,
                                            color = PaginexNeonPurple.copy(alpha = bgAlpha)
                                        ) {}
                                    }
                                    
                                    Icon(
                                        imageVector = screen.icon!!,
                                        contentDescription = screen.label,
                                        tint = if (isSelected) PaginexNeonPurple else Color.Gray,
                                        modifier = Modifier.size(24.dp * iconGlowScale)
                                    )
                                }
                                
                                if (screen.label != null) {
                                    Text(
                                        screen.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) PaginexNeonPurple else Color.Gray,
                                        modifier = Modifier.graphicsLayer(alpha = if (isSelected) 1f else 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Screen.Splash.route, modifier = Modifier.padding(padding)) {
            composable(Screen.Splash.route) { SplashScreen { navController.navigate(Screen.Login.route) } }
            composable(Screen.Login.route) { LoginScreen { navController.navigate(Screen.Home.route) } }
            composable(Screen.Home.route) { 
                PaginexHomeScreen(
                    onNavigateToDetail = { postId -> navController.navigate("detail/$postId") },
                    onNavigateToEdit = { postId -> navController.navigate("create-post?postId=$postId") },
                    onNavigateToUser = { userId -> 
                        if (userId == "u1" || userId == MockData.currentUser.id) navController.navigate(Screen.Profile.route)
                        else {
                            navController.navigate(Screen.PublicProfile.createRoute(userId))
                        }
                    }
                ) 
            }
            composable(Screen.Explore.route) { ExploreScreen(onBookClick = { postId -> navController.navigate("detail/$postId") }) }
            composable(
                route = Screen.CreatePost.route,
                arguments = listOf(navArgument("postId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null 
                })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")
                CreatePostScreen(
                    initialPostId = postId,
                    onPost = { 
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    },
                    onDraftsClick = { navController.navigate(Screen.Drafts.route) }
                ) 
            }
            composable(Screen.Saved.route) { SavedPostsScreen(onBookClick = { postId -> navController.navigate("detail/$postId") }) }
            composable(Screen.Profile.route) { PaginexProfileScreen(
                onEditClick = { navController.navigate(Screen.Settings.route) },
                onListsClick = { navController.navigate("book_lists/${MockData.currentUser.id.ifEmpty { "u1" }}") },
                onConstellationClick = { navController.navigate("constellation/${MockData.currentUser.id.ifEmpty { "u1" }}") },
                onBookClick = { postId -> navController.navigate("detail/$postId?ownerOnly=true") }
            ) }
            composable(Screen.EditProfile.route) { EditProfileScreen { navController.popBackStack() } }
            composable(
                route = "book_lists/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry -> 
                val userId = backStackEntry.arguments?.getString("userId") ?: MockData.currentUser.id.ifEmpty { "u1" }
                BookListsScreen(targetUserId = userId) { navController.popBackStack() } 
            }
            composable(Screen.Drafts.route) { DraftsScreen { navController.popBackStack() } }
            composable(
                route = "constellation/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry -> 
                val userId = backStackEntry.arguments?.getString("userId") ?: MockData.currentUser.id.ifEmpty { "u1" }
                ConstellationScreen(
                    targetUserId = userId,
                    onBack = { navController.popBackStack() },
                    onBookClick = { postId -> navController.navigate("detail/$postId") }
                ) 
            }
            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(
                    navArgument("postId") { type = NavType.StringType },
                    navArgument("ownerOnly") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry -> 
                val postId = backStackEntry.arguments?.getString("postId")
                val ownerOnly = backStackEntry.arguments?.getBoolean("ownerOnly") ?: false
                BookDetailScreen(postId, ownerOnly) { navController.popBackStack() } 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.PublicProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PublicProfileScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() },
                    onBookClick = { postId -> navController.navigate("detail/$postId") },
                    onListsClick = { navController.navigate("book_lists/${userId}") },
                    onConstellationClick = { navController.navigate("constellation/${userId}") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFavoritesSheet(
    currentFavorites: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(currentFavorites) } }
    val allBooks = remember(MockData.readingStatuses.size) {
        val currentUid = MockData.currentUser.id.ifEmpty { "u1" }
        MockData.readingStatuses.filter { it.userId == currentUid }.map { it.book }.distinctBy { it.id }
    }
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PaginexSpace,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Edit Favorite Books", color = PaginexWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Select up to 5 books to show in your galaxy (${selected.size}/5)", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search books...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaginexNeonPurple,
                    unfocusedBorderColor = PaginexGlassBorder,
                    focusedTextColor = PaginexWhite,
                    unfocusedTextColor = PaginexWhite
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                val filtered = allBooks.filter { it.title.lowercase().contains(searchQuery.lowercase()) }
                items(filtered) { book ->
                    val isSelected = selected.contains(book.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selected.remove(book.id)
                                } else if (selected.size < 5) {
                                    selected.add(book.id)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(uncheckedColor = Color.Gray, checkedColor = PaginexNeonPurple)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(book.title, color = PaginexWhite)
                    }
                }
            }
            
            Button(
                onClick = { onSave(selected) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- GALAXY COMPONENT ---
@Composable
fun UserGalaxy(
    user: User,
    rotation: Float,
    onBookClick: (String) -> Unit
) {
    val pOrbit = PaginexOrbit
    Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
        // Sun Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700).copy(alpha = 0.25f), Color.Transparent),
                    center = center,
                    radius = size.width / 2
                ),
                radius = size.width / 2
            )
            // Orbit Rings
            listOf(80.dp.toPx(), 110.dp.toPx(), 140.dp.toPx(), 165.dp.toPx()).forEach { r ->
                drawCircle(
                    color = pOrbit,
                    radius = r,
                    style = Stroke(width = 1f)
                )
            }
        }

        // Profile Photo
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .border(2.5.dp, Color(0xFFFFD700), CircleShape)
        ) {
            AsyncImage(
                model = user.avatarUrl.ifEmpty { "https://via.placeholder.com/200" },
                contentDescription = user.fullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Orbiting Books (Favorites Only)
        val favoriteBooks = remember(user.favoriteBooks, MockData.readingStatuses.size) {
            val favIds = user.favoriteBooks.toSet()
            val fromLibrary = MockData.readingStatuses.filter { it.userId == user.id && favIds.contains(it.book.id) }.map { it.book }
            val fromSample = MockData.sampleBooks.filter { favIds.contains(it.id) && fromLibrary.none { lb -> lb.id == it.id } }
            (fromLibrary + fromSample).take(5)
        }
        favoriteBooks.forEachIndexed { index, book ->
            val orbitRadiusDp = listOf(110f, 140f, 80f, 165f, 125f).getOrElse(index) { 140f }
            val speedMultiplier = 1f + (index * 0.2f)
            val baseAngle = index * (360f / favoriteBooks.size.coerceAtLeast(1))
            val angle = (rotation * speedMultiplier + baseAngle) % 360
            val rad = Math.toRadians(angle.toDouble())
            val xOffset = (orbitRadiusDp * cos(rad)).dp
            val yOffset = (orbitRadiusDp * sin(rad)).dp

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CosmicPlanet(
                    book = book,
                    size = 44.dp,
                    glowColor = PaginexNeonTeal,
                    isShattered = false,
                    onClick = { 
                        val p = MockData.feedPosts.find { it.book.id == book.id && it.userId == user.id }
                        if (p != null) onBookClick(p.id)
                    }
                )
            }
        }
    }
}

// --- SCREENS ---

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(2000); onFinish() }
    
    val isDark = LocalIsDarkTheme.current
    val bgColor = if (isDark) PaginexSpace else Color(0xFFFCFCFC)
    
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnimation = true }

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "logo_fade"
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(1500, easing = EaseOutExpo),
        label = "logo_scale"
    )

    Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_paginex_icon),
                    contentDescription = "Paginex Icon",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Paginex",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = PaginexWhite,
                    letterSpacing = (-1).sp
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                "Share your story.", 
                color = PaginexNeonTeal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )
        }
    }
}

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(PaginexSpace).padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Welcome to Paginex", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = "", 
            onValueChange = {}, 
            label = { Text("Email", color = Color.Gray) }, 
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PaginexNeonPurple, 
                unfocusedBorderColor = PaginexGlassBorder,
                focusedTextColor = PaginexWhite,
                unfocusedTextColor = PaginexWhite
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = "", 
            onValueChange = {}, 
            label = { Text("Password", color = Color.Gray) }, 
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PaginexNeonPurple, 
                unfocusedBorderColor = PaginexGlassBorder,
                focusedTextColor = PaginexWhite,
                unfocusedTextColor = PaginexWhite
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onLogin, 
            modifier = Modifier.fillMaxWidth().height(56.dp), 
            colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("LOGIN", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Don't have an account? ", color = Color.Gray)
            Text("Sign Up", color = PaginexNeonPurple, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaginexHomeScreen(
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToUser: (String) -> Unit = {}
) {
    val listState = rememberLazyListState()
    var currentSortMode by remember { mutableStateOf("Latest") }
    var isAscending by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Refresh data from Firestore on load and when explicitly needed
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            isRefreshing = true
            FirestoreService.syncMockData()
            isRefreshing = false
            listState.animateScrollToItem(0)
        }
    }

    val posts = when (currentSortMode) {
        "A-Z" -> if (isAscending) MockData.feedPosts.sortedBy { it.book.title } else MockData.feedPosts.sortedByDescending { it.book.title }
        "Rating" -> {
            val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val recentPuanPosts = MockData.feedPosts.filter { it.createdAt >= oneMonthAgo && it.rating > 0f }
                .let { filtered ->
                    if (isAscending) {
                        filtered.sortedWith(compareBy<Post> { it.rating }.thenByDescending { it.createdAt })
                    } else {
                        filtered.sortedWith(compareByDescending<Post> { it.rating }.thenByDescending { it.createdAt })
                    }
                }
            
            if (recentPuanPosts.isEmpty()) {
                MockData.feedPosts.filter { it.rating > 0f }
                    .let { filtered ->
                        if (isAscending) {
                            filtered.sortedWith(compareBy<Post> { it.rating }.thenByDescending { it.createdAt })
                        } else {
                            filtered.sortedWith(compareByDescending<Post> { it.rating }.thenByDescending { it.createdAt })
                        }
                    }
                    .take(2)
            } else {
                recentPuanPosts
            }
        }
        else -> MockData.feedPosts.sortedByDescending { it.createdAt } // Default to most recent
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = PaginexSpace,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { 
            CenterAlignedTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_paginex_icon),
                            contentDescription = "Paginex Icon",
                            modifier = Modifier.size(48.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Paginex",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PaginexWhite,
                            letterSpacing = (-1).sp
                        )
                    }
                },
                actions = {
                    val toggleTheme = LocalThemeToggle.current
                    val isDark = LocalIsDarkTheme.current
                    IconButton(onClick = toggleTheme) {
                        Text(if (isDark) "🌞" else "🌙", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Sort Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sortOptions = listOf("Latest", "A-Z", "Rating")
                items(sortOptions, key = { it }) { option ->
                    val isSelected = currentSortMode == option
                    val label = if ((option == "A-Z" || option == "Rating") && isSelected) {
                        if (isAscending) "$option ↑" else "$option ↓"
                    } else option
                    
                    val contentColor = if (isSelected) Color.White else PaginexWhite
                    val containerColor = if (isSelected) PaginexNeonPurple else PaginexGlass
                    val borderColor = if (isSelected) PaginexNeonPurple else PaginexGlassBorder

                    Surface(
                        onClick = { 
                            if (currentSortMode == option) {
                                // If already selected, toggle direction if applicable
                                if (option == "A-Z" || option == "Rating") {
                                    isAscending = !isAscending
                                }
                            } else {
                                // If a new one is clicked, set it as the only active one
                                currentSortMode = option
                                // Default directions for new selection
                                isAscending = option == "A-Z"
                            }
                        },
                        color = containerColor,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { refreshTrigger++ },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                            BookPostCard(
                                post = post,
                                onBookClick = { bookId -> onNavigateToDetail(post.id) },
                                onUserClick = onNavigateToUser,
                                onEditClick = { postId -> onNavigateToEdit(postId) },
                                onDeleteClick = { postToDelete ->
                                val now = System.currentTimeMillis()
                                val diff = now - postToDelete.createdAt
                                if (diff > 5 * 60 * 1000) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Posts can only be deleted within 5 minutes of creation.")
                                    }
                                } else {
                                    // Update local cache immediately
                                    MockData.feedPosts.removeAll { it.id == postToDelete.id }
                                    // Write to Firestore in GlobalScope
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.deletePost(postToDelete.id)
                                    }
                                }
                            },
                            onLikeClick = {
                                // Update local cache immediately
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
                                    FirestoreService.toggleLike(post.id, "u1")
                                }
                            },
                            onSaveClick = {
                                // Update local cache immediately
                                val index = MockData.feedPosts.indexOfFirst { it.id == post.id }
                                if (index != -1) {
                                    val currentPost = MockData.feedPosts[index]
                                    MockData.feedPosts[index] = currentPost.copy(
                                        isSaved = !currentPost.isSaved
                                    )
                                }
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleSave(post.id, "u1")
                                }
                            },
                            onCommentAdded = {
                                val index = MockData.feedPosts.indexOfFirst { it.id == post.id }
                                if (index != -1) {
                                    val currentPost = MockData.feedPosts[index]
                                    MockData.feedPosts[index] = currentPost.copy(
                                        commentsCount = currentPost.commentsCount + 1
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(onBookClick: (String) -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition()
    val nebulaOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Sync explore data from database
    LaunchedEffect(Unit) {
        FirestoreService.syncMockData()
    }

    Column(modifier = Modifier.fillMaxSize().background(PaginexSpace)) {
        // Nebula Header
        val neonPurple = PaginexNeonPurple
        val neonTeal = PaginexNeonTeal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().blur(60.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(neonPurple.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(nebulaOffset % size.width, size.height / 2),
                        radius = size.width
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(neonTeal.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(size.width - (nebulaOffset % size.width), size.height / 2),
                        radius = size.width
                    )
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    "Explore the Universe",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = PaginexWhite,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Minimalist Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    placeholder = { Text("Search in stardust...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PaginexWhite.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = PaginexGlass,
                        focusedContainerColor = PaginexGlass,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedBorderColor = PaginexNeonTeal
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Discover Lists Section
        val publicLists = remember(MockData.sampleBookLists.size) {
            MockData.sampleBookLists.filter { !it.isPrivate && it.userId != "u1" }
        }
        if (publicLists.isNotEmpty()) {
            Text(
                "Popular Galaxies",
                modifier = Modifier.padding(horizontal = 24.dp),
                color = PaginexWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(publicLists) { list ->
                    Box(modifier = Modifier.width(200.dp)) {
                        BookListCard(
                            bookList = list,
                            isOwner = false,
                            onAddBook = {},
                            onLikeClick = {
                                val idx = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                if (idx != -1) {
                                    val current = MockData.sampleBookLists[idx]
                                    val newIsLiked = !current.isLiked
                                    MockData.sampleBookLists[idx] = current.copy(
                                        isLiked = newIsLiked,
                                        likesCount = if (newIsLiked) current.likesCount + 1 else (current.likesCount - 1).coerceAtLeast(0)
                                    )
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.toggleBookListLike(list.id, "u1")
                                    }
                                }
                            },
                            onSaveClick = {
                                val idx = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                                if (idx != -1) {
                                    val current = MockData.sampleBookLists[idx]
                                    MockData.sampleBookLists[idx] = current.copy(isSaved = !current.isSaved)
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.toggleBookListSave(list.id, "u1")
                                    }
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Genre Categories
        val genres = listOf("All", "Sci-Fi", "Dystopia", "Classic", "Novel", "History", "Mystery", "Art")
        var selectedGenre by remember { mutableStateOf("All") }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(genres) { genre ->
                GenreChip(
                    genre = genre,
                    isSelected = genre == selectedGenre,
                    onClick = { selectedGenre = genre }
                )
            }
        }

        // Dynamic Staggered Grid
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp
        ) {
            items(MockData.sampleBooks) { book ->
                val randomHeight = remember { (200..350).random().dp }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(randomHeight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PaginexGlass)
                        .border(1.dp, PaginexGlassBorder, RoundedCornerShape(24.dp))
                        .clickable { 
                            val post = MockData.feedPosts.find { it.book.title == book.title }
                            post?.let { onBookClick(it.id) }
                        }
                ) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Glow Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.4f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.9f)
                                )
                            )
                    )
                    Text(
                        text = book.title,
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(postId: String?, ownerOnly: Boolean = false, onBack: () -> Unit) {
    val initialPost = MockData.feedPosts.find { it.id == postId } ?: MockData.feedPosts.firstOrNull() ?: return
    val book = initialPost.book
    val bookReviews = MockData.feedPosts.filter { 
        it.book.id == book.id && (!ownerOnly || it.userId == "u1")
    }
    val avgRating = if (bookReviews.isNotEmpty()) bookReviews.filter { it.rating > 0f }.map { it.rating }.average().let { if (it.isNaN()) 0f else it.toFloat() } else 0f

    // Cache for reviewer user data
    val reviewerCache = remember { mutableStateMapOf<String, FireUser?>() }
    LaunchedEffect(bookReviews) {
        bookReviews.map { it.userId }.distinct().forEach { uid ->
            if (!reviewerCache.containsKey(uid)) {
                reviewerCache[uid] = FirestoreService.getUserById(uid)
            }
        }
    }

    Scaffold(
        containerColor = PaginexSpace,
        topBar = { 
            TopAppBar(
                title = { Text("Planet Detail", color = PaginexWhite) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CosmicPlanet(book = book, size = 200.dp, glowColor = PaginexNeonPurple)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(book.title, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)
            Text(book.author, color = PaginexNeonTeal, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            // New fields: Publish Year, ISBN and Average Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Year: ${book.publishYear}", color = Color.Gray, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ISBN: ${if (book.isbn.isNotEmpty()) book.isbn else "000-0000000000"}", color = Color.Gray, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(String.format("%.1f/10", avgRating), color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.weight(1f))
                
                var isSaved by remember { mutableStateOf(false) }
                IconButton(onClick = { isSaved = !isSaved }) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Book",
                        tint = if (isSaved) PaginexNeonPurple else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Book Summary
            Text("Book Summary", color = PaginexWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (book.summary.isNotEmpty()) book.summary else "No summary available for this book yet...",
                color = PaginexWhite.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Reviews
            Text("Journey Notes (${bookReviews.size})", color = PaginexWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            bookReviews.forEach { reviewPost ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = PaginexGlass),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PaginexGlassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val reviewer = reviewerCache[reviewPost.userId]
                            AsyncImage(
                                model = reviewer?.avatarUrl?.ifEmpty { "https://via.placeholder.com/200" } ?: "https://via.placeholder.com/200", 
                                contentDescription = null,
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reviewer?.let { "@${it.username}" } ?: "@user", color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            if (reviewPost.rating > 0f) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${reviewPost.rating.toInt()}/10", fontSize = 12.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = reviewPost.review,
                            lineHeight = 22.sp,
                            color = PaginexWhite.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaginexProfileScreen(
    onEditClick: () -> Unit, // This will now go to SettingsScreen
    onListsClick: () -> Unit,
    onConstellationClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onLogoutClick: () -> Unit = {} // This might not be needed directly here anymore, but keeping it
) {
    val user = MockData.currentUser
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    var showAddBookDialog by remember { mutableStateOf(false) }
    val currentlyReading = MockData.readingStatuses.filter { it.status == "Reading" && it.userId == (MockData.currentUser.id.ifEmpty { "u1" }) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(PaginexSpace)) {
        // Logout Icon removed, moved to Settings.
        // Instead, the top-right gear icon goes to Settings

        // Settings Icon in top-right corner
        IconButton(
            onClick = onEditClick, // This corresponds to nav to Settings
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .zIndex(10f)
                .background(PaginexGlass, CircleShape)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PaginexWhite.copy(alpha = 0.7f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ---- GALAXY VISUALIZATION ----
            UserGalaxy(user = user, rotation = rotation, onBookClick = onBookClick)

            Spacer(modifier = Modifier.height(12.dp))

            var showFavoritesDialog by remember { mutableStateOf(false) }
            TextButton(onClick = { showFavoritesDialog = true }) {
                Text("Edit Favorites (${user.favoriteBooks.size}/5)", color = PaginexNeonPurple)
            }
            if (showFavoritesDialog) {
                EditFavoritesSheet(
                    currentFavorites = user.favoriteBooks,
                    onDismiss = { showFavoritesDialog = false },
                    onSave = { selectedIds ->
                        // Update local cache immediately
                        MockData.currentUser = MockData.currentUser.copy(favoriteBooks = selectedIds)
                        showFavoritesDialog = false
                        // Write to Firestore in GlobalScope
                        kotlinx.coroutines.MainScope().launch {
                            FirestoreService.updateFavoriteBooks("u1", selectedIds)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // User Name + Username
            Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = PaginexWhite)
            Text("@${user.username}", color = PaginexNeonPurple, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // Bio
            Text(
                user.bio,
                color = PaginexWhite.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Join Date
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text(" ${user.joinDate}", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            var showFollowers by remember { mutableStateOf(false) }
            var showFollowing by remember { mutableStateOf(false) }
            var showLibrary by remember { mutableStateOf(false) }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showLibrary = true }
                ) {
                    Text("${MockData.readingStatuses.count { it.userId == user.id }}", fontWeight = FontWeight.ExtraBold, color = PaginexNeonTeal, fontSize = 20.sp)
                    Text("Books", fontSize = 11.sp, color = Color.Gray)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showFollowers = true }
                ) {
                    Text("${user.followersCount}", fontWeight = FontWeight.ExtraBold, color = PaginexWhite, fontSize = 20.sp)
                    Text("Followers", fontSize = 11.sp, color = Color.Gray)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showFollowing = true }
                ) {
                    Text("${user.followingCount}", fontWeight = FontWeight.ExtraBold, color = PaginexWhite, fontSize = 20.sp)
                    Text("Following", fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (showLibrary) {
                UserLibrarySheet(targetUserId = user.id, onDismiss = { showLibrary = false })
            }

            if (showFollowers) {
                UserListSheet("Followers", onDismiss = { showFollowers = false })
            }
            if (showFollowing) {
                UserListSheet("Following", onDismiss = { showFollowing = false })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Book Button
                Button(
                    onClick = { showAddBookDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Book", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                // Book Lists Button
                OutlinedButton(
                    onClick = onListsClick,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, PaginexNeonPurple.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.List, null, tint = PaginexNeonPurple, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("My Lists", color = PaginexNeonPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Constellation Button
            TextButton(
                onClick = onConstellationClick,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Constellation Map", color = Color(0xFFFFD700), fontSize = 13.sp)
            }



            Spacer(modifier = Modifier.height(20.dp))


            // ---- READING SECTION ----
            if (currentlyReading.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = PaginexNeonPurple.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, PaginexNeonPurple.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing dot
                            val pulse by infiniteTransition.animateFloat(
                                initialValue = 0.4f, targetValue = 1f,
                                animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = ""
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(PaginexNeonPurple.copy(alpha = pulse), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "CURRENTLY READING",
                                color = PaginexNeonPurple,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        currentlyReading.forEach { rs ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                AsyncImage(
                                    model = rs.book.coverUrl.ifEmpty { R.drawable.ic_paginex_icon },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(44.dp, 64.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(rs.book.title, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(rs.book.author, color = Color.Gray, fontSize = 12.sp)
                                    Text(rs.book.genre, color = PaginexNeonPurple.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }


            // ---- MY POSTS SECTION ----
            val myPosts = MockData.feedPosts.filter { it.userId == (user.id.ifEmpty { "u1" }) }
            if (myPosts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = PaginexNeonTeal.copy(alpha = 0.06f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, PaginexNeonTeal.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, null, tint = PaginexNeonTeal, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "MY POSTS",
                                color = PaginexNeonTeal,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${myPosts.size}", color = PaginexNeonTeal.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        myPosts.take(10).forEach { post ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PaginexGlass)
                                    .clickable { onBookClick(post.id) }
                                    .padding(10.dp)
                                    .fillMaxWidth()
                            ) {
                                AsyncImage(
                                    model = post.book.coverUrl.ifEmpty { R.drawable.ic_paginex_icon },
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp, 58.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.book.title, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (post.review.isNotEmpty()) {
                                        Text(post.review, color = Color.Gray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                if (post.rating > 0f) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                        Text("${post.rating.toInt()}/10", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }


        }
    }

    // Add Book Dialog
    if (showAddBookDialog) {
        AddBookToLibraryDialog(
            onDismiss = { showAddBookDialog = false },
            onBookAdded = { book, status ->
                val uid = user.id.ifEmpty { "u1" }
                // Optimistically add to local cache
                val newStatus = ReadingStatus(
                    id = "rs_${System.currentTimeMillis()}",
                    userId = uid,
                    book = book,
                    status = status
                )
                val existingIndex = MockData.readingStatuses.indexOfFirst { it.userId == uid && it.book.id == book.id }
                if (existingIndex != -1) {
                    MockData.readingStatuses[existingIndex] = newStatus
                } else {
                    MockData.readingStatuses.add(newStatus)
                }
                // Write to Firestore in GlobalScope (survives composable lifecycle)
                kotlinx.coroutines.MainScope().launch {
                    FirestoreService.addBookToLibrary(uid, book.id, status)
                }
                showAddBookDialog = false 
            }
        )
    }
}

@Composable
fun EditProfileScreen(onSave: () -> Unit) {
    val user = MockData.currentUser
    var email by remember { mutableStateOf("user@paginex.com") } // Placeholder
    var name by remember { mutableStateOf(user.fullName) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(PaginexSpace)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSave, modifier = Modifier.padding(end = 16.dp)) {
                    Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite)
                }
                Text(
                    "Edit profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PaginexWhite,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Avatar Edit
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = user.avatarUrl.ifEmpty { "https://via.placeholder.com/200" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, PaginexNeonPurple, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Edit image", color = PaginexNeonTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Form Fields
            CosmicInputField(label = "Email address", value = email, onValueChange = { email = it })
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "Name", value = name, onValueChange = { name = it })
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Change password", color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            CosmicInputField(label = "Old password", value = oldPassword, onValueChange = { oldPassword = it })
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "New password", value = newPassword, onValueChange = { newPassword = it })
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = onSave, 
                modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp).width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
                shape = RoundedCornerShape(25.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) { 
                Text("Save", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = Color.Black) 
            }
        }
    }
}

@Composable
fun CosmicInputField(label: String, value: String, onValueChange: (String) -> Unit, isLongText: Boolean = false) {
    Column {
        Text(label, color = PaginexNeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = PaginexGlass),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PaginexGlassBorder)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth().then(if (isLongText) Modifier.height(120.dp) else Modifier),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedTextColor = PaginexWhite,
                    unfocusedTextColor = PaginexWhite
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun SavedPostsScreen(onBookClick: (String) -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }
    var isAscending by remember { mutableStateOf(true) }
    
    val uniqueBooks = MockData.feedPosts
        .filter { it.isSaved && it.book.title.lowercase().startsWith(searchQuery.lowercase()) }
        .distinctBy { it.book.id }
        .let { posts ->
            if (isAscending) posts.sortedBy { it.book.title } else posts.sortedByDescending { it.book.title }
        }

    Column(modifier = Modifier.fillMaxSize().background(PaginexSpace).padding(16.dp)) {
        Text("Saved Stars", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search book...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaginexNeonPurple,
                    unfocusedBorderColor = PaginexGlassBorder,
                    focusedTextColor = PaginexWhite,
                    unfocusedTextColor = PaginexWhite,
                    unfocusedContainerColor = PaginexGlass,
                    focusedContainerColor = PaginexGlass
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { isAscending = !isAscending },
                modifier = Modifier.background(PaginexGlass, RoundedCornerShape(12.dp)).border(1.dp, PaginexGlassBorder, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    if (isAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = PaginexNeonPurple
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(uniqueBooks) { post ->
                CosmicPlanet(
                    book = post.book,
                    size = 120.dp,
                    glowColor = PaginexNeonTeal,
                    onClick = { onBookClick(post.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListsScreen(targetUserId: String, onBack: () -> Unit) {
    val currentUid = MockData.currentUser.id.ifEmpty { "u1" }
    val isOwner = targetUserId == currentUid
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val bookLists = remember(MockData.sampleBookLists.size, targetUserId, selectedTab) {
        if (isOwner) {
            if (selectedTab == 0) {
                MockData.sampleBookLists.filter { it.userId == targetUserId }
            } else {
                MockData.sampleBookLists.filter { it.isSaved && it.userId != targetUserId }
            }
        } else {
            // Viewing someone else's lists: only show their public ones
            MockData.sampleBookLists.filter { it.userId == targetUserId && !it.isPrivate }
        }
    }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedListForAdd by remember { mutableStateOf<BookList?>(null) }
    var listToEdit by remember { mutableStateOf<BookList?>(null) }

    LaunchedEffect(Unit) {
        FirestoreService.syncMockData()
    }

    Scaffold(
        containerColor = PaginexSpace,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (isOwner) "My Book Lists" else "Book Lists", color = PaginexWhite, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite)
                        }
                    },
                    actions = {
                        if (isOwner) {
                            IconButton(onClick = { showCreateDialog = true }) {
                                Icon(Icons.Default.Add, null, tint = PaginexNeonPurple)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                
                if (isOwner) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = PaginexNeonPurple,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = PaginexNeonPurple
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("My Lists", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Saved", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (bookLists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedTab == 0) "No lists found." else "No saved lists found.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(bookLists, key = { it.id }) { list ->
                    BookListCard(
                        bookList = list,
                        isOwner = list.userId == currentUid,
                        onAddBook = { selectedListForAdd = list },
                        onLikeClick = {
                            val idx = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                            if (idx != -1) {
                                val current = MockData.sampleBookLists[idx]
                                val newIsLiked = !current.isLiked
                                MockData.sampleBookLists[idx] = current.copy(
                                    isLiked = newIsLiked,
                                    likesCount = if (newIsLiked) current.likesCount + 1 else (current.likesCount - 1).coerceAtLeast(0)
                                )
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListLike(list.id, currentUid)
                                }
                            }
                        },
                        onSaveClick = {
                            val idx = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                            if (idx != -1) {
                                val current = MockData.sampleBookLists[idx]
                                MockData.sampleBookLists[idx] = current.copy(isSaved = !current.isSaved)
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListSave(list.id, currentUid)
                                }
                            }
                        },
                        onEditClick = { listToEdit = list },
                        onDeleteClick = {
                            MockData.sampleBookLists.removeAll { it.id == list.id }
                            kotlinx.coroutines.MainScope().launch {
                                FirestoreService.deleteBookList(list.id)
                            }
                        },
                        onRemoveBookClick = { bookId ->
                            val idx = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                            if (idx != -1) {
                                val current = MockData.sampleBookLists[idx]
                                val updatedBooks = current.books.toMutableList()
                                updatedBooks.removeAll { it.id == bookId }
                                MockData.sampleBookLists[idx] = current.copy(books = updatedBooks)
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.removeBookFromList(list.id, bookId)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateBookListDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, desc, isPrivate ->
                kotlinx.coroutines.MainScope().launch {
                    val newList = FirestoreService.createBookList(name, desc, targetUserId, isPrivate)
                    if (newList != null) {
                        MockData.sampleBookLists.add(0, newList)
                    }
                    showCreateDialog = false
                }
            }
        )
    }

    listToEdit?.let { list ->
        EditBookListDialog(
            bookList = list,
            onDismiss = { listToEdit = null },
            onSave = { name, desc, isPrivate ->
                val idx = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                if (idx != -1) {
                    MockData.sampleBookLists[idx] = MockData.sampleBookLists[idx].copy(
                        name = name,
                        description = desc,
                        isPrivate = isPrivate
                    )
                }
                kotlinx.coroutines.MainScope().launch {
                    FirestoreService.updateBookList(list.id, name, desc, isPrivate)
                }
                listToEdit = null
            }
        )
    }

    selectedListForAdd?.let { list ->
        AddBookToListDialog(
            bookList = list,
            onDismiss = { selectedListForAdd = null },
            onBookAdded = { book ->
                val idx = MockData.sampleBookLists.indexOfFirst { it.id == list.id }
                if (idx != -1) {
                    val updatedBooks = MockData.sampleBookLists[idx].books.toMutableList()
                    if (updatedBooks.none { it.id == book.id }) {
                        updatedBooks.add(book)
                        MockData.sampleBookLists[idx] = MockData.sampleBookLists[idx].copy(books = updatedBooks)
                        kotlinx.coroutines.MainScope().launch {
                            FirestoreService.addBookToList(list.id, book.id)
                        }
                    }
                }
                selectedListForAdd = null
            }
        )
    }
}

@Composable
fun EditBookListDialog(bookList: BookList, onDismiss: () -> Unit, onSave: (String, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf(bookList.name) }
    var description by remember { mutableStateOf(bookList.description) }
    var isPrivate by remember { mutableStateOf(bookList.isPrivate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Edit List", color = PaginexWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaginexNeonPurple,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedTextColor = PaginexWhite,
                        unfocusedTextColor = PaginexWhite
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaginexNeonPurple,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedTextColor = PaginexWhite,
                        unfocusedTextColor = PaginexWhite
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Private List", color = PaginexWhite, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = PaginexNeonPurple)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, description, isPrivate) },
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun BookListCard(
    bookList: BookList,
    isOwner: Boolean,
    onAddBook: () -> Unit,
    onLikeClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRemoveBookClick: (String) -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
        border = BorderStroke(1.dp, PaginexGlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cover: stacked book covers or placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaginexNeonPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bookList.coverUrl.isNotEmpty()) {
                        AsyncImage(
                            model = bookList.coverUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.List, null, tint = PaginexNeonPurple, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(bookList.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PaginexWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (bookList.isPrivate) Icons.Default.Lock else Icons.Default.Public,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (bookList.description.isNotEmpty()) {
                        Text(bookList.description, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                    }
                    Text("${bookList.books.size} books", fontSize = 12.sp, color = PaginexNeonPurple.copy(alpha = 0.8f))
                }
                
                if (isOwner) {
                    IconButton(onClick = onAddBook) {
                        Icon(Icons.Default.Add, null, tint = PaginexNeonPurple)
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(PaginexGalaxy)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = PaginexWhite) },
                                onClick = { menuExpanded = false; onEditClick() },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = PaginexNeonTeal) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color.Red) },
                                onClick = { menuExpanded = false; onDeleteClick() },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = PaginexNeonPink) }
                            )
                        }
                    }
                } else {
                    // Social actions for non-owned public lists
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onLikeClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (bookList.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (bookList.isLiked) Color.Red else PaginexWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(bookList.likesCount.toString(), color = PaginexWhite, fontSize = 12.sp)
                        
                        IconButton(onClick = onSaveClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (bookList.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (bookList.isSaved) PaginexNeonPurple else PaginexWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Horizontal book cover strip
            if (bookList.books.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bookList.books) { book ->
                        Box {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = book.title,
                                modifier = Modifier
                                    .size(44.dp, 64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, PaginexGlassBorder, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (isOwner) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .clickable { onRemoveBookClick(book.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateBookListDialog(onDismiss: () -> Unit, onCreate: (String, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Create New List", color = PaginexWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaginexNeonPurple,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedTextColor = PaginexWhite,
                        unfocusedTextColor = PaginexWhite
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaginexNeonPurple,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedTextColor = PaginexWhite,
                        unfocusedTextColor = PaginexWhite
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Private List", color = PaginexWhite, modifier = Modifier.weight(1f))
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = PaginexNeonPurple)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, description, isPrivate) },
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
            ) {
                Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun AddBookToListDialog(bookList: BookList, onDismiss: () -> Unit, onBookAdded: (Book) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text("Add Book", color = PaginexWhite, fontWeight = FontWeight.Bold)
                Text("→ ${bookList.name}", color = PaginexNeonPurple, fontSize = 13.sp)
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val currentUid = MockData.currentUser.id.ifEmpty { "u1" }
                val userLibraryBooks = MockData.readingStatuses.filter { it.userId == currentUid }.map { it.book }
                val availableBooks = userLibraryBooks.filter { b -> bookList.books.none { it.id == b.id } }
                
                items(availableBooks) { book ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaginexGlass)
                            .clickable { onBookAdded(book) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = book.coverUrl,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp, 52.dp).clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(book.title, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(book.author, color = Color.Gray, fontSize = 12.sp)
                            Text(book.genre, color = PaginexNeonTeal.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) }
        }
    )
}

@Composable
fun AddBookToLibraryDialog(onDismiss: () -> Unit, onBookAdded: (Book, String) -> Unit) {
    var selectedStatus by remember { mutableStateOf("To Read") }
    val statuses = listOf("Reading", "Read", "To Read", "On Hold", "Dropped")
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Add Book to Library", color = PaginexWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Select status:", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(statuses) { s ->
                        val color = when(s) {
                            "Read" -> PaginexNeonTeal
                            "Reading" -> PaginexNeonPurple
                            "Dropped" -> PaginexNeonPink
                            "On Hold" -> Color(0xFFD97706)
                            else -> PaginexWhite.copy(alpha = 0.6f)
                        }
                        Surface(
                            onClick = { selectedStatus = s },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedStatus == s) color.copy(alpha = 0.2f) else PaginexGlass,
                            border = BorderStroke(1.dp, if (selectedStatus == s) color else PaginexGlassBorder)
                        ) {
                            Text(s, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (selectedStatus == s) color else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    placeholder = { Text("Search...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaginexNeonPurple,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedTextColor = PaginexWhite,
                        unfocusedTextColor = PaginexWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Select book:", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val currentUid = MockData.currentUser.id.ifEmpty { "u1" }
                    val existingBookIds = MockData.readingStatuses.filter { it.userId == currentUid }.map { it.book.id }.toSet()
                    val filteredBooks = MockData.sampleBooks.filter { it.title.contains(searchQuery, ignoreCase = true) && !existingBookIds.contains(it.id) }
                    items(filteredBooks) { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(PaginexGlass)
                                .clickable { onBookAdded(book, selectedStatus) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp, 48.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(book.title, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(book.author, color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) }
        }
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstellationScreen(targetUserId: String, onBack: () -> Unit, onBookClick: (String) -> Unit) {
    var targetUser by remember { mutableStateOf(MockData.currentUser) }
    LaunchedEffect(targetUserId) {
        if (targetUserId != MockData.currentUser.id && targetUserId != "u1") {
            FirestoreService.getUserProfile(targetUserId)?.let { targetUser = it }
        }
    }
    val readingList = remember(MockData.readingStatuses.size, targetUserId) {
        MockData.readingStatuses.filter { it.userId == targetUserId }
    }
    val infiniteTransition = rememberInfiniteTransition()
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    val twinkle2 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(2100, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    val twinkle3 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    val twinkles = listOf(twinkle, twinkle2, twinkle3)

    val genreMap = readingList.groupBy { it.book.genre }
    val genres = genreMap.keys.toList()

    val animScale = remember { Animatable(1f) }
    val animOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scope = rememberCoroutineScope()
    var expandedGenres by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        containerColor = PaginexSpace,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Constellation", color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        val isMe = targetUserId == "u1" || targetUserId == MockData.currentUser.id
                        Text(if (isMe) "My Book Universe" else "Book Universe", color = Color(0xFFFFD700), fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PaginexSpace)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scope.launch {
                            animScale.stop()
                            animOffset.stop()
                            animScale.snapTo((animScale.value * zoom).coerceIn(0.5f, 4f))
                            animOffset.snapTo(animOffset.value + pan)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rng = java.util.Random(42L)
                repeat(120) {
                    val x = rng.nextFloat() * size.width
                    val y = rng.nextFloat() * size.height
                    val r = rng.nextFloat() * 2f + 0.5f
                    drawCircle(
                        color = Color.White.copy(alpha = rng.nextFloat() * 0.4f + 0.1f),
                        radius = r,
                        center = Offset(x, y)
                    )
                }
            }

                BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = animScale.value,
                        scaleY = animScale.value,
                        translationX = animOffset.value.x,
                        translationY = animOffset.value.y
                    )
            ) {
                val scale = animScale.value
                val offset = animOffset.value
                val density = LocalDensity.current
                val bwW = constraints.maxWidth.toFloat()
                val bwH = constraints.maxHeight.toFloat()
                val cx = bwW / 2f
                val cy = bwH / 2f
                val rootPos = Offset(cx, cy)
                val genreCount = genres.size.coerceAtLeast(1)
                val genreRadius = bwW * 0.28f
                val bookRadiusConfig = bwW * 0.16f
                val neonTeal = PaginexNeonTeal

                Canvas(modifier = Modifier.fillMaxSize()) {
                    genres.forEachIndexed { gi, genre ->
                        val angle = Math.toRadians((gi * 360.0 / genreCount) - 90.0)
                        val gx = cx + (genreRadius * cos(angle)).toFloat()
                        val gy = cy + (genreRadius * sin(angle)).toFloat()
                        val gPos = Offset(gx, gy)

                        drawLine(
                            color = Color(0xFFFFD700).copy(alpha = 0.3f),
                            start = rootPos,
                            end = gPos,
                            strokeWidth = 1.2f / scale
                        )

                        val isExpanded = expandedGenres.contains(genre)
                        val books = genreMap[genre] ?: emptyList()
                        
                        // Only render books if expanded — exactly 1 star per book
                        val renderBooks = if (isExpanded) books else emptyList()

                        val genreAngleBase = Math.toRadians((gi * 360.0 / genreCount) - 90.0)
                        renderBooks.forEachIndexed { bi, bookStatus ->
                            val spread = if (renderBooks.size == 1) 0.0 else (bi - (renderBooks.size - 1) / 2.0) * 0.7
                            val bookAngle = genreAngleBase + spread
                            val bookDist = bookRadiusConfig + (if (bi % 2 == 0) 10f else -10f)
                            val bx = gx + (bookDist * cos(bookAngle)).toFloat()
                            val by = gy + (bookDist * sin(bookAngle)).toFloat()
                            val bPos = Offset(bx, by)

                            drawLine(
                                color = neonTeal.copy(alpha = 0.25f),
                                start = gPos,
                                end = bPos,
                                strokeWidth = 0.8f / scale
                            )
                            val bookAlpha = twinkles[bi % twinkles.size]
                            drawCircle(
                                color = neonTeal.copy(alpha = bookAlpha * 0.9f),
                                radius = 7f / scale.coerceAtLeast(1f),
                                center = bPos
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(neonTeal.copy(alpha = bookAlpha * 0.5f), Color.Transparent),
                                    center = bPos, radius = 20f / scale.coerceAtLeast(1f)
                                ),
                                radius = 20f / scale.coerceAtLeast(1f),
                                center = bPos
                            )
                        }

                        val gAlpha = twinkles[gi % twinkles.size]
                        drawCircle(color = Color(0xFFFFD700).copy(alpha = gAlpha), radius = 10f / scale.coerceAtLeast(1f), center = gPos)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700).copy(alpha = gAlpha * 0.4f), Color.Transparent),
                                center = gPos, radius = 28f / scale.coerceAtLeast(1f)
                            ),
                            radius = 28f / scale.coerceAtLeast(1f),
                            center = gPos
                        )
                    }

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color.Transparent),
                            center = rootPos, radius = 55f / scale.coerceAtLeast(1f)
                        ),
                        radius = 55f / scale.coerceAtLeast(1f),
                        center = rootPos
                    )
                    drawCircle(color = Color(0xFFFFD700), radius = 18f / scale.coerceAtLeast(1f), center = rootPos)
                }

                Box(modifier = Modifier.offset(x = with(density) { (cx - 40f).toDp() }, y = with(density) { (cy + 22f).toDp() })) {
                    Text(targetUser.fullName.split(" ")[0], color = Color(0xFFFFD700), fontSize = (11 / scale.coerceAtLeast(1f)).sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                }

                genres.forEachIndexed { gi, genre ->
                    val angle = Math.toRadians((gi * 360.0 / genreCount) - 90.0)
                    val gx = cx + (genreRadius * cos(angle)).toFloat()
                    val gy = cy + (genreRadius * sin(angle)).toFloat()
                    val isExpanded = expandedGenres.contains(genre)
                    
                    Box(
                        modifier = Modifier
                            .offset(x = with(density) { (gx - 40f).toDp() }, y = with(density) { (gy - 20f).toDp() })
                            .size(80.dp)
                            .clickable {
                                scope.launch {
                                    if (isExpanded) {
                                        expandedGenres = expandedGenres - genre
                                        // Zoom out to center
                                        launch { animScale.animateTo(1f, tween(600)) }
                                        launch { animOffset.animateTo(Offset.Zero, tween(600)) }
                                    } else {
                                        expandedGenres = expandedGenres + genre
                                        // Focus and Growth effect
                                        val focusScale = 2.5f
                                        val targetX = (cx - gx) * focusScale
                                        val targetY = (cy - gy) * focusScale
                                        launch { animScale.animateTo(focusScale, tween(700, easing = FastOutSlowInEasing)) }
                                        launch { animOffset.animateTo(Offset(targetX, targetY), tween(700, easing = FastOutSlowInEasing)) }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            genre, 
                            color = if (isExpanded) PaginexNeonTeal else Color(0xFFFFD700).copy(alpha = 0.9f), 
                            fontSize = (10 / scale.coerceAtLeast(1f)).sp, 
                            fontWeight = FontWeight.Bold, 
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isExpanded) {
                        val books = genreMap[genre] ?: emptyList()
                        val genreAngleBase = Math.toRadians((gi * 360.0 / genreCount) - 90.0)
                        books.forEachIndexed { bi, bookStatus ->
                            val spread = if (books.size == 1) 0.0 else (bi - (books.size - 1) / 2.0) * 0.7
                            val bookAngle = genreAngleBase + spread
                            val bookDist = bookRadiusConfig + (if (bi % 2 == 0) 10f else -10f)
                            val bx = gx + (bookDist * cos(bookAngle)).toFloat()
                            val by = gy + (bookDist * sin(bookAngle)).toFloat()
                            Box(
                                modifier = Modifier
                                    .offset(x = with(density) { (bx - 36f).toDp() }, y = with(density) { (by + 11f).toDp() })
                                    .clickable { onBookClick(bookStatus.id) }
                            ) {
                                Text(
                                    bookStatus.book.title,
                                    color = PaginexNeonTeal.copy(alpha = 0.85f),
                                    fontSize = (8 / scale.coerceAtLeast(1f)).sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(72.dp)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PaginexGlass)
                    .border(1.dp, PaginexGlassBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text("Map", color = PaginexWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFD700), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Profile / Genre", color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(PaginexNeonTeal, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Book", color = Color.Gray, fontSize = 10.sp)
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 12.dp)
            ) {
                IconButton(
                    onClick = { scope.launch { animScale.animateTo((animScale.value + 0.5f).coerceAtMost(4f)) } },
                    modifier = Modifier.background(PaginexGlass, CircleShape).border(1.dp, PaginexGlassBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Add, "Zoom In", tint = PaginexNeonTeal)
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = { 
                        scope.launch {
                            val nextScale = (animScale.value - 0.5f).coerceAtLeast(0.5f)
                            if (nextScale <= 1.0f) expandedGenres = emptySet()
                            animScale.animateTo(nextScale)
                            if (nextScale <= 1.0f) animOffset.animateTo(Offset.Zero)
                        }
                    },
                    modifier = Modifier.background(PaginexGlass, CircleShape).border(1.dp, PaginexGlassBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Remove, "Zoom Out", tint = PaginexNeonTeal)
                }
            }
        }
    }
}

@Composable
fun DynamicNebula() {
    val infiniteTransition = rememberInfiniteTransition()
    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val t2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val neonPurple = PaginexNeonPurple
    val neonTeal = PaginexNeonTeal
    val neonPink = PaginexNeonPink
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        repeat(3) { i ->
            val angle = if (i == 0) t1 else if (i == 1) t2 else (t1 + t2) / 2
            val color = when(i) {
                0 -> neonPurple.copy(alpha = 0.15f)
                1 -> neonTeal.copy(alpha = 0.12f)
                else -> neonPink.copy(alpha = 0.1f)
            }
            val radius = size.minDimension * 0.8f * (1f + 0.1f * sin(angle))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = center + Offset(100f * cos(angle * (i + 1)), 150f * sin(angle * (i + 1))),
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
fun OrbitalStatusSelector(
    statuses: List<String>,
    selectedStatus: String?,
    onStatusSelected: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing))
    )

    val pWhite = PaginexWhite
    Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
        // Orbital Paths
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = pWhite.copy(alpha = 0.05f), radius = 130.dp.toPx(), style = Stroke(1.dp.toPx()))
        }

        statuses.forEachIndexed { index, s ->
            val angle = Math.toRadians((orbitAngle + (index * 360f / statuses.size)).toDouble())
            val x = (130.dp.value * cos(angle)).dp
            val y = (130.dp.value * sin(angle)).dp
            val isSelected = selectedStatus == s
            
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(if (isSelected) 80.dp else 70.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PaginexNeonPurple.copy(alpha = 0.3f) else PaginexGlass)
                    .border(1.dp, if (isSelected) PaginexNeonPurple else PaginexGlassBorder, CircleShape)
                    .clickable { onStatusSelected(s) }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = s,
                    color = if (isSelected) Color.White else PaginexWhite.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CelestialCategoryPortal(
    label: String,
    description: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer(scaleX = if (isSelected) 1.02f else 1f, scaleY = if (isSelected) 1.02f else 1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) color.copy(alpha = 0.8f) else PaginexGlassBorder
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(label) {
                        "Reading" -> Icons.Default.MenuBook
                        "Read" -> Icons.Default.CheckCircle
                        "To Read" -> Icons.Default.Star
                        "On Hold" -> Icons.Default.Info
                        "Dropped" -> Icons.Default.Close
                        else -> Icons.Default.Book
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp).graphicsLayer(scaleX = if (isSelected) glowScale else 1f, scaleY = if (isSelected) glowScale else 1f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(description, color = color.copy(alpha = 0.7f), fontSize = 13.sp)
            }
            
            Icon(
                Icons.Default.KeyboardArrowRight,
                null, 
                tint = if (isSelected) color else color.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(initialPostId: String? = null, onPost: () -> Unit, onDraftsClick: () -> Unit) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val initialPost = remember(initialPostId) {
        MockData.feedPosts.find { it.id == initialPostId } ?: MockData.drafts.find { it.id == initialPostId }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<Book?>(initialPost?.book) }
    var status by remember { mutableStateOf<String?>(initialPost?.status) }
    var reviewText by remember { mutableStateOf(initialPost?.review ?: "") }
    var showResults by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(initialPost?.rating ?: 5f) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var showLibrarySelector by remember { mutableStateOf(false) }
    
    // Undo States
    var undoTimer by remember { mutableStateOf<Int?>(null) }
    var pendingPost by remember { mutableStateOf<Post?>(null) }
    var isDraftAction by remember { mutableStateOf(false) }

    // Countdown for undo overlay, then navigate
    LaunchedEffect(undoTimer) {
        if (undoTimer != null && undoTimer!! > 0) {
            delay(1000L)
            undoTimer = undoTimer!! - 1
        } else if (undoTimer == 0) {
            onPost()
            undoTimer = null
            pendingPost = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PaginexSpace)) {
        DynamicNebula()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Avant-Garde",
                    fontSize = 12.sp,
                    color = PaginexNeonTeal,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    modifier = Modifier.graphicsLayer(alpha = 0.6f)
                )

                // Drafts Button
                OutlinedButton(
                    onClick = onDraftsClick,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, PaginexNeonTeal.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Create, null, tint = PaginexNeonTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Drafts", color = PaginexNeonTeal, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "NEW REVIEW",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PaginexWhite,
                letterSpacing = (-1).sp
            )

            if (true) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // 2. Step: Search & Book Orbit
                Text(
                    "SELECT THE WORK",
                    fontSize = 10.sp,
                    color = PaginexWhite.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { showLibrarySelector = true },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.8f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, PaginexNeonPurple.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Add, null, tint = PaginexNeonPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Book from Library", color = PaginexNeonPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                if (selectedBook != null) {
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Selected Book "Box"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
                        border = BorderStroke(1.dp, PaginexNeonTeal.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = selectedBook!!.coverUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp, 90.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Column {
                                Text(
                                    text = selectedBook!!.title,
                                    color = PaginexWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = selectedBook!!.author,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "SELECTED",
                                    color = PaginexNeonTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { selectedBook = null }) {
                                Icon(Icons.Default.Close, null, tint = PaginexNeonPink)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // 3. Step: Expressive Input Pane
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(rotationZ = 1f),
                        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
                        shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp, topEnd = 40.dp, bottomStart = 40.dp),
                        border = BorderStroke(1.dp, PaginexGlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("MENTAL JOURNEY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaginexNeonTeal, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            BasicTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = PaginexWhite, fontSize = 16.sp),
                                decorationBox = { innerTextField ->
                                    if (reviewText.isEmpty()) Text("Let your thoughts flow like stars...", color = Color.Gray)
                                    innerTextField()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Rating Slider
                    if (status != "To Read") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("YOUR RATING: ${rating.toInt()}/10", color = PaginexNeonTeal, fontWeight = FontWeight.Bold)
                            Slider(
                                value = rating,
                                onValueChange = { rating = Math.round(it).toFloat() },
                                valueRange = 1f..10f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = PaginexNeonPurple,
                                    activeTrackColor = PaginexNeonPurple,
                                    inactiveTrackColor = PaginexGlassBorder
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final Action Button
                    val isReady = reviewText.isNotEmpty() && selectedBook != null && status != null
                    val btnColor = when (status) {
                        "Read" -> PaginexNeonTeal
                        "Reading" -> PaginexNeonPurple
                        "Dropped" -> PaginexNeonPink
                        else -> PaginexNeonTeal
                    }
                    Surface(
                        onClick = { if (isReady) showPublishDialog = true },
                        enabled = isReady,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 32.dp)
                            .size(200.dp, 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = if (isReady) btnColor else PaginexGlass,
                        border = BorderStroke(2.dp, if (isReady) Color.White else PaginexGlassBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "PUBLISH", 
                                color = if (isReady) Color.Black else Color.Gray,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showLibrarySelector) {
            LibrarySelectorSheet(
                targetUserId = MockData.currentUser.id.ifEmpty { "u1" },
                onDismiss = { showLibrarySelector = false },
                onBookSelected = { rs -> 
                    selectedBook = rs.book
                    status = rs.status
                    showLibrarySelector = false
                }
            )
        }

        if (showPublishDialog) {
            AlertDialog(
                onDismissRequest = { showPublishDialog = false },
                containerColor = PaginexGalaxy,
                title = { Text("Save your journey?", color = PaginexWhite) },
                text = { Text("Do you want to draft or publish?", color = Color.Gray) },
                confirmButton = {
                    Button(
                        onClick = { 
                            showPublishDialog = false
                            val uid = MockData.currentUser.id.ifEmpty { "u1" }
                            val newPost = Post(
                                id = "post_${System.currentTimeMillis()}",
                                userId = uid,
                                book = selectedBook!!,
                                status = status ?: "To Read",
                                rating = rating,
                                review = reviewText
                            )
                            pendingPost = newPost
                            isDraftAction = false
                            // Add to local cache immediately
                            MockData.feedPosts.add(0, newPost)
                            // Handle old post cleanup + Firestore write in GlobalScope (survives navigation)
                            kotlinx.coroutines.MainScope().launch {
                                if (initialPostId != null) {
                                    val ei = MockData.feedPosts.indexOfFirst { it.id == initialPostId }
                                    if (ei != -1) MockData.feedPosts.removeAt(ei)
                                    val edi = MockData.drafts.indexOfFirst { it.id == initialPostId }
                                    if (edi != -1) MockData.drafts.removeAt(edi)
                                    FirestoreService.deletePost(initialPostId)
                                }
                                FirestoreService.createPost(newPost)
                            }
                            undoTimer = 5 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
                    ) { Text("Publish", color = Color.Black) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { 
                            showPublishDialog = false
                            val uid = MockData.currentUser.id.ifEmpty { "u1" }
                            pendingPost = Post(
                                id = "draft_${System.currentTimeMillis()}",
                                userId = uid,
                                book = selectedBook!!,
                                status = status ?: "To Read",
                                rating = rating,
                                review = reviewText
                            )
                            isDraftAction = true
                            undoTimer = 5
                        },
                        border = BorderStroke(1.dp, PaginexNeonPurple)
                    ) { Text("Save as Draft", color = PaginexNeonPurple) }
                }
            )
        }

        // Undo Overlay
        AnimatedVisibility(
            visible = undoTimer != null && undoTimer!! > 0,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        ) {
            Surface(
                color = Color(0xFF1E1E2E), // Dark Glassy
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, PaginexNeonTeal.copy(alpha = 0.5f)),
                shadowElevation = 12.dp,
                modifier = Modifier.padding(horizontal = 24.dp).height(56.dp).fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(PaginexNeonTeal.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${undoTimer}", color = PaginexNeonTeal, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(if (isDraftAction) "Saving to drafts..." else "Publishing...", color = PaginexWhite, fontSize = 13.sp)
                    }
                    
                    TextButton(
                        onClick = { 
                            // Rollback: delete the post we just saved to Firestore
                            val postToUndo = pendingPost
                            if (postToUndo != null && !isDraftAction) {
                                val idx = MockData.feedPosts.indexOfFirst { it.id == postToUndo.id }
                                if (idx != -1) MockData.feedPosts.removeAt(idx)
                                kotlinx.coroutines.MainScope().launch { FirestoreService.deletePost(postToUndo.id) }
                            } else if (postToUndo != null && isDraftAction) {
                                val idx = MockData.drafts.indexOfFirst { it.id == postToUndo.id }
                                if (idx != -1) MockData.drafts.removeAt(idx)
                            }
                            undoTimer = null
                            pendingPost = null
                        }
                    ) {
                        Text("UNDO", color = PaginexNeonPink, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CosmicSectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PaginexGlassBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = PaginexNeonTeal, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = PaginexWhite.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun CosmicStatusChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "glowIntensity"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PaginexNeonPurple.copy(alpha = 0.2f) else PaginexGlass,
        border = BorderStroke(
            1.dp, 
            if (isSelected) PaginexNeonPurple.copy(alpha = glowIntensity) else PaginexGlassBorder
        )
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                label, 
                color = if (isSelected) PaginexNeonPurple else PaginexWhite.copy(alpha = 0.8f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ProfileMindMap(onClick: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val neonPurple = PaginexNeonPurple
    val neonTeal = PaginexNeonTeal
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Thin Neon Threads
            val points = listOf(
                Offset(100f, 80f),
                Offset(size.width - 100f, 80f),
                Offset(80f, size.height - 80f),
                Offset(size.width - 80f, size.height - 80f),
                Offset(size.width / 2, 40f)
            )
            
            points.forEach { point ->
                drawLine(
                    color = neonTeal.copy(alpha = 0.4f),
                    start = center,
                    end = point,
                    strokeWidth = 1.5f
                )
                drawCircle(
                    color = neonTeal.copy(alpha = 0.6f),
                    radius = 15f,
                    center = point
                )
            }

            // Central Glowing Sun
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(neonPurple.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = 80f * glowScale
                ),
                radius = 80f * glowScale,
                center = center
            )
            drawCircle(
                color = neonPurple,
                radius = 35f,
                center = center
            )
        }
        
        Text(
            text = "Mental Journey",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = PaginexSpace,
        topBar = {
            TopAppBar(
                title = { Text("Drafts", color = PaginexWhite) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        val drafts = MockData.drafts
        if (drafts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("You don't have any saved drafts yet.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
                items(drafts) { draft ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PaginexGlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = draft.book.coverUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp, 75.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(draft.book.title, color = PaginexWhite, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(draft.book.author, color = Color.Gray, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${draft.rating.toInt()}/10", fontSize = 12.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Status: ${draft.status}", color = PaginexNeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Review: ${draft.review}",
                                color = PaginexWhite.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(
                                    onClick = { MockData.drafts.remove(draft) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                                ) {
                                    Text("Delete", color = Color.Red)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        MockData.feedPosts.add(0, draft)
                                        MockData.drafts.remove(draft)
                                        onBack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
                                ) {
                                    Text("Yayınla", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val user = MockData.currentUser
    val isDarkTheme = LocalIsDarkTheme.current
    val themeToggle = LocalThemeToggle.current

    Scaffold(
        containerColor = PaginexSpace,
        topBar = {
            TopAppBar(
                title = { Text("Profile settings", color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = user.avatarUrl.ifEmpty { "https://via.placeholder.com/200" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, PaginexNeonPurple, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(user.fullName, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("@${user.username}", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account settings list
            Text(
                "Account settings",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToEditProfile() }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Person, null, tint = PaginexNeonTeal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Personal information", color = PaginexWhite, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
            }
            Divider(color = PaginexGlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))

            Spacer(modifier = Modifier.height(32.dp))

            // Customisation list
            Text(
                "Customisation",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, null, tint = PaginexNeonPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Theme", color = PaginexWhite, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { themeToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PaginexWhite,
                        checkedTrackColor = PaginexNeonPurple,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = PaginexGlass
                    )
                )
            }
            Divider(color = PaginexGlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Log out", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String, icon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = PaginexWhite, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(value, fontWeight = FontWeight.ExtraBold, color = PaginexWhite, fontSize = 20.sp)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onBookClick: (String) -> Unit,
    onListsClick: () -> Unit,
    onConstellationClick: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showFollowers by remember { mutableStateOf(false) }
    var showFollowing by remember { mutableStateOf(false) }
    var showLibrary by remember { mutableStateOf(false) }
    
    val userFeed = remember { mutableStateListOf<Post>() }
    
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(userId) {
        user = FirestoreService.getUserProfile(userId)
        val posts = FirestoreService.getFeed()
        userFeed.clear()
        userFeed.addAll(posts.filter { it.userId == userId })
        
        // Check if current user is following this user
        val followsReq = FirestoreService.getFollowing("u1")
        isFollowing = followsReq.any { it.id == userId }
    }

    Scaffold(
        containerColor = PaginexSpace,
        topBar = {
            TopAppBar(
                title = { Text(user?.username ?: "", color = PaginexWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PaginexNeonPurple) }
            return@Scaffold
        }
        
        val u = user!!

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(PaginexSpace),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserGalaxy(user = u, rotation = rotation, onBookClick = onBookClick)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(u.fullName, color = PaginexWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("@${u.username}", color = Color.Gray, fontSize = 14.sp)

                    if (u.bio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            u.bio,
                            color = PaginexWhite.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))


                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem("Followers", u.followersCount.toString(), null, onClick = { showFollowers = true })
                        ProfileStatItem("Following", u.followingCount.toString(), null, onClick = { showFollowing = true })
                        ProfileStatItem("Books", MockData.readingStatuses.count { it.userId == u.id }.toString(), null, onClick = { showLibrary = true })
                    }

                    if (showFollowers) {
                        UserListSheet("Followers", targetUserId = userId, onDismiss = { showFollowers = false })
                    }
                    if (showFollowing) {
                        UserListSheet("Following", targetUserId = userId, onDismiss = { showFollowing = false })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Book Lists Button
                        OutlinedButton(
                            onClick = onListsClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, PaginexNeonPurple.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.List, null, tint = PaginexNeonPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lists", color = PaginexNeonPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        // Constellation Button
                        OutlinedButton(
                            onClick = onConstellationClick,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, PaginexNeonPurple.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = PaginexNeonPurple, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Map", color = PaginexNeonPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (isFollowing) {
                                    isFollowing = false
                                    MockData.currentUser = MockData.currentUser.copy(followingCount = (MockData.currentUser.followingCount - 1).coerceAtLeast(0))
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.unfollowUser("u1", userId)
                                    }
                                } else {
                                    isFollowing = true
                                    MockData.currentUser = MockData.currentUser.copy(followingCount = MockData.currentUser.followingCount + 1)
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.followUser("u1", userId)
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) PaginexGlass else PaginexNeonPurple),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
                    ) {
                        Text(
                            if (isFollowing) "Unfollow" else "Follow",
                            color = if (isFollowing) PaginexWhite else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            items(userFeed, key = { it.id }) { post ->
                BookPostCard(
                    post = post,
                    onUserClick = { /* Already here */ },
                    onBookClick = { onBookClick(post.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showFollowing) {
            UserListSheet("Following", targetUserId = userId, onDismiss = { showFollowing = false })
        }
        if (showLibrary) {
            UserLibrarySheet(targetUserId = userId, onDismiss = { showLibrary = false })
        }
    }
}
