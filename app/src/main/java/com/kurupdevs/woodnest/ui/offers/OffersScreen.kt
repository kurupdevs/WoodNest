package com.kurupdevs.woodnest.ui.offers

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter

private data class OfferDef(val code: String, val description: String)

private val OFFERS = listOf(
    OfferDef("WELCOME10", "10% OFF up to ₹2,000 on your first order"),
    OfferDef("FLAT500", "₹500 off on orders above ₹9,999"),
    OfferDef("FREESHIP", "FREE delivery on your order"),
    OfferDef("FESTIVE15", "15% off on orders above ₹24,999"),
    OfferDef("REFBONUS500", "₹500 off — referral reward")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersScreen(navController: NavController, repo: WoodNestRepository) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        WoodNestTopBar(
            title = "Offers",
            onBack = { navController.popBackStack() },
            actions = {}
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(OFFERS) { offer ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Cream)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = CoffeeBrown,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                offer.code,
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = CoffeeBrown
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                offer.description,
                                fontFamily = Inter,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(offer.code))
                            Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy code", tint = CoffeeBrown)
                        }
                    }
                }
            }
        }
    }
}
