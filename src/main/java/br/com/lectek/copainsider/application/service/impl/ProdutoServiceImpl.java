package br.com.lectek.copainsider.application.service.impl;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.dto.request.CadastroProdutoRequestDTO;
import br.com.lectek.copainsider.application.mapper.ProdutoMapper;
import br.com.lectek.copainsider.application.service.ProdutoService;
import br.com.lectek.copainsider.application.service.ProductCategoryBindingService;
import br.com.lectek.copainsider.domain.Produto;
import lombok.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Primary
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoJpaRepository repo;
    private final ProductCategoryBindingService categoryBindingService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    @Override
    @Transactional(readOnly = true)
    public Produto findById(Long id) {
        final Long tenantId = resolveTenantId();
        ProdutoEntity e = (tenantId == null
                ? repo.findById(id)
                : repo.findByScopedId(tenantId, id))
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        return ProdutoMapper.toDomain(e);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> list() {
        final Long tenantId = resolveTenantId();
        final List<ProdutoEntity> produtos = tenantId == null
                ? repo.findAll()
                : repo.findByTenantId(tenantId, Pageable.unpaged()).getContent();
        return produtos.stream()
                .map(ProdutoMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Produto create(Produto produto) {
        ProdutoEntity e = ProdutoMapper.toEntity(produto);
        e.setId(null);
        if (e.getStatus() == null) e.setStatus(ProdutoStatus.IMPORTADO);
        if (e.getDataImportacao() == null) e.setDataImportacao(LocalDateTime.now());
        final Long tenantId = resolveTenantId();
        if (tenantId != null) {
            e.setTenantId(tenantId);
        }
        this.ensureManualSource(e);
        this.categoryBindingService.bind(e);

        ProdutoEntity salvo = repo.save(e);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public Produto update(Long id, Produto produto) {
        final Long tenantId = resolveTenantId();
        ProdutoEntity atual = (tenantId == null
                ? repo.findById(id)
                : repo.findByScopedId(tenantId, id))
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        ProdutoMapper.updateEntity(atual, produto);
        this.ensureManualSource(atual);
        this.categoryBindingService.bind(atual);
        ProdutoEntity salvo = repo.save(atual);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        final Long tenantId = resolveTenantId();
        if (tenantId == null) {
            if (!repo.existsById(id)) {
                throw new IllegalArgumentException("Produto não encontrado: " + id);
            }
            repo.deleteById(id);
            return;
        }
        ProdutoEntity atual = repo.findByScopedId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
        repo.delete(atual);
    }

    @Override
    @Transactional
    public Produto createFromDto(CadastroProdutoRequestDTO dto) {
        Produto domain = new Produto();
        domain.setId(null);
        domain.setNome(dto.getNome());
        domain.setDescricao(dto.getDescricao());
        domain.setPrecoVenda(dto.getPrecoVenda());
        domain.setCategoria(dto.getCategoria());
        domain.setDataCadastro(LocalDateTime.now());

        ProdutoEntity e = ProdutoMapper.toEntity(domain);
        e.setId(null);
        if (e.getStatus() == null) e.setStatus(ProdutoStatus.IMPORTADO);
        e.setDataImportacao(LocalDateTime.now());
        final Long tenantId = resolveTenantId();
        if (tenantId != null) {
            e.setTenantId(tenantId);
        }
        this.ensureManualSource(e);
        this.categoryBindingService.bind(e);

        ProdutoEntity salvo = repo.save(e);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public Produto validar(Long id, String validador) {
        final Long tenantId = resolveTenantId();
        ProdutoEntity entity = (tenantId == null
                ? repo.findById(id)
                : repo.findByScopedId(tenantId, id))
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        entity.setStatus(ProdutoStatus.VALIDADO);
        entity.setValidador(validador);

        ProdutoEntity salvo = repo.save(entity);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional
    public Produto publicar(Long id, String validador) {
        final Long tenantId = resolveTenantId();
        ProdutoEntity entity = (tenantId == null
                ? repo.findById(id)
                : repo.findByScopedId(tenantId, id))
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        entity.setStatus(ProdutoStatus.PUBLICADO);
        entity.setValidador(validador);
        entity.setPublicadoEm(LocalDateTime.now());

        ProdutoEntity salvo = repo.save(entity);
        return ProdutoMapper.toDomain(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produto> listByStatus(ProdutoStatus status) {
        return repo.findByStatus(status, Pageable.unpaged()).stream()
                .map(ProdutoMapper::toDomain)
                .toList();
    }

    private void ensureManualSource(ProdutoEntity entity) {
        if (entity.getMetodoLeituraCodigoBarras() == null) {
            entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        }
    }

    @Generated
    public ProdutoServiceImpl(
            final ProdutoJpaRepository repo,
            final ProductCategoryBindingService categoryBindingService
    ) {
        this.repo = repo;
        this.categoryBindingService = categoryBindingService;
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
