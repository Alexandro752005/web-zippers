/* ============================================================
   Zippers Perú — app.js   (núcleo de navegación + HTMX glue)
   Resuelve: CSRF, apertura/cierre de modales, toasts y módulo activo.
   ============================================================ */
(function () {
  "use strict";

  /* ---------- 1) Leer cookie (para CSRF) ---------- */
  function getCookie(name) {
    var m = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
    return m ? decodeURIComponent(m[2]) : null;
  }

  /* ---------- 2) CSRF: inyectar header en TODA petición HTMX ---------- */
  /* Sin esto, los POST devuelven 403 y "el botón no hace nada".          */
  document.body.addEventListener("htmx:configRequest", function (evt) {
    var token = getCookie("XSRF-TOKEN");
    if (token) {
      evt.detail.headers["X-XSRF-TOKEN"] = token;
    }
  });

  /* ---------- 3) Abrir automáticamente cualquier modal recién insertado ----------
     Los fragmentos *form/matriz/detalle* traen un <div class="modal"> con id.
     Tras el swap de HTMX, lo detectamos y lo mostramos con Bootstrap.
     Esto reemplaza los <script> inline (que HTMX no ejecuta de forma fiable). */
  document.body.addEventListener("htmx:afterSwap", function (evt) {
    var target = evt.detail.target;
    if (!target) return;

    // 3a) Si en el contenido insertado hay un modal sin abrir, abrirlo.
    target.querySelectorAll(".modal").forEach(function (modalEl) {
      if (!modalEl.classList.contains("show") && window.bootstrap) {
        var inst = bootstrap.Modal.getOrCreateInstance(modalEl);
        inst.show();
        // Al cerrarse, limpiar el slot para poder reabrir el mismo modal después.
        modalEl.addEventListener("hidden.bs.modal", function () {
          var slot = document.getElementById("zp-modal-slot");
          if (slot && slot.contains(modalEl)) slot.innerHTML = "";
        }, { once: true });
      }
    });

    // 3b) Si se reemplazó el contenido principal, marcar módulo activo.
    if (target.id === "zp-contenido") {
      var raiz = target.querySelector("[id^='zp-modulo-']");
      if (raiz) marcarActivo(raiz.id.replace("zp-modulo-", ""));
    }
  });

  /* ---------- 4) Toasts unificados (escucha AMBOS nombres de evento) ----------
     - Controllers mandan:  zpToast {tipo, msg}
     - GlobalExceptionHandler (legacy): toast {type, message}
     Escuchamos los dos para no perder ningún aviso. */
  function mostrarToast(tipo, msg) {
    var cont = document.getElementById("zp-toasts");
    if (!cont) { alert(msg); return; }
    var clase = (tipo === "success" || tipo === "ok") ? "text-bg-success"
              : (tipo === "error" || tipo === "danger") ? "text-bg-danger"
              : (tipo === "warning") ? "text-bg-warning"
              : "text-bg-primary";
    var icono = (tipo === "success" || tipo === "ok") ? "fa-circle-check"
              : (tipo === "error" || tipo === "danger") ? "fa-circle-exclamation"
              : "fa-circle-info";
    var el = document.createElement("div");
    el.className = "toast align-items-center border-0 " + clase;
    el.setAttribute("role", "alert");
    el.innerHTML =
      '<div class="d-flex">' +
      '  <div class="toast-body"><i class="fa-solid ' + icono + ' me-2"></i>' + msg + '</div>' +
      '  <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>' +
      '</div>';
    cont.appendChild(el);
    if (window.bootstrap) {
      var t = new bootstrap.Toast(el, { delay: 3000 });
      t.show();
      el.addEventListener("hidden.bs.toast", function () { el.remove(); });
    }
  }

  document.body.addEventListener("zpToast", function (evt) {
    var d = evt.detail || {};
    mostrarToast(d.tipo || "primary", d.msg || "Operación realizada.");
  });
  // Compatibilidad con el evento "toast" del GlobalExceptionHandler.
  document.body.addEventListener("toast", function (evt) {
    var d = evt.detail || {};
    mostrarToast(d.type || "primary", d.message || "Operación realizada.");
  });

  /* ---------- 5) Cierre de modal tras guardar OK (evento zpClose) ---------- */
  document.body.addEventListener("zpClose", function () {
    document.querySelectorAll(".modal.show").forEach(function (m) {
      var inst = window.bootstrap ? bootstrap.Modal.getInstance(m) : null;
      if (inst) inst.hide();
    });
    // Limpieza defensiva de backdrops huérfanos.
    setTimeout(function () {
      document.querySelectorAll(".modal-backdrop").forEach(function (b) { b.remove(); });
      document.body.classList.remove("modal-open");
      document.body.style.removeProperty("padding-right");
      document.body.style.removeProperty("overflow");
    }, 300);
  });

  /* ---------- 6) Marcado de módulo activo en el sidebar (sin tocar URL) ---------- */
  var TITULOS = {
    home:"Panel de control", usuarios:"Usuarios", perfiles:"Perfiles",
    categorias:"Categorías", productos:"Productos", inventario:"Inventario",
    temporadas:"Temporadas y Descuentos", clientes:"Clientes", ventas:"Ventas",
    despacho:"Despacho", recojo:"Recojo", cotizaciones:"Cotizaciones",
    tienda:"Gestión Tienda", presentaciones:"Presentaciones"
  };
  function marcarActivo(modulo) {
    document.querySelectorAll(".zp-nav-item").forEach(function (a) {
      a.classList.toggle("active", a.getAttribute("data-modulo") === modulo);
    });
    var titleEl = document.getElementById("zp-topbar-title");
    if (titleEl && TITULOS[modulo]) titleEl.textContent = TITULOS[modulo];
  }
  document.addEventListener("click", function (e) {
    var item = e.target.closest(".zp-nav-item");
    if (!item || item.classList.contains("disabled")) return;
    if (item.getAttribute("target") === "_blank") return; // Sitio web
    var modulo = item.getAttribute("data-modulo");
    if (modulo) marcarActivo(modulo);
  });

  /* ---------- 7) Errores de red HTMX → aviso claro ---------- */
  document.body.addEventListener("htmx:responseError", function (evt) {
    var code = (evt.detail && evt.detail.xhr) ? evt.detail.xhr.status : 0;
    if (code === 403)      mostrarToast("error", "No tienes permisos o la sesión expiró. Vuelve a iniciar sesión.");
    else if (code === 404) mostrarToast("error", "Recurso no encontrado (404).");
    else                   mostrarToast("error", "Ocurrió un error en el servidor.");
  });

  /* ---------- 8) Toggle sidebar (responsive) ---------- */
  window.zpToggleSidebar = function () {
    var app = document.querySelector(".zp-app");
    if (app) app.classList.toggle("zp-sidebar-collapsed");
  };

})();