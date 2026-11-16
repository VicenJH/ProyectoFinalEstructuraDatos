package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

/** Usuario del sistema. Sus colecciones se consultan mediante copias defensivas. */
public class Usuario implements Comparable<Usuario> {

    private final String id;
    private String nombre;
    private String correo;
    private int puntosAcumulados;
    private String nivel;
    private final SimpleLinkedList<Billetera> billeteras;
    private final SimpleLinkedList<Transaccion> historialTransacciones;
    private final SimpleLinkedList<Alerta> historialAlertas;
    private final SimpleLinkedList<CanjeBeneficio> historialCanjes;
    private boolean activo;

    public Usuario(String id, String nombre, String correo) {
        this.id = validarTexto(id, "El identificador");
        this.nombre = validarTexto(nombre, "El nombre");
        this.correo = validarCorreo(correo);
        this.puntosAcumulados = 0;
        this.nivel = "Bronce";
        this.billeteras = new SimpleLinkedList<>();
        this.historialTransacciones = new SimpleLinkedList<>();
        this.historialAlertas = new SimpleLinkedList<>();
        this.historialCanjes = new SimpleLinkedList<>();
        this.activo = true;
    }

    private Usuario(Usuario original) {
        this.id = original.id;
        this.nombre = original.nombre;
        this.correo = original.correo;
        this.puntosAcumulados = original.puntosAcumulados;
        this.nivel = original.nivel;
        this.activo = original.activo;
        this.billeteras = new SimpleLinkedList<>();
        this.historialTransacciones = new SimpleLinkedList<>();
        this.historialAlertas = new SimpleLinkedList<>();
        this.historialCanjes = new SimpleLinkedList<>();
        for (Billetera billetera : original.billeteras) this.billeteras.addLast(billetera.copia());
        for (Transaccion transaccion : original.historialTransacciones) this.historialTransacciones.addLast(transaccion.copia());
        for (Alerta alerta : original.historialAlertas) this.historialAlertas.addLast(alerta.copia());
        for (CanjeBeneficio canje : original.historialCanjes) this.historialCanjes.addLast(canje.copia());
    }

    /** Copia profunda para consultas externas; no comparte saldos ni historiales mutables. */
    public Usuario copia() {
        return new Usuario(this);
    }

    public void actualizarNivel() {
        if (puntosAcumulados > 5000) nivel = "Platino";
        else if (puntosAcumulados > 1000) nivel = "Oro";
        else if (puntosAcumulados > 500) nivel = "Plata";
        else nivel = "Bronce";
    }

    public void agregarPuntos(int puntos) {
        if (puntos < 0) throw new IllegalArgumentException("No se pueden agregar puntos negativos.");
        puntosAcumulados = Math.addExact(puntosAcumulados, puntos);
        actualizarNivel();
    }

    public void descontarPuntos(int puntos) {
        if (puntos < 0) throw new IllegalArgumentException("La cantidad a descontar no puede ser negativa.");
        if (puntos > puntosAcumulados) throw new IllegalArgumentException("El usuario no tiene suficientes puntos.");
        puntosAcumulados -= puntos;
        actualizarNivel();
    }

    @Override
    public int compareTo(Usuario otro) {
        if (otro == null) throw new NullPointerException("No se puede comparar con un usuario null.");
        int comparacionPuntos = Integer.compare(puntosAcumulados, otro.puntosAcumulados);
        return comparacionPuntos != 0 ? comparacionPuntos : id.compareTo(otro.id);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = validarTexto(nombre, "El nombre"); }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = validarCorreo(correo); }

    /** Valida ambos campos antes de modificar cualquiera de ellos. */
    public void actualizarDatos(String nombre, String correo) {
        String nombreValidado = validarTexto(nombre, "El nombre");
        String correoValidado = validarCorreo(correo);
        this.nombre = nombreValidado;
        this.correo = correoValidado;
    }

    public int getPuntosAcumulados() { return puntosAcumulados; }
    public String getNivel() { return nivel; }
    public boolean isActivo() { return activo; }

    /** Devuelve billeteras independientes para que una vista no cambie los objetos internos. */
    public SimpleLinkedList<Billetera> getBilleteras() {
        SimpleLinkedList<Billetera> copia = new SimpleLinkedList<>();
        for (Billetera billetera : billeteras) copia.addLast(billetera.copia());
        return copia;
    }

    public SimpleLinkedList<Transaccion> getHistorialTransacciones() {
        SimpleLinkedList<Transaccion> copia = new SimpleLinkedList<>();
        for (Transaccion transaccion : historialTransacciones) copia.addLast(transaccion.copia());
        return copia;
    }

    public SimpleLinkedList<Alerta> getHistorialAlertas() {
        SimpleLinkedList<Alerta> copia = new SimpleLinkedList<>();
        for (Alerta alerta : historialAlertas) copia.addLast(alerta.copia());
        return copia;
    }

    public SimpleLinkedList<CanjeBeneficio> getHistorialCanjes() {
        SimpleLinkedList<CanjeBeneficio> copia = new SimpleLinkedList<>();
        for (CanjeBeneficio canje : historialCanjes) copia.addLast(canje.copia());
        return copia;
    }

    /* Operaciones de dominio: las utiliza la fachada del sistema. */
    public void agregarBilletera(Billetera billetera) {
        if (billetera == null || !id.equals(billetera.getIdUsuario())) {
            throw new IllegalArgumentException("La billetera debe pertenecer a este usuario.");
        }
        for (Billetera existente : billeteras) {
            if (existente.getCodigo() == billetera.getCodigo()) {
                throw new IllegalArgumentException("El usuario ya tiene una billetera con ese código.");
            }
        }
        billeteras.addLast(billetera);
    }

    public void desactivarBilleteras() {
        for (Billetera billetera : billeteras) billetera.setActivo(false);
    }

    public void registrarTransaccion(Transaccion transaccion) {
        if (transaccion == null) throw new IllegalArgumentException("La transacción no puede ser null.");
        historialTransacciones.addLast(transaccion);
    }

    public void registrarAlerta(Alerta alerta) {
        if (alerta == null) throw new IllegalArgumentException("La alerta no puede ser null.");
        historialAlertas.addLast(alerta);
    }

    public void registrarCanje(CanjeBeneficio canje) {
        if (canje == null || !id.equals(canje.getIdUsuario())) {
            throw new IllegalArgumentException("El canje no pertenece a este usuario.");
        }
        historialCanjes.addLast(canje);
    }

    /** Baja lógica; la fachada mantiene sincronizado el árbol de usuarios activos. */
    public void setActivo(boolean activo) { this.activo = activo; }

    private static String validarTexto(String texto, String campo) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return texto.trim();
    }

    private static String validarCorreo(String correo) {
        String valor = validarTexto(correo, "El correo");
        if (!valor.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        return valor;
    }
}
