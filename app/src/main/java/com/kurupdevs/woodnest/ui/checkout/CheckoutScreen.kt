package com.kurupdevs.woodnest.ui.checkout

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.billing.BillLineInput
import com.kurupdevs.woodnest.data.billing.PricingEngine
import com.kurupdevs.woodnest.data.db.Address
import com.kurupdevs.woodnest.data.db.Consult
import com.kurupdevs.woodnest.data.db.LoyaltyAccount
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.nav.Routes
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.ui.theme.SuccessGreen
import com.kurupdevs.woodnest.util.DeliveryEstimate
import com.kurupdevs.woodnest.util.DeliveryEstimator
import com.kurupdevs.woodnest.util.inr
import com.kurupdevs.woodnest.util.toDateString
import kotlinx.coroutines.launch

private val DELIVERY_SLOTS = listOf("Morning 9–12", "Afternoon 12–4", "Evening 4–8")
private val CONSULT_SLOTS = listOf("10 AM", "12 PM", "3 PM", "5 PM")
private val EXCHANGE_CATEGORIES = listOf("Sofa", "Bed", "Table", "Chair", "Other")
private val EXCHANGE_VALUES = mapOf("Good" to 2000, "Fair" to 1200, "Poor" to 600)
private const val DAY_MS = 24 * 60 * 60 * 1000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController, repo: WoodNestRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle

    val address by repo.defaultAddressFlow().collectAsStateWithLifecycle(initialValue = null)
    val loyalty by repo.loyaltyFlow().collectAsStateWithLifecycle(initialValue = LoyaltyAccount())
    val lines by repo.cartLines().collectAsStateWithLifecycle(initialValue = emptyList())

    var promo by remember { mutableStateOf(savedStateHandle?.get<String>("promo") ?: "") }
    var slot by remember { mutableStateOf(DELIVERY_SLOTS[0]) }

    var showForm by remember { mutableStateOf(false) }
    var fName by remember { mutableStateOf("") }
    var fPhone by remember { mutableStateOf("") }
    var fLine1 by remember { mutableStateOf("") }
    var fLine2 by remember { mutableStateOf("") }
    var fCity by remember { mutableStateOf("") }
    var fState by remember { mutableStateOf("") }
    var fPin by remember { mutableStateOf("") }

    var wantAssembly by remember { mutableStateOf(false) }
    var assemblySlot by remember { mutableStateOf(DELIVERY_SLOTS[0]) }

    var wantExchange by remember { mutableStateOf(false) }
    var exCategory by remember { mutableStateOf(EXCHANGE_CATEGORIES[0]) }
    var exCondition by remember { mutableStateOf("Good") }
    var exExpanded by remember { mutableStateOf(false) }

    var wantConsult by remember { mutableStateOf(false) }
    val baseDay = remember { System.currentTimeMillis() }
    val consultDays = remember { (0..6).map { baseDay + it * DAY_MS } }
    var consultDay by remember { mutableStateOf(baseDay) }
    var consultSlot by remember { mutableStateOf(CONSULT_SLOTS[0]) }

    var useLoyalty by remember { mutableStateOf(false) }

    val exchangeCredit = if (wantExchange) EXCHANGE_VALUES[exCondition] ?: 0 else 0
    val exchangeDesc = if (wantExchange) "Old $exCategory ($exCondition)" else ""

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
        promoCode = promo,
        useLoyalty = useLoyalty,
        loyaltyPoints = loyalty.points,
        exchangeCredit = exchangeCredit,
        wantAssembly = wantAssembly,
        wantConsult = wantConsult,
        referralPromoAvailable = !loyalty.referralPromoUsed
    )

    val estimate: DeliveryEstimate? = address?.let { DeliveryEstimator.estimate(it.pincode) }
    val unserviceable = estimate is DeliveryEstimate.Unserviceable
    val canPlace = address != null && !unserviceable && lines.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WoodNestTopBar(
            title = "Checkout",
            onBack = { navController.popBackStack() },
            actions = {}
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // a) Address
            item {
                SectionTitle(text = "Delivery address")
                Spacer(Modifier.height(6.dp))
                val a = address
                if (a != null && !showForm) {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(a.name, fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${a.line1}${if (a.line2.isNotBlank()) ", ${a.line2}" else ""}, ${a.city}, ${a.state} ${a.pincode}",
                                fontFamily = Inter,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(a.phone, fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = { navController.navigate(Routes.ADDRESSES) }) {
                                Text("Change", fontFamily = Inter, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                            }
                        }
                    }
                } else {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (a == null) TextButton(onClick = { showForm = true }) {
                                Text("Add address", fontFamily = Inter, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                            }
                            AddressField(fName, { fName = it }, "Full name")
                            AddressField(fPhone, { if (it.length <= 10 && it.all(Char::isDigit)) fPhone = it }, "Phone (10 digits)")
                            AddressField(fLine1, { fLine1 = it }, "Address line 1")
                            AddressField(fLine2, { fLine2 = it }, "Address line 2 (optional)")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AddressField(fCity, { fCity = it }, "City", Modifier.weight(1f))
                                AddressField(fState, { fState = it }, "State", Modifier.weight(1f))
                            }
                            AddressField(fPin, { if (it.length <= 6 && it.all(Char::isDigit)) fPin = it }, "Pincode", singleLine = true)
                            PrimaryButton(
                                text = "Save address",
                                onClick = {
                                    if (fName.isBlank() || fPhone.length != 10 || fLine1.isBlank() || fCity.isBlank() || fState.isBlank() || fPin.length != 6) {
                                        Toast.makeText(context, "Fill all address fields correctly", Toast.LENGTH_SHORT).show()
                                        return@PrimaryButton
                                    }
                                    scope.launch {
                                        val id = repo.upsertAddress(
                                            Address(
                                                name = fName.trim(),
                                                phone = fPhone.trim(),
                                                line1 = fLine1.trim(),
                                                line2 = fLine2.trim(),
                                                city = fCity.trim(),
                                                state = fState.trim(),
                                                pincode = fPin.trim()
                                            )
                                        )
                                        repo.setDefaultAddress(id)
                                        showForm = false
                                        Toast.makeText(context, "Address saved", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // b) Delivery slot
            item {
                SectionTitle(text = "Delivery slot")
                Spacer(Modifier.height(6.dp))
                SlotChips(options = DELIVERY_SLOTS, selected = slot, onSelect = { slot = it })
            }

            // c) Delivery date
            item {
                SectionTitle(text = "Delivery date")
                Spacer(Modifier.height(6.dp))
                when (val e = estimate) {
                    is DeliveryEstimate.Serviceable -> Text(
                        "${e.label} · arrives by ${e.promisedAt.toDateString()}",
                        fontFamily = Inter,
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen
                    )
                    is DeliveryEstimate.Unserviceable -> Text(
                        "Not serviceable in this pincode. Change the address to place the order.",
                        fontFamily = Inter,
                        style = MaterialTheme.typography.bodySmall,
                        color = SaleRed
                    )
                    null -> Text(
                        "Add a delivery address to see the date.",
                        fontFamily = Inter,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // d) Assembly
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Assembly service", fontFamily = Inter, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = wantAssembly,
                                onCheckedChange = { wantAssembly = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CoffeeBrown, checkedTrackColor = CoffeeBrown.copy(alpha = 0.4f))
                            )
                        }
                        if (wantAssembly) {
                            Spacer(Modifier.height(8.dp))
                            SlotChips(options = DELIVERY_SLOTS, selected = assemblySlot, onSelect = { assemblySlot = it })
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Free above ₹20,000, else ₹499.", fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            // e) Exchange
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Exchange old furniture", fontFamily = Inter, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = wantExchange,
                                onCheckedChange = { wantExchange = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CoffeeBrown, checkedTrackColor = CoffeeBrown.copy(alpha = 0.4f))
                            )
                        }
                        if (wantExchange) {
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.foundation.layout.Box {
                                OutlinedTextField(
                                    value = exCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category", fontFamily = Inter) },
                                    trailingIcon = {
                                        IconButton(onClick = { exExpanded = true }) {
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                DropdownMenu(expanded = exExpanded, onDismissRequest = { exExpanded = false }) {
                                    EXCHANGE_CATEGORIES.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, fontFamily = Inter) },
                                            onClick = { exCategory = cat; exExpanded = false }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                EXCHANGE_VALUES.keys.forEach { cond ->
                                    FilterChip(
                                        selected = exCondition == cond,
                                        onClick = { exCondition = cond },
                                        label = { Text("$cond ${EXCHANGE_VALUES[cond]!!.inr()}", fontFamily = Inter, style = MaterialTheme.typography.bodySmall) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown, selectedLabelColor = Color.White)
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Credit applied: $exchangeDesc = ${exchangeCredit.inr()}", fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = CoffeeBrown, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Max one per order · free pickup with delivery.", fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            // f) Designer consult
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Designer consult", fontFamily = Inter, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = wantConsult,
                                onCheckedChange = { wantConsult = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CoffeeBrown, checkedTrackColor = CoffeeBrown.copy(alpha = 0.4f))
                            )
                        }
                        if (wantConsult) {
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(consultDays) { day ->
                                    FilterChip(
                                        selected = consultDay == day,
                                        onClick = { consultDay = day },
                                        label = { Text(day.toDateString(), fontFamily = Inter, style = MaterialTheme.typography.bodySmall) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown, selectedLabelColor = Color.White)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            SlotChips(options = CONSULT_SLOTS, selected = consultSlot, onSelect = { consultSlot = it })
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Free above ₹25,000, else ₹499.", fontFamily = Inter, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            // g) Loyalty
            if (loyalty.joined && loyalty.points >= 100) {
                item {
                    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Use points (${loyalty.points} pts = ${bill.loyaltyDiscount.inr()} off)",
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = useLoyalty,
                                onCheckedChange = { useLoyalty = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = CoffeeBrown, checkedTrackColor = CoffeeBrown.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }

            // h) Promo
            item {
                SectionTitle(text = "Promo code")
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = promo,
                    onValueChange = { promo = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter promo code", fontFamily = Inter) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                if (bill.promoError.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(bill.promoError, fontFamily = Inter, color = SaleRed, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Bill
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Cream)) {
                    Column(Modifier.padding(16.dp)) {
                        SectionTitle(text = "Order summary")
                        Spacer(Modifier.height(8.dp))
                        BillRow("Subtotal", (bill.purchaseSubtotal + bill.rentalSubtotal).inr())
                        if (bill.bundleDiscount > 0) BillRow("Bundle discount", "−${bill.bundleDiscount.inr()}")
                        if (bill.promoDiscount > 0) BillRow("Promo (${bill.promoCode})", "−${bill.promoDiscount.inr()}")
                        if (bill.exchangeCredit > 0) BillRow("Exchange credit", "−${bill.exchangeCredit.inr()}")
                        if (bill.loyaltyDiscount > 0) BillRow("Loyalty (${bill.loyaltyPointsUsed} pts)", "−${bill.loyaltyDiscount.inr()}")
                        if (bill.rentalMonthlyTotal > 0) BillRow("Rental monthly", "${bill.rentalMonthlyTotal.inr()}/mo")
                        BillRow("Delivery", if (bill.deliveryFee == 0) "FREE" else bill.deliveryFee.inr())
                        if (bill.assemblyFee > 0) BillRow("Assembly", bill.assemblyFee.inr())
                        if (bill.consultFee > 0) BillRow("Designer consult", bill.consultFee.inr())
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand total", fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(bill.grandTotal.inr(), fontFamily = Inter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = CoffeeBrown)
                        }
                        if (bill.pointsToEarn > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "You will earn ${bill.pointsToEarn} loyalty points",
                                fontFamily = Inter,
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }

            item {
                PrimaryButton(
                    text = "Place Order",
                    onClick = {
                        scope.launch {
                            val consult = if (wantConsult) {
                                Consult(
                                    date = consultDay,
                                    slot = consultSlot,
                                    status = "BOOKED",
                                    fee = bill.consultFee,
                                    createdAt = System.currentTimeMillis()
                                )
                            } else null
                            val oid = repo.placeOrder(
                                com.kurupdevs.woodnest.data.repo.WoodNestRepository.OrderInput(
                                    promoCode = promo,
                                    useLoyalty = useLoyalty,
                                    loyaltyPointsUsed = bill.loyaltyPointsUsed,
                                    exchangeCredit = exchangeCredit,
                                    exchangeDesc = exchangeDesc,
                                    assembly = wantAssembly,
                                    assemblySlot = assemblySlot,
                                    consult = consult,
                                    bill = bill
                                )
                            )
                            navController.navigate(Routes.orderRoute(oid)) {
                                popUpTo(Routes.CHECKOUT) { inclusive = true }
                            }
                        }
                    },
                    enabled = canPlace,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Demo checkout — no real payment",
                        fontFamily = Inter,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, fontFamily = Inter) },
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun SlotChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(option, fontFamily = Inter, style = MaterialTheme.typography.bodySmall) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown, selectedLabelColor = Color.White)
            )
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
