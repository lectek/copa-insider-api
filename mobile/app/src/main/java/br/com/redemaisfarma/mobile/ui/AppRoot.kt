package br.com.redemaisfarma.mobile.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.redemaisfarma.mobile.ui.navigation.AppNavHost
import br.com.redemaisfarma.mobile.ui.navigation.Routes
import br.com.redemaisfarma.mobile.ui.screens.LoginScreen
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val loginState by authViewModel.loginState.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    if (isAuthenticated == null) {
        MaterialTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    if (isAuthenticated == false) {
        MaterialTheme {
            LoginScreen(
                state = if (loginState is UiState.Success) UiState.Idle else loginState,
                onLogin = authViewModel::login
            )
        }
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val items = listOf(
        NavItem(Routes.HOME, "Catalogo", Icons.Filled.Storefront),
        NavItem(Routes.CART, "Carrinho", Icons.Filled.ShoppingCart),
        NavItem(Routes.NOTIFICATIONS, "Avisos", Icons.Filled.Notifications),
        NavItem(Routes.PROFILE, "Conta", Icons.Filled.Person)
    )

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentDestination?.route) {
                                Routes.CART -> "Carrinho"
                                Routes.ORDERS -> "Pedidos"
                                Routes.ORDER_DETAIL -> "Pedido"
                                Routes.PROFILE -> "Minha conta"
                                Routes.NOTIFICATIONS -> "Avisos"
                                Routes.PRODUCT_DETAIL -> "Produto"
                                else -> "Catalogo"
                            }
                        )
                    },
                    actions = {
                        TextButton(onClick = { authViewModel.logout() }) {
                            Text(text = "Sair")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(text = item.label) }
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            AppNavHost(navController = navController, paddingValues = paddingValues, modifier = Modifier)
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
