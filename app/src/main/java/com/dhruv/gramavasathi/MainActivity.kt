package com.dhruv.gramavasathi
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dhruv.gramavasathi.ui.theme.GramaVasathiTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GramaVasathiTheme {
                MainScreen()
            }
        }
    }
}
fun fetchFarmStays(onResult: (List<FarmStay>) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("farm_stay")
        .get()
        .addOnSuccessListener { result ->
            val list = result.mapNotNull { doc ->
                val stay = doc.toObject(FarmStay::class.java)
                stay?.copy(
                    id = doc.id,
                    hostName = doc.getString("hostName") ?: "",
                    hostDescription = doc.getString("hostDescription") ?: ""
                )
            }
            onResult(list)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Explore : Screen("explore", "Explore", Icons.Default.Explore)
    object HostTraining : Screen("host_training", "Host Training", Icons.Default.ModelTraining)
    object CulturalGuide : Screen("cultural_guide", "Cultural Guide", Icons.Default.MenuBook)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var farmStays by remember { mutableStateOf<List<FarmStay>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fetchFarmStays { result ->
            farmStays = result
            isLoading = false
        }
    }

    val navItems = listOf(
        Screen.Explore,
        Screen.HostTraining,
        Screen.CulturalGuide
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            // Hide bottom bar on detail screen
            if (currentDestination?.route?.startsWith("detail_screen") != true) {
                NavigationBar {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Explore.route) {
                ExploreScreen(navController, farmStays, isLoading)
            }
            composable(Screen.HostTraining.route) {
                HostTrainingScreen(onExploreClick = {
                    navController.navigate(Screen.Explore.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(Screen.CulturalGuide.route) {
                CulturalGuideScreen()
            }
            composable(
                route = "detail_screen/{index}",
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250))
                },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
                }
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
                if (index < farmStays.size) {
                    FarmStayDetailScreen(farmStays[index])
                }
            }
        }
    }
}

@Composable
fun SimpleScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}
