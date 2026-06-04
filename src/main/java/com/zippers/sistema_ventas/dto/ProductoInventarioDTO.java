package com.zippers.sistema_ventas.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductoInventarioDTO {
    private Integer id;
    private String nombre;
    private Integer stockGeneral;
    private Integer stockMinimo;
    private String estado;
    private BigDecimal valorTotal;
}