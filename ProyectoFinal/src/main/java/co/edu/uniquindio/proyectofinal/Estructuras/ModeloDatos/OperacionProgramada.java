package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/** Operación futura o periódica asociada a un usuario responsable. */
public class OperacionProgramada extends OperacionFinanciera {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String EJECUTADA = "EJECUTADA";
    public static final String FALLIDA = "FALLIDA";
    public static final String CANCELADA_POR_ELIMINACION_DE_USUARIO = "CANCELADA_POR_ELIMINACION_DE_USUARIO";
    public static final String CANCELADA_POR_REFERENCIA_INACTIVA = "CANCELADA_POR_REFERENCIA_INACTIVA";
    public static final String CANCELADA_POR_USUARIO = "CANCELADA_POR_USUARIO";

    private Frecuencia frecuencia;
    private String idUsuario;
    private boolean notificacionProximaEnviada;
    private final int diaOriginalDelMes;

    /** Constructor legado. Para la plataforma debe usarse la variante con idUsuario. */
    public OperacionProgramada(String id, double valor, String billeteraOrigen, String billeteraDestino,
                               LocalDateTime fechaHora, TipoTransaccion tipo, Frecuencia frecuencia, String estado) {
        this(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo, frecuencia, estado, null, true);
    }

    public OperacionProgramada(String id, double valor, String billeteraOrigen, String billeteraDestino,
                               LocalDateTime fechaHora, TipoTransaccion tipo, Frecuencia frecuencia,
                               String estado, String idUsuario) {
        this(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo, frecuencia, estado, idUsuario, false);
    }

    private OperacionProgramada(String id, double valor, String billeteraOrigen, String billeteraDestino,
                                LocalDateTime fechaHora, TipoTransaccion tipo, Frecuencia frecuencia,
                                String estado, String idUsuario, boolean permitirUsuarioAusente) {
        super(id, valor, billeteraOrigen, billeteraDestino, fechaHora, tipo, estado);
        if (frecuencia == null) throw new IllegalArgumentException("La frecuencia no puede ser null.");
        if (!PENDIENTE.equals(estado)) {
            throw new IllegalArgumentException("Una operación nueva debe iniciar en estado PENDIENTE.");
        }
        if (!permitirUsuarioAusente && (idUsuario == null || idUsuario.trim().isEmpty())) {
            throw new IllegalArgumentException("El usuario responsable es obligatorio.");
        }
        if (idUsuario != null && idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del usuario no puede estar vacío.");
        }
        this.frecuencia = frecuencia;
        this.idUsuario = idUsuario == null ? null : idUsuario.trim();
        this.notificacionProximaEnviada = false;
        this.diaOriginalDelMes = fechaHora.getDayOfMonth();
    }

    private OperacionProgramada(OperacionProgramada original) {
        super(original.getId(), original.getValor(), original.getBilleteraOrigen(),
                original.getBilleteraDestino(), original.getFechaHora(), original.getTipo(), original.getEstado());
        this.frecuencia = original.frecuencia;
        this.idUsuario = original.idUsuario;
        this.notificacionProximaEnviada = original.notificacionProximaEnviada;
        this.diaOriginalDelMes = original.diaOriginalDelMes;
    }

    public OperacionProgramada copia() { return new OperacionProgramada(this); }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public void setFrecuencia(Frecuencia frecuencia) {
        if (frecuencia == null) throw new IllegalArgumentException("La frecuencia no puede ser null.");
        this.frecuencia = frecuencia;
    }
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio.");
        }
        this.idUsuario = idUsuario.trim();
    }
    public void reprogramar(LocalDateTime fechaHora) {
        if (!PENDIENTE.equals(getEstado())) {
            throw new IllegalStateException("Solo una operación pendiente puede reprogramarse.");
        }
        cambiarFechaHora(fechaHora);
    }
    public void actualizarEstado(String estado) {
        if (!EJECUTADA.equals(estado) && !FALLIDA.equals(estado)
                && !CANCELADA_POR_ELIMINACION_DE_USUARIO.equals(estado)
                && !CANCELADA_POR_REFERENCIA_INACTIVA.equals(estado)
                && !CANCELADA_POR_USUARIO.equals(estado)) {
            throw new IllegalArgumentException("Estado final de programación no válido.");
        }
        if (!PENDIENTE.equals(getEstado())) {
            throw new IllegalStateException("La operación ya no está pendiente.");
        }
        cambiarEstado(estado);
    }
    public boolean isNotificacionProximaEnviada() { return notificacionProximaEnviada; }
    public void setNotificacionProximaEnviada(boolean enviada) { this.notificacionProximaEnviada = enviada; }

    /** Calcula la ocurrencia siguiente sin perder el día original en meses cortos. */
    public LocalDateTime calcularSiguienteFecha() {
        LocalDateTime actual = getFechaHora();
        if (frecuencia == Frecuencia.SEMANAL) return actual.plusWeeks(1);
        if (frecuencia == Frecuencia.MENSUAL) {
            YearMonth siguienteMes = YearMonth.from(actual).plusMonths(1);
            return fechaEnMes(siguienteMes, actual.toLocalTime());
        }
        throw new IllegalStateException("Una operación de frecuencia única no se reprograma.");
    }

    /**
     * Calcula la primera ocurrencia estrictamente posterior a una fecha de proceso.
     * Omite de forma aritmética las ocurrencias pasadas, sin ejecutar cobros
     * acumulados por cada semana/mes durante el tiempo que la aplicación estuvo cerrada.
     */
    public LocalDateTime calcularSiguienteFechaPosteriorA(LocalDateTime ahora) {
        if (ahora == null) throw new IllegalArgumentException("La fecha de referencia no puede ser null.");
        LocalDateTime siguiente = calcularSiguienteFecha();
        if (siguiente.isAfter(ahora)) return siguiente;

        if (frecuencia == Frecuencia.SEMANAL) {
            long semanas = ChronoUnit.WEEKS.between(siguiente, ahora) + 1;
            return siguiente.plusWeeks(semanas);
        }

        if (frecuencia == Frecuencia.MENSUAL) {
            YearMonth mesSiguiente = YearMonth.from(siguiente);
            long meses = ChronoUnit.MONTHS.between(mesSiguiente, YearMonth.from(ahora));
            YearMonth mesCandidato = mesSiguiente.plusMonths(meses);
            LocalDateTime candidato = fechaEnMes(mesCandidato, siguiente.toLocalTime());
            if (!candidato.isAfter(ahora)) {
                candidato = fechaEnMes(mesCandidato.plusMonths(1), siguiente.toLocalTime());
            }
            return candidato;
        }

        throw new IllegalStateException("Una operación de frecuencia única no se reprograma.");
    }

    private LocalDateTime fechaEnMes(YearMonth mes, java.time.LocalTime hora) {
        int dia = Math.min(diaOriginalDelMes, mes.lengthOfMonth());
        return LocalDateTime.of(mes.atDay(dia), hora);
    }
}
