package br.com.lectek.copainsider.domain.catalogo;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.view.ProductCardVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProdutoQueryServiceImpl implements ProdutoQueryService {
    private final ProdutoJpaRepository repo;
    private final AppSettingService settings;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public ProdutoQueryServiceImpl(ProdutoJpaRepository repo, AppSettingService settings) {
        this.repo = repo;
        this.settings = settings;
    }

    @Override
    public List<ProductCardVM> topSellers(Pageable page, boolean incluirIndisponiveis) {
        Sort sort = Sort.by(Sort.Direction.DESC, "estoque");
        PageRequest p = PageRequest.of(page.getPageNumber(), page.getPageSize(), sort);
        final Long tenantId = resolveTenantId();
        Page<ProdutoEntity> slice = incluirIndisponiveis
                ? (tenantId == null ? repo.findAll(p) : repo.searchPage(tenantId, null, p))
                : (tenantId == null ? repo.searchPublicPage(null, p) : repo.searchPublicPage(tenantId, null, p));

        List<ProdutoEntity> list = slice.getContent();
        return ProductCardVM.fromList(list);
    }

    @Override
    public List<ProductCardVM> newArrivals(Pageable page, boolean incluirIndisponiveis) {
        List<ProdutoEntity> list;
        final Long tenantId = resolveTenantId();
        if (incluirIndisponiveis) {
            list = tenantId == null ? repo.findRecent(page) : repo.findRecent(tenantId, page);
        } else {
            PageRequest publicPage = PageRequest.of(
                    page.getPageNumber(),
                    page.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "dataCadastro")
                            .and(Sort.by(Sort.Direction.DESC, "id"))
            );
            list = (tenantId == null ? repo.searchPublicPage(null, publicPage) : repo.searchPublicPage(tenantId, null, publicPage))
                    .getContent();
        }
        return ProductCardVM.fromList(list);
    }

    @Override
    public List<ProductCardVM> featured(Pageable page, boolean incluirIndisponiveis) {
        String csvIds = settings.getOrDefault("home.featured.ids", "");
        List<Long> ids = parseIds(csvIds);
        final Long tenantId = resolveTenantId();

        if (!ids.isEmpty()) {
            List<ProdutoEntity> produtos = incluirIndisponiveis
                    ? (tenantId == null ? repo.findAllByIdIn(ids) : repo.findAllByTenantIdIn(tenantId, ids))
                    : (tenantId == null ? repo.findAllPublicByIdIn(ids) : repo.findAllPublicByIdIn(tenantId, ids));
            Map<Long, ProdutoEntity> byId = produtos.stream().collect(Collectors.toMap(ProdutoEntity::getId, x -> x));

            List<ProductCardVM> out = new ArrayList<>();
            for (Long id : ids) {
                ProdutoEntity p = byId.get(id);
                if (p != null) out.add(ProductCardVM.of(p));
                if (out.size() >= page.getPageSize()) break;
            }
            if (!out.isEmpty()) return out;
        }

        List<ProdutoEntity> carrossel = tenantId == null ? repo.findCarrossel(page) : repo.findCarrossel(tenantId, page);
        if (!carrossel.isEmpty()) return ProductCardVM.fromList(carrossel);

        return topSellers(page, incluirIndisponiveis);
    }

    @Override
    public List<ProductCardVM> recommended(Pageable page, boolean incluirIndisponiveis) {
        int size = Math.max(1, page.getPageSize());
        final Long tenantId = resolveTenantId();
        List<ProdutoEntity> base = new ArrayList<>(incluirIndisponiveis
                ? (tenantId == null
                    ? repo.findRecent(PageRequest.of(0, size * 3))
                    : repo.findRecent(tenantId, PageRequest.of(0, size * 3)))
                : (tenantId == null
                    ? repo.searchPublicPage(
                        null,
                        PageRequest.of(
                            0,
                            size * 3,
                            Sort.by(Sort.Direction.DESC, "dataCadastro")
                                .and(Sort.by(Sort.Direction.DESC, "id"))
                        )
                    )
                    : repo.searchPublicPage(
                        tenantId,
                        null,
                        PageRequest.of(
                            0,
                            size * 3,
                            Sort.by(Sort.Direction.DESC, "dataCadastro")
                                .and(Sort.by(Sort.Direction.DESC, "id"))
                        )
                    )).getContent());
        Collections.shuffle(base);
        return ProductCardVM.fromList(base.stream().limit(size).toList());
    }

    @Override
    public Optional<ProductCardVM> findById(Long id, boolean incluirIndisponiveis) {
        if (id == null) return Optional.empty();
        final Long tenantId = resolveTenantId();
        Optional<ProdutoEntity> opt = incluirIndisponiveis
                ? (tenantId == null ? repo.findById(id) : repo.findByScopedId(tenantId, id))
                : (tenantId == null ? repo.findPublicById(id) : repo.findPublicById(tenantId, id));
        if (opt.isEmpty()) return Optional.empty();
        ProdutoEntity p = opt.get();
        return Optional.of(ProductCardVM.of(p));
    }

    private List<Long> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    try { return Long.parseLong(s); }
                    catch (NumberFormatException e) { return null; }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
