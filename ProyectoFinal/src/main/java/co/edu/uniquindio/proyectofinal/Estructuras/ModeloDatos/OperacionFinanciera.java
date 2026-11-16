package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/** Datos comunes e invariantes de una operación financiera. */
public abstract class OperacionFinanciera {

    private final String id;
    private final double valor;
    private final String billeteraOrigen;
    private final String billeteraDestino;
    private LocalDateTime fechaHora;
    private final TipoTransaccion tipo;
    private String estado;

    public OperacionFinanciera(String id, double valor, String billeteraOrigen,
                               String billeteraDestino, LocalDateTime fechaHora,
                               TipoTransaccion tipo, String estado) {
        this.id = validarTexto(id, "El identificador");
        if (!Double.isFinite(valor) || valor <= 0 || valor != Math.rint(valor)
                || valor > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El valor debe ser un monto entero positivo representable.");
        }
        if (fechaHora == null) throw new IllegalArgumentException("La fecha y hora no pueden ser null.");
        if (tipo == null) throw new IllegalArgumentException("El tipo de transacción no puede ser null.");
        this.valor = valor;
        this.billeteraOrigen = validarTexto(billeteraOrigen, "La billetera de origen");
        boolean esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA;
        if (esTransferencia) {
            this.billeteraDestino = validarTexto(billeteraDestino, "La billetera de destino");
        } else {
            if (billeteraDestino != null && !billeteraDestino.trim().isEmpty()) {
                throw new IllegalArgumentException("Una recarga o retiro no debe tener billetera destino.");
            }
            this.billeteraDestino = null;
        }
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.estado = validarTexto(estado, "El estado");
    }

    public String getId() { return id; }
    public double getValor() { return valor; }
    public String getBilleteraOrigen() { return billeteraOrigen; }
    public String getBilleteraDestino() { return billeteraDestino; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public TipoTransaccion getTipo() { return tipo; }
    public String getEstado() { return estado; }

    protected final void cambiarFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) throw new IllegalArgumentException("La fecha y hora no pueden ser null.");
        this.fechaHora = fechaHora;
    }

    protected final void cambiarEstado(String estado) {
        this.estado = validarTexto(estado, "El estado");
    }

    private static String validarTexto(String texto, String nombre) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(nombre + " no puede ser null ni estar vacío.");
        }
        return texto.trim();
    }
}
