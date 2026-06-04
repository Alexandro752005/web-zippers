package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpcionRepository extends JpaRepository<Opcion, Integer> {
}