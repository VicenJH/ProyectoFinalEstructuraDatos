
package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

/**
 * Representa una billetera perteneciente a un usuario del sistema Fintech.
 *
 * Almacena su código identificador, categoría, saldo y estado de actividad.
 * Permite realizar recargas y retiros mediante operaciones controladas.
 *
 * El saldo se representa mediante int para conservar la estructura
 * original del proyecto. Por tanto, los montos manejados por esta clase
 * deben expresarse en unidades monetarias enteras.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class Billetera {

    /** Código único de identificación de la billetera. */
    public int codigo;

    /** Categoría de la billetera. */
    public TipoBilletera tipo;

    /** Saldo disponible expresado en unidades enteras. */
    public int saldo;

    /** Indica si la billetera está habilitada para operar. */
    public boolean activo;

    /**
     * Construye una billetera con sus datos iniciales.
     *
     * @param codigo código identificador.
     * @param tipo categoría de la billetera.
     * @param saldo saldo inicial.
     * @param activo estado inicial.
     * @throws IllegalArgumentException si el código no es positivo,
     * el tipo es null o el saldo es negativo.
     */
    public Billetera(
            int codigo,
            TipoBilletera tipo,
            int saldo,
            boolean activo
    ) {
        if (codigo <= 0) {
            throw new IllegalArgumentException(
                    "El código de la billetera debe ser positivo."
            );
        }

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "El tipo de billetera no puede ser null."
            );
        }

        if (saldo < 0) {
            throw new IllegalArgumentException(
                    "El saldo inicial no puede ser negativo."
            );
        }

        this.codigo = codigo;
        this.tipo = tipo;
        this.saldo = saldo;
        this.activo = activo;
    }

    /** @return código de la billetera. */
    public int getCodigo() {
        return codigo;
    }

    /**
     * Modifica el código de la billetera.
     *
     * @param codigo nuevo código positivo.
     */
    public void setCodigo(int codigo) {
        if (codigo <= 0) {
            throw new IllegalArgumentException(
                    "El código debe ser positivo."
            );
        }

        this.codigo = codigo;
    }

    /** @return categoría de la billetera. */
    public TipoBilletera getTipo() {
        return tipo;
    }

    /**
     * Modifica la categoría.
     *
     * @param tipo nueva categoría.
     */
    public void setTipo(TipoBilletera tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException(
                    "El tipo no puede ser null."
            );
        }

        this.tipo = tipo;
    }

    /** @return saldo disponible. */
    public int getSaldo() {
        return saldo;
    }

    /**
     * Establece un nuevo saldo.
     *
     * Este método permite ajustar el saldo desde el controlador.
     *
     * @param saldo nuevo saldo, no puede ser negativo.
     */
    public void setSaldo(int saldo) {
        if (saldo < 0) {
            throw new IllegalArgumentException(
                    "El saldo no puede ser negativo."
            );
        }

        this.saldo = saldo;
    }

    /** @return true si la billetera está activa. */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Modifica el estado de actividad.
     *
     * @param activo nuevo estado.
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Incrementa el saldo de la billetera.
     *
     * Comprueba que la operación no provoque un desbordamiento
     * del tipo entero.
     *
     * @param monto cantidad que se desea agregar.
     * @throws IllegalArgumentException si el monto no es positivo.
     * @throws ArithmeticException si el resultado supera Integer.MAX_VALUE.
     *
     * Complejidad temporal: O(1).
     */
    public void recargar(int monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto de la recarga debe ser positivo."
            );
        }

        saldo = Math.addExact(saldo, monto);
    }

    /**
     * Retira dinero de la billetera si existe saldo suficiente.
     *
     * @param monto cantidad que se desea retirar.
     * @return true si el retiro se realizó; false si no hay saldo suficiente.
     * @throws IllegalArgumentException si el monto no es positivo.
     *
     * Complejidad temporal: O(1).
     */
    public boolean retirar(int monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException(
                    "El monto del retiro debe ser positivo."
            );
        }

        if (saldo < monto) {
            return false;
        }

        saldo -= monto;
        return true;
    }
}
