package br.com.redemaisfarma.mobile.data.model

data class ClienteMeResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val cpf: String?,
    val telefone: String?,
    val endereco: String?,
    val avatarUrl: String?
)

data class ClienteMeUpdateRequest(
    val nome: String,
    val email: String,
    val cpf: String?,
    val telefone: String?,
    val endereco: String?
)

data class AvatarResponse(val avatarUrl: String)

data class LoginRequestPayload(
    val usuario: String,
    val senha: String,
    val lembrarMe: Boolean = true,
    val tenantId: String = "rede-mais-farma"
)

data class LoginResponsePayload(
    val accessToken: String?,
    val refreshToken: String?,
    val userId: Long?,
    val fullName: String?,
    val email: String?,
    val permissions: List<String>?,
    val expiresAt: String?,
    val tenantId: String?
)

data class PedidoResumoResponse(
    val id: Long,
    val data: String?,
    val total: Double?,
    val status: String,
    val metodoPagamento: String?
)

data class PedidoItemResponse(
    val produtoId: Long?,
    val produto: String?,
    val quantidade: Int?,
    val subtotal: Double?
)

data class PedidoDetalheResponse(
    val id: Long,
    val data: String?,
    val total: Double?,
    val status: String,
    val metodoPagamento: String?,
    val itens: List<PedidoItemResponse>
)

data class CartItemRequest(val produtoId: Long, val quantidade: Int)

data class CartUpdateRequest(val quantidade: Int)

data class CartItemResponse(
    val produtoId: Long?,
    val nome: String?,
    val imagem: String?,
    val preco: Double?,
    val quantidade: Int?,
    val subtotal: Double?,
    val invalido: Boolean?,
    val motivo: String?,
    val estoque: Int?
)

data class CartSummaryResponse(
    val items: List<CartItemResponse>,
    val subtotal: Double?,
    val total: Double?,
    val hasInvalidItems: Boolean?
)

data class PaymentMethodResponse(
    val value: String,
    val label: String,
    val type: String?
)

data class CheckoutResumoResponse(
    val carrinho: CartSummaryResponse,
    val metodosPagamento: List<PaymentMethodResponse>
)

data class CheckoutFinalizarRequest(
    val nome: String,
    val cpf: String,
    val email: String,
    val pagamento: String
)

data class CheckoutFinalizarResponse(
    val pedidoId: Long,
    val paymentMethodLabel: String?
)

data class FavoritoRequest(val produtoId: Long)

data class FavoritoResponse(
    val produtoId: Long?,
    val nome: String?,
    val imagem: String?,
    val preco: Double?,
    val createdAt: String?
)

data class NotificacaoResponse(
    val id: Long,
    val tipo: String?,
    val titulo: String?,
    val mensagem: String?,
    val lida: Boolean?,
    val createdAt: String?
)

data class NotificacoesResponse(
    val items: List<NotificacaoResponse>,
    val unreadCount: Long
)

data class NotificacaoLidasRequest(val ids: List<Long>)

data class ProdutoResponse(
    val entityId: Long?,
    val nome: String?,
    val descricao: String?,
    val preco: Double?,
    val imagem: String?,
    val categoria: String?,
    val estoqueAtual: Int?,
    val validade: String?,
    val codigoBarras: String?,
    val marca: String?,
    val fornecedor: String?,
    val quantidadeVendida: Long?,
    val dataCadastro: String?,
    val dataAtualizacao: String?,
    val produtoDestaque: Boolean?,
    val produtoRecomendadoIA: Boolean?,
    val produtoControlado: Boolean?,
    val avaliacaoMedia: Double?,
    val tags: List<String>?,
    val situacao: String?
)

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long?,
    val totalPages: Int?,
    val number: Int?,
    val size: Int?
)
