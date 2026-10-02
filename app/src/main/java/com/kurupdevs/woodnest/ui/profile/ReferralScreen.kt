package com.kurupdevs.woodnest.ui.profile

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.components.OutlineButton
import com.kurupdevs.woodnest.ui.components.PrimaryButton
import com.kurupdevs.woodnest.ui.components.SectionTitle
import com.kurupdevs.woodnest.ui.components.WoodNestTopBar
import com.kurupdevs.woodnest.ui.theme.AmberGold
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Cream
import com.kurupdevs.woodnest.ui.theme.Inter
import com.kurupdevs.woodnest.ui.theme.WarmGrey
import kotlinx.coroutines.launch

@Composable
fun ReferralScreen(navController: NavController, repo: WoodNestRepository) {
    val loyalty by repo.loyaltyFlow().collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var codeInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = { WoodNestTopBar(title = "Refer & Earn", onBack = { navController.popBackStack() }) },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                val account = loyalty
                if (account == null || !account.joined) {
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
                                "Invite friends, earn together",
                                fontFamily = Inter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = CoffeeBrown,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Join WoodNest Circle to get your referral code. You and your friend both get ₹500 off.",
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = WarmGrey,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton(
                                text = "Join to get your code",
                                onClick = {
                                    scope.launch {
                                        repo.joinCircle()
                                        Toast.makeText(context, "Welcome to WoodNest Circle", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                } else {
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
                                "Your referral code",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                account.referralCode,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                color = AmberGold,
                                letterSpacing = 4.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Share it - you both get ₹500 off",
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            OutlineButton(
                                text = "Share my code",
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Shop furniture on WoodNest with my code ${account.referralCode} - we both get ₹500 off!"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share referral code"))
                                }
                            )
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
                        SectionTitle(text = "Have a code?")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = codeInput,
                                onValueChange = { codeInput = it.uppercase().trim() },
                                label = { Text("Referral code", fontFamily = Inter) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            PrimaryButton(
                                text = "Apply",
                                onClick = {
                                    if (codeInput.isBlank()) return@PrimaryButton
                                    scope.launch {
                                        val ok = repo.applyReferralCode(codeInput)
                                        Toast.makeText(
                                            context,
                                            if (ok) "Code applied" else "Invalid code",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                        Text(
                            "You get ₹500 off coupon REFBONUS500 on orders above ₹4,999.",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = WarmGrey
                        )
                    }
                }
            }
        }
    }
}
