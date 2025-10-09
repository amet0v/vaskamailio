package com.nurtel.vaskamailio.cdr.repository;

import com.nurtel.vaskamailio.cdr.entity.CdrEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CdrRepository extends JpaRepository<CdrEntity, Long> {

    @Query(value = """
            SELECT * FROM cdr
            WHERE call_time BETWEEN :startTime AND :endTime
              AND (source ~* :source)
              AND (cid ~* :cid)
              AND (did ~* :did)
              AND (:setid IS NULL OR setid = :setid)
            ORDER BY call_time DESC
            """, nativeQuery = true)
    List<CdrEntity> findByFieldsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("source") String source,
            @Param("cid") String cid,
            @Param("did") String did,
            @Param("setid") Integer setid
    );
}
