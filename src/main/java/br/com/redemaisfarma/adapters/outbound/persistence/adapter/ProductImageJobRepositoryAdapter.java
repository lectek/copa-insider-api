/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository
 *  br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository$Job
 *  br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository$Status
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Repository
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.adapters.outbound.persistence.adapter;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductImageJobEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProductImageJobJpaRepository;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductImageJobRepositoryAdapter
implements ProductImageJobRepository {
    private static final int RESULT_URL_MAX_LENGTH = 512;
    private static final int ERROR_MSG_MAX_LENGTH = 512;
    private final ProductImageJobJpaRepository jpa;

    public ProductImageJobRepositoryAdapter(ProductImageJobJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Transactional
    public ProductImageJobRepository.Job createQueued(Long productId, String fingerprint) {
        ProductImageJobEntity e = new ProductImageJobEntity();
        e.setProductId(productId);
        e.setStatus(ProductImageJobRepository.Status.QUEUED.name());
        e.setFingerprint(fingerprint);
        e = (ProductImageJobEntity)this.jpa.save(e);
        return this.map(e);
    }

    @Transactional
    public void markRunning(Long jobId) {
        this.update(jobId, ProductImageJobRepository.Status.RUNNING, null, null);
    }

    @Transactional
    public void markDone(Long jobId, String resultUrl) {
        this.update(jobId, ProductImageJobRepository.Status.DONE, resultUrl, null);
    }

    @Transactional
    public void markError(Long jobId, String errorMsg) {
        this.update(jobId, ProductImageJobRepository.Status.ERROR, null, errorMsg);
    }

    @Transactional
    public void markSkipped(Long jobId, String reason) {
        this.update(jobId, ProductImageJobRepository.Status.SKIPPED, null, reason);
    }

    public Optional<ProductImageJobRepository.Job> findLastByProduct(Long productId) {
        ProductImageJobEntity e = this.jpa.findTopByProductIdOrderByIdDesc(productId);
        return Optional.ofNullable(e).map(this::map);
    }

    public List<ProductImageJobRepository.Job> findByStatus(ProductImageJobRepository.Status status, int limit, int offset) {
        int size = Math.max(1, limit);
        int pageIndex = Math.max(0, offset / size);
        PageRequest pageReq = PageRequest.of((int)pageIndex, (int)size);
        return this.jpa.findByStatusOrderByCreatedAtAsc(status.name(), (Pageable)pageReq).getContent().stream().map(this::map).toList();
    }

    public boolean existsByProductIdAndFingerprint(Long productId, String fingerprint) {
        return this.jpa.existsByProductIdAndFingerprint(productId, fingerprint);
    }

    private void update(Long id, ProductImageJobRepository.Status st, String url, String err) {
        ProductImageJobEntity e = (ProductImageJobEntity)this.jpa.findById(id).orElseThrow();
        e.setStatus(st.name());
        if (url != null) {
            e.setResultUrl(this.truncate(url, RESULT_URL_MAX_LENGTH));
        }
        if (err != null) {
            e.setErrorMsg(this.truncate(err, ERROR_MSG_MAX_LENGTH));
        }
        this.jpa.save(e);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private ProductImageJobRepository.Job map(ProductImageJobEntity e) {
        ProductImageJobRepository.Status st;
        try {
            st = e.getStatus() != null ? ProductImageJobRepository.Status.valueOf((String)e.getStatus()) : ProductImageJobRepository.Status.QUEUED;
        }
        catch (IllegalArgumentException ex) {
            st = ProductImageJobRepository.Status.ERROR;
        }
        return new ProductImageJobRepository.Job(e.getId(), e.getProductId(), st, e.getResultUrl(), e.getErrorMsg(), e.getFingerprint(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
