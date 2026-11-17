
package com.smartpay.payment.repository;

import com.smartpay.payment.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            SELECT event
            FROM OutboxEvent event
            WHERE event.status = :status
            AND (event.nextRetryAt IS NULL OR event.nextRetryAt <= :now)
            ORDER BY event.createdAt ASC
            """)
    List<OutboxEvent> findEventsReadyForPublishing(@Param("status") String status,
                                                   @Param("now") LocalDateTime now);

    Optional<OutboxEvent> findByAggregateIdAndStatus(String aggregateId, String status);
}

