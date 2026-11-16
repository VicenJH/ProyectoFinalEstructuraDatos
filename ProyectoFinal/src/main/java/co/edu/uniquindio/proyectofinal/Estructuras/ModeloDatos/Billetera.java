package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

/** Billetera de un usuario. Los montos se expresan en unidades monetarias enteras. */
public class Billetera {

    private final int codigo;
    private String nombre;
    private TipoBilletera tipo;
    private int saldo;
    private boolean activo;
    private final String idUsuario;
    private final SimpleLinkedList<Transaccion> historialTransacciones;

    /** Constructor legado para ejercicios aislados; el sistema exige dueño al registrar. */
    public Billetera(int codigo, TipoBilletera tipo, int saldo, boolean activo) {
        this(codigo, null, tipo, saldo, activo, null);
    }

    public Billetera(int codigo, String nombre, TipoBilletera tipo, int saldo, boolean activo, String idUsuario) {
        if (codigo <= 0) throw new IllegalArgumentException("El código de la billetera debe ser positivo.");
        if (tipo == null) throw new IllegalArgumentException("El tipo de billetera no puede ser null.");
        if (saldo < 0) throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        if (idUsuario != null && idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del dueño no puede estar vacío.");
        }
        this.codigo = codigo;
        this.nombre = nombre == null || nombre.trim().isEmpty() ? tipo.name() : nombre.trim();
        this.tipo = tipo;
        this.saldo = saldo;
        this.activo = activo;
        this.idUsuario = idUsuario == null ? null : idUsuario.trim();
        this.historialTransacciones = new SimpleLinkedList<>();
    }

    private Billetera(Billetera original) {
        this(original.codigo, original.nombre, original.tipo, original.saldo, original.activo, original.idUsuario);
        for (Transaccion transaccion : original.historialTransacciones) {
            this.historialTransacciones.addLast(transaccion.copia());
        }
    }

    public Billetera copia() { return new Billetera(this); }

    public int getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("El nombre no puede estar vacío.");
        this.nombre = nombre.trim();
    }
    public TipoBilletera getTipo() { return tipo; }
    public void setTipo(TipoBilletera tipo) {
        if (tipo == null) throw new IllegalArgumentException("El tipo no puede ser null.");
        this.tipo = tipo;
    }
    public int getSaldo() { return saldo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getIdUsuario() { return idUsuario; }

    /** Historial independiente para impedir que una vista altere registros internos. */
    public SimpleLinkedList<Transaccion> getHistorialTransacciones() {
        SimpleLinkedList<Transaccion> copia = new SimpleLinkedList<>();
        for (Transaccion transaccion : historialTransacciones) copia.addLast(transaccion.copia());
        return copia;
    }

    public void registrarTransaccion(Transaccion transaccion) {
        if (transaccion == null) throw new IllegalArgumentException("La transacción no puede ser null.");
        String codigoTexto = String.valueOf(codigo);
        if (!codigoTexto.equals(transaccion.getBilleteraOrigen())
                && !codigoTexto.equals(transaccion.getBilleteraDestino())) {
            throw new IllegalArgumentException("La transacción no referencia esta billetera.");
        }
        historialTransacciones.addLast(transaccion);
    }

    public boolean puedeRecargar(int monto) {
        return monto > 0 && (long) saldo + monto <= Integer.MAX_VALUE;
    }

    public void recargar(int monto) {
        if (monto <= 0) throw new IllegalArgumentException("El monto de la recarga debe ser positivo.");
        saldo = Math.addExact(saldo, monto);
    }

    public boolean retirar(int monto) {
        if (monto <= 0) throw new IllegalArgumentException("El monto del retiro debe ser positivo.");
        if (saldo < monto) return false;
        saldo -= monto;
        return true;
    }
}
