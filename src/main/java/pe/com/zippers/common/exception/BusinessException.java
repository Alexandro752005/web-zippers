package pe.com.zippers.common.exception;

/**
 * Excepción de regla de negocio. Se lanza cuando un dato es inválido
 * o una operación viola una política del sistema (defensa en backend).
 * Su mensaje es seguro para mostrarse al usuario.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }

    public BusinessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}