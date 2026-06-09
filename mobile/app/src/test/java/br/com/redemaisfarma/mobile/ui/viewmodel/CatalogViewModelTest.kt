package br.com.redemaisfarma.mobile.ui.viewmodel

import br.com.redemaisfarma.mobile.data.model.PageResponse
import br.com.redemaisfarma.mobile.data.model.ProdutoResponse
import br.com.redemaisfarma.mobile.data.network.ProdutosPublicApiService
import br.com.redemaisfarma.mobile.data.repository.ProdutosRepository
import br.com.redemaisfarma.mobile.testing.MainDispatcherRule
import br.com.redemaisfarma.mobile.ui.state.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun carregarCatalogo_emiteSucesso() = runTest {
        val viewModel = CatalogViewModel(ProdutosRepository(FakeProdutosPublicApiService()))
        viewModel.carregarCatalogo()
        advanceUntilIdle()
        val state = viewModel.state.value
        assertTrue(state is UiState.Success && state.data.size == 1)
    }
}

private class FakeProdutosPublicApiService : ProdutosPublicApiService {
    override suspend fun listarProdutos(
        page: Int,
        size: Int,
        query: String?,
        sort: String,
        dir: String
    ): PageResponse<ProdutoResponse> {
        return PageResponse(
            content = listOf(
                ProdutoResponse(
                    entityId = 1L,
                    nome = "Dipirona 500mg",
                    descricao = null,
                    preco = 12.5,
                    imagem = null,
                    categoria = null,
                    estoqueAtual = null,
                    validade = null,
                    codigoBarras = null,
                    marca = null,
                    fornecedor = null,
                    quantidadeVendida = null,
                    dataCadastro = null,
                    dataAtualizacao = null,
                    produtoDestaque = null,
                    produtoRecomendadoIA = null,
                    produtoControlado = null,
                    avaliacaoMedia = null,
                    tags = null,
                    situacao = null
                )
            ),
            totalElements = 1,
            totalPages = 1,
            number = page,
            size = size
        )
    }

    override suspend fun destaques(limit: Int): List<ProdutoResponse> = emptyList()

    override suspend fun obter(id: Long): ProdutoResponse {
        return ProdutoResponse(
            entityId = id,
            nome = "Produto",
            descricao = null,
            preco = null,
            imagem = null,
            categoria = null,
            estoqueAtual = null,
            validade = null,
            codigoBarras = null,
            marca = null,
            fornecedor = null,
            quantidadeVendida = null,
            dataCadastro = null,
            dataAtualizacao = null,
            produtoDestaque = null,
            produtoRecomendadoIA = null,
            produtoControlado = null,
            avaliacaoMedia = null,
            tags = null,
            situacao = null
        )
    }
}
