package com.kurupdevs.woodnest.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.repo.WoodNestRepository.UserProfile
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.AmberGold
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
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

@Composable
fun ProfileScreen(navController: NavController, repo: WoodNestRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val loyalty by repo.loyaltyFlow().collectAsStateWithLifecycle(initialValue = null)
    val unread by repo.unreadCount().collectAsStateWithLifecycle(initialValue = 0)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val profile = repo.getProfile()
        name = profile.name
        phone = profile.phone
    }

    val menu: List<Triple<ImageVector, String, String>> = listOf(
        Triple(Icons.Outlined.ShoppingBag, "My Orders", Routes.ORDERS),
        Triple(Icons.Outlined.LocationOn, "Addresses", Routes.ADDRESSES),
        Triple(Icons.Outlined.Notifications, "Notifications", Routes.NOTIFICATIONS),
        Triple(Icons.Outlined.CalendarMonth, "My Consults", Routes.CONSULTS),
        Triple(Icons.Outlined.CardGiftcard, "Refer & Earn", Routes.REFERRAL),
        Triple(Icons.Outlined.Stars, "WoodNest Circle", Routes.LOYALTY),
        Triple(Icons.Outlined.LocalOffer, "Offers", Routes.OFFERS)
    )

    Scaffold(
        topBar = { WoodNestTopBar(title = "Profile") },
        containerColor = Color.White
    ) { padding ->
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
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle(text = "Your details")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name", fontFamily = Inter) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                            label = { Text("Phone", fontFamily = Inter) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        PrimaryButton(
                            text = "Save",
                            onClick = {
                                scope.launch {
                                    repo.saveProfile(UserProfile(name = name, phone = phone))
                                    Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            loyalty?.let { account ->
                if (account.joined) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CoffeeBrown),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth().clickable { navController.navigate(Routes.LOYALTY) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "WoodNest Circle",
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        tierName(account.points),
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AmberGold
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "${account.points} points",
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(8.dp))
                                nextTierAt(account.points)?.let { target ->
                                    LinearProgressIndicator(
                                        progress = { account.points.toFloat() / target },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = AmberGold,
                                        trackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${target - account.points} pts to ${tierName(target)}",
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
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
                    Column(Modifier.padding(vertical = 6.dp)) {
                        menu.forEach { (icon, title, route) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate(route) }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (route == Routes.NOTIFICATIONS && unread > 0) {
                                            Badge(containerColor = AmberGold) { Text("$unread") }
                                        }
                                    }
                                ) {
                                    Icon(icon, contentDescription = title, tint = CoffeeBrown)
                                }
                                Text(
                                    text = title,
                                    fontFamily = Inter,
                                    fontSize = 15.sp,
                                    modifier = Modifier.weight(1f).padding(start = 14.dp)
                                )
                                Icon(
                                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = WarmGrey
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "WoodNest 1.0",
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = WarmGrey,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }
    }
}
