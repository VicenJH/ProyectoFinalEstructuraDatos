package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Datos comunes de una operación financiera. La identidad, el monto, las
 * billeteras y el tipo quedan inmutables después de crear la operación.
 * Solo las subclases pueden modificar estado y fecha cuando su ciclo de vida
 * lo requiere (reversión o reprogramación).
 */
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
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException("El valor de la operación debe ser positivo y finito.");
        }
        if (fechaHora == null) throw new IllegalArgumentException("La fecha y hora no pueden ser null.");
        if (tipo == null) throw new IllegalArgumentException("El tipo de transacción no puede ser null.");
        this.valor = valor;
        this.billeteraOrigen = validarTexto(billeteraOrigen, "La billetera de origen");
        if (billeteraDestino != null && billeteraDestino.trim().isEmpty()) {
            throw new IllegalArgumentException("La billetera de destino no puede estar vacía.");
        }
        boolean esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA;
        if (esTransferencia && billeteraDestino == null) {
            throw new IllegalArgumentException("Una transferencia debe indicar la billetera de destino.");
        }
        this.billeteraDestino = billeteraDestino == null ? null : billeteraDestino.trim();
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
