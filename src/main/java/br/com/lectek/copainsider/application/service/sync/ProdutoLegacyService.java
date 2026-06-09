/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.application.dto.legacy.ProdutoLegacyDTO
 *  br.com.lectek.copainsider.application.mapper.ProdutoLegacyMapper
 *  lombok.Generated
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.lectek.copainsider.application.service.sync;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.lectek.copainsider.application.dto.legacy.ProdutoLegacyDTO;
import br.com.lectek.copainsider.application.mapper.ProdutoLegacyMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.Generated;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix="app.sync.legacy", name={"enabled"}, havingValue="true")
public class ProdutoLegacyService {
    private final ProdutoLegacyRepository produtoLegacyRepository;
    private final ProdutoLegacyMapper produtoLegacyMapper;

    @Transactional(readOnly=true)
    public List<ProdutoLegacyDTO> buscarTodosComEstoque() {
        List<ProdutoLegacyEntity> produtos = this.produtoLegacyRepository.findBySaldoGreaterThan(BigDecimal.ZERO);
        return produtos.stream().map(arg_0 -> ((ProdutoLegacyMapper)this.produtoLegacyMapper).toDto(arg_0)).toList();
    }

    @Transactional(readOnly=true)
    public Optional<ProdutoLegacyDTO> buscarPorId(Integer id) {
        return this.produtoLegacyRepository.findById(id).map(arg_0 -> ((ProdutoLegacyMapper)this.produtoLegacyMapper).toDto(arg_0));
    }

    @Transactional(readOnly=true)
    public Optional<ProdutoLegacyDTO> buscarPorCodigoBarras(String codigoBarras) {
        return this.produtoLegacyRepository.findByCodigoBarras(codigoBarras).map(arg_0 -> ((ProdutoLegacyMapper)this.produtoLegacyMapper).toDto(arg_0));
    }

    @Transactional(readOnly=true)
    public List<ProdutoLegacyDTO> buscarPorNomeParcial(String nome) {
        return this.produtoLegacyRepository.findByNomeContainingIgnoreCase(nome).stream().map(arg_0 -> ((ProdutoLegacyMapper)this.produtoLegacyMapper).toDto(arg_0)).toList();
    }

    @Generated
    public ProdutoLegacyService(ProdutoLegacyRepository produtoLegacyRepository, ProdutoLegacyMapper produtoLegacyMapper) {
        this.produtoLegacyRepository = produtoLegacyRepository;
        this.produtoLegacyMapper = produtoLegacyMapper;
    }
}

