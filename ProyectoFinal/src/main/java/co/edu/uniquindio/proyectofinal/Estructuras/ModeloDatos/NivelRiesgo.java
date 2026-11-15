package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

/**
 * Nivel de riesgo asignado a una transacción por el módulo de detección
 * de comportamiento financiero inusual.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public enum NivelRiesgo {

    /** No se detectó ningún patrón sospechoso. */
    BAJO,

    /** Se detectó un patrón inusual. */
    MEDIO,

    /** Se detectaron varios patrones inusuales o un monto muy alto. */
    ALTO
}