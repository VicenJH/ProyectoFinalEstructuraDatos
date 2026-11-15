package co.edu.uniquindio.proyectofinal.Estructuras.Controllers;

import co.edu.uniquindio.proyectofinal.Estructuras.Grafos.Grafo;
import co.edu.uniquindio.proyectofinal.Estructuras.Hash.TablaHash;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;
import co.edu.uniquindio.proyectofinal.Estructuras.Arbol.ArbolBinario;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.Cola;
import co.edu.uniquindio.proyectofinal.Estructuras.Pilas.Stack;

import java.time.LocalDateTime;

public class FintechController {


    private TablaHash<String, Usuario> usuariosPorId;
    private TablaHash<Integer, Billetera> billeterasPorCodigo;
    private ArbolBinario<Usuario> arbolUsuariosPorPuntos;
    private Cola<OperacionProgramada> operacionesProgramadas;
    private Stack<Transaccion> pilaReversiones;
    private Grafo<String> grafoTransferencias;

    public FintechController() {
        this.usuariosPorId = new TablaHash<>(100);
        this.billeterasPorCodigo = new TablaHash<>(500);
        this.arbolUsuariosPorPuntos = new ArbolBinario<>();
        this.operacionesProgramadas = new Cola<>();
        this.pilaReversiones = new Stack<>();
        this.grafoTransferencias = new Grafo<>();
    }



    // MÉTODOS DE GESTIÓN DE USUARIOS
    public boolean registrarUsuario(String id, String nombre, String correo) {
        if (usuariosPorId.buscar(id) != null) {
            return false;
        }
        Usuario nuevoUsuario = new Usuario(id, nombre, correo);
        usuariosPorId.insertar(id, nuevoUsuario);
        arbolUsuariosPorPuntos.insertar(nuevoUsuario);
        grafoTransferencias.agregarVertice(id);
        return true;
    }

    public Usuario buscarUsuario(String id) {
        return usuariosPorId.buscar(id);
    }



    // MÉTODOS DE GESTIÓN DE BILLETERAS
    public boolean crearBilleteraAUsuario(String idUsuario, int codigoBilletera, TipoBilletera tipo, int saldoInicial) {
        Usuario usuario = buscarUsuario(idUsuario);
        if (usuario == null) return false;

        if (billeterasPorCodigo.buscar(codigoBilletera) != null) {
            return false; // La billetera ya existe en el sistema
        }

        Billetera nuevaBilletera = new Billetera(codigoBilletera, tipo, saldoInicial, true);
        usuario.getBilleteras().addLast(nuevaBilletera);
        billeterasPorCodigo.insertar(codigoBilletera, nuevaBilletera);
        return true;
    }



    // MÉTODOS DE OPERACIONES FINANCIERAS Y PUNTOS
    public boolean realizarRecarga(int codigoBilletera, int monto, String idUsuario) {
        Billetera billetera = billeterasPorCodigo.buscar(codigoBilletera);
        Usuario usuario = buscarUsuario(idUsuario);

        if (billetera == null || usuario == null || !billetera.isActivo()) return false;

        billetera.recargar(monto);

        int puntosGenerados = monto / 100;
        usuario.agregarPuntos(puntosGenerados);

        return true;
    }



    // MÓDULO DE TRANSACCIONES Y REVERSIONES

    /*
     * Realiza una transferencia de dinero entre dos billeteras (pueden ser del mismo o de otro usuario).
     * Registra la transacción, calcula puntos, actualiza saldos y la guarda en la pila de reversiones.
     */
    public boolean realizarTransferencia(int codigoOrigen, int codigoDestino, double monto, String idUsuarioOrigen) {
        Billetera billeteraOrigen = billeterasPorCodigo.buscar(codigoOrigen);
        Billetera billeteraDestino = billeterasPorCodigo.buscar(codigoDestino);
        Usuario usuarioOrigen = buscarUsuario(idUsuarioOrigen);

        if (billeteraOrigen == null || billeteraDestino == null || usuarioOrigen == null) {
            return false;
        }
        if (!billeteraOrigen.isActivo() || !billeteraDestino.isActivo()) {
            return false;
        }
        if (billeteraOrigen.getSaldo() < monto) {
            return false;
        }
        if (evaluarYGenerarAlertaInusual(usuarioOrigen, monto, "TX-CHECK")) {
            System.out.println("⚠️ La transacción generó una alerta de seguridad, pero fue procesada bajo supervisión.");
        }

        billeteraOrigen.retirar((int) monto);
        billeteraDestino.recargar((int) monto);

        int puntosGenerados = (int) (monto / 100) * 3;
        usuarioOrigen.agregarPuntos(puntosGenerados);

        String idTransaccion = "TX-" + System.currentTimeMillis();
        Transaccion nuevaTransaccion = new Transaccion(
                idTransaccion,
                monto,
                String.valueOf(codigoOrigen),
                String.valueOf(codigoDestino),
                java.time.LocalDateTime.now(),
                TipoTransaccion.TRANSFERENCIA_INTERNA,
                "EXITOSA",
                puntosGenerados
        );

        usuarioOrigen.getHistorialTransacciones().addLast(nuevaTransaccion);
        pilaReversiones.push(nuevaTransaccion);

        return true;
    }

    /*
     * Revierte la última operación realizada utilizando la Pila (LIFO: Último en entrar, primero en salir).
     */
    public boolean revertirUltimaOperacion() {
        if (pilaReversiones.isEmpty()) {
            return false;
        }

        Transaccion ultimaTx = pilaReversiones.pop();

        if (!"EXITOSA".equals(ultimaTx.getEstado())) {
            return false;
        }

        int codOrigen = Integer.parseInt(ultimaTx.getBilleteraOrigen());
        int codDestino = Integer.parseInt(ultimaTx.getBilleteraDestino());

        Billetera billeteraOrigen = billeterasPorCodigo.buscar(codOrigen);
        Billetera billeteraDestino = billeterasPorCodigo.buscar(codDestino);

        if (billeteraOrigen == null || billeteraDestino == null) {
            return false;
        }

        billeteraDestino.retirar((int) ultimaTx.getValor());
        billeteraOrigen.recargar((int) ultimaTx.getValor());

        ultimaTx.setEstado("REVERTIDA");

        return true;
    }




    // MÓDULO DE OPERACIONES PROGRAMADAS (COLA)

    /*
     * Agrega una operación programada a la cola del sistema.
     */
    public void programarOperacion(String id, double valor, int codOrigen, int codDestino,
                                   LocalDateTime fechaHora, TipoTransaccion tipo, Frecuencia frecuencia, String estado) {
        OperacionProgramada op = new OperacionProgramada(
                id,
                valor,
                String.valueOf(codOrigen),
                String.valueOf(codDestino),
                fechaHora,
                tipo,
                frecuencia,
                estado
        );
        operacionesProgramadas.encolar(op);
    }

    /*
     * Simula el procesamiento automático de las operaciones programadas cuya fecha ya se cumplió.
     */
    public void procesarOperacionesProgramadas() {
        if (operacionesProgramadas.isEmpty()) {
            return;
        }


        Cola<OperacionProgramada> colaAuxiliar = new Cola<>();
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();

        while (!operacionesProgramadas.isEmpty()) {
            OperacionProgramada op = operacionesProgramadas.desencolar();


            if (op.getFechaHora().isBefore(ahora) || op.getFechaHora().isEqual(ahora)) {
                realizarTransferencia(
                        Integer.parseInt(op.getBilleteraOrigen()),
                        Integer.parseInt(op.getBilleteraDestino()),
                        op.getValor(),
                        "USR-DEFAULT"
                );
            } else {

                colaAuxiliar.encolar(op);
            }
        }


        while (!colaAuxiliar.isEmpty()) {
            operacionesProgramadas.encolar(colaAuxiliar.desencolar());
        }
    }




    // MÓDULO DE REPORTES Y ÁRBOL DE PUNTOS

    /*
     * Muestra en consola (o devuelve en lista) los usuarios ordenados de menor a mayor
     * según sus puntos de fidelización usando el recorrido Inorden del Árbol Binario.
     */
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




    // MÓDULO DE GRAFOS Y ANALÍTICA DE TRANSFERENCIAS

    /*
     * Registra o actualiza una arista en el grafo cada vez que se realiza una transferencia entre usuarios.
     * El peso de la arista puede representar el monto total transferido o la frecuencia de las transacciones.
     */
    public void registrarTransferenciaEnGrafo(String idUsuarioOrigen, String idUsuarioDestino, double monto) {
        grafoTransferencias.agregarVertice(idUsuarioOrigen);
        grafoTransferencias.agregarVertice(idUsuarioDestino);

        Grafo.Vertice<String> origen = grafoTransferencias.buscarVertice(idUsuarioOrigen);
        boolean aristaExistente = false;

        if (origen != null) {
            for (int i = 0; i < origen.getAdyacentes().getSize(); i++) {
                Grafo.Arista<String> arista = origen.getAdyacentes().getNodeValue(i);
                if (arista.getDestino().getDato().equals(idUsuarioDestino)) {
                    aristaExistente = true;
                    break;
                }
            }
        }

        if (!aristaExistente) {
            grafoTransferencias.agregarArista(idUsuarioOrigen, idUsuarioDestino, monto);
        }
    }

    /*
     * Analiza el grafo para identificar el usuario con mayor actividad o conexiones salientes.
     */
    public String obtenerUsuarioConMayorActividadRed() {
        SimpleLinkedList<Grafo.Vertice<String>> vertices = grafoTransferencias.getVertices();
        if (vertices.isEmpty()) return null;

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



    // MÓDULO DE DETECCIÓN DE COMPORTAMIENTO INUSUAL

    /*
     * Analiza si una transacción cumple con criterios de comportamiento sospechoso o inusual.
     * Criterios de ejemplo:
     * 1. Monto excesivamente alto (ej: mayor a $5,000,000 en una sola operación).
     * 2. Múltiples transacciones en menos de 1 minuto (posible fraude o bot).
     */
    public boolean evaluarYGenerarAlertaInusual(Usuario usuario, double montoActual, String idTransaccion) {
        boolean esSospechoso = false;
        String motivo = "";

        // Criterio 1: Monto atípicamente alto
        if (montoActual > 5000000.0) {
            esSospechoso = true;
            motivo = "Transacción con monto atípicamente alto: $" + montoActual;
        }


        SimpleLinkedList<Transaccion> historial = usuario.getHistorialTransacciones();
        if (!historial.isEmpty() && !esSospechoso) {

            Transaccion ultimaTx = historial.getNodeValue(historial.getSize() - 1);


            long segundosDiferencia = java.time.Duration.between(ultimaTx.getFechaHora(), java.time.LocalDateTime.now()).getSeconds();
            if (segundosDiferencia < 60) {
                esSospechoso = true;
                motivo = "Alta frecuencia de transacciones detectada en menos de 1 minuto.";
            }
        }

        if (esSospechoso) {
            generarAlertaParaUsuario(usuario, motivo, "SEGURIDAD");
            return true;
        }

        return false;
    }

    /*
     * Crea una alerta de seguridad y la asocia al usuario (puedes guardarla en una lista propia del usuario o global).
     */
    private void generarAlertaParaUsuario(Usuario usuario, String mensaje, String tipoAlerta) {
        int idAlerta = (int) (System.currentTimeMillis() % 100000); // ID numérico único generado por tiempo
        Alerta nuevaAlerta = new Alerta(idAlerta, java.time.LocalDateTime.now(), mensaje, tipoAlerta);

        System.out.println("⚠️ [ALERTA DE SEGURIDAD] Usuario: " + usuario.getNombre() + " - " + mensaje);
    }
}