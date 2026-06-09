package br.com.redemaisfarma.mobile.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import br.com.redemaisfarma.mobile.ui.screens.CartScreen
import br.com.redemaisfarma.mobile.ui.screens.CatalogScreen
import br.com.redemaisfarma.mobile.ui.screens.NotificationsScreen
import br.com.redemaisfarma.mobile.ui.screens.OrderDetailScreen
import br.com.redemaisfarma.mobile.ui.screens.OrdersScreen
import br.com.redemaisfarma.mobile.ui.screens.ProductDetailScreen
import br.com.redemaisfarma.mobile.ui.screens.ProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            CatalogScreen(paddingValues = paddingValues, onOpenProduct = { id ->
                navController.navigate(Routes.productDetail(id))
            })
        }
        composable(Routes.CART) { CartScreen(paddingValues) }
        composable(Routes.ORDERS) {
            OrdersScreen(paddingValues = paddingValues, onOpenPedido = { id ->
                navController.navigate(Routes.orderDetail(id))
            })
        }
        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            OrderDetailScreen(paddingValues = paddingValues, pedidoId = id)
        }
        composable(Routes.PROFILE) { ProfileScreen(paddingValues) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(paddingValues) }
        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            ProductDetailScreen(paddingValues = paddingValues, productId = id)
        }
    }
}
