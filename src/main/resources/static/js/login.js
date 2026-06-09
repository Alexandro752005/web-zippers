/* Zippers Perú — login.js : mostrar / ocultar contraseña */
(function () {
  "use strict";
  var btn  = document.getElementById("zp-eye-btn");
  var inp  = document.getElementById("zp-password");
  var icon = document.getElementById("zp-eye-icon");
  if (!btn || !inp || !icon) return;

  btn.addEventListener("click", function () {
    var visible = inp.type === "text";
    inp.type = visible ? "password" : "text";
    icon.classList.toggle("fa-eye", visible);
    icon.classList.toggle("fa-eye-slash", !visible);
  });
})();