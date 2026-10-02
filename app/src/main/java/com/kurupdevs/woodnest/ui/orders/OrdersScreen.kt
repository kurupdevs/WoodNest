package com.kurupdevs.woodnest.ui.orders

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.db.Order
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.EmptyState
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.AmberGold
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.ui.theme.SuccessGreen
import com.kurupdevs.woodnest.ui.theme.WarmGrey
import com.kurupdevs.woodnest.util.inr
import com.kurupdevs.woodnest.util.toDateString
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(navController: NavController, repo: WoodNestRepository) {
    val orders by repo.ordersFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var cancelTarget by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        topBar = { WoodNestTopBar(title = "My Orders") },
        containerColor = Color.White
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No orders yet",
                    message = "Your orders will appear here once you place one.",
                    actionLabel = "Start shopping",
                    onAction = { navController.navigate(Routes.HOME) }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    OrderRow(
                        order = order,
                        onClick = { navController.navigate(Routes.orderRoute(order.id)) },
                        onCancel = { cancelTarget = order }
                    )
                }
            }
        }
    }

    cancelTarget?.let { order ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text("Cancel order?", fontFamily = Inter, fontWeight = FontWeight.Bold) },
            text = { Text("This will cancel order #${order.id.takeLast(8)}.", fontFamily = Inter) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repo.cancelOrder(order.id) }
                    cancelTarget = null
                }) { Text("Yes, cancel", color = SaleRed, fontFamily = Inter) }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) { Text("Keep order", fontFamily = Inter) }
            }
        )
    }
}

@Composable
private fun OrderRow(order: Order, onClick: () -> Unit, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${order.id.takeLast(8)}",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CoffeeBrown
                )
                StatusChip(status = order.status)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = order.placedAt.toDateString(),
                fontFamily = Inter,
                fontSize = 13.sp,
                color = WarmGrey
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.grandTotal.inr(),
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = CoffeeBrown
                )
                if (order.status == "PLACED") {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = SaleRed, fontFamily = Inter)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, label) = when (status) {
        "DELIVERED" -> SuccessGreen to "Delivered"
        "CANCELLED" -> SaleRed to "Cancelled"
        "OUT_FOR_DELIVERY" -> AmberGold to "Out for delivery"
        "SHIPPED" -> CoffeeBrown to "Shipped"
        "PACKED" -> CoffeeBrown to "Packed"
        else -> CoffeeBrown to "Placed"
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg.copy(alpha = 0.14f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontFamily = Inter,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = bg
        )
    }
}
