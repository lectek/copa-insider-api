package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.ProductCardVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutoQueryServiceImplTest {

    private ProdutoJpaRepository repo;
    private AppSettingService settings;
    private ProdutoQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = mock(ProdutoJpaRepository.class);
        settings = mock(AppSettingService.class);
        service = new ProdutoQueryServiceImpl(repo, settings);
    }

    @Test
    void featuredComIdsConfiguradosFiltraSomenteProdutosPublicos() {
        when(settings.getOrDefault("home.featured.ids", "")).thenReturn("10,20");
        when(repo.findAllPublicByIdIn(List.of(10L, 20L)))
                .thenReturn(List.of(produto(20L, "B"), produto(10L, "A")));

        List<ProductCardVM> resultado = service.featured(PageRequest.of(0, 8), false);

        assertThat(resultado).extracting(ProductCardVM::id).containsExactly(10L, 20L);
        verify(repo).findAllPublicByIdIn(List.of(10L, 20L));
        verify(repo, never()).findAllDisponiveisByIdIn(anyCollection());
        verify(repo, never()).findAllByIdIn(anyCollection());
    }

    @Test
    void featuredComFlagIncluirIndisponiveisUsaConsultaSemFiltroPublico() {
        when(settings.getOrDefault("home.featured.ids", "")).thenReturn("10,20");
        when(repo.findAllByIdIn(List.of(10L, 20L)))
                .thenReturn(List.of(produto(10L, "A"), produto(20L, "B")));

        List<ProductCardVM> resultado = service.featured(PageRequest.of(0, 8), true);

        assertThat(resultado).extracting(ProductCardVM::id).containsExactly(10L, 20L);
        verify(repo).findAllByIdIn(List.of(10L, 20L));
        verify(repo, never()).findAllPublicByIdIn(anyCollection());
    }

    @Test
    void recommendedAceitaListaImutavelSemEstourarShuffle() {
        when(repo.findRecent(PageRequest.of(0, 6)))
                .thenReturn(List.of(
                        produto(10L, "A"),
                        produto(20L, "B"),
                        produto(30L, "C")
                ));

        List<ProductCardVM> resultado = service.recommended(PageRequest.of(0, 2), true);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(ProductCardVM::id)
                .allMatch(id -> List.of(10L, 20L, 30L).contains(id));
    }

    @Test
    void featuredSemIdsNemCarrosselCaiParaTopSellersSemUsarVitrineFallback() {
        when(settings.getOrDefault("home.featured.ids", "")).thenReturn("");
        when(repo.findCarrossel(PageRequest.of(0, 8))).thenReturn(List.of());
        when(repo.searchPublicPage(null, PageRequest.of(0, 8, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "estoque"))))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(
                        produto(30L, "C"),
                        produto(40L, "D")
                )));

        List<ProductCardVM> resultado = service.featured(PageRequest.of(0, 8), false);

        assertThat(resultado).extracting(ProductCardVM::id).containsExactly(30L, 40L);
        verify(repo, never()).findVitrineFallback(PageRequest.of(0, 8));
    }

    private static ProdutoEntity produto(Long id, String nome) {
        ProdutoEntity e = new ProdutoEntity();
        e.setId(id);
        e.setNome(nome);
        e.setPrecoVenda(BigDecimal.TEN);
        e.setDisponivel(true);
        e.setEstoque(10);
        return e;
    }
}
