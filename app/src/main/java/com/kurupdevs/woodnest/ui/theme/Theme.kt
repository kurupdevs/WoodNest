package com.kurupdevs.woodnest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WoodNestColorScheme = lightColorScheme(
    primary = CoffeeBrown,
    onPrimary = White,
    secondary = CoffeeLight,
    background = White,
    surface = White,
    surfaceVariant = Cream,
    error = SaleRed
)

@Composable
fun WoodNestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WoodNestColorScheme,
        typography = WoodNestTypography,
        content = content
    )
}
