package com.nurtel.vaskamailio.dispatcher.repository;

import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DispatcherRepository extends JpaRepository<DispatcherEntity, Integer> {
}
