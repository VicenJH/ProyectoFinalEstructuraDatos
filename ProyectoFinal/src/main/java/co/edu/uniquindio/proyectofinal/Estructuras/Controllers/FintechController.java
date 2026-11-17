package co.edu.uniquindio.proyectofinal.Estructuras.Controllers;

import co.edu.uniquindio.proyectofinal.Estructuras.Arbol.ArbolBinario;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.Cola;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.ColaPrioridad;
import co.edu.uniquindio.proyectofinal.Estructuras.Grafos.Grafo;
import co.edu.uniquindio.proyectofinal.Estructuras.Hash.TablaHash;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;
import co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.EstadoFintech;
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
    private Cola<Alerta> notificacionesPendientes;
    private Stack<Transaccion> pilaReversiones;

    private TablaHash<String, Beneficio> beneficiosPorId;
    private SimpleLinkedList<Beneficio> catalogoBeneficios;
    private SimpleLinkedList<CanjeBeneficio> historialCanjes;
    private Grafo<String> grafoTransferencias;

    /** Todas las transacciones registradas en orden cronológico (base de la analítica). */
    private SimpleLinkedList<Transaccion> historialGlobal;

    /** Lista de usuarios registrados, para recorridos de analítica. */
    private SimpleLinkedList<Usuario> listaUsuarios;

    private DetectorRiesgo detectorRiesgo;
    private AnaliticaFintech analitica;

    private int contadorAlertas;
    private long contadorTransacciones;
    private long contadorCanjes;

    public FintechController() {
        this.usuariosPorId = new TablaHash<>(100);
        this.billeterasPorCodigo = new TablaHash<>(500);
        this.arbolUsuariosPorPuntos = new ArbolBinario<>();
        this.operacionesProgramadas = crearColaOperaciones();
        this.operacionesProcesadas = new SimpleLinkedList<>();
        this.notificacionesPendientes = new Cola<>();
        this.pilaReversiones = new Stack<>();
        this.beneficiosPorId = new TablaHash<>(32);
        this.catalogoBeneficios = new SimpleLinkedList<>();
        this.historialCanjes = new SimpleLinkedList<>();
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
        this.contadorCanjes = 0;
        cargarCatalogoInicialBeneficios(beneficiosPorId, catalogoBeneficios);
    }

    private ColaPrioridad<OperacionProgramada> crearColaOperaciones() {
        return new ColaPrioridad<>((a, b) -> {
            int comparacionFecha = a.getFechaHora().compareTo(b.getFechaHora());
            return comparacionFecha != 0 ? comparacionFecha : a.getId().compareTo(b.getId());
        });
    }

    /** Beneficios de ejemplo para que el módulo de canjes pueda probarse desde el inicio. */
    private static void cargarCatalogoInicialBeneficios(
            TablaHash<String, Beneficio> indice, SimpleLinkedList<Beneficio> catalogo) {
        agregarBeneficioInicial(indice, catalogo,
                new Beneficio("BEN-001", "Bono de saldo $5.000",
                        "Cupón de recompensa que abona $5.000 a una de tus billeteras.", 250, 5_000));
        agregarBeneficioInicial(indice, catalogo,
                new Beneficio("BEN-002", "Bono de saldo $10.000",
                        "Cupón de recompensa que abona $10.000 a una de tus billeteras.", 400, 10_000));
        agregarBeneficioInicial(indice, catalogo,
                new Beneficio("BEN-003", "Bono de saldo $20.000",
                        "Cupón de recompensa que abona $20.000 a una de tus billeteras.", 600, 20_000));
    }

    private static void agregarBeneficioInicial(
            TablaHash<String, Beneficio> indice, SimpleLinkedList<Beneficio> catalogo, Beneficio beneficio) {
        indice.insertar(beneficio.getId(), beneficio);
        catalogo.addLast(beneficio);
    }

    // ------------------------------------------------------------------
    // GESTIÓN DE USUARIOS
    // ------------------------------------------------------------------

    public boolean registrarUsuario(String id, String nombre, String correo) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        String idNormalizado = id.trim();
        // Se conserva la identificación histórica: no se puede reutilizar después de eliminar un usuario.
        if (usuariosPorId.buscar(idNormalizado) != null) {
            return false;
        }

        Usuario nuevoUsuario;
        try {
            nuevoUsuario = new Usuario(idNormalizado, nombre, correo);
        } catch (IllegalArgumentException e) {
            return false;
        }

        usuariosPorId.insertar(idNormalizado, nuevoUsuario);
        arbolUsuariosPorPuntos.insertar(nuevoUsuario);
        grafoTransferencias.agregarVertice(idNormalizado);
        listaUsuarios.addLast(nuevoUsuario);
        return true;
    }

    public Usuario buscarUsuario(String id) {
        if (id == null) return null;
        Usuario usuario = usuariosPorId.buscar(id.trim());
        return usuario != null && usuario.isActivo() ? usuario : null;
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
        if (usuario == null) return false;

        // Baja lógica: preserva transacciones, billeteras y referencias para auditoría/reversiones.
        arbolUsuariosPorPuntos.eliminar(usuario);
        usuario.setActivo(false);
        usuario.desactivarBilleteras();
        // listaUsuarios conserva el registro histórico para que la analítica
        // no pierda la actividad de usuarios dados de baja. El árbol de puntos
        // y buscarUsuario() sí representan exclusivamente usuarios activos.

        // Una operación periódica de un usuario eliminado no debe reprogramarse indefinidamente.
        SimpleLinkedList<OperacionProgramada> pendientes = operacionesProgramadas.aListaOrdenada();
        operacionesProgramadas.clear();
        for (OperacionProgramada operacion : pendientes) {
            if (usuario.getId().equals(operacion.getIdUsuario())) {
                operacion.actualizarEstado("CANCELADA_POR_ELIMINACION_DE_USUARIO");
                operacionesProcesadas.addLast(operacion);
            } else {
                operacionesProgramadas.encolar(operacion);
            }
        }
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
            // Guardar siempre el identificador canónico evita que espacios
            // accidentales rompan la relación usuario-billetera.
            nuevaBilletera = new Billetera(codigoBilletera, nombre, tipo, saldoInicial, true, usuario.getId());
        } catch (IllegalArgumentException e) {
            return false;
        }

        try {
            usuario.agregarBilletera(nuevaBilletera);
        } catch (IllegalArgumentException e) {
            return false;
        }
        billeterasPorCodigo.insertar(codigoBilletera, nuevaBilletera);
        return true;
    }

    public Billetera buscarBilletera(int codigo) {
        return billeterasPorCodigo.buscar(codigo);
    }

    /** Modifica el nombre y la categoría de una billetera perteneciente al usuario. */
    public boolean modificarBilletera(String idUsuario, int codigo, String nombre, TipoBilletera tipo) {
        Usuario usuario = buscarUsuario(idUsuario);
        Billetera billetera = billeterasPorCodigo.buscar(codigo);
        if (usuario == null || billetera == null || !billetera.isActivo()
                || !usuario.getId().equals(billetera.getIdUsuario())
                || nombre == null || nombre.trim().isEmpty() || tipo == null) return false;
        billetera.setNombre(nombre);
        billetera.setTipo(tipo);
        return true;
    }

    /** Cambia el estado de una billetera sin borrar su historial ni sus referencias. */
    public boolean cambiarEstadoBilletera(String idUsuario, int codigo, boolean activa) {
        Usuario usuario = buscarUsuario(idUsuario);
        Billetera billetera = billeterasPorCodigo.buscar(codigo);
        if (usuario == null || billetera == null
                || !usuario.getId().equals(billetera.getIdUsuario())) return false;
        billetera.setActivo(activa);
        return true;
    }

    /** Baja lógica de una billetera: conserva saldo e historial para auditoría. */
    public boolean eliminarBilletera(String idUsuario, int codigo) {
        return cambiarEstadoBilletera(idUsuario, codigo, false);
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

        if (usuario == null) return null;
        if (billetera == null || !usuario.getId().equals(billetera.getIdUsuario())) {
            return rechazar(usuario, "La billetera de origen no existe o no pertenece al usuario.");
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

        if (usuario == null) return null;
        if (billetera == null || !usuario.getId().equals(billetera.getIdUsuario())) {
            return rechazar(usuario, "La billetera de origen no existe o no pertenece al usuario.");
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
            return usuario == null ? null : rechazar(usuario, "La billetera de origen o destino no existe.");
        }

        Integer monto = convertirMonto(montoBruto);
        if (monto == null) return rechazar(usuario, "Monto inválido para la transferencia.");
        if (codOrigen == codDestino) return rechazar(usuario, "La billetera de origen y destino no pueden ser la misma.");
        if (!origen.isActivo() || !destino.isActivo()) return rechazar(usuario, "Alguna de las billeteras está inactiva.");

        Usuario propietarioDestino = buscarUsuario(destino.getIdUsuario());
        if (propietarioDestino == null) return rechazar(usuario, "El usuario destino no está activo.");
        if (origen.getSaldo() < monto) return rechazar(usuario, "Saldo insuficiente en la billetera " + codOrigen + ".");
        if (!destino.puedeRecargar(monto)) return rechazar(usuario, "La billetera destino no puede recibir ese monto.");

        DetectorRiesgo.Evaluacion riesgo = detectorRiesgo.evaluar(
                usuario.getId(), monto, true, codOrigen, codDestino, LocalDateTime.now());

        // Las dos modificaciones están protegidas por las validaciones previas.
        origen.retirar(monto);
        destino.recargar(monto);

        int puntos = (monto / 100) * 3 + (programada ? BONO_PUNTOS_PROGRAMADA : 0);
        TipoTransaccion tipo = usuario.getId().equals(propietarioDestino.getId())
                ? TipoTransaccion.TRANSFERENCIA_INTERNA
                : TipoTransaccion.TRANSFERENCIA_EXTERNA;
        return registrar(usuario, origen, destino, monto, tipo, puntos, riesgo);
    }

    /**
     * Registra una operación ya aplicada: historiales, pila de reversiones,
     * grafo, puntos y alerta de saldo bajo.
     */
    private Transaccion registrar(Usuario actor, Billetera origen, Billetera destino,
                                  int monto, TipoTransaccion tipo, int puntos,
                                  DetectorRiesgo.Evaluacion riesgo) {
        // Evita que el acumulado de puntos desborde aunque una cuenta opere durante años.
        puntos = Math.min(puntos, Integer.MAX_VALUE - actor.getPuntosAcumulados());
        contadorTransacciones++;
        LocalDateTime fechaOperacion = LocalDateTime.now();

        Transaccion tx = new Transaccion(
                "TX-" + contadorTransacciones,
                monto,
                String.valueOf(origen.getCodigo()),
                destino == null ? null : String.valueOf(destino.getCodigo()),
                fechaOperacion,
                tipo,
                "EXITOSA",
                puntos,
                riesgo.getNivel()
        );
        historialGlobal.addLast(tx);
        actor.registrarTransaccion(tx);
        origen.registrarTransaccion(tx);

        if (destino != null) {
            destino.registrarTransaccion(tx);

            Usuario duenoDestino = usuariosPorId.buscar(destino.getIdUsuario());

            if (duenoDestino != null && !duenoDestino.getId().equals(actor.getId())) {
                duenoDestino.registrarTransaccion(tx);
                if (tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA) {
                    registrarTransferenciaEnGrafo(actor.getId(), duenoDestino.getId(), monto);
                }
            }
        }

        pilaReversiones.push(tx);

        boolean movimientoSaliente = tipo == TipoTransaccion.RETIRO
                || tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA;
        detectorRiesgo.registrarOperacion(
                actor.getId(), monto, movimientoSaliente, origen.getCodigo(),
                destino == null ? null : destino.getCodigo(), fechaOperacion);

        if (riesgo.getNivel() != NivelRiesgo.BAJO) {
            detectorRiesgo.registrarEvento(actor.getId(), tx.getId(), riesgo, tx.getFechaHora());
            generarAlertaParaUsuario(actor,
                    "Riesgo " + riesgo.getNivel() + " en " + tx.getId() + ": " + riesgo.getMotivo(),
                    "SEGURIDAD");
        }

        otorgarPuntos(actor, puntos);

        if (movimientoSaliente && origen.getSaldo() < UMBRAL_SALDO_BAJO) {
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
        if (puntos <= 0) return;
        String nivelAnterior = usuario.getNivel();
        boolean activo = usuario.isActivo();
        if (activo) arbolUsuariosPorPuntos.eliminar(usuario);
        try {
            usuario.agregarPuntos(puntos);
        } finally {
            if (activo) arbolUsuariosPorPuntos.insertar(usuario);
        }
        if (!nivelAnterior.equals(usuario.getNivel())) {
            generarAlertaParaUsuario(usuario, "¡Ascendiste al nivel " + usuario.getNivel() + "!", "ASCENSO_NIVEL");
        }
    }

    /** Descuenta puntos y mantiene sincronizado el árbol únicamente para usuarios activos. */
    private boolean quitarPuntos(Usuario usuario, int puntos) {
        if (usuario == null || puntos < 0 || usuario.getPuntosAcumulados() < puntos) return false;
        if (puntos == 0) return true;
        boolean activo = usuario.isActivo();
        if (activo) arbolUsuariosPorPuntos.eliminar(usuario);
        try {
            usuario.descontarPuntos(puntos);
        } finally {
            if (activo) arbolUsuariosPorPuntos.insertar(usuario);
        }
        return true;
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
                || ((tx.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tx.getTipo() == TipoTransaccion.TRANSFERENCIA_EXTERNA) && secundaria == null)) {
            pilaReversiones.pop();
            return false;
        }

        Usuario actor = usuariosPorId.buscar(principal.getIdUsuario());
        if (actor != null && actor.getPuntosAcumulados() < tx.getPuntosGenerados()) {
            // Si los puntos ya se gastaron en un canje, se conserva la operación sin revertir.
            return false;
        }

        switch (tx.getTipo()) {
            case TRANSFERENCIA_INTERNA:
            case TRANSFERENCIA_EXTERNA:
                if (secundaria.getSaldo() < monto || !principal.puedeRecargar(monto)) {
                    return false;
                }
                secundaria.retirar(monto);
                principal.recargar(monto);
                if (tx.getTipo() == TipoTransaccion.TRANSFERENCIA_EXTERNA) {
                    revertirPesoEnGrafo(principal.getIdUsuario(), secundaria.getIdUsuario(), monto);
                }
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

        if (actor != null) quitarPuntos(actor, tx.getPuntosGenerados());

        tx.marcarRevertida();
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
        if (id == null || id.trim().isEmpty() || existeIdOperacionProgramada(id.trim())
                || origen == null || !origen.isActivo()
                || tipo == null || tipo == TipoTransaccion.BONIFICACION
                || fechaHora == null || frecuencia == null || convertirMonto(valor) == null) {
            return false;
        }
        Usuario usuarioOrigen = buscarUsuario(origen.getIdUsuario());
        if (usuarioOrigen == null) return false;

        boolean esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA_INTERNA
                || tipo == TipoTransaccion.TRANSFERENCIA_EXTERNA;
        if (esTransferencia) {
            Billetera destino = billeterasPorCodigo.buscar(codDestino);
            if (destino == null || !destino.isActivo() || codOrigen == codDestino
                    || buscarUsuario(destino.getIdUsuario()) == null) return false;
        }

        try {
            operacionesProgramadas.encolar(new OperacionProgramada(
                    id.trim(), valor,
                    String.valueOf(codOrigen), esTransferencia ? String.valueOf(codDestino) : null,
                    fechaHora, tipo, frecuencia, estado,
                    origen.getIdUsuario()
            ));
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    private boolean existeIdOperacionProgramada(String id) {
        for (OperacionProgramada pendiente : operacionesProgramadas.aListaOrdenada()) {
            if (pendiente.getId().equals(id)) return true;
        }
        for (OperacionProgramada procesada : operacionesProcesadas) {
            if (procesada.getId().equals(id)) return true;
        }
        return false;
    }

    /**
     * Genera una sola alerta por cada ocurrencia de una operación programada
     * cuando entra en la ventana de los próximos diez minutos. Si ya venció,
     * también genera la alerta antes de procesarla.
     *
     * @return cantidad de alertas nuevas.
     */
    public int notificarOperacionesProximas(LocalDateTime ahora) {
        if (ahora == null) throw new IllegalArgumentException("La fecha de consulta no puede ser null.");
        int notificadas = 0;
        LocalDateTime limite = ahora.plusMinutes(10);
        for (OperacionProgramada op : operacionesProgramadas.aListaOrdenada()) {
            if (!op.isNotificacionProximaEnviada() && !op.getFechaHora().isAfter(limite)) {
                Usuario usuario = buscarUsuario(op.getIdUsuario());
                if (usuario != null) {
                    generarAlertaParaUsuario(usuario,
                            "La operación programada " + op.getId() + " está próxima a ejecutarse ("
                                    + op.getFechaHora() + ").",
                            "OPERACION_PROGRAMADA_PROXIMA");
                    op.setNotificacionProximaEnviada(true);
                    notificadas++;
                }
            }
        }
        return notificadas;
    }

    /**
     * Ejecuta todas las operaciones cuya fecha ya llegó, en orden de fecha.
     * Las periódicas (SEMANAL, MENSUAL) se reprograman a la siguiente fecha
     * posterior a este momento; las UNICA pasan al registro de procesadas.
     *
     * @return cantidad de operaciones ejecutadas con éxito.
     */
    public int procesarOperacionesProgramadas() {
        return procesarOperacionesProgramadas(LocalDateTime.now());
    }

    /** Sobrecarga determinista para pruebas y simulaciones con una fecha controlada. */
    public int procesarOperacionesProgramadas(LocalDateTime ahora) {
        if (ahora == null) throw new IllegalArgumentException("La fecha de procesamiento no puede ser null.");
        int ejecutadas = 0;

        while (!operacionesProgramadas.isEmpty()
                && !operacionesProgramadas.consultarPrimero().getFechaHora().isAfter(ahora)) {

            OperacionProgramada op = operacionesProgramadas.desencolar();
            boolean propietarioActivo = buscarUsuario(op.getIdUsuario()) != null;
            boolean billeteraOrigenActiva = false;
            boolean billeteraDestinoActiva = true;
            try {
                Billetera origenProgramado = billeterasPorCodigo.buscar(Integer.parseInt(op.getBilleteraOrigen()));
                billeteraOrigenActiva = origenProgramado != null && origenProgramado.isActivo();
                if (op.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA
                        || op.getTipo() == TipoTransaccion.TRANSFERENCIA_EXTERNA) {
                    Billetera destinoProgramado = billeterasPorCodigo.buscar(Integer.parseInt(op.getBilleteraDestino()));
                    billeteraDestinoActiva = destinoProgramado != null && destinoProgramado.isActivo()
                            && buscarUsuario(destinoProgramado.getIdUsuario()) != null;
                }
            } catch (NumberFormatException ignored) {
                // Una referencia dañada se archivará como cancelada, sin bloquear la cola.
            }

            if (!propietarioActivo || !billeteraOrigenActiva || !billeteraDestinoActiva) {
                op.actualizarEstado("CANCELADA_POR_REFERENCIA_INACTIVA");
                operacionesProcesadas.addLast(op);
                continue;
            }

            boolean exito = ejecutarOperacionProgramada(op) != null;
            if (exito) ejecutadas++;

            if (op.getFrecuencia() == Frecuencia.UNICA) {
                op.actualizarEstado(exito ? "EJECUTADA" : "FALLIDA");
                operacionesProcesadas.addLast(op);
            } else {
                op.reprogramar(op.calcularSiguienteFechaPosteriorA(ahora));
                op.setNotificacionProximaEnviada(false);
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
            case TRANSFERENCIA_INTERNA:
            case TRANSFERENCIA_EXTERNA:
                return ejecutarTransferencia(
                        codOrigen,
                        Integer.parseInt(op.getBilleteraDestino()),
                        op.getValor(),
                        idUsuario,
                        true
                );
            default:
                return null;
        }
    }

    /** @return operaciones pendientes ordenadas por fecha de ejecución. */
    public SimpleLinkedList<OperacionProgramada> obtenerOperacionesPendientes() {
        return operacionesProgramadas.aListaOrdenada();
    }

    /** @return operaciones únicas ya procesadas (ejecutadas o fallidas). */
    public SimpleLinkedList<OperacionProgramada> obtenerOperacionesProcesadas() {
        return copiarLista(operacionesProcesadas);
    }

    // ------------------------------------------------------------------
    // BENEFICIOS Y CANJES DE PUNTOS
    // ------------------------------------------------------------------

    /** Registra un beneficio nuevo. Los identificadores se guardan en mayúscula. */
    public boolean registrarBeneficio(String id, String nombre, String descripcion, int costoPuntos) {
        return registrarBeneficio(id, nombre, descripcion, costoPuntos, 0);
    }

    /** Registra un cupón que, al aplicarse, acredita un bono fijo en la billetera. */
    public boolean registrarBeneficio(String id, String nombre, String descripcion,
                                      int costoPuntos, int valorBono) {
        if (id == null || id.trim().isEmpty() || valorBono <= 0) return false;
        String idNormalizado = id.trim().toUpperCase();
        if (beneficiosPorId.buscar(idNormalizado) != null) return false;
        try {
            Beneficio beneficio = new Beneficio(idNormalizado, nombre, descripcion, costoPuntos, valorBono);
            beneficiosPorId.insertar(idNormalizado, beneficio);
            catalogoBeneficios.addLast(beneficio);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /** Busca por identificador sin distinguir mayúsculas ni espacios exteriores. */
    private Beneficio buscarBeneficioInterno(String id) {
        return id == null ? null : beneficiosPorId.buscar(id.trim().toUpperCase());
    }

    /** Devuelve una copia del beneficio para impedir modificaciones externas al catálogo. */
    public Beneficio buscarBeneficio(String id) {
        Beneficio beneficio = buscarBeneficioInterno(id);
        return beneficio == null ? null : beneficio.copia();
    }

    /** Modifica la información de catálogo; los canjes anteriores conservan su fotografía histórica. */
    public boolean modificarBeneficio(String id, String nombre, String descripcion, int costoPuntos) {
        Beneficio beneficio = buscarBeneficioInterno(id);
        if (beneficio == null) return false;
        try {
            beneficio.actualizarDatos(nombre, descripcion, costoPuntos);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /** Actualiza el catálogo y el importe de la bonificación para nuevos canjes. */
    public boolean modificarBeneficio(String id, String nombre, String descripcion,
                                      int costoPuntos, int valorBono) {
        Beneficio beneficio = buscarBeneficioInterno(id);
        if (beneficio == null || valorBono <= 0) return false;
        try {
            beneficio.actualizarDatos(nombre, descripcion, costoPuntos);
            beneficio.setValorBono(valorBono);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /** Devuelve una copia del catálogo completo, incluidos los beneficios inactivos. */
    public SimpleLinkedList<Beneficio> obtenerCatalogoBeneficios() {
        SimpleLinkedList<Beneficio> copia = new SimpleLinkedList<>();
        for (Beneficio beneficio : catalogoBeneficios) copia.addLast(beneficio.copia());
        return copia;
    }

    /** Devuelve únicamente los beneficios que pueden canjearse actualmente. */
    public SimpleLinkedList<Beneficio> obtenerBeneficiosDisponibles() {
        SimpleLinkedList<Beneficio> resultado = new SimpleLinkedList<>();
        for (Beneficio beneficio : catalogoBeneficios) {
            if (beneficio.isActivo()) resultado.addLast(beneficio.copia());
        }
        return resultado;
    }

    /** Desactiva un beneficio para nuevos canjes, sin invalidar los ya entregados. */
    public boolean desactivarBeneficio(String id) {
        Beneficio beneficio = buscarBeneficioInterno(id);
        if (beneficio == null || !beneficio.isActivo()) return false;
        beneficio.setActivo(false);
        return true;
    }

    /** Reactiva un beneficio previamente desactivado. */
    public boolean activarBeneficio(String id) {
        Beneficio beneficio = buscarBeneficioInterno(id);
        if (beneficio == null || beneficio.isActivo()) return false;
        beneficio.setActivo(true);
        return true;
    }

    /**
     * Canjea un beneficio. Primero valida y construye el comprobante; solo después
     * descuenta los puntos y registra el canje, evitando cobros sin comprobante.
     * Retorna null cuando el usuario, beneficio o saldo de puntos no son válidos.
     */
    public CanjeBeneficio canjearBeneficio(String idUsuario, String idBeneficio) {
        Usuario usuario = buscarUsuario(idUsuario);
        Beneficio beneficio = buscarBeneficioInterno(idBeneficio);
        if (usuario == null || beneficio == null || !beneficio.isActivo()) return null;
        if (usuario.getPuntosAcumulados() < beneficio.getCostoPuntos()) return null;

        long siguiente = contadorCanjes + 1;
        CanjeBeneficio canje;
        try {
            canje = new CanjeBeneficio(
                    String.format("CANJE-%06d", siguiente), usuario.getId(), beneficio,
                    LocalDateTime.now(), beneficio.getCostoPuntos());
        } catch (IllegalArgumentException ex) {
            return null;
        }

        if (!quitarPuntos(usuario, beneficio.getCostoPuntos())) return null;

        contadorCanjes = siguiente;
        usuario.registrarCanje(canje);
        historialCanjes.addLast(canje);
        generarAlertaParaUsuario(usuario,
                "Canje realizado: " + beneficio.getNombre() + ". Se descontaron "
                        + beneficio.getCostoPuntos() + " puntos. Código: " + canje.getCodigoRedencion(),
                "CANJE_BENEFICIO");
        return canje.copia();
    }

    /** Busca un canje por su identificador en el historial global. */
    private CanjeBeneficio buscarCanjeBeneficioInterno(String idCanje) {
        if (idCanje == null || idCanje.trim().isEmpty()) return null;
        String idNormalizado = idCanje.trim();
        for (CanjeBeneficio canje : historialCanjes) {
            if (canje.getId().equalsIgnoreCase(idNormalizado)
                    || canje.getCodigoRedencion().equalsIgnoreCase(idNormalizado)) return canje;
        }
        return null;
    }

    /** Devuelve una copia del comprobante para proteger el estado interno del canje. */
    public CanjeBeneficio buscarCanjeBeneficio(String idCanje) {
        CanjeBeneficio canje = buscarCanjeBeneficioInterno(idCanje);
        return canje == null ? null : canje.copia();
    }

    /** Marca como utilizado un canje, comprobando que pertenezca al usuario indicado. */
    public boolean utilizarCanjeBeneficio(String idUsuario, String idCanje) {
        Usuario usuario = buscarUsuario(idUsuario);
        CanjeBeneficio canje = buscarCanjeBeneficioInterno(idCanje);
        if (usuario == null || canje == null || !usuario.getId().equals(canje.getIdUsuario())) return false;
        boolean actualizado = canje.marcarComoUtilizado();
        if (actualizado) {
            generarAlertaParaUsuario(usuario,
                    "Beneficio utilizado: " + canje.getNombreBeneficio()
                            + " (" + canje.getCodigoRedencion() + ").",
                    "BENEFICIO_UTILIZADO");
        }
        return actualizado;
    }

    /**
     * Aplica un cupón disponible y acredita su valor como bonificación de saldo.
     * El cupón pertenece al usuario, solo puede consumirse una vez y se aplica
     * a una billetera activa del mismo usuario. Las bonificaciones no generan
     * puntos adicionales ni comisiones, evitando ciclos de recompensas.
     */
    public Transaccion aplicarCupon(String idUsuario, int codigoBilletera, String codigoRedencion) {
        Usuario usuario = buscarUsuario(idUsuario);
        Billetera billetera = billeterasPorCodigo.buscar(codigoBilletera);
        CanjeBeneficio canje = buscarCanjeBeneficioInterno(codigoRedencion);
        if (usuario == null || billetera == null || canje == null
                || !usuario.getId().equals(billetera.getIdUsuario())
                || !billetera.isActivo() || !usuario.getId().equals(canje.getIdUsuario())
                || !canje.isDisponible() || canje.getValorBono() <= 0
                || !billetera.puedeRecargar(canje.getValorBono())) {
            return null;
        }

        billetera.recargar(canje.getValorBono());
        canje.marcarComoUtilizado();
        contadorTransacciones++;
        Transaccion transaccion = new Transaccion(
                "TX-" + contadorTransacciones,
                canje.getValorBono(),
                String.valueOf(codigoBilletera),
                null,
                LocalDateTime.now(),
                TipoTransaccion.BONIFICACION,
                "EXITOSA",
                0,
                NivelRiesgo.BAJO
        );
        historialGlobal.addLast(transaccion);
        usuario.registrarTransaccion(transaccion);
        billetera.registrarTransaccion(transaccion);
        generarAlertaParaUsuario(usuario,
                "Cupón " + canje.getCodigoRedencion() + " aplicado. Se abonaron $"
                        + canje.getValorBono() + " a la billetera " + codigoBilletera + ".",
                "CUPON_APLICADO");
        return transaccion;
    }

    /** Historial global de canjes, en orden de realización. */
    public SimpleLinkedList<CanjeBeneficio> obtenerHistorialCanjes() {
        SimpleLinkedList<CanjeBeneficio> copia = new SimpleLinkedList<>();
        for (CanjeBeneficio canje : historialCanjes) copia.addLast(canje.copia());
        return copia;
    }

    /** Historial de un usuario; también permite consultar usuarios dados de baja. */
    public SimpleLinkedList<CanjeBeneficio> obtenerHistorialCanjesUsuario(String idUsuario) {
        SimpleLinkedList<CanjeBeneficio> resultado = new SimpleLinkedList<>();
        if (idUsuario == null) return resultado;
        Usuario usuario = usuariosPorId.buscar(idUsuario.trim());
        if (usuario == null) return resultado;
        for (CanjeBeneficio canje : usuario.getHistorialCanjes()) resultado.addLast(canje);
        return resultado;
    }

    /** Canjes que el usuario todavía puede utilizar. */
    public SimpleLinkedList<CanjeBeneficio> obtenerCanjesDisponiblesUsuario(String idUsuario) {
        SimpleLinkedList<CanjeBeneficio> resultado = new SimpleLinkedList<>();
        for (CanjeBeneficio canje : obtenerHistorialCanjesUsuario(idUsuario)) {
            if (canje.isDisponible()) resultado.addLast(canje);
        }
        return resultado;
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
    private void registrarTransferenciaEnGrafo(String idUsuarioOrigen, String idUsuarioDestino, double monto) {
        if (idUsuarioOrigen == null || idUsuarioOrigen.trim().isEmpty()
                || idUsuarioDestino == null || idUsuarioDestino.trim().isEmpty()
                || !Double.isFinite(monto) || monto <= 0 || monto != Math.rint(monto)) {
            throw new IllegalArgumentException("Los datos de la transferencia para el grafo no son válidos.");
        }
        // Los vértices se crean al registrar usuarios: no se deben inventar
        // nodos que no correspondan a cuentas registradas.
        if (grafoTransferencias.buscarVertice(idUsuarioOrigen) == null
                || grafoTransferencias.buscarVertice(idUsuarioDestino) == null) {
            throw new IllegalStateException("No existen ambos usuarios en el grafo de transferencias.");
        }
        grafoTransferencias.agregarArista(idUsuarioOrigen, idUsuarioDestino, monto);
    }

    /** Resta de la arista el monto de una transferencia revertida (sin bajar de 0). */
    private void revertirPesoEnGrafo(String idOrigen, String idDestino, int monto) {
        if (idOrigen == null || idDestino == null || idOrigen.equals(idDestino)) {
            return;
        }

        Double peso = grafoTransferencias.obtenerPesoArista(idOrigen, idDestino);
        if (peso == null) return;
        double nuevoPeso = peso - monto;
        if (nuevoPeso <= 0.0) {
            grafoTransferencias.eliminarArista(idOrigen, idDestino);
        } else {
            grafoTransferencias.actualizarPesoArista(idOrigen, idDestino, nuevoPeso);
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

        for (Grafo.Vertice<String> v : vertices) {
            int conexiones = grafoTransferencias.contarAristasActivas(v);

            if (conexiones > maxConexiones) {
                maxConexiones = conexiones;
                usuarioMax = v.getDato();
            }
        }

        return maxConexiones <= 0 ? null : usuarioMax;
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

            for (Grafo.Arista<String> arista : actual.getAdyacentes()) {
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
        if (usuario == null || !usuario.isActivo() || !Double.isFinite(montoActual)
                || montoActual <= 0 || montoActual != Math.rint(montoActual)
                || montoActual > Integer.MAX_VALUE) return false;
        int monto = (int) montoActual;

        DetectorRiesgo.Evaluacion evaluacion = detectorRiesgo.evaluar(
                usuario.getId(), monto, true, -1, null, LocalDateTime.now());

        if (evaluacion.getNivel() == NivelRiesgo.BAJO) {
            return false;
        }

        detectorRiesgo.registrarEvento(usuario.getId(), idTransaccion == null ? "EVALUACION-MANUAL" : idTransaccion,
                evaluacion, LocalDateTime.now());
        generarAlertaParaUsuario(usuario, evaluacion.getMotivo(), "SEGURIDAD");
        return true;
    }

    // ------------------------------------------------------------------
    // CONSULTAS PARA LA INTERFAZ
    // ------------------------------------------------------------------

    /** Usuarios registrados, incluidos los dados de baja para administración. */
    public SimpleLinkedList<Usuario> obtenerUsuarios() {
        return copiarLista(listaUsuarios);
    }

    /** Billeteras del sistema, incluidas las inactivas. */
    public SimpleLinkedList<Billetera> obtenerBilleteras() {
        SimpleLinkedList<Billetera> resultado = new SimpleLinkedList<>();
        for (Usuario usuario : listaUsuarios) {
            for (Billetera billetera : usuario.getBilleteras()) resultado.addLast(billetera);
        }
        return resultado;
    }

    /** Historial de alertas de todos los usuarios, sin exponer la cola interna. */
    public SimpleLinkedList<Alerta> obtenerHistorialAlertasGlobal() {
        SimpleLinkedList<Alerta> resultado = new SimpleLinkedList<>();
        for (Usuario usuario : listaUsuarios) {
            for (Alerta alerta : usuario.getHistorialAlertas()) resultado.addLast(alerta.copia());
        }
        return resultado;
    }

    /** @return servicio con los informes y consultas de analítica. */
    public AnaliticaFintech getAnalitica() {
        return analitica;
    }

    /** @return todas las transacciones registradas, en orden cronológico. */
    public SimpleLinkedList<Transaccion> obtenerHistorialGlobal() {
        return copiarLista(historialGlobal);
    }

    /** @return historial de auditoría de riesgo (eventos MEDIO y ALTO). */
    public SimpleLinkedList<EventoAuditoria> obtenerHistorialAuditoria() {
        return copiarLista(detectorRiesgo.obtenerHistorialAuditoria());
    }

    public SimpleLinkedList<EventoAuditoria> obtenerEventosAuditoriaDeUsuario(String idUsuario) {
        return detectorRiesgo.obtenerEventosPorUsuario(idUsuario);
    }

    public SimpleLinkedList<EventoAuditoria> obtenerEventosAuditoriaPorNivel(NivelRiesgo nivel) {
        return detectorRiesgo.obtenerEventosPorNivel(nivel);
    }

    /** Devuelve una copia de la cola de notificaciones pendientes en orden FIFO. */
    public Cola<Alerta> obtenerNotificacionesPendientes() {
        Cola<Alerta> copia = new Cola<>();
        for (Alerta alerta : notificacionesPendientes) copia.encolar(alerta.copia());
        return copia;
    }

    public int getCantidadNotificacionesPendientes() {
        return notificacionesPendientes.getSize();
    }

    /** Despacha la notificación más antigua y la marca como atendida. */
    public Alerta despacharSiguienteNotificacion() {
        if (notificacionesPendientes.isEmpty()) return null;
        Alerta alerta = notificacionesPendientes.desencolar();
        alerta.marcarAtendida();
        return alerta;
    }

    public int getCantidadOperacionesProgramadasPendientes() {
        return operacionesProgramadas.getSize();
    }

    /** Crea la alerta, la guarda en el historial del usuario y la muestra en consola. */
    private void generarAlertaParaUsuario(Usuario usuario, String mensaje, String tipoAlerta) {
        contadorAlertas++;

        Alerta alerta = new Alerta(contadorAlertas, LocalDateTime.now(), mensaje, tipoAlerta);
        usuario.registrarAlerta(alerta);
        notificacionesPendientes.encolar(alerta);

        System.out.println("⚠️ [" + tipoAlerta + "] Usuario: " + usuario.getNombre() + " - " + mensaje);
    }

    private static <T> SimpleLinkedList<T> copiarLista(Iterable<T> origen) {
        SimpleLinkedList<T> copia = new SimpleLinkedList<>();
        for (T elemento : origen) copia.addLast(elemento);
        return copia;
    }

    // ------------------------------------------------------------------
    // ESTADO PERSISTIBLE: exportación y restauración de la aplicación
    // ------------------------------------------------------------------

    /** Crea una instantánea de datos simples para guardar/restaurar el sistema. */
    public EstadoFintech crearEstadoPersistible() {
        java.util.ArrayList<EstadoFintech.UsuarioData> usuarios = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.BilleteraData> billeteras = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.TransaccionData> transacciones = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.AlertaData> alertas = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.BeneficioData> beneficios = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.CanjeData> canjes = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.OperacionData> pendientes = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.OperacionData> procesadas = new java.util.ArrayList<>();
        java.util.ArrayList<EstadoFintech.EventoData> auditoria = new java.util.ArrayList<>();

        for (Usuario usuario : listaUsuarios) {
            usuarios.add(new EstadoFintech.UsuarioData(usuario.getId(), usuario.getNombre(),
                    usuario.getCorreo(), usuario.getPuntosAcumulados(), usuario.isActivo()));
            for (Billetera billetera : usuario.getBilleteras()) {
                billeteras.add(new EstadoFintech.BilleteraData(billetera.getCodigo(), usuario.getId(),
                        billetera.getNombre(), billetera.getTipo(), billetera.getSaldo(), billetera.isActivo()));
            }
            for (Alerta alerta : usuario.getHistorialAlertas()) {
                alertas.add(new EstadoFintech.AlertaData(usuario.getId(), alerta.getId(), alerta.getFecha(),
                        alerta.getMensaje(), alerta.getTipoAlerta(), alerta.isAtendida()));
            }
        }
        for (Transaccion tx : historialGlobal) {
            transacciones.add(new EstadoFintech.TransaccionData(tx.getId(), tx.getValor(),
                    tx.getBilleteraOrigen(), tx.getBilleteraDestino(), tx.getFechaHora(), tx.getTipo(),
                    tx.getEstado(), tx.getPuntosGenerados(), tx.getNivelRiesgo()));
        }
        for (Beneficio beneficio : catalogoBeneficios) {
            beneficios.add(new EstadoFintech.BeneficioData(beneficio.getId(), beneficio.getNombre(),
                    beneficio.getDescripcion(), beneficio.getCostoPuntos(), beneficio.getValorBono(),
                    beneficio.isActivo()));
        }
        for (CanjeBeneficio canje : historialCanjes) {
            canjes.add(new EstadoFintech.CanjeData(canje.getId(), canje.getCodigoRedencion(),
                    canje.getIdUsuario(), canje.getIdBeneficio(), canje.getNombreBeneficio(),
                    canje.getDescripcionBeneficio(), canje.getFechaHora(), canje.getPuntosGastados(),
                    canje.getValorBono(), canje.getEstado(), canje.getFechaUso()));
        }
        for (OperacionProgramada op : operacionesProgramadas.aListaOrdenada()) {
            pendientes.add(datosOperacion(op));
        }
        for (OperacionProgramada op : operacionesProcesadas) {
            procesadas.add(datosOperacion(op));
        }
        for (EventoAuditoria evento : detectorRiesgo.obtenerHistorialAuditoria()) {
            auditoria.add(new EstadoFintech.EventoData(evento.getId(), evento.getFecha(),
                    evento.getIdUsuario(), evento.getIdTransaccion(), evento.getNivel(), evento.getMotivo()));
        }
        return new EstadoFintech(1, LocalDateTime.now(), usuarios, billeteras, transacciones,
                alertas, beneficios, canjes, pendientes, procesadas, auditoria);
    }

    private EstadoFintech.OperacionData datosOperacion(OperacionProgramada op) {
        return new EstadoFintech.OperacionData(op.getId(), op.getValor(), op.getBilleteraOrigen(),
                op.getBilleteraDestino(), op.getFechaHora(), op.getTipo(), op.getFrecuencia(),
                op.getEstado(), op.getIdUsuario(), op.isNotificacionProximaEnviada());
    }

    /**
     * Reconstruye todas las estructuras de datos a partir de una instantánea.
     * Los saldos y puntos se restauran directamente: nunca se vuelven a ejecutar
     * las operaciones históricas ni se duplican recompensas.
     */
    public void restaurarEstadoPersistible(EstadoFintech estado) {
        if (estado == null || estado.version() != 1) {
            throw new IllegalArgumentException("El respaldo Fintech no es compatible.");
        }

        usuariosPorId = new TablaHash<>(Math.max(100, estado.usuarios().size() * 2));
        billeterasPorCodigo = new TablaHash<>(Math.max(500, estado.billeteras().size() * 2));
        arbolUsuariosPorPuntos = new ArbolBinario<>();
        operacionesProgramadas = crearColaOperaciones();
        operacionesProcesadas = new SimpleLinkedList<>();
        notificacionesPendientes = new Cola<>();
        pilaReversiones = new Stack<>();
        beneficiosPorId = new TablaHash<>(Math.max(32, estado.beneficios().size() * 2));
        catalogoBeneficios = new SimpleLinkedList<>();
        historialCanjes = new SimpleLinkedList<>();
        grafoTransferencias = new Grafo<>();
        historialGlobal = new SimpleLinkedList<>();
        listaUsuarios = new SimpleLinkedList<>();
        detectorRiesgo = new DetectorRiesgo();
        analitica = new AnaliticaFintech(historialGlobal, listaUsuarios, billeterasPorCodigo,
                arbolUsuariosPorPuntos, grafoTransferencias);
        contadorAlertas = 0;
        contadorTransacciones = 0;
        contadorCanjes = 0;

        for (EstadoFintech.UsuarioData dato : estado.usuarios()) {
            Usuario usuario = new Usuario(dato.id(), dato.nombre(), dato.correo());
            if (dato.puntos() > 0) usuario.agregarPuntos(dato.puntos());
            usuariosPorId.insertar(usuario.getId(), usuario);
            listaUsuarios.addLast(usuario);
            grafoTransferencias.agregarVertice(usuario.getId());
            if (dato.activo()) arbolUsuariosPorPuntos.insertar(usuario);
            else usuario.setActivo(false);
        }

        for (EstadoFintech.BilleteraData dato : estado.billeteras()) {
            Usuario propietario = usuariosPorId.buscar(dato.idUsuario());
            if (propietario == null) continue;
            Billetera billetera = new Billetera(dato.codigo(), dato.nombre(), dato.tipo(),
                    dato.saldo(), dato.activa(), dato.idUsuario());
            propietario.agregarBilletera(billetera);
            billeterasPorCodigo.insertar(billetera.getCodigo(), billetera);
        }

        for (EstadoFintech.BeneficioData dato : estado.beneficios()) {
            Beneficio beneficio = new Beneficio(dato.id(), dato.nombre(), dato.descripcion(),
                    dato.costoPuntos(), dato.valorBono());
            beneficio.setActivo(dato.activo());
            beneficiosPorId.insertar(beneficio.getId(), beneficio);
            catalogoBeneficios.addLast(beneficio);
        }

        java.util.HashMap<String, Transaccion> transaccionesPorId = new java.util.HashMap<>();
        for (EstadoFintech.TransaccionData dato : estado.transacciones()) {
            Transaccion tx = new Transaccion(dato.id(), dato.valor(), dato.origen(), dato.destino(),
                    dato.fecha(), dato.tipo(), dato.estado(), dato.puntos(), dato.riesgo());
            historialGlobal.addLast(tx);
            transaccionesPorId.put(tx.getId(), tx);
            int codigoOrigen = Integer.parseInt(tx.getBilleteraOrigen());
            Billetera origen = billeterasPorCodigo.buscar(codigoOrigen);
            Usuario propietarioOrigen = origen == null ? null : usuariosPorId.buscar(origen.getIdUsuario());
            if (origen != null) origen.registrarTransaccion(tx);
            if (propietarioOrigen != null) propietarioOrigen.registrarTransaccion(tx);

            if (tx.getBilleteraDestino() != null) {
                Billetera destino = billeterasPorCodigo.buscar(Integer.parseInt(tx.getBilleteraDestino()));
                if (destino != null) {
                    destino.registrarTransaccion(tx);
                    Usuario propietarioDestino = usuariosPorId.buscar(destino.getIdUsuario());
                    if (propietarioDestino != null && propietarioOrigen != null
                            && !propietarioDestino.getId().equals(propietarioOrigen.getId())) {
                        propietarioDestino.registrarTransaccion(tx);
                    }
                    if (tx.getTipo() == TipoTransaccion.TRANSFERENCIA_EXTERNA
                            && propietarioOrigen != null && propietarioDestino != null) {
                        registrarTransferenciaEnGrafo(propietarioOrigen.getId(), propietarioDestino.getId(),
                                tx.getValor());
                    }
                }
            }

            if (!TipoTransaccion.BONIFICACION.equals(tx.getTipo()) && propietarioOrigen != null) {
                boolean saliente = tx.getTipo() == TipoTransaccion.RETIRO
                        || tx.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA
                        || tx.getTipo() == TipoTransaccion.TRANSFERENCIA_EXTERNA;
                Integer destinoCodigo = tx.getBilleteraDestino() == null ? null
                        : Integer.valueOf(tx.getBilleteraDestino());
                detectorRiesgo.registrarOperacion(propietarioOrigen.getId(), (int) tx.getValor(), saliente,
                        codigoOrigen, destinoCodigo, tx.getFechaHora());
            }
            if ("EXITOSA".equals(tx.getEstado()) && tx.getTipo() != TipoTransaccion.BONIFICACION) {
                pilaReversiones.push(tx);
            }
            contadorTransacciones = Math.max(contadorTransacciones, secuenciaNumerica(tx.getId()));
        }

        for (EstadoFintech.AlertaData dato : estado.alertas()) {
            Usuario usuario = usuariosPorId.buscar(dato.idUsuario());
            if (usuario == null) continue;
            Alerta alerta = new Alerta(dato.id(), dato.fecha(), dato.mensaje(), dato.tipo());
            if (dato.atendida()) alerta.marcarAtendida();
            usuario.registrarAlerta(alerta);
            if (!dato.atendida()) notificacionesPendientes.encolar(alerta);
            contadorAlertas = Math.max(contadorAlertas, dato.id());
        }

        for (EstadoFintech.CanjeData dato : estado.canjes()) {
            CanjeBeneficio canje = CanjeBeneficio.restaurar(dato.id(), dato.codigo(), dato.idUsuario(),
                    dato.idBeneficio(), dato.nombre(), dato.descripcion(), dato.fecha(), dato.puntosGastados(),
                    dato.valorBono(), dato.estado(), dato.fechaUso());
            historialCanjes.addLast(canje);
            Usuario usuario = usuariosPorId.buscar(dato.idUsuario());
            if (usuario != null) usuario.registrarCanje(canje);
            contadorCanjes = Math.max(contadorCanjes, secuenciaNumerica(dato.id()));
        }

        for (EstadoFintech.OperacionData dato : estado.pendientes()) {
            OperacionProgramada op = restaurarOperacion(dato);
            operacionesProgramadas.encolar(op);
        }
        for (EstadoFintech.OperacionData dato : estado.procesadas()) {
            operacionesProcesadas.addLast(restaurarOperacion(dato));
        }

        for (EstadoFintech.EventoData dato : estado.auditoria()) {
            detectorRiesgo.restaurarEvento(new EventoAuditoria(dato.id(), dato.fecha(), dato.idUsuario(),
                    dato.idTransaccion(), dato.nivel(), dato.motivo()));
        }
    }

    private OperacionProgramada restaurarOperacion(EstadoFintech.OperacionData dato) {
        OperacionProgramada op = new OperacionProgramada(dato.id(), dato.valor(), dato.origen(),
                dato.destino(), dato.fecha(), dato.tipo(), dato.frecuencia(), dato.estado(), dato.idUsuario());
        op.setNotificacionProximaEnviada(dato.notificacionEnviada());
        return op;
    }

    private static long secuenciaNumerica(String identificador) {
        if (identificador == null) return 0;
        int inicio = identificador.length();
        while (inicio > 0 && Character.isDigit(identificador.charAt(inicio - 1))) inicio--;
        if (inicio == identificador.length()) return 0;
        try { return Long.parseLong(identificador.substring(inicio)); }
        catch (NumberFormatException ex) { return 0; }
    }

    // ------------------------------------------------------------------
    // RESPALDO
    // ------------------------------------------------------------------

    /**
     * Exporta una instantánea textual del estado del sistema en formato TSV UTF-8.
     * Cada fila tiene un tipo de registro; en los campos se escapan el tabulador,
     * los saltos de línea y la barra invertida.
     * No es un mecanismo de restauración automática; es un respaldo legible para
     * conservar los datos principales y permitir una restauración posterior.
     *
     * @param rutaArchivo ruta del archivo de salida.
     * @throws IllegalArgumentException si la ruta es nula o vacía.
     * @throws IllegalStateException si el archivo no se puede escribir.
     */
    public void guardarRespaldoSistema(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del respaldo no puede estar vacía.");
        }

        java.nio.file.Path ruta = java.nio.file.Paths.get(rutaArchivo.trim());
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(
                ruta, java.nio.charset.StandardCharsets.UTF_8)) {
            escribirFilaRespaldo(writer, "FINTECH_BACKUP", "1", LocalDateTime.now().toString());

            escribirFilaRespaldo(writer, "SECCION", "USUARIOS");
            for (Usuario usuario : listaUsuarios) {
                escribirFilaRespaldo(writer, "USUARIO", usuario.getId(), usuario.getNombre(),
                        usuario.getCorreo(), String.valueOf(usuario.getPuntosAcumulados()),
                        usuario.getNivel(), String.valueOf(usuario.isActivo()));
                for (Billetera billetera : usuario.getBilleteras()) {
                    escribirFilaRespaldo(writer, "BILLETERA", String.valueOf(billetera.getCodigo()),
                            usuario.getId(), billetera.getNombre(), String.valueOf(billetera.getTipo()),
                            String.valueOf(billetera.getSaldo()), String.valueOf(billetera.isActivo()));
                }
                for (Alerta alerta : usuario.getHistorialAlertas()) {
                    escribirFilaRespaldo(writer, "ALERTA", usuario.getId(), String.valueOf(alerta.getId()),
                            alerta.getFecha().toString(), alerta.getTipoAlerta(), alerta.getMensaje(),
                            String.valueOf(alerta.isAtendida()));
                }
                for (CanjeBeneficio canje : usuario.getHistorialCanjes()) {
                    escribirFilaRespaldo(writer, "CANJE_USUARIO", canje.getId(), usuario.getId(),
                            canje.getIdBeneficio(), canje.getNombreBeneficio(), canje.getFechaHora().toString(),
                            String.valueOf(canje.getPuntosGastados()), canje.getEstado(),
                            canje.getCodigoRedencion(),
                            canje.getFechaUso() == null ? "" : canje.getFechaUso().toString());
                }
            }

            escribirFilaRespaldo(writer, "SECCION", "TRANSACCIONES");
            for (Transaccion tx : historialGlobal) {
                escribirFilaRespaldo(writer, "TRANSACCION", tx.getId(), tx.getFechaHora().toString(),
                        String.valueOf(tx.getTipo()), String.valueOf(tx.getValor()),
                        tx.getBilleteraOrigen(), tx.getBilleteraDestino(), tx.getEstado(),
                        String.valueOf(tx.getPuntosGenerados()), String.valueOf(tx.getNivelRiesgo()));
            }

            escribirFilaRespaldo(writer, "SECCION", "OPERACIONES_PROGRAMADAS_PENDIENTES");
            for (OperacionProgramada op : operacionesProgramadas.aListaOrdenada()) {
                escribirOperacionProgramadaRespaldo(writer, "OPERACION_PENDIENTE", op);
            }
            escribirFilaRespaldo(writer, "SECCION", "OPERACIONES_PROGRAMADAS_PROCESADAS");
            for (OperacionProgramada op : operacionesProcesadas) {
                escribirOperacionProgramadaRespaldo(writer, "OPERACION_PROCESADA", op);
            }

            escribirFilaRespaldo(writer, "SECCION", "BENEFICIOS_Y_CANJES");
            for (Beneficio beneficio : catalogoBeneficios) {
                escribirFilaRespaldo(writer, "BENEFICIO", beneficio.getId(), beneficio.getNombre(),
                        beneficio.getDescripcion(), String.valueOf(beneficio.getCostoPuntos()),
                        String.valueOf(beneficio.isActivo()), String.valueOf(beneficio.getValorBono()));
            }
            for (CanjeBeneficio canje : historialCanjes) {
                escribirFilaRespaldo(writer, "CANJE", canje.getId(), canje.getIdUsuario(),
                        canje.getIdBeneficio(), canje.getNombreBeneficio(), canje.getFechaHora().toString(),
                        String.valueOf(canje.getPuntosGastados()), canje.getEstado(),
                        canje.getCodigoRedencion(),
                        canje.getFechaUso() == null ? "" : canje.getFechaUso().toString(),
                        String.valueOf(canje.getValorBono()));
            }

            escribirFilaRespaldo(writer, "SECCION", "NOTIFICACIONES_PENDIENTES");
            for (Alerta alerta : notificacionesPendientes) {
                escribirFilaRespaldo(writer, "NOTIFICACION", String.valueOf(alerta.getId()),
                        alerta.getFecha().toString(), alerta.getTipoAlerta(), alerta.getMensaje(),
                        String.valueOf(alerta.isAtendida()));
            }

            escribirFilaRespaldo(writer, "SECCION", "AUDITORIA_DE_RIESGO");
            for (EventoAuditoria evento : detectorRiesgo.obtenerHistorialAuditoria()) {
                escribirFilaRespaldo(writer, "EVENTO_AUDITORIA", String.valueOf(evento.getId()),
                        evento.getFecha().toString(), evento.getIdUsuario(), evento.getIdTransaccion(),
                        String.valueOf(evento.getNivel()), evento.getMotivo());
            }
        } catch (java.io.IOException | java.nio.file.InvalidPathException e) {
            throw new IllegalStateException("No fue posible guardar el respaldo en: " + rutaArchivo, e);
        }
    }

    private void escribirOperacionProgramadaRespaldo(
            java.io.BufferedWriter writer, String tipoRegistro, OperacionProgramada op) throws java.io.IOException {
        escribirFilaRespaldo(writer, tipoRegistro, op.getId(), op.getFechaHora().toString(),
                String.valueOf(op.getTipo()), String.valueOf(op.getValor()), op.getBilleteraOrigen(),
                op.getBilleteraDestino(), String.valueOf(op.getFrecuencia()), op.getEstado(),
                op.getIdUsuario(), String.valueOf(op.isNotificacionProximaEnviada()));
    }

    private static void escribirFilaRespaldo(java.io.BufferedWriter writer, String... campos)
            throws java.io.IOException {
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) writer.write('\t');
            String campo = campos[i] == null ? "" : campos[i];
            writer.write(campo.replace("\\", "\\\\")
                    .replace("\t", "\\t")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n"));
        }
        writer.newLine();
    }
}