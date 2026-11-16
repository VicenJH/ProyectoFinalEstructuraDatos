
package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

/**
 * Clasifica las operaciones financieras que puede realizar el sistema.
 *
 * Se utiliza para identificar la naturaleza de una transacción y permitir
 * que el controlador aplique las reglas correspondientes.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public enum TipoTransaccion {

    /** Ingreso de dinero a una billetera. */
    RECARGA,

    /** Salida de dinero de una billetera. */
    RETIRO,

    /** Transferencia entre billeteras administradas por el sistema. */
    TRANSFERENCIA_INTERNA,

    /** Transferencia hacia una entidad o destino externo. */
    TRANSFERENCIA_EXTERNA,

    /** Abono promocional aplicado mediante un cupón de recompensa. */
    BONIFICACION
}
