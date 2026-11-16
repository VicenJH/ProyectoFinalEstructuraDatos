package co.edu.uniquindio.proyectofinal.Estructuras.Servicios;

import co.edu.uniquindio.proyectofinal.Estructuras.Arbol.ArbolBinario;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.ColaPrioridad;
import co.edu.uniquindio.proyectofinal.Estructuras.Grafos.Grafo;
import co.edu.uniquindio.proyectofinal.Estructuras.Hash.TablaHash;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Billetera;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoBilletera;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoTransaccion;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Transaccion;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Usuario;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.time.LocalDateTime;

/**
 * Consultas e informes de analítica (requisitos 4.8 y 8).
 *
 * No guarda datos propios: lee las estructuras del controlador.
 * Solo cuentan las transacciones en estado EXITOSA, salvo
 * obtenerTransaccionesEnPeriodo, que devuelve todas.
 *
 * Los "top N" usan una cola de prioridad (min-heap) acotada a N elementos:
 * O(m log N) en vez de ordenar todo en O(m log m).
 *
 * En los rangos de fechas, un límite null significa "sin límite".
 */
public class AnaliticaFintech {

    /** Ruta de dinero entre dos usuarios con su frecuencia y monto acumulado. */
    public static class RutaFrecuente {
        private final String origen;
        private final String destino;
        private int veces;
        private long montoTotal;

        private RutaFrecuente(String origen, String destino) {
            this.origen = origen;
            this.destino = destino;
        }

        private void registrar(long monto) {
            if (veces < Integer.MAX_VALUE) veces++;
            montoTotal = montoTotal > Long.MAX_VALUE - monto ? Long.MAX_VALUE : montoTotal + monto;
        }

        public String getOrigen() {
            return origen;
        }

        public String getDestino() {
            return destino;
        }

        public int getVeces() {
            return veces;
        }

        public long getMontoTotal() {
            return montoTotal;
        }

        @Override
        public String toString() {
            return origen + " → " + destino + " (" + veces + " veces, $" + montoTotal + ")";
        }
    }

    /** Elemento con su cantidad, usado para armar los rankings. */
    private static class Conteo<T> {
        private final T elemento;
        private final long cantidad;

        private Conteo(T elemento, long cantidad) {
            this.elemento = elemento;
            this.cantidad = cantidad;
        }
    }

    private final SimpleLinkedList<Transaccion> historialGlobal;
    private final SimpleLinkedList<Usuario> usuarios;
    private final TablaHash<Integer, Billetera> billeteras;
    private final ArbolBinario<Usuario> arbolPuntos;
    private final Grafo<String> grafo;

    public AnaliticaFintech(
            SimpleLinkedList<Transaccion> historialGlobal,
            SimpleLinkedList<Usuario> usuarios,
            TablaHash<Integer, Billetera> billeteras,
            ArbolBinario<Usuario> arbolPuntos,
            Grafo<String> grafo
    ) {
        this.historialGlobal = historialGlobal;
        this.usuarios = usuarios;
        this.billeteras = billeteras;
        this.arbolPuntos = arbolPuntos;
        this.grafo = grafo;
    }

    // ------------------------------------------------------------------
    // PERIODOS Y TOTALES
    // ------------------------------------------------------------------

    /** @return todas las transacciones del período (cualquier estado). O(n). */
    public SimpleLinkedList<Transaccion> obtenerTransaccionesEnPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        validarRango(desde, hasta);

        SimpleLinkedList<Transaccion> resultado = new SimpleLinkedList<>();

        for (Transaccion t : historialGlobal) {
            if (enPeriodo(t, desde, hasta)) {
                resultado.addLast(t.copia());
            }
        }

        return resultado;
    }

    /** @return monto total movilizado en el rango (solo transacciones exitosas). */
    public long obtenerMontoTotalMovilizado(LocalDateTime desde, LocalDateTime hasta) {
        validarRango(desde, hasta);

        long total = 0;

        for (Transaccion t : historialGlobal) {
            if (exitosa(t) && enPeriodo(t, desde, hasta)) {
                long monto = (long) t.getValor();
                total = total > Long.MAX_VALUE - monto ? Long.MAX_VALUE : total + monto;
            }
        }

        return total;
    }

    /**
     * Frecuencia de transacciones por tipo.
     *
     * @return arreglo en el orden de TipoTransaccion.values().
     */
    public int[] obtenerFrecuenciaPorTipo() {
        int[] frecuencias = new int[TipoTransaccion.values().length];

        for (Transaccion t : historialGlobal) {
            if (exitosa(t)) {
                if (frecuencias[t.getTipo().ordinal()] < Integer.MAX_VALUE) {
                    frecuencias[t.getTipo().ordinal()]++;
                }
            }
        }

        return frecuencias;
    }

    // ------------------------------------------------------------------
    // RANKINGS
    // ------------------------------------------------------------------

    /** @return las N billeteras con más movimientos, de mayor a menor. */
    public SimpleLinkedList<Billetera> obtenerBilleterasMasUsadas(int n) {
        SimpleLinkedList<Conteo<Billetera>> conteos = new SimpleLinkedList<>();

        for (Usuario usuario : usuarios) {
            for (Billetera billetera : usuario.getBilleteras()) {
                long usos = 0;

                for (Transaccion t : billetera.getHistorialTransacciones()) {
                    if (exitosa(t) && usos < Long.MAX_VALUE) {
                        usos++;
                    }
                }

                if (usos > 0) {
                    conteos.addLast(new Conteo<>(billetera, usos));
                }
            }
        }

        return elementos(mayores(conteos, n));
    }

    /** @return la billetera con más movimientos o null si no hay ninguno. */
    public Billetera obtenerBilleteraMasUsada() {
        SimpleLinkedList<Billetera> top = obtenerBilleterasMasUsadas(1);
        return top.isEmpty() ? null : top.getNodeValue(0);
    }

    /** @return las N transacciones de mayor valor, de mayor a menor. */
    public SimpleLinkedList<Transaccion> obtenerTransaccionesMayorValor(int n) {
        SimpleLinkedList<Conteo<Transaccion>> conteos = new SimpleLinkedList<>();

        for (Transaccion t : historialGlobal) {
            if (exitosa(t)) {
                conteos.addLast(new Conteo<>(t, Math.round(t.getValor())));
            }
        }

        SimpleLinkedList<Transaccion> seleccionadas = elementos(mayores(conteos, n));
        SimpleLinkedList<Transaccion> resultado = new SimpleLinkedList<>();
        for (Transaccion transaccion : seleccionadas) resultado.addLast(transaccion.copia());
        return resultado;
    }

    /** @return los N usuarios con más transferencias realizadas. */
    public SimpleLinkedList<Usuario> obtenerUsuariosConMasTransferencias(int n) {
        return elementos(mayores(contarPorUsuario(null, null, true), n));
    }

    /** @return los N usuarios con más operaciones en el período. */
    public SimpleLinkedList<Usuario> obtenerUsuariosMasActivos(int n, LocalDateTime desde, LocalDateTime hasta) {
        validarRango(desde, hasta);
        return elementos(mayores(contarPorUsuario(desde, hasta, false), n));
    }

    /** @return el usuario con mayor actividad en el período o null si no hubo. */
    public Usuario obtenerUsuarioMasActivo(LocalDateTime desde, LocalDateTime hasta) {
        SimpleLinkedList<Usuario> top = obtenerUsuariosMasActivos(1, desde, hasta);
        return top.isEmpty() ? null : top.getNodeValue(0);
    }

    /**
     * Movimientos por categoría de billetera (origen y destino cuentan).
     *
     * @return arreglo en el orden de TipoBilletera.values().
     */
    public int[] obtenerMovimientosPorCategoria() {
        int[] movimientos = new int[TipoBilletera.values().length];

        for (Transaccion t : historialGlobal) {
            if (!exitosa(t)) {
                continue;
            }

            sumarCategoria(movimientos, t.getBilleteraOrigen());
            sumarCategoria(movimientos, t.getBilleteraDestino());
        }

        return movimientos;
    }

    /** @return la categoría con más movimientos o null si no hay movimientos. */
    public TipoBilletera obtenerCategoriaMasActiva() {
        int[] movimientos = obtenerMovimientosPorCategoria();
        int mejor = -1;
        int maximo = 0;

        for (int i = 0; i < movimientos.length; i++) {
            if (movimientos[i] > maximo) {
                maximo = movimientos[i];
                mejor = i;
            }
        }

        return mejor < 0 ? null : TipoBilletera.values()[mejor];
    }

    // ------------------------------------------------------------------
    // GRAFO
    // ------------------------------------------------------------------

    /**
     * Rutas de dinero entre usuarios más frecuentes (patrones de movimiento).
     *
     * @return las N rutas con más transferencias, de mayor a menor.
     */
    public SimpleLinkedList<RutaFrecuente> obtenerRutasFrecuentes(int n) {
        TablaHash<String, RutaFrecuente> porClave = new TablaHash<>(101);
        SimpleLinkedList<RutaFrecuente> rutas = new SimpleLinkedList<>();

        for (Transaccion t : historialGlobal) {
            if (!exitosa(t) || !esTransferencia(t) || t.getBilleteraDestino() == null) {
                continue;
            }

            Billetera origen = billeteras.buscar(Integer.parseInt(t.getBilleteraOrigen()));
            Billetera destino = billeteras.buscar(Integer.parseInt(t.getBilleteraDestino()));

            if (origen == null || destino == null
                    || origen.getIdUsuario() == null || destino.getIdUsuario() == null
                    || origen.getIdUsuario().equals(destino.getIdUsuario())) {
                continue;
            }

            String idOrigen = origen.getIdUsuario();
            String idDestino = destino.getIdUsuario();
            String clave = idOrigen.length() + ":" + idOrigen + idDestino.length() + ":" + idDestino;
            RutaFrecuente ruta = porClave.buscar(clave);

            if (ruta == null) {
                ruta = new RutaFrecuente(origen.getIdUsuario(), destino.getIdUsuario());
                porClave.insertar(clave, ruta);
                rutas.addLast(ruta);
            }

            ruta.registrar((long) t.getValor());
        }

        SimpleLinkedList<Conteo<RutaFrecuente>> conteos = new SimpleLinkedList<>();

        for (RutaFrecuente ruta : rutas) {
            conteos.addLast(new Conteo<>(ruta, ruta.veces));
        }

        return elementos(mayores(conteos, n));
    }

    /**
     * Detecta ciclos de transferencias (ej. A → B → C → A) con un DFS.
     * Informa un ciclo por cada arista de retorno encontrada. Las transferencias
     * revertidas se retiran del grafo y, por tanto, no participan en el recorrido.
     *
     * Complejidad: O(V + A).
     *
     * @return ciclos como texto "A → B → A".
     */
    public SimpleLinkedList<String> detectarCiclos() {
        TablaHash<String, Integer> estado = new TablaHash<>(101); // 1 = en proceso, 2 = terminado
        SimpleLinkedList<String> camino = new SimpleLinkedList<>();
        SimpleLinkedList<String> ciclos = new SimpleLinkedList<>();

        for (Grafo.Vertice<String> vertice : grafo.getVertices()) {
            if (estado.buscar(vertice.getDato()) == null) {
                explorar(vertice, estado, camino, ciclos);
            }
        }

        return ciclos;
    }

    private void explorar(Grafo.Vertice<String> vertice, TablaHash<String, Integer> estado,
                          SimpleLinkedList<String> camino, SimpleLinkedList<String> ciclos) {
        estado.insertar(vertice.getDato(), 1);
        camino.addLast(vertice.getDato());

        for (Grafo.Arista<String> arista : vertice.getAdyacentes()) {
            if (arista.getPeso() <= 0) {
                continue;
            }

            Grafo.Vertice<String> vecino = arista.getDestino();
            Integer estadoVecino = estado.buscar(vecino.getDato());

            if (estadoVecino == null) {
                explorar(vecino, estado, camino, ciclos);
            } else if (estadoVecino == 1) {
                ciclos.addLast(describirCiclo(camino, vecino.getDato()));
            }
        }

        // Los vértices del camino son únicos, así que remove quita el correcto.
        camino.remove(vertice.getDato());
        estado.insertar(vertice.getDato(), 2);
    }

    private String describirCiclo(SimpleLinkedList<String> camino, String inicio) {
        StringBuilder texto = new StringBuilder();
        boolean dentro = false;

        for (String id : camino) {
            if (id.equals(inicio)) {
                dentro = true;
            }
            if (dentro) {
                texto.append(id).append(" → ");
            }
        }

        return texto.append(inicio).toString();
    }

    // ------------------------------------------------------------------
    // ÁRBOL DE PUNTOS
    // ------------------------------------------------------------------

    /**
     * Busca usuarios con puntos entre min y max (inclusive), en orden
     * ascendente. Descarta subárboles completos: O(h + k).
     */
    public SimpleLinkedList<Usuario> buscarUsuariosPorRangoDePuntos(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("El mínimo no puede ser mayor que el máximo.");
        }

        SimpleLinkedList<Usuario> encontrados = arbolPuntos.buscarPorRango(
                Usuario::getPuntosAcumulados, min, max);
        SimpleLinkedList<Usuario> resultado = new SimpleLinkedList<>();
        for (Usuario usuario : encontrados) resultado.addLast(usuario.copia());
        return resultado;
    }

    /** @return todos los usuarios de menor a mayor puntaje (recorrido inorden). */
    public SimpleLinkedList<Usuario> obtenerUsuariosOrdenadosPorPuntos() {
        return buscarUsuariosPorRangoDePuntos(0, Integer.MAX_VALUE);
    }

    /**
     * @param nivel Bronce, Plata, Oro o Platino (sin distinguir mayúsculas).
     * @throws IllegalArgumentException si el nivel no existe.
     */
    public SimpleLinkedList<Usuario> obtenerUsuariosPorNivel(String nivel) {
        if (nivel == null) {
            throw new IllegalArgumentException("El nivel no puede ser null.");
        }

        switch (nivel.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "bronce":
                return buscarUsuariosPorRangoDePuntos(0, 500);
            case "plata":
                return buscarUsuariosPorRangoDePuntos(501, 1000);
            case "oro":
                return buscarUsuariosPorRangoDePuntos(1001, 5000);
            case "platino":
                return buscarUsuariosPorRangoDePuntos(5001, Integer.MAX_VALUE);
            default:
                throw new IllegalArgumentException("Nivel desconocido: " + nivel);
        }
    }

    // ------------------------------------------------------------------
    // AUXILIARES
    // ------------------------------------------------------------------

    private SimpleLinkedList<Conteo<Usuario>> contarPorUsuario(LocalDateTime desde, LocalDateTime hasta,
                                                               boolean soloTransferencias) {
        TablaHash<String, Long> conteos = new TablaHash<>(101);

        for (Transaccion t : historialGlobal) {
            if (!exitosa(t) || !enPeriodo(t, desde, hasta)
                    || (soloTransferencias && !esTransferencia(t))) {
                continue;
            }

            Billetera origen = billeteras.buscar(Integer.parseInt(t.getBilleteraOrigen()));

            if (origen == null || origen.getIdUsuario() == null) {
                continue;
            }

            incrementarConteo(conteos, origen.getIdUsuario());

            // Para actividad general también se cuenta la participación como receptor
            // de una transferencia externa. El ranking de transferencias realizadas
            // (soloTransferencias=true) conserva el criterio de contar al emisor.
            if (!soloTransferencias && esTransferencia(t) && t.getBilleteraDestino() != null) {
                Billetera destino = billeteras.buscar(Integer.parseInt(t.getBilleteraDestino()));
                if (destino != null && destino.getIdUsuario() != null
                        && !destino.getIdUsuario().equals(origen.getIdUsuario())) {
                    incrementarConteo(conteos, destino.getIdUsuario());
                }
            }
        }

        SimpleLinkedList<Conteo<Usuario>> resultado = new SimpleLinkedList<>();

        for (Usuario usuario : usuarios) {
            Long cantidad = conteos.buscar(usuario.getId());

            if (cantidad != null) {
                resultado.addLast(new Conteo<>(usuario.copia(), cantidad));
            }
        }

        return resultado;
    }

    /** Selecciona los N mayores con un min-heap acotado. O(m log N). */
    private static <T> SimpleLinkedList<Conteo<T>> mayores(SimpleLinkedList<Conteo<T>> conteos, int n) {
        SimpleLinkedList<Conteo<T>> resultado = new SimpleLinkedList<>();

        if (n <= 0) {
            return resultado;
        }

        ColaPrioridad<Conteo<T>> heap = new ColaPrioridad<>(
                (a, b) -> Long.compare(a.cantidad, b.cantidad)
        );

        for (Conteo<T> conteo : conteos) {
            heap.encolar(conteo);

            if (heap.getSize() > n) {
                heap.desencolar();
            }
        }

        // Salen de menor a mayor; addFirst deja la lista de mayor a menor.
        while (!heap.isEmpty()) {
            resultado.addFirst(heap.desencolar());
        }

        return resultado;
    }

    private static <T> SimpleLinkedList<T> elementos(SimpleLinkedList<Conteo<T>> conteos) {
        SimpleLinkedList<T> resultado = new SimpleLinkedList<>();

        for (Conteo<T> conteo : conteos) {
            resultado.addLast(conteo.elemento);
        }

        return resultado;
    }

    private void sumarCategoria(int[] movimientos, String codigoBilletera) {
        if (codigoBilletera == null) {
            return;
        }

        Billetera billetera = billeteras.buscar(Integer.parseInt(codigoBilletera));

        if (billetera != null) {
            int indice = billetera.getTipo().ordinal();
            if (movimientos[indice] < Integer.MAX_VALUE) movimientos[indice]++;
        }
    }

    private void incrementarConteo(TablaHash<String, Long> conteos, String idUsuario) {
        Long previo = conteos.buscar(idUsuario);
        long siguiente = previo == null ? 1L : (previo == Long.MAX_VALUE ? Long.MAX_VALUE : previo + 1L);
        conteos.insertar(idUsuario, siguiente);
    }

    private boolean exitosa(Transaccion t) {
        return "EXITOSA".equals(t.getEstado());
    }

    private boolean esTransferencia(Transaccion t) {
        return t.getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA
                || t.getTipo() == TipoTransaccion.TRANSFERENCIA_EXTERNA;
    }

    private boolean enPeriodo(Transaccion t, LocalDateTime desde, LocalDateTime hasta) {
        return (desde == null || !t.getFechaHora().isBefore(desde))
                && (hasta == null || !t.getFechaHora().isAfter(hasta));
    }

    private void validarRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
    }
}