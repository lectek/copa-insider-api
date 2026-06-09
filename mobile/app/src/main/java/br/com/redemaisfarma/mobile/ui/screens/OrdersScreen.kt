package br.com.redemaisfarma.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.redemaisfarma.mobile.ui.components.EmptyState
import br.com.redemaisfarma.mobile.ui.components.ErrorState
import br.com.redemaisfarma.mobile.ui.components.SimpleCard
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.viewmodel.OrdersViewModel

@Composable
fun OrdersScreen(paddingValues: PaddingValues, onOpenPedido: (Long) -> Unit) {
    val viewModel: OrdersViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.padding(paddingValues).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Pedidos", style = MaterialTheme.typography.headlineSmall)
        when (val current = state) {
            UiState.Idle -> Text(text = "Carregando pedidos...", style = MaterialTheme.typography.bodyMedium)
            UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    EmptyState(message = "Nenhum pedido encontrado.")
                } else {
                    current.data.forEach { pedido ->
                        val lines = mutableListOf(pedido.status)
                        pedido.metodoPagamento?.let(lines::add)
                        SimpleCard(
                            title = "Pedido #${pedido.id}",
                            lines = lines,
                            actionLabel = "Detalhes",
                            onAction = { onOpenPedido(pedido.id) }
                        )
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(message = current.message, label = "Pedidos")
                Button(onClick = { viewModel.carregarPedidos() }) {
                    Text(text = "Tentar novamente")
                }
            }
        }
    }
}
