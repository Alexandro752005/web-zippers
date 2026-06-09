package pe.com.zippers.common.exception;

/**
 * Se lanza cuando se intenta acceder directamente a un fragmento interno
 * de módulo (sin HX-Request y sin forward interno del dispatcher).
 * Activa el redireccionamiento a /dashboard?modulo=X.
 */
public class AccessDirectoException extends RuntimeException {

    private final String modulo;

    public AccessDirectoException(String modulo) {
        super("Acceso directo no permitido al módulo: " + modulo);
        this.modulo = modulo;
    }

    public String getModulo() {
        return modulo;
    }
}