package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.MovieRepository
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.ImmCardSurface
import com.example.ui.theme.ImmDarkBackground
import com.example.ui.theme.ImmNetflixRed
import com.example.ui.theme.ImmTextMuted
import com.example.ui.theme.ImmTextPrimary
import com.example.ui.theme.ImmTextSecondary
import com.example.ui.viewmodel.DetailViewModel
import com.example.ui.viewmodel.DetailViewModelFactory
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.HomeViewModelFactory
import com.example.ui.viewmodel.SearchViewModel
import com.example.ui.viewmodel.SearchViewModelFactory
import com.example.ui.viewmodel.WatchlistViewModel
import com.example.ui.viewmodel.WatchlistViewModelFactory

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object MyList : Screen("my_list", "My List", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    object Detail : Screen("detail/{movieId}", "Detail", Icons.Filled.Home, Icons.Outlined.Home) {
        fun createRoute(movieId: Int) = "detail/$movieId"
    }

    object Player : Screen("player/{movieId}/{server}", "Player", Icons.Filled.Home, Icons.Outlined.Home) {
        fun createRoute(movieId: Int, server: String) = "player/$movieId/$server"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.MyList,
    Screen.Settings
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val repository = remember { MovieRepository.getInstance(context) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation on full player screen
    val shouldShowBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        containerColor = ImmDarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = ImmDarkBackground.copy(alpha = 0.98f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ImmNetflixRed,
                                selectedTextColor = ImmNetflixRed,
                                indicatorColor = ImmNetflixRed.copy(alpha = 0.15f),
                                unselectedIconColor = ImmTextMuted,
                                unselectedTextColor = ImmTextMuted
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(repository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.Detail.createRoute(movie.id))
                    },
                    onWatchNow = { movie ->
                        navController.navigate(Screen.Detail.createRoute(movie.id))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Search Screen
            composable(Screen.Search.route) {
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModelFactory(repository)
                )
                SearchScreen(
                    viewModel = searchViewModel,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.Detail.createRoute(movie.id))
                    }
                )
            }

            // My List / Watchlist Screen
            composable(Screen.MyList.route) {
                val watchlistViewModel: WatchlistViewModel = viewModel(
                    factory = WatchlistViewModelFactory(repository)
                )
                WatchlistScreen(
                    viewModel = watchlistViewModel,
                    onMovieClick = { movie ->
                        navController.navigate(Screen.Detail.createRoute(movie.id))
                    },
                    onBrowseTrending = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(repository = repository)
            }

            // Detail Screen
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                val detailViewModel: DetailViewModel = viewModel(
                    key = "detail_$movieId",
                    factory = DetailViewModelFactory(movieId, repository)
                )

                DetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    onMovieClick = { movie ->
                        navController.navigate(Screen.Detail.createRoute(movie.id))
                    },
                    onOpenFullscreenPlayer = { id, server ->
                        navController.navigate(Screen.Player.createRoute(id, server))
                    }
                )
            }

            // Fullscreen Player Screen
            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType },
                    navArgument("server") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                val server = backStackEntry.arguments?.getString("server") ?: "SERVER_1"

                PlayerScreen(
                    movieId = movieId,
                    initialServerName = server,
                    repository = repository,
                    onClose = { navController.popBackStack() }
                )
            }
        }
    }
}
