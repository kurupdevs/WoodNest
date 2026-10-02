package com.kurupdevs.woodnest.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.kurupdevs.woodnest.data.db.Consult
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.EmptyState
import com.kurupdevs.woodnest.ui.components.OutlineButton
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.SectionTitle
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

private val SLOTS = listOf("10 AM - 12 PM", "12 PM - 2 PM", "2 PM - 4 PM", "4 PM - 6 PM")
private const val DAY_MS = 24 * 60 * 60 * 1000L

@Composable
fun ConsultsScreen(navController: NavController, repo: WoodNestRepository) {
    val consults by repo.consultsFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var cancelTarget by remember { mutableStateOf<Consult?>(null) }
    var rescheduleTarget by remember { mutableStateOf<Consult?>(null) }

    Scaffold(
        topBar = { WoodNestTopBar(title = "My Consults", onBack = { navController.popBackStack() }) },
        containerColor = Color.White
    ) { padding ->
        if (consults.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState(
                    title = "No consults booked",
                    message = "Book a free designer consult to plan your space.",
                    actionLabel = "Book a consult",
                    onAction = { navController.navigate(Routes.CONSULTS) }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(consults, key = { it.id }) { consult ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Cream),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    consult.date.toDateString(),
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = CoffeeBrown
                                )
                                ConsultStatusChip(consult.status)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(consult.slot, fontFamily = Inter, fontSize = 13.sp, color = WarmGrey)
                            Text(
                                if (consult.fee == 0) "Free consult" else consult.fee.inr(),
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = WarmGrey
                            )
                            if (consult.status == "BOOKED") {
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlineButton(text = "Reschedule", onClick = { rescheduleTarget = consult })
                                    TextButton(onClick = { cancelTarget = consult }) {
                                        Text("Cancel", color = SaleRed, fontFamily = Inter)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    cancelTarget?.let { consult ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            title = { Text("Cancel consult?", fontFamily = Inter, fontWeight = FontWeight.Bold) },
            text = { Text("Your consult on ${consult.date.toDateString()} (${consult.slot}) will be cancelled.", fontFamily = Inter) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repo.cancelConsult(consult.id) }
                    cancelTarget = null
                }) { Text("Yes, cancel", color = SaleRed, fontFamily = Inter) }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) { Text("Keep it", fontFamily = Inter) }
            }
        )
    }

    rescheduleTarget?.let { consult ->
        RescheduleSheet(
            consult = consult,
            onDismiss = { rescheduleTarget = null },
            onSave = { date, slot ->
                scope.launch { repo.rescheduleConsult(consult.id, date, slot) }
                rescheduleTarget = null
            }
        )
    }
}

@Composable
private fun ConsultStatusChip(status: String) {
    val (bg, label) = when (status) {
        "BOOKED" -> AmberGold to "Booked"
        "COMPLETED" -> SuccessGreen to "Completed"
        else -> SaleRed to "Cancelled"
    }
    Surface(shape = RoundedCornerShape(50), color = bg.copy(alpha = 0.14f)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontFamily = Inter,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = bg
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RescheduleSheet(consult: Consult, onDismiss: () -> Unit, onSave: (Long, String) -> Unit) {
    val today = remember { System.currentTimeMillis() }
    var pickedDate by remember { mutableStateOf(today) }
    var pickedSlot by remember { mutableStateOf(consult.slot) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reschedule consult", fontFamily = Inter, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle(text = "Pick a date")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (0..6).forEach { i ->
                        val date = today + i * DAY_MS
                        FilterChip(
                            selected = pickedDate == date,
                            onClick = { pickedDate = date },
                            label = { Text(date.toDateString(), fontFamily = Inter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown)
                        )
                    }
                }
                SectionTitle(text = "Pick a slot")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SLOTS.forEach { slot ->
                        FilterChip(
                            selected = pickedSlot == slot,
                            onClick = { pickedSlot = slot },
                            label = { Text(slot, fontFamily = Inter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoffeeBrown)
                        )
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(text = "Save", onClick = { onSave(pickedDate, pickedSlot) })
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", fontFamily = Inter) }
        }
    )
}
