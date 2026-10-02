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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.kurupdevs.woodnest.data.db.Address
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.SaleRed
import com.kurupdevs.woodnest.ui.theme.WarmGrey
import kotlinx.coroutines.launch

@Composable
fun AddressesScreen(navController: NavController, repo: WoodNestRepository) {
    val addresses by repo.addressesFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var editingId by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var line1 by remember { mutableStateOf("") }
    var line2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }

    fun clearForm() {
        editingId = null
        name = ""; phone = ""; line1 = ""; line2 = ""; city = ""; state = ""; pincode = ""
    }

    Scaffold(
        topBar = { WoodNestTopBar(title = "Addresses", onBack = { navController.popBackStack() }) },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(addresses, key = { it.id }) { address ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Cream),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = address.isDefault,
                            onClick = { scope.launch { repo.setDefaultAddress(address.id) } },
                            colors = RadioButtonDefaults.colors(selectedColor = CoffeeBrown)
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(
                                address.name,
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                listOf(address.line1, address.line2, address.city, address.state, address.pincode)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", "),
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = WarmGrey
                            )
                            Text(address.phone, fontFamily = Inter, fontSize = 13.sp, color = WarmGrey)
                            if (address.isDefault) {
                                Text(
                                    "DEFAULT",
                                    fontFamily = Inter,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoffeeBrown
                                )
                            }
                        }
                        IconButton(onClick = {
                            editingId = address.id
                            name = address.name
                            phone = address.phone
                            line1 = address.line1
                            line2 = address.line2
                            city = address.city
                            state = address.state
                            pincode = address.pincode
                        }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = CoffeeBrown)
                        }
                        IconButton(onClick = { scope.launch { repo.deleteAddress(address.id) } }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = SaleRed)
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionTitle(text = if (editingId == null) "Add address" else "Edit address")
                        AddressField(value = name, onChange = { name = it }, label = "Full name")
                        AddressField(
                            value = phone,
                            onChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                            label = "Phone"
                        )
                        AddressField(value = line1, onChange = { line1 = it }, label = "Address line 1")
                        AddressField(value = line2, onChange = { line2 = it }, label = "Address line 2 (optional)")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AddressField(value = city, onChange = { city = it }, label = "City", modifier = Modifier.weight(1f))
                            AddressField(value = state, onChange = { state = it }, label = "State", modifier = Modifier.weight(1f))
                        }
                        AddressField(
                            value = pincode,
                            onChange = { pincode = it.filter { c -> c.isDigit() }.take(6) },
                            label = "Pincode"
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PrimaryButton(
                                text = if (editingId == null) "Add address" else "Save changes",
                                onClick = {
                                    if (name.isBlank() || phone.length != 10 || line1.isBlank() ||
                                        city.isBlank() || state.isBlank() || pincode.length != 6
                                    ) return@PrimaryButton
                                    scope.launch {
                                        repo.upsertAddress(
                                            Address(
                                                id = editingId ?: 0L,
                                                name = name.trim(),
                                                phone = phone,
                                                line1 = line1.trim(),
                                                line2 = line2.trim(),
                                                city = city.trim(),
                                                state = state.trim(),
                                                pincode = pincode,
                                                isDefault = false
                                            )
                                        )
                                        clearForm()
                                    }
                                }
                            )
                            if (editingId != null) {
                                androidx.compose.material3.TextButton(onClick = { clearForm() }) {
                                    Text("Cancel", fontFamily = Inter)
                                }
                            }
                        }
                    }
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
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontFamily = Inter) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}
