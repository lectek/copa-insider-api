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
import br.com.redemaisfarma.mobile.ui.components.EmptyState
import br.com.redemaisfarma.mobile.ui.components.ErrorState
import br.com.redemaisfarma.mobile.ui.components.SimpleCard
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(paddingValues: PaddingValues) {
    val viewModel: NotificationsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.padding(paddingValues).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Avisos", style = MaterialTheme.typography.headlineSmall)
        when (val current = state) {
            UiState.Idle -> Text(text = "Carregando avisos...", style = MaterialTheme.typography.bodyMedium)
            UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    EmptyState(message = "Nenhum aviso encontrado.")
                } else {
                    current.data.forEach { aviso ->
                        val lines = mutableListOf(aviso.mensagem ?: "")
                        if (aviso.lida == true) {
                            lines.add("Lido")
                        }
                        SimpleCard(
                            title = aviso.titulo ?: "Aviso",
                            lines = lines
                        )
                    }
                    Button(
                        onClick = {
                            val ids = current.data.mapNotNull { it.id }
                            viewModel.marcarTodasComoLidas(ids)
                        }
                    ) {
                        Text(text = "Marcar como lidas")
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(message = current.message, label = "Avisos")
                Button(onClick = { viewModel.carregarNotificacoes() }) {
                    Text(text = "Tentar novamente")
                }
            }
        }
    }
}
