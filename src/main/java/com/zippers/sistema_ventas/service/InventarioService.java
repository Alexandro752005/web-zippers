package com.zippers.sistema_ventas.service;

import com.zippers.sistema_ventas.dto.ProductoInventarioDTO;
import com.zippers.sistema_ventas.entity.Producto;
import com.zippers.sistema_ventas.entity.ProductoVariante;
import com.zippers.sistema_ventas.repository.DetalleVentaRepository;
import com.zippers.sistema_ventas.repository.ProductoRepository;
import com.zippers.sistema_ventas.repository.ProductoVarianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioService {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProductoVarianteRepository varianteRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;

    public List<ProductoInventarioDTO> obtenerResumenInventario() {
        List<Producto> productos = productoRepository.findAllActivosConVariantes();
        
        return productos.stream().map(p -> {
            List<ProductoVariante> variantes = p.getVariantes();
            
            int stockTotal = 0;
            BigDecimal valorTotal = BigDecimal.ZERO;
            
            if (variantes != null && !variantes.isEmpty()) {
                stockTotal = variantes.stream()
                    .mapToInt(v -> v.getStockActual() != null ? v.getStockActual() : 0)
                    .sum();
                
                valorTotal = variantes.stream()
                    .map(v -> {
                        BigDecimal costo = v.getPrecioCosto() != null ? v.getPrecioCosto() : BigDecimal.ZERO;
                        int stock = v.getStockActual() != null ? v.getStockActual() : 0;
                        return costo.multiply(BigDecimal.valueOf(stock));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            
            int stockMinimo = p.getStock_minimo() != null ? p.getStock_minimo() : 0;
            String estado = stockTotal >= stockMinimo ? "ÓPTIMO" : "CRÍTICO";
            
            return new ProductoInventarioDTO(
                p.getId_producto(), p.getNombre(), stockTotal, stockMinimo, estado, valorTotal
            );
        }).collect(Collectors.toList());
    }

    public Producto getProductoConVariantes(Integer id) {
        // Le garantizamos al compilador que 'id' no es nulo
        Objects.requireNonNull(id, "El ID del producto es requerido");
        return productoRepository.findByIdWithVariantesAndFotos(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public void actualizarStockVariante(Integer idVariante, Integer nuevoStock) {
        Objects.requireNonNull(idVariante, "El ID de la variante es requerido");
        Objects.requireNonNull(nuevoStock, "El nuevo stock es requerido");
        varianteRepository.actualizarStock(idVariante, nuevoStock);
    }

    public void ocultarProducto(Integer idProducto) {
        // Exigencia de no-nulo para que Spring Data JPA y el IDE estén felices
        Objects.requireNonNull(idProducto, "El ID del producto es requerido");
        Producto p = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        p.setEstado("INACTIVO");
        productoRepository.save(p);
    }

    public List<Object[]> obtenerUltimasVentas(Integer idProducto) {
        Objects.requireNonNull(idProducto, "El ID del producto es requerido");
        return detalleVentaRepository.obtenerUltimasVentasPorProducto(idProducto, PageRequest.of(0, 10));
    }
}