package com.nurtel.vaskamailio.router.repository;

import com.nurtel.vaskamailio.router.entity.RouterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

@Repository
public interface RouterRepository extends JpaRepository<RouterEntity, Long>{
    Page<RouterEntity> findByDidContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String filterText, String filterText1, PageRequest id);

    Integer countByDidContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String filterText, String filterText1);
}
