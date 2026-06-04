package com.zippers.sistema_ventas.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/dashboard/resumen")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    @GetMapping
    public String cargarDashboard(Model model, HttpServletRequest request) {

        // 1. SEGURIDAD DE RUTEO (Arquitectura Zippers)
        boolean isHtmx = "true".equalsIgnoreCase(request.getHeader("HX-Request"));
        boolean isForwarded = request.getAttribute("jakarta.servlet.forward.request_uri") != null;

        if (!isHtmx && !isForwarded) {
            return "redirect:/dashboard";
        }

        // =========================================================
        // 2. LÓGICA DE FECHAS Y BARRA DE PROGRESO MENSUAL
        // =========================================================
        LocalDate hoy = LocalDate.now();
        int diaActual = hoy.getDayOfMonth();
        int totalDiasMes = hoy.lengthOfMonth();
        int progresoMensual = (int) Math.round(((double) diaActual / totalDiasMes) * 100);

        // =========================================================
        // 3. DATOS MOCKEADOS (Para no romper compilación por Repositorios faltantes)
        // =========================================================
        /* * EJEMPLO DE QUERY REAL PARA VENTAS DIARIAS (Cuando exista VentaRepository):
         * @Query("SELECT SUM(v.total) FROM Venta v WHERE DATE(v.fechaCreacion) = CURRENT_DATE")
         * Double ventasHoy = ventaRepository.obtenerVentasDiarias();
         */
        
        double ventasDiariasMock = 1540.50; 
        double ventasMensualesMock = 24500.00; 

        // KPIs de Módulos Faltantes
        int cotizacionesPendientes = 12;
        int pagosPendientes = 5;
        int enviosPendientes = 8;

        // Datos para Chart.js (Top 5 Productos)
        List<String> topProductosNombres = Arrays.asList("Polo Oversize Básico", "Casaca Denim Zippers", "Pantalón Cargo Negro", "Polera con Capucha", "Short de Verano");
        List<Integer> topProductosCantidades = Arrays.asList(120, 85, 60, 45, 30);

        // =========================================================
        // 4. INYECCIÓN AL MODELO
        // =========================================================
        model.addAttribute("ventasDiarias", ventasDiariasMock);
        model.addAttribute("ventasMensuales", ventasMensualesMock);
        model.addAttribute("progresoMensual", progresoMensual);
        model.addAttribute("diaActual", diaActual);
        model.addAttribute("totalDiasMes", totalDiasMes);
        
        model.addAttribute("cotizacionesPendientes", cotizacionesPendientes);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("enviosPendientes", enviosPendientes);

        model.addAttribute("chartLabels", topProductosNombres);
        model.addAttribute("chartData", topProductosCantidades);

        // Retorna el fragmento de la vista
        return "dashboard-home :: panel-principal";
    }
}