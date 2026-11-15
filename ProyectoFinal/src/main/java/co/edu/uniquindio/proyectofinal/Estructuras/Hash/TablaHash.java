package co.edu.uniquindio.proyectofinal.Estructuras.Hash;

import java.util.Objects;

/**
 * Implementa una tabla hash genérica que almacena pares de clave y valor.
 *
 * Utiliza el método de encadenamiento para resolver colisiones. Cada
 * posición del arreglo puede contener una lista enlazada de entradas.
 *
 * La estructura permite insertar, buscar y eliminar elementos mediante
 * una clave. Si se inserta una clave que ya existe, se actualiza su valor.
 *
 * Esta implementación utiliza una capacidad fija: no realiza redimensionamiento
 * automático cuando aumenta la cantidad de elementos.
 *
 * @param <K> tipo de las claves utilizadas para identificar los elementos.
 * @param <V> tipo de los valores almacenados.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class TablaHash<K, V> {

    /**
     * Representa una entrada individual de la tabla hash.
     *
     * Cada entrada contiene una clave, un valor y una referencia
     * a la siguiente entrada de la misma posición.
     *
     * @param <K> tipo de la clave.
     * @param <V> tipo del valor.
     */
    private class Entrada<K, V> {

        /** Clave que identifica la entrada. */
        private K clave;

        /** Valor asociado a la clave. */
        private V valor;

        /** Referencia a la siguiente entrada de la lista enlazada. */
        private Entrada<K, V> siguiente;

        /**
         * Construye una entrada con la clave y el valor indicados.
         *
         * @param clave clave que identifica el elemento.
         * @param valor información asociada a la clave.
         */
        public Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }
    }

    /** Arreglo que almacena las listas de entradas. */
    private Entrada<K, V>[] tabla;

    /** Cantidad de posiciones disponibles en el arreglo. */
    private int capacidad;

    /** Cantidad actual de elementos almacenados. */
    private int tamano;

    /**
     * Construye una tabla hash con la capacidad especificada.
     *
     * @param capacidad cantidad de posiciones que tendrá el arreglo.
     * @throws IllegalArgumentException si la capacidad es menor o igual a cero.
     */
    @SuppressWarnings("unchecked")
    public TablaHash(int capacidad) {

        if (capacidad <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad de la tabla debe ser mayor que cero."
            );
        }

        this.capacidad = capacidad;
        this.tabla = (Entrada<K, V>[]) new Entrada[capacidad];
        this.tamano = 0;
    }

    /**
     * Calcula la posición del arreglo correspondiente a una clave.
     *
     * Se utiliza Math.floorMod para garantizar un índice no negativo,
     * incluso cuando el código hash de la clave es negativo.
     *
     * @param clave clave cuyo índice se desea calcular.
     * @return posición válida dentro del arreglo.
     * @throws IllegalArgumentException si la clave es null.
     *
     * Complejidad temporal: O(1), suponiendo que hashCode() es O(1).
     */
    private int obtenerIndice(K clave) {

        if (clave == null) {
            throw new IllegalArgumentException(
                    "La clave no puede ser null."
            );
        }

        return Math.floorMod(clave.hashCode(), capacidad);
    }

    /**
     * Inserta un elemento en la tabla hash.
     *
     * Si la clave ya existe, actualiza el valor asociado sin incrementar
     * el tamaño de la tabla.
     *
     * Si la clave no existe, agrega una nueva entrada al inicio de la
     * lista enlazada correspondiente.
     *
     * @param clave clave que identifica el elemento.
     * @param valor valor que se desea almacenar.
     * @throws IllegalArgumentException si la clave es null.
     *
     * Complejidad temporal:
     * - Caso promedio: O(1).
     * - Peor caso: O(n), cuando muchas claves colisionan.
     */
    public void insertar(K clave, V valor) {

        int indice = obtenerIndice(clave);

        Entrada<K, V> actual = tabla[indice];

        // Comprobar si la clave ya existe.
        while (actual != null) {

            if (Objects.equals(actual.clave, clave)) {
                actual.valor = valor;
                return;
            }

            actual = actual.siguiente;
        }

        // Crear una nueva entrada al inicio de la lista.
        Entrada<K, V> nuevaEntrada = new Entrada<>(clave, valor);

        nuevaEntrada.siguiente = tabla[indice];
        tabla[indice] = nuevaEntrada;

        tamano++;
    }

    /**
     * Busca el valor asociado a una clave.
     *
     * Recorre únicamente la lista enlazada de la posición calculada
     * mediante la función hash.
     *
     * @param clave clave del elemento que se desea buscar.
     * @return valor asociado a la clave o null si no existe.
     * @throws IllegalArgumentException si la clave es null.
     *
     * Complejidad temporal:
     * - Caso promedio: O(1).
     * - Peor caso: O(n).
     */
    public V buscar(K clave) {

        int indice = obtenerIndice(clave);

        Entrada<K, V> actual = tabla[indice];

        while (actual != null) {

            if (Objects.equals(actual.clave, clave)) {
                return actual.valor;
            }

            actual = actual.siguiente;
        }

        return null;
    }

    /**
     * Elimina una entrada identificada por su clave.
     *
     * Mantiene correctamente los enlaces de la lista, tanto si el
     * elemento se encuentra al inicio como en una posición intermedia
     * o al final.
     *
     * @param clave clave del elemento que se desea eliminar.
     * @return true si el elemento fue eliminado; false si no existía.
     * @throws IllegalArgumentException si la clave es null.
     *
     * Complejidad temporal:
     * - Caso promedio: O(1).
     * - Peor caso: O(n).
     */
    public boolean eliminar(K clave) {

        int indice = obtenerIndice(clave);

        Entrada<K, V> actual = tabla[indice];
        Entrada<K, V> anterior = null;

        while (actual != null) {

            if (Objects.equals(actual.clave, clave)) {

                // El elemento es el primero de la lista.
                if (anterior == null) {
                    tabla[indice] = actual.siguiente;
                } else {
                    // El elemento está en medio o al final.
                    anterior.siguiente = actual.siguiente;
                }

                tamano--;

                return true;
            }

            anterior = actual;
            actual = actual.siguiente;
        }

        return false;
    }

    /**
     * Obtiene la cantidad de elementos almacenados en la tabla.
     *
     * @return número actual de entradas.
     *
     * Complejidad temporal: O(1).
     */
    public int getTamano() {
        return tamano;
    }

    /**
     * Comprueba si la tabla no contiene elementos.
     *
     * @return true si está vacía; false en caso contrario.
     *
     * Complejidad temporal: O(1).
     */
    public boolean isEmpty() {
        return tamano == 0;
    }
}