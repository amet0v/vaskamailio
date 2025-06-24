package com.nurtel.vaskamailio.host.repository;

import com.nurtel.vaskamailio.host.entity.HostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostRepository extends JpaRepository<HostEntity, Long> {
}
