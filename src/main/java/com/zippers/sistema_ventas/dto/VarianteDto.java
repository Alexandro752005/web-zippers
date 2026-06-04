package com.zippers.sistema_ventas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VarianteDto {

    private Integer idVariante;

    @NotBlank(message = "La talla es obligatoria")
    private String talla;

    @NotNull(message = "El contorno en cm es obligatorio")
    @DecimalMin(value = "0.1", inclusive = true, message = "El contorno debe ser mayor que cero")
    private BigDecimal medidaCm;

    @NotBlank(message = "El color es obligatorio")
    private String color;
    
    private String codigoHex;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El precio de venta debe ser mayor o igual a 0.01")
    private BigDecimal precioVenta;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}