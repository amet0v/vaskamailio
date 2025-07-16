package com.nurtel.vaskamailio.db.repository;

import com.nurtel.vaskamailio.db.entity.DbEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface DbRepository extends JpaRepository<DbEntity, Long> {
    List<DbEntity> findAllByOrderByIdAsc();
}
