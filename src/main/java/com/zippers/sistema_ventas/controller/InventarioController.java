package com.zippers.sistema_ventas.controller;

import com.zippers.sistema_ventas.dto.ProductoInventarioDTO;
import com.zippers.sistema_ventas.service.InventarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/dashboard/inventario")
public class InventarioController {


    @Autowired
    private InventarioService inventarioService;

    // 1. Carga de vista principal (Dashboard)
    @GetMapping
    public String inventario(Model model, HttpServletRequest request) {
        boolean isHtmx = "true".equalsIgnoreCase(request.getHeader("HX-Request"));
        boolean isForwarded = request.getAttribute("jakarta.servlet.forward.request_uri") != null;

        if (!isHtmx && !isForwarded) {
            return "redirect:/dashboard?modulo=inventario";
        }

        cargarMétricas(model);
        return "inventario";
    }

    // 2. Refrescar solo la tabla y las métricas
    @GetMapping("/tabla")
    public String tablaInventario(Model model) {
        cargarMétricas(model);
        return "inventario :: inventario-contenido"; 
    }

    // 3. Modal de Edición
    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Integer id, Model model) {
        model.addAttribute("producto", inventarioService.getProductoConVariantes(id));
        model.addAttribute("historial", inventarioService.obtenerUltimasVentas(id));
        return "inventario-editar :: modal-edicion";
    }

    // 4. Actualizar Stock (HTMX)
    @PostMapping("/actualizar-stock")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public String actualizarStock(@RequestParam Integer idVariante, @RequestParam Integer nuevoStock) {
        inventarioService.actualizarStockVariante(idVariante, nuevoStock);
        return "<i class='fas fa-check text-success'></i> Actualizado";
    }

    // 5. Ocultar Producto (Soft Delete)
    @PostMapping("/ocultar/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public String ocultarProducto(@PathVariable Integer id, Model model) {
        inventarioService.ocultarProducto(id);
        cargarMétricas(model);
        return "inventario :: inventario-contenido";
    }

    // Método auxiliar para calcular tarjetas de resumen
    private void cargarMétricas(Model model) {
        List<ProductoInventarioDTO> productos = inventarioService.obtenerResumenInventario();
        
        long criticos = productos.stream().filter(p -> "CRÍTICO".equals(p.getEstado())).count();
        long optimos = productos.size() - criticos;
        BigDecimal valorTotal = productos.stream()
                .map(ProductoInventarioDTO::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("stockCritico", criticos);
        model.addAttribute("stockOptimo", optimos);
        model.addAttribute("valorTotal", valorTotal);
    }
}