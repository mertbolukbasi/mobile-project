package com.example.paginex

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.lazy.rememberLazyListState
import java.util.Locale
import com.google.firebase.auth.FirebaseAuthException

// --- NAVIGATION ROUTES ---
sealed class Screen(val route: String, val icon: ImageVector? = null, val label: String? = null) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object VerifyEmail : Screen("verify_email")
    object ForgotPassword : Screen("forgot_password")
    object ProfileSetup : Screen("profile_setup")
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
    val context = LocalContext.current
    val activity = context as? Activity
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Explore.route, Screen.CreatePost.route, 
        Screen.Saved.route, Screen.Profile.route
    )
    val swipeableTabRoutes = listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Saved.route,
        Screen.Profile.route
    )
    var selectedMainTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    val tabBackHistory = remember { mutableStateListOf<Int>() }

    val isAtMainRoot = currentRoute == Screen.Home.route && navController.previousBackStackEntry == null
    androidx.activity.compose.BackHandler(enabled = isAtMainRoot || showExitConfirmDialog) {
        if (showExitConfirmDialog) {
            showExitConfirmDialog = false
        } else if (isAtMainRoot && tabBackHistory.isNotEmpty()) {
            selectedMainTabIndex = tabBackHistory.removeAt(tabBackHistory.lastIndex)
        } else {
            showExitConfirmDialog = true
        }
    }

    fun setSelectedMainTab(newIndex: Int) {
        if (newIndex == selectedMainTabIndex) return
        tabBackHistory.remove(newIndex)
        tabBackHistory.add(selectedMainTabIndex)
        selectedMainTabIndex = newIndex
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = PaginexSpace,
        bottomBar = {
            if (showBottomBar) {
                val onCreatePostScreen = currentRoute == Screen.CreatePost.route
                val items = listOf(Screen.Home, Screen.Explore, Screen.CreatePost, Screen.Saved, Screen.Profile)
                
                val bColor = PaginexSpace.copy(alpha = 0.85f)
                val neonPurple = PaginexNeonPurple
                val barShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                // Single-layer bar: avoid Surface + drawRect + drawLine stacking (read as double border).
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(bColor, barShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(neonPurple)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .padding(top = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { screen ->
                            val tabIndex = swipeableTabRoutes.indexOf(screen.route)
                            val isSelected = when {
                                onCreatePostScreen ->
                                    screen.route == Screen.CreatePost.route
                                tabIndex >= 0 ->
                                    tabIndex == selectedMainTabIndex
                                else ->
                                    currentRoute == screen.route
                            }
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
                                        when {
                                            screen.route == Screen.CreatePost.route -> navigateToTab(screen.route)
                                            tabIndex >= 0 -> {
                                                // Create Post is a separate NavHost destination; pager tab state alone
                                                // does not pop it. Mirror Explore/Saved/Profile: go Home, then switch tab.
                                                if (onCreatePostScreen) {
                                                    navController.navigate(Screen.Home.route) {
                                                        launchSingleTop = true
                                                        popUpTo(navController.graph.startDestinationId)
                                                    }
                                                }
                                                setSelectedMainTab(tabIndex)
                                            }
                                            else -> navigateToTab(screen.route)
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
        NavHost(
            navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) { SplashScreen { 
                if (AuthService.isUserLoggedIn() && AuthService.isEmailVerified()) {
                    if (AppCache.currentUser.fullName == "New User" || AppCache.currentUser.username == "newuser") {
                        navController.navigate(Screen.ProfileSetup.route) { popUpTo(0) }
                    } else {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                } else if (AuthService.isUserLoggedIn() && !AuthService.isEmailVerified()) {
                    navController.navigate(Screen.VerifyEmail.route) { popUpTo(0) }
                } else {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            } }
            composable(Screen.Login.route) { 
                LoginScreen(
                    onLogin = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onEmailNotVerified = { navController.navigate(Screen.VerifyEmail.route) { popUpTo(0) } },
                    onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                ) 
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegistered = { navController.navigate(Screen.VerifyEmail.route) { popUpTo(0) } },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.VerifyEmail.route) {
                VerifyEmailScreen(
                    onVerified = {
                        if (AppCache.currentUser.fullName == "New User" || AppCache.currentUser.username == "newuser") {
                            navController.navigate(Screen.ProfileSetup.route) { popUpTo(0) }
                        } else {
                            navController.navigate(Screen.Home.route) { popUpTo(0) }
                        }
                    },
                    onBackToLogin = {
                        AuthService.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    }
                )
            }
            composable(Screen.ProfileSetup.route) {
                ProfileSetupScreen(
                    onSetupComplete = {
                        navController.navigate(Screen.Home.route) { popUpTo(0) }
                    }
                )
            }
            composable(Screen.Home.route) {
                MainTabsPager(
                    selectedTabIndex = selectedMainTabIndex,
                    onTabSettled = { settledIndex -> setSelectedMainTab(settledIndex) },
                    navController = navController
                )
            }
            composable(Screen.Explore.route) {
                LaunchedEffect(Unit) {
                    selectedMainTabIndex = 1
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            }
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
            composable(Screen.Saved.route) {
                LaunchedEffect(Unit) {
                    selectedMainTabIndex = 2
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            }
            composable(Screen.Profile.route) {
                LaunchedEffect(Unit) {
                    selectedMainTabIndex = 3
                    navController.navigate(Screen.Home.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            }
            composable(Screen.EditProfile.route) { EditProfileScreen { navController.popBackStack() } }
            composable(
                route = "book_lists/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry -> 
                val userId = backStackEntry.arguments?.getString("userId") ?: AuthService.getUid()
                BookListsScreen(targetUserId = userId) { navController.popBackStack() } 
            }
            composable(Screen.Drafts.route) {
                DraftsScreen(
                    onBack = { navController.popBackStack() },
                    onEditDraft = { draftId -> navController.navigate("create-post?postId=$draftId") }
                )
            }
            composable(
                route = "post_list/{postId}?mode={mode}",
                arguments = listOf(
                    navArgument("postId") { type = NavType.StringType },
                    navArgument("mode") { type = NavType.StringType; defaultValue = "single" }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId") ?: ""
                val mode = backStackEntry.arguments?.getString("mode") ?: "single"
                PostListScreen(
                    postId = postId,
                    mode = mode,
                    onBack = { navController.popBackStack() },
                    onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                    onNavigateToUser = { id -> navController.navigate(Screen.PublicProfile.route.replace("{userId}", id)) },
                    onNavigateToEdit = { id -> navController.navigate(Screen.CreatePost.route + "?postId=$id") }
                )
            }
            composable(
                route = "constellation/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry -> 
                val userId = backStackEntry.arguments?.getString("userId") ?: AuthService.getUid()
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
                BookDetailScreen(
                    postId = postId,
                    ownerOnly = ownerOnly,
                    onBack = { navController.popBackStack() },
                    onReviewClick = { reviewPostId -> navController.navigate("post_list/$reviewPostId?mode=single") }
                ) 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = {
                        AuthService.logout()
                        navController.navigate(Screen.Login.route) { popUpTo(0) }
                    },
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
                    onGalaxyBookClick = { bookId -> navController.navigate("detail/$bookId") },
                    onPostClick = { postId -> navController.navigate("post_list/$postId?mode=single") },
                    onListsClick = { navController.navigate("book_lists/${userId}") },
                    onConstellationClick = { navController.navigate("constellation/${userId}") }
                )
            }
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Exit app?", color = PaginexWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to exit the app?", color = PaginexWhite.copy(alpha = 0.8f)) },
            containerColor = PaginexGalaxy,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        activity?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("No", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

}

@Composable
fun MainTabsPager(
    selectedTabIndex: Int,
    onTabSettled: (Int) -> Unit,
    navController: NavHostController
) {
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { 4 }
    )

    LaunchedEffect(selectedTabIndex) {
        if (!pagerState.isScrollInProgress && pagerState.currentPage != selectedTabIndex) {
            pagerState.scrollToPage(selectedTabIndex)
        }
    }

    val onTabSettledState by rememberUpdatedState(onTabSettled)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page ->
                onTabSettledState(page)
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> PaginexHomeScreen(
                onNavigateToDetail = { postId -> navController.navigate("detail/$postId") },
                onNavigateToEdit = { postId -> navController.navigate("create-post?postId=$postId") },
                onNavigateToUser = { userId ->
                    if (userId == AuthService.getUid()) onTabSettled(3)
                    else navController.navigate(Screen.PublicProfile.createRoute(userId))
                }
            )
            1 -> ExploreScreen(
                onBookClick = { postId -> navController.navigate("detail/$postId") }
            )
            2 -> SavedPostsScreen(
                onBookClick = { postId -> navController.navigate("post_list/$postId?mode=saved_book") }
            )
            3 -> PaginexProfileScreen(
                onEditClick = { navController.navigate(Screen.Settings.route) },
                onListsClick = { navController.navigate("book_lists/${AuthService.getUid()}") },
                onConstellationClick = { navController.navigate("constellation/${AuthService.getUid()}") },
                onGalaxyBookClick = { bookId -> navController.navigate("detail/$bookId") },
                onMyPostClick = { postId -> navController.navigate("post_list/$postId?mode=single") },
                onLogoutClick = {
                    AuthService.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allBooks = remember(AppCache.readingStatuses.size) {
        val currentUid = AuthService.getUid()
        AppCache.readingStatuses.filter { it.userId == currentUid }.map { it.book }.distinctBy { it.id }
    }
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = PaginexSpace,
        dragHandle = { BottomSheetDefaults.DragHandle(color = PaginexWhite.copy(alpha = 0.7f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.92f)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Edit Favorite Books", color = PaginexWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Select up to 5 books to show in your galaxy (${selected.size}/5)", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search books...", color = PaginexWhite.copy(alpha = 0.7f)) },
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

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = BorderStroke(1.dp, PaginexGlassBorder)
                ) {
                    Text("Cancel", color = PaginexWhite, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onSave(selected.toList()) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
                ) {
                    Text("Save", color = PaginexWhite, fontWeight = FontWeight.Bold)
                }
            }
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
                model = avatarModel(user.avatarUrl),
                contentDescription = user.fullName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Orbiting Books (Favorites Only)
        val favoriteBooks = remember(user.favoriteBooks, AppCache.readingStatuses.size) {
            val favIds = user.favoriteBooks.toSet()
            val fromLibrary = AppCache.readingStatuses.filter { it.userId == user.id && favIds.contains(it.book.id) }.map { it.book }
            val fromBooksCatalog = AppCache.books.filter { favIds.contains(it.id) && fromLibrary.none { lb -> lb.id == it.id } }
            (fromLibrary + fromBooksCatalog).take(5)
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
                modifier = Modifier.offset(x = xOffset, y = yOffset),
                contentAlignment = Alignment.Center
            ) {
                CosmicPlanet(
                    book = book,
                    size = 44.dp,
                    glowColor = PaginexNeonTeal,
                    isShattered = false,
                    onClick = { 
                        onBookClick(book.id)
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
fun LoginScreen(
    onLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onEmailNotVerified: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(PaginexSpace).padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Welcome to Paginex", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)

        Spacer(modifier = Modifier.height(32.dp))
        
        if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = email, 
            onValueChange = { email = it }, 
            label = { Text("Email", color = PaginexWhite.copy(alpha = 0.7f)) }, 
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
            value = password, 
            onValueChange = { password = it }, 
            label = { Text("Password", color = PaginexWhite.copy(alpha = 0.7f)) }, 
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PaginexNeonPurple, 
                unfocusedBorderColor = PaginexGlassBorder,
                focusedTextColor = PaginexWhite,
                unfocusedTextColor = PaginexWhite
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                "Forgot password?",
                color = PaginexNeonPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onForgotPassword() }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = AuthService.signIn(email, password)
                        isLoading = false
                        if (result.isSuccess) {
                            FirestoreService.refreshSessionCacheFromFirestore()
                            onLogin()
                        } else {
                            val error = result.exceptionOrNull()?.message ?: "Login failed"
                            if (error == "EMAIL_NOT_VERIFIED") {
                                onEmailNotVerified()
                            } else {
                                errorMessage = error
                            }
                        }
                    }
                } else {
                    errorMessage = "Please enter email and password"
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = PaginexWhite, modifier = Modifier.size(24.dp))
            else Text("LOGIN", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Don't have an account? ", color = PaginexWhite.copy(alpha = 0.7f))
            Text("Sign Up", color = PaginexNeonPurple, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
        }
    }
}

@Composable
fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaginexSpace)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBackToLogin, modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PaginexWhite)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Reset password", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Enter your email and we'll send you a link to reset your password.",
                color = PaginexWhite.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (successMessage != null) {
                Text(successMessage!!, color = PaginexNeonTeal, modifier = Modifier.padding(bottom = 8.dp))
            }
            if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    successMessage = null
                    errorMessage = null
                },
                label = { Text("Email", color = PaginexWhite.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PaginexNeonPurple,
                    unfocusedBorderColor = PaginexGlassBorder,
                    focusedTextColor = PaginexWhite,
                    unfocusedTextColor = PaginexWhite
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Please enter your email"
                        successMessage = null
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    successMessage = null
                    scope.launch {
                        val result = AuthService.sendPasswordResetEmail(email)
                        isLoading = false
                        when {
                            result.isSuccess -> {
                                successMessage =
                                    "If an account exists for this email, we've sent password reset instructions."
                                errorMessage = null
                            }
                            else -> {
                                val ex = result.exceptionOrNull()
                                val obscureMissingUser =
                                    ex is FirebaseAuthException &&
                                        ex.errorCode == "ERROR_USER_NOT_FOUND"
                                if (obscureMissingUser) {
                                    successMessage =
                                        "If an account exists for this email, we've sent password reset instructions."
                                    errorMessage = null
                                } else {
                                    errorMessage =
                                        ex?.localizedMessage?.takeIf { it.isNotBlank() }
                                            ?: "Something went wrong. Please try again."
                                    successMessage = null
                                }
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PaginexWhite, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send reset link", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(PaginexSpace).padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Join Paginex", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)
        Text("Create your account to get started.", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = PaginexWhite.copy(alpha = 0.7f)) },
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
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = PaginexWhite.copy(alpha = 0.7f)) },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password", color = PaginexWhite.copy(alpha = 0.7f)) },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
            onClick = {
                when {
                    email.isBlank() || password.isBlank() -> errorMessage = "Please fill in all fields"
                    password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                    password != confirmPassword -> errorMessage = "Passwords do not match"
                    else -> {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = AuthService.signUp(email, password)
                            isLoading = false
                            if (result.isSuccess) {
                                onRegistered()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Something went wrong"
                            }
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) CircularProgressIndicator(color = PaginexWhite, modifier = Modifier.size(24.dp))
            else Text("SIGN UP", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Back to Login", color = PaginexWhite.copy(alpha = 0.7f))
        }
    }
}
@Composable
fun ProfileSetupScreen(onSetupComplete: () -> Unit) {
    val user = AppCache.currentUser
    val userEmail = AuthService.getUserEmail()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(userEmail.substringBefore("@")) }
    var avatarUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        avatarUri = uri
    }

    Box(modifier = Modifier.fillMaxSize().background(PaginexSpace)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Complete Your Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tell us a bit about yourself", color = PaginexWhite.copy(alpha = 0.7f))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Avatar Picker
            Column(
                modifier = Modifier.fillMaxWidth().clickable { photoPickerLauncher.launch("image/*") },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = avatarUri ?: "https://via.placeholder.com/200",
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, PaginexNeonPurple, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select profile photo", color = PaginexNeonTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Form Fields
            CosmicInputField(label = "Email address", value = userEmail, onValueChange = { }, enabled = false)
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "Username", value = username, onValueChange = { username = it })
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "First Name", value = name, onValueChange = { name = it })
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "Last Name", value = surname, onValueChange = { surname = it })
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    if (name.isBlank() || username.isBlank() || isLoading) return@Button
                    isLoading = true
                    val actualUid = AuthService.getUid()
                    scope.launch {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                            var newAvatarUrl = ""
                            if (avatarUri != null) {
                                val uploadedUrl = FirestoreService.uploadProfileImage(actualUid, avatarUri!!, context)
                                if (uploadedUrl != null) newAvatarUrl = uploadedUrl
                            }
                            
                            val updates = mutableMapOf<String, Any>(
                                "name" to name,
                                "surname" to surname,
                                "username" to username
                            )
                            if (newAvatarUrl.isNotEmpty()) {
                                updates["avatarUrl"] = newAvatarUrl
                            }
                            val success = FirestoreService.updateUserProfile(actualUid, updates)
                            
                            if (success) {
                                FirestoreService.refreshSessionCacheFromFirestore()
                                onSetupComplete()
                            }
                            isLoading = false
                        }
                    }
                }, 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
                shape = RoundedCornerShape(28.dp),
                enabled = name.isNotBlank() && username.isNotBlank() && !isLoading
            ) { 
                if (isLoading) {
                    CircularProgressIndicator(color = PaginexWhite, modifier = Modifier.size(24.dp))
                } else {
                    Text("COMPLETE SETUP", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = PaginexWhite) 
                }
            }
        }
    }
}

@Composable
fun VerifyEmailScreen(onVerified: () -> Unit, onBackToLogin: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var resendCooldown by remember { mutableStateOf(0) }
    val userEmail = AuthService.getCurrentUser()?.email ?: ""

    // Countdown timer for resend cooldown
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000)
            resendCooldown--
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(PaginexSpace).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Email, contentDescription = null, tint = PaginexNeonPurple, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Verify your email", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PaginexWhite)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "We sent a verification email to $userEmail. Please click the link in the email, then tap the button below.",
            textAlign = TextAlign.Center,
            color = PaginexWhite.copy(alpha = 0.7f)
        )

        if (message != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(message!!, color = Color(0xFFFF6B6B), textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isChecking = true
                message = null
                scope.launch {
                    val verified = AuthService.reloadUser()
                    isChecking = false
                    if (verified) {
                        FirestoreService.refreshSessionCacheFromFirestore()
                        onVerified()
                    } else {
                        message = "Email not verified yet. Please check your inbox and click the verification link."
                    }
                }
            },
            enabled = !isChecking,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isChecking) CircularProgressIndicator(color = PaginexWhite, modifier = Modifier.size(24.dp))
            else Text("I'VE VERIFIED MY EMAIL", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                scope.launch {
                    AuthService.sendVerificationEmail()
                    resendCooldown = 60
                    message = "Verification email resent!"
                }
            },
            enabled = resendCooldown == 0
        ) {
            Text(
                if (resendCooldown > 0) "Resend in ${resendCooldown}s" else "Resend verification email",
                color = if (resendCooldown > 0) Color.Gray else PaginexNeonTeal
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login", color = PaginexWhite.copy(alpha = 0.7f))
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
    var isAscending by remember { mutableStateOf(false) } // Default to false for Latest
    val scope = rememberCoroutineScope()
    
    var showSortMenu by remember { mutableStateOf(false) }

    // Refresh data from Firestore on load and when explicitly needed
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(refreshTrigger) {
        isRefreshing = true
        FirestoreService.refreshSessionCacheFromFirestore()
        isRefreshing = false
        listState.scrollToItem(0)
    }

    val isScrollToEnd by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    var isLoadingMore by remember { mutableStateOf(false) }
    LaunchedEffect(isScrollToEnd) {
        if (isScrollToEnd && !FirestoreService.isFeedEndReached && !isLoadingMore) {
            isLoadingMore = true
            FirestoreService.loadMoreFeed()
            isLoadingMore = false
        }
    }

    LaunchedEffect(currentSortMode, isAscending) {
        listState.scrollToItem(0)
    }

    val posts = when (currentSortMode) {
        "A-Z" -> if (isAscending) AppCache.feedPosts.sortedBy { it.book.title } else AppCache.feedPosts.sortedByDescending { it.book.title }
        "Rating" -> {
            AppCache.feedPosts.filter { it.rating > 0f }
                .let { filtered ->
                    if (isAscending) {
                        filtered.sortedWith(compareBy<Post> { it.rating }.thenByDescending { it.createdAt })
                    } else {
                        filtered.sortedWith(compareByDescending<Post> { it.rating }.thenByDescending { it.createdAt })
                    }
                }
        }
        else -> if (isAscending) AppCache.feedPosts.sortedBy { it.createdAt } else AppCache.feedPosts.sortedByDescending { it.createdAt }
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
                            contentScale = ContentScale.Fit
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
            // Sort Order and Filter Menu newly placed below the logo
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort Order Toggle
                IconButton(onClick = { isAscending = !isAscending }) {
                    Icon(
                        imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Sort Order",
                        tint = PaginexNeonTeal
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort/Filter Menu
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Sort Options", tint = PaginexNeonPurple)
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(PaginexGalaxy)
                    ) {
                        val options = listOf("Latest", "A-Z", "Rating")
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = option, 
                                        color = if (currentSortMode == option) PaginexNeonPurple else PaginexWhite,
                                        fontWeight = if (currentSortMode == option) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                onClick = {
                                    currentSortMode = option
                                    showSortMenu = false
                                    // Auto-set direction for better UX
                                    isAscending = when(option) {
                                        "Latest" -> false
                                        "A-Z" -> true
                                        "Rating" -> false
                                        else -> false
                                    }
                                },
                                leadingIcon = {
                                    if (currentSortMode == option) {
                                        Icon(Icons.Default.Check, null, tint = PaginexNeonPurple, modifier = Modifier.size(18.dp))
                                    }
                                }
                            )
                        }
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
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
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
                                    AppCache.feedPosts.removeAll { it.id == postToDelete.id }
                                    // Write to Firestore in GlobalScope
                                    kotlinx.coroutines.MainScope().launch {
                                        FirestoreService.deletePost(postToDelete.id)
                                    }
                                }
                            },
                            onLikeClick = {
                                // Update local cache immediately
                                val index = AppCache.feedPosts.indexOfFirst { it.id == post.id }
                                val newIsLiked = if (index != -1) !AppCache.feedPosts[index].isLiked else !post.isLiked
                                if (index != -1) {
                                    val currentPost = AppCache.feedPosts[index]
                                    val newLikesCount = if (newIsLiked) currentPost.likesCount + 1 else (currentPost.likesCount - 1).coerceAtLeast(0)
                                    AppCache.feedPosts[index] = currentPost.copy(
                                        isLiked = newIsLiked,
                                        likesCount = newLikesCount
                                    )
                                }
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleLike(post.id, AuthService.getUid(), newIsLiked)
                                }
                            },
                            onSaveClick = {
                                // Update local cache immediately
                                val index = AppCache.feedPosts.indexOfFirst { it.id == post.id }
                                val newIsSaved = if (index != -1) !AppCache.feedPosts[index].isSaved else !post.isSaved
                                if (index != -1) {
                                    val currentPost = AppCache.feedPosts[index]
                                    AppCache.feedPosts[index] = currentPost.copy(
                                        isSaved = newIsSaved
                                    )
                                }
                                // Write to Firestore in GlobalScope
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleSave(post.id, AuthService.getUid(), newIsSaved)
                                }
                            },
                            onCommentAdded = {
                                val index = AppCache.feedPosts.indexOfFirst { it.id == post.id }
                                if (index != -1) {
                                    val currentPost = AppCache.feedPosts[index]
                                    AppCache.feedPosts[index] = currentPost.copy(
                                        commentsCount = currentPost.commentsCount + 1
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    if (isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PaginexNeonPurple)
                            }
                        }
                    }
                }
            }
        }
    }

}

private fun exploreSearchKeywords(query: String): List<String> =
    query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter { it.isNotEmpty() }

private fun bookMatchesExploreKeywords(book: Book, keywords: List<String>): Boolean {
    if (keywords.isEmpty()) return true
    val haystack = book.title.lowercase(Locale.ROOT)
    return keywords.all { kw -> haystack.contains(kw) }
}

private fun userMatchesExploreKeywords(user: User, keywords: List<String>): Boolean {
    if (keywords.isEmpty()) return true
    val haystack = "${user.username} ${user.fullName}".lowercase(Locale.ROOT)
    return keywords.all { kw -> haystack.contains(kw) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(onBookClick: (String) -> Unit = {}) {
    var selectedListForDetails by remember { mutableStateOf<BookList?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val booksTab = "Books"
    val usersTab = "Users"
    var searchType by remember { mutableStateOf(booksTab) }
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
        FirestoreService.refreshSessionCacheFromFirestore()
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
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search in stardust...", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PaginexWhite.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = PaginexGlass,
                        focusedContainerColor = PaginexGlass,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedBorderColor = PaginexNeonTeal,
                        focusedTextColor = PaginexWhite,
                        unfocusedTextColor = PaginexWhite
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Discover Lists Section removed per user request

        // Search Type Toggles
        val searchTypes = listOf(booksTab, usersTab)
        val searchKeywords = exploreSearchKeywords(searchQuery)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            searchTypes.forEach { type ->
                Text(
                    text = type,
                    color = if (searchType == type) PaginexNeonPurple else Color.Gray,
                    fontWeight = if (searchType == type) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { searchType = type }
                )
            }
        }

        if (searchType == booksTab) {
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
    
            val filteredBooks = AppCache.books.filter { book ->
                book.genre != "Booklist" &&
                    (selectedGenre == "All" || book.genre == selectedGenre) &&
                    bookMatchesExploreKeywords(book, searchKeywords)
            }
    
            var exploreBooksLimit by remember { mutableIntStateOf(20) }
            val staggeredGridState = rememberLazyStaggeredGridState()
            val isBooksScrollToEnd by remember {
                derivedStateOf {
                    val totalItems = staggeredGridState.layoutInfo.totalItemsCount
                    val lastVisible = staggeredGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    totalItems > 0 && lastVisible >= totalItems - 4
                }
            }
            LaunchedEffect(isBooksScrollToEnd) {
                if (isBooksScrollToEnd) exploreBooksLimit += 20
            }
            LaunchedEffect(searchQuery, selectedGenre) {
                exploreBooksLimit = 20
                staggeredGridState.scrollToItem(0)
            }

            // Dynamic Staggered Grid
            LazyVerticalStaggeredGrid(
                state = staggeredGridState,
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(filteredBooks.take(exploreBooksLimit), key = { it.id }) { book ->
                // Keep card height deterministic per book to avoid jitter/glitches on recomposition.
                val cardHeight = ((kotlin.math.abs(book.id.hashCode()) % 151) + 200).dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PaginexGlass)
                        .border(1.dp, PaginexGlassBorder, RoundedCornerShape(24.dp))
                        .clickable { 
                            onBookClick(book.id)
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
        } else {
            val filteredUsers = AppCache.users.filter { user ->
                user.isActive && userMatchesExploreKeywords(user, searchKeywords)
            }
            
            var exploreUsersLimit by remember { mutableIntStateOf(20) }
            val listState = rememberLazyListState()
            val isUsersScrollToEnd by remember {
                derivedStateOf {
                    val totalItems = listState.layoutInfo.totalItemsCount
                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    totalItems > 0 && lastVisible >= totalItems - 4
                }
            }
            LaunchedEffect(isUsersScrollToEnd) {
                if (isUsersScrollToEnd) exploreUsersLimit += 20
            }
            LaunchedEffect(searchQuery) {
                exploreUsersLimit = 20
                listState.scrollToItem(0)
            }
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (filteredUsers.isEmpty()) {
                    item {
                        Text("No users found.", color = PaginexWhite.copy(alpha = 0.7f), modifier = Modifier.padding(16.dp))
                    }
                }
                items(filteredUsers.take(exploreUsersLimit), key = { it.id }) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PaginexGlass)
                            .border(1.dp, PaginexGlassBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = avatarModel(user.avatarUrl, "https://via.placeholder.com/150"),
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).clip(CircleShape).border(2.dp, PaginexNeonPurple, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(user.fullName, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("@${user.username}", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    selectedListForDetails?.let { list ->
        BookListDetailsDialog(
            bookList = list,
            isOwner = false,
            onDismiss = { selectedListForDetails = null },
            onRemoveBookClick = {}
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    postId: String?,
    ownerOnly: Boolean = false,
    onBack: () -> Unit,
    onReviewClick: (String) -> Unit = {}
) {
    var selectedListForDetails by remember { mutableStateOf<BookList?>(null) }
    val foundBook = AppCache.books.find { it.id == postId }
    val initialPost = AppCache.feedPosts.find { it.id == postId || it.book.id == postId } 
        ?: (if (foundBook != null) Post(id = "dummy", userId = "system", book = foundBook, status = "Read", rating = 0f, review = "No posts yet.", createdAt = 0L) else null)
        ?: AppCache.feedPosts.firstOrNull() 
        ?: return
    val book = initialPost.book
    val bookReviews = AppCache.feedPosts.filter { 
        it.book.id == book.id && (!ownerOnly || it.userId == AuthService.getUid())
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
                Icon(Icons.Default.DateRange, contentDescription = null, tint = PaginexWhite.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Year: ${book.publishYear}", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Icon(Icons.Default.Info, contentDescription = null, tint = PaginexWhite.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ISBN: ${if (book.isbn.isNotEmpty()) book.isbn else "000-0000000000"}", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(String.format("%.1f/10", avgRating), color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.weight(1f))

                val bookIdx = AppCache.books.indexOfFirst { it.id == book.id }
                val isBookSaved = if (bookIdx != -1) AppCache.books[bookIdx].isSaved else false
                val saveScope = rememberCoroutineScope()
                IconButton(onClick = {
                    if (bookIdx != -1) {
                        AppCache.books[bookIdx] = AppCache.books[bookIdx].copy(isSaved = !isBookSaved)
                        saveScope.launch { FirestoreService.toggleBookSave(book.id, AuthService.getUid(), !book.isSaved) }
                    }
                }) {
                    Icon(
                        imageVector = if (isBookSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Book",
                        tint = if (isBookSaved) PaginexNeonPurple else Color.Gray
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { onReviewClick(reviewPost.id) },
                    colors = CardDefaults.cardColors(containerColor = PaginexGlass),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, PaginexGlassBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val reviewer = reviewerCache[reviewPost.userId]
                            AsyncImage(
                                model = avatarModel(reviewer?.avatarUrl ?: ""), 
                                contentDescription = null,
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reviewer.uiDisplayHandle(), color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
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
            
            // Booklists made for this book
            Spacer(modifier = Modifier.height(32.dp))
            val listsWithBook = AppCache.bookLists.filter { list -> list.books.any { it.id == book.id } }
            if (listsWithBook.isNotEmpty()) {
                Text("Kitap İnfografisinde Yapılan Booklistler", color = PaginexWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(listsWithBook, key = { it.id }) { list ->
                        Box(modifier = Modifier.width(280.dp).padding(end = 4.dp)) {
                            BookListCard(
                                bookList = list,
                                isOwner = list.userId == AuthService.getUid(),
                                onListClick = { selectedListForDetails = list },
                                onAddBook = {},
                                onLikeClick = {
                                    val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                                    if (idx != -1) {
                                        val current = AppCache.bookLists[idx]
                                        val newIsLiked = !current.isLiked
                                        AppCache.bookLists[idx] = current.copy(
                                            isLiked = newIsLiked,
                                            likesCount = if (newIsLiked) current.likesCount + 1 else (current.likesCount - 1).coerceAtLeast(0)
                                        )
                                        kotlinx.coroutines.MainScope().launch {
                                            FirestoreService.toggleBookListLike(list.id, AuthService.getUid(), newIsLiked)
                                        }
                                    }
                                },
                                onSaveClick = {
                                    val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                                    val newIsSaved = if (idx != -1) !AppCache.bookLists[idx].isSaved else !list.isSaved
                                    if (idx != -1) {
                                        val current = AppCache.bookLists[idx]
                                        AppCache.bookLists[idx] = current.copy(isSaved = newIsSaved)
                                        kotlinx.coroutines.MainScope().launch {
                                            FirestoreService.toggleBookListSave(list.id, AuthService.getUid(), newIsSaved)
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

    selectedListForDetails?.let { list ->
        BookListDetailsDialog(
            bookList = list,
            isOwner = false,
            onDismiss = { selectedListForDetails = null },
            onRemoveBookClick = {}
        )
    }

}

@Composable
fun readingStatusColor(status: String): Color = when (status) {
    "Reading" -> PaginexNeonPurple
    "Plan To Read" -> PaginexNeonPink
    "Completed" -> PaginexNeonTeal
    "On-hold" -> Color(0xFFD97706)
    "Dropped" -> Color(0xFFEF4444)
    else -> PaginexWhite.copy(alpha = 0.7f)
}

@Composable
fun PaginexProfileScreen(
    onEditClick: () -> Unit, // This will now go to SettingsScreen
    onListsClick: () -> Unit,
    onConstellationClick: () -> Unit,
    onGalaxyBookClick: (String) -> Unit,
    onMyPostClick: (String) -> Unit,
    onLogoutClick: () -> Unit = {} // This might not be needed directly here anymore, but keeping it
) {
    val user = AppCache.currentUser
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
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedFilterStatus by remember { mutableStateOf("Reading") }
    val availableStatuses = listOf("Completed", "Reading", "Plan To Read", "On-hold", "Dropped")
    val filteredLibrary = AppCache.readingStatuses.filter { it.status == selectedFilterStatus && it.userId == (AuthService.getUid()) }
    val scope = rememberCoroutineScope()

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", color = PaginexWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Profilden çıkmaktan emin misiniz?", color = PaginexWhite.copy(alpha = 0.7f)) },
            containerColor = PaginexGalaxy,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogoutClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Evet", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Hayır", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }

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
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ---- GALAXY VISUALIZATION ----
            UserGalaxy(user = user, rotation = rotation, onBookClick = onGalaxyBookClick)

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
                        AppCache.currentUser = AppCache.currentUser.copy(favoriteBooks = selectedIds)
                        showFavoritesDialog = false
                        // Write to Firestore in GlobalScope
                        kotlinx.coroutines.MainScope().launch {
                            FirestoreService.updateFavoriteBooks(AuthService.getUid(), selectedIds)
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
                Icon(Icons.Default.DateRange, null, tint = PaginexWhite.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                Text(" ${user.joinDate}", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 12.sp)
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
                    Text("${AppCache.readingStatuses.count { it.userId == user.id }}", fontWeight = FontWeight.ExtraBold, color = PaginexNeonTeal, fontSize = 20.sp)
                    Text("Books", fontSize = 11.sp, color = PaginexWhite.copy(alpha = 0.7f))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showFollowers = true }
                ) {
                    Text("${user.followersCount}", fontWeight = FontWeight.ExtraBold, color = PaginexWhite, fontSize = 20.sp)
                    Text("Followers", fontSize = 11.sp, color = PaginexWhite.copy(alpha = 0.7f))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showFollowing = true }
                ) {
                    Text("${user.followingCount}", fontWeight = FontWeight.ExtraBold, color = PaginexWhite, fontSize = 20.sp)
                    Text("Following", fontSize = 11.sp, color = PaginexWhite.copy(alpha = 0.7f))
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
                    Icon(Icons.Default.Add, null, tint = PaginexWhite, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Book", color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
            Spacer(modifier = Modifier.height(20.dp))
            
            // Filters
            ScrollableTabRow(
                selectedTabIndex = availableStatuses.indexOf(selectedFilterStatus).coerceAtLeast(0),
                containerColor = Color.Transparent,
                edgePadding = 20.dp,
                indicator = {},
                divider = {}
            ) {
                availableStatuses.forEach { status ->
                    val isSelected = selectedFilterStatus == status
                    val color = readingStatusColor(status)
                    Tab(
                        selected = isSelected,
                        onClick = { selectedFilterStatus = status },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) color.copy(alpha = 0.2f) else PaginexGlass)
                                .border(1.dp, if (isSelected) color else PaginexGlassBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                status,
                                color = if (isSelected) color else PaginexWhite.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredLibrary.isNotEmpty()) {
                val color = readingStatusColor(selectedFilterStatus)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedFilterStatus == "Reading") {
                                // Pulsing dot
                                val pulse by infiniteTransition.animateFloat(
                                    initialValue = 0.4f, targetValue = 1f,
                                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = ""
                                )
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color.copy(alpha = pulse), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                selectedFilterStatus.uppercase(),
                                color = color,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        filteredLibrary.forEach { rs ->
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
                                    Text(rs.book.author, color = PaginexWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text(rs.book.genre, color = PaginexNeonPurple.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }


            // ---- MY POSTS SECTION ----
            val myPosts = AppCache.feedPosts.filter { it.userId == (AuthService.getUid()) }
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
                                    .clickable { onMyPostClick(post.id) }
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
                                        Text(post.review, color = PaginexWhite.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                val uid = AuthService.getUid()
                // Optimistically add to local cache
                val newStatus = ReadingStatus(
                    id = "rs_${System.currentTimeMillis()}",
                    userId = uid,
                    book = book,
                    status = status
                )
                val existingIndex = AppCache.readingStatuses.indexOfFirst { it.userId == uid && it.book.id == book.id }
                if (existingIndex != -1) {
                    AppCache.readingStatuses[existingIndex] = newStatus
                } else {
                    AppCache.readingStatuses.add(newStatus)
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
    val user = AppCache.currentUser
    val userEmail = AuthService.getUserEmail()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // We parse name/surname from fullName for simpler editing, though you could keep it as fullName
    val initialName = user.fullName.substringBefore(" ")
    val initialSurname = user.fullName.substringAfter(" ", "")
    
    var name by remember { mutableStateOf(initialName) }
    var surname by remember { mutableStateOf(initialSurname) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio) }
    var location by remember { mutableStateOf(user.location) }
    var avatarUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        avatarUri = uri
    }

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
                modifier = Modifier.fillMaxWidth().clickable { photoPickerLauncher.launch("image/*") },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = avatarUri ?: avatarModel(user.avatarUrl),
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
            CosmicInputField(label = "Email address (Cannot be changed)", value = userEmail, onValueChange = { }, enabled = false)
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "Username", value = username, onValueChange = { username = it })
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "First Name", value = name, onValueChange = { name = it })
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "Last Name", value = surname, onValueChange = { surname = it })
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "Bio", value = bio, onValueChange = { bio = it }, isLongText = true)
            Spacer(modifier = Modifier.height(16.dp))
            CosmicInputField(label = "Location", value = location, onValueChange = { location = it })
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    if (isLoading) return@Button
                    isLoading = true
                    val actualUid = AuthService.getUid()
                    scope.launch {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                            // 1. Upload new avatar if selected
                            var newAvatarUrl = user.avatarUrl
                            if (avatarUri != null) {
                                val uploadedUrl = FirestoreService.uploadProfileImage(actualUid, avatarUri!!, context)
                                if (uploadedUrl != null) newAvatarUrl = uploadedUrl
                            }
                            
                            // 2. Update Firestore
                            val updates = mapOf(
                                "name" to name,
                                "surname" to surname,
                                "username" to username,
                                "bio" to bio,
                                "location" to location,
                                "avatarUrl" to newAvatarUrl
                            )
                            val success = FirestoreService.updateUserProfile(actualUid, updates)
                            
                            if (success) {
                                FirestoreService.refreshSessionCacheFromFirestore() // Refresh local cache
                                onSave()
                            }
                            isLoading = false
                        }
                    }
                }, 
                modifier = Modifier.align(Alignment.CenterHorizontally).height(50.dp).width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
                shape = RoundedCornerShape(25.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) { 
                if (isLoading) {
                    CircularProgressIndicator(color = PaginexWhite, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = PaginexWhite) 
                }
            }
        }
    }

}

@Composable
fun CosmicInputField(label: String, value: String, onValueChange: (String) -> Unit, isLongText: Boolean = false, enabled: Boolean = true) {
    Column {
        Text(label, color = PaginexNeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = if (enabled) PaginexGlass else PaginexGlass.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PaginexGlassBorder)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().then(if (isLongText) Modifier.height(120.dp) else Modifier),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    focusedTextColor = PaginexWhite,
                    unfocusedTextColor = PaginexWhite,
                    disabledTextColor = PaginexWhite.copy(alpha = 0.5f),
                    disabledContainerColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
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
    
    val uniquePosts = AppCache.feedPosts
        .filter { it.isSaved && it.book.title.contains(searchQuery, ignoreCase = true) }
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
                placeholder = { Text("Search book...", color = PaginexWhite.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = PaginexWhite.copy(alpha = 0.7f)) },
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
        
        var savedLimit by remember { mutableIntStateOf(20) }
        val gridState = rememberLazyGridState()
        val isScrollToEnd by remember {
            derivedStateOf {
                val totalItems = gridState.layoutInfo.totalItemsCount
                val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                totalItems > 0 && lastVisible >= totalItems - 4
            }
        }
        LaunchedEffect(isScrollToEnd) {
            if (isScrollToEnd) savedLimit += 20
        }
        LaunchedEffect(searchQuery) {
            savedLimit = 20
            gridState.scrollToItem(0)
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(uniquePosts.take(savedLimit), key = { it.id }) { post ->
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
    val currentUid = AuthService.getUid()
    val isOwner = targetUserId == currentUid
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val bookLists = if (isOwner) {
        if (selectedTab == 0) {
            AppCache.bookLists.filter { it.userId == targetUserId }
        } else {
            AppCache.bookLists.filter { it.isSaved }
        }
    } else {
        AppCache.bookLists.filter { it.userId == targetUserId && !it.isPrivate }
    }
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedListForAdd by remember { mutableStateOf<BookList?>(null) }
    var listToEdit by remember { mutableStateOf<BookList?>(null) }
    var selectedListForDetails by remember { mutableStateOf<BookList?>(null) }




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
                    color = PaginexWhite.copy(alpha = 0.7f)
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
                        onListClick = { selectedListForDetails = list },
                        onAddBook = { selectedListForAdd = list },
                        onLikeClick = {
                            val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                            if (idx != -1) {
                                val current = AppCache.bookLists[idx]
                                val newIsLiked = !current.isLiked
                                AppCache.bookLists[idx] = current.copy(
                                    isLiked = newIsLiked,
                                    likesCount = if (newIsLiked) current.likesCount + 1 else (current.likesCount - 1).coerceAtLeast(0)
                                )
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListLike(list.id, currentUid, newIsLiked)
                                }
                            }
                        },
                        onSaveClick = {
                            val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                            val newIsSaved = if (idx != -1) !AppCache.bookLists[idx].isSaved else !list.isSaved
                            if (idx != -1) {
                                val current = AppCache.bookLists[idx]
                                AppCache.bookLists[idx] = current.copy(isSaved = newIsSaved)
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.toggleBookListSave(list.id, currentUid, newIsSaved)
                                }
                            }
                        },
                        onEditClick = { listToEdit = list },
                        onDeleteClick = {
                            AppCache.bookLists.removeAll { it.id == list.id }
                            kotlinx.coroutines.MainScope().launch {
                                FirestoreService.deleteBookList(list.id)
                            }
                        },
                        onRemoveBookClick = { bookId ->
                            val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                            if (idx != -1) {
                                val current = AppCache.bookLists[idx]
                                val updatedBooks = current.books.toMutableList()
                                updatedBooks.removeAll { it.id == bookId }
                                AppCache.bookLists[idx] = current.copy(books = updatedBooks)
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
                        AppCache.bookLists.add(0, newList)
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
                val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                if (idx != -1) {
                    AppCache.bookLists[idx] = AppCache.bookLists[idx].copy(
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
                val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                if (idx != -1) {
                    val updatedBooks = AppCache.bookLists[idx].books.toMutableList()
                    if (updatedBooks.none { it.id == book.id }) {
                        updatedBooks.add(book)
                        AppCache.bookLists[idx] = AppCache.bookLists[idx].copy(books = updatedBooks)
                        kotlinx.coroutines.MainScope().launch {
                            FirestoreService.addBookToList(list.id, book.id)
                        }
                    }
                }
                selectedListForAdd = null
            }
        )
    }

    selectedListForDetails?.let { list ->
        BookListDetailsDialog(
            bookList = list,
            isOwner = list.userId == currentUid,
            onDismiss = { selectedListForDetails = null },
            onRemoveBookClick = { bookId ->
                val idx = AppCache.bookLists.indexOfFirst { it.id == list.id }
                if (idx != -1) {
                    val current = AppCache.bookLists[idx]
                    val updatedBooks = current.books.toMutableList()
                    updatedBooks.removeAll { it.id == bookId }
                    val updatedList = current.copy(books = updatedBooks)
                    AppCache.bookLists[idx] = updatedList
                    selectedListForDetails = updatedList // Trigger immediate recomposition
                    kotlinx.coroutines.MainScope().launch {
                        FirestoreService.removeBookFromList(list.id, bookId)
                    }
                }
            }
        )
    }
}

@Composable
fun BookListDetailsDialog(
    bookList: BookList,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onRemoveBookClick: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(bookList.name, color = PaginexWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (bookList.description.isNotEmpty()) {
                    Text(bookList.description, color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
        },
        text = {
            if (bookList.books.isEmpty()) {
                Text("No books in this list.", color = PaginexWhite.copy(alpha = 0.7f))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    items(bookList.books, key = { it.id }) { book ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PaginexGlass)
                                .border(1.dp, PaginexGlassBorder, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentAlignment = Alignment.TopEnd
                                ) {
                                    AsyncImage(
                                        model = book.coverUrl,
                                        contentDescription = book.title,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (isOwner) {
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                .clickable { onRemoveBookClick(book.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = book.title,
                                    color = PaginexWhite,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    modifier = Modifier.padding(6.dp),
                                    lineHeight = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = PaginexWhite.copy(alpha = 0.7f), fontWeight = FontWeight.Bold) }
        }
    )
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
                    label = { Text("List name", color = PaginexWhite.copy(alpha = 0.7f)) },
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
                    label = { Text("Description (optional)", color = PaginexWhite.copy(alpha = 0.7f)) },
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
                Text("Save", color = PaginexWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = PaginexWhite.copy(alpha = 0.7f)) }
        }
    )
}

@Composable
fun BookListCard(
    bookList: BookList,
    isOwner: Boolean,
    onListClick: () -> Unit,
    onAddBook: () -> Unit,
    onLikeClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onRemoveBookClick: (String) -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onListClick() },
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
                        Text(
                            bookList.name, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 16.sp, 
                            color = PaginexWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (bookList.isPrivate) Icons.Default.Lock else Icons.Default.Public,
                            contentDescription = null,
                            tint = PaginexWhite.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (bookList.description.isNotEmpty()) {
                        Text(bookList.description, fontSize = 12.sp, color = PaginexWhite.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text("${bookList.books.size} books", fontSize = 12.sp, color = PaginexNeonPurple.copy(alpha = 0.8f))
                }
                
                if (isOwner) {
                    IconButton(onClick = onAddBook) {
                        Icon(Icons.Default.Add, null, tint = PaginexNeonPurple)
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = PaginexWhite.copy(alpha = 0.7f))
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
                    label = { Text("List name", color = PaginexWhite.copy(alpha = 0.7f)) },
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
                    label = { Text("Description (optional)", color = PaginexWhite.copy(alpha = 0.7f)) },
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
                Text("Create", color = PaginexWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = PaginexWhite.copy(alpha = 0.7f)) }
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
                val currentUid = AuthService.getUid()
                val userLibraryBooks = AppCache.readingStatuses.filter { it.userId == currentUid }.map { it.book }
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
                            Text(book.author, color = PaginexWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(book.genre, color = PaginexNeonTeal.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = PaginexWhite.copy(alpha = 0.7f)) }
        }
    )
}

@Composable
fun AddBookToLibraryDialog(onDismiss: () -> Unit, onBookAdded: (Book, String) -> Unit) {
    var selectedStatus by remember { mutableStateOf("Plan To Read") }
    val statuses = listOf("Completed", "Reading", "Plan To Read", "On-hold", "Dropped")
    var searchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaginexGalaxy,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Add Book to Library", color = PaginexWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Select status:", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(statuses) { s ->
                        val color = readingStatusColor(s)
                        Surface(
                            onClick = { selectedStatus = s },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selectedStatus == s) color.copy(alpha = 0.2f) else PaginexGlass,
                            border = BorderStroke(1.dp, if (selectedStatus == s) color else PaginexGlassBorder)
                        ) {
                            Text(s, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                color = if (selectedStatus == s) color else PaginexWhite.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    placeholder = { Text("Search...", color = PaginexWhite.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PaginexNeonPurple,
                        unfocusedBorderColor = PaginexGlassBorder,
                        focusedTextColor = PaginexWhite,
                        unfocusedTextColor = PaginexWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Select book:", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(200.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val currentUid = AuthService.getUid()
                    val existingBookIds = AppCache.readingStatuses.filter { it.userId == currentUid }.map { it.book.id }.toSet()
                    val filteredBooks = AppCache.books.filter { it.title.contains(searchQuery, ignoreCase = true) && !existingBookIds.contains(it.id) }
                    items(filteredBooks) { book ->
                        val isBookSelected = selectedBook?.id == book.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isBookSelected) PaginexNeonPurple.copy(alpha = 0.2f) else PaginexGlass)
                                .border(1.dp, if (isBookSelected) PaginexNeonPurple else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { selectedBook = book }
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
                                Text(book.author, color = PaginexWhite.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedBook?.let { onBookAdded(it, selectedStatus) } },
                enabled = selectedBook != null,
                colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tamam", color = PaginexWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = PaginexWhite.copy(alpha = 0.7f)) }
        }
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstellationScreen(targetUserId: String, onBack: () -> Unit, onBookClick: (String) -> Unit) {
    var targetUser by remember { mutableStateOf(AppCache.currentUser) }
    LaunchedEffect(targetUserId) {
        if (targetUserId != AuthService.getUid()) {
            FirestoreService.getUserProfile(targetUserId)?.let { targetUser = it }
        }
    }
    val readingList = AppCache.readingStatuses.filter { it.userId == targetUserId }
    val viewerUid = AuthService.getUid()
    val favoriteIdsForRank =
        if (targetUserId == viewerUid) AppCache.currentUser.favoriteBooks else targetUser.favoriteBooks
    val libraryKey = readingList.joinToString("|") { "${it.book.id}:${it.addedAt}" }
    val postsKey = AppCache.feedPosts.joinToString("|") {
        "${it.id}:${it.isLiked}:${it.isSaved}:${it.rating}:${it.userId}:${it.book.id}"
    }
    val rankedResult = remember(libraryKey, postsKey, favoriteIdsForRank.joinToString(), targetUserId, viewerUid) {
        ConstellationRanking.rankForConstellation(
            readingList,
            AppCache.feedPosts.toList(),
            favoriteIdsForRank,
            targetUserId,
            viewerUid
        )
    }
    val rankedGenreMap = rankedResult.rankedByGenre
    val extraBooksPerGenre = rankedResult.extraBooksPerGenre
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
                        val isMe = targetUserId == AuthService.getUid()
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
                val bookLabelMaxWidth = with(density) { (bwW * 0.22f).toDp() }.coerceIn(64.dp, 132.dp)
                val cx = bwW / 2f
                val cy = bwH / 2f
                val rootPos = Offset(cx, cy)
                val genreCount = genres.size.coerceAtLeast(1)
                val genreRadius = bwW * 0.28f
                val bookRadiusConfig = bwW * 0.185f
                val neonTeal = PaginexNeonTeal

                fun bookOrbitAngleAndDist(bi: Int, bookCount: Int, genreAngleBaseRad: Double): Pair<Double, Float> {
                    val n = bookCount.coerceAtLeast(1)
                    val angleStepRad =
                        if (n <= 1) 0.0 else (3.35 / (n - 1)).coerceIn(0.28, 0.88)
                    val spread =
                        if (n <= 1) 0.0 else (bi - (n - 1) / 2.0) * angleStepRad
                    val ang = genreAngleBaseRad + spread
                    val ring = bi % 2
                    val dist = bookRadiusConfig + ring * 54f
                    return ang to dist
                }

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
                        val books = rankedGenreMap[genre] ?: emptyList()

                        // Only render books if expanded — top-ranked stars per genre
                        val renderBooks = if (isExpanded) books else emptyList()

                        val genreAngleBase = Math.toRadians((gi * 360.0 / genreCount) - 90.0)
                        val rc = renderBooks.size
                        renderBooks.forEachIndexed { bi, bookStatus ->
                            val (bookAngleRad, bookDist) = bookOrbitAngleAndDist(bi, rc, genreAngleBase)
                            val bx = gx + (bookDist * cos(bookAngleRad)).toFloat()
                            val by = gy + (bookDist * sin(bookAngleRad)).toFloat()
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
                                radius = 5.5f / scale.coerceAtLeast(1f),
                                center = bPos
                            )
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(neonTeal.copy(alpha = bookAlpha * 0.45f), Color.Transparent),
                                    center = bPos, radius = 14f / scale.coerceAtLeast(1f)
                                ),
                                radius = 14f / scale.coerceAtLeast(1f),
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
                            .offset(x = with(density) { (gx - 44f).toDp() }, y = with(density) { (gy - 28f).toDp() })
                            .widthIn(min = 72.dp, max = 100.dp)
                            .wrapContentHeight()
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                genre,
                                color = if (isExpanded) PaginexNeonTeal else Color(0xFFFFD700).copy(alpha = 0.9f),
                                fontSize = (8f / scale.coerceAtLeast(1f)).sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            val extra = extraBooksPerGenre[genre] ?: 0
                            if (isExpanded && extra > 0) {
                                Text(
                                    "+$extra in library",
                                    color = PaginexWhite.copy(alpha = 0.55f),
                                    fontSize = max(5f, 6f / scale.coerceAtLeast(1f)).sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        val books = rankedGenreMap[genre] ?: emptyList()
                        val genreAngleBase = Math.toRadians((gi * 360.0 / genreCount) - 90.0)
                        val bc = books.size
                        books.forEachIndexed { bi, bookStatus ->
                            val (bookAngleRad, bookDist) = bookOrbitAngleAndDist(bi, bc, genreAngleBase)
                            val bx = gx + (bookDist * cos(bookAngleRad)).toFloat()
                            val by = gy + (bookDist * sin(bookAngleRad)).toFloat()
                            val bookLabelFontSize =
                                (6f / scale.coerceAtLeast(1f)).coerceIn(5f, 7f)
                            val halfLabelPx = with(density) { bookLabelMaxWidth.toPx() / 2f }
                            Box(
                                modifier = Modifier
                                    .offset(
                                        x = with(density) { (bx - halfLabelPx).toDp() },
                                        y = with(density) { (by + 6f).toDp() }
                                    )
                                    .width(bookLabelMaxWidth)
                                    .clickable { onBookClick(bookStatus.book.id) }
                            ) {
                                Text(
                                    bookStatus.book.title,
                                    color = PaginexNeonTeal.copy(alpha = 0.85f),
                                    fontSize = bookLabelFontSize.sp,
                                    lineHeight = (bookLabelFontSize + 1f).sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
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
                    Text("Profile / Genre", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(PaginexNeonTeal, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Book (top picks)",
                        color = PaginexWhite.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
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
                        "Completed" -> Icons.Default.CheckCircle
                        "Plan To Read" -> Icons.Default.Star
                        "On-hold" -> Icons.Default.Info
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

/** At most one draft per book, or one per book list (same user). Excludes the draft currently being edited. */
private fun findConflictingDraft(
    drafts: List<Post>,
    userId: String,
    selectedBook: Book?,
    selectedBookList: BookList?,
    editingDraftId: String?
): Post? {
    if (selectedBook == null && selectedBookList == null) return null
    val isBooklist = selectedBookList != null
    val keyId = if (isBooklist) selectedBookList!!.id else selectedBook!!.id
    return drafts.firstOrNull { d ->
        if (d.userId != userId) return@firstOrNull false
        if (editingDraftId != null && d.id == editingDraftId) return@firstOrNull false
        when {
            isBooklist -> d.isBooklistPost && (d.booklist?.id == keyId || d.book.id == keyId)
            else -> !d.isBooklistPost && d.book.id == keyId
        }
    }
}

@Composable
private fun DraftSavedSnackbar(snackbarData: SnackbarData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = PaginexGalaxy.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, PaginexNeonTeal.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PaginexNeonTeal.copy(alpha = 0.4f),
                                PaginexNeonTeal.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PaginexNeonTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = snackbarData.visuals.message,
                color = PaginexWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(initialPostId: String? = null, onPost: () -> Unit, onDraftsClick: () -> Unit) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val initialPost = remember(initialPostId) {
        AppCache.feedPosts.find { it.id == initialPostId } ?: AppCache.drafts.find { it.id == initialPostId }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<Book?>(initialPost?.book) }
    var status by remember { mutableStateOf<String?>(initialPost?.status) }
    var reviewText by remember { mutableStateOf(initialPost?.review ?: "") }
    var showResults by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(initialPost?.rating ?: 5f) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var showLibrarySelector by remember { mutableStateOf(false) }
    var showListSelector by remember { mutableStateOf(false) }
    var selectedBookList by remember { mutableStateOf<BookList?>(initialPost?.booklist) }
    var showDuplicateDraftDialog by remember { mutableStateOf(false) }
    
    // Undo / countdown: publish only (draft saves immediately + snackbar).
    var undoTimer by remember { mutableStateOf<Int?>(null) }
    var pendingPost by remember { mutableStateOf<Post?>(null) }

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
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedButton(
                            onClick = { showLibrarySelector = true },
                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.8f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, PaginexNeonPurple.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Add, null, tint = PaginexNeonPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select book", color = PaginexNeonPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = { showListSelector = true },
                            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, PaginexNeonTeal.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.List, null, tint = PaginexNeonTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select booklist", color = PaginexNeonTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                if (selectedBook != null || selectedBookList != null) {
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
                            if (selectedBook != null) {
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
                                    Text(selectedBook!!.title, color = PaginexWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(selectedBook!!.author, color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("SELECTED BOOK", color = PaginexNeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                }
                            } else if (selectedBookList != null) {
                                if (selectedBookList!!.coverUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = selectedBookList!!.coverUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp, 90.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(modifier = Modifier.size(60.dp, 90.dp).background(PaginexNeonTeal.copy(alpha=0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.List, null, tint = PaginexNeonTeal)
                                    }
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Column {
                                    Text(selectedBookList!!.name, color = PaginexWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("${selectedBookList!!.books.size} books", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("SELECTED LIST", color = PaginexNeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { 
                                selectedBook = null
                                selectedBookList = null
                            }) {
                                Icon(Icons.Default.Close, null, tint = PaginexNeonPink)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // 3. Step: Expressive Input Pane
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PaginexGlass),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, PaginexGlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("MENTAL JOURNEY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PaginexNeonTeal, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            BasicTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                modifier = Modifier.fillMaxWidth().height(160.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = PaginexWhite, fontSize = 16.sp, lineHeight = 22.sp),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                                        if (reviewText.isEmpty()) {
                                            Text(
                                                "Let your thoughts flow like stars...",
                                                color = PaginexWhite.copy(alpha = 0.7f),
                                                style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 22.sp)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Rating Slider
                    if (status != "Plan To Read") {
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

                    // Final Action Buttons
                    val isReady = reviewText.isNotEmpty() && (selectedBook != null || selectedBookList != null) && status != null
                    val btnColor = when (status) {
                        "Read" -> PaginexNeonTeal
                        "Reading" -> PaginexNeonPurple
                        "Dropped" -> PaginexNeonPink
                        else -> PaginexNeonTeal
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isReady) {
                                    val uid = AuthService.getUid()
                                    val editingDraftId =
                                        if (initialPostId != null && initialPostId.startsWith("draft_")) initialPostId else null
                                    val conflict = findConflictingDraft(
                                        AppCache.drafts,
                                        uid,
                                        selectedBook,
                                        selectedBookList,
                                        editingDraftId
                                    )
                                    if (conflict != null) {
                                        showDuplicateDraftDialog = true
                                        return@OutlinedButton
                                    }
                                    val dummyBook = if (selectedBookList != null) {
                                        Book(
                                            id = selectedBookList!!.id,
                                            title = selectedBookList!!.name,
                                            author = "List by author",
                                            coverUrl = selectedBookList!!.coverUrl,
                                            genre = "Booklist"
                                        )
                                    } else {
                                        selectedBook!!
                                    }
                                    val draftId = if (initialPostId != null && initialPostId.startsWith("draft_")) initialPostId else "draft_${System.currentTimeMillis()}"
                                    val draft = Post(
                                        id = draftId,
                                        userId = uid,
                                        book = dummyBook,
                                        status = status ?: "Plan To Read",
                                        rating = rating,
                                        review = reviewText,
                                        isBooklistPost = selectedBookList != null,
                                        booklist = selectedBookList
                                    )
                                    if (initialPostId != null && initialPostId != draft.id) {
                                        val oldDraftIdx = AppCache.drafts.indexOfFirst { it.id == initialPostId }
                                        if (oldDraftIdx != -1) AppCache.drafts.removeAt(oldDraftIdx)
                                    }
                                    val existingDraftIdx = AppCache.drafts.indexOfFirst { it.id == draft.id }
                                    if (existingDraftIdx != -1) AppCache.drafts[existingDraftIdx] = draft
                                    else AppCache.drafts.add(0, draft)
                                    val draftedMsg =
                                        if (selectedBookList != null) "The booklist is drafted!" else "The book is drafted!"
                                    scope.launch {
                                        if (initialPostId != null && initialPostId != draft.id && initialPostId.startsWith("draft_")) {
                                            FirestoreService.deleteDraft(initialPostId)
                                        }
                                        FirestoreService.createDraft(draft)
                                        snackbarHostState.showSnackbar(
                                            message = draftedMsg,
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                }
                            },
                            enabled = isReady,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(2.dp, if (isReady) PaginexNeonPurple else PaginexGlassBorder)
                        ) {
                            Text(
                                "DRAFT",
                                color = if (isReady) PaginexNeonPurple else Color.Gray,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            onClick = { if (isReady) showPublishDialog = true },
                            enabled = isReady,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
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
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        if (showLibrarySelector) {
            LibrarySelectorSheet(
                targetUserId = AuthService.getUid(),
                onDismiss = { showLibrarySelector = false },
                onBookSelected = { rs -> 
                    selectedBook = rs.book
                    selectedBookList = null
                    status = rs.status
                    showLibrarySelector = false
                }
            )
        }

        if (showListSelector) {
            BookListSelectorSheet(
                targetUserId = AuthService.getUid(),
                onDismiss = { showListSelector = false },
                onListSelected = { lst ->
                    selectedBookList = lst
                    selectedBook = null
                    status = "Read" // Dummy status
                    showListSelector = false
                }
            )
        }

        if (showDuplicateDraftDialog) {
            AlertDialog(
                onDismissRequest = { showDuplicateDraftDialog = false },
                containerColor = PaginexGalaxy,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text("The draft already exists.", color = PaginexWhite, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        if (selectedBookList != null) {
                            "A draft already exists for this book list. You cannot create a second draft using the same list. You can edit or delete the existing draft from the Drafts screen."
                        } else {
                            "A draft for this book already exists. You cannot create a second draft for the same book. You can edit or delete the existing draft from the Drafts screen."
                        },
                        color = PaginexWhite.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showDuplicateDraftDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Tamam", color = PaginexWhite, fontWeight = FontWeight.Bold) }
                }
            )
        }

        if (showPublishDialog) {
            AlertDialog(
                onDismissRequest = { showPublishDialog = false },
                containerColor = PaginexGalaxy,
                title = { Text("Publish post?", color = PaginexWhite) },
                text = { Text("Are you sure you want to publish?", color = PaginexWhite.copy(alpha = 0.7f)) },
                confirmButton = {
                    Button(
                        onClick = { 
                            showPublishDialog = false
                            val uid = AuthService.getUid()
                            val dummyBook = if (selectedBookList != null) {
                                Book(
                                    id = selectedBookList!!.id,
                                    title = selectedBookList!!.name,
                                    author = "List by author",
                                    coverUrl = selectedBookList!!.coverUrl,
                                    genre = "Booklist"
                                )
                            } else {
                                selectedBook!!
                            }
                            val newPost = Post(
                                id = "post_${System.currentTimeMillis()}",
                                userId = uid,
                                book = dummyBook,
                                status = status ?: "Plan To Read",
                                rating = rating,
                                review = reviewText,
                                isBooklistPost = selectedBookList != null,
                                booklist = selectedBookList
                            )
                            pendingPost = newPost
                            // Add to local cache immediately
                            AppCache.feedPosts.add(0, newPost)
                            // Handle old post cleanup + Firestore write in GlobalScope (survives navigation)
                            kotlinx.coroutines.MainScope().launch {
                                if (initialPostId != null) {
                                    val ei = AppCache.feedPosts.indexOfFirst { it.id == initialPostId }
                                    if (ei != -1) AppCache.feedPosts.removeAt(ei)
                                    val edi = AppCache.drafts.indexOfFirst { it.id == initialPostId }
                                    if (edi != -1) AppCache.drafts.removeAt(edi)
                                    if (initialPostId.startsWith("draft_")) FirestoreService.deleteDraft(initialPostId)
                                    else FirestoreService.deletePost(initialPostId)
                                }
                                FirestoreService.createPost(newPost)
                            }
                            undoTimer = 5 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
                    ) { Text("Publish", color = PaginexWhite) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showPublishDialog = false },
                        border = BorderStroke(1.dp, PaginexGlassBorder)
                    ) { Text("Cancel", color = PaginexWhite) }
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
                        Text("Publishing...", color = PaginexWhite, fontSize = 13.sp)
                    }
                    
                    TextButton(
                        onClick = { 
                            val postToUndo = pendingPost
                            if (postToUndo != null) {
                                val idx = AppCache.feedPosts.indexOfFirst { it.id == postToUndo.id }
                                if (idx != -1) AppCache.feedPosts.removeAt(idx)
                                kotlinx.coroutines.MainScope().launch { FirestoreService.deletePost(postToUndo.id) }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 92.dp)
                .imePadding(),
            snackbar = { DraftSavedSnackbar(it) }
        )
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
fun DraftsScreen(onBack: () -> Unit, onEditDraft: (String) -> Unit = {}) {
    var draftIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var draftIdPendingPublish by remember { mutableStateOf<String?>(null) }
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
        val drafts = AppCache.drafts
        if (drafts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("You don't have any saved drafts yet.", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 16.sp)
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
                                    Text(draft.book.author, color = PaginexWhite.copy(alpha = 0.7f), fontSize = 12.sp)
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
                                    onClick = { onEditDraft(draft.id) },
                                    border = BorderStroke(1.dp, PaginexNeonTeal.copy(alpha = 0.5f))
                                ) {
                                    Text("Edit", color = PaginexNeonTeal)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedButton(
                                    onClick = { draftIdPendingDelete = draft.id },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                                ) {
                                    Text("Delete", color = Color.Red)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { draftIdPendingPublish = draft.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
                                ) {
                                    Text("Publish", color = PaginexWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val pendingId = draftIdPendingDelete
    if (pendingId != null) {
        AlertDialog(
            onDismissRequest = { draftIdPendingDelete = null },
            containerColor = PaginexGalaxy,
            title = { Text("Delete draft?", color = PaginexWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete this draft?",
                    color = PaginexWhite.copy(alpha = 0.85f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        AppCache.drafts.removeAll { it.id == pendingId }
                        kotlinx.coroutines.MainScope().launch { FirestoreService.deleteDraft(pendingId) }
                        draftIdPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = PaginexWhite) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { draftIdPendingDelete = null },
                    border = BorderStroke(1.dp, PaginexGlassBorder)
                ) { Text("Cancel", color = PaginexWhite) }
            }
        )
    }

    val pendingPublishId = draftIdPendingPublish
    if (pendingPublishId != null) {
        AlertDialog(
            onDismissRequest = { draftIdPendingPublish = null },
            containerColor = PaginexGalaxy,
            title = { Text("Publish post?", color = PaginexWhite) },
            text = {
                Text("Are you sure you want to publish?", color = PaginexWhite.copy(alpha = 0.7f))
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toPublish = AppCache.drafts.find { it.id == pendingPublishId }
                        if (toPublish != null) {
                            AppCache.feedPosts.add(0, toPublish)
                            AppCache.drafts.remove(toPublish)
                            kotlinx.coroutines.MainScope().launch {
                                FirestoreService.createPost(toPublish)
                                FirestoreService.deleteDraft(toPublish.id)
                            }
                            onBack()
                        }
                        draftIdPendingPublish = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
                ) { Text("Publish", color = PaginexWhite) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { draftIdPendingPublish = null },
                    border = BorderStroke(1.dp, PaginexGlassBorder)
                ) { Text("Cancel", color = PaginexWhite) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val user = AppCache.currentUser
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
                    model = avatarModel(user.avatarUrl),
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
                    Text("@${user.username}", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account settings list
            Text(
                "Account settings",
                color = PaginexWhite.copy(alpha = 0.7f),
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
                Icon(Icons.Default.KeyboardArrowRight, null, tint = PaginexWhite.copy(alpha = 0.7f))
            }
            Divider(color = PaginexGlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))
            
            var showPasswordDialog by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPasswordDialog = true }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, null, tint = PaginexNeonPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Password", color = PaginexWhite, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, null, tint = PaginexWhite.copy(alpha = 0.7f))
            }
            Divider(color = PaginexGlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))

            if (showPasswordDialog) {
                var oldPassword by remember { mutableStateOf("") }
                var newPassword by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                AlertDialog(
                    onDismissRequest = { if (!isLoading) showPasswordDialog = false },
                    containerColor = PaginexSpace,
                    title = { Text("Change Password", color = PaginexWhite, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Current Password", color = PaginexWhite.copy(alpha = 0.7f)) },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PaginexNeonPurple,
                                    unfocusedBorderColor = PaginexGlassBorder,
                                    focusedTextColor = PaginexWhite,
                                    unfocusedTextColor = PaginexWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("New Password", color = PaginexWhite.copy(alpha = 0.7f)) },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PaginexNeonPurple,
                                    unfocusedBorderColor = PaginexGlassBorder,
                                    focusedTextColor = PaginexWhite,
                                    unfocusedTextColor = PaginexWhite
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (oldPassword.isBlank() || newPassword.isBlank()) {
                                    errorMessage = "Please fill in all fields"
                                    return@Button
                                }
                                if (newPassword.length < 6) {
                                    errorMessage = "New password must be at least 6 characters"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = AuthService.changePassword(oldPassword, newPassword)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        showPasswordDialog = false
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to change password"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PaginexNeonPurple)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = PaginexWhite, modifier = Modifier.size(16.dp))
                            else Text("Save", color = PaginexWhite, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPasswordDialog = false }, enabled = !isLoading) {
                            Text("Cancel", color = PaginexWhite.copy(alpha = 0.7f))
                        }
                    }
                )
            }
            
            var showDeleteAccountDialog by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteAccountDialog = true }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Delete account", color = Color.Red, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, null, tint = PaginexWhite.copy(alpha = 0.7f))
            }
            Divider(color = PaginexGlassBorder, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))

            if (showDeleteAccountDialog) {
                var password by remember { mutableStateOf("") }
                var isLoading by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()

                AlertDialog(
                    onDismissRequest = { if (!isLoading) showDeleteAccountDialog = false },
                    containerColor = PaginexSpace,
                    title = { Text("Delete Account", color = Color.Red, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("This action will anonymize your profile but keep your posts visible as 'Deleted Account'. Your personal library, followers, and booklists will be permanently deleted. This action cannot be undone.", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (errorMessage != null) {
                                Text(errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Enter Password to Confirm", color = PaginexWhite.copy(alpha = 0.7f)) },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Red,
                                    unfocusedBorderColor = PaginexGlassBorder,
                                    focusedTextColor = PaginexWhite,
                                    unfocusedTextColor = PaginexWhite
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (password.isBlank()) {
                                    errorMessage = "Please enter your password"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = AuthService.deleteAccount(password)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        showDeleteAccountDialog = false
                                        onLogout()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete account"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            else Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteAccountDialog = false }, enabled = !isLoading) {
                            Text("Cancel", color = PaginexWhite.copy(alpha = 0.7f))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Customisation list
            Text(
                "Customisation",
                color = PaginexWhite.copy(alpha = 0.7f),
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
        Text(label, fontSize = 11.sp, color = PaginexWhite.copy(alpha = 0.7f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onGalaxyBookClick: (String) -> Unit,
    onPostClick: (String) -> Unit,
    onListsClick: () -> Unit,
    onConstellationClick: () -> Unit
) {
    var user by remember { mutableStateOf<User?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var followStatusReady by remember(userId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showFollowers by remember { mutableStateOf(false) }
    var showFollowing by remember { mutableStateOf(false) }
    var showLibrary by remember { mutableStateOf(false) }
    
    val userFeed = remember { mutableStateListOf<Post>() }
    var lastUserPostDoc by remember { mutableStateOf<com.google.firebase.firestore.DocumentSnapshot?>(null) }
    var isUserFeedEndReached by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isScrollToEnd by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 3
        }
    }

    LaunchedEffect(isScrollToEnd) {
        if (isScrollToEnd && !isUserFeedEndReached && !isLoadingMore) {
            isLoadingMore = true
            val (newPosts, nextDoc) = FirestoreService.getUserPosts(userId, limit = 10, startAfterDoc = lastUserPostDoc)
            if (newPosts.isNotEmpty()) {
                val existingIds = userFeed.map { it.id }.toSet()
                userFeed.addAll(newPosts.filter { !existingIds.contains(it.id) })
                lastUserPostDoc = nextDoc
            }
            if (newPosts.size < 10) isUserFeedEndReached = true
            isLoadingMore = false
        }
    }
    
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
        followStatusReady = false
        val profile = FirestoreService.getUserProfile(userId)
        val viewer = AuthService.getUid()
        // Resolve follow before setting user so the first painted frame doesn’t briefly show wrong "Follow".
        isFollowing =
            viewer.isNotBlank() && userId != viewer && FirestoreService.isFollowing(viewer, userId)
        user = profile
        if (profile != null) {
            val (posts, lastDoc) = FirestoreService.getUserPosts(userId, limit = 10)
            userFeed.clear()
            userFeed.addAll(posts)
            lastUserPostDoc = lastDoc
            if (posts.size < 10) isUserFeedEndReached = true
        }
        followStatusReady = true
    }

    Scaffold(
        containerColor = PaginexSpace,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (user?.isActive != false) (user?.username ?: "") else "Deleted user",
                        color = PaginexWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
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
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).background(PaginexSpace),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserGalaxy(user = u, rotation = rotation, onBookClick = onGalaxyBookClick)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(u.fullName, color = PaginexWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("@${u.username}", color = PaginexWhite.copy(alpha = 0.7f), fontSize = 14.sp)

                    if (!u.isActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This account is no longer available.",
                            color = PaginexWhite.copy(alpha = 0.65f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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
                        ProfileStatItem("Books", AppCache.readingStatuses.count { it.userId == u.id }.toString(), null, onClick = { showLibrary = true })
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

                    val viewerId = AuthService.getUid()
                    if (viewerId.isNotBlank() && userId != viewerId && followStatusReady && u.isActive) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val viewer = AuthService.getUid()
                                    val target = userId
                                    val profileSnap = user ?: return@launch
                                    if (viewer.isBlank() || viewer == target) return@launch
                                    if (isFollowing) {
                                        if (!FirestoreService.unfollowUser(viewer, target)) return@launch
                                        isFollowing = false
                                        AppCache.currentUser = AppCache.currentUser.copy(
                                            followingCount = (AppCache.currentUser.followingCount - 1).coerceAtLeast(0)
                                        )
                                        user = profileSnap.copy(
                                            followersCount = (profileSnap.followersCount - 1).coerceAtLeast(0)
                                        )
                                    } else {
                                        if (!FirestoreService.followUser(viewer, target)) return@launch
                                        isFollowing = true
                                        AppCache.currentUser = AppCache.currentUser.copy(
                                            followingCount = AppCache.currentUser.followingCount + 1
                                        )
                                        user = profileSnap.copy(followersCount = profileSnap.followersCount + 1)
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
            }

            items(userFeed, key = { it.id }) { post ->
                BookPostCard(
                    post = post,
                    onUserClick = { /* Already here */ },
                    onBookClick = { onPostClick(post.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (isLoadingMore) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PaginexNeonPurple)
                    }
                }
            }
        }

        if (showFollowers) {
            UserListSheet("Followers", targetUserId = userId, onDismiss = { showFollowers = false })
        }
        if (showFollowing) {
            UserListSheet("Following", targetUserId = userId, onDismiss = { showFollowing = false })
        }
        if (showLibrary) {
            UserLibrarySheet(targetUserId = userId, onDismiss = { showLibrary = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    postId: String, 
    mode: String, 
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToUser: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    var postsVersion by remember { mutableIntStateOf(0) }
    val posts = remember(postId, mode, AppCache.feedPosts.size, postsVersion) {
        if (mode == "single") {
            AppCache.feedPosts.filter { it.id == postId }
        } else if (mode == "saved_book") {
            // Find the book from the clicked post ID in SavedPostsScreen
            val bookId = AppCache.feedPosts.find { it.id == postId }?.book?.id
            AppCache.feedPosts.filter { it.book.id == bookId && it.isSaved }
        } else {
            emptyList()
        }
    }
    
    val title = if (mode == "single") "Posts" else "Saved Posts"
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = PaginexSpace,
        topBar = {
            TopAppBar(
                title = { Text(title, color = PaginexWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = PaginexWhite) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No posts found.", color = PaginexWhite.copy(alpha = 0.7f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    BookPostCard(
                        post = post,
                        onBookClick = { onNavigateToDetail(post.id) },
                        onUserClick = onNavigateToUser,
                        onEditClick = { onNavigateToEdit(post.id) },
                        onDeleteClick = { postToDelete ->
                            val now = System.currentTimeMillis()
                            val diff = now - postToDelete.createdAt
                            if (diff > 5 * 60 * 1000) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Posts can only be deleted within 5 minutes of creation.")
                                }
                            } else {
                                AppCache.feedPosts.removeAll { it.id == postToDelete.id }
                                postsVersion++
                                kotlinx.coroutines.MainScope().launch {
                                    FirestoreService.deletePost(postToDelete.id)
                                }
                            }
                        },
                        onLikeClick = {
                            val index = AppCache.feedPosts.indexOfFirst { it.id == post.id }
                            val newIsLiked = if (index != -1) !AppCache.feedPosts[index].isLiked else !post.isLiked
                            if (index != -1) {
                                val currentPost = AppCache.feedPosts[index]
                                val newLikesCount = if (newIsLiked) currentPost.likesCount + 1 else (currentPost.likesCount - 1).coerceAtLeast(0)
                                AppCache.feedPosts[index] = currentPost.copy(
                                    isLiked = newIsLiked,
                                    likesCount = newLikesCount
                                )
                                postsVersion++
                            }
                            kotlinx.coroutines.MainScope().launch {
                                FirestoreService.toggleLike(post.id, AuthService.getUid(), newIsLiked)
                            }
                        },
                        onSaveClick = {
                            val index = AppCache.feedPosts.indexOfFirst { it.id == post.id }
                            val newIsSaved = if (index != -1) !AppCache.feedPosts[index].isSaved else !post.isSaved
                            if (index != -1) {
                                AppCache.feedPosts[index] = AppCache.feedPosts[index].copy(isSaved = newIsSaved)
                                postsVersion++
                            }
                            kotlinx.coroutines.MainScope().launch {
                                FirestoreService.toggleSave(post.id, AuthService.getUid(), newIsSaved)
                            }
                        },
                        onCommentAdded = {
                            val index = AppCache.feedPosts.indexOfFirst { it.id == post.id }
                            if (index != -1) {
                                val currentPost = AppCache.feedPosts[index]
                                AppCache.feedPosts[index] = currentPost.copy(
                                    commentsCount = currentPost.commentsCount + 1
                                )
                                postsVersion++
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
