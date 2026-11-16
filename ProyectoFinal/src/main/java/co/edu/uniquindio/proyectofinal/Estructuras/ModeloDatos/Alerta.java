
package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Representa una alerta generada por el sistema Fintech.
 *
 * Las alertas permiten registrar eventos que requieren atención,
 * como operaciones de monto elevado o comportamientos inusuales.
 *
 * El sistema puede conservarlas en el historial del usuario para
 * facilitar posteriores revisiones y auditorías.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class Alerta {

    /** Identificador numérico de la alerta. */
    private final int id;

    /** Fecha y hora de generación. */
    private final LocalDateTime fecha;

    /** Descripción del evento detectado. */
    private final String mensaje;

    /** Clasificación de la alerta. */
    private final String tipoAlerta;

    /** Indica si la alerta ya fue revisada. */
    private boolean atendida;

    /**
     * Construye una alerta con sus datos principales.
     *
     * @param id identificador de la alerta.
     * @param fecha fecha y hora de generación.
     * @param mensaje descripción del evento.
     * @param tipoAlerta clasificación de la alerta.
     *
     * @throws IllegalArgumentException si el identificador no es positivo,
     * la fecha es null o los textos obligatorios están vacíos.
     */
    public Alerta(
            int id,
            LocalDateTime fecha,
            String mensaje,
            String tipoAlerta
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador debe ser positivo."
            );
        }

        validarTexto(mensaje, "El mensaje");
        validarTexto(tipoAlerta, "El tipo de alerta");

        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha de la alerta no puede ser null."
            );
        }

        this.id = id;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.tipoAlerta = tipoAlerta;
        this.atendida = false;
    }

    /** @return identificador de la alerta. */
    public int getId() {
        return id;
    }

    /** @return fecha de generación. */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /** @return mensaje descriptivo. */
    public String getMensaje() {
        return mensaje;
    }

    /** @return clasificación de la alerta. */
    public String getTipoAlerta() {
        return tipoAlerta;
    }

    /** @return true si la alerta fue atendida. */
    public boolean isAtendida() {
        return atendida;
    }

    /**
     * Marca la alerta como atendida o pendiente.
     *
     * @param atendida nuevo estado.
     */
    public void marcarAtendida() {
        this.atendida = true;
    }

    /** Copia independiente para consultas que no deben modificar el historial. */
    public Alerta copia() {
        Alerta copia = new Alerta(id, fecha, mensaje, tipoAlerta);
        if (atendida) copia.marcarAtendida();
        return copia;
    }

    /**
     * Comprueba que un texto obligatorio tenga contenido.
     *
     * @param texto texto que se validará.
     * @param nombre nombre del atributo.
     */
    private static void validarTexto(String texto, String nombre) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    nombre + " no puede estar vacío."
            );
        }
    }
}
