package com.zippers.sistema_ventas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class WebController {

    private static final Map<String, String> MODULE_VIEWS = Map.ofEntries(
            Map.entry("usuarios", "usuarios"),
            Map.entry("perfiles", "perfiles"),
            Map.entry("categorias", "categorias"),
            Map.entry("productos", "productos"),
            Map.entry("inventario", "inventario"),
            Map.entry("temporadas-descuentos", "temporadas-descuentos"), // NUEVO
            Map.entry("clientes", "clientes"),
            Map.entry("ventas", "ventas"),
            Map.entry("despacho", "despacho"), // NUEVO
            Map.entry("recojo", "recojo"), // NUEVO
            Map.entry("cotizaciones", "cotizaciones"),
            Map.entry("gestion-tienda", "gestion-tienda"),
            Map.entry("presentaciones", "presentaciones"),
            Map.entry("sitio-web", "sitio-web") // NUEVO
    );

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String modulo) {
        if (modulo == null || modulo.isBlank()) {
            return "index";
        }

        String moduloLower = modulo.toLowerCase();

        if (!MODULE_VIEWS.containsKey(moduloLower)) {
            return "index";
        }

        // Forward al controlador del módulo para que cargue sus datos
        return "forward:/dashboard/" + moduloLower;
    }
}