package com.kurupdevs.woodnest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kurupdevs.woodnest.ui.theme.CoffeeBrown
import com.kurupdevs.woodnest.ui.theme.Inter
import kotlinx.coroutines.delay

data class TickerOffer(val id: String, val text: String)

@Composable
fun OfferTicker(
    offers: List<TickerOffer>,
    onOfferTap: (TickerOffer) -> Unit,
    modifier: Modifier = Modifier
) {
    if (offers.isEmpty()) return

    val repeated = remember(offers) { List(100) { offers }.flatten() }
    val listState = rememberLazyListState()

    LaunchedEffect(repeated) {
        var index = 0
        while (true) {
            delay(2500)
            index++
            if (index >= repeated.size) {
                listState.scrollToItem(0)
                index = 0
            } else {
                listState.animateScrollToItem(index)
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = repeated.size, key = { it }) { index ->
            val offer = repeated[index]
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(CoffeeBrown)
                    .clickable { onOfferTap(offer) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = offer.text,
                    color = Color.White,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
