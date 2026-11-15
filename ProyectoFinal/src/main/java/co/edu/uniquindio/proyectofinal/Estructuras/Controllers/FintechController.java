package co.edu.uniquindio.proyectofinal.Estructuras.Controllers;

import co.edu.uniquindio.proyectofinal.Estructuras.Arbol.ArbolBinario;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.Cola;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.ColaPrioridad;
import co.edu.uniquindio.proyectofinal.Estructuras.Grafos.Grafo;
import co.edu.uniquindio.proyectofinal.Estructuras.Hash.TablaHash;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;
import co.edu.uniquindio.proyectofinal.Estructuras.Pilas.Stack;
import co.edu.uniquindio.proyectofinal.Estructuras.Servicios.AnaliticaFintech;
import co.edu.uniquindio.proyectofinal.Estructuras.Servicios.DetectorRiesgo;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.time.LocalDateTime;

/**
 * Fachada del sistema Fintech. La interfaz gráfica solo debe llamar
 * a los métodos públicos de esta clase.
 *
 * Estructuras utilizadas:
 * - TablaHash: usuarios por id y billeteras por código (acceso O(1)).
 * - ArbolBinario: usuarios ordenados por puntos.
 * - ColaPrioridad: operaciones programadas ordenadas por fecha y hora.
 * - Stack: transacciones reversibles (LIFO).
 * - Grafo: transferencias entre usuarios (peso = monto acumulado).
 */
public class FintechController {

    /** Saldo por debajo del cual se genera una alerta de saldo bajo. */
    private static final int UMBRAL_SALDO_BAJO = 10000;

    /** Bono de puntos por pago programado ejecutado con éxito. */
    private static final int BONO_PUNTOS_PROGRAMADA = 10;

    private TablaHash<String, Usuario> usuariosPorId;
    private TablaHash<Integer, Billetera> billeterasPorCodigo;
    private ArbolBinario<Usuario> arbolUsuariosPorPuntos;
    private ColaPrioridad<OperacionProgramada> operacionesProgramadas;
    private SimpleLinkedList<OperacionProgramada> operacionesProcesadas;
    private Stack<Transaccion> pilaReversiones;
    private Grafo<String> grafoTransferencias;

    /** Todas las transacciones registradas en orden cronológico (base de la analítica). */
    private SimpleLinkedList<Transaccion> historialGlobal;

    /** Lista de usuarios registrados, para recorridos de analítica. */
    private SimpleLinkedList<Usuario> listaUsuarios;

    private DetectorRiesgo detectorRiesgo;
    private AnaliticaFintech analitica;

    private int contadorAlertas;
    private long contadorTransacciones;

    public FintechController() {
        this.usuariosPorId = new TablaHash<>(100);
        this.billeterasPorCodigo = new TablaHash<>(500);
        this.arbolUsuariosPorPuntos = new ArbolBinario<>();
        this.operacionesProgramadas = new ColaPrioridad<>(
                (a, b) -> a.getFechaHora().compareTo(b.getFechaHora())
        );
        this.operacionesProcesadas = new SimpleLinkedList<>();
        this.pilaReversiones = new Stack<>();
        this.grafoTransferencias = new Grafo<>();
        this.historialGlobal = new SimpleLinkedList<>();
        this.listaUsuarios = new SimpleLinkedList<>();
        this.detectorRiesgo = new DetectorRiesgo();
        this.analitica = new AnaliticaFintech(
                historialGlobal, listaUsuarios, billeterasPorCodigo,
                arbolUsuariosPorPuntos, grafoTransferencias
        );
        this.contadorAlertas = 0;
        this.contadorTransacciones = 0;
    }

    // ------------------------------------------------------------------
    // GESTIÓN DE USUARIOS
    // ------------------------------------------------------------------

    public boolean registrarUsuario(String id, String nombre, String correo) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        if (usuariosPorId.buscar(id) != null) {
            return false;
        }

        Usuario nuevoUsuario;
        try {
            nuevoUsuario = new Usuario(id, nombre, correo);
        } catch (IllegalArgumentException e) {
            return false;
        }

        usuariosPorId.insertar(id, nuevoUsuario);
        arbolUsuariosPorPuntos.insertar(nuevoUsuario);
        grafoTransferencias.agregarVertice(id);
        listaUsuarios.addLast(nuevoUsuario);
        return true;
    }

    public Usuario buscarUsuario(String id) {
        if (id == null) {
            return null;
        }
        return usuariosPorId.buscar(id);
    }

    /** Modifica nombre y correo de un usuario. No toca id ni puntos. */
    public boolean modificarUsuario(String id, String nombre, String correo) {
        Usuario usuario = buscarUsuario(id);

        if (usuario == null
                || nombre == null || nombre.trim().isEmpty()
                || correo == null || correo.trim().isEmpty()) {
            return false;
        }

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        return true;
    }

    /**
     * Elimina un usuario junto con sus billeteras.
     * El vértice del grafo se conserva como registro histórico de transferencias.
     */
    public boolean eliminarUsuario(String id) {
        Usuario usuario = buscarUsuario(id);

        if (usuario == null) {
            return false;
        }

        // El árbol se ordena por puntos: hay que eliminar con los puntos actuales.
        arbolUsuariosPorPuntos.eliminar(usuario);

        for (int i = 0; i < usuario.getBilleteras().getSize(); i++) {
            billeterasPorCodigo.eliminar(usuario.getBilleteras().getNodeValue(i).getCodigo());
        }

        usuariosPorId.eliminar(id);
        listaUsuarios.remove(usuario);
        return true;
    }

    // ------------------------------------------------------------------
    // GESTIÓN DE BILLETERAS
    // ------------------------------------------------------------------

    public boolean crearBilleteraAUsuario(String idUsuario, int codigoBilletera,
                                          TipoBilletera tipo, int saldoInicial) {
        return crearBilleteraAUsuario(idUsuario, codigoBilletera, null, tipo, saldoInicial);
    }

    public boolean crearBilleteraAUsuario(String idUsuario, int codigoBilletera, String nombre,
                                          TipoBilletera tipo, int saldoInicial) {
        Usuario usuario = buscarUsuario(idUsuario);
        if (usuario == null) {
            return false;
        }
        if (billeterasPorCodigo.buscar(codigoBilletera) != null) {
            return false;
        }

        Billetera nuevaBilletera;
        try {
            nuevaBilletera = new Billetera(codigoBilletera, nombre, tipo, saldoInicial, true, idUsuario);
        } catch (IllegalArgumentException e) {
            return false;
        }

        usuario.getBilleteras().addLast(nuevaBilletera);
        billeterasPorCodigo.insertar(codigoBilletera, nuevaBilletera);
        return true;
    }

    public Billetera buscarBilletera(int codigo) {
        return billeterasPorCodigo.buscar(codigo);
    }

    /** @return saldo actual o null si la billetera no existe. */
    public Integer consultarSaldo(int codigoBilletera) {
        Billetera billetera = billeterasPorCodigo.buscar(codigoBilletera);
        return billetera == null ? null : billetera.getSaldo();
    }

    // ------------------------------------------------------------------
    // OPERACIONES FINANCIERAS
    // ------------------------------------------------------------------

    public boolean realizarRecarga(int codigoBilletera, int monto, String idUsuario) {
        return ejecutarRecarga(codigoBilletera, monto, idUsuario, false) != null;
    }

    public boolean realizarRetiro(int codigoBilletera, int monto, String idUsuario) {
        return ejecutarRetiro(codigoBilletera, monto, idUsuario, false) != null;
    }

    /**
     * Transfiere dinero entre dos billeteras (del mismo o de otro usuario).
     * El monto debe ser un valor entero positivo.
     */
    public boolean realizarTransferencia(int codigoOrigen, int codigoDestino,
                                         double monto, String idUsuarioOrigen) {
        return ejecutarTransferencia(codigoOrigen, codigoDestino, monto, idUsuarioOrigen, false) != null;
    }

    private Transaccion ejecutarRecarga(int codigo, double montoBruto, String idUsuario, boolean programada) {
        Billetera billetera = billeterasPorCodigo.buscar(codigo);
        Usuario usuario = buscarUsuario(idUsuario);

        if (billetera == null || usuario == null || !usuario.getId().equals(billetera.getIdUsuario())) {
            return null;
        }

        Integer monto = convertirMonto(montoBruto);
        if (monto == null) {
            return rechazar(usuario, "Monto inválido para la recarga.");
        }
        if (!billetera.isActivo()) {
            return rechazar(usuario, "La billetera " + codigo + " está inactiva.");
        }
        if (!billetera.puedeRecargar(monto)) {
            return rechazar(usuario, "La recarga supera el saldo máximo permitido.");
        }

        DetectorRiesgo.Evaluacion riesgo = detectorRiesgo.evaluar(
                usuario.getId(), monto, false, codigo, null, LocalDateTime.now());

        billetera.recargar(monto);

        int puntos = monto / 100 + (programada ? BONO_PUNTOS_PROGRAMADA : 0);
        return registrar(usuario, billetera, null, monto, TipoTransaccion.RECARGA, puntos, riesgo);
    }

    private Transaccion ejecutarRetiro(int codigo, double montoBruto, String idUsuario, boolean programada) {
        Billetera billetera = billeterasPorCodigo.buscar(codigo);
        Usuario usuario = buscarUsuario(idUsuario);

        if (billetera == null || usuario == null || !usuario.getId().equals(billetera.getIdUsuario())) {
            return null;
        }

        Integer monto = convertirMonto(montoBruto);
        if (monto == null) {
            return rechazar(usuario, "Monto inválido para el retiro.");
        }
        if (!billetera.isActivo()) {
            return rechazar(usuario, "La billetera " + codigo + " está inactiva.");
        }
        if (billetera.getSaldo() < monto) {
            return rechazar(usuario, "Saldo insuficiente en la billetera " + codigo + ".");
        }

        DetectorRiesgo.Evaluacion riesgo = detectorRiesgo.evaluar(
                usuario.getId(), monto, true, codigo, null, LocalDateTime.now());

        billetera.retirar(monto);

        int puntos = (monto / 100) * 2 + (programada ? BONO_PUNTOS_PROGRAMADA : 0);
        return registrar(usuario, billetera, null, monto, TipoTransaccion.RETIRO, puntos, riesgo);
    }

    private Transaccion ejecutarTransferencia(int codOrigen, int codDestino, double montoBruto,
                                              String idUsuario, boolean programada) {
        Billetera origen = billeterasPorCodigo.buscar(codOrigen);
        Billetera destino = billeterasPorCodigo.buscar(codDestino);
        Usuario usuario = buscarUsuario(idUsuario);

        if (origen == null || destino == null || usuario == null
                || !usuario.getId().equals(origen.getIdUsuario())) {
            return null;
        }

        Integer monto = convertirMonto(montoBruto);
        if (monto == null) {
            return rechazar(usuario, "Monto inválido para la transferencia.");
        }
        if (codOrigen == codDestino) {
            return rechazar(usuario, "La billetera de origen y destino no pueden ser la misma.");
        }
        if (!origen.isActivo() || !destino.isActivo()) {
            return rechazar(usuario, "Alguna de las billeteras está inactiva.");
        }
        if (origen.getSaldo() < monto) {
            return rechazar(usuario, "Saldo insuficiente en la billetera " + codOrigen + ".");
        }
        if (!destino.puedeRecargar(monto)) {
            return rechazar(usuario, "La billetera destino no puede recibir ese monto.");
        }

        DetectorRiesgo.Evaluacion riesgo = detectorRiesgo.evaluar(
                usuario.getId(), monto, true, codOrigen, codDestino, LocalDateTime.now());

        if (riesgo.getNivel() != NivelRiesgo.BAJO) {
            System.out.println("⚠️ La transacción generó una alerta de seguridad, pero fue procesada bajo supervisión.");
        }

        origen.retirar(monto);
        destino.recargar(monto);

        int puntos = (monto / 100) * 3 + (programada ? BONO_PUNTOS_PROGRAMADA : 0);
        return registrar(usuario, origen, destino, monto, TipoTransaccion.TRANSFERENCIA_INTERNA, puntos, riesgo);
    }

    /**
     * Registra una operación ya aplicada: historiales, pila de reversiones,
     * grafo, puntos y alerta de saldo bajo.
     */
    private Transaccion registrar(Usuario actor, Billetera origen, Billetera destino,
                                  int monto, TipoTransaccion tipo, int puntos,
                                  DetectorRiesgo.Evaluacion riesgo) {
        contadorTransacciones++;

        Transaccion tx = new Transaccion(
                "TX-" + contadorTransacciones,
                monto,
                String.valueOf(origen.getCodigo()),
                destino == null ? null : String.valueOf(destino.getCodigo()),
                LocalDateTime.now(),
                tipo,
                "EXITOSA",
                puntos
        );

        tx.setNivelRiesgo(riesgo.getNivel());
        historialGlobal.addLast(tx);
        actor.getHistorialTransacciones().addLast(tx);
        origen.getHistorialTransacciones().addLast(tx);

        if (destino != null) {
            destino.getHistorialTransacciones().addLast(tx);

            Usuario duenoDestino = buscarUsuario(destino.getIdUsuario());

            if (duenoDestino != null && !duenoDestino.getId().equals(actor.getId())) {
                duenoDestino.getHistorialTransacciones().addLast(tx);
                registrarTransferenciaEnGrafo(actor.getId(), duenoDestino.getId(), monto);
            }
        }

        pilaReversiones.push(tx);

        detectorRiesgo.registrarOperacion(
                actor.getId(), monto, tipo != TipoTransaccion.RECARGA, origen.getCodigo(),
                destino == null ? null : destino.getCodigo(), tx.getFechaHora());

        if (riesgo.getNivel() != NivelRiesgo.BAJO) {
            detectorRiesgo.registrarEvento(actor.getId(), tx.getId(), riesgo, tx.getFechaHora());
            generarAlertaParaUsuario(actor,
                    "Riesgo " + riesgo.getNivel() + " en " + tx.getId() + ": " + riesgo.getMotivo(),
                    "SEGURIDAD");
        }

        otorgarPuntos(actor, puntos);

        if (tipo != TipoTransaccion.RECARGA && origen.getSaldo() < UMBRAL_SALDO_BAJO) {
            generarAlertaParaUsuario(actor,
                    "Saldo bajo en la billetera " + origen.getCodigo() + ": $" + origen.getSaldo(),
                    "SALDO_BAJO");
        }

        return tx;
    }

    private Transaccion rechazar(Usuario usuario, String motivo) {
        generarAlertaParaUsuario(usuario, motivo, "OPERACION_RECHAZADA");
        return null;
    }

    /** Convierte a int solo montos enteros, positivos y representables. */
    private Integer convertirMonto(double monto) {
        if (!Double.isFinite(monto) || monto <= 0
                || monto != Math.rint(monto) || monto > Integer.MAX_VALUE) {
            return null;
        }
        return (int) monto;
    }

    // ------------------------------------------------------------------
    // PUNTOS (el árbol se mantiene consistente)
    // ------------------------------------------------------------------

    /**
     * Suma puntos al usuario. Como el árbol ordena por puntos, el usuario
     * se saca antes de cambiar su puntaje y se reinserta después.
     */
    private void otorgarPuntos(Usuario usuario, int puntos) {
        if (puntos <= 0) {
            return;
        }

        String nivelAnterior = usuario.getNivel();

        arbolUsuariosPorPuntos.eliminar(usuario);
        try {
            usuario.agregarPuntos(puntos);
        } finally {
            arbolUsuariosPorPuntos.insertar(usuario);
        }

        if (!nivelAnterior.equals(usuario.getNivel())) {
            generarAlertaParaUsuario(usuario,
                    "¡Ascendiste al nivel " + usuario.getNivel() + "!", "ASCENSO_NIVEL");
        }
    }

    /** Resta puntos sin dejar el saldo de puntos negativo. */
    private void quitarPuntos(Usuario usuario, int puntos) {
        int aDescontar = Math.min(puntos, usuario.getPuntosAcumulados());

        if (aDescontar <= 0) {
            return;
        }

        arbolUsuariosPorPuntos.eliminar(usuario);
        try {
            usuario.descontarPuntos(aDescontar);
        } finally {
            arbolUsuariosPorPuntos.insertar(usuario);
        }
    }

    // ------------------------------------------------------------------
    // REVERSIONES (PILA)
    // ------------------------------------------------------------------

    /**
     * Revierte la última operación exitosa (LIFO): ajusta saldos, estado
     * y descuenta los puntos que había generado.
     *
     * @return true si se revirtió. Si el destino ya no tiene fondos
     * suficientes, la operación se conserva en la pila y devuelve false.
     */
    public boolean revertirUltimaOperacion() {
        if (pilaReversiones.isEmpty()) {
            return false;
        }

        Transaccion tx = pilaReversiones.peek();

        if (!"EXITOSA".equals(tx.getEstado())) {
            pilaReversiones.pop();
            return false;
        }

        int monto = (int) tx.getValor();
        Billetera principal = billeterasPorCodigo.buscar(Integer.parseInt(tx.getBilleteraOrigen()));
        Billetera secundaria = tx.getBilleteraDestino() == null
                ? null
                : billeterasPorCodigo.buscar(Integer.parseInt(tx.getBilleteraDestino()));

        // Si una billetera ya no existe, la operación nunca podrá revertirse.
        if (principal == null
                || (tx.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA && secundaria == null)) {
            pilaReversiones.pop();
            return false;
        }

        switch (tx.getTipo()) {
            case TRANSFERENCIA_INTERNA:
                if (secundaria.getSaldo() < monto || !principal.puedeRecargar(monto)) {
                    return false;
                }
                secundaria.retirar(monto);
                principal.recargar(monto);
                revertirPesoEnGrafo(principal.getIdUsuario(), secundaria.getIdUsuario(), monto);
                break;

            case RECARGA:
                if (principal.getSaldo() < monto) {
                    return false;
                }
                principal.retirar(monto);
                break;

            case RETIRO:
                if (!principal.puedeRecargar(monto)) {
                    return false;
                }
                principal.recargar(monto);
                break;

            default:
                pilaReversiones.pop();
                return false;
        }

        Usuario actor = buscarUsuario(principal.getIdUsuario());
        if (actor != null) {
            quitarPuntos(actor, tx.getPuntosGenerados());
        }

        tx.setEstado("REVERTIDA");
        pilaReversiones.pop();
        return true;
    }

    // ------------------------------------------------------------------
    // OPERACIONES PROGRAMADAS (COLA DE PRIORIDAD)
    // ------------------------------------------------------------------

    /**
     * Programa una operación. El usuario responsable es el dueño de la
     * billetera de origen. Para RECARGA y RETIRO se ignora el destino.
     *
     * @return false si las billeteras no existen o los datos son inválidos.
     */
    public boolean programarOperacion(String id, double valor, int codOrigen, int codDestino,
                                      LocalDateTime fechaHora, TipoTransaccion tipo,
                                      Frecuencia frecuencia, String estado) {
        Billetera origen = billeterasPorCodigo.buscar(codOrigen);

        if (origen == null || origen.getIdUsuario() == null) {
            return false;
        }

        boolean esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA;

        if (esTransferencia && billeterasPorCodigo.buscar(codDestino) == null) {
            return false;
        }

        try {
            operacionesProgramadas.encolar(new OperacionProgramada(
                    id, valor,
                    String.valueOf(codOrigen), String.valueOf(codDestino),
                    fechaHora, tipo, frecuencia, estado,
                    origen.getIdUsuario()
            ));
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    /**
     * Ejecuta todas las operaciones cuya fecha ya llegó, en orden de fecha.
     * Las periódicas (SEMANAL, MENSUAL) se reprograman a la siguiente fecha
     * posterior a este momento; las UNICA pasan al registro de procesadas.
     *
     * @return cantidad de operaciones ejecutadas con éxito.
     */
    public int procesarOperacionesProgramadas() {
        LocalDateTime ahora = LocalDateTime.now();
        int ejecutadas = 0;

        while (!operacionesProgramadas.isEmpty()
                && !operacionesProgramadas.consultarPrimero().getFechaHora().isAfter(ahora)) {

            OperacionProgramada op = operacionesProgramadas.desencolar();
            boolean exito = ejecutarOperacionProgramada(op) != null;

            if (exito) {
                ejecutadas++;
            }

            if (op.getFrecuencia() == Frecuencia.UNICA) {
                op.setEstado(exito ? "EJECUTADA" : "FALLIDA");
                operacionesProcesadas.addLast(op);
            } else {
                op.setFechaHora(siguienteFecha(op.getFechaHora(), op.getFrecuencia(), ahora));
                operacionesProgramadas.encolar(op);
            }
        }

        return ejecutadas;
    }

    private Transaccion ejecutarOperacionProgramada(OperacionProgramada op) {
        int codOrigen = Integer.parseInt(op.getBilleteraOrigen());
        Billetera origen = billeterasPorCodigo.buscar(codOrigen);

        String idUsuario = op.getIdUsuario() != null
                ? op.getIdUsuario()
                : (origen == null ? null : origen.getIdUsuario());

        switch (op.getTipo()) {
            case RECARGA:
                return ejecutarRecarga(codOrigen, op.getValor(), idUsuario, true);
            case RETIRO:
                return ejecutarRetiro(codOrigen, op.getValor(), idUsuario, true);
            default:
                return ejecutarTransferencia(
                        codOrigen,
                        Integer.parseInt(op.getBilleteraDestino()),
                        op.getValor(),
                        idUsuario,
                        true
                );
        }
    }

    private LocalDateTime siguienteFecha(LocalDateTime actual, Frecuencia frecuencia, LocalDateTime ahora) {
        LocalDateTime fecha = actual;

        do {
            fecha = frecuencia == Frecuencia.SEMANAL ? fecha.plusWeeks(1) : fecha.plusMonths(1);
        } while (!fecha.isAfter(ahora));

        return fecha;
    }

    /** @return operaciones pendientes ordenadas por fecha de ejecución. */
    public SimpleLinkedList<OperacionProgramada> obtenerOperacionesPendientes() {
        return operacionesProgramadas.aListaOrdenada();
    }

    /** @return operaciones únicas ya procesadas (ejecutadas o fallidas). */
    public SimpleLinkedList<OperacionProgramada> obtenerOperacionesProcesadas() {
        return operacionesProcesadas;
    }

    // ------------------------------------------------------------------
    // REPORTES Y ÁRBOL DE PUNTOS
    // ------------------------------------------------------------------

    public void mostrarUsuariosOrdenadosPorPuntos() {
        System.out.println("--- USUARIOS ORDENADOS POR PUNTOS (INORDEN) ---");
        arbolUsuariosPorPuntos.recorrerInorden();
    }

    public boolean verificarExistenciaEnArbol(Usuario usuario) {
        return arbolUsuariosPorPuntos.existe(usuario);
    }

    public int obtenerTotalUsuariosEnArbol() {
        return arbolUsuariosPorPuntos.obtenerPeso();
    }

    // ------------------------------------------------------------------
    // GRAFO DE TRANSFERENCIAS
    // ------------------------------------------------------------------

    /**
     * Registra una transferencia entre usuarios. Si la arista ya existe,
     * Grafo.agregarArista acumula el monto sobre el peso anterior.
     */
    public void registrarTransferenciaEnGrafo(String idUsuarioOrigen, String idUsuarioDestino, double monto) {
        grafoTransferencias.agregarVertice(idUsuarioOrigen);
        grafoTransferencias.agregarVertice(idUsuarioDestino);
        grafoTransferencias.agregarArista(idUsuarioOrigen, idUsuarioDestino, monto);
    }

    /** Resta de la arista el monto de una transferencia revertida (sin bajar de 0). */
    private void revertirPesoEnGrafo(String idOrigen, String idDestino, int monto) {
        if (idOrigen == null || idDestino == null || idOrigen.equals(idDestino)) {
            return;
        }

        Double peso = grafoTransferencias.obtenerPesoArista(idOrigen, idDestino);

        if (peso != null) {
            grafoTransferencias.actualizarPesoArista(idOrigen, idDestino, Math.max(0, peso - monto));
        }
    }

    /** @return id del usuario con más conexiones salientes, o null si no hay usuarios. */
    public String obtenerUsuarioConMayorActividadRed() {
        SimpleLinkedList<Grafo.Vertice<String>> vertices = grafoTransferencias.getVertices();

        if (vertices.isEmpty()) {
            return null;
        }

        String usuarioMax = null;
        int maxConexiones = -1;

        for (int i = 0; i < vertices.getSize(); i++) {
            Grafo.Vertice<String> v = vertices.getNodeValue(i);
            int conexiones = v.getAdyacentes().getSize();

            if (conexiones > maxConexiones) {
                maxConexiones = conexiones;
                usuarioMax = v.getDato();
            }
        }

        return usuarioMax;
    }

    /**
     * Verifica con BFS si existe una ruta (directa o indirecta) de
     * transferencias entre dos usuarios. Termina siempre porque cada
     * vértice se visita una sola vez.
     */
    public boolean existeRutaTransferencias(String idOrigen, String idDestino) {
        if (idOrigen == null || idDestino == null) {
            return false;
        }

        Grafo.Vertice<String> inicio = grafoTransferencias.buscarVertice(idOrigen);
        Grafo.Vertice<String> fin = grafoTransferencias.buscarVertice(idDestino);

        if (inicio == null || fin == null) {
            return false;
        }
        if (idOrigen.equals(idDestino)) {
            return true;
        }

        Cola<Grafo.Vertice<String>> cola = new Cola<>();
        TablaHash<String, Boolean> visitados = new TablaHash<>(101);

        cola.encolar(inicio);
        visitados.insertar(inicio.getDato(), true);

        while (!cola.isEmpty()) {
            Grafo.Vertice<String> actual = cola.desencolar();

            for (int i = 0; i < actual.getAdyacentes().getSize(); i++) {
                Grafo.Arista<String> arista = actual.getAdyacentes().getNodeValue(i);

                // Una arista con peso 0 corresponde a una transferencia revertida.
                if (arista.getPeso() <= 0) {
                    continue;
                }

                Grafo.Vertice<String> vecino = arista.getDestino();

                if (vecino.getDato().equals(idDestino)) {
                    return true;
                }

                if (visitados.buscar(vecino.getDato()) == null) {
                    visitados.insertar(vecino.getDato(), true);
                    cola.encolar(vecino);
                }
            }
        }

        return false;
    }

    // ------------------------------------------------------------------
    // COMPORTAMIENTO INUSUAL Y ALERTAS
    // ------------------------------------------------------------------

    /**
     * Evalúa una operación hipotética sin ejecutarla y, si es sospechosa,
     * guarda una alerta de seguridad. Se conserva por compatibilidad: las
     * operaciones reales ya se evalúan internamente con todos los criterios.
     */
    public boolean evaluarYGenerarAlertaInusual(Usuario usuario, double montoActual, String idTransaccion) {
        int monto = (int) Math.min(montoActual, Integer.MAX_VALUE);

        if (monto <= 0) {
            return false;
        }

        DetectorRiesgo.Evaluacion evaluacion = detectorRiesgo.evaluar(
                usuario.getId(), monto, true, -1, null, LocalDateTime.now());

        if (evaluacion.getNivel() == NivelRiesgo.BAJO) {
            return false;
        }

        generarAlertaParaUsuario(usuario, evaluacion.getMotivo(), "SEGURIDAD");
        return true;
    }

    // ------------------------------------------------------------------
    // CONSULTAS PARA LA INTERFAZ
    // ------------------------------------------------------------------

    /** @return servicio con los informes y consultas de analítica. */
    public AnaliticaFintech getAnalitica() {
        return analitica;
    }

    /** @return todas las transacciones registradas, en orden cronológico. */
    public SimpleLinkedList<Transaccion> obtenerHistorialGlobal() {
        return historialGlobal;
    }

    /** @return historial de auditoría de riesgo (eventos MEDIO y ALTO). */
    public SimpleLinkedList<EventoAuditoria> obtenerHistorialAuditoria() {
        return detectorRiesgo.obtenerHistorialAuditoria();
    }

    public SimpleLinkedList<EventoAuditoria> obtenerEventosAuditoriaDeUsuario(String idUsuario) {
        return detectorRiesgo.obtenerEventosPorUsuario(idUsuario);
    }

    public SimpleLinkedList<EventoAuditoria> obtenerEventosAuditoriaPorNivel(NivelRiesgo nivel) {
        return detectorRiesgo.obtenerEventosPorNivel(nivel);
    }

    /** Crea la alerta, la guarda en el historial del usuario y la muestra en consola. */
    private void generarAlertaParaUsuario(Usuario usuario, String mensaje, String tipoAlerta) {
        contadorAlertas++;

        Alerta alerta = new Alerta(contadorAlertas, LocalDateTime.now(), mensaje, tipoAlerta);
        usuario.getHistorialAlertas().addLast(alerta);

        System.out.println("⚠️ [" + tipoAlerta + "] Usuario: " + usuario.getNombre() + " - " + mensaje);
    }

    // ------------------------------------------------------------------
    // RESPALDO
    // ------------------------------------------------------------------

    /** Guarda un respaldo en texto plano con la información general del sistema. */
    public void guardarRespaldoSistema(String rutaArchivo) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.File(rutaArchivo))) {
            writer.println("=== REPORTE DE ESTADO FINTECH ===");
            writer.println("Fecha de respaldo: " + LocalDateTime.now());
            writer.println("Total usuarios en árbol de puntos: " + arbolUsuariosPorPuntos.obtenerPeso());
            writer.println("Operaciones programadas pendientes: " + operacionesProgramadas.getSize());
            writer.println("Sistema respaldado exitosamente.");
            System.out.println("💾 Respaldo guardado correctamente en: " + rutaArchivo);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("❌ Error al guardar el respaldo: " + e.getMessage());
        }
    }
}