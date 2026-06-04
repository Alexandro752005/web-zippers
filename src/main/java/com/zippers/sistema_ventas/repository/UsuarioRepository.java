package com.zippers.sistema_ventas.repository;

import com.zippers.sistema_ventas.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Crucial para Spring Security
    Optional<Usuario> findByEmail(String email);

    // 1. Mostrar todos EXCEPTO los que tengan rol 'CLIENTE' (Paginado)
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.rol.nombre) != 'cliente'")
    Page<Usuario> buscarUsuariosNoClientes(Pageable pageable);

    // 2. Búsqueda por término (Fuzzy Search) EXCLUYENDO clientes (Paginado)
    @Query("SELECT u FROM Usuario u WHERE " +
           "(LOWER(u.nombre_completo) LIKE LOWER(concat('%', :term, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(concat('%', :term, '%'))) " +
           "AND LOWER(u.rol.nombre) != 'cliente'")
    Page<Usuario> buscarPorTerminoNoClientes(@Param("term") String term, Pageable pageable);
}