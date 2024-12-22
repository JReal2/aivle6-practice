package com.example.em.repository;

import com.example.em.domain.EMData;
import io.micrometer.common.lang.NonNullApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@NonNullApi
public interface EMRepository extends JpaRepository<EMData, Long> {
    Page<EMData> findAll(Pageable pageable);
}
