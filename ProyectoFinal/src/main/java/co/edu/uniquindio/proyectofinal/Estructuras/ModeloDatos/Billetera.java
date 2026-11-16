package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

/**
 * Representa una billetera perteneciente a un usuario del sistema Fintech.
 *
 * Almacena su código, nombre, categoría, saldo, estado, el usuario dueño
 * y el historial de transacciones en las que ha participado.
 *
 * El saldo se representa mediante int, por lo que los montos deben
 * expresarse en unidades monetarias enteras.
 *
 * @author Proyecto Fintech
 * @version 1.1
 */
public class Billetera {

    /** Código único de identificación de la billetera. */
    private final int codigo;

    /** Nombre descriptivo de la billetera (ej. "Ahorro vacaciones"). */
    private String nombre;

    /** Categoría de la billetera. */
    private TipoBilletera tipo;

    /** Saldo disponible expresado en unidades enteras. */
    private int saldo;

    /** Indica si la billetera está habilitada para operar. */
    private boolean activo;

    /** Identificador del usuario dueño de la billetera. */
    private String idUsuario;

    /** Historial de transacciones de esta billetera (origen o destino). */
    private SimpleLinkedList<Transaccion> historialTransacciones;

    /**
     * Constructor compatible con la versión anterior.
     * El nombre toma el del tipo y no tiene dueño asignado.
     */
    public Billetera(int codigo, TipoBilletera tipo, int saldo, boolean activo) {
        this(codigo, null, tipo, saldo, activo, null);
    }

    /**
     * Construye una billetera completa.
     *
     * @param codigo código positivo.
     * @param nombre nombre descriptivo; si es null o vacío se usa el del tipo.
     * @param tipo categoría, no null.
     * @param saldo saldo inicial, no negativo.
     * @param activo estado inicial.
     * @param idUsuario identificador del dueño (puede ser null).
     * @throws IllegalArgumentException si el código, el tipo o el saldo no son válidos.
     */
    public Billetera(
            int codigo,
            String nombre,
            TipoBilletera tipo,
            int saldo,
            boolean activo,
            String idUsuario
    ) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("El código de la billetera debe ser positivo.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de billetera no puede ser null.");
        }
        if (saldo < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo.");
        }

        this.codigo = codigo;
        this.nombre = (nombre == null || nombre.trim().isEmpty()) ? tipo.name() : nombre.trim();
        this.tipo = tipo;
        this.saldo = saldo;
        if (idUsuario != null && idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del dueño no puede estar vacío.");
        }
        this.activo = activo;
        this.idUsuario = idUsuario == null ? null : idUsuario.trim();
        this.historialTransacciones = new SimpleLinkedList<>();
    }

    /** @return código de la billetera. */
    public int getCodigo() {
        return codigo;
    }


    /** @return nombre descriptivo. */
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    /** @return categoría de la billetera. */
    public TipoBilletera getTipo() {
        return tipo;
    }

    public void setTipo(TipoBilletera tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser null.");
        }
        this.tipo = tipo;
    }

    /** @return saldo disponible. */
    public int getSaldo() {
        return saldo;
    }


    /** @return true si la billetera está activa. */
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /** @return identificador del usuario dueño (puede ser null). */
    public String getIdUsuario() {
        return idUsuario;
    }


    /** @return historial de transacciones de la billetera. */
    public SimpleLinkedList<Transaccion> getHistorialTransacciones() {
        return historialTransacciones;
    }

    /**
     * Indica si se puede sumar el monto al saldo sin desbordar un int.
     *
     * @param monto cantidad que se quiere agregar.
     * @return true si el resultado cabe en un int.
     */
    public boolean puedeRecargar(int monto) {
        return monto > 0 && (long) saldo + monto <= Integer.MAX_VALUE;
    }

    /**
     * Incrementa el saldo de la billetera.
     *
     * @param monto cantidad que se desea agregar.
     * @throws IllegalArgumentException si el monto no es positivo.
     * @throws ArithmeticException si el resultado supera Integer.MAX_VALUE.
     *
     * Complejidad temporal: O(1).
     */
    public void recargar(int monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto de la recarga debe ser positivo.");
        }
        saldo = Math.addExact(saldo, monto);
    }

    /**
     * Retira dinero de la billetera si existe saldo suficiente.
     *
     * @param monto cantidad que se desea retirar.
     * @return true si el retiro se realizó; false si no hay saldo suficiente.
     * @throws IllegalArgumentException si el monto no es positivo.
     *
     * Complejidad temporal: O(1).
     */
    public boolean retirar(int monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto del retiro debe ser positivo.");
        }
        if (saldo < monto) {
            return false;
        }
        saldo -= monto;
        return true;
    }
}