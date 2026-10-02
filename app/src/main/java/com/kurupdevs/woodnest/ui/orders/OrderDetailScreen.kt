package com.kurupdevs.woodnest.ui.orders

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.db.Order
import com.kurupdevs.woodnest.data.db.OrderItem
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.theme.AmberGold
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.CoffeeLight
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.ui.theme.SuccessGreen
import com.kurupdevs.woodnest.ui.theme.WarmGrey
import com.kurupdevs.woodnest.util.inr
import com.kurupdevs.woodnest.util.toDateString
import kotlinx.coroutines.launch

private val STEPS = listOf("Placed", "Packed", "Shipped", "Out for delivery", "Delivered")

private fun statusIndex(status: String): Int = when (status) {
    "PLACED" -> 0
    "PACKED" -> 1
    "SHIPPED" -> 2
    "OUT_FOR_DELIVERY" -> 3
    "DELIVERED" -> 4
    else -> -1
}

@Composable
fun OrderDetailScreen(navController: NavController, repo: WoodNestRepository, orderId: String) {
    val data by repo.orderWithItems(orderId).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = { WoodNestTopBar(title = "Order details", onBack = { navController.popBackStack() }) },
        containerColor = Color.White
    ) { padding ->
        val pair = data
        if (pair == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CoffeeBrown)
            }
            return@Scaffold
        }
        val (order, items) = pair

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order #${order.id.takeLast(8)}",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = CoffeeBrown
                    )
                    Text(order.placedAt.toDateString(), fontFamily = Inter, fontSize = 13.sp, color = WarmGrey)
                }
            }

            if (order.status == "CANCELLED") {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SaleRed.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "This order has been cancelled.",
                            modifier = Modifier.padding(16.dp),
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            color = SaleRed,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                item { StatusTimeline(order) }
                item { DeliveryCard(order) }
            }

            item { SectionTitle(text = "Items (${items.size})") }
            items(items, key = { it.id }) { item ->
                OrderItemCard(
                    item = item,
                    onBuyout = {
                        scope.launch {
                            val amount = repo.buyoutRental(order.id, item.id)
                            Toast.makeText(context, "Bought out for ${amount.inr()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            if (order.assemblyFee > 0 || order.consultFee > 0) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SectionTitle(text = "Services booked")
                            if (order.assemblyFee > 0) {
                                Text("Assembly service booked", fontFamily = Inter, fontSize = 14.sp)
                            }
                            if (order.consultFee > 0) {
                                Text("Designer consult booked", fontFamily = Inter, fontSize = 14.sp)
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
                    Column(Modifier.padding(16.dp)) {
                        SectionTitle(text = "Bill details")
                        Spacer(Modifier.height(8.dp))
                        BillRow("Subtotal", order.subtotal.inr())
                        if (order.bundleDiscount > 0) BillRow("Bundle discount", "-${order.bundleDiscount.inr()}", SuccessGreen)
                        if (order.promoDiscount > 0) BillRow("Promo discount", "-${order.promoDiscount.inr()}", SuccessGreen)
                        if (order.exchangeCredit > 0) BillRow("Exchange credit", "-${order.exchangeCredit.inr()}", SuccessGreen)
                        if (order.loyaltyDiscount > 0) BillRow("Loyalty points", "-${order.loyaltyDiscount.inr()}", SuccessGreen)
                        BillRow("Delivery", if (order.deliveryFee == 0) "FREE" else order.deliveryFee.inr())
                        if (order.assemblyFee > 0) BillRow("Assembly", order.assemblyFee.inr())
                        if (order.consultFee > 0) BillRow("Consult", order.consultFee.inr())
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = CoffeeLight.copy(alpha = 0.4f))
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand total", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(order.grandTotal.inr(), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CoffeeBrown)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTimeline(order: Order) {
    val current = statusIndex(order.status)
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            SectionTitle(text = "Tracking")
            Spacer(Modifier.height(12.dp))
            STEPS.forEachIndexed { i, label ->
                val done = i <= current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (done) SuccessGreen else CoffeeLight.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = label,
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        fontWeight = if (i == current) FontWeight.Bold else FontWeight.Normal,
                        color = if (done) CoffeeBrown else WarmGrey
                    )
                }
                if (i < STEPS.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .width(2.dp)
                            .height(14.dp)
                            .background(if (i < current) SuccessGreen else CoffeeLight.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryCard(order: Order) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AmberGold.copy(alpha = 0.14f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Arriving by", fontFamily = Inter, fontSize = 13.sp, color = WarmGrey)
            Spacer(Modifier.height(4.dp))
            Text(
                text = order.promisedAt.toDateString(),
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = CoffeeBrown
            )
            if (order.deliverySlot.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Slot: ${order.deliverySlot}", fontFamily = Inter, fontSize = 13.sp, color = WarmGrey)
            }
        }
    }
}

@Composable
private fun OrderItemCard(item: OrderItem, onBuyout: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Cream),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                if (item.isRental) {
                    Surface(shape = RoundedCornerShape(50), color = CoffeeBrown) {
                        Text(
                            "RENTAL",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontFamily = Inter,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Qty ${item.qty}  ·  ${item.price.inr()}",
                fontFamily = Inter,
                fontSize = 13.sp,
                color = WarmGrey
            )
            if (item.isRental) {
                Spacer(Modifier.height(4.dp))
                Text("${item.monthly.inr()}/mo", fontFamily = Inter, fontSize = 13.sp, color = CoffeeBrown)
                if (!item.boughtOut) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onBuyout,
                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Buyout", fontFamily = Inter)
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text("Bought out", fontFamily = Inter, fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String, valueColor: Color = CoffeeBrown) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = Inter, fontSize = 14.sp, color = WarmGrey)
        Text(value, fontFamily = Inter, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}
