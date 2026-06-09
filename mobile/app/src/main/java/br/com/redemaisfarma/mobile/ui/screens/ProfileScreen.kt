package br.com.redemaisfarma.mobile.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.redemaisfarma.mobile.ui.components.ErrorState
import br.com.redemaisfarma.mobile.ui.state.UiState
import br.com.redemaisfarma.mobile.ui.util.formatCpf
import br.com.redemaisfarma.mobile.ui.util.formatTelefone
import br.com.redemaisfarma.mobile.ui.viewmodel.ProfileViewModel
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(paddingValues: PaddingValues) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val avatarState by viewModel.avatarState.collectAsState()
    val nomeState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val cpfState = remember { mutableStateOf("") }
    val telefoneState = remember { mutableStateOf("") }
    val enderecoState = remember { mutableStateOf("") }
    val telefoneError = remember { mutableStateOf<String?>(null) }
    val emailError = remember { mutableStateOf<String?>(null) }
    val cpfError = remember { mutableStateOf<String?>(null) }
    val dialogOpen = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.uploadAvatar(context.contentResolver, uri)
        }
    }

    Column(
        modifier = Modifier.padding(paddingValues).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Minha conta", style = MaterialTheme.typography.headlineSmall)
        when (val current = state) {
            UiState.Idle -> {
                Text(text = "Carregando perfil...", style = MaterialTheme.typography.bodyMedium)
            }
            UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Success -> {
                if (nomeState.value.isBlank()) {
                    nomeState.value = current.data.nome
                    emailState.value = current.data.email
                    cpfState.value = current.data.cpf ?: ""
                    telefoneState.value = current.data.telefone ?: ""
                    enderecoState.value = current.data.endereco ?: ""
                }
                if (!current.data.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = current.data.avatarUrl,
                        contentDescription = "Avatar do cliente",
                        modifier = Modifier.height(96.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { imagePicker.launch("image/*") }) {
                        Text(text = "Enviar foto")
                    }
                    Button(onClick = { dialogOpen.value = true }) {
                        Text(text = "Editar dados")
                    }
                }
                Text(text = current.data.nome, style = MaterialTheme.typography.titleMedium)
                Text(text = current.data.email, style = MaterialTheme.typography.bodyMedium)
                current.data.telefone?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
                current.data.endereco?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            }
            is UiState.Error -> {
                ErrorState(message = current.message, label = "Perfil")
                Button(onClick = { viewModel.carregarPerfil() }) {
                    Text(text = "Tentar novamente")
                }
            }
        }

        when (val current = avatarState) {
            UiState.Idle -> Unit
            UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> Text(text = "Foto atualizada.", style = MaterialTheme.typography.bodyMedium)
            is UiState.Error -> ErrorState(message = current.message, label = "Avatar")
        }

        if (dialogOpen.value) {
            AlertDialog(
                onDismissRequest = { dialogOpen.value = false },
                confirmButton = {
                    Button(onClick = {
                        val telefoneDigits = telefoneState.value.filter(Char::isDigit)
                        val cpfDigits = cpfState.value.filter(Char::isDigit)
                        val emailOk = emailState.value.contains("@")
                        val cpfOk = cpfDigits.isEmpty() || cpfDigits.length == 11
                        val telefoneOk = telefoneDigits.isEmpty() || telefoneDigits.length in 10..11
                        telefoneError.value = if (!telefoneOk) "Telefone invalido" else null
                        emailError.value = if (!emailOk) "Email invalido" else null
                        cpfError.value = if (!cpfOk) "CPF invalido" else null
                        if (!telefoneOk || !emailOk || !cpfOk) return@Button
                        viewModel.atualizarPerfil(
                            nome = nomeState.value,
                            email = emailState.value,
                            cpf = cpfDigits,
                            telefone = telefoneState.value.filter(Char::isDigit),
                            endereco = enderecoState.value
                        )
                        dialogOpen.value = false
                    }) {
                        Text(text = "Salvar")
                    }
                },
                dismissButton = {
                    Button(onClick = { dialogOpen.value = false }) {
                        Text(text = "Cancelar")
                    }
                },
                title = { Text(text = "Editar dados") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = nomeState.value,
                            onValueChange = { nomeState.value = it },
                            label = { Text(text = "Nome") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = emailState.value,
                            onValueChange = { emailState.value = it },
                            label = { Text(text = "Email") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailError.value != null,
                            supportingText = {
                                emailError.value?.let { Text(text = it) }
                            }
                        )
                        OutlinedTextField(
                            value = cpfState.value,
                            onValueChange = { cpfState.value = formatCpf(it) },
                            label = { Text(text = "CPF") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = cpfError.value != null,
                            supportingText = {
                                cpfError.value?.let { Text(text = it) }
                            }
                        )
                        OutlinedTextField(
                            value = telefoneState.value,
                            onValueChange = { telefoneState.value = formatTelefone(it) },
                            label = { Text(text = "Telefone") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = telefoneError.value != null,
                            supportingText = {
                                telefoneError.value?.let { Text(text = it) }
                            }
                        )
                        OutlinedTextField(
                            value = enderecoState.value,
                            onValueChange = { enderecoState.value = it },
                            label = { Text(text = "Endereco") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }

        when (val current = updateState) {
            UiState.Idle -> Unit
            UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> Text(text = "Dados atualizados.", style = MaterialTheme.typography.bodyMedium)
            is UiState.Error -> ErrorState(message = current.message, label = "Perfil")
        }
    }
}
