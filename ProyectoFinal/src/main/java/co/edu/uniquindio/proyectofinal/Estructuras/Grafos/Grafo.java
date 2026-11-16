package co.edu.uniquindio.proyectofinal.Estructuras.Grafos;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.util.Objects;

/**
 * Implementa un grafo dirigido y ponderado mediante listas de adyacencia.
 *
 * Cada vértice representa un elemento del sistema y cada arista representa
 * una conexión dirigida hacia otro vértice.
 *
 * En el sistema Fintech:
 * - Los vértices representan usuarios.
 * - Las aristas representan transferencias entre usuarios.
 * - Los pesos representan los montos asociados a las transferencias.
 *
 * El grafo utiliza una lista simplemente enlazada para almacenar los
 * vértices y otra lista de adyacencia para almacenar sus conexiones.
 *
 * No utiliza las colecciones nativas de Java para representar el grafo.
 *
 * @param <T> tipo de dato almacenado en los vértices.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class Grafo<T> {

    /**
     * Representa un vértice del grafo.
     *
     * Cada vértice contiene un dato y una lista de aristas salientes.
     *
     * @param <T> tipo de dato almacenado.
     */
    public static class Vertice<T> {

        /** Dato que identifica al vértice. */
        private T dato;

        /** Lista de aristas que salen de este vértice. */
        private SimpleLinkedList<Arista<T>> adyacentes;

        /**
         * Construye un vértice con el dato especificado.
         *
         * @param dato información que identifica al vértice.
         * @throws IllegalArgumentException si el dato es null.
         */
        public Vertice(T dato) {

            if (dato == null) {
                throw new IllegalArgumentException(
                        "El dato del vértice no puede ser null."
                );
            }

            this.dato = dato;
            this.adyacentes = new SimpleLinkedList<>();
        }

        /**
         * Obtiene el dato almacenado en el vértice.
         *
         * @return dato del vértice.
         *
         * Complejidad temporal: O(1).
         */
        public T getDato() {
            return dato;
        }

        /**
         * Obtiene la lista de aristas salientes del vértice.
         *
         * @return lista de adyacencia del vértice.
         *
         * Complejidad temporal: O(A), porque genera una copia defensiva de las aristas.
         */
        public SimpleLinkedList<Arista<T>> getAdyacentes() {
            SimpleLinkedList<Arista<T>> copia = new SimpleLinkedList<>();
            for (Arista<T> arista : adyacentes) {
                copia.addLast(new Arista<>(arista.destino, arista.peso));
            }
            return copia;
        }
    }

    /**
     * Representa una arista dirigida y ponderada.
     *
     * Una arista conecta un vértice de origen con un vértice de destino.
     * El origen está determinado por el vértice cuya lista de adyacencia
     * contiene esta arista.
     *
     * @param <T> tipo de dato de los vértices.
     */
    public static class Arista<T> {

        /** Vértice al que conduce la arista. */
        private Vertice<T> destino;

        /** Peso asociado a la conexión. */
        private double peso;

        /**
         * Construye una arista con un destino y un peso.
         *
         * @param destino vértice al que apunta la arista.
         * @param peso valor numérico asociado a la conexión.
         * @throws IllegalArgumentException si el destino es null
         * o el peso no es positivo, NaN o infinito.
         */
        public Arista(Vertice<T> destino, double peso) {

            if (destino == null) {
                throw new IllegalArgumentException(
                        "El destino de la arista no puede ser null."
                );
            }

            validarPeso(peso);

            this.destino = destino;
            this.peso = peso;
        }

        /**
         * Obtiene el vértice de destino.
         *
         * @return vértice de destino.
         *
         * Complejidad temporal: O(1).
         */
        public Vertice<T> getDestino() {
            return destino;
        }

        /**
         * Obtiene el peso de la arista.
         *
         * @return peso de la conexión.
         *
         * Complejidad temporal: O(1).
         */
        public double getPeso() {
            return peso;
        }

        /**
         * Actualiza el peso de la arista.
         *
         * @param peso nuevo peso de la conexión.
         * @throws IllegalArgumentException si el peso no es positivo,
         * NaN o infinito.
         */
        public void setPeso(double peso) {
            validarPeso(peso);
            this.peso = peso;
        }

        /**
         * Valida que el peso sea un número finito y no negativo.
         *
         * @param peso valor que se desea validar.
         * @throws IllegalArgumentException si el valor no es válido.
         */
        private static void validarPeso(double peso) {

            if (!Double.isFinite(peso) || peso <= 0) {
                throw new IllegalArgumentException(
                        "El peso debe ser un número finito y positivo."
                );
            }
        }
    }

    /** Lista de vértices que conforman el grafo. */
    private SimpleLinkedList<Vertice<T>> vertices;

    /**
     * Construye un grafo vacío.
     *
     * Complejidad temporal: O(1).
     */
    public Grafo() {
        this.vertices = new SimpleLinkedList<>();
    }

    /**
     * Agrega un vértice al grafo si todavía no existe.
     *
     * No permite almacenar dos vértices con datos iguales.
     *
     * @param dato dato que identificará al nuevo vértice.
     * @throws IllegalArgumentException si el dato es null.
     *
     * Complejidad temporal: O(V), donde V es la cantidad de vértices.
     */
    public void agregarVertice(T dato) {

        if (dato == null) {
            throw new IllegalArgumentException(
                    "El dato del vértice no puede ser null."
            );
        }

        if (buscarVertice(dato) == null) {
            vertices.addLast(new Vertice<>(dato));
        }
    }

    /**
     * Busca un vértice mediante el dato que almacena.
     *
     * @param dato dato que identifica al vértice.
     * @return vértice encontrado o null si no existe.
     * @throws IllegalArgumentException si el dato es null.
     *
     * Complejidad temporal: O(V).
     */
    public Vertice<T> buscarVertice(T dato) {

        if (dato == null) {
            throw new IllegalArgumentException(
                    "El dato de búsqueda no puede ser null."
            );
        }

        for (Vertice<T> vertice : vertices) {
            if (Objects.equals(vertice.getDato(), dato)) {
                return vertice;
            }
        }

        return null;
    }

    /**
     * Agrega una arista dirigida entre dos vértices existentes.
     *
     * Si ya existe una arista entre el origen y el destino, acumula
     * el nuevo peso sobre el peso existente, en lugar de crear
     * una conexión duplicada.
     *
     * El grafo no crea automáticamente los vértices faltantes.
     *
     * @param origen dato del vértice de origen.
     * @param destino dato del vértice de destino.
     * @param peso peso que se agregará a la conexión.
     * @throws IllegalArgumentException si algún dato es null,
     * el peso no es positivo o no es finito.
     *
     * Complejidad temporal: O(V + A), donde A es la cantidad de
     * aristas salientes del vértice de origen.
     */
    public void agregarArista(T origen, T destino, double peso) {

        validarDato(origen);
        validarDato(destino);
        Arista.validarPeso(peso);

        Vertice<T> verticeOrigen = buscarVertice(origen);
        Vertice<T> verticeDestino = buscarVertice(destino);

        if (verticeOrigen == null || verticeDestino == null) {
            return;
        }

        Arista<T> aristaExistente = buscarArista(verticeOrigen, destino);

        if (aristaExistente != null) {

            double nuevoPeso = aristaExistente.getPeso() + peso;

            Arista.validarPeso(nuevoPeso);
            aristaExistente.setPeso(nuevoPeso);

            return;
        }

        // El acceso público devuelve una copia defensiva; la inserción debe hacerse
        // sobre la lista interna del vértice desde la propia clase Grafo.
        verticeOrigen.adyacentes.addLast(new Arista<>(verticeDestino, peso));
    }

    /**
     * Busca una arista saliente de un vértice hacia un destino.
     *
     * @param origen vértice desde el que comienza la conexión.
     * @param destino dato que identifica al destino buscado.
     * @return arista encontrada o null si no existe.
     *
     * Complejidad temporal: O(A), donde A es el número de aristas
     * salientes del vértice de origen.
     */
    private Arista<T> buscarArista(Vertice<T> origen, T destino) {

        // Acceso directo interno: getAdyacentes() entrega copias defensivas.
        for (Arista<T> arista : origen.adyacentes) {
            if (Objects.equals(arista.getDestino().getDato(), destino)) {
                return arista;
            }
        }

        return null;
    }

    /**
     * Consulta el peso acumulado de una conexión dirigida.
     *
     * @param origen dato del vértice de origen.
     * @param destino dato del vértice de destino.
     * @return peso acumulado o null si no existe la conexión.
     * @throws IllegalArgumentException si algún dato es null.
     *
     * Complejidad temporal: O(V + A).
     */
    public Double obtenerPesoArista(T origen, T destino) {

        validarDato(origen);
        validarDato(destino);

        Vertice<T> verticeOrigen = buscarVertice(origen);

        if (verticeOrigen == null) {
            return null;
        }

        Arista<T> arista = buscarArista(verticeOrigen, destino);

        return arista == null ? null : arista.getPeso();
    }

    /**
     * Reemplaza el peso de una conexión existente.
     *
     * A diferencia de agregarArista(), este método no acumula el peso:
     * establece exactamente el valor indicado.
     *
     * @param origen dato del vértice de origen.
     * @param destino dato del vértice de destino.
     * @param nuevoPeso nuevo peso de la conexión.
     * @return true si se actualizó la conexión; false si no existe.
     * @throws IllegalArgumentException si algún dato es null
     * o el peso no es válido.
     *
     * Complejidad temporal: O(V + A).
     */
    public boolean actualizarPesoArista(
            T origen,
            T destino,
            double nuevoPeso
    ) {

        validarDato(origen);
        validarDato(destino);
        Arista.validarPeso(nuevoPeso);

        Vertice<T> verticeOrigen = buscarVertice(origen);

        if (verticeOrigen == null) {
            return false;
        }

        Arista<T> arista = buscarArista(verticeOrigen, destino);

        if (arista == null) {
            return false;
        }

        arista.setPeso(nuevoPeso);

        return true;
    }

    /**
     * Elimina una conexión dirigida entre dos vértices.
     *
     * La operación no elimina los vértices, solamente la arista.
     *
     * @param origen dato del vértice de origen.
     * @param destino dato del vértice de destino.
     * @return true si se eliminó la conexión; false si no existía.
     * @throws IllegalArgumentException si algún dato es null.
     *
     * Complejidad temporal: O(V + A).
     */
    public boolean eliminarArista(T origen, T destino) {

        validarDato(origen);
        validarDato(destino);

        Vertice<T> verticeOrigen = buscarVertice(origen);

        if (verticeOrigen == null) {
            return false;
        }

        for (Arista<T> arista : verticeOrigen.adyacentes) {
            if (Objects.equals(arista.getDestino().getDato(), destino)) {
                // Se elimina de la lista real y se retorna inmediatamente.
                verticeOrigen.adyacentes.remove(arista);
                return true;
            }
        }

        return false;
    }

    /**
     * Obtiene la lista de vértices del grafo.
     *
     * Se conserva este método para mantener la compatibilidad
     * con FintechController.
     *
     * @return lista de vértices del grafo.
     *
     * Complejidad temporal: O(V), porque crea una lista defensiva de vértices.
     */
    public SimpleLinkedList<Vertice<T>> getVertices() {
        // Los vértices se pueden consultar, pero la colección interna no se expone.
        SimpleLinkedList<Vertice<T>> copia = new SimpleLinkedList<>();
        for (Vertice<T> vertice : vertices) copia.addLast(vertice);
        return copia;
    }

    /**
     * Obtiene la cantidad de vértices del grafo.
     *
     * @return número de vértices almacenados.
     *
     * Complejidad temporal: O(1).
     */
    public int getCantidadVertices() {
        return vertices.getSize();
    }

    /** Cuenta únicamente aristas con peso positivo (transferencias vigentes). */
    public int contarAristasActivas(Vertice<T> vertice) {
        if (vertice == null) return 0;
        int cantidad = 0;
        for (Arista<T> arista : vertice.adyacentes) {
            if (arista.getPeso() > 0) cantidad++;
        }
        return cantidad;
    }

    /**
     * Comprueba si el grafo no contiene vértices.
     *
     * @return true si el grafo está vacío; false en caso contrario.
     *
     * Complejidad temporal: O(1).
     */
    public boolean isEmpty() {
        return vertices.isEmpty();
    }

    /**
     * Valida que un dato utilizado en una operación no sea null.
     *
     * @param dato dato que se desea validar.
     * @throws IllegalArgumentException si el dato es null.
     */
    private void validarDato(T dato) {

        if (dato == null) {
            throw new IllegalArgumentException(
                    "El dato no puede ser null."
            );
        }
    }
}