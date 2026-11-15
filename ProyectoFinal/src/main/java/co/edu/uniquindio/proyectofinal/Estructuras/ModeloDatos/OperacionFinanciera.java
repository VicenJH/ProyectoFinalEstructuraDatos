package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import java.time.LocalDateTime;

/**
 * Representa la información común de una operación financiera.
 *
 * Es una clase abstracta que sirve como base para los diferentes tipos
 * de operaciones del sistema Fintech, como las transacciones y las
 * operaciones programadas.
 *
 * Contiene los datos generales de una operación:
 * - Identificador único.
 * - Valor monetario.
 * - Billetera de origen.
 * - Billetera de destino.
 * - Fecha y hora de ejecución o programación.
 * - Tipo de transacción.
 * - Estado de la operación.
 *
 * Las clases hijas pueden ampliar esta información con atributos
 * específicos, como puntos generados o frecuencia de ejecución.
 *
 * Los atributos se mantienen públicos para conservar la compatibilidad
 * con las clases existentes del proyecto. Se recomienda acceder a ellos
 * mediante los métodos getters y setters.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public abstract class OperacionFinanciera {

    /** Identificador de la operación financiera. */
    public String id;

    /** Valor monetario de la operación. Debe ser mayor que cero. */
    public double valor;

    /** Identificador de la billetera desde la que se origina la operación. */
    public String billeteraOrigen;

    /** Identificador de la billetera que recibe la operación, si aplica. */
    public String billeteraDestino;

    /** Fecha y hora asociadas a la operación. */
    public LocalDateTime fechaHora;

    /** Tipo de operación financiera realizada o programada. */
    public TipoTransaccion tipo;

    /** Estado actual de la operación. */
    public String estado;

    /**
     * Construye una operación financiera con sus datos principales.
     *
     * La validación del origen y destino de las billeteras se deja a las
     * clases o servicios que conocen las reglas específicas de cada tipo
     * de operación. Por ejemplo, una recarga no necesariamente necesita
     * una billetera de destino.
     *
     * @param id identificador único de la operación.
     * @param valor monto monetario de la operación.
     * @param billeteraOrigen identificador de la billetera de origen.
     * @param billeteraDestino identificador de la billetera de destino,
     *                         puede ser null cuando no aplique.
     * @param fechaHora fecha y hora de la operación.
     * @param tipo clasificación de la operación.
     * @param estado estado inicial de la operación.
     *
     * @throws IllegalArgumentException si el identificador o el estado
     * están vacíos, el valor no es positivo y finito, o la fecha o el
     * tipo son null.
     */
    public OperacionFinanciera(
            String id,
            double valor,
            String billeteraOrigen,
            String billeteraDestino,
            LocalDateTime fechaHora,
            TipoTransaccion tipo,
            String estado
    ) {

        validarTexto(id, "El identificador");
        validarValor(valor);
        validarFechaHora(fechaHora);
        validarTipo(tipo);
        validarTexto(estado, "El estado");

        this.id = id;
        this.valor = valor;
        this.billeteraOrigen = billeteraOrigen;
        this.billeteraDestino = billeteraDestino;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.estado = estado;
    }

    /**
     * Obtiene el identificador de la operación.
     *
     * @return identificador de la operación.
     *
     * Complejidad temporal: O(1).
     */
    public String getId() {
        return id;
    }

    /**
     * Modifica el identificador de la operación.
     *
     * @param id nuevo identificador.
     * @throws IllegalArgumentException si el identificador es null
     * o está vacío.
     *
     * Complejidad temporal: O(1).
     */
    public void setId(String id) {
        validarTexto(id, "El identificador");
        this.id = id;
    }

    /**
     * Obtiene el valor monetario de la operación.
     *
     * @return monto de la operación.
     *
     * Complejidad temporal: O(1).
     */
    public double getValor() {
        return valor;
    }

    /**
     * Modifica el valor monetario de la operación.
     *
     * @param valor nuevo monto.
     * @throws IllegalArgumentException si el monto es menor o igual
     * a cero, NaN o infinito.
     *
     * Complejidad temporal: O(1).
     */
    public void setValor(double valor) {
        validarValor(valor);
        this.valor = valor;
    }

    /**
     * Obtiene el identificador de la billetera de origen.
     *
     * @return identificador de la billetera de origen,
     * o null si no aplica.
     *
     * Complejidad temporal: O(1).
     */
    public String getBilleteraOrigen() {
        return billeteraOrigen;
    }

    /**
     * Modifica la billetera de origen.
     *
     * @param billeteraOrigen identificador de la billetera de origen.
     *
     * Complejidad temporal: O(1).
     */
    public void setBilleteraOrigen(String billeteraOrigen) {
        this.billeteraOrigen = billeteraOrigen;
    }

    /**
     * Obtiene el identificador de la billetera de destino.
     *
     * @return identificador de la billetera de destino,
     * o null si no aplica.
     *
     * Complejidad temporal: O(1).
     */
    public String getBilleteraDestino() {
        return billeteraDestino;
    }

    /**
     * Modifica la billetera de destino.
     *
     * @param billeteraDestino identificador de la billetera de destino.
     *
     * Complejidad temporal: O(1).
     */
    public void setBilleteraDestino(String billeteraDestino) {
        this.billeteraDestino = billeteraDestino;
    }

    /**
     * Obtiene la fecha y hora de la operación.
     *
     * @return fecha y hora registradas.
     *
     * Complejidad temporal: O(1).
     */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    /**
     * Modifica la fecha y hora de la operación.
     *
     * @param fechaHora nueva fecha y hora.
     * @throws IllegalArgumentException si la fecha es null.
     *
     * Complejidad temporal: O(1).
     */
    public void setFechaHora(LocalDateTime fechaHora) {
        validarFechaHora(fechaHora);
        this.fechaHora = fechaHora;
    }

    /**
     * Obtiene el tipo de transacción.
     *
     * @return tipo de la operación.
     *
     * Complejidad temporal: O(1).
     */
    public TipoTransaccion getTipo() {
        return tipo;
    }

    /**
     * Modifica el tipo de transacción.
     *
     * @param tipo nuevo tipo de operación.
     * @throws IllegalArgumentException si el tipo es null.
     *
     * Complejidad temporal: O(1).
     */
    public void setTipo(TipoTransaccion tipo) {
        validarTipo(tipo);
        this.tipo = tipo;
    }

    /**
     * Obtiene el estado actual de la operación.
     *
     * @return estado de la operación.
     *
     * Complejidad temporal: O(1).
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Modifica el estado de la operación.
     *
     * @param estado nuevo estado.
     * @throws IllegalArgumentException si el estado es null
     * o está vacío.
     *
     * Complejidad temporal: O(1).
     */
    public void setEstado(String estado) {
        validarTexto(estado, "El estado");
        this.estado = estado;
    }

    /**
     * Valida que un texto obligatorio no sea null ni esté vacío.
     *
     * Utiliza trim() para comprobar que el texto contenga al menos
     * un carácter distinto de espacios en blanco.
     *
     * @param texto texto que se desea validar.
     * @param nombre nombre del dato para construir el mensaje de error.
     *
     * @throws IllegalArgumentException si el texto es null o está vacío.
     */
    private static void validarTexto(String texto, String nombre) {

        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    nombre + " no puede ser null ni estar vacío."
            );
        }
    }

    /**
     * Valida que el valor monetario sea positivo y finito.
     *
     * @param valor monto que se desea validar.
     *
     * @throws IllegalArgumentException si el valor es menor o igual
     * a cero, NaN o infinito.
     */
    private static void validarValor(double valor) {

        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                    "El valor de la operación debe ser positivo y finito."
            );
        }
    }

    /**
     * Valida que la fecha y hora de la operación existan.
     *
     * @param fechaHora fecha que se desea validar.
     *
     * @throws IllegalArgumentException si la fecha es null.
     */
    private static void validarFechaHora(LocalDateTime fechaHora) {

        if (fechaHora == null) {
            throw new IllegalArgumentException(
                    "La fecha y hora de la operación no pueden ser null."
            );
        }
    }

    /**
     * Valida que la operación tenga un tipo de transacción definido.
     *
     * @param tipo tipo que se desea validar.
     *
     * @throws IllegalArgumentException si el tipo es null.
     */
    private static void validarTipo(TipoTransaccion tipo) {

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "El tipo de transacción no puede ser null."
            );
        }
    }
}