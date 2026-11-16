
package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Representa una operación financiera que debe ejecutarse en una fecha futura.
 *
 * Hereda los datos comunes de OperacionFinanciera y agrega la frecuencia
 * con la que debe ejecutarse, además del usuario responsable.
 *
 * Las frecuencias disponibles son UNICA, SEMANAL y MENSUAL.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class OperacionProgramada extends OperacionFinanciera {

    /** Frecuencia de ejecución de la operación. */
    private Frecuencia frecuencia;

    /** Identificador del usuario que solicita la operación. */
    private String idUsuario;

    /** Evita repetir la alerta de proximidad durante una misma ocurrencia. */
    private boolean notificacionProximaEnviada;

    /**
     * Constructor compatible con la versión original.
     *
     * El usuario queda sin identificar. Para procesar correctamente
     * una operación programada se recomienda utilizar el constructor
     * que recibe idUsuario.
     *
     * @param id identificador de la operación.
     * @param valor monto de la operación.
     * @param billeteraOrigen billetera de origen.
     * @param billeteraDestino billetera de destino.
     * @param fechaHora fecha y hora de ejecución.
     * @param tipo tipo de transacción.
     * @param frecuencia frecuencia de ejecución.
     * @param estado estado inicial.
     */
    public OperacionProgramada(
            String id,
            double valor,
            String billeteraOrigen,
            String billeteraDestino,
            LocalDateTime fechaHora,
            TipoTransaccion tipo,
            Frecuencia frecuencia,
            String estado
    ) {
        this(
                id,
                valor,
                billeteraOrigen,
                billeteraDestino,
                fechaHora,
                tipo,
                frecuencia,
                estado,
                null
        );
    }

    /**
     * Construye una operación programada asociada a un usuario.
     *
     * @param id identificador único.
     * @param valor monto de la operación.
     * @param billeteraOrigen identificador de la billetera de origen.
     * @param billeteraDestino identificador de la billetera de destino.
     * @param fechaHora fecha y hora de ejecución.
     * @param tipo tipo de transacción.
     * @param frecuencia frecuencia de ejecución.
     * @param estado estado inicial.
     * @param idUsuario identificador del usuario responsable.
     */
    public OperacionProgramada(
            String id,
            double valor,
            String billeteraOrigen,
            String billeteraDestino,
            LocalDateTime fechaHora,
            TipoTransaccion tipo,
            Frecuencia frecuencia,
            String estado,
            String idUsuario
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

        if (frecuencia == null) {
            throw new IllegalArgumentException("La frecuencia no puede ser null.");
        }
        if (idUsuario != null && idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del usuario no puede estar vacío.");
        }
        this.frecuencia = frecuencia;
        this.idUsuario = idUsuario == null ? null : idUsuario.trim();
        this.notificacionProximaEnviada = false;
    }

    /**
     * Obtiene la frecuencia de ejecución.
     *
     * @return frecuencia configurada.
     */
    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    /**
     * Actualiza la frecuencia de ejecución.
     *
     * @param frecuencia nueva frecuencia.
     * @throws IllegalArgumentException si es null.
     */
    public void setFrecuencia(Frecuencia frecuencia) {
        if (frecuencia == null) {
            throw new IllegalArgumentException(
                    "La frecuencia no puede ser null."
            );
        }

        this.frecuencia = frecuencia;
    }

    /**
     * Obtiene el identificador del usuario responsable.
     *
     * @return identificador del usuario o null si no fue especificado.
     */
    public String getIdUsuario() {
        return idUsuario;
    }

    /**
     * Actualiza el usuario responsable.
     *
     * @param idUsuario identificador del usuario.
     * @throws IllegalArgumentException si se proporciona un texto vacío.
     */
    public void setIdUsuario(String idUsuario) {
        if (idUsuario != null && idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El identificador del usuario no puede estar vacío."
            );
        }

        this.idUsuario = idUsuario == null ? null : idUsuario.trim();
    }

    /** Actualiza la fecha de la siguiente ocurrencia de la programación. */
    public void reprogramar(LocalDateTime fechaHora) {
        cambiarFechaHora(fechaHora);
    }

    /** Cambia el estado del ciclo de vida de la programación. */
    public void actualizarEstado(String estado) {
        cambiarEstado(estado);
    }

    public boolean isNotificacionProximaEnviada() {
        return notificacionProximaEnviada;
    }

    public void setNotificacionProximaEnviada(boolean enviada) {
        this.notificacionProximaEnviada = enviada;
    }
}
