package com.nurtel.vaskamailio.dispatcher.repository;

import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispatcherRepository extends JpaRepository<DispatcherEntity, Integer> {
    Optional<DispatcherEntity> findByDestination(String destination);
}
