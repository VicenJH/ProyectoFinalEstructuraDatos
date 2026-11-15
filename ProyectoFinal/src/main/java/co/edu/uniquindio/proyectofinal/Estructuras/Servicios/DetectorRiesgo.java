package co.edu.uniquindio.proyectofinal.Estructuras.Servicios;

import co.edu.uniquindio.proyectofinal.Estructuras.Hash.TablaHash;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.EventoAuditoria;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.NivelRiesgo;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Módulo de detección de comportamiento financiero inusual (requisito 4.9).
 *
 * Mantiene un perfil por usuario (TablaHash, acceso O(1)) y un historial
 * de auditoría (lista). Cada operación se evalúa ANTES de aplicarse y se
 * registra en el perfil DESPUÉS de aplicarse.
 *
 * Criterios evaluados:
 * 1. Operaciones consecutivas en menos de 1 minuto.
 * 2. Envío o retiro muy superior al promedio del usuario.
 * 3. Envíos repetidos al mismo destino en pocos minutos.
 * 4. Uso de varias billeteras del usuario en pocos minutos (fragmentación).
 * 5. Cambio brusco de frecuencia (6 operaciones en 10 minutos).
 * 6. Actividad en un horario donde el usuario nunca había operado.
 * 7. Monto fijo atípicamente alto (riesgo ALTO directo).
 *
 * Nivel: 0 criterios = BAJO, 1 = MEDIO, 2 o más (o monto alto) = ALTO.
 */
public class DetectorRiesgo {

    private static final double UMBRAL_MONTO_ALTO = 5000000.0;
    private static final long SEGUNDOS_ALTA_FRECUENCIA = 60;
    private static final int FACTOR_PROMEDIO = 3;
    private static final int MIN_ENVIOS_PARA_PROMEDIO = 3;
    private static final long MINUTOS_MISMO_DESTINO = 5;
    private static final long MINUTOS_FRAGMENTACION = 5;
    private static final int BILLETERAS_FRAGMENTACION = 3;
    private static final long MINUTOS_CAMBIO_BRUSCO = 10;
    private static final int MIN_OPERACIONES_HORARIO = 10;

    /** Cantidad de operaciones recientes que recuerda cada perfil. */
    private static final int VENTANA = 5;

    /** Resultado de evaluar una operación. */
    public static class Evaluacion {
        private final NivelRiesgo nivel;
        private final String motivo;

        private Evaluacion(NivelRiesgo nivel, String motivo) {
            this.nivel = nivel;
            this.motivo = motivo;
        }

        public NivelRiesgo getNivel() {
            return nivel;
        }

        /** @return motivos separados por ";" o cadena vacía si el riesgo es BAJO. */
        public String getMotivo() {
            return motivo;
        }
    }

    /** Resumen del comportamiento histórico de un usuario. */
    private static class Perfil {
        private long sumaSalientes = 0;
        private int cantidadSalientes = 0;
        private int totalOperaciones = 0;
        private final int[] operacionesPorHora = new int[24];

        private Integer ultimoDestino = null;
        private LocalDateTime ultimoEnvioDestino = null;

        // Buffer circular con las últimas operaciones.
        private final LocalDateTime[] tiempos = new LocalDateTime[VENTANA];
        private final int[] billeteras = new int[VENTANA];
        private final boolean[] salientes = new boolean[VENTANA];
        private int posicion = 0;

        private LocalDateTime ultimaOperacion() {
            return tiempos[(posicion + VENTANA - 1) % VENTANA];
        }
    }

    private final TablaHash<String, Perfil> perfiles;
    private final SimpleLinkedList<EventoAuditoria> historialAuditoria;
    private int contadorEventos;

    public DetectorRiesgo() {
        this.perfiles = new TablaHash<>(100);
        this.historialAuditoria = new SimpleLinkedList<>();
        this.contadorEventos = 0;
    }

    /**
     * Evalúa una operación sin modificar ningún estado.
     *
     * @param idUsuario usuario que realiza la operación.
     * @param monto valor de la operación.
     * @param saliente true si el dinero sale de su billetera (retiro o envío).
     * @param codigoOrigen billetera de origen (usar -1 si se desconoce).
     * @param codigoDestino billetera destino o null si no aplica.
     * @param ahora momento de la operación.
     */
    public Evaluacion evaluar(String idUsuario, int monto, boolean saliente,
                              int codigoOrigen, Integer codigoDestino, LocalDateTime ahora) {
        Perfil perfil = perfiles.buscar(idUsuario);

        if (perfil == null) {
            perfil = new Perfil();
        }

        StringBuilder motivos = new StringBuilder();
        int criterios = 0;
        boolean montoMuyAlto = false;

        if (saliente && monto > UMBRAL_MONTO_ALTO) {
            montoMuyAlto = true;
            agregarMotivo(motivos, "Monto atípicamente alto: $" + monto);
        }

        // 1. Operaciones consecutivas en un tiempo muy corto.
        LocalDateTime ultima = perfil.ultimaOperacion();
        if (ultima != null && Duration.between(ultima, ahora).getSeconds() < SEGUNDOS_ALTA_FRECUENCIA) {
            criterios++;
            agregarMotivo(motivos, "Operaciones consecutivas en menos de 1 minuto");
        }

        // 2. Valor inusualmente alto respecto al promedio del usuario.
        if (saliente
                && perfil.cantidadSalientes >= MIN_ENVIOS_PARA_PROMEDIO
                && (long) monto * perfil.cantidadSalientes > (long) FACTOR_PROMEDIO * perfil.sumaSalientes) {
            criterios++;
            agregarMotivo(motivos, "Valor superior a " + FACTOR_PROMEDIO + " veces su promedio de envíos");
        }

        // 3. Movimientos repetitivos hacia el mismo destino.
        if (saliente && codigoDestino != null
                && codigoDestino.equals(perfil.ultimoDestino)
                && perfil.ultimoEnvioDestino != null
                && Duration.between(perfil.ultimoEnvioDestino, ahora).toMinutes() < MINUTOS_MISMO_DESTINO) {
            criterios++;
            agregarMotivo(motivos, "Envíos repetidos al mismo destino en menos de "
                    + MINUTOS_MISMO_DESTINO + " minutos");
        }

        // 4. Fragmentación de montos usando varias billeteras.
        if (saliente && codigoOrigen > 0 && contarBilleterasRecientes(perfil, codigoOrigen, ahora)
                >= BILLETERAS_FRAGMENTACION) {
            criterios++;
            agregarMotivo(motivos, "Uso de varias billeteras en pocos minutos (posible fragmentación)");
        }

        // 5. Cambio brusco en la frecuencia de transacciones.
        LocalDateTime masAntigua = perfil.tiempos[perfil.posicion];
        if (masAntigua != null && Duration.between(masAntigua, ahora).toMinutes() < MINUTOS_CAMBIO_BRUSCO) {
            criterios++;
            agregarMotivo(motivos, "Cambio brusco en la frecuencia de transacciones");
        }

        // 6. Actividad en un horario no habitual.
        if (perfil.totalOperaciones >= MIN_OPERACIONES_HORARIO
                && perfil.operacionesPorHora[ahora.getHour()] == 0) {
            criterios++;
            agregarMotivo(motivos, "Actividad en un horario no habitual (" + ahora.getHour() + ":00)");
        }

        NivelRiesgo nivel;

        if (montoMuyAlto || criterios >= 2) {
            nivel = NivelRiesgo.ALTO;
        } else if (criterios == 1) {
            nivel = NivelRiesgo.MEDIO;
        } else {
            nivel = NivelRiesgo.BAJO;
        }

        return new Evaluacion(nivel, motivos.toString());
    }

    /** Actualiza el perfil del usuario con una operación ya aplicada. */
    public void registrarOperacion(String idUsuario, int monto, boolean saliente,
                                   int codigoOrigen, Integer codigoDestino, LocalDateTime ahora) {
        Perfil perfil = perfiles.buscar(idUsuario);

        if (perfil == null) {
            perfil = new Perfil();
            perfiles.insertar(idUsuario, perfil);
        }

        perfil.totalOperaciones++;
        perfil.operacionesPorHora[ahora.getHour()]++;

        if (saliente) {
            perfil.sumaSalientes += monto;
            perfil.cantidadSalientes++;

            if (codigoDestino != null) {
                perfil.ultimoDestino = codigoDestino;
                perfil.ultimoEnvioDestino = ahora;
            }
        }

        perfil.tiempos[perfil.posicion] = ahora;
        perfil.billeteras[perfil.posicion] = codigoOrigen;
        perfil.salientes[perfil.posicion] = saliente;
        perfil.posicion = (perfil.posicion + 1) % VENTANA;
    }

    /** Guarda un evento en el historial de auditoría. */
    public EventoAuditoria registrarEvento(String idUsuario, String idTransaccion,
                                           Evaluacion evaluacion, LocalDateTime fecha) {
        contadorEventos++;

        EventoAuditoria evento = new EventoAuditoria(
                contadorEventos, fecha, idUsuario, idTransaccion,
                evaluacion.getNivel(), evaluacion.getMotivo()
        );

        historialAuditoria.addLast(evento);
        return evento;
    }

    /** @return todos los eventos de auditoría en orden cronológico. */
    public SimpleLinkedList<EventoAuditoria> obtenerHistorialAuditoria() {
        return historialAuditoria;
    }

    public SimpleLinkedList<EventoAuditoria> obtenerEventosPorUsuario(String idUsuario) {
        SimpleLinkedList<EventoAuditoria> resultado = new SimpleLinkedList<>();

        for (EventoAuditoria evento : historialAuditoria) {
            if (evento.getIdUsuario().equals(idUsuario)) {
                resultado.addLast(evento);
            }
        }

        return resultado;
    }

    public SimpleLinkedList<EventoAuditoria> obtenerEventosPorNivel(NivelRiesgo nivel) {
        SimpleLinkedList<EventoAuditoria> resultado = new SimpleLinkedList<>();

        for (EventoAuditoria evento : historialAuditoria) {
            if (evento.getNivel() == nivel) {
                resultado.addLast(evento);
            }
        }

        return resultado;
    }

    /** Cuenta billeteras distintas con envíos recientes, incluyendo la actual. */
    private int contarBilleterasRecientes(Perfil perfil, int codigoActual, LocalDateTime ahora) {
        int[] distintas = new int[VENTANA + 1];
        int cantidad = 0;

        distintas[cantidad++] = codigoActual;

        for (int i = 0; i < VENTANA; i++) {
            LocalDateTime tiempo = perfil.tiempos[i];

            if (tiempo == null || !perfil.salientes[i]
                    || Duration.between(tiempo, ahora).toMinutes() >= MINUTOS_FRAGMENTACION) {
                continue;
            }

            boolean repetida = false;

            for (int j = 0; j < cantidad; j++) {
                if (distintas[j] == perfil.billeteras[i]) {
                    repetida = true;
                }
            }

            if (!repetida) {
                distintas[cantidad++] = perfil.billeteras[i];
            }
        }

        return cantidad;
    }

    private void agregarMotivo(StringBuilder motivos, String texto) {
        if (motivos.length() > 0) {
            motivos.append("; ");
        }
        motivos.append(texto);
    }
}