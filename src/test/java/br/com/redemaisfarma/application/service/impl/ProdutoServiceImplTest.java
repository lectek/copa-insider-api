package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.application.service.ProductCategoryBindingService;
import br.com.redemaisfarma.domain.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceImplTest {

    @Mock
    private ProdutoJpaRepository repo;
    @Mock
    private ProductCategoryBindingService categoryBindingService;

    private ProdutoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProdutoServiceImpl(repo, categoryBindingService);
    }

    @Test
    void createAssignsManualSourceWhenMapperLeavesItNull() {
        Produto input = new Produto();
        input.setNome("Dipirona");
        input.setDescricao("500mg");

        when(repo.save(any(ProdutoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(input);

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getMetodoLeituraCodigoBarras())
                .isEqualTo(MetodoLeituraCodigoBarras.MANUAL);
    }

    @Test
    void updateAssignsManualSourceOnlyWhenCurrentEntityHasNoSource() {
        ProdutoEntity atual = new ProdutoEntity();
        atual.setId(42L);
        atual.setNome("Vitamina C");
        atual.setMetodoLeituraCodigoBarras(null);

        Produto input = new Produto();
        input.setNome("Vitamina C 1g");

        when(repo.findById(42L)).thenReturn(Optional.of(atual));
        when(repo.save(any(ProdutoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(42L, input);

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getMetodoLeituraCodigoBarras())
                .isEqualTo(MetodoLeituraCodigoBarras.MANUAL);
    }

    @Test
    void updatePreservesExistingSourceWhenAlreadyDefined() {
        ProdutoEntity atual = new ProdutoEntity();
        atual.setId(43L);
        atual.setNome("Pomada");
        atual.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.CSV_ESTOQUE);

        Produto input = new Produto();
        input.setDescricao("Uso topico");

        when(repo.findById(43L)).thenReturn(Optional.of(atual));
        when(repo.save(any(ProdutoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.update(43L, input);

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getMetodoLeituraCodigoBarras())
                .isEqualTo(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
    }

    @Test
    void fromLegacyMarksEntityAsLegacy() {
        ProdutoLegacyEntity legacy = new ProdutoLegacyEntity();
        legacy.setId(7);
        legacy.setNome("Paracetamol");
        legacy.setApresentacao("750mg");
        legacy.setCodigoBarras("7891234567890");
        legacy.setPrecoVenda(new BigDecimal("12.90"));
        legacy.setSaldo(new BigDecimal("5"));

        ProdutoEntity entity = ProdutoMapper.fromLegacy(legacy);

        assertThat(entity.getMetodoLeituraCodigoBarras())
                .isEqualTo(MetodoLeituraCodigoBarras.LEGADO);
    }

    @Test
    void createDelegatesCategoryBindingBeforeSave() {
        Produto input = new Produto();
        input.setNome("Dipirona");
        input.setCategoria(" Medicamentos ");
        doAnswer(invocation -> {
            ProdutoEntity entity = invocation.getArgument(0);
            entity.setCategoria("Medicamentos");
            return null;
        }).when(categoryBindingService).bind(any(ProdutoEntity.class));
        when(repo.save(any(ProdutoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(input);

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getCategoria()).isEqualTo("Medicamentos");
        verify(categoryBindingService).bind(captor.getValue());
    }
}
