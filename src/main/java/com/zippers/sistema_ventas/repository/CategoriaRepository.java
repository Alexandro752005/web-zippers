package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    // Búsqueda inteligente para la tabla paginada del administrador
    @Query("SELECT c FROM Categoria c WHERE " +
           "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Categoria> buscarPorTerminoPaginado(@Param("term") String term, Pageable pageable);

    // Verificación de nombre único al CREAR
    boolean existsByNombreIgnoreCase(String nombre);

    // Verificación de nombre único al EDITAR (Consulta personalizada para evitar el error del guion bajo)
    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE LOWER(c.nombre) = LOWER(:nombre) AND c.id_categoria <> :idCategoria")
    boolean existeNombreExcluyendoId(@Param("nombre") String nombre, @Param("idCategoria") Integer idCategoria);
}