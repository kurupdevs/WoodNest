package com.kurupdevs.woodnest.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import com.kurupdevs.woodnest.ui.cart.CartScreen
import com.kurupdevs.woodnest.ui.checkout.CheckoutScreen
import com.kurupdevs.woodnest.ui.profile.ConsultsScreen
import com.kurupdevs.woodnest.ui.detail.ProductDetailScreen
import com.kurupdevs.woodnest.ui.favorites.FavoritesScreen
import com.kurupdevs.woodnest.ui.home.HomeScreen
import com.kurupdevs.woodnest.ui.offers.OffersScreen
import com.kurupdevs.woodnest.ui.orders.OrdersScreen
import com.kurupdevs.woodnest.ui.profile.AddressesScreen
import com.kurupdevs.woodnest.ui.profile.LoyaltyScreen
import com.kurupdevs.woodnest.ui.profile.NotificationsScreen
import com.kurupdevs.woodnest.ui.profile.ProfileScreen
import com.kurupdevs.woodnest.ui.profile.ReferralScreen
import com.kurupdevs.woodnest.ui.reels.ReelsScreen

@Composable
fun WoodNestNavGraph(navController: NavHostController, repo: WoodNestRepository) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController, repo) }
        composable(Routes.REELS) { ReelsScreen(navController, repo) }
        composable(Routes.FAVORITES) { FavoritesScreen(navController, repo) }
        composable(Routes.CART) { CartScreen(navController, repo) }
        composable(Routes.ORDERS) { OrdersScreen(navController, repo) }
        composable(Routes.PROFILE) { ProfileScreen(navController, repo) }
        composable(
            route = Routes.PRODUCT,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(navController, repo, productId)
        }
        composable(Routes.CHECKOUT) { CheckoutScreen(navController, repo) }
        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(navController, repo, orderId)
        }
        composable(Routes.OFFERS) { OffersScreen(navController, repo) }
        composable(Routes.ADDRESSES) { AddressesScreen(navController, repo) }
        composable(Routes.CONSULTS) { ConsultsScreen(navController, repo) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(navController, repo) }
        composable(Routes.REFERRAL) { ReferralScreen(navController, repo) }
        composable(Routes.LOYALTY) { LoyaltyScreen(navController, repo) }
    }
}
