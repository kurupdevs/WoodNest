package com.kurupdevs.woodnest.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.EmptyState
import com.kurupdevs.woodnest.ui.components.ProductCard
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.ui.theme.WarmGrey
import com.kurupdevs.woodnest.util.inr
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(navController: NavController, repo: WoodNestRepository) {
    val favorites by repo.favoritesFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val drops by repo.priceDrops().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { WoodNestTopBar(title = "Favorites") },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (drops.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SaleRed.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "Price drops on your favorites",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SaleRed
                        )
                        Spacer(Modifier.height(6.dp))
                        drops.forEach { drop ->
                            Text(
                                text = "${drop.product.name} dropped from ${drop.oldPrice.inr()} to ${drop.newPrice.inr()}",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = SaleRed,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No favorites yet",
                        message = "Tap the heart on any product to save it here.",
                        actionLabel = "Explore products",
                        onAction = { navController.navigate(Routes.HOME) }
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favorites, key = { it.second.id }) { (favorite, product) ->
                        Column {
                            ProductCard(
                                product = product,
                                isFav = true,
                                onClick = { navController.navigate(Routes.productRoute(product.id)) },
                                onFavClick = { scope.launch { repo.toggleFavorite(product.id) } }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Price alert",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = WarmGrey
                                )
                                Switch(
                                    checked = favorite.alert,
                                    onCheckedChange = { on ->
                                        scope.launch { repo.setAlert(product.id, on) }
                                    },
                                    colors = SwitchDefaults.colors(checkedTrackColor = CoffeeBrown)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
