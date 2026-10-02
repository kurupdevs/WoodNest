package com.kurupdevs.woodnest.ui.cart

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.billing.BillLineInput
import com.kurupdevs.woodnest.data.billing.PricingEngine
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.EmptyState
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.components.productPainter
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.util.inr
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, repo: WoodNestRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lines by repo.cartLines().collectAsStateWithLifecycle(initialValue = emptyList())
    var promoInput by remember { mutableStateOf("") }
    var appliedPromo by remember { mutableStateOf("") }

    val billInputs = lines.map { line ->
        BillLineInput(
            productId = line.product.id,
            name = line.product.name,
            price = line.product.price,
            qty = line.item.qty,
            isRental = line.item.isRental,
            monthly = PricingEngine.monthlyFor(line.product.price)
        )
    }
    val bill = PricingEngine.computeBill(
        lines = billInputs,
        promoCode = appliedPromo,
        useLoyalty = false,
        loyaltyPoints = 0,
        exchangeCredit = 0,
        wantAssembly = false,
        wantConsult = false,
        referralPromoAvailable = false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WoodNestTopBar(
            title = "Cart",
            onBack = { navController.popBackStack() },
            actions = {}
        )

        if (lines.isEmpty()) {
            EmptyState(
                title = "Your cart is empty",
                message = "Beautiful furniture is waiting for you.",
                actionLabel = "Shop now",
                onAction = { navController.navigate(Routes.HOME) }
            )
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(lines, key = { it.item.id }) { line ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = productPainter(line.product),
                            contentDescription = line.product.name,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    line.product.name,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (line.item.isRental) {
                                    Spacer(Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CoffeeBrown
                                    ) {
                                        Text(
                                            "RENTAL",
                                            fontFamily = Inter,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (line.item.isRental)
                                    "${PricingEngine.monthlyFor(line.product.price).inr()}/mo × ${line.item.qty}"
                                else
                                    (line.product.price * line.item.qty).inr(),
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                color = CoffeeBrown,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            val next = line.item.qty - 1
                                            if (next <= 0) repo.removeFromCart(line.item.id)
                                            else repo.updateQty(line.item.id, next)
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(line.item.qty.toString(), fontFamily = Inter, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = { scope.launch { repo.updateQty(line.item.id, line.item.qty + 1) } },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repo.removeFromCart(line.item.id)
                                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = SaleRed)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                SectionTitle(text = "Promo code")
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promoInput,
                        onValueChange = { promoInput = it.uppercase() },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter code", fontFamily = Inter) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    TextButton(onClick = {
                        appliedPromo = promoInput.trim()
                        if (appliedPromo.isNotBlank()) {
                            Toast.makeText(context, "Promo applied", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Apply", fontFamily = Inter, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                    }
                }
                if (bill.promoError.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(bill.promoError, fontFamily = Inter, color = SaleRed, style = MaterialTheme.typography.bodySmall)
                } else if (bill.promoDiscount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Promo applied: −${bill.promoDiscount.inr()}",
                        fontFamily = Inter,
                        color = CoffeeBrown,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F2))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionTitle(text = "Bill summary")
                        Spacer(Modifier.height(8.dp))
                        BillRow("Subtotal", (bill.purchaseSubtotal + bill.rentalSubtotal).inr())
                        if (bill.bundleDiscount > 0) BillRow("Bundle discount", "−${bill.bundleDiscount.inr()}")
                        if (bill.promoDiscount > 0) BillRow("Promo (${bill.promoCode})", "−${bill.promoDiscount.inr()}")
                        if (bill.rentalMonthlyTotal > 0) BillRow("Rental monthly", "${bill.rentalMonthlyTotal.inr()}/mo")
                        BillRow("Delivery", if (bill.deliveryFee == 0) "FREE" else bill.deliveryFee.inr())
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand total", fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(bill.grandTotal.inr(), fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = CoffeeBrown)
                        }
                    }
                }
            }

            item {
                PrimaryButton(
                    text = "Proceed to Checkout",
                    onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set("promo", appliedPromo)
                        navController.navigate(Routes.CHECKOUT)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BillRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, fontFamily = Inter, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
