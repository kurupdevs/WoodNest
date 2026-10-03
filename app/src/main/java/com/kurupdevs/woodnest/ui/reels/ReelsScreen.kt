package com.kurupdevs.woodnest.ui.reels

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.productPainter
import com.kurupdevs.woodnest.ui.theme.Inter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class Reel(
    val id: String,
    val title: String,
    val imageName: String,
    val productIds: List<String>
)

val reels = listOf(
    Reel("reel1", "₹40k Living Room Glow-Up", "hero", listOf("oslo-sofa", "nordic-coffee-table", "arc-floor-lamp")),
    Reel("reel2", "Sheesham Bedroom Retreat", "bedroom", listOf("sheesham-bed", "drift-nightstand")),
    Reel("reel3", "Work-From-Home Corner", "dining", listOf("focus-desk", "atlas-bookshelf", "ember-accent-chair")),
    Reel("reel4", "Small Space, Big Style", "sofa", listOf("ember-accent-chair", "halo-arch-mirror")),
    Reel("reel5", "Dining Done Right", "dining_table", listOf("haven-dining-table", "marco-counter-stool")),
    Reel("reel6", "Corners That Glow", "floor_lamp", listOf("arc-floor-lamp", "nordic-coffee-table"))
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsScreen(navController: NavController, repo: WoodNestRepository) {
    val likedIds by repo.reelLikeIdsFlow().collectAsStateWithLifecycle(initialValue = emptySet())
    val pagerState = rememberPagerState(pageCount = { reels.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val reel = reels[page]
            ReelPage(
                reel = reel,
                liked = likedIds.contains(reel.id),
                repo = repo,
                scope = scope,
                onToggleLike = { scope.launch { repo.toggleReelLike(reel.id) } },
                onAddToCart = { pid ->
                    scope.launch {
                        repo.addToCart(pid, 1, false)
                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reels.indices.forEach { i ->
                val active = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (active) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (active) Color.White else Color.White.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReelPage(
    reel: Reel,
    liked: Boolean,
    repo: WoodNestRepository,
    scope: CoroutineScope,
    onToggleLike: () -> Unit,
    onAddToCart: (String) -> Unit
) {
    var names by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(reel) {
        names = reel.productIds.associateWith { id -> repo.productById(id)?.name ?: "Product" }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = productPainter(reel.imageName),
            contentDescription = reel.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(
                    text = reel.title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (pid in reel.productIds) {
                        AssistChip(
                            onClick = { onAddToCart(pid) },
                            label = {
                                Text(
                                    names[pid] ?: "Product",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.18f)
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
        IconButton(
            onClick = onToggleLike,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 150.dp)
        ) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (liked) Color(0xFFE5484D) else Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
