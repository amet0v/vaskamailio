package com.nurtel.vaskamailio.audit.repository;

import com.nurtel.vaskamailio.audit.entity.AuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntity, Long> {

    @Query(value = """
    SELECT *
    FROM audit
    WHERE date BETWEEN :startTime AND :endTime

      AND (
            :username = ''
            OR username ~* :username
          )

      AND (
            :server = ''
            OR server ~* :server
          )

      AND (
            :action = ''
            OR action ~* :action
          )

      AND (
            :description = ''
            OR description ~* :description
          )

    ORDER BY date DESC
""", nativeQuery = true)
    List<AuditEntity> findByFieldsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("username") String username,
            @Param("server") String server,
            @Param("action") String action,
            @Param("description") String description
    );
}
