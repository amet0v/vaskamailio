package com.nurtel.vaskamailio.router.repository;

import com.nurtel.vaskamailio.router.entity.RouterEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface RouterRepository extends JpaRepository<RouterEntity, Long>{
}
