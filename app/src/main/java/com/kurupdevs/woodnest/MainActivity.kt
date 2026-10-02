package com.kurupdevs.woodnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.nav.WoodNestNavGraph
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.ui.theme.WoodNestTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WoodNestTheme {
                val app = application as WoodNestApp
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val route = backStack?.destination?.route
                val tabs = listOf(
                    Triple(Routes.HOME, "Home", Icons.Filled.Home),
                    Triple(Routes.REELS, "Reels", Icons.Filled.PlayArrow),
                    Triple(Routes.FAVORITES, "Favorites", Icons.Filled.Favorite),
                    Triple(Routes.CART, "Cart", Icons.Filled.ShoppingCart),
                    Triple(Routes.ORDERS, "Orders", Icons.Filled.ReceiptLong),
                    Triple(Routes.PROFILE, "Profile", Icons.Filled.Person)
                )
                val showBar = tabs.any { it.first == route }
                val cartCount by app.repo.cartLines().collectAsStateWithLifecycle(initial = emptyList())
                val drops by app.repo.dropCount().collectAsStateWithLifecycle(initial = 0)
                Scaffold(
                    bottomBar = {
                        if (showBar) {
                            NavigationBar {
                                tabs.forEach { (r, label, icon) ->
                                    val badge = when (r) {
                                        Routes.CART -> cartCount.sumOf { it.item.qty }
                                        Routes.FAVORITES -> drops
                                        else -> 0
                                    }
                                    NavigationBarItem(
                                        selected = route == r,
                                        onClick = {
                                            navController.navigate(r) {
                                                popUpTo(Routes.HOME) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (badge > 0) Badge(containerColor = SaleRed) {
                                                        Text(badge.toString())
                                                    }
                                                }
                                            ) { Icon(icon, label) }
                                        },
                                        label = { Text(label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = CoffeeBrown,
                                            selectedTextColor = CoffeeBrown,
                                            indicatorColor = Cream
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    Box(Modifier.padding(padding)) {
                        WoodNestNavGraph(navController, app.repo)
                    }
                }
            }
        }
    }
}
