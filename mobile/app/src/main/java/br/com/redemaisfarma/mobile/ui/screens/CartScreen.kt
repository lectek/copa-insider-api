package br.com.redemaisfarma.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.redemaisfarma.mobile.ui.components.EmptyState
import br.com.redemaisfarma.mobile.ui.components.ErrorState
import br.com.redemaisfarma.mobile.ui.components.SimpleCard
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.formatCpf
import br.com.redemaisfarma.mobile.ui.viewmodel.CartViewModel
import br.com.redemaisfarma.mobile.ui.viewmodel.CheckoutViewModel
import kotlinx.coroutines.launch

@Composable
fun CartScreen(paddingValues: PaddingValues) {
    val viewModel: CartViewModel = hiltViewModel()
    val checkoutViewModel: CheckoutViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val resumoState by checkoutViewModel.resumoState.collectAsState()
    val finalizarState by checkoutViewModel.finalizarState.collectAsState()
    val nomeState = remember { mutableStateOf("") }
    val cpfState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf<String?>(null) }
    val cpfError = remember { mutableStateOf<String?>(null) }
    val pagamentoState = remember { mutableStateOf("") }
    val snackState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackState) }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(scaffoldPadding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Carrinho", style = MaterialTheme.typography.headlineSmall)
            when (val current = state) {
                UiState.Idle -> Text(text = "Carregando carrinho...", style = MaterialTheme.typography.bodyMedium)
                UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> {
                    val items = current.data.items
                    if (items.isEmpty()) {
                        EmptyState(message = "Carrinho vazio.")
                    } else {
                        items.forEach { item ->
                            SimpleCard(
                                title = item.nome ?: "Produto",
                                lines = listOf("Qtd: ${item.quantidade ?: 0}"),
                                actionLabel = "Remover",
                                onAction = { item.produtoId?.let(viewModel::removerItem) }
                            )
                        }
                        Text(
                            text = "Total: ${current.data.total ?: 0.0}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                is UiState.Error -> {
                    ErrorState(message = current.message, label = "Carrinho")
                    Button(onClick = { viewModel.carregarCarrinho() }) {
                        Text(text = "Tentar novamente")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Checkout", style = MaterialTheme.typography.titleMedium)
            when (val current = resumoState) {
                UiState.Idle -> Text(text = "Carregando resumo...", style = MaterialTheme.typography.bodyMedium)
                UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> {
                    Text(
                        text = "Total: ${current.data.carrinho.total ?: 0.0}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(text = "Metodo de pagamento", style = MaterialTheme.typography.bodyMedium)
                    current.data.metodosPagamento.forEach { metodo ->
                        val selected = pagamentoState.value == metodo.value
                        if (selected) {
                            Button(
                                onClick = { pagamentoState.value = metodo.value },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(text = "Selecionado: ${metodo.label}")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { pagamentoState.value = metodo.value },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(text = metodo.label)
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorState(message = current.message, label = "Checkout")
                    Button(onClick = { checkoutViewModel.carregarResumo() }) {
                        Text(text = "Recarregar resumo")
                    }
                }
            }

            OutlinedTextField(
                value = nomeState.value,
                onValueChange = { nomeState.value = it },
                label = { Text(text = "Nome") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cpfState.value,
                onValueChange = { cpfState.value = formatCpf(it) },
                label = { Text(text = "CPF") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = cpfError.value != null,
                supportingText = {
                    cpfError.value?.let { Text(text = it) }
                }
            )
            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                label = { Text(text = "Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError.value != null,
                supportingText = {
                    emailError.value?.let { Text(text = it) }
                }
            )

            Button(
                onClick = {
                    val cpfDigits = cpfState.value.filter(Char::isDigit)
                    val cpfOk = cpfDigits.length == 11
                    val emailOk = emailState.value.contains("@")
                    cpfError.value = if (!cpfOk) "CPF invalido" else null
                    emailError.value = if (!emailOk) "Email invalido" else null
                    if (cpfOk && emailOk && pagamentoState.value.isNotBlank()) {
                        checkoutViewModel.finalizar(
                            nome = nomeState.value,
                            cpf = cpfDigits,
                            email = emailState.value,
                            pagamento = pagamentoState.value
                        )
                    } else {
                        scope.launch {
                            snackState.showSnackbar("Preencha os dados obrigatorios.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(text = "Finalizar pedido")
            }

            when (val current = finalizarState) {
                UiState.Idle -> Unit
                UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> {
                    Text(
                        text = "Pedido #${current.data.pedidoId} confirmado.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is UiState.Error -> {
                    ErrorState(message = current.message, label = "Finalizacao")
                }
            }
        }
    }
}
