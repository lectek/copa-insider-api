package br.com.redemaisfarma.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.redemaisfarma.mobile.ui.components.EmptyState
import br.com.redemaisfarma.mobile.ui.components.ErrorState
import br.com.redemaisfarma.mobile.ui.components.ProductCard
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.viewmodel.CatalogViewModel
import br.com.redemaisfarma.mobile.ui.viewmodel.FavoritosViewModel
import kotlinx.coroutines.delay

@Composable
fun CatalogScreen(paddingValues: androidx.compose.foundation.layout.PaddingValues, onOpenProduct: (Long) -> Unit) {
    val catalogViewModel: CatalogViewModel = hiltViewModel()
    val favoritosViewModel: FavoritosViewModel = hiltViewModel()
    val catalogState by catalogViewModel.state.collectAsState()
    val loadingMore by catalogViewModel.loadingMore.collectAsState()
    val favoritosState by favoritosViewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }
    val favoritosIds = when (val current = favoritosState) {
        is UiState.Success -> current.data
        else -> emptySet()
    }

    Column(
        modifier = Modifier.padding(paddingValues).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LaunchedEffect(query) {
            val term = query.trim()
            if (term.isEmpty()) {
                catalogViewModel.carregarCatalogo()
                return@LaunchedEffect
            }
            delay(400)
            if (term.length >= 2) {
                catalogViewModel.carregarCatalogo(term)
            }
        }

        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(text = "Buscar produto") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { catalogViewModel.carregarCatalogo(query) },
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = "Buscar")
            }
            OutlinedButton(
                onClick = {
                    query = ""
                    catalogViewModel.carregarCatalogo()
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = "Limpar")
            }
        }

        when (val current = catalogState) {
            UiState.Idle -> Text(text = "Carregando catalogo...", style = MaterialTheme.typography.bodyMedium)
            UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    EmptyState(message = "Nenhum produto encontrado.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(current.data) { index, produto ->
                            val id = produto.entityId ?: 0L
                        ProductCard(
                            title = produto.nome ?: "Produto",
                            priceLabel = produto.preco?.let { "R$ $it" },
                            imageUrl = produto.imagem,
                            isFavorite = favoritosIds.contains(id),
                            lowStock = (produto.estoqueAtual ?: 0) in 1..10,
                            onToggleFavorite = {
                                if (id > 0) {
                                    val favorito = favoritosIds.contains(id)
                                    favoritosViewModel.toggleFavorito(id, favorito)
                                    }
                                },
                                onOpen = { if (id > 0) onOpenProduct(id) }
                            )
                            if (index == current.data.lastIndex) {
                                catalogViewModel.carregarMais()
                            }
                        }
                        if (loadingMore) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                    Text(
                                        text = "Carregando mais...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorState(message = current.message, label = "Catalogo")
                Button(onClick = { catalogViewModel.carregarCatalogo() }) {
                    Text(text = "Tentar novamente")
                }
            }
        }
    }
}
