package com.kurupdevs.woodnest.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.EmptyState
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.AmberGold
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.WarmGrey
import com.kurupdevs.woodnest.util.toDateString

@Composable
fun NotificationsScreen(navController: NavController, repo: WoodNestRepository) {
    val notifications by repo.notificationsFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    LaunchedEffect(Unit) {
        repo.markAllRead()
    }

    Scaffold(
        topBar = { WoodNestTopBar(title = "Notifications", onBack = { navController.popBackStack() }) },
        containerColor = Color.White
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "No notifications",
                    message = "Price drops, order updates and offers will show up here.",
                    actionLabel = "Browse products",
                    onAction = { navController.navigate(Routes.HOME) }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { n ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            if (!n.read) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp, end = 10.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AmberGold)
                                )
                            } else {
                                Spacer(Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    n.title,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = CoffeeBrown
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(n.body, fontFamily = Inter, fontSize = 13.sp, color = WarmGrey)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    n.createdAt.toDateString(),
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    color = WarmGrey
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
