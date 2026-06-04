package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaLog, Integer> {
}