package com.kurupdevs.woodnest.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.theme.AmberGold
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SuccessGreen
import com.kurupdevs.woodnest.ui.theme.WarmGrey
import kotlinx.coroutines.launch

private fun tierName(points: Int): String = when {
    points >= 5000 -> "Gold"
    points >= 1000 -> "Silver"
    else -> "Bronze"
}

private fun nextTierAt(points: Int): Int? = when {
    points < 1000 -> 1000
    points < 5000 -> 5000
    else -> null
}

private val JOIN_BENEFITS = listOf(
    "Earn 1 point for every ₹100 you spend",
    "100 pts = ₹100 off at checkout",
    "Silver tier: 1.25x points earning",
    "Gold tier: 1.5x points earning + free assembly"
)

@Composable
fun LoyaltyScreen(navController: NavController, repo: WoodNestRepository) {
    val loyalty by repo.loyaltyFlow().collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { WoodNestTopBar(title = "WoodNest Circle", onBack = { navController.popBackStack() }) },
        containerColor = Color.White
    ) { padding ->
        val account = loyalty
        if (account == null || !account.joined) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Join WoodNest Circle",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = CoffeeBrown,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            JOIN_BENEFITS.forEach { benefit ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.padding(end = 10.dp)
                                    )
                                    Text(benefit, fontFamily = Inter, fontSize = 14.sp)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Join Circle",
                                onClick = { scope.launch { repo.joinCircle() } }
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CoffeeBrown),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                tierName(account.points),
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AmberGold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${account.points}",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 44.sp,
                                color = Color.White
                            )
                            Text(
                                "points",
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.height(14.dp))
                            nextTierAt(account.points)?.let { target ->
                                LinearProgressIndicator(
                                    progress = { account.points.toFloat() / target },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = AmberGold,
                                    trackColor = Color.White.copy(alpha = 0.2f)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "${target - account.points} points to ${tierName(target)}",
                                    fontFamily = Inter,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            } ?: Text(
                                "Top tier reached",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle(text = "Your stats")
                            StatRow("Points earned all time", "${account.totalEarned}")
                            StatRow("Conversion", "100 pts = ₹100 off at checkout")
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionTitle(text = "Tier perks")
                            PerkRow("Bronze", "1x points earning")
                            PerkRow("Silver (1,000+ pts)", "1.25x points earning")
                            PerkRow("Gold (5,000+ pts)", "1.5x points earning + free assembly")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontFamily = Inter, fontSize = 14.sp, color = WarmGrey)
        Text(value, fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CoffeeBrown)
    }
}

@Composable
private fun PerkRow(tier: String, perk: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.padding(end = 10.dp))
        Column {
            Text(tier, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(perk, fontFamily = Inter, fontSize = 13.sp, color = WarmGrey)
        }
    }
}
