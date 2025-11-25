package com.nurtel.vaskamailio.router.repository;

import com.nurtel.vaskamailio.router.entity.RouterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouterRepository extends JpaRepository<RouterEntity, Long>{
    Page<RouterEntity> findByDidContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String filterText, String filterText1, PageRequest id);

    Integer countByDidContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String filterText, String filterText1);

    @Query(value = """
        SELECT * FROM ht_router
        WHERE (:did IS NULL OR key_name ~* :did)
          AND (:setid IS NULL OR key_value = :setid)
          AND (:description IS NULL OR description ~* :description)
        ORDER BY id ASC
        """, nativeQuery = true)
    List<RouterEntity> findByFields(
            @Param("did") String did,
            @Param("setid") String setid,
            @Param("description") String description
    );
}
