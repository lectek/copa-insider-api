package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailCampaignQueueRepository extends JpaRepository<EmailCampaignQueue, Long> {
    List<EmailCampaignQueue> findByStatusOrderByScheduledAtAsc(String status, Pageable pageable);

    @Query("""
        select q
        from EmailCampaignQueue q
        where q.status = :status
          and (q.scheduledAt is null or q.scheduledAt <= :now)
        order by q.scheduledAt asc, q.createdAt asc
        """)
    List<EmailCampaignQueue> findReady(@Param("status") String status, @Param("now") Instant now, Pageable pageable);

    List<EmailCampaignQueue> findByCampaignIdAndStatusIn(Long campaignId, List<String> statuses);

    @Query("""
        select q.status as status, count(q) as total
        from EmailCampaignQueue q
        group by q.status
        """)
    List<StatusCount> countByStatus();

    List<EmailCampaignQueue> findTop20ByOrderByCreatedAtDesc();

    interface StatusCount {
        String getStatus();
        Long getTotal();
    }
}
