package br.com.redemaisfarma.mobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun EmptyState(message: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Filled.Info, contentDescription = "Info")
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorState(message: String, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Filled.Info, contentDescription = "Erro")
        Text(text = "$label: $message", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SimpleCard(
    title: String,
    lines: List<String>,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            lines.filter { it.isNotBlank() }.forEach {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction, modifier = Modifier.height(48.dp)) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    title: String,
    priceLabel: String?,
    imageUrl: String?,
    isFavorite: Boolean,
    lowStock: Boolean = false,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onToggleFavorite, modifier = Modifier.height(48.dp)) {
                    if (isFavorite) {
                        Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Favorito")
                    } else {
                        Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = "Favoritar")
                    }
                }
            }
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .aspectRatio(16f / 9f)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Image, contentDescription = "Sem imagem")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Sem imagem")
                }
            }
            priceLabel?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            if (lowStock) {
                Text(text = "Estoque baixo", style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = onOpen, modifier = Modifier.height(48.dp)) {
                Text(text = "Ver detalhes")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TagRow(tags: List<String>) {
    if (tags.isEmpty()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        tags.take(4).forEach { tag ->
            Card(modifier = Modifier.padding(0.dp)) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
