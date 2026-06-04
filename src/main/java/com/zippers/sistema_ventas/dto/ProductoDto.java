package com.zippers.sistema_ventas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductoDto {

    private Integer idProducto;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;

    @NotBlank(message = "El género es obligatorio")
    private String genero = "UNISEX";
    
    private String marca;
    private String temporada = "Atemporal";
    private String materialPrincipal;

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio de costo debe ser mayor o igual a 0.01")
    private BigDecimal precioCosto;

    private String estado = "ACTIVO";

    @Valid
    @NotEmpty(message = "Debe agregar al menos una variante")
    private List<VarianteDto> variantes = new ArrayList<>();
}