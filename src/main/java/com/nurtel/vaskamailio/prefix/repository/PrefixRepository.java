package com.nurtel.vaskamailio.prefix.repository;

import com.nurtel.vaskamailio.prefix.entity.PrefixEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrefixRepository extends JpaRepository<PrefixEntity, Long> {
    Optional<PrefixEntity> findByPattern(String pattern);
}
