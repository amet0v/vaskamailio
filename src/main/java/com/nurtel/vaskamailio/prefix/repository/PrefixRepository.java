package com.nurtel.vaskamailio.prefix.repository;

import com.nurtel.vaskamailio.prefix.entity.PrefixEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrefixRepository extends JpaRepository<PrefixEntity, Long> {
}
