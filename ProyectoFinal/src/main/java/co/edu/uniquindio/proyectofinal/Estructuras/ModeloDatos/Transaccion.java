
package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Representa una transacción financiera realizada dentro del sistema Fintech.
 *
 * Hereda de OperacionFinanciera los atributos y comportamientos comunes
 * de una operación, como el identificador, el valor, las billeteras
 * involucradas, la fecha, el tipo y el estado.
 *
 * Además, almacena los puntos generados por la transacción, que pueden
 * ser utilizados por el sistema de recompensas y niveles de usuario.
 *
 * Las transacciones pueden almacenarse en el historial de los usuarios
 * y en la pila de reversiones del sistema.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class Transaccion extends OperacionFinanciera {

    /**
     * Cantidad de puntos generados por la transacción.
     *
     * Este valor no puede ser negativo.
     */
    private int puntosGenerados;

    /**
     * Construye una transacción con todos sus datos.
     *
     * Inicializa los atributos heredados mediante el constructor
     * de OperacionFinanciera y valida los puntos generados.
     *
     * @param id identificador único de la transacción.
     * @param valor monto monetario de la transacción.
     * @param billeteraOrigen identificador de la billetera de origen.
     * @param billeteraDestino identificador de la billetera de destino,
     *                         puede ser null si no aplica.
     * @param fechaHora fecha y hora en que se realizó la transacción.
     * @param tipo tipo de transacción.
     * @param estado estado inicial de la transacción.
     * @param puntosGenerados cantidad de puntos obtenidos.
     *
     * @throws IllegalArgumentException si los puntos generados son negativos
     * o alguno de los datos heredados incumple las validaciones del padre.
     */
    public Transaccion(
            String id,
            double valor,
            String billeteraOrigen,
            String billeteraDestino,
            LocalDateTime fechaHora,
            TipoTransaccion tipo,
            String estado,
            int puntosGenerados
    ) {

        super(
                id,
                valor,
                billeteraOrigen,
                billeteraDestino,
                fechaHora,
                tipo,
                estado
        );

        validarPuntos(puntosGenerados);
        this.puntosGenerados = puntosGenerados;
    }

    /**
     * Obtiene la cantidad de puntos generados.
     *
     * @return puntos asociados a la transacción.
     *
     * Complejidad temporal: O(1).
     */
    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    /**
     * Modifica la cantidad de puntos generados.
     *
     * @param puntosGenerados nueva cantidad de puntos.
     *
     * @throws IllegalArgumentException si el valor es negativo.
     *
     * Complejidad temporal: O(1).
     */
    public void setPuntosGenerados(int puntosGenerados) {

        validarPuntos(puntosGenerados);
        this.puntosGenerados = puntosGenerados;
    }

    /**
     * Obtiene el estado de la transacción.
     *
     * Este método se conserva para mantener la compatibilidad
     * con las llamadas existentes del proyecto.
     *
     * @return estado actual de la transacción.
     *
     * Complejidad temporal: O(1).
     */
    @Override
    public String getEstado() {
        return super.getEstado();
    }

    /**
     * Modifica el estado de la transacción.
     *
     * Delega la validación y asignación en la clase padre.
     *
     * @param estado nuevo estado de la transacción.
     *
     * @throws IllegalArgumentException si el estado es null
     * o está vacío.
     *
     * Complejidad temporal: O(1).
     */
    @Override
    public void setEstado(String estado) {
        super.setEstado(estado);
    }

    /**
     * Valida que los puntos generados no sean negativos.
     *
     * @param puntos cantidad de puntos que se desea validar.
     *
     * @throws IllegalArgumentException si la cantidad es negativa.
     */
    private void validarPuntos(int puntos) {

        if (puntos < 0) {
            throw new IllegalArgumentException(
                    "Los puntos generados no pueden ser negativos."
            );
        }
    }
}
