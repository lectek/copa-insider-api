package br.com.redemaisfarma.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.redemaisfarma.mobile.ui.components.DetailRow
import br.com.redemaisfarma.mobile.ui.components.ErrorState
import br.com.redemaisfarma.mobile.ui.components.TagRow
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.viewmodel.CartViewModel
import br.com.redemaisfarma.mobile.ui.viewmodel.ProductDetailViewModel
import coil.compose.AsyncImage

@Composable
fun ProductDetailScreen(paddingValues: PaddingValues, productId: Long) {
    val viewModel: ProductDetailViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(productId) {
        viewModel.carregarProduto(productId)
    }

    Column(
        modifier = Modifier.padding(paddingValues).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (val current = state) {
            UiState.Idle -> Text(text = "Carregando produto...", style = MaterialTheme.typography.bodyMedium)
            UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> {
                val produto = current.data
                Text(text = produto.nome ?: "Produto", style = MaterialTheme.typography.headlineSmall)
                if (!produto.imagem.isNullOrBlank()) {
                    AsyncImage(
                        model = produto.imagem,
                        contentDescription = produto.nome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .aspectRatio(4f / 3f)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Sem imagem"
                        )
                        Text(text = "Sem imagem")
                    }
                }
                produto.descricao?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                produto.preco?.let {
                    Text(text = "R$ $it", style = MaterialTheme.typography.titleMedium)
                }
                produto.estoqueAtual?.let {
                    DetailRow(label = "Estoque", value = it.toString())
                    if (it <= 10) {
                        Text(text = "Estoque baixo", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                produto.marca?.let { DetailRow(label = "Marca", value = it) }
                produto.categoria?.let { DetailRow(label = "Categoria", value = it) }
                produto.codigoBarras?.let { DetailRow(label = "Codigo de barras", value = it) }
                TagRow(tags = produto.tags ?: emptyList())
                Button(
                    onClick = {
                        val id = produto.entityId ?: productId
                        if (id > 0) {
                            cartViewModel.adicionarItem(id, 1)
                        }
                    },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(text = "Adicionar ao carrinho")
                }
            }
            is UiState.Error -> {
                ErrorState(message = current.message, label = "Produto")
                Button(onClick = { viewModel.carregarProduto(productId) }) {
                    Text(text = "Tentar novamente")
                }
            }
        }
    }
}
