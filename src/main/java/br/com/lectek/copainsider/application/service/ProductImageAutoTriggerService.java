/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.messaging.ProductImageEventPublisher;
import br.com.lectek.copainsider.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImageAutoTriggerService {
    private static final Logger log = LoggerFactory.getLogger(ProductImageAutoTriggerService.class);
    private final ProdutoRepository produtoRepo;
    private final ProductImageEventPublisher publisher;
    private final boolean cronEnabled;
    private final int cronBatchSize;

    public ProductImageAutoTriggerService(ProdutoRepository produtoRepo, ProductImageEventPublisher publisher, @Value(value="${app.ai.image.autogen.cron-enabled:false}") boolean cronEnabled, @Value(value="${app.ai.image.autogen.batch-size:25}") int cronBatchSize) {
        this.produtoRepo = produtoRepo;
        this.publisher = publisher;
        this.cronEnabled = cronEnabled;
        this.cronBatchSize = cronBatchSize;
    }

    @Transactional(readOnly=true)
    public boolean triggerOne(Long productId) {
        return this.produtoRepo.findById(productId).filter(this::semImagem).map(p -> {
            this.publishEvent((ProdutoEntity)p);
            return true;
        }).orElse(false);
    }

    @Transactional(readOnly=true)
    public int triggerBatch(int size) {
        PageRequest pageable = PageRequest.of((int)0, (int)Math.max(1, size));
        Page<ProdutoEntity> page = this.produtoRepo.findSemMidia((Pageable)pageable);
        page.forEach(this::publishEvent);
        return page.getNumberOfElements();
    }

    private boolean semImagem(ProdutoEntity p) {
        String img = p.getImagem();
        return img == null || img.isBlank();
    }

    private void publishEvent(ProdutoEntity p) {
        ProductImageRequestedEvent evt = new ProductImageRequestedEvent(
                p.getId(),
                this.safe(p.getNome()),
                this.safe(p.getFabricante()),
                this.safe(p.getCategoria()),
                ""
        );
        this.publisher.publish(evt);
        log.info("Publicado pedido de imagem para produto {} ({})", (Object)p.getId(), (Object)p.getNome());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Scheduled(fixedDelayString="${app.ai.image.autogen.fixed-delay-ms:300000}")
    public void scheduledScan() {
        if (!this.cronEnabled) {
            return;
        }
        int n = this.triggerBatch(this.cronBatchSize);
        if (n > 0) {
            log.info("AutoTrigger: publicados {} pedidos de imagem (sem m\u00eddia).", (Object)n);
        }
    }
}
