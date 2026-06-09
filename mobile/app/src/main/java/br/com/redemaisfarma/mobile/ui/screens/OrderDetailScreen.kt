package br.com.redemaisfarma.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import br.com.redemaisfarma.mobile.ui.components.ErrorState
import br.com.redemaisfarma.mobile.ui.components.SimpleCard
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.viewmodel.OrderDetailViewModel

@Composable
fun OrderDetailScreen(paddingValues: PaddingValues, pedidoId: Long) {
    val viewModel: OrderDetailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.padding(paddingValues).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Pedido #$pedidoId", style = MaterialTheme.typography.headlineSmall)
        when (val current = state) {
            UiState.Idle -> Text(text = "Carregando pedido...", style = MaterialTheme.typography.bodyMedium)
            UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                Text(text = current.data.status, style = MaterialTheme.typography.bodyMedium)
                current.data.metodoPagamento?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                current.data.itens.forEach { item ->
                    SimpleCard(
                        title = item.produto ?: "Produto",
                        lines = listOf("Qtd: ${item.quantidade ?: 0}")
                    )
                }
            }
            is UiState.Error -> {
                ErrorState(message = current.message, label = "Pedido")
                Button(onClick = { viewModel.carregarPedido(pedidoId) }) {
                    Text(text = "Tentar novamente")
                }
            }
        }
    }
}
