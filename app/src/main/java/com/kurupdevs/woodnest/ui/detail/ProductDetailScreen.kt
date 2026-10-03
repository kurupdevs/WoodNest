package com.kurupdevs.woodnest.ui.detail

import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.billing.PricingEngine
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.EmptyState
import com.kurupdevs.woodnest.ui.components.PriceText
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.RatingStars
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.components.productPainter
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.AmberGold
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.ui.theme.SuccessGreen
import com.kurupdevs.woodnest.util.DeliveryEstimate
import com.kurupdevs.woodnest.util.DeliveryEstimator
import com.kurupdevs.woodnest.util.FitChecker
import com.kurupdevs.woodnest.util.FitVerdict
import com.kurupdevs.woodnest.util.inr
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController, repo: WoodNestRepository, productId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val product by repo.productFlow(productId).collectAsStateWithLifecycle(initialValue = null)
    val favIds by repo.favoriteIdsFlow().collectAsStateWithLifecycle(initialValue = emptySet())
    val reviews by repo.reviewsFlow(productId).collectAsStateWithLifecycle(initialValue = emptyList())

    var pin by remember { mutableStateOf("") }
    var estimate by remember { mutableStateOf<DeliveryEstimate?>(null) }
    var doorW by remember { mutableStateOf("") }
    var stairW by remember { mutableStateOf("") }
    var verdict by remember { mutableStateOf<FitVerdict?>(null) }
    var mode by remember { mutableStateOf("buy") }

    var reviewStars by remember { mutableStateOf(5) }
    var reviewerName by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }
    var reviewPhoto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        reviewerName = repo.getProfile().name
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        reviewPhoto = uri?.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WoodNestTopBar(
            title = "Product",
            onBack = { navController.popBackStack() },
            actions = {}
        )

        val p = product
        if (p == null) {
            EmptyState(
                title = "Product not found",
                message = "This product may have been removed.",
                actionLabel = "Go back",
                onAction = { navController.popBackStack() }
            )
            return
        }

        val monthly = PricingEngine.monthlyFor(p.price)
        val isFav = favIds.contains(p.id)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Image(
                        painter = productPainter(p.imageName),
                        contentDescription = p.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(18.dp))
                    )
                    IconButton(
                        onClick = { scope.launch { repo.toggleFavorite(p.id) } },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.White, RoundedCornerShape(50))
                    ) {
                        Icon(
                            if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) SaleRed else CoffeeBrown
                        )
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(p.name, fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(rating = p.rating)
                        Spacer(Modifier.size(6.dp))
                        Text("${p.rating} (${p.reviewCount} reviews)", fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    PriceText(price = p.price, large = true)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "EMI from ${(p.price / 12).inr()}/mo × 12",
                        fontFamily = Inter,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            item {
                SectionTitle(text = "Delivery", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pin = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter pincode", fontFamily = Inter) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    PrimaryButton(text = "Check", onClick = {
                        if (pin.length == 6) estimate = DeliveryEstimator.estimate(pin)
                    })
                }
                when (val e = estimate) {
                    is DeliveryEstimate.Serviceable -> Text(
                        e.label,
                        fontFamily = Inter,
                        color = SuccessGreen,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    is DeliveryEstimate.Unserviceable -> Text(
                        "Not serviceable in this pincode",
                        fontFamily = Inter,
                        color = SaleRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    null -> {}
                }
            }

            item {
                SectionTitle(text = "Size", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                Text(
                    "L ${p.widthCm} × W ${p.depthCm} × H ${p.heightCm} cm",
                    fontFamily = Inter,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Cream)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Will it fit?", fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = doorW,
                                onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) doorW = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Door width (cm)", fontFamily = Inter, style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = stairW,
                                onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) stairW = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Staircase (cm)", fontFamily = Inter, style = MaterialTheme.typography.labelSmall) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(text = "Check fit", onClick = {
                            verdict = FitChecker.check(
                                doorW.toIntOrNull() ?: 0,
                                stairW.toIntOrNull() ?: 0,
                                p.packW, p.packH, p.packD
                            )
                        })
                        verdict?.let { v ->
                            Spacer(Modifier.height(8.dp))
                            Text(v.title, fontFamily = Inter, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = CoffeeBrown)
                            Text(v.desc, fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Fit Guarantee: if our checker says it fits and it doesn't, return is free.",
                            fontFamily = Inter,
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == "buy",
                        onClick = { mode = "buy" },
                        label = { Text("Buy", fontFamily = Inter) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = mode == "rent",
                        onClick = { mode = "rent" },
                        label = { Text("Rent · 12 mo", fontFamily = Inter) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown, selectedLabelColor = Color.White)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    if (mode == "rent") {
                        Text(
                            "${monthly.inr()}/mo × 12 months",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = CoffeeBrown
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Buyout anytime: remaining − 10%",
                            fontFamily = Inter,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(10.dp))
                        PrimaryButton(
                            text = "Add rental to cart",
                            onClick = {
                                scope.launch {
                                    repo.addToCart(p.id, 1, true)
                                    Toast.makeText(context, "Rental added to cart", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PrimaryButton(
                                text = "Add to Cart",
                                onClick = {
                                    scope.launch {
                                        repo.addToCart(p.id, 1, false)
                                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PrimaryButton(
                                text = "Buy Now",
                                onClick = {
                                    scope.launch {
                                        repo.addToCart(p.id, 1, false)
                                        navController.navigate(Routes.CART)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle(text = "Details", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(p.description, fontFamily = Inter, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Material: ${p.material}", fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            item {
                SectionTitle(text = "Reviews", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            }
            if (reviews.isEmpty()) {
                item {
                    Text(
                        "No reviews yet. Be the first to review.",
                        fontFamily = Inter,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(reviews.sortedByDescending { it.createdAt }, key = { it.id }) { review ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingStars(rating = review.stars.toFloat())
                                Spacer(Modifier.size(8.dp))
                                Text(review.userName, fontFamily = Inter, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                if (review.verified) {
                                    Spacer(Modifier.size(6.dp))
                                    Icon(Icons.Filled.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.size(2.dp))
                                    Text("Verified buyer", fontFamily = Inter, style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                                }
                            }
                            if (review.text.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(review.text, fontFamily = Inter, style = MaterialTheme.typography.bodySmall)
                            }
                            if (!review.photoUri.isNullOrBlank()) {
                                Spacer(Modifier.height(8.dp))
                                val uriString = review.photoUri
                                AndroidView(
                                    factory = { ctx ->
                                        ImageView(ctx).apply {
                                            scaleType = ImageView.ScaleType.CENTER_CROP
                                            adjustViewBounds = true
                                        }
                                    },
                                    update = { view -> view.setImageURI(Uri.parse(uriString)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Cream)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Write a review", fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { star ->
                                Icon(
                                    if (star <= reviewStars) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = AmberGold,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable { reviewStars = star }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reviewerName,
                            onValueChange = { reviewerName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your name", fontFamily = Inter) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your review", fontFamily = Inter) },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { picker.launch("image/*") }) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = "Add photo", tint = CoffeeBrown)
                            }
                            Text(
                                if (reviewPhoto != null) "Photo attached" else "Add a photo (optional)",
                                fontFamily = Inter,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        PrimaryButton(
                            text = "Submit review",
                            onClick = {
                                scope.launch {
                                    repo.addReview(productId, reviewerName.ifBlank { "Anonymous" }, reviewStars, reviewText, reviewPhoto)
                                    Toast.makeText(context, "Review submitted", Toast.LENGTH_SHORT).show()
                                    reviewText = ""
                                    reviewPhoto = null
                                    reviewStars = 5
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
