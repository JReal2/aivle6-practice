package com.example.em.repository;

import com.example.em.domain.EMData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EMRepository extends JpaRepository<EMData, Long> {
}
