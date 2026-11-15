
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
    public int id;

    /** Fecha y hora de generación. */
    public LocalDateTime fecha;

    /** Descripción del evento detectado. */
    public String mensaje;

    /** Clasificación de la alerta. */
    public String tipoAlerta;

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

    /**
     * Modifica el identificador.
     *
     * @param id nuevo identificador positivo.
     */
    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador debe ser positivo."
            );
        }

        this.id = id;
    }

    /** @return fecha de generación. */
    public LocalDateTime getFecha() {
        return fecha;
    }

    /**
     * Modifica la fecha de generación.
     *
     * @param fecha nueva fecha.
     */
    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha no puede ser null."
            );
        }

        this.fecha = fecha;
    }

    /** @return mensaje descriptivo. */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Modifica el mensaje.
     *
     * @param mensaje nuevo mensaje.
     */
    public void setMensaje(String mensaje) {
        validarTexto(mensaje, "El mensaje");
        this.mensaje = mensaje;
    }

    /** @return clasificación de la alerta. */
    public String getTipoAlerta() {
        return tipoAlerta;
    }

    /**
     * Modifica el tipo de alerta.
     *
     * @param tipoAlerta nueva clasificación.
     */
    public void setTipoAlerta(String tipoAlerta) {
        validarTexto(tipoAlerta, "El tipo de alerta");
        this.tipoAlerta = tipoAlerta;
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
    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
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
