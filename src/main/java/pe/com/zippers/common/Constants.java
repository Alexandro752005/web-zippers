package pe.com.zippers.common;

/**
 * Constantes globales del sistema Zippers Perú.
 * Centraliza estados, acciones de auditoría, headers HTMX y claves de modelo
 * para evitar strings mágicos y mantener coherencia con la BD.
 */
public final class Constants {

    private Constants() {
    }

    /* ============================ RUTAS ============================ */
    public static final String DASHBOARD = "/dashboard";
    public static final String LOGIN = "/login";

    /* ====================== HEADERS HTMX 1.9.10 ==================== */
    public static final String HX_REQUEST = "HX-Request";
    public static final String HX_REDIRECT = "HX-Redirect";
    public static final String HX_TRIGGER = "HX-Trigger";
    public static final String HX_RETARGET = "HX-Retarget";
    public static final String HX_RESWAP = "HX-Reswap";

    /* =================== ATRIBUTO REQUEST (forward) ================ */
    public static final String ATTR_FORWARD_INTERNO = "zp_forward_interno";

    /* ===================== ESTADOS GENÉRICOS ====================== */
    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_INACTIVO = "INACTIVO";
    public static final String ESTADO_BLOQUEADO = "BLOQUEADO";

    /* ================= RESULTADOS DE AUDITORÍA ==================== */
    public static final String AUD_OK = "OK";
    public static final String AUD_ERROR = "ERROR";
    public static final String AUD_DENEGADO = "DENEGADO";

    /** registro_id obligatorio (NOT NULL); 0 = acción masiva o sin registro único. */
    public static final long REGISTRO_MASIVO = 0L;

    /* ================ SEGURIDAD / LOGIN BACKOFFICE ================ */
    public static final int MAX_INTENTOS_FALLIDOS = 5;
    /** Bloqueo de cuenta: 5 minutos (regla de negocio confirmada). */
    public static final int MINUTOS_BLOQUEO = 5;

    /* =================== COMPROBANTE ÚNICO ======================== */
    public static final String COMPROBANTE_UNICO = "NOTA_DE_VENTA";

    /* =================== VALIDACIÓN PERÚ ========================= */
    public static final String REGEX_DNI = "^[0-9]{8}$";
    public static final String REGEX_TELEFONO = "^[0-9]{9}$";
    public static final String REGEX_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
}