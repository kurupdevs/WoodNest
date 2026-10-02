package com.kurupdevs.woodnest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.billing.PricingEngine
import com.kurupdevs.woodnest.data.db.Product
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.OfferTicker
import com.kurupdevs.woodnest.ui.components.ProductCard
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.TickerOffer
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, repo: WoodNestRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allProducts by repo.productsFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val favIds by repo.favoriteIdsFlow().collectAsStateWithLifecycle(initialValue = emptySet())
    val recent by repo.recentFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val dropCount by repo.dropCount().collectAsStateWithLifecycle(initialValue = 0)

    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var region by remember { mutableStateOf<String?>(null) }
    var sort by remember { mutableStateOf("Featured") }
    var displayed by remember { mutableStateOf<List<Product>>(emptyList()) }

    val nameById = remember(allProducts) { allProducts.associate { it.id to it.name } }

    LaunchedEffect(query, region, allProducts) {
        val base: List<Product> = when {
            region != null -> repo.productsByRegion(region!!).first()
            query.isBlank() -> allProducts
            else -> repo.searchProducts(query).first()
        }
        displayed = when (sort) {
            "Price: Low to High" -> base.sortedBy { it.price }
            "Price: High to Low" -> base.sortedByDescending { it.price }
            "Rating" -> base.sortedByDescending { it.rating }
            else -> base
        }
    }

    val tickerOffers = listOf(
        TickerOffer(id = "WELCOME10", text = "10% OFF up to ₹2,000"),
        TickerOffer(id = "FLAT500", text = "₹500 off above ₹9,999"),
        TickerOffer(id = "FREESHIP", text = "FREE delivery"),
        TickerOffer(id = "FESTIVE15", text = "15% off above ₹24,999")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WoodNestTopBar(
            title = "WoodNest",
            actions = {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = CoffeeBrown)
                }
                IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                    BadgedBox(
                        badge = {
                            if (dropCount > 0) Badge { Text(dropCount.toString()) }
                        }
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = CoffeeBrown)
                    }
                }
            }
        )
        OfferTicker(offers = tickerOffers, onOfferTap = { navController.navigate(Routes.OFFERS) })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (showSearch) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search sofas, beds, tables...", fontFamily = Inter) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                SectionTitle(text = "Shop by Region", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RegionCard(
                        title = "North",
                        subtitle = "Sheesham Classics",
                        selected = region == "north",
                        modifier = Modifier.weight(1f),
                        onClick = { region = if (region == "north") null else "north" }
                    )
                    RegionCard(
                        title = "South",
                        subtitle = "Light & Modular",
                        selected = region == "south",
                        modifier = Modifier.weight(1f),
                        onClick = { region = if (region == "south") null else "south" }
                    )
                }
                if (region != null) {
                    FilterChip(
                        selected = true,
                        onClick = { region = null },
                        label = { Text("All", fontFamily = Inter) },
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown, selectedLabelColor = Color.White)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                SectionTitle(text = "Shop the Look", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
            items(PricingEngine.BUNDLES.values.toList(), key = { it.id }) { bundle ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Cream)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bundle.name, fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${bundle.discountPct}% OFF",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                color = CoffeeBrown,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(bundle.blurb, fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(Modifier.height(6.dp))
                        val itemNames = bundle.items.keys.mapNotNull { nameById[it] }
                        Text(
                            itemNames.joinToString(" + ").ifBlank { "${bundle.items.size} pieces" },
                            fontFamily = Inter,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(10.dp))
                        TextButton(
                            onClick = {
                                scope.launch {
                                    bundle.items.forEach { (pid, qty) -> repo.addToCart(pid, qty, false) }
                                    android.widget.Toast.makeText(context, "Bundle added to cart", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Add all to cart", fontFamily = Inter, fontWeight = FontWeight.SemiBold, color = CoffeeBrown)
                        }
                    }
                }
            }

            if (recent.isNotEmpty()) {
                item {
                    SectionTitle(text = "Recently viewed", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recent, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isFav = favIds.contains(product.id),
                                onFavClick = { scope.launch { repo.toggleFavorite(product.id) } },
                                onClick = {
                                    scope.launch { repo.recordView(product.id) }
                                    navController.navigate(Routes.productRoute(product.id))
                                },
                                modifier = Modifier.size(width = 150.dp, height = 210.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(text = if (region != null || query.isNotBlank()) "Results" else "All Furniture")
                    SortDropdown(sort = sort, onSort = { sort = it })
                }
            }

            items(displayed.chunked(2)) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { product ->
                        Box(Modifier.weight(1f)) {
                            ProductCard(
                                product = product,
                                isFav = favIds.contains(product.id),
                                onFavClick = { scope.launch { repo.toggleFavorite(product.id) } },
                                onClick = {
                                    scope.launch { repo.recordView(product.id) }
                                    navController.navigate(Routes.productRoute(product.id))
                                }
                            )
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun RegionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(92.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CoffeeBrown else Cream
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) Color.White else CoffeeBrown
            )
            Text(
                subtitle,
                fontFamily = Inter,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White.copy(alpha = 0.85f) else Color.Gray
            )
        }
    }
}

@Composable
private fun SortDropdown(sort: String, onSort: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Featured", "Price: Low to High", "Price: High to Low", "Rating")
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(sort, fontFamily = Inter, color = CoffeeBrown, style = MaterialTheme.typography.bodySmall)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = Inter, style = MaterialTheme.typography.bodySmall) },
                    onClick = { onSort(option); expanded = false }
                )
            }
        }
    }
}
